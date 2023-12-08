package mods.thecomputerizer.scriptify.util;

import mcp.MethodsReturnNonnullByDefault;
import mods.thecomputerizer.scriptify.Scriptify;

import javax.annotation.Nullable;
import java.lang.reflect.Array;
import java.util.*;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.function.Supplier;

/**
 * Used for bundling generic helper methods with a final collection
 */
@MethodsReturnNonnullByDefault
@SuppressWarnings({"unchecked", "unused"})
public class CollectionBundle<E> implements Iterable<E> {

    public static <E> CollectionBundle<E> make(Supplier<Collection<E>> supplier) {
        return new CollectionBundle<>(supplier.get());
    }

    private final Collection<E> collection;

    public CollectionBundle(Collection<E> collection) {
        this.collection = collection;
    }

    public void add(E element) {
        this.collection.add(element);
    }

    /**
     * This may have weird results if the collection instance does not preserce order
     */
    public void add(int index, E element) {
        if(this.collection instanceof List<?>) ((List<E>)this.collection).add(index,element);
        else {
            int count = size();
            if(count==0 || index==count) this.add(element);
            else {
                E[] elementCopy = (E[])Array.newInstance(element.getClass(),count+1);
                if(index<0 || index>count) index = count;
                count = 0;
                for(E e : this.collection) {
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
        this.collection.clear();
    }

    public Collection<E> get() {
        return this.collection;
    }

    public @Nullable E get(int index) {
        if(this.collection instanceof List<?>) return ((List<E>)this.collection).get(index);
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

    public List<E> getAsList(Function<CollectionBundle<E>,? extends List<E>> onCastException) {
        return (List<E>)getAsType(List.class,onCastException);
    }

    public Set<E> getAsSet() throws ClassCastException  {
        return (Set<E>)getAsType(Set.class,null);
    }

    public Set<E> getAsSet(Function<CollectionBundle<E>,? extends Set<E>> onCastException) {
        return (Set<E>)getAsType(Set.class,onCastException);
    }

    public <T extends Collection<E>> T getAsType(Class<T> clazz) throws ClassCastException {
        return getAsType(clazz,null);
    }

    /**
     * Syntactic sugar...
     */
    public <T extends Collection<E>> T getAsType(Class<T> clazz, @Nullable Function<CollectionBundle<E>,? extends Collection<E>> onCastException) {
        try {
            return clazz.cast(this.collection);
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

    public boolean isList() {
        return this.collection instanceof List<?>;
    }

    public boolean isSet() {
        return this.collection instanceof Set<?>;
    }

    public boolean isUnique() {
        return !isList() && !isSet();
    }

    public void remove(E element) {
        this.collection.remove(element);
    }

    /**
     * This may have weird results if the collection instance does not preserce order or if the elements are integers
     */
    public void removeIndex(int index) {
        if(this.collection instanceof List<?>) ((List<E>)this.collection).remove(index);
        else {
            int count = size()-1;
            if(count<=0) clear();
            E removal = null;
            if(index<0 || index>count) index = count;
            count = 0;
            for(E e : this.collection) {
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

    public void set(Collection<E> elements) {
        this.set(true,elements);
    }

    public void set(boolean clearExisting, Collection<E> elements) {
        if(clearExisting) this.collection.clear();
        this.collection.addAll(elements);
    }

    public int size() {
        return this.collection.size();
    }

    public E[] toArray() {
        return (E[])Array.newInstance(getElementClass(),size());
    }

    @Override
    public void forEach(Consumer<? super E> action) {
        this.collection.forEach(action);
    }

    @Override
    public Iterator<E> iterator() {
        return this.collection.iterator();
    }

    @Override
    public Spliterator<E> spliterator() {
        return this.collection.spliterator();
    }
}
