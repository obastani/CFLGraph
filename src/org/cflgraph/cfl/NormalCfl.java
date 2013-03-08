package org.cflgraph.cfl;

import java.util.Set;

import org.cflgraph.utility.Utility.MultivalueMap;
import org.cflgraph.utility.Utility.PairString;

public class NormalCfl {
	public static class UnaryProduction {
		private String output;
		private String input;

		public UnaryProduction(String output, String input) {
			this.output = output;
			this.input = input;
		}

		public String getOutput() {
			return this.output;
		}

		public String getInput() {
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
		private String output;

		private String firstInput;
		private String secondInput;

		public BinaryProduction(String output, String firstInput, String secondInput) {
			this.output = output;
			this.firstInput = firstInput;
			this.secondInput = secondInput;
		}

		public String getOutput() {
			return this.output;
		}

		public String getFirstInput() {
			return this.firstInput;
		}

		public String getSecondInput() {
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

	private MultivalueMap<String,UnaryProduction> unaryProductionsByInput = new MultivalueMap<String,UnaryProduction>();
	private MultivalueMap<String,BinaryProduction> binaryProductionsByFirstInput = new MultivalueMap<String,BinaryProduction>();
	private MultivalueMap<String,BinaryProduction> binaryProductionsBySecondInput = new MultivalueMap<String,BinaryProduction>();
	private MultivalueMap<PairString,BinaryProduction> binaryProductionsByInputs = new MultivalueMap<PairString,BinaryProduction>(); 

	public void add(UnaryProduction UnaryProduction) {
		this.unaryProductionsByInput.add(UnaryProduction.getInput(), UnaryProduction);
	}

	public Set<UnaryProduction> getUnaryProductionsByInput(String input) {
		return this.unaryProductionsByInput.get(input);
	}

	public void add(BinaryProduction binaryProduction) {
		this.binaryProductionsByFirstInput.add(binaryProduction.getFirstInput(), binaryProduction);
		this.binaryProductionsBySecondInput.add(binaryProduction.getSecondInput(), binaryProduction);
		this.binaryProductionsByInputs.add(new PairString(binaryProduction.getFirstInput(), binaryProduction.getSecondInput()), binaryProduction);
	}

	public Set<BinaryProduction> getBinaryProductionsByFirstInput(String input) {
		return this.binaryProductionsByFirstInput.get(input);
	}

	public Set<BinaryProduction> getBinaryProductionsBySecondInput(String input) {
		return this.binaryProductionsBySecondInput.get(input);
	}
	
	public Set<BinaryProduction> getBinaryProductionsByInputs(String firstInput, String secondInput) {
		return this.binaryProductionsByInputs.get(new PairString(firstInput, secondInput));
	}
	
	public void add(String output, String ... inputs) {
		this.add(true, output, inputs);
	}

	public void add(boolean forward, String output, String ... inputs) {
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
			if(forward) {
				String firstInput = inputs[0];
				String secondInput = inputs[1];

				String outputString = firstInput + "^" + secondInput;
				String tempOutput = new String(outputString);
				this.add(new BinaryProduction(tempOutput , firstInput, secondInput));

				for(int i=2; i<inputs.length-1; i++) {
					firstInput = tempOutput;
					secondInput = inputs[i];
					outputString += "^" + secondInput;
					tempOutput  = new String(outputString);
					this.add(new BinaryProduction(tempOutput , firstInput, secondInput));
				}

				firstInput = tempOutput ;
				secondInput = inputs[inputs.length-1];
				tempOutput  = output;
				this.add(new BinaryProduction(tempOutput , firstInput, secondInput));
			} else {
				String firstInput = inputs[inputs.length-1];
				String secondInput = inputs[inputs.length-2];
	
				String outputString = secondInput + "^" + firstInput;
				String tempOutput = new String(outputString);
				this.add(new BinaryProduction(tempOutput , secondInput, firstInput));
	
				for(int i=inputs.length-3; i>0; i--) {
					firstInput = tempOutput;
					secondInput = inputs[i];
					outputString = secondInput + "^" + outputString;
					tempOutput  = new String(outputString);
					this.add(new BinaryProduction(tempOutput, secondInput, firstInput));
				}
	
				firstInput = tempOutput;
				secondInput = inputs[0];
				tempOutput  = output;
				this.add(new BinaryProduction(tempOutput, secondInput, firstInput));
			}
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
