package rocks.blackblock.polyvalent.mixin;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.block.BlockState;
import net.minecraft.client.network.ClientPlayNetworkHandler;
import net.minecraft.client.world.ClientWorld;
import net.minecraft.network.packet.s2c.play.BlockUpdateS2CPacket;
import net.minecraft.network.packet.s2c.play.CustomPayloadS2CPacket;
import net.minecraft.network.packet.s2c.play.GameJoinS2CPacket;
import net.minecraft.network.packet.s2c.play.KeepAliveS2CPacket;
import net.minecraft.util.math.BlockPos;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import rocks.blackblock.polyvalent.Polyvalent;
import rocks.blackblock.polyvalent.networking.PolyvalentClientProtocol;
import rocks.blackblock.polyvalent.networking.PolyvalentClientProtocolHandler;
import rocks.blackblock.polyvalent.networking.PolyvalentHandshakeHandlerLogin;

@Environment(EnvType.CLIENT)
@Mixin(ClientPlayNetworkHandler.class)
public abstract class ClientPlayNetworkHandlerMixin {

    @Shadow
    public abstract ClientWorld getWorld();

    @Inject(method = "onGameJoin", at = @At("TAIL"))
    private void polyvalent_sendHandshake(GameJoinS2CPacket packet, CallbackInfo ci) {
        // Send a handshake packet to the server
        PolyvalentClientProtocol.sendHandshake((ClientPlayNetworkHandler) (Object) this);
    }

    @Inject(method = "onCustomPayload", at = @At("HEAD"), cancellable = true)
    private void polyvalent_catchPackets(CustomPayloadS2CPacket packet, CallbackInfo ci) {
        if (packet.getChannel().getNamespace().equals(Polyvalent.MOD_ID)) {
            var buf = ((CustomPayloadS2CPacketAccessor) packet).polyvalent_getData();
            PolyvalentClientProtocolHandler.handle((ClientPlayNetworkHandler) (Object) this, packet.getChannel(), buf);
            buf.release();
            ci.cancel();
        }
    }

    @Inject(method = "onKeepAlive", at = @At("HEAD"))
    private void polyvalent_handleHackfest(KeepAliveS2CPacket packet, CallbackInfo ci) {
        // When the server sends our special keep-alive packet, we know that the server is a Polyvalent server,
        // and we can now send our handshake packet.
        if (packet.getId() == PolyvalentHandshakeHandlerLogin.MAGIC_VALUE) {
            Polyvalent.log("Polyvalent server detected: received magic handshake packet.");
            PolyvalentClientProtocol.sendHandshake((ClientPlayNetworkHandler) (Object) this);
        }
    }
}