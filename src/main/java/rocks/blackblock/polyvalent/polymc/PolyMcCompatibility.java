package rocks.blackblock.polyvalent.polymc;

import io.github.theepicblock.polymc.api.PolyMcEntrypoint;
import io.github.theepicblock.polymc.api.PolyRegistry;
import io.github.theepicblock.polymc.impl.poly.block.SimpleReplacementPoly;
import net.minecraft.block.Block;
import net.minecraft.block.Blocks;
import rocks.blackblock.polyvalent.block.PolyvalentBlock;

public class PolyMcCompatibility implements PolyMcEntrypoint {

    SimpleReplacementPoly STONE = new SimpleReplacementPoly(Blocks.STONE.getDefaultState());

    @Override
    public void registerPolys(PolyRegistry registry) {

        // Ignore our own Polyvalent registry, of course!
        if (registry instanceof PolyvalentRegistry) {
            return;
        }

        for (Block block : PolyvalentBlock.ALL_POLYVALENT_BLOCKS) {
            registry.registerBlockPoly(block, STONE);
        }
    }
}
