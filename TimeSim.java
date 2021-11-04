import java.time.Duration;
import java.time.Instant;
import java.time.ZoneOffset;
import java.time.temporal.ChronoUnit;

/**
 * @author group2
 * SimTime increments an Instant (simulationTime) by 1 second, from a start to a finish Instant
 * It does this at a pace set by the timeAccel factor.
 * It will continuously .yield() until it is time for the clock to tick, to minimise processor time wasted 
 *
 */
public class TimeSim implements Runnable {
	private static Instant startTime = Instant.parse("2020-06-03T04:00:00.00Z") ;
	private static Instant endTime   = Instant.parse("2020-06-03T22:30:00.00Z") ; 
	private Instant simulationTime;
	private Long timeAccel = 60L ;
	private static TimeSim single_instance = null;
	private Logger clockLog ;
	private Duration clockIncrement ;
	private int timeInts[] = new int[4] ;
	private SimulationModel airport ;
	private Boolean paused = true ;
	private Boolean started = false ;
	private Instant realTimeStart ;
	private Instant realTimePaused ;
	private int fps = 6 ;
	private int guiUpdateTrigger = (int) (timeAccel / fps) ;


	/**
	 * TimeSim()
	 * @param m , a SimulationModel
	 * Constructor, sets up this TimeSim.
	 */
	private TimeSim( SimulationModel model ) {
		this.simulationTime = startTime ;
		this.clockLog = Logger.getInstance() ;
		this.clockIncrement = Duration.ZERO.plus(1, ChronoUnit.SECONDS) ;		
		this.airport = model ;
		this.paused= true;
		String log = getTime24() + " " + Thread.currentThread().getName() + " instantiated clock" ;
		System.out.println(log);
		clockLog.add(log );
	}

	/**
	 * getInstance()
	 * @param m, a SimulationModel
	 * @return the only instance of this class.
	 */
	public static TimeSim getInstance( SimulationModel m ) {
		if (single_instance == null) {
			single_instance = new TimeSim( m );
		}
		return single_instance;
	}

	/**
	 * run()
	 * The thread that operates the airport clock.
	 */
	public void run() {
		Instant targetSystTime ;
		Instant delayStart ;
		Instant delayEnd ;
		String log = getTime24() + " " + Thread.currentThread().getName() + " Thread started, paused= " + this.paused ;
		clockLog.add(log);
		System.out.println(log) ;
		Thread.currentThread().setPriority(Thread.MAX_PRIORITY);
		do {
			while ( paused ) {
				Thread.currentThread();
				Thread.yield();
			}
			if (started == false) {
				this.realTimeStart = Instant.now();
				started = true ;
			}
			delayStart = Instant.now() ;
			targetSystTime = delayStart.plus( clockIncrement.dividedBy( timeAccel ) ) ;
			// now cycle doing nothing until we have waited for a period of real time proportioned to accelerated time
			do {
				Thread.currentThread();
				Thread.yield() ;
			} while (Instant.now().isBefore( targetSystTime ) ) ;	
			simulationTime = simulationTime.plus( clockIncrement )  ;
			if( getSeconds() == 0 ) {
				System.out.println(getSimpleTime() ) ;
			}
			delayEnd = Instant.now();
			this.realTimePaused = delayEnd ;
		} while ( (! airport.isFinished()) && simulationTime.isBefore( endTime )  ) ;
	}

	/**
	 * getTime() 
	 * @author group2
	 * 
	 * @return an Instant, the current Time
	 */		
	public Instant getTime() {
		Instant t = simulationTime;
		return t ;
	}	

	/**
	 * isAfter(time) 
	 * @author group2
	 * @param an Instant to compare with
	 * @return a Boolean, true if time is after this clock
	 */		
	public Boolean isAfter(Instant time) {
		return this.simulationTime.isAfter( time);
	}

	/**
	 * getAccel()
	 * Returns current parameter of long that is used to accelerate time
	 * @author group2
	 * @return a long, how many simulation minutes passes in one second
	 */
	public Long getAccel() {
		return timeAccel;
	}


	/**
	 * simpleTime() 
	 * @author group2
	 * @return a String with simplified time HH:MM:SS stripping away dates and milliseconds
	 */		
	public String getSimpleTime() {
		return simulationTime.toString().substring(11, 19);
	}	


	/**
	 * userControlsSpeed() 
	 * @author group2
	 * @param speed, an integer representing how many seconds a SimTime second should represent.
	 */		
	public void userControlsSpeed(int speed) {
		timeAccel = (long) speed ; 
		guiUpdateTrigger = (int) (timeAccel / fps) ;
		if (guiUpdateTrigger == 0) {
			guiUpdateTrigger = 1 ;
		}
	}


	/**
	 * getHours() 
	 * @author group2
	 * @return an integer representing the hours in the simulationClock 
	 */		
	public int getHours() {
		return simulationTime.atZone(ZoneOffset.UTC).getHour() ;
	}	

	/**
	 * getMinutes() 
	 * @author group2
	 * @return an integer representing the minutes in the simulationClock 
	 */		
	public int getMinutes() {
		return simulationTime.atZone(ZoneOffset.UTC).getMinute() ;
	}

	/**
	 * getSeconds() 
	 * @author group2
	 * @return an integer representing the seconds in the simulationClock 
	 */		
	public int getSeconds() {
		return simulationTime.atZone(ZoneOffset.UTC).getSecond() ;
	}

	/**
	 * getMillis() 
	 * @author group2
	 * @return an integer representing the seconds in the simulationClock 
	 */		
	public int getMillis() {
		return simulationTime.atZone(ZoneOffset.UTC).getNano() * 1000000 ;
	}

	/**
	 * getTimeInInts() 
	 * @author group2
	 * @return an array of integers representing the hours, minutes & seconds in the simulationClock 
	 */		
	public int[] getTimeInInts() {
		timeInts[0] = getHours() ;
		timeInts[1] = getMinutes() ;
		timeInts[2] = getSeconds() ;
		timeInts[3] = getMillis() ;
		return timeInts ;
	}

	/**
	 * getTime24()
	 * @author group2
	 * @return a string of the hours, minutes, seconds components of time
	 */
	public String getTime24() {
		String time = "";
		int wholeHours = this.getHours();
		int minsLeft = this.getMinutes();
		int secleft = this.getSeconds();
		if (wholeHours <= 9)
			time += "0";
		time += wholeHours + ":";
		if (minsLeft <= 9)
			time += "0";
		time += minsLeft + ":";
		if(secleft <= 9)
			time += "0";
		time += secleft;
		return time;
	}

	/**
	 * pauseOn() 
	 * @author group2
	 * sets pause to true
	 */	
	public  void pauseOn() {
		this.paused = true ;
		String log = getSimpleTime() + " Clock paused.\n";
		System.out.println(log);
		clockLog.add(log); 
	}

	/**
	 * pauseOff() 
	 * @author group2
	 * sets pause to false
	 */	
	public  void pauseOff() {
		this.paused = false ;
		String log = "\n" + getSimpleTime() + " Clock unpaused.";
		System.out.println(log);
		clockLog.add(log); 
	}

	/**
	 * getRealTimeElapsed()
	 * @return the real time elapsed from when the clock was first started, to when it is stopped.
	 * Note the time returned will include any time spent paused (after the start of the clock).
	 */
	public Duration getRealTimeElapsed() {
		return Duration.between( realTimeStart , realTimePaused ) ;
	}

	/**
	 * getGUIfps()
	 * @return the number of times per second (real time) we intend to update the GUI
	 */
	public int getGUIfps() {
		return guiUpdateTrigger ;
	}

}