package cz.cuni.mff.d3s.jdeeco.roleSwitching.demo;

import cz.cuni.mff.d3s.deeco.annotations.Component;
import cz.cuni.mff.d3s.deeco.annotations.In;
import cz.cuni.mff.d3s.deeco.annotations.PeriodicScheduling;
import cz.cuni.mff.d3s.deeco.annotations.PlaysRole;
import cz.cuni.mff.d3s.deeco.annotations.Process;
import cz.cuni.mff.d3s.deeco.knowledge.KnowledgeManager;
import cz.cuni.mff.d3s.deeco.task.ProcessContext;

/**
 * One implementation of the vehicle role.
 * 
 * See {@link BoardingEnsemble} for more details.
 * 
 * @author Zbyněk Jiráček
 *
 */

@Component
@PlaysRole(VehicleRole.class)
public class Car {

	public String id;
	
	public Integer capacity;
	
	public String type = "Car";
	
	public Car(String id, int capacity) {
		this.id = id;
		this.capacity = capacity;
	}
	
	@Process
	@PeriodicScheduling(period = 200)
	public static void abandonRole(
			@In("id") String id
		) {
		long time = ProcessContext.getTimeProvider().getCurrentMilliseconds();

		KnowledgeManager kManager = ProcessContext.getCurrentProcess().getComponentInstance().getKnowledgeManager();

		if (id.equals("Seat Ibiza")) {
			if (time == 600) {
				kManager.updateRoles(null);
				System.out.println("Seat Ibiza abandoned VehicleRole at time " + time);
			}
			else if (time == 800) {
				kManager.updateRoles(new Class[]{VehicleRole.class});
				System.out.println("Seat Ibiza plays VehicleRole at time " + time);
			}
			else {
				System.out.println("Seat Ibiza abandonedRole - wrong time");
			}
		}
	}
	
	@Override
	public String toString() {
		return String.format("%s car (capacity: %d)", id, capacity);
	}

}
