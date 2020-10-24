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
package net.web;

import java.io.File;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import simulator.Statistics;
import ui.model.CallcenterModel;

/**
 * Fasst die Informationen zu einem Ordner mit Statistik-Daten für die Ausgabe über den Webserver zusammen
 * @author Alexander Herzog
 * @version 1.0
 */
public class StatisticFolder {
	private static final long UPDATE_SECONDS=2;

	/**
	 * Verzeichnis, über den dieses Objekt Daten enthält
	 */
	public final File folder;

	/**
	 * Anzeigename (entweder im Konstruktor explizit angegeben, ansonsten der letzte Abschnitt des Verzeichnisnamens)
	 */
	public final String name;

	private File[] fileNames;
	private Date[] fileDates;
	private long lastUpdate;

	/**
	 * Konstruktor der Klasse <code>StatisticFolder</code>
	 * @param folderName	Verzeichnis, auf das sich dieses Objekt beziehen soll
	 * @param name	Anzeigename (kann leer oder <code>null</code> sein; in diesem Fall wird der letzte Abschnitt des Verzeichnisnamens als Anzeigenname verwendet)
	 */
	public StatisticFolder(final String folderName, final String name) {
		folder=new File(folderName);
		if (name==null || name.trim().isEmpty()) this.name=folder.getName(); else this.name=name;
		lastUpdate=-1;
	}

	/**
	 * Konstruktor der Klasse <code>StatisticFolder</code>
	 * @param folder	Verzeichnis, auf das sich dieses Objekt beziehen soll
	 * @param name	Anzeigename (kann leer oder <code>null</code> sein; in diesem Fall wird der letzte Abschnitt des Verzeichnisnamens als Anzeigenname verwendet)
	 */

	public StatisticFolder(final File folder, final String name) {
		this.folder=folder;
		if (name==null || name.trim().isEmpty()) this.name=folder.getName(); else this.name=name;
		lastUpdate=-1;
	}

	/**
	 * Gibt an, ob es sich bei dem angegebenen Verzeichnis um ein existierendes Verzeichnis handelt
	 * @return	Gibt <code>true</code> zurück, wenn das angegebene Verzeichnis existiert
	 */
	public boolean isDirectory() {
		return folder.isDirectory();
	}

	/**
	 * Aktualisiert die Dateilisten
	 * (Wird von <code>listFiles</code>, <code>getFileDates</code> und <code>isByDateAvailable</code> aufgerufen, muss daher eigentlich nie direkt aufgerufen werden.)
	 * @param force	Wird hier <code>true</code> übergeben, so wird ein Update erzwungen; ansonsten wird nur aktualisiert, wenn die Daten älter als <code>UPDATE_SECONDS</code> Sekunden sind.
	 * @see #listFiles()
	 * @see #getFileDates()
	 * @see #isByDateAvailable()
	 */
	public void update(boolean force) {
		long time=System.currentTimeMillis();

		if (!isDirectory()) {
			fileNames=new File[0];
			fileDates=new Date[0];
			lastUpdate=time;
			return;
		}

		if (!force && lastUpdate+1000*UPDATE_SECONDS>time) return;
		lastUpdate=time;

		fileNames=readFileList();
		fileDates=new Date[fileNames.length];

		for (int i=0;i<fileNames.length;i++) fileDates[i]=getModelDate(fileNames[i]);
	}

	private File[] readFileList() {
		File[] files=folder.listFiles();
		List<File> result=new ArrayList<File>();

		if (files!=null) for (File file: files) if (file!=null && file.isFile()) {
			String[] parts=file.toString().split("\\.");
			if (parts.length>1 && parts[parts.length-1].equalsIgnoreCase("xml")) result.add(file);
		}

		return result.toArray(new File[0]);
	}

	private Date getModelDate(final File modelFile) {
		Statistics statistic=new Statistics(null,null,0,0);
		if (statistic.loadFromFile(modelFile)!=null) return null;
		String s=statistic.editModel.date;
		if (s==null || s.trim().isEmpty()) return null;
		return CallcenterModel.stringToDate(s);
	}

	/**
	 * Liefert ein Array aller xml-Dateien in dem angegebenen Verzeichnis
	 * @return	Array mit allen xml-Dateien in dem Verzeichnis (kann leer sein, ist aber nie <code>null</code>)
	 */
	public File[] listFiles() {
		update(false);
		return fileNames;
	}

	/**
	 * Liefert ein Array mit den Modelldatums-Angaben der eingebetteten Modelldateien in die xml-Statistik-Dateien in dem
	 * Verzeichnis. Handelt es sich bei einer xml-Datei nicht um eine gültige Statistik-Datei oder ist in dem Modell kein
	 * Modell-Datum hinterlegt, so wird <code>null</code> in dem jeweiligen Array-Eintrag zurückgeliefert.
	 * @return	Array mit den Modell-Datumsangaben der einzelnen Statistik-xml-Dateien (kann leer sein, ist aber nie <code>null</code>)
	 */
	public Date[] getFileDates() {
		update(false);
		return fileDates;
	}

	/**
	 * Prüft, ob zumindest eine der Statistik-xml-Dateien in dem Verzeichnis ein Modell-Datum besitzt.
	 * @return	Liefert <code>true</code> zurück, wenn mindestens eine der Statistik-xml-Dateien in dem Verzeichnis ein Modell-Datum besitzt.
	 */
	public boolean isByDateAvailable() {
		update(false);
		for (Date d : fileDates) if (d!=null) return true;
		return false;
	}

	/**
	 * Prüft ob die Datei mit dem angegebenen Dateinamen in dem Verzeichnis existiert und liefert wenn ja ein <code>File</code>-Objekt darauf zurück
	 * @param fileName	Dateiname (ohne Pfad) der gesuchten Datei
	 * @return	<code>File</code>-Objekt auf die angegebene Datei oder <code>null</code> im Fehlerfall.
	 */
	public File getFileByName(String fileName) {
		if (!isDirectory()) return null;
		if (fileName==null || fileName.trim().isEmpty()) return null;
		if (fileName.contains("/") || fileName.contains("\\") || fileName.contains("..")) return null;
		File file=new File(folder,fileName);
		if (!file.isFile()) return null;
		return file;
	}

	/**
	 * Prüft ob die Datei mit dem angegebenen Dateinamen in dem Verzeichnis existiert als Statistik-Datei geladen werden kann.
	 * Wenn ja liefert die Funktion ein Statistik-Objekt zurück.
	 * @param fileName	Dateiname (ohne Pfad) der gesuchten Datei
	 * @return	{@link Statistics}-Objekt mit den Daten der angegebenen Datei oder <code>null</code> im Fehlerfall.
	 */
	public Statistics getStatisticFromFile(String fileName) {
		File file=getFileByName(fileName);
		if (file==null) return null;

		Statistics statistic=new Statistics(null,null,0,0);
		if (statistic.loadFromFile(file)!=null) return null;

		return statistic;
	}
}