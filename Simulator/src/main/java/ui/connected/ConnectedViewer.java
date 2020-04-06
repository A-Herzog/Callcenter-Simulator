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
package ui.connected;

import java.awt.BorderLayout;
import java.awt.Window;
import java.net.URL;

import javax.swing.JButton;

import language.Language;
import simulator.Statistics;
import systemtools.statistics.StatisticNode;
import tools.SetupData;
import ui.HelpLink;
import ui.editor.BaseEditDialog;
import ui.editor.CallcenterModelEditorPanelDialog;
import ui.images.Images;
import ui.model.CallcenterModel;
import ui.optimizer.OptimizeSelectResult;
import ui.specialpanels.ViewerWithLoadModelCallback;
import ui.statistic.connected.StatisticViewerConnectedLineChart;
import ui.statistic.connected.StatisticViewerConnectedTable;
import ui.statistic.core.StatisticBasePanel;

/**
 * Zeigt die Ergebnisse einer verbundenen Simulation an.
 * @author Alexander Herzog
 * @version 1.0
 */
public class ConnectedViewer extends ViewerWithLoadModelCallback {
	private static final long serialVersionUID = -3595198939335681263L;

	private final Window owner;

	private final Statistics[] data;
	private final ConnectedStatisticPanel statistic;

	private final HelpLink helpLink;

	/**
	 * Konstruktor der Klasse
	 * @param owner	Übergeordnetes Fenster
	 * @param helpLink	Hilfe-Link
	 * @param data	Liste mit den anzuzeigenden Statistikdaten
	 */
	public ConnectedViewer(final Window owner, final HelpLink helpLink, final Statistics[] data) {
		super(null,helpLink.pageConnectedViewer);
		this.owner=owner;
		this.helpLink=helpLink;
		this.data=data;

		/* Statistik */
		setLayout(new BorderLayout());
		add(statistic=new ConnectedStatisticPanel(Language.tr("ConnectedResults.Title"),Images.SIMULATION_CONNECTED_VIEWER.getURL(),true,helpLink,null,null),BorderLayout.CENTER);
		StatisticNode root=new StatisticNode();
		addNodes(root);
		statistic.setStatisticData(root);

		/* Fußzeile */
		addFooter(null,null,null);
		JButton button;

		button=addFooterButton(Language.tr("ConnectedResults.SingleRun"));
		button.setToolTipText(Language.tr("ConnectedResults.SingleRun.Info"));
		button.setIcon(Images.STATISTICS.getIcon());
	}

	private final void addNodes(StatisticNode root) {
		StatisticNode node, node2;

		/* Kunden-Daten */
		root.addChild(node=new StatisticNode(Language.tr("SimStatistic.Clients")));

		node.addChild(node2=new StatisticNode(Language.tr("SimStatistic.NumberOfCallers"),true,Language.tr("SimStatistic.NumberOfCallers")));
		node2.addChild(new StatisticNode(Language.tr("SimStatistic.Overview"),new StatisticViewerConnectedTable(data,StatisticViewerConnectedTable.Mode.DATA_TYPE_CALLERS)));
		if (data[0].kundenProTyp.length>1) for (int i=0;i<data[0].kundenProTyp.length;i++)
			node2.addChild(new StatisticNode(data[0].kundenProTyp[i].name,new StatisticViewerConnectedLineChart(data,StatisticViewerConnectedLineChart.Mode.DATA_TYPE_CALLERS,i),Language.tr("SimStatistic.NumberOfCallers")+" - "+Language.tr("ConnectedResults.PerClientType")));
		node2.addChild(new StatisticNode(Language.tr("Editor.Overview.Caller.All"),new StatisticViewerConnectedLineChart(data,StatisticViewerConnectedLineChart.Mode.DATA_TYPE_CALLERS,-1)));

		node.addChild(node2=new StatisticNode(Language.tr("SimStatistic.Accessibility"),Language.tr("SimStatistic.Accessibility")));
		node2.addChild(new StatisticNode(Language.tr("SimStatistic.Overview"),new StatisticViewerConnectedTable(data,StatisticViewerConnectedTable.Mode.DATA_TYPE_SUCCESS)));
		if (data[0].kundenProTyp.length>1) for (int i=0;i<data[0].kundenProTyp.length;i++)
			node2.addChild(new StatisticNode(data[0].kundenProTyp[i].name,new StatisticViewerConnectedLineChart(data,StatisticViewerConnectedLineChart.Mode.DATA_TYPE_SUCCESS,i),Language.tr("SimStatistic.Accessibility")+" - "+Language.tr("ConnectedResults.PerClientType")));
		node2.addChild(new StatisticNode(Language.tr("Editor.Overview.Caller.All"),new StatisticViewerConnectedLineChart(data,StatisticViewerConnectedLineChart.Mode.DATA_TYPE_SUCCESS,-1)));

		node.addChild(node2=new StatisticNode(Language.tr("SimStatistic.NumberOfCancelations"),Language.tr("SimStatistic.NumberOfCancelations")));
		node2.addChild(new StatisticNode(Language.tr("SimStatistic.Overview"),new StatisticViewerConnectedTable(data,StatisticViewerConnectedTable.Mode.DATA_TYPE_CANCEL)));
		if (data[0].kundenProTyp.length>1) for (int i=0;i<data[0].kundenProTyp.length;i++)
			node2.addChild(new StatisticNode(data[0].kundenProTyp[i].name,new StatisticViewerConnectedLineChart(data,StatisticViewerConnectedLineChart.Mode.DATA_TYPE_CANCEL,i),Language.tr("SimStatistic.NumberOfCancelations")+" - "+Language.tr("ConnectedResults.PerClientType")));
		node2.addChild(new StatisticNode(Language.tr("Editor.Overview.Caller.All"),new StatisticViewerConnectedLineChart(data,StatisticViewerConnectedLineChart.Mode.DATA_TYPE_CANCEL,-1)));

		node.addChild(node2=new StatisticNode(Language.tr("SimStatistic.WaitingTimes"),Language.tr("SimStatistic.WaitingTimes")));
		node2.addChild(new StatisticNode(Language.tr("SimStatistic.Overview"),new StatisticViewerConnectedTable(data,StatisticViewerConnectedTable.Mode.DATA_TYPE_WAITING_TIME)));
		if (data[0].kundenProTyp.length>1) for (int i=0;i<data[0].kundenProTyp.length;i++)
			node2.addChild(new StatisticNode(data[0].kundenProTyp[i].name,new StatisticViewerConnectedLineChart(data,StatisticViewerConnectedLineChart.Mode.DATA_TYPE_WAITING_TIME,i),Language.tr("SimStatistic.WaitingTimes")+" - "+Language.tr("ConnectedResults.PerClientType")));
		node2.addChild(new StatisticNode(Language.tr("Editor.Overview.Caller.All"),new StatisticViewerConnectedLineChart(data,StatisticViewerConnectedLineChart.Mode.DATA_TYPE_WAITING_TIME,-1)));

		node.addChild(node2=new StatisticNode(Language.tr("SimStatistic.ResidenceTimes"),true,Language.tr("SimStatistic.ResidenceTimes")));
		node2.addChild(new StatisticNode(Language.tr("SimStatistic.Overview"),new StatisticViewerConnectedTable(data,StatisticViewerConnectedTable.Mode.DATA_TYPE_STAYING_TIME)));
		if (data[0].kundenProTyp.length>1) for (int i=0;i<data[0].kundenProTyp.length;i++)
			node2.addChild(new StatisticNode(data[0].kundenProTyp[i].name,new StatisticViewerConnectedLineChart(data,StatisticViewerConnectedLineChart.Mode.DATA_TYPE_STAYING_TIME,i),Language.tr("SimStatistic.ResidenceTimes")+" - "+Language.tr("ConnectedResults.PerClientType")));
		node2.addChild(new StatisticNode(Language.tr("Editor.Overview.Caller.All"),new StatisticViewerConnectedLineChart(data,StatisticViewerConnectedLineChart.Mode.DATA_TYPE_STAYING_TIME,-1)));

		node.addChild(node2=new StatisticNode(Language.tr("SimStatistic.ServiceLevel"),Language.tr("SimStatistic.ServiceLevel")));
		node2.addChild(new StatisticNode(Language.tr("SimStatistic.Overview")+" ("+Language.tr("SimStatistic.CalculatedOn.SuccessfulCalls")+")",new StatisticViewerConnectedTable(data,StatisticViewerConnectedTable.Mode.DATA_TYPE_SERVICE_LEVEL_CALLS_SUCCESS)));
		if (data[0].kundenProTyp.length>1) for (int i=0;i<data[0].kundenProTyp.length;i++)
			node2.addChild(new StatisticNode(data[0].kundenProTyp[i].name+" ("+Language.tr("SimStatistic.CalculatedOn.SuccessfulCalls")+")",new StatisticViewerConnectedLineChart(data,StatisticViewerConnectedLineChart.Mode.DATA_TYPE_SERVICE_LEVEL_CALLS_SUCCESS,i),Language.tr("SimStatistic.ServiceLevel")+" - "+Language.tr("SimStatistic.CalculatedOn.SuccessfulCalls")+" - "+Language.tr("ConnectedResults.PerClientType")));
		node2.addChild(new StatisticNode(Language.tr("Editor.Overview.Caller.All")+" ("+Language.tr("SimStatistic.CalculatedOn.SuccessfulCalls")+")",new StatisticViewerConnectedLineChart(data,StatisticViewerConnectedLineChart.Mode.DATA_TYPE_SERVICE_LEVEL_CALLS_SUCCESS,-1)));
		node2.addChild(new StatisticNode(Language.tr("SimStatistic.Overview")+" ("+Language.tr("SimStatistic.CalculatedOn.SuccessfulClients")+")",new StatisticViewerConnectedTable(data,StatisticViewerConnectedTable.Mode.DATA_TYPE_SERVICE_LEVEL_CLIENTS_SUCCESS)));
		if (data[0].kundenProTyp.length>1) for (int i=0;i<data[0].kundenProTyp.length;i++)
			node2.addChild(new StatisticNode(data[0].kundenProTyp[i].name+" ("+Language.tr("SimStatistic.CalculatedOn.SuccessfulClients")+")",new StatisticViewerConnectedLineChart(data,StatisticViewerConnectedLineChart.Mode.DATA_TYPE_SERVICE_LEVEL_CLIENTS_SUCCESS,i),Language.tr("SimStatistic.ServiceLevel")+" - "+Language.tr("SimStatistic.CalculatedOn.SuccessfulClients")+" - "+Language.tr("ConnectedResults.PerClientType")));
		node2.addChild(new StatisticNode(Language.tr("Editor.Overview.Caller.All")+" ("+Language.tr("SimStatistic.CalculatedOn.SuccessfulClients")+")",new StatisticViewerConnectedLineChart(data,StatisticViewerConnectedLineChart.Mode.DATA_TYPE_SERVICE_LEVEL_CLIENTS_SUCCESS,-1)));
		node2.addChild(new StatisticNode(Language.tr("SimStatistic.Overview")+" ("+Language.tr("SimStatistic.CalculatedOn.AllCalls")+")",new StatisticViewerConnectedTable(data,StatisticViewerConnectedTable.Mode.DATA_TYPE_SERVICE_LEVEL_CALLS_ALL)));
		if (data[0].kundenProTyp.length>1) for (int i=0;i<data[0].kundenProTyp.length;i++)
			node2.addChild(new StatisticNode(data[0].kundenProTyp[i].name+" ("+Language.tr("SimStatistic.CalculatedOn.AllCalls")+")",new StatisticViewerConnectedLineChart(data,StatisticViewerConnectedLineChart.Mode.DATA_TYPE_SERVICE_LEVEL_CALLS_ALL,i),Language.tr("SimStatistic.ServiceLevel")+" - "+Language.tr("SimStatistic.CalculatedOn.AllCalls")+" - "+Language.tr("ConnectedResults.PerClientType")));
		node2.addChild(new StatisticNode(Language.tr("Editor.Overview.Caller.All")+" ("+Language.tr("SimStatistic.CalculatedOn.AllCalls")+")",new StatisticViewerConnectedLineChart(data,StatisticViewerConnectedLineChart.Mode.DATA_TYPE_SERVICE_LEVEL_CALLS_ALL,-1)));
		node2.addChild(new StatisticNode(Language.tr("SimStatistic.Overview")+" ("+Language.tr("SimStatistic.CalculatedOn.AllClients")+")",new StatisticViewerConnectedTable(data,StatisticViewerConnectedTable.Mode.DATA_TYPE_SERVICE_LEVEL_CLIENTS_ALL)));
		if (data[0].kundenProTyp.length>1) for (int i=0;i<data[0].kundenProTyp.length;i++)
			node2.addChild(new StatisticNode(data[0].kundenProTyp[i].name+" ("+Language.tr("SimStatistic.CalculatedOn.AllClients")+")",new StatisticViewerConnectedLineChart(data,StatisticViewerConnectedLineChart.Mode.DATA_TYPE_SERVICE_LEVEL_CLIENTS_ALL,i),Language.tr("SimStatistic.ServiceLevel")+" - "+Language.tr("SimStatistic.CalculatedOn.AllClients")+" - "+Language.tr("ConnectedResults.PerClientType")));
		node2.addChild(new StatisticNode(Language.tr("Editor.Overview.Caller.All")+" ("+Language.tr("SimStatistic.CalculatedOn.AllClients")+")",new StatisticViewerConnectedLineChart(data,StatisticViewerConnectedLineChart.Mode.DATA_TYPE_SERVICE_LEVEL_CLIENTS_ALL,-1)));

		/* Agenten-Daten */
		root.addChild(node=new StatisticNode(Language.tr("SimStatistic.ActiveAgents")));

		node.addChild(node2=new StatisticNode(Language.tr("SimStatistic.Agents.PerCallcenter"),Language.tr("SimStatistic.Agents.PerCallcenter")));
		node2.addChild(new StatisticNode(Language.tr("SimStatistic.Overview"),new StatisticViewerConnectedTable(data,StatisticViewerConnectedTable.Mode.DATA_TYPE_AGENTS_CALLCENTER)));
		if (data[0].agentenProCallcenter.length>1) for (int i=0;i<data[0].agentenProCallcenter.length;i++)
			node2.addChild(new StatisticNode(data[0].agentenProCallcenter[i].name,new StatisticViewerConnectedLineChart(data,StatisticViewerConnectedLineChart.Mode.DATA_TYPE_AGENTS_CALLCENTER,i),Language.tr("SimStatistic.Agents.PerCallcenter")+" - "+Language.tr("ConnectedResults.SingleGraphics")));
		node2.addChild(new StatisticNode(Language.tr("SimStatistic.AllCallcenter"),new StatisticViewerConnectedLineChart(data,StatisticViewerConnectedLineChart.Mode.DATA_TYPE_AGENTS_CALLCENTER,-1)));

		node.addChild(node2=new StatisticNode(Language.tr("SimStatistic.Agents.PerSkillLevel"),true,Language.tr("SimStatistic.Agents.PerSkillLevel")));
		node2.addChild(new StatisticNode(Language.tr("SimStatistic.Overview"),new StatisticViewerConnectedTable(data,StatisticViewerConnectedTable.Mode.DATA_TYPE_AGENTS_SKILL_LEVEL)));
		for (int i=0;i<data[0].agentenProSkilllevel.length;i++)
			node2.addChild(new StatisticNode(data[0].agentenProSkilllevel[i].name,new StatisticViewerConnectedLineChart(data,StatisticViewerConnectedLineChart.Mode.DATA_TYPE_AGENTS_SKILL_LEVEL,i),Language.tr("SimStatistic.Agents.PerSkillLevel")+" - "+Language.tr("ConnectedResults.SingleGraphics")));

		node.addChild(node2=new StatisticNode(Language.tr("SimStatistic.WorkLoad.PerCallcenter"),Language.tr("SimStatistic.WorkLoad.PerCallcenter")));
		node2.addChild(new StatisticNode(Language.tr("SimStatistic.Overview"),new StatisticViewerConnectedTable(data,StatisticViewerConnectedTable.Mode.DATA_TYPE_WORKLOAD_CALLCENTER)));
		if (data[0].agentenProCallcenter.length>1) for (int i=0;i<data[0].agentenProCallcenter.length;i++)
			node2.addChild(new StatisticNode(data[0].agentenProCallcenter[i].name,new StatisticViewerConnectedLineChart(data,StatisticViewerConnectedLineChart.Mode.DATA_TYPE_WORKLOAD_CALLCENTER,i),Language.tr("SimStatistic.WorkLoad.PerCallcenter")+" - "+Language.tr("ConnectedResults.SingleGraphics")));
		node2.addChild(new StatisticNode(Language.tr("SimStatistic.AllCallcenter"),new StatisticViewerConnectedLineChart(data,StatisticViewerConnectedLineChart.Mode.DATA_TYPE_WORKLOAD_CALLCENTER,-1)));

		node.addChild(node2=new StatisticNode(Language.tr("SimStatistic.WorkLoad.PerSkillLevel"),true,Language.tr("SimStatistic.WorkLoad.PerSkillLevel")));
		node2.addChild(new StatisticNode(Language.tr("SimStatistic.Overview"),new StatisticViewerConnectedTable(data,StatisticViewerConnectedTable.Mode.DATA_TYPE_WORKLOAD_SKILL_LEVEL)));
		for (int i=0;i<data[0].agentenProSkilllevel.length;i++)
			node2.addChild(new StatisticNode(data[0].agentenProSkilllevel[i].name,new StatisticViewerConnectedLineChart(data,StatisticViewerConnectedLineChart.Mode.DATA_TYPE_WORKLOAD_SKILL_LEVEL,i),Language.tr("SimStatistic.WorkLoad.PerSkillLevel")+" - "+Language.tr("ConnectedResults.SingleGraphics")));

		/* Agenten-Daten */
		root.addChild(node=new StatisticNode(Language.tr("SimStatistic.AgentsOnModelBasis")));
		node.addChild(new StatisticNode(Language.tr("SimStatistic.AgentsOnModelBasis.AgentsInSimulation"),new StatisticViewerConnectedTable(data,StatisticViewerConnectedTable.Mode.DATA_TYPE_SIM_AGENTS)));
		node.addChild(new StatisticNode(Language.tr("SimStatistic.AgentsOnModelBasis.AgentsInModel"),new StatisticViewerConnectedTable(data,StatisticViewerConnectedTable.Mode.DATA_TYPE_MODEL_AGENTS)));
		node.addChild(new StatisticNode(Language.tr("SimStatistic.AgentsOnModelBasis.AgentsWithAddition"),new StatisticViewerConnectedTable(data,StatisticViewerConnectedTable.Mode.DATA_TYPE_FULL_AGENTS)));

		/* Übersichtstabelle */
		root.addChild(new StatisticNode(Language.tr("ConnectedResults.AllResults"),new StatisticViewerConnectedTable(data,StatisticViewerConnectedTable.Mode.DATA_TYPE_SUMMARY)));
	}

	private final class ModelViewerClosed implements Runnable {
		private final CallcenterModelEditorPanelDialog modelViewer;

		public ModelViewerClosed(CallcenterModelEditorPanelDialog modelViewer) {
			this.modelViewer=modelViewer;
		}

		@Override
		public void run() {
			setEnableGUI(ConnectedViewer.this,true);
			if (modelViewer.getLoadModelToEditor()) {
				editModel=modelViewer.getCallcenterModel();
				if (doneNotify!=null) doneNotify.run();
			}
		}
	}

	private final void showSingleRun() {
		OptimizeSelectResult select=new OptimizeSelectResult(owner,data,helpLink.pageConnectedViewerModal);
		select.setVisible(true);
		if (select.getClosedBy()!=BaseEditDialog.CLOSED_BY_OK) return;

		Statistics statistic=data[select.getSelectedResult()];
		CallcenterModel model=statistic.editModel;

		CallcenterModelEditorPanelDialog viewer=new CallcenterModelEditorPanelDialog(owner,model,statistic,true,helpLink);
		viewer.setCloseNotify(new ModelViewerClosed(viewer));

		setEnableGUI(this,false);
		viewer.setVisible(true);
	}

	@Override
	protected final void userButtonClick(int index, JButton button) {
		switch (index) {
		case 0: showSingleRun(); return;
		}
	}

	private final class ConnectedStatisticPanel extends StatisticBasePanel {
		private static final long serialVersionUID = 5350416436702759598L;

		public ConnectedStatisticPanel(String title, URL icon, boolean filterTree, HelpLink helpLink, Runnable startSilmulation, Runnable loadStatistics) {
			super(title, icon, null, null, true, filterTree, helpLink.pageConnectedViewerModal, helpLink, startSilmulation, loadStatistics);
		}

		@Override
		protected void loadFilterDialogSettings() {
			setHiddenIDsFromSetupString(SetupData.getSetup().connectedReportTreeFilter);
		}


		@Override
		protected void saveFilterDialogSettings() {
			SetupData setup=SetupData.getSetup();
			setup.connectedReportTreeFilter=getHiddenIDsSetupString();
			setup.saveSetupWithWarning(this);
		}

		@Override
		protected String getReportSettings() {
			return SetupData.getSetup().connectedReportTreeFilter;
		}

		@Override
		protected void setReportSettings(String settings) {
			SetupData setup=SetupData.getSetup();
			setup.connectedReportTreeFilter=settings;
			setup.saveSetupWithWarning(this);
		}
	}
}
