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
package net.calc;

import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import language.Language;
import mathtools.NumberTools;
import simulator.CallcenterSimulatorInterface;
import simulator.Simulator;
import tools.SetupData;
import ui.model.CallcenterModel;
import ui.model.CallcenterRunModel;

/**
 * Factory-Klasse zum Starten von Simulator instanzen
 * @author Alexander Herzog
 * @version 1.0
 */
public final class StartAnySimulator {
	/** Zu prüfendes und zu simulierendes Modell */
	private final CallcenterModel editModel;
	/** Optionale Logdatei (kann <code>null</code> sein) */
	private final File logFile;

	/** Anteile die auf die Server verteilt werden sollen */
	private double[] networkParts=null;
	/** Netzwerknamen der Server */
	private String[] networkServers=null;
	/** Ports der Server */
	private int[] networkPorts=null;
	/** Passwörter für die Server */
	private String[] networkPasswords=null;
	/** Zu dem Editor-Modell erstelltes zugehöriges Laufzeitmodell */
	private CallcenterRunModel runModel;

	/**
	 * Konstruktor der Klasse
	 * @param editModel	Zu prüfendes und zu simulierendes Modell
	 * @param logFile	Optionale Logdatei (kann <code>null</code> sein)
	 */
	public StartAnySimulator(final CallcenterModel editModel, final File logFile) {
		this.editModel=editModel;
		this.logFile=logFile;
	}

	/**
	 * Konstruktor der Klasse
	 * @param editModel	Zu prüfendes und zu simulierendes Modell
	 */
	public StartAnySimulator(final CallcenterModel editModel) {
		this(editModel,null);
	}

	/**
	 * Überprüft die Einstellungen zu den Netzwerkservern.
	 * @return	Liefert <code>true</code>, wenn die Einstellungen in Ordnung sind.
	 */
	private String checkNetwork() {
		final SetupData setup=SetupData.getSetup();

		if (setup.networkServer.isEmpty())
			return Language.tr("Server.Error.NoServerAddress");

		networkServers=setup.networkServer.split(";");

		String[] ports=setup.networkPort.split(";");
		if (ports.length<networkServers.length) {
			List<String> portsList=new ArrayList<>(Arrays.asList(ports));
			while (portsList.size()<networkServers.length) portsList.add("6783");
			ports=portsList.toArray(String[]::new);
		}
		networkPorts=new int[ports.length];
		for (int i=0;i<ports.length;i++) {
			Integer I=NumberTools.getNotNegativeInteger(ports[i]);
			if (I==null || I<1) return String.format(Language.tr("Server.Error.PortNumberInvalid"),ports[i],""+(i+1));
			networkPorts[i]=I;
		}

		networkPasswords=setup.networkPassword.split(";");
		if (networkPasswords.length<networkServers.length) {
			List<String> passwordsList=new ArrayList<>(Arrays.asList(networkPasswords));
			while (passwordsList.size()<networkServers.length) passwordsList.add("");
			networkPasswords=passwordsList.toArray(String[]::new);
		}

		String[] parts=setup.networkPart.split(";");
		if (parts.length<networkServers.length) {
			List<String> partsList=new ArrayList<>(Arrays.asList(parts));
			while (partsList.size()<networkServers.length) partsList.add("1");
			parts=partsList.toArray(String[]::new);
		}
		networkParts=new double[parts.length];
		for (int i=0;i<parts.length;i++) {
			Double D=NumberTools.getProbability(parts[i]);
			if (D==null) return String.format(Language.tr("Server.Error.ServerPartInvalid"),parts[i],""+(i+1));
			networkParts[i]=D;
		}

		return null;
	}

	/**
	 * Prüft, ob das im Konstruktor angegebene Modell simuliert werden kann.
	 * @return	Gibt im Erfolgfall <code>null</code> zurück, sonst eine Fehlermeldung
	 */
	public String check() {
		return check(null);
	}

	/**
	 * Prüft, ob das angegebene Modell simuliert werden kann.
	 * @param checkedRunModel	Zu prüfendes Modell (wird <code>null</code> übergeben, so wird das im Konstruktor übergebene Modell geprüft)
	 * @return	Gibt im Erfolgsfall <code>null</code> zurück, sonst eine Fehlermeldung
	 */
	public String check(final CallcenterRunModel checkedRunModel) {
		final SetupData setup=SetupData.getSetup();
		String s;

		s=(setup.networkUse && logFile==null)?checkNetwork():null;
		if (s!=null) return s;

		if (checkedRunModel==null) {
			runModel=new CallcenterRunModel(editModel);
			s=runModel.checkAndInit(false,false,SetupData.getSetup().strictCheck);
			if (s!=null) return s;
		} else {
			runModel=checkedRunModel;
		}

		return null;
	}

	/**
	 * Startet die Simulation
	 * @return	Interface über das auf den lokalen Simulator oder den Wrapper für die Netzwerksimulation zugegriffen werden kann
	 * @see CallcenterSimulatorInterface
	 */
	public CallcenterSimulatorInterface run() {
		return run(true);
	}

	/**
	 * Startet die Simulation
	 * @param allowIncreaseDays	Darf die Anzahl an simulierten Tagen erhöht werden? (Wenn dies in der Konfiguration eingestellt ist.)
	 * @return	Interface über das auf den lokalen Simulator oder den Wrapper für die Netzwerksimulation zugegriffen werden kann
	 * @see CallcenterSimulatorInterface
	 */
	public CallcenterSimulatorInterface run(final boolean allowIncreaseDays) {
		final SetupData setup=SetupData.getSetup();

		CallcenterSimulatorInterface simulator;

		if (networkParts!=null) {
			double s=0;
			for (int i=0;i<networkParts.length;i++) s+=networkParts[i];
			if (s>1) {
				for (int i=0;i<networkParts.length;i++) networkParts[i]/=s;
			}
			simulator=new RemoteAndLocalComplexCallcenterSimulator(editModel,setup.getRealMaxThreadNumber(),networkParts,networkServers,networkPorts,networkPasswords);
		} else {
			int threadCount=Math.min(setup.getRealMaxThreadNumber(),Runtime.getRuntime().availableProcessors());
			if (setup.increaseNumberOfDays && allowIncreaseDays) {
				int days=runModel.editModel.days;
				while (days%threadCount!=0) days++;
				runModel.editModel.days=days;
				runModel.setDays(days);
			}
			simulator=new Simulator(threadCount,runModel,logFile);
		}

		return simulator;
	}
}
