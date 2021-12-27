package rocks.blackblock.polyvalent.polymc;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;
import com.google.gson.JsonElement;
import com.google.gson.stream.JsonReader;
import io.github.theepicblock.polymc.api.PolyRegistry;
import io.github.theepicblock.polymc.api.block.BlockPoly;
import io.github.theepicblock.polymc.api.block.BlockStateManager;
import io.github.theepicblock.polymc.api.block.BlockStateProfile;
import io.github.theepicblock.polymc.api.resource.JsonBlockState;
import io.github.theepicblock.polymc.api.resource.ResourcePackMaker;
import io.github.theepicblock.polymc.impl.Util;
import io.github.theepicblock.polymc.impl.poly.block.SimpleReplacementPoly;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.state.property.Property;
import net.minecraft.util.Identifier;
import net.minecraft.util.registry.Registry;

import java.io.InputStreamReader;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.function.BiConsumer;
import java.util.function.Function;

public class PropertyRetainingUnusedBlocksStatePoly implements BlockPoly {
    private final ImmutableMap<BlockState,BlockState> states;
    private final Function<BlockState,BlockState> filter;
    private Property[] properties = null;
    private BlockStateProfile stateProfile;

    private static final BiConsumer<Block,PolyRegistry> DEFAULT_ON_FIRST_REGISTER = (block, polyRegistry) -> polyRegistry.registerBlockPoly(block, new SimpleReplacementPoly(block.getDefaultState()));

    /**
     * @param moddedBlock  the block this poly represents
     * @param registry     registry used to register this poly
     * @param filter       function that should remove all blockstates that you want to filter
     * @throws BlockStateManager.StateLimitReachedException when the clientSideBlock doesn't have any more BlockStates left.
     */
    public PropertyRetainingUnusedBlocksStatePoly(Block moddedBlock, PolyRegistry registry, Block replacementBlock, Property<?>[] filter) throws BlockStateManager.StateLimitReachedException {

        // Get the default blockstate values for the properties we want to retain
        Object[] defaultValues = new Object[filter.length];
        int i = -1;
        for (Property<?> p : filter) {
            i++;
            defaultValues[i] = (moddedBlock.getDefaultState().get(p));
        }

        stateProfile = new BlockStateProfile("temp", replacementBlock, blockState -> {

            boolean result = true;

            int i3 = -1;
            for (Property<?> p : filter) {
                i3++;
                result = blockState.get(p).equals(defaultValues[i3]);

                if (!result) {
                    break;
                }
            }

            return result;
        }, DEFAULT_ON_FIRST_REGISTER);

        Collection<Property<?>> moddedProperties = moddedBlock.getStateManager().getProperties();

        this.properties = filter;

        for (Property<?> p : filter) {
            if (!moddedProperties.contains(p)) {
                throw new IllegalArgumentException(String.format("[%s]: %s doesn't have property %s", this.getClass().getName(), moddedBlock.getTranslationKey(), p.getName()));
            }
        }


        Function<BlockState,BlockState> filterFunction = (blockstate) -> {
            int i2 = -1;
            for (Property<?> p : filter) {
                i2++;
                blockstate = with(blockstate, p, defaultValues[i2]);
            }
            return blockstate;
        };

        states = getBlockStateMap(moddedBlock, registry, stateProfile, filterFunction);
        this.filter = filterFunction;
    }

    @SuppressWarnings("unchecked")
    private <T extends Comparable<T>> BlockState with(BlockState b, Property<T> property, Object value) {
        return b.with(property, (T)value);
    }

    private ImmutableMap<BlockState,BlockState> getBlockStateMap(Block moddedBlock, PolyRegistry registry, BlockStateProfile stateProfile, Function<BlockState,BlockState> filter) throws BlockStateManager.StateLimitReachedException {
        ImmutableMap<BlockState,BlockState> states;
        BlockStateManager manager = registry.getBlockStateManager();

        ImmutableList<BlockState> unFilteredModdedStates = moddedBlock.getStateManager().getStates();

        //BlockState[] moddedStates = unFilteredModdedStates.stream().map(filter).toArray(BlockState[]::new);
        BlockState[] moddedStates = unFilteredModdedStates.toArray(BlockState[]::new);

        if (!manager.isAvailable(stateProfile, moddedStates.length)) {
            throw new BlockStateManager.StateLimitReachedException("Block doesn't have enough blockstates left. Profile: '" + stateProfile.name + "'");
        }

        BlockState defaultState = manager.requestBlockState(stateProfile);
        // Request 2 more?


        HashMap<BlockState,BlockState> res = new HashMap<>();
        for (BlockState state : moddedStates) {
            BlockState replacementState = defaultState;

            System.out.println("Requested: " + replacementState + " for " + state);

            for (Property<?> p : this.properties) {
                replacementState = copyProperty(replacementState, state, p);
            }

            res.put(state, replacementState);
        }
        states = ImmutableMap.copyOf(res);
        return states;
    }

    /**
     * Copies Property p from BlockState b into BlockState a
     */
    private <T extends Comparable<T>> BlockState copyProperty(BlockState a, BlockState b, Property<T> p) {
        return a.with(p, b.get(p));
    }

    @Override
    public BlockState getClientBlock(BlockState input) {
        BlockState result = states.get(filter.apply(input));

        if (result != null && this.properties != null) {
            for (Property<?> p : this.properties) {
                result = copyProperty(result, input, p);
            }
        }

        return result;
    }

    public void addToResourcePack(Block block, ResourcePackMaker pack) {
        Identifier moddedBlockId = Registry.BLOCK.getId(block);
        InputStreamReader blockStateReader = pack.getAsset(moddedBlockId.getNamespace(), ResourcePackMaker.BLOCKSTATES + moddedBlockId.getPath() + ".json");
        JsonBlockState moddedBlockStates = pack.getGson().fromJson(new JsonReader(blockStateReader), JsonBlockState.class);

        states.forEach((moddedState, clientState) -> {
            Identifier clientBlockId = Registry.BLOCK.getId(clientState.getBlock());
            JsonBlockState clientBlockStates = pack.getOrDefaultPendingBlockState(clientBlockId);
            String clientStateString = Util.getPropertiesFromBlockState(clientState);

            JsonElement moddedVariants = moddedBlockStates.getVariantBestMatching(moddedState);
            if (moddedVariants == null) pack.getLogger().warn("Couldn't get blockstate definition for " + moddedState);
            clientBlockStates.variants.put(clientStateString, moddedVariants);

            for (JsonBlockState.Variant v : JsonBlockState.getVariantsFromJsonElement(moddedVariants)) {
                Identifier vId = Identifier.tryParse(v.model);
                if (vId != null) pack.copyModel(new Identifier(v.model));
            }
        });
    }

    @Override
    public String getDebugInfo(Block obj) {
        StringBuilder out = new StringBuilder();
        out.append(states.size()).append(" states");
        states.forEach((moddedState, clientState) -> {
            out.append("\n");
            out.append("    #");
            out.append(moddedState);
            out.append(" -> ");
            out.append(clientState);
        });
        return out.toString();
    }
}