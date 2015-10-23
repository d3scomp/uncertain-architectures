package cz.cuni.mff.d3s.jdeeco.ua.filter;

import cz.cuni.mff.d3s.jdeeco.position.Position;

public class PositionFilter extends GaussianFilter<Position> {

	public PositionFilter(double mean, double deviation) {
		super(mean,deviation);
	}

	@Override
	public Position applyNoise(final Position data) {
		// horizontal noise
		double h = noise.getNext();
		// vertical noise
		double v = noise.getNext();
		
		return new Position(data.x + h, data.y + v);
	}
}
