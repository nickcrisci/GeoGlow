# GeoGlow
Im Modul MODI (Mobile and Distributed (interactive) Systems) im Sommersemester 2024 werden mobile und verteilte Systeme entwickelt.

Die Idee von GeoGlow ist es, ein Gefühl für den Standort eines (später auch mehrerer) Freundes zu vermitteln.
Dabei besitzt jeder Freund bei sich zu Hause NanoLeafs. Ist nun einer der Freunde unterwegs, so kann dieser mithilfe einer App ein Foto übertragen. Aus diesem Foto werden dann, abhängig von der Anzahl an angeschlossenen NanoLeafs des Freundes, die am stärksten vertretenen Farben herausgearbeitet.
Anschließend können diese Farben verwendet werden, um die NanoLeafs des Freundes in genau diesen Farben leuchten zu lassen.

Somit kann der Freund die Umgebung des anderen Freundes mithilfe von Licht wahrnehmen und ein Stückchen mehr am Leben des Freundes teilhaben.

## Architektur
_Im Folgenden ist die grobe (erste) Architektur beschrieben._

Um das Ganze wie geplant umzusetzen, sind verschiedene Komponenten vonnöten:

- NanoLeafs: Die NanoLeaf Lichtpanels werden verwendet, um die Farben der Umgebung des Freundes darzustellen.
- Microcontroller: Da die NanoLeaf API nur aus demselben Netzwerk wie das, mit dem die Panels verbunden sind, erreichbar ist, wird ein Microcontroller bei jedem Freund benötigt. Dieser ist dafür zuständig, die Farben und wichtige Informationen zwischen den Panels und der restlichen Anwendung zu kommunizieren.
- Friend-Service: (Name könnte sich noch ändern) Dieser Service wird im Kontext des Moduls lediglich Informationen zwischenspeichern und weiterleiten. Die vorgegebene Struktur soll allerdings einfach erweiterbar sein, damit dieser Service später um weitere Funktionalität erweitert werden kann. Die Idee liegt darin, dass dieser Service Informationen darüber hält, welche Controller (und somit Freunde) erreichbar sind, welche Panele erreichbar sind (falls ein Freund mehrere besitzt) und welcher Freund mit wem eigentlich kommunizieren darf. Es ist denkbar, dass in einer späteren Version des Projektes eine Art soziales Netzwerk entsteht. In diesem müssen Freunde anfragen, ob diese mit anderen Kommunizieren dürfen. Diese Berechtigungen werden dann von dem Friend-Service verwaltet.
- App: Zu guter Letzt wird noch eine App benötigt, welche das Erstellen und Versenden von Fotos ermöglicht. Im Projekt wird eine 1 zu 1 Kommunikation prototypisch aufgebaut, allerdings soll die Architektur der App es ermöglichen, dies später einfach zu erweitern. Geplant ist es, dass sich in späteren Versionen ganze **Freundesgruppen** miteinander verbinden können. Dabei könnte es sogar möglich sein zu konfigurieren, welche Lichtpanele für welchen Freund stehen (somit könnte eine Anordnung für mehrere Freunde genutzt werden).