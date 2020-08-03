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
package ui.optimizer;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;

import language.Language;
import mathtools.NumberTools;
import simulator.Statistics;
import xml.XMLTools;

/**
 * Kapselt die Optimierungsergebnisse
 * @author Alexander Herzog
 * @version 1.0
 */
public final class OptimizeData {
	/**
	 * Liste der Simulationsläufe innerhalb des Optimierungsprozesses
	 */
	public List<Statistics> data=new ArrayList<Statistics>();

	/**
	 * Optimierer-Einstellungen, die für den Optimierungsprozess verwendet wurden
	 */
	public OptimizeSetup setup;

	/**
	 * Anzahl der Simulationsläufe (insgesamt, nicht nur die gespeicherten Läufe)
	 */
	public int runCount=0;

	/**
	 * Laufzeit des gesamten Optimierungsprozesses gemessen in Sekunden
	 */
	public int runTime=0;

	/**
	 * Mögliche Namen des Basiselement von Optimierer-Daten-XML-Dateien (zur Erkennung von Dateien dieses Typs.)
	 */
	public static final String[] XMLBaseElement=Language.trAll("XML.OptimizerResults");

	/**
	 * Konstruktor der Klasse <code>OptimizeData</code>
	 * @param setup Objekt vom Typ <code>OptimizeSetup</code>, welches im <code>setup</code>-Datenfeld gespeichert werden soll.
	 */
	public OptimizeData(OptimizeSetup setup) {
		this.setup=setup;
	}

	/**
	 * Konstruktor der Klasse <code>OptimizeData</code>
	 */
	public OptimizeData() {
		this(null);
	}

	/**
	 * Versucht die kompletten Optimierer-Daten aus der angegebenen XML-Datei zu laden
	 * @param file	Dateiname der XML-Datei, aus der die Optimierer-Daten geladen werden soll
	 * @return	Tritt ein Fehler auf, so wird die Fehlermeldung als String zurückgegeben. Im Erfolgsfall wird <code>null</code> zurückgegeben.
	 */
	public String loadFromFile(File file) {
		if (file==null) return Language.tr("Optimizer.NoFileSelected");
		if (!file.exists()) return String.format(Language.tr("Optimizer.FileNotFound"),file.toString());

		XMLTools xml=new XMLTools(file);
		Element root=xml.load();
		if (root==null) return xml.getError();
		return loadFromXML(root);
	}

	/**
	 * Versucht ein die Optimierer-Daten aus dem übergebenen XML-Node zu laden
	 * @param node	XML-Knoten, der die Optimierer-Daten enthält
	 * @return	Tritt ein Fehler auf, so wird die Fehlermeldung als String zurückgegeben. Im Erfolgsfall wird <code>null</code> zurückgegeben.
	 */
	public String loadFromXML(Element node) {
		if (!Language.trAll("XML.OptimizerResults",node.getNodeName())) return String.format(Language.tr("XML.OptimizerResults.Error"),Language.trPrimary("XML.OptimizerResults"));

		data.clear();
		setup=null;
		runTime=0;
		runCount=1;

		NodeList l=node.getChildNodes();
		for (int i=0; i<l.getLength();i++) {
			if (!(l.item(i) instanceof Element)) continue;
			Element e=(Element)l.item(i);
			String s=e.getNodeName();

			if (Language.trAll("XML.OptimizerResults.RunTime",s)) {
				Integer in=NumberTools.getNotNegativeInteger(e.getTextContent());
				if (in==null) return String.format(Language.tr("Optimizer.LoadError.Runtime"),e.getTextContent());
				runTime=in;
				continue;
			}

			if (Language.trAll("XML.OptimizerResults.RunCount",s)) {
				Integer in=NumberTools.getNotNegativeInteger(e.getTextContent());
				if (in==null) return String.format(Language.tr("Optimizer.LoadError.NumberOfSimulations"),e.getTextContent());
				runCount=in;
				continue;
			}

			if (Language.trAll("XML.OptimizerResults.SimulationRun",s)) {
				String u=Language.trAllAttribute("XML.OptimizerResults.SimulationRun.Number",e);
				Integer in=NumberTools.getNotNegativeInteger(u);
				if (in==null) return String.format(Language.tr("Optimizer.LoadError.RunNumber"),u);
				while (data.size()<in) data.add(null);
				Statistics statistics=new Statistics(null,null,0,0);
				NodeList l2=e.getChildNodes();
				for (int j=0; j<l2.getLength();j++) {
					if (!(l2.item(j) instanceof Element)) continue;
					Element e2=(Element)l2.item(j);
					if (Language.trAll("XML.Statistic.BaseElement",e2.getNodeName())) {
						String t=statistics.loadFromXML(e2);
						if (t!=null) return t;
						data.set(in-1,statistics);
						break;
					}
					continue;
				}
			}

			if (Language.trAll("XML.OptimizerSetup",s)) {
				setup=new OptimizeSetup();
				String t=setup.loadFromXML(e);
				if (t!=null) return t;
				continue;
			}
		}

		for (int i=0;i<data.size();i++) if (data.get(i)==null) return String.format(Language.tr("Optimizer.LoadError.MissingData"),i+1);
		return null;
	}

	/**
	 * Speichert die kompletten Optimierer-Daten in der angegebenen XML-Datei.
	 * @param file	Dateiname der Datei, in der die Optimierer-Daten gespeichert werden sollen
	 * @return	Gibt an, ob die Daten erfolgreich gespeichert werden konnten.
	 */
	public boolean saveToFile(File file) {
		XMLTools xml=new XMLTools(file);
		Element root=xml.generateRoot(Language.trPrimary("XML.OptimizerResults"));
		if (root==null) return false;
		addDataToXML(root);
		return xml.save(root);
	}

	/**
	 * Erstellt unterhalb des übergebenen XML-Knotens einen neuen Knoten, der die gesamten Optimierer-Daten enthält.
	 * @param parent	Eltern-XML-Knoten
	 */
	public void saveToXML(Element parent) {
		Document doc=parent.getOwnerDocument();
		Element node=doc.createElement(Language.trPrimary("XML.OptimizerResults")); parent.appendChild(node);
		addDataToXML(node);
	}

	private void addDataToXML(Element node) {
		Document doc=node.getOwnerDocument();
		Element e;

		node.appendChild(e=doc.createElement(Language.trPrimary("XML.OptimizerResults.RunTime"))); e.setTextContent(""+runTime);
		node.appendChild(e=doc.createElement(Language.trPrimary("XML.OptimizerResults.RunCount"))); e.setTextContent(""+runCount);
		setup.saveToXML(node);
		for(int i=0;i<data.size();i++) {
			node.appendChild(e=doc.createElement(Language.trPrimary("XML.OptimizerResults.SimulationRun")));
			e.setAttribute(Language.trPrimary("XML.OptimizerResults.SimulationRun.Number"),""+(i+1));
			data.get(i).saveToXML(e,true);
		}
	}
}
