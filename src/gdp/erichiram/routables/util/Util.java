package gdp.erichiram.routables.util;

public class Util {

	final static boolean DEBUG = true;
	
	/**
	 * debug messages switchable with DEBUG boolean
	 * @param message
	 */
	public static void debug(int id, String message)
	{
		if (DEBUG)
		{
			System.out.println(id + ": " + message);
		}
	}
}
