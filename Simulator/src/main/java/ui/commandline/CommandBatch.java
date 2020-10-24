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
import mathtools.NumberTools;
import simulator.Statistics;
import systemtools.commandline.AbstractCommand;
import ui.model.CallcenterModel;

/**
 * Simuliert alle Modelle in einem Verzeichnis im Batch-Betrieb.
 * @author Alexander Herzog
 * @version 1.0
 * @see AbstractCommand
 * @see AbstractSimulationCommand
 * @see CommandLineSystem
 */
public final class CommandBatch extends AbstractSimulationCommand {
	/** Verzeichnis für die Modelldateien */
	private File batchFolder;

	@Override
	public String[] getKeys() {
		List<String> list=new ArrayList<String>();
		list.add(Language.tr("CommandLine.Batch.Name"));
		for (String s: Language.trOther("CommandLine.Batch.Name")) if (!list.contains(s)) list.add(s);
		return list.toArray(new String[0]);
	}

	@Override
	public String getShortDescription() {
		return Language.tr("CommandLine.Batch.Description.Short");
	}

	@Override
	public String[] getLongDescription() {
		return Language.tr("CommandLine.Batch.Description.Long").split("\n");
	}

	@Override
	public String prepare(String[] additionalArguments, InputStream in, PrintStream out) {
		String s=parameterCountCheck(1,additionalArguments); if (s!=null) return s;
		batchFolder=new File(additionalArguments[0]);
		if (!batchFolder.isDirectory()) return String.format(Language.tr("Batch.Folder.ErrorInputFolderDoesNotExist"),batchFolder.toString());
		return null;
	}

	@Override
	public void run(AbstractCommand[] allCommands, InputStream in, PrintStream out) {
		long startTime=System.currentTimeMillis();

		File[] list=batchFolder.listFiles();
		int simCount=0;
		if (list!=null) for (int i=0;i<list.length;i++) if (list[i].isFile()) {
			File inFile=list[i];

			/* Ausgabedatei bestimmen */
			if (!isModelFile(inFile)) continue;
			int j=inFile.getName().lastIndexOf('.');
			String baseName=inFile.getName();
			if (j>=0) baseName=inFile.getName().substring(0,j);
			File statisticsFile=new File(inFile.getParent(),baseName+Language.tr("Batch.OutputFileNameAddOn")+".xml");
			if (statisticsFile.exists()) {
				out.println(String.format(Language.tr("CommandLine.Batch.OutputFileAlreadyExists"),statisticsFile.getName(),inFile.getName()));
				continue;
			}
			simCount++;

			/* Einzelnes Modell simulieren */
			out.println("Simulation von "+inFile.getName());
			CallcenterModel editModel=new CallcenterModel();
			String s=editModel.loadFromFile(inFile);

			if (s!=null) {out.println(Language.tr("Dialog.Title.Error").toUpperCase()+": "+Language.tr("Batch.LoadError")+": "+s); continue;}
			Statistics statistics=singleSimulation(editModel,true,out);
			if (statistics==null) {out.println(Language.tr("Dialog.Title.Error").toUpperCase()+": "+Language.tr("Batch.Simulation.Canceled.Short")); return;}
			saveStatistics(statistics,statisticsFile,out);
		}
		if (simCount==0)
			out.println(String.format(Language.tr("CommandLine.Batch.Result.NoFiles"),batchFolder.toString()));
		else
			out.println(String.format((simCount==1)?Language.tr("CommandLine.Batch.Result.SuccessSingle"):Language.tr("CommandLine.Batch.Result.SuccessMultiple"),simCount,NumberTools.formatLong((System.currentTimeMillis()-startTime)/1000)));
	}
}
