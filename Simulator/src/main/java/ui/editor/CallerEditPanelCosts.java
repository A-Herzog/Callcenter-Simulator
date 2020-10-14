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
import java.awt.event.KeyEvent;
import java.io.Serializable;

import javax.swing.Icon;
import javax.swing.JPanel;
import javax.swing.JTextField;

import language.Language;
import mathtools.NumberTools;
import ui.images.Images;
import ui.model.CallcenterModelCaller;

/**
 * In diesem Panel können die Kundentyp-abhängigen Kosten für eine
 * Kundengruppe eingestellt werden.
 * @author Alexander Herzog
 * @see CallerEditDialog
 */
public class CallerEditPanelCosts extends CallerEditPanel {
	/**
	 * Serialisierungs-ID der Klasse
	 * @see Serializable
	 */
	private static final long serialVersionUID = -2674610320989485395L;

	private final JTextField revenuePerClient;
	private final JTextField costPerCancel;
	private final JTextField costPerWaitingSec;

	/**
	 * Konstruktor der Klasse
	 * @param initData	Informamtionen über den aktuellen Kundentyp zur Initialisierung des Panels
	 * @see CallerEditPanel.InitData
	 */
	public CallerEditPanelCosts(final InitData initData) {
		super(initData);

		JPanel p2;

		setLayout(new BorderLayout());

		add(p2=new JPanel(new GridLayout(6,1)));
		revenuePerClient=addInputLine(p2,Language.tr("Editor.Caller.Costs.RevenuePerSuccessfulClient")+":",caller.revenuePerClient);
		revenuePerClient.setEnabled(!readOnly);
		revenuePerClient.addKeyListener(dialogElementListener);
		costPerCancel=addInputLine(p2,Language.tr("Editor.Caller.Costs.PerCancel")+":",caller.costPerCancel);
		costPerCancel.setEnabled(!readOnly);
		costPerCancel.addKeyListener(dialogElementListener);
		costPerWaitingSec=addInputLine(p2,Language.tr("Editor.Caller.Costs.PerWaitingSecond")+":",caller.costPerWaitingSec);
		costPerWaitingSec.setEnabled(!readOnly);
		costPerWaitingSec.addKeyListener(dialogElementListener);
	}

	@Override
	public String[] check(KeyEvent e) {
		String[] result=null;

		if (NumberTools.getNotNegativeDouble(revenuePerClient,true)==null) {
			if (result==null) result=new String[]{Language.tr("Editor.Caller.Error.RevenuePerSuccessfulClient.Title"),String.format(Language.tr("Editor.Caller.Error.RevenuePerSuccessfulClient.Info"),revenuePerClient.getText())};
		}
		if (NumberTools.getNotNegativeDouble(costPerCancel,true)==null) {
			if (result==null) result=new String[]{Language.tr("Editor.Caller.Error.CostsPerCancel.Title"),String.format(Language.tr("Editor.Caller.Error.CostsPerCancel.Info"),costPerCancel.getText())};
		}
		if (NumberTools.getNotNegativeDouble(costPerWaitingSec,true)==null) {
			if (result==null) result=new String[]{Language.tr("Editor.Caller.Error.CostsPerWaitingSecond.Title"),String.format(Language.tr("Editor.Caller.Error.CostsPerWaitingSecond.Info"),costPerWaitingSec.getText())};
		}

		return result;
	}

	@Override
	public void writeToCaller(CallcenterModelCaller newCaller) {
		newCaller.revenuePerClient=NumberTools.getNotNegativeDouble(revenuePerClient,false);
		newCaller.costPerCancel=NumberTools.getNotNegativeDouble(costPerCancel,false);
		newCaller.costPerWaitingSec=NumberTools.getNotNegativeDouble(costPerWaitingSec,false);
	}

	@Override
	public String getTabName() {
		return Language.tr("Editor.Caller.Tabs.Costs");
	}

	@Override
	public Icon getTabIconObject() {
		return Images.EDITOR_COSTS.getIcon();
	}
}
