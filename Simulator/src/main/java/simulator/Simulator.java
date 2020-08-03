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

import java.io.File;
import java.net.InetAddress;
import java.net.UnknownHostException;
import java.text.SimpleDateFormat;
import java.util.Calendar;

import language.Language;
import simcore.SimData;
import simcore.SimulatorBase;
import simcore.eventcache.HashMapEventCache;
import simcore.eventmanager.MultiArrayEventManagerWithHeapSort;
import tools.SetupData;
import ui.VersionConst;
import ui.model.CallcenterModel;
import ui.model.CallcenterRunModel;
import ui.model.CallcenterRunModelCaller;

/**
 * @author Alexander Herzog
 * @version 1.0
 */
public final class Simulator extends SimulatorBase implements CallcenterSimulatorInterface {
	private Statistics statisticResults=null;
	private final File logFile;

	/**
	 * Lesezugriff auf das Daten-Modell aus dem Editor (wird mit der Statistik gespeichert)
	 */
	private final CallcenterModel editModel;

	/**
	 * Lesezugriff auf das bei der Simulation verwendete statische Simulationsdatenobjekt
	 */
	public CallcenterRunModel runModel;

	/**
	 * Legt fest, mit wie vielen Threads der Simulator arbeiten soll.
	 * @param logFile	Gibt den Namen der Log-Datei (oder <code>null</code>, wenn keine Aufzeichnung erfolgen soll) an.
	 * @param runModel	Beinhaltet das aktuell zu simulierende Laufzeit-Modell
	 * @return	Gibt zurück, wie viele Threads unter den als Parameter angegebenen Bedingungen verwendet werden sollen.
	 */
	private static int getThreadCount(File logFile, CallcenterRunModel runModel) {
		if (logFile!=null) return 1;

		/* Mehr Speicher für das JRE ist per -Xmx6G einstellbar, der Simulator verwendet dann auch entsprechend mehr. */

		long MB=Runtime.getRuntime().maxMemory()/1024/1024;
		int countKiloAgents=runModel.getTotalNumberOfAgents()/1000;
		int countKiloCalls=runModel.getTotalNumberOfFreshCalls()/1000;

		return Math.min(getThreadCountByAgents(MB,countKiloAgents),getThreadCountByCalls(MB,countKiloCalls));
	}

	private static int getThreadCountByAgents(long MB, int countKiloAgents) {
		if (MB<3600) {
			if (countKiloAgents>=192) return 1;
			if (countKiloAgents>=96) return 2;
			if (countKiloAgents>=48) return 4;
			if (countKiloAgents>=24) return 8;
			if (countKiloAgents>=12) return 16;
			if (countKiloAgents>=8) return 32;
			return 64;
		}

		if (MB<5500) {
			if (countKiloAgents>=384) return 1;
			if (countKiloAgents>=192) return 2;
			if (countKiloAgents>=96) return 4;
			if (countKiloAgents>=48) return 8;
			if (countKiloAgents>=24) return 16;
			if (countKiloAgents>=12) return 32;
			return 64;
		}

		if (countKiloAgents>=768) return 1;
		if (countKiloAgents>=384) return 2;
		if (countKiloAgents>=192) return 4;
		if (countKiloAgents>=96) return 8;
		if (countKiloAgents>=48) return 16;
		if (countKiloAgents>=24) return 32;
		return 64;
	}

	private static int getThreadCountByCalls(long MB, int countKiloCalls) {
		if (MB<3600) {
			if (countKiloCalls>=7680) return 1;
			if (countKiloCalls>=3840) return 2;
			if (countKiloCalls>=1920) return 4;
			if (countKiloCalls>=960) return 8;
			if (countKiloCalls>=480) return 16;
			if (countKiloCalls>=240) return 32;
			return 64;
		}

		if (countKiloCalls>=15360) return 1;
		if (countKiloCalls>=7680) return 2;
		if (countKiloCalls>=3840) return 4;
		if (countKiloCalls>=1920) return 8;
		if (countKiloCalls>=960) return 16;
		if (countKiloCalls>=480) return 32;
		return 64;
	}

	/**
	 * Konstruktor der Klasse <code>ComplexCallcenterSimulator</code>
	 * @param allowMaxCore	Gibt an, wie viele Rechenthreads maximal genutzt werden sollen. (Es werden generell maximal so viele Thread wie Kerne verwendet.)
	 * @param runModel Referenz auf die statischen Daten für die Simulation.
	 * @param logFile Logdatei, in der die Simulationsausführung aufgezeichnet werden soll. Fehlt dieser Parameter oder ist er gleich <code>null</code>, so erfolgt keine Aufzeichnung (was der Normalfall sein sollte).
	 * @see CallcenterRunModel
	 */
	public Simulator(final int allowMaxCore, final CallcenterRunModel runModel, final File logFile) {
		super(Math.min(Math.min(allowMaxCore,runModel.getDays()),getThreadCount(logFile,runModel)),SetupData.getSetup().moreThreads,false);
		this.editModel=runModel.editModel;
		this.runModel=runModel;
		this.logFile=logFile;
	}

	/**
	 * Konstruktor der Klasse <code>ComplexCallcenterSimulator</code>
	 * @param allowMaxCore	Gibt an, wie viele Rechenthreads maximal genutzt werden sollen. (Es werden generell maximal so viele Thread wie Kerne verwendet.)
	 * @param runModel Referenz auf die statischen Daten für die Simulation.
	 * @see CallcenterRunModel
	 */
	public Simulator(final int allowMaxCore, final CallcenterRunModel runModel) {
		this(allowMaxCore,runModel,null);
	}

	/* (non-Javadoc)
	 * @see simcore.Simulator#getSimDataForThread(int)
	 */
	@Override
	protected SimData getSimDataForThread(final int threadNr, final int threadCount) {
		int callerSum=0;
		for (CallcenterRunModelCaller group: runModel.caller) callerSum+=group.freshCallsCountMean;
		SimData data=new SimulationData(
				new MultiArrayEventManagerWithHeapSort(), /* MultiArray ist ein paar Prozent schneller als MultiPriorityQueue */
				new HashMapEventCache(Math.max(2000,callerSum*11/10)), /* AssociativeEventCache bringt nichts */
				threadNr,
				threadCount,
				runModel
				);
		if (logFile!=null) data.activateLogging(logFile);
		return data;
	}

	private static String serverAddress=null;
	private static String serverOS=null;

	private Statistics collectStatisticIntern() {
		if (threads==null || threads.length==0 || threads[0]==null || threads[0].simData==null) return null;
		Statistics statistics=((SimulationData)threads[0].simData).statisticSimData;
		statistics.editModel=editModel.clone();
		statistics.calcModelAgents();
		statistics.editModel.version=VersionConst.version;

		for (int i=1;i<threadCount;i++)
			statistics.addData(((SimulationData)threads[i].simData).statisticSimData);

		Calendar cal=Calendar.getInstance();
		SimpleDateFormat sdf=new SimpleDateFormat(Language.tr("Simulation.FullDateFormat"));
		String runDate=sdf.format(cal.getTime());
		if (serverAddress==null) {
			serverAddress="";
			try {serverAddress=InetAddress.getLocalHost().getHostName();} catch (UnknownHostException | SecurityException e) {}
		}
		if (serverOS==null) {
			serverOS=System.getProperty("os.name")+" ("+System.getProperty("os.arch")+"), "+System.getProperty("java.vm.name")+" ("+System.getProperty("java.version")+")";
		}

		statistics.simulationData.runUser=System.getProperty("user.name");
		statistics.simulationData.runOS=serverAddress+" - "+serverOS;
		statistics.simulationData.runDate=runDate;
		statistics.simulationData.runTime=runTime;
		statistics.simulationData.runThreads=threadCount;
		statistics.finalQueueLengthCalc();
		statistics.finalAgentTimesCalc();
		statistics.calcCallerCosts();
		statistics.calcWarnings();

		return statistics;
	}

	/* (non-Javadoc)
	 * @see complexcallcenter.simulator.ComplexCallcenterSimulatorInterface#collectStatistic()
	 */
	@Override
	public Statistics collectStatistic() {
		if (statisticResults==null) {
			statisticResults=collectStatisticIntern();
			for (int i=0;i<threads.length;i++) threads[i]=null;
			runModel=null;
		}
		return statisticResults;
	}

	/* (non-Javadoc)
	 * @see complexcallcenter.simulator.ComplexCallcenterSimulatorInterface#getSimDaysCount()
	 */
	@Override
	public long getSimDaysCount() {return editModel.days;}

	@Override
	public File getLogFile() {
		return logFile;
	}
}