package rocks.blackblock.polyvalent.networking;

import net.minecraft.network.packet.s2c.play.CustomPayloadS2CPacket;
import net.minecraft.server.network.ServerPlayNetworkHandler;
import rocks.blackblock.polyvalent.utils.SimpleEvent;

import java.util.function.BiConsumer;
import java.util.function.Consumer;

public final class PolyvalentSyncUtils {
    private PolyvalentSyncUtils() {
    }

    /**
     * This event is run after receiving client handshake
     */
    public static final SimpleEvent<Consumer<PolyvalentHandshakeHandler>> ON_HANDSHAKE = new SimpleEvent<>();
    /**
     * This event is run before Polymer registry sync
     */
    public static final SimpleEvent<Consumer<ServerPlayNetworkHandler>> ON_SYNC_STARTED = new SimpleEvent<>();
    /**
     * This event is run when it's suggested to sync custom content
     */
    public static final SimpleEvent<BiConsumer<ServerPlayNetworkHandler, Boolean>> ON_SYNC_CUSTOM = new SimpleEvent<>();
    /**
     * This event is run after Polymer registry sync
     */
    public static final SimpleEvent<Consumer<ServerPlayNetworkHandler>> ON_SYNC_FINISHED = new SimpleEvent<>();

}