package ds.gae;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.ObjectInputStream;
import java.util.ArrayList;
import java.util.HashMap;

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
		
		HttpSession session = req.getSession();
		
		try {
			
			ServletInputStream is = req.getInputStream();
			ObjectInputStream ois = new ObjectInputStream(is);

		    ArrayList<Quote> qs = (ArrayList<Quote>) ois.readObject();
			
		    System.out.println("Retrieving object succesful -------------------------------- ");
		    
			CarRentalModel.get().confirmQuotes(qs);
			
			session.setAttribute("quotes", new HashMap<String, ArrayList<Quote>>());
			
			resp.sendRedirect(JSPSite.CONFIRM_QUOTES_RESPONSE.url());
		} catch (ReservationException | ClassNotFoundException e) {
			session.setAttribute("errorMsg", Tools.encodeHTML(e.getMessage()));
			resp.sendRedirect(JSPSite.RESERVATION_ERROR.url());				
		}
	}
}
