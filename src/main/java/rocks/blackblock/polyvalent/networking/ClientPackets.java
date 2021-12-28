package rocks.blackblock.polyvalent.networking;

import it.unimi.dsi.fastutil.ints.IntArraySet;
import net.minecraft.util.Identifier;
import rocks.blackblock.polyvalent.Polyvalent;

import java.util.HashMap;
import java.util.Map;
import java.util.stream.IntStream;

public class ClientPackets {
    public static final Map<String, int[]> REGISTRY = new HashMap<>();
    public static final String HANDSHAKE = "handshake";
    public static final Identifier HANDSHAKE_ID = Polyvalent.id(HANDSHAKE);
    public static final String RAW_ID_SYNC = "raw_id_sync";
    public static final Identifier RAW_ID_SYNC_ID = Polyvalent.id(RAW_ID_SYNC);
    public static final String BLOCK_ID_MAP = "block_id_map";
    public static final Identifier BLOCK_ID_MAP_ID = Polyvalent.id(BLOCK_ID_MAP);

    public static int getBestSupported(String identifier, int[] ver) {

        var values = REGISTRY.get(identifier);

        if (values != null) {
            var verSet = new IntArraySet(ver);

            var value = IntStream.of(values).filter(verSet::contains).max();

            return value.isPresent() ? value.getAsInt() : -1;
        }

        return -1;
    }

    public static void register(String id, int... ver) {
        REGISTRY.put(id, ver);
    }

    static {
        register(HANDSHAKE, 0);
        register(RAW_ID_SYNC, 0);
        register(BLOCK_ID_MAP, 0);
    }
}
