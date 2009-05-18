/**
 * @author Hiram van Paassen, Eric Broersma
 */

package gdp.erichiram.partsim.test;

import gdp.erichiram.partsim.Particle;

import org.junit.Assert;
import org.junit.Test;


public class ParticleTest {

		
	@Test
	public void testRandomParticle() {
		
		for (int i = 0; i < 100000; i++ ) {
			Particle p = new Particle(i);
			Assert.assertTrue(p.getDx() >= -799 && p.getDx() <= 799 );
			Assert.assertTrue(p.getDy() >= -599 && p.getDy() <= 599 );
		}			
	}
	
	@Test
	public void testParticleMove() {
		
		for (int i = 0; i < 1000; i++ ) {
			Particle p = new Particle(i);
			for ( int j = 0; j < 10000; j++ ) {
				p.move();
				Assert.assertTrue(p.getX() >= 0 && p.getX() < 800 );
				Assert.assertTrue(p.getY() >= 0 && p.getY() < 600 );
			}
		}
		
		for (int i = 0; i < 1000; i++ ) {
			Particle p = new Particle(i);
			p.setDx((int) (Math.random() * 10000 - 5000));
			p.setDy((int) (Math.random() * 10000 - 5000));
			for ( int j = 0; j < 10000; j++ ) {
				p.move();
				Assert.assertTrue(p.getX() >= 0 && p.getX() < 800 );
				Assert.assertTrue(p.getY() >= 0 && p.getY() < 600 );
			}
		}
	}
}
