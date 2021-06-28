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

import java.awt.Component;
import java.io.File;
import java.util.ArrayList;
import java.util.List;

import javax.swing.JFileChooser;
import javax.swing.filechooser.FileFilter;
import javax.swing.filechooser.FileNameExtensionFilter;

import language.Language;
import mathtools.Table;
import mathtools.TimeTools;
import mathtools.distribution.swing.CommonVariables;

/**
 * Diese abstrakte Basisklasse bietet Funktion an, um Callcenter-Modell-Daten
 * aus einer Tabelle zu laden.
 * @author Alexander Herzog
 */
public abstract class TableLoader {
	/**
	 * Zu verarbeitende Tabelle
	 */
	private final Table table;

	/**
	 * Zuletzt aufgetretenen Fehler
	 * @see #getLastError()
	 * @see #setLastError(String)
	 */
	private String lastError=null;

	/**
	 * Namen der zu verarbeitenden Spalten
	 * @see #setColNamesToProcess(String[])
	 */
	private String[] colNamesToProcess;

	/**
	 * Konstruktor der Klasse
	 * @param file	Dateiname der Datei aus der die Daten geladen werden sollen (darf nicht <code>null</code> sein)
	 */
	protected TableLoader(final File file) {
		if (!file.exists()) {lastError=String.format(Language.tr("Loader.ProcessError.FileDoesNotExist"),file.toString()); table=null; return;}

		Table t=new Table();
		t.load(file);
		if (t.getSize(0)<2) {
			lastError=String.format(Language.tr("Loader.ProcessError.Load"),file.toString());
			table=null;
		} else {
			table=t;
		}
	}

	/**
	 * Liefert den zuletzt aufgetretenen Fehler.
	 * @return	Zuletzt aufgetretener Fehler oder <code>null</code>, wenn kein Fehler aufgetreten ist
	 */
	public final String getLastError() {
		return lastError;
	}

	/**
	 * Stellt ein, was als zuletzt aufgetretener Fehler ausgegeben werden soll.
	 * @param error	Informationstext zum letzten Fehler
	 * @see #getLastError()
	 */
	protected final void setLastError(final String error) {
		lastError=error;
	}

	/**
	 * Liefert eine Reihe von Spalten aus {@link #table}.
	 * @param colNames	Namen der Spalten
	 * @return	Teiltabelle die nur noch die angegebenen Spalen enthält
	 */
	private final String[][] getCols(final String[] colNames) {
		String[][] cols=new String[colNames.length][];
		Table colTable=table.transpose();
		for (int i=0;i<colNames.length;i++) {
			String name=colNames[i];
			int nr=-1;
			for (int j=0;j<colTable.getSize(0);j++) if (colTable.getValue(j,0).equalsIgnoreCase(name)) {nr=j; break;}
			if (nr<0) {lastError=String.format(Language.tr("Loader.ProcessError.NoColummnWithCaption"),name); return null;}
			List<String> col=new ArrayList<>(colTable.getLine(nr));
			col.remove(0);
			cols[i]=col.toArray(new String[0]);
		}
		return cols;
	}

	/**
	 * Stellt die Namen der Spalten ein, die die zu verarbeitenden Daten enthalten.<br>
	 * Die Namen sind dabei Zellwerte innerhalb der Spalte.
	 * @param colNamesToProcess	Namen der zu verarbeitenden Spalten
	 */
	protected final void setColNamesToProcess(final String[] colNamesToProcess) {
		this.colNamesToProcess=colNamesToProcess;
		if (colNamesToProcess.length==0) lastError=Language.tr("Loader.ProcessError.NoColumnsSelected");
	}

	/**
	 * Führt die Verarbeitung einer Zeile durch.
	 * @param row	Zeile
	 * @return	Gibt <code>true</code> zurück, wenn die Verarbeitung erfolgreich durchgeführt werden konnte.
	 * @see #process()
	 */
	protected abstract boolean processRow(String[] row);

	/**
	 * Führt die Verarbeitung durch.
	 * @return	Gibt <code>true</code> zurück, wenn die Verarbeitung erfolgreich durchgeführt werden konnte.
	 */
	public final boolean process() {
		String[][] cols=getCols(colNamesToProcess);
		if (getLastError()!=null) return false;
		for (int i=0;i<cols[0].length;i++) {
			String[] row=new String[cols.length];
			for (int j=0;j<row.length;j++) row[j]=cols[j][i];
			if (!processRow(row)) {
				lastError+=" ("+String.format(Language.tr("Loader.ProcessError.WhileProcessingLineInfo"),i+2)+")";
				return false;
			}
		}
		return true;
	}

	/**
	 * Trägt die Ergebnisse der Verarbeitung in eine Tabelle ein.
	 * @param results	Tabelle in die die Ergebnisse eingetragen werden sollen
	 * @return	Gibt <code>true</code> zurück, wenn die Ergebnisse erfolgreich in die Tabelle eingetragen werden konnten
	 */
	protected abstract boolean getResults(Table results);

	/**
	 * Gibt zusätzliche Informationen zur Verarbeitung in einer Datei aus.
	 * @param file	Datei in die die Verarbeitungsergebnisse ausgegeben werden sollen
	 * @return	Gibt <code>true</code> zurück, wenn die Ergebnisse erfolgreich gespeichert werden konnten
	 * @see #getResults(Table)
	 */
	public final boolean saveResults(final File file) {
		Table results=new Table();
		boolean b=getResults(results);
		if (!b) {
			lastError=Language.tr("Loader.Error.NoResultsToBeSaved");
			return false;
		}
		b=results.save(file);
		if (!b) lastError=String.format(Language.tr("Loader.ProcessError.Save"),file.toString());
		return b;
	}

	/**
	 * Rechnet eine Zeitangabe in ein 0-basiertes Intervall um.
	 * @param time	Umzurechnende Zeitangabe
	 * @return	Zugehöriger 0-basierte Intervallnummer
	 */
	protected final int getIntervalFromTime(final String time) {
		Integer i=TimeTools.getTime(time);
		if (i==null) {lastError=String.format(Language.tr("Loader.ProcessError.InvalidTimeValue"),time); return 0;}
		return i/1800;
	}

	/**
	 * Zeigt einen Dialog zum Laden einer Textdatei an
	 * @param parent	Übergeordnetes Element
	 * @return	Liefert im Erfolgsfall den Dateinamen oder <code>null</code>, wenn der Dialog abgebrochen wurde
	 */
	public static final File getTextFileToLoad(final Component parent) {
		return getTextFileToLoad(parent,null);
	}

	/**
	 * Zeigt einen Dialog zum Laden einer Textdatei an
	 * @param parent	Übergeordnetes Element
	 * @param initialDirectory	Initial anzuzeigendes Verzeichnis (kann <code>null</code> sein, dann wird die Programmvorgabe verwendet)
	 * @return	Liefert im Erfolgsfall den Dateinamen oder <code>null</code>, wenn der Dialog abgebrochen wurde
	 */
	public static final File getTextFileToLoad(final Component parent, final File initialDirectory) {
		JFileChooser fc;
		if (initialDirectory!=null) fc=new JFileChooser(initialDirectory.toString()); else {
			fc=new JFileChooser();
			CommonVariables.initialDirectoryToJFileChooser(fc);
		}
		fc.setDialogTitle(Language.tr("Loader.LoadData"));
		FileFilter txt=new FileNameExtensionFilter(Language.tr("FileType.Text")+" (*.txt)","txt");
		fc.addChoosableFileFilter(txt);
		fc.setFileFilter(txt);
		if (fc.showOpenDialog(parent)!=JFileChooser.APPROVE_OPTION) return null;
		CommonVariables.initialDirectoryFromJFileChooser(fc);
		File file=fc.getSelectedFile();
		if (file.getName().indexOf('.')<0 && fc.getFileFilter()==txt) file=new File(file.getAbsoluteFile()+".txt");
		return file;
	}

	/**
	 * Zeigt einen Dialog zur Auswahl eines Namens für eine zu erstellende Callcenter-Modell-Datei an.
	 * @param parent	Übergeordnete Komponente
	 * @param initialDirectory	Am Anfang anzuzeigendes Verzeichnis
	 * @return	Gewählte Modelldatei oder <code>null</code>, wenn der Dialog abgebrochen wurde
	 */
	public static final File getModelFileToCreate(final Component parent, final File initialDirectory) {
		JFileChooser fc;
		if (initialDirectory!=null) fc=new JFileChooser(initialDirectory.toString()); else {
			fc=new JFileChooser();
			CommonVariables.initialDirectoryToJFileChooser(fc);
		}
		fc.setDialogTitle(Language.tr("Loader.SaveData"));
		FileFilter txt=new FileNameExtensionFilter(Language.tr("FileType.xml")+" (*.xml)","xml");
		fc.addChoosableFileFilter(txt);
		fc.setFileFilter(txt);
		fc.setAcceptAllFileFilterUsed(false);
		if (fc.showSaveDialog(parent)!=JFileChooser.APPROVE_OPTION) return null;
		CommonVariables.initialDirectoryFromJFileChooser(fc);
		File file=fc.getSelectedFile();
		if (file.getName().indexOf('.')<0 && fc.getFileFilter()==txt) file=new File(file.getAbsoluteFile()+".xml");
		return file;
	}

	/**
	 * Zeigt einen Dialog zur Auswahl eines zu ladenden Callcenter-Modells an.
	 * @param parent	Übergeordnete Komponente
	 * @param initialDirectory	Am Anfang anzuzeigendes Verzeichnis
	 * @return	Gewählte Modelldatei oder <code>null</code>, wenn der Dialog abgebrochen wurde
	 */
	public static final File getModelFileToLoad(final Component parent, final File initialDirectory) {
		JFileChooser fc;
		if (initialDirectory!=null) fc=new JFileChooser(initialDirectory.toString()); else {
			fc=new JFileChooser();
			CommonVariables.initialDirectoryToJFileChooser(fc);
		}
		fc.setDialogTitle(Language.tr("Loader.LoadData"));
		FileFilter txt=new FileNameExtensionFilter(Language.tr("FileType.xml")+" (*.xml)","xml");
		fc.addChoosableFileFilter(txt);
		fc.setFileFilter(txt);
		fc.setAcceptAllFileFilterUsed(false);
		if (fc.showSaveDialog(parent)!=JFileChooser.APPROVE_OPTION) return null;
		CommonVariables.initialDirectoryFromJFileChooser(fc);
		File file=fc.getSelectedFile();
		if (file.getName().indexOf('.')<0 && fc.getFileFilter()==txt) file=new File(file.getAbsoluteFile()+".xml");
		return file;
	}
}
