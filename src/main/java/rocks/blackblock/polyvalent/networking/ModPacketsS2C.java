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

    private static void handleHandshakeVelocity(MinecraftClient minecraftClient, ClientPlayNetworkHandler clientPlayNetworkHandler, PacketByteBuf packetByteBuf, PacketSender packetSender) {
        Polyvalent.log("Handshake through Velocity?");

        PacketByteBuf buf = PacketByteBufs.create();
        Polyvalent.log("Got Polyvalent handshake request");
        buf.writeString("0.0.1");
        PolyvalentClient.writeBlockStateRawIds(buf);
        //return CompletableFuture.completedFuture(buf);
    }

    private static void handleIdMapVelocity(MinecraftClient minecraftClient, ClientPlayNetworkHandler clientPlayNetworkHandler, PacketByteBuf buf, PacketSender packetSender) {
        Polyvalent.log("Got Polyvalent id map: " + buf.readableBytes());

        PolyvalentClient.connectedToPolyvalentServer = true;

        // Remove all the old identifiers
        PolyvalentClient.actualBlockIdentifiers.clear();

        int version = buf.readVarInt();
        int block_count = buf.readVarInt();
        int byte_size = buf.readVarInt();

        Polyvalent.log("Server is going to use " + block_count + " polyvalent blocks");

        for (int i = 0; i < block_count; i++) {
            String block_name = buf.readString(1024);
            int state_count = buf.readVarInt();

            Identifier block_id = new Identifier(block_name);

            for (int j = 0; j < state_count; j++) {
                int state_id = buf.readVarInt();
                PolyvalentClient.actualBlockIdentifiers.put(state_id, block_id);
                Polyvalent.log(" » " + block_name + ": " + state_id);
            }
        }
    }

    private static CompletableFuture<PacketByteBuf> handleIdMap(MinecraftClient minecraftClient, ClientLoginNetworkHandler clientLoginNetworkHandler, PacketByteBuf buf, Consumer<GenericFutureListener<? extends Future<? super Void>>> genericFutureListenerConsumer) {
        Polyvalent.log("Got Polyvalent id map: " + buf.readableBytes());

        PolyvalentClient.connectedToPolyvalentServer = true;

        // Remove all the old identifiers
        PolyvalentClient.actualBlockIdentifiers.clear();

        int version = buf.readVarInt();
        int block_count = buf.readVarInt();
        int byte_size = buf.readVarInt();

        Polyvalent.log("Server is going to use " + block_count + " polyvalent blocks");

        for (int i = 0; i < block_count; i++) {
            String block_name = buf.readString(1024);
            int state_count = buf.readVarInt();

            Identifier block_id = new Identifier(block_name);

            for (int j = 0; j < state_count; j++) {
                int state_id = buf.readVarInt();
                PolyvalentClient.actualBlockIdentifiers.put(state_id, block_id);
                Polyvalent.log(" » " + block_name + ": " + state_id);
            }
        }

        PacketByteBuf response_buf = PacketByteBufs.create();
        response_buf.writeBoolean(true);

        Polyvalent.log("Read id map, sending response?");

        return CompletableFuture.completedFuture(response_buf);
    }

    @Environment(EnvType.CLIENT)
    private static CompletableFuture<PacketByteBuf> handleHandshake(MinecraftClient minecraftClient, ClientLoginNetworkHandler clientLoginNetworkHandler, PacketByteBuf packetByteBuf, Consumer<GenericFutureListener<? extends Future<? super Void>>> genericFutureListenerConsumer) {
        PacketByteBuf buf = PacketByteBufs.create();
        Polyvalent.log("Got Polyvalent handshake request");
        buf.writeString("0.0.1");
        PolyvalentClient.writeBlockStateRawIds(buf);
        return CompletableFuture.completedFuture(buf);
    }

}
