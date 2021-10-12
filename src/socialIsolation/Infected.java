package socialIsolation;

import java.util.ArrayList;
import java.util.List;

import cern.jet.random.Normal;
import cern.jet.random.Uniform;
import repast.simphony.context.Context;
import repast.simphony.engine.environment.RunEnvironment;
import repast.simphony.engine.schedule.ScheduledMethod;
import repast.simphony.parameter.Parameters;
import repast.simphony.query.space.grid.GridCell;
import repast.simphony.query.space.grid.GridCellNgh;
import repast.simphony.random.RandomHelper;
import repast.simphony.space.SpatialMath;
import repast.simphony.space.continuous.ContinuousSpace;
import repast.simphony.space.continuous.NdPoint;
import repast.simphony.space.graph.Network;
import repast.simphony.space.grid.Grid;
import repast.simphony.space.grid.GridPoint;
import repast.simphony.util.ContextUtils;
import repast.simphony.util.SimUtilities;

public class Infected {
	private ContinuousSpace<Object> space;
	private Grid<Object> grid;
	private int days_infected;

	private boolean symptomatic;
	public boolean hospitalized;
	public boolean asympyomatic;

	public Hospital hospital;

	private static Uniform rand;

	Parameters params = RunEnvironment.getInstance().getParameters();

	private double prob_to_go_to_hospital = (double) params.getValue("prob_to_go_to_hospital");
	private double chance_to_infect = (double) params.getValue("chance_to_infect");
	private double chance_to_recover = (double) params.getValue("chance_to_recover");
	private double chance_to_be_asymptomatic = (double) params.getValue("chance_to_be_asymptomatic");
	private double prob_to_die_of_hospitalized = (double) params.getValue("prob_to_die_of_hospitalized");
	private double prob_to_die_of_infected = (double) params.getValue("prob_to_die_of_infected");

	public Infected(ContinuousSpace<Object> space, Grid<Object> grid) {
		this.space = space;
		this.grid = grid;

		this.days_infected = 0;
		this.symptomatic = false;
		this.hospitalized = false;
		this.hospital = null;

		rand = RandomHelper.createUniform(0, 1);

		if (Math.random() < chance_to_be_asymptomatic) {
			asympyomatic = true;
		} else
			asympyomatic = false;
	}

	@ScheduledMethod(start = 1, interval = 1)
	public void step() {

		if (rand.nextDouble() < chance_to_recover) {
			exit_hospital();
			BecameRecovered();
			return;
		}

		if (this.hospitalized) {
			if (rand.nextDouble() < prob_to_die_of_hospitalized) {
				Die();
				return;
			}
			return;
		} else {
			if (rand.nextDouble() < prob_to_die_of_infected) {
				Die();
				return;
			}
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

		infect();

		// ------------------
		if (this.days_infected > 14) {
			this.symptomatic = true;
		}

		if (!this.asympyomatic && this.symptomatic) {

			if (rand.nextDouble() < this.prob_to_go_to_hospital) {
				go_to_hospital();
			}
		}

		this.days_infected++;

		// ------------
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

	@SuppressWarnings("unchecked")
	public void infect() {
		GridPoint pt = grid.getLocation(this);
		List<Object> healthy = new ArrayList<Object>();
		// Get all healthys at the new location
		for (Object obj : grid.getObjectsAt(pt.getX(), pt.getY())) {
			if (obj instanceof Healthy) {
				healthy.add(obj);
			}
		}

		// infect any random healthy
		if (healthy.size() > 0) {
			for (Object obj : healthy) {
				double random = Math.random();
				if (rand.nextDouble() <= chance_to_infect && !((Healthy) obj).social_isolate) {
					NdPoint spacePt = space.getLocation(obj);
					Context<Object> context = ContextUtils.getContext(obj);
					context.remove(obj);
					Infected infected = new Infected(space, grid);
					context.add(infected);
					space.moveTo(infected, spacePt.getX(), spacePt.getY());
					grid.moveTo(infected, pt.getX(), pt.getY());
					Network<Object> net = (Network<Object>) context.getProjection("infection network");
					net.addEdge(this, infected);
				}
			}
		}
	}

	@SuppressWarnings("unchecked")
	public Hospital getNearestHospital() {
		double minDistSq = Double.POSITIVE_INFINITY;
		Hospital minAgent = null;
		// NdPoint myLocation;
		Context<Object> context = ContextUtils.getContext(this);

		for (Object agent : context) {
			if (agent instanceof Hospital) {
				Hospital thisHospital = (Hospital) agent;
				if (thisHospital.current_capacity > 0) {
					NdPoint currloc = space.getLocation(this);
					NdPoint loc = space.getLocation(agent);

					// sanirim burayi yanlis yazmislar düzelttim
					double distSq = (currloc.getX() - loc.getX()) * (currloc.getX() - loc.getX())
							+ (currloc.getY() - loc.getY()) * (currloc.getY() - loc.getY());

					if (distSq < minDistSq) {
						minDistSq = distSq;
						// burada direkt thishospital kullanilabilir
						minAgent = (Hospital) agent;
					}

				}
			}
		}

		if (minAgent != null)
			minAgent.current_capacity--;

		return minAgent;
	}

	private void go_to_hospital() {

		Hospital nearest_hospital = getNearestHospital();
		if (nearest_hospital == null)
			return;

		NdPoint target_location = space.getLocation(nearest_hospital);
		space.moveTo(this, (double) target_location.getX(), (double) target_location.getY());
		grid.moveTo(this, (int) target_location.getX(), (int) target_location.getY());

		this.hospital = nearest_hospital;
		hospitalized = true;

	}

	@SuppressWarnings("unchecked")
	private void BecameRecovered() {
		NdPoint spacePt = space.getLocation(this);
		GridPoint pt = grid.getLocation(this);

		Context<Object> context = ContextUtils.getContext(this);
		Recovered recovered = new Recovered(space, grid);
		context.add(recovered);
		space.moveTo(recovered, spacePt.getX(), spacePt.getY());
		grid.moveTo(recovered, pt.getX(), pt.getY());

		context.remove(this);
	}

	private void exit_hospital() {
		if (this.hospital != null) {
			hospital.current_capacity++;
			this.hospital = null;
		}
	}

	@SuppressWarnings("unchecked")
	private void Die() {

		if (this.hospital != null) {
			hospital.current_capacity++;
			this.hospital = null;
		}

		GridPoint pt = grid.getLocation(this);

		NdPoint spacePt = space.getLocation(this);

		Context<Object> context = ContextUtils.getContext(this);

		Dead dead = new Dead();
		context.add(dead);

		space.moveTo(dead, spacePt.getX(), spacePt.getY());
		grid.moveTo(dead, pt.getX(), pt.getY());

		context.remove(this);

	}

}