package rocks.blackblock.polyvalent.mixin;

import net.luckperms.api.LuckPerms;
import net.luckperms.api.model.user.User;
import net.luckperms.api.model.user.UserManager;
import net.luckperms.api.node.Node;
import net.minecraft.network.ClientConnection;
import net.minecraft.server.PlayerManager;
import net.minecraft.server.network.ServerPlayerEntity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import rocks.blackblock.polyvalent.Polyvalent;
import rocks.blackblock.polyvalent.PolyvalentServer;
import rocks.blackblock.polyvalent.networking.PolyvalentAttachments;
import rocks.blackblock.polyvalent.networking.TempPlayerLoginAttachments;

import java.util.concurrent.CompletableFuture;

@Mixin(PlayerManager.class)
public class PlayerManagerMixin {

    
    @Inject(method = "onPlayerConnect", at = @At("RETURN"))
    public void polyvalent_onPlayerConnect(ClientConnection connection, ServerPlayerEntity player, CallbackInfo ci) {

        PolyvalentAttachments attachments = (PolyvalentAttachments) connection;

        if (attachments.getIsPolyvalent()) {

            try {
                if (PolyvalentServer.hasLuckPerms()) {
                    LuckPerms luckPerms = PolyvalentServer.getLuckPerms();

                    if (luckPerms != null) {
                        UserManager userManager = luckPerms.getUserManager();
                        CompletableFuture<User> userFuture = userManager.loadUser(player.getUuid());

                        userFuture.thenAcceptAsync(user -> {
                            user.data().add(Node.builder("polyvalent.hasclient").build());
                            luckPerms.getUserManager().saveUser(user);
                        });
                    }

                    Polyvalent.log("Enabled polyvalent.hasclient permission for " + player.getEntityName());
                }
            } catch (Exception e) {
                Polyvalent.log("Failed to set LuckPerms node: " + e.getMessage());
            }
        }

    }
}
