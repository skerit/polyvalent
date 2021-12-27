package rocks.blackblock.polyvalent.block;

import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.block.SlabBlock;
import net.minecraft.block.enums.SlabType;
import net.minecraft.state.StateManager;
import net.minecraft.state.property.IntProperty;

public class PolySlabBlock extends SlabBlock implements PolyvalentBlock {

    public static final IntProperty NONCE = PolyvalentBlock.NONCE;

    public PolySlabBlock(Settings settings) {
        super(settings);
        this.setDefaultState((BlockState)((BlockState)this.getDefaultState().with(TYPE, SlabType.BOTTOM)).with(WATERLOGGED, false).with(NONCE, 0));
    }

    @Override
    protected void appendProperties(StateManager.Builder<Block, BlockState> builder) {
        builder.add(NONCE, TYPE, WATERLOGGED);
    }
}
