import java.time.Duration;
import java.time.Instant;
import java.util.TreeSet;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * @author group2
 *
 */
public class Arrivals implements Runnable {
	// other classes used here
	private BookingSet allBookings ; 
	private SimulationModel airport;
	private TimeSim commonClock ;
	private RandomNumbers random ;
	private ArrivalGaps durations ; 
	private Logger arrivalsLog ;
	private ThreadPlanner threadPlanner ;
	// time variables
	private Instant arrivalStartTime;
	private Instant arrivalEndTime; 
	private Instant thisThreadTime ;	
	// data used to manage arrivals
	private Queue<Passenger> checkInQueue ;
	private Queue<Passenger> selfCheckInQueue ;
	private TreeSet<ThreadPlanner.ThreadData> listOfThreads ;
	private String flightCode ;
	private static int arrivalsCounter = 0 ;
	private int id ;
	private int howMany ;
	private MXlockMgmt protectSharedFactory= new MXlockMgmt();	

	public Arrivals(int howMany , BookingSet allBookings, Queue<Passenger> checkInQueue, Queue<Passenger> selfCheckInQueue, Instant startTime, Instant endTime, SimulationModel model, String flightCode, ThreadPlanner tp , TreeSet<ThreadPlanner.ThreadData> tpL) {
		this.howMany = howMany ;
		this.allBookings=allBookings;
		this.arrivalStartTime = startTime;
		this.arrivalEndTime = endTime;
		this.airport=model;
		this.checkInQueue=checkInQueue;
		this.selfCheckInQueue = selfCheckInQueue ;
		this.thisThreadTime = startTime ;
		this.flightCode = flightCode ;
		this.random = RandomNumbers.getInstance() ;
		this.durations = new ArrivalGaps(this.howMany, Duration.between(this.arrivalStartTime, this.arrivalEndTime)) ;
		this.id = arrivalsCounter ;
		arrivalsCounter++ ;
		commonClock = TimeSim.getInstance( airport );
		this.arrivalsLog = Logger.getInstance();
		this.threadPlanner = tp ;
		this.listOfThreads = tpL ;
		String log = " Arrivals process for " + this.flightCode + " instantiated. n=" + this.howMany + " Start = " + this.arrivalStartTime.toString().substring(11, 19) 
				+ " EndTime = " + this.arrivalEndTime.toString().substring(11, 19)  ;
		arrivalsLog.add(commonClock.getSimpleTime() + log);
	}


	/**
	 * run()
	 * This thread will, for a specific flight, monitor the clock and
	 * then bring passengers to the airport in random time intervals, within
	 * the time limits set by airport rules.
	 * The passenger will either join the check in queue, or self check in queue
	 * depending on their baggage.
	 */
	public void run() {		 
		String logEntry ;
		logEntry = commonClock.getSimpleTime() + " Starting Arrivals " + this.flightCode + " Thread @ " + this.thisThreadTime ;
		arrivalsLog.add(logEntry);
		if (arrivalStartTime.isAfter( commonClock.getTime()) ) {
			notifyMeAt( arrivalStartTime );
		}
		int count = 0;
		while ( count < this.howMany)  {
			Duration delay = durations.getGapToNextArrival() ;
			this.thisThreadTime = this.thisThreadTime.plus( delay );
			Passenger newPassenger = createArrivingPassenger(this.thisThreadTime) ;
			logEntry = commonClock.getSimpleTime() + " Arrivals " + flightCode +" No " + count + " " + newPassenger.getBooking().getName().getFirstName() 
					+ " id= " +newPassenger.getPassengerIDNo() + " with bag type : " +newPassenger.getBagType();
			if ( ! commonClock.getTime().isAfter(thisThreadTime) ) {
				notifyMeAt( thisThreadTime );
			}
			if (newPassenger.getCanSelfCheckIn()) {
				selfCheckInQueue.enQueue( newPassenger);
				logEntry += " has arrived intending to Self Check In." ; 
			}
			else {
				checkInQueue.enQueue( newPassenger ) ;
				logEntry += " has arrived & joined the CheckIn queue." ; 
			}
			newPassenger.setStatus(Passenger.passengerStatus.ARRIVEDINQUEUE) ;
			allBookings.removeBooking(newPassenger.getBooking()) ;
			count ++ ;
			arrivalsLog.add(logEntry);
		}
		logEntry = commonClock.getSimpleTime() + " Arrivals " + flightCode + " is Finished & Exiting, " + count + " have Arrived." ;
		arrivalsLog.add(logEntry) ;
		this.durations = null ; // kill this for garbage collection
	}


	/**
	 * createArrivingPassenger()
	 * @author group2
	 * @param Instant clock, the current time
	 * @return a Passenger, or null if none left.
	 * this method creates a Passenger
	 */	
	public Passenger createArrivingPassenger(Instant clock ) {
		synchronized(this) { 
			protectSharedFactory.enterRegion();
			Bookings b = allBookings.findNextBookingToArrive( this.flightCode) ;
			if ( b == null) {
				return null ;
			}
			Baggage bag = Baggage.setBaggage( random.getNextRandomInteger(7)%6 ) ; // makes 0 & 1 twice as likely as 2,3,4,5.
			Passenger p = new Passenger(b, bag, clock) ;
			protectSharedFactory.leaveRegion();
			return p ;
		}
	}



	/**
	 * @return the id
	 */
	public int getId() {
		return id;
	}

	/**
	 * notifyMeAt()
	 * @author group2
	 * @param targetTime - an Instant being the time this thread needs to take next action
	 * this method logs the required notify() time to the ThreadPlanner
	 * and puts the holding thread into wait(). 
	 */	
	private void notifyMeAt (Instant targetTime) {
		Boolean done = false ;
		do {
			done = threadPlanner.wakeMeUpAt( targetTime ) ;
			targetTime = targetTime.plusMillis(1) ;
		} while (! done ) ;
		synchronized ( Thread.currentThread() ) {
			try { Thread.currentThread().wait(); } catch (InterruptedException e) { e.printStackTrace(); }
		}
	}


	/**
	 * MXlockMgmt
	 * code reused from F29OC lectures/labs
	 * a class providing MX
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
