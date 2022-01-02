package rocks.blackblock.polyvalent.mixin;

import com.google.common.collect.ArrayTable;
import com.google.common.collect.HashBasedTable;
import com.google.common.collect.Table;
import net.minecraft.state.State;
import net.minecraft.state.property.Property;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Overwrite;
import org.spongepowered.asm.mixin.Shadow;
import com.google.common.collect.ArrayTable;
import com.google.common.collect.HashBasedTable;
import com.google.common.collect.ImmutableMap;
import com.google.common.collect.Maps;
import com.google.common.collect.Table;
import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Optional;
import java.util.function.Function;
import java.util.stream.Collectors;
import net.minecraft.state.property.Property;
import org.jetbrains.annotations.Nullable;
import rocks.blackblock.polyvalent.Polyvalent;

import java.util.Map;

@Mixin(State.class)
public abstract class StateMixin<O, S> {

    private HashMap<Property<?>, HashMap<Property<?>, Comparable<?>>> property_cache = new HashMap<>();

    @Shadow private Table<Property<?>, Comparable<?>, S> withTable;

    @Shadow @Final private ImmutableMap<Property<?>, Comparable<?>> entries;

    // @TODO: inject a clear property_cache upon return
    /*public void createWithTable(Map<Map<Property<?>, Comparable<?>>, S> states) {

        if (this.withTable != null) {
            throw new IllegalStateException();
        }

        HashBasedTable<Property, Comparable, S> table = HashBasedTable.create();

        for (Map.Entry entry : this.entries.entrySet()) {
            Property property = (Property)entry.getKey();
            for (Comparable comparable : property.getValues()) {
                if (comparable == entry.getValue()) continue;
                table.put(property, comparable, states.get(this.toMapWith(property, comparable)));
            }
        }
        this.withTable = table.isEmpty() ? table : ArrayTable.create(table);
    }*/

    /**
     * The result of `toMapWith` is only ever used to lookup a certain state once.
     * Re-creating it every time is incredibly slow when a block has many states (which Polyvalent blocks do)
     * Sometimes reaching up to 60 seconds to create a single block with 6000 states.
     * (Just because this map had to be created thousands and thousands of times)
     *
     * @author Jelle De Loecker
     */
    @Overwrite
    private Map<Property<?>, Comparable<?>> toMapWith(Property<?> property, Comparable<?> value) {

        HashMap<Property<?>, Comparable<?>> map = this.property_cache.computeIfAbsent(property, k -> Maps.newHashMap(this.entries));

        map.put(property, value);

        return map;
    }

}
