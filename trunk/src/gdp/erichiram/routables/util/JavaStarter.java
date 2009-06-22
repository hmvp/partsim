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
		putMap(1103, new Tuple(1102,200), new Tuple(1100,20));
		putMap(1102, new Tuple(1101,30), new Tuple(1104,100), new Tuple(1103,1000));
		putMap(1100, new Tuple(1104,100), new Tuple(1103,50));
		putMap(1104, new Tuple(1102,40), new Tuple(1101,120), new Tuple(1100,40));
		putMap(1101, new Tuple(1102,70), new Tuple(1104,50));

		
		for(int s : m.keySet())
		{
			startProcess(s, m.get(s));
		}
	}
	
	private Thread startProcess(final Integer port, final Set<Tuple> d)
	{
		Thread t = new Thread(){

			public void run() {
				Map<Integer, Integer> neighbours = new HashMap<Integer, Integer>();

				for (Tuple p : d)
				{
					neighbours.put(p.port,p.w);
				}
				
				new NetwProg(port,neighbours,true).run();
			}
			
		};
		
		t.start();
		return t;
	}
}

class Tuple {
	public int port;
	public int w;
	
	public Tuple(Integer port, Integer w){
		if(port < 1100 || port > 1120 || w < 1 || w > 1000)
			throw new RuntimeException("AArggh input wrong!");
		
		this.port = port;
		this.w = w;
	}
}

