package rocks.blackblock.polyvalent.mixin;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.hud.DebugHud;
import net.minecraft.util.Formatting;
import net.minecraft.util.Identifier;
import net.minecraft.util.hit.BlockHitResult;
import net.minecraft.util.hit.HitResult;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.registry.Registry;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
import org.spongepowered.asm.mixin.injection.callback.LocalCapture;
import rocks.blackblock.polyvalent.PolyvalentClient;
import rocks.blackblock.polyvalent.client.InternalClientRegistry;

import java.util.List;

@Environment(EnvType.CLIENT)
@Mixin(DebugHud.class)
public class DebugHudMixin {

    @Shadow
    private HitResult blockHit;

    @Shadow @Final
    private MinecraftClient client;

    @Inject(method = "getRightText", at = @At(value = "INVOKE", target = "Ljava/util/List;add(Ljava/lang/Object;)Z", ordinal = 2), locals = LocalCapture.CAPTURE_FAILSOFT)
    private void polymer_replaceString(CallbackInfoReturnable<List<String>> cir, long l, long m, long n, long o, List<String> list) {
        if (this.blockHit.getType() == HitResult.Type.BLOCK && InternalClientRegistry.enabled && InternalClientRegistry.enabled) {
            BlockPos blockPos = ((BlockHitResult)this.blockHit).getBlockPos();
            BlockState block_state = this.client.world.getBlockState(blockPos);

            int block_state_id = Block.STATE_IDS.getRawId(block_state);

            if (PolyvalentClient.actualBlockIdentifiers.containsKey(block_state_id)) {
                Identifier block_identifier = PolyvalentClient.actualBlockIdentifiers.get(block_state_id);

                list.add(block_identifier.toString());

                list.add("");
                list.add(Formatting.UNDERLINE + "Targeted Client Block: " + blockPos.getX() + ", " + blockPos.getY() + ", " + blockPos.getZ());
            }
        }
    }

}
