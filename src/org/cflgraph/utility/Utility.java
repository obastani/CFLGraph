package org.cflgraph.utility;

import gnu.trove.map.hash.TIntObjectHashMap;
import gnu.trove.map.hash.TObjectIntHashMap;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

public class Utility {
	public static class Factory<V> {
		private TObjectIntHashMap<V> idByElement = new TObjectIntHashMap<V>();
		private TIntObjectHashMap<V> elementById = new TIntObjectHashMap<V>();
		
		int curId = 0;
		
		public V getElementById(int id) {
			return this.elementById.get(id);
		}
		
		public int getIdByElement(V v) {
			if(!this.idByElement.containsKey(v)) {
				this.idByElement.put(v, curId);
				this.elementById.put(curId, v);
				curId++;
			}
			return this.idByElement.get(v);
		}
	}
	
	public static class PairInt {
		private int x;
		private int y;
		
		public PairInt(int x, int y) {
			this.x = x;
			this.y = y;
		}
		
		public int getX() {
			return this.x;
		}
		
		public int getY() {
			return this.y;
		}
		
		@Override
		public String toString() {
			return "(" + this.x + "," + this.y + ")";
		}

		@Override
		public int hashCode() {
			final int prime = 31;
			int result = 1;
			result = prime * result + x;
			result = prime * result + y;
			return result;
		}

		@Override
		public boolean equals(Object obj) {
			if (this == obj)
				return true;
			if (obj == null)
				return false;
			if (getClass() != obj.getClass())
				return false;
			PairInt other = (PairInt) obj;
			if (x != other.x)
				return false;
			if (y != other.y)
				return false;
			return true;
		}
	}
	
	public static class Pair<X> {
		private X x;
		private int y;
		
		public Pair(X x, int y) {
			this.x = x;
			this.y = y;
		}
		
		public X getX() {
			return this.x;
		}
		
		public int getY() {
			return this.y;
		}
		
		@Override
		public String toString() {
			return "(" + this.x.toString() + "," + this.y + ")";
		}

		@Override
		public int hashCode() {
			final int prime = 31;
			int result = 1;
			result = prime * result + ((x == null) ? 0 : x.hashCode());
			result = prime * result + y;
			return result;
		}

		@Override
		public boolean equals(Object obj) {
			if (this == obj)
				return true;
			if (obj == null)
				return false;
			if (getClass() != obj.getClass())
				return false;
			@SuppressWarnings("rawtypes")
			Pair other = (Pair) obj;
			if (x == null) {
				if (other.x != null)
					return false;
			} else if (!x.equals(other.x))
				return false;
			if (y != other.y)
				return false;
			return true;
		}
	}
	
	public static class MultivalueMap<K,V> extends HashMap<K,Set<V>> {
		private static final long serialVersionUID = 1L;
	
		public void add(K k, V v) {
			Set<V> vSet = super.get(k);
			if(vSet == null) {
				super.put(k, vSet = new HashSet<V>());
			}
			vSet.add(v);
		}
		
		@Override
		public Set<V> get(Object k) {
			Set<V> vSet = super.get(k);
			return vSet == null ? new HashSet<V>() : vSet;
		}
	}
	
	public static class MultivalueMapInt<V> extends TIntObjectHashMap<Set<V>> {
		public void add(int k, V v) {
			Set<V> vSet = super.get(k);
			if(vSet == null) {
				super.put(k, vSet = new HashSet<V>());
			}
			vSet.add(v);
		}
		
		@Override
		public Set<V> get(int k) {
			Set<V> vSet = super.get(k);
			return vSet == null ? new HashSet<V>() : vSet;
		}
	}
	
	public static class Heap<T> {
		private List<Pair<T>> heap = new ArrayList<Pair<T>>();
		private Map<T,Integer> positions = new HashMap<T,Integer>();
		private Integer maxPriority;
		
		public Heap() {
			this.maxPriority = null;
		}
		
		public Heap(int maxPriority) {
			this.maxPriority = maxPriority;
		}
		
		public void push(T t, int priority) {
			if(this.maxPriority == null || priority <= this.maxPriority) {
				this.positions.put(t, this.heap.size());
				this.heap.add(new Pair<T>(t,priority));
				this.pushUp(heap.size()-1);
			}
		}
		
		public T pop() {
			if(this.heap.size() > 0) {
				this.swap(0, this.heap.size()-1);
				T t = this.heap.remove(this.heap.size()-1).getX();
				this.positions.remove(t);
				return t;
			} else {
				return null;
			}
		}
		
		public T get(int i) {
			return this.heap.get(i).getX();
		}
		
		public int size() {
			return this.heap.size();
		}
		
		public boolean isEmpty() {
			return this.heap.isEmpty();
		}
		
		public void update(T t, int priority) {
			Integer i = this.positions.get(t);
			if(i == null) {
				this.push(t, priority);
			} else {				
				// save old priority
				int prevPriority = this.heap.get(i).getY();
				
				// update priority
				this.heap.set(i, new Pair<T>(t, priority));
				
				// push up or down
				if(prevPriority > priority) {
					this.pushUp(i);
				} else {
					this.pushDown(i);
				}
			}
		}
		
		private int parent(int i) {
			return (i-1)/2;
		}
		
		private int left(int i) {
			return 2*i+1;
		}
		
		private int right(int i) {
			return 2*i+2;
		}
		
		private void swap(int i, int j) {
			Pair<T> pair = this.heap.get(i);
			this.heap.set(i, this.heap.get(j));
			this.heap.set(j, pair);
			this.positions.put(this.heap.get(i).getX(), i);
			this.positions.put(this.heap.get(j).getX(), j);
		}
		
		private void pushDown(int i) {
	        int left = this.left(i);
	        int right = this.right(i);
	        int largest = i;
	        
	        if(left < this.heap.size() && this.heap.get(largest).getY() > this.heap.get(left).getY()) {
	        	largest = left;
	        }
	        if(right < this.heap.size() && this.heap.get(largest).getY() > this.heap.get(right).getY()) {
	        	largest = right;
	        }
	        
	        if(largest != i) {
	        	this.swap(largest, i);
	        	this.pushDown(largest);
	        }
		}
		
	    private void pushUp(int i) {
	        while(i > 0 && this.heap.get(this.parent(i)).getY() > this.heap.get(i).getY()) {
	            this.swap(this.parent(i), i);
	            i = this.parent(i);
	        }
	    }
	    
	    @Override
	    public String toString() {
	        StringBuilder s = new StringBuilder();
	        for(Pair<T> pair : this.heap){
	            s.append(pair.getX().toString() + ": " + pair.getY() + "\n");
	        }
	        return s.toString();
	    }
	}
	
	public static class Counter<V> extends HashMap<V,Integer> {
		private static final long serialVersionUID = -6883888043330022742L;

		public int getCount(V v) {
			Integer count = super.get(v);
			return count == null ? 0 : count;
		}
		
		public void incrementCount(V v) {
			Integer curCount = super.get(v);
			if(curCount == null) {
				super.put(v, 1);
			} else {
				super.put(v, curCount+1);
			}
		}
		
		public List<Map.Entry<V,Integer>> sortedKeySet() {
			List<Map.Entry<V,Integer>> entries = new ArrayList<Map.Entry<V,Integer>>(super.entrySet());
			Collections.sort(entries, new Comparator<Map.Entry<V,Integer>>() {
				@Override
				public int compare(Entry<V, Integer> arg0, Entry<V, Integer> arg1) {
					if(arg0.getValue() > arg1.getValue()) {
						return -1;
					} else if(arg0.getValue() == arg1.getValue()) {
						return 0;
					} else {
						return 1;
					}
				}
			});
			return entries;
		}
	}
}
