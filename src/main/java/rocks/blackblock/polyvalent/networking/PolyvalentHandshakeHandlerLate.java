package rocks.blackblock.polyvalent.networking;

import it.unimi.dsi.fastutil.objects.Object2IntMap;
import net.minecraft.network.Packet;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.network.ServerPlayNetworkHandler;
import net.minecraft.server.network.ServerPlayerEntity;

public class PolyvalentHandshakeHandlerLate implements PolyvalentHandshakeHandler {

    private final MinecraftServer server;
    private final ServerPlayNetworkHandler handler;

    public PolyvalentHandshakeHandlerLate(MinecraftServer server, ServerPlayNetworkHandler handler) {
        this.server = server;
        this.handler = handler;
    }

    public void sendPacket(Packet<?> packet) {
        this.handler.sendPacket(packet);
    }

    @Override
    public void set(String polyvalentVersion, Object2IntMap<String> protocolVersions) {

    }

    @Override
    public boolean isPolyvalent() {
        return false;
    }

    @Override
    public String getPolyvalentVersion() {
        return null;
    }

    @Override
    public int getSupportedProtocol(String identifier) {
        return 0;
    }

    @Override
    public void setLastPacketTime(String identifier) {

    }

    @Override
    public long getLastPacketTime(String identifier) {
        return 0;
    }

    public MinecraftServer getServer() {
        return server;
    }

    @Override
    public boolean shouldUpdateWorld() {
        return true;
    }

    public ServerPlayerEntity getPlayer() {
        return this.handler.getPlayer();
    }

    @Override
    public void apply(ServerPlayNetworkHandler handler) {
        // No need to apply, as it's send late anyway!
    }

    public static PolyvalentHandshakeHandler of(ServerPlayNetworkHandler handler) {
        return new PolyvalentHandshakeHandlerLate(handler.getPlayer().getServer(), handler);
    }
}