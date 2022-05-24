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
import systemtools.commandline.AbstractCommand;
import ui.connected.ConnectedModel;
import ui.connected.ConnectedModelUebertrag;
import ui.connected.ConnectedSimulation;

/**
 * Führt eine verknüpfte Simulation aus, entweder basierend auf einer xml-Datei oder basierend
 * auf den weiteren Kommandozeilenparametern.
 * @author Alexander Herzog
 * @version 1.0
 * @see AbstractCommand
 * @see AbstractSimulationCommand
 * @see CommandLineSystem
 */
public final class CommandConnected extends AbstractSimulationCommand {
	/** Verbundenes Modell */
	private ConnectedModel model;

	/**
	 * Konstruktor der Klasse
	 */
	public CommandConnected() {
		/*
		 * Wird nur benötigt, um einen JavaDoc-Kommentar für diesen (impliziten) Konstruktor
		 * setzen zu können, damit der JavaDoc-Compiler keine Warnung mehr ausgibt.
		 */
	}

	@Override
	public String[] getKeys() {
		List<String> list=new ArrayList<>();
		list.add(Language.tr("CommandLine.Connected.Name"));
		for (String s: Language.trOther("CommandLine.Connected.Name")) if (!list.contains(s)) list.add(s);
		return list.toArray(new String[0]);
	}

	@Override
	public String getShortDescription() {
		return Language.tr("CommandLine.Connected.Description.Short");
	}

	@Override
	public String[] getLongDescription() {
		return Language.tr("CommandLine.Connected.Description.Long").split("\n");
	}

	/**
	 * Initialisiert die Modelleinstellungen basiertend auf den angegebenen Kommandozeilenparametern
	 * @param additionalArguments	Kommandozeilenparameter
	 * @return	Liefert im Erfolgsfall <code>null</code>, sonst eine Fehlermeldung
	 * @see #prepare(String[], InputStream, PrintStream)
	 */
	private String initModelFromArguments(String[] additionalArguments) {
		if (additionalArguments.length<3) return String.format(Language.tr("CommandLine.Connected.Error.ParameterNumber1"),additionalArguments.length);
		if (additionalArguments.length%2==0) return String.format(Language.tr("CommandLine.Connected.Error.ParameterNumber2"),additionalArguments.length);

		File statisticsInputFile=new File(additionalArguments[1]);
		if (!statisticsInputFile.exists()) return Language.tr("CommandLine.Connected.Error.InputStatisticFile.DoesNotExist");
		if (!isStatisticFile(statisticsInputFile)) return Language.tr("CommandLine.Connected.Error.InputStatisticFile.IsNoStatisticFile");

		model.statisticsDay0=statisticsInputFile.toString();
		model.addRecord();
		model.models.set(0,additionalArguments[0]);
		model.statistics.set(0,additionalArguments[2]);
		int nr=3;
		while (nr<additionalArguments.length-1) {
			String s=additionalArguments[nr];
			Double D=NumberTools.getProbability(additionalArguments[nr+1]);
			if (D==null || D<0 || D>1) return String.format(Language.tr("CommandLine.Connected.Error.ParameterInvalid"),nr+2,additionalArguments[nr+1]);
			model.uebertrag.get(0).put(s,new ConnectedModelUebertrag(D));
			nr+=2;
		}

		return null;
	}

	@Override
	public String prepare(String[] additionalArguments, InputStream in, PrintStream out) {
		model=new ConnectedModel();

		if (additionalArguments.length==1) {
			File modelFile=new File(additionalArguments[0]);
			if (!modelFile.isFile()) return String.format(Language.tr("CommandLine.Error.File.InputDoesNotExist"),modelFile.toString());
			String s=model.loadFromFile(modelFile);
			if (s!=null) return String.format(Language.tr("CommandLine.Connected.Error.LoadingConfiguration"),modelFile)+"\n"+s;
			return null;
		} else {
			return initModelFromArguments(additionalArguments);
		}
	}

	@Override
	public void run(AbstractCommand[] allCommands, InputStream in, PrintStream out) {
		ConnectedSimulation connectedSimulation=new ConnectedSimulation();
		String s=connectedSimulation.loadData(model); if (s!=null) {out.println(Language.tr("Dialog.Title.Error").toUpperCase()+": "+Language.tr("CommandLine.Connected.Error.Simulation.Initialization")+": "+s); return;}
		int day=0;
		while (true) {
			day++;
			s=connectedSimulation.initNextSimulation(null);
			if (s!=null) {
				if (s.isEmpty()) break; /* fertig */
				out.println(Language.tr("Dialog.Title.Error").toUpperCase()+": "+String.format(Language.tr("CommandLine.Connected.Error.StartingDay"),day)+": "+s); return;
			}
			out.println(String.format(Language.tr("Connected.Progress"),day,connectedSimulation.getSimCount()));
			waitForSimulationDone(connectedSimulation.getSimulator(),false,out);
			s=connectedSimulation.doneSimulation(); if (s!=null) {out.println(Language.tr("Dialog.Title.Error").toUpperCase()+": "+Language.tr("CommandLine.Connected.Error.Simulation.Done")+": "+s); return;}
		}
		out.println(Language.tr("CommandLine.Connected.SimulationDone"));
	}

}
