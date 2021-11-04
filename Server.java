import java.time.Duration;
import java.time.Instant;
import java.util.TreeSet;
import java.util.concurrent.atomic.AtomicBoolean;

/**
 * 
 */

/**
 * @author group2
 * This class provides a generic queue server
 *
 */
public abstract class Server implements Runnable {
	public enum ServerTypes { CHECKIN, BOARDING } ; // can add SECURITY DUTYFREE BOARDING etc
	private ServerTypes typeOfServer ;
	protected Queue<Passenger> checkInQueue;
	protected Queue<Passenger> selfCheckInQueue ;
	protected Queue<Passenger> waiting;
	protected Queue<Passenger> lateArrivals;
	protected Queue<Passenger> boardingQueue; 	// a queue at each gate which passengers jon to board the floght
	protected Queue<Flight> flightQueue; 		// a queue of planned flights, in time order, again used as has MX protection &
												// methods to manipulate
	
	public enum Stage { EMPTY, ARRIVAL, LATE, BAGGAGE, FEES, PAID, CHECKEDIN} ;
	protected ServerCheckInDesk.Stage empty = 	ServerCheckInDesk.Stage.EMPTY ;	
	protected ServerCheckInDesk.Stage arrival = 	ServerCheckInDesk.Stage.ARRIVAL ;
	protected ServerCheckInDesk.Stage late = 		ServerCheckInDesk.Stage.LATE ;
	protected ServerCheckInDesk.Stage baggage = 	ServerCheckInDesk.Stage.BAGGAGE ;
	protected ServerCheckInDesk.Stage fees = 		ServerCheckInDesk.Stage.FEES ;
	protected ServerCheckInDesk.Stage paid = 		ServerCheckInDesk.Stage.PAID ;
	protected ServerCheckInDesk.Stage checkedin = ServerCheckInDesk.Stage.CHECKEDIN ;
	protected Stage stage;
	
	
	protected TimeSim airportTime;
	private int serverID ;
	private int cumCountOfServers = 0 ;
	private int id;
	private int waitTime; 
	private boolean hasBaggage = false;
	private boolean hasFees = false;
	protected Passenger passenger;
	protected Flight passFlight;
	protected SimulationModel airport;
	public FlightSet flightList;
	static int counter;
	static int numOfDesks;
	AtomicBoolean mutex = new AtomicBoolean(false);
	protected ThreadPlanner threadPlanner ;
	protected TreeSet<ThreadPlanner.ThreadData> listOfThreads ;
	protected Logger log ;


	public Server(int id,ServerTypes type,SimulationModel airport, FlightSet flightList, ThreadPlanner tp,
			TreeSet<ThreadPlanner.ThreadData> tpL ) {
		typeOfServer = type ;
		cumCountOfServers ++ ;
		serverID = cumCountOfServers ;
		this.id = id;
		this.airport=airport;
		this.airportTime = TimeSim.getInstance(airport);
		this.log = Logger.getInstance();
//		this.checkInQueue = checkInQueue;
		this.flightList=flightList;
		this.threadPlanner = tp ;
		this.listOfThreads = tpL ;
	}

	private int getCounter() {
		return counter;
	}
	/**
	 * this is an abstract version of the run method present in every server
	 */
	public abstract void run();
	/**
	 * this is an abstract version of the getServerDetails method present in every server
	 */
	public abstract String getServerDetails();


	/**
	 * this method returns the ID of the server
	 * @return server ID
	 */
	public int getServerID() {
		return serverID ;
	}
	/**
	 * this method returns the type of the server
	 * @return typeOfServer
	 */
	public ServerTypes getType() {
		return typeOfServer;
	}

	/**
	 * @author group2
	 * @param targetTime - an Instant being the time this thread needs to take next
	 *                   action this method logs the required notify() time to the
	 *                   ThreadPlanner and puts the holding thread into wait().
	 */
	protected void notifyMeAt(Instant targetTime) {
		Boolean done = false;
		do {
			done = threadPlanner.wakeMeUpAt(targetTime);
			// if (!done) {System.out.println("*********************XXXXXXXXXXXXXX Boarding
			// FAILURE XXXXXXXXXXXXX***************************") ;}
			targetTime = targetTime.plusMillis(1);
		} while (!done);
		synchronized (Thread.currentThread()) {
			// System.out.println(commonClock.getSimpleTime() + " " +
			// Thread.currentThread().getName() + " is about to wait() until " +
			// targetTime);
			try {
				Thread.currentThread().wait();
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
			// System.out.println(commonClock.getSimpleTime() + " " +
			// Thread.currentThread().getName() + " moved on from wait()");
		}

	}
}
