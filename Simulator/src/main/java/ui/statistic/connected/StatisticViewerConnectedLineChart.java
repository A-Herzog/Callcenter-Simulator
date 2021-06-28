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
package ui.statistic.connected;

import java.awt.Color;
import java.awt.Paint;
import java.text.FieldPosition;
import java.text.NumberFormat;
import java.text.ParsePosition;
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
import ui.model.CallcenterModel;

/**
 * Stellt verschiedene Statistikergebnisse der verbundenen Simulation als Liniendiagramme dar.
 * @author Alexander Herzog
 * @version 1.0
 */
public class StatisticViewerConnectedLineChart extends StatisticViewerLineChart {
	/** Array aus Objekten vom Typ {@link Statistics}, dem die Ergebnisse der einzelnen Simulationstage entnommen werden sollen */
	private final Statistics[] results;
	/** Darstellungsart, siehe <code>DATA_TYPE_*</code> Konstanten. */
	private final Mode dataType;
	/** Index im <code>kundenProTyp</code>-Array bzw. im <code>agentenProCallcenter</code>-Array, aus dem die Daten genommen werden sollen. ("-1" bedeutet, dass das <code>kundenGlobal</code>- bzw. das <code>agentenGlobal</code>-Objekt verwendet wird.) */
	private final int dataNr;

	/**
	 * Welche Informationen sollen angezeigt werden?
	 * @author Alexander Herzog
	 * @see StatisticViewerConnectedLineChart#StatisticViewerConnectedLineChart(Statistics[], Mode, int)
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
		 * Zeigt die Veränderung des Service-Level an (bezogen auf erfolgreiche Anrufe).
		 */
		DATA_TYPE_SERVICE_LEVEL_CALLS_SUCCESS,

		/**
		 * Zeigt die Veränderung des Service-Level an (bezogen auf erfolgreiche Kunden).
		 */
		DATA_TYPE_SERVICE_LEVEL_CLIENTS_SUCCESS,

		/**
		 * Zeigt die Veränderung des Service-Level an (bezogen auf alle Anrufe).
		 */
		DATA_TYPE_SERVICE_LEVEL_CALLS_ALL,

		/**
		 * Zeigt die Veränderung des Service-Level an (bezogen auf alle Kunden).
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
		DATA_TYPE_WORKLOAD_SKILL_LEVEL
	}

	/**
	 * Konstruktor der Klasse <code>StatisticViewerConnectedLineChart</code>
	 * @param results	Array aus Objekten vom Typ {@link Statistics}, dem die Ergebnisse der einzelnen Simulationstage entnommen werden sollen
	 * @param dataType	Darstellungsart, siehe <code>DATA_TYPE_*</code> Konstanten.
	 * @param dataNr	Index im <code>kundenProTyp</code>-Array bzw. im <code>agentenProCallcenter</code>-Array, aus dem die Daten genommen werden sollen. ("-1" bedeutet, dass das <code>kundenGlobal</code>- bzw. das <code>agentenGlobal</code>-Objekt verwendet wird.)
	 */
	public StatisticViewerConnectedLineChart(Statistics[] results, Mode dataType, int dataNr) {
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
	private void initConnectedChart(String title, String yLabel, boolean percent) {
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

		axis.setRange(1,results.length);
		axis.setLabel(Language.tr("SimStatistic.SimulatedDay"));
		NumberFormat format=new NumberFormat() {
			private static final long serialVersionUID = 6373338438212933118L;
			@Override public StringBuffer format(double number, StringBuffer toAppendTo, FieldPosition pos) {return toAppendTo.append(getDayName((int)Math.round(number)-1));}
			@Override public StringBuffer format(long number, StringBuffer toAppendTo, FieldPosition pos) {return format((double)number,toAppendTo,pos);}
			@Override public Number parse(String source, ParsePosition parsePosition) {return null;}
		};
		axis.setNumberFormatOverride(format);
		boolean vertical=false;
		for (Statistics statistic : results) {String date=statistic.editModel.date; if (date!=null && !date.trim().isEmpty()) {vertical=true; break;}}
		axis.setVerticalTickLabels(vertical);
		axis.setTickUnit(new NumberTickUnit(1));

		plot.setDomainAxis(axis);
	}

	/**
	 * Fügt eine Datenreihe zu dem Diagramm hinzu.
	 * @param title	Titel der Datenreihe
	 * @param paint	Darstellung der Datenreihe
	 * @param dist	Datenreihe
	 */
	private void addConnectedSeries(final String title, final Paint paint, final List<Double> dist) {
		if (dist.size()>1) {
			XYSeries series=new XYSeries(title);
			for (int i=0;i<dist.size();i++) series.add(i+1,dist.get(i));
			data.addSeries(series);
			plot.getRenderer().setSeriesPaint(data.getSeriesCount()-1,paint);
		}
	}

	/**
	 * Liefert den Namen für einen bestimmten Simulationstag.
	 * @param index	Index des Tages
	 * @return	Namen für einen bestimmten Simulationstag
	 */
	private String getDayName(int index) {
		if (index>=0 && index<results.length) {
			String date=results[index].editModel.date;
			if (date!=null && !date.trim().isEmpty()) return CallcenterModel.dateToLocalString(CallcenterModel.stringToDate(date));
		}

		return ""+(index+1);
	}

	@Override
	protected void firstChartRequest() {
		List<Double> dist1=new ArrayList<>();
		List<Double> dist2=new ArrayList<>();
		List<Double> dist3=new ArrayList<>();
		List<Double> dist4=new ArrayList<>();
		List<Double> dist5=new ArrayList<>();
		KundenDaten kunden=null;
		AgentenDaten agenten=null;

		String type="";
		int dataSource=0;
		if (dataType==Mode.DATA_TYPE_AGENTS_CALLCENTER || dataType==Mode.DATA_TYPE_WORKLOAD_CALLCENTER) dataSource=1;
		if (dataType==Mode.DATA_TYPE_AGENTS_SKILL_LEVEL || dataType==Mode.DATA_TYPE_WORKLOAD_SKILL_LEVEL) dataSource=2;

		switch (dataSource) {
		case 0: type=(dataNr<0)?Language.tr("SimStatistic.AllClients"):results[0].kundenProTyp[dataNr].name; break;
		case 1:	type=(dataNr<0)?Language.tr("SimStatistic.AllCallcenter"):results[0].agentenProCallcenter[dataNr].name; break;
		case 2: type=(dataNr<0)?Language.tr("SimStatistic.AllSkillLevel"):results[0].agentenProSkilllevel[dataNr].name; break;
		}

		switch (dataType) {
		case DATA_TYPE_CALLERS: initConnectedChart(Language.tr("SimStatistic.NumberOfCallers"),Language.tr("SimStatistic.NumberPerDay"),false); break;
		case DATA_TYPE_SUCCESS: initConnectedChart(Language.tr("SimStatistic.Accessibility"),Language.tr("SimStatistic.AverageAccessibility"),true); break;
		case DATA_TYPE_CANCEL: initConnectedChart(Language.tr("SimStatistic.NumberOfCancelations"),Language.tr("SimStatistic.NumberPerDay"),false); break;
		case DATA_TYPE_WAITING_TIME: initConnectedChart(Language.tr("SimStatistic.WaitingTime"),Language.tr("SimStatistic.AverageWaitingTime"),false); break;
		case DATA_TYPE_STAYING_TIME: initConnectedChart(Language.tr("SimStatistic.ResidenceTime"),Language.tr("SimStatistic.AverageResidenceTime"),false); break;
		case DATA_TYPE_SERVICE_LEVEL_CALLS_SUCCESS: initConnectedChart(Language.tr("SimStatistic.ServiceLevel")+" - "+Language.tr("SimStatistic.CalculatedOn.SuccessfulCalls"),Language.tr("SimStatistic.ServiceLevel")+" - "+Language.tr("SimStatistic.CalculatedOn.SuccessfulCalls"),true); break;
		case DATA_TYPE_SERVICE_LEVEL_CLIENTS_SUCCESS: initConnectedChart(Language.tr("SimStatistic.ServiceLevel")+" - "+Language.tr("SimStatistic.CalculatedOn.SuccessfulClients"),Language.tr("SimStatistic.ServiceLevel")+" - "+Language.tr("SimStatistic.CalculatedOn.SuccessfulClients"),true); break;
		case DATA_TYPE_SERVICE_LEVEL_CALLS_ALL: initConnectedChart(Language.tr("SimStatistic.ServiceLevel")+" - "+Language.tr("SimStatistic.CalculatedOn.AllCalls"),Language.tr("SimStatistic.ServiceLevel")+" - "+Language.tr("SimStatistic.CalculatedOn.AllCalls"),true); break;
		case DATA_TYPE_SERVICE_LEVEL_CLIENTS_ALL: initConnectedChart(Language.tr("SimStatistic.ServiceLevel")+" - "+Language.tr("SimStatistic.CalculatedOn.AllClients"),Language.tr("SimStatistic.ServiceLevel")+" - "+Language.tr("SimStatistic.CalculatedOn.AllClients"),true); break;
		case DATA_TYPE_AGENTS_CALLCENTER: initConnectedChart(Language.tr("SimStatistic.NumberOfAgents"),Language.tr("SimStatistic.ManHoursPerDay"),false); break;
		case DATA_TYPE_AGENTS_SKILL_LEVEL: initConnectedChart(Language.tr("SimStatistic.NumberOfAgents"),Language.tr("SimStatistic.ManHoursPerDay"),false); break;
		case DATA_TYPE_WORKLOAD_CALLCENTER: initConnectedChart(Language.tr("SimStatistic.WorkLoad"),Language.tr("SimStatistic.WorkLoad"),true); break;
		case DATA_TYPE_WORKLOAD_SKILL_LEVEL: initConnectedChart(Language.tr("SimStatistic.WorkLoad"),Language.tr("SimStatistic.WorkLoad"),true); break;
		}

		for (int i=0;i<results.length;i++) {
			Statistics statistic=results[i];

			switch (dataSource) {
			case 0: if (dataNr<0) kunden=statistic.kundenGlobal; else kunden=statistic.kundenProTyp[dataNr]; break;
			case 1: if (dataNr<0) agenten=statistic.agentenGlobal; else agenten=statistic.agentenProCallcenter[dataNr]; break;
			case 2: if (dataNr<0) agenten=statistic.agentenGlobal; else agenten=statistic.agentenProSkilllevel[dataNr]; break;
			}

			switch (dataType) {
			case DATA_TYPE_CALLERS:
				if (kunden!=null) {
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
			case DATA_TYPE_SERVICE_LEVEL_CALLS_SUCCESS:
				if (kunden!=null) dist1.add((double)kunden.anrufeServicelevel/Math.max(1,kunden.anrufeErfolg));
				break;
			case DATA_TYPE_SERVICE_LEVEL_CLIENTS_SUCCESS:
				if (kunden!=null) dist1.add((double)kunden.anrufeServicelevel/Math.max(1,kunden.kundenErfolg));
				break;
			case DATA_TYPE_SERVICE_LEVEL_CALLS_ALL:
				if (kunden!=null) dist1.add((double)kunden.anrufeServicelevel/kunden.anrufe);
				break;
			case DATA_TYPE_SERVICE_LEVEL_CLIENTS_ALL:
				if (kunden!=null) dist1.add((double)kunden.anrufeServicelevel/kunden.kunden);
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
			}
		}

		switch (dataType) {
		case DATA_TYPE_CALLERS:
			addConnectedSeries(Language.tr("SimStatistic.FreshCalls")+" ("+type+")",Color.BLUE,dist1);
			addConnectedSeries(Language.tr("SimStatistic.Calls.Info")+" ("+type+")",Color.RED,dist2);
			addFillColor(1);
			break;
		case DATA_TYPE_SUCCESS:
			addConnectedSeries(Language.tr("SimStatistic.Accessibility")+" "+Language.tr("SimStatistic.OnClientBasis")+" ("+type+")",Color.BLUE,dist1);
			addConnectedSeries(Language.tr("SimStatistic.Accessibility")+" "+Language.tr("SimStatistic.OnCallBasis")+" ("+type+")",Color.RED,dist2);
			break;
		case DATA_TYPE_CANCEL:
			addConnectedSeries("Warteabbrecher "+Language.tr("SimStatistic.OnClientBasis")+" ("+type+")",Color.BLUE,dist1);
			addConnectedSeries("Warteabbrecher "+Language.tr("SimStatistic.OnCallBasis")+" ("+type+")",Color.RED,dist2);
			break;
		case DATA_TYPE_WAITING_TIME:
			addConnectedSeries(Language.tr("SimStatistic.AverageWaitingTime")+" ("+type+")",Color.BLUE,dist1);
			addFillColor(0);
			break;
		case DATA_TYPE_STAYING_TIME:
			addConnectedSeries(Language.tr("SimStatistic.AverageResidenceTime")+" ("+type+")",Color.BLUE,dist1);
			addFillColor(0);
			break;
		case DATA_TYPE_SERVICE_LEVEL_CALLS_SUCCESS:
		case DATA_TYPE_SERVICE_LEVEL_CLIENTS_SUCCESS:
		case DATA_TYPE_SERVICE_LEVEL_CALLS_ALL:
		case DATA_TYPE_SERVICE_LEVEL_CLIENTS_ALL:
			addConnectedSeries(Language.tr("SimStatistic.ServiceLevel")+" ("+type+")",Color.BLUE,dist1);
			addFillColor(0);
			break;
		case DATA_TYPE_AGENTS_CALLCENTER:
		case DATA_TYPE_AGENTS_SKILL_LEVEL:
			addConnectedSeries(Language.tr("Statistic.Total")+" ("+type+")",Color.BLACK,dist1);
			addConnectedSeries(Language.tr("SimStatistic.IdleTime")+" ("+type+")",Color.CYAN,dist2);
			addConnectedSeries(Language.tr("SimStatistic.TechnicalFreeTime")+" ("+type+")",Color.BLUE,dist3);
			addConnectedSeries(Language.tr("SimStatistic.HoldingTime")+" ("+type+")",Color.RED,dist4);
			addConnectedSeries(Language.tr("SimStatistic.PostProcessingTime")+" ("+type+")",Color.GREEN,dist5);
			break;
		case DATA_TYPE_WORKLOAD_CALLCENTER:
		case DATA_TYPE_WORKLOAD_SKILL_LEVEL:
			addConnectedSeries(Language.tr("SimStatistic.WorkLoad")+" ("+type+")",Color.BLUE,dist1);
			break;
		}

		initTooltips();
	}

	@Override
	public void unZoom() {
		super.unZoom();
		((NumberAxis)plot.getDomainAxis()).setRange(1,results.length);
	}
}
