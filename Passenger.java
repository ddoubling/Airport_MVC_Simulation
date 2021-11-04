/**
 * 
 */


import java.time.Instant;

/**
 * @author group2
 * This class generates a Passenger arriving at the airport for a flight.
 * A Passenger is a Booking, a Bag, and a time of arrival.
 * 
 * @param <Bookings> a passenger booking
 * @param <Baggage> a bag
 * @param <Instant> time of the next arrival
 */
public class Passenger {
	private static int passengerCount = 0;
	private static int passengerIDno;
	private Bookings bookingDetails;
	private Baggage bagDetails;
	private Instant arrivalTime;
	private Boolean fees ;
	private Double cost ;
	public enum passengerStatus {BOOKING ,ARRIVEDINQUEUE ,BEINGCHECKEDIN, DEPARTURES} // add other status's later
	private passengerStatus status ;


	/**
	 * Passenger(b, bb, time)
	 * @param b
	 * @param bb
	 * @param timeOfArrival
	 * Constructor, builds a Passenger with these parameters.
	 */
	public Passenger( Bookings b, Baggage bb, Instant timeOfArrival) {
		passengerIDno = provideIDNo();
		bookingDetails = b;
		bagDetails = bb;
		arrivalTime = timeOfArrival;
		fees = false ;
		cost = 0D ;
		status = passengerStatus.BOOKING ;
	}

	/**
	 * provideIDNo()
	 * @return passengers ID number
	 */
	private int provideIDNo() {
		passengerCount ++ ;
		return passengerCount;
	}
	/**
	 * getPassengerIDNo()
	 * @return passengers ID Number
	 */
	public int getPassengerIDNo() {
		return passengerIDno;
	}
	
	/**
	 * getBagWeight()
	 * @return this passengers bag weight.
	 */
	public double getBagWeight() {
		return bagDetails.getBagWeight();
	}
	
	/** 
	 * getBagType()
	 * @return the type of bag this passenger has.
	 */
	public String getBagType() {
		return bagDetails.getTypeOfBag() ;
	}

	/**
	 * getBagVolume()
	 * @return the cubic volume of this passengers bag.
	 */
	public double getBagVolume() {
		return bagDetails.getBagVolume();
	}

	/**
	 * getTimeOfArrival()
	 * @return the time at which this passenger willl/has arrived at the airport
	 */
	public Instant getTimeOfArrival() {
		return arrivalTime;
	}

	/**
	 * getPassengersFlight()
	 * @return this passengers Flight code.
	 */
	public String getPassengersFlight() {
		return this.bookingDetails.getBookingFlightCode();
	}

	/**
	 * checkFees()
	 * @return a boolean indicating if this passenger has fees.
	 */
	public Boolean checkFees() {
		return fees ;
	}

	/**
	 * @author group2
	 * getBaggage()
	 * @return a boolean indicating if the passenger has a bag
	 */
	public Boolean getBaggage() {
		if ( ! bagDetails.getHasABag() ) {
			return false ;
		}
		return true ;
	}

	/**
	 * @author group2
	 * getBooking()
	 * @return the Passengers Booking
	 */
	public Bookings getBooking() {
		return bookingDetails ;
	}

	/**
	 * @author group2
	 * getPassengerName()
	 * @return the Passengers Name
	 */
	public String getPassengerName() {
		return (bookingDetails.getName().getFullName() ) ;
	}	

	/**
	 * @author group2
	 * getPassengerName()
	 * @return the Passengers first name
	 */
	public String getPassengerFirstName() {
		return (bookingDetails.getFirstName().toString() ) ;
	}

	/**
	 * @author group2
	 * setCheckInStatus()
	 * @param a boolean
	 */
	public void setCheckInStatus(Boolean YorN) {
		bookingDetails.setCheckinStatus(YorN);
	}

	/**
	 *@author group2
	 * getPassengerDetails()
	 * @return the Passengers Details
	 */
	public String getPassengerDetails() {
		String details = "" ;
		details += getPassengerFirstName() + " " ;
		details += "Bag: " + getBagType() ;
		details += " Weight: " + getBagWeight() + "Kgs ";
		details += "Arrives @ " + getTimeOfArrival().toString();
		return details  ;
	}
	
	/**
	 *@author group2
	 * getPassengerInformation()
	 * with two options, to feed two different queue reports
	 * @param deskORself a boolean, true = checkindesk, false = selfCheckIn
	 * @return the Passengers Details
	 */
	public String getPassengerInformation(boolean deskORself) {
		String information = "";
		synchronized( this ) {
		information += "   " + getTimeOfArrival().toString().substring(11, 19) + "     ";
		information += String.format( "%0$-8s", getPassengersFlight()) + "    ";
		information += getPassengerNameIn20Chars() + "  " ;
		if (deskORself) {
			information += bagDetails.getTypeOfBagLower() ;
		}
		else {
			information += this.getStatus().toString();
		}
		information += "\n" ;
		}
		return information;
	}

	/**
	 * getPassengerNameIn20Chars()
	 * @return The longest format of a passengers name that fits in 20 characters.
	 */
	private String getPassengerNameIn20Chars() {
		return (bookingDetails.getName().getNameIn20Chars() );
	}


	/**
	 * getStatus()
	 * @author group2
	 * @return the status
	 */
	public passengerStatus getStatus() {
		return status;
	}


	/**
	 * setStatus()
	 * @author group2
	 * @param status, the status to set
	 */
	public void setStatus(passengerStatus status) {
		this.status = status;
	}	
	/**
	 * setCost()
	 * @author group2
	 * @param any fees to be paid
	 */
	public void setCost(Double price) {
		this.cost= price;
	}	
	
	/**
	 * getCost()
	 * @return the cost.
	 */
	public Double getCost() {
		return this.cost ;
	}
	
	/**
	 * nullPassengerDetails()
	 * This method sets all objects & data within the Passenger to null.
	 * It should only be used as the passenger is leaving the airport.
	 * This represents us 'forgetting' the personal data of this passenger,
	 * so its good GDPR practice, plus setting all these objects to null signals
	 * the garbage collector it may clear this data out of memory. 
	 */
	public void nullPassengerDetails() {
		this.passengerIDno = 0;
		this.bookingDetails = null;
		this.bagDetails = null;
		this.arrivalTime = null;
		this.fees = null ;
		this.cost = null ;
		this.status = null ;
	}
	
	/**
	 * canSelfCheckIn()
	 * @return boolean flag
	 * A Passenger may only self check in if they are travelling with 
	 * no baggage, or just a cabin bag.
	 */
	public boolean getCanSelfCheckIn() {
		String bag = this.getBagType() ;
		boolean noBag = (bag == "NOBAG");
		boolean cabinB =(bag == "CABIN");
		return  ( noBag || cabinB ) ;		
	}
	
}