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

import java.io.Serializable;

import language.Language;
import tools.SetupData;
import ui.HelpLink;
import ui.images.Images;
import ui.statistic.core.StatisticBasePanel;

/**
 * Statistikpanel welche Informationen über das das Modell
 * (die ohne Simulation zur Verfügung stehen) anzeigt. *
 * @author Alexander Herzog
 * @see StatisticBasePanel
 * @see CallcenterModelEditorPanel
 */
public class CallcenterModelInfoPanel extends StatisticBasePanel {
	/**
	 * Serialisierungs-ID der Klasse
	 * @see Serializable
	 */
	private static final long serialVersionUID = -4367495882664664124L;

	/**
	 * Konstruktor der Klasse
	 * @param helpLink	Hilfe-Link
	 */
	public CallcenterModelInfoPanel(final HelpLink helpLink) {
		super(Language.tr("Editor.ModelOverview"),Images.EDITOR_MODELINFO.getURL(),Language.tr("CommandLine.ModelInfo.Name"),null,true,true,helpLink.pageModelInformationModal,helpLink,null,null,false);
	}

	@Override
	protected final void loadFilterDialogSettings() {
		setHiddenIDsFromSetupString(SetupData.getSetup().modelReportTreeFilter);
	}

	@Override
	protected final void saveFilterDialogSettings() {
		SetupData setup=SetupData.getSetup();
		setup.modelReportTreeFilter=getHiddenIDsSetupString();
		setup.saveSetupWithWarning(this);
	}

	@Override
	protected String getReportSettings() {
		return SetupData.getSetup().modelReportTreeFilter;
	}

	@Override
	protected void setReportSettings(String settings) {
		SetupData setup=SetupData.getSetup();
		setup.modelReportTreeFilter=settings;

		setup.saveSetupWithWarning(this);
	}
}
