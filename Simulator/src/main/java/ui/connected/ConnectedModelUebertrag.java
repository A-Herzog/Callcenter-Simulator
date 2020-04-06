/**
 * Copyright 2020 Alexander Herzog
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package ui.connected;

import java.util.HashMap;
import java.util.Iterator;
import java.util.Set;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;

import language.Language;
import mathtools.NumberTools;

/**
 * Gibt an, wie viele am Vortag endgültig abgebrochene Kunden eines bestimmten Typs am aktuellen Tag einen erneuten Anlauf starten.
 * @author Alexander Herzog
 * @version 1.0
 * @see ConnectedModel#uebertrag
 */
public final class ConnectedModelUebertrag {
	/**
	 * Wahrscheinlichkeit, mit der Kunden einen weiteren Versuch starten
	 */
	public double probability;

	/**
	 * Gibt an, mit welchen Raten sich der Kundentyp bei einem Neuanruf von Kunden des jeweiligen Typs ändert.
	 */
	public HashMap<String,Double> changeRates;

	/**
	 * Konstruktor der Klasse <code>ConnectedModelUebertrag</code>
	 * Initialisiert <code>probability</code> mit dem im Parameter angegebenen Wert
	 * @param probability Initialer Wert für das gleichnamige Feld
	 */
	public ConnectedModelUebertrag(double probability) {
		this.probability=probability;
		changeRates=new HashMap<String, Double>();
	}

	/**
	 * Konstruktor der Klasse <code>ConnectedModelUebertrag</code>
	 */
	public ConnectedModelUebertrag() {
		this(0);
	}

	/**
	 * Vergleicht zwei <code>ConnectedModelUebertrag</code>-Objekte
	 * @param uebertrag Zweites <code>ConnectedModelUebertrag</code>-Objekte mit dem das aktuelle Objekt verglichen werden soll.
	 * @return	Gibt <code>true</code> zurück, wenn die beiden Objekte inhaltlich identisch sind.
	 */
	public boolean equalsConnectedModelUebertrag(ConnectedModelUebertrag uebertrag) {
		if (probability!=uebertrag.probability) return false;

		if (uebertrag.changeRates.size()!=changeRates.size()) return false;

		Set<String> s1=changeRates.keySet();
		Set<String> s2=uebertrag.changeRates.keySet();

		if (!s1.containsAll(s2) || !s2.containsAll(s1)) return false;

		Iterator<String> it=s1.iterator();
		while (it.hasNext()) {
			String s=it.next();
			double d1=uebertrag.changeRates.get(s);
			double d2=changeRates.get(s);
			if (d1!=d2) return false;
		}

		return true;
	}

	/**
	 * Erstellt eine Kopie des aktuellen Objekts.
	 * @return Kopiertes <code>ConnectedModelUebertrag</code>-Objekt
	 */
	public ConnectedModelUebertrag cloneUebertrag() {
		ConnectedModelUebertrag uebertrag=new ConnectedModelUebertrag(probability);
		Iterator<String> it=changeRates.keySet().iterator();
		while (it.hasNext()) {
			String key=it.next();
			uebertrag.changeRates.put(key,changeRates.get(key));
		}
		return uebertrag;
	}

	/**
	 * Lädt die Einstellungen für das aktuelle <code>ConnectedModelUebertrag</code>-Objekt aus einem XML-Node.
	 * @param node	XML-Node, aus dem die Daten geladen werden sollen.
	 * @return	In den XML-Node ist als Attribut der Kundentypenname enthalten. Dieser wird nicht in dem <code>ConnectedModelUebertrag</code>-Element selbst gespeichert und daher hier zur weiteren Verwendung zurückgegeben.
	 */
	public String loadFromXML(Element node) {
		final HashMap<String,Double> changeRatesNew=new HashMap<String,Double>();

		/* Allgemeine Übertragsrate */
		Double D=NumberTools.getSystemProbability(Language.trAllAttribute("XML.Connected.CarryOver.Value",node)); if (D==null) return null;
		probability=D;

		/* Kundentypänderungen */
		NodeList l=node.getChildNodes();
		for (int i=0; i<l.getLength();i++) {
			if (!(l.item(i) instanceof Element)) continue;
			Element e=(Element)l.item(i);
			if (!Language.trAll("XML.Connected.CarryOver.ClientType",e.getNodeName())) continue;
			D=NumberTools.getNotNegativeDouble(NumberTools.systemNumberToLocalNumber(Language.trAllAttribute("XML.Connected.CarryOver.Value",e))); if (D==null) return null;
			changeRatesNew.put(Language.trAllAttribute("XML.Connected.CarryOver.Type",node),D);
		}
		changeRates=changeRatesNew;

		/* Im Erfolgsfall Name des Kundentyps zurückgeben */
		return Language.trAllAttribute("XML.Connected.CarryOver.Type",node);
	}

	/**
	 * Speichert den Inhalt des aktuellen <code>ConnectedModelUebertrag</code>-Elements in einem neuen XML-Node.
	 * @param parent	XML-Elternelement, in dem der neuer XML-Node zur Speicherung der Daten angelegt werden soll.
	 * @param name	Name des Kundentyps, auf den sich die <code>ConnectedModelUebertrag</code>-Daten beziehen.
	 */
	public void saveToXML(Element parent, String name) {
		Document doc=parent.getOwnerDocument();
		Element node;

		parent.appendChild(node=doc.createElement(Language.tr("XML.Connected.CarryOver.Single")));
		node.setAttribute(Language.trPrimary("XML.Connected.CarryOver.Type"),name);

		/* Allgemeine Übertragsrate */
		node.setAttribute(Language.trPrimary("XML.Connected.CarryOver.Value"),NumberTools.formatSystemNumber(probability));

		/* Kundentypänderungen */
		Iterator<String> it=changeRates.keySet().iterator();
		while (it.hasNext()) {
			String newType=it.next();
			Element sub; node.appendChild(sub=doc.createElement(Language.trPrimary("XML.Connected.CarryOver.ClientType")));
			sub.setAttribute(Language.trPrimary("XML.Connected.CarryOver.Type"),newType);
			sub.setAttribute(Language.trPrimary("XML.Connected.CarryOver.Value"),NumberTools.formatSystemNumber(changeRates.get(newType)));
		}
	}
}