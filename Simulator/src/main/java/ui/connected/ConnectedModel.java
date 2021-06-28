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

import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Set;

import javax.swing.filechooser.FileSystemView;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;

import language.Language;
import mathtools.NumberTools;
import simulator.Statistics;
import xml.XMLTools;

/**
 * Diese Klasse speichert die Namen der Modelle, Statistikdateien und Übertragsdaten für ein verbundenes Simulationsmodell.
 * @author Alexander Herzog
 * @version 1.0
 */
public final class ConnectedModel implements Cloneable {
	/**
	 * Basisordner für Modell- und Statistikdateien
	 * Wird ein Dateiname ohne Pfad angegeben, so wird angenommen, dass die Datei sich in diesem Verzeichnis befindet.
	 */
	public String defaultFolder;

	/**
	 * Dateinamen der Modelle, die nacheinander simuliert werden sollen
	 * Die Dateinamen können mit oder ohne Pfad angegeben werden. Dateinamen ohne Pfad werden als relativ zu <code>defaultFolder</code> angenommen.
	 * @see #defaultFolder
	 */
	public List<String> models=new ArrayList<>();

	/**
	 * Dateinamen der Statistikdateien, in denen die Simulationsergebnisse gespeichert werden sollen.
	 * Ist ein Dateinamenseintrag leer, so wird angenommen, dass die Ergebnisse nicht gespeichert werden sollen.
	 * Die Dateinamen können mit oder ohne Pfad angegeben werden. Dateinamen ohne Pfad werden als relativ zu <code>defaultFolder</code> angenommen.
	 * @see #defaultFolder
	 */
	public List<String> statistics=new ArrayList<>();

	/**
	 * Gibt an, wie viele am Vortag endgültig abgebrochene Kunden am aktuellen Tag einen erneuten Anlauf starten.
	 */
	public List<HashMap<String,ConnectedModelUebertrag>> uebertrag=new ArrayList<>();

	/**
	 * Statistikdatei für den Übertrag hin zum ersten Tag.
	 * (In <code>uebertrag.get(0)</code> steht der Übertrag von Tag 0 zu Tag 1.)
	 * Ist das Feld leer, so wird angenommen, dass kein Übertrag zum ersten Tag hin erfolgen soll.
	 * Der Dateiname kann mit oder ohne Pfad angegeben werden. Dateinamen ohne Pfad werden als relativ zu <code>defaultFolder</code> angenommen.
	 * @see #uebertrag
	 */
	public String statisticsDay0="";

	/**
	 * Kundentypen der zusätzlichen Anrufer, die in Tag 1 zusätzlich eingespielt werden sollen.
	 */
	public List<String> additionalDay0CallerNames=new ArrayList<>();


	/**
	 * Anruferanzahlen nach Kundentypen, die in Tag 1 zusätzlich eingespielt werden sollen.
	 */
	public List<Integer> additionalDay0CallerCount=new ArrayList<>();

	/**
	 * Mögliche Namen des Basiselement von Connected-XML-Dateien (zur Erkennung von Dateien dieses Typs.)
	 */
	public static final String[] XMLBaseElement=Language.trAll("XML.Connected");

	/**
	 * Konstruktor der Klasse <code>ConnectedModel</code>
	 */
	public ConnectedModel() {
		defaultFolder=FileSystemView.getFileSystemView().getDefaultDirectory().getPath();
	}

	/**
	 * Vergleicht das vorliegende Mehrtages-Modell mit einem anderen Mehrtages-Modell
	 * @param model	Modell, mit dem das aktuelle Modell verglichen werden soll
	 * @return	Gibt <code>true</code> zurück, wenn die Modelle identisch sind.
	 */
	public boolean equalsConnectedModel(ConnectedModel model) {
		if (model==null) return false;

		if (!model.defaultFolder.equalsIgnoreCase(defaultFolder)) return false;

		if (model.models.size()!=models.size()) return false;
		for (int i=0;i<models.size();i++) if (!model.models.get(i).equalsIgnoreCase(models.get(i))) return false;

		if (model.statistics.size()!=statistics.size()) return false;
		for (int i=0;i<statistics.size();i++) if (!model.statistics.get(i).equalsIgnoreCase(statistics.get(i))) return false;

		if (model.uebertrag.size()!=uebertrag.size()) return false;
		for (int i=0;i<uebertrag.size();i++) {
			HashMap<String,ConnectedModelUebertrag> m1=model.uebertrag.get(i);
			HashMap<String,ConnectedModelUebertrag> m2=uebertrag.get(i);

			if (m1.size()!=m2.size()) return false;
			Set<String> s1=m1.keySet();
			Set<String> s2=m1.keySet();
			if (!s1.containsAll(s2) || !s2.containsAll(s1)) return false;
			Iterator<String> it=s1.iterator();
			while (it.hasNext()) {
				String s=it.next();
				ConnectedModelUebertrag u1=m1.get(s);
				ConnectedModelUebertrag u2=m2.get(s);
				if (!u1.equalsConnectedModelUebertrag(u2)) return false;
			}
		}

		if (!model.statisticsDay0.equalsIgnoreCase(statisticsDay0)) return false;

		if (model.additionalDay0CallerNames.size()!=additionalDay0CallerNames.size()) return false;
		for (int i=0;i<additionalDay0CallerNames.size();i++) if (!model.additionalDay0CallerNames.get(i).equals(additionalDay0CallerNames.get(i))) return false;
		if (model.additionalDay0CallerCount.size()!=additionalDay0CallerCount.size()) return false;
		for (int i=0;i<additionalDay0CallerCount.size();i++) {
			int a=model.additionalDay0CallerCount.get(i);
			int b=additionalDay0CallerCount.get(i);
			if (a!=b) return false;
		}

		return true;
	}

	/**
	 * Dupliziert das <code>ConnectedModel</code>-Objekt
	 */
	@Override
	public ConnectedModel clone() {
		final ConnectedModel model=new ConnectedModel();
		model.defaultFolder=defaultFolder;
		model.models.addAll(models);
		model.statistics.addAll(statistics);
		model.uebertrag.addAll(uebertrag);
		model.statisticsDay0=statisticsDay0;
		model.additionalDay0CallerNames.addAll(additionalDay0CallerNames);
		model.additionalDay0CallerCount.addAll(additionalDay0CallerCount);
		return model;
	}

	/**
	 * Setzt alle Einstellungen in dem Mehrtages-Modell zurück.
	 */
	public void clear() {
		models.clear();
		statistics.clear();
		uebertrag.clear();
		statisticsDay0="";
		additionalDay0CallerNames.clear();
		additionalDay0CallerCount.clear();
	}

	/**
	 * Legt einen neuen Eintrag an.
	 * @return	Index des neuen Eintrags
	 */
	public int addRecord() {
		models.add("");
		statistics.add("");
		uebertrag.add(new HashMap<String,ConnectedModelUebertrag>());
		return models.size()-1;
	}

	/**
	 * Löscht einen Eintrag
	 * @param index	0-basierender Index des Eintrags
	 * @return	Gibt an, ob der Eintrag erfolgreich gelöscht werden konnte
	 */
	public boolean delRecord(int index) {
		if (index<0 || index>=models.size()) return false;
		models.remove(index);
		statistics.remove(index);
		uebertrag.remove(index);
		return true;
	}

	/**
	 * Verschiebt einen Eintrag um eine Position nach oben in
	 * der Liste der zu simulierenden, verknüpften Modelle.
	 * @param index	0-basierender Index des Eintrags
	 * @return	Gibt an, ob der Eintrag verschoben werden konnte
	 */
	public boolean moveUp(final int index) {
		if (index<=0 || index>=models.size()) return false;
		String s;
		HashMap<String,ConnectedModelUebertrag> m;
		s=models.get(index); models.set(index,models.get(index-1)); models.set(index-1,s);
		s=statistics.get(index); statistics.set(index,statistics.get(index-1)); statistics.set(index-1,s);
		m=uebertrag.get(index); uebertrag.set(index,uebertrag.get(index-1)); uebertrag.set(index-1,m);
		return true;
	}

	/**
	 * Verschiebt einen Eintrag um eine Position nach unten in
	 * der Liste der zu simulierenden, verknüpften Modelle.
	 * @param index	0-basierender Index des Eintrags
	 * @return	Gibt an, ob der Eintrag verschoben werden konnte
	 */
	public boolean moveDown(int index) {
		if (index<0 || index==models.size()-1) return false;
		String s;
		HashMap<String,ConnectedModelUebertrag> m;
		s=models.get(index); models.set(index,models.get(index+1)); models.set(index+1,s);
		s=statistics.get(index); statistics.set(index,statistics.get(index+1)); statistics.set(index+1,s);
		m=uebertrag.get(index); uebertrag.set(index,uebertrag.get(index+1)); uebertrag.set(index+1,m);
		return true;
	}

	/**
	 * Versucht ein verbundenes Modell aus einem xml-Element zu laden.
	 * @param root	xml-Element
	 * @return	Liefert im Erfolgsfall <code>null</code>, sonst eine Fehlermeldung
	 * @see #loadFromFile(File)
	 */
	private String loadFromXML(final Element root) {
		int dayCount=0;

		final List<String> modelsNew=new ArrayList<>();
		final List<String> statisticsNew=new ArrayList<>();
		final List<HashMap<String,ConnectedModelUebertrag>> uebertragNew=new ArrayList<>();
		final List<String> additionalCallerNew=new ArrayList<>();
		final List<Integer> additionalCountNew=new ArrayList<>();

		if (!Language.trAll("XML.Connected",root.getNodeName())) return String.format(Language.tr("XML.Connected.Error"),Language.trPrimary("XML.Connected"));

		String newDefaultFolder=Language.trAllAttribute("XML.Connected.BaseFolder",root);
		String newStatisticsDay0=Language.trAllAttribute("XML.Connected.InitialCarryOver",root);

		NodeList l=root.getChildNodes();
		for (int i=0; i<l.getLength();i++) {
			if (!(l.item(i) instanceof Element)) continue;
			Element e=(Element)l.item(i);
			String s=e.getNodeName();
			if (Language.trAll("XML.Connected.Day",s)) {
				dayCount++;
				NodeList l2=e.getChildNodes();
				for (int j=0; j<l2.getLength();j++) {
					if (!(l2.item(j) instanceof Element)) continue;
					Element e2=(Element)l2.item(j);
					String t=e2.getNodeName();

					if (Language.trAll("XML.Connected.Model",t)) {
						if (modelsNew.size()!=dayCount-1) return String.format(Language.tr("XML.Connected.Model.Error"),dayCount);
						modelsNew.add(e2.getTextContent());
						continue;
					}

					if (Language.trAll("XML.Connected.Statistics",t)) {
						if (statisticsNew.size()!=dayCount-1) return String.format(Language.tr("XML.Connected.Statistics.Error"),dayCount);
						statisticsNew.add(e2.getTextContent());
						continue;
					}


					if (Language.trAll("XML.Connected.CarryOver",t)) {
						if (uebertragNew.size()!=dayCount-1) return String.format(Language.tr("XML.Connected.CarryOver.Single.Error"),dayCount);
						HashMap<String,ConnectedModelUebertrag> h=new HashMap<>();

						NodeList l3=e2.getChildNodes();
						for (int k=0; k<l3.getLength();k++) {
							if (!(l3.item(k) instanceof Element)) continue;
							Element e3=(Element)l3.item(k);
							if (!Language.trAll("XML.Connected.CarryOver.Single",e3.getNodeName())) continue;
							ConnectedModelUebertrag u=new ConnectedModelUebertrag();
							String s1=u.loadFromXML(e3);
							if (s1==null) return String.format(Language.tr("XML.Connected.CarryOver.Single.Error"),dayCount);
							h.put(s1,u);
						}
						uebertragNew.add(h);
						continue;
					}
				}
			}

			if (Language.trAll("XML.Connected.InitialInventory",s)) {
				NodeList l3=e.getChildNodes();
				for (int k=0; k<l3.getLength();k++) {
					if (!(l3.item(k) instanceof Element)) continue;
					Element e3=(Element)l3.item(k);
					if (!Language.trAll("XML.Connected.InitialInventory.ClientType",e3.getNodeName())) continue;
					Integer I=NumberTools.getNotNegativeInteger(e3.getTextContent()); if (I==null) return String.format(Language.tr("XML.Connected.InitialInventory.Error"),k+1);
					String u=Language.trAllAttribute("XML.Connected.InitialInventory.ClientType.Name",e3); if (u.isEmpty()) return String.format(Language.tr("XML.Connected.InitialInventory.ClientType.Name.Error"),k+1);
					additionalCallerNew.add(u);
					additionalCountNew.add(I);
				}
				continue;
			}
		}

		if (modelsNew.size()!=statisticsNew.size() || statisticsNew.size()!=uebertragNew.size()) return Language.tr("XML.Connected.ErrorDataDoNotMatch");

		defaultFolder=newDefaultFolder;
		models=modelsNew;
		statistics=statisticsNew;
		uebertrag=uebertragNew;
		statisticsDay0=newStatisticsDay0;
		additionalDay0CallerNames=additionalCallerNew;
		additionalDay0CallerCount=additionalCountNew;
		return null;
	}

	/**
	 * Versucht ein verbundenes Modell aus einer Datei zu laden.
	 * @param file	Eingabedatei
	 * @return	Liefert im Erfolgsfall <code>null</code>, sonst eine Fehlermeldung
	 */
	public String loadFromFile(final File file) {
		XMLTools xml=new XMLTools(file);
		Element root=xml.load();
		if (root==null) return xml.getError();
		clear();
		return loadFromXML(root);
	}

	/**
	 * Speichert das verbundene Modell in einem xml-Element.
	 * @param root	xml-Element
	 * @see #saveToFile(File)
	 */
	private void addDataToXML(final Element root) {
		Document doc=root.getOwnerDocument();
		Element e,e2;

		root.setAttribute(Language.trPrimary("XML.Connected.BaseFolder"),defaultFolder);
		root.setAttribute(Language.trPrimary("XML.Connected.InitialCarryOver"),statisticsDay0);

		for (int i=0;i<models.size();i++) {
			root.appendChild(e=doc.createElement(Language.trPrimary("XML.Connected.Day")));
			e.appendChild(e2=doc.createElement(Language.trPrimary("XML.Connected.Model"))); e2.setTextContent(models.get(i));
			e.appendChild(e2=doc.createElement(Language.trPrimary("XML.Connected.Statistics"))); e2.setTextContent(statistics.get(i));
			e.appendChild(e2=doc.createElement(Language.trPrimary("XML.Connected.CarryOver")));
			HashMap<String,ConnectedModelUebertrag> h=uebertrag.get(i);
			Iterator<String> it=h.keySet().iterator();
			while (it.hasNext()) {
				String s=it.next();
				h.get(s).saveToXML(e2,s);
			}
		}

		if (additionalDay0CallerNames.size()>0) {
			root.appendChild(e=doc.createElement(Language.trPrimary("XML.Connected.InitialInventory")));
			for (int i=0;i<additionalDay0CallerNames.size();i++) {
				e.appendChild(e2=doc.createElement(Language.trPrimary("XML.Connected.InitialInventory.ClientType")));
				e2.setAttribute(Language.trPrimary("XML.Connected.InitialInventory.ClientType.Name"),additionalDay0CallerNames.get(i));
				int j=additionalDay0CallerCount.get(i);
				e2.setTextContent(""+j);
			}
		}
	}

	/**
	 * Speichert das verbundene Modell in einer Datei.
	 * @param file	Ausgabedatei
	 * @return	Gibt an, ob das Speichern erfolgreich war
	 */
	public boolean saveToFile(final File file) {
		XMLTools xml=new XMLTools(file);
		Element root=xml.generateRoot(Language.trPrimary("XML.Connected"));
		if (root==null) return false;
		addDataToXML(root);
		return xml.save(root);
	}

	/**
	 * Lädt die Statistikergebnisse eines verknüpften Simulationslaufes in ein Array als Statistikobjekten.
	 * @return	Liefert im Erfolgsfall ein Objekt vom Typ <code>ComplexStatisticSimData[]</code> zurück. Im Fehlerfall wird ein String geliefert, der die Fehlermeldung enthält.
	 */
	public Object getStatistics() {
		if (statistics.size()==0) return Language.tr("XML.Connected.LoadStatistic.ErrorNoDays");

		File folder=new File(defaultFolder);
		Statistics[] data=new Statistics[statistics.size()];

		for (int i=0;i<statistics.size();i++) {
			String s=statistics.get(i);
			if (s==null || s.isEmpty()) return String.format(Language.tr("XML.Connected.LoadStatistic.ErrorNoStatisticFile"),i+1);
			File statFile=(s.contains("/") || s.contains("\\"))?new File(s):new File(folder,s);
			if (!statFile.exists()) return String.format(Language.tr("XML.Connected.LoadStatistic.ErrorNoStatisticFileFound"),i+1,statFile);

			Statistics stat=new Statistics(null,null,0,0);
			String t=stat.loadFromFile(statFile); if (t!=null) return String.format(Language.tr("XML.Connected.LoadStatistic.ErrorLoadingStatisticFile"),statFile,i+1)+" "+t;

			if (i>0) {
				if (data[0].kundenProTyp.length!=stat.kundenProTyp.length) return String.format(Language.tr("XML.Connected.LoadStatistic.ErrorClientTypesDoNotMatch"),i+1,i);
				for (int j=0;j<stat.kundenProTyp.length;j++) if (!data[0].kundenProTyp[j].name.equals(stat.kundenProTyp[j].name)) return String.format(Language.tr("XML.Connected.LoadStatistic.ErrorClientTypesDoNotMatch"),i+1,i);

				if (data[0].agentenProCallcenter.length!=stat.agentenProCallcenter.length) return String.format(Language.tr("XML.Connected.LoadStatistic.ErrorCallCenterDoNotMatch"),i+1,i);
				for (int j=0;j<stat.agentenProCallcenter.length;j++) if (!data[0].agentenProCallcenter[j].name.equals(stat.agentenProCallcenter[j].name)) return String.format(Language.tr("XML.Connected.LoadStatistic.ErrorCallCenterDoNotMatch"),i+1,i);

				if (data[0].agentenProSkilllevel.length!=stat.agentenProSkilllevel.length) return String.format(Language.tr("XML.Connected.LoadStatistic.ErrorSkillLevelDoNotMatch"),i+1,i);
				for (int j=0;j<stat.agentenProSkilllevel.length;j++) if (!data[0].agentenProSkilllevel[j].name.equals(stat.agentenProSkilllevel[j].name)) return String.format(Language.tr("XML.Connected.LoadStatistic.ErrorSkillLevelDoNotMatch"),i+1,i);
			}

			data[i]=stat;
		}

		return data;
	}
}
