package org.cflgraph.cfl;

public abstract class Element {
	public static class Variable extends Element {
		public Variable(String name) {
			super(name);
		}
	}
	
	public static class Terminal extends Element {
		public Terminal(String name) {
			super(name);
		}
	}
	
	private String name;
	
	public Element(String name) {
		this.name = name;
	}
	
	public String getName() {
		return this.name;
	}
	
	@Override
	public String toString() {
		return this.name;
	}
	
	public int hashCode() {
		return this.name.hashCode();
	}
	
	public boolean equals(Object object) {
		if(this == object) {
			return true;
		} else if(object == null || this.getClass() != object.getClass()) {
			return false;
		} else {
			Element element = (Element)object;
			return this.name.equals(element.getName());
		}
	}
}
