package org.cflgraph.cfl;

import java.util.Set;

import org.cflgraph.cfl.Element.Variable;
import org.cflgraph.utility.Utility.MultivalueMap;

public class NormalCFL {
    public NormalCFL(BufferedReader input) {
	String line;
	while((line = input) != null) {
	    String[] params = line.split(" ");
	    if(params.length >= 2) {
		//this.add(new Element(params[0]), Variable target, Element ... inputs) {
	    }
	}
    }

	public static class SingleProduction {
		private Variable target;
		private Element input;
		
		public SingleProduction(Variable target, Element input) {
			this.target = target;
			this.input = input;
		}
		
		public Variable getTarget() {
			return this.target;
		}
		
		public Element getInput() {
			return this.input;
		}

		@Override
		public String toString() {
			return this.target.toString() + " -> (" + this.input.toString() + ")";
		}

		@Override
		public int hashCode() {
			final int prime = 31;
			int result = 1;
			result = prime * result + ((input == null) ? 0 : input.hashCode());
			result = prime * result
					+ ((target == null) ? 0 : target.hashCode());
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
			SingleProduction other = (SingleProduction) obj;
			if (input == null) {
				if (other.input != null)
					return false;
			} else if (!input.equals(other.input))
				return false;
			if (target == null) {
				if (other.target != null)
					return false;
			} else if (!target.equals(other.target))
				return false;
			return true;
		}
	}

	public static class PairProduction {
		private Variable target;
		
		private Element firstInput;
		private Element secondInput;
		
		public PairProduction(Variable target, Element firstInput, Element secondInput) {
			this.target = target;
			this.firstInput = firstInput;
			this.secondInput = secondInput;
		}
		
		public Variable getTarget() {
			return this.target;
		}
		
		public Element getFirstInput() {
			return this.firstInput;
		}
		
		public Element getSecondInput() {
			return this.secondInput;
		}

		@Override
		public String toString() {
			return this.target.toString() + " -> (" + this.firstInput.toString() + "," + this.secondInput.toString() + ")";
		}

		@Override
		public int hashCode() {
			final int prime = 31;
			int result = 1;
			result = prime * result
					+ ((firstInput == null) ? 0 : firstInput.hashCode());
			result = prime * result
					+ ((secondInput == null) ? 0 : secondInput.hashCode());
			result = prime * result
					+ ((target == null) ? 0 : target.hashCode());
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
			PairProduction other = (PairProduction) obj;
			if (firstInput == null) {
				if (other.firstInput != null)
					return false;
			} else if (!firstInput.equals(other.firstInput))
				return false;
			if (secondInput == null) {
				if (other.secondInput != null)
					return false;
			} else if (!secondInput.equals(other.secondInput))
				return false;
			if (target == null) {
				if (other.target != null)
					return false;
			} else if (!target.equals(other.target))
				return false;
			return true;
		}
	}
	
	private MultivalueMap<Element,SingleProduction> singleProductionsByInput = new MultivalueMap<Element,SingleProduction>();
	private MultivalueMap<Element,PairProduction> pairProductionsByFirstInput = new MultivalueMap<Element,PairProduction>();
	private MultivalueMap<Element,PairProduction> pairProductionsBySecondInput = new MultivalueMap<Element,PairProduction>();
	
	public void add(SingleProduction singleProduction) {
		this.singleProductionsByInput.add(singleProduction.getInput(), singleProduction);
	}
	
	public Set<SingleProduction> getSingleProductionsByInput(Element input) {
		return this.singleProductionsByInput.get(input);
	}
	
	public void add(PairProduction pairProduction) {
		this.pairProductionsByFirstInput.add(pairProduction.getFirstInput(), pairProduction);
		this.pairProductionsBySecondInput.add(pairProduction.getSecondInput(), pairProduction);
	}
	
	/*
	public Set<SingleProduction> getSingleProductions() {
		Set<SingleProduction> singleProductions = new HashSet<SingleProduction>();
		for(Set<SingleProduction> singleProductionSet : this.singleProductionsByInput.values()) {
			singleProductions.addAll(singleProductionSet);
		}
		return singleProductions;
	}
	
	public Set<PairProduction> getPairProductions() {
		Set<PairProduction> pairProductions = new HashSet<PairProduction>();
		for(Set<PairProduction> pairProductionSet : this.pairProductionsByFirstInput.values()) {
			pairProductions.addAll(pairProductionSet);
		}
		return pairProductions;
	}
	*/
	
	public Set<PairProduction> getPairProductionsByFirstInput(Element input) {
		return this.pairProductionsByFirstInput.get(input);
	}
	
	public Set<PairProduction> getPairProductionsBySecondInput(Element input) {
		return this.pairProductionsBySecondInput.get(input);
	}
	
	// assume the production isn't empty!
	public void add(Variable target, Element ... inputs) {
		switch(inputs.length) {
		case 0:
			return;
	    case 1:
	    	this.add(new SingleProduction(target, inputs[0]));
	    	break;
	    case 2:
	    	this.add(new PairProduction(target, inputs[0], inputs[1]));
	    	break;
	    default:
	      	Element firstInput = inputs[0];
	     	Element secondInput = inputs[1];
	
	     	//String targetString = production.getTarget() + "->" + firstInput.getName() + "^" + secondInput.getName();
	     	String targetString = firstInput.getName() + "^" + secondInput.getName();
	     	Variable tempTarget = new Variable(targetString);
	     	this.add(new PairProduction(tempTarget , firstInput, secondInput));
	
	     	for(int i=2; i<inputs.length-1; i++) {
	     		firstInput = tempTarget ;
	     		secondInput = inputs[i];
	     		targetString += "^" + secondInput.getName();
	     		tempTarget  = new Variable(targetString);
	     		this.add(new PairProduction(tempTarget , firstInput, secondInput));
	     	}
	
	     	firstInput = tempTarget ;
	     	secondInput = inputs[inputs.length-1];
	     	tempTarget  = target;
	     	this.add(new PairProduction(tempTarget , firstInput, secondInput));
	     	break;
		}
	}
	
	@Override
	public String toString() {
		StringBuilder result = new StringBuilder();
		for(Set<SingleProduction> singleProductions : this.singleProductionsByInput.values()) {
			for(SingleProduction singleProduction : singleProductions) {
				result.append(singleProduction.toString()+"\n");
			}
		}
		for(Set<PairProduction> pairProductions : this.pairProductionsByFirstInput.values()) {
			for(PairProduction pairProduction : pairProductions) {
				result.append(pairProduction.toString()+"\n");
			}
		}
		return result.toString();
	}
}
