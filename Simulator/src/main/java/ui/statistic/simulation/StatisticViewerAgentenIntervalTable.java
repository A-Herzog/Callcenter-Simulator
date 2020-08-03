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
import mathtools.TimeTools;
import mathtools.distribution.DataDistributionImpl;
import simulator.Statistics;
import simulator.Statistics.AgentModelData;
import simulator.Statistics.AgentenDaten;

/**
 * Klasse zur Anzeige einer Tabelle mit Agenten-spezifischen Statistikdaten auf Intervallbasis
 * @author Alexander Herzog
 * @version 1.0
 */
public class StatisticViewerAgentenIntervalTable extends StatisticViewerIntervalTable {
	private final Mode dataType;
	private final Statistics statistic;

	/**
	 * Welche Informationen sollen angezeigt werden?
	 * @author Alexander Herzog
	 * @see StatisticViewerAgentenIntervalTable#StatisticViewerAgentenIntervalTable(Statistics, Mode)
	 */
	public enum Mode {
		/**
		 * Anzeige der Auslastung der Agenten
		 */
		DATA_TYPE_AUSLASTUNG,

		/**
		 * Anzeige der Auslastung der Agenten (detailliert)
		 */
		DATA_TYPE_AUSLASTUNG_DETAILS,

		/**
		 * Anzeige der Bereitzeiten der Agenten
		 */
		DATA_TYPE_BEREITZEIT,

		/**
		 * Anzeige der Agentenzahlen gem‰ﬂ Modell (Agenten f¸r die Simulation)
		 */
		DATA_MODEL_SIM_AGENTS,

		/**
		 * Anzeige der Agentenzahlen gem‰ﬂ Modell (Agenten aus dem Modell)
		 */
		DATA_MODEL_MODEL_AGENTS,

		/**
		 * Anzeige der Agentenzahlen gem‰ﬂ Modell (Agenten inkl. Zuschlag)
		 */
		DATA_MODEL_FULL_AGENTS,

		/**
		 * Anzeige der Anzahl an Anrufen pro Callcenter
		 */
		DATA_TYPE_CALLS,

		/**
		 * Anzeige der Auslastung der Agenten per Skill-Level (detailliert)
		 */
		DATA_TYPE_SKILL_LEVEL_DETAILS,

		/**
		 * Anzeige der Warteschlangenl‰nge pro Intervall
		 */
		DATA_TYPE_QUEUE
	}

	/**
	 * Konstruktor der Klasse <code>StatisticViewerAgentenIntervalTable</code>
	 * @param statistic	Statistikobjekt dem die Agentendaten entnommen werden sollen
	 * @param dataType	Darstellungsart
	 */
	public StatisticViewerAgentenIntervalTable(Statistics statistic, Mode dataType) {
		super();
		this.dataType=dataType;
		this.statistic=statistic;
	}

	@Override
	protected void buildTable() {
		String sumRow;
		switch (dataType) {
		case DATA_TYPE_AUSLASTUNG: sumRow=Language.tr("Statistic.Average"); break;
		case DATA_TYPE_AUSLASTUNG_DETAILS: sumRow=Language.tr("Statistic.Sum"); break;
		case DATA_TYPE_BEREITZEIT: sumRow=Language.tr("Statistic.Sum"); break;
		case DATA_TYPE_CALLS: sumRow=Language.tr("Statistic.Sum"); break;
		case DATA_MODEL_SIM_AGENTS: sumRow=Language.tr("Statistic.Sum"); break;
		case DATA_MODEL_MODEL_AGENTS: sumRow=Language.tr("Statistic.Sum"); break;
		case DATA_MODEL_FULL_AGENTS: sumRow=Language.tr("Statistic.Sum"); break;
		case DATA_TYPE_SKILL_LEVEL_DETAILS: sumRow=Language.tr("Statistic.Sum"); break;
		case DATA_TYPE_QUEUE: sumRow=Language.tr("Statistic.Average"); break;
		default: sumRow=null;
		}

		buildIntervalTable(statistic,getAgentenCols(dataType,statistic),sumRow);
	}

	private static String[] getAgentenCols(Mode dataType, Statistics statistic) {
		String[] cols=null;

		switch (dataType) {
		case DATA_TYPE_AUSLASTUNG:
			cols=new String[statistic.agentenProCallcenter.length+1];
			for (int i=0;i<cols.length-1;i++) cols[i]=statistic.agentenProCallcenter[i].name;
			cols[cols.length-1]=Language.tr("Statistic.Average");
			break;
		case DATA_TYPE_BEREITZEIT:
		case DATA_TYPE_CALLS:
			cols=new String[statistic.agentenProCallcenter.length+1];
			for (int i=0;i<cols.length-1;i++) cols[i]=statistic.agentenProCallcenter[i].name;
			cols[cols.length-1]=Language.tr("Statistic.Total");
			break;
		case DATA_TYPE_AUSLASTUNG_DETAILS:
			cols=new String[(statistic.agentenProCallcenter.length+1)*4];
			for (int i=0;i<statistic.agentenProCallcenter.length;i++) {
				cols[i*4+0]=Language.tr("SimStatistic.IdleTime")+" ("+statistic.agentenProCallcenter[i].name+")";
				cols[i*4+1]=Language.tr("SimStatistic.TechnicalFreeTime")+" ("+statistic.agentenProCallcenter[i].name+")";
				cols[i*4+2]=Language.tr("SimStatistic.HoldingTime")+" ("+statistic.agentenProCallcenter[i].name+")";
				cols[i*4+3]=Language.tr("SimStatistic.PostProcessingTime")+" ("+statistic.agentenProCallcenter[i].name+")";
			}
			cols[cols.length-4]=Language.tr("SimStatistic.IdleTime")+" ("+Language.tr("Statistic.Total")+")";
			cols[cols.length-3]=Language.tr("SimStatistic.TechnicalFreeTime")+" ("+Language.tr("Statistic.Total")+")";
			cols[cols.length-2]=Language.tr("SimStatistic.HoldingTime")+" ("+Language.tr("Statistic.Total")+")";
			cols[cols.length-1]=Language.tr("SimStatistic.PostProcessingTime")+" ("+Language.tr("Statistic.Total")+")";
			break;
		case DATA_TYPE_SKILL_LEVEL_DETAILS:
			cols=new String[(statistic.agentenProSkilllevel.length+1)*4];
			for (int i=0;i<statistic.agentenProSkilllevel.length;i++) {
				cols[i*4+0]=Language.tr("SimStatistic.IdleTime")+" ("+statistic.agentenProSkilllevel[i].name+")";
				cols[i*4+1]=Language.tr("SimStatistic.TechnicalFreeTime")+" ("+statistic.agentenProSkilllevel[i].name+")";
				cols[i*4+2]=Language.tr("SimStatistic.HoldingTime")+" ("+statistic.agentenProSkilllevel[i].name+")";
				cols[i*4+3]=Language.tr("SimStatistic.PostProcessingTime")+" ("+statistic.agentenProSkilllevel[i].name+")";
			}
			cols[cols.length-4]=Language.tr("SimStatistic.IdleTime")+" ("+Language.tr("Statistic.Total")+")";
			cols[cols.length-3]=Language.tr("SimStatistic.TechnicalFreeTime")+" ("+Language.tr("Statistic.Total")+")";
			cols[cols.length-2]=Language.tr("SimStatistic.HoldingTime")+" ("+Language.tr("Statistic.Total")+")";
			cols[cols.length-1]=Language.tr("SimStatistic.PostProcessingTime")+" ("+Language.tr("Statistic.Total")+")";
			break;
		case DATA_MODEL_SIM_AGENTS:
		case DATA_MODEL_MODEL_AGENTS:
		case DATA_MODEL_FULL_AGENTS:
			cols=new String[statistic.agentenModellProGruppe.length+1];
			for (int i=0;i<cols.length-1;i++) cols[i]=statistic.agentenModellProGruppe[i].name;
			cols[cols.length-1]=Language.tr("Statistic.Total");
			break;
		case DATA_TYPE_QUEUE:
			cols=new String[1];
			cols[0]=Language.tr("Statistic.QueueLength");
			break;
		}

		return cols;
	}

	private String[] getAgentenCol(AgentenDaten agenten, long days) {
		String[] col=new String[49];

		double[] d;

		switch (dataType) {
		case DATA_TYPE_AUSLASTUNG:
			double[] dist=agenten.leerlaufProIntervall.densityData;
			double[] dist1=agenten.technischerLeerlaufProIntervall.densityData;
			double[] dist2=agenten.arbeitProIntervall.densityData;
			double[] dist3=agenten.postProcessingProIntervall.densityData;
			for (int i=0;i<48;i++) {
				double div=(dist[i]+dist1[i]+dist2[i]+dist3[i]);
				double value=(div>0)?(1-dist[i]/div):0;
				col[i]=NumberTools.formatPercent(value);
			}
			double value1=1-(double)agenten.leerlaufGesamt/(agenten.leerlaufGesamt+agenten.technischerLeerlaufGesamt+agenten.arbeitGesamt+agenten.postProcessingGesamt);
			col[48]=NumberTools.formatPercent(value1);
			break;
		case DATA_TYPE_BEREITZEIT:
			long sum=0;
			d=agenten.leerlaufProIntervall.densityData;
			for (int i=0;i<48;i++) {sum+=(d[i]/days); col[i]=TimeTools.formatTime((int)Math.round(d[i]/days));}
			col[48]=TimeTools.formatTime(sum);
			break;
		case DATA_TYPE_CALLS:
			d=agenten.anzahlAnrufeProIntervall.densityData;
			for (int i=0;i<48;i++) col[i]=NumberTools.formatNumberLong(d[i]/days);
			col[48]=NumberTools.formatNumberLong(agenten.anzahlAnrufeGesamt/days);
			break;
		default:
			for (int i=0;i<col.length;i++) col[i]="";
		}

		return col;
	}

	private String[] getAgentenDetailsCol(AgentenDaten agenten, long days, int colNr) {
		String[] col=new String[49];

		switch (dataType) {
		case DATA_TYPE_AUSLASTUNG_DETAILS:
		case DATA_TYPE_SKILL_LEVEL_DETAILS:
			DataDistributionImpl dist=null;
			switch (colNr) {
			case 0: dist=agenten.leerlaufProIntervall; break;
			case 1: dist=agenten.technischerLeerlaufProIntervall; break;
			case 2: dist=agenten.arbeitProIntervall; break;
			case 3: dist=agenten.postProcessingProIntervall; break;
			default: dist=new DataDistributionImpl(48,48); break;
			}
			for (int i=0;i<48;i++) col[i]=TimeTools.formatTime(Math.round(dist.densityData[i]/days));
			col[48]=TimeTools.formatTime(Math.round(dist.sum()/days));
			break;
		default:
			/* Andere Typen treten hier nicht auf.  */
			break;
		}

		return col;
	}

	private String[] getModellAgentenCol(AgentModelData agents) {
		String[] col=new String[49];
		col[48]="";
		double sum=0,v;

		switch (dataType) {
		case DATA_MODEL_SIM_AGENTS:
			for (int i=0;i<48;i++) {v=agents.simAgents.densityData[i]; col[i]=NumberTools.formatNumber(v); sum+=v;}
			col[48]=NumberTools.formatNumber(sum);
			break;
		case DATA_MODEL_MODEL_AGENTS:
			for (int i=0;i<48;i++) {v=agents.modelAgents.densityData[i]; col[i]=NumberTools.formatNumber(v); sum+=v;}
			break;
		case DATA_MODEL_FULL_AGENTS:
			for (int i=0;i<48;i++) {v=agents.fullAgents.densityData[i]; col[i]=NumberTools.formatNumber(v); sum+=v;}
			break;
		default:
			for (int i=0;i<col.length;i++) col[i]="";
		}

		return col;
	}

	private String[] getQueueCol(Statistics statistic) {
		String[] col=new String[49];
		for (int i=0;i<48;i++) col[i]=NumberTools.formatNumber(statistic.meanQueueLengthProIntervall.densityData[i]);
		col[48]=NumberTools.formatNumber(statistic.meanQueueLength);

		return col;
	}

	/* (non-Javadoc)
	 * @see complexcallcenter.statistic.simulation.StatisticViewerIntervalTable#getUserCol(complexcallcenter.simulator.ComplexStatisticSimData, int)
	 */
	@Override
	protected String[] getUserCol(Statistics statistic, int colNr) {
		switch (dataType) {
		case DATA_TYPE_AUSLASTUNG:
		case DATA_TYPE_BEREITZEIT:
		case DATA_TYPE_CALLS:
			return (colNr<statistic.agentenProCallcenter.length)?getAgentenCol(statistic.agentenProCallcenter[colNr],statistic.simulationData.runRepeatCount):getAgentenCol(statistic.agentenGlobal,statistic.simulationData.runRepeatCount);
		case DATA_TYPE_AUSLASTUNG_DETAILS:
			return (colNr/4<statistic.agentenProCallcenter.length)?getAgentenDetailsCol(statistic.agentenProCallcenter[colNr/4],statistic.simulationData.runRepeatCount,colNr%4):getAgentenDetailsCol(statistic.agentenGlobal,statistic.simulationData.runRepeatCount,colNr%4);
		case DATA_TYPE_SKILL_LEVEL_DETAILS:
			return (colNr/4<statistic.agentenProSkilllevel.length)?getAgentenDetailsCol(statistic.agentenProSkilllevel[colNr/4],statistic.simulationData.runRepeatCount,colNr%4):getAgentenDetailsCol(statistic.agentenGlobal,statistic.simulationData.runRepeatCount,colNr%4);
		case DATA_MODEL_SIM_AGENTS:
		case DATA_MODEL_MODEL_AGENTS:
		case DATA_MODEL_FULL_AGENTS:
			return (colNr<statistic.agentenModellProGruppe.length)?getModellAgentenCol(statistic.agentenModellProGruppe[colNr]):getModellAgentenCol(statistic.agentenModellGlobal);
		case DATA_TYPE_QUEUE:
			return getQueueCol(statistic);
		}
		return null;
	}
}
