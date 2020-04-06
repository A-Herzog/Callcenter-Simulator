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
package ui;

import java.io.File;

import language.Language;
import simulator.Statistics;
import tools.SetupData;
import ui.statistic.core.filter.DataFilter;
import ui.statistic.core.filter.DataFilterBase;

/**
 * Führt die automatische Speicherung der Simulationsergebnisse durch
 * @author Alexander Herzog
 * @version 1.0
 */
public class AutoSave {
	private final Statistics statistic;

	/**
	 * Konstruktor der Klasse <code>AutoSave</code>
	 * @param statistic	Statistikobjekt, dessen Daten gespeichert werden sollen
	 */
	public AutoSave(Statistics statistic) {
		this.statistic=statistic;
	}

	private String createFolder(File dir) {
		if (dir==null) return null;
		if (dir.isFile()) return String.format(Language.tr("AutoSave.ErrorOutputFolderExistsAsFile"),dir);
		if (!dir.isDirectory()) {
			if (!dir.mkdir()) return String.format(Language.tr("AutoSave.ErrorOutputFolder"),dir);
		}
		return null;
	}

	/**
	 * Speichert die Statistikdaten als xml-Datei
	 * @param folder	Ausgabeordner
	 * @return	Gibt <code>null</code> zurück, wenn die Daten gespeichert werden konnten, sonst wird eine Fehlermeldung zurückgegeben
	 */
	public String saveStatistic(String folder) {
		/* Ausgabeverzeichnis vorbereiten */
		final File dir=new File(folder);
		final String s=createFolder(dir); if (s!=null) return s;

		/* Nächsten Dateinamen suchen und Statistik speichern */
		int nr=1;
		while (true) {
			File output=new File(dir,String.format("Statistik-%04d.xml",nr));
			if (!output.exists()) {
				if (!statistic.saveToFile(output)) return String.format(Language.tr("AutoSave.ErrorStatistic"),output);
				return null;
			}
			nr++;
		}
	}

	/**
	 * Speichert die gefilterten Statistikergebnisse in einer Datei
	 * @param script	Auszuführendes externes Script. Wird hier <code>null</code> oder ein leerer String übergeben, so wird das im Setup gespeicherte Script ausgeführt.
	 * @param output	Ausgabedatei, an die die gefilterten Daten angehängt werden sollen.
	 * @return	Gibt <code>null</code> zurück, wenn die Daten gespeichert werden konnten, sonst wird eine Fehlermeldung zurückgegeben
	 */
	public String saveFilter(String script, String output) {
		/* Ausgabeverzeichnis vorbereiten */
		File file=new File(output);
		String s=createFolder(file.getParentFile()); if (s!=null) return s;

		/* Filter-Script laden */
		String filterScript="";
		if (script==null || script.isEmpty()) {
			filterScript=SetupData.getSetup().filter[0];
		} else {
			filterScript=DataFilterBase.loadText(new File(script));
			if (filterScript==null) return String.format(Language.tr("AutoSave.Error.FilterLoad"),script);
		}

		/* Script ausführen */
		DataFilter filter=new DataFilter(statistic.saveToDocument());
		if (!filter.run(filterScript,false)) return String.format(Language.tr("AutoSave.Error.FilterExecute"),filter.getResults());

		/* Ergebnis speichern */
		DataFilterBase.saveText(filter.getResults(),new File(output),true);
		return null;
	}
}
