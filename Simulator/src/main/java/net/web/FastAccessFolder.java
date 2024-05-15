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

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;

import org.w3c.dom.Document;

import ui.statistic.core.filter.DataFilter;

/**
 * Kapselt Daten zu einem Verzeichnis mit Schnellzugriffs-Skripten.
 * @author Alexander Herzog
 * @see HandlerViewerSystem
 */
public class FastAccessFolder {
	/** Reales Verzeichnis */
	private final File folder;

	/**
	 * Konstruktor der Klasse
	 * @param folder	Reales Verzeichnis
	 */
	public FastAccessFolder(final String folder) {
		this.folder=(folder!=null && !folder.isBlank())?new File(folder):null;
	}

	/**
	 * Liest eine Datei in eine Zeichenkette ein
	 * @param file	Zu ladende Datei
	 * @return	Zeichenkette oder <code>null</code>, wenn das Laden fehlgeschlagen ist
	 */
	private String readFile(File file) {
		try (BufferedReader br=new BufferedReader(new InputStreamReader(new FileInputStream(file),StandardCharsets.UTF_8))) {
			StringBuilder sb=new StringBuilder();
			String line=br.readLine();
			while (line!=null) {
				sb.append(line);
				sb.append(System.lineSeparator());
				line=br.readLine();
			}
			return sb.toString();
		} catch (Exception e) {return null;}
	}

	/**
	 * Liefert Skriptnamen und Skriptdateien
	 * @return	2-elementiges Array aus: Skriptdateien und Skripttiteln
	 */
	public String[][] getScriptsList() {
		List<String> scriptFiles=new ArrayList<>();
		List<String> scriptTitles=new ArrayList<>();

		if (folder!=null && folder.isDirectory()) {
			File[] list=folder.listFiles();
			if (list!=null) for (File file : list) {
				String content=readFile(file);
				if (content==null) continue;
				String name=DataFilter.getTitleFromCommand(content);
				if (name==null || name.isBlank()) name=file.getName();
				scriptFiles.add(file.getName());
				scriptTitles.add(name);
			}
		}

		return new String[][]{scriptFiles.toArray(String[]::new),scriptTitles.toArray(String[]::new)};
	}

	/**
	 * Führt ein Skript aus
	 * @param xmlDoc	xml-Statistik-Dokument auf das das Skript angewandt werden soll
	 * @param filterFile	Skript
	 * @return	Ergebnisse
	 */
	public String runScript(Document xmlDoc, String filterFile) {
		if (folder==null || !folder.isDirectory()) return null;
		String commands=readFile(new File(folder,filterFile));
		if (commands==null) return null;

		DataFilter filter=new DataFilter(xmlDoc);
		filter.run(commands,false);
		return filter.getResults();
	}
}
