import java.io.File;
import java.io.FileNotFoundException;
import java.text.SimpleDateFormat;
import java.time.Instant;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Scanner;
import java.util.TreeSet;
/**
 * 
 * @author group2
 * This class stores list of flights ordered by their departure time
 */
public class FlightSet implements Iterable {
	private TreeSet<Flight> flightList;
	private List<String> flightsCheckingIn = new LinkedList<String>();
	private File flights;

	public FlightSet() {
		flightList = new TreeSet<Flight>();
	}
/**
 * @author group2
 * @param fileSize
 * @throws FileNotFoundException
 * Loads the text file according to user selection
 */
	public void loadFile(String fileSize) throws FileNotFoundException {
		if (fileSize == "TEST") {
			flights = new File("flights file test.TXT");
		} else if (fileSize == "SMALL") {
			flights = new File("flights file small.TXT");
		} else if (fileSize == "LARGE") {
			flights = new File("flights file large.TXT");
		}
		Scanner sc = new Scanner(flights);
		int count = 1;
		while (sc.hasNextLine()) {
			if (count != 1) {
				try {
					String row = sc.nextLine();
					String[] flightData = row.split(",");

					flightData[0] = "2020-06-03T" + flightData[0] + ":00.00Z";
					Instant flightTime = Instant.parse(flightData[0]);

					String flightCode = flightData[1];
					String destination = flightData[2];
					String airline = flightData[3];
					String aircraft = flightData[4];
					int passengers;
					Double weight, volume;
					if (checkInt(flightData[5])) {
						passengers = Integer.parseInt(flightData[5]);
					} else {
						throw new Exception();

					}
					if (checkDouble(flightData[6])) {
						weight = Double.parseDouble(flightData[6]);
					} else {
						throw new Exception();
					}
					if (checkDouble(flightData[6])) {
						volume = Double.parseDouble(flightData[7]);
					} else {
						throw new Exception();
					}
					Flight f = new Flight(weight, volume, passengers, destination, airline, flightCode, aircraft,
							flightTime);
					getFlightList().add(f);

				} catch (Exception e) {
					System.out.println("flight Error on line number " + count);
				}
			} else {
				String s = sc.nextLine();
			}
			count++;
		}
		sc.close();
	}

	public boolean checkInt(String s) {
		try {
			int d = Integer.parseInt(s);
			return true;
		} catch (Exception e) {
			return false;
		}
	}

	public boolean checkDouble(String s) {
		try {
			double d = Double.parseDouble(s);
			return true;
		} catch (Exception e) {
			return false;
		}
	}
/**
 * @author group2
 * @param s
 * @return Flight object
 * Finds flight from the list by their flight code
 */
	public Flight findByCode(String s) {
		for (Flight f : flightList) {
			if (f.getFlightCode().equals(s))
				return f;
		}
		return null;
	}
/**
 * @author group2
 * @return String with flight details
 * This method provides the flights summary for the closing report
 */
	public String printReport() {
		String s = "Flight      Passengers          Boarded         Total Weight(kg)         Total Volume(m^3)              Earning(GBP)             Overloaded\n";
		for (Flight f : getFlightList()) {
			double weight = f.getCurrentWeight();
			double volume = f.getCurrentVolume() / 1000000;
			double totalVolume = f.getVolume() / 1000000;
			String over = "No";
			if (weight > f.getWeightCapacity() || volume > f.getVolume())
				over = "Yes";
			s += f.getFlightCode() + "\t" + f.getCheckedIn() + "\t  (" + f.getBoarded() + "/" + f.getNumberOfPassengers() + 
					")\t(" + weight + "/" + f.getWeightCapacity() + ")\t\t("
					+ volume + "/" + totalVolume + ") \t\t" + f.getRevenue() + "\t    " + over + "\n";
		}
		return s;

	}

	public String getFlights() {
		String s = "";
		for (Flight f : getFlightList()) {
			SimpleDateFormat formatter = new SimpleDateFormat("hh:mm");
			s += s + formatter.format(f.getFlightTime()) + "\t" + f.getFlightCode() + "\t" + f.getDestinationAirport()
					+ "\n";
		}
		return s;
	}

	/**
	 * getFlightsCheckingIn()
	 * 
	 * @author group2
	 * @param time of checking which flights are OK to check in
	 * @return a list of flights codes which can be checked in
	 * 
	 */
	public List<String> getFlightsCheckingIn(Instant time) {
		for (Flight f : getFlightList()) {
			if (f.canFlightCheckIn(time)) {
				if (!flightsCheckingIn.contains(f.getFlightCode())) {
					f.setFlightStatusGatesOpen();
					flightsCheckingIn.add(f.getFlightCode());
				}
			}
		}
		return flightsCheckingIn;
	}

	/**
	 * @return the flightList
	 */
	public TreeSet<Flight> getFlightList() {
		return flightList;
	}

	public int getSize() {
		return flightList.size();
	}

	@Override
	public Iterator<Flight> iterator() {
		return flightList.iterator();
	}

	public boolean noFlights() {
		return flightList.size() == 0;
	}

	public String findFlightInformation(String fCode) {
		String info = "";
		for (Flight f : flightList) {
			f = findByCode(fCode);
			info = f.getInfo();
		}
		return info;
	}

	public boolean allFlightsDeparted() {
		int count = 0;
		for (Flight f : flightList) {
			Flight.Status fStatus = f.getFlightStatus();
			if (fStatus != Flight.Status.DEPARTED) {
				count++;
			}
		}
		if (count == 0) {
			return true;
		}
		return false;
	}

	public int getTotalBoarded() {
		int totalBoarded = 0;
		for (Flight f : flightList) {
			totalBoarded += f.getBoarded();
		}
		return totalBoarded;
	}

	public Integer getCountByStatus(Flight.Status s) {
		int count = 0;
		for (Flight f : flightList) {
			if (f.getFlightStatus() == s)
				count++;
		}
		return count;
	}

	public String getAllFlightStatus() {
		String r = "";
		for (Flight.Status s : Flight.Status.values()) {
			int n = getCountByStatus(s);
			r += s.toString() + " " + n + " \t";
		}
		return r;
	}
}
