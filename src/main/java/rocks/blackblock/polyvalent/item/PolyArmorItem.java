package rocks.blackblock.polyvalent.item;

import net.minecraft.entity.EquipmentSlot;
import net.minecraft.item.*;
import net.minecraft.recipe.Ingredient;
import net.minecraft.sound.SoundEvent;
import rocks.blackblock.polyvalent.Polyvalent;

public class PolyArmorItem extends ArmorItem {
    public PolyArmorItem(ArmorMaterial material, EquipmentSlot slot, Item.Settings settings) {
        super(material, slot, settings);
    }

    public PolyArmorItem(ArmorMaterial material, EquipmentSlot slot) {
        //.group(ItemGroup.COMBAT)
        this(material, slot, new Item.Settings());
    }

    /**
     * Register a set of armor items.
     *
     * @param   armor_number   The armor number
     */
    public static void createSet(int armor_number) {

        // The suffix
        String suffix = "";

        if (armor_number < 10) {
            suffix = "0" + armor_number;
        } else {
            suffix = "" + armor_number;
        }

        // A new armor material has to be made, as these actually define the texture
        ArmorMaterial material = new PolyArmorMaterial("polyvalent_armor_" + suffix);

        Polyvalent.registerItem("helmet_" + suffix, new PolyArmorItem(material, EquipmentSlot.HEAD));
        Polyvalent.registerItem("chestplate_" + suffix, new PolyArmorItem(material, EquipmentSlot.CHEST));
        Polyvalent.registerItem("leggings_" + suffix, new PolyArmorItem(material, EquipmentSlot.LEGS));
        Polyvalent.registerItem("boots_" + suffix, new PolyArmorItem(material, EquipmentSlot.FEET));
    }
}
