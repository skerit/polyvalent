package rocks.blackblock.polyvalent.client;

import it.unimi.dsi.fastutil.objects.Object2IntMap;
import it.unimi.dsi.fastutil.objects.Object2IntOpenHashMap;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.block.BlockState;
import net.minecraft.client.MinecraftClient;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.ChunkSectionPos;
import net.minecraft.world.chunk.ChunkStatus;
import net.minecraft.world.chunk.WorldChunk;
import org.jetbrains.annotations.ApiStatus;
import rocks.blackblock.polyvalent.networking.ClientPackets;

@ApiStatus.Internal
@Environment(EnvType.CLIENT)
public class InternalClientRegistry {
    public static boolean enabled = false;
    public static String serverVersion = "";
    public static final Object2IntMap<String> CLIENT_PROTOCOL = new Object2IntOpenHashMap<>();

    public static void setVersion(String version) {
        serverVersion = version;
        enabled = !version.isEmpty();
    }

    public static void disable() {
        setVersion("");
    }

    public static void registerClientProtocol(String id, int version) {
        CLIENT_PROTOCOL.put(id, version);
    }

    public static void registerClientProtocol(String id, int[] versions) {
        int best_supported = ClientPackets.getBestSupported(id, versions);
        registerClientProtocol(id, best_supported);
    }

    public static int getProtocol(String identifier) {
        return CLIENT_PROTOCOL.getOrDefault(identifier, -1);
    }

    public static BlockState getBlockAt(BlockPos pos) {
        if (MinecraftClient.getInstance().world != null) {
            WorldChunk chunk = MinecraftClient.getInstance().world.getChunkManager().getChunk(
                    ChunkSectionPos.getSectionCoord(pos.getX()), ChunkSectionPos.getSectionCoord(pos.getZ()),
                    ChunkStatus.FULL,
                    true
            );

            if (chunk == null) {
                return null;
            }

            return chunk.getBlockState(pos);
        }

        return null;
    }
}
