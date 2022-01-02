package rocks.blackblock.polyvalent.networking;

import io.github.theepicblock.polymc.api.PolyMap;
import net.minecraft.network.PacketByteBuf;
import rocks.blackblock.polyvalent.polymc.PolyvalentMap;

public interface TempPlayerLoginAttachments {
    void polyvalent_setWorldReload(boolean value);
    boolean polyvalent_getWorldReload();


    void setHasPolyvalent(boolean has_polyvalent);
    boolean hasPolyvalent();

    void setPolyvalentMap(PolyvalentMap map);
    PolyvalentMap getPolyvalentMap();

    void loadPolyvalentStates(int amount, PacketByteBuf buf);
}
