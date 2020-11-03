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
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.io.Serializable;

import javax.swing.DefaultListModel;
import javax.swing.Icon;
import javax.swing.JButton;
import javax.swing.JList;
import javax.swing.JPanel;
import javax.swing.JScrollPane;

import language.Language;
import mathtools.NumberTools;
import mathtools.TimeTools;
import ui.HelpLink;
import ui.images.Images;
import ui.model.CallcenterModel;
import ui.model.CallcenterModelWarnings;

/**
 * Ermöglicht die Bearbeitung der Liste der Schwellenwerte
 * @author Alexander Herzog
 * @version 1.0
 */
public class CallcenterThresholdValuesDialog extends BaseEditDialog {
	/**
	 * Serialisierungs-ID der Klasse
	 * @see Serializable
	 */
	private static final long serialVersionUID = 8297314571875608691L;

	/** Schaltfläche "Hinzufügen" */
	private JButton buttonAdd;
	/** Schaltfläche "Bearbeiten" */
	private JButton buttonEdit;
	/** Schaltfläche "Löschen" */
	private JButton buttonDelete;

	private JList<CallcenterModelWarnings.WarningRecord> list;
	private DefaultListModel<CallcenterModelWarnings.WarningRecord> listData;

	/** Gesamtes Callcenter-Modell */
	private final CallcenterModel model;
	private final CallcenterModelWarnings warnings;

	/**
	 * Konstruktor der Klasse
	 * @param owner	Übergeordnetes Fenster
	 * @param model	Gesamtes Callcenter-Modell
	 * @param readOnly	Nur-Lese-Status
	 * @param helpLink	Hilfe-Link
	 */
	public CallcenterThresholdValuesDialog(final Window owner, final CallcenterModel model, final boolean readOnly, final HelpLink helpLink) {
		super(owner,Language.tr("Editor.GeneralData.ThresholdValues"),readOnly,helpLink.dialogThreshold);
		this.model=model;
		this.warnings=model.warnings.clone();
		createSimpleGUI(550,550,null,null);
		buildList();
	}

	@Override
	protected void createSimpleContent(JPanel content) {
		JPanel p;
		content.setLayout(new BorderLayout());

		content.add(p=new JPanel(new FlowLayout(FlowLayout.LEFT)),BorderLayout.NORTH);

		if (!readOnly) {
			p.add(buttonAdd=new JButton(Language.tr("Dialog.Button.Add")));
			buttonAdd.setIcon(Images.EDIT_ADD.getIcon());
			buttonAdd.addActionListener(new DialogElementListener());
		}

		p.add(buttonEdit=new JButton(readOnly?Language.tr("Dialog.Button.View"):Language.tr("Dialog.Button.Edit")));
		buttonEdit.setIcon(Images.GENERAL_TOOLS.getIcon());
		buttonEdit.addActionListener(new DialogElementListener());

		if (!readOnly) {
			p.add(buttonDelete=new JButton(Language.tr("Dialog.Button.Delete")));
			buttonDelete.setIcon(Images.EDIT_DELETE.getIcon());
			buttonDelete.addActionListener(new DialogElementListener());
		}

		content.add(new JScrollPane(list=new JList<CallcenterModelWarnings.WarningRecord>(listData=new DefaultListModel<CallcenterModelWarnings.WarningRecord>())),BorderLayout.CENTER);
		list.setCellRenderer(new WarningsListRenderer());
		list.addKeyListener(new DialogElementListener());
		list.addMouseListener(new DialogElementListener());
	}

	/**
	 * Liefert nach dem Schließen des Dialogs mit "Ok" das veränderte Modell.
	 * @return	Neues Callcenter-Modell
	 */
	public CallcenterModel getModel() {
		model.warnings=warnings;
		return model;
	}

	private void buildList() {
		listData.clear();
		for (CallcenterModelWarnings.WarningRecord record : warnings.records) listData.addElement(record);
	}

	private void warningAdd() {
		if (readOnly) return;
		CallcenterModelWarnings.WarningRecord record=new CallcenterModelWarnings.WarningRecord();
		CallcenterThresholdValueEditDialog dialog=new CallcenterThresholdValueEditDialog(getOwner(),model,record,readOnly,helpCallback);
		dialog.setVisible(true);
		if (dialog.getClosedBy()!=BaseEditDialog.CLOSED_BY_OK) return;
		warnings.records.add(record);
		buildList();
	}

	private void warningEdit() {
		if (list.getSelectedIndex()<0) return;
		int index=list.getSelectedIndex();
		CallcenterModelWarnings.WarningRecord record=warnings.records.get(index).clone();
		CallcenterThresholdValueEditDialog dialog=new CallcenterThresholdValueEditDialog(this,model,record,readOnly,helpCallback);
		dialog.setVisible(true);
		if (readOnly) return;
		if (dialog.getClosedBy()!=BaseEditDialog.CLOSED_BY_OK) return;
		warnings.records.set(index,record);
		buildList();
	}

	private void warningDelete() {
		if (readOnly) return;
		if (list.getSelectedIndex()<0) return;
		warnings.records.remove(list.getSelectedIndex());
		buildList();
	}

	private class DialogElementListener implements ActionListener, KeyListener, MouseListener {
		@Override
		public void actionPerformed(ActionEvent e) {
			if (e.getSource()==buttonAdd) {warningAdd(); return;}
			if (e.getSource()==buttonEdit) {warningEdit(); return;}
			if (e.getSource()==buttonDelete) {warningDelete(); return;}
		}

		@Override
		public void keyTyped(KeyEvent e) {}

		@Override
		public void keyPressed(KeyEvent e) {
			if (e.getKeyCode()==KeyEvent.VK_ENTER && e.getModifiersEx()==0) {warningEdit(); e.consume();}
			if (e.getKeyCode()==KeyEvent.VK_INSERT && e.getModifiersEx()==0) {warningAdd(); e.consume();}
			if (e.getKeyCode()==KeyEvent.VK_DELETE && e.getModifiersEx()==0) {warningDelete(); e.consume();}
		}

		@Override
		public void keyReleased(KeyEvent e) {}

		@Override
		public void mouseClicked(MouseEvent e) {if (e.getClickCount()==2) warningEdit();}

		@Override
		public void mousePressed(MouseEvent e) {}

		@Override
		public void mouseReleased(MouseEvent e) {}

		@Override
		public void mouseEntered(MouseEvent e) {}

		@Override
		public void mouseExited(MouseEvent e) {}
	}

	private class WarningsListRenderer extends AdvancedListCellRenderer {
		/**
		 * Serialisierungs-ID der Klasse
		 * @see Serializable
		 */
		private static final long serialVersionUID = 6668671715230234568L;

		@Override
		protected void buildString(Object value, int index, StringBuilder s) {
			if (!(value instanceof CallcenterModelWarnings.WarningRecord)) {
				s.append(index);
				return;
			}

			CallcenterModelWarnings.WarningRecord record=(CallcenterModelWarnings.WarningRecord)value;
			String type="";
			switch (record.type) {
			case WARNING_TYPE_WAITINGTIME_CALL: type=Language.tr("Editor.GeneralData.ThresholdValues.Type.WaitingTimeCalls"); break;
			case WARNING_TYPE_WAITINGTIME_CLIENT: type=Language.tr("Editor.GeneralData.ThresholdValues.Type.WaitingTimeClients"); break;
			case WARNING_TYPE_RESIDENCETIME_CALL: type=Language.tr("Editor.GeneralData.ThresholdValues.Type.ResidenceTimeCalls"); break;
			case WARNING_TYPE_RESIDENCETIME_CLIENT: type=Language.tr("Editor.GeneralData.ThresholdValues.Type.ResidenceTimeClients"); break;
			case WARNING_TYPE_SUCCESSPART_CALL: type=Language.tr("Editor.GeneralData.ThresholdValues.Type.SuccessPartCalls"); break;
			case WARNING_TYPE_SUCCESSPART_CLIENT: type=Language.tr("Editor.GeneralData.ThresholdValues.Type.SuccessPartClients"); break;
			case WARNING_TYPE_SERVICELEVEL_CALL_SUCCESSFUL: type=Language.tr("Editor.GeneralData.ThresholdValues.Type.ServiceLevelOnSuccessfulCalls"); break;
			case WARNING_TYPE_SERVICELEVEL_CLIENTS_SUCCESSFUL: type=Language.tr("Editor.GeneralData.ThresholdValues.Type.ServiceLevelOnSuccessfulClients"); break;
			case WARNING_TYPE_SERVICELEVEL_CALL_ALL: type=Language.tr("Editor.GeneralData.ThresholdValues.Type.ServiceLevelOnAllCalls"); break;
			case WARNING_TYPE_SERVICELEVEL_CLIENTS_ALL: type=Language.tr("Editor.GeneralData.ThresholdValues.Type.ServiceLevelOnAllClients"); break;
			case WARNING_TYPE_WORKLOAD: type=Language.tr("Editor.GeneralData.ThresholdValues.Type.Workload"); break;
			}
			s.append("<b>"+type+"</b><br>");
			switch (record.modeTime) {
			case WARNING_MODE_AVERAGE:
				s.append(Language.tr("Editor.GeneralData.ThresholdValues.ModeTime.Average")+"<br>");
				break;
			case WARNING_MODE_EACH:
				s.append(Language.tr("Editor.GeneralData.ThresholdValues.ModeTime.Each")+"<br>");
				break;
			case WARNING_MODE_SELECTED:
				s.append(Language.tr("Editor.GeneralData.ThresholdValues.ModeTime.Intervals.Info")+"<br>");
				break;
			}
			switch (record.modeGroup) {
			case WARNING_MODE_AVERAGE:
				s.append(Language.tr("Editor.GeneralData.ThresholdValues.ModeGroups.Average")+"<br>");
				break;
			case WARNING_MODE_EACH:
				s.append(Language.tr("Editor.GeneralData.ThresholdValues.ModeGroups.Each")+"<br>");
				break;
			case WARNING_MODE_SELECTED:
				String group=record.group;
				if (record.type==CallcenterModelWarnings.WarningType.WARNING_TYPE_WORKLOAD) {
					int i=group.indexOf('-');
					if (i>0) {
						group=group.substring(i+1)+" - "+Language.tr("Editor.Callcenter.List.AgentGroup")+" "+group.substring(0,i).trim();
					}
				}
				s.append(Language.tr("Editor.GeneralData.ThresholdValues.ModeGroups.Group.Info")+": "+group+"<br>");
				break;
			}

			String warningYellow="";
			String warningRed="";
			if (record.type==CallcenterModelWarnings.WarningType.WARNING_TYPE_WAITINGTIME_CALL || record.type==CallcenterModelWarnings.WarningType.WARNING_TYPE_WAITINGTIME_CLIENT || record.type==CallcenterModelWarnings.WarningType.WARNING_TYPE_RESIDENCETIME_CALL || record.type==CallcenterModelWarnings.WarningType.WARNING_TYPE_RESIDENCETIME_CLIENT) {
				warningYellow=TimeTools.formatTime(Math.round(record.warningYellow));
				warningRed=TimeTools.formatTime(Math.round(record.warningRed));
			} else {
				warningYellow=NumberTools.formatPercent(record.warningYellow);
				warningRed=NumberTools.formatPercent(record.warningRed);
			}
			s.append("<span style='color: orange'>"+Language.tr("Editor.GeneralData.ThresholdValues.warningYellow")+": <b>"+warningYellow+"</b></span><br>");
			s.append("<span style='color: red'>"+Language.tr("Editor.GeneralData.ThresholdValues.warningRed")+": <b>"+warningRed+"</b></span>");
		}

		@Override
		protected Icon getIcon(Object value) {
			return Images.EDITOR_THRESHOLD_BIG.getIcon();
		}
	}
}
