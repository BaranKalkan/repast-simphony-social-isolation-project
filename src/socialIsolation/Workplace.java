package socialIsolation;

import repast.simphony.space.continuous.ContinuousSpace;
import repast.simphony.space.grid.Grid;

public class Workplace {

	@SuppressWarnings("unused")
	private ContinuousSpace<Object> space;

	@SuppressWarnings("unused")
	private Grid<Object> grid;

	public Workplace(ContinuousSpace<Object> space, Grid<Object> grid) {
		this.space = space;
		this.grid = grid;
	}
}
