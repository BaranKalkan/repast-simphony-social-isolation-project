package socialIsolation;

import java.util.List;

import repast.simphony.context.Context;
import repast.simphony.engine.environment.RunEnvironment;
//import repast.simphony.engine.environment.RunEnvironment;
import repast.simphony.engine.schedule.ScheduledMethod;
import repast.simphony.parameter.Parameters;
//import repast.simphony.parameter.Parameters;
import repast.simphony.query.space.grid.GridCell;
import repast.simphony.query.space.grid.GridCellNgh;
import repast.simphony.random.RandomHelper;
import repast.simphony.space.*;
import repast.simphony.space.continuous.*;
import repast.simphony.space.grid.*;
import repast.simphony.util.ContextUtils;
import repast.simphony.util.SimUtilities;

public class Recovered {

	private ContinuousSpace<Object> space;
	private Grid<Object> grid;
	private int days_immune;

	Parameters params = RunEnvironment.getInstance().getParameters();

	private int days_of_immunity = (int) params.getValue("days_of_immunity");

	public Recovered(ContinuousSpace<Object> space, Grid<Object> grid) {
		this.space = space;
		this.grid = grid;
		this.days_immune = 0;
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

		days_immune++;

		if (days_immune > days_of_immunity) {
			BecameNormal();
			return;
		}

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

	@SuppressWarnings("unchecked")
	private void BecameNormal() {
		NdPoint spacePt = space.getLocation(this);
		GridPoint pt = grid.getLocation(this);

		Context<Object> context = ContextUtils.getContext(this);
		Healthy healthy = new Healthy(space, grid);
		context.add(healthy);
		space.moveTo(healthy, spacePt.getX(), spacePt.getY());
		grid.moveTo(healthy, pt.getX(), pt.getY());

		context.remove(this);
	}

}
