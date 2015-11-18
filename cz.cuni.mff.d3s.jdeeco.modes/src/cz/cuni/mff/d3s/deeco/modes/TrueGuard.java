package cz.cuni.mff.d3s.deeco.modes;

public class TrueGuard implements ModeGuard<String> {

	@Override
	public String getKnowledgeName() {
		return "id";
	}

	@Override
	public boolean isSatisfied(String knowledgeValue) {
		return true;
	}

}
