package rocks.blackblock.polyvalent.item;

import net.minecraft.block.BlockState;
import net.minecraft.item.*;
import org.jetbrains.annotations.Nullable;
import rocks.blackblock.polyvalent.Polyvalent;
import rocks.blackblock.polyvalent.client.PolyvalentBlockInfo;
import rocks.blackblock.polyvalent.client.PolyvalentItemInfo;

public class PolyBlockItem extends BlockItem {

    public PolyBlockItem(Settings settings) {
        super(Polyvalent.STONE_BLOCK_ONE, settings);
    }

    /**
     * Try to get the correct placement state,
     * which will help to avoid a FOUC.
     *
     * @author   Jelle De Loecker   <jelle@elevenways.be>
     * @since    0.1.1
     *
     * @param    context   The placement context
     *
     * @return   The correct BlockState that will be placed in the world
     */
    @Nullable
    protected BlockState getPlacementState(ItemPlacementContext context) {

        ItemStack stack = context.getStack();
        PolyvalentItemInfo info = PolyvalentItemInfo.of(stack);

        if (info == null) {
            return super.getPlacementState(context);
        }

        PolyvalentBlockInfo block_info = info.getBlockInfo();

        if (block_info == null) {
            return super.getPlacementState(context);
        }

        BlockState state = block_info.getBlockState();

        if (state == null) {
            return super.getPlacementState(context);
        }

        return this.canPlace(context, state) ? state : null;
    }

}
