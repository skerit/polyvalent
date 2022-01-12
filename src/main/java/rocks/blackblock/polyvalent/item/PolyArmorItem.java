package rocks.blackblock.polyvalent.item;

import io.github.theepicblock.polymc.api.PolyRegistry;
import io.github.theepicblock.polymc.impl.poly.item.ArmorMaterialPoly;
import net.minecraft.entity.EquipmentSlot;
import net.minecraft.item.ArmorItem;
import net.minecraft.item.ArmorMaterial;
import net.minecraft.item.Item;
import rocks.blackblock.polyvalent.Polyvalent;

import java.util.HashMap;

public class PolyArmorItem extends ArmorItem {

    public static HashMap<Integer, PolyArmorItem> helmetMap = new HashMap<>();
    public static HashMap<Integer, PolyArmorItem> chestplateMap = new HashMap<>();
    public static HashMap<Integer, PolyArmorItem> leggingsMap = new HashMap<>();
    public static HashMap<Integer, PolyArmorItem> bootsMap = new HashMap<>();

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

        var helmetItem = Polyvalent.registerItem("helmet_" + suffix, new PolyArmorItem(material, EquipmentSlot.HEAD));
        var chestplateItem = Polyvalent.registerItem("chestplate_" + suffix, new PolyArmorItem(material, EquipmentSlot.CHEST));
        var leggingsItem = Polyvalent.registerItem("leggings_" + suffix, new PolyArmorItem(material, EquipmentSlot.LEGS));
        var bootsItem = Polyvalent.registerItem("boots_" + suffix, new PolyArmorItem(material, EquipmentSlot.FEET));

        helmetMap.put(armor_number, helmetItem);
        chestplateMap.put(armor_number, chestplateItem);
        leggingsMap.put(armor_number, leggingsItem);
        bootsMap.put(armor_number, bootsItem);
    }
}
