package socialIsolation;


import repast.simphony.context.Context;
import repast.simphony.engine.environment.RunEnvironment;
import repast.simphony.engine.schedule.ScheduledMethod;
import repast.simphony.parameter.Parameters;
import repast.simphony.space.SpatialMath;
import repast.simphony.space.continuous.*;
import repast.simphony.space.grid.*;
import repast.simphony.util.ContextUtils;

public class Recovered {

	private ContinuousSpace<Object> space;
	private Grid<Object> grid;

	boolean dontGoSocial = true;
	int maxIsolationTimer = 100;
	int maxTimer = 12;
	int timer = 0;
	
	State CurrentState;
	GridPoint workplace_location;
	GridPoint home_location;
	GridPoint social_location;
	
	private int days_immune;
	Parameters params = RunEnvironment.getInstance().getParameters();
	public int days_of_immunity = (int) params.getValue("days_of_immunity");

	public Recovered(ContinuousSpace<Object> space, Grid<Object> grid) {
		this.days_immune = 0;
		this.space = space;
		this.grid = grid;
		CurrentState = State.INIT;
	}
	
	public Recovered(ContinuousSpace<Object> space, Grid<Object> grid, State currentState) {
		this.days_immune = 0;
		this.space = space;
		this.grid = grid;
		CurrentState = currentState;
	}
	
	
	
	@ScheduledMethod(start = 1, interval = 1, priority = 1 )
	public void step() {
		switch (CurrentState) {
		case INIT:
			
			Workplace targetWorkplace = Utils.FindTargetWorkplace();
			Home targetHome = Utils.FindTargetHome();
			Social targetSocial = Utils.FindTargetSocial();
				
			workplace_location = grid.getLocation(targetWorkplace);
			home_location = grid.getLocation(targetHome);
			social_location = grid.getLocation(targetSocial);
			
			CurrentState = State.GOINGHOME;
			break;
			
	
		case GOINGHOME:
			if(grid.getDistance(CheckAndReturnHomeLocation(), grid.getLocation(this)) > 1)
			{
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
				moveTowards(workplace_location);
			} else {
				CurrentState = State.WORK;
			}
			break;
		case WORK:
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
				moveTowards(social_location);
			} else {
				CurrentState = State.SOCIAL;
			}
			break;
		case SOCIAL:
			timer--;
			if(timer < 1)
			{
				timer = maxTimer;
				CurrentState = State.GOINGHOME;
			}
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
			break;
		}
	
		
		days_immune++;

		if (days_immune > days_of_immunity) {
			BecameNormal();
			return;
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
		if (!pt.equals(grid.getLocation(this))) {
			NdPoint myPoint = space.getLocation(this);
			NdPoint otherPoint = new NdPoint(pt.getX(), pt.getY());
			
			double angle = SpatialMath.calcAngleFor2DMovement(space, myPoint, otherPoint);
			
			space.moveByVector(this, 1, angle, 0);
			
			myPoint = space.getLocation(this);
			grid.moveTo(this, (int) myPoint.getX(), (int) myPoint.getY());
			
			return true;
		}
		else {
			return false;
		}
	}
	
	@SuppressWarnings("unchecked")
	private void BecameNormal() {
		NdPoint spacePt = space.getLocation(this);
		GridPoint pt = grid.getLocation(this);

		Context<Object> context = ContextUtils.getContext(this);
		Healthy healthy = new Healthy(space, grid, CurrentState);
		context.add(healthy);
		space.moveTo(healthy, spacePt.getX(), spacePt.getY());
		grid.moveTo(healthy, pt.getX(), pt.getY());

		context.remove(this);
	}
}
