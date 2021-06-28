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

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;

import language.Language;
import mathtools.MultiTable;
import mathtools.Table;
import mathtools.distribution.DataDistributionImpl;

/**
 * Lädt und ergänzt ein Callcentermodell um Tabellendaten.
 * @author Alexander Herzog
 * @see XMLPreprocessing
 */
public class XMLPreprocessor {
	/** Als Basis zu verwendende Modell-xml-Datei */
	private final File xmlFile;
	/** Zusätzliche, in das Modell aufzunehmende Tabellendaten */
	private final File tableFile;
	/** Datenzeilen aus {@link #xmlFile} */
	private final List<String> xmlLines;
	/** Zusätzliche, in das Modell aufzunehmende Tabellendaten */
	private final MultiTable multiTable;
	/** Verarbeitete Datenzeilen */
	private final List<String> processedLines;

	/**
	 * Konstruktor der Klasse
	 * @param xmlFile	Als Basis zu verwendende Modell-xml-Datei
	 * @param tableFile	Zusätzliche, in das Modell aufzunehmende Tabellendaten
	 */
	public XMLPreprocessor(final File xmlFile, final File tableFile) {
		this.xmlFile=xmlFile;
		this.tableFile=tableFile;
		xmlLines=new ArrayList<>();
		processedLines=new ArrayList<>();
		multiTable=new MultiTable();
	}

	/**
	 * Prüft die angegebenen Dateien auf Existenz und versucht das Basismodell als Datei zu laden.
	 * @return	Liefert im Erfolgsfall <code>null</code>, sonst eine Fehlermeldung
	 */
	public String prepare() {
		if (!xmlFile.exists()) return String.format(Language.tr("Loader.ProcessError.FileDoesNotExist"),xmlFile.toString());
		if (!multiTable.load(tableFile) || multiTable.size()==0) return String.format(Language.tr("Loader.ProcessError.Load"),tableFile.toString());

		try {
			try (BufferedReader br=new BufferedReader(new InputStreamReader(new FileInputStream(xmlFile),StandardCharsets.UTF_8))) {
				String s;
				while ((s=br.readLine())!=null) xmlLines.add(s);
			}
		} catch (IOException e) {return String.format(Language.tr("Loader.ProcessError.Load"),xmlFile.toString());}

		return null;
	}

	/**
	 * Verarbeitet einen Befehl.
	 * @param command	Zeile
	 * @return	Liefert ein Array aus zwei Einträgen: Neue Zeile und Fehlermeldung (einer der Einträge ist immer <code>null</code>)
	 * @see #processLine(String)
	 */
	private String[] processCommand(final String command) {
		String[] parts=command.split("!");
		String tableName="";
		String range="";
		if (parts.length==1) {tableName=""; range=parts[0];}
		if (parts.length==2) {tableName=parts[0]; range=parts[1];}
		if (parts.length>2) return new String[]{null,String.format(Language.tr("Loader.ProcessError.XMLProcessing.InvalidCommand"),command)};

		/* Tabelle selektieren */
		if (tableName.length()>2 && tableName.charAt(0)=='\'' && tableName.charAt(tableName.length()-1)=='\'') tableName=tableName.substring(1,tableName.length()-1);
		if (tableName.trim().isEmpty()) tableName="";
		Table table=multiTable.get(tableName);
		if (table==null) return new String[]{null,String.format(Language.tr("Loader.ProcessError.XMLProcessing.InvalidTableName"),tableName)};

		/* Bereich interpretieren */
		String[] cells=range.split(":");
		if (cells.length>2) return new String[]{null,String.format(Language.tr("Loader.ProcessError.XMLProcessing.InvalidCommand"),command)};
		double[] numbers;
		if (cells.length==1) {
			/* Einzelne Zelle */
			numbers=table.getNumberArea(cells[0],cells[0]);
		} else {
			/* Bereich */
			numbers=table.getNumberArea(cells[0],cells[1]);
		}
		if (numbers==null) return new String[]{null,table.getAreaError()};

		DataDistributionImpl data=new DataDistributionImpl(86400,numbers);

		return new String[]{data.storeToLocalString(),null};
	}

	/**
	 * Verarbeitet eine Zeile
	 * @param line	Zu verarbeitende Zeile
	 * @return	Liefert ein Array aus zwei Einträgen: Neue Zeile und Fehlermeldung (einer der Einträge ist immer <code>null</code>)
	 * @see #process()
	 */
	private String[] processLine(String line) {
		StringBuilder processed=new StringBuilder();

		int index=line.indexOf("$(");
		while (index>=0) {
			processed.append(line.substring(0,index));
			line=line.substring(index+2);
			index=line.indexOf(")");
			if (index>=0) {
				String command=line.substring(0,index).trim();
				line=line.substring(index+1);
				String[] result=processCommand(command);
				if (result[1]!=null) return result;
				processed.append(result[0]);
			}
			index=line.indexOf("$(");
		}
		processed.append(line);

		return new String[]{processed.toString(),null};
	}

	/**
	 * Führt die eigentliche Verarbeitung durch.
	 * @return	Liefert im Erfolgsfall <code>null</code>, sonst eine Fehlermeldung
	 */
	public String process() {
		for (int i=0;i<xmlLines.size();i++) {
			String[] result=processLine(xmlLines.get(i));
			if (result[1]!=null) return String.format(Language.tr("Loader.ProcessError.XMLProcessing.ErrorInLine"),i+1,result[1]);
			processedLines.add(result[0]);
		}
		return null;
	}

	/**
	 * Liefert das neue Callcenter-Modell.
	 * @return	Neues Callcenter-Modell als xml-Zeichenkette
	 */
	public String getResult() {
		StringBuilder sb=new StringBuilder();
		for (String line: processedLines) {sb.append(line); sb.append("\n");}
		return sb.toString();
	}
}