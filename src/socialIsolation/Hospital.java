package socialIsolation;

import java.util.ArrayList;
import repast.simphony.context.Context;
import repast.simphony.engine.schedule.ScheduledMethod;
import repast.simphony.engine.environment.RunEnvironment;
import repast.simphony.parameter.Parameters;
import repast.simphony.space.continuous.ContinuousSpace;
import repast.simphony.space.continuous.NdPoint;
import repast.simphony.space.grid.Grid;
import repast.simphony.space.grid.GridPoint;
import repast.simphony.util.ContextUtils;
import repast.simphony.util.collections.IndexedIterable;

public class Hospital {
	Parameters params = RunEnvironment.getInstance().getParameters();

	private int number_of_rooms = 10; // (Integer) params.getValue("number_of_rooms");
	public int current_capacity;

	private ContinuousSpace<Object> space;
	private Grid<Object> grid;

	public Hospital(ContinuousSpace<Object> space, Grid<Object> grid) {
		this.space = space;
		this.grid = grid;
		this.current_capacity = number_of_rooms;
	}

	@SuppressWarnings("unchecked")
	@ScheduledMethod(start = 1, interval = 1)
	public void step() {
		
		Context<Object> context = ContextUtils.getContext(this);
		IndexedIterable<Object> indexedItarable = context.getObjects(Infected.class);
		for (Object obj : indexedItarable) {

			Infected infected = (Infected) obj;

			if(infected.symptomatic){
				if(current_capacity > 0)
				{
					if(infected.willGoToHospital){
						infected.CurrentState = State.HOSPITALIZED;
						current_capacity--;
						NdPoint target_location = space.getLocation(this);
						space.moveTo(infected, (double) target_location.getX(), (double) target_location.getY());
						grid.moveTo(infected, (int) target_location.getX(), (int) target_location.getY());

						infected.hospital = this;
						infected.hospitalized = true;
					}
				} else if(infected.willGoToIsolation) {
					infected.CurrentState = State.GOINGISOLATION;
				}
			}
		}
	}
	
    @ScheduledMethod(start = 2400, interval = 1)
    public void VaccinatePeople() {
    	Context<Object> context = ContextUtils.getContext(this);
		IndexedIterable<Object> indexedItarable = context.getObjects(Healthy.class);
		

		ArrayList<Healthy> toVacc = new ArrayList<>();
		int max = 2;
		
		int current = 0;
		for (Object obj : indexedItarable) {
			
			if( obj instanceof Recovered)
			{
				continue;
			}
			
			if ( ++current > max)
				break;
			toVacc.add((Healthy)obj);
		}
		
		for (Healthy agent : toVacc) {
			agent.social_isolate = true;
			State temp = agent.CurrentState;
			agent.CurrentState = State.ISOLATION;
			NdPoint agentSpacePt = space.getLocation(agent);
			GridPoint agentPt = grid.getLocation(agent);
			
			Recovered recovered = new Recovered(space, grid, temp);

			context.add(recovered);
			space.moveTo(recovered, agentSpacePt.getX(), agentSpacePt.getY());
			grid.moveTo(recovered, agentPt.getX(), agentPt.getY());
			recovered.days_of_immunity *= 2;

			context.remove(agent);	
		}
	}
}
