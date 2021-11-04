import java.awt.event.*;
import javax.swing.JOptionPane;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;

/**
 * SimulationControl is the control for the MCV pattern
 * @author group2
 *
 */
public class SimulationControl implements ActionListener{

	private SimulationModel model;
	private SimulationView view;
	private Boolean tf = true ;
	
	/**
	 * Constructor of SimulationControl
	 * @param model - obtains information through the model
	 * @param view - actions adjust SimulationView
	 */
	public SimulationControl(SimulationModel model, SimulationView view) {
		this.model = model;
		this.view = view;

		view.addActionListener(this);

		view.addChangeListenerToView(new ChangeListener() {
			public void stateChanged(ChangeEvent e) {
				int speed = 	view.getTimeSlider().getValue();
				model.setSpeedOfSim(speed);
			}
		}
				);	

		view.addToggleButtonListener(new ItemListener() {  
			public void itemStateChanged(ItemEvent itemEvent) { 
				int state = itemEvent.getStateChange();
				if (state == ItemEvent.SELECTED) { 
					model.pauseOn();
				} 
				else {
					model.pauseOff();
				} 
			}
		}
				);
	}

	/**
	 * actionPerformed()
	 * contains the instructions for the actions of buttons
	 */
	@Override
	public void actionPerformed(ActionEvent e) {
		String action = e.getActionCommand();

		if (action.contentEquals("Random Seed")) {
			System.out.println("GUIcontrol setting false");
			tf = false ;
			//				model.setSeed( false );
		}
		else if (action.contentEquals("Fixed Seed")) {
			System.out.println("GUIcontrol setting true");
			//			model.setSeed( true );
			tf = true ;
		}
		else if (action.contentEquals("Test Data")) {
			SimulationModel.setModelSize("TEST");
			System.out.println("DATASET: Testing");
		}
		else if (action.contentEquals("Small Data")) {
			SimulationModel.setModelSize("SMALL");
			System.out.println("DATASET: Small");
		}
		else if (action.contentEquals("Large Data")) {
			SimulationModel.setModelSize("LARGE");
			System.out.println("DATASET: Large");
		}
		else if(action.contentEquals("START")) {
			view.disableStartButton();

			Thread airportThread = new Thread(model);
			airportThread.setName("Airport Thread");
			airportThread.start();
			model.setSeed(tf) ;
			view.createSimulationFrame();
		}
		else if (action.contentEquals("FINISH")) {
			model.pauseOn();
			model.setFinished();
			view.simulationSummaryFrame();
		}
		else if (action.contentEquals("Exit")) {
			model.writeLog();
			JOptionPane.showMessageDialog(null, "Events are written to a log");		
			System.exit(0);
		}

	}
}


