// Hiram van Paassen (HIRAM#)
// Eric Broersma (ERIC#)

/**
 * @author Hiram van Paassen, Eric Broersma
 */

package gdp.erichiram.partsim.util;

import gdp.erichiram.partsim.Main;
import gdp.erichiram.partsim.Particle;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.io.StreamTokenizer;
import java.util.Collection;
import java.util.LinkedList;

/**
 * Class for reading particle data from a file
 *
 */
public class ConfigurationReader {
	/**
	 * method for reading a file with particle data and 
	 * returning an collection of particle
	 * uses some {@link StreamTokenizer} magic
	 * @param file the file to read from
	 * @return an collection of particles
	 * @throws FileNotFoundException
	 */
	public static Collection<Particle> readFile(File f) throws FileNotFoundException {
		Collection<Particle> particles = new LinkedList<Particle>();
		StreamTokenizer st = new StreamTokenizer(new FileReader(f));

		try {
			//do we have a number?
			if (st.nextToken() == StreamTokenizer.TT_NUMBER) {
				int numParticles = (int)st.nval;
				
				//while we find a name and 4 numbers add particles with the right data
				while (st.nextToken() == StreamTokenizer.TT_WORD) {
					String name = st.sval;
					int x = getNumber(st);
					int y = getNumber(st);
					int dx = getNumber(st);
					int dy = getNumber(st);

					Main.debug("Adding particle from file!");
					particles.add(new Particle(x,y,dx,dy,name.charAt(0), Main.initialRound));
				}
				
				//show a warning if the number of particle doesnt match the number specified
				if ( particles.size() != numParticles ) {
					System.err.println("Number of particles found in input file (" + particles.size() + ") doesn't match specified number (" + numParticles + ").");
				}
				
			}
		} catch (Exception e) {
			System.err.println("there is something wrong with this file or it is not in the right format");
		}

		return particles;
	}
	
	/**
	 * helper function to get the number value from the streamreader
	 * @param st
	 * @return
	 * @throws IOException
	 */
	private static int getNumber(StreamTokenizer st) throws IOException
	{
		if (st.nextToken() != StreamTokenizer.TT_NUMBER)
			throw new IOException("wrong data format");
		return (int) st.nval;
	}
}
