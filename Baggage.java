

/**
 * @author group2
 * Simple class to describe types of baggage passengers might arrive with
 */
public class Baggage {
	public enum bagType {NOBAG , CABIN, COMPACT, MEDIUM, LARGE, OVERSIZE }
	private Boolean hasBag;
	private bagType description;
	private int width;
	private int height;
	private int depth;
	private double weight;
	private double maxWeight;
	private double minWeight;
	private RandomNumbers random = RandomNumbers.getInstance();


	public Baggage(Boolean bagYN, bagType desc, int bw, int bh, int bd, double kgs) {
		hasBag = bagYN;
		description = desc;
		width = bw;
		height = bh;
		depth = bd;
		weight = setRandomBagWeight(description);
	}

	/**
	 * setRandomBagWeight()
	 * @param desc, the type of bag.
	 * @return a double representing the weight (Kg) of the bag
	 */
	private double setRandomBagWeight(bagType desc) {
		switch (desc) {
		case NOBAG :
			minWeight = 0;
			maxWeight = 0;
			break;
		case CABIN:
			minWeight = 0;
			maxWeight = 0;
			break;
		case COMPACT:
			minWeight = 5;
			maxWeight = 20;
			break;
		case MEDIUM:
			minWeight = 10;
			maxWeight = 30;	
			break;
		case LARGE:
			minWeight = 10;
			maxWeight = 45;
			break;
		case OVERSIZE:
			minWeight = 10;
			maxWeight = 55;
			break;
		}
		Double randomValue = random.getNextRandomDouble(minWeight, maxWeight) ;
		Double bagWeight = (double) (( (int) ( randomValue *10 )) / 10) ;
		return bagWeight ;
	}

	private static Baggage bag0 = new Baggage(false, bagType.NOBAG , 0,0,0, 0);
	private static Baggage bag1 = new Baggage(false, bagType.CABIN , 32,48,18, 0); 
	private static Baggage bag2 = new Baggage(true, bagType.COMPACT , 32,63,21, 0);
	private static Baggage bag3 = new Baggage(true, bagType.MEDIUM , 45,67,25, 0);
	private static Baggage bag4 = new Baggage(true, bagType.LARGE , 48,76,29, 0);
	private static Baggage bag5 = new Baggage(true, bagType.OVERSIZE , 50,80,35, 0);

	/** setBaggage
	 * @author group2
	 * @param b, an integer 0-5 describing the type of bag wanted
	 * @return a bag, type decided by b, with random weight in range defined by type of Bag
	 */	
	public static Baggage setBaggage(int b) 
	{
		switch(b) 
		{
		case (0): { return bag0;}
		case (1): { return bag1;}
		case (2): { return bag2;}
		case (3): { return bag3;}
		case (4): { return bag4;}
		case (5): { return bag5;}
		}
		return bag0;
	}

	/** getHasABag()
	 * @author group2
	 * @return indicates if there is a bag
	 */
	public Boolean getHasABag() {
		return hasBag;
	}
	/** getTypeOfBag()
	 * @author group2
	 * @return the type of bag
	 */
	public String getTypeOfBag() {
		return description.name();
	}
	
	/** getTypeOfBagLower()
	 * @author group2
	 * @return the type of bag in lower case
	 */
	public String getTypeOfBagLower() {
		String bt = "" ;
		if ( description == bagType.CABIN ) { bt += "Cabin" ;}
		else if ( description == bagType.COMPACT ){ bt += "Compact" ; }
		else if ( description == bagType.MEDIUM ){ bt += "Medium" ; }
		else if ( description == bagType.LARGE ){ bt += "Large" ; }
		else if ( description == bagType.OVERSIZE) { bt += "Oversize"; }
		return bt ;
	}
		
	/** getBagWeight()
	 * @author group2
	 * @return kgs weight
	 */	
	public double getBagWeight() {
		return weight ;
	}

	/** getBagVolume()
	 * @author group2
	 * @return m3 volume
	 */
	public double getBagVolume() {
		// m3
		return (width * height * depth / (10^6));
	}

}