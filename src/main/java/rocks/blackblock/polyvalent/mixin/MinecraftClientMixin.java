package rocks.blackblock.polyvalent.mixin;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.network.ClientPlayNetworkHandler;
import net.minecraft.client.network.ClientPlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.util.Hand;
import net.minecraft.util.hit.BlockHitResult;
import net.minecraft.util.hit.HitResult;
import net.minecraft.util.math.BlockPos;
import org.jetbrains.annotations.Nullable;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import rocks.blackblock.polyvalent.Polyvalent;
import rocks.blackblock.polyvalent.PolyvalentClient;
import rocks.blackblock.polyvalent.client.PolyvalentBlockInfo;

@Environment(EnvType.CLIENT)
@Mixin(MinecraftClient.class)
public abstract class MinecraftClientMixin {

    @Shadow @Nullable public abstract ClientPlayNetworkHandler getNetworkHandler();

    @Shadow @Nullable public HitResult crosshairTarget;

    @Shadow @Nullable public ClientPlayerEntity player;

    @Inject(method = "doItemPick", at = @At("HEAD"), cancellable = true)
    private void doPickBlock(CallbackInfo ci) {

        if (!PolyvalentClient.connectedToPolyvalentServer) {
            return;
        }

        if (this.getNetworkHandler() == null) {
            return;
        }

        if (this.crosshairTarget == null) {
            return;
        }

        switch (this.crosshairTarget.getType()) {
            case BLOCK -> {
                BlockPos pos = ((BlockHitResult) this.crosshairTarget).getBlockPos();
                PolyvalentBlockInfo blockInfo = PolyvalentBlockInfo.getBlockInfoAt(pos);

                if (blockInfo == null) {
                    return;
                }

                ItemStack stack = blockInfo.getItemStack(true);

                if (stack != null) {
                    this.player.setStackInHand(Hand.MAIN_HAND, stack);
                    ci.cancel();
                }
            }
            case ENTITY -> {
                // Ignore for now
            }
        }
    }

}
