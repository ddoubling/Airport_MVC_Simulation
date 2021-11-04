import java.time.Duration;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.TreeSet;



/**
 * @author group2
 * This class manages Arrivals.
 * It watches the clock, and instantiates a thread to action arrivals at the airport
 * at 4hrs before departure, using ArrivalGaps to spread arrivals randomly over the next 3hrs, 20mins.
 *
 */
public class ArrivalSupervisor implements Runnable { 
	private SimulationModel airport;
	private Queue<Passenger> checkInQueue ;
	private Queue<Passenger> selfCheckInQueue ;
	private Instant startTime ;
	private BookingSet source ;
	private FlightSet destination ;
	private Logger arrivalsLog ;
	private TreeSet<ThreadPlanner.ThreadData> listOfThreads ;
	private final Duration fortyMinutes = Duration.of(40 * 60L, ChronoUnit.SECONDS);
	private final Duration fourHours = Duration.of(4 * 60 * 60L, ChronoUnit.SECONDS);
	public enum Type { ARRIVALS , SECURITY , BOARDING } ;
	private Type type ;
	private int count = 0 ;
	private TimeSim commonClock ;
	private ThreadPlanner tp ;
	

	public ArrivalSupervisor (Queue<Passenger> queue , Queue<Passenger> selfCheckInQueue, SimulationModel passedAirport , Type typeOfArrival, Instant openTime ,
			BookingSet passedSource, FlightSet passedDestination, ThreadPlanner threadPlanner , TreeSet<ThreadPlanner.ThreadData> threadList   ) {
		airport = passedAirport ;
		this.checkInQueue = queue ;
		this.selfCheckInQueue = selfCheckInQueue ;
		this.type = typeOfArrival  ;
		startTime = openTime ;
		source = passedSource ;
		destination = passedDestination ;
		tp = threadPlanner ; 
		this.listOfThreads = threadList ;
		arrivalsLog = Logger.getInstance() ;
		commonClock = TimeSim.getInstance(airport) ;
	}


	public void run() {
		Thread.currentThread().setPriority(Thread.NORM_PRIORITY - 1);
		// instantiate list of arrivals that need managing
			for ( Object o : destination ) {
				Flight f = (Flight) o ;
				Instant takeOffTime = f.getFlightTime() ;
				Instant startArrivingAt = startTime ;
				if ( takeOffTime.minus(fourHours).isAfter(startTime)) {
					startArrivingAt= takeOffTime.minus(fourHours) ;
				}
				int n = source.countBookingsForFlight(f.getFlightCode()) ;
				Arrivals arrival =  new Arrivals( n , source , checkInQueue, selfCheckInQueue , startArrivingAt, takeOffTime.minus(fortyMinutes), airport, f.getFlightCode(), tp , listOfThreads );
				if ( startArrivingAt.isAfter( commonClock.getTime() ) ) {
					notifyMeAt( startArrivingAt );
				}
				// now launch a thread for these arrivals
				synchronized(Thread.currentThread().getThreadGroup() ) {	// acquire lock on ThreadGroup to prevent clash with thread reporting in SimModel
					Thread at = new Thread( arrival ) ;
					at.setName(type + " " + f.getFlightCode());
					at.setPriority(Thread.NORM_PRIORITY);
					at.start();
				}
				count ++ ;
			}
		arrivalsLog.add("ArrivalsSupervisor created " + count + " arrival threads of type " + type);
		// finished all work, so :
		Thread.currentThread().setPriority(Thread.MIN_PRIORITY);
		do { // nothing	
			notifyMeAt( commonClock.getTime().plus(fourHours));
		} while (! airport.isFinished()) ;
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
			done = tp.wakeMeUpAt( targetTime ) ;
			targetTime = targetTime.plusMillis(1) ;
		} while (! done ) ;
		synchronized ( Thread.currentThread() ) {
			//				System.out.println(commonClock.getSimpleTime() + " " + Thread.currentThread().getName() + " is about to wait() until " + targetTime);
			try { Thread.currentThread().wait(); } catch (InterruptedException e) { e.printStackTrace(); }
			//				System.out.println(commonClock.getSimpleTime() + " " + Thread.currentThread().getName() + " moved on from wait()");
		}
	}
}
