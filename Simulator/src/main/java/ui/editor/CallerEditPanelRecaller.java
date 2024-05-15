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
import java.awt.event.ActionEvent;
import java.awt.event.KeyEvent;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.Icon;
import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextField;

import language.Language;
import mathtools.NumberTools;
import systemtools.MsgBox;
import ui.images.Images;
import ui.model.CallcenterModelCaller;

/**
 * In diesem Panel können die Wiederanrufraten für eine Kundengruppe eingestellt werden.
 * @author Alexander Herzog
 * @see CallerEditDialog
 */
public class CallerEditPanelRecaller extends CallerEditPanel {
	/**
	 * Serialisierungs-ID der Klasse
	 * @see Serializable
	 */
	private static final long serialVersionUID = 8881145422082171403L;

	/** Liste der Kundentypen für Wiederanrufe */
	private final List<String> recallTypeSkillType;
	/** Liste der Raten für die Kundentypen bei Wiederanrufen */
	private final List<Double> recallTypeSkillTypeProbability;
	/** Skill-Level-Namen für die Agententyp-abhängigen Wiederanrufe */
	private final List<List<String>> recallTypeSkillTypeName;
	/** Raten für die Agententyp-abhängigen Wiederanrufe */
	private final List<List<Double>> recallTypeSkillTypeRate;

	/** Eingabefeld für die allgemeine Wiederanrufwahrscheinlichkeit */
	private final JTextField recallProbability;
	/** Beschriftung für die Wahrscheinlichkeiten von Wiederanrufen zum aktuellen Kundentyp (um so direkt auf Änderungen des Namens des aktuellen Typs reagieren zu können) */
	private JLabel recallToThisType;
	/** Eingabefelder für die Wiederanrufwahrscheinlichkeiten zu den einzelnen Kundentypen */
	private final JTextField[] recallToType;
	/** Schaltfläche "Agententyp-abhängige Wiederanrufe" */
	private JButton recallSpecialButton;
	/** Zeigt an, ob Agententyp-abhängige Wiederanrufe vorhanden sind */
	private JLabel recallSpecialLabel;


	/**
	 * Konstruktor der Klasse
	 * @param initData	Informationen über den aktuellen Kundentyp zur Initialisierung des Panels
	 * @see CallerEditPanel.InitData
	 */
	public CallerEditPanelRecaller(final InitData initData) {
		super(initData);

		recallTypeSkillType=new ArrayList<>();
		for (int i=0;i<caller.recallTypeSkillType.size();i++) recallTypeSkillType.add(caller.recallTypeSkillType.get(i));
		recallTypeSkillTypeProbability=new ArrayList<>();
		for (int i=0;i<caller.recallTypeSkillTypeProbability.size();i++) recallTypeSkillTypeProbability.add(caller.recallTypeSkillTypeProbability.get(i));
		recallTypeSkillTypeName=new ArrayList<>();
		for (int i=0;i<caller.recallTypeSkillTypeName.size();i++) {
			List<String> list1=caller.recallTypeSkillTypeName.get(i);
			List<String> list2=new ArrayList<>(); recallTypeSkillTypeName.add(list2);
			for (int j=0;j<list1.size();j++) list2.add(list1.get(j));
		}
		recallTypeSkillTypeRate=new ArrayList<>();
		for (int i=0;i<caller.recallTypeSkillTypeRate.size();i++) {
			List<Double> list1=caller.recallTypeSkillTypeRate.get(i);
			List<Double> list2=new ArrayList<>(); recallTypeSkillTypeRate.add(list2);
			for (int j=0;j<list1.size();j++) list2.add(list1.get(j));
		}

		JPanel p2, p3, p4;

		setLayout(new BorderLayout());

		add(p2=new JPanel(new GridLayout(3,1)),BorderLayout.NORTH);
		p2.setBorder(BorderFactory.createEmptyBorder(5,5,0,0));
		p2.add(new JLabel("<html><body>"+Language.tr("Editor.Caller.RecallProbability.Info")+"</boby></html>"),BorderLayout.NORTH);
		recallProbability=addPercentInputLine(p2,Language.tr("Editor.Caller.RecallProbability.Probability"),caller.recallProbability);
		recallProbability.setEnabled(!readOnly);
		recallProbability.addKeyListener(dialogElementListener);
		p3=new JPanel(new GridLayout(callerTypeNames.length,2));
		recallToThisType=null;
		recallToType=new JTextField[callerTypeNames.length];
		for (int i=0;i<callerTypeNames.length;i++) {
			JLabel l=new JLabel(callerTypeNames[i]);
			final Icon icon=(i==callerTypeIndexForThisType)?Images.EDITOR_CALLER_RED.getIcon():Images.EDITOR_CALLER.getIcon();
			if (icon!=null) l.setIcon(icon);
			if (i==callerTypeIndexForThisType) recallToThisType=l;
			p4=new JPanel(new FlowLayout(FlowLayout.LEFT));
			p4.add(l);
			p3.add(p4);
			recallToType[i]=new JTextField(10);
			double d=0;
			for (int j=0;j<caller.recallTypeName.size();j++) if (caller.recallTypeName.get(j).equalsIgnoreCase(callerTypeNames[i])) {
				d=caller.recallTypeRate.get(j); break;
			}
			recallToType[i].setText(NumberTools.formatNumberMax(d));
			recallToType[i].setEnabled(!readOnly);
			recallToType[i].addKeyListener(dialogElementListener);
			p4=new JPanel(new FlowLayout(FlowLayout.LEFT));
			p4.add(recallToType[i]);
			p3.add(p4);
		}

		JScrollPane s=new JScrollPane(p4=new JPanel());
		p4.setLayout(new BoxLayout(p4,BoxLayout.PAGE_AXIS));
		p4.add(p3);
		p4.add(Box.createVerticalGlue());
		s.setColumnHeaderView(new JLabel(Language.tr("Editor.Caller.RecallProbability.ClientTypeChange")));
		s.setBorder(BorderFactory.createEmptyBorder(0,5,0,0));
		add(s,BorderLayout.CENTER);

		add(p2=new JPanel(new FlowLayout(FlowLayout.LEFT)),BorderLayout.SOUTH);
		p2.add(recallSpecialButton=new JButton(Language.tr("Editor.Caller.RecallProbability.AgentTypeDependendRecalls")));
		recallSpecialButton.addActionListener(dialogElementListener);
		recallSpecialButton.setIcon(Images.EDITOR_CALLER.getIcon());
		p2.add(recallSpecialLabel=new JLabel());

		setRecallSpecialLabel();
	}

	@Override
	public String[] check(KeyEvent e) {
		String[] result=null;

		if (NumberTools.getProbability(recallProbability,true)==null) {
			if (result==null) result=new String[]{Language.tr("Editor.Caller.Error.RecallProbability.Title"),String.format(Language.tr("Editor.Caller.Error.RecallProbability.Info"),recallProbability.getText())};
		}
		for (int i=0;i<recallToType.length;i++) if ((!recallToType[i].getText().isBlank()) && (NumberTools.getNotNegativeDouble(recallToType[i],true)==null)) {
			if (result==null) result=new String[]{Language.tr("Editor.Caller.Error.RecallCallerType.Title"),String.format(Language.tr("Editor.Caller.Error.RecallCallerType.Info"),callerTypeNames[i],recallToType[i].getText())};
		}

		return result;
	}

	@Override
	public void writeToCaller(CallcenterModelCaller newCaller) {
		newCaller.recallProbability=NumberTools.getProbability(recallProbability,false);
		newCaller.recallTypeName.clear();
		newCaller.recallTypeRate.clear();
		for (int i=0;i<recallToType.length;i++) {
			if (recallToType[i].getText().isBlank()) continue;
			Double d=NumberTools.getNotNegativeDouble(recallToType[i],false);
			newCaller.recallTypeName.add(callerTypeNames[i]);
			newCaller.recallTypeRate.add(d);
		}

		/* Skill-Level-abhängige Wiederanrufe */
		newCaller.recallTypeSkillType.clear();
		for (int i=0;i<recallTypeSkillType.size();i++) newCaller.recallTypeSkillType.add(recallTypeSkillType.get(i));
		newCaller.recallTypeSkillTypeProbability.clear();
		for (int i=0;i<recallTypeSkillTypeProbability.size();i++) newCaller.recallTypeSkillTypeProbability.add(recallTypeSkillTypeProbability.get(i));
		newCaller.recallTypeSkillTypeName.clear();
		for (int i=0;i<recallTypeSkillTypeName.size();i++) {
			List<String> list1=recallTypeSkillTypeName.get(i);
			List<String> list2=new ArrayList<>(); newCaller.recallTypeSkillTypeName.add(list2);
			for (int j=0;j<list1.size();j++) list2.add(list1.get(j));
		}
		newCaller.recallTypeSkillTypeRate.clear();
		for (int i=0;i<recallTypeSkillTypeRate.size();i++) {
			List<Double> list1=recallTypeSkillTypeRate.get(i);
			List<Double> list2=new ArrayList<>(); newCaller.recallTypeSkillTypeRate.add(list2);
			for (int j=0;j<list1.size();j++) list2.add(list1.get(j));
		}
	}

	@Override
	public String getTabName() {
		return Language.tr("Editor.Caller.Tabs.RecallProbability");
	}

	@Override
	public Icon getTabIconObject() {
		return Images.EDITOR_CALLER_PAGE_RECALLER.getIcon();
	}

	@Override
	public void nameChange(String newName) {
		if (recallToThisType!=null) recallToThisType.setText(newName);
	}

	/**
	 * Konfiguriert die Informationsanzeige, ob Agententyp-abhängige Wiederanrufe vorliegen.
	 * @see #recallSpecialLabel
	 */
	private void setRecallSpecialLabel() {
		recallSpecialLabel.setText(
				(recallTypeSkillType.size()>0)?Language.tr("Editor.Caller.RecallProbability.AgentSpecific.Active"):Language.tr("Editor.Caller.RecallProbability.AgentSpecific.NotActive")
				);
	}

	@Override
	protected void processDialogEvents(ActionEvent e) {
		if (e.getSource()==recallSpecialButton) {
			if (skills.length==0) {
				MsgBox.error(parent,Language.tr("Editor.Caller.Error.NoSkillRecall.Title"),Language.tr("Editor.Caller.Error.NoSkillRecall.Info"));
				return;
			}
			CallerSpecialEditDialog specialDialog=new CallerSpecialEditDialog(parent,callerTypeNames,skills,recallTypeSkillType,recallTypeSkillTypeProbability,recallTypeSkillTypeName,recallTypeSkillTypeRate,readOnly,helpCallback,CallerSpecialEditDialog.Mode.MODE_RECALL);
			specialDialog.setVisible(true);
			setRecallSpecialLabel();
			return;
		}
	}
}
