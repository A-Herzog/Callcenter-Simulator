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
import java.util.Arrays;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;

import language.Language;
import mathtools.NumberTools;
import mathtools.TimeTools;
import mathtools.distribution.DataDistributionImpl;
import ui.connected.ConnectedModelUebertrag;
import xml.XMLTools;

/**
 * Diese Klasse Kapselt die Einstellungen für einen Optimierungslauf.
 * @author Alexander Herzog
 * @version 1.0
 */
public final class OptimizeSetup {
	/**
	 * Zu optimierende Größe
	 * @author Alexander Herzog
	 * @see OptimizeSetup#optimizeProperty
	 */
	public enum OptimizeProperty {
		/** Erreichbarkeit (auf Anrufbasis) als Zielgröße verwenden. */
		OPTIMIZE_PROPERTY_SUCCESS_BY_CALL,

		/** Erreichbarkeit (auf Kundenbasis) als Zielgröße verwenden. */
		OPTIMIZE_PROPERTY_SUCCESS_BY_CLIENT,

		/** Wartezeit (auf Anrufbasis) als Zielgröße verwenden. */
		OPTIMIZE_PROPERTY_WAITING_TIME_BY_CALL,

		/** Wartezeit (auf Anrufbasis) als Zielgröße verwenden. */
		OPTIMIZE_PROPERTY_WAITING_TIME_BY_CLIENT,

		/** Verweiltzeit (auf Anrufbasis) als Zielgröße verwenden. */
		OPTIMIZE_PROPERTY_STAYING_TIME_BY_CALL,

		/** Verweiltzeit (auf Kundenbasis) als Zielgröße verwenden. */
		OPTIMIZE_PROPERTY_STAYING_TIME_BY_CLIENT,

		/** Service-Level (auf Anrufbasis bezogen auf die erfolgreichen Anrufe) als Zielgröße verwenden. */
		OPTIMIZE_PROPERTY_SERVICE_LEVEL_BY_CALL,

		/** Service-Level (auf Anrufbasis bezogen auf alle Anrufe) als Zielgröße verwenden. */
		OPTIMIZE_PROPERTY_SERVICE_LEVEL_BY_CALL_ALL,

		/** Service-Level (auf Kundenbasis bezogen auf erfolgreiche Kunden) als Zielgröße verwenden. */
		OPTIMIZE_PROPERTY_SERVICE_LEVEL_BY_CLIENT,

		/** Service-Level (auf Kundenbasis bezogen auf alle Kunden) als Zielgröße verwenden. */
		OPTIMIZE_PROPERTY_SERVICE_LEVEL_BY_CLIENT_ALL,

		/** Auslastung der Agenten als Zielgröße verwenden. */
		OPTIMIZE_PROPERTY_WORK_LOAD
	}

	/**
	 * Optimierung im Mittel über den Tag oder in jedem einzelnen Intervall bzw. intervallweise.
	 * @author Alexander Herzog
	 * @see OptimizeSetup#optimizeByInterval
	 */
	public enum OptimizeInterval {
		/** Optimierung im Mittel über alle Intervalle. */
		OPTIMIZE_BY_INTERVAL_NO,

		/** Zielgröße muss in jedem einzelnen Intervall erreicht werden. */
		OPTIMIZE_BY_INTERVAL_YES,

		/** Zielgröße muss in jedem einzelnen Intervall erreicht werden. Außerdem wird Intervall für Intervall nacheinander optimiert. */
		OPTIMIZE_BY_INTERVAL_IN_ORDER
	}

	/**
	 * Welche Kundengruppen sollen berücksichtigt werden?
	 * @author Alexander Herzog
	 * @see OptimizeSetup#optimizeGroups
	 */
	public enum OptimizeGroups {
		/** Zielgröße muss im Mittel über alle Gruppen erreicht werden. */
		OPTIMIZE_GROUPS_AVERAGE,

		/** Zielgröße muss für jede Gruppe einzeln erreicht werden. */
		OPTIMIZE_GROUPS_ALL,

		/** Zielgröße muss für ausgewählte Gruppen erreicht werden. (Siehe auch OptimizeSetup#optimizeGroupNames) */
		OPTIMIZE_GROUPS_SELECTION
	}

	/**
	 * Zu optimierende Größe
	 */
	public OptimizeProperty optimizeProperty=OptimizeProperty.OPTIMIZE_PROPERTY_SUCCESS_BY_CALL;

	/**
	 * Zielwert der zu optimierenden Eigenschaft.
	 */
	public double optimizeValue=0.95;

	/**
	 * Obergrenze der zu optimierenden Eigenschaft.
	 * (negativer Wert, wenn keine Obergrenze gesetzt sein soll)
	 */
	public double optimizeMaxValue=-1;

	/**
	 * Im Mittel über den Tag oder in jedem einzelnen Intervall bzw. intervallweise.
	 * @see OptimizeProperty
	 */
	public OptimizeInterval optimizeByInterval=OptimizeInterval.OPTIMIZE_BY_INTERVAL_IN_ORDER;

	/**
	 * Zu betrachtende Intervalle
	 */
	public DataDistributionImpl optimizeIntervals;

	/**
	 * Welche Gruppen sollen die Zielgröße erreichen
	 */
	public OptimizeGroups optimizeGroups=OptimizeGroups.OPTIMIZE_GROUPS_AVERAGE;

	/**
	 * Auswahl der Gruppen, über die optimiert werden soll, im Fall von
	 * <code>optimizeGroups==OPTIMIZE_GROUPS_SELECTION</code>
	 * @see #optimizeGroups
	 * @see OptimizeGroups#OPTIMIZE_GROUPS_SELECTION
	 */
	public String[] optimizeGroupNames=null;

	/**
	 * Einschränkungen in Bezug auf die Agentenanzahl für bestimmte Gruppen
	 * Namen der Gruppen
	 */
	public List<String> groupRestrictionName=new ArrayList<>();

	/**
	 * Einschränkungen in Bezug auf die Agentenanzahl für bestimmte Gruppen
	 * Minimalanzahlen an Agenten pro Halbstundenintervall
	 */
	public List<DataDistributionImpl> groupRestrictionMin=new ArrayList<>();

	/**
	 * Einschränkungen in Bezug auf die Agentenanzahl für bestimmte Gruppen
	 * Maximalanzahlen an Agenten pro Halbstundenintervall
	 */
	public List<DataDistributionImpl> groupRestrictionMax=new ArrayList<>();

	/**
	 * Agentenarbeitszeiterhöhung pro Optimierungsschritt
	 */
	public double changeValue=0.01;

	/**
	 * Über alle Agentengruppen (<code>true</code>) oder nur über ausgewählte Gruppen (<code>false</code>)
	 */
	public boolean changeAll=true;

	/**
	 * Agentengruppen, in denen die Agentenzahlen erhöht werden sollen, im Fall von
	 * <code>changeAll=false</code>
	 * @see #changeAll
	 */
	public String[] changeGroups=null;

	/**
	 * Gibt den Dateinamen für die Übertragsdatei ein.
	 * @see #uebertrag
	 */
	public String uebertragFile;

	/**
	 * Gibt an, wie viele am Vortag endgültig abgebrochene Kunden am aktuellen Tag einen erneuten Anlauf starten.
	 * @see #uebertragFile
	 */
	public HashMap<String,ConnectedModelUebertrag> uebertrag=new HashMap<>();

	/**
	 * Gibt an, Kunden welcher Typen zusätzlich zu Beginn als Erstanrufer dem Modell hinzugefügt werden sollen.
	 * @see #uebertragAdditionalCount
	 */
	public List<String> uebertragAdditionalCaller=new ArrayList<>();

	/**
	 * Gibt an, wie viele Kunden der einzelnen Typen zu Beginn als Erstanrufer dem Modell hinzugefügt werden sollen.
	 * @see #uebertragAdditionalCaller
	 */
	public List<Integer> uebertragAdditionalCount=new ArrayList<>();

	/**
	 * Konstruktor der Klasse <code>OptimizeSetup</code>
	 */
	public OptimizeSetup() {
		optimizeIntervals=new DataDistributionImpl(86399,48);
		optimizeIntervals.setToValue(1);
	}

	/**
	 * Mögliche Namen des Basiselement von Optimierer-Setup-XML-Dateien (zur Erkennung von Dateien dieses Typs.)
	 */
	public static final String[] XMLBaseElement=Language.trAll("XML.OptimizerSetup");

	/**
	 * Speichert die Optimierer-Einstellungen in der angegebenen XML-Datei.
	 * @param file	Dateiname der Datei, in der die Einstellungen gespeichert werden soll
	 * @return	Gibt an, ob das die Einstellungen erfolgreich gespeichert werden konnten.
	 */
	public boolean saveToFile(File file) {
		XMLTools xml=new XMLTools(file);
		Element root=xml.generateRoot(Language.trPrimary("XML.OptimizerSetup"));
		if (root==null) return false;
		addDataToXML(root);
		return xml.save(root);
	}

	/**
	 * Erstellt unterhalb des übergebenen XML-Knotens einen neuen Knoten, der die Optimierer-Einstellungen enthält.
	 * @param parent	Eltern-XML-Knoten
	 */
	public void saveToXML(final Element parent) {
		Document doc=parent.getOwnerDocument();
		Element node=doc.createElement(Language.trPrimary("XML.OptimizerSetup")); parent.appendChild(node);
		addDataToXML(node);
	}

	/**
	 * Speichert die Optimierer-Einstellungen in einem XML-Knoten
	 * @param node	XML-Knoten in dem die Optimierer-Einstellungen gespeichert werden sollen
	 * @see #saveToXML(Element)
	 */
	private void addDataToXML(final Element node) {
		Document doc=node.getOwnerDocument();
		Element e,e2;
		String s="";

		switch (optimizeProperty) {
		case OPTIMIZE_PROPERTY_SUCCESS_BY_CALL: s=Language.trPrimary("XML.OptimizerSetup.Property.AccessibilityCalls"); break;
		case OPTIMIZE_PROPERTY_SUCCESS_BY_CLIENT: s=Language.trPrimary("XML.OptimizerSetup.Property.AccessibilityClients"); break;
		case OPTIMIZE_PROPERTY_WAITING_TIME_BY_CALL: s=Language.trPrimary("XML.OptimizerSetup.Property.WaitingTimeCalls"); break;
		case OPTIMIZE_PROPERTY_WAITING_TIME_BY_CLIENT: s=Language.trPrimary("XML.OptimizerSetup.Property.WaitingTimeClients"); break;
		case OPTIMIZE_PROPERTY_STAYING_TIME_BY_CALL: s=Language.trPrimary("XML.OptimizerSetup.Property.HoldingTimeCalls"); break;
		case OPTIMIZE_PROPERTY_STAYING_TIME_BY_CLIENT: s=Language.trPrimary("XML.OptimizerSetup.Property.HoldingTimeClients"); break;
		case OPTIMIZE_PROPERTY_SERVICE_LEVEL_BY_CALL: s=Language.trPrimary("XML.OptimizerSetup.Property.ServiceLevelCallsSuccess"); break;
		case OPTIMIZE_PROPERTY_SERVICE_LEVEL_BY_CALL_ALL: s=Language.trPrimary("XML.OptimizerSetup.Property.ServiceLevelCallsAll"); break;
		case OPTIMIZE_PROPERTY_SERVICE_LEVEL_BY_CLIENT: s=Language.trPrimary("XML.OptimizerSetup.Property.ServiceLevelClientsSuccess"); break;
		case OPTIMIZE_PROPERTY_SERVICE_LEVEL_BY_CLIENT_ALL: s=Language.trPrimary("XML.OptimizerSetup.Property.ServiceLevelClientsAll"); break;
		case OPTIMIZE_PROPERTY_WORK_LOAD: s=Language.trPrimary("XML.OptimizerSetup.Property.WorkLoad"); break;
		}
		node.appendChild(e=doc.createElement(Language.trPrimary("XML.OptimizerSetup.Property"))); e.setTextContent(s);

		switch (optimizeProperty) {
		case OPTIMIZE_PROPERTY_SUCCESS_BY_CALL:
		case OPTIMIZE_PROPERTY_SUCCESS_BY_CLIENT:
			s=NumberTools.formatNumber(optimizeValue*100)+"%";
			break;
		case OPTIMIZE_PROPERTY_WAITING_TIME_BY_CALL:
		case OPTIMIZE_PROPERTY_WAITING_TIME_BY_CLIENT:
			s=TimeTools.formatTime((int)Math.round(optimizeValue));
			break;
		case OPTIMIZE_PROPERTY_STAYING_TIME_BY_CALL:
		case OPTIMIZE_PROPERTY_STAYING_TIME_BY_CLIENT:
			s=TimeTools.formatTime((int)Math.round(optimizeValue));
			break;
		case OPTIMIZE_PROPERTY_SERVICE_LEVEL_BY_CALL:
		case OPTIMIZE_PROPERTY_SERVICE_LEVEL_BY_CALL_ALL:
		case OPTIMIZE_PROPERTY_SERVICE_LEVEL_BY_CLIENT:
		case OPTIMIZE_PROPERTY_SERVICE_LEVEL_BY_CLIENT_ALL:
			s=NumberTools.formatNumber(optimizeValue*100)+"%";
			break;
		case OPTIMIZE_PROPERTY_WORK_LOAD:
			s=NumberTools.formatNumber(optimizeValue*100)+"%";
			break;
		}
		node.appendChild(e=doc.createElement(Language.trPrimary("XML.OptimizerSetup.Value"))); e.setTextContent(s);

		if (optimizeMaxValue>=0) {
			switch (optimizeProperty) {
			case OPTIMIZE_PROPERTY_SUCCESS_BY_CALL:
			case OPTIMIZE_PROPERTY_SUCCESS_BY_CLIENT:
				s=NumberTools.formatNumber(optimizeMaxValue*100)+"%";
				break;
			case OPTIMIZE_PROPERTY_WAITING_TIME_BY_CALL:
			case OPTIMIZE_PROPERTY_WAITING_TIME_BY_CLIENT:
				s=TimeTools.formatTime((int)Math.round(optimizeMaxValue));
				break;
			case OPTIMIZE_PROPERTY_STAYING_TIME_BY_CALL:
			case OPTIMIZE_PROPERTY_STAYING_TIME_BY_CLIENT:
				s=TimeTools.formatTime((int)Math.round(optimizeMaxValue));
				break;
			case OPTIMIZE_PROPERTY_SERVICE_LEVEL_BY_CALL:
			case OPTIMIZE_PROPERTY_SERVICE_LEVEL_BY_CLIENT:
				s=NumberTools.formatNumber(optimizeMaxValue*100)+"%";
				break;
			case OPTIMIZE_PROPERTY_WORK_LOAD:
				s=NumberTools.formatNumber(optimizeMaxValue*100)+"%";
				break;
			case OPTIMIZE_PROPERTY_SERVICE_LEVEL_BY_CALL_ALL:
			case OPTIMIZE_PROPERTY_SERVICE_LEVEL_BY_CLIENT_ALL:
				s=NumberTools.formatNumber(optimizeMaxValue*100)+"%";
				break;
			default:
				break;
			}
			node.appendChild(e=doc.createElement(Language.trPrimary("XML.OptimizerSetup.ValueMaximum"))); e.setTextContent(s);
		}

		switch (optimizeByInterval) {
		case OPTIMIZE_BY_INTERVAL_NO: s=Language.trPrimary("XML.OptimizerSetup.Intervals.Average"); break;
		case OPTIMIZE_BY_INTERVAL_YES: s=Language.trPrimary("XML.OptimizerSetup.Intervals.Individual"); break;
		case OPTIMIZE_BY_INTERVAL_IN_ORDER: s=Language.trPrimary("XML.OptimizerSetup.Intervals.InOrder"); break;
		}
		node.appendChild(e=doc.createElement(Language.trPrimary("XML.OptimizerSetup.Intervals"))); e.setTextContent(s);

		if (optimizeIntervals.getMin()<1) {
			node.appendChild(e=doc.createElement(Language.trPrimary("XML.OptimizerSetup.UseIntervals")));
			e.setTextContent(optimizeIntervals.storeToString());
		}

		switch (optimizeGroups) {
		case OPTIMIZE_GROUPS_AVERAGE: s=Language.trPrimary("XML.OptimizerSetup.Groups.Average"); break;
		case OPTIMIZE_GROUPS_ALL: s=Language.trPrimary("XML.OptimizerSetup.Groups.All"); break;
		case OPTIMIZE_GROUPS_SELECTION: s=Language.trPrimary("XML.OptimizerSetup.Groups.Selection"); break;
		}
		node.appendChild(e=doc.createElement(Language.trPrimary("XML.OptimizerSetup.Groups"))); e.setTextContent(s);

		if (optimizeGroups==OptimizeGroups.OPTIMIZE_GROUPS_SELECTION && optimizeGroupNames!=null) for (int i=0;i<optimizeGroupNames.length;i++) {
			node.appendChild(e=doc.createElement(Language.trPrimary("XML.OptimizerSetup.Group"))); e.setTextContent(optimizeGroupNames[i]);
		}

		s=NumberTools.formatNumber(changeValue*100)+"%";
		node.appendChild(e=doc.createElement(Language.trPrimary("XML.OptimizerSetup.ModificationValue"))); e.setTextContent(s);

		if (changeAll) s=Language.trPrimary("XML.OptimizerSetup.ModificationGroups.All"); else s=Language.trPrimary("XML.OptimizerSetup.ModificationGroups.Selection");
		node.appendChild(e=doc.createElement(Language.trPrimary("XML.OptimizerSetup.ModificationGroups"))); e.setTextContent(s);

		if (!changeAll && changeGroups!=null) for (int i=0;i<changeGroups.length;i++) {
			node.appendChild(e=doc.createElement(Language.trPrimary("XML.OptimizerSetup.ModificationGroup"))); e.setTextContent(changeGroups[i]);
		}

		if (!uebertragFile.isEmpty() || uebertrag.size()>0) {
			node.appendChild(e=doc.createElement(Language.trPrimary("XML.OptimizerSetup.CarryOver")));
			if (!uebertragFile.isEmpty()) e.setAttribute(Language.trPrimary("XML.OptimizerSetup.CarryOver.StatisticFile"),uebertragFile);
			Iterator<String> it=uebertrag.keySet().iterator();
			while (it.hasNext()) {
				String name=it.next();
				uebertrag.get(name).saveToXML(e,name);
			}
		}

		if (uebertragAdditionalCaller.size()>0) {
			node.appendChild(e=doc.createElement(Language.trPrimary("XML.OptimizerSetup.InitialInventory")));
			for (int i=0;i<uebertragAdditionalCaller.size();i++) {
				e.appendChild(e2=doc.createElement(Language.trPrimary("XML.OptimizerSetup.InitialInventory.ClientType")));
				e2.setAttribute(Language.trPrimary("XML.OptimizerSetup.InitialInventory.ClientType.Name"),uebertragAdditionalCaller.get(i));
				int j=uebertragAdditionalCount.get(i);
				e2.setTextContent(""+j);
			}
		}

		if (groupRestrictionName.size()>0) {
			for (int i=0;i<groupRestrictionName.size();i++) {
				node.appendChild(e=doc.createElement(Language.trPrimary("XML.OptimizerSetup.GroupRestriction")));
				e.setAttribute(Language.trPrimary("XML.OptimizerSetup.GroupRestriction.Name"),groupRestrictionName.get(i));
				e.setAttribute(Language.trPrimary("XML.OptimizerSetup.GroupRestriction.Min"),groupRestrictionMin.get(i).storeToString());
				e.setAttribute(Language.trPrimary("XML.OptimizerSetup.GroupRestriction.Max"),groupRestrictionMax.get(i).storeToString());
			}
		}
	}

	/**
	 * Versucht die Optimierer-Einstellungen aus der angegebenen XML-Datei zu laden
	 * @param file	Dateiname der XML-Datei, aus der die Einstellungen geladen werden sollen
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
	 * Versucht die Optimierer-Einstellungen aus dem übergebenen XML-Node zu laden
	 * @param node	XML-Knoten, der die Einstellungen enthält
	 * @return	Tritt ein Fehler auf, so wird die Fehlermeldung als String zurückgegeben. Im Erfolgsfall wird <code>null</code> zurückgegeben.
	 */
	public String loadFromXML(Element node) {
		optimizeProperty=OptimizeProperty.OPTIMIZE_PROPERTY_SUCCESS_BY_CALL;
		optimizeValue=0.95;
		optimizeMaxValue=-1;
		optimizeByInterval=OptimizeInterval.OPTIMIZE_BY_INTERVAL_NO;
		optimizeIntervals.setToValue(1);
		optimizeGroups=OptimizeGroups.OPTIMIZE_GROUPS_AVERAGE;
		optimizeGroupNames=null;
		changeValue=0.01;
		changeAll=true;
		changeGroups=null;
		uebertragFile="";
		uebertrag.clear();
		uebertragAdditionalCaller.clear();
		uebertragAdditionalCount.clear();
		groupRestrictionName.clear();
		groupRestrictionMin.clear();
		groupRestrictionMax.clear();

		if (!Language.trAll("XML.OptimizerSetup",node.getNodeName())) return String.format(Language.tr("XML.OptimizerSetup.Error"),Language.trPrimary("XML.OptimizerSetup"));

		NodeList l=node.getChildNodes();
		for (int i=0; i<l.getLength();i++) {
			if (!(l.item(i) instanceof Element)) continue;
			Element e=(Element)l.item(i);
			String s=e.getNodeName();

			if (Language.trAll("XML.OptimizerSetup.Property",s)) {
				String t=e.getTextContent();
				boolean ok=false;
				if (Language.trAll("XML.OptimizerSetup.Property.AccessibilityCalls",t)) {optimizeProperty=OptimizeProperty.OPTIMIZE_PROPERTY_SUCCESS_BY_CALL; ok=true;}
				if (Language.trAll("XML.OptimizerSetup.Property.AccessibilityClients",t)) {optimizeProperty=OptimizeProperty.OPTIMIZE_PROPERTY_SUCCESS_BY_CLIENT; ok=true;}
				if (Language.trAll("XML.OptimizerSetup.Property.WaitingTimeCalls",t)) {optimizeProperty=OptimizeProperty.OPTIMIZE_PROPERTY_WAITING_TIME_BY_CALL; ok=true;}
				if (Language.trAll("XML.OptimizerSetup.Property.WaitingTimeClients",t)) {optimizeProperty=OptimizeProperty.OPTIMIZE_PROPERTY_WAITING_TIME_BY_CLIENT; ok=true;}
				if (Language.trAll("XML.OptimizerSetup.Property.HoldingTimeCalls",t)) {optimizeProperty=OptimizeProperty.OPTIMIZE_PROPERTY_STAYING_TIME_BY_CALL; ok=true;}
				if (Language.trAll("XML.OptimizerSetup.Property.HoldingTimeClients",t)) {optimizeProperty=OptimizeProperty.OPTIMIZE_PROPERTY_STAYING_TIME_BY_CLIENT; ok=true;}
				if (Language.trAll("XML.OptimizerSetup.Property.ServiceLevelCallsSuccess",t)) {optimizeProperty=OptimizeProperty.OPTIMIZE_PROPERTY_SERVICE_LEVEL_BY_CALL; ok=true;}
				if (Language.trAll("XML.OptimizerSetup.Property.ServiceLevelCallsAll",t)) {optimizeProperty=OptimizeProperty.OPTIMIZE_PROPERTY_SERVICE_LEVEL_BY_CALL_ALL; ok=true;}
				if (Language.trAll("XML.OptimizerSetup.Property.ServiceLevelClientsSuccess",t)) {optimizeProperty=OptimizeProperty.OPTIMIZE_PROPERTY_SERVICE_LEVEL_BY_CLIENT; ok=true;}
				if (Language.trAll("XML.OptimizerSetup.Property.ServiceLevelClientsAll",t)) {optimizeProperty=OptimizeProperty.OPTIMIZE_PROPERTY_SERVICE_LEVEL_BY_CLIENT_ALL; ok=true;}
				if (Language.trAll("XML.OptimizerSetup.Property.WorkLoad",t)) {optimizeProperty=OptimizeProperty.OPTIMIZE_PROPERTY_WORK_LOAD; ok=true;}
				if (!ok) return Language.tr("XML.OptimizerSetup.Property.Error");
				continue;
			}

			if (Language.trAll("XML.OptimizerSetup.Value",s)) {
				String t=e.getTextContent();
				Long time=TimeTools.getTime(t);
				if (time!=null) {optimizeValue=time; continue;}
				Double d=NumberTools.getProbability(t);
				if (d!=null) {optimizeValue=d; continue;}
				return String.format(Language.tr("XML.OptimizerSetup.Value.Error"),t);
			}

			if (Language.trAll("XML.OptimizerSetup.ValueMaximum",s)) {
				String t=e.getTextContent();
				Long time=TimeTools.getTime(t);
				if (time!=null) {optimizeMaxValue=time; continue;}
				Double d=NumberTools.getProbability(t);
				if (d!=null) {optimizeMaxValue=d; continue;}
				return String.format(Language.tr("XML.OptimizerSetup.ValueMaximum.Error"),t);
			}

			if (Language.trAll("XML.OptimizerSetup.Intervals",s)) {
				String t=e.getTextContent();
				boolean ok=false;
				if (Language.trAll("XML.OptimizerSetup.Intervals.Average",t)) {optimizeByInterval=OptimizeInterval.OPTIMIZE_BY_INTERVAL_NO; ok=true;}
				if (Language.trAll("XML.OptimizerSetup.Intervals.Individual",t)) {optimizeByInterval=OptimizeInterval.OPTIMIZE_BY_INTERVAL_YES; ok=true;}
				if (Language.trAll("XML.OptimizerSetup.Intervals.InOrder",t)) {optimizeByInterval=OptimizeInterval.OPTIMIZE_BY_INTERVAL_IN_ORDER; ok=true;}
				if (!ok) return Language.tr("XML.OptimizerSetup.Intervals.Error");
				continue;
			}

			if (Language.trAll("XML.OptimizerSetup.UseIntervals",s)) {
				String t=e.getTextContent();
				if (t!=null && !t.isEmpty()) {
					DataDistributionImpl dist=DataDistributionImpl.createFromString(t,48);
					if (dist==null) return String.format(Language.tr("XML.OptimizerSetup.UseIntervals.Error"),t);
					if (dist.densityData.length!=48) return String.format(Language.tr("XML.OptimizerSetup.UseIntervals.ErrorValueCount"),dist.densityData.length);
					optimizeIntervals=dist;
				}
			}

			if (Language.trAll("XML.OptimizerSetup.Groups",s)) {
				String t=e.getTextContent();
				boolean ok=false;
				if (Language.trAll("XML.OptimizerSetup.Groups.Average",t)) {optimizeGroups=OptimizeGroups.OPTIMIZE_GROUPS_AVERAGE; ok=true;}
				if (Language.trAll("XML.OptimizerSetup.Groups.All",t)) {optimizeGroups=OptimizeGroups.OPTIMIZE_GROUPS_ALL; ok=true;}
				if (Language.trAll("XML.OptimizerSetup.Groups.Selection",t)) {optimizeGroups=OptimizeGroups.OPTIMIZE_GROUPS_SELECTION; ok=true;}
				if (!ok) return Language.tr("XML.OptimizerSetup.Groups.Error");
				continue;
			}

			if (Language.trAll("XML.OptimizerSetup.Group",s)) {
				List<String> list;
				if (optimizeGroupNames==null || optimizeGroupNames.length==0) list=new ArrayList<>(); else list=new ArrayList<>(Arrays.asList(optimizeGroupNames));
				list.add(e.getTextContent());
				optimizeGroupNames=list.toArray(String[]::new);
				continue;
			}

			if (Language.trAll("XML.OptimizerSetup.ModificationValue",s)) {
				Double d=NumberTools.getExtProbability(e.getTextContent());
				if (d==null) return Language.tr("XML.OptimizerSetup.ModificationValue.Error");
				changeValue=d;
				continue;
			}

			if (Language.trAll("XML.OptimizerSetup.ModificationGroups",s)) {
				String t=e.getTextContent();
				boolean ok=false;
				if (Language.trAll("XML.OptimizerSetup.ModificationGroups.All",t)) {changeAll=true; ok=true;}
				if (Language.trAll("XML.OptimizerSetup.ModificationGroups.Selection",t)) {changeAll=false; ok=true;}
				if (!ok) return Language.tr("XML.OptimizerSetup.ModificationGroups.Error");
				continue;
			}

			if (Language.trAll("XML.OptimizerSetup.ModificationGroup",s)) {
				List<String> list;
				if (changeGroups==null || changeGroups.length==0) list=new ArrayList<>(); else list=new ArrayList<>(Arrays.asList(changeGroups));
				list.add(e.getTextContent());
				changeGroups=list.toArray(String[]::new);
				continue;
			}


			if (Language.trAll("XML.OptimizerSetup.CarryOver",s)) {
				uebertragFile=Language.trAllAttribute("XML.OptimizerSetup.CarryOver.StatisticFile",e);

				HashMap<String,ConnectedModelUebertrag> h=new HashMap<>();
				NodeList l3=e.getChildNodes();
				for (int k=0; k<l3.getLength();k++) {
					if (!(l3.item(k) instanceof Element)) continue;
					Element e3=(Element)l3.item(k);
					if (!Language.trAll("XML.Connected.CarryOver.Single",e3.getNodeName())) continue;
					ConnectedModelUebertrag u=new ConnectedModelUebertrag();
					String s1=u.loadFromXML(e3);
					if (s1==null) return Language.tr("XML.Connected.CarryOver.Single.ErrorOptimize");
					h.put(s1,u);
				}
				uebertrag=h;
				continue;
			}

			if (Language.trAll("XML.OptimizerSetup.InitialInventory",s)) {
				NodeList l3=e.getChildNodes();
				for (int k=0; k<l3.getLength();k++) {
					if (!(l3.item(k) instanceof Element)) continue;
					Element e3=(Element)l3.item(k);
					if (!Language.trAll("XML.OptimizerSetup.InitialInventory.ClientType",e3.getNodeName())) continue;
					String u=Language.trAllAttribute("XML.OptimizerSetup.InitialInventory.ClientType.Name",e3); if (u.isEmpty()) return Language.tr("XML.OptimizerSetup.InitialInventory.ClientType.Name.Error");
					Integer I=NumberTools.getNotNegativeInteger(e3.getTextContent()); if (I==null) return String.format(Language.tr("XML.OptimizerSetup.InitialInventory.ClientType.Error"),u);
					uebertragAdditionalCaller.add(u);
					uebertragAdditionalCount.add(I);
				}
				continue;
			}

			if (Language.trAll("XML.OptimizerSetup.GroupRestriction",s)) {
				String name=Language.trAllAttribute("XML.OptimizerSetup.GroupRestriction.Name",e);
				if (name.isEmpty()) return String.format(Language.tr("XML.OptimizerSetup.GroupRestriction.Name.Error"),groupRestrictionName.size()+1);
				String min=Language.trAllAttribute("XML.OptimizerSetup.GroupRestriction.Min",e);
				if (min.isEmpty()) return String.format(Language.tr("XML.OptimizerSetup.GroupRestriction.Min.Error"),groupRestrictionMin.size()+1);
				String max=Language.trAllAttribute("XML.OptimizerSetup.GroupRestriction.Max",e);
				if (max.isEmpty()) return String.format(Language.tr("XML.OptimizerSetup.GroupRestriction.Max.Error"),groupRestrictionMax.size()+1);

				DataDistributionImpl minDist=DataDistributionImpl.createFromString(min,86399);
				if (minDist==null) return String.format(Language.tr("XML.OptimizerSetup.GroupRestriction.Min.InvalidError"),groupRestrictionMin.size()+1,min);
				DataDistributionImpl maxDist=DataDistributionImpl.createFromString(max,86399);
				if (maxDist==null) return String.format(Language.tr("XML.OptimizerSetup.GroupRestriction.Max.InvalidError"),groupRestrictionMax.size()+1,max);

				groupRestrictionName.add(name);
				groupRestrictionMin.add(minDist);
				groupRestrictionMax.add(maxDist);
				continue;
			}
		}

		return null;
	}

	/**
	 * Teilt die Agentengruppen und Callcenter-Namen, die in einem String-Array übergeben wurden in zwei Arrays auf.
	 * @param data	Array mit Ausdrücken der Form "3-Callcentername".
	 * @return	Liefert ein <code>Object</code>-Array der Länge 2. Der erste Eintrag ist vom Typ <code>String[]</code> und der zweite vom Typ <code>int[]</code>.
	 */
	public static Object[] splitCallcenterAgentGroupData(String[] data) {
		String[] callcenterNames=new String[data.length];
		int[] groupNumbers=new int[data.length];
		for (int i=0;i<data.length;i++) {
			String s=data[i];
			int index=s.indexOf('-');
			if (index<0) {callcenterNames[i]=s; groupNumbers[i]=0; continue;}
			Long l=NumberTools.getLong(s.substring(0,index));
			if (l==null || l<=0) {callcenterNames[i]=s; groupNumbers[i]=0; continue;}
			callcenterNames[i]=s.substring(index+1);
			groupNumbers[i]=(int)((long)l);
		}

		Object[] result=new Object[2];
		result[0]=callcenterNames;
		result[1]=groupNumbers;
		return result;
	}

	/**
	 * Erstellt eine Kopie der Optimierereinstellungen
	 * @return	Kopie der Optimierereinstellungen
	 */
	public OptimizeSetup cloneOptimizeSetup() {
		final OptimizeSetup setup=new OptimizeSetup();

		setup.optimizeProperty=optimizeProperty;
		setup.optimizeValue=optimizeValue;
		setup.optimizeMaxValue=optimizeMaxValue;
		setup.optimizeByInterval=optimizeByInterval;
		setup.optimizeIntervals=optimizeIntervals.clone();
		setup.optimizeGroups=optimizeGroups;
		if (optimizeGroupNames!=null) {
			setup.optimizeGroupNames=new String[optimizeGroupNames.length];
			System.arraycopy(optimizeGroupNames,0,setup.optimizeGroupNames,0,optimizeGroupNames.length);
		}
		setup.changeValue=changeValue;
		setup.changeAll=changeAll;
		if (changeGroups!=null) {
			setup.changeGroups=new String[changeGroups.length];
			System.arraycopy(changeGroups,0,setup.changeGroups,0,changeGroups.length);
		}

		setup.uebertragFile=uebertragFile;
		setup.uebertrag=cloneUebertrag(uebertrag);

		setup.uebertragAdditionalCaller.addAll(uebertragAdditionalCaller);
		setup.uebertragAdditionalCount.addAll(uebertragAdditionalCount);

		setup.groupRestrictionName.addAll(groupRestrictionName);
		for (DataDistributionImpl dist : groupRestrictionMin) setup.groupRestrictionMin.add(dist.clone());
		for (DataDistributionImpl dist : groupRestrictionMax) setup.groupRestrictionMax.add(dist.clone());

		return setup;
	}

	/**
	 * Erstellt eine Kopie einer Übertrags-HashMap
	 * @param map	Zu kopierende Übertrags-HashMap
	 * @return	Kopierte Übertrags-HashMap
	 */
	public static HashMap<String,ConnectedModelUebertrag> cloneUebertrag(HashMap<String,ConnectedModelUebertrag> map) {
		HashMap<String,ConnectedModelUebertrag> result=new HashMap<>();

		Iterator<String> it=map.keySet().iterator();
		while (it.hasNext()) {
			String name=it.next();
			result.put(name,map.get(name).cloneUebertrag());
		}
		return result;
	}
}