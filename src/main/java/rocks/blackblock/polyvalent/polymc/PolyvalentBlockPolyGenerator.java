package rocks.blackblock.polyvalent.polymc;

import io.github.theepicblock.polymc.PolyMc;
import io.github.theepicblock.polymc.api.PolyRegistry;
import io.github.theepicblock.polymc.api.block.BlockPoly;
import io.github.theepicblock.polymc.api.block.BlockStateManager;
import io.github.theepicblock.polymc.api.block.BlockStateProfile;
import io.github.theepicblock.polymc.impl.generator.BlockPolyGenerator;
import io.github.theepicblock.polymc.impl.misc.BooleanContainer;
import io.github.theepicblock.polymc.impl.poly.block.FunctionBlockStatePoly;
import io.github.theepicblock.polymc.impl.poly.block.SimpleReplacementPoly;
import net.minecraft.block.*;
import net.minecraft.tag.BlockTags;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.shape.VoxelShape;
import net.minecraft.util.shape.VoxelShapes;
import rocks.blackblock.polyvalent.PolyvalentServer;

public class PolyvalentBlockPolyGenerator {

    /**
     * Generates the most suitable {@link BlockPoly} and directly adds it to the {@link PolyRegistry}
     * @see #generatePoly(Block, PolyRegistry)
     */
    public static void addBlockToBuilder(Block block, PolyRegistry builder) {
        try {
            builder.registerBlockPoly(block, generatePoly(block, builder));
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
    public static BlockPoly generatePoly(Block block, PolyRegistry registry) {
        return new FunctionBlockStatePoly(block, (state, isUniqueCallback) -> registerClientState(state, isUniqueCallback, registry.getSharedValues(BlockStateManager.KEY)));
    }

    /**
     * Generates the most suitable {@link BlockPoly} for a given {@link Block}
     */
    public static BlockState registerClientState(BlockState state, BooleanContainer isUniqueCallback, BlockStateManager manager) {

        Block block = state.getBlock();
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

        // === LEAVES ===
        if (block instanceof LeavesBlock || state.isIn(BlockTags.LEAVES)) { //TODO I don't like that leaves can be set tags in datapacks, it might cause issues. However, as not every leaf block extends LeavesBlock I can't see much of a better option. Except to maybe check the id if it ends on "_leaves"
            try {
                isUniqueCallback.set(true);
                return manager.requestBlockState(BlockStateProfile.LEAVES_PROFILE);
                //return new SingleUnusedBlockStatePoly(builder, PolyvalentServer.LEAVES_BLOCK_PROFILE);
            } catch (BlockStateManager.StateLimitReachedException ignored) {}
        }

        //=== FULL BLOCKS ===
        if (Block.isShapeFullCube(collisionShape)) {

            try {
                if (state.hasEmissiveLighting(fakeWorld, BlockPos.ORIGIN)) {
                    isUniqueCallback.set(true);
                    return manager.requestBlockState(PolyvalentServer.GLOW_BLOCK_PROFILE);
                }
            } catch (Exception e) {
                // Ignore
            }

            try {
                if (!state.isOpaque()) {
                    isUniqueCallback.set(true);
                    return manager.requestBlockState(PolyvalentServer.GLASS_BLOCK_PROFILE);
                }
            } catch (Exception e) {
                // Ignore
            }

            try {
                if (material.equals(Material.WOOD)) {
                    isUniqueCallback.set(true);
                    return manager.requestBlockState(PolyvalentServer.WOOD_BLOCK_PROFILE);
                }
            } catch (Exception e) {
                // Ignore
            }

            try {
                if (material.equals(Material.STONE)) {
                    isUniqueCallback.set(true);
                    return manager.requestBlockState(PolyvalentServer.STONE_BLOCK_PROFILE);
                }
            } catch (Exception e) {
                // Ignore
            }

            try {
                isUniqueCallback.set(true);
                return manager.requestBlockState(PolyvalentServer.STONE_BLOCK_PROFILE);
            } catch (BlockStateManager.StateLimitReachedException ignored) {
                System.out.println("Failed to generate a polyvalent block " + ignored);
            } catch (Exception e) {
                System.out.println("Failed to generate a polyvalent block " + e);
                e.printStackTrace();
            }
        }

        // Fallback!
        return BlockPolyGenerator.registerClientState(state, isUniqueCallback, manager);
    }
}
