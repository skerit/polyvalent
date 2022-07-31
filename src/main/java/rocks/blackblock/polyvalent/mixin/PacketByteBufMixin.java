package rocks.blackblock.polyvalent.mixin;

import io.github.theepicblock.polymc.api.PolyMap;
import io.github.theepicblock.polymc.impl.Util;
import net.minecraft.item.Item;
import net.minecraft.network.PacketByteBuf;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.util.collection.IndexedIterable;
import net.minecraft.util.registry.Registry;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;
import rocks.blackblock.polyvalent.polymc.PolyvalentMap;
import xyz.nucleoid.packettweaker.PacketContext;

@Mixin(PacketByteBuf.class)
public abstract class PacketByteBufMixin {

    @Redirect(
            method = "writeRegistryValue",
            at = @At(
                    value = "INVOKE",
                    target = "Lnet/minecraft/util/collection/IndexedIterable;getRawId(Ljava/lang/Object;)I"
            )
    )
    public <T> int getIdRedirect(IndexedIterable<T> registry, T value) {

        if (registry == Registry.ITEM) {
            ServerPlayerEntity player = PacketContext.get().getTarget();
            PolyMap map = Util.tryGetPolyMap(player);
            if (map instanceof PolyvalentMap polyvalentMap) {
                Item item = (Item) value;
                return polyvalentMap.getClientItemRawId(item, player);
            }
        }

        return registry.getRawId(value);
    }

    @Redirect(
            method = "readRegistryValue",
            at = @At(
                    value = "INVOKE",
                    target = "Lnet/minecraft/util/collection/IndexedIterable;get(I)Ljava/lang/Object;"
            )
    )
    public <T> T reverseClientItemRawId(IndexedIterable<T> registry, int index) {

        // There are currently 1151 vanilla items, so we can also skip those
        if (registry == Registry.ITEM && index > 1150) {
            ServerPlayerEntity player = PacketContext.get().getTarget();
            PolyMap map = Util.tryGetPolyMap(player);
            if (map instanceof PolyvalentMap polyvalentMap) {
                return (T) polyvalentMap.reverseClientItemRawId(index, player);
            }
        }

        return registry.get(index);
    }
}
