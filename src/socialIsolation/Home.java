package socialIsolation;

//import repast.simphony.engine.environment.RunEnvironment;
//import repast.simphony.parameter.Parameters;
import repast.simphony.space.continuous.ContinuousSpace;
import repast.simphony.space.grid.Grid;

public class Home {
	// Parameters params = RunEnvironment.getInstance().getParameters();

	@SuppressWarnings("unused")
	private ContinuousSpace<Object> space;

	@SuppressWarnings("unused")
	private Grid<Object> grid;

	public Home(ContinuousSpace<Object> space, Grid<Object> grid) {
		this.space = space;
		this.grid = grid;
	}
}
