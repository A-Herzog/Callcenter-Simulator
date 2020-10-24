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
package ui.importer;

import java.awt.Container;
import java.io.File;
import java.util.ArrayList;
import java.util.List;

import org.w3c.dom.Element;
import org.w3c.dom.NodeList;

import language.Language;
import mathtools.MultiTable;
import systemtools.MsgBox;
import ui.model.CallcenterModel;
import xml.XMLTools;

/**
 * Diese Klasse enthält alle Schabloneneinträge um Daten aus einer Tabelle zu laden und in ein Callcenter-Modell zu schreiben.
 * @author Alexander Herzog
 * @version 1.0
 * @see ImporterRecord
 */
public final class ImporterData implements Cloneable {
	/**
	 * Fehlermeldung, wenn {@link #process(CallcenterModel, MultiTable)} fehlgeschlagen ist
	 * @see #getProcessError()
	 * @see #process(CallcenterModel, MultiTable)
	 */
	private String processError;

	/**
	 * Liste der Schabloneneinträge
	 */
	public final List<ImporterRecord> records;

	/**
	 * Mögliche Namen des Basiselements von Schablonen-XML-Dateien (zur Erkennung von Dateien dieses Typs.)
	 */
	public static final String[] XMLBaseElement=Language.trAll("XML.ImportTemplate");

	/**
	 * Konstruktor der Klasse <code>ImporterData</code>
	 * @param records	Liste mit Schabloneneinträgen, die in diese Klasse kopiert werden sollen.
	 */
	public ImporterData(List<ImporterRecord> records) {
		this.records=new ArrayList<ImporterRecord>();
		if (records!=null) for (int i=0;i<records.size();i++) this.records.add(records.get(i).clone());
	}

	/**
	 * Konstruktor der Klasse <code>ImporterData</code>
	 * (Erstellt eine leere Schablonenliste.)
	 */
	public ImporterData() {
		this(null);
	}

	/**
	 * Vergleicht zwei Objekte vom Typ <code>ImporterData</code>
	 * @param data Mit dem aktuellen Objekt zu vergleichendes zweites <code>ImporterData</code>-Objekt
	 * @return	Gibt <code>true</code> zurück, wenn die beiden Objekte identisch sind.
	 */
	public boolean equalsImporterData(ImporterData data) {
		if (data.records.size()!=records.size()) return false;
		for (int i=0;i<records.size();i++) if (!records.get(i).equalsImporterRecord(data.records.get(i))) return false;
		return true;
	}

	/**
	 * Kopiert das aktuelle <code>ImporterData</code>-Objekt.
	 * @return	Gibt eine Kopie des aktuellen Objektes zurück
	 */
	@Override
	public ImporterData clone() {
		return new ImporterData(records);
	}

	/**
	 * Löscht alle Einträge aus der Schablonenliste
	 * @see #records
	 */
	public void clear() {
		records.clear();
	}

	/**
	 * Lädt die Schablonenliste aus einem xml-Root-Element.
	 * @param root	xml-Root-Element
	 * @return	Gibt <code>null</code> zurück, wenn das Laden erfolgreich war, sonst eine Fehlermeldung.
	 */
	private boolean loadFromXML(Element root) {
		if (!Language.trAll("XML.ImportTemplate",root.getNodeName())) return false;

		NodeList l=root.getChildNodes();
		for (int i=0; i<l.getLength();i++) {
			if (!(l.item(i) instanceof Element)) continue;
			Element e=(Element)l.item(i);
			if (!Language.trAll("XML.ImportTemplate.Record",e.getNodeName())) continue;

			ImporterRecord record=new ImporterRecord();
			if (!record.loadFromXMLElement(e)) return false;
			records.add(record);
		}
		return true;
	}

	/**
	 * Lädt die Schablonenliste aus einer Datei.
	 * @param file Dateiname, aus der die Liste geladen werden soll
	 * @return	Gibt <code>null</code> zurück, wenn das Laden erfolgreich war, sonst eine Fehlermeldung.
	 */
	public String loadFromFile(File file) {
		if (file==null || !file.exists()) return Language.tr("Importer.LoadTemplate.ErrorNoFile");
		if (!file.exists()) return String.format(Language.tr("Importer.LoadTemplate.ErrorExistsNot.Info"),file.toString());

		XMLTools xml=new XMLTools(file);
		Element root=xml.load();
		if (root==null) return xml.getError();
		clear();
		if (loadFromXML(root)) return null;
		return String.format(Language.tr("XML.ErrorProcessingFile"),file.toString());
	}

	/**
	 * Lädt die Schablonenliste aus einer Datei.
	 * @param parent	Übergeordnete Komponente (für mögliche Fehler-Messageboxes)
	 * @return	Gibt <code>true</code> zurück, wenn das Laden erfolgreich war. Im Fehlerfall wird außerdem eine Fehler-Messagebox angezeigt.
	 */
	public boolean loadFromFile(Container parent) {
		File file=XMLTools.showLoadDialog(parent,Language.tr("Importer.LoadTemplate"));
		if (file==null) return false;

		if (!file.exists()) {
			MsgBox.error(parent,Language.tr("Importer.LoadTemplate.ErrorExistsNot.Title"),String.format(Language.tr("Importer.LoadTemplate.ErrorExistsNot.Info"),file.toString()));
			return false;
		}

		String error=loadFromFile(file);
		if (error!=null) {
			MsgBox.error(parent,Language.tr("Importer.LoadTemplate.ErrorLoading.Title"),error);
			return false;
		}

		return true;
	}

	/**
	 * Speichert die aktuelle Schablonenliste.
	 * @param root	xml-Root-Element
	 */
	private void addDataToXML(final Element root) {
		for (int i=0;i<records.size();i++) records.get(i).saveToXMLElement(root);
	}

	/**
	 * Speichert die aktuelle Schablonenliste.
	 * @param file	Dateiname, in der die Liste gespeichert werden soll
	 * @return	Gibt <code>true</code> zurück, wenn das Speichern erfolgreich war.
	 */
	public boolean saveToFile(File file) {
		XMLTools xml=new XMLTools(file);
		Element root=xml.generateRoot(Language.trPrimary("XML.ImportTemplate"));
		if (root==null) return false;
		addDataToXML(root);
		return xml.save(root);	}

	/**
	 * Speichert die aktuelle Schablonenliste
	 * @param parent	Übergeordnete Komponente (für mögliche Fehler-Messageboxes)
	 * @return	Gibt <code>true</code> zurück, wenn das Speichern erfolgreich war. Im Fehlerfall wird außerdem eine Fehler-Messagebox angezeigt.
	 */
	public boolean saveToFile(Container parent) {
		File file=XMLTools.showSaveDialog(parent,Language.tr("Importer.SaveTemplate"));
		if (file==null) return false;

		if (file.exists()) {
			if (!MsgBox.confirmOverwrite(parent,file)) return false;
		}

		if (!saveToFile(file)) {
			MsgBox.error(parent,Language.tr("Importer.SaveTemplate.ErrorSaving.Title"),String.format(Language.tr("Importer.SaveTemplate.ErrorSaving.Info"),file.toString()));
			return false;
		}

		return true;
	}

	/**
	 * Verschiebt einen Schabloneneintrag eine Eintrag nach oben
	 * @param index	Zu verschiebender Eintrag
	 */
	public void moveUp(int index) {
		if (index<=0 || index>=records.size()) return;
		ImporterRecord temp=records.get(index);
		records.set(index,records.get(index-1));
		records.set(index-1,temp);
	}

	/**
	 * Verschiebt einen Schabloneneintrag eine Eintrag nach unten
	 * @param index	Zu verschiebender Eintrag
	 */
	public void moveDown(int index) {
		if (index>=records.size()-1) return;
		ImporterRecord temp=records.get(index);
		records.set(index,records.get(index+1));
		records.set(index+1,temp);
	}

	/**
	 * Wendet die aktuelle Schablone auf das Callcenter-Modell an.
	 * @param model	Callcenter-Modell, auf das die Schabloneneinträge angewandt werden sollen.
	 * @param table	Tabelle aus der die Daten geladen werden sollen.
	 * @return	Gibt im Erfolgsfall ein neues Callcenter-Modell zurück, sonst <code>null</code>. Eine Fehlerbeschreibung kann im Fehlerfall über <code>getProcessError</code> ausgelesen werden.
	 * @see #getProcessError()
	 */
	public CallcenterModel process(CallcenterModel model, MultiTable table) {
		processError=null;
		CallcenterModel newModel=model.clone();
		for (int i=0;i<records.size();i++) {
			String s=records.get(i).process(newModel,table);
			if (s!=null) {processError=s; return null;}
		}
		return newModel;
	}

	/**
	 * Liefert eine Fehlermeldung zurück, wenn {@link #process(CallcenterModel, MultiTable)} fehlgeschlagen ist.
	 * @return	Fehlermeldung
	 * @see #process(CallcenterModel, MultiTable)
	 */
	public String getProcessError() {
		return processError;
	}
}