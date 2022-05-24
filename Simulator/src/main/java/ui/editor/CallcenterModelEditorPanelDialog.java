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
import java.awt.CardLayout;
import java.awt.Window;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.io.Serializable;

import javax.swing.AbstractAction;
import javax.swing.InputMap;
import javax.swing.JButton;
import javax.swing.JComponent;
import javax.swing.JFrame;
import javax.swing.JPanel;
import javax.swing.JRootPane;
import javax.swing.JToolBar;
import javax.swing.KeyStroke;
import javax.swing.WindowConstants;

import language.Language;
import simulator.Statistics;
import systemtools.MsgBox;
import tools.SetupData;
import ui.HelpLink;
import ui.images.Images;
import ui.model.CallcenterModel;
import ui.statistic.StatisticPanel;

/**
 * Dialog, der die Anzeige eines Callcenter-Modells ermöglicht.
 * @author Alexander Herzog
 * @version 1.0
 */
public class CallcenterModelEditorPanelDialog extends JFrame {
	/**
	 * Serialisierungs-ID der Klasse
	 * @see Serializable
	 */
	private static final long serialVersionUID = 426126815352751934L;

	/** Verknüpfung mit der Online-Hilfe */
	private final HelpLink helpLink;

	/** Symbolleiste */
	private JToolBar toolbar;
	/** "Schließen"-Schaltfläche */
	private JButton closeButton;
	/** "Speichern"-Schaltfläche */
	private JButton saveButton;
	/** "Laden"-Schaltfläche */
	private JButton loadButton;
	/** "Statistik speichern"-Schaltfläche */
	private JButton saveStatisticButton=null;
	/** "Modell-Editor"-Schaltfläche */
	private JButton editorButton=null;
	/** "Simulationsergebnisse"-Schaltfläche */
	private JButton statistikButton=null;
	/** "Hilfe"-Schaltfläche */
	private JButton helpButton;
	/** Registerseite in dem Dialog */
	private JPanel tabs;
	/** Modell-Editor */
	private CallcenterModelEditorPanel modelPanel;
	/** Statistikansicht */
	private StatisticPanel statisticPanel;

	/** Callback das aufgerufen wird, wenn das Fenster geschlossen werden soll, um die GUI des unterliegenden Fensters wieder zu aktivieren */
	private Runnable closeNotify;

	/**
	 * Wurde ausgewählt, dass das aktuell angezeigte Modell in den Editor geladen werden soll?
	 * @see #getLoadModelToEditor()
	 */
	private boolean loadModelToEditor=false;

	/**
	 * Konstruktor der Klasse <code>CallcenterModelEditorPanelDialog</code>
	 * @param owner	Übergeordnetes Fenster
	 * @param editModel	Callcenter-Datenmodell, welches angezeigt werden soll
	 * @param statistic	Callcenter-Statistikmodell, welches angezeigt werden soll. Ist dieser Parameter <code>null</code>, so wird nur das Datenmodell angezeigt	 *
	 * @param allowLoadToEditor	Gibt an, ob die Schaltfläche zum Laden des Modells in den Editor angezeigt werden soll.
	 * @param helpLink Verknüpfung mit der Online-Hilfe
	 */
	public CallcenterModelEditorPanelDialog(Window owner, CallcenterModel editModel, Statistics statistic, boolean allowLoadToEditor, HelpLink helpLink) {
		super((statistic==null)?Language.tr("Window.Viewer.Model"):Language.tr("Window.Viewer.Results"));

		this.helpLink=helpLink;

		setDefaultCloseOperation(WindowConstants.DO_NOTHING_ON_CLOSE);
		addWindowListener(new WindowAdapter() {@Override public void windowClosing(WindowEvent event) {doClose();}});

		setIconImage(Images.MODEL.getImage());

		getContentPane().setLayout(new BorderLayout());

		/* Toolbar */
		getContentPane().add(toolbar=new JToolBar(),BorderLayout.NORTH);

		toolbar.add(closeButton=new JButton(Language.tr("Window.Viewer.CloseWindow")));
		closeButton.addActionListener(new ToolbarListener());
		closeButton.setToolTipText(Language.tr("Window.Viewer.CloseWindow.Info"));
		closeButton.setIcon(Images.GENERAL_EXIT.getIcon());

		toolbar.add(saveButton=new JButton(Language.tr("MainToolbar.Save")));
		saveButton.addActionListener(new ToolbarListener());
		saveButton.setToolTipText(Language.tr("MainToolbar.Save.Tooltip"));
		saveButton.setIcon(Images.GENERAL_SAVE.getIcon());

		if (allowLoadToEditor) {
			toolbar.add(loadButton=new JButton(Language.tr("Window.Viewer.LoadToEditor")));
			loadButton.addActionListener(new ToolbarListener());
			loadButton.setToolTipText(Language.tr("Window.Viewer.LoadToEditor.Tooltip"));
			loadButton.setIcon(Images.MODEL.getIcon());
		}

		if (statistic!=null) {
			toolbar.add(saveStatisticButton=new JButton(Language.tr("MainToolbar.SaveStatistic")));
			saveStatisticButton.addActionListener(new ToolbarListener());
			saveStatisticButton.setToolTipText(Language.tr("MainToolbar.SaveStatistic.Tooltip"));
			saveStatisticButton.setIcon(Images.STATISTICS_SAVE.getIcon());

			toolbar.addSeparator();

			toolbar.add(editorButton=new JButton(Language.tr("Window.Viewer.Tabs.Model")));
			editorButton.addActionListener(new ToolbarListener());
			editorButton.setToolTipText(Language.tr("Window.Viewer.Tabs.Model.Tooltip"));
			editorButton.setIcon(Images.MODEL.getIcon());
			editorButton.setSelected(true);

			toolbar.add(statistikButton=new JButton(Language.tr("MainToolbar.Results")));
			statistikButton.addActionListener(new ToolbarListener());
			statistikButton.setToolTipText(Language.tr("MainToolbar.Results.Tooltip"));
			statistikButton.setIcon(Images.STATISTICS.getIcon());
		}

		toolbar.addSeparator();

		toolbar.add(helpButton=new JButton(Language.tr("MainToolbar.Help")));
		helpButton.addActionListener(new ToolbarListener());
		helpButton.setToolTipText(Language.tr("MainToolbar.Help.Tooltip"));
		helpButton.setIcon(Images.HELP.getIcon());

		toolbar.setFloatable(false);

		getContentPane().add(tabs=new JPanel(new CardLayout()),BorderLayout.CENTER);

		tabs.add(modelPanel=new CallcenterModelEditorPanel(
				this,true,editModel,true,
				helpLink,
				null,null,null,null,null,null,null,null,null),Language.tr("Window.Viewer.Tabs.Model"));
		if (statistic!=null) {
			tabs.add(statisticPanel=new StatisticPanel(true,helpLink,helpLink.pageStatisticModal,null,null,1),Language.tr("MainToolbar.Results"));
			statisticPanel.setStatistic(new Statistics[]{statistic});
		}
		setGUIMode(0);

		addWindowListener(new WindowAdapter() {@Override
			public void windowClosing(WindowEvent event) {setVisible(false); dispose();}});
		SetupData setup=SetupData.getSetup();
		setSize((int)Math.round(750*setup.scaleGUI),(int)Math.round(550*setup.scaleGUI));
		setMinimumSize(getSize());
		setLocationRelativeTo(owner);
	}

	@Override
	protected JRootPane createRootPane() {
		JRootPane rootPane=new JRootPane();
		InputMap inputMap=rootPane.getInputMap(JComponent.WHEN_IN_FOCUSED_WINDOW);

		KeyStroke stroke=KeyStroke.getKeyStroke("ESCAPE");
		inputMap.put(stroke,"ESCAPE");
		rootPane.getActionMap().put("ESCAPE",new AbstractAction(){
			/**
			 * Serialisierungs-ID der Klasse
			 * @see Serializable
			 */
			private static final long serialVersionUID = -6894097779421071249L;
			@Override public void actionPerformed(ActionEvent e) {doClose();}
		});

		stroke=KeyStroke.getKeyStroke("F1");
		inputMap.put(stroke,"F1");
		rootPane.getActionMap().put("F1",new AbstractAction(){
			/**
			 * Serialisierungs-ID der Klasse
			 * @see Serializable
			 */
			private static final long serialVersionUID = -6894097779421071249L;
			@Override public void actionPerformed(ActionEvent e) {showHelp();}
		});

		return rootPane;
	}

	/**
	 * Wurde ausgewählt, dass das aktuell angezeigte Modell
	 * in den Editor geladen werden soll?
	 * @return	Callcenter-Modell in den Editor laden?
	 * @see #getCallcenterModel()
	 */
	public boolean getLoadModelToEditor() {
		return loadModelToEditor;
	}

	/**
	 * Wenn über {@link #getLoadModelToEditor()} angegeben
	 * wurde, dass das angezeigte Callcenter-Modell in den
	 * Editor geladen werden soll, so wird über diese
	 * Methode das Modell bereitgestellt.
	 * @return	Callcenter-Modell das in den Editor geladen werden soll
	 */
	public CallcenterModel getCallcenterModel() {
		return modelPanel.getModel(true);
	}

	/**
	 * Meldet zurück, dass das Fenster geschlossen wird, um die
	 * GUI des unterliegenden Fensters wieder zu aktivieren
	 * @param closeNotify	Objekt vom Type <code>Runnabble</code>, dessen <code>run</code>-Methode aufgerufen wird
	 */
	public void setCloseNotify(Runnable closeNotify) {
		this.closeNotify=closeNotify;
	}

	/**
	 * Befehl: Modell speichern.
	 */
	private void saveModel() {
		String s=modelPanel.saveModel(null);
		if (s!=null) MsgBox.error(this,Language.tr("Window.SaveModelError.Title"),s);
	}

	/**
	 * Stellt die Betriebsart des Dialogs ein.
	 * @param mode	Betriebsart (0: Modell-Editor, 1: Statistikansicht)
	 */
	private void setGUIMode(int mode) {
		if (editorButton!=null) {
			editorButton.setSelected(mode==0);
			statistikButton.setSelected(mode==1);

			saveButton.setVisible(mode==0);
			if (loadButton!=null) loadButton.setVisible(mode==0);
			saveStatisticButton.setVisible(mode==1);
		}

		String s=(mode==0)?Language.tr("Window.Viewer.Tabs.Model"):Language.tr("MainToolbar.Results");
		((CardLayout)tabs.getLayout()).show(tabs,s);
	}

	/**
	 * Befehl: Fenster schließen.
	 */

	private void doClose() {
		if (closeNotify!=null) closeNotify.run();
		setVisible(false);
		dispose();
	}

	/**
	 * Befehl: Hilfe aufrufen.
	 */
	private void showHelp() {
		if (editorButton.isSelected()) {
			switch (modelPanel.getSelectedTabIndex()) {
			case 0: helpLink.pageGeneralModal.run(); break;
			case 1: helpLink.pageCallerModal.run(); break;
			case 2: helpLink.pageCallcenterModal.run(); break;
			case 3: helpLink.pageSkillLevelModal.run(); break;
			case 4: helpLink.pageModelInformationModal.run(); break;
			}
		}
		if (statistikButton!=null && statistikButton.isSelected()) helpLink.pageStatisticModal.run();
	}

	/**
	 * Reagiert auf Klicks auf die Symbolleisten-Schaltflächen
	 */
	private class ToolbarListener implements ActionListener {
		/**
		 * Konstruktor der Klasse
		 */
		public ToolbarListener() {
			/*
			 * Wird nur benötigt, um einen JavaDoc-Kommentar für diesen (impliziten) Konstruktor
			 * setzen zu können, damit der JavaDoc-Compiler keine Warnung mehr ausgibt.
			 */
		}

		@Override
		public void actionPerformed(ActionEvent e) {
			if (e.getSource()==closeButton) {doClose(); return;}
			if (e.getSource()==saveButton) {saveModel(); return;}
			if (e.getSource()==loadButton) {loadModelToEditor=true; doClose(); return;}
			if (e.getSource()==saveStatisticButton) {
				String s=statisticPanel.saveStatistic(null);
				if (s!=null) MsgBox.error(CallcenterModelEditorPanelDialog.this,Language.tr("Window.SaveStatisticsError.Title"),s);
			}
			if (e.getSource()==editorButton) {setGUIMode(0); return;}
			if (e.getSource()==statistikButton) {setGUIMode(1); return;}
			if (e.getSource()==helpButton) {showHelp();	return;}
		}
	}
}
