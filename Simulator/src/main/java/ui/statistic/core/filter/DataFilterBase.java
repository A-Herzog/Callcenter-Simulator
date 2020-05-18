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
package ui.statistic.core.filter;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;
import java.util.Scanner;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;

import language.Language;
import mathtools.NumberTools;
import mathtools.TimeTools;
import mathtools.distribution.DataDistributionImpl;
import parser.CalcSystem;
import parser.MathCalcError;
import parser.MathParser;
import simulator.Statistics;

/**
 * Basisklasse zur Filterung von XML-Daten
 * @author Alexander Herzog
 */
public class DataFilterBase {
	private final Document xmlDoc;
	private StringBuilder results;
	private int simDays=1;

	private final List<String> commandNames;
	private final List<DataFilterCommand> commandObjects;
	private final List<Boolean> commandAllowEmptyParameters;

	/**
	 * Liste der Namen der momentan belegten Variablen
	 */
	protected final List<String> varNames;

	/**
	 * Liste der Werte der momentan belegten Variablen
	 */
	protected final List<String> varValues;

	/**
	 * Konstruktor der Klasse <code>DataFilterBase</code>
	 * @param xmlDoc XML-Dokument, aus dem die Daten entnommen werden sollen
	 * @param readSimDays	Anzahl an zu simulierenden Tagen aus dem XML-Dokument mit auslesen
	 */
	public DataFilterBase(Document xmlDoc, boolean readSimDays) {
		varNames=new ArrayList<String>();
		varValues=new ArrayList<String>();
		this.xmlDoc=xmlDoc;
		results=new StringBuilder();
		commandNames=new ArrayList<String>();
		commandObjects=new ArrayList<DataFilterCommand>();
		commandAllowEmptyParameters=new ArrayList<Boolean>();
		if (readSimDays) simDays=readSimDays(xmlDoc);
	}

	/**
	 * Ließt aus einer XML-Statistikdatei aus, wie viele Tage simuliert wurde.
	 * @param xmlDoc	Datenobjekt der XML-Datei
	 * @return	Anzahl an simulierten Tagen
	 */
	public static int readSimDays(Document xmlDoc) {
		if (xmlDoc==null) return 1;
		Element node=xmlDoc.getDocumentElement();
		boolean ok=false;
		for (String s : Statistics.XMLBaseElement)	if (node.getNodeName().equalsIgnoreCase(s)) {ok=true; break;}
		if (!ok) return 1;
		NodeList l=node.getChildNodes();
		for (int i=0; i<l.getLength();i++) {
			if (!(l.item(i) instanceof Element)) continue;
			Element e=(Element)l.item(i);
			String s=e.getNodeName();

			if (Language.trAll("XML.Statistic.Info.SimulatedDays",s)) {
				Integer J=NumberTools.getNotNegativeInteger(e.getTextContent());
				if (J!=null) return J;
			}
		}
		return 1;
	}

	/**
	 * Fügt einen Befehl zur Liste der bekannten Befehle hinzu
	 * @param commandRunner	Objekt, welches die Ausführung übernehmen soll
	 */
	protected final void registerCommand(DataFilterCommand commandRunner) {
		commandNames.add(commandRunner.getName());
		commandObjects.add(commandRunner);
		commandAllowEmptyParameters.add(commandRunner.allowEmptyParameters());
	}

	private String removeComments(String line) {
		if (line.indexOf("//")==-1) return line;
		boolean inText=false;
		boolean lastWasSlash=false;
		for (int i=0;i<line.length();i++) {
			if (line.charAt(i)=='"') inText=!inText;
			if (!inText && line.charAt(i)=='/') {
				if (lastWasSlash) return line.substring(0,i-1);
				lastWasSlash=true;
			} else lastWasSlash=false;
		}
		return line;
	}

	private static String[] needsDiv={
			Language.trPrimary("XML.Statistic.Clients")+","+Language.trPrimary("XML.Statistic.Clients.Count")+",*",
			Language.trPrimary("XML.Statistic.Clients")+","+Language.trPrimary("XML.Statistic.Clients.WaitingTime.Clients")+",*",
			Language.trPrimary("XML.Statistic.Clients")+","+Language.trPrimary("XML.Statistic.Clients.HoldingTime.Clients")+",*",
			Language.trPrimary("XML.Statistic.Clients")+","+Language.trPrimary("XML.Statistic.Clients.CancelTime.Clients")+",*",
			Language.trPrimary("XML.Statistic.Clients")+","+Language.trPrimary("XML.Statistic.Clients.ServiceLevel.Clients")+",*",
			Language.trPrimary("XML.Statistic.Clients")+","+Language.trPrimary("XML.Statistic.Clients.Forwarding.Clients")+",*",
			Language.trPrimary("XML.Statistic.Clients")+","+Language.trPrimary("XML.Statistic.Clients.Retry.Clients")+",*",
			Language.trPrimary("XML.Statistic.Clients")+","+Language.trPrimary("XML.Statistic.Clients.Recall.Clients")+",*",
			Language.trPrimary("XML.Statistic.Clients")+","+Language.trPrimary("XML.Statistic.Clients.WaitingTimeDistribution.Clients")+","+Language.trPrimary("XML.Statistic.GeneralAttributes.Distribution"),
			Language.trPrimary("XML.Statistic.Clients")+","+Language.trPrimary("XML.Statistic.Clients.HoldingTimeDistribution.Clients")+","+Language.trPrimary("XML.Statistic.GeneralAttributes.Distribution"),
			Language.trPrimary("XML.Statistic.Clients")+","+Language.trPrimary("XML.Statistic.Clients.CancelTimeDistribution.Clients")+","+Language.trPrimary("XML.Statistic.GeneralAttributes.Distribution"),
			Language.trPrimary("XML.Statistic.Clients")+","+Language.trPrimary("XML.Statistic.Clients.WaitingTimeDistributionLong.Clients")+","+Language.trPrimary("XML.Statistic.GeneralAttributes.Distribution"),
			Language.trPrimary("XML.Statistic.Clients")+","+Language.trPrimary("XML.Statistic.Clients.HoldingTimeDistributionLong.Clients")+","+Language.trPrimary("XML.Statistic.GeneralAttributes.Distribution"),
			Language.trPrimary("XML.Statistic.Clients")+","+Language.trPrimary("XML.Statistic.Clients.CancelTimeDistributionLong.Clients")+","+Language.trPrimary("XML.Statistic.GeneralAttributes.Distribution"),
			Language.trPrimary("XML.Statistic.Clients")+","+Language.trPrimary("XML.Statistic.Calls.Count")+",*",
			Language.trPrimary("XML.Statistic.Clients")+","+Language.trPrimary("XML.Statistic.Clients.WaitingTime.Calls")+",*",
			Language.trPrimary("XML.Statistic.Clients")+","+Language.trPrimary("XML.Statistic.Clients.HoldingTime.Calls")+",*",
			Language.trPrimary("XML.Statistic.Clients")+","+Language.trPrimary("XML.Statistic.Clients.CancelTime.Calls")+",*",
			Language.trPrimary("XML.Statistic.Clients")+","+Language.trPrimary("XML.Statistic.Clients.ServiceLevel.Calls")+",*",
			Language.trPrimary("XML.Statistic.Clients")+","+Language.trPrimary("XML.Statistic.Clients.Forwarding.Calls")+",*",
			Language.trPrimary("XML.Statistic.Clients")+","+Language.trPrimary("XML.Statistic.Clients.Retry.Calls")+",*",
			Language.trPrimary("XML.Statistic.Clients")+","+Language.trPrimary("XML.Statistic.Clients.WaitingTimeDistribution.Calls")+","+Language.trPrimary("XML.Statistic.GeneralAttributes.Distribution"),
			Language.trPrimary("XML.Statistic.Clients")+","+Language.trPrimary("XML.Statistic.Clients.HoldingTimeDistribution.Calls")+","+Language.trPrimary("XML.Statistic.GeneralAttributes.Distribution"),
			Language.trPrimary("XML.Statistic.Clients")+","+Language.trPrimary("XML.Statistic.Clients.CancelTimeDistribution.Calls")+","+Language.trPrimary("XML.Statistic.GeneralAttributes.Distribution"),
			Language.trPrimary("XML.Statistic.Clients")+","+Language.trPrimary("XML.Statistic.Clients.WaitingTimeDistributionLong.Calls")+","+Language.trPrimary("XML.Statistic.GeneralAttributes.Distribution"),
			Language.trPrimary("XML.Statistic.Clients")+","+Language.trPrimary("XML.Statistic.Clients.HoldingTimeDistributionLong.Calls")+","+Language.trPrimary("XML.Statistic.GeneralAttributes.Distribution"),
			Language.trPrimary("XML.Statistic.Clients")+","+Language.trPrimary("XML.Statistic.Clients.CancelTimeDistributionLong.Calls")+","+Language.trPrimary("XML.Statistic.GeneralAttributes.Distribution"),
			Language.trPrimary("XML.Statistic.Clients")+","+Language.trPrimary("XML.Statistic.Costs")+",*",
			Language.trPrimary("XML.Statistic.Agents")+","+Language.trPrimary("XML.Statistic.Agents.Summary")+",*",
			Language.trPrimary("XML.Statistic.Agents")+","+Language.trPrimary("XML.Statistic.Agents.ClientType")+","+Language.trPrimary("XML.Statistic.Agents.Summary.HoldingTime"),
			Language.trPrimary("XML.Statistic.Agents")+","+Language.trPrimary("XML.Statistic.Agents.ClientType")+","+Language.trPrimary("XML.Statistic.Agents.Summary.HoldingTimePerInterval"),
			Language.trPrimary("XML.Statistic.Agents")+","+Language.trPrimary("XML.Statistic.Agents.ClientType")+","+Language.trPrimary("XML.Statistic.Agents.Summary.PostProcessingTime"),
			Language.trPrimary("XML.Statistic.Agents")+","+Language.trPrimary("XML.Statistic.Agents.ClientType")+","+Language.trPrimary("XML.Statistic.Agents.Summary.PostProcessingTimePerInterval"),
			Language.trPrimary("XML.Statistic.Agents")+","+Language.trPrimary("XML.Statistic.Agents.ClientType")+","+Language.trPrimary("XML.Statistic.Agents.Summary.TechnicalFreeTime"),
			Language.trPrimary("XML.Statistic.Agents")+","+Language.trPrimary("XML.Statistic.Agents.ClientType")+","+Language.trPrimary("XML.Statistic.Agents.Summary.TechnicalFreeTimePerInterval")
	};

	private static String[] doNotFormat={
			Language.trPrimary("XML.Model.BaseElement")+","+Language.trPrimary("XML.Model.Name"),
			Language.trPrimary("XML.Model.BaseElement")+","+Language.trPrimary("XML.Model.Version"),
			Language.trPrimary("XML.Model.BaseElement")+","+Language.trPrimary("XML.Model.Description"),
			Language.trPrimary("XML.Statistic.Info.RunDate"),
			Language.trPrimary("XML.Statistic.Info.User"),
			Language.trPrimary("XML.Statistic.Info.Server"),
			Language.trPrimary("XML.Statistic.Info.ServerOS")
	};

	private static boolean simDaysCheck(List<String> path) {
		if (path==null || path.size()==0) return false;
		for (int i=0;i<needsDiv.length;i++) {
			String[] s=needsDiv[i].split(",");
			if (s.length!=path.size()) continue;
			boolean b=true;
			for (int j=0;j<Math.min(s.length,path.size());j++) if (!s[j].equalsIgnoreCase(path.get(j)) && !s[j].equals("*")) {b=false; break;}
			if (b) return true;
		}
		return false;
	}

	private static boolean doNotFormatCheck(List<String> path) {
		if (path==null || path.size()==0) return false;
		for (int i=0;i<doNotFormat.length;i++) {
			String[] s=doNotFormat[i].split(",");
			if (s.length!=path.size()) continue;
			boolean b=true;
			for (int j=0;j<Math.min(s.length,path.size());j++) if (!s[j].equalsIgnoreCase(path.get(j)) && !s[j].equals("*")) {b=false; break;}
			if (b) return true;
		}
		return false;
	}

	/**
	 * Interpretiert eine aus einem XML-Element/Attribut ausgelesene Zahl
	 * @param value	Zu interpretierender Zahlenwert
	 * @param path	Zugehöriger XML-Pfad (darüber wird erkannt, ob es sich z.B. um einen Gesamtwert, der noch durch die Anzahl an Wiederholungen dividiert werden muss, handelt usw.)
	 * @param simDays	Anzahl an Wiederholungen des Simulationstags
	 * @param systemNumbers	Sollen Zahlen in System-Notation ausgegeben werden? (per vorherigen Skriptbefehlen vorgenommene Konfiguration)
	 * @param percent	Sollen Zahlen als Prozentangaben ausgegeben werden? (per vorherigen Skriptbefehlen vorgenommene Konfiguration)
	 * @param time	Soll der Wert als Zeitangabe ausgegeben werden? (per vorherigen Skriptbefehlen vorgenommene Konfiguration)
	 * @param distributionSeparator	Zeichen zur Trennung von Einträgen in empirischen Verteilungen
	 * @return	Entsprechend formatierter Wert
	 */
	public static String formatNumber(String value, List<String> path, int simDays, boolean systemNumbers, boolean percent, boolean time, char distributionSeparator) {
		if (doNotFormatCheck(path)) return value;

		boolean needDiv=(path==null)?false:simDaysCheck(path);
		if (!needDiv && systemNumbers) return value;

		if (value.indexOf(';')>=0) {
			/* Verteilung */
			DataDistributionImpl dist=DataDistributionImpl.createFromString(value,1000);
			if (dist==null) return value;
			if (needDiv) dist=dist.divide(simDays);
			if (systemNumbers) return dist.storeToString(Character.toString(distributionSeparator)); else return dist.storeToLocalString(Character.toString(distributionSeparator));
		}

		Double D=null;
		if (value.indexOf(':')>=0) {
			/* Zeitangabe */
			D=TimeTools.getExactTime(value);
		} else {
			/* Einzelwert */
			D=NumberTools.getExtProbability(NumberTools.systemNumberToLocalNumber(value));
		}

		if (D==null) return value;
		if (needDiv) D=D/simDays;
		String suffix="";
		if (time) {
			if (systemNumbers) return TimeTools.formatExactSystemTime(D); else return TimeTools.formatExactTime(D);
		} else {
			if (percent) {D=D*100; suffix="%";}
			if (systemNumbers) return NumberTools.formatSystemNumber(D)+suffix; else return NumberTools.formatNumberMax(D)+suffix;
		}
	}

	/**
	 * Formatiert einen Wert und gibt ihn wieder als String zurück
	 * @param value	Eingabewert; kann ein Text, eine Zahl oder auch eine Verteilung sein
	 * @param systemNumbers	In System- oder lokaler Notation
	 * @param percent	Als Prozentwert oder normale Fließkommazahl
	 * @param time	Als Zeitangabe oder als Zahl
	 * @param distributionSeparator	Trenner für die Einträge bei Verteilungen
	 * @return	Rückgabe des Wertes in entsprechender Formatierung
	 */
	protected String formatNumber(String value, boolean systemNumbers, boolean percent, boolean time, char distributionSeparator) {
		return formatNumber(value,null,simDays,systemNumbers,percent,time,distributionSeparator);
	}

	/**
	 * Formatiert einen Wert und gibt ihn wieder als String zurück
	 * @param value	Eingabewert
	 * @param systemNumbers	In System- oder lokaler Notation
	 * @param percent	Als Prozentwert oder normale Fließkommazahl
	 * @param time	Als Zeitangabe oder als Zahl
	 * @param distributionSeparator	Trenner für die Einträge bei Verteilungen
	 * @return	Rückgabe des Wertes in entsprechender Formatierung
	 */
	protected String formatNumber(double value, boolean systemNumbers, boolean percent, boolean time, char distributionSeparator) {
		return formatNumber(NumberTools.formatSystemNumber(value),null,simDays,systemNumbers,percent,time,distributionSeparator);
	}

	private String[] findElement(Scanner selectors, Element parent, List<String> parentTags, boolean systemNumbers, boolean percent, boolean time, char distributionSeparator) {
		/* Selektor dekodieren */
		String sel=selectors.next();
		String tag=sel, attr="", attrValue="";
		int attrNr=-1;
		int index=sel.indexOf('[');
		if (index>=0) {
			if (!sel.endsWith("]")) return new String[]{Language.tr("Statistic.Filter.InvalidSelector")+" ("+sel+")",null};
			attr=sel.substring(index+1,sel.length()-1).trim();
			tag=sel.substring(0,index).trim();
			if (attr.isEmpty()) return new String[]{Language.tr("Statistic.Filter.InvalidSelector")+" ("+sel+")",null};
			index=attr.indexOf('=');
			if (index>=0) {
				attrValue=attr.substring(index+1).trim();
				attr=attr.substring(0,index).trim();
				if (attrValue.length()>2 && (attrValue.length() > 0 && attrValue.charAt(0) == '"') && attrValue.endsWith("\""))
					attrValue=attrValue.substring(1,attrValue.length()-1);
			} else {
				Integer I=NumberTools.getInteger(attr);
				if (I!=null && I>=1) attrNr=I;
			}
		}

		/* Attribut aus Parent zurückgeben */
		if (!selectors.hasNext() && tag.isEmpty()) {
			List<String> path=new ArrayList<String>(parentTags);
			path.add(attr);
			return new String[]{null,formatNumber(parent.getAttribute(attr),path,simDays,systemNumbers,percent,time,distributionSeparator)};
		}

		/* Kindelement suchen */
		Element searchResult=null;
		NodeList list=parent.getChildNodes();
		int nr=0;
		for (int i=0; i<list.getLength();i++) {
			if (!(list.item(i) instanceof Element)) continue;
			Element node=(Element)list.item(i);
			if (node.getNodeName().equalsIgnoreCase(tag)) {
				nr++;
				if (attr.isEmpty()) {searchResult=node; break;}
				if (!selectors.hasNext() && attrValue.isEmpty() && attrNr<=0) {searchResult=node; break;}
				if (attrNr>0) {
					if (nr==attrNr) {searchResult=node; break;}
				} else {
					if (node.getAttribute(attr).equalsIgnoreCase(attrValue)) {searchResult=node; break;}
					if (node.getAttribute(attr).isEmpty() && attrValue.equals("\"\"")) {searchResult=node; break;}
				}
			}
		}
		if (searchResult==null) return new String[]{String.format(Language.tr("Statistic.Filter.NoElementMatchingSelector"),sel),null};

		/* Elementinhalt zurückgeben */
		if (!selectors.hasNext()) {
			List<String> path=new ArrayList<String>(parentTags);
			path.add(tag);
			if (attr.isEmpty() || !attrValue.isEmpty() || attrNr>0) {
				return new String[]{null,formatNumber(searchResult.getTextContent(),path,simDays,systemNumbers,percent,time,distributionSeparator)};
			} else {
				path.add(attr);
				return new String[]{null,formatNumber(searchResult.getAttribute(attr),path,simDays,systemNumbers,percent,time,distributionSeparator)};
			}
		}

		/* Suche fortsetzen */
		List<String> tags=new ArrayList<String>(parentTags);
		tags.add(tag);
		return findElement(selectors,searchResult,tags,systemNumbers,percent,time,distributionSeparator);
	}

	/**
	 * Selektiert ein XML-Objekt
	 * @param command	Suchstring für das XML-Objekt
	 * @param systemNumbers	 Legt fest, ob Zahlen in System- oder lokaler Notation ausgegeben werden sollen.
	 * @param percent	Als Prozentwert oder normale Fließkommazahl
	 * @param time	Als Zeitangabe oder als Zahl
	 * @param distributionSeparator	Trenner für die Einträge bei Verteilungen
	 * @return	Gibt ein String-Array aus zwei Elementen zurück. Im ersten Eintrag wird ein Fehler und im zweiten ein Wert zurückgegeben. Genau einer der beiden Einträge ist immer <code>null</code>.
	 */
	protected final String[] findElement(String command, boolean systemNumbers, boolean percent, boolean time, char distributionSeparator) {
		try (Scanner selectors=new Scanner(command)) {
			selectors.useDelimiter("->");
			if (!selectors.hasNext()) return new String[]{Language.tr("Statistic.Filter.InvalidParameters")+" ("+command+")",null};
			if (xmlDoc==null) return new String[]{Language.tr("Statistic.Filter.InvalidSelector")+" ("+command+")",null};
			return findElement(selectors,xmlDoc.getDocumentElement(),new ArrayList<String>(),systemNumbers,percent,time,distributionSeparator);
		}
	}

	/**
	 * Versucht den übergebenen Ausdruck zu einem Wert zu berechnen
	 * @param command	Zu berechnender Ausdruck
	 * @return	Gibt ein String-Array aus zwei Elementen zurück. Im ersten Eintrag wird ein Fehler und im zweiten ein Wert zurückgegeben. Genau einer der beiden Einträge ist immer <code>null</code>.
	 */
	protected final String[] calc(String command) {
		List<Double> varValuesNumber=new ArrayList<Double>();
		for (int i=0;i<varValues.size();i++) {
			Double d=TimeTools.getExactTime(varValues.get(i));
			if (d==null || d==0.0) {
				d=NumberTools.getDouble(NumberTools.systemNumberToLocalNumber(varValues.get(i)));
				if (d==null) d=0.0;
			}
			varValuesNumber.add(d);
		}
		MathParser calc=new CalcSystem(varNames,varValuesNumber);
		int pos=calc.parse(command);
		if (pos>=0) return new String[]{String.format(Language.tr("Statistic.Filter.InvalidExpressionPosition"),pos+1),null};
		try {
			return new String[]{null,NumberTools.formatSystemNumber(calc.calc())};
		} catch (MathCalcError e) {
			return new String[]{Language.tr("Statistic.Filter.InvalidExpression"),null};
		}
	}

	/**
	 * Fügt einen String an die Ausgabe an.
	 * @param output	Auszugebende Zeichenkette
	 */
	protected final void addResult(String output) {
		results.append(output);
	}

	private String processCommand(String command) {
		int index=-1;
		String parameters="";
		for (int i=0;i<commandNames.size();i++) if (command.toLowerCase().startsWith(commandNames.get(i).toLowerCase())) {
			parameters=command.substring(commandNames.get(i).length()).trim();
			index=i;
			break;
		}
		if (index<0) return Language.tr("Statistic.Filter.UnknownCommand")+" ("+command+")";

		String result=null;
		if (!commandAllowEmptyParameters.get(index) && parameters.isEmpty()) {
			result=Language.tr("Statistic.Filter.InvalidParameters");
		} else {
			result=commandObjects.get(index).run(parameters);
		}
		if (result!=null) return String.format(Language.tr("Statistic.Filter.WhileExecutingCommand"),commandNames.get(index))+":\n"+result; else return null;
	}

	/**
	 * Führt das übergebene Filter-Script aus
	 * @param commands	Auszuführende Filterbefehle
	 * @param ignoreErrors	Setzt, wenn <code>true</code> nach einem Fehler bei dem nächsten Befehl fort, anstatt die Verarbeitung mit einer Fehlermeldung abzubrechen
	 * @return	Gibt <code>true</code> zurück, wenn das Skript erfolgreich ausgeführt werden konne. In jedem Fall liefert <code>getResults</code> Informationen zurück.
	 * @see #getResults()
	 */
	public boolean run(String commands, boolean ignoreErrors) {
		results=new StringBuilder();
		try (Scanner scanner=new Scanner(commands)) {
			scanner.useDelimiter("\n");
			int line=0;
			while (scanner.hasNext()) {
				line++;
				String s=scanner.next().trim();
				s=removeComments(s);
				if (s.isEmpty()) continue;
				s=processCommand(s);
				if (s!=null && !ignoreErrors) {
					results=new StringBuilder();
					results.append(String.format(Language.tr("Statistic.Filter.ErrorInLineNumber"),line)+":\n");
					results.append(s+"\n");
					return false;
				}
			}
			return true;
		}
	}

	/**
	 * Liefert nach der Ausführungen von <code>run</code> Informationen zurück.
	 * Wenn <code>run</code> erfolgreich war, liefert diese Funktion die Rückgabewerte
	 * des Skriptes, sonst eine Fehlermeldung.
	 * @return	Ergebnisse der Sctipt-Ausführung
	 * @see #run(String, boolean)
	 */
	public final String getResults() {
		return results.toString();
	}

	/**
	 * Liefert das dem Filter zu grunde liegende XML-Dokument zurück
	 * @return	Verwendetes XML-Dokument
	 */
	public Document getXMLDocument() {
		return xmlDoc;
	}

	/**
	 * Speichert einen Text (z.B. Filter-Script oder Filter-Ergebnisse) in einer Datei
	 * @param text	Zu speichernder Text
	 * @param output	Ausgabedatei, in der der Text gespeichert werden sll
	 * @param append	Ist dieser Parameter <code>true</code> so werden die Zeilen (so fern die Datei bereits existiert) angehängt. Andernfalls wird die Datei ggf. überschrieben.
	 * @return	Gibt <code>true</code> zurück, wenn die Daten erfolgreich geschrieben werden konnten.
	 */
	public static boolean saveText(String text, File output, boolean append) {
		try {
			try (BufferedWriter bw=new BufferedWriter(new OutputStreamWriter(new FileOutputStream(output,append),StandardCharsets.UTF_8))) {
				bw.write(text);
			}
		} catch (IOException e) {return false;}
		return true;
	}

	/**
	 * Lädt einen Text (z.B. ein Filter-Script) aus einer Datei.
	 * @param input	Zu lesende Eingabedatei.
	 * @return	Gibt im Erfolgsfall den Dateiinhalt zurück, sonst <code>null</code>.
	 */
	public static String loadText(File input) {
		StringBuilder text=new StringBuilder();
		try (BufferedReader br=new BufferedReader(new InputStreamReader(new FileInputStream(input),StandardCharsets.UTF_8))) {
			String line=null;
			while ((line=br.readLine())!=null) {
				text.append(line);
				text.append(System.getProperty("line.separator"));
			}
			return text.toString();
		} catch (IOException e) {return null;}
	}
}
