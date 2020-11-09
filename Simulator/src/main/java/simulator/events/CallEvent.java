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
import parser.MathCalcError;
import simcore.Event;
import simcore.SimData;
import simulator.LogTools;
import simulator.RunData.AgentRecord;
import simulator.RunData.CallerRecord;
import simulator.SimulationData;
import simulator.Statistics.KundenDaten;
import ui.model.CallcenterRunModelCaller;

/**
 * Ereignis: Anruf eines Kunden
 * @author Alexander Herzog
 * @version 1.0
 */
public final class CallEvent extends Event {
	/**
	 * Kundendatensatz
	 */
	public CallerRecord callerRecord;

	/**
	 * Zugehörige Kundengruppe<br>
	 * nur zur Initialisieurng von {@link #callerRecord}; ohne Bedeutung, wenn <code>callerRecord!=null</code>
	 */
	public CallcenterRunModelCaller callerGroup;

	/**
	 * Erfasst einen Anruf
	 * @param data	Simulationsdatenobjekt
	 * @param callerRecord	Anruferdatensatz
	 * @param time	Aktuelle Simulationszeit
	 * @param newCall	Neuer Anruf (<code>true</code>) oder Weiterleitung (<code>false</code>)
	 */
	private static void logCall(final SimulationData data, final CallerRecord callerRecord, final long time, final boolean newCall) {
		final KundenDaten statisticGlobal=data.statisticSimData.kundenGlobal;
		final KundenDaten statisticClient=callerRecord.statisticClient;
		final KundenDaten statisticCall=callerRecord.statisticCall;

		/* alt: int Interval=(int)Math.min(47,Math.round(Math.floor((double)time/1000/1800))); */
		final int interval=(int)Math.max(0,Math.min(47,time/1800000));
		statisticGlobal.anrufe++;
		statisticCall.anrufe++;
		statisticGlobal.anrufeProIntervall.densityData[interval]++;
		statisticCall.anrufeProIntervall.densityData[interval]++;
		if (newCall && !callerRecord.isRecall) {
			statisticGlobal.kunden++;
			statisticClient.kunden++;
			statisticGlobal.kundenProIntervall.densityData[interval]++;
			statisticClient.kundenProIntervall.densityData[interval]++;
		}

		if (callerRecord.retryCount>0) {
			statisticGlobal.anrufeWiederholungen++;
			statisticGlobal.anrufeWiederholungenProIntervall.densityData[interval]++;
			if (callerRecord.retryCount==1) {
				statisticGlobal.kundenWiederholungen++;
				statisticGlobal.kundenWiederholungenProIntervall.densityData[interval]++;
			}
			if (callerRecord.retryCount==1) {
				statisticClient.kundenWiederholungen++;
				statisticClient.kundenWiederholungenProIntervall.densityData[interval]++;
			}
			statisticCall.anrufeWiederholungen++;
			statisticCall.anrufeWiederholungenProIntervall.densityData[interval]++;
		}
	}

	/**
	 * Erfasst einen blockierten Anruf
	 * @param data	Simulationsdatenobjekt
	 * @param callerRecord	Anruferobjekt
	 * @param time	Zeitpunkt des erfolglosen Anrufs
	 * @param retry	Wird der Kunde später eine Wiederholung starten?
	 */
	private static void logBlocked(final SimulationData data, final CallerRecord callerRecord, final long time, final boolean retry) {
		final KundenDaten statisticGlobal=data.statisticSimData.kundenGlobal;
		final KundenDaten statisticClient=callerRecord.statisticClient;
		final KundenDaten statisticCall=callerRecord.statisticCall;

		/* alt: int Interval=(int)Math.min(47,Math.round(Math.floor((double)time/1000/1800))); */
		int interval=(int)Math.max(0,Math.min(47,time/1800000));
		statisticGlobal.anrufeBlocked++;
		statisticCall.anrufeBlocked++;
		statisticGlobal.anrufeBlockedProIntervall.densityData[interval]++;
		statisticCall.anrufeBlockedProIntervall.densityData[interval]++;
		if (!retry) {
			/* alt: Interval=(int)Math.min(47,Math.round(Math.floor((double)callerRecord.firstCallTime/1000/1800))); */
			interval=(int)Math.max(0,Math.min(47,callerRecord.firstCallTime/1800000));
			statisticGlobal.kundenBlocked++;
			statisticClient.kundenBlocked++;
			statisticGlobal.kundenAbbruchThisDay++;
			statisticClient.kundenAbbruchThisDay++;
			statisticGlobal.kundenBlockedProIntervall.densityData[interval]++;
			statisticClient.kundenBlockedProIntervall.densityData[interval]++;
		}
	}

	/**
	 * Legt für einen Anrufer die Abbruch- und Recheck-Events an.
	 * @param callerRecord	Kudnendatensatz
	 * @param agentRecord	Agentendatensatz (kann <code>null</code> sein, wenn noch keine Zuordnung besteht)
	 * @param time	Zeitpunkt des Anrufs
	 * @param data	Simulationsdatenobjekt
	 * @return	Zeitpunkt an dem der Kunde das Warten aufgeben wird
	 */
	private static long buildEvents(final CallerRecord callerRecord, final AgentRecord agentRecord, final long time, final SimulationData data) {
		long cancelTime=Long.MAX_VALUE;

		if (callerRecord.callerType.waitingTimeDistActive) {
			double waitingTime;

			if (callerRecord.initialRestWaitingTolerance>0) { /* <0 kann auch auftreten: Dann ist gemeint: Normaler Anrufer, der aber gestern schon einmal da war. */
				waitingTime=((double)callerRecord.initialRestWaitingTolerance)/1000;
				callerRecord.initialRestWaitingTolerance=0;
			} else {
				waitingTime=DistributionRandomNumber.randomNonNegative(callerRecord.callerType.waitingTimeDist);
			}

			/* Cancel-Event nicht anlegen, wenn bereits zugeordnet und technische Bereitzeit < Abbruchzeit */
			if (agentRecord==null || (agentRecord.callcenter.technicalFreeTime>waitingTime && agentRecord.callcenter.technicalFreeTimeIsWaitingTime)) {
				cancelTime=time+Math.round(waitingTime*1000);
				if (cancelTime<0) cancelTime=86400*1000; /* Überlauf weil Wartezeittoleranz nahe \infty */
				CallCancelEvent cancel=((CallCancelEvent)data.getEvent(CallCancelEvent.class));
				cancel.init(cancelTime);
				cancel.callerRecord=callerRecord;
				callerRecord.callCancelEvent=cancel;
				data.eventManager.addEvent(cancel);
			}
		}

		/* Prüfen, ob Recheck-Events nötig sind */
		/* agentRecord!=null => Wenn bereits zugeordnet, nur noch Abbruch. */
		if (agentRecord==null && data.model.callerMinWaitingTimeUsed && callerRecord.callerType.recheckTimesMilliSecond!=null) {
			int minWaitingTime;
			ReCheckEvent recheck;
			Event[] old;
			for (int i=0;i<callerRecord.callerType.recheckTimesMilliSecond.length;i++) {
				/* Mindestwartezeit für Callcenter bestimmen */
				minWaitingTime=callerRecord.callerType.recheckTimesMilliSecond[i];

				/* Keine Rechecks nach Abbruchzeit */
				if (time+minWaitingTime>=cancelTime) continue;

				/* Recheck-Event anlegen */
				recheck=(ReCheckEvent)(data.getEvent(ReCheckEvent.class));
				recheck.init(time+minWaitingTime);
				recheck.callerRecord=callerRecord;
				data.eventManager.addEvent(recheck);

				/* Recheck-Event in callerRecord aufnehmen */
				if (callerRecord.reCheckEvents==null) {
					callerRecord.reCheckEvents=new Event[1];
					callerRecord.reCheckEvents[0]=recheck;
				} else {
					old=callerRecord.reCheckEvents;
					callerRecord.reCheckEvents=new Event[old.length+1];
					System.arraycopy(old,0,callerRecord.reCheckEvents,0,old.length);
					callerRecord.reCheckEvents[old.length]=recheck;
				}
			}
		}

		return cancelTime;
	}

	/**
	 * Führt die Verarbeitung beim Eintreffen eines Kunden an der Warteschlange
	 * (Anrufer oder Weiterleitung) durch.
	 * @param callerRecord	Kundendatensatz
	 * @param time	Zeitpunkt
	 * @param data	Simulationsdatenobjekt
	 */
	public static void processCall(final CallerRecord callerRecord, final long time, final SimulationData data) {
		logCall(data,callerRecord,time,(callerRecord.retryCount==0) && !callerRecord.callContinued);

		String info=null;
		if (callerRecord.initialRestWaitingTolerance!=0) {
			if (callerRecord.initialRestWaitingTolerance<0) callerRecord.initialRestWaitingTolerance=0; /* Ein Wert <0 wird nur hier verwendet, um anzudeuten, dass es sich um einen Anrufer handelt, der schon am Vortag einmal da war. */
			if (data.loggingActive) {
				info=Language.tr("Simulation.Log.Call.CarriedOver")+"\n";
				if (callerRecord.startWaitingTime!=time) info+=Language.tr("Simulation.Log.Call.CarriedOver.LastDayWaitingTime")+": "+SimData.formatSimTime(time-callerRecord.startWaitingTime)+"\n";
			}
		}

		/* Warteschlange voll ? */
		if (callerRecord.callerType.blocksLine) {
			final double[] param=data.dynamicSimData.workingAgentsCountArray;
			param[0]=data.dynamicSimData.workingAgentsCount;
			try {
				final double d=data.model.maxQueueLength.calc(param);
				if (d<=data.dynamicSimData.getPhoneCallQueueLength()) {
					/* Warteschlange ist voll */
					if (data.loggingActive) LogTools.log(data,Language.tr("Simulation.Log.Call.Blocked"),callerRecord,null,info);
					final double retryProbability=(callerRecord.retryCount==0)?callerRecord.callerType.retryProbabiltyAfterBlockedFirstRetry:callerRecord.callerType.retryProbabiltyAfterBlocked;
					final boolean retry=(retryProbability>=ThreadLocalRandom.current().nextDouble());
					logBlocked(data,callerRecord,time,retry);
					if (retry) CallCancelEvent.retryCall(callerRecord,time,data,callerRecord.retryCount==0,true);
					return;
				}
			} catch (MathCalcError e) {}
		}

		/* Agent finden, zuordnen und technische Bereitzeit beginnen*/
		AgentRecord agentRecord=data.dynamicSimData.findAgentForCaller(time,callerRecord,data,false);

		/* Abbruch- und Recheck-Events erstellen */
		final long cancelTime=buildEvents(callerRecord,agentRecord,time,data);

		/* Warteschlange anstellen */
		if (agentRecord==null) {
			data.dynamicSimData.addCallerToQueue(callerRecord,time,data.statisticSimData);
			if (data.loggingActive) {
				if (info==null) info="";
				info+=(cancelTime==Long.MAX_VALUE)?"":(Language.tr("Simulation.Log.Call.EndOfWaitingTimeTolerance")+": "+SimData.formatSimTime(cancelTime))+"\n";
				LogTools.log(data,Language.tr("Simulation.Log.Call.Queue"),callerRecord,null,info);
			}
		} else {
			if (data.loggingActive) LogTools.log(data,Language.tr("Simulation.Log.Call.Match"),callerRecord,agentRecord,info);
		}

		/* Prüfen, ob der Tag evtl. zu Ende ist */
		StopTestEvent.runStopTest(data);
	}

	/* (non-Javadoc)
	 * @see simcore.Event#run(simcore.SimData)
	 */
	@Override
	public void run(final SimData data) {
		/* Verspätete Initialisierung des callerRecord-Feldes */
		if (callerRecord==null) {
			callerRecord=((SimulationData)data).dynamicSimData.getNewCallerRecord();
			callerRecord.callerType=callerGroup;
			KundenDaten k=((SimulationData)data).statisticSimData.kundenProTyp[callerGroup.index];
			callerRecord.statisticClient=k;
			callerRecord.statisticCall=k;
		}

		/* Bei Überträgen vom Vortag eigentlichen Wartezeitbeginn verwenden */
		if (callerRecord.initialStartWaitingTime==0) {
			callerRecord.startWaitingTime=time;
		} else {
			callerRecord.startWaitingTime=callerRecord.initialStartWaitingTime;
			callerRecord.initialStartWaitingTime=0;
		}

		if (callerRecord.retryCount==0) {
			callerRecord.firstCallTime=time;
		} else {
			((SimulationData)data).dynamicSimData.removeCallerFromExternalQueue(callerRecord);
			callerRecord.retryEvent=null;
		}

		processCall(callerRecord,time,(SimulationData)data);

		callerRecord=null; /* Speicher sparen / nicht mehr benötigte Referenz freigeben */
	}
}
