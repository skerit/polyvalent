package rocks.blackblock.polyvalent.networking;

import it.unimi.dsi.fastutil.objects.Object2IntMap;
import net.minecraft.network.Packet;
import net.minecraft.server.network.ServerPlayNetworkHandler;
import org.jetbrains.annotations.ApiStatus;

@ApiStatus.Internal
@SuppressWarnings({"unused"})
public interface PolyvalentNetworkHandlerExtension {

    void polyvalent_schedulePacket(Packet<?> packet, int duration);

    boolean polyvalent_hasPolyvalent();
    String polyvalent_version();
    int polyvalent_protocolVersion();

    void polyvalent_setVersion(String version);

    long polyvalent_lastPacketUpdate(String identifier);
    void polyvalent_savePacketTime(String identifier);

    int polyvalent_getSupportedVersion(String identifier);
    void polyvalent_setSupportedVersion(String identifier, int i);
    Object2IntMap<String> polyvalent_getSupportMap();

    boolean polyvalent_advancedTooltip();
    void polyvalent_setAdvancedTooltip(boolean value);

    void polyvalent_delayAction(String identifier, int delay, Runnable action);

    static PolyvalentNetworkHandlerExtension of(ServerPlayNetworkHandler handler) {
        return (PolyvalentNetworkHandlerExtension) handler;
    }
}