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
import java.util.concurrent.LinkedBlockingQueue;

public class ConfigurationReader {
	public static Collection<Particle> readFile(File f)
			throws FileNotFoundException {
		Collection<Particle> particles = new LinkedBlockingQueue<Particle>();
		StreamTokenizer st = new StreamTokenizer(new FileReader(f));

		try {
			if (st.nextToken() == StreamTokenizer.TT_NUMBER) {
				particles = new LinkedBlockingQueue<Particle>();
				int numParticles = (int)st.nval;
				
				while (st.nextToken() == StreamTokenizer.TT_WORD) {
					String name = st.sval;
					int x = getNumber(st);
					int y = getNumber(st);
					int dx = getNumber(st);
					int dy = getNumber(st);

					Main.debug("Adding particle from file!");
					particles.add(new Particle(x,y,dx,dy,name.charAt(0), Main.initialRound));
				}
				
				if ( particles.size() != numParticles ) {
					// TODO beter bericht
					System.err.println("Number of particles in input file (" + particles.size() + ") doesn't match the specified number (" + numParticles + ").");
				}
				
			}
		} catch (IOException e) {
			e.printStackTrace();
		}

		return particles;
	}
	
	private static int getNumber(StreamTokenizer st) throws IOException
	{
		if (st.nextToken() != StreamTokenizer.TT_NUMBER)
			throw new IOException("wrong data format");
		return (int) st.nval;
	}
}
