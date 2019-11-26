package ds.gae.entities;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.logging.Level;
import java.util.logging.Logger;

import com.google.cloud.Timestamp;
import com.google.cloud.datastore.Datastore;
import com.google.cloud.datastore.Entity;
import com.google.cloud.datastore.Key;
import com.google.cloud.datastore.ProjectionEntity;
import com.google.cloud.datastore.Query;
import com.google.cloud.datastore.QueryResults;
import com.google.cloud.datastore.StructuredQuery.CompositeFilter;
import com.google.cloud.datastore.StructuredQuery.PropertyFilter;

import ds.gae.CarRentalModel;
import ds.gae.ReservationException;

public class CarRentalCompany {

	private static Logger logger = Logger.getLogger(CarRentalCompany.class.getName());

	private String name;


	/***************
	 * CONSTRUCTOR *
	 ***************/

	public CarRentalCompany(String name) {
		setName(name);
	}
	
	public CarRentalCompany(Entity entity) {
		setName(entity.getKey().getName());
	}
	
	/********
	 * NAME *
	 ********/

	public String getName() {
		return name;
	}

	private void setName(String name) {
		this.name = name;
	}

	/*************
	 * CAR TYPES *
	 *************/

	public Collection<CarType> getAllCarTypes() {
		//TODO: Implement
		return null;
	}

	public CarType getCarType(String carTypeName) {
		//TODO: Implement
		return null;
	}

	public boolean isAvailable(String carTypeName, Date start, Date end) {
		logger.log(Level.INFO, "<{0}> Checking availability for car type {1}", new Object[] { name, carTypeName });
		return getAvailableCarTypes(start, end).contains(getCarType(carTypeName));
	}

	public Set<CarType> getAvailableCarTypes(Date start, Date end) {
		//TODO: Implement
		return null;
//		Set<CarType> availableCarTypes = new HashSet<CarType>();
//		for (Car car : getCars()) {
//			if (car.isAvailable(start, end)) {
//				availableCarTypes.add(car.getType());
//			}
//		}
//		return availableCarTypes;
	}

	/*********
	 * CARS *
	 *********/

	private Car getCar(int uid) {
		//TODO: Implement
		return null;
	}

	public Set<Car> getCars() {
		Datastore datastore = CarRentalModel.get().datastore;
		Set<Car> cars = new HashSet<Car>();
		
		Query<Entity> query = Query.newEntityQueryBuilder()
				.setKind("Car")
				.build();
		QueryResults<Entity> results = datastore.run(query);
		
		while(results.hasNext()) {
			Entity car = results.next();
			cars.add(new Car(Math.toIntExact(car.getKey().getId())));
		}
		
		return cars;
	}
	
	public Set<Car> getCarsOfType(String carType){
		Datastore datastore = CarRentalModel.get().datastore;
		Set<Car> cars = new HashSet<Car>();
		
		Query<Entity> query = Query.newEntityQueryBuilder()
				.setKind("Car")
				.setFilter(PropertyFilter.has))
				.build();
		QueryResults<Entity> results = datastore.run(query);
		
		while(results.hasNext()) {
			Entity car = results.next();
			cars.add(new Car(Math.toIntExact(car.getKey().getId())));
		}
		
		return cars;
	}

	private List<Car> getAvailableCars(String carType, Date start, Date end) {
		List<Car> availableCars = new ArrayList<Car>();
		Datastore datastore = CarRentalModel.get().datastore;
		Set<Car> unavailableCars = getUnavailableCars(start, end);
		
		for (Car car : this.getCars()) {
			if()
		}
		return availableCars;
	}
	
	public Set<Car> getUnavailableCars(Date start, Date end) {
		Timestamp startTimestamp = Timestamp.of(start);
		Timestamp endTimestamp = Timestamp.of(end);
		
		Set<Car> unavailableCars = new HashSet<Car>();
		
		Datastore datastore = CarRentalModel.get().datastore;
		Query<ProjectionEntity> query1 = Query.newProjectionEntityQueryBuilder()
				.setKind("Reservation")
				.setFilter(CompositeFilter.and(
						PropertyFilter.ge("startDate", startTimestamp), 
						PropertyFilter.lt("startDate", endTimestamp)))
				.setProjection("carId")
				.build();
		QueryResults<ProjectionEntity> results1 = datastore.run(query1);
		
		while(results1.hasNext()) {
			ProjectionEntity reservation = results1.next();
			unavailableCars.add(new Car((int) reservation.getLong("carId")));
		}
		
		Query<ProjectionEntity> query2 = Query.newProjectionEntityQueryBuilder()
				.setKind("Reservation")
				.setFilter(CompositeFilter.and(
						PropertyFilter.gt("endDate", startTimestamp), 
						PropertyFilter.le("endDate", endTimestamp)))
				.setProjection("carId")
				.build();
		QueryResults<ProjectionEntity> results2 = datastore.run(query1);
		
		while(results2.hasNext()) {
			ProjectionEntity reservation = results2.next();
			unavailableCars.add(new Car((int) reservation.getLong("carId")));
		}
		
		return unavailableCars;
	}

	/****************
	 * RESERVATIONS *
	 ****************/


	
	public Quote createQuote(ReservationConstraints constraints, String client) throws ReservationException {
		logger.log(Level.INFO, "<{0}> Creating tentative reservation for {1} with constraints {2}",
				new Object[] { name, client, constraints.toString() });

		CarType type = getCarType(constraints.getCarType());

		if (!isAvailable(constraints.getCarType(), constraints.getStartDate(), constraints.getEndDate())) {
			throw new ReservationException("<" + name + "> No cars available to satisfy the given constraints.");
		}

		double price = calculateRentalPrice(
				type.getRentalPricePerDay(), 
				constraints.getStartDate(),
				constraints.getEndDate()
		);

		return new Quote(
				client,
				constraints.getStartDate(),
				constraints.getEndDate(),
				getName(),
				constraints.getCarType(),
				price
		);
	}

	// Implementation can be subject to different pricing strategies
	private double calculateRentalPrice(double rentalPricePerDay, Date start, Date end) {
		return rentalPricePerDay * Math.ceil((end.getTime() - start.getTime()) / (1000 * 60 * 60 * 24D));
	}

	public Reservation confirmQuote(Quote quote) throws ReservationException {
		logger.log(Level.INFO, "<{0}> Reservation of {1}", new Object[] { name, quote.toString() });
		List<Car> availableCars = getAvailableCars(quote.getCarType(), quote.getStartDate(), quote.getEndDate());
		if (availableCars.isEmpty()) {
			throw new ReservationException("Reservation failed, all cars of type " + quote.getCarType()
					+ " are unavailable from " + quote.getStartDate() + " to " + quote.getEndDate());
		}
		Car car = availableCars.get((int) (Math.random() * availableCars.size()));

		Reservation res = new Reservation(quote, car.getId());
		car.addReservation(res);
		return res;
	}

	public void cancelReservation(Reservation res) {
		logger.log(Level.INFO, "<{0}> Cancelling reservation {1}", new Object[] { name, res.toString() });
		getCar(res.getCarId()).removeReservation(res);
	}

	public void persist() {
		Datastore datastore = CarRentalModel.get().datastore;
		Key carRentalCompanyKey = datastore.newKeyFactory()
				.setKind("CarRentalCompany")
				.newKey(getName());
		
		Entity rentalCompany = Entity.newBuilder(carRentalCompanyKey)
				.set("name", getName())
				.build();
		
		datastore.put(rentalCompany);
	}
}
