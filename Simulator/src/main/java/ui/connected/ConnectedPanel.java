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
import java.awt.Component;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Window;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.io.File;
import java.util.Timer;
import java.util.TimerTask;

import javax.swing.BorderFactory;
import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JFileChooser;
import javax.swing.JLabel;
import javax.swing.JMenuItem;
import javax.swing.JPanel;
import javax.swing.JPopupMenu;
import javax.swing.JProgressBar;
import javax.swing.JScrollPane;
import javax.swing.JTextField;
import javax.swing.filechooser.FileFilter;
import javax.swing.filechooser.FileNameExtensionFilter;
import javax.swing.filechooser.FileSystemView;

import language.Language;
import mathtools.NumberTools;
import mathtools.distribution.swing.CommonVariables;
import mathtools.distribution.tools.FileDropper;
import mathtools.distribution.tools.FileDropperData;
import systemtools.MsgBox;
import tools.JTableExt;
import tools.TrayNotify;
import ui.HelpLink;
import ui.editor.BaseEditDialog;
import ui.images.Images;
import ui.model.CallcenterModel;
import ui.specialpanels.JWorkPanel;
import xml.XMLTools;

/**
 * @author Alexander Herzog
 * @version 1.0
 */
public final class ConnectedPanel extends JWorkPanel {
	private static final long serialVersionUID = -8502043951150157035L;

	private final Window owner;
	private final HelpLink helpLink;

	private final JTextField folderField, day0statisticsField;
	private final JButton folderButton, day0statisticsButton, additionalDay0Caller;
	private final FileDropper drop1;

	private final FileDropper drop2;

	private final JPopupMenu toolsPopup;
	private final JMenuItem toolsLog;
	private final ConnectedJTableModel tableModel;
	private final JTableExt table;
	private final JLabel statusLabel;
	private final JProgressBar statusProgress;

	private final ConnectedSimulation simulation=new ConnectedSimulation();

	private File logFile=null;

	private Timer timer;
	private long startTime;
	private int count;

	/**
	 * Zeigt das Panel zur Bearbeitung von verketteten Modellen an.
	 * @param owner	Elternfenster
	 * @param loadFile	Zu ladende Verkettete-XML-Datei. (Wird <code>null</code> übergeben, so wird initial kein Modell geladen.)
	 * @param doneNotify	Objekt vom Typ <code>Runnable</code>, welches ausgeführt wird, wenn das Panel sich schließen will.
	 * @param helpLink Verknüpfung mit der Online-Hilfe
	 */
	public ConnectedPanel(Window owner, File loadFile, Runnable doneNotify, HelpLink helpLink) {
		super(doneNotify,helpLink.pageConnected);
		this.owner=owner;
		this.helpLink=helpLink;

		JPanel main,p,p2,p3;

		/* Main area */
		add(main=new JPanel(new BorderLayout()),BorderLayout.CENTER);

		main.add(p=new JPanel(),BorderLayout.NORTH);
		p.setLayout(new BoxLayout(p,BoxLayout.Y_AXIS));

		p.add(p2=new JPanel(new FlowLayout(FlowLayout.LEFT)));
		JLabel folderFieldLabel;
		p2.add(folderFieldLabel=new JLabel(Language.tr("Connected.BaseFolder")));
		p.add(p2=new JPanel(new BorderLayout()));
		p2.setBorder(BorderFactory.createEmptyBorder(0,5,0,5));
		p2.add(p3=new JPanel(),BorderLayout.CENTER);
		p3.setLayout(new BoxLayout(p3,BoxLayout.Y_AXIS));
		p3.setBorder(BorderFactory.createEmptyBorder(5,0,5,0));
		p3.add(folderField=new JTextField(60));
		Dimension d2=folderField.getPreferredSize();
		Dimension d3=folderField.getMaximumSize();
		d3.height=d2.height; folderField.setMaximumSize(d3);
		folderField.setText(getDefaultFolder());
		folderField.addKeyListener(new FolderFieldKeyListener());
		drop1=new FileDropper(new Component[]{folderFieldLabel,folderField},new ButtonListener());
		p2.add(p3=new JPanel(new FlowLayout(FlowLayout.LEFT)),BorderLayout.EAST);
		p3.setBorder(BorderFactory.createEmptyBorder(0,0,0,0));
		p3.add(folderButton=new JButton(Language.tr("Connected.BaseFolder.Select")));
		folderButton.addActionListener(new ButtonListener());
		folderButton.setToolTipText(Language.tr("Connected.BaseFolder.Select.Info"));
		folderButton.setIcon(Images.GENERAL_SELECT_FOLDER.getIcon());

		p.add(p2=new JPanel(new FlowLayout(FlowLayout.LEFT)));
		JLabel day0statisticsFieldLabel;
		p2.add(day0statisticsFieldLabel=new JLabel(Language.tr("Connected.Day1Statistic")));
		p.add(p2=new JPanel(new BorderLayout()));
		p2.setBorder(BorderFactory.createEmptyBorder(0,5,0,5));
		p2.add(p3=new JPanel(),BorderLayout.CENTER);
		p3.setLayout(new BoxLayout(p3,BoxLayout.Y_AXIS));
		p3.setBorder(BorderFactory.createEmptyBorder(5,0,5,0));
		p3.add(day0statisticsField=new JTextField(60));
		d2=day0statisticsField.getPreferredSize();
		d3=day0statisticsField.getMaximumSize();
		d3.height=d2.height; day0statisticsField.setMaximumSize(d3);
		day0statisticsField.addKeyListener(new FileFieldKeyListener());
		drop2=new FileDropper(new Component[]{day0statisticsFieldLabel,day0statisticsField},new ButtonListener());
		p2.add(p3=new JPanel(new FlowLayout(FlowLayout.LEFT)),BorderLayout.EAST);
		p3.setBorder(BorderFactory.createEmptyBorder(0,0,0,0));
		p3.add(day0statisticsButton=new JButton(Language.tr("Connected.Day1Statistic.Select")));
		day0statisticsButton.addActionListener(new ButtonListener());
		day0statisticsButton.setToolTipText(Language.tr("Connected.Day1Statistic.Select.Info"));
		day0statisticsButton.setIcon(Images.GENERAL_SELECT_FILE.getIcon());

		p.add(p2=new JPanel(new FlowLayout(FlowLayout.LEFT)));
		p2.add(additionalDay0Caller=new JButton(Language.tr("Connected.Day1Additional")));
		additionalDay0Caller.addActionListener(new ButtonListener());
		additionalDay0Caller.setIcon(Images.SIMULATION_CONNECTED_CARRY_OVER.getIcon());

		toolsPopup=new JPopupMenu();
		toolsPopup.add(toolsLog=new JMenuItem(Language.tr("Connected.LogRun"),Images.SIMULATION_LOG.getIcon()));
		toolsLog.addActionListener(new ButtonListener());

		main.add(p=new JPanel(new BorderLayout()),BorderLayout.CENTER);
		p.setBorder(BorderFactory.createEmptyBorder(5,5,5,5));
		tableModel=new ConnectedJTableModel(owner,helpLink.pageConnectedModal,new Runnable() {@Override public void run() {table.editingStopped(null);}});
		tableModel.setDefaultFolder(folderField.getText());
		p.add(new JScrollPane(table=new JTableExt(tableModel)));
		table.setIsPanelCellTable();

		main.add(p=new JPanel(),BorderLayout.SOUTH);
		p.setLayout(new BoxLayout(p,BoxLayout.Y_AXIS));
		p.add(p2=new JPanel(new FlowLayout(FlowLayout.LEFT)));
		p2.add(statusLabel=new JLabel(""));

		p.add(p2=new JPanel(new BorderLayout()));
		p2.setBorder(BorderFactory.createEmptyBorder(0,5,0,5));
		p2.add(statusProgress=new JProgressBar(),BorderLayout.CENTER);
		statusProgress.setStringPainted(true);

		/* Bottom line */
		addFooter(Language.tr("Connected.Simulation.Start"),Images.SIMULATION_CONNECTED_RUN.getIcon(),Language.tr("Connected.Simulation.Abort"));
		JButton button;
		button=addFooterButton(Language.tr("Connected.NewModel"));
		button.setToolTipText(Language.tr("Connected.NewModel.Info"));
		button.setIcon(Images.MODEL_NEW.getIcon());
		button=addFooterButton(Language.tr("Connected.LoadModel"));
		button.setToolTipText(Language.tr("Connected.LoadModel.Info"));
		button.setIcon(Images.MODEL_LOAD.getIcon());
		button=addFooterButton(Language.tr("Connected.SaveModel"));
		button.setToolTipText(Language.tr("Connected.SaveModel.Info"));
		button.setIcon(Images.MODEL_SAVE.getIcon());
		button=addFooterButton(Language.tr("Dialog.Button.Tools"));
		button.setIcon(Images.GENERAL_SETUP.getIcon());

		if (loadFile!=null && loadFile.exists()) {
			if (tableModel.loadFromFile(loadFile)==null) {folderField.setText(tableModel.getModel().defaultFolder); day0statisticsField.setText(tableModel.getModel().statisticsDay0);}
		}
	}

	private void selectFolder() {
		JFileChooser fc=new JFileChooser();
		CommonVariables.initialDirectoryToJFileChooser(fc);
		fc.setDialogTitle(Language.tr("Connected.BaseFolder.Select"));
		fc.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY);
		if (fc.showSaveDialog(owner)!=JFileChooser.APPROVE_OPTION) return;
		CommonVariables.initialDirectoryFromJFileChooser(fc);
		File file=fc.getSelectedFile();
		folderField.setText(file.toString());
		tableModel.setDefaultFolder(folderField.getText());
	}

	private String getDefaultFolder() {
		return FileSystemView.getFileSystemView().getDefaultDirectory().getPath();
	}

	private void selectDay0Statistics() {
		ConnectedModel model=tableModel.getModel();

		File file=XMLTools.showLoadDialog(owner,Language.tr("Connected.LoadStatistic"),new File(model.defaultFolder));
		if (file==null) return;

		if (!file.exists()) {
			MsgBox.error(owner,Language.tr("Window.LoadStatisticsError.Title"),String.format(Language.tr("Connected.Error.StatisticFileDoesNotExist"),file.toString()));
			return;
		}

		File folder=new File(tableModel.getModel().defaultFolder);
		String s;
		if (folder.equals(file.getParentFile())) s=file.getName(); else s=file.toString();
		tableModel.setDay0Statistics(s);
		day0statisticsField.setText(s);
	}

	private void selectAdditionalDay0Caller() {
		ConnectedModel connected=tableModel.getModel();
		if (connected.models.size()==0 || connected.models.get(0).isEmpty()) {
			MsgBox.error(owner,Language.tr("Connected.Error.NoDay1StatisticSelected.Title"),Language.tr("Connected.Error.NoDay1StatisticSelected.Info"));
			return;
		}

		String s=connected.models.get(0);
		File file=(s.contains("/") || s.contains("\\"))?new File(s):new File(connected.defaultFolder,s);
		if (!file.exists()) {
			MsgBox.error(owner,Language.tr("Connected.Error.Day1ModelFileDoesNotExist.Title"),String.format(Language.tr("Connected.Error.Day1ModelFileDoesNotExist.Info"),file.toString()));
			return;
		}

		CallcenterModel model=new CallcenterModel();
		s=model.loadFromFile(file);
		if (s!=null) {
			MsgBox.error(owner,Language.tr("Connected.Error.Day1ModelFileError.Title"),String.format(Language.tr("Connected.Error.Day1ModelFileError.Info"),file.toString())+"\n"+s);
			return;
		}

		AdditionalCallerSetupDialog dialog=new AdditionalCallerSetupDialog(owner,helpLink.pageConnectedModal,model,connected.additionalDay0CallerNames,connected.additionalDay0CallerCount);
		dialog.setVisible(true);
		if (dialog.getClosedBy()==BaseEditDialog.CLOSED_BY_OK) tableModel.setAddditionalDay0Caller(dialog.getCallerNames(),dialog.getCallerCount());
	}

	private void everythingDone() {
		String s;
		if (cancelWork)
			s=String.format(Language.tr("Connected.Aborted"),simulation.getSimNr());
		else {
			new TrayNotify(this,Language.tr("Connected.DoneNotify.Title"),Language.tr("Connected.DoneNotify.Info"));
			s=String.format((simulation.getSimNr()==1)?Language.tr("Connected.DoneInfoSingle"):Language.tr("Connected.DoneInfoMultiple"),simulation.getSimNr())+" "+NumberTools.formatLong((System.currentTimeMillis()-startTime)/1000)+" "+Language.tr("Statistic.Seconds");
		}
		MsgBox.info(this,Language.tr("Connected.SimulationDone"),s);

		System.gc();

		/* GUI umschalten */
		setWorkMode(false);
		folderField.setEnabled(true);
		folderButton.setEnabled(true);
		day0statisticsField.setEnabled(true);
		day0statisticsButton.setEnabled(true);
		table.setEnabled(true);
		statusLabel.setText("");
		statusProgress.setValue(0);
		logFile=null;
	}

	private final class SimTimerTask extends TimerTask {
		@Override
		public void run() {
			if (cancelWork) {timer.cancel(); simulation.getSimulator().cancel(); everythingDone(); return;}

			if (simulation.getSimulator().isRunning()) {
				count++; if (count%4==0) {
					statusProgress.setValue((int)((simulation.getSimNr()-1)*1000+1000*simulation.getSimulator().getSimDayCount()/simulation.getSimulator().getSimDaysCount()));
				}
				return;
			}

			String s=simulation.doneSimulation();
			if (s!=null) {
				timer.cancel(); cancelWork=true; everythingDone();
				MsgBox.error(ConnectedPanel.this,Language.tr("Connected.Error.Finishing"),s);
			}
			if (cancelWork) return;
			s=simulation.initNextSimulation(logFile);
			if (s!=null) {
				if (!s.isEmpty()) {
					MsgBox.error(ConnectedPanel.this,Language.tr("Connected.Error.Initialization"),s);
				}
				timer.cancel(); everythingDone(); return;
			}
			count=0;
			statusLabel.setText(String.format(Language.tr("Connected.Progress"),simulation.getSimNr(),simulation.getSimCount()));
		}
	}

	/* (non-Javadoc)
	 * @see complexcallcenter.specialpanels.JWorkPanel#run()
	 */
	@Override
	protected void run() {
		cancelWork=false;
		statusLabel.setText("");

		/* Vorbereitung der Simulationsläufe */
		String s=simulation.loadData(tableModel.getModel());
		if (s!=null) {
			MsgBox.error(this,Language.tr("Connected.Error.Initialization"),s);
			return;
		}

		/* GUI umschalten */
		setWorkMode(true);
		folderField.setEnabled(false);
		folderButton.setEnabled(false);
		day0statisticsField.setEnabled(false);
		day0statisticsButton.setEnabled(false);
		table.setEnabled(false);

		/* Start der Simulation */
		startTime=System.currentTimeMillis();
		s=simulation.initNextSimulation(logFile);
		if (s!=null) {
			if (!s.isEmpty()) {
				MsgBox.error(this,Language.tr("Connected.Error.Starting"),s);
			}
			everythingDone();
		} else {
			statusProgress.setMaximum(simulation.getSimCount()*1000);
			statusLabel.setText(String.format(Language.tr("Connected.Progress"),1,simulation.getSimCount()));
			count=0;
			timer=new Timer();
			timer.schedule(new SimTimerTask(),50,50);
		}
	}

	private void runLog() {
		JFileChooser fc=new JFileChooser();
		CommonVariables.initialDirectoryToJFileChooser(fc);
		fc.setDialogTitle(Language.tr("Connected.LogRun.Title"));
		FileFilter txt=new FileNameExtensionFilter(Language.tr("FileType.Text")+" (*.txt)","txt");
		fc.addChoosableFileFilter(txt);
		fc.setFileFilter(txt);

		if (fc.showSaveDialog(owner)!=JFileChooser.APPROVE_OPTION) return;
		CommonVariables.initialDirectoryFromJFileChooser(fc);
		File file=fc.getSelectedFile();

		if (file.getName().indexOf('.')<0) {
			if (fc.getFileFilter()==txt) file=new File(file.getAbsoluteFile()+".txt");
		}
		logFile=file;

		run();
	}

	@Override
	protected void done() {
		if (tableModel.clear(true)) super.done();
	}

	@Override
	protected void userButtonClick(int index, JButton button) {
		switch (index) {
		case 0 : tableModel.clear(true); return;
		case 1: if (tableModel.loadFromFile()) {folderField.setText(tableModel.getModel().defaultFolder); day0statisticsField.setText(tableModel.getModel().statisticsDay0);} return;
		case 2: tableModel.saveToFile(); return;
		case 3: toolsPopup.show(button,0,button.getBounds().height); return;
		}
	}

	@Override
	public boolean dragDropLoad(File file) {
		return tableModel.loadFromFile(file)==null;
	}

	private final class FolderFieldKeyListener implements KeyListener {
		@Override public void keyTyped(KeyEvent e) {tableModel.setDefaultFolder(folderField.getText());}
		@Override public void keyPressed(KeyEvent e) {tableModel.setDefaultFolder(folderField.getText());}
		@Override public void keyReleased(KeyEvent e) {tableModel.setDefaultFolder(folderField.getText());}
	}

	private final class FileFieldKeyListener implements KeyListener {
		@Override public void keyTyped(KeyEvent e) {tableModel.setDay0Statistics(day0statisticsField.getText());}
		@Override public void keyPressed(KeyEvent e) {tableModel.setDay0Statistics(day0statisticsField.getText());}
		@Override public void keyReleased(KeyEvent e) {tableModel.setDay0Statistics(day0statisticsField.getText());}
	}

	private final class ButtonListener implements ActionListener {
		@Override
		public void actionPerformed(ActionEvent e) {
			if (e.getSource()==folderButton) {selectFolder(); return;}
			if (e.getSource()==day0statisticsButton) {selectDay0Statistics(); return;}
			if (e.getSource()==toolsLog) {runLog(); return;}
			if (e.getSource()==additionalDay0Caller) {selectAdditionalDay0Caller(); return;}
			if (e.getSource() instanceof FileDropperData) {
				final FileDropperData data=(FileDropperData)e.getSource();
				final File file=data.getFile();
				if (data.getFileDropper()==drop1) {
					if (file.isDirectory()) {
						folderField.setText(file.toString());
						data.dragDropConsumed();
					}
				}
				if (data.getFileDropper()==drop2) {
					if (file.isFile()) {
						day0statisticsField.setText(file.toString());
						drop2.dragDropConsumed();
					}
				}
			}
		}
	}
}
