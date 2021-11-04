import java.time.Duration;

/**
 * 
 */

/**
 * @author group2
 * This class builds a list of inter arrival times to represent the gaps of time between
 * passengers arriving at the airport.
 * It could also be used to model other queue's as well, but in practice is only used for checkin.
 *
 */
public class ArrivalGaps {
	private static int numberOfGaps ;
	private static Duration overTime ;
	private Queue<Duration> durations;
	private static RandomNumbers random ; 



	public ArrivalGaps(int n, Duration lengthOfTime ) {
		numberOfGaps = n ;
		overTime = lengthOfTime ; 
		durations = new Queue<Duration>(Queue.QueueTypes.TIMEGAPS);
		random = RandomNumbers.getInstance();
		this.buildArrivalGaps() ;
	}

	/**
	 * getListOfGaps()
	 * @author group2
	 * @return the gaps
	 */
	public Queue<Duration> getListOfGaps() {
		return durations ;
	}


	/**
	 * @author group2
	 * @param gap, the gap to set
	 */
	public void setGap(Duration gap) {
		durations.enQueue( gap ) ;
	}

	/**
	 * buildArrivalGaps()
	 * @author group2
	 * 
	 * @return a list of Durations matching the number of Bookings
	 */
	private void buildArrivalGaps() {
		double numberOfIntervals = numberOfGaps ;
		double lengthOfDay = (int) overTime.getSeconds() ; // working day in seconds
		double passengerArrivalLambda = numberOfIntervals / lengthOfDay * 1000;
		double sumOfList =0;

		// Build a list of Poisson intervals		
		int[] randomIntervals = new int[(int) numberOfIntervals]  ;
		for (int k = 0  ; k < numberOfIntervals ; k++) {
			int randomSeconds =  poissonKnuth(passengerArrivalLambda) ;
			randomIntervals[k] = randomSeconds ;
			sumOfList = sumOfList + randomSeconds;
		}
		// Now we have a list of Poisson inter arrival times,
		// it needs to be scaled to our operating day,
		// ie sumOfList needs to scale to the working day.
		double timeScale = sumOfList / lengthOfDay ;
		for (int k = 0  ; k < numberOfIntervals ; k++) {
			double timeInterval = randomIntervals[k] / timeScale;  	// scale the timeInterval to Seconds through the working day
			int seconds = (int)timeInterval ;						// take Seconds
			int milliS = (int)((timeInterval - seconds)*1000) ;		// and milli Seconds
			Duration delay = Duration.ofSeconds(seconds) ;
			delay = delay.plusMillis(milliS) ;
			durations.enQueue(delay) ;
		}
	}

	/**
	 * size()
	 * @return the size of this list of durations
	 * 
	 */
	public int size() {
		return durations.queueSize();
	}


	/**
	 * poissonKnuth()
	 * @@author group2
	 * @param lambda, the mean of the pdf
	 * @return gives a poisson number
	 * N.B. Algorithm proposed by DE.Knuth
	 */
	private static int poissonKnuth(double lambda) {
		double L = Math.exp(-lambda);
		double p = 1.0;
		int k = 0 ;
		do {
			k++ ;
			Double r = random.getNextRandom() ;
			p = p*r ;
		} while (p > L) ;
		return (k - 1) ;	
	}

	/**
	 * @@author group2
	 * @return the gap to next arrival
	 */
	public Duration getGapToNextArrival() {
		return durations.deQueue() ;
	}

	/**
	 * peekAtNextGap()
	 * @@author group2
	 * @return the gap to next arrival without destroying it
	 * useful for debugging
	 */
	public Duration peekAtNextGap() {
		return durations.peekAtFrontOfQueue() ;
	}
}
