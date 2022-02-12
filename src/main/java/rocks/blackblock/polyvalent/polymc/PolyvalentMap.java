package rocks.blackblock.polyvalent.polymc;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;
import com.google.gson.JsonObject;
import io.github.theepicblock.polymc.api.PolyMcEntrypoint;
import io.github.theepicblock.polymc.api.SharedValuesKey;
import io.github.theepicblock.polymc.api.block.BlockPoly;
import io.github.theepicblock.polymc.api.entity.EntityPoly;
import io.github.theepicblock.polymc.api.gui.GuiPoly;
import io.github.theepicblock.polymc.api.item.ItemPoly;
import io.github.theepicblock.polymc.api.item.ItemTransformer;
import io.github.theepicblock.polymc.api.resource.PolyMcResourcePack;
import io.github.theepicblock.polymc.impl.PolyMapImpl;
import io.github.theepicblock.polymc.impl.misc.logging.SimpleLogger;
import io.github.theepicblock.polymc.impl.resource.ModdedResourceContainerImpl;
import io.github.theepicblock.polymc.impl.resource.ResourcePackImplementation;
import net.fabricmc.loader.api.FabricLoader;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.entity.EntityType;
import net.minecraft.item.ArmorMaterial;
import net.minecraft.item.Item;
import net.minecraft.screen.ScreenHandlerType;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.util.JsonHelper;
import org.jetbrains.annotations.Nullable;
import rocks.blackblock.polyvalent.item.PolyvalentArmorMaterialPoly;
import rocks.blackblock.polyvalent.networking.TempPlayerLoginAttachments;

import java.io.FileWriter;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.List;

public class PolyvalentMap extends PolyMapImpl {

    private final ImmutableMap<Item, ItemPoly> original_itemPolys;
    private final ItemTransformer[] original_globalItemPolys;
    private final ImmutableMap<Block, BlockPoly> original_blockPolys;
    private final ImmutableMap<ScreenHandlerType<?>, GuiPoly> original_guiPolys;
    private final ImmutableMap<EntityType<?>, EntityPoly<?>> original_entityPolys;
    private final ImmutableList<SharedValuesKey.ResourceContainer> original_sharedValueResources;
    private final ImmutableMap<ArmorMaterial, PolyvalentArmorMaterialPoly> original_armorPolys;

    private ServerPlayerEntity player = null;
    private TempPlayerLoginAttachments attachments = null;
    private HashMap<Integer, Integer> server_to_client_ids = new HashMap<>();
    private HashMap<Integer, Integer> server_to_client_item_ids = new HashMap<>();
    private HashMap<Integer, Integer> client_to_server_item_ids = new HashMap<>();

    public PolyvalentMap(ImmutableMap<Item, ItemPoly> itemPolys,
                         ItemTransformer[] globalItemPolys,
                         ImmutableMap<Block, BlockPoly> blockPolys,
                         ImmutableMap<ScreenHandlerType<?>, GuiPoly> guiPolys,
                         ImmutableMap<EntityType<?>, EntityPoly<?>> entityPolys,
                         ImmutableList<SharedValuesKey.ResourceContainer> sharedValueResources,
                         ImmutableMap<ArmorMaterial, PolyvalentArmorMaterialPoly> armorPolys
    ) {
        super(itemPolys, globalItemPolys, blockPolys, guiPolys, entityPolys, sharedValueResources);
        this.original_itemPolys = itemPolys;
        this.original_globalItemPolys = globalItemPolys;
        this.original_blockPolys = blockPolys;
        this.original_guiPolys = guiPolys;
        this.original_entityPolys = entityPolys;
        this.original_armorPolys = armorPolys;
        this.original_sharedValueResources = sharedValueResources;
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

        PolyvalentMap map = new PolyvalentMap(
                this.original_itemPolys,
                this.original_globalItemPolys,
                this.original_blockPolys,
                this.original_guiPolys,
                this.original_entityPolys,
                this.original_sharedValueResources,
                this.original_armorPolys
        );

        return map;
    }

    public PolyvalentMap createPlayerMap(ServerPlayerEntity player) {

        PolyvalentMap map = new PolyvalentMap(
                this.original_itemPolys,
                this.original_globalItemPolys,
                this.original_blockPolys,
                this.original_guiPolys,
                this.original_entityPolys,
                this.original_sharedValueResources,
                this.original_armorPolys
        );
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

    @Override
    public @Nullable PolyMcResourcePack generateResourcePack(SimpleLogger logger) {
        var moddedResources = new ModdedResourceContainerImpl();
        var pack = new PolyvalentResourcePack();

        //Let mods register resources via the api
        List<PolyMcEntrypoint> entrypoints = FabricLoader.getInstance().getEntrypoints("polymc", PolyMcEntrypoint.class);
        for (PolyMcEntrypoint entrypointEntry : entrypoints) {
            entrypointEntry.registerModSpecificResources(moddedResources, pack, logger);
        }

        // Hooks for all itempolys
        this.original_itemPolys.forEach((item, itemPoly) -> {
            try {
                itemPoly.addToResourcePack(item, moddedResources, pack, logger);
            } catch (Exception e) {
                logger.warn("Exception whilst generating resources for " + item.getTranslationKey());
                e.printStackTrace();
            }
        });

        // Hooks for all blockpolys
        this.original_blockPolys.forEach((block, blockPoly) -> {
            try {
                blockPoly.addToResourcePack(block, moddedResources, pack, logger);
            } catch (Exception e) {
                logger.warn("Exception whilst generating resources for " + block.getTranslationKey());
                e.printStackTrace();
            }
        });

        // Write the resources generated from shared values
        this.original_sharedValueResources.forEach((sharedValueResourceContainer) -> {
            try {
                sharedValueResourceContainer.addToResourcePack(moddedResources, pack, logger);
            } catch (Exception e) {
                logger.warn("Exception whilst generating resources for shared values: " + sharedValueResourceContainer);
                e.printStackTrace();
            }
        });

        System.out.println(" -- ARMOR POLYS: " + this.original_armorPolys.size());

        this.original_armorPolys.forEach((armor, armorPoly) -> {
            try {
                armorPoly.addToResourcePack(moddedResources, pack, logger);
            } catch (Exception e) {
                logger.warn("Exception whilst generating resources for armor: " + armor);
                e.printStackTrace();
            }
        });

        // Import the language files for all mods
        var languageKeys = new HashMap<String,HashMap<String, String>>(); // The first hashmap is per-language. Then it's translationkey->translation
        for (var lang : moddedResources.locateLanguageFiles()) {
            // Ignore fapi
            if (lang.getNamespace().equals("fabric")) continue;
            for (var stream : moddedResources.getInputStreams(lang.getNamespace(), lang.getPath())) {
                // Copy all of the language keys into the main map
                var languageObject = pack.getGson().fromJson(new InputStreamReader(stream, StandardCharsets.UTF_8), JsonObject.class);
                var mainLangMap = languageKeys.computeIfAbsent(lang.getPath(), (key) -> new HashMap<>());
                languageObject.entrySet().forEach(entry -> mainLangMap.put(entry.getKey(), JsonHelper.asString(entry.getValue(), entry.getKey())));
            }
        }
        // It doesn't actually matter which namespace the language files are under. We're just going to put them all under 'polymc-lang'
        languageKeys.forEach((path, translations) -> {
            pack.setAsset("polymc-lang", path, (location, gson) -> {
                try (var writer = new FileWriter(location.toFile())) {
                    gson.toJson(translations, writer);
                }
            });
        });

        // Import sounds
        for (var namespace : moddedResources.getAllNamespaces()) {
            var soundsRegistry = moddedResources.getSoundRegistry(namespace, "sounds.json");
            if (soundsRegistry == null) continue;
            pack.setSoundRegistry(namespace, "sounds.json", soundsRegistry);
            pack.importRequirements(moddedResources, soundsRegistry, logger);
        }

        try {
            moddedResources.close();
        } catch (Exception e) {
            e.printStackTrace();
            logger.error("Failed to close modded resources");
        }
        return pack;
    }
}
