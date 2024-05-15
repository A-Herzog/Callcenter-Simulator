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

import java.io.PrintStream;
import java.util.ArrayList;
import java.util.List;

import language.Language;
import mathtools.NumberTools;
import mathtools.Table;
import mathtools.distribution.DataDistributionImpl;
import systemtools.commandline.BaseCommandLineSystem;
import ui.model.CallcenterModel;
import ui.model.CallcenterModelAgent;
import ui.model.CallcenterModelCallcenter;

/**
 * Lädt per Kommandozeilenbefehl die Krankheitsaufschlag für Agenten aus einer Tabelle und fügt diesen in ein Callcenter-Modell ein.
 * @author Alexander Herzog
 * @see AbstractGeneratorCommand
 * @see CommandLineSystem
 */
public class CommandGeneratorAgentsAddition extends AbstractGeneratorCommand {
	/**
	 * Konstruktor der Klasse
	 * @param system	Referenz auf das Kommandozeilensystem
	 */
	public CommandGeneratorAgentsAddition(final BaseCommandLineSystem system) {
		super(system);
		nameArgumentCount=1;
	}

	@Override
	public final String[] getKeys() {
		List<String> list=new ArrayList<>();
		list.add(Language.tr("CommandLine.GeneratorAgentsAddition.Name"));
		for (String s: Language.trOther("CommandLine.GeneratorAgentsAddition.Name")) if (!list.contains(s)) list.add(s);
		return list.toArray(String[]::new);
	}

	@Override
	public final String getShortDescription() {
		return Language.tr("CommandLine.GeneratorAgentsAddition.Description.Short");
	}

	@Override
	public final String[] getLongDescription() {
		return Language.tr("CommandLine.GeneratorAgentsAddition.Description.Long").split("\n");
	}

	/**
	 * Lädt die Agentendaten aus einer Tabelle.
	 * @param row	Startzeile
	 * @param col	Startspalte
	 * @param editModel	Ziel-Callcenter-Modell
	 * @param table	Eingabetabelle
	 * @param type	Name der Agentengruppe
	 * @return	Liefert im Erfolgsfall <code>null</code>, sonst eine Fehlermeldung
	 */
	private final String generatorAgents(int row, int col, CallcenterModel editModel, Table table, String type) {
		/* Callcenter wählen */
		CallcenterModelCallcenter callcenter=null;
		for (int i=0;i<editModel.callcenter.size();i++) if (editModel.callcenter.get(i).name.equalsIgnoreCase(name[0])) {callcenter=editModel.callcenter.get(i); break;}
		if (callcenter==null) return String.format(Language.tr("CommandLine.Error.NoCallCenterWithName"),name[0]);

		/* Agentengruppe wählen */
		Integer nr=NumberTools.getNotNegativeInteger(type);
		if (nr==null || nr<1) return String.format(Language.tr("CommandLine.Error.InvalidAgentsGroupNumber1"),type);
		if (nr>callcenter.agents.size()) return String.format(Language.tr("CommandLine.Error.InvalidAgentsGroupNumber2"),nr,name[0],callcenter.agents.size());
		CallcenterModelAgent agent=callcenter.agents.get(nr-1);

		/* Daten laden und eintragen */
		DataDistributionImpl addition=editModel.additionPerInterval.clone();
		String s=loadData(row,col,table,addition);
		if (s!=null) return s;
		agent.additionPerInterval=addition;
		return null;
	}

	@Override
	protected final boolean runIntern(CallcenterModel editModel, Table table, PrintStream out) {
		for (int i=0;i<generatorColumn.size();i++) {
			int[] cell=getStartCell(generatorColumn.get(i));
			if (cell==null) {out.print(Language.tr("Dialog.Title.Error").toUpperCase()+": "+String.format(Language.tr("CommandLine.Error.InvalidStartCell"),generatorColumn.get(i))); return true;}
			String t=generatorAgents(cell[0],cell[1],editModel,table,generatorType.get(i));
			if (t!=null) {out.println(t); return false;}
			out.println(String.format(Language.tr("CommandLine.GeneratorAgentsAddition.Done"),generatorType.get(i),name[0]));
		}

		return true;
	}
}
