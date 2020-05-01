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
package ui.statistic;

import java.io.File;
import java.net.URL;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.w3c.dom.Element;

import language.Language;
import simulator.Statistics;
import systemtools.MsgBox;
import systemtools.statistics.StatisticNode;
import systemtools.statistics.StatisticViewer;
import tools.SetupData;
import ui.HelpLink;
import ui.StatisticWebAppWriter;
import ui.VersionConst;
import ui.images.Images;
import ui.statistic.core.StatisticBasePanel;
import ui.statistic.simulation.StatisticViewerAgentenIntervalTable;
import ui.statistic.simulation.StatisticViewerAgentenTable;
import ui.statistic.simulation.StatisticViewerAgentenText;
import ui.statistic.simulation.StatisticViewerAuslastungPieChart;
import ui.statistic.simulation.StatisticViewerErlangCIntervalTable;
import ui.statistic.simulation.StatisticViewerErlangCLineChart;
import ui.statistic.simulation.StatisticViewerKostenText;
import ui.statistic.simulation.StatisticViewerKundenAgentenBarChart;
import ui.statistic.simulation.StatisticViewerKundenAgentenLineChart;
import ui.statistic.simulation.StatisticViewerKundenAnteilePieChart;
import ui.statistic.simulation.StatisticViewerKundenIntervalTable;
import ui.statistic.simulation.StatisticViewerKundenTable;
import ui.statistic.simulation.StatisticViewerKundenText;
import ui.statistic.simulation.StatisticViewerTextInformation;
import xml.XMLTools;

/**
 * Statistikpanel, welches die Ergebnisse einer Simulation anzeigt
 * @author Alexander Herzog
 * @version 1.0
 */
public final class StatisticPanel extends StatisticBasePanel {
	private static final long serialVersionUID = -2131842717612014699L;

	private Statistics[] statisticData;

	/**
	 * Konstruktor der Klasse
	 * @param nebeneinander	Sollen mehrere Statistikdaten nebeneinander (<code>true</code>) oder untereinander (<code>false</code>) angezeigt werden?
	 * @param helpLink	Hilfe-Link
	 * @param helpModal	Callback zum Aufurf eines modalen Hilfe-Fensters
	 * @param startSilmulation	Callback, das ausgelöst wird, wenn der Nutzer auf der "Noch keine Daten"-Seite auf "Simulation jetzt starten" klickt. (Wird hier <code>null</code> übergeben, so wird diese Option nicht angezeigt.)
	 * @param loadStatistics	Callback zum Laden von Statistikdaten
	 * @param numberOfViewers	Anzahl der nebeneinander anzuzeigenden Ergebnisse
	 */
	public StatisticPanel(boolean nebeneinander, HelpLink helpLink, Runnable helpModal, Runnable startSilmulation, Runnable loadStatistics, int numberOfViewers) {
		super(Language.tr("MainMenu.View.Statistics"),Images.STATISTICS_DARK.getURL(),Language.tr("CommandLine.Report.Name"),null,nebeneinander,true,helpModal,helpLink,startSilmulation,loadStatistics,numberOfViewers,true);
		statisticData=new Statistics[numberOfViewers];
		Arrays.fill(statisticData,null);
		setStatistic(statisticData);
	}

	/**
	 * Liefert die bisher eingestellten Statistik-Objekt (Einträge in dem Array können auch <code>null</code> sein)
	 * @return	Aktuelle Statistik-Objekte
	 */
	public Statistics[] getStatistic() {
		return statisticData;
	}

	/**
	 * Setzt mehrere Statistik-Objekte für die parallele Anzeige (kann auch <code>null</code> sein, wenn nichts ausgegeben werden soll)
	 * @param data	Neue anzuzeigende Statistik-Objekte
	 */
	public void setStatistic(Statistics[] data) {
		setStatistic(data,null);
		setDataFileName(null);
	}

	/**
	 * Setzt mehrere Statistik-Objekte für die parallele Anzeige (kann auch <code>null</code> sein, wenn nichts ausgegeben werden soll)
	 * @param data	Neue anzuzeigende Statistik-Objekte
	 * @param title	Titel über den Anzeigen
	 */
	public void setStatistic(Statistics[] data, String[] title) {
		/* Leeres Array abfangen */
		if (data==null || data.length==0) {
			data=new Statistics[Math.max(statisticData.length,1)];
			Arrays.fill(data,null);
		}

		/* Zu lange oder zu kurze Arrays anpassen */
		Statistics[] data2=new Statistics[statisticData.length];
		for (int i=0;i<data2.length;i++) data2[i]=(i<data.length)?data[i]:null;
		data=data2;

		statisticData=data;

		/* Titel */
		String[] titleArray=new String[data.length];
		for (int i=0;i<titleArray.length;i++) {
			if (title==null || title.length<=i || title[i]==null) titleArray[i]=null; else titleArray[i]=title[i];
		}
		additionalTitle=titleArray;

		final StatisticNode root=new StatisticNode();
		Statistics doc=null;
		if (data[0]!=null) {
			addStatisticToTree(root,data);
			doc=data[0];
		}

		setStatisticData(root,doc);
	}

	private class MultiNumbers {
		public final String name;
		public int[] nr;
		public MultiNumbers(String name, int size) {this.name=name; nr=new int[size];}
	}

	private MultiNumbers[] callerTypes(Statistics data[]) {
		List<MultiNumbers> list=new ArrayList<MultiNumbers>();
		if (data.length>0 && data[0]!=null) for (int i=0;i<data[0].kundenProTyp.length;i++) {
			String name=data[0].kundenProTyp[i].name;
			MultiNumbers rec=new MultiNumbers(name,data.length); rec.nr[0]=i;
			boolean ok1=true;
			for (int j=1;j<data.length;j++) if (data[j]!=null) {
				boolean ok2=false;
				for (int k=0;k<data[j].kundenProTyp.length;k++) if (data[j].kundenProTyp[k].name.equals(name)) {ok2=true; rec.nr[j]=k; break;}
				if (!ok2) {ok1=false; break;}
			}
			if (ok1) list.add(rec);
		}
		return list.toArray(new MultiNumbers[0]);
	}

	private MultiNumbers[] agentCallcenterTypes(Statistics data[]) {
		List<MultiNumbers> list=new ArrayList<MultiNumbers>();
		if (data.length>0 && data[0]!=null) for (int i=0;i<data[0].agentenProCallcenter.length;i++) {
			String name=data[0].agentenProCallcenter[i].name;
			MultiNumbers rec=new MultiNumbers(name,data.length); rec.nr[0]=i;
			boolean ok1=true;
			for (int j=1;j<data.length;j++) if (data[j]!=null) {
				boolean ok2=false;
				for (int k=0;k<data[j].agentenProCallcenter.length;k++) if (data[j].agentenProCallcenter[k].name.equals(name)) {ok2=true; rec.nr[j]=k; break;}
				if (!ok2) {ok1=false; break;}
			}
			if (ok1) list.add(rec);
		}
		return list.toArray(new MultiNumbers[0]);
	}

	private MultiNumbers[] agentSkillLevelTypes(Statistics data[]) {
		List<MultiNumbers> list=new ArrayList<MultiNumbers>();
		if (data.length>0 && data[0]!=null) for (int i=0;i<data[0].agentenProSkilllevel.length;i++) {
			String name=data[0].agentenProSkilllevel[i].name;
			MultiNumbers rec=new MultiNumbers(name,data.length); rec.nr[0]=i;
			boolean ok1=true;
			for (int j=1;j<data.length;j++) if (data[j]!=null) {
				boolean ok2=false;
				for (int k=0;k<data[j].agentenProSkilllevel.length;k++) if (data[j].agentenProSkilllevel[k].name.equals(name)) {ok2=true; rec.nr[j]=k; break;}
				if (!ok2) {ok1=false; break;}
			}
			if (ok1) list.add(rec);
		}
		return list.toArray(new MultiNumbers[0]);
	}

	private void addStatisticToTree(StatisticNode root, Statistics data[]) {
		StatisticNode node, node2, node3;
		List<StatisticViewer> list=new ArrayList<StatisticViewer>();

		MultiNumbers[] caller=callerTypes(data);
		MultiNumbers[] agentsCallcenter=agentCallcenterTypes(data);
		MultiNumbers[] agentsSkills=agentSkillLevelTypes(data);

		/* Allgemeine Daten */
		list.clear(); for (int i=0;i<data.length;i++) list.add(new StatisticViewerTextInformation(data[i],StatisticViewerTextInformation.Mode.MODE_BASE_INFORMATION));
		root.addChild(new StatisticNode(Language.tr("SimStatistic.ResultOverview"),list.toArray(new StatisticViewer[0]),Language.tr("SimStatistic.Category.General")));
		list.clear(); for (int i=0;i<data.length;i++) list.add(new StatisticViewerTextInformation(data[i],StatisticViewerTextInformation.Mode.MODE_MODEL_INFORMATION));
		root.addChild(new StatisticNode(Language.tr("SimStatistic.ModelInformation"),list.toArray(new StatisticViewer[0]),Language.tr("SimStatistic.Category.General")));
		list.clear(); for (int i=0;i<data.length;i++) list.add(new StatisticViewerTextInformation(data[i],StatisticViewerTextInformation.Mode.MODE_CONFIDENCE_INFORMATION));
		root.addChild(new StatisticNode(Language.tr("SimStatistic.ConfidenceIntervals"),list.toArray(new StatisticViewer[0]),Language.tr("SimStatistic.Category.General")));
		list.clear(); for (int i=0;i<data.length;i++) list.add(new StatisticViewerTextInformation(data[i],StatisticViewerTextInformation.Mode.MODE_THRESHOLDS));
		root.addChild(new StatisticNode(Language.tr("SimStatistic.ThresholdsAndRecommendations"),list.toArray(new StatisticViewer[0]),Language.tr("SimStatistic.Category.General")));

		/* Kundendaten */
		root.addChild(node=new StatisticNode(Language.tr("SimStatistic.Clients")));

		node.addChild(node2=new StatisticNode(Language.tr("SimStatistic.NumberOfCallers"),Language.tr("SimStatistic.Category.NumberOfCallers")));
		list.clear(); for (int i=0;i<data.length;i++) list.add(new StatisticViewerKundenText(data[i],StatisticViewerKundenText.Mode.DATA_TYPE_COUNT));
		node2.addChild(new StatisticNode(Language.tr("SimStatistic.Overview"),list.toArray(new StatisticViewer[0])));
		list.clear(); for (int i=0;i<data.length;i++) list.add(new StatisticViewerKundenTable(data[i],StatisticViewerKundenTable.Mode.DATA_TYPE_COUNT));
		node2.addChild(new StatisticNode(Language.tr("SimStatistic.Overview"),list.toArray(new StatisticViewer[0])));
		node2.addChild(node3=new StatisticNode(Language.tr("SimStatistic.PartsPerCallerType"),true,Language.tr("SimStatistic.Category.NumberOfCallers.PartsPerCallerType")));
		list.clear(); for (int i=0;i<data.length;i++) list.add(new StatisticViewerKundenAnteilePieChart(data[i],StatisticViewerKundenAnteilePieChart.Mode.DATA_TYPE_FRESH_CALLS));
		node3.addChild(new StatisticNode(Language.tr("SimStatistic.FreshCalls"),list.toArray(new StatisticViewer[0])));
		list.clear(); for (int i=0;i<data.length;i++) list.add(new StatisticViewerKundenAnteilePieChart(data[i],StatisticViewerKundenAnteilePieChart.Mode.DATA_TYPE_CALLS));
		node3.addChild(new StatisticNode(Language.tr("SimStatistic.Calls"),list.toArray(new StatisticViewer[0])));
		list.clear(); for (int i=0;i<data.length;i++) list.add(new StatisticViewerKundenAnteilePieChart(data[i],StatisticViewerKundenAnteilePieChart.Mode.DATA_TYPE_RECALLS));
		node3.addChild(new StatisticNode(Language.tr("SimStatistic.RecallingClients"),list.toArray(new StatisticViewer[0])));
		list.clear(); for (int i=0;i<data.length;i++) list.add(new StatisticViewerKundenAnteilePieChart(data[i],StatisticViewerKundenAnteilePieChart.Mode.DATA_TYPE_CANCELED_CALLS));
		node3.addChild(new StatisticNode(Language.tr("SimStatistic.CanceledCalls"),list.toArray(new StatisticViewer[0])));
		list.clear(); for (int i=0;i<data.length;i++) list.add(new StatisticViewerKundenAnteilePieChart(data[i],StatisticViewerKundenAnteilePieChart.Mode.DATA_TYPE_CONTINUED_CALLS));
		node3.addChild(new StatisticNode(Language.tr("SimStatistic.ForwardedCalls"),list.toArray(new StatisticViewer[0])));
		list.clear(); for (int i=0;i<data.length;i++) list.add(new StatisticViewerKundenAnteilePieChart(data[i],StatisticViewerKundenAnteilePieChart.Mode.DATA_TYPE_RETRIED_CALLS));
		node3.addChild(new StatisticNode(Language.tr("SimStatistic.RetryCalls"),list.toArray(new StatisticViewer[0])));

		node.addChild(node2=new StatisticNode(Language.tr("SimStatistic.NumberOfCallersOverTheDay"),true,Language.tr("SimStatistic.Category.NumberOfCallersOverTheDay")));
		list.clear(); for (int i=0;i<data.length;i++) list.add(new StatisticViewerKundenIntervalTable(data[i],StatisticViewerKundenIntervalTable.Mode.DATA_TYPE_CLIENTS));
		node2.addChild(new StatisticNode(Language.tr("SimStatistic.FreshCalls"),list.toArray(new StatisticViewer[0])));
		list.clear(); for (int i=0;i<data.length;i++) list.add(new StatisticViewerKundenIntervalTable(data[i],StatisticViewerKundenIntervalTable.Mode.DATA_TYPE_RECALLS));
		node2.addChild(new StatisticNode(Language.tr("SimStatistic.RecallingClients"),list.toArray(new StatisticViewer[0])));
		list.clear(); for (int i=0;i<data.length;i++) list.add(new StatisticViewerKundenIntervalTable(data[i],StatisticViewerKundenIntervalTable.Mode.DATA_TYPE_CALLS));
		node2.addChild(new StatisticNode(Language.tr("SimStatistic.Calls"),list.toArray(new StatisticViewer[0])));
		list.clear(); for (int i=0;i<data.length;i++) list.add(new StatisticViewerKundenIntervalTable(data[i],StatisticViewerKundenIntervalTable.Mode.DATA_TYPE_CALLS_CONTINUE));
		node2.addChild(new StatisticNode(Language.tr("SimStatistic.ForwardedCalls.Number"),list.toArray(new StatisticViewer[0])));
		list.clear(); for (int i=0;i<data.length;i++) list.add(new StatisticViewerKundenIntervalTable(data[i],StatisticViewerKundenIntervalTable.Mode.DATA_TYPE_CALLS_CONTINUE_PART));
		node2.addChild(new StatisticNode(Language.tr("SimStatistic.ForwardedCalls.Part"),list.toArray(new StatisticViewer[0])));
		list.clear(); for (int i=0;i<data.length;i++) list.add(new StatisticViewerKundenIntervalTable(data[i],StatisticViewerKundenIntervalTable.Mode.DATA_TYPE_CALLS_RETRIED));
		node2.addChild(new StatisticNode(Language.tr("SimStatistic.RetryCalls"),list.toArray(new StatisticViewer[0])));

		if (caller.length>1) for (MultiNumbers m : caller) {
			list.clear(); for (int i=0;i<data.length;i++) list.add(new StatisticViewerKundenAgentenLineChart(data[i],StatisticViewerKundenAgentenLineChart.Mode.DATA_TYPE_CALLER,m.nr[i]));
			node2.addChild(new StatisticNode(m.name,list.toArray(new StatisticViewer[0]),Language.tr("SimStatistic.Category.NumberOfCallersOverTheDay.PerCallerType")));
		}
		list.clear(); for (int i=0;i<data.length;i++) list.add(new StatisticViewerKundenAgentenLineChart(data[i],StatisticViewerKundenAgentenLineChart.Mode.DATA_TYPE_CALLER,-1));
		node2.addChild(new StatisticNode(Language.tr("SimStatistic.AllClients"),list.toArray(new StatisticViewer[0])));

		node.addChild(node2=new StatisticNode(Language.tr("SimStatistic.WaitingTimesPerCallerType"),Language.tr("SimStatistic.Category.WaitingTimesPerCallerType")));
		list.clear(); for (int i=0;i<data.length;i++) list.add(new StatisticViewerKundenText(data[i],StatisticViewerKundenText.Mode.DATA_TYPE_WAITINGTIME));
		node2.addChild(new StatisticNode(Language.tr("SimStatistic.Overview"),list.toArray(new StatisticViewer[0])));
		list.clear(); for (int i=0;i<data.length;i++) list.add(new StatisticViewerKundenTable(data[i],StatisticViewerKundenTable.Mode.DATA_TYPE_WAITINGTIME));
		node2.addChild(new StatisticNode(Language.tr("SimStatistic.Overview"),list.toArray(new StatisticViewer[0])));
		list.clear(); for (int i=0;i<data.length;i++) list.add(new StatisticViewerKundenAgentenBarChart(data[i],StatisticViewerKundenAgentenBarChart.Mode.DATA_TYPE_WAITINGTIME_BYCLIENT));
		node2.addChild(new StatisticNode(Language.tr("SimStatistic.WaitingTimes")+" ("+Language.tr("SimStatistic.OnClientBasis")+")",list.toArray(new StatisticViewer[0]),Language.tr("SimStatistic.Category.WaitingTimesPerCallerType.SingleGraphics")));
		list.clear(); for (int i=0;i<data.length;i++) list.add(new StatisticViewerKundenAgentenBarChart(data[i],StatisticViewerKundenAgentenBarChart.Mode.DATA_TYPE_WAITINGTIME_BYCALL));
		node2.addChild(new StatisticNode(Language.tr("SimStatistic.WaitingTimes")+" ("+Language.tr("SimStatistic.OnCallBasis")+")",list.toArray(new StatisticViewer[0]),Language.tr("SimStatistic.Category.WaitingTimesPerCallerType.SingleGraphics")));
		list.clear(); for (int i=0;i<data.length;i++) list.add(new StatisticViewerKundenAgentenBarChart(data[i],StatisticViewerKundenAgentenBarChart.Mode.DATA_TYPE_STAYINGTIME_BYCLIENT));
		node2.addChild(new StatisticNode(Language.tr("SimStatistic.ResidenceTimes")+" ("+Language.tr("SimStatistic.OnClientBasis")+")",list.toArray(new StatisticViewer[0]),Language.tr("SimStatistic.Category.WaitingTimesPerCallerType.SingleGraphics")));
		list.clear(); for (int i=0;i<data.length;i++) list.add(new StatisticViewerKundenAgentenBarChart(data[i],StatisticViewerKundenAgentenBarChart.Mode.DATA_TYPE_STAYINGTIME_BYCALL));
		node2.addChild(new StatisticNode(Language.tr("SimStatistic.ResidenceTimes")+" ("+Language.tr("SimStatistic.OnCallBasis")+")",list.toArray(new StatisticViewer[0]),Language.tr("SimStatistic.Category.WaitingTimesPerCallerType.SingleGraphics")));
		list.clear(); for (int i=0;i<data.length;i++) list.add(new StatisticViewerKundenAgentenBarChart(data[i],StatisticViewerKundenAgentenBarChart.Mode.DATA_TYPE_WAITINGANDSTAYINGTIME_BYCLIENT));
		node2.addChild(new StatisticNode(Language.tr("SimStatistic.WaitingAndResidenceTimes")+" ("+Language.tr("SimStatistic.OnClientBasis")+")",list.toArray(new StatisticViewer[0])));
		list.clear(); for (int i=0;i<data.length;i++) list.add(new StatisticViewerKundenAgentenBarChart(data[i],StatisticViewerKundenAgentenBarChart.Mode.DATA_TYPE_WAITINGANDSTAYINGTIME_BYCALL));
		node2.addChild(new StatisticNode(Language.tr("SimStatistic.WaitingAndResidenceTimes")+" ("+Language.tr("SimStatistic.OnCallBasis")+")",list.toArray(new StatisticViewer[0])));

		node.addChild(node2=new StatisticNode(Language.tr("SimStatistic.WaitingAndResidenceTimesDistribution"),true,Language.tr("SimStatistic.Category.WaitingAndResidenceTimesDistribution")));
		list.clear(); for (int i=0;i<data.length;i++) list.add(new StatisticViewerKundenTable(data[i],StatisticViewerKundenTable.Mode.DATA_TYPE_CLIENT_WAITINGTIME_DIST));
		node2.addChild(new StatisticNode(Language.tr("SimStatistic.WaitingTimesDistribution")+" ("+Language.tr("SimStatistic.OnClientBasis")+")",list.toArray(new StatisticViewer[0])));
		list.clear(); for (int i=0;i<data.length;i++) list.add(new StatisticViewerKundenTable(data[i],StatisticViewerKundenTable.Mode.DATA_TYPE_CALLER_WAITINGTIME_DIST));
		node2.addChild(new StatisticNode(Language.tr("SimStatistic.WaitingTimesDistribution")+" ("+Language.tr("SimStatistic.OnCallBasis")+")",list.toArray(new StatisticViewer[0])));
		list.clear(); for (int i=0;i<data.length;i++) list.add(new StatisticViewerKundenTable(data[i],StatisticViewerKundenTable.Mode.DATA_TYPE_CLIENT_WAITINGTIME_DIST_LONG));
		node2.addChild(new StatisticNode(Language.tr("SimStatistic.WaitingTimesDistribution")+" ["+Language.tr("SimStatistic.long")+"] ("+Language.tr("SimStatistic.OnClientBasis")+")",list.toArray(new StatisticViewer[0])));
		list.clear(); for (int i=0;i<data.length;i++) list.add(new StatisticViewerKundenTable(data[i],StatisticViewerKundenTable.Mode.DATA_TYPE_CALLER_WAITINGTIME_DIST_LONG));
		node2.addChild(new StatisticNode(Language.tr("SimStatistic.WaitingTimesDistribution")+" ["+Language.tr("SimStatistic.long")+"] ("+Language.tr("SimStatistic.OnCallBasis")+")",list.toArray(new StatisticViewer[0])));
		list.clear(); for (int i=0;i<data.length;i++) list.add(new StatisticViewerKundenTable(data[i],StatisticViewerKundenTable.Mode.DATA_TYPE_CLIENT_STAYINGTIME_DIST));
		node2.addChild(new StatisticNode(Language.tr("SimStatistic.ResidenceTimesDistribution")+" ("+Language.tr("SimStatistic.OnClientBasis")+")",list.toArray(new StatisticViewer[0])));
		list.clear(); for (int i=0;i<data.length;i++) list.add(new StatisticViewerKundenTable(data[i],StatisticViewerKundenTable.Mode.DATA_TYPE_CALLER_STAYINGTIME_DIST));
		node2.addChild(new StatisticNode(Language.tr("SimStatistic.ResidenceTimesDistribution")+" ("+Language.tr("SimStatistic.OnCallBasis")+")",list.toArray(new StatisticViewer[0])));
		list.clear(); for (int i=0;i<data.length;i++) list.add(new StatisticViewerKundenTable(data[i],StatisticViewerKundenTable.Mode.DATA_TYPE_CLIENT_STAYINGTIME_DIST_LONG));
		node2.addChild(new StatisticNode(Language.tr("SimStatistic.ResidenceTimesDistribution")+" ["+Language.tr("SimStatistic.long")+"] ("+Language.tr("SimStatistic.OnClientBasis")+")",list.toArray(new StatisticViewer[0])));
		list.clear(); for (int i=0;i<data.length;i++) list.add(new StatisticViewerKundenTable(data[i],StatisticViewerKundenTable.Mode.DATA_TYPE_CALLER_STAYINGTIME_DIST_LONG));
		node2.addChild(new StatisticNode(Language.tr("SimStatistic.ResidenceTimesDistribution")+" ["+Language.tr("SimStatistic.long")+"] ("+Language.tr("SimStatistic.OnCallBasis")+")",list.toArray(new StatisticViewer[0])));
		if (caller.length>1) for (MultiNumbers m : caller) {
			list.clear(); for (int i=0;i<data.length;i++) list.add(new StatisticViewerKundenAgentenLineChart(data[i],StatisticViewerKundenAgentenLineChart.Mode.DATA_TYPE_CLIENT_WAITINGANDSTAYINGTIME_DIST,m.nr[i]));
			node2.addChild(new StatisticNode(m.name+" ("+Language.tr("SimStatistic.OnClientBasis")+")",list.toArray(new StatisticViewer[0]),Language.tr("SimStatistic.Category.WaitingAndResidenceTimesDistribution.PerCallerType")));
			list.clear(); for (int i=0;i<data.length;i++) list.add(new StatisticViewerKundenAgentenLineChart(data[i],StatisticViewerKundenAgentenLineChart.Mode.DATA_TYPE_CALLER_WAITINGANDSTAYINGTIME_DIST,m.nr[i]));
			node2.addChild(new StatisticNode(m.name+" ("+Language.tr("SimStatistic.OnCallBasis")+")",list.toArray(new StatisticViewer[0]),Language.tr("SimStatistic.Category.WaitingAndResidenceTimesDistribution.PerCallerType")));
			list.clear(); for (int i=0;i<data.length;i++) list.add(new StatisticViewerKundenAgentenLineChart(data[i],StatisticViewerKundenAgentenLineChart.Mode.DATA_TYPE_CLIENT_WAITINGANDSTAYINGTIME_DIST_LONG,m.nr[i]));
			node2.addChild(new StatisticNode(m.name+" ["+Language.tr("SimStatistic.long")+"] ("+Language.tr("SimStatistic.OnClientBasis")+")",list.toArray(new StatisticViewer[0]),Language.tr("SimStatistic.Category.WaitingAndResidenceTimesDistribution.PerCallerType")));
			list.clear(); for (int i=0;i<data.length;i++) list.add(new StatisticViewerKundenAgentenLineChart(data[i],StatisticViewerKundenAgentenLineChart.Mode.DATA_TYPE_CALLER_WAITINGANDSTAYINGTIME_DIST_LONG,m.nr[i]));
			node2.addChild(new StatisticNode(m.name+" ["+Language.tr("SimStatistic.long")+"] ("+Language.tr("SimStatistic.OnCallBasis")+")",list.toArray(new StatisticViewer[0]),Language.tr("SimStatistic.Category.WaitingAndResidenceTimesDistribution.PerCallerType")));
		}
		list.clear(); for (int i=0;i<data.length;i++) list.add(new StatisticViewerKundenAgentenLineChart(data[i],StatisticViewerKundenAgentenLineChart.Mode.DATA_TYPE_CLIENT_WAITINGANDSTAYINGTIME_DIST,-1));
		node2.addChild(new StatisticNode(Language.tr("SimStatistic.AllClients")+" ("+Language.tr("SimStatistic.OnClientBasis")+")",list.toArray(new StatisticViewer[0])));
		list.clear(); for (int i=0;i<data.length;i++) list.add(new StatisticViewerKundenAgentenLineChart(data[i],StatisticViewerKundenAgentenLineChart.Mode.DATA_TYPE_CALLER_WAITINGANDSTAYINGTIME_DIST,-1));
		node2.addChild(new StatisticNode(Language.tr("SimStatistic.AllClients")+" ("+Language.tr("SimStatistic.OnCallBasis")+")",list.toArray(new StatisticViewer[0])));
		list.clear(); for (int i=0;i<data.length;i++) list.add(new StatisticViewerKundenAgentenLineChart(data[i],StatisticViewerKundenAgentenLineChart.Mode.DATA_TYPE_CLIENT_WAITINGANDSTAYINGTIME_DIST_LONG,-1));
		node2.addChild(new StatisticNode(Language.tr("SimStatistic.AllClients")+" ["+Language.tr("SimStatistic.long")+"] ("+Language.tr("SimStatistic.OnClientBasis")+")",list.toArray(new StatisticViewer[0])));
		list.clear(); for (int i=0;i<data.length;i++) list.add(new StatisticViewerKundenAgentenLineChart(data[i],StatisticViewerKundenAgentenLineChart.Mode.DATA_TYPE_CALLER_WAITINGANDSTAYINGTIME_DIST_LONG,-1));
		node2.addChild(new StatisticNode(Language.tr("SimStatistic.AllClients")+" ["+Language.tr("SimStatistic.long")+"] ("+Language.tr("SimStatistic.OnCallBasis")+")",list.toArray(new StatisticViewer[0])));

		node.addChild(node2=new StatisticNode(Language.tr("SimStatistic.CancelTimesDistribution"),true,Language.tr("SimStatistic.Category.CancelTimesDistribution")));
		list.clear(); for (int i=0;i<data.length;i++) list.add(new StatisticViewerKundenTable(data[i],StatisticViewerKundenTable.Mode.DATA_TYPE_CLIENT_CANCELTIME_DIST));
		node2.addChild(new StatisticNode(Language.tr("SimStatistic.Overview")+" ("+Language.tr("SimStatistic.OnClientBasis")+")",list.toArray(new StatisticViewer[0])));
		list.clear(); for (int i=0;i<data.length;i++) list.add(new StatisticViewerKundenTable(data[i],StatisticViewerKundenTable.Mode.DATA_TYPE_CALLER_CANCELTIME_DIST));
		node2.addChild(new StatisticNode(Language.tr("SimStatistic.Overview")+" ("+Language.tr("SimStatistic.OnCallBasis")+")",list.toArray(new StatisticViewer[0])));
		list.clear(); for (int i=0;i<data.length;i++) list.add(new StatisticViewerKundenTable(data[i],StatisticViewerKundenTable.Mode.DATA_TYPE_CLIENT_CANCELTIME_DIST_LONG));
		node2.addChild(new StatisticNode(Language.tr("SimStatistic.Overview")+" ["+Language.tr("SimStatistic.long")+"] ("+Language.tr("SimStatistic.OnClientBasis")+")",list.toArray(new StatisticViewer[0])));
		list.clear(); for (int i=0;i<data.length;i++) list.add(new StatisticViewerKundenTable(data[i],StatisticViewerKundenTable.Mode.DATA_TYPE_CALLER_CANCELTIME_DIST_LONG));
		node2.addChild(new StatisticNode(Language.tr("SimStatistic.Overview")+" ["+Language.tr("SimStatistic.long")+"] ("+Language.tr("SimStatistic.OnCallBasis")+")",list.toArray(new StatisticViewer[0])));
		if (caller.length>1) for (MultiNumbers m : caller) {
			list.clear(); for (int i=0;i<data.length;i++) list.add(new StatisticViewerKundenAgentenLineChart(data[i],StatisticViewerKundenAgentenLineChart.Mode.DATA_TYPE_CLIENT_CANCELTIME_DIST,m.nr[i]));
			node2.addChild(new StatisticNode(m.name+" ("+Language.tr("SimStatistic.OnClientBasis")+")",list.toArray(new StatisticViewer[0]),"Abbruchzeitverteilung - pro Kundentyp"));
			list.clear(); for (int i=0;i<data.length;i++) list.add(new StatisticViewerKundenAgentenLineChart(data[i],StatisticViewerKundenAgentenLineChart.Mode.DATA_TYPE_CALLER_CANCELTIME_DIST,m.nr[i]));
			node2.addChild(new StatisticNode(m.name+" ("+Language.tr("SimStatistic.OnCallBasis")+")",list.toArray(new StatisticViewer[0]),"Abbruchzeitverteilung - pro Kundentyp"));
			list.clear(); for (int i=0;i<data.length;i++) list.add(new StatisticViewerKundenAgentenLineChart(data[i],StatisticViewerKundenAgentenLineChart.Mode.DATA_TYPE_CLIENT_CANCELTIME_DIST_LONG,m.nr[i]));
			node2.addChild(new StatisticNode(m.name+" ["+Language.tr("SimStatistic.long")+"] ("+Language.tr("SimStatistic.OnClientBasis")+")",list.toArray(new StatisticViewer[0]),Language.tr("SimStatistic.Category.CancelTimesDistribution.PerCallerType")));
			list.clear(); for (int i=0;i<data.length;i++) list.add(new StatisticViewerKundenAgentenLineChart(data[i],StatisticViewerKundenAgentenLineChart.Mode.DATA_TYPE_CALLER_CANCELTIME_DIST_LONG,m.nr[i]));
			node2.addChild(new StatisticNode(m.name+" ["+Language.tr("SimStatistic.long")+"] ("+Language.tr("SimStatistic.OnCallBasis")+")",list.toArray(new StatisticViewer[0]),Language.tr("SimStatistic.Category.CancelTimesDistribution.PerCallerType")));
		}
		list.clear(); for (int i=0;i<data.length;i++) list.add(new StatisticViewerKundenAgentenLineChart(data[i],StatisticViewerKundenAgentenLineChart.Mode.DATA_TYPE_CLIENT_CANCELTIME_DIST,-1));
		node2.addChild(new StatisticNode(Language.tr("SimStatistic.AllClients")+" ("+Language.tr("SimStatistic.OnClientBasis")+")",list.toArray(new StatisticViewer[0])));
		list.clear(); for (int i=0;i<data.length;i++) list.add(new StatisticViewerKundenAgentenLineChart(data[i],StatisticViewerKundenAgentenLineChart.Mode.DATA_TYPE_CALLER_CANCELTIME_DIST,-1));
		node2.addChild(new StatisticNode(Language.tr("SimStatistic.AllClients")+" ("+Language.tr("SimStatistic.OnCallBasis")+")",list.toArray(new StatisticViewer[0])));
		list.clear(); for (int i=0;i<data.length;i++) list.add(new StatisticViewerKundenAgentenLineChart(data[i],StatisticViewerKundenAgentenLineChart.Mode.DATA_TYPE_CLIENT_CANCELTIME_DIST_LONG,-1));
		node2.addChild(new StatisticNode(Language.tr("SimStatistic.AllClients")+" ["+Language.tr("SimStatistic.long")+"] ("+Language.tr("SimStatistic.OnClientBasis")+")",list.toArray(new StatisticViewer[0])));
		list.clear(); for (int i=0;i<data.length;i++) list.add(new StatisticViewerKundenAgentenLineChart(data[i],StatisticViewerKundenAgentenLineChart.Mode.DATA_TYPE_CALLER_CANCELTIME_DIST_LONG,-1));
		node2.addChild(new StatisticNode(Language.tr("SimStatistic.AllClients")+" ["+Language.tr("SimStatistic.long")+"] ("+Language.tr("SimStatistic.OnCallBasis")+")",list.toArray(new StatisticViewer[0])));

		node.addChild(node2=new StatisticNode(Language.tr("SimStatistic.WaitingTimesOverTheDay"),Language.tr("SimStatistic.Category.WaitingTimesOverTheDay")));
		list.clear(); for (int i=0;i<data.length;i++) list.add(new StatisticViewerKundenIntervalTable(data[i],StatisticViewerKundenIntervalTable.Mode.DATA_TYPE_CLIENTS_WAITINGTIME));
		node2.addChild(new StatisticNode(Language.tr("SimStatistic.WaitingTimes")+" ("+Language.tr("SimStatistic.OnClientBasis")+")",list.toArray(new StatisticViewer[0])));
		list.clear(); for (int i=0;i<data.length;i++) list.add(new StatisticViewerKundenIntervalTable(data[i],StatisticViewerKundenIntervalTable.Mode.DATA_TYPE_CALLS_WAITINGTIME));
		node2.addChild(new StatisticNode(Language.tr("SimStatistic.WaitingTimes")+" ("+Language.tr("SimStatistic.OnCallBasis")+")",list.toArray(new StatisticViewer[0])));
		list.clear(); for (int i=0;i<data.length;i++) list.add(new StatisticViewerKundenIntervalTable(data[i],StatisticViewerKundenIntervalTable.Mode.DATA_TYPE_CLIENTS_STAYINGTIME));
		node2.addChild(new StatisticNode(Language.tr("SimStatistic.ResidenceTimes")+" ("+Language.tr("SimStatistic.OnClientBasis")+")",list.toArray(new StatisticViewer[0])));
		list.clear(); for (int i=0;i<data.length;i++) list.add(new StatisticViewerKundenIntervalTable(data[i],StatisticViewerKundenIntervalTable.Mode.DATA_TYPE_CALLS_STAYINGTIME));
		node2.addChild(new StatisticNode(Language.tr("SimStatistic.ResidenceTimes")+" ("+Language.tr("SimStatistic.OnCallBasis")+")",list.toArray(new StatisticViewer[0])));
		list.clear(); for (int i=0;i<data.length;i++) list.add(new StatisticViewerKundenIntervalTable(data[i],StatisticViewerKundenIntervalTable.Mode.DATA_TYPE_CLIENTS_CANCELTIME));
		node2.addChild(new StatisticNode(Language.tr("SimStatistic.CancelTimes")+" ("+Language.tr("SimStatistic.OnClientBasis")+")",list.toArray(new StatisticViewer[0])));
		list.clear(); for (int i=0;i<data.length;i++) list.add(new StatisticViewerKundenIntervalTable(data[i],StatisticViewerKundenIntervalTable.Mode.DATA_TYPE_CALLS_CANCELTIME));
		node2.addChild(new StatisticNode(Language.tr("SimStatistic.CancelTimes")+" ("+Language.tr("SimStatistic.OnCallBasis")+")",list.toArray(new StatisticViewer[0])));
		if (caller.length>1) for (MultiNumbers m : caller) {
			list.clear(); for (int i=0;i<data.length;i++) list.add(new StatisticViewerKundenAgentenLineChart(data[i],StatisticViewerKundenAgentenLineChart.Mode.DATA_TYPE_CLIENT_WAITING_AND_CANCEL,m.nr[i]));
			node2.addChild(new StatisticNode(m.name+" ("+Language.tr("SimStatistic.OnClientBasis")+")",list.toArray(new StatisticViewer[0]),Language.tr("SimStatistic.Category.WaitingTimesOverTheDay.PerCallerType")));
			list.clear(); for (int i=0;i<data.length;i++) list.add(new StatisticViewerKundenAgentenLineChart(data[i],StatisticViewerKundenAgentenLineChart.Mode.DATA_TYPE_CALLER_WAITING_AND_CANCEL,m.nr[i]));
			node2.addChild(new StatisticNode(m.name+" ("+Language.tr("SimStatistic.OnCallBasis")+")",list.toArray(new StatisticViewer[0]),Language.tr("SimStatistic.Category.WaitingTimesOverTheDay.PerCallerType")));
		}
		list.clear(); for (int i=0;i<data.length;i++) list.add(new StatisticViewerKundenAgentenLineChart(data[i],StatisticViewerKundenAgentenLineChart.Mode.DATA_TYPE_CLIENT_WAITING_AND_CANCEL,-1));
		node2.addChild(new StatisticNode(Language.tr("SimStatistic.AllClients")+" ("+Language.tr("SimStatistic.OnClientBasis")+")",list.toArray(new StatisticViewer[0])));
		list.clear(); for (int i=0;i<data.length;i++) list.add(new StatisticViewerKundenAgentenLineChart(data[i],StatisticViewerKundenAgentenLineChart.Mode.DATA_TYPE_CALLER_WAITING_AND_CANCEL,-1));
		node2.addChild(new StatisticNode(Language.tr("SimStatistic.AllClients")+" ("+Language.tr("SimStatistic.OnCallBasis")+")",list.toArray(new StatisticViewer[0])));

		node.addChild(node2=new StatisticNode(Language.tr("SimStatistic.AccessibilityPerCallerType"),Language.tr("SimStatistic.Category.AccessibilityPerCallerType")));
		list.clear(); for (int i=0;i<data.length;i++) list.add(new StatisticViewerKundenText(data[i],StatisticViewerKundenText.Mode.DATA_TYPE_SUCCESS));
		node2.addChild(new StatisticNode(Language.tr("SimStatistic.Overview"),list.toArray(new StatisticViewer[0])));
		list.clear(); for (int i=0;i<data.length;i++) list.add(new StatisticViewerKundenTable(data[i],StatisticViewerKundenTable.Mode.DATA_TYPE_SUCCESS));
		node2.addChild(new StatisticNode(Language.tr("SimStatistic.Overview"),list.toArray(new StatisticViewer[0])));
		list.clear(); for (int i=0;i<data.length;i++) list.add(new StatisticViewerKundenAgentenBarChart(data[i],StatisticViewerKundenAgentenBarChart.Mode.DATA_TYPE_SUCCESS_BYCLIENT));
		node2.addChild(new StatisticNode(Language.tr("SimStatistic.Overview")+" ("+Language.tr("SimStatistic.OnClientBasis")+")",list.toArray(new StatisticViewer[0])));
		list.clear(); for (int i=0;i<data.length;i++) list.add(new StatisticViewerKundenAgentenBarChart(data[i],StatisticViewerKundenAgentenBarChart.Mode.DATA_TYPE_SUCCESS_BYCALL));
		node2.addChild(new StatisticNode(Language.tr("SimStatistic.Overview")+" ("+Language.tr("SimStatistic.OnCallBasis")+")",list.toArray(new StatisticViewer[0])));

		node.addChild(node2=new StatisticNode(Language.tr("SimStatistic.AccessibilityOverTheDay"),Language.tr("SimStatistic.Category.AccessibilityOverTheDay")));
		list.clear(); for (int i=0;i<data.length;i++) list.add(new StatisticViewerKundenIntervalTable(data[i],StatisticViewerKundenIntervalTable.Mode.DATA_TYPE_CALLS_SUCCESS));
		node2.addChild(new StatisticNode(Language.tr("SimStatistic.SuccessfulCalls")+" ("+Language.tr("SimStatistic.Number")+")",list.toArray(new StatisticViewer[0])));
		list.clear(); for (int i=0;i<data.length;i++) list.add(new StatisticViewerKundenIntervalTable(data[i],StatisticViewerKundenIntervalTable.Mode.DATA_TYPE_CALLS_SUCCESS_PART));
		node2.addChild(new StatisticNode(Language.tr("SimStatistic.SuccessfulCalls")+" ("+Language.tr("SimStatistic.Part")+")",list.toArray(new StatisticViewer[0])));
		list.clear(); for (int i=0;i<data.length;i++) list.add(new StatisticViewerKundenIntervalTable(data[i],StatisticViewerKundenIntervalTable.Mode.DATA_TYPE_CLIENTS_SUCCESS));
		node2.addChild(new StatisticNode(Language.tr("SimStatistic.SuccessfulClients")+" ("+Language.tr("SimStatistic.Number")+")",list.toArray(new StatisticViewer[0])));
		list.clear(); for (int i=0;i<data.length;i++) list.add(new StatisticViewerKundenIntervalTable(data[i],StatisticViewerKundenIntervalTable.Mode.DATA_TYPE_CLIENTS_SUCCESS_PART));
		node2.addChild(new StatisticNode(Language.tr("SimStatistic.SuccessfulClients")+" ("+Language.tr("SimStatistic.Part")+")",list.toArray(new StatisticViewer[0])));
		list.clear(); for (int i=0;i<data.length;i++) list.add(new StatisticViewerKundenIntervalTable(data[i],StatisticViewerKundenIntervalTable.Mode.DATA_TYPE_CALLS_CANCEL));
		node2.addChild(new StatisticNode(Language.tr("SimStatistic.CanceledCalls")+" ("+Language.tr("SimStatistic.Number")+")",list.toArray(new StatisticViewer[0])));
		list.clear(); for (int i=0;i<data.length;i++) list.add(new StatisticViewerKundenIntervalTable(data[i],StatisticViewerKundenIntervalTable.Mode.DATA_TYPE_CALLS_CANCEL_PART));
		node2.addChild(new StatisticNode(Language.tr("SimStatistic.CanceledCalls")+" ("+Language.tr("SimStatistic.Part")+")",list.toArray(new StatisticViewer[0])));
		list.clear(); for (int i=0;i<data.length;i++) list.add(new StatisticViewerKundenIntervalTable(data[i],StatisticViewerKundenIntervalTable.Mode.DATA_TYPE_CLIENTS_CANCEL));
		node2.addChild(new StatisticNode(Language.tr("SimStatistic.CanceledCallers")+" ("+Language.tr("SimStatistic.Number")+")",list.toArray(new StatisticViewer[0])));
		list.clear(); for (int i=0;i<data.length;i++) list.add(new StatisticViewerKundenIntervalTable(data[i],StatisticViewerKundenIntervalTable.Mode.DATA_TYPE_CLIENTS_CANCEL_PART));
		node2.addChild(new StatisticNode(Language.tr("SimStatistic.CanceledCallers")+" ("+Language.tr("SimStatistic.Part")+")",list.toArray(new StatisticViewer[0])));
		if (caller.length>1) for (MultiNumbers m : caller) {
			list.clear(); for (int i=0;i<data.length;i++) list.add(new StatisticViewerKundenAgentenLineChart(data[i],StatisticViewerKundenAgentenLineChart.Mode.DATA_TYPE_CLIENT_SUCCESS,m.nr[i]));
			node2.addChild(new StatisticNode(m.name+" ("+Language.tr("SimStatistic.OnClientBasis")+")",list.toArray(new StatisticViewer[0]),Language.tr("SimStatistic.Category.AccessibilityOverTheDay.PerCallerType")));
			list.clear(); for (int i=0;i<data.length;i++) list.add(new StatisticViewerKundenAgentenLineChart(data[i],StatisticViewerKundenAgentenLineChart.Mode.DATA_TYPE_CALLER_SUCCESS,m.nr[i]));
			node2.addChild(new StatisticNode(m.name+" ("+Language.tr("SimStatistic.OnCallBasis")+")",list.toArray(new StatisticViewer[0]),Language.tr("SimStatistic.Category.AccessibilityOverTheDay.PerCallerType")));
		}
		list.clear(); for (int i=0;i<data.length;i++) list.add(new StatisticViewerKundenAgentenLineChart(data[i],StatisticViewerKundenAgentenLineChart.Mode.DATA_TYPE_CLIENT_SUCCESS,-1));
		node2.addChild(new StatisticNode(Language.tr("SimStatistic.AllClients")+" ("+Language.tr("SimStatistic.OnClientBasis")+")",list.toArray(new StatisticViewer[0])));
		list.clear(); for (int i=0;i<data.length;i++) list.add(new StatisticViewerKundenAgentenLineChart(data[i],StatisticViewerKundenAgentenLineChart.Mode.DATA_TYPE_CALLER_SUCCESS,-1));
		node2.addChild(new StatisticNode(Language.tr("SimStatistic.AllClients")+" ("+Language.tr("SimStatistic.OnCallBasis")+")",list.toArray(new StatisticViewer[0])));

		node.addChild(node2=new StatisticNode(Language.tr("SimStatistic.ServiceLevelPerCallerType"),Language.tr("SimStatistic.Category.ServiceLevelPerCallerType")));
		list.clear(); for (int i=0;i<data.length;i++) list.add(new StatisticViewerKundenText(data[i],StatisticViewerKundenText.Mode.DATA_TYPE_SERVICE_LEVEL));
		node2.addChild(new StatisticNode(Language.tr("SimStatistic.Overview"),list.toArray(new StatisticViewer[0])));
		list.clear(); for (int i=0;i<data.length;i++) list.add(new StatisticViewerKundenTable(data[i],StatisticViewerKundenTable.Mode.DATA_TYPE_SERVICE_LEVEL));
		node2.addChild(new StatisticNode(Language.tr("SimStatistic.Overview"),list.toArray(new StatisticViewer[0])));
		list.clear(); for (int i=0;i<data.length;i++) list.add(new StatisticViewerKundenAgentenBarChart(data[i],StatisticViewerKundenAgentenBarChart.Mode.DATA_TYPE_SERVICELEVEL_BYCLIENT));
		node2.addChild(new StatisticNode(Language.tr("SimStatistic.Overview")+" ("+Language.tr("SimStatistic.CalculatedOn.SuccessfulClients")+")",list.toArray(new StatisticViewer[0])));
		list.clear(); for (int i=0;i<data.length;i++) list.add(new StatisticViewerKundenAgentenBarChart(data[i],StatisticViewerKundenAgentenBarChart.Mode.DATA_TYPE_SERVICELEVEL_BYCALL));
		node2.addChild(new StatisticNode(Language.tr("SimStatistic.Overview")+" ("+Language.tr("SimStatistic.CalculatedOn.SuccessfulCalls")+")",list.toArray(new StatisticViewer[0])));
		list.clear(); for (int i=0;i<data.length;i++) list.add(new StatisticViewerKundenAgentenBarChart(data[i],StatisticViewerKundenAgentenBarChart.Mode.DATA_TYPE_SERVICELEVEL_BYCLIENT_ALL));
		node2.addChild(new StatisticNode(Language.tr("SimStatistic.Overview")+" ("+Language.tr("SimStatistic.CalculatedOn.AllClients")+")",list.toArray(new StatisticViewer[0])));
		list.clear(); for (int i=0;i<data.length;i++) list.add(new StatisticViewerKundenAgentenBarChart(data[i],StatisticViewerKundenAgentenBarChart.Mode.DATA_TYPE_SERVICELEVEL_BYCALL_ALL));
		node2.addChild(new StatisticNode(Language.tr("SimStatistic.Overview")+" ("+Language.tr("SimStatistic.CalculatedOn.AllCalls")+")",list.toArray(new StatisticViewer[0])));

		node.addChild(node2=new StatisticNode(Language.tr("SimStatistic.ServiceLevelOverTheDay"),Language.tr("SimStatistic.Category.ServiceLevelOverTheDay")));
		list.clear(); for (int i=0;i<data.length;i++) list.add(new StatisticViewerKundenIntervalTable(data[i],StatisticViewerKundenIntervalTable.Mode.DATA_TYPE_CALLS_SERVICE_LEVEL));
		node2.addChild(new StatisticNode(Language.tr("SimStatistic.Overview")+" ("+Language.tr("SimStatistic.CalculatedOn.SuccessfulCalls")+")",list.toArray(new StatisticViewer[0])));
		list.clear(); for (int i=0;i<data.length;i++) list.add(new StatisticViewerKundenIntervalTable(data[i],StatisticViewerKundenIntervalTable.Mode.DATA_TYPE_CALLS_SERVICE_LEVEL_ALL));
		node2.addChild(new StatisticNode(Language.tr("SimStatistic.Overview")+" ("+Language.tr("SimStatistic.CalculatedOn.AllCalls")+")",list.toArray(new StatisticViewer[0])));
		list.clear(); for (int i=0;i<data.length;i++) list.add(new StatisticViewerKundenIntervalTable(data[i],StatisticViewerKundenIntervalTable.Mode.DATA_TYPE_CLIENTS_SERVICE_LEVEL));
		node2.addChild(new StatisticNode(Language.tr("SimStatistic.Overview")+" ("+Language.tr("SimStatistic.CalculatedOn.SuccessfulClients")+")",list.toArray(new StatisticViewer[0])));
		list.clear(); for (int i=0;i<data.length;i++) list.add(new StatisticViewerKundenIntervalTable(data[i],StatisticViewerKundenIntervalTable.Mode.DATA_TYPE_CLIENTS_SERVICE_LEVEL_ALL));
		node2.addChild(new StatisticNode(Language.tr("SimStatistic.Overview")+" ("+Language.tr("SimStatistic.CalculatedOn.AllClients")+")",list.toArray(new StatisticViewer[0])));
		if (caller.length>1) for (MultiNumbers m : caller) {
			list.clear(); for (int i=0;i<data.length;i++) list.add(new StatisticViewerKundenAgentenLineChart(data[i],StatisticViewerKundenAgentenLineChart.Mode.DATA_TYPE_CLIENT_SERVICE_LEVEL,m.nr[i]));
			node2.addChild(new StatisticNode(m.name+" ("+Language.tr("SimStatistic.CalculatedOn.SuccessfulClients")+")",list.toArray(new StatisticViewer[0]),Language.tr("SimStatistic.Category.ServiceLevelOverTheDay.PerCallerType")));
			list.clear(); for (int i=0;i<data.length;i++) list.add(new StatisticViewerKundenAgentenLineChart(data[i],StatisticViewerKundenAgentenLineChart.Mode.DATA_TYPE_CALLER_SERVICE_LEVEL,m.nr[i]));
			node2.addChild(new StatisticNode(m.name+" ("+Language.tr("SimStatistic.CalculatedOn.SuccessfulCalls")+")",list.toArray(new StatisticViewer[0]),Language.tr("SimStatistic.Category.ServiceLevelOverTheDay.PerCallerType")));
			list.clear(); for (int i=0;i<data.length;i++) list.add(new StatisticViewerKundenAgentenLineChart(data[i],StatisticViewerKundenAgentenLineChart.Mode.DATA_TYPE_CLIENT_SERVICE_LEVEL_ALL,m.nr[i]));
			node2.addChild(new StatisticNode(m.name+" ("+Language.tr("SimStatistic.CalculatedOn.AllClients")+")",list.toArray(new StatisticViewer[0]),Language.tr("SimStatistic.Category.ServiceLevelOverTheDay.PerCallerType")));
			list.clear(); for (int i=0;i<data.length;i++) list.add(new StatisticViewerKundenAgentenLineChart(data[i],StatisticViewerKundenAgentenLineChart.Mode.DATA_TYPE_CALLER_SERVICE_LEVEL_ALL,m.nr[i]));
			node2.addChild(new StatisticNode(m.name+" ("+Language.tr("SimStatistic.CalculatedOn.AllCalls")+")",list.toArray(new StatisticViewer[0]),Language.tr("SimStatistic.Category.ServiceLevelOverTheDay.PerCallerType")));
		}
		list.clear(); for (int i=0;i<data.length;i++) list.add(new StatisticViewerKundenAgentenLineChart(data[i],StatisticViewerKundenAgentenLineChart.Mode.DATA_TYPE_CLIENT_SERVICE_LEVEL,-1));
		node2.addChild(new StatisticNode(Language.tr("SimStatistic.AllClients")+" ("+Language.tr("SimStatistic.CalculatedOn.SuccessfulClients")+")",list.toArray(new StatisticViewer[0])));
		list.clear(); for (int i=0;i<data.length;i++) list.add(new StatisticViewerKundenAgentenLineChart(data[i],StatisticViewerKundenAgentenLineChart.Mode.DATA_TYPE_CALLER_SERVICE_LEVEL,-1));
		node2.addChild(new StatisticNode(Language.tr("SimStatistic.AllClients")+" ("+Language.tr("SimStatistic.CalculatedOn.SuccessfulCalls")+")",list.toArray(new StatisticViewer[0])));
		list.clear(); for (int i=0;i<data.length;i++) list.add(new StatisticViewerKundenAgentenLineChart(data[i],StatisticViewerKundenAgentenLineChart.Mode.DATA_TYPE_CLIENT_SERVICE_LEVEL_ALL,-1));
		node2.addChild(new StatisticNode(Language.tr("SimStatistic.AllClients")+" ("+Language.tr("SimStatistic.CalculatedOn.AllClients")+")",list.toArray(new StatisticViewer[0])));
		list.clear(); for (int i=0;i<data.length;i++) list.add(new StatisticViewerKundenAgentenLineChart(data[i],StatisticViewerKundenAgentenLineChart.Mode.DATA_TYPE_CALLER_SERVICE_LEVEL_ALL,-1));
		node2.addChild(new StatisticNode(Language.tr("SimStatistic.AllClients")+" ("+Language.tr("SimStatistic.CalculatedOn.AllCalls")+")",list.toArray(new StatisticViewer[0])));

		/* Agentendaten */
		root.addChild(node=new StatisticNode(Language.tr("SimStatistic.ActiveAgents")));

		node.addChild(node2=new StatisticNode(Language.tr("SimStatistic.WorkLoadPerCallcenter"),Language.tr("SimStatistic.Category.WorkLoadPerCallcenter")));
		list.clear(); for (int i=0;i<data.length;i++) list.add(new StatisticViewerAgentenText(data[i],StatisticViewerAgentenText.SortType.SORT_BY_CALLCENTER));
		node2.addChild(new StatisticNode(Language.tr("SimStatistic.Overview"),list.toArray(new StatisticViewer[0])));
		list.clear(); for (int i=0;i<data.length;i++) list.add(new StatisticViewerAgentenTable(data[i],StatisticViewerAgentenTable.SortType.SORT_BY_CALLCENTER));
		node2.addChild(new StatisticNode(Language.tr("SimStatistic.Overview"),list.toArray(new StatisticViewer[0])));
		list.clear(); for (int i=0;i<data.length;i++) list.add(new StatisticViewerKundenAgentenBarChart(data[i],StatisticViewerKundenAgentenBarChart.Mode.DATA_TYPE_WORKING_TIME));
		node2.addChild(new StatisticNode(Language.tr("SimStatistic.Overview"),list.toArray(new StatisticViewer[0])));
		if (agentsCallcenter.length>1) for (MultiNumbers m: agentsCallcenter) {
			list.clear(); for (int i=0;i<data.length;i++) list.add(new StatisticViewerAuslastungPieChart(data[i],StatisticViewerAuslastungPieChart.Mode.DATA_TYPE_CALLCENTER,m.nr[i]));
			node2.addChild(new StatisticNode(m.name,list.toArray(new StatisticViewer[0]),Language.tr("SimStatistic.Category.WorkLoadPerCallcenter.SingleGraphics")));
		}
		list.clear(); for (int i=0;i<data.length;i++) list.add(new StatisticViewerAuslastungPieChart(data[i],StatisticViewerAuslastungPieChart.Mode.DATA_TYPE_ALL,-1));
		node2.addChild(new StatisticNode(Language.tr("SimStatistic.AllCallcenter"),list.toArray(new StatisticViewer[0])));
		if (agentsCallcenter.length>1) for (MultiNumbers m: agentsCallcenter) {
			list.clear(); for (int i=0;i<data.length;i++) list.add(new StatisticViewerAuslastungPieChart(data[i],StatisticViewerAuslastungPieChart.Mode.DATA_TYPE_CALLCENTER_FULL,m.nr[i]));
			node2.addChild(new StatisticNode(m.name+" ("+Language.tr("SimStatistic.Detailed")+")",list.toArray(new StatisticViewer[0]),Language.tr("SimStatistic.Category.WorkLoadPerCallcenter.SingleGraphics")));
		}
		list.clear(); for (int i=0;i<data.length;i++) list.add(new StatisticViewerAuslastungPieChart(data[i],StatisticViewerAuslastungPieChart.Mode.DATA_TYPE_ALL_FULL,-1));
		node2.addChild(new StatisticNode(Language.tr("SimStatistic.AllCallcenter")+" ("+Language.tr("SimStatistic.Detailed")+")",list.toArray(new StatisticViewer[0])));

		node.addChild(node2=new StatisticNode(Language.tr("SimStatistic.WorkLoadOverTheDay"),Language.tr("SimStatistic.Category.WorkLoadOverTheDay")));

		list.clear(); for (int i=0;i<data.length;i++) list.add(new StatisticViewerAgentenIntervalTable(data[i],StatisticViewerAgentenIntervalTable.Mode.DATA_TYPE_AUSLASTUNG));
		node2.addChild(new StatisticNode(Language.tr("SimStatistic.Overview"),list.toArray(new StatisticViewer[0])));
		list.clear(); for (int i=0;i<data.length;i++) list.add(new StatisticViewerAgentenIntervalTable(data[i],StatisticViewerAgentenIntervalTable.Mode.DATA_TYPE_AUSLASTUNG_DETAILS));
		node2.addChild(new StatisticNode(Language.tr("SimStatistic.Overview")+" ("+Language.tr("SimStatistic.Detailed")+")",list.toArray(new StatisticViewer[0])));

		list.clear(); for (int i=0;i<data.length;i++) list.add(new StatisticViewerAgentenIntervalTable(data[i],StatisticViewerAgentenIntervalTable.Mode.DATA_TYPE_CALLS));
		node2.addChild(new StatisticNode(Language.tr("SimStatistic.NumberOfCalls"),list.toArray(new StatisticViewer[0])));
		if (agentsCallcenter.length>1) for (MultiNumbers m: agentsCallcenter) {
			list.clear(); for (int i=0;i<data.length;i++) list.add(new StatisticViewerKundenAgentenLineChart(data[i],StatisticViewerKundenAgentenLineChart.Mode.DATA_TYPE_SERVICETIMEPART,m.nr[i]));
			node2.addChild(new StatisticNode(m.name,list.toArray(new StatisticViewer[0]),Language.tr("SimStatistic.Category.WorkLoadOverTheDay.SingleGraphics")));
		}
		list.clear(); for (int i=0;i<data.length;i++) list.add(new StatisticViewerKundenAgentenLineChart(data[i],StatisticViewerKundenAgentenLineChart.Mode.DATA_TYPE_SERVICETIMEPART,-1));
		node2.addChild(new StatisticNode(Language.tr("SimStatistic.AllCallcenter"),list.toArray(new StatisticViewer[0])));
		if (agentsCallcenter.length>1) for (MultiNumbers m: agentsCallcenter) {
			list.clear(); for (int i=0;i<data.length;i++) list.add(new StatisticViewerKundenAgentenLineChart(data[i],StatisticViewerKundenAgentenLineChart.Mode.DATA_TYPE_SERVICETIMEPART_FULL,m.nr[i]));
			node2.addChild(new StatisticNode(m.name+" ("+Language.tr("SimStatistic.Detailed")+")",list.toArray(new StatisticViewer[0]),Language.tr("SimStatistic.Category.WorkLoadOverTheDay.SingleGraphics")));
		}
		list.clear(); for (int i=0;i<data.length;i++) list.add(new StatisticViewerKundenAgentenLineChart(data[i],StatisticViewerKundenAgentenLineChart.Mode.DATA_TYPE_SERVICETIMEPART_FULL,-1));
		node2.addChild(new StatisticNode(Language.tr("SimStatistic.AllCallcenter")+" ("+Language.tr("SimStatistic.Detailed")+")",list.toArray(new StatisticViewer[0])));
		list.clear(); for (int i=0;i<data.length;i++) list.add(new StatisticViewerAgentenIntervalTable(data[i],StatisticViewerAgentenIntervalTable.Mode.DATA_TYPE_QUEUE));
		node2.addChild(new StatisticNode(Language.tr("SimStatistic.Queue"),list.toArray(new StatisticViewer[0])));
		list.clear(); for (int i=0;i<data.length;i++) list.add(new StatisticViewerKundenAgentenLineChart(data[i],StatisticViewerKundenAgentenLineChart.Mode.DATA_TYPE_QUEUE,-1));
		node2.addChild(new StatisticNode(Language.tr("SimStatistic.Queue"),list.toArray(new StatisticViewer[0])));

		node.addChild(node2=new StatisticNode(Language.tr("SimStatistic.IdleTimeOverTheDay"),Language.tr("SimStatistic.Category.IdleTimeOverTheDay")));
		list.clear(); for (int i=0;i<data.length;i++) list.add(new StatisticViewerAgentenIntervalTable(data[i],StatisticViewerAgentenIntervalTable.Mode.DATA_TYPE_BEREITZEIT));
		node2.addChild(new StatisticNode(Language.tr("SimStatistic.Overview"),list.toArray(new StatisticViewer[0])));
		if (agentsCallcenter.length>1) for (MultiNumbers m: agentsCallcenter) {
			list.clear(); for (int i=0;i<data.length;i++) list.add(new StatisticViewerKundenAgentenLineChart(data[i],StatisticViewerKundenAgentenLineChart.Mode.DATA_TYPE_FREETIME,m.nr[i]));
			node2.addChild(new StatisticNode(m.name,list.toArray(new StatisticViewer[0]),Language.tr("SimStatistic.Category.IdleTimeOverTheDay.SingleGraphics")));
		}
		list.clear(); for (int i=0;i<data.length;i++) list.add(new StatisticViewerKundenAgentenLineChart(data[i],StatisticViewerKundenAgentenLineChart.Mode.DATA_TYPE_FREETIME,-1));
		node2.addChild(new StatisticNode(Language.tr("SimStatistic.AllCallcenter"),list.toArray(new StatisticViewer[0])));

		node.addChild(node2=new StatisticNode(Language.tr("SimStatistic.WorkLoadPerSkillLevel"),true,Language.tr("SimStatistic.Category.WorkLoadPerSkillLevel")));
		list.clear(); for (int i=0;i<data.length;i++) list.add(new StatisticViewerAgentenText(data[i],StatisticViewerAgentenText.SortType.SORT_BY_SKILL_LEVEL));
		node2.addChild(new StatisticNode(Language.tr("SimStatistic.Overview"),list.toArray(new StatisticViewer[0])));
		list.clear(); for (int i=0;i<data.length;i++) list.add(new StatisticViewerAgentenTable(data[i],StatisticViewerAgentenTable.SortType.SORT_BY_SKILL_LEVEL));
		node2.addChild(new StatisticNode(Language.tr("SimStatistic.Overview"),list.toArray(new StatisticViewer[0])));
		list.clear(); for (int i=0;i<data.length;i++) list.add(new StatisticViewerAgentenIntervalTable(data[i],StatisticViewerAgentenIntervalTable.Mode.DATA_TYPE_SKILL_LEVEL_DETAILS));
		node2.addChild(new StatisticNode(Language.tr("SimStatistic.Overview")+" ("+Language.tr("SimStatistic.Detailed")+")",list.toArray(new StatisticViewer[0])));
		list.clear(); for (int i=0;i<data.length;i++) list.add(new StatisticViewerKundenAgentenBarChart(data[i],StatisticViewerKundenAgentenBarChart.Mode.DATA_TYPE_WORKING_TIME_BY_SKILL));
		node2.addChild(new StatisticNode(Language.tr("SimStatistic.Overview"),list.toArray(new StatisticViewer[0])));
		if (agentsSkills.length>1) for (MultiNumbers m: agentsSkills) {
			list.clear(); for (int i=0;i<data.length;i++) list.add(new StatisticViewerAuslastungPieChart(data[i],StatisticViewerAuslastungPieChart.Mode.DATA_TYPE_SKILL_LEVEL,m.nr[i]));
			node2.addChild(new StatisticNode(m.name,list.toArray(new StatisticViewer[0]),Language.tr("SimStatistic.Category.WorkLoadPerSkillLevel.SingleGraphics")));
		}
		if (agentsSkills.length>1) for (MultiNumbers m: agentsSkills) {
			list.clear(); for (int i=0;i<data.length;i++) list.add(new StatisticViewerAuslastungPieChart(data[i],StatisticViewerAuslastungPieChart.Mode.DATA_TYPE_SKILL_LEVEL_FULL,m.nr[i]));
			node2.addChild(new StatisticNode(m.name+" ("+Language.tr("SimStatistic.Detailed")+")",list.toArray(new StatisticViewer[0]),Language.tr("SimStatistic.Category.WorkLoadPerSkillLevel.SingleGraphics")));
		}

		/* Agentendaten */
		root.addChild(node=new StatisticNode(Language.tr("SimStatistic.AgentsOnModelBasis")));
		list.clear(); for (int i=0;i<data.length;i++) list.add(new StatisticViewerAgentenIntervalTable(data[i],StatisticViewerAgentenIntervalTable.Mode.DATA_MODEL_SIM_AGENTS));
		node.addChild(node2=new StatisticNode(Language.tr("SimStatistic.AgentsOnModelBasis.AgentsInSimulation"),list.toArray(new StatisticViewer[0]),Language.tr("SimStatistic.Category.AgentsOnModelBasis")));
		list.clear(); for (int i=0;i<data.length;i++) list.add(new StatisticViewerAgentenIntervalTable(data[i],StatisticViewerAgentenIntervalTable.Mode.DATA_MODEL_MODEL_AGENTS));
		node.addChild(node2=new StatisticNode(Language.tr("SimStatistic.AgentsOnModelBasis.AgentsInModel"),list.toArray(new StatisticViewer[0]),Language.tr("SimStatistic.Category.AgentsOnModelBasis")));
		list.clear(); for (int i=0;i<data.length;i++) list.add(new StatisticViewerAgentenIntervalTable(data[i],StatisticViewerAgentenIntervalTable.Mode.DATA_MODEL_FULL_AGENTS));
		node.addChild(node2=new StatisticNode(Language.tr("SimStatistic.AgentsOnModelBasis.AgentsWithAddition"),list.toArray(new StatisticViewer[0]),Language.tr("SimStatistic.Category.AgentsOnModelBasis")));

		/* Erlang-C Vergleich */
		root.addChild(node=new StatisticNode(Language.tr("SimStatistic.ErlangCComparison")));
		list.clear(); for (int i=0;i<data.length;i++) list.add(new StatisticViewerErlangCIntervalTable(data[i],StatisticViewerErlangCIntervalTable.Mode.DATA_SUCCESS));
		node.addChild(node2=new StatisticNode(Language.tr("SimStatistic.ErlangCComparison.Success"),list.toArray(new StatisticViewer[0]),Language.tr("SimStatistic.Category.ErlangCComparison")));
		list.clear(); for (int i=0;i<data.length;i++) list.add(new StatisticViewerErlangCLineChart(data[i],StatisticViewerErlangCLineChart.Mode.DATA_SUCCESS));
		node.addChild(node2=new StatisticNode(Language.tr("SimStatistic.ErlangCComparison.Success"),list.toArray(new StatisticViewer[0]),Language.tr("SimStatistic.Category.ErlangCComparison")));
		list.clear(); for (int i=0;i<data.length;i++) list.add(new StatisticViewerErlangCIntervalTable(data[i],StatisticViewerErlangCIntervalTable.Mode.DATA_WAITING_TIME));
		node.addChild(node2=new StatisticNode(Language.tr("SimStatistic.ErlangCComparison.WaitingTime"),list.toArray(new StatisticViewer[0]),Language.tr("SimStatistic.Category.ErlangCComparison")));
		list.clear(); for (int i=0;i<data.length;i++) list.add(new StatisticViewerErlangCLineChart(data[i],StatisticViewerErlangCLineChart.Mode.DATA_WAITING_TIME));
		node.addChild(node2=new StatisticNode(Language.tr("SimStatistic.ErlangCComparison.WaitingTime"),list.toArray(new StatisticViewer[0]),Language.tr("SimStatistic.Category.ErlangCComparison")));
		list.clear(); for (int i=0;i<data.length;i++) list.add(new StatisticViewerErlangCIntervalTable(data[i],StatisticViewerErlangCIntervalTable.Mode.DATA_SERVICE_LEVEL));
		node.addChild(node2=new StatisticNode(Language.tr("SimStatistic.ErlangCComparison.ServiceLevel"),list.toArray(new StatisticViewer[0]),Language.tr("SimStatistic.Category.ErlangCComparison")));
		list.clear(); for (int i=0;i<data.length;i++) list.add(new StatisticViewerErlangCLineChart(data[i],StatisticViewerErlangCLineChart.Mode.DATA_SERVICE_LEVEL));
		node.addChild(node2=new StatisticNode(Language.tr("SimStatistic.ErlangCComparison.ServiceLevel"),list.toArray(new StatisticViewer[0]),Language.tr("SimStatistic.Category.ErlangCComparison")));

		/* Kosten */
		list.clear(); for (int i=0;i<data.length;i++) list.add(new StatisticViewerKostenText(data[i]));
		root.addChild(new StatisticNode(Language.tr("SimStatistic.Costs"),list.toArray(new StatisticViewer[0]),Language.tr("SimStatistic.Costs")));

		/* Systemdaten */
		list.clear(); for (int i=0;i<data.length;i++) list.add(new StatisticViewerTextInformation(data[i],StatisticViewerTextInformation.Mode.MODE_SYSTEM_INFORMATION));
		root.addChild(new StatisticNode(Language.tr("SimStatistic.SystemData"),list.toArray(new StatisticViewer[0]),Language.tr("SimStatistic.Category.General")));

	}

	@Override
	protected final void loadFilterDialogSettings() {
		setHiddenIDsFromSetupString(SetupData.getSetup().statisticTreeFilter);
	}

	@Override
	protected final void saveFilterDialogSettings() {
		SetupData setup=SetupData.getSetup();
		setup.statisticTreeFilter=getHiddenIDsSetupString();
		setup.saveSetupWithWarning(this);
	}

	@Override
	protected String getReportSettings() {
		return SetupData.getSetup().simReportTreeFilter;
	}

	@Override
	protected void setReportSettings(String settings) {
		SetupData setup=SetupData.getSetup();
		setup.simReportTreeFilter=settings;
		setup.saveSetupWithWarning(this);
	}

	/**
	 * Setzt ein Statistik-Objekt für die Anzeige und prüft dabei die Modellversion
	 * @param statistic	Neues, anzuzeigendes Statistik-Objekt
	 * @return	Gibt im Erfolgsfall <code>null</code> zurück, sonst eine Fehlermeldung.
	 */
	public final String loadStatistic(Statistics statistic) {
		if (statistic.editModel!=null && VersionConst.isNewerVersion(statistic.editModel.version)) {
			MsgBox.warning(this,Language.tr("Statistic.NewVersionWarning.Title"),Language.tr("Statistic.NewVersionWarning.Info"));
		}

		setStatistic(new Statistics[]{statistic});

		return null;
	}

	/**
	 * Lädt die Statistikdaten aus einer Datei
	 * @param file	Datei, aus der die Statistikdaten geladen werden sollen. Wird hier <code>null</code> übergeben, so wird ein Dateiauswahl-Dialog angezeigt.
	 * @return	Gibt im Erfolgsfall <code>null</code> zurück, sonst eine Fehlermeldung.
	 */
	public final String loadStatistic(File file) {
		if (statisticData!=null && statisticData.length!=1) return null;

		if (file==null) {
			file=XMLTools.showLoadDialog(getParent(),Language.tr("MainToolbar.LoadStatistic"));
			if (file==null) return null;
		}

		Statistics newData=new Statistics(null,null,0,0);
		String s=newData.loadFromFile(file); if (s!=null) return s;

		return loadStatistic(newData);
	}

	/**
	 * Lädt die Statistikdaten aus einem XML-Element
	 * @param root	XML-Wurzelelement, aus dem die Statistikdaten geladen werden sollen.
	 * @return	Gibt im Erfolgsfall <code>null</code> zurück, sonst eine Fehlermeldung.
	 */
	public final String loadStatistic(final Element root) {
		if (statisticData!=null && statisticData.length!=1) return null;

		Statistics newData=new Statistics(null,null,0,0);
		String s=newData.loadFromXML(root); if (s!=null) return s;

		return loadStatistic(newData);
	}

	/**
	 * Speichert die Statistikdaten in einer Datei
	 * @param file	Datei, in die die Statistikdaten geschrieben werden sollen. Wird hier <code>null</code> übergeben, so wird ein Dateiauswahl-Dialog angezeigt.
	 * @return	Gibt im Erfolgsfall <code>null</code> zurück, sonst eine Fehlermeldung.
	 */
	public final String saveStatistic(File file) {
		if (statisticData==null || statisticData.length==0 || statisticData[0]==null) return null;

		if (file==null) {
			file=XMLTools.showSaveDialog(getParent(),Language.tr("MainToolbar.SaveStatistic"),null,new String[]{Language.tr("FileType.HTML")+" (*.html, *.htm)",Language.tr("FileType.Word")+" (*.docx)"},new String[]{"html;htm","docx"});
			if (file==null) return null;
		}

		if (file.exists()) {
			if (!MsgBox.confirmOverwrite(this,file)) return null;
		}

		final String ERROR_MESSAGE=String.format(Language.tr("Window.SaveStatisticsError.Info"),file.toString());

		String fileName=file.getName().toUpperCase();

		if (fileName.endsWith(".HTML") || fileName.endsWith(".HTM")) {
			URL iconWebApp=Images.STATISTICS_SHOW_WEBVIEWER.getURL();
			URL iconStatic=Images.STATISTICS_SAVE.getURL();
			switch (StatisticWebAppWriter.showSelectDialog(getParent(),iconWebApp,iconStatic)) {
			case 0:
				StatisticWebAppWriter writer=new StatisticWebAppWriter(statisticData[0]);
				return writer.saveToFile(file)?null:ERROR_MESSAGE;
			case 1:
				return runReportGeneratorHTML(file,true,true)?null:ERROR_MESSAGE;
			default:
				return null;
			}
		}
		if (fileName.endsWith(".DOCX")) return runReportGeneratorDOCX(file,true)?null:ERROR_MESSAGE;

		if (!statisticData[0].saveToFile(file)) return ERROR_MESSAGE;

		setDataFileName(file.toString());

		return null;
	}
}
