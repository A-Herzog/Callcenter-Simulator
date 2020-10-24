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
package ui.commandline;

import java.io.File;
import java.io.InputStream;
import java.io.PrintStream;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import language.Language;
import mathtools.NumberTools;
import mathtools.Table;
import mathtools.distribution.DataDistributionImpl;
import systemtools.commandline.AbstractCommand;
import ui.model.CallcenterModel;

/**
 * Abstrakte Basisklasse für Generator-Kommandozeilenbefehle.
 * Diese Klasse stellt zusätzliche geschützte Methoden bereit, die alle Generatorbefehle benötigen.
 * @author Alexander Herzog
 * @version 1.0
 * @see AbstractCommand
 * @see CommandLineSystem
 */
public abstract class AbstractGeneratorCommand extends AbstractSimulationCommand {
	/**
	 * Anzahl an zusätzlich erwarteten Kommandozeilenargumenten für den Befehl.
	 * Dieser Wert sollte bereits vom Konstruktor eingestellt werden. Die
	 * zusätzlichen Argumente werden von {@link #prepare(String[], InputStream, PrintStream)}
	 * in {@link #name} abgelegt.
	 */
	protected int nameArgumentCount=0;

	/**
	 * Zusätzliche Kommandozeilenargumente, siehe {@link #nameArgumentCount}.
	 */
	protected String[] name;

	/** Eingabe-Modelldatei */
	private File modelFile;
	/** Eingabe-Tabellendatei */
	private File tableFile;
	/** Ausgabe-Modelldatei */
	private File modelFileOut;

	/**
	 * Spalten aus denen die Daten geladen werden soll
	 */
	protected List<String> generatorColumn;

	/**
	 * Modell-interne Typen auf die sich die Daten beziehen sollen
	 */
	protected List<String> generatorType;

	/**
	 * Löst einen Bezeichner für eine Zeille (z.B. A1) in einzelne Werte für Zeile und Spalte auf
	 * @param startCell	Zellenname der aufgelöst werden soll
	 * @return	2-elementiges Array aus jeweils 0-basierter Nummer der Zeile und der Spalte
	 */
	protected final int[] getStartCell(String startCell) {
		if (startCell==null || startCell.isEmpty()) return null;
		startCell=startCell.toUpperCase();

		/* Zeilen- und Spaltennummer trennen */
		int p=-1;
		for (int i=0;i<startCell.length();i++) if (startCell.charAt(i)<'A' || startCell.charAt(i)>'Z') {p=i-1; break;}
		if (p<0) return null;
		String col=startCell.substring(0,p+1);
		String row=startCell.substring(p+1);

		/* Zeile ermitteln */
		Integer rowNr=NumberTools.getNotNegativeInteger(row);
		if (rowNr==null || rowNr==0) return null;

		/* Spalte ermitteln */
		int colNr=0;
		for (int i=0;i<col.length();i++) {
			colNr*=26;
			colNr+=(col.charAt(i)-'A')+1;
		}

		/* Ergebnisarray erstellen */
		int[] result=new int[2];
		result[0]=rowNr-1;
		result[1]=colNr-1;
		return result;
	}

	/**
	 * Überträgt Daten aus einer Tabellenspalte in eine Verteilung
	 * @param row	0-basierte Startzeile
	 * @param col	0-basierte Spalte
	 * @param table	Tabelle aus der die Daten geladen werden sollen
	 * @param data	Verteilungsobjekt in das die Daten übertragen werden sollen
	 * @return	Liefert im Erfolgsfall <code>null</code> sonst eine Fehlermeldung
	 */
	protected final String loadData(int row, int col, Table table, DataDistributionImpl data) {
		if (table.getSize(0)<=col) return String.format(Language.tr("CommandLine.Error.Table.TooFewTableColumns"),col+1,table.getSize(0));
		if (table.getSize(1)<=row+data.densityData.length-1) return String.format(Language.tr("CommandLine.Error.Table.TooFewTableRows"),row+1,row+1+(data.densityData.length-1),table.getSize(1));

		for (int i=0;i<data.densityData.length;i++) {
			Double d=NumberTools.getNotNegativeDouble(table.getValue(col,row+i));
			if (d==null) return String.format(Language.tr("CommandLine.Error.Table.InvalidCellContent"),row+i+1,col+1,table.getValue(col,row+i));
			data.densityData[i]=d;
		}

		return null;
	}

	/**
	 * Überträgt Daten aus einer Tabellenspalte in eine Verteilung
	 * @param row	0-basierte Startzeile
	 * @param col	0-basierte Spalte
	 * @param table	Tabelle aus der die Daten geladen werden sollen
	 * @param data	Verteilungsobjekt in das die Daten übertragen werden sollen
	 * @param maxValues	Maximalanzahl an zu ladenden Werten (auch wenn die Verteilung mehr Einträge vorsieht)
	 * @return	Liefert im Erfolgsfall <code>null</code> sonst eine Fehlermeldung
	 */
	protected final String loadData(int row, int col, Table table, DataDistributionImpl data, int maxValues) {
		if (table.getSize(0)<=col) return String.format(Language.tr("CommandLine.Error.Table.TooFewTableColumns"),col+1,table.getSize(0));

		Arrays.fill(data.densityData,0);
		for (int i=0;i<Math.min(data.densityData.length,maxValues);i++) {
			Double d=NumberTools.getNotNegativeDouble(table.getValue(col,row+i));
			if (d==null) return null;
			data.densityData[i]=d;
		}

		return null;
	}

	@Override
	public final String prepare(String[] additionalArguments, InputStream in, PrintStream out) {
		if (additionalArguments.length<5+nameArgumentCount || Math.abs(additionalArguments.length-nameArgumentCount)%2!=1) {
			String s="";
			s+=String.format(Language.tr("CommandLine.Error.WrongNumber.If"),getName())+" ";
			s+=String.format(Language.tr("CommandLine.Error.RuleThreeExamples"),5+nameArgumentCount,7+nameArgumentCount,9+nameArgumentCount)+" ";
			s+=String.format(Language.tr("CommandLine.Error.WrongNumber.ButNumber"),additionalArguments.length);
			return s;
		}

		name=new String[nameArgumentCount];
		System.arraycopy(additionalArguments,0,name,0,nameArgumentCount);
		modelFile=new File(additionalArguments[0+nameArgumentCount]);
		tableFile=new File(additionalArguments[1+nameArgumentCount]);
		modelFileOut=new File(additionalArguments[2+nameArgumentCount]);
		generatorColumn=new ArrayList<String>();
		generatorType=new ArrayList<String>();
		if (!modelFile.isFile()) return String.format(Language.tr("CommandLine.Error.File.InputDoesNotExist"),modelFile.toString());
		if (!isModelFile(modelFile)) return String.format(Language.tr("CommandLine.Error.File.InputNoValidCallCenterModel"),modelFile.toString());
		if (!tableFile.isFile()) return String.format(Language.tr("CommandLine.Error.File.TableDoesNotExist"),tableFile.toString());
		if (modelFileOut.isFile()) return String.format(Language.tr("CommandLine.Error.File.OutputAlreadyExist"),modelFileOut.toString());
		int nr=3+nameArgumentCount;
		while (nr<additionalArguments.length-1) {
			generatorColumn.add(additionalArguments[nr]);
			generatorType.add(additionalArguments[nr+1]);
			nr+=2;
		}
		return null;
	}

	/**
	 * Führt die eigentliche Verarbeitung durch.
	 * @param editModel	Callcenter-Modell in das die Daten geladen werden sollen
	 * @param table	Tabelle aus der die Daten geladen werden sollen
	 * @param out	Ausgabe
	 * @return	Gibt an, ob die Verarbeitung erfolgreich war (und das veränderte Callcenter-Modell gespeichert werden soll)
	 */
	protected abstract boolean runIntern(CallcenterModel editModel, Table table, PrintStream out);

	@Override
	public final void run(AbstractCommand[] allCommands, InputStream in, PrintStream out) {
		CallcenterModel editModel=new CallcenterModel();
		String s=editModel.loadFromFile(modelFile);
		if (s!=null) {out.println(Language.tr("Dialog.Title.Error").toUpperCase()+": "+Language.tr("CommandLine.Error.LoadingModel")+" "+s); return;}
		Table table=new Table(Table.IndexMode.COLS);
		if (!table.load(tableFile)) {out.println(Language.tr("Dialog.Title.Error").toUpperCase()+": "+String.format(Language.tr("CommandLine.Error.LoadingTable"),tableFile)); return;}
		if (!runIntern(editModel,table,out)) return;
		if (!editModel.saveToFile(modelFileOut)) {out.println(Language.tr("Dialog.Title.Error").toUpperCase()+": "+Language.tr("CommandLine.Error.SavingModel")); return;}
	}
}
