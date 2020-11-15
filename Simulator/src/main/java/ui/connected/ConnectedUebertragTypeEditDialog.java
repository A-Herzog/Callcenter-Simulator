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
package ui.connected;

import java.awt.Window;
import java.io.Serializable;

import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.table.AbstractTableModel;

import language.Language;
import mathtools.NumberTools;
import tools.JTableExt;
import ui.editor.BaseEditDialog;

/**
 * Dieser Dialog erlaubt die Kundentyp-abhängige Steuerung, von Kundentypänderungen
 * beim Übertrag von Warteabbrecher von einem Tag einer verbundenen Simulation in den
 * nächsten Simulationstag.
 * @author Alexander Herzog
 * @see ConnectedUebertragEditDialog
 * @see ConnectedJTableModel
 */
public final class ConnectedUebertragTypeEditDialog extends BaseEditDialog {
	/**
	 * Serialisierungs-ID der Klasse
	 * @see Serializable
	 */
	private static final long serialVersionUID = 1732595764749627708L;

	/** Liste mit Namen der verfügbaren Kundentypen */
	private final String[] caller;
	/** Raten mit denen Kunden ihren Typ zu dem angegebenen Kundentyp ändern (Originalobjekt, wird in {@link #storeData()} aktualisiert) */
	private final ConnectedModelUebertrag callerRetryOrig;
	/** Temporäre Arbeitskopie der Raten mit denen Kunden ihren Typ zu dem angegebenen Kundentyp ändern */
	private final ConnectedModelUebertrag callerRetry;

	/** Datenmodell für die Tabelle zur Steuerung der Kundentypänderungen */
	private UebertragChangeTableModel changeData;

	/**
	 * Konstruktor der Klasse
	 * @param owner	Übergeordnetes Fenster
	 * @param typeName	Ausgangskundentypname
	 * @param caller	Liste mit Namen der verfügbaren Kundentypen
	 * @param callerRetry	Raten mit denen Kunden ihren Typ zu dem angegebenen Kundentyp ändern
	 * @param helpCallback	Hilfe-Callback
	 */
	public ConnectedUebertragTypeEditDialog(final Window owner, final String typeName, final String[] caller, final ConnectedModelUebertrag callerRetry, final Runnable helpCallback) {
		super(owner,String.format(Language.tr("Connected.ClientTypeChangeTitle"),typeName),false,helpCallback);
		this.caller=caller;
		callerRetryOrig=callerRetry;
		this.callerRetry=callerRetry.cloneUebertrag();

		createSimpleGUI(450,400,null,null);
		pack();
	}

	@Override
	protected void createSimpleContent(JPanel content) {
		changeData=new UebertragChangeTableModel();
		JScrollPane sc;
		content.add(sc=new JScrollPane(new JTableExt(changeData)));

		sc.setAlignmentX(0);
	}


	/* (non-Javadoc)
	 * @see complexcallcenter.editor.BaseEditDialog#checkData()
	 */
	@Override
	protected boolean checkData() {
		return true;
	}

	/* (non-Javadoc)
	 * @see complexcallcenter.editor.BaseEditDialog#storeData()
	 */
	@Override
	protected void storeData() {
		callerRetryOrig.changeRates=callerRetry.changeRates;
	}

	/**
	 * Datenmodell für die Tabelle zur Steuerung der Kundentypänderungen
	 */
	private final class UebertragChangeTableModel extends AbstractTableModel {
		/**
		 * Serialisierungs-ID der Klasse
		 * @see Serializable
		 */
		private static final long serialVersionUID = 3426277156013494341L;
		@Override public boolean isCellEditable(int rowIndex, int columnIndex) {return columnIndex>0;}
		@Override public String getColumnName(int column) {return (column==0)?Language.tr("Connected.ClientType"):Language.tr("Connected.Rate");}
		@Override public int getRowCount() {return caller.length;}
		@Override public int getColumnCount() {return 2;}
		@Override
		public Object getValueAt(int rowIndex, int columnIndex) {
			if (columnIndex==0)  return caller[rowIndex];
			Double d=callerRetry.changeRates.get(caller[rowIndex]);
			if (d==null) d=0.0;
			return NumberTools.formatNumberMax(d);
		}
		@Override
		public void setValueAt(Object aValue, int rowIndex, int columnIndex) {
			if (columnIndex!=1) return;
			Double D;
			if (aValue==null) D=0.0; else D=NumberTools.getExtProbability(aValue.toString());
			if (D!=null) callerRetry.changeRates.put(caller[rowIndex],D);
		}
	}
}
