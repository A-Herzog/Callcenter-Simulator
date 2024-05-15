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
package ui.model;

import java.util.ArrayList;
import java.util.List;

import language.Language;

/**
 * Diese Klasse speichert alle Daten zu einem Callcenter.<br>
 * Sie wird als Teil der Klasse <code>CallcenterModel</code> verwendet.
 * @author Alexander Herzog
 * @version 1.0
 * @see CallcenterRunModel
 * @see CallcenterModelCallcenter
 */
public final class CallcenterRunModelCallcenter {
	/** Name des Callcenters */
	public final String name;

	/** Liste der Agentengruppen innerhalb des Callcenters */
	public final CallcenterRunModelAgent[] agents;

	/** Benötigte Vermittlungszeit, bevor ein Gespräch beginnt */
	public int technicalFreeTime;

	/** Kann der Kunde das Warten in der Vermittlungszeit noch abbrechen? */
	public final boolean technicalFreeTimeIsWaitingTime;

	/** Globale Score des Callcenters für die Vermittlung von Anrufen */
	public int score;

	/** Faktor für die Agentenscore zur Berücksichtigung der freien Zeit seit dem letzten Anruf */
	public double agentScoreFreeTimeSinceLastCall;
	/** Faktor für die Agentenscore zur Berücksichtigung des Leerlaufanteils */
	public double agentScoreFreeTimePart;

	/** Liste der Namen für die kundenspezifischen Mindestwartezeiten */
	public List<CallcenterRunModelCaller> callerMinWaitingTimeClass;
	/** Liste der kundenspezifischen Mindestwartezeiten */
	public List<Integer> callerMinWaitingTimeMilliSecond;

	/** Liste der Namen für die kundenspezifischen Mindestwartezeiten */
	private final List<String> callerMinWaitingTimeName;

	/**
	 * Konstruktor der Klasse <code>CallcenterRunModelCallcenter</code>
	 * @param editModelCallcenter	Zugehöriges Editor-Modell Callcenter-Objekt
	 * @param editModel	Zugehöriges Gesamt-Editor-Modell
	 */
	public CallcenterRunModelCallcenter(CallcenterModelCallcenter editModelCallcenter, CallcenterModel editModel) {
		if (editModelCallcenter.name==null || editModelCallcenter.name.isEmpty()) name=Language.tr("Model.Check.Callcenter.NoName.Default"); else name=editModelCallcenter.name;

		final List<CallcenterRunModelAgent> agentsList=CallcenterRunModelAgent.buildAgentsList(editModelCallcenter.agents,editModelCallcenter,editModel);
		agents=agentsList.toArray(CallcenterRunModelAgent[]::new);

		technicalFreeTime=editModelCallcenter.technicalFreeTime;
		technicalFreeTimeIsWaitingTime=editModelCallcenter.technicalFreeTimeIsWaitingTime;
		score=editModelCallcenter.score;

		agentScoreFreeTimeSinceLastCall=editModelCallcenter.agentScoreFreeTimeSinceLastCall;
		agentScoreFreeTimePart=editModelCallcenter.agentScoreFreeTimePart;

		callerMinWaitingTimeClass=new ArrayList<>();
		callerMinWaitingTimeMilliSecond=new ArrayList<>();
		for (int i=0;i<editModelCallcenter.callerMinWaitingTime.size();i++) callerMinWaitingTimeMilliSecond.add(editModelCallcenter.callerMinWaitingTime.get(i)*1000);
		callerMinWaitingTimeName=editModelCallcenter.callerMinWaitingTimeName;
	}

	/**
	 * Bereitet das Objekt auf die Simulation vor.
	 * @param caller	Liste mit allen Anrufer-Klassen
	 * @param skills	Liste mit allen Skill-Level-Klassen
	 * @param strict	Strenge Modellprüfung
	 * @return Gibt <code>null</code> zurück, wenn die Initialisierung erfolgreich war, andernfalls wird eine Fehlermeldung als String zurückgegeben,
	 */
	public String checkAndInit(final CallcenterRunModelCaller[] caller, final CallcenterRunModelSkillLevel[] skills, final boolean strict) {
		if (name.isBlank()) {
			if (strict) return Language.tr("Model.Check.Callcenter.NoName");
		}
		if (technicalFreeTime<0) {
			if (strict) return String.format(Language.tr("Model.Check.Callcenter.TechnicalFreeTime"),name);
			technicalFreeTime=0;
		}

		for (int i=0;i<agents.length;i++) {
			String s=agents[i].checkAndInit(caller,skills);
			if (s!=null) return s+" ("+String.format(Language.tr("Model.Check.Callcenter.AgentsGroupError"),agents[i].groupNr+1,name)+")";
		}

		if (callerMinWaitingTimeMilliSecond.size()!=callerMinWaitingTimeName.size()) return String.format(Language.tr("Model.Check.Callcenter.MinimumWaitingTimeInternalError"),name);
		int index=0;
		while (index<callerMinWaitingTimeName.size()) {
			String s=callerMinWaitingTimeName.get(index);
			CallcenterRunModelCaller c=null;
			for (int j=0;j<caller.length;j++) if (caller[j].name.equalsIgnoreCase(s)) {c=caller[j]; break;}
			if (c==null) {
				if (strict && callerMinWaitingTimeMilliSecond.get(index)>0) return String.format(Language.tr("Model.Check.Callcenter.MinimumWaitingTimeUnknownClientType"),name,s);
				callerMinWaitingTimeName.remove(index);
				callerMinWaitingTimeMilliSecond.remove(index);
			} else {
				if (callerMinWaitingTimeMilliSecond.get(index)<0) {
					if (strict) return String.format(Language.tr("Model.Check.Callcenter.InvalidMinimumWaiting"),callerMinWaitingTimeName.get(index),name,callerMinWaitingTimeMilliSecond.get(index));
					callerMinWaitingTimeName.remove(index);
					callerMinWaitingTimeMilliSecond.remove(index);
				} else {
					callerMinWaitingTimeClass.add(c);
					index++;
				}
			}
		}

		return null;
	}

}
