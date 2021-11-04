/**
 * Bookings
 * @author group2
 * this class manages passenger Bookings
 *
 */

public class Bookings  {
	private String bookingCode;
	private Name passengerName;
	private String passengerEmail;
	private Boolean checkInStatus;
	private String flightCode;
	private static final int asciiA = 'A';
	private static final int asciiZ = 'Z';

	public Bookings(String booking, Name name, String email, Boolean status, String flight) {
		bookingCode = booking;
		passengerName = name;
		passengerEmail = email;
		checkInStatus = status;
		flightCode = flight;
	}
	
	/**
	 * setBooking()
	 * @param b, a String representing a booking code
	 */
	public void setBooking(String b) {
		bookingCode = b;
	}

	/**
	 * getBooking()
	 * @return this Booking code.
	 */
	public String getBooking() {
		return bookingCode;
	}

	/**
	 * setName()
	 * @param n, a name of a passenger.
	 */
	public void setName(Name n) {
		passengerName = n;
	}

	/**
	 * getName()
	 * @return the passengers name.
	 */
	public Name getName() {
		return passengerName;
	}
	
	/**
	 * getFirstName
	 * @return the passengers first name only.
	 */
	public String getFirstName() {
		return passengerName.getFirstName() ;
	}
	
	/**
	 * setPassengerEmail()
	 * @param e, a String representing an email address
	 */
	public void setPassengerEmail(String e) {
		passengerEmail = e;
	}

	/**
	 * getPassengerEmail()
	 * @return passenger Email
	 */
	public String getPassengerEmail() {
		return passengerEmail;
	}

	/**
	 * getCheckInStatus
	 * @return check in status (Boolean)
	 */
	public Boolean getCheckInStatus() {
		return checkInStatus;
	}

	/**
	 * setCheckInStatus()
	 * @param status (boolean)
	 * sets the checkInStatus flag
	 */
	public void setCheckinStatus(boolean status) {
		checkInStatus = status;
	}

	/**
	 * checkIn()
	 * @author group2
	 * @return true if booking check in status is successfully changed to true
	 * Changes status of checkInStatus, and returns true is succesful.
	 */
	public Boolean checkIn() {
		if (checkInStatus == false) {
			checkInStatus = true;
			return true;
		} else {
			return false;
		}
	}

	/**
	 * cancelCheckIn 
	 * @author group2
	 * @param
	 * @return true if booking check in status is successfully changed to false.
	 */
	public Boolean cancelCheckIn() {
		if (checkInStatus = true) {
			checkInStatus = false;
			return true;
		} else {
			return false;
		}
	}

	/**
	 * setBookingFlightCode()
	 * @param f, a String 
	 * sets the flightcode to the string parameter.
	 */
	public void setBookingFlightCode(String f) {
		flightCode = f;
	}

	public String getBookingFlightCode() {
		return flightCode;
	}

	/**
	 * hashCode() 
	 * 
	 * @author group2
	 * @return the hashcode of the booking, based on the booking code.
	 */
	public int hashCode() {
		return bookingCode.hashCode();
	}

	/**
	 * equals()
	 * 
	 * @author group2
	 * @param b the booking to be compared.
	 * @return true if both objects are the same.
	 */
	public Boolean equals(Bookings b) {
		if ((this.bookingCode == b.bookingCode) && (this.passengerName == b.passengerName)
				&& (this.passengerEmail == b.passengerEmail) && (this.flightCode == b.flightCode)) {
			return true;
		} else {
			return false;
		}
	}

	/**
	 * checkValidBC()
	 * @author group2
	 * @param s the string to be validated
	 * @return true if s is a valid booking code, false if not
	 */
	public Boolean checkValidBC(String s) {
		int testChar;
		if (s.length() != 6) {
			return false;
		}
		for (int letter = 0; letter < 6; letter++) {
			testChar = s.charAt(letter);
			if ((testChar > asciiZ) || (testChar < asciiA)) {
				return false;
			}
		}
		return true;
	}

	/**
	 * toString()
	 * @author group2
	 * @param b the Booking to be returned as a string
	 * @return s a string of the booking details
	 */
	public String toString() {
		String s = bookingCode ;
		s += " " + passengerName.getFirstAndLastName() ;
		s+= " " + flightCode ;

		return s ;
	}	

	/**
	 * getBookingCode()
	 * @author group2
	 * @return the booking code as a string
	 */
	public String getbookingCode() {
		return bookingCode ;
	}	
}
