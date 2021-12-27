package rocks.blackblock.polyvalent.networking;

import it.unimi.dsi.fastutil.objects.Object2IntMap;
import net.minecraft.network.Packet;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.network.ServerPlayNetworkHandler;
import net.minecraft.server.network.ServerPlayerEntity;
import org.jetbrains.annotations.Nullable;

public interface PolyvalentHandshakeHandler {

    void sendPacket(Packet<?> packet);

    void set(String polyvalentVersion, Object2IntMap<String> protocolVersions);

    boolean isPolyvalent();
    String getPolyvalentVersion();
    int getSupportedProtocol(String identifier);
    void setLastPacketTime(String identifier);
    long getLastPacketTime(String identifier);

    MinecraftServer getServer();

    boolean shouldUpdateWorld();

    @Nullable
    ServerPlayerEntity getPlayer();

    static PolyvalentHandshakeHandler of(ServerPlayNetworkHandler handler) {
        return PolyvalentHandshakeHandlerLate.of(handler);
    }

    void apply(ServerPlayNetworkHandler handler);
}