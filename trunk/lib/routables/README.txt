Hiram van Paassen (HIRAM#)
Eric Broersma (ERIC#)



DE ROUTING TABLE 



0. INDEX

1. GLOBALE STRUCTUUR VAN HET PROGRAMMA
2. INTERACTIES VAN HET PROGRAMMA
3. UITVOEREN VAN HET PROGRAMMA
3.1 Parameters Instellen
3.2 Connectie Failen
3.3 Connectie Repairen of het gewicht van een connectie aanpassen
3.4 Aantal verzonden berichten en routing table
4. BEPERKEN VAN BLOKKEREN VAN THREADS



1. GLOBALE STRUCTUUR VAN HET PROGRAMMA

Hieronder volgt een beschrijving van de globale structuur van het programma.

gdp.erichiram.routable:
	NetwProg	- Entry-punt van het programma; Start de Gui, de Routingtable, opent eventueel connecties naar anderen en gaat vervolgens luisteren.
	Gui		- Gui van het programma geeft het venster met de gewenste informatie weer
	RoutingTable	- De Routing table implementeerd het netchange algoritme
	Channel		- Deze klasse is verantwoordelijk voor een connectie met een andere Node. 
	Configuration	- Statische klasse met configuratie data
	RoutingTableTableModel
			- Deze klasse is een tabel model voor de tabel in de gui, hij observeert de routingtabel en update zichzelf aan de hand daarvan
	
gdp.erichiram.routable.util:
	ObservableAtomicInteger
			- Deze klasse is niet veel meer dan een AtomicInteger die ook observable is
	JavaStarter	- Deze klasse is om vanuit eclipse makkelijk x processen te starten met een bepaalde configuratie



2. INTERACTIES VAN HET PROGRAMMA

Hieronder volgt een beschrijving van de interacties van de verschillende onderdelen van het programma.

// ...



3. UITVOEREN VAN HET PROGRAMMA

Voer het volgende commando uit:

//java -jar Routables.jar



4. INTERFACE VAN HET PROGRAMMA

3.1 T-Parameter Instellen

Met behulp van het bovenste deel van het menu kunnen parameters ingesteld worden die ervoor zorgen dat er threads worden opgestart die concurrent (zie 3) de deeltjes verplaatsen.

- Het "t"-veld bepaalt de slaaptijd (in milliseconden, t) van een thread per deeltje.


3.2 Connectie Failen

Met het behulp van de "Fail"-knop kan een connectie Failed (vernietigd) worden.

- Selecteer een ID van een node (alleen nodes waar een bestaande connectie mee bestaat zijn selecteerbaar). Wanneer op de "Fail"-knop wordt gedrukt, ontvangen beide nodes een Fail-bericht voor de connectie.


3.3 Connectie Repairen of het gewicht van een connectie aanpassen

Met behulp van de "Change Weight/Repair"-knop kan een connectie Repaired (aangemaakt) worden, of het gewicht van een bestaande connectie aangepast worden.

- Selecteer een ID van een node in de spinner rechts van de knop, en geef een gewicht op in de spinner daar rechts van. Wanneer op de "Change Weight/Repair"-knop wordt gedrukt, wordt een nieuwe connectie met het opgegeven gewicht Repaired (aangemaakt) als de connectie nog niet bestond: er wordt een Repair-bericht gestuurd naar beide nodes. Als de connectie wel bestond, wordt het gewicht van de connectie aangepast naar het opgegeven gewicht: er wordt een "intern" ChangeWeight-bericht gestuurd naar de huidige node.


3.4 Aantal verzonden berichten en routing table

"Number of sent messages" geeft aan hoeveel berichten er door dit programma zijn verzonden.

De tabel laat de voor elke bekende node zien wat de geprefereerde buurman voor die node is ("Preferred neighbour"), en wat de lengte is van het kortste pad naar die node is ("|Shortest path|").



4. BEPERKEN VAN BLOKKEREN VAN THREADS

De RoutingTable en de verschillende Channels vormen een "Single Consumer/Multiple Producer"-structuur.

RoutingTable heeft een BlockingQueue, geïmplementeerd door een LinkedBlockingQueue. Channel-threads kunnen onbeperkt en wachtvrij Message-objecten in deze queue plaatsen, en een consumer-thread in RoutingTable verwerkt op zijn beurt deze berichten.

De RoutingTable-consumer-thread zal weliswaar blokkeren wanneer er geen berichten in de queue staan, maar de Channel-threads zullen op deze manier nooit meer blocken bij het afleveren van een bericht omdat RoutingTable bezig was met een eerder bericht te verwerken. In een opzet waar de berichtverwerkende methodes van RoutingTable met elkaar gesynchroniseerd waren door middel van een monitor, bestond er wel een kans dat de Channel-threads blokkeerden in afwachting van de mogelijkheid om het volgende bericht te mogen afleveren.