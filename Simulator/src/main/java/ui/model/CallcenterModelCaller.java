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
import org.apache.commons.math3.distribution.ExponentialDistribution;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;

import language.Language;
import mathtools.NumberTools;
import mathtools.TimeTools;
import mathtools.distribution.DataDistributionImpl;
import mathtools.distribution.tools.DistributionTools;

/**
 * Diese Klasse speichert alle Daten zu einem Kundentyp.<br>
 * Sie wird als Teil der Klasse <code>CallcenterModel</code> verwendet.
 * @author Alexander Herzog
 * @version 1.0
 * @see CallcenterModel
 */
public final class CallcenterModelCaller implements Cloneable {
	/** Maximaler (Sekunden-)Wert für die Skalierung der Werte der Verteilungen über den Tag */
	public static final int freshCallsDistMaxX=86399;
	/** Maximaler (Sekunden-)Wert für die Skalierung der Wartezeiten (im Normalmodus) */
	public static final int waitingTimeDistMaxX=3600;
	/** Maximaler (Sekunden-)Wert für die Skalierung der Wartezeiten (im Lang-Modus) */
	public static final int waitingTimeDistLongMaxX=10*86400;
	/** Maximaler (Sekunden-)Wert für die Skalierung Wiederholabstände */
	public static final int retryTimeDistMaxX=7200;
	/** Maximaler (Sekunden-)Wert für die Skalierung Wiederanrufabstände */
	public static final int recallTimeDistMaxX=7200;

	/** Name des Kundentyps */
	public String name;

	/** Kundentyp aktiv? */
	public boolean active;

	/** Mittelwert der Anzahl an Kunden dieses Typs pro Tag */
	public int freshCallsCountMean;
	/** Standardabweichung der Anzahl an Kunden dieses Typs pro Tag */
	public double freshCallsCountSD;
	/** Verteilung der Kundenankünfte über den Tag (bei 24 Intervallen pro Tag; kann auch <code>null</code> sein) */
	public DataDistributionImpl freshCallsDist24;
	/** Verteilung der Kundenankünfte über den Tag (bei 48 Intervallen pro Tag; kann auch <code>null</code> sein) */
	public DataDistributionImpl freshCallsDist48;
	/** Verteilung der Kundenankünfte über den Tag (bei 96 Intervallen pro Tag; kann auch <code>null</code> sein) */
	public DataDistributionImpl freshCallsDist96;

	/** Score-Basis für diesen Kundentyp */
	public double scoreBase;
	/** Erhöhung des Scores pro Wartesekunde */
	public double scoreSecond;
	/** Erhöhung des Scores, wenn es sich um weitergeleitete Kunden handelt */
	public double scoreContinued;

	/** Belegt ein Kunde dieses Typs eine Telefonleitung? */
	public boolean blocksLine;

	/** Zuverwendende Anzahl an Sekunden für den Service-Level (oder -1 für model.serviceLevelSeconds) */
	public short serviceLevelSeconds;

	/**
	 * Kunden sind bereit, beliebig lange zu warten.
	 * @see #waitingTimeMode
	 */
	public static final byte WAITING_TIME_MODE_OFF=0;

	/**
	 * Wartezeittoleranzverteilung gemäß <code>waitingTimeDist</code>
	 * @see #waitingTimeMode
	 * @see #waitingTimeDist
	 */
	public static final byte WAITING_TIME_MODE_SHORT=1;

	/**
	 * Wartezeittoleranzverteilung gemäß <code>waitingTimeDistLong</code>
	 * @see #waitingTimeMode
	 * @see #waitingTimeDistLong
	 */
	public static final byte WAITING_TIME_MODE_LONG=2;

	/**
	 * Wartezeittoleranz nicht per Verteilung vorgeben, sondern aus mittlerer Wartezeit und Abbruchwahrscheinlichkeit schätzen
	 * @see #waitingTimeMode
	 * @see #waitingTimeCalcMeanWaitingTime
	 * @see #waitingTimeCalcCancelProbability
	 * @see #waitingTimeCalcAdd
	 */
	public static final byte WAITING_TIME_MODE_CALC=3;

	/** Ist die Wartezeittoleranz aktiv oder warten die Kunden beliebig lang? (siehe <code>WAITING_TIME_MODE_*</code>-Konstanten) */
	public byte waitingTimeMode;

	/** Wartezeittoleranzverteilung (kurze Wartezeittoleranz) */
	public AbstractRealDistribution waitingTimeDist;

	/** Wartezeittoleranzverteilung (lange Wartezeittoleranz) */
	public AbstractRealDistribution waitingTimeDistLong;

	/** Gemessene mittlere Wartezeit zur Schätzung der Wartezeittoleranz */
	public double waitingTimeCalcMeanWaitingTime;
	/** Gemessene Abbruchwahrscheinlichkeit zur Schätzung der Wartezeittoleranz */
	public double waitingTimeCalcCancelProbability;
	/** Korrekturterm bei der Schätzung der Wartezeittoleranz */
	public double waitingTimeCalcAdd;

	/** Wiederholabständeverteilung */
	public AbstractRealDistribution retryTimeDist;

	/** Wiederholrate nach "besetzt" - erster Versuch */
	public double retryProbabiltyAfterBlockedFirstRetry;
	/** Wiederholrate nach "besetzt" - ab dem zweiten Versuch */
	public double retryProbabiltyAfterBlocked;
	/** Wiederholrate nach Warteabbruch - erster Versuch */
	public double retryProbabiltyAfterGiveUpFirstRetry;
	/** Wiederholrate nach Warteabbruch - ab dem zweiten Versuch */
	public double retryProbabiltyAfterGiveUp;

	/** Kundentypenwechsel nach "besetzt" - erster Versuch (Kundentypen) */
	public List<String> retryCallerTypeAfterBlockedFirstRetry;
	/** Kundentypenwechsel nach "besetzt" - erster Versuch (Raten) */
	public List<Double> retryCallerTypeRateAfterBlockedFirstRetry;
	/** Kundentypenwechsel nach "besetzt" - ab dem zweiten Versuch (Kundentypen) */
	public List<String> retryCallerTypeAfterBlocked;
	/** Kundentypenwechsel nach "besetzt" - ab dem zweiten Versuch (Raten) */
	public List<Double> retryCallerTypeRateAfterBlocked;
	/** Kundentypenwechsel nach Warteabbruch - erster Versuch (Kundentypen) */
	public List<String> retryCallerTypeAfterGiveUpFirstRetry;
	/** Kundentypenwechsel nach Warteabbruch - erster Versuch (Raten) */
	public List<Double> retryCallerTypeRateAfterGiveUpFirstRetry;
	/** Kundentypenwechsel nach Warteabbruch - ab dem zweiten Versuch (Kundentypen) */
	public List<String> retryCallerTypeAfterGiveUp;
	/** Kundentypenwechsel nach Warteabbruch - ab dem zweiten Versuch (Raten) */
	public List<Double> retryCallerTypeRateAfterGiveUp;

	/** Weiterleitungswahrscheinlichkeit */
	public double continueProbability;

	/** Liste der Namen der möglichen neuen Kundentypen bei einer Weiterleitung */
	public List<String> continueTypeName;
	/** Liste der Raten, mit denen Weiterleitungen zu den bestimmten Kundentypen erfolgen */
	public List<Double> continueTypeRate;

	/** Spezielle Weiterleitungsmöglichkeiten in Abhängigkeit vom Skill-Level des bedienenden Agenten */
	public List<String> continueTypeSkillType;
	/** Wahrscheinlichkeit in Abhängigkeit vom Skill-Level des bedienenden Agenten */
	public List<Double> continueTypeSkillTypeProbability;
	/** Liste der Namen der möglichen neuen Kundentypen bei einer Weiterleitung in Abhängigkeit vom Skill-Level des bedienenden Agenten */
	public List<List<String>> continueTypeSkillTypeName;
	/** Liste der Raten, mit denen Weiterleitungen zu den bestimmten Kundentypen erfolgen in Abhängigkeit vom Skill-Level des bedienenden Agenten */
	public List<List<Double>> continueTypeSkillTypeRate;

	/** Wiederanrufwahrscheinlichkeit **/
	public double recallProbability;
	/** Wiederanrufabständeverteilung */
	public AbstractRealDistribution recallTimeDist;

	/** Liste der Namen der möglichen neuen Kundentypen bei einem Wiederanruf */
	public List<String> recallTypeName;
	/** Liste der Raten, mit denen Wiederanrufer ihren Kundentyp wechseln */
	public List<Double> recallTypeRate;

	/** Spezielle Wiederanrufmöglichkeiten in Abhängigkeit vom Skill-Level des bedienenden Agenten */
	public List<String> recallTypeSkillType;
	/** Wahrscheinlichkeit in Abhängigkeit vom Skill-Level des bedienenden Agenten */
	public List<Double> recallTypeSkillTypeProbability;
	/** Liste der Namen der möglichen neuen Kundentypen bei einem Wiederanruf in Abhängigkeit vom Skill-Level des bedienenden Agenten */
	public List<List<String>> recallTypeSkillTypeName;
	/** Liste der Raten, mit denen Wiederanrufe zu den bestimmten Kundentypen erfolgen in Abhängigkeit vom Skill-Level des bedienenden Agenten */
	public List<List<Double>> recallTypeSkillTypeRate;

	/** Ertrag pro erfolgreich bedientem Kunden */
	public double revenuePerClient;
	/** Virtuelle Kosten pro Warteabbruch */
	public double costPerCancel;
	/** Virtuelle Kosten pro Wartesekunde */
	public double costPerWaitingSec;

	/** Konstruktor der Klasse <code>CallcenterModelCaller</code>
	 * @param name Name der Kundengruppe
	 */
	public CallcenterModelCaller(String name) {
		this.name=name;

		active=true;

		freshCallsCountMean=1;
		freshCallsCountSD=0;
		freshCallsDist24=null;
		freshCallsDist48=new DataDistributionImpl(freshCallsDistMaxX,48);
		freshCallsDist96=null;

		scoreBase=1;
		scoreSecond=1;
		scoreContinued=0;

		blocksLine=true;

		serviceLevelSeconds=-1;

		waitingTimeMode=WAITING_TIME_MODE_SHORT;
		waitingTimeDist=new ExponentialDistribution(null,300,ExponentialDistribution.DEFAULT_INVERSE_ABSOLUTE_ACCURACY);
		waitingTimeDistLong=new ExponentialDistribution(null,2*86400,ExponentialDistribution.DEFAULT_INVERSE_ABSOLUTE_ACCURACY);
		waitingTimeCalcMeanWaitingTime=15;
		waitingTimeCalcCancelProbability=0.05;
		waitingTimeCalcAdd=0;

		retryTimeDist=new ExponentialDistribution(null,1200,ExponentialDistribution.DEFAULT_INVERSE_ABSOLUTE_ACCURACY);

		retryProbabiltyAfterBlockedFirstRetry=0.9;
		retryProbabiltyAfterBlocked=0.8;
		retryProbabiltyAfterGiveUpFirstRetry=0.9;
		retryProbabiltyAfterGiveUp=0.8;

		retryCallerTypeAfterBlockedFirstRetry=new ArrayList<String>();
		retryCallerTypeRateAfterBlockedFirstRetry=new ArrayList<Double>();
		retryCallerTypeAfterBlocked=new ArrayList<String>();
		retryCallerTypeRateAfterBlocked=new ArrayList<Double>();
		retryCallerTypeAfterGiveUpFirstRetry=new ArrayList<String>();
		retryCallerTypeRateAfterGiveUpFirstRetry=new ArrayList<Double>();
		retryCallerTypeAfterGiveUp=new ArrayList<String>();
		retryCallerTypeRateAfterGiveUp=new ArrayList<Double>();

		continueProbability=0.2;

		continueTypeName=new ArrayList<String>();
		continueTypeRate=new ArrayList<Double>();

		continueTypeSkillType=new ArrayList<String>();
		continueTypeSkillTypeProbability=new ArrayList<Double>();
		continueTypeSkillTypeName=new ArrayList<List<String>>();
		continueTypeSkillTypeRate=new ArrayList<List<Double>>();

		recallProbability=0;
		recallTimeDist=new ExponentialDistribution(null,1800,ExponentialDistribution.DEFAULT_INVERSE_ABSOLUTE_ACCURACY);

		recallTypeName=new ArrayList<String>();
		recallTypeRate=new ArrayList<Double>();
		recallTypeSkillType=new ArrayList<String>();
		recallTypeSkillTypeProbability=new ArrayList<Double>();
		recallTypeSkillTypeName=new ArrayList<List<String>>();
		recallTypeSkillTypeRate=new ArrayList<List<Double>>();

		revenuePerClient=0;
		costPerCancel=0;
		costPerWaitingSec=0;
	}

	/** Konstruktor der Klasse <code>CallcenterModelCaller</code> */
	public CallcenterModelCaller() {
		this(Language.tr("Model.DefaultName.Clients"));
	}

	@Override
	public CallcenterModelCaller clone() {
		CallcenterModelCaller caller=new CallcenterModelCaller();

		caller.name=name;

		caller.active=active;

		caller.freshCallsCountMean=freshCallsCountMean;
		caller.freshCallsCountSD=freshCallsCountSD;
		if (freshCallsDist24!=null) caller.freshCallsDist24=freshCallsDist24.clone(); else caller.freshCallsDist24=null;
		if (freshCallsDist48!=null) caller.freshCallsDist48=freshCallsDist48.clone(); else caller.freshCallsDist48=null;
		if (freshCallsDist96!=null) caller.freshCallsDist96=freshCallsDist96.clone(); else caller.freshCallsDist96=null;

		caller.scoreBase=scoreBase;
		caller.scoreSecond=scoreSecond;
		caller.scoreContinued=scoreContinued;

		caller.blocksLine=blocksLine;

		caller.serviceLevelSeconds=serviceLevelSeconds;

		caller.waitingTimeMode=waitingTimeMode;
		caller.waitingTimeDist=DistributionTools.cloneDistribution(waitingTimeDist);
		caller.waitingTimeDistLong=DistributionTools.cloneDistribution(waitingTimeDistLong);
		caller.waitingTimeCalcMeanWaitingTime=waitingTimeCalcMeanWaitingTime;
		caller.waitingTimeCalcCancelProbability=waitingTimeCalcCancelProbability;
		caller.waitingTimeCalcAdd=waitingTimeCalcAdd;

		caller.retryTimeDist=DistributionTools.cloneDistribution(retryTimeDist);
		caller.retryProbabiltyAfterBlockedFirstRetry=retryProbabiltyAfterBlockedFirstRetry;
		caller.retryProbabiltyAfterBlocked=retryProbabiltyAfterBlocked;
		caller.retryProbabiltyAfterGiveUpFirstRetry=retryProbabiltyAfterGiveUpFirstRetry;
		caller.retryProbabiltyAfterGiveUp=retryProbabiltyAfterGiveUp;

		caller.retryCallerTypeAfterBlockedFirstRetry=new ArrayList<String>(retryCallerTypeAfterBlockedFirstRetry);
		caller.retryCallerTypeRateAfterBlockedFirstRetry=new ArrayList<Double>(retryCallerTypeRateAfterBlockedFirstRetry);
		caller.retryCallerTypeAfterBlocked=new ArrayList<String>(retryCallerTypeAfterBlocked);
		caller.retryCallerTypeRateAfterBlocked=new ArrayList<Double>(retryCallerTypeRateAfterBlocked);
		caller.retryCallerTypeAfterGiveUpFirstRetry=new ArrayList<String>(retryCallerTypeAfterGiveUpFirstRetry);
		caller.retryCallerTypeRateAfterGiveUpFirstRetry=new ArrayList<Double>(retryCallerTypeRateAfterGiveUpFirstRetry);
		caller.retryCallerTypeAfterGiveUp=new ArrayList<String>(retryCallerTypeAfterGiveUp);
		caller.retryCallerTypeRateAfterGiveUp=new ArrayList<Double>(retryCallerTypeRateAfterGiveUp);

		caller.continueProbability=continueProbability;
		caller.continueTypeName=new ArrayList<String>(continueTypeName);
		caller.continueTypeRate=new ArrayList<Double>(continueTypeRate);

		caller.continueTypeSkillType=new ArrayList<String>(continueTypeSkillType);
		caller.continueTypeSkillTypeProbability=new ArrayList<Double>(continueTypeSkillTypeProbability);
		for (int i=0;i<continueTypeSkillTypeName.size();i++) {
			List<String> list2=new ArrayList<String>(continueTypeSkillTypeName.get(i));
			caller.continueTypeSkillTypeName.add(list2);
		}
		for (int i=0;i<continueTypeSkillTypeRate.size();i++) {
			List<Double> list2=new ArrayList<Double>(continueTypeSkillTypeRate.get(i));
			caller.continueTypeSkillTypeRate.add(list2);
		}

		caller.recallProbability=recallProbability;
		caller.recallTimeDist=DistributionTools.cloneDistribution(recallTimeDist);

		caller.recallTypeName=new ArrayList<String>(recallTypeName);
		caller.recallTypeRate=new ArrayList<Double>(recallTypeRate);

		caller.recallTypeSkillType=new ArrayList<String>(recallTypeSkillType);
		caller.recallTypeSkillTypeProbability=new ArrayList<Double>(recallTypeSkillTypeProbability);
		for (int i=0;i<recallTypeSkillTypeName.size();i++) {
			List<String> list2=new ArrayList<String>(recallTypeSkillTypeName.get(i));
			caller.recallTypeSkillTypeName.add(list2);
		}
		for (int i=0;i<recallTypeSkillTypeRate.size();i++) {
			List<Double> list2=new ArrayList<Double>(recallTypeSkillTypeRate.get(i));
			caller.recallTypeSkillTypeRate.add(list2);
		}

		caller.revenuePerClient=revenuePerClient;
		caller.costPerCancel=costPerCancel;
		caller.costPerWaitingSec=costPerWaitingSec;

		return caller;
	}

	/**
	 * Vergleicht das Anrufergruppen-Objekt mit einem anderen Anrufergruppen-Objekt
	 * @param caller Anderes Anrufergruppen-Objekt
	 * @return	Liefert <code>true</code>, wenn die beiden Anrufergruppen inhaltlich identisch sind
	 */
	public boolean equalsCallcenterModelCaller(final CallcenterModelCaller caller) {
		if (caller==null) return false;

		if (!caller.name.equals(name)) return false;

		if (caller.active!=active) return false;

		if (caller.freshCallsCountMean!=freshCallsCountMean) return false;
		if (caller.freshCallsCountSD!=freshCallsCountSD) return false;
		if (freshCallsDist24!=null) {
			if (caller.freshCallsDist24==null) return false;
			if (!DistributionTools.compare(caller.freshCallsDist24,freshCallsDist24)) return false;
		}
		if (freshCallsDist48!=null) {
			if (caller.freshCallsDist48==null) return false;
			if (!DistributionTools.compare(caller.freshCallsDist48,freshCallsDist48)) return false;
		}
		if (freshCallsDist96!=null) {
			if (caller.freshCallsDist96==null) return false;
			if (!DistributionTools.compare(caller.freshCallsDist96,freshCallsDist96)) return false;
		}

		if (caller.scoreBase!=scoreBase) return false;
		if (caller.scoreSecond!=scoreSecond) return false;
		if (caller.scoreContinued!=scoreContinued) return false;

		if (caller.blocksLine!=blocksLine) return false;

		if (caller.serviceLevelSeconds!=serviceLevelSeconds) return false;

		if (caller.waitingTimeMode!=waitingTimeMode) return false;
		switch (waitingTimeMode) {
		case WAITING_TIME_MODE_SHORT:
			if (!DistributionTools.compare(caller.waitingTimeDist,waitingTimeDist)) return false;
			break;
		case WAITING_TIME_MODE_LONG:
			if (!DistributionTools.compare(caller.waitingTimeDistLong,waitingTimeDistLong)) return false;
			break;
		case WAITING_TIME_MODE_CALC:
			if (caller.waitingTimeCalcMeanWaitingTime!=waitingTimeCalcMeanWaitingTime) return false;
			if (caller.waitingTimeCalcCancelProbability!=waitingTimeCalcCancelProbability) return false;
			if (caller.waitingTimeCalcAdd!=waitingTimeCalcAdd) return false;
			break;
		}

		if (!DistributionTools.compare(caller.retryTimeDist,retryTimeDist)) return false;
		if (caller.retryProbabiltyAfterBlockedFirstRetry!=retryProbabiltyAfterBlockedFirstRetry) return false;
		if (caller.retryProbabiltyAfterBlocked!=retryProbabiltyAfterBlocked) return false;
		if (caller.retryProbabiltyAfterGiveUpFirstRetry!=retryProbabiltyAfterGiveUpFirstRetry) return false;
		if (caller.retryProbabiltyAfterGiveUp!=retryProbabiltyAfterGiveUp) return false;
		if (!caller.retryCallerTypeAfterBlockedFirstRetry.equals(retryCallerTypeAfterBlockedFirstRetry)) return false;
		if (!caller.retryCallerTypeRateAfterBlockedFirstRetry.equals(retryCallerTypeRateAfterBlockedFirstRetry)) return false;
		if (!caller.retryCallerTypeAfterBlocked.equals(retryCallerTypeAfterBlocked)) return false;
		if (!caller.retryCallerTypeRateAfterBlocked.equals(retryCallerTypeRateAfterBlocked)) return false;
		if (!caller.retryCallerTypeAfterGiveUpFirstRetry.equals(retryCallerTypeAfterGiveUpFirstRetry)) return false;
		if (!caller.retryCallerTypeRateAfterGiveUpFirstRetry.equals(retryCallerTypeRateAfterGiveUpFirstRetry)) return false;
		if (!caller.retryCallerTypeAfterGiveUp.equals(retryCallerTypeAfterGiveUp)) return false;
		if (!caller.retryCallerTypeRateAfterGiveUp.equals(retryCallerTypeRateAfterGiveUp)) return false;

		if (caller.continueProbability!=continueProbability) return false;
		if (!caller.continueTypeName.equals(continueTypeName)) return false;
		if (!caller.continueTypeRate.equals(continueTypeRate)) return false;

		if (caller.continueTypeSkillType.size()!=continueTypeSkillType.size()) return false;
		if (caller.continueTypeSkillTypeProbability.size()!=continueTypeSkillTypeProbability.size()) return false;
		if (caller.continueTypeSkillTypeName.size()!=continueTypeSkillTypeName.size()) return false;
		if (caller.continueTypeSkillTypeRate.size()!=continueTypeSkillTypeRate.size()) return false;
		for (int i=0;i<continueTypeSkillType.size();i++) {
			if (!caller.continueTypeSkillType.get(i).equals(continueTypeSkillType.get(i))) return false;
			if (!caller.continueTypeSkillTypeProbability.get(i).equals(continueTypeSkillTypeProbability.get(i))) return false;
			List<String> names1=caller.continueTypeSkillTypeName.get(i);
			List<String> names2=continueTypeSkillTypeName.get(i);
			if (!names1.equals(names2)) return false;
			List<Double> rates1=caller.continueTypeSkillTypeRate.get(i);
			List<Double> rates2=continueTypeSkillTypeRate.get(i);
			if (!rates1.equals(rates2)) return false;
		}

		if (caller.recallProbability!=recallProbability) return false;
		if (!DistributionTools.compare(caller.recallTimeDist,recallTimeDist)) return false;
		if (caller.recallTypeName.size()!=recallTypeName.size()) return false;
		if (caller.recallTypeRate.size()!=recallTypeRate.size()) return false;
		if (caller.recallTypeSkillType.size()!=recallTypeSkillType.size()) return false;
		if (caller.recallTypeSkillTypeProbability.size()!=recallTypeSkillTypeProbability.size()) return false;
		if (caller.recallTypeSkillTypeName.size()!=recallTypeSkillTypeName.size()) return false;
		if (caller.recallTypeSkillTypeRate.size()!=recallTypeSkillTypeRate.size()) return false;
		for (int i=0;i<recallTypeName.size();i++) {
			if (!caller.recallTypeName.get(i).equals(recallTypeName.get(i))) return false;
			if (!caller.recallTypeRate.get(i).equals(recallTypeRate.get(i))) return false;
		}
		for (int i=0;i<recallTypeSkillType.size();i++) {
			if (!caller.recallTypeSkillType.get(i).equals(recallTypeSkillType.get(i))) return false;
			if (caller.recallTypeSkillTypeProbability.get(i)!=recallTypeSkillTypeProbability.get(i)) return false;
			List<String> names1=caller.recallTypeSkillTypeName.get(i);
			List<String> names2=recallTypeSkillTypeName.get(i);
			if (!names1.equals(names2)) return false;
			List<Double> rates1=caller.recallTypeSkillTypeRate.get(i);
			List<Double> rates2=recallTypeSkillTypeRate.get(i);
			if (!rates1.equals(rates2)) return false;
		}

		if (caller.revenuePerClient!=revenuePerClient) return false;
		if (caller.costPerCancel!=costPerCancel) return false;
		if (caller.costPerWaitingSec!=costPerWaitingSec) return false;

		return true;
	}

	/**
	 * Ergänzt optionale Daten (wie es auch die Bearbeiten-Dialoge machen),
	 * um unnötige Starts der Hintergrunsimulation bedingt durch nur scheinbar
	 * im Dialog veränderte Modell zu vermeiden.
	 * @param model	Modell das ergänzt werden soll
	 */
	public void prepareData(CallcenterModel model) {
		/* Kundentypenänderungen ggf. mit 0 vorbelegen */
		for (int i=0;i<model.caller.size();i++) {
			String name=model.caller.get(i).name;
			if (retryCallerTypeAfterBlockedFirstRetry.indexOf(name)<0) {
				retryCallerTypeAfterBlockedFirstRetry.add(name);
				retryCallerTypeRateAfterBlockedFirstRetry.add(0.0);
			}
			if (retryCallerTypeAfterBlocked.indexOf(name)<0) {
				retryCallerTypeAfterBlocked.add(name);
				retryCallerTypeRateAfterBlocked.add(0.0);
			}
			if (retryCallerTypeAfterGiveUpFirstRetry.indexOf(name)<0) {
				retryCallerTypeAfterGiveUpFirstRetry.add(name);
				retryCallerTypeRateAfterGiveUpFirstRetry.add(0.0);
			}
			if (retryCallerTypeAfterGiveUp.indexOf(name)<0) {
				retryCallerTypeAfterGiveUp.add(name);
				retryCallerTypeRateAfterGiveUp.add(0.0);
			}
		}

		/* Weiterleitungsraten für Kundentypen, zu denen keine Weiterleitungen definiert sind, mit 0 belegen. */
		for (int i=0;i<model.caller.size();i++) {
			String name=model.caller.get(i).name;
			if (continueTypeName.indexOf(name)<0) {
				continueTypeName.add(name);
				continueTypeRate.add(0.0);
			}
		}

		/* Wiederanrufraten für Kundentypen, zu denen keine Kundentypänderungen definiert sind, mit 0 belegen. */
		for (int i=0;i<model.caller.size();i++) {
			String name=model.caller.get(i).name;
			if (recallTypeName.indexOf(name)<0) {
				recallTypeName.add(name);
				recallTypeRate.add(0.0);
			}
		}
	}

	/**
	 * Lädt Daten zu Kundentypänderungen bei einer Wiederholung aus einem XML-Element
	 * @param node	XML-Knoten aus dem die Daten geladen werden sollen
	 * @param names	Liste mit den neuen Kundentypen
	 * @param rates	Liste mit den Raten, die zu den Kundentypen gehören
	 * @return	Liefert im Erfolgsfall <code>null</code>, sonst eine Fehlermeldung
	 * @see #loadFromXML(Element)
	 */
	private String loadRetryCallerTypeChangeData(Element node, List<String> names, List<Double> rates) {
		NodeList l=node.getChildNodes();
		for (int i=0; i<l.getLength();i++) {
			if (!(l.item(i) instanceof Element)) continue;
			Element e=(Element)l.item(i);
			if (!Language.trAll("XML.Model.ClientType.Retry.ClientType.NewClientType",e.getNodeName())) continue;
			Double D=NumberTools.getNotNegativeDouble(NumberTools.systemNumberToLocalNumber(e.getTextContent()));
			String name=Language.trAllAttribute("XML.Model.GeneralAttributes.Name",e);
			if (D==null) return String.format(Language.tr("XML.Model.ClientType.Retry.ClientType.NewClientType.Error"),e.getTextContent(),name);
			names.add(name);
			rates.add(D);
		}
		return null;
	}

	/**
	 * Versucht einen Kundentyp aus dem übergebenen XML-Node zu laden
	 * @param node	XML-Knoten, der die Kundentyp-Daten enthält
	 * @return	Tritt ein Fehler auf, so wird die Fehlermeldung als String zurückgegeben. Im Erfolgsfall wird <code>null</code> zurückgegeben.
	 */
	public String loadFromXML(Element node) {
		active=true;
		blocksLine=true;
		serviceLevelSeconds=-1;

		name=Language.trAllAttribute("XML.Model.GeneralAttributes.Name",node);

		String a=Language.trAllAttribute("XML.Model.GeneralAttributes.Active",node);
		if (Language.trAll("XML.General.BoolFalse",a)) active=false;

		a=Language.trAllAttribute("XML.Model.ClientType.BlocksLine",node);
		if (Language.trAll("XML.General.BoolFalse",a)) blocksLine=false;

		a=Language.trAllAttribute("XML.Model.ClientType.ServiceLevel",node);
		if (a!=null && !a.isEmpty())  {
			Short SH=NumberTools.getNotNegativeShort(a);
			if (SH==null) return String.format(Language.tr("XML.Model.ClientType.ServiceLevel.Error"),a);
			serviceLevelSeconds=SH;
		}

		freshCallsCountMean=1;
		freshCallsCountSD=0;
		freshCallsDist24=null;
		freshCallsDist48=null;
		freshCallsDist96=null;
		scoreBase=1;
		scoreSecond=0;
		scoreContinued=0;
		waitingTimeMode=WAITING_TIME_MODE_OFF; /* Keine Ungeduld, falls kein XML-Node dazu existiert. */
		waitingTimeDist=new ExponentialDistribution(null,300,ExponentialDistribution.DEFAULT_INVERSE_ABSOLUTE_ACCURACY);
		waitingTimeDistLong=new ExponentialDistribution(null,2*86400,ExponentialDistribution.DEFAULT_INVERSE_ABSOLUTE_ACCURACY);
		waitingTimeCalcMeanWaitingTime=15;
		waitingTimeCalcCancelProbability=0.05;
		waitingTimeCalcAdd=0;
		retryTimeDist=new ExponentialDistribution(null,1200,ExponentialDistribution.DEFAULT_INVERSE_ABSOLUTE_ACCURACY);
		retryProbabiltyAfterBlockedFirstRetry=0.9;
		retryProbabiltyAfterBlocked=0.8;
		retryProbabiltyAfterGiveUpFirstRetry=0.9;
		retryProbabiltyAfterGiveUp=0.8;
		retryCallerTypeAfterBlockedFirstRetry.clear();
		retryCallerTypeRateAfterBlockedFirstRetry.clear();
		retryCallerTypeAfterBlocked.clear();
		retryCallerTypeRateAfterBlocked.clear();
		retryCallerTypeAfterGiveUpFirstRetry.clear();
		retryCallerTypeRateAfterGiveUpFirstRetry.clear();
		retryCallerTypeAfterGiveUp.clear();
		retryCallerTypeRateAfterGiveUp.clear();
		continueProbability=0.2;
		continueTypeName.clear();
		continueTypeRate.clear();
		continueTypeSkillType.clear();
		continueTypeSkillTypeProbability.clear();
		continueTypeSkillTypeName.clear();
		continueTypeSkillTypeRate.clear();
		recallProbability=0;
		recallTimeDist=new ExponentialDistribution(null,1800,ExponentialDistribution.DEFAULT_INVERSE_ABSOLUTE_ACCURACY);
		recallTypeName.clear();
		recallTypeRate.clear();
		recallTypeSkillType.clear();
		recallTypeSkillTypeProbability.clear();
		recallTypeSkillTypeName.clear();
		recallTypeSkillTypeRate.clear();
		revenuePerClient=0;
		costPerCancel=0;
		costPerWaitingSec=0;

		NodeList l=node.getChildNodes();
		for (int i=0; i<l.getLength();i++) {
			if (!(l.item(i) instanceof Element)) continue;
			Element e=(Element)l.item(i);
			String s=e.getNodeName();

			if (Language.trAll("XML.Model.ClientType.FreshCalls",s)) {
				NodeList l2=e.getChildNodes();
				for (int j=0; j<l2.getLength();j++) {
					if (!(l2.item(j) instanceof Element)) continue;
					Element e2=(Element)l2.item(j);
					String t=e2.getNodeName();

					if (Language.trAll("XML.Model.ClientType.FreshCalls.Count",t)) {
						Integer K=NumberTools.getNotNegativeInteger(e2.getTextContent());
						if (K==null) return String.format(Language.tr("XML.Model.ClientType.FreshCalls.Count.Error"),e2.getTextContent());
						freshCallsCountMean=K;

						String u=Language.trAllAttribute("XML.Model.ClientType.FreshCalls.StandardDeviation",e2);
						if (!u.isEmpty()) {
							Double L=NumberTools.getNotNegativeDouble(NumberTools.systemNumberToLocalNumber(u));
							if (L==null) return String.format(Language.tr("XML.Model.ClientType.FreshCalls.StandardDeviation.Error"),u);
							freshCallsCountSD=L;
						}

						continue;
					}
					if (Language.trAll("XML.Model.ClientType.FreshCalls.Distribution",t)) {
						DataDistributionImpl dist=DataDistributionImpl.createFromString(e2.getTextContent(),freshCallsDistMaxX);

						if (dist!=null && dist.densityData.length<24) {dist.stretchToValueCount(24); freshCallsDist24=dist; dist=null;}
						if (dist!=null && dist.densityData.length==24) {freshCallsDist24=dist; dist=null;}
						if (dist!=null && dist.densityData.length<48) {dist.stretchToValueCount(48); freshCallsDist48=dist; dist=null;}
						if (dist!=null && dist.densityData.length==48) {freshCallsDist48=dist; dist=null;}
						if (dist!=null && dist.densityData.length<96) {dist.stretchToValueCount(96); freshCallsDist96=dist; dist=null;}
						if (dist!=null && dist.densityData.length==96) {freshCallsDist96=dist; dist=null;}
						if (dist!=null) {dist.stretchToValueCount(96); freshCallsDist96=dist; dist=null;}

						if (freshCallsDist24==null && freshCallsDist48==null && freshCallsDist96==null) return Language.tr("XML.Model.ClientType.FreshCalls.Distribution.Error");
						continue;
					}
				}
				continue;
			}

			if (Language.trAll("XML.Model.ClientType.ClientsScore",s)) {
				String u;
				u=Language.trAllAttribute("XML.Model.ClientType.ClientsScore.Base",e); if (!u.isEmpty()) {
					Double D=NumberTools.getDouble(u);
					if (D==null) return String.format(Language.tr("XML.Model.ClientType.ClientsScore.Base.Error"),u); else scoreBase=D;
				}
				u=Language.trAllAttribute("XML.Model.ClientType.ClientsScore.PerWaitingSecond",e); if (!u.isEmpty()) {
					Double D=NumberTools.getDouble(u);
					if (D==null) return String.format(Language.tr("XML.Model.ClientType.ClientsScore.PerWaitingSecond.Error"),u); else scoreSecond=D;
				}
				u=Language.trAllAttribute("XML.Model.ClientType.ClientsScore.Forwarding",e); if (!u.isEmpty()) {
					Double D=NumberTools.getDouble(u);
					if (D==null) return String.format(Language.tr("XML.Model.ClientType.ClientsScore.Forwarding.Error"),u); else scoreContinued=D;
				}
				continue;
			}

			if (Language.trAll("XML.Model.ClientType.WaitingTimeTolerance.Normal",s)) {
				waitingTimeMode=WAITING_TIME_MODE_SHORT;
				waitingTimeDist=DistributionTools.distributionFromString(e.getTextContent(),waitingTimeDistMaxX);
				if (waitingTimeDist==null) return String.format(Language.tr("XML.Model.ClientType.WaitingTimeTolerance.Error"),e.getTextContent());
				continue;
			}

			if (Language.trAll("XML.Model.ClientType.WaitingTimeTolerance.Long",s)) {
				waitingTimeMode=WAITING_TIME_MODE_LONG;
				waitingTimeDistLong=DistributionTools.distributionFromString(e.getTextContent(),waitingTimeDistLongMaxX);
				if (waitingTimeDistLong==null) return String.format(Language.tr("XML.Model.ClientType.WaitingTimeTolerance.Error"),e.getTextContent());
				continue;
			}

			if (Language.trAll("XML.Model.ClientType.WaitingTimeTolerance.Estimation",s)) {
				waitingTimeMode=WAITING_TIME_MODE_CALC;
				String t=Language.trAllAttribute("XML.Model.ClientType.WaitingTimeTolerance.Estimation.WaitingTime",e);
				if (t==null || t.isEmpty()) return Language.tr("XML.Model.ClientType.WaitingTimeTolerance.Estimation.WaitingTime.ErrorNo");
				Double d=TimeTools.getExactTime(t); if (d==null || d<0) return String.format(Language.tr("XML.Model.ClientType.WaitingTimeTolerance.Estimation.WaitingTime.Error"),t);
				waitingTimeCalcMeanWaitingTime=d;
				t=Language.trAllAttribute("XML.Model.ClientType.WaitingTimeTolerance.Estimation.CancelRate",e);
				if (t==null || t.isEmpty()) return Language.tr("XML.Model.ClientType.WaitingTimeTolerance.Estimation.CancelRate.ErrorNo");
				d=NumberTools.getSystemProbability(t); if (d==null) return String.format(Language.tr("XML.Model.ClientType.WaitingTimeTolerance.Estimation.CancelRate.Error"),t);
				waitingTimeCalcCancelProbability=d;
				t=Language.trAllAttribute("XML.Model.ClientType.WaitingTimeTolerance.Estimation.Correction",e);
				if (t==null || t.isEmpty()) waitingTimeCalcAdd=0; {
					d=TimeTools.getExactTime(t); if (d==null) return String.format(Language.tr("XML.Model.ClientType.WaitingTimeTolerance.Estimation.Correction.Error"),t);
					waitingTimeCalcAdd=d;
				}
			}

			if (Language.trAll("XML.Model.ClientType.Retry",s)) {
				NodeList l2=e.getChildNodes();
				for (int j=0; j<l2.getLength();j++) {
					if (!(l2.item(j) instanceof Element)) continue;
					Element e2=(Element)l2.item(j);
					String t=e2.getNodeName();

					if (Language.trAll("XML.Model.ClientType.Retry.Distribution",t)) {
						retryTimeDist=DistributionTools.distributionFromString(e2.getTextContent(),retryTimeDistMaxX);
						if (retryTimeDist==null) return String.format(Language.tr("XML.Model.ClientType.Retry.Distribution.Error"),e2.getTextContent());
						continue;
					}
					if (Language.trAll("XML.Model.ClientType.Retry.Probability.BlockedFirst",t)) {
						Double D=NumberTools.getSystemProbability(e2.getTextContent());
						if (D==null) return String.format(Language.tr("XML.Model.ClientType.Retry.Probability.BlockedFirst.Error"),e2.getTextContent());
						retryProbabiltyAfterBlockedFirstRetry=D;
						continue;
					}
					if (Language.trAll("XML.Model.ClientType.Retry.Probability.Blocked",t)) {
						Double D=NumberTools.getSystemProbability(e2.getTextContent());
						if (D==null) return String.format(Language.tr("XML.Model.ClientType.Retry.Probability.Blocked.Error"),e2.getTextContent());
						retryProbabiltyAfterBlocked=D;
						continue;
					}
					if (Language.trAll("XML.Model.ClientType.Retry.Probability.CanceledFirst",t)) {
						Double D=NumberTools.getSystemProbability(e2.getTextContent());
						if (D==null) return String.format(Language.tr("XML.Model.ClientType.Retry.Probability.CanceledFirst.Error"),e2.getTextContent());
						retryProbabiltyAfterGiveUpFirstRetry=D;
						continue;
					}
					if (Language.trAll("XML.Model.ClientType.Retry.Probability.Canceled",t)) {
						Double D=NumberTools.getSystemProbability(e2.getTextContent());
						if (D==null) return String.format(Language.tr("XML.Model.ClientType.Retry.Probability.Canceled.Error"),e2.getTextContent());
						retryProbabiltyAfterGiveUp=D;
						continue;
					}
					if (Language.trAll("XML.Model.ClientType.Retry.ClientType.BlockedFirst",t)) {
						String u=loadRetryCallerTypeChangeData(e2,retryCallerTypeAfterBlockedFirstRetry,retryCallerTypeRateAfterBlockedFirstRetry);
						if (u!=null) return s;
						continue;
					}
					if (Language.trAll("XML.Model.ClientType.Retry.ClientType.Blocked",t)) {
						String u=loadRetryCallerTypeChangeData(e2,retryCallerTypeAfterBlocked,retryCallerTypeRateAfterBlocked);
						if (u!=null) return s;
						continue;
					}
					if (Language.trAll("XML.Model.ClientType.Retry.ClientType.CanceledFirst",t)) {
						String u=loadRetryCallerTypeChangeData(e2,retryCallerTypeAfterGiveUpFirstRetry,retryCallerTypeRateAfterGiveUpFirstRetry);
						if (u!=null) return s;
						continue;
					}
					if (Language.trAll("XML.Model.ClientType.Retry.ClientType.Canceled",t)) {
						String u=loadRetryCallerTypeChangeData(e2,retryCallerTypeAfterGiveUp,retryCallerTypeRateAfterGiveUp);
						if (u!=null) return s;
						continue;
					}
				}
				continue;
			}

			if (Language.trAll("XML.Model.ClientType.Forwarding",s)) {
				String u=Language.trAllAttribute("XML.Model.ClientType.Forwarding.Probability",e);
				if (!u.isEmpty()) {
					Double D=NumberTools.getSystemProbability(u);
					if (D==null) return String.format(Language.tr("XML.Model.ClientType.Forwarding.Probability.Error"),u); else continueProbability=D;
				}
				NodeList l2=e.getChildNodes();
				for (int j=0; j<l2.getLength();j++) {
					if (!(l2.item(j) instanceof Element)) continue;
					Element e2=(Element)l2.item(j);

					if (Language.trAll("XML.Model.ClientType.Forwarding.NewClientType",e2.getNodeName())) {
						Double D=NumberTools.getNotNegativeDouble(NumberTools.systemNumberToLocalNumber(e2.getTextContent()));
						String name=Language.trAllAttribute("XML.Model.GeneralAttributes.Name",e2);
						if (D==null) return String.format(Language.tr("XML.Model.ClientType.Forwarding.NewClientType.Error"),e2.getTextContent(),name);
						continueTypeName.add(name);
						continueTypeRate.add(D);
						continue;
					}

					if (Language.trAll("XML.Model.ClientType.Forwarding.SkillLevelDepending",e2.getNodeName())) {
						u=Language.trAllAttribute("XML.Model.ClientType.Forwarding.SkillLevel",e2); if (u.isEmpty()) continue;
						continueTypeSkillType.add(u);

						u=Language.trAllAttribute("XML.Model.ClientType.Forwarding.Probability",e2);
						if (!u.isEmpty()) {
							Double D=NumberTools.getSystemProbability(u);
							if (D==null) return String.format(Language.tr("XML.Model.ClientType.Forwarding.Probability.Error"),u); else continueTypeSkillTypeProbability.add(D);
						}

						List<String> names=new ArrayList<String>(); continueTypeSkillTypeName.add(names);
						List<Double> rates=new ArrayList<Double>(); continueTypeSkillTypeRate.add(rates);
						NodeList l3=e2.getChildNodes();
						for (int k=0; k<l3.getLength();k++) {
							if (!(l3.item(k) instanceof Element)) continue;
							Element e3=(Element)l3.item(k);
							if (!Language.trAll("XML.Model.ClientType.Forwarding.NewClientType",e3.getNodeName())) continue;
							Double D=NumberTools.getNotNegativeDouble(NumberTools.systemNumberToLocalNumber(e3.getTextContent()));
							String name=Language.trAllAttribute("XML.Model.GeneralAttributes.Name",e3);
							if (D==null) return String.format(Language.tr("XML.Model.ClientType.Forwarding.NewClientType.Error"),e3.getTextContent(),name);
							names.add(name);
							rates.add(D);
							continue;
						}
						continue;
					}
				}
				continue;
			}

			if (Language.trAll("XML.Model.ClientType.Recall",s)) {
				String u=Language.trAllAttribute("XML.Model.ClientType.Recall.Probability",e);
				if (!u.isEmpty()) {
					Double D=NumberTools.getSystemProbability(u);
					if (D==null) return String.format(Language.tr("XML.Model.ClientType.Recall.Probability.Error"),u); else recallProbability=D;
				}
				NodeList l2=e.getChildNodes();
				for (int j=0; j<l2.getLength();j++) {
					if (!(l2.item(j) instanceof Element)) continue;
					Element e2=(Element)l2.item(j);
					String t=e2.getNodeName();

					if (Language.trAll("XML.Model.ClientType.Recall.IntervalDistribution",t)) {
						recallTimeDist=DistributionTools.distributionFromString(e2.getTextContent(),recallTimeDistMaxX);
						if (recallTimeDist==null) return String.format(Language.tr("XML.Model.ClientType.Recall.IntervalDistribution.Error"),e2.getTextContent());
						continue;
					}

					if (Language.trAll("XML.Model.ClientType.Recall.NewClientType",t)) {
						Double D=NumberTools.getNotNegativeDouble(NumberTools.systemNumberToLocalNumber(e2.getTextContent()));
						String name=Language.trAllAttribute("XML.Model.GeneralAttributes.Name",e2);
						if (D==null) return String.format(Language.tr("XML.Model.ClientType.Recall.NewClientType.Error"),e2.getTextContent(),name);
						recallTypeName.add(name);
						recallTypeRate.add(D);
						continue;
					}

					if (Language.trAll("XML.Model.ClientType.Recall.SkillLevelDepending",e2.getNodeName())) {
						u=Language.trAllAttribute("XML.Model.ClientType.Recall.SkillLevel",e2); if (u.isEmpty()) continue;
						recallTypeSkillType.add(u);

						u=Language.trAllAttribute("XML.Model.ClientType.Recall.Probability",e2);
						if (!u.isEmpty()) {
							Double D=NumberTools.getSystemProbability(u);
							if (D==null) return String.format(Language.tr("XML.Model.ClientType.Recall.Probability.Error"),u); else recallTypeSkillTypeProbability.add(D);
						}

						List<String> names=new ArrayList<String>(); recallTypeSkillTypeName.add(names);
						List<Double> rates=new ArrayList<Double>(); recallTypeSkillTypeRate.add(rates);
						NodeList l3=e2.getChildNodes();
						for (int k=0; k<l3.getLength();k++) {
							if (!(l3.item(k) instanceof Element)) continue;
							Element e3=(Element)l3.item(k);
							if (!Language.trAll("XML.Model.ClientType.Recall.NewClientType",e3.getNodeName())) continue;
							Double D=NumberTools.getNotNegativeDouble(NumberTools.systemNumberToLocalNumber(e3.getTextContent()));
							String name=Language.trAllAttribute("XML.Model.GeneralAttributes.Name",e3);
							if (D==null) return String.format(Language.tr("XML.Model.ClientType.Recall.NewClientType.Error"),e3.getTextContent(),name);
							names.add(name);
							rates.add(D);
							continue;
						}
						continue;
					}
				}
				continue;
			}

			if (Language.trAll("XML.Model.ClientType.YieldPerClient",s)) {
				Double D=NumberTools.getNotNegativeDouble(NumberTools.systemNumberToLocalNumber(e.getTextContent()));
				if (D==null) return String.format(Language.tr("XML.Model.ClientType.YieldPerClient.Error"),e.getTextContent());
				revenuePerClient=D;
				continue;
			}
			if (Language.trAll("XML.Model.ClientType.CostPerCaller",s)) {
				String t=Language.trAllAttribute("XML.Model.ClientType.CostPerCaller.Canceling",e);
				Double D=NumberTools.getNotNegativeDouble(NumberTools.systemNumberToLocalNumber(t));
				if (D==null) return String.format(Language.tr("XML.Model.ClientType.CostPerCaller.Canceling.Error"),t);
				costPerCancel=D;
				t=Language.trAllAttribute("XML.Model.ClientType.CostPerCaller.WaitingSecond",e);
				D=NumberTools.getNotNegativeDouble(NumberTools.systemNumberToLocalNumber(t));
				if (D==null) return String.format(Language.tr("XML.Model.ClientType.CostPerCaller.WaitingSecond.Error"),t);
				costPerWaitingSec=D;
				continue;
			}
		}

		if (freshCallsDist24==null && freshCallsDist48==null && freshCallsDist96==null)	freshCallsDist48=new DataDistributionImpl(freshCallsDistMaxX,48);

		return null;
	}

	/**
	 * Erstellt unterhalb des übergebenen XML-Knotens einen neuen Knoten, der die gesamten Kundentyp-Daten enthält.
	 * @param parent	Eltern-XML-Knoten
	 */
	public void saveToXML(Element parent) {
		Document doc=parent.getOwnerDocument();
		Element node=doc.createElement(Language.trPrimary("XML.Model.ClientType")); parent.appendChild(node);
		if (!active) node.setAttribute(Language.trPrimary("XML.Model.GeneralAttributes.Active"),Language.trPrimary("XML.General.BoolFalse"));
		node.setAttribute(Language.trPrimary("XML.Model.GeneralAttributes.Name"),name);
		if (!blocksLine) node.setAttribute(Language.trPrimary("XML.Model.ClientType.BlocksLine"),Language.trPrimary("XML.General.BoolFalse"));
		if (serviceLevelSeconds>0) node.setAttribute(Language.trPrimary("XML.Model.ClientType.ServiceLevel"),""+serviceLevelSeconds);

		Element e,e2,e3;

		node.appendChild(e=doc.createElement(Language.trPrimary("XML.Model.ClientType.FreshCalls")));
		e.appendChild(e2=doc.createElement(Language.trPrimary("XML.Model.ClientType.FreshCalls.Count"))); e2.setTextContent(""+freshCallsCountMean);
		if (freshCallsCountSD>0) e2.setAttribute(Language.trPrimary("XML.Model.ClientType.FreshCalls.StandardDeviation"),NumberTools.formatSystemNumber(freshCallsCountSD));
		if (freshCallsDist24!=null) {e.appendChild(e2=doc.createElement(Language.trPrimary("XML.Model.ClientType.FreshCalls.Distribution"))); e2.setTextContent(freshCallsDist24.storeToString());}
		if (freshCallsDist48!=null) {e.appendChild(e2=doc.createElement(Language.trPrimary("XML.Model.ClientType.FreshCalls.Distribution"))); e2.setTextContent(freshCallsDist48.storeToString());}
		if (freshCallsDist96!=null) {e.appendChild(e2=doc.createElement(Language.trPrimary("XML.Model.ClientType.FreshCalls.Distribution"))); e2.setTextContent(freshCallsDist96.storeToString());}

		node.appendChild(e=doc.createElement(Language.trPrimary("XML.Model.ClientType.ClientsScore")));
		e.setAttribute(Language.trPrimary("XML.Model.ClientType.ClientsScore.Base"),NumberTools.formatSystemNumber(scoreBase));
		e.setAttribute(Language.trPrimary("XML.Model.ClientType.ClientsScore.PerWaitingSecond"),NumberTools.formatSystemNumber(scoreSecond));
		e.setAttribute(Language.trPrimary("XML.Model.ClientType.ClientsScore.Forwarding"),NumberTools.formatSystemNumber(scoreContinued));

		switch (waitingTimeMode) {
		case WAITING_TIME_MODE_OFF:
			break;
		case WAITING_TIME_MODE_SHORT:
			node.appendChild(e=doc.createElement(Language.trPrimary("XML.Model.ClientType.WaitingTimeTolerance.Normal")));
			e.setTextContent(DistributionTools.distributionToString(waitingTimeDist));
			break;
		case WAITING_TIME_MODE_LONG:
			node.appendChild(e=doc.createElement(Language.trPrimary("XML.Model.ClientType.WaitingTimeTolerance.Long")));
			e.setTextContent(DistributionTools.distributionToString(waitingTimeDistLong));
			break;
		case WAITING_TIME_MODE_CALC:
			node.appendChild(e=doc.createElement(Language.trPrimary("XML.Model.ClientType.WaitingTimeTolerance.Estimation")));
			e.setAttribute(Language.trPrimary("XML.Model.ClientType.WaitingTimeTolerance.Estimation.WaitingTime"),TimeTools.formatExactSystemTime(waitingTimeCalcMeanWaitingTime));
			e.setAttribute(Language.trPrimary("XML.Model.ClientType.WaitingTimeTolerance.Estimation.CancelRate"),NumberTools.formatSystemNumber(waitingTimeCalcCancelProbability*100)+"%");
			e.setAttribute(Language.trPrimary("XML.Model.ClientType.WaitingTimeTolerance.Estimation.Correction"),TimeTools.formatExactSystemTime(waitingTimeCalcAdd));
			break;
		default:
			node.appendChild(e=doc.createElement(Language.trPrimary("XML.Model.ClientType.WaitingTimeTolerance.Normal")));
			e.setTextContent(DistributionTools.distributionToString(waitingTimeDist));
			break;
		}

		node.appendChild(e=doc.createElement(Language.trPrimary("XML.Model.ClientType.Retry")));
		e.appendChild(e2=doc.createElement(Language.trPrimary("XML.Model.ClientType.Retry.Distribution"))); e2.setTextContent(DistributionTools.distributionToString(retryTimeDist));
		e.appendChild(e2=doc.createElement(Language.trPrimary("XML.Model.ClientType.Retry.Probability.BlockedFirst"))); e2.setTextContent(NumberTools.formatSystemNumber(retryProbabiltyAfterBlockedFirstRetry*100)+"%");
		e.appendChild(e2=doc.createElement(Language.trPrimary("XML.Model.ClientType.Retry.Probability.Blocked"))); e2.setTextContent(NumberTools.formatSystemNumber(retryProbabiltyAfterBlocked*100)+"%");
		e.appendChild(e2=doc.createElement(Language.trPrimary("XML.Model.ClientType.Retry.Probability.CanceledFirst"))); e2.setTextContent(NumberTools.formatSystemNumber(retryProbabiltyAfterGiveUpFirstRetry*100)+"%");
		e.appendChild(e2=doc.createElement(Language.trPrimary("XML.Model.ClientType.Retry.Probability.Canceled"))); e2.setTextContent(NumberTools.formatSystemNumber(retryProbabiltyAfterGiveUp*100)+"%");

		e.appendChild(e2=doc.createElement(Language.trPrimary("XML.Model.ClientType.Retry.ClientType.BlockedFirst")));
		for (int i=0;i<retryCallerTypeAfterBlockedFirstRetry.size();i++) {
			e2.appendChild(e3=doc.createElement(Language.trPrimary("XML.Model.ClientType.Retry.ClientType.NewClientType")));
			e3.setAttribute(Language.trPrimary("XML.Model.GeneralAttributes.Name"),retryCallerTypeAfterBlockedFirstRetry.get(i));
			e3.setTextContent(NumberTools.formatSystemNumber(retryCallerTypeRateAfterBlockedFirstRetry.get(i)));
		}
		e.appendChild(e2=doc.createElement(Language.trPrimary("XML.Model.ClientType.Retry.ClientType.Blocked")));
		for (int i=0;i<retryCallerTypeAfterBlocked.size();i++) {
			e2.appendChild(e3=doc.createElement(Language.trPrimary("XML.Model.ClientType.Retry.ClientType.NewClientType")));
			e3.setAttribute(Language.trPrimary("XML.Model.GeneralAttributes.Name"),retryCallerTypeAfterBlocked.get(i));
			e3.setTextContent(NumberTools.formatSystemNumber(retryCallerTypeRateAfterBlocked.get(i)));
		}
		e.appendChild(e2=doc.createElement(Language.trPrimary("XML.Model.ClientType.Retry.ClientType.CanceledFirst")));
		for (int i=0;i<retryCallerTypeAfterGiveUpFirstRetry.size();i++) {
			e2.appendChild(e3=doc.createElement(Language.trPrimary("XML.Model.ClientType.Retry.ClientType.NewClientType")));
			e3.setAttribute(Language.trPrimary("XML.Model.GeneralAttributes.Name"),retryCallerTypeAfterGiveUpFirstRetry.get(i));
			e3.setTextContent(NumberTools.formatSystemNumber(retryCallerTypeRateAfterGiveUpFirstRetry.get(i)));
		}
		e.appendChild(e2=doc.createElement(Language.trPrimary("XML.Model.ClientType.Retry.ClientType.Canceled")));
		for (int i=0;i<retryCallerTypeAfterGiveUp.size();i++) {
			e2.appendChild(e3=doc.createElement(Language.trPrimary("XML.Model.ClientType.Retry.ClientType.NewClientType")));
			e3.setAttribute(Language.trPrimary("XML.Model.GeneralAttributes.Name"),retryCallerTypeAfterGiveUp.get(i));
			e3.setTextContent(NumberTools.formatSystemNumber(retryCallerTypeRateAfterGiveUp.get(i)));
		}

		node.appendChild(e=doc.createElement(Language.trPrimary("XML.Model.ClientType.Forwarding")));
		e.setAttribute(Language.trPrimary("XML.Model.ClientType.Forwarding.Probability"),NumberTools.formatSystemNumber(continueProbability*100)+"%");
		for (int i=0;i<continueTypeName.size();i++) {
			e.appendChild(e2=doc.createElement(Language.trPrimary("XML.Model.ClientType.Forwarding.NewClientType")));
			e2.setAttribute(Language.trPrimary("XML.Model.GeneralAttributes.Name"),continueTypeName.get(i));
			e2.setTextContent(NumberTools.formatSystemNumber(continueTypeRate.get(i)));
		}
		for (int i=0;i<continueTypeSkillType.size();i++) {
			e.appendChild(e2=doc.createElement(Language.trPrimary("XML.Model.ClientType.Forwarding.SkillLevelDepending")));
			e2.setAttribute(Language.trPrimary("XML.Model.ClientType.Forwarding.SkillLevel"),continueTypeSkillType.get(i));
			e2.setAttribute(Language.trPrimary("XML.Model.ClientType.Forwarding.Probability"),NumberTools.formatSystemNumber(continueTypeSkillTypeProbability.get(i)*100)+"%");
			List<String> names=continueTypeSkillTypeName.get(i);
			List<Double> rates=continueTypeSkillTypeRate.get(i);
			for (int j=0;j<names.size();j++) {
				e2.appendChild(e3=doc.createElement(Language.trPrimary("XML.Model.ClientType.Forwarding.NewClientType")));
				e3.setAttribute(Language.trPrimary("XML.Model.GeneralAttributes.Name"),names.get(j));
				e3.setTextContent(NumberTools.formatSystemNumber(rates.get(j)));
			}
		}

		node.appendChild(e=doc.createElement(Language.trPrimary("XML.Model.ClientType.Recall")));
		e.setAttribute(Language.trPrimary("XML.Model.ClientType.Recall.Probability"),NumberTools.formatSystemNumber(recallProbability*100)+"%");
		e.appendChild(e2=doc.createElement(Language.trPrimary("XML.Model.ClientType.Recall.IntervalDistribution"))); e2.setTextContent(DistributionTools.distributionToString(recallTimeDist));
		for (int i=0;i<recallTypeName.size();i++) {
			e.appendChild(e2=doc.createElement(Language.trPrimary("XML.Model.ClientType.Recall.NewClientType")));
			e2.setAttribute(Language.trPrimary("XML.Model.GeneralAttributes.Name"),recallTypeName.get(i));
			e2.setTextContent(NumberTools.formatSystemNumber(recallTypeRate.get(i)));
		}
		for (int i=0;i<recallTypeSkillType.size();i++) {
			e.appendChild(e2=doc.createElement(Language.trPrimary("XML.Model.ClientType.Recall.SkillLevelDepending")));
			e2.setAttribute(Language.trPrimary("XML.Model.ClientType.Recall.SkillLevel"),recallTypeSkillType.get(i));
			e2.setAttribute(Language.trPrimary("XML.Model.ClientType.Recall.Probability"),NumberTools.formatSystemNumber(recallTypeSkillTypeProbability.get(i)*100)+"%");
			List<String> names=recallTypeSkillTypeName.get(i);
			List<Double> rates=recallTypeSkillTypeRate.get(i);
			for (int j=0;j<names.size();j++) {
				e2.appendChild(e3=doc.createElement(Language.trPrimary("XML.Model.ClientType.Recall.NewClientType")));
				e3.setAttribute(Language.trPrimary("XML.Model.GeneralAttributes.Name"),names.get(j));
				e3.setTextContent(NumberTools.formatSystemNumber(rates.get(j)));
			}
		}

		node.appendChild(e=doc.createElement(Language.trPrimary("XML.Model.ClientType.YieldPerClient")));
		e.setTextContent(NumberTools.formatSystemNumber(revenuePerClient));

		node.appendChild(e=doc.createElement(Language.trPrimary("XML.Model.ClientType.CostPerCaller")));
		e.setAttribute(Language.trPrimary("XML.Model.ClientType.CostPerCaller.Canceling"),NumberTools.formatSystemNumber(costPerCancel));
		e.setAttribute(Language.trPrimary("XML.Model.ClientType.CostPerCaller.WaitingSecond"),NumberTools.formatSystemNumber(costPerWaitingSec));
	}

	/**
	 * Liefert unabhängig davon, ob die Kundenankunftsverteilung in 24, 48 oder 96 Intervallen definiert
	 * ist, die Verteilung in 48 Intervallen über den Tag (ggf. werden dafür zwei Intervalle addiert oder
	 * der Wert eines Intervalls auf zwei Intervalle verteilt).
	 * @return	Verteilung der Kundenankünfte auf 48 Intervalle aufgeteilt.
	 */
	public DataDistributionImpl getFreshCallsDistOn48Base() {
		if (freshCallsDist24!=null) {
			DataDistributionImpl dist48=new DataDistributionImpl(86399,48);
			for (int i=0;i<24;i++) {dist48.densityData[i*2]=freshCallsDist24.densityData[i]/2; dist48.densityData[i*2+1]=freshCallsDist24.densityData[i]/2;}
			return dist48;
		}
		if (freshCallsDist48!=null) {
			return freshCallsDist48.clone();
		}
		if (freshCallsDist96!=null) {
			DataDistributionImpl dist48=new DataDistributionImpl(86399,48);
			for (int i=0;i<48;i++) dist48.densityData[i]=freshCallsDist96.densityData[2*i]+freshCallsDist96.densityData[2*i+1];
			return dist48;
		}
		return null;
	}
}
