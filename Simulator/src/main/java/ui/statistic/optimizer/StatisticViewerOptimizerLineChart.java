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

import java.awt.Color;
import java.awt.Paint;
import java.text.NumberFormat;
import java.util.ArrayList;
import java.util.List;

import org.jfree.chart.axis.NumberAxis;
import org.jfree.chart.axis.NumberTickUnit;
import org.jfree.data.xy.XYSeries;

import language.Language;
import simulator.Statistics;
import simulator.Statistics.AgentenDaten;
import simulator.Statistics.KundenDaten;
import systemtools.statistics.StatisticViewerLineChart;
import ui.optimizer.OptimizeData;
import ui.statistic.model.StatisticViewerErlangCTools;

/**
 * Stellt verschiedene Optimierer-Statistikergebnisse als Liniendiagramme dar.
 * @author Alexander Herzog
 * @version 1.0
 */
public class StatisticViewerOptimizerLineChart extends StatisticViewerLineChart {
	/** Objekt vom Typ <code>OptimizeData</code>, dem die Optimierungsergebnisse entnommen werden sollen */
	private final OptimizeData results;
	/** Darstellungsart, siehe <code>DATA_TYPE_*</code> Konstanten. */
	private final Mode dataType;
	/** Index im <code>kundenProTyp</code>-Array bzw. im <code>agentenProCallcenter</code>-Array, aus dem die Daten genommen werden sollen. ("-1" bedeutet, dass das <code>kundenGlobal</code>- bzw. das <code>agentenGlobal</code>-Objekt verwendet wird.) */
	private final int dataNr;

	/**
	 * Welche Informationen sollen angezeigt werden?
	 * @author Alexander Herzog
	 * @see StatisticViewerOptimizerLineChart#StatisticViewerOptimizerLineChart(OptimizeData, Mode, int)
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
		 * Zeigt die Veränderung des Service-Level auf Anrufbasis (bezogen auf die erfolgreichen Anrufe) an.
		 */
		DATA_TYPE_SERVICE_LEVEL_CALLS,

		/**
		 * Zeigt die Veränderung des Service-Level auf Anrufbasis (bezogen auf alle Anrufe) an.
		 */
		DATA_TYPE_SERVICE_LEVEL_CALLS_ALL,

		/**
		 * Zeigt die Veränderung des Service-Level auf Kundenbasis (bezogen auf die erfolgreichen Kunden) an.
		 */
		DATA_TYPE_SERVICE_LEVEL_CLIENTS,

		/**
		 * Zeigt die Veränderung des Service-Level auf Kundenbasis (bezogen auf alle Kunden) an.
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
		 * Zeigt die Veränderung der Kosten und Erträge pro Kundentyp an.
		 */
		DATA_TYPE_CALLER_COSTS,

		/**
		 * Zeigt die Veränderung der Kosten und Erträge pro Skill-Level an.
		 */
		DATA_TYPE_SKILL_LEVEL_COSTS,

		/**
		 * Zeigt die Veränderung der Gesamtkosten und Erträge an.
		 */
		DATA_TYPE_COSTS,

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
	 * Konstruktor der Klasse <code>StatisticViewerOptimizerLineChart</code>
	 * @param results	Objekt vom Typ <code>OptimizeData</code>, dem die Optimierungsergebnisse entnommen werden sollen
	 * @param dataType	Darstellungsart, siehe <code>DATA_TYPE_*</code> Konstanten.
	 * @param dataNr	Index im <code>kundenProTyp</code>-Array bzw. im <code>agentenProCallcenter</code>-Array, aus dem die Daten genommen werden sollen. ("-1" bedeutet, dass das <code>kundenGlobal</code>- bzw. das <code>agentenGlobal</code>-Objekt verwendet wird.)
	 */
	public StatisticViewerOptimizerLineChart(OptimizeData results, Mode dataType, int dataNr) {
		super();
		this.results=results;
		this.dataType=dataType;
		this.dataNr=dataNr;
	}

	/**
	 * Konfiguriert die Diagrammanzeige
	 * @param title	Diagrammtitel
	 * @param yLabel	y-Achsen-Beschriftung
	 * @param percent	Soll die y-Achse Zahlenwerte (<code>false</code>) oder Prozentwerte (<code>true</code>) anzeigen?
	 */
	private void initOptimizeChart(String title, String yLabel, boolean percent) {
		initLineChart(title);

		if (percent) {
			NumberAxis axis=new NumberAxis();
			NumberFormat formater=NumberFormat.getPercentInstance();
			formater.setMinimumFractionDigits(1);
			formater.setMaximumFractionDigits(1);
			axis.setNumberFormatOverride(formater);
			axis.setLabel(yLabel);
			plot.setRangeAxis(axis);
		}
		plot.getRangeAxis().setLabel(yLabel);

		NumberAxis axis=new NumberAxis();
		axis.setRange(1,results.data.size());
		axis.setLabel(Language.tr("SimStatistic.SimulationRun"));
		NumberFormat formater=NumberFormat.getNumberInstance();
		formater.setMaximumFractionDigits(0);
		axis.setNumberFormatOverride(formater);
		axis.setTickUnit(new NumberTickUnit(1));
		plot.setDomainAxis(axis);
	}

	/**
	 * Fügt eine Datenreihe zu dem Diagramm hinzu.
	 * @param title	Titel der Datenreihe
	 * @param paint	Darstellung der Datenreihe
	 * @param dist	Anzuzeigende Datenreihe
	 */
	private void addOptimizeSeries(final String title, final Paint paint, final List<Double> dist) {
		if (dist.size()>1) {
			XYSeries series=new XYSeries(title);
			for (int i=0;i<dist.size();i++) series.add(i+1,dist.get(i));
			data.addSeries(series);
			plot.getRenderer().setSeriesPaint(data.getSeriesCount()-1,paint);
		}
	}

	/**
	 * Berechnet den mit den Anrufern pro Intervall gewichteten Mittelwert über eine Datenreihe
	 * @param data	Datenreihe
	 * @param statistic	Statistikobjekt dem die Informationen zu den Anzahlen an Anrufern pro Intervall entnommen werden sollen
	 * @return	Gewichteter Mittelwert der Datenreihe
	 */
	private double average(final double[] data, Statistics statistic) {
		double count=0, sum=0;
		final double[] weights=statistic.kundenGlobal.anrufeProIntervall.densityData;
		for (int i=0;i<48;i++) {
			count+=weights[i];
			sum+=data[i]*weights[i];
		}
		return (count>0)?(sum/count):0;
	}

	@Override
	protected void firstChartRequest() {
		List<Double> dist1=new ArrayList<Double>();
		List<Double> dist2=new ArrayList<Double>();
		List<Double> dist3=new ArrayList<Double>();
		List<Double> dist4=new ArrayList<Double>();
		List<Double> dist5=new ArrayList<Double>();
		KundenDaten kunden=null;
		AgentenDaten agenten=null;

		StatisticViewerErlangCTools erlangC1, erlangC2;
		double erlang1, erlang2, simValue;

		String type="";
		int dataSource=0;
		if (dataType==Mode.DATA_TYPE_AGENTS_CALLCENTER || dataType==Mode.DATA_TYPE_WORKLOAD_CALLCENTER) dataSource=1;
		if (dataType==Mode.DATA_TYPE_AGENTS_SKILL_LEVEL || dataType==Mode.DATA_TYPE_WORKLOAD_SKILL_LEVEL || dataType==Mode.DATA_TYPE_SKILL_LEVEL_COSTS) dataSource=2;

		switch (dataSource) {
		case 0: type=(dataNr<0)?Language.tr("SimStatistic.AllClients"):results.data.get(0).kundenProTyp[dataNr].name; break;
		case 1:	type=(dataNr<0)?Language.tr("SimStatistic.AllCallcenter"):results.data.get(0).agentenProCallcenter[dataNr].name; break;
		case 2: type=(dataNr<0)?Language.tr("SimStatistic.AllSkillLevel"):results.data.get(0).agentenProSkilllevel[dataNr].name; break;
		}

		switch (dataType) {
		case DATA_TYPE_CALLERS: initOptimizeChart(Language.tr("SimStatistic.NumberOfCallers"),Language.tr("SimStatistic.NumberPerDay"),false); break;
		case DATA_TYPE_SUCCESS: initOptimizeChart(Language.tr("SimStatistic.Accessibility"),Language.tr("SimStatistic.AverageAccessibility"),true); break;
		case DATA_TYPE_CANCEL: initOptimizeChart(Language.tr("SimStatistic.NumberOfCancelations"),Language.tr("SimStatistic.NumberPerDay"),false); break;
		case DATA_TYPE_WAITING_TIME: initOptimizeChart(Language.tr("SimStatistic.WaitingTime"),Language.tr("SimStatistic.AverageWaitingTime"),false); break;
		case DATA_TYPE_STAYING_TIME: initOptimizeChart(Language.tr("SimStatistic.ResidenceTime"),Language.tr("SimStatistic.AverageResidenceTime"),false); break;
		case DATA_TYPE_SERVICE_LEVEL_CALLS: initOptimizeChart(Language.tr("SimStatistic.ServiceLevel"),Language.tr("SimStatistic.ServiceLevel"),true); break;
		case DATA_TYPE_SERVICE_LEVEL_CALLS_ALL: initOptimizeChart(Language.tr("SimStatistic.ServiceLevel"),Language.tr("SimStatistic.ServiceLevel"),true); break;
		case DATA_TYPE_SERVICE_LEVEL_CLIENTS: initOptimizeChart(Language.tr("SimStatistic.ServiceLevel"),Language.tr("SimStatistic.ServiceLevel"),true); break;
		case DATA_TYPE_SERVICE_LEVEL_CLIENTS_ALL: initOptimizeChart(Language.tr("SimStatistic.ServiceLevel"),Language.tr("SimStatistic.ServiceLevel"),true); break;
		case DATA_TYPE_AGENTS_CALLCENTER: initOptimizeChart(Language.tr("SimStatistic.NumberOfAgents"),Language.tr("SimStatistic.ManHoursPerDay"),false); break;
		case DATA_TYPE_AGENTS_SKILL_LEVEL: initOptimizeChart(Language.tr("SimStatistic.NumberOfAgents"),Language.tr("SimStatistic.ManHoursPerDay"),false); break;
		case DATA_TYPE_WORKLOAD_CALLCENTER: initOptimizeChart(Language.tr("SimStatistic.WorkLoad"),Language.tr("SimStatistic.WorkLoad"),true); break;
		case DATA_TYPE_WORKLOAD_SKILL_LEVEL: initOptimizeChart(Language.tr("SimStatistic.WorkLoad"),Language.tr("SimStatistic.WorkLoad"),true); break;
		case DATA_TYPE_CALLER_COSTS: initOptimizeChart(Language.tr("SimStatistic.Costs")+" & "+Language.tr("SimStatistic.Yield"),Language.tr("SimStatistic.Costs")+" & "+Language.tr("SimStatistic.Yield"),false); break;
		case DATA_TYPE_SKILL_LEVEL_COSTS: initOptimizeChart(Language.tr("SimStatistic.Costs"),Language.tr("SimStatistic.Costs"),false); break;
		case DATA_TYPE_COSTS: initOptimizeChart(Language.tr("SimStatistic.Costs")+" & "+Language.tr("SimStatistic.Yield"),Language.tr("SimStatistic.Costs")+" & "+Language.tr("SimStatistic.Yield"),false); break;
		case DATA_TYPE_ERLANGC_SUCCESS: initOptimizeChart(Language.tr("SimStatistic.Accessibility"),Language.tr("SimStatistic.AverageAccessibility"),true); break;
		case DATA_TYPE_ERLANGC_WAITING_TIME: initOptimizeChart(Language.tr("SimStatistic.WaitingTime"),Language.tr("SimStatistic.AverageWaitingTime"),false); break;
		case DATA_TYPE_ERLANGC_SERVICE_LEVEL: initOptimizeChart(Language.tr("SimStatistic.ServiceLevel"),Language.tr("SimStatistic.ServiceLevel"),true); break;
		}

		for (int i=0;i<results.data.size();i++) {
			Statistics statistic=results.data.get(i);

			switch (dataSource) {
			case 0: if (dataNr<0) kunden=statistic.kundenGlobal; else kunden=statistic.kundenProTyp[dataNr]; break;
			case 1: if (dataNr<0) agenten=statistic.agentenGlobal; else agenten=statistic.agentenProCallcenter[dataNr]; break;
			case 2: if (dataNr<0) agenten=statistic.agentenGlobal; else agenten=statistic.agentenProSkilllevel[dataNr]; break;
			}

			switch (dataType) {
			case DATA_TYPE_CALLERS:
				if (kunden!=null)  {
					dist1.add((double)(kunden.kunden+kunden.kundenWiederanruf)/statistic.simulationData.runRepeatCount);
					dist2.add((double)kunden.anrufe/statistic.simulationData.runRepeatCount);
				}
				break;
			case DATA_TYPE_SUCCESS:
				if (kunden!=null) {
					dist1.add((double)kunden.kundenErfolg/(kunden.kunden+kunden.kundenWiederanruf));
					dist2.add((double)kunden.anrufeErfolg/kunden.anrufe);
				}
				break;
			case DATA_TYPE_CANCEL:
				if (kunden!=null) {
					dist1.add((double)kunden.kundenAbbruch/statistic.simulationData.runRepeatCount);
					dist2.add((double)kunden.anrufeAbbruch/statistic.simulationData.runRepeatCount);
				}
				break;
			case DATA_TYPE_WAITING_TIME:
				if (kunden!=null) dist1.add((double)kunden.anrufeWartezeitSum/Math.max(1,kunden.anrufeErfolg));
				break;
			case DATA_TYPE_STAYING_TIME:
				if (kunden!=null) dist1.add((double)kunden.anrufeVerweilzeitSum/Math.max(1,kunden.anrufeErfolg));
				break;
			case DATA_TYPE_SERVICE_LEVEL_CALLS:
				if (kunden!=null) dist1.add((double)kunden.anrufeServicelevel/Math.max(1,kunden.anrufeErfolg));
				break;
			case DATA_TYPE_SERVICE_LEVEL_CALLS_ALL:
				if (kunden!=null) dist1.add((double)kunden.anrufeServicelevel/Math.max(1,kunden.anrufe));
				break;
			case DATA_TYPE_SERVICE_LEVEL_CLIENTS:
				if (kunden!=null) dist1.add((double)kunden.kundenServicelevel/Math.max(1,kunden.kundenErfolg));
				break;
			case DATA_TYPE_SERVICE_LEVEL_CLIENTS_ALL:
				if (kunden!=null) dist1.add((double)kunden.kundenServicelevel/Math.max(1,kunden.kunden));
				break;
			case DATA_TYPE_AGENTS_CALLCENTER:
			case DATA_TYPE_AGENTS_SKILL_LEVEL:
				if (agenten!=null) {
					dist1.add((double)(agenten.leerlaufGesamt+agenten.technischerLeerlaufGesamt+agenten.arbeitGesamt+agenten.postProcessingGesamt)/statistic.simulationData.runRepeatCount/3600);
					dist2.add((double)agenten.leerlaufGesamt/statistic.simulationData.runRepeatCount/3600);
					dist3.add((double)agenten.technischerLeerlaufGesamt/statistic.simulationData.runRepeatCount/3600);
					dist4.add((double)agenten.arbeitGesamt/statistic.simulationData.runRepeatCount/3600);
					dist5.add((double)agenten.postProcessingGesamt/statistic.simulationData.runRepeatCount/3600);
				}
				break;
			case DATA_TYPE_WORKLOAD_CALLCENTER:
			case DATA_TYPE_WORKLOAD_SKILL_LEVEL:
				if (agenten!=null) dist1.add(1-(double)agenten.leerlaufGesamt/(agenten.leerlaufGesamt+agenten.technischerLeerlaufGesamt+agenten.arbeitGesamt+agenten.postProcessingGesamt));
				break;
			case DATA_TYPE_CALLER_COSTS:
				if (kunden!=null) {
					dist1.add(kunden.revenue/statistic.simulationData.runRepeatCount);
					dist2.add(kunden.costCancel/statistic.simulationData.runRepeatCount);
					dist3.add(kunden.costWaiting/statistic.simulationData.runRepeatCount);
					dist4.add((kunden.revenue-kunden.costCancel-kunden.costWaiting)/statistic.simulationData.runRepeatCount);
				}
				break;
			case DATA_TYPE_SKILL_LEVEL_COSTS:
				if (agenten!=null) {
					dist1.add(agenten.costOfficeTime/statistic.simulationData.runRepeatCount);
					dist2.add(agenten.costCalls/statistic.simulationData.runRepeatCount);
					dist3.add(agenten.costProcessTime/statistic.simulationData.runRepeatCount);
					dist4.add((agenten.costOfficeTime+agenten.costCalls+agenten.costProcessTime)/statistic.simulationData.runRepeatCount);
				}
				break;
			case DATA_TYPE_COSTS:
				dist1.add(statistic.kundenGlobal.revenue/statistic.simulationData.runRepeatCount);
				dist2.add((statistic.kundenGlobal.costCancel+statistic.kundenGlobal.costWaiting+statistic.agentenGlobal.costOfficeTime+statistic.agentenGlobal.costCalls+statistic.agentenGlobal.costProcessTime)/statistic.simulationData.runRepeatCount);
				dist3.add((statistic.kundenGlobal.revenue-(statistic.kundenGlobal.costCancel+statistic.kundenGlobal.costWaiting+statistic.agentenGlobal.costOfficeTime+statistic.agentenGlobal.costCalls+statistic.agentenGlobal.costProcessTime))/statistic.simulationData.runRepeatCount);
				break;
			case DATA_TYPE_ERLANGC_SUCCESS:
				erlangC1=new StatisticViewerErlangCTools(statistic.editModel,false);
				erlangC2=new StatisticViewerErlangCTools(statistic.editModel,true);
				erlang1=average(erlangC1.getSuccessProbability(),statistic);
				erlang2=average(erlangC2.getSuccessProbability(),statistic);
				simValue=(double)statistic.kundenGlobal.anrufeErfolg/statistic.kundenGlobal.anrufe;
				dist1.add(erlang1);
				dist2.add(erlang2);
				dist3.add(simValue);
				dist4.add(erlang1-simValue);
				dist5.add(erlang2-simValue);
				break;
			case DATA_TYPE_ERLANGC_WAITING_TIME:
				erlangC1=new StatisticViewerErlangCTools(statistic.editModel,false);
				erlangC2=new StatisticViewerErlangCTools(statistic.editModel,true);
				erlang1=average(erlangC1.getMeanWaitingTime(),statistic);
				erlang2=average(erlangC2.getMeanWaitingTime(),statistic);
				simValue=(double)statistic.kundenGlobal.anrufeWartezeitSum/Math.max(1,statistic.kundenGlobal.anrufeErfolg);
				dist1.add(erlang1);
				dist2.add(erlang2);
				dist3.add(simValue);
				dist4.add(erlang1-simValue);
				dist5.add(erlang2-simValue);
				break;
			case DATA_TYPE_ERLANGC_SERVICE_LEVEL:
				erlangC1=new StatisticViewerErlangCTools(statistic.editModel,false);
				erlangC2=new StatisticViewerErlangCTools(statistic.editModel,true);
				erlang1=average(erlangC1.getServiceLevel(),statistic);
				erlang2=average(erlangC2.getServiceLevel(),statistic);
				simValue=(double)statistic.kundenGlobal.anrufeServicelevel/Math.max(1,statistic.kundenGlobal.anrufeErfolg);
				dist1.add(erlang1);
				dist2.add(erlang2);
				dist3.add(simValue);
				dist4.add(erlang1-simValue);
				dist5.add(erlang2-simValue);
				break;
			}
		}

		switch (dataType) {
		case DATA_TYPE_CALLERS:
			addOptimizeSeries(Language.tr("SimStatistic.FreshCalls")+" ("+type+")",Color.BLUE,dist1);
			addOptimizeSeries(Language.tr("SimStatistic.Calls.Info")+" ("+type+")",Color.RED,dist2);
			addFillColor(1);
			break;
		case DATA_TYPE_SUCCESS:
			addOptimizeSeries(Language.tr("SimStatistic.Accessibility")+" "+Language.tr("SimStatistic.OnClientBasis")+" ("+type+")",Color.BLUE,dist1);
			addOptimizeSeries(Language.tr("SimStatistic.Accessibility")+" "+Language.tr("SimStatistic.OnCallBasis")+" ("+type+")",Color.RED,dist2);
			break;
		case DATA_TYPE_CANCEL:
			addOptimizeSeries("Warteabbrecher "+Language.tr("SimStatistic.OnClientBasis")+" ("+type+")",Color.BLUE,dist1);
			addOptimizeSeries("Warteabbrecher "+Language.tr("SimStatistic.OnCallBasis")+" ("+type+")",Color.RED,dist2);
			break;
		case DATA_TYPE_WAITING_TIME:
			addOptimizeSeries(Language.tr("SimStatistic.AverageWaitingTime")+" ("+type+")",Color.BLUE,dist1);
			addFillColor(0);
			break;
		case DATA_TYPE_STAYING_TIME:
			addOptimizeSeries(Language.tr("SimStatistic.AverageResidenceTime")+" ("+type+")",Color.BLUE,dist1);
			addFillColor(0);
			break;
		case DATA_TYPE_SERVICE_LEVEL_CALLS:
			addOptimizeSeries(Language.tr("SimStatistic.ServiceLevel")+" ("+Language.tr("SimStatistic.OnCallBasis")+", "+Language.tr("SimStatistic.CalculatedOn.SuccessfulCalls")+", "+type+")",Color.BLUE,dist1);
			addFillColor(0);
			break;
		case DATA_TYPE_SERVICE_LEVEL_CALLS_ALL:
			addOptimizeSeries(Language.tr("SimStatistic.ServiceLevel")+" ("+Language.tr("SimStatistic.OnCallBasis")+", "+Language.tr("SimStatistic.CalculatedOn.AllCalls")+", "+type+")",Color.BLUE,dist1);
			addFillColor(0);
			break;
		case DATA_TYPE_SERVICE_LEVEL_CLIENTS:
			addOptimizeSeries(Language.tr("SimStatistic.ServiceLevel")+" ("+Language.tr("SimStatistic.OnClientBasis")+", "+Language.tr("SimStatistic.CalculatedOn.SuccessfulClients")+", "+type+")",Color.BLUE,dist1);
			addFillColor(0);
			break;
		case DATA_TYPE_SERVICE_LEVEL_CLIENTS_ALL:
			addOptimizeSeries(Language.tr("SimStatistic.ServiceLevel")+" ("+Language.tr("SimStatistic.OnClientBasis")+", "+Language.tr("SimStatistic.CalculatedOn.AllClients")+", "+type+")",Color.BLUE,dist1);
			addFillColor(0);
			break;
		case DATA_TYPE_AGENTS_CALLCENTER:
		case DATA_TYPE_AGENTS_SKILL_LEVEL:
			addOptimizeSeries(Language.tr("Statistic.Total")+" ("+type+")",Color.BLACK,dist1);
			addOptimizeSeries(Language.tr("SimStatistic.IdleTime")+" ("+type+")",Color.CYAN,dist2);
			addOptimizeSeries(Language.tr("SimStatistic.TechnicalFreeTime")+" ("+type+")",Color.BLUE,dist3);
			addOptimizeSeries(Language.tr("SimStatistic.HoldingTime")+" ("+type+")",Color.RED,dist4);
			addOptimizeSeries(Language.tr("SimStatistic.PostProcessingTime")+" ("+type+")",Color.GREEN,dist5);
			break;
		case DATA_TYPE_WORKLOAD_CALLCENTER:
		case DATA_TYPE_WORKLOAD_SKILL_LEVEL:
			addOptimizeSeries(Language.tr("SimStatistic.WorkLoad")+" ("+type+")",Color.BLUE,dist1);
			break;
		case DATA_TYPE_CALLER_COSTS:
			addOptimizeSeries(Language.tr("SimStatistic.Yield.Clients")+" ("+type+")",Color.GREEN,dist1);
			addOptimizeSeries(Language.tr("SimStatistic.Costs.Cancel")+" ("+type+")",Color.RED,dist2);
			addOptimizeSeries(Language.tr("SimStatistic.Costs.WaitingTime")+" ("+type+")",Color.ORANGE,dist3);
			addOptimizeSeries(Language.tr("Statistic.Total")+" ("+type+")",Color.BLUE,dist4);
			break;
		case DATA_TYPE_SKILL_LEVEL_COSTS:
			addOptimizeSeries(Language.tr("SimStatistic.Costs.Wage")+" ("+type+")",Color.BLACK,dist1);
			addOptimizeSeries(Language.tr("SimStatistic.Costs.HoldingTimes")+" ("+type+")",Color.RED,dist2);
			addOptimizeSeries(Language.tr("SimStatistic.Costs.HoldingAndPostProcessingTimes")+" ("+type+")",Color.ORANGE,dist3);
			addOptimizeSeries(Language.tr("Statistic.Total")+" ("+type+")",Color.BLUE,dist4);
			break;
		case DATA_TYPE_COSTS:
			addOptimizeSeries(Language.tr("SimStatistic.Yield"),Color.GREEN,dist1);
			addOptimizeSeries(Language.tr("SimStatistic.Costs"),Color.RED,dist2);
			addOptimizeSeries(Language.tr("Statistic.Total"),Color.BLUE,dist3);
			break;
		case DATA_TYPE_ERLANGC_SUCCESS:
			addOptimizeSeries(Language.tr("SimStatistic.Accessibility")+" ("+Language.tr("SimStatistic.Type.ErlangCSimple")+")",Color.RED,dist1);
			addOptimizeSeries(Language.tr("SimStatistic.Accessibility")+" ("+Language.tr("SimStatistic.Type.ErlangCComplex")+")",Color.ORANGE,dist2);
			addOptimizeSeries(Language.tr("SimStatistic.Accessibility")+" ("+Language.tr("SimStatistic.Type.Simulation")+")",Color.GREEN,dist3);
			addOptimizeSeries(Language.tr("SimStatistic.Error")+" "+Language.tr("SimStatistic.Type.ErlangCSimple")+" - "+Language.tr("SimStatistic.Type.Simulation"),Color.GRAY,dist4);
			addOptimizeSeries(Language.tr("SimStatistic.Error")+" "+Language.tr("SimStatistic.Type.ErlangCComplex")+" - "+Language.tr("SimStatistic.Type.Simulation"),Color.BLACK,dist5);
			break;
		case DATA_TYPE_ERLANGC_WAITING_TIME:
			addOptimizeSeries(Language.tr("SimStatistic.AverageWaitingTime")+" ("+Language.tr("SimStatistic.Type.ErlangCSimple")+")",Color.RED,dist1);
			addOptimizeSeries(Language.tr("SimStatistic.AverageWaitingTime")+" ("+Language.tr("SimStatistic.Type.ErlangCComplex")+")",Color.ORANGE,dist2);
			addOptimizeSeries(Language.tr("SimStatistic.AverageWaitingTime")+" ("+Language.tr("SimStatistic.Type.Simulation")+")",Color.GREEN,dist3);
			addOptimizeSeries(Language.tr("SimStatistic.Error")+" "+Language.tr("SimStatistic.Type.ErlangCSimple")+" - "+Language.tr("SimStatistic.Type.Simulation"),Color.GRAY,dist4);
			addOptimizeSeries(Language.tr("SimStatistic.Error")+" "+Language.tr("SimStatistic.Type.ErlangCComplex")+" - "+Language.tr("SimStatistic.Type.Simulation"),Color.BLACK,dist5);
			break;
		case DATA_TYPE_ERLANGC_SERVICE_LEVEL:
			addOptimizeSeries(Language.tr("SimStatistic.ServiceLevel")+" ("+Language.tr("SimStatistic.Type.ErlangCSimple")+")",Color.RED,dist1);
			addOptimizeSeries(Language.tr("SimStatistic.ServiceLevel")+" ("+Language.tr("SimStatistic.Type.ErlangCComplex")+")",Color.ORANGE,dist2);
			addOptimizeSeries(Language.tr("SimStatistic.ServiceLevel")+" ("+Language.tr("SimStatistic.Type.Simulation")+")",Color.GREEN,dist3);
			addOptimizeSeries(Language.tr("SimStatistic.Error")+" "+Language.tr("SimStatistic.Type.ErlangCSimple")+" - "+Language.tr("SimStatistic.Type.Simulation"),Color.GRAY,dist4);
			addOptimizeSeries(Language.tr("SimStatistic.Error")+" "+Language.tr("SimStatistic.Type.ErlangCComplex")+" - "+Language.tr("SimStatistic.Type.Simulation"),Color.BLACK,dist5);
			break;
		}

		initTooltips();
	}

	@Override
	public void unZoom() {
		super.unZoom();
		((NumberAxis)plot.getDomainAxis()).setRange(1,results.data.size());
	}
}
