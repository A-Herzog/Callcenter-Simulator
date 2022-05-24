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

import java.util.ArrayList;
import java.util.List;

import org.w3c.dom.Element;
import org.w3c.dom.NodeList;

import language.Language;
import mathtools.NumberTools;
import mathtools.TimeTools;
import mathtools.distribution.DataDistributionImpl;
import mathtools.distribution.tools.DistributionTools;

/**
 * Diese Klasse speichert alle Daten zu den Schwellenwert-Überschreitungs-Warnungen.<br>
 * Sie wird als Teil der Klasse {@link CallcenterModel} verwendet.
 * @author Alexander Herzog
 * @version 1.0
 * @see CallcenterModel
 */
public class CallcenterModelWarnings implements Cloneable {
	/** Handelt es sich um die Zusammenstellung der Warnungskonfigurationen für den Editor (<code>false</code>) oder sind dies Statistikdaten (<code>true</code>) */
	private final boolean withStatistics;

	/**
	 * Liste mit den in diesem Objekt enthaltenen Warnungsdatensätzen
	 * @see CallcenterModelWarnings.WarningRecord
	 */
	public final List<WarningRecord> records=new ArrayList<>();

	/**
	 * Konstruktor der Klasse
	 * @param withStatistics	Handelt es sich um die Zusammenstellung der Warnungskonfigurationen für den Editor (<code>false</code>) oder sind dies Statistikdaten (<code>true</code>)
	 */
	public CallcenterModelWarnings(final boolean withStatistics) {
		this.withStatistics=withStatistics;
	}

	@Override
	public CallcenterModelWarnings clone() {
		CallcenterModelWarnings warnings=new CallcenterModelWarnings(withStatistics);
		for (WarningRecord record : records) warnings.records.add(record.clone());
		return warnings;
	}

	/**
	 * Vergleicht die Liste der Warnungsdatensätze mit einer weiteren Liste
	 * @param warnings	Weitere Liste der Warnungsdatensätze
	 * @return	Liefert <code>true</code>, wenn die beiden Listen inhaltlich identisch sind
	 */
	public boolean equalsCallcenterModelWarnings(final CallcenterModelWarnings warnings) {
		List<WarningRecord> list=new ArrayList<>(records);

		for (WarningRecord record: warnings.records) {
			boolean ok=false;
			for (int i=0;i<list.size();i++) if (record.equalsWarningRecord(list.get(i),withStatistics)) {list.remove(i); ok=true; break;}
			if (!ok) return false;
		}
		if (list.size()>0) return false;

		return true;
	}

	/**
	 * Lädt die Daten aus einem XML-Element
	 * @param node	XML-Element aus dem die Daten geladen werden sollen
	 * @return	Liefert im Erfolgsfall <code>null</code> sonst eine Fehlermeldung
	 */
	public String loadFromXML(final Element node) {
		records.clear();

		NodeList l=node.getChildNodes();
		for (int i=0; i<l.getLength();i++) {
			if (!(l.item(i) instanceof Element)) continue;
			Element e=(Element)l.item(i);
			String s=e.getNodeName();

			if (Language.trAll("XML.Model.Warnings.Record",s)) {
				WarningRecord record=new WarningRecord();
				String t=record.loadFromXML(e,withStatistics); if (t!=null) return t+" ("+String.format(Language.tr("XML.Model.Warnings.Error"),records.size()+1)+")";
				records.add(record); continue;
			}
		}
		return null;
	}

	/**
	 * Speichert die Daten in einem XML-Element
	 * @param parent	Übergeordnetes Element für das neue XML-Element
	 */
	public final void saveToXML(final Element parent) {
		Element node;
		String elementName=(withStatistics)?Language.trPrimary("XML.Statistic.Warnings"):Language.trPrimary("XML.Model.Warnings");
		parent.appendChild(node=parent.getOwnerDocument().createElement(elementName));
		for (WarningRecord record : records) record.saveToXML(node,withStatistics);
	}

	/**
	 * Mögliche Typen von Schwellenwert-Überschreitungs-Warnungen
	 * @author Alexander Herzog
	 * @see CallcenterModelWarnings.WarningRecord#type
	 */
	public enum WarningType {
		/** Warnung, wenn die Wartezeit auf Anrufbasis zu lang ist */
		WARNING_TYPE_WAITINGTIME_CALL(0),

		/** Warnung, wenn die Wartezeit auf Kundenbasis zu lang ist */
		WARNING_TYPE_WAITINGTIME_CLIENT(1),

		/** Warnung, wenn die Verweilzeit auf Anrufbasis zu lang ist */
		WARNING_TYPE_RESIDENCETIME_CALL(2),

		/** Warnung, wenn die Verweilzeit auf Kundenbasis zu lang ist */
		WARNING_TYPE_RESIDENCETIME_CLIENT(3),

		/** Warnung, wenn die Erreichbarkeit auf Anrufbasis zu gering ist */
		WARNING_TYPE_SUCCESSPART_CALL(4),

		/** Warnung, wenn die Erreichbarkeit auf Kundenbasis zu gering ist */
		WARNING_TYPE_SUCCESSPART_CLIENT(5),

		/** Warning, wenn der Service-Levle auf Anrufbasis bezogen auf die erfolgreichen Anrufe zu gering ist */
		WARNING_TYPE_SERVICELEVEL_CALL_SUCCESSFUL(6),

		/** Warning, wenn der Service-Levle auf Kundenbasis bezogen auf die erfolgreichen Kunden zu gering ist */
		WARNING_TYPE_SERVICELEVEL_CLIENTS_SUCCESSFUL(7),

		/** Warning, wenn der Service-Levle auf Anrufbasis bezogen auf alle Anrufe zu gering ist */
		WARNING_TYPE_SERVICELEVEL_CALL_ALL(8),

		/** Warning, wenn der Service-Levle auf Kundenbasis bezogen auf alle Kunden zu gering ist */
		WARNING_TYPE_SERVICELEVEL_CLIENTS_ALL(9),

		/** Warnung, wenn die Auslastung zu gering ist */
		WARNING_TYPE_WORKLOAD(10);

		/** ID des Typs */
		public final int id;

		/**
		 * Konstruktor der Enum
		 * @param id	ID des Typs
		 */
		WarningType(final int id) {
			this.id=id;
		}
	}

	/**
	 * Über welche Intervalle soll die als Schwellenwert
	 * zu betrachtende Kenngröße berechnet werden?
	 * @author Alexander Herzog
	 * @see CallcenterModelWarnings.WarningRecord#modeTime
	 * @see CallcenterModelWarnings.WarningRecord#modeGroup
	 */
	public enum WarningMode {
		/** Im Mittel über alle Intervalle (<code>modeTime</code>) bzw. Kunden- oder Agentengruppen (<code>modeGroups</code>) */
		WARNING_MODE_AVERAGE(0),

		/** Für jedes Intervall einzeln (<code>modeTime</code>) bzw. für jede Kunden- oder Agentengruppen einzeln (<code>modeGroups</code>) */
		WARNING_MODE_EACH(1),

		/** Für ausgewählte Intervalle (<code>modeTime</code>) bzw. für eine bestimmte Kunden- oder Agentengruppe (<code>modeGroups</code>) */
		WARNING_MODE_SELECTED(2);

		/** ID des Modus */
		public final int id;

		/**
		 * Konstruktor der Enum
		 * @param id	ID des Modus
		 */
		WarningMode(final int id) {
			this.id=id;
		}
	}

	/**
	 * Status für einen einzelnen Warnungsdatensatz
	 * (im Falle der Variante mit Statistikergebnissen)
	 * @author Alexander Herzog
	 * @see CallcenterModelWarnings.WarningRecord#warningStatus
	 */
	public enum WarningStatus {
		/** Warnungsstatus: in Ordnung, keine Warnung */
		WARNING_STATUS_OK(0),

		/** Warnungsstatus: Warnung gelb */
		WARNING_STATUS_YELLOW(1),

		/** Warnungsstatus: Warnung rot */
		WARNING_STATUS_RED(2);

		/** ID des Status */
		public final int id;

		/**
		 * Konstruktor der Enum
		 * @param id	ID des Status
		 */
		WarningStatus(final int id) {
			this.id=id;
		}
	}

	/**
	 * Diese Klasse speichert alle Daten zu einer einzelnen Schwellenwert-Überschreitungs-Warnung.<br>
	 * Sie wird als Teil der Klasse {@link CallcenterModelWarnings} verwendet.
	 * @author Alexander Herzog
	 * @version 1.0
	 * @see CallcenterModelWarnings
	 */
	public static class WarningRecord implements Cloneable {
		/** Gibt an, auf welche Eigenschaft geachtet werden soll (siehe <code>WARNING_TYPE_*</code>) */
		public WarningType type=WarningType.WARNING_TYPE_WAITINGTIME_CALL;

		/** Gibt an, in welchem Zeitraum die Eigenschaft erfüllt sein muss (siehe <code>WARNING_MODE_*</code>) */
		public WarningMode modeTime=WarningMode.WARNING_MODE_AVERAGE;

		/** Gibt im Falle von <code>modeTime==WARNING_MODE_SELECTED</code> an, welche Intervalle betrachtet werden sollen. (Kann <code>null</code> sein oder eine Verteilung aus 48 Werten, die auf 0 oder 1 lauten.) */
		public DataDistributionImpl intervals=null;

		/** Gibt an, für welche Kunden- oder Agentengruppen die Eigenschaft erfüllt sein muss (siehe <code>WARNING_MODE_*</code>) */
		public WarningMode modeGroup=WarningMode.WARNING_MODE_AVERAGE;

		/** Gibt im Falle von <code>modeTime==WARNING_MODE_SELECTED</code> an, welche Kunden oder Agentengruppe die Eigenschaft erfüllen muss. */
		public String group="";

		/** Gibt den Wert an, bei dem gerade so noch keine gelbe Warnung ausgegeben wird. (Werte &lt;0 stehen für "ausgeschaltet".) */
		public double warningYellow=-1;

		/** Gibt den Wert an, bei dem gerade so noch keine rote Warnung ausgegeben wird. (Werte &lt;0 stehen für "ausgeschaltet".) */
		public double warningRed=-1;

		/** Konkreter Wert (in der Variante für die Statistik-Daten) */
		public double value=-1;

		/** Warnungsstatus (in der Variante für die Statistik-Daten) */
		public WarningStatus warningStatus=WarningStatus.WARNING_STATUS_OK;

		/**
		 * Konstruktor der Klasse
		 */
		public WarningRecord() {
			/*
			 * Wird nur benötigt, um einen JavaDoc-Kommentar für diesen (impliziten) Konstruktor
			 * setzen zu können, damit der JavaDoc-Compiler keine Warnung mehr ausgibt.
			 */
		}

		@Override
		public WarningRecord clone() {
			WarningRecord record=new WarningRecord();

			/* Typ */
			record.type=type;

			/* Intervalle */
			record.modeTime=modeTime;
			record.intervals=null; if (intervals!=null) record.intervals=intervals.clone();

			/* Gruppen */
			record.modeGroup=modeGroup;
			record.group=group;

			/* Wertebereich */
			record.warningYellow=warningYellow;
			record.warningRed=warningRed;

			/* Wert */
			record.value=value;

			/* Warnungsstatus */
			record.warningStatus=warningStatus;

			return record;
		}

		/**
		 * Vergleich diesen Schwellenwert-Überschreitungs-Warnungsdatensatz mit einem weiteren Datensatz.
		 * @param record	Anderer Schwellenwert-Überschreitungs-Warnungsdatensatz
		 * @param withStatistics	Liegen konkrete Werte vor?
		 * @return	Liefert <code>true</code>, wenn beide Datensätze inhaltlich identisch sind.
		 */
		private boolean equalsWarningRecord(final WarningRecord record, final boolean withStatistics) {
			/* Typ */
			if (record.type!=type) return false;

			/* Intervalle */
			if (record.modeTime!=modeTime) return false;
			if (modeTime==WarningMode.WARNING_MODE_SELECTED && !DistributionTools.compare(record.intervals,intervals)) return false;

			/* Gruppen */
			if (record.modeGroup!=modeGroup) return false;
			if (record.modeGroup==WarningMode.WARNING_MODE_SELECTED) {
				if (record.group==null || record.group.isEmpty()) {
					if (group!=null && !group.isEmpty()) return false;
				} else {
					if (group==null) return false;
					if (!record.group.equalsIgnoreCase(group)) return false;
				}
			}

			/* Wertebereich */
			if (Math.abs(record.warningYellow-warningYellow)>0.00001) return false;
			if (Math.abs(record.warningRed-warningRed)>0.00001) return false;

			/* Wert und Warnungsstatus */
			if (withStatistics) {
				if (Math.abs(record.value-value)>0.00001) return false;
				if (record.warningStatus!=warningStatus) return false;
			}

			return true;
		}

		/**
		 * Lädt einen einzelnen Warnungs-Datensatz aus einem XML-Element
		 * @param node	XML-Element
		 * @param withStatistics	Handelt es sich um die Zusammenstellung der Warnungskonfigurationen für den Editor (<code>false</code>) oder sind dies Statistikdaten (<code>true</code>)
		 * @return	Liefert im Erfolgsfall <code>null</code>, sonst eine Fehlermeldung
		 */
		private String loadFromXML(final Element node, final boolean withStatistics) {
			/* Typ */
			String typeString=Language.trAllAttribute("XML.Model.Warnings.Record.Type",node);
			type=null;
			if (Language.trAll("XML.Model.Warnings.Record.Type.WaitingTimeCalls",typeString)) type=WarningType.WARNING_TYPE_WAITINGTIME_CALL;
			if (Language.trAll("XML.Model.Warnings.Record.Type.WaitingTimeClients",typeString)) type=WarningType.WARNING_TYPE_WAITINGTIME_CLIENT;
			if (Language.trAll("XML.Model.Warnings.Record.Type.ResidenceTimeCalls",typeString)) type=WarningType.WARNING_TYPE_RESIDENCETIME_CALL;
			if (Language.trAll("XML.Model.Warnings.Record.Type.ResidenceTimeClients",typeString)) type=WarningType.WARNING_TYPE_RESIDENCETIME_CLIENT;
			if (Language.trAll("XML.Model.Warnings.Record.Type.SuccessPartCalls",typeString)) type=WarningType.WARNING_TYPE_SUCCESSPART_CALL;
			if (Language.trAll("XML.Model.Warnings.Record.Type.SuccessPartClients",typeString)) type=WarningType.WARNING_TYPE_SUCCESSPART_CLIENT;
			if (Language.trAll("XML.Model.Warnings.Record.Type.ServiceLevelOnSuccessfulCalls",typeString)) type=WarningType.WARNING_TYPE_SERVICELEVEL_CALL_SUCCESSFUL;
			if (Language.trAll("XML.Model.Warnings.Record.Type.ServiceLevelOnSuccessfulClients",typeString)) type=WarningType.WARNING_TYPE_SERVICELEVEL_CLIENTS_SUCCESSFUL;
			if (Language.trAll("XML.Model.Warnings.Record.Type.ServiceLevelOnAllCalls",typeString)) type=WarningType.WARNING_TYPE_SERVICELEVEL_CALL_ALL;
			if (Language.trAll("XML.Model.Warnings.Record.Type.ServiceLevelOnAllClients",typeString)) type=WarningType.WARNING_TYPE_SERVICELEVEL_CLIENTS_ALL;
			if (Language.trAll("XML.Model.Warnings.Record.Type.Workload",typeString)) type=WarningType.WARNING_TYPE_WORKLOAD;
			if (type==null) return Language.tr("XML.Model.Warnings.Record.Type.Error.NoType");

			/* Intervalle */
			modeTime=null;
			String intervalsString=Language.trAllAttribute("XML.Model.Warnings.Record.Intervals",node);
			if (Language.trAll("XML.Model.Warnings.Record.Intervals.Average",intervalsString)) modeTime=WarningMode.WARNING_MODE_AVERAGE;
			if (Language.trAll("XML.Model.Warnings.Record.Intervals.Each",intervalsString)) modeTime=WarningMode.WARNING_MODE_EACH;
			if (modeTime==null) {
				DataDistributionImpl dist=DataDistributionImpl.createFromString(intervalsString,CallcenterModelCaller.freshCallsDistMaxX);
				if (dist==null || dist.densityData.length!=48) return Language.tr("XML.Model.Warnings.Record.Intervals.Error.NoInterval");
				modeTime=WarningMode.WARNING_MODE_SELECTED;
				intervals=dist;
			}

			/* Gruppen */
			modeGroup=null;
			String groupsString=Language.trAllAttribute("XML.Model.Warnings.Record.Groups",node);
			if (Language.trAll("XML.Model.Warnings.Record.Groups.Average",groupsString)) modeGroup=WarningMode.WARNING_MODE_AVERAGE;
			if (Language.trAll("XML.Model.Warnings.Record.Groups.Each",groupsString)) modeGroup=WarningMode.WARNING_MODE_EACH;
			if (modeGroup==null) {
				if ((groupsString.trim().isEmpty())) Language.tr("XML.Model.Warnings.Record.Groups.Error.NoGroup");
				modeGroup=WarningMode.WARNING_MODE_SELECTED;
				group=groupsString;
			}

			/* Wertebereich */
			String warningYellowString=Language.trAllAttribute("XML.Model.Warnings.Record.WarningYellow",node);
			String warningRedString=Language.trAllAttribute("XML.Model.Warnings.Record.WarningRed",node);
			if (warningYellowString.trim().isEmpty()) return Language.tr("XML.Model.Warnings.Record.WarningYellow.Error.NoValue");
			if (warningRedString.trim().isEmpty()) return Language.tr("XML.Model.Warnings.Record.WarningRed.Error.NoValue");

			Double D;
			switch (type) {
			case WARNING_TYPE_WAITINGTIME_CALL:
			case WARNING_TYPE_WAITINGTIME_CLIENT:
			case WARNING_TYPE_RESIDENCETIME_CALL:
			case WARNING_TYPE_RESIDENCETIME_CLIENT:
				D=TimeTools.getExactTime(warningYellowString);
				if (D==null || D<0) return Language.tr("XML.Model.Warnings.Record.WarningYellow.Error.InvalidValue");
				warningYellow=D;
				D=TimeTools.getExactTime(warningRedString);
				if (D==null || D<0) return Language.tr("XML.Model.Warnings.Record.WarningRed.Error.InvalidValue");
				warningRed=D;
				break;
			case WARNING_TYPE_SUCCESSPART_CALL:
			case WARNING_TYPE_SUCCESSPART_CLIENT:
			case WARNING_TYPE_SERVICELEVEL_CALL_SUCCESSFUL:
			case WARNING_TYPE_SERVICELEVEL_CLIENTS_SUCCESSFUL:
			case WARNING_TYPE_SERVICELEVEL_CALL_ALL:
			case WARNING_TYPE_SERVICELEVEL_CLIENTS_ALL:
			case WARNING_TYPE_WORKLOAD:
				D=NumberTools.getSystemProbability(warningYellowString);
				if (D==null || D<0) return Language.tr("XML.Model.Warnings.Record.WarningYellow.Error.InvalidValue");
				warningYellow=D;
				D=NumberTools.getSystemProbability(warningRedString);
				if (D==null || D<0) return Language.tr("XML.Model.Warnings.Record.WarningRed.Error.InvalidValue");
				warningRed=D;
				break;
			}

			/* Wert */
			if (withStatistics) {
				String valueString=Language.trAllAttribute("XML.Model.Warnings.Record.Value",node);
				if (valueString.trim().isEmpty()) return Language.tr("XML.Model.Warnings.Record.Value.Error.NoValue");


				switch (type) {
				case WARNING_TYPE_WAITINGTIME_CALL:
				case WARNING_TYPE_WAITINGTIME_CLIENT:
				case WARNING_TYPE_RESIDENCETIME_CALL:
				case WARNING_TYPE_RESIDENCETIME_CLIENT:
					D=TimeTools.getExactTime(valueString);
					if (D==null || D<0) return Language.tr("XML.Model.Warnings.Record.Value.Error.InvalidValue");
					value=D;
					break;
				case WARNING_TYPE_SUCCESSPART_CALL:
				case WARNING_TYPE_SUCCESSPART_CLIENT:
				case WARNING_TYPE_SERVICELEVEL_CALL_SUCCESSFUL:
				case WARNING_TYPE_SERVICELEVEL_CLIENTS_SUCCESSFUL:
				case WARNING_TYPE_SERVICELEVEL_CALL_ALL:
				case WARNING_TYPE_SERVICELEVEL_CLIENTS_ALL:
				case WARNING_TYPE_WORKLOAD:
					D=NumberTools.getSystemProbability(valueString);
					if (D==null || D<0) return Language.tr("XML.Model.Warnings.Record.Value.Error.InvalidValue");
					value=D;
					break;
				}
			}

			/* Warnungsstatus */
			if (withStatistics) {
				warningStatus=null;
				String warningStatusString=Language.trAllAttribute("XML.Model.Warnings.Record.Status",node);
				if (Language.trAll("XML.Model.Warnings.Record.Status.Ok",warningStatusString)) warningStatus=WarningStatus.WARNING_STATUS_OK;
				if (Language.trAll("XML.Model.Warnings.Record.Status.Yellow",warningStatusString)) warningStatus=WarningStatus.WARNING_STATUS_YELLOW;
				if (Language.trAll("XML.Model.Warnings.Record.Status.Red",warningStatusString)) warningStatus=WarningStatus.WARNING_STATUS_RED;
				if (warningStatus==null) return Language.tr("XML.Model.Warnings.Record.Status.Error.NoData");
			}

			return null;
		}

		/**
		 * Speichert einen einzelnen Warnungs-Datensatz in einem XML-Element
		 * @param parent	Übergeordnetes XML-Element
		 * @param withStatistics	Handelt es sich um die Zusammenstellung der Warnungskonfigurationen für den Editor (<code>false</code>) oder sind dies Statistikdaten (<code>true</code>)
		 */
		private final void saveToXML(final Element parent, final boolean withStatistics) {
			Element node;
			parent.appendChild(node=parent.getOwnerDocument().createElement(Language.trPrimary("XML.Model.Warnings.Record")));

			/* Typ */
			String typeString=Language.trPrimary("XML.Model.Warnings.Record.Type.WaitingTime");
			switch (type) {
			case WARNING_TYPE_WAITINGTIME_CALL: typeString=Language.trPrimary("XML.Model.Warnings.Record.Type.WaitingTimeCalls"); break;
			case WARNING_TYPE_WAITINGTIME_CLIENT: typeString=Language.trPrimary("XML.Model.Warnings.Record.Type.WaitingTimeClients"); break;
			case WARNING_TYPE_RESIDENCETIME_CALL: typeString=Language.trPrimary("XML.Model.Warnings.Record.Type.ResidenceTimeCalls"); break;
			case WARNING_TYPE_RESIDENCETIME_CLIENT: typeString=Language.trPrimary("XML.Model.Warnings.Record.Type.ResidenceTimeClients"); break;
			case WARNING_TYPE_SUCCESSPART_CALL: typeString=Language.trPrimary("XML.Model.Warnings.Record.Type.SuccessPartCalls"); break;
			case WARNING_TYPE_SUCCESSPART_CLIENT: typeString=Language.trPrimary("XML.Model.Warnings.Record.Type.SuccessPartClients"); break;
			case WARNING_TYPE_SERVICELEVEL_CALL_SUCCESSFUL: typeString=Language.trPrimary("XML.Model.Warnings.Record.Type.ServiceLevelOnSuccessfulCalls"); break;
			case WARNING_TYPE_SERVICELEVEL_CLIENTS_SUCCESSFUL: typeString=Language.trPrimary("XML.Model.Warnings.Record.Type.ServiceLevelOnSuccessfulClients"); break;
			case WARNING_TYPE_SERVICELEVEL_CALL_ALL: typeString=Language.trPrimary("XML.Model.Warnings.Record.Type.ServiceLevelOnAllCalls"); break;
			case WARNING_TYPE_SERVICELEVEL_CLIENTS_ALL: typeString=Language.trPrimary("XML.Model.Warnings.Record.Type.ServiceLevelOnAllClients"); break;
			case WARNING_TYPE_WORKLOAD: typeString=Language.trPrimary("XML.Model.Warnings.Record.Type.Workload"); break;
			}
			node.setAttribute(Language.trPrimary("XML.Model.Warnings.Record.Type"),typeString);

			/* Intervalle */
			String intervalsString=Language.trPrimary("XML.Model.Warnings.Record.Intervals.Average");
			switch (modeTime) {
			case WARNING_MODE_AVERAGE: intervalsString=Language.trPrimary("XML.Model.Warnings.Record.Intervals.Average"); break;
			case WARNING_MODE_EACH: intervalsString=Language.trPrimary("XML.Model.Warnings.Record.Intervals.Each"); break;
			case WARNING_MODE_SELECTED: if (intervals!=null) intervalsString=intervals.storeToString(); break;
			}
			node.setAttribute(Language.trPrimary("XML.Model.Warnings.Record.Intervals"),intervalsString);

			/* Gruppen */
			String groupsString=Language.trPrimary("XML.Model.Warnings.Record.Groups.Average");
			switch (modeGroup) {
			case WARNING_MODE_AVERAGE: groupsString=Language.trPrimary("XML.Model.Warnings.Record.Groups.Average"); break;
			case WARNING_MODE_EACH: groupsString=Language.trPrimary("XML.Model.Warnings.Record.Groups.Each"); break;
			case WARNING_MODE_SELECTED: if (group!=null && !group.isEmpty()) groupsString=group; break;
			}
			node.setAttribute(Language.trPrimary("XML.Model.Warnings.Record.Groups"),groupsString);

			/* Wertebereich */
			String warningYellowString="";
			String warningRedString="";
			String valueString="";
			switch (type) {
			case WARNING_TYPE_WAITINGTIME_CALL:
			case WARNING_TYPE_WAITINGTIME_CLIENT:
			case WARNING_TYPE_RESIDENCETIME_CALL:
			case WARNING_TYPE_RESIDENCETIME_CLIENT:
				if (warningYellow>=0) warningYellowString=TimeTools.formatExactSystemTime(Math.round(warningYellow));
				if (warningRed>=0) warningRedString=TimeTools.formatExactSystemTime(Math.round(warningRed));
				if (value>=0) valueString=TimeTools.formatExactSystemTime(Math.round(value));
				break;
			case WARNING_TYPE_SUCCESSPART_CALL:
			case WARNING_TYPE_SUCCESSPART_CLIENT:
			case WARNING_TYPE_SERVICELEVEL_CALL_SUCCESSFUL:
			case WARNING_TYPE_SERVICELEVEL_CLIENTS_SUCCESSFUL:
			case WARNING_TYPE_SERVICELEVEL_CALL_ALL:
			case WARNING_TYPE_SERVICELEVEL_CLIENTS_ALL:
			case WARNING_TYPE_WORKLOAD:
				if (warningYellow>=0) warningYellowString=NumberTools.formatSystemNumber(warningYellow*100)+"%";
				if (warningRed>=0) warningRedString=NumberTools.formatSystemNumber(warningRed*100)+"%";
				if (value>=0) valueString=NumberTools.formatSystemNumber(value*100)+"%";
				break;
			}
			if (!warningYellowString.isEmpty()) node.setAttribute(Language.trPrimary("XML.Model.Warnings.Record.WarningYellow"),warningYellowString);
			if (!warningRedString.isEmpty()) node.setAttribute(Language.trPrimary("XML.Model.Warnings.Record.WarningRed"),warningRedString);
			if (withStatistics) {
				if (!valueString.isEmpty()) node.setAttribute(Language.trPrimary("XML.Model.Warnings.Record.Value"),valueString);
				String warningStatusString="";
				switch (warningStatus) {
				case WARNING_STATUS_OK: warningStatusString=Language.trPrimary("XML.Model.Warnings.Record.Status.Ok"); break;
				case WARNING_STATUS_YELLOW: warningStatusString=Language.trPrimary("XML.Model.Warnings.Record.Status.Yellow"); break;
				case WARNING_STATUS_RED: warningStatusString=Language.trPrimary("XML.Model.Warnings.Record.Status.Red"); break;
				}
				if (!warningStatusString.isEmpty()) node.setAttribute(Language.trPrimary("XML.Model.Warnings.Record.Status"),warningStatusString);
			}
		}
	}
}
