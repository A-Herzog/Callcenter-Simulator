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
package ui.statistic.optimizer;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import language.Language;
import mathtools.NumberTools;
import mathtools.Table;
import mathtools.TimeTools;
import simulator.Statistics;
import simulator.Statistics.AgentenDaten;
import simulator.Statistics.KundenDaten;
import systemtools.statistics.StatisticViewerTable;
import ui.optimizer.OptimizeData;
import ui.statistic.model.StatisticViewerErlangCTools;

/**
 * Zeigt verschiedene Optimierer-Statistikergebnisse als Tabelle an.
 * @author Alexander Herzog
 * @version 1.0
 */
public class StatisticViewerOptimizerTable extends StatisticViewerTable {
	/** Optimierungsergebnisobjekt dem die Optimierungsergebnisse entnommen werden sollen */
	private final OptimizeData results;
	/** Darstellungsart, siehe <code>DATA_TYPE_*</code> Konstanten. */
	private Mode dataType;
	/** Zusätzliche Angaben zur Darstellungsart, z.B. Nummer des Kundentyps */
	private final int dataNr;

	/**
	 * Welche Informationen sollen angezeigt werden?
	 * @author Alexander Herzog
	 * @see StatisticViewerOptimizerTable#StatisticViewerOptimizerTable(OptimizeData, Mode)
	 * @see StatisticViewerOptimizerTable#StatisticViewerOptimizerTable(OptimizeData, Mode, int)
	 */
	public enum Mode {
		/**
		 * Zeigt die Veränderung der Anruferzahlen an.
		 */
		DATA_TYPE_CALLERS,

		/**
		 * Zeigt die Veränderung der Erreichbarkeit an.
		 */
		DATA_TYPE_SUCCESS,

		/**
		 * Zeigt die Veränderung der Abbrecherzahlen an.
		 */
		DATA_TYPE_CANCEL,

		/**
		 * Zeigt die Veränderung der Wartezeiten an.
		 */
		DATA_TYPE_WAITING_TIME,

		/**
		 * Zeigt die Veränderung der Verweilzeiten an.
		 */
		DATA_TYPE_STAYING_TIME,

		/**
		 * Zeigt die Veränderung des Service-Level auf Anrufbasis an (bezogen auf erfolgreiche Anrufe).
		 */
		DATA_TYPE_SERVICE_LEVEL_CALLS,

		/**
		 * Zeigt die Veränderung des Service-Level auf Kundenbasis an (bezogen auf erfolgreiche Kunden).
		 */
		DATA_TYPE_SERVICE_LEVEL_CLIENTS,

		/**
		 * Zeigt die Veränderung des Service-Level auf Anrufbasis an (bezogen auf alle Anrufe).
		 */
		DATA_TYPE_SERVICE_LEVEL_CALLS_ALL,

		/**
		 * Zeigt die Veränderung des Service-Level auf Kundenbasis an (bezogen auf alle Kunden).
		 */
		DATA_TYPE_SERVICE_LEVEL_CLIENTS_ALL,

		/**
		 * Zeigt die Veränderung der Agentenzahlen pro Callcenter an.
		 */
		DATA_TYPE_AGENTS_CALLCENTER,

		/**
		 * Zeigt die Veränderung der Agentenzahlen pro Skill-Level an.
		 */
		DATA_TYPE_AGENTS_SKILL_LEVEL,

		/**
		 * Zeigt die Veränderung der Auslastung pro Callcenter an.
		 */
		DATA_TYPE_WORKLOAD_CALLCENTER,

		/**
		 * Zeigt die Veränderung der Auslastung pro Skill-Level an.
		 */
		DATA_TYPE_WORKLOAD_SKILL_LEVEL,

		/**
		 * Zeigt die Kosten und Erträge pro Kundentyp an.
		 */
		DATA_TYPE_CALLER_COSTS,

		/**
		 * Zeigt die Kosten und Erträge pro Skill-Level an.
		 */
		DATA_TYPE_SKILL_LEVEL_COSTS,

		/**
		 * Zeigt die Gesamtkosten und Erträge an.
		 */
		DATA_TYPE_COSTS,

		/**
		 * Zeigt eine Übersichtstabelle an.
		 */
		DATA_TYPE_SUMMARY,

		/**
		 * Erlang-C-Vergleich in Bezug auf die Erreichbarkeit.
		 */
		DATA_TYPE_ERLANGC_SUCCESS,

		/**
		 * Erlang-C-Vergleich in Bezug auf die mittlere Wartezeit.
		 */
		DATA_TYPE_ERLANGC_WAITING_TIME,

		/**
		 * Erlang-C-Vergleich in Bezug auf den Service-Level.
		 */
		DATA_TYPE_ERLANGC_SERVICE_LEVEL
	}

	/**
	 * Konstruktor der Klasse <code>StatisticViewerOptimizerTable</code>
	 * @param results	Optimierungsergebnisobjekt dem die Optimierungsergebnisse entnommen werden sollen
	 * @param dataType	Darstellungsart, siehe <code>DATA_TYPE_*</code> Konstanten.
	 * @param dataNr	Zusätzliche Angaben zur Darstellungsart, z.B. Nummer des Kundentyps
	 */
	public StatisticViewerOptimizerTable(OptimizeData results, Mode dataType, int dataNr) {
		super();
		this.results=results;
		this.dataType=dataType;
		this.dataNr=dataNr;
	}

	/**
	 * Konstruktor der Klasse <code>StatisticViewerOptimizerTable</code>
	 * @param results	Objekt vom Typ <code>OptimizeData</code>, dem die Optimierungsergebnisse entnommen werden sollen
	 * @param dataType	Darstellungsart, siehe <code>DATA_TYPE_*</code> Konstanten.
	 */
	public StatisticViewerOptimizerTable(OptimizeData results, Mode dataType) {
		this(results,dataType,-1);
	}

	@Override
	protected void buildTable() {
		setData(getTableData(),getColNames());
	}

	/**
	 * Liefert Spaltennamen aus {@link #getColNames()} aber ohne die Simulationstage-Spalte
	 * @param extraCaption	Zusätzlicher Vorspann für die Spaltennamen
	 * @return	Spaltennamen
	 */
	private	List<String> getColParts(String extraCaption) {
		List<String> cols=new ArrayList<>(Arrays.asList(getColNames()));
		cols.remove(0);
		for (int i=0;i<cols.size();i++) cols.set(i,extraCaption+" "+cols.get(i));
		return cols;
	}

	/**
	 * Liefert die Namen für die Spaltenüberschriften.
	 * @return	Namen für die Spaltenüberschriften
	 */
	private String[] getColNames() {
		int dataCols=0;

		switch (dataType) {
		case DATA_TYPE_CALLERS:
		case DATA_TYPE_SUCCESS:
		case DATA_TYPE_CANCEL:
			dataCols=2*(1+results.data.get(0).kundenProTyp.length);
			break;
		case DATA_TYPE_WAITING_TIME:
		case DATA_TYPE_STAYING_TIME:
		case DATA_TYPE_SERVICE_LEVEL_CALLS:
		case DATA_TYPE_SERVICE_LEVEL_CLIENTS:
		case DATA_TYPE_SERVICE_LEVEL_CALLS_ALL:
		case DATA_TYPE_SERVICE_LEVEL_CLIENTS_ALL:
			dataCols=1+results.data.get(0).kundenProTyp.length;
			break;
		case DATA_TYPE_AGENTS_CALLCENTER:
			dataCols=5*(1+results.data.get(0).agentenProCallcenter.length);
			break;
		case DATA_TYPE_AGENTS_SKILL_LEVEL:
			dataCols=5*(1+results.data.get(0).agentenProSkilllevel.length);
			break;
		case DATA_TYPE_WORKLOAD_CALLCENTER:
			dataCols=1+results.data.get(0).agentenProCallcenter.length;
			break;
		case DATA_TYPE_WORKLOAD_SKILL_LEVEL:
			dataCols=1+results.data.get(0).agentenProSkilllevel.length;
			break;
		case DATA_TYPE_CALLER_COSTS:
			dataCols=4;
			break;
		case DATA_TYPE_SKILL_LEVEL_COSTS:
			dataCols=4;
			break;
		case DATA_TYPE_COSTS:
			dataCols=3;
			break;
		case DATA_TYPE_SUMMARY:
			dataCols=0;
			break;
		case DATA_TYPE_ERLANGC_SUCCESS:
		case DATA_TYPE_ERLANGC_WAITING_TIME:
		case DATA_TYPE_ERLANGC_SERVICE_LEVEL:
			dataCols=5;
			break;
		}

		String[] cols=new String[1+dataCols];
		cols[0]=Language.tr("SimStatistic.SimulationRun");

		switch (dataType) {
		case DATA_TYPE_CALLERS:
			for (int i=0;i<results.data.get(0).kundenProTyp.length;i++) {
				String s=results.data.get(0).kundenProTyp[i].name;
				cols[2*i+1]=s+" ("+Language.tr("SimStatistic.FreshCalls")+")";
				cols[2*i+2]=s+" ("+Language.tr("SimStatistic.Caller.Total")+")";
			}
			cols[cols.length-2]=Language.tr("SimStatistic.AllClients")+" ("+Language.tr("SimStatistic.FreshCalls")+")";
			cols[cols.length-1]=Language.tr("SimStatistic.AllClients")+" ("+Language.tr("SimStatistic.Caller.Total")+")";
			break;
		case DATA_TYPE_SUCCESS:
		case DATA_TYPE_CANCEL:
			for (int i=0;i<results.data.get(0).kundenProTyp.length;i++) {
				String s=results.data.get(0).kundenProTyp[i].name;
				cols[2*i+1]=s+" ("+Language.tr("SimStatistic.OnClientBasis")+")";
				cols[2*i+2]=s+" ("+Language.tr("SimStatistic.OnCallBasis")+")";
			}
			cols[cols.length-2]=Language.tr("SimStatistic.AllClients")+" ("+Language.tr("SimStatistic.OnClientBasis")+")";
			cols[cols.length-1]=Language.tr("SimStatistic.AllClients")+" ("+Language.tr("SimStatistic.OnCallBasis")+")";
			break;
		case DATA_TYPE_WAITING_TIME:
		case DATA_TYPE_STAYING_TIME:
		case DATA_TYPE_SERVICE_LEVEL_CALLS:
		case DATA_TYPE_SERVICE_LEVEL_CLIENTS:
		case DATA_TYPE_SERVICE_LEVEL_CALLS_ALL:
		case DATA_TYPE_SERVICE_LEVEL_CLIENTS_ALL:
			for (int i=0;i<results.data.get(0).kundenProTyp.length;i++) cols[i+1]=results.data.get(0).kundenProTyp[i].name;
			cols[cols.length-1]=Language.tr("SimStatistic.AllClients");
			break;
		case DATA_TYPE_AGENTS_CALLCENTER:
			for (int i=0;i<results.data.get(0).agentenProCallcenter.length;i++) {
				String s=results.data.get(0).agentenProCallcenter[i].name;
				cols[5*i+1]=s+" ("+Language.tr("SimStatistic.IdleTime")+")";
				cols[5*i+2]=s+" ("+Language.tr("SimStatistic.TechnicalFreeTime")+")";
				cols[5*i+3]=s+" ("+Language.tr("SimStatistic.HoldingTime")+")";
				cols[5*i+4]=s+" ("+Language.tr("SimStatistic.PostProcessingTime")+")";
				cols[5*i+5]=s+" ("+Language.tr("Statistic.Total")+")";
			}
			cols[cols.length-5]=Language.tr("SimStatistic.AllCallcenter")+" ("+Language.tr("SimStatistic.IdleTime")+")";
			cols[cols.length-4]=Language.tr("SimStatistic.AllCallcenter")+" ("+Language.tr("SimStatistic.TechnicalFreeTime")+")";
			cols[cols.length-3]=Language.tr("SimStatistic.AllCallcenter")+" ("+Language.tr("SimStatistic.HoldingTime")+")";
			cols[cols.length-2]=Language.tr("SimStatistic.AllCallcenter")+" ("+Language.tr("SimStatistic.PostProcessingTime")+")";
			cols[cols.length-1]=Language.tr("SimStatistic.AllCallcenter")+" ("+Language.tr("Statistic.Total")+")";
			break;
		case DATA_TYPE_AGENTS_SKILL_LEVEL:
			for (int i=0;i<results.data.get(0).agentenProSkilllevel.length;i++) {
				String s=results.data.get(0).agentenProSkilllevel[i].name;
				cols[5*i+1]=s+" ("+Language.tr("SimStatistic.IdleTime")+")";
				cols[5*i+2]=s+" ("+Language.tr("SimStatistic.TechnicalFreeTime")+")";
				cols[5*i+3]=s+" ("+Language.tr("SimStatistic.HoldingTime")+")";
				cols[5*i+4]=s+" ("+Language.tr("SimStatistic.PostProcessingTime")+")";
				cols[5*i+5]=s+" ("+Language.tr("Statistic.Total")+")";
			}
			cols[cols.length-5]=Language.tr("SimStatistic.AllCallcenter")+" ("+Language.tr("SimStatistic.IdleTime")+")";
			cols[cols.length-4]=Language.tr("SimStatistic.AllCallcenter")+" ("+Language.tr("SimStatistic.TechnicalFreeTime")+")";
			cols[cols.length-3]=Language.tr("SimStatistic.AllCallcenter")+" ("+Language.tr("SimStatistic.HoldingTime")+")";
			cols[cols.length-2]=Language.tr("SimStatistic.AllCallcenter")+" ("+Language.tr("SimStatistic.PostProcessingTime")+")";
			cols[cols.length-1]=Language.tr("SimStatistic.AllCallcenter")+" ("+Language.tr("Statistic.Total")+")";
			break;
		case DATA_TYPE_WORKLOAD_CALLCENTER:
			for (int i=0;i<results.data.get(0).agentenProCallcenter.length;i++) cols[i+1]=results.data.get(0).agentenProCallcenter[i].name;
			cols[cols.length-1]=Language.tr("SimStatistic.AllCallcenter");
			break;
		case DATA_TYPE_WORKLOAD_SKILL_LEVEL:
			for (int i=0;i<results.data.get(0).agentenProSkilllevel.length;i++) cols[i+1]=results.data.get(0).agentenProSkilllevel[i].name;
			cols[cols.length-1]=Language.tr("SimStatistic.AllSkillLevel");
			break;
		case DATA_TYPE_CALLER_COSTS:
			cols[1]=Language.tr("SimStatistic.Yield.Clients");
			cols[2]=Language.tr("SimStatistic.Costs.Cancel");
			cols[3]=Language.tr("SimStatistic.Costs.WaitingTime");
			cols[4]=Language.tr("Statistic.Total");
			break;
		case DATA_TYPE_SKILL_LEVEL_COSTS:
			cols[1]=Language.tr("SimStatistic.Costs.Wage");
			cols[2]=Language.tr("SimStatistic.Costs.HoldingTimes");
			cols[3]=Language.tr("SimStatistic.Costs.HoldingAndPostProcessingTimes");
			cols[4]=Language.tr("Statistic.Total");
			break;
		case DATA_TYPE_COSTS:
			cols[1]=Language.tr("SimStatistic.Yield");
			cols[2]=Language.tr("SimStatistic.Costs");
			cols[3]=Language.tr("Statistic.Total");
			break;
		case DATA_TYPE_SUMMARY:
			List<String> tempCol=new ArrayList<>(Arrays.asList(cols));
			dataType=Mode.DATA_TYPE_CALLERS;	tempCol.addAll(getColParts(Language.tr("SimStatistic.NumberOfCallers")));
			dataType=Mode.DATA_TYPE_SUCCESS; tempCol.addAll(getColParts(Language.tr("SimStatistic.Accessibility")));
			dataType=Mode.DATA_TYPE_CANCEL; tempCol.addAll(getColParts(Language.tr("SimStatistic.NumberOfCancelations")));
			dataType=Mode.DATA_TYPE_WAITING_TIME; tempCol.addAll(getColParts(Language.tr("SimStatistic.AverageWaitingTime")));
			dataType=Mode.DATA_TYPE_STAYING_TIME; tempCol.addAll(getColParts(Language.tr("SimStatistic.AverageResidenceTime")));
			dataType=Mode.DATA_TYPE_SERVICE_LEVEL_CALLS; tempCol.addAll(getColParts(Language.tr("SimStatistic.ServiceLevel")+" ("+Language.tr("SimStatistic.CalculatedOn.SuccessfulCalls")+")"));
			dataType=Mode.DATA_TYPE_SERVICE_LEVEL_CLIENTS; tempCol.addAll(getColParts(Language.tr("SimStatistic.ServiceLevel")+" ("+Language.tr("SimStatistic.CalculatedOn.SuccessfulClients")+")"));
			dataType=Mode.DATA_TYPE_SERVICE_LEVEL_CALLS_ALL; tempCol.addAll(getColParts(Language.tr("SimStatistic.ServiceLevel")+" ("+Language.tr("SimStatistic.CalculatedOn.AllCalls")+")"));
			dataType=Mode.DATA_TYPE_SERVICE_LEVEL_CLIENTS_ALL; tempCol.addAll(getColParts(Language.tr("SimStatistic.ServiceLevel")+" ("+Language.tr("SimStatistic.CalculatedOn.AllClients")+")"));
			dataType=Mode.DATA_TYPE_COSTS; tempCol.addAll(getColParts(Language.tr("SimStatistic.Costs")));
			dataType=Mode.DATA_TYPE_AGENTS_CALLCENTER; tempCol.addAll(getColParts(Language.tr("SimStatistic.Agents.PerCallcenter")));
			dataType=Mode.DATA_TYPE_AGENTS_SKILL_LEVEL; tempCol.addAll(getColParts(Language.tr("SimStatistic.Agents.PerSkillLevel")));
			dataType=Mode.DATA_TYPE_WORKLOAD_CALLCENTER; tempCol.addAll(getColParts(Language.tr("SimStatistic.WorkLoad.PerCallcenter")));
			dataType=Mode.DATA_TYPE_WORKLOAD_SKILL_LEVEL; tempCol.addAll(getColParts(Language.tr("SimStatistic.WorkLoad.PerSkillLevel")));
			cols=tempCol.toArray(String[]::new);
			dataType=Mode.DATA_TYPE_SUMMARY;
			break;
		case DATA_TYPE_ERLANGC_SUCCESS:
			cols[1]=Language.tr("SimStatistic.Accessibility")+" ("+Language.tr("SimStatistic.Type.ErlangCSimple")+")";
			cols[2]=Language.tr("SimStatistic.Accessibility")+" ("+Language.tr("SimStatistic.Type.ErlangCComplex")+")";
			cols[3]=Language.tr("SimStatistic.Accessibility")+" ("+Language.tr("SimStatistic.Type.Simulation")+")";
			cols[4]=Language.tr("SimStatistic.Error")+" "+Language.tr("SimStatistic.Type.ErlangCSimple")+" - "+Language.tr("SimStatistic.Type.Simulation");
			cols[5]=Language.tr("SimStatistic.Error")+" "+Language.tr("SimStatistic.Type.ErlangCComplex")+" - "+Language.tr("SimStatistic.Type.Simulation");
			break;
		case DATA_TYPE_ERLANGC_WAITING_TIME:
			cols[1]=Language.tr("SimStatistic.AverageWaitingTime")+" ("+Language.tr("SimStatistic.Type.ErlangCSimple")+")";
			cols[2]=Language.tr("SimStatistic.AverageWaitingTime")+" ("+Language.tr("SimStatistic.Type.ErlangCComplex")+")";
			cols[3]=Language.tr("SimStatistic.AverageWaitingTime")+" ("+Language.tr("SimStatistic.Type.Simulation")+")";
			cols[4]=Language.tr("SimStatistic.Error")+" "+Language.tr("SimStatistic.Type.ErlangCSimple")+" - "+Language.tr("SimStatistic.Type.Simulation");
			cols[5]=Language.tr("SimStatistic.Error")+" "+Language.tr("SimStatistic.Type.ErlangCComplex")+" - "+Language.tr("SimStatistic.Type.Simulation");
			break;
		case DATA_TYPE_ERLANGC_SERVICE_LEVEL:
			cols[1]=Language.tr("SimStatistic.ServiceLevel")+" ("+Language.tr("SimStatistic.Type.ErlangCSimple")+")";
			cols[2]=Language.tr("SimStatistic.ServiceLevel")+" ("+Language.tr("SimStatistic.Type.ErlangCComplex")+")";
			cols[3]=Language.tr("SimStatistic.ServiceLevel")+" ("+Language.tr("SimStatistic.Type.Simulation")+")";
			cols[4]=Language.tr("SimStatistic.Error")+" "+Language.tr("SimStatistic.Type.ErlangCSimple")+" - "+Language.tr("SimStatistic.Type.Simulation");
			cols[5]=Language.tr("SimStatistic.Error")+" "+Language.tr("SimStatistic.Type.ErlangCComplex")+" - "+Language.tr("SimStatistic.Type.Simulation");
			break;
		}

		return cols;
	}

	/**
	 * Berechnet den mit den Anrufern pro Intervall gewichteten Mittelwert über eine Datenreihe
	 * @param data	Datenreihe
	 * @param statistic	Statistikobjekt dem die Informationen zu den Anzahlen an Anrufern pro Intervall entnommen werden sollen
	 * @return	Gewichteter Mittelwert der Datenreihe
	 */
	private double average(final double[] data, final Statistics statistic) {
		double count=0, sum=0;
		final double[] weights=statistic.kundenGlobal.anrufeProIntervall.densityData;
		for (int i=0;i<48;i++) {
			count+=weights[i];
			sum+=data[i]*weights[i];
		}
		return (count>0)?(sum/count):0;
	}

	/**
	 * Gibt die Daten zu einem Kundentyp aus.
	 * @param table	Ausgabetabelle
	 * @param nr	Nummer des Kundentyps oder -1 für globale Daten über alle
	 */
	private void addKundenLines(final Table table, final int nr) {
		final String[] row1=new String[results.data.size()];
		final String[] row2=new String[results.data.size()];
		final String[] row3=new String[results.data.size()];
		final String[] row4=new String[results.data.size()];
		final String[] row5=new String[results.data.size()];

		StatisticViewerErlangCTools erlangC1, erlangC2;
		double erlang1, erlang2, simValue;

		for (int i=0;i<results.data.size();i++) {
			Statistics statistic=results.data.get(i);
			KundenDaten kunden=(nr>=0)?statistic.kundenProTyp[nr]:statistic.kundenGlobal;

			switch (dataType) {
			case DATA_TYPE_CALLERS:
				row1[i]=NumberTools.formatNumber((double)(kunden.kunden+kunden.kundenWiederanruf)/statistic.simulationData.runRepeatCount);
				row2[i]=NumberTools.formatNumber((double)kunden.anrufe/statistic.simulationData.runRepeatCount);
				break;
			case DATA_TYPE_SUCCESS:
				row1[i]=NumberTools.formatPercent((double)kunden.kundenErfolg/(kunden.kunden+kunden.kundenWiederanruf));
				row2[i]=NumberTools.formatPercent((double)kunden.anrufeErfolg/kunden.anrufe);
				break;
			case DATA_TYPE_CANCEL:
				row1[i]=NumberTools.formatNumber((double)kunden.kundenAbbruch/statistic.simulationData.runRepeatCount);
				row2[i]=NumberTools.formatNumber((double)kunden.anrufeAbbruch/statistic.simulationData.runRepeatCount);
				break;
			case DATA_TYPE_WAITING_TIME:
				row1[i]=TimeTools.formatTime((int)(kunden.anrufeWartezeitSum/Math.max(1,kunden.anrufeErfolg)));
				break;
			case DATA_TYPE_STAYING_TIME:
				row1[i]=TimeTools.formatTime((int)(kunden.anrufeVerweilzeitSum/Math.max(1,kunden.anrufeErfolg)));
				break;
			case DATA_TYPE_SERVICE_LEVEL_CALLS:
				row1[i]=NumberTools.formatPercent((double)kunden.anrufeServicelevel/Math.max(1,kunden.anrufeErfolg));
				break;
			case DATA_TYPE_SERVICE_LEVEL_CLIENTS:
				row1[i]=NumberTools.formatPercent((double)kunden.kundenServicelevel/Math.max(1,kunden.kundenErfolg));
				break;
			case DATA_TYPE_SERVICE_LEVEL_CALLS_ALL:
				row1[i]=NumberTools.formatPercent((double)kunden.anrufeServicelevel/Math.max(1,kunden.anrufe));
				break;
			case DATA_TYPE_SERVICE_LEVEL_CLIENTS_ALL:
				row1[i]=NumberTools.formatPercent((double)kunden.kundenServicelevel/Math.max(1,kunden.kunden));
				break;
			case DATA_TYPE_CALLER_COSTS:
				row1[i]=NumberTools.formatNumber(kunden.revenue/statistic.simulationData.runRepeatCount);
				row2[i]=NumberTools.formatNumber(kunden.costCancel/statistic.simulationData.runRepeatCount);
				row3[i]=NumberTools.formatNumber(kunden.costWaiting/statistic.simulationData.runRepeatCount);
				row4[i]=NumberTools.formatNumber((kunden.revenue-kunden.costCancel-kunden.costWaiting)/statistic.simulationData.runRepeatCount);
				break;
			case DATA_TYPE_COSTS:
				row1[i]=NumberTools.formatNumber(statistic.kundenGlobal.revenue/statistic.simulationData.runRepeatCount);
				row2[i]=NumberTools.formatNumber((statistic.kundenGlobal.costCancel+statistic.kundenGlobal.costWaiting+statistic.agentenGlobal.costOfficeTime+statistic.agentenGlobal.costCalls+statistic.agentenGlobal.costProcessTime)/statistic.simulationData.runRepeatCount);
				row3[i]=NumberTools.formatNumber((statistic.kundenGlobal.revenue-(statistic.kundenGlobal.costCancel+statistic.kundenGlobal.costWaiting+statistic.agentenGlobal.costOfficeTime+statistic.agentenGlobal.costCalls+statistic.agentenGlobal.costProcessTime))/statistic.simulationData.runRepeatCount);
				break;
			case DATA_TYPE_ERLANGC_SUCCESS:
				erlangC1=new StatisticViewerErlangCTools(statistic.editModel,false);
				erlangC2=new StatisticViewerErlangCTools(statistic.editModel,true);
				erlang1=average(erlangC1.getSuccessProbability(),statistic);
				erlang2=average(erlangC2.getSuccessProbability(),statistic);
				simValue=(double)statistic.kundenGlobal.anrufeErfolg/statistic.kundenGlobal.anrufe;
				row1[i]=NumberTools.formatPercent(erlang1);
				row2[i]=NumberTools.formatPercent(erlang2);
				row3[i]=NumberTools.formatPercent(simValue);
				row4[i]=NumberTools.formatPercent(erlang1-simValue);
				row5[i]=NumberTools.formatPercent(erlang2-simValue);
				break;
			case DATA_TYPE_ERLANGC_WAITING_TIME:
				erlangC1=new StatisticViewerErlangCTools(statistic.editModel,false);
				erlangC2=new StatisticViewerErlangCTools(statistic.editModel,true);
				erlang1=average(erlangC1.getMeanWaitingTime(),statistic);
				erlang2=average(erlangC2.getMeanWaitingTime(),statistic);
				simValue=(double)statistic.kundenGlobal.anrufeWartezeitSum/Math.max(1,statistic.kundenGlobal.anrufeErfolg);
				row1[i]=TimeTools.formatTime((int)erlang1);
				row2[i]=TimeTools.formatTime((int)erlang2);
				row3[i]=TimeTools.formatTime((int)simValue);
				row4[i]=TimeTools.formatExactTime((int)(erlang1-simValue));
				row5[i]=TimeTools.formatExactTime((int)(erlang2-simValue));
				break;
			case DATA_TYPE_ERLANGC_SERVICE_LEVEL:
				erlangC1=new StatisticViewerErlangCTools(statistic.editModel,false);
				erlangC2=new StatisticViewerErlangCTools(statistic.editModel,true);
				erlang1=average(erlangC1.getServiceLevel(),statistic);
				erlang2=average(erlangC2.getServiceLevel(),statistic);
				simValue=(double)statistic.kundenGlobal.anrufeServicelevel/Math.max(1,statistic.kundenGlobal.anrufeErfolg);
				row1[i]=NumberTools.formatPercent(erlang1);
				row2[i]=NumberTools.formatPercent(erlang2);
				row3[i]=NumberTools.formatPercent(simValue);
				row4[i]=NumberTools.formatPercent(erlang1-simValue);
				row5[i]=NumberTools.formatPercent(erlang2-simValue);
				break;
			default:
				/* Keine Agenten-Daten */
				break;
			}
		}

		table.addLine(row1);
		if (dataType==Mode.DATA_TYPE_CALLERS || dataType==Mode.DATA_TYPE_SUCCESS || dataType==Mode.DATA_TYPE_CANCEL || dataType==Mode.DATA_TYPE_CALLER_COSTS || dataType==Mode.DATA_TYPE_COSTS || dataType==Mode.DATA_TYPE_ERLANGC_SUCCESS || dataType==Mode.DATA_TYPE_ERLANGC_WAITING_TIME || dataType==Mode.DATA_TYPE_ERLANGC_SERVICE_LEVEL) table.addLine(row2);
		if (dataType==Mode.DATA_TYPE_CALLER_COSTS || dataType==Mode.DATA_TYPE_COSTS || dataType==Mode.DATA_TYPE_ERLANGC_SUCCESS || dataType==Mode.DATA_TYPE_ERLANGC_WAITING_TIME || dataType==Mode.DATA_TYPE_ERLANGC_SERVICE_LEVEL) table.addLine(row3);
		if (dataType==Mode.DATA_TYPE_CALLER_COSTS || dataType==Mode.DATA_TYPE_ERLANGC_SUCCESS || dataType==Mode.DATA_TYPE_ERLANGC_WAITING_TIME || dataType==Mode.DATA_TYPE_ERLANGC_SERVICE_LEVEL) table.addLine(row4);
		if (dataType==Mode.DATA_TYPE_ERLANGC_SUCCESS || dataType==Mode.DATA_TYPE_ERLANGC_WAITING_TIME || dataType==Mode.DATA_TYPE_ERLANGC_SERVICE_LEVEL) table.addLine(row5);
	}

	/**
	 * Gibt die Daten zu einer Agentengruppe aus.
	 * @param table	Ausgabetabelle
	 * @param nr	Nummer der Agentengruppe in der pro Callcenter oder pro Skill-Lvel Liste
	 * @param proCallcenter	Ausgabe pro Callcenter (<code>true</code>) oder pro Skill-Level (<code>false</code>)
	 */
	private void addAgentenLines(final Table table, final int nr, final boolean proCallcenter) {
		String[] row1=new String[results.data.size()];
		String[] row2=new String[results.data.size()];
		String[] row3=new String[results.data.size()];
		String[] row4=new String[results.data.size()];
		String[] row5=new String[results.data.size()];

		for (int i=0;i<results.data.size();i++) {
			Statistics statistic=results.data.get(i);
			AgentenDaten agenten;
			if (proCallcenter) {
				agenten=(nr>=0)?statistic.agentenProCallcenter[nr]:statistic.agentenGlobal;
			} else {
				agenten=(nr>=0)?statistic.agentenProSkilllevel[nr]:statistic.agentenGlobal;
			}

			switch (dataType) {
			case DATA_TYPE_AGENTS_CALLCENTER:
			case DATA_TYPE_AGENTS_SKILL_LEVEL:
				row1[i]=NumberTools.formatNumber((double)agenten.leerlaufGesamt/statistic.simulationData.runRepeatCount/3600);
				row2[i]=NumberTools.formatNumber((double)agenten.technischerLeerlaufGesamt/statistic.simulationData.runRepeatCount/3600);
				row3[i]=NumberTools.formatNumber((double)agenten.arbeitGesamt/statistic.simulationData.runRepeatCount/3600);
				row4[i]=NumberTools.formatNumber((double)agenten.postProcessingGesamt/statistic.simulationData.runRepeatCount/3600);
				row5[i]=NumberTools.formatNumber((double)(agenten.leerlaufGesamt+agenten.technischerLeerlaufGesamt+agenten.arbeitGesamt+agenten.postProcessingGesamt)/statistic.simulationData.runRepeatCount/3600);
				break;
			case DATA_TYPE_WORKLOAD_CALLCENTER:
			case DATA_TYPE_WORKLOAD_SKILL_LEVEL:
				row1[i]=NumberTools.formatPercent((1-(double)agenten.leerlaufGesamt/(agenten.leerlaufGesamt+agenten.technischerLeerlaufGesamt+agenten.arbeitGesamt+agenten.postProcessingGesamt)));
				break;
			case DATA_TYPE_SKILL_LEVEL_COSTS:
				row1[i]=NumberTools.formatNumber(agenten.costOfficeTime/statistic.simulationData.runRepeatCount);
				row2[i]=NumberTools.formatNumber(agenten.costCalls/statistic.simulationData.runRepeatCount);
				row3[i]=NumberTools.formatNumber(agenten.costProcessTime/statistic.simulationData.runRepeatCount);
				row4[i]=NumberTools.formatNumber((agenten.costOfficeTime+agenten.costCalls+agenten.costProcessTime)/statistic.simulationData.runRepeatCount);
				break;
			default:
				/* Keine Kunden-Daten */
				break;
			}
		}

		table.addLine(row1);
		if (dataType==Mode.DATA_TYPE_AGENTS_CALLCENTER || dataType==Mode.DATA_TYPE_AGENTS_SKILL_LEVEL) {
			table.addLine(row2);
			table.addLine(row3);
			table.addLine(row4);
			table.addLine(row5);
		}
		if (dataType==Mode.DATA_TYPE_SKILL_LEVEL_COSTS) {
			table.addLine(row2);
			table.addLine(row3);
			table.addLine(row4);
		}
	}

	/**
	 * Fügt die Datenreihen zu der Ausgabetabelle hinzu.
	 * @param table	Ausgabetabelle
	 */
	private void addLines(final Table table) {
		switch (dataType) {
		case DATA_TYPE_CALLERS:
		case DATA_TYPE_SUCCESS:
		case DATA_TYPE_CANCEL:
		case DATA_TYPE_WAITING_TIME:
		case DATA_TYPE_STAYING_TIME:
		case DATA_TYPE_SERVICE_LEVEL_CALLS:
		case DATA_TYPE_SERVICE_LEVEL_CLIENTS:
		case DATA_TYPE_SERVICE_LEVEL_CALLS_ALL:
		case DATA_TYPE_SERVICE_LEVEL_CLIENTS_ALL:
		case DATA_TYPE_CALLER_COSTS:
			for (int i=0;i<results.data.get(0).kundenProTyp.length;i++) addKundenLines(table,i);
			addKundenLines(table,-1);
			break;
		case DATA_TYPE_AGENTS_CALLCENTER:
		case DATA_TYPE_WORKLOAD_CALLCENTER:
			for (int i=0;i<results.data.get(0).agentenProCallcenter.length;i++) addAgentenLines(table,i,true);
			addAgentenLines(table,-1,true);
			break;
		case DATA_TYPE_AGENTS_SKILL_LEVEL:
		case DATA_TYPE_WORKLOAD_SKILL_LEVEL:
			for (int i=0;i<results.data.get(0).agentenProSkilllevel.length;i++) addAgentenLines(table,i,false);
			addAgentenLines(table,-1,false);
			break;
		case DATA_TYPE_SKILL_LEVEL_COSTS:
			addAgentenLines(table,dataNr,false);
			break;
		case DATA_TYPE_COSTS:
			addKundenLines(table,-1);
			break;
		case DATA_TYPE_ERLANGC_SUCCESS:
		case DATA_TYPE_ERLANGC_WAITING_TIME:
		case DATA_TYPE_ERLANGC_SERVICE_LEVEL:
			addKundenLines(table,-1);
			break;
		case DATA_TYPE_SUMMARY:
			/* Kein Summary in Summary. */
			break;
		}
	}

	/**
	 * Erstellt und liefert die Tabelle mit den auszugebenden Daten.
	 * @return	Tabelle mit den auszugebenden Daten
	 */
	private Table getTableData() {
		Table table=new Table();

		/* Spalte 1 */
		String[] row=new String[results.data.size()];
		for (int i=0;i<row.length;i++) row[i]=""+(i+1);
		table.addLine(row);

		/* Eigentliche Daten */
		if (dataType!=Mode.DATA_TYPE_SUMMARY) {
			addLines(table);
		} else {
			dataType=Mode.DATA_TYPE_CALLERS; addLines(table);
			dataType=Mode.DATA_TYPE_SUCCESS; addLines(table);
			dataType=Mode.DATA_TYPE_CANCEL; addLines(table);
			dataType=Mode.DATA_TYPE_WAITING_TIME; addLines(table);
			dataType=Mode.DATA_TYPE_STAYING_TIME; addLines(table);
			dataType=Mode.DATA_TYPE_SERVICE_LEVEL_CALLS; addLines(table);
			dataType=Mode.DATA_TYPE_SERVICE_LEVEL_CLIENTS; addLines(table);
			dataType=Mode.DATA_TYPE_SERVICE_LEVEL_CALLS_ALL; addLines(table);
			dataType=Mode.DATA_TYPE_SERVICE_LEVEL_CLIENTS_ALL; addLines(table);
			dataType=Mode.DATA_TYPE_COSTS; addLines(table);
			dataType=Mode.DATA_TYPE_AGENTS_CALLCENTER; addLines(table);
			dataType=Mode.DATA_TYPE_AGENTS_SKILL_LEVEL; addLines(table);
			dataType=Mode.DATA_TYPE_WORKLOAD_CALLCENTER; addLines(table);
			dataType=Mode.DATA_TYPE_WORKLOAD_SKILL_LEVEL; addLines(table);
			dataType=Mode.DATA_TYPE_SUMMARY;
		}

		return table.transpose();
	}
}
