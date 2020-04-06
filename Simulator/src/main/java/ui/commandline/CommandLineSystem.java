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

		commands.add(new CommandSimulation());
		commands.add(new CommandBatch());
		commands.add(new CommandConnected());
		commands.add(new CommandOptimize());
		commands.add(new CommandOptimizeExport());
		commands.add(new CommandRearrange());
		commands.add(new CommandPreplanning());
		commands.add(new CommandSimplify());
		commands.add(new CommandRevenueOptimizer());
		commands.add(new CommandGeneratorCaller());
		commands.add(new CommandGeneratorAgents());
		commands.add(new CommandGeneratorAgentsEfficiency());
		commands.add(new CommandGeneratorAgentsAddition());
		commands.add(new CommandGeneratorSkills());
		commands.add(new CommandGeneratorLoadSimpleModel());
		commands.add(new CommandGeneratorXMLPreprocessing());
		commands.add(new CommandBenchmark());
		commands.add(new CommandServer());
		commands.add(new CommandServerLog());
		commands.add(new CommandSaaSServer());
		commands.add(new CommandFilter());
		commands.add(new CommandImporter());
		commands.add(new CommandReport());
		commands.add(new CommandModelInfo());
		commands.add(new CommandExportStatistic());
		commands.add(new CommandLanguage());
		commands.add(new CommandReset());
		commands.add(new CommandMaxThreads());
		commands.add(new CommandUpdate());

		return commands;
	}

	/**
	 * Liefert eine Liste mit allen verfügbaren Kommandozeilenbefehlen.
	 * @return	Liste mit allen verfügbaren Kommandozeilenbefehlen
	 */
	public static AbstractCommand[] list() {
		final CommandLineSystem system=new CommandLineSystem(null,null);
		return system.getCommands().toArray(new AbstractCommand[0]);
	}
}
