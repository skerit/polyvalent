package rocks.blackblock.polyvalent.polymc;

import io.github.theepicblock.polymc.PolyMc;
import io.github.theepicblock.polymc.api.PolyRegistry;
import io.github.theepicblock.polymc.api.item.CustomModelDataManager;
import io.github.theepicblock.polymc.api.item.ItemPoly;
import io.github.theepicblock.polymc.impl.poly.item.CustomModelDataPoly;
import io.github.theepicblock.polymc.impl.poly.item.DamageableItemPoly;
import net.minecraft.item.*;
import rocks.blackblock.polyvalent.Polyvalent;

/**
 * Class to automatically generate {@link ItemPoly}s for {@link Item}s
 */
public class PolyvalentItemPolyGenerator {
    /**
     * Generates the most suitable {@link ItemPoly} for a given {@link Item}
     */
    public static ItemPoly generatePoly(Item item, PolyRegistry builder) {

        var cmdManager = builder.getSharedValues(CustomModelDataManager.KEY);

        if (item instanceof ArmorItem armorItem && builder instanceof PolyvalentRegistry polyRegistry) {
            return new PolyvalentArmorItemPoly(polyRegistry, armorItem);
        }

        if (item instanceof ShieldItem) {
            return new DamageableItemPoly(cmdManager, item, Items.SHIELD);
        }
        if (item instanceof CompassItem) {
            return new CustomModelDataPoly(cmdManager, item, Items.COMPASS);
        }
        if (item instanceof CrossbowItem) {
            return new DamageableItemPoly(cmdManager, item, Items.CROSSBOW);
        }
        if (item instanceof RangedWeaponItem) {
            return new DamageableItemPoly(cmdManager, item);
        }
        if (item.isDamageable()) {
            if (item instanceof DyeableItem) {
                return new DamageableItemPoly(cmdManager, item, Items.LEATHER_HELMET);
            }
            return new DamageableItemPoly(cmdManager, item);
        }
        if (item.isFood()) {
            return new CustomModelDataPoly(cmdManager, item, CustomModelDataManager.FOOD_ITEMS);
        }
        if (item instanceof DyeableItem) {
            return new CustomModelDataPoly(cmdManager, item, Items.LEATHER_HORSE_ARMOR);
        }
        if (item instanceof BlockItem) {
            return new CustomModelDataPoly(cmdManager, item, Polyvalent.BLOCK_ITEM);
        }
        return new CustomModelDataPoly(cmdManager, item);
    }

    /**
     * Generates the most suitable {@link ItemPoly} and directly adds it to the {@link PolyRegistry}
     * @see #generatePoly(Item, PolyRegistry)
     */
    public static void addItemToBuilder(Item item, PolyRegistry builder) {
        try {
            builder.registerItemPoly(item, generatePoly(item, builder));
        } catch (Exception e) {
            PolyMc.LOGGER.error("Failed to generate a poly for item " + item.getTranslationKey());
            e.printStackTrace();
            PolyMc.LOGGER.error("Attempting to recover by using a default poly. Please report this");
            builder.registerItemPoly(item, (input, location) -> new ItemStack(Items.BARRIER));
        }
    }
}
