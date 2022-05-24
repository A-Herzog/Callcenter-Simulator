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

import java.awt.Color;

import language.Language;
import mathtools.NumberTools;
import mathtools.TimeTools;
import simulator.Statistics;
import simulator.Statistics.AgentenDaten;
import systemtools.statistics.StatisticViewerPieChart;

/**
 * Zeigt die Auslastung der Agenten als Tortendiagramm an.
 * @author Alexander Herzog
 * @version 1.0
 */
public class StatisticViewerAuslastungPieChart extends StatisticViewerPieChart {
	/** Objekt vom Typ {@link Statistics}, dem die Auslastungszahlen entnommen werden sollen */
	private final Statistics statistic;
	/** Darstellungsart, siehe <code>DATA_TYPE_*</code> Konstanten. */
	private final Mode dataType;
	/** Nummer des Callcenters oder des Skill-Levels bei Verwendung von <code>DATA_TYPE_CALLCENTER</code> oder <code>DATA_TYPE_SKILL_LEVEL</code> */
	private final int dataNr;

	/**
	 * Welche Informationen sollen angezeigt werden?
	 * @author Alexander Herzog
	 * @see StatisticViewerAuslastungPieChart#StatisticViewerAuslastungPieChart(Statistics, Mode, int)
	 */
	public enum Mode {

		/**
		 * Zeigt die Auslastung über alle Agenten an.
		 */
		DATA_TYPE_ALL,

		/**
		 * Zeigt die Auslastung über alle Agenten (nach Kundentypen) an.
		 */
		DATA_TYPE_ALL_FULL,

		/**
		 * Zeigt die Auslastung in einem bestimmten Callcenter an.
		 */
		DATA_TYPE_CALLCENTER,

		/**
		 * Zeigt die Auslastung in einem bestimmten Callcenter (nach Kundentypen) an.
		 */
		DATA_TYPE_CALLCENTER_FULL,

		/**
		 * Zeigt die Auslastung der Agenten eines bestimmten Skill-Levels an.
		 */
		DATA_TYPE_SKILL_LEVEL,

		/**
		 * Zeigt die Auslastung der Agenten eines bestimmten Skill-Levels (nach Kundentypen) an.
		 */
		DATA_TYPE_SKILL_LEVEL_FULL
	}

	/**
	 * Konstruktor der Klasse
	 * @param statistic	Objekt vom Typ {@link Statistics}, dem die Auslastungszahlen entnommen werden sollen
	 * @param dataType	Darstellungsart, siehe <code>DATA_TYPE_*</code> Konstanten.
	 * @param dataNr	Nummer des Callcenters oder des Skill-Levels bei Verwendung von <code>DATA_TYPE_CALLCENTER</code> oder <code>DATA_TYPE_SKILL_LEVEL</code>
	 */
	public StatisticViewerAuslastungPieChart(Statistics statistic, Mode dataType, int dataNr) {
		super();
		this.statistic=statistic;
		this.dataType=dataType;
		this.dataNr=dataNr;
	}

	@Override
	protected void firstChartRequest() {
		initPieChart("");

		switch (dataType) {
		case DATA_TYPE_ALL:
			chart.setTitle(Language.tr("SimStatistic.AgentsWorkLoad"));
			buildPie(statistic.agentenGlobal,statistic.simulationData.runRepeatCount);
			break;
		case DATA_TYPE_ALL_FULL:
			chart.setTitle(Language.tr("SimStatistic.AgentsWorkLoad"));
			buildFullPie(statistic.agentenGlobal,statistic.simulationData.runRepeatCount);
			break;
		case DATA_TYPE_CALLCENTER:
			chart.setTitle(String.format(Language.tr("SimStatistic.AgentsWorkLoad.Callcenter"),statistic.agentenProCallcenter[dataNr].name));
			buildPie(statistic.agentenProCallcenter[dataNr],statistic.simulationData.runRepeatCount);
			break;
		case DATA_TYPE_CALLCENTER_FULL:
			chart.setTitle(String.format(Language.tr("SimStatistic.AgentsWorkLoad.Callcenter"),statistic.agentenProCallcenter[dataNr].name));
			buildFullPie(statistic.agentenProCallcenter[dataNr],statistic.simulationData.runRepeatCount);
			break;
		case DATA_TYPE_SKILL_LEVEL:
			chart.setTitle(String.format(Language.tr("SimStatistic.AgentsWorkLoad.SkillLevel"),statistic.agentenProSkilllevel[dataNr].name));
			buildPie(statistic.agentenProSkilllevel[dataNr],statistic.simulationData.runRepeatCount);
			break;
		case DATA_TYPE_SKILL_LEVEL_FULL:
			chart.setTitle(String.format(Language.tr("SimStatistic.AgentsWorkLoad.SkillLevel"),statistic.agentenProSkilllevel[dataNr].name));
			buildFullPie(statistic.agentenProSkilllevel[dataNr],statistic.simulationData.runRepeatCount);
			break;
		}
	}

	/**
	 * Erzeugt eine Textzeile zur Beschriftung eines Tortensegments.
	 * @param label	Name für das Segment
	 * @param value	Wert (wird durch <code>count</code> dividiert)
	 * @param count	Gesamtanzahl
	 * @param sum	Summe für Prozentangabe
	 * @return	Textzeile zur Beschriftung
	 */
	private String buildText(String label, long value, long count, long sum) {
		return String.format("%s (%s, %s)",label,TimeTools.formatTime((int)Math.round((double)value/count)),NumberTools.formatPercent((double)value/sum));
	}

	/**
	 * Fügt ein Segment zu dem Tortendiagramm hinzu.
	 * @param label	Name für das Segment
	 * @param value	Wert (wird durch <code>count</code> dividiert)
	 * @param count	Gesamtanzahl
	 * @param sum	Summe für Prozentangabe
	 * @param color	Farbe für das Segment
	 */
	private void segment(String label, long value, long count, long sum, Color color) {
		addPieSegment(buildText(label,value,count,sum),value,color);
	}

	/**
	 * Generiert ein Tortendiagramm über die Auslastung einer Agentengruppe.
	 * @param agenten	Agentengruppe
	 * @param days	Anzahl an simulierten Tagen
	 */
	private void buildPie(AgentenDaten agenten, long days) {
		long sum=agenten.leerlaufGesamt+agenten.technischerLeerlaufGesamt+agenten.arbeitGesamt+agenten.postProcessingGesamt;
		long count=days;

		segment(Language.tr("SimStatistic.IdleTime"),agenten.leerlaufGesamt,count,sum,Color.WHITE);
		segment(Language.tr("SimStatistic.TechnicalFreeTime"),agenten.technischerLeerlaufGesamt,count,sum,Color.BLUE);
		segment(Language.tr("SimStatistic.HoldingTime"),agenten.arbeitGesamt,count,sum,Color.RED);
		segment(Language.tr("SimStatistic.PostProcessingTime"),agenten.postProcessingGesamt,count,sum,Color.GREEN);
	}

	/**
	 * Generiert ein Tortendiagramm über die Auslastung einer Agentengruppe
	 * und differenziert dieses weiter nach Kundentypen aus.
	 * @param agenten	Agentengruppe
	 * @param days	Anzahl an simulierten Tagen
	 */
	private void buildFullPie(AgentenDaten agenten, long days) {
		long sum=agenten.leerlaufGesamt+agenten.technischerLeerlaufGesamt+agenten.arbeitGesamt+agenten.postProcessingGesamt;
		long count=days;

		final Color[] colors=new Color[]{Color.BLUE,Color.RED,Color.GREEN,Color.CYAN,Color.MAGENTA,Color.ORANGE};

		int nr=0;
		addPieSegment(buildText(Language.tr("SimStatistic.IdleTime"),agenten.leerlaufGesamt,count,sum),agenten.leerlaufGesamt,Color.WHITE);
		for (int i=0;i<agenten.dataByCaller.length;i++) {
			if (agenten.dataByCallerTechnial[i]+agenten.dataByCallerService[i]+agenten.dataByCallerPostProcessing[i]==0) continue;
			String name=agenten.dataByCaller[i];
			Color c=colors[nr%colors.length]; nr++;
			segment(Language.tr("SimStatistic.TechnicalFreeTime")+" ("+name+")",agenten.dataByCallerTechnial[i],count,sum,c);
			segment(Language.tr("SimStatistic.HoldingTime")+" ("+name+")",agenten.dataByCallerService[i],count,sum,c.darker().darker());
			segment(Language.tr("SimStatistic.PostProcessingTime")+" ("+name+")",agenten.dataByCallerPostProcessing[i],count,sum,c.darker());
		}
	}
}
