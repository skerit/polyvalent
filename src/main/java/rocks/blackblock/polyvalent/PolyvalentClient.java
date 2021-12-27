package rocks.blackblock.polyvalent;

import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.network.PacketByteBuf;
import rocks.blackblock.polyvalent.block.PolyvalentBlock;

import java.util.ArrayList;

public class PolyvalentClient implements ClientModInitializer {
    @Override
    public void onInitializeClient() {
        ClientPlayNetworking.registerGlobalReceiver(Polyvalent.CHANNEL_ID, (client, handler, packet, sender) -> {
            System.out.println("Received packet from server: " + packet.toString());

            int number = packet.readInt();

            System.out.println("Number: " + number);
        });
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

        buffer.writeVarInt(polyvalent_states.size());

        System.out.println("There are " + polyvalent_states.size() + " polyvalent states");

        for (BlockState state : polyvalent_states) {
            String state_id = state.toString();
            int raw_id = Block.STATE_IDS.getRawId(state);

            buffer.writeVarInt(raw_id);
            buffer.writeString(state_id);

            System.out.println("Writing " + state_id + " with raw id " + raw_id);
        }
    }
}
