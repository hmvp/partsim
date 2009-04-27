package gdp.erichiram.partsim.util;

import gdp.erichiram.partsim.Particle;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.io.StreamTokenizer;
import java.util.ArrayList;
import java.util.Collection;

public class ConfigurationReader {
	public static Collection<Particle> readFile(File f)
			throws FileNotFoundException {
		Collection<Particle> particles = new ArrayList<Particle>(0);
		StreamTokenizer st = new StreamTokenizer(new FileReader(f));

		try {
			if (st.nextToken() == st.TT_NUMBER) {
				particles = new ArrayList<Particle>((int) st.nval);

				while (st.nextToken() == st.TT_WORD) {
					String name = st.sval;
					int x = getNumber(st);
					int y = getNumber(st);
					int dx = getNumber(st);
					int dy = getNumber(st);
					
					particles.add(new Particle(x,y,dx,dy,name.charAt(0)));
					
				}
			}
		} catch (IOException e) {
			e.printStackTrace();
		}

		return particles;
	}
	
	private static int getNumber(StreamTokenizer st) throws IOException
	{
		if (st.nextToken() != st.TT_NUMBER)
			throw new IOException("wrong data format");
		return (int) st.nval;
	}
}
