package cz.cuni.mff.d3s.deeco.modes;

import java.util.HashSet;
import java.util.Set;

@SuppressWarnings("rawtypes")
public class ModeChartFactory {

	private ModeChartImpl modeChart;

	public ModeChartFactory() {
		modeChart = new ModeChartImpl();
	}

	public ModeChartFactory withTransitionWithGuard(
			Class<? extends DEECoMode> from, Class<? extends DEECoMode> to,
			ModeGuard guard) {
		if (from == null) throw new IllegalArgumentException(
				String.format("The \"%s\" argument is null.", "from"));
		if (to == null) throw new IllegalArgumentException(
				String.format("The \"%s\" argument is null.", "to"));
		if (guard == null) throw new IllegalArgumentException(
				String.format("The \"%s\" argument is null.", "guard"));

		if (!modeChart.modes.containsKey(from)) {
			modeChart.modes.put(from, new HashSet<>());
		} else {
			if (modeChart.modes.get(from).contains(to)) {
				throw new IllegalStateException(
						String.format("Transition \"%s\" -> \"%s\" already defined.",
								from, to));
			}
		}
		modeChart.modes.get(from).add(new ModeSuccessor(to, 1, guard));

		return this;
	}

	public ModeChartFactory withTransitionWithProbability(
			Class<? extends DEECoMode> from, Class<? extends DEECoMode> to,
			double probability) {
		if (from == null) throw new IllegalArgumentException(
				String.format("The \"%s\" argument is null.", "from"));
		if (to == null) throw new IllegalArgumentException(
				String.format("The \"%s\" argument is null.", "to"));
		if(probability < 0 || probability > 1) throw new IllegalArgumentException(
				String.format("The \"%s\" argument has to be within the [0, 1] interval.",
						"probability"));
			
		if (!modeChart.modes.containsKey(from)) {
			modeChart.modes.put(from, new HashSet<>());
		} else {
			if (modeChart.modes.get(from).contains(to)) {
				throw new IllegalStateException(
						String.format("Transition \"%s\" -> \"%s\" already defined.",
								from, to));
			}
		}
		modeChart.modes.get(from).add(new ModeSuccessor(to, probability, new TrueGuard()));

		return this;
	}

	public ModeChartFactory withTransition(
			Class<? extends DEECoMode> from, Class<? extends DEECoMode> to,
			ModeGuard guard, double probability) {
		if (from == null) throw new IllegalArgumentException(
				String.format("The \"%s\" argument is null.", "from"));
		if (to == null) throw new IllegalArgumentException(
				String.format("The \"%s\" argument is null.", "to"));
		if (guard == null) throw new IllegalArgumentException(
				String.format("The \"%s\" argument is null.", "guard"));
		if(probability < 0 || probability > 1) throw new IllegalArgumentException(
				String.format("The \"%s\" argument has to be within the [0, 1] interval.",
						"probability"));
			
		if (!modeChart.modes.containsKey(from)) {
			modeChart.modes.put(from, new HashSet<>());
		} else {
			if (modeChart.modes.get(from).contains(to)) {
				throw new IllegalStateException(
						String.format("Transition \"%s\" -> \"%s\" already defined.",
								from, to));
			}
		}
		modeChart.modes.get(from).add(new ModeSuccessor(to, probability, guard));

		return this;
	}

	public ModeChartFactory withInitialMode(Class<? extends DEECoMode> mode){
		modeChart.setInitialNode(mode);
		return this;
	}

	public ModeChartImpl create(){
		// chceck probabilities sum, check initial mode, check graph consistency
		if(modeChart.getInitialMode() == null) throw new IllegalStateException(
				"The initial state has not been set.");
		checkChartConnected();
		
		return modeChart;
	}
	
	private void checkChartConnected(){
		Set<Class<? extends DEECoMode>> reachableModes = new HashSet<>();
		Set<Class<? extends DEECoMode>> oldReachableModes = new HashSet<>();
		
		reachableModes.add(modeChart.getInitialMode());
		while(!oldReachableModes.containsAll(reachableModes)){
			oldReachableModes.addAll(reachableModes);
			for(Object current : reachableModes.toArray()){
				@SuppressWarnings("unchecked")
				Class<? extends DEECoMode> currentMode = (Class<? extends DEECoMode>) current;
				if(modeChart.modes.containsKey(currentMode)){
					for(ModeSuccessor succMode : modeChart.modes.get(currentMode)){
						reachableModes.add(succMode.successor);
					}
				}
			}
		}
		
		if(!reachableModes.containsAll(modeChart.modes.keySet())){
			Set<Class<? extends DEECoMode>> unreachableModes = new HashSet<>();
			unreachableModes.addAll(modeChart.modes.keySet());
			unreachableModes.removeAll(reachableModes);
			
			StringBuilder builder = new StringBuilder();
			for(Class<? extends DEECoMode> unconnected : unreachableModes){
				builder.append("\n").append(unconnected);
			}
			throw new IllegalStateException(String.format(
					"The following states are unreachable from the initial state: %s",
					builder.toString()));
		}
	}
	
	
}
