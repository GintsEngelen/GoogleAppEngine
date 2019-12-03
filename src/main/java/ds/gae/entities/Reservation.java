package ds.gae.entities;

import java.util.Date;
import java.util.Objects;

import com.google.cloud.Timestamp;
import com.google.cloud.datastore.Datastore;
import com.google.cloud.datastore.Entity;
import com.google.cloud.datastore.Key;

import ds.gae.CarRentalModel;

public class Reservation extends Quote {
	
	private int carId;

	/***************
	 * CONSTRUCTOR *
	 ***************/

	public Reservation(Quote quote, int carId) {
		this( 
				quote.getRenter(),
				quote.getStartDate(),
				quote.getEndDate(),
				quote.getRentalCompany(),
				quote.getCarType(),
				quote.getRentalPrice()
		);
		this.carId = carId;
	}

	private Reservation(
			String renter,
			Date start,
			Date end,
			String rentalCompany,
			String carType,
			double rentalPrice) {
		super(renter, start, end, rentalCompany, carType, rentalPrice);
	}

	public Reservation(Entity reservationEntity) {
		this(reservationEntity.getString("renter"),
				reservationEntity.getTimestamp("startDate").toDate(),
				reservationEntity.getTimestamp("endDate").toDate(),
				reservationEntity.getString("rentalCompany"),
				reservationEntity.getString("carType"),
				reservationEntity.getDouble("rentalPrice"));
		this.carId = Math.toIntExact(reservationEntity.getLong("carId"));
	}

	/******
	 * ID *
	 ******/

	public int getCarId() {
		return carId;
	}

	/*************
	 * TO STRING *
	 *************/

	@Override
	public String toString() {
		return String.format(
				"Reservation for %s from %s to %s at %s\nCar type: %s\tCar: %s\nTotal price: %.2f",
				getRenter(),
				getStartDate(),
				getEndDate(),
				getRentalCompany(),
				getCarType(),
				getCarId(),
				getRentalPrice()
		);
	}

	@Override
	public int hashCode() {
		return Objects.hash(super.hashCode(), getCarId());
	}

	@Override
	public boolean equals(Object obj) {
		if (!super.equals(obj)) {
			return false;
		}
		Reservation other = (Reservation) obj;
		if (getCarId() != other.getCarId()) {
			return false;
		}
		return true;
	}

	public void persist() {
		Datastore datastore = CarRentalModel.get().datastore;
		
		datastore.put(getReservationEntity());
	}
	
	public Entity getReservationEntity() {
		Datastore datastore = CarRentalModel.get().datastore;
		Key reservationKey = datastore.allocateId(datastore.newKeyFactory().setKind("Reservation").newKey());
				
		Entity reservation = Entity.newBuilder(reservationKey)
				.set("carId", getCarId())
				.set("startDate", Timestamp.of(getStartDate()))
				.set("endDate", Timestamp.of(getEndDate()))
				.set("renter", getRenter())
				.set("rentalCompany", getRentalCompany())
				.set("carType", getCarType())
				.set("rentalPrice", getRentalPrice())
				.build();
		
		return reservation;
	}
}
