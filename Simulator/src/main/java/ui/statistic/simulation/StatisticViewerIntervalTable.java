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
package ui.statistic.simulation;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import language.Language;
import mathtools.Table;
import mathtools.TimeTools;
import simulator.Statistics;
import systemtools.statistics.StatisticViewerTable;

/**
 * Basisklasse zur Anzeige einer Tabelle mit Statistikdaten auf Intervallbasis
 * @author Alexander Herzog
 * @version 1.0
 */
public class StatisticViewerIntervalTable extends StatisticViewerTable {

	/**
	 * Erzeugt die Tabelle
	 * @param statistic	{@link Statistics}-Objekt, dem die Daten entnommen werden sollen
	 * @param colNames	Namen der einzelnen Spalten (die "Intervall"-Spalte wird automatisch erzeugt und muss hier nicht mit aufgeführt werden)
	 * @param sumRow	Optional Name für eine Summen-Zeile (bei <code>null</code> wird keine Zeile erzeugt)
	 */
	protected void buildIntervalTable(Statistics statistic, String[] colNames, String sumRow) {
		setData(getTableData(statistic,colNames.length,sumRow),getTableHeading(colNames));
	}

	/**
	 * Liefert eine Zeile der Tabelle
	 * @param statistic	statistic	{@link Statistics}-Objekt, dem die Daten entnommen werden sollen
	 * @param colNr	Nummer der zu erzeugenden Spalte (die erste Spalte nach der "Intervall"-Spalte trägt die Nummer 0)
	 * @return	48 Strings, die die Spalte bilden
	 */
	protected String[] getUserCol(Statistics statistic, int colNr) {
		return new String[0];
	}

	/**
	 * Liefert die Spalten mit den Intervall-Zeitangaben.
	 * @param sumRow	Beschriftung der Summen-Zeile
	 * @return	Spalten mit den Intervall-Zeitangaben
	 * @see #getTableData(Statistics, int, String)
	 */
	private String[] getTimeRow(String sumRow) {
		String[] row=new String[48+((sumRow!=null)?1:0)];
		for (int i=0;i<48;i++) row[i]=TimeTools.formatTime(i*1800)+"-"+TimeTools.formatTime((i+1)*1800-1);
		if (sumRow!=null) row[48]=sumRow;
		return row;
	}

	/**
	 * Erzeugt die Ausgabetabelle.
	 * @param statistic	{@link Statistics}-Objekt, dem die Daten entnommen werden sollen
	 * @param userColCount	Anzahl der benutzerdefinierten Spalten
	 * @param sumRow	Optional Name für eine Summen-Zeile (bei <code>null</code> wird keine Zeile erzeugt)
	 * @return	Ausgabetabelle
	 * @see #buildIntervalTable(Statistics, String[], String)
	 */
	private Table getTableData(Statistics statistic, int userColCount, String sumRow) {
		Table table=new Table();
		table.addLine(getTimeRow(sumRow));
		for (int i=0;i<userColCount;i++) {
			String[] col=getUserCol(statistic,i);
			if (col!=null) table.addLine(col);
		}
		return table.transpose();
	}

	/**
	 * Liefert die Spaltenüberschriften.
	 * @param colNames	Namen der einzelnen Spalten (die "Intervall"-Spalte wird automatisch erzeugt und muss hier nicht mit aufgeführt werden)
	 * @return	Spaltenüberschriften
	 * @see #buildIntervalTable(Statistics, String[], String)
	 */
	private String[] getTableHeading(String[] colNames) {
		List<String> list=new ArrayList<String>(Arrays.asList(colNames));
		list.add(0,Language.tr("Statistic.Interval"));
		return list.toArray(new String[0]);
	}
}
