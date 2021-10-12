package socialIsolation;

import java.util.List;

import repast.simphony.engine.environment.RunEnvironment;
import repast.simphony.engine.schedule.ScheduledMethod;
import repast.simphony.parameter.Parameters;
import repast.simphony.query.space.grid.GridCell;
import repast.simphony.query.space.grid.GridCellNgh;
import repast.simphony.random.RandomHelper;
import repast.simphony.space.*;
import repast.simphony.space.continuous.*;
import repast.simphony.space.grid.*;
import repast.simphony.util.SimUtilities;

public class Healthy {

	private ContinuousSpace<Object> space;
	private Grid<Object> grid;
	public boolean social_isolate;

	Parameters params = RunEnvironment.getInstance().getParameters();
	private double prob_to_social_isolate = (double) params.getValue("prob_to_social_isolate");

	public Healthy(ContinuousSpace<Object> space, Grid<Object> grid) {
		this.space = space;
		this.grid = grid;

		if (Math.random() <= prob_to_social_isolate) {
			this.social_isolate = true;
		}
	}

	public void moveTowards(GridPoint pt) {
		// only move if we are not already in this grid location
		if (!pt.equals(grid.getLocation(this))) {
			NdPoint myPoint = space.getLocation(this);
			NdPoint otherPoint = new NdPoint(pt.getX(), pt.getY());
			double angle = SpatialMath.calcAngleFor2DMovement(space, myPoint, otherPoint);
			space.moveByVector(this, 2, angle, 0);
			myPoint = space.getLocation(this);
			grid.moveTo(this, (int) myPoint.getX(), (int) myPoint.getY());
		}
	}

	@ScheduledMethod(start = 1, interval = 1)
	public void step() {
		if (this.social_isolate)
			return;
		// get the grid location of this Human
		GridPoint pt = grid.getLocation(this);

		// use the GridCellNgh class to create GridCells for
		// the surrounding neighborhood .
		GridCellNgh<Object> nghCreator = new GridCellNgh<Object>(grid, pt, Object.class, 1, 1);

		List<GridCell<Object>> gridcells = nghCreator.getNeighborhood(true);
		SimUtilities.shuffle(gridcells, RandomHelper.getUniform());

		GridCell<Object> cell = gridcells.get(0);

		GridPoint point_to_move = cell.getPoint();
		moveTowards(point_to_move);
	}

}
