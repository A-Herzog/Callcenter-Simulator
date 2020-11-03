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
import java.awt.Window;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.DefaultComboBoxModel;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTextField;

import language.Language;
import mathtools.NumberTools;
import mathtools.TimeTools;
import mathtools.distribution.DataDistributionImpl;
import systemtools.MsgBox;
import ui.images.Images;
import ui.model.CallcenterModel;
import ui.model.CallcenterModelCallcenter;
import ui.model.CallcenterModelCaller;
import ui.model.CallcenterModelWarnings;

/**
 * Ermöglicht das Bearbeiten eines einzelnen Schwellenwertes
 * @author Alexander Herzog
 * @version 1.0
 */
public class CallcenterThresholdValueEditDialog extends BaseEditDialog {
	/**
	 * Serialisierungs-ID der Klasse
	 * @see Serializable
	 */
	private static final long serialVersionUID = 2534499825234284466L;

	private DataDistributionImpl intervals;
	private final CallcenterModelWarnings.WarningRecord record;

	private final String[] groupsClients;
	private final String[] groupsCallcenters;

	private JComboBox<String> selectType;
	private JComboBox<String> selectTime;
	private JButton timeButton;
	private JLabel timeLabel;
	private JComboBox<String> selectGroups;
	private JComboBox<String> selectGroupName;
	private DefaultComboBoxModel<String> selectGroupNameModel;
	private boolean justSettingGroup=false;
	private JTextField warningYellow;
	private JTextField warningRed;

	private String groupNameClients;
	private String groupNameAgents;

	private String warningYellowTime;
	private String warningYellowPercent;
	private String warningRedTime;
	private String warningRedPercent;

	/**
	 * Konstruktor der Klasse
	 * @param owner	Übergeordnetes Fenster
	 * @param model	Gesamtes Callcenter-Modell
	 * @param record	Zu bearbeitende Schwellenwert-Warnung
	 * @param readOnly	Nur-Lese-Status
	 * @param helpCallback	Hilfe-Callback
	 */
	public CallcenterThresholdValueEditDialog(final Window owner, final CallcenterModel model, final CallcenterModelWarnings.WarningRecord record, final boolean readOnly, final Runnable helpCallback) {
		super(owner,Language.tr("Editor.GeneralData.ThresholdValues.Edit"),readOnly,helpCallback);

		List<String> groups;

		/* Daten vorbereiten */
		groups=new ArrayList<String>();
		for (CallcenterModelCaller caller : model.caller) if (caller.active) groups.add(caller.name);
		groupsClients=groups.toArray(new String[0]);

		groups=new ArrayList<String>();
		for (CallcenterModelCallcenter callcenter : model.callcenter) if (callcenter.active) groups.add(callcenter.name);
		groupsCallcenters=groups.toArray(new String[0]);

		this.record=record;
		if (record.intervals!=null) intervals=record.intervals; else intervals=new DataDistributionImpl(CallcenterModelCaller.freshCallsDistMaxX,48);

		/* GUI aufbauen */
		createSimpleGUI(500,500,null,null);
		pack();

		/* Daten in GUI laden */
		selectType.setSelectedIndex(Math.max(0,Math.min(selectType.getItemCount()-1,record.type.id)));
		selectTime.setSelectedIndex(0);
		switch (record.modeTime) {
		case WARNING_MODE_AVERAGE: selectTime.setSelectedIndex(0); break;
		case WARNING_MODE_EACH: selectTime.setSelectedIndex(1); break;
		case WARNING_MODE_SELECTED: selectTime.setSelectedIndex(2); break;
		}
		updateIntervalsLabel();

		selectGroups.setSelectedIndex(0);
		switch (record.modeGroup) {
		case WARNING_MODE_AVERAGE: selectGroups.setSelectedIndex(0); break;
		case WARNING_MODE_EACH: selectGroups.setSelectedIndex(1); break;
		case WARNING_MODE_SELECTED: selectGroups.setSelectedIndex(2); break;
		}

		if (record.type==CallcenterModelWarnings.WarningType.WARNING_TYPE_WORKLOAD) {
			groupNameClients="";
			groupNameAgents=record.group;
		} else {
			groupNameClients=record.group;
			groupNameAgents="";
		}
		setGroupNames();

		warningYellowTime="0:00:30";
		warningYellowPercent="85%";
		warningRedTime="0:00:60";
		warningRedPercent="80%";
		if (record.type==CallcenterModelWarnings.WarningType.WARNING_TYPE_WAITINGTIME_CALL || record.type==CallcenterModelWarnings.WarningType.WARNING_TYPE_WAITINGTIME_CLIENT || record.type==CallcenterModelWarnings.WarningType.WARNING_TYPE_RESIDENCETIME_CALL || record.type==CallcenterModelWarnings.WarningType.WARNING_TYPE_RESIDENCETIME_CLIENT) {
			if (record.warningYellow>=0) warningYellowTime=TimeTools.formatTime(Math.round(record.warningYellow));
			warningYellow.setText(warningYellowTime);
			if(record.warningRed>=0) warningRedTime=TimeTools.formatTime(Math.round(record.warningRed));
			warningRed.setText(warningRedTime);
		} else {
			if (record.warningYellow>=0) warningYellowPercent=NumberTools.formatPercent(record.warningYellow);
			warningYellow.setText(warningYellowPercent);
			if(record.warningRed>=0) warningRedPercent=NumberTools.formatPercent(record.warningRed);
			warningRed.setText(warningRedPercent);
		}

		if (readOnly) {
			selectType.setEnabled(false);
			selectTime.setEnabled(false);
			timeButton.setEnabled(false);
			selectGroups.setEnabled(false);
			selectGroupName.setEnabled(false);
			warningYellow.setEditable(false);
			warningYellow.setEnabled(false);
			warningRed.setEditable(false);
			warningRed.setEnabled(false);
		}
	}

	private void addElement(JPanel p, JComponent child) {
		JPanel subPanel;
		p.add(subPanel=new JPanel(new FlowLayout(FlowLayout.LEFT,0,0)));
		subPanel.setBorder(BorderFactory.createEmptyBorder(0,0,0,0));
		subPanel.add(child);

	}

	private void addElement(JPanel p, JComponent[] childs, int horizontalStrut) {
		JPanel subPanel;
		p.add(subPanel=new JPanel(new FlowLayout(FlowLayout.LEFT,0,0)));
		subPanel.setBorder(BorderFactory.createEmptyBorder(0,0,0,0));
		for (JComponent child : childs)	{
			subPanel.add(child);
			subPanel.add(Box.createHorizontalStrut(horizontalStrut));
		}
	}

	private void addLabel(JPanel p, String name) {
		addElement(p,new JLabel(name));
	}

	/**
	 * Diese Funktion fügt ein Textfeld inkl. Beschreibung zu einem Panel hinzu.
	 * Das Panel sollte dabei den Layout-Typ GridLayout(2n,1) besitzen.
	 * @param p	Panel, in das das Eingabefeld eingefügt werden soll
	 * @param name	Beschriftung für das Textfeld
	 * @param initialValue	Anfänglicher Wert für das Textfeld
	 * @param width	Anzahl an anzuzeigenden Spalten
	 * @param additionalInfo	Optionaler zusätzlicher Text hinter dem Eingabefeld (kann <code>null</code> sein)
	 * @return	Referenz auf das neu erzeugte Textfeld
	 */
	private JTextField addInputLine(JPanel p, String name, String initialValue, int width, String additionalInfo) {
		JPanel subPanel,p2;

		p.add(subPanel=new JPanel());
		subPanel.setLayout(new BoxLayout(subPanel,BoxLayout.X_AXIS));
		subPanel.add(new JLabel(name));
		subPanel.add(Box.createHorizontalGlue());

		JTextField text;
		p.add(subPanel=new JPanel());
		subPanel.setLayout(new BoxLayout(subPanel,BoxLayout.X_AXIS));
		subPanel.add(p2=new JPanel(new FlowLayout(FlowLayout.LEFT,0,0)));
		p2.setBorder(BorderFactory.createEmptyBorder(0,0,5,0));
		p2.add(text=new JTextField(initialValue,width));

		if (additionalInfo!=null && !additionalInfo.isEmpty()) {
			p2.add(Box.createHorizontalStrut(10));
			p2.add(new JLabel(additionalInfo));
		}
		subPanel.add(Box.createHorizontalGlue());

		p.add(Box.createVerticalStrut(5));

		return text;
	}

	@Override
	protected void createSimpleContent(JPanel content) {
		JPanel p;

		content.setLayout(new BorderLayout());
		content.add(p=new JPanel(),BorderLayout.CENTER);

		List<String> types;

		p.setLayout(new BoxLayout(p,BoxLayout.Y_AXIS));

		addLabel(p,Language.tr("Editor.GeneralData.ThresholdValues.Type"));
		types=new ArrayList<String>();
		types.add(Language.tr("Editor.GeneralData.ThresholdValues.Type.WaitingTimeCalls"));
		types.add(Language.tr("Editor.GeneralData.ThresholdValues.Type.WaitingTimeClients"));
		types.add(Language.tr("Editor.GeneralData.ThresholdValues.Type.ResidenceTimeCalls"));
		types.add(Language.tr("Editor.GeneralData.ThresholdValues.Type.ResidenceTimeClients"));
		types.add(Language.tr("Editor.GeneralData.ThresholdValues.Type.SuccessPartCalls"));
		types.add(Language.tr("Editor.GeneralData.ThresholdValues.Type.SuccessPartClients"));
		types.add(Language.tr("Editor.GeneralData.ThresholdValues.Type.ServiceLevelOnSuccessfulCalls"));
		types.add(Language.tr("Editor.GeneralData.ThresholdValues.Type.ServiceLevelOnSuccessfulClients"));
		types.add(Language.tr("Editor.GeneralData.ThresholdValues.Type.ServiceLevelOnAllCalls"));
		types.add(Language.tr("Editor.GeneralData.ThresholdValues.Type.ServiceLevelOnAllClients"));
		types.add(Language.tr("Editor.GeneralData.ThresholdValues.Type.Workload"));
		addElement(p,selectType=new JComboBox<String>(types.toArray(new String[0])));
		selectType.addActionListener(new DialogListener());

		p.add(Box.createVerticalStrut(20));

		addLabel(p,Language.tr("Editor.GeneralData.ThresholdValues.ModeTime"));
		types=new ArrayList<String>();
		types.add(Language.tr("Editor.GeneralData.ThresholdValues.ModeTime.Average"));
		types.add(Language.tr("Editor.GeneralData.ThresholdValues.ModeTime.Each"));
		types.add(Language.tr("Editor.GeneralData.ThresholdValues.ModeTime.Intervals"));
		addElement(p,selectTime=new JComboBox<String>(types.toArray(new String[0])));
		selectTime.addActionListener(new DialogListener());

		p.add(Box.createVerticalStrut(5));

		JComponent[] childs={timeButton=new JButton(Language.tr("Editor.GeneralData.ThresholdValues.ModeTime.Intervals.Button")),timeLabel=new JLabel()};
		addElement(p,childs,5);

		timeButton.setIcon(Images.EDITOR_THRESHOLD_TIME.getIcon());
		timeButton.addActionListener(new DialogListener());

		p.add(Box.createVerticalStrut(20));

		addLabel(p,Language.tr("Editor.GeneralData.ThresholdValues.ModeGroups"));
		types=new ArrayList<String>();
		types.add(Language.tr("Editor.GeneralData.ThresholdValues.ModeGroups.Average"));
		types.add(Language.tr("Editor.GeneralData.ThresholdValues.ModeGroups.Each"));
		types.add(Language.tr("Editor.GeneralData.ThresholdValues.ModeGroups.Group"));
		addElement(p,selectGroups=new JComboBox<String>(types.toArray(new String[0])));
		selectGroups.addActionListener(new DialogListener());

		p.add(Box.createVerticalStrut(5));

		selectGroupNameModel=new DefaultComboBoxModel<String>();
		addElement(p,selectGroupName=new JComboBox<String>(selectGroupNameModel));
		selectGroupName.addActionListener(new DialogListener());

		p.add(Box.createVerticalStrut(20));

		warningYellow=addInputLine(p,Language.tr("Editor.GeneralData.ThresholdValues.warningYellow"),"",8,null);
		warningYellow.addKeyListener(new DialogListener());
		warningYellow.addActionListener(new DialogListener());
		warningRed=addInputLine(p,Language.tr("Editor.GeneralData.ThresholdValues.warningRed"),"",8,null);
		warningRed.addKeyListener(new DialogListener());
		warningRed.addActionListener(new DialogListener());
	}

	private void setGroupNames() {
		justSettingGroup=true;
		selectGroupNameModel.removeAllElements();
		int index=0;
		if (selectType.getSelectedIndex()==CallcenterModelWarnings.WarningType.WARNING_TYPE_WORKLOAD.id) {
			for (int i=0;i<groupsCallcenters.length;i++) {
				selectGroupNameModel.addElement(groupsCallcenters[i]);
				if (groupsCallcenters[i].equalsIgnoreCase(groupNameAgents)) index=i;
			}
		} else {
			for (int i=0;i<groupsClients.length;i++) {
				selectGroupNameModel.addElement(groupsClients[i]);
				if (groupsClients[i].equalsIgnoreCase(groupNameClients)) index=i;
			}
		}
		selectGroupName.setSelectedIndex(index);
		justSettingGroup=false;
	}

	private boolean checkWarningValues(boolean message) {
		CallcenterModelWarnings.WarningType type=CallcenterModelWarnings.WarningType.WARNING_TYPE_WAITINGTIME_CALL;
		switch (selectType.getSelectedIndex()) {
		case 0: type=CallcenterModelWarnings.WarningType.WARNING_TYPE_WAITINGTIME_CALL; break;
		case 1: type=CallcenterModelWarnings.WarningType.WARNING_TYPE_WAITINGTIME_CLIENT; break;
		case 2: type=CallcenterModelWarnings.WarningType.WARNING_TYPE_RESIDENCETIME_CALL; break;
		case 3: type=CallcenterModelWarnings.WarningType.WARNING_TYPE_RESIDENCETIME_CLIENT; break;
		case 4: type=CallcenterModelWarnings.WarningType.WARNING_TYPE_SUCCESSPART_CALL; break;
		case 5: type=CallcenterModelWarnings.WarningType.WARNING_TYPE_SUCCESSPART_CLIENT; break;
		case 6: type=CallcenterModelWarnings.WarningType.WARNING_TYPE_SERVICELEVEL_CALL_SUCCESSFUL; break;
		case 7: type=CallcenterModelWarnings.WarningType.WARNING_TYPE_SERVICELEVEL_CLIENTS_SUCCESSFUL; break;
		case 8: type=CallcenterModelWarnings.WarningType.WARNING_TYPE_SERVICELEVEL_CALL_ALL; break;
		case 9: type=CallcenterModelWarnings.WarningType.WARNING_TYPE_SERVICELEVEL_CLIENTS_ALL; break;
		case 10: type=CallcenterModelWarnings.WarningType.WARNING_TYPE_WORKLOAD; break;
		}

		if (type==CallcenterModelWarnings.WarningType.WARNING_TYPE_WAITINGTIME_CALL || type==CallcenterModelWarnings.WarningType.WARNING_TYPE_WAITINGTIME_CLIENT || type==CallcenterModelWarnings.WarningType.WARNING_TYPE_RESIDENCETIME_CALL || type==CallcenterModelWarnings.WarningType.WARNING_TYPE_RESIDENCETIME_CLIENT) {
			warningYellowTime=warningYellow.getText();
			warningRedTime=warningRed.getText();
			Integer I;
			I=TimeTools.getTime(warningYellow,true);
			if (I==null || I<0) {
				if (message) MsgBox.error(this,Language.tr("Editor.GeneralData.ThresholdValues.warningYellow.InvalidValue"),Language.tr("Editor.GeneralData.ThresholdValues.warningYellow.InvalidValue.TimeNeeded"));
				return false;
			}
			I=TimeTools.getTime(warningRed,true);
			if (I==null || I<0) {
				if (message) MsgBox.error(this,Language.tr("Editor.GeneralData.ThresholdValues.warningRed.InvalidValue"),Language.tr("Editor.GeneralData.ThresholdValues.warningRed.InvalidValue.TimeNeeded"));
				return false;
			}
		} else {
			warningYellowPercent=warningYellow.getText();
			warningRedPercent=warningRed.getText();
			Double D;
			D=NumberTools.getExtProbability(warningYellow,true);
			if (D==null || D<0 || D>1) {
				if (message) MsgBox.error(this,Language.tr("Editor.GeneralData.ThresholdValues.warningYellow.InvalidValue"),Language.tr("Editor.GeneralData.ThresholdValues.warningYellow.InvalidValue.PercentNeeded"));
				return false;
			}
			D=NumberTools.getExtProbability(warningRed,true);
			if (D==null || D<0 || D>1) {
				if (message) MsgBox.error(this,Language.tr("Editor.GeneralData.ThresholdValues.warningRed.InvalidValue"),Language.tr("Editor.GeneralData.ThresholdValues.warningRed.InvalidValue.PercentNeeded"));
				return false;
			}
		}
		return true;
	}

	private void updateIntervalsLabel() {
		timeLabel.setVisible(selectTime.getSelectedIndex()==CallcenterModelWarnings.WarningMode.WARNING_MODE_SELECTED.id);
		long count=Math.round(intervals.sum());
		if (count==0) {
			timeLabel.setText(Language.tr("Editor.GeneralData.ThresholdValues.ModeTime.Intervals.None"));
			return;
		}
		if (count==48) {
			timeLabel.setText(Language.tr("Editor.GeneralData.ThresholdValues.ModeTime.Intervals.All"));
			return;
		}
		if (count==1) {
			Language.tr("Editor.GeneralData.ThresholdValues.ModeTime.Intervals.Some.One");
			return;
		}
		timeLabel.setText(String.format(Language.tr("Editor.GeneralData.ThresholdValues.ModeTime.Intervals.Some"),count));
	}

	@Override
	protected boolean checkData() {
		return checkWarningValues(true);
	}

	@Override
	protected void storeData() {
		record.type=CallcenterModelWarnings.WarningType.WARNING_TYPE_WAITINGTIME_CALL;
		switch (selectType.getSelectedIndex()) {
		case 0: record.type=CallcenterModelWarnings.WarningType.WARNING_TYPE_WAITINGTIME_CALL; break;
		case 1: record.type=CallcenterModelWarnings.WarningType.WARNING_TYPE_WAITINGTIME_CLIENT; break;
		case 2: record.type=CallcenterModelWarnings.WarningType.WARNING_TYPE_RESIDENCETIME_CALL; break;
		case 3: record.type=CallcenterModelWarnings.WarningType.WARNING_TYPE_RESIDENCETIME_CLIENT; break;
		case 4: record.type=CallcenterModelWarnings.WarningType.WARNING_TYPE_SUCCESSPART_CALL; break;
		case 5: record.type=CallcenterModelWarnings.WarningType.WARNING_TYPE_SUCCESSPART_CLIENT; break;
		case 6: record.type=CallcenterModelWarnings.WarningType.WARNING_TYPE_SERVICELEVEL_CALL_SUCCESSFUL; break;
		case 7: record.type=CallcenterModelWarnings.WarningType.WARNING_TYPE_SERVICELEVEL_CLIENTS_SUCCESSFUL; break;
		case 8: record.type=CallcenterModelWarnings.WarningType.WARNING_TYPE_SERVICELEVEL_CALL_ALL; break;
		case 9: record.type=CallcenterModelWarnings.WarningType.WARNING_TYPE_SERVICELEVEL_CLIENTS_ALL; break;
		case 10: record.type=CallcenterModelWarnings.WarningType.WARNING_TYPE_WORKLOAD; break;
		}

		switch (selectTime.getSelectedIndex()) {
		case 0:
			record.modeTime=CallcenterModelWarnings.WarningMode.WARNING_MODE_AVERAGE;
			record.intervals=null;
			break;
		case 1:
			record.modeTime=CallcenterModelWarnings.WarningMode.WARNING_MODE_EACH;
			record.intervals=null;
			break;
		case 2:
			record.modeTime=CallcenterModelWarnings.WarningMode.WARNING_MODE_SELECTED;
			record.intervals=intervals;
			break;
		}

		switch (selectGroups.getSelectedIndex()) {
		case 0:
			record.modeGroup=CallcenterModelWarnings.WarningMode.WARNING_MODE_AVERAGE;
			record.group="";
			break;
		case 1:
			record.modeGroup=CallcenterModelWarnings.WarningMode.WARNING_MODE_EACH;
			record.group="";
			break;
		case 2:
			record.modeGroup=CallcenterModelWarnings.WarningMode.WARNING_MODE_SELECTED;
			if (record.type==CallcenterModelWarnings.WarningType.WARNING_TYPE_WORKLOAD) record.group=groupNameAgents; else record.group=groupNameClients;
			break;
		}

		if (record.type==CallcenterModelWarnings.WarningType.WARNING_TYPE_WAITINGTIME_CALL || record.type==CallcenterModelWarnings.WarningType.WARNING_TYPE_WAITINGTIME_CLIENT || record.type==CallcenterModelWarnings.WarningType.WARNING_TYPE_RESIDENCETIME_CALL || record.type==CallcenterModelWarnings.WarningType.WARNING_TYPE_RESIDENCETIME_CLIENT) {
			Integer I;
			I=TimeTools.getTime(warningYellow,true);
			record.warningYellow=I;
			I=TimeTools.getTime(warningRed,true);
			record.warningRed=I;
		} else {
			Double D;
			D=NumberTools.getExtProbability(warningYellow,true);
			record.warningYellow=D;
			D=NumberTools.getExtProbability(warningRed,true);
			record.warningRed=D;
		}
	}

	private class DialogListener implements ActionListener, KeyListener {
		public void changed(Object obj) {
			if (obj==selectType) {

				CallcenterModelWarnings.WarningType type=CallcenterModelWarnings.WarningType.WARNING_TYPE_WAITINGTIME_CALL;
				switch (selectType.getSelectedIndex()) {
				case 0: type=CallcenterModelWarnings.WarningType.WARNING_TYPE_WAITINGTIME_CALL; break;
				case 1: type=CallcenterModelWarnings.WarningType.WARNING_TYPE_WAITINGTIME_CLIENT; break;
				case 2: type=CallcenterModelWarnings.WarningType.WARNING_TYPE_RESIDENCETIME_CALL; break;
				case 3: type=CallcenterModelWarnings.WarningType.WARNING_TYPE_RESIDENCETIME_CLIENT; break;
				case 4: type=CallcenterModelWarnings.WarningType.WARNING_TYPE_SUCCESSPART_CALL; break;
				case 5: type=CallcenterModelWarnings.WarningType.WARNING_TYPE_SUCCESSPART_CLIENT; break;
				case 6: type=CallcenterModelWarnings.WarningType.WARNING_TYPE_SERVICELEVEL_CALL_SUCCESSFUL; break;
				case 7: type=CallcenterModelWarnings.WarningType.WARNING_TYPE_SERVICELEVEL_CLIENTS_SUCCESSFUL; break;
				case 8: type=CallcenterModelWarnings.WarningType.WARNING_TYPE_SERVICELEVEL_CALL_ALL; break;
				case 9: type=CallcenterModelWarnings.WarningType.WARNING_TYPE_SERVICELEVEL_CLIENTS_ALL; break;
				case 10: type=CallcenterModelWarnings.WarningType.WARNING_TYPE_WORKLOAD; break;
				}

				if (type==CallcenterModelWarnings.WarningType.WARNING_TYPE_WAITINGTIME_CALL || type==CallcenterModelWarnings.WarningType.WARNING_TYPE_WAITINGTIME_CLIENT || type==CallcenterModelWarnings.WarningType.WARNING_TYPE_RESIDENCETIME_CALL || type==CallcenterModelWarnings.WarningType.WARNING_TYPE_RESIDENCETIME_CLIENT) {
					warningYellow.setText(warningYellowTime);
					warningRed.setText(warningRedTime);
				} else {
					warningYellow.setText(warningYellowPercent);
					warningRed.setText(warningRedPercent);
				}
				setGroupNames();
			}

			if (obj==selectTime) {
				timeButton.setEnabled(selectTime.getSelectedIndex()==CallcenterModelWarnings.WarningMode.WARNING_MODE_SELECTED.id);
				updateIntervalsLabel();
				return;
			}

			if (obj==timeButton) {
				CallcenterThresholdIntervalsDialog dialog=new CallcenterThresholdIntervalsDialog(CallcenterThresholdValueEditDialog.this,readOnly,intervals,helpCallback);
				dialog.setVisible(true);
				if (dialog.getClosedBy()!=BaseEditDialog.CLOSED_BY_OK) return;
				intervals=dialog.getIntervals();
				updateIntervalsLabel();
				return;
			}

			if (obj==selectGroups) {
				selectGroupName.setEnabled(selectGroups.getSelectedIndex()==CallcenterModelWarnings.WarningMode.WARNING_MODE_SELECTED.id);
				return;
			}

			if (obj==selectGroupName) {
				if (!justSettingGroup) {
					if (selectType.getSelectedIndex()==CallcenterModelWarnings.WarningType.WARNING_TYPE_WORKLOAD.id) {
						groupNameAgents=groupsCallcenters[selectGroupName.getSelectedIndex()];
					} else {
						groupNameClients=groupsClients[selectGroupName.getSelectedIndex()];
					}
				}
				return;
			}

			if (obj==warningYellow || obj==warningRed) {
				checkWarningValues(false);
				return;
			}
		}

		@Override
		public void keyTyped(KeyEvent e) {changed(e.getSource());}

		@Override
		public void keyPressed(KeyEvent e) {changed(e.getSource());}

		@Override
		public void keyReleased(KeyEvent e) {changed(e.getSource());}

		@Override
		public void actionPerformed(ActionEvent e) {changed(e.getSource());}
	}
}