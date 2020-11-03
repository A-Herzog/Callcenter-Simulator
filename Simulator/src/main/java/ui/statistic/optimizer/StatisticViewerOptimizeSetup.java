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
package ui.statistic.optimizer;

import language.Language;
import mathtools.NumberTools;
import mathtools.TimeTools;
import simulator.Statistics;
import systemtools.statistics.StatisticViewerText;
import ui.optimizer.OptimizeData;
import ui.optimizer.OptimizeSetup;

/**
 * Zeigt die Einstellungen des Optimierers an
 * @author Alexander Herzog
 * @version 1.0
 */
public class StatisticViewerOptimizeSetup extends StatisticViewerText {
	/** Optimierungsergebnisobjekt dem die Optimierungsergebnisse entnommen werden sollen */
	private final OptimizeData results;
	/** Gibt an, welche Informationen ausgegeben werden sollen */
	private final Mode dataType;

	/**
	 * Welche Informationen sollen angezeigt werden?
	 * @author Alexander Herzog
	 * @see StatisticViewerOptimizeSetup#StatisticViewerOptimizeSetup(OptimizeData, Mode)
	 */
	public enum Mode {
		/** Einstellungen der Optimierung */
		DATA_SETUP,
		/** Angaben zum verwendeten System */
		DATA_SYSTEM
	}

	/**
	 * Konstruktor der Klasse
	 * @param results	Optimierungsergebnisobjekt dem die Optimierungsergebnisse entnommen werden sollen
	 * @param dataType	Gibt an, welche Informationen ausgegeben werden sollen
	 */
	public StatisticViewerOptimizeSetup(OptimizeData results, Mode dataType) {
		super();
		this.results=results;
		this.dataType=dataType;
	}

	/**
	 * Ausgabe der
	 * Einstellungen der Optimierung
	 * @see Mode#DATA_SETUP
	 */
	private void buildTextSetup() {
		String s="",t="";

		addHeading(1,Language.tr("SimStatistic.OptimizeSetup.Title"));

		addHeading(2,Language.tr("SimStatistic.OptimizeSetup.Property"));
		beginParagraph();
		switch (results.setup.optimizeProperty) {
		case OPTIMIZE_PROPERTY_SUCCESS_BY_CALL: s=Language.tr("SimStatistic.OptimizeSetup.Property.Accessibility")+" ("+Language.tr("SimStatistic.OnCallBasis")+")"; break;
		case OPTIMIZE_PROPERTY_SUCCESS_BY_CLIENT: s=Language.tr("SimStatistic.OptimizeSetup.Property.Accessibility")+" ("+Language.tr("SimStatistic.OnClientBasis")+")"; break;
		case OPTIMIZE_PROPERTY_WAITING_TIME_BY_CALL: s=Language.tr("SimStatistic.OptimizeSetup.Property.WaitingTime")+" ("+Language.tr("SimStatistic.OnCallBasis")+") "; break;
		case OPTIMIZE_PROPERTY_WAITING_TIME_BY_CLIENT: s=Language.tr("SimStatistic.OptimizeSetup.Property.WaitingTime")+" ("+Language.tr("SimStatistic.OnClientBasis")+") "; break;
		case OPTIMIZE_PROPERTY_STAYING_TIME_BY_CALL: s=Language.tr("SimStatistic.OptimizeSetup.Property.ResidenceTime")+" ("+Language.tr("SimStatistic.OnCallBasis")+")"; break;
		case OPTIMIZE_PROPERTY_STAYING_TIME_BY_CLIENT: s=Language.tr("SimStatistic.OptimizeSetup.Property.ResidenceTime")+" ("+Language.tr("SimStatistic.OnClientBasis")+")"; break;
		case OPTIMIZE_PROPERTY_SERVICE_LEVEL_BY_CALL: s=Language.tr("SimStatistic.OptimizeSetup.Property.ServiceLevel")+" ("+Language.tr("SimStatistic.OnCallBasis")+" "+Language.tr("SimStatistic.CalculatedOn.SuccessfulCalls")+")"; break;
		case OPTIMIZE_PROPERTY_SERVICE_LEVEL_BY_CALL_ALL: s=Language.tr("SimStatistic.OptimizeSetup.Property.ServiceLevel")+" ("+Language.tr("SimStatistic.OnCallBasis")+" "+Language.tr("SimStatistic.CalculatedOn.AllCalls")+")"; break;
		case OPTIMIZE_PROPERTY_SERVICE_LEVEL_BY_CLIENT: s=Language.tr("SimStatistic.OptimizeSetup.Property.ServiceLevel")+" ("+Language.tr("SimStatistic.OnClientBasis")+" "+Language.tr("SimStatistic.CalculatedOn.SuccessfulClients")+")"; break;
		case OPTIMIZE_PROPERTY_SERVICE_LEVEL_BY_CLIENT_ALL: s=Language.tr("SimStatistic.OptimizeSetup.Property.ServiceLevel")+" ("+Language.tr("SimStatistic.OnClientBasis")+" "+Language.tr("SimStatistic.CalculatedOn.AllClients")+")"; break;
		case OPTIMIZE_PROPERTY_WORK_LOAD: s=Language.tr("SimStatistic.OptimizeSetup.Property.WorkLoad"); break;
		}
		addLine(s);
		switch (results.setup.optimizeProperty) {
		case OPTIMIZE_PROPERTY_WAITING_TIME_BY_CALL:
		case OPTIMIZE_PROPERTY_STAYING_TIME_BY_CALL:
		case OPTIMIZE_PROPERTY_STAYING_TIME_BY_CLIENT:
		case OPTIMIZE_PROPERTY_SUCCESS_BY_CLIENT:
		case OPTIMIZE_PROPERTY_WAITING_TIME_BY_CLIENT:
			s=TimeTools.formatTime((int)Math.round(results.setup.optimizeValue));
			if (results.setup.optimizeMaxValue>=0) t=TimeTools.formatTime((int)Math.round(results.setup.optimizeMaxValue));
			break;
		case OPTIMIZE_PROPERTY_SUCCESS_BY_CALL:
		case OPTIMIZE_PROPERTY_SERVICE_LEVEL_BY_CALL:
		case OPTIMIZE_PROPERTY_SERVICE_LEVEL_BY_CALL_ALL:
		case OPTIMIZE_PROPERTY_SERVICE_LEVEL_BY_CLIENT:
		case OPTIMIZE_PROPERTY_SERVICE_LEVEL_BY_CLIENT_ALL:
		case OPTIMIZE_PROPERTY_WORK_LOAD:
			s=NumberTools.formatNumber(results.setup.optimizeValue*100)+"%";
			if (results.setup.optimizeMaxValue>=0) t=NumberTools.formatNumber(results.setup.optimizeMaxValue*100)+"%";
			break;
		}
		if (t.isEmpty()) {
			addLine(Language.tr("SimStatistic.OptimizeSetup.Target.Value")+": "+s);
		} else {
			addLine(Language.tr("SimStatistic.OptimizeSetup.Target.Range")+": "+s+"..."+t);
		}

		switch (results.setup.optimizeByInterval) {
		case OPTIMIZE_BY_INTERVAL_NO:
			addLine(Language.tr("SimStatistic.OptimizeSetup.Target.Day"));
			break;
		case OPTIMIZE_BY_INTERVAL_YES:
		case OPTIMIZE_BY_INTERVAL_IN_ORDER:
			addLine(Language.tr("SimStatistic.OptimizeSetup.Target.Interval"));
			break;
		}

		switch (results.setup.optimizeGroups) {
		case OPTIMIZE_GROUPS_AVERAGE:
			if (results.setup.optimizeProperty==OptimizeSetup.OptimizeProperty.OPTIMIZE_PROPERTY_WORK_LOAD)
				addLine(Language.tr("SimStatistic.OptimizeSetup.Target.AllAgents"));
			else
				addLine(Language.tr("SimStatistic.OptimizeSetup.Target.AllCaller"));
			break;
		case OPTIMIZE_GROUPS_ALL:
			if (results.setup.optimizeProperty==OptimizeSetup.OptimizeProperty.OPTIMIZE_PROPERTY_WORK_LOAD)
				addLine(Language.tr("SimStatistic.OptimizeSetup.Target.AllAgentGroups"));
			else
				addLine(Language.tr("SimStatistic.OptimizeSetup.Target.AllCallerGroups"));
			break;
		case OPTIMIZE_GROUPS_SELECTION:
			if (results.setup.optimizeProperty==OptimizeSetup.OptimizeProperty.OPTIMIZE_PROPERTY_WORK_LOAD)
				addLine(Language.tr("SimStatistic.OptimizeSetup.Target.SelectCallcenter"));
			else
				addLine(Language.tr("SimStatistic.OptimizeSetup.Target.SelectCaller"));
			for (int i=0;i<results.setup.optimizeGroupNames.length;i++) addLine(results.setup.optimizeGroupNames[i]);
			break;
		}
		endParagraph();

		addHeading(2,Language.tr("SimStatistic.OptimizeSetup.Change.Title"));
		beginParagraph();
		addLine(String.format(Language.tr("SimStatistic.OptimizeSetup.Change.AgentNumber"),NumberTools.formatPercent(results.setup.changeValue)));

		if (results.setup.changeAll) {
			addLine(Language.tr("SimStatistic.OptimizeSetup.Change.AgentGroups.All"));
		} else {
			addLine(Language.tr("SimStatistic.OptimizeSetup.Change.AgentGroups.Select"));
			Object[] obj=OptimizeSetup.splitCallcenterAgentGroupData(results.setup.changeGroups);
			String[] callcenter=(String[])obj[0];
			int[] group=(int[])obj[1];
			for (int i=0;i<callcenter.length;i++) addLine(callcenter[i]+" "+Language.tr("SimStatistic.AgentGroup")+" "+group[i]);
		}

		if (results.setup.optimizeByInterval==OptimizeSetup.OptimizeInterval.OPTIMIZE_BY_INTERVAL_IN_ORDER) {
			addLines(Language.tr("SimStatistic.OptimizeSetup.Process.IntervalByInterval"));
		} else {
			addLines(Language.tr("SimStatistic.OptimizeSetup.Process.AllIntervalAtTheSameTime"));
		}
		endParagraph();

		addHeading(2,Language.tr("SimStatistic.OptimizeSetup.Intervals.Title"));
		beginParagraph();
		if (results.setup.optimizeIntervals.getMin()>0.1) {
			addLines(Language.tr("SimStatistic.OptimizeSetup.Intervals.All"));
		} else {
			addLines(Language.tr("SimStatistic.OptimizeSetup.Intervals.List"));
			for (int i=0;i<results.setup.optimizeIntervals.densityData.length;i++) if (results.setup.optimizeIntervals.densityData[i]>0.1)
				addLines(TimeTools.formatShortTime(i*1800)+"-"+TimeTools.formatShortTime((i+1)*1800));
		}
		endParagraph();
	}

	/**
	 * Ausgabe der
	 * Angaben zum verwendeten System
	 * @see Mode#DATA_SYSTEM
	 */
	private void buildTextSystem() {
		addHeading(1,Language.tr("SimStatistic.SystemData"));

		beginParagraph();
		Statistics statistic=results.data.get(0);
		addLine(Language.tr("SimStatistic.SystemData.Version")+": "+statistic.editModel.version);
		if (!statistic.simulationData.runUser.isEmpty()) addLine(Language.tr("SimStatistic.SystemData.User")+": "+statistic.simulationData.runUser);
		if (!statistic.simulationData.runDate.isEmpty()) addLine(Language.tr("SimStatistic.SystemData.Date")+": "+statistic.simulationData.runDate);
		addLine(Language.tr("SimStatistic.SystemData.Threads")+": "+NumberTools.formatLong(statistic.simulationData.runThreads));
		if (!statistic.simulationData.runOS.isEmpty()) addLine(Language.tr("SimStatistic.SystemData.ServerOS")+": "+statistic.simulationData.runOS);
		addLine(Language.tr("SimStatistic.SystemData.SimDays")+": "+NumberTools.formatLong(statistic.simulationData.runRepeatCount));
		endParagraph();

		if (results.runTime>0) {
			addHeading(2,Language.tr("SimStatistic.OptimizeSetup.RunTime"));
			beginParagraph();
			if (results.runCount>0) addLine(""+results.runCount+" "+Language.tr("SimStatistic.OptimizeSetup.RunTime.RunCount"));
			addLine(""+results.data.size()+" "+Language.tr("SimStatistic.OptimizeSetup.RunTime.ResultCount"));
			addLine(""+results.runTime+" "+Language.tr("Statistic.Seconds")+" "+Language.tr("SimStatistic.OptimizeSetup.RunTime.Time"));
			endParagraph();
		}
	}

	@Override
	protected void buildText() {
		switch (dataType) {
		case DATA_SETUP: buildTextSetup(); break;
		case DATA_SYSTEM: buildTextSystem(); break;
		}
	}

}
