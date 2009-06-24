Hiram van Paassen (HIRAM#)
Eric Broersma (ERIC#)



DE ROUTING TABLE 


0. INDEX



1. GLOBALE STRUCTUUR VAN HET PROGRAMMA







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


2. UITVOEREN VAN HET PROGRAMMA

Voer het volgende commando uit:

java -jar Routables.jar 