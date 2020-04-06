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
import mathtools.NumberTools;
import systemtools.statistics.StatisticViewerPieChart;
import ui.model.CallcenterModel;

/**
 * Zeigt die Verteilung der Erstanrufer auf die verschiedenen Kundengruppe an.
 * @author Alexander Herzog
 * @version 1.0
 */
public class StatisticViewerCallerPieChart extends StatisticViewerPieChart {
	private final CallcenterModel model;

	/**
	 * Konstruktor der Klasse
	 * @param model Callcenter-Modell, aus dem die Daten gewonnen werden sollen
	 */
	public StatisticViewerCallerPieChart(CallcenterModel model) {
		super();
		this.model=model;
	}

	@Override
	protected void firstChartRequest() {
		initPieChart(Language.tr("SimStatistic.FreshCallsDistribution"));
		for (int i=0;i<model.caller.size();i++) if (model.caller.get(i).active)
			addPieSegment(String.format("%s (%s "+Language.tr("SimStatistic.FreshCalls")+")",model.caller.get(i).name,NumberTools.formatLong(model.caller.get(i).freshCallsCountMean)),model.caller.get(i).freshCallsCountMean);
	}
}
