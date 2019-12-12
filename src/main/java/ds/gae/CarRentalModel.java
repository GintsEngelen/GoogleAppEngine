package ds.gae;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import com.google.cloud.datastore.Datastore;
import com.google.cloud.datastore.DatastoreOptions;
import com.google.cloud.datastore.Entity;
import com.google.cloud.datastore.Key;
import com.google.cloud.datastore.ProjectionEntity;
import com.google.cloud.datastore.Query;
import com.google.cloud.datastore.QueryResults;
import com.google.cloud.datastore.StructuredQuery.PropertyFilter;
import com.google.cloud.datastore.Transaction;

import ds.gae.entities.Car;
import ds.gae.entities.CarRentalCompany;
import ds.gae.entities.CarType;
import ds.gae.entities.Quote;
import ds.gae.entities.Reservation;
import ds.gae.entities.ReservationConstraints;

public class CarRentalModel {

	public Datastore datastore = DatastoreOptions.getDefaultInstance().getService();
	
	private static CarRentalModel instance;

	public static CarRentalModel get() {
		if (instance == null) {
			instance = new CarRentalModel();
		}
		return instance;
	}

	public void persistCarRentalCompany(CarRentalCompany company) {
		company.persist();
	}
	
	public void persistCars(CarRentalCompany company, Map<Car, CarType> carsMap) {
		Set<CarType> carTypes = new HashSet<CarType>(carsMap.values());
		Set<Car> cars = carsMap.keySet();
		
		for(CarType carType : carTypes) {
			persistCarType(company, carType);
		}
		
		for(Car car : cars) {
			persistCar(company, car);
		}
	}
	
	public void persistCar(CarRentalCompany company, Car car) {
		car.persist(company.getName());
	}
	
	public void persistCarType(CarRentalCompany company, CarType carType) {
		carType.persist(company.getName());
	}
	
	/**
	 * Get the car types available in the given car rental company.
	 *
	 * @param companyName the car rental company
	 * @return The list of car types (i.e. name of car type), available in the given
	 *         car rental company.
	 */
	public Set<String> getCarTypesNames(String companyName) {
		CarRentalCompany company = new CarRentalCompany(companyName);
				
		return company.getCarTypesString();
	}

	/**
	 * Get the names of all registered car rental companies
	 *
	 * @return the list of car rental companies
	 */
	public Collection<String> getAllRentalCompanyNames() {
		Query<ProjectionEntity> query = Query.newProjectionEntityQueryBuilder()
				.setKind("CarRentalCompany")
				.setProjection("name")
				.build();
		QueryResults<ProjectionEntity> results = datastore.run(query);
		
		Collection<String> rentalCompanyNames = new ArrayList<>();
		while(results.hasNext()) {
			ProjectionEntity company = results.next();
			rentalCompanyNames.add(company.getString("name"));
		}
		
    	return rentalCompanyNames;
	}

	/**
	 * Create a quote according to the given reservation constraints (tentative
	 * reservation).
	 * 
	 * @param companyName name of the car renter company
	 * @param renterName  name of the car renter
	 * @param constraints reservation constraints for the quote
	 * @return The newly created quote.
	 * 
	 * @throws ReservationException No car available that fits the given
	 *                              constraints.
	 */
	public Quote createQuote(String companyName, String renterName, ReservationConstraints constraints)
			throws ReservationException {
		CarRentalCompany company = new CarRentalCompany(companyName);
		
		return company.createQuote(constraints, renterName);
	}

	/**
	 * Confirm the given quote.
	 *
	 * @param quote Quote to confirm
	 * 
	 * @throws ReservationException Confirmation of given quote failed.
	 */
	public Reservation confirmQuote(Quote quote) throws ReservationException {
		CarRentalCompany company = new CarRentalCompany(quote.getRentalCompany());
		
		Reservation res = company.confirmQuote(quote);
		res.persist();
		return res;
	}

	/**
	 * Confirm the given list of quotes
	 * 
	 * @param quotes the quotes to confirm
	 * @return The list of reservations, resulting from confirming all given quotes.
	 * 
	 * @throws ReservationException One of the quotes cannot be confirmed. Therefore
	 *                              none of the given quotes is confirmed.
	 */
	public List<Reservation> confirmQuotes(List<Quote> quotes) throws ReservationException {
		List<Reservation> reservations = new ArrayList<>();
		Transaction txn = datastore.newTransaction();
		
		try {
			for(Quote quote : quotes) {
				CarRentalCompany company = new CarRentalCompany(quote.getRentalCompany());
				
				Reservation res = company.confirmQuote(quote, new ArrayList<Reservation>(reservations));
				reservations.add(res);
				
				txn.put(res.getReservationEntity());
			}
			
			txn.commit();
		}
		finally {
			if(txn.isActive()) {
				txn.rollback();
				throw new ReservationException("One of the quotes could not be confirmed");
			}
		}
		
    	return reservations;
	}

	/**
	 * Get all reservations made by the given car renter.
	 *
	 * @param renter name of the car renter
	 * @return the list of reservations of the given car renter
	 */
	public List<Reservation> getReservations(String renter) {
		List<Reservation> reservations = new ArrayList<>();
		
		Datastore datastore = CarRentalModel.get().datastore;
		Query<Entity> query = Query.newEntityQueryBuilder()
				.setKind("Reservation")
				.setFilter(PropertyFilter.eq("renter", renter))
				.build();
		
		QueryResults<Entity> results = datastore.run(query);
		
		while(results.hasNext()) {
			Entity reservationEntity = results.next();
			reservations.add(new Reservation(reservationEntity));
		}

		return reservations;

	}

	/**
	 * Get the car types available in the given car rental company.
	 *
	 * @param companyName the given car rental company
	 * @return The list of car types in the given car rental company.
	 */
	public Collection<CarType> getCarTypesOfCarRentalCompany(String companyName) {
		CarRentalCompany company = new CarRentalCompany(companyName);
		return company.getAllCarTypes();
	}

	/**
	 * Get the list of cars of the given car type in the given car rental company.
	 *
	 * @param companyName name of the car rental company
	 * @param carType     the given car type
	 * @return A list of car IDs of cars with the given car type.
	 */
	public Collection<Integer> getCarIdsByCarType(String companyName, CarType carType) {
		CarRentalCompany company = new CarRentalCompany(companyName);
		return company.getCarIdsByCarType(carType.getName());
	}

	/**
	 * Get the amount of cars of the given car type in the given car rental company.
	 *
	 * @param companyName name of the car rental company
	 * @param carType     the given car type
	 * @return A number, representing the amount of cars of the given car type.
	 */
	public int getAmountOfCarsByCarType(String companyName, CarType carType) {
		return this.getCarsByCarType(companyName, carType).size();
	}

	/**
	 * Get the list of cars of the given car type in the given car rental company.
	 *
	 * @param companyName name of the car rental company
	 * @param carType     the given car type
	 * @return List of cars of the given car type
	 */
	private List<Car> getCarsByCarType(String companyName, CarType carType) {
		// FIXME: use persistence instead
		CarRentalCompany company = new CarRentalCompany(companyName);
		
		return new ArrayList<Car>(company.getCarsOfType(carType.getName()));

	}

	/**
	 * Check whether the given car renter has reservations.
	 *
	 * @param renter the car renter
	 * @return True if the number of reservations of the given car renter is higher
	 *         than 0. False otherwise.
	 */
	public boolean hasReservations(String renter) {
		return this.getReservations(renter).size() > 0;
	}
}
