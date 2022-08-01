package rocks.blackblock.polyvalent.networking;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.fabricmc.fabric.api.client.networking.v1.ClientLoginNetworking;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayConnectionEvents;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;
import net.fabricmc.fabric.api.networking.v1.PacketByteBufs;
import net.fabricmc.fabric.api.networking.v1.PacketSender;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.network.ClientLoginNetworkHandler;
import net.minecraft.client.network.ClientPlayNetworkHandler;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.nbt.NbtElement;
import net.minecraft.nbt.NbtInt;
import net.minecraft.nbt.NbtList;
import net.minecraft.network.PacketByteBuf;
import io.netty.util.concurrent.Future;
import io.netty.util.concurrent.GenericFutureListener;
import net.minecraft.util.Identifier;
import rocks.blackblock.polyvalent.Polyvalent;
import rocks.blackblock.polyvalent.PolyvalentClient;
import rocks.blackblock.polyvalent.client.PolyvalentBlockInfo;
import rocks.blackblock.polyvalent.client.PolyvalentItemInfo;

import java.util.concurrent.CompletableFuture;
import java.util.function.Consumer;

/**
 * Handle incoming server packets on the client-side
 *
 * @author   Jelle De Loecker   <jelle@elevenways.be>
 * @since    0.1.0
 */
public class ModPacketsS2C {

    /**
     * Register the Polyvalent client-side packets
     *
     * @author   Jelle De Loecker   <jelle@elevenways.be>
     * @since    0.1.0
     */
    @Environment(EnvType.CLIENT)
    public static void register() {

        // Handle our Handshake packet
        ClientLoginNetworking.registerGlobalReceiver(ModPackets.HANDSHAKE, ModPacketsS2C::handleHandshake);

        // Handle ID-map packets
        ClientLoginNetworking.registerGlobalReceiver(ModPackets.ID_MAP, ModPacketsS2C::handleIdMap);

        // Fix for Velocity (which requires a custom Polyvalent plugin):
        // the ID-map packet will be sent after login
        ClientPlayConnectionEvents.INIT.register(((clientPlayNetworkHandler, minecraftClient) -> {
            // Velocity will send the id map later
            ClientPlayNetworking.registerReceiver(ModPackets.ID_MAP, ModPacketsS2C::handleIdMapVelocity);
        }));
    }

    /**
     * Respond to the Polyvalent handshake with
     * the available blocks & items the server can use.
     *
     * @author   Jelle De Loecker   <jelle@elevenways.be>
     * @since    0.1.0
     */
    @Environment(EnvType.CLIENT)
    private static CompletableFuture<PacketByteBuf> handleHandshake(MinecraftClient minecraftClient, ClientLoginNetworkHandler clientLoginNetworkHandler, PacketByteBuf packetByteBuf, Consumer<GenericFutureListener<? extends Future<? super Void>>> genericFutureListenerConsumer) {

        Polyvalent.log("Got Polyvalent handshake request");

        // Create a new empty buffer
        PacketByteBuf buf = Polyvalent.buf();

        // Write the version as a string
        buf.writeString(Polyvalent.VERSION);

        // Write the available blockstates
        PolyvalentClient.writeBlockStateRawIds(buf);

        // Write the available items
        PolyvalentClient.writeItemRawIds(buf);

        // And send it back!
        return CompletableFuture.completedFuture(buf);
    }

    /**
     * Handle the ID-map sent through Velocity
     *
     * @author   Jelle De Loecker   <jelle@elevenways.be>
     * @since    0.1.0
     */
    private static void handleIdMapVelocity(MinecraftClient minecraftClient, ClientPlayNetworkHandler clientPlayNetworkHandler, PacketByteBuf buf, PacketSender packetSender) {
        handleIdMap(buf);
    }

    /**
     * Handle the ID-map packet that requires a response
     *
     * @author   Jelle De Loecker   <jelle@elevenways.be>
     * @since    0.1.0
     */
    private static CompletableFuture<PacketByteBuf> handleIdMap(MinecraftClient minecraftClient, ClientLoginNetworkHandler clientLoginNetworkHandler, PacketByteBuf buf, Consumer<GenericFutureListener<? extends Future<? super Void>>> genericFutureListenerConsumer) {
        handleIdMap(buf);

        PacketByteBuf response_buf = PacketByteBufs.create();
        response_buf.writeBoolean(true);

        return CompletableFuture.completedFuture(response_buf);
    }

    /**
     * Handle the ID-maps of Blocks & Items
     *
     * @author   Jelle De Loecker   <jelle@elevenways.be>
     * @since    0.1.0
     */
    private static void handleIdMap(PacketByteBuf buf) {
        Polyvalent.log("Got ID map of " + buf.readableBytes() + " bytes");

        // Because we got this packet, it means we are
        // connected to a Polyvalent server
        PolyvalentClient.markAsPolyvalentServer(true);

        // Get the version
        int version = buf.readVarInt();

        Polyvalent.log("Packet version: " + version);

        // Read out how many bytes in the buffer are
        // spent on the Block map
        int block_byte_size = buf.readVarInt();

        Polyvalent.log("Block byte size: " + block_byte_size);

        PacketByteBuf blocks = new PacketByteBuf(buf.readBytes(block_byte_size));

        // Get how many blocks are in the map
        int block_count = blocks.readVarInt();
        Polyvalent.log(" -- Server is going to use " + block_count + " polyvalent blocks");

        try {

            for (int i = 0; i < block_count; i++) {
                NbtCompound block_nbt = blocks.readNbt();
                if (block_nbt == null) {
                    continue;
                }
                PolyvalentBlockInfo.parse(block_nbt);
            }
        } catch (Exception e) {
            Polyvalent.log(" -- Error reading blocks from map: " + e.getMessage());
            e.printStackTrace();
        }

        Polyvalent.log(" -- Reading items...");

        try {
            int byte_size_items = buf.readVarInt();

            Polyvalent.log("Item byte size: " + byte_size_items);

            PacketByteBuf items = new PacketByteBuf(buf.readBytes(byte_size_items));
            int item_count = items.readVarInt();

            Polyvalent.log(" -- Server is going to use " + item_count + " polyvalent items");

            for (int i = 0; i < item_count; i++) {
                NbtCompound nbt = items.readNbt();

                if (nbt == null || !nbt.contains("ns")) {
                    continue;
                }

                Polyvalent.log(" »» Got NBT: " + nbt);

                PolyvalentItemInfo item = new PolyvalentItemInfo(nbt);
                PolyvalentClient.itemInfo.put(item.raw_client_id, item);
                PolyvalentClient.itemInfoById.put(item.identifier, item);
            }

        } catch (Exception e) {
            Polyvalent.log(" -- Error reading items from map: " + e.getMessage());
            e.printStackTrace();
        }
    }
}
