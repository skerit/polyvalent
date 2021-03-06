package rocks.blackblock.polyvalent.client;

import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.text.Style;
import net.minecraft.text.TranslatableText;
import net.minecraft.util.Formatting;
import net.minecraft.util.Identifier;
import net.minecraft.util.Language;
import net.minecraft.util.math.BlockPos;
import rocks.blackblock.polyvalent.Polyvalent;
import rocks.blackblock.polyvalent.PolyvalentClient;
import rocks.blackblock.polyvalent.utils.Translations;

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

    // The item info of this block
    private PolyvalentItemInfo item_info = null;

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
     * Get the client-side item info
     *
     * @author   Jelle De Loecker   <jelle@elevenways.be>
     * @since    0.1.1
     */
    public PolyvalentItemInfo getItemInfo() {

        if (this.item_info != null) {
            return this.item_info;
        }

        PolyvalentItemInfo item_info = PolyvalentClient.itemInfoById.get(this.identifier);

        // If the item info is not found, see if an alias item is defined in the translations
        if (item_info == null) {
            String key = "alias." + this.identifier.getNamespace() + "." + this.identifier.getPath();
            String alias = Translations.getIfTranslated(key);

            if (alias != null) {
                item_info = PolyvalentClient.itemInfoById.get(Identifier.tryParse(alias));
            }
        }

        this.item_info = item_info;

        return this.item_info;
    }

    /**
     * Get the client-side ItemStack representation of this block
     *
     * @author   Jelle De Loecker   <jelle@elevenways.be>
     * @since    0.1.1
     */
    public ItemStack getItemStack() {

        PolyvalentItemInfo item_info = this.getItemInfo();

        if (item_info != null) {
            return item_info.getItemStack();
        }

        return null;
    }

    /**
     * Get the client-side ItemStack representation of this block
     * for use as an icon
     *
     * @author   Jelle De Loecker   <jelle@elevenways.be>
     * @since    0.1.1
     */
    public ItemStack getItemStackForIcon() {

        if (this.stack == null) {
            this.stack = this.getItemStack();
        }

        return this.stack;
    }

    /**
     * Get the client-side ItemStack representation of this block
     *
     * @author   Jelle De Loecker   <jelle@elevenways.be>
     * @since    0.1.1
     */
    public ItemStack getItemStack(boolean for_creative) {

        ItemStack stack = this.getItemStack();

        if (stack == null) {
            return null;
        }

        if (for_creative) {
            stack.setCount(1);
            NbtCompound nbt = stack.getOrCreateNbt();
            NbtCompound polymc = new NbtCompound();
            polymc.putString("id", this.identifier.toString());
            polymc.putByte("Count", (byte) 1);
            nbt.put(Polyvalent.POLY_MC_ORIGINAL, polymc);

            TranslatableText title = new TranslatableText(this.getTranslationPath());
            title.getWithStyle(Style.EMPTY.withItalic(false).withColor(Formatting.WHITE));
            stack.setCustomName(title);
        }

        return stack;
    }

    /**
     * Get the default blockstate
     *
     * @author   Jelle De Loecker   <jelle@elevenways.be>
     * @since    0.1.1
     */
    public BlockState getBlockState() {
        return Block.STATE_IDS.get(this.state_id);
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
