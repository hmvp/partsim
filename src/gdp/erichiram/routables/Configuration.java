// Hiram van Paassen (HIRAM#)
// Eric Broersma (ERIC#)

package gdp.erichiram.routables;

/**
 * Static configuration class containing some global variables.
 * @author Hiram van Paassen, Eric Broersma
 */
public class Configuration {
	/**
	 * Time to wait before retrying to repair a non-existing connection.
	 */
	final static int retryConnectionTime = 1000;
	
	/**
	 * Show debug output.
	 */
	final static boolean printDebug = true;
}
