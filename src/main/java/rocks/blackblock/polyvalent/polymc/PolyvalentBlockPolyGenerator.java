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
import net.minecraft.state.property.Property;
import net.minecraft.tag.BlockTags;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import net.minecraft.util.shape.VoxelShape;
import net.minecraft.util.shape.VoxelShapes;
import rocks.blackblock.polyvalent.Polyvalent;
import rocks.blackblock.polyvalent.PolyvalentServer;
import rocks.blackblock.polyvalent.block.PolyPortalBlock;

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
    public static BlockState registerClientState(BlockState moddedState, BooleanContainer isUniqueCallback, BlockStateManager manager) {

        Block block = moddedState.getBlock();
        Material material = null;
        BlockPolyGenerator.FakedWorld fakeWorld = new BlockPolyGenerator.FakedWorld(moddedState);

        //Get the block's collision shape.
        VoxelShape collisionShape;
        try {
            collisionShape = moddedState.getCollisionShape(fakeWorld, BlockPos.ORIGIN);
        } catch (Exception e) {
            PolyMc.LOGGER.warn("Failed to get collision shape for " + block.getTranslationKey());
            e.printStackTrace();
            collisionShape = VoxelShapes.UNBOUNDED;
        }

        try {
            material = moddedState.getMaterial();
        } catch (Exception e) {
            PolyMc.LOGGER.warn("Failed to get material for " + block.getTranslationKey());
        }

        if (material != null && material == Material.PORTAL) {
            try {
                isUniqueCallback.set(true);
                return manager.requestBlockState(PolyvalentServer.PORTAL_PROFILE.and(
                        state -> {

                            // Get the polyblock's axis state
                            Direction.Axis poly_axis = state.get(PolyPortalBlock.AXIS);
                            Direction.Axis mod_axis = null;

                            try {
                                // Try to get the X-Y-Z axis
                                mod_axis = moddedState.get(PolyPortalBlock.AXIS);
                            } catch (Exception e) {
                                // Ignore
                            }

                            if (mod_axis == null) {
                                mod_axis = moddedState.get(PolyPortalBlock.HORIZONTAL_AXIS);
                            }

                            return poly_axis == mod_axis;
                        }
                ));
            } catch (BlockStateManager.StateLimitReachedException ignored) {}
        }

        // Carpets
        if (block instanceof CarpetBlock) {

            if (collisionShape.isEmpty()) {
                try {
                    isUniqueCallback.set(true);
                    return manager.requestBlockState(PolyvalentServer.NO_COLLISION_CARPET_PROFILE);
                } catch (BlockStateManager.StateLimitReachedException ignored) {
                    Polyvalent.log("Failed to register a carpet poly for " + block.getTranslationKey());
                    ignored.printStackTrace();
                }
            } else {
                try {
                    isUniqueCallback.set(true);
                    return manager.requestBlockState(PolyvalentServer.CARPET_PROFILE);
                } catch (BlockStateManager.StateLimitReachedException ignored) {
                    Polyvalent.log("Failed to register a carpet poly for " + block.getTranslationKey());
                    ignored.printStackTrace();
                }
            }
        }

        //=== SLABS ===
        if (block instanceof SlabBlock) {
            try {
                isUniqueCallback.set(true);
                return manager.requestBlockState(PolyvalentServer.SLAB_PROFILE.and(
                        state -> propertyMatches(state, moddedState, SlabBlock.WATERLOGGED, SlabBlock.TYPE)
                ));
            } catch (BlockStateManager.StateLimitReachedException ignored) {}
        }

        // === LEAVES ===
        if (block instanceof LeavesBlock || moddedState.isIn(BlockTags.LEAVES)) { //TODO I don't like that leaves can be set tags in datapacks, it might cause issues. However, as not every leaf block extends LeavesBlock I can't see much of a better option. Except to maybe check the id if it ends on "_leaves"
            try {
                isUniqueCallback.set(true);
                return manager.requestBlockState(BlockStateProfile.LEAVES_PROFILE);
                //return new SingleUnusedBlockStatePoly(builder, PolyvalentServer.LEAVES_BLOCK_PROFILE);
            } catch (BlockStateManager.StateLimitReachedException ignored) {}
        }

        //=== FULL BLOCKS ===
        if (Block.isShapeFullCube(collisionShape)) {

            try {
                if (moddedState.hasEmissiveLighting(fakeWorld, BlockPos.ORIGIN)) {
                    isUniqueCallback.set(true);
                    return manager.requestBlockState(PolyvalentServer.GLOW_BLOCK_PROFILE);
                }
            } catch (Exception e) {
                // Ignore
            }

            try {
                if (!moddedState.isOpaque()) {
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
        return BlockPolyGenerator.registerClientState(moddedState, isUniqueCallback, manager);
    }

    public static boolean propertyMatches(BlockState a, BlockState b, Property<?>... properties) {
        for (var property : properties) {
            if (!propertyMatches(a, b, property)) return false;
        }
        return true;
    }

    public static <T extends Comparable<T>> boolean propertyMatches(BlockState a, BlockState b, Property<T> property) {
        return a.get(property) == b.get(property);
    }
}
