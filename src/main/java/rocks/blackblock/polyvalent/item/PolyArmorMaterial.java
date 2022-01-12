package rocks.blackblock.polyvalent.item;

import net.minecraft.entity.EquipmentSlot;
import net.minecraft.item.ArmorMaterial;
import net.minecraft.item.Items;
import net.minecraft.recipe.Ingredient;
import net.minecraft.sound.SoundEvent;
import net.minecraft.sound.SoundEvents;

public class PolyArmorMaterial implements ArmorMaterial {

    public final String name;

    public PolyArmorMaterial(String name) {
        this.name = name;
    }

    @Override
    public int getDurability(EquipmentSlot slot) {
        return 50;
    }

    @Override
    public int getProtectionAmount(EquipmentSlot slot) {
        return 10;
    }

    @Override
    public int getEnchantability() {
        return 1;
    }

    @Override
    public SoundEvent getEquipSound() {
        return SoundEvents.ITEM_ARMOR_EQUIP_TURTLE;
    }

    @Override
    public Ingredient getRepairIngredient() {
        return Ingredient.ofItems(Items.IRON_INGOT);
    }

    @Override
    public String getName() {
        return this.name;
    }

    @Override
    public float getToughness() {
        return 3.0f;
    }

    @Override
    public float getKnockbackResistance() {
        return 0.0f;
    }
}
