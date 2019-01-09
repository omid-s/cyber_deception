/**
 * 
 */
package querying.tools;

import classes.ResourceType;

/**
 * @author omid
 *
 */
public class EnumTools {

	/**
	 * This method returns enum representation of the input string
	 * 
	 * @param enumeration the enum object in which the value should be looked up
	 * @param search      the string to look for in the enum
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

	
	/**
	 * turns a resource item type into a char represntation of sysdig types. 
	 * @param inp resource type
	 * @return the the character sysdig type
	 */
	public static String resourceTypeToChar(ResourceType inp) {

		String ret = "f";
		switch (inp) {
		case File:
			ret = "f";
			break;
		case NetworkIPV4:
			ret = "4";
			break;
		case NetworkIPV6:
			ret = "6";
			break;
		case Unix:
			ret = "u";
			break;
		case SignalFDs:
			ret = "s";
			break;
		case EventFDs:
			ret = "e";
			break;
		case iNotifyFDS:
			ret = "i";
			break;
		case TimerFDs:
			ret ="t";
			break;
		case Pipe:
			ret  = "p";
			break;
		}
		
		return ret;

	}
	
	/**
	 * converts the type char string as recieved from sysdig to a 
	 * resourcetype represenations 
	 * @param typeChar type character as defined by sysdig
	 * @return the resource item equavalent of the enterd character
	 */
	public static ResourceType typeCharToResourceType( String typeChar ){

		ResourceType ItemType = ResourceType.File;
		switch (typeChar) {
		case "f":
			ItemType = ResourceType.File;
			break;
		case "4":
			ItemType = ResourceType.NetworkIPV4;
			break;
		case "6":
			ItemType = ResourceType.NetworkIPV6;
			break;
		case "u":
			ItemType = ResourceType.Unix;
			break;
		case "s":
			ItemType = ResourceType.SignalFDs;
			break;
		case "e":
			ItemType = ResourceType.EventFDs;
			break;
		case "i":
			ItemType = ResourceType.iNotifyFDS;
			break;
		case "t":
			ItemType = ResourceType.TimerFDs;
			break;
		case "p":
			ItemType = ResourceType.Pipe;
			break;
		}
		return ItemType;

	}

}
