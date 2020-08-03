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

import java.util.ArrayList;
import java.util.List;

import org.apache.commons.math3.util.FastMath;

import mathtools.distribution.DataDistributionImpl;
import mathtools.distribution.tools.DistributionRandomNumber;
import simcore.SimData;
import simcore.eventcache.EventCache;
import simcore.eventmanager.EventManager;
import simulator.RunData.AgentRecord;
import simulator.RunData.CallerRecord;
import simulator.Statistics.KundenDaten;
import simulator.events.AgentReadyEvent;
import simulator.events.CallEvent;
import simulator.events.StopTestEvent;
import ui.model.CallcenterModel;
import ui.model.CallcenterRunModel;
import ui.model.CallcenterRunModelAgent;
import ui.model.CallcenterRunModelCallcenter;
import ui.model.CallcenterRunModelCaller;

/**
 * Die Klasse <code>ComplexSimData</code> stellt alle während der
 * Simulation notwendigen Daten zur Verfügung. Dies umfasst sowohl
 * das statische Callcenter-Modell als auch die Statistikdaten.
 * @author Alexander Herzog
 * @version 1.0
 */
public final class SimulationData extends SimData {
	/**
	 * Enthält eine Referenz auf das für alle Threads globale <code>CallcenterModel</code>-Objekt.
	 * @see CallcenterModel
	 */
	public CallcenterRunModel model;

	/**
	 * Enthält eine Referenz auf die dynamischen Simulationsdaten.
	 * @see RunData
	 */
	public RunData dynamicSimData;

	/**
	 * Enthält eine Referenz aug die gesammelten Statistikdaten.
	 * @see Statistics
	 */
	public final Statistics statisticSimData;

	/**
	 * Bremst das Anlagen von Anrufer-Objekten bei der Initialisierung
	 * des ersten Tages bei Verwendung der Hintergrundsimulation um
	 * die angegebene Anzahl an Millisekunden.
	 */
	private static final int backgroundModeDelay=5;

	/**
	 * Konstruktor der Klasse <code>ComplexSimData</code>
	 * @param eventManager	Referenz auf das zur Simulation zu verwendende EventManager-Objekt
	 * @param eventCache	Referenz auf das zur Simulation zu verwendende EventCache-Objekt
	 * @param threadNr	Nummer des Simulationsthreads (von 0 an gezählt)
	 * @param threadCount	Anzahl der Rechenthreads (wird benötigt, um aus der Gesamtzahl der zu simulierenden Anrufe auf die in diesem Thread zu simulierende Anruferanzahl zu schließen)
	 * @param model	Referenz auf das Objekt, das die statischen Daten für die Simulation enthält
	 */
	public SimulationData(final EventManager eventManager, final EventCache eventCache, final int threadNr, final int threadCount, final CallcenterRunModel model) {
		super(eventManager,eventCache,threadNr,threadCount);
		this.model=model;

		simDaysByOtherThreads=0;
		for (int i=0;i<threadNr;i++) {
			simDaysByOtherThreads+=model.getDays()/threadCount;
			if (model.getDays()%threadCount>=threadNr+1) simDaysByOtherThreads++;
		}

		simDays=model.getDays()/threadCount;
		if (model.getDays()%threadCount>=threadNr+1) simDays++;

		dynamicSimData=new RunData(model);
		statisticSimData=new Statistics(null,model,threadCount,simDays);
	}

	private List<CallEvent> initCallsList=null;

	private void initCalls(final boolean firstDay, final long dayGlobal, boolean backgroundMode) {
		dynamicSimData.cacheAllCallerRecords();

		if (initCallsList==null) {
			int sum=0;
			for (CallcenterRunModelCaller group: model.caller) sum+=group.freshCallsCountMean;
			initCallsList=new ArrayList<CallEvent>(sum*11/10);
		} else {
			initCallsList.clear();
		}

		/* Liste mit allen Anrufern anlegen */
		for (int i=0;i<model.caller.length;i++) {

			if (backgroundMode && firstDay && i>0 && backgroundModeDelay>0) try {Thread.sleep(backgroundModeDelay);} catch (InterruptedException e) {}

			CallcenterRunModelCaller group=model.caller[i];
			KundenDaten k2=statisticSimData.kundenProTyp[i];

			assert(i==group.index);

			/* Echte Erstanrufer und additionalCaller anlegen */
			final DataDistributionImpl freshCallsDist=group.freshCalls;
			int freshCallsCount=group.freshCallsCountMean;
			if (group.freshCallsCountSD>0) {
				freshCallsCount=(int) Math.max(0,Math.round(freshCallsCount+DistributionRandomNumber.getStdNormalRandom()*group.freshCallsCountSD));
			}
			if (group.freshCallsCountAddByDay!=null && group.freshCallsCountAddByDay.length>dayGlobal) freshCallsCount+=group.freshCallsCountAddByDay[(int)dayGlobal];
			for (int j=0;j<freshCallsCount;j++) {
				/*
				final CallerRecord record=dynamicSimData.getNewCallerRecord();
				record.callerType=group;
				record.statisticClient=k2;
				record.statisticCall=k2;
				 */
				final CallEvent call;
				if (firstDay) {
					/* Am ersten Simulationstag kann noch kein Event im Cache sein, dann ist das direkt Anlegen schneller. */
					call=new CallEvent();
				} else {
					call=(CallEvent)getEvent(CallEvent.class);
				}
				long time=FastMath.round(1000*freshCallsDist.random(DistributionRandomNumber.generator));
				call.init(time);
				/* call.callerRecord=record; */
				call.callerRecord=null;
				call.callerGroup=group;
				initCallsList.add(call);
			}

			/* Wiederholer vom Vortag anlegen */
			if (group.freshCallsSheduledByDay!=null && group.freshCallsSheduledByDay.length>dayGlobal) {
				final long[] retryTimes=group.freshCallsSheduledByDay[(int)dayGlobal];
				for (int j=0;j<retryTimes.length;j++) {
					final CallerRecord record=dynamicSimData.getNewCallerRecord();
					record.callerType=group;
					record.statisticClient=k2;
					record.statisticCall=k2;
					record.initialRestWaitingTolerance=-1;
					CallEvent call=(CallEvent)getEvent(CallEvent.class);
					long time=retryTimes[j];
					call.init(time);
					call.callerRecord=record;
					initCallsList.add(call);
				}
			}

			/* Übertragkunden vom Vortag sind sofort da */
			if (group.freshCallsInitialWaitingByDay!=null && group.freshCallsInitialWaitingByDay.length>dayGlobal) {
				final long[] waiting=group.freshCallsInitialWaitingByDay[(int)dayGlobal];
				final long[] tolerance=group.freshCallsInitialToleranceByDay[(int)dayGlobal];
				CallEvent call;
				CallerRecord record;
				for (int j=0;j<waiting.length;j++) {
					record=dynamicSimData.getNewCallerRecord();
					record.callerType=group;
					record.statisticClient=k2;
					record.statisticCall=k2;
					record.initialStartWaitingTime=0-waiting[j];
					record.initialRestWaitingTolerance=tolerance[j];
					call=(CallEvent)getEvent(CallEvent.class);
					call.init(0);
					call.callerRecord=record;
					initCallsList.add(call);
				}
			}
		}

		/* Sortieren, verketten, erstes Ereignis einfügen */
		eventManager.addInitialEvents(initCallsList);
	}

	private void initAgents(final boolean firstDay, boolean backgroundMode) {
		int count=0;

		final List<AgentReadyEvent> agents=new ArrayList<AgentReadyEvent>(firstDay?2000:dynamicSimData.agentRecord.length);

		List<AgentRecord> firstDayList=null;
		if (firstDay) firstDayList=new ArrayList<AgentRecord>();

		/* Liste mit allen Agenten anlegen */
		CallcenterRunModelCallcenter callcenter;
		for (int i=0;i<model.callcenter.length;i++) {
			if (backgroundMode && firstDay && i>0 && backgroundModeDelay>0) try {Thread.sleep(backgroundModeDelay);} catch (InterruptedException e) {}

			callcenter=model.callcenter[i];
			AgentRecord record;
			CallcenterRunModelAgent agent;
			for (int j=0;j<callcenter.agents.length;j++) {
				agent=callcenter.agents[j];

				if (firstDay) {
					record=dynamicSimData.new AgentRecord(model.caller);
					record.agent=agent;
					record.skillLevel=agent.skillLevel;
					record.callcenter=callcenter;
					record.statisticGlobal=statisticSimData.agentenGlobal;
					record.statisticProCallcenter=statisticSimData.agentenProCallcenter[i];
					int k=-1; for (int l=0;l<model.skills.length;l++) if (model.skills[l]==agent.skillLevel) {k=l; break;}
					assert(k>=0);
					record.statisticProSkilllevel=statisticSimData.agentenProSkilllevel[k];
					if (firstDayList!=null) firstDayList.add(record);

					/* Einmalig die Agenten zählen usw. */
					record.statisticGlobal.anzahlAgenten++;
					record.statisticProCallcenter.anzahlAgenten++;
					record.statisticProSkilllevel.anzahlAgenten++;

				} else {
					record=dynamicSimData.agentRecord[count]; count++;
					record.reinit();
				}

				AgentReadyEvent agentEvent=(AgentReadyEvent)getEvent(AgentReadyEvent.class);
				agentEvent.init(agent.workingTimeStart*1000);
				agentEvent.agentRecord=record;
				agents.add(agentEvent);
			}
		}

		if (firstDay && firstDayList!=null) dynamicSimData.agentRecord=firstDayList.toArray(new AgentRecord[0]);

		/* Sortieren, verketten, erstes Ereignis einfügen */
		eventManager.addInitialEvents(agents);
	}

	/**
	 * Liefert das zugehörige <code>KundenDaten</code>-Statistik-Objekt zu einem gegebenen <code>CallcenterRunModelCaller</code>-Objekt
	 * @param caller	<code>CallcenterRunModelCaller</code>-Objekt, zu dem das zugehörige Statistik-Objekt zurückgegeben werden soll.
	 * @return	Zugehöriges Statistik-Objekt
	 */
	public Statistics.KundenDaten getStatisticDataForCaller(final CallcenterRunModelCaller caller) {
		for (int i=0;i<model.caller.length;i++) if (model.caller[i]==caller) return statisticSimData.kundenProTyp[i];
		return null;
	}

	@Override
	public void initDay(final long day, final long dayGlobal, boolean backgroundMode) {
		initCalls(day==0,dayGlobal,backgroundMode);
		initAgents(day==0,backgroundMode);
		dynamicSimData.workingAgentsCount=0;
		StopTestEvent.addStopTestEvent(this,86401*1000);
	}

	@Override
	public void terminateCleanUp(final long now) {
		for (AgentRecord record : dynamicSimData.agentRecord) record.doneDay(Math.max(86400*1000,now),model.agentCostsUsed);
		dynamicSimData.freeAgents.clear();

		statisticSimData.kundenGlobal.updateInterDayData();
		for (int i=0;i<statisticSimData.kundenProTyp.length;i++) statisticSimData.kundenProTyp[i].updateInterDayData();
	}

	@Override
	public void finalTerminateCleanUp(final long eventCount) {
		super.finalTerminateCleanUp(eventCount);

		statisticSimData.simulationData.runEvents=eventCount;

		/* anrufeWartezeitSum wird bereits während der Simulation benötigt, kann also nicht erst am Ende zusammengezählt werden */
		statisticSimData.kundenGlobal.anrufeVerweilzeitSum=Math.round(statisticSimData.kundenGlobal.anrufeVerweilzeitSumProIntervall.sum());
		for (int i=0;i<statisticSimData.kundenProTyp.length;i++) statisticSimData.kundenProTyp[i].anrufeVerweilzeitSum=Math.round(statisticSimData.kundenProTyp[i].anrufeVerweilzeitSumProIntervall.sum());
		statisticSimData.kundenGlobal.anrufeAbbruchzeitSum=Math.round(statisticSimData.kundenGlobal.anrufeAbbruchzeitSumProIntervall.sum());
		for (int i=0;i<statisticSimData.kundenProTyp.length;i++) statisticSimData.kundenProTyp[i].anrufeAbbruchzeitSum=Math.round(statisticSimData.kundenProTyp[i].anrufeAbbruchzeitSumProIntervall.sum());

		statisticSimData.kundenGlobal.kundenWartezeitSum=Math.round(statisticSimData.kundenGlobal.kundenWartezeitSumProIntervall.sum());
		for (int i=0;i<statisticSimData.kundenProTyp.length;i++) statisticSimData.kundenProTyp[i].kundenWartezeitSum=Math.round(statisticSimData.kundenProTyp[i].kundenWartezeitSumProIntervall.sum());
		statisticSimData.kundenGlobal.kundenVerweilzeitSum=Math.round(statisticSimData.kundenGlobal.kundenVerweilzeitSumProIntervall.sum());
		for (int i=0;i<statisticSimData.kundenProTyp.length;i++) statisticSimData.kundenProTyp[i].kundenVerweilzeitSum=Math.round(statisticSimData.kundenProTyp[i].kundenVerweilzeitSumProIntervall.sum());
		statisticSimData.kundenGlobal.kundenAbbruchzeitSum=Math.round(statisticSimData.kundenGlobal.kundenAbbruchzeitSumProIntervall.sum());
		for (int i=0;i<statisticSimData.kundenProTyp.length;i++) statisticSimData.kundenProTyp[i].kundenAbbruchzeitSum=Math.round(statisticSimData.kundenProTyp[i].kundenAbbruchzeitSumProIntervall.sum());

		dynamicSimData=null;
		model=null;
		initCallsList=null;
	}
}
