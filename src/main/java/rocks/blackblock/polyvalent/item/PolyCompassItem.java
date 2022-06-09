package rocks.blackblock.polyvalent.item;

import net.minecraft.item.CompassItem;
import net.minecraft.item.ItemStack;
import net.minecraft.item.ItemUsageContext;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.util.ActionResult;

public class PolyCompassItem extends CompassItem {

    public PolyCompassItem(Settings settings) {
        super(settings);
    }

    @Override
    public boolean hasGlint(ItemStack stack) {

        NbtCompound nbt = stack.getSubNbt("PolyCompass");

        if (nbt == null) {
            return false;
        }

        if (nbt.contains("Glint")) {
            return nbt.getBoolean("Glint");
        }

        return false;
    }

    @Override public ActionResult useOnBlock(ItemUsageContext context) {
        return ActionResult.FAIL;
    }

    @Override
    public String getTranslationKey(ItemStack stack) {
        return this.getTranslationKey();
    }
}
