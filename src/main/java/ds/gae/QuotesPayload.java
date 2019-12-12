package ds.gae;

import java.io.Serializable;
import java.util.ArrayList;

import ds.gae.entities.Quote;

public class QuotesPayload implements Serializable {
	
	private ArrayList<Quote> quotes;
	private String renter;
	
	public QuotesPayload(ArrayList<Quote> quotes, String renter) {
		this.quotes = quotes;
		this.renter = renter;
	}

	public ArrayList<Quote> getQuotes() {
		return quotes;
	}

	public String getRenter() {
		return renter;
	}
	
	
}
