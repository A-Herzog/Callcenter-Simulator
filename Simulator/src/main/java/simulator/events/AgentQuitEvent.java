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
import simulator.SimulationData;

/**
 * Dienstende Ereignis für einen Agenten
 * @author Alexander Herzog
 * @version 1.0
 */
public final class AgentQuitEvent extends Event {
	/**
	 * Agent für den das Dienstende erreicht ist
	 */
	public AgentRecord agentRecord;

	/* (non-Javadoc)
	 * @see simcore.Event#run(simcore.SimData)
	 */
	@Override
	public void run(final SimData data) {
		/* Wenn Agent gerade arbeitet: Zu Ende arbeiten lassen. Feierabend wird danach automatisch ausgelöst. */
		if (agentRecord.status!=RunData.AGENT_LEERLAUF) return;

		if (data.loggingActive) LogTools.log(data,Language.tr("Simulation.Log.Quit"),null,agentRecord,null);

		((SimulationData)data).dynamicSimData.freeAgents.remove(agentRecord);
		agentRecord.status=RunData.AGENT_NACH_DIENST;
		agentRecord.logStatusChange(time,RunData.AGENT_LEERLAUF,RunData.AGENT_NACH_DIENST,null);

		/* Prüfen, ob der Tag evtl. zu Ende ist */
		StopTestEvent.runStopTest((SimulationData)data);
	}
}
