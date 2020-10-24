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

import java.io.InputStream;
import java.io.PrintStream;
import java.util.ArrayList;
import java.util.List;

import language.Language;
import systemtools.commandline.AbstractCommand;
import tools.SetupData;

/**
 * Stellt die zu verwendende Sprache ein.
 * @author Alexander Herzog
 * @version 1.0
 * @see AbstractCommand
 * @see CommandLineSystem
 */
public class CommandLanguage extends AbstractCommand {
	/** Einzustellende Sprache ("de" oder "en") */
	private String languageName="";

	@Override
	public String[] getKeys() {
		final List<String> list=new ArrayList<String>();
		list.add(Language.tr("CommandLine.Language.Name"));
		for (String s: Language.trOther("CommandLine.Language.Name")) if (!list.contains(s)) list.add(s);
		return list.toArray(new String[0]);
	}

	@Override
	public String getShortDescription() {
		return Language.tr("CommandLine.Language.Description.Short");
	}

	@Override
	public String[] getLongDescription() {
		return Language.tr("CommandLine.Language.Description.Long").split("\n");
	}

	@Override
	public String prepare(String[] additionalArguments, InputStream in, PrintStream out) {
		String s=parameterCountCheck(1,additionalArguments); if (s!=null) return s;
		languageName=additionalArguments[0];
		return null;
	}

	/**
	 * Stellt eine Sprache im Setup ein.
	 * @param languageID	ID der Sprache ("de" oder "en")
	 */
	private void setLanguage(String languageID) {
		final SetupData setup=SetupData.getSetup();
		setup.language=languageID;
		setup.saveSetup();
	}

	@Override
	public void run(AbstractCommand[] allCommands, InputStream in, PrintStream out) {
		if (languageName==null || languageName.isEmpty()) {setLanguage("en"); return;}
		if (Language.trAll("CommandLine.Language.English",languageName) || languageName.equalsIgnoreCase("en")) {setLanguage("en"); return;}
		if (Language.trAll("CommandLine.Language.German",languageName) || languageName.equalsIgnoreCase("de")) {setLanguage("de"); return;}
		out.println(String.format(Language.tr("CommandLine.Language.UnknownLanguage"),languageName));
	}

}
