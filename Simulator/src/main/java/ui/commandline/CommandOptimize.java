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
import ui.model.CallcenterModel;
import ui.optimizer.OptimizeSetup;
import ui.optimizer.Optimizer;

/**
 * Führt eine Optimierung eines Callcenter-Modells durch.
 * @author Alexander Herzog
 * @version 1.0
 * @see AbstractCommand
 * @see AbstractSimulationCommand
 * @see CommandLineSystem
 */
public final class CommandOptimize extends AbstractSimulationCommand {
	/** Eingabe-Modelldatei */
	private File modelFile;
	/** Eingabe-Optimierer-Konfiguration */
	private File optimizeFile;
	/** Ausgabe-Statistikdatei */
	private File statisticsFile;

	/**
	 * Konstruktor der Klasse
	 * @param system	Referenz auf das Kommandozeilensystem
	 */
	public CommandOptimize(final BaseCommandLineSystem system) {
		super(system);
	}

	@Override
	public String[] getKeys() {
		List<String> list=new ArrayList<>();
		list.add(Language.tr("CommandLine.Optimizer.Name"));
		for (String s: Language.trOther("CommandLine.Optimizer.Name")) if (!list.contains(s)) list.add(s);
		return list.toArray(String[]::new);
	}

	@Override
	public String getShortDescription() {
		return Language.tr("CommandLine.Optimizer.Description.Short");
	}

	@Override
	public String[] getLongDescription() {
		return Language.tr("CommandLine.Optimizer.Description.Long").split("\n");
	}

	@Override
	public String prepare(String[] additionalArguments, InputStream in, PrintStream out) {
		String s=parameterCountCheck(3,additionalArguments); if (s!=null) return s;
		modelFile=new File(additionalArguments[0]);
		optimizeFile=new File(additionalArguments[1]);
		statisticsFile=new File(additionalArguments[2]);
		if (!modelFile.isFile()) return String.format(Language.tr("CommandLine.Error.File.InputDoesNotExist"),modelFile);
		if (!isModelFile(modelFile)) return String.format(Language.tr("CommandLine.Error.File.InputNoValidCallCenterModel"),modelFile);
		if (!optimizeFile.isFile()) return String.format(Language.tr("CommandLine.Error.File.ConfigDoesNotExist"),optimizeFile);
		if (statisticsFile.exists()) return String.format(Language.tr("CommandLine.Error.File.OutputAlreadyExist"),statisticsFile);
		return null;
	}

	/**
	 * Führt die eigentliche Optimierung aus.
	 * @param optimizer	Optimierer-System
	 * @param out	Ausgabe für Fehlermeldungen
	 */
	private void runOptimizer(final Optimizer optimizer, final PrintStream out) {
		boolean firstRun=true;
		int changeNeeded=0;
		while (changeNeeded!=0 || firstRun) {
			firstRun=false;
			optimizer.simulationStart(changeNeeded);
			out.println(String.format(Language.tr("CommandLine.Optimizer.RunStarted"),optimizer.getCurrentRunNr()));
			while (optimizer.getSimulator().isRunning()) {
				try {Thread.sleep(250);} catch (InterruptedException e) {}
			}
			changeNeeded=optimizer.simulationDone();
			if (optimizer.isCanceled()) {
				out.println(Language.tr("Dialog.Title.Error").toUpperCase()+": "+Language.tr("CommandLine.Optimizer.Canceled"));
				return;
			}
		}
		out.println(String.format(Language.tr("CommandLine.Optimizer.Done"),optimizer.getResults().runTime));
		if (!optimizer.getResults().saveToFile(statisticsFile))	{out.println(Language.tr("Dialog.Title.Error").toUpperCase()+": "+Language.tr("CommandLine.Error.UnableToSaveStatistic")); return;}
	}

	@Override
	public void run(AbstractCommand[] allCommands, InputStream in, PrintStream out) {
		CallcenterModel editModel=new CallcenterModel();
		String s=editModel.loadFromFile(modelFile);
		if (s!=null) {out.println(Language.tr("Dialog.Title.Error").toUpperCase()+": "+Language.tr("CommandLine.Error.LoadingModel")+" "+s); return;}
		OptimizeSetup optimizeSetup=new OptimizeSetup();
		s=optimizeSetup.loadFromFile(optimizeFile);
		if (s!=null) {out.println(Language.tr("Dialog.Title.Error").toUpperCase()+": "+Language.tr("CommandLine.Optimizer.ErrorLoadingSetup")+": "+s); return;}
		if (optimizeSetup.optimizeMaxValue>=0) out.println(Language.tr("Dialog.Title.Warning").toUpperCase()+": "+Language.tr("Optimizer.UpDownWarning.Info"));
		Optimizer optimizer=new Optimizer(null,out,editModel,optimizeSetup);
		s=optimizer.checkAndInit();
		if (s!=null) {out.println(Language.tr("Dialog.Title.Error").toUpperCase()+": "+Language.tr("CommandLine.Optimizer.InitializationError")+": "+s); return;}
		runOptimizer(optimizer,out);
	}
}
