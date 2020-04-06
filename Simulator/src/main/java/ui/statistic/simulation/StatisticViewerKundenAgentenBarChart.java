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

import org.jfree.chart.renderer.category.CategoryItemRenderer;

import language.Language;
import simulator.Statistics;
import simulator.Statistics.AgentenDaten;
import systemtools.statistics.StatisticViewerBarChart;

/**
 * Stellt verschiedene Statistikergebnisse zu den Kunden- und Agentengruppen als Balkendiagramme dar.
 * @author Alexander Herzog
 * @version 1.0
 */
public class StatisticViewerKundenAgentenBarChart extends StatisticViewerBarChart {
	private final Statistics statistic;
	private final Mode dataType;

	/**
	 * Welche Informationen sollen angezeigt werden?
	 * @author Alexander Herzog
	 * @see StatisticViewerKundenAgentenBarChart#StatisticViewerKundenAgentenBarChart(Statistics, Mode)
	 */
	public enum Mode {

		/**
		 * Zeigt einen Wartezeitvergleich nach Kundentypen an (auf Kundenbasis).
		 */
		DATA_TYPE_WAITINGTIME_BYCLIENT,

		/**
		 * Zeigt einen Verweilzeitvergleich nach Kundentypen an (auf Kundenbasis).
		 */
		DATA_TYPE_STAYINGTIME_BYCLIENT,

		/**
		 * Zeigt einen Wartezeit- und Verweilzeitvergleich nach Kundentypen an (auf Kundenbasis).
		 */
		DATA_TYPE_WAITINGANDSTAYINGTIME_BYCLIENT,

		/**
		 * Zeigt einen Wartezeitvergleich nach Kundentypen an (auf Anrufbasis).
		 */
		DATA_TYPE_WAITINGTIME_BYCALL,

		/**
		 * Zeigt einen Verweilzeitvergleich nach Kundentypen an (auf Anrufbasis).
		 */
		DATA_TYPE_STAYINGTIME_BYCALL,

		/**
		 * Zeigt einen Wartezeit- und Verweilzeitvergleich nach Kundentypen an (auf Anrufbasis).
		 */
		DATA_TYPE_WAITINGANDSTAYINGTIME_BYCALL,

		/**
		 * Zeigt einen Vergleich der Erreichbarkeit nach Kundentypen an (auf Kundenbasis).
		 */
		DATA_TYPE_SUCCESS_BYCLIENT,

		/**
		 * Zeigt einen Vergleich des Service-Levels nach Kundentypen an (bezogen auf erfolgreiche Kunden, auf Kundenbasis).
		 */
		DATA_TYPE_SERVICELEVEL_BYCLIENT,

		/**
		 * Zeigt einen Vergleich des Service-Levels nach Kundentypen an (bezogen auf alle Kunden, auf Kundenbasis).
		 */
		DATA_TYPE_SERVICELEVEL_BYCLIENT_ALL,

		/**
		 * Zeigt einen Vergleich der Erreichbarkeit nach Kundentypen an (auf Anrufbasis).
		 */
		DATA_TYPE_SUCCESS_BYCALL,

		/**
		 * Zeigt einen Vergleich des Service-Levels nach Kundentypen an (bezogen auf erfolgreiche Anrufe, auf Anrufbasis).
		 */
		DATA_TYPE_SERVICELEVEL_BYCALL,

		/**
		 * Zeigt einen Vergleich des Service-Levels nach Kundentypen an (bezogen auf alle Anrufe, auf Anrufbasis).
		 */
		DATA_TYPE_SERVICELEVEL_BYCALL_ALL,

		/**
		 * Zeigt einen Vergleich der Auslastung der Agenten nach Callcentern an.
		 */
		DATA_TYPE_WORKING_TIME,

		/**
		 * Zeigt einen Vergleich der Auslastung der Agenten nach Skill-Levels an.
		 */
		DATA_TYPE_WORKING_TIME_BY_SKILL
	}

	/**
	 * Konstruktor der Klasse <code>StatisticViewerKundenAgentenLineChart</code>
	 * @param statistic	Objekt vom Typ <code>ComplexStatisticSimData</code>, dem die Kunden- bzw. die Agentendaten entnommen werden sollen
	 * @param dataType	Darstellungsart, siehe <code>DATA_TYPE_*</code> Konstanten.
	 */
	public StatisticViewerKundenAgentenBarChart(Statistics statistic, Mode dataType) {
		super();
		this.statistic=statistic;
		this.dataType=dataType;
	}

	@Override
	protected void firstChartRequest() {
		initBarChart("");
		long div;
		double v,w;
		CategoryItemRenderer r;

		switch (dataType) {
		case DATA_TYPE_WAITINGTIME_BYCLIENT:
			setupBarChart(Language.tr("SimStatistic.WaitingTimes")+" ("+Language.tr("SimStatistic.OnClientBasis")+")",Language.tr("SimStatistic.ClientTypes"),Language.tr("SimStatistic.AverageWaitingTime")+" ("+Language.tr("Statistic.Units.InSeconds")+")",false);
			for (int i=0;i<statistic.kundenProTyp.length;i++)
				data.addValue(((double)(statistic.kundenProTyp[i].kundenWartezeitSum))/Math.max(1,statistic.kundenProTyp[i].kundenErfolg),Language.tr("SimStatistic.WaitingTime"),statistic.kundenProTyp[i].name);
			data.addValue(((double)statistic.kundenGlobal.kundenWartezeitSum)/Math.max(1,statistic.kundenGlobal.kundenErfolg),Language.tr("SimStatistic.WaitingTime")+" - "+Language.tr("SimStatistic.AllClients"),Language.tr("SimStatistic.AllClients"));
			plot.getRendererForDataset(data).setSeriesPaint(0,Color.RED);
			plot.getRendererForDataset(data).setSeriesPaint(1,Color.RED.darker());
			chart.getLegend().visible=false;
			setOutlineColor(Color.BLACK);
			initTooltips();
			break;

		case DATA_TYPE_STAYINGTIME_BYCLIENT:
			setupBarChart(Language.tr("SimStatistic.ResidenceTimes")+" ("+Language.tr("SimStatistic.OnClientBasis")+")",Language.tr("SimStatistic.ClientTypes"),Language.tr("SimStatistic.AverageResidenceTime")+" ("+Language.tr("Statistic.Units.InSeconds")+")",false);
			for (int i=0;i<statistic.kundenProTyp.length;i++)
				data.addValue(((double)statistic.kundenProTyp[i].kundenVerweilzeitSum)/Math.max(1,statistic.kundenProTyp[i].kundenErfolg),Language.tr("SimStatistic.ResidenceTimes"),statistic.kundenProTyp[i].name);
			data.addValue(((double)statistic.kundenGlobal.kundenVerweilzeitSum)/Math.max(1,statistic.kundenGlobal.kundenErfolg),Language.tr("SimStatistic.ResidenceTimes")+" - "+Language.tr("SimStatistic.AllClients"),Language.tr("SimStatistic.AllClients"));
			plot.getRendererForDataset(data).setSeriesPaint(0,Color.BLUE);
			plot.getRendererForDataset(data).setSeriesPaint(1,Color.BLUE.darker());
			chart.getLegend().visible=false;
			setOutlineColor(Color.BLACK);
			initTooltips();
			break;

		case DATA_TYPE_WAITINGANDSTAYINGTIME_BYCLIENT:
			setupBarChart(Language.tr("SimStatistic.WaitingAndResidenceTimes")+" ("+Language.tr("SimStatistic.OnClientBasis")+")",Language.tr("SimStatistic.ClientTypes"),Language.tr("SimStatistic.AverageTime")+" ("+Language.tr("Statistic.Units.InSeconds")+")",false);
			for (int i=0;i<statistic.kundenProTyp.length;i++) {
				String name=statistic.kundenProTyp[i].name;
				w=(double)statistic.kundenProTyp[i].kundenWartezeitSum/Math.max(1,statistic.kundenProTyp[i].kundenErfolg);
				v=(double)statistic.kundenProTyp[i].kundenVerweilzeitSum/Math.max(1,statistic.kundenProTyp[i].kundenErfolg)-w;
				data.addValue(w,Language.tr("SimStatistic.WaitingTime"),name);
				data.addValue(v,Language.tr("SimStatistic.ResidenceTimesWithoutWaitingTime"),name);
			}
			w=(double)statistic.kundenGlobal.kundenWartezeitSum/Math.max(1,statistic.kundenGlobal.kundenErfolg);
			v=(double)statistic.kundenGlobal.kundenVerweilzeitSum/Math.max(1,statistic.kundenGlobal.kundenErfolg)-w;
			data.addValue(w,Language.tr("SimStatistic.WaitingTime")+" - "+Language.tr("SimStatistic.AllClients"),Language.tr("SimStatistic.AllClients"));
			data.addValue(v,Language.tr("SimStatistic.ResidenceTimesWithoutWaitingTime")+" - "+Language.tr("SimStatistic.AllClients"),Language.tr("SimStatistic.AllClients"));

			r=plot.getRendererForDataset(data);
			r.setSeriesPaint(0,Color.RED);
			r.setSeriesPaint(1,Color.BLUE);
			r.setSeriesPaint(2,Color.RED.darker());
			r.setSeriesPaint(3,Color.BLUE.darker());

			chart.getLegend().visible=false;
			setOutlineColor(Color.BLACK);
			initTooltips();
			break;

		case DATA_TYPE_WAITINGTIME_BYCALL:
			setupBarChart(Language.tr("SimStatistic.WaitingTimes")+" ("+Language.tr("SimStatistic.OnCallBasis")+")",Language.tr("SimStatistic.ClientTypes"),Language.tr("SimStatistic.AverageWaitingTime")+" ("+Language.tr("Statistic.Units.InSeconds")+")",false);
			for (int i=0;i<statistic.kundenProTyp.length;i++)
				data.addValue(statistic.kundenProTyp[i].anrufeWartezeitSum/Math.max(1,statistic.kundenProTyp[i].anrufeErfolg),Language.tr("SimStatistic.WaitingTime"),statistic.kundenProTyp[i].name);
			data.addValue(statistic.kundenGlobal.anrufeWartezeitSum/Math.max(1,statistic.kundenGlobal.anrufeErfolg),Language.tr("SimStatistic.WaitingTime")+" - "+Language.tr("SimStatistic.AllCalls"),Language.tr("SimStatistic.AllClients"));
			plot.getRendererForDataset(data).setSeriesPaint(0,Color.RED);
			plot.getRendererForDataset(data).setSeriesPaint(1,Color.RED.darker());
			chart.getLegend().visible=false;
			setOutlineColor(Color.BLACK);
			initTooltips();
			break;

		case DATA_TYPE_STAYINGTIME_BYCALL:
			setupBarChart(Language.tr("SimStatistic.ResidenceTimes")+" ("+Language.tr("SimStatistic.OnCallBasis")+")",Language.tr("SimStatistic.ClientTypes"),Language.tr("SimStatistic.AverageResidenceTime")+" ("+Language.tr("Statistic.Units.InSeconds")+")",false);
			for (int i=0;i<statistic.kundenProTyp.length;i++)
				data.addValue(statistic.kundenProTyp[i].anrufeVerweilzeitSum/Math.max(1,statistic.kundenProTyp[i].anrufeErfolg),Language.tr("SimStatistic.ResidenceTimes"),statistic.kundenProTyp[i].name);
			data.addValue(statistic.kundenGlobal.anrufeVerweilzeitSum/Math.max(1,statistic.kundenGlobal.anrufeErfolg),Language.tr("SimStatistic.ResidenceTimes")+" - "+Language.tr("SimStatistic.AllCalls"),Language.tr("SimStatistic.AllCalls"));
			plot.getRendererForDataset(data).setSeriesPaint(0,Color.BLUE);
			plot.getRendererForDataset(data).setSeriesPaint(1,Color.BLUE.darker());
			chart.getLegend().visible=false;
			setOutlineColor(Color.BLACK);
			initTooltips();
			break;

		case DATA_TYPE_WAITINGANDSTAYINGTIME_BYCALL:
			setupBarChart(Language.tr("SimStatistic.WaitingAndResidenceTimes")+" ("+Language.tr("SimStatistic.OnCallBasis")+")",Language.tr("SimStatistic.ClientTypes"),Language.tr("SimStatistic.AverageTime")+" ("+Language.tr("Statistic.Units.InSeconds")+")",false);
			for (int i=0;i<statistic.kundenProTyp.length;i++) {
				String name=statistic.kundenProTyp[i].name;
				w=(double)statistic.kundenProTyp[i].anrufeWartezeitSum/Math.max(1,statistic.kundenProTyp[i].anrufeErfolg);
				v=(double)statistic.kundenProTyp[i].anrufeVerweilzeitSum/Math.max(1,statistic.kundenProTyp[i].anrufeErfolg)-w;
				data.addValue(w,Language.tr("SimStatistic.WaitingTime"),name);
				data.addValue(v,Language.tr("SimStatistic.ResidenceTimesWithoutWaitingTime"),name);
			}
			w=(double)statistic.kundenGlobal.anrufeWartezeitSum/Math.max(1,statistic.kundenGlobal.anrufeErfolg);
			v=(double)statistic.kundenGlobal.anrufeVerweilzeitSum/Math.max(1,statistic.kundenGlobal.anrufeErfolg)-w;
			data.addValue(w,Language.tr("SimStatistic.WaitingTime")+" - "+Language.tr("SimStatistic.AllCalls"),Language.tr("SimStatistic.AllCalls"));
			data.addValue(v,Language.tr("SimStatistic.ResidenceTimesWithoutWaitingTime")+" - "+Language.tr("SimStatistic.AllCalls"),Language.tr("SimStatistic.AllCalls"));

			r=plot.getRendererForDataset(data);
			r.setSeriesPaint(0,Color.RED);
			r.setSeriesPaint(1,Color.BLUE);
			r.setSeriesPaint(2,Color.RED.darker());
			r.setSeriesPaint(3,Color.BLUE.darker());

			chart.getLegend().visible=false;
			setOutlineColor(Color.BLACK);
			initTooltips();
			break;

		case DATA_TYPE_SUCCESS_BYCLIENT:
			setupBarChart(Language.tr("SimStatistic.Accessibility")+" ("+Language.tr("SimStatistic.OnClientBasis")+")",Language.tr("SimStatistic.ClientTypes"),Language.tr("SimStatistic.SuccessfulClients"),true);
			for (int i=0;i<statistic.kundenProTyp.length;i++)
				data.addValue((double)statistic.kundenProTyp[i].kundenErfolg/Math.max(1,statistic.kundenProTyp[i].kunden+statistic.kundenProTyp[i].kundenWiederanruf),Language.tr("SimStatistic.Accessibility"),statistic.kundenProTyp[i].name);
			data.addValue((double)statistic.kundenGlobal.kundenErfolg/Math.max(1,statistic.kundenGlobal.kunden+statistic.kundenGlobal.kundenWiederanruf),Language.tr("SimStatistic.Accessibility")+" - "+Language.tr("SimStatistic.AllClients"),Language.tr("SimStatistic.AllClients"));
			plot.getRendererForDataset(data).setSeriesPaint(0,Color.BLUE);
			plot.getRendererForDataset(data).setSeriesPaint(1,Color.RED);
			chart.getLegend().visible=false;
			setOutlineColor(Color.BLACK);
			initTooltips();
			break;

		case DATA_TYPE_SERVICELEVEL_BYCLIENT:
			setupBarChart(Language.tr("SimStatistic.ServiceLevel")+" ("+Language.tr("SimStatistic.CalculatedOn.SuccessfulClients")+", "+Language.tr("SimStatistic.OnClientBasis")+")",Language.tr("SimStatistic.ClientTypes"),Language.tr("SimStatistic.ServiceLevel"),true);
			for (int i=0;i<statistic.kundenProTyp.length;i++)
				data.addValue((double)statistic.kundenProTyp[i].kundenServicelevel/Math.max(1,statistic.kundenProTyp[i].kundenErfolg),Language.tr("SimStatistic.ServiceLevel"),statistic.kundenProTyp[i].name);
			data.addValue((double)statistic.kundenGlobal.kundenServicelevel/Math.max(1,statistic.kundenGlobal.kundenErfolg),Language.tr("SimStatistic.ServiceLevel")+" - "+Language.tr("SimStatistic.AllClients"),Language.tr("SimStatistic.AllClients"));
			plot.getRendererForDataset(data).setSeriesPaint(0,Color.BLUE);
			plot.getRendererForDataset(data).setSeriesPaint(1,Color.RED);
			chart.getLegend().visible=false;
			setOutlineColor(Color.BLACK);
			initTooltips();
			break;

		case DATA_TYPE_SERVICELEVEL_BYCLIENT_ALL:
			setupBarChart(Language.tr("SimStatistic.ServiceLevel")+" ("+Language.tr("SimStatistic.CalculatedOn.AllClients")+", "+Language.tr("SimStatistic.OnClientBasis")+")",Language.tr("SimStatistic.ClientTypes"),Language.tr("SimStatistic.ServiceLevel"),true);
			for (int i=0;i<statistic.kundenProTyp.length;i++)
				data.addValue((double)statistic.kundenProTyp[i].kundenServicelevel/Math.max(1,statistic.kundenProTyp[i].kunden),Language.tr("SimStatistic.ServiceLevel"),statistic.kundenProTyp[i].name);
			data.addValue((double)statistic.kundenGlobal.kundenServicelevel/Math.max(1,statistic.kundenGlobal.kunden),Language.tr("SimStatistic.ServiceLevel")+" - "+Language.tr("SimStatistic.AllClients"),Language.tr("SimStatistic.AllClients"));
			plot.getRendererForDataset(data).setSeriesPaint(0,Color.BLUE);
			plot.getRendererForDataset(data).setSeriesPaint(1,Color.RED);
			chart.getLegend().visible=false;
			setOutlineColor(Color.BLACK);
			initTooltips();
			break;

		case DATA_TYPE_SUCCESS_BYCALL:
			setupBarChart(Language.tr("SimStatistic.Accessibility")+" ("+Language.tr("SimStatistic.OnCallBasis")+")",Language.tr("SimStatistic.ClientTypes"),Language.tr("SimStatistic.SuccessfulClients"),true);
			for (int i=0;i<statistic.kundenProTyp.length;i++)
				data.addValue((double)statistic.kundenProTyp[i].anrufeErfolg/Math.max(1,statistic.kundenProTyp[i].anrufe),Language.tr("SimStatistic.Accessibility"),statistic.kundenProTyp[i].name);
			data.addValue((double)statistic.kundenGlobal.anrufeErfolg/Math.max(1,statistic.kundenGlobal.anrufe),Language.tr("SimStatistic.Accessibility")+" - "+Language.tr("SimStatistic.AllCalls"),Language.tr("SimStatistic.AllCalls"));
			plot.getRendererForDataset(data).setSeriesPaint(0,Color.BLUE);
			plot.getRendererForDataset(data).setSeriesPaint(1,Color.RED);
			chart.getLegend().visible=false;
			setOutlineColor(Color.BLACK);
			initTooltips();
			break;

		case DATA_TYPE_SERVICELEVEL_BYCALL:
			setupBarChart(Language.tr("SimStatistic.ServiceLevel")+" ("+Language.tr("SimStatistic.CalculatedOn.SuccessfulCalls")+", "+Language.tr("SimStatistic.OnCallBasis")+")",Language.tr("SimStatistic.ClientTypes"),Language.tr("SimStatistic.ServiceLevel"),true);
			for (int i=0;i<statistic.kundenProTyp.length;i++)
				data.addValue((double)statistic.kundenProTyp[i].anrufeServicelevel/Math.max(1,statistic.kundenProTyp[i].anrufeErfolg),Language.tr("SimStatistic.ServiceLevel"),statistic.kundenProTyp[i].name);
			data.addValue((double)statistic.kundenGlobal.anrufeServicelevel/Math.max(1,statistic.kundenGlobal.anrufeErfolg),Language.tr("SimStatistic.ServiceLevel")+" - "+Language.tr("SimStatistic.AllCalls"),Language.tr("SimStatistic.AllCalls"));
			plot.getRendererForDataset(data).setSeriesPaint(0,Color.BLUE);
			plot.getRendererForDataset(data).setSeriesPaint(1,Color.RED);
			chart.getLegend().visible=false;
			setOutlineColor(Color.BLACK);
			initTooltips();
			break;

		case DATA_TYPE_SERVICELEVEL_BYCALL_ALL:
			setupBarChart(Language.tr("SimStatistic.ServiceLevel")+" ("+Language.tr("SimStatistic.CalculatedOn.AllCalls")+", "+Language.tr("SimStatistic.OnCallBasis")+")",Language.tr("SimStatistic.ClientTypes"),Language.tr("SimStatistic.ServiceLevel"),true);
			for (int i=0;i<statistic.kundenProTyp.length;i++)
				data.addValue((double)statistic.kundenProTyp[i].anrufeServicelevel/Math.max(1,statistic.kundenProTyp[i].anrufe),Language.tr("SimStatistic.ServiceLevel"),statistic.kundenProTyp[i].name);
			data.addValue((double)statistic.kundenGlobal.anrufeServicelevel/Math.max(1,statistic.kundenGlobal.anrufe),Language.tr("SimStatistic.ServiceLevel")+" - "+Language.tr("SimStatistic.AllCalls"),Language.tr("SimStatistic.AllClients"));
			plot.getRendererForDataset(data).setSeriesPaint(0,Color.BLUE);
			plot.getRendererForDataset(data).setSeriesPaint(1,Color.RED);
			chart.getLegend().visible=false;
			setOutlineColor(Color.BLACK);
			initTooltips();
			break;

		case DATA_TYPE_WORKING_TIME:
			setupBarChart(Language.tr("SimStatistic.AgentWorkingTimes"),Language.tr("SimStatistic.AgentGroups"),Language.tr("SimStatistic.Time")+" ("+Language.tr("Statistic.Units.InMinutes")+")",false);
			div=statistic.simDays*60;
			for (int i=0;i<statistic.agentenProCallcenter.length;i++) {
				AgentenDaten a=statistic.agentenProCallcenter[i];
				data.addValue((double)a.postProcessingGesamt/div,Language.tr("SimStatistic.PostProcessingTime"),a.name);
				data.addValue((double)a.arbeitGesamt/div,Language.tr("SimStatistic.HoldingTime"),a.name);
				data.addValue((double)a.technischerLeerlaufGesamt/div,Language.tr("SimStatistic.TechnicalFreeTime"),a.name);
				data.addValue((double)a.leerlaufGesamt/div,Language.tr("SimStatistic.IdleTime"),a.name);
			}
			plot.getRendererForDataset(data).setSeriesPaint(0,Color.GREEN);
			plot.getRendererForDataset(data).setSeriesPaint(1,Color.RED);
			plot.getRendererForDataset(data).setSeriesPaint(2,Color.BLUE);
			plot.getRendererForDataset(data).setSeriesPaint(3,Color.WHITE);
			setOutlineColor(Color.BLACK);
			initTooltips();
			break;

		case DATA_TYPE_WORKING_TIME_BY_SKILL:
			setupBarChart(Language.tr("SimStatistic.AgentWorkingTimes"),Language.tr("SimStatistic.AgentGroups"),Language.tr("SimStatistic.Time")+" ("+Language.tr("Statistic.Units.InMinutes")+")",false);
			div=statistic.simDays*60;
			for (int i=0;i<statistic.agentenProSkilllevel.length;i++) {
				AgentenDaten a=statistic.agentenProSkilllevel[i];
				data.addValue((double)a.postProcessingGesamt/div,Language.tr("SimStatistic.PostProcessingTime"),a.name);
				data.addValue((double)a.arbeitGesamt/div,Language.tr("SimStatistic.HoldingTime"),a.name);
				data.addValue((double)a.technischerLeerlaufGesamt/div,Language.tr("SimStatistic.TechnicalFreeTime"),a.name);
				data.addValue((double)a.leerlaufGesamt/div,Language.tr("SimStatistic.IdleTime"),a.name);
			}
			plot.getRendererForDataset(data).setSeriesPaint(0,Color.GREEN);
			plot.getRendererForDataset(data).setSeriesPaint(1,Color.RED);
			plot.getRendererForDataset(data).setSeriesPaint(2,Color.BLUE);
			plot.getRendererForDataset(data).setSeriesPaint(3,Color.WHITE);
			setOutlineColor(Color.BLACK);
			initTooltips();
			break;
		}
	}
}
