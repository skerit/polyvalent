package rocks.blackblock.polyvalent.networking;

import rocks.blackblock.polyvalent.polymc.PolyvalentMap;

public interface PolyvalentAttachments {

    void setIsPolyvalent(boolean isPolyvalent);
    boolean getIsPolyvalent();

    void setPolyvalentMap(PolyvalentMap polyvalentMap);
    PolyvalentMap getPolyvalentMap();

    default boolean hasPolyvalentMap() {
        return getPolyvalentMap() != null;
    }

}
