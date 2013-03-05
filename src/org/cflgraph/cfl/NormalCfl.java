package org.cflgraph.cfl;

import java.util.Set;

import org.cflgraph.utility.Utility.Factory;
import org.cflgraph.utility.Utility.MultivalueMapInt;

public class NormalCfl {
	public Factory<String> elements = new Factory<String>();
	
	public class UnaryProduction {
		private int output;
		private int input;

		public UnaryProduction(int output, int input) {
			this.output = output;
			this.input = input;
		}

		public int getOutput() {
			return this.output;
		}

		public int getInput() {
			return this.input;
		}

		@Override
		public String toString() {
			return elements.getElementById(this.output) + " <- " + elements.getElementById(this.input);
		}

		@Override
		public int hashCode() {
			final int prime = 31;
			int result = 1;
			result = prime * result + getOuterType().hashCode();
			result = prime * result + input;
			result = prime * result + output;
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
			if (!getOuterType().equals(other.getOuterType()))
				return false;
			if (input != other.input)
				return false;
			if (output != other.output)
				return false;
			return true;
		}

		private NormalCfl getOuterType() {
			return NormalCfl.this;
		}
	}

	public class BinaryProduction {
		private int output;

		private int firstInput;
		private int secondInput;

		public BinaryProduction(int output, int firstInput, int secondInput) {
			this.output = output;
			this.firstInput = firstInput;
			this.secondInput = secondInput;
		}

		public int getOutput() {
			return this.output;
		}

		public int getFirstInput() {
			return this.firstInput;
		}

		public int getSecondInput() {
			return this.secondInput;
		}

		@Override
		public String toString() {
			return elements.getElementById(this.output) + " <- " + elements.getElementById(this.firstInput) + " " + elements.getElementById(this.secondInput);
		}

		@Override
		public int hashCode() {
			final int prime = 31;
			int result = 1;
			result = prime * result + getOuterType().hashCode();
			result = prime * result + firstInput;
			result = prime * result + output;
			result = prime * result + secondInput;
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
			if (!getOuterType().equals(other.getOuterType()))
				return false;
			if (firstInput != other.firstInput)
				return false;
			if (output != other.output)
				return false;
			if (secondInput != other.secondInput)
				return false;
			return true;
		}

		private NormalCfl getOuterType() {
			return NormalCfl.this;
		}
	}

	private MultivalueMapInt<UnaryProduction> unaryProductionsByInput = new MultivalueMapInt<UnaryProduction>();
	private MultivalueMapInt<BinaryProduction> binaryProductionsByFirstInput = new MultivalueMapInt<BinaryProduction>();
	private MultivalueMapInt<BinaryProduction> binaryProductionsBySecondInput = new MultivalueMapInt<BinaryProduction>();

	public void add(UnaryProduction UnaryProduction) {
		this.unaryProductionsByInput.add(UnaryProduction.getInput(), UnaryProduction);
	}

	public Set<UnaryProduction> getUnaryProductionsByInput(int input) {
		return this.unaryProductionsByInput.get(input);
	}

	public void add(BinaryProduction binaryProduction) {
		this.binaryProductionsByFirstInput.add(binaryProduction.getFirstInput(), binaryProduction);
		this.binaryProductionsBySecondInput.add(binaryProduction.getSecondInput(), binaryProduction);
	}

	public Set<BinaryProduction> getBinaryProductionsByFirstInput(int input) {
		return this.binaryProductionsByFirstInput.get(input);
	}

	public Set<BinaryProduction> getBinaryProductionsBySecondInput(int input) {
		return this.binaryProductionsBySecondInput.get(input);
	}

	public void add(int output, int ... inputs) {
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
			int firstInput = inputs[0];
			int secondInput = inputs[1];

			String curOutputString = elements.getElementById(firstInput) + "^" + elements.getElementById(secondInput);
			int curOutput = elements.getIdByElement(curOutputString);
			
			this.add(new BinaryProduction(curOutput, firstInput, secondInput));

			for(int i=2; i<inputs.length-1; i++) {
				firstInput = curOutput;
				secondInput = inputs[i];
				curOutputString += "^" + elements.getElementById(secondInput);
				curOutput  = elements.getIdByElement(curOutputString);
				this.add(new BinaryProduction(curOutput, firstInput, secondInput));
			}

			firstInput = curOutput;
			secondInput = inputs[inputs.length-1];
			curOutput = output;
			this.add(new BinaryProduction(curOutput, firstInput, secondInput));
			break;
		}
	}

	@Override
	public String toString() {
		StringBuilder result = new StringBuilder();
		for(Set<UnaryProduction> UnaryProductions : this.unaryProductionsByInput.valueCollection()) {
			for(UnaryProduction UnaryProduction : UnaryProductions) {
				result.append(UnaryProduction.toString()+"\n");
			}
		}
		for(Set<BinaryProduction> BinaryProductions : this.binaryProductionsByFirstInput.valueCollection()) {
			for(BinaryProduction BinaryProduction : BinaryProductions) {
				result.append(BinaryProduction.toString()+"\n");
			}
		}
		return result.toString();
	}
}
