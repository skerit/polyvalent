package rocks.blackblock.polyvalent.polymc;

import io.github.theepicblock.polymc.PolyMc;
import io.github.theepicblock.polymc.api.PolyRegistry;
import io.github.theepicblock.polymc.api.item.CustomModelDataManager;
import io.github.theepicblock.polymc.api.item.ItemPoly;
import io.github.theepicblock.polymc.api.resource.ResourcePackMaker;
import io.github.theepicblock.polymc.impl.generator.ItemPolyGenerator;
import io.github.theepicblock.polymc.impl.poly.item.*;
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

        if (item instanceof ArmorItem armorItem) {
            return new PolyvalentArmorItemPoly(builder, armorItem);
        }

        if (item instanceof ShieldItem) {
            return new ShieldPoly(builder.getCMDManager(), item);
        }
        if (item instanceof CompassItem) {
            return new CustomModelDataPoly(builder.getCMDManager(), item, Items.COMPASS);
        }
        if (item instanceof CrossbowItem) {
            return new PredicateBasedDamageableItem(builder.getCMDManager(), item, Items.CROSSBOW);
        }
        if (item instanceof RangedWeaponItem) {
            return new BowPoly(builder.getCMDManager(), item);
        }
        if (item.isDamageable()) {
            if (item instanceof DyeableItem) {
                return new DamageableItemPoly(builder.getCMDManager(), item, Items.LEATHER_HELMET);
            }
            return new DamageableItemPoly(builder.getCMDManager(), item);
        }
        if (item.isFood()) {
            return new CustomModelDataPoly(builder.getCMDManager(), item, CustomModelDataManager.FOOD_ITEMS);
        }
        if (item instanceof DyeableItem) {
            return new CustomModelDataPoly(builder.getCMDManager(), item, Items.LEATHER_HORSE_ARMOR);
        }
        if (item instanceof BlockItem) {
            return new CustomModelDataPoly(builder.getCMDManager(), item, Polyvalent.BLOCK_ITEM);
        }
        return new CustomModelDataPoly(builder.getCMDManager(), item);
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
            builder.registerItemPoly(item, new ItemPoly() {
                @Override
                public ItemStack getClientItem(ItemStack input) {
                    return new ItemStack(Items.BARRIER);
                }

                @Override public void addToResourcePack(Item item, ResourcePackMaker pack) {}
            });
        }
    }
}
