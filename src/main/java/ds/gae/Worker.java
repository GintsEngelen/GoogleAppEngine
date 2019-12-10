package ds.gae;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.ObjectInputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Properties;

import javax.mail.Message;
import javax.mail.MessagingException;
import javax.mail.Session;
import javax.mail.Transport;
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

import ds.gae.entities.Quote;
import ds.gae.view.JSPSite;
import ds.gae.view.Tools;

@SuppressWarnings("serial")
public class Worker extends HttpServlet {

	@Override
	protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
		
		try {
			
			ServletInputStream is = req.getInputStream();
			ObjectInputStream ois = new ObjectInputStream(is);

		    ArrayList<Quote> qs = (ArrayList<Quote>) ois.readObject();
					    
			CarRentalModel.get().confirmQuotes(qs);
			
			String renter = (String) req.getAttribute("renter");
			
			Session session = Session.getDefaultInstance(new Properties(), null);
			Message msg = new MimeMessage(session);
			msg.setFrom(new InternetAddress("admin@distributed-systems-gae.appspotemail.com"));
			msg.addRecipient(Message.RecipientType.TO, new InternetAddress(renter + "@gmail.com"));
			msg.setSubject("Do you copy?");msg.setText("This was sent by our google app engine");
			Transport.send(msg);
			
			System.out.println("Mail succesfully sent, maybe");
		} catch (ReservationException | ClassNotFoundException | MessagingException e) {
				
		}
	}
}
