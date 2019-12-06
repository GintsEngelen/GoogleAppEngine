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
import com.google.cloud.datastore.PathElement;
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
		List<CarType> carTypes = new ArrayList<>();
		
		Datastore datastore = CarRentalModel.get().datastore;
		Query<Entity> query = Query.newEntityQueryBuilder()
				.setKind("CarType")
				.setFilter(PropertyFilter.hasAncestor(datastore.newKeyFactory().setKind("CarRentalCompany").newKey(this.getName())))
				.build();
		
		QueryResults<Entity> results = datastore.run(query);
		
		while(results.hasNext()) {
			Entity carTypeEntity = results.next();
			carTypes.add(new CarType(carTypeEntity));
		}

		return carTypes;
	}
	
	public Set<String> getCarTypesString() {
		Set<String> carTypesString = new HashSet<>();
		
		Collection<CarType> carTypes = getAllCarTypes();
		for(CarType carType : carTypes) {
			carTypesString.add(carType.getName());
		}
		return carTypesString;
	}

	public CarType getCarType(String carTypeName) {
		
		Datastore datastore = CarRentalModel.get().datastore;
		Key carTypeKey = datastore.newKeyFactory().
				addAncestor(PathElement.of("CarRentalCompany", this.getName())).
				setKind("CarType").newKey(carTypeName);

		return new CarType(datastore.get(carTypeKey));
	}

	public boolean isAvailable(String carTypeName, Date start, Date end) {
		logger.log(Level.INFO, "<{0}> Checking availability for car type {1}", new Object[] { name, carTypeName });
		return getAvailableCarTypes(start, end).contains(getCarType(carTypeName));
	}

	public Set<CarType> getAvailableCarTypes(Date start, Date end) {
		Set<Car> availableCars = new HashSet(this.getAvailableCars(start, end));
		Set<CarType> availableCarTypes = new HashSet<>();
		
		for(Car car : availableCars) {
			availableCarTypes.add(getCarType(car.getCarType()));
		}
		
		return availableCarTypes;
	}

	/*********
	 * CARS *
	 *********/

	private Car getCar(int uid) {
		Datastore datastore = CarRentalModel.get().datastore;
		Key carKey = datastore.newKeyFactory()
				.addAncestor(PathElement.of("CarRentalCompany", this.getName()))
				.setKind("Car").newKey(uid);

		return new Car(datastore.get(carKey));

	}

	public Set<Car> getCars() {
		Datastore datastore = CarRentalModel.get().datastore;
		Set<Car> cars = new HashSet<Car>();
		
		Query<Entity> query = Query.newEntityQueryBuilder()
				.setKind("Car")
				.setFilter(PropertyFilter.hasAncestor(datastore.newKeyFactory().setKind("CarRentalCompany").newKey(this.getName())))
				.build();
		QueryResults<Entity> results = datastore.run(query);
		
		while(results.hasNext()) {
			Entity car = results.next();
			cars.add(new Car(Math.toIntExact(car.getKey().getId()), car.getString("carType")));
		}
		
		return cars;
	}
	
	public Set<Car> getCarsOfType(String carType){
		Datastore datastore = CarRentalModel.get().datastore;
		Set<Car> cars = new HashSet<Car>();
		
		Query<Entity> query = Query.newEntityQueryBuilder()
				.setKind("Car")
				.setFilter(CompositeFilter.and(
						PropertyFilter.eq("carType", carType),
						PropertyFilter.hasAncestor(datastore.newKeyFactory().setKind("CarRentalCompany").newKey(this.getName()))))
				.build();
		QueryResults<Entity> results = datastore.run(query);
		
		while(results.hasNext()) {
			Entity car = results.next();
			cars.add(new Car(Math.toIntExact(car.getKey().getId()), car.getString("carType")));
		}
		
		return cars;
	}
	
	public Set<Integer> getCarIdsByCarType(String carType){
		Set<Car> cars = getCarsOfType(carType);
		Set<Integer> carIds = new HashSet<Integer>();
		
		for(Car car : cars) {
			carIds.add(car.getId());
		}
		
		return carIds;
	}

	private List<Car> getAvailableCars(Date start, Date end) {
		List<Car> availableCars = new ArrayList<Car>();
		Datastore datastore = CarRentalModel.get().datastore;
		Set<Car> unavailableCars = getUnavailableCars(start, end);
		
		for (Car car : this.getCars()) {
			if(!unavailableCars.contains(car)) availableCars.add(car);
		}
		return availableCars;
	}
	
	private List<Car> getAvailableCarsForCarType(String carType, Date start, Date end) {
		List<Car> availableCars = getAvailableCars(start, end);
		List<Car> availableCarsForCarType = new ArrayList<Car>();
		
		for(Car car : availableCars) {
			if(car.getCarType().equals(carType)) availableCarsForCarType.add(car);
		}
		return availableCarsForCarType;
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
						PropertyFilter.lt("startDate", endTimestamp),
						PropertyFilter.eq("rentalCompany", this.getName())))
				.setProjection("carId", "carType")
				.build();
		QueryResults<ProjectionEntity> results1 = datastore.run(query1);
		
		while(results1.hasNext()) {
			ProjectionEntity reservation = results1.next();
			unavailableCars.add(new Car((int) reservation.getLong("carId"), reservation.getString("carType")));
		}
		
		Query<ProjectionEntity> query2 = Query.newProjectionEntityQueryBuilder()
				.setKind("Reservation")
				.setFilter(CompositeFilter.and(
						PropertyFilter.gt("endDate", startTimestamp), 
						PropertyFilter.le("endDate", endTimestamp),
						PropertyFilter.eq("rentalCompany", this.getName())))
				.setProjection("carId", "carType")
				.build();
		QueryResults<ProjectionEntity> results2 = datastore.run(query1);
		
		while(results2.hasNext()) {
			ProjectionEntity reservation = results2.next();
			unavailableCars.add(new Car((int) reservation.getLong("carId"), reservation.getString("carType")));
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
		List<Car> availableCars = getAvailableCarsForCarType(quote.getCarType(), quote.getStartDate(), quote.getEndDate());
		if (availableCars.isEmpty()) {
			throw new ReservationException("Reservation failed, all cars of type " + quote.getCarType()
					+ " are unavailable from " + quote.getStartDate() + " to " + quote.getEndDate());
		}
		Car car = availableCars.get((int) (Math.random() * availableCars.size()));
		
		Reservation res = new Reservation(quote, car.getId());
		return res;
	}

	public void cancelReservation(Reservation res) {
		//TODO: implement
		logger.log(Level.INFO, "<{0}> Cancelling reservation {1}", new Object[] { name, res.toString() });
		//getCar(res.getCarId()).removeReservation(res);
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
