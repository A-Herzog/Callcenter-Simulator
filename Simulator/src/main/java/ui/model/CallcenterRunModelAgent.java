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
 * Modelliert einen einzelnen Agenten<br>
 * Die Klasse wird als Teil der Klasse <code>CallcenterModelCallcenter</code> und damit als Teil der Klasse <code>CallcenterRunModel</code> verwendet.
 * @author Alexander Herzog
 * @version 1.0
 * @see CallcenterRunModelCallcenter
 * @see CallcenterRunModel
 * @see CallcenterRunModelSkillLevel
 * @see CallcenterModelAgent
 */
public final class CallcenterRunModelAgent {
	/** Editor-Modell-Agentengruppe */
	private final CallcenterModelAgent editAgent;

	/** Beginn der Arbeitszeit */
	public final int workingTimeStart;
	/** Ende der Arbeitszeit (laufende Gespräche werden über das Arbeitszeitende hinaus zu Ende geführt) */
	public final int workingTimeEnd;
	/** Arbeitszeitende (<code>workingTimeEnd</code>) berücksichtigen ? */
	public final boolean workingNoEndTime;

	/** Kosten pro Arbeitsstunde (unabhängig von der Auslastung) */
	public final double costPerWorkingHour;

	/** Kundentypabhängige Kosten pro Anruf (kann <code>null</code> sein)*/
	public double[] costPerCall;
	/** Kundentypabhängige Kosten pro Gesprächsminute (kann <code>null</code> sein) */
	public double[] costPerCallMinute;

	/** Verweis auf das zu verwendende <code>CallcenterRunModellSkillLevel</code>-Objekt */
	public CallcenterRunModelSkillLevel skillLevel;

	/** Nummer der Agentengruppe im Edit-Modell, auf der dieser Agent basiert. */
	public final int groupNr;

	private final String skillLevelName;

	/** Konstruktor der Klasse <code>CallcenterRunModelAgent</code>
	 * @param editModel	Editor-Modell-Agentengruppe
	 * @param groupNr Nummer der Agentengruppe
	 */
	public CallcenterRunModelAgent(final CallcenterModelAgent editModel, final int groupNr) {
		editAgent=editModel;
		workingTimeStart=editModel.workingTimeStart;
		workingTimeEnd=editModel.workingTimeEnd;
		workingNoEndTime=editModel.workingNoEndTime;
		costPerWorkingHour=editModel.costPerWorkingHour;
		costPerCall=null;
		costPerCallMinute=null;
		skillLevel=null;
		skillLevelName=editModel.skillLevel;
		this.groupNr=groupNr;
	}

	/**
	 * Bereitet das Objekt auf die Simulation vor.
	 * @param caller	Liste mit allen Kundengruppen
	 * @param skills	Liste mit allen Skill-Level-Klassen
	 * @return Gibt <code>null</code> zurück, wenn die Initialisierung erfolgreich war, andernfalls wird eine Fehlermeldung als String zurückgegeben,
	 */
	public String checkAndInit(final CallcenterRunModelCaller[] caller, final CallcenterRunModelSkillLevel[] skills) {
		for (int i=0;i<skills.length;i++) if (skills[i].name.equalsIgnoreCase(skillLevelName)) {skillLevel=skills[i]; break;}
		if (skillLevel==null) return String.format(Language.tr("Model.Check.Agents.UnknownSkillLevel"),skillLevelName);

		if (!workingNoEndTime && workingTimeEnd<workingTimeStart) return Language.tr("Model.Check.Agents.WorkingTimeEndsBeforeStart");

		for (int i=0;i<caller.length;i++) {
			int index=-1;
			String s=caller[i].name;
			for (int j=0;j<editAgent.costCallerTypes.size();j++) if (editAgent.costCallerTypes.get(j).equalsIgnoreCase(s)) {index=j; break;}
			if (index<0) continue;

			if (costPerCall==null) {
				costPerCall=new double[caller.length];
				costPerCallMinute=new double[caller.length];
			}
			costPerCall[i]=editAgent.costPerCall.get(index);
			costPerCallMinute[i]=editAgent.costPerCallMinute.get(index);
		}

		return null;
	}

	private static void addAgentGroupToRunList(final CallcenterModelAgent agentList, final int groupNr, final CallcenterModelCallcenter callcenter, final CallcenterModel model, final List<CallcenterRunModelAgent> runList) {
		if (agentList.count>=0) {
			/* Agentengruppe mit vorgegebenen Arbeitszeiten anlegen */
			for (int i=0;i<agentList.count;i++) runList.add(new CallcenterRunModelAgent(agentList,groupNr));
			return;
		}

		/* Schichtplan aufbauen */
		final List<CallcenterModelAgent> list=agentList.calcAgentShifts(agentList.lastShiftIsOpenEnd,callcenter,model,true);
		for (CallcenterModelAgent a : list) addAgentGroupToRunList(a,groupNr,callcenter,model,runList);
	}

	/**
	 * Erstellt eine Liste mit Laufzeit-Agenten für ein Callcenter
	 * @param editAgents	Zugehörige Editor-Agenten-Gruppen
	 * @param callcenter	Zu betrachtendes Callcenter
	 * @param model	Gesamtes Callcenter-Modell
	 * @return	Liste mit Laufzeit-Agenten für ein Callcenter
	 */
	public static List<CallcenterRunModelAgent> buildAgentsList(final List<CallcenterModelAgent> editAgents, final CallcenterModelCallcenter callcenter, final CallcenterModel model) {
		final List<CallcenterRunModelAgent> runList=new ArrayList<CallcenterRunModelAgent>();
		for (int i=0;i<editAgents.size();i++) if (editAgents.get(i).active)
			addAgentGroupToRunList(editAgents.get(i),i,callcenter,model,runList);
		return runList;
	}
}
