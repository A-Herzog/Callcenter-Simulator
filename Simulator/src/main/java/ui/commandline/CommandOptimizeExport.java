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
import java.io.InputStream;
import java.io.PrintStream;
import java.util.ArrayList;
import java.util.List;

import language.Language;
import systemtools.commandline.AbstractCommand;
import systemtools.commandline.BaseCommandLineSystem;
import ui.optimizer.OptimizeData;

/**
 * Extrahiert die Daten zum letzten Simulationslauf aus den Optimierungsdaten
 * @author Alexander Herzog
 * @version 1.0
 * @see AbstractCommand
 * @see CommandLineSystem
 */
public class CommandOptimizeExport extends AbstractCommand {
	/** Eingabe-Optimierer-Ergebnisse */
	private File optimizeFile;
	/** Ausgabe-Statistikdaten */
	private File statisticsFile;

	/**
	 * Konstruktor der Klasse
	 * @param system	Referenz auf das Kommandozeilensystem
	 */
	public CommandOptimizeExport(final BaseCommandLineSystem system) {
		super(system);
	}

	@Override
	public String[] getKeys() {
		List<String> list=new ArrayList<>();
		list.add(Language.tr("CommandLine.OptimizerExport.Name"));
		for (String s: Language.trOther("CommandLine.OptimizerExport.Name")) if (!list.contains(s)) list.add(s);
		return list.toArray(String[]::new);
	}

	@Override
	public String getShortDescription() {
		return Language.tr("CommandLine.OptimizerExport.Description.Short");
	}

	@Override
	public String[] getLongDescription() {
		return Language.tr("CommandLine.OptimizerExport.Description.Long").split("\n");
	}

	@Override
	public final String prepare(String[] additionalArguments, InputStream in, PrintStream out) {
		String s=parameterCountCheck(2,additionalArguments); if (s!=null) return s;
		optimizeFile=new File(additionalArguments[0]);
		statisticsFile=new File(additionalArguments[1]);
		if (!optimizeFile.isFile()) return String.format(Language.tr("CommandLine.Error.File.OptimizerResultsFileDoesNotExist"),optimizeFile);
		if (statisticsFile.exists()) return String.format(Language.tr("CommandLine.Error.File.OutputAlreadyExist"),statisticsFile);
		return null;
	}

	@Override
	public void run(AbstractCommand[] allCommands, InputStream in, PrintStream out) {
		OptimizeData data=new OptimizeData();
		String s=data.loadFromFile(optimizeFile);
		if (s!=null) {out.println(Language.tr("Dialog.Title.Error").toUpperCase()+": "+Language.tr("CommandLine.OptimizerExport.ErrorLoadingResults")+": "+s); return;}

		if (!data.data.get(data.data.size()-1).saveToFile(statisticsFile)) {out.println(Language.tr("Dialog.Title.Error").toUpperCase()+": "+Language.tr("CommandLine.Error.UnableToSaveStatistic")); return;}
	}
}
