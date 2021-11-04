import java.time.Duration;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
/**
 * 
 * @author group2
 *This class keeps all the data related to a particular flight
 */
public class Flight implements Comparable<Object> {
	private String destinationAirport; // Stores the destination Airport
	private double weightCapacity; // Stores the total luggage capacity by weight
	private double volumeCapacity; // Stores total luggage capacity by volume
	private double volume;
	private double weight;
	private int numberOfPassengers; // Stores total max number of passengers
	private double remainingWeight; // Stores remaining weight capacity
	private double remainingVolume; // Stores remaining volume capacity
	private int checkedIn; // Stores number of passengers already checked in
	private String airline;
	private String flightCode;
	private String aircraft;
	private Instant flightTime;
	private double revenue; // Stores total earnings of flight for excess baggage
	private int boarded;
	// handy durations, for managing checkin window
	private final Duration thirtyMinutes = Duration.of(30 * 60L, ChronoUnit.SECONDS);
	private final Duration fourHours = Duration.of(4 * 60 * 60L, ChronoUnit.SECONDS);
	private final Duration fourtyMinutes = Duration.of(40 * 60L, ChronoUnit.SECONDS);
	private final Duration tenMinutes = Duration.of(10 * 60L, ChronoUnit.SECONDS);
	private Instant earliestTime;
	private Instant latestTime;
	private String boardingGate;

	public enum Status {
		WAITING, GATES_OPEN, BOARDING, DEPARTED
	};

	private Flight.Status waiting = Flight.Status.WAITING;
	private Flight.Status gatesOpen = Flight.Status.GATES_OPEN;
	private Flight.Status boarding = Flight.Status.BOARDING;
	private Flight.Status departed = Flight.Status.DEPARTED;
	private Status status;

	// Constructor for Flight class
	public Flight(double w, double v, int num, String dA, String airline, String code, String aircraft, Instant time) {
		weightCapacity = w;
		volumeCapacity = v * 1000000;
		numberOfPassengers = num;
		destinationAirport = dA;
		remainingWeight = weightCapacity; // Initialized as total weight
		remainingVolume = volumeCapacity; // Initialized as total volume
		weight = 0;
		volume = 0;
		checkedIn = 0;
		this.airline = airline;
		flightCode = code;
		this.aircraft = aircraft;
		flightTime = time;
		this.status = waiting;
		boardingGate = "-";

	}

	public Instant getFlightTime() {
		return flightTime;
	}

	public void setFlightTime(Instant t) {
		flightTime = t;
	}

	public String getAircraft() {
		return aircraft;
	}

	public void setAircraft(String air) {
		aircraft = air;
	}

	public String getFlightCode() {
		return flightCode;
	}

	public void setFlightCode(String s) {
		flightCode = s;
	}

	public String getAirline() {
		return airline;
	}

	public void setAirline(String s) {
		airline = s;
	}

//Sets the destination airport
	public void setDestinationAirport(String dA) {
		destinationAirport = dA;
	}

//Returns the destination Airport
	public String getDestinationAirport() {
		return destinationAirport;
	}

//Sets the total weight capacity
	public void setWeightCapacity(double wgt) {
		weightCapacity = wgt;
	}

//Gets the total weight capacity
	public double getWeightCapacity() {
		return weightCapacity;
	}

//Sets the total volume
	public void setVolume(int vol) {
		this.volumeCapacity = vol;
	}

//Gets the total volume
	public double getVolume() {
		return volumeCapacity;
	}

	public void setCurrentWeight(double wgt) {
		weight = wgt;
	}

	public double getCurrentWeight() {
		return weight;
	}

	public void setCurrentVolume(double vol) {
		volume = vol;
	}

	public double getCurrentVolume() {
		return volume;
	}

//Gets the remaining weight on the flight
	public double getRemainingWeight() {
		return remainingWeight;
	}

//Sets the remaining weight on the flight
	public void setRemainingWeight(double wgt) {
		remainingWeight = wgt;
	}

//Gets the remaining volume on the flight
	public double getRemainingVolume() {
		return remainingVolume;
	}

//Sets the remaining volume on the flight
	public void setRemainingVolume(double vol) {
		remainingVolume = vol;
	}

//Updates remaining weight and weight	
	public void addBaggage(double wgt, double vol) {
		weight = weight + wgt;
		remainingWeight = remainingWeight - wgt;
		volume = volume + vol;
		remainingVolume = remainingVolume - vol;
	}

//Sets the total number of passengers
	public void setNumberOfPassengers(int passenger) {
		numberOfPassengers = passenger;
	}

//Gets the total number of passengers
	public int getNumberOfPassengers() {
		return numberOfPassengers;
	}

//Gets the total number of passenegers checked in
	public int getCheckedIn() {
		return checkedIn;
	}

////Sets the total number of passengers checked in

	// Increments the number of passengers checked in.
	public void incrementCheckedIn() {
		checkedIn++;
	}

	public boolean checkCapacity(double vol, double wgt) {
		double ppweight = (double) weightCapacity / numberOfPassengers;
		double ppvolume = (double) volumeCapacity / numberOfPassengers;
		double wcost = (wgt - ppweight);
		double vcost = (vol - ppvolume);
		if (wcost <= 0 && vcost <= 0)
			return false;
		else
			return true;
	}

	public double getCost(double vol, double wgt) {

		double ppweight = (double) weightCapacity / numberOfPassengers;
		double ppvolume = (double) volumeCapacity / numberOfPassengers;
		double wcost = (wgt - ppweight) * 2;
		double vcost = (vol - ppvolume) * 1.5;
		if (vcost > 0 && wcost <= 0)
			return vcost;
		else if (wcost > 0 && vcost <= 0)
			return wcost;
		else
			return wcost + vcost;
	}

	public void checkInConfirm(double wgt, double vol, double cost) {
		remainingWeight = remainingWeight - wgt;
		remainingVolume = remainingVolume - vol;
		checkedIn++;
		weight = weight + wgt;
		volume = volume + vol;
		revenue = revenue + cost;
	}

	/**
	 * canFlightCheckIn()
	 * 
	 * @author group2
	 * @param time; the times between which a flight can be checked in.
	 * @return boolean flag indicating if checkin is open
	 * 
	 */
	public Boolean canFlightCheckIn(Instant time) {
//		earliestTime = time;
//		System.out.println(time.toString());
//		earliestTime = time.plus(30, ChronoUnit.MINUTES);
		earliestTime = time.plus(fourtyMinutes);
		latestTime = time.plus(fourHours);
		Instant fTime = this.getFlightTime();
		Boolean inTimeWindow = ((fTime.isAfter(earliestTime)) && (fTime.isBefore(latestTime)));
		return inTimeWindow;
	}

// Checks if flight can board i.e. 40 mins before take off	
	public Boolean canFlightBoard(Instant time) {
		earliestTime = time.plus(tenMinutes);
		latestTime = time.plus(fourtyMinutes);
		Instant fTime = this.getFlightTime();
		Boolean inTimeWindow = ((fTime.isAfter(earliestTime)) && (fTime.isBefore(latestTime)));
		return inTimeWindow;
	}

	@Override
	public int compareTo(Object o) {
		if (!(o instanceof Flight))
			return -1;
		else {
			Flight f = (Flight) o;
			return this.flightTime.compareTo(f.getFlightTime());
		}
		// return 0;
	}

	public void takeOff() {
		status = departed;
		boardingGate = "-";
	}

	public void setBoarded(int i) {
		boarded = i;
	}

	public int getBoarded() {
		return boarded;
	}

	public String getInfo() {
		String info = "";
		info += String.format("%-17s", getFlightTime().toString().substring(11, 19));
		info += String.format("%-14s", getFlightCode());
		info += String.format("%-20s", getAirline());
		info += String.format("%-15s", getFlightStatus());
		info += String.format("%-25s",getDestinationAirport());
		info += String.format(boardingGate);
		return info;
	}

	public Status getFlightStatus() {
		return status;
	}

	public void setFlightStatusGatesOpen() {
		this.status = gatesOpen;
	}

	public void setFlightStatusBoarding() {
		this.status = boarding;
	}

	public void setFlightStatusDeparted() {
		this.status = departed;
	}

	// Returns the total earnings of the flight for excess baggage
	public double getRevenue() {
		return revenue;
	}
	public void setBoardingGate(String s) {
		boardingGate = s;
	}
}
