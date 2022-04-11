package rocks.blackblock.polyvalent.networking;

import io.github.theepicblock.polymc.api.item.ItemPoly;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.fabricmc.fabric.api.networking.v1.*;
import net.luckperms.api.LuckPerms;
import net.luckperms.api.model.user.User;
import net.luckperms.api.model.user.UserManager;
import net.luckperms.api.node.Node;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.client.MinecraftClient;
import net.minecraft.item.ArmorItem;
import net.minecraft.item.BlockItem;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
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
 *
 * @author   Jelle De Loecker   <jelle@elevenways.be>
 * @since    0.1.0
 */
public class ModPacketsC2S {

    /**
     * Register the Polyvalent server-side packets
     *
     * @author   Jelle De Loecker   <jelle@elevenways.be>
     * @since    0.1.0
     */
    @Environment(EnvType.SERVER)
    public static void register() {

        // Send a handshake packet as soon as a client connects
        ServerLoginConnectionEvents.QUERY_START.register(ModPacketsC2S::handshake);

        // Register the handshake reply handler
        ServerLoginNetworking.registerGlobalReceiver(ModPackets.HANDSHAKE, ModPacketsC2S::handleHandshakeReply);

        // And handle the id-map reply handler too
        ServerLoginNetworking.registerGlobalReceiver(ModPackets.ID_MAP, ModPacketsC2S::handleIdMapReply);
    }

    /**
     * Send the actual Polyvalent Handshake request
     *
     * @author   Jelle De Loecker   <jelle@elevenways.be>
     * @since    0.1.0
     */
    private static void handshake(ServerLoginNetworkHandler serverLoginNetworkHandler, MinecraftServer minecraftServer, PacketSender packetSender, ServerLoginNetworking.LoginSynchronizer loginSynchronizer) {
        packetSender.sendPacket(ModPackets.HANDSHAKE, PacketByteBufs.empty());
    }

    /**
     * Handle the handshake reply of the client.
     * This will contain all the blockstate ids
     * and item ids we're allowed to use.
     *
     * @author   Jelle De Loecker   <jelle@elevenways.be>
     * @since    0.1.0
     */
    private static void handleHandshakeReply(MinecraftServer minecraftServer, ServerLoginNetworkHandler serverLoginNetworkHandler, boolean understood, PacketByteBuf packetByteBuf, ServerLoginNetworking.LoginSynchronizer loginSynchronizer, PacketSender packetSender) {

        if (!understood) {
            Polyvalent.log("Client joined without Polyvalent");
            return;
        }

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
            e.printStackTrace();
        }

        sendIdMap(packetSender, attachments);
    }

    /**
     * Process the actual Handshake buffer
     *
     * @author   Jelle De Loecker   <jelle@elevenways.be>
     * @since    0.1.0
     *
     * @param    attachments
     * @param    buf
     */
    private static void handleHandshakeBuffer(PolyvalentAttachments attachments, PacketByteBuf buf) {

        // Read out the version-as-a-string
        String version = buf.readString(64);

        Polyvalent.log("Connecting client version: " + version);

        int client_blockstate_count = buf.readVarInt();

        Polyvalent.log("The client has " + client_blockstate_count + " polyvalent blockstates");

        // Read out the blockstates
        PolyvalentMap map = loadPolyvalentStates(client_blockstate_count, buf);

        int client_item_count;

        try {
            client_item_count = buf.readVarInt();
        } catch (Exception e) {
            client_item_count = 0;
            Polyvalent.log("Failed to read polyvalent item count: " + e.getMessage());
        }

        Polyvalent.log("The client has " + client_item_count + " polyvalent items");

        try {
            loadPolyvalentItems(map, client_item_count, buf);
        } catch (Exception e) {
            Polyvalent.log("Failed to load Polyvalent items: " + e.getMessage());
            e.printStackTrace();
        }

        attachments.setPolyvalentMap(map);
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

        Polyvalent.log("Creating player specific map...");

        while (added < amount) {
            String block_id = buffer.readString(1024);
            int amount_of_states = buffer.readVarInt();
            int start_id = buffer.readVarInt();
            int nonce_nr = -1;
            int state_nr = -1;

            String client_name = "Block{" + block_id + "}";

            for (int i = 0; i < amount_of_states; i++) {
                int client_raw_id = start_id + i;
                String type_name;

                state_nr++;
                nonce_nr++;

                if (client_name.startsWith("Block{polyvalent:slab")) {
                    String nonce_name = client_name + "[nonce=" + nonce_nr + ",";

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
                } else if (client_name.startsWith("Block{polyvalent:portal")) {

                    if (state_nr < 100) {
                        type_name = client_name + "[axis=x,nonce=" + nonce_nr + "]";
                    } else if (state_nr < 200) {
                        type_name = client_name + "[axis=y,nonce=" + (nonce_nr-100) + "]";
                    }else {
                        type_name = client_name + "[axis=z,nonce=" + (nonce_nr-200) + "]";
                    }

                    // And now get the raw id from the server
                    Integer server_raw_id = PolyvalentServer.BLOCK_STATE_ID_MAP.get(type_name);

                    if (server_raw_id == null) {
                        Polyvalent.log("Failed to find server raw id for " + client_name);
                        continue;
                    }

                    if (client_raw_id != server_raw_id) {
                        map.setServerToClientId(server_raw_id, client_raw_id);
                    }

                } else {

                    type_name = client_name + "[nonce=" + i + "]";

                    // And now get the raw id from the server
                    Integer server_raw_id = PolyvalentServer.BLOCK_STATE_ID_MAP.get(type_name);

                    if (server_raw_id == null) {
                        Polyvalent.log("Failed to find server raw id for " + type_name);
                        continue;
                    }

                    if (client_raw_id != server_raw_id) {
                        map.setServerToClientId(server_raw_id, client_raw_id);
                    }
                }
            }

            added += amount_of_states;
        }

        return map;
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
     * The ID-map has been handled by the client
     *
     * @author   Jelle De Loecker   <jelle@elevenways.be>
     * @since    0.1.0
     */
    private static void handleIdMapReply(MinecraftServer minecraftServer, ServerLoginNetworkHandler serverLoginNetworkHandler, boolean b, PacketByteBuf packetByteBuf, ServerLoginNetworking.LoginSynchronizer loginSynchronizer, PacketSender packetSender) {
        Polyvalent.log("Received id map reply. Client should be ready");
    }

    /**
     * Send the ID-map packet to the client
     *
     * @author   Jelle De Loecker   <jelle@elevenways.be>
     * @since    0.1.0
     */
    public static void sendIdMap(PacketSender packetSender, PolyvalentAttachments attachments) {

        PacketByteBuf result = Polyvalent.createPacketBuf();

        PacketByteBuf blocks = getBlockIdMap(attachments);

        if (blocks == null) {
            Polyvalent.log("Could not get block id map, not sending anything");
            return;
        }

        result.writeVarInt(blocks.readableBytes());
        result.writeBytes(blocks);

        PacketByteBuf items = getItemMap(attachments);
        result.writeVarInt(items.readableBytes());
        result.writeBytes(items);

        Polyvalent.log("Sending ID map to client: " + result.readableBytes() + " bytes");

        //player_handler.sendPacket(new CustomPayloadS2CPacket(ServerPackets.BLOCK_ID_MAP_ID, buf));
        packetSender.sendPacket(ModPackets.ID_MAP, result);
    }

    /**
     * Construct the Block ID-map as a buffer
     *
     * @author   Jelle De Loecker   <jelle@elevenways.be>
     * @since    0.1.0
     */
    private static PacketByteBuf getBlockIdMap(PolyvalentAttachments attachments) {

        PacketByteBuf id_buf = Polyvalent.buf();

        PolyvalentMap map = attachments.getPolyvalentMap();

        if (map == null) {
            return null;
        }

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

    /**
     * Construct the Item ID-map as a buffer
     *
     * @author   Jelle De Loecker   <jelle@elevenways.be>
     * @since    0.1.0
     */
    private static PacketByteBuf getItemMap(PolyvalentAttachments attachments) {

        PacketByteBuf item_buf = Polyvalent.buf();
        int nbt_count = 0;

        PacketByteBuf items = Polyvalent.buf();
        PolyvalentMap map = attachments.getPolyvalentMap();

        for (Item item : Registry.ITEM) {
            Identifier id = Registry.ITEM.getId(item);

            if (!Polyvalent.requiresPoly(id)) {
                continue;
            }

            boolean is_armor = false;
            int type = 0;
            ArmorItem armor_item = null;

            int raw_client_id = attachments.getPolyvalentMap().getClientItemRawId(item, null);

            // Create a dummy stack of the item
            ItemStack example_stack = new ItemStack(item);

            // Get a converted itemstack for the client-side
            ItemStack poly_stack = map.getClientItem(example_stack, null, null);

            // Get the NBT data
            NbtCompound poly_nbt = poly_stack.getOrCreateNbt();

            // Get the poly item
            Item poly_item = poly_stack.getItem();

            // Get the client-side poly item id
            Identifier poly_item_id = Registry.ITEM.getId(poly_item);

            NbtCompound nbt = new NbtCompound();
            nbt.putInt("id", raw_client_id);
            nbt.putString("ns", id.getNamespace());
            nbt.putString("path", id.getPath());
            nbt.putString("poly", poly_item_id.toString());

            if (poly_nbt.contains("CustomModelData")) {
                nbt.putInt("cmd", poly_nbt.getInt("CustomModelData"));
            }

            if (item instanceof ArmorItem temp_armor_item) {
                is_armor = true;
                type = 1;
                armor_item = temp_armor_item;
            } else if (item instanceof BlockItem block_item) {
                type = 2;

                Block block = block_item.getBlock();
                Identifier block_id = Registry.BLOCK.getId(block);
                nbt.putString("block", block_id.toString());
            }

            nbt.putInt("t", type);

            if (is_armor) {
                nbt.putInt("maxDamage", armor_item.getMaxDamage());
                nbt.putInt("maxProtection", armor_item.getMaterial().getProtectionAmount(armor_item.getSlotType()));
                nbt.putFloat("maxToughness", armor_item.getMaterial().getToughness());
            }

            nbt_count++;
            items.writeNbt(nbt);
        }

        Polyvalent.log("Sending " + nbt_count + " item nbt");

        item_buf.writeVarInt(nbt_count);
        item_buf.writeBytes(items);

        return item_buf;
    }
}
