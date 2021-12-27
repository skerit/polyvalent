package rocks.blackblock.polyvalent.mixin;

import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;
import net.minecraft.network.ClientConnection;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.network.ServerLoginNetworkHandler;
import net.minecraft.server.network.ServerPlayerEntity;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import rocks.blackblock.polyvalent.Polyvalent;
import rocks.blackblock.polyvalent.networking.PolyvalentHandshakeHandlerLogin;

@Mixin(ServerLoginNetworkHandler.class)
public abstract class ServerLoginNetworkHandlerMixin {

    @Shadow
    @Final
    private MinecraftServer server;
    @Shadow @Final public ClientConnection connection;

    @Shadow protected abstract void addToServer(ServerPlayerEntity player);

    @Unique
    private Boolean checked_polyvalent_client = false;

    @Inject(method = "addToServer", at = @At("HEAD"), cancellable = true)
    private void polyvalent_prePlayHandshakeHackfest(ServerPlayerEntity player, CallbackInfo ci) {

        // If the player has already been checked, don't check again.
        if (this.checked_polyvalent_client) {
            return;
        }

        // Do a handshake check
        new PolyvalentHandshakeHandlerLogin(this.server, player, this.connection, (self) -> {
            this.checked_polyvalent_client = true;
            this.addToServer(player);
        });

        ci.cancel();
    }

}
