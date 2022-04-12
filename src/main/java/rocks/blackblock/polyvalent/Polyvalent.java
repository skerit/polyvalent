package rocks.blackblock.polyvalent;

import io.github.theepicblock.polymc.api.block.BlockStateProfile;
import io.netty.buffer.Unpooled;
import net.fabricmc.api.ModInitializer;
import net.fabricmc.fabric.api.item.v1.FabricItemSettings;
import net.fabricmc.fabric.api.networking.v1.PacketByteBufs;
import net.fabricmc.loader.api.FabricLoader;
import net.fabricmc.loader.api.ModContainer;
import net.minecraft.block.Block;
import net.minecraft.block.Material;
import net.minecraft.client.MinecraftClient;
import net.minecraft.item.BlockItem;
import net.minecraft.item.Item;
import net.minecraft.network.PacketByteBuf;
import net.minecraft.util.Identifier;
import net.minecraft.util.registry.Registry;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import rocks.blackblock.polyvalent.block.*;
import rocks.blackblock.polyvalent.item.PolyArmorItem;
import rocks.blackblock.polyvalent.item.PolyBlockItem;
import rocks.blackblock.polyvalent.networking.ModPacketsC2S;

import java.util.HashMap;

/**
 * The main Polyvalent class that's available on both the client and the server.
 *
 * @author   Jelle De Loecker   <jelle@elevenways.be>
 * @since    0.1.0
 */
public class Polyvalent implements ModInitializer {

	public static final String MC_NAMESPACE = "minecraft";

	// The key PolyMC uses in NBT data
	public static final String POLY_MC_ORIGINAL = "PolyMcOriginal";

	// Place to store all polyvalent items
	public static final HashMap<Identifier, Item> ITEMS = new HashMap<>();

	// Is PolyMC loaded? (Can't do much without it)
	public static final boolean DETECTED_POLYMC = FabricLoader.getInstance().isModLoaded("polymc");

	// Get our own ModContainer instance
	private static final ModContainer CONTAINER = FabricLoader.getInstance().getModContainer("polyvalent").get();

	// Gather some mod metadata
	public static final String MOD_ID = CONTAINER.getMetadata().getId();
	public static final String NAME = CONTAINER.getMetadata().getName();
	public static final String DESCRIPTION = CONTAINER.getMetadata().getDescription();
	public static final String VERSION = CONTAINER.getMetadata().getVersion().getFriendlyString().split("\\+")[0];

	public static final boolean ENABLE_NETWORKING_CLIENT = true;

	// The logger polyvalent can use
	public static final Logger LOGGER = LogManager.getLogger("polyvalent");

	// All the available poly blocks
	public static final PolyFullBlock WOOD_BLOCK_ONE = PolyvalentBlock.createMaterialBlock("wood_one", Material.WOOD);
	public static final PolyFullBlock WOOD_BLOCK_TWO = PolyvalentBlock.createMaterialBlock("wood_two", Material.WOOD);

	public static final PolyFullBlock STONE_BLOCK_ONE = PolyvalentBlock.createMaterialBlock("stone_one", Material.STONE);
	public static final PolyFullBlock STONE_BLOCK_TWO = PolyvalentBlock.createMaterialBlock("stone_two", Material.STONE);
	public static final PolyFullBlock STONE_BLOCK_THREE = PolyvalentBlock.createMaterialBlock("stone_three", Material.STONE);
	public static final PolyFullBlock STONE_BLOCK_FOUR = PolyvalentBlock.createMaterialBlock("stone_four", Material.STONE);

	public static final PolyFullBlock SOIL_BLOCK_ONE = PolyvalentBlock.createMaterialBlock("soil_one", Material.SOIL);

	public static final PolyFullBlock GLOW_BLOCK_ONE = PolyvalentBlock.createGlowBlock("glow_one", Material.STONE);
	public static final PolyFullBlock GLOW_BLOCK_TWO = PolyvalentBlock.createGlowBlock("glow_two", Material.STONE);
	public static final PolyFullBlock GLOW_BLOCK_THREE = PolyvalentBlock.createGlowBlock("glow_three", Material.STONE);

	public static final PolyTransparentBlock GLASS_BLOCK_ONE = PolyvalentBlock.createTransparentBlock("glass_one", Material.GLASS);
	public static final PolyTransparentBlock GLASS_BLOCK_TWO = PolyvalentBlock.createTransparentBlock("glass_two", Material.GLASS);
	public static final PolyTransparentBlock GLASS_BLOCK_THREE = PolyvalentBlock.createTransparentBlock("glass_three", Material.GLASS);

	public static final PolySlabBlock SLAB_BLOCK_ONE = PolyvalentBlock.createSlabBlock("slab_one", Material.STONE);
	public static final PolySlabBlock SLAB_BLOCK_TWO = PolyvalentBlock.createSlabBlock("slab_two", Material.STONE);
	public static final PolySlabBlock SLAB_BLOCK_THREE = PolyvalentBlock.createSlabBlock("slab_three", Material.STONE);

	public static final PolyLeavesBlock LEAVES_BLOCK_ONE = PolyvalentBlock.createLeavesBlock("leaves_one", Material.LEAVES);
	public static final PolyLeavesBlock LEAVES_BLOCK_TWO = PolyvalentBlock.createLeavesBlock("leaves_two", Material.LEAVES);
	public static final PolyLeavesBlock LEAVES_BLOCK_THREE = PolyvalentBlock.createLeavesBlock("leaves_three", Material.LEAVES);

	public static final PolyPortalBlock PORTAL_BLOCK_ONE = PolyvalentBlock.createPortalBlock("portal_one", Material.PORTAL);

	public static final PolyCarpetBlock CARPET_BLOCK_ONE = PolyvalentBlock.createCarpetBlock("carpet_one", Material.CARPET);
	public static final PolyCarpetBlock NO_COLLISION_CARPET_BLOCK = PolyvalentBlock.createNoCollisionCarpetBlock("carpet_nc", Material.CARPET);

	public static final PolyFullBlock[] WOOD_BLOCKS = {WOOD_BLOCK_ONE, WOOD_BLOCK_TWO};
	public static final PolyFullBlock[] STONE_BLOCKS = {STONE_BLOCK_ONE, STONE_BLOCK_TWO, STONE_BLOCK_THREE, STONE_BLOCK_FOUR};
	public static final PolyFullBlock[] GLOW_BLOCKS = {GLOW_BLOCK_ONE, GLOW_BLOCK_TWO, GLOW_BLOCK_THREE};
	public static final PolyTransparentBlock[] GLASS_BLOCKS = {GLASS_BLOCK_ONE, GLASS_BLOCK_TWO, GLASS_BLOCK_THREE};
	public static final PolySlabBlock[] SLAB_BLOCKS = {SLAB_BLOCK_ONE, SLAB_BLOCK_TWO, SLAB_BLOCK_THREE};
	public static final PolyLeavesBlock[] LEAVES_BLOCKS = {LEAVES_BLOCK_ONE, LEAVES_BLOCK_TWO, LEAVES_BLOCK_THREE};
	public static final PolyFullBlock[] SOIL_BLOCKS = {SOIL_BLOCK_ONE};
	public static final PolyPortalBlock[] PORTAL_BLOCKS = {PORTAL_BLOCK_ONE};
	public static final PolyCarpetBlock[] CARPET_BLOCKS = {CARPET_BLOCK_ONE};
	public static final PolyCarpetBlock[] NO_COLLISION_CARPET_BLOCKS = {NO_COLLISION_CARPET_BLOCK};

	public static final PolyBlockItem BLOCK_ITEM = Polyvalent.registerItem("block_item", new PolyBlockItem(new Item.Settings()));

	/**
	 * See if the mod has been installed on a client
	 *
	 * @author   Jelle De Loecker   <jelle@elevenways.be>
	 * @since    0.1.0
	 */
	public static boolean isClient() {
		try {
			if (MinecraftClient.getInstance() != null) {
				return true;
			}
		} catch (NoClassDefFoundError e) {
			return false;
		}

		return false;
	}

	/**
	 * Create a new buffer with version 0
	 *
	 * @author   Jelle De Loecker   <jelle@elevenways.be>
	 * @since    0.1.0
	 */
	public static PacketByteBuf createPacketBuf() {
		return buf(0);
	}

	/**
	 * Create a new buffer with the given version
	 *
	 * @author   Jelle De Loecker   <jelle@elevenways.be>
	 * @since    0.1.0
	 *
	 * @param    version   The version of the buffer
	 */
	public static PacketByteBuf buf(int version) {
		var buf = new PacketByteBuf(Unpooled.buffer());
		return buf.writeVarInt(version);
	}

	/**
	 * Create a totally empty buffer
	 *
	 * @author   Jelle De Loecker   <jelle@elevenways.be>
	 * @since    0.1.0
	 */
	public static PacketByteBuf buf() {
		return PacketByteBufs.create();
	}

	/**
	 * Create a new identifier in our Polyvalent namespace
	 *
	 * @author   Jelle De Loecker   <jelle@elevenways.be>
	 * @since    0.1.0
	 *
	 * @param    path   The path part of the identifier
	 *
	 * @return   The identifier, eg: "polyvalent:my_path"
	 */
	public static Identifier id(String path) {
		return new Identifier(MOD_ID, path);
	}

	/**
	 * Log a message to the console
	 *
	 * @author   Jelle De Loecker   <jelle@elevenways.be>
	 * @since    0.1.0
	 *
	 * @param    obj   The object to log
	 */
	public static void log(Object obj) {
		LOGGER.info("[Polyvalent] " + obj);
	}

	/**
	 * Register an item under the `polyvalent` namespace
	 *
	 * @param   item_name   The name of the item (without namespace)
	 * @param   item        The item instance to register
	 */
	public static <T extends Item> T registerItem(String item_name, T item) {
		Identifier id = new Identifier(MOD_ID, item_name);
		Polyvalent.ITEMS.put(id, item);
		return Registry.register(Registry.ITEM, id, item);
	}

	/**
	 * Register a block under the `polyvalent` namespace
	 *
	 * @param   block_name   The name of the block (without namespace)
	 * @param   block        The block instance to register
	 */
	public static <T extends Block> T registerBlock(String block_name, T block) {
		return Registry.register(Registry.BLOCK, new Identifier(MOD_ID, block_name), block);
	}

	/**
	 * Register a block under the `polyvalent` namespace and also create a generic block item for it
	 *
	 * @param   block_name   The name of the block (without namespace)
	 * @param   block        The block instance to register
	 */
	public static <T extends Block> T registerBlockAndItem(String block_name, T block) {
		T registered_block = registerBlock(block_name, block);
		registerBlockItem(block_name, registered_block);
		return registered_block;
	}

	/**
	 * Register an item for the given block
	 *
	 * @param   block_name   The name of the block (without namespace)
	 * @param   block        The block instance to register
	 */
	public static BlockItem registerBlockItem(String block_name, Block block) {
		BlockItem block_item = new BlockItem(block, new FabricItemSettings());
		BlockItem item = registerItem(block_name, block_item);
		return item;
	}

	/**
	 * Create a blockstateprofile if PolyMC is loaded
	 */
	public static BlockStateProfile createBlockStateProfile(String name, Block block) {

		if (DETECTED_POLYMC) {
			return BlockStateProfile.getProfileWithDefaultFilter(name, block);
		}

		return null;
	}

	/**
	 * Create a blockstateprofile if PolyMC is loaded
	 */
	public static BlockStateProfile createBlockStateProfile(String name, Block[] blocks) {

		if (DETECTED_POLYMC) {
			return BlockStateProfile.getProfileWithDefaultFilter(name, blocks);
		}

		return null;
	}

	/**
	 * Returns true if this identifier requires a poly implementation
	 */
	public static boolean requiresPoly(Identifier id) {
		if (id == null) return false;

		String namespace = id.getNamespace();

		if (isNamespaceVanilla(namespace)) {
			return false;
		}

		return !namespace.equals(MOD_ID);
	}

	/**
	 * Returns true if this identifier is in the minecraft namespace
	 */
	public static boolean isVanilla(Identifier id) {
		if (id == null) return false;
		return isNamespaceVanilla(id.getNamespace());
	}

	/**
	 * Returns true if this namespace is minecraft
	 */
	public static boolean isNamespaceVanilla(String v) {
		return v.equals(MC_NAMESPACE);
	}

	/**
	 * Initialize the mod
	 *
	 * @author   Jelle De Loecker   <jelle@elevenways.be>
	 * @since    0.1.0
	 */
	@Override
	public void onInitialize() {

		// Create 100 armor sets
		for (int i = 1; i <= 100; i++) {
			PolyArmorItem.createSet(i);
		}
	}
}
