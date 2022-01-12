package rocks.blackblock.polyvalent.mixin;

import net.minecraft.client.item.TooltipContext;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.text.LiteralText;
import net.minecraft.text.Text;
import net.minecraft.text.TranslatableText;
import net.minecraft.util.Formatting;
import net.minecraft.util.registry.Registry;
import org.jetbrains.annotations.Nullable;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.ModifyArg;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
import org.spongepowered.asm.mixin.injection.callback.LocalCapture;
import rocks.blackblock.polyvalent.item.PolyArmorItem;

import java.util.List;

@Mixin(ItemStack.class)
public abstract class ClientItemStackMixin {

    private static final String POLY_MC_ORIGINAL = "PolyMcOriginal";

    @Shadow public abstract boolean isDamaged();

    @Shadow public abstract int getMaxDamage();

    @Shadow public abstract int getDamage();

    @Shadow public abstract boolean hasNbt();

    @Shadow public abstract Item getItem();

    @Shadow private @Nullable NbtCompound nbt;

    @Inject(method = "getTooltip", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/item/TooltipContext;isAdvanced()Z", ordinal = 2), cancellable = true, locals = LocalCapture.CAPTURE_FAILSOFT)
    private void addPolyId(@Nullable PlayerEntity player, TooltipContext context, CallbackInfoReturnable<List<Text>> cir, List list) {
        //if (PolymerItemUtils.isPolymerServerItem((ItemStack) (Object) this)) {
            //cir.setReturnValue(list);
        //}

        // No need to do anything if this isn't for an advanced context tooltip view
        if (!context.isAdvanced()) {
            return;
        }

        ItemStack stack = (ItemStack) (Object) this;
        NbtCompound nbt = stack.getNbt();
        Item item = stack.getItem();

        if (this.isDamaged()) {
            int maxDamage;
            int damage;

            // PolyArmorItems have the correct damage values in their itemstack nbt data
            if (item instanceof PolyArmorItem armorItem && nbt.contains("maxDamage") && nbt.contains("originalDamage")) {
                maxDamage = nbt.getInt("maxDamage");
                damage = nbt.getInt("originalDamage");
            } else {
                maxDamage = this.getMaxDamage();
                damage = this.getDamage();
            }

            list.add(new TranslatableText("item.durability", maxDamage - damage, maxDamage));
        }

        String item_id;

        if (nbt != null && nbt.contains(POLY_MC_ORIGINAL)) {
            NbtCompound original = nbt.getCompound(POLY_MC_ORIGINAL);
            item_id = original.getString("id");
        } else {
            item_id = Registry.ITEM.getId(this.getItem()).toString();
        }

        list.add(new LiteralText(item_id).formatted(Formatting.DARK_GRAY));

        if (this.hasNbt()) {
            list.add(new TranslatableText("item.nbt_tags", this.nbt.getKeys().size()).formatted(Formatting.DARK_GRAY));
        }

        cir.setReturnValue(list);
    }

    @ModifyArg(method = "getTooltip", at = @At(value = "INVOKE", target = "Lnet/minecraft/text/LiteralText;<init>(Ljava/lang/String;)V", ordinal = 3))
    private String polymer_changeId(String id) {
        ItemStack stack = (ItemStack) (Object) this;
        //return stack.hasNbt() && stack.getNbt().contains(PolymerItemUtils.POLYMER_ITEM_ID) ? stack.getNbt().getString(PolymerItemUtils.POLYMER_ITEM_ID) : id;

        return "PolyvalentTest";

        //return id;
    }
}
