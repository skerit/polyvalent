package rocks.blackblock.polyvalent.networking;

import io.netty.buffer.ByteBuf;
import it.unimi.dsi.fastutil.ints.IntArrayList;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.network.ClientPlayNetworkHandler;
import net.minecraft.client.search.SearchManager;
import net.minecraft.command.argument.BlockArgumentParser;
import net.minecraft.item.ItemGroup;
import net.minecraft.item.ItemStack;
import net.minecraft.network.PacketByteBuf;
import net.minecraft.server.network.ServerPlayNetworkHandler;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.ChunkSectionPos;
import net.minecraft.util.registry.Registry;
import net.minecraft.world.chunk.ChunkStatus;
import org.jetbrains.annotations.ApiStatus;
import org.jetbrains.annotations.Nullable;
import rocks.blackblock.polyvalent.Polyvalent;
import rocks.blackblock.polyvalent.PolyvalentClient;
import rocks.blackblock.polyvalent.client.InternalClientRegistry;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

@ApiStatus.Internal
@SuppressWarnings({"unused"})
@Environment(EnvType.CLIENT)
public class PolyvalentClientProtocolHandler {
    public static volatile int packetsPerSecond = 0;

    private static int currentPacketsPerSecond = 0;
    private static int ticker = 0;
    public static final HashMap<String, PolyvalentClientPacketHandler> CUSTOM_PACKETS = new HashMap<>();

    public static void tick() {
        ticker++;

        if (ticker >= 20) {
            ticker = 0;

            packetsPerSecond = currentPacketsPerSecond;
            currentPacketsPerSecond = 0;
        }

    }

    //public static final HashMap<String, PolyvalentClientPacketHandler> CUSTOM_PACKETS = new HashMap<>();

    public static void handle(ClientPlayNetworkHandler handler, Identifier identifier, PacketByteBuf buf) {
        if (Polyvalent.ENABLE_NETWORKING_CLIENT) {
            var version = -1;
            try {
                version = buf.readVarInt();
                if (!handle(handler, identifier.getPath(), version, buf)) {
                    Polyvalent.LOGGER.warn("Unsupported packet " + identifier + " (" + version + ") was received from server!");
                }
            } catch (Exception e) {
                Polyvalent.LOGGER.error("Invalid " + identifier + " (" + version + ") packet received from server!");
                Polyvalent.LOGGER.error(e);
            }
            currentPacketsPerSecond++;
        }
    }

    private static boolean handle(ClientPlayNetworkHandler handler, String packet, int version, PacketByteBuf buf) {
        return switch (packet) {
            case ServerPackets.HANDSHAKE -> handleHandshake(handler, version, buf);
            case ServerPackets.BLOCK_ID_MAP -> handleBlockIdMap(handler, version, buf);

            default -> {
                var packetHandler = CUSTOM_PACKETS.get(packet);
                if (packetHandler != null) {
                    packetHandler.onPacket(handler, version, buf);
                    yield true;
                }
                yield false;
            }
        };
    }

    public static boolean handleBlockIdMap(ClientPlayNetworkHandler handler, int version, PacketByteBuf buf) {

        // Remove all the old identifiers
        PolyvalentClient.actualBlockIdentifiers.clear();

        int block_count = buf.readVarInt();
        int byte_size = buf.readVarInt();

        for (int i = 0; i < block_count; i++) {
            String block_name = buf.readString(1024);
            int state_count = buf.readVarInt();

            Identifier block_id = new Identifier(block_name);

            for (int j = 0; j < state_count; j++) {
                int state_id = buf.readVarInt();
                PolyvalentClient.actualBlockIdentifiers.put(state_id, block_id);
            }
        }

        return true;
    }

    private static boolean run(Runnable runnable) {
        runnable.run();
        return true;
    }

    private static boolean handleHandshake(ClientPlayNetworkHandler handler, int version, PacketByteBuf buf) {
        if (version == 0) {
            String server_version = buf.readString(64);

            System.out.println("Server version: " + server_version);

            InternalClientRegistry.setVersion(server_version);

            // Get the amount of entries in the registry
            int size = buf.readVarInt();

            for (int i = 0; i < size; i++) {
                String id = buf.readString();

                int size2 = buf.readVarInt();
                var list = new IntArrayList();

                for (int i2 = 0; i2 < size2; i2++) {
                    list.add(buf.readVarInt());
                }

                InternalClientRegistry.registerClientProtocol(id, list.elements());
            }

            /*
            MinecraftClient.getInstance().execute(() -> {

                InternalClientRegistry.itemsMatch = InternalClientRegistry.getProtocol(ServerPackets.SYNC_ITEM) >= 2;
                PolymerClientUtils.ON_HANDSHAKE.invoke(EventRunners.RUN);
                PolymerClientProtocol.sendTooltipContext(handler);
                PolymerClientProtocol.sendSyncRequest(handler);
            });
             */

            return true;
        }
        return false;
    }

    interface EntryReader<T> {
        @Nullable
        T read(PacketByteBuf buf, int version);
    }
}