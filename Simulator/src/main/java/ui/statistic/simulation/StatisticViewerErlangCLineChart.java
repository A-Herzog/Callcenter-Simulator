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

import language.Language;
import mathtools.distribution.DataDistributionImpl;
import simulator.Statistics;
import systemtools.statistics.StatisticViewerLineChart;
import ui.statistic.model.StatisticViewerErlangCTools;

/**
 * Klasse zur Anzeige von Vergleichsdaten zwischen Simulation und Erlang-C-Daten
 * @author Alexander Herzog
 * @version 1.0
 */
public class StatisticViewerErlangCLineChart extends StatisticViewerLineChart {
	/** Darstellungsart, siehe <code>DATA_*</code> Konstanten. */
	private final Mode dataType;
	/** Objekt vom Typ {@link Statistics}, dem die Daten entnommen werden sollen */
	private final Statistics statistic;
	private final StatisticViewerErlangCTools erlangC1;
	private final StatisticViewerErlangCTools erlangC2;

	/**
	 * Welche Informationen sollen angezeigt werden?
	 * @author Alexander Herzog
	 * @see StatisticViewerErlangCLineChart#StatisticViewerErlangCLineChart(Statistics, Mode)
	 */
	public enum Mode {
		/**
		 * Anzeige der Erreichbarkeit
		 */
		DATA_SUCCESS,

		/**
		 * Anzeige der mittleren Wartezeit
		 */
		DATA_WAITING_TIME,

		/**
		 * Anzeige des Service-Levels
		 */
		DATA_SERVICE_LEVEL
	}

	/**
	 * Konstruktor der Klasse <code>StatisticViewerErlangCLineChart</code>
	 * @param statistic	Objekt vom Typ {@link Statistics}, dem die Daten entnommen werden sollen
	 * @param dataType	Darstellungsart, siehe <code>DATA_*</code> Konstanten.
	 */
	public StatisticViewerErlangCLineChart(Statistics statistic, Mode dataType) {
		super();
		this.dataType=dataType;
		this.statistic=statistic;
		erlangC1=new StatisticViewerErlangCTools(statistic.editModel,false);
		erlangC2=new StatisticViewerErlangCTools(statistic.editModel,true);
	}

	@Override
	protected void firstChartRequest() {
		initLineChart("");

		String title;
		DataDistributionImpl erlang1, erlang2, sim;

		switch (dataType) {
		case DATA_SUCCESS:
			title=Language.tr("SimStatistic.Accessibility.OverTheDay");
			erlang1=new DataDistributionImpl(48,erlangC1.getSuccessProbability());
			erlang2=new DataDistributionImpl(48,erlangC2.getSuccessProbability());
			sim=statistic.kundenGlobal.anrufeErfolgProIntervall.divide(statistic.kundenGlobal.anrufeProIntervall);
			setupChartDayPercent(title,Language.tr("SimStatistic.AveragePerHalfHourInterval"));
			break;
		case DATA_WAITING_TIME:
			title=Language.tr("SimStatistic.AverageWaitingTime.OverTheDay");
			erlang1=new DataDistributionImpl(48,erlangC1.getMeanWaitingTime());
			erlang2=new DataDistributionImpl(48,erlangC2.getMeanWaitingTime());
			sim=statistic.kundenGlobal.anrufeWartezeitSumProIntervall.divide(statistic.kundenGlobal.anrufeErfolgProIntervall);
			setupChartDayValue(title,Language.tr("SimStatistic.AveragePerHalfHourInterval")+" ("+Language.tr("Statistic.Units.InSeconds")+")");
			break;
		case DATA_SERVICE_LEVEL:
			title=Language.tr("SimStatistic.ServiceLevel.OverTheDay");
			erlang1=new DataDistributionImpl(48,erlangC1.getServiceLevel());
			erlang2=new DataDistributionImpl(48,erlangC2.getServiceLevel());
			sim=statistic.kundenGlobal.anrufeServicelevelProIntervall.divide(statistic.kundenGlobal.anrufeErfolgProIntervall);
			setupChartDayPercent(title,Language.tr("SimStatistic.AveragePerHalfHourInterval"));
			break;
		default:
			return;
		}

		addSeries(title+" ("+Language.tr("SimStatistic.Type.Simulation")+")",Color.BLUE,sim);
		addSeries(title+" ("+Language.tr("SimStatistic.Type.ErlangCSimple")+")",Color.RED,erlang1);
		addSeries(title+" ("+Language.tr("SimStatistic.Type.ErlangCComplex")+")",Color.ORANGE,erlang2);

		addSeriesToSecondSet(Language.tr("SimStatistic.Caller"),Color.BLACK,statistic.kundenGlobal.kundenProIntervall.divide(statistic.simulationData.runRepeatCount));
		plot.getRangeAxis(1).setLabel(Language.tr("SimStatistic.ArrivalsPerHalfHourInterval"));

		initTooltips();
	}
}
