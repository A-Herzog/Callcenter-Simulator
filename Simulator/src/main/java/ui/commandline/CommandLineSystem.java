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
import java.util.List;

import systemtools.commandline.AbstractCommand;
import systemtools.commandline.BaseCommandLineSystem;
import ui.MainFrame;
import ui.MainPanel;
import ui.VersionConst;

/**
 * Klasse zur Bearbeitung von Kommandozeilen-Aufrufen des Simulators
 * @author Alexander Herzog
 * @version 1.0
 * @see AbstractCommand
 */
public final class CommandLineSystem extends BaseCommandLineSystem {
	/**
	 * Konstruktor der Klasse <code>CommandLine</code>
	 * @param in	Ein {@link InputStream}-Objekt oder <code>null</code>, über das Zeichen von der Konsole gelesen werden können (<code>null</code>, wenn keine Konsole verfügbar ist)
	 * @param out	Ein {@link PrintStream}-Objekt, über das Texte ausgegeben werden können.
	 */
	public CommandLineSystem(InputStream in, PrintStream out) {
		super(MainFrame.PROGRAM_NAME,VersionConst.version,MainPanel.AUTHOR,in,out);
	}

	/**
	 * Konstruktor der Klasse <code>CommandLineSystem</code><br>
	 * Die Ausgabe der Befehle erfolgt auf <code>System.out</code>
	 */
	public CommandLineSystem() {
		this(System.in,System.out);
	}

	@Override
	protected List<AbstractCommand> getCommands() {
		final List<AbstractCommand> commands=super.getCommands();

		commands.add(new CommandSimulation(this));
		commands.add(new CommandBatch(this));
		commands.add(new CommandConnected(this));
		commands.add(new CommandOptimize(this));
		commands.add(new CommandOptimizeExport(this));
		commands.add(new CommandRearrange(this));
		commands.add(new CommandPreplanning(this));
		commands.add(new CommandSimplify(this));
		commands.add(new CommandRevenueOptimizer(this));
		commands.add(new CommandGeneratorCaller(this));
		commands.add(new CommandGeneratorAgents(this));
		commands.add(new CommandGeneratorAgentsEfficiency(this));
		commands.add(new CommandGeneratorAgentsAddition(this));
		commands.add(new CommandGeneratorSkills(this));
		commands.add(new CommandGeneratorLoadSimpleModel(this));
		commands.add(new CommandGeneratorXMLPreprocessing(this));
		commands.add(new CommandBenchmark(this));
		commands.add(new CommandServer(this));
		commands.add(new CommandServerLog(this));
		commands.add(new CommandSaaSServer(this));
		commands.add(new CommandFilter(this));
		commands.add(new CommandImporter(this));
		commands.add(new CommandReport(this));
		commands.add(new CommandModelInfo(this));
		commands.add(new CommandExportStatistic(this));
		commands.add(new CommandLanguage(this));
		commands.add(new CommandReset(this));
		commands.add(new CommandMaxThreads(this));
		commands.add(new CommandUpdate(this));

		return commands;
	}

	/**
	 * Liefert eine Liste mit allen verfügbaren Kommandozeilenbefehlen.
	 * @return	Liste mit allen verfügbaren Kommandozeilenbefehlen
	 */
	public static AbstractCommand[] list() {
		final CommandLineSystem system=new CommandLineSystem(null,null);
		return system.getCommands().toArray(AbstractCommand[]::new);
	}
}
