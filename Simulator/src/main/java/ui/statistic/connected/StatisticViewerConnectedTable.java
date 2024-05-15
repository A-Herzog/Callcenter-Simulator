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
package ui.statistic.connected;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import language.Language;
import mathtools.NumberTools;
import mathtools.Table;
import mathtools.TimeTools;
import simulator.Statistics;
import simulator.Statistics.AgentModelData;
import simulator.Statistics.AgentenDaten;
import simulator.Statistics.KundenDaten;
import systemtools.statistics.StatisticViewerTable;
import ui.model.CallcenterModel;

/**
 * Zeigt verschiedene Statistikergebnisse der verbundenen Simulation als Tabelle an.
 * @author Alexander Herzog
 * @version 1.0
 */
public class StatisticViewerConnectedTable extends StatisticViewerTable {
	/** Array aus Objekten vom Typ {@link Statistics}, dem die Ergebnisse der einzelnen Simulationstage entnommen werden sollen */
	private final Statistics[] results;
	/** Darstellungsart, siehe <code>DATA_TYPE_*</code> Konstanten. */
	private Mode dataType;

	/**
	 * Welche Informationen sollen angezeigt werden?
	 * @author Alexander Herzog
	 * @see StatisticViewerConnectedTable#StatisticViewerConnectedTable(Statistics[], Mode)
	 */
	public enum Mode {
		/**
		 * Zeigt die Veränderung der Anruferzahlen an.
		 */
		DATA_TYPE_CALLERS,

		/**
		 * Zeigt die Veränderung der Erreichbarkeit an.
		 */
		DATA_TYPE_SUCCESS,

		/**
		 * Zeigt die Veränderung der Abbrecherzahlen an.
		 */
		DATA_TYPE_CANCEL,

		/**
		 * Zeigt die Veränderung der Wartezeiten an.
		 */
		DATA_TYPE_WAITING_TIME,

		/**
		 * Zeigt die Veränderung der Verweilzeiten an.
		 */
		DATA_TYPE_STAYING_TIME,

		/**
		 * Zeigt die Veränderung des Service-Level (bezogen auf erfolgreiche Anrufe) an.
		 */
		DATA_TYPE_SERVICE_LEVEL_CALLS_SUCCESS,

		/**
		 * Zeigt die Veränderung des Service-Level (bezogen auf erfolgreiche Kunden) an.
		 */
		DATA_TYPE_SERVICE_LEVEL_CLIENTS_SUCCESS,

		/**
		 * Zeigt die Veränderung des Service-Level (bezogen auf alle Anrufe) an.
		 */
		DATA_TYPE_SERVICE_LEVEL_CALLS_ALL,

		/**
		 * Zeigt die Veränderung des Service-Level (bezogen auf alle Kunden) an.
		 */
		DATA_TYPE_SERVICE_LEVEL_CLIENTS_ALL,

		/**
		 * Zeigt die Veränderung der Agentenzahlen pro Callcenter an.
		 */
		DATA_TYPE_AGENTS_CALLCENTER,

		/**
		 * Zeigt die Veränderung der Agentenzahlen pro Skill-Level an.
		 */
		DATA_TYPE_AGENTS_SKILL_LEVEL,

		/**
		 * Zeigt die Veränderung der Auslastung pro Callcenter an.
		 */
		DATA_TYPE_WORKLOAD_CALLCENTER,

		/**
		 * Zeigt die Veränderung der Auslastung pro Skill-Level an.
		 */
		DATA_TYPE_WORKLOAD_SKILL_LEVEL,

		/**
		 * Zeigt eine Übersichtstabelle an.
		 */
		DATA_TYPE_SUMMARY,

		/**
		 * Anzeige der Agentenzahlen gemäß Modell (Agenten für die Simulation)
		 */
		DATA_TYPE_SIM_AGENTS,

		/**
		 * Anzeige der Agentenzahlen gemäß Modell (Agenten aus dem Modell)
		 */
		DATA_TYPE_MODEL_AGENTS,

		/**
		 * Anzeige der Agentenzahlen gemäß Modell (Agenten inkl. Zuschlag)
		 */
		DATA_TYPE_FULL_AGENTS
	}

	/**
	 * Konstruktor der Klasse <code>StatisticViewerConnectedTable</code>
	 * @param results	Array aus Objekten vom Typ {@link Statistics}, dem die Ergebnisse der einzelnen Simulationstage entnommen werden sollen
	 * @param dataType	Darstellungsart, siehe <code>DATA_TYPE_*</code> Konstanten.
	 */
	public StatisticViewerConnectedTable(Statistics[] results, Mode dataType) {
		super();
		this.results=results;
		this.dataType=dataType;
	}

	@Override
	protected void buildTable() {
		setData(getTableData(),getColNames());
	}

	/**
	 * Liefert Spaltennamen aus {@link #getColNames()} aber ohne die Simulationstage-Spalte
	 * @param extraCaption	Zusätzlicher Vorspann für die Spaltennamen
	 * @return	Spaltennamen
	 */
	private	List<String> getColParts(String extraCaption) {
		final ArrayList<String> cols=new ArrayList<>(Arrays.asList(getColNames()));
		cols.remove(0);
		for (int i=0;i<cols.size();i++) cols.set(i,extraCaption+" "+cols.get(i));
		return cols;
	}

	/**
	 * Liefert eine Liste der Agentengruppen-Namen.
	 * @return	Liste der Agentengruppen-Namen
	 */
	private List<String> getModelAgentsGroups() {
		List<String> groupNames=new ArrayList<>();
		for (AgentModelData group: results[0].agentenModellProGruppe) groupNames.add(group.name);
		for (int i=1;i<results.length;i++) for (AgentModelData group: results[i].agentenModellProGruppe) if (groupNames.indexOf(group.name)<0) groupNames.add(group.name);
		return groupNames;
	}

	/**
	 * Liefert die Namen für die Spaltenüberschriften.
	 * @return	Namen für die Spaltenüberschriften
	 */
	private String[] getColNames() {
		int dataCols=0;
		List<String> colNames=null;

		switch (dataType) {
		case DATA_TYPE_CALLERS:
		case DATA_TYPE_SUCCESS:
		case DATA_TYPE_CANCEL:
			dataCols=2*(1+results[0].kundenProTyp.length);
			break;
		case DATA_TYPE_WAITING_TIME:
		case DATA_TYPE_STAYING_TIME:
		case DATA_TYPE_SERVICE_LEVEL_CALLS_SUCCESS:
		case DATA_TYPE_SERVICE_LEVEL_CLIENTS_SUCCESS:
		case DATA_TYPE_SERVICE_LEVEL_CALLS_ALL:
		case DATA_TYPE_SERVICE_LEVEL_CLIENTS_ALL:
			dataCols=1+results[0].kundenProTyp.length;
			break;
		case DATA_TYPE_AGENTS_CALLCENTER:
			dataCols=5*(1+results[0].agentenProCallcenter.length);
			break;
		case DATA_TYPE_AGENTS_SKILL_LEVEL:
			dataCols=5*(1+results[0].agentenProSkilllevel.length);
			break;
		case DATA_TYPE_WORKLOAD_CALLCENTER:
			dataCols=1+results[0].agentenProCallcenter.length;
			break;
		case DATA_TYPE_WORKLOAD_SKILL_LEVEL:
			dataCols=1+results[0].agentenProSkilllevel.length;
			break;
		case DATA_TYPE_SUMMARY:
			dataCols=0;
			break;
		case DATA_TYPE_SIM_AGENTS:
		case DATA_TYPE_MODEL_AGENTS:
		case DATA_TYPE_FULL_AGENTS:
			colNames=getModelAgentsGroups();
			dataCols=colNames.size()+1;
			break;
		}

		String[] cols=new String[1+dataCols];
		cols[0]=Language.tr("SimStatistic.SimulatedDay");

		switch (dataType) {
		case DATA_TYPE_CALLERS:
			for (int i=0;i<results[0].kundenProTyp.length;i++) {
				String s=results[0].kundenProTyp[i].name;
				cols[2*i+1]=s+" ("+Language.tr("SimStatistic.FreshCalls")+")";
				cols[2*i+2]=s+" ("+Language.tr("SimStatistic.Caller.Total")+")";
			}
			cols[cols.length-2]=Language.tr("SimStatistic.AllClients")+" ("+Language.tr("SimStatistic.FreshCalls")+")";
			cols[cols.length-1]=Language.tr("SimStatistic.AllClients")+" ("+Language.tr("SimStatistic.Caller.Total")+")";
			break;
		case DATA_TYPE_SUCCESS:
		case DATA_TYPE_CANCEL:
			for (int i=0;i<results[0].kundenProTyp.length;i++) {
				String s=results[0].kundenProTyp[i].name;
				cols[2*i+1]=s+" ("+Language.tr("SimStatistic.OnClientBasis")+")";
				cols[2*i+2]=s+" ("+Language.tr("SimStatistic.OnCallBasis")+")";
			}
			cols[cols.length-2]=Language.tr("SimStatistic.AllClients")+" ("+Language.tr("SimStatistic.OnClientBasis")+")";
			cols[cols.length-1]=Language.tr("SimStatistic.AllClients")+" ("+Language.tr("SimStatistic.OnCallBasis")+")";
			break;
		case DATA_TYPE_WAITING_TIME:
		case DATA_TYPE_STAYING_TIME:
		case DATA_TYPE_SERVICE_LEVEL_CALLS_SUCCESS:
		case DATA_TYPE_SERVICE_LEVEL_CLIENTS_SUCCESS:
		case DATA_TYPE_SERVICE_LEVEL_CALLS_ALL:
		case DATA_TYPE_SERVICE_LEVEL_CLIENTS_ALL:
			for (int i=0;i<results[0].kundenProTyp.length;i++) cols[i+1]=results[0].kundenProTyp[i].name;
			cols[cols.length-1]=Language.tr("SimStatistic.AllClients");
			break;
		case DATA_TYPE_AGENTS_CALLCENTER:
			for (int i=0;i<results[0].agentenProCallcenter.length;i++) {
				String s=results[0].agentenProCallcenter[i].name;
				cols[5*i+1]=s+" ("+Language.tr("SimStatistic.IdleTime")+")";
				cols[5*i+2]=s+" ("+Language.tr("SimStatistic.TechnicalFreeTime")+")";
				cols[5*i+3]=s+" ("+Language.tr("SimStatistic.HoldingTime")+")";
				cols[5*i+4]=s+" ("+Language.tr("SimStatistic.PostProcessingTime")+")";
				cols[5*i+5]=s+" ("+Language.tr("Statistic.Total")+")";
			}
			cols[cols.length-5]=Language.tr("SimStatistic.AllCallcenter")+" ("+Language.tr("SimStatistic.IdleTime")+")";
			cols[cols.length-4]=Language.tr("SimStatistic.AllCallcenter")+" ("+Language.tr("SimStatistic.TechnicalFreeTime")+")";
			cols[cols.length-3]=Language.tr("SimStatistic.AllCallcenter")+" ("+Language.tr("SimStatistic.HoldingTime")+")";
			cols[cols.length-2]=Language.tr("SimStatistic.AllCallcenter")+" ("+Language.tr("SimStatistic.PostProcessingTime")+")";
			cols[cols.length-1]=Language.tr("SimStatistic.AllCallcenter")+" ("+Language.tr("Statistic.Total")+")";
			break;
		case DATA_TYPE_AGENTS_SKILL_LEVEL:
			for (int i=0;i<results[0].agentenProSkilllevel.length;i++) {
				String s=results[0].agentenProSkilllevel[i].name;
				cols[5*i+1]=s+" ("+Language.tr("SimStatistic.IdleTime")+")";
				cols[5*i+2]=s+" ("+Language.tr("SimStatistic.TechnicalFreeTime")+")";
				cols[5*i+3]=s+" ("+Language.tr("SimStatistic.HoldingTime")+")";
				cols[5*i+4]=s+" ("+Language.tr("SimStatistic.PostProcessingTime")+")";
				cols[5*i+5]=s+" ("+Language.tr("Statistic.Total")+")";
			}
			cols[cols.length-5]=Language.tr("SimStatistic.AllCallcenter")+" ("+Language.tr("SimStatistic.IdleTime")+")";
			cols[cols.length-4]=Language.tr("SimStatistic.AllCallcenter")+" ("+Language.tr("SimStatistic.TechnicalFreeTime")+")";
			cols[cols.length-3]=Language.tr("SimStatistic.AllCallcenter")+" ("+Language.tr("SimStatistic.HoldingTime")+")";
			cols[cols.length-2]=Language.tr("SimStatistic.AllCallcenter")+" ("+Language.tr("SimStatistic.PostProcessingTime")+")";
			cols[cols.length-1]=Language.tr("SimStatistic.AllCallcenter")+" ("+Language.tr("Statistic.Total")+")";
			break;
		case DATA_TYPE_WORKLOAD_CALLCENTER:
			for (int i=0;i<results[0].agentenProCallcenter.length;i++) cols[i+1]=results[0].agentenProCallcenter[i].name;
			cols[cols.length-1]=Language.tr("SimStatistic.AllCallcenter");
			break;
		case DATA_TYPE_WORKLOAD_SKILL_LEVEL:
			for (int i=0;i<results[0].agentenProSkilllevel.length;i++) cols[i+1]=results[0].agentenProSkilllevel[i].name;
			cols[cols.length-1]=Language.tr("SimStatistic.AllSkillLevel");
			break;
		case DATA_TYPE_SUMMARY:
			List<String> tempCol=new ArrayList<>(Arrays.asList(cols));
			dataType=Mode.DATA_TYPE_CALLERS; tempCol.addAll(getColParts(Language.tr("SimStatistic.NumberOfCallers")));
			dataType=Mode.DATA_TYPE_SUCCESS; tempCol.addAll(getColParts(Language.tr("SimStatistic.Accessibility")));
			dataType=Mode.DATA_TYPE_CANCEL; tempCol.addAll(getColParts(Language.tr("SimStatistic.NumberOfCancelations")));
			dataType=Mode.DATA_TYPE_WAITING_TIME; tempCol.addAll(getColParts(Language.tr("SimStatistic.AverageWaitingTime")));
			dataType=Mode.DATA_TYPE_STAYING_TIME; tempCol.addAll(getColParts(Language.tr("SimStatistic.AverageResidenceTime")));
			dataType=Mode.DATA_TYPE_SERVICE_LEVEL_CALLS_SUCCESS; tempCol.addAll(getColParts(Language.tr("SimStatistic.ServiceLevel")+" ("+Language.tr("SimStatistic.CalculatedOn.SuccessfulCalls")+")"));
			dataType=Mode.DATA_TYPE_SERVICE_LEVEL_CLIENTS_SUCCESS; tempCol.addAll(getColParts(Language.tr("SimStatistic.ServiceLevel")+" ("+Language.tr("SimStatistic.CalculatedOn.SuccessfulClients")+")"));
			dataType=Mode.DATA_TYPE_SERVICE_LEVEL_CALLS_ALL; tempCol.addAll(getColParts(Language.tr("SimStatistic.ServiceLevel")+" ("+Language.tr("SimStatistic.CalculatedOn.AllCalls")+")"));
			dataType=Mode.DATA_TYPE_SERVICE_LEVEL_CLIENTS_ALL; tempCol.addAll(getColParts(Language.tr("SimStatistic.ServiceLevel")+" ("+Language.tr("SimStatistic.CalculatedOn.AllCalls")+")"));
			dataType=Mode.DATA_TYPE_AGENTS_CALLCENTER; tempCol.addAll(getColParts(Language.tr("SimStatistic.Agents.PerCallcenter")));
			dataType=Mode.DATA_TYPE_AGENTS_SKILL_LEVEL; tempCol.addAll(getColParts(Language.tr("SimStatistic.Agents.PerSkillLevel")));
			dataType=Mode.DATA_TYPE_WORKLOAD_CALLCENTER; tempCol.addAll(getColParts(Language.tr("SimStatistic.WorkLoad.PerCallcenter")));
			dataType=Mode.DATA_TYPE_WORKLOAD_SKILL_LEVEL; tempCol.addAll(getColParts(Language.tr("SimStatistic.WorkLoad.PerSkillLevel")));
			cols=tempCol.toArray(String[]::new);
			dataType=Mode.DATA_TYPE_SUMMARY;
			break;
		case DATA_TYPE_SIM_AGENTS:
		case DATA_TYPE_MODEL_AGENTS:
		case DATA_TYPE_FULL_AGENTS:
			if (colNames!=null) for (int i=1;i<cols.length-1;i++) cols[i]=colNames.get(i-1);
			cols[cols.length-1]=Language.tr("Statistic.Total");
			break;
		}

		return cols;
	}

	/**
	 * Fügt die Datenreihen für einen Kundentyp zu der Ausgabetabelle hinzu.
	 * @param table	Ausgabetabelle
	 * @param nr Nummer des Kundentyps oder -1 für die Daten über alle Kundentypen
	 */
	private void addKundenLines(final Table table, final int nr) {
		final String[] row1=new String[results.length];
		final String[] row2=new String[results.length];

		for (int i=0;i<results.length;i++) {
			Statistics statistic=results[i];
			KundenDaten kunden=(nr>=0)?statistic.kundenProTyp[nr]:statistic.kundenGlobal;

			switch (dataType) {
			case DATA_TYPE_CALLERS:
				row1[i]=NumberTools.formatNumber((double)(kunden.kunden+kunden.kundenWiederanruf)/statistic.simulationData.runRepeatCount);
				row2[i]=NumberTools.formatNumber((double)kunden.anrufe/statistic.simulationData.runRepeatCount);
				break;
			case DATA_TYPE_SUCCESS:
				row1[i]=NumberTools.formatPercent((double)kunden.kundenErfolg/(kunden.kunden+kunden.kundenWiederanruf));
				row2[i]=NumberTools.formatPercent((double)kunden.anrufeErfolg/kunden.anrufe);
				break;
			case DATA_TYPE_CANCEL:
				row1[i]=NumberTools.formatNumber((double)kunden.kundenAbbruch/statistic.simulationData.runRepeatCount);
				row2[i]=NumberTools.formatNumber((double)kunden.anrufeAbbruch/statistic.simulationData.runRepeatCount);
				break;
			case DATA_TYPE_WAITING_TIME:
				row1[i]=TimeTools.formatTime((int)(kunden.anrufeWartezeitSum/Math.max(1,kunden.anrufeErfolg)));
				break;
			case DATA_TYPE_STAYING_TIME:
				row1[i]=TimeTools.formatTime((int)(kunden.anrufeVerweilzeitSum/Math.max(1,kunden.anrufeErfolg)));
				break;
			case DATA_TYPE_SERVICE_LEVEL_CALLS_SUCCESS:
				row1[i]=NumberTools.formatPercent((double)kunden.anrufeServicelevel/Math.max(1,kunden.anrufeErfolg));
				break;
			case DATA_TYPE_SERVICE_LEVEL_CLIENTS_SUCCESS:
				row1[i]=NumberTools.formatPercent((double)kunden.kundenServicelevel/Math.max(1,kunden.kundenErfolg));
				break;
			case DATA_TYPE_SERVICE_LEVEL_CALLS_ALL:
				row1[i]=NumberTools.formatPercent((double)kunden.anrufeServicelevel/kunden.anrufe);
				break;
			case DATA_TYPE_SERVICE_LEVEL_CLIENTS_ALL:
				row1[i]=NumberTools.formatPercent((double)kunden.kundenServicelevel/kunden.kunden);
				break;
			default:
				/* Agenten- und Callcenter-Fälle */
				break;
			}
		}

		table.addLine(row1);
		if (dataType==Mode.DATA_TYPE_CALLERS || dataType==Mode.DATA_TYPE_SUCCESS || dataType==Mode.DATA_TYPE_CANCEL) table.addLine(row2);
	}

	/**
	 * Fügt die Datenreihen für eine Agentengruppe zu der Ausgabetabelle hinzu.
	 * @param table	Ausgabetabelle
	 * @param nr Nummer der Agentengruoppe oder -1 für die Daten über alle Gruppen
	 * @param perCallcenter	Die Nummer bezieht sich auf die Callcenter-Gruppen-Nummerierung (<code>true</code>) oder auf die Skill-Level-Nummerierung (<code>false</code>)
	 */

	private void addAgentenLines(final Table table, final int nr, final boolean perCallcenter) {
		String[] row1=new String[results.length];
		String[] row2=new String[results.length];
		String[] row3=new String[results.length];
		String[] row4=new String[results.length];
		String[] row5=new String[results.length];

		for (int i=0;i<results.length;i++) {
			Statistics statistic=results[i];
			AgentenDaten agenten;
			if (perCallcenter) {
				agenten=(nr>=0)?statistic.agentenProCallcenter[nr]:statistic.agentenGlobal;
			} else {
				agenten=(nr>=0)?statistic.agentenProSkilllevel[nr]:statistic.agentenGlobal;
			}

			switch (dataType) {
			case DATA_TYPE_AGENTS_CALLCENTER:
			case DATA_TYPE_AGENTS_SKILL_LEVEL:
				row1[i]=NumberTools.formatNumber((double)agenten.leerlaufGesamt/statistic.simulationData.runRepeatCount/3600);
				row2[i]=NumberTools.formatNumber((double)agenten.technischerLeerlaufGesamt/statistic.simulationData.runRepeatCount/3600);
				row3[i]=NumberTools.formatNumber((double)agenten.arbeitGesamt/statistic.simulationData.runRepeatCount/3600);
				row4[i]=NumberTools.formatNumber((double)agenten.postProcessingGesamt/statistic.simulationData.runRepeatCount/3600);
				row5[i]=NumberTools.formatNumber((double)(agenten.leerlaufGesamt+agenten.technischerLeerlaufGesamt+agenten.arbeitGesamt+agenten.postProcessingGesamt)/statistic.simulationData.runRepeatCount/3600);
				break;
			case DATA_TYPE_WORKLOAD_CALLCENTER:
			case DATA_TYPE_WORKLOAD_SKILL_LEVEL:
				row1[i]=NumberTools.formatPercent((1-(double)agenten.leerlaufGesamt/(agenten.leerlaufGesamt+agenten.technischerLeerlaufGesamt+agenten.arbeitGesamt+agenten.postProcessingGesamt)));
				break;
			default:
				/* Kunden-Fälle */
				break;
			}
		}

		table.addLine(row1);
		if (dataType==Mode.DATA_TYPE_AGENTS_CALLCENTER || dataType==Mode.DATA_TYPE_AGENTS_SKILL_LEVEL) {
			table.addLine(row2);
			table.addLine(row3);
			table.addLine(row4);
			table.addLine(row5);
		}
	}

	/**
	 * Fügt die Datenreihen zu der Ausgabetabelle hinzu.
	 * @param table	Ausgabetabelle
	 */
	private void addLines(final Table table) {
		List<String> groupNames=null;
		switch (dataType) {
		case DATA_TYPE_CALLERS:
		case DATA_TYPE_SUCCESS:
		case DATA_TYPE_CANCEL:
		case DATA_TYPE_WAITING_TIME:
		case DATA_TYPE_STAYING_TIME:
		case DATA_TYPE_SERVICE_LEVEL_CALLS_SUCCESS:
		case DATA_TYPE_SERVICE_LEVEL_CLIENTS_SUCCESS:
		case DATA_TYPE_SERVICE_LEVEL_CALLS_ALL:
		case DATA_TYPE_SERVICE_LEVEL_CLIENTS_ALL:
			for (int i=0;i<results[0].kundenProTyp.length;i++) addKundenLines(table,i);
			addKundenLines(table,-1);
			break;
		case DATA_TYPE_AGENTS_CALLCENTER:
		case DATA_TYPE_WORKLOAD_CALLCENTER:
			for (int i=0;i<results[0].agentenProCallcenter.length;i++) addAgentenLines(table,i,true);
			addAgentenLines(table,-1,true);
			break;
		case DATA_TYPE_AGENTS_SKILL_LEVEL:
		case DATA_TYPE_WORKLOAD_SKILL_LEVEL:
			for (int i=0;i<results[0].agentenProSkilllevel.length;i++) addAgentenLines(table,i,false);
			addAgentenLines(table,-1,false);
			break;
		case DATA_TYPE_SIM_AGENTS:
			groupNames=getModelAgentsGroups();
			for (int i=0;i<results.length;i++) {
				AgentModelData[] data=results[i].agentenModellProGruppe;
				for (int j=0;j<48;j++) {
					List<String> line=new ArrayList<>();
					line.add(getDayName(i)+", "+TimeTools.formatTime(j*1800)+"-"+TimeTools.formatTime((j+1)*1800-1));
					int sum=0;
					for (String name : groupNames) {
						AgentModelData rec=null; for (AgentModelData d : data) if (d.name.equals(name)) {rec=d; break;}
						int count=(rec==null)?0:((int)Math.round(rec.simAgents.densityData[j]));
						sum+=count;	line.add(""+count);
					}
					line.add(""+sum);
					table.addLine(line);
				}
			}
			break;
		case DATA_TYPE_MODEL_AGENTS:
			groupNames=getModelAgentsGroups();
			for (int i=0;i<results.length;i++) {
				AgentModelData[] data=results[i].agentenModellProGruppe;
				for (int j=0;j<48;j++) {
					List<String> line=new ArrayList<>();
					line.add(getDayName(i)+", "+TimeTools.formatTime(j*1800)+"-"+TimeTools.formatTime((j+1)*1800-1));
					int sum=0;
					for (String name : groupNames) {
						AgentModelData rec=null; for (AgentModelData d : data) if (d.name.equals(name)) {rec=d; break;}
						int count=(rec==null)?0:((int)Math.round(rec.modelAgents.densityData[j]));
						sum+=count;	line.add(""+count);
					}
					line.add(""+sum);
					table.addLine(line);
				}
			}
			break;
		case DATA_TYPE_FULL_AGENTS:
			groupNames=getModelAgentsGroups();
			for (int i=0;i<results.length;i++) {
				AgentModelData[] data=results[i].agentenModellProGruppe;
				for (int j=0;j<48;j++) {
					List<String> line=new ArrayList<>();
					line.add(getDayName(i)+", "+TimeTools.formatTime(j*1800)+"-"+TimeTools.formatTime((j+1)*1800-1));
					int sum=0;
					for (String name : groupNames) {
						AgentModelData rec=null; for (AgentModelData d : data) if (d.name.equals(name)) {rec=d; break;}
						int count=(rec==null)?0:((int)Math.round(rec.fullAgents.densityData[j]));
						sum+=count;	line.add(""+count);
					}
					line.add(""+sum);
					table.addLine(line);
				}
			}
			break;
		case DATA_TYPE_SUMMARY:
			/* Wird innerhalb von Summary aufgerufen. Nicht noch mal Summary. */
			break;
		}
	}

	/**
	 * Liefert den Namen für einen bestimmten Simulationstag.
	 * @param index	Index des Tages
	 * @return	Namen für einen bestimmten Simulationstag
	 */
	private String getDayName(int index) {
		if (index>=0 && index<results.length) {
			String date=results[index].editModel.date;
			if (date!=null && !date.isBlank()) return CallcenterModel.dateToLocalString(CallcenterModel.stringToDate(date));
		}

		return ""+(index+1);
	}

	/**
	 * Erstellt und liefert die Tabelle mit den auszugebenden Daten.
	 * @return	Tabelle mit den auszugebenden Daten
	 */
	private Table getTableData() {
		final Table table=new Table();

		/* Kein Processing, Daten direkt ausgeben */
		switch (dataType) {
		case DATA_TYPE_SIM_AGENTS:
		case DATA_TYPE_MODEL_AGENTS:
		case DATA_TYPE_FULL_AGENTS:
			addLines(table);
			return table;
		default:
			/* Verarbeitung s.u. */
			break;
		}

		/* Spalte 1 */
		String[] row=new String[results.length];
		for (int i=0;i<row.length;i++) row[i]=getDayName(i);
		table.addLine(row);

		/* Eigentliche Daten */
		if (dataType!=Mode.DATA_TYPE_SUMMARY) {
			addLines(table);
		} else {
			dataType=Mode.DATA_TYPE_CALLERS; addLines(table);
			dataType=Mode.DATA_TYPE_SUCCESS; addLines(table);
			dataType=Mode.DATA_TYPE_CANCEL; addLines(table);
			dataType=Mode.DATA_TYPE_WAITING_TIME; addLines(table);
			dataType=Mode.DATA_TYPE_STAYING_TIME; addLines(table);
			dataType=Mode.DATA_TYPE_SERVICE_LEVEL_CALLS_SUCCESS; addLines(table);
			dataType=Mode.DATA_TYPE_SERVICE_LEVEL_CLIENTS_SUCCESS; addLines(table);
			dataType=Mode.DATA_TYPE_SERVICE_LEVEL_CALLS_ALL; addLines(table);
			dataType=Mode.DATA_TYPE_SERVICE_LEVEL_CLIENTS_ALL; addLines(table);
			dataType=Mode.DATA_TYPE_AGENTS_CALLCENTER; addLines(table);
			dataType=Mode.DATA_TYPE_AGENTS_SKILL_LEVEL; addLines(table);
			dataType=Mode.DATA_TYPE_WORKLOAD_CALLCENTER; addLines(table);
			dataType=Mode.DATA_TYPE_WORKLOAD_SKILL_LEVEL; addLines(table);
			dataType=Mode.DATA_TYPE_SUMMARY;
		}

		return table.transpose();
	}
}
