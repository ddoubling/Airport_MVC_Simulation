import java.time.Duration;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.TreeSet;
import java.util.concurrent.atomic.AtomicInteger;
/**
 * 
 * @author group2
 * This class boards the passengers onto their respective flights
 */

public class ServerBoarding extends Server implements Runnable {

	Instant startClock;
	Instant endClock;
	Instant takeOffTime;
	public String name;
	int num;
	private boolean areCheckInDesksCreated = false;
	private final Duration thirtySeconds = Duration.of(30, ChronoUnit.SECONDS);
	private final Duration fiveMinutes = Duration.of(5 * 60L, ChronoUnit.SECONDS);
	private final Duration tenMinutes = Duration.of(10 * 60L, ChronoUnit.SECONDS);
	private final Duration fortyMinutes = Duration.of(40 * 60L, ChronoUnit.SECONDS);
//	private Logger boardingLog;
	private Boolean message1 = false;
	private Boolean message2 = false;
	private static CriticalRegion protectWaitingOps = new CriticalRegion("Protect waiting queue");

	public ServerBoarding(int id, ServerTypes type, SimulationModel airport, Instant clock, FlightSet allFlights,
			String flightCode, Queue<Passenger> departures, Queue<Flight> flightQueue, ThreadPlanner tp,
			TreeSet<ThreadPlanner.ThreadData> tpL) {

		super(id, type, airport, allFlights, tp, tpL);
		this.airport = airport;
//		this.allFlights = allFlights;
		name = flightCode;
		this.waiting = departures;
		this.flightQueue = flightQueue;
//		this.threadPlanner = tp;
//		this.listOfThreads = tpL;
		num = id;
		this.boardingQueue = new Queue<Passenger>(Queue.QueueTypes.BOARDING);
	}

	public synchronized void run() {
		synchronized (this) {
			String s;
			while (!airport.isFinished()) {
				// first code block assigns an available gate to the next departing flight
				if (name.equals("") && !flightQueue.queueIsEmpty()) {
					areCheckInDesksCreated = true;
					passFlight = flightQueue.deQueue();
					name = passFlight.getFlightCode();
					passFlight.setBoardingGate(String.valueOf(num+1));
					notifyMeAt(passFlight.getFlightTime().minus(fortyMinutes));
					passFlight.setFlightStatusBoarding();
				}
				// now we are within 40mins of departure time, passengers may queue to board
				if (!name.equals("")) {
					if (message1 == false) {
						s = airportTime.getSimpleTime() + " Departure Gate " + num + " is open for " + name
								+ " Passengers.";
						log.add(s);
						message1 = true;
					}
					Thread.currentThread().setPriority(Thread.NORM_PRIORITY);
					long timeGapCheckedIn = 1800 / (this.passFlight.getCheckedIn() + 10);
					do {
//						Duration  timeGapCheckedIn = Duration.between(airportTime.getTime(), f.getFlightTime() ).dividedBy(this.f.getCheckedIn() - this.boardingQueue.queueSize() );
						protectWaitingOps.enterRegion();
//						System.out.println("Departures = " + waiting.queueSize() + " " + (! waiting.queueIsEmpty()));
						if (!waiting.queueIsEmpty()) {
							if (waiting.spinFor(name)) {
								passenger = this.waiting.deQueue();
								this.boardingQueue.enQueue(passenger);
								s = airportTime.getSimpleTime() + " Departure gate " + num + ", Passenger "
										+ passenger.getPassengerName() + " has arrived for " + name + ".";
								log.add(s);
							}
						}
						protectWaitingOps.leaveRegion();
						notifyMeAt(airportTime.getTime().plus(Duration.of(timeGapCheckedIn, ChronoUnit.SECONDS)));
					} while (airportTime.getTime().isBefore(passFlight.getFlightTime().minus(tenMinutes)));
					// now allow passengers onto the plane
					if (message2 == false) {
						s = airportTime.getSimpleTime() + " Departure Gate " + num
								+ " is letting passengers onto plane " + name + ".";
						log.add(s);
//						System.out.println(s);
						message2 = true;
					}
					long timeGapBoarding = 600 / (boardingQueue.queueSize() + 10);
					while (!(this.boardingQueue.queueSize() == 0)) {
//						Duration  timeGapCheckedIn = Duration.between(airportTime.getTime(), f.getFlightTime() ).dividedBy(this.boardingQueue.queueSize() );
						Thread.currentThread().setPriority(Thread.NORM_PRIORITY + 2);
						passenger = boardingQueue.deQueue();
						passFlight.setBoarded(passFlight.getBoarded() + 1);
						s = airportTime.getSimpleTime() + " Passenger " + passenger.getPassengerFirstName()
								+ " has boarded plane " + passFlight.getFlightCode() + ".";
						log.add(s);
//						System.out.println( s ) ;
						passenger.nullPassengerDetails(); // set all details to null for gc
						passenger = null; // kill passenger for garbage collection
						// throw in small delay to make look more realistic
						notifyMeAt(airportTime.getTime().plus(Duration.of(timeGapBoarding, ChronoUnit.SECONDS)));
//						notifyMeAt(airportTime.getTime().plus( timeGapCheckedIn ));						
					}
					// this code section retains the gate for 5 minutes, to clean & reset the gate
					// area & retain departed message before releasing for next flight
					if (airportTime.getTime().isAfter(passFlight.getFlightTime()) && (!name.equals(""))) {
						this.passFlight.takeOff();
						s = airportTime.getSimpleTime() + " Flight " + name + " has taken off with "
								+ this.passFlight.getBoarded() + " passengers.";
//						System.out.println(s);
						log.add(s);
						notifyMeAt(airportTime.getTime().plus(fiveMinutes));
						message1 = false;
						message2 = false;
						name = ""; // reset the gate so another flight can grab it.
						Thread.currentThread().setPriority(Thread.NORM_PRIORITY - 2);
					}
				}
			}
		}
	}

	public String getServerDetails() {
		String report = "Boarding Gate: " + num + " is unallocated.";
		if (!(name == "")) {
			report = "Boarding Gate: " + num + "," + this.passFlight.getFlightCode() + ", to " + this.passFlight.getDestinationAirport()
					+ " Departs at " + this.passFlight.getFlightTime().toString().substring(11, 16) + ", Status =   "
					+ this.passFlight.getFlightStatus() + "\n";
			// report += this.f.getFlightCode() + " Status = " + this.f.getFlightStatus() +
			// "\n";
			if (this.passFlight.getFlightStatus().equals(Flight.Status.GATES_OPEN)) {
				report += "Checked in             " + this.passFlight.getCheckedIn() + "\n";
			}
			if (this.passFlight.getFlightStatus().equals(Flight.Status.BOARDING)) {
				report += "Checked in             " + this.passFlight.getCheckedIn() + "\n";
				report += "At this Boarding Gate  " + this.boardingQueue.queueSize() + "\n";
				if (airportTime.isAfter(this.passFlight.getFlightTime().minus(tenMinutes))) {
					report += "Boarded the Flight     " + this.passFlight.getBoarded() + "\n";
				}
			}
			if (this.passFlight.getFlightStatus().equals(Flight.Status.DEPARTED)) {
				report += "Checked in             " + this.passFlight.getCheckedIn() + "\n";
				report += "At this Boarding Gate  " + this.boardingQueue.queueSize() + "\n";
				report += "Boarded the Flight     " + this.passFlight.getBoarded() + "\n";
				report += name + " Has departed with " + this.passFlight.getBoarded() + " passengers.";
			}
		}
		return report;
	}

	public boolean getAreCheckInDesksCreated() {
		return areCheckInDesksCreated;
	}

	public String getName() {
		return name;
	}

	public void setName(String s) {
		name = s;
	}

	public Queue<Flight> getBoardingFlightQueue() {
		return flightQueue;
	}


	/**
	 * @author group2
	 *         example
	 */
	private static class CriticalRegion {
		// Declare and set lock to empty
		AtomicInteger thisCRatomicLock = new AtomicInteger(0);
		String nameOfCriticalRegion;

		// Constructor
		CriticalRegion(String name) {
			nameOfCriticalRegion = name;
		}

		private void enterRegion() {
			int lock;
			do {
				// Copy lock into register and set lock to 1
				lock = thisCRatomicLock.getAndSet(1);
				// Repeat loop if lock was 1
			} while (lock == 1);
			// Now lock has been set to 1 by caller, and caller has control of critical
			// region
			// System.out.println( Thread.currentThread().getName() + " has control of " +
			// nameOfCriticalRegion + " CR @" + System.currentTimeMillis() ) ;
		}

		private void leaveRegion() {
			// Release lock and release critical region
			thisCRatomicLock.set(0);
			// System.out.println( Thread.currentThread().getName() + " is releasing " +
			// nameOfCriticalRegion + " CR @" + System.currentTimeMillis() ) ;
		}

		// useful for debugging
		private int getLockStatus() {
			return thisCRatomicLock.get();
		}
	}
}
