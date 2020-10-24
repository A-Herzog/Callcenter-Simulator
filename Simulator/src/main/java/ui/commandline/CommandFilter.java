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

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.PrintStream;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;

import language.Language;
import simulator.Statistics;
import systemtools.commandline.AbstractCommand;
import ui.statistic.core.filter.DataFilter;
import ui.statistic.core.filter.DataFilterBase;

/**
 * Extrahiert ein Einzelergebnis aus einer Statistik-xml-Datei.
 * @author Alexander Herzog
 * @see AbstractCommand
 * @see CommandLineSystem
 */
public final class CommandFilter extends AbstractSimulationCommand {
	/** Statistik-Eingabedatei */
	private File statisticsInputFile;
	/** Filter-Eingabedatei */
	private File filterFile;
	/** Ausgabedatei für die gefilternten Ergebnisse */
	private File filterResultFile;

	@Override
	public String[] getKeys() {
		List<String> list=new ArrayList<String>();
		list.add(Language.tr("CommandLine.Filter.Name"));
		for (String s: Language.trOther("CommandLine.Filter.Name")) if (!list.contains(s)) list.add(s);
		return list.toArray(new String[0]);
	}

	@Override
	public String getShortDescription() {
		return Language.tr("CommandLine.Filter.Description.Short");
	}

	@Override
	public String[] getLongDescription() {
		return Language.tr("CommandLine.Filter.Description.Long").split("\n");
	}

	@Override
	public String prepare(String[] additionalArguments, InputStream in, PrintStream out) {
		String s=parameterCountCheck(3,additionalArguments); if (s!=null) return s;

		statisticsInputFile=new File(additionalArguments[0]);
		filterFile=new File(additionalArguments[1]);
		filterResultFile=new File(additionalArguments[2]);
		if (!statisticsInputFile.isFile()) return String.format(Language.tr("CommandLine.Error.File.InputDoesNotExist"),statisticsInputFile);
		if (!isStatisticFile(statisticsInputFile)) return String.format(Language.tr("CommandLine.Error.File.InputNoValidStatisticFile"),statisticsInputFile);
		if (!filterFile.isFile()) return String.format(Language.tr("CommandLine.Error.File.ConfigDoesNotExist"),filterFile);
		if (filterResultFile.isDirectory()) return String.format(Language.tr("CommandLine.Error.File.OutputFileIsFolder"),filterResultFile);
		return null;
	}

	/**
	 * Führt die Verarbeitung des Filters aus.
	 * @param statistic	Statistik-Eingabedaten
	 * @param commands	Filterbefehle
	 * @param results	Ausgabedatei
	 * @param out	Ausgabe für Fehlermeldungen
	 * @see #run(AbstractCommand[], InputStream, PrintStream)
	 */
	private void runFilter(Statistics statistic, String commands, File results, PrintStream out) {
		DataFilter dataFilter=new DataFilter(statistic.saveToXMLDocument());
		if (!dataFilter.run(commands,false)) {
			out.println(Language.tr("CommandLine.Filter.Done.Error")+":");
			out.println(dataFilter.getResults());
			return;
		}
		if (DataFilterBase.saveText(dataFilter.getResults(),results,true)) {
			out.println(Language.tr("CommandLine.Filter.Done.Success"));
		} else {
			out.println(String.format(Language.tr("CommandLine.Filter.Done.CouldNotSave"),results.toString()));
		}
	}

	@Override
	public void run(AbstractCommand[] allCommands, InputStream in, PrintStream out) {
		Statistics inputStatistics=new Statistics(null,null,0,0);
		String s=inputStatistics.loadFromFile(statisticsInputFile);
		if (s!=null) {out.println(Language.tr("Dialog.Title.Error").toUpperCase()+": "+Language.tr("CommandLine.Filter.ErrorLoadingStatistic")+": "+s); return;}
		String commands="";
		StringBuilder text=new StringBuilder();
		try (BufferedReader br=new BufferedReader(new InputStreamReader(new FileInputStream(filterFile),StandardCharsets.UTF_8))) {
			String line=null;
			while ((line=br.readLine())!=null) {
				text.append(line);
				text.append(System.getProperty("line.separator"));
			}
			commands=text.toString();
		} catch (IOException e) {
			out.println(Language.tr("Dialog.Title.Error").toUpperCase()+": "+Language.tr("CommandLine.Filter.ErrorLoadingFilterConfiguration"));
			return;
		}
		runFilter(inputStatistics,commands,filterResultFile,out);
	}
}
