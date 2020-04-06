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
package ui.importer;

import language.Language;
import ui.model.CallcenterModel;
import ui.model.CallcenterModelAgent;
import ui.model.CallcenterModelCaller;

/**
 * Dies ist die abstrakte Basisklasse für alle Importprozessoren.
 * @author Alexander Herzog
 * @version 1.0
 */
public abstract class ImporterProcessor {
	/**
	 * Von welcher Art sind die Parameter für diesen Importprozessor?
	 * @author Alexander Herzog
	 * @see ImporterProcessor#getParameterType()
	 */
	public enum ParameterType {
		/** Gibt an, dass der Parameter einen Kundentyp beschreiben. */
		PARAMETER_TYPE_CALLER,

		/** Gibt an, dass der Parameter eine Agentengruppe beschreibt. */
		PARAMETER_TYPE_AGENT_GROUP,

		/** Gibt an, dass der Parameter eine Zeit in einem Skill-Level beschreibt. */
		PARAMETER_TYPE_SKILL_TIME
	}

	/**
	 * Liefert den Namen des Importprozessors
	 * @return Name des Importprozessors
	 */
	public abstract String[] getNames();

	/**
	 * Gibt zurück von welcher Art die Parameter für diesen Importprozessor sind.
	 * @return Type des Parameters für diesen Importprozessor
	 */
	public abstract ParameterType getParameterType();

	/**
	 * Gibt an, ob die zu ladenden Daten Zahlen oder Zeichenketten sind.
	 * Es wird dann entsprechen entweder <code>processNumbers</code> oder <code>processStrings</code> aufgerufen.
	 * @return Gibt <code>true</code> zurück, wenn Zahlen gewünscht sind.
	 * @see #processNumbers(CallcenterModel, String, double[])
	 * @see #processStrings(CallcenterModel, String, String[])
	 */
	public boolean isNumbericalParameter() {
		return true;
	}

	/**
	 * Führt den Importvorgang aus
	 * @param model	Callcenter-Modell, in das die Daten importiert werden sollen
	 * @param parameter	 Zusätzlicher Parameter, der angibt, was importiert werden soll
	 * @param data	 Datenfelder die importiert werden sollen
	 * @return	Liefert im Erfolgsfall <code>null</code> zurück, sonst eine entsprechende Fehlermeldung.
	 */
	public String processNumbers(CallcenterModel model, String parameter, double[] data) {
		return null;
	}

	/**
	 * Führt den Importvorgang aus
	 * @param model	Callcenter-Modell, in das die Daten importiert werden sollen
	 * @param parameter	 Zusätzlicher Parameter, der angibt, was importiert werden soll
	 * @param data	 Datenfelder die importiert werden sollen
	 * @return	Liefert im Erfolgsfall <code>null</code> zurück, sonst eine entsprechende Fehlermeldung.
	 */
	public String processStrings(CallcenterModel model, String parameter, String[] data) {
		return null;
	}

	/**
	 * Sucht einen zu dem angegebenen Parameter passenden Kundentyp
	 * @param model	Callcenter-Modell, in dem gesucht werden soll
	 * @param parameter	Parameter zu dem der passende Kundentyp gesucht werden soll
	 * @return	Gibt den Kundentyp zurück oder im Fehlerfall <code>null</code>
	 */
	protected final CallcenterModelCaller getCallerFromParameter(CallcenterModel model, String parameter) {
		for (int i=0;i<model.caller.size();i++) if (model.caller.get(i).name.equalsIgnoreCase(parameter)) return model.caller.get(i);
		return null;
	}

	/**
	 * Sucht eine zu dem angegebenen Parameter passende Agentengruppe
	 * @param model	Callcenter-Modell, in dem gesucht werden soll
	 * @param parameter	Parameter zu dem die passende Agentengruppe gesucht werden soll
	 * @return	Gibt die Agentengruppe zurück oder im Fehlerfall <code>null</code>
	 */
	protected final CallcenterModelAgent getAgentFromParameter(CallcenterModel model, String parameter) {
		for (int i=0;i<model.callcenter.size();i++) {
			String s=model.callcenter.get(i).name;
			for (int j=0;j<model.callcenter.get(i).agents.size();j++) {
				String t=s+" - "+Language.tr("Importer.AgentsGroup")+" "+(j+1);
				if (t.equals(parameter)) return model.callcenter.get(i).agents.get(j);
			}
		}
		return null;
	}

	/**
	 * Sucht einen zu dem angegebenen Parameter passenden Skill-Level
	 * @param model	Callcenter-Modell, in dem gesucht werden soll
	 * @param parameter	Parameter zu dem der passende Skill-Level gesucht werden soll
	 * @return	Gibt ein Array aus zwei Elementen zurück: ein Objekt vom Typ <code>CallcenterModelSkillLevel</code> und die Nummer des Kundentyps innerhalb des Skill-Levels oder im Fehlerfall insgesamt <code>null</code>
	 */
	protected final Object[] getSkillLevelFromParameter(CallcenterModel model, String parameter) {
		for (int i=0;i<model.skills.size();i++) {
			String s=model.skills.get(i).name;
			for (int j=0;j<model.skills.get(i).callerTypeName.size();j++) {
				String t;
				t=s+" - "+model.skills.get(i).callerTypeName.get(j);
				if (t.equals(parameter)) return new Object[] {model.skills.get(i),j};
			}
		}
		return null;
	}
}
