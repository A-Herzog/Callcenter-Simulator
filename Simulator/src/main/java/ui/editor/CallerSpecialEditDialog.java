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
import java.awt.FlowLayout;
import java.awt.GridLayout;
import java.awt.Window;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.DefaultListModel;
import javax.swing.JCheckBox;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextField;
import javax.swing.ListCellRenderer;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;

import language.Language;
import mathtools.NumberTools;
import ui.images.Images;

/**
 * In diesem Dialog können Agententyp-abhängige
 * Weiterleitungs- oder Wiederanrufregeln für einzelne
 * Kundengruppen definiert werden, d.h. die Wahrscheinlichkeit
 * für ein entsprechendes Event für eine Kundengruppe in
 * Abhängigkeit davon von was für einem Agenten (Skill-Level)
 * der jeweilige Kunde bedient wurde.
 * @author Alexander Herzog
 * @version 1.0
 */
public class CallerSpecialEditDialog extends BaseEditDialog {
	/**
	 * Serialisierungs-ID der Klasse
	 * @see Serializable
	 */
	private static final long serialVersionUID = 9065392515740427353L;

	/**
	 * Welche Daten sollen in dem Dialog bearbeitet werden?
	 * @author Alexander Herzog
	 * @see CallerSpecialEditDialog#CallerSpecialEditDialog(Window, String[], String[], List, List, List, List, boolean, Runnable, Mode)
	 */
	public enum Mode {
		/** Skill-Level-abhängige Weiterleitungsraten */
		MODE_CONTINUE,
		/** Skill-Level-abhängige Wiederanrufraten */
		MODE_RECALL
	}

	/**
	 * Welche Daten sollen in dem Dialog bearbeitet werden?
	 * @see Mode
	 */
	private final Mode mode;

	/** Liste der Namen der Skill-Levels */
	private final String[] skills;

	/** Liste mit Skill-Levels für Skill-Level-abhängige Werte */
	private final List<String> continueTypeSkillType;
	/** Liste mit den Wahrscheinlichkeiten pro Skill-Level */
	private final List<Double> continueTypeSkillTypeProbability;
	/** Liste mit den Listen der neuen Kundentypnamen pro Skill-Level */
	private final List<List<String>> continueTypeSkillTypeName;
	/** Liste mit den Listen der Kundentyp-Änderungs-Wahrscheinlichkeiten pro Skill-Level */
	private final List<List<Double>> continueTypeSkillTypeRate;

	/** Sonderregel für den Kundentyp aktiv? */
	private final boolean[] active;
	/** Wahrscheinlichkeiten für dies Kundentypen */
	private final double[] probability;
	/** Raten mit denen der Kundentyp geändert wird */
	private final double[][] rate;

	/** Zuletzt in {@link #list} gewählter Eintrag */
	private int lastSelectedIndex;
	/** Liste der Kundentypen */
	private JList<String> list;
	/** Datenmodell für die Liste der Kundentypen ({@link #list()}) */
	private DefaultListModel<String> listData;

	/** Option: Sonderregel für den Kundentyp aktiv? */
	private JCheckBox ruleActive;
	/** Eingabefeld für die Wahrscheinlichkeit für diesen Kundentyp */
	private JTextField ruleProbability;
	/** Eingabefelder für die Raten mit denen der Kundentyp geändert wird */
	private JTextField[] ruleRate;

	/**
	 * Konstruktor der Klasse
	 * @param owner	Übergeordnetes Fenster
	 * @param callerTypeNames	Liste der Namen aller Anrufertypen (für optionale Typänderungen)
	 * @param skills	Liste der Namen der Skill-Levels
	 * @param typeSkillType	Liste mit Skill-Levels für Skill-Level-abhängige Werte
	 * @param typeSkillTypeProbability	Liste mit den Wahrscheinlichkeiten pro Skill-Level
	 * @param typeSkillTypeName	Liste mit den Listen der neuen Kundentypnamen pro Skill-Level
	 * @param typeSkillTypeRate	Liste mit den Listen der Kundentyp-Änderungs-Wahrscheinlichkeiten pro Skill-Level
	 * @param readOnly	Nur-Lese-Status
	 * @param helpCallback	Hilfe-Callback
	 * @param mode	Welche Daten sollen in dem Dialog bearbeitet werden?
	 */
	public CallerSpecialEditDialog(final Window owner, final String[] callerTypeNames, final String[] skills, final List<String> typeSkillType, final List<Double> typeSkillTypeProbability, final List<List<String>> typeSkillTypeName, final List<List<Double>> typeSkillTypeRate, final boolean readOnly, final Runnable helpCallback, final Mode mode) {
		super(owner,(mode==Mode.MODE_CONTINUE?Language.tr("Editor.Caller.Special.TitleForwarding"):Language.tr("Editor.Caller.Special.TitleRecalling")),callerTypeNames,readOnly,helpCallback);

		this.mode=mode;
		this.skills=skills;
		this.continueTypeSkillType=typeSkillType;
		this.continueTypeSkillTypeProbability=typeSkillTypeProbability;
		this.continueTypeSkillTypeName=typeSkillTypeName;
		this.continueTypeSkillTypeRate=typeSkillTypeRate;

		active=new boolean[skills.length];
		probability=new double[skills.length];
		rate=new double[skills.length][callerTypeNames.length];
		loadData();

		createSimpleGUI(500,400,null,null);
	}

	/**
	 * Lädt die im Konstruktor übergebenen Daten in die internen Strukturen.
	 */
	private void loadData() {
		/* Alles auf 0 setzen */
		for (int i=0;i<active.length;i++) {
			active[i]=false;
			probability[i]=0;
			for (int j=0;j<callerTypeNames.length;j++) rate[i][j]=0;
		}

		/* Daten zuordnen */
		for (int i=0;i<continueTypeSkillType.size();i++) {
			int index=-1;
			String s=continueTypeSkillType.get(i);
			for (int j=0;j<skills.length;j++) if (skills[j].equalsIgnoreCase(s)) {index=j; break;}
			if (index<0) continue;

			/* und Daten übertragen */
			active[index]=true;
			probability[index]=continueTypeSkillTypeProbability.get(i);

			/* Beim Datenüberagen: Anruferzuordnungen */
			List<String> names=continueTypeSkillTypeName.get(i);
			List<Double> rates=continueTypeSkillTypeRate.get(i);
			for (int j=0;j<names.size();j++) {
				int index2=-1;
				s=names.get(j);
				for (int k=0;k<callerTypeNames.length;k++) if (callerTypeNames[k].equalsIgnoreCase(s)) {index2=k; break;}
				if (index2<0) continue;
				rate[index][index2]=rates.get(j);
			}
		}
	}

	@Override
	protected void createSimpleContent(JPanel content) {
		content.setLayout(new BorderLayout());

		/* Liste */
		content.add(list=new JList<String>(listData=new DefaultListModel<String>()),BorderLayout.WEST);
		list.addListSelectionListener(new ListListener());
		list.setBorder(BorderFactory.createLineBorder(Color.BLACK));
		for (int i=0;i<skills.length;i++) listData.addElement(skills[i]);
		list.setCellRenderer(new SkillLevelListRenderer());

		/* Inhalt */
		JPanel p,p2,p3,p4;
		content.add(p=new JPanel(),BorderLayout.CENTER);
		p.setLayout(new BoxLayout(p,BoxLayout.Y_AXIS));

		p.add(p2=new JPanel(new GridLayout(3,1)),BorderLayout.NORTH);
		p2.setBorder(BorderFactory.createEmptyBorder(0,5,0,0));
		p2.add(ruleActive=new JCheckBox(Language.tr("Editor.Caller.Special.RuleForSkillLevel")));
		ruleProbability=addPercentInputLine(p2,(mode==Mode.MODE_CONTINUE?Language.tr("Editor.Caller.Forwarding.Probability"):Language.tr("Editor.Caller.RecallProbability.Probability")),"");
		ruleProbability.setEnabled(!readOnly);

		p3=new JPanel(new GridLayout(callerTypeNames.length,2));
		ruleRate=new JTextField[callerTypeNames.length];
		for (int i=0;i<callerTypeNames.length;i++) {
			JLabel l=new JLabel(callerTypeNames[i]);
			p4=new JPanel(new FlowLayout(FlowLayout.LEFT));
			p4.add(l);
			p3.add(p4);
			ruleRate[i]=new JTextField(10);
			ruleRate[i].setText("");
			ruleRate[i].setEnabled(!readOnly);
			p4=new JPanel(new FlowLayout(FlowLayout.LEFT));
			p4.add(ruleRate[i]);
			p3.add(p4);
		}
		JScrollPane s=new JScrollPane(p4=new JPanel());
		p4.setLayout(new BoxLayout(p4,BoxLayout.PAGE_AXIS));
		p4.add(p3);
		p4.add(Box.createVerticalGlue());
		s.setColumnHeaderView(new JLabel(mode==Mode.MODE_CONTINUE?Language.tr("Editor.Caller.Forwarding.RatesToClientTypes"):Language.tr("Editor.Caller.RecallProbability.ClientTypeChange")));
		s.setBorder(BorderFactory.createEmptyBorder(0,5,0,0));
		p.add(s);

		lastSelectedIndex=-1;
		list.setSelectedIndex(0);
	}

	/* (non-Javadoc)
	 * @see complexcallcenter.editor.BaseEditDialog#checkData()
	 */
	@Override
	protected boolean checkData() {return true;}

	/* (non-Javadoc)
	 * @see complexcallcenter.editor.BaseEditDialog#storeData()
	 */
	@Override
	protected void storeData() {
		list.getListSelectionListeners()[0].valueChanged(null);

		continueTypeSkillType.clear();
		continueTypeSkillTypeProbability.clear();
		continueTypeSkillTypeName.clear();
		continueTypeSkillTypeRate.clear();

		for (int i=0;i<active.length;i++) if (active[i]) {
			continueTypeSkillType.add(skills[i]);
			continueTypeSkillTypeProbability.add(probability[i]);
			List<String> names=new ArrayList<String>(); continueTypeSkillTypeName.add(names);
			List<Double> rates=new ArrayList<Double>(); continueTypeSkillTypeRate.add(rates);
			for (int j=0;j<callerTypeNames.length;j++) {
				names.add(callerTypeNames[j]);
				rates.add(rate[i][j]);
			}
		}
	}

	/**
	 * Reagiert auf eine veränderte Auswahl in {@link CallerSpecialEditDialog#list()}
	 * @see CallerSpecialEditDialog#list
	 */
	private class ListListener implements ListSelectionListener {
		@Override
		public void valueChanged(ListSelectionEvent e) {
			if (lastSelectedIndex>=0) {
				active[lastSelectedIndex]=ruleActive.isSelected();
				probability[lastSelectedIndex]=0;
				String s=ruleProbability.getText();
				if (!s.trim().isEmpty())  {
					Double d=NumberTools.getProbability(s);
					if (d!=null && d>=0 && d<=1) probability[lastSelectedIndex]=d;
				}
				for (int i=0;i<ruleRate.length;i++) {
					rate[lastSelectedIndex][i]=0;
					s=ruleRate[i].getText();
					if (!s.trim().isEmpty())  {
						Double d=NumberTools.getNotNegativeDouble(s);
						if (d!=null) rate[lastSelectedIndex][i]=d;
					}
				}
			}

			if (e==null) return;
			int index=list.getSelectedIndex();
			lastSelectedIndex=index;
			if (index<0) return;
			ruleActive.setSelected(active[index]);
			ruleProbability.setText(NumberTools.formatNumberMax(probability[index]*100)+"%");
			for (int i=0;i<ruleRate.length;i++)
				ruleRate[i].setText(NumberTools.formatNumberMax(rate[index][i]));
		}
	}

	/**
	 * Renderer für die Liste der Skill-Level
	 * @see CallerSpecialEditDialog#list
	 */
	private final class SkillLevelListRenderer extends JLabel implements ListCellRenderer<String> {
		/**
		 * Serialisierungs-ID der Klasse
		 * @see Serializable
		 */
		private static final long serialVersionUID = 5330825980819412995L;

		/**
		 * Konstruktor der Klasse
		 */
		public SkillLevelListRenderer() {
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

			setIcon(Images.EDITOR_SKILLLEVEL.getIcon());
			setText(" "+value);

			return this;
		}
	}
}
