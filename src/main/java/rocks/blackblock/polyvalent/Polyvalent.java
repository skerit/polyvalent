package rocks.blackblock.polyvalent;

import io.github.theepicblock.polymc.api.block.BlockStateProfile;
import io.netty.buffer.Unpooled;
import net.fabricmc.api.ModInitializer;
import net.fabricmc.fabric.api.item.v1.FabricItemSettings;
import net.fabricmc.loader.api.FabricLoader;
import net.fabricmc.loader.api.ModContainer;
import net.minecraft.block.Block;
import net.minecraft.block.Material;
import net.minecraft.entity.EquipmentSlot;
import net.minecraft.item.*;
import net.minecraft.network.PacketByteBuf;
import net.minecraft.util.Identifier;
import net.minecraft.util.registry.Registry;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import rocks.blackblock.polyvalent.block.*;
import rocks.blackblock.polyvalent.item.PolyArmorItem;
import rocks.blackblock.polyvalent.networking.ModPacketsC2S;

public class Polyvalent implements ModInitializer {

	public static final String MC_NAMESPACE = "minecraft";

	public static final boolean DETECTED_POLYMC = FabricLoader.getInstance().isModLoaded("polymc");

	private static final ModContainer CONTAINER = FabricLoader.getInstance().getModContainer("polyvalent").get();
	public static final String MOD_ID = CONTAINER.getMetadata().getId();
	public static final String NAME = CONTAINER.getMetadata().getName();
	public static final String DESCRIPTION = CONTAINER.getMetadata().getDescription();
	public static final String VERSION = CONTAINER.getMetadata().getVersion().getFriendlyString().split("\\+")[0];

	public static final boolean ENABLE_NETWORKING_CLIENT = true;


	public final static Identifier CHANNEL_ID = new Identifier(MOD_ID,"enabled");

	// This logger is used to write text to the console and the log file.
	// It is considered best practice to use your mod id as the logger's name.
	// That way, it's clear which mod wrote info, warnings, and errors.
	public static final Logger LOGGER = LogManager.getLogger("polyvalent");

	public static final PolyFullBlock WOOD_BLOCK = PolyvalentBlock.createMaterialBlock("wood", Material.WOOD);
	public static final PolyFullBlock STONE_BLOCK = PolyvalentBlock.createMaterialBlock("stone", Material.STONE);
	public static final PolyFullBlock GLOW_BLOCK = PolyvalentBlock.createGlowBlock("glow", Material.STONE);
	public static final PolyTransparentBlock GLASS_BLOCK = PolyvalentBlock.createTransparentBlock("glass", Material.GLASS);
	public static final PolySlabBlock SLAB_BLOCK = PolyvalentBlock.createSlabBlock("slab", Material.STONE);
	public static final PolyLeavesBlock LEAVES_BLOCK = PolyvalentBlock.createLeavesBlock("leaves", Material.LEAVES);

	public static PacketByteBuf buf(int version) {
		var buf = new PacketByteBuf(Unpooled.buffer());
		return buf.writeVarInt(version);
	}

	public static Identifier id(String path) {
		return new Identifier(MOD_ID, path);
	}

	public static void log(Object o) {
		LOGGER.info("[Polyvalent] " + o);
	}

	@Override
	public void onInitialize() {

		// Create 100 armor sets
		/*for (int i = 1; i <= 100; i++) {
			PolyArmorItem.createSet(i);
		}*/

		// Create polyvalent blocks of each material
		//PolyvalentBlock.createMaterialBlock("glass", Material.GLASS);
		//PolyvalentBlock.createMaterialBlock("leaves", Material.LEAVES);
		//PolyvalentBlock.createMaterialBlock("soil", Material.SOIL);
		//PolyvalentBlock.createMaterialBlock("water", Material.WATER);

		ModPacketsC2S.register();
	}

	/**
	 * Register an item under the `polyvalent` namespace
	 *
	 * @param   item_name   The name of the item (without namespace)
	 * @param   item        The item instance to register
	 */
	public static <T extends Item> T registerItem(String item_name, T item) {
		return Registry.register(Registry.ITEM, new Identifier(MOD_ID, item_name), item);
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
}
