package rocks.blackblock.polyvalent.networking;

import net.fabricmc.fabric.api.networking.v1.*;
import net.luckperms.api.LuckPerms;
import net.luckperms.api.model.user.User;
import net.luckperms.api.model.user.UserManager;
import net.luckperms.api.node.Node;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.client.MinecraftClient;
import net.minecraft.item.ArmorItem;
import net.minecraft.item.Item;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.network.PacketByteBuf;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.network.ServerLoginNetworkHandler;
import net.minecraft.server.network.ServerPlayNetworkHandler;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.util.Identifier;
import net.minecraft.util.registry.Registry;
import rocks.blackblock.polyvalent.Polyvalent;
import rocks.blackblock.polyvalent.PolyvalentServer;
import rocks.blackblock.polyvalent.polymc.PolyvalentMap;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.concurrent.CompletableFuture;

/**
 * Handle incoming client packets on the server-side
 */
public class ModPacketsC2S {

    /**
     * Register the packets
     */
    public static void register() {

        // Return early if this code runs on a client
        if (Polyvalent.isClient()) {
            Polyvalent.log("Not registering packets on client");
            return;
        }

        ServerLoginConnectionEvents.QUERY_START.register(ModPacketsC2S::handshake);
        ServerLoginNetworking.registerGlobalReceiver(ModPackets.HANDSHAKE, ModPacketsC2S::handleHandshakeReply);
        ServerLoginNetworking.registerGlobalReceiver(ModPackets.ID_MAP, ModPacketsC2S::handleIdMapReply);

    }

    private static void handleIdMapReply(MinecraftServer minecraftServer, ServerLoginNetworkHandler serverLoginNetworkHandler, boolean b, PacketByteBuf packetByteBuf, ServerLoginNetworking.LoginSynchronizer loginSynchronizer, PacketSender packetSender) {
        Polyvalent.log("Received id map reply?");
    }

    private static void handleIdMapRequest(MinecraftServer minecraftServer, ServerPlayerEntity serverPlayerEntity, ServerPlayNetworkHandler serverPlayNetworkHandler, PacketByteBuf packetByteBuf, PacketSender packetSender) {
        Polyvalent.log("Received id map request");
    }

    private static void handleHandshakeReply(MinecraftServer minecraftServer, ServerLoginNetworkHandler serverLoginNetworkHandler, boolean understood, PacketByteBuf packetByteBuf, ServerLoginNetworking.LoginSynchronizer loginSynchronizer, PacketSender packetSender) {
        if (understood) {
            Polyvalent.log("Client joined with Polyvalent");
            Polyvalent.log("Buf size: " + packetByteBuf.readableBytes());

            // Get the `attachments` object
            PolyvalentAttachments attachments = (PolyvalentAttachments) serverLoginNetworkHandler.connection;

            // Now that the handshake was understood by the client, we can be sure it is a polyvalent client
            attachments.setIsPolyvalent(true);

            try {
                handleHandshakeBuffer(attachments, packetByteBuf);
            } catch (Exception e) {
                Polyvalent.log("Failed to read handshake packet: " + e.getMessage());
            }

            sendIdMap(packetSender, attachments);

        } else {
            Polyvalent.log("Client joined without Polyvalent");
        }
    }

    private static void handshake(ServerLoginNetworkHandler serverLoginNetworkHandler, MinecraftServer minecraftServer, PacketSender packetSender, ServerLoginNetworking.LoginSynchronizer loginSynchronizer) {
        Polyvalent.log("Sending handshake request!!");
        packetSender.sendPacket(ModPackets.HANDSHAKE, PacketByteBufs.empty());
    }

    private static void handleHandshakeBuffer(PolyvalentAttachments attachments, PacketByteBuf buf) {

        String version = buf.readString(64);

        Polyvalent.log("Connecting client version: " + version);

        int polyvalent_state_size = buf.readVarInt();

        Polyvalent.log("The client has " + polyvalent_state_size + " polyvalent states");

        PolyvalentMap map = loadPolyvalentStates(polyvalent_state_size, buf);

        int polyvalent_item_count;

        try {
            polyvalent_item_count = buf.readVarInt();
        } catch (Exception e) {
            polyvalent_item_count = 0;
            Polyvalent.log("Failed to read polyvalent item count: " + e.getMessage());
        }

        Polyvalent.log("The client has " + polyvalent_item_count + " polyvalent items");

        try {
            loadPolyvalentItems(map, polyvalent_item_count, buf);
        } catch (Exception e) {
            Polyvalent.log("Failed to load Polyvalent items: " + e.getMessage());
            e.printStackTrace();
        }

        attachments.setPolyvalentMap(map);
    }

    /**
     * Load the client-side Polyvalent state numbers into a map
     *
     * @param    map      The map to load into
     * @param    amount   The amount of states to load from the buffer
     * @param    buffer   The buffer to read from
     *
     * @return   The player-specific PolyvalentMap
     */
    private static PolyvalentMap loadPolyvalentItems(PolyvalentMap map, int amount, PacketByteBuf buffer) {

        for (int i = 0; i < amount; i++) {
            String item_id_string = buffer.readString(1024);
            Identifier item_id = new Identifier(item_id_string);
            int client_raw_id = buffer.readVarInt();

            // And now get the raw id from the server
            int server_raw_id = Item.getRawId(Registry.ITEM.get(item_id));

            if (client_raw_id != server_raw_id) {
                map.setServerToClientItemId(server_raw_id, client_raw_id);
            }
        }

        return map;
    }

    /**
     * Load the client-side Polyvalent state numbers into a map
     *
     * @param    amount   The amount of states to load from the buffer
     * @param    buffer   The buffer to read from
     *
     * @return   The player-specific PolyvalentMap
     */
    private static PolyvalentMap loadPolyvalentStates(int amount, PacketByteBuf buffer) {

        int added = 0;

        PolyvalentMap map = PolyvalentServer.getMainMap().createPlayerMap();

        while (added < amount) {
            String block_id = buffer.readString(1024);
            int amount_of_states = buffer.readVarInt();
            int start_id = buffer.readVarInt();
            int nonce_nr = -1;

            for (int i = 0; i < amount_of_states; i++) {
                int client_raw_id = start_id + i;
                String client_name = "Block{" + block_id + "}";
                nonce_nr++;

                if (client_name.startsWith("Block{polyvalent:slab")) {
                    String nonce_name = client_name + "[nonce=" + nonce_nr + ",";
                    String type_name;

                    // Decrease the loop counter by one, we'll increase it again later
                    i--;

                    for (int type_count = 0; type_count < 3; type_count++) {
                        if (type_count == 0) {
                            type_name = nonce_name + "type=top,";
                        } else if (type_count == 1) {
                            type_name = nonce_name + "type=bottom,";
                        } else {
                            type_name = nonce_name + "type=double,";
                        }

                        for (int water_count = 0; water_count < 2; water_count++) {
                            String water_name;

                            if (water_count == 0) {
                                water_name = type_name + "waterlogged=true]";
                            } else {
                                water_name = type_name + "waterlogged=false]";
                            }

                            // And now get the raw id from the server
                            int server_raw_id = PolyvalentServer.BLOCK_STATE_ID_MAP.get(water_name);

                            if (client_raw_id != server_raw_id) {
                                map.setServerToClientId(server_raw_id, client_raw_id);
                            }

                            client_raw_id++;

                            i++;
                        }
                    }
                } else {

                    client_name = client_name + "[nonce=" + i + "]";

                    // And now get the raw id from the server
                    int server_raw_id = PolyvalentServer.BLOCK_STATE_ID_MAP.get(client_name);

                    if (client_raw_id != server_raw_id) {
                        map.setServerToClientId(server_raw_id, client_raw_id);
                    }
                }
            }

            added += amount_of_states;
        }

        return map;
    }

    public static void sendIdMap(PacketSender packetSender, PolyvalentAttachments attachments) {

        PacketByteBuf result = Polyvalent.createPacketBuf();

        PacketByteBuf blocks = getBlockIdMap(attachments);
        result.writeVarInt(blocks.readableBytes());
        result.writeBytes(blocks);

        PacketByteBuf items = getItemMap(attachments);
        result.writeVarInt(items.readableBytes());
        result.writeBytes(items);

        Polyvalent.log("Sending ID map to client: " + result.readableBytes() + " bytes");

        //player_handler.sendPacket(new CustomPayloadS2CPacket(ServerPackets.BLOCK_ID_MAP_ID, buf));
        packetSender.sendPacket(ModPackets.ID_MAP, result);
    }

    private static PacketByteBuf getBlockIdMap(PolyvalentAttachments attachments) {

        PacketByteBuf id_buf = Polyvalent.buf();

        PolyvalentMap map = attachments.getPolyvalentMap();
        HashMap<Identifier, List<Integer>> block_map = new HashMap<>();
        int block_count = 0;

        for (Block block : Registry.BLOCK) {
            Identifier id = Registry.BLOCK.getId(block);

            if (!Polyvalent.requiresPoly(id)) {
                continue;
            }

            // Get all available blockstates for this block
            List<BlockState> states = block.getStateManager().getStates();

            List<Integer> state_ids = new ArrayList<>();

            // See if there are valid client-side states
            // (the ones without a valid id (or stone) are ignored)
            for (BlockState state : states) {
                int raw_id = map.getClientStateRawId(state, null);

                if (raw_id > 10) {
                    state_ids.add(raw_id);
                }
            }

            // Only send blocks that have valid client-side states
            if (state_ids.size() > 0) {
                block_count++;

                id_buf.writeString(id.toString());
                id_buf.writeVarInt(state_ids.size());

                for (Integer client_id : state_ids) {
                    id_buf.writeVarInt(client_id);
                }
            }
        }

        Polyvalent.log("Sending " + block_count + " block ids");

        PacketByteBuf buf = Polyvalent.buf();
        buf.writeVarInt(block_count);
        buf.writeBytes(id_buf);

        return buf;
    }

    private static PacketByteBuf getItemMap(PolyvalentAttachments attachments) {

        PacketByteBuf item_buf = Polyvalent.buf();
        int nbt_count = 0;

        PacketByteBuf items = Polyvalent.buf();

        for (Item item : Registry.ITEM) {
            Identifier id = Registry.ITEM.getId(item);

            if (!Polyvalent.requiresPoly(id)) {
                continue;
            }

            if (!(item instanceof ArmorItem armorItem)) {
                continue;
            }

            int raw_client_id = attachments.getPolyvalentMap().getClientItemRawId(item, null);

            NbtCompound nbt = new NbtCompound();
            nbt.putInt("id", raw_client_id);
            nbt.putInt("maxDamage", armorItem.getMaxDamage());
            nbt.putInt("maxProtection", armorItem.getMaterial().getProtectionAmount(armorItem.getSlotType()));
            nbt.putFloat("maxToughness", armorItem.getMaterial().getToughness());

            nbt_count++;
            items.writeNbt(nbt);
        }

        Polyvalent.log("Sending " + nbt_count + " item nbt");

        item_buf.writeVarInt(nbt_count);
        item_buf.writeBytes(items);

        return item_buf;
    }
}
