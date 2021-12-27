package rocks.blackblock.polyvalent.polymc;

import io.github.theepicblock.polymc.PolyMc;
import io.github.theepicblock.polymc.api.PolyRegistry;
import io.github.theepicblock.polymc.api.block.BlockPoly;
import io.github.theepicblock.polymc.api.block.BlockStateManager;
import io.github.theepicblock.polymc.api.block.BlockStateProfile;
import io.github.theepicblock.polymc.impl.generator.BlockPolyGenerator;
import io.github.theepicblock.polymc.impl.poly.block.PropertyFilteringUnusedBlocksStatePoly;
import io.github.theepicblock.polymc.impl.poly.block.SimpleReplacementPoly;
import io.github.theepicblock.polymc.impl.poly.block.UnusedBlockStatePoly;
import net.minecraft.block.*;
import net.minecraft.state.property.Properties;
import net.minecraft.state.property.Property;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.shape.VoxelShape;
import net.minecraft.util.shape.VoxelShapes;
import rocks.blackblock.polyvalent.Polyvalent;
import rocks.blackblock.polyvalent.PolyvalentServer;
import rocks.blackblock.polyvalent.block.PolySlabBlock;
import rocks.blackblock.polyvalent.block.PolyvalentBlock;

import java.util.ArrayList;

public class PolyvalentBlockPolyGenerator {
    /**
     * Generates the most suitable {@link BlockPoly} and directly adds it to the {@link PolyRegistry}
     * @see #generatePoly(Block, PolyRegistry)
     */
    public static void addBlockToBuilder(Block block, PolyRegistry builder) {
        try {
            System.out.println("Adding block to builder: " + block);
            BlockPoly poly = generatePoly(block, builder);

            if (poly == null) {
                System.out.println(" -- Poly is null!");
            }

            builder.registerBlockPoly(block, poly);
        } catch (Exception e) {
            PolyMc.LOGGER.error("Failed to generate a poly for block " + block.getTranslationKey());
            e.printStackTrace();
            PolyMc.LOGGER.error("Attempting to recover by using a default poly. Please report this");
            builder.registerBlockPoly(block, new SimpleReplacementPoly(Blocks.RED_STAINED_GLASS));
        }
    }

    /**
     * Generates the most suitable {@link BlockPoly} for a given {@link Block}
     */
    public static BlockPoly generatePoly(Block block, PolyRegistry builder) {

        BlockState state = block.getDefaultState();
        Material material = null;
        BlockPolyGenerator.FakedWorld fakeWorld = new BlockPolyGenerator.FakedWorld(state);

        //Get the block's collision shape.
        VoxelShape collisionShape;
        try {
            collisionShape = state.getCollisionShape(fakeWorld, BlockPos.ORIGIN);
        } catch (Exception e) {
            PolyMc.LOGGER.warn("Failed to get collision shape for " + block.getTranslationKey());
            e.printStackTrace();
            collisionShape = VoxelShapes.UNBOUNDED;
        }

        try {
            material = state.getMaterial();
        } catch (Exception e) {
            PolyMc.LOGGER.warn("Failed to get material for " + block.getTranslationKey());
        }

        System.out.println("  Collision shape: " + collisionShape);

        //=== FULL BLOCKS ===
        if (Block.isShapeFullCube(collisionShape)) {

            try {
                if (state.hasEmissiveLighting(fakeWorld, BlockPos.ORIGIN)) {
                    return new UnusedBlockStatePoly(block, builder, PolyvalentServer.GLOW_BLOCK_PROFILE);
                }
            } catch (Exception e) {
                // Ignore
            }

            try {
                if (!state.isOpaque()) {
                    return new UnusedBlockStatePoly(block, builder, PolyvalentServer.GLASS_BLOCK_PROFILE);
                }
            } catch (Exception e) {
                // Ignore
            }

            try {
                if (material.equals(Material.WOOD)) {
                    return new UnusedBlockStatePoly(block, builder, PolyvalentServer.WOOD_BLOCK_PROFILE);
                }
            } catch (Exception e) {
                // Ignore
            }

            try {
                if (material.equals(Material.STONE)) {
                    return new UnusedBlockStatePoly(block, builder, PolyvalentServer.STONE_BLOCK_PROFILE);
                }
            } catch (Exception e) {
                // Ignore
            }

            try {
                return new UnusedBlockStatePoly(block, builder, PolyvalentServer.STONE_BLOCK_PROFILE);
            } catch (BlockStateManager.StateLimitReachedException ignored) {
                System.out.println("Failed to generate a polyvalent block " + ignored);
            } catch (Exception e) {
                System.out.println("Failed to generate a polyvalent block " + e);
                e.printStackTrace();
            }
        }

        if (block instanceof SlabBlock) {
            try {
                /*
                ArrayList<Property<?>> properties = new ArrayList<>();
                properties.add(Properties.SLAB_TYPE);
                properties.add(Properties.WATERLOGGED);
                 */

                Property<?>[] properties = new Property[]{Properties.SLAB_TYPE, Properties.WATERLOGGED};

                return new PropertyRetainingUnusedBlocksStatePoly(block, builder, Polyvalent.SLAB_BLOCK, properties);
            } catch (Exception e) {
                // Ignore
                System.out.println("Failed to generate a slab block " + e);
                e.printStackTrace();
            }
        }

        System.out.println("  Falling back!");

        // Fallback!
        return BlockPolyGenerator.generatePoly(block, builder);
    }
}
