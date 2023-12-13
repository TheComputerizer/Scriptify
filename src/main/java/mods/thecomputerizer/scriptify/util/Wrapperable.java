package mods.thecomputerizer.scriptify.util;

import mcp.MethodsReturnNonnullByDefault;
import mods.thecomputerizer.scriptify.Scriptify;
import org.apache.commons.lang3.mutable.MutableInt;

import javax.annotation.Nullable;
import java.lang.reflect.Array;
import java.util.*;
import java.util.function.BiFunction;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.function.Supplier;
import java.util.stream.Stream;
import java.util.stream.StreamSupport;

/**
 * Wrapper used for bundling generic helper methods with a final Iterable instance
 */
@MethodsReturnNonnullByDefault
@SuppressWarnings({"unchecked", "unused"})
public class Wrapperable<E> implements Iterable<E> {

    /**
     * Helper method for iterable suppliers
     */
    public static <E> Wrapperable<E> make(Supplier<Iterable<E>> supplier) {
        return new Wrapperable<>(supplier.get(),false);
    }

    public static <E> Wrapperable<E> make(Supplier<Iterable<E>> supplier, Consumer<Wrapperable<E>> settings) {
        Wrapperable<E> bundle = make(supplier);
        settings.accept(bundle);
        return bundle;
    }

    /**
     * Synchronized versions of the make methods
     */
    public static <E> Wrapperable<E> makeSynchronized(Supplier<Iterable<E>> supplier) {
        return new Wrapperable<>(supplier.get(),true);
    }

    public static <E> Wrapperable<E> makeSynchronized(Supplier<Iterable<E>> supplier, Consumer<Wrapperable<E>> settings) {
        Wrapperable<E> bundle = make(supplier);
        settings.accept(bundle);
        return bundle;
    }

    /**
     * Array versions of the make methods
     */
    public static <E> Wrapperable<E> makeArray(E ... elements) {
        List<E> list = new ArrayList<>(Arrays.asList(elements));
        return make(() -> list);
    }
    public static <E> Wrapperable<E> makeArray(Consumer<Wrapperable<E>> settings, E ... elements) {
        List<E> list = new ArrayList<>(Arrays.asList(elements));
        return make(() -> list,settings);
    }

    public static <E> Wrapperable<E> makeArray(Supplier<E[]> supplier) {
        return makeArray(supplier.get());
    }

    public static <E> Wrapperable<E> makeArray(Supplier<E[]> supplier, Consumer<Wrapperable<E>> settings) {
        return makeArray(settings,supplier.get());
    }

    /**
     * Synchronized versions of the makeArray methods
     */
    public static <E> Wrapperable<E> makeSynchronizedArray(E ... elements) {
        List<E> list = new ArrayList<>(Arrays.asList(elements));
        return makeSynchronized(() -> list);
    }
    public static <E> Wrapperable<E> makeSynchronizedArray(Consumer<Wrapperable<E>> settings, E ... elements) {
        List<E> list = new ArrayList<>(Arrays.asList(elements));
        return makeSynchronized(() -> list,settings);
    }

    public static <E> Wrapperable<E> makeSynchronizedArray(Supplier<E[]> supplier) {
        return makeSynchronizedArray(supplier.get());
    }

    public static <E> Wrapperable<E> makeSynchronizedArray(Supplier<E[]> supplier, Consumer<Wrapperable<E>> settings) {
        return makeSynchronizedArray(settings,supplier.get());
    }

    private final Iterable<E> iterable;

    public Wrapperable(Iterable<E> iterable, boolean isSynchronized) {
        this.iterable = iterable;
    }

    public void add(E element) {
        if(this.iterable instanceof Collection<?>)
            ((Collection<E>)this.iterable).add(element);
        else throw new UnsupportedOperationException("Cannot add to non collection iterable!");
    }

    /**
     * This may have weird results if the collection instance does not preserce order
     */
    public void add(int index, E element) {
        if(this.iterable instanceof List<?>) ((List<E>)this.iterable).add(index,element);
        else {
            int count = size();
            if(count==0 || index==count) this.add(element);
            else {
                E[] elementCopy = (E[])Array.newInstance(element.getClass(),count+1);
                if(index<0 || index>count) index = count;
                count = 0;
                for(E e : this.iterable) {
                    if(count==index) {
                        elementCopy[count] = element;
                        count++;
                    }
                    elementCopy[count] = e;
                    count++;
                }
                set(elementCopy);
            }
        }
    }

    public void clear() {
        if(this.iterable instanceof Collection<?>)
            ((Collection<E>)this.iterable).clear();
        else throw new UnsupportedOperationException("Cannot clear non collection iterable!");
    }

    public Collection<E> get() {
        if(this.iterable instanceof Collection<?>)
            return (Collection<E>)this.iterable;
        else throw new UnsupportedOperationException("Cannot get non collection iterable!");
    }

    public @Nullable E get(int index) {
        if(this.iterable instanceof List<?>) return ((List<E>)this.iterable).get(index);
        int count = size();
        if(index<0 || index>count) index = count;
        for(E element : get()) {
            if(count==index) return element;
            count++;
        }
        return null;
    }

    public List<E> getAsList() throws ClassCastException  {
        return (List<E>)getAsType(List.class,null);
    }

    public List<E> getAsList(Function<Wrapperable<E>,? extends List<E>> onCastException) {
        return (List<E>)getAsType(List.class,onCastException);
    }

    public List<E> getAsSynchronizedList() throws ClassCastException  {
        return (List<E>)getAsType(List.class,null);
    }

    public List<E> getAsSynchronizedList(Function<Wrapperable<E>,? extends List<E>> onCastException) {
        return (List<E>)getAsType(List.class,onCastException);
    }

    public Set<E> getAsSet() throws ClassCastException  {
        return (Set<E>)getAsType(Set.class,null);
    }

    public Set<E> getAsSet(Function<Wrapperable<E>,? extends Set<E>> onCastException) {
        return (Set<E>)getAsType(Set.class,onCastException);
    }

    public <T extends Collection<E>> T getAsType(Class<T> clazz) throws ClassCastException {
        return getAsType(clazz,null);
    }

    /**
     * Syntactic sugar...
     */
    public <T extends Collection<E>> T getAsType(
            Class<T> clazz, @Nullable Function<Wrapperable<E>,? extends Collection<E>> onCastException) {
        try {
            return clazz.cast(this.iterable);
        } catch(Exception ex) {
            Object[] args = new Object[]{get().getClass().getName(),clazz.getName()};
            if(Objects.isNull(onCastException))
                throw new ClassCastException(Misc.translateLog(getClass(),"exception",null,args));
            Scriptify.logError(getClass(),null,ex,args);
            return (T)onCastException.apply(this);
        }
    }

    public Class<?> getElementClass() {
        E element = getNonNullElement();
        if(Objects.isNull(element)) {
            Scriptify.logWarn(getClass(),"warn");
            return Object.class;
        }
        return element.getClass();
    }

    /**
     * Runs getFirstMatching with Objects#equals as the matcherFunc
     */
    public @Nullable E getFirstEquals(@Nullable Iterable<E> otherItr) {
        return getFirstMatching(otherItr,Objects::equals);
    }

    /**
     * Iterates through the other iterable for each element of the wrapped iterable and returns the matching element.
     * Both iterables are assumed to be ordered or the result may be inconsistent
     */
    public @Nullable E getFirstMatching(@Nullable Iterable<E> otherItr, BiFunction<E,E,Boolean> matcherFunc) {
        if(Objects.isNull(otherItr)) return null;
        for(E element : this)
            for(E otherElement : otherItr)
                if(matcherFunc.apply(element,otherElement)) return element;
        return null;
    }

    /**
     * Gets the first element in the collection that is not null. Returns null if nothing is found
     */
    public @Nullable E getNonNullElement() {
        E element = null;
        for(E e : get()) {
            if(Objects.nonNull(e)) {
                element = e;
                break;
            }
        }
        return element;
    }

    public boolean isEmpty() {
        return this.iterable instanceof Collection<?> ? ((Collection<E>)this.iterable).isEmpty() : size()==0;
    }

    public boolean isList() {
        return this.iterable instanceof List<?>;
    }

    public boolean isNotEmpty() {
        return this.iterable instanceof Collection<?> ? !((Collection<E>)this.iterable).isEmpty() : size()>=0;
    }

    public boolean isSet() {
        return this.iterable instanceof Set<?>;
    }

    public boolean isUnique() {
        return !isList() && !isSet();
    }

    public void remove(E element) {
        if(this.iterable instanceof Collection<?>) ((Collection<E>)this.iterable).remove(element);
    }

    /**
     * This may have weird results if the collection instance does not preserce order or if the elements are integers
     */
    public void removeIndex(int index) {
        if(this.iterable instanceof List<?>) ((List<E>)this.iterable).remove(index);
        else {
            int count = size()-1;
            if(count<=0) clear();
            E removal = null;
            if(index<0 || index>count) index = count;
            count = 0;
            for(E e : this.iterable) {
                if(count==index) {
                    removal = e;
                    break;
                }
                count++;
            }
            if(Objects.nonNull(removal)) remove(removal);
            else Scriptify.logDebug(getClass(),"debug","removal",index);
        }
    }

    public final void set(E ... elements) {
        this.set(true,Arrays.asList(elements));
    }

    public final void set(boolean clearExisting, E ... elements) {
        this.set(clearExisting,Arrays.asList(elements));
    }

    public void set(Iterable<E> elements) {
        this.set(true,elements);
    }

    public void set(boolean clearExisting, Iterable<E> elements) {
        if(this.iterable instanceof Collection<?>) {
            Collection<E> c = (Collection<E>)this.iterable;
            if(clearExisting) c.clear();
            if(elements instanceof Collection<?>) c.addAll((Collection<E>)elements);
            else elements.forEach(c::add);
        } else throw new UnsupportedOperationException("Cannot set elements for non collection iterable!");
    }

    public int size() {
        if(this.iterable instanceof Collection<?>) return ((Collection<E>)this.iterable).size();
        final MutableInt sizeCounter = new MutableInt();
        this.iterable.forEach(e -> sizeCounter.add(1));
        return sizeCounter.getValue();
    }

    public Stream<E> stream() {
        if(this.iterable instanceof Collection<?>) return ((Collection<E>)this.iterable).stream();
        return StreamSupport.stream(spliterator(), false);
    }

    public Stream<E> parallelStream() {
        if(this.iterable instanceof Collection<?>) return ((Collection<E>)this.iterable).parallelStream();
        return StreamSupport.stream(spliterator(),true);
    }

    public E[] toArray() {
        return (E[])Array.newInstance(getElementClass(),size());
    }

    @Override
    public void forEach(Consumer<? super E> action) {
        this.iterable.forEach(action);
    }

    @Override
    public Iterator<E> iterator() {
        return this.iterable.iterator();
    }

    @Override
    public Spliterator<E> spliterator() {
        return this.iterable.spliterator();
    }
}
