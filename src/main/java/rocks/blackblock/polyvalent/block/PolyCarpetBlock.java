package rocks.blackblock.polyvalent.block;

import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.block.CarpetBlock;
import net.minecraft.state.StateManager;
import net.minecraft.state.property.IntProperty;

public class PolyCarpetBlock extends CarpetBlock implements PolyvalentBlock {

    public static final IntProperty NONCE = PolyvalentBlock.NONCE;

    public PolyCarpetBlock(Settings settings) {
        super(settings);
        this.setDefaultState(this.getStateManager().getDefaultState().with(NONCE, 0));
    }

    @Override
    protected void appendProperties(StateManager.Builder<Block, BlockState> builder) {
        builder.add(NONCE);
    }
}
