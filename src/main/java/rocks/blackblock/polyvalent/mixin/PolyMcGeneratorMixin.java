package rocks.blackblock.polyvalent.mixin;

import io.github.theepicblock.polymc.api.PolyRegistry;
import io.github.theepicblock.polymc.impl.Util;
import io.github.theepicblock.polymc.impl.generator.*;
import net.minecraft.util.registry.Registry;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Overwrite;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import rocks.blackblock.polyvalent.polymc.PolyvalentBlockPolyGenerator;
import rocks.blackblock.polyvalent.polymc.PolyvalentGenerator;

import java.util.Comparator;
import java.util.function.BiConsumer;

@Mixin(Generator.class)
public class PolyMcGeneratorMixin {

    @Inject(method = "generateMissing", at = @At("HEAD"), cancellable = true, remap = false)
    private static void generateMissing(PolyRegistry builder, CallbackInfo ci) {

        generateMissingPolysWithoutPolyvalent(builder, Registry.ITEM, ItemPolyGenerator::addItemToBuilder, builder::hasItemPoly);
        generateMissingPolysWithoutPolyvalent(builder, Registry.BLOCK, BlockPolyGenerator::addBlockToBuilder, builder::hasBlockPoly);
        generateMissingPolysWithoutPolyvalent(builder, Registry.SCREEN_HANDLER, GuiGenerator::addGuiToBuilder, builder::hasGuiPoly);
        generateMissingPolysWithoutPolyvalent(builder, Registry.ENTITY_TYPE, EntityPolyGenerator::addEntityToBuilder, builder::hasEntityPoly);

        ci.cancel();
    }

    private static <T> void generateMissingPolysWithoutPolyvalent(PolyRegistry builder, Registry<T> registry, BiConsumer<T, PolyRegistry> generator, PolyvalentGenerator.BooleanFunction<T> contains) {

        registry.getEntrySet()
                .stream()
                .filter(entry -> !Util.isVanilla(entry.getKey().getValue()))
                .filter(entry -> !contains.accept(entry.getValue()))
                .filter(entry -> !entry.getKey().getValue().getNamespace().equals("polyvalent"))
                .sorted(Comparator.comparing(a -> a.getKey().getValue()))  // Compares the identifier
                .forEach(entry -> generator.accept(entry.getValue(), builder));
    }

}
