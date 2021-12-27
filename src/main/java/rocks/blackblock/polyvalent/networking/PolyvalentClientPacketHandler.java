package rocks.blackblock.polyvalent.networking;

import net.minecraft.client.network.ClientPlayNetworkHandler;
import net.minecraft.network.PacketByteBuf;

@FunctionalInterface
public interface PolyvalentClientPacketHandler {
    void onPacket(ClientPlayNetworkHandler handler, int version, PacketByteBuf buf);
}
