package ds.gae;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.ObjectInputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Properties;

import javax.mail.Message;
import javax.mail.MessagingException;
import javax.mail.Session;
import javax.mail.Transport;
import javax.mail.internet.AddressException;
import javax.mail.internet.InternetAddress;
import javax.mail.internet.MimeMessage;
import javax.servlet.ServletException;
import javax.servlet.ServletInputStream;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

import com.google.appengine.api.taskqueue.Queue;
import com.google.appengine.api.taskqueue.QueueFactory;
import com.google.appengine.api.taskqueue.TaskOptions;
import com.google.cloud.datastore.Datastore;
import com.google.cloud.datastore.DatastoreOptions;
import com.google.cloud.datastore.Transaction;

import ds.gae.entities.CarRentalCompany;
import ds.gae.entities.Quote;
import ds.gae.entities.Reservation;
import ds.gae.view.JSPSite;
import ds.gae.view.Tools;

@SuppressWarnings("serial")
public class Worker extends HttpServlet {

	@Override
	protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
		String renter = "";
		try {
			
			ServletInputStream is = req.getInputStream();
			ObjectInputStream ois = new ObjectInputStream(is);

		    QuotesPayload payload = (QuotesPayload) ois.readObject();
					    
		    ArrayList<Quote> quotes = payload.getQuotes();
		    
			renter = payload.getRenter();
			this.confirmQuotes(quotes);
						
			Session session = Session.getDefaultInstance(new Properties(), null);
			Message msg = new MimeMessage(session);
			msg.setFrom(new InternetAddress("admin@distributed-systems-gae.appspotemail.com"));
			msg.addRecipient(Message.RecipientType.TO, new InternetAddress(renter + "@gmail.com"));
			msg.setSubject("Quotes confirmed");
			msg.setText("Your quotes have succesfully been confirmed");
			Transport.send(msg);

		} catch (ReservationException e) {
				System.out.println(e.getMessage());
				
				Session session = Session.getDefaultInstance(new Properties(), null);
				Message msg = new MimeMessage(session);
				try {
					msg.setFrom(new InternetAddress("admin@distributed-systems-gae.appspotemail.com"));
					msg.addRecipient(Message.RecipientType.TO, new InternetAddress(renter + "@gmail.com"));
					msg.setSubject("Could not confirm quotes");
					msg.setText("Confirmation of your quotes was not succesful");
					Transport.send(msg);
				} catch (MessagingException e1) {
					e1.printStackTrace();
				}
				
		} catch (MessagingException e ) {			
			e.printStackTrace();
		} catch (ClassNotFoundException e) {
			e.printStackTrace();
		} 
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
		Datastore datastore = DatastoreOptions.getDefaultInstance().getService();
		
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
}
