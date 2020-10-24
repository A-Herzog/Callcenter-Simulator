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

import java.util.List;

import language.Language;
import mathtools.TimeTools;
import systemtools.statistics.StatisticViewerTable;
import ui.model.CallcenterModelAgent;

/**
 * Zeigt einen Schichtplan als Tabelle an.
 * @author Alexander Herzog
 * @version 1.0
 */
public class StatisticViewerAgentShiftPlanTable extends StatisticViewerTable {
	/** Konkrete Agenten deren Arbeitszeiten als Schichtplantabelle dargestellt werden sollen */
	private final List<CallcenterModelAgent> translatedAgents;

	/**
	 * Konstruktor der Klasse
	 * @param translatedAgents	Konkrete Agenten deren Arbeitszeiten als Schichtplantabelle dargestellt werden sollen
	 */
	public StatisticViewerAgentShiftPlanTable(List<CallcenterModelAgent> translatedAgents) {
		super();
		this.translatedAgents=translatedAgents;
	}

	@Override
	protected void buildTable() {
		setData(getTableData(translatedAgents),new String[]{
				Language.tr("SimStatistic.Count"),
				Language.tr("SimStatistic.Shift.Start"),
				Language.tr("SimStatistic.Shift.End"),
				Language.tr("SimStatistic.Shift.Length"),
				Language.tr("SimStatistic.SkillLevel")
		});
	}

	private static String[][] getTableData(List<CallcenterModelAgent> agents) {
		String[][] data=new String[agents.size()+1][];

		int sum=0;
		for (int i=0;i<agents.size();i++) {
			CallcenterModelAgent a=agents.get(i);
			data[i]=new String[5];
			data[i][0]=""+a.count;
			sum+=a.count;
			data[i][1]=TimeTools.formatTime(a.workingTimeStart);
			if (a.workingNoEndTime) {
				data[i][2]="bis Simulationsende";
				data[i][3]=TimeTools.formatTime(86400-a.workingTimeStart);
			} else {
				data[i][2]=TimeTools.formatTime(a.workingTimeEnd);
				data[i][3]=TimeTools.formatTime(a.workingTimeEnd-a.workingTimeStart);
			}
			data[i][4]=a.skillLevel;
		}

		data[agents.size()]=new String[5];
		data[agents.size()][0]=""+sum;
		data[agents.size()][4]=Language.tr("Statistic.Sum");

		return data;
	}
}
