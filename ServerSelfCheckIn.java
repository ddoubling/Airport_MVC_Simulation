import java.time.Duration;
import java.time.Instant;
import java.util.TreeSet;

/**
 * ServerSelfCheckIn
 * @author group2
 * This class manages self service check in.
 *
 */
public class ServerSelfCheckIn extends Server implements Runnable {

	private Queue<String> recentArrivals ;
	static int counter;
	static int numOfDesks;
//	public enum Stage { EMPTY, ARRIVAL, LATE, BAGGAGE, FEES, PAID, CHECKEDIN } ;
//	private ServerSelfCheckIn.Stage empty = ServerSelfCheckIn.Stage.EMPTY ;	
//	private ServerSelfCheckIn.Stage arrival = ServerSelfCheckIn.Stage.ARRIVAL ;
//	private ServerSelfCheckIn.Stage late = ServerSelfCheckIn.Stage.LATE ;
//	private ServerSelfCheckIn.Stage checkedin = ServerSelfCheckIn.Stage.CHECKEDIN ;
//	Stage stage;
	private String deskInfo;
	private int checkedinCount  ;
	private Duration speedOfSelfCheckIn = Duration.ofMinutes(1L) ;	// a duration representing how long a passenger takes to self check in
	private long multipleGates = 1 ;		// a long representing how many self check in machines are available
	private int maxGates = 25 ;
	private Duration timeIncrement ;
	private boolean maxEffort ;
	private int n = 0 ;
	private String report ;

	/**
	 * ServerSelfCheckIn()
	 * @param id			- the unique ID of this server
	 * @param type			- the type of Server
	 * @param s				- the initial stage
	 * @param airport		- a SimulationModel
	 * @param passedQueue	- the queue this server will serve
	 * @param waiting		- the queue used to represent Departures
	 * @param lateArrivals	- a queue used to drop late arrivals into (for later reporting)
	 * @param flightList	- the list of flights passengers will check in for
	 * @param tp			- the ThreadPlanner this class's thread will use to plan its .wait()/.notify()
	 * 
	 * A Constructor of the SelfCheckIn.
	 */
	public ServerSelfCheckIn(int id, ServerTypes type, ServerSelfCheckIn.Stage s, SimulationModel airport, 
			Queue<Passenger> passedQueue, Queue<Passenger> waiting, Queue<Passenger> lateArrivals, FlightSet flightList, ThreadPlanner tp , TreeSet<ThreadPlanner.ThreadData> tpL ) {
		super(id, type, airport, flightList, tp, tpL);
		this.stage=s;
		this.airport=airport;
		this.selfCheckInQueue = passedQueue;
		this.lateArrivals=lateArrivals;
		this.waiting=waiting;
		this.recentArrivals = new Queue<String>() ;
//		this.flightList=flightList;
//		this.airportTime = TimeSim.getInstance( airport );
//		this.threadPlanner = tp ;
		if (airport.getModelSize().toString() == "SMALL") { 
			this.multipleGates = 10 ;
			this.maxGates = 100 ;
		}
		else if (airport.getModelSize().toString() == "LARGE") {
			this.multipleGates = 70 ;
			this.maxGates = 100 ;
		}
		this.checkedinCount = 0 ;
	}

	/**
	 * run()
	 * This is the runnable thread of SelfService Checkin.
	 */
	public synchronized void run() {
//		synchronized(this){	
			numOfDesks++;
			timeIncrement = speedOfSelfCheckIn.dividedBy( this.multipleGates ) ;
			report = airportTime.getSimpleTime() +Thread.currentThread().getName() + " server open";
			log.add(report);
			stage=empty;
			while (!airport.isFinished()) {	
				counter=getCounter();
				counter++;
				if(counter==numOfDesks) {
					counter=0;
				}
				stage=empty;
//				selfCheckInQueue = airport.getSCQueue();
				if (!(selfCheckInQueue.queueSize()==0)) {  //while waiting if there is no passenger at the front of the queue
					airport.notifyGUI() ;
					notifyMeAt( airportTime.getTime().plus( timeIncrement ) ) ;
					passenger = (Passenger) selfCheckInQueue.deQueue();
					
//					System.out.println(passenger.getBooking().getName().getFirstName() + " is at " + Thread.currentThread().getName());
//					airport.setSCQueue(selfCheckInQueue);
					stage=arrival;
					String flightCode= passenger.getBooking().getBookingFlightCode();	
					passFlight = flightList.findByCode(flightCode);	
					boolean onTime = flightList.findByCode(passenger.getBooking().getBookingFlightCode()).canFlightCheckIn(airportTime.getTime());
					if (onTime == true) {
						report = airportTime.getSimpleTime() + " Self Check In : " + passenger.getBooking().getName().getFirstAndLastName() 
								+ " is self checking in to flight " + passenger.getBooking().getBookingFlightCode();
						log.add( report );
						passenger.setCheckInStatus(true);
						passenger.setStatus(Passenger.passengerStatus.DEPARTURES);
						stage=checkedin;
						String code = passenger.getBooking().getBookingFlightCode();
						flightList.findByCode(code).checkInConfirm(0,0,0) ; 				// nb for self check in, cabin baggage only.
						recentArrivals.enQueue(passenger.getPassengerInformation(false));
						waiting.enQueue(passenger);
//						System.out.println(passenger.getBooking().getName().getFirstName() + " has Self checked in.");
						report = airport.getSimulationTime().getSimpleTime() + " Self Check In: " + passenger.getBooking().getName().getFirstAndLastName() + " has self checked in." ;
						log.add( report );

						stage=empty;
						if ( timeIncrement.toSeconds() < 1 )
							airport.notifyGUI() ;
					}
					else {
						notifyMeAt( airportTime.getTime().plus( timeIncrement ) ) ;
						stage = late;
						lateArrivals.enQueue(passenger);
						stage=empty;
					}
					
					if (selfCheckInQueue.queueSize() > 50)  { // Boost mode
						synchronized (waiting ) {	// grab the monitor, shared with ServerCheckInDesks & source of contention
							n = selfCheckInQueue.queueSize() ;
							for (int i=0;i<(n-5);i++) {
								multipleGates = maxGates;
								stage = arrival ;
								passenger = (Passenger) selfCheckInQueue.deQueue();
								report = airportTime.getSimpleTime() + " Self Check In : " + passenger.getBooking().getName().getFirstAndLastName() 
										+ " is self checking in to flight " + passenger.getBooking().getBookingFlightCode();
								log.add( report );
								passenger.setCheckInStatus(true);
								passenger.setStatus(Passenger.passengerStatus.DEPARTURES);
								stage=checkedin;
								String code = passenger.getBooking().getBookingFlightCode();
								flightList.findByCode(code).checkInConfirm(0,0,0) ; 				// nb for self check in, cabin baggage only.
								recentArrivals.enQueue(passenger.getPassengerInformation(false));
								waiting.enQueue(passenger);
								report = airport.getSimulationTime().getSimpleTime() + " Self Check In: " + passenger.getBooking().getName().getFirstAndLastName() + " has self checked in." ;
								log.add( report );
								stage=empty;
//								if(i%10 == 0 ) {airport.notifyGUI();}
							}
						}
					}
					// review & manage number of machines available. (ie time taken to process).
					checkedinCount ++ ;
					if ( (checkedinCount%5 == 0) && (-selfCheckInQueue.queueLengthTime(airport.getSimulationTime().getTime()).toMinutes() > 3 )  ) {
						this.multipleGates = Math.min( 1 + (int) (this.multipleGates * 1.3 ) , maxGates )  ; 
						timeIncrement = speedOfSelfCheckIn.dividedBy( this.multipleGates ) ;
					}
					if ( (checkedinCount%2 == 0) && ((-selfCheckInQueue.queueLengthTime(airport.getSimulationTime().getTime()).toMinutes() < 1 ) && ( this.multipleGates > 2))) {
						this.multipleGates--; 
						timeIncrement = speedOfSelfCheckIn.dividedBy( this.multipleGates ) ;
					}
						
				}
			}
//		} 
	}


	/**
	 * getServerDetails()
	 * Provides the GUI with an update of the status of SelfCheckIn.
	 */
	public String getServerDetails() {
		String passengerDetails= "";
		deskInfo = getRecentArrivals() ;
		deskInfo+= selfCheckInQueue.getReport(3);
		return deskInfo;
	}
	
	/**
	 * getNumGates() 
	 * @return the number of Self check in machines available
	 */
	private long getNumGates() {
		return this.multipleGates ;
	}



	/**
	 * getCounter()
	 * @return counter
	 */
	private int getCounter() {
		return counter;
	}

	/**
	 * getLateArrivals()
	 * @return the queue containing late arriving passengers.
	 */
	public Queue<Passenger> getLateArrivals() {
		return lateArrivals ;
	}
	/**
	 * getRecentArrivals()
	 * @return a String report detailing thestatus of SelfCheckIn
	 */
	private String getRecentArrivals() {
		String report="";
		report += "Self Check Queue length = " + selfCheckInQueue.queueSize() + ". Todays throughput = " + selfCheckInQueue.getQueueThroughput() + "\n" ;
		report += "No. Checkin Machines = " + getNumGates() + ". Est' time in queue = " 
				+ (selfCheckInQueue.queueLengthTime(airport.getSimulationTime().getTime()).toMinutes()*(-1))+ " minutes\n";
		report += "ARRIVAL TIME  | FLIGHT NO.| PASSENGER'S NAME    | STATUS\n";	
		if (recentArrivals.queueSize() > 5) {
			String s = recentArrivals.deQueue();
			s = null ;  	// for .gc()
		}
		for (Object a : recentArrivals) {
			report+=a.toString();
		}
		report += "------------------------------------------------------------\n";
		return report ;
	}
}	
