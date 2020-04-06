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
import ui.model.CallcenterModelAgent;
import ui.model.CallcenterModelCallcenter;

/**
 * Zeigt Informationen über die Agentenarbeitszeien als Tabelle an.
 * @author Alexander Herzog
 * @version 1.0
 */
public class StatisticViewerAgentsTable extends StatisticViewerTable {
	private final CallcenterModel model;
	private final Mode viewType;

	/**
	 * Welche Informationen sollen angezeigt werden?
	 * @author Alexander Herzog
	 * @see StatisticViewerAgentsTable#StatisticViewerAgentsTable(CallcenterModel, Mode)
	 */
	public enum Mode {
		/**
		 * Agenten nach Callcentern sortieren
		 */
		BY_CALLCENTER,

		/**
		 * Agenten nach Skill-Levels sortieren
		 */
		BY_SKILLLEVEL,

		/**
		 * Agenten nach bedienbaren Kundentypen sortieren
		 */
		BY_SKILL
	}

	/**
	 * Konstruktor der Klasse <code>StatisticViewerAgentsTable</code>
	 * @param model	Callcenter-Modell, aus dem die Daten gewonnen werden sollen
	 * @param viewType	Bestimmt, wie die Agenten-Arbeitszeiten sortiert bzw. zusammengefasst werden sollen
	 */
	public StatisticViewerAgentsTable(CallcenterModel model, Mode viewType) {
		super();
		this.model=model;
		this.viewType=viewType;
	}

	@Override
	protected void buildTable() {
		columnNames.add(Language.tr("Statistic.Interval"));

		switch (viewType) {
		case BY_CALLCENTER : callcenterTable(); break;
		case BY_SKILLLEVEL : skillLevelTable(); break;
		case BY_SKILL : skillTable(); break;
		}
	}

	private int activeAgentsInInterval(int interval, List<CallcenterModelAgent> translatedAgents) {
		int count=0;
		for (int i=0;i<translatedAgents.size();i++) {
			CallcenterModelAgent a=translatedAgents.get(i);
			if (a.workingTimeStart>1800*interval+1799) continue;
			if (a.workingTimeEnd<=1800*interval+1 && !a.workingNoEndTime) continue;
			count+=a.count;
		}
		return count;
	}

	private void buildTimeTable(List<ArrayList<CallcenterModelAgent>> translatedAgents, boolean sumColumn) {
		for (int i=0;i<48;i++) {
			int activeSum=0;
			ArrayList<String> col=new ArrayList<String>();
			data.add(col);
			col.add(TimeTools.formatTime(i*1800)+"-"+TimeTools.formatTime((i+1)*1800));
			for (int j=0;j<translatedAgents.size();j++) {
				int activeAgents=activeAgentsInInterval(i,translatedAgents.get(j));
				col.add(""+activeAgents);
				activeSum+=activeAgents;
			}
			if (sumColumn) col.add(""+activeSum);
		}
	}

	private void callcenterTable() {
		/* Spaltenüberschriften */
		for (int i=0;i<model.callcenter.size();i++)	if (model.callcenter.get(i).active) columnNames.add(model.callcenter.get(i).name);
		columnNames.add(Language.tr("Statistic.Total"));

		/* Bei Bedarf Schichtpläne erstellen */
		List<ArrayList<CallcenterModelAgent>> translatedAgents=new ArrayList<ArrayList<CallcenterModelAgent>>();
		for (int i=0;i<model.callcenter.size();i++) if (model.callcenter.get(i).active) {
			CallcenterModelCallcenter c=model.callcenter.get(i);
			if (!c.active) continue;
			ArrayList<CallcenterModelAgent> list=new ArrayList<CallcenterModelAgent>();
			translatedAgents.add(list);
			for (int j=0;j<c.agents.size();j++) {
				CallcenterModelAgent a=c.agents.get(j);
				if (!a.active) continue;
				list.addAll(a.calcAgentShifts(false,c,model,true));
			}
		}

		/* Daten zusammenstellen */
		buildTimeTable(translatedAgents,true);
	}

	private void skillLevelTable() {
		/* Spaltenüberschriften */
		for (int i=0;i<model.skills.size();i++)	columnNames.add(model.skills.get(i).name);
		columnNames.add(Language.tr("Statistic.Total"));

		/* Bei Bedarf Schichtpläne erstellen */
		ArrayList<ArrayList<CallcenterModelAgent>> agents=new ArrayList<ArrayList<CallcenterModelAgent>>();
		for (int i=0;i<model.skills.size();i++) {
			String skillLevelName=model.skills.get(i).name;
			ArrayList<CallcenterModelAgent> list=new ArrayList<CallcenterModelAgent>();
			agents.add(list);
			for (int j=0;j<model.callcenter.size();j++) {
				CallcenterModelCallcenter c=model.callcenter.get(j);
				for (int k=0;k<c.agents.size();k++) if (c.agents.get(k).skillLevel.equalsIgnoreCase(skillLevelName)) {
					list.addAll(c.agents.get(k).calcAgentShifts(false,c,model,true));
				}
			}
		}

		/* Daten zusammenstellen */
		buildTimeTable(agents,true);
	}

	private void skillTable() {
		/* Spaltenüberschriften */
		for (int i=0;i<model.caller.size();i++)	if (model.caller.get(i).active) columnNames.add(model.caller.get(i).name);

		/* Bei Bedarf Schichtpläne erstellen */
		ArrayList<ArrayList<CallcenterModelAgent>> agents=new ArrayList<ArrayList<CallcenterModelAgent>>();
		for (int i=0;i<model.caller.size();i++) if (model.caller.get(i).active) {
			String callerName=model.caller.get(i).name;
			/* Skill-Level-Namen bestimmen, die den gewählten Kundentyp bedienen können */
			ArrayList<String> st=new ArrayList<String>();
			for (int j=0;j<model.skills.size();j++) {
				List<String> l=model.skills.get(j).callerTypeName;
				for (int k=0;k<l.size();k++) if (l.get(k).equalsIgnoreCase(callerName)) {st.add(model.skills.get(j).name); break;}
			}

			ArrayList<CallcenterModelAgent> list=new ArrayList<CallcenterModelAgent>();
			agents.add(list);
			for (int j=0;j<model.callcenter.size();j++) if (model.callcenter.get(j).active) {
				CallcenterModelCallcenter c=model.callcenter.get(j);
				for (int k=0;k<c.agents.size();k++) if (c.agents.get(k).active) {
					String s=c.agents.get(k).skillLevel;
					for (int n=0;n<st.size();n++) if (st.get(n).equalsIgnoreCase(s)) list.addAll(c.agents.get(k).calcAgentShifts(false,c,model,true));
				}
			}
		}

		/* Daten zusammenstellen */
		buildTimeTable(agents,false);
	}
}
