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
import simulator.Statistics.KundenDaten;
import systemtools.statistics.StatisticViewerText;

/**
 * Zeigt Informationen zu den Kosten an.
 * @author Alexander Herzog
 * @version 1.0
 */
public class StatisticViewerKostenText extends StatisticViewerText {
	/** Objekt vom Typ {@link Statistics}, dem die Kostendaten entnommen werden sollen */
	private final Statistics statistic;

	/**
	 * Konstruktor der Klasse <code>StatisticViewerKostenText</code>
	 * @param statistic	Objekt vom Typ {@link Statistics}, dem die Kostendaten entnommen werden sollen
	 */
	public StatisticViewerKostenText(Statistics statistic) {
		super();
		this.statistic=statistic;
	}

	/*
	private void addDescription(final String topic) {
		final URL url=StatisticViewerKostenText.class.getResource("description_"+Language.getCurrentLanguage()+"/"+topic+".html");
		addDescription(url,helpTopic->Help.topic(getViewer(false),helpTopic));
	}
	 */

	/**
	 * Generiert den Kostentext für eine Kundengruppe
	 * @param kunden	Kundengruppe
	 * @param days	Anzahl an simulierten Tagen
	 */
	private void buildClientCosts(final KundenDaten kunden, final long days) {
		if (kunden.name.isEmpty()) addHeading(2,Language.tr("SimStatistic.AllClients.Short")); else addHeading(2,kunden.name);
		beginParagraph();
		addLineDiv2(1,Language.tr("SimStatistic.Yield.Clients"),kunden.revenue,days);
		addLineDiv2(1,Language.tr("SimStatistic.Costs.Cancel"),kunden.costCancel,days);
		addLineDiv2(1,Language.tr("SimStatistic.Costs.WaitingTime"),kunden.costWaiting,days);
		addLineDiv2(1,Language.tr("Statistic.Total"),kunden.revenue-kunden.costCancel-kunden.costWaiting,days);
		endParagraph();
	}

	/**
	 * Generiert den Kostentext für eine Agentengruppe
	 * @param agenten	Agentengruppe
	 * @param days	Anzahl an simulierten Tagen
	 */
	private void buildAgentCosts(final AgentenDaten agenten, final long days) {
		if (agenten.name.isEmpty() && agenten.type.isEmpty()) addHeading(2,Language.tr("SimStatistic.AllAgents")); else {
			if (agenten.name.isEmpty())	addHeading(2,agenten.type); else addHeading(2,agenten.name);
		}

		beginParagraph();
		addLine(1,Language.tr("SimStatistic.Count")+": "+NumberTools.formatLong(agenten.anzahlAgenten));
		addLineDiv2(1,Language.tr("SimStatistic.Costs.Wage"),agenten.costOfficeTime,days);
		addLineDiv2(1,Language.tr("SimStatistic.Costs.HoldingTimes"),agenten.costCalls,days);
		addLineDiv2(1,Language.tr("SimStatistic.Costs.HoldingAndPostProcessingTimes"),agenten.costProcessTime,days);
		addLineDiv2(1,Language.tr("SimStatistic.Costs.Total.Short"),agenten.costOfficeTime+agenten.costCalls+agenten.costProcessTime,days);
		endParagraph();
	}

	@Override
	protected void buildText() {
		addHeading(1,Language.tr("SimStatistic.Costs.ByClients"));
		if (statistic.kundenProTyp.length>1) for (int i=0;i<statistic.kundenProTyp.length;i++) buildClientCosts(statistic.kundenProTyp[i],statistic.simulationData.runRepeatCount);
		buildClientCosts(statistic.kundenGlobal,statistic.simulationData.runRepeatCount);

		addHeading(1,Language.tr("SimStatistic.Costs.ByAgents"));
		if (statistic.agentenProCallcenter.length>1) for (int i=0;i<statistic.agentenProCallcenter.length;i++) buildAgentCosts(statistic.agentenProCallcenter[i],statistic.simulationData.runRepeatCount);
		if (statistic.agentenProSkilllevel.length>1) for (int i=0;i<statistic.agentenProSkilllevel.length;i++) buildAgentCosts(statistic.agentenProSkilllevel[i],statistic.simulationData.runRepeatCount);
		buildAgentCosts(statistic.agentenGlobal,statistic.simulationData.runRepeatCount);

		addHeading(1,Language.tr("SimStatistic.Costs.Total"));
		beginParagraph();
		addLineDiv2(1,Language.tr("SimStatistic.Yield"),statistic.kundenGlobal.revenue,statistic.simulationData.runRepeatCount);
		addLineDiv2(1,Language.tr("SimStatistic.Costs"),statistic.kundenGlobal.costCancel+statistic.kundenGlobal.costWaiting+statistic.agentenGlobal.costOfficeTime+statistic.agentenGlobal.costCalls+statistic.agentenGlobal.costProcessTime,statistic.simulationData.runRepeatCount);
		addLineDiv2(1,Language.tr("Statistic.Total"),statistic.kundenGlobal.revenue-(statistic.kundenGlobal.costCancel+statistic.kundenGlobal.costWaiting+statistic.agentenGlobal.costOfficeTime+statistic.agentenGlobal.costCalls+statistic.agentenGlobal.costProcessTime),statistic.simulationData.runRepeatCount);
		endParagraph();

		/* Infotext  */
		/*
		Bringt keinen wirklich Mehrwert.
		addDescription("Costs");
		 */
	}
}
