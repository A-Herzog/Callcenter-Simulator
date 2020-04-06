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
package ui.model;

import java.util.ArrayList;
import java.util.List;

import org.apache.commons.math3.distribution.AbstractRealDistribution;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;

import language.Language;
import mathtools.NumberTools;
import mathtools.distribution.tools.DistributionTools;

/**
 * Modelliert einen Agenten-Skill-Typ
 * @author Alexander Herzog
 * @see CallcenterRunModelSkillLevel
 * @see CallcenterModelAgent
 */
public final class CallcenterModelSkillLevel implements Cloneable {
	/** Maximaler (Sekunden-)Wert für die Skalierung der Werte der Bedienzeitenverteilungen über den Tag */
	public static final int callerTypeWorkingTimeMaxX=3600;
	/** Maximaler (Sekunden-)Wert für die Skalierung der Werte der Nachbearbeitungszeitenverteilungen über den Tag */
	public static final int callerTypePostProcessingTimeMaxX=3600;

	/** Name des Skill-Levels */
	public String name;

	/** Liste der Kundentypnamen für Kundentyp spezifische Daten */
	public List<String> callerTypeName;

	/** Liste der Kundentyp-spezifischen Bedienzeitverlängerung in Abhängigkeit von der Wartezeit */
	public List<String> callerTypeWorkingTimeAddOn;
	/** Liste der Kundentyp-spezifischen Bedienzeitverteilungen */
	public List<AbstractRealDistribution> callerTypeWorkingTime;
	/** Liste der Kundentyp-spezifischen Nachbearbeitungszeitverteilung */
	public List<AbstractRealDistribution> callerTypePostProcessingTime;

	/** Liste der Intervall-abhängigen Kundentyp-spezifischen Bedienzeitverlängerung in Abhängigkeit von der Wartezeit (die einzelnen Einträge des Arrays können <code>null</code> sein, dann gilt der globale Kundentyp-spezifische Wert) */
	public List<String[]> callerTypeIntervalWorkingTimeAddOn;
	/** Liste der Intervall-abhängigen Kundentyp-spezifischen Bedienzeitverteilungen (die einzelnen Einträge der Arrays können <code>null</code> sein, dann gilt der globale Kundentyp-spezifische Wert) */
	public List<AbstractRealDistribution[]> callerTypeIntervalWorkingTime;
	/** Liste der Intervall-abhängigen Kundentyp-spezifischen Nachbearbeitungszeitverteilung (die einzelnen Einträge der Arrays können <code>null</code> sein, dann gilt der globale Kundentyp-spezifische Wert) */
	public List<AbstractRealDistribution[]> callerTypeIntervalPostProcessingTime;

	/** Liste der Kundentyp spezifischen Prioritäten */
	public List<Integer> callerTypeScore;

	/**
	 * Konstruktor der Klasse
	 * @param name	Name des Skill-Levels
	 */
	public CallcenterModelSkillLevel(final String name) {
		this.name=name;
		callerTypeName=new ArrayList<String>();
		callerTypeWorkingTimeAddOn=new ArrayList<String>();
		callerTypeWorkingTime=new ArrayList<AbstractRealDistribution>();
		callerTypePostProcessingTime=new ArrayList<AbstractRealDistribution>();
		callerTypeIntervalWorkingTimeAddOn=new ArrayList<String[]>();
		callerTypeIntervalWorkingTime=new ArrayList<AbstractRealDistribution[]>();
		callerTypeIntervalPostProcessingTime=new ArrayList<AbstractRealDistribution[]>();
		callerTypeScore=new ArrayList<Integer>();
	}

	/**
	 * Konstruktor der Klasse
	 */
	public CallcenterModelSkillLevel() {this("");}

	@Override
	public CallcenterModelSkillLevel clone() {
		CallcenterModelSkillLevel skillLevel=new CallcenterModelSkillLevel();
		skillLevel.name=name;
		for (int i=0;i<callerTypeName.size();i++) skillLevel.callerTypeName.add(callerTypeName.get(i));

		for (int i=0;i<callerTypeWorkingTimeAddOn.size();i++) skillLevel.callerTypeWorkingTimeAddOn.add(callerTypeWorkingTimeAddOn.get(i));
		for (int i=0;i<callerTypeWorkingTime.size();i++) skillLevel.callerTypeWorkingTime.add(DistributionTools.cloneDistribution(callerTypeWorkingTime.get(i)));
		for (int i=0;i<callerTypePostProcessingTime.size();i++) skillLevel.callerTypePostProcessingTime.add(DistributionTools.cloneDistribution(callerTypePostProcessingTime.get(i)));

		for (int i=0;i<callerTypeIntervalWorkingTimeAddOn.size();i++) {
			String[] orig=callerTypeIntervalWorkingTimeAddOn.get(i);
			String[] dArray=new String[orig.length];
			for (int j=0;j<dArray.length;j++) dArray[j]=orig[j];
			skillLevel.callerTypeIntervalWorkingTimeAddOn.add(dArray);
		}

		for (int i=0;i<callerTypeIntervalWorkingTime.size();i++) {
			AbstractRealDistribution[] orig=callerTypeIntervalWorkingTime.get(i);
			AbstractRealDistribution[] dArray=new AbstractRealDistribution[orig.length];
			for (int j=0;j<dArray.length;j++) dArray[j]=DistributionTools.cloneDistribution(orig[j]);
			skillLevel.callerTypeIntervalWorkingTime.add(dArray);
		}

		for (int i=0;i<callerTypeIntervalPostProcessingTime.size();i++) {
			AbstractRealDistribution[] orig=callerTypeIntervalPostProcessingTime.get(i);
			AbstractRealDistribution[] dArray=new AbstractRealDistribution[orig.length];
			for (int j=0;j<dArray.length;j++) dArray[j]=DistributionTools.cloneDistribution(orig[j]);
			skillLevel.callerTypeIntervalPostProcessingTime.add(dArray);
		}

		for (int i=0;i<callerTypeScore.size();i++) skillLevel.callerTypeScore.add(callerTypeScore.get(i));
		return skillLevel;
	}

	/**
	 * Vergleicht das Skilllevel-Objekt mit einem anderen Skilllevel-Objekt
	 * @param skill Anderes Skilllevel-Objekt
	 * @return	Liefert <code>true</code>, wenn die beiden Skilllevels inhaltlich identisch sind
	 */
	public boolean equalsCallcenterModelSkillLevel(final CallcenterModelSkillLevel skill) {
		if (skill==null) return false;

		if (!skill.name.equals(name)) return false;

		if (skill.callerTypeName.size()!=callerTypeName.size()) return false;
		for (int i=0;i<callerTypeName.size();i++) if (!skill.callerTypeName.get(i).equals(callerTypeName.get(i))) return false;

		if (skill.callerTypeWorkingTimeAddOn.size()!=callerTypeWorkingTimeAddOn.size()) return false;
		for (int i=0;i<callerTypeWorkingTimeAddOn.size();i++) if (!skill.callerTypeWorkingTimeAddOn.get(i).equals(callerTypeWorkingTimeAddOn.get(i))) return false;

		if (skill.callerTypeWorkingTime.size()!=callerTypeWorkingTime.size()) return false;
		for (int i=0;i<callerTypeWorkingTime.size();i++) if (!DistributionTools.compare(skill.callerTypeWorkingTime.get(i),callerTypeWorkingTime.get(i))) return false;

		if (skill.callerTypePostProcessingTime.size()!=callerTypePostProcessingTime.size()) return false;
		for (int i=0;i<callerTypePostProcessingTime.size();i++) if (!DistributionTools.compare(skill.callerTypePostProcessingTime.get(i),callerTypePostProcessingTime.get(i))) return false;

		if (skill.callerTypeIntervalWorkingTimeAddOn.size()!=callerTypeIntervalWorkingTimeAddOn.size()) return false;
		for (int i=0;i<callerTypeIntervalWorkingTimeAddOn.size();i++) {
			String[] a=skill.callerTypeIntervalWorkingTimeAddOn.get(i);
			String[] b=callerTypeIntervalWorkingTimeAddOn.get(i);
			if (a.length!=b.length) return false;
			for (int j=0;j<a.length;j++) {
				if ((a[j]==null && b[j]!=null) || (a[j]!=null && b[j]==null)) return false;
				if (a[j]!=null && !a[j].equals(b[j])) return false;
			}
		}

		if (skill.callerTypeIntervalWorkingTime.size()!=callerTypeIntervalWorkingTime.size()) return false;
		for (int i=0;i<callerTypeIntervalWorkingTime.size();i++) {
			AbstractRealDistribution[] a=skill.callerTypeIntervalWorkingTime.get(i);
			AbstractRealDistribution[] b=callerTypeIntervalWorkingTime.get(i);
			if (a.length!=b.length) return false;
			for (int j=0;j<a.length;j++) if (!DistributionTools.compare(a[j],b[j])) return false;
		}

		if (skill.callerTypeIntervalPostProcessingTime.size()!=callerTypeIntervalPostProcessingTime.size()) return false;
		for (int i=0;i<callerTypeIntervalPostProcessingTime.size();i++) {
			AbstractRealDistribution[] a=skill.callerTypeIntervalPostProcessingTime.get(i);
			AbstractRealDistribution[] b=callerTypeIntervalPostProcessingTime.get(i);
			if (a.length!=b.length) return false;
			for (int j=0;j<a.length;j++) if (!DistributionTools.compare(a[j],b[j])) return false;
		}

		if (skill.callerTypeScore.size()!=callerTypeScore.size()) return false;
		for (int i=0;i<callerTypeScore.size();i++) if (!skill.callerTypeScore.get(i).equals(callerTypeScore.get(i))) return false;

		return true;
	}

	/**
	 * Ergänzt optionale Daten (wie es auch die Bearbeiten-Dialoge machen),
	 * um unnötige Starts der Hintergrunsimulation bedingt durch nur scheinbar
	 * im Dialog veränderte Modell zu vermeiden.
	 * @param model	Modell das ergänzt werden soll
	 */
	public void prepareData(CallcenterModel model) {
		/* Hier gibt's nichts vorzubereiten. */
	}

	/**
	 * Versucht einen Skill-Level aus dem übergebenen XML-Node zu laden
	 * @param node	XML-Knoten, der die Skill-Level-Daten enthält
	 * @return	Tritt ein Fehler auf, so wird die Fehlermeldung als String zurückgegeben. Im Erfolgsfall wird <code>null</code> zurückgegeben.
	 */
	public String loadFromXML(Element node) {
		name=Language.trAllAttribute("XML.Model.GeneralAttributes.Name",node);

		callerTypeName.clear();
		callerTypeWorkingTimeAddOn.clear();
		callerTypeWorkingTime.clear();
		callerTypePostProcessingTime.clear();
		callerTypeIntervalWorkingTimeAddOn.clear();
		callerTypeIntervalWorkingTime.clear();
		callerTypeIntervalPostProcessingTime.clear();
		callerTypeScore.clear();

		NodeList l=node.getChildNodes();
		for (int i=0; i<l.getLength();i++) {
			if (!(l.item(i) instanceof Element)) continue;
			Element e=(Element)l.item(i);
			if (!Language.trAll("XML.Model.SkillLevel.ClientType",e.getNodeName())) continue;

			String typeName=Language.trAllAttribute("XML.Model.GeneralAttributes.Name",e);
			String typeWorkingAddOn="0";
			AbstractRealDistribution typeWorking=null;
			AbstractRealDistribution typePostProcessing=null;
			String[] typeIntervalWorkingAddOn=new String[48];
			AbstractRealDistribution[] typeIntervalWorking=new AbstractRealDistribution[48];
			AbstractRealDistribution[] typeIntervalPostProcessing=new AbstractRealDistribution[48];
			int typeScore=1;

			NodeList l2=e.getChildNodes();
			for (int j=0; j<l2.getLength();j++) {
				if (!(l2.item(j) instanceof Element)) continue;
				Element e2=(Element)l2.item(j);
				String t=e2.getNodeName();

				if (Language.trAll("XML.Model.SkillLevel.ClientType.HoldingTimeAddOn",t)) {
					String intervalStr=Language.trAllAttribute("XML.Model.SkillLevel.Interval",e2);
					int interval=-1;
					if (intervalStr!=null && !intervalStr.isEmpty()) {
						Integer I=NumberTools.getNotNegativeInteger(intervalStr);
						if (I!=null && I>0 && I<=48) interval=I-1;
					}
					String s=e2.getTextContent();
					if (interval<0)	typeWorkingAddOn=s; else typeIntervalWorkingAddOn[interval]=s;
					continue;
				}

				if (Language.trAll("XML.Model.SkillLevel.ClientType.HoldingTimeDistribution",t)) {
					String intervalStr=Language.trAllAttribute("XML.Model.SkillLevel.Interval",e2);
					int interval=-1;
					if (intervalStr!=null && !intervalStr.isEmpty()) {
						Integer I=NumberTools.getNotNegativeInteger(intervalStr);
						if (I!=null && I>0 && I<=48) interval=I-1;
					}
					AbstractRealDistribution d=DistributionTools.distributionFromString(e2.getTextContent(),3600);
					if (d==null) return String.format(Language.tr("XML.Model.SkillLevel.ClientType.HoldingTimeDistribution.Error"),callerTypeName.size()+1);
					if (interval<0)	typeWorking=d; else typeIntervalWorking[interval]=d;
					continue;
				}
				if (Language.trAll("XML.Model.SkillLevel.ClientType.PostProcessingTimeDistribution",t)) {
					String intervalStr=Language.trAllAttribute("XML.Model.SkillLevel.Interval",e2);
					int interval=-1;
					if (intervalStr!=null && !intervalStr.isEmpty()) {
						Integer I=NumberTools.getNotNegativeInteger(intervalStr);
						if (I!=null && I>0 && I<=48) interval=I-1;
					}
					AbstractRealDistribution d=DistributionTools.distributionFromString(e2.getTextContent(),3600);
					if (d==null) return String.format(Language.tr("XML.Model.SkillLevel.ClientType.PostProcessingTimeDistribution.Error"),callerTypeName.size()+1);
					if (interval<0)	typePostProcessing=d; else typeIntervalPostProcessing[interval]=d;
					continue;
				}
				if (Language.trAll("XML.Model.SkillLevel.ClientType.Score",t)) {
					Integer K=NumberTools.getNotNegativeInteger(e2.getTextContent());
					if (K==null) return String.format(Language.tr("XML.Model.SkillLevel.ClientType.Score.Error"),callerTypeName.size()+1);
					typeScore=K; continue;
				}
			}
			if (typeWorking==null || typePostProcessing==null) return String.format(Language.tr("XML.Model.SkillLevel.ClientType.Error"),callerTypeName.size()+1);
			callerTypeName.add(typeName);

			callerTypeWorkingTimeAddOn.add(typeWorkingAddOn);
			callerTypeWorkingTime.add(typeWorking);
			callerTypePostProcessingTime.add(typePostProcessing);
			callerTypeIntervalWorkingTimeAddOn.add(typeIntervalWorkingAddOn);
			callerTypeIntervalWorkingTime.add(typeIntervalWorking);
			callerTypeIntervalPostProcessingTime.add(typeIntervalPostProcessing);
			callerTypeScore.add(typeScore);
		}
		return null;
	}

	/**
	 * Erstellt unterhalb des übergebenen XML-Knotens einen neuen Knoten, der die gesamten Skill-Level-Daten enthält.
	 * @param parent	Eltern-XML-Knoten
	 */
	public void saveToXML(Element parent) {
		Document doc=parent.getOwnerDocument();
		Element node=doc.createElement(Language.trPrimary("XML.Model.SkillLevel")); parent.appendChild(node);
		node.setAttribute(Language.trPrimary("XML.Model.GeneralAttributes.Name"),name);
		Element e,e2;
		AbstractRealDistribution[] dArray;
		String[] sArray;

		for (int i=0;i<callerTypeName.size();i++) {
			node.appendChild(e=doc.createElement(Language.trPrimary("XML.Model.SkillLevel.ClientType"))); e.setAttribute(Language.trPrimary("XML.Model.GeneralAttributes.Name"),callerTypeName.get(i));

			e.appendChild(e2=doc.createElement(Language.trPrimary("XML.Model.SkillLevel.ClientType.HoldingTimeAddOn"))); e2.setTextContent(callerTypeWorkingTimeAddOn.get(i));
			sArray=callerTypeIntervalWorkingTimeAddOn.get(i);
			for (int j=0;j<sArray.length;j++) if (sArray[j]!=null) {
				e.appendChild(e2=doc.createElement(Language.trPrimary("XML.Model.SkillLevel.ClientType.HoldingTimeAddOn")));
				e2.setAttribute(Language.trPrimary("XML.Model.SkillLevel.Interval"),""+(j+1));
				e2.setTextContent(sArray[j]);
			}

			e.appendChild(e2=doc.createElement(Language.trPrimary("XML.Model.SkillLevel.ClientType.HoldingTimeDistribution"))); e2.setTextContent(DistributionTools.distributionToString(callerTypeWorkingTime.get(i)));
			dArray=callerTypeIntervalWorkingTime.get(i);
			for (int j=0;j<dArray.length;j++) if (dArray[j]!=null) {
				e.appendChild(e2=doc.createElement(Language.trPrimary("XML.Model.SkillLevel.ClientType.HoldingTimeDistribution")));
				e2.setAttribute(Language.trPrimary("XML.Model.SkillLevel.Interval"),""+(j+1));
				e2.setTextContent(DistributionTools.distributionToString(dArray[j]));
			}

			e.appendChild(e2=doc.createElement(Language.trPrimary("XML.Model.SkillLevel.ClientType.PostProcessingTimeDistribution"))); e2.setTextContent(DistributionTools.distributionToString(callerTypePostProcessingTime.get(i)));
			dArray=callerTypeIntervalPostProcessingTime.get(i);
			for (int j=0;j<dArray.length;j++) if (dArray[j]!=null) {
				e.appendChild(e2=doc.createElement(Language.trPrimary("XML.Model.SkillLevel.ClientType.PostProcessingTimeDistribution")));
				e2.setAttribute(Language.trPrimary("XML.Model.SkillLevel.Interval"),""+(j+1));
				e2.setTextContent(DistributionTools.distributionToString(dArray[j]));
			}

			e.appendChild(e2=doc.createElement(Language.trPrimary("XML.Model.SkillLevel.ClientType.Score"))); e2.setTextContent(""+callerTypeScore.get(i));
		}
	}
}
