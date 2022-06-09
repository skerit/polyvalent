package rocks.blackblock.polyvalent.mixin;


import com.mojang.datafixers.util.Pair;
import io.github.theepicblock.polymc.api.PolyMap;
import io.github.theepicblock.polymc.api.misc.PolyMapProvider;
import it.unimi.dsi.fastutil.ints.IntArrayList;
import it.unimi.dsi.fastutil.ints.IntList;
import net.minecraft.block.Blocks;
import net.minecraft.network.ClientConnection;
import net.minecraft.server.PlayerManager;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.tag.TagKey;
import net.minecraft.tag.TagPacketSerializer;
import net.minecraft.util.Identifier;
import net.minecraft.util.registry.*;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.ModifyArg;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import rocks.blackblock.polyvalent.polymc.PolyvalentMap;

import java.util.HashMap;
import java.util.Map;
import java.util.stream.Collectors;

@Mixin(PlayerManager.class)
public class PlayerManagerSyncTagsMixin {

    @Shadow @Final private DynamicRegistryManager.Immutable registryManager;
    private ServerPlayerEntity current_player = null;

    @Inject(method = "onPlayerConnect", at = @At("HEAD"))
    private void onPlayerConnect(ClientConnection connection, ServerPlayerEntity player, CallbackInfo ci) {
        this.current_player = player;
    }

    @ModifyArg(
            method = "onPlayerConnect",
            at = @At(
                    value = "INVOKE",
                    target = "Lnet/minecraft/network/packet/s2c/play/SynchronizeTagsS2CPacket;<init>(Ljava/util/Map;)V"
            )
    )
    private
    Map<RegistryKey<? extends Registry<?>>, TagPacketSerializer.Serialized>
    injected(Map<RegistryKey<? extends Registry<?>>, TagPacketSerializer.Serialized> serializedTags)
    {

        if (this.current_player == null) {
            return serializedTags;
        }

        PolyMap polyMap = PolyMapProvider.getPolyMap(this.current_player);

        if (polyMap == null) {
            return serializedTags;
        }

        if (polyMap instanceof PolyvalentMap polyvalentMap) {
            // Allowed
        } else {
            return serializedTags;
        }

        // Get the registry manager
        DynamicRegistryManager registryManager = this.registryManager;

        // Get the tag manager
        serializedTags = serializeTags(registryManager, polyvalentMap);

        return serializedTags;
    }

    private static
    Map<RegistryKey<? extends Registry<?>>, TagPacketSerializer.Serialized>
    serializeTags(DynamicRegistryManager dynamicRegistryManager, PolyvalentMap polyMap)
    {
        return dynamicRegistryManager.streamSyncedRegistries().map((entry) -> {
            return Pair.of(entry.key(), serializeTags(entry.value(), polyMap));
        }).filter((pair) -> {
            return !(pair.getSecond()).isEmpty();
        }).collect(Collectors.toMap(Pair::getFirst, Pair::getSecond));
    }

    private static
    <T> TagPacketSerializer.Serialized
    serializeTags(Registry<T> registry, PolyvalentMap polyMap)
    {
        HashMap<Identifier, IntList> map = new HashMap<>();

        registry.streamTagsAndEntries().forEach(pair -> {
            RegistryEntryList<T> registryEntryList = pair.getSecond();
            IntArrayList intList = new IntArrayList(registryEntryList.size());
            for (RegistryEntry<T> registryEntry : registryEntryList) {
                if (registryEntry.getType() != RegistryEntry.Type.REFERENCE) {
                    throw new IllegalStateException("Can't serialize unregistered value " + registryEntry);
                }

                int raw_id;

                if (registry == Registry.BLOCK) {
                    // Get the raw server-side id
                    raw_id = registry.getRawId(registryEntry.value());

                    // Get the raw id the client side uses (might be the same)
                    raw_id = polyMap.getClientBlockRawId(raw_id);
                } else {
                    raw_id = registry.getRawId(registryEntry.value());
                }

                intList.add(raw_id);
            }
            map.put((pair.getFirst()).id(), intList);
        });
        return new TagPacketSerializer.Serialized(map);
    }

}
