import java.time.Duration;
import java.time.Instant;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.concurrent.atomic.AtomicBoolean;

/**
 * @author group2
 * This class provides a simple queue, usually FIFO
 * However as this class has been equipped with concurrency protection this project
 * sometimes uses Queues for purposes where FIFO does not apply.
 * For example, a Check in Queue or a Boarding queue will obey FIFO rules.
 * However a pool of passengers in departures does not necessarily operate on a FIFO basis.
 * Because of this some Queue methods that are antithetical to FIFO are restricted in their use.
 * 
 */
public class Queue<Object> implements Iterable {

	private LinkedList<Object>  queue = new LinkedList <> ();
	public enum QueueTypes { TIMEGAPS , PASSENGERS4CHECKIN, LATE, DEPARTURES ,BOARDING, FLIGHTSTOBOARD } ; // can add SECURITY DUTYFREE BOARDING etc
	protected QueueTypes queueType ;
	private static int queueCount = 0 ;
	private int queueID  ;
	private int throughput ;
	private AtomicBoolean qMutex = new AtomicBoolean(false);	// MX protection for queue operations
	private AtomicBoolean rMutex = new AtomicBoolean(false);	// MX protection for scanning & rotating the queue

	/**
	 * Queue(style)
	 * @param style, the enum style of Queue to be constructed.
	 * Constructor, builds a queue.
	 */
	public Queue(QueueTypes style) {
		queueID = queueCount ;
		queueCount ++ ;
		queueType = style ;
		throughput = 0 ;
	}

	/**
	 * Queue()
	 * Empty constructor, for non specific (unconstrained) queues.
	 */
	public Queue() {
	}

	/**
	 * deQueue()
	 * @author group2
	 * @return item at the front of the queue
	 * also removes this item from the queue
	 */  
	public Object deQueue() {
		do {}while( ! qMutex.compareAndSet(false, true) );
		Object o = queue.removeFirst() ;
		throughput ++ ;
		qMutex.set(false);
		return o; 
	}

	/**
	 * enQueue()
	 * @author group2
	 * @param o, an object to be added at the end of the queue.
	 * adds an object at the back of the queue.
	 * 
	 */
	public void enQueue(Object p) {
		do {} while( ! qMutex.compareAndSet(false, true)) ;
		queue.addLast(p);
		qMutex.set(false);
	}

	/**
	 * queueSize()
	 * @author group2
	 * @return size of queue
	 */  
	public int queueSize() {
		do {}while( ! qMutex.compareAndSet(false, true) );
		int n = queue.size();
		qMutex.set(false);
		return n ;
	}
	
	/**
	 * getType()
	 * @return the type (enum) of this queue.
	 */
	public QueueTypes getType() {
		return queueType ;
	}

	/**
	 * queueLengthTime()
	 * @author group2
	 * @param Instant, time when method is called.
	 * @return Estimated length of time from back to front of queue
	 * The time to the front of the queue is estimated by taking the difference
	 * between the time the passenger at the front arrived and the time now, ie
	 * it returns how long the first passenger has been waiting.
	 * This takes no account of whether servers have been added or reduced 
	 * whilst this passenger has been waiting.
	 */  
	public synchronized Duration queueLengthTime(Instant clockNow) {
		Duration d = Duration.ZERO ;
		Instant timeFrontJoinedQueue = clockNow;
		if ( ( queue.size() > 3 ) ) {
			do {}while( ! qMutex.compareAndSet(false, true) );
			timeFrontJoinedQueue = ((Passenger) queue.getFirst() ).getTimeOfArrival();
			qMutex.set(false);
			d = Duration.between( timeFrontJoinedQueue , clockNow ).negated() ;
		}
		return d ;
	}	  



	/**
	 * queueisEmpty()
	 * @author group2
	 * @return true if queue is empty
	 */  
	public synchronized boolean queueIsEmpty() {
		do {}while( ! qMutex.compareAndSet(false, true) );
		boolean empty = queue.size() == 0;
		qMutex.set(false);
		return empty ;
	}


	/**
	 * iterator()
	 * @return Iterator<>
	 * Provides an iterator for this queue.
	 */
	@Override
	public synchronized Iterator<Object> iterator() {
		do {}while( ! qMutex.compareAndSet(false, true) );
		Iterator<Object> itO = queue.iterator();
		qMutex.set(false);
		return itO ;
	}
	
	/**
	 * getReport()
	 * @param typeOfReport
	 * @return a report on the contents of this queue.
	 * This report will return an empty String unless the queue's type is appropriate.
	 * If they are passengers it will provide 3 variants of the report, depending on whether
	 * the caller wants a report on a queue for CheckInDesks (1), SelfServiceCheckIn (3) or
	 * recently checked in via self service (2)  
	 */
	public  String getReport(int typeOfReport) {
		String report = "" ;
		synchronized( queue ) {
			if ((queueType == QueueTypes.PASSENGERS4CHECKIN ) | (queueType == QueueTypes.DEPARTURES )) {
				if (typeOfReport==1 ) {
					report +=  "ARRIVAL TIME  | FLIGHT NO.| PASSENGER'S NAME    | BAGGAGE\n";
				}
				else if(typeOfReport==2) {
					report += "Self Check Queue length = " + queue.size() + " todays throughput = " + getQueueThroughput() + "\n" ;
					report += "ARRIVAL TIME  | FLIGHT NO.| PASSENGER'S NAME    | STATUS\n";
				}
				else if(typeOfReport==3) {
					report+="Passengers trying to self check-in :\n";
				}
				while ( ! qMutex.compareAndSet(false, true) ) {} 
				if (! (queue.size() == 0 )) {
					for (Object p: queue ) {
						report += ((Passenger) p).getPassengerInformation((typeOfReport==1)) ;
					}
				}
				qMutex.set(false);
			} 
		}
		return report;
	}

	/**
	 * peekAtFrontOfQueue()
	 * @author group2
	 * @returns the first Object, without removing them
	 * 
	 */
	public synchronized Object peekAtFrontOfQueue() {
		do {}while( ! qMutex.compareAndSet(false, true) );
		Object o= null ;
		if ( ! (queue.size() == 0 )) {
			o = queue.getFirst();
		}
		qMutex.set(false);
		return o ;
	}

	/**
	 * peekAtFrontOfQueue()
	 * @author group2
	 * @returns the first Object, without removing them
	 * 
	 */
	public Object peekAtBackOfQueue() {
		do {}while( ! qMutex.compareAndSet(false, true) );
		Object o= null ;
		if ( ! queueIsEmpty() ) {
			o = queue.getLast();
		}
		qMutex.set(false);
		return o ;
	}


	/**
	 * reportQueueStatus()
	 * @author group2
	 * @param Instant clock, the time at which the report is called.
	 * @return a report on the queue status
	 * This is useful for debugging.
	 */
	public String reportQueueStatus(Instant clock) {
		do {}while( ! qMutex.compareAndSet(false, true) );
		String report = "------------- Current Status of Queue:"+ " : queueID = " + queueID + " ----------------------\n" ;
		report += "Time = " + clock.toString() ;
		report += "\nLength of Queue = " + this.queueSize() ;
		report += "\nTime to Front of Queue = " + this.queueLengthTime(clock) ;
		report += "\n------------------------------------------------------------------------------------------" ;
		qMutex.set(false);
		return report ;
	}

	/**
	 * getQueueID()
	 * @author group2
	 * @return the queueID
	 */
	public int getQueueID() {
		do {}while( ! qMutex.compareAndSet(false, true) );
		int identifier = queueID ;
		qMutex.set(false);
		return identifier ;
	}
	
	/**
	 * getQueueThroughput()
	 * @author group2
	 * @return the queue's throughput.
	 * This is a count of the people who have been deQueued from this queue.
	 */
	public int getQueueThroughput() {
		do {}while( ! qMutex.compareAndSet(false, true) );
		int n = throughput ;
		qMutex.set(false);
		return n ;
	}


	/**
	 * rotateQueue()
	 * @author group2
	 * @returns no return
	 * this method rotates the queue, not much use for Passengers, as it breaks FIFO. 
	 * However this is useful for non FIFO queues. 
	 * Only works for non FIFO queues!
	 */
	public void rotateQueue() {
		if( (this.queueType == Queue.QueueTypes.DEPARTURES) ) {
			do {}while( ! rMutex.compareAndSet(false, true) );
			Object o = deQueue() ;
			enQueue(o) ;
			rMutex.set(false);
		}
	}

	/**
	 * countByFlightInQueue(flightCode)
	 * @author group2
	 * @param String flightCode - a flight code we want to identify queueing passengers by
	 * @returns int count of passengers in queue waiting for flightCode
	 *  
	 */
	public int countByFlightInQueue( String code ) {
		do {}while( ! qMutex.compareAndSet(false, true) );
		int count = 0 ;
		for (Object o : this.queue ) {
			Passenger p = (Passenger) o ;
			if ( p.getPassengersFlight().equals( code ) ){
				count ++ ;
			}
		}
		qMutex.set(false);
		return count ;
	}
	
	/**
	 * spinFor(String flightCode)
	 * @author group2
	 * @param String flightCode - a flight code we want to identify queueing passengers by
	 * @returns Boolean, true if first Passenger in queue  matches the flight code, false if no match, null if this is not a FIFO queue.
	 * Will spin the entire queue looking for a match, returning false if no match in the entire queue.
	 * Note using this method destroys FIFO behavior of a queue, its use on FIFO queues will return null
	 *   
	 */
	public boolean spinFor(String flight) {
		Boolean matchAtFront = null ; 
		do {}while( ! rMutex.compareAndSet(false, true) );
		if ( this.queueType == QueueTypes.DEPARTURES) {
			matchAtFront = false ;
			int n = 0 ;
			do {
				n ++ ;
				Passenger p = (Passenger) queue.getFirst();
				if ( p.getPassengersFlight().equals( flight ) ) {
					matchAtFront = true ;
				}
				else {
					enQueue( deQueue() ) ;
				}
			} while ( (n < queue.size()) && (matchAtFront == false) ) ;
		}
		rMutex.set(false);
		return matchAtFront ;
	}
}

