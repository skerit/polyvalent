package rocks.blackblock.polyvalent.mixin;

import net.minecraft.network.packet.c2s.play.CustomPayloadC2SPacket;
import net.minecraft.server.network.ServerPlayNetworkHandler;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import rocks.blackblock.polyvalent.Polyvalent;
import rocks.blackblock.polyvalent.networking.PolyvalentServerProtocolHandler;

@Mixin(ServerPlayNetworkHandler.class)
public abstract class ServerPlayNetworkHandlerMixin {
    @Inject(method = "onCustomPayload", at = @At("HEAD"))
    private void polymer_catchPackets(CustomPayloadC2SPacket packet, CallbackInfo ci) {
        if (packet.getChannel().getNamespace().equals(Polyvalent.MOD_ID)) {
            PolyvalentServerProtocolHandler.handle((ServerPlayNetworkHandler) (Object) this, packet.getChannel(), packet.getData());
        }
    }
}
