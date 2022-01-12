package rocks.blackblock.polyvalent.polymc;

import io.github.theepicblock.polymc.api.PolyRegistry;
import io.github.theepicblock.polymc.impl.poly.item.ArmorItemPoly;
import io.github.theepicblock.polymc.impl.poly.item.ArmorMaterialPoly;
import net.minecraft.entity.EquipmentSlot;
import net.minecraft.item.ArmorItem;
import net.minecraft.item.ArmorMaterial;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NbtCompound;
import rocks.blackblock.polyvalent.item.PolyArmorItem;
import rocks.blackblock.polyvalent.item.PolyvalentArmorMaterialPoly;

import java.util.HashMap;

public class PolyvalentArmorItemPoly extends ArmorItemPoly {

    public PolyvalentArmorItemPoly(PolyRegistry builder, ArmorItem base) {
        super(builder, base, getReplacement(builder, base));
    }

    @Override
    public boolean useColorId() {
        return false;
    }

    /**
     * Get the replacement item for the given armor
     */
    public static PolyArmorItem getReplacement(PolyRegistry builder, ArmorItem base) {

        EquipmentSlot slot = base.getSlotType();
        ArmorMaterial material = base.getMaterial();
        HashMap<Integer, PolyArmorItem> polyArmorMap;

        polyArmorMap = switch (slot) {
            case HEAD -> PolyArmorItem.helmetMap;
            case CHEST -> PolyArmorItem.chestplateMap;
            case LEGS -> PolyArmorItem.leggingsMap;
            case FEET -> PolyArmorItem.bootsMap;
            default -> null;
        };

        if (polyArmorMap == null) {
            return null;
        }

        //ArmorMaterialPoly materialPoly = builder.registerArmorMaterialPoly(material, (ArmorItemPoly) null);
        ArmorMaterialPoly materialPoly = builder.getArmorMaterialPoly(material);

        if (materialPoly == null) {
            materialPoly = new PolyvalentArmorMaterialPoly(material);
            builder.registerArmorMaterialPoly(material, materialPoly);
        }

        Integer armor_number = materialPoly.getNumber();

        if (armor_number == null) {
            return null;
        }

        PolyArmorItem polyArmorItem = polyArmorMap.get(armor_number);

        return polyArmorItem;
    }

    @Override
    public ItemStack getClientItem(ItemStack input) {
        Item originalItem = input.getItem();
        ItemStack result = super.getClientItem(input);

        if (!(originalItem instanceof ArmorItem armorItem)) {
            return result;
        }

        NbtCompound nbt = result.getOrCreateNbt();

        nbt.putInt("maxDamage", originalItem.getMaxDamage());
        nbt.putInt("originalDamage", input.getDamage());

        return result;
    }

}
