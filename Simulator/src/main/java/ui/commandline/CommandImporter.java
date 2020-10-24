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
import mathtools.MultiTable;
import systemtools.commandline.AbstractCommand;
import ui.importer.ImporterData;
import ui.model.CallcenterModel;

/**
 * Importiert Daten gem‰ﬂ einer Schablone aus einer Tabelle in ein Modell
 * @author Alexander Herzog
 * @version 1.0
 * @see AbstractCommand
 * @see CommandLineSystem
 */
public class CommandImporter extends AbstractSimulationCommand {
	/** Eingabe-Modelldatei */
	private File modelFileIn;
	/** Ausgabe-Modelldatei */
	private File modelFileOut;
	/** Eingabe-Importschablone */
	private File importerFile;
	/** Eingabe-Tabelle */
	private File tableFile;

	@Override
	public String[] getKeys() {
		List<String> list=new ArrayList<String>();
		list.add(Language.tr("CommandLine.Import.Name"));
		for (String s: Language.trOther("CommandLine.Import.Name")) if (!list.contains(s)) list.add(s);
		return list.toArray(new String[0]);
	}

	@Override
	public String getShortDescription() {
		return Language.tr("CommandLine.Import.Description.Short");
	}

	@Override
	public String[] getLongDescription() {
		return Language.tr("CommandLine.Import.Description.Long").split("\n");
	}

	@Override
	public final String prepare(String[] additionalArguments, InputStream in, PrintStream out) {
		String s=parameterCountCheck(4,additionalArguments); if (s!=null) return s;
		modelFileIn=new File(additionalArguments[0]);
		importerFile=new File(additionalArguments[1]);
		tableFile=new File(additionalArguments[2]);
		modelFileOut=new File(additionalArguments[3]);
		if (!modelFileIn.isFile()) return String.format(Language.tr("CommandLine.Error.File.InputDoesNotExist"),modelFileIn);
		if (!isModelFile(modelFileIn)) return String.format(Language.tr("CommandLine.Error.File.InputNoValidCallCenterModel"),modelFileIn);
		if (!importerFile.isFile()) return String.format(Language.tr("CommandLine.Error.File.ImporterInputDoesNotExist"),importerFile);
		if (!tableFile.isFile()) return String.format(Language.tr("CommandLine.Error.File.TableDoesNotExist"),tableFile);
		if (modelFileOut.exists()) return String.format(Language.tr("CommandLine.Error.File.OutputAlreadyExist"),modelFileOut);
		return null;
	}

	@Override
	public void run(AbstractCommand[] allCommands, InputStream in, PrintStream out) {
		CallcenterModel modelIn=new CallcenterModel();
		String s=modelIn.loadFromFile(modelFileIn);
		if (s!=null) {
			out.println(Language.tr("Dialog.Title.Error").toUpperCase()+"; "+s);
			return;
		}

		ImporterData data=new ImporterData();
		String error=data.loadFromFile(importerFile);
		if (error!=null) {
			out.println(Language.tr("Dialog.Title.Error").toUpperCase()+": "+error);
			return;
		}

		MultiTable table=new MultiTable();
		if (!table.load(tableFile)) {
			out.println(Language.tr("Dialog.Title.Error").toUpperCase()+": "+String.format(Language.tr("CommandLine.Import.CouldNotLoadTable"),tableFile));
			return;
		}

		CallcenterModel modelOut=data.process(modelIn,table);
		if (modelOut==null) {
			out.println(Language.tr("Dialog.Title.Error").toUpperCase()+": "+data.getProcessError());
			return;
		}

		if (!modelOut.saveToFile(modelFileOut)) {
			out.println(Language.tr("Dialog.Title.Error").toUpperCase()+": "+String.format(Language.tr("CommandLine.Import.CouldNotSaveResult"),modelOut));
			return;
		}

		out.println((data.records.size()==1)?Language.tr("CommandLine.Import.Done.Single"):String.format(Language.tr("CommandLine.Import.Done.Multiple"),data.records.size()));
	}
}