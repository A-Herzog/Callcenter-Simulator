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

import javax.swing.Icon;

import language.Language;
import mathtools.distribution.swing.JDistributionPanel;
import tools.SetupData;
import ui.images.Images;
import ui.model.CallcenterModelCaller;

/**
 * In diesem Panel können die Wiederholabstände für eine Kundengruppe eingestellt werden.
 * @author Alexander Herzog
 * @see CallerEditDialog
 */
public class CallerEditPanelRetryDistribution extends CallerEditPanel {
	private static final long serialVersionUID = -5849131132110088748L;

	private JDistributionPanel retryTimeDist;

	/**
	 * Konstruktor der Klasse
	 * @param initData	Informamtionen über den aktuellen Kundentyp zur Initialisierung des Panels
	 * @see CallerEditPanel.InitData
	 */
	public CallerEditPanelRetryDistribution(final InitData initData) {
		super(initData);

		setLayout(new BorderLayout());

		add(retryTimeDist=new JDistributionPanel(caller.retryTimeDist,CallcenterModelCaller.retryTimeDistMaxX,true),BorderLayout.CENTER);
		retryTimeDist.setAllowChangeDistributionData(!readOnly);
		retryTimeDist.setImageSaveSize(SetupData.getSetup().imageSize);
	}

	@Override
	public void writeToCaller(CallcenterModelCaller newCaller) {
		newCaller.retryTimeDist=retryTimeDist.getDistribution();
	}

	@Override
	public String getTabName() {
		return Language.tr("Editor.Caller.Tabs.RetryIntervals");
	}

	@Override
	public Icon getTabIconObject() {
		return Images.EDITOR_CALLER_PAGE_RETRY.getIcon();
	}
}
