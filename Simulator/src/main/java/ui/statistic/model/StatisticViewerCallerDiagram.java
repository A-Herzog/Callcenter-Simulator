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
import ui.model.CallcenterModel;

/**
 * Zeigt Informationen über die Anruferverteilung als Diagramm an.
 * @author Alexander Herzog
 * @version 1.0
 */
public class StatisticViewerCallerDiagram extends StatisticViewerLineChart {
	/** Callcenter-Modell, aus dem die Daten gewonnen werden sollen */
	private final CallcenterModel model;
	/** Gibt an, die Anrufer welchen Kundentyps angezeigt werden sollen (-1=alle Kundentypen) */
	private final int callerNr;

	/**
	 * Konstruktor der Klasse
	 * @param model Callcenter-Modell, aus dem die Daten gewonnen werden sollen
	 * @param callerNr Gibt an, die Anrufer welchen Kundentyps angezeigt werden sollen (-1=alle Kundentypen)
	 */
	public StatisticViewerCallerDiagram(final CallcenterModel model, final int callerNr) {
		super();
		this.model=model;
		this.callerNr=callerNr;
	}

	@Override
	protected void firstChartRequest() {
		initLineChart(Language.tr("SimStatistic.CallerDistribution"),Language.tr("SimStatistic.FreshCalls")+" ("+getCallerName(model,callerNr)+")",buildDistribution(model,callerNr),true);
		initTooltips();
	}

	private String getCallerName(CallcenterModel model, int callerNr) {
		if (callerNr<0) return Language.tr("SimStatistic.AllClients"); else return model.caller.get(callerNr).name;
	}

	private DataDistributionImpl buildDistribution(CallcenterModel model, int callerNr) {
		/* Unnormierte Verteilung bestimmen */
		DataDistributionImpl dist=new DataDistributionImpl(48,48);
		for (int i=0;i<dist.densityData.length;i++) {
			dist.densityData[i]=0;
			if (callerNr<0) {
				for (int j=0;j<model.caller.size();j++) if (model.caller.get(j).active) dist.densityData[i]+=model.caller.get(j).getFreshCallsDistOn48Base().densityData[i];
			} else {
				dist.densityData[i]+=model.caller.get(callerNr).getFreshCallsDistOn48Base().densityData[i];
			}
		}

		/* Normieren */
		dist.normalizeDensity();

		return dist;
	}
}
