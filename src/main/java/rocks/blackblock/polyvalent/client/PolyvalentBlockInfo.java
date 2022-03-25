package rocks.blackblock.polyvalent.client;

import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.item.ItemStack;
import net.minecraft.util.Identifier;
import net.minecraft.util.Language;
import net.minecraft.util.math.BlockPos;
import rocks.blackblock.polyvalent.Polyvalent;
import rocks.blackblock.polyvalent.PolyvalentClient;

/**
 * Info on a Polyvalent block
 *
 * @author   Jelle De Loecker   <jelle@elevenways.be>
 * @since    0.1.1
 */
public class PolyvalentBlockInfo {

    // The client-side state id
    public final int state_id;

    // The block's server-side identifier
    public final Identifier identifier;

    // The translated title
    private String title = null;

    // The itemstack representation
    private ItemStack stack = null;

    /**
     * Construct the PolyvalentBlockInfo instance
     *
     * @author   Jelle De Loecker   <jelle@elevenways.be>
     * @since    0.1.1
     */
    public PolyvalentBlockInfo(int state_id, String identifier) {
        this.state_id = state_id;
        this.identifier = Identifier.tryParse(identifier);
    }

    /**
     * Construct the path of this Block's name
     * where we expect its translation to be
     *
     * @author   Jelle De Loecker   <jelle@elevenways.be>
     * @since    0.1.1
     */
    public String getTranslationPath() {
        // Get the block's translation path
        return "block." + this.identifier.getNamespace() + "." + this.identifier.getPath();
    }

    /**
     * Get the (translated) title of this block
     *
     * @author   Jelle De Loecker   <jelle@elevenways.be>
     * @since    0.1.1
     */
    public String getTitle() {

        if (this.title == null) {
            this.title = Language.getInstance().get(this.getTranslationPath());
        }

        return this.title;
    }

    /**
     * Get the client-side ItemStack representation of this block
     *
     * @author   Jelle De Loecker   <jelle@elevenways.be>
     * @since    0.1.1
     */
    public ItemStack getItemStack() {

        if (this.stack == null) {
            PolyvalentItemInfo item_info = PolyvalentClient.itemInfoById.get(this.identifier);

            Polyvalent.log("Item info of " + this.identifier + ": " + item_info);

            if (item_info != null) {
                this.stack = item_info.getItemStack();
            }
        }

        return this.stack;

    }

    /**
     * Get the PolyvalentBlockInfo instance of the block at the given position.
     *
     * @param    pos   The position of the block in the current world
     *
     * @return   The PolyvalentBlockInfo instance of the block at the given position
     */
    public static PolyvalentBlockInfo getBlockInfoAt(BlockPos pos) {
        BlockState block_state = InternalClientRegistry.getBlockAt(pos);

        // Get blockstate id
        int state_id = Block.STATE_IDS.getRawId(block_state);

        return PolyvalentClient.blockInfo.get(state_id);
    }
}
