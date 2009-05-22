/**
 * @author Hiram van Paassen, Eric Broersma
 */

package gdp.erichiram.partsim.test;

import gdp.erichiram.partsim.Main;
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
		
		for (int i = 0; i < 200; i++ ) {
			Particle p = new Particle(i);
			for ( int j = 0; j < 10000; j++ ) {
				p.move();
				Assert.assertTrue(p.getX() >= 0 && p.getX() < 800 );
				Assert.assertTrue(p.getY() >= 0 && p.getY() < 600 );
			}
		}
	}
	
	@Test
	public void testParticleStupidMove() {
		
		for (int i = 0; i < 200; i++ ) {
			Particle p = new Particle(i);
			for ( int j = 0; j < 10000; j++ ) {
				p.stupidMove();
				Assert.assertTrue(p.getX() >= 0 && p.getX() < 800 );
				Assert.assertTrue(p.getY() >= 0 && p.getY() < 600 );
			}
		}
	}
	
	@Test
	public void testParticleSmartMove() {
		
		for (int i = 0; i < 200; i++ ) {
			Particle p = new Particle(i);
			for ( int j = 0; j < 10000; j++ ) {
				p.smartMove();
				Assert.assertTrue(p.getX() >= 0 && p.getX() < 800 );
				Assert.assertTrue(p.getY() >= 0 && p.getY() < 600 );
			}
		}
	}
	
	@Test
	public void testParticleBigDMove() {
		
		for (int i = 0; i < 200; i++ ) {
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
	
	@Test
	public void testParticleBigDStupidMove() {
		
		for (int i = 0; i < 200; i++ ) {
			Particle p = new Particle(i);
			p.setDx((int) (Math.random() * 10000 - 5000));
			p.setDy((int) (Math.random() * 10000 - 5000));
			for ( int j = 0; j < 10000; j++ ) {
				p.stupidMove();
				Assert.assertTrue(p.getX() >= 0 && p.getX() < 800 );
				Assert.assertTrue(p.getY() >= 0 && p.getY() < 600 );
			}
		}
	}
	
	@Test
	public void testParticleBigDSmartMove() {
		
		for (int i = 0; i < 200; i++ ) {
			Particle p = new Particle(i);
			p.setDx((int) (Math.random() * 10000 - 5000));
			p.setDy((int) (Math.random() * 10000 - 5000));
			for ( int j = 0; j < 10000; j++ ) {
				p.smartMove();
				Assert.assertTrue(p.getX() >= 0 && p.getX() < 800 );
				Assert.assertTrue(p.getY() >= 0 && p.getY() < 600 );
			}
		}
	}
	
	@Test
	public void testParticleStaticMove() {
		
		Particle p = new Particle(799,599,-800,-600,'a', 0);
		p.move();
		Assert.assertEquals(1, p.getX());
		Assert.assertEquals(1, p.getY());
		
		p = new Particle(0,0,800,600,'a', 0);
		p.move();
		Assert.assertEquals(798, p.getX());
		Assert.assertEquals(598, p.getY());
		
		p = new Particle(799,599,1598,1198,'a', 0);
		p.move();
		Assert.assertEquals(799, p.getX());
		Assert.assertEquals(599, p.getY());
		
		p = new Particle(0,0,1598,1198,'a', 0);
		p.move();
		Assert.assertEquals(0, p.getX());
		Assert.assertEquals(0, p.getY());
		
		int startDX = -100;
		int endDX = 1000;
		
		int expectedX = 100;
		int dExpectedX = -1;
		for ( int dx = startDX; dx <= endDX; ++dx) {
			p = new Particle(0,0,dx,0,'a', 0);
			p.move();
			Assert.assertEquals(expectedX, p.getX());
			
			expectedX += dExpectedX;
			if ( expectedX == Main.width ) {
				expectedX = Main.width - 2;
				dExpectedX = -1;
			} else if ( expectedX == -1 ) {
				expectedX = 1;
				dExpectedX = 1;
			}
		}	
	}
	
	@Test
	public void testParticleStaticStupidMove() {

		Particle p = new Particle(799,599,-800,-600,'a', 0);
		p.stupidMove();
		Assert.assertEquals(1, p.getX());
		Assert.assertEquals(1, p.getY());
		
		p = new Particle(0,0,800,600,'a', 0);
		p.stupidMove();
		Assert.assertEquals(798, p.getX());
		Assert.assertEquals(598, p.getY());
		
		p = new Particle(799,599,1598,1198,'a', 0);
		p.stupidMove();
		Assert.assertEquals(799, p.getX());
		Assert.assertEquals(599, p.getY());
		
		p = new Particle(0,0,1598,1198,'a', 0);
		p.stupidMove();
		Assert.assertEquals(0, p.getX());
		Assert.assertEquals(0, p.getY());
		
		int startDX = -100;
		int endDX = 1000;
		
		int expectedX = 100;
		int dExpectedX = -1;
		for ( int dx = startDX; dx <= endDX; ++dx) {
			p = new Particle(0,0,dx,0,'a', 0);
			p.stupidMove();
			Assert.assertEquals(expectedX, p.getX());
			
			expectedX += dExpectedX;
			if ( expectedX == Main.width ) {
				expectedX = Main.width - 2;
				dExpectedX = -1;
			} else if ( expectedX == -1 ) {
				expectedX = 1;
				dExpectedX = 1;
			}
		}		
	}
	
	@Test
	public void testParticleStupidMoveVsMove() {
				
		for (int i = 0; i < 1000; i++ ) {
			Particle p = new Particle(i);
			p.setDx((int) (Math.random() * 10000 - 5000));
			p.setDy((int) (Math.random() * 10000 - 5000));
			Particle q = new Particle(p);
			for ( int j = 0; j < 1000; j++ ) {
				p.stupidMove();
				q.move();
				Assert.assertEquals(p.getX(), q.getX());
				Assert.assertEquals(p.getY(), q.getY());
			}
		}
	}
	
	@Test
	public void testParticleStupidMoveVsSmartMove() {
				
		for (int i = 0; i < 1000; i++ ) {
			Particle p = new Particle(i);
			p.setDx((int) (Math.random() * 10000 - 5000));
			p.setDy((int) (Math.random() * 10000 - 5000));
			Particle q = new Particle(p);
			for ( int j = 0; j < 1000; j++ ) {
				p.stupidMove();
				q.smartMove();
				Assert.assertEquals(p.getX(), q.getX());
				Assert.assertEquals(p.getY(), q.getY());
			}
		}
	}
}
