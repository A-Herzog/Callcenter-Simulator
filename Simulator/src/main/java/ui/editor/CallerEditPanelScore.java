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

import javax.swing.Icon;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTextField;

import language.Language;
import mathtools.NumberTools;
import ui.images.Images;
import ui.model.CallcenterModelCaller;

/**
 * In diesem Panel können die Scorewerte für eine Kundengruppe eingestellt werden.
 * @author Alexander Herzog
 * @see CallerEditDialog
 */
public class CallerEditPanelScore extends CallerEditPanel {
	private static final long serialVersionUID = -7843545974839816989L;

	private final JTextField scoreBase;
	private final JTextField scoreSecond;
	private final JTextField scoreContinued;
	private JLabel scoreWarning;

	/**
	 * Konstruktor der Klasse
	 * @param initData	Informamtionen über den aktuellen Kundentyp zur Initialisierung des Panels
	 * @see CallerEditPanel.InitData
	 */
	public CallerEditPanelScore(final InitData initData) {
		super(initData);

		JPanel p2;

		setLayout(new BorderLayout());

		add(p2=new JPanel(new GridLayout(7,1)));
		scoreBase=addInputLine(p2,Language.tr("Editor.Caller.Score.Base")+":",caller.scoreBase);
		scoreBase.setEnabled(!readOnly);
		scoreBase.addKeyListener(dialogElementListener);
		scoreSecond=addInputLine(p2,Language.tr("Editor.Caller.Score.Waiting")+":",caller.scoreSecond);
		scoreSecond.setEnabled(!readOnly);
		scoreSecond.addKeyListener(dialogElementListener);
		scoreContinued=addInputLine(p2,Language.tr("Editor.Caller.Score.Forwarded")+":",caller.scoreContinued);
		scoreContinued.setEnabled(!readOnly);
		scoreContinued.addKeyListener(dialogElementListener);
		p2.add(scoreWarning=new JLabel("<html><font style=\"color: red\">"+Language.tr("Editor.Caller.Score.Waiting.ZeroWarning")+"</font></html>"));
		scoreWarning.setVisible(Math.abs(caller.scoreSecond)<0.0000001);
	}

	@Override
	public String[] check(KeyEvent e) {
		String[] result=null;

		if (NumberTools.getDouble(scoreBase,true)==null) {
			if (result==null) result=new String[]{Language.tr("Editor.Caller.Error.ScoreBase.Title"),String.format(Language.tr("Editor.Caller.Error.ScoreBase.Info"),scoreBase.getText())};
		}
		if (NumberTools.getDouble(scoreSecond,true)==null) {
			if (result==null) result=new String[]{Language.tr("Editor.Caller.Error.ScoreSecond.Title"),String.format(Language.tr("Editor.Caller.Error.ScoreSecond.Info"),scoreSecond.getText())};
		}
		if (NumberTools.getDouble(scoreContinued,true)==null) {
			if (result==null) result=new String[]{Language.tr("Editor.Caller.Error.ScoreForward.Title"),String.format(Language.tr("Editor.Caller.Error.ScoreForward.Info"),scoreContinued.getText())};
		}

		if (e!=null && e.getSource()==scoreSecond) {
			Double D=NumberTools.getDouble(scoreSecond,true);
			scoreWarning.setVisible(D!=null && Math.abs(D)<0.0000001);
		}

		return result;
	}

	@Override
	public void writeToCaller(CallcenterModelCaller newCaller) {
		newCaller.scoreBase=NumberTools.getDouble(scoreBase,false);
		newCaller.scoreSecond=NumberTools.getDouble(scoreSecond,false);
		newCaller.scoreContinued=NumberTools.getDouble(scoreContinued,false);
	}

	@Override
	public String getTabName() {
		return Language.tr("Editor.Caller.Tabs.Score");
	}

	@Override
	public Icon getTabIconObject() {
		return Images.EDITOR_CALLER_PAGE_SCORE.getIcon();
	}

	@Override
	protected void processDialogEvents(ActionEvent e) {
	}
}
