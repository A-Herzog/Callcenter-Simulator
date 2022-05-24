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
import simcore.Event;
import simcore.SimData;
import simulator.LogTools;
import simulator.RunData;
import simulator.RunData.AgentRecord;
import simulator.RunData.CallerRecord;
import simulator.SimulationData;

/**
 * Ereignis: Agent meldet sich als bereit
 * @author Alexander Herzog
 * @version 1.0
 */
public final class AgentReadyEvent extends Event {
	/**
	 * Agent, der sich als bereit meldet
	 */
	public AgentRecord agentRecord;

	/**
	 * Konstruktor der Klasse
	 */
	public AgentReadyEvent() {
		/*
		 * Wird nur benötigt, um einen JavaDoc-Kommentar für diesen (impliziten) Konstruktor
		 * setzen zu können, damit der JavaDoc-Compiler keine Warnung mehr ausgibt.
		 */
	}

	/**
	 * Agenten wieder als verfügbar verbuchen
	 * @param agentRecord	Agentendatensatz
	 * @param time	Aktuelle Zeit
	 * @param data	Simulationsdatenobjekt
	 */
	public static void markAgentAsFree(final AgentRecord agentRecord, final long time, final SimData data) {
		agentRecord.callerType=null;

		if (!agentRecord.agent.workingNoEndTime && ((long)(agentRecord.agent.workingTimeEnd))*1000<time) {
			/* Dienstzeitende */
			if (data.loggingActive) LogTools.log(data,Language.tr("Simulation.Log.Ready.Quit"),null,agentRecord,null);
			agentRecord.status=RunData.AGENT_NACH_DIENST;
			agentRecord.logStatusChange(time,RunData.AGENT_LEERLAUF,RunData.AGENT_NACH_DIENST,null);

			/* Prüfen, ob der Tag evtl. zu Ende ist */
			StopTestEvent.runStopTest((SimulationData)data);

			return;
		}

		/* Als frei melden */
		agentRecord.status=RunData.AGENT_LEERLAUF;
		/* Aber noch nocht in die Liste freier Agenten einfügen */

		/* Passenden Anrufer finden */
		final CallerRecord caller=((SimulationData)data).dynamicSimData.findCallerForAgent(time,agentRecord,data,false); /* false= Agent ist nicht in der Liste der freien Agenten und muss dort folglich auch nicht entfernt werden */
		if (caller!=null) {
			if (data.loggingActive) LogTools.log(data,Language.tr("Simulation.Log.Ready.Match"),caller,agentRecord,null);
			agentRecord.logStatusChange(time,RunData.AGENT_LEERLAUF,RunData.AGENT_TECHNISCHER_LEERLAUF,caller);
		} else {
			/* Kein Kunde gefunden, jetzt Agent in die Liste der freien Agenten einfügen */
			((SimulationData)data).dynamicSimData.freeAgents.add(agentRecord);
		}
	}

	/* (non-Javadoc)
	 * @see simcore.Event#run(simcore.SimData)
	 */
	@Override
	public void run(final SimData data) {
		if (agentRecord.status==RunData.AGENT_VOR_DIENST) {
			/* Arbeitszeitbeginn */
			if (data.loggingActive) LogTools.log(data,Language.tr("Simulation.Log.Ready.ShiftStart"),null,agentRecord,null);
			agentRecord.logStatusChange(time,RunData.AGENT_VOR_DIENST,RunData.AGENT_LEERLAUF,null);
			if (!agentRecord.agent.workingNoEndTime) {
				final AgentQuitEvent quitEvent=(AgentQuitEvent)data.getEvent(AgentQuitEvent.class);
				quitEvent.init(((long)(agentRecord.agent.workingTimeEnd))*1000);
				quitEvent.agentRecord=agentRecord;
				data.eventManager.addEvent(quitEvent);
			}
		} else {
			/* Ende der Nachbearbeitungszeit */
			if (data.loggingActive) LogTools.log(data,Language.tr("Simulation.Log.Ready.ReadyAfterService"),null,agentRecord,null);
			assert(agentRecord.status==RunData.AGENT_NACHBEARBEITUNG);
			agentRecord.logStatusChange(time,RunData.AGENT_NACHBEARBEITUNG,RunData.AGENT_LEERLAUF,null);
		}

		markAgentAsFree(agentRecord,time,data);
	}
}
