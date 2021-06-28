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

import java.util.ArrayList;
import java.util.List;

import simulator.CallcenterSimulatorInterface;
import simulator.Simulator;
import tools.SetupData;
import ui.model.CallcenterModel;
import ui.model.CallcenterRunModel;

/**
 * Ermöglicht es, den lokalen Rechner und einen entfernten Rechner für die Simulation zusammenzuschalten.
 * @author Alexander Herzog
 * @version 1.0
 */
public class RemoteAndLocalComplexCallcenterSimulator extends MultiComplexCallcenterSimulator {
	/**
	 * Konstruktor der Klasse
	 * @param editModel	Zu simulierendes Modell
	 * @param allowMaxCore	Maximale (lokale) Thread-Anzahl
	 * @param serverParts	Anteile die auf die Server verteilt werden sollen
	 * @param serverNames	Netzwerknamen der Server
	 * @param serverPorts	Ports der Server
	 * @param serverPasswords	Passwörter für die Server
	 */
	public RemoteAndLocalComplexCallcenterSimulator(CallcenterModel editModel, int allowMaxCore, double[] serverParts, String[] serverNames, int[] serverPorts, String[] serverPasswords) {
		super(buildSimulators(editModel,allowMaxCore,serverParts,serverNames,serverPorts,serverPasswords));
	}

	/**
	 * Startet eine lokale Simulation.
	 * @param model	Zu simulierendes Modell
	 * @param allowMaxCore	Maximale (lokale) Thread-Anzahl
	 * @return	Simulatorobjekte
	 * @see #buildSimulators(CallcenterModel, int, double[], String[], int[], String[])
	 */
	private static CallcenterSimulatorInterface getLocal(final CallcenterModel model, final int allowMaxCore) {
		CallcenterRunModel runModel=new CallcenterRunModel(model);
		runModel.checkAndInit(false,false,SetupData.getSetup().strictCheck);
		return new Simulator(allowMaxCore,runModel);
	}

	/**
	 * Startet eine entfernte Simulation.
	 * @param model	Zu simulierendes Modell
	 * @param serverName	Netzwerknamen der Server
	 * @param serverPort	Ports der Server
	 * @param serverPassword	Passwörter für die Server
	 * @return	Simulatorobjekte
	 * @see #buildSimulators(CallcenterModel, int, double[], String[], int[], String[])
	 */
	private static CallcenterSimulatorInterface getRemote(final CallcenterModel model, final String serverName, final int serverPort, final String serverPassword) {
		return new RemoteComplexCallcenterSimulator(serverName,serverPort,serverPassword,model);
	}

	/**
	 * Startet lokale und entfernte Simulatoren.
	 * @param editModel	Zu simulierendes Modell
	 * @param allowMaxCore	Maximale (lokale) Thread-Anzahl
	 * @param serverParts	Anteile die auf die Server verteilt werden sollen
	 * @param serverNames	Netzwerknamen der Server
	 * @param serverPorts	Ports der Server
	 * @param serverPasswords	Passwörter für die Server
	 * @return	Simulatorobjekte
	 */
	private static CallcenterSimulatorInterface[] buildSimulators(final CallcenterModel editModel, final int allowMaxCore, final double[] serverParts, final String[] serverNames, final int[] serverPorts, final String[] serverPasswords) {
		/* Modelle mit verschiedenen Anzahlen an Tagen zusammenstellen (Modell für lokal ganz am Ende) */
		CallcenterModel[] model=new CallcenterModel[serverParts.length+1];
		int remaining=editModel.days;
		double sum=0;
		for (int i=0;i<serverParts.length;i++) sum+=serverParts[i];
		if (sum>=1) for (int i=0;i<serverParts.length;i++) serverParts[i]/=sum;
		for (int i=0;i<serverParts.length;i++) {
			int days=(int)Math.round(Math.floor((editModel.days)*serverParts[i]));
			model[i]=editModel.clone(); model[i].days=days;
			remaining-=days;
		}
		model[serverParts.length]=editModel.clone(); model[serverParts.length].days=remaining;

		/* Simulator-Objekte anlegen */
		List<CallcenterSimulatorInterface> simulators=new ArrayList<>();
		for (int i=0;i<model.length;i++) if (model[i].days>0) {
			if (i==model.length-1) {
				/* lokal */
				simulators.add(getLocal(model[i],allowMaxCore));
			} else {
				/* remote */
				simulators.add(getRemote(model[i],serverNames[i],serverPorts[i],serverPasswords[i]));
			}
		}

		return simulators.toArray(new CallcenterSimulatorInterface[0]);
	}
}
