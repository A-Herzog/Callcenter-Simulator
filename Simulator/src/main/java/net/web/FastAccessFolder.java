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
	private final File folder;

	/**
	 * Konstruktor der Klasse
	 * @param folder	Reales Verzeichnis
	 */
	public FastAccessFolder(final String folder) {
		this.folder=(folder!=null && !folder.trim().isEmpty())?new File(folder):null;
	}

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
		List<String> scriptFiles=new ArrayList<String>();
		List<String> scriptTitles=new ArrayList<String>();

		if (folder!=null && folder.isDirectory()) {
			File[] list=folder.listFiles();
			if (list!=null) for (File file : list) {
				String content=readFile(file);
				if (content==null) continue;
				String name=DataFilter.getTitleFromCommand(content);
				if (name==null || name.trim().isEmpty()) name=file.getName();
				scriptFiles.add(file.getName());
				scriptTitles.add(name);
			}
		}

		return new String[][]{scriptFiles.toArray(new String[0]),scriptTitles.toArray(new String[0])};
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
