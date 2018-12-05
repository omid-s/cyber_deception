/**
 * 
 */
package querying.tools;

/**
 * @author omid
 *
 */
public class EnumTools {
	
	/**
	 * This method returns  enum representation of the input string
	 * @param enumeration the enum object in which the value should be looked up
	 * @param search the string to look for in the enum
	 * @return the enum representation  
	 */
	public static <T extends Enum<?>> T searchEnum(Class<T> enumeration, String search) {
		for (T each : enumeration.getEnumConstants()) {
			if (each.name().compareToIgnoreCase(search) == 0) {
				return each;
			}
		}
		return null;
	}
}
