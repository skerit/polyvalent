package rocks.blackblock.polyvalent.mixin;

import net.minecraft.server.network.ServerPlayerEntity;
import org.spongepowered.asm.mixin.Mixin;
import rocks.blackblock.polyvalent.networking.PolyvalentAttachments;
import rocks.blackblock.polyvalent.polymc.PolyvalentMap;

@Mixin(ServerPlayerEntity.class)
public class ServerPlayerEntityMixin implements PolyvalentAttachments {

    PolyvalentMap polyvalentMap = null;
    boolean isPolyvalent = false;

    @Override
    public void setIsPolyvalent(boolean isPolyvalent) {
        this.isPolyvalent = isPolyvalent;
    }

    @Override
    public boolean getIsPolyvalent() {
        return this.isPolyvalent;
    }

    @Override
    public void setPolyvalentMap(PolyvalentMap polyvalentMap) {
        this.isPolyvalent = polyvalentMap != null;
        this.polyvalentMap = polyvalentMap;
        polyvalentMap.setPlayer((ServerPlayerEntity) (Object) this);
    }

    @Override
    public PolyvalentMap getPolyvalentMap() {
        return this.polyvalentMap;
    }
}
