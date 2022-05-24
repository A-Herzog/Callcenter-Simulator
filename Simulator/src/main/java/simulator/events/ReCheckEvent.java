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
import simulator.RunData.AgentRecord;
import simulator.RunData.CallerRecord;
import simulator.SimulationData;

/**
 * Ereignis: Erneute Prüfung verfügbarer Agenten nach Mindestwartezeit
 * @author Alexander Herzog
 * @version 1.0
 */
public final class ReCheckEvent extends Event {
	/** Kunden auf den sich die Prüfung bezieht */
	public CallerRecord callerRecord;

	/**
	 * Konstruktor der Klasse
	 */
	public ReCheckEvent() {
		/*
		 * Wird nur benötigt, um einen JavaDoc-Kommentar für diesen (impliziten) Konstruktor
		 * setzen zu können, damit der JavaDoc-Compiler keine Warnung mehr ausgibt.
		 */
	}

	/* (non-Javadoc)
	 * @see simcore.Event#run(simcore.SimData)
	 */
	@Override
	public void run(final SimData data) {
		/* Recheck-Event aus der reCheckEvents-Liste entfernen */
		if (callerRecord.reCheckEvents.length==1) callerRecord.reCheckEvents=null; else {
			final Event[] old=callerRecord.reCheckEvents;
			callerRecord.reCheckEvents=new Event[old.length-1];
			int j=0; for (int i=0;i<old.length;i++) if (old[i]!=this) {callerRecord.reCheckEvents[j]=old[i]; j++;}
		}

		if (data.loggingActive) LogTools.log(data,Language.tr("Simulation.Log.ReCheck"),callerRecord,null,null);

		/* Erneut prüfen, ob es einen passenden Agenten gibt */
		final AgentRecord agent=((SimulationData)data).dynamicSimData.findAgentForCaller(time,callerRecord,data,true);

		if (agent!=null && data.loggingActive) LogTools.log(data,Language.tr("Simulation.Log.Ready.Match"),callerRecord,agent,null);
	}

}
