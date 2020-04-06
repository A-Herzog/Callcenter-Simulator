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

import language.Language;
import mathtools.distribution.DataDistributionImpl;
import mathtools.distribution.LogNormalDistributionImpl;
import mathtools.distribution.tools.DistributionTools;

/**
 * Diese Klasse speichert alle Daten zu einem Kundentyp.<br>
 * Sie wird als Teil der Klasse <code>CallcenterModel</code> verwendet.
 * @author Alexander Herzog
 * @version 1.0
 * @see CallcenterModel
 */
public final class CallcenterRunModelCaller {
	/** Nummer des Kundentyps in der Liste */
	public final short index;

	/** Name des Kundentyps */
	public final String name;

	/** Mittlere Anzahl an Kunden dieses Typs pro Tag */
	public final int freshCallsCountMean;
	/** Standardabweichung der Anzahl an Kunden dieses Typs pro Tag */
	public final double freshCallsCountSD;
	/** Zusätzliche Erstanrufer in Abhängigkeit vom Simulationstag (kann auch <code>null</code> sein) */
	public final int[] freshCallsCountAddByDay;
	/** Verteilung der Kundenankünfte über den Tag */
	public final DataDistributionImpl freshCalls;

	/** Wiederholer vom Vortrag mit bestimmten Anrufzeiten [Tag][nr] */
	public final long[][] freshCallsSheduledByDay;
	/** Übertrag vom Vortrag (bisherige Wartezeit) [Tag][nr] */
	public final long[][] freshCallsInitialWaitingByDay;
	/** Übertrag vom Vortrag (verbleibende Wartezeittoleranz) [Tag][nr] */
	public final long[][] freshCallsInitialToleranceByDay;

	/** Score-Basis für diesen Kundentyp */
	public final double scoreBase;
	/** Erhöhung des Scores pro Wartemillisekunde */
	public final double scoreMilliSecond;
	/** Erhöhung des Scores, wenn es sich um weitergeleitete Kunden handelt */
	public final double scoreContinued;

	/** Belegt ein Kunde dieses Typs eine Telefonleitung? */
	public boolean blocksLine;

	/** Zu verwendende Anzahl an Sekunden für den Service-Level */
	public final short serviceLevelSeconds;

	/** Ist die Wartezeittoleranzverteilung aktiv? (Also gibt es überhaupt Ungeduld?) */
	public boolean waitingTimeDistActive;
	/** Wartezeittoleranzverteilung */
	public final AbstractRealDistribution waitingTimeDist;

	/** Wiederholabständeverteilung */
	public final AbstractRealDistribution retryTimeDist;

	/** Wiederholrate nach "besetzt" - erster Versuch */
	public double retryProbabiltyAfterBlockedFirstRetry;
	/** Wiederholrate nach "besetzt" - ab dem zweiten Versuch */
	public double retryProbabiltyAfterBlocked;
	/** Wiederholrate nach Warteabbruch - erster Versuch */
	public double retryProbabiltyAfterGiveUpFirstRetry;
	/** Wiederholrate nach Warteabbruch - ab dem zweiten Versuch */
	public double retryProbabiltyAfterGiveUp;

	/** Liste der möglichen neuen Kundentypen bei Kundentypenänderung nach "besetzt" - erster Versuch */
	public CallcenterRunModelCaller[] retryCallerTypeAfterBlockedFirstRetry;
	/** Liste der Wahrscheinlichkeiten für die Kundentypenänderung nach "besetzt" - erster Versuch */
	public double[] retryCallerTypeRateAfterBlockedFirstRetry;
	/** Liste der möglichen neuen Kundentypen bei Kundentypenänderung nach "besetzt" - ab dem zweiten Versuch */
	public CallcenterRunModelCaller[] retryCallerTypeAfterBlocked;
	/** Liste der Wahrscheinlichkeiten für die Kundentypenänderung nach "besetzt" - ab dem zweiten Versuch */
	public double[] retryCallerTypeRateAfterBlocked;
	/** Liste der möglichen neuen Kundentypen bei Kundentypenänderung nach Warteabbruch - erster Versuch */
	public CallcenterRunModelCaller[] retryCallerTypeAfterGiveUpFirstRetry;
	/** Liste der Wahrscheinlichkeiten für die Kundentypenänderung nach Warteabbruch - erster Versuch */
	public double[] retryCallerTypeRateAfterGiveUpFirstRetry;
	/** Liste der möglichen neuen Kundentypen bei Kundentypenänderung nach Warteabbruch - ab dem zweiten Versuch */
	public CallcenterRunModelCaller[] retryCallerTypeAfterGiveUp;
	/** Liste der Wahrscheinlichkeiten für die Kundentypenänderung nach Warteabbruch - ab dem zweiten Versuch */
	public double[] retryCallerTypeRateAfterGiveUp;

	/** Weiterleitungswahrscheinlichkeit */
	public double continueProbability;

	/** Liste der möglichen neuen Kundentypen bei einer Weiterleitung (wird von <code>checkAndInit</code> gesetzt) */
	public CallcenterRunModelCaller[] continueType;
	/** Liste der Wahrscheinlichkeiten, mit denen Weiterleitungen zu den bestimmten Kundentypen erfolgen (wird von <code>checkAndInit</code> von Rate zu Wahrscheinlichkeit umgewandelt)*/
	public double[] continueTypeProbability;

	/** Spezielle Weiterleitungsmöglichkeiten in Abhängigkeit vom Skill-Level des bedienenden Agenten */
	public final List<CallcenterRunModelSkillLevel> continueSkillLevel;
	/** Spezielle Weiterleitungswahrscheinlichkeiten in Abhängigkeit vom Skill-Level des bedienenden Agenten */
	public final List<Double> continueSkillLevelProbability;
	/** Liste der möglichen neuen Kundentypen bei einer Weiterleitung (wird von <code>checkAndInit</code> gesetzt) in Abhängigkeit vom Skill-Level des bedienenden Agenten */
	public final List<List<CallcenterRunModelCaller>> continueSkillLevelType;
	/** Liste der Wahrscheinlichkeiten, mit denen Weiterleitungen zu den bestimmten Kundentypen erfolgen (wird von <code>checkAndInit</code> von Rate zu Wahrscheinlichkeit umgewandelt) in Abhängigkeit vom Skill-Level des bedienenden Agenten */
	public final List<List<Double>> continueSkillLevelProbabilities;

	/** Wiederanrufwahrscheinlichkeit */
	public double recallProbability;
	/** Wiederanrufabständeverteilung */
	public final AbstractRealDistribution recallTimeDist;

	/** Liste der möglichen neuen Kundentypen bei einem Wiederanruf (wird von <code>checkAndInit</code> gesetzt) */
	public CallcenterRunModelCaller[] recallType;
	/** Liste der Wahrscheinlichkeiten, mit denen Wiederanrufe zu den bestimmten Kundentypen erfolgen (wird von <code>checkAndInit</code> von Rate zu Wahrscheinlichkeit umgewandelt)*/
	public double[] recallTypeProbability;

	/** Spezielle Wiederanrufmöglichkeiten in Abhängigkeit vom Skill-Level des bedienenden Agenten */
	public final List<CallcenterRunModelSkillLevel> recallSkillLevel;
	/** Spezielle Wiederanrufwahrscheinlichkeiten in Abhängigkeit vom Skill-Level des bedienenden Agenten */
	public final List<Double> recallSkillLevelProbability;
	/** Liste der möglichen neuen Kundentypen bei einem Wiederanruf (wird von <code>checkAndInit</code> gesetzt) in Abhängigkeit vom Skill-Level des bedienenden Agenten */
	public final List<List<CallcenterRunModelCaller>> recallSkillLevelType;
	/** Liste der Wahrscheinlichkeiten, mit denen Wiederanrufe zu den bestimmten Kundentypen erfolgen (wird von <code>checkAndInit</code> von Rate zu Wahrscheinlichkeit umgewandelt) in Abhängigkeit vom Skill-Level des bedienenden Agenten */
	public final List<List<Double>> recallSkillLevelProbabilities;

	/** Wird temporär bei der Intialisierung verwendet */

	private List<String> tempRetryCallerTypeAfterBlockedFirstRetry;
	private List<Double> tempRetryCallerTypeAfterBlockedFirstRetryProbability;
	private List<String> tempRetryCallerTypeAfterBlocked;
	private List<Double> tempRetryCallerTypeAfterBlockedProbability;
	private List<String> tempRetryCallerTypeAfterGiveUpFirstRetry;
	private List<Double> tempRetryCallerTypeAfterGiveUpFirstRetryProbability;
	private List<String> tempRetryCallerTypeAfterGiveUp;
	private List<Double> tempRetryCallerTypeAfterGiveUpProbability;
	private List<String> tempContinueTypeName;
	private List<Double> tempContinueTypeProbability;
	private List<String> tempContinueTypeSkillType;
	private List<List<String>> tempContinueTypeSkillTypeName;
	private List<String> tempRecallTypeName;
	private List<Double> tempRecallTypeProbability;
	private List<String> tempRecallTypeSkillType;
	private List<List<String>> tempRecallTypeSkillTypeName;

	/** Nach diesen Zeiten (in Sekunden) Mindestwartezeit-Rechecks durchführen */
	public int[] recheckTimesMilliSecond=null;

	/**
	 * Konstruktor der Klasse <code>CallcenterModelRunCaller</code>
	 * @param serviceLevelSecondsGlobal	Globaler Service-Level-Sekundenwert
	 * @param editModel	Referenz auf ein Kundentyp-Modell, aus dem dieses Simulations-Modell erstellt werden soll.
	 * @param index	Index der Kundengruppe im Gesamtmodell
	 * @param additionalCallerByDay	Zusätzliche Anrufer über den Tag (Übertrag aus dem Vortrag; kann <code>null</code> sein)
	 * @param retryByDay	Wiederholer vom Vortag (kann <code>null</code> sein)
	 * @param uebertragWaitingByDay	Wartezeiten vom aktuellen Anrufern im System vom Vortrag (kann <code>null</code> sein)
	 * @param uebertragToleranceByDay	Wartezeittoleranzen von aktuellen Anrufern im System vom Vortrag (kann <code>null</code> sein)
	 */
	public CallcenterRunModelCaller(short serviceLevelSecondsGlobal, CallcenterModelCaller editModel, short index, int[] additionalCallerByDay, long[][] retryByDay, long[][] uebertragWaitingByDay, long[][] uebertragToleranceByDay) {
		this.index=index;
		name=editModel.name;

		/* Fresh calls */

		freshCallsCountMean=editModel.freshCallsCountMean;
		freshCallsCountSD=editModel.freshCallsCountSD;
		freshCallsCountAddByDay=additionalCallerByDay;
		DataDistributionImpl temp=null;
		if (editModel.freshCallsDist24!=null) temp=(DataDistributionImpl)DistributionTools.normalizeDistribution(editModel.freshCallsDist24);
		if (editModel.freshCallsDist48!=null) temp=(DataDistributionImpl)DistributionTools.normalizeDistribution(editModel.freshCallsDist48);
		if (editModel.freshCallsDist96!=null) temp=(DataDistributionImpl)DistributionTools.normalizeDistribution(editModel.freshCallsDist96);
		freshCalls=temp;

		freshCallsSheduledByDay=retryByDay;
		freshCallsInitialWaitingByDay=uebertragWaitingByDay;
		freshCallsInitialToleranceByDay=uebertragToleranceByDay;

		/* Score */

		scoreBase=editModel.scoreBase;
		scoreMilliSecond=editModel.scoreSecond/1000.0;
		scoreContinued=editModel.scoreContinued;

		/* Blockiert der Kunde eine Telefonleitung */

		blocksLine=editModel.blocksLine;

		/* Service-Level */

		serviceLevelSeconds=(editModel.serviceLevelSeconds<=0)?serviceLevelSecondsGlobal:(editModel.serviceLevelSeconds);

		/* Wartezeittoleranz */

		switch (editModel.waitingTimeMode) {
		case CallcenterModelCaller.WAITING_TIME_MODE_OFF:
			waitingTimeDistActive=false;
			waitingTimeDist=null;
			break;
		case CallcenterModelCaller.WAITING_TIME_MODE_SHORT:
			waitingTimeDistActive=true;
			waitingTimeDist=DistributionTools.normalizeDistribution(editModel.waitingTimeDist);
			break;
		case CallcenterModelCaller.WAITING_TIME_MODE_LONG:
			waitingTimeDistActive=true;
			waitingTimeDist=DistributionTools.normalizeDistribution(editModel.waitingTimeDistLong);
			break;
		case CallcenterModelCaller.WAITING_TIME_MODE_CALC:
			waitingTimeDistActive=true;
			double mean=editModel.waitingTimeCalcMeanWaitingTime/editModel.waitingTimeCalcCancelProbability;
			double sd=mean;
			mean=Math.max(1,mean+editModel.waitingTimeCalcAdd);
			waitingTimeDist=new LogNormalDistributionImpl(mean,sd);
			break;
		default:
			waitingTimeDistActive=false;
			waitingTimeDist=null;
		}

		/* Wiederholungen */

		retryTimeDist=DistributionTools.normalizeDistribution(editModel.retryTimeDist);
		retryProbabiltyAfterBlockedFirstRetry=editModel.retryProbabiltyAfterBlockedFirstRetry;
		retryProbabiltyAfterBlocked=editModel.retryProbabiltyAfterBlocked;
		retryProbabiltyAfterGiveUpFirstRetry=editModel.retryProbabiltyAfterGiveUpFirstRetry;
		retryProbabiltyAfterGiveUp=editModel.retryProbabiltyAfterGiveUp;

		retryCallerTypeAfterBlockedFirstRetry=null;
		retryCallerTypeRateAfterBlockedFirstRetry=null;
		retryCallerTypeAfterBlocked=null;
		retryCallerTypeRateAfterBlocked=null;
		retryCallerTypeAfterGiveUpFirstRetry=null;
		retryCallerTypeRateAfterGiveUpFirstRetry=null;
		retryCallerTypeAfterGiveUp=null;
		retryCallerTypeRateAfterGiveUp=null;

		tempRetryCallerTypeAfterBlockedFirstRetry=new ArrayList<String>(editModel.retryCallerTypeAfterBlockedFirstRetry);
		tempRetryCallerTypeAfterBlockedFirstRetryProbability=new ArrayList<Double>(editModel.retryCallerTypeRateAfterBlockedFirstRetry);
		tempRetryCallerTypeAfterBlocked=new ArrayList<String>(editModel.retryCallerTypeAfterBlocked);
		tempRetryCallerTypeAfterBlockedProbability=new ArrayList<Double>(editModel.retryCallerTypeRateAfterBlocked);
		tempRetryCallerTypeAfterGiveUpFirstRetry=new ArrayList<String>(editModel.retryCallerTypeAfterGiveUpFirstRetry);
		tempRetryCallerTypeAfterGiveUpFirstRetryProbability=new ArrayList<Double>(editModel.retryCallerTypeRateAfterGiveUpFirstRetry);
		tempRetryCallerTypeAfterGiveUp=new ArrayList<String>(editModel.retryCallerTypeAfterGiveUp);
		tempRetryCallerTypeAfterGiveUpProbability=new ArrayList<Double>(editModel.retryCallerTypeRateAfterGiveUp);

		/* Weiterleitungen */

		continueProbability=editModel.continueProbability;
		continueType=null;
		continueTypeProbability=null;

		continueSkillLevel=new ArrayList<CallcenterRunModelSkillLevel>();
		continueSkillLevelProbability=new ArrayList<Double>(editModel.continueTypeSkillTypeProbability);
		continueSkillLevelType=new ArrayList<List<CallcenterRunModelCaller>>();
		continueSkillLevelProbabilities=new ArrayList<List<Double>>();
		for (int i=0;i<editModel.continueTypeSkillTypeRate.size();i++) {
			List<Double> list1=editModel.continueTypeSkillTypeRate.get(i);
			List<Double> list2=new ArrayList<Double>(list1); continueSkillLevelProbabilities.add(list2);
		}

		tempContinueTypeName=new ArrayList<String>(editModel.continueTypeName);
		tempContinueTypeProbability=new ArrayList<Double>(editModel.continueTypeRate);
		tempContinueTypeSkillType=editModel.continueTypeSkillType;
		tempContinueTypeSkillTypeName=editModel.continueTypeSkillTypeName;

		/* Wiederanrufer */

		recallProbability=editModel.recallProbability;
		recallTimeDist=DistributionTools.normalizeDistribution(editModel.recallTimeDist);
		recallType=null;
		recallTypeProbability=null;

		recallSkillLevel=new ArrayList<CallcenterRunModelSkillLevel>();
		recallSkillLevelProbability=new ArrayList<Double>(editModel.recallTypeSkillTypeProbability);
		recallSkillLevelType=new ArrayList<List<CallcenterRunModelCaller>>();
		recallSkillLevelProbabilities=new ArrayList<List<Double>>();
		for (int i=0;i<editModel.recallTypeSkillTypeRate.size();i++) {
			List<Double> list1=editModel.recallTypeSkillTypeRate.get(i);
			List<Double> list2=new ArrayList<Double>(list1); recallSkillLevelProbabilities.add(list2);
		}

		tempRecallTypeName=new ArrayList<String>(editModel.recallTypeName);
		tempRecallTypeProbability=new ArrayList<Double>(editModel.recallTypeRate);
		tempRecallTypeSkillType=editModel.recallTypeSkillType;
		tempRecallTypeSkillTypeName=editModel.recallTypeSkillTypeName;
	}

	private static double[] doubleArrayFromDoubleList(List<Double> list) {
		double[] d=new double[list.size()];
		for (int i=0;i<d.length;i++) d[i]=list.get(i);
		return d;
	}

	private static String calcProbabilities(final CallcenterRunModelCaller[] caller, final boolean strict, final List<Double> listProbabilites, final List<String> listNames, final List<CallcenterRunModelCaller> listCaller, final String errorSize, final String errorContinueToUnknown, final String errorNegativeProbability, final String errorSumZero) {
		double sum;

		if (listNames.size()!=listProbabilites.size()) return errorSize;

		/* Bei Wiederhol-Kundentyp-Änderungen ist summe=0 ok, dann ändert sich der Typ nicht */
		if (errorSumZero==null) {
			boolean changes=false;
			for (int i=0;i<listProbabilites.size();i++) if (listProbabilites.get(i)>0) {changes=true; break;}
			if (!changes) return null;
		}

		sum=0;
		listCaller.clear();
		int index=0;
		while (index<listNames.size()) {
			/* Kundentyp finden */
			String s=listNames.get(index);
			CallcenterRunModelCaller c=null;
			for (int j=0;j<caller.length;j++) if (caller[j].name.equalsIgnoreCase(s)) {c=caller[j]; break;}

			if (c==null) {
				if (strict && listProbabilites.get(index)>0) return errorContinueToUnknown.replace("$name",s);
				listNames.remove(index);
				listProbabilites.remove(index);
			} else {
				listCaller.add(c);
				if (listProbabilites.get(index)<0) {
					if (strict) return errorNegativeProbability.replace("$name",listNames.get(index));
					listProbabilites.set(index,0.0);
				}
				sum+=listProbabilites.get(index);
				index++;
			}
		}

		if (sum==0) return errorSumZero;
		for (int i=0;i<listProbabilites.size();i++) listProbabilites.set(i,listProbabilites.get(i)/sum);
		return null;
	}

	/**
	 * Bereitet das Objekt auf die Simulation vor.
	 * @param caller	Liste mit allen Anrufer-Klassen
	 * @param skills	Liste mit allen Skill-Levels
	 * @param strict	Strenge Modellprüfung
	 * @return Gibt <code>null</code> zurück, wenn die Initialisierung erfolgreich war, andernfalls wird eine Fehlermeldung als String zurückgegeben,
	 */
	public String checkAndInit(final CallcenterRunModelCaller[] caller, final CallcenterRunModelSkillLevel[] skills, final boolean strict) {
		String s,t;

		if (name==null || name.trim().isEmpty()) return Language.tr("Model.Check.ClientType.NoName");

		/* Wiederholer */

		if (retryProbabiltyAfterBlockedFirstRetry<0 || retryProbabiltyAfterBlockedFirstRetry>1) {
			if (strict || retryProbabiltyAfterBlockedFirstRetry>1) return String.format(Language.tr("Model.Check.ClientType.InvalidRetryProbability"),name,Language.tr("Model.Check.ClientType.InvalidRetryProbability.BlockedFirst"));
			retryProbabiltyAfterBlockedFirstRetry=0;
		}
		if (retryProbabiltyAfterBlocked<0 || retryProbabiltyAfterBlocked>1) {
			if (strict || retryProbabiltyAfterBlocked>1) return String.format(Language.tr("Model.Check.ClientType.InvalidRetryProbability"),name,Language.tr("Model.Check.ClientType.InvalidRetryProbability.Blocked"));
			retryProbabiltyAfterBlocked=0;
		}
		if (retryProbabiltyAfterGiveUpFirstRetry<0 || retryProbabiltyAfterGiveUpFirstRetry>1) {
			if (strict || retryProbabiltyAfterGiveUpFirstRetry>1) return String.format(Language.tr("Model.Check.ClientType.InvalidRetryProbability"),name,Language.tr("Model.Check.ClientType.InvalidRetryProbability.CanceledFirst"));
			retryProbabiltyAfterGiveUpFirstRetry=0;
		}
		if (retryProbabiltyAfterGiveUp<0 || retryProbabiltyAfterGiveUp>1) {
			if (strict || retryProbabiltyAfterGiveUp>1) return String.format(Language.tr("Model.Check.ClientType.InvalidRetryProbability"),name,Language.tr("Model.Check.ClientType.InvalidRetryProbability.Canceled"));
			retryProbabiltyAfterGiveUp=0;
		}

		final List<CallcenterRunModelCaller> retryCallerTypeAfterBlockedFirstRetryList=new ArrayList<CallcenterRunModelCaller>();
		t=Language.tr("Model.Check.ClientType.RetryCheck.BlockedFirst");
		s=calcProbabilities(
				caller,strict,
				tempRetryCallerTypeAfterBlockedFirstRetryProbability,tempRetryCallerTypeAfterBlockedFirstRetry,retryCallerTypeAfterBlockedFirstRetryList,
				String.format(Language.tr("Model.Check.ClientType.RetryCheck.InternalTypeChangeError"),name)+" ("+t+")",
				String.format(Language.tr("Model.Check.ClientType.RetryCheck.UnknownClientType"),name)+" ("+t+")",
				String.format(Language.tr("Model.Check.ClientType.RetryCheck.InvalidChangeRate"),name)+" ("+t+")",
				null
				);
		if (s!=null) return s;
		retryCallerTypeRateAfterBlockedFirstRetry=doubleArrayFromDoubleList(tempRetryCallerTypeAfterBlockedFirstRetryProbability);
		retryCallerTypeAfterBlockedFirstRetry=retryCallerTypeAfterBlockedFirstRetryList.toArray(new CallcenterRunModelCaller[0]);


		final List<CallcenterRunModelCaller> retryCallerTypeAfterBlockedList=new ArrayList<CallcenterRunModelCaller>();
		t=Language.tr("Model.Check.ClientType.RetryCheck.Blocked");
		s=calcProbabilities(
				caller,strict,
				tempRetryCallerTypeAfterBlockedProbability,tempRetryCallerTypeAfterBlocked,retryCallerTypeAfterBlockedList,
				String.format(Language.tr("Model.Check.ClientType.RetryCheck.InternalTypeChangeError"),name)+" ("+t+")",
				String.format(Language.tr("Model.Check.ClientType.RetryCheck.UnknownClientType"),name)+" ("+t+")",
				String.format(Language.tr("Model.Check.ClientType.RetryCheck.InvalidChangeRate"),name)+" ("+t+")",
				null
				);
		if (s!=null) return s;
		retryCallerTypeRateAfterBlocked=doubleArrayFromDoubleList(tempRetryCallerTypeAfterBlockedProbability);
		retryCallerTypeAfterBlocked=retryCallerTypeAfterBlockedList.toArray(new CallcenterRunModelCaller[0]);

		final List<CallcenterRunModelCaller> retryCallerTypeAfterGiveUpFirstRetryList=new ArrayList<CallcenterRunModelCaller>();
		t=Language.tr("Model.Check.ClientType.RetryCheck.CanceledFirst");
		s=calcProbabilities(
				caller,strict,
				tempRetryCallerTypeAfterGiveUpFirstRetryProbability,tempRetryCallerTypeAfterGiveUpFirstRetry,retryCallerTypeAfterGiveUpFirstRetryList,
				String.format(Language.tr("Model.Check.ClientType.RetryCheck.InternalTypeChangeError"),name)+" ("+t+")",
				String.format(Language.tr("Model.Check.ClientType.RetryCheck.UnknownClientType"),name)+" ("+t+")",
				String.format(Language.tr("Model.Check.ClientType.RetryCheck.InvalidChangeRate"),name)+" ("+t+")",
				null
				);
		if (s!=null) return s;
		retryCallerTypeRateAfterGiveUpFirstRetry=doubleArrayFromDoubleList(tempRetryCallerTypeAfterGiveUpFirstRetryProbability);
		retryCallerTypeAfterGiveUpFirstRetry=retryCallerTypeAfterGiveUpFirstRetryList.toArray(new CallcenterRunModelCaller[0]);

		final List<CallcenterRunModelCaller> retryCallerTypeAfterGiveUpList=new ArrayList<CallcenterRunModelCaller>();
		t=Language.tr("Model.Check.ClientType.RetryCheck.Canceled");
		s=calcProbabilities(
				caller,strict,
				tempRetryCallerTypeAfterGiveUpProbability,tempRetryCallerTypeAfterGiveUp,retryCallerTypeAfterGiveUpList,
				String.format(Language.tr("Model.Check.ClientType.RetryCheck.InternalTypeChangeError"),name)+" ("+t+")",
				String.format(Language.tr("Model.Check.ClientType.RetryCheck.UnknownClientType"),name)+" ("+t+")",
				String.format(Language.tr("Model.Check.ClientType.RetryCheck.InvalidChangeRate"),name)+" ("+t+")",
				null
				);
		if (s!=null) return s;
		retryCallerTypeRateAfterGiveUp=doubleArrayFromDoubleList(tempRetryCallerTypeAfterGiveUpProbability);
		retryCallerTypeAfterGiveUp=retryCallerTypeAfterGiveUpList.toArray(new CallcenterRunModelCaller[0]);

		/* Weiterleitungen */

		if (continueProbability<0 || continueProbability>1) {
			if (strict || continueProbability>1) return String.format(Language.tr("Model.Check.ClientType.ForwardCheck.InvalidProbability"),name);
			continueProbability=0;
		}

		final List<CallcenterRunModelCaller> continueTypeList=new ArrayList<CallcenterRunModelCaller>();
		s=calcProbabilities(
				caller,strict,
				tempContinueTypeProbability,tempContinueTypeName,continueTypeList,
				String.format(Language.tr("Model.Check.ClientType.ForwardCheck.InternalTypeChangeError"),name),
				String.format(Language.tr("Model.Check.ClientType.ForwardCheck.UnknownClientType"),name),
				String.format(Language.tr("Model.Check.ClientType.ForwardCheck.InvalidChangeRate"),name),
				(continueProbability==0)?null:String.format(Language.tr("Model.Check.ClientType.ForwardCheck.NoChangeRates"),name)
				);
		if (s!=null) return s;
		continueTypeProbability=doubleArrayFromDoubleList(tempContinueTypeProbability);
		continueType=continueTypeList.toArray(new CallcenterRunModelCaller[0]);

		continueSkillLevel.clear();
		continueSkillLevelType.clear();
		for (int i=0;i<tempContinueTypeSkillType.size();i++) {
			if (continueSkillLevelProbability.get(i)<0) return String.format(Language.tr("Model.Check.ClientType.ForwardCheck.InvalidProbability.SkillLevelSpecific"),name,tempContinueTypeName.get(i));

			/* Skill-Level finden */
			s=tempContinueTypeSkillType.get(i);
			CallcenterRunModelSkillLevel skill=null;
			for (int j=0;j<skills.length;j++) if (skills[j].name.equalsIgnoreCase(s)) {skill=skills[j]; break;}
			if (skill==null) return String.format(Language.tr("Model.Check.ClientType.ForwardCheck.UnknownSkillLevel"),name,s);
			continueSkillLevel.add(skill);

			/* Kundentyp finden */
			double continueProbabilitiesSum=0;
			List<String> edit=tempContinueTypeSkillTypeName.get(i);
			List<CallcenterRunModelCaller> run=new ArrayList<CallcenterRunModelCaller>(); continueSkillLevelType.add(run);
			int index=0;
			while (index<edit.size()) {
				s=edit.get(index);
				CallcenterRunModelCaller c=null;
				for (int k=0;k<caller.length;k++) if (caller[k].name.equalsIgnoreCase(s)) {c=caller[k]; break;}
				if (c==null) {
					if (strict) return String.format(Language.tr("Model.Check.ClientType.ForwardCheck.UnknownClientType2"),name,s);
					edit.remove(index);
					continueSkillLevelProbabilities.get(i).remove(index);
				} else {
					run.add(c);
					if (continueSkillLevelProbabilities.get(i).get(index)<0) {
						if (strict) return String.format(Language.tr("Model.Check.ClientType.ForwardCheck.InvalidRate"),name,s);
						continueSkillLevelProbabilities.get(i).set(index,0.0);
					}
					continueProbabilitiesSum+=continueSkillLevelProbabilities.get(i).get(index);
					index++;
				}
			}
			if (continueSkillLevelProbability.get(i)>0 && continueProbabilitiesSum==0) return String.format(Language.tr("Model.Check.ClientType.ForwardCheck.NoChangeRates.SkillLevelSpecific"),name,tempContinueTypeName.get(i));
			if (continueProbabilitiesSum>0) for (int j=0;j<continueSkillLevelProbabilities.get(i).size();j++) continueSkillLevelProbabilities.get(i).set(j,continueSkillLevelProbabilities.get(i).get(j)/continueProbabilitiesSum);
		}

		/* Wiederanrufer */

		if (recallProbability<0 || recallProbability>1) {
			if (strict || recallProbability>1) return String.format(Language.tr("Model.Check.ClientType.RecallCheck.InvalidProbability"),name);
			recallProbability=0;
		}

		final List<CallcenterRunModelCaller> recallTypeList=new ArrayList<CallcenterRunModelCaller>();
		s=calcProbabilities(
				caller,strict,
				tempRecallTypeProbability,tempRecallTypeName,recallTypeList,
				String.format(Language.tr("Model.Check.ClientType.RecallCheck.InternalTypeChangeError"),name),
				String.format(Language.tr("Model.Check.ClientType.RecallCheck.UnknownClientType"),name),
				String.format(Language.tr("Model.Check.ClientType.RecallCheck.InvalidChangeRate"),name),
				(recallProbability==0)?null:String.format(Language.tr("Model.Check.ClientType.RecallCheck.NoChangeRates"),name)
				);
		if (s!=null) return s;
		recallTypeProbability=doubleArrayFromDoubleList(tempRecallTypeProbability);
		recallType=recallTypeList.toArray(new CallcenterRunModelCaller[0]);

		recallSkillLevel.clear();
		recallSkillLevelType.clear();
		for (int i=0;i<tempRecallTypeSkillType.size();i++) {
			if (recallSkillLevelProbability.get(i)<0) return String.format(Language.tr("Model.Check.ClientType.RecallCheck.InvalidProbability.SkillLevelSpecific"),name,tempRecallTypeName.get(i));

			/* Skill-Level finden */
			s=tempRecallTypeSkillType.get(i);
			CallcenterRunModelSkillLevel skill=null;
			for (int j=0;j<skills.length;j++) if (skills[j].name.equalsIgnoreCase(s)) {skill=skills[j]; break;}
			if (skill==null) return String.format(Language.tr("Model.Check.ClientType.RecallCheck.UnknownSkillLevel"),name,s);
			recallSkillLevel.add(skill);

			/* Kundentyp finden */
			double recallProbabilitiesSum=0;
			List<String> edit=tempRecallTypeSkillTypeName.get(i);
			List<CallcenterRunModelCaller> run=new ArrayList<CallcenterRunModelCaller>(); recallSkillLevelType.add(run);
			int index=0;
			while (index<edit.size()) {
				s=edit.get(index);
				CallcenterRunModelCaller c=null;
				for (int k=0;k<caller.length;k++) if (caller[k].name.equalsIgnoreCase(s)) {c=caller[k]; break;}
				if (c==null) {
					if (strict) return String.format(Language.tr("Model.Check.ClientType.RecallCheck.UnknownClientType2"),name,s);
					edit.remove(index);
					recallSkillLevelProbabilities.get(i).remove(index);
				} else {
					run.add(c);
					if (recallSkillLevelProbabilities.get(i).get(index)<0) {
						if (strict) return String.format(Language.tr("Model.Check.ClientType.RecallCheck.InvalidRate"),name,s);
						recallSkillLevelProbabilities.get(i).set(index,0.0);
					}
					recallProbabilitiesSum+=recallSkillLevelProbabilities.get(i).get(index);
					index++;
				}
			}
			if (recallSkillLevelProbability.get(i)>0 && recallProbabilitiesSum==0) return String.format(Language.tr("Model.Check.ClientType.RecallCheck.NoChangeRates.SkillLevelSpecific"),name,tempRecallTypeName.get(i));
			if (recallProbabilitiesSum>0) for (int j=0;j<recallSkillLevelProbabilities.get(i).size();j++) recallSkillLevelProbabilities.get(i).set(j,recallSkillLevelProbabilities.get(i).get(j)/recallProbabilitiesSum);
		}

		/* Speicher sparen */
		tempRetryCallerTypeAfterBlockedFirstRetry=null;
		tempRetryCallerTypeAfterBlockedFirstRetryProbability=null;
		tempRetryCallerTypeAfterBlocked=null;
		tempRetryCallerTypeAfterBlockedProbability=null;
		tempRetryCallerTypeAfterGiveUpFirstRetry=null;
		tempRetryCallerTypeAfterGiveUpFirstRetryProbability=null;
		tempRetryCallerTypeAfterGiveUp=null;
		tempRetryCallerTypeAfterGiveUpProbability=null;
		tempContinueTypeName=null;
		tempContinueTypeProbability=null;
		tempContinueTypeSkillType=null;
		tempContinueTypeSkillTypeName=null;
		tempRecallTypeName=null;
		tempRecallTypeProbability=null;
		tempRecallTypeSkillType=null;
		tempRecallTypeSkillTypeName=null;

		return null;
	}
}
