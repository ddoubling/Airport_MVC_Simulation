import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.GridLayout;
import java.awt.event.ActionListener;
import java.awt.event.ItemListener;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.util.Hashtable;
import java.util.Observable;
import java.util.Observer;
import javax.swing.BorderFactory;
import javax.swing.BoxLayout;
import javax.swing.ButtonGroup;
import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JRadioButton;
import javax.swing.JScrollPane;
import javax.swing.JSlider;
import javax.swing.JTextArea;
import javax.swing.JTextField;
import javax.swing.JToggleButton;
import javax.swing.event.ChangeListener;

/**
 * SimulationView class for creating the stage 2 GUI, iteration 1
 * @author group2
 *
 */
@SuppressWarnings({ "deprecation", "serial" })
public class SimulationView extends JFrame implements Observer{

	private SimulationModel model;

	private JFrame mainFrame;
	private JFrame summaryFrame;

	private JPanel checkinDeskDisplay;
	private JPanel boardingDeskDisplay;
	private JPanel introPanel;
	private JPanel openingButtonsPane; 
	private JPanel selfCheckinPanel;
	private JPanel queueAndAirportCheckinPane;

	private JTextArea flightStatusBoard;

	private JScrollPane deskScrollPane;
	private JScrollPane boardingDeskScrollPane;
	private JScrollPane selfCheckInScrollPane;


	private JTextField simulationSpeed;
	private JTextField datasetSize;

	private JTextArea [] checkinDesks;
	private JTextArea [] boardingDesks;
	private JTextArea checkinQueueSize;
	private JTextArea listOfPeopleInQueue;
	private JTextArea rules;
	private JTextArea selfCheckInServerPane;

	private ButtonGroup randomSeedButtons;
	private ButtonGroup datasetSizeButtons;

	private JButton startSimulation = new JButton("START AIRPORT");
	private JButton finishSimulation = new JButton("FINISH SIMULATION");
	private JButton exitSimulation = new JButton("Exit");

	private JToggleButton pauseSimulation = new JToggleButton("PAUSE CLOCK");

	private JRadioButton setRandSeedZero;
	private JRadioButton setRandSeedOne;
	private JRadioButton selectTestDataset;
	private JRadioButton selectSmallDataset;
	private JRadioButton selectLargeDataset;

	private int flightsBoarding;

	private boolean isSummaryWindowOn;

	private String datasetInformation;
	private String selfCheckInServerDetails = "";
	private String passengerAtSelfCheckIn;
	private String updatedPassengerAtSelfCheckIn = "";

	private TimeAnalog analogClock = new TimeAnalog();
	private TimeDigital digitalClock = new TimeDigital();
	private JSlider timeFlowCtrl = new JSlider(JSlider.HORIZONTAL, 1, 600, 60);


	/**
	 * @author group2
	 * @author group2
	 * @param model - MCV design pattern, view has access to a model
	 */
	@SuppressWarnings({ "unchecked", "deprecation" })
	public SimulationView(SimulationModel model) {
		this.model = model;
		model.addObserver(this);
		if(model.getModelSizeString() == "Test" || model.getModelSizeString() == "Small")
			flightsBoarding = 4;
		else
			flightsBoarding = 9;
		JFrame.setDefaultLookAndFeelDecorated(true);
		mainFrame =  new JFrame("Welcome to Edinburgh Airport!");
		mainFrame.setDefaultCloseOperation(JFrame.DO_NOTHING_ON_CLOSE);
		mainFrame.addWindowListener(new WindowAdapter() {
			@Override
			public void windowClosing(WindowEvent e) {
				model.pauseOn();
				int userResult = JOptionPane.showConfirmDialog(mainFrame, "Are you sure you want to exit? As you close the software, the event log will be written into 'Logger.txt' file");
				if (userResult == JOptionPane.OK_OPTION) {
					mainFrame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
					model.writeLog();
					System.exit(0);

				}
				else if (userResult == JOptionPane.CANCEL_OPTION) {
					model.pauseOff();
				}
				else if (userResult == JOptionPane.NO_OPTION) {
					model.pauseOff();
				}
			}
		});
		mainFrame.setExtendedState(getExtendedState() | JFrame.MAXIMIZED_BOTH);
		mainFrame.setLocationRelativeTo(null);
		mainFrame.setLayout(new GridLayout(3,0));

		mainFrame.add(introductionPanel());
		mainFrame.add(openingButtonsPane());
		//		mainFrame.add(fileButtonPanel());

		startSimulation.setPreferredSize(new Dimension(200, 100));
		startSimulation.setBackground(Color.green);
		startSimulation.setActionCommand("START");
		
		mainFrame.add(startSimulation); 

		mainFrame.setVisible(true); 
	}
	
	/**
	 * populates the main simulation frame with passengers queue, check-in and boarding related panels
	 */
	public void createSimulationFrame() {
		mainFrame.remove(startSimulation);	
		mainFrame.remove(setRandSeedOne);
		mainFrame.remove(setRandSeedZero);
		mainFrame.remove(openingButtonsPane);
		mainFrame.remove(introPanel);
		mainFrame.add(clockPanel());
		mainFrame.add(sliderPanel());
		mainFrame.add(queueAndSelfCheckIn());
		mainFrame.add(checkInDeskDisplay());
		mainFrame.add(flightStatusDisplay());	
		mainFrame.add(boardingDeskDisplay());
	}

	/**
	 * creates the summary statistics frame with panel containing details of descriptive statistics of airport simulation
	 */
	public void simulationSummaryFrame() {
		summaryFrame = new JFrame("SUMMARY STATISTICS");
		summaryFrame.setLayout(new BorderLayout());
		JPanel sumReport = new JPanel();
		JLabel title = new JLabel("DESCRIPTIVE STATISTICS FOR AIRPORT SIMULATION");
		title.setFont(new Font(Font.MONOSPACED,Font.BOLD,20));	
		JTextArea descriptiveInfo = new JTextArea(model.closingReport());
		descriptiveInfo.setFont(new Font(Font.SANS_SERIF, Font.PLAIN,14));
		descriptiveInfo.setEditable(false);
		descriptiveInfo.setBackground(Color.LIGHT_GRAY);
		descriptiveInfo.setMaximumSize(new Dimension(Integer.MAX_VALUE, descriptiveInfo.getMinimumSize().height));
		JScrollPane scroll = new JScrollPane(descriptiveInfo);
		scroll.setPreferredSize(new Dimension(650, 500));

		sumReport.add(title);
		sumReport.add(scroll);
		sumReport.setBackground(Color.LIGHT_GRAY);

		exitSimulation.setBackground(Color.RED);
		exitSimulation.setActionCommand("Exit");
		summaryFrame.setLocation(600,150);
		summaryFrame.setSize(700,650);
		summaryFrame.setVisible(true);
		summaryFrame.add(BorderLayout.CENTER, sumReport);
		summaryFrame.add(BorderLayout.PAGE_END, exitSimulation);
	}

	/**
	 * returns two button groups, first button group gives user choice to select simulation to be random or not
	 * other button group allows users to choose dataset size
	 * @return openingButtonsPane - a JPanel containing radio buttons for random seed and file size
	 */
	public JPanel openingButtonsPane() {
		openingButtonsPane =  new JPanel();
		openingButtonsPane.setLayout(new GridLayout(2, 0));

		setRandSeedZero = new JRadioButton("Random Seed", false);
		setRandSeedZero.setActionCommand("Random Seed");

		setRandSeedOne = new JRadioButton("Fixed Seed", true);
		setRandSeedOne.setActionCommand("Fixed Seed");

		randomSeedButtons = new ButtonGroup();
		randomSeedButtons.add(setRandSeedZero);
		randomSeedButtons.add(setRandSeedOne);

		selectTestDataset = new JRadioButton("Testing dataset", false);
		selectTestDataset.setActionCommand("Test Data");
		selectSmallDataset = new JRadioButton("Small dataset", true);
		selectSmallDataset.setActionCommand("Small Data");
		selectLargeDataset = new JRadioButton("Large dataset", false);
		selectLargeDataset.setActionCommand("Large Data");

		datasetSizeButtons = new ButtonGroup();
		datasetSizeButtons.add(selectTestDataset);
		datasetSizeButtons.add(selectSmallDataset);
		datasetSizeButtons.add(selectLargeDataset);

		JPanel fileSizeButtonPane = new JPanel();
		fileSizeButtonPane.add(selectTestDataset);
		fileSizeButtonPane.add(selectSmallDataset);
		fileSizeButtonPane.add(selectLargeDataset);
		JPanel randButtonPane = new JPanel();
		randButtonPane.add(setRandSeedZero);
		randButtonPane.add(setRandSeedOne);
		//randButtonPane.add(fileButtonPanel());
		openingButtonsPane.add(fileSizeButtonPane);
		openingButtonsPane.add(randButtonPane);
		return openingButtonsPane;
	}

	/**
	 * time panel containing analog clock, digital clock, pause button, finish button
	 * @return timePanel - a JPanel containing digital and analog clocks, as well as button that pause or finish the simulation
	 */
	public JPanel clockPanel() {
		JPanel clockPanel =  new JPanel();
		clockPanel.setLayout(new BoxLayout(clockPanel, BoxLayout.X_AXIS));
		
		pauseSimulation.setBackground(Color.ORANGE);
		pauseSimulation.setActionCommand("PAUSE CLOCK");

//		finishSimulation = new JButton("FINISH SIMULATION");
		finishSimulation.setActionCommand("FINISH");
		finishSimulation.setBackground(Color.CYAN);
		
		clockPanel.add(analogClock);
		clockPanel.add(digitalClock);
		clockPanel.add(pauseSimulation);
		clockPanel.add(finishSimulation);
		return clockPanel;
	}

	/**
	 * the method creates and returns a JPanel that is displayed when GUI is first displayed
	 * @return introPanel - a JPanel that greets the user and contains JTextArea about the rules of simulation
	 */
	@SuppressWarnings("unchecked")
	public JPanel introductionPanel() {
		introPanel = new JPanel();
		introPanel.setLayout(new BoxLayout(introPanel, BoxLayout.Y_AXIS));
		JTextArea greeting = new JTextArea("Welcome to Edinburgh Airport!");
		greeting.setFont(new Font(Font.SANS_SERIF, Font.BOLD, 30));
		greeting.setEditable(false);
		introPanel.add(greeting);
		rules = new JTextArea("The airport will run once the green START AIRPORT button has been clicked.\n"
				+ "You may choose between a randomised airport simulation or one with a fixed seed for the random generator.\n"
				 + "There are 3 different input files :\n"
				 + "	a) An extremely small test dataset, which will allow you to see how the airport works, but which will not challenge your hardware, or our concurrent programming.\n"
				 + "	b) A small dataset, representing 30 flights over 8 hours. If your PC can run this at 600x real time, then it should take c40 seconds to run.\n"
				 + "	c) A large dataset, of 108 flights and 20,000 passengers over a full working day, flights & passengers are representative of a real day at Edinburgh Airport.\n"
				 + "Once the airport is running you can pause/unpause the time as well as speed it up or slow it down.\n"
				 + "On Exit, or Finish, the program will give you an option to write events to the Logger.TXT file.");  
		rules.setEditable(false);
		rules.setFont(new Font("SansSerif", Font.PLAIN, 22));
		rules.setForeground(Color.DARK_GRAY);
		introPanel.add(rules);
		introPanel.setSize(100, 50);
		return introPanel;
	}

	/**
	 * creates and returns a panel that contains information about simulation speed, size of a chosen dataset
	 * and a slider that allows user to control speed of the simulation
	 * @return sliderPanel - a JPanel that contains JTextArea containing simulation rules, dataset information and a JSlider that allows user to manipulate the speed of simulation
	 */
	public JPanel sliderPanel() {
		JPanel sliderPanel = new JPanel();
		sliderPanel.setLayout(new BoxLayout(sliderPanel,BoxLayout.Y_AXIS));
		String rules = "The rules of this airport:\n"
				+ "1. Opening time is 04:00, closing time varies by dataset and matches the time of the last flight.\n"
				+ "2. Passengers can arrive from 4 hours before their flight until no later than forty minutes before their flight.\n"
				+ "3. Passengers can check in fom 4 hours before their flight till 40 minutes before flight departure.\n"
				+ "4. Boarding Gates are announced 40 minutes prior to departure.\n"
				+ "5. Passengers may board their flight from 10 minutes before departure time." ;
		JTextArea operations = new JTextArea( rules );
		operations.setFont(new Font(Font.SANS_SERIF, Font.BOLD, 15));
		operations.setEditable(false);
		sliderPanel.add(operations);
		
		datasetInformation = "Size of the dataset: " + model.getBookingFileSize() + 
				"				It contains " + model.getBookings().size() + " bookings and " + model.getFlightSet().getSize() + " flights";
		datasetSize = new JTextField(datasetInformation);
		datasetSize.setEditable(false);
		sliderPanel.add(datasetSize);
		
		long accelSettingLong = model.getSimulationTime().getAccel();
		int accelSetting = (int) accelSettingLong;
		String speedText = "Acceleration rate is " + accelSetting;
		simulationSpeed = new JTextField(speedText);
		simulationSpeed.setEditable(false);

		timeFlowCtrl.setMajorTickSpacing(60);
		timeFlowCtrl.setMinorTickSpacing(30);
		timeFlowCtrl.setPaintTicks(true);
		timeFlowCtrl.setPaintLabels(true);

		@SuppressWarnings("rawtypes")
		Hashtable speed = new Hashtable();
		speed.put(0, new JLabel("1"));
		speed.put(60, new JLabel("60"));
		speed.put(120, new JLabel("120"));
		speed.put(180, new JLabel("180"));
		speed.put(240, new JLabel("240"));
		speed.put(300, new JLabel("300"));
		speed.put(360, new JLabel("360"));
		speed.put(420, new JLabel("420"));
		speed.put(480, new JLabel("480"));
		speed.put(540, new JLabel("540"));
		speed.put(600, new JLabel("600"));
		timeFlowCtrl.setLabelTable(speed);
		timeFlowCtrl.setAutoscrolls(true);
		
		sliderPanel.add(simulationSpeed);
		sliderPanel.add(timeFlowCtrl);
		
		return sliderPanel;
	}

	/**
	 * a method that adds change listener to time flow control JSlider 
	 * so that user could manipulate the speed of simulation. 
	 * This method can be called from other classes that have access to MCV view, such as MCV controller
	 * @param cl - a ChangeListener
	 */
	public void addChangeListenerToView(ChangeListener cl) {
		timeFlowCtrl.addChangeListener(cl);
	}

	/**
	 * a getter method that returns JSlider used to change simulation speed
	 * @return timeFlowCtrl - a JSlider that allows user to control the speed of simulation
	 */
	public JSlider getTimeSlider() {
		return timeFlowCtrl;
	}

	/**
	 * a method that adds item listener to a toggle button used for pausing simulation.
	 * This method can be called from other classes that have access to MCV view, such as MCV controller
	 * @param il - an ItemListener
	 */
	public void addToggleButtonListener(ItemListener il) {
		pauseSimulation.addItemListener(il);
	}

	/**
	 * a getter method that returns JToggleButton used to pause/unpause simulation
	 * @return pauseSimulation - a JToggleButton that pauses/unpauses simulation
	 */
	public JToggleButton getPauseButton() {
		return pauseSimulation;
	}

	/**
	 * a method adds action listener to JButtons used in a SimulationView.
	 * This method can be called from other classes that have access to MCV view, such as MCV controller
	 * @param al -  an ActionListener
	 */
	public void addActionListener(ActionListener al) {
		startSimulation.addActionListener(al);
		setRandSeedZero.addActionListener(al);
		setRandSeedOne.addActionListener(al);
		finishSimulation.addActionListener(al);
		exitSimulation.addActionListener(al);
		selectTestDataset.addActionListener(al);
		selectSmallDataset.addActionListener(al);
		selectLargeDataset.addActionListener(al);

	}

	/**
	 * disables START JBbutton, which is used to start the simulation
	 */
	public void disableStartButton() {
		startSimulation.setEnabled(false);
	}

	/**
	 * a method that creates and returns display and self check-in and check in desks queue panels
	 * @return queueAndAirportCheckinPane - a JPanel that contains self check-in and desk check-in display
	 */
	public JPanel queueAndSelfCheckIn() {
		queueAndAirportCheckinPane = new JPanel();
		queueAndAirportCheckinPane.add(selfCheckIn());
		queueAndAirportCheckinPane.add(queueInfoDisplay());
		queueAndAirportCheckinPane.setLayout(new GridLayout(1,2));
		return queueAndAirportCheckinPane;
	}
	
	/**
	 * a method creates and returns a panel that shows information about passengers using self check in 
	 * @return selfCheckinPanel -  a JPanel that has all information about self check-in services
	 */
	public JPanel selfCheckIn() {
		selfCheckinPanel = new JPanel();
		selfCheckinPanel.setLayout(new BoxLayout(selfCheckinPanel, BoxLayout.Y_AXIS));
		JTextArea title = new JTextArea(">>> SELF CHECK-IN SERVICES <<<");
		title.setFont(new Font(Font.MONOSPACED, Font.PLAIN, 12));
		title.setEditable(false);
		selfCheckInServerPane = new JTextArea("Loading passengers");
		selfCheckInServerPane.setSize(new Dimension(600, 200));
		selfCheckInServerPane.setWrapStyleWord(true);
		selfCheckInServerPane.setFont(new Font(Font.MONOSPACED, Font.PLAIN, 12));
		selfCheckInServerPane.setEditable(false);
		selfCheckInScrollPane = new JScrollPane(selfCheckInServerPane);
		selfCheckInScrollPane.setPreferredSize(new Dimension(461, 332));
		selfCheckInScrollPane.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_ALWAYS);
		selfCheckInScrollPane.setHorizontalScrollBarPolicy(JScrollPane.HORIZONTAL_SCROLLBAR_ALWAYS);
		selfCheckInScrollPane.setViewportView(selfCheckInServerPane);
		selfCheckinPanel.add(selfCheckInScrollPane);
		
		selfCheckinPanel.setBorder(BorderFactory.createMatteBorder(4, 4, 4, 4, Color.LIGHT_GRAY));
		return selfCheckinPanel;
	}

	/**
	 * a method creates and returns a JPanel which shows the average waiting time in queue as well as the size of a queue,
	 * it also displays information of passengers waiting in queue, such as time or arrival, flight, name and type of baggage they are carrying
	 * @return queueInformartion - a JPanel that contains information about passengers waiting in a queue for check-in desks
	 */
	public JPanel queueInfoDisplay() {
		JPanel queueInformation = new JPanel();
		queueInformation.setLayout(new BoxLayout(queueInformation, BoxLayout.Y_AXIS));
		checkinQueueSize = new JTextArea("There are currently X people waiting in the queue");
		checkinQueueSize.setFont(new Font(Font.MONOSPACED, Font.PLAIN, 10));
		checkinQueueSize.setMaximumSize(new Dimension(Integer.MAX_VALUE, checkinQueueSize.getMinimumSize().height));
		// queueSize.setLineWrap(true);
		// queueSize.setWrapStyleWord(true);
		checkinQueueSize.setEditable(false);
		listOfPeopleInQueue = new JTextArea("LOADING PASSENGERS...");
		listOfPeopleInQueue.setSize(new Dimension(600, 200));
		listOfPeopleInQueue.setLineWrap(true);
		listOfPeopleInQueue.setWrapStyleWord(true);
		listOfPeopleInQueue.setFont(new Font(Font.MONOSPACED, Font.PLAIN, 12));
		listOfPeopleInQueue.setEditable(false);
		JScrollPane queueList = new JScrollPane(listOfPeopleInQueue);
		queueList.setSize(100, 100);
		queueInformation.add(checkinQueueSize);
		queueInformation.add(queueList);
		queueInformation.setBorder(BorderFactory.createMatteBorder(4, 4, 4, 4, Color.LIGHT_GRAY));
		return queueInformation;
	}


	/**
	 * a method creates and returns a JPanel that holds multiple JTextAreas (equivalent to the number of check-in desks open, max - 99 desks)
	 * @return deskScroll - a JScrollPane that holds all check-in desks open
	 */
	public JScrollPane checkInDeskDisplay() {
		checkinDeskDisplay = new JPanel(new GridLayout(33, 3));
		checkinDesks = new JTextArea [99]; //desk open
		for (int i=0; i<99; i++) {
			checkinDesks[i] = new JTextArea("Check in desk " + (i+1) + " is CLOSED"); 
			checkinDesks[i].setFont(new Font(Font.MONOSPACED, Font.PLAIN, 11));
			checkinDesks[i].setBorder(BorderFactory.createMatteBorder(2, 2, 2, 2, Color.LIGHT_GRAY));
			checkinDesks[i].setEditable(false);
			checkinDesks[i].setPreferredSize(new Dimension(200, 150));
			checkinDeskDisplay.add(checkinDesks[i]);
		}
		deskScrollPane = new JScrollPane(checkinDeskDisplay);
		deskScrollPane.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_ALWAYS);
		deskScrollPane.setHorizontalScrollBarPolicy(JScrollPane.HORIZONTAL_SCROLLBAR_NEVER);
		deskScrollPane.setViewportView(checkinDeskDisplay);
		return deskScrollPane;
	}

	/**
	 * a method creates and returns a JPanel that holds multiple JTextAreas (equivalent to the number of boarding desks open, max - 9 desks)
	 * @return boardingDeskScrollPane - a JScrollPane that holds all boarding desks open
	 */
	public JScrollPane boardingDeskDisplay() {
		boardingDeskDisplay = new JPanel(new GridLayout(3, 3));
		boardingDesks = new JTextArea[9];
		for (int i=0; i<9; i++) {
			boardingDesks[i] = new JTextArea("Gate number " + (i+1) + " is CLOSED");
			boardingDesks[i].setFont(new Font(Font.MONOSPACED, Font.PLAIN, 11));
			boardingDesks[i].setBorder(BorderFactory.createMatteBorder(4, 4, 4, 4, Color.LIGHT_GRAY));
			boardingDesks[i].setPreferredSize(new Dimension(200, 150));
			boardingDesks[i].setEditable(false);
			boardingDeskDisplay.add(boardingDesks[i]);
		}
		boardingDeskScrollPane = new JScrollPane(boardingDeskDisplay);
		boardingDeskScrollPane.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_ALWAYS);
		boardingDeskScrollPane.setHorizontalScrollBarPolicy(JScrollPane.HORIZONTAL_SCROLLBAR_AS_NEEDED);
		boardingDeskScrollPane.setViewportView(boardingDeskDisplay);
		return boardingDeskScrollPane;
	}

	/**
	 * a method creates and returns a JPanel that holds JTextArea containing information about flights of the simulation
	 * @return timeAndStatusBoard - a JPanel that displays a board that contains flight list and their time of boarding, destination, status and gate number
	 */
	public JPanel flightStatusDisplay() {
		JPanel timeAndStatusBoard = new JPanel();
		timeAndStatusBoard.setLayout(new BoxLayout(timeAndStatusBoard,BoxLayout.Y_AXIS));
		flightStatusBoard = new JTextArea("");
		flightStatusBoard.setLineWrap(true);
		flightStatusBoard.setWrapStyleWord(true);
		flightStatusBoard.setFont(new Font(Font.MONOSPACED, Font.PLAIN, 12));
		flightStatusBoard.setEditable(false);
		JScrollPane flightList = new JScrollPane(flightStatusBoard);
		timeAndStatusBoard.add(flightList);
		return timeAndStatusBoard;
	}

	/**
	 * method implements Observable/Observer pattern, it updates MCV View accordingly by information provided by MCV model.
	 * The method is called when model calls notifyObservers()
	 */
	@Override
	public synchronized void update(Observable obj, Object o) {
		
		//updates desks check-in desks queue size and average waiting time
		int updatedCheckInQueueLength = model.getCheckInDeskQueue().queueSize();
		this.checkinQueueSize.setText(updatedCheckInQueueLength + " people waiting, estimated time to front of check-in queue: " + model.timeLengthOfQueue());
		
		//updates  list of people waiting for check-in desks
		String updatedListOfPeopleInQueue = model.getCheckInDeskQueue().getReport(1);
		this.listOfPeopleInQueue.setText(updatedListOfPeopleInQueue);
		
		//updates check-in desks server details
		for (int i =0; i<model.openCheckInDesks(); i++) {
			String checkInDeskActivity = "Check-in desk no."+ (i +1)+ "\n" + 
					model.getCheckInDeskServerList().get(i).getServerDetails();
			this.checkinDesks[i].setText(checkInDeskActivity);
			this.checkinDesks[i].setLineWrap(true);
			this.checkinDesks[i].setWrapStyleWord(true);
		}
		
		// updates self check-in JTextArea
		selfCheckInServerDetails = model.getSelfCheckInServer().getServerDetails();
		selfCheckInServerPane.setText(selfCheckInServerDetails);
		
		// updates board displaying flights
		String updatedFlightStatusDisplay = model.getFlightStatusReport();
		flightStatusBoard.setText(updatedFlightStatusDisplay);


		// updates boarding desk server details
		if (model.haveFlightsStartBoarding() == true) {
			for (int i=0; i<model.getFlightsServerList().getSize(); i++) {
				String boardingReport = model.getBoardingList().get(i).getServerDetails();
				this.boardingDesks[i].setText(boardingReport);
				this.boardingDesks[i].setLineWrap(true);
				this.boardingDesks[i].setWrapStyleWord(true);	
			}
		} 
		
		// updates how many booking are left in the dataset
		datasetInformation = "Size of the dataset: " + model.getBookingFileSize() + 
				"				It contains " + model.getBookings().size() + " bookings and " + model.getFlightSet().getSize() + " flights";

		datasetSize.setText(datasetInformation);

		//updates time acceleration/deceleration
		long updatedSpeedLong = model.getSimulationTime().getAccel();
		int updatedSpeed = (int) updatedSpeedLong;
		double factor = (double) updatedSpeed / 60 ;
		String updatedSpeedCheck = "The current acceleration speed is " + updatedSpeed ;
		String f = "" ;
		String tailMessage = "" ;
		if ( factor >= 1) { 
			 f = String.format("%.1f", factor);
			 tailMessage = "x. One Second of real time is " + f + " minutes(s) of airport time.";
		}
		else {
			factor = factor * 60 ;
			f = String.format("%.1f", factor);
			tailMessage = "x. One Second of real time is " + f + " second(s) of airport time.";
		}
		updatedSpeedCheck += tailMessage ;
		simulationSpeed.setText(updatedSpeedCheck);

		//components of clock/time that require updates
		int [] time = model.getSimulationTime().getTimeInInts();
		String twentyfour = model.getSimulationTime().getTime24();
		int hours = time[0];
		int minutes = time[1];
		analogClock.updateAnalog(hours, minutes);
		digitalClock.updateDigital(twentyfour);

		if (model.isFinished() && isSummaryWindowOn == false) {
			simulationSummaryFrame();
			isSummaryWindowOn = true;
		}
	}
}
