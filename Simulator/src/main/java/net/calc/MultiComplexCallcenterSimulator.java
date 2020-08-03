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

/**
 * Erlaubt die Koordination mehrere Simulatorobjekte.
 * Das Objekt stellt die Methoden des Interface <code>ComplexCallcenterSimulatorInterface</code>
 * bereit, so als würde es sich um einen einzelnen Simulator handeln. Bei der Statistikabfrage
 * werden die Daten entsprechend vereint.
 * @author Alexander Herzog
 * @version 1.0
 */
public class MultiComplexCallcenterSimulator implements CallcenterSimulatorInterface {
	private final CallcenterSimulatorInterface[] simulators;
	private Statistics statistics=null;

	/**
	 * Konstruktur der Klasse <code>MultiComplexCallcenterSimulator</code>
	 * @param simulators	Array der Simulatorojekte, die in diesem Objekt zusammengefasst werden sollen.
	 */
	public MultiComplexCallcenterSimulator(CallcenterSimulatorInterface[] simulators) {
		this.simulators=simulators;
	}

	/* (non-Javadoc)
	 * @see complexcallcenter.simulator.ComplexCallcenterSimulatorInterface#start(boolean)
	 */
	@Override
	public void start(boolean lowPriority) {
		for (int i=0;i<simulators.length;i++) simulators[i].start(lowPriority);
	}

	/* (non-Javadoc)
	 * @see complexcallcenter.simulator.ComplexCallcenterSimulatorInterface#cancel()
	 */
	@Override
	public void cancel() {
		for (int i=0;i<simulators.length;i++) simulators[i].cancel();

	}

	/* (non-Javadoc)
	 * @see complexcallcenter.simulator.ComplexCallcenterSimulatorInterface#isRunning()
	 */
	@Override
	public boolean isRunning() {
		for (int i=0;i<simulators.length;i++) if (simulators[i].isRunning()) return true;
		return false;
	}

	/* (non-Javadoc)
	 * @see complexcallcenter.simulator.ComplexCallcenterSimulatorInterface#finalizeRun()
	 */
	@Override
	public String finalizeRun() {
		String s;
		for (int i=0;i<simulators.length;i++) {
			s=simulators[i].finalizeRun(); if (s!=null) return s;
		}
		return null;
	}

	/* (non-Javadoc)
	 * @see complexcallcenter.simulator.ComplexCallcenterSimulatorInterface#collectStatistic()
	 */
	@Override
	public Statistics collectStatistic() {
		if (statistics==null) {
			statistics=simulators[0].collectStatistic();
			if (statistics==null) return null;
			int threadCount=statistics.simulationData.runThreads;
			for (int i=1;i<simulators.length;i++) {
				Statistics stat=simulators[i].collectStatistic();
				if (stat==null) return null;
				statistics.addData(stat);
				statistics.simulationData.runOS+=" / "+stat.simulationData.runOS;
				statistics.simulationData.runUser+=" / "+stat.simulationData.runUser;
				threadCount+=stat.simulationData.runThreads;
			}
			statistics.simulationData.runThreads=threadCount;
			statistics.calcWarnings();
			if (simulators.length>1) {
				statistics.finalQueueLengthCalc();
				statistics.finalAgentTimesCalc();
				statistics.calcCallerCosts();
			}
		}
		return statistics;
	}

	/* (non-Javadoc)
	 * @see complexcallcenter.simulator.ComplexCallcenterSimulatorInterface#getEventCount()
	 */
	@Override
	public long getEventCount() {
		long count=0;
		for (int i=0;i<simulators.length;i++) count+=simulators[i].getEventCount();
		return count;
	}

	/* (non-Javadoc)
	 * @see complexcallcenter.simulator.ComplexCallcenterSimulatorInterface#getEventsPerSecond()
	 */
	@Override
	public int getEventsPerSecond() {
		int count=0;
		for (int i=0;i<simulators.length;i++) count+=simulators[i].getEventsPerSecond();
		return count;
	}

	/* (non-Javadoc)
	 * @see complexcallcenter.simulator.ComplexCallcenterSimulatorInterface#getSimDayCount()
	 */
	@Override
	public long getSimDayCount() {
		long count=0;
		for (int i=0;i<simulators.length;i++) count+=simulators[i].getSimDayCount();
		return count;
	}

	/* (non-Javadoc)
	 * @see complexcallcenter.simulator.ComplexCallcenterSimulatorInterface#getSimDaysCount()
	 */
	@Override
	public long getSimDaysCount() {
		long count=0;
		for (int i=0;i<simulators.length;i++) count+=simulators[i].getSimDaysCount();
		return count;
	}

	/* (non-Javadoc)
	 * @see complexcallcenter.simulator.ComplexCallcenterSimulatorInterface#setPriority(boolean)
	 */
	@Override
	public void setPriority(boolean low) {
		for (int i=0;i<simulators.length;i++) simulators[i].setPriority(low);
	}

	/* (non-Javadoc)
	 * @see complexcallcenter.simulator.ComplexCallcenterSimulatorInterface#getLogFile()
	 */
	@Override
	public File getLogFile() {
		return null;
	}

}
