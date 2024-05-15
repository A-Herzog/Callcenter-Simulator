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
package ui.dataloader;

import java.io.File;
import java.util.List;

import language.Language;
import mathtools.MultiTable;
import mathtools.NumberTools;
import mathtools.Table;
import ui.model.CallcenterModel;

/**
 * Abstrakte Basisklasse zur Erzeugung von Callcenter-Modellen aus einer Anrufer- und einer Agententabelle.<br>
 * Diese Klasse stellt Methoden zum Landen und Vorverarbeiten von Tabellen bereit, so dass hiervon abgeleitete Klassen
 * zur eigentlichen Verarbeitung sich ganz auf eben diese konzentrieren können.
 * @author Alexander Herzog
 * @version 1.0
 */
public abstract class SimpleModelBaseLoader {
	/**
	 * Konstruktor der Klasse
	 */
	public SimpleModelBaseLoader() {
		/*
		 * Wird nur benötigt, um einen JavaDoc-Kommentar für diesen (impliziten) Konstruktor
		 * setzen zu können, damit der JavaDoc-Compiler keine Warnung mehr ausgibt.
		 */
	}

	/**
	 * Lädt eine Tabelle aus einer Datei.
	 * @param file	Tabellendatei
	 * @param table	Optional Name der Tabelle in der Arbeitsmappe
	 * @return	Liefert im Erfolgsfall ein {@link Table}-Objekt, sonst eine Fehlermeldung
	 */
	private final Object loadTable(final File file, final String table) {
		/* Datei vorhanden und ladbar? */
		if (!file.exists()) return String.format(Language.tr("Loader.ProcessError.FileDoesNotExist"),file.toString());
		MultiTable multi=new MultiTable();
		if (!multi.load(file) || multi.size()==0) return String.format(Language.tr("Loader.ProcessError.Load"),file.toString());

		/* Kein Name angegeben -> erste Tabelle der Mappe */
		if (table==null || table.isBlank()) return multi.get(0);

		/* Gibt's eine Tabelle mit dem angegebenen Namen?*/
		Table t=multi.get(table);
		if (t!=null) return t;

		return String.format(Language.tr("Loader.ProcessError.Load"),file.toString());
	}

	/**
	 * Entfernt aus einer Tabelle alle Spalten vor einer Startspalte.
	 * @param table	Tabelle
	 * @param startCol	Startspalte
	 * @return	Tabelle bei der die Spalten vor der Startspalte entfernt wurde (oder im Fehlerfall eine Fehlermeldung)
	 */
	private final Object removeColumns(final Table table, final String startCol) {
		if (startCol==null || startCol.isBlank()) return table;

		int col=Table.numberFromColumnName(startCol);
		if (col<0) {
			Long L=NumberTools.getPositiveLong(startCol);
			if (L!=null) col=(int)(L-1);
		}
		if (col<0) return String.format(Language.tr("Loader.InvalidColumnName"),startCol);

		if (col==0) return table.clone();
		Table tempTable=table.transpose();
		Table newTable=new Table();

		String[][] data=tempTable.getDataArray();
		for (int i=col;i<data.length;i++) newTable.addLine(data[i]);

		return newTable.transpose();
	}

	/**
	 * Läd eine Tabelle aus einer Arbeitsmappe und entfernt ggf. einige Spalten.
	 * @param file	Zu ladende Datei
	 * @param table	Tabelle in der Arbeitsmappe (kann <code>null</code> oder leer sein, dann wird die erste Tabelle verwendet)
	 * @param startCol	Startspalte (kann <code>null</code> oder leer sein, dann wird "A" angenommen, d.h. es werden dann keine Spalten entfernt)
	 * @return	Gibt im Erfolgsfall die Tabelle zurück, im Fehlerfall einen String mit der Fehlermeldung
	 */
	protected final Object processTable(File file, String table, String startCol) {
		Object O=loadTable(file,table);
		if (O instanceof String) return O;
		return removeColumns((Table)O,startCol);
	}

	/**
	 * Erstellt einen durch ";" getrennten String, der als Verteilung aufgefasst werden kann aus einem Teil einer String-Liste.
	 * @param list	String-Liste, der die Werte zu entnehmen sind
	 * @param from	Erster zu verwendender Index in der Liste (also inklusive)
	 * @param to	Letzter zu verwendender Index in der Liste (also inklusive)
	 * @return	Zeichenkette, die die Daten in durch ";" getrennter Form als Zeichenkette enthält
	 */
	protected final String getDistLoaderString(List<String> list, int from, int to) {
		StringBuilder sb=new StringBuilder();
		for (int i=from;i<=to;i++) {if (i>from) sb.append(";"); sb.append(list.get(i));}
		return sb.toString();
	}

	/**
	 * Methode, die die eigentliche Verarbeitung durchführt
	 * @return	Liefert im Erfolgsfall ein Objekt vom Typ <code>CallcenterModel</code> sonst einen String mit der Fehlermeldung.
	 */
	protected abstract Object buildModelIntern();

	/**
	 * Erstellt aus den geladenen Daten ein Callcenter-Modell
	 * @return	Liefert im Erfolgsfall ein Objekt vom Typ <code>CallcenterModel</code> sonst einen String mit der Fehlermeldung.
	 */
	public final Object buildModel() {
		Object O=buildModelIntern();
		if (O instanceof String) return O;
		CallcenterModel model=(CallcenterModel)O;
		model.description=model.generateDescription();
		return model;
	}

	/**
	 * Speichert ein Callcenter-Modell in einer Datei
	 * @param model	Zu speicherndes Modell
	 * @param outputFile	Datei, in der das Modell gespeichert werden soll
	 * @return	Gibt im Erfolgsfall <code>null</code> zurück, sonst die Fehlermeldung als String
	 */
	public static String saveModel(CallcenterModel model, File outputFile) {
		if (!model.saveToFile(outputFile)) return String.format(Language.tr("Editor.Save.Error"),outputFile.toString());
		return null;
	}

	/**
	 * Erstellt aus den geladenen Daten ein Callcenter-Modell und speichert dieses als Datei.
	 * @param outputFile	Datei, in der das Callcenter-Modell gespeichert werden soll.
	 * @return	Gibt im Erfolgsfall <code>null</code> zurück, sonst einen String mit der Fehlermeldung.
	 */
	public final String buildModel(File outputFile) {
		Object O=buildModel();
		if (O instanceof String) return (String)O;
		return saveModel((CallcenterModel)O,outputFile);
	}
}
