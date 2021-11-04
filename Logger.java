import java.io.FileNotFoundException;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.concurrent.atomic.AtomicBoolean;

/**
 * 
 */

/**
 * Logger
 * @author group2
 * This Singleton class provides functionality to build a log of events in the airport.
 */

//Singleton pattern implementation
public class Logger {

	private ArrayList<String> log;
	private static Logger single_instance = null;
	private AtomicBoolean lMutex = new AtomicBoolean(false);


	/**
	 * 
	 */
	private Logger() {
		this.log = new ArrayList<String> ();
		this.log.add("THE EVENT LOG:\n") ;
	}
	
	/**
	 * getInstance()
	 * @return the single instance of Logger.
	 */
	public static Logger getInstance() {
		if(single_instance ==null) {
			single_instance = new Logger();
		}
		return single_instance;
	}

	/**
	 * add()
	 * @param String s
	 * Adds the String s to the log.
	 */
	public void add(String s) {
		do {}while( ! lMutex.compareAndSet(false, true) );
		log.add(s);
		lMutex.set(false);
	}

	/**
	 * writeEventListToFile()
	 * @author group2
	 * @param filename
	 * Writes the log to a file.
	 */
	public void writeEventListToFile(String filename) {	
		FileWriter fw;
		try {
			fw = new FileWriter(filename);
			Iterator iter = log.iterator();
			while (iter.hasNext()) {
				fw.write(iter.next() + "\n");
			}
			fw.close();
		}
		catch (FileNotFoundException fnf){
			System.out.println(filename + " not found ");
			System.exit(0);
		}
		catch (IOException ioe){
			ioe.printStackTrace();
			System.exit(1);
		}
	}

	/**
	 * @author group2
	 * @param filename
	 * 
	 */
	public static void writeEventPlanToFile(String filename) {
		FileWriter fw;
		try {
			fw = new FileWriter(filename);
			fw.close();
		}
		catch (FileNotFoundException fnf){
			System.out.println(filename + " not found ");
			System.exit(0);
		}
		catch (IOException ioe){
			ioe.printStackTrace();
			System.exit(1);
		}
	}	
}
