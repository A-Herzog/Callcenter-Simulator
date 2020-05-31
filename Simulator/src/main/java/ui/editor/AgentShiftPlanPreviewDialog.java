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
package ui.editor;

import java.awt.BorderLayout;
import java.awt.Container;
import java.awt.Dialog;
import java.awt.FlowLayout;
import java.awt.Window;
import java.awt.datatransfer.StringSelection;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.io.File;
import java.util.List;

import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JFileChooser;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTabbedPane;
import javax.swing.JTable;
import javax.swing.JToolBar;
import javax.swing.filechooser.FileFilter;
import javax.swing.filechooser.FileNameExtensionFilter;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.TableModel;

import language.Language;
import mathtools.Table;
import mathtools.TimeTools;
import mathtools.distribution.DataDistributionImpl;
import mathtools.distribution.swing.CommonVariables;
import mathtools.distribution.swing.JDataDistributionEditPanel;
import systemtools.MsgBox;
import tools.SetupData;
import ui.images.Images;
import ui.model.CallcenterModel;
import ui.model.CallcenterModelAgent;
import ui.model.CallcenterModelCallcenter;

/**
 * Zeigt den Schichtplan einer Liste von Agentengruppen als Tabelle und Diagramm an.
 * @author Alexander Herzog
 * @version 1.0
 * @see CallcenterModelAgent
 */
public class AgentShiftPlanPreviewDialog extends JDialog {
	private static final long serialVersionUID = -3187138325967105147L;

	private static final String[] COLHEADS_DIST = {Language.tr("Statistic.Interval"),Language.tr("SimStatistic.Count")};
	private static final String[] COLHEADS_PLAN = {Language.tr("SimStatistic.Count"),Language.tr("Editor.AgentsGroup.Shift.Start"),Language.tr("Editor.AgentsGroup.Shift.End"),Language.tr("Editor.AgentsGroup.Shift.Length"),Language.tr("Editor.AgentsGroup.SkillLevel")};

	private final List<CallcenterModelAgent> agents;
	private final DataDistributionImpl distribution;
	private final AgentShiftPlanDiagram agentShiftPlanDiagram;

	/**
	 * Konstruktor der Klasse <code>AgentShiftPlanPreviewDialog</code><br>
	 * Der Konstruktor zeigt das Fenster auch gleich als modaler Dialog an.
	 * @param owner	Übergeordnetes Fenster des Dialogs
	 * @param showDistribution Gibt an, ob auch Dialogseiten zur Verteilung der Agenten über den Tag angezeigt werden sollen
	 * @param agentGroup	Agentengruppen, deren Daten angezeigt werden sollen
	 * @param callcenter	Callcenter, in dem die Agentengruppe angesiedelt ist
	 * @param model	Gesamt-Modell
	 */
	public AgentShiftPlanPreviewDialog(Window owner, boolean showDistribution, CallcenterModelAgent agentGroup, CallcenterModelCallcenter callcenter, CallcenterModel model) {
		super(owner,Language.tr("Editor.AgentsGroup.Shift.ShowPlan.Preview"),Dialog.ModalityType.APPLICATION_MODAL);

		agents=agentGroup.calcAgentShifts(false,callcenter,model,true);
		if (showDistribution) distribution=agentGroup.calcAgentDistributionFromCallers(model.caller); else distribution=null;

		Container content=getContentPane();
		content.setLayout(new BorderLayout());

		JTabbedPane tabs;
		JPanel p;
		JToolBar toolbar;
		JButton b;

		content.add(tabs=new JTabbedPane());

		if (showDistribution) {
			/* Dialogseite "Verteilungsdiagramm" */
			JDataDistributionEditPanel distributionPanel;
			tabs.addTab(Language.tr("Editor.AgentsGroup.Shift.ShowPlan.Tabs.DistributionDiagram"),distributionPanel=new JDataDistributionEditPanel(distribution,JDataDistributionEditPanel.PlotMode.PLOT_DENSITY,false,0,true));
			distributionPanel.setImageSaveSize(SetupData.getSetup().imageSize);

			/* Dialogseite "Verteilungstabelle" */
			p=new JPanel(new BorderLayout());
			p.add(toolbar=new JToolBar(),BorderLayout.NORTH);
			toolbar.setFloatable(false);
			toolbar.add(b=new JButton(Language.tr("Editor.AgentsGroup.Shift.ShowPlan.CopyDistribution")));
			b.setIcon(Images.EDIT_COPY.getIcon());
			b.addActionListener(new ExportActionListener(ActionNr.COPY_DIST_TABLE));
			b.setToolTipText(Language.tr("Editor.AgentsGroup.Shift.ShowPlan.CopyDistribution.Info"));
			toolbar.add(b=new JButton(Language.tr("Editor.AgentsGroup.Shift.ShowPlan.SaveDistribution")));
			b.setIcon(Images.GENERAL_SAVE.getIcon());
			b.addActionListener(new ExportActionListener(ActionNr.SAVE_DIST_TABLE));
			b.setToolTipText(Language.tr("Editor.AgentsGroup.Shift.ShowPlan.SaveDistribution.Info"));
			TableModel data=new DefaultTableModel(getTableData(0),COLHEADS_DIST) {
				private static final long serialVersionUID = -8269778979021579082L;
				@Override
				public boolean isCellEditable(int rowIndex, int columnIndex) {return false;}
			};
			JTable t=new JTable(data);
			t.getTableHeader().setReorderingAllowed(false);
			p.add(new JScrollPane(t));
			tabs.addTab(Language.tr("Editor.AgentsGroup.Shift.ShowPlan.Tabs.DistributionTable"),p);
		}

		/* Dialogseite "Schichtplandiagramm" */
		p=new JPanel(new BorderLayout());
		p.add(toolbar=new JToolBar(),BorderLayout.NORTH);
		toolbar.setFloatable(false);
		toolbar.add(b=new JButton(Language.tr("Editor.AgentsGroup.Shift.ShowPlan.CopyPlan")));
		b.setIcon(Images.EDIT_COPY.getIcon());
		b.addActionListener(new ExportActionListener(ActionNr.COPY_PLAN_IMAGE));
		b.setToolTipText(Language.tr("Editor.AgentsGroup.Shift.ShowPlan.CopyPlan.Info"));
		toolbar.add(b=new JButton(Language.tr("Editor.AgentsGroup.Shift.ShowPlan.SavePlan")));
		b.setIcon(Images.GENERAL_SAVE.getIcon());
		b.addActionListener(new ExportActionListener(ActionNr.SAVE_PLAN_IMAGE));
		b.setToolTipText(Language.tr("Editor.AgentsGroup.Shift.ShowPlan.SavePlan.Info"));
		p.add(agentShiftPlanDiagram=new AgentShiftPlanDiagram(agents));
		tabs.addTab(Language.tr("Editor.AgentsGroup.Shift.ShowPlan.Tabs.ShiftPlanDiagram"),p);

		/* Dialogseite "Schichtplantabelle" */
		p=new JPanel(new BorderLayout());
		p.add(toolbar=new JToolBar(),BorderLayout.NORTH);
		toolbar.setFloatable(false);
		toolbar.add(b=new JButton(Language.tr("Editor.AgentsGroup.Shift.ShowPlan.CopyPlan")));
		b.setIcon(Images.EDIT_COPY.getIcon());
		b.addActionListener(new ExportActionListener(ActionNr.COPY_PLAN_TABLE));
		b.setToolTipText(Language.tr("Editor.AgentsGroup.Shift.ShowPlan.CopyPlanTable.Info"));
		toolbar.add(b=new JButton(Language.tr("Editor.AgentsGroup.Shift.ShowPlan.SavePlan")));
		b.setIcon(Images.GENERAL_SAVE.getIcon());
		b.addActionListener(new ExportActionListener(ActionNr.SAVE_PLAN_TABLE));
		b.setToolTipText(Language.tr("Editor.AgentsGroup.Shift.ShowPlan.SavePlanTable.Info"));
		TableModel data=new DefaultTableModel(getTableData(1),COLHEADS_PLAN) {
			private static final long serialVersionUID = -8269778979021579082L;
			@Override
			public boolean isCellEditable(int rowIndex, int columnIndex) {return false;}
		};
		JTable t=new JTable(data);
		p.add(new JScrollPane(t));
		tabs.addTab(Language.tr("Editor.AgentsGroup.Shift.ShowPlan.Tabs.ShiftPlanTable"),p);

		/* Weiteres GUI */
		if (showDistribution) {
			tabs.setIconAt(0,Images.EDITOR_SHIFT_PLAN_DISTRIBUTION.getIcon());
			tabs.setIconAt(1,Images.EDITOR_SHIFT_PLAN_TABLE.getIcon());
		}
		tabs.setIconAt(showDistribution?2:0,Images.EDITOR_SHIFT_PLAN_RESULT_DIAGRAM.getIcon());
		tabs.setIconAt(showDistribution?3:1,Images.EDITOR_SHIFT_PLAN_RESULT_TABLE.getIcon());

		content.add(p=new JPanel(new FlowLayout(FlowLayout.LEFT)),BorderLayout.SOUTH);
		p.add(b=new JButton(Language.tr("Dialog.Button.Ok"))); b.addActionListener(new ButtonActionListener());
		b.setIcon(Images.MSGBOX_OK.getIcon());

		getRootPane().setDefaultButton(b);

		addWindowListener(new WindowAdapter() {@Override
			public void windowClosing(WindowEvent event) {setVisible(false);}});
		setResizable(false);
		SetupData setup=SetupData.getSetup();
		setSize((int)Math.round(700*setup.scaleGUI),(int)Math.round(500*setup.scaleGUI));
		setLocationRelativeTo(owner);
		setVisible(true);
	}

	private String[][] getTableData(int nr) {
		String[][] data;
		if (nr==0) {
			data=new String[distribution.densityData.length][];
			int intervalLength=86400/distribution.densityData.length;
			for (int i=0;i<distribution.densityData.length;i++) {
				data[i]=new String[2];
				data[i][0]=TimeTools.formatTime(i*intervalLength)+"-"+TimeTools.formatTime((i+1)*intervalLength);
				data[i][1]=""+Math.round(distribution.densityData[i]);
			}
		} else {
			data=new String[agents.size()+1][];

			int count=0;
			long sum=0;
			for (int i=0;i<agents.size();i++) {
				CallcenterModelAgent a=agents.get(i);
				data[i]=new String[5];
				data[i][0]=""+a.count;
				count+=a.count;
				data[i][1]=TimeTools.formatTime(a.workingTimeStart);
				if (a.workingNoEndTime) {
					data[i][2]=Language.tr("Editor.AgentsGroup.Shift.ShowPlan.WorkingEndIsSimulationEnd");
					data[i][3]=TimeTools.formatTime(86400-a.workingTimeStart);
					sum+=a.count*(86400-a.workingTimeStart);
				} else {
					data[i][2]=TimeTools.formatTime(a.workingTimeEnd);
					data[i][3]=TimeTools.formatTime(a.workingTimeEnd-a.workingTimeStart);
					sum+=a.count*(a.workingTimeEnd-a.workingTimeStart);
				}
				data[i][4]=a.skillLevel;
			}

			data[agents.size()]=new String[5];
			data[agents.size()][0]=""+count;
			data[agents.size()][3]=TimeTools.formatTime(sum);
			data[agents.size()][4]=Language.tr("Statistic.Sum");
		}
		return data;
	}

	private class ButtonActionListener implements ActionListener {
		@Override
		public void actionPerformed(ActionEvent e) {setVisible(false);}
	}

	private enum ActionNr {
		COPY_DIST_TABLE,
		SAVE_DIST_TABLE,
		COPY_PLAN_TABLE,
		SAVE_PLAN_TABLE,
		COPY_PLAN_IMAGE,
		SAVE_PLAN_IMAGE
	}

	private class ExportActionListener implements ActionListener {
		private final ActionNr nr;

		public ExportActionListener(ActionNr nr) {
			this.nr=nr;
		}

		private Table getTable(int nr) {
			Table table=new Table();
			if (nr==0) {
				/* Verteilung über den Tag */
				table.addLine(COLHEADS_DIST);
			} else {
				/* Schichtplan */
				table.addLine(COLHEADS_PLAN);
			}
			table.addLines(getTableData(nr));
			return table;
		}

		private File getImageSaveName() {
			JFileChooser fc=new JFileChooser();
			CommonVariables.initialDirectoryToJFileChooser(fc);
			fc.setDialogTitle(Language.tr("FileType.Save.Image"));
			FileFilter jpg=new FileNameExtensionFilter(Language.tr("FileType.jpeg")+" (*.jpg, *.jpeg)","jpg","jpeg");
			FileFilter gif=new FileNameExtensionFilter(Language.tr("FileType.gif")+" (*.gif)","gif");
			FileFilter png=new FileNameExtensionFilter(Language.tr("FileType.png")+" (*.png)","png");
			fc.addChoosableFileFilter(jpg);
			fc.addChoosableFileFilter(gif);
			fc.addChoosableFileFilter(png);
			fc.setFileFilter(png);
			fc.setAcceptAllFileFilterUsed(false);

			if (fc.showSaveDialog(AgentShiftPlanPreviewDialog.this)!=JFileChooser.APPROVE_OPTION) return null;
			CommonVariables.initialDirectoryFromJFileChooser(fc);
			File file=fc.getSelectedFile();

			if (file.getName().indexOf('.')<0) {
				if (fc.getFileFilter()==jpg) file=new File(file.getAbsoluteFile()+".jpg");
				if (fc.getFileFilter()==gif) file=new File(file.getAbsoluteFile()+".gif");
				if (fc.getFileFilter()==png) file=new File(file.getAbsoluteFile()+".png");
			}

			return file;
		}

		@Override
		public void actionPerformed(ActionEvent e) {
			File file;
			switch (nr) {
			case COPY_DIST_TABLE:
				getToolkit().getSystemClipboard().setContents(new StringSelection(getTable(0).toString()),null);
				break;
			case SAVE_DIST_TABLE:
				file=Table.showSaveDialog(AgentShiftPlanPreviewDialog.this,Language.tr("Editor.AgentsGroup.Shift.ShowPlan.SaveDistribution")); if (file==null) break;
				if (file.exists()) {
					if (!MsgBox.confirmOverwrite(AgentShiftPlanPreviewDialog.this,file)) break;
				}
				getTable(0).save(file);
				break;
			case COPY_PLAN_TABLE:
				getToolkit().getSystemClipboard().setContents(new StringSelection(getTable(1).toString()),null);
				break;
			case SAVE_PLAN_TABLE:
				file=Table.showSaveDialog(AgentShiftPlanPreviewDialog.this,Language.tr("Editor.AgentsGroup.Shift.ShowPlan.SavePlan")); if (file==null) break;
				if (file.exists()) {
					if (!MsgBox.confirmOverwrite(AgentShiftPlanPreviewDialog.this,file)) break;
				}
				getTable(1).save(file);
				break;
			case COPY_PLAN_IMAGE:
				agentShiftPlanDiagram.copyToClipboard(SetupData.getSetup().imageSize);
				break;
			case SAVE_PLAN_IMAGE:
				file=getImageSaveName();
				if (file==null) break;
				String extension="png";
				if (file.getName().toLowerCase().endsWith(".jpg")) extension="jpg";
				if (file.getName().toLowerCase().endsWith(".gif")) extension="gif";
				agentShiftPlanDiagram.saveToFile(file,extension,SetupData.getSetup().imageSize);
				break;
			}
		}
	}
}