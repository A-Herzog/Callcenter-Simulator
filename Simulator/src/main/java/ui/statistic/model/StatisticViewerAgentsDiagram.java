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
import mathtools.distribution.DataDistributionImpl;
import systemtools.statistics.StatisticViewerLineChart;
import ui.model.CallcenterModel;
import ui.model.CallcenterModelAgent;
import ui.model.CallcenterModelCallcenter;
import ui.model.CallcenterModelSkillLevel;

/**
 * Zeigt Informationen über die Agentenarbeitszeiten als Diagramm an.
 * @author Alexander Herzog
 * @version 1.0
 */
public class StatisticViewerAgentsDiagram extends StatisticViewerLineChart {
	/** Callcenter-Modell, aus dem die Daten gewonnen werden sollen */
	private final CallcenterModel model;
	/** Callcenter, in der die jeweilige Agentengruppe arbeitet */
	private final List<CallcenterModelCallcenter> callcenter;
	/** Liste der Agentengruppen */
	private final List<CallcenterModelAgent> agents;
	/** Bestimmt, wie die Agenten-Arbeitszeiten sortiert bzw. zusammengefasst werden sollen */
	private final Mode viewType;
	/** Angabe des Skills bzw. des Kundentyps (siehe <code>viewType</code>) */
	private final String viewValue;

	/**
	 * Welche Informationen sollen angezeigt werden?
	 * @author Alexander Herzog
	 * @see StatisticViewerAgentsDiagram#StatisticViewerAgentsDiagram(CallcenterModel, List, List, Mode, String)
	 */
	public enum Mode {
		/**
		 * Alle Agenten anzeigen
		 */
		ADDBY_ALL,

		/**
		 * Agenten eines Skill-Levels anzeigen
		 */
		ADDBY_SKILLLEVEL,

		/**
		 * Agenten, die einen bestimmten Kundentypen bedienen können, anzeigen
		 */
		ADDBY_SKILL
	}

	/**
	 * Konstruktor der Klasse <code>StatisticViewerAgentsTable</code>
	 * @param model	Callcenter-Modell, aus dem die Daten gewonnen werden sollen
	 * @param agents	Liste der Agentengruppen
	 * @param callcenter	Callcenter, in der die jeweilige Agentengruppe arbeitet
	 * @param viewType	Bestimmt, wie die Agenten-Arbeitszeiten sortiert bzw. zusammengefasst werden sollen
	 * @param viewValue	Angabe des Skills bzw. des Kundentyps (siehe <code>viewType</code>)
	 */
	public StatisticViewerAgentsDiagram(final CallcenterModel model, final List<CallcenterModelAgent> agents, final List<CallcenterModelCallcenter> callcenter, final Mode viewType, final String viewValue) {
		super();
		this.model=model;
		this.callcenter=callcenter;
		this.agents=agents;
		this.viewType=viewType;
		this.viewValue=viewValue;
	}

	@Override
	protected void firstChartRequest() {
		initLineChart(Language.tr("SimStatistic.NumberOfAgents"),Language.tr("SimStatistic.ActiveAgents")+" ("+getAgentenName(viewValue)+")",buildDistribution(model,callcenter,agents,viewType,viewValue),false);
		initTooltips();
	}

	/**
	 * Liefert den Namen der Agentengruppe
	 * @param viewValue	Name der Agentengruppe oder leer bzw. <code>null</code>, wenn es um alle Agenten geht
	 * @return	Name der Agentengruppe
	 */
	private static String getAgentenName(String viewValue) {
		if (viewValue==null || viewValue.isEmpty()) return Language.tr("SimStatistic.AllAgents"); else return viewValue;
	}

	/**
	 * Generiert die Verteilung für die Diagrammdarstellung
	 * @param model	Callcenter-Modell, aus dem die Daten gewonnen werden sollen
	 * @param callcenter	Callcenter, in der die jeweilige Agentengruppe arbeitet
	 * @param agents	Liste der Agentengruppen
	 * @param viewType	Bestimmt, wie die Agenten-Arbeitszeiten sortiert bzw. zusammengefasst werden sollen
	 * @param viewValue	Angabe des Skills bzw. des Kundentyps (siehe <code>viewType</code>)
	 * @return	Verteilung für die Diagrammdarstellung
	 */
	private static DataDistributionImpl buildDistribution(CallcenterModel model, List<CallcenterModelCallcenter> callcenter, List<CallcenterModelAgent> agents, Mode viewType, String viewValue) {
		/* Relevante Agenten bestimmen und ggf. Schichtplan erstellen */
		List<CallcenterModelAgent> translatedAgents=new ArrayList<>();
		for (int i=0;i<agents.size();i++) {
			CallcenterModelAgent a=agents.get(i);

			/* Agentengruppen hinzufügen ? */
			if (viewType==Mode.ADDBY_SKILLLEVEL && !a.skillLevel.equalsIgnoreCase(viewValue)) continue;
			if (viewType==Mode.ADDBY_SKILL) {
				boolean ok=false;
				for (int j=0;j<model.skills.size();j++) if (model.skills.get(j).name.equalsIgnoreCase(a.skillLevel)) {
					CallcenterModelSkillLevel s=model.skills.get(j);
					for (int k=0;k<s.callerTypeName.size();k++) if (s.callerTypeName.get(k).equalsIgnoreCase(viewValue)) {ok=true; break;}
					if (ok) break;
				}
				if (!ok) continue;
			}

			/* Ggf. Schichtplan erstellen und hinzufügen */
			if (a.count>=0) translatedAgents.add(a); else translatedAgents.addAll(a.calcAgentShifts(false,callcenter.get(i),model,true));
		}

		/* Agenten zählen */
		int[] timeTable = new int[48];
		for (int i=0;i<timeTable.length;i++) {
			timeTable[i]=0;
			for (int j=0;j<translatedAgents.size();j++) {
				CallcenterModelAgent a=translatedAgents.get(j);
				if (a.workingTimeStart>1800*i+1799) continue;
				if (a.workingTimeEnd<=1800*i+1 && !a.workingNoEndTime) continue;
				timeTable[i]+=a.count;
			}
		}

		DataDistributionImpl dist=new DataDistributionImpl(48,48);
		for (int i=0;i<timeTable.length;i++) dist.densityData[i]=timeTable[i];
		dist.updateCumulativeDensity();

		return dist;
	}
}
