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
import java.awt.Component;
import java.awt.FlowLayout;
import java.awt.Point;
import java.awt.Window;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.InputEvent;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.io.File;
import java.io.Serializable;
import java.text.ParseException;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;
import java.util.Properties;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.DefaultListModel;
import javax.swing.Icon;
import javax.swing.JButton;
import javax.swing.JCheckBoxMenuItem;
import javax.swing.JFormattedTextField.AbstractFormatter;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JMenuItem;
import javax.swing.JPanel;
import javax.swing.JPopupMenu;
import javax.swing.JScrollPane;
import javax.swing.JTabbedPane;
import javax.swing.JTextArea;
import javax.swing.JTextField;
import javax.swing.JToolBar;
import javax.swing.KeyStroke;
import javax.swing.SwingUtilities;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;

import org.jdatepicker.impl.JDatePanelImpl;
import org.jdatepicker.impl.JDatePickerImpl;
import org.jdatepicker.impl.UtilDateModel;
import org.w3c.dom.Element;

import language.Language;
import mathtools.NumberTools;
import mathtools.TimeTools;
import mathtools.distribution.DataDistributionImpl;
import mathtools.distribution.tools.DistributionTools;
import systemtools.MsgBox;
import systemtools.statistics.StatisticNode;
import ui.HelpLink;
import ui.VersionConst;
import ui.commandline.AbstractReportCommandConnect;
import ui.editor.events.RenameEvent;
import ui.editor.events.RenameListener;
import ui.images.Images;
import ui.model.CallcenterModel;
import ui.model.CallcenterModelAgent;
import ui.model.CallcenterModelCallcenter;
import ui.model.CallcenterModelCaller;
import ui.model.CallcenterModelSkillLevel;
import ui.statistic.core.StatisticBasePanel;
import ui.statistic.model.StatisticViewerAgentShiftPlanDiagram;
import ui.statistic.model.StatisticViewerAgentShiftPlanTable;
import ui.statistic.model.StatisticViewerAgentenPieChart;
import ui.statistic.model.StatisticViewerAgentsDiagram;
import ui.statistic.model.StatisticViewerAgentsTable;
import ui.statistic.model.StatisticViewerAgentsText;
import ui.statistic.model.StatisticViewerCallerDiagram;
import ui.statistic.model.StatisticViewerCallerPieChart;
import ui.statistic.model.StatisticViewerCallerTable;
import ui.statistic.model.StatisticViewerErlangCDiagramm;
import ui.statistic.model.StatisticViewerErlangCTable;
import ui.statistic.model.StatisticViewerErlangCTools;
import ui.statistic.model.StatisticViewerModelInformation;
import xml.XMLTools;

/**
 * Diese Klasse kapselt einen vollständigen Callcenter-Editor in Form eines einbettbaren JPanels.
 * @author Alexander Herzog
 * @version 1.0
 * @see CallcenterModel
 */
public class CallcenterModelEditorPanel extends JPanel implements AbstractReportCommandConnect {
	/**
	 * Serialisierungs-ID der Klasse
	 * @see Serializable
	 */
	private static final long serialVersionUID = 4413950001154670788L;

	/** Übergeordnetes Fenster */
	private final Window owner;
	/** Aktuell in Bearbeitung befindliches Modell */
	private CallcenterModel model;
	/** Unverändertes Originalmodell, um prüfen zu können, ob das Modell im Editor verändert wurde */
	private CallcenterModel modelOriginal;
	/** Wurde das Modell seit dem letzten Laden oder Speichern verändert */
	private boolean modelChanged;
	/** Editor im Read-only-Modus als reinen Betrachter aufrufen */
	private final boolean readOnly;
	/** Beim letzten Laden oder Speichern verwendeter Dateiname */
	private String lastFileName;

	/** Registerreiter für den Dialog */
	private JTabbedPane tabs;

	/* Dialogseite "Allgemeine Daten" */

	/** Eingabefeld für den Namen des Modells */
	private JTextField name;
	/** Eingabepanel für das Datum des Modelltages */
	private UtilDateModel dateModel;
	/** Eingabefeld für die Modellbeschreibung */
	private JTextArea description;
	/** Schaltfläche "Globale Parameter" */
	private JButton globalParameters;
	/** Schaltfläche "Schwellenwerte" */
	private JButton thresholdValues;
	/** Schaltfläche "Beschreibung automatisch erstellen" */
	private JButton generateDescription;

	/* Dialogseite "Anrufergruppen" */

	/** Datenmodell für die Listendarstellung der Anrufergruppen ({@link #callerList}) */
	private final DefaultListModel<CallcenterModelCaller> callerListData;
	/** Listendarstellung der Anrufergruppen */
	private final JList<CallcenterModelCaller> callerList;

	/* Dialogseite "Callcenter und Agenten" */

	/** Datenmodell für die Listendarstellung der Callcenter ({@link #callcenterList}) */
	private final DefaultListModel<CallcenterModelCallcenter> callcenterListData;
	/** Listendarstellung der Callcenter */
	private final JList<CallcenterModelCallcenter> callcenterList;

	/* Dialogseite "Skill-Level der Agenten" */

	/** Datenmodell für die Listendarstellung der Skill-Level ({@link #skillLevelList}) */
	private final DefaultListModel<CallcenterModelSkillLevel> skillLevelListData;
	/** Listendarstellung der Skill-Level */
	private final JList<CallcenterModelSkillLevel> skillLevelList;

	/* Dialogseite "Modellüberblick" */

	/** Anzeige der ohne Simulation ermittelbaren Daten (Modellüberblick) */
	private StatisticBasePanel statistics;

	/** Verknüpfung mit der Online-Hilfe */
	private final HelpLink helpLink;

	/** Wird hier ein Wert ungleich <code>null</code> übergeben, so wird die <code>Run</code>-Methode dieses Objekts zur Ausführung des Kunden-Generators (für 24 Intervalle) aufgerufen */
	private final Runnable generatoCallerCallback24;
	/** Wird hier ein Wert ungleich <code>null</code> übergeben, so wird die <code>Run</code>-Methode dieses Objekts zur Ausführung des Kunden-Generators (für 48 Intervalle) aufgerufen */
	private final Runnable generatoCallerCallback48;
	/** Wird hier ein Wert ungleich <code>null</code> übergeben, so wird die <code>Run</code>-Methode dieses Objekts zur Ausführung des Kunden-Generators (für 96 Intervalle) aufgerufen */
	private final Runnable generatoCallerCallback96;
	/** Wird hier ein Wert ungleich <code>null</code> übergeben, so wird die <code>Run</code>-Methode dieses Objekts zur Ausführung des Agenten-Generators (für 24 Intervalle) aufgerufen */
	private final Runnable generatorAgentsCallback24;
	/** Wird hier ein Wert ungleich <code>null</code> übergeben, so wird die <code>Run</code>-Methode dieses Objekts zur Ausführung des Agenten-Generators (für 48 Intervalle) aufgerufen */
	private final Runnable generatorAgentsCallback48;
	/** Wird hier ein Wert ungleich <code>null</code> übergeben, so wird die <code>Run</code>-Methode dieses Objekts zur Ausführung des Agenten-Generators (für 96 Intervalle) aufgerufen */
	private final Runnable generatorAgentsCallback96;
	/** Wird hier ein Wert ungleich <code>null</code> übergeben, so wird die <code>Run</code>-Methode dieses Objekts zur Ausführung des Agenten-Produktivitäts-Generators aufgerufen */
	private final Runnable generatorAgentsEfficiencyCallback;
	/** Wird hier ein Wert ungleich <code>null</code> übergeben, so wird die <code>Run</code>-Methode dieses Objekts zur Ausführung des Agenten-Zuschlag-Generators aufgerufen */
	private final Runnable generatorAgentsAdditionCallback;
	/** Wird hier ein Wert ungleich <code>null</code> übergeben, so wird die <code>Run</code>-Methode dieses Objekts zur Ausführung des Skills-Generators aufgerufen */
	private final Runnable generatorSkillsCallback;

	/**
	 * Konstruktor der Klasse <code>CallcenterModelEditorPanel</code>
	 * @param owner	Übergeordnetes Fenster
	 * @param withTabs	Tabs zum Wechseln der Kategorien anzeigen
	 * @param model	Referenz auf das zu bearbeitende Modell. Alle Änderungen erfolgen an einer lokalen Kopie das Original wird nicht verändert.
	 * @param readOnly	Editor im Read-only-Modus als reinen Betrachter aufrufen
	 * @param helpLink Verknüpfung mit der Online-Hilfe
	 * @param generatoCallerCallback24 Wird hier ein Wert ungleich <code>null</code> übergeben, so wird die <code>Run</code>-Methode dieses Objekts zur Ausführung des Kunden-Generators (für 24 Intervalle) aufgerufen
	 * @param generatoCallerCallback48 Wird hier ein Wert ungleich <code>null</code> übergeben, so wird die <code>Run</code>-Methode dieses Objekts zur Ausführung des Kunden-Generators (für 48 Intervalle) aufgerufen
	 * @param generatoCallerCallback96 Wird hier ein Wert ungleich <code>null</code> übergeben, so wird die <code>Run</code>-Methode dieses Objekts zur Ausführung des Kunden-Generators (für 96 Intervalle) aufgerufen
	 * @param generatorAgentsCallback24 Wird hier ein Wert ungleich <code>null</code> übergeben, so wird die <code>Run</code>-Methode dieses Objekts zur Ausführung des Agenten-Generators (für 24 Intervalle) aufgerufen
	 * @param generatorAgentsCallback48 Wird hier ein Wert ungleich <code>null</code> übergeben, so wird die <code>Run</code>-Methode dieses Objekts zur Ausführung des Agenten-Generators (für 48 Intervalle) aufgerufen
	 * @param generatorAgentsCallback96 Wird hier ein Wert ungleich <code>null</code> übergeben, so wird die <code>Run</code>-Methode dieses Objekts zur Ausführung des Agenten-Generators (für 96 Intervalle) aufgerufen
	 * @param generatorAgentsEfficiencyCallback Wird hier ein Wert ungleich <code>null</code> übergeben, so wird die <code>Run</code>-Methode dieses Objekts zur Ausführung des Agenten-Produktivitäts-Generators aufgerufen
	 * @param generatorAgentsAdditionCallback  Wird hier ein Wert ungleich <code>null</code> übergeben, so wird die <code>Run</code>-Methode dieses Objekts zur Ausführung des Agenten-Zuschlag-Generators aufgerufen
	 * @param generatorSkillsCallback  Wird hier ein Wert ungleich <code>null</code> übergeben, so wird die <code>Run</code>-Methode dieses Objekts zur Ausführung des Skills-Generators aufgerufen
	 * @see #getModel(boolean)
	 */
	public CallcenterModelEditorPanel(Window owner, boolean withTabs, CallcenterModel model, boolean readOnly, HelpLink helpLink, Runnable generatoCallerCallback24, Runnable generatoCallerCallback48, Runnable generatoCallerCallback96, Runnable generatorAgentsCallback24, Runnable generatorAgentsCallback48, Runnable generatorAgentsCallback96, Runnable generatorAgentsEfficiencyCallback, Runnable generatorAgentsAdditionCallback, Runnable generatorSkillsCallback) {
		super(new BorderLayout());
		if (model==null) model=new CallcenterModel();
		mutexGetSetModel.lock();
		try {
			this.model=model.clone();
		} finally {
			mutexGetSetModel.unlock();
		}
		this.owner=owner;
		this.readOnly=readOnly;
		this.helpLink=helpLink;
		this.generatoCallerCallback24=generatoCallerCallback24;
		this.generatoCallerCallback48=generatoCallerCallback48;
		this.generatoCallerCallback96=generatoCallerCallback96;
		this.generatorAgentsCallback24=generatorAgentsCallback24;
		this.generatorAgentsCallback48=generatorAgentsCallback48;
		this.generatorAgentsCallback96=generatorAgentsCallback96;
		this.generatorAgentsEfficiencyCallback=generatorAgentsEfficiencyCallback;
		this.generatorAgentsAdditionCallback=generatorAgentsAdditionCallback;
		this.generatorSkillsCallback=generatorSkillsCallback;
		lastFileName="";

		JPanel p;
		ButtonActionListener a;

		if (withTabs) {
			add(tabs=new JTabbedPane());
			tabs.setTabLayoutPolicy(JTabbedPane.SCROLL_TAB_LAYOUT);
			tabs.addChangeListener(e->{if (tabs.getSelectedIndex()==tabs.getTabCount()-1) updateStatistics();});
		} else {
			setLayout(new CardLayout());
		}

		if (withTabs) {
			tabs.addTab(Language.tr("Editor.GeneralData"),p=new JPanel());
		} else {
			add(p=new JPanel(),"0");

		}
		buildGeneralTab(p);

		if (withTabs) {
			tabs.addTab(Language.tr("Editor.CallerGroups"),p=new JPanel(new BorderLayout()));
		} else {
			add(p=new JPanel(new BorderLayout()),"1");
		}
		a=addEditPanel(
				p,0,
				Images.EDITOR_CALLER_ADD.getIcon(),Images.EDITOR_CALLER_EDIT.getIcon(),Images.EDITOR_CALLER_DELETE.getIcon(),Images.EDITOR_CALLER_COPY.getIcon(),Images.ARROW_UP.getIcon(),Images.ARROW_DOWN.getIcon(),Images.GENERAL_SETUP.getIcon(),
				Language.tr("Editor.CallerGroups.Add.Info"),Language.tr("Editor.CallerGroups.Show.Info"),Language.tr("Editor.CallerGroups.Edit.Info"),Language.tr("Editor.CallerGroups.Delete.Info"),Language.tr("Editor.CallerGroups.Copy.Info"),Language.tr("Editor.CallerGroups.Up.Info"),Language.tr("Editor.CallerGroups.Down.Info")
				);
		callerList=new JList<CallcenterModelCaller>(callerListData=new DefaultListModel<CallcenterModelCaller>());
		callerList.setCellRenderer(new CallerListRenderer());
		addList(p,callerList,a);
		updateCallerList();

		if (withTabs) {
			tabs.addTab(Language.tr("Editor.CallcenterAndAgents"),p=new JPanel(new BorderLayout()));
		} else {
			add(p=new JPanel(new BorderLayout()),"2");
		}
		a=addEditPanel(
				p,1,
				Images.EDIT_ADD.getIcon(),Images.GENERAL_TOOLS.getIcon(),Images.EDIT_DELETE.getIcon(),Images.EDIT_COPY.getIcon(),Images.ARROW_UP.getIcon(),Images.ARROW_DOWN.getIcon(),Images.GENERAL_SETUP.getIcon(),
				Language.tr("Editor.CallcenterAndAgents.Add.Info"),Language.tr("Editor.CallcenterAndAgents.Show.Info"),Language.tr("Editor.CallcenterAndAgents.Edit.Info"),Language.tr("Editor.CallcenterAndAgents.Delete.Info"),Language.tr("Editor.CallcenterAndAgents.Copy.Info"),Language.tr("Editor.CallcenterAndAgents.Up.Info"),Language.tr("Editor.CallcenterAndAgents.Down.Info")
				);
		callcenterList=new JList<CallcenterModelCallcenter>(callcenterListData=new DefaultListModel<CallcenterModelCallcenter>());
		callcenterList.setCellRenderer(new CallcenterListRenderer());
		addList(p,callcenterList,a);
		updateCallcenterList();

		if (withTabs) {
			tabs.addTab(Language.tr("Editor.SkillLevelOfTheAgents"),p=new JPanel(new BorderLayout()));
		} else {
			add(p=new JPanel(new BorderLayout()),"3");
		}
		a=addEditPanel(
				p,2,
				Images.EDIT_ADD.getIcon(),Images.GENERAL_TOOLS.getIcon(),Images.EDIT_DELETE.getIcon(),Images.EDIT_COPY.getIcon(),Images.ARROW_UP.getIcon(),Images.ARROW_DOWN.getIcon(),Images.GENERAL_SETUP.getIcon(),
				Language.tr("Editor.SkillLevelOfTheAgents.Add.Info"),Language.tr("Editor.SkillLevelOfTheAgents.Show.Info"),Language.tr("Editor.SkillLevelOfTheAgents.Edit.Info"),Language.tr("Editor.SkillLevelOfTheAgents.Delete.Info"),Language.tr("Editor.SkillLevelOfTheAgents.Copy.Info"),Language.tr("Editor.SkillLevelOfTheAgents.Up.Info"),Language.tr("Editor.SkillLevelOfTheAgents.Down.Info")
				);
		skillLevelList=new JList<CallcenterModelSkillLevel>(skillLevelListData=new DefaultListModel<CallcenterModelSkillLevel>());
		skillLevelList.setCellRenderer(new SkillLevelListRenderer());
		addList(p,skillLevelList,a);
		updateSkillLevelList();

		if (withTabs) {
			tabs.addTab(Language.tr("Editor.ModelOverview"),p=new JPanel(new BorderLayout()));
		} else {
			add(p=new JPanel(new BorderLayout()),"4");
		}
		p.add(statistics=new CallcenterModelInfoPanel(helpLink));

		if (withTabs) {
			tabs.setIconAt(0,Images.GENERAL_SETUP.getIcon());
			tabs.setIconAt(1,Images.EDITOR_CALLER.getIcon());
			tabs.setIconAt(2,Images.EDITOR_CALLCENTER.getIcon());
			tabs.setIconAt(3,Images.EDITOR_SKILLLEVEL.getIcon());
			tabs.setIconAt(4,Images.GENERAL_INFO.getIcon());
		}

		setModel(model);
	}

	/**
	 * Formatierung der Darstellung in {@link CallcenterModelEditorPanel#dateModel}
	 * @see CallcenterModelEditorPanel#dateModel
	 */
	private class DateLabelFormatter extends AbstractFormatter {
		/**
		 * Serialisierungs-ID der Klasse
		 * @see Serializable
		 */
		private static final long serialVersionUID = -6177382334742454499L;
		@Override public Object stringToValue(String text) {return CallcenterModel.stringToDate(text);}
		@Override public String valueToString(Object value) throws ParseException {return (value!=null && (value instanceof Calendar))?CallcenterModel.dateToLocalString((Calendar)value):"";}
	}

	/**
	 * Erzeugt die Dialogseite "Allgemeine Daten"
	 * @param mainarea	Hauptpanel
	 */
	private void buildGeneralTab(final JPanel mainarea) {
		JPanel p,p2;

		mainarea.setLayout(new BorderLayout());
		mainarea.setBorder(BorderFactory.createEmptyBorder(5,5,5,5));

		mainarea.add(p=new JPanel(),BorderLayout.NORTH);
		p.setLayout(new BoxLayout(p,BoxLayout.X_AXIS));
		p.add(new JLabel(Language.tr("Editor.GeneralData.Name")+":"));
		p.add(Box.createHorizontalStrut(5));
		mutexGetSetModel.lock();
		try {
			p.add(name=new JTextField(model.name,50));
		} finally {
			mutexGetSetModel.unlock();
		}
		p.add(Box.createHorizontalStrut(5));

		p.add(new JLabel(Language.tr("Editor.GeneralData.Date")+":"));
		p.add(Box.createHorizontalStrut(5));
		Properties i18nStrings=new Properties();
		i18nStrings.setProperty("text.today",Language.tr("Statistic.Units.Today"));
		i18nStrings.setProperty("text.month",Language.tr("Statistic.Units.Month"));
		i18nStrings.setProperty("text.year",Language.tr("Statistic.Units.Year"));
		dateModel=new UtilDateModel();
		JDatePanelImpl datePanel=new JDatePanelImpl(dateModel,i18nStrings);
		p.add(new JDatePickerImpl(datePanel,new DateLabelFormatter()));

		mainarea.add(p=new JPanel(),BorderLayout.CENTER);
		p.setLayout(new BoxLayout(p,BoxLayout.Y_AXIS));
		p.setAlignmentX(Component.LEFT_ALIGNMENT);
		p.add(Box.createVerticalStrut(20));
		p.add(p2=new JPanel());
		p2.setLayout(new BoxLayout(p2,BoxLayout.X_AXIS));

		p2.add(new JLabel(Language.tr("Editor.GeneralData.Description")));
		p2.add(Box.createHorizontalGlue());
		p.add(Box.createVerticalStrut(2));
		mutexGetSetModel.lock();
		try {
			p.add(new JScrollPane(description=new JTextArea(model.description,9,30)));
		} finally {
			mutexGetSetModel.unlock();
		}
		p.add(Box.createVerticalStrut(5));

		mainarea.add(p=new JPanel(new FlowLayout(FlowLayout.LEFT)),BorderLayout.SOUTH);

		p.add(globalParameters=new JButton(Language.tr("Editor.GeneralData.GlobalParameters")));
		globalParameters.addActionListener(new ButtonActionListener(0));
		globalParameters.setIcon(Images.GENERAL_SETUP.getIcon());

		p.add(thresholdValues=new JButton(Language.tr("Editor.GeneralData.ThresholdValues")));
		thresholdValues.addActionListener(new ButtonActionListener(0));
		thresholdValues.setIcon(Images.EDITOR_THRESHOLD.getIcon());

		p.add(generateDescription=new JButton(Language.tr("Editor.GeneralData.CreateDescription")));
		generateDescription.addActionListener(new ButtonActionListener(0));
		generateDescription.setIcon(Images.GENERAL_INFO.getIcon());

		name.setEditable(!readOnly);
		description.setEditable(!readOnly);
	}

	/**
	 * Erzeugt eine Symbolleiste über eine Liste
	 * @param parent	Übergeordnetes Element für die Symbolleiste
	 * @param actionListenerNr	Nummer unter der sich die Schaltflächen in dem Button-Listener melden
	 * @param iconAdd	Icon "Hinzufügen"
	 * @param iconEdit	Icon "Bearbeiten"
	 * @param iconDelete	Icon "Löschen"
	 * @param iconCopy	Icon "Kopieren"
	 * @param iconUp	Icon "Nach oben verschieben"
	 * @param iconDown	Icon "Nach unten verschieben"
	 * @param iconTools	Icon "Tools"
	 * @param addTooltip	Tooltip für die "Hinzufügen"-Schaltfläche
	 * @param showTooltip	Tooltip für die "Anzeigen"-Schaltfläche (vergleichbar mit "Bearbeiten", aber im Nur-Lese-Modus)
	 * @param editTooltip	Tooltip für die "Bearbeiten"-Schaltfläche
	 * @param deleteTooltip	Tooltip für die "Löschen"-Schaltfläche
	 * @param copyTooltip	Tooltip für die "Kopieren"-Schaltfläche
	 * @param upTooltip	Tooltip für die "Nach oben verschieben"-Schaltfläche
	 * @param downTooltip	Tooltip für die "Nach unten verschieben"-Schaltfläche
	 * @return	Button-Listener der auf die Schaltflächen reagiert
	 */
	private ButtonActionListener addEditPanel(JPanel parent, int actionListenerNr, Icon iconAdd, Icon iconEdit, Icon iconDelete, Icon iconCopy, Icon iconUp, Icon iconDown, Icon iconTools, String addTooltip, String showTooltip, String editTooltip, String deleteTooltip, String copyTooltip, String upTooltip, String downTooltip) {
		JToolBar p;
		final JButton[] buttons=new JButton[7];
		final ButtonActionListener listener=new ButtonActionListener(actionListenerNr);

		parent.add(p=new JToolBar(),BorderLayout.NORTH);
		p.setFloatable(false);

		p.add(buttons[0]=new JButton(Language.tr("Dialog.Button.Add")));
		buttons[0].setToolTipText(addTooltip);
		buttons[0].addActionListener(listener);

		p.add(buttons[1]=new JButton(readOnly?Language.tr("Dialog.Button.View"):Language.tr("Dialog.Button.Edit")));
		buttons[1].setToolTipText(readOnly?showTooltip:editTooltip);
		buttons[1].addActionListener(listener);

		p.add(buttons[2]=new JButton(Language.tr("Dialog.Button.Delete")));
		buttons[2].setToolTipText(deleteTooltip);
		buttons[2].addActionListener(listener);

		p.add(buttons[3]=new JButton(Language.tr("Dialog.Button.Copy")));
		buttons[3].setToolTipText(copyTooltip);
		buttons[3].addActionListener(listener);

		p.addSeparator();

		p.add(buttons[4]=new JButton(Language.tr("Dialog.Button.Up")));
		buttons[4].setToolTipText(upTooltip);
		buttons[4].addActionListener(listener);

		p.add(buttons[5]=new JButton(Language.tr("Dialog.Button.Down")));
		buttons[5].setToolTipText(downTooltip);
		buttons[5].addActionListener(listener);

		if (iconTools!=null) {
			p.addSeparator();
			p.add(buttons[6]=new JButton(Language.tr("Dialog.Button.Tools"))); buttons[6].addActionListener(listener);
		} else buttons[6]=null;
		listener.buttons=buttons;

		if (iconAdd!=null) buttons[0].setIcon(iconAdd);
		if (iconEdit!=null) buttons[1].setIcon(iconEdit);
		if (iconDelete!=null) buttons[2].setIcon(iconDelete);
		if (iconCopy!=null) buttons[3].setIcon(iconCopy);
		if (iconUp!=null) buttons[4].setIcon(iconUp);
		if (iconDown!=null) buttons[5].setIcon(iconDown);
		if (iconTools!=null) buttons[6].setIcon(iconTools);

		if (readOnly) buttons[0].setEnabled(false);
		buttons[1].setEnabled(false);
		buttons[2].setEnabled(false);
		buttons[3].setEnabled(false);
		buttons[4].setEnabled(false);
		buttons[5].setEnabled(false);
		if (readOnly && iconTools!=null) buttons[6].setEnabled(false);

		return listener;
	}

	/**
	 * Erstellt ein Listenfeld.
	 * @param parent	Übergeordnetes Element für das Listenfeld
	 * @param list	Darzustellende Liste
	 * @param actionListener	Listener der auf die verschiedenen Listen-Aktionen reagieren soll
	 */
	private void addList(JPanel parent, @SuppressWarnings("rawtypes") JList list, ButtonActionListener actionListener) {
		parent.add(new JScrollPane(list),BorderLayout.CENTER);
		ListListener mouseKeySelectionListener=new ListListener(list,actionListener.buttons,actionListener);
		list.addMouseListener(mouseKeySelectionListener);
		list.addKeyListener(mouseKeySelectionListener);
		list.addListSelectionListener(mouseKeySelectionListener);
	}


	/**
	 * Stellt sicher, dass keine überlappenden Set- und Get-Operationen
	 * in Bezug auf das Modell erfolgen.
	 * @see #setModel(CallcenterModel)
	 * @see #getModel(boolean)
	 */
	private final Lock mutexGetSetModel=new ReentrantLock();

	/**
	 * Liefert eine Kopie des momentan in Bearbeitung befindlichen Modells zurück<br>
	 * (nicht eine Referenz auf das tatsächliche Modell, d.h. an dem zurückgegebenen Modell
	 * können beliebige Veränderungen vorgenommen werden, ohne dass sich das Modell im Editor daruch ändert.)
	 * @param	cloneModel	Gibt an, ob das Modell im Original (zur kurzfristigen read-only Verwendung) (<code>false</code>) oder als Kopie (<code>true</code>) übergeben werden soll.
	 * @return	Objekt vom Typ <code>CallcenterModell</code> aus dem Editor
	 * @see #setModel(CallcenterModel)
	 */
	public CallcenterModel getModel(final boolean cloneModel) {
		mutexGetSetModel.lock();
		try {
			model.name=name.getText();
			model.description=description.getText();
			model.date=CallcenterModel.dateToString(dateModel.getValue());

			if (!modelChanged) modelChanged=(modelOriginal!=null) && !modelOriginal.equalsCallcenterModel(model);

			return cloneModel?model.clone():model;
		} finally {
			mutexGetSetModel.unlock();
		}
	}

	/**
	 * Ersetzt das momentane Modell im Editor durch eine Kopie des übergebenen Modells.<br>
	 * (Im Editor wird mit einer lokalen Kopie gearbeitet, d.h. das übergebene Objekt wird nicht verändert.)
	 * @param newModel	Neues Callcenter-Modell für den Editor
	 * @see #getModel(boolean)
	 */
	public void setModel(CallcenterModel newModel) {
		mutexGetSetModel.lock();
		try {
			model=newModel.clone();
			modelOriginal=newModel.clone();
			modelChanged=false;

			name.setText(model.name);
			dateModel.setValue(CallcenterModel.stringToDate(model.date));
			description.setText(model.description);
			description.setSelectionStart(0);
			description.setSelectionEnd(0);

			callerList.setSelectedIndex(-1);
			updateCallerList();
			callcenterList.setSelectedIndex(-1);
			updateCallcenterList();
			skillLevelList.setSelectedIndex(-1);
			updateSkillLevelList();

			updateStatistics();

			lastFileName="";
			statistics.setDataFileName(null);
		} finally {
			mutexGetSetModel.unlock();
		}
	}

	/**
	 * Speichert das Modell in der angegebenen Datei. Wird <code>null</code> als Parameter angegeben, so wird zunächst ein Dateiauswahldialog angezeigt.
	 * @param file	Dateiname der Zieldatei (kann auch <code>null</code> sein; in diesem Fall fragt die Methode per Dialog selbst nach dem Dateinamen.)
	 * @return Gibt <code>null</code> zurück, wenn das Modell erfolgreich gespeichert wurde. Andernfalls wird die Fehlermeldung als String zurückgegeben.
	 */
	public String saveModel(File file) {
		CallcenterModel model=getModel(false);

		if (file==null) {
			file=XMLTools.showSaveDialog(getParent(),Language.tr("Editor.Save.Title"));
			if (file==null) return null;
		}

		if (file.exists() && (lastFileName==null || !lastFileName.equalsIgnoreCase(file.toString()))) {
			if (!MsgBox.confirmOverwrite(this,file)) return null;
		}

		if (!model.saveToFile(file)) return String.format(Language.tr("Editor.Save.Error"),file.toString());
		lastFileName=file.toString();
		mutexGetSetModel.lock();
		try {
			modelOriginal=model.clone();
			modelChanged=false;
		} finally {
			mutexGetSetModel.unlock();
		}
		statistics.setDataFileName(file.toString());
		return null;
	}

	/**
	 * Lädt das Modell aus der angegebenen Datei. Wird <code>null</code> als Parameter angegeben, so wird zunächst ein Dateiauswahldialog angezeigt.
	 * @param file	Dateiname der Quelldatei (kann auch <code>null</code> sein; in diesem Fall fragt die Methode per Dialog selbst nach dem Dateinamen.)
	 * @return Gibt <code>null</code> zurück, wenn das Modell erfolgreich geladen wurde. Andernfalls wird die Fehlermeldung als String zurückgegeben.
	 */
	public String loadModel(File file) {
		if (file==null) {
			file=XMLTools.showLoadDialog(getParent(),Language.tr("Editor.Load.Title"));
			if (file==null) return null;
		}

		CallcenterModel newModel=new CallcenterModel();
		String s=newModel.loadFromFile(file); if (s!=null) return s;
		if (VersionConst.isNewerVersion(newModel.version)) {
			MsgBox.warning(this,Language.tr("Editor.Load.NewVersionWarning.Title"),String.format(Language.tr("Editor.Load.NewVersionWarning.Info"),newModel.version));
		}

		setModel(newModel);
		lastFileName=file.toString();
		statistics.setDataFileName(file.toString());
		return null;
	}

	/**
	 * Lädt das Modell aus dem angegebenen root Element.
	 * @param root	XML-Root-Element
	 * @param fileName	Dateiname der Quelldatei
	 * @return Gibt <code>null</code> zurück, wenn das Modell erfolgreich geladen wurde. Andernfalls wird die Fehlermeldung als String zurückgegeben.
	 */
	public String loadModel(Element root, String fileName) {
		CallcenterModel newModel=new CallcenterModel();
		String s=newModel.loadFromXML(root); if (s!=null) return s;
		if (VersionConst.isNewerVersion(newModel.version)) {
			MsgBox.warning(this,Language.tr("Editor.Load.NewVersionWarning.Title"),String.format(Language.tr("Editor.Load.NewVersionWarning.Info"),newModel.version));
		}

		setModel(newModel);
		lastFileName=fileName;
		statistics.setDataFileName(fileName);
		return null;
	}

	/**
	 * Setzt den zuletzt beim Speichern verwendeten Namen.<br>
	 * (Diese Methode muss nicht beim Speichern manuell aufgerufen werden, sie dient nur dazu, die Serialisierung des Panels zu ermöglichen.)
	 * @param lastFileName	Neuer Name, der als zuletzt verwendeter Name angegeben werden soll.
	 */
	public void setLastFileName(String lastFileName) {this.lastFileName=lastFileName;}

	/**
	 * Liefert den zuletzt beim Laden oder Speichern des Modells verwendeten Dateinamen.<br>
	 * Wurde das Modell zuletzt per <code>setModel</code> ersetzt, so wird ein leerer String zurückgegeben.
	 * @return Zuletzt verwendeter Dateiname
	 */
	public String getLastFileName() {return lastFileName;}

	/**
	 * Gibt an, ob das Modell seit dem letzten Speichern verändert wurde.
	 * @return Liefert <code>true</code> zurück, wenn das Modell geändert wurde.
	 */
	public boolean isModelChanged() {getModel(false); return modelChanged;}

	/**
	 * Setzt den Geändert-Status des Modells. Diese Funktion muss eigentlich nie aufgerufen werden; wird das Modell
	 * im Editor geändert, so wird es automatisch auf "geändert" gesetzt und beim Speichern wird "geändert" automatisch zurückgesetzt.
	 * @param changed	Gibt an, ob das Modell als seit dem letzten Speichern geändert angesehen werden soll oder nicht.
	 */
	public void setModelChanged(boolean changed) {modelChanged=changed;}

	/**
	 * Aktualisiert die Anrufergruppen-Liste
	 * @see #callerListData
	 * @see #callerList
	 */
	private void updateCallerList() {
		int index=callerList.getSelectedIndex();
		callerListData.clear();
		for (int i=0;i<model.caller.size();i++) callerListData.addElement(model.caller.get(i));
		if (index<callerListData.getSize()) callerList.setSelectedIndex(index); else {
			if (callerListData.getSize()>0) callerList.setSelectedIndex(0);
		}
	}

	/**
	 * Liefert zurück, welche Seite im Modell-Editor momentan angezeigt wird.
	 * @return	Nummer der aktiven Seiten, 0-basierend.
	 */
	public int getSelectedTabIndex() {
		if (tabs==null) {
			for (int i=0;i<getComponentCount();i++) if (getComponent(i).isVisible()) return i;
			return 0;
		} else {
			return tabs.getSelectedIndex();
		}
	}

	/**
	 * Stellt ein, welche Seite im Modell-Editor angezeigt werden soll.
	 * @param	index	Nummer der Seiten, 0-basierend.
	 * @return	Gibt <code>true</code> zurück, wenn der Parameter im gültigen Bereich lag.
	 */
	public boolean setSelectedTabIndex(int index) {
		if (tabs==null) {
			if (index<0 || index>=getComponents().length) return false;
			CardLayout layout=(CardLayout)getLayout();
			layout.show(this,""+index);
			if (index==getComponents().length-1) updateStatistics();
			return true;
		} else {
			if (index<0 || index>=tabs.getComponents().length) return false;
			tabs.setSelectedIndex(index);
			if (tabs.getSelectedIndex()==tabs.getTabCount()-1) updateStatistics();
			return true;
		}
	}

	/**
	 * Liefert zurück, welches Callcenter in der Liste der Callcenter momentan ausgewählt ist.
	 * @return	Nummer des selektierten Callcenters, 0-basierend.
	 */
	public int getSelectedCallcenterIndex() {
		return callcenterList.getSelectedIndex();
	}

	/**
	 * Aktualisiert die Callcenter-Liste
	 * @see #callcenterListData
	 * @see #callcenterList
	 */
	private void updateCallcenterList() {
		int index=callcenterList.getSelectedIndex();
		callcenterListData.clear();
		for (int i=0;i<model.callcenter.size();i++) callcenterListData.addElement(model.callcenter.get(i));
		if (index<callcenterListData.getSize()) callcenterList.setSelectedIndex(index); else {
			if (callcenterListData.getSize()>0) callcenterList.setSelectedIndex(0);
		}
	}

	/**
	 * Aktualisiert die Callcenter-Liste
	 * @see #skillLevelListData
	 * @see #skillLevelList
	 */
	private void updateSkillLevelList() {
		int index=skillLevelList.getSelectedIndex();
		skillLevelListData.clear();
		for (int i=0;i<model.skills.size();i++) {
			CallcenterModelSkillLevel c=model.skills.get(i);
			skillLevelListData.addElement(c);
		}
		if (index<skillLevelListData.getSize()) skillLevelList.setSelectedIndex(index); else {
			if (skillLevelListData.getSize()>0) skillLevelList.setSelectedIndex(0);
		}
	}

	/**
	 * Liefert eine Liste aller Anrufergruppennamen.
	 * @param addNewName	Wird ein Wert ungleich <code>null</code> übergeben, so wird dieser Wert an die Ausgabeliste angehängt
	 * @return	Liste aller Anrufergruppennamen
	 */
	private String[] callerNames(String addNewName) {
		String[] callerTypeNames=new String[model.caller.size()+((addNewName!=null)?1:0)];
		for (int i=0;i<model.caller.size();i++) callerTypeNames[i]=model.caller.get(i).name;
		if (addNewName!=null) callerTypeNames[callerTypeNames.length-1]=addNewName;
		return callerTypeNames;
	}

	/**
	 * Liefert eine Liste aller Skill-Level-Namen.
	 * @return	Liste aller Skill-Level-Namen
	 */
	final private String[] skillNames() {
		String[] skills=new String[model.skills.size()];
		for (int i=0;i<model.skills.size();i++) skills[i]=model.skills.get(i).name;
		return skills;
	}

	/**
	 * Befehl: Anrufergruppe hinzufügen
	 * @see #callerList
	 */
	private void addCaller() {
		CallcenterModelCaller caller=new CallcenterModelCaller();

		/* Eindeutigen Namen für neue Anrufergruppe bestimmen */
		String callerName=caller.name;
		int nr=0;
		boolean ok;
		do {
			ok=true;
			for (int i=0;i<model.caller.size();i++) if (model.caller.get(i).name.equalsIgnoreCase(callerName)) {ok=false; break;}
			if (!ok) {nr++; callerName=caller.name+" "+nr;}
		} while (!ok);
		caller.name=callerName;

		/* Dialog anzeigen */
		CallerEditDialog dialog=new CallerEditDialog(owner,model.serviceLevelSeconds,caller,model.caller.toArray(new CallcenterModelCaller[0]),callerNames(caller.name),skillNames(),false,false,false,helpLink.dialogCaller);
		dialog.setVisible(true);
		if (dialog.getClosedBy()!=BaseEditDialog.CLOSED_BY_OK) return;
		model.caller.add(caller);
		updateCallerList();
		callerList.setSelectedIndex(callerListData.getSize()-1);

		switch (dialog.isOpenGenerator()) {
		case 24: if (generatoCallerCallback24!=null) SwingUtilities.invokeLater(generatoCallerCallback24); break;
		case 48: if (generatoCallerCallback48!=null) SwingUtilities.invokeLater(generatoCallerCallback48); break;
		case 96: if (generatoCallerCallback96!=null) SwingUtilities.invokeLater(generatoCallerCallback96); break;
		}
	}

	/**
	 * Befehl: Anrufergruppe bearbeiten
	 * @see #callerList
	 */
	private void editCaller() {
		if (callerList.getSelectedIndex()<0) return;
		int index=callerList.getSelectedIndex();
		CallcenterModelCaller caller;

		int editCount=0;
		int lastTab=-1;
		Point lastLocation=null;
		int lastClose=BaseEditDialog.CLOSED_BY_PREVIOUS;
		CallerEditDialog dialog=null;
		while (lastClose==BaseEditDialog.CLOSED_BY_PREVIOUS || lastClose==BaseEditDialog.CLOSED_BY_NEXT) {
			caller=model.caller.get(index);
			dialog=new CallerEditDialog(owner,model.serviceLevelSeconds,caller,model.caller.toArray(new CallcenterModelCaller[0]),callerNames(null),skillNames(),readOnly,index>0,index<callerListData.size()-1,helpLink.dialogCaller);
			dialog.addCallerTypeRenameListener(new CallerTypeRenameListener());
			dialog.setTabIndex(lastTab);
			if (lastLocation!=null) dialog.setLocation(lastLocation);
			dialog.setVisible(true);
			lastTab=dialog.getTabIndex();
			lastLocation=dialog.getLocation();
			lastClose=dialog.getClosedBy();
			switch (lastClose) {
			case BaseEditDialog.CLOSED_BY_OK: editCount++; break;
			case BaseEditDialog.CLOSED_BY_PREVIOUS: editCount++; index--; break;
			case BaseEditDialog.CLOSED_BY_NEXT: editCount++; index++; break;
			}
		}
		if (editCount>0) {
			updateCallerList();
			if (dialog!=null) switch (dialog.isOpenGenerator()) {
			case 24: if (generatoCallerCallback24!=null) SwingUtilities.invokeLater(generatoCallerCallback24); break;
			case 48: if (generatoCallerCallback48!=null) SwingUtilities.invokeLater(generatoCallerCallback48); break;
			case 96: if (generatoCallerCallback96!=null) SwingUtilities.invokeLater(generatoCallerCallback96); break;
			}
		}
	}

	/**
	 * Befehl: Anrufergruppe löschen
	 * @see #callerList
	 */
	private void delCaller() {
		if (callerList.getSelectedIndex()<0) return;
		if (!MsgBox.confirm(this,Language.tr("Editor.Caller.DeleteGroup.Title"),
				String.format(Language.tr("Editor.Caller.DeleteGroup.Info"),model.caller.get(callerList.getSelectedIndex()).name),
				Language.tr("Editor.Caller.DeleteGroup.Yes.Info"),
				Language.tr("Editor.Caller.DeleteGroup.No.Info"))
				) return;
		CallerTypeRenameListener listener=new CallerTypeRenameListener();
		CallcenterModelCaller caller=model.caller.get(callerList.getSelectedIndex());
		RenameEvent event=new RenameEvent(caller,caller.name,null);
		listener.renamed(event);

		model.caller.remove(callerList.getSelectedIndex());

		updateCallerList();
	}

	/**
	 * Reagiert darauf, wenn eine Anrufergruppe umbenannt wurde.
	 */
	private class CallerTypeRenameListener implements RenameListener {
		@Override
		public void renamed(RenameEvent e) {
			for (int i=0;i<model.callcenter.size();i++) {
				CallcenterModelCallcenter callcenter=model.callcenter.get(i);
				for (int j=0;j<callcenter.callerMinWaitingTimeName.size();j++) if (callcenter.callerMinWaitingTimeName.get(j).equalsIgnoreCase(e.oldName)) {
					if (e.deleted) {
						callcenter.callerMinWaitingTimeName.remove(j);
						callcenter.callerMinWaitingTime.remove(j);
					} else {
						callcenter.callerMinWaitingTimeName.set(j,e.newName);
					}
					break;
				}
			}

			for (int i=0;i<model.skills.size();i++) {
				CallcenterModelSkillLevel skill=model.skills.get(i);
				for (int j=0;j<skill.callerTypeName.size();j++) if (skill.callerTypeName.get(j).equalsIgnoreCase(e.oldName)) {
					if (e.deleted) {
						skill.callerTypeName.remove(j);
						skill.callerTypeWorkingTimeAddOn.remove(j);
						skill.callerTypeWorkingTime.remove(j);
						skill.callerTypePostProcessingTime.remove(j);
						skill.callerTypeIntervalWorkingTimeAddOn.remove(j);
						skill.callerTypeIntervalWorkingTime.remove(j);
						skill.callerTypeIntervalPostProcessingTime.remove(j);
						skill.callerTypeScore.remove(j);
					} else {
						skill.callerTypeName.set(j,e.newName);
					}
					break;
				}
			}
			updateSkillLevelList();
		}
	}

	/**
	 * Befehl: Anrufergruppe kopieren
	 * @see #callerList
	 */
	private void copyCaller() {
		if (callerList.getSelectedIndex()<0) return;
		CallcenterModelCaller newCaller=model.caller.get(callerList.getSelectedIndex()).clone();

		SmartNewName newName=new SmartNewName();
		for (int i=0;i<model.caller.size();i++) newName.addReservedName(model.caller.get(i).name);
		newCaller.name=newName.getUniqueNewName(newCaller.name);
		CopyDialog copy=new CopyDialog(owner,Language.tr("Editor.Caller.CopyGroup.Title"),Language.tr("Editor.Caller.CopyGroup.NameLabel"),newCaller.name,Language.tr("Editor.Caller.CopyGroup.IntensitySingle"),Language.tr("Editor.Caller.CopyGroup.IntensityMulti"),newCaller.freshCallsCountMean,helpLink.pageCallerModal);
		copy.setVisible(true);
		if (copy.getClosedBy()!=BaseEditDialog.CLOSED_BY_OK) return;
		newCaller.name=copy.getName();
		newCaller.freshCallsCountMean=(int)Math.round(newCaller.freshCallsCountMean*copy.getProbability());
		newCaller.freshCallsCountSD=(int)Math.round(newCaller.freshCallsCountSD*copy.getProbability());

		model.caller.add(newCaller);
		updateCallerList();
		callerList.setSelectedIndex(callerListData.getSize()-1);
	}

	/**
	 * Liefert eine Liste aller Callcenter-Namen.
	 * @param addNewName	Wird ein Wert ungleich <code>null</code> übergeben, so wird dieser Wert an die Ausgabeliste angehängt
	 * @return	Liste aller Callcenter-Namen
	 */

	private String[] callcenterNames(final String addNewName) {
		String[] callcenterNames=new String[model.callcenter.size()+((addNewName!=null)?1:0)];
		for (int i=0;i<model.callcenter.size();i++) callcenterNames[i]=model.callcenter.get(i).name;
		if (addNewName!=null) callcenterNames[callcenterNames.length-1]=addNewName;
		return callcenterNames;
	}

	/**
	 * Befehl: Anrufergruppe in der Liste nach oben verschieben
	 * @see #callerList
	 */
	private void moveCallerUp() {
		int selected=callerList.getSelectedIndex();
		CallcenterModelCaller c1=model.caller.get(selected);
		CallcenterModelCaller c2=model.caller.get(selected-1);
		model.caller.set(selected,c2);
		model.caller.set(selected-1,c1);
		callerList.setSelectedIndex(selected-1);
		updateCallerList();
	}

	/**
	 * Befehl: Anrufergruppe in der Liste nach unten verschieben
	 * @see #callerList
	 */
	private void moveCallerDown() {
		int selected=callerList.getSelectedIndex();
		CallcenterModelCaller c1=model.caller.get(selected);
		CallcenterModelCaller c2=model.caller.get(selected+1);
		model.caller.set(selected,c2);
		model.caller.set(selected+1,c1);
		callerList.setSelectedIndex(selected+1);
		updateCallerList();
	}

	/**
	 * Befehl: Callcenter hinzufügen
	 * @see #callcenterList
	 */
	private void addCallcenter() {
		CallcenterModelCallcenter callcenter=new CallcenterModelCallcenter();

		/* Eindeutigen Namen für neues Callcenter bestimmen */
		String callcenterName=callcenter.name;
		int nr=0;
		boolean ok;
		do {
			ok=true;
			for (int i=0;i<model.callcenter.size();i++) if (model.callcenter.get(i).name.equalsIgnoreCase(callcenterName)) {ok=false; break;}
			if (!ok) {nr++; callcenterName=callcenter.name+" "+nr;}
		} while (!ok);
		callcenter.name=callcenterName;

		/* Dialog anzeigen */
		CallcenterEditDialog dialog=new CallcenterEditDialog(owner,callcenter,model,callerNames(null),callcenterNames(callcenter.name),false,false,false,helpLink);
		dialog.addSkillLevelRenameListener(new SkillLevelRenameListener());
		dialog.setVisible(true);
		if (dialog.getClosedBy()!=BaseEditDialog.CLOSED_BY_OK) return;
		model.callcenter.add(callcenter);
		updateCallcenterList();
		callcenterList.setSelectedIndex(callcenterListData.getSize()-1);
		if (dialog.isOpenAgentsGenerator24() && generatorAgentsCallback24!=null) SwingUtilities.invokeLater(generatorAgentsCallback24);
		if (dialog.isOpenAgentsGenerator48() && generatorAgentsCallback48!=null) SwingUtilities.invokeLater(generatorAgentsCallback48);
		if (dialog.isOpenAgentsGenerator96() && generatorAgentsCallback96!=null) SwingUtilities.invokeLater(generatorAgentsCallback96);
		if (dialog.isOpenAgentsEfficiencyGenerator() && generatorAgentsEfficiencyCallback!=null) SwingUtilities.invokeLater(generatorAgentsEfficiencyCallback);
		if (dialog.isOpenAgentsAdditionGenerator() && generatorAgentsAdditionCallback!=null) SwingUtilities.invokeLater(generatorAgentsAdditionCallback);
	}

	/**
	 * Befehl: Callcenter bearbeiten
	 * @see #callcenterList
	 */
	private void editCallcenter() {
		if (callcenterList.getSelectedIndex()<0) return;
		int index=callcenterList.getSelectedIndex();
		CallcenterModelCallcenter callcenter;

		int editCount=0;
		int lastTab=-1;
		Point lastLocation=null;
		int lastClose=BaseEditDialog.CLOSED_BY_PREVIOUS;
		CallcenterEditDialog dialog=null;
		while (lastClose==BaseEditDialog.CLOSED_BY_PREVIOUS || lastClose==BaseEditDialog.CLOSED_BY_NEXT) {
			callcenter=model.callcenter.get(index);
			dialog=new CallcenterEditDialog(owner,callcenter,model,callerNames(null),callcenterNames(null),readOnly,index>0,index<callcenterListData.size()-1,helpLink);
			dialog.addSkillLevelRenameListener(new SkillLevelRenameListener());
			dialog.setTabIndex(lastTab);
			if (lastLocation!=null) dialog.setLocation(lastLocation);
			dialog.setVisible(true);
			lastTab=dialog.getTabIndex();
			lastLocation=dialog.getLocation();
			lastClose=dialog.getClosedBy();
			switch (lastClose) {
			case BaseEditDialog.CLOSED_BY_OK: editCount++; break;
			case BaseEditDialog.CLOSED_BY_PREVIOUS: editCount++; index--; break;
			case BaseEditDialog.CLOSED_BY_NEXT: editCount++; index++; break;
			}
		}
		if (editCount>0) {
			updateCallcenterList();
			if (dialog!=null && dialog.isOpenAgentsGenerator24() && generatorAgentsCallback24!=null) SwingUtilities.invokeLater(generatorAgentsCallback24);
			if (dialog!=null && dialog.isOpenAgentsGenerator48() && generatorAgentsCallback48!=null) SwingUtilities.invokeLater(generatorAgentsCallback48);
			if (dialog!=null && dialog.isOpenAgentsGenerator96() && generatorAgentsCallback96!=null) SwingUtilities.invokeLater(generatorAgentsCallback96);
			if (dialog!=null && dialog.isOpenAgentsEfficiencyGenerator() && generatorAgentsEfficiencyCallback!=null) SwingUtilities.invokeLater(generatorAgentsEfficiencyCallback);
			if (dialog!=null && dialog.isOpenAgentsAdditionGenerator() && generatorAgentsAdditionCallback!=null) SwingUtilities.invokeLater(generatorAgentsAdditionCallback);
		}
	}

	/**
	 * Befehl: Callcenter löschen
	 * @see #callcenterList
	 */
	private void delCallcenter() {
		if (callcenterList.getSelectedIndex()<0) return;

		if (!MsgBox.confirm(this,
				Language.tr("Editor.Callcenter.Delete.Title"),
				String.format(Language.tr("Editor.Callcenter.Delete.Info"),model.callcenter.get(callcenterList.getSelectedIndex()).name),
				Language.tr("Editor.Callcenter.Delete.Yes.Info"),
				Language.tr("Editor.Callcenter.Delete.No.Info"))
				) return;

		model.callcenter.remove(callcenterList.getSelectedIndex());
		updateCallcenterList();
	}

	/**
	 * Befehl: Callcenter kopieren
	 * @see #callcenterList
	 */
	private void copyCallcenter() {
		if (callcenterList.getSelectedIndex()<0) return;

		CallcenterModelCallcenter newCallcenter=model.callcenter.get(callcenterList.getSelectedIndex()).clone();

		SmartNewName newName=new SmartNewName();
		for (int i=0;i<model.callcenter.size();i++) newName.addReservedName(model.callcenter.get(i).name);
		newCallcenter.name=newName.getUniqueNewName(newCallcenter.name);

		double count=0;
		for (int i=0;i<newCallcenter.agents.size();i++) {
			CallcenterModelAgent a=newCallcenter.agents.get(i);
			if (a.count>=0) {
				int time;
				if (a.workingNoEndTime) time=86400-a.workingTimeStart; else time=a.workingTimeStart-a.workingTimeEnd;
				count+=time*a.count/1800;
			} else {
				if (a.count==-1) {
					if (a.countPerInterval24!=null) {
						double[] dist=a.countPerInterval24.densityData;
						for (int j=0;j<dist.length;j++) count+=dist[j]*2;
					}
					if (a.countPerInterval48!=null) {
						double[] dist=a.countPerInterval48.densityData;
						for (int j=0;j<dist.length;j++) count+=dist[j];
					}
					if (a.countPerInterval96!=null) {
						double[] dist=a.countPerInterval96.densityData;
						for (int j=0;j<dist.length;j++) count+=dist[j]/2;
					}
				}
				if (a.count==-2) {
					count+=a.byCallersAvailableHalfhours;
				}
			}
		}
		CopyDialog copy=new CopyDialog(owner,Language.tr("Editor.Callcenter.Copy.Title"),Language.tr("Editor.Callcenter.Copy.NameLabel"),newCallcenter.name,Language.tr("Editor.Callcenter.Copy.IntensitySingle"),Language.tr("Editor.Callcenter.Copy.IntensityMulti"),(int)Math.round(count),helpLink.pageCallcenterModal);
		copy.setVisible(true);
		if (copy.getClosedBy()!=BaseEditDialog.CLOSED_BY_OK) return;
		newCallcenter.name=copy.getName();
		double d=copy.getProbability();
		for (int i=0;i<newCallcenter.agents.size();i++) {
			CallcenterModelAgent a=newCallcenter.agents.get(i);
			if (a.count>=0) a.count=(int)Math.round(a.count*d);
			if (a.count==-1)  {
				double[] dist=null;
				if (a.countPerInterval24!=null) dist=a.countPerInterval24.densityData;
				if (a.countPerInterval48!=null) dist=a.countPerInterval48.densityData;
				if (a.countPerInterval96!=null) dist=a.countPerInterval96.densityData;
				if (dist!=null)	for (int j=0;j<dist.length;j++) dist[j]=Math.round(dist[j]*d);
			}
			if (a.count==-2) {
				a.byCallersAvailableHalfhours=(int)Math.round(a.byCallersAvailableHalfhours*d);
			}
		}

		model.callcenter.add(newCallcenter);
		updateCallcenterList();
		callcenterList.setSelectedIndex(callcenterListData.getSize()-1);
	}

	/**
	 * Befehl: Callcenter in der Liste nach oben verschieben
	 * @see #callcenterList
	 */
	private void moveCallcenterUp() {
		int selected=callcenterList.getSelectedIndex();
		if (selected<1) return;
		CallcenterModelCallcenter c1=model.callcenter.get(selected);
		CallcenterModelCallcenter c2=model.callcenter.get(selected-1);
		model.callcenter.set(selected,c2);
		model.callcenter.set(selected-1,c1);
		callcenterList.setSelectedIndex(selected-1);
		updateCallcenterList();
	}

	/**
	 * Befehl: Callcenter in der Liste nach unten verschieben
	 * @see #callcenterList
	 */
	private void moveCallcenterDown() {
		int selected=callcenterList.getSelectedIndex();
		if (selected<0 || selected==callcenterList.getModel().getSize()-1) return;
		CallcenterModelCallcenter c1=model.callcenter.get(selected);
		CallcenterModelCallcenter c2=model.callcenter.get(selected+1);
		model.callcenter.set(selected,c2);
		model.callcenter.set(selected+1,c1);
		callcenterList.setSelectedIndex(selected+1);
		updateCallcenterList();
	}

	/**
	 * Reagiert darauf, wenn ein Skill-Level umbenannt wird
	 */
	private class SkillLevelRenameListener implements RenameListener {
		@Override
		public void renamed(RenameEvent e) {
			for (int i=0;i<model.skills.size();i++) if (model.skills.get(i).name.equalsIgnoreCase(e.oldName)) {
				if (e.deleted) model.skills.remove(i); else model.skills.get(i).name=e.newName;
				break;
			}
			updateSkillLevelList();
		}
	}

	/**
	 * Liefert eine Liste aller Skill-Level-Namen.
	 * @param addNewName	Wird ein Wert ungleich <code>null</code> übergeben, so wird dieser Wert an die Ausgabeliste angehängt
	 * @return	Liste aller Skill-Level-Namen
	 */

	private String[] skillLevelNames(final String addNewName) {
		String[] skillLevelNames=new String[model.skills.size()+((addNewName!=null)?1:0)];
		for (int i=0;i<model.skills.size();i++) skillLevelNames[i]=model.skills.get(i).name;
		if (addNewName!=null) skillLevelNames[skillLevelNames.length-1]=addNewName;
		return skillLevelNames;
	}

	/**
	 * Befehl: Skill-Level hinzufügen
	 * @see #skillLevelList
	 */
	private void addSkillLevel() {
		CallcenterModelSkillLevel skill=new CallcenterModelSkillLevel();

		/* Eindeutigen Namen für neuen Skill-Level bestimmen */
		String skillName=skill.name;
		int nr=0;
		boolean ok;
		do {
			ok=true;
			for (int i=0;i<model.skills.size();i++) if (model.skills.get(i).name.equalsIgnoreCase(skillName)) {ok=false; break;}
			if (!ok) {nr++; skillName=skill.name+" "+nr;}
		} while (!ok);
		skill.name=skillName;

		/* Dialog anzeigen */
		SkillLevelEditDialog dialog=new SkillLevelEditDialog(owner,skill,callerNames(null),skillLevelNames(skill.name),false,false,false,helpLink);
		dialog.setVisible(true);
		if (dialog.getClosedBy()!=BaseEditDialog.CLOSED_BY_OK) return;
		model.skills.add(skill);
		updateSkillLevelList();
		skillLevelList.setSelectedIndex(skillLevelListData.getSize()-1);

		if (dialog.isOpenGenerator() && generatorSkillsCallback!=null) SwingUtilities.invokeLater(generatorSkillsCallback);
	}

	/**
	 * Befehl: Skill-Level bearbeiten
	 * @see #skillLevelList
	 */
	private void editSkillLevel() {
		if (skillLevelList.getSelectedIndex()<0) return;
		int index=skillLevelList.getSelectedIndex();
		CallcenterModelSkillLevel skill;

		int editCount=0;
		int lastTab=-1;
		Point lastLocation=null;
		int lastClose=BaseEditDialog.CLOSED_BY_PREVIOUS;
		SkillLevelEditDialog dialog=null;
		while (lastClose==BaseEditDialog.CLOSED_BY_PREVIOUS || lastClose==BaseEditDialog.CLOSED_BY_NEXT) {
			skill=model.skills.get(index);
			dialog=new SkillLevelEditDialog(owner,skill,callerNames(null),skillLevelNames(null),readOnly,index>0,index<skillLevelListData.size()-1,helpLink);
			dialog.setTabIndex(lastTab);
			if (lastLocation!=null) dialog.setLocation(lastLocation);
			dialog.setVisible(true);
			lastTab=dialog.getTabIndex();
			lastLocation=dialog.getLocation();
			lastClose=dialog.getClosedBy();
			switch (lastClose) {
			case BaseEditDialog.CLOSED_BY_OK: editCount++; break;
			case BaseEditDialog.CLOSED_BY_PREVIOUS: editCount++; index--; break;
			case BaseEditDialog.CLOSED_BY_NEXT: editCount++; index++; break;
			}
		}
		if (editCount>0) {
			updateSkillLevelList();
			if (dialog!=null && dialog.isOpenGenerator() && generatorSkillsCallback!=null) SwingUtilities.invokeLater(generatorSkillsCallback);
		}
	}

	/**
	 * Befehl: Skill-Level löschen
	 * @see #skillLevelList
	 */
	private void delSkillLevel() {
		if (skillLevelList.getSelectedIndex()<0) return;
		if (!MsgBox.confirm(this,
				Language.tr("Editor.SkillLevel.Delete.Title"),
				String.format(Language.tr("Editor.SkillLevel.Delete.Info"),model.skills.get(skillLevelList.getSelectedIndex()).name),
				Language.tr("Editor.SkillLevel.Delete.Yes.Info"),
				Language.tr("Editor.SkillLevel.Delete.No.Info"))
				) return;

		model.skills.remove(skillLevelList.getSelectedIndex());

		updateSkillLevelList();
	}

	/**
	 * Befehl: Skill-Level kopieren
	 * @see #skillLevelList
	 */
	private void copySkillLevel() {
		if (skillLevelList.getSelectedIndex()<0) return;

		CallcenterModelSkillLevel newSkill=model.skills.get(skillLevelList.getSelectedIndex()).clone();

		SmartNewName newName=new SmartNewName();
		for (int i=0;i<model.skills.size();i++) newName.addReservedName(model.skills.get(i).name);
		newSkill.name=newName.getUniqueNewName(newSkill.name);

		CopyDialog copy=new CopyDialog(owner,Language.tr("Editor.SkillLevel.Copy.Title"),Language.tr("Editor.SkillLevel.Copy.NameLabel"),newSkill.name,helpLink.pageSkillLevelModal);
		copy.setVisible(true);
		if (copy.getClosedBy()!=BaseEditDialog.CLOSED_BY_OK) return;
		newSkill.name=copy.getName();

		model.skills.add(newSkill);
		updateSkillLevelList();
		skillLevelList.setSelectedIndex(skillLevelListData.getSize()-1);
	}

	/**
	 * Befehl: Skill-Level in der Liste nach oben verschieben
	 * @see #skillLevelList
	 */
	private void moveSkillLevelUp() {
		int selected=skillLevelList.getSelectedIndex();
		CallcenterModelSkillLevel s1=model.skills.get(selected);
		CallcenterModelSkillLevel s2=model.skills.get(selected-1);
		model.skills.set(selected,s2);
		model.skills.set(selected-1,s1);
		skillLevelList.setSelectedIndex(selected-1);
		updateSkillLevelList();
	}

	/**
	 * Befehl: Skill-Level in der Liste nach unten verschieben
	 * @see #skillLevelList
	 */
	private void moveSkillLevelDown() {
		int selected=skillLevelList.getSelectedIndex();
		CallcenterModelSkillLevel s1=model.skills.get(selected);
		CallcenterModelSkillLevel s2=model.skills.get(selected+1);
		model.skills.set(selected,s2);
		model.skills.set(selected+1,s1);
		skillLevelList.setSelectedIndex(selected+1);
		updateSkillLevelList();
	}

	/**
	 * Aktualisiert den Modellüberblíck.
	 * @see #statistics
	 */
	private void updateStatistics() {
		if (statistics==null) return;
		StatisticNode root=new StatisticNode();
		StatisticNode node,node2;

		/* Allgemeine Informationen */
		root.addChild(new StatisticNode(Language.tr("Editor.Overview.General.Information"),new StatisticViewerModelInformation(model,false),Language.tr("Editor.Overview.Category.General")));
		root.addChild(new StatisticNode(Language.tr("Editor.Overview.General.Description"),new StatisticViewerModelInformation(model,true),Language.tr("Editor.Overview.Category.General")));

		/* Kundenankünfte */
		root.addChild(node=new StatisticNode(Language.tr("Editor.Overview.Caller.Title"),Language.tr("Editor.Overview.Category.Caller")));

		node.addChild(new StatisticNode(Language.tr("Editor.Overview.Overview"),new StatisticViewerCallerTable(model),Language.tr("Editor.Overview.Category.Caller")));
		node.addChild(new StatisticNode(Language.tr("Editor.Overview.Overview"),new StatisticViewerCallerPieChart(model),Language.tr("Editor.Overview.Category.Caller")));
		for (int i=0;i<model.caller.size();i++) if (model.caller.get(i).active) {
			node.addChild(new StatisticNode(model.caller.get(i).name,new StatisticViewerCallerDiagram(model,i),Language.tr("Editor.Overview.Category.Caller")));
		}
		node.addChild(new StatisticNode(Language.tr("Editor.Overview.Caller.All"),new StatisticViewerCallerDiagram(model,-1),Language.tr("Editor.Overview.Category.Caller")));

		/* Agenten */
		root.addChild(node=new StatisticNode(Language.tr("Editor.Overview.Agents.Title")));

		/* Agenten pro Callcenter */
		node.addChild(node2=new StatisticNode(Language.tr("Editor.Overview.Agents.PerCallcenter"),Language.tr("Editor.Overview.Category.AgentsPerCallcenter")));
		node2.addChild(new StatisticNode(Language.tr("Editor.Overview.Overview"),new StatisticViewerAgentsText(model,StatisticViewerAgentsText.Mode.BY_CALLCENTER),Language.tr("Editor.Overview.Category.AgentsPerCallcenter")));
		node2.addChild(new StatisticNode(Language.tr("Editor.Overview.Overview"),new StatisticViewerAgentsTable(model,StatisticViewerAgentsTable.Mode.BY_CALLCENTER),Language.tr("Editor.Overview.Category.AgentsPerCallcenter")));
		List<CallcenterModelAgent> agentList=new ArrayList<CallcenterModelAgent>();
		List<CallcenterModelCallcenter> callcenterList=new ArrayList<CallcenterModelCallcenter>();
		for (int i=0;i<model.callcenter.size();i++) if (model.callcenter.get(i).active) {
			List<CallcenterModelCallcenter> c=new ArrayList<CallcenterModelCallcenter>();
			for (int j=0;j<model.callcenter.get(i).agents.size();j++) c.add(model.callcenter.get(i));
			node2.addChild(new StatisticNode(model.callcenter.get(i).name,new StatisticViewerAgentsDiagram(model,model.callcenter.get(i).agents,c,StatisticViewerAgentsDiagram.Mode.ADDBY_ALL,model.callcenter.get(i).name),Language.tr("Editor.Overview.Category.AgentsPerCallcenter")));
			agentList.addAll(model.callcenter.get(i).agents);
			callcenterList.addAll(c);
		}
		node2.addChild(new StatisticNode(Language.tr("Editor.Overview.Agents.All"),new StatisticViewerAgentsDiagram(model,agentList,callcenterList,StatisticViewerAgentsDiagram.Mode.ADDBY_ALL,""),Language.tr("Editor.Overview.Category.AgentsPerCallcenter")));

		/* Aktive Agenten pro Skill-Level */
		node.addChild(node2=new StatisticNode(Language.tr("Editor.Overview.Agents.PerSkillLevel"),Language.tr("Editor.Overview.Category.AgentsPerSkillLevel")));
		node2.addChild(new StatisticNode(Language.tr("Editor.Overview.Overview"),new StatisticViewerAgentsText(model,StatisticViewerAgentsText.Mode.BY_SKILLLEVEL),Language.tr("Editor.Overview.Category.AgentsPerSkillLevel")));
		node2.addChild(new StatisticNode(Language.tr("Editor.Overview.Overview"),new StatisticViewerAgentsTable(model,StatisticViewerAgentsTable.Mode.BY_SKILLLEVEL),Language.tr("Editor.Overview.Category.AgentsPerSkillLevel")));
		node2.addChild(new StatisticNode(Language.tr("Editor.Overview.Overview"),new StatisticViewerAgentenPieChart(model,-1),Language.tr("Editor.Overview.Category.AgentsPerSkillLevel")));
		for (int i=0;i<model.skills.size();i++) {
			node2.addChild(new StatisticNode(model.skills.get(i).name,new StatisticViewerAgentsDiagram(model,agentList,callcenterList,StatisticViewerAgentsDiagram.Mode.ADDBY_SKILLLEVEL,model.skills.get(i).name),Language.tr("Editor.Overview.Category.AgentsPerSkillLevel")));
		}
		for (int i=0;i<model.callcenter.size();i++)
			node2.addChild(new StatisticNode(model.callcenter.get(i).name,new StatisticViewerAgentenPieChart(model,i),Language.tr("Editor.Overview.Category.AgentsPerSkillLevel")));

		/* Agenten  pro Kundentyp */
		node.addChild(node2=new StatisticNode(Language.tr("Editor.Overview.Agents.PerCallerType"),Language.tr("Editor.Overview.Category.AgentsPerCallerType")));
		node2.addChild(new StatisticNode(Language.tr("Editor.Overview.Overview"),new StatisticViewerAgentsTable(model,StatisticViewerAgentsTable.Mode.BY_SKILL),Language.tr("Editor.Overview.Category.AgentsPerCallerType")));
		for (int i=0;i<model.caller.size();i++) if (model.caller.get(i).active) {
			node2.addChild(new StatisticNode(model.caller.get(i).name,new StatisticViewerAgentsDiagram(model,agentList,callcenterList,StatisticViewerAgentsDiagram.Mode.ADDBY_SKILL,model.caller.get(i).name),Language.tr("Editor.Overview.Category.AgentsPerCallerType")));
		}

		/* Schichtplan */
		node.addChild(node2=new StatisticNode(Language.tr("Editor.Overview.Agents.ShiftSchedulePerCallcenter"),Language.tr("Editor.Overview.Category.ShiftSchedulePerCallcenter")));
		List<CallcenterModelAgent> fullList=new ArrayList<CallcenterModelAgent>();
		List<CallcenterModelAgent> translatedList;
		for (int i=0;i<model.callcenter.size();i++) if (model.callcenter.get(i).active) {
			translatedList=calcShiftPlan(model.callcenter.get(i).agents,model.callcenter.get(i));
			fullList.addAll(translatedList);
		}
		for (int i=0;i<model.callcenter.size();i++) if (model.callcenter.get(i).active) {
			translatedList=calcShiftPlan(model.callcenter.get(i).agents,model.callcenter.get(i));
			node2.addChild(new StatisticNode(model.callcenter.get(i).name,new StatisticViewerAgentShiftPlanTable(translatedList),Language.tr("Editor.Overview.Category.ShiftSchedulePerCallcenter")));
			node2.addChild(new StatisticNode(model.callcenter.get(i).name,new StatisticViewerAgentShiftPlanDiagram(translatedList),Language.tr("Editor.Overview.Category.ShiftSchedulePerCallcenter")));
		}
		node2.addChild(new StatisticNode(Language.tr("Editor.Overview.Agents.All"),new StatisticViewerAgentShiftPlanTable(fullList),Language.tr("Editor.Overview.Category.ShiftSchedulePerCallcenter")));
		node2.addChild(new StatisticNode(Language.tr("Editor.Overview.Agents.All"),new StatisticViewerAgentShiftPlanDiagram(fullList),Language.tr("Editor.Overview.Category.ShiftSchedulePerCallcenter")));

		/* Einfaches und Erweitertes Erlang-C Modell */
		StatisticViewerErlangCTools erlangCData;
		root.addChild(node=new StatisticNode(Language.tr("Editor.Overview.SimpleErlangCModel.Title"),Language.tr("Editor.Overview.Category.SimpleErlangCModel")));
		erlangCData=new StatisticViewerErlangCTools(model,false);
		node.addChild(new StatisticNode(Language.tr("Editor.Overview.SimpleErlangCModel.AccessibilityAndServiceLevel"),new StatisticViewerErlangCTable(erlangCData),Language.tr("Editor.Overview.Category.ExtendedErlangCModel")));
		node.addChild(new StatisticNode(Language.tr("Editor.Overview.SimpleErlangCModel.Accessibility"),new StatisticViewerErlangCDiagramm(erlangCData,StatisticViewerErlangCDiagramm.Mode.ERREICHBARKEIT),Language.tr("Editor.Overview.Category.ExtendedErlangCModel")));
		node.addChild(new StatisticNode(Language.tr("Editor.Overview.SimpleErlangCModel.AverageWaitingTime"),new StatisticViewerErlangCDiagramm(erlangCData,StatisticViewerErlangCDiagramm.Mode.WARTEZEIT),Language.tr("Editor.Overview.Category.ExtendedErlangCModel")));
		node.addChild(new StatisticNode(Language.tr("Editor.Overview.SimpleErlangCModel.ServiceLevel"),new StatisticViewerErlangCDiagramm(erlangCData,StatisticViewerErlangCDiagramm.Mode.SERVICELEVEL),Language.tr("Editor.Overview.Category.ExtendedErlangCModel")));
		root.addChild(node=new StatisticNode(Language.tr("Editor.Overview.ExtendedErlangCModel.Title"),Language.tr("Editor.Overview.Category.ExtendedErlangCModel")));
		erlangCData=new StatisticViewerErlangCTools(model,true);
		node.addChild(new StatisticNode(Language.tr("Editor.Overview.ExtendedErlangCModel.AccessibilityAndServiceLevel"),new StatisticViewerErlangCTable(erlangCData),Language.tr("Editor.Overview.Category.ExtendedErlangCModel")));
		node.addChild(new StatisticNode(Language.tr("Editor.Overview.ExtendedErlangCModel.Accessibility"),new StatisticViewerErlangCDiagramm(erlangCData,StatisticViewerErlangCDiagramm.Mode.ERREICHBARKEIT),Language.tr("Editor.Overview.Category.ExtendedErlangCModel")));
		node.addChild(new StatisticNode(Language.tr("Editor.Overview.ExtendedErlangCModel.AverageWaitingTime"),new StatisticViewerErlangCDiagramm(erlangCData,StatisticViewerErlangCDiagramm.Mode.WARTEZEIT),Language.tr("Editor.Overview.Category.ExtendedErlangCModel")));
		node.addChild(new StatisticNode(Language.tr("Editor.Overview.ExtendedErlangCModel.ServiceLevel"),new StatisticViewerErlangCDiagramm(erlangCData,StatisticViewerErlangCDiagramm.Mode.SERVICELEVEL),Language.tr("Editor.Overview.Category.ExtendedErlangCModel")));

		statistics.setStatisticData(root);
	}

	/**
	 * Befehl: Erstanruferanzahl in allen Gruppen verändern
	 */
	private void changeCallerCount() {
		String[] countNames=new String[model.caller.size()];
		int[] countValues=new int[model.caller.size()];
		for (int i=0;i<model.caller.size();i++) {
			CallcenterModelCaller caller=model.caller.get(i);
			countNames[i]=caller.name;
			countValues[i]=caller.freshCallsCountMean;
		}

		String s1=Language.tr("Editor.Caller.CountEdit.Single");
		String s2=Language.tr("Editor.Caller.CountEdit.Multiple");
		CopyDialog copy=new CopyDialog(owner,Language.tr("Editor.CountEdit"),s1,s2,countNames,countValues,helpLink.pageCallerModal);
		copy.setVisible(true);
		if (copy.getClosedBy()!=BaseEditDialog.CLOSED_BY_OK) return;

		double d=copy.getProbability();
		for (int i=0;i<model.caller.size();i++) {
			CallcenterModelCaller caller=model.caller.get(i);
			caller.freshCallsCountMean=(int)Math.max(0,d*caller.freshCallsCountMean);
		}
		updateCallerList();
	}

	/**
	 * Befehl: Agentenanzahl in allen Intervallen in allen Callcentern verändern
	 */
	private void changeAgentCount() {
		int groupCount=0;
		for (CallcenterModelCallcenter callcenter : model.callcenter) groupCount+=callcenter.agents.size();

		String[] countNames=new String[groupCount];
		int[] countValues=new int[groupCount];
		int i=0;
		for (CallcenterModelCallcenter callcenter : model.callcenter) for (CallcenterModelAgent agent : callcenter.agents) {
			int count=0;
			if (agent.count>=0) {
				int workingEnd=agent.workingNoEndTime?86400:agent.workingTimeEnd;
				count=(int)Math.round(agent.count*(double)Math.max(0,workingEnd-agent.workingTimeStart)/1800.0);
			} else {
				if (agent.count==-1) {
					if (agent.countPerInterval24!=null) count=(int)Math.round(agent.countPerInterval24.sum()*2);
					if (agent.countPerInterval48!=null) count=(int)Math.round(agent.countPerInterval48.sum());
					if (agent.countPerInterval96!=null) count=(int)Math.round(agent.countPerInterval96.sum()/2);
				}
				if (agent.count==-2) {
					count=agent.byCallersAvailableHalfhours;
				}
			}
			countNames[i]=String.format(callcenter.name+" - "+Language.tr("Editor.Callcenter.AgentGroupNr"),i+1);
			countValues[i]=count;
			i++;
		}

		String s1=Language.tr("Editor.Callcenter.Count.HalfHourIntervalSingleAbout");
		String s2=Language.tr("Editor.Callcenter.Count.HalfHourIntervalMultipleAbout");
		CopyDialog copy=new CopyDialog(owner,Language.tr("Editor.AgentsGroup.Change"),s1,s2,countNames,countValues,helpLink.pageCallcenterModal);
		copy.setVisible(true);
		if (copy.getClosedBy()!=BaseEditDialog.CLOSED_BY_OK) return;

		double d=copy.getProbability();
		for (CallcenterModelCallcenter callcenter : model.callcenter) for (CallcenterModelAgent agent : callcenter.agents) {
			if (agent.count>=0) {
				agent.count=Math.max(1,(int)Math.round(agent.count*d));
			} else {
				if (agent.count==-1) {
					if (agent.countPerInterval24!=null) agent.countPerInterval24=agent.countPerInterval24.multiply(d).round();
					if (agent.countPerInterval48!=null) agent.countPerInterval48=agent.countPerInterval48.multiply(d).round();
					if (agent.countPerInterval96!=null) agent.countPerInterval96=agent.countPerInterval96.multiply(d).round();
				}
				if (agent.count==-2) {
					agent.byCallersAvailableHalfhours=Math.max(1,(int)Math.round(agent.byCallersAvailableHalfhours*d));
				}
			}
			i++;
		}
		updateCallcenterList();
	}


	/**
	 * Reagiert auf Ereignisse für eine Listendarstellung
	 */
	private class ListListener implements KeyListener, MouseListener, ListSelectionListener {
		/** Liste auf deren Ereignisse reagiert werden soll */
		@SuppressWarnings("rawtypes")
		private final JList list;
		/** Zu der Listendarstellung gehörige Schaltflächen */
		private final JButton[] buttons;
		/** Aktions-Callbacks zu der Liste bzw. den Schaltflächen */
		private final ActionListener actionListener;

		/**
		 * Konstruktor der Klasse
		 * @param list	Liste auf deren Ereignisse reagiert werden soll
		 * @param buttons	Zu der Listendarstellung gehörige Schaltflächen
		 * @param actionListener	Aktions-Callbacks zu der Liste bzw. den Schaltflächen
		 */
		public ListListener(@SuppressWarnings("rawtypes") JList list, JButton[] buttons, ActionListener actionListener) {
			this.list=list;
			this.buttons=buttons;
			this.actionListener=actionListener;
		}

		@Override
		public void keyTyped(KeyEvent e) {}

		/**
		 * Löst die Verarbeitung für einen Klick auf eine Schaltfläche aus.
		 * @param button	Schaltfläche deren Aktion ausgeführt werden soll
		 */
		private void buttonClick(final JButton button) {
			ActionEvent e=new ActionEvent(button,0,button.getText());
			actionListener.actionPerformed(e);
		}

		/**
		 * Aktiviert oder deaktiviert den selektierten Listeneintrag.
		 */
		private void toggleActive() {
			if (list.getSelectedIndex()<0) return;

			if (list==callcenterList) {
				CallcenterModelCallcenter callcenter=model.callcenter.get(list.getSelectedIndex());
				callcenter.active=!callcenter.active;
				updateCallcenterList();
			}

			if (list==callerList) {
				CallcenterModelCaller caller=model.caller.get(list.getSelectedIndex());
				caller.active=!caller.active;
				updateCallerList();
			}
		}

		@Override
		public void keyPressed(KeyEvent e) {
			if (e.getKeyCode()==KeyEvent.VK_ENTER && e.getModifiersEx()==0) {buttonClick(buttons[1]); e.consume();}
			if (e.getKeyCode()==KeyEvent.VK_INSERT && e.getModifiersEx()==0) {buttonClick(buttons[0]); e.consume();}
			if (e.getKeyCode()==KeyEvent.VK_DELETE && e.getModifiersEx()==0) {buttonClick(buttons[2]); e.consume();}
			if (e.getKeyCode()==KeyEvent.VK_C && (e.getModifiersEx() & (InputEvent.CTRL_DOWN_MASK+InputEvent.SHIFT_DOWN_MASK))!=0) {buttonClick(buttons[3]); e.consume();}
			if (e.getKeyCode()==KeyEvent.VK_ENTER && (e.getModifiersEx() & InputEvent.CTRL_DOWN_MASK)!=0) {toggleActive(); e.consume();}
			if (e.getKeyCode()==KeyEvent.VK_UP && (e.getModifiersEx() & InputEvent.CTRL_DOWN_MASK)!=0) {buttonClick(buttons[4]); e.consume();}
			if (e.getKeyCode()==KeyEvent.VK_DOWN && (e.getModifiersEx() & InputEvent.CTRL_DOWN_MASK)!=0) {buttonClick(buttons[5]); e.consume();}
		}

		/**
		 * Löst ggf. ein Popupmenü zu einem Eintrag in der Anrufer- oder der Callcenter-Liste aus.
		 * @see CallcenterModelEditorPanel#callerList
		 * @see CallcenterModelEditorPanel#callcenterList
		 * @param e	Maus-Ereignis
		 */
		private void mousePopup(MouseEvent e) {
			if (!e.isPopupTrigger()) return;

			int index=list.locationToIndex(e.getPoint());
			if (!list.getCellBounds(index,index).contains(e.getPoint())) index=-1;

			String name="";
			boolean active=false;
			ActionListener activateListener=null;

			if (index>=0 && list==callerList) {
				final CallcenterModelCaller caller=model.caller.get(index);
				name=Language.tr("Editor.CallerGroup")+" \""+caller.name+"\" "+Language.tr("Dialog.active.lower");
				active=caller.active;
				activateListener=new ActionListener() {
					@Override
					public void actionPerformed(ActionEvent e) {
						caller.active=!caller.active;
						updateCallerList();
					}
				};
			}

			if (index>=0 && list==callcenterList) {
				final CallcenterModelCallcenter callcenter=model.callcenter.get(index);
				name=Language.tr("Editor.Callcenter")+" \""+callcenter.name+"\" "+Language.tr("Dialog.active.lower");
				active=callcenter.active;
				activateListener=new ActionListener() {
					@Override
					public void actionPerformed(ActionEvent e) {
						callcenter.active=!callcenter.active;
						updateCallcenterList();
					}
				};
			}

			JMenuItem item;
			JPopupMenu popup=new JPopupMenu();

			if (activateListener!=null) {
				popup.add(item=new JCheckBoxMenuItem(name,active));
				item.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_ENTER,InputEvent.CTRL_DOWN_MASK));
				item.addActionListener(activateListener);
				popup.addSeparator();
			}

			popup.add(item=new JMenuItem(buttons[0].getText(),buttons[0].getIcon()));
			item.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_INSERT,0));
			item.addActionListener(new PopupActionListener(0,-1));

			if (index>=0) {
				popup.add(item=new JMenuItem(buttons[1].getText(),buttons[1].getIcon()));
				item.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_ENTER,0));
				item.addActionListener(new PopupActionListener(1,index));
			}

			if (!readOnly && index>=0) {
				popup.add(item=new JMenuItem(buttons[2].getText(),buttons[2].getIcon()));
				item.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_DELETE,0));
				item.addActionListener(new PopupActionListener(2,index));
				popup.add(item=new JMenuItem(buttons[3].getText(),buttons[3].getIcon()));
				item.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_C,InputEvent.CTRL_DOWN_MASK+InputEvent.SHIFT_DOWN_MASK));
				item.addActionListener(new PopupActionListener(3,index));
				if (index>0 || index<list.getModel().getSize()-1) {
					popup.addSeparator();
				}
				if (index>0) {
					popup.add(item=new JMenuItem(buttons[4].getText(),buttons[4].getIcon()));
					item.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_UP,InputEvent.CTRL_DOWN_MASK));
					item.addActionListener(new PopupActionListener(4,index));
				}
				if (index<list.getModel().getSize()-1) {
					popup.add(item=new JMenuItem(buttons[5].getText(),buttons[5].getIcon()));
					item.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_DOWN,InputEvent.CTRL_DOWN_MASK));
					item.addActionListener(new PopupActionListener(5,index));
				}
			}

			popup.show(e.getComponent(),e.getX(), e.getY());
		}

		/**
		 * Reagiert auf einen Eintrag in dem Anrufergruppen- oder Callcenter-Listen-Popupmenü
		 * @see CallcenterModelEditorPanel#callerList
		 * @see CallcenterModelEditorPanel#callcenterList
		 */
		private class PopupActionListener implements ActionListener {
			/** Nummer des angeklickten Menüpunktes */
			private final int buttonNr;
			/** Index des Eintrags in der aktuellen Liste */
			private final int index;

			/**
			 * Konstruktor der Klasse
			 * @param buttonNr	Nummer des angeklickten Menüpunktes
			 * @param index	Index des Eintrags in der aktuellen Liste
			 */
			public PopupActionListener(final int buttonNr, final int index) {
				this.buttonNr=buttonNr;
				this.index=index;
			}

			@Override
			public void actionPerformed(ActionEvent e) {
				if (index>=0) list.setSelectedIndex(index);
				buttonClick(buttons[buttonNr]);
			}
		}

		@Override
		public void keyReleased(KeyEvent e) {}
		@Override
		public void mouseClicked(MouseEvent e) {if (e.getClickCount()==2) buttonClick(buttons[1]);}
		@Override
		public void mousePressed(MouseEvent e) {mousePopup(e);}
		@Override
		public void mouseReleased(MouseEvent e) {mousePopup(e);}
		@Override
		public void mouseEntered(MouseEvent e) {}
		@Override
		public void mouseExited(MouseEvent e) {}

		@Override
		public void valueChanged(ListSelectionEvent e) {
			buttons[1].setEnabled(list.getSelectedIndex()!=-1);
			buttons[2].setEnabled(!readOnly && list.getSelectedIndex()!=-1);
			buttons[3].setEnabled(!readOnly && list.getSelectedIndex()!=-1);
			buttons[4].setEnabled(!readOnly && list.getSelectedIndex()>0);
			buttons[5].setEnabled(!readOnly && list.getSelectedIndex()!=-1 && list.getSelectedIndex()<list.getModel().getSize()-1);
		}
	}

	/**
	 * Reagiert auf einen Klick auf eine der Schaltflächen über einer
	 * der Listen auf einer der Dialogseiten.
	 */
	private class ButtonActionListener implements ActionListener {
		/** Art der zugehörigen Liste (0: Anrufergruppen, 1: Callcenter und Agenten, 2: Skill-Level) */
		private final int nr;

		/**
		 * Zu der Liste gehörige Schaltflächen.
		 * @see CallcenterModelEditorPanel#addEditPanel(JPanel, int, Icon, Icon, Icon, Icon, Icon, Icon, Icon, String, String, String, String, String, String, String)
		 */
		public JButton[] buttons;

		/**
		 * In {@link #openTinyPopup(String, Icon, Component)} erzeugter Menüpunkt
		 * @see #openTinyPopup(String, Icon, Component)
		 */
		private JMenuItem popupItem=null;

		/** In {@link #openTinyPopup(String[], Icon[], Component)} erzeugte Menüpunkte
		 *  @see #openTinyPopup(String[], Icon[], Component)
		 */
		private JMenuItem[] popupItems=null;

		/**
		 * Erzeugt ein kleines Popupmenü mit einem Menüpunkt.
		 * @param item	Name des Menüpunkts
		 * @param icon	Icon für den Menüpunkt
		 * @param invoker	Aufrufer (zur Ausrichtung des Menüs)
		 * @see #actionPerformed(ActionEvent)
		 */
		private void openTinyPopup(final String item, final Icon icon, final Component invoker) {
			JPopupMenu popupMenu=new JPopupMenu();
			popupMenu.add(popupItem=new JMenuItem(item));
			popupItem.addActionListener(this);
			if (icon!=null) popupItem.setIcon(icon);
			popupMenu.show(invoker,0,invoker.getBounds().height);
		}

		/**
		 * Erzeugt ein kleines Popupmenü mit mehreren Menüpunkten.
		 * @param items	Namen der Menüpunkte
		 * @param icons	Icons für die Menüpunkte
		 * @param invoker	Aufrufer (zur Ausrichtung des Menüs)
		 * @see #actionPerformed(ActionEvent)
		 */
		private void openTinyPopup(final String[] items, final Icon[] icons, final Component invoker) {
			JPopupMenu popupMenu=new JPopupMenu();
			popupItems=new JMenuItem[items.length];
			for (int i=0;i<items.length;i++) {
				popupMenu.add(popupItems[i]=new JMenuItem(items[i]));
				popupItems[i].addActionListener(this);
				if (icons!=null && icons.length>i && icons[i]!=null) popupItems[i].setIcon(icons[i]);
			}

			popupMenu.show(invoker,0,invoker.getBounds().height);
		}

		/**
		 * Konstruktor der Klasse
		 * @param nr	Art der zugehörigen Liste (0: Anrufergruppen, 1: Callcenter und Agenten, 2: Skill-Level)
		 */
		public ButtonActionListener(int nr) {
			this.nr=nr;
		}

		@Override
		public void actionPerformed(ActionEvent e) {
			if (e.getSource()==popupItem) {
				switch (nr) {
				case 2: if (generatorSkillsCallback!=null) SwingUtilities.invokeLater(generatorSkillsCallback); break;
				}
			}
			if (popupItems!=null && nr==0) {
				if (e.getSource()==popupItems[0]) {changeCallerCount(); return;}
				if (e.getSource()==popupItems[1]) {if (generatoCallerCallback24!=null) SwingUtilities.invokeLater(generatoCallerCallback24);}
				if (e.getSource()==popupItems[2]) {if (generatoCallerCallback48!=null) SwingUtilities.invokeLater(generatoCallerCallback48);}
				if (e.getSource()==popupItems[3]) {if (generatoCallerCallback96!=null) SwingUtilities.invokeLater(generatoCallerCallback96);}
			}

			if (popupItems!=null && nr==1) {
				if (e.getSource()==popupItems[0]) {changeAgentCount(); return;}
				if (e.getSource()==popupItems[1]) {if (generatorAgentsCallback24!=null) SwingUtilities.invokeLater(generatorAgentsCallback24);}
				if (e.getSource()==popupItems[2]) {if (generatorAgentsCallback48!=null) SwingUtilities.invokeLater(generatorAgentsCallback48);}
				if (e.getSource()==popupItems[3]) {if (generatorAgentsCallback96!=null) SwingUtilities.invokeLater(generatorAgentsCallback96);}
			}

			if (e.getSource()==globalParameters) {
				CallcenterModelGlobalDialog dialog=new CallcenterModelGlobalDialog(owner,model,readOnly,helpLink);
				dialog.setVisible(true);
				if (dialog.getClosedBy()!=BaseEditDialog.CLOSED_BY_OK) return;
				mutexGetSetModel.lock();
				try {
					model=dialog.getModel().clone();
				} finally {
					mutexGetSetModel.unlock();
				}
				return;
			}

			if (e.getSource()==thresholdValues) {
				CallcenterThresholdValuesDialog dialog=new CallcenterThresholdValuesDialog(owner,model,readOnly,helpLink);
				dialog.setVisible(true);
				if (dialog.getClosedBy()!=BaseEditDialog.CLOSED_BY_OK) return;
				mutexGetSetModel.lock();
				try {
					model=dialog.getModel().clone();
				} finally {
					mutexGetSetModel.unlock();
				}
				return;
			}

			if (e.getSource()==generateDescription) {
				String s=model.generateDescription().trim();
				if (s.equalsIgnoreCase(description.getText().trim())) return;
				if (!description.getText().trim().isEmpty()) {
					if (!MsgBox.confirm(owner,
							Language.tr("Editor.GeneralData.CreateDescription.Message"),
							Language.tr("Editor.GeneralData.CreateDescription.Info"),
							Language.tr("Editor.GeneralData.CreateDescription.Yes.Info"),
							Language.tr("Editor.GeneralData.CreateDescription.No.Info"))) return;
				}
				description.setText(s);
				model.description=s;
				return;
			}

			if (buttons==null) return;

			if ((e.getSource()!=buttons[1]) && readOnly) return;

			if (e.getSource()==buttons[0]) switch (nr) {
			case 0: addCaller(); break;
			case 1: addCallcenter(); break;
			case 2: addSkillLevel(); break;
			}
			if (e.getSource()==buttons[1]) switch (nr) {
			case 0: editCaller(); break;
			case 1: editCallcenter(); break;
			case 2: editSkillLevel(); break;
			}
			if (e.getSource()==buttons[2]) switch (nr) {
			case 0: delCaller(); break;
			case 1: delCallcenter(); break;
			case 2: delSkillLevel(); break;
			}
			if (e.getSource()==buttons[3]) switch (nr) {
			case 0: copyCaller(); break;
			case 1: copyCallcenter(); break;
			case 2: copySkillLevel(); break;
			}
			if (e.getSource()==buttons[4]) switch (nr) {
			case 0: moveCallerUp(); break;
			case 1: moveCallcenterUp(); break;
			case 2: moveSkillLevelUp(); break;
			}
			if (e.getSource()==buttons[5]) switch (nr) {
			case 0: moveCallerDown(); break;
			case 1: moveCallcenterDown(); break;
			case 2: moveSkillLevelDown(); break;
			}
			if (e.getSource()==buttons[6]) switch (nr) {
			case 0:
				openTinyPopup(
						new String[]{
								Language.tr("Editor.ToolsPopup.CallerChangeAllIntervals"),
								Language.tr("Editor.ToolsPopup.Caller24"),
								Language.tr("Editor.ToolsPopup.Caller48"),
								Language.tr("Editor.ToolsPopup.Caller96")
						},
						new Icon[]{
								Images.GENERAL_TOOLS.getIcon(),
								Images.EDITOR_CALLER.getIcon(),
								Images.EDITOR_CALLER.getIcon(),
								Images.EDITOR_CALLER.getIcon()
						},(Component)e.getSource());
				break;
			case 1:
				openTinyPopup(
						new String[]{
								Language.tr("Editor.AgentsGroup.Tools.ChangeAllIntervals.AllCallcenter"),
								Language.tr("Editor.ToolsPopup.Agents24"),
								Language.tr("Editor.ToolsPopup.Agents48"),
								Language.tr("Editor.ToolsPopup.Agents96")
						},
						new Icon[]{
								Images.GENERAL_TOOLS.getIcon(),
								Images.EDITOR_AGENTS.getIcon(),
								Images.EDITOR_AGENTS.getIcon(),
								Images.EDITOR_AGENTS.getIcon()
						},(Component)e.getSource());
				break;
			case 2:
				openTinyPopup(
						Language.tr("Editor.ToolsPopup.SkillLevel"),
						Images.EDITOR_SKILLLEVEL.getIcon(),
						(Component)e.getSource());
				break;
			}
		}
	}

	/**
	 * Berechnet einen Schichtplan für die Anzeige im Modelüberblick
	 * @param agents	Liste der Agenten in einem Callcenter
	 * @param callcenter	Callcenter
	 * @return	Liste der Schichtplan-Agenten
	 */
	private List<CallcenterModelAgent> calcShiftPlan(final List<CallcenterModelAgent> agents, final CallcenterModelCallcenter callcenter) {
		List<CallcenterModelAgent> agentsList=new ArrayList<CallcenterModelAgent>();
		for (int i=0;i<agents.size();i++) if (agents.get(i).active) {
			if (agents.get(i).count>=0) agentsList.add(agents.get(i)); else agentsList.addAll(agents.get(i).calcAgentShifts(false,callcenter,model,true));
		}
		return agentsList;
	}

	/**
	 * Renderer für die Liste der Anrufergruppen
	 * @see CallcenterModelEditorPanel#callerList
	 */
	private class CallerListRenderer extends AdvancedListCellRenderer {
		/**
		 * Serialisierungs-ID der Klasse
		 * @see Serializable
		 */
		private static final long serialVersionUID = -3181757143428275248L;

		/**
		 * Kann sich der Kundentyp für Kunden eines bestimmten Typs bei einer Wiederholung verändern?
		 * @param caller	Zu prüfender Kundentyp
		 * @return	Liefert <code>true</code>, wenn die Kunden dieses Typs bei einer Wiederholung unter einem neuen Typ auftreten können
		 */
		private boolean callerTypeMayChangeOnRetry(final CallcenterModelCaller caller) {
			for (int i=0;i<caller.retryCallerTypeRateAfterBlockedFirstRetry.size();i++) if (caller.retryCallerTypeRateAfterBlockedFirstRetry.get(i)>0) return true;
			for (int i=0;i<caller.retryCallerTypeRateAfterBlocked.size();i++) if (caller.retryCallerTypeRateAfterBlocked.get(i)>0) return true;
			for (int i=0;i<caller.retryCallerTypeRateAfterGiveUpFirstRetry.size();i++) if (caller.retryCallerTypeRateAfterGiveUpFirstRetry.get(i)>0) return true;
			for (int i=0;i<caller.retryCallerTypeRateAfterGiveUp.size();i++) if (caller.retryCallerTypeRateAfterGiveUp.get(i)>0) return true;
			return false;
		}

		@Override
		protected void buildString(Object value, int index, StringBuilder s) {
			CallcenterModelCaller c=(CallcenterModelCaller)value;
			addName(s,c.name,c.active);
			addBr(s);

			String t="";
			if (c.freshCallsCountSD>0) t="; "+Language.tr("Distribution.StdDev")+" "+NumberTools.formatNumber(c.freshCallsCountSD,1);
			if (c.freshCallsCountMean==1) addColor(s,Language.tr("Editor.Caller.List.CallsPerDaySingle")+t,"green"); else addColor(s,String.format(Language.tr("Editor.Caller.List.CallsPerDayMultiple"),c.freshCallsCountMean)+t,"green");
			addBr(s);

			s.append(String.format(Language.tr("Editor.Caller.List.Score"),NumberTools.formatNumberMax(c.scoreBase),NumberTools.formatNumberMax(c.scoreContinued),NumberTools.formatNumberMax(c.scoreSecond)));
			addBr(s);

			switch (c.waitingTimeMode) {
			case CallcenterModelCaller.WAITING_TIME_MODE_SHORT:
				s.append(Language.tr("Editor.Caller.List.WaitingTimeTolerance")+": ");
				if (c.waitingTimeDist instanceof DataDistributionImpl)
					s.append(Language.tr("Editor.Caller.List.WaitingTimeTolerance.EmpiricalDistribution")); else s.append(DistributionTools.getDistributionName(c.waitingTimeDist)+", "+DistributionTools.getDistributionInfo(c.waitingTimeDist));
				addBr(s);
				break;
			case CallcenterModelCaller.WAITING_TIME_MODE_LONG:
				s.append(Language.tr("Editor.Caller.List.WaitingTimeTolerance")+": ");
				if (c.waitingTimeDistLong instanceof DataDistributionImpl)
					s.append(Language.tr("Editor.Caller.List.WaitingTimeTolerance.EmpiricalDistribution")); else s.append(DistributionTools.getDistributionName(c.waitingTimeDistLong)+", "+DistributionTools.getDistributionInfo(c.waitingTimeDistLong));
				addBr(s);
				break;
			case CallcenterModelCaller.WAITING_TIME_MODE_CALC:
				s.append(Language.tr("Editor.Caller.List.WaitingTimeTolerance")+": ");
				s.append(String.format(Language.tr("Editor.Caller.List.WaitingTimeTolerance.Extrapolation"),TimeTools.formatExactTime(c.waitingTimeCalcMeanWaitingTime),NumberTools.formatPercent(c.waitingTimeCalcCancelProbability),TimeTools.formatExactTime(c.waitingTimeCalcAdd)));
				addBr(s);
				break;
			}

			if (c.retryProbabiltyAfterBlocked>0 || c.retryProbabiltyAfterBlockedFirstRetry>0 || c.retryProbabiltyAfterGiveUp>0 || c.retryProbabiltyAfterGiveUpFirstRetry>0) {
				double min=c.retryProbabiltyAfterBlocked;
				double max=c.retryProbabiltyAfterBlocked;
				min=Math.min(min,c.retryProbabiltyAfterBlockedFirstRetry);
				max=Math.max(max,c.retryProbabiltyAfterBlockedFirstRetry);
				min=Math.min(min,c.retryProbabiltyAfterGiveUp);
				max=Math.max(max,c.retryProbabiltyAfterGiveUp);
				min=Math.min(min,c.retryProbabiltyAfterGiveUpFirstRetry);
				max=Math.max(max,c.retryProbabiltyAfterGiveUpFirstRetry);
				if (min==max) {
					s.append("P("+Language.tr("Editor.Caller.List.Retry")+")="+NumberTools.formatNumberMax(max*100)+"%");
				} else {
					s.append("P("+Language.tr("Editor.Caller.List.Retry")+")="+NumberTools.formatNumberMax(min*100)+"%..."+NumberTools.formatNumberMax(max*100)+"%");

				}
				if (callerTypeMayChangeOnRetry(c)) s.append(", "+Language.tr("Editor.Caller.List.Retry.CallerTypeCanChange"));
				s.append(", ");
			}

			s.append("P("+Language.tr("Editor.Caller.List.Forwarding")+")="+NumberTools.formatNumberMax(c.continueProbability*100)+"%");

			if (c.recallProbability>0) {
				s.append(", P("+Language.tr("Editor.Caller.List.Recall")+")="+NumberTools.formatNumberMax(c.recallProbability*100)+"%");
			}
		}

		@Override
		protected Icon getIcon(Object value) {
			CallcenterModelCaller c=(CallcenterModelCaller)value;
			return c.active?Images.EDITOR_CALLER_BIG.getIcon():Images.EDITOR_CALLER_BIG_DISABLED.getIcon();
		}
	}

	/**
	 * Renderer für die Skill-Level-Liste
	 * @see CallcenterModelEditorPanel#callcenterList
	 */
	private class CallcenterListRenderer extends AdvancedListCellRenderer {
		/**
		 * Serialisierungs-ID der Klasse
		 * @see Serializable
		 */
		private static final long serialVersionUID = 747140015293848146L;

		@Override
		protected void buildString(Object value, int index, StringBuilder s) {
			CallcenterModelCallcenter c=(CallcenterModelCallcenter)value;
			addName(s,c.name,c.active);
			addBr(s);

			StringBuilder skills=new StringBuilder();
			int activeCount=0;
			int halfHourCount=0;
			String active="";
			boolean dotsAdded=false;
			for (int i=0;i<c.agents.size();i++) {
				CallcenterModelAgent a=c.agents.get(i);
				if (!a.active) {active=" "+Language.tr("Editor.Callcenter.List.AgentsActive"); continue;}
				activeCount++;
				if (skills.length()>=100) {
					if (!dotsAdded) {dotsAdded=true; skills.append(", ...");}
				} else {
					if (skills.length()>0) skills.append(", ");
					skills.append(a.skillLevel);
				}
				if (a.count==-1) {
					if (a.countPerInterval24!=null) halfHourCount+=a.countPerInterval24.sum()*2;
					if (a.countPerInterval48!=null) halfHourCount+=a.countPerInterval48.sum();
					if (a.countPerInterval96!=null) halfHourCount+=a.countPerInterval96.sum()/2;
				} else {
					if (a.count==-2) halfHourCount+=a.byCallersAvailableHalfhours; else halfHourCount+=Math.round((double)a.count*(a.workingTimeEnd-a.workingTimeStart)/1800);
				}
			}
			if (skills.length()>0) skills=new StringBuilder(" ("+skills.toString()+")");
			if (activeCount==1) addColor(s,"1 "+active+Language.tr("Editor.Callcenter.List.AgentGroup")+" "+skills,"green"); else addColor(s,activeCount+active+" "+Language.tr("Editor.Callcenter.List.AgentGroups")+skills,"green");
			addBr(s);

			if (halfHourCount==1) s.append(Language.tr("Editor.Callcenter.Count.HalfHourIntervalSingle")); else s.append(String.format(Language.tr("Editor.Callcenter.Count.HalfHourIntervalMultiple"),halfHourCount));
			addBr(s);

			s.append(String.format(Language.tr("Editor.Callcenter.List.Score"),""+c.score));
			addBr(s);

			s.append(String.format(Language.tr("Editor.Callcenter.List.TechnicalFreeTime"),""+c.technicalFreeTime));
			if (!c.technicalFreeTimeIsWaitingTime) s.append(" ("+Language.tr("Editor.Callcenter.List.TechnicalFreeTime.NoCancel")+")");
			List<String> time=new ArrayList<String>();
			for (int j=0;j<c.callerMinWaitingTimeName.size();j++) if (c.callerMinWaitingTime.get(j)>0) time.add(c.callerMinWaitingTimeName.get(j));
			if (time.size()!=0) {addBr(s); s.append(Language.tr("Editor.Callcenter.List.MinimumWaitingTimeFor")+" "+time.get(0)); for (int j=1;j<time.size();j++) s.append(", "+time.get(j));}
		}

		@Override
		protected Icon getIcon(Object value) {
			CallcenterModelCallcenter c=(CallcenterModelCallcenter)value;
			return c.active?Images.EDITOR_CALLCENTER_BIG.getIcon():Images.EDITOR_CALLCENTER_BIG_DISABLED.getIcon();
		}

	}

	/**
	 * Renderer für die Skill-Level-Liste
	 * @see CallcenterModelEditorPanel#skillLevelList
	 */
	private class SkillLevelListRenderer extends AdvancedListCellRenderer {
		/**
		 * Serialisierungs-ID der Klasse
		 * @see Serializable
		 */
		private static final long serialVersionUID = -838162614370052084L;

		@Override
		protected void buildString(Object value, int index, StringBuilder s) {
			CallcenterModelSkillLevel c=(CallcenterModelSkillLevel)value;
			addName(s,c.name,true);
			addBr(s);

			StringBuilder t=new StringBuilder();
			t.append(Language.tr("Editor.SkillLevel.List.ClientTypes")+": ");
			if (c.callerTypeName.size()==0) t.append(Language.tr("Editor.SkillLevel.List.ClientTypes.None"));
			for (int j=0;j<c.callerTypeName.size();j++) {
				t.append(c.callerTypeName.get(j));
				if (j<c.callerTypeName.size()-1) t.append(", ");
			}
			addColor(s,t.toString(),"green");
		}

		@Override
		protected Icon getIcon(Object value) {
			return Images.EDITOR_SKILLLEVEL_BIG.getIcon();
		}
	}

	/**
	 * Ruft den HTML-Report-Generator auf und speichert die Modell-Informationen in der angegebenen Form in der angegebenen Datei.
	 * @param outputFile	Dateiname, in der der HTML-Report gespeichert werden soll.
	 * @param inline	Gibt an, ob die Grafiken direkt in die HTML-Datei eingebettet werden sollen.
	 * @return	Gibt an, ob der Report erfolgreich erstellt werden konnte.
	 */
	@Override
	public boolean runReportGeneratorHTML(File outputFile, boolean inline, boolean exportAllItems) {
		updateStatistics();
		return statistics.runReportGeneratorHTML(outputFile,inline,exportAllItems);
	}

	/**
	 * Ruft den DOCX-Report-Generator auf und speichert die Modell-Informationen in der angegebenen Form in der angegebenen Datei.
	 * @param outputFile	Dateiname, in der der DOCX-Report gespeichert werden soll.
	 * @return	Gibt an, ob der Report erfolgreich erstellt werden konnte.
	 */
	@Override
	public boolean runReportGeneratorDOCX(File outputFile, boolean exportAllItems) {
		updateStatistics();
		return statistics.runReportGeneratorDOCX(outputFile,exportAllItems);
	}

	/**
	 * Ruft den PDF-Report-Generator auf und speichert die Modell-Informationen in der angegebenen Form in der angegebenen Datei.
	 * @param outputFile	Dateiname, in der der PDF-Report gespeichert werden soll.
	 * @return	Gibt an, ob der Report erfolgreich erstellt werden konnte.
	 */
	@Override
	public boolean runReportGeneratorPDF(File outputFile, boolean exportAllItems) {
		updateStatistics();
		return statistics.runReportGeneratorPDF(outputFile,exportAllItems);
	}

	/**
	 * Speichert eine Liste aller Einträge im Statistikbaum als Datei
	 * @param output	Dateiname, in der die Liste gespeichert werden soll.
	 * @return	Gibt an, ob die Liste erfolgreich gespeichert werden konnte.
	 */
	@Override
	public boolean getReportList(File output) {
		updateStatistics();
		return statistics.getReportList(output);
	}

	/**
	 * Speichert ein einzelnes Dokument aus dem Statisitikbaum als Datei
	 * @param output	Dateiname, in dem das Dokument gespeichert werden soll.
	 * @param entry	Gibt den Namen des Dokuments im Statistikbaum an.
	 * @return	Gibt an, ob das Dokument erfolgreich gespeichert werden konnte.
	 */
	@Override
	public boolean getReportListEntry(File output, String entry) {
		updateStatistics();
		return statistics.getReportListEntry(output,entry);
	}
}
