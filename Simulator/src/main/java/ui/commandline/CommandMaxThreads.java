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
import mathtools.NumberTools;
import systemtools.commandline.AbstractCommand;
import systemtools.commandline.BaseCommandLineSystem;
import tools.SetupData;

/**
 * Stellt die Maximalanzahl an für die Simulation zu verwendenden Threads ein.
 * @author Alexander Herzog
 * @see AbstractCommand
 * @see CommandLineSystem
 */
public class CommandMaxThreads extends AbstractCommand {
	/** Maximalzahl an Threads (-1 für unbegrenzt) */
	private int newValue;

	/**
	 * Konstruktor der Klasse
	 * @param system	Referenz auf das Kommandozeilensystem
	 */
	public CommandMaxThreads(final BaseCommandLineSystem system) {
		super(system);
	}

	@Override
	public String[] getKeys() {
		List<String> list=new ArrayList<>();
		list.add(Language.tr("CommandLine.MaxThreads.Name"));
		for (String s: Language.trOther("CommandLine.MaxThreads.Name")) if (!list.contains(s)) list.add(s);
		return list.toArray(String[]::new);
	}

	@Override
	public String getShortDescription() {
		return Language.tr("CommandLine.MaxThreads.Description.Short");
	}

	@Override
	public String[] getLongDescription() {
		return Language.tr("CommandLine.MaxThreads.Description.Long").split("\n");
	}

	@Override
	public String prepare(String[] additionalArguments, InputStream in, PrintStream out) {
		String s=parameterCountCheck(1,additionalArguments); if (s!=null) return s;
		Integer I=NumberTools.getNotNegativeInteger(additionalArguments[0].trim());
		if (I==null) {
			return String.format(Language.tr("CommandLine.MaxThreads.InvalidParameter"),additionalArguments[0].trim());
		}
		newValue=I;
		return null;
	}

	@Override
	public void run(AbstractCommand[] allCommands, InputStream in, PrintStream out) {
		SetupData setup=SetupData.getSetup();
		setup.maxNumberOfThreads=newValue;
		if (!setup.saveSetup()) {
			out.println(Language.tr("CommandLine.MaxThreads.Result.ErrorSaving"));
		} else {
			if (newValue==0) {
				out.println(Language.tr("CommandLine.MaxThreads.Result.NoLimitation"));
			} else {
				out.println(String.format(Language.tr("CommandLine.MaxThreads.Result.NewLimit"),newValue));
			}
		}
	}
}
