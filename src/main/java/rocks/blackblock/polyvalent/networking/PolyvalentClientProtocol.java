package rocks.blackblock.polyvalent.networking;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.network.ClientPlayNetworkHandler;
import net.minecraft.network.PacketByteBuf;
import net.minecraft.network.packet.c2s.play.CustomPayloadC2SPacket;
import net.minecraft.util.math.BlockPos;
import org.jetbrains.annotations.ApiStatus;
import rocks.blackblock.polyvalent.Polyvalent;
import rocks.blackblock.polyvalent.PolyvalentClient;

@ApiStatus.Internal
@Environment(EnvType.CLIENT)
public class PolyvalentClientProtocol {

    public static void sendHandshake(ClientPlayNetworkHandler handler) {
        if (Polyvalent.ENABLE_NETWORKING_CLIENT) {
            PacketByteBuf buf = Polyvalent.buf(0);

            // Start with the client's version
            buf.writeString(Polyvalent.VERSION);

            buf.writeVarInt(ServerPackets.REGISTRY.size());

            for (var id : ServerPackets.REGISTRY.keySet()) {
                buf.writeString(id);

                var entry = ServerPackets.REGISTRY.get(id);

                buf.writeVarInt(entry.length);

                for (int i : entry) {
                    buf.writeVarInt(i);
                }
            }

            // Write all available Polyvalent blockstates to the buffer
            PolyvalentClient.writeBlockStateRawIds(buf);

            Polyvalent.log("Sending handshake with Polyvalent blockstates to server");

            handler.sendPacket(new CustomPayloadC2SPacket(ClientPackets.HANDSHAKE_ID, buf));
        }
    }

    /*
    public static void sendSyncRequest(ClientPlayNetworkHandler handler) {
        if (InternalClientRegistry.enabled) {
            InternalClientRegistry.delayAction(ClientPackets.SYNC_REQUEST, 200, () -> {
                InternalClientRegistry.syncRequests++;
                PolymerClientUtils.ON_SYNC_REQUEST.invoke(EventRunners.RUN);
                handler.sendPacket(new CustomPayloadC2SPacket(ClientPackets.SYNC_REQUEST_ID, buf(0)));
            });
        }
    }

    public static void sendPickBlock(ClientPlayNetworkHandler handler, BlockPos pos) {
        if (InternalClientRegistry.getProtocol(ClientPackets.WORLD_PICK_BLOCK) == 0) {
            var buf = buf(0);
            buf.writeBlockPos(pos);
            buf.writeBoolean(Screen.hasControlDown());
            handler.sendPacket(new CustomPayloadC2SPacket(ClientPackets.WORLD_PICK_BLOCK_ID, buf));
        }
    }

    public static void sendTooltipContext(ClientPlayNetworkHandler handler) {
        if (InternalClientRegistry.getProtocol(ClientPackets.CHANGE_TOOLTIP) == 0) {
            InternalClientRegistry.delayAction(ClientPackets.CHANGE_TOOLTIP, 200, () -> {
                var buf = buf(0);
                buf.writeBoolean(MinecraftClient.getInstance().options.advancedItemTooltips);
                handler.sendPacket(new CustomPayloadC2SPacket(ClientPackets.CHANGE_TOOLTIP_ID, buf));
            });
        }
    }

    public static void sendPickEntity(ClientPlayNetworkHandler handler, int id) {
        if (InternalClientRegistry.getProtocol(ClientPackets.WORLD_PICK_ENTITY) == 0) {
            var buf = buf(0);
            buf.writeVarInt(id);
            buf.writeBoolean(Screen.hasControlDown());
            handler.sendPacket(new CustomPayloadC2SPacket(ClientPackets.WORLD_PICK_ENTITY_ID, buf));
        }
    }*/
}
