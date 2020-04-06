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
package ui.statistic.core.filter;

/**
 * Kapselt einen einzelnen Filter-Befehl
 * @author Alexander Herzog
 * @version 1.0
 */
public interface DataFilterCommand {
	/**
	 * Liefert den Namen des Befehls zurück
	 * @return	Name des Befehls
	 */
	String getName();

	/**
	 * Gibt an, ob der Befehl mit leeren Parametern umgehen kann.
	 * @return	Gibt <code>false</code> zurück, wenn bereits der Interpreter leere Parameter für diesen Befehl als Fehler behandeln soll.
	 */
	boolean allowEmptyParameters();

	/**
	 * Führt den jeweiligen Befehl aus.
	 * @param parameters	Parameter des Befehls
	 * @return	Gibt im Erfolgsfall <code>null</code> zurück, sonst eine Fehlermeldung.
	 */
	String run(String parameters);
}
