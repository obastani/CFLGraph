package org.cflgraph.cfl;

import java.util.Set;

import org.cflgraph.utility.Utility.MultivalueMap;

public class NormalCfl {
	/*
	public NormalCFL(BufferedReader input) throws IOException {
		String line;
		while((line = input.readLine()) != null) {
			String[] params = line.split(" ");
			if(params.length >= 2) {
				List<Element> inputs = new ArrayList<Element>();
				for(int i=1; i<params.length; i++) {
					inputs.add(new Element(params[i]));
				}
				this.add(new Element(params[0]), inputs);
			}
		}
	}
	*/
	
	public static class Element {
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

	public static class UnaryProduction {
		private Element output;
		private Element input;

		public UnaryProduction(Element output, Element input) {
			this.output = output;
			this.input = input;
		}

		public Element getOutput() {
			return this.output;
		}

		public Element getInput() {
			return this.input;
		}

		@Override
		public String toString() {
			return this.output.toString() + " -> (" + this.input.toString() + ")";
		}

		@Override
		public int hashCode() {
			final int prime = 31;
			int result = 1;
			result = prime * result + ((input == null) ? 0 : input.hashCode());
			result = prime * result
					+ ((output == null) ? 0 : output.hashCode());
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
			UnaryProduction other = (UnaryProduction) obj;
			if (input == null) {
				if (other.input != null)
					return false;
			} else if (!input.equals(other.input))
				return false;
			if (output == null) {
				if (other.output != null)
					return false;
			} else if (!output.equals(other.output))
				return false;
			return true;
		}
	}

	public static class BinaryProduction {
		private Element output;

		private Element firstInput;
		private Element secondInput;

		public BinaryProduction(Element output, Element firstInput, Element secondInput) {
			this.output = output;
			this.firstInput = firstInput;
			this.secondInput = secondInput;
		}

		public Element getOutput() {
			return this.output;
		}

		public Element getFirstInput() {
			return this.firstInput;
		}

		public Element getSecondInput() {
			return this.secondInput;
		}

		@Override
		public String toString() {
			return this.output.toString() + " " + this.firstInput.toString() + " " + this.secondInput.toString();
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
					+ ((output == null) ? 0 : output.hashCode());
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
			BinaryProduction other = (BinaryProduction) obj;
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
			if (output == null) {
				if (other.output != null)
					return false;
			} else if (!output.equals(other.output))
				return false;
			return true;
		}
	}

	private MultivalueMap<Element,UnaryProduction> unaryProductionsByInput = new MultivalueMap<Element,UnaryProduction>();
	private MultivalueMap<Element,BinaryProduction> binaryProductionsByFirstInput = new MultivalueMap<Element,BinaryProduction>();
	private MultivalueMap<Element,BinaryProduction> binaryProductionsBySecondInput = new MultivalueMap<Element,BinaryProduction>();

	public void add(UnaryProduction UnaryProduction) {
		this.unaryProductionsByInput.add(UnaryProduction.getInput(), UnaryProduction);
	}

	public Set<UnaryProduction> getUnaryProductionsByInput(Element input) {
		return this.unaryProductionsByInput.get(input);
	}

	public void add(BinaryProduction binaryProduction) {
		this.binaryProductionsByFirstInput.add(binaryProduction.getFirstInput(), binaryProduction);
		this.binaryProductionsBySecondInput.add(binaryProduction.getSecondInput(), binaryProduction);
	}

	public Set<BinaryProduction> getBinaryProductionsByFirstInput(Element input) {
		return this.binaryProductionsByFirstInput.get(input);
	}

	public Set<BinaryProduction> getBinaryProductionsBySecondInput(Element input) {
		return this.binaryProductionsBySecondInput.get(input);
	}

	public void add(Element output, Element ... inputs) {
		assert inputs.length > 0;
		switch(inputs.length) {
		case 0:
			return;
		case 1:
			this.add(new UnaryProduction(output, inputs[0]));
			break;
		case 2:
			this.add(new BinaryProduction(output, inputs[0], inputs[1]));
			break;
		default:
			Element firstInput = inputs[0];
			Element secondInput = inputs[1];

			String outputString = firstInput.getName() + "^" + secondInput.getName();
			Element tempOutput = new Element(outputString);
			this.add(new BinaryProduction(tempOutput , firstInput, secondInput));

			for(int i=2; i<inputs.length-1; i++) {
				firstInput = tempOutput;
				secondInput = inputs[i];
				outputString += "^" + secondInput.getName();
				tempOutput  = new Element(outputString);
				this.add(new BinaryProduction(tempOutput , firstInput, secondInput));
			}

			firstInput = tempOutput ;
			secondInput = inputs[inputs.length-1];
			tempOutput  = output;
			this.add(new BinaryProduction(tempOutput , firstInput, secondInput));
			break;
		}
	}

	@Override
	public String toString() {
		StringBuilder result = new StringBuilder();
		for(Set<UnaryProduction> UnaryProductions : this.unaryProductionsByInput.values()) {
			for(UnaryProduction UnaryProduction : UnaryProductions) {
				result.append(UnaryProduction.toString()+"\n");
			}
		}
		for(Set<BinaryProduction> BinaryProductions : this.binaryProductionsByFirstInput.values()) {
			for(BinaryProduction BinaryProduction : BinaryProductions) {
				result.append(BinaryProduction.toString()+"\n");
			}
		}
		return result.toString();
	}
}
