package ds.gae.entities;

import java.util.Objects;

import com.google.cloud.datastore.Datastore;
import com.google.cloud.datastore.Entity;
import com.google.cloud.datastore.Key;
import com.google.cloud.datastore.PathElement;

import ds.gae.CarRentalModel;

public class CarType {

	private String name;
	private int nbOfSeats;
	private boolean smokingAllowed;
	private double rentalPricePerDay;
	// trunk space in liters
	private float trunkSpace;

	/***************
	 * CONSTRUCTOR *
	 ***************/

	public CarType(
			String name,
			int nbOfSeats,
			float trunkSpace,
			double rentalPricePerDay,
			boolean smokingAllowed) {
		this.name = name;
		this.nbOfSeats = nbOfSeats;
		this.trunkSpace = trunkSpace;
		this.rentalPricePerDay = rentalPricePerDay;
		this.smokingAllowed = smokingAllowed;
	}

	public CarType(Entity carTypeEntity) {
		this(carTypeEntity.getKey().getName(),
				Math.toIntExact(carTypeEntity.getLong("nbOfSeats")),
				(float) carTypeEntity.getDouble("trunkSpace"),
				carTypeEntity.getDouble("rentalPricePerDay"),
				carTypeEntity.getBoolean("smokingAllowed"));
	}

	public String getName() {
		return name;
	}

	public int getNbOfSeats() {
		return nbOfSeats;
	}

	public boolean isSmokingAllowed() {
		return smokingAllowed;
	}

	public double getRentalPricePerDay() {
		return rentalPricePerDay;
	}

	public float getTrunkSpace() {
		return trunkSpace;
	}

	/*************
	 * TO STRING *
	 *************/

	@Override
	public String toString() {
		return String.format(
				"Car type: %s \t[seats: %d, price: %.2f, smoking: %b, trunk: %.0fl]",
				getName(),
				getNbOfSeats(),
				getRentalPricePerDay(),
				isSmokingAllowed(),
				getTrunkSpace()
		);
	}

	@Override
	public int hashCode() {
		return Objects.hash(name);
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj) {
			return true;
		}
		if (obj == null) {
			return false;
		}
		if (getClass() != obj.getClass()) {
			return false;
		}
		CarType other = (CarType) obj;
		if (!Objects.equals(name, other.name)) {
			return false;
		}
		return true;
	}

	public void persist( String companyName) {
		Datastore datastore = CarRentalModel.get().datastore;
		Key carTypeKey = datastore.newKeyFactory()
				.addAncestors(PathElement.of("CarRentalCompany", companyName))
				.setKind("CarType")
				.newKey(getName());
		
		Entity carType = Entity.newBuilder(carTypeKey)
				.set("nbOfSeats", getNbOfSeats())
				.set("smokingAllowed", isSmokingAllowed())
				.set("rentalPricePerDay", getRentalPricePerDay())
				.set("trunkSpace", getTrunkSpace())
				.build();
		
		datastore.put(carType);
	}
}
