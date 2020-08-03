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

import language.Language;
import mathtools.NumberTools;
import simulator.Statistics;
import simulator.Statistics.KundenDaten;
import systemtools.statistics.StatisticViewerPieChart;

/**
 * Zeigt die Anteile der Kundentypen an Anrufern, Abbrechern usw. an.
 * @author Alexander Herzog
 * @version 1.0
 */
public class StatisticViewerKundenAnteilePieChart extends StatisticViewerPieChart {
	private final Statistics statistic;
	private final Mode dataType;

	/**
	 * Welche Informationen sollen angezeigt werden?
	 * @author Alexander Herzog
	 * @see StatisticViewerKundenAnteilePieChart#StatisticViewerKundenAnteilePieChart(Statistics, Mode)
	 */
	public enum Mode {

		/**
		 * Erstanrufer anzeigen.
		 */
		DATA_TYPE_FRESH_CALLS,

		/**
		 * Wiederanrufer anzeigen.
		 */
		DATA_TYPE_RECALLS,

		/**
		 * Alle Anrufer anzeigen.
		 */
		DATA_TYPE_CALLS,

		/**
		 * Abbrecher anzeigen.
		 */
		DATA_TYPE_CANCELED_CALLS,

		/**
		 * Weiterleitungen anzeigen.
		 */
		DATA_TYPE_CONTINUED_CALLS,

		/**
		 * Weiterleitungen anzeigen.
		 */
		DATA_TYPE_RETRIED_CALLS
	}


	/**
	 * Konstruktor der Klasse <code>StatisticViewerKundenAnteilePieChart</code>
	 * @param statistic	Objekt vom Typ <code>ComplexStatisticSimData</code>, dem die Kundendaten entnommen werden sollen
	 * @param dataType	Darstellungsart, siehe <code>DATA_TYPE_*</code> Konstanten.
	 */
	public StatisticViewerKundenAnteilePieChart(Statistics statistic, Mode dataType) {
		super();
		this.statistic=statistic;
		this.dataType=dataType;
	}

	@Override
	protected void firstChartRequest() {
		initPieChart("");

		int sum=0;
		KundenDaten k=statistic.kundenGlobal;
		switch (dataType) {
		case DATA_TYPE_FRESH_CALLS:
			chart.setTitle(Language.tr("SimStatistic.FreshCalls"));
			sum=k.kunden;
			break;
		case DATA_TYPE_RECALLS:
			chart.setTitle(Language.tr("SimStatistic.RecallingClients"));
			sum=k.kundenWiederanruf;
			break;
		case DATA_TYPE_CALLS:
			chart.setTitle(Language.tr("SimStatistic.Calls"));
			sum=(k.anrufe-k.anrufeUebertrag);
			break;
		case DATA_TYPE_CANCELED_CALLS:
			chart.setTitle(Language.tr("SimStatistic.CanceledCalls"));
			sum=k.anrufeAbbruch;
			break;
		case DATA_TYPE_CONTINUED_CALLS:
			chart.setTitle(Language.tr("SimStatistic.ForwardedCalls"));
			sum=k.anrufeWeiterleitungen;
			break;
		case DATA_TYPE_RETRIED_CALLS:
			chart.setTitle(Language.tr("SimStatistic.RetryCalls"));
			sum=k.anrufeWiederholungen;
			break;
		}

		if (sum==0) sum=1;
		for (int i=0;i<statistic.kundenProTyp.length;i++) {
			k=statistic.kundenProTyp[i];
			int value=0;
			switch (dataType) {
			case DATA_TYPE_FRESH_CALLS: value=k.kunden; break;
			case DATA_TYPE_RECALLS:  value=k.kundenWiederanruf; break;
			case DATA_TYPE_CALLS: value=k.anrufe; break;
			case DATA_TYPE_CANCELED_CALLS: value=k.anrufeAbbruch; break;
			case DATA_TYPE_CONTINUED_CALLS: value=k.anrufeWeiterleitungen; break;
			case DATA_TYPE_RETRIED_CALLS: value=k.anrufeWiederholungen; break;
			}
			data.setValue(String.format("%s (%s, %s)",k.name,NumberTools.formatLong(value/statistic.simulationData.runRepeatCount),NumberTools.formatPercent((double)value/sum)),value);
		}
	}

}
