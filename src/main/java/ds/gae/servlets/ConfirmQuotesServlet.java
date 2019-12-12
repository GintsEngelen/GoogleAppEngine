package ds.gae.servlets;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.ObjectOutputStream;
import java.util.ArrayList;
import java.util.HashMap;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

import com.google.appengine.api.taskqueue.Queue;
import com.google.appengine.api.taskqueue.QueueFactory;
import com.google.appengine.api.taskqueue.TaskOptions;

import ds.gae.CarRentalModel;
import ds.gae.QuotesPayload;
import ds.gae.ReservationException;
import ds.gae.entities.Quote;
import ds.gae.view.JSPSite;
import ds.gae.view.Tools;

@SuppressWarnings("serial")
public class ConfirmQuotesServlet extends HttpServlet {
		
	@SuppressWarnings("unchecked")
	@Override
	protected void doPost(HttpServletRequest req, HttpServletResponse resp)
			throws ServletException, IOException {
		
		HttpSession session = req.getSession();
		HashMap<String, ArrayList<Quote>> allQuotes = (HashMap<String, ArrayList<Quote>>) session.getAttribute("quotes");

		String renter = (String) session.getAttribute("renter");
		
		ArrayList<Quote> qs = new ArrayList<Quote>();
			
		for (String crcName : allQuotes.keySet()) {
			qs.addAll(allQuotes.get(crcName));
		}
		
		QuotesPayload payload = new QuotesPayload(qs, renter);
					
		ByteArrayOutputStream baos = new ByteArrayOutputStream();
		ObjectOutputStream oos = new ObjectOutputStream(baos);
		oos.writeObject(payload);
		oos.close();
			
		byte[] rawData = baos.toByteArray();
			
		Queue queue = QueueFactory.getDefaultQueue();
		queue.add(TaskOptions.Builder.withUrl("/worker").payload(rawData));
		
		resp.sendRedirect(JSPSite.CONFIRM_QUOTES_RESPONSE.url());
	}
}
