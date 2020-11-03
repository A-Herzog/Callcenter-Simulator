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

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import language.Language;
import mathtools.Table;
import simulator.Statistics;
import simulator.Statistics.AgentenDaten;
import systemtools.statistics.StatisticViewerTable;

/**
 * Zeigt Agenten-spezifischen Statistikdaten in einer Tabelle an.
 * @author Alexander Herzog
 * @version 1.0
 */
public class StatisticViewerAgentenTable extends StatisticViewerTable {
	/** Wonach sollen die Agenten gruppiert werden? */
	private final SortType sortType;
	/** Statistikobjekt dem die Agentendaten entnommen werden sollen */
	private final Statistics statistic;

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
	public StatisticViewerAgentenTable(final Statistics statistic, final SortType sortType) {
		super();
		this.sortType=sortType;
		this.statistic=statistic;
	}

	@Override
	protected void buildTable() {
		setData(getTableData(statistic,getRowNames(statistic)),getColNames(statistic));
	}

	/**
	 * Liefert die Namen für die Spaltenüberschriften.
	 * @param statistic	Statistikobjekt
	 * @return	Namen für die Spaltenüberschriften
	 */
	private String[] getColNames(final Statistics statistic) {
		List<String> list=new ArrayList<String>();
		list.add("");
		switch (sortType) {
		case SORT_BY_CALLCENTER:
			for (int i=0;i<statistic.agentenProCallcenter.length;i++) list.add(statistic.agentenProCallcenter[i].name);
			list.add(Language.tr("SimStatistic.AllCallcenter"));
			break;
		case SORT_BY_SKILL_LEVEL:
			for (int i=0;i<statistic.agentenProSkilllevel.length;i++) list.add(statistic.agentenProSkilllevel[i].name);
			list.add(Language.tr("SimStatistic.AllAgents"));
			break;
		}

		return list.toArray(new String[0]);
	}

	/**
	 * Liefert die Namen für die Zeilen (erste Spalte).
	 * @param statistic	Statistikobjekt
	 * @return	Namen für die Zeilen
	 */
	private String[] getRowNames(final Statistics statistic) {
		List<String> list=new ArrayList<String>(Arrays.asList(new String[]{
				Language.tr("SimStatistic.Number"),
				Language.tr("SimStatistic.NumberOfConversations"),
				Language.tr("SimStatistic.IdleTime"),Language.tr("SimStatistic.IdleTime"),
				Language.tr("SimStatistic.TechnicalFreeTime"),Language.tr("SimStatistic.TechnicalFreeTime"),
				Language.tr("SimStatistic.HoldingTime"),Language.tr("SimStatistic.HoldingTime"),
				Language.tr("SimStatistic.PostProcessingTime"),Language.tr("SimStatistic.PostProcessingTime"),
		}));

		for (int i=0;i<statistic.agentenGlobal.dataByCaller.length;i++) {
			String s=statistic.agentenGlobal.dataByCaller[i];

			list.add(String.format(Language.tr("SimStatistic.NumberOfConversationsWith"),s));
			list.add(String.format(Language.tr("SimStatistic.CompleteZimeFor"),s));
			list.add(String.format(Language.tr("SimStatistic.CompleteZimeFor"),s));
			list.add(String.format(Language.tr("SimStatistic.TechnicalFreeTimeFor"),s));
			list.add(String.format(Language.tr("SimStatistic.TechnicalFreeTimeFor"),s));
			list.add(String.format(Language.tr("SimStatistic.HoldingTimeFor"),s));
			list.add(String.format(Language.tr("SimStatistic.HoldingTimeFor"),s));
			list.add(String.format(Language.tr("SimStatistic.PostProcessingTimeFor"),s));
			list.add(String.format(Language.tr("SimStatistic.PostProcessingTimeFor"),s));
		}

		return list.toArray(new String[0]);
	}

	/**
	 * Fügt eine Zeile mit Zeit- und Prozentangaben an die Liste an
	 * @param list	Ausgabeliste
	 * @param time	Zeit
	 * @param div	Divisionswert
	 * @param sum	Gesamtwert (für Anteilsberechnung)
	 */
	private void addTimeAndPercent(final List<String> list, final long time, final long div, final long sum) {
		list.add(addTimeCell(time,div));
		list.add(addPercentCellParts(time,sum));
	}

	/**
	 * Liefert eine Ausgabeliste für eine Agentengruppe
	 * @param agenten	Agentengruppe
	 * @param days	Anzahl an simulierten Tagen
	 * @return	Ausgabeliste
	 */
	private List<String> getLine(final AgentenDaten agenten, final long days) {
		List<String> line=new ArrayList<String>();

		long sum=agenten.leerlaufGesamt+agenten.technischerLeerlaufGesamt+agenten.arbeitGesamt+agenten.postProcessingGesamt;

		line.add(addCell(agenten.anzahlAgenten,1));
		line.add(addCell(agenten.anzahlAnrufeGesamt,days));
		addTimeAndPercent(line,agenten.leerlaufGesamt,days,sum);
		addTimeAndPercent(line,agenten.technischerLeerlaufGesamt,days,sum);
		addTimeAndPercent(line,agenten.arbeitGesamt,days,sum);
		addTimeAndPercent(line,agenten.postProcessingGesamt,days,sum);

		for (int i=0;i<agenten.dataByCaller.length;i++) {
			long subsum=agenten.dataByCallerTechnial[i]+agenten.dataByCallerService[i]+agenten.dataByCallerPostProcessing[i];
			line.add(addCell(agenten.dataByCallerAnzahlAnrufe[i],days));
			addTimeAndPercent(line,subsum,days,sum);
			addTimeAndPercent(line,agenten.dataByCallerTechnial[i],days,sum);
			addTimeAndPercent(line,agenten.dataByCallerService[i],days,sum);
			addTimeAndPercent(line,agenten.dataByCallerPostProcessing[i],days,sum);
		}

		return line;
	}

	/**
	 * Erstellt den Tabelleninhalt
	 * @param statistic	Statistikobjekt
	 * @param rowNames	Zeilennamen (erste Spalte)
	 * @return	Tabelleninhalt
	 */
	private Table getTableData(Statistics statistic, String[] rowNames) {
		Table table=new Table();

		table.addLine(rowNames);
		switch (sortType) {
		case SORT_BY_CALLCENTER:
			for (int i=0;i<statistic.agentenProCallcenter.length;i++) table.addLine(getLine(statistic.agentenProCallcenter[i],statistic.simulationData.runRepeatCount));
			break;
		case SORT_BY_SKILL_LEVEL:
			for (int i=0;i<statistic.agentenProSkilllevel.length;i++) table.addLine(getLine(statistic.agentenProSkilllevel[i],statistic.simulationData.runRepeatCount));
			break;
		}
		table.addLine(getLine(statistic.agentenGlobal,statistic.simulationData.runRepeatCount));

		return table.transpose();
	}
}
