# Introduction #

Also see [Requirements](Requirements.md).

# Some thoughts #

  * Model-View-Controller
  * Treat View and Controllers as separate threads.
  * View:
    * Displaying the rectangle and the particles
      * "An animation process of the simulation is easily programmed by having a thread with a single 'infinite' loop which repaints the canvas on which the particles are displayed and then sleeps for a number of milliseconds (find out yourself what value is needed to get it fluent while not too time consuming).  The display of the simulation process should not be synchronized with the threads that compute the locations of the particles, and therefore it might occasionally occur that displayed particles are displayed on their locations in the previous round.  This is no problem."
  * Controllers:
    * User input
      * Adding particles
      * Concurrency control
    * Updating the model
      * Multiple threads (<= `P`)
  * ...

# Interface mockup #

http://lh3.ggpht.com/_e-egFSNqDEk/SfJEoZs3boI/AAAAAAAAABg/KwbJBpuwG5w/mockup.GIF