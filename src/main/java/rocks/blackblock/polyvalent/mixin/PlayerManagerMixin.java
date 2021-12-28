package rocks.blackblock.polyvalent.mixin;

import net.minecraft.network.ClientConnection;
import net.minecraft.server.PlayerManager;
import net.minecraft.server.network.ServerPlayerEntity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import rocks.blackblock.polyvalent.networking.PolyvalentServerProtocol;
import rocks.blackblock.polyvalent.networking.TempPlayerLoginAttachments;

@Mixin(PlayerManager.class)
public class PlayerManagerMixin {

    @Inject(method = "onPlayerConnect", at = @At("RETURN"))
    public void polyvalent_onPlayerConnect(ClientConnection connection, ServerPlayerEntity player, CallbackInfo ci) {

        TempPlayerLoginAttachments attachments = (TempPlayerLoginAttachments) player;

        if (attachments.hasPolyvalent()) {

            System.out.println("Sending blockidmap to the polyvalent client...");

            try {
                PolyvalentServerProtocol.sendBlockIdMap(player.networkHandler);
            } catch (Exception e) {
                System.out.println("Failed to send blockidmap to the client... " + e);
                e.printStackTrace();
            }
        }

    }
}
