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
	NetwProg
		- Entry-punt van het programma: Start de GUI, de routing table, en opent op basis van zijn parameters connecties naar anderen en gaat vervolgens luisteren naar inkomende connecties.
	RoutingTable
		- De routing table implementeert het netchange algoritme.
	Channel
		- Deze klasse is verantwoordelijk voor de connectie met een andere node. 
	Configuration
		- Statische klasse met configuratiedata.

gdp.erichiram.routable.gui:
	Gui
		- De GUI van het programma geeft informatie over de routing table en het aantal verzonden berichten, en geeft inputmogelijkheden.
	RoutingTableTableModel
		- Deze klasse is een TabelModel voor de tabel in de GUI. Hij observeert de routing table.

gdp.erichiram.routable.util:
	ObservableAtomicInteger
		- Wrapped AtomicInteger en inherit van Observable.



2. INTERACTIES VAN HET PROGRAMMA

Hieronder volgt een beschrijving van de interacties van de verschillende onderdelen van het programma.

NetwProg start de GUI, de routing table, en opent op basis van zijn parameters Channel-objecten voor connecties naar anderen en gaat vervolgens luisteren naar inkomende connecties. Voor een inkomende connectie wordt een nieuw Channel-object aangemaakt. 

GUI observeert de routing table en het aantal verzonden berichten, en update zichzelf zo vaak mogelijk. Wanneer de T-spinner wordt veranderd wordt de bijbehorende variabele aangepast, en wanneer er op de "Fail"- of "Change Weight/Repair"-knop wordt gedrukt, wordt er intern een bijpassend bericht (een Fail-, ChangeWeight- of Repair-bericht) verstuurd. Dat bericht wordt in de message queue van het RoutingTable-object geplaatst en daar verwerkt.

RoutingTable start een consumer-thread voor de message queue. Bij het verwerken van de berichten wordt het Netchange-algoritme uitgevoerd om de routing table te updaten.

Channel-objecten draaien in een eigen thread en luisteren naar berichten. Binnenkomende berichten (Fail, Repair of MyDist) worden in de message queue van het RoutingTable-object geplaatst en daar verwerkt.



3. UITVOEREN VAN HET PROGRAMMA

Voer de volgende commando's uit.

Onder Windows:

start java -jar NetwProg.jar 1103   1102  20   1100  2
start java -jar NetwProg.jar 1102   1101   3   1104 10   1103 100
start java -jar NetwProg.jar 1100   1104  10   1103  5
start java -jar NetwProg.jar 1104   1102   4   1101 12   1100   4
java -jar NetwProg.jar       1101   1102   7   1104 5

Onder Linux/Mac OS X:

java -jar NetwProg.jar 1103   1102  20   1100  2 &
java -jar NetwProg.jar 1102   1101   3   1104 10   1103 100 &
java -jar NetwProg.jar 1100   1104  10   1103  5 &
java -jar NetwProg.jar 1104   1102   4   1101 12   1100   4 &
java -jar NetwProg.jar 1101   1102   7   1104 5



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