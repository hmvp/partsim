!!!Each file (both source and documentation) should have both names and student numbers of the responsible students at the top!!!

Hiram van Paassen (xxxxxxx)
Eric Broersma (xxxxxxx)


DE PARALLELLE DEELTJESSIMULATOR


0. INDEX

1. UITVOEREN VAN HET PROGRAMMA
1.1 Parameters Instellen
etc.
etc.
etc.



1. UITVOEREN VAN HET PROGRAMMA

$ javac src/*.java
$ java src/Main

Het bestand particles.txt met de initiele deeltjesconfiguratie wordt geladen. Dit bestand bevat op de eerste regel een getal N, gevolgd door N regels met informatie over de deeltjes. Deze regels zijn als volgt opgebouwd: "[naam] [X] [Y] [dX] [dY]", waarbij de naam een enkele (hoofd)letter is, en X, Y, dX en dY integers.

Vervolgens verschijnt een venster met een menu (zie 1.1 t/m 1.3) en een zwart uitvoerpaneel (zie 1.4).


1.1 Parameters Instellen

Met behulp van het bovenste deel van het menu kunnen parameters ingesteld worden die ervoor zorgen dat er threads worden opgestart die concurrent (zie 3) de deeltjes verplaatsen.

- Het "P"-veld bepaalt het maximum aantal threads (P) dat mag worden opgestart.
- Het "t"-veld bepaalt de slaaptijd (in milliseconden, t) van een thread per deeltje.
- Het "k"-veld bepaalt het aantal deeltjes (k) dat een thread wordt geacht in eerste instantie te pakken.


1.2 Deeltjes Toevoegen

Met behulp van het middelste deel van het menu kunnen deeltjes worden toegevoegd aan de simulatie.

- Voeg een geparametriseerd deeltje toe door de gewenste waardes voor de positie (X en Y) en de snelheid (dX en dY) in te vullen en op de "Add new Particle"-knop te klikken.
- Voeg een willekeurig geparametriseerd deeltje toe door op de "Add new random Particle"-knop te klikken.


1.3 Deeltjes Verwijderen

Met behulp van het onderste deel van het menu kunnen deeltjes worden verwijderd uit de simulatie.

- Verwijder een deeltje door de naam van het deeltje te selecteren en op de "Remove Particle"-knop te klikken.


1.4 Interpreteren Van Het Uitvoerpaneel

In eerste instantie bevat het uitvoerpaneel mogelijk enkele niet-bewegende deeltjes, en worden de waardes voor het aantal actieve threads (p) en het aantal aanwezige deeltjes (n) weergegeven in de linkerbovenhoek van het paneel.

Zodra er door middel van het menu het maximum aantal threads (P) groter wordt dan 0, start het programma nieuwe threads op. De waarde van het aantal actieve threads (p) neemt toe en de aanwezige deeltjes zullen verplaatst worden door de actieve threads.

De kleur van een deeltje wordt bepaald door het ID van de thread dat het deeltje voor het laatst verplaatste.



2. GLOBALE STRUCTUUR VAN HET PROGRAMMA

Hieronder volgt een beschrijving van de globale structuur van het programma.

gdp.erichiram.partsim:
	Main		- Entry-punt van het programma; start ConfigurationReader, BlockingQueue, Gui en ThreadPool.
	ThreadPool	- Start of beëindigt Animation-threads op basis van P (het maximum aantal threads). 
	Gui			- Geeft het venster met het menu en het uitvoerpaneel weer en tekent de deeltjes.
	Animation	- Een subklasse van Thread, verplaatst elke ronde k deeltjes uit de queue en alle deeltjes die daarna nog overblijven.   
	Round		- Houdt gesynchroniseerd de huidige ronde bij zodat Animation-threads synchroon lopen.
	Particle	- Een deeltje, bevat X-, Y-, dX-, dY-properties en de move-method voor het verplaatsen van een deeltje.

gdp.erichiram.partsim.util:
	ConfigurationReader	- Leest een initiele deeltjesconfiguratie uit een bestand.
	BlockingQueue		- Een gesynchroniseerde queue-implementatie.



3. CONCURRENCY OF THE PROGRAM

"In the documentation we expect reasoning about the concurrency of your program. As you know, a program may contain concurrency control features and nevertheless be totally sequential in nature. Make sure that your reasoning is concise and clear."

The queue is added by the 

"Be sure to use the proper form of notify/notifyAll.  Only use notifyAll when notify is not sufficient.  Explain in your README file why your choice is correct."

3.x
NotifyAll

We gebruiken ŽŽn keer in het programma notifyAll, dat is omdat op het moment dat die aangroepen wordt alle min ŽŽn Animation threads aan het wachten zijn tot de volgende ronde begint. De laatste thread start de nieuwe rond en maakt alle threads weer wakker. Dit zorgt ervoor dat threads niet onnodig resources innemen als er niet meer te doen valt.


"Even better: give arguments why your program is correct, using the techniques taught in the lectures."



4. EXTRA LOAD BALANCING AND SYNCHRONIZATION

"Besides the basic behavior as explained above, you are allowed to do some nice and extra on the simulation of systems of particles and/or the load balancing and synchronization.  This may lead top a bonus on your grade (which will not exceed 10), but this bonus will only be assigned for something with regard to load balancing and/or synchronization.  Explain your approach in the documentation."