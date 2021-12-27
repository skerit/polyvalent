package rocks.blackblock.polyvalent.block;

import io.github.theepicblock.polymc.api.block.BlockStateProfile;
import io.github.theepicblock.polymc.impl.generator.BlockPolyGenerator;
import net.minecraft.block.AbstractBlock;
import net.minecraft.block.Block;
import net.minecraft.block.Blocks;
import net.minecraft.block.Material;
import net.minecraft.state.property.IntProperty;
import rocks.blackblock.polyvalent.Polyvalent;

public interface PolyvalentBlock {

    // The NONCE-property will be the main attribute used to differentiate blocks.
    IntProperty NONCE = IntProperty.of("nonce", 0, 255);

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

        AbstractBlock.Settings settings = AbstractBlock.Settings.of(material).nonOpaque().blockVision((state, world, pos) -> false).solidBlock((state, world, pos) -> false);

        PolyTransparentBlock block = new PolyTransparentBlock(settings);

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
