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
import java.awt.Color;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.GridLayout;
import java.awt.Window;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

import javax.swing.BoxLayout;
import javax.swing.DefaultListModel;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JMenu;
import javax.swing.JMenuItem;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JPopupMenu;
import javax.swing.JScrollPane;
import javax.swing.JTabbedPane;
import javax.swing.JTextField;
import javax.swing.ListCellRenderer;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;

import org.apache.commons.math3.distribution.AbstractRealDistribution;
import org.apache.commons.math3.distribution.ExponentialDistribution;

import language.Language;
import mathtools.NumberTools;
import mathtools.TimeTools;
import mathtools.distribution.swing.JDistributionPanel;
import mathtools.distribution.tools.DistributionTools;
import parser.CalcSystem;
import parser.MathParser;
import systemtools.MsgBox;
import tools.SetupData;
import ui.HelpLink;
import ui.editor.events.RenameEvent;
import ui.editor.events.RenameListener;
import ui.images.Images;
import ui.model.CallcenterModel;
import ui.model.CallcenterModelSkillLevel;

/**
 * Diese Klasse kapselt einen kompletten Bearbeitungsdialog für Skill-Levels.
 * @author Alexander Herzog
 * @version 1.0
 * @see CallcenterModel
 */
public class SkillLevelEditDialog extends BaseEditDialog {
	/**
	 * Serialisierungs-ID der Klasse
	 * @see Serializable
	 */
	private static final long serialVersionUID = -177706251057837446L;

	/** Im Konstruktor übergebenes Skill-Level-Objekt in das beim Schließen mit "Ok" die Daten zurückgeschrieben werden */
	private final CallcenterModelSkillLevel skill;
	/** Temporäre Arbeitskopie des Skill-Levels zur Anzeige im Dialog */
	private final CallcenterModelSkillLevel  tempSkill;

	/** Index dieses Skill-Levels in der Liste aller Skill-Level */
	private int indexForThisSkillLevel;
	/** Listen mit allen Skill-Level-Namen (um doppelte Namen zu verhindern) */
	private final String[] skillLevelNames;

	/** Listener die über Skill-Level-Namensänderungen benachrichtigt werden sollen */
	private final List<RenameListener> listener;

	/** Schaltfläche "Skill hinzufügen" */
	private JButton addButton;
	/** Schaltfläche "Skill löschen" */
	private JButton delButton;
	/** Schaltfläche "Skill kopieren" */
	private JButton copyButton;
	/** Schaltfläche "Tools" */
	private JButton toolsButton;
	/** Durch das {@link #toolsButton} aufrufbares Popupmenü */
	private JPopupMenu toolsPopup;
	/** Menüpunkte "Einstellungen zu anderem Skill kopieren" ({@link #toolsPopup}) */
	private JMenuItem[] toolsPopupCopyTo;
	/** Menüpunkte "Verteilungen global laden" ({@link #toolsPopup}) */
	private JMenuItem generatorButton;
	/** Liste der Skills in diesem Skill-Level */
	private JList<String> list;
	/** Datenmodell der Liste der Skills in diesem Skill-Level {@link #list} */
	private DefaultListModel<String> listData;
	/** Zuletzt in {@link #list} ausgewählter Eintrag */
	private int lastSelected=-1;
	/** Zuletzt in {@link #workingTimeComboBox} ausgewähltes Intervall */
	private int lastInterval1=-1;
	/** Zuletzt in {@link #postprocessingTimeComboBox} ausgewähltes Intervall */
	private int  lastInterval2=-1;
	/** Zuletzt in {@link #workingTimeAddOnComboBox} ausgewähltes Intervall */
	private int  lastInterval3=-1;
	/** Darstellung der Registerreiter für Bedien-, Nachbearbeitungszeit usw. */
	private JTabbedPane tabs;
	/** Verteilung der Bedienzeiten im aktuellen Skill */
	private JDistributionPanel workingTime;
	/** Verteilung der Nachbearbeitungszeiten im aktuellen Skill */
	private JDistributionPanel postprocessingTime;
	/** Auswahlbox für das Zeitintervall für {@link #workingTime} */
	private JComboBox<String> workingTimeComboBox;
	/** Auswahlbox für das Zeitintervall für {@link #workingTimeAddOn} */
	private JComboBox<String> workingTimeAddOnComboBox;
	/** Auswahlbox für das Zeitintervall für {@link #postprocessingTime} */
	private JComboBox<String> postprocessingTimeComboBox;
	/** Option: Intervall-abhängige Bedienzeit für dieses Intervall aktiv? */
	private JCheckBox workingTimeCheckBox;
	/** Option: Intervall-abhängige wartezeitabhängige Bedienzeitverlängerung für dieses Intervall aktiv? */
	private JCheckBox workingTimeAddOnCheckBox;
	/** Option: Intervall-abhängige Nachbearbeitungszeit für dieses Intervall aktiv? */
	private JCheckBox postprocessingTimeCheckBox;
	/** Eingabefeld für die wartezeitabhängige Bedienzeitverlängerung */
	private JTextField workingTimeAddOn;
	/** Hilfe-Schaltfläche zur Erklärung der wartezeitabhängigen Bedienzeitverlängerung */
	private JButton workingTimeAddOnButton;
	/** Eingabefeld für den Score für den Kundentyp */
	private JTextField score;

	/**
	 * Dialog zum Laden von Bedienzeiten öffnen?
	 * @see #isOpenGenerator()
	 */
	private boolean openGenerator=false;

	/** Verknüpfung mit der Online-Hilfe */
	private final HelpLink helpLink;

	/**
	 * Konstruktor der Klasse <code>SkillLevelEditDialog</code>
	 * @param owner	Übergeordnetes Fenster
	 * @param skill	Objekt vom Typ <code>CallcenterModelSkillLevel</code> welches die Skill-Level-Daten enthält (beim Klicken auf "Ok" wird auch dieses Objekt verändert)
	 * @param callerTypeNames	Liste mit allen Kundentypen-Namen (für die Agenten-Skills)
	 * @param skillLevelNames Listen mit allen Skill-Level-Namen (um doppelte Namen zu verhindern)
	 * @param readOnly	Wird dieser Parameter auf <code>true</code> gesetzt, so wird die "Ok"-Schaltfläche deaktiviert
	 * @param previous Button "Vorheriger Skill-Level" anzeigen
	 * @param next Button "Nächster Skill-Level" anzeigen
	 * @param helpLink Verknüpfung mit der Online-Hilfe
	 */
	public SkillLevelEditDialog(Window owner, CallcenterModelSkillLevel skill, String[] callerTypeNames, String[] skillLevelNames, boolean readOnly, boolean previous, boolean next, HelpLink helpLink) {
		super(owner,Language.tr("Editor.SkillLevel.Title"),callerTypeNames,readOnly,helpLink.dialogSkillLevel);
		this.skill=skill;
		tempSkill=skill.clone();
		this.skillLevelNames=skillLevelNames;
		this.helpLink=helpLink;

		indexForThisSkillLevel=-1;
		for (int i=0;i<this.skillLevelNames.length;i++) if (this.skill.name.equalsIgnoreCase(this.skillLevelNames[i])) {
			indexForThisSkillLevel=i; break;
		}

		listener=new ArrayList<>();

		/* Kundentypen aus der Skill-Liste entfernen, die es nicht in callerTypeNames gibt */
		int i=0;
		while (i<tempSkill.callerTypeName.size()) {
			String s=tempSkill.callerTypeName.get(i);
			boolean ok=false;
			for (int j=0;j<callerTypeNames.length;j++) if (s.equalsIgnoreCase(callerTypeNames[j])) {ok=true; break;}
			if (ok) {i++; continue;}
			tempSkill.callerTypeName.remove(i);
			tempSkill.callerTypeWorkingTimeAddOn.remove(i);
			tempSkill.callerTypeWorkingTime.remove(i);
			tempSkill.callerTypePostProcessingTime.remove(i);
			tempSkill.callerTypeIntervalWorkingTimeAddOn.remove(i);
			tempSkill.callerTypeIntervalWorkingTime.remove(i);
			tempSkill.callerTypeIntervalPostProcessingTime.remove(i);
			tempSkill.callerTypeScore.remove(i);
		}

		String previousText=null;
		if (previous) previousText=readOnly?Language.tr("Editor.SkillLevel.Move.ViewPrevious"):Language.tr("Editor.SkillLevel.Move.EditPrevious");
		String nextText=null;
		if (next) nextText=readOnly?Language.tr("Editor.SkillLevel.Move.ViewNext"):Language.tr("Editor.SkillLevel.Move.EditNext");
		createSimpleGUI(Language.tr("Editor.SkillLevel.Name"),skill.name,null,false,825,500,previousText,nextText);
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
	 * von Skill-Level-Bedienzeiten werden soll?
	 * @return	Dialog zum Laden von Bedienzeiten öffnen?
	 */
	public boolean isOpenGenerator() {
		return openGenerator;
	}

	@Override
	protected void createSimpleContent(JPanel content) {
		content.setLayout(new BorderLayout());
		JPanel p,p2,p3,p4,p5;

		content.add(p=new JPanel(new FlowLayout(FlowLayout.LEFT)),BorderLayout.NORTH);

		p.add(addButton=new JButton(Language.tr("Editor.SkillLevel.Add")));
		addButton.addActionListener(new ButtonListener());
		addButton.setEnabled(!readOnly);
		addButton.setIcon(Images.EDIT_ADD.getIcon());

		p.add(delButton=new JButton(Language.tr("Editor.SkillLevel.Delete")));
		delButton.addActionListener(new ButtonListener());
		delButton.setEnabled(!readOnly);
		delButton.setIcon(Images.EDIT_DELETE.getIcon());

		p.add(copyButton=new JButton(Language.tr("Editor.SkillLevel.Copy")));
		copyButton.addActionListener(new ButtonListener());
		copyButton.setEnabled(!readOnly);
		copyButton.setIcon(Images.EDIT_COPY.getIcon());

		p.add(toolsButton=new JButton(Language.tr("Dialog.Button.Tools")));
		toolsButton.addActionListener(new ButtonListener());
		toolsButton.setEnabled(!readOnly);
		toolsButton.setIcon(Images.GENERAL_SETUP.getIcon());

		content.add(p=new JPanel(new BorderLayout()),BorderLayout.CENTER);
		p.add(new JScrollPane(list=new JList<>(listData=new DefaultListModel<>())),BorderLayout.WEST);
		list.setPreferredSize(new Dimension(150,200));
		list.addListSelectionListener(new ListListener());
		list.addKeyListener(new ListListener());
		for (int i=0;i<tempSkill.callerTypeName.size();i++) listData.addElement(tempSkill.callerTypeName.get(i));
		list.setCellRenderer(new ClientTypeListRenderer());

		List<String> intervalNames=new ArrayList<>();
		intervalNames.add(Language.tr("Editor.SkillLevel.Distribution.Global"));
		for (int i=0;i<48;i++) intervalNames.add(Language.tr("Editor.SkillLevel.Distribution.Specific")+" "+TimeTools.formatTime(i*1800)+"-"+TimeTools.formatTime((i+1)*1800-1));

		List<String> intervalNames2=new ArrayList<>();
		intervalNames2.add(Language.tr("Editor.SkillLevel.Value.Global"));
		for (int i=0;i<48;i++) intervalNames2.add(Language.tr("Editor.SkillLevel.Value.Specific")+" "+TimeTools.formatTime(i*1800)+"-"+TimeTools.formatTime((i+1)*1800-1));

		tabs=new JTabbedPane();
		p.add(tabs,BorderLayout.CENTER);

		tabs.addTab(Language.tr("Editor.SkillLevel.Distribution.HoldingTime"),p2=new JPanel(new BorderLayout()));
		p2.add(p3=new JPanel(new FlowLayout(FlowLayout.LEFT)),BorderLayout.NORTH);
		p3.add(workingTimeComboBox=new JComboBox<>(intervalNames.toArray(String[]::new)));
		workingTimeComboBox.addActionListener(e->listSelectionChanged());
		p3.add(workingTimeCheckBox=new JCheckBox(Language.tr("Editor.SkillLevel.Distribution.UseGlobal")));
		workingTime=new JDistributionPanel(new ExponentialDistribution(600),CallcenterModelSkillLevel.callerTypeWorkingTimeMaxX,true) {
			/**
			 * Serialisierungs-ID der Klasse
			 * @see Serializable
			 */
			private static final long serialVersionUID = 6683308072127986212L;
			@Override
			protected void changedByUser() {
				if (workingTimeCheckBox.isVisible() && workingTimeCheckBox.isSelected()) workingTimeCheckBox.setSelected(false);
			}
		};
		p2.add(workingTime,BorderLayout.CENTER);
		workingTime.setAllowChangeDistributionData(!readOnly);
		workingTime.setImageSaveSize(SetupData.getSetup().imageSize);

		tabs.addTab(Language.tr("Editor.SkillLevel.Distribution.HoldingTimeAddOn"),p2=new JPanel(new BorderLayout()));
		p2.add(p3=new JPanel(new FlowLayout(FlowLayout.LEFT)),BorderLayout.NORTH);
		p3.add(workingTimeAddOnComboBox=new JComboBox<>(intervalNames2.toArray(String[]::new)));
		workingTimeAddOnComboBox.addActionListener(e->listSelectionChanged());
		p3.add(workingTimeAddOnCheckBox=new JCheckBox(Language.tr("Editor.SkillLevel.Value.UseGlobal")));
		p2.add(p3=new JPanel(new FlowLayout(FlowLayout.LEFT)),BorderLayout.CENTER);
		p3.add(p4=new JPanel());
		p4.setLayout(new BoxLayout(p4,BoxLayout.Y_AXIS));
		workingTimeAddOn=addVerticalInputLine(p4,Language.tr("Editor.SkillLevel.HoldingTimeAddOn"),"0",60);
		workingTimeAddOn.setEnabled(!readOnly);
		workingTimeAddOn.addKeyListener(new DialogElementListener());
		p4.add(p5=new JPanel(new FlowLayout(FlowLayout.LEFT)));
		p5.add(new JLabel("<html><body style='font-size: 95%;'>"+Language.tr("Editor.SkillLevel.HoldingTimeAddOn.Info")+"</body></html>"));
		if (helpLink.dialogParser!=null) {
			p4.add(p5=new JPanel(new FlowLayout(FlowLayout.LEFT)));
			p5.add(workingTimeAddOnButton=new JButton(Language.tr("Dialog.Button.Help")));
			workingTimeAddOnButton.addActionListener(new ButtonListener());
			workingTimeAddOnButton.setIcon(Images.HELP.getIcon());
		}

		tabs.addTab(Language.tr("Editor.SkillLevel.Distribution.PostProcessingTime"),p2=new JPanel(new BorderLayout()));
		p2.add(p3=new JPanel(new FlowLayout(FlowLayout.LEFT)),BorderLayout.NORTH);
		p3.add(postprocessingTimeComboBox=new JComboBox<>(intervalNames.toArray(String[]::new)));
		postprocessingTimeComboBox.addActionListener(e->listSelectionChanged());
		p3.add(postprocessingTimeCheckBox=new JCheckBox(Language.tr("Editor.SkillLevel.Distribution.UseGlobal")));
		postprocessingTime=new JDistributionPanel(new ExponentialDistribution(300),CallcenterModelSkillLevel.callerTypePostProcessingTimeMaxX,true) {
			/**
			 * Serialisierungs-ID der Klasse
			 * @see Serializable
			 */
			private static final long serialVersionUID = 9170868502143676468L;
			@Override
			protected void changedByUser() {
				if (workingTimeCheckBox.isVisible() && workingTimeCheckBox.isSelected()) workingTimeCheckBox.setSelected(false);
			}
		};
		p2.add(postprocessingTime,BorderLayout.CENTER);
		postprocessingTime.setAllowChangeDistributionData(!readOnly);
		postprocessingTime.setImageSaveSize(SetupData.getSetup().imageSize);

		tabs.addTab("Score",p=new JPanel(new FlowLayout(FlowLayout.LEFT)));
		p2=new JPanel(new GridLayout(3,1)); p.add(p2);
		score=addInputLine(p2,Language.tr("Editor.SkillLevel.Score"),0);
		score.setEnabled(!readOnly);
		score.addKeyListener(new DialogElementListener());

		tabs.setIconAt(0,Images.EDITOR_SKILLLEVEL_PAGE_SERVICE.getIcon());
		tabs.setIconAt(1,Images.EDITOR_SKILLLEVEL_PAGE_WAITING.getIcon());
		tabs.setIconAt(2,Images.EDITOR_SKILLLEVEL_PAGE_POST_PROCESSING.getIcon());
		tabs.setIconAt(3,Images.EDITOR_SKILLLEVEL_PAGE_SCORE.getIcon());

		if (!readOnly) {
			toolsPopup=new JPopupMenu();
			toolsPopup.add(generatorButton=new JMenuItem(Language.tr("Editor.SkillLevel.Tools.LoadDistribution")));
			generatorButton.setIcon(Images.EDITOR_SKILLLEVEL.getIcon());
			generatorButton.addActionListener(new ButtonListener());
		}

		initListSystem(false);
	}

	/**
	 * Initialisiert die Skill-Liste
	 * @param selectLast	Zuletzt gewählten Eintrag nach der Neuinitialisierung wieder auswählen?
	 * @see #list
	 */
	private void initListSystem(final boolean selectLast) {
		if (tempSkill.callerTypeName.size()==0) {
			tabs.setVisible(false);
		} else {
			tabs.setVisible(true);
			if (selectLast) list.setSelectedIndex(listData.getSize()-1); else list.setSelectedIndex(0);
			listSelectionChanged();
		}
		addButton.setEnabled(!readOnly && tempSkill.callerTypeName.size()<callerTypeNames.length);
		delButton.setEnabled(!readOnly && tempSkill.callerTypeName.size()>0);
		copyButton.setEnabled(!readOnly && tempSkill.callerTypeName.size()<callerTypeNames.length);
	}

	@Override
	protected boolean checkData() {
		/* Name */
		if (name.getText().isBlank()) {
			MsgBox.error(this,Language.tr("Editor.SkillLevel.Error.NoName.Title"),Language.tr("Editor.SkillLevel.Error.NoName.Info"));
			return false;
		}

		/* Doppelte Namen */
		String s=name.getText();
		for (int i=0;i<skillLevelNames.length;i++) {
			if (i==indexForThisSkillLevel) continue;
			if (skillLevelNames[i].equalsIgnoreCase(s)) {
				MsgBox.error(this,Language.tr("Editor.SkillLevel.Error.NameInUse.Title"),String.format(Language.tr("Editor.SkillLevel.Error.NameInUse.Info"),s));
				return false;
			}
		}

		return true;
	}

	@Override
	protected void storeData() {

		if (!skill.name.equalsIgnoreCase(name.getText())) {
			RenameEvent event=new RenameEvent(this,skill.name,name.getText());
			for (int i=0;i<listener.size();i++) listener.get(i).renamed(event);
		}

		skill.name=name.getText();

		listSelectionChanged();
		skill.callerTypeName.clear();
		skill.callerTypeWorkingTimeAddOn.clear();
		skill.callerTypeWorkingTime.clear();
		skill.callerTypePostProcessingTime.clear();
		skill.callerTypeIntervalWorkingTimeAddOn.clear();
		skill.callerTypeIntervalWorkingTime.clear();
		skill.callerTypeIntervalPostProcessingTime.clear();
		skill.callerTypeScore.clear();
		for (int i=0;i<tempSkill.callerTypeName.size();i++) {
			skill.callerTypeName.add(tempSkill.callerTypeName.get(i));
			skill.callerTypeWorkingTimeAddOn.add(tempSkill.callerTypeWorkingTimeAddOn.get(i));
			skill.callerTypeWorkingTime.add(tempSkill.callerTypeWorkingTime.get(i));
			skill.callerTypePostProcessingTime.add(tempSkill.callerTypePostProcessingTime.get(i));
			skill.callerTypeIntervalWorkingTimeAddOn.add(tempSkill.callerTypeIntervalWorkingTimeAddOn.get(i));
			skill.callerTypeIntervalWorkingTime.add(tempSkill.callerTypeIntervalWorkingTime.get(i));
			skill.callerTypeIntervalPostProcessingTime.add(tempSkill.callerTypeIntervalPostProcessingTime.get(i));
			skill.callerTypeScore.add(tempSkill.callerTypeScore.get(i));
		}
	}

	/**
	 * Reagiert darauf, wenn sich die Auswahl in {@link #list} geändert hat.
	 * @see #list
	 */
	private void listSelectionChanged() {
		AbstractRealDistribution dist;

		if (lastSelected>=0) {
			if (lastInterval1<0) {
				tempSkill.callerTypeWorkingTime.set(lastSelected,workingTime.getDistribution());
			} else {
				AbstractRealDistribution[] dArray=tempSkill.callerTypeIntervalWorkingTime.get(lastSelected);
				if (workingTimeCheckBox.isSelected()) dArray[lastInterval1]=null; else dArray[lastInterval1]=workingTime.getDistribution();
			}
			if (lastInterval3<0) {
				tempSkill.callerTypeWorkingTimeAddOn.set(lastSelected,workingTimeAddOn.getText());
			} else {
				String[] sArray=tempSkill.callerTypeIntervalWorkingTimeAddOn.get(lastSelected);
				if (workingTimeAddOnCheckBox.isSelected()) sArray[lastInterval3]=null; else sArray[lastInterval3]=workingTimeAddOn.getText();
			}
			if (lastInterval2<0) {
				tempSkill.callerTypePostProcessingTime.set(lastSelected,postprocessingTime.getDistribution());
			} else {
				AbstractRealDistribution[] dArray=tempSkill.callerTypeIntervalPostProcessingTime.get(lastSelected);
				if (postprocessingTimeCheckBox.isSelected()) dArray[lastInterval2]=null; else dArray[lastInterval2]=postprocessingTime.getDistribution();
			}
			Integer i=NumberTools.getNotNegativeInteger(score,false);
			if (i!=null) tempSkill.callerTypeScore.set(lastSelected,i);
		}

		if (lastSelected==list.getSelectedIndex() && lastInterval1==workingTimeComboBox.getSelectedIndex()-1 && lastInterval3==workingTimeAddOnComboBox.getSelectedIndex()-1 && lastInterval2==postprocessingTimeComboBox.getSelectedIndex()-1) return;
		lastSelected=list.getSelectedIndex();
		if (lastSelected<0) return;
		lastInterval1=workingTimeComboBox.getSelectedIndex()-1;
		lastInterval3=workingTimeAddOnComboBox.getSelectedIndex()-1;
		lastInterval2=postprocessingTimeComboBox.getSelectedIndex()-1;

		workingTimeAddOnCheckBox.setVisible(lastInterval3>=0);
		String s=(lastInterval3<0)?null:tempSkill.callerTypeIntervalWorkingTimeAddOn.get(lastSelected)[lastInterval3];
		workingTimeAddOnCheckBox.setSelected(s==null || s.isEmpty() || s.equals("0"));
		workingTimeAddOn.setText((s!=null && !s.isEmpty() && !s.equals("0"))?s:tempSkill.callerTypeWorkingTimeAddOn.get(lastSelected));

		workingTimeCheckBox.setVisible(lastInterval1>=0);
		dist=(lastInterval1<0)?null:tempSkill.callerTypeIntervalWorkingTime.get(lastSelected)[lastInterval1];
		workingTimeCheckBox.setSelected(dist==null);
		workingTime.setDistribution((dist!=null)?dist:tempSkill.callerTypeWorkingTime.get(lastSelected));

		postprocessingTimeCheckBox.setVisible(lastInterval2>=0);
		dist=(lastInterval2<0)?null:tempSkill.callerTypeIntervalPostProcessingTime.get(lastSelected)[lastInterval2];
		postprocessingTimeCheckBox.setSelected(dist==null);
		postprocessingTime.setDistribution((dist!=null)?dist:tempSkill.callerTypePostProcessingTime.get(lastSelected));

		score.setText(""+tempSkill.callerTypeScore.get(lastSelected));
		score.setBackground(NumberTools.getTextFieldDefaultBackground());
	}

	/**
	 * Befehl: Skill hinzufügen
	 */
	private void addSkill() {
		if (tempSkill.callerTypeName.size()==callerTypeNames.length) return;
		String[] options=new String[callerTypeNames.length-tempSkill.callerTypeName.size()];
		int c=0;
		for (int i=0;i<callerTypeNames.length;i++) {
			boolean inList=false;
			for (int j=0;j<tempSkill.callerTypeName.size();j++) if (tempSkill.callerTypeName.get(j).equalsIgnoreCase(callerTypeNames[i])) {inList=true; break;}
			if (!inList) {options[c]=callerTypeNames[i]; c++;}
		}

		String s=(String)JOptionPane.showInputDialog(this,Language.tr("Editor.SkillLevel.Add.Info")+":",Language.tr("Editor.SkillLevel.Add"),JOptionPane.PLAIN_MESSAGE,null,options,options[0]);
		if (s==null) return;

		tempSkill.callerTypeName.add(s);
		tempSkill.callerTypeWorkingTimeAddOn.add("0");
		tempSkill.callerTypeWorkingTime.add(new ExponentialDistribution(180));
		tempSkill.callerTypePostProcessingTime.add(new ExponentialDistribution(120));
		tempSkill.callerTypeIntervalWorkingTimeAddOn.add(new String[48]);
		tempSkill.callerTypeIntervalWorkingTime.add(new AbstractRealDistribution[48]);
		tempSkill.callerTypeIntervalPostProcessingTime.add(new AbstractRealDistribution[48]);
		tempSkill.callerTypeScore.add(0);
		listData.addElement(s);
		initListSystem(true);
	}

	/**
	 * Befehl: Skill löschen
	 */
	private void delSkill() {
		int i=list.getSelectedIndex();
		if (i<0) return;
		if (!MsgBox.confirm(this,
				Language.tr("Editor.SkillLevel.Delete"),
				String.format(Language.tr("Editor.SkillLevel.DeleteSkill.Info"),list.getSelectedValue()),
				Language.tr("Editor.SkillLevel.DeleteSkill.Yes.Info"),
				Language.tr("Editor.SkillLevel.DeleteSkill.No.Info")
				)) return;

		lastSelected=-1;
		tempSkill.callerTypeName.remove(i);
		tempSkill.callerTypeWorkingTimeAddOn.remove(i);
		tempSkill.callerTypeWorkingTime.remove(i);
		tempSkill.callerTypePostProcessingTime.remove(i);
		tempSkill.callerTypeIntervalWorkingTimeAddOn.remove(i);
		tempSkill.callerTypeIntervalWorkingTime.remove(i);
		tempSkill.callerTypeIntervalPostProcessingTime.remove(i);
		tempSkill.callerTypeScore.remove(i);
		listData.remove(i);

		initListSystem(false);
	}

	/**
	 * Befehl: Skill kopieren
	 */
	private void copySkill() {
		listSelectionChanged();
		int index=list.getSelectedIndex();
		if (index<0) return;

		if (tempSkill.callerTypeName.size()==callerTypeNames.length) return;
		String[] options=new String[callerTypeNames.length-tempSkill.callerTypeName.size()];
		int c=0;
		for (int i=0;i<callerTypeNames.length;i++) {
			boolean inList=false;
			for (int j=0;j<tempSkill.callerTypeName.size();j++) if (tempSkill.callerTypeName.get(j).equalsIgnoreCase(callerTypeNames[i])) {inList=true; break;}
			if (!inList) {options[c]=callerTypeNames[i]; c++;}
		}

		String s=(String)JOptionPane.showInputDialog(this,Language.tr("Editor.SkillLevel.Add.Info")+":",Language.tr("Editor.SkillLevel.Add"),JOptionPane.PLAIN_MESSAGE,null,options,options[0]);
		if (s==null) return;

		tempSkill.callerTypeName.add(s);
		tempSkill.callerTypeWorkingTimeAddOn.add(tempSkill.callerTypeWorkingTimeAddOn.get(index));
		tempSkill.callerTypeWorkingTime.add(DistributionTools.cloneDistribution(tempSkill.callerTypeWorkingTime.get(index)));
		tempSkill.callerTypePostProcessingTime.add(DistributionTools.cloneDistribution(tempSkill.callerTypePostProcessingTime.get(index)));
		String[] sArray=new String[tempSkill.callerTypeIntervalWorkingTime.get(index).length];
		AbstractRealDistribution[] dArray;
		AbstractRealDistribution[] dOrig;
		String[] sOrig=tempSkill.callerTypeIntervalWorkingTimeAddOn.get(index);
		for (int i=0;i<sArray.length;i++) sArray[i]=sOrig[i];
		tempSkill.callerTypeIntervalWorkingTimeAddOn.add(sArray);
		dOrig=tempSkill.callerTypeIntervalWorkingTime.get(index);
		dArray=new AbstractRealDistribution[tempSkill.callerTypeIntervalWorkingTime.get(index).length];
		for (int i=0;i<dArray.length;i++) dArray[i]=DistributionTools.cloneDistribution(dOrig[i]);
		tempSkill.callerTypeIntervalWorkingTime.add(dArray);
		dOrig=tempSkill.callerTypeIntervalPostProcessingTime.get(index);
		dArray=new AbstractRealDistribution[tempSkill.callerTypeIntervalWorkingTime.get(index).length];
		for (int i=0;i<dArray.length;i++) dArray[i]=DistributionTools.cloneDistribution(dOrig[i]);
		tempSkill.callerTypeIntervalPostProcessingTime.add(dArray);
		tempSkill.callerTypeScore.add(tempSkill.callerTypeScore.get(index));
		listData.addElement(s);
		initListSystem(true);
	}

	/**
	 * Kopiert die Einstellungen aus dem aktuelle Skill zu einem anderen Skill
	 * @param index	Index des Ziel-Skills (oder -1 für alle)
	 */
	private void copySettingsToSkillLevel(final int index) {
		listSelectionChanged();
		for (int i=0;i<tempSkill.callerTypeName.size();i++) if (i!=lastSelected && (i==index || index==-1)) {
			tempSkill.callerTypeWorkingTimeAddOn.set(i,tempSkill.callerTypeWorkingTimeAddOn.get(lastSelected));
			tempSkill.callerTypeWorkingTime.set(i,tempSkill.callerTypeWorkingTime.get(lastSelected));
			tempSkill.callerTypePostProcessingTime.set(i,tempSkill.callerTypePostProcessingTime.get(lastSelected));
			String[] sArray=new String[tempSkill.callerTypeIntervalWorkingTime.get(lastSelected).length];
			AbstractRealDistribution[] dArray;
			AbstractRealDistribution[] dOrig;
			String[] sOrig=tempSkill.callerTypeIntervalWorkingTimeAddOn.get(lastSelected);
			for (int j=0;j<sArray.length;j++) sArray[j]=sOrig[j];
			tempSkill.callerTypeIntervalWorkingTimeAddOn.set(i,sArray);
			dOrig=tempSkill.callerTypeIntervalWorkingTime.get(lastSelected);
			dArray=new AbstractRealDistribution[tempSkill.callerTypeIntervalWorkingTime.get(lastSelected).length];
			for (int j=0;j<dArray.length;j++) dArray[j]=DistributionTools.cloneDistribution(dOrig[j]);
			tempSkill.callerTypeIntervalWorkingTime.set(i,dArray);
			dOrig=tempSkill.callerTypeIntervalPostProcessingTime.get(lastSelected);
			dArray=new AbstractRealDistribution[tempSkill.callerTypeIntervalWorkingTime.get(lastSelected).length];
			for (int j=0;j<dArray.length;j++) dArray[j]=DistributionTools.cloneDistribution(dOrig[j]);
			tempSkill.callerTypeIntervalPostProcessingTime.set(i,dArray);
			tempSkill.callerTypeScore.set(i,tempSkill.callerTypeScore.get(lastSelected));
		}
	}

	@Override
	public int getTabIndex() {
		return tabs.getSelectedIndex();
	}

	@Override
	public void setTabIndex(int index) {
		if (index>=0) tabs.setSelectedIndex(index);
	}

	/**
	 * Reagiert auf Klicks auf die verschiedenen Schaltflächen in dem Dialog
	 */
	private class ButtonListener implements ActionListener {
		/**
		 * Konstruktor der Klasse
		 */
		public ButtonListener() {
			/*
			 * Wird nur benötigt, um einen JavaDoc-Kommentar für diesen (impliziten) Konstruktor
			 * setzen zu können, damit der JavaDoc-Compiler keine Warnung mehr ausgibt.
			 */
		}

		@Override
		public void actionPerformed(ActionEvent e) {
			if (e.getSource()==toolsButton) {
				if (toolsPopup.getSubElements().length>1) toolsPopup.remove(1);
				if (list.getModel().getSize()>1) {
					toolsPopupCopyTo=new JMenuItem[list.getModel().getSize()+1];
					JMenu m; toolsPopup.add(m=new JMenu(Language.tr("Editor.SkillLevel.CopySkillDataToOtherSkill")));
					for (int i=0;i<list.getModel().getSize();i++) {
						m.add(toolsPopupCopyTo[i]=new JMenuItem(list.getModel().getElementAt(i)));
						toolsPopupCopyTo[i].setEnabled(i!=lastSelected);
						toolsPopupCopyTo[i].setIcon(Images.EDITOR_CALLER.getIcon());
						toolsPopupCopyTo[i].addActionListener(new ButtonListener());
					}
					if (list.getModel().getSize()>2) {
						m.addSeparator();
						m.add(toolsPopupCopyTo[toolsPopupCopyTo.length-1]=new JMenuItem(Language.tr("Editor.SkillLevel.CopySkillDataToOtherSkill.All")));
						toolsPopupCopyTo[toolsPopupCopyTo.length-1].setIcon(Images.EDITOR_AGENTS.getIcon());
						toolsPopupCopyTo[toolsPopupCopyTo.length-1].addActionListener(new ButtonListener());
					}
				}
				toolsPopup.show(toolsButton,0,toolsButton.getBounds().height);
				return;
			}
			if (e.getSource()==addButton) {addSkill(); return;}
			if (e.getSource()==delButton) {delSkill(); return;}
			if (e.getSource()==copyButton) {copySkill(); return;}
			if (e.getSource()==generatorButton) {openGenerator=true; if (!closeDialog(CLOSED_BY_OK)) openGenerator=false; return;}
			if (e.getSource()==workingTimeAddOnButton && helpLink.dialogParser!=null) {helpLink.dialogParser.run(); return;}
			if (toolsPopupCopyTo!=null) for (int i=0;i<toolsPopupCopyTo.length;i++) if (e.getSource()==toolsPopupCopyTo[i]) {
				copySettingsToSkillLevel((i==toolsPopupCopyTo.length-1)?-1:i);
				return;
			}
		}
	}

	/**
	 * Reagiert auf Ereignisse für die Listendarstellung {@link SkillLevelEditDialog#list}
	 * @see SkillLevelEditDialog#list
	 */
	private class ListListener implements ListSelectionListener,KeyListener {
		/**
		 * Konstruktor der Klasse
		 */
		public ListListener() {
			/*
			 * Wird nur benötigt, um einen JavaDoc-Kommentar für diesen (impliziten) Konstruktor
			 * setzen zu können, damit der JavaDoc-Compiler keine Warnung mehr ausgibt.
			 */
		}

		@Override
		public void valueChanged(ListSelectionEvent e) {listSelectionChanged();}

		@Override
		public void keyTyped(KeyEvent e) {}

		@Override
		public void keyPressed(KeyEvent e) {
			if (e.getKeyCode()==KeyEvent.VK_DELETE && e.getModifiersEx()==0) {delSkill(); e.consume();}
		}

		@Override
		public void keyReleased(KeyEvent e) {}
	}

	/**
	 * Reagiert auf Tastendrücke in den verschiedenen
	 * Eingabefeldern des Dialogs
	 */
	private class DialogElementListener implements KeyListener {
		/**
		 * Konstruktor der Klasse
		 */
		public DialogElementListener() {
			/*
			 * Wird nur benötigt, um einen JavaDoc-Kommentar für diesen (impliziten) Konstruktor
			 * setzen zu können, damit der JavaDoc-Compiler keine Warnung mehr ausgibt.
			 */
		}

		/**
		 * Reagiert auf ein Tastenereignis
		 * @param e	Tastenereignis
		 * @see #keyTyped(KeyEvent)
		 * @see #keyPressed(KeyEvent)
		 * @see #keyReleased(KeyEvent)
		 */
		private void keyEvent(KeyEvent e) {
			NumberTools.getNotNegativeInteger(score,true);
			MathParser calc=new CalcSystem(workingTimeAddOn.getText(),new String[]{"w"});
			if (calc.parse()>=0) workingTimeAddOn.setBackground(Color.red); else workingTimeAddOn.setBackground(NumberTools.getTextFieldDefaultBackground());
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
	 * Renderer für die Kundentypendarstellung in {@link SkillLevelEditDialog#list()}
	 * @see SkillLevelEditDialog#list
	 */
	private final class ClientTypeListRenderer extends JLabel implements ListCellRenderer<String> {
		/**
		 * Serialisierungs-ID der Klasse
		 * @see Serializable
		 */
		private static final long serialVersionUID = 8557411673629233407L;

		/**
		 * Konstruktor der Klasse
		 */
		public ClientTypeListRenderer() {
			setHorizontalAlignment(LEFT);
			setVerticalAlignment(CENTER);
		}

		@Override
		public Component getListCellRendererComponent(JList<? extends String> list, String value, int index, boolean isSelected, boolean cellHasFocus) {
			if (isSelected) {
				setBackground(list.getSelectionBackground());
				setForeground(list.getSelectionForeground());
				setOpaque(true);
			} else {
				setBackground(list.getBackground());
				setForeground(list.getForeground());
				setOpaque(false);
			}

			setIcon(Images.EDITOR_CALLER.getIcon());
			setText(" "+value);

			return this;
		}
	}
}
