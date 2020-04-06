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

import java.io.File;

import net.calc.MultiComplexCallcenterSimulator;
import net.calc.RemoteAndLocalComplexCallcenterSimulator;
import net.calc.RemoteComplexCallcenterSimulator;
import net.calc.StartAnySimulator;

/**
 * Dieses Interface ermöglicht den Zugriff auf die Funktionen
 * des Simulators ohne dass die Methode, die dieses Interface
 * verwendet, wissen muss, welche Simulator-Klasse sich genau
 * dahinter verbirgt.
 * @author Alexander Herzog
 * @see MultiComplexCallcenterSimulator
 * @see RemoteAndLocalComplexCallcenterSimulator
 * @see RemoteComplexCallcenterSimulator
 * @see Simulator
 * @see StartAnySimulator
 */
public interface CallcenterSimulatorInterface {

	/**
	 * Startet die Simulationssthreads.
	 * @param lowPriority Wird hier <code>true</code> übergeben, so werden die Thrads mit niedriger Priorität gestartet, sonst mit normaler Priorität. Die Einstellung kann auch später über <code>setPriority</code> verändert werden.
	 */
	void start(boolean lowPriority);

	/**
	 * Bricht die Simulation vorzeitig ab.
	 */
	void cancel();

	/**
	 * Prüft, ob die Simulationsthreads noch laufen.
	 * @return Liefert <code>true</code> wenn mindestens ein Thread noch aktiv ist.
	 */
	boolean isRunning();

	/**
	 * Wartet bis alle Simulationsthreads beendet sind und berechnet dann die gesamte Laufzeit.
	 * @return Liefert <code>null</code> zurück, wenn die Simulation erfolgreich beendet wurde (sonst Fehlermeldung)
	 */
	String finalizeRun();

	/**
	 * Führt nach dem Ende der Simulation die Statistikdaten der einzelnen Threads zusammen und
	 * gibt diese als gemeinsames Objekt des Typs <code>ComplexStatisticSimData</code> zurück.
	 * @return Statistikobjekt mit Daten, die von allen Threads zusammengeführt wurden.
	 */
	Statistics collectStatistic();

	/**
	 * Gibt die Summe der in allen Threads bisher simulierten Ereignisse zurück.
	 * @return Anzahl der simulierten Ereignisse in allen Threads
	 */
	long getEventCount();

	/**
	 * Gibt zurück, wie viele Ereignisse pro Sekunde verarbeitet werden.
	 * @return	Ereignisse pro Sekunde
	 */
	int getEventsPerSecond();

	/**
	 * Gibt die Nummer des gerade im Simulator in Bearbeitung befindlichen Tages zurück.
	 * @return Gerade in Arbeit befindlicher Tag
	 */
	long getSimDayCount();

	/**
	 * Gbit die Gesamtzahl der zu simulierenden Tage zurück.
	 * @return Gesamtzahl der zu simulierenden Tage.
	 */
	long getSimDaysCount();

	/**
	 * Stellt die Priorität der Rechenthreads ein.
	 * (Die Funktion kann erst nach dem Start der Threads per <code>start()</code> verwendet werden.)
	 * @param low	Ist dieser Parameter <code>true</code>, so wird mit verminderter Priorität gerechnet, sonst mit normaler.
	 */
	void setPriority(boolean low);

	/**
	 * Gibt den Dateinamen der Datei, in der die Log-Ergebnisse geschrieben werden sollen, zurück oder <code>null</code>, wenn keine Logdatei angegeben wurde.
	 * @return	Dateiname der Logdatei (oder <code>null</code>)
	 */
	File getLogFile();
}