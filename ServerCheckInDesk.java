import java.time.Duration;
import java.time.Instant;
import java.util.List;
import java.util.TreeSet;
import java.util.concurrent.atomic.AtomicBoolean;
import java.time.temporal.ChronoUnit;
/**
 * 
 * @author group2
 * contains the check-in desk object 
 * and the run method for the check-in desk thread
 */
public class ServerCheckInDesk extends Server implements Runnable {
	private boolean hasBaggage = false;
	private boolean hasFees = false;
	private double fee;
	
	private String deskInfo;
	private int emptyCount ;	
	private int arrivalCount ;
	private int lateCount  ;
	private int baggageCount  ;
	private int feesCount ;
	private int paidCount ;
	private int checkedinCount  ;

	public ServerCheckInDesk(int id, ServerTypes type, Stage stage, SimulationModel airport,
			Queue<Passenger> checkInQueue, Queue<Passenger> waiting, Queue<Passenger> lateArrivals, 
			FlightSet flightList, ThreadPlanner tp , TreeSet<ThreadPlanner.ThreadData> tpL ) {
		
		super(id, type, airport, flightList, tp, tpL);
		this.stage=stage;
		this.airport=airport;
		this.checkInQueue = checkInQueue;
//		this.flightList=flightList;
		this.waiting=waiting;
		this.lateArrivals=lateArrivals;
//		airportTime = TimeSim.getInstance( airport );
//		this.threadPlanner = tp ;
//		this.listOfThreads = tpL ;
	}

	public synchronized void run() {
		synchronized(this){	
			System.out.println(Thread.currentThread().getName() + " server open");
			log.add(airportTime.getSimpleTime() + " "  +  Thread.currentThread().getName() + " is open.");
			stage=empty;
			airport.notifyGUI();
			while (!airport.isFinished()) {	
				stage=empty;
				airport.notifyGUI();
				while(!mutex.compareAndSet(false, true)) {}
				checkInQueue=airport.getCheckInDeskQueue();
				if (!(checkInQueue.queueSize()==0)) {       
					passenger = (Passenger) checkInQueue.deQueue();
//					System.out.println(passenger.getBooking().getName().getFirstName() + " is at " + Thread.currentThread().getName());
					airport.setQueue(checkInQueue);
					stage=arrival;
//					System.out.println(Thread.currentThread().getName() + " sees a queue of " + airport.getCheckInDeskQueue().queueSize());
					mutex.set(false);
					hasBaggage = passenger.getBaggage();		
					String flightCode= passenger.getBooking().getBookingFlightCode();	
					passFlight = flightList.findByCode(flightCode);	
					hasFees=passFlight.checkCapacity(passenger.getBagVolume(), passenger.getBagWeight());
					boolean onTime = flightList.findByCode(passenger.getBooking().getBookingFlightCode()).canFlightCheckIn(airportTime.getTime());
					if (onTime == true) {
						if (hasBaggage == true ) {
							notifyMeAt(airportTime.getTime().plus(Duration.ofSeconds(30L))) ;
							stage=baggage;
//							System.out.println(passenger.getBooking().getName().getFirstName() + " has baggage");
						}
						else {
							notifyMeAt(airportTime.getTime().plus(Duration.ofMinutes(1L))) ;
							stage=baggage;
//							System.out.println(passenger.getBooking().getName().getFirstName() + " has no baggage");
						}
						if ( hasFees == true){	
							notifyMeAt(airportTime.getTime().plus(Duration.ofSeconds(30L))) ; 
							stage=fees;
							fee=passFlight.getCost(passenger.getBagVolume(),passenger.getBagWeight());
//							System.out.println(passenger.getBooking().getName().getFirstName() + " has fees");
							notifyMeAt(airportTime.getTime().plus(Duration.ofSeconds(30L))) ;
							stage=paid;
//							System.out.println(passenger.getBooking().getName().getFirstName() + " has paid an excess baggage fee of GBP" +fee+".");
							notifyMeAt(airportTime.getTime().plus(Duration.ofSeconds(30L))) ; 
						}
						else {
							notifyMeAt(airportTime.getTime().plus(Duration.ofSeconds(30L))) ; 
						}
						passenger.setCheckInStatus(true);
						flightList.findByCode(passenger.getBooking().getBookingFlightCode()).checkInConfirm(passenger.getBagWeight(), passenger.getBagVolume(),fee);
						stage=checkedin;
						synchronized ( waiting ) {
							waiting.enQueue(passenger);
							}
//						System.out.println(passenger.getBooking().getName().getFirstName() + " has checked in.");
						notifyMeAt(airportTime.getTime().plus(Duration.ofSeconds(30L))) ;
						stage=empty;
					}else {
						notifyMeAt(airportTime.getTime().plus(Duration.ofSeconds(30L))) ;
						stage = late;
						lateArrivals.enQueue(passenger);
						notifyMeAt(airportTime.getTime().plus(Duration.ofMinutes(1L))) ; 
						stage=empty;
					}
				}else {
					mutex.set(false);
					notifyMeAt(airportTime.getTime().plus(Duration.ofSeconds(10L))) ;
				}
			}
		}
	}

/**
 * 
 *  @author group2
 * 	the desk information based on the stage of the desk
 *  @return deskInfo
 */
	public String getServerDetails() {
		String passengerDetails= "";
		if(stage==empty && emptyCount==0) {
			emptyCount++;
			arrivalCount=0;
			lateCount=0;
			baggageCount=0;
			feesCount=0;
			paidCount=0;
			checkedinCount=0;
			passengerDetails =  "No passenger at this desk.";
			deskInfo=passengerDetails;
		}
		else if(stage==arrival && arrivalCount==0) {
			arrivalCount++ ;
			passengerDetails =  passenger.getBooking().getName().getFirstAndLastName() + " checking in for flight " + passenger.getBooking().getBookingFlightCode() ;
			log.add(airportTime.getSimpleTime() + " "  +  Thread.currentThread().getName() + "-" + passengerDetails);
			deskInfo=passengerDetails;
			deskInfo+="\n";
		}
		else if(stage==late && lateCount==0) {
			lateCount++;
			emptyCount=0;
			passengerDetails += passenger.getBooking().getName().getFirstName() + " arrived late and cannot check-in to flight " + passenger.getBooking().getBookingFlightCode();
			log.add(airportTime.getSimpleTime() + " " + passengerDetails);
			deskInfo+=passengerDetails;
			deskInfo+="\n";
		}
		else if(stage==baggage && baggageCount==0) {
			baggageCount++ ;
			if(passenger.getBaggage()==true) {
				passengerDetails =passenger.getBooking().getName().getFirstName() + " has a " +
						passenger.getBagType() + " bag, weighing " + passenger.getBagWeight() + "Kg.";
				log.add(airportTime.getSimpleTime() + " " +  Thread.currentThread().getName() + "-" + passengerDetails);
				deskInfo+=passengerDetails;
				deskInfo+="\n";
			}
			else {
				passengerDetails = passenger.getBooking().getName().getFirstName() + " has no baggage.";
				log.add(airportTime.getSimpleTime() + " " +  Thread.currentThread().getName() + "-" + passengerDetails);
				deskInfo+=passengerDetails;
				deskInfo+="\n";
			}	
		}
		else if(stage==fees && feesCount==0) {
			feesCount++; 
			if(!(fee==0.0)) {
				passengerDetails = passenger.getBooking().getName().getFirstName() + " has to pay baggage fee of GBP"  + flightList.findByCode(passenger.getBooking().getBookingFlightCode()).getCost(passenger.getBagVolume() , passenger.getBagWeight()) ;
				log.add(airportTime.getSimpleTime() + " " +  Thread.currentThread().getName() + "-" + passengerDetails);
				deskInfo+=passengerDetails;
				deskInfo+="\n";
			}
			else {
				passengerDetails = passenger.getBooking().getName().getFirstName()+ " has no excess fees to pay.";
				log.add(airportTime.getSimpleTime() + " " +  Thread.currentThread().getName() + "-" + passengerDetails);
				deskInfo+=passengerDetails;
				deskInfo+="\n";
			}
		}
		else if(stage==paid && paidCount==0) {
			paidCount++;
			passengerDetails =  passenger.getBooking().getName().getFirstName() + " has paid an excess baggage fee of GBP" +fee+"." ;
			log.add(airportTime.getSimpleTime() + " " +  Thread.currentThread().getName() + "-" + passengerDetails);
			deskInfo+=passengerDetails;
			deskInfo+="\n";
		}
		else if(stage==checkedin && checkedinCount==0) {
			checkedinCount++;
			emptyCount=0;
			passengerDetails =  passenger.getBooking().getName().getFirstName() + " has checked in." ;
			log.add(airportTime.getSimpleTime() + " " +  Thread.currentThread().getName() + "-" + passengerDetails);
			deskInfo+=passengerDetails;
			deskInfo+="\n";
		}
		return deskInfo;
	}
}	
