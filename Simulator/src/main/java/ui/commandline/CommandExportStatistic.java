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

import org.w3c.dom.Element;

import language.Language;
import simulator.Statistics;
import systemtools.commandline.AbstractCommand;
import systemtools.commandline.BaseCommandLineSystem;
import ui.StatisticWebAppWriter;
import xml.XMLTools;

/**
 * Exportiert Statistikdaten aus eienr xml in anderen Ausgabeformaten.
 * @author Alexander Herzog
 * @version 1.0
 * @see AbstractCommand
 * @see CommandLineSystem
 */
public class CommandExportStatistic extends AbstractCommand {
	/** Export-Modus */
	private enum ExportMode {
		/** JSON-Daten */
		EXPORT_MODE_JSON,
		/** WebApp */
		EXPORT_MODE_WEBAPP
	}

	/** Statistik-Eingabedatei */
	private File statisticsInputFile;
	/** Export-Ausgabedatei */
	private File  exportOutputFile;
	/** Export-Modus */
	private ExportMode exportMode=null;

	/**
	 * Konstruktor der Klasse
	 * @param system	Referenz auf das Kommandozeilensystem
	 */
	public CommandExportStatistic(final BaseCommandLineSystem system) {
		super(system);
	}

	@Override
	public String[] getKeys() {
		List<String> list=new ArrayList<>();
		list.add(Language.tr("CommandLine.ExportStatistic.Name"));
		for (String s: Language.trOther("CommandLine.ExportStatistic.Name")) if (!list.contains(s)) list.add(s);
		return list.toArray(String[]::new);
	}

	@Override
	public String getShortDescription() {
		return Language.tr("CommandLine.ExportStatistic.Description.Short");
	}

	@Override
	public String[] getLongDescription() {
		return Language.tr("CommandLine.ExportStatistic.Description.Long").split("\n");
	}

	/**
	 * Prüft, ob die übergebene Datei eine Statistik-Datei ist
	 * @param file	Zu prüfende Datei
	 * @return	Gibt <code>true</code> zurück, wenn es sich um eine Statistik-Datei handelt
	 */
	private boolean isStatisticFile(final File file) {
		XMLTools xml=new XMLTools(file);
		Element root=xml.load();
		if (root==null) return false;
		for (String s: Statistics.XMLBaseElement) if (root.getNodeName().equalsIgnoreCase(s)) return true;
		return false;
	}

	@Override
	public String prepare(String[] additionalArguments, InputStream in, PrintStream out) {
		String s=parameterCountCheck(3,additionalArguments); if (s!=null) return s;

		statisticsInputFile=new File(additionalArguments[1]);
		exportOutputFile=new File(additionalArguments[2]);

		String cmd=additionalArguments[0];
		exportMode=null;
		if (cmd.equalsIgnoreCase("json")) exportMode=ExportMode.EXPORT_MODE_JSON;
		if (cmd.equalsIgnoreCase("WebApp")) exportMode=ExportMode.EXPORT_MODE_WEBAPP;
		if (exportMode==null) return Language.tr("CommandLine.ExportStatistic.InvalidExportMode");

		if (!statisticsInputFile.isFile()) return String.format(Language.tr("CommandLine.Error.File.InputDoesNotExist"),statisticsInputFile);
		if (!isStatisticFile(statisticsInputFile)) return String.format(Language.tr("CommandLine.Error.File.InputNoValidStatisticFile"),statisticsInputFile);
		if (exportOutputFile.isFile()) return String.format(Language.tr("CommandLine.Error.File.OutputAlreadyExist"),exportOutputFile);
		if (exportOutputFile.isDirectory()) return String.format(Language.tr("CommandLine.Error.File.OutputFileIsFolder"),exportOutputFile);

		return null;
	}

	@Override
	public void run(AbstractCommand[] allCommands, InputStream in, PrintStream out) {
		Statistics inputStatistics=new Statistics(null,null,0,0);
		String s=inputStatistics.loadFromFile(statisticsInputFile);
		if (s!=null) {out.println(Language.tr("Dialog.Title.Error").toUpperCase()+": "+Language.tr("CommandLine.ExportStatistic.ErrorLoadingStatistic")+": "+s); return;}

		boolean ok=false;
		switch (exportMode) {
		case EXPORT_MODE_JSON:
			ok=inputStatistics.saveToFile(exportOutputFile);
			break;
		case EXPORT_MODE_WEBAPP:
			StatisticWebAppWriter writer=new StatisticWebAppWriter(inputStatistics);
			ok=writer.saveToFile(exportOutputFile);
			break;
		}
		if (!ok) {
			out.println(String.format(Language.tr("CommandLine.ExportStatistic.ErrorCouldNotSave"),exportOutputFile.toString()));
		}
	}
}
