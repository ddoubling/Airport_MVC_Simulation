import java.io.FileNotFoundException;
import java.time.Duration;
import java.time.Instant;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Observable;
import java.util.TreeSet;

/**
 * @author group2
 * This Class is the Model in an MCV design pattern, and uses Observable to make the information the GUI needs
 * to display visible to SimulationView. In line with MCV design pattern it is controlled by SimulationControl.
 *
 */
public class SimulationModel extends Observable implements Runnable { // implements Runnable {
	// lists & queues used to manage the aiport
	public static Queue<Passenger> checkInQueue;
	public static Queue<Passenger> selfCheckInQueue;
	public static Queue<Flight> flightQueue = new Queue<Flight>();
	private static List<String> currentFlightsThatCanCheckIn;
	private static Queue<Passenger> waitingInDepartures = new Queue<Passenger>(Queue.QueueTypes.DEPARTURES);
	private static Queue<Passenger> lateArrivals = new Queue<Passenger>(Queue.QueueTypes.LATE);
	private static TreeSet<ThreadPlanner.ThreadData> listOfThreads;
	private ArrayList<ThreadGroup> threadGroups;
	// input files
	private FlightSet allFlights;
	private BookingSet allBookings;
	private boolean finished = false;
	// Server Lists manage collation of information which is observable by GUIView
	private ServerList desks = new ServerList();
	private ServerList selfCheckIn = new ServerList();
	private ServerList flights = new ServerList();
	private Server selfServe;
	// Thread mgmt data
	private Thread[] desksThreads;
	private Thread[] extensionDesksThreads;
	private Thread[] boardingThreads; 
	private ThreadPlanner threadManager;
	private int maxRunnable = 0;
	private Instant peakTime;
	private int maxThreadsCounted = 0;
	private Instant peakThreadCountTime;
	private int notifyCount = 0;
	// Logger, the record of airport events
	private Logger log;
	// time based variables & constants
	private TimeSim clock;
	private Instant airportTime;
	private Instant startTime;
	private int timeInt[];
	private int hours = 0;
	private int minutes = 1;
	private int seconds = 2;
	private int millis = 3;
	// random numbers
	private Boolean seeded;
	private RandomNumbers rn;
	// model size 
	public static enum ModelSize {TEST, SMALL, LARGE }
	public static ModelSize modelSize = ModelSize.SMALL;
	private String bookingFileSize = "Small";
	private String flightFileSize = "Small";

	/**
	 * SimulationModel()
	 * @param allBookings
	 * @param allFlights
	 * @param log
	 * This is the Constructor of the model.
	 */
	public SimulationModel() {
		this.allBookings = new BookingSet();
		this.allFlights = new FlightSet();
		this.checkInQueue = new Queue<Passenger>(Queue.QueueTypes.PASSENGERS4CHECKIN);
		this.selfCheckInQueue = new Queue<Passenger>(Queue.QueueTypes.PASSENGERS4CHECKIN);
		this.clock = TimeSim.getInstance(this);
		this.threadManager = ThreadPlanner.getInstance(this);
		listOfThreads = threadManager.getThreadList(); // new TreeSet<ThreadPlanner.ThreadData>() ;
		this.threadGroups = new ArrayList<ThreadGroup>();
		this.log = Logger.getInstance();

	}

	/**
	 * setSeed()
	 * @param s, a boolean
	 * If called with parameter true, will fix the seed for the random number generator 
	 */
	public void setSeed(Boolean s) {
		System.out.println("setSeed has been called" + this.seeded + " ->" + s);
		this.seeded = s;
	}

	/**
	 * Informs check'in desks whether the airport is closed (finished) or not
	 * 
	 * @return boolean flag finished
	 * @author group2
	 */
	public boolean isFinished() {
		return finished;
	}

	/**
	 * Indicates whether the airport is closed for the day
	 * 
	 * @author group2
	 */
	public void setFinished() {
		finished = true;
	}

	/**
	 * a method checks the list holding check in desks and returns the number of check in desks open 
	 * @return number of check in desks open
	 */
	public int openCheckInDesks() {
		return desks.getSize();
	}

	/**
	 * a getter method that returns a list of bookings
	 * @return allBookings - a list of bookings
	 */
	public BookingSet getBookings() {
		return allBookings;
	}

	/**
	 * a method calls writeEventListToFile(String filename) from Logger class, which writes an array list of strings to a text file
	 */
	public void writeLog() {
		log.writeEventListToFile("Logger.TXT");
	}

	/**
	 * a method checks the list holding boarding servers and returns the number of boarding desks open 
	 * @return number of boarding desks open
	 */
	public ServerList getBoardingList() {
		return flights;
	}

	/**
	 * 
	 * @return a size of booking file that the user choosen
	 */
	public String getBookingFileSize() {
		return bookingFileSize;
	}

	/**
	 * This sets/selects the relevant model enum of size, for a given string input.
	 * @param newSize, a string indicating data set to use.
	 */
	public static void setModelSize(String newSize) {
		switch (newSize) {
		case "SMALL": {
			modelSize = ModelSize.SMALL;
			break;
		}
		case "LARGE": {
			modelSize = ModelSize.LARGE;
			break;
		}
		default: {
			modelSize = ModelSize.TEST;
		}
		}
	}

	/**
	 * getModelSize()
	 * @return the enum of the model size
	 */
	public ModelSize getModelSize() {
		return modelSize;
	}

	/**
	 * getModelSizeString()
	 * @return the enum of the model size as  camel case string.
	 */
	public String getModelSizeString() {
		String sizeOfModel = "Small";
		if (modelSize == ModelSize.SMALL) {
			sizeOfModel = "Small";
		} else if (modelSize == ModelSize.LARGE) {
			sizeOfModel = "Large";
		} else if (modelSize == ModelSize.TEST) {
			sizeOfModel = "Test";
		}
		return sizeOfModel;
	}

	/**
	 * The runnable thread of SimulationModel.
	 */
	public void run() {
		// load input files
		System.out.println(modelSize.toString());
		try {
			allBookings.loadBookings(modelSize.toString());
			allFlights.loadFile(modelSize.toString());
		} catch (FileNotFoundException fnf) {
			System.out.println("Could not load files");
		}
		setFlightQueue();

		ThreadGroup tSM = new ThreadGroup("0. SimulationModel");
		threadGroups.add(tSM);
		Thread.currentThread().setPriority(Thread.MAX_PRIORITY - 2);
		// initialise random numbers
		do {
		} while (seeded == null);
		rn = RandomNumbers.getInstance(seeded);
		// initialise start, end and current time
		airportTime = clock.getTime();
		startTime = airportTime;
		peakTime = startTime;
		ThreadGroup tc = new ThreadGroup("1. Clock");
		threadGroups.add(tc);
		Thread threadedTime = new Thread(tc, clock);
		threadedTime.setName("Common Clock");
		threadedTime.start();
		// set up thread management
		ThreadGroup tp = new ThreadGroup("2. Thread Manager");
		threadGroups.add(tp);
		Thread managerOfThreads = new Thread(tp, threadManager);
		managerOfThreads.setName("Thread Manager");
		managerOfThreads.setPriority(Thread.MAX_PRIORITY);
		// set up Arrivals
		ThreadGroup tas = new ThreadGroup("3. Arrivals+Supervisor");
		threadGroups.add(tas);
		tas.setMaxPriority(Thread.NORM_PRIORITY + 1);
		Thread arrivals = new Thread(tas, new ArrivalSupervisor(checkInQueue, selfCheckInQueue, this,
				ArrivalSupervisor.Type.ARRIVALS, startTime, allBookings, allFlights, threadManager, listOfThreads));
		arrivals.setName("ArrivalSupervisor");
		arrivals.setPriority(Thread.NORM_PRIORITY - 1);
		arrivals.start();
		try {
			arrivals.join(100);
		} catch (Exception e) {
			System.out.println(e);
		}
		currentFlightsThatCanCheckIn = allFlights.getFlightsCheckingIn(airportTime);
		// set up check in desks
		for (int i = 1; i <= 3; i++) {
			Server checkIn = new ServerCheckInDesk(i, Server.ServerTypes.CHECKIN, ServerCheckInDesk.Stage.EMPTY, this,
					checkInQueue, waitingInDepartures, lateArrivals, allFlights, threadManager, listOfThreads);
			desks.add(checkIn);
		}
		selfServe = new ServerSelfCheckIn(100, Server.ServerTypes.CHECKIN, ServerSelfCheckIn.Stage.EMPTY, this,
				selfCheckInQueue, waitingInDepartures, lateArrivals, allFlights, threadManager, listOfThreads);
		selfCheckIn.add(selfServe);
		ThreadGroup ts = new ThreadGroup("5. SelfCheckIn");
		threadGroups.add(ts);
		Thread selfCheckInT = new Thread(ts, selfServe);
		selfCheckInT.setName("Self Check In");
		selfCheckInT.setPriority(Thread.MAX_PRIORITY);
		instantiateBoarding();
		// assign checkin desks to threads
		desksThreads = new Thread[desks.getSize()];
		ThreadGroup td = new ThreadGroup("4. CheckIn");
		threadGroups.add(td);
		for (int i = 0; i < desks.getSize(); i++) {
			desksThreads[i] = new Thread(td, desks.get(i));
			desksThreads[i].setName("Check-in desk " + (i + 1));
			desksThreads[i].setPriority(Thread.NORM_PRIORITY-1);
			desksThreads[i].start();
			try {
				desksThreads[i].join(100);
			} catch (Exception e) {
				System.out.print(e);
			}
		}
		// start up Self Check In
		selfCheckInT.start();
		// various other instantiations & initialisations
		Boolean reported = false;
		Boolean testForEnd = false;
		Boolean GUIbeenNotified = false;
		extensionDesksThreads = new Thread[96];
		// now all instantiation is done, unpause the clock & start
		managerOfThreads.start();
		clock.pauseOff();
		do {
			airportTime = clock.getTime();
			timeInt = clock.getTimeInInts();
			if ( timeInt[millis] < 750) {
				Thread.currentThread();
				Thread.yield();
				}
			if ((timeInt[seconds] % clock.getGUIfps() == 0) && (GUIbeenNotified = false)) {
				notifyGUI();
				GUIbeenNotified = true;
			}
			if ((!(timeInt[seconds] % clock.getGUIfps() == 0)) && (GUIbeenNotified = false)) {
				GUIbeenNotified = false;
			}
			if ((timeInt[minutes] % 3 == 0) && (!reported)) {
				System.out.println(reportOnThreads());
				reported = true;
			}
			if (timeInt[minutes] % 3 != 0) {
				reported = false;
			}
			// check if there are any flights to board
			testForEnd = (allFlights.getCountByStatus(Flight.Status.DEPARTED).intValue() == +allFlights.getSize());
			if (testForEnd) {
				System.out.println(testForEnd);
				log.add("Airport Day has finished");
				this.setFinished();
			}
			if (((checkInQueue.queueSize() / desks.getSize()) * 5) > 30 && desks.getSize() < 100) {
				Server extensionCheckIn = new ServerCheckInDesk(desks.getSize() - 1, Server.ServerTypes.CHECKIN,
						ServerCheckInDesk.Stage.EMPTY, this, checkInQueue, waitingInDepartures, lateArrivals,
						allFlights, threadManager, listOfThreads);
				desks.add(extensionCheckIn);
				synchronized (td) { // get lock on td (ThreadGroup CheckIn Desks)
					extensionDesksThreads[desks.getSize() - 3] = new Thread(td, desks.get(desks.getSize() - 1));
					extensionDesksThreads[desks.getSize() - 3].setName("Check-in desk " + (desks.getSize()));
					extensionDesksThreads[desks.getSize() - 3].setPriority(Thread.NORM_PRIORITY);
					extensionDesksThreads[desks.getSize() - 3].start();
					try {
						extensionDesksThreads[desks.getSize() - 3].join(100);
					} catch (Exception e) {
						System.out.print(e);
					}
				}
			} 
		} while (!isFinished());
	}


	/**
	 *  getter for central clock
	 * @return a TimeSim - central clock of a simulation
	 */
	public TimeSim getSimulationTime() {
		return clock;
	}

	/**
	 *  pauses the cental clock/time stops running
	 */
	public void pauseOn() {
		clock.pauseOn();
	}

	/**
	 * unpauses central clock/ time resumes to running
	 */
	public void pauseOff() {
		clock.pauseOff();
	}

	/**
	 * 
	 * @return queue of people waiting to be processed by check in desks
	 */
	public Queue getCheckInDeskQueue() {
		return checkInQueue;
	}

	/**
	 * 
	 * @return queue of peple waiting to check in using self check-in services
	 */
	public Queue getSCQueue() {
		return selfCheckInQueue;
	}

	/**
	 * setQueue()
	 * @param q, the queue to set as checkInQueue
	 */
	public void setQueue(Queue q) {
		checkInQueue = q;
		setChanged();
		notifyObservers();
	}

	/**
	 * setSCQueue()
	 * @param q,the queue to set as selfCheckInQueue
	 */
	public void setSCQueue(Queue q) {
		selfCheckInQueue = q;
		setChanged();
		notifyObservers();
	}

	/**
	 * timeLengthOfQueue()
	 * @return String, with the estimated time taken to reach the front of the queue.
	 */
	public String timeLengthOfQueue() {
		Duration d = getCheckInDeskQueue().queueLengthTime(this.airportTime);
		String s = d.toHours() + "H" + d.toMinutesPart() + "M";
		return s;
	}

	/**
	 * getLates()
	 * @return a queue of passengers who arrived late
	 */
	public Queue getLates() {
		return lateArrivals;
	}

	/**
	 * notifyGUI()
	 */
	public void notifyGUI() {
		setChanged();
		notifyObservers();
		notifyCount++;
	}

	/**
	 * getCheckInDeskServerList()
	 * @return a ServerList, which holder check in desks open
	 */
	public ServerList getCheckInDeskServerList() {
		return desks;
	}

	/**
	 * getFlightSet()
	 * @return a total list of flights that will be processed throughout full simulation day
	 */
	public FlightSet getFlightSet() {
		return allFlights;
	}

	/**
	 * getSelfCheckInServer()
	 * @return a self check in server
	 */
	public Server getSelfCheckInServer() {
		return selfServe;
	}

	/**
	 * getFlightStatusReport()
	 * Creates and returns a String, which displays departure time, flight code, airlines, status, destination and gate at which flight will be boarding
	 * @return a String flightBoard, which contains information about flights in a table.
	 */
	public String getFlightStatusReport() {
		String flightBoard = "Total Flights " + allFlights.getSize() + ":\t" + allFlights.getAllFlightStatus();
		flightBoard += "\n_____________________________________________________________________________________________________________________________________\n";
		flightBoard += "| DEPATURE TIME | FLIGHT CODE |    AIRLINES     |    STATUS    |   DESTINATION      |	GATE\n";
		for (String next : allFlights.getFlightsCheckingIn(clock.getTime())) {
			flightBoard += allFlights.findFlightInformation(next) + "\n";
		}
		return flightBoard;
	}

	/**
	 * reportBookings()
	 * @return a String report of any remaining Bookings in the Booking Set. Report analysed by Flight.
	 * useful for debugging
	 */
	public String reportBookings() {
		String report = "";
		ArrayList<String> listAllFlights = new ArrayList<String>();
		ArrayList<Integer> countBookings = new ArrayList<Integer>(allFlights.getFlightList().size());
		Iterator<Flight> itF = allFlights.iterator();
		int previousCount = 0;
		while (itF.hasNext()) {
			Flight flight = itF.next();
			listAllFlights.add(flight.getFlightCode());
			countBookings.add(0);
		}
		for (Integer cb : countBookings) {
			countBookings.set(cb, 0);
		}
		Iterator<Bookings> itB = allBookings.iterator();
		synchronized (allBookings) {
			while (itB.hasNext()) {
				Bookings b = itB.next();
				String bookingsFlightCode = b.getBookingFlightCode();
				int index = listAllFlights.indexOf(bookingsFlightCode);
				if (index >= 0) {
					previousCount = countBookings.get(index);
					countBookings.set(index, previousCount + 1);
				}
			}
		}
		report += "\n\nSummarising Remaining Bookings by Flight";
		int totalFlights = 0;
		int totalBookings = 0;
		for (int index = 0; index < listAllFlights.size(); index++) {
			if (index % 10 == 0) {
				report += "\n ";
			}

			int n = countBookings.get(index);
			if (n > 0) {
				report += String.format("%1$7s", listAllFlights.get(index));
				report += String.format("%5d", countBookings.get(index)) + " : ";
			}
			totalFlights++;
			totalBookings += countBookings.get(index);
		}
		report += "\nTotal no Flights = " + totalFlights + " total remaining Bookings = " + totalBookings;
		return report;
	}

	/**
	 * setSpeedOfSim()
	 * @param speed
	 * Allows the time acceleration of the simulation to be changed.
	 */
	public void setSpeedOfSim(int speed) {
		clock.userControlsSpeed(speed);
	}

	/**
	 * getModel
	 * @return this SimulationModel
	 */
	public SimulationModel getModel() {
		return this;
	}

	/**
	 * haveFlightsStartedBoarding()
	 * @return a boolean flag.
	 * True if flights have started boarding.
	 */
	public boolean haveFlightsStartBoarding() {
		boolean checkInStarted = false;
		for (int i = 0; i < flights.getSize(); i++) {
			ServerBoarding flightBoard = (ServerBoarding) flights.get(i);
			if (!(checkInStarted == flightBoard.getAreCheckInDesksCreated())) {
				checkInStarted = flightBoard.getAreCheckInDesksCreated();
				break;
			}
		}
		return checkInStarted;

	}

	/**
	 * setFlightQueue()
	 * @author group2jasrajpethe
	 * Sets flight queue
	 */
	public void setFlightQueue() {
		for (Flight f : allFlights.getFlightList()) {
			flightQueue.enQueue(f);
		}
	}

	/**
	 * instantiateBoarding()
	 * @author group2
	 * Instantiates threads managing boarding of flights
	 */
	public void instantiateBoarding() {
		int n = 4;
		if (modelSize == ModelSize.LARGE)
			n = 9;
		for (int j = 0; j < n; j++) {
			ServerBoarding temp = new ServerBoarding(j, Server.ServerTypes.BOARDING, this, airportTime, allFlights, "",
					waitingInDepartures, flightQueue, threadManager, listOfThreads);
			flights.add(temp);
		}
		ThreadGroup bt = new ThreadGroup("6. BoardingGate");
		threadGroups.add(bt);
		boardingThreads = new Thread[n];
		for (int j = 0; j < n; j++) {
			boardingThreads[j] = new Thread(bt, flights.get(j));
			boardingThreads[j].setPriority(Thread.NORM_PRIORITY);
			boardingThreads[j].setName("Boarding desk no." + j);
			boardingThreads[j].start();
			try {
				boardingThreads[j].join(100);
			} catch (Exception e) {
				System.out.print(e);
			}
		}
	}

	/**
	 * getFlightServerList()
	 * @return a list of boarding servers
	 */
	public ServerList getFlightsServerList() {
		return flights;
	}

	/**
	 * getWaitingInDepartures()
	 * @return the queue representing passengers waiting in departures.
	 */
	public Queue<Passenger> getWaitinginDepartures() {
		return waitingInDepartures;
	}

	/**
	 * addThreadGroup
	 * @param tg
	 * adds a ThreadGroup to the list.
	 */
	public void addThreadGroup(ThreadGroup tg) {
		threadGroups.add(tg);
	}

	/**
	 * totalPassengersCheckedIn()
	 * @author group2
	 * @return int
	 * retrieves total number of passengers that have checked-in
	 */
	public int totalPassengersCheckedIn() {
		int totalPassengers = 0;
		for (Flight f : allFlights.getFlightList()) {
			totalPassengers += f.getCheckedIn();
		}
		return totalPassengers;
	}

	/**
	 * totalFlightsDeparted()
	 * @author group2
	 * @return int
	 * retrieves total flights with status of DEPARTED
	 */
	public int totalFlightsDeparted() {
		int fDeparted = 0;
		for (Flight f : allFlights.getFlightList()) {
			Flight.Status fStatus = f.getFlightStatus();
			if (fStatus == Flight.Status.DEPARTED) {
				fDeparted++;
			}
		}
		return fDeparted;
	}

	/**
	 * totalRunTime()
	 * @author group2
	 * @return Duration
	 * calculates runtime of simulation from when start button is clicked till finish.
	 */
	public Duration totalRunTime() {
		Instant pauseTime = clock.getTime();
		Duration difference = Duration.between(startTime, pauseTime);
		return difference;
	}

	/**
	 * converttDToHMS()
	 * @author group2
	 * @return String of the hours, minutes, seconds
	 */
	private String convertDToHMS(Duration gap) {
		String runtime = new String();
		long seconds = gap.toSecondsPart();
		long minutes = gap.toMinutesPart();
		long hours = gap.toHoursPart();
		if (hours <= 9)
			runtime += "0";
		runtime += hours + "H:";
		if (minutes <= 9)
			runtime += "0";
		runtime += minutes + "M:";
		if (seconds <= 9)
			runtime += "0";
		runtime += seconds + "S";
		return runtime;
	}

	/**
	 * reportOnThreads()
	 * @author group2
	 * @return a String.
	 * This method tells you the status & priority of threads by ThreadGroup, with totals.
	 * Note in the totals, Priority is the weighted average.
	 */
	public synchronized String reportOnThreads() {
		long start = System.nanoTime();
		int liveThreads = 0;
		int groupCount = 0;
		int[] totalStatus = new int[7];
		String report = "\n\n";
		report += " Thread Group           Qty    Priority                    Status\n";
		report += "No  Name                No.   Max    Avg     NEW RUNNABLE BLOCKED WAITING TIMED' TERMINATED\n";
		String underline = "\n------------------------------------------------------------------------------------------\n";
		report += underline;
		for (ThreadGroup tg : threadGroups) {
			String group = "";
			int tgCount = tg.activeCount();
			group = tg.getName();
			group += "               ".substring(0, (22 - group.length())) + String.format("%3d", tgCount); 
			group += "     " + String.format("%2d", tg.getMaxPriority()); 
			if (!tg.getName().toString().equals("0. SimulationModel") && (!(tgCount == 0))) {
				String d = "";
				int[] thisGroupStatus = getThreadGroupStates(tg);
				d += "    " + String.format("%4.1f", (double) (thisGroupStatus[6] / tgCount)) + ""; 
				totalStatus[6] += thisGroupStatus[6];
				for (int i = 0; i < 6; i++) {
					d += "     " + String.format("%3d", thisGroupStatus[i]);
					totalStatus[i] += thisGroupStatus[i];
				}
				group += d;
			}
			group += "\n";
			report += group;
			liveThreads += tgCount;
			groupCount++;
		}
		report += underline;
		String line = "Total Threads = ";
		line += "                            ".substring(0, (22 - line.length())) + String.format("%3d", liveThreads);
		line += "           " + String.format("%4.1f", (double) (totalStatus[6] / liveThreads));
		for (int i = 0; i < 6; i++) {
			line += "     " + String.format("%3d", totalStatus[i]);
		}
		report += line + "\n";
		if (liveThreads > maxThreadsCounted) {
			maxThreadsCounted = liveThreads;
			peakThreadCountTime = airportTime;
			report += "*** New maximum count of Threads seen = " + liveThreads + " @ "
					+ peakThreadCountTime.toString().substring(10) + " ***\n";
		}
		if (totalStatus[1] > maxRunnable) {
			maxRunnable = totalStatus[1];
			peakTime = airportTime;
			report += "*** New maximum of RUNNABLE threads set @ " + peakTime.toString().substring(10) + " ***\n";
		}
		int cores = Runtime.getRuntime().availableProcessors();
		double memory = Runtime.getRuntime().freeMemory() / 1024 / 1024;
		report += "Your PC has " + cores + " cores, the JVM has " + memory + "MB free memory.";
		long finish = System.nanoTime();
		report += " Report took " + String.format("%.1f", (double) (finish - start) / 1000000)
		+ " milli Seconds to prepare.\n";
		return report;
	}

	/**
	 * getThreadGroupStates() 
	 * @author group2
	 * @param a thread group
	 * @return an int[] giving the count of all possible thread status's. In
	 *         addition statusCounts[6] holds the cumulative Priority, summed over
	 *         all threads.
	 * This method tells you the status of all the threads in a given ThreadGroup    
	 */
	private int[] getThreadGroupStates(ThreadGroup tg) {
		int[] statusCounts = new int[7];
		synchronized (tg) {
			Thread[] group = new Thread[tg.activeCount()];
			int count = tg.enumerate(group);
			for (Thread t : group) {
				statusCounts[getThreadState(t)]++;
				statusCounts[6] += t.getPriority(); // use index 6 to store cumulative priority
			}
		}
		return statusCounts;
	}

	/**
	 * getThreadState()
	 * @author group2
	 * @param a thread
	 * @return an int indicating the status of the thread
	 *  This method tells you the status of any given thread
	 */
	private int getThreadState(Thread thisThread) {
		int newT = 0, runnable = 1, blocked = 2, waiting = 3, sleep = 4, terminated = 5;
		int status;
		switch (thisThread.getState()) {
		case NEW: {
			status = newT;
			break;
		}
		case RUNNABLE: {
			status = runnable;
			break;
		}
		case BLOCKED: {
			status = blocked;
			break;
		}
		case WAITING: {
			status = waiting;
			break;
		}
		case TIMED_WAITING: {
			status = sleep;
			break;
		}
		case TERMINATED: {
			status = terminated;
			break;
		}
		default: {
			status = 9;
		}
		}
		return status;
	}

	/**
	 * passengersInCheckIn()
	 * @return int - the number of passengers still at a check in desk.
	 */
	private int passengersInCheckIn() {
		int count = 0;
		for (int i = 0; i < desks.getSize(); i++) {
			String details = getCheckInDeskServerList().get(i).getServerDetails();
			if (!(details == "No passenger at this desk.")) {
				count++;
			}
		}
		return count;
	}

	/**
	 * closingReport()
	 * @author group2
	 * @return report - a String of all statistics about the airport simulation after it's finished running
	 * This method contains statistics about airport runtime, passengers passing through airport, flights information, general statistics on threads
	 */
	public String closingReport() {
		int avgTimeAccel = +(int) totalRunTime().dividedBy(clock.getRealTimeElapsed());
		Long worstLag = threadManager.getMaxLag();
		String report = "";
		report += " You chose to run the " + modelSize.toString() + " dataset, with ";
		if (seeded)
			report += "a fixed random seed\n\n";
		else
			report += "no fixed seed for the random number generator.\n\n";
		report += "TIME\n";
		report += "Simulation Runtime = " + convertDToHMS(totalRunTime()) + ".\n";
		report += "Real Time elapsed =  " + convertDToHMS(clock.getRealTimeElapsed())
		+ " n.b. includes any time spent paused\n";
		report += "Average Time Acceleration = " + avgTimeAccel + "x\n";
		report += "Screen Updated =" + notifyCount + "x\n";
		report += "\n";
		report += "PASSENGERS\n";
		report += "Passengers presenting from CheckIn Queue =                  " + checkInQueue.getQueueThroughput()
		+ "\n";
		report += "Passengers presenting for Self Check In =                         "
				+ selfCheckInQueue.getQueueThroughput() + "\n";
		report += "Passengers turned away as they did not checkin in time = " + lateArrivals.queueSize() + "\n";
		report += "Passengers still stood in the checkin queue =                    " + checkInQueue.queueSize() + "\n";
		report += "Passengers held at a Checkin desk  =                                " + passengersInCheckIn() + "\n";
		report += "Passengers still trying to self check in =                              "
				+ selfCheckInQueue.queueSize() + "\n";
		report += "                                                                                          -----\n";
		report += "Total passengers checked in =                                           "
				+ totalPassengersCheckedIn() + "\n";
		report += "Passengers still in departures= " + waitingInDepartures.queueSize() + "\n";
		report += "Passengers held at a Boarding Gate = " + "\n"; // need a report on Boarding Gate queues.

		report += "Passengers who left on a Flight = " + allFlights.getTotalBoarded() + "\n";
		report += "Passengers with Bookings who never Arrived = " + allBookings.size() + "\n";
		if (allBookings.size() == 0) {
			report += "All Passengers with a booking arrived at the airport";
		} else {
			report += reportBookings();
		}
		report += "\n\n";
		report += "FLIGHTS\n";
		report += allFlights.printReport();
		report += "Total number of flights departed = " + totalFlightsDeparted() + "\n";
		report += "\n";
		report += "THREADS\n";
		report += "Total number of .notify() handled  = " + threadManager.getThreadWakeUpCallCount() + "\n";
		report += "At " + peakTime.toString().substring(10) + " we saw the maximum number of RUNNABLE threads :  "
				+ maxRunnable + "\n";
		report += "At " + peakThreadCountTime.toString().substring(10)
				+ " we saw the peak number of Threads (all states) :       " + maxThreadsCounted + "\n";
		report += "Worst lag during run  = " + worstLag + " Airport seconds. Equates to approx "
				+ (worstLag / avgTimeAccel) + " second real time lag.";
		return report;
	}
}