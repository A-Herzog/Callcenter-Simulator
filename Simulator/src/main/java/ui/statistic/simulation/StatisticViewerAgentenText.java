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
import simulator.Statistics.AgentenDaten;
import systemtools.statistics.StatisticViewerText;

/**
 * Zeigt Informationen zu Agentengruppen an.
 * @author Alexander Herzog
 * @version 1.0
 */
public class StatisticViewerAgentenText extends StatisticViewerText {
	/** Statistikobjekt dem die Agentendaten entnommen werden sollen */
	private final Statistics statistic;
	/** Wonach sollen die Agenten gruppiert werden? */
	private final SortType sortType;

	/**
	 * Wonach sollen die Agenten gruppiert werden?
	 * @author Alexander Herzog
	 * @see StatisticViewerAgentenTable
	 */
	public enum SortType {
		/**
		 * Anzeige der Daten nach Callcentern sortiert.
		 */
		SORT_BY_CALLCENTER,

		/**
		 * Anzeige der Daten nach Skill-Levels sortiert.
		 */
		SORT_BY_SKILL_LEVEL
	}

	/**
	 * Konstruktor der Klasse
	 * @param statistic	Statistikobjekt dem die Agentendaten entnommen werden sollen
	 * @param sortType	Wonach sollen die Agenten gruppiert werden?
	 */
	public StatisticViewerAgentenText(Statistics statistic, SortType sortType) {
		super();
		this.statistic=statistic;
		this.sortType=sortType;
	}

	/*
	private void addDescription(final String topic) {
		final URL url=StatisticViewerAgentenText.class.getResource("description_"+Language.getCurrentLanguage()+"/"+topic+".html");
		addDescription(url,helpTopic->Help.topic(getViewer(false),helpTopic));
	}
	 */

	@Override
	protected void buildText() {
		addHeading(1,Language.tr("SimStatistic.AgentsWorkLoad"));
		buildAgentData(statistic.agentenGlobal,statistic.simulationData.runRepeatCount);
		switch (sortType) {
		case SORT_BY_CALLCENTER:
			if (statistic.agentenProCallcenter.length>1) for (int i=0;i<statistic.agentenProCallcenter.length;i++)
				buildAgentData(statistic.agentenProCallcenter[i],statistic.simulationData.runRepeatCount);
			break;
		case SORT_BY_SKILL_LEVEL:
			if (statistic.agentenProSkilllevel.length>1) for (int i=0;i<statistic.agentenProSkilllevel.length;i++)
				buildAgentData(statistic.agentenProSkilllevel[i],statistic.simulationData.runRepeatCount);
			break;
		}

		/* Infotext  */
		/*
		Bringt keinen wirklich Mehrwert.
		addDescription("WorkLoad");
		 */
	}

	/**
	 * Erstellt den Text für eine Agentengruppe.
	 * @param agenten	Agentengruppe
	 * @param days	Anzahl an simulierten Tagen
	 */
	private void buildAgentData(AgentenDaten agenten, long days) {
		if (agenten.name.isEmpty() && agenten.type.isEmpty()) addHeading(2,Language.tr("SimStatistic.AllActiveAgents")); else {
			if (agenten.name.isEmpty())	addHeading(2,agenten.type); else addHeading(2,agenten.name);
		}

		beginParagraph();
		addLine(1,Language.tr("SimStatistic.Number")+": "+NumberTools.formatLong(agenten.anzahlAgenten));
		endParagraph();

		long sum=agenten.leerlaufGesamt+agenten.technischerLeerlaufGesamt+agenten.arbeitGesamt+agenten.postProcessingGesamt;
		beginParagraph();
		addLineDiv(1,Language.tr("SimStatistic.NumberOfConversations"),agenten.anzahlAnrufeGesamt,days);
		addPercentTime(1,Language.tr("SimStatistic.IdleTime"),agenten.leerlaufGesamt,days,sum);
		addPercentTime(1,Language.tr("SimStatistic.TechnicalFreeTime"),agenten.technischerLeerlaufGesamt,days,sum);
		addPercentTime(1,Language.tr("SimStatistic.HoldingTime"),agenten.arbeitGesamt,days,sum);
		addPercentTime(1,Language.tr("SimStatistic.PostProcessingTime"),agenten.postProcessingGesamt,days,sum);
		beginParagraph();

		endParagraph();
		double brutto=agenten.getAgentsTimes(statistic.editModel,false);
		double netto=agenten.getAgentsTimes(statistic.editModel,true);
		if (Math.abs(brutto-netto)<0.1) {
			addLine(1,Language.tr("SimStatistic.AgentsWorkingHours.Scheduled")+": "+NumberTools.formatNumberLong(brutto/3600)+" "+Language.tr("Statistic.Units.Hours.lower"));
		} else {
			addLine(1,Language.tr("SimStatistic.AgentsWorkingHours.ScheduledBrutto")+": "+NumberTools.formatNumberLong(brutto/3600)+" "+Language.tr("Statistic.Units.Hours.lower"));
			addLine(1,Language.tr("SimStatistic.AgentsWorkingHours.ScheduledNetto")+": "+NumberTools.formatNumberLong(netto/3600)+" "+Language.tr("Statistic.Units.Hours.lower"));
		}
		addLine(1,Language.tr("SimStatistic.AgentsWorkingHours.Worked")+": "+NumberTools.formatNumberLong((double)sum/days/3600)+" "+Language.tr("Statistic.Units.Hours.lower"));
		endParagraph();

		for (int i=0;i<agenten.dataByCaller.length;i++) {
			if (agenten.dataByCallerTechnial[i]+agenten.dataByCallerService[i]+agenten.dataByCallerPostProcessing[i]==0) continue;
			addHeading(3,String.format(Language.tr("SimStatistic.AgentConversations"),agenten.dataByCaller[i]));
			beginParagraph();
			addLineDiv(2,Language.tr("SimStatistic.NumberOfConversations"),agenten.dataByCallerAnzahlAnrufe[i],days);
			addPercentTime(2,Language.tr("Statistic.Total"),agenten.dataByCallerTechnial[i]+agenten.dataByCallerService[i]+agenten.dataByCallerPostProcessing[i],days,sum);
			addPercentTime(2,Language.tr("SimStatistic.TechnicalFreeTime"),agenten.dataByCallerTechnial[i],days,sum);
			addPercentTime(2,Language.tr("SimStatistic.HoldingTime"),agenten.dataByCallerService[i],days,sum);
			addPercentTime(2,Language.tr("SimStatistic.PostProcessingTime"),agenten.dataByCallerPostProcessing[i],days,sum);
			endParagraph();
		}
	}
}
