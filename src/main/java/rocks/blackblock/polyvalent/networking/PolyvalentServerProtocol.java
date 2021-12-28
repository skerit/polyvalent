package rocks.blackblock.polyvalent.networking;


import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.item.Item;
import net.minecraft.network.PacketByteBuf;
import net.minecraft.network.packet.s2c.play.CustomPayloadS2CPacket;
import net.minecraft.server.network.ServerPlayNetworkHandler;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.util.Identifier;
import net.minecraft.util.registry.Registry;
import rocks.blackblock.polyvalent.Polyvalent;
import rocks.blackblock.polyvalent.polymc.PolyvalentMap;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

public class PolyvalentServerProtocol {

    public static void sendHandshake(PolyvalentHandshakeHandler handler) {
        PacketByteBuf buf = Polyvalent.buf(0);

        // Always start with the version
        buf.writeString(Polyvalent.VERSION);

        buf.writeVarInt(ClientPackets.REGISTRY.size());

        for (var id : ClientPackets.REGISTRY.keySet()) {
            buf.writeString(id);

            var entry = ClientPackets.REGISTRY.get(id);

            buf.writeVarInt(entry.length);
            for (int i : entry) {
                buf.writeVarInt(i);
            }
        }

        handler.sendPacket(new CustomPayloadS2CPacket(ServerPackets.HANDSHAKE_ID, buf));
    }

    public static void sendBlockIdMap(ServerPlayNetworkHandler player_handler) {

        PacketByteBuf id_buf = Polyvalent.buf(0);

        ServerPlayerEntity player = player_handler.getPlayer();
        TempPlayerLoginAttachments attachments = (TempPlayerLoginAttachments) player;

        PolyvalentMap map = attachments.getPolyvalentMap();
        HashMap<Identifier, List<Integer>> block_map = new HashMap<>();
        int block_count = 0;

        for (Block block : Registry.BLOCK) {
            Identifier id = Registry.BLOCK.getId(block);

            if (!Polyvalent.requiresPoly(id)) {
                continue;
            }

            // Get all available blockstates for this block
            List<BlockState> states = block.getStateManager().getStates();

            List<Integer> state_ids = new ArrayList<>();

            // See if there are valid client-side states
            // (the ones without a valid id (or stone) are ignored)
            for (BlockState state : states) {
                int raw_id = map.getClientStateRawId(state, player);

                if (raw_id > 10) {
                    state_ids.add(raw_id);
                }
            }

            // Only send blocks that have valid client-side states
            if (state_ids.size() > 0) {
                block_count++;

                id_buf.writeString(id.toString());
                id_buf.writeVarInt(state_ids.size());

                for (Integer client_id : state_ids) {
                    id_buf.writeVarInt(client_id);
                }
            }
        }

        PacketByteBuf buf = Polyvalent.buf(0);
        buf.writeVarInt(block_count);
        buf.writeBytes(id_buf);

        System.out.println("Sending block id buffer: " + buf.readableBytes());

        player_handler.sendPacket(new CustomPayloadS2CPacket(ServerPackets.BLOCK_ID_MAP_ID, buf));
    }
}
