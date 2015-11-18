package cz.cuni.mff.d3s.deeco.modes;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.Set;

import cz.cuni.mff.d3s.deeco.annotations.DEECoMode;
import cz.cuni.mff.d3s.deeco.annotations.ModeChart;
import cz.cuni.mff.d3s.deeco.knowledge.KnowledgeNotFoundException;
import cz.cuni.mff.d3s.deeco.logging.Log;
import cz.cuni.mff.d3s.deeco.model.runtime.api.KnowledgePath;
import cz.cuni.mff.d3s.deeco.model.runtime.api.PathNodeField;
import cz.cuni.mff.d3s.deeco.model.runtime.custom.RuntimeMetadataFactoryExt;

// TODO: move to cz.cuni.mff.d3s.jdeeco.modes project and make tests
public class ModeChartImpl extends ModeChart{
	
	public static final double MODE_NOT_FOUND = -1;
	
	public static final double TRANSITION_NOT_FOUND = -2;
	
	public static final Random rand = new Random(78631);
	
	Map<Class<? extends DEECoMode>, Set<ModeSuccessor>> modes;
	
	/**
	 * Internal constructor enables {@link ModeChartFactory} to be the
	 * Privileged creator of {link {@link ModeChartImpl} instances.
	 */
	ModeChartImpl() {
		currentMode = null;
		modes = new HashMap<>();
	}
	
	void setInitialNode(Class<? extends DEECoMode> mode){
		currentMode = mode;
	}
	
	Class<? extends DEECoMode> getInitialMode(){
		return currentMode;
	}

	@Override
	public Set<Class<? extends DEECoMode>> getModes() {
		return modes.keySet();
	}
	
	@Override
	@SuppressWarnings("unchecked")
	public Class<? extends DEECoMode> findSetAndReturnCurrentMode(){
		
		// Switch mode only if there is a transition from it
		if(modes.containsKey(currentMode)){
			// Get successor modes
			List<ModeSuccessor> successors = new ArrayList<>(modes.get(currentMode));
			// Filter out inapplicable transitions
			for(ModeSuccessor succ : successors.toArray(new ModeSuccessor[]{})){
				String knowledge = succ.guard.getKnowledgeName();
				Object value = getValue(knowledge);
				//System.out.format("Knowledge: %s Value %s%n", knowledge, String.valueOf(value));
				if(!succ.guard.isSatisfied(value)){
					successors.remove(succ);
				}
			}
			// Sort according to the probabilities
			Collections.sort(successors, new Comparator<ModeSuccessor>(){
				@Override
				public int compare(ModeSuccessor s1, ModeSuccessor s2) {
					return Double.compare(s1.probability, s2.probability);
				}
			});
			// Check probability consistency
			double probabilitySum = 0;
			for(ModeSuccessor s : successors){
				probabilitySum += s.probability;
			}
			if(probabilitySum > 1){
				StringBuilder builder = new StringBuilder();
				for(ModeSuccessor succ : successors){
					builder.append("\n").append(succ.getClass())
						.append(" probability = ").append(succ.probability);
				}
				Log.e("The probabilities of these satisfied mode successors"
						+ " is greater than 1 and will lead to unconsistent behavior:"
						+ builder.toString());
			}
			// switch nondeterministically
			double random = rand.nextDouble();
			double successorTreshold = 0;
			for(ModeSuccessor s : successors){
				successorTreshold += s.probability;
				if(random < successorTreshold){
					currentMode = s.successor;
					break;
				}
			}
		}

		return currentMode;
	}
	
	private Object getValue(String knowledge) {
		KnowledgePath path = RuntimeMetadataFactoryExt.eINSTANCE.createKnowledgePath();
		PathNodeField pNode = RuntimeMetadataFactoryExt.eINSTANCE.createPathNodeField();
		pNode.setName(knowledge);
		path.getNodes().add(pNode);
		ArrayList<KnowledgePath> paths = new ArrayList<>();
		paths.add(path);
		try {
			return component.getKnowledgeManager().get(paths).getValue(path);
		} catch (KnowledgeNotFoundException e) {
			Log.e("Couldn't find knowledge " + knowledge + " in component " + component);
			return null;
		}
	}
	
	public Set<Class<? extends DEECoMode>> getSuccessors(Class<? extends DEECoMode> mode){
		Set<Class<? extends DEECoMode>> successors = new HashSet<>();
		for(ModeSuccessor succ : modes.get(mode)){
			successors.add(succ.successor);
		}
		
		return successors;
	}
	
	public double getProbability(Class<? extends DEECoMode> from, Class<? extends DEECoMode> to){
		if(modes.containsKey(from)){
			for(ModeSuccessor succ : modes.get(from)){
				if(succ.successor.equals(to)){
					return succ.probability;
				}
			}
			return TRANSITION_NOT_FOUND;
		}
		return MODE_NOT_FOUND;
	}
}
