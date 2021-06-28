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

import java.awt.Color;
import java.util.ArrayList;
import java.util.List;

import language.Language;
import mathtools.distribution.DataDistributionImpl;
import simulator.Statistics;
import simulator.Statistics.AgentenDaten;
import simulator.Statistics.KundenDaten;
import systemtools.statistics.StatisticViewerLineChart;
import ui.model.CallcenterModelAgent;
import ui.model.CallcenterModelCallcenter;

/**
 * Stellt verschiedene Statistikergebnisse zu den Kunden- und Anrufergruppen als Liniendiagramme dar.
 * @author Alexander Herzog
 * @version 1.0
 */
public class StatisticViewerKundenAgentenLineChart extends StatisticViewerLineChart {
	/** Objekt vom Typ {@link Statistics}, dem die Kunden- bzw. Agentendaten entnommen werden sollen */
	private final Statistics statistic;
	/** Darstellungsart, siehe <code>DATA_TYPE_*</code> Konstanten. */
	private final Mode dataType;
	/** Index im <code>kundenProTyp</code>-Array bzw. im <code>agentenProCallcenter</code>-Array, aus dem die Daten genommen werden sollen. ("-1" bedeutet, dass das <code>kundenGlobal</code>- bzw. das <code>agentenGlobal</code>-Objekt verwendet wird.) */
	private final int dataNr;

	/**
	 * Welche Informationen sollen angezeigt werden?
	 * @author Alexander Herzog
	 * @see StatisticViewerKundenAgentenLineChart#StatisticViewerKundenAgentenLineChart(Statistics, Mode, int)
	 */
	public enum Mode {
		/**
		 * Zeigt die Anruferzahlen pro Halbstundenintervall an.
		 */
		DATA_TYPE_CALLER,

		/**
		 * Zeigt die Verteilung der Wartezeitlängen an (auf Kundenbasis).
		 */
		DATA_TYPE_CLIENT_WAITINGTIME_DIST,

		/**
		 * Zeigt die Verteilung der Wartezeitlängen an (auf Anrufbasis).
		 */
		DATA_TYPE_CALLER_WAITINGTIME_DIST,

		/**
		 * Zeigt die Verteilung der Verweilzeitlängen an (auf Kundenbasis).
		 */
		DATA_TYPE_CLIENT_STAYINGTIME_DIST,

		/**
		 * Zeigt die Verteilung der Verweilzeitlängen an (auf Anrufbasis).
		 */
		DATA_TYPE_CALLER_STAYINGTIME_DIST,

		/**
		 * Zeigt die Verteilung der langen Wartezeitlängen an (auf Kundenbasis).
		 */
		DATA_TYPE_CLIENT_WAITINGTIME_DIST_LONG,

		/**
		 * Zeigt die Verteilung der langen Wartezeitlängen an (auf Anrufbasis).
		 */
		DATA_TYPE_CALLER_WAITINGTIME_DIST_LONG,

		/**
		 * Zeigt die Verteilung der langen Verweilzeitlängen an (auf Kundenbasis).
		 */
		DATA_TYPE_CLIENT_STAYINGTIME_DIST_LONG,

		/**
		 * Zeigt die Verteilung der langen Verweilzeitlängen an (auf Anrufbasis).
		 */
		DATA_TYPE_CALLER_STAYINGTIME_DIST_LONG,

		/**
		 * Zeigt die Verteilung der Wartezeitlängen und der Verweilzeitlängen an (auf Kundenbasis).
		 */
		DATA_TYPE_CLIENT_WAITINGANDSTAYINGTIME_DIST,

		/**
		 * Zeigt die Verteilung der Wartezeitlängen und der Verweilzeitlängen an (auf Anrufbasis).
		 */
		DATA_TYPE_CALLER_WAITINGANDSTAYINGTIME_DIST,

		/**
		 * Zeigt die Verteilung der langen Wartezeitlängen und der langen Verweilzeitlängen an (auf Kundenbasis).
		 */
		DATA_TYPE_CLIENT_WAITINGANDSTAYINGTIME_DIST_LONG,

		/**
		 * Zeigt die Verteilung der langen Wartezeitlängen und der langen Verweilzeitlängen an (auf Anrufbasis).
		 */
		DATA_TYPE_CALLER_WAITINGANDSTAYINGTIME_DIST_LONG,

		/**
		 * Zeigt die Verteilung der Abbruchzeitlängen an (auf Kundenbasis).
		 */
		DATA_TYPE_CLIENT_CANCELTIME_DIST,

		/**
		 * Zeigt die Verteilung der Abbruchzeitlängen an (auf Anrufbasis).
		 */
		DATA_TYPE_CALLER_CANCELTIME_DIST,

		/**
		 * Zeigt die Verteilung der langen Abbruchzeitlängen an (auf Kundenbasis).
		 */
		DATA_TYPE_CLIENT_CANCELTIME_DIST_LONG,

		/**
		 * Zeigt die Verteilung der langen Abbruchzeitlängen an (auf Anrufbasis).
		 */
		DATA_TYPE_CALLER_CANCELTIME_DIST_LONG,

		/**
		 * Zeigt die mittleren Warte- und Abbruchzeiten über den Tag an (auf Kundenbasis).
		 */
		DATA_TYPE_CLIENT_WAITING_AND_CANCEL,

		/**
		 * Zeigt die mittleren Warte- und Abbruchzeiten über den Tag an (auf Anrufbasis).
		 */
		DATA_TYPE_CALLER_WAITING_AND_CANCEL,

		/**
		 * Zeigt Erreichbarkeit über den Tag an (auf Kundenbasis).
		 */
		DATA_TYPE_CLIENT_SUCCESS,

		/**
		 * Zeigt Erreichbarkeit über den Tag an ("+Language.tr("SimStatistic.OnCallBasis")+").
		 */
		DATA_TYPE_CALLER_SUCCESS,

		/**
		 * Zeigt den Service-Level über den Tag an (auf Kundenbasis, bezogen auf erfolgreiche Kunden).
		 */
		DATA_TYPE_CLIENT_SERVICE_LEVEL,

		/**
		 * Zeigt den Service-Level über den Tag an (auf Anrufbasis, bezogen auf erfolgreiche Anrufe).
		 */
		DATA_TYPE_CALLER_SERVICE_LEVEL,

		/**
		 * Zeigt den Service-Level über den Tag an (auf Kundenbasis, bezogen auf alle Kunden).
		 */
		DATA_TYPE_CLIENT_SERVICE_LEVEL_ALL,

		/**
		 * Zeigt den Service-Level über den Tag an (auf Anrufbasis, bezogen auf alle Anrufe).
		 */
		DATA_TYPE_CALLER_SERVICE_LEVEL_ALL,

		/**
		 * Zeigt die Auslastung der Agenten über den Tag an.
		 */
		DATA_TYPE_SERVICETIMEPART,

		/**
		 * Zeigt die Auslastung der Agenten pro Kundentyp über den Tag an.
		 */
		DATA_TYPE_SERVICETIMEPART_FULL,

		/**
		 * Zeigt die Leerlaufzeiten der Agenten über den Tag an.
		 */
		DATA_TYPE_FREETIME,

		/**
		 * Zeigt die Warteschlangenlänge über den Tag an.
		 */
		DATA_TYPE_QUEUE
	}

	/**
	 * Konstruktor der Klasse <code>StatisticViewerKundenAgentenLineChart</code>
	 * @param statistic	Objekt vom Typ {@link Statistics}, dem die Kunden- bzw. Agentendaten entnommen werden sollen
	 * @param dataType	Darstellungsart, siehe <code>DATA_TYPE_*</code> Konstanten.
	 * @param dataNr	Index im <code>kundenProTyp</code>-Array bzw. im <code>agentenProCallcenter</code>-Array, aus dem die Daten genommen werden sollen. ("-1" bedeutet, dass das <code>kundenGlobal</code>- bzw. das <code>agentenGlobal</code>-Objekt verwendet wird.)
	 */
	public StatisticViewerKundenAgentenLineChart(Statistics statistic, Mode dataType, int dataNr) {
		super();
		this.statistic=statistic;
		this.dataType=dataType;
		this.dataNr=dataNr;
	}

	@Override
	protected void firstChartRequest() {
		initLineChart("");

		switch (dataType) {
		case DATA_TYPE_CALLER:
		case DATA_TYPE_CLIENT_WAITINGTIME_DIST:
		case DATA_TYPE_CALLER_WAITINGTIME_DIST:
		case DATA_TYPE_CLIENT_STAYINGTIME_DIST:
		case DATA_TYPE_CALLER_STAYINGTIME_DIST:
		case DATA_TYPE_CLIENT_WAITINGTIME_DIST_LONG:
		case DATA_TYPE_CALLER_WAITINGTIME_DIST_LONG:
		case DATA_TYPE_CLIENT_STAYINGTIME_DIST_LONG:
		case DATA_TYPE_CALLER_STAYINGTIME_DIST_LONG:
		case DATA_TYPE_CLIENT_WAITINGANDSTAYINGTIME_DIST:
		case DATA_TYPE_CALLER_WAITINGANDSTAYINGTIME_DIST:
		case DATA_TYPE_CLIENT_WAITINGANDSTAYINGTIME_DIST_LONG:
		case DATA_TYPE_CALLER_WAITINGANDSTAYINGTIME_DIST_LONG:
		case DATA_TYPE_CLIENT_CANCELTIME_DIST:
		case DATA_TYPE_CALLER_CANCELTIME_DIST:
		case DATA_TYPE_CLIENT_CANCELTIME_DIST_LONG:
		case DATA_TYPE_CALLER_CANCELTIME_DIST_LONG:
		case DATA_TYPE_CLIENT_WAITING_AND_CANCEL:
		case DATA_TYPE_CALLER_WAITING_AND_CANCEL:
		case DATA_TYPE_CLIENT_SUCCESS:
		case DATA_TYPE_CALLER_SUCCESS:
		case DATA_TYPE_CLIENT_SERVICE_LEVEL:
		case DATA_TYPE_CALLER_SERVICE_LEVEL:
		case DATA_TYPE_CLIENT_SERVICE_LEVEL_ALL:
		case DATA_TYPE_CALLER_SERVICE_LEVEL_ALL:
			firstKundenChartRequest();
			break;
		case DATA_TYPE_SERVICETIMEPART:
		case DATA_TYPE_SERVICETIMEPART_FULL:
		case DATA_TYPE_FREETIME:
			firstAgentenChartRequest();
			break;
		case DATA_TYPE_QUEUE:
			firstQueueChartRequest();
			break;
		}
	}

	/**
	 * Generiert ein Kundendaten-Diagramm.
	 */
	private void firstKundenChartRequest() {
		KundenDaten kunden;
		String type;

		if (dataNr<0) {
			kunden=statistic.kundenGlobal;
			type=Language.tr("SimStatistic.AllClients");
		} else {
			kunden=statistic.kundenProTyp[dataNr];
			type=kunden.name;
		}

		switch (dataType) {
		case DATA_TYPE_CALLER:
			setupChartDayValue(Language.tr("SimStatistic.Caller"),Language.tr("Statistic.LineChart.CountPerHalfHour"));
			addSeries(Language.tr("SimStatistic.FreshCalls")+" ("+type+")",Color.BLUE,kunden.kundenProIntervall.divide(statistic.simulationData.runRepeatCount));
			if (kunden.kundenWiederanruf>0) addSeries(Language.tr("SimStatistic.RecallingClients")+" ("+type+")",Color.GREEN,kunden.kundenWiederanrufProIntervall.divide(statistic.simulationData.runRepeatCount));
			addSeries(Language.tr("SimStatistic.Calls.Info")+" ("+type+")",Color.RED,kunden.anrufeProIntervall.divide(statistic.simulationData.runRepeatCount));
			addFillColor((kunden.kundenWiederanruf>0)?2:1);
			break;

		case DATA_TYPE_CLIENT_WAITINGTIME_DIST:
			setupChartTimeValue(Language.tr("SimStatistic.WaitingTimeDistribution")+" ("+Language.tr("SimStatistic.OnClientBasis")+")",Language.tr("SimStatistic.WaitingTime"),Language.tr("Statistic.Frequency"));
			addSeriesPart(Language.tr("SimStatistic.WaitingTimeDistribution")+" ("+type+")",Color.BLUE,kunden.kundenWartezeitVerteilung.divide(statistic.simulationData.runRepeatCount),1200,5);
			addFillColor(0);
			smartZoom(10);
			break;

		case DATA_TYPE_CALLER_WAITINGTIME_DIST:
			setupChartTimeValue(Language.tr("SimStatistic.WaitingTimeDistribution")+" ("+Language.tr("SimStatistic.OnCallBasis")+")",Language.tr("SimStatistic.WaitingTime"),Language.tr("Statistic.Frequency"));
			addSeriesPart(Language.tr("SimStatistic.WaitingTimeDistribution")+" ("+type+")",Color.BLUE,kunden.anrufeWartezeitVerteilung.divide(statistic.simulationData.runRepeatCount),1200,5);
			addFillColor(0);
			smartZoom(10);
			break;

		case DATA_TYPE_CLIENT_STAYINGTIME_DIST:
			setupChartTimeValue(Language.tr("SimStatistic.ResidenceTimeDistribution")+" ("+Language.tr("SimStatistic.OnClientBasis")+")",Language.tr("SimStatistic.ResidenceTime"),Language.tr("Statistic.Frequency"));
			addSeriesPart(Language.tr("SimStatistic.ResidenceTimeDistribution")+" ("+type+")",Color.BLUE,kunden.kundenVerweilzeitVerteilung.divide(statistic.simulationData.runRepeatCount),1200,5);
			addFillColor(0);
			smartZoom(10);
			break;

		case DATA_TYPE_CALLER_STAYINGTIME_DIST:
			setupChartTimeValue(Language.tr("SimStatistic.ResidenceTimeDistribution")+" ("+Language.tr("SimStatistic.OnCallBasis")+")",Language.tr("SimStatistic.ResidenceTime"),Language.tr("Statistic.Frequency"));
			addSeriesPart(Language.tr("SimStatistic.ResidenceTimeDistribution")+" ("+type+")",Color.BLUE,kunden.anrufeVerweilzeitVerteilung.divide(statistic.simulationData.runRepeatCount),1200,5);
			addFillColor(0);
			smartZoom(10);
			break;

		case DATA_TYPE_CLIENT_WAITINGTIME_DIST_LONG:
			setupLongTime(Language.tr("SimStatistic.WaitingTimeDistribution")+" ("+Language.tr("SimStatistic.OnClientBasis")+")",Language.tr("SimStatistic.WaitingTime"),Language.tr("Statistic.Frequency"));
			addLongSeriesPart(Language.tr("SimStatistic.WaitingTimeDistribution")+" ("+type+")",Color.BLUE,kunden.kundenWartezeitVerteilungLang.divide(statistic.simulationData.runRepeatCount),1200,5);
			addFillColor(0);
			smartZoom(10);
			break;

		case DATA_TYPE_CALLER_WAITINGTIME_DIST_LONG:
			setupLongTime(Language.tr("SimStatistic.WaitingTimeDistribution")+" ("+Language.tr("SimStatistic.OnCallBasis")+")",Language.tr("SimStatistic.WaitingTime"),Language.tr("Statistic.Frequency"));
			addLongSeriesPart(Language.tr("SimStatistic.WaitingTimeDistribution")+" ("+type+")",Color.BLUE,kunden.anrufeWartezeitVerteilungLang.divide(statistic.simulationData.runRepeatCount),1200,5);
			addFillColor(0);
			smartZoom(10);
			break;

		case DATA_TYPE_CLIENT_STAYINGTIME_DIST_LONG:
			setupLongTime(Language.tr("SimStatistic.ResidenceTimeDistribution")+" ("+Language.tr("SimStatistic.OnClientBasis")+")",Language.tr("SimStatistic.ResidenceTime"),Language.tr("Statistic.Frequency"));
			addLongSeriesPart(Language.tr("SimStatistic.ResidenceTimeDistribution")+" ("+type+")",Color.BLUE,kunden.kundenVerweilzeitVerteilungLang.divide(statistic.simulationData.runRepeatCount),1200,5);
			addFillColor(0);
			smartZoom(10);
			break;

		case DATA_TYPE_CALLER_STAYINGTIME_DIST_LONG:
			setupLongTime(Language.tr("SimStatistic.ResidenceTimeDistribution")+" ("+Language.tr("SimStatistic.OnCallBasis")+")",Language.tr("SimStatistic.ResidenceTime"),Language.tr("Statistic.Frequency"));
			addLongSeriesPart(Language.tr("SimStatistic.ResidenceTimeDistribution")+" ("+type+")",Color.BLUE,kunden.anrufeVerweilzeitVerteilungLang.divide(statistic.simulationData.runRepeatCount),1200,5);
			addFillColor(0);
			smartZoom(10);
			break;

		case DATA_TYPE_CLIENT_WAITINGANDSTAYINGTIME_DIST:
			setupChartTimeValue(Language.tr("SimStatistic.WaitingResidenceTimeDistribution")+" ("+Language.tr("SimStatistic.OnClientBasis")+")",Language.tr("Statistic.Period"),Language.tr("Statistic.Frequency"));
			addSeriesPart(Language.tr("SimStatistic.WaitingTimeDistribution")+" ("+type+")",Color.RED,kunden.kundenWartezeitVerteilung.divide(statistic.simulationData.runRepeatCount),1200,5);
			addSeriesPart(Language.tr("SimStatistic.ResidenceTimeDistribution")+" ("+type+")",Color.BLUE,kunden.kundenVerweilzeitVerteilung.divide(statistic.simulationData.runRepeatCount),1200,5);
			smartZoom(10);
			break;

		case DATA_TYPE_CALLER_WAITINGANDSTAYINGTIME_DIST:
			setupChartTimeValue(Language.tr("SimStatistic.WaitingResidenceTimeDistribution")+" ("+Language.tr("SimStatistic.OnCallBasis")+")",Language.tr("Statistic.Period"),Language.tr("Statistic.Frequency"));
			addSeriesPart(Language.tr("SimStatistic.WaitingTimeDistribution")+" ("+type+")",Color.RED,kunden.anrufeWartezeitVerteilung.divide(statistic.simulationData.runRepeatCount),1200,5);
			addSeriesPart(Language.tr("SimStatistic.ResidenceTimeDistribution")+" ("+type+")",Color.BLUE,kunden.anrufeVerweilzeitVerteilung.divide(statistic.simulationData.runRepeatCount),1200,5);
			smartZoom(10);
			break;

		case DATA_TYPE_CLIENT_WAITINGANDSTAYINGTIME_DIST_LONG:
			setupLongTime(Language.tr("SimStatistic.WaitingResidenceTimeDistribution")+" ("+Language.tr("SimStatistic.OnClientBasis")+")",Language.tr("Statistic.Period"),Language.tr("Statistic.Frequency"));
			addLongSeriesPart(Language.tr("SimStatistic.WaitingTimeDistribution")+" ("+type+")",Color.RED,kunden.kundenWartezeitVerteilungLang.divide(statistic.simulationData.runRepeatCount),1200,5);
			addLongSeriesPart(Language.tr("SimStatistic.ResidenceTimeDistribution")+" ("+type+")",Color.BLUE,kunden.kundenVerweilzeitVerteilungLang.divide(statistic.simulationData.runRepeatCount),1200,5);
			smartZoom(10);
			break;

		case DATA_TYPE_CALLER_WAITINGANDSTAYINGTIME_DIST_LONG:
			setupLongTime(Language.tr("SimStatistic.WaitingResidenceTimeDistribution")+" ("+Language.tr("SimStatistic.OnCallBasis")+")",Language.tr("Statistic.Period"),Language.tr("Statistic.Frequency"));
			addLongSeriesPart(Language.tr("SimStatistic.WaitingTimeDistribution")+" ("+type+")",Color.RED,kunden.anrufeWartezeitVerteilungLang.divide(statistic.simulationData.runRepeatCount),1200,5);
			addLongSeriesPart(Language.tr("SimStatistic.ResidenceTimeDistribution")+" ("+type+")",Color.BLUE,kunden.anrufeVerweilzeitVerteilungLang.divide(statistic.simulationData.runRepeatCount),1200,5);
			smartZoom(10);
			break;

		case DATA_TYPE_CLIENT_CANCELTIME_DIST:
			setupChartTimeValue(Language.tr("SimStatistic.CancelTimeDistribution")+" ("+Language.tr("SimStatistic.OnClientBasis")+")",Language.tr("SimStatistic.CancelTime"),Language.tr("Statistic.Frequency"));
			addSeriesPart(Language.tr("SimStatistic.CancelTimeDistribution")+" ("+type+")",Color.RED,kunden.kundenAbbruchzeitVerteilung.divide(statistic.simulationData.runRepeatCount),1200,5);
			addFillColor(0);
			smartZoom(10);
			break;

		case DATA_TYPE_CALLER_CANCELTIME_DIST:
			setupChartTimeValue(Language.tr("SimStatistic.CancelTimeDistribution")+" ("+Language.tr("SimStatistic.OnCallBasis")+")",Language.tr("SimStatistic.CancelTime"),Language.tr("Statistic.Frequency"));
			addSeriesPart(Language.tr("SimStatistic.CancelTimeDistribution")+" ("+type+")",Color.RED,kunden.anrufeAbbruchzeitVerteilung.divide(statistic.simulationData.runRepeatCount),1200,5);
			addFillColor(0);
			smartZoom(10);
			break;

		case DATA_TYPE_CLIENT_CANCELTIME_DIST_LONG:
			setupLongTime(Language.tr("SimStatistic.CancelTimeDistribution")+" ("+Language.tr("SimStatistic.OnClientBasis")+")",Language.tr("SimStatistic.CancelTime"),Language.tr("Statistic.Frequency"));
			addLongSeriesPart(Language.tr("SimStatistic.CancelTimeDistribution")+" ("+type+")",Color.RED,kunden.kundenAbbruchzeitVerteilungLang.divide(statistic.simulationData.runRepeatCount),1200,5);
			addFillColor(0);
			smartZoom(10);
			break;

		case DATA_TYPE_CALLER_CANCELTIME_DIST_LONG:
			setupLongTime(Language.tr("SimStatistic.CancelTimeDistribution")+" ("+Language.tr("SimStatistic.OnCallBasis")+")",Language.tr("SimStatistic.CancelTime"),Language.tr("Statistic.Frequency"));
			addLongSeriesPart(Language.tr("SimStatistic.CancelTimeDistribution")+" ("+type+")",Color.RED,kunden.anrufeAbbruchzeitVerteilungLang.divide(statistic.simulationData.runRepeatCount),1200,5);
			addFillColor(0);
			smartZoom(10);
			break;

		case DATA_TYPE_CLIENT_WAITING_AND_CANCEL:
			setupChartDayValue(Language.tr("SimStatistic.AverageWaitingAndCancelTimes")+" ("+Language.tr("SimStatistic.OnClientBasis")+")",Language.tr("SimStatistic.AveragePerHalfHourInterval")+" ("+Language.tr("Statistic.Units.InSeconds")+")");
			addSeries(Language.tr("SimStatistic.AverageWaitingTime")+" ("+type+")",Color.BLUE,kunden.kundenWartezeitSumProIntervall.divide(kunden.kundenProIntervall));
			addSeries(Language.tr("SimStatistic.AverageCancelTime")+" ("+type+")",Color.RED,kunden.kundenAbbruchzeitSumProIntervall.divide(kunden.kundenAbbruchProIntervall));
			addSeriesToSecondSet(Language.tr("SimStatistic.Caller")+" ("+type+")",Color.BLACK,kunden.kundenProIntervall.divide(statistic.simulationData.runRepeatCount));
			plot.getRangeAxis(1).setLabel(Language.tr("SimStatistic.ArrivalsPerHalfHourInterval"));
			addFillColor(0);
			break;

		case DATA_TYPE_CALLER_WAITING_AND_CANCEL:
			setupChartDayValue(Language.tr("SimStatistic.AverageWaitingAndCancelTimes")+" ("+Language.tr("SimStatistic.OnCallBasis")+")",Language.tr("SimStatistic.AveragePerHalfHourInterval")+" ("+Language.tr("Statistic.Units.InSeconds")+")");
			addSeries(Language.tr("SimStatistic.AverageWaitingTime")+" ("+type+")",Color.BLUE,kunden.anrufeWartezeitSumProIntervall.divide(kunden.anrufeProIntervall));
			addSeries(Language.tr("SimStatistic.AverageCancelTime")+" ("+type+")",Color.RED,kunden.anrufeAbbruchzeitSumProIntervall.divide(kunden.anrufeAbbruchProIntervall));
			addSeriesToSecondSet(Language.tr("SimStatistic.Caller")+" ("+type+")",Color.BLACK,kunden.anrufeProIntervall.divide(statistic.simulationData.runRepeatCount));
			plot.getRangeAxis(1).setLabel(Language.tr("SimStatistic.ArrivalsPerHalfHourInterval"));
			addFillColor(0);
			break;

		case DATA_TYPE_CLIENT_SUCCESS:
			setupChartDayPercent(Language.tr("SimStatistic.Accessibility")+" ("+Language.tr("SimStatistic.OnClientBasis")+")",Language.tr("SimStatistic.AveragePerHalfHourInterval"));
			addSeries(Language.tr("SimStatistic.Accessibility")+" ("+type+")",Color.BLUE,kunden.kundenErfolgProIntervall.divide(kunden.kundenProIntervall));
			addSeriesToSecondSet(Language.tr("SimStatistic.Caller")+" ("+type+")",Color.BLACK,kunden.kundenProIntervall.divide(statistic.simulationData.runRepeatCount));
			plot.getRangeAxis(1).setLabel(Language.tr("SimStatistic.ArrivalsPerHalfHourInterval"));
			addFillColor(0);
			break;

		case DATA_TYPE_CALLER_SUCCESS:
			setupChartDayPercent(Language.tr("SimStatistic.Accessibility")+" ("+Language.tr("SimStatistic.OnCallBasis")+")",Language.tr("SimStatistic.AveragePerHalfHourInterval"));
			addSeries(Language.tr("SimStatistic.Accessibility")+" ("+type+")",Color.BLUE,kunden.anrufeErfolgProIntervall.divide(kunden.anrufeProIntervall));
			addSeriesToSecondSet(Language.tr("SimStatistic.Caller")+" ("+type+")",Color.BLACK,kunden.anrufeProIntervall.divide(statistic.simulationData.runRepeatCount));
			plot.getRangeAxis(1).setLabel(Language.tr("SimStatistic.ArrivalsPerHalfHourInterval"));
			addFillColor(0);
			break;

		case DATA_TYPE_CLIENT_SERVICE_LEVEL:
			setupChartDayPercent(Language.tr("SimStatistic.ServiceLevel")+" ("+Language.tr("SimStatistic.OnClientBasis")+", "+Language.tr("SimStatistic.CalculatedOn.SuccessfulClients")+")",Language.tr("SimStatistic.AveragePerHalfHourInterval"));
			addSeries(Language.tr("SimStatistic.ServiceLevel")+" ("+type+")",Color.BLUE,kunden.kundenServicelevelProIntervall.divide(kunden.kundenErfolgProIntervall));
			addSeriesToSecondSet(Language.tr("SimStatistic.Caller")+" ("+type+")",Color.BLACK,kunden.kundenProIntervall.divide(statistic.simulationData.runRepeatCount));
			plot.getRangeAxis(1).setLabel(Language.tr("SimStatistic.ArrivalsPerHalfHourInterval"));
			addFillColor(0);
			break;

		case DATA_TYPE_CALLER_SERVICE_LEVEL:
			setupChartDayPercent(Language.tr("SimStatistic.ServiceLevel")+" ("+Language.tr("SimStatistic.OnCallBasis")+", "+Language.tr("SimStatistic.CalculatedOn.SuccessfulCalls")+")",Language.tr("SimStatistic.AveragePerHalfHourInterval"));
			addSeries(Language.tr("SimStatistic.ServiceLevel")+" ("+type+")",Color.BLUE,kunden.anrufeServicelevelProIntervall.divide(kunden.anrufeErfolgProIntervall));
			addSeriesToSecondSet(Language.tr("SimStatistic.Caller")+" ("+type+")",Color.BLACK,kunden.anrufeProIntervall.divide(statistic.simulationData.runRepeatCount));
			plot.getRangeAxis(1).setLabel(Language.tr("SimStatistic.ArrivalsPerHalfHourInterval"));
			addFillColor(0);
			break;

		case DATA_TYPE_CLIENT_SERVICE_LEVEL_ALL:
			setupChartDayPercent(Language.tr("SimStatistic.ServiceLevel")+" ("+Language.tr("SimStatistic.OnClientBasis")+", "+Language.tr("SimStatistic.CalculatedOn.AllClients")+")",Language.tr("SimStatistic.AveragePerHalfHourInterval"));
			addSeries(Language.tr("SimStatistic.ServiceLevel")+" ("+type+")",Color.BLUE,kunden.kundenServicelevelProIntervall.divide(kunden.kundenProIntervall));
			addSeriesToSecondSet(Language.tr("SimStatistic.Caller")+" ("+type+")",Color.BLACK,kunden.kundenProIntervall.divide(statistic.simulationData.runRepeatCount));
			plot.getRangeAxis(1).setLabel(Language.tr("SimStatistic.ArrivalsPerHalfHourInterval"));
			addFillColor(0);
			break;

		case DATA_TYPE_CALLER_SERVICE_LEVEL_ALL:
			setupChartDayPercent(Language.tr("SimStatistic.ServiceLevel")+" ("+Language.tr("SimStatistic.OnCallBasis")+", "+Language.tr("SimStatistic.CalculatedOn.AllCalls")+")",Language.tr("SimStatistic.AveragePerHalfHourInterval"));
			addSeries(Language.tr("SimStatistic.ServiceLevel")+" ("+type+")",Color.BLUE,kunden.anrufeServicelevelProIntervall.divide(kunden.anrufeProIntervall));
			addSeriesToSecondSet(Language.tr("SimStatistic.Caller")+" ("+type+")",Color.BLACK,kunden.anrufeProIntervall.divide(statistic.simulationData.runRepeatCount));
			plot.getRangeAxis(1).setLabel(Language.tr("SimStatistic.ArrivalsPerHalfHourInterval"));
			addFillColor(0);
			break;

		default:
			/* Andere Typen treten hier nicht auf.  */
			break;
		}

		initTooltips();
	}

	/**
	 * Liefert die Verteilung der aktiven Agenten pro Intervall.
	 * @param nr	Nummer des Callcenters oder -1 für den globalen Wert über alle Callcenter
	 * @return	Verteilung der aktiven Agenten pro Intervall
	 */
	private DataDistributionImpl agentenProInterval(final int nr) {
		DataDistributionImpl dist=new DataDistributionImpl(48,48);

		/* Liste über alle Callcenter ? */
		if (nr<0) {
			for (int i=0;i<statistic.editModel.callcenter.size();i++) if (statistic.editModel.callcenter.get(i).active) dist=dist.add(agentenProInterval(i));
			return dist;
		}

		/* Agentenliste aufstellen und ggf. Schichtpläne berechnen */

		int editNr=-1, index=-1;
		for (int i=0;i<statistic.editModel.callcenter.size();i++) if (statistic.editModel.callcenter.get(i).active) {
			index++;
			if (index==nr) {editNr=i; break;}
		}
		CallcenterModelCallcenter callcenter=statistic.editModel.callcenter.get(editNr);
		List<CallcenterModelAgent> translatedAgents=new ArrayList<>();
		for (int i=0;i<callcenter.agents.size();i++) translatedAgents.addAll(callcenter.agents.get(i).calcAgentShifts(false,callcenter,statistic.editModel,true));

		/* Zusammenzählen */
		for (int i=0;i<48;i++) {
			int count=0;
			for (int j=0;j<translatedAgents.size();j++) {
				CallcenterModelAgent a=translatedAgents.get(j);
				if (a.workingTimeStart>1800*i+1799) continue;
				if (a.workingTimeEnd<=1800*i+1 && !a.workingNoEndTime) continue;
				count+=a.count;
			}
			dist.densityData[i]=count;
		}
		return dist;
	}

	/**
	 * Generiert ein Agentendaten-Diagramm.
	 */
	private void firstAgentenChartRequest() {
		AgentenDaten agenten;
		String type;
		DataDistributionImpl arbeit;
		DataDistributionImpl gesamt;

		if (dataNr<0) {
			agenten=statistic.agentenGlobal;
			type=Language.tr("SimStatistic.AllAgents");
		} else {
			agenten=statistic.agentenProCallcenter[dataNr];
			type=agenten.name;
		}

		switch (dataType) {
		case DATA_TYPE_SERVICETIMEPART:
			setupChartDayPercent(Language.tr("SimStatistic.WorkLoad"),Language.tr("SimStatistic.AveragePerHalfHourInterval"));
			arbeit=agenten.technischerLeerlaufProIntervall.add(agenten.arbeitProIntervall).add(agenten.postProcessingProIntervall);
			gesamt=arbeit.add(agenten.leerlaufProIntervall);
			addSeries(Language.tr("SimStatistic.WorkLoad")+" ("+type+")",Color.RED,arbeit.divide(gesamt));
			addSeriesToSecondSet(Language.tr("SimStatistic.ActiveAgents")+" ("+type+")",Color.BLACK,agentenProInterval(dataNr));
			plot.getRangeAxis(1).setLabel(Language.tr("SimStatistic.ActiveAgentsPerHalfHourInterval"));
			addFillColor(0);
			break;

		case DATA_TYPE_SERVICETIMEPART_FULL:
			setupChartDayPercent(Language.tr("SimStatistic.WorkLoad"),Language.tr("SimStatistic.AveragePerHalfHourInterval"));
			final Color[] colors=new Color[]{Color.BLUE,Color.GREEN,Color.CYAN,Color.MAGENTA,Color.ORANGE};
			arbeit=agenten.technischerLeerlaufProIntervall.add(agenten.arbeitProIntervall).add(agenten.postProcessingProIntervall);
			gesamt=arbeit.add(agenten.leerlaufProIntervall);
			int nr=0, count=0;
			for (int i=0;i<agenten.dataByCaller.length;i++) {
				if (agenten.dataByCallerTechnial[i]+agenten.dataByCallerService[i]+agenten.dataByCallerPostProcessing[i]==0) continue;
				count++;
				String name=agenten.dataByCaller[i];
				Color c=colors[nr%colors.length]; nr++;
				DataDistributionImpl teil=agenten.dataByCallerTechnialProIntervall[i].add(agenten.dataByCallerServiceProIntervall[i]).add(agenten.dataByCallerPostProcessingProIntervall[i]);
				addSeries(Language.tr("SimStatistic.WorkLoad")+" ("+type+", "+name+")",c,teil.divide(gesamt));
			}
			addSeries(Language.tr("SimStatistic.WorkLoad")+" ("+type+")",Color.RED,arbeit.divide(gesamt));
			addSeriesToSecondSet(Language.tr("SimStatistic.ActiveAgents")+" ("+type+")",Color.BLACK,agentenProInterval(dataNr));
			plot.getRangeAxis(1).setLabel(Language.tr("SimStatistic.ActiveAgentsPerHalfHourInterval"));
			addFillColor(count);
			break;

		case DATA_TYPE_FREETIME:
			setupChartDayValue(Language.tr("SimStatistic.IdleTime"),Language.tr("SimStatistic.AveragePerHalfHourInterval")+" ("+Language.tr("Statistic.Units.InMinutes")+")");
			addSeries(Language.tr("SimStatistic.IdleTime")+" ("+type+")",Color.RED,agenten.leerlaufProIntervall.divide(statistic.simulationData.runRepeatCount*60));
			addSeriesToSecondSet(Language.tr("SimStatistic.ActiveAgents")+" ("+type+")",Color.BLACK,agentenProInterval(dataNr));
			plot.getRangeAxis(1).setLabel(Language.tr("SimStatistic.ActiveAgentsPerHalfHourInterval"));
			addFillColor(0);
			break;
		default:
			/* Andere Typen treten hier nicht auf.  */
			break;
		}
	}

	/**
	 * Generiert ein Warteschlangenlängen-Diagramm.
	 */
	private void firstQueueChartRequest() {
		switch (dataType) {
		case DATA_TYPE_QUEUE:
			setupChartDayValue(Language.tr("Statistic.QueueLength"),Language.tr("Statistic.QueueLength"));
			addSeries(Language.tr("Statistic.QueueLength")+" ("+Language.tr("Statistic.QueueLength.AveragePerHalfHourInterval")+")",Color.RED,statistic.meanQueueLengthProIntervall);
			addFillColor(0);
			break;
		default:
			/* Andere Typen treten hier nicht auf.  */
			break;
		}
	}
}
