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

import java.util.ArrayList;
import java.util.List;

import org.apache.commons.math3.stat.StatUtils;

import language.Language;
import mathtools.NumberTools;
import mathtools.TimeTools;
import systemtools.statistics.StatisticViewerTable;

/**
 * Dieser Viewer zeigt Tabellendaten auf Basis einer
 * {@link StatisticViewerErlangCTools}-Berechnung an.
 * @author Alexander Herzog
 * @version 1.0
 */
public class StatisticViewerErlangCTable extends StatisticViewerTable {
	/** Erlang-C-Vergleichsrechnung der die anzuzeigenden Daten entnommen werden sollen */
	private final StatisticViewerErlangCTools erlangCData;

	/**
	 * Konstruktor der Klasse
	 * @param erlangCData	Erlang-C-Vergleichsrechnung der die anzuzeigenden Daten entnommen werden sollen
	 */
	public StatisticViewerErlangCTable(final StatisticViewerErlangCTools erlangCData) {
		super();
		this.erlangCData=erlangCData;
	}

	@Override
	protected void buildTable() {
		final List<String> columnNames=new ArrayList<>();
		final List<List<String>> data=new ArrayList<>();

		columnNames.add(Language.tr("Statistic.Interval"));
		columnNames.add(Language.tr("SimStatistic.Agents"));
		columnNames.add(Language.tr("SimStatistic.FreshAndForwardedCalls"));
		columnNames.add(Language.tr("SimStatistic.Retryer"));
		columnNames.add(Language.tr("SimStatistic.Accessibility"));
		columnNames.add(Language.tr("SimStatistic.AverageWaitingTime"));
		columnNames.add(Language.tr("SimStatistic.ServiceLevel"));

		double[] agents=erlangCData.getAgents();
		double[] freshCalls=erlangCData.getFreshCalls();
		double[] retryCalls=erlangCData.getRetryCalls();
		double[] successProbability=erlangCData.getSuccessProbability();
		double[] meanWaitingTime=erlangCData.getMeanWaitingTime();
		double[] serviceLevel=erlangCData.getServiceLevel();

		for (int i=0;i<48;i++) {
			List<String> row=new ArrayList<String>();
			data.add(row);
			row.add(TimeTools.formatTime(i*1800)+"-"+TimeTools.formatTime((i+1)*1800));
			row.add(NumberTools.formatNumber(agents[i]));
			row.add(NumberTools.formatNumber(freshCalls[i]));
			row.add(NumberTools.formatNumber(retryCalls[i]));
			row.add(NumberTools.formatNumber(successProbability[i]*100)+"%");
			row.add(NumberTools.formatNumber(meanWaitingTime[i]));
			row.add(NumberTools.formatNumber(serviceLevel[i]*100)+"%");
		}
		List<String> col=new ArrayList<String>();
		data.add(col);
		col.add(Language.tr("Statistic.Sum"));
		col.add("");
		col.add(NumberTools.formatNumber(StatUtils.sum(freshCalls)));
		col.add(NumberTools.formatNumber(StatUtils.sum(retryCalls)));
		col.add("");
		col.add("");
		col.add("");

		setData(data,columnNames);
	}
}
