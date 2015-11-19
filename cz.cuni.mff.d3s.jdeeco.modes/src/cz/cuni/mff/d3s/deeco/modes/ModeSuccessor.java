package cz.cuni.mff.d3s.deeco.modes;

@SuppressWarnings("rawtypes")
class ModeSuccessor {

	final double probability;
	
	final ModeGuard guard;
	
	public final Class<? extends DEECoMode> successor;
	
	public ModeSuccessor(Class<? extends DEECoMode> successor,
			double probability, ModeGuard guard) {
		this.successor = successor;
		this.probability = probability;
		this.guard = guard;
	}
	
	public double getProbability(){
		return probability;
	}
	
}
