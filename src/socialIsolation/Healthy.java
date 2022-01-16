package socialIsolation;

import java.util.LinkedList;
import java.util.List;
import repast.simphony.context.Context;
import repast.simphony.engine.environment.RunEnvironment;
import repast.simphony.engine.schedule.ScheduledMethod;
import repast.simphony.parameter.Parameters;
import repast.simphony.random.RandomHelper;
import repast.simphony.space.*;
import repast.simphony.space.continuous.*;
import repast.simphony.space.grid.*;
import repast.simphony.util.ContextUtils;
import repast.simphony.util.collections.IndexedIterable;

public class Healthy {
	protected ContinuousSpace<Object> space;
	protected Grid<Object> grid;

	boolean dontGoSocial = true;
	int maxTimer = 12;
	int timer = 0;
	
	public boolean social_isolate;
	Parameters params = RunEnvironment.getInstance().getParameters();
	private double prob_to_social_isolate = (double) params.getValue("prob_to_social_isolate");
	
	GridPoint workplace_location;
	GridPoint home_location;
	GridPoint social_location;
	
	State CurrentState;
	
	public Healthy(ContinuousSpace<Object> space, Grid<Object> grid) {
		this.space = space;
		this.grid = grid;

		if (Math.random() <= prob_to_social_isolate) {
			this.social_isolate = true;
		}
		
		CurrentState = State.INIT;
	}
	
	
	public Healthy(ContinuousSpace<Object> space, Grid<Object> grid, State currentState) {
		this.space = space;
		this.grid = grid;

		if (Math.random() <= prob_to_social_isolate) {
			this.social_isolate = true;
		}
		
		CurrentState = currentState;
	}
	
	
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
	
	@ScheduledMethod(start = 1, interval = 1, priority = 1)
	public void step() {
		if (this.social_isolate)
			return;
			
		
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
	

}
