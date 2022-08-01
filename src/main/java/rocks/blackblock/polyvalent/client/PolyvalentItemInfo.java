package rocks.blackblock.polyvalent.client;

import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.util.Identifier;
import net.minecraft.util.Language;
import net.minecraft.util.registry.Registry;
import rocks.blackblock.polyvalent.Polyvalent;
import rocks.blackblock.polyvalent.PolyvalentClient;

/**
 * Info on a Polyvalent item
 *
 * @author   Jelle De Loecker   <jelle@elevenways.be>
 * @since    0.1.1
 */
public class PolyvalentItemInfo {

    // The type of this item
    public final int type_nr;

    // The id of this item
    public final int raw_client_id;

    // The identifier of the server-side item this is representing
    public final Identifier identifier;

    // The identifier of the item on the client-side
    public final Identifier poly_identifier;

    // THe CustomModelData number of this item (if any)
    public final Integer custom_model_data;

    // The namespace of this item
    private String namespace;

    // The title of this item
    private String title = null;

    // Is this a block item?
    public final boolean is_block_item;

    // If it's a block item, which block is it for
    private Identifier block_identifier = null;

    /**
     * Construct the PolyvalentItemInfo instance
     *
     * @author   Jelle De Loecker   <jelle@elevenways.be>
     * @since    0.1.1
     *
     * @param    data    The data of this item (in an NbtCompound instance)
     */
    public PolyvalentItemInfo(NbtCompound data) {

        // The Polyvalent numerical type designation
        this.type_nr = data.getInt("t");

        // Is this a block item?
        this.is_block_item = this.type_nr == 2;

        if (this.is_block_item) {
            this.block_identifier = Identifier.tryParse(data.getString("block"));
        }

        // The raw client id
        this.raw_client_id = data.getInt("id");

        // Construct the identifier
        this.identifier = new Identifier(data.getString("ns"), data.getString("path"));
        this.namespace = this.identifier.getNamespace();

        if (data.contains("poly")) {
            this.poly_identifier = Identifier.tryParse(data.getString("poly"));
        } else {
            this.poly_identifier = null;
        }

        if (data.contains("cmd")) {
            this.custom_model_data = data.getInt("cmd");
        } else {
            this.custom_model_data = null;
        }
    }

    /**
     * Get the ItemInfo of the given stack
     *
     * @author   Jelle De Loecker   <jelle@elevenways.be>
     * @since    0.1.1
     *
     * @param    stack    The client-side stack
     */
    public static PolyvalentItemInfo of(ItemStack stack) {

        if (stack == null) {
            return null;
        }

        Item item = stack.getItem();

        if (item == null) {
            return null;
        }

        NbtCompound nbt = stack.getNbt();

        if (nbt == null || !nbt.contains(Polyvalent.POLY_MC_ORIGINAL)) {
            return null;
        }

        NbtCompound original = nbt.getCompound(Polyvalent.POLY_MC_ORIGINAL);
        String item_id = original.getString("id");

        if (item_id == null) {
            return null;
        }

        Identifier identifier = Identifier.tryParse(item_id);

        if (identifier == null) {
            return null;
        }

        return PolyvalentClient.itemInfoById.get(identifier);
    }

    /**
     * Get the block identifier, if it's a BlockItem
     *
     * @author   Jelle De Loecker   <jelle@elevenways.be>
     * @since    0.1.1
     */
    public Identifier getBlockIdentifier() {
        return this.block_identifier;
    }

    /**
     * Try to get the block info
     *
     * @author   Jelle De Loecker   <jelle@elevenways.be>
     * @since    0.1.1
     */
    public PolyvalentBlockInfo getBlockInfo() {

        if (this.block_identifier == null) {
            return null;
        }

        if (PolyvalentClient.defaultBlockInfo.containsKey(this.block_identifier)) {
            return PolyvalentClient.defaultBlockInfo.get(this.block_identifier);
        }

        // If for some reason no default id was found, iterate over all the blocks
        for (PolyvalentBlockInfo info : PolyvalentClient.blockInfo.values()) {

            if (info.identifier.equals(this.block_identifier)) {
                return info;
            }
        }

        return null;
    }

    /**
     * Construct the path of this Item's name
     * where we expect its translation to be
     *
     * @author   Jelle De Loecker   <jelle@elevenways.be>
     * @since    0.1.1
     */
    public String getTranslationPath() {

        String result;

        if (this.is_block_item) {
            result = "block.";
        } else {
            result = "item.";
        }

        // Get the item's translation path
        return result + this.identifier.getNamespace() + "." + this.identifier.getPath();
    }

    /**
     * Construct a dummy ItemStack
     *
     * @author   Jelle De Loecker   <jelle@elevenways.be>
     * @since    0.1.1
     */
    public ItemStack getItemStack() {

        if (this.poly_identifier == null) {
            return null;
        }

        Item poly_item = Registry.ITEM.get(this.poly_identifier);

        if (poly_item != null) {
            ItemStack stack = new ItemStack(poly_item);
            stack.setCount(1);
            NbtCompound nbt = stack.getOrCreateNbt();

            if (this.custom_model_data != null) {
                nbt.putInt("CustomModelData", this.custom_model_data);
            }

            NbtCompound original = new NbtCompound();
            original.putString("id", this.identifier.toString());
            original.putByte("Count", (byte) 1);
            nbt.put(Polyvalent.POLY_MC_ORIGINAL, original);

            return stack;
        }

        return null;
    }

    /**
     * Get the mod name of this item
     *
     * @author   Jelle De Loecker   <jelle@elevenways.be>
     * @since    0.1.1
     */
    public String getModName() {

        if (Language.getInstance().hasTranslation(this.namespace)) {
            return Language.getInstance().get(this.namespace);
        }

        return this.namespace.substring(0, 1).toUpperCase() + this.namespace.substring(1);
    }

    /**
     * Get the name of this item
     *
     * @author   Jelle De Loecker   <jelle@elevenways.be>
     * @since    0.1.1
     */
    public String getTitle() {
        return this.getTitle(null);
    }

    /**
     * Get the name of this item
     *
     * @author   Jelle De Loecker   <jelle@elevenways.be>
     * @since    0.1.1
     */
    public String getTitle(ItemStack stack) {

        // If the stack has a custom name, see if it's actually the item's name
        /*
        if (stack != null && stack.hasCustomName()) {
            NbtCompound nbt = stack.getNbt();

            if (nbt != null && nbt.contains(Polyvalent.POLY_MC_ORIGINAL)) {
                NbtCompound original = nbt.getCompound(Polyvalent.POLY_MC_ORIGINAL);
                String named = nbt.getString

                if (original.contains("display")) {
                    NbtCompound display = original.getCompound("display");
                    String custom_name = display.getString("Name");
                }
            }
        }*/

        if (this.title == null) {
            this.title = Language.getInstance().get(this.getTranslationPath());
        }

        if (this.title == null) {
            return this.identifier.getPath();
        }

        return this.title;
    }

    /**
     * Return a string representation of this Flow.
     *
     * @author   Jelle De Loecker   <jelle@elevenways.be>
     * @since    0.2.0
     */
    @Override
    public String toString() {
        String result = "PolyvalentItemInfo{id='" + this.identifier + "',poly='" + this.poly_identifier + "',raw_id='" + this.raw_client_id + "'";

        if (this.is_block_item) {
            result += ",block='" + this.block_identifier + "'";
        }

        return result += "}";
    }

}
