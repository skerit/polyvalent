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
    IntProperty NONCE = IntProperty.of("nonce", 0, 999);

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

        PolyFullBlock block = new PolyFullBlock(AbstractBlock.Settings.of(material));

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

        PolySlabBlock block = new PolySlabBlock(settings);

        Polyvalent.registerBlockAndItem(name, block);

        return block;
    }
}
