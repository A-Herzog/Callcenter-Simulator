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
package ui.commandline;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

import language.Language;
import systemtools.commandline.AbstractCommand;
import systemtools.commandline.AbstractReportCommand;
import systemtools.commandline.BaseCommandLineSystem;
import ui.HelpLink;
import ui.editor.CallcenterModelEditorPanel;
import ui.model.CallcenterModel;

/**
 * Exportiert einen Teil oder die gesamten Modell-Informationen.
 * @author Alexander Herzog
 * @see AbstractCommand
 * @see CommandLineSystem
 */
public class CommandModelInfo extends AbstractReportCommand {
	/**
	 * Konstruktor der Klasse
	 * @param system	Referenz auf das Kommandozeilensystem
	 */
	public CommandModelInfo(final BaseCommandLineSystem system) {
		super(system);
	}

	@Override
	public String[] getKeys() {
		List<String> list=new ArrayList<>();
		list.add(Language.tr("CommandLine.ModelInfo.Name"));
		for (String s: Language.trOther("CommandLine.ModelInfo.Name")) if (!list.contains(s)) list.add(s);
		return list.toArray(new String[0]);
	}

	@Override
	public String getShortDescription() {
		return Language.tr("CommandLine.ModelInfo.Description.Short");
	}

	@Override
	protected Object getReportCommandConnect(File input) {
		CallcenterModel model=new CallcenterModel();
		String s=model.loadFromFile(input); if (s!=null) return s;

		CallcenterModelEditorPanel panel=new CallcenterModelEditorPanel(null,true,model,true,new HelpLink(null,null,null),null,null,null,null,null,null,null,null,null);

		return panel;
	}
}
