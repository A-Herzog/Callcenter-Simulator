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

import java.util.List;
import java.util.concurrent.ThreadLocalRandom;

import language.Language;
import mathtools.distribution.tools.DistributionRandomNumber;
import simcore.Event;
import simcore.SimData;
import simulator.LogTools;
import simulator.RunData;
import simulator.RunData.AgentRecord;
import simulator.RunData.CallerRecord;
import simulator.SimulationData;
import simulator.Statistics.KundenDaten;
import ui.model.CallcenterRunModelCaller;

/**
 * Ereignis: Beginn der Nachbearbeitungszeit
 * @author Alexander Herzog
 * @version 1.0
 */
public final class Service2StartEvent extends Event {
	/**
	 * Kunden für den die Bedienzeit endet
	 */
	public CallerRecord caller;

	/**
	 * Agent für den die Bedienung endet und die Nachbearbeitungszeit beginnt
	 */
	public AgentRecord agent;

	/**
	 * Wartezeit des Kunden an der Station in Millisekunden
	 */
	public long wartezeitMS;

	/**
	 * Bedienzeit des Kunden an der Station in Millisekunden
	 */
	public long workingtimeMS;

	/**
	 * Skill-Level des Agenten der den Kunden bedient hat
	 */
	public short skillLevelNr;

	/**
	 * Konstruktor der Klasse
	 */
	public Service2StartEvent() {
		/*
		 * Wird nur benötigt, um einen JavaDoc-Kommentar für diesen (impliziten) Konstruktor
		 * setzen zu können, damit der JavaDoc-Compiler keine Warnung mehr ausgibt.
		 */
	}

	/**
	 * Erfasst den Abschluss eines Anrufs
	 * @param data	Simulationsdatenobjekt
	 * @param serviceLevelSeconds	Service-Level-Sekunden für die Wartezeit vor der Bedienung
	 * @param callContinue	Wird der Anrufer weitergeleitet?
	 */
	private void logCallDone(final SimulationData data, final int serviceLevelSeconds, final boolean callContinue) {
		if (callContinue) {
			/* Weiterleitung wird als neuer Anruf gewertet */
			/* und von CallEvent.processCall(caller,time,data); geloggt */

			caller.callerWaitingTime+=wartezeitMS/1000;
			caller.callerStayingTime+=(wartezeitMS+workingtimeMS)/1000;
		} else {
			final KundenDaten statisticGlobal=data.statisticSimData.kundenGlobal;
			final KundenDaten statisticClient=caller.statisticClient;
			/* KundenDaten statisticCall=caller.statistic3; */

			/* Wenn der Anrufer nicht weitergeleitet wird, legt er auf und wird als Kunde insgesamt als erfolgreich gezählt */
			/* alt: int Interval=(int)Math.min(47,Math.round(Math.floor((double)caller.firstCallTime/1000/1800))); */
			final int interval=(int)Math.max(0,Math.min(47,caller.firstCallTime/1000/1800));
			statisticGlobal.kundenErfolg++;
			statisticClient.kundenErfolg++;
			statisticGlobal.kundenErfolgProIntervall.densityData[interval]++;
			statisticClient.kundenErfolgProIntervall.densityData[interval]++;

			final long callerWait1000stelSekundenBasis=caller.callerWaitingTime*1000+wartezeitMS;
			final long callerStay1000stelSekundenBasis=caller.callerStayingTime*1000+(wartezeitMS+workingtimeMS);

			final int callerWait=(int)callerWait1000stelSekundenBasis/1000; caller.callerWaitingTime=callerWait;
			final int callerStay=(int)callerStay1000stelSekundenBasis/1000; caller.callerStayingTime=callerStay;

			final int callerWaitSqr=callerWait*callerWait;
			final int callerStaySqr=callerStay*callerStay;

			/* kundenWartezeitSum und kundenVerweilzeitSum werden in ComplexSimData.finalTerminateCleanUp aus den *ProInterval-Daten zusammengezählt */
			statisticGlobal.kundenWartezeitSum2+=callerWaitSqr;
			statisticClient.kundenWartezeitSum2+=callerWaitSqr;
			statisticGlobal.kundenVerweilzeitSum2+=callerStaySqr;
			statisticClient.kundenVerweilzeitSum2+=callerStaySqr;
			statisticGlobal.kundenWartezeitSumProIntervall.densityData[interval]+=((double)callerWait1000stelSekundenBasis)/1000;
			statisticClient.kundenWartezeitSumProIntervall.densityData[interval]+=((double)callerWait1000stelSekundenBasis)/1000;
			statisticGlobal.kundenVerweilzeitSumProIntervall.densityData[interval]+=((double)callerStay1000stelSekundenBasis)/1000;
			statisticClient.kundenVerweilzeitSumProIntervall.densityData[interval]+=((double)callerStay1000stelSekundenBasis)/1000;

			int index=Math.min(statisticGlobal.kundenWartezeitVerteilung.densityData.length-1,callerWait);
			statisticGlobal.kundenWartezeitVerteilung.densityData[index]++;
			statisticClient.kundenWartezeitVerteilung.densityData[index]++;

			index=Math.min(statisticGlobal.kundenWartezeitVerteilungLang.densityData.length-1,callerWait/1800);
			statisticGlobal.kundenWartezeitVerteilungLang.densityData[index]++;
			statisticClient.kundenWartezeitVerteilungLang.densityData[index]++;

			index=Math.min(statisticGlobal.kundenVerweilzeitVerteilung.densityData.length-1,callerStay);
			statisticGlobal.kundenVerweilzeitVerteilung.densityData[index]++;
			statisticClient.kundenVerweilzeitVerteilung.densityData[index]++;

			index=Math.min(statisticGlobal.kundenVerweilzeitVerteilungLang.densityData.length-1,callerStay/1800);
			statisticGlobal.kundenVerweilzeitVerteilungLang.densityData[index]++;
			statisticClient.kundenVerweilzeitVerteilungLang.densityData[index]++;

			if (callerWait<=serviceLevelSeconds) {
				statisticGlobal.kundenServicelevel++;
				statisticClient.kundenServicelevel++;
				statisticGlobal.kundenServicelevelProIntervall.densityData[interval]++;
				statisticClient.kundenServicelevelProIntervall.densityData[interval]++;
			}
		}
	}

	/**
	 * Erfasst eine Weiterleitung
	 * @param data	Simulationsdatenobjekt
	 */
	private void logCallContinue(SimulationData data) {
		final KundenDaten statisticGlobal=data.statisticSimData.kundenGlobal;
		final KundenDaten statisticClient=caller.statisticClient;
		final KundenDaten statisticCall=caller.statisticCall;

		/* alt: int Interval=(int)Math.min(47,Math.round(Math.floor((double)caller.startWaitingTime/1000/1800))); */
		int interval=(int)Math.max(0,Math.min(47,caller.startWaitingTime/1000/1800));

		statisticGlobal.anrufeWeiterleitungen++;
		statisticCall.anrufeWeiterleitungen++;
		statisticGlobal.anrufeWeiterleitungenProIntervall.densityData[interval]++;
		statisticCall.anrufeWeiterleitungenProIntervall.densityData[interval]++;

		if (!caller.callContinued) {
			/* Erste Weiterleitung => Auch auf Kunden-Ebene loggen */
			/* alt: Interval=(int)Math.min(47,Math.round(Math.floor((double)caller.firstCallTime/1000/1800))); */
			interval=(int)Math.max(0,Math.min(47,caller.firstCallTime/1000/1800));
			statisticGlobal.kundenWeiterleitungen++;
			statisticClient.kundenWeiterleitungen++;
			statisticGlobal.kundenWeiterleitungenProIntervall.densityData[interval]++;
			statisticClient.kundenWeiterleitungenProIntervall.densityData[interval]++;
		}
	}

	/**
	 * Plant einen Wiederanruf ein.
	 * @param data	Simulationsdatenobjekt
	 * @param time	Zeitpunkt des Wiederanrufs
	 * @param oldType	Alter Kundentyp
	 * @param newType	Neuer Kundentyp
	 */
	private static void scheduleRecall(final SimulationData data, final long time, final CallcenterRunModelCaller oldType, final CallcenterRunModelCaller newType) {
		final long recallCallTime=time+Math.round(DistributionRandomNumber.randomNonNegative(oldType.recallTimeDist)*1000);

		final CallerRecord record=data.dynamicSimData.getNewCallerRecord();
		final KundenDaten statisticGlobal=data.statisticSimData.kundenGlobal;
		final KundenDaten statisticClient=data.statisticSimData.kundenProTyp[newType.index];

		record.callerType=newType;
		record.statisticClient=statisticClient;
		record.statisticCall=statisticClient;
		record.isRecall=true;
		CallEvent call=(CallEvent)data.getEvent(CallEvent.class);
		call.init(recallCallTime);
		call.callerRecord=record;

		data.eventManager.addEvent(call);

		final int interval=(int)Math.max(0,Math.min(47,recallCallTime/1000/1800));

		statisticGlobal.kundenWiederanruf++;
		statisticClient.kundenWiederanruf++;
		statisticGlobal.kundenWiederanrufProIntervall.densityData[interval]++;
		statisticClient.kundenWiederanrufProIntervall.densityData[interval]++;
	}

	/* (non-Javadoc)
	 * @see simcore.Event#run(simcore.SimData)
	 */
	@Override
	public void run(final SimData data) {
		/* Status des Agenten ändern */
		agent.logStatusChange(time,RunData.AGENT_BEDIENUNG,RunData.AGENT_NACHBEARBEITUNG,caller);
		agent.status=RunData.AGENT_NACHBEARBEITUNG;

		/* Nachbearbeitungszeit ermitteln */
		final int interval=(int)Math.max(0,Math.min(47,time/1000/1800));
		final double processingTime=DistributionRandomNumber.randomNonNegative(agent.skillLevel.callerTypePostProcessingTime[skillLevelNr][interval]);

		/* Ereignis für Ende der Nachbearbeitungszeit erstellen */
		final AgentReadyEvent agentReady=((AgentReadyEvent)data.getEvent(AgentReadyEvent.class));
		agentReady.init(time+Math.round(processingTime*1000));
		agentReady.agentRecord=agent;
		data.eventManager.addEvent(agentReady);

		if (data.loggingActive) LogTools.log(data,Language.tr("Simulation.Log.Service2Start"),caller,agent,Language.tr("Simulation.Log.Service2Start.Time")+": "+SimData.formatSimTime(Math.round(processingTime*1000))+"\n");

		final CallcenterRunModelCaller callerType=caller.callerType;

		/* Weiterleitung ? */
		final int specialContinue=(callerType.continueSkillLevel.size()>0)?callerType.continueSkillLevel.indexOf(agent.skillLevel):-1;

		boolean callContinue;
		if (specialContinue>=0) {
			callContinue=(callerType.continueSkillLevelProbability.get(specialContinue)>=ThreadLocalRandom.current().nextDouble());
		} else {
			callContinue=(callerType.continueProbability>=ThreadLocalRandom.current().nextDouble());
		}

		logCallDone((SimulationData)data,callerType.serviceLevelSeconds,callContinue);

		if (callContinue) {
			logCallContinue((SimulationData)data);

			/* Status=Weitergeleitet, Wartezeit auf neuen Startwert setzen */
			caller.startWaitingTime=time;
			caller.callContinued=true;

			/* Neuen Caller-Typ setzen */
			final double p=ThreadLocalRandom.current().nextDouble();
			double sum=0;
			if (specialContinue>=0) {
				List<Double> probabilities=callerType.continueSkillLevelProbabilities.get(specialContinue);
				final int count=probabilities.size();
				for (int i=0;i<count;i++) {
					sum+=probabilities.get(i);
					if (sum>p) {
						caller.callerType=callerType.continueSkillLevelType.get(specialContinue).get(i);
						/* ab hier ist die lokale Variable callerType nicht mehr aktuell */
						caller.statisticCall=((SimulationData)data).getStatisticDataForCaller(caller.callerType);
						break;
					}
				}
			} else {
				double[] probabilities=callerType.continueTypeProbability;
				for (int i=0;i<probabilities.length;i++) {
					sum+=probabilities[i];
					if (sum>p) {
						caller.callerType=callerType.continueType[i];
						/* ab hier ist die lokale Variable callerType nicht mehr aktuell */
						caller.statisticCall=((SimulationData)data).getStatisticDataForCaller(caller.callerType);
						break;
					}
				}
			}

			if (data.loggingActive) LogTools.log(data,Language.tr("Simulation.Log.Service2Start.Forwarded"),caller,null,null);

			/* An Warteschlange anstellen */
			CallEvent.processCall(caller,time,(SimulationData)data);
			return;
		}

		/* Wiederanrufer */
		final int specialRecall=(callerType.recallSkillLevel.size()>0)?callerType.recallSkillLevel.indexOf(agent.skillLevel):-1;

		boolean callRecall;
		if (specialRecall>=0) {
			callRecall=(callerType.recallSkillLevelProbability.get(specialRecall)>=/* Math.random() */ ThreadLocalRandom.current().nextDouble());
		} else {
			callRecall=(callerType.recallProbability>=/* Math.random() */ ThreadLocalRandom.current().nextDouble());
		}

		if (callRecall) {
			/* Neuen Caller-Typ setzen */
			final double p=/* Math.random() */ ThreadLocalRandom.current().nextDouble();
			double sum=0;
			CallcenterRunModelCaller recallCallerType=null;
			if (specialRecall>=0) {
				List<Double> probabilities=callerType.recallSkillLevelProbabilities.get(specialRecall);
				final int count=probabilities.size();
				for (int i=0;i<count;i++) {
					sum+=probabilities.get(i);
					if (sum>p) {
						recallCallerType=callerType.recallSkillLevelType.get(specialRecall).get(i);
						break;
					}
				}
			} else {
				double[] probabilities=callerType.recallTypeProbability;
				for (int i=0;i<probabilities.length;i++) {
					sum+=probabilities[i];
					if (sum>p) {
						recallCallerType=callerType.recallType[i];
						break;
					}
				}
			}

			assert(recallCallerType!=null);

			if (data.loggingActive) LogTools.log(data,Language.tr("Simulation.Log.Service2Start.Retry"),caller,null,null);

			scheduleRecall((SimulationData)data,time,caller.callerType,recallCallerType);
		} else {
			if (data.loggingActive) LogTools.log(data,Language.tr("Simulation.Log.Service2Start.Finish"),caller,null,null);
			((SimulationData)data).dynamicSimData.cacheSingleRecord(caller);

		}
	}
}
