package rocks.blackblock.polyvalent.mixin;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.item.ItemStack;
import net.minecraft.network.packet.c2s.play.CreativeInventoryActionC2SPacket;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
import rocks.blackblock.polyvalent.Polyvalent;

@Environment(EnvType.CLIENT)
@Mixin(CreativeInventoryActionC2SPacket.class)
public class CreativeInventoryActionC2SPacketMixin {
    @Unique
    ItemStack polymer_cachedItemStack = null;

    @Inject(method = "getItemStack", at = @At("TAIL"), cancellable = true)
    private void polymer_replaceWithReal(CallbackInfoReturnable<ItemStack> cir) {

        ItemStack stack = cir.getReturnValue();

        Polyvalent.log("[CreativeInventoryActionC2SPacketMixin] getItemStack() called: " + stack);


    }
}