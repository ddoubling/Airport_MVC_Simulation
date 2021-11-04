import java.time.Duration;
import java.time.Instant;
import java.util.TreeSet;
import java.util.concurrent.atomic.AtomicBoolean;

/**
 * @author group2 
 * This class manages threads, it allows threads to call its wakeMeUpAt(time) method to request a notify() signal at a specific time.
 * This class's runnable then monitors the first planned notify() vs the airport clock and issues that call when appropriate.
 * It is a a Parametric Singleton.
 */
public class ThreadPlanner implements Runnable { // , Comparable<ThreadData> 
	private static TreeSet<ThreadData> listOfThreads = new TreeSet<ThreadData>();
	private SimulationModel airport ;
	private TimeSim tpTime ;
	private Logger tpLog ;
	private static ThreadPlanner single_instance = null ;
	private static AtomicBoolean tPMutex = new AtomicBoolean(false);
	private int count ;
	private Boolean flag ;
	private Boolean cpuOverTasked = false ;
	private Long lag ;
	private Long maxLag = Duration.ZERO.toSeconds() ;

	private ThreadPlanner(SimulationModel model) {
		airport = model ;
		tpTime = TimeSim.getInstance( model ) ;
		this.count = 0 ;
		tpLog = Logger.getInstance() ;
		String s = tpTime.getSimpleTime() + " ThreadPlanner instantiated." ;
		tpLog.add( s ) ;
		System.out.println( s ) ;
	}

	
	/**
	 * getInstance()
	 * @param m , a SimulationModel
	 * @return the single instance of this class.
	 */
	public static ThreadPlanner getInstance( SimulationModel m ) {
		if (single_instance == null) {
			single_instance = new ThreadPlanner( m );
		}
		return single_instance;
	}

	/**
	 * getThreadList()
	 * @return the listOfThreads
	 * the list in which threads are held (with some meta data)
	 * until it is time to .notify() them. 
	 */
	public TreeSet<ThreadData> getThreadList() {
		return listOfThreads ;
	}


	/**
	 * @author group2 
	 * wakeMeUpAt()
	 * @param time, an Instant representing the airport time the thread wishes to be notified() at.
	 * This method creates some ThreadData (meta data) describing the thread calling it, and then
	 * stores that data in a treeset (ordered by time).
	 * 
	 */
	public Boolean wakeMeUpAt(Instant time) {
		//		System.out.println("Into ThreadPlanner wakeMeUpAt() ");
		while(! tPMutex.compareAndSet(false, true) ) {}
		Thread.currentThread();
		String tName = Thread.currentThread().getName();
		ThreadGroup tGroup = Thread.currentThread().getThreadGroup() ;
		Instant tTime = time ;
		ThreadData incomingThread = new ThreadData( tName, tGroup, tTime , Thread.currentThread() ) ;
		flag = false ;
		flag =	listOfThreads.add(incomingThread) ;
		tPMutex.set(false);
		count ++ ;
		return flag ;
	}


	/**
	 * @author group2 
	 * run()
	 * This run() looks at the earliest thread to be notified() and wakes it up when airport time has reached the right time.
	 * If there is a significant delay till next planned notify() then the thread will repeatedly .yield()
	 * 
	 */
	public void run() {
		synchronized(this ) {
			ThreadData nextThread ;
			System.out.println("ThreadPlanner thread is running ");
			while (! airport.isFinished()) {
				while (! listOfThreads.isEmpty()) {
					if (tpTime.getSeconds() == 0) {
					}
					while(! tPMutex.compareAndSet(false, true) ) {}
					nextThread = listOfThreads.first() ;
					if ( tpTime.getTime().isAfter( nextThread.targetTime )) {
						// work out if we are late (CPU doing too much) 
						lag = Duration.between(nextThread.targetTime, tpTime.getTime()).toSeconds() ;
						
						if ( lag.compareTo(maxLag) > 0 ) { maxLag = lag ;}
						if ( lag > 1) {
							cpuOverTasked = true ; 
							System.out.println( "lag = " + lag + " "  + (lag > 1) + " maxLag= " + maxLag);
						}
						else { cpuOverTasked = false ; }
						// now wake it up
						ThreadData threadDataInclThread = listOfThreads.pollFirst() ;
						Thread threadToBeNotified = threadDataInclThread.getThread() ;
						synchronized ( threadToBeNotified) {		// grab a lock on thread to be .notify()
							threadToBeNotified.notify();
						}
					}
					tPMutex.set(false);

					// this section yields this threads time slice, to avoid wasting core time waiting for next notify needed
					if ( ! listOfThreadsIsEmpty() ) {
						Duration systemTimeInHand ;
						do {
							while(! tPMutex.compareAndSet(false, true) ) {}
							Duration timeInHand = Duration.between(tpTime.getTime() , listOfThreads.first().getTargetTime()) ;
							tPMutex.set(false);
							systemTimeInHand = timeInHand.dividedBy(tpTime.getAccel()) ;
							if (systemTimeInHand.toMillis() > 10 ) { 	// ie convert to mS
									Thread.currentThread();
									Thread.yield();						// ie give up our time slice
							}
						} while (systemTimeInHand.toMillis() > 15);
					}
				}
			}
		}
	}


	/**
	 * compareTo()
	 * @param t , a thread.
	 * @return the compareTo indicator.
	 */
	public int compareTo(ThreadData t) {
		return this.compareTo( t ) ;
	}

	/**
	 * listOfThreadsIsEmpty()
	 * @return a boolean true if empty, false otherwise.
	 */
	private Boolean listOfThreadsIsEmpty() {
		Boolean isEmpty = false ;
		while(! tPMutex.compareAndSet(false, true) ) {}
		if (listOfThreads.size() == 0) {
			isEmpty = true ;
		}
		tPMutex.set(false);
		return isEmpty ;
	}
	
	/**
	 * getThreadWakeUpCallCount()
	 * @return the count of how many threads have been issued .notify()
	 */
	public int getThreadWakeUpCallCount() {
		return count ;
	}
	
	/**
	 * getCPUOverTasked()
	 * @return cpuOverTasked, a boolean flag indicating if it has not been possible to process
	 * all events within the time interval allowed.
	 */
	public Boolean getCPUoverTasked() {
		return cpuOverTasked ;
	}

	/**
	 * getMaxLag()
	 * @return a Long representing the longest lag experienced
	 * lag is when an .notify() was issued to a thread after the planned time,
	 * which can happen if the CPU is over tasked.
	 * Small lags are imperceptible by the user. the lag long represents 
	 * airport seconds elapsed.
	 */
	public Long getMaxLag() {
		return maxLag ;
	}
	
	/**
	 * ThreadData
	 * @author group2 
	 *This small class describes the meta data needed on a thread, 
	 *in order to control when to issue a .notify()
	 */
	public class ThreadData implements Comparable<ThreadData> {
		String name ;
		ThreadGroup group ;
		Instant targetTime ;
		Thread threadStored ;

		public ThreadData(String n, ThreadGroup g, Instant t, Thread threadWantingToWait) {
			synchronized(this) {
				this.name = n ;
				this.group = g ;
				this.targetTime = t ;
				this.threadStored = threadWantingToWait ;
			}
		}

		/**
		 * getTargetTime()
		 * @return the target time that this thread wishes to be .notify()
		 */
		public Instant getTargetTime() {
			return this.targetTime;
		}

		/**
		 * getThread()
		 * @return the thread
		 */
		public Thread getThread() {
			return this.threadStored ;
		}

		/**
		 * @author group2
		 * compareTo()
		 * Overriding this method to ensure ThreadData is stored in time order.
		 * 
		 */
		@Override
		public int compareTo(ThreadData td) {
			return this.targetTime.compareTo(td.getTargetTime());
		}

		/**
		 * getName()
		 * @return the name of this thread
		 */
		public String getName() {
			return name;
		}
		/**
		 * getGroup()
		 * @return the group this thread belongs to.
		 */
		public ThreadGroup getGroup() {
			return group;
		}
	}

}