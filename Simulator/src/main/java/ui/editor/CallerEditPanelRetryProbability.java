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
import java.awt.GridLayout;
import java.awt.event.ActionEvent;
import java.awt.event.KeyEvent;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import javax.swing.Icon;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTextField;

import language.Language;
import mathtools.NumberTools;
import ui.images.Images;
import ui.model.CallcenterModelCaller;

/**
 * In diesem Panel können die Wiederholwahrscheinlichkeiten für eine Kundengruppe eingestellt werden.
 * @author Alexander Herzog
 * @see CallerEditDialog
 */
public class CallerEditPanelRetryProbability extends CallerEditPanel {
	/**
	 * Serialisierungs-ID der Klasse
	 * @see Serializable
	 */
	private static final long serialVersionUID = -4361383032129176751L;

	private final List<String> retryCallerTypeChangeNames;
	private final List<Double> retryCallerTypeChangeRatesAfterBlockedFirstRetry;
	private final List<Double> retryCallerTypeChangeRatesAfterBlocked;
	private final List<Double> retryCallerTypeChangeRatesAfterGiveUpFirstRetry;
	private final List<Double> retryCallerTypeChangeRatesAfterGiveUp;

	private JCheckBox blocksLine;

	private final JTextField retryProbabiltyAfterBlockedFirstRetry;
	private final JTextField retryProbabiltyAfterBlocked;
	private final JTextField retryProbabiltyAfterGiveUpFirstRetry;
	private final JTextField retryProbabiltyAfterGiveUp;

	private final JButton retryProbabiltyAfterBlockedFirstRetryButton;
	private final JButton retryProbabiltyAfterBlockedButton;
	private final JButton retryProbabiltyAfterGiveUpFirstRetryButton;
	private final JButton retryProbabiltyAfterGiveUpButton;

	private final JLabel retryProbabiltyAfterBlockedFirstRetryLabel;
	private final JLabel retryProbabiltyAfterBlockedLabel;
	private final JLabel retryProbabiltyAfterGiveUpFirstRetryLabel;
	private final JLabel retryProbabiltyAfterGiveUpLabel;

	/**
	 * Konstruktor der Klasse
	 * @param initData	Informationen über den aktuellen Kundentyp zur Initialisierung des Panels
	 * @see CallerEditPanel.InitData
	 */
	public CallerEditPanelRetryProbability(final InitData initData) {
		super(initData);

		retryCallerTypeChangeNames=new ArrayList<String>(Arrays.asList(callerTypeNames));
		retryCallerTypeChangeRatesAfterBlockedFirstRetry=new ArrayList<Double>();
		retryCallerTypeChangeRatesAfterBlocked=new ArrayList<Double>();
		retryCallerTypeChangeRatesAfterGiveUpFirstRetry=new ArrayList<Double>();
		retryCallerTypeChangeRatesAfterGiveUp=new ArrayList<Double>();
		for (int i=0;i<retryCallerTypeChangeNames.size();i++) {
			String name=retryCallerTypeChangeNames.get(i);
			int nr;
			nr=caller.retryCallerTypeAfterBlockedFirstRetry.indexOf(name);
			if (nr<0) {
				retryCallerTypeChangeRatesAfterBlockedFirstRetry.add(0.0);
			} else {
				retryCallerTypeChangeRatesAfterBlockedFirstRetry.add(caller.retryCallerTypeRateAfterBlockedFirstRetry.get(nr));
			}
			nr=caller.retryCallerTypeAfterBlocked.indexOf(name);
			if (nr<0) {
				retryCallerTypeChangeRatesAfterBlocked.add(0.0);
			} else {
				retryCallerTypeChangeRatesAfterBlocked.add(caller.retryCallerTypeRateAfterBlocked.get(nr));
			}
			nr=caller.retryCallerTypeAfterGiveUpFirstRetry.indexOf(name);
			if (nr<0) {
				retryCallerTypeChangeRatesAfterGiveUpFirstRetry.add(0.0);
			} else {
				retryCallerTypeChangeRatesAfterGiveUpFirstRetry.add(caller.retryCallerTypeRateAfterGiveUpFirstRetry.get(nr));
			}
			nr=caller.retryCallerTypeAfterGiveUp.indexOf(name);
			if (nr<0) {
				retryCallerTypeChangeRatesAfterGiveUp.add(0.0);
			} else {
				retryCallerTypeChangeRatesAfterGiveUp.add(caller.retryCallerTypeRateAfterGiveUp.get(nr));
			}
		}

		JPanel p2;

		setLayout(new BorderLayout());

		add(blocksLine=new JCheckBox(Language.tr("Editor.Caller.RetryProbability.CallerBlocksLine"),caller.blocksLine));

		JComponent[] c;
		add(p2=new JPanel(new GridLayout(8,1)));

		c=addPercentInputLineWithButton(p2,Language.tr("Editor.Caller.RetryProbability.AfterBlockedFirst")+":",caller.retryProbabiltyAfterBlockedFirstRetry);
		retryProbabiltyAfterBlockedFirstRetry=(JTextField)c[0];
		retryProbabiltyAfterBlockedFirstRetry.setEnabled(!readOnly);
		retryProbabiltyAfterBlockedFirstRetry.addKeyListener(dialogElementListener);
		retryProbabiltyAfterBlockedFirstRetryButton=(JButton)c[1];
		retryProbabiltyAfterBlockedFirstRetryButton.addActionListener(dialogElementListener);
		retryProbabiltyAfterBlockedFirstRetryLabel=(JLabel)c[2];

		c=addPercentInputLineWithButton(p2,Language.tr("Editor.Caller.RetryProbability.AfterBlocked")+":",caller.retryProbabiltyAfterBlocked);
		retryProbabiltyAfterBlocked=(JTextField)c[0];
		retryProbabiltyAfterBlocked.setEnabled(!readOnly);
		retryProbabiltyAfterBlocked.addKeyListener(dialogElementListener);
		retryProbabiltyAfterBlockedButton=(JButton)c[1];
		retryProbabiltyAfterBlockedButton.addActionListener(dialogElementListener);
		retryProbabiltyAfterBlockedLabel=(JLabel)c[2];

		c=addPercentInputLineWithButton(p2,Language.tr("Editor.Caller.RetryProbability.AfterCancelFirst")+":",caller.retryProbabiltyAfterGiveUpFirstRetry);
		retryProbabiltyAfterGiveUpFirstRetry=(JTextField)c[0];
		retryProbabiltyAfterGiveUpFirstRetry.setEnabled(!readOnly);
		retryProbabiltyAfterGiveUpFirstRetry.addKeyListener(dialogElementListener);
		retryProbabiltyAfterGiveUpFirstRetryButton=(JButton)c[1];
		retryProbabiltyAfterGiveUpFirstRetryButton.addActionListener(dialogElementListener);
		retryProbabiltyAfterGiveUpFirstRetryLabel=(JLabel)c[2];

		c=addPercentInputLineWithButton(p2,Language.tr("Editor.Caller.RetryProbability.AfterCancel")+":",caller.retryProbabiltyAfterGiveUp);
		retryProbabiltyAfterGiveUp=(JTextField)c[0];
		retryProbabiltyAfterGiveUp.setEnabled(!readOnly);
		retryProbabiltyAfterGiveUp.addKeyListener(dialogElementListener);
		retryProbabiltyAfterGiveUpButton=(JButton)c[1];
		retryProbabiltyAfterGiveUpButton.addActionListener(dialogElementListener);
		retryProbabiltyAfterGiveUpLabel=(JLabel)c[2];

		setRetrySpecialLabels();
	}

	private void setRetrySpecialLabels() {
		double d;

		d=0;
		for (int i=0;i<retryCallerTypeChangeRatesAfterBlockedFirstRetry.size();i++) d+=retryCallerTypeChangeRatesAfterBlockedFirstRetry.get(i);
		retryProbabiltyAfterBlockedFirstRetryLabel.setText((d>0)?Language.tr("Editor.Caller.RetryProbability.ClientTypeChange.Active"):Language.tr("Editor.Caller.RetryProbability.ClientTypeChange.NotActive"));

		d=0;
		for (int i=0;i<retryCallerTypeChangeRatesAfterBlocked.size();i++) d+=retryCallerTypeChangeRatesAfterBlocked.get(i);
		retryProbabiltyAfterBlockedLabel.setText((d>0)?Language.tr("Editor.Caller.RetryProbability.ClientTypeChange.Active"):Language.tr("Editor.Caller.RetryProbability.ClientTypeChange.NotActive"));

		d=0;
		for (int i=0;i<retryCallerTypeChangeRatesAfterGiveUpFirstRetry.size();i++) d+=retryCallerTypeChangeRatesAfterGiveUpFirstRetry.get(i);
		retryProbabiltyAfterGiveUpFirstRetryLabel.setText((d>0)?Language.tr("Editor.Caller.RetryProbability.ClientTypeChange.Active"):Language.tr("Editor.Caller.RetryProbability.ClientTypeChange.NotActive"));

		d=0;
		for (int i=0;i<retryCallerTypeChangeRatesAfterGiveUp.size();i++) d+=retryCallerTypeChangeRatesAfterGiveUp.get(i);
		retryProbabiltyAfterGiveUpLabel.setText((d>0)?Language.tr("Editor.Caller.RetryProbability.ClientTypeChange.Active"):Language.tr("Editor.Caller.RetryProbability.ClientTypeChange.NotActive"));
	}

	private JComponent[] addPercentInputLineWithButton(JPanel p, String name, double initialValue) {
		return addPercentInputLineWithButton(p,name,initialValue,Language.tr("Editor.Caller.RetryProbability.ClientTypeChange"),Images.EDITOR_CALLER.getURL());
	}

	@Override
	public String[] check(KeyEvent e) {
		String[] result=null;

		if (NumberTools.getProbability(retryProbabiltyAfterBlockedFirstRetry,true)==null) {
			if (result==null) result=new String[]{Language.tr("Editor.Caller.Error.RetryProbabiltyAfterBlockedFirstRetry.Title"),String.format(Language.tr("Editor.Caller.Error.RetryProbabiltyAfterBlockedFirstRetry.Info"),retryProbabiltyAfterBlockedFirstRetry.getText())};
		}
		if (NumberTools.getProbability(retryProbabiltyAfterBlocked,true)==null) {
			if (result==null) result=new String[]{Language.tr("Editor.Caller.Error.RetryProbabiltyAfterBlocked.Title"),String.format(Language.tr("Editor.Caller.Error.RetryProbabiltyAfterBlocked.Info"),retryProbabiltyAfterBlocked.getText())};
		}
		if (NumberTools.getProbability(retryProbabiltyAfterGiveUpFirstRetry,true)==null) {
			if (result==null) result=new String[]{Language.tr("Editor.Caller.Error.RetryProbabiltyAfterGiveUpFirstRetry.Title"),String.format(Language.tr("Editor.Caller.Error.RetryProbabiltyAfterGiveUpFirstRetry.Info"),retryProbabiltyAfterGiveUpFirstRetry.getText())};
		}
		if (NumberTools.getProbability(retryProbabiltyAfterGiveUp,true)==null) {
			if (result==null) result=new String[]{Language.tr("Editor.Caller.Error.RetryProbabiltyAfterGiveUp.Title"),String.format(Language.tr("Editor.Caller.Error.RetryProbabiltyAfterGiveUp.Info"),retryProbabiltyAfterGiveUp.getText())};
		}

		return result;
	}

	@Override
	public void writeToCaller(CallcenterModelCaller newCaller) {
		newCaller.blocksLine=blocksLine.isSelected();
		newCaller.retryProbabiltyAfterBlockedFirstRetry=NumberTools.getProbability(retryProbabiltyAfterBlockedFirstRetry,false);
		newCaller.retryProbabiltyAfterBlocked=NumberTools.getProbability(retryProbabiltyAfterBlocked,false);
		newCaller.retryProbabiltyAfterGiveUpFirstRetry=NumberTools.getProbability(retryProbabiltyAfterGiveUpFirstRetry,false);
		newCaller.retryProbabiltyAfterGiveUp=NumberTools.getProbability(retryProbabiltyAfterGiveUp,false);

		newCaller.retryCallerTypeAfterBlockedFirstRetry.clear();
		newCaller.retryCallerTypeAfterBlockedFirstRetry.addAll(retryCallerTypeChangeNames);
		newCaller.retryCallerTypeRateAfterBlockedFirstRetry.clear();
		newCaller.retryCallerTypeRateAfterBlockedFirstRetry.addAll(retryCallerTypeChangeRatesAfterBlockedFirstRetry);

		newCaller.retryCallerTypeAfterBlocked.clear();
		newCaller.retryCallerTypeAfterBlocked.addAll(retryCallerTypeChangeNames);
		newCaller.retryCallerTypeRateAfterBlocked.clear();
		newCaller.retryCallerTypeRateAfterBlocked.addAll(retryCallerTypeChangeRatesAfterBlocked);

		newCaller.retryCallerTypeAfterGiveUpFirstRetry.clear();
		newCaller.retryCallerTypeAfterGiveUpFirstRetry.addAll(retryCallerTypeChangeNames);
		newCaller.retryCallerTypeRateAfterGiveUpFirstRetry.clear();
		newCaller.retryCallerTypeRateAfterGiveUpFirstRetry.addAll(retryCallerTypeChangeRatesAfterGiveUpFirstRetry);

		newCaller.retryCallerTypeAfterGiveUp.clear();
		newCaller.retryCallerTypeAfterGiveUp.addAll(retryCallerTypeChangeNames);
		newCaller.retryCallerTypeRateAfterGiveUp.clear();
		newCaller.retryCallerTypeRateAfterGiveUp.addAll(retryCallerTypeChangeRatesAfterGiveUp);
	}

	@Override
	public String getTabName() {
		return Language.tr("Editor.Caller.Tabs.RetryProbability");
	}

	@Override
	public Icon getTabIconObject() {
		return Images.EDITOR_CALLER_PAGE_RETRY.getIcon();
	}

	@Override
	protected void processDialogEvents(ActionEvent e) {
		if (e.getSource()==retryProbabiltyAfterBlockedFirstRetryButton) {
			CallerRetryChangeDialog specialDialog=new CallerRetryChangeDialog(parent,retryCallerTypeChangeNames,retryCallerTypeChangeRatesAfterBlockedFirstRetry,readOnly,helpCallback);
			specialDialog.setVisible(true);
			setRetrySpecialLabels();
			return;
		}

		if (e.getSource()==retryProbabiltyAfterBlockedButton) {
			CallerRetryChangeDialog specialDialog=new CallerRetryChangeDialog(parent,retryCallerTypeChangeNames,retryCallerTypeChangeRatesAfterBlocked,readOnly,helpCallback);
			specialDialog.setVisible(true);
			setRetrySpecialLabels();
			return;
		}

		if (e.getSource()==retryProbabiltyAfterGiveUpFirstRetryButton) {
			CallerRetryChangeDialog specialDialog=new CallerRetryChangeDialog(parent,retryCallerTypeChangeNames,retryCallerTypeChangeRatesAfterGiveUpFirstRetry,readOnly,helpCallback);
			specialDialog.setVisible(true);
			setRetrySpecialLabels();
			return;
		}

		if (e.getSource()==retryProbabiltyAfterGiveUpButton) {
			CallerRetryChangeDialog specialDialog=new CallerRetryChangeDialog(parent,retryCallerTypeChangeNames,retryCallerTypeChangeRatesAfterGiveUp,readOnly,helpCallback);
			specialDialog.setVisible(true);
			setRetrySpecialLabels();
			return;
		}
	}
}
