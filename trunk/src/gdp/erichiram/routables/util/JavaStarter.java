/**
 * 
 */
package gdp.erichiram.routables.util;

import gdp.erichiram.routables.NetwProg;

import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;


/**
 * @author hiram, eric
 *
 */
public class JavaStarter {

	/**
	 * @param args
	 */
	
	
	public static void main(String[] args) {		
		new JavaStarter();
	}

	private Map<Integer,Set<Tuple>> m = new HashMap<Integer, Set<Tuple>>();
	
	private void putMap(int port, Tuple... data)
	{
		m.put(port, new HashSet<Tuple>(Arrays.asList(data)));
	}
	
	public JavaStarter()
	{	
		putMap(1103, new Tuple(1102,20), new Tuple(1100,2));
		putMap(1102, new Tuple(1101,3), new Tuple(1104,10), new Tuple(1103,100));
		putMap(1100, new Tuple(1104,10), new Tuple(1103,5));
		putMap(1104, new Tuple(1102,4), new Tuple(1101,12), new Tuple(1100,4));
		putMap(1101, new Tuple(1102,7), new Tuple(1104,5));

		
		for(int s : m.keySet())
		{
			startProcess(s, m.get(s));
		}
	}
	
	private Thread startProcess(final Integer port, final Set<Tuple> d)
	{
		Thread t = new Thread(){

			public void run() {
				String[] args = new String[(d.size() * 2) + 1];
				args[0] = port.toString();
				int i = 1;
				for (Tuple p : d)
				{
					args[i++] = p.port;
					args[i++] = p.w;
				}
				
				NetwProg.main(args);
			}
			
		};
		
		t.start();
		return t;
	}
}

class Tuple {
	public String port;
	public String w;
	
	public Tuple(Integer port, Integer w){
		if(port < 1100 || port > 1120 || w < 1 || w > 1000)
			throw new RuntimeException("AArggh input wrong!");
		
		this.port = port.toString();
		this.w = w.toString();
	}
}

