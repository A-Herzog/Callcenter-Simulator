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
import mathtools.Table;
import mathtools.distribution.DataDistributionImpl;
import systemtools.commandline.BaseCommandLineSystem;
import ui.model.CallcenterModel;
import ui.model.CallcenterModelCaller;

/**
 * Lädt per Kommandozeilenbefehl Kundenankunftszeitenverteilungen aus einer Tabelle und fügt diese in ein Callcenter-Modell ein.
 * @author Alexander Herzog
 * @see AbstractGeneratorCommand
 * @see CommandLineSystem
 */
public final class CommandGeneratorCaller extends AbstractGeneratorCommand {
	/**
	 * Konstruktor der Klasse
	 * @param system	Referenz auf das Kommandozeilensystem
	 */
	public CommandGeneratorCaller(final BaseCommandLineSystem system) {
		super(system);
	}

	@Override
	public String[] getKeys() {
		List<String> list=new ArrayList<>();
		list.add(Language.tr("CommandLine.GeneratorClients.Name"));
		for (String s: Language.trOther("CommandLine.GeneratorClients.Name")) if (!list.contains(s)) list.add(s);
		return list.toArray(new String[0]);
	}

	@Override
	public String getShortDescription() {
		return Language.tr("CommandLine.GeneratorClients.Description.Short");
	}

	@Override
	public String[] getLongDescription() {
		return Language.tr("CommandLine.GeneratorClients.Description.Long").split("\n");
	}

	/**
	 * Lädt die Kundenankünfte aus einer Tabelle.
	 * @param row	Startzeile
	 * @param col	Startspalte
	 * @param editModel	Ziel-Callcenter-Modell
	 * @param table	Eingabetabelle
	 * @param type	Name des Kundentyps
	 * @return	Liefert im Erfolgsfall <code>null</code>, sonst eine Fehlermeldung
	 */
	private String generatorCaller(int row, int col, CallcenterModel editModel, Table table, String type) {
		/* Anrufergruppe wählen */
		CallcenterModelCaller caller=null;
		for (int i=0;i<editModel.caller.size();i++) if (editModel.caller.get(i).name.equalsIgnoreCase(type)) {caller=editModel.caller.get(i);}
		if (caller==null) {
			return String.format(Language.tr("CommandLine.Error.NoClientTypeWithName"),type);
		} else {
			/* Daten laden und eintragen */
			DataDistributionImpl dist;
			String s;

			dist=new DataDistributionImpl(CallcenterModelCaller.freshCallsDistMaxX,96);
			s=loadData(row,col,table,dist);
			if (s==null) {
				dist.updateCumulativeDensity();
				caller.freshCallsDist24=null;
				caller.freshCallsDist48=null;
				caller.freshCallsDist96=dist;
				caller.freshCallsCountMean=(int) Math.round(dist.sum());
				return null;
			}

			dist=new DataDistributionImpl(CallcenterModelCaller.freshCallsDistMaxX,48);
			s=loadData(row,col,table,dist);
			if (s==null) {
				dist.updateCumulativeDensity();
				caller.freshCallsDist24=null;
				caller.freshCallsDist48=dist;
				caller.freshCallsDist96=null;
				caller.freshCallsCountMean=(int) Math.round(dist.sum());
				return null;
			}

			dist=new DataDistributionImpl(CallcenterModelCaller.freshCallsDistMaxX,24);
			s=loadData(row,col,table,dist);
			if (s==null) {
				dist.updateCumulativeDensity();
				caller.freshCallsDist24=dist;
				caller.freshCallsDist48=null;
				caller.freshCallsDist96=null;
				caller.freshCallsCountMean=(int) Math.round(dist.sum());
				return null;
			}

			return s;
		}
	}

	@Override
	protected boolean runIntern(CallcenterModel editModel, Table table, PrintStream out) {
		for (int i=0;i<generatorColumn.size();i++) {
			int[] cell=getStartCell(generatorColumn.get(i));
			if (cell==null) {out.print(Language.tr("Dialog.Title.Error").toUpperCase()+": "+String.format(Language.tr("CommandLine.Error.InvalidStartCell"),generatorColumn.get(i))); return false;}
			String t=generatorCaller(cell[0],cell[1],editModel,table,generatorType.get(i));
			if (t!=null) {out.println(t); return false;}
			out.println(String.format(Language.tr("CommandLine.GeneratorClients.Done"),generatorType.get(i)));
		}
		return true;
	}
}
