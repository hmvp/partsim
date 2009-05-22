Hiram van Paassen (HIRAM#)
Eric Broersma (ERIC#)


DE PARALLELLE DEELTJESSIMULATOR


0. INDEX

1. GLOBALE STRUCTUUR VAN HET PROGRAMMA
2. UITVOEREN VAN HET PROGRAMMA
	2.1 Parameters Instellen
	2.2 Deeltjes Toevoegen
	2.3 Deeltjes Verwijderen
	2.4 Interpreteren Van Het Uitvoerpaneel
3. CONCURRENCY VAN HET PROGRAMMA
	3.1 Round
	3.2 BlockingQueue
	3.3 Particle
	3.4 Gui
	3.5 ThreadPool
	3.6 Animation
4. EXTRA LOAD BALANCING EN SYNCHRONISATIE
	4.1 Efficiënt thread-management


1. GLOBALE STRUCTUUR VAN HET PROGRAMMA

Hieronder volgt een beschrijving van de globale structuur van het programma.

gdp.erichiram.partsim:
	Main		- Entry-punt van het programma; start ConfigurationReader, BlockingQueue, Gui en ThreadPool.
	Gui			- Geeft het venster met het menu en het uitvoerpaneel weer en tekent de deeltjes.
	ThreadPool	- Start of beëindigt Animation-threads op basis van P (het maximum aantal threads).
	Animation	- Een subklasse van Thread, verplaatst elke ronde k deeltjes uit de queue en alle deeltjes die daarna nog overblijven.   
	Particle	- Een deeltje, bevat X-, Y-, dX-, dY-properties en de move-method voor het verplaatsen van een deeltje.
	Round		- Een gesynchroniseerde integer-implementatie; wordt gebruikt om het rondenummer bij te houden.

gdp.erichiram.partsim.util:
	ConfigurationReader	- Leest een initiele deeltjesconfiguratie uit een bestand.
	BlockingQueue		- Een gesynchroniseerde queue-implementatie; wordt gebruikt als queue voor de Particle-objecten.

2. UITVOEREN VAN HET PROGRAMMA

$ javac src/*.java
$ java src/Main

Het bestand particles.txt met de initiele deeltjesconfiguratie wordt geladen. Dit bestand bevat op de eerste regel een getal N, gevolgd door N regels met informatie over de deeltjes. Deze regels zijn als volgt opgebouwd: "[naam] [X] [Y] [dX] [dY]", waarbij de naam een enkele (hoofd)letter is, en X, Y, dX en dY integers.

Vervolgens verschijnt een venster met een menu (zie 1.1 t/m 1.3) en een zwart uitvoerpaneel (zie 1.4).


2.1 Parameters Instellen

Met behulp van het bovenste deel van het menu kunnen parameters ingesteld worden die ervoor zorgen dat er threads worden opgestart die concurrent (zie 3) de deeltjes verplaatsen.

- Het "P"-veld bepaalt het maximum aantal threads (P) dat mag worden opgestart.
- Het "t"-veld bepaalt de slaaptijd (in milliseconden, t) van een thread per deeltje.
- Het "k"-veld bepaalt het aantal deeltjes (k) dat een thread wordt geacht per ronde te pakken.


2.2 Deeltjes Toevoegen

Met behulp van het middelste deel van het menu kunnen deeltjes worden toegevoegd aan de simulatie.

- Voeg een geparametriseerd deeltje toe door de gewenste waardes voor de positie (X en Y) en de snelheid (dX en dY) in te vullen en op de "Add new Particle"-knop te klikken.
- Voeg een willekeurig geparametriseerd deeltje toe door op de "Add new random Particle"-knop te klikken.


2.3 Deeltjes Verwijderen

Met behulp van het onderste deel van het menu kunnen deeltjes worden verwijderd uit de simulatie.

- Verwijder een deeltje door de naam van het deeltje te selecteren en op de "Remove Particle"-knop te klikken.


2.4 Interpreteren Van Het Uitvoerpaneel

In eerste instantie bevat het uitvoerpaneel mogelijk enkele niet-bewegende deeltjes, en worden de waardes voor het aantal actieve threads (p) en het aantal aanwezige deeltjes (n) weergegeven in de linkerbovenhoek van het paneel.

Zodra er door middel van het menu het maximum aantal threads (P) groter wordt dan 0, start het programma nieuwe threads op. De waarde van het aantal actieve threads (p) neemt toe en de aanwezige deeltjes zullen verplaatst worden door de actieve threads.

De kleur van een deeltje wordt bepaald door het ID van de thread dat het deeltje voor het laatst verplaatste.



3. CONCURRENCY VAN HET PROGRAMMA

Concurrency control van de Animation-threads vindt plaats in de Animation-klasse en met behulp van een globaal rondenummer dat wordt bijgehouden door middel van een Round-object in de Main-klasse. Dit interactie van dit laatste object met de Animation-threads zorgt ervoor dat de Animation-threads ronde voor ronde alle deeltjes behandelen. In de onderstaande paragrafen wordt deze interactie in meer detail uitgelegd. 

[TODO Argumenten voor concurrency in programma.]
[TODO Bewijs voor correctheid van programma, "using the techniques taught in the lectures".]


3.1 Round

De Main-klasse bevat een gesynchroniseerd Integer-object om het globale rondenummer bij te houden en op te hogen als dat mogelijk is.


3.2 BlockingQueue

BlockingQueue is een Queue-implementatie waarvan het toevoegen en verwijderen van elementen gesynchroniseerd is. In het programma bevat het BlockingQueue-object Main.q een queue met deeltjes. De deeltjes worden er door Animation-thread-objecten vanaf gepakt en verwerkt. Door middel van de synchronisatie kan het niet zo zijn dat twee Animation-threads hetzelfde deeltje te pakken krijgen en verwerken.


3.3 Particle

De methodes van de klasse Particle die X en Y schrijven en lezen (move en getParticle) zijn gesynchroniseerd zodat Animation- en Gui-threads elkaar niet in de weg zitten met gelijktijdig schrijven en lezen. Het interne rondenummer van ieder deeltje wordt opgehoogd bij het verplaatsen.


3.4 Gui

De constructor van de Main-klasse start een aparte Gui-thread op. Deze thread schrijft de waardes van de parameters in Main (k, t en ThreadPool.pMax) en leest de Particle-set uit om de deeltjes weer te geven op het uitvoerpaneel. 


3.5 ThreadPool

De constructor van de Main-klasse start een aparte ThreadPool-thread op. Zodra de waarde van pMax wordt aangepast vanuit de Gui-thread zal deze thread Animation-threads starten of manen om te stopppen (door Animation.finish aan te roepen).


3.6 Animation

Een actieve Animation-thread probeert gesynchroniseerd k deeltjes uit de Particle-queue (Main.q) te halen waarvan het interne rondenummer overeenkomt met het globale rondenummer. Als er minder dan k deeltjes in de queue zitten zal de thread al die deeltjes pakken.

Zodra de thread klaar is met het bemachtigen van deeltjes roept de thread Particle.move aan om ze te verplaatsen. Het interne rondenummer van ieder deeltje wordt opgehoogd bij het verplaatsen.

Na het verplaatsen worden de k deeltjes teruggeplaatst in de queue, mits het deeltje nog onderdeel is van de Particle-set (niet te verwarren met de Particle-queue) waar het mogelijk uit is verdwenen als gevolg van het verwijderen van deeltjes door de gebruiker.

Als er dan nog deeltjes voor deze ronden in de queue zitten, probeert de Animation-thread deze te pakken en verwerkt die ook, zo niet dan gaat de thread een nieuwe ronde aanvragen bij het Round-object. Als alle Animation-threads hun deeltjes hebben behandelt dan verhoogt het Round-object het globale rondenummer en worden alle threads weer wakker gemaakt door middel van Object.notifyAll. Hier is gekozen voor notifyAll omdat alle threads vanaf het begin van de nieuwe ronde weer mee mogen doen met het verplaatsen van deeltjes.



4. EXTRA LOAD BALANCING EN SYNCHRONISATIE

4.1 Efficiënt thread-management
De manier waarop het maken en stoppen van threads is geregeld is zeer efficiënt. Bovendien vermijd het race-condities waarbij meerdere threads tegelijkertijd proberen uit te vinden of ze moeten stoppen. [TODO Hoe dan? :-)]