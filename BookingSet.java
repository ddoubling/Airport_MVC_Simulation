import java.io.File;
import java.io.FileNotFoundException;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Scanner;
import java.util.concurrent.atomic.AtomicInteger;


/**
 * Booking Set
 * This class manages a set of Bookings
 * @author group2
 *
 */
public class BookingSet {

	private HashSet<Bookings> hsBookings;
	private File bookingsFile;
	MXlockMgmt protectNonConcurrentList = new MXlockMgmt();

	/**
	 * Constructor of hsBookings creates a Hash Set of Bookings
	 */
	public BookingSet() {
		hsBookings = new HashSet<Bookings>();
	}

	/**
	 * getBookings()
	 * 
	 * @return the hashset of bookings.
	 */
	public HashSet<Bookings> getBookings() {
		return hsBookings;
	} 

	/**
	 * addBooking()
	 * 
	 * @param b booking to be added
	 * @return true if booking is successfully added to the Booking set.
	 */
	public void addBooking(Bookings b) {
		protectNonConcurrentList.enterRegion();
		if (! getBookings().contains(b)) {
			getBookings().add(b);
		}
		protectNonConcurrentList.leaveRegion();
		return ; 
	}



	/**
	 * findThePassenger()
	 * @param name
	 * @param booking
	 * @return true, if this is the booking
	 * looks through the booking set and returns true if a booking of this code and name exist.
	 */
	public Boolean findThePassenger(String name, String booking) {
		Boolean canFind = false ;
		protectNonConcurrentList.enterRegion();
		for (Bookings b : getBookings()) {
			if ((b.getName().getLastName().equals(name)) && (b.getBooking().equals(booking))) {
				canFind = true ;
				break ;
			}
		}
		protectNonConcurrentList.leaveRegion();
		return canFind;
	}

	/**
	 * findByDetails()
	 * @param name
	 * @param booking
	 * @return booking
	 * looks through the booking set and returns the booking matching the parameters, or null if it does not exist.
	 */
	public Bookings findByDetails(String name, String booking) {
		Bookings found = null ;
		protectNonConcurrentList.enterRegion();
		for (Bookings b : getBookings()) {
			if ((b.getName().getLastName().equals(name)) && (b.getBooking().equals(booking))) {
				found = b;
			}
		}
		protectNonConcurrentList.leaveRegion();
		return found;
	}

	/**
	 * removeBooking()
	 * 
	 * @param booking to be removed
	 * @return true if booking is successfully removed.
	 */
	public Boolean removeBooking(Bookings b) {
		Boolean removed = false ;
		protectNonConcurrentList.enterRegion();
		if (getBookings().contains(b)) {
			getBookings().remove(b);
			removed = true;
		} 
		protectNonConcurrentList.leaveRegion();
		return removed ;
	}


	/**
	 * loadBookings()
	 * loads the file of bookings and creates a Booking set.
	 * @author group2
	 * 
	 */
	public void loadBookings(String fileSize) throws FileNotFoundException {
		if (fileSize == "TEST") {
			bookingsFile = new File("bookings file test.TXT");
		}
		else if (fileSize == "SMALL" ) {
			bookingsFile = new File("bookings file small.TXT");
		}
		else if (fileSize == "LARGE") {
			bookingsFile = new File("bookings file large.TXT");
		}
		Scanner sc;
		sc = new Scanner(bookingsFile);
		int count = 1;
		while ( sc.hasNextLine() ) {
			try {
				String row = sc.nextLine();
				String[] bookingData = row.split(",");
				String booking = bookingData[0];
				Name passengerName = new Name(bookingData[1], bookingData[2], bookingData[3]);
				String email = bookingData[4];
				boolean checkInStatus = false;
				if (bookingData[5].equals("Y")) {
					checkInStatus = true;
				}
				String flightCodeString = bookingData[6];				
				Bookings b = new Bookings(booking, passengerName, email, checkInStatus, flightCodeString);
				getBookings().add( b ) ;
			} catch (Exception e) {
				System.out.println(e + " " + "-booking Error on line number " + count );
			}
			count++;
		}
		sc.close();
	}

	/**
	 * findNextBookingToArrive()
	 * @author group2
	 * @param flightCode, a string for a flight
	 * @return a booking for the flight
	 * N.B. if called when hsBookings is empty it will throw nullPointer
	 */
	public Bookings findNextBookingToArrive(String flightCode) {
		protectNonConcurrentList.enterRegion();
		Bookings suitableBooking = null;
		for ( Bookings b : getBookings() ) {
			if (flightCode.equals(b.getBookingFlightCode())) {
				suitableBooking = b;
				getBookings().remove(b);
				break;
			}
		}
		protectNonConcurrentList.leaveRegion();
		return suitableBooking;

	}

	/**
	 * size()
	 * @author group2
	 * @return length of Booking set 
	 * 
	 */
	public int size() {
		int n ;
		protectNonConcurrentList.enterRegion();
		n = getBookings().size() ;
		protectNonConcurrentList.leaveRegion();
		return n;
	}

	/**
	 * iterator()
	 * @return an iterator for the Booking set.
	 */
	public Iterator<Bookings> iterator() {
		protectNonConcurrentList.enterRegion();
		Iterator<Bookings> it = hsBookings.iterator();
		protectNonConcurrentList.leaveRegion();
		return it ;
	}

	/**
	 * countBookingsForFlight()
	 * @param flightCode
	 * @return the count of bookings for this flight in the Booking Set.
	 */
	public int countBookingsForFlight(String flightCode) {
		int count = 0 ;
		protectNonConcurrentList.enterRegion();
		if (! (hsBookings.size() == 0)) {
			for (Bookings b : hsBookings ) {
				if ( b.getBookingFlightCode().contentEquals(flightCode) ) {
					count ++ ;
				}
			}
		} 
		protectNonConcurrentList.leaveRegion() ;
		return count ;
	}

	/**
	 * code reused from F29OC lectures/labs
	 * 
	 */		
	private class MXlockMgmt {
		//Declare and set lock to empty
		AtomicInteger thisCRatomicLock = new AtomicInteger(0);

		MXlockMgmt() {}

		private void enterRegion() {
			int lock;
			do {
				//Copy lock into register and set lock to 1
				lock = thisCRatomicLock.getAndSet(1);
				//Repeat loop if lock was 1				    
			} while (lock == 1);
			// Now lock has been set to 1 by caller, and caller has control of critical region
		}	

		private void leaveRegion() {
			//Release lock and release critical region
			thisCRatomicLock.set(0);
		}
	}	
}