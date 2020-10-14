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
import java.io.Serializable;

import javax.swing.BorderFactory;
import javax.swing.Icon;
import javax.swing.JLabel;
import javax.swing.JPanel;

import language.Language;
import mathtools.distribution.swing.JDistributionPanel;
import tools.SetupData;
import ui.images.Images;
import ui.model.CallcenterModelCaller;

/**
 * In diesem Panel können die Wiederanrufabstände für eine Kundengruppe eingestellt werden.
 * @author Alexander Herzog
 * @see CallerEditDialog
 */
public class CallerEditPanelRecallerDistribution extends CallerEditPanel {
	/**
	 * Serialisierungs-ID der Klasse
	 * @see Serializable
	 */
	private static final long serialVersionUID = -5982396824981613714L;

	private JDistributionPanel recallTimeDist;

	/**
	 * Konstruktor der Klasse
	 * @param initData	Informamtionen über den aktuellen Kundentyp zur Initialisierung des Panels
	 * @see CallerEditPanel.InitData
	 */
	public CallerEditPanelRecallerDistribution(final InitData initData) {
		super(initData);

		setLayout(new BorderLayout());

		JPanel p2;

		add(p2=new JPanel(),BorderLayout.NORTH);
		p2.setLayout(new FlowLayout(FlowLayout.LEFT,5,0));
		p2.setBorder(BorderFactory.createEmptyBorder(5,0,5,0));
		p2.add(new JLabel("<html><body>"+Language.tr("Editor.Caller.RecallProbability.Info")+"</boby></html>"),BorderLayout.NORTH);
		add(recallTimeDist=new JDistributionPanel(caller.recallTimeDist,CallcenterModelCaller.recallTimeDistMaxX,true),BorderLayout.CENTER);
		recallTimeDist.setAllowChangeDistributionData(!readOnly);
		recallTimeDist.setImageSaveSize(SetupData.getSetup().imageSize);
	}

	@Override
	public void writeToCaller(CallcenterModelCaller newCaller) {
		caller.recallTimeDist=recallTimeDist.getDistribution();
	}

	@Override
	public String getTabName() {
		return Language.tr("Editor.Caller.Tabs.RecallIntervals");
	}

	@Override
	public Icon getTabIconObject() {
		return Images.EDITOR_CALLER_PAGE_RECALLER.getIcon();
	}
}
