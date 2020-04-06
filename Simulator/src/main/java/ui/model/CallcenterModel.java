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
package ui.model;

import java.io.File;
import java.io.InputStream;
import java.io.OutputStream;
import java.text.DateFormat;
import java.text.ParseException;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;
import java.util.Locale;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;

import language.Language;
import mathtools.NumberTools;
import mathtools.distribution.DataDistributionImpl;
import mathtools.distribution.tools.DistributionTools;
import tools.SetupData;
import ui.VersionConst;
import xml.XMLTools;

/**
 * Diese Klasse kapselt das komplette Callcenter Modell.<br>
 * @author Alexander Herzog
 * @version 1.0
 * @see CallcenterRunModel
 */
public final class CallcenterModel implements Cloneable {
	/** Version des Simulators, mit dem das Modell angelegt wurde */
	public String version;

	/** Modellname */
	public String name;

	/** Modellbeschreibung */
	public String description;

	/** Datum, für den der Modelltag stehen soll (kann "" sein) */
	public String date;

	/** Kundentypen */
	public List<CallcenterModelCaller> caller;

	/** Callcenter */
	public List<CallcenterModelCallcenter> callcenter;

	/** Skill-Level */
	public List<CallcenterModelSkillLevel> skills;

	/** Maximale Warteschlangenlänge<br>(Ist die Warteschlange voll, erhalten Anrufer das Besetztzeichen.) */
	public String maxQueueLength;

	/** Anzahl der zu simulierenden Tage */
	public int days;

	/** Bevorzugte Schichtlänge (in Halbstundenintervallen) wenn die Agenten aus Verteilungen aufgebaut werden */
	public int preferredShiftLength;

	/** Minimale Schichtlänge (in Halbstundenintervallen) wenn die Agenten aus Verteilungen aufgebaut werden */
	public int minimumShiftLength;

	/** Zuverwendende Anzahl an Sekunden für den Service-Level */
	public short serviceLevelSeconds;

	/** Globale Produktivität pro Intervall */
	public DataDistributionImpl efficiencyPerInterval;

	/** Globale Planungsaufschlag pro Intervall */
	public DataDistributionImpl additionPerInterval;

	/** Einstellungen zu möglichen Schwellenwert-Überschreitungen in den Ergebnissen */
	public CallcenterModelWarnings warnings;

	/**
	 * Konstruktor der Klasse
	 * @param name	Name des Modells
	 */
	public CallcenterModel(String name) {
		version=VersionConst.version;
		this.name=name;
		description="";
		date="";
		maxQueueLength="500";
		days=100;
		preferredShiftLength=16;
		minimumShiftLength=1;
		serviceLevelSeconds=20;
		caller=new ArrayList<CallcenterModelCaller>();
		callcenter=new ArrayList<CallcenterModelCallcenter>();
		skills=new ArrayList<CallcenterModelSkillLevel>();
		efficiencyPerInterval=new DataDistributionImpl(CallcenterModelAgent.countPerIntervalMaxX,48);
		efficiencyPerInterval.setToValue(1);
		additionPerInterval=new DataDistributionImpl(CallcenterModelAgent.countPerIntervalMaxX,48);
		additionPerInterval.setToValue(1);
		warnings=new CallcenterModelWarnings(false);
	}

	/**
	 * Konstruktor der Klasse<br>
	 * (Verwendet den Vorgabe-Modellnamen)
	 */
	public CallcenterModel() {this(Language.tr("Example.DefaultTitle"));}

	@Override
	public CallcenterModel clone() {
		CallcenterModel c=new CallcenterModel(name);

		for (CallcenterModelCaller callerGroup : caller) c.caller.add(callerGroup.clone());
		for (CallcenterModelCallcenter callcenterRecord : callcenter) c.callcenter.add(callcenterRecord.clone());
		for (CallcenterModelSkillLevel skill : skills) c.skills.add(skill.clone());

		c.version=version;
		c.description=description;
		c.date=date;
		c.maxQueueLength=maxQueueLength;
		c.days=days;
		c.preferredShiftLength=preferredShiftLength;
		c.minimumShiftLength=minimumShiftLength;
		c.serviceLevelSeconds=serviceLevelSeconds;
		c.efficiencyPerInterval=efficiencyPerInterval.clone();
		c.additionPerInterval=additionPerInterval.clone();
		c.warnings=warnings.clone();

		return c;
	}

	private int increaseDays(int days) {
		int threadCount=Math.min(SetupData.getSetup().getRealMaxThreadNumber(),Runtime.getRuntime().availableProcessors());
		while (days%threadCount!=0) days++;
		return days;
	}

	/**
	 * Vergleicht das Callcenter-Modell mit einem anderen Callcenter-Modell
	 * @param model	Anderes Callcenter-Modell
	 * @return	Liefert <code>true</code>, wenn die beiden Modelle inhaltlich identisch sind
	 */
	public boolean equalsCallcenterModel(final CallcenterModel model) {
		if (model==null) return false;

		/* if (!model.version.equals(version)) return false; sonst gibt's beim Beenden nach dem Speichern eines Modells, welches mit einer alten Version des Simulators erstellte wurde, immer false */
		if (!model.name.equals(name)) return false;
		if (!model.description.equals(description)) return false;
		if (!model.date.equals(date)) return false;

		if (model.caller.size()!=caller.size()) return false;
		for (int i=0;i<caller.size();i++) if (!caller.get(i).equalsCallcenterModelCaller(model.caller.get(i))) return false;

		if (model.callcenter.size()!=callcenter.size()) return false;
		for (int i=0;i<callcenter.size();i++) if (!callcenter.get(i).equalsCallcenterModelCallcenter(model.callcenter.get(i))) return false;

		if (model.skills.size()!=skills.size()) return false;
		for (int i=0;i<skills.size();i++) if (!skills.get(i).equalsCallcenterModelSkillLevel(model.skills.get(i))) return false;

		if (!model.maxQueueLength.equals(maxQueueLength)) return false;
		if (model.days!=days && increaseDays(model.days)!=increaseDays(days)) return false;
		if (model.preferredShiftLength!=preferredShiftLength) return false;
		if (model.minimumShiftLength!=minimumShiftLength) return false;
		if (model.serviceLevelSeconds!=serviceLevelSeconds) return false;

		if (!DistributionTools.compare(model.efficiencyPerInterval,efficiencyPerInterval)) return false;
		if (!DistributionTools.compare(model.additionPerInterval,additionPerInterval)) return false;

		if (!warnings.equalsCallcenterModelWarnings(model.warnings)) return false;

		return true;
	}

	/**
	 * Ergänzt optionale Daten (wie es auch die Bearbeiten-Dialoge machen),
	 * um unnötige Starts der Hintergrunsimulation bedingt durch nur scheinbar
	 * im Dialog veränderte Modell zu vermeiden.
	 */
	public void prepareData() {
		for (int i=0;i<caller.size();i++) caller.get(i).prepareData(this);
		for (int i=0;i<callcenter.size();i++) callcenter.get(i).prepareData(this);
		for (int i=0;i<skills.size();i++) skills.get(i).prepareData(this);
	}

	/**
	 * Mögliche Namen des Basiselements von Callcenter-Modell-XML-Dateien (zur Erkennung von Dateien dieses Typs.)
	 */
	public static final String[] XMLBaseElement=Language.trAll("XML.Model.BaseElement");


	/**
	 * Versucht das übergebene Modell-Datum als Datumsangabe zu interpretieren und gibt es im Format YYYY-MM-DD zurück.
	 * @param date	Datumsangabe, die gerpüft und normiert werden soll.
	 * @return	Datumsangabe im Format YYYY-MM-DD oder "" im Falle eines Fehlers.
	 */
	public static String interpreteModelDate(String date) {
		java.util.Date parsed=stringToDate(date);
		return dateToString(parsed);
	}

	/**
	 * Versucht das übergebene Modell-Datum als Datumsangabe zu interpretieren und gibt es als <code>Date</code>-Objekt zurück
	 * @param textDate	Datumsangabe, die interpretiert werden soll
	 * @return	Datumsangabe als <code>Date</code>-Objekt oder <code>null</code> im Falle eines Fehlers.
	 */
	public static java.util.Date stringToDate(String textDate) {
		if (textDate==null || textDate.trim().isEmpty()) return null;

		java.util.Date parsed;

		for (Locale testLocale : new Locale[]{Locale.US,Locale.getDefault()}) {
			for (int testStyle : new int[]{DateFormat.SHORT,DateFormat.LONG}) {
				DateFormat dateInternationalShort=DateFormat.getDateInstance(testStyle,testLocale);
				try {parsed=dateInternationalShort.parse(textDate);} catch (ParseException e) {parsed=null;}
				if (parsed!=null) return parsed;
			}
		}

		return null;
	}

	/**
	 * Wandelt das übergebene Datum in eine Zeichenkette vom Format MM/DD/YYYY um.
	 * (Wird <code>null</code> übergeben, so wird eine leere Zeichenkette erzeugt.
	 * @param date	Datum, das als Zeichenkette zurückgegeben werden soll.
	 * @return	Datum als Zeichenkette oder "" im Falle von <code>null</code> als übergebenem Datum.
	 */
	public static String dateToString(java.util.Date date) {
		if (date==null) return "";
		DateFormat dateInternationalShort=DateFormat.getDateInstance(DateFormat.SHORT,Locale.US);
		return dateInternationalShort.format(date);
	}

	/**
	 * Wandelt das übergebene Datum in eine Zeichenkette vom Format MM/DD/YYYY um.
	 * (Wird <code>null</code> übergeben, so wird eine leere Zeichenkette erzeugt.
	 * @param date	Datum, das als Zeichenkette zurückgegeben werden soll.
	 * @return	Datum als Zeichenkette oder "" im Falle von <code>null</code> als übergebenem Datum.
	 */
	public static String dateToString(Calendar date) {
		if (date==null) return "";
		DateFormat dateInternationalShort=DateFormat.getDateInstance(DateFormat.SHORT,Locale.US);
		return dateInternationalShort.format(date.getTime());
	}

	/**
	 * Wandelt das übergebene Datum in eine Zeichenkette gemäß des lokalen Datumsformats um.
	 * (Wird <code>null</code> übergeben, so wird eine leere Zeichenkette erzeugt.
	 * @param date	Datum, das als Zeichenkette zurückgegeben werden soll.
	 * @return	Datum als Zeichenkette oder "" im Falle von <code>null</code> als übergebenem Datum.
	 */
	public static String dateToLocalString(java.util.Date date) {
		if (date==null) return "";
		DateFormat dateInternationalShort=DateFormat.getDateInstance(DateFormat.LONG,Locale.getDefault());
		return dateInternationalShort.format(date);
	}

	/**
	 * Wandelt das übergebene Datum in eine Zeichenkette gemäß des lokalen Datumsformats um.
	 * (Wird <code>null</code> übergeben, so wird eine leere Zeichenkette erzeugt.
	 * @param date	Datum, das als Zeichenkette zurückgegeben werden soll.
	 * @return	Datum als Zeichenkette oder "" im Falle von <code>null</code> als übergebenem Datum.
	 */
	public static String dateToLocalString(Calendar date) {
		if (date==null) return "";
		DateFormat dateInternationalShort=DateFormat.getDateInstance(DateFormat.LONG,Locale.getDefault());
		return dateInternationalShort.format(date.getTime());
	}

	/**
	 * Versucht ein komplettes Callcenter-Modell aus der angegebenen XML-Datei zu laden
	 * @param file	Dateiname der XML-Datei, aus der das Modell geladen werden soll
	 * @return	Tritt ein Fehler auf, so wird die Fehlermeldung als String zurückgegeben. Im Erfolgsfall wird <code>null</code> zurückgegeben.
	 */
	public String loadFromFile(File file) {
		XMLTools xml=new XMLTools(file);
		Element root=xml.load();
		if (root==null) return xml.getError();
		return loadFromXML(root);
	}

	/**
	 * Versucht ein komplettes Callcenter-Modell aus dem angegebenen Stream zu laden
	 * @param stream	InputStream, aus dem das Modell geladen werden soll
	 * @return	Tritt ein Fehler auf, so wird die Fehlermeldung als String zurückgegeben. Im Erfolgsfall wird <code>null</code> zurückgegeben.
	 */
	public String loadFromStream(InputStream stream) {
		XMLTools xml=new XMLTools(stream);
		Element root=xml.load();
		if (root==null) return xml.getError();
		return loadFromXML(root);
	}

	/**
	 * Versucht ein komplettes Callcenter-Modell aus dem angegebenen String zu laden
	 * @param text	String, aus dem das Modell geladen werden soll
	 * @return	Tritt ein Fehler auf, so wird die Fehlermeldung als String zurückgegeben. Im Erfolgsfall wird <code>null</code> zurückgegeben.
	 */
	public String loadFromString(String text) {
		XMLTools xml=new XMLTools(text);
		Element root=xml.load();
		if (root==null) return xml.getError();
		return loadFromXML(root);
	}

	/**
	 * Versucht ein Callcenter-Modell aus dem übergebenen XML-Node zu laden
	 * @param node	XML-Knoten, der das Callcenter-Modell enthält
	 * @return	Tritt ein Fehler auf, so wird die Fehlermeldung als String zurückgegeben. Im Erfolgsfall wird <code>null</code> zurückgegeben.
	 */
	public String loadFromXML(Element node) {
		if (!Language.trAll("XML.Model.BaseElement",node.getNodeName())) return String.format(Language.tr("XML.Model.BaseElement.Error"),Language.trPrimary("XML.Model.BaseElement"));

		name="";
		description="";
		date="";
		maxQueueLength="500";
		days=100;
		preferredShiftLength=16;
		minimumShiftLength=1;
		serviceLevelSeconds=20;
		caller.clear();
		callcenter.clear();
		skills.clear();
		efficiencyPerInterval.setToValue(1);
		additionPerInterval.setToValue(1);

		NodeList l=node.getChildNodes();
		for (int i=0; i<l.getLength();i++) {
			if (!(l.item(i) instanceof Element)) continue;
			Element e=(Element)l.item(i);
			String s=e.getNodeName();
			if (Language.trAll("XML.Model.Name",s)) {name=e.getTextContent(); continue;}
			if (Language.trAll("XML.Model.Version",s)) {version=e.getTextContent(); if (version.isEmpty()) version=VersionConst.version; continue;}
			if (Language.trAll("XML.Model.Description",s)) {description=e.getTextContent(); continue;}
			if (Language.trAll("XML.Model.Date",s)) {date=interpreteModelDate(e.getTextContent()); continue;}
			if (Language.trAll("XML.Model.MaxQueueLength",s)) {maxQueueLength=e.getTextContent(); continue;}
			if (Language.trAll("XML.Model.Days",s)) {
				Integer J=NumberTools.getNotNegativeInteger(e.getTextContent());
				if (J==null || J==0) return Language.tr("XML.Model.Days.Error");
				days=J; continue;
			}
			if (Language.trAll("XML.Model.PreferredShiftLength",s)) {
				Integer J=NumberTools.getNotNegativeInteger(e.getTextContent());
				if (J==null || J==0) return Language.tr("XML.Model.PreferredShiftLength.Error");
				preferredShiftLength=J; continue;
			}
			if (Language.trAll("XML.Model.MinimumShiftLength",s)) {
				Integer J=NumberTools.getNotNegativeInteger(e.getTextContent());
				if (J==null || J==0) return Language.tr("XML.Model.MinimumShiftLength.Error");
				minimumShiftLength=J; continue;
			}
			if (Language.trAll("XML.Model.ServiceLevel",s)) {
				Short SH=NumberTools.getNotNegativeShort(e.getTextContent());
				if (SH==null) return Language.tr("XML.Model.ServiceLevel.Error");
				serviceLevelSeconds=SH; continue;
			}
			if (Language.trAll("XML.Model.Productivity",s)) {
				DataDistributionImpl newEfficiencyPerInterval=DataDistributionImpl.createFromString(e.getTextContent(),efficiencyPerInterval.upperBound);
				if (newEfficiencyPerInterval==null) return Language.tr("XML.Model.Productivity.Error");
				efficiencyPerInterval=newEfficiencyPerInterval;
				continue;
			}
			if (Language.trAll("XML.Model.Surcharge",s)) {
				DataDistributionImpl newAdditionPerInterval=DataDistributionImpl.createFromString(e.getTextContent(),additionPerInterval.upperBound);
				if (newAdditionPerInterval==null) return Language.tr("XML.Model.Surcharge.Error");
				additionPerInterval=newAdditionPerInterval;
				continue;
			}
			if (Language.trAll("XML.Model.Warnings",s)) {
				String t=warnings.loadFromXML(e); if (t!=null) return t;
				continue;
			}
			if (Language.trAll("XML.Model.ClientType",s)) {
				CallcenterModelCaller c=new CallcenterModelCaller();
				String t=c.loadFromXML(e); if (t!=null) return t+" ("+String.format(Language.tr("XML.Model.ClientType.Error"),caller.size()+1)+")";
				caller.add(c); continue;
			}
			if (Language.trAll("XML.Model.CallCenter",s)) {
				CallcenterModelCallcenter c=new CallcenterModelCallcenter();
				String t=c.loadFromXML(e); if (t!=null) return t+" ("+String.format(Language.tr("XML.Model.CallCenter.Error"),callcenter.size()+1)+")";
				callcenter.add(c); continue;
			}
			if (Language.trAll("XML.Model.SkillLevel",s)) {
				CallcenterModelSkillLevel c=new CallcenterModelSkillLevel();
				String t=c.loadFromXML(e); if (t!=null) return t+" ("+String.format(Language.tr("XML.Model.SkillLevel.Error"),skills.size()+1)+")";
				skills.add(c); continue;
			}
		}

		prepareData();
		return null;
	}

	/**
	 * Speichert das komplette Callcenter-Modell in der angegebenen XML-Datei.
	 * @param file	Dateiname der Datei, in der das Modell gespeichert werden soll
	 * @return	Gibt an, ob das Modell erfolgreich gespeichert werden konnte.
	 */
	public boolean saveToFile(File file) {
		XMLTools xml=new XMLTools(file);
		Element root=xml.generateRoot(Language.trPrimary("XML.Model.BaseElement"));
		if (root==null) return false;
		addDataToXML(root,false);
		return xml.save(root);
	}

	/**
	 * Speichert das komplette Callcenter-Modell in dem angegebenen OutputStream.
	 * @param stream	OutputStream, in dem das Modell gespeichert werden soll
	 * @param isStatisticFilePart	Gibt an, ob die Modelldaten Teil einer Statistikdatei werden sollen (wenn nein, wird die Versionskennung auf die aktuelle Programmversion gesetzt)
	 * @return	Gibt an, ob das Modell erfolgreich gespeichert werden konnte.
	 */
	public boolean saveToStream(OutputStream stream, boolean isStatisticFilePart) {
		XMLTools xml=new XMLTools(stream);
		Element root=xml.generateRoot(Language.trPrimary("XML.Model.BaseElement"));
		if (root==null) return false;
		addDataToXML(root,isStatisticFilePart);
		return xml.save(root);
	}

	/**
	 * Erstellt unterhalb des übergebenen XML-Knotens einen neuen Knoten, der das gesamte Callcenter-Modell enthält.	 *
	 * @param parent	Eltern-XML-Knoten
	 * @param isStatisticFilePart	Gibt an, ob die Modelldaten Teil einer Statistikdatei werden sollen (wenn nein, wird die Versionskennung auf die aktuelle Programmversion gesetzt)
	 */
	public void saveToXML(Element parent, boolean isStatisticFilePart) {
		Document doc=parent.getOwnerDocument();
		Element node=doc.createElement(Language.trPrimary("XML.Model.BaseElement")); parent.appendChild(node);
		addDataToXML(node,isStatisticFilePart);
	}

	/**
	 * Speichert das komplette Callcenter-Modell in einem Objekt vom Typ <code>Document</code>.
	 * @return	Liefert im Erfolgsfall das <code>Document</code>-Element und im Fehlerfall <code>null</code>.
	 */
	public Document saveToXMLDocument() {
		XMLTools xml=new XMLTools();
		Element root=xml.generateRoot(Language.trPrimary("XML.Model.BaseElement"));
		if (root==null) return null;
		addDataToXML(root,false);
		return root.getOwnerDocument();
	}

	private void addDataToXML(Element node, boolean isStatisticFilePart) {
		Document doc=node.getOwnerDocument();
		Element e;

		node.appendChild(e=doc.createElement(Language.trPrimary("XML.Model.Name"))); e.setTextContent(name);
		if (!isStatisticFilePart) version=VersionConst.version;
		node.appendChild(e=doc.createElement(Language.trPrimary("XML.Model.Version"))); e.setTextContent(version);
		node.appendChild(e=doc.createElement(Language.trPrimary("XML.Model.Description"))); e.setTextContent(description);
		if (date!=null && !date.trim().isEmpty()) {
			node.appendChild(e=doc.createElement(Language.trPrimary("XML.Model.Date"))); e.setTextContent(date);
		}
		node.appendChild(e=doc.createElement(Language.trPrimary("XML.Model.MaxQueueLength"))); e.setTextContent(""+maxQueueLength);
		node.appendChild(e=doc.createElement(Language.trPrimary("XML.Model.Days"))); e.setTextContent(""+days);
		node.appendChild(e=doc.createElement(Language.trPrimary("XML.Model.PreferredShiftLength"))); e.setTextContent(""+preferredShiftLength);
		if (minimumShiftLength>1) {node.appendChild(e=doc.createElement(Language.trPrimary("XML.Model.MinimumShiftLength"))); e.setTextContent(""+minimumShiftLength);}
		node.appendChild(e=doc.createElement(Language.trPrimary("XML.Model.ServiceLevel"))); e.setTextContent(""+serviceLevelSeconds);
		node.appendChild(e=doc.createElement(Language.trPrimary("XML.Model.Productivity"))); e.setTextContent(efficiencyPerInterval.storeToString());
		node.appendChild(e=doc.createElement(Language.trPrimary("XML.Model.Surcharge"))); e.setTextContent(additionPerInterval.storeToString());

		warnings.saveToXML(node);

		for (CallcenterModelCaller c : caller) c.saveToXML(node);
		for (CallcenterModelCallcenter c : callcenter) c.saveToXML(node);
		for (CallcenterModelSkillLevel s : skills) s.saveToXML(node);
	}

	/**
	 * Erstellt eine Modellbeschreibung
	 * @return	Modellbeschreibung
	 */
	public String generateDescription() {
		StringBuilder sb=new StringBuilder();
		sb.append(String.format(Language.tr("Model.GenerateDescription.Name"),name)+"\n\n");

		int callerGroupCount=0, callerCount=0;
		for (CallcenterModelCaller c : caller) if (c.active) {callerGroupCount++; callerCount+=c.freshCallsCountMean;}
		sb.append(callerGroupCount+" "+((callerGroupCount==1)?Language.tr("Model.GenerateDescription.ClientType.Single"):Language.tr("Model.GenerateDescription.ClientType.Multiple"))+" ("+String.format(Language.tr("Model.GenerateDescription.FreshCalls.All"),callerCount)+"): \n");

		for (CallcenterModelCaller c : caller) {
			sb.append("  - "+c.name);
			if (!c.active) sb.append(" ("+Language.tr("Dialog.inactive.lower")+")");
			sb.append(" ("+c.freshCallsCountMean+" "+Language.tr("Model.GenerateDescription.FreshCalls"));
			if (c.freshCallsCountSD>0) sb.append("; "+Language.tr("Distribution.StdDev")+" "+NumberTools.formatNumber(c.freshCallsCountSD,1));
			sb.append(')');
			sb.append('\n');
		}

		boolean callContinue=false;
		boolean callCancel=false;
		boolean callRetry=false;
		if (caller.size()>0) {
			callContinue=(caller.get(0).continueProbability>0);
			callCancel=(caller.get(0).waitingTimeMode!=CallcenterModelCaller.WAITING_TIME_MODE_OFF);
			callRetry=(caller.get(0).retryProbabiltyAfterGiveUpFirstRetry>0);
			double scoreBase=caller.get(0).scoreBase;
			double scoreSecond=caller.get(0).scoreSecond;
			double scoreContinued=caller.get(0).scoreContinued;
			boolean sameScore=true;
			for (int i=1;i<caller.size();i++) {
				if (scoreBase!=caller.get(i).scoreBase) sameScore=false;
				if (scoreSecond!=caller.get(i).scoreSecond) sameScore=false;
				if (scoreContinued!=caller.get(i).scoreContinued) sameScore=false;
				callContinue=callContinue || (caller.get(i).continueProbability>0);
				callCancel=callCancel || (caller.get(i).waitingTimeMode!=CallcenterModelCaller.WAITING_TIME_MODE_OFF);
				callRetry=callRetry || (caller.get(i).retryProbabiltyAfterGiveUpFirstRetry>0);
			}
			if (sameScore) sb.append("  - "+Language.tr("Model.GenerateDescription.ClientPriority.Same")+"\n"); else sb.append("  - "+Language.tr("Model.GenerateDescription.ClientPriority.Different")+"\n");
		}

		int callcenterCount=0;
		for (CallcenterModelCallcenter c : callcenter) if (c.active) callcenterCount++;
		sb.append("\n"+callcenterCount+" "+((callcenterCount==1)?Language.tr("Model.GenerateDescription.Callcenter.Single"):Language.tr("Model.GenerateDescription.Callcenter.Multiple"))+":\n");

		int agentType=-3;

		for (CallcenterModelCallcenter c : callcenter) {
			sb.append("  - "+c.name);
			if (!c.active) sb.append(" ("+Language.tr("Dialog.inactive.lower")+")");
			sb.append(":\n");
			int groupCount=0; for (CallcenterModelAgent a : c.agents) if (a.active) groupCount++;
			sb.append("    "+groupCount+" "+((groupCount==1)?Language.tr("Model.GenerateDescription.AgentGroup.Single"):Language.tr("Model.GenerateDescription.AgentGroupMultiple"))+"\n    (");
			List<String> types=new ArrayList<String>();
			for (CallcenterModelAgent a : c.agents) if (a.active) {

				if (agentType==-3) {
					if (a.count>0) agentType=1;
					if (a.count<0) agentType=a.count;
				} else {
					if (a.count>0 && agentType<0) agentType=0;
					if (a.count<0 && agentType!=a.count) agentType=0;
				}

				for (CallcenterModelSkillLevel sk : skills) if (sk.name.equalsIgnoreCase(a.skillLevel)) {
					for (String type : sk.callerTypeName) if (types.indexOf(type)<0) types.add(type);
				}
			}
			boolean first=true;	for (String type : types) {if (!first) sb.append(", "); sb.append(type); first=false;}
			sb.append(")\n");
		}

		sb.append("\n"+(skills.size())+" "+((skills.size()==1)?Language.tr("Model.GenerateDescription.SkillLevel.Single"):Language.tr("Model.GenerateDescription.SkillLevel.Multiple"))+":\n");
		for (CallcenterModelSkillLevel sk : skills) {
			sb.append("  - "+sk.name+"\n    (");
			boolean first=true;	for (String s : sk.callerTypeName) {if (!first) sb.append(", "); sb.append(s); first=false;}
			sb.append(")\n");
		}

		sb.append('\n');
		switch (agentType) {
		case -2: sb.append(Language.tr("Model.GenerateDescription.Agents.FromFreshCalls")+"\n"); break;
		case -1: sb.append(Language.tr("Model.GenerateDescription.Agents.DistributionOverTheDay")+"\n"); break;
		case 0: sb.append(Language.tr("Model.GenerateDescription.Agents.Different")+"\n"); break;
		case 1: sb.append(Language.tr("Model.GenerateDescription.Agents.FixedWorkingTimes")+"\n"); break;
		default: sb.append(Language.tr("Model.GenerateDescription.Agents.NoAgents")+"\n"); break;
		}

		if (callCancel || callRetry || callContinue) {
			sb.append(Language.tr("Model.GenerateDescription.Extra.TheModelContains")+" ");
			if (callCancel || callRetry) {
				if (callRetry) sb.append(Language.tr("Model.GenerateDescription.Extra.Retry")); else sb.append(Language.tr("Model.GenerateDescription.Extra.Cancelation"));
				if (callContinue) sb.append(" "+Language.tr("Model.GenerateDescription.Extra.And")+" ");
			}
			if (callContinue) sb.append(Language.tr("Model.GenerateDescription.Extra.Forwarding"));
			sb.append(".\n");
		}

		return sb.toString();
	}
}