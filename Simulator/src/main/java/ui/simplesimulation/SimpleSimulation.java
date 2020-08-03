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
package ui.simplesimulation;

import javax.swing.SwingUtilities;

import language.Language;
import net.calc.StartAnySimulator;
import simulator.CallcenterSimulatorInterface;
import simulator.Statistics;
import ui.model.CallcenterModel;
import ui.model.CallcenterModelSimple;

/**
 * Führt eine Simulation eines Einfach-Modells durch und liefert die unmittelbar relevanten Kenngrößen.
 * @author Alexander Herzog
 * @version 1.0
 */
public class SimpleSimulation extends Thread {
	private final StartAnySimulator starter;
	private CallcenterSimulatorInterface simulator;
	private Runnable whenDone;

	/**
	 * Tritt während der Simulation ein Fehler auf, so gibt dieses Feld die Fehlermeldung aus. Alle anderen Felder sind dann undefiniert. Im Erfolgsfall steht hier <code>null</code>.
	 */
	public String error;

	/**
	 * Laufzeit der Simulation in ms.
	 */
	public long runTime;

	/**
	 * Anteil der erfolgreichen Anrufe.
	 */
	public double anrufeErfolg;

	/**
	 * Anteil der erfolgreichen Kunden.
	 */
	public double kundenErfolg;

	/**
	 * Mittlere Wartezeit der erfolgreichen Anrufe
	 */
	public double anrufeWartezeit;

	/**
	 * Mittlere Abbruchzeit der abgebrochenen Anrufe
	 */
	public double anrufeAbbruchzeit;

	/**
	 * Mittlere Wartezeit über alle Anrufe
	 */
	public double anrufeWartezeitAlle;

	/**
	 * Mittlere Wartezeit der erfolgreichen Kunden
	 */
	public double kundenWartezeit;

	/**
	 * Mittlere Abbruchzeit der abgebrochenen Kunden
	 */
	public double kundenAbbruchzeit;

	/**
	 * Mittlere Wartezeit über alle Kunden
	 */
	public double kundenWartezeitAlle;

	/**
	 * Mittlerew Verweilzeit der erfolgreichen Anrufe
	 */
	public double anrufeVerweilzeit;

	/**
	 * Mittlere Verweilzeit über alle Anrufe
	 */
	public double anrufeVerweilzeitAlle;

	/**
	 * Mittlere Verweilzeit der erfolgreichen Kunden
	 */
	public double kundenVerweilzeit;

	/**
	 * Mittlere Verweilzeit über alle Kunden
	 */
	public double kundenVerweilzeitAlle;

	/**
	 * Service-Level bezogen auf die erfolgreichen Anrufe
	 */
	public double anrufeServiceLevelErfolg;

	/**
	 * Service-Level bezogen auf alle Anrufe
	 */
	public double anrufeServiceLevelAlle;

	/**
	 * Service-Level bezogen auf die erfolgreichen Kunden
	 */
	public double kundenServiceLevelErfolg;

	/**
	 * Service-Level bezogen auf alle Kunden
	 */
	public double kundenServiceLevelAlle;

	/**
	 * Auslastung der Agenten
	 */
	public double auslastung;

	/**
	 * Konstruktor der Klasse <code>SimpleSimulation</code>
	 * @param input	Objekt vom Typ <code>SimpleSimulationInput</code> das die Eingabeparameter für die Einfach-Simulation enthält. Wird hier <code>null</code> übergeben, so wird dies erkannt und als Fehlermeldung vermerkt.
	 */
	public SimpleSimulation(final SimpleSimulationInput input) {
		super();
		if (input==null) {
			starter=null;
			error=Language.tr("LoadCalculator.InvalidInput");
		} else {
			CallcenterModel model=CallcenterModelSimple.getModel(input.lambda,input.EWT,input.StdWT,input.retryProbability,input.ERetry,input.c,input.ES,input.StdS,input.ES2,input.StdS2,input.continueProbability);
			starter=new StartAnySimulator(model);
		}
	}

	private void calcStatistic(final Statistics statistic) {
		runTime=statistic.simulationData.runTime;

		anrufeErfolg=((double)statistic.kundenGlobal.anrufeErfolg)/Math.max(1,statistic.kundenGlobal.anrufe);
		kundenErfolg=((double)statistic.kundenGlobal.kundenErfolg)/Math.max(1,statistic.kundenGlobal.kunden);

		anrufeWartezeit=((double)statistic.kundenGlobal.anrufeWartezeitSum)/Math.max(1,statistic.kundenGlobal.anrufeErfolg);
		anrufeAbbruchzeit=((double)statistic.kundenGlobal.anrufeAbbruchzeitSum)/Math.max(1,statistic.kundenGlobal.anrufeAbbruch);
		anrufeWartezeitAlle=((double)statistic.kundenGlobal.anrufeWartezeitSum+statistic.kundenGlobal.anrufeAbbruchzeitSum)/Math.max(1,statistic.kundenGlobal.anrufe);

		kundenWartezeit=((double)statistic.kundenGlobal.kundenWartezeitSum)/Math.max(1,statistic.kundenGlobal.kundenErfolg);
		kundenAbbruchzeit=((double)statistic.kundenGlobal.kundenAbbruchzeitSum)/Math.max(1,statistic.kundenGlobal.kundenAbbruch);
		kundenWartezeitAlle=((double)statistic.kundenGlobal.kundenWartezeitSum+statistic.kundenGlobal.kundenAbbruchzeitSum)/Math.max(1,statistic.kundenGlobal.kunden);

		anrufeVerweilzeit=((double)statistic.kundenGlobal.anrufeVerweilzeitSum)/Math.max(1,statistic.kundenGlobal.anrufeErfolg);
		anrufeVerweilzeitAlle=((double)statistic.kundenGlobal.anrufeVerweilzeitSum+statistic.kundenGlobal.anrufeAbbruchzeitSum)/Math.max(1,statistic.kundenGlobal.anrufe);
		kundenVerweilzeit=((double)statistic.kundenGlobal.kundenVerweilzeitSum)/Math.max(1,statistic.kundenGlobal.kundenErfolg);
		kundenVerweilzeitAlle=((double)statistic.kundenGlobal.kundenVerweilzeitSum+statistic.kundenGlobal.kundenAbbruchzeitSum)/Math.max(1,statistic.kundenGlobal.kunden);

		anrufeServiceLevelErfolg=((double)statistic.kundenGlobal.anrufeServicelevel)/Math.max(1,statistic.kundenGlobal.anrufeErfolg);
		anrufeServiceLevelAlle=((double)statistic.kundenGlobal.anrufeServicelevel)/Math.max(1,statistic.kundenGlobal.anrufe);
		kundenServiceLevelErfolg=((double)statistic.kundenGlobal.kundenServicelevel)/Math.max(1,statistic.kundenGlobal.kundenErfolg);
		kundenServiceLevelAlle=((double)statistic.kundenGlobal.kundenServicelevel)/Math.max(1,statistic.kundenGlobal.kunden);

		auslastung=((double)statistic.agentenGlobal.arbeitGesamt+statistic.agentenGlobal.postProcessingGesamt)/(statistic.agentenGlobal.leerlaufGesamt+statistic.agentenGlobal.technischerLeerlaufGesamt+statistic.agentenGlobal.arbeitGesamt+statistic.agentenGlobal.postProcessingGesamt);
	}

	/**
	 * Startet die Einfach-Simulation
	 * @param whenDone	Objekt vom Typ <code>Runnable</code> das aufgerufen wird, wenn die Simulation beendet ist.
	 */
	public void start(final Runnable whenDone) {
		this.whenDone=whenDone;
		if (starter==null) {SwingUtilities.invokeLater(whenDone); return;}

		error=starter.check(); if (error!=null) {SwingUtilities.invokeLater(whenDone); return;}
		simulator=starter.run();
		simulator.start(false);
		super.start();
	}

	@Override
	public void run() {
		error=simulator.finalizeRun();
		if (error==null) calcStatistic(simulator.collectStatistic());
		SwingUtilities.invokeLater(whenDone);
	}
}