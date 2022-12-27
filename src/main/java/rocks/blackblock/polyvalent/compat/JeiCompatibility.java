package rocks.blackblock.polyvalent.compat;

import mezz.jei.api.IModPlugin;
import mezz.jei.api.constants.VanillaTypes;
import mezz.jei.api.registration.IRecipeRegistration;
import mezz.jei.api.registration.ISubtypeRegistration;
import mezz.jei.api.runtime.IIngredientManager;
import mezz.jei.api.runtime.IJeiRuntime;
import net.minecraft.item.ItemGroup;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.util.Identifier;
import rocks.blackblock.polyvalent.Polyvalent;
import rocks.blackblock.polyvalent.client.PolyvalentItemInfo;

import java.util.Collection;
import java.util.function.Predicate;

public class JeiCompatibility implements IModPlugin {
    private static final Identifier ID = new Identifier("polyvalent", "jei_plugin");
    private static final Predicate<ItemStack> SHOULD_REMOVE = (stack) -> (!Polyvalent.isVanilla(stack));

    @Override
    public void registerItemSubtypes(ISubtypeRegistration registration) {



        registration.registerSubtypeInterpreter(VanillaTypes.ITEM_STACK, Polyvalent.BLOCK_ITEM, (stack, uid_context) -> {
            NbtCompound nbt = stack.getNbt();
            Polyvalent.log("Has subtype? " + stack + " - " + nbt);

            if (nbt == null) {
                return "";
            }

            if (nbt.contains("PolyMcId")) {
                return nbt.getString("PolyMcId");
            }

            if (nbt.contains(Polyvalent.POLY_MC_ORIGINAL)) {
                NbtCompound original = nbt.getCompound(Polyvalent.POLY_MC_ORIGINAL);
                if (original.contains("id")) {
                    return original.getString("id");
                }
            }

            return "";
        });
    }

    @Override
    public void registerRecipes(IRecipeRegistration registration) {
        if (Polyvalent.isClient()) {
            update(registration.getIngredientManager());
        }
    }

    public void onRuntimeAvailable(IJeiRuntime jeiRuntime) {
        if (Polyvalent.isClient()) {
            //PolymerClientUtils.ON_CLEAR.register(() -> update(jeiRuntime.getIngredientManager()));
            //PolymerClientUtils.ON_SEARCH_REBUILD.register(() -> update(jeiRuntime.getIngredientManager()));
        }
    }

    private static void update(IIngredientManager manager) {
        synchronized (manager) {
            try {
                var list = manager.getAllIngredients(VanillaTypes.ITEM_STACK).stream().filter(SHOULD_REMOVE).toList();
                if (list.size() > 0) {
                    manager.removeIngredientsAtRuntime(VanillaTypes.ITEM_STACK, list);
                }

                for (var group : ItemGroup.GROUPS) {
                    if (group == ItemGroup.SEARCH) {
                        continue;
                    }

                    Collection<ItemStack> stacks = PolyvalentItemInfo.getStacks();

                    /*
                    if (group instanceof InternalClientItemGroup clientItemGroup) {
                        stacks = clientItemGroup.getStacks();
                    } else {
                        stacks = ((ClientItemGroupExtension) group).polymer_getStacks();
                    }*/

                    if (stacks != null && !stacks.isEmpty()) {
                        manager.addIngredientsAtRuntime(VanillaTypes.ITEM_STACK, stacks);
                    }
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    @Override
    public Identifier getPluginUid() {
        return ID;
    }
}
