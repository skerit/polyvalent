package rocks.blackblock.polyvalent.block;

import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.block.LeavesBlock;
import net.minecraft.state.StateManager;
import net.minecraft.state.property.IntProperty;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.shape.VoxelShape;
import net.minecraft.util.shape.VoxelShapes;
import net.minecraft.world.BlockView;

public class PolyLeavesBlock extends Block implements PolyvalentBlock {

    public static final IntProperty NONCE = PolyvalentBlock.NONCE;

    protected PolyLeavesBlock(Settings settings) {
        super(settings);
        this.setDefaultState(this.getStateManager().getDefaultState().with(NONCE, 0));
    }

    @Override
    public VoxelShape getSidesShape(BlockState state, BlockView world, BlockPos pos) {
        return VoxelShapes.empty();
    }

    @Override
    public int getOpacity(BlockState state, BlockView world, BlockPos pos) {
        return 1;
    }

    @Override
    protected void appendProperties(StateManager.Builder<Block, BlockState> builder) {
        builder.add(NONCE);
    }
}
