package rocks.blackblock.polyvalent.block;

import io.github.theepicblock.polymc.api.block.BlockStateProfile;
import io.github.theepicblock.polymc.impl.generator.BlockPolyGenerator;
import net.minecraft.block.*;
import net.minecraft.sound.BlockSoundGroup;
import net.minecraft.state.property.IntProperty;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.BlockView;
import rocks.blackblock.polyvalent.Polyvalent;

public interface PolyvalentBlock {

    // The NONCE-property will be the main attribute used to differentiate blocks.
    IntProperty NONCE = IntProperty.of("nonce", 0, 99);

    /**
     * A shortcut to always return {@code true} a context predicate, used as
     * {@code settings.solidBlock(Blocks::always)}.
     */
    private static boolean always(BlockState state, BlockView world, BlockPos pos) {
        return true;
    }

    /**
     * A shortcut to always return {@code false} a context predicate, used as
     * {@code settings.solidBlock(Blocks::never)}.
     */
    private static boolean never(BlockState state, BlockView world, BlockPos pos) {
        return false;
    }

    /**
     * Create a material block and its item
     *
     * @param name     The name of the block
     * @param material The material of the block
     *                 (e.g. Material.STONE)
     *
     */
    static PolyFullBlock createMaterialBlock(String name, Material material) {

        AbstractBlock.Settings settings = AbstractBlock.Settings.of(material);

        settings.requiresTool();
        settings.strength(3.5f, 6.0f);

        PolyFullBlock block = new PolyFullBlock(settings);

        Polyvalent.registerBlockAndItem(name, block);

        return block;
    }

    /**
     * Create a glowing block and its item
     *
     * @param name     The name of the block
     * @param material The material of the block
     *                 (e.g. Material.STONE)
     *
     */
    static PolyFullBlock createGlowBlock(String name, Material material) {

        AbstractBlock.Settings settings = AbstractBlock.Settings.of(material).luminance(blockState -> {
            return 3;
        }).postProcess((state, world, pos) -> {
            return true;
        }).emissiveLighting((state, world, pos) -> {
            return true;
        });

        settings.requiresTool();
        settings.strength(3.5f, 6.0f);

        PolyFullBlock block = new PolyFullBlock(settings);

        Polyvalent.registerBlockAndItem(name, block);

        return block;
    }

    /**
     * Create a non-opaque block and its item
     *
     * @param name     The name of the block
     * @param material The material of the block
     *                 (e.g. Material.STONE)
     *
     */
    static PolyTransparentBlock createTransparentBlock(String name, Material material) {

        AbstractBlock.Settings settings = AbstractBlock.Settings.of(material)
                .nonOpaque()
                .blockVision(PolyvalentBlock::never)
                .solidBlock(PolyvalentBlock::never);

        settings.requiresTool();
        settings.strength(2f, 6.0f);

        PolyTransparentBlock block = new PolyTransparentBlock(settings);

        Polyvalent.registerBlockAndItem(name, block);

        return block;
    }

    /**
     * Create a leaves block and its item
     *
     * @param name     The name of the block
     * @param material The material of the block
     *                 (e.g. Material.STONE)
     *
     */
    static PolyLeavesBlock createLeavesBlock(String name, Material material) {

        AbstractBlock.Settings settings = AbstractBlock.Settings.of(material)
                .nonOpaque()
                .blockVision(PolyvalentBlock::never)
                .sounds(BlockSoundGroup.GRASS)
                .suffocates(PolyvalentBlock::never);

        settings.requiresTool();
        settings.strength(2f, 6.0f);

        PolyLeavesBlock block = new PolyLeavesBlock(settings);

        Polyvalent.registerBlockAndItem(name, block);

        return block;
    }

    /**
     * Create a slab block and its item
     *
     * @param name     The name of the block
     * @param material The material of the block
     *                 (e.g. Material.STONE)
     *
     */
    static PolySlabBlock createSlabBlock(String name, Material material) {

        AbstractBlock.Settings settings = AbstractBlock.Settings.of(material);

        settings.requiresTool();
        settings.strength(3.5f, 6.0f);

        PolySlabBlock block = new PolySlabBlock(settings);

        Polyvalent.registerBlockAndItem(name, block);

        return block;
    }

    /**
     * Create a portal block and its item
     *
     * @param name     The name of the block
     * @param material The material of the block
     *                 (e.g. Material.STONE)
     *
     */
    static PolyPortalBlock createPortalBlock(String name, Material material) {

        AbstractBlock.Settings settings = AbstractBlock.Settings.of(material);

        settings.noCollision();
        settings.strength(-1.0f);
        settings.luminance(blockState -> 11);

        PolyPortalBlock block = new PolyPortalBlock(settings);

        Polyvalent.registerBlockAndItem(name, block);

        return block;
    }

    /**
     * Create a carpet block and its item
     *
     * @param name     The name of the block
     * @param material The material of the block
     *                 (e.g. Material.STONE)
     *
     */
    static PolyCarpetBlock createCarpetBlock(String name, Material material) {

        AbstractBlock.Settings settings = AbstractBlock.Settings.of(material);

        settings.strength(0.1f);

        PolyCarpetBlock block = new PolyCarpetBlock(settings);

        Polyvalent.registerBlockAndItem(name, block);

        return block;
    }

    /**
     * Create a non-collidable carpet block and its item
     *
     * @param name     The name of the block
     * @param material The material of the block
     *                 (e.g. Material.STONE)
     *
     */
    static PolyCarpetBlock createNoCollisionCarpetBlock(String name, Material material) {

        AbstractBlock.Settings settings = AbstractBlock.Settings.of(material);

        settings.noCollision();
        settings.strength(0.1f);

        PolyCarpetBlock block = new PolyCarpetBlock(settings);

        Polyvalent.registerBlockAndItem(name, block);

        return block;
    }

    /**
     * Create a non-collidable transparent carpet block and its item
     *
     * @param name     The name of the block
     * @param material The material of the block
     *                 (e.g. Material.STONE)
     *
     */
    static PolyCarpetBlock createNoCollisionTransparentCarpetBlock(String name, Material material) {

        AbstractBlock.Settings settings = AbstractBlock.Settings.of(material);

        settings.nonOpaque();
        settings.noCollision();
        settings.strength(0.1f);

        PolyCarpetBlock block = new PolyCarpetBlock(settings);

        Polyvalent.registerBlockAndItem(name, block);

        return block;
    }

    /**
     * Create a plant block
     *
     * @param name     The name of the block
     *
     */
    static PolyPlantBlock createPlantBlock(String name) {

        AbstractBlock.Settings settings = AbstractBlock.Settings.of(Material.PLANT);

        settings.noCollision();
        settings.breakInstantly();
        settings.sounds(BlockSoundGroup.CAVE_VINES);

        PolyPlantBlock block = new PolyPlantBlock(settings);

        Polyvalent.registerBlockAndItem(name, block);

        return block;
    }

    /**
     * Create a sapling block
     *
     * @param name     The name of the block
     *
     */
    static PolySaplingBlock createSaplingBlock(String name) {
        AbstractBlock.Settings settings = AbstractBlock.Settings.of(Material.PLANT);

        settings.noCollision();
        settings.breakInstantly();
        settings.sounds(BlockSoundGroup.GRASS);

        PolySaplingBlock block = new PolySaplingBlock(settings);

        Polyvalent.registerBlockAndItem(name, block);

        return block;
    }

}
