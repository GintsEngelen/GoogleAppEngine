package ds.gae.entities;

import java.util.Date;
import java.util.HashSet;
import java.util.Set;

import com.google.cloud.datastore.Datastore;
import com.google.cloud.datastore.Entity;
import com.google.cloud.datastore.Key;
import com.google.cloud.datastore.PathElement;

import ds.gae.CarRentalModel;

public class Car {

	private int id;
	private String carType;
//	private CarType carType;
//	private Set<Reservation> reservations;

	/***************
	 * CONSTRUCTOR *
	 ***************/

	public Car(int uid, String carType) {
		this.id = uid;
		this.carType = carType;
	}

	public Car(Entity carEntity) {
		this(Math.toIntExact(carEntity.getKey().getId()), carEntity.getString("carType"));
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

	public String getCarType() {
		//TODO: Implement
		return this.carType;
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

	public void persist(String companyName) {
		Datastore datastore = CarRentalModel.get().datastore;
		Key carKey = datastore.newKeyFactory()
				.addAncestors(PathElement.of("CarRentalCompany", companyName), PathElement.of("CarType", getCarType()))
				.setKind("Car")
				.newKey(this.id);
		
		Entity car = Entity.newBuilder(carKey)
				.set("carType", getCarType())
				.build();
		
		datastore.put(car);
	}
	
	@Override
	public boolean equals(Object object) {
		if(object instanceof Car) {
			return this.id == ((Car) object).id;
		}
		return false;
	}
}
