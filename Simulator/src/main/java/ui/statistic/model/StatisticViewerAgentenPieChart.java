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
import mathtools.NumberTools;
import systemtools.statistics.StatisticViewerPieChart;
import ui.model.CallcenterModel;
import ui.model.CallcenterModelAgent;

/**
 * Zeigt die Verteilung der Agenten nach Skill-Levels.
 * @author Alexander Herzog
 * @version 1.0
 */
public class StatisticViewerAgentenPieChart extends StatisticViewerPieChart {
	private final CallcenterModel model;
	private final int callcenterNr;

	/**
	 * Konstruktor der Klasse <code>StatisticViewerAgentsTable</code>
	 * @param model	Callcenter-Modell, aus dem die Daten gewonnen werden sollen
	 * @param callcenterNr	 0-basierte Nummer des Callcenters zu dem Daten angezeigt werden sollen (Werte &lt;0 werden als "über alle Callcenter" interpretiert)
	 */
	public StatisticViewerAgentenPieChart(CallcenterModel model, int callcenterNr) {
		super();
		this.model=model;
		this.callcenterNr=callcenterNr;
	}

	@Override
	protected void firstChartRequest() {
		initPieChart(Language.tr("SimStatistic.AvailableWorkPerformance"));
		double[] workingHours=calcWorkingHours(model,callcenterNr);

		for (int i=0;i<workingHours.length;i++)
			addPieSegment(String.format("%s (%s "+Language.tr("Statistic.ManHours")+")",model.skills.get(i).name,NumberTools.formatNumber(workingHours[i]/2)),workingHours[i]);
	}

	private double[] calcWorkingHours(CallcenterModel model, int callcenterNr) {
		double[] workingHours=new double[model.skills.size()];
		for (int i=0;i<workingHours.length;i++) workingHours[i]=0;

		if (callcenterNr<0) {
			/* Über alle Callcenter */
			for (int i=0;i<model.callcenter.size();i++) {
				double[] tempBlocks=calcWorkingHours(model,i);
				for (int j=0;j<workingHours.length;j++) workingHours[j]+=tempBlocks[j];
			}

		} else {
			/* Über ein einzelnes Callcenter */
			List<CallcenterModelAgent> agents=model.callcenter.get(callcenterNr).agents;

			for (int i=0;i<model.skills.size();i++) {
				String s=model.skills.get(i).name;
				double sum=0;
				for (int j=0;j<agents.size();j++) {
					CallcenterModelAgent a=agents.get(j);
					if (!a.skillLevel.equalsIgnoreCase(s)) continue;
					if (a.count>=0) {
						/* Feste Arbeitszeiten (Anzahl*Zeitspanne) */
						int ende=(a.workingNoEndTime)?86400:a.workingTimeEnd;
						sum+=(ende-a.workingTimeStart)*a.count;
					} else {
						/* Vorgegebene Verteilung über den Tag / Verteilung gemäß anrufern */
						List<CallcenterModelAgent> translatedAgents=a.calcAgentShifts(false,model.callcenter.get(callcenterNr),model,true);
						for (int l=0;l<translatedAgents.size();l++) {
							CallcenterModelAgent a2=translatedAgents.get(l);
							int ende=(a2.workingNoEndTime)?86400:a2.workingTimeEnd;
							sum+=(ende-a2.workingTimeStart)*a2.count;
						}
					}
				}
				workingHours[i]=sum/3600;
			}
		}
		return workingHours;
	}
}
