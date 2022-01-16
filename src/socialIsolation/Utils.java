package socialIsolation;

import java.util.LinkedList;
import java.util.List;

import repast.simphony.random.RandomHelper;

public class Utils {
	
	static List<Social> socialPlaces;
	static List<Workplace> workplaces;
	static List<Home> homes;
	
	public static void Init() {
		socialPlaces = new LinkedList<Social>();
		workplaces = new LinkedList<Workplace>();
		homes = new LinkedList<Home>();
		
	}
	
	public static Social FindTargetSocial() {
		return socialPlaces.get(RandomHelper.nextIntFromTo(0, socialPlaces.size()-1));
	}
	
	public static Workplace FindTargetWorkplace() {
		return workplaces.get(RandomHelper.nextIntFromTo(0, workplaces.size()-1));
	}
	
	public static Home FindTargetHome() {
		return homes.get(RandomHelper.nextIntFromTo(0, homes.size()-1));
	}
}
