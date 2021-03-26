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

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;

import language.Language;
import mathtools.NumberTools;
import mathtools.TimeTools;
import mathtools.distribution.DataDistributionImpl;
import mathtools.distribution.tools.DistributionTools;

/**
 * Modelliert eine Agentengruppe<br>
 * Sie wird als Teil der Klasse <code>CallcenterModelCallcenter</code> und damit als Teil der Klasse <code>CallcenterModel</code> verwendet.
 * @author Alexander Herzog
 * @version 1.0
 * @see CallcenterModelCallcenter
 * @see CallcenterModel
 * @see CallcenterModelSkillLevel
 * @see CallcenterRunModelAgent
 */
public final class CallcenterModelAgent implements Cloneable {
	/** Maximaler (Sekunden-)Wert für die Skalierung der Werte der Verteilungen über den Tag */
	public static final int countPerIntervalMaxX=86399;

	/** Anzahl der Angenten in dieser Gruppe (wenn =-1, aus Anzahl pro Halbstundenintervall aufbauen; wenn =-2, gemäß Kundenankünften) */
	public int count;

	/** Agentengruppe aktiv? */
	public boolean active;

	/** *** count==-1 *** */
	/** Anzahl pro Stunde */
	public DataDistributionImpl countPerInterval24;
	/** Anzahl pro Halbstundenintervall */
	public DataDistributionImpl countPerInterval48;
	/** Anzahl pro 15-Minuten-Intervall */
	public DataDistributionImpl countPerInterval96;
	/** Schicht bis 24 Uhr bis open end verlängern */
	public boolean lastShiftIsOpenEnd;

	/** *** count==-2 *** */
	/** Verfügbare Mann-Halbstunden **/
	public int byCallersAvailableHalfhours;
	/** Kundentypenverteilungen aus denen die Agentenverteilung berechnet werden soll */
	public List<String> byCallers;
	/** Gewichtung der einzelnen Kundentypen bei der Bestimmung der Agentenverteilung */
	public List<Double> byCallersRate;

	/** *** count>=0 *** */
	/** Beginn der Arbeitszeit (wenn count&ge;0) */
	public int workingTimeStart;
	/** Ende der Arbeitszeit (laufende Gespräche werden über das Arbeitszeitende hinaus zu Ende geführt) (wenn count&ge;0) */
	public int workingTimeEnd;
	/** Arbeitszeitende (<code>workingTimeEnd</code>) berücksichtigen ? (wenn count&ge;0) */
	public boolean workingNoEndTime;

	/** Name des <code>CallcenterModellSkillLevel</code>-Objektes */
	public String skillLevel;

	/** Kosten pro Arbeitsstunde (unabhängig von der Auslastung) */
	public double costPerWorkingHour;

	/** Namen der Kundentypen zur Bestimmung der Kosten */
	public List<String> costCallerTypes;
	/** Kosten pro Anruf */
	public List<Double> costPerCall;
	/** Kosten pro Gesprächsminute */
	public List<Double> costPerCallMinute;

	/** Bevorzugte Schichtlänge (in Halbstundenintervallen) wenn die Agenten aus Verteilungen aufgebaut werden (=-1, wenn die globale Vorgabe verwendet werden soll) */
	public int preferredShiftLength;

	/** Minimale Schichtlänge (in Halbstundenintervallen) wenn die Agenten aus Verteilungen aufgebaut werden (=-1, wenn die globale Vorgabe verwendet werden soll) */
	public int minimumShiftLength;

	/** Agenten-abhängige Produktivität pro Intervall (kann <code>null</code> sein, dann gilt der übergeordente Wert; Callcenter oder global), nur von Bedeutung bei count&lt;0 */
	public DataDistributionImpl efficiencyPerInterval;

	/** Agenten-abhängiger Planungsaufschlag pro Intervall (kann <code>null</code> sein, dann gilt der übergeordente Wert; Callcenter oder global), nur von Bedeutung bei count&lt;0 */
	public DataDistributionImpl additionPerInterval;

	/** Konstruktor der Klasse <code>CallcenterModelAgent</code> */
	public CallcenterModelAgent() {
		this("");
	}

	/**
	 * Konstruktor der Klasse <code>CallcenterModelAgent</code>
	 * @param	skillLevel	Skill-Level der neuen Agentengruppe
	 */
	public CallcenterModelAgent(final String skillLevel) {
		count=1;
		active=true;
		countPerInterval24=null;
		countPerInterval48=null;
		countPerInterval96=null;
		lastShiftIsOpenEnd=false;
		byCallersAvailableHalfhours=0;
		byCallers=new ArrayList<String>();
		byCallersRate=new ArrayList<Double>();
		workingTimeStart=0;
		workingTimeEnd=86400;
		workingNoEndTime=false;
		this.skillLevel=skillLevel;
		costPerWorkingHour=0;
		costCallerTypes=new ArrayList<String>();
		costPerCall=new ArrayList<Double>();
		costPerCallMinute=new ArrayList<Double>();
		preferredShiftLength=-1;
		minimumShiftLength=-1;
		efficiencyPerInterval=null;
		additionPerInterval=null;
	}

	@Override
	public CallcenterModelAgent clone() {
		CallcenterModelAgent agent=new CallcenterModelAgent();
		agent.count=count;
		agent.active=active;
		agent.countPerInterval24=null;
		agent.countPerInterval48=null;
		agent.countPerInterval96=null;
		if (countPerInterval24!=null) agent.setCountPerInterval(countPerInterval24.clone());
		if (countPerInterval48!=null) agent.setCountPerInterval(countPerInterval48.clone());
		if (countPerInterval96!=null) agent.setCountPerInterval(countPerInterval96.clone());
		agent.lastShiftIsOpenEnd=lastShiftIsOpenEnd;
		agent.byCallersAvailableHalfhours=byCallersAvailableHalfhours;
		for (int i=0;i<byCallers.size();i++) agent.byCallers.add(byCallers.get(i));
		for (int i=0;i<byCallersRate.size();i++) {
			double d=byCallersRate.get(i); /* Nicht einfach das Double-Objekt neu referenzieren, sondern echte Kopie */
			agent.byCallersRate.add(d);
		}
		agent.workingTimeStart=workingTimeStart;
		agent.workingTimeEnd=workingTimeEnd;
		agent.workingNoEndTime=workingNoEndTime;
		agent.skillLevel=skillLevel;
		agent.costPerWorkingHour=costPerWorkingHour;
		for (int i=0;i<costCallerTypes.size();i++) agent.costCallerTypes.add(costCallerTypes.get(i));
		for (int i=0;i<costPerCall.size();i++) agent.costPerCall.add(costPerCall.get(i));
		for (int i=0;i<costPerCallMinute.size();i++) agent.costPerCallMinute.add(costPerCallMinute.get(i));
		agent.preferredShiftLength=preferredShiftLength;
		agent.minimumShiftLength=minimumShiftLength;

		if (efficiencyPerInterval==null) agent.efficiencyPerInterval=null; else agent.efficiencyPerInterval=efficiencyPerInterval.clone();
		if (additionPerInterval==null) agent.additionPerInterval=null; else agent.additionPerInterval=additionPerInterval.clone();

		return agent;
	}

	/**
	 * Stellt die Anzahlverteilung über den Tag ein
	 * @param dist	Verteilungsfunktion
	 * @return	Gibt an, ob die Daten erfolgreich eingestellt werden konnten (dafür müssen 24, 48 oder 96 Werte übergeben werden)
	 */
	public boolean setCountPerInterval(double[] dist) {
		return setCountPerInterval(new DataDistributionImpl(countPerIntervalMaxX,dist));
	}

	/**
	 * Stellt die Anzahlverteilung über den Tag ein
	 * @param dist	Verteilungsfunktion
	 * @return	Gibt an, ob die Daten erfolgreich eingestellt werden konnten (dafür müssen 24, 48 oder 96 Werte übergeben werden)
	 */
	public boolean setCountPerInterval(DataDistributionImpl dist) {
		countPerInterval24=null;
		countPerInterval48=null;
		countPerInterval96=null;
		if (dist.densityData.length==24) {countPerInterval24=dist; return true;}
		if (dist.densityData.length==48) {countPerInterval48=dist; return true;}
		if (dist.densityData.length==96) {countPerInterval96=dist; return true;}
		return false;
	}

	/**
	 * Liefert die Anzahlverteilung über den Tag
	 * @return	Anzahlverteilung über den Tag
	 */
	public DataDistributionImpl getCountPerInterval() {
		if (countPerInterval24!=null) return countPerInterval24;
		if (countPerInterval48!=null) return countPerInterval48;
		if (countPerInterval96!=null) return countPerInterval96;
		return new DataDistributionImpl(countPerIntervalMaxX,48);

	}

	/**
	 * Rechnet die bisherige Verteilung der Agenten über den Tag
	 * auf die angegebene Anzahl an Intervallen um.
	 * Die Anzahlen muss 24, 48 oder 96 sein und es muss
	 * eine Verteilung vorliegen (was nicht für alle Agentengruppen
	 * der Fall sein muss).
	 * @param steps	Anzahl an Interallen auf die die Agentenverteilung umgerechnet werden soll
	 * @return	Liefert <code>true</code>, wenn die 24, 48 oder 96 Schritte angegeben wurden und eine Verteilung vorlag
	 */
	public boolean stretchCountPerInterval(int steps) {
		if (steps!=24 && steps!=48 && steps!=96) return false;
		DataDistributionImpl dist=null;
		if (countPerInterval24!=null) dist=countPerInterval24;
		if (countPerInterval48!=null) dist=countPerInterval48;
		if (countPerInterval96!=null) dist=countPerInterval96;
		if (dist==null) return false;
		if (dist.densityData.length==steps) return true;
		dist.stretchToValueCount(steps);
		return setCountPerInterval(dist);
	}

	/**
	 * Vergleicht dieses Objekt mit einem anderen {@link CallcenterModelAgent}-Objekt.
	 * @param agent	Anderes {@link CallcenterModelAgent}-Objekt
	 * @return	Liefert <code>true</code>, wenn beide Objekte inhaltlich identisch sind
	 */
	public boolean equalsCallcenterModelAgent(final CallcenterModelAgent agent) {
		if (agent==null) return false;

		if (agent.count!=count) return false;

		if (agent.active!=active) return false;

		if (agent.count==-1) {
			if (countPerInterval24!=null) {
				if (!DistributionTools.compare(agent.countPerInterval24,countPerInterval24)) return false;
			}
			if (countPerInterval48!=null) {
				if (!DistributionTools.compare(agent.countPerInterval48,countPerInterval48)) return false;
			}
			if (countPerInterval96!=null) {
				if (!DistributionTools.compare(agent.countPerInterval96,countPerInterval96)) return false;
			}
			if (agent.lastShiftIsOpenEnd!=lastShiftIsOpenEnd) return false;
		}

		if (agent.count==-2) {
			if (agent.byCallersAvailableHalfhours!=byCallersAvailableHalfhours) return false;
			if (agent.byCallers.size()!=byCallers.size()) return false;
			for (int i=0;i<byCallers.size();i++) if (!agent.byCallers.get(i).equals(byCallers.get(i))) return false;
			for (int i=0;i<byCallersRate.size();i++) if (!agent.byCallersRate.get(i).equals(byCallersRate.get(i))) return false;
		}

		if (agent.count>=0) {
			if (agent.workingTimeStart!=workingTimeStart) return false;
			if (agent.workingTimeEnd!=workingTimeEnd) return false;
			if (agent.workingNoEndTime!=workingNoEndTime) return false;
		}

		if (!agent.skillLevel.equals(skillLevel)) return false;

		if (agent.costPerWorkingHour!=costPerWorkingHour) return false;
		if (agent.costCallerTypes.size()!=costCallerTypes.size()) return false;
		for (int i=0;i<costCallerTypes.size();i++) if (!agent.costCallerTypes.get(i).equals(costCallerTypes.get(i))) return false;
		if (agent.costPerCall.size()!=costPerCall.size()) return false;
		for (int i=0;i<costPerCall.size();i++) if (!agent.costPerCall.get(i).equals(costPerCall.get(i))) return false;
		if (agent.costPerCallMinute.size()!=costPerCallMinute.size()) return false;
		for (int i=0;i<costPerCallMinute.size();i++) if (!agent.costPerCallMinute.get(i).equals(costPerCallMinute.get(i))) return false;

		if (agent.preferredShiftLength!=preferredShiftLength) return false;
		if (agent.minimumShiftLength!=minimumShiftLength) return false;

		if (agent.efficiencyPerInterval!=null && efficiencyPerInterval==null) return false;
		if (agent.efficiencyPerInterval==null && efficiencyPerInterval!=null) return false;
		if (agent.efficiencyPerInterval!=null && efficiencyPerInterval!=null && !DistributionTools.compare(agent.efficiencyPerInterval,efficiencyPerInterval)) return false;

		if (agent.additionPerInterval!=null && additionPerInterval==null) return false;
		if (agent.additionPerInterval==null && additionPerInterval!=null) return false;
		if (agent.additionPerInterval!=null && additionPerInterval!=null && !DistributionTools.compare(agent.additionPerInterval,additionPerInterval)) return false;

		return true;
	}

	/**
	 * Legt einen Agenten mit festen Arbeitszeiten an
	 * @param begin	Startintervall
	 * @param end	Endintervall
	 * @param intervalLength	Intervalllänge
	 * @param openEnd	Arbeiten bis open-end?
	 * @return	Neu erstellter Agent
	 * @see #calcAgentShiftsInt(int, int, boolean, CallcenterModelCallcenter, CallcenterModel, boolean)
	 */
	private CallcenterModelAgent createAgent(final int begin, final int end, final int intervalLength, final boolean openEnd) {
		CallcenterModelAgent agent=new CallcenterModelAgent();
		agent.count=1;
		agent.active=true;
		agent.workingTimeStart=begin*intervalLength;
		agent.workingTimeEnd=(end+1)*intervalLength;
		agent.workingNoEndTime=openEnd;
		agent.skillLevel=skillLevel;
		agent.costPerWorkingHour=costPerWorkingHour;
		for (int i=0;i<costCallerTypes.size();i++) agent.costCallerTypes.add(costCallerTypes.get(i));
		for (int i=0;i<costPerCall.size();i++) agent.costPerCall.add(costPerCall.get(i));
		for (int i=0;i<costPerCallMinute.size();i++) agent.costPerCallMinute.add(costPerCallMinute.get(i));
		agent.additionPerInterval=additionPerInterval;
		return agent;
	}

	/**
	 * Berechnet eine Verteilung der Agenten über den Tag
	 * @param caller	Zu berücksichtigende Anrufergruppen
	 * @return	Liefert <code>false</code>, wenn die Agenten über feste Arbeitszeiten definiert sind, sonst die hinterlegte Verteilung oder die auf Basis der Anrufer berechnete Verteilung
	 */
	public DataDistributionImpl calcAgentDistributionFromCallers(final List<CallcenterModelCaller> caller) {
		if (count>=0) return null;
		if (count==-1) {
			if (countPerInterval24!=null) {
				DataDistributionImpl dist=countPerInterval24.clone();
				dist.stretchToValueCount(48);
				return dist;
			}
			if (countPerInterval48!=null) {
				return countPerInterval48.clone();
			}
			if (countPerInterval96!=null) {
				return countPerInterval96.clone();
			}
		}

		DataDistributionImpl dist=new DataDistributionImpl(countPerIntervalMaxX,48);

		/* Kundenankünfteverteilungen mit korrekter Gewichtung aufaddieren */
		for (int i=0;i<Math.min(byCallers.size(),byCallersRate.size());i++) {
			int index=-1;
			for (int j=0;j<caller.size();j++) if (caller.get(j).name.equalsIgnoreCase(byCallers.get(i))) {index=j; break;}
			if (index<0) continue;
			DataDistributionImpl dist48=caller.get(index).getFreshCallsDistOn48Base();
			DataDistributionImpl temp=dist48.multiply(Math.max(0,byCallersRate.get(i)));
			dist=dist.add(temp);
		}

		/* Auf Anzahl an verfügbaren Halbstundenintervallen normieren */
		DataDistributionImpl result;
		double sum=dist.sum();
		if (sum>0) {
			dist=dist.multiply(byCallersAvailableHalfhours/sum);
			result=dist.round();
			sum=result.sum();
			dist=dist.max(result);
			while (sum<byCallersAvailableHalfhours) {
				int index=-1;
				double value=0;
				for (int i=0;i<dist.densityData.length;i++) {
					double d=dist.densityData[i]-Math.floor(dist.densityData[i]);
					if (d>value) {value=d; index=i;}
				}
				if (index==-1) break;
				result.densityData[index]++;
				dist.densityData[index]=result.densityData[index];
				sum++;
			}
		} else {
			result=new DataDistributionImpl(countPerIntervalMaxX,48);
		}

		return result;
	}

	/**
	 * Berechnet Agenten mit festen Arbeitszeiten (=Schichtplan) auf Basis der hinterlegten Daten
	 * @param lastIntervalOpenEnd	Ist das letzte Halbstundenintervall "open end"
	 * @param callcenter	Zugehöriges Callcenter
	 * @param model	Gesamtes Callcenter-Modell
	 * @param useProductivity	Sollen die Angaben zur Effizienz verwendet werden (d.h. die Agentenanzahl auf Netto-Agenten umgerechnet werden)
	 * @return	Array mit Agenten mit festen Arbeitszeiten
	 */
	public ArrayList<CallcenterModelAgent> calcAgentShifts(final boolean lastIntervalOpenEnd, final CallcenterModelCallcenter callcenter, final CallcenterModel model, final boolean useProductivity) {
		CallcenterModelAgent group;
		if (count==-2) {
			group=this.clone();
			group.setCountPerInterval(calcAgentDistributionFromCallers(model.caller));
			group.count=-1;
		} else {
			group=this;
		}

		int preferredShiftLength=model.preferredShiftLength;
		if (this.preferredShiftLength>0) preferredShiftLength=this.preferredShiftLength;

		int minimumShiftLength=model.minimumShiftLength;
		if (this.minimumShiftLength>0) minimumShiftLength=this.minimumShiftLength;

		return group.calcAgentShiftsInt(preferredShiftLength,minimumShiftLength,lastIntervalOpenEnd,callcenter,model,useProductivity);
	}

	/**
	 * Liefert die Verteilung der Produktivität pro Intervall.
	 * @param callcenter	Zugehöriges Callcenter
	 * @param model	Gesamtes Callcenter-Modell
	 * @return	Produktivität pro Intervall
	 */
	public DataDistributionImpl getEfficiency(final CallcenterModelCallcenter callcenter, final CallcenterModel model) {
		if (efficiencyPerInterval!=null) return efficiencyPerInterval;
		if (callcenter.efficiencyPerInterval!=null) return callcenter.efficiencyPerInterval;
		return model.efficiencyPerInterval;
	}

	/**
	 * Liefert die Verteilung der krankheitsbedingten Zuschläge pro Intervall.
	 * @param callcenter	Zugehöriges Callcenter
	 * @param model	Gesamtes Callcenter-Modell
	 * @return	Krankheitsbedingte Zuschläge pro Intervall
	 */
	public DataDistributionImpl getAddition(final CallcenterModelCallcenter callcenter, final CallcenterModel model) {
		if (additionPerInterval!=null) return additionPerInterval;
		if (callcenter.additionPerInterval!=null) return callcenter.additionPerInterval;
		return model.additionPerInterval;
	}

	/**
	 * Berechnet Agenten mit festen Arbeitszeiten (=Schichtplan) auf Basis der hinterlegten Daten
	 * @param preferredShiftLength	Gewünschte Schichtplänge
	 * @param minimumShiftLength	Minimale Schichtpläne
	 * @param lastIntervalOpenEnd	Ist das letzte Halbstundenintervall "open end"
	 * @param callcenter	Zugehöriges Callcenter
	 * @param model	Gesamtes Callcenter-Modell
	 * @param useProductivity	Sollen die Angaben zur Effizienz verwendet werden (d.h. die Agentenanzahl auf Netto-Agenten umgerechnet werden)
	 * @return	Array mit Agenten mit festen Arbeitszeiten
	 */
	private ArrayList<CallcenterModelAgent> calcAgentShiftsInt(int preferredShiftLength, int minimumShiftLength, final boolean lastIntervalOpenEnd, final CallcenterModelCallcenter callcenter, final CallcenterModel model, final boolean useProductivity) {
		ArrayList<CallcenterModelAgent> list=new ArrayList<CallcenterModelAgent>();

		if (this.preferredShiftLength>0) preferredShiftLength=this.preferredShiftLength;
		if (this.minimumShiftLength>0) minimumShiftLength=this.minimumShiftLength;

		/* Wenn die Arbeitszeiten bereits explizit gegeben sind, ist gar keine Schichtplanung nötig. */
		if (count>=0) {list.add(clone()); return list;}

		final ArrayList<Integer> workingAgents=new ArrayList<Integer>();

		DataDistributionImpl efficiencyPerInterval=getEfficiency(callcenter,model);

		double[] density=null;
		if (countPerInterval24!=null) density=countPerInterval24.densityData;
		if (countPerInterval48!=null) density=countPerInterval48.densityData;
		if (countPerInterval96!=null) density=countPerInterval96.densityData;
		if (density==null) return list;

		/* Erkennung von negativen Agentenzahlen */
		for (int i=0;i<density.length;i++) density[i]=Math.max(0,density[i]);
		/* Da die Schichtplanung schon vom Konstruktor von CallcenterRunModelCallcenter ausgelöst wird, können wir das nicht später in checkAndInit abfangen und melden.*/

		preferredShiftLength=(int)Math.round((double)preferredShiftLength*density.length/48);
		minimumShiftLength=(int)Math.round((double)minimumShiftLength*density.length/48);

		for (int i=0;i<density.length;i++) {
			/* Produktivität ggf. berücksichtigen */
			int c=(int)Math.max(0,Math.round(density[i]*(useProductivity?efficiencyPerInterval.densityData[(int)Math.round(Math.floor((double)i/density.length*48))]:1)));

			/* Feierabend für die ersten workingAgents.size()-c Agenten */
			while (c<workingAgents.size()) {
				int start=workingAgents.get(0);
				if (start+minimumShiftLength>i) break; /* Minimale Schichtlänge noch nicht erreicht */
				list.add(createAgent(workingAgents.get(0),i-1,86400/density.length,false));
				workingAgents.remove(0);
			}

			/* Schichtende prüfen */
			while (workingAgents.size()>0 && i-workingAgents.get(0)>=preferredShiftLength) {
				list.add(createAgent(workingAgents.get(0),i-1,86400/density.length,false));
				workingAgents.remove(0);
			}

			/* Agenten hinzufügen */
			if (c>workingAgents.size()) {
				int c2=c-workingAgents.size();
				for (int j=0;j<c2;j++) workingAgents.add(i);
			}
		}

		/* Feierabend für alle */
		while (workingAgents.size()>0) {
			list.add(createAgent(workingAgents.get(0),density.length-1,86400/density.length,lastIntervalOpenEnd));
			workingAgents.remove(0);
		}

		/* Agenten zu Gruppen zusammenfassen */
		int i=1;
		while (i<list.size()) {
			if (list.get(i).workingTimeStart==list.get(i-1).workingTimeStart && list.get(i).workingTimeEnd==list.get(i-1).workingTimeEnd) {
				/* Agent i zu i-1 hizufügen; i ändert sich nicht */
				list.get(i-1).count++;
				list.remove(i);
			} else {
				/* Neue Agentengruppe, i erhöhen */
				i++;
			}
		}

		/* Minimale Schichtlänge für Agenten bis Mitternacht erreicht */
		for (CallcenterModelAgent agents : list) if (agents.workingTimeEnd==86400)
			if ((86400-agents.workingTimeStart)<minimumShiftLength*86400/density.length) {
				agents.workingTimeStart=86400-(minimumShiftLength*86400/density.length);
			}

		return list;
	}

	/**
	 * Versucht einen Agenten-Datensatz aus dem übergebenen XML-Node zu laden
	 * @param node	XML-Knoten, der die Agenten-Daten enthält
	 * @return	Tritt ein Fehler auf, so wird die Fehlermeldung als String zurückgegeben. Im Erfolgsfall wird <code>null</code> zurückgegeben.
	 */
	public String loadFromXML(Element node) {
		count=-1;
		active=true;
		DataDistributionImpl newCountPerInterval=null;
		lastShiftIsOpenEnd=false;
		List<String> newByCaller=null;
		List<Double> newByCallerRate=null;

		workingTimeStart=0;
		workingTimeEnd=86400;
		workingNoEndTime=true;
		skillLevel="";

		byCallersAvailableHalfhours=0;
		byCallers=new ArrayList<String>();
		byCallersRate=new ArrayList<Double>();
		costPerWorkingHour=0;
		costCallerTypes=new ArrayList<String>();
		costPerCall=new ArrayList<Double>();
		costPerCallMinute=new ArrayList<Double>();
		preferredShiftLength=-1;
		minimumShiftLength=-1;
		efficiencyPerInterval=null;
		additionPerInterval=null;

		String a=Language.trAllAttribute("XML.Model.GeneralAttributes.Active",node);
		if (Language.trAll("XML.General.BoolFalse",a)) active=false;

		NodeList l=node.getChildNodes();
		for (int i=0; i<l.getLength();i++) {
			if (!(l.item(i) instanceof Element)) continue;
			Element e=(Element)l.item(i);
			String s=e.getNodeName();

			if (Language.trAll("XML.Model.AgentsGroup.Count",s)) {
				Integer J=NumberTools.getNotNegativeInteger(e.getTextContent());
				if (J==null) return String.format(Language.tr("XML.Model.AgentsGroup.Count.Error"),e.getTextContent());
				count=J; continue;
			}
			if (Language.trAll("XML.Model.AgentsGroup.WorkingTimeBegin",s)) {
				Integer J=TimeTools.getTime(e.getTextContent());
				if (J==null) return String.format(Language.tr("XML.Model.AgentsGroup.WorkingTimeBegin.Error"),e.getTextContent());
				workingTimeStart=J; continue;
			}
			if (Language.trAll("XML.Model.AgentsGroup.WorkingTimeEnd",s)) {
				Integer J=TimeTools.getTime(e.getTextContent());
				if (J==null) return String.format(Language.tr("XML.Model.AgentsGroup.WorkingTimeEnd.Error"),e.getTextContent());
				workingTimeEnd=J; workingNoEndTime=false; continue;
			}
			if (Language.trAll("XML.Model.AgentsGroup.Distribution",s)) {
				newCountPerInterval=DataDistributionImpl.createFromString(e.getTextContent(),countPerIntervalMaxX);
				if (newCountPerInterval==null) return Language.tr("XML.Model.AgentsGroup.Distribution.Error");
				String o=Language.trAllAttribute("XML.Model.AgentsGroup.Distribution.LastShiftIsOpenEnd",e);
				lastShiftIsOpenEnd=Language.trAll("XML.General.BoolTrue",o);
				continue;
			}
			if (Language.trAll("XML.Model.AgentsGroup.ByClients",s)) {
				String number=Language.trAllAttribute("XML.Model.AgentsGroup.ByClients.AgentsHalfHours",e);
				Integer I=NumberTools.getNotNegativeInteger(number);
				if (I==null) return String.format(Language.tr("XML.Model.AgentsGroup.ByClients.AgentsHalfHours.Error"),number);
				byCallersAvailableHalfhours=I;
				newByCaller=new ArrayList<String>();
				newByCallerRate=new ArrayList<Double>();
				NodeList l2=e.getChildNodes();
				for (int j=0; j<l2.getLength();j++) {
					if (!(l2.item(j) instanceof Element)) continue;
					Element e2=(Element)l2.item(j);
					String t=e2.getNodeName();
					if (!Language.trAll("XML.Model.AgentsGroup.ByClients.Group",t)) continue;
					String rate=Language.trAllAttribute("XML.Model.AgentsGroup.ByClients.Rate",e2);
					Double D=NumberTools.getNotNegativeDouble(NumberTools.systemNumberToLocalNumber(rate));
					if (D==null) return String.format(Language.tr("XML.Model.AgentsGroup.ByClients.Rate.Error"),rate);
					newByCallerRate.add(D);
					newByCaller.add(e2.getTextContent());
				}
				continue;
			}
			if (Language.trAll("XML.Model.AgentsGroup.SkillLevel",s)) {
				skillLevel=e.getTextContent();
				continue;
			}
			if (Language.trAll("XML.Model.AgentsGroup.CostsPerHour",s)) {
				Double D=NumberTools.getNotNegativeDouble(NumberTools.systemNumberToLocalNumber(e.getTextContent()));
				if (D==null) return String.format(Language.tr("XML.Model.AgentsGroup.CostsPerHour.Error"),e.getTextContent());
				costPerWorkingHour=D;
				continue;
			}
			if (Language.trAll("XML.Model.AgentsGroup.CostsPerClientType",s)) {
				String s1=Language.trAllAttribute("XML.Model.AgentsGroup.CostsPerClientType.ClientType",e);
				if (s1==null || s1.isEmpty()) return Language.tr("XML.Model.AgentsGroup.CostsPerClientType.ClientType.Error");
				String s2=Language.trAllAttribute("XML.Model.AgentsGroup.CostsPerClientType.PerCall",e);
				if (s2==null || s2.isEmpty()) return String.format(Language.tr("XML.Model.AgentsGroup.CostsPerClientType.PerCall.Error1"),s1);
				String s3=Language.trAllAttribute("XML.Model.AgentsGroup.CostsPerClientType.PerMinute",e);
				if (s3==null || s3.isEmpty()) return String.format(Language.tr("XML.Model.AgentsGroup.CostsPerClientType.PerMinute.Error1"),s1);
				Double D2=NumberTools.getNotNegativeDouble(NumberTools.systemNumberToLocalNumber(s2));
				Double D3=NumberTools.getNotNegativeDouble(NumberTools.systemNumberToLocalNumber(s3));
				if (D2==null) return String.format(Language.tr("XML.Model.AgentsGroup.CostsPerClientType.PerCall.Error2"),s1,s2);
				if (D3==null) return String.format(Language.tr("XML.Model.AgentsGroup.CostsPerClientType.PerMinute.Error2"),s1,s2);
				costCallerTypes.add(s1);
				costPerCall.add(D2);
				costPerCallMinute.add(D3);
				continue;
			}
			if (Language.trAll("XML.Model.PreferredShiftLength",s)) {
				Integer J=NumberTools.getInteger(e.getTextContent());
				if (J==null || J==0) return Language.tr("XML.Model.PreferredShiftLength.Error");
				if (J<0) J=-1;
				preferredShiftLength=J; continue;
			}
			if (Language.trAll("XML.Model.MinimumShiftLength",s)) {
				Integer J=NumberTools.getInteger(e.getTextContent());
				if (J==null || J==0) return Language.tr("XML.Model.MinimumShiftLength.Error");
				if (J<0) J=-1;
				minimumShiftLength=J; continue;
			}
			if (Language.trAll("XML.Model.Productivity",s)) {
				DataDistributionImpl newEfficiencyPerInterval=DataDistributionImpl.createFromString(e.getTextContent(),CallcenterModelAgent.countPerIntervalMaxX);
				if (newEfficiencyPerInterval==null) return Language.tr("XML.Model.Productivity.ErrorAgents");
				efficiencyPerInterval=newEfficiencyPerInterval;
				continue;
			}
			if (Language.trAll("XML.Model.Surcharge",s)) {
				DataDistributionImpl newAdditionPerInterval=DataDistributionImpl.createFromString(e.getTextContent(),CallcenterModelAgent.countPerIntervalMaxX);
				if (newAdditionPerInterval==null) return Language.tr("XML.Model.Surcharge.ErrorAgents");
				additionPerInterval=newAdditionPerInterval;
				continue;
			}
		}

		if (count==-1 && newCountPerInterval==null && newByCaller==null) return Language.tr("XML.Model.AgentsGroup.ErrorDistribution");
		if (newCountPerInterval!=null) {
			if (newCountPerInterval.densityData.length!=24 && newCountPerInterval.densityData.length!=48 && newCountPerInterval.densityData.length!=96)
				return String.format(Language.tr("XML.Model.AgentsGroup.ErrorDistributionNumberOfValues"),newCountPerInterval.densityData.length);
			count=-1; setCountPerInterval(newCountPerInterval);
		}
		if (newByCaller!=null) {count=-2; byCallers=newByCaller; byCallersRate=newByCallerRate;}

		return null;
	}

	/**
	 * Erstellt unterhalb des übergebenen XML-Knotens einen neuen Knoten, der die gesamten Agenten-Daten enthält.
	 * @param parent	Eltern-XML-Knoten
	 */
	public void saveToXML(Element parent) {
		Document doc=parent.getOwnerDocument();
		Element node=doc.createElement(Language.trPrimary("XML.Model.AgentsGroup")); parent.appendChild(node);
		if (!active) node.setAttribute(Language.trPrimary("XML.Model.GeneralAttributes.Active"),Language.trPrimary("XML.General.BoolFalse"));

		Element e,e2;

		if (count>=0) {
			node.appendChild(e=doc.createElement(Language.trPrimary("XML.Model.AgentsGroup.Count"))); e.setTextContent(""+count);
			node.appendChild(e=doc.createElement(Language.trPrimary("XML.Model.AgentsGroup.WorkingTimeBegin"))); e.setTextContent(TimeTools.formatTime(workingTimeStart));
			if (!workingNoEndTime) {
				node.appendChild(e=doc.createElement(Language.trPrimary("XML.Model.AgentsGroup.WorkingTimeEnd")));
				e.setTextContent(TimeTools.formatTime(workingTimeEnd));
			}
		} else {
			if (count==-1) {
				node.appendChild(e=doc.createElement(Language.trPrimary("XML.Model.AgentsGroup.Distribution")));
				if (countPerInterval24!=null) e.setTextContent(countPerInterval24.storeToString());
				if (countPerInterval48!=null) e.setTextContent(countPerInterval48.storeToString());
				if (countPerInterval96!=null) e.setTextContent(countPerInterval96.storeToString());
				if (lastShiftIsOpenEnd)	e.setAttribute(Language.trPrimary("XML.Model.AgentsGroup.Distribution.LastShiftIsOpenEnd"),Language.trPrimary("XML.General.BoolTrue"));
			}
			if (count==-2) {
				node.appendChild(e=doc.createElement(Language.trPrimary("XML.Model.AgentsGroup.ByClients")));
				e.setAttribute(Language.trPrimary("XML.Model.AgentsGroup.ByClients.AgentsHalfHours"),""+byCallersAvailableHalfhours);
				for (int i=0;i<Math.min(byCallers.size(),byCallersRate.size());i++) {
					e.appendChild(e2=doc.createElement(Language.trPrimary("XML.Model.AgentsGroup.ByClients.Group")));
					e2.setAttribute(Language.trPrimary("XML.Model.AgentsGroup.ByClients.Rate"),NumberTools.formatSystemNumber(byCallersRate.get(i)));
					e2.setTextContent(byCallers.get(i));
				}
			}
		}
		node.appendChild(e=doc.createElement(Language.trPrimary("XML.Model.AgentsGroup.SkillLevel"))); e.setTextContent(skillLevel);
		node.appendChild(e=doc.createElement(Language.trPrimary("XML.Model.AgentsGroup.CostsPerHour"))); e.setTextContent(NumberTools.formatSystemNumber(costPerWorkingHour));
		for (int i=0;i<costCallerTypes.size();i++) {
			node.appendChild(e=doc.createElement(Language.trPrimary("XML.Model.AgentsGroup.CostsPerClientType")));
			e.setAttribute(Language.trPrimary("XML.Model.AgentsGroup.CostsPerClientType.ClientType"),costCallerTypes.get(i));
			e.setAttribute(Language.trPrimary("XML.Model.AgentsGroup.CostsPerClientType.PerCall"),NumberTools.formatSystemNumber(costPerCall.get(i)));
			e.setAttribute(Language.trPrimary("XML.Model.AgentsGroup.CostsPerClientType.PerMinute"),NumberTools.formatSystemNumber(costPerCallMinute.get(i)));
		}

		if (preferredShiftLength>=0) {
			node.appendChild(e=doc.createElement(Language.trPrimary("XML.Model.PreferredShiftLength"))); e.setTextContent(""+preferredShiftLength);
		}
		if (minimumShiftLength>=0) {
			node.appendChild(e=doc.createElement(Language.trPrimary("XML.Model.MinimumShiftLength"))); e.setTextContent(""+minimumShiftLength);
		}
		if (efficiencyPerInterval!=null) {
			node.appendChild(e=doc.createElement(Language.trPrimary("XML.Model.Productivity"))); e.setTextContent(efficiencyPerInterval.storeToString());
		}
		if (additionPerInterval!=null) {
			node.appendChild(e=doc.createElement(Language.trPrimary("XML.Model.Surcharge"))); e.setTextContent(additionPerInterval.storeToString());
		}
	}
}