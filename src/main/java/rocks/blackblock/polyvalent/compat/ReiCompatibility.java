package rocks.blackblock.polyvalent.compat;

import me.shedaniel.rei.api.client.plugins.REIClientPlugin;
import me.shedaniel.rei.api.client.registry.entry.EntryRegistry;
import me.shedaniel.rei.api.client.subsets.SubsetsRegistry;
import me.shedaniel.rei.api.common.entry.EntryStack;
import me.shedaniel.rei.api.common.entry.type.VanillaEntryTypes;
import net.minecraft.item.ItemStack;
import rocks.blackblock.polyvalent.client.PolyvalentItemInfo;

import java.util.Collection;

public class ReiCompatibility implements REIClientPlugin {

    @Override
    public void registerSubsets(SubsetsRegistry registry) {

    }

    @Override
    public void registerEntries(EntryRegistry registry) {
        Collection<ItemStack> stacks = PolyvalentItemInfo.getStacks();

        for (ItemStack stack : stacks) {
            registry.addEntry(EntryStack.of(VanillaEntryTypes.ITEM, stack));
        }

    }

}
