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
 * Ereignis: Warteabbruch eines Kunden
 * @author Alexander Herzog
 * @version 1.0
 */
public final class CallCancelEvent extends Event {
	/**
	 * Kunde der das Warten aufgibt
	 */
	public CallerRecord callerRecord;

	/**
	 * Verbucht den Warteabbruch in der Statistik
	 * @param data	Simulationsdatenobjekt
	 * @param callerRecord	Kunde der das Warten aufgibt
	 * @param time	Zeitpunkt des Wartebabbruchs
	 * @param retry	Wird der Kunde einen neuen Versuch unternehmen?
	 * @param endOfDay	Erfolgte der Warteabbruch, weil nach Mitternacht die Warteschlange geleert wurde?
	 */
	public static void logGiveUp(final SimulationData data, final CallerRecord callerRecord, final long time, final boolean retry, final boolean endOfDay) {
		int cancelTime=(int)((time-callerRecord.startWaitingTime)/1000);
		if (cancelTime<0) cancelTime=86400+cancelTime; /* Abbruch war eigentlich erst am n‰chsten Tag */
		assert(cancelTime>=0);
		/* alt: int Interval=(int)Math.min(47,Math.round(Math.floor((double)callerRecord.startWaitingTime/1000/1800))); */
		final int interval=(int)Math.max(0,Math.min(47,callerRecord.startWaitingTime/1000/1800));

		final KundenDaten statisticGlobal=data.statisticSimData.kundenGlobal;
		final KundenDaten statisticCall=callerRecord.statisticCall;

		if (endOfDay) {
			KundenDaten statisticClient=callerRecord.statisticClient;
			statisticGlobal.anrufeUebertrag++;
			statisticCall.anrufeUebertrag++;
			statisticGlobal.kundenUebertrag++;
			statisticClient.kundenUebertrag++;
		} else {
			statisticGlobal.anrufeAbbruch++;
			statisticCall.anrufeAbbruch++;
			statisticGlobal.anrufeAbbruchProIntervall.densityData[interval]++;
			statisticCall.anrufeAbbruchProIntervall.densityData[interval]++;

			/* anrufeAbbruchzeitSum wird in ComplexSimData.finalTerminateCleanUp aus den anrufeAbbruchzeitSumProInterval-Daten zusammengez‰hlt */
			statisticGlobal.anrufeAbbruchzeitSum2+=cancelTime*cancelTime;
			statisticCall.anrufeAbbruchzeitSum2+=cancelTime*cancelTime;
			statisticGlobal.anrufeAbbruchzeitSumProIntervall.densityData[interval]+=cancelTime;
			statisticCall.anrufeAbbruchzeitSumProIntervall.densityData[interval]+=cancelTime;

			int index=Math.min(statisticGlobal.anrufeAbbruchzeitVerteilung.densityData.length-1,cancelTime);
			statisticGlobal.anrufeAbbruchzeitVerteilung.densityData[index]++;
			statisticCall.anrufeAbbruchzeitVerteilung.densityData[index]++;

			index=Math.min(statisticGlobal.anrufeAbbruchzeitVerteilungLang.densityData.length-1,cancelTime/1800);
			statisticGlobal.anrufeAbbruchzeitVerteilungLang.densityData[index]++;
			statisticCall.anrufeAbbruchzeitVerteilungLang.densityData[index]++;
		}

		if (!retry) logGiveUpWhileExtern(data,callerRecord,time,0,endOfDay);
	}

	/**
	 * Erfasst den Abbruch eines Kunden des sich momentan auﬂerhalb des Systems befindet.
	 * @param data	Simulationsdatenobjekt
	 * @param callerRecord	Kundendatensatz
	 * @param time	Zeitpunkt des Abbruchs
	 * @param plannedRetryTime	Geplante Wiederholzeit am n‰chsten Tag (einer berbundenen Simulation)
	 * @param endOfDay	Ist der Abbruch des Kunden durch das Ende des Simulationstages bedingt?
	 */
	public static void logGiveUpWhileExtern(final SimulationData data, final CallerRecord callerRecord, final long time, final long plannedRetryTime, final boolean endOfDay) {
		final KundenDaten statisticGlobal=data.statisticSimData.kundenGlobal;
		final KundenDaten statisticClient=callerRecord.statisticClient;

		int cancelTime=(int)((time-callerRecord.startWaitingTime)/1000);
		if (cancelTime<0) cancelTime=86400+cancelTime; /* Abbruch war eigentlich erst am n‰chsten Tag */


		final int interval=(int)Math.max(0,Math.min(47,Math.round(Math.floor((double)callerRecord.firstCallTime/1000/1800))));
		if (endOfDay) {
			if (plannedRetryTime>0) {
				statisticGlobal.kundenUebertrag++;
				statisticClient.kundenUebertrag++;
				data.statisticSimData.kundenGlobal.kundenNextDayRetryThisDay.add(plannedRetryTime);
				callerRecord.statisticCall.kundenNextDayRetryThisDay.add(plannedRetryTime); /* mit aktuellem Kundentyp speichern */
			}
		} else {
			statisticGlobal.kundenAbbruch++;
			statisticClient.kundenAbbruch++;
			statisticGlobal.kundenAbbruchThisDay++;
			statisticClient.kundenAbbruchThisDay++;

			statisticGlobal.kundenAbbruchProIntervall.densityData[interval]++;
			statisticClient.kundenAbbruchProIntervall.densityData[interval]++;

			/* kundenAbbruchzeitSum wird in ComplexSimData.finalTerminateCleanUp aus den kundenAbbruchzeitSumProInterval-Daten zusammengez‰hlt */
			statisticGlobal.kundenAbbruchzeitSum2+=cancelTime*cancelTime;
			statisticClient.kundenAbbruchzeitSum2+=cancelTime*cancelTime;
			statisticGlobal.kundenAbbruchzeitSumProIntervall.densityData[interval]+=cancelTime;
			statisticClient.kundenAbbruchzeitSumProIntervall.densityData[interval]+=cancelTime;

			int index=Math.min(statisticGlobal.kundenAbbruchzeitVerteilung.densityData.length-1,cancelTime);
			statisticGlobal.kundenAbbruchzeitVerteilung.densityData[index]++;
			statisticClient.kundenAbbruchzeitVerteilung.densityData[index]++;

			index=Math.min(statisticGlobal.kundenAbbruchzeitVerteilungLang.densityData.length-1,cancelTime/1800);
			statisticGlobal.kundenAbbruchzeitVerteilungLang.densityData[index]++;
			statisticClient.kundenAbbruchzeitVerteilungLang.densityData[index]++;
		}
	}

	/**
	 * Plant einen Wiederanruf eines Kunden ein.
	 * @param callerRecord	Kundendatensatz
	 * @param time	Aktuelle Zeit (nicht die Zeit des Wiederanrufs!)
	 * @param data	Simulationsdatenobjekt
	 * @param firstRetry	Handelt es sich um den ersten Wiederholversucht
	 * @param blocked	Handelt es sich um einen bei der Ankunft blockierten Kunden (<code>true</code>) oder um einen Warteabbruch (<code>false</code>)?
	 */
	public static void retryCall(final CallerRecord callerRecord, final long time, final SimData data, boolean firstRetry, boolean blocked) {
		/* Evtl. neuen Kundentyp festlegen */
		CallcenterRunModelCaller[] types;
		double[] rates;
		if (firstRetry) {
			if (blocked) {
				types=callerRecord.callerType.retryCallerTypeAfterBlockedFirstRetry;
				rates=callerRecord.callerType.retryCallerTypeRateAfterBlockedFirstRetry;
			} else {
				types=callerRecord.callerType.retryCallerTypeAfterGiveUpFirstRetry;
				rates=callerRecord.callerType.retryCallerTypeRateAfterGiveUpFirstRetry;
			}
		} else {
			if (blocked) {
				types=callerRecord.callerType.retryCallerTypeAfterBlocked;
				rates=callerRecord.callerType.retryCallerTypeRateAfterBlocked;
			} else {
				types=callerRecord.callerType.retryCallerTypeAfterGiveUp;
				rates=callerRecord.callerType.retryCallerTypeRateAfterGiveUp;
			}
		}
		if (types.length>0) {
			double p=ThreadLocalRandom.current().nextDouble();
			double sum=0;
			for (int i=0;i<rates.length;i++) {
				sum+=rates[i];
				if (sum>p) {
					callerRecord.callerType=types[i];
					callerRecord.statisticCall=((SimulationData)data).getStatisticDataForCaller(callerRecord.callerType);
					break;
				}
			}
		}

		/* Wiederholung anlegen */
		final long retryCallTime=time+Math.round(DistributionRandomNumber.randomNonNegative(callerRecord.callerType.retryTimeDist)*1000);
		CallEvent call=((CallEvent)(data.getEvent(CallEvent.class)));
		call.init(retryCallTime);
		call.callerRecord=callerRecord;
		callerRecord.retryCount++;
		callerRecord.callContinued=false;
		callerRecord.retryEvent=call;
		data.eventManager.addEvent(call);

		/* Kunden in die Liste extern wartender Kunden aufnehmen */
		((SimulationData)data).dynamicSimData.addCallerToExternalQueue(callerRecord);

		/* Textfile-Logging */
		if (data.loggingActive) LogTools.log(data,Language.tr("Simulation.Log.CallCancel.Retry"),callerRecord,null,Language.tr("Simulation.Log.CallCancel.Retry.Time")+": "+SimData.formatSimTime(retryCallTime)+"\n");
	}

	/* (non-Javadoc)
	 * @see simcore.Event#run(simcore.SimData)
	 */
	@Override
	public void run(final SimData data) {
		callerRecord.callCancelEvent=null;
		((SimulationData)data).dynamicSimData.removeCallerFromQueue(callerRecord,time,((SimulationData)data).statisticSimData);

		/* Abbruch in der technischen Bereitzeit ? */
		if (callerRecord.agentAssignedStartEvent!=null) {
			AgentRecord agent=callerRecord.agentAssignedStartEvent.agent;
			if (data.loggingActive) LogTools.log(data,Language.tr("Simulation.Log.CallCancel.TechnicalFreeTime"),callerRecord,agent,null);
			/* Agent freigeben */
			agent.logStatusChange(time,RunData.AGENT_TECHNISCHER_LEERLAUF,RunData.AGENT_LEERLAUF,null);
			AgentReadyEvent.markAgentAsFree(agent,time,data);
			/* Ereignis, welche den Gespr‰chsbeginn darstellt, lˆschen */
			data.eventManager.deleteEvent(callerRecord.agentAssignedStartEvent,data);
			callerRecord.agentAssignedStartEvent=null;
		} else {
			if (data.loggingActive) LogTools.log(data,Language.tr("Simulation.Log.CallCancel"),callerRecord,null,null);
		}

		/* Wiederholung ? */
		double retryProbability=(callerRecord.retryCount==0)?callerRecord.callerType.retryProbabiltyAfterGiveUpFirstRetry:callerRecord.callerType.retryProbabiltyAfterGiveUp;
		boolean retry=(retryProbability>=ThreadLocalRandom.current().nextDouble());

		logGiveUp((SimulationData)data,callerRecord,time,retry,false);
		if (retry) {
			retryCall(callerRecord,time,data,callerRecord.retryCount==0,false);
		} else {
			((SimulationData)data).dynamicSimData.cacheSingleRecord(callerRecord);
		}
	}

}
