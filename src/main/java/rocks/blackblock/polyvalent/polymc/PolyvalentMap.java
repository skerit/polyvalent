package rocks.blackblock.polyvalent.polymc;

import com.google.common.collect.ImmutableMap;
import io.github.theepicblock.polymc.api.PolyMap;
import io.github.theepicblock.polymc.api.block.BlockPoly;
import io.github.theepicblock.polymc.api.entity.EntityPoly;
import io.github.theepicblock.polymc.api.gui.GuiPoly;
import io.github.theepicblock.polymc.api.item.ItemPoly;
import io.github.theepicblock.polymc.api.item.ItemTransformer;
import io.github.theepicblock.polymc.impl.PolyMapImpl;
import io.github.theepicblock.polymc.impl.poly.item.ArmorMaterialPoly;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityType;
import net.minecraft.item.ArmorMaterial;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.network.PacketByteBuf;
import net.minecraft.screen.ScreenHandlerType;
import net.minecraft.server.network.ServerPlayerEntity;
import org.jetbrains.annotations.Nullable;
import rocks.blackblock.polyvalent.Polyvalent;
import rocks.blackblock.polyvalent.block.PolyvalentBlock;
import rocks.blackblock.polyvalent.networking.TempPlayerLoginAttachments;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

public class PolyvalentMap extends PolyMapImpl {

    private final ImmutableMap<Item, ItemPoly> original_itemPolys;
    private final ItemTransformer[] original_globalItemPolys;
    private final ImmutableMap<Block, BlockPoly> original_blockPolys;
    private final ImmutableMap<ScreenHandlerType<?>, GuiPoly> original_guiPolys;
    private final ImmutableMap<EntityType<?>, EntityPoly<?>> original_entityPolys;
    private final ImmutableMap<ArmorMaterial, ArmorMaterialPoly> original_armorPolys;

    private ServerPlayerEntity player = null;
    private TempPlayerLoginAttachments attachments = null;
    private HashMap<Integer, Integer> server_to_client_ids = new HashMap<>();
    private HashMap<Integer, Integer> server_to_client_item_ids = new HashMap<>();
    private HashMap<Integer, Integer> client_to_server_item_ids = new HashMap<>();

    public PolyvalentMap(ImmutableMap<Item, ItemPoly> itemPolys, ItemTransformer[] globalItemPolys, ImmutableMap<Block, BlockPoly> blockPolys, ImmutableMap<ScreenHandlerType<?>, GuiPoly> guiPolys, ImmutableMap<EntityType<?>, EntityPoly<?>> entityPolys, ImmutableMap<ArmorMaterial, ArmorMaterialPoly> armorPolys) {
        super(itemPolys, globalItemPolys, blockPolys, guiPolys, entityPolys, armorPolys);
        this.original_itemPolys = itemPolys;
        this.original_globalItemPolys = globalItemPolys;
        this.original_blockPolys = blockPolys;
        this.original_guiPolys = guiPolys;
        this.original_entityPolys = entityPolys;
        this.original_armorPolys = armorPolys;
    }

    /**
     * Get the raw block id to send to the client
     *
     * @param   state         The block state to get the id of
     * @param   playerEntity  The player the id is for
     */
    public int getClientStateRawId(BlockState state, ServerPlayerEntity playerEntity) {

        // Get the state info as it is known to the server
        BlockState clientState = this.getClientBlock(state);
        int state_id = Block.STATE_IDS.getRawId(clientState);

        // Don't lookup vanilla ids
        if (state_id < 20342) {
            return state_id;
        }

        // If the state is different on the client, we need to use that id
        if (server_to_client_ids.containsKey(state_id)) {
            state_id = server_to_client_ids.get(state_id);
        }

        return state_id;
    }

    /**
     * Get the RawId of the client-side item
     */
    public int getClientItemRawId(Item item, ServerPlayerEntity playerEntity) {

        int server_side_raw_id = Item.getRawId(item);
        int client_side_raw_id;

        client_side_raw_id = server_to_client_item_ids.getOrDefault(server_side_raw_id, server_side_raw_id);

        return client_side_raw_id;
    }

    /**
     * Get the server-side RawId of the client-side item
     */
    public Item reverseClientItemRawId(int raw_id, ServerPlayerEntity playerEntity) {

        Item item = null;

        if (client_to_server_item_ids.containsKey(raw_id)) {
            raw_id = client_to_server_item_ids.get(raw_id);
        }

        return Item.byRawId(raw_id);
    }

    public PolyvalentMap createPlayerMap() {

        PolyvalentMap map = new PolyvalentMap(this.original_itemPolys, this.original_globalItemPolys, this.original_blockPolys, this.original_guiPolys, this.original_entityPolys, this.original_armorPolys);

        return map;
    }

    public PolyvalentMap createPlayerMap(ServerPlayerEntity player) {

        PolyvalentMap map = new PolyvalentMap(this.original_itemPolys, this.original_globalItemPolys, this.original_blockPolys, this.original_guiPolys, this.original_entityPolys, this.original_armorPolys);
        map.setPlayer(player);

        return map;
    }

    public void setServerToClientId(int server_id, int client_id) {
        server_to_client_ids.put(server_id, client_id);
    }

    public void setServerToClientItemId(int server_id, int client_id) {
        server_to_client_item_ids.put(server_id, client_id);
        client_to_server_item_ids.put(client_id, server_id);
    }

    public void setPlayer(ServerPlayerEntity player) {
        this.player = player;
        this.attachments = (TempPlayerLoginAttachments) player;
    }
}
