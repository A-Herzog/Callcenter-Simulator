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
import systemtools.statistics.StatisticViewerText;
import ui.model.CallcenterModel;
import ui.statistic.simulation.StatisticViewerTextInformation;

/**
 * Dieser Viewer zeigt Informationen zu den jeweils verfügbaren
 * Agenten pro Callcenter oder pro Skill-Level als Text an.
 * @author Alexander Herzog
 */
public class StatisticViewerAgentsText extends StatisticViewerText {
	private final CallcenterModel model;
	private final Mode viewType;

	/**
	 * Welche Informationen sollen angezeigt werden?
	 * @author Alexander Herzog
	 * @see StatisticViewerAgentsText#StatisticViewerAgentsText(CallcenterModel, Mode)
	 */
	public enum Mode {
		/**
		 * Agenten nach Callcentern sortieren
		 */
		BY_CALLCENTER,

		/**
		 * Agenten nach Skill-Levels sortieren
		 */
		BY_SKILLLEVEL
	}

	/**
	 * Konstruktor der Klasse <code>StatisticViewerAgentsText</code>
	 * @param model	Callcenter-Modell, aus dem die Daten gewonnen werden sollen
	 * @param viewType	Bestimmt, wie die Agenten-Arbeitszeiten sortiert bzw. zusammengefasst werden sollen
	 */
	public StatisticViewerAgentsText(CallcenterModel model, Mode viewType) {
		super();
		this.model=model;
		this.viewType=viewType;
	}

	private void buildAgentData(String name, boolean isSkillLevel) {
		if (name==null || name.isEmpty()) addHeading(2,Language.tr("SimStatistic.AllActiveAgents")); else addHeading(2,name);

		endParagraph();
		double brutto=StatisticViewerTextInformation.getAgentsTimes(model,(!isSkillLevel)?name:null,(isSkillLevel)?name:null,false);
		double netto=StatisticViewerTextInformation.getAgentsTimes(model,(!isSkillLevel)?name:null,(isSkillLevel)?name:null,true);
		if (Math.abs(brutto-netto)<0.1) {
			addLine(1,Language.tr("SimStatistic.AgentsWorkingHours.Scheduled")+": "+NumberTools.formatNumberLong(brutto/3600)+" "+Language.tr("Statistic.Units.Hours.lower"));
		} else {
			addLine(1,Language.tr("SimStatistic.AgentsWorkingHours.ScheduledBrutto")+": "+NumberTools.formatNumberLong(brutto/3600)+" "+Language.tr("Statistic.Units.Hours.lower"));
			addLine(1,Language.tr("SimStatistic.AgentsWorkingHours.ScheduledNetto")+": "+NumberTools.formatNumberLong(netto/3600)+" "+Language.tr("Statistic.Units.Hours.lower"));
		}
		endParagraph();
	}

	@Override
	protected void buildText() {
		addHeading(1,Language.tr("SimStatistic.AgentsWorkLoad"));

		buildAgentData(null,false);
		switch (viewType) {
		case BY_CALLCENTER:
			for (int i=0;i<model.callcenter.size();i++) if (model.callcenter.get(i).active) buildAgentData(model.callcenter.get(i).name,false);
			break;
		case BY_SKILLLEVEL:
			for (int i=0;i<model.skills.size();i++) buildAgentData(model.skills.get(i).name,true);
			break;
		}
	}
}
