package socialIsolation;

import java.util.ArrayList;
import java.util.List;
import repast.simphony.context.Context;
import repast.simphony.engine.environment.RunEnvironment;
import repast.simphony.engine.schedule.ScheduledMethod;
import repast.simphony.parameter.Parameters;
import repast.simphony.random.RandomHelper;
import repast.simphony.space.SpatialMath;
import repast.simphony.space.continuous.ContinuousSpace;
import repast.simphony.space.continuous.NdPoint;
import repast.simphony.space.graph.Network;
import repast.simphony.space.grid.Grid;
import repast.simphony.space.grid.GridPoint;
import repast.simphony.util.ContextUtils;

public class Infected {
	protected ContinuousSpace<Object> space;
	protected Grid<Object> grid;
	
	private int days_infected;

	public boolean symptomatic;
	public boolean hospitalized;
	public boolean asympyomatic;

	boolean dontGoSocial = true;
	
	public Hospital hospital;

	GridPoint workplace_location;
	GridPoint home_location;
	GridPoint social_location;
	public State CurrentState;

	Parameters params = RunEnvironment.getInstance().getParameters();

	private double prob_to_go_to_hospital = (double) params.getValue("prob_to_go_to_hospital");
	private double prob_to_go_to_isolation = (double) params.getValue("prob_to_go_to_isolation");
	private double chance_to_infect = (double) params.getValue("chance_to_infect");
	private double chance_to_recover = (double) params.getValue("chance_to_recover");
	private double chance_to_be_asymptomatic = (double) params.getValue("chance_to_be_asymptomatic");
	private double prob_to_die_of_hospitalized = (double) params.getValue("prob_to_die_of_hospitalized");
	private double prob_to_die_of_infected = (double) params.getValue("prob_to_die_of_infected");
	boolean willGoToHospital = false;
	boolean willGoToIsolation = false;
	
	int maxTimer = 18;
	int maxIsolationTimer = 100;
	int timer = 0;
	
	public Infected(ContinuousSpace<Object> space, Grid<Object> grid, State currentState) {
		this.space = space;
		this.grid = grid;
		
		this.days_infected = 0;
		this.symptomatic = false;
		this.hospitalized = false;
		this.hospital = null;

		
		if (RandomHelper.nextDoubleFromTo(0d, 1d) < this.prob_to_go_to_isolation) {
			willGoToIsolation = true;
		}
		
		if (RandomHelper.nextDoubleFromTo(0d, 1d) < this.prob_to_go_to_hospital) {
			willGoToHospital = true;
		}
		
		if (RandomHelper.nextDoubleFromTo(0d, 1d) < chance_to_be_asymptomatic) {
			asympyomatic = true;
		} else
			asympyomatic = false;
		

		CurrentState = currentState;
	}
	
	@ScheduledMethod(start = 1, interval = 1, priority = 1)
	public void step() {

		if (RandomHelper.nextDoubleFromTo(0d, 1d) < chance_to_recover) {
			BecameRecovered();
			return;
		}
		else if (this.hospitalized) {
			if (RandomHelper.nextDoubleFromTo(0d, 1d) < prob_to_die_of_hospitalized) {
				Die();
			}
			return;
		} else if (RandomHelper.nextDoubleFromTo(0d, 1d) < prob_to_die_of_infected) {
			Die();
			return;
		}
		
		if (this.days_infected > 50 && !this.asympyomatic) {
			this.symptomatic = true;
			
		}
		
		if(this.days_infected > 600) {
			BecameRecovered();
			return;
		}
		
		this.days_infected++;
				
		switch (CurrentState) {
		
			case GOINGHOME:
				if(grid.getDistance(CheckAndReturnHomeLocation(), grid.getLocation(this)) > 1)
				{
					infect();
					moveTowards(home_location);
				} else {
					CurrentState = State.HOME;
				}
				break;
			case HOME:
				timer--;
				if(timer < 1)
				{
					timer = maxTimer;
					CurrentState = State.GOINGWORKPLACE;
				}
				break;
				
			case GOINGWORKPLACE:

				if(grid.getDistance(CheckAndReturnWorkplaceLocation(), grid.getLocation(this)) > 1)
				{
					infect();
					moveTowards(workplace_location);
				} else {
					CurrentState = State.WORK;
				}
				break;
			case WORK:
				infect();
				timer--;
				if(timer < 1)
				{
					timer = maxTimer;
					if(dontGoSocial)
					{

						CurrentState = State.GOINGHOME;
					}
					else
					{
						CurrentState = State.GOINGSOCIAL;
					}
				}
				break;
			case GOINGSOCIAL:
				if(grid.getDistance(CheckAndReturnSocialLocation(), grid.getLocation(this)) > 1)
				{
					infect();
					moveTowards(social_location);
				} else {
					CurrentState = State.SOCIAL;
				}
				break;
			case SOCIAL:

				infect();
				timer--;
				if(timer < 1)
				{
					timer = maxTimer;
					CurrentState = State.GOINGHOME;
				}
				break;
			case HOSPITALIZED:
				
				break;
			case GOINGISOLATION:
				if(grid.getDistance(CheckAndReturnHomeLocation(), grid.getLocation(this)) > 1)
				{
					moveTowards(home_location);
				} else {
					CurrentState = State.ISOLATION;
				}
				break;
			case ISOLATION:
				
				break;
			default:
				CurrentState = State.GOINGHOME;
				break;
			}
	}
	
	private GridPoint CheckAndReturnHomeLocation() {
		if(home_location == null) {
			Home targetHome = Utils.FindTargetHome();
			home_location = grid.getLocation(targetHome);
			return home_location;
		}
		return home_location;
	}
	
	private GridPoint CheckAndReturnWorkplaceLocation() {
		if(workplace_location == null) {
			Workplace targetWorkplace = Utils.FindTargetWorkplace();
			workplace_location = grid.getLocation(targetWorkplace);
			return workplace_location;
		}
		return workplace_location;
	}
	
	private GridPoint CheckAndReturnSocialLocation() {
		if(social_location == null) {
			Social targetSocial = Utils.FindTargetSocial();
			social_location = grid.getLocation(targetSocial);
			return social_location;
		}
		return social_location;
	}
	
	@ScheduledMethod(start = 1200)
	public void StopGoingSocial() { 
		dontGoSocial = true;
	}
	
	/*
	 * @ScheduledMethod(start = 1300, priority = 0) public void FullIsolation() {
	 * CurrentState = State.GOINGISOLATION; }
	 * 
	 * @ScheduledMethod(start = 2000) public void EndFullIsolation() { CurrentState
	 * = State.GOINGHOME; }
	 */
	
	public boolean moveTowards(GridPoint pt) {
		// only move if we are not already in this grid location
		if (!pt.equals(grid.getLocation(this))) {
			NdPoint myPoint = space.getLocation(this);
			NdPoint otherPoint = new NdPoint(pt.getX(), pt.getY());
			double angle = SpatialMath.calcAngleFor2DMovement(space, myPoint, otherPoint);
			space.moveByVector(this, 2, angle, 0);
			myPoint = space.getLocation(this);
			grid.moveTo(this, (int) myPoint.getX(), (int) myPoint.getY());
			return false;
		}
		else {
			return true;
		}
	}

	@SuppressWarnings("unchecked")
	public void infect() {
		GridPoint pt = grid.getLocation(this);
		List<Object> healthy = new ArrayList<Object>();
		// Get all healthy at the new location
		for (Object obj : grid.getObjectsAt(pt.getX(), pt.getY())) {
			if (obj instanceof Healthy) {
				if(obj instanceof Recovered)
				{
					continue;
				}
				healthy.add(obj);
			}
		}

		// infect any random healthy
		if (healthy.size() > 0) {
			for (Object obj : healthy) {
				if (RandomHelper.nextDoubleFromTo(0d, 1d) <= chance_to_infect && !((Healthy) obj).social_isolate) {
					
					NdPoint spacePt = space.getLocation(obj);
					Context<Object> context = ContextUtils.getContext(obj);
					context.remove(obj);
					Infected infected = new Infected(space, grid, State.INIT);
					context.add(infected);
					space.moveTo(infected, spacePt.getX(), spacePt.getY());
					grid.moveTo(infected, pt.getX(), pt.getY());
					Network<Object> net = (Network<Object>) context.getProjection("infection network");
					net.addEdge(this, infected);
				}
			}
		}
	}

	private void BecameRecovered() {
		NdPoint spacePt = space.getLocation(this);
		GridPoint pt = grid.getLocation(this);

		Context<Object> context = ContextUtils.getContext(this);
		Recovered recovered = new Recovered(space, grid, CurrentState);
		context.add(recovered);
		space.moveTo(recovered, spacePt.getX(), spacePt.getY());
		grid.moveTo(recovered, pt.getX(), pt.getY());

		exit_hospital();
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

		exit_hospital();

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