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
import java.awt.FlowLayout;
import java.awt.GridLayout;
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
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.DefaultListModel;
import javax.swing.Icon;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JCheckBoxMenuItem;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JMenu;
import javax.swing.JMenuItem;
import javax.swing.JPanel;
import javax.swing.JPopupMenu;
import javax.swing.JScrollPane;
import javax.swing.JTabbedPane;
import javax.swing.JTextField;
import javax.swing.JToolBar;
import javax.swing.KeyStroke;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;

import language.Language;
import mathtools.MultiTable;
import mathtools.NumberTools;
import mathtools.Table;
import mathtools.TimeTools;
import systemtools.MsgBox;
import ui.HelpLink;
import ui.editor.events.RenameEvent;
import ui.editor.events.RenameListener;
import ui.images.Images;
import ui.model.CallcenterModel;
import ui.model.CallcenterModelAgent;
import ui.model.CallcenterModelCallcenter;

/**
 * Diese Klasse kapselt einen kompletten Bearbeitungsdialog für Callcenter.
 * @author Alexander Herzog
 * @version 1.0
 * @see CallcenterModelCallcenter
 */
public class CallcenterEditDialog extends BaseEditDialog {
	/**
	 * Serialisierungs-ID der Klasse
	 * @see Serializable
	 */
	private static final long serialVersionUID = -4444119481485832099L;

	/** Index dieses Callcenters in der Liste aller Callcenter */
	private final int indexForThisCallcenter;
	/** Listen mit allen Callcenter-Namen (um doppelte Namen zu verhindern) */
	private final String[] callcenterNames;

	/** Objekt vom Typ <code>CallcenterModelCallcenter</code> welches die Callcenter-Daten enthält (beim Klicken auf "Ok" wird auch dieses Objekt verändert) */
	private final CallcenterModelCallcenter callcenter;
	/** Objekt vom Typ <code>CallcenterModel</code> welches das globale Modell enthält */
	private final CallcenterModel model;

	/**
	 * Liste der Listener die über Skill-Level-Namensänderungen benachrichtigt werden sollen
	 * @see SkillLevelRenameListener
	 */
	private final List<RenameListener> listener;

	/* Dialogseite "Agenten" */

	/** Schaltfläche "Hinzufügen" */
	private JButton agentAddButton;
	/** Schaltfläche "Bearbeiten" */
	private JButton agentEditButton;
	/** Schaltfläche "Löschen" */
	private JButton agentDeleteButton;
	/** Schaltfläche "Kopieren" */
	private JButton agentCopyButton;
	/** Schaltfläche "Nach oben" */
	private JButton agentUpButton;
	/** Schaltfläche "Nach unten" */
	private JButton agentDownButton;
	/** Schaltfläche "Tools" */
	private JButton agentToolsButton;

	/** Liste der Agentengruppen in diesem Callcenter */
	private List<CallcenterModelAgent> agents;
	/** Listenmodell für {@link #agentList} zur Darstellung der Agentengruppen in diesem Callcenter  */
	private DefaultListModel<CallcenterModelAgent> agentListData;
	/** Listendarstellung der Agentengruppen in diesem Callcenter */
	private JList<CallcenterModelAgent> agentList;

	/* Dialogseite "Callcenter-Parameter" */

	/** Eingabefeld für die technische Bereitzeit */
	private JTextField technicalFreeTime;
	/** Option: Die technische Bereitzeit wird als Wartezeit empfunden */
	private JCheckBox technicalFreeTimeIsWaitingTime;
	/** Eingabefeld für den Score des Callcenters*/
	private JTextField score;
	/** Eingabefeld "Faktor für die Agentenscore zur Berücksichtigung der freien Zeit seit dem letzten Anruf" */
	private JTextField agentScoreFreeTimeSinceLastCall;
	/** Eingabefeld "Faktor für die Agentenscore zur Berücksichtigung des Leerlaufanteils" */
	private JTextField agentScoreFreeTimePart;
	/** "Produktivität der Agentengruppen in diesem Callcenter"-Schaltfläche */
	private JButton efficiency;
	/** "Krankheitsbedingter Zuschlag in diesem Callcenter"-Schaltfläche */
	private JButton addition;

	/* Dialogseite "Mindestwartezeiten" */

	/** Eingabefelder für die Mindestwartezeiten nach Kundentypen */
	private JTextField[] waitingTimeByType;

	/** Popupmenü für die Kosten-Dialogseite */
	private final JPopupMenu popupMenu2;
	/** Menüpunkte zum Übertragen der Einstellungen dieser Dialogseite von dieser Gruppe zu anderen Gruppen */
	private final JMenuItem[] applyThisPage;
	/** Menüpunkte zum Übertragen der Einstellungen aller Dialogseiten von dieser Gruppe zu anderen Gruppen */
	private final JMenuItem[] applyAllPages;

	/** Popupmenü zum Verändern der Agentenanzahlen in allen Intervallen ({@link #agentToolsButton}) */
	private JPopupMenu popupMenu;
	/** Menüpunkt "Agentenanzahl in allen Intervallen in allen Gruppen ändern" */
	private JMenuItem popupItem1;
	/** Menüpunkt "Agentenarbeitszeiten aller Agentengruppen auf Stundenbasis global laden" */
	private JMenuItem popupItem2a;
	/** Menüpunkt "Agentenarbeitszeiten aller Agentengruppen auf Halbstundenbasis global laden" */
	private JMenuItem popupItem2b;
	/** Menüpunkt "Agentenarbeitszeiten aller Agentengruppen auf 15-Minuten-Basis global laden" */
	private JMenuItem popupItem2c;
	/** Menüpunkt "Produktivität aller Agentengruppen global laden" */
	private JMenuItem popupItem3;
	/** Menüpunkt "Krankheitsbedingten Zuschlag aller Agentengruppen global laden" */
	private JMenuItem popupItem4;
	/** Menüpunkt "Produktivität aller Agentengruppen in diesem Callcenter einstellen" */
	private JMenuItem popupItem5;
	/** Menüpunkt "Krankheitsbedingten Zuschlag aller Agentengruppen in diesem Callcenter einstellen" */
	private JMenuItem popupItem6;
	/** Menüpunkt "Agentengruppen mit festen Arbeitszeiten aus Tabelle laden" */
	private JMenuItem popupItem7;

	/** Verknüpfung mit der Online-Hilfe */
	private final HelpLink helpLink;

	/**
	 * Dialog zum Laden von Agentenarbeitszeiten auf Stundenbasis öffnen?
	 * @see #isOpenAgentsGenerator24()
	 */
	private boolean openAgentsGenerator24=false;

	/**
	 * Dialog zum Laden von Agentenarbeitszeiten auf Halbstundenbasis öffnen?
	 * @see #isOpenAgentsGenerator48()
	 */
	private boolean openAgentsGenerator48=false;

	/**
	 * Dialog zum Laden von Agentenarbeitszeiten auf Viertelstundenbasis öffnen?
	 * @see #isOpenAgentsGenerator96()
	 */
	private boolean openAgentsGenerator96=false;

	/**
	 * Dialog zum Laden von Agentenproduktivitäten öffnen?
	 * @see #isOpenAgentsEfficiencyGenerator()
	 */
	private boolean openAgentsEfficiencyGenerator=false;

	/**
	 * Dialog zum Laden von krankheitsbedingten Zuschlägen öffnen?
	 * @see #isOpenAgentsAdditionGenerator()
	 */
	private boolean openAgentsAdditionGenerator=false;

	/**
	 * Konstruktor der Klasse <code>CallcenterEditorDialog</code>
	 * @param owner	Übergeordnetes Fenster
	 * @param callcenter	Objekt vom Typ <code>CallcenterModelCallcenter</code> welches die Callcenter-Daten enthält (beim Klicken auf "Ok" wird auch dieses Objekt verändert)
	 * @param model	Objekt vom Typ <code>CallcenterModel</code> welches das globale Modell enthält
	 * @param callerTypeNames	Liste mit allen Kundentypen-Namen (für die Agenten-Skills)
	 * @param callcenterNames Listen mit allen Callcenter-Namen (um doppelte Namen zu verhindern)
	 * @param readOnly	Wird dieser Parameter auf <code>true</code> gesetzt, so wird die "Ok"-Schaltfläche deaktiviert
	 * @param previous Button "Vorheriger Callcenter" anzeigen
	 * @param next Button "Nächstes Callcenter" anzeigen
	 * @param helpLink Verknüpfung mit der Online-Hilfe
	 */
	public CallcenterEditDialog(final Window owner, final CallcenterModelCallcenter callcenter, final CallcenterModel model, final String[] callerTypeNames, final String[] callcenterNames, final boolean readOnly, final boolean previous, final boolean next, final HelpLink helpLink) {
		super(owner,Language.tr("Editor.Callcenter.Title"),callerTypeNames,readOnly,helpLink.dialogCallcenter);
		this.callcenter=callcenter;
		this.model=model;
		this.callcenterNames=callcenterNames;
		this.helpLink=helpLink;
		indexForThisCallcenter=Arrays.asList(callcenterNames).indexOf(this.callcenter.name);

		popupMenu2=new JPopupMenu();
		applyThisPage=new JMenuItem[callcenterNames.length+1];
		applyAllPages=new JMenuItem[callcenterNames.length+1];
		if (!readOnly && callcenterNames.length>1) {
			buildMenu();
			addUserButtons(
					new String[]{""},
					new String[]{Language.tr("Editor.Callcenter.Apply.Info")},
					new Icon[]{Images.GENERAL_TOOLS.getIcon()},
					new Runnable[]{()->{final JButton b=getUserButton(0); popupMenu2.show(b,0,b.getHeight());}}
					);
		}

		String previousText=null;
		if (previous) previousText=readOnly?Language.tr("Editor.Callcenter.Move.ViewPrevious"):Language.tr("Editor.Callcenter.Move.EditPrevious");
		String nextText=null;
		if (next) nextText=readOnly?Language.tr("Editor.Callcenter.Move.ViewNext"):Language.tr("Editor.Callcenter.Move.EditNext");
		listener=new ArrayList<>();
		createTabsGUI(Language.tr("Editor.Callcenter.Name")+":",callcenter.name,Language.tr("Editor.Callcenter.Active"),callcenter.active,775,500,previousText,nextText);

		if (!readOnly && callcenterNames.length>1) {
			getUserButton(0).setVisible(false);
			tabs.addChangeListener(e->getUserButton(0).setVisible(tabs.getSelectedIndex()>0));
		}
	}

	/**
	 * Erstellt das Menü für die Tools-Schaltfläche (zum Übertragen der Daten zu den anderen Gruppen)
	 * @see #popupMenu2
	 */
	private void buildMenu() {
		final Icon callcenter=Images.EDITOR_CALLCENTER.getIcon();

		JMenu m;
		popupMenu2.add(m=new JMenu(Language.tr("Editor.Callcenter.Apply.ThisPage")));
		m.setIcon(Images.EDITOR_APPLY_SINGLE.getIcon());

		for (int i=0;i<callcenterNames.length;i++) {
			m.add(applyThisPage[i]=new JMenuItem(callcenterNames[i]));
			applyThisPage[i].setIcon(callcenter);
			applyThisPage[i].setEnabled(i!=indexForThisCallcenter);
			applyThisPage[i].addActionListener(new PopupActionListener());
		}
		if (callcenterNames.length>2) {
			m.addSeparator();
			m.add(applyThisPage[callcenterNames.length]=new JMenuItem(Language.tr("Editor.Callcenter.Apply.ForAllCallcenters")));
			applyThisPage[callcenterNames.length].setIcon(callcenter);
			applyThisPage[callcenterNames.length].addActionListener(new PopupActionListener());
		}

		popupMenu2.add(m=new JMenu(Language.tr("Editor.Callcenter.Apply.AllPages")));
		m.setIcon(Images.EDITOR_APPLY_ALL.getIcon());

		for (int i=0;i<callcenterNames.length;i++) {
			m.add(applyAllPages[i]=new JMenuItem(callcenterNames[i]));
			applyAllPages[i].setIcon(callcenter);
			applyAllPages[i].setEnabled(i!=indexForThisCallcenter);
			applyAllPages[i].addActionListener(new PopupActionListener());
		}
		if (callcenterNames.length>2) {
			m.addSeparator();
			m.add(applyAllPages[callcenterNames.length]=new JMenuItem(Language.tr("Editor.Callcenter.Apply.ForAllCallcenters")));
			applyAllPages[callcenterNames.length].setIcon(callcenter);
			applyAllPages[callcenterNames.length].addActionListener(new PopupActionListener());
		}
	}

	/**
	 * Die hier registrierten Listener werden benachrichtigt, wenn
	 * in dem Dialog ein Skill-Level umbenannt wird.
	 * @param listener	Listener für Skill-Level-Namensänderungen
	 * @see #removeSkillLevelRenameListener(RenameListener)
	 */
	public void addSkillLevelRenameListener(final RenameListener listener) {
		this.listener.add(listener);
	}

	/**
	 * Entfernt einen Listener aus der Liste der Listener die über
	 * Skill-Level-Namensänderungen benachrichtigt werden sollen.
	 * @param listener	Listener für Skill-Level-Namensänderungen
	 * @return	Gibt an, ob der Listener erfolgreich deregistriert werden konnte
	 * @see #addSkillLevelRenameListener(RenameListener)
	 */
	public boolean removeSkillLevelRenameListener(final RenameListener listener) {
		return this.listener.remove(listener);
	}

	/**
	 * Wurde in dem Dialog ausgewählt, dass die Funktion zum Laden
	 * von Agentenarbeitszeiten auf Stundenbasis geöffnet werden soll?
	 * @return	Dialog zum Laden von Agentenarbeitszeiten auf Stundenbasis öffnen?
	 */
	public boolean isOpenAgentsGenerator24() {
		return openAgentsGenerator24;
	}

	/**
	 * Wurde in dem Dialog ausgewählt, dass die Funktion zum Laden
	 * von Agentenarbeitszeiten auf Halbstundenbasis geöffnet werden soll?
	 * @return	Dialog zum Laden von Agentenarbeitszeiten auf Halbstundenbasis öffnen?
	 */
	public boolean isOpenAgentsGenerator48() {
		return openAgentsGenerator48;
	}

	/**
	 * Wurde in dem Dialog ausgewählt, dass die Funktion zum Laden
	 * von Agentenarbeitszeiten auf Viertelstundenbasis geöffnet werden soll?
	 * @return	Dialog zum Laden von Agentenarbeitszeiten auf Viertelstundenbasis öffnen?
	 */
	public boolean isOpenAgentsGenerator96() {
		return openAgentsGenerator96;
	}

	/**
	 * Wurde in dem Dialog ausgewählt, dass die Funktion zum Laden
	 * von Agentenproduktivitäten geöffnet werden soll?
	 * @return	Dialog zum Laden von Agentenproduktivitäten öffnen?
	 */
	public boolean isOpenAgentsEfficiencyGenerator() {
		return openAgentsEfficiencyGenerator;
	}

	/**
	 * Wurde in dem Dialog ausgewählt, dass die Funktion zum Laden
	 * von krankheitsbedingten Zuschlägen geöffnet werden soll?
	 * @return	Dialog zum Laden von krankheitsbedingten Zuschlägen öffnen?
	 */
	public boolean isOpenAgentsAdditionGenerator() {
		return openAgentsAdditionGenerator;
	}

	@Override
	protected void createTabs(JTabbedPane tabs) {
		JPanel p,p2,p3,p4;
		JToolBar toolbar;

		/* Agenten */
		tabs.addTab(Language.tr("Editor.Callcenter.Tabs.Agents"),p=new JPanel(new BorderLayout()));
		agents=new ArrayList<>();
		for (int i=0;i<callcenter.agents.size();i++) agents.add(callcenter.agents.get(i).clone());
		p.add(new JScrollPane(agentList=new JList<>(agentListData=new DefaultListModel<>())),BorderLayout.CENTER);
		agentList.setCellRenderer(new AgentListRenderer());
		agentList.addMouseListener(new ListListener());
		agentList.addKeyListener(new ListListener());
		agentList.addListSelectionListener(new ListListener());
		updateAgentsList();
		toolbar=new JToolBar();
		toolbar.setFloatable(false);
		toolbar.add(agentAddButton=new JButton(Language.tr("Dialog.Button.Add")));
		agentAddButton.setToolTipText(Language.tr("Editor.Callcenter.Agents.Add.Info"));
		agentAddButton.addActionListener(new ButtonActionListener());
		agentAddButton.setEnabled(!readOnly);
		agentAddButton.setIcon(Images.EDITOR_AGENTS_ADD.getIcon());
		toolbar.add(agentEditButton=new JButton(readOnly?Language.tr("Dialog.Button.Show"):Language.tr("Dialog.Button.Edit")));
		agentEditButton.setToolTipText(Language.tr("Editor.Callcenter.Agents.Edit.Info"));
		agentEditButton.addActionListener(new ButtonActionListener());
		agentEditButton.setEnabled(false);
		agentEditButton.setIcon(Images.EDITOR_AGENTS_EDIT.getIcon());
		toolbar.add(agentDeleteButton=new JButton(Language.tr("Dialog.Button.Delete")));
		agentDeleteButton.addActionListener(new ButtonActionListener());
		agentDeleteButton.setToolTipText(Language.tr("Editor.Callcenter.Agents.Delete.Info"));
		agentDeleteButton.setEnabled(false);
		agentDeleteButton.setIcon(Images.EDITOR_AGENTS_DELETE.getIcon());
		toolbar.add(agentCopyButton=new JButton(Language.tr("Dialog.Button.Copy")));
		agentCopyButton.addActionListener(new ButtonActionListener());
		agentCopyButton.setToolTipText(Language.tr("Editor.Callcenter.Agents.Copy.Info"));
		agentCopyButton.setEnabled(false);
		agentCopyButton.setIcon(Images.EDITOR_AGENTS_COPY.getIcon());
		toolbar.addSeparator();
		toolbar.add(agentUpButton=new JButton(Language.tr("Dialog.Button.Up")));
		agentUpButton.addActionListener(new ButtonActionListener());
		agentUpButton.setToolTipText(Language.tr("Editor.Callcenter.Agents.Up.Info"));
		agentUpButton.setEnabled(false);
		agentUpButton.setIcon(Images.ARROW_UP.getIcon());
		toolbar.add(agentDownButton=new JButton(Language.tr("Dialog.Button.Down")));
		agentDownButton.addActionListener(new ButtonActionListener());
		agentDownButton.setToolTipText(Language.tr("Editor.Callcenter.Agents.Down.Info"));
		agentDownButton.setEnabled(false);
		agentDownButton.setIcon(Images.ARROW_DOWN.getIcon());
		toolbar.addSeparator();
		toolbar.add(agentToolsButton=new JButton(Language.tr("Dialog.Button.Tools")));
		agentToolsButton.addActionListener(new ButtonActionListener());
		agentToolsButton.setIcon(Images.GENERAL_SETUP.getIcon());
		p.add(toolbar,BorderLayout.NORTH);

		popupMenu=new JPopupMenu();
		popupMenu.add(popupItem1=new JMenuItem(Language.tr("Editor.AgentsGroup.Tools.ChangeAllIntervals.AllGroups")));
		popupItem1.addActionListener(new ButtonActionListener());
		popupItem1.setEnabled(!readOnly);
		popupItem1.setIcon(Images.GENERAL_TOOLS.getIcon());
		popupMenu.addSeparator();
		popupMenu.add(popupItem2a=new JMenuItem(Language.tr("Editor.Callcenter.Tools.LoadWorkingTimes24")));
		popupItem2a.addActionListener(new ButtonActionListener());
		popupItem2a.setEnabled(!readOnly);
		popupItem2a.setIcon(Images.EDITOR_AGENTS.getIcon());
		popupMenu.add(popupItem2b=new JMenuItem(Language.tr("Editor.Callcenter.Tools.LoadWorkingTimes48")));
		popupItem2b.addActionListener(new ButtonActionListener());
		popupItem2b.setEnabled(!readOnly);
		popupItem2b.setIcon(Images.EDITOR_AGENTS.getIcon());
		popupMenu.add(popupItem2c=new JMenuItem(Language.tr("Editor.Callcenter.Tools.LoadWorkingTimes96")));
		popupItem2c.addActionListener(new ButtonActionListener());
		popupItem2c.setEnabled(!readOnly);
		popupItem2c.setIcon(Images.EDITOR_AGENTS.getIcon());
		popupMenu.add(popupItem3=new JMenuItem(Language.tr("Editor.Callcenter.Tools.LoadEfficiencyGlobal")));
		popupItem3.addActionListener(new ButtonActionListener());
		popupItem3.setEnabled(!readOnly);
		popupItem3.setIcon(Images.EDITOR_AGENTS_EFFICIENCY.getIcon());
		popupMenu.add(popupItem4=new JMenuItem(Language.tr("Editor.Callcenter.Tools.LoadSurchargeGlobal")));
		popupItem4.addActionListener(new ButtonActionListener());
		popupItem4.setEnabled(!readOnly);
		popupItem4.setIcon(Images.EDITOR_AGENTS_ADDITION.getIcon());
		popupMenu.addSeparator();
		popupMenu.add(popupItem5=new JMenuItem(Language.tr("Editor.Callcenter.Tools.LoadEfficiency")));
		popupItem5.addActionListener(new ButtonActionListener());
		popupItem5.setIcon(Images.EDITOR_AGENTS_EFFICIENCY.getIcon());
		popupMenu.add(popupItem6=new JMenuItem(Language.tr("Editor.Callcenter.Tools.LoadSurcharge")));
		popupItem6.addActionListener(new ButtonActionListener());
		popupItem6.setIcon(Images.EDITOR_AGENTS_ADDITION.getIcon());
		popupMenu.addSeparator();
		popupMenu.add(popupItem7=new JMenuItem(Language.tr("Editor.Callcenter.Tools.LoadFixedGroupsFromFile")));
		popupItem7.addActionListener(new ButtonActionListener());
		popupItem7.setEnabled(!readOnly);
		popupItem7.setIcon(Images.EDITOR_AGENTS_ADD.getIcon());

		/* Callcenter-Parameter */
		tabs.addTab(Language.tr("Editor.Callcenter.Tabs.CallcenterParameters"),p=new JPanel());
		p.setLayout(new BoxLayout(p,BoxLayout.Y_AXIS));
		p.add(p3=new JPanel());
		p3.setLayout(new BoxLayout(p3,BoxLayout.X_AXIS));
		p3.add(Box.createHorizontalStrut(5));
		p3.add(p2=new JPanel(new GridLayout(9,1)));
		technicalFreeTime=addInputLine(p2,Language.tr("Editor.Callcenter.TechnicalFreeTime")+" ("+Language.tr("Statistic.Units.InSeconds")+"):",callcenter.technicalFreeTime);
		technicalFreeTime.setEditable(!readOnly);
		technicalFreeTime.addKeyListener(new DialogElementListener());
		p2.add(technicalFreeTimeIsWaitingTime=new JCheckBox(Language.tr("Editor.Callcenter.TechnicalFreeTime.Info"),callcenter.technicalFreeTimeIsWaitingTime));
		technicalFreeTimeIsWaitingTime.setEnabled(!readOnly);
		score=addInputLine(p2,Language.tr("Editor.Callcenter.Score.Callcenter")+":",callcenter.score);
		score.setEditable(!readOnly);
		score.addKeyListener(new DialogElementListener());
		agentScoreFreeTimeSinceLastCall=addInputLine(p2,Language.tr("Editor.Callcenter.Score.AgentsFreeTime")+":",callcenter.agentScoreFreeTimeSinceLastCall);
		agentScoreFreeTimeSinceLastCall.setEditable(!readOnly);
		agentScoreFreeTimeSinceLastCall.addKeyListener(new DialogElementListener());
		agentScoreFreeTimePart=addInputLine(p2,Language.tr("Editor.Callcenter.Score.AgentsFreeTimePart")+":",callcenter.agentScoreFreeTimePart);
		agentScoreFreeTimePart.setEditable(!readOnly);
		agentScoreFreeTimePart.addKeyListener(new DialogElementListener());
		p.add(p2=new JPanel(new FlowLayout(FlowLayout.LEFT)));
		p2.add(efficiency=new JButton(Language.tr("Editor.Callcenter.AgentGroupsProductivity")));
		efficiency.addActionListener(new ButtonActionListener());
		efficiency.setIcon(Images.EDITOR_AGENTS_EFFICIENCY.getIcon());
		p.add(p2=new JPanel(new FlowLayout(FlowLayout.LEFT)));
		p2.add(addition=new JButton(Language.tr("Editor.Callcenter.DiseaseRelatedSurcharge")));
		addition.addActionListener(new ButtonActionListener());
		addition.setIcon(Images.EDITOR_AGENTS_ADDITION.getIcon());
		p.add(Box.createVerticalGlue());

		/* Mindestwartezeiten */
		tabs.addTab(Language.tr("Editor.Callcenter.Tabs.MinimumWaitingTimes"),p=new JPanel(new BorderLayout()));
		p3=new JPanel(new GridLayout(callerTypeNames.length,2));
		waitingTimeByType=new JTextField[callerTypeNames.length];
		for (int i=0;i<callerTypeNames.length;i++) {
			JLabel l=new JLabel(callerTypeNames[i]);
			l.setIcon(Images.EDITOR_CALLER.getIcon());
			p3.add(l);
			waitingTimeByType[i]=new JTextField(10);
			int w=0;
			for (int j=0;j<callcenter.callerMinWaitingTimeName.size();j++) if (callcenter.callerMinWaitingTimeName.get(j).equalsIgnoreCase(callerTypeNames[i])) {
				w=callcenter.callerMinWaitingTime.get(j); break;
			}
			waitingTimeByType[i].setText(""+w);
			waitingTimeByType[i].setEditable(!readOnly);
			waitingTimeByType[i].addKeyListener(new DialogElementListener());
			p4=new JPanel(new FlowLayout(FlowLayout.LEFT));
			p4.add(waitingTimeByType[i]);
			p3.add(p4);
		}

		JScrollPane s=new JScrollPane(p4=new JPanel());
		p4.setLayout(new BoxLayout(p4,BoxLayout.PAGE_AXIS));
		p4.add(p3);
		p4.add(Box.createVerticalGlue());
		s.setColumnHeaderView(new JLabel(Language.tr("Editor.Callcenter.MinimumWaitingTimesByClients")+" ("+Language.tr("Statistic.Units.InSeconds")+")"));
		s.setBorder(BorderFactory.createEmptyBorder(10,5,0,0));
		p.add(s,BorderLayout.CENTER);

		tabs.setIconAt(0,Images.EDITOR_AGENTS.getIcon());
		tabs.setIconAt(1,Images.GENERAL_SETUP.getIcon());
		tabs.setIconAt(2,Images.EDITOR_MINIMUM_WAITING_TIMES.getIcon());
	}

	/**
	 * Aktualisiert die Darstellung der Agentengruppen
	 * @see #agentList
	 * @see #agentListData
	 */
	private void updateAgentsList() {
		int selected=agentList.getSelectedIndex();

		agentListData.clear();
		for (int i=0;i<agents.size();i++) {
			CallcenterModelAgent a=agents.get(i);
			agentListData.addElement(a);
		}
		if (agentListData.size()>0)	agentList.setSelectedIndex(Math.min(agentListData.size()-1,selected));
	}

	/**
	 * Prüft den Inhalt einer Dialogseite
	 * @param index	Index der zu prüfenden Dialogseite
	 * @return	Liefert im Erfolgsfall <code>null</code> und im Fehlerfall ein zwei-elementiges Array aus Titel und Inhalt der Fehlermeldung
	 */
	private String[] plainCheckPage(int index) {
		String[] error=null;

		switch (index) {
		case 1:
			/* Parameter */
			if (NumberTools.getNotNegativeInteger(technicalFreeTime,true)==null) {
				if (error==null) error=new String[]{Language.tr("Editor.Callcenter.Error.TechnicalFreeTime.Title"),String.format(Language.tr("Editor.Callcenter.Error.TechnicalFreeTime.Info"),technicalFreeTime.getText())};
			}
			if (NumberTools.getNotNegativeInteger(score,true)==null) {
				if (error==null) error=new String[]{Language.tr("Editor.Callcenter.Error.ScoreCallcenter.Title"),String.format(Language.tr("Editor.Callcenter.Error.ScoreCallcenter.Info"),score.getText())};
			}
			if (NumberTools.getNotNegativeDouble(agentScoreFreeTimeSinceLastCall,true)==null) {
				if (error==null) error=new String[]{Language.tr("Editor.Callcenter.Error.AgentsFreeTime.Title"),String.format(Language.tr("Editor.Callcenter.Error.AgentsFreeTime.Info"),agentScoreFreeTimeSinceLastCall.getText())};
			}
			if (NumberTools.getNotNegativeDouble(agentScoreFreeTimePart,true)==null) {
				if (error==null) error=new String[]{Language.tr("Editor.Callcenter.Error.AgentsFreeTimePart.Title"),String.format(Language.tr("Editor.Callcenter.Error.AgentsFreeTimePart.Info"),agentScoreFreeTimePart.getText())};
			}
			break;
		case 2:
			/* Mindestwartezeiten */
			for (int i=0;i<waitingTimeByType.length;i++) {
				if (waitingTimeByType[i].getText().trim().isEmpty()) continue;
				if (NumberTools.getNotNegativeInteger(waitingTimeByType[i],true)==null) {
					if (error==null) error=new String[]{Language.tr("Editor.Callcenter.Error.MinimumWaitingTime.Title"),String.format(Language.tr("Editor.Callcenter.Error.MinimumWaitingTime.Info"),callerTypeNames[i],waitingTimeByType[i].getText())};
				}
			}
			break;
		}

		return error;
	}

	@Override
	protected boolean checkData() {
		/* Name */
		if (name.getText().trim().isEmpty()) {
			MsgBox.error(this,Language.tr("Editor.Callcenter.Error.NoName.Title"),Language.tr("Editor.Callcenter.Error.NoName.Info"));
			return false;
		}

		/* Doppelte Namen */
		String s=name.getText();
		for (int i=0;i<callcenterNames.length;i++) {
			if (i==indexForThisCallcenter) continue;
			if (callcenterNames[i].equalsIgnoreCase(s)) {
				MsgBox.error(this,String.format(Language.tr("Editor.Callcenter.Error.NameInUse.Title"),s),String.format(Language.tr("Editor.Callcenter.Error.NameInUse.Info"),s));
				return false;
			}
		}

		/* Seiten */
		String[] error=null;
		for (int i=1;i<=2;i++) {
			String[] e=plainCheckPage(i);
			if (error==null) error=e;
		}
		if (error!=null) {
			MsgBox.error(this,error[0],error[1]);
			return false;
		}

		return true;
	}

	/**
	 * Speichert den Inhalt einer Dialogseite in einem Callcenter-Objekt
	 * @param callcenter	Callcenter-Objekt in das die Daten eingetragen werden sollen
	 * @param index	Index der Dialogseite
	 */
	private void storePage(final CallcenterModelCallcenter callcenter, final int index) {
		switch (index) {
		case 1:
			/* Parameter */
			callcenter.technicalFreeTime=NumberTools.getNotNegativeInteger(technicalFreeTime,false);
			callcenter.technicalFreeTimeIsWaitingTime=technicalFreeTimeIsWaitingTime.isSelected();
			callcenter.score=NumberTools.getNotNegativeInteger(score,false);
			callcenter.agentScoreFreeTimeSinceLastCall=NumberTools.getNotNegativeDouble(agentScoreFreeTimeSinceLastCall,false);
			callcenter.agentScoreFreeTimePart=NumberTools.getNotNegativeDouble(agentScoreFreeTimePart,false);
			break;
		case 2:
			/* Mindestwartezeiten */
			callcenter.callerMinWaitingTimeName.clear();
			callcenter.callerMinWaitingTime.clear();
			for (int i=0;i<waitingTimeByType.length;i++) {
				if (waitingTimeByType[i].getText().trim().isEmpty()) continue;
				Integer w=NumberTools.getNotNegativeInteger(waitingTimeByType[i],false);
				callcenter.callerMinWaitingTimeName.add(callerTypeNames[i]);
				callcenter.callerMinWaitingTime.add(w);
			}
			break;
		}
	}

	@Override
	protected void storeData() {
		callcenter.active=active.isSelected();

		/* Name */
		callcenter.name=name.getText();

		/* Agenten */
		callcenter.agents.clear();
		for (int i=0;i<agents.size();i++) callcenter.agents.add(agents.get(i).clone());

		/* Seiten */
		for (int i=1;i<=2;i++) storePage(callcenter,i);
	}

	/**
	 * Befehl: Agentengruppe hinzufügen
	 * @see #agentList
	 * @see #agentListData
	 */
	private void agentAdd() {
		CallcenterModelAgent agent=new CallcenterModelAgent();
		if (model.skills.size()>0) agent.skillLevel=model.skills.get(0).name;
		AgentEditDialog dialog=new AgentEditDialog(this,agent,callcenter,agents,model,callerTypeNames,Language.tr("Editor.Callcenter.NewAgentsGroup"),-1,false,false,false,helpLink,true);
		dialog.addSkillLevelRenameListener(new SkillLevelRenameListener());
		dialog.setVisible(true);
		if (dialog.getClosedBy()!=BaseEditDialog.CLOSED_BY_OK) return;
		agents.add(agent);
		updateAgentsList();
		agentList.setSelectedIndex(agentListData.getSize()-1);
	}

	/**
	 * Befehl: Agentengruppe bearbeiten
	 * @see #agentList
	 * @see #agentListData
	 */
	private void agentEdit() {
		if (agentList.getSelectedIndex()<0) return;
		int index=agentList.getSelectedIndex();
		CallcenterModelAgent agent;

		int editCount=0;
		int lastTab=-1;
		Point lastLocation=null;
		int lastClose=BaseEditDialog.CLOSED_BY_PREVIOUS;
		while (lastClose==BaseEditDialog.CLOSED_BY_PREVIOUS || lastClose==BaseEditDialog.CLOSED_BY_NEXT) {
			agent=agents.get(index);
			AgentEditDialog dialog=new AgentEditDialog(this,agent,callcenter,agents,model,callerTypeNames,String.format(Language.tr("Editor.Callcenter.AgentGroupNr"),index+1),index,readOnly,index>0,index<agents.size()-1,helpLink,true);
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
		if (editCount>0) updateAgentsList();
	}

	/**
	 * Befehl: Agentengruppe löschen
	 * @see #agentList
	 * @see #agentListData
	 */
	private void agentDelete() {
		if (agentList.getSelectedIndex()<0) return;
		if (!MsgBox.confirm(
				this,
				Language.tr("Editor.Callcenter.DeleteAgentsGroup.Title"),
				Language.tr("Editor.Callcenter.DeleteAgentsGroup.Info"),
				Language.tr("Editor.Callcenter.DeleteAgentsGroup.Yes.Info"),
				Language.tr("Editor.Callcenter.DeleteAgentsGroup.No.Info")
				)) return;
		agents.remove(agentList.getSelectedIndex());
		updateAgentsList();
	}

	/**
	 * Befehl: Aktivitätsstatus einer Agentengruppe umschalten
	 * @see #agentList
	 * @see #agentListData
	 */
	private void agentToggleActive() {
		agents.get(agentList.getSelectedIndex()).active=!agents.get(agentList.getSelectedIndex()).active;
		updateAgentsList();
	}

	/**
	 * Befehl: Agentengruppe kopieren
	 * @see #agentList
	 * @see #agentListData
	 */
	private void agentCopy() {
		if (agentList.getSelectedIndex()<0) return;

		CallcenterModelAgent agent;
		agent=agents.get(agentList.getSelectedIndex());

		double count=agent.count;
		String s1,s2;
		if (count==-1) {
			count=0;
			double[] dist=null;
			if (agent.countPerInterval24!=null) {
				dist=agent.countPerInterval24.densityData;
				for (int i=0;i<dist.length;i++) count+=dist[i]*2;
			}
			if (agent.countPerInterval48!=null) {
				dist=agent.countPerInterval48.densityData;
				for (int i=0;i<dist.length;i++) count+=dist[i];
			}
			if (agent.countPerInterval96!=null) {
				dist=agent.countPerInterval96.densityData;
				for (int i=0;i<dist.length;i++) count+=dist[i]/2;
			}
			s1=Language.tr("Editor.Callcenter.Count.HalfHourIntervalSingleAbout");
			s2=Language.tr("Editor.Callcenter.Count.HalfHourIntervalMultipleAbout");
		} else {
			if (count==-2) {
				count+=agent.byCallersAvailableHalfhours;
				s1=Language.tr("Editor.Callcenter.Count.HalfHourIntervalSingleAbout");
				s2=Language.tr("Editor.Callcenter.Count.HalfHourIntervalMultipleAbout");
			} else {
				s1=Language.tr("Editor.Callcenter.Count.AgentSingle");
				s2=Language.tr("Editor.Callcenter.Count.AgentMultiple");
			}
		}

		CopyDialog copy=new CopyDialog(this,Language.tr("Editor.Callcenter.CopyAgentsGroup"),s1,s2,(int)Math.round(count),helpCallback);
		copy.setVisible(true);
		if (copy.getClosedBy()!=BaseEditDialog.CLOSED_BY_OK) return;

		agent=agents.get(agentList.getSelectedIndex()).clone();
		double d=copy.getProbability();
		if (agent.count>=0) agent.count=(int)Math.round(agent.count*d);
		if (agent.count==-1) {
			double[] dist=null;
			if (agent.countPerInterval24!=null) dist=agent.countPerInterval24.densityData;
			if (agent.countPerInterval48!=null) dist=agent.countPerInterval48.densityData;
			if (agent.countPerInterval96!=null) dist=agent.countPerInterval96.densityData;
			if (dist!=null) for (int i=0;i<dist.length;i++) dist[i]=Math.round(dist[i]*d);
		}
		if (agent.count==-2) {
			agent.byCallersAvailableHalfhours=(int)Math.round(agent.byCallersAvailableHalfhours*d);
		}

		agents.add(agent);
		updateAgentsList();
		agentList.setSelectedIndex(agentListData.getSize()-1);
	}

	/**
	 * Befehl: Agentengruppe in der Liste nach oben verschieben
	 * @see #agentList
	 * @see #agentListData
	 */
	private void agentMoveUp() {
		int selected=agentList.getSelectedIndex();
		if (selected<1) return;
		CallcenterModelAgent a1=agents.get(selected);
		CallcenterModelAgent a2=agents.get(selected-1);
		agents.set(selected,a2);
		agents.set(selected-1,a1);
		agentList.setSelectedIndex(selected-1);
		updateAgentsList();
	}

	/**
	 * Befehl: Agentengruppe in der Liste nach unten verschieben
	 * @see #agentList
	 * @see #agentListData
	 */
	private void agentMoveDown() {
		int selected=agentList.getSelectedIndex();
		if (selected<0 || selected==agentList.getModel().getSize()-1) return;
		CallcenterModelAgent a1=agents.get(selected);
		CallcenterModelAgent a2=agents.get(selected+1);
		agents.set(selected,a2);
		agents.set(selected+1,a1);
		agentList.setSelectedIndex(selected+1);
		updateAgentsList();
	}

	/**
	 * Lädt Agentengruppen mit festen Arbeitszeiten einer Tabelle.
	 * @param table	Tabelle aus der die Daten geladen werden sollen
	 * @return	Array mit jeweils einem Eintrag pro Gruppe (jeweils ein 3-elementiges Array aus Anzahl, Startzeit, Endzeit)
	 */
	private int[][] loadFixedTimedAgentsFromTableSheet(final Table table) {
		if (table.getSize(0)==0 || table.getSize(1)<2) return null;

		List<Integer> count=new ArrayList<>();
		List<Integer> timeStart=new ArrayList<>();
		List<Integer> timeEnd=new ArrayList<>();

		String[][] data=table.getDataArray();

		for (String[] row: data) {
			int c=-1,t1=-1,t2=-1;
			for (String cell: row) {
				if (cell.trim().isEmpty()) continue;
				Integer I=NumberTools.getNotNegativeInteger(cell);
				String[] parts=cell.split(" ");
				Integer T=TimeTools.getTime(parts[parts.length-1]);
				if (I!=null && c<0) {c=I; continue;}
				if (T!=null && t2<0) {if (t1<0) t1=T; else t2=T; continue;}
				t2=-1; break;
			}
			if (t2<0) continue;
			if (c<0) c=1;

			boolean ok=false;
			for (int i=0;i<timeStart.size();i++) if (timeStart.get(i)==t1 && timeEnd.get(i)==t2) {
				count.set(i,count.get(i)+c);
				ok=true;
				break;
			}
			if (!ok) {timeStart.add(t1); timeEnd.add(t2); count.add(c);}
		}

		if (count.size()==0) return null;

		int[][] results=new int[count.size()][];
		for (int i=0;i<count.size();i++) {
			int[] row=new int[3];
			row[0]=count.get(i);
			row[1]=timeStart.get(i);
			row[2]=timeEnd.get(i);
			results[i]=row;
		}
		return results;
	}

	/**
	 * Lädt Agentengruppen mit festen Arbeitszeiten aus auszuwählenden einer Tabelle.
	 * @return	Liefert im Erfolgsfall <code>true</code>
	 */
	private boolean loadFixedTimedAgentsFromTable() {
		File tableFile=MultiTable.showLoadDialog(this,Language.tr("Editor.Callcenter.Tools.LoadFixedGroupsFromFile.LoadTableTitle"));
		if (tableFile==null) return false;
		MultiTable multiTable=new MultiTable();
		if (!multiTable.load(tableFile)) return false;

		int[][] data=null;
		for (int i=0;i<multiTable.size();i++) {
			data=loadFixedTimedAgentsFromTableSheet(multiTable.get(i));
			if (data!=null) break;
		}
		if (data==null) {
			MsgBox.error(this,Language.tr("Editor.Callcenter.Tools.LoadFixedGroupsFromFile"),Language.tr("Editor.Callcenter.Tools.LoadFixedGroupsFromFile.NoDataFound"));
			return false;
		}

		CallcenterModelAgent agentTemplate=new CallcenterModelAgent();
		if (model.skills.size()>0) agentTemplate.skillLevel=model.skills.get(0).name;
		AgentEditDialog dialog=new AgentEditDialog(owner,agentTemplate,callcenter,agents,model,callerTypeNames,Language.tr("Editor.Callcenter.NewAgentsGroup"),-1,false,false,false,helpLink,false);
		dialog.addSkillLevelRenameListener(new SkillLevelRenameListener());
		dialog.setVisible(true);
		if (dialog.getClosedBy()==BaseEditDialog.CLOSED_BY_OK) {
			for (int i=0;i<data.length;i++) {
				CallcenterModelAgent agent=agentTemplate.clone();
				agent.count=data[i][0];
				agent.workingTimeStart=data[i][1];
				agent.workingTimeEnd=data[i][2];
				agents.add(agent);
			}
			return true;
		}

		return false;
	}

	/**
	 * Befehl: Agentenanzahl in allen Intervallen in allen Gruppen ändern
	 * @see #popupItem1
	 */
	private final void changeAgentCount() {
		String[] countNames=new String[agents.size()];
		int[] countValues=new int[agents.size()];
		for (int i=0;i<agents.size();i++) {
			CallcenterModelAgent agent=agents.get(i);
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
			countNames[i]=String.format(Language.tr("Editor.Callcenter.AgentGroupNr"),i+1);
			countValues[i]=count;
		}

		String s1=Language.tr("Editor.Callcenter.Count.HalfHourIntervalSingleAbout");
		String s2=Language.tr("Editor.Callcenter.Count.HalfHourIntervalMultipleAbout");
		CopyDialog copy=new CopyDialog(this,Language.tr("Editor.AgentsGroup.Change"),s1,s2,countNames,countValues,helpCallback);
		copy.setVisible(true);
		if (copy.getClosedBy()!=BaseEditDialog.CLOSED_BY_OK) return;

		double d=copy.getProbability();
		for (int i=0;i<agents.size();i++) {
			CallcenterModelAgent agent=agents.get(i);

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
		}
		updateAgentsList();
	}

	/**
	 * Können die Daten einer bestimmten Seite an auf Callcenter übertragen werden?
	 * @param page	Zu prüfende Seite
	 * @return	Liefert im Erfolgsfall <code>true</code> (im Fehlerfall wird eine Fehlermeldung ausgegeben)
	 */
	private boolean checkPagesForApply(final int page) {
		String[] error=null;
		for (int i=1;i<=2;i++) {
			String[] e=plainCheckPage(i);
			if (error==null) error=e;
		}

		if (error!=null) {
			final String largeStart="<b><span style=\"font-size: 115%\">";
			final String largeEnd="</span></b>";
			StringBuilder sb=new StringBuilder();
			sb.append("<html><body>\n");
			sb.append(Language.tr("Editor.Callcenter.Apply.Error.Info")+"<br>\n");
			sb.append(largeStart+Language.tr("Editor.Callcenter.Apply.Error.AdditionalInformation")+":"+largeEnd+"<br>\n");
			sb.append("<div style=\"border: 1px solid red; background-color: #FFBBBB; padding: 5px;\">");
			sb.append(largeStart+error[0]+largeEnd+"\n");
			sb.append(error[1]+"</div>\n");
			sb.append("</body></html>");
			MsgBox.error(owner,Language.tr("Editor.Callcenter.Apply.Error.Title"),sb.toString());
			return false;
		}
		return true;
	}

	/**
	 * Überträgt die Einstellungen aus einer Dialogseite an ein anderes Callcenter
	 * @param index	Index der Dialogseite deren Einstellungen übertragen werden sollen
	 * @param callcenter	Index des Callcenters in das die Einstellungen eingetragen werden sollen
	 */
	private void applyPage(final int index, final int callcenter) {
		for (int i=0;i<model.callcenter.size();i++) if (callcenter==i || (callcenter==-1 && i!=indexForThisCallcenter)) {
			storePage(model.callcenter.get(i),index);
		}
	}

	/**
	 * Reagiert auf Klicks auf die verschiedenen Schaltflächen in diesem Dialog
	 */
	private class ButtonActionListener implements ActionListener {
		@Override
		public void actionPerformed(ActionEvent e) {
			if (e.getSource()==agentAddButton) {agentAdd(); return;}
			if (e.getSource()==agentEditButton) {agentEdit(); return;}
			if (e.getSource()==agentDeleteButton) {agentDelete(); return;}
			if (e.getSource()==agentCopyButton) {agentCopy(); return;}
			if (e.getSource()==agentUpButton) {agentMoveUp(); return;}
			if (e.getSource()==agentDownButton) {agentMoveDown(); return;}
			if (e.getSource()==agentToolsButton) {popupMenu.show(agentToolsButton,0,agentToolsButton.getBounds().height); return;}
			if (e.getSource()==popupItem1) {changeAgentCount(); return;}
			if (e.getSource()==popupItem2a) {
				openAgentsGenerator24=true;
				if (!closeDialog(CLOSED_BY_OK)) openAgentsGenerator24=false; return;
			}
			if (e.getSource()==popupItem2b) {
				openAgentsGenerator48=true;
				if (!closeDialog(CLOSED_BY_OK)) openAgentsGenerator48=false; return;
			}
			if (e.getSource()==popupItem2c) {
				openAgentsGenerator96=true;
				if (!closeDialog(CLOSED_BY_OK)) openAgentsGenerator96=false; return;
			}
			if (e.getSource()==popupItem3) {
				openAgentsEfficiencyGenerator=true;
				if (!closeDialog(CLOSED_BY_OK)) openAgentsEfficiencyGenerator=false; return;
			}
			if (e.getSource()==popupItem4) {
				openAgentsAdditionGenerator=true;
				if (!closeDialog(CLOSED_BY_OK)) openAgentsAdditionGenerator=false; return;
			}
			if (e.getSource()==efficiency || e.getSource()==popupItem5) {
				EfficiencyEditDialog dialog=new EfficiencyEditDialog(CallcenterEditDialog.this,readOnly,false,callcenter.efficiencyPerInterval,helpCallback,EfficiencyEditDialog.Mode.MODE_EFFICIENCY);
				dialog.setVisible(true);
				if (dialog.getClosedBy()!=BaseEditDialog.CLOSED_BY_OK) return;
				callcenter.efficiencyPerInterval=dialog.getEfficiency();
				return;
			}
			if (e.getSource()==addition || e.getSource()==popupItem6) {
				EfficiencyEditDialog dialog=new EfficiencyEditDialog(CallcenterEditDialog.this,readOnly,false,callcenter.additionPerInterval,helpCallback,EfficiencyEditDialog.Mode.MODE_ADDITION);
				dialog.setVisible(true);
				if (dialog.getClosedBy()!=BaseEditDialog.CLOSED_BY_OK) return;
				callcenter.additionPerInterval=dialog.getEfficiency();
				return;
			}
			if (e.getSource()==popupItem7) {
				if (loadFixedTimedAgentsFromTable()) {
					updateAgentsList();
					agentList.setSelectedIndex(agentListData.getSize()-1);
				}
				return;
			}
		}
	}

	/**
	 * Reagiert auf Ereignisse für die Listendarstellung {@link CallcenterEditDialog#agentList}
	 * @see CallcenterEditDialog#agentList
	 */
	private class ListListener implements KeyListener,MouseListener,ListSelectionListener {
		@Override
		public void keyTyped(KeyEvent e) {}

		@Override
		public void keyPressed(KeyEvent e) {
			if (e.getKeyCode()==KeyEvent.VK_ENTER && e.getModifiersEx()==0) {agentEdit(); e.consume();}
			if (e.getKeyCode()==KeyEvent.VK_INSERT && e.getModifiersEx()==0) {agentAdd(); e.consume();}
			if (e.getKeyCode()==KeyEvent.VK_DELETE && e.getModifiersEx()==0) {agentDelete(); e.consume();}
			if (e.getKeyCode()==KeyEvent.VK_ENTER && (e.getModifiersEx() & InputEvent.CTRL_DOWN_MASK)!=0) {agentToggleActive(); e.consume();}
			if (e.getKeyCode()==KeyEvent.VK_UP && (e.getModifiersEx() & InputEvent.CTRL_DOWN_MASK)!=0) {agentMoveUp(); e.consume();}
			if (e.getKeyCode()==KeyEvent.VK_DOWN && (e.getModifiersEx() & InputEvent.CTRL_DOWN_MASK)!=0) {agentMoveDown(); e.consume();}
		}

		/**
		 * Löst ggf. ein Popupmenü zu einem Eintrag in der Agentengruppen-Liste aus.
		 * @see CallcenterEditDialog#agents
		 * @param e	Maus-Ereignis
		 */
		private void mousePopup(MouseEvent e) {
			if (!e.isPopupTrigger()) return;

			int index=agentList.locationToIndex(e.getPoint());
			if (!agentList.getCellBounds(index,index).contains(e.getPoint())) index=-1;

			String name="";
			boolean active=false;
			ActionListener activateListener=null;

			if (index>=0) {
				final CallcenterModelAgent agent=agents.get(index);
				name=String.format(Language.tr("Editor.Callcenter.AgentGroupNr"),index+1)+" "+Language.tr("Dialog.active.lower");
				active=agent.active;
				activateListener=e1-> {
					agent.active=!agent.active;
					updateAgentsList();
				};
			}

			JMenuItem item;
			final JPopupMenu popup=new JPopupMenu();

			if (activateListener!=null) {
				popup.add(item=new JCheckBoxMenuItem(name,active));
				item.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_ENTER,InputEvent.CTRL_DOWN_MASK));
				item.addActionListener(activateListener);
				popup.addSeparator();
			}

			popup.add(item=new JMenuItem(agentAddButton.getText(),agentAddButton.getIcon()));
			item.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_INSERT,0));
			item.addActionListener(new PopupActionListener(0,-1));

			if (index>=0) {
				popup.add(item=new JMenuItem(agentEditButton.getText(),agentEditButton.getIcon()));
				item.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_ENTER,0));
				item.addActionListener(new PopupActionListener(1,index));
			}

			if (!readOnly && index>=0) {
				popup.add(item=new JMenuItem(agentDeleteButton.getText(),agentDeleteButton.getIcon()));
				item.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_DELETE,0));
				item.addActionListener(new PopupActionListener(2,index));
				popup.add(item=new JMenuItem(agentCopyButton.getText(),agentCopyButton.getIcon()));
				item.addActionListener(new PopupActionListener(3,index));
				if (index>0 || index<agents.size()-1) {
					popup.addSeparator();
				}
				if (index>0) {
					popup.add(item=new JMenuItem(agentUpButton.getText(),agentUpButton.getIcon()));
					item.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_UP,InputEvent.CTRL_DOWN_MASK));
					item.addActionListener(new PopupActionListener(4,index));
				}
				if (index<agents.size()-1) {
					popup.add(item=new JMenuItem(agentDownButton.getText(),agentDownButton.getIcon()));
					item.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_DOWN,InputEvent.CTRL_DOWN_MASK));
					item.addActionListener(new PopupActionListener(5,index));
				}
			}

			popup.show(e.getComponent(),e.getX(), e.getY());
		}

		/**
		 * Reagiert auf einen Eintrag in dem Agentengruppen-Listen-Popupmenü
		 * @see CallcenterEditDialog#agents
		 * @see ListListener#mousePopup(MouseEvent)
		 */
		private class PopupActionListener implements ActionListener {
			/** Nummer des angeklickten Menüpunktes */
			private final int buttonNr;
			/** Gewählte Zeile in der Liste */
			private final int index;

			/**
			 * Konstruktor der Klasse
			 * @param buttonNr	Nummer des angeklickten Menüpunktes
			 * @param index	Gewählte Zeile in der Liste
			 */
			public PopupActionListener(final int buttonNr, final int index) {
				this.buttonNr=buttonNr;
				this.index=index;
			}

			@Override
			public void actionPerformed(ActionEvent e) {
				if (index>=0) agentList.setSelectedIndex(index);
				switch (buttonNr) {
				case 0: agentAdd(); break;
				case 1: agentEdit(); break;
				case 2: agentDelete(); break;
				case 3: agentCopy(); break;
				case 4: agentMoveUp(); break;
				case 5: agentMoveDown(); break;
				}
			}
		}

		@Override
		public void keyReleased(KeyEvent e) {}
		@Override
		public void mouseClicked(MouseEvent e) {if (e.getClickCount()==2) agentEdit();}
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
			agentAddButton.setEnabled(!readOnly);
			agentEditButton.setEnabled(agentList.getSelectedIndex()!=-1);
			agentDeleteButton.setEnabled(!readOnly && agentList.getSelectedIndex()!=-1);
			agentCopyButton.setEnabled(!readOnly && agentList.getSelectedIndex()!=-1);
			agentUpButton.setEnabled(!readOnly && agentList.getSelectedIndex()>0);
			agentDownButton.setEnabled(!readOnly && agentList.getSelectedIndex()!=-1 && agentList.getSelectedIndex()<agentList.getModel().getSize()-1);
		}
	}

	/**
	 * Reagiert darauf, wenn in {@link SkillLevelEditDialog}
	 * ein Skill-Level umbenannt wurde.
	 * @see SkillLevelEditDialog
	 * @see AgentEditDialog#editSkillLevel()
	 */
	private class SkillLevelRenameListener implements RenameListener {
		@Override
		public void renamed(RenameEvent e) {
			if (e.renamed) for (int i=0;i<agents.size();i++) if (agents.get(i).skillLevel.equalsIgnoreCase(e.oldName)) agents.get(i).skillLevel=e.newName;
			updateAgentsList();
			for (int i=0;i<listener.size();i++) listener.get(i).renamed(e);
		}
	}

	/**
	 * Renderer für {@link CallcenterEditDialog#agentList}
	 * @see CallcenterEditDialog#agentList
	 */
	private static class AgentListRenderer extends AdvancedListCellRenderer {
		/**
		 * Serialisierungs-ID der Klasse
		 * @see Serializable
		 */
		private static final long serialVersionUID = 2294389689021269527L;

		@Override
		protected void buildString(Object value, int index, StringBuilder s) {
			CallcenterModelAgent a=(CallcenterModelAgent)value;
			addName(s,String.format(Language.tr("Editor.Callcenter.AgentGroupNr"),index+1),a.active);
			addBr(s);

			if (a.count==-1) {
				addColor(s,Language.tr("Editor.Callcenter.AgentsMode.Distribution"),"green");
				addBr(s);
				double count=0;
				if (a.countPerInterval24!=null) {
					double[] dist=a.countPerInterval24.densityData;
					for (int i=0;i<dist.length;i++) count+=dist[i]*2;
				}
				if (a.countPerInterval48!=null) {
					double[] dist=a.countPerInterval48.densityData;
					for (int i=0;i<dist.length;i++) count+=dist[i];
				}
				if (a.countPerInterval96!=null) {
					double[] dist=a.countPerInterval96.densityData;
					for (int i=0;i<dist.length;i++) count+=dist[i]/2;
				}
				if (count==1) s.append(Language.tr("Editor.Callcenter.Count.HalfHourIntervalSingle")); else s.append(String.format(Language.tr("Editor.Callcenter.Count.HalfHourIntervalMultiple"),Math.round(count)));
			} else {
				if (a.count==-2) {
					addColor(s,Language.tr("Editor.Callcenter.AgentsMode.ClientArrivals"),"green");
					addBr(s);
					if (a.byCallersAvailableHalfhours==1) s.append(Language.tr("Editor.Callcenter.Count.HalfHourIntervalSingle")); else s.append(String.format(Language.tr("Editor.Callcenter.Count.HalfHourIntervalMultiple"),a.byCallersAvailableHalfhours));
					addBr(s);
					s.append(Language.tr("Editor.Callcenter.AgentsMode.ClientArrivals")+": ");
					for (int i=0;i<a.byCallers.size();i++) {
						if (i>0) s.append(", ");
						s.append(a.byCallers.get(i));
					}
				} else {
					if (a.count!=1) addColor(s,String.format(Language.tr("Editor.Callcenter.Count.AgentMultiple"),a.count),"green"); else addColor(s,Language.tr("Editor.Callcenter.Count.AgentSingle"),"green");
					addBr(s);
					s.append(Language.tr("Editor.Callcenter.WorkingTime")+": "+TimeTools.formatTime(a.workingTimeStart)+"-");
					if (a.workingNoEndTime) s.append(Language.tr("Distribution.Infinite")); else s.append(TimeTools.formatTime(a.workingTimeEnd));
				}
			}
			addBr(s);

			s.append(Language.tr("Editor.Callcenter.SkillLevel")+": "+a.skillLevel);
		}

		@Override
		protected Icon getIcon(Object value) {
			CallcenterModelAgent a=(CallcenterModelAgent)value;
			return a.active?Images.EDITOR_AGENTS_BIG.getIcon():Images.EDITOR_AGENTS_BIG_DISABLED.getIcon();
		}
	}

	/**
	 * Reagiert auf Tastendrücke in den verschiedenen
	 * Eingabefeldern des Dialogs
	 */
	private class DialogElementListener implements KeyListener {
		/**
		 * Reagiert auf ein Tastenereignis
		 * @param e	Tastenereignis
		 * @see #keyTyped(KeyEvent)
		 * @see #keyPressed(KeyEvent)
		 * @see #keyReleased(KeyEvent)
		 */
		private void keyEvent(KeyEvent e) {
			NumberTools.getNotNegativeInteger(technicalFreeTime,true);
			NumberTools.getNotNegativeInteger(score,true);
			NumberTools.getNotNegativeDouble(agentScoreFreeTimeSinceLastCall,true);
			NumberTools.getNotNegativeDouble(agentScoreFreeTimePart,true);
			for (int i=0;i<waitingTimeByType.length;i++) if (waitingTimeByType[i].getText().trim().isEmpty()) waitingTimeByType[i].setBackground(NumberTools.getTextFieldDefaultBackground()); else NumberTools.getNotNegativeInteger(waitingTimeByType[i],true);
		}

		@Override
		public void keyTyped(KeyEvent e) {
			keyEvent(e);
		}

		@Override
		public void keyPressed(KeyEvent e) {
			keyEvent(e);
		}

		@Override
		public void keyReleased(KeyEvent e) {
			keyEvent(e);
		}
	}

	/**
	 * Reagiert auf Klicks im {@link CallcenterEditDialog#popupMenu2}
	 * auf die Menüpunkte.
	 * @see CallcenterEditDialog#popupMenu2
	 * @see CallcenterEditDialog#applyThisPage
	 * @see CallcenterEditDialog#applyAllPages
	 */
	private class PopupActionListener implements ActionListener {
		@Override
		public void actionPerformed(ActionEvent e) {
			for (int i=0;i<applyThisPage.length;i++) if (applyThisPage[i]==e.getSource()) {
				if (!checkPagesForApply(tabs.getSelectedIndex())) return;
				applyPage(tabs.getSelectedIndex(),(i==applyThisPage.length-1)?-1:i);
				return;
			}
			for (int i=0;i<applyAllPages.length;i++) if (applyAllPages[i]==e.getSource()) {
				for (int j=1;j<=2;j++) if (!checkPagesForApply(j)) return;
				for (int j=1;j<=2;j++) applyPage(j,(i==applyThisPage.length-1)?-1:i);
				return;
			}
		}
	}
}
