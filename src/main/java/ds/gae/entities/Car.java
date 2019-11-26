package ds.gae.entities;

import java.util.Date;
import java.util.HashSet;
import java.util.Set;

import com.google.cloud.datastore.Datastore;
import com.google.cloud.datastore.Entity;
import com.google.cloud.datastore.Key;
import com.google.cloud.datastore.PathElement;

public class Car {

	private int id;
//	private CarType carType;
//	private Set<Reservation> reservations;

	/***************
	 * CONSTRUCTOR *
	 ***************/

	public Car(int uid) {
		this.id = uid;
	}

	/******
	 * ID *
	 ******/

	public int getId() {
		return id;
	}

	/************
	 * CAR TYPE *
	 ************/

	public CarType getType() {
		//TODO: Implement
		return null;
	}

	/****************
	 * RESERVATIONS *
	 ****************/

	public Set<Reservation> getReservations() {
		//TODO: Implement
		return null;
	}

	public boolean isAvailable(Date start, Date end) {
		if (!start.before(end)) {
			throw new IllegalArgumentException("Illegal given period");
		}

		for (Reservation reservation : getReservations()) {
			if (reservation.getEndDate().before(start) || reservation.getStartDate().after(end)) {
				continue;
			}
			return false;
		}
		return true;
	}

	public void addReservation(Reservation res) {
		//TODO: Implement
	}

	public void removeReservation(Reservation reservation) {
		//TODO: Implement
	}

	public void persist(Datastore datastore, String companyName) {
		Key carKey = datastore.newKeyFactory()
				.addAncestor(PathElement.of("CarRentalCompany", companyName))
				.setKind("Car")
				.newKey(this.id);
		
		Entity car = Entity.newBuilder(carKey)
				.build();
		
		datastore.put(car);
	}
}
