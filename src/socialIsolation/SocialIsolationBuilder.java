package socialIsolation;

import repast.simphony.context.Context;
import repast.simphony.context.space.continuous.ContinuousSpaceFactory;
import repast.simphony.context.space.continuous.ContinuousSpaceFactoryFinder;
import repast.simphony.context.space.graph.NetworkBuilder;
import repast.simphony.context.space.grid.GridFactory;
import repast.simphony.context.space.grid.GridFactoryFinder;
import repast.simphony.dataLoader.ContextBuilder;
import repast.simphony.engine.environment.RunEnvironment;
import repast.simphony.parameter.Parameters;
import repast.simphony.space.continuous.ContinuousSpace;
import repast.simphony.space.continuous.NdPoint;
import repast.simphony.space.continuous.RandomCartesianAdder;
import repast.simphony.space.grid.Grid;
import repast.simphony.space.grid.GridBuilderParameters;
import repast.simphony.space.grid.SimpleGridAdder;
import repast.simphony.space.grid.WrapAroundBorders;

public class SocialIsolationBuilder implements ContextBuilder<Object> {

	@Override
	public Context<Object> build(Context<Object> context) {

		NetworkBuilder<Object> netBuilder = new NetworkBuilder<Object>("infection network", context, true);
		netBuilder.buildNetwork();

		context.setId("SocialIsolation");

		ContinuousSpaceFactory spaceFactory = ContinuousSpaceFactoryFinder.createContinuousSpaceFactory(null);

		ContinuousSpace<Object> space = spaceFactory.createContinuousSpace("space", context,
				new RandomCartesianAdder<Object>(), new repast.simphony.space.continuous.WrapAroundBorders(), 100, 100);

		GridFactory gridFactory = GridFactoryFinder.createGridFactory(null);
		Grid<Object> grid = gridFactory.createGrid("grid", context,
				new GridBuilderParameters<>(new WrapAroundBorders(), new SimpleGridAdder<Object>(), true, 100, 100));

		Parameters params = RunEnvironment.getInstance().getParameters();

		int infectedCount = (Integer) params.getValue("infected_count");

		for (int i = 0; i < infectedCount; i++) {
			context.add(new Infected(space, grid));
		}

		int healthyCount = (Integer) params.getValue("healthy_count");

		for (int i = 0; i < healthyCount; i++) {
			context.add(new Healthy(space, grid));
		}

		int hospitalCount = (Integer) params.getValue("hospital_count");

		for (int i = 0; i < hospitalCount; i++) {
			context.add(new Hospital(space, grid));
		}

		for (Object obj : context) {
			NdPoint pt = space.getLocation(obj);
			grid.moveTo(obj, (int) pt.getX(), (int) pt.getY());
		}

		return context;
	}

}
