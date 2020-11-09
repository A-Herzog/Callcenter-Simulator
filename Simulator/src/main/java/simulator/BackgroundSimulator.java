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
package simulator;

import java.util.Timer;
import java.util.TimerTask;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;
import java.util.function.Consumer;

import net.calc.StartAnySimulator;
import tools.SetupData;
import ui.model.CallcenterModel;
import ui.model.CallcenterRunModel;

/**
 * Simuliert Modelle automatisch im Hintergrund
 * @author Alexander Herzog
 * @version 1.0
 */
public final class BackgroundSimulator {
	/**
	 * Maximalanzahl an zu simulierenden Anrufen pro CPU-Kern,
	 * damit eine Hintergrundsimulation erfolgen darf.
	 */
	private final static int backgroundMaxCallerDaysPerCore=1000000;

	/** Runnable über das aufgerufen wird, wenn ein neues Modell angefordert werden soll. */
	private final Runnable modelGetter;
	/** Consumer, der mögliche Fehlermeldungen aufnimmt */
	private final Consumer<String> errorInfo;

	/** Hinterlegtes Editor-Modell, welches mit neu übergebenen Modellen verglichen wird */
	private CallcenterModel editModel;
	/** Laufzeit-Modell für die Hintergrundsimulation */
	private CallcenterRunModel runModel;
	/** Simulator-Objekt für die Hintergrundsimulation */
	private CallcenterSimulatorInterface simulator;
	/** Timer dessen Aktionsmethode in regelmäßigen Abständen prüft, ob sich das Modell verändert hat */
	private Timer updateTimer;
	/** Sichert parallele Zugriffe auf {@link #simulator} ab */
	private final Lock simMutex;
	/** Legt fest, dass momentan keine Hintergrundsimulation gestartet werden darf */
	private boolean noStart=false;
	/** Soll nur eine Modellprüfung (<code>false</code>) oder eine vollständige Simulation (<code>true</code>) im Hintergrund durchgeführt werden? */
	private boolean fullBackgroundSimulation=true;

	/** Anzahl der verfügbaren CPU-Kerne */
	private final int cores=Runtime.getRuntime().availableProcessors();

	/**
	 * Konstruktor der Klasse
	 * @param modelGetter	Runnable über das aufgerufen wird, wenn ein neues Modell angefordert werden soll.
	 * @param errorInfo	Consumer, der mögliche Fehlermeldungen aufnimmt
	 */
	public BackgroundSimulator(final Runnable modelGetter, final Consumer<String> errorInfo) {
		this.modelGetter=modelGetter;
		this.errorInfo=errorInfo;
		editModel=null;
		simulator=null;
		simMutex=new ReentrantLock();
		loadSetup();
	}

	/**
	 * Einstellungen der Hintergrundsimulation aus dem Setup laden.<br>
	 * Muss aufgerufen werden, wenn sich das Setup geändert hat.
	 */
	public void loadSetup() {
		final SetupData setup=SetupData.getSetup();
		if (updateTimer!=null) {
			updateTimer.cancel();
			updateTimer=null;
			editModel=null;
		}
		boolean ok=setup.backgroundSim;
		if (setup.networkUse) {
			ok=fullBackgroundSimulation && setup.backgroundSimInNetworkMode;
		}
		if (fullBackgroundSimulation && !ok) stop(true);
		fullBackgroundSimulation=ok;

		updateTimer=new Timer("Background-Check",true);
		updateTimer.schedule(new TimerTask() {
			@Override
			public void run() {
				modelGetter.run();
			}
		},1000,1000);

		setErrorLabel(null);
	}

	/**
	 * Prüft, ob das in der Hintergrundsimulation enthaltene Modell dem
	 * übergebenen Callcenter-Modell entspricht.
	 * @param editModel	Modell das mit dem in der Hintergrundsimulation enthaltenen Modell verglichen werden soll
	 * @return	Liefert <code>true</code>, wenn die beiden Modelle inhaltlich identisch sind
	 */
	public boolean equalsCurrentModel(final CallcenterModel editModel) {
		if (editModel==null) {
			return (this.editModel==null);
		} else {
			if (this.editModel==null) return false;
			return this.editModel.equalsCallcenterModel(editModel);
		}
	}

	/**
	 * Liefert über {@link #errorInfo} eine Fehlermeldung aus.
	 * @param s	Auszugebende Fehlermeldung (<code>null</code> für "kein Fehler")
	 */
	private void setErrorLabel(final String s) {
		if (errorInfo!=null) errorInfo.accept(s);
	}

	/**
	 * Stellt ein neues Modell zur Hintergrundsimulation ein.<br>
	 * Es wird von dieser Methode geprüft, ob es evtl. identisch zum
	 * bereits übergebenen Modell ist.
	 * @param editModel	Im Hintergrund zu simulierendes Modell (kann auch <code>null</code> sein)
	 */
	public void setModel(final CallcenterModel editModel) {
		simMutex.lock();
		try {
			int callers=0;

			if (editModel!=null) {
				if (this.editModel!=null && this.editModel.equalsCallcenterModel(editModel)) return;
				for (int i=0;i<editModel.caller.size();i++) if (editModel.caller.get(i).active) callers+=editModel.caller.get(i).freshCallsCountMean;
			}

			stop(false);

			this.editModel=editModel; /* clone wird nicht benötigt, da das Modell bereits vor dem Aufruf von setModel gecloned wurde. */
			if (noStart || editModel==null) {runModel=null; return;}
			runModel=new CallcenterRunModel(this.editModel);
			String error=runModel.checkAndInit(false,false,SetupData.getSetup().strictCheck);
			setErrorLabel(error);
			if (error!=null) {runModel=null; return;}
			if (!fullBackgroundSimulation) return;
			if (editModel.days*callers/cores>backgroundMaxCallerDaysPerCore) return;
			if (runModel.getTotalNumberOfAgents()/cores>1000) return;

			StartAnySimulator starter=new StartAnySimulator(editModel);
			if (starter.check(runModel)==null) {
				simulator=starter.run();
				simulator.start(true);
			}
		} finally {
			simMutex.unlock();
		}
	}

	/**
	 * Bricht die Hintergrundsimulation ab.
	 * @param needLock	Muss <code>true</code> sein, wenn die Methode von außerhalb dieser Klasse aufgerufen wird.
	 */
	public void stop(final boolean needLock) {
		if (needLock) {
			simMutex.lock();
			try {
				if (simulator!=null) simulator.cancel();
				simulator=null;
			} finally {
				simMutex.unlock();
			}
		} else {
			try {
				if (simulator!=null) simulator.cancel();
				simulator=null;
			} finally {}
		}

		/* editModel=null; nein, sonst würde der Hintergrundsimulator sofort wieder anlaufen */
	}

	/**
	 * Wurde bereits eine Hintergrundsimulation zu einem begonnen, so kann über
	 * diese Methode das zugehörige Simulatorobjekt abgerufen werden.
	 * @param editModel	Modell für das das Simulatorobjekt angefragt wird
	 * @return	Liefert, sofern zu dem angegebenen Editor-Modell eine Hintergrundsimulation gestartet wurde, das Simulatorobjekt sonst <code>null</code>
	 */
	public CallcenterSimulatorInterface getSimulatorForModel(final CallcenterModel editModel) {
		simMutex.lock();
		try {
			if (this.editModel==null || !this.editModel.equalsCallcenterModel(editModel)) {
				stop(false);
				noStart=true; modelGetter.run(); noStart=false;
				return null;
			}

			if (simulator!=null)
			{
				simulator.setPriority(false);
				if (!simulator.isRunning()) {
					if (simulator.finalizeRun()!=null) simulator=null;
				}
			}
			CallcenterSimulatorInterface currentSimulator=simulator;
			simulator=null;

			return currentSimulator;
		} finally {
			simMutex.unlock();
		}
	}

	/**
	 * Wurde bereits eine Hintergrundsimulation zu einem begonnen, so kann über
	 * diese Methode das zugehörige Laufzeit-Callcenter-Modell abgerufen werden.
	 * @param editModel	Modell für das das Laufzeit-Callcenter-Modell angefragt wird
	 * @return	Liefert, sofern zu dem angegebenen Editor-Modell eine Hintergrundsimulation gestartet wurde, das Laufzeit-Callcenter-Modell sonst <code>null</code>
	 */
	public CallcenterRunModel getRunModelforModel(final CallcenterModel editModel) {
		simMutex.lock();
		try {
			if (editModel==null || this.editModel==null || !this.editModel.equalsCallcenterModel(editModel) || runModel==null) return null;
			return runModel;
		} finally {
			simMutex.unlock();
		}
	}

	/**
	 * Startet die Hintergrundsimulation (mit dem aktuellen Modell) erneut.
	 */
	public void restart() {
		stop(true);
		CallcenterModel model=this.editModel;
		this.editModel=null;
		setModel(model);
	}
}
