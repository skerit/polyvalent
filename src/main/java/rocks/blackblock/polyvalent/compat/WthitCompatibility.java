package rocks.blackblock.polyvalent.compat;

import mcp.mobius.waila.api.*;
import mcp.mobius.waila.api.component.ItemComponent;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.entity.ItemEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.util.Identifier;
import org.jetbrains.annotations.Nullable;
import rocks.blackblock.polyvalent.Polyvalent;
import rocks.blackblock.polyvalent.client.InternalClientRegistry;
import rocks.blackblock.polyvalent.client.PolyvalentBlockInfo;
import rocks.blackblock.polyvalent.client.PolyvalentItemInfo;

@SuppressWarnings("unused")
public class WthitCompatibility implements IWailaPlugin {
    private static final Identifier BLOCK_STATES = Identifier.tryParse("waila:show_states");

    @Override
    public void register(IRegistrar registrar) {
        registrar.addComponent(BlockOverride.INSTANCE, TooltipPosition.HEAD, Block.class, 1000);
        registrar.addComponent(BlockOverride.INSTANCE, TooltipPosition.BODY, Block.class, 1000);
        registrar.addComponent(BlockOverride.INSTANCE, TooltipPosition.TAIL, Block.class, 1000);
        registrar.addOverride(BlockOverride.INSTANCE, Block.class, 1000);
        registrar.addIcon(BlockOverride.INSTANCE, Block.class, 500);

        registrar.addComponent(ItemEntityOverride.INSTANCE, TooltipPosition.HEAD, ItemEntity.class, 1000);
        registrar.addComponent(ItemEntityOverride.INSTANCE, TooltipPosition.TAIL, ItemEntity.class, 1000);

        registrar.addEventListener(OtherOverrides.INSTANCE);
    }

    /**
     * Block overrides
     */
    private static class BlockOverride implements IBlockComponentProvider {
        public static final BlockOverride INSTANCE = new BlockOverride();

        @Override
        public @Nullable BlockState getOverride(IBlockAccessor accessor, IPluginConfig config) {
            BlockState block = InternalClientRegistry.getBlockAt(accessor.getPosition());

            if (block != null) {
                return block;
            }

            return null;
        }

        @Override
        public void appendHead(ITooltip tooltip, IBlockAccessor accessor, IPluginConfig config) {
            PolyvalentBlockInfo info = PolyvalentBlockInfo.getBlockInfoAt(accessor.getPosition());

            if (info != null) {
                IWailaConfig.Formatter formatter = IWailaConfig.get().getFormatter();
                tooltip.setLine(WailaConstants.OBJECT_NAME_TAG, formatter.blockName(info.getTitle()));

                if (config.getBoolean(WailaConstants.CONFIG_SHOW_REGISTRY)) {
                    tooltip.setLine(WailaConstants.REGISTRY_NAME_TAG, formatter.registryName(info.identifier.toString()));
                }
            }
        }

        @Override
        public ITooltipComponent getIcon(IBlockAccessor accessor, IPluginConfig config) {

            PolyvalentBlockInfo info = PolyvalentBlockInfo.getBlockInfoAt(accessor.getPosition());

            if (info != null) {
                ItemStack stack = info.getItemStackForIcon();

                if (stack != null) {
                    ItemComponent component = new ItemComponent(stack);
                    return component;
                }
            }

            return null;
        }

        @Override
        public void appendBody(ITooltip tooltip, IBlockAccessor accessor, IPluginConfig config) {
            if (config.getBoolean(BLOCK_STATES)) {

                PolyvalentBlockInfo info = PolyvalentBlockInfo.getBlockInfoAt(accessor.getPosition());

                if (info != null) {

                }
            }
        }

        @Override
        public void appendTail(ITooltip tooltip, IBlockAccessor accessor, IPluginConfig config) {
            if (config.getBoolean(WailaConstants.CONFIG_SHOW_MOD_NAME)) {

                PolyvalentBlockInfo info = PolyvalentBlockInfo.getBlockInfoAt(accessor.getPosition());

                if (info != null && info.identifier != null) {
                    IWailaConfig.Formatter formatter = IWailaConfig.get().getFormatter();

                    String mod_name = IModInfo.get(info.identifier).getName();
                    tooltip.setLine(WailaConstants.MOD_NAME_TAG, formatter.modName(mod_name));
                }
            }
        }
    }

    /**
     * ItemEntity overrides
     */
    private static final class ItemEntityOverride implements IEntityComponentProvider {
        public static final ItemEntityOverride INSTANCE = new ItemEntityOverride();

        @Override
        public void appendHead(ITooltip tooltip, IEntityAccessor accessor, IPluginConfig config) {

            ItemStack stack = accessor.<ItemEntity>getEntity().getStack();
            PolyvalentItemInfo info = PolyvalentItemInfo.of(stack);

            if (info == null) {
                return;
            }

            IWailaConfig.Formatter formatter = IWailaConfig.get().getFormatter();

            if (config.getBoolean(WailaConstants.CONFIG_SHOW_REGISTRY)) {
                tooltip.setLine(WailaConstants.REGISTRY_NAME_TAG, formatter.registryName(info.identifier));
            }

            String title = info.getTitle(stack);

            tooltip.setLine(WailaConstants.OBJECT_NAME_TAG, formatter.entityName(title));
        }

        @Override
        public void appendTail(ITooltip tooltip, IEntityAccessor accessor, IPluginConfig config) {
            if (config.getBoolean(WailaConstants.CONFIG_SHOW_MOD_NAME)) {
                ItemStack stack = accessor.<ItemEntity>getEntity().getStack();
                PolyvalentItemInfo info = PolyvalentItemInfo.of(stack);

                if (info == null) {
                    return;
                }

                IWailaConfig.Formatter formatter = IWailaConfig.get().getFormatter();
                String modname = info.getModName();

                tooltip.setLine(WailaConstants.MOD_NAME_TAG, formatter.modName(modname));
            }
        }
    }

    /**
     * Other overrides
     */
    private static class OtherOverrides implements IEventListener {
        public static final OtherOverrides INSTANCE = new OtherOverrides();

        @Override
        public @Nullable String getHoveredItemModName(ItemStack stack, IPluginConfig config) {

            PolyvalentItemInfo info = PolyvalentItemInfo.of(stack);

            if (info == null) {
                return null;
            }

            NbtCompound nbt = stack.getNbt();

            if (nbt != null && nbt.getBoolean(Polyvalent.HIDE_INFO)) {
                return null;
            }

            IWailaConfig.Formatter formatter = IWailaConfig.get().getFormatter();
            String modname = info.getModName();

            return modname;
        }
    }
}
