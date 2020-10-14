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
import java.net.URL;
import java.util.ArrayList;
import java.util.List;

import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.Icon;
import javax.swing.ImageIcon;
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
 * In diesem Panel können die Weiterleitungsraten für eine Kundengruppe eingestellt werden.
 * @author Alexander Herzog
 * @see CallerEditDialog
 */
public class CallerEditPanelForwarding extends CallerEditPanel {
	/**
	 * Serialisierungs-ID der Klasse
	 * @see Serializable
	 */
	private static final long serialVersionUID = 8805197214671745707L;

	private final List<String> continueTypeSkillType;
	private final List<Double> continueTypeSkillTypeProbability;
	private final List<List<String>> continueTypeSkillTypeName;
	private final List<List<Double>> continueTypeSkillTypeRate;

	private final JTextField continueProbability;
	private JLabel continueToThisType;
	private final JTextField[] continueToType;
	private JLabel continueSpecialLabel;
	private JButton continueSpecialButton;

	/**
	 * Konstruktor der Klasse
	 * @param initData	Informamtionen über den aktuellen Kundentyp zur Initialisierung des Panels
	 * @see CallerEditPanel.InitData
	 */
	public CallerEditPanelForwarding(final InitData initData) {
		super(initData);

		continueTypeSkillType=new ArrayList<String>();
		for (int i=0;i<caller.continueTypeSkillType.size();i++) continueTypeSkillType.add(caller.continueTypeSkillType.get(i));
		continueTypeSkillTypeProbability=new ArrayList<Double>();
		for (int i=0;i<caller.continueTypeSkillTypeProbability.size();i++) continueTypeSkillTypeProbability.add(caller.continueTypeSkillTypeProbability.get(i));
		continueTypeSkillTypeName=new ArrayList<List<String>>();
		for (int i=0;i<caller.continueTypeSkillTypeName.size();i++) {
			List<String> list1=caller.continueTypeSkillTypeName.get(i);
			List<String> list2=new ArrayList<String>(); continueTypeSkillTypeName.add(list2);
			for (int j=0;j<list1.size();j++) list2.add(list1.get(j));
		}
		continueTypeSkillTypeRate=new ArrayList<List<Double>>();
		for (int i=0;i<caller.continueTypeSkillTypeRate.size();i++) {
			List<Double> list1=caller.continueTypeSkillTypeRate.get(i);
			List<Double> list2=new ArrayList<Double>(); continueTypeSkillTypeRate.add(list2);
			for (int j=0;j<list1.size();j++) list2.add(list1.get(j));
		}

		setLayout(new BorderLayout());

		URL imgURL;
		JPanel p2, p3, p4;

		add(p2=new JPanel(new GridLayout(2,1)),BorderLayout.NORTH);
		p2.setBorder(BorderFactory.createEmptyBorder(0,5,0,0));
		continueProbability=addPercentInputLine(p2,Language.tr("Editor.Caller.Forwarding.Probability"),caller.continueProbability);
		continueProbability.setEnabled(!readOnly);
		continueProbability.addKeyListener(dialogElementListener);

		p3=new JPanel(new GridLayout(callerTypeNames.length,2));
		continueToThisType=null;
		continueToType=new JTextField[callerTypeNames.length];
		for (int i=0;i<callerTypeNames.length;i++) {
			JLabel l=new JLabel(callerTypeNames[i]);
			imgURL=(i==callerTypeIndexForThisType)?Images.EDITOR_CALLER_RED.getURL():Images.EDITOR_CALLER.getURL();
			if (imgURL!=null) l.setIcon(new ImageIcon(imgURL));
			if (i==callerTypeIndexForThisType) continueToThisType=l;
			p4=new JPanel(new FlowLayout(FlowLayout.LEFT));
			p4.add(l);
			p3.add(p4);
			continueToType[i]=new JTextField(10);
			double d=0;
			for (int j=0;j<caller.continueTypeName.size();j++) if (caller.continueTypeName.get(j).equalsIgnoreCase(callerTypeNames[i])) {
				d=caller.continueTypeRate.get(j); break;
			}
			continueToType[i].setText(NumberTools.formatNumberMax(d));
			continueToType[i].setEnabled(!readOnly);
			continueToType[i].addKeyListener(dialogElementListener);
			p4=new JPanel(new FlowLayout(FlowLayout.LEFT));
			p4.add(continueToType[i]);
			p3.add(p4);
		}

		JScrollPane s=new JScrollPane(p4=new JPanel());
		p4.setLayout(new BoxLayout(p4,BoxLayout.PAGE_AXIS));
		p4.add(p3);
		p4.add(Box.createVerticalGlue());
		s.setColumnHeaderView(new JLabel(Language.tr("Editor.Caller.Forwarding.RatesToClientTypes")));
		s.setBorder(BorderFactory.createEmptyBorder(0,5,0,0));
		add(s,BorderLayout.CENTER);

		add(p2=new JPanel(new FlowLayout(FlowLayout.LEFT)),BorderLayout.SOUTH);
		p2.add(continueSpecialButton=new JButton(Language.tr("Editor.Caller.Forwarding.AgentTypeDependendForwarding")));
		continueSpecialButton.addActionListener(dialogElementListener);
		continueSpecialButton.setIcon(Images.EDITOR_CALLER.getIcon());
		p2.add(continueSpecialLabel=new JLabel());

		setContinueSpecialLabel();
	}

	@Override
	public String[] check(KeyEvent e) {
		String[] result=null;

		if (NumberTools.getProbability(continueProbability,true)==null) {
			if (result==null) result=new String[]{Language.tr("Editor.Caller.Error.ContinueProbability.Title"),String.format(Language.tr("Editor.Caller.Error.ContinueProbability.Info"),continueProbability.getText())};
		}
		for (int i=0;i<continueToType.length;i++) if ((!continueToType[i].getText().trim().isEmpty()) && (NumberTools.getNotNegativeDouble(continueToType[i],true)==null)) {
			if (result==null) result=new String[]{Language.tr("Editor.Caller.Error.ContinueCallerType.Title"),String.format(Language.tr("Editor.Caller.Error.ContinueCallerType.Info"),callerTypeNames[i],continueToType[i].getText())};
		}

		return result;
	}

	@Override
	public void writeToCaller(CallcenterModelCaller newCaller) {
		newCaller.continueProbability=NumberTools.getProbability(continueProbability,false);
		newCaller.continueTypeName.clear();
		newCaller.continueTypeRate.clear();
		for (int i=0;i<continueToType.length;i++) {
			if (continueToType[i].getText().trim().isEmpty()) continue;
			Double d=NumberTools.getNotNegativeDouble(continueToType[i],false);
			newCaller.continueTypeName.add(callerTypeNames[i]);
			newCaller.continueTypeRate.add(d);
		}

		/* Skill-Level-abhängige Weiterleitungen */
		newCaller.continueTypeSkillType.clear();
		for (int i=0;i<continueTypeSkillType.size();i++) newCaller.continueTypeSkillType.add(continueTypeSkillType.get(i));
		newCaller.continueTypeSkillTypeProbability.clear();
		for (int i=0;i<continueTypeSkillTypeProbability.size();i++) newCaller.continueTypeSkillTypeProbability.add(continueTypeSkillTypeProbability.get(i));
		newCaller.continueTypeSkillTypeName.clear();
		for (int i=0;i<continueTypeSkillTypeName.size();i++) {
			List<String> list1=continueTypeSkillTypeName.get(i);
			List<String> list2=new ArrayList<String>(); newCaller.continueTypeSkillTypeName.add(list2);
			for (int j=0;j<list1.size();j++) list2.add(list1.get(j));
		}
		newCaller.continueTypeSkillTypeRate.clear();
		for (int i=0;i<continueTypeSkillTypeRate.size();i++) {
			List<Double> list1=continueTypeSkillTypeRate.get(i);
			List<Double> list2=new ArrayList<Double>(); newCaller.continueTypeSkillTypeRate.add(list2);
			for (int j=0;j<list1.size();j++) list2.add(list1.get(j));
		}
	}

	@Override
	public String getTabName() {
		return Language.tr("Editor.Caller.Tabs.Forwarding");
	}

	@Override
	public Icon getTabIconObject() {
		return Images.EDITOR_CALLER_PAGE_FORWARDING.getIcon();
	}

	private void setContinueSpecialLabel() {
		continueSpecialLabel.setText(
				(continueTypeSkillType.size()>0)?Language.tr("Editor.Caller.Forwarding.AgentSpecific.Active"):Language.tr("Editor.Caller.Forwarding.AgentSpecific.NotActive")
				);
	}

	@Override
	protected void processDialogEvents(ActionEvent e) {
		if (e.getSource()==continueSpecialButton) {
			if (skills.length==0) {
				MsgBox.error(parent,Language.tr("Editor.Caller.Error.NoSkillForwarding.Title"),Language.tr("Editor.Caller.Error.NoSkillForwarding.Info"));
				return;
			}
			CallerSpecialEditDialog specialDialog=new CallerSpecialEditDialog(parent,callerTypeNames,skills,continueTypeSkillType,continueTypeSkillTypeProbability,continueTypeSkillTypeName,continueTypeSkillTypeRate,readOnly,helpCallback,CallerSpecialEditDialog.Mode.MODE_CONTINUE);
			specialDialog.setVisible(true);
			setContinueSpecialLabel();
			return;
		}
	}

	@Override
	public void nameChange(String newName) {
		if (continueToThisType!=null) continueToThisType.setText(newName);
	}
}
