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

import language.Language;
import mathtools.TimeTools;
import systemtools.statistics.StatisticViewerTable;
import ui.model.CallcenterModel;

/**
 * Zeigt Informationen über die Kundenankünfte als Tabelle an.
 * @author Alexander Herzog
 * @version 1.0
 */
public class StatisticViewerCallerTable extends StatisticViewerTable {
	private final CallcenterModel model;

	/**
	 * Konstruktor der Klasse <code>StatisticViewerCallerTable</code>
	 * @param model	Callcenter-Modell, aus dem die Daten gewonnen werden sollen
	 */
	public StatisticViewerCallerTable(CallcenterModel model) {
		super();
		this.model=model;
	}

	@Override
	protected void buildTable() {
		columnNames.add(Language.tr("Statistic.Interval"));
		int count=0;
		for (int i=0;i<model.caller.size();i++) if (model.caller.get(i).active) {count++; columnNames.add(model.caller.get(i).name);}
		columnNames.add(Language.tr("Statistic.Total"));

		List<String> col;
		double[] sum=new double[count];
		for (int i=0;i<model.caller.size();i++) if (model.caller.get(i).active) sum[i]=model.caller.get(i).getFreshCallsDistOn48Base().sum();

		for (int i=0;i<48;i++) {
			long intervalSum=0;
			data.add(col=new ArrayList<String>());
			col.add(TimeTools.formatTime(i*1800)+"-"+TimeTools.formatTime((i+1)*1800));
			int nr=0;
			for (int j=0;j<model.caller.size();j++) if (model.caller.get(j).active) {
				long n=(sum[nr]>0)?Math.round(model.caller.get(j).freshCallsCountMean*model.caller.get(j).getFreshCallsDistOn48Base().densityData[i]/sum[nr]):0;
				col.add(""+n);
				intervalSum+=n;
				nr++;
			}
			col.add(""+intervalSum);
		}
		data.add(col=new ArrayList<String>());
		col.add(Language.tr("Statistic.WholeDay"));
		int s=0;
		for (int i=0;i<model.caller.size();i++) if (model.caller.get(i).active) {col.add(""+model.caller.get(i).freshCallsCountMean); s+=model.caller.get(i).freshCallsCountMean;}
		col.add(""+s);
	}
}
