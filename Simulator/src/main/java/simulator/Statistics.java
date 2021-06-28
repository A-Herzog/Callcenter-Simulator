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
package simulator;

import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.apache.commons.math3.distribution.NormalDistribution;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;

import language.Language;
import mathtools.NumberTools;
import mathtools.TimeTools;
import mathtools.distribution.DataDistributionImpl;
import statistics.StatisticsBase;
import statistics.StatisticsSimulationBaseData;
import ui.VersionConst;
import ui.model.CallcenterModel;
import ui.model.CallcenterModelAgent;
import ui.model.CallcenterModelCallcenter;
import ui.model.CallcenterModelCaller;
import ui.model.CallcenterModelWarnings;
import ui.model.CallcenterRunModel;
import ui.model.CallcenterRunModelCaller;
import ui.statistic.model.StatisticViewerErlangCTools;

/**
 * Speichert die Statistikdaten, die während der Simulation aufgezeichnet werden.
 * @author Alexander Herzog
 * @version 1.0
 */
public final class Statistics extends StatisticsBase {
	/**
	 * Das Editor-Modell wird mit in der Statistik gespeichert. So ist immer nachvollziehbar, auf welches Modell sich die Statistik bezieht.
	 */
	public CallcenterModel editModel;

	/**
	 * Technische Basisdaten zur Simulation
	 */
	public StatisticsSimulationBaseData simulationData;

	/** Globale Kunden-Statistik */
	public KundenDaten kundenGlobal;

	/** Daten pro Kundentyp */
	public KundenDaten[] kundenProTyp;

	/** Globale Agenten-Statistik */
	public AgentenDaten agentenGlobal;

	/** Daten pro Callcenter */
	public AgentenDaten[] agentenProCallcenter;

	/** Daten pro Skilllevel */
	public AgentenDaten[] agentenProSkilllevel;

	/** Mittlere Warteschlangenlänge */
	public double meanQueueLength; /* muss durch simDays geteilt werden (wird von finalQueueLengthCalc erledigt) */

	/** Maximal aufgetretende Warteschlangenlänge */
	public int maxQueueLength;

	/** Mittlere Warteschlangenlänge pro betrachtetem Intervall */
	public DataDistributionImpl meanQueueLengthProIntervall=new DataDistributionImpl(48,48); /* muss durch simDays geteilt werden (wird von finalQueueLengthCalc erledigt) */

	/** Agentenzahlen (laut Modell) - insgesamt */
	public AgentModelData agentenModellGlobal;

	/** Agentenzahlen (laut Modell) - pro Agentengruppe */
	public AgentModelData[] agentenModellProGruppe;

	/** Schwellenwert-Warnungen */
	public CallcenterModelWarnings warnings=null;

	/**
	 * Mögliche Namen des Basiselement von Statistik-XML-Dateien (zur Erkennung von Dateien dieses Typs.)
	 */
	public static final String[] XMLBaseElement=Language.trAll("XML.Statistic.BaseElement");

	/**
	 * Konstruktor der Klasse
	 * @param editModel	Editor-Modell
	 * @param runModel	Laufzeit-Modell
	 * @param runThreads	Anzahl an verwendeteten Simulations-Threads
	 * @param simDays	Anzahl an simulierten Tagen
	 */
	public Statistics(final CallcenterModel editModel, final CallcenterRunModel runModel, final int runThreads, final long simDays) {
		if (editModel!=null) {
			this.editModel=editModel.clone();
			this.editModel.version=VersionConst.version;
		} else {
			this.editModel=null;
		}

		addPerformanceIndicator(simulationData=new StatisticsSimulationBaseData(Language.trAll("Statistics.XML.BaseData")));

		if (runModel==null) {
			kundenGlobal=new KundenDaten();
			kundenProTyp=new KundenDaten[0];
			agentenGlobal=new AgentenDaten("",null);
			agentenProCallcenter=new AgentenDaten[0];
			agentenProSkilllevel=new AgentenDaten[0];
		} else {
			kundenGlobal=new KundenDaten();
			kundenProTyp=new KundenDaten[runModel.caller.length];
			for (int i=0;i<kundenProTyp.length;i++) kundenProTyp[i]=new KundenDaten(runModel.caller[i].name);

			String s;

			agentenGlobal=new AgentenDaten("",runModel.caller);
			agentenProCallcenter=new AgentenDaten[runModel.callcenter.length];
			s=Language.trPrimary("XML.Statistic.CallCenter");
			for (int i=0;i<agentenProCallcenter.length;i++) agentenProCallcenter[i]=new AgentenDaten(s,runModel.callcenter[i].name,runModel.caller);
			agentenProSkilllevel=new AgentenDaten[runModel.skills.length];
			s=Language.trPrimary("XML.Statistic.SkillLevel");
			for (int i=0;i<agentenProSkilllevel.length;i++) agentenProSkilllevel[i]=new AgentenDaten(s,runModel.skills[i].name,runModel.caller);
		}

		calcModelAgents();

		resetData();
		simulationData.runThreads=runThreads;
		simulationData.runRepeatCount=simDays;
	}

	/**
	 * Wurzel-Element für Statistik-xml-Dateien
	 */
	@Override
	public String[] getRootNodeNames() {
		return XMLBaseElement;
	}

	/**
	 * Wird aufgerufen, um die Agentenanzahlen laut Modell zu berechnen.<br>
	 * Muss aufgerufen werden, wenn ein neues Editor-Modell zugewiesen wurde.
	 * @see #editModel
	 */
	public void calcModelAgents() {
		if (editModel==null) {
			agentenModellProGruppe=new AgentModelData[]{new AgentModelData()};
			agentenModellGlobal=new AgentModelData();
		} else {
			agentenModellProGruppe=buildAgentModelData(editModel);
			agentenModellGlobal=new AgentModelData(agentenModellProGruppe);
		}
	}

	@Override
	public void addData(final StatisticsBase moreStatistics) {
		if (moreStatistics==null) return;
		super.addData(moreStatistics);
		if (moreStatistics instanceof Statistics) {
			final Statistics data=(Statistics)moreStatistics;
			simulationData.runRepeatCount+=data.simulationData.runRepeatCount;
			kundenGlobal.addData(data.kundenGlobal);
			for (int i=0;i<kundenProTyp.length;i++) kundenProTyp[i].addData(data.kundenProTyp[i]);
			agentenGlobal.addData(data.agentenGlobal);
			for (int i=0;i<agentenProCallcenter.length;i++) agentenProCallcenter[i].addData(data.agentenProCallcenter[i]);
			for (int i=0;i<agentenProSkilllevel.length;i++) agentenProSkilllevel[i].addData(data.agentenProSkilllevel[i]);
			meanQueueLength+=data.meanQueueLength;
			maxQueueLength=Math.max(maxQueueLength,data.maxQueueLength);
			addDensity(meanQueueLengthProIntervall,data.meanQueueLengthProIntervall);
			warnings=null;
		}
	}

	/**
	 * Die mittlere Warteschlangenlänge wird von Tag zu Tag aufsummiert, muss also am Ende durch simDays geteilt
	 * werden. Erfolgt dies Thread-weise, so kann bei verschieden vielen Tagen pro Thread ein verzerrtes Ergebnis
	 * entstehen. Daher erfolgt die Division durch simDays erst nach dem zusammenfügen der Teilstatistiken.
	 */
	public void finalQueueLengthCalc() {
		meanQueueLength/=simulationData.runRepeatCount;
		final double[] density=meanQueueLengthProIntervall.densityData;
		for (int i=0;i<48;i++) density[i]/=simulationData.runRepeatCount;
	}

	/**
	 * Berechnet die Agenten-Statistik am Ende der Simulation für eine Verteilung innerhalb einer Agentengruppe.
	 * @param dist	Verteilung innerhalb der Agentengruppe
	 * @see #finalAgentTimesCalcSingle(AgentenDaten)
	 */
	private void finalAgentTimesCalcSingleDistribution(final DataDistributionImpl dist) {
		final double[] data=dist.densityData;
		for (int i=0;i<data.length;i++) data[i]/=1000;
	}

	/**
	 * Berechnet die Agenten-Statistik am Ende der Simulation für eine Agentengruppe.
	 * @param agent	Agentengruppe
	 * @see #finalAgentTimesCalc()
	 */
	private void finalAgentTimesCalcSingle(final AgentenDaten agent) {
		agent.leerlaufGesamt/=1000;
		agent.technischerLeerlaufGesamt/=1000;
		agent.arbeitGesamt/=1000;
		agent.postProcessingGesamt/=1000;
		finalAgentTimesCalcSingleDistribution(agent.leerlaufProIntervall);
		finalAgentTimesCalcSingleDistribution(agent.technischerLeerlaufProIntervall);
		finalAgentTimesCalcSingleDistribution(agent.arbeitProIntervall);
		finalAgentTimesCalcSingleDistribution(agent.postProcessingProIntervall);

		for (int i=0;i<agent.dataByCaller.length;i++) {
			agent.dataByCallerTechnial[i]/=1000;
			finalAgentTimesCalcSingleDistribution(agent.dataByCallerTechnialProIntervall[i]);
			agent.dataByCallerService[i]/=1000;
			finalAgentTimesCalcSingleDistribution(agent.dataByCallerServiceProIntervall[i]);
			agent.dataByCallerPostProcessing[i]/=1000;
			finalAgentTimesCalcSingleDistribution(agent.dataByCallerPostProcessingProIntervall[i]);
		}
	}

	/**
	 * Berechnet die Agenten-Statistik am Ende der Simulation.
	 */
	public void finalAgentTimesCalc() {
		finalAgentTimesCalcSingle(agentenGlobal);
		for (int i=0;i<agentenProCallcenter.length;i++) finalAgentTimesCalcSingle(agentenProCallcenter[i]);
		for (int i=0;i<agentenProSkilllevel.length;i++) finalAgentTimesCalcSingle(agentenProSkilllevel[i]);
	}

	/**
	 * Berechnet die durch die Anrufer entstandenen Kosten am Ende der Simulation
	 */
	public void calcCallerCosts() {
		double d1=0,d2=0,d3=0;
		for (int i=0;i<kundenProTyp.length;i++) {
			KundenDaten d=kundenProTyp[i];
			d1+=d.revenue=d.kundenErfolg*editModel.caller.get(i).revenuePerClient;
			d2+=d.costWaiting=d.anrufeWartezeitSum*editModel.caller.get(i).costPerWaitingSec;
			d3+=d.costCancel=d.anrufeAbbruch*editModel.caller.get(i).costPerCancel;
		}
		kundenGlobal.revenue=d1;
		kundenGlobal.costWaiting=d2;
		kundenGlobal.costCancel=d3;
	}

	/**
	 * Liefert die für die Berechnung von Schwellenwert-Warnungen relevanten Kundengruppen.
	 * @param record	Schwellenwert-Warnungsdatensatz
	 * @return	Kundengruppen
	 * @see #calcWarningValue(ui.model.CallcenterModelWarnings.WarningRecord)
	 */
	private KundenDaten[] getWarningClientGroup(final CallcenterModelWarnings.WarningRecord record) {
		final List<KundenDaten> list=new ArrayList<>();

		if (record.modeGroup==CallcenterModelWarnings.WarningMode.WARNING_MODE_AVERAGE) {
			list.add(kundenGlobal);
		}

		if (record.modeGroup==CallcenterModelWarnings.WarningMode.WARNING_MODE_EACH) {
			for(KundenDaten group : kundenProTyp) list.add(group);
		}

		if (record.modeGroup==CallcenterModelWarnings.WarningMode.WARNING_MODE_SELECTED) {
			boolean ok=false;
			for (KundenDaten clients : kundenProTyp) if (clients.name.equalsIgnoreCase(record.group)) {list.add(clients); ok=true; break;}
			if (!ok) list.add(kundenGlobal);
		}

		return list.toArray(new KundenDaten[0]);
	}

	/**
	 * Liefert die für die Berechnung von Schwellenwert-Warnungen relevanten Agentengruppen.
	 * @param record	Schwellenwert-Warnungsdatensatz
	 * @return	Agentengruppen
	 * @see #calcWarningValue(ui.model.CallcenterModelWarnings.WarningRecord)
	 */
	private AgentenDaten[] getWarningAgentsGroup(final CallcenterModelWarnings.WarningRecord record) {
		final List<AgentenDaten> list=new ArrayList<>();

		if (record.modeGroup==CallcenterModelWarnings.WarningMode.WARNING_MODE_AVERAGE) {
			list.add(agentenGlobal);
		}

		if (record.modeGroup==CallcenterModelWarnings.WarningMode.WARNING_MODE_EACH) {
			for(AgentenDaten group : agentenProCallcenter) list.add(group);
		}

		if (record.modeGroup==CallcenterModelWarnings.WarningMode.WARNING_MODE_SELECTED) {
			boolean ok=false;
			for (AgentenDaten callcenter : agentenProCallcenter) if (callcenter.name.equalsIgnoreCase(record.group)) {list.add(callcenter); ok=true; break;}
			if (!ok) list.add(agentenGlobal);
		}

		return list.toArray(new AgentenDaten[0]);
	}

	/**
	 * Berechnet den Mittelwert zum Vergleich mit dem Schwellenwert
	 * @param type	 Typ der Schwellenwert-Überschreitungs-Warnung
	 * @param groupsKunden	Kundengruppen
	 * @param groupsAgenten	Agentengruppen
	 * @return	Mittelwert
	 * @see #calcWarningValue(ui.model.CallcenterModelWarnings.WarningRecord)
	 */
	private double calcWarningValueAverage(final CallcenterModelWarnings.WarningType type, final KundenDaten[] groupsKunden, final AgentenDaten[] groupsAgenten) {
		double d;

		if (type==CallcenterModelWarnings.WarningType.WARNING_TYPE_WAITINGTIME_CALL || type==CallcenterModelWarnings.WarningType.WARNING_TYPE_WAITINGTIME_CLIENT || type==CallcenterModelWarnings.WarningType.WARNING_TYPE_RESIDENCETIME_CALL || type==CallcenterModelWarnings.WarningType.WARNING_TYPE_RESIDENCETIME_CLIENT) {
			d=0;
		} else {
			d=1;
		}

		if (type==CallcenterModelWarnings.WarningType.WARNING_TYPE_WORKLOAD) {
			for (AgentenDaten agents : groupsAgenten) d=Math.min(d,1-((double)agents.leerlaufGesamt)/Math.max(1,agents.leerlaufGesamt+agents.technischerLeerlaufGesamt+agents.arbeitGesamt+agents.postProcessingGesamt));
		} else {
			for (KundenDaten clients : groupsKunden) switch (type) {
			case WARNING_TYPE_WAITINGTIME_CALL:
				d=Math.max(d,((double)clients.anrufeWartezeitSum)/Math.max(1,clients.anrufeErfolg));
				break;
			case WARNING_TYPE_WAITINGTIME_CLIENT:
				d=Math.max(d,((double)clients.kundenWartezeitSum)/Math.max(1,clients.kundenErfolg));
				break;
			case WARNING_TYPE_RESIDENCETIME_CALL:
				d=Math.max(d,((double)clients.anrufeVerweilzeitSum)/Math.max(1,clients.anrufeErfolg));
				break;
			case WARNING_TYPE_RESIDENCETIME_CLIENT:
				d=Math.max(d,((double)clients.kundenVerweilzeitSum)/Math.max(1,clients.kundenErfolg));
				break;
			case WARNING_TYPE_SUCCESSPART_CALL:
				d=Math.min(d,((double)clients.anrufeErfolg)/Math.max(1,clients.anrufe-clients.anrufeUebertrag));
				break;
			case WARNING_TYPE_SUCCESSPART_CLIENT:
				d=Math.min(d,((double)clients.kundenErfolg)/Math.max(1,clients.kunden-clients.kundenUebertrag));
				break;
			case WARNING_TYPE_SERVICELEVEL_CALL_SUCCESSFUL:
				d=Math.min(d,((double)clients.anrufeServicelevel)/Math.max(1,clients.anrufeErfolg));
				break;
			case WARNING_TYPE_SERVICELEVEL_CLIENTS_SUCCESSFUL:
				d=Math.min(d,((double)clients.kundenServicelevel)/Math.max(1,clients.kundenErfolg));
				break;
			case WARNING_TYPE_SERVICELEVEL_CALL_ALL:
				d=Math.min(d,((double)clients.anrufeServicelevel)/Math.max(1,clients.anrufe));
				break;
			case WARNING_TYPE_SERVICELEVEL_CLIENTS_ALL:
				d=Math.min(d,((double)clients.kundenServicelevel)/Math.max(1,clients.kunden));
				break;
			case WARNING_TYPE_WORKLOAD:
				/* Tritt nicht auf, haben wir oben schon per "if" abgefangen. */
				break;
			}
		}

		return d;
	}

	/**
	 * Berechnet den Wert zum Vergleich mit dem Schwellenwert
	 * @param type	 Typ der Schwellenwert-Überschreitungs-Warnung
	 * @param groupsKunden	Kundengruppen
	 * @param groupsAgenten	Agentengruppen
	 * @param intervals	Zu berücksichtigende Intervalle
	 * @return	Wert
	 * @see #calcWarningValue(ui.model.CallcenterModelWarnings.WarningRecord)
	 */
	private double calcWarningValueIntervals(final CallcenterModelWarnings.WarningType type, final KundenDaten[] groupsKunden, final AgentenDaten[] groupsAgenten, final DataDistributionImpl intervals) {
		double d;

		if (type==CallcenterModelWarnings.WarningType.WARNING_TYPE_WAITINGTIME_CALL || type==CallcenterModelWarnings.WarningType.WARNING_TYPE_WAITINGTIME_CLIENT || type==CallcenterModelWarnings.WarningType.WARNING_TYPE_RESIDENCETIME_CALL || type==CallcenterModelWarnings.WarningType.WARNING_TYPE_RESIDENCETIME_CLIENT) {
			d=0;
		} else {
			d=1;
		}

		for (int i=0;i<intervals.densityData.length;i++) if (intervals.densityData[i]>0.1) {
			if (type==CallcenterModelWarnings.WarningType.WARNING_TYPE_WORKLOAD) {
				for (AgentenDaten agents : groupsAgenten) d=Math.min(d,1-agents.leerlaufProIntervall.densityData[i]/Math.max(1,agents.leerlaufProIntervall.densityData[i]+agents.technischerLeerlaufProIntervall.densityData[i]+agents.arbeitProIntervall.densityData[i]+agents.postProcessingProIntervall.densityData[i]));
			} else {
				for (KundenDaten clients : groupsKunden) switch (type) {
				case WARNING_TYPE_WAITINGTIME_CALL:
					d=Math.max(d,(clients.anrufeWartezeitSumProIntervall.densityData[i])/Math.max(1,clients.anrufeErfolgProIntervall.densityData[i]));
					break;
				case WARNING_TYPE_WAITINGTIME_CLIENT:
					d=Math.max(d,(clients.kundenWartezeitSumProIntervall.densityData[i])/Math.max(1,clients.kundenErfolgProIntervall.densityData[i]));
					break;
				case WARNING_TYPE_RESIDENCETIME_CALL:
					d=Math.max(d,(clients.anrufeVerweilzeitSumProIntervall.densityData[i])/Math.max(1,clients.anrufeErfolgProIntervall.densityData[i]));
					break;
				case WARNING_TYPE_RESIDENCETIME_CLIENT:
					d=Math.max(d,(clients.kundenVerweilzeitSumProIntervall.densityData[i])/Math.max(1,clients.kundenErfolgProIntervall.densityData[i]));
					break;
				case WARNING_TYPE_SUCCESSPART_CALL:
					d=Math.min(d,(clients.anrufeErfolgProIntervall.densityData[i])/Math.max(1,clients.anrufeProIntervall.densityData[i]));
					break;
				case WARNING_TYPE_SUCCESSPART_CLIENT:
					d=Math.min(d,(clients.kundenErfolgProIntervall.densityData[i])/Math.max(1,clients.kundenProIntervall.densityData[i]));
					break;
				case WARNING_TYPE_SERVICELEVEL_CALL_SUCCESSFUL:
					d=Math.min(d,(clients.anrufeServicelevelProIntervall.densityData[i])/Math.max(1,clients.anrufeErfolgProIntervall.densityData[i]));
					break;
				case WARNING_TYPE_SERVICELEVEL_CLIENTS_SUCCESSFUL:
					d=Math.min(d,(clients.kundenServicelevelProIntervall.densityData[i])/Math.max(1,clients.kundenErfolgProIntervall.densityData[i]));
					break;
				case WARNING_TYPE_SERVICELEVEL_CALL_ALL:
					d=Math.min(d,(clients.anrufeServicelevelProIntervall.densityData[i])/Math.max(1,clients.anrufeProIntervall.densityData[i]));
					break;
				case WARNING_TYPE_SERVICELEVEL_CLIENTS_ALL:
					d=Math.min(d,(clients.kundenServicelevelProIntervall.densityData[i])/Math.max(1,clients.kundenProIntervall.densityData[i]));
					break;
				case WARNING_TYPE_WORKLOAD:
					/* Tritt nicht auf, haben wir oben schon per "if" abgefangen. */
					break;
				}
			}
		}

		return d;
	}

	/**
	 * Berechnet den Wert zum Vergleich mit dem Schwellenwert
	 * @param record	Typ der Schwellenwert-Überschreitungs-Warnung
	 * @return	Wert
	 */
	private double calcWarningValue(final CallcenterModelWarnings.WarningRecord record) {
		/* Gruppen auswählen */
		KundenDaten[] groupsKunden=null;
		AgentenDaten[] groupsAgenten=null;
		if (record.type==CallcenterModelWarnings.WarningType.WARNING_TYPE_WORKLOAD) {
			groupsAgenten=getWarningAgentsGroup(record);
		} else {
			groupsKunden=getWarningClientGroup(record);
		}

		/* Intervalle auswählen */
		if (record.modeTime==CallcenterModelWarnings.WarningMode.WARNING_MODE_AVERAGE) {
			return calcWarningValueAverage(record.type,groupsKunden,groupsAgenten);
		} else {
			DataDistributionImpl dist;
			if (record.modeTime==CallcenterModelWarnings.WarningMode.WARNING_MODE_EACH) {
				dist=new DataDistributionImpl(CallcenterModelCaller.freshCallsDistMaxX,48);
				dist.setToValue(1);
			} else {
				dist=record.intervals.clone();
			}
			return calcWarningValueIntervals(record.type,groupsKunden,groupsAgenten,dist);
		}
	}

	/**
	 * Prüft, ob die Warnungsschwellen eines Warnungs-Datensatzes überschritten
	 * sind und trägt dies wenn ja in den Datensatz ein.
	 * @param record	Warnungs-Datensatz
	 */
	private void checkWarning(final CallcenterModelWarnings.WarningRecord record) {
		record.value=calcWarningValue(record);
		record.warningStatus=CallcenterModelWarnings.WarningStatus.WARNING_STATUS_OK;
		switch (record.type) {
		case WARNING_TYPE_WAITINGTIME_CALL:
		case WARNING_TYPE_WAITINGTIME_CLIENT:
		case WARNING_TYPE_RESIDENCETIME_CALL:
		case WARNING_TYPE_RESIDENCETIME_CLIENT:
			if (record.warningYellow>=0 && record.value>record.warningYellow) record.warningStatus=CallcenterModelWarnings.WarningStatus.WARNING_STATUS_YELLOW;
			if (record.warningRed>=0 && record.value>record.warningRed) record.warningStatus=CallcenterModelWarnings.WarningStatus.WARNING_STATUS_RED;
			break;
		case WARNING_TYPE_SUCCESSPART_CALL:
		case WARNING_TYPE_SUCCESSPART_CLIENT:
		case WARNING_TYPE_SERVICELEVEL_CALL_ALL:
		case WARNING_TYPE_SERVICELEVEL_CALL_SUCCESSFUL:
		case WARNING_TYPE_SERVICELEVEL_CLIENTS_ALL:
		case WARNING_TYPE_SERVICELEVEL_CLIENTS_SUCCESSFUL:
		case WARNING_TYPE_WORKLOAD:
			if (record.warningYellow>=0 && record.value<record.warningYellow) record.warningStatus=CallcenterModelWarnings.WarningStatus.WARNING_STATUS_YELLOW;
			if (record.warningRed>=0 && record.value<record.warningRed) record.warningStatus=CallcenterModelWarnings.WarningStatus.WARNING_STATUS_RED;
			break;
		}
	}

	/**
	 * Prüft, ob die Schwellenwerte eingehalten wurden.
	 */
	public void calcWarnings() {
		warnings=new CallcenterModelWarnings(true);
		for (CallcenterModelWarnings.WarningRecord record : editModel.warnings.records) {
			CallcenterModelWarnings.WarningRecord newRecord=record.clone();
			checkWarning(newRecord);
			warnings.records.add(newRecord);
		}
	}

	@Override
	protected String loadProperty(final String name, final String text, final Element node) {
		if (Language.trAll("XML.Model.BaseElement",name)) {
			editModel=new CallcenterModel();
			return editModel.loadFromXML(node);
		}

		if (Language.trAll("XML.Statistic.Info.RunDate",name)) {
			String t=text;
			if (t!=null && !t.isEmpty()) simulationData.runDate=t;
			return null;
		}
		if (Language.trAll("XML.Statistic.Info.User",name)) {
			String t=text;
			if (t!=null && !t.isEmpty()) simulationData.runUser=t;
			return null;
		}
		if (Language.trAll("XML.Statistic.Info.ServerOS",name)) {
			String t=text;
			if (t!=null && !t.isEmpty()) simulationData.runOS=t;
			return null;
		}
		if (Language.trAll("XML.Statistic.Info.RunTime",name)) {
			Integer J=NumberTools.getNotNegativeInteger(text);
			if (J==null) return String.format(Language.tr("XML.Statistic.Info.RunTime.Error"),text);
			simulationData.runTime=J;
			return null;
		}
		if (Language.trAll("XML.Statistic.Info.Threads",name)) {
			Integer J=NumberTools.getNotNegativeInteger(text);
			if (J==null) return String.format(Language.tr("XML.Statistic.Info.Threads.Error"),text);
			simulationData.runThreads=J;
			return null;
		}
		if (Language.trAll("XML.Statistic.Info.SimulatedDays",name)) {
			Integer J=NumberTools.getNotNegativeInteger(text);
			if (J==null) return String.format(Language.tr("XML.Statistic.Info.SimulatedDays.Error"),text);
			simulationData.runRepeatCount=J;
			return null;
		}
		if (Language.trAll("XML.Statistic.Info.SimulatedEvents",name)) {
			Long J=NumberTools.getNotNegativeLong(text);
			if (J==null) return String.format(Language.tr("XML.Statistic.Info.SimulatedEvents.Error"),text);
			simulationData.runEvents=J;
			return null;
		}

		if (Language.trAll("XML.Statistic.Clients",name)) {
			String t=Language.trAllAttribute("XML.Statistic.GeneralAttributes.Name",node);
			String u;
			if (t.isEmpty()) u=kundenGlobal.loadFromXML(node); else {
				KundenDaten k=new KundenDaten(); u=k.loadFromXML(node);
				if (u==null) {
					KundenDaten[] old=kundenProTyp;
					kundenProTyp=new KundenDaten[kundenProTyp.length+1];
					System.arraycopy(old,0,kundenProTyp,0,old.length);
					kundenProTyp[old.length]=k;
				}
			}
			if (u!=null) return u;
			return null;
		}

		if (Language.trAll("XML.Statistic.Agents",name)) {
			String t=Language.trAllAttribute("XML.Statistic.GeneralAttributes.Name",node);
			String u=null;
			if (t.isEmpty()) u=agentenGlobal.loadFromXML(node); else {
				AgentenDaten a=new AgentenDaten("",null); u=a.loadFromXML(node);
				if (u==null) {
					if (Language.trAll("XML.Statistic.CallCenter",a.type)) {
						AgentenDaten[] old=agentenProCallcenter;
						agentenProCallcenter=new AgentenDaten[agentenProCallcenter.length+1];
						System.arraycopy(old,0,agentenProCallcenter,0,old.length);
						agentenProCallcenter[old.length]=a;
					} else {
						AgentenDaten[] old=agentenProSkilllevel;
						agentenProSkilllevel=new AgentenDaten[agentenProSkilllevel.length+1];
						System.arraycopy(old,0,agentenProSkilllevel,0,old.length);
						agentenProSkilllevel[old.length]=a;
					}
				}
			}
			if (u!=null) return u;
			return null;
		}

		if (Language.trAll("XML.Statistic.Queue",name)) {
			String t=Language.trAllAttribute("XML.Statistic.Queue.Average",node);
			double J=Double.parseDouble(t);
			if (J<0) return String.format(Language.tr("XML.Statistic.Queue.Average.Error"),t);
			meanQueueLength=J;
			t=Language.trAllAttribute("XML.Statistic.Queue.AveragePerInterval",node);
			meanQueueLengthProIntervall=DataDistributionImpl.createFromString(t,meanQueueLengthProIntervall.upperBound);
			if (meanQueueLengthProIntervall==null) return Language.tr("XML.Statistic.Queue.AveragePerInterval.Error");
			t=Language.trAllAttribute("XML.Statistic.Queue.Maximum",node);
			Integer K=NumberTools.getNotNegativeInteger(t);
			if (K==null) return String.format(Language.tr("XML.Statistic.Queue.Maximum.Error"),t);
			maxQueueLength=K;
			return null;
		}

		if (Language.trAll("XML.Statistic.ModelAgents",name)) {
			AgentModelData agenten=new AgentModelData();
			String t=agenten.loadFromXML(node);
			if (t!=null) return t;
			if (agenten.name.isEmpty()) agentenModellGlobal=agenten; else {
				final List<AgentModelData> agentenModellProGruppeList=(agentenModellProGruppe==null)?new ArrayList<>():new ArrayList<>(Arrays.asList(agentenModellProGruppe));
				if (agentenModellProGruppeList.size()==1 && agentenModellProGruppeList.get(0).name.isEmpty()) agentenModellProGruppeList.remove(0);
				agentenModellProGruppeList.add(agenten);
				agentenModellProGruppe=agentenModellProGruppeList.toArray(new AgentModelData[0]);
			}
		}

		if (Language.trAll("XML.Statistic.Warnings",name)) {
			warnings=new CallcenterModelWarnings(true);
			String t=warnings.loadFromXML(node);
			if (t!=null) return t;
			return null;
		}

		return null;
	}

	/**
	 * Speichert die Warteschlangenlängen-Statistik-Zusammenfassung
	 * @param parent	Übergeordnetes XML-Element
	 * @param intFrom	Erstes zu berücksichtigendes Intervall (0..47)
	 * @param intTo	Letztes zu berücksichtigendes Intervall (0..47)
	 */
	private void saveQueueSummary(final Element parent, int intFrom, int intTo) {
		final Document doc=parent.getOwnerDocument();
		Element node;

		parent.appendChild(node=doc.createElement(Language.trPrimary("XML.Statistic.Queue.Summary")));

		if (intFrom<0 || intFrom>47) intFrom=0;
		if (intTo<0 || intTo>47) intTo=47;

		node.setAttribute(Language.trPrimary("XML.Statistic.Queue.Summary.Range"),TimeTools.formatTime(intFrom*1800)+"-"+TimeTools.formatTime((intTo+1)*1800));

		double sum=0;
		for (int i=intFrom;i<=intTo;i++) sum+=meanQueueLengthProIntervall.densityData[i];

		node.setAttribute(Language.trPrimary("XML.Statistic.Queue.Average"),NumberTools.formatSystemNumber(sum/(intTo-intFrom+1),12));
	}


	/**
	 * Speichert die Erlang-Vergleichs-Daten
	 * @param node	XML-Element in dem die Daten als Attribute gespeichert werden sollen
	 * @param erlangCData	Erlang-Vergleichsdaten
	 * @param isWithRetry	Sind Wiederholer in dem Modell enthalten?
	 */
	private void saveErlangCData(final Element node, final StatisticViewerErlangCTools erlangCData, final boolean isWithRetry) {
		DataDistributionImpl dist;

		dist=new DataDistributionImpl(86400,erlangCData.getAgents());
		node.setAttribute(Language.trPrimary("XML.Statistic.ErlangC.Agents"),dist.storeToString());

		dist=new DataDistributionImpl(86400,erlangCData.getFreshCalls());
		node.setAttribute(Language.trPrimary("XML.Statistic.ErlangC.FreshCalls"),dist.storeToString());

		if (isWithRetry) {
			dist=new DataDistributionImpl(86400,erlangCData.getRetryCalls());
			node.setAttribute(Language.trPrimary("XML.Statistic.ErlangC.RetryCalls"),dist.storeToString());
		}

		dist=new DataDistributionImpl(86400,erlangCData.getSuccessProbability());
		node.setAttribute(Language.trPrimary("XML.Statistic.ErlangC.Success"),dist.storeToString());

		dist=new DataDistributionImpl(86400,erlangCData.getMeanWaitingTime());
		node.setAttribute(Language.trPrimary("XML.Statistic.ErlangC.WaitingTime"),dist.storeToString());

		dist=new DataDistributionImpl(86400,erlangCData.getServiceLevel());
		node.setAttribute(Language.trPrimary("XML.Statistic.ErlangC.ServiceLevel"),dist.storeToString());
	}

	@Override
	protected void addDataToXML(final Document doc, final Element node, final boolean isPartOfOtherFile, final File file) {
		editModel.saveToXML(node,true);

		Element e;

		if (!simulationData.runDate.isEmpty()) {node.appendChild(e=doc.createElement(Language.trPrimary("XML.Statistic.Info.RunDate"))); e.setTextContent(simulationData.runDate);}
		if (!simulationData.runUser.isEmpty()) {node.appendChild(e=doc.createElement(Language.trPrimary("XML.Statistic.Info.User"))); e.setTextContent(simulationData.runUser);}
		if (!simulationData.runOS.isEmpty()) {node.appendChild(e=doc.createElement(Language.trPrimary("XML.Statistic.Info.ServerOS"))); e.setTextContent(simulationData.runOS);}
		node.appendChild(e=doc.createElement(Language.trPrimary("XML.Statistic.Info.RunTime"))); e.setTextContent(""+simulationData.runTime);
		node.appendChild(e=doc.createElement(Language.trPrimary("XML.Statistic.Info.Threads"))); e.setTextContent(""+simulationData.runThreads);
		node.appendChild(e=doc.createElement(Language.trPrimary("XML.Statistic.Info.SimulatedDays"))); e.setTextContent(""+simulationData.runRepeatCount);
		node.appendChild(e=doc.createElement(Language.trPrimary("XML.Statistic.Info.SimulatedEvents"))); e.setTextContent(""+simulationData.runEvents);

		kundenGlobal.saveToXML(node);
		for (int i=0;i<kundenProTyp.length;i++) kundenProTyp[i].saveToXML(node);

		agentenGlobal.saveToXML(node);
		for (int i=0;i<agentenProCallcenter.length;i++) agentenProCallcenter[i].saveToXML(node);
		for (int i=0;i<agentenProSkilllevel.length;i++) agentenProSkilllevel[i].saveToXML(node);

		node.appendChild(e=doc.createElement(Language.trPrimary("XML.Statistic.RevenueSummary")));
		e.setTextContent(NumberTools.formatSystemNumber((kundenGlobal.revenue-(kundenGlobal.costCancel+kundenGlobal.costWaiting+agentenGlobal.costOfficeTime+agentenGlobal.costCalls+agentenGlobal.costProcessTime))/simulationData.runRepeatCount));

		node.appendChild(e=doc.createElement(Language.trPrimary("XML.Statistic.Queue")));
		e.setAttribute(Language.trPrimary("XML.Statistic.Queue.Average"),NumberTools.formatSystemNumber(meanQueueLength));
		e.setAttribute(Language.trPrimary("XML.Statistic.Queue.AveragePerInterval"),meanQueueLengthProIntervall.storeToString());
		e.setAttribute(Language.trPrimary("XML.Statistic.Queue.Maximum"),""+maxQueueLength);

		saveQueueSummary(node,-1,-1); /* ganzer Tag */
		saveQueueSummary(node,2,45); /* 01..11 */
		saveQueueSummary(node,4,43); /* 02..10 */
		saveQueueSummary(node,0,23); /* 00..12 */
		saveQueueSummary(node,24,47); /* 12..24 */
		saveQueueSummary(node,12,35); /* 06..18 */
		saveQueueSummary(node,0,11); /* 00..06 */
		saveQueueSummary(node,12,23); /* 06..12 */
		saveQueueSummary(node,24,35); /* 12..18 */
		saveQueueSummary(node,36,47); /* 18..24 */

		for (int i=0;i<agentenModellProGruppe.length;i++) agentenModellProGruppe[i].saveDataToXML(node);
		agentenModellGlobal.saveDataToXML(node);

		StatisticViewerErlangCTools erlangCData;
		erlangCData=new StatisticViewerErlangCTools(editModel,false);
		node.appendChild(e=doc.createElement(Language.trPrimary("XML.Statistic.ErlangC.ModeSimple")));
		saveErlangCData(e,erlangCData,false);
		erlangCData=new StatisticViewerErlangCTools(editModel,true);
		node.appendChild(e=doc.createElement(Language.trPrimary("XML.Statistic.ErlangC.ModeComplex")));
		saveErlangCData(e,erlangCData,true);

		if (warnings!=null) warnings.saveToXML(node);
	}

	/**
	 * Addiert die Diche-Daten einer Verteilung zu einer anderen
	 * @param sum	Verteilungs-Objekt zu dem die zweite Dichte addiert werden soll (d.h. die Daten dieses Objektes werden verändert)
	 * @param add	Zweite Verteilungs-Objekt dessen Dichte-Daten bei der ersten Verteilung aufaddiert werden sollen
	 */
	private static void addDensity(final DataDistributionImpl sum, final DataDistributionImpl add) {
		final double[] s=sum.densityData;
		final double[] a=add.densityData;

		for (int i=0;i<s.length;i++) s[i]+=a[i];
	}

	/*
	private static void addDensity(final DataDistributionImpl sum, final double[] add) {
		final double[] s=sum.densityData;

		for (int i=0;i<s.length;i++) s[i]+=add[i];
	}

	private static void maxDensity(final DataDistributionImpl sum, final DataDistributionImpl add) {
		final double[] s=sum.densityData;
		final double[] a=add.densityData;
		for (int i=0;i<s.length;i++) s[i]=Math.max(s[i],a[i]);
	}
	 */

	/**
	 * Hält die Statistikdaten für eine Kundengruppe vor.
	 * @author Alexander Herzog
	 * @see Statistics#kundenGlobal
	 * @see Statistics#kundenProTyp
	 */
	public final class KundenDaten {
		/** Maximalwert der Werteverteilung (z.B. Wartezeiten) im Normalfall */
		public static final int DistMax=1800;
		/** Maximalwert der Werteverteilung (z.B. Wartezeiten) für die Langzeiterfassung */
		public static final int DistMaxLong=3240000;

		/** Name der Kundengruppe */
		public String name;

		/** Anzahl der Kunden in dieser Gruppe */
		public int kunden;
		/** Anzahl der erfolgreichen Kunden in dieser Gruppe */
		public int kundenErfolg;
		/** Anzahl der blockierten Kunden in dieser Gruppe */
		public int kundenBlocked;
		/** Anzahl der Abbrecher in dieser Gruppe */
		public int kundenAbbruch;
		/** Anzahl der Übertrag-Kunden in dieser Gruppe */
		public int kundenUebertrag;
		/** Anzahl der Wiederanrufer in dieser Gruppe */
		public int kundenWiederanruf;
		/** Summe der Wartezeiten der Kunden in dieser Gruppe */
		public long kundenWartezeitSum;
		/** Summe der quadrierten Wartezeiten der Kunden in dieser Gruppe */
		public long kundenWartezeitSum2;
		/** Summe der Verweilzeiten der Kunden in dieser Gruppe */
		public long kundenVerweilzeitSum;
		/** Summe der quadrierten Verweilzeiten der Kunden in dieser Gruppe */
		public long kundenVerweilzeitSum2;
		/** Summe der Abbruchzeiten der Kunden in dieser Gruppe */
		public long kundenAbbruchzeitSum;
		/** Summe der quadrierten Abbruchzeiten der Kunden in dieser Gruppe */
		public long kundenAbbruchzeitSum2;
		/** Anzahl der Kunden in dieser Gruppe, für die die Service-Level-Vorgabe eingehalten werden konnte */
		public int kundenServicelevel;
		/** Anzahl der Weiterleitungen von Kunden in dieser Gruppe */
		public int kundenWeiterleitungen;
		/** Anzahl von Wiederholungen durch Kunden in dieser Gruppe */
		public int kundenWiederholungen;

		/** Anzahl der Kunden in dieser Gruppe (pro Halbstundenintervall) */
		public DataDistributionImpl kundenProIntervall=new DataDistributionImpl(48,48);
		/** Anzahl der erfolgreichen Kunden in dieser Gruppe (pro Halbstundenintervall) */
		public DataDistributionImpl kundenErfolgProIntervall=new DataDistributionImpl(48,48);
		/** Anzahl der blockierten Kunden in dieser Gruppe (pro Halbstundenintervall) */
		public DataDistributionImpl kundenBlockedProIntervall=new DataDistributionImpl(48,48);
		/** Anzahl der Abbrecher in dieser Gruppe (pro Halbstundenintervall) */
		public DataDistributionImpl kundenAbbruchProIntervall=new DataDistributionImpl(48,48);
		/** Anzahl der Wiederanrufer in dieser Gruppe (pro Halbstundenintervall) */
		public DataDistributionImpl kundenWiederanrufProIntervall=new DataDistributionImpl(48,48);
		/** Summe der Wartezeiten der Kunden in dieser Gruppe (pro Halbstundenintervall) */
		public DataDistributionImpl kundenWartezeitSumProIntervall=new DataDistributionImpl(48,48);
		/** Summe der Verweilzeiten der Kunden in dieser Gruppe (pro Halbstundenintervall) */
		public DataDistributionImpl kundenVerweilzeitSumProIntervall=new DataDistributionImpl(48,48);
		/** Summe der Abbruchzeiten der Kunden in dieser Gruppe (pro Halbstundenintervall) */
		public DataDistributionImpl kundenAbbruchzeitSumProIntervall=new DataDistributionImpl(48,48);
		/** Anzahl der Kunden in dieser Gruppe, für die die Service-Level-Vorgabe eingehalten werden konnte (pro Halbstundenintervall) */
		public DataDistributionImpl kundenServicelevelProIntervall=new DataDistributionImpl(48,48);
		/** Anzahl der Weiterleitungen von Kunden in dieser Gruppe (pro Halbstundenintervall) */
		public DataDistributionImpl kundenWeiterleitungenProIntervall=new DataDistributionImpl(48,48);
		/** Anzahl von Wiederholungen durch Kunden in dieser Gruppe (pro Halbstundenintervall) */
		public DataDistributionImpl kundenWiederholungenProIntervall=new DataDistributionImpl(48,48);

		/** Verteilung der Wartezeiten-Häufigkeiten der Kunden (normale Zeitdauern; auf Sekundenbasis) */
		public DataDistributionImpl kundenWartezeitVerteilung=new DataDistributionImpl(DistMax,DistMax);
		/** Verteilung der Verweilzeiten-Häufigkeiten der Kunden (normale Zeitdauern; auf Sekundenbasis) */
		public DataDistributionImpl kundenVerweilzeitVerteilung=new DataDistributionImpl(DistMax,DistMax);
		/** Verteilung der Abbruchzeiten-Häufigkeiten der Kunden (normale Zeitdauern; auf Sekundenbasis) */
		public DataDistributionImpl kundenAbbruchzeitVerteilung=new DataDistributionImpl(DistMax,DistMax);

		/** Verteilung der Wartezeiten-Häufigkeiten der Kunden (lange Zeitdauern; auf Halbstundenbasis) */
		public DataDistributionImpl kundenWartezeitVerteilungLang=new DataDistributionImpl(DistMaxLong,DistMax);
		/** Verteilung der Verweilzeiten-Häufigkeiten der Kunden (lange Zeitdauern; auf Halbstundenbasis) */
		public DataDistributionImpl kundenVerweilzeitVerteilungLang=new DataDistributionImpl(DistMaxLong,DistMax);
		/** Verteilung der Abbruchzeiten-Häufigkeiten der Kunden (lange Zeitdauern; auf Halbstundenbasis) */
		public DataDistributionImpl kundenAbbruchzeitVerteilungLang=new DataDistributionImpl(DistMaxLong,DistMax);

		/** Anzahl der Anrufe in dieser Gruppe */
		public int anrufe;
		/** Anzahl der erfolgreichen Anrufe in dieser Gruppe */
		public int anrufeErfolg;
		/** Anzahl der blockierten Anrufe in dieser Gruppe */
		public int anrufeBlocked;
		/** Anzahl der abgebrochenen Anrufe in dieser Gruppe */
		public int anrufeAbbruch;
		/** Anzahl der Übertrag-Anrufe in dieser Gruppe */
		public int anrufeUebertrag;
		/** Summe der Wartezeiten der Anrufe in dieser Gruppe */
		public long anrufeWartezeitSum;
		/** Summe der quadrierten Wartezeiten der Anrufe in dieser Gruppe */
		public long anrufeWartezeitSum2;
		/** Summe der Verweilzeiten der Anrufe in dieser Gruppe */
		public long anrufeVerweilzeitSum;
		/** Summe der quadrierten Verweilzeiten der Anrufe in dieser Gruppe */
		public long anrufeVerweilzeitSum2;
		/** Summe der Abbruchzeiten der Anrufe in dieser Gruppe */
		public long anrufeAbbruchzeitSum;
		/** Summe der quadrierten Abbruchzeiten der Anrufe in dieser Gruppe */
		public long anrufeAbbruchzeitSum2;
		/** Anzahl der Anrufe in dieser Gruppe, für die die Service-Level-Vorgabe eingehalten werden konnte */
		public int anrufeServicelevel;
		/** Anzahl der Weiterleitungen von Anrufe in dieser Gruppe */
		public int anrufeWeiterleitungen;
		/** Anzahl von Wiederholungen durch Anrufe in dieser Gruppe */
		public int anrufeWiederholungen;

		/** Anzahl der Anrufe in dieser Gruppe (pro Halbstundenintervall) */
		public DataDistributionImpl anrufeProIntervall=new DataDistributionImpl(48,48);
		/** Anzahl der erfolgreichen Anrufe in dieser Gruppe (pro Halbstundenintervall) */
		public DataDistributionImpl anrufeErfolgProIntervall=new DataDistributionImpl(48,48);
		/** Anzahl der blockierten Anrufe in dieser Gruppe (pro Halbstundenintervall) */
		public DataDistributionImpl anrufeBlockedProIntervall=new DataDistributionImpl(48,48);
		/** Anzahl der abgebrochenen Anrufe in dieser Gruppe (pro Halbstundenintervall) */
		public DataDistributionImpl anrufeAbbruchProIntervall=new DataDistributionImpl(48,48);
		/** Summe der Wartezeiten der Anrufe in dieser Gruppe (pro Halbstundenintervall) */
		public DataDistributionImpl anrufeWartezeitSumProIntervall=new DataDistributionImpl(48,48);
		/** Summe der Verweilzeiten der Anrufe in dieser Gruppe (pro Halbstundenintervall) */
		public DataDistributionImpl anrufeVerweilzeitSumProIntervall=new DataDistributionImpl(48,48);
		/** Summe der Abbruchzeiten der Anrufe in dieser Gruppe (pro Halbstundenintervall) */
		public DataDistributionImpl anrufeAbbruchzeitSumProIntervall=new DataDistributionImpl(48,48);
		/** Anzahl der Anrufe in dieser Gruppe, für die die Service-Level-Vorgabe eingehalten werden konnte (pro Halbstundenintervall) */
		public DataDistributionImpl anrufeServicelevelProIntervall=new DataDistributionImpl(48,48);
		/** Anzahl der Weiterleitungen von Anrufe in dieser Gruppe (pro Halbstundenintervall) */
		public DataDistributionImpl anrufeWeiterleitungenProIntervall=new DataDistributionImpl(48,48);
		/** Anzahl von Wiederholungen durch Anrufe in dieser Gruppe (pro Halbstundenintervall) */
		public DataDistributionImpl anrufeWiederholungenProIntervall=new DataDistributionImpl(48,48);

		/** Verteilung der Wartezeiten-Häufigkeiten der Anrufe (normale Zeitdauern; auf Sekundenbasis) */
		public DataDistributionImpl anrufeWartezeitVerteilung=new DataDistributionImpl(DistMax,DistMax);
		/** Verteilung der Verweilzeiten-Häufigkeiten der Anrufe (normale Zeitdauern; auf Sekundenbasis) */
		public DataDistributionImpl anrufeVerweilzeitVerteilung=new DataDistributionImpl(DistMax,DistMax);
		/** Verteilung der Abbruchzeiten-Häufigkeiten der Anrufe (normale Zeitdauern; auf Sekundenbasis) */
		public DataDistributionImpl anrufeAbbruchzeitVerteilung=new DataDistributionImpl(DistMax,DistMax);

		/** Verteilung der Wartezeiten-Häufigkeiten der Anrufe (lange Zeitdauern; auf Halbstundenbasis) */
		public DataDistributionImpl anrufeWartezeitVerteilungLang=new DataDistributionImpl(DistMaxLong,DistMax);
		/** Verteilung der Verweilzeiten-Häufigkeiten der Anrufe (lange Zeitdauern; auf Halbstundenbasis) */
		public DataDistributionImpl anrufeVerweilzeitVerteilungLang=new DataDistributionImpl(DistMaxLong,DistMax);
		/** Verteilung der Abbruchzeiten-Häufigkeiten der Anrufe (lange Zeitdauern; auf Halbstundenbasis) */
		public DataDistributionImpl anrufeAbbruchzeitVerteilungLang=new DataDistributionImpl(DistMaxLong,DistMax);

		/* Konfidenzintervall-Daten */

		/** Summe über die Mittelwerte der Wartezeiten pro Simulationstag (für die Konfidenzintervall-Berechnung) */
		public double interDayWartezeitSum;
		/** Summe über die quadrierten Mittelwerte der Wartezeiten pro Simulationstag (für die Konfidenzintervall-Berechnung) */
		public double interDayWartezeitSum2;
		/** Summe über die Anzahlen an erfolgreichen Anrufen pro Simulationstag (für die Konfidenzintervall-Berechnung) */
		public double interDaySuccessCallsSum;
		/** Summe über die quadrierten Anzahlen an erfolgreichen Anrufen pro Simulationstag (für die Konfidenzintervall-Berechnung) */
		public double interDaySuccessCallsSum2;
		/** Summe über die Anzahlen an erfolgreichen Kunden pro Simulationstag (für die Konfidenzintervall-Berechnung) */
		public double interDaySuccessClientsSum;
		/** Summe über die quadrierten Anzahlen an erfolgreichen Kunden pro Simulationstag (für die Konfidenzintervall-Berechnung) */
		public double interDaySuccessClientsSum2;
		/** Summe über die Service-Level-Werte auf Anrufbasis (erfolgreiche Anrufe) pro Simulationstag (für die Konfidenzintervall-Berechnung) */
		public double interDayServiceLevelCallsSuccessSum;
		/** Summe über die quadrierten Service-Level-Werte auf Anrufbasis (erfolgreiche Anrufe) pro Simulationstag (für die Konfidenzintervall-Berechnung) */
		public double interDayServiceLevelCallsSuccessSum2;
		/** Summe über die Service-Level-Werte auf Anrufbasis (alle Anrufe) pro Simulationstag (für die Konfidenzintervall-Berechnung) */
		public double interDayServiceLevelCallsAllSum;
		/** Summe über die quadrierten Service-Level-Werte auf Anrufbasis (alle Anrufe) pro Simulationstag (für die Konfidenzintervall-Berechnung) */
		public double interDayServiceLevelCallsAllSum2;
		/** Summe über die Service-Level-Werte auf Kundenbasis (erfolgreiche Kunden) pro Simulationstag (für die Konfidenzintervall-Berechnung) */
		public double interDayServiceLevelClientsSuccessSum;
		/** Summe über die quadrierten Service-Level-Werte auf Kundenbasis (erfolgreiche Kunden) pro Simulationstag (für die Konfidenzintervall-Berechnung) */
		public double interDayServiceLevelClientsSuccessSum2;
		/** Summe über die Service-Level-Werte auf Kundenbasis (alle Kunden) pro Simulationstag (für die Konfidenzintervall-Berechnung) */
		public double interDayServiceLevelClientsAllSum;
		/** Summe über die quadrierten Service-Level-Werte auf Kundenbasis (alle Kunden) pro Simulationstag (für die Konfidenzintervall-Berechnung) */
		public double interDayServiceLevelClientsAllSum2;

		/** Ertrag durch diese Kundengruppe */
		public double revenue;
		/** Wartezeitkosten durch diese Kundengruppe */
		public double costWaiting;
		/** Abbruchkosten durch diese Kundengruppe */
		public double costCancel;

		/** Wird zur Berechnung von kundenAbbruchProSimDay verwendet; wird nicht mitgespeichert oder gecloned, nur während der Simulation zu verwenden */
		public int kundenAbbruchThisDay;

		/** Abbrecher pro Simulationstag (wird für die verkettete Simulation benötigt) */
		public List<Integer> kundenAbbruchProSimDay=new ArrayList<>();

		/** Wird zur Berechnung von kundenNextDayRetryProSimDay verwendet; wird nicht mitgespeichert oder gecloned, nur während der Simulation zu verwenden */
		public List<Long> kundenNextDayRetryThisDay=new ArrayList<>();

		/** Wiederanrufer am nächsten Tag inkl. der Anrufzeitpunkte */
		public List<List<Long>> kundenNextDayRetryProSimDay=new ArrayList<>();

		/** Wird zur Berechnung von kundenNextDayUebertragWaitingTimeProSimDay verwendet; wird nicht mitgespeichert oder gecloned, nur während der Simulation zu verwenden */
		public List<Long> kundenNextDayUebertragWaitingTimeThisDay=new ArrayList<>();

		/** Übertrag zum nächsten Tag (bisherige Wartezeiten der einzelnen Kunden) */
		public List<List<Long>> kundenNextDayUebertragWaitingTimeProSimDay=new ArrayList<>();

		/** Wird zur Berechnung von kundenNextDayUebertragRestWaitingToleranceProSimDay verwendet; wird nicht mitgespeichert oder gecloned, nur während der Simulation zu verwenden */
		public List<Long> kundenNextDayUebertragRestWaitingToleranceThisDay=new ArrayList<>();

		/** Übertrag zum nächsten Tag (Restwartezeiten der einzelnen Kunden) */
		public List<List<Long>> kundenNextDayUebertragRestWaitingToleranceProSimDay=new ArrayList<>();


		/**
		 * Konstruktor der Klasse
		 */
		private KundenDaten() {
			this("");
		}

		/**
		 * Konstruktor der Klasse
		 * @param name	Name der Kundengruppe
		 */
		private KundenDaten(final String name) {
			this.name=name;
		}

		/**
		 * Fügt Daten eines weiteren Kunden-Datensatzes zu diesem Datensatz hinzu.
		 * @param data	Weitere Daten die zu diesem Datensatz hinzugefügt werden sollen
		 */
		private void addData(final KundenDaten data) {
			kunden+=data.kunden;
			kundenErfolg+=data.kundenErfolg;
			kundenBlocked+=data.kundenBlocked;
			kundenAbbruch+=data.kundenAbbruch;
			kundenUebertrag+=data.kundenUebertrag;
			kundenWiederanruf+=data.kundenWiederanruf;
			kundenWartezeitSum+=data.kundenWartezeitSum;
			kundenWartezeitSum2+=data.kundenWartezeitSum2;
			kundenVerweilzeitSum+=data.kundenVerweilzeitSum;
			kundenVerweilzeitSum2+=data.kundenVerweilzeitSum2;
			kundenAbbruchzeitSum+=data.kundenAbbruchzeitSum;
			kundenAbbruchzeitSum2+=data.kundenAbbruchzeitSum2;
			kundenServicelevel+=data.kundenServicelevel;
			kundenWeiterleitungen+=data.kundenWeiterleitungen;
			kundenWiederholungen+=data.kundenWiederholungen;

			addDensity(kundenProIntervall,data.kundenProIntervall);
			addDensity(kundenErfolgProIntervall,data.kundenErfolgProIntervall);
			addDensity(kundenBlockedProIntervall,data.kundenBlockedProIntervall);
			addDensity(kundenAbbruchProIntervall,data.kundenAbbruchProIntervall);
			addDensity(kundenWartezeitSumProIntervall,data.kundenWartezeitSumProIntervall);
			addDensity(kundenVerweilzeitSumProIntervall,data.kundenVerweilzeitSumProIntervall);
			addDensity(kundenAbbruchzeitSumProIntervall,data.kundenAbbruchzeitSumProIntervall);
			addDensity(kundenServicelevelProIntervall,data.kundenServicelevelProIntervall);
			addDensity(kundenWeiterleitungenProIntervall,data.kundenWeiterleitungenProIntervall);
			addDensity(kundenWiederholungenProIntervall,data.kundenWiederholungenProIntervall);
			addDensity(kundenWiederanrufProIntervall,data.kundenWiederanrufProIntervall);

			addDensity(kundenWartezeitVerteilung,data.kundenWartezeitVerteilung);
			addDensity(kundenVerweilzeitVerteilung,data.kundenVerweilzeitVerteilung);
			addDensity(kundenAbbruchzeitVerteilung,data.kundenAbbruchzeitVerteilung);

			addDensity(kundenWartezeitVerteilungLang,data.kundenWartezeitVerteilungLang);
			addDensity(kundenVerweilzeitVerteilungLang,data.kundenVerweilzeitVerteilungLang);
			addDensity(kundenAbbruchzeitVerteilungLang,data.kundenAbbruchzeitVerteilungLang);

			anrufe+=data.anrufe;
			anrufeBlocked+=data.anrufeBlocked;
			anrufeAbbruch+=data.anrufeAbbruch;
			anrufeUebertrag+=data.anrufeUebertrag;
			anrufeErfolg+=data.anrufeErfolg;
			anrufeWartezeitSum+=data.anrufeWartezeitSum;
			anrufeWartezeitSum2+=data.anrufeWartezeitSum2;
			anrufeVerweilzeitSum+=data.anrufeVerweilzeitSum;
			anrufeVerweilzeitSum2+=data.anrufeVerweilzeitSum2;
			anrufeAbbruchzeitSum+=data.anrufeAbbruchzeitSum;
			anrufeAbbruchzeitSum2+=data.anrufeAbbruchzeitSum2;
			anrufeServicelevel+=data.anrufeServicelevel;
			anrufeWeiterleitungen+=data.anrufeWeiterleitungen;
			anrufeWiederholungen+=data.anrufeWiederholungen;

			addDensity(anrufeProIntervall,data.anrufeProIntervall);
			addDensity(anrufeErfolgProIntervall,data.anrufeErfolgProIntervall);
			addDensity(anrufeBlockedProIntervall,data.anrufeBlockedProIntervall);
			addDensity(anrufeAbbruchProIntervall,data.anrufeAbbruchProIntervall);
			addDensity(anrufeWartezeitSumProIntervall,data.anrufeWartezeitSumProIntervall);
			addDensity(anrufeVerweilzeitSumProIntervall,data.anrufeVerweilzeitSumProIntervall);
			addDensity(anrufeAbbruchzeitSumProIntervall,data.anrufeAbbruchzeitSumProIntervall);
			addDensity(anrufeServicelevelProIntervall,data.anrufeServicelevelProIntervall);
			addDensity(anrufeWeiterleitungenProIntervall,data.anrufeWeiterleitungenProIntervall);
			addDensity(anrufeWiederholungenProIntervall,data.anrufeWiederholungenProIntervall);

			addDensity(anrufeWartezeitVerteilung,data.anrufeWartezeitVerteilung);
			addDensity(anrufeVerweilzeitVerteilung,data.anrufeVerweilzeitVerteilung);
			addDensity(anrufeAbbruchzeitVerteilung,data.anrufeAbbruchzeitVerteilung);

			addDensity(anrufeWartezeitVerteilungLang,data.anrufeWartezeitVerteilungLang);
			addDensity(anrufeVerweilzeitVerteilungLang,data.anrufeVerweilzeitVerteilungLang);
			addDensity(anrufeAbbruchzeitVerteilungLang,data.anrufeAbbruchzeitVerteilungLang);

			interDayWartezeitSum+=data.interDayWartezeitSum;
			interDayWartezeitSum2+=data.interDayWartezeitSum2;
			interDaySuccessCallsSum+=data.interDaySuccessCallsSum;
			interDaySuccessCallsSum2+=data.interDaySuccessCallsSum2;
			interDaySuccessClientsSum+=data.interDaySuccessClientsSum;
			interDaySuccessClientsSum2+=data.interDaySuccessClientsSum2;
			interDayServiceLevelCallsSuccessSum+=data.interDayServiceLevelCallsSuccessSum;
			interDayServiceLevelCallsSuccessSum2+=data.interDayServiceLevelCallsSuccessSum2;
			interDayServiceLevelCallsAllSum+=data.interDayServiceLevelCallsAllSum;
			interDayServiceLevelCallsAllSum2+=data.interDayServiceLevelCallsAllSum2;
			interDayServiceLevelClientsSuccessSum+=data.interDayServiceLevelClientsSuccessSum;
			interDayServiceLevelClientsSuccessSum2+=data.interDayServiceLevelClientsSuccessSum2;
			interDayServiceLevelClientsAllSum+=data.interDayServiceLevelClientsAllSum;
			interDayServiceLevelClientsAllSum2+=data.interDayServiceLevelClientsAllSum2;

			revenue+=data.revenue;
			costWaiting+=data.costWaiting;
			costCancel+=data.costCancel;

			kundenAbbruchProSimDay.addAll(data.kundenAbbruchProSimDay);

			kundenNextDayRetryProSimDay.addAll(data.kundenNextDayRetryProSimDay);
			kundenNextDayUebertragWaitingTimeProSimDay.addAll(data.kundenNextDayUebertragWaitingTimeProSimDay);
			kundenNextDayUebertragRestWaitingToleranceProSimDay.addAll(data.kundenNextDayUebertragRestWaitingToleranceProSimDay);
		}

		/**
		 * Wird in {@link #updateInterDayData()} verwendet, um Überträge zwischen
		 * mehreren Tagen verwalten zu können.
		 * @see #updateInterDayData()
		 */
		private int lastDayAnrufe;

		/**
		 * Wird in {@link #updateInterDayData()} verwendet, um Überträge zwischen
		 * mehreren Tagen verwalten zu können.
		 * @see #updateInterDayData()
		 */
		private int lastDayAnrufeErfolg;

		/**
		 * Wird in {@link #updateInterDayData()} verwendet, um Überträge zwischen
		 * mehreren Tagen verwalten zu können.
		 * @see #updateInterDayData()
		 */
		private int lastDayKunden;

		/**
		 * Wird in {@link #updateInterDayData()} verwendet, um Überträge zwischen
		 * mehreren Tagen verwalten zu können.
		 * @see #updateInterDayData()
		 */
		private int lastDayKundenErfolg;

		/**
		 * Wird in {@link #updateInterDayData()} verwendet, um Überträge zwischen
		 * mehreren Tagen verwalten zu können.
		 * @see #updateInterDayData()
		 */
		private long lastDayAnrufeWartezeitSum;

		/**
		 * Wird in {@link #updateInterDayData()} verwendet, um Überträge zwischen
		 * mehreren Tagen verwalten zu können.
		 * @see #updateInterDayData()
		 */
		private int lastDayAnrufeServicelevel;

		/**
		 * Wird in {@link #updateInterDayData()} verwendet, um Überträge zwischen
		 * mehreren Tagen verwalten zu können.
		 * @see #updateInterDayData()
		 */
		private int lastDayKundenServicelevel;

		/**
		 * Aktualisiert die Daten für die Konfidenzinervall-Berechnung nach jeweils einem Simulationstag.
		 */
		public void updateInterDayData() {
			/* Daten für die Konfidenzintervalle */
			double wartezeit=(double)(anrufeWartezeitSum-lastDayAnrufeWartezeitSum)/Math.max(1,anrufeErfolg-lastDayAnrufeErfolg);
			interDayWartezeitSum+=wartezeit;
			interDayWartezeitSum2+=wartezeit*wartezeit;

			double success=(double)(anrufeErfolg-lastDayAnrufeErfolg)/Math.max(1,(anrufe-anrufeUebertrag)-lastDayAnrufe);
			interDaySuccessCallsSum+=success;
			interDaySuccessCallsSum2+=success*success;
			success=(double)(kundenErfolg-lastDayKundenErfolg)/Math.max(1,(kunden-kundenUebertrag)-lastDayKunden);
			interDaySuccessClientsSum+=success;
			interDaySuccessClientsSum2+=success*success;

			double serviceLevel=(double)(anrufeServicelevel-lastDayAnrufeServicelevel)/Math.max(1,anrufeErfolg-lastDayAnrufeErfolg);
			interDayServiceLevelCallsSuccessSum+=serviceLevel;
			interDayServiceLevelCallsSuccessSum2+=serviceLevel*serviceLevel;
			serviceLevel=(double)(anrufeServicelevel-lastDayAnrufeServicelevel)/Math.max(1,anrufe-lastDayAnrufe);
			interDayServiceLevelCallsAllSum+=serviceLevel;
			interDayServiceLevelCallsAllSum2+=serviceLevel*serviceLevel;
			serviceLevel=(double)(kundenServicelevel-lastDayKundenServicelevel)/Math.max(1,kundenErfolg-lastDayKundenErfolg);
			interDayServiceLevelClientsSuccessSum+=serviceLevel;
			interDayServiceLevelClientsSuccessSum2+=serviceLevel*serviceLevel;
			serviceLevel=(double)(kundenServicelevel-lastDayKundenServicelevel)/Math.max(1,kunden-lastDayKunden);
			interDayServiceLevelClientsAllSum+=serviceLevel;
			interDayServiceLevelClientsAllSum2+=serviceLevel*serviceLevel;

			/* Hilfsdaten für die Berechnung der Konfidenzintervalle */
			lastDayAnrufe=anrufe-anrufeUebertrag;
			lastDayAnrufeErfolg=anrufeErfolg;
			lastDayKunden=kunden-kundenUebertrag;
			lastDayKundenErfolg=kundenErfolg;
			lastDayAnrufeWartezeitSum=anrufeWartezeitSum;
			lastDayAnrufeServicelevel=anrufeServicelevel;
			lastDayKundenServicelevel=kundenServicelevel;

			/* Daten pro Tag aufzeichnen */
			kundenAbbruchProSimDay.add(kundenAbbruchThisDay);
			kundenAbbruchThisDay=0;

			kundenNextDayRetryProSimDay.add(kundenNextDayRetryThisDay);
			kundenNextDayRetryThisDay=new ArrayList<>();

			kundenNextDayUebertragWaitingTimeProSimDay.add(kundenNextDayUebertragWaitingTimeThisDay);
			kundenNextDayUebertragWaitingTimeThisDay=new ArrayList<>();

			kundenNextDayUebertragRestWaitingToleranceProSimDay.add(kundenNextDayUebertragRestWaitingToleranceThisDay);
			kundenNextDayUebertragRestWaitingToleranceThisDay=new ArrayList<>();
		}

		/**
		 * Versucht einen Kunden-Datensatz aus dem übergebenen XML-Node zu laden
		 * @param node	XML-Knoten, der die Kunden-Daten enthält
		 * @return	Tritt ein Fehler auf, so wird die Fehlermeldung als String zurückgegeben. Im Erfolgsfall wird <code>null</code> zurückgegeben.
		 */
		private String loadFromXML(final Element node) {
			name=Language.trAllAttribute("XML.Statistic.GeneralAttributes.Name",node);

			NodeList l=node.getChildNodes();
			for (int i=0; i<l.getLength();i++) {
				if (!(l.item(i) instanceof Element)) continue;
				Element e=(Element)l.item(i);
				String s=e.getNodeName();

				if (Language.trAll("XML.Statistic.Clients.Count",s)) {
					Integer K;
					String t;
					t=Language.trAllAttribute("XML.Statistic.Clients.ClientsCallsCount.Count",e);
					K=NumberTools.getNotNegativeInteger(NumberTools.systemNumberToLocalNumber(t));
					if (K!=null) kunden=K; else return String.format(Language.tr("XML.Statistic.Clients.ClientsCallsCount.Count.ErrorClients"),t);
					t=Language.trAllAttribute("XML.Statistic.Clients.ClientsCallsCount.Success",e);
					K=NumberTools.getNotNegativeInteger(NumberTools.systemNumberToLocalNumber(t));
					if (K!=null) kundenErfolg=K; else return String.format(Language.tr("XML.Statistic.Clients.ClientsCallsCount.Success.ErrorClients"),t);
					t=Language.trAllAttribute("XML.Statistic.Clients.ClientsCallsCount.Blocked",e);
					K=NumberTools.getNotNegativeInteger(NumberTools.systemNumberToLocalNumber(t));
					if (K!=null) kundenBlocked=K; else return String.format(Language.tr("XML.Statistic.Clients.ClientsCallsCount.Blocked.ErrorClients"),t);
					t=Language.trAllAttribute("XML.Statistic.Clients.ClientsCallsCount.Canceled",e);
					K=NumberTools.getNotNegativeInteger(NumberTools.systemNumberToLocalNumber(t));
					if (K!=null) kundenAbbruch=K; else return String.format(Language.tr("XML.Statistic.Clients.ClientsCallsCount.Canceled.ErrorClients"),t);

					t=Language.trAllAttribute("XML.Statistic.Clients.ClientsCallsCount.CarryOver",e);
					if (t.isEmpty()) {
						kundenUebertrag=0;
					} else {
						K=NumberTools.getNotNegativeInteger(NumberTools.systemNumberToLocalNumber(t));
						if (K!=null) kundenUebertrag=K; else return String.format(Language.tr("XML.Statistic.Clients.ClientsCallsCount.CarryOver.ErrorClients"),t);
					}
					kundenProIntervall=DataDistributionImpl.createFromString(Language.trAllAttribute("XML.Statistic.Clients.ClientsCallsCount.CountPerInterval",e),kundenProIntervall.upperBound);
					if (kundenProIntervall==null) return Language.tr("XML.Statistic.Clients.ClientsCallsCount.CountPerInterval.ErrorClients");
					kundenProIntervall.stretchToValueCount(48);
					kundenErfolgProIntervall=DataDistributionImpl.createFromString(Language.trAllAttribute("XML.Statistic.Clients.ClientsCallsCount.SuccessPerInterval",e),kundenErfolgProIntervall.upperBound);
					if (kundenErfolgProIntervall==null) return Language.tr("XML.Statistic.Clients.ClientsCallsCount.SuccessPerInterval.ErrorClients");
					kundenErfolgProIntervall.stretchToValueCount(48);
					kundenBlockedProIntervall=DataDistributionImpl.createFromString(Language.trAllAttribute("XML.Statistic.Clients.ClientsCallsCount.BlockedPerInterval",e),kundenBlockedProIntervall.upperBound);
					if (kundenBlockedProIntervall==null) return Language.tr("XML.Statistic.Clients.ClientsCallsCount.BlockedPerInterval.ErrorClients");
					kundenBlockedProIntervall.stretchToValueCount(48);
					kundenAbbruchProIntervall=DataDistributionImpl.createFromString(Language.trAllAttribute("XML.Statistic.Clients.ClientsCallsCount.CanceledPerInterval",e),kundenAbbruchProIntervall.upperBound);
					if (kundenAbbruchProIntervall==null) return Language.tr("XML.Statistic.Clients.ClientsCallsCount.CanceledPerInterval.ErrorClients");
					kundenAbbruchProIntervall.stretchToValueCount(48);

					/* Alte Stelle zum Laden verwenden um Kompatibilität zu wahren */
					t=Language.trAllAttribute("XML.Statistic.Clients.Recall.Clients.Old",e);
					if (!t.isEmpty()) {
						kundenWiederanrufProIntervall=DataDistributionImpl.createFromString(t,kundenWiederanrufProIntervall.upperBound);
						if (kundenWiederanrufProIntervall==null) return Language.tr("XML.Statistic.Clients.Recall.Clients.ErrorSumPerInterval");
						kundenWiederanrufProIntervall.stretchToValueCount(48);
					}

					/* Alte Stelle zum Laden verwenden um Kompatibilität zu wahren */
					t=Language.trAllAttribute("XML.Statistic.Clients.CarryOver.Canceled",e);
					if (t!=null && !t.isEmpty()) {
						DataDistributionImpl abbruchProTag=DataDistributionImpl.createFromString(t,1000);
						if (abbruchProTag==null) return Language.tr("XML.Statistic.Clients.CarryOver.Canceled.Error");
						kundenAbbruchProSimDay.clear();
						for (int j=0;j<abbruchProTag.densityData.length;j++) kundenAbbruchProSimDay.add((int)abbruchProTag.densityData[j]);
					}

					continue;
				}

				if (Language.trAll("XML.Statistic.Clients.CarryOver",s)) {
					NodeList l2=e.getChildNodes();
					for (int j=0; j<l.getLength();j++) {
						if (!(l2.item(j) instanceof Element)) continue;
						Element e2=(Element)l2.item(j);
						String t=e2.getNodeName();

						if (Language.trAll("XML.Statistic.Clients.CarryOver.Canceled",t)) {
							DataDistributionImpl abbruchProTag=DataDistributionImpl.createFromString(e2.getTextContent(),1000);
							if (abbruchProTag==null) return Language.tr("XML.Statistic.Clients.CarryOver.Canceled.Error");
							kundenAbbruchProSimDay.clear();
							for (int k=0;k<abbruchProTag.densityData.length;k++) kundenAbbruchProSimDay.add((int)abbruchProTag.densityData[k]);
							continue;
						}

						if (Language.trAll("XML.Statistic.Clients.CarryOver.RetryTimes",t)) {
							kundenNextDayRetryProSimDay.clear();
							String all=e2.getTextContent();
							String[] data=all.split("\\|");
							for (String part: data) {
								List<Long> list=new ArrayList<>();
								for (String value: part.split(";")) if (!value.isEmpty()) {
									Long L=NumberTools.getLong(value);
									if (L==null) return Language.tr("XML.Statistic.Clients.CarryOver.RetryTimes.Error");
									list.add(L);
								}
								kundenNextDayRetryProSimDay.add(list);
							}
							while (kundenNextDayRetryProSimDay.size()<simulationData.runRepeatCount) kundenNextDayRetryProSimDay.add(new ArrayList<Long>());
							continue;
						}

						if (Language.trAll("XML.Statistic.Clients.CarryOver.WaitingTimeTolerances",t)) {
							kundenNextDayUebertragWaitingTimeProSimDay.clear();
							kundenNextDayUebertragRestWaitingToleranceProSimDay.clear();
							for (String part: e2.getTextContent().split("\\|")) {
								List<Long> list1=new ArrayList<>();
								List<Long> list2=new ArrayList<>();
								for (String value: part.split(";")) if (!value.isEmpty()) {
									String[] lists=value.split("/");
									if (lists.length==2) {
										Long L1=NumberTools.getLong(lists[0]);
										if (L1==null) return Language.tr("XML.Statistic.Clients.CarryOver.WaitingTimeTolerances.Error");
										list1.add(L1);
										Long L2=NumberTools.getLong(lists[1]);
										if (L2==null) return Language.tr("XML.Statistic.Clients.CarryOver.WaitingTimeTolerances.Error");
										list2.add(L2);
									}
								}
								kundenNextDayUebertragWaitingTimeProSimDay.add(list1);
								kundenNextDayUebertragRestWaitingToleranceProSimDay.add(list2);
							}
							while (kundenNextDayUebertragWaitingTimeProSimDay.size()<simulationData.runRepeatCount) {
								kundenNextDayUebertragWaitingTimeProSimDay.add(new ArrayList<Long>());
								kundenNextDayUebertragRestWaitingToleranceProSimDay.add(new ArrayList<Long>());
							}
							continue;
						}
					}
				}

				if (Language.trAll("XML.Statistic.Clients.WaitingTime.Clients",s)) {
					Long K;
					String t;
					t=Language.trAllAttribute("XML.Statistic.GeneralAttributes.Sum",e);
					K=NumberTools.getNotNegativeLong(NumberTools.systemNumberToLocalNumber(t));
					if (K!=null) kundenWartezeitSum=K; else return String.format(Language.tr("XML.Statistic.Clients.WaitingTime.Clients.ErrorSum"),t);
					t=Language.trAllAttribute("XML.Statistic.GeneralAttributes.SquaresSum",e);
					K=NumberTools.getNotNegativeLong(NumberTools.systemNumberToLocalNumber(t));
					if (K!=null) kundenWartezeitSum2=K; else return String.format(Language.tr("XML.Statistic.Clients.WaitingTime.Clients.ErrorSquaresSum"),t);
					t=Language.trAllAttribute("XML.Statistic.GeneralAttributes.SumPerInterval",e);
					kundenWartezeitSumProIntervall=DataDistributionImpl.createFromString(t,kundenWartezeitSumProIntervall.upperBound);
					if (kundenWartezeitSumProIntervall==null) return Language.tr("XML.Statistic.Clients.WaitingTime.Clients.ErrorSumPerInterval");
					kundenWartezeitSumProIntervall.stretchToValueCount(48);
					continue;
				}

				if (Language.trAll("XML.Statistic.Clients.HoldingTime.Clients",s)) {
					Long K;
					String t;
					t=Language.trAllAttribute("XML.Statistic.GeneralAttributes.Sum",e);
					K=NumberTools.getNotNegativeLong(NumberTools.systemNumberToLocalNumber(t));
					if (K!=null) kundenVerweilzeitSum=K; else return String.format(Language.tr("XML.Statistic.Clients.HoldingTime.Clients.ErrorSum"),t);
					t=Language.trAllAttribute("XML.Statistic.GeneralAttributes.SquaresSum",e);
					K=NumberTools.getNotNegativeLong(NumberTools.systemNumberToLocalNumber(t));
					if (K!=null) kundenVerweilzeitSum2=K; else return String.format(Language.tr("XML.Statistic.Clients.HoldingTime.Clients.ErrorSquaresSum"),t);
					t=Language.trAllAttribute("XML.Statistic.GeneralAttributes.SumPerInterval",e);
					kundenVerweilzeitSumProIntervall=DataDistributionImpl.createFromString(t,kundenVerweilzeitSumProIntervall.upperBound);
					if (kundenVerweilzeitSumProIntervall==null) return Language.tr("XML.Statistic.Clients.HoldingTime.Clients.ErrorSumPerInterval");
					kundenVerweilzeitSumProIntervall.stretchToValueCount(48);
					continue;
				}

				if (Language.trAll("XML.Statistic.Clients.CancelTime.Clients",s)) {
					Long K;
					String t;
					t=Language.trAllAttribute("XML.Statistic.GeneralAttributes.Sum",e);
					K=NumberTools.getNotNegativeLong(NumberTools.systemNumberToLocalNumber(t));
					if (K!=null) kundenAbbruchzeitSum=K; else return String.format(Language.tr("XML.Statistic.Clients.CancelTime.Clients.ErrorSum"),t);
					t=Language.trAllAttribute("XML.Statistic.GeneralAttributes.SquaresSum",e);
					K=NumberTools.getNotNegativeLong(NumberTools.systemNumberToLocalNumber(t));
					if (K!=null) kundenAbbruchzeitSum2=K; else return String.format(Language.tr("XML.Statistic.Clients.CancelTime.Clients.ErrorSquaresSum"),t);
					t=Language.trAllAttribute("XML.Statistic.GeneralAttributes.SumPerInterval",e);
					kundenAbbruchzeitSumProIntervall=DataDistributionImpl.createFromString(t,kundenAbbruchzeitSumProIntervall.upperBound);
					if (kundenAbbruchzeitSumProIntervall==null) return Language.tr("XML.Statistic.Clients.CancelTime.Clients.ErrorSumPerInterval");
					kundenAbbruchzeitSumProIntervall.stretchToValueCount(48);
					continue;
				}

				if (Language.trAll("XML.Statistic.Clients.ServiceLevel.Clients",s)) {
					Integer K;
					String t;
					t=Language.trAllAttribute("XML.Statistic.GeneralAttributes.Count",e);
					K=NumberTools.getNotNegativeInteger(NumberTools.systemNumberToLocalNumber(t));
					if (K!=null) kundenServicelevel=K; else return String.format(Language.tr("XML.Statistic.Clients.ServiceLevel.Clients.ErrorSum"),t);
					t=Language.trAllAttribute("XML.Statistic.GeneralAttributes.CountPerInterval",e);
					kundenServicelevelProIntervall=DataDistributionImpl.createFromString(t,kundenServicelevelProIntervall.upperBound);
					if (kundenServicelevelProIntervall==null) return Language.tr("XML.Statistic.Clients.ServiceLevel.Clients.ErrorSumPerInterval");
					kundenServicelevelProIntervall.stretchToValueCount(48);
					continue;
				}

				if (Language.trAll("XML.Statistic.Clients.Forwarding.Clients",s)) {
					Integer K;
					String t;
					t=Language.trAllAttribute("XML.Statistic.GeneralAttributes.Count",e);
					K=NumberTools.getNotNegativeInteger(NumberTools.systemNumberToLocalNumber(t));
					if (K!=null) kundenWeiterleitungen=K; else return String.format(Language.tr("XML.Statistic.Clients.Forwarding.Clients.ErrorSum"),t);
					t=Language.trAllAttribute("XML.Statistic.GeneralAttributes.CountPerInterval",e);
					kundenWeiterleitungenProIntervall=DataDistributionImpl.createFromString(t,kundenWeiterleitungenProIntervall.upperBound);
					if (kundenWeiterleitungenProIntervall==null) return Language.tr("XML.Statistic.Clients.Forwarding.Clients.ErrorSumPerInterval");
					kundenWeiterleitungenProIntervall.stretchToValueCount(48);
					continue;
				}

				if (Language.trAll("XML.Statistic.Clients.Retry.Clients",s)) {
					Integer K;
					String t;
					t=Language.trAllAttribute("XML.Statistic.GeneralAttributes.Count",e);
					K=NumberTools.getNotNegativeInteger(NumberTools.systemNumberToLocalNumber(t));
					if (K!=null) kundenWiederholungen=K; else return String.format(Language.tr("XML.Statistic.Clients.Retry.Clients.ErrorSum"),t);
					t=Language.trAllAttribute("XML.Statistic.GeneralAttributes.CountPerInterval",e);
					kundenWiederholungenProIntervall=DataDistributionImpl.createFromString(t,kundenWiederholungenProIntervall.upperBound);
					if (kundenWiederholungenProIntervall==null) return Language.tr("XML.Statistic.Clients.Retry.Clients.ErrorSumPerInterval");
					kundenWiederholungenProIntervall.stretchToValueCount(48);
					continue;
				}

				if (Language.trAll("XML.Statistic.Clients.Recall.Clients",s)) {
					Integer K;
					String t;
					t=Language.trAllAttribute("XML.Statistic.GeneralAttributes.Count",e);
					K=NumberTools.getNotNegativeInteger(NumberTools.systemNumberToLocalNumber(t));
					if (K!=null) kundenWiederanruf=K; else return String.format(Language.tr("XML.Statistic.Clients.Recall.Clients.ErrorSum"),t);
					t=Language.trAllAttribute("XML.Statistic.GeneralAttributes.CountPerInterval",e);
					kundenWiederanrufProIntervall=DataDistributionImpl.createFromString(t,kundenWiederanrufProIntervall.upperBound);
					if (kundenWiederanrufProIntervall==null) return Language.tr("XML.Statistic.Clients.Recall.Clients.ErrorSumPerInterval");
					kundenWiederanrufProIntervall.stretchToValueCount(48);
					continue;
				}

				if (Language.trAll("XML.Statistic.Clients.WaitingTimeDistribution.Clients",s)) {
					String t=Language.trAllAttribute("XML.Statistic.GeneralAttributes.Distribution",e);
					kundenWartezeitVerteilung=DataDistributionImpl.createFromString(t,kundenWartezeitVerteilung.upperBound);
					if (kundenWartezeitVerteilung==null) return Language.tr("XML.Statistic.Clients.WaitingTimeDistribution.Clients.Error");
					kundenWartezeitVerteilung.stretchToValueCount(DistMax);
					continue;
				}

				if (Language.trAll("XML.Statistic.Clients.HoldingTimeDistribution.Clients",s)) {
					String t=Language.trAllAttribute("XML.Statistic.GeneralAttributes.Distribution",e);
					kundenVerweilzeitVerteilung=DataDistributionImpl.createFromString(t,kundenVerweilzeitVerteilung.upperBound);
					if (kundenVerweilzeitVerteilung==null) return Language.tr("XML.Statistic.Clients.HoldingTimeDistribution.Clients.Error");
					kundenVerweilzeitVerteilung.stretchToValueCount(DistMax);
					continue;
				}

				if (Language.trAll("XML.Statistic.Clients.CancelTimeDistribution.Clients",s)) {
					String t=Language.trAllAttribute("XML.Statistic.GeneralAttributes.Distribution",e);
					kundenAbbruchzeitVerteilung=DataDistributionImpl.createFromString(t,kundenAbbruchzeitVerteilung.upperBound);
					if (kundenAbbruchzeitVerteilung==null) return Language.tr("XML.Statistic.Clients.CancelTimeDistribution.Clients.Error");
					kundenAbbruchzeitVerteilung.stretchToValueCount(DistMax);
					continue;
				}

				if (Language.trAll("XML.Statistic.Clients.WaitingTimeDistributionLong.Clients",s)) {
					String t=Language.trAllAttribute("XML.Statistic.GeneralAttributes.Distribution",e);
					kundenWartezeitVerteilungLang=DataDistributionImpl.createFromString(t,kundenWartezeitVerteilungLang.upperBound);
					if (kundenWartezeitVerteilungLang==null) return Language.tr("XML.Statistic.Clients.WaitingTimeDistributionLong.Clients.Error");
					kundenWartezeitVerteilungLang.stretchToValueCount(DistMax);
					continue;
				}

				if (Language.trAll("XML.Statistic.Clients.HoldingTimeDistributionLong.Clients",s)) {
					String t=Language.trAllAttribute("XML.Statistic.GeneralAttributes.Distribution",e);
					kundenVerweilzeitVerteilungLang=DataDistributionImpl.createFromString(t,kundenVerweilzeitVerteilungLang.upperBound);
					if (kundenVerweilzeitVerteilungLang==null) return Language.tr("XML.Statistic.Clients.HoldingTimeDistributionLong.Clients.Error");
					kundenVerweilzeitVerteilungLang.stretchToValueCount(DistMax);
					continue;
				}

				if (Language.trAll("XML.Statistic.Clients.CancelTimeDistributionLong.Clients",s)) {
					String t=Language.trAllAttribute("XML.Statistic.GeneralAttributes.Distribution",e);
					kundenAbbruchzeitVerteilungLang=DataDistributionImpl.createFromString(t,kundenAbbruchzeitVerteilungLang.upperBound);
					if (kundenAbbruchzeitVerteilungLang==null) return Language.tr("XML.Statistic.Clients.CancelTimeDistributionLong.Clients.Error");
					kundenAbbruchzeitVerteilungLang.stretchToValueCount(DistMax);
					continue;
				}

				if (Language.trAll("XML.Statistic.Calls.Count",s)) {
					Integer K;
					String t;
					t=Language.trAllAttribute("XML.Statistic.Clients.ClientsCallsCount.Count",e);
					K=NumberTools.getNotNegativeInteger(NumberTools.systemNumberToLocalNumber(t));
					if (K!=null) anrufe=K; else return String.format(Language.tr("XML.Statistic.Clients.ClientsCallsCount.Count.ErrorCalls"),t);
					t=Language.trAllAttribute("XML.Statistic.Clients.ClientsCallsCount.Success",e);
					K=NumberTools.getNotNegativeInteger(NumberTools.systemNumberToLocalNumber(t));
					if (K!=null) anrufeErfolg=K; else return String.format(Language.tr("XML.Statistic.Clients.ClientsCallsCount.Success.ErrorCalls"),t);
					t=Language.trAllAttribute("XML.Statistic.Clients.ClientsCallsCount.Blocked",e);
					K=NumberTools.getNotNegativeInteger(NumberTools.systemNumberToLocalNumber(t));
					if (K!=null) anrufeBlocked=K; else return String.format(Language.tr("XML.Statistic.Clients.ClientsCallsCount.Blocked.ErrorCalls"),t);
					t=Language.trAllAttribute("XML.Statistic.Clients.ClientsCallsCount.Canceled",e);
					K=NumberTools.getNotNegativeInteger(NumberTools.systemNumberToLocalNumber(t));
					if (K!=null) anrufeAbbruch=K; else return String.format(Language.tr("XML.Statistic.Clients.ClientsCallsCount.Canceled.ErrorCalls"),t);
					t=Language.trAllAttribute("XML.Statistic.Clients.ClientsCallsCount.CarryOver",e);
					if (t.isEmpty()) {
						anrufeUebertrag=0;
					} else {
						K=NumberTools.getNotNegativeInteger(NumberTools.systemNumberToLocalNumber(t));
						if (K!=null) anrufeUebertrag=K; else return String.format(Language.tr("XML.Statistic.Clients.ClientsCallsCount.CarryOver.ErrorCalls"),t);
					}
					anrufeProIntervall=DataDistributionImpl.createFromString(Language.trAllAttribute("XML.Statistic.Clients.ClientsCallsCount.CountPerInterval",e),anrufeProIntervall.upperBound);
					if (anrufeProIntervall==null) return Language.tr("XML.Statistic.Clients.ClientsCallsCount.CountPerInterval.ErrorCalls");
					anrufeProIntervall.stretchToValueCount(48);
					anrufeErfolgProIntervall=DataDistributionImpl.createFromString(Language.trAllAttribute("XML.Statistic.Clients.ClientsCallsCount.SuccessPerInterval",e),anrufeErfolgProIntervall.upperBound);
					if (anrufeErfolgProIntervall==null) return Language.tr("XML.Statistic.Clients.ClientsCallsCount.SuccessPerInterval.ErrorCalls");
					anrufeErfolgProIntervall.stretchToValueCount(48);
					anrufeBlockedProIntervall=DataDistributionImpl.createFromString(Language.trAllAttribute("XML.Statistic.Clients.ClientsCallsCount.BlockedPerInterval",e),anrufeBlockedProIntervall.upperBound);
					if (anrufeBlockedProIntervall==null) return Language.tr("XML.Statistic.Clients.ClientsCallsCount.BlockedPerInterval.ErrorCalls");
					anrufeBlockedProIntervall.stretchToValueCount(48);
					anrufeAbbruchProIntervall=DataDistributionImpl.createFromString(Language.trAllAttribute("XML.Statistic.Clients.ClientsCallsCount.CanceledPerInterval",e),anrufeAbbruchProIntervall.upperBound);
					if (anrufeAbbruchProIntervall==null) return Language.tr("XML.Statistic.Clients.ClientsCallsCount.CanceledPerInterval.ErrorCalls");
					anrufeAbbruchProIntervall.stretchToValueCount(48);
					continue;
				}

				if (Language.trAll("XML.Statistic.Clients.WaitingTime.Calls",s)) {
					Long K;
					String t;
					t=Language.trAllAttribute("XML.Statistic.GeneralAttributes.Sum",e);
					K=NumberTools.getNotNegativeLong(NumberTools.systemNumberToLocalNumber(t));
					if (K!=null) anrufeWartezeitSum=K; else return String.format(Language.tr("XML.Statistic.Clients.WaitingTime.Calls.ErrorSum"),t);
					t=Language.trAllAttribute("XML.Statistic.GeneralAttributes.SquaresSum",e);
					K=NumberTools.getNotNegativeLong(NumberTools.systemNumberToLocalNumber(t));
					if (K!=null) anrufeWartezeitSum2=K; else return String.format(Language.tr("XML.Statistic.Clients.WaitingTime.Calls.ErrorSquaresSum"),t);
					t=Language.trAllAttribute("XML.Statistic.GeneralAttributes.SumPerInterval",e);
					anrufeWartezeitSumProIntervall=DataDistributionImpl.createFromString(t,anrufeWartezeitSumProIntervall.upperBound);
					if (anrufeWartezeitSumProIntervall==null) return Language.tr("XML.Statistic.Clients.WaitingTime.Calls.ErrorSumPerInterval");
					anrufeWartezeitSumProIntervall.stretchToValueCount(48);
					continue;
				}

				if (Language.trAll("XML.Statistic.Clients.HoldingTime.Calls",s)) {
					Long K;
					String t;
					t=Language.trAllAttribute("XML.Statistic.GeneralAttributes.Sum",e);
					K=NumberTools.getNotNegativeLong(NumberTools.systemNumberToLocalNumber(t));
					if (K!=null) anrufeVerweilzeitSum=K; else return String.format(Language.tr("XML.Statistic.Clients.HoldingTime.Calls.ErrorSum"),t);
					t=Language.trAllAttribute("XML.Statistic.GeneralAttributes.SquaresSum",e);
					K=NumberTools.getNotNegativeLong(NumberTools.systemNumberToLocalNumber(t));
					if (K!=null) anrufeVerweilzeitSum2=K; else return String.format(Language.tr("XML.Statistic.Clients.HoldingTime.Calls.ErrorSquaresSum"),t);
					t=Language.trAllAttribute("XML.Statistic.GeneralAttributes.SumPerInterval",e);
					anrufeVerweilzeitSumProIntervall=DataDistributionImpl.createFromString(t,anrufeVerweilzeitSumProIntervall.upperBound);
					if (anrufeVerweilzeitSumProIntervall==null) return Language.tr("XML.Statistic.Clients.HoldingTime.Calls.ErrorSumPerInterval");
					anrufeVerweilzeitSumProIntervall.stretchToValueCount(48);
					continue;
				}

				if (Language.trAll("XML.Statistic.Clients.CancelTime.Calls",s)) {
					Long K;
					String t;
					t=Language.trAllAttribute("XML.Statistic.GeneralAttributes.Sum",e);
					K=NumberTools.getNotNegativeLong(NumberTools.systemNumberToLocalNumber(t));
					if (K!=null) anrufeAbbruchzeitSum=K; else return String.format(Language.tr("XML.Statistic.Clients.CancelTime.Calls.ErrorSum"),t);
					t=Language.trAllAttribute("XML.Statistic.GeneralAttributes.SquaresSum",e);
					K=NumberTools.getNotNegativeLong(NumberTools.systemNumberToLocalNumber(t));
					if (K!=null) anrufeAbbruchzeitSum2=K; else return String.format(Language.tr("XML.Statistic.Clients.CancelTime.Calls.ErrorSquaresSum"),t);
					t=Language.trAllAttribute("XML.Statistic.GeneralAttributes.SumPerInterval",e);
					anrufeAbbruchzeitSumProIntervall=DataDistributionImpl.createFromString(t,anrufeAbbruchzeitSumProIntervall.upperBound);
					if (anrufeAbbruchzeitSumProIntervall==null) return Language.tr("XML.Statistic.Clients.CancelTime.Calls.ErrorSumPerInterval");
					anrufeAbbruchzeitSumProIntervall.stretchToValueCount(48);
					continue;
				}

				if (Language.trAll("XML.Statistic.Clients.ServiceLevel.Calls",s)) {
					Integer K;
					String t;
					t=Language.trAllAttribute("XML.Statistic.GeneralAttributes.Count",e);
					K=NumberTools.getNotNegativeInteger(NumberTools.systemNumberToLocalNumber(t));
					if (K!=null) anrufeServicelevel=K; else return String.format(Language.tr("XML.Statistic.Clients.ServiceLevel.Calls.ErrorSum"),t);
					t=Language.trAllAttribute("XML.Statistic.GeneralAttributes.CountPerInterval",e);
					anrufeServicelevelProIntervall=DataDistributionImpl.createFromString(t,anrufeServicelevelProIntervall.upperBound);
					if (anrufeServicelevelProIntervall==null) return Language.tr("XML.Statistic.Clients.ServiceLevel.Calls.ErrorSumPerInterval");
					anrufeServicelevelProIntervall.stretchToValueCount(48);
					continue;
				}

				if (Language.trAll("XML.Statistic.Clients.Forwarding.Calls",s)) {
					Integer K;
					String t;
					t=Language.trAllAttribute("XML.Statistic.GeneralAttributes.Count",e);
					K=NumberTools.getNotNegativeInteger(NumberTools.systemNumberToLocalNumber(t));
					if (K!=null) anrufeWeiterleitungen=K; else return String.format(Language.tr("XML.Statistic.Clients.Forwarding.Calls.ErrorSum"),t);
					t=Language.trAllAttribute("XML.Statistic.GeneralAttributes.CountPerInterval",e);
					anrufeWeiterleitungenProIntervall=DataDistributionImpl.createFromString(t,anrufeWeiterleitungenProIntervall.upperBound);
					if (anrufeWeiterleitungenProIntervall==null) return Language.tr("XML.Statistic.Clients.Forwarding.Calls.ErrorSumPerInterval");
					anrufeWeiterleitungenProIntervall.stretchToValueCount(48);
					continue;
				}

				if (Language.trAll("XML.Statistic.Clients.Retry.Calls",s)) {
					Integer K;
					String t;
					t=Language.trAllAttribute("XML.Statistic.GeneralAttributes.Count",e);
					K=NumberTools.getNotNegativeInteger(NumberTools.systemNumberToLocalNumber(t));
					if (K!=null) anrufeWiederholungen=K; else return String.format(Language.tr("XML.Statistic.Clients.Retry.Calls.ErrorSum"),t);
					t=Language.trAllAttribute("XML.Statistic.GeneralAttributes.CountPerInterval",e);
					anrufeWiederholungenProIntervall=DataDistributionImpl.createFromString(t,anrufeWiederholungenProIntervall.upperBound);
					if (anrufeWiederholungenProIntervall==null) return Language.tr("XML.Statistic.Clients.Retry.Calls.ErrorSumPerInterval");
					anrufeWiederholungenProIntervall.stretchToValueCount(48);
					continue;
				}

				if (Language.trAll("XML.Statistic.Clients.WaitingTimeDistribution.Calls",s)) {
					String t=Language.trAllAttribute("XML.Statistic.GeneralAttributes.Distribution",e);
					anrufeWartezeitVerteilung=DataDistributionImpl.createFromString(t,anrufeWartezeitVerteilung.upperBound);
					if (anrufeWartezeitVerteilung==null) return Language.tr("XML.Statistic.Clients.WaitingTimeDistribution.Calls.Error");
					anrufeWartezeitVerteilung.stretchToValueCount(DistMax);
					continue;
				}

				if (Language.trAll("XML.Statistic.Clients.HoldingTimeDistribution.Calls",s)) {
					String t=Language.trAllAttribute("XML.Statistic.GeneralAttributes.Distribution",e);
					anrufeVerweilzeitVerteilung=DataDistributionImpl.createFromString(t,anrufeVerweilzeitVerteilung.upperBound);
					if (anrufeVerweilzeitVerteilung==null) return Language.tr("XML.Statistic.Clients.HoldingTimeDistribution.Calls.Error");
					anrufeVerweilzeitVerteilung.stretchToValueCount(DistMax);
					continue;
				}

				if (Language.trAll("XML.Statistic.Clients.CancelTimeDistribution.Calls",s)) {
					String t=Language.trAllAttribute("XML.Statistic.GeneralAttributes.Distribution",e);
					anrufeAbbruchzeitVerteilung=DataDistributionImpl.createFromString(t,anrufeAbbruchzeitVerteilung.upperBound);
					if (anrufeAbbruchzeitVerteilung==null) return Language.tr("XML.Statistic.Clients.CancelTimeDistribution.Calls.Error");
					anrufeAbbruchzeitVerteilung.stretchToValueCount(DistMax);
					continue;
				}

				if (Language.trAll("XML.Statistic.Clients.WaitingTimeDistributionLong.Calls",s)) {
					String t=Language.trAllAttribute("XML.Statistic.GeneralAttributes.Distribution",e);
					anrufeWartezeitVerteilungLang=DataDistributionImpl.createFromString(t,anrufeWartezeitVerteilungLang.upperBound);
					if (anrufeWartezeitVerteilungLang==null) return Language.tr("XML.Statistic.Clients.WaitingTimeDistributionLong.Calls.Error");
					anrufeWartezeitVerteilungLang.stretchToValueCount(DistMax);
					continue;
				}

				if (Language.trAll("XML.Statistic.Clients.HoldingTimeDistributionLong.Calls",s)) {
					String t=Language.trAllAttribute("XML.Statistic.GeneralAttributes.Distribution",e);
					anrufeVerweilzeitVerteilungLang=DataDistributionImpl.createFromString(t,anrufeVerweilzeitVerteilungLang.upperBound);
					if (anrufeVerweilzeitVerteilungLang==null) return Language.tr("XML.Statistic.Clients.HoldingTimeDistributionLong.Calls.Error");
					anrufeVerweilzeitVerteilungLang.stretchToValueCount(DistMax);
					continue;
				}

				if (Language.trAll("XML.Statistic.Clients.CancelTimeDistributionLong.Calls",s)) {
					String t=Language.trAllAttribute("XML.Statistic.GeneralAttributes.Distribution",e);
					anrufeAbbruchzeitVerteilungLang=DataDistributionImpl.createFromString(t,anrufeAbbruchzeitVerteilungLang.upperBound);
					if (anrufeAbbruchzeitVerteilungLang==null) return Language.tr("XML.Statistic.Clients.CancelTimeDistributionLong.Calls.Error");
					anrufeAbbruchzeitVerteilungLang.stretchToValueCount(DistMax);
					continue;
				}

				if (Language.trAll("XML.Statistic.Confidence",s)) {
					NodeList l2=e.getChildNodes();
					for (int j=0; j<l2.getLength();j++) {
						if (!(l2.item(j) instanceof Element)) continue;
						Element e2=(Element)l2.item(j);
						String t=e2.getNodeName();

						if (Language.trAll("XML.Statistic.Confidence.Accessibility.Calls",t)) {
							String u;
							u=Language.trAllAttribute("XML.Statistic.GeneralAttributes.Sum",e2);
							Double d=NumberTools.getNotNegativeDouble(NumberTools.systemNumberToLocalNumber(u));
							if (d!=null) interDaySuccessCallsSum=d; else return String.format(Language.tr("XML.Statistic.Confidence.Accessibility.Calls.ErrorSum"),u);
							u=Language.trAllAttribute("XML.Statistic.GeneralAttributes.SquaresSum",e2);
							d=NumberTools.getNotNegativeDouble(NumberTools.systemNumberToLocalNumber(u));
							if (d!=null) interDaySuccessCallsSum2=d; else return String.format(Language.tr("XML.Statistic.Confidence.Accessibility.Calls.ErrorSquaresSum"),u);
							continue;
						}

						if (Language.trAll("XML.Statistic.Confidence.Accessibility.Clients",t)) {
							String u;
							u=Language.trAllAttribute("XML.Statistic.GeneralAttributes.Sum",e2);
							Double d=NumberTools.getNotNegativeDouble(NumberTools.systemNumberToLocalNumber(u));
							if (d!=null) interDaySuccessClientsSum=d; else return String.format(Language.tr("XML.Statistic.Confidence.Accessibility.Clients.ErrorSum"),u);
							u=Language.trAllAttribute("XML.Statistic.GeneralAttributes.SquaresSum",e2);
							d=NumberTools.getNotNegativeDouble(NumberTools.systemNumberToLocalNumber(u));
							if (d!=null) interDaySuccessClientsSum2=d; else return String.format(Language.tr("XML.Statistic.Confidence.Accessibility.Clients.ErrorSquaresSum"),u);
							continue;
						}


						if (Language.trAll("XML.Statistic.Confidence.WaitingTime",t)) {
							String u;
							u=Language.trAllAttribute("XML.Statistic.GeneralAttributes.Sum",e2);
							Double d=NumberTools.getNotNegativeDouble(NumberTools.systemNumberToLocalNumber(u));
							if (d!=null) interDayWartezeitSum=d; else return String.format(Language.tr("XML.Statistic.Confidence.WaitingTime.ErrorSum"),u);
							u=Language.trAllAttribute("XML.Statistic.GeneralAttributes.SquaresSum",e2);
							d=NumberTools.getNotNegativeDouble(NumberTools.systemNumberToLocalNumber(u));
							if (d!=null) interDayWartezeitSum2=d; else return String.format(Language.tr("XML.Statistic.Confidence.WaitingTime.ErrorSquaresSum"),u);
							continue;
						}

						if (Language.trAll("XML.Statistic.Confidence.ServiceLevel.CallsSuccess",t)) {
							String u;
							u=Language.trAllAttribute("XML.Statistic.GeneralAttributes.Sum",e2);
							Double d=NumberTools.getNotNegativeDouble(NumberTools.systemNumberToLocalNumber(u));
							if (d!=null) interDayServiceLevelCallsSuccessSum=d; else return String.format(Language.tr("XML.Statistic.Confidence.ServiceLevel.CallsSuccess.ErrorSum"),u);
							u=Language.trAllAttribute("XML.Statistic.GeneralAttributes.SquaresSum",e2);
							d=NumberTools.getNotNegativeDouble(NumberTools.systemNumberToLocalNumber(u));
							if (d!=null) interDayServiceLevelCallsSuccessSum2=d; else return String.format(Language.tr("XML.Statistic.Confidence.ServiceLevel.CallsSuccess.ErrorSquaresSum"),u);
							continue;
						}

						if (Language.trAll("XML.Statistic.Confidence.ServiceLevel.CallsAll",t)) {
							String u;
							u=Language.trAllAttribute("XML.Statistic.GeneralAttributes.Sum",e2);
							Double d=NumberTools.getNotNegativeDouble(NumberTools.systemNumberToLocalNumber(u));
							if (d!=null) interDayServiceLevelCallsAllSum=d; else return String.format(Language.tr("XML.Statistic.Confidence.ServiceLevel.CallsAll.ErrorSum"),u);
							u=Language.trAllAttribute("XML.Statistic.GeneralAttributes.SquaresSum",e2);
							d=NumberTools.getNotNegativeDouble(NumberTools.systemNumberToLocalNumber(u));
							if (d!=null) interDayServiceLevelCallsAllSum2=d; else return String.format(Language.tr("XML.Statistic.Confidence.ServiceLevel.CallsAll.ErrorSquaresSum"),u);
							continue;
						}

						if (Language.trAll("XML.Statistic.Confidence.ServiceLevel.ClientsSuccess",t)) {
							String u;
							u=Language.trAllAttribute("XML.Statistic.GeneralAttributes.Sum",e2);
							Double d=NumberTools.getNotNegativeDouble(NumberTools.systemNumberToLocalNumber(u));
							if (d!=null) interDayServiceLevelClientsSuccessSum=d; else return String.format(Language.tr("XML.Statistic.Confidence.ServiceLevel.ClientsSuccess.ErrorSum"),u);
							u=Language.trAllAttribute("XML.Statistic.GeneralAttributes.SquaresSum",e2);
							d=NumberTools.getNotNegativeDouble(NumberTools.systemNumberToLocalNumber(u));
							if (d!=null) interDayServiceLevelClientsSuccessSum2=d; else return String.format(Language.tr("XML.Statistic.Confidence.ServiceLevel.ClientsSuccess.ErrorSquaresSum"),u);
							continue;
						}

						if (Language.trAll("XML.Statistic.Confidence.ServiceLevel.ClientsAll",t)) {
							String u;
							u=Language.trAllAttribute("XML.Statistic.GeneralAttributes.Sum",e2);
							Double d=NumberTools.getNotNegativeDouble(NumberTools.systemNumberToLocalNumber(u));
							if (d!=null) interDayServiceLevelClientsAllSum=d; else return String.format(Language.tr("XML.Statistic.Confidence.ServiceLevel.ClientsAll.ErrorSum"),u);
							u=Language.trAllAttribute("XML.Statistic.GeneralAttributes.SquaresSum",e2);
							d=NumberTools.getNotNegativeDouble(NumberTools.systemNumberToLocalNumber(u));
							if (d!=null) interDayServiceLevelClientsAllSum2=d; else return String.format(Language.tr("XML.Statistic.Confidence.ServiceLevel.ClientsAll.ErrorSquaresSum"),u);
							continue;
						}
					}
					continue;
				}

				if (Language.trAll("XML.Statistic.Costs",s)) {
					String t;
					t=Language.trAllAttribute("XML.Statistic.Costs.Yield",e);
					Double d=NumberTools.getNotNegativeDouble(t);
					if (d!=null) revenue=d; else return String.format(Language.tr("XML.Statistic.Costs.Yield.Error"),t);
					t=Language.trAllAttribute("XML.Statistic.Costs.WaitingTimes",e);
					d=NumberTools.getNotNegativeDouble(t);
					if (d!=null) costWaiting=d; else return String.format(Language.tr("XML.Statistic.Costs.WaitingTimes.Error"),t);
					t=Language.trAllAttribute("XML.Statistic.Costs.Cancelations",e);
					d=NumberTools.getNotNegativeDouble(t);
					if (d!=null) costCancel=d; else return String.format(Language.tr("XML.Statistic.Costs.Cancelations.Error"),t);
					continue;
				}
			}
			return null;
		}

		/**
		 * Berechnet aus Messreihen-Kenngrößen die Standardabweichung
		 * @param x2	Quadrierte Summe der Werte
		 * @param x	Summe der Werte
		 * @param n	Anzahl der Werte
		 * @return	Standardabweichung der Messreihe
		 */
		private double calcStd(double x2, double x, double n) {
			if (n>0) return StrictMath.sqrt(x2/n-x*x/n/n); else return 0;
		}

		/**
		 * Berechnet aus Messreihen-Kenngrößen ein Konfidenzintervall
		 * @param x2	Quadrierte Summe der Werte
		 * @param x	Summe der Werte
		 * @param n	Anzahl der Werte
		 * @param p	Wahrscheinlichkeit, zu der das Konfidenzintervall bestimmt werden soll
		 * @return	Konfidenzintervall der Messreihe zur Wahrscheinlichkeit <code>p</code>
		 */
		private double[] calcConfidence(double x2, double x, double n, double p) {
			double[] interval=new double[2];

			double mean=x/n;
			double sd=calcStd(x2,x,n);

			/* x +- z(1-alpha/2)*sd/sqrt(n) */
			NormalDistribution dist=new NormalDistribution(0,1);
			double half;
			half=dist.inverseCumulativeProbability(1-(1-p)/2)*sd/Math.sqrt(n);

			interval[0]=mean-half;
			interval[1]=mean+half;

			return interval;
		}

		/**
		 * Speichert Konfidenzintervall-Daten für eine Kenngröße.
		 * @param node2	XML-Element in dem die Daten als Attribute gespeichert werden sollen
		 * @param sum	Summe der Werte für die Kenngröße
		 * @param sum2	Summe der quadrierten Werte für die Kenngröße
		 */
		private void saveConfidencePercentValue(final Element node2, final double sum, final double sum2) {
			double[] c;
			node2.setAttribute(Language.trPrimary("XML.Statistic.GeneralAttributes.Sum"),NumberTools.formatSystemNumber(sum));
			node2.setAttribute(Language.trPrimary("XML.Statistic.GeneralAttributes.SquaresSum"),NumberTools.formatSystemNumber(sum2));
			c=calcConfidence(sum2,sum,simulationData.runRepeatCount,0.9);
			node2.setAttribute(Language.trPrimary("XML.Statistic.GeneralAttributes.Confidence90Min"),NumberTools.formatSystemNumber(c[0]*100,5)+"%");
			node2.setAttribute(Language.trPrimary("XML.Statistic.GeneralAttributes.Confidence90Max"),NumberTools.formatSystemNumber(c[1]*100,5)+"%");
			c=calcConfidence(sum2,sum,simulationData.runRepeatCount,0.95);
			node2.setAttribute(Language.trPrimary("XML.Statistic.GeneralAttributes.Confidence95Min"),NumberTools.formatSystemNumber(c[0]*100,5)+"%");
			node2.setAttribute(Language.trPrimary("XML.Statistic.GeneralAttributes.Confidence95Max"),NumberTools.formatSystemNumber(c[1]*100,5)+"%");
		}

		/**
		 * Speichert die Zusammenfassungsdaten
		 * @param parent	Übergeordnetes XML-Element
		 * @param intFrom	Erstes zu berücksichtigendes Intervall (0..47)
		 * @param intTo	Letztes zu berücksichtigendes Intervall (0..47)
		 */
		private void saveSummary(final Element parent, int intFrom, int intTo) {
			Document doc=parent.getOwnerDocument();
			Element node;

			parent.appendChild(node=doc.createElement(Language.trPrimary("XML.Statistic.Clients.Summary")));

			if (intFrom<0 || intFrom>47) intFrom=0;
			if (intTo<0 || intTo>47) intTo=47;

			node.setAttribute(Language.trPrimary("XML.Statistic.Clients.Summary.Range"),TimeTools.formatTime(intFrom*1800)+"-"+TimeTools.formatTime((intTo+1)*1800));

			double sum, success, result, time;

			/* Erreichbarkeit auf Kundenbasis */
			sum=0;
			for (int i=intFrom;i<=intTo;i++) sum+=kundenProIntervall.densityData[i]+kundenWiederanrufProIntervall.densityData[i];
			if (intFrom==0) sum-=kundenUebertrag;
			if (sum<=0) result=0; else {
				success=0;
				for (int i=intFrom;i<=intTo;i++) success+=kundenErfolgProIntervall.densityData[i];
				result=success/sum;
			}
			node.setAttribute(Language.trPrimary("XML.Statistic.Clients.Summary.ClientsAccessibility"),NumberTools.formatSystemNumber(result*100)+"%");

			/* Abbruchzeit auf Kundenbasis */
			sum=0;
			for (int i=intFrom;i<=intTo;i++) sum+=kundenAbbruchProIntervall.densityData[i];
			if (sum<=0) result=0; else {
				success=0;
				for (int i=intFrom;i<=intTo;i++) success+=kundenAbbruchzeitSumProIntervall.densityData[i];
				result=success/sum;
			}
			node.setAttribute(Language.trPrimary("XML.Statistic.Clients.Summary.ClientsCancelTime"),TimeTools.formatExactSystemTime(result));

			/* Erreichbarkeit auf Anrufbasis */
			sum=0;
			for (int i=intFrom;i<=intTo;i++) sum+=anrufeProIntervall.densityData[i];
			if (intFrom==0) sum-=anrufeUebertrag;
			if (sum<=0) result=0; else {
				success=0;
				for (int i=intFrom;i<=intTo;i++) success+=anrufeErfolgProIntervall.densityData[i];
				result=success/sum;
			}
			node.setAttribute(Language.trPrimary("XML.Statistic.Clients.Summary.CallsAccessibility"),NumberTools.formatSystemNumber(result*100)+"%");

			/* Abbruchzeit auf Anrufbasis */
			sum=0;
			for (int i=intFrom;i<=intTo;i++) sum+=anrufeAbbruchProIntervall.densityData[i];
			if (sum<=0) result=0; else {
				success=0;
				for (int i=intFrom;i<=intTo;i++) success+=anrufeAbbruchzeitSumProIntervall.densityData[i];
				result=success/sum;
			}
			node.setAttribute(Language.trPrimary("XML.Statistic.Clients.Summary.CallsCancelTime"),TimeTools.formatExactSystemTime(result));

			/* Summe der erfolgreichen Kunden (für weitere Berechnungen) */
			sum=0;
			for (int i=intFrom;i<=intTo;i++) sum+=kundenErfolgProIntervall.densityData[i];

			/* Wartezeit auf Kundenbasis */
			if (sum<=0) result=0; else {
				success=0;
				for (int i=intFrom;i<=intTo;i++) success+=kundenWartezeitSumProIntervall.densityData[i];
				result=success/sum;
			}
			node.setAttribute(Language.trPrimary("XML.Statistic.Clients.Summary.ClientsAverageWaitingTime"),TimeTools.formatExactSystemTime(result));

			/* Kombinierte Warte- bzw. Abbruchzeit auf Kundenbasis */
			sum=0;
			for (int i=intFrom;i<=intTo;i++) {
				sum+=kundenAbbruchProIntervall.densityData[i];
				sum+=kundenErfolgProIntervall.densityData[i];
			}
			if (sum<=0) result=0; else {
				time=0;
				for (int i=intFrom;i<=intTo;i++) {
					time+=kundenAbbruchzeitSumProIntervall.densityData[i];
					time+=kundenWartezeitSumProIntervall.densityData[i];
				}
				result=time/sum;
			}
			node.setAttribute(Language.trPrimary("XML.Statistic.Clients.Summary.ClientsCombinedWaitingCancelTime"),TimeTools.formatExactSystemTime(result));

			/* Verweilzeit auf Kundenbasis */
			if (sum<=0) result=0; else {
				success=0;
				for (int i=intFrom;i<=intTo;i++) success+=kundenVerweilzeitSumProIntervall.densityData[i];
				result=success/sum;
			}
			node.setAttribute(Language.trPrimary("XML.Statistic.Clients.Summary.ClientsAverageResidenceTime"),TimeTools.formatExactSystemTime(result));

			/* Service-Level (bezogen auf erfolgreiche) auf Kundenbasis */
			if (sum<=0) result=0; else {
				success=0;
				for (int i=intFrom;i<=intTo;i++) success+=kundenServicelevelProIntervall.densityData[i];
				result=success/sum;
			}
			node.setAttribute(Language.trPrimary("XML.Statistic.Clients.Summary.ClientsServiceLevel"),NumberTools.formatSystemNumber(result*100)+"%");

			/* Summe der Kunden (für weitere Berechnungen) */
			sum=0;
			for (int i=intFrom;i<=intTo;i++) sum+=kundenProIntervall.densityData[i];

			/* Service-Level (bezogen auf alle) auf Kundenbasis */
			if (sum<=0) result=0; else {
				success=0;
				for (int i=intFrom;i<=intTo;i++) success+=kundenServicelevelProIntervall.densityData[i];
				result=success/sum;
			}
			node.setAttribute(Language.trPrimary("XML.Statistic.Clients.Summary.ClientsServiceLevelAll"),NumberTools.formatSystemNumber(result*100)+"%");

			/* Summe der erfolgreichen Anrufe (für weitere Berechnungen) */
			sum=0;
			for (int i=intFrom;i<=intTo;i++) sum+=anrufeErfolgProIntervall.densityData[i];

			/* Wartezeit auf Anrufbasis */
			if (sum<=0) result=0; else {
				success=0;
				for (int i=intFrom;i<=intTo;i++) success+=anrufeWartezeitSumProIntervall.densityData[i];
				result=success/sum;
			}
			node.setAttribute(Language.trPrimary("XML.Statistic.Clients.Summary.CallsAverageWaitingTime"),TimeTools.formatExactSystemTime(result));

			/* Kombinierte Warte- bzw. Abbruchzeit auf Anrufbasis */
			sum=0;
			for (int i=intFrom;i<=intTo;i++) {
				sum+=anrufeAbbruchProIntervall.densityData[i];
				sum+=anrufeErfolgProIntervall.densityData[i];
			}
			if (sum<=0) result=0; else {
				time=0;
				for (int i=intFrom;i<=intTo;i++) {
					time+=anrufeAbbruchzeitSumProIntervall.densityData[i];
					time+=anrufeWartezeitSumProIntervall.densityData[i];
				}
				result=time/sum;
			}
			node.setAttribute(Language.trPrimary("XML.Statistic.Clients.Summary.CallsCombinedWaitingCancelTime"),TimeTools.formatExactSystemTime(result));


			/* Verweilzeit auf Anrufbasis */
			if (sum<=0) result=0; else {
				success=0;
				for (int i=intFrom;i<=intTo;i++) success+=anrufeVerweilzeitSumProIntervall.densityData[i];
				result=success/sum;
			}
			node.setAttribute(Language.trPrimary("XML.Statistic.Clients.Summary.CallsAverageResidenceTime"),TimeTools.formatExactSystemTime(result));

			/* Service-Level (bezogen auf erfolgreiche) auf Anrufbasis */
			if (sum<=0) result=0; else {
				success=0;
				for (int i=intFrom;i<=intTo;i++) success+=anrufeServicelevelProIntervall.densityData[i];
				result=success/sum;
			}
			node.setAttribute(Language.trPrimary("XML.Statistic.Clients.Summary.CallsServiceLevel"),NumberTools.formatSystemNumber(result*100)+"%");

			/* Summe der Anrufe (für weitere Berechnungen) */
			sum=0;
			for (int i=intFrom;i<=intTo;i++) sum+=anrufeProIntervall.densityData[i];

			/* Service-Level (bezogen auf alle) auf Anrufbasis */
			if (sum<=0) result=0; else {
				success=0;
				for (int i=intFrom;i<=intTo;i++) success+=anrufeServicelevelProIntervall.densityData[i];
				result=success/sum;
			}
			node.setAttribute(Language.trPrimary("XML.Statistic.Clients.Summary.CallsServiceLevelAll"),NumberTools.formatSystemNumber(result*100)+"%");
		}

		/**
		 * Versucht einen Kunden-Datensatz in einem XML-Knoten zu speichern
		 * @param parent Übergeordneter XML-Knoten
		 */
		private void saveDataToXML(final Element parent) {
			final Document doc=parent.getOwnerDocument();
			Element node, node2;

			parent.appendChild(node=doc.createElement(Language.trPrimary("XML.Statistic.Clients.Count")));
			node.setAttribute(Language.trPrimary("XML.Statistic.Clients.ClientsCallsCount.Count"),""+kunden);
			node.setAttribute(Language.trPrimary("XML.Statistic.Clients.ClientsCallsCount.Success"),""+kundenErfolg);
			node.setAttribute(Language.trPrimary("XML.Statistic.Clients.ClientsCallsCount.Blocked"),""+kundenBlocked);
			node.setAttribute(Language.trPrimary("XML.Statistic.Clients.ClientsCallsCount.Canceled"),""+kundenAbbruch);
			node.setAttribute(Language.trPrimary("XML.Statistic.Clients.ClientsCallsCount.CarryOver"),""+kundenUebertrag);
			node.setAttribute(Language.trPrimary("XML.Statistic.Clients.ClientsCallsCount.CountPerInterval"),kundenProIntervall.storeToString());
			node.setAttribute(Language.trPrimary("XML.Statistic.Clients.ClientsCallsCount.SuccessPerInterval"),kundenErfolgProIntervall.storeToString());
			node.setAttribute(Language.trPrimary("XML.Statistic.Clients.ClientsCallsCount.BlockedPerInterval"),kundenBlockedProIntervall.storeToString());
			node.setAttribute(Language.trPrimary("XML.Statistic.Clients.ClientsCallsCount.CanceledPerInterval"),kundenAbbruchProIntervall.storeToString());

			saveSummary(parent,-1,-1); /* ganzer Tag */
			saveSummary(parent,2,45); /* 01..11 */
			saveSummary(parent,4,43); /* 02..10 */
			saveSummary(parent,0,23); /* 00..12 */
			saveSummary(parent,24,47); /* 12..24 */
			saveSummary(parent,12,35); /* 06..18 */
			saveSummary(parent,0,11); /* 00..06 */
			saveSummary(parent,12,23); /* 06..12 */
			saveSummary(parent,24,35); /* 12..18 */
			saveSummary(parent,36,47); /* 18..24 */

			parent.appendChild(node=doc.createElement(Language.trPrimary("XML.Statistic.Clients.CarryOver")));

			node.appendChild(node2=doc.createElement(Language.trPrimary("XML.Statistic.Clients.CarryOver.Canceled")));
			DataDistributionImpl AbbruchProTag=new DataDistributionImpl(1000,kundenAbbruchProSimDay.toArray(new Integer[0]));
			node2.setTextContent(AbbruchProTag.storeToString());

			node.appendChild(node2=doc.createElement(Language.trPrimary("XML.Statistic.Clients.CarryOver.RetryTimes")));
			StringBuilder s=new StringBuilder();
			for (int i=0;i<kundenNextDayRetryProSimDay.size();i++) {
				if (i>0) s.append('|');
				Long[] list=kundenNextDayRetryProSimDay.get(i).toArray(new Long[0]);
				StringBuilder t=new StringBuilder(); for (int j=0;j<list.length;j++) {if (t.length()>0) t.append(';'); t.append(list[j].toString());}
				s.append(t);
			}
			node2.setTextContent(s.toString());

			node.appendChild(node2=doc.createElement(Language.trPrimary("XML.Statistic.Clients.CarryOver.WaitingTimeTolerances")));
			s=new StringBuilder();
			for (int i=0;i<kundenNextDayUebertragWaitingTimeProSimDay.size();i++) {
				if (i>0) s.append('|');
				Long[] list1=kundenNextDayUebertragWaitingTimeProSimDay.get(i).toArray(new Long[0]);
				Long[] list2=kundenNextDayUebertragRestWaitingToleranceProSimDay.get(i).toArray(new Long[0]);
				StringBuilder t=new StringBuilder(); for (int j=0;j<list1.length;j++) {if (t.length()>0) t.append(';'); t.append(list1[j].toString()+"/"+list2[j].toString());}
				s.append(t);
			}
			node2.setTextContent(s.toString());

			parent.appendChild(node=doc.createElement(Language.trPrimary("XML.Statistic.Clients.WaitingTime.Clients")));
			node.setAttribute(Language.trPrimary("XML.Statistic.GeneralAttributes.Sum"),""+kundenWartezeitSum);
			node.setAttribute(Language.trPrimary("XML.Statistic.GeneralAttributes.SquaresSum"),""+kundenWartezeitSum2);
			node.setAttribute(Language.trPrimary("XML.Statistic.GeneralAttributes.SumPerInterval"),kundenWartezeitSumProIntervall.storeToString());

			parent.appendChild(node=doc.createElement(Language.trPrimary("XML.Statistic.Clients.HoldingTime.Clients")));
			node.setAttribute(Language.trPrimary("XML.Statistic.GeneralAttributes.Sum"),""+kundenVerweilzeitSum);
			node.setAttribute(Language.trPrimary("XML.Statistic.GeneralAttributes.SquaresSum"),""+kundenVerweilzeitSum2);
			node.setAttribute(Language.trPrimary("XML.Statistic.GeneralAttributes.SumPerInterval"),kundenVerweilzeitSumProIntervall.storeToString());

			parent.appendChild(node=doc.createElement(Language.trPrimary("XML.Statistic.Clients.CancelTime.Clients")));
			node.setAttribute(Language.trPrimary("XML.Statistic.GeneralAttributes.Sum"),""+kundenAbbruchzeitSum);
			node.setAttribute(Language.trPrimary("XML.Statistic.GeneralAttributes.SquaresSum"),""+kundenAbbruchzeitSum2);
			node.setAttribute(Language.trPrimary("XML.Statistic.GeneralAttributes.SumPerInterval"),kundenAbbruchzeitSumProIntervall.storeToString());

			parent.appendChild(node=doc.createElement(Language.trPrimary("XML.Statistic.Clients.ServiceLevel.Clients")));
			node.setAttribute(Language.trPrimary("XML.Statistic.GeneralAttributes.Count"),""+kundenServicelevel);
			node.setAttribute(Language.trPrimary("XML.Statistic.GeneralAttributes.CountPerInterval"),kundenServicelevelProIntervall.storeToString());

			parent.appendChild(node=doc.createElement(Language.trPrimary("XML.Statistic.Clients.Forwarding.Clients")));
			node.setAttribute(Language.trPrimary("XML.Statistic.GeneralAttributes.Count"),""+kundenWeiterleitungen);
			node.setAttribute(Language.trPrimary("XML.Statistic.GeneralAttributes.CountPerInterval"),kundenWeiterleitungenProIntervall.storeToString());

			parent.appendChild(node=doc.createElement(Language.trPrimary("XML.Statistic.Clients.Retry.Clients")));
			node.setAttribute(Language.trPrimary("XML.Statistic.GeneralAttributes.Count"),""+kundenWiederholungen);
			node.setAttribute(Language.trPrimary("XML.Statistic.GeneralAttributes.CountPerInterval"),kundenWiederholungenProIntervall.storeToString());

			parent.appendChild(node=doc.createElement(Language.trPrimary("XML.Statistic.Clients.Recall.Clients")));
			node.setAttribute(Language.trPrimary("XML.Statistic.GeneralAttributes.Count"),""+kundenWiederanruf);
			node.setAttribute(Language.trPrimary("XML.Statistic.GeneralAttributes.CountPerInterval"),kundenWiederanrufProIntervall.storeToString());

			parent.appendChild(node=doc.createElement(Language.trPrimary("XML.Statistic.Clients.WaitingTimeDistribution.Clients")));
			node.setAttribute(Language.trPrimary("XML.Statistic.GeneralAttributes.Distribution"),kundenWartezeitVerteilung.storeToString());

			parent.appendChild(node=doc.createElement(Language.trPrimary("XML.Statistic.Clients.HoldingTimeDistribution.Clients")));
			node.setAttribute(Language.trPrimary("XML.Statistic.GeneralAttributes.Distribution"),kundenVerweilzeitVerteilung.storeToString());

			parent.appendChild(node=doc.createElement(Language.trPrimary("XML.Statistic.Clients.CancelTimeDistribution.Clients")));
			node.setAttribute(Language.trPrimary("XML.Statistic.GeneralAttributes.Distribution"),kundenAbbruchzeitVerteilung.storeToString());

			parent.appendChild(node=doc.createElement(Language.trPrimary("XML.Statistic.Clients.WaitingTimeDistributionLong.Clients")));
			node.setAttribute(Language.trPrimary("XML.Statistic.GeneralAttributes.Distribution"),kundenWartezeitVerteilungLang.storeToString());

			parent.appendChild(node=doc.createElement(Language.trPrimary("XML.Statistic.Clients.HoldingTimeDistributionLong.Clients")));
			node.setAttribute(Language.trPrimary("XML.Statistic.GeneralAttributes.Distribution"),kundenVerweilzeitVerteilungLang.storeToString());

			parent.appendChild(node=doc.createElement(Language.trPrimary("XML.Statistic.Clients.CancelTimeDistributionLong.Clients")));
			node.setAttribute(Language.trPrimary("XML.Statistic.GeneralAttributes.Distribution"),kundenAbbruchzeitVerteilungLang.storeToString());

			parent.appendChild(node=doc.createElement(Language.trPrimary("XML.Statistic.Calls.Count")));
			node.setAttribute(Language.trPrimary("XML.Statistic.Clients.ClientsCallsCount.Count"),""+anrufe);
			node.setAttribute(Language.trPrimary("XML.Statistic.Clients.ClientsCallsCount.Success"),""+anrufeErfolg);
			node.setAttribute(Language.trPrimary("XML.Statistic.Clients.ClientsCallsCount.Blocked"),""+anrufeBlocked);
			node.setAttribute(Language.trPrimary("XML.Statistic.Clients.ClientsCallsCount.Canceled"),""+anrufeAbbruch);
			node.setAttribute(Language.trPrimary("XML.Statistic.Clients.ClientsCallsCount.CarryOver"),""+anrufeUebertrag);
			node.setAttribute(Language.trPrimary("XML.Statistic.Clients.ClientsCallsCount.CountPerInterval"),anrufeProIntervall.storeToString());
			node.setAttribute(Language.trPrimary("XML.Statistic.Clients.ClientsCallsCount.SuccessPerInterval"),anrufeErfolgProIntervall.storeToString());
			node.setAttribute(Language.trPrimary("XML.Statistic.Clients.ClientsCallsCount.BlockedPerInterval"),anrufeBlockedProIntervall.storeToString());
			node.setAttribute(Language.trPrimary("XML.Statistic.Clients.ClientsCallsCount.CanceledPerInterval"),anrufeAbbruchProIntervall.storeToString());

			parent.appendChild(node=doc.createElement(Language.trPrimary("XML.Statistic.Clients.WaitingTime.Calls")));
			node.setAttribute(Language.trPrimary("XML.Statistic.GeneralAttributes.Sum"),""+anrufeWartezeitSum);
			node.setAttribute(Language.trPrimary("XML.Statistic.GeneralAttributes.SquaresSum"),""+anrufeWartezeitSum2);
			node.setAttribute(Language.trPrimary("XML.Statistic.GeneralAttributes.SumPerInterval"),anrufeWartezeitSumProIntervall.storeToString());

			parent.appendChild(node=doc.createElement(Language.trPrimary("XML.Statistic.Clients.HoldingTime.Calls")));
			node.setAttribute(Language.trPrimary("XML.Statistic.GeneralAttributes.Sum"),""+anrufeVerweilzeitSum);
			node.setAttribute(Language.trPrimary("XML.Statistic.GeneralAttributes.SquaresSum"),""+anrufeVerweilzeitSum2);
			node.setAttribute(Language.trPrimary("XML.Statistic.GeneralAttributes.SumPerInterval"),anrufeVerweilzeitSumProIntervall.storeToString());

			parent.appendChild(node=doc.createElement(Language.trPrimary("XML.Statistic.Clients.CancelTime.Calls")));
			node.setAttribute(Language.trPrimary("XML.Statistic.GeneralAttributes.Sum"),""+anrufeAbbruchzeitSum);
			node.setAttribute(Language.trPrimary("XML.Statistic.GeneralAttributes.SquaresSum"),""+anrufeAbbruchzeitSum2);
			node.setAttribute(Language.trPrimary("XML.Statistic.GeneralAttributes.SumPerInterval"),anrufeAbbruchzeitSumProIntervall.storeToString());

			parent.appendChild(node=doc.createElement(Language.trPrimary("XML.Statistic.Clients.ServiceLevel.Calls")));
			node.setAttribute(Language.trPrimary("XML.Statistic.GeneralAttributes.Count"),""+anrufeServicelevel);
			node.setAttribute(Language.trPrimary("XML.Statistic.GeneralAttributes.CountPerInterval"),anrufeServicelevelProIntervall.storeToString());

			parent.appendChild(node=doc.createElement(Language.trPrimary("XML.Statistic.Clients.Forwarding.Calls")));
			node.setAttribute(Language.trPrimary("XML.Statistic.GeneralAttributes.Count"),""+anrufeWeiterleitungen);
			node.setAttribute(Language.trPrimary("XML.Statistic.GeneralAttributes.CountPerInterval"),anrufeWeiterleitungenProIntervall.storeToString());

			parent.appendChild(node=doc.createElement(Language.trPrimary("XML.Statistic.Clients.Retry.Calls")));
			node.setAttribute(Language.trPrimary("XML.Statistic.GeneralAttributes.Count"),""+anrufeWiederholungen);
			node.setAttribute(Language.trPrimary("XML.Statistic.GeneralAttributes.CountPerInterval"),anrufeWiederholungenProIntervall.storeToString());

			parent.appendChild(node=doc.createElement(Language.trPrimary("XML.Statistic.Clients.WaitingTimeDistribution.Calls")));
			node.setAttribute(Language.trPrimary("XML.Statistic.GeneralAttributes.Distribution"),anrufeWartezeitVerteilung.storeToString());

			parent.appendChild(node=doc.createElement(Language.trPrimary("XML.Statistic.Clients.HoldingTimeDistribution.Calls")));
			node.setAttribute(Language.trPrimary("XML.Statistic.GeneralAttributes.Distribution"),anrufeVerweilzeitVerteilung.storeToString());

			parent.appendChild(node=doc.createElement(Language.trPrimary("XML.Statistic.Clients.CancelTimeDistribution.Calls")));
			node.setAttribute(Language.trPrimary("XML.Statistic.GeneralAttributes.Distribution"),anrufeAbbruchzeitVerteilung.storeToString());

			parent.appendChild(node=doc.createElement(Language.trPrimary("XML.Statistic.Clients.WaitingTimeDistributionLong.Calls")));
			node.setAttribute(Language.trPrimary("XML.Statistic.GeneralAttributes.Distribution"),anrufeWartezeitVerteilungLang.storeToString());

			parent.appendChild(node=doc.createElement(Language.trPrimary("XML.Statistic.Clients.HoldingTimeDistributionLong.Calls")));
			node.setAttribute(Language.trPrimary("XML.Statistic.GeneralAttributes.Distribution"),anrufeVerweilzeitVerteilungLang.storeToString());

			parent.appendChild(node=doc.createElement(Language.trPrimary("XML.Statistic.Clients.CancelTimeDistributionLong.Calls")));
			node.setAttribute(Language.trPrimary("XML.Statistic.GeneralAttributes.Distribution"),anrufeAbbruchzeitVerteilungLang.storeToString());

			double[] c;
			parent.appendChild(node=doc.createElement(Language.trPrimary("XML.Statistic.Confidence")));

			node.appendChild(node2=doc.createElement(Language.trPrimary("XML.Statistic.Confidence.Accessibility.Calls")));
			saveConfidencePercentValue(node2,interDaySuccessCallsSum,interDaySuccessCallsSum2);

			node.appendChild(node2=doc.createElement(Language.trPrimary("XML.Statistic.Confidence.Accessibility.Clients")));
			saveConfidencePercentValue(node2,interDaySuccessClientsSum,interDaySuccessClientsSum2);

			node.appendChild(node2=doc.createElement(Language.trPrimary("XML.Statistic.Confidence.WaitingTime")));
			node2.setAttribute(Language.trPrimary("XML.Statistic.GeneralAttributes.Sum"),NumberTools.formatSystemNumber(interDayWartezeitSum));
			node2.setAttribute(Language.trPrimary("XML.Statistic.GeneralAttributes.SquaresSum"),NumberTools.formatSystemNumber(interDayWartezeitSum2));
			c=calcConfidence(interDayWartezeitSum2,interDayWartezeitSum,simulationData.runRepeatCount,0.9);
			node2.setAttribute(Language.trPrimary("XML.Statistic.GeneralAttributes.Confidence90Min"),TimeTools.formatExactSystemTime(c[0]));
			node2.setAttribute(Language.trPrimary("XML.Statistic.GeneralAttributes.Confidence90Max"),TimeTools.formatExactSystemTime(c[1]));
			c=calcConfidence(interDayWartezeitSum2,interDayWartezeitSum,simulationData.runRepeatCount,0.95);
			node2.setAttribute(Language.trPrimary("XML.Statistic.GeneralAttributes.Confidence95Min"),TimeTools.formatExactSystemTime(c[0]));
			node2.setAttribute(Language.trPrimary("XML.Statistic.GeneralAttributes.Confidence95Max"),TimeTools.formatExactSystemTime(c[1]));

			node.appendChild(node2=doc.createElement(Language.trPrimary("XML.Statistic.Confidence.ServiceLevel.CallsSuccess")));
			saveConfidencePercentValue(node2,interDayServiceLevelCallsSuccessSum,interDayServiceLevelCallsSuccessSum2);

			node.appendChild(node2=doc.createElement(Language.trPrimary("XML.Statistic.Confidence.ServiceLevel.CallsAll")));
			saveConfidencePercentValue(node2,interDayServiceLevelCallsAllSum,interDayServiceLevelCallsAllSum2);

			node.appendChild(node2=doc.createElement(Language.trPrimary("XML.Statistic.Confidence.ServiceLevel.ClientsSuccess")));
			saveConfidencePercentValue(node2,interDayServiceLevelClientsSuccessSum,interDayServiceLevelClientsSuccessSum2);

			node.appendChild(node2=doc.createElement(Language.trPrimary("XML.Statistic.Confidence.ServiceLevel.ClientsAll")));
			saveConfidencePercentValue(node2,interDayServiceLevelClientsAllSum,interDayServiceLevelClientsAllSum2);

			parent.appendChild(node=doc.createElement(Language.trPrimary("XML.Statistic.Costs")));
			node.setAttribute(Language.trPrimary("XML.Statistic.Costs.Yield"),NumberTools.formatSystemNumber(revenue));
			node.setAttribute(Language.trPrimary("XML.Statistic.Costs.WaitingTimes"),NumberTools.formatSystemNumber(costWaiting));
			node.setAttribute(Language.trPrimary("XML.Statistic.Costs.Cancelations"),NumberTools.formatSystemNumber(costCancel));
		}

		/**
		 * Speichert die Daten in einem XML-Element
		 * @param parent	Übergeordnetes Element für das neue XML-Element
		 */
		private void saveToXML(final Element parent) {
			Document doc=parent.getOwnerDocument();
			Element node; parent.appendChild(node=doc.createElement(Language.trPrimary("XML.Statistic.Clients")));

			if (!name.isEmpty()) node.setAttribute(Language.trPrimary("XML.Statistic.GeneralAttributes.Name"),name);
			saveDataToXML(node);
		}
	}

	/**
	 * Hält die Statistikdaten für eine Agentengruppe vor.
	 * @author Alexander Herzog
	 * @see Statistics#agentenGlobal
	 * @see Statistics#agentenProCallcenter
	 * @see Statistics#agentenProSkilllevel
	 */
	public final class AgentenDaten {
		/** Name der Agentengruppe */
		public String name;
		/** Skill-Level */
		public String type;

		/** Anzahl an Agenten in dieser Gruppe */
		public int anzahlAgenten;

		/** Gesamtzeit der Agenten im Leerlauf */
		public long leerlaufGesamt;
		/** Gesamtzeit der Agenten in technischer Bereitzeit */
		public long technischerLeerlaufGesamt;
		/** Gesamte Bedienzeit der Agenten */
		public long arbeitGesamt;
		/** Gesamte Nachbearbeitungszeit der Agenten */
		public long postProcessingGesamt;
		/** Leerlaufzeit der Agenten pro Intervall */
		public DataDistributionImpl leerlaufProIntervall=new DataDistributionImpl(48,48);
		/** Technische Bereitzeit der Agenten pro Intervall */
		public DataDistributionImpl technischerLeerlaufProIntervall=new DataDistributionImpl(48,48);
		/** Bedienzeit der Agenten pro Intervall */
		public DataDistributionImpl arbeitProIntervall=new DataDistributionImpl(48,48);
		/** Nachbearbeitungszeit der Agenten pro Intervall */
		public DataDistributionImpl postProcessingProIntervall=new DataDistributionImpl(48,48);

		/** Name der Kundentypen für die Erfassung von Kenngrößen auf Kundentypbasis (innerhalb der Agentengruppe) */
		public String[] dataByCaller;
		/** Technische Bereitzeit nach Kundentypen */
		public long[] dataByCallerTechnial;
		/** Technische Bereitzeit nach Kundentypen und pro Intervall */
		public DataDistributionImpl[] dataByCallerTechnialProIntervall;
		/** Bedienzeit nach Kundentypen */
		public long[] dataByCallerService;
		/** Bedienzeit nach Kundentypen und pro Intervall */
		public DataDistributionImpl[] dataByCallerServiceProIntervall;
		/** Nachbearbeitungszeit nach Kundentypen */
		public long[] dataByCallerPostProcessing;
		/** Nachbearbeitungszeit nach Kundentypen und pro Intervall */
		public DataDistributionImpl[] dataByCallerPostProcessingProIntervall;

		/** Gesamtanzahl an bedienten Anrufen */
		public long anzahlAnrufeGesamt;
		/** Anzahl an bedienten Anrufen pro Intervall */
		public DataDistributionImpl anzahlAnrufeProIntervall=new DataDistributionImpl(48,48);
		/** Anzahl an bedienten Anrufen pro Kundengruppe */
		public long[] dataByCallerAnzahlAnrufe;
		/** Anzahl an bedienten Anrufen pro Kundengruppe und pro Intervall */
		public DataDistributionImpl[] dataByCallerAnzahlAnrufeProIntervall;

		/** Kosten durch Anwesenheitszeit */
		public double costOfficeTime;
		/** Kosten durch bediente Anrufe (Kosten pro Anruf) */
		public double costCalls;
		/** Kosten durch die Gesprächszeiten */
		public double costProcessTime;

		/**
		 * Konstruktor der Klasse
		 * @param type	Skill-Level
		 * @param name	Name der Agentengruppe
		 * @param caller	Zugehörige Anrufer-Datensätze
		 */
		private AgentenDaten(final String type, final String name, final CallcenterRunModelCaller[] caller) {
			this.type=type;
			this.name=name;

			if (caller!=null) {
				dataByCaller=new String[caller.length];
				dataByCallerTechnial=new long[caller.length];
				dataByCallerService=new long[caller.length];
				dataByCallerPostProcessing=new long[caller.length];
				dataByCallerAnzahlAnrufe=new long[caller.length];
				dataByCallerTechnialProIntervall=new DataDistributionImpl[caller.length];
				dataByCallerServiceProIntervall=new DataDistributionImpl[caller.length];
				dataByCallerPostProcessingProIntervall=new DataDistributionImpl[caller.length];
				dataByCallerAnzahlAnrufeProIntervall=new DataDistributionImpl[caller.length];
				for (int i=0;i<caller.length;i++) {
					dataByCaller[i]=caller[i].name;
					dataByCallerTechnialProIntervall[i]=new DataDistributionImpl(48,48);
					dataByCallerServiceProIntervall[i]=new DataDistributionImpl(48,48);
					dataByCallerPostProcessingProIntervall[i]=new DataDistributionImpl(48,48);
					dataByCallerAnzahlAnrufeProIntervall[i]=new DataDistributionImpl(48,48);
				}
			}
		}

		/**
		 * Konstruktor der Klasse
		 * @param type	Skill-Level
		 * @param caller	Zugehörige Anrufer-Datensätze
		 */
		private AgentenDaten(final String type, final CallcenterRunModelCaller[] caller) {
			this(type,"",caller);
		}

		/**
		 * Fügt Daten eines weiteren Agenten-Datensatzes zu diesem hinzu.
		 * @param data	Weiterer Agenten-Datensatz
		 */
		private void addData(final AgentenDaten data) {
			anzahlAgenten=data.anzahlAgenten; /* kein += hier */
			leerlaufGesamt+=data.leerlaufGesamt;
			technischerLeerlaufGesamt+=data.technischerLeerlaufGesamt;
			arbeitGesamt+=data.arbeitGesamt;
			postProcessingGesamt+=data.postProcessingGesamt;
			anzahlAnrufeGesamt+=data.anzahlAnrufeGesamt;

			addDensity(leerlaufProIntervall,data.leerlaufProIntervall);
			addDensity(technischerLeerlaufProIntervall,data.technischerLeerlaufProIntervall);
			addDensity(arbeitProIntervall,data.arbeitProIntervall);
			addDensity(postProcessingProIntervall,data.postProcessingProIntervall);
			addDensity(anzahlAnrufeProIntervall,data.anzahlAnrufeProIntervall);

			for (int i=0;i<dataByCaller.length;i++) {
				dataByCallerTechnial[i]+=data.dataByCallerTechnial[i];
				addDensity(dataByCallerTechnialProIntervall[i],data.dataByCallerTechnialProIntervall[i]);
				dataByCallerService[i]+=data.dataByCallerService[i];
				addDensity(dataByCallerServiceProIntervall[i],data.dataByCallerServiceProIntervall[i]);
				dataByCallerPostProcessing[i]+=data.dataByCallerPostProcessing[i];
				addDensity(dataByCallerPostProcessingProIntervall[i],data.dataByCallerPostProcessingProIntervall[i]);
				dataByCallerAnzahlAnrufe[i]+=data.dataByCallerAnzahlAnrufe[i];
				addDensity(dataByCallerAnzahlAnrufeProIntervall[i],data.dataByCallerAnzahlAnrufeProIntervall[i]);
			}

			costOfficeTime+=data.costOfficeTime;
			costCalls+=data.costCalls;
			costProcessTime+=data.costProcessTime;
		}

		/**
		 * Lädt die kundenspezifischen Agenten-Daten aus einem XML-Element
		 * @param node	XML-Element aus dem die Daten geladen werden sollen
		 * @return	Liefert im Erfolgsfall <code>null</code> sonst eine Fehlermeldung
		 */
		private String loadClientDataFromXML(final Element node) {
			Long K;
			String s;

			s=Language.trAllAttribute("XML.Statistic.Agents.Summary.TechnicalFreeTime",node);
			K=NumberTools.getNotNegativeLong(NumberTools.systemNumberToLocalNumber(s));
			if (K==null) return String.format(Language.tr("XML.Statistic.Agents.Summary.TechnicalFreeTime.ErrorPerClientType"),s);
			dataByCallerTechnial=Arrays.copyOf(dataByCallerTechnial,dataByCallerTechnial.length+1);
			dataByCallerTechnial[dataByCallerTechnial.length-1]=K;

			s=Language.trAllAttribute("XML.Statistic.Agents.Summary.HoldingTime",node);
			K=NumberTools.getNotNegativeLong(NumberTools.systemNumberToLocalNumber(s));
			if (K==null) return String.format(Language.tr("XML.Statistic.Agents.Summary.HoldingTime.ErrorPerClientType"),s);
			dataByCallerService=Arrays.copyOf(dataByCallerService,dataByCallerService.length+1);
			dataByCallerService[dataByCallerService.length-1]=K;

			s=Language.trAllAttribute("XML.Statistic.Agents.Summary.PostProcessingTime",node);
			K=NumberTools.getNotNegativeLong(NumberTools.systemNumberToLocalNumber(s));
			if (K==null) return String.format(Language.tr("XML.Statistic.Agents.Summary.PostProcessingTime.ErrorPerClientType"),s);
			dataByCallerPostProcessing=Arrays.copyOf(dataByCallerPostProcessing,dataByCallerPostProcessing.length+1);
			dataByCallerPostProcessing[dataByCallerPostProcessing.length-1]=K;

			s=Language.trAllAttribute("XML.Statistic.Agents.Summary.Calls",node);
			K=NumberTools.getNotNegativeLong(NumberTools.systemNumberToLocalNumber(s));
			if (K==null) return String.format(Language.tr("XML.Statistic.Agents.Summary.Calls.ErrorPerClientType"),s);
			dataByCallerAnzahlAnrufe=Arrays.copyOf(dataByCallerAnzahlAnrufe,dataByCallerAnzahlAnrufe.length+1);
			dataByCallerAnzahlAnrufe[dataByCallerAnzahlAnrufe.length-1]=K;

			DataDistributionImpl d;

			s=Language.trAllAttribute("XML.Statistic.Agents.Summary.TechnicalFreeTimePerInterval",node);
			d=DataDistributionImpl.createFromString(s,technischerLeerlaufProIntervall.upperBound);
			if (d==null) return Language.tr("XML.Statistic.Agents.Summary.TechnicalFreeTimePerInterval.ErrorPerClientType");
			dataByCallerTechnialProIntervall=Arrays.copyOf(dataByCallerTechnialProIntervall,dataByCallerTechnialProIntervall.length+1);
			dataByCallerTechnialProIntervall[dataByCallerTechnialProIntervall.length-1]=d;

			s=Language.trAllAttribute("XML.Statistic.Agents.Summary.HoldingTimePerInterval",node);
			d=DataDistributionImpl.createFromString(s,arbeitProIntervall.upperBound);
			if (d==null) return Language.tr("XML.Statistic.Agents.Summary.HoldingTimePerInterval.ErrorPerClientType");
			dataByCallerServiceProIntervall=Arrays.copyOf(dataByCallerServiceProIntervall,dataByCallerServiceProIntervall.length+1);
			dataByCallerServiceProIntervall[dataByCallerServiceProIntervall.length-1]=d;

			s=Language.trAllAttribute("XML.Statistic.Agents.Summary.PostProcessingTimePerInterval",node);
			d=DataDistributionImpl.createFromString(s,postProcessingProIntervall.upperBound);
			if (d==null) return Language.tr("XML.Statistic.Agents.Summary.PostProcessingTimePerInterval.ErrorPerClientType");
			dataByCallerPostProcessingProIntervall=Arrays.copyOf(dataByCallerPostProcessingProIntervall,dataByCallerPostProcessingProIntervall.length+1);
			dataByCallerPostProcessingProIntervall[dataByCallerPostProcessingProIntervall.length-1]=d;

			s=Language.trAllAttribute("XML.Statistic.Agents.Summary.CallsPerInterval",node);
			d=DataDistributionImpl.createFromString(s,anzahlAnrufeProIntervall.upperBound);
			if (d==null) return Language.tr("XML.Statistic.Agents.Summary.CallsPerInterval.ErrorPerClientType");
			dataByCallerAnzahlAnrufeProIntervall=Arrays.copyOf(dataByCallerAnzahlAnrufeProIntervall,dataByCallerAnzahlAnrufeProIntervall.length+1);
			dataByCallerAnzahlAnrufeProIntervall[dataByCallerAnzahlAnrufeProIntervall.length-1]=d;

			return null;
		}

		/**
		 * Versucht einen Agenten-Datensatz aus dem übergebenen XML-Node zu laden
		 * @param node	XML-Knoten, der die Agenten-Daten enthält
		 * @return	Tritt ein Fehler auf, so wird die Fehlermeldung als String zurückgegeben. Im Erfolgsfall wird <code>null</code> zurückgegeben.
		 */
		private String loadFromXML(final Element node) {
			dataByCaller=new String[0];
			dataByCallerTechnial=new long[0];
			dataByCallerService=new long[0];
			dataByCallerPostProcessing=new long[0];
			dataByCallerAnzahlAnrufe=new long[0];
			dataByCallerTechnialProIntervall=new DataDistributionImpl[0];
			dataByCallerServiceProIntervall=new DataDistributionImpl[0];
			dataByCallerPostProcessingProIntervall=new DataDistributionImpl[0];
			dataByCallerAnzahlAnrufeProIntervall=new DataDistributionImpl[0];

			type=Language.trAllAttribute("XML.Statistic.GeneralAttributes.Type",node);
			if (type.isEmpty()) name=""; else name=Language.trAllAttribute("XML.Statistic.GeneralAttributes.Name",node);
			Integer J;
			String u;
			u=Language.trAllAttribute("XML.Statistic.GeneralAttributes.Count",node);
			J=NumberTools.getNotNegativeInteger(NumberTools.systemNumberToLocalNumber(u));
			if (J!=null) anzahlAgenten=J; else return String.format(Language.tr("XML.Statistic.Agents.ErrorCount"),u);

			NodeList l=node.getChildNodes();
			for (int i=0; i<l.getLength();i++) {
				if (!(l.item(i) instanceof Element)) continue;
				Element e=(Element)l.item(i);
				String s=e.getNodeName();

				if (Language.trAll("XML.Statistic.Agents.Summary",s)) {
					Long K;
					u=Language.trAllAttribute("XML.Statistic.Agents.Summary.FreeTime",e);
					K=NumberTools.getNotNegativeLong(NumberTools.systemNumberToLocalNumber(u));
					if (K!=null) leerlaufGesamt=K; else return String.format(Language.tr("XML.Statistic.Agents.Summary.FreeTime.Error"),u);
					u=Language.trAllAttribute("XML.Statistic.Agents.Summary.TechnicalFreeTime",e);
					K=NumberTools.getNotNegativeLong(NumberTools.systemNumberToLocalNumber(u));
					if (K!=null) technischerLeerlaufGesamt=K; else return String.format(Language.tr("XML.Statistic.Agents.Summary.TechnicalFreeTime.Error"),u);
					u=Language.trAllAttribute("XML.Statistic.Agents.Summary.HoldingTime",e);
					K=NumberTools.getNotNegativeLong(NumberTools.systemNumberToLocalNumber(u));
					if (K!=null) arbeitGesamt=K; else return String.format(Language.tr("XML.Statistic.Agents.Summary.HoldingTime.Error"),u);
					u=Language.trAllAttribute("XML.Statistic.Agents.Summary.PostProcessingTime",e);
					K=NumberTools.getNotNegativeLong(NumberTools.systemNumberToLocalNumber(u));
					if (K!=null) postProcessingGesamt=K; else return String.format(Language.tr("XML.Statistic.Agents.Summary.PostProcessingTime.Error"),u);

					u=Language.trAllAttribute("XML.Statistic.Agents.Summary.Calls",e);
					K=NumberTools.getNotNegativeLong(NumberTools.systemNumberToLocalNumber(u));
					if (K!=null) anzahlAnrufeGesamt=K; else return String.format(Language.tr("XML.Statistic.Agents.Summary.Calls.Error"),u);

					u=Language.trAllAttribute("XML.Statistic.Agents.Summary.FreeTimePerInterval",e);
					leerlaufProIntervall=DataDistributionImpl.createFromString(u,leerlaufProIntervall.upperBound);
					if (leerlaufProIntervall==null) return Language.tr("XML.Statistic.Agents.Summary.FreeTimePerInterval.Error");
					u=Language.trAllAttribute("XML.Statistic.Agents.Summary.TechnicalFreeTimePerInterval",e);
					technischerLeerlaufProIntervall=DataDistributionImpl.createFromString(u,technischerLeerlaufProIntervall.upperBound);
					if (technischerLeerlaufProIntervall==null) return Language.tr("XML.Statistic.Agents.Summary.TechnicalFreeTimePerInterval.Error");
					u=Language.trAllAttribute("XML.Statistic.Agents.Summary.HoldingTimePerInterval",e);
					arbeitProIntervall=DataDistributionImpl.createFromString(u,arbeitProIntervall.upperBound);
					if (arbeitProIntervall==null) return Language.tr("XML.Statistic.Agents.Summary.HoldingTimePerInterval.Error");
					u=Language.trAllAttribute("XML.Statistic.Agents.Summary.PostProcessingTimePerInterval",e);
					postProcessingProIntervall=DataDistributionImpl.createFromString(u,postProcessingProIntervall.upperBound);
					if (postProcessingProIntervall==null) return Language.tr("XML.Statistic.Agents.Summary.PostProcessingTimePerInterval.Error");

					u=Language.trAllAttribute("XML.Statistic.Agents.Summary.CallsPerInterval",e);
					anzahlAnrufeProIntervall=DataDistributionImpl.createFromString(u,anzahlAnrufeProIntervall.upperBound);
					if (anzahlAnrufeProIntervall==null) return Language.tr("XML.Statistic.Agents.Summary.CallsPerInterval.Error");

					continue;
				}

				if (Language.trAll("XML.Statistic.Agents.ClientType",s)) {
					String t=Language.trAllAttribute("XML.Statistic.GeneralAttributes.Name",e);
					if (t.isEmpty()) continue;
					dataByCaller=Arrays.copyOf(dataByCaller,dataByCaller.length+1);
					dataByCaller[dataByCaller.length-1]=t;
					t=loadClientDataFromXML(e);
					if (t!=null) return t;

					continue;
				}

				if (Language.trAll("XML.Statistic.Agents.Costs",s)) {
					String t;
					t=Language.trAllAttribute("XML.Statistic.Agents.Costs.Wages",e);
					Double d=NumberTools.getNotNegativeDouble(t);
					if (d!=null) costOfficeTime=d; else return String.format(Language.tr("XML.Statistic.Agents.Costs.Wages.Error"),t);
					t=Language.trAllAttribute("XML.Statistic.Agents.Costs.Calls",e);
					d=NumberTools.getNotNegativeDouble(t);
					if (d!=null) costCalls=d; else return String.format(Language.tr("XML.Statistic.Agents.Costs.Calls.Error"),t);
					t=Language.trAllAttribute("XML.Statistic.Agents.Costs.CallTimes",e);
					d=NumberTools.getNotNegativeDouble(t);
					if (d!=null) costProcessTime=d; else return String.format(Language.tr("XML.Statistic.Agents.Costs.CallTimes.Error"),t);
					continue;
				}
			}
			return null;
		}

		/**
		 * Berechnet die in dem Callcenter-Modell vorhandene Agentenarbeitsleistung
		 * @param model	Callcenter-Modell aus dem die Agentendaten ausgelesen werden sollen
		 * @param useProductivity	Sollen die Effizient-Angaben berücksichtigt werden (<code>true</code>), d.h. Netto-Agenten ausgegeben werden, oder nicht (<code>false</code>), d.h. Brutto-Agenten
		 * @return	Agenten-Arbeitsleistung im Gesamtmodell
		 */
		public double getAgentsTimes(final CallcenterModel model, final boolean useProductivity) {
			String callcenterName="";
			String skillLevelName="";

			if (type!=null && name!=null) {
				if (Language.trAll("XML.Statistic.SkillLevel",type)) skillLevelName=name;
				if (Language.trAll("XML.Statistic.CallCenter",type)) callcenterName=name;
			}

			/* Bei Bedarf Schichtpläne erstellen */
			List<CallcenterModelAgent> translatedAgents=new ArrayList<>();
			for (int i=0;i<model.callcenter.size();i++) {
				CallcenterModelCallcenter c=model.callcenter.get(i);
				if (!c.active) continue;
				if (callcenterName!=null && !callcenterName.isEmpty() && !callcenterName.equals(c.name)) continue;
				for (int j=0;j<c.agents.size();j++) {
					CallcenterModelAgent a=c.agents.get(j);
					if (!a.active) continue;
					if (skillLevelName!=null && !skillLevelName.isEmpty() && !skillLevelName.equals(a.skillLevel)) continue;
					translatedAgents.addAll(c.agents.get(j).calcAgentShifts(false,c,model,useProductivity));
				}
			}

			/* Daten zusammenstellen */
			double time=0;
			for (int i=0;i<translatedAgents.size();i++) {
				CallcenterModelAgent a=translatedAgents.get(i);
				int start=a.workingTimeStart;
				int end=((a.workingNoEndTime)?86400:a.workingTimeEnd)+1;
				time+=(end-start)*a.count;
			}
			return time;
		}

		/**
		 * Versucht einen Agenten-Datensatz in einem XML-Knoten zu speichern
		 * @param parent Übergeordneter XML-Knoten
		 */
		private void saveDataToXML(final Element parent) {
			final Document doc=parent.getOwnerDocument();
			Element node;

			parent.appendChild(node=doc.createElement(Language.trPrimary("XML.Statistic.Agents.Summary")));
			node.setAttribute(Language.trPrimary("XML.Statistic.Agents.Summary.FreeTime"),""+leerlaufGesamt);
			node.setAttribute(Language.trPrimary("XML.Statistic.Agents.Summary.TechnicalFreeTime"),""+technischerLeerlaufGesamt);
			node.setAttribute(Language.trPrimary("XML.Statistic.Agents.Summary.HoldingTime"),""+arbeitGesamt);
			node.setAttribute(Language.trPrimary("XML.Statistic.Agents.Summary.PostProcessingTime"),""+postProcessingGesamt);
			node.setAttribute(Language.trPrimary("XML.Statistic.Agents.Summary.Calls"),""+anzahlAnrufeGesamt);

			double d;
			long sum=leerlaufGesamt+technischerLeerlaufGesamt+arbeitGesamt+postProcessingGesamt;
			if (sum<=0) d=0; else d=((double)leerlaufGesamt)/((double)sum);
			node.setAttribute(Language.trPrimary("XML.Statistic.Agents.Summary.FreeTimePart"),NumberTools.formatSystemNumber(d*100)+"%");
			if (sum<=0) d=0; else d=((double)technischerLeerlaufGesamt)/((double)sum);
			node.setAttribute(Language.trPrimary("XML.Statistic.Agents.Summary.TechnicalFreeTimePart"),NumberTools.formatSystemNumber(d*100)+"%");
			if (sum<=0) d=0; else d=((double)arbeitGesamt)/((double)sum);
			node.setAttribute(Language.trPrimary("XML.Statistic.Agents.Summary.HoldingTimePart"),NumberTools.formatSystemNumber(d*100)+"%");
			if (sum<=0) d=0; else d=((double)postProcessingGesamt)/((double)sum);
			node.setAttribute(Language.trPrimary("XML.Statistic.Agents.Summary.PostProcessingTimePart"),NumberTools.formatSystemNumber(d*100)+"%");

			node.setAttribute(Language.trPrimary("XML.Statistic.Agents.Summary.GrossTime"),NumberTools.formatSystemNumber(getAgentsTimes(editModel,false)*simulationData.runRepeatCount));
			node.setAttribute(Language.trPrimary("XML.Statistic.Agents.Summary.NetTime"),NumberTools.formatSystemNumber(getAgentsTimes(editModel,true)*simulationData.runRepeatCount));

			node.setAttribute(Language.trPrimary("XML.Statistic.Agents.Summary.FreeTimePerInterval"),leerlaufProIntervall.storeToString());
			node.setAttribute(Language.trPrimary("XML.Statistic.Agents.Summary.TechnicalFreeTimePerInterval"),technischerLeerlaufProIntervall.storeToString());
			node.setAttribute(Language.trPrimary("XML.Statistic.Agents.Summary.HoldingTimePerInterval"),arbeitProIntervall.storeToString());
			node.setAttribute(Language.trPrimary("XML.Statistic.Agents.Summary.PostProcessingTimePerInterval"),postProcessingProIntervall.storeToString());
			node.setAttribute(Language.trPrimary("XML.Statistic.Agents.Summary.CallsPerInterval"),anzahlAnrufeProIntervall.storeToString());

			for (int i=0;i<dataByCaller.length;i++) {
				parent.appendChild(node=doc.createElement(Language.trPrimary("XML.Statistic.Agents.ClientType")));
				node.setAttribute(Language.trPrimary("XML.Statistic.GeneralAttributes.Name"),dataByCaller[i]);

				node.setAttribute(Language.trPrimary("XML.Statistic.Agents.Summary.TechnicalFreeTime"),""+dataByCallerTechnial[i]);
				node.setAttribute(Language.trPrimary("XML.Statistic.Agents.Summary.TechnicalFreeTimePerInterval"),dataByCallerTechnialProIntervall[i].storeToString());
				node.setAttribute(Language.trPrimary("XML.Statistic.Agents.Summary.HoldingTime"),""+dataByCallerService[i]);
				node.setAttribute(Language.trPrimary("XML.Statistic.Agents.Summary.HoldingTimePerInterval"),dataByCallerServiceProIntervall[i].storeToString());
				node.setAttribute(Language.trPrimary("XML.Statistic.Agents.Summary.PostProcessingTime"),""+dataByCallerPostProcessing[i]);
				node.setAttribute(Language.trPrimary("XML.Statistic.Agents.Summary.PostProcessingTimePerInterval"),dataByCallerPostProcessingProIntervall[i].storeToString());
				node.setAttribute(Language.trPrimary("XML.Statistic.Agents.Summary.Calls"),""+dataByCallerAnzahlAnrufe[i]);
				node.setAttribute(Language.trPrimary("XML.Statistic.Agents.Summary.CallsPerInterval"),dataByCallerAnzahlAnrufeProIntervall[i].storeToString());
			}

			parent.appendChild(node=doc.createElement(Language.trPrimary("XML.Statistic.Agents.Costs")));
			node.setAttribute(Language.trPrimary("XML.Statistic.Agents.Costs.Wages"),NumberTools.formatNumberMax(costOfficeTime));
			node.setAttribute(Language.trPrimary("XML.Statistic.Agents.Costs.Calls"),NumberTools.formatNumberMax(costCalls));
			node.setAttribute(Language.trPrimary("XML.Statistic.Agents.Costs.CallTimes"),NumberTools.formatNumberMax(costProcessTime));
		}

		/**
		 * Speichert die Daten in einem XML-Element
		 * @param parent	Übergeordnetes Element für das neue XML-Element
		 */
		private void saveToXML(final Element parent) {
			Document doc=parent.getOwnerDocument();
			Element node;
			parent.appendChild(node=doc.createElement(Language.trPrimary("XML.Statistic.Agents")));

			if (!type.isEmpty()) {
				node.setAttribute(Language.trPrimary("XML.Statistic.GeneralAttributes.Type"),type);
				if (!name.isEmpty()) node.setAttribute(Language.trPrimary("XML.Statistic.GeneralAttributes.Name"),name);
			}
			node.setAttribute(Language.trPrimary("XML.Statistic.GeneralAttributes.Count"),""+anzahlAgenten);
			saveDataToXML(node);
		}
	}

	/**
	 * Erstellt aus Basis eines Callcenter-Modells die Modell-Agenten
	 * @param model	Callcenter-Modell
	 * @return	Modell-Agenten
	 */
	private AgentModelData[] buildAgentModelData(final CallcenterModel model) {
		List<AgentModelData> list=new ArrayList<>();

		for (int i=0;i<model.callcenter.size();i++) {
			CallcenterModelCallcenter callcenter=model.callcenter.get(i);
			if (!callcenter.active) continue;
			for (int j=0;j<callcenter.agents.size();j++) {
				CallcenterModelAgent agent=callcenter.agents.get(j);
				if (!agent.active) continue;

				if (agent.count>0) {
					list.add(new AgentModelData(callcenter.name,new CallcenterModelAgent[]{agent},j,callcenter,model));
				} else {
					list.add(new AgentModelData(callcenter.name,agent.calcAgentShifts(agent.lastShiftIsOpenEnd,callcenter,model,false).toArray(new CallcenterModelAgent[0]),j,callcenter,model));
				}
			}
		}

		return list.toArray(new AgentModelData[0]);
	}

	/**
	 * Erstellt den Agentengruppennamen aus Callcentername und Nummer der Agentengruppe
	 * @param callcenterName	Callcentername
	 * @param groupNr	0-basierte Nummer der Agentengruppe innerhalb des Callcenters
	 * @return	Vollständiger Name der Agentengruppe
	 */
	public static String getAgentModelDataGroupName(String callcenterName, int groupNr) {
		return callcenterName+" - "+Language.tr("SimStatistic.AgentGroup")+" "+(groupNr+1);
	}

	/**
	 * Hält die Statistikdaten für eine Agentengruppe aus Modell-Sicht vor.
	 * @author Alexander Herzog
	 * @see Statistics#agentenModellGlobal
	 * @see Statistics#agentenModellProGruppe
	 */
	public static final class AgentModelData {
		/** Name der Agentengruppe */
		public String name;
		/** Skill-Level */
		public String type;

		/** Agentenzahlen gemäß Modell (Agenten für die Simulation) */
		public DataDistributionImpl simAgents=new DataDistributionImpl(48,48);
		/** Agentenzahlen gemäß Modell (Agenten aus dem Modell) */
		public DataDistributionImpl modelAgents=new DataDistributionImpl(48,48);
		/** Agentenzahlen gemäß Modell (Agenten inkl. Zuschlag) */
		public DataDistributionImpl fullAgents=new DataDistributionImpl(48,48);

		/**
		 * Konstruktor der Klasse
		 */
		private AgentModelData() {
			this(null,null,null,null,null);
		}

		/**
		 * Konstruktor der Klasse
		 * @param name	Name der Agentengruppe
		 * @param type	Skill-Level
		 * @param simAgents	Agentenzahlen gemäß Modell (Agenten für die Simulation)
		 * @param modelAgents	Agentenzahlen gemäß Modell (Agenten aus dem Modell)
		 * @param fullAgents	Agentenzahlen gemäß Modell (Agenten inkl. Zuschlag)
		 */
		private AgentModelData(String name, String type, DataDistributionImpl simAgents, DataDistributionImpl modelAgents, DataDistributionImpl fullAgents) {
			this.name=(name!=null)?name:"";
			this.type=(type!=null)?type:"";

			if (simAgents!=null) this.simAgents=simAgents.clone();
			if (modelAgents!=null) this.modelAgents=modelAgents.clone();
			if (fullAgents!=null) this.fullAgents=fullAgents.clone();
		}

		/**
		 * Konstruktor der Klasse
		 * @param callcenterName	Name des Callcenters in dem die Agentengruppe zum Einsatz kommt
		 * @param simpleAgentGroups	Agenten in der Gruppe
		 * @param groupNr	Index der Agentengruppe in dem Callcenter
		 * @param callcenter	Callcenter in dem die Agentengruppe zum Einsatz kommt
		 * @param model	Callcenter-Modell
		 */
		private AgentModelData(final String callcenterName, final CallcenterModelAgent[] simpleAgentGroups, final int groupNr, final CallcenterModelCallcenter callcenter, final CallcenterModel model) {
			name=getAgentModelDataGroupName(callcenterName,groupNr);
			type=simpleAgentGroups[0].skillLevel;

			for (int i=0;i<simpleAgentGroups.length;i++) {
				int start=simpleAgentGroups[i].workingTimeStart;
				int end=(simpleAgentGroups[i].workingNoEndTime)?86400:simpleAgentGroups[i].workingTimeEnd;

				for (int j=0;j<48;j++) {
					int start2=Math.max(j*1800,start);
					int end2=Math.min((j+1)*1800,end);
					modelAgents.densityData[j]+=((double)(Math.max(end2-start2,0)))/1800*simpleAgentGroups[i].count;
				}

				DataDistributionImpl efficiency=simpleAgentGroups[i].getEfficiency(callcenter,model);
				DataDistributionImpl addition=simpleAgentGroups[i].getAddition(callcenter,model);

				simAgents=modelAgents.multiply(efficiency).round();
				fullAgents=modelAgents.multiply(addition).round();
			}
		}

		/**
		 * Konstruktor der Klasse<br>
		 * Überträge Daten aus mehreren anderen Objekten in dieses
		 * @param agents	Quell-Objekte aus denen die Agenten-Daten in dieses Objekt übernommen werden sollen
		 */
		private AgentModelData(final AgentModelData[] agents) {
			name="";
			type="";

			for (int i=0;i<agents.length;i++) {
				simAgents=simAgents.add(agents[i].simAgents);
				modelAgents=modelAgents.add(agents[i].modelAgents);
				fullAgents=fullAgents.add(agents[i].fullAgents);
			}
		}

		/**
		 * Lädt die Daten aus einem XML-Element
		 * @param node	XML-Element aus dem die Daten geladen werden sollen
		 * @return	Liefert im Erfolgsfall <code>null</code> sonst eine Fehlermeldung
		 */
		private String loadFromXML(final Element node) {
			name=Language.trAllAttribute("XML.Statistic.GeneralAttributes.Name",node);
			if (!name.isEmpty()) type=Language.trAllAttribute("XML.Statistic.GeneralAttributes.Type",node);

			simAgents=new DataDistributionImpl(48,48);
			modelAgents=new DataDistributionImpl(48,48);
			fullAgents=new DataDistributionImpl(48,48);

			NodeList l=node.getChildNodes();
			for (int i=0; i<l.getLength();i++) {
				if (!(l.item(i) instanceof Element)) continue;
				Element e=(Element)l.item(i);
				String s=e.getNodeName();

				if (Language.trAll("XML.Statistic.Agents.Model.Simulation",s)) {
					simAgents=DataDistributionImpl.createFromString(e.getTextContent(),simAgents.upperBound);
					if (simAgents==null) return Language.tr("XML.Statistic.Agents.Model.Simulation.Error");
					continue;
				}
				if (Language.trAll("XML.Statistic.Agents.Model.Model",s)) {
					modelAgents=DataDistributionImpl.createFromString(e.getTextContent(),modelAgents.upperBound);
					if (modelAgents==null) return Language.tr("XML.Statistic.Agents.Model.Model.Error");
					continue;
				}
				if (Language.trAll("XML.Statistic.Agents.Model.WithSurcharge",s)) {
					fullAgents=DataDistributionImpl.createFromString(e.getTextContent(),modelAgents.upperBound);
					if (fullAgents==null) return Language.tr("XML.Statistic.Agents.Model.WithSurcharge.Error");
					continue;
				}
			}

			return null;
		}

		/**
		 * Versucht einen Modell-Agenten-Datensatz in einem XML-Knoten zu speichern
		 * @param parent Übergeordneter XML-Knoten
		 */
		private void saveDataToXML(final Element parent) {
			final Document doc=parent.getOwnerDocument();
			Element node, child;

			parent.appendChild(node=doc.createElement(Language.trPrimary("XML.Statistic.ModelAgents")));

			if (!name.isEmpty()) node.setAttribute(Language.trPrimary("XML.Statistic.GeneralAttributes.Name"),name);
			if (!type.isEmpty()) node.setAttribute(Language.trPrimary("XML.Statistic.GeneralAttributes.Type"),type);

			node.appendChild(child=doc.createElement(Language.trPrimary("XML.Statistic.Agents.Model.Simulation")));
			child.setTextContent(simAgents.storeToString());
			node.appendChild(child=doc.createElement(Language.trPrimary("XML.Statistic.Agents.Model.Model")));
			child.setTextContent(modelAgents.storeToString());
			node.appendChild(child=doc.createElement(Language.trPrimary("XML.Statistic.Agents.Model.WithSurcharge")));
			child.setTextContent(fullAgents.storeToString());
		}
	}
}