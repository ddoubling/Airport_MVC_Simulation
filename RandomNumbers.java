import java.util.Random;

/**
 * RandomNumber class
 * creates random numbers
 * @author group2
 */
public class RandomNumbers {

	public static Boolean fixedSeed = false ;
	private static Random randomNumbers = new Random() ;
	private static Random fixedRandomNumbers = new Random(1L) ;
	private static Random unfixedRandomNumbers = new Random(System.currentTimeMillis());
	private static RandomNumbers single_instance = null;


	/**
	 * getInstance
	 * @author group2
	 * creates a singleton, parameterised with a boolean indicating if the seed should be fixed.
	 * 
	 */
	public static RandomNumbers getInstance(Boolean fixedSeed) {
		if( ! ( single_instance == null)) {
		}
		else {
			if(fixedSeed) {
				randomNumbers = fixedRandomNumbers ;
				single_instance = new RandomNumbers();
			}
			else {
				randomNumbers = unfixedRandomNumbers ;
				single_instance = new RandomNumbers() ;
			}
		}
		return single_instance ;
	}

	/**
	 * getInstance()
	 * @return the instance of this class.
	 */
	public static RandomNumbers getInstance() {
		if(single_instance ==null) {
			single_instance = new RandomNumbers();
		}
		return single_instance;
	}


	/**
	 * getNextRandomInteger()
	 * @author group2
	 * creates a random number
	 * @param an integer, the maximum number wanted.
	 * @returns a random integer in range 0....max.
	 */
	public int getNextRandomInteger(int max) {
		return randomNumbers.nextInt(max+1);
	}


	/**
	 * getNextRandomDouble()
	 * @author group2
	 * creates a random number
	 * @param min & max, doubles representing range wanted
	 * @returns a random double in range min...max
	 */
	public Double getNextRandomDouble(Double min, Double max) {
		Double randomDouble = randomNumbers.nextDouble( );
		return ( min + randomDouble*(max-min) );
	}


	/**
	 * getNextRandom()
	 * @author group2
	 * creates a random number
	 * @returns a random double in range 0....1
	 */
	public Double getNextRandom() {
		return randomNumbers.nextDouble( ) ;
	}

	/**
	 * fixSeed()
	 * @author group2
	 * sets the seed for this chain of pseudo random numbers
	 *  
	 */
	public void fixSeed() {
		fixedSeed = true ;
	}
}
