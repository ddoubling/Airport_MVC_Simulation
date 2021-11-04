/**
 * Name
 * @author group2 
 * This class manages passengers names.
 * It was originally provided by course F21SF, but has been expanded since.
 */
public class Name {
	// instance variables
	private String firstName;
	private String middleName;
	private String lastName;

	
	/**
	 * Name()
	 * @param fName
	 * @param lName
	 * constructor creating object with first and last name
	 */
	public Name(String fName, String lName) {
		firstName = fName;
		middleName = "";
		lastName = lName;
	}

	/**
	 * Name()
	 * @param fName
	 * @param mName
	 * @param lName
	 * constructor creating object with full name, in which middle name could be empty if it doesn't exist.
	 */
	public Name(String fName, String mName, String lName) {
		firstName = fName;
		middleName = mName;
		lastName = lName;
	}

	
	/**
	 * Name()
	 * @param fName
	 * @param mName
	 * @param lName
	 * constructor to create name from full name in the format, 
	 * first name then space then last name, or first name then space then middle name then space then last name,
	 */
	public Name(String fullName) {
		int spacePos1 = fullName.indexOf(' ');
		firstName = fullName.substring(0, spacePos1);
		int spacePos2 = fullName.lastIndexOf(' ');
		if (spacePos1 == spacePos2)
			middleName = "";
		else
			middleName = fullName.substring(spacePos1 + 1, spacePos2);
		lastName = fullName.substring(spacePos2 + 1);

	}

	/**
	 * getFirstName()
	 * @return firstName
	 */
	public String getFirstName() {
		return firstName;
	}

	/**
	 * getLastName()
	 * @return lastName
	 */
	public String getLastName() {
		return lastName;
	}

	/**
	 * setLastName()
	 * @param String ln
	 * sets the last name
	 */
	public void setLastName(String ln) {
		lastName = ln;
	}
	/**
	 * getFirstAndLastName()
	 * @return first & last name
	 */
	public String getFirstAndLastName() {
		return firstName + " " + lastName;
	}

	/**
	 * getLastCommaMiddle,CommaFirst()
	 * @return name in format last,middle,first.
	 */
	public String getLastCommaMiddleCommaFirst() {
		return lastName + ", " + firstName;
	}

	/**
	 * getFullName()
	 * @return full name.
	 */
	public String getFullName() {
		String result = firstName + " ";
		if (!middleName.equals("")) {
			result += middleName + " ";
		}
		result += lastName;
		return result;
	}

	/**
	 * getInitials()
	 * @return the initials.
	 */
	public String getInitials() {
		return firstName.charAt(0) + "." + lastName.charAt(0);
	}

	/**
	 * getFirstAndLast()
	 * @return first & last name.
	 */
	public String getFirstILast() {
		String shorterName = firstName + " ";
		if (middleName.length() != 0) {
			shorterName += Character.toString(middleName.charAt(0)) + " ";
		}
		shorterName += lastName;
		return shorterName;
	}

	/**
	 * getInitPeriodLast()
	 * @return first (initial of middle name) last
	 */
	public String getInitPeriodLast() {
		return firstName.charAt(0) + ". " + lastName;
	}

	/** 
	 * getNameIn20Chars()
	 * @return the longest, most descriptive name that will fit in 20 characters.
	 */
	public String getNameIn20Chars() {
		String nameIn20 = getFullName();
		if (nameIn20.length() > 20) { nameIn20 = getFirstILast(); }
		if (nameIn20.length() > 20) { nameIn20 = getFirstAndLastName(); }
		if (nameIn20.length() > 20) { nameIn20 = getInitPeriodLast(); }
		if (nameIn20.length() > 20) { nameIn20 = getInitials(); }
		return String.format("%0$-20s", nameIn20);
	}
}
