package rocks.blackblock.polyvalent.networking;

import it.unimi.dsi.fastutil.ints.IntArrayList;
import it.unimi.dsi.fastutil.objects.Object2IntOpenHashMap;
import net.minecraft.network.PacketByteBuf;
import net.minecraft.server.network.ServerPlayerEntity;
import org.jetbrains.annotations.ApiStatus;

@ApiStatus.Internal
public class PolyvalentServerProtocolHandler {

    public static void handleHandshake(PolyvalentHandshakeHandler handler, int version, PacketByteBuf buf) {
        System.out.println("Version: " + version + " - Polyvalent? " + handler.isPolyvalent());

        if (version == 0 && !handler.isPolyvalent()) {

            var polyvalentVersion = buf.readString(64);

            System.out.println("Polyvalent version: " + polyvalentVersion);

            int settings_size = buf.readVarInt();

            System.out.println("Settings size: " + settings_size);

            var versionMap = new Object2IntOpenHashMap<String>();

            for (int i = 0; i < settings_size; i++) {
                var id = buf.readString();

                var size2 = buf.readVarInt();
                var list = new IntArrayList();

                for (int i2 = 0; i2 < size2; i2++) {
                    list.add(buf.readVarInt());
                }

                //versionMap.put(id, ServerPackets.getBestSupported(id, list.elements()));
            }

            int polyvalent_state_size = buf.readVarInt();

            System.out.println("The client has " + polyvalent_state_size + " polyvalent states");

            ServerPlayerEntity player = handler.getPlayer();

            System.out.println("Got player? " + player);

            if (player != null) {
                TempPlayerLoginAttachments attachments = (TempPlayerLoginAttachments) player;
                attachments.setHasPolyvalent(true);
                attachments.loadPolyvalentStates(polyvalent_state_size, buf);
            }

            handler.getServer().execute(() -> {
                //handler.set(polyvalentVersion, versionMap);
                //handler.setLastPacketTime(ClientPackets.HANDSHAKE);

                /*if (handler.getPlayer() != null) {
                    ((TempPlayerLoginAttachments) handler.getPlayer()).polymer_setWorldReload(handler.shouldUpdateWorld());
                }*/

                PolyvalentSyncUtils.ON_HANDSHAKE.invoke((c) -> c.accept(handler));
                PolyvalentServerProtocol.sendHandshake(handler);
            });
        }
    }
}
