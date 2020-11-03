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
package ui.statistic.simulation;

import java.awt.Container;
import java.awt.Window;
import java.net.URL;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import javax.swing.Icon;
import javax.swing.JPanel;

import org.apache.commons.math3.distribution.AbstractRealDistribution;

import language.Language;
import mathtools.NumberTools;
import mathtools.TimeTools;
import simulator.Statistics;
import simulator.Statistics.AgentenDaten;
import simulator.Statistics.KundenDaten;
import systemtools.statistics.StatisticViewerText;
import tools.SetupData;
import ui.editor.BaseEditDialog;
import ui.help.Help;
import ui.images.Images;
import ui.model.CallcenterModel;
import ui.model.CallcenterModelAgent;
import ui.model.CallcenterModelCallcenter;
import ui.model.CallcenterModelSkillLevel;
import ui.model.CallcenterModelWarnings;
import ui.statistic.core.StatisticFilterDialog;

/**
 * Zeigt eine allgemeine Übersicht über die Ergebnisse an.
 * @author Alexander Herzog
 * @version 1.0
 */
public class StatisticViewerTextInformation extends StatisticViewerText {
	/** Statistik-Objekt, das die anzuzeigenden Informationen enthält */
	private final Statistics statistic;
	/** Angabe, was angezeigt werden soll (siehe <code>MODE_</code>-Konstanten) */
	private final Mode mode;

	/**
	 * Welche Informationen sollen angezeigt werden?
	 * @author Alexander Herzog
	 * @see StatisticViewerTextInformation#StatisticViewerTextInformation(Statistics, Mode)
	 */
	public enum Mode {
		/**
		 * Ergebnisübersicht anzeigen
		 */
		MODE_BASE_INFORMATION,

		/**
		 * Daten zu Simulator und Simulationslauf
		 */
		MODE_SYSTEM_INFORMATION,

		/**
		 * Konfidenzintervalle zu Wartezeiten, Erfolgswahrscheinlichkeiten und Service-Level
		 */
		MODE_CONFIDENCE_INFORMATION,

		/**
		 * Modellbeschreibung
		 */
		MODE_MODEL_INFORMATION,

		/**
		 * Schwellenwerte
		 */
		MODE_THRESHOLDS
	}

	/**
	 * Konstruktor der Klasse <code>StatisticViewerBaseInformation</code>
	 * @param statistic	Statistik-Objekt, das die anzuzeigenden Informationen enthält
	 * @param mode	Angabe, was angezeigt werden soll (siehe <code>MODE_</code>-Konstanten)
	 */
	public StatisticViewerTextInformation(final Statistics statistic, final Mode mode) {
		super();
		this.statistic=statistic;
		this.mode=mode;
	}

	/**
	 * Zeigt im Fußbereich der Hilfeseite eine "Erklärung einblenden"-Schaltfläche, die,
	 * wenn sie angeklickt wird, eine html-Hilfeseite anzeigt.
	 * @param topic	Hilfe-Thema (wird als Datei in den "description_*"-Ordern gesucht)
	 */
	private void addDescription(final String topic) {
		final URL url=StatisticViewerTextInformation.class.getResource("description_"+Language.getCurrentLanguage()+"/"+topic+".html");
		addDescription(url,helpTopic->Help.topic(getViewer(false),helpTopic));
	}

	/**
	 * Ausgabe Basisinformation zu einem bestimmten Kundentyp
	 * @param kunden	Kundentyp
	 * @param simDays	Anzahl an simulierten Tagen
	 * @param serviceLevel	Service-Level-Sekundenwert
	 * @see #buildBaseInformation()
	 */
	private void buildBaseClientData(final KundenDaten kunden, final long simDays, final int serviceLevel) {
		String hiddenSetup=SetupData.getSetup().simOverviewFilter;

		String serviceLevelText="P(W<="+serviceLevel+")";
		if (serviceLevel<0) serviceLevelText="("+Language.tr("SimStatistic.DifferentServiceLevelValuesPerClientType")+")";

		if (kunden.name.isEmpty()) addHeading(2,Language.tr("SimStatistic.AllClients")); else addHeading(2,kunden.name);
		beginParagraph();
		if (isIDVisible(hiddenSetup,FILTER_OVERVIEW_CLIENTS_FRESHCALLS)) addLineDiv(1,Language.tr("SimStatistic.FreshCalls"),kunden.kunden,simDays);
		if (isIDVisible(hiddenSetup,FILTER_OVERVIEW_CLIENTS_RECALL) && kunden.kundenWiederanruf>0) addLineDiv(1,Language.tr("SimStatistic.RecallingClients.Info"),kunden.kundenWiederanruf,simDays);
		if (isIDVisible(hiddenSetup,FILTER_OVERVIEW_CLIENTS_CALLS)) addLineDiv(1,Language.tr("SimStatistic.Calls.Info"),kunden.anrufe-kunden.anrufeUebertrag,simDays);
		if (isIDVisible(hiddenSetup,FILTER_OVERVIEW_CLIENTS_ACCESSIBILITY_CALLS)) addPercentLineParts(1,Language.tr("SimStatistic.Accessibility")+" ("+Language.tr("SimStatistic.OnCallBasis")+")",kunden.anrufeErfolg,kunden.anrufe-kunden.anrufeUebertrag);
		if (isIDVisible(hiddenSetup,FILTER_OVERVIEW_CLIENTS_ACCESSIBILITY_CLIENTS)) addPercentLineParts(1,Language.tr("SimStatistic.Accessibility")+" ("+Language.tr("SimStatistic.OnClientBasis")+")",kunden.kundenErfolg,kunden.kunden+kunden.kundenWiederanruf-kunden.kundenUebertrag);
		if (isIDVisible(hiddenSetup,FILTER_OVERVIEW_CLIENTS_WAITINGTIME)) addShortTimeParts(1,Language.tr("SimStatistic.AverageWaitingTime"),kunden.anrufeWartezeitSum,kunden.anrufeErfolg);
		if (isIDVisible(hiddenSetup,FILTER_OVERVIEW_CLIENTS_CANCELTIME)) addShortTimeParts(1,Language.tr("SimStatistic.AverageCancelTime"),kunden.anrufeAbbruchzeitSum,kunden.anrufeAbbruch);
		if (isIDVisible(hiddenSetup,FILTER_OVERVIEW_CLIENTS_FORWARDED)) addPercentLineParts(1,Language.tr("SimStatistic.ForwardedCallsPart"),kunden.anrufeWeiterleitungen,kunden.anrufeErfolg);
		if (isIDVisible(hiddenSetup,FILTER_OVERVIEW_CLIENTS_BLOCKED)) addPercentLineParts(1,Language.tr("SimStatistic.BlockedCallsPart"),kunden.anrufeBlocked,kunden.anrufe-kunden.anrufeUebertrag);
		if (isIDVisible(hiddenSetup,FILTER_OVERVIEW_CLIENTS_SERVICELEVEL_CALL_SUCCESS)) addPercentLineParts(1,Language.tr("SimStatistic.ServiceLevel")+" "+Language.tr("SimStatistic.OnCallBasis")+" ("+Language.tr("SimStatistic.CalculatedOn.SuccessfulCalls")+") "+serviceLevelText,kunden.anrufeServicelevel,kunden.anrufeErfolg);
		if (isIDVisible(hiddenSetup,FILTER_OVERVIEW_CLIENTS_SERVICELEVEL_CLIENT_SUCCESS)) addPercentLineParts(1,Language.tr("SimStatistic.ServiceLevel")+" "+Language.tr("SimStatistic.OnClientBasis")+" ("+Language.tr("SimStatistic.CalculatedOn.SuccessfulClients")+") "+serviceLevelText,kunden.kundenServicelevel,kunden.kundenErfolg);
		if (isIDVisible(hiddenSetup,FILTER_OVERVIEW_CLIENTS_SERVICELEVEL_CALL_ALL)) addPercentLineParts(1,Language.tr("SimStatistic.ServiceLevel")+" "+Language.tr("SimStatistic.OnCallBasis")+" ("+Language.tr("SimStatistic.CalculatedOn.AllCalls")+") "+serviceLevelText,kunden.anrufeServicelevel,kunden.anrufe);
		if (isIDVisible(hiddenSetup,FILTER_OVERVIEW_CLIENTS_SERVICELEVEL_CLIENT_ALL)) addPercentLineParts(1,Language.tr("SimStatistic.ServiceLevel")+" "+Language.tr("SimStatistic.OnClientBasis")+" ("+Language.tr("SimStatistic.CalculatedOn.AllClients")+") "+serviceLevelText,kunden.kundenServicelevel,kunden.kunden);
		endParagraph();
	}

	/**
	 * Ausgabe von Angaben zu den Warteschlangenlängen
	 * @param statistic	Statistik-Objekt, das die anzuzeigenden Informationen enthält
	 * @see #buildBaseInformation()
	 */
	private void buildBaseQueueLengthData(final Statistics statistic) {
		addHeading(2,Language.tr("SimStatistic.AverageQueueLength"));

		beginParagraph();
		addLine(1,Language.tr("SimStatistic.AverageQueueLength.OverTheDay"),statistic.meanQueueLength);

		int maxNr=0;
		double maxValue=0;
		for (int i=0;i<statistic.meanQueueLengthProIntervall.densityData.length;i++) if (statistic.meanQueueLengthProIntervall.densityData[i]>maxValue) {
			maxValue=statistic.meanQueueLengthProIntervall.densityData[i];
			maxNr=i;
		}
		String t=TimeTools.formatTime(maxNr*1800)+"-"+TimeTools.formatTime((maxNr+1)*1800);
		addLine(2,String.format(Language.tr("SimStatistic.AverageQueueLength.Maximum"),t),statistic.meanQueueLengthProIntervall.densityData[maxNr]);
		endParagraph();
	}

	/*
	public static double getAgentsTimes(Statistics statistic, String callcenterName, String skillLevelName, boolean useProductivity) {
		return getAgentsTimes(statistic.editModel,callcenterName,skillLevelName,useProductivity);
	}
	 */

	/**
	 * Berechnet auf Basis von Modelldaten die verfügbare Agentenarbeitsleistung
	 * @param model	Callcenter-Modell dem die Daten entnommen werden sollen
	 * @param callcenterName	Name des zu betrachtenden Callcenters oder <code>null</code>, wenn alle Callcenter berücksichtigt werden sollen
	 * @param skillLevelName	Name des zu betrachtenden Skill-Levels oder <code>null</code>, wenn alle Skill-Level berücksichtigt werden sollen
	 * @param useProductivity	Soll die angegebene Produktivität berücksichtigt werden (=Nettowert, <code>true</code>) oder soll die Brutto-Bedienleistung ausgegeben werden (<code>false</code>)
	 * @return	Anzahl an pro Simulationstag verfügbaren Agenten-Bediensekunden
	 */
	public static double getAgentsTimes(CallcenterModel model, String callcenterName, String skillLevelName, boolean useProductivity) {
		/* Bei Bedarf Schichtpläne erstellen */
		List<CallcenterModelAgent> translatedAgents=new ArrayList<CallcenterModelAgent>();
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
	 * Ausgabe von Basisdaten zu einer Agentengruppe
	 * @param agenten	Agentengruppe
	 * @see #buildBaseInformation()
	 */
	private void buildBaseCallcenterData(final AgentenDaten agenten) {
		String hiddenSetup=SetupData.getSetup().simOverviewFilter;

		if (agenten.name.isEmpty()) addHeading(2,Language.tr("SimStatistic.AllActiveAgents")); else addHeading(2,agenten.name);

		beginParagraph();
		if (isIDVisible(hiddenSetup,FILTER_OVERVIEW_AGENTS_NUMBER)) addLine(1,Language.tr("SimStatistic.Number"),agenten.anzahlAgenten);
		long sum=agenten.leerlaufGesamt+agenten.technischerLeerlaufGesamt+agenten.arbeitGesamt+agenten.postProcessingGesamt;
		if (isIDVisible(hiddenSetup,FILTER_OVERVIEW_AGENTS_IDLE)) addPercentLineParts(1,Language.tr("SimStatistic.IdleTime"),agenten.leerlaufGesamt,sum);
		if (isIDVisible(hiddenSetup,FILTER_OVERVIEW_AGENTS_TECHNICAL)) addPercentLineParts(1,Language.tr("SimStatistic.TechnicalFreeTime"),agenten.technischerLeerlaufGesamt,sum);
		if (isIDVisible(hiddenSetup,FILTER_OVERVIEW_AGENTS_HOLDING)) addPercentLineParts(1,Language.tr("SimStatistic.HoldingTime"),agenten.arbeitGesamt,sum);
		if (isIDVisible(hiddenSetup,FILTER_OVERVIEW_AGENTS_POSTPROCESSING)) addPercentLineParts(1,Language.tr("SimStatistic.PostProcessingTime"),agenten.postProcessingGesamt,sum);
		double brutto=agenten.getAgentsTimes(statistic.editModel,false);
		double netto=agenten.getAgentsTimes(statistic.editModel,true);
		if (isIDVisible(hiddenSetup,FILTER_OVERVIEW_AGENTS_WORK_SCHEDULED)) {
			if (Math.abs(brutto-netto)<0.1) {
				addLine(1,Language.tr("SimStatistic.AgentsWorkingHours.Scheduled")+": "+NumberTools.formatNumberLong(brutto/3600)+" "+Language.tr("Statistic.Units.Hours.lower"));
			} else {
				addLine(1,Language.tr("SimStatistic.AgentsWorkingHours.ScheduledBrutto")+": "+NumberTools.formatNumberLong(brutto/3600)+" "+Language.tr("Statistic.Units.Hours.lower"));
				addLine(1,Language.tr("SimStatistic.AgentsWorkingHours.ScheduledNetto")+": "+NumberTools.formatNumberLong(netto/3600)+" "+Language.tr("Statistic.Units.Hours.lower"));
			}
		}
		if (isIDVisible(hiddenSetup,FILTER_OVERVIEW_AGENTS_WORK_WORKED)) addLine(1,Language.tr("SimStatistic.AgentsWorkingHours.Worked")+": "+NumberTools.formatNumberLong((double)sum/statistic.simulationData.runRepeatCount/3600)+" "+Language.tr("Statistic.Units.Hours.lower"));
		endParagraph();
	}

	/**
	 * Ergebnisübersicht anzeigen
	 * @see Mode#MODE_BASE_INFORMATION
	 */
	private void buildBaseInformation() {
		addHeading(1,Language.tr("SimStatistic.ResultOverview"));

		boolean needDetailedData=(statistic.kundenProTyp.length>1);
		if (!needDetailedData && statistic.editModel.caller.get(0).serviceLevelSeconds>0) needDetailedData=true;

		int serviceLevel=statistic.editModel.serviceLevelSeconds;
		if (statistic.editModel.caller.get(0).serviceLevelSeconds>0) serviceLevel=statistic.editModel.caller.get(0).serviceLevelSeconds;
		for (int i=1;i<statistic.kundenProTyp.length;i++) {
			int sl=statistic.editModel.caller.get(i).serviceLevelSeconds;
			if (sl<=0) sl=statistic.editModel.serviceLevelSeconds;
			if (sl!=serviceLevel) {serviceLevel=-1; break;}
		}

		buildBaseClientData(statistic.kundenGlobal,statistic.simulationData.runRepeatCount,serviceLevel);
		if (needDetailedData) for (int i=0;i<statistic.kundenProTyp.length;i++) {
			int sl=statistic.editModel.caller.get(i).serviceLevelSeconds;
			if (sl<=0) sl=statistic.editModel.serviceLevelSeconds;
			buildBaseClientData(statistic.kundenProTyp[i],statistic.simulationData.runRepeatCount,sl);
		}

		if (isIDVisible(SetupData.getSetup().simOverviewFilter,FILTER_OVERVIEW_QUEUE)) buildBaseQueueLengthData(statistic);

		buildBaseCallcenterData(statistic.agentenGlobal);
		if (statistic.agentenProCallcenter.length>1) for (int i=0;i<statistic.agentenProCallcenter.length;i++)
			buildBaseCallcenterData(statistic.agentenProCallcenter[i]);

		if (isIDVisible(SetupData.getSetup().simOverviewFilter,FILTER_OVERVIEW_THRESHOLDS)) {
			buildThresholdInformation(false);
			buildRecommendations(false);
		}

		/* Infotext  */
		addDescription("BaseInformation");
	}

	/**
	 * Ausgabe von
	 * Daten zu Simulator und Simulationslauf
	 * @see Mode#MODE_SYSTEM_INFORMATION
	 */
	private void buildSystemInformation() {
		addHeading(1,Language.tr("SimStatistic.SystemData"));

		beginParagraph();
		addLine(Language.tr("SimStatistic.SystemData.Version")+": "+statistic.editModel.version);
		if (!statistic.simulationData.runUser.isEmpty()) addLine(Language.tr("SimStatistic.SystemData.User")+": "+statistic.simulationData.runUser);
		if (!statistic.simulationData.runDate.isEmpty()) addLine(Language.tr("SimStatistic.SystemData.Date")+": "+statistic.simulationData.runDate);
		addLine(Language.tr("SimStatistic.SystemData.Threads")+": "+NumberTools.formatLong(statistic.simulationData.runThreads));
		if (!statistic.simulationData.runOS.isEmpty()) addLine(Language.tr("SimStatistic.SystemData.ServerOS")+": "+statistic.simulationData.runOS);
		addLine(Language.tr("SimStatistic.SystemData.SimDays")+": "+NumberTools.formatLong(statistic.simulationData.runRepeatCount));
		endParagraph();

		beginParagraph();
		String t=(statistic.simulationData.runThreads>1)?" (*)":"";
		addLine(Language.tr("SimStatistic.SystemData.SimulationTime")+": "+NumberTools.formatLong(statistic.simulationData.runTime)+" "+Language.tr("Statistic.Units.MilliSeconds"));
		addLine(Language.tr("SimStatistic.SystemData.SimulationTimePerSimulatedDay")+t+": "+NumberTools.formatLong(statistic.simulationData.runTime*statistic.simulationData.runThreads/statistic.simulationData.runRepeatCount)+" "+Language.tr("Statistic.Units.MilliSeconds"));
		addLine(Language.tr("SimStatistic.SystemData.SimulatedEvents")+": "+NumberTools.formatLong(statistic.simulationData.runEvents));
		endParagraph();

		if (statistic.simulationData.runTime>0) {
			beginParagraph();
			addLine(Language.tr("SimStatistic.SystemData.EventsPerSecond")+": "+NumberTools.formatLong(1000*statistic.simulationData.runEvents/statistic.simulationData.runTime));
			addLine(Language.tr("SimStatistic.SystemData.SimulationModelClientsPerSecond")+": "+NumberTools.formatLong(1000*(long)(statistic.kundenGlobal.kunden+statistic.kundenGlobal.kundenWiederanruf)/statistic.simulationData.runRepeatCount/statistic.simulationData.runTime));
			addLine(Language.tr("SimStatistic.SystemData.ClientsPerSecond")+": "+NumberTools.formatLong(1000*(long)(statistic.kundenGlobal.kunden+statistic.kundenGlobal.kundenWiederanruf)/statistic.simulationData.runTime));
			if (statistic.kundenGlobal.anrufe>0) addLine(Language.tr("SimStatistic.SystemData.TimePerCall")+t+": "+NumberTools.formatNumber(1000*(double)statistic.simulationData.runTime*statistic.simulationData.runThreads/statistic.kundenGlobal.anrufe,1)+" µs");
			if (statistic.simulationData.runEvents>0) addLine(Language.tr("SimStatistic.SystemData.TimePerEvent")+t+": "+NumberTools.formatNumber(1000*(double)statistic.simulationData.runTime*statistic.simulationData.runThreads/statistic.simulationData.runEvents,2)+" µs");
			endParagraph();
		}

		if (statistic.simulationData.runThreads>1) {
			beginParagraph();
			addLines(Language.tr("SimStatistic.SystemData.MultiThreadInfo"));
			endParagraph();
		}
	}

	/**
	 * Ausgabe von Konfidenzintervallen zu einem bestimmten Kundentyp
	 * @param kunden	Kundentyp
	 * @param simDays	Anzahl an simulierten Tagen
	 * @param serviceLevel	Service-Level-Sekundenwert
	 * @see #buildConfidenceInformation()
	 */
	private void buildKonfidenceClientData(final KundenDaten kunden, final long simDays, final int serviceLevel) {
		String hiddenSetup=SetupData.getSetup().confidenceFilter;

		if (kunden.name.isEmpty()) addHeading(2,Language.tr("SimStatistic.AllClients")); else addHeading(2,kunden.name);

		double d;
		double[] c;

		if (isIDVisible(hiddenSetup,FILTER_CONFIDENCE_ACCESSIBILITY_CALLS)) {
			addHeading(3,Language.tr("SimStatistic.Accessibility")+" ("+Language.tr("SimStatistic.OnCallBasis")+")");
			beginParagraph();
			addPercentLine(2,Language.tr("Distribution.Mean"),kunden.interDaySuccessCallsSum/simDays,2);
			d=calcStd(kunden.interDaySuccessCallsSum2,kunden.interDaySuccessCallsSum,simDays);
			addLine(2,Language.tr("Distribution.StdDev"),d,3);
			addLine(2,Language.tr("Distribution.CV"),d/(kunden.interDaySuccessCallsSum/simDays),3);
			c=calcConfidence(kunden.interDaySuccessCallsSum2,kunden.interDaySuccessCallsSum,simDays,0.9);
			addLine(2,"90%"+Language.tr("SimStatistic.ConfidenceIntervals.ForMean")+": ["+NumberTools.formatNumber(c[0]*100,2)+"%;"+NumberTools.formatNumber(c[1]*100,2)+"%]");
			c=calcConfidence(kunden.interDaySuccessCallsSum2,kunden.interDaySuccessCallsSum,simDays,0.95);
			addLine(2,"95%"+Language.tr("SimStatistic.ConfidenceIntervals.ForMean")+": ["+NumberTools.formatNumber(c[0]*100,2)+"%;"+NumberTools.formatNumber(c[1]*100,2)+"%]");
			endParagraph();
		}

		if (isIDVisible(hiddenSetup,FILTER_CONFIDENCE_ACCESSIBILITY_CLIENTS)) {
			addHeading(3,Language.tr("SimStatistic.Accessibility")+" ("+Language.tr("SimStatistic.OnClientBasis")+")");
			beginParagraph();
			addPercentLine(2,Language.tr("Distribution.Mean"),kunden.interDaySuccessClientsSum/simDays,2);
			d=calcStd(kunden.interDaySuccessClientsSum2,kunden.interDaySuccessClientsSum,simDays);
			addLine(2,Language.tr("Distribution.StdDev"),d,3);
			addLine(2,Language.tr("Distribution.CV"),d/(kunden.interDaySuccessClientsSum/simDays),3);
			c=calcConfidence(kunden.interDaySuccessClientsSum2,kunden.interDaySuccessClientsSum,simDays,0.9);
			addLine(2,"90%"+Language.tr("SimStatistic.ConfidenceIntervals.ForMean")+": ["+NumberTools.formatNumber(c[0]*100,2)+"%;"+NumberTools.formatNumber(c[1]*100,2)+"%]");
			c=calcConfidence(kunden.interDaySuccessClientsSum2,kunden.interDaySuccessClientsSum,simDays,0.95);
			addLine(2,"95%"+Language.tr("SimStatistic.ConfidenceIntervals.ForMean")+": ["+NumberTools.formatNumber(c[0]*100,2)+"%;"+NumberTools.formatNumber(c[1]*100,2)+"%]");
			endParagraph();
		}

		if (isIDVisible(hiddenSetup,FILTER_CONFIDENCE_WAITINGTIME)) {
			addHeading(3,Language.tr("SimStatistic.WaitingTime"));
			beginParagraph();
			addShortTime(2,Language.tr("Distribution.Mean"),kunden.interDayWartezeitSum/simDays);
			d=calcStd(kunden.interDayWartezeitSum2,kunden.interDayWartezeitSum,simDays);
			addShortTime(2,Language.tr("Distribution.StdDev"),d);
			addLine(2,Language.tr("Distribution.CV"),d/(kunden.interDayWartezeitSum/simDays),3);
			c=calcConfidence(kunden.interDayWartezeitSum2,kunden.interDayWartezeitSum,simDays,0.9);
			addLine(2,"90%"+Language.tr("SimStatistic.ConfidenceIntervals.ForMean")+": ["+TimeTools.formatExactTime(c[0])+";"+TimeTools.formatExactTime(c[1])+"]");
			c=calcConfidence(kunden.interDayWartezeitSum2,kunden.interDayWartezeitSum,simDays,0.95);
			addLine(2,"95%"+Language.tr("SimStatistic.ConfidenceIntervals.ForMean")+": ["+TimeTools.formatExactTime(c[0])+";"+TimeTools.formatExactTime(c[1])+"]");
			endParagraph();
		}

		if (isIDVisible(hiddenSetup,FILTER_CONFIDENCE_SERVICELEVEL_CALL_SUCCESS)) {
			addHeading(3,Language.tr("SimStatistic.ServiceLevel")+" P(W<="+serviceLevel+") ("+Language.tr("SimStatistic.OnCallBasis")+", "+Language.tr("SimStatistic.CalculatedOn.SuccessfulCalls")+")");
			beginParagraph();
			addPercentLine(2,Language.tr("Distribution.Mean"),kunden.interDayServiceLevelCallsSuccessSum/simDays,2);
			d=calcStd(kunden.interDayServiceLevelCallsSuccessSum2,kunden.interDayServiceLevelCallsSuccessSum,simDays);
			addLine(2,Language.tr("Distribution.StdDev"),d,3);
			addLine(2,Language.tr("Distribution.CV"),d/(kunden.interDayServiceLevelCallsSuccessSum/simDays),3);
			c=calcConfidence(kunden.interDayServiceLevelCallsSuccessSum2,kunden.interDayServiceLevelCallsSuccessSum,simDays,0.9);
			addLine(2,"90%"+Language.tr("SimStatistic.ConfidenceIntervals.ForMean")+": ["+NumberTools.formatNumber(c[0]*100,2)+"%;"+NumberTools.formatNumber(c[1]*100,2)+"%]");
			c=calcConfidence(kunden.interDayServiceLevelCallsSuccessSum2,kunden.interDayServiceLevelCallsSuccessSum,simDays,0.95);
			addLine(2,"95%"+Language.tr("SimStatistic.ConfidenceIntervals.ForMean")+": ["+NumberTools.formatNumber(c[0]*100,2)+"%;"+NumberTools.formatNumber(c[1]*100,2)+"%]");
			endParagraph();
		}

		if (isIDVisible(hiddenSetup,FILTER_CONFIDENCE_SERVICELEVEL_CALL_ALL)) {
			addHeading(3,Language.tr("SimStatistic.ServiceLevel")+" P(W<="+serviceLevel+") ("+Language.tr("SimStatistic.OnCallBasis")+", "+Language.tr("SimStatistic.CalculatedOn.AllCalls")+")");
			beginParagraph();
			addPercentLine(2,Language.tr("Distribution.Mean"),kunden.interDayServiceLevelCallsAllSum/simDays,2);
			d=calcStd(kunden.interDayServiceLevelCallsAllSum2,kunden.interDayServiceLevelCallsAllSum,simDays);
			addLine(2,Language.tr("Distribution.StdDev"),d,3);
			addLine(2,Language.tr("Distribution.CV"),d/(kunden.interDayServiceLevelCallsAllSum/simDays),3);
			c=calcConfidence(kunden.interDayServiceLevelCallsAllSum2,kunden.interDayServiceLevelCallsAllSum,simDays,0.9);
			addLine(2,"90%"+Language.tr("SimStatistic.ConfidenceIntervals.ForMean")+": ["+NumberTools.formatNumber(c[0]*100,2)+"%;"+NumberTools.formatNumber(c[1]*100,2)+"%]");
			c=calcConfidence(kunden.interDayServiceLevelCallsAllSum2,kunden.interDayServiceLevelCallsAllSum,simDays,0.95);
			addLine(2,"95%"+Language.tr("SimStatistic.ConfidenceIntervals.ForMean")+": ["+NumberTools.formatNumber(c[0]*100,2)+"%;"+NumberTools.formatNumber(c[1]*100,2)+"%]");
			endParagraph();
		}

		if (isIDVisible(hiddenSetup,FILTER_CONFIDENCE_SERVICELEVEL_CLIENT_SUCCESS)) {
			addHeading(3,Language.tr("SimStatistic.ServiceLevel")+" P(W<="+serviceLevel+") ("+Language.tr("SimStatistic.OnClientBasis")+", "+Language.tr("SimStatistic.CalculatedOn.SuccessfulClients")+")");
			beginParagraph();
			addPercentLine(2,Language.tr("Distribution.Mean"),kunden.interDayServiceLevelClientsSuccessSum/simDays,2);
			d=calcStd(kunden.interDayServiceLevelClientsSuccessSum2,kunden.interDayServiceLevelClientsSuccessSum,simDays);
			addLine(2,Language.tr("Distribution.StdDev"),d,3);
			addLine(2,Language.tr("Distribution.CV"),d/(kunden.interDayServiceLevelClientsSuccessSum/simDays),3);
			c=calcConfidence(kunden.interDayServiceLevelClientsSuccessSum2,kunden.interDayServiceLevelClientsSuccessSum,simDays,0.9);
			addLine(2,"90%"+Language.tr("SimStatistic.ConfidenceIntervals.ForMean")+": ["+NumberTools.formatNumber(c[0]*100,2)+"%;"+NumberTools.formatNumber(c[1]*100,2)+"%]");
			c=calcConfidence(kunden.interDayServiceLevelClientsSuccessSum2,kunden.interDayServiceLevelClientsSuccessSum,simDays,0.95);
			addLine(2,"95%"+Language.tr("SimStatistic.ConfidenceIntervals.ForMean")+": ["+NumberTools.formatNumber(c[0]*100,2)+"%;"+NumberTools.formatNumber(c[1]*100,2)+"%]");
			endParagraph();
		}

		if (isIDVisible(hiddenSetup,FILTER_CONFIDENCE_SERVICELEVEL_CLIENT_ALL)) {
			addHeading(3,Language.tr("SimStatistic.ServiceLevel")+" P(W<="+serviceLevel+") ("+Language.tr("SimStatistic.OnClientBasis")+", "+Language.tr("SimStatistic.CalculatedOn.AllClients")+")");
			beginParagraph();
			addPercentLine(2,Language.tr("Distribution.Mean"),kunden.interDayServiceLevelClientsAllSum/simDays,2);
			d=calcStd(kunden.interDayServiceLevelClientsAllSum2,kunden.interDayServiceLevelClientsAllSum,simDays);
			addLine(2,Language.tr("Distribution.StdDev"),d,3);
			addLine(2,Language.tr("Distribution.CV"),d/(kunden.interDayServiceLevelClientsAllSum/simDays),3);
			c=calcConfidence(kunden.interDayServiceLevelClientsAllSum2,kunden.interDayServiceLevelClientsAllSum,simDays,0.9);
			addLine(2,"90%"+Language.tr("SimStatistic.ConfidenceIntervals.ForMean")+": ["+NumberTools.formatNumber(c[0]*100,2)+"%;"+NumberTools.formatNumber(c[1]*100,2)+"%]");
			c=calcConfidence(kunden.interDayServiceLevelClientsAllSum2,kunden.interDayServiceLevelClientsAllSum,simDays,0.95);
			addLine(2,"95%"+Language.tr("SimStatistic.ConfidenceIntervals.ForMean")+": ["+NumberTools.formatNumber(c[0]*100,2)+"%;"+NumberTools.formatNumber(c[1]*100,2)+"%]");
			endParagraph();
		}
	}


	/**
	 * Ausgabe von
	 * Konfidenzintervallen zu Wartezeiten, Erfolgswahrscheinlichkeiten und Service-Level
	 * @see Mode#MODE_CONFIDENCE_INFORMATION
	 */
	private void buildConfidenceInformation() {
		addHeading(1,Language.tr("SimStatistic.ConfidenceIntervals"));

		addLines(Language.tr("SimStatistic.ConfidenceIntervals.Info"));

		buildKonfidenceClientData(statistic.kundenGlobal,statistic.simulationData.runRepeatCount,statistic.editModel.serviceLevelSeconds);
		if (statistic.kundenProTyp.length>1) for (int i=0;i<statistic.kundenProTyp.length;i++) {
			int sl=(statistic.editModel.caller.get(i).serviceLevelSeconds>0)?(statistic.editModel.caller.get(i).serviceLevelSeconds):(statistic.editModel.serviceLevelSeconds);
			buildKonfidenceClientData(statistic.kundenProTyp[i],statistic.simulationData.runRepeatCount,sl);
		}

		/* Infotext  */
		addDescription("ConfidenceInformation");
	}

	/**
	 * Ausgabe einer
	 * Modellbeschreibung
	 * @see Mode#MODE_MODEL_INFORMATION
	 */
	private void buildModelInformation() {
		addHeading(1,Language.tr("SimStatistic.ModelInformation.Name"));
		addLines(statistic.editModel.name);

		addHeading(1,Language.tr("SimStatistic.ModelInformation.Description"));
		addLines(statistic.editModel.description);

		if (statistic.editModel.date!=null && !statistic.editModel.date.trim().isEmpty()) {
			addHeading(2,Language.tr("SimStatistic.ModelInformation.Date"));
			beginParagraph();
			addLine(CallcenterModel.dateToLocalString(CallcenterModel.stringToDate(statistic.editModel.date)));
			endParagraph();
		}

		addHeading(1,Language.tr("SimStatistic.ModelInformation.Summary"));
		addLines(statistic.editModel.generateDescription());
	}

	/**
	 * Ausgabe von
	 * Schwellenwerten
	 * @see Mode#MODE_THRESHOLDS
	 */
	private void buildThresholdAndRecommendationsInformation() {
		addHeading(1,Language.tr("SimStatistic.ThresholdsAndRecommendations"));

		buildThresholdInformation(true);
		buildRecommendations(true);
	}

	/**
	 * Ausgabe von Empfehlungen
	 * @param fullInformation	Vollständige (<code>true</code>) oder überblicksartige Informationen (<code>false</code>)
	 * @see #buildThresholdAndRecommendationsInformation()
	 */
	private void buildRecommendations(final boolean fullInformation) {
		List <String> recommendations=new ArrayList<String>();

		/* Schlechte Erreichbarkeit */
		double success=((double)statistic.kundenGlobal.anrufeErfolg)/Math.max(1,statistic.kundenGlobal.anrufe);
		if (success<0.6) recommendations.add(String.format(Language.tr("SimStatistic.Recommendations.LowAccessibility"),NumberTools.formatPercent(success,0)));

		/* Lange Wartezeiten */
		long waitingtime=statistic.kundenGlobal.anrufeWartezeitSum/Math.max(1,statistic.kundenGlobal.anrufeErfolg);
		if (waitingtime>180) recommendations.add(String.format(Language.tr("SimStatistic.Recommendations.LongWaitingTimes"),TimeTools.formatTime(waitingtime)));

		/* Niedrige Auslastung */
		double workload=((double)(statistic.agentenGlobal.arbeitGesamt+statistic.agentenGlobal.postProcessingGesamt))/Math.max(1,statistic.agentenGlobal.leerlaufGesamt+statistic.agentenGlobal.technischerLeerlaufGesamt+statistic.agentenGlobal.arbeitGesamt+statistic.agentenGlobal.postProcessingGesamt);
		if (workload<0.5) recommendations.add(String.format(Language.tr("SimStatistic.Recommendations.LowWorkload"),NumberTools.formatPercent(workload,0)));

		/* Starke Schwankungen in der Auslastung über den Tag */
		double minWorkLoad=1;
		double maxWorkLoad=0;
		for (int i=0;i<48;i++) {
			if (statistic.kundenGlobal.anrufeProIntervall.densityData[i]<statistic.kundenGlobal.anrufe/24) continue;
			double w=(statistic.agentenGlobal.arbeitProIntervall.densityData[i]+statistic.agentenGlobal.postProcessingProIntervall.densityData[i])/Math.max(1,statistic.agentenGlobal.leerlaufProIntervall.densityData[i]+statistic.agentenGlobal.technischerLeerlaufProIntervall.densityData[i]+statistic.agentenGlobal.arbeitProIntervall.densityData[i]+statistic.agentenGlobal.postProcessingProIntervall.densityData[i]);
			if (w>maxWorkLoad) maxWorkLoad=w;
			if (w<minWorkLoad) minWorkLoad=w;
		}
		if (maxWorkLoad-minWorkLoad>0.2) recommendations.add(Language.tr("SimStatistic.Recommendations.HighWorkloadVarinace"));

		/* Starke Schwankungen in der Auslastung zwischen den Agentengruppen */
		minWorkLoad=1;
		maxWorkLoad=0;
		for (Statistics.AgentenDaten agents : statistic.agentenProCallcenter) {
			double w=(double)(agents.arbeitGesamt+agents.postProcessingGesamt)/(agents.leerlaufGesamt+agents.technischerLeerlaufGesamt+agents.arbeitGesamt+agents.postProcessingGesamt);
			if (w>maxWorkLoad) maxWorkLoad=w;
			if (w<minWorkLoad) minWorkLoad=w;
		}
		if (maxWorkLoad-minWorkLoad>0.5) recommendations.add(Language.tr("SimStatistic.Recommendations.HighWorkloadVarinaceByCallcenters"));

		/* Hohe Variationskoeffizienten in den Bedien- und Nachbearbeitungszeiten */
		for (CallcenterModelSkillLevel skill: statistic.editModel.skills) {
			for (AbstractRealDistribution dist: skill.callerTypeWorkingTime) {
				double valueVar=dist.getNumericalVariance();
				double valueE=dist.getNumericalMean();
				if (Double.isInfinite(valueVar) || Double.isNaN(valueVar)) continue;
				if (Math.abs(valueE)<0.0001) continue;
				double CV=Math.sqrt(valueVar)/valueE;
				if (CV>0.5) recommendations.add(String.format(Language.tr("SimStatistic.Recommendations.HighWorkingTimeCV"),skill.name));
			}
			for (AbstractRealDistribution dist: skill.callerTypePostProcessingTime) {
				double valueVar=dist.getNumericalVariance();
				double valueE=dist.getNumericalVariance();
				if (Double.isInfinite(valueVar) || Double.isNaN(valueVar)) continue;
				if (Math.abs(valueE)<0.0001) continue;
				double CV=Math.sqrt(valueVar)/valueE;
				if (CV>0.5) recommendations.add(String.format(Language.tr("SimStatistic.Recommendations.HighPostProcessingTimeCV"),skill.name));
			}
		}

		/* Ausgabe der Empfehlungen */
		if (recommendations.size()==0) {
			if (fullInformation) {
				addHeading(2,Language.tr("SimStatistic.Recommendations"));
				beginParagraph();
				addLine(Language.tr("SimStatistic.Recommendations.NoRecommendations"));
				endParagraph();
			}
		} else {
			addHeading(2,Language.tr("SimStatistic.Recommendations"));
			beginParagraph();
			for (String line: recommendations) addLine(line);
			endParagraph();
		}
	}

	/**
	 * Ausgabe von Schwellenwert-Informationen
	 * @param fullInformation	Vollständige (<code>true</code>) oder überblicksartige Informationen (<code>false</code>)
	 * @see #buildThresholdAndRecommendationsInformation()
	 */
	private void buildThresholdInformation(final boolean fullInformation) {
		if (!fullInformation && statistic.warnings.records.size()==0) return;

		addHeading(2,Language.tr("SimStatistic.Thresholds"));

		if (statistic.warnings.records.size()==0) {
			beginParagraph();
			addLine(Language.tr("SimStatistic.Thresholds.NoThresholds"));
			endParagraph();
			return;
		}

		if (!fullInformation) beginParagraph();
		int ok=0;
		for (CallcenterModelWarnings.WarningRecord record : statistic.warnings.records) {

			if (record.warningStatus==CallcenterModelWarnings.WarningStatus.WARNING_STATUS_OK) {
				ok++;
				if (!fullInformation) continue;
			}

			if (fullInformation) beginParagraph();

			String warning="";
			switch (record.warningStatus) {
			case WARNING_STATUS_OK: warning=Language.tr("SimStatistic.Thresholds.Status.Ok"); break;
			case WARNING_STATUS_YELLOW: warning=Language.tr("SimStatistic.Thresholds.Status.Yellow"); break;
			case WARNING_STATUS_RED: warning=Language.tr("SimStatistic.Thresholds.Status.Red"); break;
			}
			String type="";
			switch (record.type) {
			case WARNING_TYPE_WAITINGTIME_CALL: type=Language.tr("Editor.GeneralData.ThresholdValues.Type.WaitingTimeCalls"); break;
			case WARNING_TYPE_WAITINGTIME_CLIENT: type=Language.tr("Editor.GeneralData.ThresholdValues.Type.WaitingTimeClients"); break;
			case WARNING_TYPE_RESIDENCETIME_CALL: type=Language.tr("Editor.GeneralData.ThresholdValues.Type.ResidenceTimeCalls"); break;
			case WARNING_TYPE_RESIDENCETIME_CLIENT: type=Language.tr("Editor.GeneralData.ThresholdValues.Type.ResidenceTimeClients"); break;
			case WARNING_TYPE_SUCCESSPART_CALL: type=Language.tr("Editor.GeneralData.ThresholdValues.Type.SuccessPartCalls"); break;
			case WARNING_TYPE_SUCCESSPART_CLIENT: type=Language.tr("Editor.GeneralData.ThresholdValues.Type.SuccessPartClients"); break;
			case WARNING_TYPE_SERVICELEVEL_CALL_SUCCESSFUL: type=Language.tr("Editor.GeneralData.ThresholdValues.Type.ServiceLevelOnSuccessfulCalls"); break;
			case WARNING_TYPE_SERVICELEVEL_CLIENTS_SUCCESSFUL: type=Language.tr("Editor.GeneralData.ThresholdValues.Type.ServiceLevelOnSuccessfulClients"); break;
			case WARNING_TYPE_SERVICELEVEL_CALL_ALL: type=Language.tr("Editor.GeneralData.ThresholdValues.Type.ServiceLevelOnAllCalls"); break;
			case WARNING_TYPE_SERVICELEVEL_CLIENTS_ALL: type=Language.tr("Editor.GeneralData.ThresholdValues.Type.ServiceLevelOnAllClients"); break;
			case WARNING_TYPE_WORKLOAD: type=Language.tr("Editor.GeneralData.ThresholdValues.Type.Workload"); break;
			}
			addLine(warning+": "+type);

			if (fullInformation) {
				String currentValue, thresholdValue;
				if (record.type==CallcenterModelWarnings.WarningType.WARNING_TYPE_WAITINGTIME_CALL || record.type==CallcenterModelWarnings.WarningType.WARNING_TYPE_WAITINGTIME_CLIENT || record.type==CallcenterModelWarnings.WarningType.WARNING_TYPE_RESIDENCETIME_CALL || record.type==CallcenterModelWarnings.WarningType.WARNING_TYPE_RESIDENCETIME_CLIENT) {
					currentValue=TimeTools.formatExactTime(record.value);
					thresholdValue=TimeTools.formatExactTime(record.warningYellow);
				} else {
					currentValue=NumberTools.formatPercent(record.value);
					thresholdValue=NumberTools.formatPercent(record.warningYellow);
				}
				String compare;
				if (record.warningStatus==CallcenterModelWarnings.WarningStatus.WARNING_STATUS_OK) {
					if (record.type==CallcenterModelWarnings.WarningType.WARNING_TYPE_WAITINGTIME_CALL || record.type==CallcenterModelWarnings.WarningType.WARNING_TYPE_WAITINGTIME_CLIENT || record.type==CallcenterModelWarnings.WarningType.WARNING_TYPE_RESIDENCETIME_CALL || record.type==CallcenterModelWarnings.WarningType.WARNING_TYPE_RESIDENCETIME_CLIENT) {
						compare="<=";
					} else {
						compare=">";
					}
				} else {
					if (record.type==CallcenterModelWarnings.WarningType.WARNING_TYPE_WAITINGTIME_CALL || record.type==CallcenterModelWarnings.WarningType.WARNING_TYPE_WAITINGTIME_CLIENT || record.type==CallcenterModelWarnings.WarningType.WARNING_TYPE_RESIDENCETIME_CALL || record.type==CallcenterModelWarnings.WarningType.WARNING_TYPE_RESIDENCETIME_CLIENT) {
						compare=">=";
					} else {
						compare="<";
					}
				}
				addLine(1,String.format(Language.tr("SimStatistic.Thresholds.Compare"),currentValue,compare,thresholdValue));

				String info1="";
				switch (record.modeTime) {
				case WARNING_MODE_AVERAGE:
					info1=Language.tr("Editor.GeneralData.ThresholdValues.ModeTime.Average");
					break;
				case WARNING_MODE_EACH:
					info1=Language.tr("Editor.GeneralData.ThresholdValues.ModeTime.Each");
					break;
				case WARNING_MODE_SELECTED:
					info1=Language.tr("Editor.GeneralData.ThresholdValues.ModeTime.Intervals.Info");
					break;
				}
				String info2="";
				switch (record.modeGroup) {
				case WARNING_MODE_AVERAGE:
					info2=Language.tr("Editor.GeneralData.ThresholdValues.ModeGroups.Average");
					break;
				case WARNING_MODE_EACH:
					info2=Language.tr("Editor.GeneralData.ThresholdValues.ModeGroups.Each");
					break;
				case WARNING_MODE_SELECTED:
					info2=Language.tr("Editor.GeneralData.ThresholdValues.ModeGroups.Group.Info")+": "+record.group;
					break;
				}
				addLine(1,"("+info1+", "+info2+")");
			}

			if (fullInformation) endParagraph();
		}

		if (!fullInformation && ok>0) {
			if (ok==statistic.warnings.records.size()) {
				addLine(String.format(Language.tr("SimStatistic.Thresholds.Count.AllOK"),ok));
			} else {
				if (ok==1) {
					addLine(Language.tr("SimStatistic.Thresholds.Count.OneOK"));
				} else {
					addLine(String.format(Language.tr("SimStatistic.Thresholds.Count.Number"),ok));
				}
			}
		}
		if (!fullInformation) endParagraph();
	}

	@Override
	protected void buildText() {
		switch (mode) {
		case MODE_BASE_INFORMATION : buildBaseInformation(); break;
		case MODE_SYSTEM_INFORMATION : buildSystemInformation(); break;
		case MODE_CONFIDENCE_INFORMATION: buildConfidenceInformation(); break;
		case MODE_MODEL_INFORMATION: buildModelInformation(); break;
		case MODE_THRESHOLDS: buildThresholdAndRecommendationsInformation(); break;
		}
	}

	/* (non-Javadoc)
	 * @see complexcallcenter.statistic.core.viewers.StatisticViewer#ownSettingsName()
	 */
	@Override
	public String ownSettingsName() {
		return (mode==Mode.MODE_BASE_INFORMATION || mode==Mode.MODE_CONFIDENCE_INFORMATION)?Language.tr("Statistic.Filter.Title"):null;
	}

	/* (non-Javadoc)
	 * @see complexcallcenter.statistic.core.viewers.StatisticViewer#ownSettingsIcon()
	 */
	@Override
	public Icon ownSettingsIcon() {
		return (mode==Mode.MODE_BASE_INFORMATION || mode==Mode.MODE_CONFIDENCE_INFORMATION)?Images.STATISTICS_FILTER.getIcon():null;
	}

	/** Filter-ID für Sichtbarkeit von Erstanrufern */
	private final static int FILTER_OVERVIEW_CLIENTS_FRESHCALLS=0;
	/** Filter-ID für Sichtbarkeit von Wiederanrufern */
	private final static int FILTER_OVERVIEW_CLIENTS_RECALL=1;
	/** Filter-ID für Sichtbarkeit von Anrufern */
	private final static int FILTER_OVERVIEW_CLIENTS_CALLS=2;
	/** Filter-ID für Sichtbarkeit von Erreichbarkeit auf Anrufbasis */
	private final static int FILTER_OVERVIEW_CLIENTS_ACCESSIBILITY_CALLS=3;
	/** Filter-ID für Sichtbarkeit von Erreichbarkeit auf Kundenbasis */
	private final static int FILTER_OVERVIEW_CLIENTS_ACCESSIBILITY_CLIENTS=4;
	/** Filter-ID für Sichtbarkeit von Kunden-Wartezeiten */
	private final static int FILTER_OVERVIEW_CLIENTS_WAITINGTIME=5;
	/** Filter-ID für Sichtbarkeit von Kunden-Abbruchzeiten */
	private final static int FILTER_OVERVIEW_CLIENTS_CANCELTIME=6;
	/** Filter-ID für Sichtbarkeit von Kunden-Weiterleitungen */
	private final static int FILTER_OVERVIEW_CLIENTS_FORWARDED=7;
	/** Filter-ID für Sichtbarkeit von Kunden-Blockierungen */
	private final static int FILTER_OVERVIEW_CLIENTS_BLOCKED=8;
	/** Filter-ID für Sichtbarkeit von Service-Level-Angabe über für erfolgreichen Anrufe */
	private final static int FILTER_OVERVIEW_CLIENTS_SERVICELEVEL_CALL_SUCCESS=9;
	/** Filter-ID für Sichtbarkeit von Service-Level-Angabe über für erfolgreichen Kunden */
	private final static int FILTER_OVERVIEW_CLIENTS_SERVICELEVEL_CLIENT_SUCCESS=10;
	/** Filter-ID für Sichtbarkeit von Service-Level-Angabe über für alle Anrufe */
	private final static int FILTER_OVERVIEW_CLIENTS_SERVICELEVEL_CALL_ALL=11;
	/** Filter-ID für Sichtbarkeit von Service-Level-Angabe über für alle Kunden */
	private final static int FILTER_OVERVIEW_CLIENTS_SERVICELEVEL_CLIENT_ALL=12;
	/** Filter-ID für Sichtbarkeit von Warteschlangenlängenangaben */
	private final static int FILTER_OVERVIEW_QUEUE=13;
	/** Filter-ID für Sichtbarkeit von Agentenanzahlen */
	private final static int FILTER_OVERVIEW_AGENTS_NUMBER=14;
	/** Filter-ID für Sichtbarkeit von Agentenanzahlen im Leerlauf */
	private final static int FILTER_OVERVIEW_AGENTS_IDLE=15;
	/** Filter-ID für Sichtbarkeit von Agentenanzahlen in technischer Bereitzeit */
	private final static int FILTER_OVERVIEW_AGENTS_TECHNICAL=16;
	/** Filter-ID für Sichtbarkeit von Agentenanzahlen im Bedienung */
	private final static int FILTER_OVERVIEW_AGENTS_HOLDING=17;
	/** Filter-ID für Sichtbarkeit von Agentenanzahlen im Nachbearbeitungszeit */
	private final static int FILTER_OVERVIEW_AGENTS_POSTPROCESSING=18;
	/** Filter-ID für Sichtbarkeit von Agentenanzahlen (eingeplant) */
	private final static int FILTER_OVERVIEW_AGENTS_WORK_SCHEDULED=19;
	/** Filter-ID für Sichtbarkeit von Agentenanzahlen (eingeplant und arbeitend) */
	private final static int FILTER_OVERVIEW_AGENTS_WORK_WORKED=20;
	/** Filter-ID für Sichtbarkeit von Schwellenwert-Angaben */
	private final static int FILTER_OVERVIEW_THRESHOLDS=21;

	/**
	 * Liefert die Bezeichner für die Filter-IDs für die Übersichtsabschnitte.
	 * @return	Bezeichner für die Filter-IDs für die Übersichtsabschnitte
	 */
	private String[] getOverviewIDs() {
		String[] ids=new String[]{
				Language.tr("SimStatistic.Clients")+" - "+Language.tr("SimStatistic.FreshCalls"),
				Language.tr("SimStatistic.Clients")+" - "+Language.tr("SimStatistic.RecallingClients.Info"),
				Language.tr("SimStatistic.Clients")+" - "+Language.tr("SimStatistic.Calls.Info"),
				Language.tr("SimStatistic.Clients")+" - "+Language.tr("SimStatistic.Accessibility")+" ("+Language.tr("SimStatistic.OnCallBasis")+")",
				Language.tr("SimStatistic.Clients")+" - "+Language.tr("SimStatistic.Accessibility")+" ("+Language.tr("SimStatistic.OnClientBasis")+")",
				Language.tr("SimStatistic.Clients")+" - "+Language.tr("SimStatistic.AverageWaitingTime"),
				Language.tr("SimStatistic.Clients")+" - "+Language.tr("SimStatistic.AverageCancelTime"),
				Language.tr("SimStatistic.Clients")+" - "+Language.tr("SimStatistic.ForwardedCallsPart"),
				Language.tr("SimStatistic.Clients")+" - "+Language.tr("SimStatistic.BlockedCallsPart"),
				Language.tr("SimStatistic.Clients")+" - "+Language.tr("SimStatistic.ServiceLevel")+" "+Language.tr("SimStatistic.OnCallBasis")+" ("+Language.tr("SimStatistic.CalculatedOn.SuccessfulCalls")+")",
				Language.tr("SimStatistic.Clients")+" - "+Language.tr("SimStatistic.ServiceLevel")+" "+Language.tr("SimStatistic.OnClientBasis")+" ("+Language.tr("SimStatistic.CalculatedOn.SuccessfulClients")+")",
				Language.tr("SimStatistic.Clients")+" - "+Language.tr("SimStatistic.ServiceLevel")+" "+Language.tr("SimStatistic.OnCallBasis")+" ("+Language.tr("SimStatistic.CalculatedOn.AllCalls")+")",
				Language.tr("SimStatistic.Clients")+" - "+Language.tr("SimStatistic.ServiceLevel")+" "+Language.tr("SimStatistic.OnClientBasis")+" ("+Language.tr("SimStatistic.CalculatedOn.AllClients")+")",
				Language.tr("SimStatistic.AverageQueueLength"),
				Language.tr("SimStatistic.Agents")+" - "+Language.tr("SimStatistic.Number"),
				Language.tr("SimStatistic.Agents")+" - "+Language.tr("SimStatistic.IdleTime"),
				Language.tr("SimStatistic.Agents")+" - "+Language.tr("SimStatistic.TechnicalFreeTime"),
				Language.tr("SimStatistic.Agents")+" - "+Language.tr("SimStatistic.HoldingTime"),
				Language.tr("SimStatistic.Agents")+" - "+Language.tr("SimStatistic.PostProcessingTime"),
				Language.tr("SimStatistic.Agents")+" - "+Language.tr("SimStatistic.AgentsWorkingHours.Scheduled"),
				Language.tr("SimStatistic.Agents")+" - "+Language.tr("SimStatistic.AgentsWorkingHours.Worked"),
				Language.tr("SimStatistic.Thresholds")
		};
		return ids;
	}

	/** Filter-ID für Sichtbarkeit von Konfidenzintervallen für die Erreichbarkeit auf Anrufbasis */
	private final static int FILTER_CONFIDENCE_ACCESSIBILITY_CALLS=0;
	/** Filter-ID für Sichtbarkeit von Konfidenzintervallen für die Erreichbarkeit auf Kundenbasis */
	private final static int FILTER_CONFIDENCE_ACCESSIBILITY_CLIENTS=1;
	/** Filter-ID für Sichtbarkeit von Konfidenzintervallen für die Wartezeiten */
	private final static int FILTER_CONFIDENCE_WAITINGTIME=2;
	/** Filter-ID für Sichtbarkeit von Konfidenzintervallen für den Service-Level über die erfolgreichen Anrufe */
	private final static int FILTER_CONFIDENCE_SERVICELEVEL_CALL_SUCCESS=3;
	/** Filter-ID für Sichtbarkeit von Konfidenzintervallen für den Service-Level über die erfolgreichen Kunden */
	private final static int FILTER_CONFIDENCE_SERVICELEVEL_CLIENT_SUCCESS=4;
	/** Filter-ID für Sichtbarkeit von Konfidenzintervallen für den Service-Level über die alle Anrufe */
	private final static int FILTER_CONFIDENCE_SERVICELEVEL_CALL_ALL=5;
	/** Filter-ID für Sichtbarkeit von Konfidenzintervallen für den Service-Level über die alle Kunden */
	private final static int FILTER_CONFIDENCE_SERVICELEVEL_CLIENT_ALL=6;

	/**
	 * Liefert die Bezeichner für die Filter-IDs für die Konfidenzintervalle.
	 * @return	Bezeichner für die Filter-IDs für die Konfidenzintervalle
	 */
	private String[] getConfidenceIDs() {
		String[] ids=new String[]{
				Language.tr("SimStatistic.Accessibility")+" ("+Language.tr("SimStatistic.OnCallBasis")+")",
				Language.tr("SimStatistic.Accessibility")+" ("+Language.tr("SimStatistic.OnClientBasis")+")",
				Language.tr("SimStatistic.WaitingTime"),
				Language.tr("SimStatistic.ServiceLevel")+" ("+Language.tr("SimStatistic.OnCallBasis")+", "+Language.tr("SimStatistic.CalculatedOn.SuccessfulCalls")+")",
				Language.tr("SimStatistic.ServiceLevel")+" ("+Language.tr("SimStatistic.OnCallBasis")+", "+Language.tr("SimStatistic.CalculatedOn.AllCalls")+")",
				Language.tr("SimStatistic.ServiceLevel")+" ("+Language.tr("SimStatistic.OnClientBasis")+", "+Language.tr("SimStatistic.CalculatedOn.SuccessfulClients")+")",
				Language.tr("SimStatistic.ServiceLevel")+" ("+Language.tr("SimStatistic.OnClientBasis")+", "+Language.tr("SimStatistic.CalculatedOn.AllClients")+")"
		};
		return ids;
	}

	/* (non-Javadoc)
	 * @see complexcallcenter.statistic.core.viewers.StatisticViewer#ownSettings()
	 */
	@Override
	public boolean ownSettings(JPanel owner) {
		Container c=owner; while (!(c instanceof Window) && c!=null) c=c.getParent();

		SetupData setup;
		String[] ids;
		StatisticFilterDialog dialog;

		switch (mode) {
		case MODE_BASE_INFORMATION:
			setup=SetupData.getSetup();
			ids=getOverviewIDs();
			dialog=new StatisticFilterDialog((Window)c,Language.tr("SimStatistic.ResultOverview.FilterTitle"),ids,getHiddenIDs(ids,setup.simOverviewFilter),()->Help.topicModal(owner,"Statistik"));
			dialog.setVisible(true);
			if (dialog.getClosedBy()!=BaseEditDialog.CLOSED_BY_OK) return false;
			setup.simOverviewFilter=setHiddenIDs(ids,dialog.getHiddenIDs());
			setup.saveSetup();
			return true;
		case MODE_CONFIDENCE_INFORMATION:
			setup=SetupData.getSetup();
			ids=getConfidenceIDs();
			dialog=new StatisticFilterDialog((Window)c,Language.tr("SimStatistic.ConfidenceIntervals.FilterTitle"),ids,getHiddenIDs(ids,setup.confidenceFilter),()->Help.topicModal(owner,"Statistik"));
			dialog.setVisible(true);
			if (dialog.getClosedBy()!=BaseEditDialog.CLOSED_BY_OK) return false;
			setup.confidenceFilter=setHiddenIDs(ids,dialog.getHiddenIDs());
			setup.saveSetup();
			return true;
		default:
			return false;
		}
	}

	/**
	 * Prüft, ob ein bestimmter Absatz sichtbar sein soll
	 * @param setup	Einstellungen zu sichtbaren/ausgeblendeten Absätzen
	 * @param id	ID des zu prüfenden Absatzes
	 * @return	Liefert <code>true</code>, wenn der Absatz sichtbar sein soll
	 */
	private boolean isIDVisible(String setup, int id) {
		while (setup.length()<id+1) setup+="X";
		boolean b=setup.charAt(id)!='-';
		return b;
	}

	/**
	 * Liefert eine Aufstellung der momentan ausgeblendeten Absatz-IDs.
	 * @param ids	Gesamtliste der Absatz-IDs.
	 * @param setup	Einstellungen zu sichtbaren/ausgeblendeten Absätzen
	 * @return	Aufstellung der momentan ausgeblendeten Absatz-IDs
	 */
	private String[] getHiddenIDs(String[] ids, String setup) {
		if (setup==null) setup="";
		while (setup.length()<ids.length) setup+="X";

		final List<String> hiddenIDs=new ArrayList<String>();
		for (int i=0;i<ids.length;i++) if (setup.charAt(i)=='-') hiddenIDs.add(ids[i]);
		return hiddenIDs.toArray(new String[0]);
	}

	/**
	 * Liefert die Einstellungen zu sichtbaren/ausgeblendeten Absätzen
	 * @param ids	Gesamtliste der Absatz-IDs.
	 * @param hidden	Aufstellung der momentan ausgeblendeten Absatz-IDs
	 * @return	Einstellungen zu sichtbaren/ausgeblendeten Absätzen
	 */
	private String setHiddenIDs(String[] ids, String[] hidden) {
		final List<String> hiddenIDs=Arrays.asList(hidden);
		StringBuilder setupString=new StringBuilder();
		for (int i=0;i<ids.length;i++) if (hiddenIDs.contains(ids[i])) setupString.append('-'); else setupString.append('X');
		return setupString.toString();
	}
}
