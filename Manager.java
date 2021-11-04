/**
 * Manager class
 * 
 * @author group2
 *
 */
public class Manager {

	private BookingSet allBookings;
	private FlightSet allFlights;
	
	public Manager() {}
	
	/**
	 * Manager's run()
	 * instantiation of BookingSet, FlightSet, Logger, SimulationModel, SimulationView, SimulationControl
	 */
	public void run() {

		SimulationModel model = new SimulationModel();
		SimulationView view = new SimulationView(model);
		model.addObserver(view);
		SimulationControl controller = new SimulationControl(model, view);
	}
}
