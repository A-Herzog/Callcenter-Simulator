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

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;

import language.Language;
import mathtools.NumberTools;
import mathtools.distribution.DataDistributionImpl;
import mathtools.distribution.tools.DistributionTools;

/**
 * Diese Klasse speichert alle Daten zu einem Callcenter.<br>
 * Sie wird als Teil der Klasse <code>CallcenterModel</code> verwendet.
 * @author Alexander Herzog
 * @version 1.0
 * @see CallcenterModel
 * @see CallcenterRunModelCallcenter
 */
public final class CallcenterModelCallcenter implements Cloneable {
	/** Name des Callcenters */
	public String name;

	/** Callcenter aktiv? */
	public boolean active;

	/** Liste der Agentengruppen innerhalb des Callcenters */
	public List<CallcenterModelAgent> agents;

	/** Benötigte Vermittlungszeit, bevor ein Gespräch beginnt */
	public int technicalFreeTime;

	/** Kann der Kunde das Warten in der technischen Bereitzeit noch abbrechen? (Empfindet er diese also als Wartezeit? Gezählt wird sie auf jeden Fall als Wartezeit.) */
	public boolean technicalFreeTimeIsWaitingTime;

	/** Globale Score des Callcenters für die Vermittlung von Anrufen */
	public int score;

	/** Faktor für die Agentenscore zur Berücksichtigung der freien Zeit seit dem letzten Anruf */
	public double agentScoreFreeTimeSinceLastCall;
	/** Faktor für die Agentenscore zur Berücksichtigung des Leerlaufanteils */
	public double agentScoreFreeTimePart;

	/** Liste der Namen für die kundenspezifischen Mindestwartezeiten */
	public List<String> callerMinWaitingTimeName;
	/** Liste der kundenspezifischen Mindestwartezeiten */
	public List<Integer> callerMinWaitingTime;

	/** Callcenter-abhängige Produktivität pro Intervall (kann <code>null</code> sein, dann gilt der globale Wert) */
	public DataDistributionImpl efficiencyPerInterval;

	/** Callcenter-abhängige Planungsaufschlag pro Intervall (kann <code>null</code> sein, dann gilt der globale Wert) */
	public DataDistributionImpl additionPerInterval;

	/** Konstruktor der Klasse <code>CallcenterModelCallcenter</code>
	 * @param name Name des neuen Callcenters
	 */
	public CallcenterModelCallcenter(String name) {
		this.name=name;

		active=true;

		agents=new ArrayList<CallcenterModelAgent>();
		technicalFreeTime=0;
		technicalFreeTimeIsWaitingTime=true;
		score=1;
		agentScoreFreeTimeSinceLastCall=1;
		agentScoreFreeTimePart=0;

		callerMinWaitingTimeName=new ArrayList<String>();
		callerMinWaitingTime=new ArrayList<Integer>();

		efficiencyPerInterval=null;
		additionPerInterval=null;
	}

	/** Konstruktor der Klasse <code>CallcenterModelCallcenter</code> */
	public CallcenterModelCallcenter() {
		this(Language.tr("Model.DefaultName.Callcenter"));
	}

	@Override
	public CallcenterModelCallcenter clone() {
		CallcenterModelCallcenter callcenter=new CallcenterModelCallcenter();

		callcenter.name=name;

		callcenter.active=active;

		for (CallcenterModelAgent agentGroup : agents) callcenter.agents.add(agentGroup.clone());
		callcenter.technicalFreeTime=technicalFreeTime;
		callcenter.technicalFreeTimeIsWaitingTime=technicalFreeTimeIsWaitingTime;
		callcenter.score=score;
		callcenter.agentScoreFreeTimeSinceLastCall=agentScoreFreeTimeSinceLastCall;
		callcenter.agentScoreFreeTimePart=agentScoreFreeTimePart;

		for (int i=0;i<callerMinWaitingTimeName.size();i++) callcenter.callerMinWaitingTimeName.add(callerMinWaitingTimeName.get(i));
		for (int i=0;i<callerMinWaitingTime.size();i++) callcenter.callerMinWaitingTime.add(callerMinWaitingTime.get(i));

		if (efficiencyPerInterval==null) callcenter.efficiencyPerInterval=null; else callcenter.efficiencyPerInterval=efficiencyPerInterval.clone();
		if (additionPerInterval==null) callcenter.additionPerInterval=null; else callcenter.additionPerInterval=additionPerInterval.clone();

		return callcenter;
	}

	/**
	 * Vergleicht das Callcenter-Objekt mit einem anderen Callcenter-Objekt
	 * @param callcenter Anderes Callcenter-Objekt
	 * @return	Liefert <code>true</code>, wenn die beiden Callcenter inhaltlich identisch sind
	 */
	public boolean equalsCallcenterModelCallcenter(final CallcenterModelCallcenter callcenter) {
		if (callcenter==null) return false;

		if (!callcenter.name.equals(name)) return false;

		if (callcenter.active!=active) return false;

		if (callcenter.agents.size()!=agents.size()) return false;
		for (int i=0;i<agents.size();i++) if (!agents.get(i).equalsCallcenterModelAgent(callcenter.agents.get(i))) return false;

		if (callcenter.technicalFreeTime!=technicalFreeTime) return false;
		if (callcenter.technicalFreeTimeIsWaitingTime!=technicalFreeTimeIsWaitingTime) return false;

		if (callcenter.score!=score) return false;

		if (callcenter.agentScoreFreeTimeSinceLastCall!=agentScoreFreeTimeSinceLastCall) return false;
		if (callcenter.agentScoreFreeTimePart!=agentScoreFreeTimePart) return false;

		if (callcenter.callerMinWaitingTimeName.size()!=callerMinWaitingTimeName.size()) return false;
		for (int i=0;i<callerMinWaitingTimeName.size();i++) if (!callerMinWaitingTimeName.get(i).equals(callcenter.callerMinWaitingTimeName.get(i))) return false;

		if (callcenter.callerMinWaitingTime.size()!=callerMinWaitingTime.size()) return false;
		for (int i=0;i<callerMinWaitingTime.size();i++) if (!callerMinWaitingTime.get(i).equals(callcenter.callerMinWaitingTime.get(i))) return false;

		if (callcenter.efficiencyPerInterval!=null && efficiencyPerInterval==null) return false;
		if (callcenter.efficiencyPerInterval==null && efficiencyPerInterval!=null) return false;
		if (callcenter.efficiencyPerInterval!=null && efficiencyPerInterval!=null && !DistributionTools.compare(callcenter.efficiencyPerInterval,efficiencyPerInterval)) return false;

		if (callcenter.additionPerInterval!=null && additionPerInterval==null) return false;
		if (callcenter.additionPerInterval==null && additionPerInterval!=null) return false;
		if (callcenter.additionPerInterval!=null && additionPerInterval!=null && !DistributionTools.compare(callcenter.additionPerInterval,additionPerInterval)) return false;

		return true;
	}

	/**
	 * Ergänzt optionale Daten (wie es auch die Bearbeiten-Dialoge machen),
	 * um unnötige Starts der Hintergrunsimulation bedingt durch nur scheinbar
	 * im Dialog veränderte Modell zu vermeiden.
	 * @param model	Modell das ergänzt werden soll
	 */
	public void prepareData(CallcenterModel model) {
		/* Mindestwartezeiten für Kundentypen, für die keine Mindestwartezeiten definiert sind mit 0 belegen. */
		for (int i=0;i<model.caller.size();i++) {
			String name=model.caller.get(i).name;
			if (callerMinWaitingTimeName.indexOf(name)<0) {
				callerMinWaitingTimeName.add(name);
				callerMinWaitingTime.add(0);
			}
		}
	}

	/**
	 * Versucht ein Callcenter aus dem übergebenen XML-Node zu laden
	 * @param node	XML-Knoten, der die Callcenter-Daten enthält
	 * @return	Tritt ein Fehler auf, so wird die Fehlermeldung als String zurückgegeben. Im Erfolgsfall wird <code>null</code> zurückgegeben.
	 */
	public String loadFromXML(Element node) {
		String b=Language.trAllAttribute("XML.Model.GeneralAttributes.Active",node);
		if (Language.trAll("XML.General.BoolFalse",b)) active=false;

		name=Language.trAllAttribute("XML.Model.GeneralAttributes.Name",node);

		agents.clear();
		technicalFreeTime=0;
		technicalFreeTimeIsWaitingTime=true;
		score=1;
		agentScoreFreeTimeSinceLastCall=1;
		agentScoreFreeTimePart=0;
		callerMinWaitingTimeName.clear();
		callerMinWaitingTime.clear();
		efficiencyPerInterval=null;
		additionPerInterval=null;

		NodeList l=node.getChildNodes();
		for (int i=0; i<l.getLength();i++) {
			if (!(l.item(i) instanceof Element)) continue;
			Element e=(Element)l.item(i);
			String s=e.getNodeName();

			if (Language.trAll("XML.Model.AgentsGroup",s)) {
				CallcenterModelAgent a=new CallcenterModelAgent();
				String t=a.loadFromXML(e); if (t!=null) return t+String.format(Language.tr("XML.Model.AgentsGroup.Error"),agents.size()+1);
				agents.add(a); continue;
			}
			if (Language.trAll("XML.Model.CallCenter.TechnicalFreeTime",s)) {
				b=Language.trAllAttribute("XML.Model.CallCenter.TechnicalFreeTime.IsWaitingTime",e);
				if (Language.trAll("XML.General.BoolFalse",b)) technicalFreeTimeIsWaitingTime=false;
				Integer J=NumberTools.getNotNegativeInteger(e.getTextContent());
				if (J==null) return String.format(Language.tr("XML.Model.CallCenter.TechnicalFreeTime.Error"),e.getTextContent());
				technicalFreeTime=J; continue;
			}
			if (Language.trAll("XML.Model.CallCenter.Score",s)) {
				Integer J=NumberTools.getNotNegativeInteger(e.getTextContent());
				if (J==null) return String.format(Language.tr("XML.Model.CallCenter.Score.Error"),e.getTextContent());
				score=J; continue;
			}
			if (Language.trAll("XML.Model.CallCenter.AgentsScore",s)) {
				NodeList l2=e.getChildNodes();
				for (int j=0; j<l2.getLength();j++) {
					if (!(l2.item(j) instanceof Element)) continue;
					Element e2=(Element)l2.item(j);
					String t=e2.getNodeName();
					if (Language.trAll("XML.Model.CallCenter.AgentsScore.FactorSinceLastCall",t)) {
						Double D=NumberTools.getNotNegativeDouble(NumberTools.systemNumberToLocalNumber(e2.getTextContent()));
						if (D==null) return String.format(Language.tr("XML.Model.CallCenter.AgentsScore.FactorSinceLastCall.Error"),e2.getTextContent());
						agentScoreFreeTimeSinceLastCall=D; continue;
					}
					if (Language.trAll("XML.Model.CallCenter.AgentsScore.FactorFreeTimePart",t)) {
						Double D=NumberTools.getNotNegativeDouble(NumberTools.systemNumberToLocalNumber(e2.getTextContent()));
						if (D==null) return String.format(Language.tr("XML.Model.CallCenter.AgentsScore.FactorFreeTimePart.Error"),e2.getTextContent());
						agentScoreFreeTimePart=D; continue;
					}
				}
			}
			if (Language.trAll("XML.Model.CallCenter.MinimumWaitingTime",s)) {
				NodeList l2=e.getChildNodes();
				for (int j=0; j<l2.getLength();j++) {
					if (!(l2.item(j) instanceof Element)) continue;
					Element e2=(Element)l2.item(j);
					if (!Language.trAll("XML.Model.CallCenter.MinimumWaitingTime.ClientType",e2.getNodeName())) continue;
					Integer K=NumberTools.getNotNegativeInteger(e2.getTextContent());
					if (K==null) return String.format(Language.tr("XML.Model.CallCenter.MinimumWaitingTime.ClientType.Error"),e2.getTextContent());
					callerMinWaitingTimeName.add(Language.trAllAttribute("XML.Model.GeneralAttributes.Name",e2));
					callerMinWaitingTime.add(K);
				}
				continue;
			}
			if (Language.trAll("XML.Model.Productivity",s)) {
				DataDistributionImpl newEfficiencyPerInterval=DataDistributionImpl.createFromString(e.getTextContent(),CallcenterModelAgent.countPerIntervalMaxX);
				if (newEfficiencyPerInterval==null) return Language.tr("XML.Model.Productivity.ErrorCallCenter");
				efficiencyPerInterval=newEfficiencyPerInterval;
				continue;
			}
			if (Language.trAll("XML.Model.Surcharge",s)) {
				DataDistributionImpl newAdditionPerInterval=DataDistributionImpl.createFromString(e.getTextContent(),CallcenterModelAgent.countPerIntervalMaxX);
				if (newAdditionPerInterval==null) return Language.tr("XML.Model.Surcharge.ErrorCallCenter");
				additionPerInterval=newAdditionPerInterval;
				continue;
			}
		}
		return null;
	}

	/**
	 * Erstellt unterhalb des übergebenen XML-Knotens einen neuen Knoten, der die gesamten Callcenter-Daten enthält.
	 * @param parent	Eltern-XML-Knoten
	 */
	public void saveToXML(Element parent) {
		Document doc=parent.getOwnerDocument();
		Element node=doc.createElement(Language.trPrimary("XML.Model.CallCenter")); parent.appendChild(node);
		if (!active) node.setAttribute(Language.trPrimary("XML.Model.GeneralAttributes.Active"),Language.trPrimary("XML.General.BoolFalse"));
		node.setAttribute(Language.trPrimary("XML.Model.GeneralAttributes.Name"),name);
		Element e,e2;

		for (int i=0;i<agents.size();i++) agents.get(i).saveToXML(node);
		node.appendChild(e=doc.createElement(Language.trPrimary("XML.Model.CallCenter.TechnicalFreeTime"))); e.setTextContent(""+technicalFreeTime);
		if (!technicalFreeTimeIsWaitingTime) e.setAttribute(Language.trPrimary("XML.Model.CallCenter.TechnicalFreeTime.IsWaitingTime"),Language.trPrimary("XML.General.BoolFalse"));
		node.appendChild(e=doc.createElement(Language.trPrimary("XML.Model.CallCenter.Score"))); e.setTextContent(""+score);
		node.appendChild(e=doc.createElement(Language.trPrimary("XML.Model.CallCenter.AgentsScore")));
		e.appendChild(e2=doc.createElement(Language.trPrimary("XML.Model.CallCenter.AgentsScore.FactorSinceLastCall"))); e2.setTextContent(NumberTools.formatSystemNumber(agentScoreFreeTimeSinceLastCall));
		e.appendChild(e2=doc.createElement(Language.trPrimary("XML.Model.CallCenter.AgentsScore.FactorFreeTimePart"))); e2.setTextContent(NumberTools.formatSystemNumber(agentScoreFreeTimePart));
		if (callerMinWaitingTimeName.size()>0) {
			node.appendChild(e=doc.createElement(Language.trPrimary("XML.Model.CallCenter.MinimumWaitingTime")));
			for (int i=0;i<callerMinWaitingTimeName.size();i++) {
				e.appendChild(e2=doc.createElement(Language.trPrimary("XML.Model.CallCenter.MinimumWaitingTime.ClientType")));
				e2.setAttribute(Language.trPrimary("XML.Model.GeneralAttributes.Name"),callerMinWaitingTimeName.get(i));
				e2.setTextContent(""+callerMinWaitingTime.get(i));
			}
		}
		if (efficiencyPerInterval!=null) {
			node.appendChild(e=doc.createElement(Language.trPrimary("XML.Model.Productivity"))); e.setTextContent(efficiencyPerInterval.storeToString());
		}
		if (additionPerInterval!=null) {
			node.appendChild(e=doc.createElement(Language.trPrimary("XML.Model.Surcharge"))); e.setTextContent(additionPerInterval.storeToString());
		}
	}
}
