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
package ui.specialpanels;

import java.awt.Component;
import java.awt.Toolkit;
import java.awt.datatransfer.StringSelection;
import java.io.File;

import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.table.AbstractTableModel;
import javax.swing.table.TableModel;

import language.Language;
import mathtools.Table;
import ui.model.CallcenterModel;

/**
 * Diese Klasse kapselt eine {@link JTable}, ein Objekt vom Typ {@link Table},
 * welches die Daten enthält, und ein {@link TableModel}, welches dies Daten aus
 * dem {@link Table}-Objekt in die {@link JTable} überträgt.
 * @author Alexander Herzog
 * @version 1.0
 */
public class VarianzAnalyseExportableTable {
	private final CallcenterModel model;
	private int boldAfterRow;
	private final JTable table;
	private final VarianzAnalyseExportableTableModel tableModel;
	private final Table tableData;

	/**
	 * Konstruktor der Klasse
	 * @param model	Callcenter-Modell welches die Basis für die Varianzanalyse darstellt
	 */
	public VarianzAnalyseExportableTable(final CallcenterModel model) {
		this(model,Integer.MAX_VALUE);
	}

	/**
	 * Konstruktor der Klasse
	 * @param model	Callcenter-Modell welches die Basis für die Varianzanalyse darstellt
	 * @param boldAfterRow	Alles nach dieser Anzahl an Zeilen fett darstellen
	 */
	public VarianzAnalyseExportableTable(final CallcenterModel model, final int boldAfterRow) {
		this.model=model;
		this.boldAfterRow=boldAfterRow;
		table=new JTable(tableModel=new VarianzAnalyseExportableTableModel());
		tableData=new Table();
	}

	/**
	 * Liefert das {@link JTable}-Objekt eingebettet in ein Scroll-Panel.
	 * @return	Tabellenobjekt zur Anzeige in einem Fenster
	 */
	public JScrollPane getTableInScrollPage() {
		return new JScrollPane(table);
	}

	/**
	 * Stellt alle Zeilen nach der angegebenen Anzahl an Zeilen fett (als Ergebnis- bzw. Summenzeilen) dar.
	 * @param boldAfterRow	Alles nach dieser Anzahl an Zeilen fett darstellen
	 */
	public void setBoldAfterRow(int boldAfterRow) {
		this.boldAfterRow=boldAfterRow;
		tableModel.fireTableDataChanged();
	}

	/**
	 * Liefert die Tabellendaten (als Basis für Diagramme usw.).
	 * @return	Tabellendaten
	 */
	public Table getTable() {
		return tableData;
	}

	/**
	 * Liefert die Tabellendaten (ergänzt mit weiteren Informationen für den Export).
	 * @return	Tabellendaten
	 */
	public Table getOutputTable() {
		Table output=tableData.clone();
		String[] line=new String[model.caller.size()+2];
		line[0]="";
		for (int i=0;i<model.caller.size();i++) line[i+1]=model.caller.get(i).name;
		line[line.length-1]=Language.tr("VarianceAnalysis.AllCaller");
		output.insertLine(line,0);
		return output;
	}

	/**
	 * Löscht alle Daten in der Tabelle.
	 */
	public void clear() {
		tableData.clear();
		tableModel.fireTableDataChanged();
	}

	/**
	 * Fügt eine Zeile zu der Tabelle hinzu.
	 * @param line	Neue Tabellenzeile
	 */
	public void addLine(String[] line) {
		if (line!=null) {
			tableData.addLine(line);
			tableModel.fireTableDataChanged();
		}
	}

	/**
	 * Kopiert die Tabellendaten in die Zwischenablage.
	 * @see #getOutputTable()
	 */
	public void copyTable() {
		Toolkit.getDefaultToolkit().getSystemClipboard().setContents(new StringSelection(getOutputTable().toString()),null);
	}

	/**
	 * Speichert die Tabellendaten in einer Datei.
	 * @param parent	Übergeordnetes Element für den Dialog.
	 * @see #getOutputTable()
	 */
	public void saveTable(Component parent) {
		File file=Table.showSaveDialog(parent,Language.tr("FileType.Save.Table")); if (file==null) return;
		getOutputTable().save(file);
	}

	private class VarianzAnalyseExportableTableModel extends AbstractTableModel {
		private static final long serialVersionUID = -3597759277796840217L;

		@Override
		public String getColumnName(int column) {
			if (column==0) return "";
			if (column==model.caller.size()+1) return Language.tr("VarianceAnalysis.AllCaller");
			return model.caller.get(column-1).name;
		}

		@Override
		public int getColumnCount() {
			return model.caller.size()+2;
		}
		@Override
		public int getRowCount() {
			return tableData.getSize(0);
		}
		@Override
		public Object getValueAt(int row, int col) {
			String s=tableData.getValue(row,col);
			if (row>=boldAfterRow && col>0) s="<html><body><b>"+s+"<b></body></html>";
			return s;
		}
	}
}