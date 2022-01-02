package rocks.blackblock.polyvalent;

import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.blockrenderlayer.v1.BlockRenderLayerMap;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;
import net.fabricmc.fabric.api.client.rendering.v1.ColorProviderRegistry;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.client.render.RenderLayer;
import net.minecraft.network.PacketByteBuf;
import net.minecraft.util.Identifier;
import net.minecraft.util.registry.Registry;
import rocks.blackblock.polyvalent.block.PolyvalentBlock;
import rocks.blackblock.polyvalent.networking.ModPacketsS2C;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Random;

public class PolyvalentClient implements ClientModInitializer {

    public static HashMap<Integer, Identifier> actualBlockIdentifiers = new HashMap<>();
    public static HashMap<Integer, Identifier> actualItemIdentifiers = new HashMap<>();
    public static boolean connectedToPolyvalentServer = false;

    public static void reset() {
        connectedToPolyvalentServer = false;
    }

    @Override
    public void onInitializeClient() {

        ModPacketsS2C.register();

        Random r = new Random();
        int low = 0x00b359;
        int high = 0xffff59;

        BlockRenderLayerMap.INSTANCE.putBlocks(RenderLayer.getTranslucent(),
                Polyvalent.GLASS_BLOCK_ONE,
                Polyvalent.GLASS_BLOCK_TWO,
                Polyvalent.GLASS_BLOCK_THREE
        );

        BlockRenderLayerMap.INSTANCE.putBlocks(RenderLayer.getCutoutMipped(),
                Polyvalent.LEAVES_BLOCK_ONE,
                Polyvalent.LEAVES_BLOCK_TWO,
                Polyvalent.LEAVES_BLOCK_THREE
        );

        ColorProviderRegistry.BLOCK.register((state, world, pos, tintIndex) -> {
            //return 0xebb359;
            return r.nextInt(high-low) + low;
        }, Polyvalent.LEAVES_BLOCK_ONE, Polyvalent.LEAVES_BLOCK_TWO, Polyvalent.LEAVES_BLOCK_THREE);
    }

    /**
     * Write all raw block ids and their state name to a PacketByteBuf buffer
     */
    public static void writeBlockStateRawIds(PacketByteBuf buffer) {

        ArrayList<BlockState> polyvalent_states = new ArrayList<>();

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
}
