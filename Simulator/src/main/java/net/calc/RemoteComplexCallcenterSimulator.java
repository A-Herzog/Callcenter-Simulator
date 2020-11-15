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

import simulator.CallcenterSimulatorInterface;
import simulator.Statistics;
import ui.model.CallcenterModel;

/**
 * Ermöglicht eine Simulation auf einem entfernten Rechner.
 * @author Alexander Herzog
 * @version 1.0
 */
public final class RemoteComplexCallcenterSimulator implements CallcenterSimulatorInterface {
	/** Client-System zur Verbindung mit einem entfernten Simulationsserver */
	private final SimClientThread thread;
	/** Wurde die Simulation gestartet? */
	private boolean started;

	/**
	 * Konstruktor der Klasse
	 * @param serverName	Netzwerkname des Servers
	 * @param serverPort	Port auf dem Server
	 * @param serverPassword	Passwort für den Server
	 * @param editModel	Zu simulierendes Modell
	 */
	public RemoteComplexCallcenterSimulator(String serverName, int serverPort, String serverPassword, CallcenterModel editModel) {
		thread=new SimClientThread(serverName,serverPort,serverPassword,editModel);
	}

	/* (non-Javadoc)
	 * @see complexcallcenter.simulator.ComplexCallcenterSimulatorInterface#start()
	 */
	@Override
	public void start(boolean lowPriority) {
		if (started) return;
		started=true;
		thread.start();
	}

	/* (non-Javadoc)
	 * @see complexcallcenter.simulator.ComplexCallcenterSimulatorInterface#cancel()
	 */
	@Override
	public void cancel() {
		thread.interrupt();
	}

	/* (non-Javadoc)
	 * @see complexcallcenter.simulator.ComplexCallcenterSimulatorInterface#isRunning()
	 */
	@Override
	public boolean isRunning() {
		return (!started) || thread.isAlive();
	}

	/* (non-Javadoc)
	 * @see complexcallcenter.simulator.ComplexCallcenterSimulatorInterface#finalizeRun()
	 */
	@Override
	public String finalizeRun() {
		while (!started) try {Thread.sleep(20);} catch (InterruptedException e){}
		while (true) {
			try {
				thread.join();
				break;
			} catch (InterruptedException e1) {continue;}
		}
		return thread.getErrorMessage();
	}

	/* (non-Javadoc)
	 * @see complexcallcenter.simulator.ComplexCallcenterSimulatorInterface#collectStatistic()
	 */
	@Override
	public Statistics collectStatistic() {
		return thread.getStatistics();
	}

	/* (non-Javadoc)
	 * @see complexcallcenter.simulator.ComplexCallcenterSimulatorInterface#getEventCount()
	 */
	@Override
	public long getEventCount() {
		return thread.getEventCount();
	}

	/* (non-Javadoc)
	 * @see complexcallcenter.simulator.ComplexCallcenterSimulatorInterface#getEventsPerSecond()
	 */
	@Override
	public int getEventsPerSecond() {
		return thread.getEventsPerSecond();
	}

	/* (non-Javadoc)
	 * @see complexcallcenter.simulator.ComplexCallcenterSimulatorInterface#getSimDayCount()
	 */
	@Override
	public long getSimDayCount() {
		return thread.getSimDayCount();
	}

	/* (non-Javadoc)
	 * @see complexcallcenter.simulator.ComplexCallcenterSimulatorInterface#getSimDaysCount()
	 */
	@Override
	public long getSimDaysCount() {
		return thread.getSimDaysCount();
	}

	/* (non-Javadoc)
	 * @see complexcallcenter.simulator.ComplexCallcenterSimulatorInterface#setPriority()
	 */
	@Override
	public void setPriority(boolean low) {
		thread.setServerPriority(low);
	}

	@Override
	public File getLogFile() {
		return null;
	}
}
