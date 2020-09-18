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

import java.util.ArrayList;
import java.util.List;

import org.w3c.dom.Document;
import org.w3c.dom.Element;

import language.Language;
import mathtools.MultiTable;
import mathtools.Table;
import ui.importer.processors.ImporterProcessorAgentAddition;
import ui.importer.processors.ImporterProcessorAgentEfficiency;
import ui.importer.processors.ImporterProcessorAgentWorkingTimes;
import ui.importer.processors.ImporterProcessorCallerDistribution;
import ui.importer.processors.ImporterProcessorCallerSingleValue;
import ui.importer.processors.ImporterProcessorSkillTimes;
import ui.model.CallcenterModel;

/**
 * Diese Klasse enthält einen Schabloneneintrag um Daten aus einer Tabelle zu laden und in ein Callcenter-Modell zu schreiben.
 * @author Alexander Herzog
 * @version 1.0
 * @see ImporterData
 */
public final class ImporterRecord implements Cloneable {
	/**
	 * Gibt an, welche <code>ImporterProcessor</code>-Klasse zur Verarbeitung verwendet werden soll.
	 * @see #getImporterProcessors()
	 * @see #getProcessor()
	 */
	public int type=0;

	/**
	 * Liefert zusätzliche Informationen zum Importieren (z.B. Name des Kundentyps)
	 */
	public String parameter="";

	/**
	 * Gibt die Startzelle in der Tabelle an.
	 */
	public String cellStart="";

	/**
	 * Gibt die Endzelle in der Tabelle an.
	 */
	public String cellEnd="";

	private static List<ImporterProcessor> importerProcessors=null;

	/**
	 * Liefert eine Liste aller <code>ImporterProcessor</code>-Klassen, zum Verarbeiten
	 * der Importschablonen.
	 * (Hier müssen die <code>ImporterProcessor</code>-Klassen registriert werden.
	 * <code>getImporterProcessors</code> liefert dann eine gecachede Version davon.)
	 * @return	Liste mit <code>ImporterProcessor</code>-Objekten.
	 * @see #getImporterProcessors()
	 */
	private static List<ImporterProcessor> getImporterProcessorsInt() {
		List<ImporterProcessor> list=new ArrayList<ImporterProcessor>();
		list.add(new ImporterProcessorCallerSingleValue(ImporterProcessorCallerSingleValue.CallerValue.CALLER_VALUE_COUNT));
		list.add(new ImporterProcessorCallerSingleValue(ImporterProcessorCallerSingleValue.CallerValue.CALLER_VALUE_STD));
		list.add(new ImporterProcessorCallerDistribution(ImporterProcessorCallerDistribution.CallerDist.CALLER_DIST_FRESH_CALLS));
		list.add(new ImporterProcessorCallerDistribution(ImporterProcessorCallerDistribution.CallerDist.CALLER_DIST_FRESH_CALLS_COUNT));
		list.add(new ImporterProcessorCallerDistribution(ImporterProcessorCallerDistribution.CallerDist.CALLER_DIST_FRESH_CALLS_AND_COUNT));
		list.add(new ImporterProcessorCallerDistribution(ImporterProcessorCallerDistribution.CallerDist.CALLER_DIST_WAITING_TIME_TOLERANCE_SHORT));
		list.add(new ImporterProcessorCallerDistribution(ImporterProcessorCallerDistribution.CallerDist.CALLER_DIST_WAITING_TIME_TOLERANCE_LONG));
		list.add(new ImporterProcessorCallerSingleValue(ImporterProcessorCallerSingleValue.CallerValue.CALLER_VALUE_RETRY_BLOCKED_FIRST));
		list.add(new ImporterProcessorCallerSingleValue(ImporterProcessorCallerSingleValue.CallerValue.CALLER_VALUE_RETRY_BLOCKED));
		list.add(new ImporterProcessorCallerSingleValue(ImporterProcessorCallerSingleValue.CallerValue.CALLER_VALUE_RETRY_CANCEL_FIRST));
		list.add(new ImporterProcessorCallerSingleValue(ImporterProcessorCallerSingleValue.CallerValue.CALLER_VALUE_RETRY_CANCEL));
		list.add(new ImporterProcessorCallerDistribution(ImporterProcessorCallerDistribution.CallerDist.CALLER_DIST_RETRY_TIMES));
		list.add(new ImporterProcessorCallerDistribution(ImporterProcessorCallerDistribution.CallerDist.CALLER_DIST_RECALL_TIMES));
		list.add(new ImporterProcessorCallerSingleValue(ImporterProcessorCallerSingleValue.CallerValue.CALLER_VALUE_CONTINUE));
		list.add(new ImporterProcessorAgentWorkingTimes());
		list.add(new ImporterProcessorAgentEfficiency());
		list.add(new ImporterProcessorAgentAddition());
		list.add(new ImporterProcessorSkillTimes(ImporterProcessorSkillTimes.SkillMode.SKILL_WORKING_TIME,-1));
		for (int i=0;i<48;i++) list.add(new ImporterProcessorSkillTimes(ImporterProcessorSkillTimes.SkillMode.SKILL_WORKING_TIME,i));
		list.add(new ImporterProcessorSkillTimes(ImporterProcessorSkillTimes.SkillMode.SKILL_POSTPROCESSING_TIME,-1));
		for (int i=0;i<48;i++) list.add(new ImporterProcessorSkillTimes(ImporterProcessorSkillTimes.SkillMode.SKILL_POSTPROCESSING_TIME,i));
		list.add(new ImporterProcessorSkillTimes(ImporterProcessorSkillTimes.SkillMode.SKILL_WORKING_TIME_ADDON,-1));
		for (int i=0;i<48;i++) list.add(new ImporterProcessorSkillTimes(ImporterProcessorSkillTimes.SkillMode.SKILL_WORKING_TIME_ADDON,i));
		return list;
	}

	/**
	 * Liefert eine Liste aller <code>ImporterProcessor</code>-Klassen, zum Verarbeiten
	 * der Importschablonen.
	 * @return	Liste mit <code>ImporterProcessor</code>-Objekten.
	 */
	public static List<ImporterProcessor> getImporterProcessors() {
		if (importerProcessors==null) importerProcessors=getImporterProcessorsInt();
		return importerProcessors;
	}

	/**
	 * Liefert den zu dem <code>type</code> gehörenden <code>ImporterProcessor</code>.
	 * @return <code>ImporterProcessor</code> zur Verarbeitung der zu importierenden Daten.
	 * @see #type
	 */
	public ImporterProcessor getProcessor() {
		List<ImporterProcessor> list=getImporterProcessors();
		if (type<0 || type>=list.size()) return null;
		return list.get(type);
	}

	/**
	 * Stellt <code>type</code> so ein, dass dieses auf den <code>ImporterProcessor</code>
	 * verweist, dessen Name als Parameter übergeben wurde.
	 * @param name	Name des neuen Importprozessors.
	 * @return	Liefert <code>true</code> zurück, wenn es einen Importprozessor mit dem angegebenen Namen gab.
	 */
	public boolean setProcessorFromName(String name) {
		List<ImporterProcessor> list=getImporterProcessors();
		for (int i=0;i<list.size();i++) {
			for (String l : list.get(i).getNames()) if (l.equalsIgnoreCase(name))  {type=i; return true;}
		}
		return false;
	}

	/**
	 * Kopiert das <code>ImportRecord</code>-Objekt.
	 * @return	Kopie des aktuellen <code>ImportRecord</code>-Objektes.
	 */
	@Override
	public ImporterRecord clone() {
		ImporterRecord record=new ImporterRecord();
		record.type=type;
		record.parameter=parameter;
		record.cellStart=cellStart;
		record.cellEnd=cellEnd;
		return record;
	}

	/**
	 * Vergleich das aktuelle <code>ImporterRecord</code>-Objekt mit einem weiteren Objekt
	 * @param record	Zweites <code>ImporterRecord</code>-Objekt, welches mit dem aktuellen Objekt verglichen werden soll.
	 * @return	Liefert <code>true</code> zurück, wenn die Objekte identisch sind.
	 */
	public boolean equalsImporterRecord(ImporterRecord record) {
		if (type!=record.type) return false;
		if (!parameter.equals(record.parameter)) return false;
		if (!cellStart.equals(record.cellStart)) return false;
		if (!cellEnd.equals(record.cellEnd)) return false;
		return true;
	}

	private String[] splitCellName(String name) {
		int index=name.lastIndexOf('!');
		if (index<0) return new String[]{"",name}; /* Kein Tabellenname */

		if (index==name.length()) return new String[]{name,""}; /* "!" am Ende */
		if (index==0) return new String[]{"",name}; /* "!" am Anfang */

		String table=name.substring(0,index);
		String cell=name.substring(index+1);

		if (table.length()>0 && table.charAt(0)=='\'' && table.charAt(table.length()-1)=='\'') {
			/* Tabellenname in Anführungszeichen */
			table=table.substring(1);
			if (table.length()>0) table=table.substring(0,table.length()-1);
		}

		return new String[]{table,cell};
	}

	/**
	 * Führt die Verarbeitung der aktuellen Importschablone durch.
	 * @param model	Callcenter-Modell, in das die Daten importiert werden sollen.
	 * @param table	Tabelle, aus der die Daten geladen werden sollen.
	 * @return	Gibt im Erfolgsfall <code>null</code> zurück, sonst eine Fehlermeldung.
	 */
	public String process(CallcenterModel model, MultiTable table) {
		ImporterProcessor processor=getProcessor();	if (processor==null) return null;

		String[] cellStartSplit=splitCellName(cellStart);
		String[] cellEndSplit=splitCellName(cellEnd);

		if (!cellStartSplit[0].equals(cellEndSplit[0])) return String.format(Language.tr("Importer.Error.DifferentTablesForArea"),cellStartSplit[0],cellEndSplit[0]);

		Table sheet=table.get(cellStartSplit[0]);
		if (sheet==null) return String.format(Language.tr("Importer.Error.TableNotInWorksheet"),cellStartSplit[0]);


		String s=null;
		if (processor.isNumbericalParameter()) {
			double[] data=sheet.getNumberArea(cellStartSplit[1],cellEndSplit[1]);
			if (data==null) return sheet.getAreaError()+" "+String.format(Language.tr("Importer.Error.Processing"),processor.getNames()[0]);
			s=processor.processNumbers(model,parameter,data);
		} else {
			String[] data=sheet.getDataArea(cellStartSplit[1],cellEndSplit[1]);
			if (data==null) return sheet.getAreaError()+" "+String.format(Language.tr("Importer.Error.Processing"),processor.getNames()[0]);
			s=processor.processStrings(model,parameter,data);
		}
		if (s!=null) return s+" "+String.format(Language.tr("Importer.Error.Processing"),processor.getNames()[0]);
		return null;
	}

	/**
	 * Lädt die Daten der Importschablone aus dem angegebenen XML-Knoten
	 * @param node XML-Knoten, aus dem die Daten geladen werden sollen.
	 * @return	Gibt im Erfolgsfall <code>true</code> zurück.
	 */
	public boolean loadFromXMLElement(Element node) {
		parameter=Language.trAllAttribute("XML.ImportTemplate.Record.Parameter",node);
		cellStart=Language.trAllAttribute("XML.ImportTemplate.Record.CellFrom",node);
		cellEnd=Language.trAllAttribute("XML.ImportTemplate.Record.CellTo",node);

		type=-1;
		String s=Language.trAllAttribute("XML.ImportTemplate.Record.Type",node); if (s==null || s.isEmpty()) return false;
		List<ImporterProcessor> list=getImporterProcessors();
		for (int i=0;i<list.size();i++) {
			for (String l : list.get(i).getNames()) if (l.equalsIgnoreCase(s))  {type=i; return true;}
		}
		return false;
	}

	/**
	 * Speichert die Daten der Importschablone in einem XML-Knoten
	 * @param parent	Elternknoten, unter dem der neue Knoten mit den Daten erstellt werden soll
	 */
	public void saveToXMLElement(Element parent) {
		ImporterProcessor processor=getProcessor();	if (processor==null) return;

		Document doc=parent.getOwnerDocument();
		Element node;
		parent.appendChild(node=doc.createElement(Language.trPrimary("XML.ImportTemplate.Record")));

		node.setAttribute(Language.trPrimary("XML.ImportTemplate.Record.Type"),processor.getNames()[0]);
		node.setAttribute(Language.trPrimary("XML.ImportTemplate.Record.Parameter"),parameter);
		node.setAttribute(Language.trPrimary("XML.ImportTemplate.Record.CellFrom"),cellStart);
		node.setAttribute(Language.trPrimary("XML.ImportTemplate.Record.CellTo"),cellEnd);
	}
}
