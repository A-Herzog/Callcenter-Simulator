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
package ui.statistic.model;

import language.Language;
import mathtools.distribution.DataDistributionImpl;
import systemtools.statistics.StatisticViewerLineChart;

/**
 * Zeigt Informationen aus dem Erlang-C Modell an.
 * @author Alexander Herzog
 * @version 1.0
 */
public class StatisticViewerErlangCDiagramm extends StatisticViewerLineChart {
	/** Erlang-C Modell aus dem die Daten entnommen werden sollen. */
	private final StatisticViewerErlangCTools erlangCData;
	/** Kann ERREICHBARKEIT, WARTEZEIT oder SERVICELEVEL sein. */
	private final Mode diagramType;

	/**
	 * Welche Informationen sollen angezeigt werden?
	 * @author Alexander Herzog
	 * @see StatisticViewerErlangCDiagramm#StatisticViewerErlangCDiagramm(StatisticViewerErlangCTools, Mode)
	 */
	public enum Mode {
		/**
		 * Erreichbarkeit anzeigen
		 */
		ERREICHBARKEIT,

		/**
		 * Mittlere Wartezeit anzeigen
		 */
		WARTEZEIT,

		/**
		 * Service-Level anzeigen
		 */
		SERVICELEVEL
	}

	/**
	 * Konstruktor der Klasse <code>StatisticViewerErlangCDiagramm</code>.
	 * @param erlangCData Erlang-C Modell aus dem die Daten entnommen werden sollen.
	 * @param diagramType Kann ERREICHBARKEIT, WARTEZEIT oder SERVICELEVEL sein.
	 */
	public StatisticViewerErlangCDiagramm(final StatisticViewerErlangCTools erlangCData, final Mode diagramType) {
		super();
		this.erlangCData=erlangCData;
		this.diagramType=diagramType;
	}

	@Override
	protected void firstChartRequest() {
		initLineChart(getTitle(diagramType),getSeriesTitle(diagramType),getSeriesTitle(diagramType),buildDistribution(erlangCData,diagramType),diagramType!=Mode.WARTEZEIT);
		initTooltips();
	}

	private static String getTitle(Mode diagramType) {
		switch (diagramType) {
		case ERREICHBARKEIT: return Language.tr("SimStatistic.Accessibility.OverTheDay");
		case WARTEZEIT: return Language.tr("SimStatistic.AverageWaitingTime.OverTheDay");
		case SERVICELEVEL: return Language.tr("SimStatistic.ServiceLevel.OverTheDay");
		}
		return "";
	}

	private static String getSeriesTitle(Mode diagramType) {
		switch (diagramType) {
		case ERREICHBARKEIT: return Language.tr("SimStatistic.Accessibility");
		case WARTEZEIT: return Language.tr("SimStatistic.AverageWaitingTime")+" ("+Language.tr("Statistic.Units.InSeconds")+")";
		case SERVICELEVEL: return Language.tr("SimStatistic.ServiceLevel");
		}
		return "";
	}

	private static DataDistributionImpl buildDistribution(StatisticViewerErlangCTools erlangCData, Mode diagramType) {
		DataDistributionImpl dist=new DataDistributionImpl(48,48);
		switch (diagramType) {
		case ERREICHBARKEIT: dist.densityData=erlangCData.getSuccessProbability(); break;
		case WARTEZEIT: dist.densityData=erlangCData.getMeanWaitingTime(); break;
		case SERVICELEVEL: dist.densityData=erlangCData.getServiceLevel(); break;
		}
		return dist;
	}
}
