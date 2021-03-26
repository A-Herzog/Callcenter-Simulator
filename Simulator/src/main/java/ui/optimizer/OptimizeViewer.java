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
package ui.optimizer;

import java.awt.BorderLayout;
import java.awt.Window;
import java.io.File;
import java.io.Serializable;
import java.net.URL;

import javax.swing.JButton;
import javax.swing.JOptionPane;

import language.Language;
import simulator.Statistics;
import systemtools.MsgBox;
import systemtools.statistics.StatisticNode;
import tools.SetupData;
import ui.HelpLink;
import ui.compare.ComparePanelDialog;
import ui.editor.BaseEditDialog;
import ui.editor.CallcenterModelEditorPanelDialog;
import ui.images.Images;
import ui.model.CallcenterModel;
import ui.specialpanels.ViewerWithLoadModelCallback;
import ui.statistic.core.StatisticBasePanel;
import ui.statistic.optimizer.StatisticViewerOptimizeSetup;
import ui.statistic.optimizer.StatisticViewerOptimizerBarChart;
import ui.statistic.optimizer.StatisticViewerOptimizerLineChart;
import ui.statistic.optimizer.StatisticViewerOptimizerTable;
import xml.XMLTools;

/**
 * Betrachter für die Optimierungsergebnisse
 * @author Alexander Herzog
 * @version 1.0
 */
public final class OptimizeViewer extends ViewerWithLoadModelCallback {
	/**
	 * Serialisierungs-ID der Klasse
	 * @see Serializable
	 */
	private static final long serialVersionUID = -1862842076679854416L;

	/** Übergeordnetes Fenster */
	private final Window owner;

	/**
	 * Objekt vom Typ <code>OptimizeData</code>, aus dem die Ergebnisse geladen werden sollen.
	 * @see #loadResults(OptimizeData)
	 */
	private OptimizeData results;

	/**
	 * Wurden die Ergebnisse bereits gespeichert?
	 * @see #closeCheck()
	 */
	private boolean resultsSaved;

	/**
	 * Panel zur Anzeige der Statistikdaten
	 */
	private OptimizerStatisticPanel statistic;

	/** Verknüpfung mit der Online-Hilfe */
	private final HelpLink helpLink;

	/**
	 * Konstruktor der Klasse <code>OptimizeViewer</code>
	 * @param owner	Übergeordnetes Fenster
	 * @param helpLink Verknüpfung mit der Online-Hilfe
	 */
	public OptimizeViewer(final Window owner, final HelpLink helpLink) {
		super(null,helpLink.pageOptimizeViewer);
		this.owner=owner;
		this.helpLink=helpLink;

		/* Statistik */
		setLayout(new BorderLayout());
		add(statistic=new OptimizerStatisticPanel(Language.tr("OptimizeResults.Title"),Images.OPTIMIZER_RESULTS_STATISTICS.getURLs()[0],true,helpLink.pageOptimizeViewerModal,helpLink,null,null),BorderLayout.CENTER);

		/* Fußzeile */
		addFooter(null,null,null);

		JButton button;

		button=addFooterButton(Language.tr("OptimizeResults.Button.RunResults"));
		button.setToolTipText(Language.tr("OptimizeResults.Button.RunResults.Info"));
		button.setIcon(Images.OPTIMIZER_RESULTS.getIcon());

		button=addFooterButton(Language.tr("OptimizeResults.Button.LastRunResult"));
		button.setToolTipText(Language.tr("OptimizeResults.Button.LastRunResult.Info"));
		button.setIcon(Images.OPTIMIZER_RESULTS_COMPARE_LAST.getIcon());

		button=addFooterButton(Language.tr("OptimizeResults.Button.CompareFirstLast"));
		button.setToolTipText(Language.tr("OptimizeResults.Button.CompareFirstLast.Info"));
		button.setIcon(Images.OPTIMIZER_RESULTS_COMPARE_FIRST_LAST.getIcon());

		button=addFooterButton(Language.tr("OptimizeResults.Button.SaveResults"));
		button.setToolTipText(Language.tr("OptimizeResults.Button.SaveResults.Info"));
		button.setIcon(Images.OPTIMIZER_SETTINGS_SAVE.getIcon());
	}

	/**
	 * Lädt die Ergebnisse direkt aus einem Optimierungs-Ergebnisse-Objekt
	 * und unterstellt dabei, dass die Ergebnisse noch nicht gespeichert wurden.
	 * @param results	Objekt vom Typ <code>OptimizeData</code>, aus dem die Ergebnisse geladen werden sollen.
	 */
	public void loadResults(final OptimizeData results) {
		this.results=results;
		resultsSaved=false;

		StatisticNode root=new StatisticNode();
		if (results!=null) addNodes(root);
		statistic.setStatisticData(root);
	}

	/**
	 * Lädt die Optimierungsergebnisse aus einer Datei
	 * @param file	Datei, aus der die Optimierungsergebnisse geladen werden sollen
	 * @return	Gibt <code>null</code> zurück, wenn die Daten korrekt geladen werden konnten. Andernfalls wird eine Fehlermeldung zurückgegeben.
	 */
	public String loadResults(final File file) {
		OptimizeData data=new OptimizeData();
		String s=data.loadFromFile(file);
		if (s!=null) return s;
		loadResults(data);
		resultsSaved=true;
		return null;
	}

	/**
	 * Fügt die Einträge zu den Optimierungsergebnissen zu der Baumstruktur in
	 * der Statistikansicht hinzu.
	 * @param root	Wurzelelement für die Baumstruktur
	 * @see #loadResults(OptimizeData)
	 */
	private void addNodes(final StatisticNode root) {
		StatisticNode node, node2, node3;

		/* Allgemeine Daten */
		root.addChild(new StatisticNode(Language.tr("OptimizeResults.OptimizerSettings"),new StatisticViewerOptimizeSetup(results,StatisticViewerOptimizeSetup.Mode.DATA_SETUP)));

		/* Kunden-Daten */
		root.addChild(node=new StatisticNode(Language.tr("SimStatistic.Clients")));

		node.addChild(node2=new StatisticNode(Language.tr("SimStatistic.NumberOfCallers"),true,Language.tr("SimStatistic.NumberOfCallers")));
		node2.addChild(new StatisticNode(Language.tr("SimStatistic.Overview"),new StatisticViewerOptimizerTable(results,StatisticViewerOptimizerTable.Mode.DATA_TYPE_CALLERS)));
		if (results.data.get(0).kundenProTyp.length>1) for (int i=0;i<results.data.get(0).kundenProTyp.length;i++)
			node2.addChild(new StatisticNode(results.data.get(0).kundenProTyp[i].name,new StatisticViewerOptimizerLineChart(results,StatisticViewerOptimizerLineChart.Mode.DATA_TYPE_CALLERS,i),Language.tr("SimStatistic.NumberOfCallers")+" - "+Language.tr("OptimizeResults.PerClientType")));
		node2.addChild(new StatisticNode(Language.tr("Editor.Overview.Caller.All"),new StatisticViewerOptimizerLineChart(results,StatisticViewerOptimizerLineChart.Mode.DATA_TYPE_CALLERS,-1)));

		node2.addChild(node3=new StatisticNode(Language.tr("SimStatistic.NumberOfCallers.Change"),true,Language.tr("SimStatistic.NumberOfCallers.Change")));
		if (results.data.get(0).kundenProTyp.length>1) for (int i=0;i<results.data.get(0).kundenProTyp.length;i++)
			node3.addChild(new StatisticNode(results.data.get(0).kundenProTyp[i].name,new StatisticViewerOptimizerBarChart(results,StatisticViewerOptimizerBarChart.DATA_TYPE_CALLERS,i),Language.tr("SimStatistic.NumberOfCallers")+" - "+Language.tr("SimStatistic.NumberOfCallers.Change")+" - "+Language.tr("OptimizeResults.PerClientType")));
		node3.addChild(new StatisticNode(Language.tr("Editor.Overview.Caller.All"),new StatisticViewerOptimizerBarChart(results,StatisticViewerOptimizerBarChart.DATA_TYPE_CALLERS,-1)));

		node.addChild(node2=new StatisticNode(Language.tr("SimStatistic.Accessibility"),Language.tr("SimStatistic.Accessibility")));
		node2.addChild(new StatisticNode(Language.tr("SimStatistic.Overview"),new StatisticViewerOptimizerTable(results,StatisticViewerOptimizerTable.Mode.DATA_TYPE_SUCCESS)));
		if (results.data.get(0).kundenProTyp.length>1) for (int i=0;i<results.data.get(0).kundenProTyp.length;i++)
			node2.addChild(new StatisticNode(results.data.get(0).kundenProTyp[i].name,new StatisticViewerOptimizerLineChart(results,StatisticViewerOptimizerLineChart.Mode.DATA_TYPE_SUCCESS,i),Language.tr("SimStatistic.Accessibility")+" - "+Language.tr("OptimizeResults.PerClientType")));
		node2.addChild(new StatisticNode(Language.tr("Editor.Overview.Caller.All"),new StatisticViewerOptimizerLineChart(results,StatisticViewerOptimizerLineChart.Mode.DATA_TYPE_SUCCESS,-1)));

		node2.addChild(node3=new StatisticNode(Language.tr("SimStatistic.Accessibility.Change"),true,Language.tr("SimStatistic.Accessibility.Change")));
		if (results.data.get(0).kundenProTyp.length>1) for (int i=0;i<results.data.get(0).kundenProTyp.length;i++)
			node3.addChild(new StatisticNode(results.data.get(0).kundenProTyp[i].name,new StatisticViewerOptimizerBarChart(results,StatisticViewerOptimizerBarChart.DATA_TYPE_SUCCESS,i),Language.tr("SimStatistic.Accessibility")+" - "+Language.tr("SimStatistic.Accessibility.Change")+" - "+Language.tr("OptimizeResults.PerClientType")));
		node3.addChild(new StatisticNode(Language.tr("Editor.Overview.Caller.All"),new StatisticViewerOptimizerBarChart(results,StatisticViewerOptimizerBarChart.DATA_TYPE_SUCCESS,-1)));

		node.addChild(node2=new StatisticNode(Language.tr("SimStatistic.NumberOfCancelations"),Language.tr("SimStatistic.NumberOfCancelations")));
		node2.addChild(new StatisticNode(Language.tr("SimStatistic.Overview"),new StatisticViewerOptimizerTable(results,StatisticViewerOptimizerTable.Mode.DATA_TYPE_CANCEL)));
		if (results.data.get(0).kundenProTyp.length>1) for (int i=0;i<results.data.get(0).kundenProTyp.length;i++)
			node2.addChild(new StatisticNode(results.data.get(0).kundenProTyp[i].name,new StatisticViewerOptimizerLineChart(results,StatisticViewerOptimizerLineChart.Mode.DATA_TYPE_CANCEL,i),Language.tr("SimStatistic.NumberOfCancelations")+" - "+Language.tr("OptimizeResults.PerClientType")));
		node2.addChild(new StatisticNode(Language.tr("Editor.Overview.Caller.All"),new StatisticViewerOptimizerLineChart(results,StatisticViewerOptimizerLineChart.Mode.DATA_TYPE_CANCEL,-1)));

		node2.addChild(node3=new StatisticNode(Language.tr("SimStatistic.NumberOfCancelations.Change"),true,Language.tr("SimStatistic.NumberOfCancelations.Change")));
		if (results.data.get(0).kundenProTyp.length>1) for (int i=0;i<results.data.get(0).kundenProTyp.length;i++)
			node3.addChild(new StatisticNode(results.data.get(0).kundenProTyp[i].name,new StatisticViewerOptimizerBarChart(results,StatisticViewerOptimizerBarChart.DATA_TYPE_CANCEL,i),Language.tr("SimStatistic.NumberOfCancelations")+" - "+Language.tr("SimStatistic.NumberOfCancelations.Change")+" - "+Language.tr("OptimizeResults.PerClientType")));
		node3.addChild(new StatisticNode(Language.tr("Editor.Overview.Caller.All"),new StatisticViewerOptimizerBarChart(results,StatisticViewerOptimizerBarChart.DATA_TYPE_CANCEL,-1)));

		node.addChild(node2=new StatisticNode(Language.tr("SimStatistic.WaitingTimes"),Language.tr("SimStatistic.WaitingTimes")));
		node2.addChild(new StatisticNode(Language.tr("SimStatistic.Overview"),new StatisticViewerOptimizerTable(results,StatisticViewerOptimizerTable.Mode.DATA_TYPE_WAITING_TIME)));
		if (results.data.get(0).kundenProTyp.length>1) for (int i=0;i<results.data.get(0).kundenProTyp.length;i++)
			node2.addChild(new StatisticNode(results.data.get(0).kundenProTyp[i].name,new StatisticViewerOptimizerLineChart(results,StatisticViewerOptimizerLineChart.Mode.DATA_TYPE_WAITING_TIME,i),Language.tr("SimStatistic.WaitingTimes")+" - "+Language.tr("OptimizeResults.PerClientType")));
		node2.addChild(new StatisticNode(Language.tr("Editor.Overview.Caller.All"),new StatisticViewerOptimizerLineChart(results,StatisticViewerOptimizerLineChart.Mode.DATA_TYPE_WAITING_TIME,-1)));

		node2.addChild(node3=new StatisticNode(Language.tr("SimStatistic.WaitingTimes.Change"),true,Language.tr("SimStatistic.WaitingTimes.Change")));
		if (results.data.get(0).kundenProTyp.length>1) for (int i=0;i<results.data.get(0).kundenProTyp.length;i++)
			node3.addChild(new StatisticNode(results.data.get(0).kundenProTyp[i].name,new StatisticViewerOptimizerBarChart(results,StatisticViewerOptimizerBarChart.DATA_TYPE_WAITING_TIME,i),Language.tr("SimStatistic.WaitingTimes")+" - "+Language.tr("SimStatistic.WaitingTimes.Change")+" - "+Language.tr("OptimizeResults.PerClientType")));
		node3.addChild(new StatisticNode(Language.tr("Editor.Overview.Caller.All"),new StatisticViewerOptimizerBarChart(results,StatisticViewerOptimizerBarChart.DATA_TYPE_WAITING_TIME,-1)));

		node.addChild(node2=new StatisticNode(Language.tr("SimStatistic.ResidenceTimes"),true,Language.tr("SimStatistic.ResidenceTimes")));
		node2.addChild(new StatisticNode(Language.tr("SimStatistic.Overview"),new StatisticViewerOptimizerTable(results,StatisticViewerOptimizerTable.Mode.DATA_TYPE_STAYING_TIME)));
		if (results.data.get(0).kundenProTyp.length>1) for (int i=0;i<results.data.get(0).kundenProTyp.length;i++)
			node2.addChild(new StatisticNode(results.data.get(0).kundenProTyp[i].name,new StatisticViewerOptimizerLineChart(results,StatisticViewerOptimizerLineChart.Mode.DATA_TYPE_STAYING_TIME,i),Language.tr("SimStatistic.ResidenceTimes")+" - "+Language.tr("OptimizeResults.PerClientType")));
		node2.addChild(new StatisticNode(Language.tr("Editor.Overview.Caller.All"),new StatisticViewerOptimizerLineChart(results,StatisticViewerOptimizerLineChart.Mode.DATA_TYPE_STAYING_TIME,-1)));

		node2.addChild(node3=new StatisticNode(Language.tr("SimStatistic.ResidenceTimes.Change"),true,Language.tr("SimStatistic.ResidenceTimes.Change")));
		if (results.data.get(0).kundenProTyp.length>1) for (int i=0;i<results.data.get(0).kundenProTyp.length;i++)
			node3.addChild(new StatisticNode(results.data.get(0).kundenProTyp[i].name,new StatisticViewerOptimizerBarChart(results,StatisticViewerOptimizerBarChart.DATA_TYPE_STAYING_TIME,i),Language.tr("SimStatistic.ResidenceTimes")+" - "+Language.tr("SimStatistic.ResidenceTimes.Change")+" - "+Language.tr("OptimizeResults.PerClientType")));
		node3.addChild(new StatisticNode(Language.tr("Editor.Overview.Caller.All"),new StatisticViewerOptimizerBarChart(results,StatisticViewerOptimizerBarChart.DATA_TYPE_STAYING_TIME,-1)));

		node.addChild(node2=new StatisticNode(Language.tr("SimStatistic.ServiceLevel"),Language.tr("SimStatistic.ServiceLevel")));
		node2.addChild(new StatisticNode(Language.tr("SimStatistic.Overview")+" ("+Language.tr("SimStatistic.CalculatedOn.SuccessfulClients")+")",new StatisticViewerOptimizerTable(results,StatisticViewerOptimizerTable.Mode.DATA_TYPE_SERVICE_LEVEL_CLIENTS)));
		node2.addChild(new StatisticNode(Language.tr("SimStatistic.Overview")+" ("+Language.tr("SimStatistic.CalculatedOn.SuccessfulCalls")+")",new StatisticViewerOptimizerTable(results,StatisticViewerOptimizerTable.Mode.DATA_TYPE_SERVICE_LEVEL_CALLS)));
		node2.addChild(new StatisticNode(Language.tr("SimStatistic.Overview")+" ("+Language.tr("SimStatistic.CalculatedOn.AllClients")+")",new StatisticViewerOptimizerTable(results,StatisticViewerOptimizerTable.Mode.DATA_TYPE_SERVICE_LEVEL_CLIENTS_ALL)));
		node2.addChild(new StatisticNode(Language.tr("SimStatistic.Overview")+" ("+Language.tr("SimStatistic.CalculatedOn.AllCalls")+")",new StatisticViewerOptimizerTable(results,StatisticViewerOptimizerTable.Mode.DATA_TYPE_SERVICE_LEVEL_CALLS_ALL)));
		if (results.data.get(0).kundenProTyp.length>1) for (int i=0;i<results.data.get(0).kundenProTyp.length;i++) {
			node2.addChild(new StatisticNode(results.data.get(0).kundenProTyp[i].name+" ("+Language.tr("SimStatistic.CalculatedOn.SuccessfulClients")+")",new StatisticViewerOptimizerLineChart(results,StatisticViewerOptimizerLineChart.Mode.DATA_TYPE_SERVICE_LEVEL_CLIENTS,i),Language.tr("SimStatistic.ServiceLevel")+" -"+Language.tr("OptimizeResults.PerClientType")));
			node2.addChild(new StatisticNode(results.data.get(0).kundenProTyp[i].name+" ("+Language.tr("SimStatistic.CalculatedOn.SuccessfulCalls")+")",new StatisticViewerOptimizerLineChart(results,StatisticViewerOptimizerLineChart.Mode.DATA_TYPE_SERVICE_LEVEL_CALLS,i),Language.tr("SimStatistic.ServiceLevel")+" - "+Language.tr("OptimizeResults.PerClientType")));
			node2.addChild(new StatisticNode(results.data.get(0).kundenProTyp[i].name+" ("+Language.tr("SimStatistic.CalculatedOn.AllClients")+")",new StatisticViewerOptimizerLineChart(results,StatisticViewerOptimizerLineChart.Mode.DATA_TYPE_SERVICE_LEVEL_CLIENTS_ALL,i),Language.tr("SimStatistic.ServiceLevel")+" - "+Language.tr("OptimizeResults.PerClientType")));
			node2.addChild(new StatisticNode(results.data.get(0).kundenProTyp[i].name+" ("+Language.tr("SimStatistic.CalculatedOn.AllCalls")+")",new StatisticViewerOptimizerLineChart(results,StatisticViewerOptimizerLineChart.Mode.DATA_TYPE_SERVICE_LEVEL_CALLS_ALL,i),Language.tr("SimStatistic.ServiceLevel")+" - "+Language.tr("OptimizeResults.PerClientType")));
		}
		node2.addChild(new StatisticNode(Language.tr("Editor.Overview.Caller.All")+" ("+Language.tr("SimStatistic.CalculatedOn.SuccessfulClients")+")",new StatisticViewerOptimizerLineChart(results,StatisticViewerOptimizerLineChart.Mode.DATA_TYPE_SERVICE_LEVEL_CLIENTS,-1)));
		node2.addChild(new StatisticNode(Language.tr("Editor.Overview.Caller.All")+" ("+Language.tr("SimStatistic.CalculatedOn.SuccessfulCalls")+")",new StatisticViewerOptimizerLineChart(results,StatisticViewerOptimizerLineChart.Mode.DATA_TYPE_SERVICE_LEVEL_CALLS,-1)));
		node2.addChild(new StatisticNode(Language.tr("Editor.Overview.Caller.All")+" ("+Language.tr("SimStatistic.CalculatedOn.AllClients")+")",new StatisticViewerOptimizerLineChart(results,StatisticViewerOptimizerLineChart.Mode.DATA_TYPE_SERVICE_LEVEL_CLIENTS_ALL,-1)));
		node2.addChild(new StatisticNode(Language.tr("Editor.Overview.Caller.All")+" ("+Language.tr("SimStatistic.CalculatedOn.AllCalls")+")",new StatisticViewerOptimizerLineChart(results,StatisticViewerOptimizerLineChart.Mode.DATA_TYPE_SERVICE_LEVEL_CALLS_ALL,-1)));

		node2.addChild(node3=new StatisticNode(Language.tr("SimStatistic.ServiceLevel.Change"),true,Language.tr("SimStatistic.ServiceLevel.Change")));
		if (results.data.get(0).kundenProTyp.length>1) for (int i=0;i<results.data.get(0).kundenProTyp.length;i++) {
			node3.addChild(new StatisticNode(results.data.get(0).kundenProTyp[i].name+" ("+Language.tr("SimStatistic.CalculatedOn.SuccessfulClients")+")",new StatisticViewerOptimizerBarChart(results,StatisticViewerOptimizerBarChart.DATA_TYPE_SERVICE_LEVEL_CLIENTS,i),Language.tr("SimStatistic.ServiceLevel")+" - "+Language.tr("SimStatistic.ServiceLevel.Change")+" - "+Language.tr("OptimizeResults.PerClientType")));
			node3.addChild(new StatisticNode(results.data.get(0).kundenProTyp[i].name+" ("+Language.tr("SimStatistic.CalculatedOn.SuccessfulCalls")+")",new StatisticViewerOptimizerBarChart(results,StatisticViewerOptimizerBarChart.DATA_TYPE_SERVICE_LEVEL_CALLS,i),Language.tr("SimStatistic.ServiceLevel")+" - "+Language.tr("SimStatistic.ServiceLevel.Change")+" - "+Language.tr("OptimizeResults.PerClientType")));
			node3.addChild(new StatisticNode(results.data.get(0).kundenProTyp[i].name+" ("+Language.tr("SimStatistic.CalculatedOn.AllClients")+")",new StatisticViewerOptimizerBarChart(results,StatisticViewerOptimizerBarChart.DATA_TYPE_SERVICE_LEVEL_CLIENTS_ALL,i),Language.tr("SimStatistic.ServiceLevel")+" - "+Language.tr("SimStatistic.ServiceLevel.Change")+" - "+Language.tr("OptimizeResults.PerClientType")));
			node3.addChild(new StatisticNode(results.data.get(0).kundenProTyp[i].name+" ("+Language.tr("SimStatistic.CalculatedOn.AllCalls")+")",new StatisticViewerOptimizerBarChart(results,StatisticViewerOptimizerBarChart.DATA_TYPE_SERVICE_LEVEL_CALLS_ALL,i),Language.tr("SimStatistic.ServiceLevel")+" - "+Language.tr("SimStatistic.ServiceLevel.Change")+" - "+Language.tr("OptimizeResults.PerClientType")));
		}
		node3.addChild(new StatisticNode(Language.tr("Editor.Overview.Caller.All")+" ("+Language.tr("SimStatistic.CalculatedOn.SuccessfulClients")+")",new StatisticViewerOptimizerBarChart(results,StatisticViewerOptimizerBarChart.DATA_TYPE_SERVICE_LEVEL_CLIENTS,-1)));
		node3.addChild(new StatisticNode(Language.tr("Editor.Overview.Caller.All")+" ("+Language.tr("SimStatistic.CalculatedOn.SuccessfulCalls")+")",new StatisticViewerOptimizerBarChart(results,StatisticViewerOptimizerBarChart.DATA_TYPE_SERVICE_LEVEL_CALLS,-1)));
		node3.addChild(new StatisticNode(Language.tr("Editor.Overview.Caller.All")+" ("+Language.tr("SimStatistic.CalculatedOn.AllClients")+")",new StatisticViewerOptimizerBarChart(results,StatisticViewerOptimizerBarChart.DATA_TYPE_SERVICE_LEVEL_CLIENTS_ALL,-1)));
		node3.addChild(new StatisticNode(Language.tr("Editor.Overview.Caller.All")+" ("+Language.tr("SimStatistic.CalculatedOn.AllCalls")+")",new StatisticViewerOptimizerBarChart(results,StatisticViewerOptimizerBarChart.DATA_TYPE_SERVICE_LEVEL_CALLS_ALL,-1)));

		/* Agenten-Daten */
		root.addChild(node=new StatisticNode(Language.tr("SimStatistic.Agents")));

		node.addChild(node2=new StatisticNode(Language.tr("SimStatistic.Agents.PerCallcenter"),Language.tr("SimStatistic.Agents.PerCallcenter")));
		node2.addChild(new StatisticNode(Language.tr("SimStatistic.Overview"),new StatisticViewerOptimizerTable(results,StatisticViewerOptimizerTable.Mode.DATA_TYPE_AGENTS_CALLCENTER)));
		if (results.data.get(0).agentenProCallcenter.length>1) for (int i=0;i<results.data.get(0).agentenProCallcenter.length;i++)
			node2.addChild(new StatisticNode(results.data.get(0).agentenProCallcenter[i].name,new StatisticViewerOptimizerLineChart(results,StatisticViewerOptimizerLineChart.Mode.DATA_TYPE_AGENTS_CALLCENTER,i),Language.tr("SimStatistic.Agents.PerCallcenter")+" - "+Language.tr("OptimizeResults.SingleGraphics")));
		node2.addChild(new StatisticNode(Language.tr("SimStatistic.AllCallcenter"),new StatisticViewerOptimizerLineChart(results,StatisticViewerOptimizerLineChart.Mode.DATA_TYPE_AGENTS_CALLCENTER,-1)));

		node2.addChild(node3=new StatisticNode(Language.tr("SimStatistic.NumberOfAgents.Change"),true,Language.tr("SimStatistic.Agents.PerCallcenter")+" - "+Language.tr("SimStatistic.NumberOfAgents.Change")));
		if (results.data.get(0).agentenProCallcenter.length>1) for (int i=0;i<results.data.get(0).agentenProCallcenter.length;i++)
			node3.addChild(new StatisticNode(results.data.get(0).agentenProCallcenter[i].name,new StatisticViewerOptimizerBarChart(results,StatisticViewerOptimizerBarChart.DATA_TYPE_AGENTS_CALLCENTER,i),Language.tr("SimStatistic.Agents.PerCallcenter")+" - "+Language.tr("SimStatistic.NumberOfAgents.Change")+" - "+Language.tr("OptimizeResults.SingleGraphics")));
		node3.addChild(new StatisticNode(Language.tr("SimStatistic.AllCallcenter"),new StatisticViewerOptimizerBarChart(results,StatisticViewerOptimizerBarChart.DATA_TYPE_AGENTS_CALLCENTER,-1)));

		node.addChild(node2=new StatisticNode(Language.tr("SimStatistic.Agents.PerSkillLevel"),true,Language.tr("SimStatistic.Agents.PerSkillLevel")));
		node2.addChild(new StatisticNode(Language.tr("SimStatistic.Overview"),new StatisticViewerOptimizerTable(results,StatisticViewerOptimizerTable.Mode.DATA_TYPE_AGENTS_SKILL_LEVEL)));
		for (int i=0;i<results.data.get(0).agentenProSkilllevel.length;i++)
			node2.addChild(new StatisticNode(results.data.get(0).agentenProSkilllevel[i].name,new StatisticViewerOptimizerLineChart(results,StatisticViewerOptimizerLineChart.Mode.DATA_TYPE_AGENTS_SKILL_LEVEL,i),Language.tr("SimStatistic.Agents.PerSkillLevel")+" - "+Language.tr("OptimizeResults.SingleGraphics")));

		node2.addChild(node3=new StatisticNode(Language.tr("SimStatistic.Agents.PerSkillLevel"),true,Language.tr("SimStatistic.Agents.PerSkillLevel")+" - "+Language.tr("SimStatistic.NumberOfAgents.Change")));
		if (results.data.get(0).agentenProSkilllevel.length>1) for (int i=0;i<results.data.get(0).agentenProSkilllevel.length;i++)
			node3.addChild(new StatisticNode(results.data.get(0).agentenProSkilllevel[i].name,new StatisticViewerOptimizerBarChart(results,StatisticViewerOptimizerBarChart.DATA_TYPE_AGENTS_SKILL_LEVEL,i),Language.tr("SimStatistic.Agents.PerSkillLevel")+" - "+Language.tr("SimStatistic.NumberOfAgents.Change")+" - "+Language.tr("OptimizeResults.SingleGraphics")));

		node.addChild(node2=new StatisticNode(Language.tr("SimStatistic.WorkLoad.PerCallcenter"),Language.tr("SimStatistic.WorkLoad.PerCallcenter")));
		node2.addChild(new StatisticNode(Language.tr("SimStatistic.Overview"),new StatisticViewerOptimizerTable(results,StatisticViewerOptimizerTable.Mode.DATA_TYPE_WORKLOAD_CALLCENTER)));
		if (results.data.get(0).agentenProCallcenter.length>1) for (int i=0;i<results.data.get(0).agentenProCallcenter.length;i++)
			node2.addChild(new StatisticNode(results.data.get(0).agentenProCallcenter[i].name,new StatisticViewerOptimizerLineChart(results,StatisticViewerOptimizerLineChart.Mode.DATA_TYPE_WORKLOAD_CALLCENTER,i),Language.tr("SimStatistic.WorkLoad.PerCallcenter")+" - "+Language.tr("OptimizeResults.SingleGraphics")));
		node2.addChild(new StatisticNode(Language.tr("SimStatistic.AllCallcenter"),new StatisticViewerOptimizerLineChart(results,StatisticViewerOptimizerLineChart.Mode.DATA_TYPE_WORKLOAD_CALLCENTER,-1)));

		node2.addChild(node3=new StatisticNode(Language.tr("SimStatistic.WorkLoad.Change"),true,Language.tr("SimStatistic.WorkLoad.PerCallcenter")+" - "+Language.tr("SimStatistic.WorkLoad.Change")));
		if (results.data.get(0).agentenProCallcenter.length>1) for (int i=0;i<results.data.get(0).agentenProCallcenter.length;i++)
			node3.addChild(new StatisticNode(results.data.get(0).agentenProCallcenter[i].name,new StatisticViewerOptimizerBarChart(results,StatisticViewerOptimizerBarChart.DATA_TYPE_WORKLOAD_CALLCENTER,i),Language.tr("SimStatistic.WorkLoad.PerCallcenter")+" - "+Language.tr("SimStatistic.WorkLoad.Change")+" - "+Language.tr("OptimizeResults.SingleGraphics")));
		node3.addChild(new StatisticNode(Language.tr("SimStatistic.AllCallcenter"),new StatisticViewerOptimizerBarChart(results,StatisticViewerOptimizerBarChart.DATA_TYPE_WORKLOAD_CALLCENTER,-1)));

		node.addChild(node2=new StatisticNode(Language.tr("SimStatistic.WorkLoad.PerSkillLevel"),true,Language.tr("SimStatistic.WorkLoad.PerSkillLevel")));
		node2.addChild(new StatisticNode(Language.tr("SimStatistic.Overview"),new StatisticViewerOptimizerTable(results,StatisticViewerOptimizerTable.Mode.DATA_TYPE_WORKLOAD_SKILL_LEVEL)));
		for (int i=0;i<results.data.get(0).agentenProSkilllevel.length;i++)
			node2.addChild(new StatisticNode(results.data.get(0).agentenProSkilllevel[i].name,new StatisticViewerOptimizerLineChart(results,StatisticViewerOptimizerLineChart.Mode.DATA_TYPE_WORKLOAD_SKILL_LEVEL,i),Language.tr("SimStatistic.WorkLoad.PerSkillLevel")+" - "+Language.tr("OptimizeResults.SingleGraphics")));

		node2.addChild(node3=new StatisticNode(Language.tr("SimStatistic.WorkLoad.Change"),true,Language.tr("SimStatistic.WorkLoad.PerSkillLevel")+" - "+Language.tr("SimStatistic.WorkLoad.Change")));
		for (int i=0;i<results.data.get(0).agentenProSkilllevel.length;i++)
			node3.addChild(new StatisticNode(results.data.get(0).agentenProSkilllevel[i].name,new StatisticViewerOptimizerBarChart(results,StatisticViewerOptimizerBarChart.DATA_TYPE_WORKLOAD_SKILL_LEVEL,i),Language.tr("SimStatistic.WorkLoad.PerSkillLevel")+" - "+Language.tr("SimStatistic.WorkLoad.Change")+" - "+Language.tr("OptimizeResults.SingleGraphics")));

		/* Erlang-C Vergleich */
		root.addChild(node=new StatisticNode(Language.tr("SimStatistic.ErlangCComparison")));
		node.addChild(new StatisticNode(Language.tr("SimStatistic.ErlangCComparison.Success"),new StatisticViewerOptimizerTable(results,StatisticViewerOptimizerTable.Mode.DATA_TYPE_ERLANGC_SUCCESS)));
		node.addChild(new StatisticNode(Language.tr("SimStatistic.ErlangCComparison.Success"),new StatisticViewerOptimizerLineChart(results,StatisticViewerOptimizerLineChart.Mode.DATA_TYPE_ERLANGC_SUCCESS,-1)));
		node.addChild(new StatisticNode(Language.tr("SimStatistic.ErlangCComparison.WaitingTime"),new StatisticViewerOptimizerTable(results,StatisticViewerOptimizerTable.Mode.DATA_TYPE_ERLANGC_WAITING_TIME)));
		node.addChild(new StatisticNode(Language.tr("SimStatistic.ErlangCComparison.WaitingTime"),new StatisticViewerOptimizerLineChart(results,StatisticViewerOptimizerLineChart.Mode.DATA_TYPE_ERLANGC_WAITING_TIME,-1)));
		node.addChild(new StatisticNode(Language.tr("SimStatistic.ErlangCComparison.ServiceLevel"),new StatisticViewerOptimizerTable(results,StatisticViewerOptimizerTable.Mode.DATA_TYPE_ERLANGC_SERVICE_LEVEL)));
		node.addChild(new StatisticNode(Language.tr("SimStatistic.ErlangCComparison.ServiceLevel"),new StatisticViewerOptimizerLineChart(results,StatisticViewerOptimizerLineChart.Mode.DATA_TYPE_ERLANGC_SERVICE_LEVEL,-1)));

		/* Kosten */
		root.addChild(node=new StatisticNode(Language.tr("SimStatistic.Costs")));

		node.addChild(new StatisticNode(Language.tr("SimStatistic.Overview"),new StatisticViewerOptimizerTable(results,StatisticViewerOptimizerTable.Mode.DATA_TYPE_COSTS)));
		node.addChild(new StatisticNode(Language.tr("SimStatistic.Overview"),new StatisticViewerOptimizerLineChart(results,StatisticViewerOptimizerLineChart.Mode.DATA_TYPE_COSTS,-1)));

		node.addChild(node2=new StatisticNode(Language.tr("SimStatistic.Costs")+" "+Language.tr("OptimizeResults.PerClientType"),true,Language.tr("OptimizeResults.PerClientType")));
		if (results.data.get(0).kundenProTyp.length>1) for (int i=0;i<results.data.get(0).kundenProTyp.length;i++)
			node2.addChild(new StatisticNode(results.data.get(0).kundenProTyp[i].name,new StatisticViewerOptimizerTable(results,StatisticViewerOptimizerTable.Mode.DATA_TYPE_CALLER_COSTS,i),Language.tr("SimStatistic.Costs")+" - "+Language.tr("OptimizeResults.PerClientType")));
		node2.addChild(new StatisticNode(Language.tr("Editor.Overview.Caller.All"),new StatisticViewerOptimizerTable(results,StatisticViewerOptimizerTable.Mode.DATA_TYPE_CALLER_COSTS,-1)));
		if (results.data.get(0).kundenProTyp.length>1) for (int i=0;i<results.data.get(0).kundenProTyp.length;i++)
			node2.addChild(new StatisticNode(results.data.get(0).kundenProTyp[i].name,new StatisticViewerOptimizerLineChart(results,StatisticViewerOptimizerLineChart.Mode.DATA_TYPE_CALLER_COSTS,i),Language.tr("SimStatistic.Costs")+" - "+Language.tr("OptimizeResults.PerClientType")));
		node2.addChild(new StatisticNode(Language.tr("Editor.Overview.Caller.All"),new StatisticViewerOptimizerLineChart(results,StatisticViewerOptimizerLineChart.Mode.DATA_TYPE_CALLER_COSTS,-1)));

		node.addChild(node2=new StatisticNode(Language.tr("SimStatistic.Costs")+" "+Language.tr("OptimizeResults.PerSkillLevel"),true,Language.tr("OptimizeResults.PerSkillLevel")));
		if (results.data.get(0).agentenProSkilllevel.length>1) for (int i=0;i<results.data.get(0).agentenProSkilllevel.length;i++)
			node2.addChild(new StatisticNode(results.data.get(0).agentenProSkilllevel[i].name,new StatisticViewerOptimizerTable(results,StatisticViewerOptimizerTable.Mode.DATA_TYPE_SKILL_LEVEL_COSTS,i),Language.tr("SimStatistic.Costs")+" - "+Language.tr("OptimizeResults.PerSkillLevel")));
		node2.addChild(new StatisticNode(Language.tr("Editor.Overview.Caller.All"),new StatisticViewerOptimizerTable(results,StatisticViewerOptimizerTable.Mode.DATA_TYPE_SKILL_LEVEL_COSTS,-1)));
		if (results.data.get(0).agentenProSkilllevel.length>1) for (int i=0;i<results.data.get(0).agentenProSkilllevel.length;i++)
			node2.addChild(new StatisticNode(results.data.get(0).agentenProSkilllevel[i].name,new StatisticViewerOptimizerLineChart(results,StatisticViewerOptimizerLineChart.Mode.DATA_TYPE_SKILL_LEVEL_COSTS,i),Language.tr("SimStatistic.Costs")+" - "+Language.tr("OptimizeResults.PerSkillLevel")));
		node2.addChild(new StatisticNode(Language.tr("Editor.Overview.Caller.All"),new StatisticViewerOptimizerLineChart(results,StatisticViewerOptimizerLineChart.Mode.DATA_TYPE_SKILL_LEVEL_COSTS,-1)));

		/* Systemdaten */
		root.addChild(new StatisticNode(Language.tr("SimStatistic.SystemData"),new StatisticViewerOptimizeSetup(results,StatisticViewerOptimizeSetup.Mode.DATA_SYSTEM)));

		/* Übersichtstabelle */
		root.addChild(new StatisticNode(Language.tr("OptimizeResults.AllResults"),new StatisticViewerOptimizerTable(results,StatisticViewerOptimizerTable.Mode.DATA_TYPE_SUMMARY)));
	}

	/**
	 * Speichert die Optimierungsergebnisse in einer Datei.
	 * @return	Liefert <code>true</code>, wenn die Ergebnisse erfolgreich gespeichert werden konnten
	 */
	private boolean saveResults() {
		if (results==null) return true;

		File file=XMLTools.showSaveDialog(getParent(),Language.tr("OptimizeResults.SaveResults"));

		boolean b=results.saveToFile(file);
		resultsSaved=resultsSaved || b;
		return b;
	}

	/**
	 * Prüft, ob der Viewer geschlossen werden darf (wozu ggf. der Nutzer gefragt wird).
	 * @return	Darf der Viewer geschlossen werden?
	 */
	private boolean closeCheck() {
		if (results==null || resultsSaved) return true;

		int i=MsgBox.confirmSave(this,Language.tr("OptimizeResults.ConfirmSave.Title"),Language.tr("OptimizeResults.ConfirmSave.Info"));
		switch (i) {
		case JOptionPane.YES_OPTION: return saveResults();
		case JOptionPane.NO_OPTION: return true;
		case JOptionPane.CANCEL_OPTION: return false;
		}
		return false;
	}

	/**
	 * Reagiert darauf, wenn der Modell-Viewer {@link OptimizeViewer#statistic}
	 * geschlossen werden möchte.
	 * @see OptimizeViewer#statistic
	 */
	private final class ModelViewerClosed implements Runnable {
		/** Viewer der geschlossen werden möchte (um ggf. weitere Wünsche auslesen zu können) */
		private final CallcenterModelEditorPanelDialog modelViewer;

		/**
		 * Konstruktor der Klasse
		 * @param modelViewer	Viewer auf dessen Schließen-Wunsch reagiert werden soll
		 */
		public ModelViewerClosed(final CallcenterModelEditorPanelDialog modelViewer) {
			this.modelViewer=modelViewer;
		}

		@Override
		public void run() {
			setEnableGUI(OptimizeViewer.this,true);
			if (modelViewer.getLoadModelToEditor()) {
				if (!closeCheck()) return;
				editModel=modelViewer.getCallcenterModel();
				if (doneNotify!=null) doneNotify.run();
			}
		}
	}

	/**
	 * Reagiert darauf, wenn der Vergleichs-Viewer {@link OptimizeViewer#statistic}
	 * geschlossen werden möchte.
	 * @see OptimizeViewer#statistic
	 */
	private final class CompareViewerClosed implements Runnable {
		/** Viewer der geschlossen werden möchte (um ggf. weitere Wünsche auslesen zu können) */
		private final ComparePanelDialog compareViewer;

		/**
		 * Konstruktor der Klasse
		 * @param compareViewer	Viewer auf dessen Schließen-Wunsch reagiert werden soll
		 */
		public CompareViewerClosed(final ComparePanelDialog compareViewer) {
			this.compareViewer=compareViewer;
		}

		@Override
		public void run() {
			setEnableGUI(OptimizeViewer.this,true);
			if (compareViewer.getLoadModelToEditor()) {
				if (!closeCheck()) return;
				editModel=compareViewer.getCallcenterModel();
				if (doneNotify!=null) doneNotify.run();
			}
		}
	}

	/**
	 * Zeigt einen Auswahldialog für einen Simulationslauf an und
	 * zeigt dann die Ergebnisse dieses einzelnen Laufs an.
	 * @param	showSelectDialog Auswahldialog anzeigen (<code>true</code>) oder Daten von letztem Lauf direkt ausgeben (<code>false</code>)
	 * @see OptimizeSelectResult
	 */
	private void showSingleRun(boolean showSelectDialog) {
		Statistics statistic;
		if (showSelectDialog) {
			OptimizeSelectResult select=new OptimizeSelectResult(owner,results,helpLink.pageOptimizeViewerModal);
			select.setVisible(true);
			if (select.getClosedBy()!=BaseEditDialog.CLOSED_BY_OK) return;
			statistic=results.data.get(select.getSelectedResult());
		} else {
			statistic=results.data.get(results.data.size()-1);
		}
		CallcenterModel model=statistic.editModel;

		CallcenterModelEditorPanelDialog viewer=new CallcenterModelEditorPanelDialog(owner,model,statistic,true,helpLink);
		viewer.setCloseNotify(new ModelViewerClosed(viewer));
		setEnableGUI(this,false);
		viewer.setVisible(true);
	}

	/**
	 * Zeigt einen Viewer zum Vergleich des ersten und des letzten Modells an.
	 */
	private void compareFirstLast() {
		final Statistics statistic1=results.data.get(0);
		final Statistics statistic2=results.data.get(results.data.size()-1);

		final ComparePanelDialog comparePanelDialog=new ComparePanelDialog(owner,
				new Statistics[]{statistic1,statistic2},new String[]{Language.tr("OptimizeResults.Model.Base"),Language.tr("OptimizeResults.Model.Optimized")},
				true,true,helpLink,helpLink.pageOptimizeViewer,helpLink.pageOptimizeViewerModal
				);
		comparePanelDialog.setCloseNotify(new CompareViewerClosed(comparePanelDialog));
		setEnableGUI(this,false);
		comparePanelDialog.setVisible(true);
	}

	@Override
	protected void userButtonClick(int index, JButton button) {
		switch (index) {
		case 0: showSingleRun(true); return;
		case 1: showSingleRun(false); return;
		case 2: compareFirstLast(); return;
		case 3: saveResults(); return;
		}
	}

	/**
	 * Panel zur Anzeige der eigentlichen Statistikdaten
	 * @see OptimizeViewer#statistic
	 */
	private final class OptimizerStatisticPanel extends StatisticBasePanel {
		/**
		 * Serialisierungs-ID der Klasse
		 * @see Serializable
		 */
		private static final long serialVersionUID = 5350416436702759598L;

		/**
		 * Konstruktor der Klasse
		 * @param title	Titel, der über der Baumstruktur angezeigt wird
		 * @param icon	Icon, das neben dem Titel über der Baumstruktur angezeigt wird (kann <code>null</code> sein, wenn kein Icon angezeigt werden soll)
		 * @param filterTree	Option zur Filterung der Daten im Statistikbaum anzeigen
		 * @param helpModal	Hilfe-Callback für den Aufruf der Hilfeseite für die Statistik
		 * @param helpLink	Hilfe-Objekt, welches alle Hilfe-Links enthält
		 * @param startSimulation	Callback zum Starten einer Simulation
		 * @param loadStatistics	Callback zum Laden von Statistikdaten
		 */
		public OptimizerStatisticPanel(String title, URL icon, boolean filterTree, Runnable helpModal, HelpLink helpLink, Runnable startSimulation, Runnable loadStatistics) {
			super(title, icon, null, null, true, true, helpModal, helpLink, startSimulation, loadStatistics, false);
		}

		@Override
		protected void loadFilterDialogSettings() {
			setHiddenIDsFromSetupString(SetupData.getSetup().optimizerTreeFilter);
		}


		@Override
		protected void saveFilterDialogSettings() {
			SetupData setup=SetupData.getSetup();
			setup.optimizerTreeFilter=getHiddenIDsSetupString();
			setup.saveSetupWithWarning(this);
		}

		@Override
		protected String getReportSettings() {
			return SetupData.getSetup().optimizeReportTreeFilter;
		}

		@Override
		protected void setReportSettings(String settings) {
			SetupData setup=SetupData.getSetup();
			setup.optimizeReportTreeFilter=settings;
			setup.saveSetupWithWarning(this);
		}
	}
}
