# Requirements #

[Specification of the practical project](http://www.cs.uu.nl/docs/vakken/gdp/Prakt/ParalParticleSim.html) at the course website.

## Summary ##

**Hopefully the list below accurately depicts the requirements for the assignment.**

**These quotations could be made a bit more concise!**

  * "There is a bounded rectangle R (of size 800 in the x-direction and 600 in the y-direction) containing N particles.  Each particle has a location within R, a direction of movement and a speed.  Particles behave naturally when they bounce at the boundary of R.  Particles have size 0, and (for reasons of ease) several particles may be on the same location.  The movement of one particle does not disturb the movement of other particles. Each particle has a (not necessarily unique) name: a lower case letter or an upper case letter."
  * "If you show a configuration of the particles on screen, assume that the x-direction is horizontally oriented to the right and that the y-direction is vertically oriented downward. Hence the origin (0,0) of the rectangle R is in the left upper corner of the screen."
  * "The initial configuration of the particles will be read from file (containing the number of particles and for each particle its name, its location, its direction, and its speed)."
  * "In the animation the user has the possibility to input interactively a name n such that at least one particle with name n (if present) is removed from the simulation.  Similarly the user has the opportunity to present a name, a location, a direction of movement and a speed so that a new particle with the given characteristics is included in the simulation. You cannot assume that input given via the screen is syntactically correct."
  * "Moreover the user is offered a button on the screen to generate a new particle with a random name, a random location in R and a random direction of movement and random speed. In case a 'random' particle will be generated you should generate its direction and speed as a vector (dx, dy) in which dx and dy are random integral numbers in the ranges `[-799, 799]` and `[-599, 599]`, respectively. All numbers (and names) are generated independently according to a uniform distribution."
  * "The user must have the opportunity to slow down the simulation by setting a fixed amount of time t such that (about) time t elapses between start and finish of the computation of each round for each particle (and during this elapse time the thread is not allowed to do the computation for other particles.  This amount t can be interactively increased and decreased by the user."
  * "It would be nice if the color of the particle on the screen gives information which thread has taken care of the particle in this round."
  * "Maintain a queue Q of N particles and let at the beginning of a round each thread take k particles from Q.  If the thread has done the computation for the assigned particles, it puts them back into Q, and if some particles must still be dealt with in this round, the thread proceeds with at least one of them.  Make sure that all particles are dealt with in a given round."
  * "The user must be allowed to interactively set a maximum P on the number of threads.  If p>P, your program must make sure that, after a reasonable number of rounds (the sooner the better), p-P threads have been finished, while the simulation remains correct.  If p<P and N>p, you must make sure that in due time (for example in the next round) a new thread for the computation will be created."
  * "At all times it must be visible on screen how many threads there are, what value the number P has, and what the number of particles is."
  * "We expect a main class, an animation class, a class which encapsulates all the graphics, a class which contains the integer monitor, a class for the queue, etc."
  * "Doing something nice and extra on the simulation of systems of particles and/or the load balancing and synchronization may lead to a bonus on your grade."