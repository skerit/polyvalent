package rocks.blackblock.polyvalent;

import io.github.theepicblock.polymc.api.PolyMcEntrypoint;
import io.github.theepicblock.polymc.api.block.BlockStateProfile;
import io.github.theepicblock.polymc.api.misc.PolyMapProvider;
import net.fabricmc.api.DedicatedServerModInitializer;
import net.fabricmc.loader.api.FabricLoader;
import net.luckperms.api.LuckPerms;
import net.luckperms.api.LuckPermsProvider;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import rocks.blackblock.polyvalent.block.PolyvalentBlock;
import rocks.blackblock.polyvalent.networking.ModPacketsC2S;
import rocks.blackblock.polyvalent.networking.PolyvalentAttachments;
import rocks.blackblock.polyvalent.polymc.PolyvalentGenerator;
import rocks.blackblock.polyvalent.polymc.PolyvalentMap;
import rocks.blackblock.polyvalent.polymc.PolyvalentRegistry;
import rocks.blackblock.polyvalent.server.PolyvalentCommands;

import java.util.HashMap;
import java.util.List;

/**
 * The server-side Polyvalent class
 *
 * @author   Jelle De Loecker   <jelle@elevenways.be>
 * @since    0.1.0
 */
public class PolyvalentServer implements DedicatedServerModInitializer {

    // The main (non player-specific) polyvalent map
    private static PolyvalentMap main_map = null;

    // The LuckPerms instance, if it's found
    private static LuckPerms luckPerms = null;

    // If we've tried to get luckperms already
    private static Boolean triedLuckPerms = false;

    // The PolyMC profiles to use for full material blocks
    public static final BlockStateProfile WOOD_BLOCK_PROFILE = Polyvalent.createBlockStateProfile("polyvalent_material_block", Polyvalent.WOOD_BLOCKS);
    public static final BlockStateProfile GLOW_BLOCK_PROFILE = Polyvalent.createBlockStateProfile("glow_material_block", Polyvalent.GLOW_BLOCKS);
    public static final BlockStateProfile STONE_BLOCK_PROFILE = Polyvalent.createBlockStateProfile("stone_material_block", Polyvalent.STONE_BLOCKS);
    public static final BlockStateProfile GLASS_BLOCK_PROFILE = Polyvalent.createBlockStateProfile("glass_material_block", Polyvalent.GLASS_BLOCKS);
    public static final BlockStateProfile LEAVES_BLOCK_PROFILE = Polyvalent.createBlockStateProfile("leaves_block", Polyvalent.LEAVES_BLOCKS);
    public static final BlockStateProfile SLAB_PROFILE = Polyvalent.createBlockStateProfile("slabs", Polyvalent.SLAB_BLOCKS);
    public static final BlockStateProfile PORTAL_PROFILE = Polyvalent.createBlockStateProfile("portals", Polyvalent.PORTAL_BLOCKS);
    public static final BlockStateProfile CARPET_PROFILE = Polyvalent.createBlockStateProfile("carpets", Polyvalent.CARPET_BLOCKS);
    public static final BlockStateProfile NO_COLLISION_CARPET_PROFILE = Polyvalent.createBlockStateProfile("carpets_nc", Polyvalent.NO_COLLISION_CARPET_BLOCKS);

    public static final HashMap<String, Integer> BLOCK_STATE_ID_MAP = new HashMap<>();

    /**
     * Is LuckPerms loaded?
     *
     * @author   Jelle De Loecker   <jelle@elevenways.be>
     * @since    0.1.0
     */
    public static boolean hasLuckPerms() {
        if (luckPerms != null) {
            return true;
        }

        return FabricLoader.getInstance().isModLoaded("luckperms");
    }

    /**
     * Try to get the Luckperms instance
     *
     * @author   Jelle De Loecker   <jelle@elevenways.be>
     * @since    0.1.0
     */
    public static LuckPerms getLuckPerms() {

        if (!triedLuckPerms && luckPerms == null) {
            triedLuckPerms = true;
            try {
                if (hasLuckPerms()) {
                    luckPerms = LuckPermsProvider.get();
                }
            } catch (Exception e) {
                Polyvalent.log("Failed to load LuckPerms! Polyvalent will not be able to use it.");
            }
        }

        return luckPerms;
    }

    /**
     * Generate the main polymap
     *
     * @deprecated this is an internal method you shouldn't call.
     *
     * @author   Jelle De Loecker   <jelle@elevenways.be>
     * @since    0.1.0
     */
    public static void generatePolyMap() {

        if (main_map != null) {
            throw new IllegalStateException("Polyvalent PolyMap already generated!");
        }

        Polyvalent.log("Generating Polyvalent map...");

        PolyvalentRegistry registry = new PolyvalentRegistry();

        // Register default global ItemPolys
        PolyvalentGenerator.addDefaultGlobalItemPolys(registry);

        // Let mods register polys via the api
        List<PolyMcEntrypoint> entrypoints = FabricLoader.getInstance().getEntrypoints("polymc", PolyMcEntrypoint.class);
        for (PolyMcEntrypoint entrypointEntry : entrypoints) {
            entrypointEntry.registerPolys(registry);
        }

        // Auto generate the rest
        PolyvalentGenerator.generateMissing(registry);

        main_map = registry.build();

        Polyvalent.log("Finished generated Polyvalent map");
    }

    /**
     * Returns the polymap built with Polyvalent
     *
     * @author   Jelle De Loecker   <jelle@elevenways.be>
     * @since    0.1.0
     *
     * @return the main PolyMap generated by polyvalent
     */
    @Deprecated
    public static PolyvalentMap getMainMap() {
        if (main_map == null) {
            throw new NullPointerException("Tried to access the PolyMap before it was initialized");
        }
        return main_map;
    }

    /**
     * Initialize server-side Polyvalent functionality
     *
     * @author   Jelle De Loecker   <jelle@elevenways.be>
     * @since    0.1.0
     */
    @Override
    public void onInitializeServer() {

        if (!Polyvalent.DETECTED_POLYMC) {
            throw new IllegalStateException("Polyvalent must be used together with PolyMc on the server");
        }

        Polyvalent.log("Registering Polyvalent commands...");

        PolyvalentCommands.registerCommands();

        // Let PolyMC use the player's custom Polyvalent map
        PolyMapProvider.EVENT.register(player -> {
            PolyvalentAttachments attachments = (PolyvalentAttachments) player.networkHandler.connection;
            return attachments.getPolyvalentMap();
        });

        // Register the custom packets used to
        // communicate between the server & client
        ModPacketsC2S.register();

        Polyvalent.log("Registering all block states...");

        // Create a map to all the state ids
        for (BlockState state : Block.STATE_IDS) {
            Block block = state.getBlock();

            if (block instanceof PolyvalentBlock) {
                String state_name = state.toString();
                int id = Block.STATE_IDS.getRawId(state);
                BLOCK_STATE_ID_MAP.put(state_name, id);
            }
        }

        Polyvalent.log("Finished registering all block states");
    }
}
