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
	private final static int backgroundMaxCallerDaysPerCore=1000000;

	private final Runnable modelGetter;
	private final Consumer<String> errorInfo;

	private CallcenterModel editModel;
	private CallcenterRunModel runModel;
	private CallcenterSimulatorInterface simulator;
	private Timer updateTimer;
	private final Lock simMutex;
	private boolean noStart=false;
	private boolean fullBackgroundSimulation=true;

	private final int cores=Runtime.getRuntime().availableProcessors();

	/**
	 * Konstruktor der Klasse
	 * @param modelGetter	Runnable �ber das aufgerufen wird, wenn ein neues Modell angefordert werden soll.
	 * @param errorInfo	Consumer, der m�gliche Fehlermeldungen aufnimmt
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
	 * Muss aufgerufen werden, wenn sich das Setup ge�ndert hat.
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
		updateTimer.schedule(new UpdateTimerTask(),1000,1000);

		setErrorLabel(null);
	}

	/**
	 * Pr�ft, ob das in der Hintergrundsimulation enthaltene Modell dem
	 * �bergebenen Callcenter-Modell entspricht.
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

	private void setErrorLabel(String s) {
		if (errorInfo!=null) errorInfo.accept(s);
	}

	/**
	 * Stellt ein neues Modell zur Hintergrundsimulation ein.<br>
	 * Es wird von dieser Methode gepr�ft, ob es evtl. identisch zum
	 * bereits �bergebenen Modell ist.
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

			this.editModel=editModel; /* clone wird nicht ben�tigt, da das Modell bereits vor dem Aufruf von setModel gecloned wurde. */
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
	 * @param needLock	Muss <code>true</code> sein, wenn die Methode von au�erhalb dieser Klasse aufgerufen wird.
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

		/* editModel=null; nein, sonst w�rde der Hintergrundsimulator sofort wieder anlaufen */
	}

	/**
	 * Wurde bereits eine Hintergrundsimulation zu einem begonnen, so kann �ber
	 * diese Methode das zugeh�rige Simulatorobjekt abgerufen werden.
	 * @param editModel	Modell f�r das das Simulatorobjekt angefragt wird
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
	 * Wurde bereits eine Hintergrundsimulation zu einem begonnen, so kann �ber
	 * diese Methode das zugeh�rige Laufzeit-Callcenter-Modell abgerufen werden.
	 * @param editModel	Modell f�r das das Laufzeit-Callcenter-Modell angefragt wird
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

	private final class UpdateTimerTask extends TimerTask {
		@Override
		public void run() {
			modelGetter.run();
		}
	}
}
