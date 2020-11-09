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
package simulator.events;

import language.Language;
import mathtools.distribution.tools.DistributionRandomNumber;
import parser.MathCalcError;
import parser.MathParser;
import simcore.Event;
import simcore.SimData;
import simulator.LogTools;
import simulator.RunData;
import simulator.RunData.AgentRecord;
import simulator.RunData.CallerRecord;
import simulator.SimulationData;
import simulator.Statistics.KundenDaten;
import ui.model.CallcenterRunModelSkillLevel;

/**
 * Ereignis: Beginn eines Gesprächs
 * @author Alexander Herzog
 * @version 1.0
 */
public final class Service1StartEvent extends Event {
	/**
	 * Anrufer der das Gespräch beginnt
	 */
	public CallerRecord caller;

	/**
	 * Agent der das Gespräch beginnt
	 */
	public AgentRecord agent;

	/**
	 * Erfasst den Start einer Bedienung
	 * @param data	Simulationsdatenobjekt
	 * @param serviceLevelSeconds	Service-Level-Sekunden für die Wartezeit vor der Bedienung
	 * @param wartezeit1000stelSekundenBasis	Bediendauer für diesen Anruf
	 * @param verweilzeit1000stelSekundenBasis	Verweildauer für diesen Anruf
	 */
	private void logServiceStarts(final SimulationData data, final int serviceLevelSeconds, final long wartezeit1000stelSekundenBasis, long verweilzeit1000stelSekundenBasis) {
		final KundenDaten statisticGlobal=data.statisticSimData.kundenGlobal;
		/* KundenDaten statisticClient=caller.statisticClient; brauchen wir hier gar nicht (Daten auf Kundenbasis) */
		final KundenDaten statisticCall=caller.statisticCall;

		/* alt: int Interval=(int)Math.min(47,Math.round(Math.floor((double)caller.startWaitingTime/1000/1800))); */
		int interval=(int)Math.max(0,Math.min(47,caller.startWaitingTime/1000/1800));
		statisticGlobal.anrufeErfolg++;
		statisticCall.anrufeErfolg++;
		statisticGlobal.anrufeErfolgProIntervall.densityData[interval]++;
		statisticCall.anrufeErfolgProIntervall.densityData[interval]++;

		final int wartezeit=(int)(wartezeit1000stelSekundenBasis/1000);
		final int verweilzeit=(int)(verweilzeit1000stelSekundenBasis/1000);
		final int wartezeitSqr=wartezeit*wartezeit;
		final int verweilzeitSqr=verweilzeit*verweilzeit;

		/* anrufeWartezeitSum wird bereits während der Simulation benötigt, kann also nicht erst am Ende zusammengezählt werden */
		statisticGlobal.anrufeWartezeitSum+=wartezeit;
		statisticCall.anrufeWartezeitSum+=wartezeit;
		statisticGlobal.anrufeWartezeitSum2+=wartezeitSqr;
		statisticCall.anrufeWartezeitSum2+=wartezeitSqr;
		/* anrufeVerweilzeitSum wird in ComplexSimData.finalTerminateCleanUp aus den anrufeVerweilzeitSumProInterval-Daten zusammengezählt */
		statisticGlobal.anrufeVerweilzeitSum2+=verweilzeitSqr;
		statisticCall.anrufeVerweilzeitSum2+=verweilzeitSqr;
		statisticGlobal.anrufeWartezeitSumProIntervall.densityData[interval]+=((double)wartezeit1000stelSekundenBasis)/1000;
		statisticCall.anrufeWartezeitSumProIntervall.densityData[interval]+=((double)wartezeit1000stelSekundenBasis)/1000;
		statisticGlobal.anrufeVerweilzeitSumProIntervall.densityData[interval]+=((double)verweilzeit1000stelSekundenBasis)/1000;
		statisticCall.anrufeVerweilzeitSumProIntervall.densityData[interval]+=((double)verweilzeit1000stelSekundenBasis)/1000;

		int index=Math.min(statisticGlobal.anrufeWartezeitVerteilung.densityData.length-1,wartezeit);
		statisticGlobal.anrufeWartezeitVerteilung.densityData[index]++;
		statisticCall.anrufeWartezeitVerteilung.densityData[index]++;

		index=Math.min(statisticGlobal.anrufeWartezeitVerteilungLang.densityData.length-1,wartezeit/1800);
		statisticGlobal.anrufeWartezeitVerteilungLang.densityData[index]++;
		statisticCall.anrufeWartezeitVerteilungLang.densityData[index]++;

		index=Math.min(statisticGlobal.anrufeVerweilzeitVerteilung.densityData.length-1,verweilzeit);
		statisticGlobal.anrufeVerweilzeitVerteilung.densityData[index]++;
		statisticCall.anrufeVerweilzeitVerteilung.densityData[index]++;

		index=Math.min(statisticGlobal.anrufeVerweilzeitVerteilungLang.densityData.length-1,verweilzeit/1800);
		statisticGlobal.anrufeVerweilzeitVerteilungLang.densityData[index]++;
		statisticCall.anrufeVerweilzeitVerteilungLang.densityData[index]++;

		if (wartezeit<=serviceLevelSeconds) {
			statisticGlobal.anrufeServicelevel++;
			statisticCall.anrufeServicelevel++;
			statisticGlobal.anrufeServicelevelProIntervall.densityData[interval]++;
			statisticCall.anrufeServicelevelProIntervall.densityData[interval]++;
		}
	}

	/* (non-Javadoc)
	 * @see simcore.Event#run(simcore.SimData)
	 */
	@Override
	public void run(final SimData data) {
		assert(caller!=null);
		int wartezeit=(int)((time-caller.startWaitingTime)/1000);
		assert(wartezeit>=0);

		/* Status des Agenten ändern */
		agent.logStatusChange(time,RunData.AGENT_TECHNISCHER_LEERLAUF,RunData.AGENT_BEDIENUNG,caller);

		/* Eigenschaften, die bei einem Warteabbruch in der technischen Bereitzeit relevant gewesen wären, zurücksetzen */
		caller.agentAssignedStartEvent=null;

		/* Abbruch-Event löschen */
		if (caller.callCancelEvent!=null) data.eventManager.deleteEvent(caller.callCancelEvent,data);
		caller.callCancelEvent=null;

		/* Bedienzeit ermitteln */
		final CallcenterRunModelSkillLevel skillLevel=agent.skillLevel;
		final short skillLevelNr=skillLevel.callerTypeByIndex[caller.callerType.index];
		assert(skillLevelNr>=0);
		final int interval=(int)Math.max(0,Math.min(47,time/1000/1800));

		long workingTime=Math.round(DistributionRandomNumber.randomNonNegative(skillLevel.callerTypeWorkingTime[skillLevelNr][interval])*1000);
		MathParser calc=skillLevel.callerTypeWorkingTimeAddOn[skillLevelNr][interval];
		if (calc!=null) {
			try {
				final double d=calc.calc(new double[]{wartezeit});
				workingTime+=Math.max(d*1000,0);
			} catch (MathCalcError e) {}
		}

		/* Loggen des Wartezeit Endes */
		logServiceStarts((SimulationData)data,caller.callerType.serviceLevelSeconds,time-caller.startWaitingTime,time-caller.startWaitingTime+workingTime);

		/* Ereignis für Beginn der Nachbearbeitungszeit anlegen */
		Service2StartEvent nextEvent=((Service2StartEvent)data.getEvent(Service2StartEvent.class));
		nextEvent.init(time+workingTime);
		nextEvent.caller=caller;
		nextEvent.agent=agent;
		nextEvent.skillLevelNr=skillLevelNr;
		nextEvent.wartezeitMS=time-caller.startWaitingTime;
		nextEvent.workingtimeMS=workingTime;
		data.eventManager.addEvent(nextEvent);

		if (data.loggingActive) LogTools.log(data,Language.tr("Simulation.Log.Service1Start"),caller,agent,
				""+
						Language.tr("Simulation.Log.Service1Start.WaitingTime")+": "+SimData.formatSimTime(time-caller.startWaitingTime)+"\n"+
						Language.tr("Simulation.Log.Service1Start.ServiceTime")+": "+SimData.formatSimTime(workingTime)+"\n"
				);
	}
}
