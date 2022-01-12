package rocks.blackblock.polyvalent.networking;

import io.netty.buffer.ByteBuf;
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
import net.minecraft.network.PacketByteBuf;
import io.netty.util.concurrent.Future;
import io.netty.util.concurrent.GenericFutureListener;
import net.minecraft.network.packet.s2c.play.CustomPayloadS2CPacket;
import net.minecraft.util.Identifier;
import rocks.blackblock.polyvalent.Polyvalent;
import rocks.blackblock.polyvalent.PolyvalentClient;

import java.util.*;
import java.util.concurrent.CompletableFuture;
import java.util.function.Consumer;

/**
 * Handle incoming server packets on the client-side
 */
public class ModPacketsS2C {

    @Environment(EnvType.CLIENT)
    public static void register() {
        ClientLoginNetworking.registerGlobalReceiver(ModPackets.HANDSHAKE, ModPacketsS2C::handleHandshake);
        ClientLoginNetworking.registerGlobalReceiver(ModPackets.ID_MAP, ModPacketsS2C::handleIdMap);

        ClientPlayConnectionEvents.INIT.register(((clientPlayNetworkHandler, minecraftClient) -> {
            Polyvalent.log("Client Play Connection INIT");

            //ClientLoginNetworking.registerReceiver(ModPackets.HANDSHAKE, ModPacketsS2C::handleHandshake);
            //ClientPlayNetworking.registerReceiver(ModPackets.HANDSHAKE, ModPacketsS2C::handleHandshakeVelocity);

            // Velocity will send the id map later
            ClientPlayNetworking.registerReceiver(ModPackets.ID_MAP, ModPacketsS2C::handleIdMapVelocity);

        }));
    }

    private static void handleIdMapVelocity(MinecraftClient minecraftClient, ClientPlayNetworkHandler clientPlayNetworkHandler, PacketByteBuf buf, PacketSender packetSender) {
        handleIdMap(buf);
    }

    private static void handleIdMap(PacketByteBuf buf) {
        Polyvalent.log("Got ID map of " + buf.readableBytes() + " bytes");

        PolyvalentClient.connectedToPolyvalentServer = true;

        // Remove all the old identifiers
        PolyvalentClient.actualBlockIdentifiers.clear();

        // Get the version
        int version = buf.readVarInt();

        Polyvalent.log("Packet version: " + version);

        int block_byte_size = buf.readVarInt();

        Polyvalent.log("Block byte size: " + block_byte_size);

        PacketByteBuf blocks = new PacketByteBuf(buf.readBytes(block_byte_size));

        int block_count = blocks.readVarInt();
        Polyvalent.log(" -- Server is going to use " + block_count + " polyvalent blocks");

        try {

            for (int i = 0; i < block_count; i++) {
                String block_name = blocks.readString(1024);
                int state_count = blocks.readVarInt();

                Identifier block_id = new Identifier(block_name);

                for (int j = 0; j < state_count; j++) {
                    int state_id = blocks.readVarInt();
                    PolyvalentClient.actualBlockIdentifiers.put(state_id, block_id);
                    Polyvalent.log(" » " + block_name + ": " + state_id);
                }
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

                Polyvalent.log(" »» Got NBT: " + nbt);
            }

        } catch (Exception e) {
            Polyvalent.log(" -- Error reading items from map: " + e.getMessage());
            e.printStackTrace();
        }
    }

    private static CompletableFuture<PacketByteBuf> handleIdMap(MinecraftClient minecraftClient, ClientLoginNetworkHandler clientLoginNetworkHandler, PacketByteBuf buf, Consumer<GenericFutureListener<? extends Future<? super Void>>> genericFutureListenerConsumer) {
        handleIdMap(buf);

        PacketByteBuf response_buf = PacketByteBufs.create();
        response_buf.writeBoolean(true);

        return CompletableFuture.completedFuture(response_buf);
    }

    @Environment(EnvType.CLIENT)
    private static CompletableFuture<PacketByteBuf> handleHandshake(MinecraftClient minecraftClient, ClientLoginNetworkHandler clientLoginNetworkHandler, PacketByteBuf packetByteBuf, Consumer<GenericFutureListener<? extends Future<? super Void>>> genericFutureListenerConsumer) {
        PacketByteBuf buf = PacketByteBufs.create();
        Polyvalent.log("Got Polyvalent handshake request");
        buf.writeString("0.0.1");
        PolyvalentClient.writeBlockStateRawIds(buf);
        PolyvalentClient.writeItemRawIds(buf);
        return CompletableFuture.completedFuture(buf);
    }

}
