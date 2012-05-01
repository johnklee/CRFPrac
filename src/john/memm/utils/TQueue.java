package john.memm.utils;

import java.util.Collection;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.Queue;

public class TQueue implements Queue<Integer>{
	private Queue<Integer> queue;
	private int fixSize = 1;
	
	public TQueue(int size){this.fixSize = size; queue = new LinkedList<Integer>();}
	

	@Override
	public boolean addAll(Collection<? extends Integer> c) {
		return queue.addAll(c);
	}

	@Override
	public void clear() {
		queue.clear();		
	}

	@Override
	public boolean contains(Object o) {
		return queue.contains(o);
	}

	@Override
	public boolean containsAll(Collection<?> c) {
		return queue.containsAll(c);
	}

	@Override
	public boolean isEmpty() {
		return queue.isEmpty();
	}

	@Override
	public Iterator<Integer> iterator() {
		return queue.iterator();
	}

	@Override
	public boolean remove(Object o) {
		return queue.remove(o);
	}

	@Override
	public boolean removeAll(Collection<?> c) {
		return queue.removeAll(c);
	}

	@Override
	public boolean retainAll(Collection<?> c) {
		return queue.retainAll(c);
	}

	@Override
	public int size() {
		return queue.size();
	}

	@Override
	public Object[] toArray() {
		return queue.toArray();
	}

	@Override
	public <T> T[] toArray(T[] a) {
		return queue.toArray(a);
	}

	@Override
	public boolean add(Integer e) {
		if(queue.size()<fixSize) return queue.add(e);
		else 
		{
			queue.poll();
			return queue.add(e);
		}
	}

	@Override
	public Integer element() {
		return queue.element();
	}

	@Override
	public boolean offer(Integer e) {
		return queue.offer(e);
	}

	@Override
	public Integer peek() {
		return queue.peek();
	}

	@Override
	public Integer poll() {
		return queue.poll();
	}

	@Override
	public Integer remove() {
		return queue.remove();
	}
	
	public String entry()
	{
		if(queue.size() == fixSize) 
		{
			Object objs[] = queue.toArray();
			StringBuffer strBuf = new StringBuffer();
			strBuf.append(objs[0]);
			for(int i=1; i<objs.length; i++)
			{
				strBuf.append(String.format("_%s", objs[i]));
			}
			return strBuf.toString();
		}
		return null;
	}
	
	public static void main(String args[])
	{
		TQueue tq = new TQueue(3);
		for(int i=0; i<20; i++)
		{
			tq.add(i);
			System.out.printf("\t[Info] Index(%d)=%s...\n", i, tq.entry());
		}
	}
}
