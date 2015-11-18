package cz.cuni.mff.d3s.deeco.modes;

public interface ModeGuard<T> {
	
	String getKnowledgeName();
	
	boolean isSatisfied(T knowledgeValue);

}
