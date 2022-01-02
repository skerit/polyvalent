package rocks.blackblock.polyvalent.mixin;

import net.minecraft.network.ClientConnection;
import org.spongepowered.asm.mixin.Mixin;
import rocks.blackblock.polyvalent.Polyvalent;
import rocks.blackblock.polyvalent.networking.PolyvalentAttachments;
import rocks.blackblock.polyvalent.polymc.PolyvalentMap;

@Mixin(ClientConnection.class)
public class ClientConnectionMixin implements PolyvalentAttachments {

    private PolyvalentMap polyvalentMap = null;
    boolean isPolyvalent = false;

    @Override
    public void setIsPolyvalent(boolean isPolyvalent) {
        this.isPolyvalent = isPolyvalent;
        Polyvalent.log("Made connection polyvalent? " + isPolyvalent);
    }

    @Override
    public boolean getIsPolyvalent() {
        return this.isPolyvalent;
    }

    @Override
    public void setPolyvalentMap(PolyvalentMap polyvalentMap) {
        this.polyvalentMap = polyvalentMap;
    }

    @Override
    public PolyvalentMap getPolyvalentMap() {
        return this.polyvalentMap;
    }
}
