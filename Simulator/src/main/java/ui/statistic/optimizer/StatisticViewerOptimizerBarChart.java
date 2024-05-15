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

import java.awt.Color;
import java.text.NumberFormat;
import java.util.ArrayList;
import java.util.List;

import org.jfree.chart.JFreeChart;
import org.jfree.chart.axis.CategoryLabelPositions;
import org.jfree.chart.axis.NumberAxis;
import org.jfree.chart.renderer.category.BarRenderer;
import org.jfree.chart.renderer.category.StandardBarPainter;
import org.jfree.data.category.DefaultCategoryDataset;

import language.Language;
import mathtools.TimeTools;
import mathtools.distribution.DataDistributionImpl;
import simulator.Statistics;
import simulator.Statistics.AgentenDaten;
import simulator.Statistics.KundenDaten;
import systemtools.statistics.StatisticViewerBarChart;
import ui.model.CallcenterModel;
import ui.model.CallcenterModelCallcenter;
import ui.model.CallcenterModelCaller;
import ui.model.CallcenterModelSkillLevel;
import ui.optimizer.OptimizeData;

/**
 * Stellt Veränderungen Ausgangsdaten-&gt;Optimierte Daten als Balkendiagramme dar.
 * @author Alexander Herzog
 * @version 1.0
 */
public class StatisticViewerOptimizerBarChart extends StatisticViewerBarChart {
	/** Statistik des Ausgangsmodells */
	private final Statistics statistic1;
	/** Statistik des finalen Modells */
	private final Statistics statistic2;
	/** Darstellungsart, siehe <code>DATA_TYPE_*</code> Konstanten. */
	private final int dataType;
	/** Index im <code>kundenProTyp</code>-Array bzw. im <code>agentenProCallcenter</code>-Array, aus dem die Daten genommen werden sollen. ("-1" bedeutet, dass das <code>kundenGlobal</code>- bzw. das <code>agentenGlobal</code>-Objekt verwendet wird.) */
	private final int dataNr;

	/**
	 * Zeigt die Veränderung der Anruferzahlen an.
	 */
	public static final int DATA_TYPE_CALLERS=0;

	/**
	 * Zeigt die Veränderung der Erreichbarkeit an.
	 */
	public static final int DATA_TYPE_SUCCESS=1;

	/**
	 * Zeigt die Veränderung der Abbrecherzahlen an.
	 */
	public static final int DATA_TYPE_CANCEL=2;

	/**
	 * Zeigt die Veränderung der Wartezeiten an.
	 */
	public static final int DATA_TYPE_WAITING_TIME=3;

	/**
	 * Zeigt die Veränderung der Verweilzeiten an.
	 */
	public static final int DATA_TYPE_STAYING_TIME=4;

	/**
	 * Zeigt die Veränderung des Service-Levels auf Anrufbasis (bezogen auf erfolgreiche Anrufe) an.
	 */
	public static final int DATA_TYPE_SERVICE_LEVEL_CALLS=5;

	/**
	 * Zeigt die Veränderung des Service-Levels auf Kundenbasis (bezogen auf erfolgreiche Kunden) an.
	 */
	public static final int DATA_TYPE_SERVICE_LEVEL_CLIENTS=6;

	/**
	 * Zeigt die Veränderung des Service-Levels auf Anrufbasis (bezogen auf alle Anrufe) an.
	 */
	public static final int DATA_TYPE_SERVICE_LEVEL_CALLS_ALL=7;

	/**
	 * Zeigt die Veränderung des Service-Levels auf Kundenbasis (bezogen auf alle Kunden) an.
	 */
	public static final int DATA_TYPE_SERVICE_LEVEL_CLIENTS_ALL=8;

	/**
	 * Zeigt die Veränderung der Agentenzahlen pro Callcenter an.
	 */
	public static final int DATA_TYPE_AGENTS_CALLCENTER=9;

	/**
	 * Zeigt die Veränderung der Agentenzahlen pro Skill-Level an.
	 */
	public static final int DATA_TYPE_AGENTS_SKILL_LEVEL=10;

	/**
	 * Zeigt die Veränderung der Auslastung pro Callcenter an.
	 */
	public static final int DATA_TYPE_WORKLOAD_CALLCENTER=11;

	/**
	 * Zeigt die Veränderung der Auslastung pro Skill-Level an.
	 */
	public static final int DATA_TYPE_WORKLOAD_SKILL_LEVEL=12;

	/**
	 * Konstruktor der Klasse <code>StatisticViewerOptimizerBarChart</code>
	 * @param results	Objekt vom Typ <code>OptimizeData</code>, dem die Optimierungsergebnisse entnommen werden sollen
	 * @param dataType	Darstellungsart, siehe <code>DATA_TYPE_*</code> Konstanten.
	 * @param dataNr	Index im <code>kundenProTyp</code>-Array bzw. im <code>agentenProCallcenter</code>-Array, aus dem die Daten genommen werden sollen. ("-1" bedeutet, dass das <code>kundenGlobal</code>- bzw. das <code>agentenGlobal</code>-Objekt verwendet wird.)
	 */
	public StatisticViewerOptimizerBarChart(OptimizeData results, int dataType, int dataNr) {
		super();
		statistic1=results.data.get(0);
		statistic2=results.data.get(results.data.size()-1);
		this.dataType=dataType;
		this.dataNr=dataNr;
	}

	/**
	 * Konfiguriert die Diagrammanzeige
	 * @param title	Diagrammtitel
	 * @param yLabel	y-Achsen-Beschriftung
	 * @param percent	Soll die y-Achse Zahlenwerte (<code>false</code>) oder Prozentwerte (<code>true</code>) anzeigen?
	 */
	private void initOptimizeChart(String title, String yLabel, boolean percent) {
		initBarChart(title);

		if (percent) {
			NumberAxis axis=new NumberAxis();
			NumberFormat formater=NumberFormat.getPercentInstance();
			formater.setMinimumFractionDigits(1);
			formater.setMaximumFractionDigits(1);
			axis.setNumberFormatOverride(formater);
			axis.setLabel(yLabel);
			plot.setRangeAxis(axis);
		}
		plot.getRangeAxis().setLabel(yLabel);

		plot.getDomainAxis().setLabel(Language.tr("Statistic.Units.IntervalHalfHour"));
		plot.getDomainAxis().setCategoryLabelPositions(CategoryLabelPositions.UP_45);

		((BarRenderer)(plot.getRenderer())).setDrawBarOutline(true);
		((BarRenderer)(plot.getRenderer())).setShadowVisible(false);
		((BarRenderer)(plot.getRenderer())).setBarPainter(new StandardBarPainter());

		initTooltips();
	}

	/**
	 * Fügt eine Textzeile zu einer Liste hinzu.
	 * @param list	Liste
	 * @param title	Titel der Textzeile
	 * @param subTitle	Untertitel der Textzeile
	 * @see #getDiagramTypesList(CallcenterModel)
	 */
	private static void addLineToList(final List<String> list, final String title, final String subTitle) {
		list.add("<html><body><b>"+title+"</b><br>"+subTitle+"</body></html>");
	}

	/**
	 * Fügt Textzeilen zu den Kundentypen zu einer Liste hinzu.
	 * @param list	Liste
	 * @param model	Gesamtes Modell
	 * @param title	Titel der Textzeilen
	 * @see #getDiagramTypesList(CallcenterModel)
	 */
	private static void addCallerLinesToList(final List<String> list, final CallcenterModel model, final String title) {
		for (CallcenterModelCaller caller: model.caller) list.add("<html><body><b>"+title+"</b><br>"+caller.name+"</body></html>");
		list.add("<html><body><b>"+title+"</b><br>"+Language.tr("SimStatistic.AllClients")+"</body></html>");
	}

	/**
	 * Fügt Textzeilen zu den Callcentern zu einer Liste hinzu.
	 * @param list	Liste
	 * @param model	Gesamtes Modell
	 * @param title	Titel der Textzeilen
	 * @see #getDiagramTypesList(CallcenterModel)
	 */
	private static void addCallcenterLinesToList(final List<String> list, final CallcenterModel model, final String title) {
		for (CallcenterModelCallcenter callcenter: model.callcenter) list.add("<html><body><b>"+title+"</b><br>"+callcenter.name+"</body></html>");
		list.add("<html><body><b>"+title+"</b><br>"+Language.tr("SimStatistic.AllCallcenter")+"</body></html>");
	}

	/**
	 * Fügt Textzeilen zu den Skill-Leveln zu einer Liste hinzu.
	 * @param list	Liste
	 * @param model	Gesamtes Modell
	 * @param title	Titel der Textzeilen
	 * @see #getDiagramTypesList(CallcenterModel)
	 */
	private static void addSkillLevelLinesToList(final List<String> list, final CallcenterModel model, final String title) {
		for (CallcenterModelSkillLevel skillLevel : model.skills) list.add("<html><body><b>"+title+"</b><br>"+skillLevel.name+"</body></html>");
		list.add("<html><body><b>"+title+"</b><br>"+Language.tr("SimStatistic.AllSkillLevel")+"</body></html>");
	}

	/**
	 * Liefert auf Basis eines Callcenter-Modells die Liste der verfügbaren Diagrammtypen
	 * @param model	Ausgangs-Callcenter-Modell
	 * @return	Liste der verfügbaren Diagrammtypen
	 */
	public static String[] getDiagramTypesList(CallcenterModel model) {
		final List<String> list=new ArrayList<>();

		addLineToList(list,Language.tr("Optimizer.SetupDiagrams.ListValue.OptimizeValue"),Language.tr("Optimizer.SetupDiagrams.ListValue.OptimizeValue.Info"));
		addLineToList(list,Language.tr("Optimizer.SetupDiagrams.ListValue.ChangeValue"),Language.tr("Optimizer.SetupDiagrams.ListValue.ChangeValue.Info"));

		addCallerLinesToList(list,model,Language.tr("SimStatistic.NumberOfCallers"));
		addCallerLinesToList(list,model,Language.tr("SimStatistic.Accessibility"));
		addCallerLinesToList(list,model,Language.tr("SimStatistic.NumberOfCancelations"));
		addCallerLinesToList(list,model,Language.tr("SimStatistic.WaitingTime"));
		addCallerLinesToList(list,model,Language.tr("SimStatistic.ResidenceTime"));
		addCallerLinesToList(list,model,Language.tr("SimStatistic.ServiceLevel")+" ("+Language.tr("SimStatistic.CalculatedOn.SuccessfulClients"));
		addCallerLinesToList(list,model,Language.tr("SimStatistic.ServiceLevel")+" ("+Language.tr("SimStatistic.CalculatedOn.SuccessfulCalls")+")");
		addCallerLinesToList(list,model,Language.tr("SimStatistic.ServiceLevel")+" ("+Language.tr("SimStatistic.CalculatedOn.AllClients")+")");
		addCallerLinesToList(list,model,Language.tr("SimStatistic.ServiceLevel")+" ("+Language.tr("SimStatistic.CalculatedOn.AllCalls")+")");
		addCallcenterLinesToList(list,model,Language.tr("SimStatistic.NumberOfAgents"));
		addSkillLevelLinesToList(list,model,Language.tr("SimStatistic.NumberOfAgents"));
		addCallcenterLinesToList(list,model,Language.tr("SimStatistic.WorkLoad"));
		addSkillLevelLinesToList(list,model,Language.tr("SimStatistic.WorkLoad"));

		return list.toArray(String[]::new);
	}

	/**
	 * Liefert den zu einem Diagramm-Setup gehörenden Eintrag
	 * in der {@link #getDiagramTypesList(CallcenterModel)}-Liste.
	 * @param model	Callcenter-Modell
	 * @param dataType	Darstellungsart, siehe <code>DATA_TYPE_*</code> Konstanten.
	 * @param dataNr	Index im <code>kundenProTyp</code>-Array bzw. im <code>agentenProCallcenter</code>-Array, aus dem die Daten genommen werden sollen. ("-1" bedeutet, dass das <code>kundenGlobal</code>- bzw. das <code>agentenGlobal</code>-Objekt verwendet wird.)
	 * @return	Passender Index in der Ergebnisliste von {@link #getDiagramTypesList(CallcenterModel)}
	 * @see #getDiagramTypesList(CallcenterModel)
	 * @see #dataTypeToListIndex(CallcenterModel, int, int)
	 */
	public static int dataTypeToListIndex(final CallcenterModel model, final int dataType, final int dataNr) {
		final int c1=model.caller.size();
		final int c2=model.callcenter.size();
		final int c3=model.skills.size();
		switch (dataType) {
		case -1: return 0;
		case -2: return 1;
		case DATA_TYPE_CALLERS: return 2+0*(c1+1)+((dataNr<0)?c1:dataNr);
		case DATA_TYPE_SUCCESS: return 2+1*(c1+1)+((dataNr<0)?c1:dataNr);
		case DATA_TYPE_CANCEL: return 2+2*(c1+1)+((dataNr<0)?c1:dataNr);
		case DATA_TYPE_WAITING_TIME: return 2+3*(c1+1)+((dataNr<0)?c1:dataNr);
		case DATA_TYPE_STAYING_TIME: return 2+4*(c1+1)+((dataNr<0)?c1:dataNr);
		case DATA_TYPE_SERVICE_LEVEL_CALLS: return 2+5*(c1+1)+((dataNr<0)?c1:dataNr);
		case DATA_TYPE_SERVICE_LEVEL_CLIENTS: return 2+6*(c1+1)+((dataNr<0)?c1:dataNr);
		case DATA_TYPE_SERVICE_LEVEL_CALLS_ALL: return 2+7*(c1+1)+((dataNr<0)?c1:dataNr);
		case DATA_TYPE_SERVICE_LEVEL_CLIENTS_ALL: return 2+8*(c1+1)+((dataNr<0)?c1:dataNr);
		case DATA_TYPE_AGENTS_CALLCENTER: return 2+9*(c1+1)+((dataNr<0)?c2:dataNr);
		case DATA_TYPE_AGENTS_SKILL_LEVEL: return 2+9*(c1+1)+1*(c2+1)+((dataNr<0)?c3:dataNr);
		case DATA_TYPE_WORKLOAD_CALLCENTER: return 2+9*(c1+1)+1*(c2+1)+1*(c3+1)+((dataNr<0)?c2:dataNr);
		case DATA_TYPE_WORKLOAD_SKILL_LEVEL: return 2+9*(c1+1)+2*(c2+1)+1*(c3+1)+((dataNr<0)?c3:dataNr);
		default: return 0;
		}
	}

	/**
	 * Diese Methode ist das Gegenstück zu {@link #dataTypeToListIndex(CallcenterModel, int, int)}.
	 * Sie liefert basierend auf dem Index in der {@link #getDiagramTypesList(CallcenterModel)}-Liste
	 * ein Diagramm-Setup.
	 * @param model	Callcenter-Modell
	 * @param index	Index in der {@link #getDiagramTypesList(CallcenterModel)}-Liste
	 * @return	Diagramm-Setup
	 */
	public static int[] listIndexToDataType(final CallcenterModel model, final int index) {
		if (index==0 || index==1) return new int[]{-(index+1),-1};

		int dataType=-1;
		int i=-1;
		while (i<=index) {
			dataType++;
			if (dataType>DATA_TYPE_WORKLOAD_SKILL_LEVEL) break;
			i=dataTypeToListIndex(model,dataType,0);
		}
		dataType--;

		i=-1;
		while (i<9999) {
			if (dataTypeToListIndex(model,dataType,i)==index) return new int[]{dataType,i};
			i++;
		}

		return new int[]{-1,-1};
	}

	/**
	 * Liefert passend zu den <code>DATA_TYPE_*</code> Konstanten Informationen zur Darstellung des Diagramms
	 * @param dataType	Darstellungsart, siehe <code>DATA_TYPE_*</code> Konstanten, für die Informationen geliefert werden soll
	 * @return	3-elementiges Array aus Diagrammtitel (String), y-Achsen-Beschriftung (String) und Angabe ob es Prozentwerte sind (Boolean)
	 */
	public static Object[] getChartInitData(int dataType) {
		switch (dataType) {
		case DATA_TYPE_CALLERS: return new Object[]{Language.tr("SimStatistic.NumberOfCallers"),Language.tr("SimStatistic.NumberPerDay"),false};
		case DATA_TYPE_SUCCESS: return new Object[]{Language.tr("SimStatistic.Accessibility"),Language.tr("SimStatistic.AverageAccessibility"),true};
		case DATA_TYPE_CANCEL: return new Object[]{Language.tr("SimStatistic.NumberOfCancelations"),Language.tr("SimStatistic.NumberPerDay"),false};
		case DATA_TYPE_WAITING_TIME: return new Object[]{Language.tr("SimStatistic.WaitingTime"),Language.tr("SimStatistic.AverageWaitingTime"),false};
		case DATA_TYPE_STAYING_TIME: return new Object[]{Language.tr("SimStatistic.ResidenceTime"),Language.tr("SimStatistic.AverageResidenceTime"),false};
		case DATA_TYPE_SERVICE_LEVEL_CALLS: return new Object[]{Language.tr("SimStatistic.ServiceLevel"),Language.tr("SimStatistic.ServiceLevel"),true};
		case DATA_TYPE_SERVICE_LEVEL_CLIENTS: return new Object[]{Language.tr("SimStatistic.ServiceLevel"),Language.tr("SimStatistic.ServiceLevel"),true};
		case DATA_TYPE_SERVICE_LEVEL_CALLS_ALL: return new Object[]{Language.tr("SimStatistic.ServiceLevel"),Language.tr("SimStatistic.ServiceLevel"),true};
		case DATA_TYPE_SERVICE_LEVEL_CLIENTS_ALL: return new Object[]{Language.tr("SimStatistic.ServiceLevel"),Language.tr("SimStatistic.ServiceLevel"),true};
		case DATA_TYPE_AGENTS_CALLCENTER: return new Object[]{Language.tr("SimStatistic.NumberOfAgents"),Language.tr("SimStatistic.ManHoursPerDay"),false};
		case DATA_TYPE_AGENTS_SKILL_LEVEL: return new Object[]{Language.tr("SimStatistic.NumberOfAgents"),Language.tr("SimStatistic.ManHoursPerDay"),false};
		case DATA_TYPE_WORKLOAD_CALLCENTER: return new Object[]{Language.tr("SimStatistic.WorkLoad"),Language.tr("SimStatistic.WorkLoad"),true};
		case DATA_TYPE_WORKLOAD_SKILL_LEVEL: return new Object[]{Language.tr("SimStatistic.WorkLoad"),Language.tr("SimStatistic.WorkLoad"),true};
		}
		return null;
	}

	/**
	 * Trägt Vergleichsdaten aus zwei Statistikdatensätzen in ein Balkendiagramm ein
	 * @param chart	Balkendiagramm das die Daten darstellen soll
	 * @param statistic1	Statiatikdatensatz 1
	 * @param statistic2	Statiatikdatensatz 2
	 * @param dataType	Darstellungsart, siehe <code>DATA_TYPE_*</code> Konstanten.
	 * @param dataNr	Index im <code>kundenProTyp</code>-Array bzw. im <code>agentenProCallcenter</code>-Array, aus dem die Daten genommen werden sollen. ("-1" bedeutet, dass das <code>kundenGlobal</code>- bzw. das <code>agentenGlobal</code>-Objekt verwendet wird.)
	 */
	public static void setChartData(JFreeChart chart, Statistics statistic1, Statistics statistic2, int dataType, int dataNr) {
		String type="";
		KundenDaten kunden1=null;
		KundenDaten kunden2=null;
		AgentenDaten agenten1=null;
		AgentenDaten agenten2=null;
		int dataSource=0;
		if (dataType==DATA_TYPE_AGENTS_CALLCENTER || dataType==DATA_TYPE_WORKLOAD_CALLCENTER) dataSource=1;
		if (dataType==DATA_TYPE_AGENTS_SKILL_LEVEL || dataType==DATA_TYPE_WORKLOAD_SKILL_LEVEL) dataSource=2;

		switch (dataSource) {
		case 0:
			type=(dataNr<0)?Language.tr("SimStatistic.AllClients"):statistic1.kundenProTyp[dataNr].name;
			if (dataNr<0) {
				kunden1=statistic1.kundenGlobal;
				kunden2=statistic2.kundenGlobal;
			} else {
				kunden1=statistic1.kundenProTyp[dataNr];
				kunden2=statistic2.kundenProTyp[dataNr];
			}
			break;
		case 1:
			type=(dataNr<0)?Language.tr("SimStatistic.AllCallcenter"):statistic1.agentenProCallcenter[dataNr].name;
			if (dataNr<0) {
				agenten1=statistic1.agentenGlobal;
				agenten2=statistic2.agentenGlobal;
			} else {
				agenten1=statistic1.agentenProCallcenter[dataNr];
				agenten2=statistic2.agentenProCallcenter[dataNr];
			}

			break;
		case 2:
			type=(dataNr<0)?Language.tr("SimStatistic.AllSkillLevel"):statistic1.agentenProSkilllevel[dataNr].name;
			if (dataNr<0) {
				agenten1=statistic1.agentenGlobal;
				agenten2=statistic2.agentenGlobal;
			} else {
				agenten1=statistic1.agentenProSkilllevel[dataNr];
				agenten2=statistic2.agentenProSkilllevel[dataNr];
			}
			break;
		}

		DataDistributionImpl dist1=null;
		DataDistributionImpl dist2=null;
		boolean up=false;
		boolean round=false;
		switch (dataType) {
		case DATA_TYPE_CALLERS:
			if (kunden1!=null) dist1=kunden1.anrufeProIntervall.divide(statistic1.simulationData.runRepeatCount);
			if (kunden2!=null) dist2=kunden2.anrufeProIntervall.divide(statistic1.simulationData.runRepeatCount);
			up=false;
			break;
		case DATA_TYPE_SUCCESS:
			if (kunden1!=null) dist1=kunden1.anrufeErfolgProIntervall.divide(kunden1.anrufeProIntervall);
			if (kunden2!=null) dist2=kunden2.anrufeErfolgProIntervall.divide(kunden2.anrufeProIntervall);
			up=true;
			break;
		case DATA_TYPE_CANCEL:
			if (kunden1!=null) dist1=kunden1.anrufeAbbruchProIntervall.divide(statistic1.simulationData.runRepeatCount);
			if (kunden2!=null) dist2=kunden2.anrufeAbbruchProIntervall.divide(statistic1.simulationData.runRepeatCount);
			up=false;
			break;
		case DATA_TYPE_WAITING_TIME:
			if (kunden1!=null) dist1=kunden1.anrufeWartezeitSumProIntervall.divide(kunden1.anrufeErfolgProIntervall);
			if (kunden2!=null) dist2=kunden2.anrufeWartezeitSumProIntervall.divide(kunden2.anrufeErfolgProIntervall);
			up=false;
			break;
		case DATA_TYPE_STAYING_TIME:
			if (kunden1!=null) dist1=kunden1.anrufeVerweilzeitSumProIntervall.divide(kunden1.anrufeErfolgProIntervall);
			if (kunden2!=null) dist2=kunden2.anrufeVerweilzeitSumProIntervall.divide(kunden2.anrufeErfolgProIntervall);
			up=false;
			break;
		case DATA_TYPE_SERVICE_LEVEL_CALLS:
			if (kunden1!=null) dist1=kunden1.anrufeServicelevelProIntervall.divide(kunden1.anrufeErfolgProIntervall);
			if (kunden2!=null) dist2=kunden2.anrufeServicelevelProIntervall.divide(kunden2.anrufeErfolgProIntervall);
			up=true;
			break;
		case DATA_TYPE_SERVICE_LEVEL_CLIENTS:
			if (kunden1!=null) dist1=kunden1.kundenServicelevelProIntervall.divide(kunden1.kundenErfolgProIntervall);
			if (kunden2!=null) dist2=kunden2.kundenServicelevelProIntervall.divide(kunden2.kundenErfolgProIntervall);
			up=true;
			break;
		case DATA_TYPE_SERVICE_LEVEL_CALLS_ALL:
			if (kunden1!=null) dist1=kunden1.anrufeServicelevelProIntervall.divide(kunden1.anrufeProIntervall);
			if (kunden2!=null) dist2=kunden2.anrufeServicelevelProIntervall.divide(kunden2.anrufeProIntervall);
			up=true;
			break;
		case DATA_TYPE_SERVICE_LEVEL_CLIENTS_ALL:
			if (kunden1!=null) dist1=kunden1.kundenServicelevelProIntervall.divide(kunden1.kundenProIntervall);
			if (kunden2!=null) dist2=kunden2.kundenServicelevelProIntervall.divide(kunden2.kundenProIntervall);
			up=true;
			break;
		case DATA_TYPE_AGENTS_CALLCENTER:
		case DATA_TYPE_AGENTS_SKILL_LEVEL:
			if (agenten1!=null) dist1=agenten1.leerlaufProIntervall.add(agenten1.technischerLeerlaufProIntervall).add(agenten1.arbeitProIntervall).add(agenten1.postProcessingProIntervall).divide(statistic1.simulationData.runRepeatCount).divide(3600);
			if (agenten2!=null) dist2=agenten2.leerlaufProIntervall.add(agenten2.technischerLeerlaufProIntervall).add(agenten2.arbeitProIntervall).add(agenten2.postProcessingProIntervall).divide(statistic2.simulationData.runRepeatCount).divide(3600);
			up=true;
			round=true;
			break;
		case DATA_TYPE_WORKLOAD_CALLCENTER:
		case DATA_TYPE_WORKLOAD_SKILL_LEVEL:
			if (agenten1!=null) dist1=agenten1.leerlaufProIntervall.add(agenten1.technischerLeerlaufProIntervall).add(agenten1.arbeitProIntervall).add(agenten1.postProcessingProIntervall);
			if (agenten2!=null) dist2=agenten2.leerlaufProIntervall.add(agenten2.technischerLeerlaufProIntervall).add(agenten2.arbeitProIntervall).add(agenten2.postProcessingProIntervall);
			if (agenten1!=null) dist1=agenten1.leerlaufProIntervall.divide(dist1);
			if (agenten2!=null) dist2=agenten2.leerlaufProIntervall.divide(dist2);
			if (dist1!=null && dist2!=null) {
				DataDistributionImpl one=dist1.clone(); one.setToValue(1);
				dist1=one.sub(dist1);
				dist2=one.sub(dist2);
			}
			up=false;
			break;
		}

		DefaultCategoryDataset data=new DefaultCategoryDataset();

		for (int i=0;i<48;i++) {
			String time=TimeTools.formatShortTime(i*1800);

			if (up) {
				double valueStart=(dist1==null)?0:dist1.densityData[i];
				double valueDelta=((dist2==null)?0:dist2.densityData[i])-((dist1==null)?0:dist1.densityData[i]);
				if (round) valueDelta=Math.round(valueDelta); else valueDelta=((double)Math.round(valueDelta*1000))/1000;
				if (valueDelta>=0) {
					data.addValue(valueStart,Language.tr("SimStatistic.Optimize.StartValue")+" ("+type+")",time);
					data.addValue(valueDelta,Language.tr("SimStatistic.Optimize.FinalValue")+" ("+type+")",time);
					data.addValue(0,Language.tr("SimStatistic.Optimize.FinalValue")+"2 ("+type+")",time);
				} else {
					data.addValue(0,Language.tr("SimStatistic.Optimize.StartValue")+" ("+type+")",time);
					data.addValue(valueStart+valueDelta,Language.tr("SimStatistic.Optimize.FinalValue")+" ("+type+")",time);
					data.addValue(-valueDelta,Language.tr("SimStatistic.Optimize.FinalValue")+"2 ("+type+")",time);
				}
			} else {
				double valueFinal=(dist2==null)?0:dist2.densityData[i];
				double valueDelta=((dist1==null)?0:dist1.densityData[i])-((dist2==null)?0:dist2.densityData[i]);
				if (valueDelta>=0) {
					data.addValue(valueFinal,Language.tr("SimStatistic.Optimize.FinalValue")+" ("+type+")",time);
					data.addValue(valueDelta,Language.tr("SimStatistic.Optimize.StartValue")+" ("+type+")",time);
					data.addValue(0,Language.tr("SimStatistic.Optimize.FinalValue")+"2 ("+type+")",time);
				} else {
					data.addValue(0,Language.tr("SimStatistic.Optimize.FinalValue")+" ("+type+")",time);
					data.addValue(valueFinal+valueDelta,Language.tr("SimStatistic.Optimize.StartValue")+" ("+type+")",time);
					data.addValue(-valueDelta,Language.tr("SimStatistic.Optimize.FinalValue")+"2 ("+type+")",time);
				}
			}
		}

		chart.getCategoryPlot().setDataset(data);

		chart.getCategoryPlot().getRendererForDataset(data).setSeriesVisibleInLegend(2,false);
		chart.getCategoryPlot().getRendererForDataset(data).setSeriesPaint(0,Color.BLUE);
		chart.getCategoryPlot().getRendererForDataset(data).setSeriesPaint(1,Color.RED);
		chart.getCategoryPlot().getRendererForDataset(data).setSeriesPaint(2,Color.BLUE);
	}

	@Override
	protected void firstChartRequest() {
		Object[] obj=getChartInitData(dataType);
		if (obj!=null) {
			initOptimizeChart((String)obj[0],(String)obj[1],(Boolean)obj[2]);
			setChartData(chart,statistic1,statistic2,dataType,dataNr);
		}
	}
}
