package mods.thecomputerizer.scriptify.util;

import mcp.MethodsReturnNonnullByDefault;

import javax.annotation.Nullable;
import javax.annotation.ParametersAreNonnullByDefault;
import java.util.*;
import java.util.function.BiFunction;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.function.Supplier;

/**
 * Wrappers that bundle generic helper methods for maps that contain Wrapperable instances as keys, values, or both.
 * Automatically returns null for methods with key inputs when the input is null.
 * TODO Implement use cases for this
 */
@MethodsReturnNonnullByDefault
@ParametersAreNonnullByDefault
public class Mapperable<K,V> implements Map<K,V>, Iterable<Map.Entry<K,V>> {

    public static <K,V> Mapperable<Wrapperable<K>,Wrapperable<V>> makeBoth(
            Supplier<Map<Wrapperable<K>,Wrapperable<V>>> supplier) {
        return new Both<>(supplier.get());
    }

    public static <K,V> Mapperable<Wrapperable<K>,V> makeKey(
            Supplier<Map<Wrapperable<K>,V>> supplier) {
        return new Key<>(supplier.get());
    }

    public static <K,V> Mapperable<K,Wrapperable<V>> makeValue(Supplier<Map<K,Wrapperable<V>>> supplier) {
        return new Value<>(supplier.get());
    }

    public static <K,V> Mapperable<Wrapperable<K>,Wrapperable<V>> makeSynchronizedBoth(
            Supplier<Map<Wrapperable<K>,Wrapperable<V>>> supplier) {
        return new Both<>(Collections.synchronizedMap(supplier.get()));
    }

    public static <K,V> Mapperable<Wrapperable<K>,V> makeSynchronizedKey(
            Supplier<Map<Wrapperable<K>,V>> supplier) {
        return new Key<>(Collections.synchronizedMap(supplier.get()));
    }

    public static <K,V> Mapperable<K,Wrapperable<V>> makeSynchronizedValue(
            Supplier<Map<K,Wrapperable<V>>> supplier) {
        return new Value<>(Collections.synchronizedMap(supplier.get()));
    }

    private final Map<K,V> map;

    private Mapperable(Map<K,V> map) {
        this.map = map;
    }

    @Override
    public void clear() {
        this.map.clear();
    }

    @Override
    public @Nullable V compute(@Nullable K key, BiFunction<? super K, ? super V, ? extends V> remappingFunc) {
        if(Objects.isNull(key)) return null;
        return this.map.computeIfPresent(key,remappingFunc);
    }

    @Override
    public @Nullable V computeIfAbsent(@Nullable K key, Function<? super K, ? extends V> mappingFunc) {
        if(Objects.isNull(key)) return null;
        return this.map.computeIfAbsent(key,mappingFunc);
    }

    @Override
    public @Nullable V computeIfPresent(@Nullable K key, BiFunction<? super K, ? super V, ? extends V> remappingFunc) {
        if(Objects.isNull(key)) return null;
        return this.map.computeIfPresent(key,remappingFunc);
    }

    @Override
    public boolean containsKey(@Nullable Object key) {
        return Objects.nonNull(key) && this.map.containsKey(key);
    }

    @Override
    public boolean containsValue(Object value) {
        return this.map.containsValue(value);
    }

    @Override
    public Set<Map.Entry<K,V>> entrySet() {
        return this.map.entrySet();
    }

    @Override
    public @Nullable V get(@Nullable Object key) {
        return Objects.nonNull(key) ? this.map.get(key) : null;
    }

    @Override
    public boolean isEmpty() {
        return this.map.isEmpty();
    }

    public boolean isNotEmpty() {
        return !isEmpty();
    }

    @Override
    public Set<K> keySet() {
        return this.map.keySet();
    }

    @Override
    public @Nullable V put(@Nullable K key, V value) {
        return Objects.nonNull(key) ? this.map.put(key,value) : null;
    }

    @Override
    public void putAll(Map<? extends K, ? extends V> otherMap) {
        this.map.putAll(otherMap);
    }

    @Override
    public @Nullable V putIfAbsent(@Nullable K key, V value) {
        if(Objects.isNull(key)) return null;
        return this.map.putIfAbsent(key,value);
    }

    @Override
    public @Nullable V remove(@Nullable Object key) {
        return Objects.nonNull(key) ? this.map.remove(key) : null;
    }

    @Override
    public int size() {
        return this.map.size();
    }

    @Override
    public Collection<V> values() {
        return this.map.values();
    }

    @Override
    public Iterator<Entry<K,V>> iterator() {
        return entrySet().iterator();
    }

    @Override
    public void forEach(Consumer<? super Entry<K,V>> action) {
        entrySet().forEach(action);
    }

    @Override
    public Spliterator<Entry<K,V>> spliterator() {
        return entrySet().spliterator();
    }

    public static class Both<K,V> extends Mapperable<Wrapperable<K>,Wrapperable<V>> {

        private Both(Map<Wrapperable<K>,Wrapperable<V>> map) {
            super(map);
        }
    }

    public static class Key<K,V> extends Mapperable<Wrapperable<K>,V> {

        private Key(Map<Wrapperable<K>, V> map) {
            super(map);
        }
    }



    public static class Value<K,V> extends Mapperable<K,Wrapperable<V>> {

        private Value(Map<K,Wrapperable<V>> map) {
            super(map);
        }
    }
}
