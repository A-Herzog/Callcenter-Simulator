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

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import language.Language;
import simcore.Event;
import simcore.SimData;
import simulator.LogTools;
import simulator.RunData;
import simulator.RunData.AgentRecord;
import simulator.RunData.CallerRecord;
import simulator.SimulationData;
import ui.model.CallcenterRunModelCaller;

/**
 * Ereignis: Prüfen, ob das System wirklich nach Mitternacht noch weiterlaufen muss.
 * @author Alexander Herzog
 * @version 1.0
 */
public class StopTestEvent extends Event {
	/**
	 * Konstruktor der Klasse
	 */
	public StopTestEvent() {
		/*
		 * Wird nur benötigt, um einen JavaDoc-Kommentar für diesen (impliziten) Konstruktor
		 * setzen zu können, damit der JavaDoc-Compiler keine Warnung mehr ausgibt.
		 */
	}

	/**
	 * Entfernt einen Kunden, der nicht mehr bedient werden kann, aus der Warteschlange.
	 * @param data	Simulationsdatenobjekt
	 * @param caller	Anrufer
	 */
	private static final void cancelCall(final SimulationData data, final CallerRecord caller) {
		/* In Statistik speichern, dass dieser Kunde eigentlich noch im System ist und eine Restwartezeittoleranz hat */
		final long alreadyWaited=data.currentTime-caller.startWaitingTime;
		long restToleranz=0;
		if (caller.callCancelEvent!=null) restToleranz=caller.callCancelEvent.time-data.currentTime;
		data.statisticSimData.kundenGlobal.kundenNextDayUebertragWaitingTimeThisDay.add(alreadyWaited);
		caller.statisticCall.kundenNextDayUebertragWaitingTimeThisDay.add(alreadyWaited); /* mit aktuellem Kundentyp speichern */
		data.statisticSimData.kundenGlobal.kundenNextDayUebertragRestWaitingToleranceThisDay.add(restToleranz);
		caller.statisticCall.kundenNextDayUebertragRestWaitingToleranceThisDay.add(restToleranz); /* mit aktuellem Kundentyp speichern */

		/* Kunden aus der Warteschlange austragen */
		data.dynamicSimData.removeCallerFromQueue(caller,data.currentTime,data.statisticSimData);

		/* Events für Kunden entfernen */
		if (caller.callCancelEvent!=null) {
			data.eventManager.deleteEvent(caller.callCancelEvent,data);
		}
		if (caller.reCheckEvents!=null && caller.reCheckEvents.length>0) for (int i=0;i<caller.reCheckEvents.length;i++) {
			data.eventManager.deleteEvent(caller.reCheckEvents[i],data);
		}

		/* Logging */
		CallCancelEvent.logGiveUp(data,caller,data.currentTime,false,true);
		if (data.loggingActive) LogTools.log(data,Language.tr("Simulation.Log.StopTest.NoMatchingAgentAnymore1"),caller,null,null);
	}

	/**
	 * Entfernt einen extern wartenden Kunden, der nicht mehr bedient werden kann.
	 * @param data	Simulationsdatenobjekt
	 * @param caller	Anrufer
	 */
	private static final void cancelExternalCall(final SimulationData data, final CallerRecord caller) {
		/* Kunde wartet ab sofort nicht mehr extern */
		data.dynamicSimData.removeCallerFromExternalQueue(caller);

		if (caller.retryEvent!=null) {
			/* Logging */
			CallCancelEvent.logGiveUpWhileExtern(data,caller,data.currentTime,caller.retryEvent.time-86400000,true);
			if (data.loggingActive) LogTools.log(data,Language.tr("Simulation.Log.StopTest.NoMatchingAgentAnymore2"),caller,null,null);

			/* Retry Event löschen */
			data.eventManager.deleteEvent(caller.retryEvent,data);
		}
	}

	/**
	 * Prüft, ob der Simulationstag zuende ist und entfernt in diesem Fall
	 * ggf. noch wartende Kunden aus der Warteschlange.
	 * @param data	Simulationsdatenobjekt
	 */
	public static final void runStopTest(final SimulationData data) {
		if (data.currentTime<=86400*1000) return;

		/* Welche Kundentypen können überhaupt noch bedient werden ? */
		final CallcenterRunModelCaller[] callerType=data.model.caller;
		boolean[] callerOk=new boolean[callerType.length];
		Arrays.fill(callerOk,false);

		AgentRecord[] agents=data.dynamicSimData.agentRecord;
		for (int i=0;i<agents.length;i++) if (agents[i].status!=RunData.AGENT_NACH_DIENST) {

			final CallcenterRunModelCaller[] skills=agents[i].skillLevel.callerType;
			for (int j=0;j<skills.length;j++) {
				final CallcenterRunModelCaller type=skills[j];
				for (int k=0;k<callerType.length;k++) if (type==callerType[k]) {callerOk[k]=true; break;}
			}
		}

		/* Textfile-Logging */
		if (data.loggingActive) {
			StringBuilder s=new StringBuilder(Language.tr("Simulation.Log.StopTest.ServableClientTypes")+": ");
			boolean first=true;
			for (int i=0;i<callerOk.length;i++) if (callerOk[i]) {if (!first) s.append(", "); else first=false; s.append(callerType[i].name);}
			s.append("\n"+Language.tr("Simulation.Log.StopTest.NotServableClientTypes")+": ");
			first=true;
			for (int i=0;i<callerOk.length;i++) if (!callerOk[i]) {if (!first) s.append(", "); else first=false; s.append(callerType[i].name);}
			s.append('\n');
			LogTools.log(data,Language.tr("Simulation.Log.StopTest"),null,null,s.toString());
		}

		/* Kunden, die nicht mehr bedient werden können, aus der Warteschlange werfen */
		for (int i=0;i<callerOk.length;i++) if (!callerOk[i]) {
			@SuppressWarnings("unchecked")
			List<CallerRecord> list=((ArrayList<CallerRecord>)(data.dynamicSimData.queueByType[i]).clone());
			int count=list.size();
			for (int j=0;j<count;j++) cancelCall(data,list.get(j));
		}

		/* Kunden, die nicht mehr bedient werden können, auch extern canceln */
		for (int i=0;i<callerOk.length;i++) if (!callerOk[i]) {
			@SuppressWarnings("unchecked")
			List<CallerRecord> list=((ArrayList<CallerRecord>)(data.dynamicSimData.externalQueueByType[i]).clone());
			int count=list.size();
			for (int j=0;j<count;j++) cancelExternalCall(data,list.get(j));
		}
	}

	@Override
	public final void run(final SimData data) {
		/* Ggf. Kunden aus dem System werfen */
		runStopTest((SimulationData)data);
	}

	/**
	 * Fügt ein Ereignis der Prüfung auf das Simulationsende in die Ereignisverarbeitung ein.
	 * @param data	Simulationsdatenobjekt
	 * @param time	Zeitpunkt der Prüfung (in MS)
	 */
	public static final void addStopTestEvent(final SimulationData data, final long time) {
		final StopTestEvent e=((StopTestEvent)(data.getEvent(StopTestEvent.class)));
		e.init(time);
		data.eventManager.addEvent(e);
	}
}
