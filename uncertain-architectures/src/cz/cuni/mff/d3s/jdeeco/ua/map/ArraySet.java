package cz.cuni.mff.d3s.jdeeco.ua.map;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;
import java.util.ListIterator;
import java.util.Spliterator;

public class ArraySet<T>
	implements	java.lang.Iterable<T>,
			  	java.util.Collection<T>,
			  	java.util.List<T>,
			  	java.util.RandomAccess,
			  	java.util.Set<T>,
			  	java.io.Serializable,
			  	java.lang.Cloneable {

	/**
	 * Generated UID.
	 */
	private static final long serialVersionUID = 6345711068405424844L;
	
	private ArrayList<T> data;
	
	public ArraySet(){
		data = new ArrayList<T>();
	}
	
	public ArraySet(int capacity){
		data = new ArrayList<>(capacity);
	}
	
	@Override
	public boolean addAll(int index, Collection<? extends T> c) {
		return data.addAll(index, c);
	}

	@Override
	public T get(int index) {
		return data.get(index);
	}

	@Override
	public T set(int index, T element) {
		return data.set(index, element);
	}

	@Override
	public void add(int index, T element) {
		data.add(index, element);
	}

	@Override
	public T remove(int index) {
		return data.remove(index);
	}

	@Override
	public int indexOf(Object o) {
		return data.indexOf(o);
	}

	@Override
	public int lastIndexOf(Object o) {
		return data.lastIndexOf(o);
	}

	@Override
	public ListIterator<T> listIterator() {
		return data.listIterator();
	}

	@Override
	public ListIterator<T> listIterator(int index) {
		return data.listIterator(index);
	}

	@Override
	public List<T> subList(int fromIndex, int toIndex) {
		return data.subList(fromIndex, toIndex);
	}

	@Override
	public int size() {
		return data.size();
	}

	@Override
	public boolean isEmpty() {
		return data.isEmpty();
	}

	@Override
	public boolean contains(Object o) {
		return data.contains(o);
	}

	@Override
	public Object[] toArray() {
		return data.toArray();
	}

	@Override
	public <S> S[] toArray(S[] a) {
		return data.toArray(a);
	}

	@Override
	public boolean add(T e) {
		return data.add(e);
	}

	@Override
	public boolean remove(Object o) {
		return data.remove(o);
	}

	@Override
	public boolean containsAll(Collection<?> c) {
		return data.containsAll(c);
	}

	@Override
	public boolean addAll(Collection<? extends T> c) {
		return data.addAll(c);
	}

	@Override
	public boolean removeAll(Collection<?> c) {
		return data.removeAll(c);
	}

	@Override
	public boolean retainAll(Collection<?> c) {
		return data.retainAll(c);
	}

	@Override
	public void clear() {
		data.clear();
	}

	@Override
	public Iterator<T> iterator() {
		return data.iterator();
	}

	@Override
	public Spliterator<T> spliterator() {
		return List.super.spliterator();
	}

}
