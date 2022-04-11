package rocks.blackblock.polyvalent;

import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.blockrenderlayer.v1.BlockRenderLayerMap;
import net.fabricmc.fabric.api.client.rendering.v1.ColorProviderRegistry;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.client.render.RenderLayer;
import net.minecraft.item.Item;
import net.minecraft.network.PacketByteBuf;
import net.minecraft.util.Identifier;
import net.minecraft.util.registry.Registry;
import rocks.blackblock.polyvalent.block.PolyvalentBlock;
import rocks.blackblock.polyvalent.client.PolyvalentBlockInfo;
import rocks.blackblock.polyvalent.client.PolyvalentItemInfo;
import rocks.blackblock.polyvalent.networking.ModPacketsS2C;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.Random;

/**
 * The client-side Polyvalent class
 *
 * @author   Jelle De Loecker   <jelle@elevenways.be>
 * @since    0.1.0
 */
public class PolyvalentClient implements ClientModInitializer {

    public static HashMap<Integer, Identifier> actualBlockIdentifiers = new HashMap<>();
    public static HashMap<Integer, Identifier> actualItemIdentifiers = new HashMap<>();
    public static HashMap<Integer, PolyvalentBlockInfo> blockInfo = new HashMap<>();
    public static HashMap<Integer, PolyvalentItemInfo> itemInfo = new HashMap<>();
    public static HashMap<Identifier, PolyvalentItemInfo> itemInfoById = new HashMap<>();

    public static boolean connectedToPolyvalentServer = false;

    /**
     * Reset the Polyvalent state
     * (when disconnecting from a Polyvalent server, for example)
     *
     * @author   Jelle De Loecker   <jelle@elevenways.be>
     * @since    0.1.0
     */
    public static void reset() {
        connectedToPolyvalentServer = false;
        actualBlockIdentifiers.clear();
        actualItemIdentifiers.clear();
        blockInfo.clear();
        itemInfoById.clear();
    }

    /**
     * Mark us as being connected to a Polyvalent server
     *
     * @author   Jelle De Loecker   <jelle@elevenways.be>
     * @since    0.1.0
     *
     * @param    connected_to_polyvalent_server   True if the server is Polyvalent
     */
    public static void markAsPolyvalentServer(boolean connected_to_polyvalent_server) {
        reset();
        connectedToPolyvalentServer = connected_to_polyvalent_server;
    }

    /**
     * Write all raw block ids and their state name to a PacketByteBuf buffer.
     * These will let the server know which polyvalent blocks it can use.
     *
     * @author   Jelle De Loecker   <jelle@elevenways.be>
     * @since    0.1.0
     *
     * @param    buffer   The buffer that will be written to
     */
    public static void writeBlockStateRawIds(PacketByteBuf buffer) {

        ArrayList<BlockState> polyvalent_states = new ArrayList<>();

        // Iterate over all the registered blockstates
        // and extract the ones that are polyvalent
        for (BlockState state : Block.STATE_IDS) {
            Block block = state.getBlock();

            if (block instanceof PolyvalentBlock) {
                polyvalent_states.add(state);
            }
        }

        // Write the amount of polyvalent states to the buffer, just for info
        // (We won't write each state to the buffer anymore)
        buffer.writeVarInt(polyvalent_states.size());

        Polyvalent.log("There are " + polyvalent_states.size() + " client-side polyvalent states");

        HashMap<String, Integer> block_state_count = new HashMap<>();
        HashMap<String, Integer> block_state_start = new HashMap<>();

        // Iterate over all the extracted polyvalent states
        for (BlockState state : polyvalent_states) {

            // Get the client-side raw id
            int raw_id = Block.STATE_IDS.getRawId(state);

            Block block = state.getBlock();
            Identifier id = Registry.BLOCK.getId(block);
            String name = id.toString();

            Integer count = block_state_count.getOrDefault(name, 0);
            count++;
            block_state_count.put(name, count);

            Integer start_id = block_state_start.getOrDefault(name, null);

            if (start_id == null) {
                start_id = raw_id;
            } else if (start_id > raw_id) {
                start_id = raw_id;
            }

            block_state_start.put(name, start_id);
        }

        for (String name : block_state_count.keySet()) {
            Polyvalent.log("Block " + name + " has " + block_state_count.get(name) + " states");
            Integer count = block_state_count.get(name);
            Integer start_id = block_state_start.get(name);

            buffer.writeString(name);
            buffer.writeVarInt(count);
            buffer.writeVarInt(start_id);
        }

        Polyvalent.log("Buffer is now: " + buffer.readableBytes() + " bytes");
    }

    /**
     * Write all raw item ids to a PacketByteBuf buffer.
     * These will let the server know which polyvalent items it can use.
     *
     * @author   Jelle De Loecker   <jelle@elevenways.be>
     * @since    0.1.0
     *
     * @param    buffer   The buffer that will be written to
     */
    public static void writeItemRawIds(PacketByteBuf buffer) {

        // Write the amount of Polyvalent items to the buffer
        buffer.writeVarInt(Polyvalent.ITEMS.size());

        // Now iterate over all the Polyvalent items
        for (Map.Entry<Identifier, Item> entry : Polyvalent.ITEMS.entrySet()) {
            Identifier id = entry.getKey();
            Item item = entry.getValue();

            // Get the raw id this client uses to identify this item
            int block_item_id = Item.getRawId(item);

            buffer.writeString(id.toString());
            buffer.writeVarInt(block_item_id);
        }
    }

    /**
     * Initialize Polyvalent on the client-side
     *
     * @author   Jelle De Loecker   <jelle@elevenways.be>
     * @since    0.1.0
     */
    @Override
    public void onInitializeClient() {

        // Register the Polyvalent packets
        ModPacketsS2C.register();

        // Register the Polyvalent blocks that are translucent
        BlockRenderLayerMap.INSTANCE.putBlocks(RenderLayer.getTranslucent(),
                Polyvalent.GLASS_BLOCK_ONE,
                Polyvalent.GLASS_BLOCK_TWO,
                Polyvalent.GLASS_BLOCK_THREE,
                Polyvalent.PORTAL_BLOCK_ONE
        );

        // Register the Polyvalent blocks that have cutouts
        // (The backside of these blocks can be seen when looking at them)
        BlockRenderLayerMap.INSTANCE.putBlocks(RenderLayer.getCutoutMipped(),
                Polyvalent.LEAVES_BLOCK_ONE,
                Polyvalent.LEAVES_BLOCK_TWO,
                Polyvalent.LEAVES_BLOCK_THREE
        );

        // Old test for leaves
        Random r = new Random();
        int low = 0x00b359;
        int high = 0xffff59;

        ColorProviderRegistry.BLOCK.register((state, world, pos, tintIndex) -> {
            //return 0xebb359;
            return r.nextInt(high-low) + low;
        }, Polyvalent.LEAVES_BLOCK_ONE, Polyvalent.LEAVES_BLOCK_TWO, Polyvalent.LEAVES_BLOCK_THREE);
    }
}
