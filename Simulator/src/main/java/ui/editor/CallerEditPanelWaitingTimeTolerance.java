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
import java.awt.GridLayout;
import java.awt.event.ActionEvent;
import java.awt.event.KeyEvent;

import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.Icon;
import javax.swing.JCheckBox;
import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTextField;

import language.Language;
import mathtools.NumberTools;
import mathtools.TimeTools;
import mathtools.distribution.swing.JDistributionPanel;
import tools.SetupData;
import ui.images.Images;
import ui.model.CallcenterModelCaller;

/**
 * In diesem Panel kann die Wartezeittoleranz für eine Kundengruppe eingestellt werden.
 * @author Alexander Herzog
 * @see CallerEditDialog
 */
public class CallerEditPanelWaitingTimeTolerance extends CallerEditPanel {
	private static final long serialVersionUID = 9036391085696026977L;

	private final JCheckBox waitingTimeDistActive;
	private final JComboBox<String> waitingTimeCalc;
	private final JPanel waitingTimePanel;
	private final JDistributionPanel waitingTimeDist;
	private final JDistributionPanel waitingTimeDistLong;
	private final JTextField waitingTimeCalcMeanWaitingTime;
	private final JTextField waitingTimeCalcCancelProbability;
	private final JTextField waitingTimeCalcAdd;
	private final JLabel waitingTimeCalcResultMean;
	private final JLabel waitingTimeCalcResultSd;

	private static final String waitingTimePanel_Tab1="Verteilung (kurz)";
	private static final String waitingTimePanel_Tab2="Verteilung (lang)";
	private static final String waitingTimePanel_Tab3="Schaetzung";

	/**
	 * Konstruktor der Klasse
	 * @param initData	Informamtionen über den aktuellen Kundentyp zur Initialisierung des Panels
	 * @see CallerEditPanel.InitData
	 */
	public CallerEditPanelWaitingTimeTolerance(final InitData initData) {
		super(initData);

		JPanel p2,p3,p4;

		setLayout(new BorderLayout());

		add(p2=new JPanel(),BorderLayout.NORTH);
		p2.setLayout(new BoxLayout(p2,BoxLayout.Y_AXIS));
		p2.setBorder(BorderFactory.createEmptyBorder(5,0,5,0));
		p2.add(p3=new JPanel(new FlowLayout(FlowLayout.LEFT)));
		p3.add(waitingTimeDistActive=new JCheckBox(Language.tr("Editor.Caller.WaitingTimeTolerance.Finite"),caller.waitingTimeMode!=CallcenterModelCaller.WAITING_TIME_MODE_OFF));
		waitingTimeDistActive.setEnabled(!readOnly);
		p3.add(Box.createHorizontalStrut(20));
		p3.add(waitingTimeCalc=new JComboBox<String>(new String[]{
				Language.tr("Editor.Caller.WaitingTimeTolerance.Mode.Distribution"),
				Language.tr("Editor.Caller.WaitingTimeTolerance.Mode.DistributionLong"),
				Language.tr("Editor.Caller.WaitingTimeTolerance.Mode.Estimation")
		}));
		waitingTimeCalc.setEnabled(!readOnly);
		waitingTimeCalc.addActionListener(dialogElementListener);

		add(waitingTimePanel=new JPanel(new CardLayout()));

		/* Wartezeittoleranz direkt */
		waitingTimePanel.add(waitingTimeDist=new JDistributionPanel(caller.waitingTimeDist,CallcenterModelCaller.waitingTimeDistMaxX,true),waitingTimePanel_Tab1);
		waitingTimeDist.setAllowChangeDistributionData(!readOnly);
		waitingTimeDist.setImageSaveSize(SetupData.getSetup().imageSize);

		/* Wartezeittoleranz direkt (lang) */
		waitingTimePanel.add(waitingTimeDistLong=new JDistributionPanel(caller.waitingTimeDistLong,CallcenterModelCaller.waitingTimeDistLongMaxX,true),waitingTimePanel_Tab2);
		waitingTimeDistLong.setAllowChangeDistributionData(!readOnly);
		waitingTimeDistLong.setImageSaveSize(SetupData.getSetup().imageSize);

		/* Wartezeittoleranz schätzen */
		waitingTimePanel.add(p4=new JPanel(new FlowLayout(FlowLayout.LEFT)),waitingTimePanel_Tab3);
		p4.add(p2=new JPanel());
		p2.setLayout(new BoxLayout(p2,BoxLayout.Y_AXIS));

		p2.add(p3=new JPanel(new GridLayout(6,1)));
		p3.setBorder(BorderFactory.createTitledBorder(Language.tr("Editor.Caller.WaitingTimeTolerance.Estimation.Title")));
		waitingTimeCalcMeanWaitingTime=addInputLine(p3,Language.tr("Editor.Caller.WaitingTimeTolerance.Estimation.AverageWaitingTime"),TimeTools.formatExactTime(caller.waitingTimeCalcMeanWaitingTime),15);
		waitingTimeCalcMeanWaitingTime.setEnabled(!readOnly);
		waitingTimeCalcMeanWaitingTime.addKeyListener(dialogElementListener);
		waitingTimeCalcCancelProbability=addInputLine(p3,Language.tr("Editor.Caller.WaitingTimeTolerance.Estimation.CancelRate"),NumberTools.formatNumberMax(caller.waitingTimeCalcCancelProbability*100)+"%",15);
		waitingTimeCalcCancelProbability.setEnabled(!readOnly);
		waitingTimeCalcCancelProbability.addKeyListener(dialogElementListener);
		waitingTimeCalcAdd=addInputLine(p3,Language.tr("Editor.Caller.WaitingTimeTolerance.Estimation.CorrectionValue"),TimeTools.formatExactTime(caller.waitingTimeCalcAdd),15);
		waitingTimeCalcAdd.setEnabled(!readOnly);
		waitingTimeCalcAdd.addKeyListener(dialogElementListener);

		p2.add(Box.createVerticalStrut(20));

		p2.add(p3=new JPanel());
		p3.setLayout(new BoxLayout(p3,BoxLayout.Y_AXIS));
		p3.setAlignmentX(Component.RIGHT_ALIGNMENT);
		p3.setBorder(BorderFactory.createTitledBorder(Language.tr("Editor.Caller.WaitingTimeTolerance.Estimation.Result")));
		p3.add(waitingTimeCalcResultMean=new JLabel(Language.tr("Distribution.Mean")+"="));
		waitingTimeCalcResultMean.setBorder(BorderFactory.createEmptyBorder(10,0,10,10));
		p3.add(waitingTimeCalcResultSd=new JLabel(Language.tr("Distribution.StdDev")+"="));
		waitingTimeCalcResultSd.setBorder(BorderFactory.createEmptyBorder(0,0,10,10));
		updateWaitingTimeCalc();

		p2.add(Box.createVerticalGlue());
		switch (caller.waitingTimeMode) {
		case CallcenterModelCaller.WAITING_TIME_MODE_OFF: waitingTimeCalc.setSelectedIndex(0); break;
		case CallcenterModelCaller.WAITING_TIME_MODE_SHORT: waitingTimeCalc.setSelectedIndex(0); break;
		case CallcenterModelCaller.WAITING_TIME_MODE_LONG: waitingTimeCalc.setSelectedIndex(1); break;
		case CallcenterModelCaller.WAITING_TIME_MODE_CALC: waitingTimeCalc.setSelectedIndex(2); break;
		default: waitingTimeCalc.setSelectedIndex(0); break;
		}
	}

	@Override
	public String[] check(KeyEvent e) {
		String[] result=null;

		if (waitingTimeCalc.getSelectedIndex()==2) {
			if (TimeTools.getExactTime(waitingTimeCalcMeanWaitingTime,true)==null) {
				if (result==null) result=new String[]{Language.tr("Editor.Caller.Error.WaitingTimeCalcMeanWaitingTime.Title"),String.format(Language.tr("Editor.Caller.Error.WaitingTimeCalcMeanWaitingTime.Info"),waitingTimeCalcMeanWaitingTime.getText())};
			}
			if (NumberTools.getProbability(waitingTimeCalcCancelProbability,true)==null) {
				if (result==null) result=new String[]{Language.tr("Editor.Caller.Error.WaitingTimeCalcCancelProbability.Title"),String.format(Language.tr("Editor.Caller.Error.WaitingTimeCalcCancelProbability.Info"),waitingTimeCalcCancelProbability.getText())};
			}
			if (TimeTools.getExactTime(waitingTimeCalcAdd,true)==null) {
				if (result==null) result=new String[]{Language.tr("Editor.Caller.Error.WaitingTimeCalcAdd.Title"),String.format(Language.tr("Editor.Caller.Error.WaitingTimeCalcAdd.Info"),waitingTimeCalcAdd.getText())};
			}
		}

		updateWaitingTimeCalc();

		return result;
	}

	@Override
	public void writeToCaller(CallcenterModelCaller newCaller) {
		if (waitingTimeDistActive.isSelected()) {
			switch (waitingTimeCalc.getSelectedIndex()) {
			case 0:
				newCaller.waitingTimeMode=CallcenterModelCaller.WAITING_TIME_MODE_SHORT;
				newCaller.waitingTimeDist=waitingTimeDist.getDistribution();
				break;
			case 1:
				newCaller.waitingTimeMode=CallcenterModelCaller.WAITING_TIME_MODE_LONG;
				newCaller.waitingTimeDistLong=waitingTimeDistLong.getDistribution();
				break;
			case 2:
				newCaller.waitingTimeMode=CallcenterModelCaller.WAITING_TIME_MODE_CALC;
				newCaller.waitingTimeCalcMeanWaitingTime=TimeTools.getExactTime(waitingTimeCalcMeanWaitingTime,false);
				newCaller.waitingTimeCalcCancelProbability=NumberTools.getProbability(waitingTimeCalcCancelProbability,false);
				newCaller.waitingTimeCalcAdd=TimeTools.getExactTime(waitingTimeCalcAdd,false);
				break;
			}
		} else {
			newCaller.waitingTimeMode=CallcenterModelCaller.WAITING_TIME_MODE_OFF;
		}
	}

	@Override
	public String getTabName() {
		return Language.tr("Editor.Caller.Tabs.WaitingTimeTolerance");
	}

	@Override
	public Icon getTabIconObject() {
		return Images.EDITOR_CALLER_PAGE_WAITING_TIME_TOLERANCE.getIcon();
	}

	private void setActiveCard() {
		String s=null;
		switch (waitingTimeCalc.getSelectedIndex()) {
		case 0: s=waitingTimePanel_Tab1; break;
		case 1: s=waitingTimePanel_Tab2; break;
		case 2: s=waitingTimePanel_Tab3; break;
		}
		if (s!=null) ((CardLayout)waitingTimePanel.getLayout()).show(waitingTimePanel,s);
	}

	private void updateWaitingTimeCalc() {
		Double EW=TimeTools.getExactTime(waitingTimeCalcMeanWaitingTime,true);
		Double PA=NumberTools.getProbability(waitingTimeCalcCancelProbability,true);
		Double Add=TimeTools.getExactTime(waitingTimeCalcAdd,true);
		if (EW==null || PA==null || Add==null) {
			waitingTimeCalcResultMean.setText(Language.tr("Distribution.Mean")+"="+Language.tr("Editor.Caller.WaitingTimeTolerance.Estimation.Error"));
			waitingTimeCalcResultSd.setText(Language.tr("Distribution.StdDev")+"="+Language.tr("Editor.Caller.WaitingTimeTolerance.Estimation.Error"));
		} else {
			double mean=EW/PA;
			double sd=mean;
			mean=Math.max(1,mean+Add);
			if (mean==Double.POSITIVE_INFINITY) {
				waitingTimeCalcResultMean.setText(Language.tr("Distribution.Mean")+"="+Language.tr("Editor.Caller.WaitingTimeTolerance.Estimation.Error"));
				waitingTimeCalcResultSd.setText(Language.tr("Distribution.StdDev")+"="+Language.tr("Editor.Caller.WaitingTimeTolerance.Estimation.Error"));
			} else {
				waitingTimeCalcResultMean.setText(Language.tr("Distribution.Mean")+"="+TimeTools.formatExactTime(mean));
				waitingTimeCalcResultSd.setText(Language.tr("Distribution.StdDev")+"="+TimeTools.formatExactTime(sd));
			}
		}
	}

	@Override
	protected void processDialogEvents(ActionEvent e) {
		if (e.getSource()==waitingTimeCalc) {
			setActiveCard();
			return;
		}

		if (e.getSource()==waitingTimeCalcMeanWaitingTime || e.getSource()==waitingTimeCalcCancelProbability || e.getSource()==waitingTimeCalcAdd) {
			updateWaitingTimeCalc();
			return;
		}
	}
}
