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

import language.Language;
import simcore.SimData;
import simulator.RunData.AgentRecord;
import simulator.RunData.CallerRecord;

/**
 * Diese Klasse enthält statische Methoden zur Erstellung
 * von Logging-Strings.
 * @author Alexander Herzog
 */
public class LogTools {
	/**
	 * Liefert Informationen zu einem Anrufer-Datensatz
	 * @param caller	Anrufer-Datensatz
	 * @return	Informations-String
	 */
	public static final String callerData(final CallerRecord caller) {
		String info=
				""+
						Language.tr("Simulation.Log.General.Type")+" ("+Language.tr("Simulation.Log.General.Client")+"): "+caller.statisticClient.name+ ", "+
						Language.tr("Simulation.Log.General.Type")+" ("+Language.tr("Simulation.Log.General.Call")+"): "+caller.statisticCall.name+", "+
						Language.tr("Simulation.Log.General.PreviousTrys")+": "+caller.retryCount+", "+
						Language.tr("Simulation.Log.General.Forwarded")+": "+(caller.callContinued?Language.tr("Simulation.Log.General.yes"):Language.tr("Simulation.Log.General.no"));
		return Language.tr("Simulation.Log.General.Client")+": "+SimData.formatObjectID(caller.toString())+" ("+info+")\n";
	}

	/**
	 * Liefert Informationen zu einem Agenten-Datensatz
	 * @param agent	Agenten-Datensatz
	 * @return	Informations-String
	 */
	public static final String agentData(final AgentRecord agent) {
		String info=
				""+
						Language.tr("Simulation.Log.General.Callcenter")+": "+agent.statisticProCallcenter.name+", "+
						Language.tr("Simulation.Log.General.SkillLevel")+": "+agent.statisticProSkilllevel.name+", "+
						Language.tr("Simulation.Log.General.ShiftStart")+": "+SimData.formatSimTime(agent.agent.workingTimeStart*1000)+", "+
						Language.tr("Simulation.Log.General.ShiftEnd")+": "+(agent.agent.workingNoEndTime?"open end":SimData.formatSimTime(agent.agent.workingTimeEnd*1000));
		return Language.tr("Simulation.Log.General.Agent")+": "+SimData.formatObjectID(agent.toString())+" ("+info+")\n";
	}

	/**
	 * Erfasst eine Logging-Meldung.<br>
	 * Es muss zuvor per {@link SimData#loggingActive} geprüft werden,
	 * ob das Logging überhaupt aktiv sein soll.
	 * @param data	Simulationsdatenobjekt
	 * @param name	Titel der Meldung
	 * @param caller	Kundendatenobjekt (kann <code>null</code> sein)
	 * @param agent	Agentendatenobjekt (kann <code>null</code> sein)
	 * @param info	Zusätzlicher Infotext (kann <code>null</code> sein)
	 */
	public static final void log(final SimData data, final String name, final CallerRecord caller, final AgentRecord agent, final String info) {
		String lines="";
		if (caller!=null) lines+=callerData(caller);
		if (agent!=null) lines+=agentData(agent);
		if (info!=null && !info.isEmpty()) lines+=info;
		data.logEventExecution(name,-1,lines);
	}
}
