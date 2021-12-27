package rocks.blackblock.polyvalent.networking;

import io.netty.buffer.Unpooled;
import net.minecraft.network.PacketByteBuf;
import net.minecraft.network.packet.s2c.play.CustomPayloadS2CPacket;
import rocks.blackblock.polyvalent.Polyvalent;

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
}
