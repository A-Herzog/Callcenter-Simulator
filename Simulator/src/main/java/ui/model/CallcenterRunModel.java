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

import java.util.Arrays;
import java.util.List;

import language.Language;
import mathtools.NumberTools;
import mathtools.distribution.DataDistributionImpl;
import mathtools.distribution.tools.DistributionTools;
import parser.CalcSystem;
import parser.MathCalcError;
import parser.MathParser;
import simulator.SimulationData;
import tools.SetupData;

/**
 * Diese Klasse kapselt das komplette Callcenter Modell.<br>
 * @author Alexander Herzog
 * @version 1.0
 * @see CallcenterModel
 * @see SimulationData
 */
public final class CallcenterRunModel {
	/** Zugehöriges Editor-Modell */
	public final CallcenterModel editModel;

	/* Kundentypen */

	/** Liste der Kundentypen */
	public final CallcenterRunModelCaller[] caller;

	/* Callcenter */

	/** Werden in dem Modell Mindestwartezeiten verwendet? */
	public final boolean callerMinWaitingTimeUsed;
	/** Werden in dem Modell Kosten für die Agenten verwendet? */
	public boolean agentCostsUsed;
	/** Liste der Callcenter */
	public final CallcenterRunModelCallcenter[] callcenter;

	/* Skill-Level */

	/** Liste der Skill-Level */
	public final CallcenterRunModelSkillLevel[] skills;

	/* Allgemeine Daten */

	/** Maximale Warteschlangenlänge<br>(Ist die Warteschlange voll, erhalten Anrufer das Besetztzeichen.) */
	public MathParser maxQueueLength;

	/** Anzahl der zu simulierenden Tage */
	private int days;

	/**
	 * Konstruktor der Klasse <code>CallcenterRunModel</code>
	 * @param editModel	Zugehöriges Editor-Modell
	 * @param additionalCallerByDay	Zusätzliche Anrufer (Übertrag aus dem Vortrag; kann <code>null</code> sein)
	 * @param retryByDay	Wiederholder vom Vortrag (kann <code>null</code> sein)
	 * @param uebertragWaitingByDay	Übertrag von wartenden Anrufern vom Vortrag (kann <code>null</code> sein)
	 * @param uebertragToleranceByDay	Übertrag der Wartezeittoleranzen der wartenden Anrufern vom Vortrag (kann <code>null</code> sein)
	 */
	public CallcenterRunModel(final CallcenterModel editModel, final List<int[]> additionalCallerByDay, final List<long[][]> retryByDay, final List<long[][]> uebertragWaitingByDay, final List<long[][]> uebertragToleranceByDay) {
		this.editModel=editModel;
		days=editModel.days;

		/* Calls */
		CallcenterRunModelCaller[] callerTemp=new CallcenterRunModelCaller[editModel.caller.size()];
		short callerTempUsed=0;

		for (int i=0;i<callerTemp.length;i++) {
			CallcenterModelCaller caller=editModel.caller.get(i);
			if (caller.active) {
				int[] add=null;	if (additionalCallerByDay!=null && additionalCallerByDay.size()>i) add=additionalCallerByDay.get(i);
				long[][] retry=null; if (retryByDay!=null && retryByDay.size()>i) retry=retryByDay.get(i);
				long[][] uebertragWaiting=null; if (uebertragWaitingByDay!=null && uebertragWaitingByDay.size()>i) uebertragWaiting=uebertragWaitingByDay.get(i);
				long[][] uebertragTolerance=null; if (uebertragToleranceByDay!=null && uebertragToleranceByDay.size()>i) uebertragTolerance=uebertragToleranceByDay.get(i);
				callerTemp[callerTempUsed]=new CallcenterRunModelCaller(editModel.serviceLevelSeconds,caller,callerTempUsed,add,retry,uebertragWaiting,uebertragTolerance);
				callerTempUsed++;
			}
		}

		if (callerTempUsed<callerTemp.length) {
			caller=Arrays.copyOf(callerTemp,callerTempUsed);
		} else {
			caller=callerTemp;
		}

		/* Callcenter */
		CallcenterRunModelCallcenter[] callcenterTemp=new CallcenterRunModelCallcenter[editModel.callcenter.size()];
		int callcenterTempUsed=0;

		int minWaitingTimeSum=0;
		for (int i=0;i<callcenterTemp.length;i++) {
			CallcenterModelCallcenter callcenter=editModel.callcenter.get(i);
			if (callcenter.active) {
				CallcenterRunModelCallcenter c=new CallcenterRunModelCallcenter(callcenter,editModel);
				callcenterTemp[callcenterTempUsed]=c;
				callcenterTempUsed++;
				for (int j=0;j<c.callerMinWaitingTimeMilliSecond.size();j++) minWaitingTimeSum+=c.callerMinWaitingTimeMilliSecond.get(j);
			}
		}
		callerMinWaitingTimeUsed=(minWaitingTimeSum>0);

		if (callcenterTempUsed<callcenterTemp.length) {
			callcenter=Arrays.copyOf(callcenterTemp,callcenterTempUsed);
		} else {
			callcenter=callcenterTemp;
		}

		/* Skill-Level */
		skills=new CallcenterRunModelSkillLevel[editModel.skills.size()];
		for (int i=0;i<editModel.skills.size();i++)
			skills[i]=new CallcenterRunModelSkillLevel(editModel.skills.get(i));
	}

	/**
	 * Konstruktor der Klasse <code>CallcenterRunModel</code>
	 * @param editModel	Zugehöriges Editor-Modell
	 */
	public CallcenterRunModel(CallcenterModel editModel) {
		this(editModel,null,null,null,null);
	}

	/**
	 * Erlaubt das nachträgliche Verändern der Anzahl an zu simulierenden Tagen
	 * @param days	Anzahl an zu simulierenden Tagen
	 * @return	Liefert <code>true</code> zurück, wenn die Anzahl verändert werden konnte, oder <code>false</code>, wenn die angegebene Anzahl ungültig ist (weil sie &le;0 ist)
	 */
	public boolean setDays(int days) {
		if (days<=0) return false;
		this.days=days;
		return true;
	}

	/**
	 * Liefert die Anzahl an zu simulierenden Tagen
	 * @return	Anzahl an zu simulierenden Tagen
	 */
	public int getDays() {
		return days;
	}

	/**
	 * Prüfen, ob bei der Bestimmung der Agenten gemäß den Kundenankünften alle Typen korrekt existieren
	 * @param agent	Zu prüfende Agentengruppe
	 * @param caller	Liste der Anrufergruppen
	 * @param strict	Gibt an, ob eine strenge Prüfung erfolgen soll.
	 * @return Gibt <code>null</code> zurück, wenn die Initialisierung erfolgreich war, andernfalls wird eine Fehlermeldung als String zurückgegeben,
	 */
	private String checkAgentByCaller(CallcenterModelAgent agent, List<CallcenterModelCaller> caller, boolean strict) {
		if (agent.byCallers.size()==0) return Language.tr("Model.Check.NoClientTypeForAgentsDistribution");
		if (agent.byCallersAvailableHalfhours<=0) return Language.tr("Model.Check.InvalidAgentsCountForDistribution");

		DataDistributionImpl dist=agent.calcAgentDistributionFromCallers(caller);
		if (dist.sumIsZero()) return null;

		if (strict) for (int i=0;i<Math.min(agent.byCallers.size(),agent.byCallersRate.size());i++) {
			boolean ok=false;
			for (int j=0;j<caller.size();j++) if (caller.get(j).name.equalsIgnoreCase(agent.byCallers.get(i))) {ok=true; break;}
			if (!ok) return String.format(Language.tr("Model.Check.UnknownClientTypeForAgentsDistribution"),agent.byCallers.get(i));
			if (agent.byCallersRate.get(i)<=0) return String.format(Language.tr("Model.Check.InvalidRateForClientTypeForAgentsDistribution"),agent.byCallers.get(i));
		}

		return null;
	}

	/**
	 * Bereitet das Objekt auf die Simulation vor.
	 * @param commandLine Gibt an, ob die Simulation per Kommandozeile erfolgen soll (setzt unabhängig von der Modellgröße eine Lizenz voraus).
	 * @param batch	Gibt an, ob die Simulation als Stapelverarbeitung erfolgen soll (setzt unabhängig von der Modellgröße eine Lizenz voraus).
	 * @param strictCheck	Gibt an, ob eine strenge Prüfung erfolgen soll.
	 * @return Gibt <code>null</code> zurück, wenn die Initialisierung erfolgreich war, andernfalls wird eine Fehlermeldung als String zurückgegeben,
	 */
	public String checkAndInit(final boolean commandLine, final boolean batch, final boolean strictCheck) {
		/* Anzahl an zu simulierenden Tagen */
		if (days<=0) return String.format(Language.tr("Model.Check.InvalidNumberOfSimulationDays"),days);

		/* Maximale Warteschlangenlänge */
		MathParser parser=new CalcSystem(editModel.maxQueueLength,new String[]{"a"});
		boolean ok=(parser.parse()==-1);
		if (ok) try {ok=(parser.calc(new double[]{1.0})>=0);} catch (MathCalcError e) {ok=false;}
		if (!ok) return String.format(Language.tr("Model.Check.InvalidMaximumQueueLength"),editModel.maxQueueLength);
		maxQueueLength=parser;

		/* Minimale / Maximale Schichtlänge */
		if (editModel.minimumShiftLength>editModel.preferredShiftLength) return String.format(Language.tr("Model.Check.MinimalShiftLengthLongerThanPreferredShiftLength"));
		for (CallcenterModelCallcenter callcenter: editModel.callcenter) if (callcenter.active) for (int i=0;i<callcenter.agents.size();i++) {
			CallcenterModelAgent agents=callcenter.agents.get(i);
			if (!agents.active || agents.count>=0) continue;
			int minimumShiftLength=editModel.minimumShiftLength;
			if (agents.minimumShiftLength>0) minimumShiftLength=agents.minimumShiftLength;
			int preferredShiftLength=editModel.preferredShiftLength;
			if (agents.preferredShiftLength>0) preferredShiftLength=agents.preferredShiftLength;
			if (minimumShiftLength>preferredShiftLength) return String.format(Language.tr("Model.Check.MinimalShiftLengthLongerThanPreferredShiftLength.AgentsGroup"),i+1,callcenter.name);
		}

		/* Skill-Level */
		if (skills.length==0) return Language.tr("Model.Check.SkillLevel.No");
		for (int i=0;i<skills.length;i++) {
			for (int j=0;j<i;j++) if (skills[i].name.equalsIgnoreCase(skills[j].name)) return String.format(Language.tr("Model.Check.SkillLevel.DoubleName"),skills[i].name);
			String s=skills[i].checkAndInit(caller,editModel.caller,strictCheck);
			if (s!=null) return s;
		}

		/* Kunden */
		if (caller.length==0) return Language.tr("Model.Check.ClientTypes.No");
		int freshCallsSum=0;
		for (int i=0;i<caller.length;i++) {
			CallcenterRunModelCaller c=caller[i];
			String name=caller[i].name;
			for (int j=0;j<i;j++) if (name.equalsIgnoreCase(caller[j].name)) return String.format(Language.tr("Model.Check.ClientTypes.DoubleName"),caller[i].name);
			String s=c.checkAndInit(caller,skills,strictCheck);
			if (s!=null) return s;
			freshCallsSum+=caller[i].freshCallsCountMean;
			if (c.freshCallsCountMean>0 && (c.freshCalls==null || c.freshCalls.sumIsZero())) return String.format(Language.tr("Model.Check.ClientTypes.NoDistribution"),caller[i].name);
			if (c.serviceLevelSeconds<=0) return String.format(Language.tr("Model.Check.ClientTypes.InvalidServiceLevelForClientType"),caller[i].name,caller[i].serviceLevelSeconds);
			if (c.freshCallsCountSD<0) return String.format(Language.tr("Model.Check.ClientTypes.InvalidStdDev"),caller[i].name,NumberTools.formatNumber(caller[i].freshCallsCountSD,1));
		}

		if (freshCallsSum==0) return Language.tr("Model.Check.ClientTypes.NoCalls");
		if (freshCallsSum>4194304) return String.format(Language.tr("Model.Check.ClientTypes.TooManyCalls"),NumberTools.formatLong(freshCallsSum));

		/* Callcenter */
		if (callcenter.length==0) return Language.tr("Model.Check.Callcenter.No");
		int agentSum=0;
		for (int i=0;i<callcenter.length;i++) {
			agentSum+=callcenter[i].agents.length;
			for (int j=0;j<i;j++) if (callcenter[i].name.equalsIgnoreCase(callcenter[j].name)) return String.format(Language.tr("Model.Check.Callcenter.DoubleName"),callcenter[i].name);
			String s=callcenter[i].checkAndInit(caller,skills,strictCheck);
			if (s!=null) return s;
		}
		if (agentSum==0) return Language.tr("Model.Check.Agents.No");

		/* Agentengruppen */
		for (int i=0;i<editModel.callcenter.size();i++) for (int j=0;j<editModel.callcenter.get(i).agents.size();j++) {
			CallcenterModelAgent a=editModel.callcenter.get(i).agents.get(j);

			/* Prüfen, ob die bei den Agentenkosten angegebene Kundengruppen existieren */
			if (strictCheck) for (int k=0;k<a.costCallerTypes.size();k++) {
				String callername=a.costCallerTypes.get(k);
				ok=false;
				for (int l=0;l<caller.length;l++) if (caller[l].name.equalsIgnoreCase(callername)) {ok=true; break;}
				if (!ok) return editModel.callcenter.get(i).name+": "+String.format(Language.tr("Model.Check.Agents.CostsForUnknownClientType"),j+1,callername);
			}

			if (a.count!=-2) continue;
			/* Prüfen, ob bei der Bestimmung der Agenten gemäß den Kundenankünften alle Typen korrekt existieren */
			String s=checkAgentByCaller(a,editModel.caller,strictCheck);
			if (s!=null) return editModel.callcenter.get(i).name+": "+s;
		}

		boolean b=false;
		for (int i=0;i<callcenter.length;i++) {
			CallcenterRunModelCallcenter c=callcenter[i];
			for (int j=0;j<c.agents.length;j++) {
				CallcenterRunModelAgent a=c.agents[j];
				if (a.costPerWorkingHour>0) {b=true; break;}
				if (a.costPerCall!=null) for (int k=0;k<a.costPerCall.length;k++) if (a.costPerCall[k]>0 || a.costPerCallMinute[k]>0) {b=true; break;}
				if (b) break;
			}
		}
		agentCostsUsed=b;

		/* Mindestwartezeiten (erst nach Callcenter.checkAndInit möglich) */
		if (callerMinWaitingTimeUsed) for (int i=0;i<caller.length;i++) for (int j=0;j<callcenter.length;j++) {
			CallcenterRunModelCaller ca=caller[i];
			CallcenterRunModelCallcenter cc=callcenter[j];
			int index=cc.callerMinWaitingTimeClass.indexOf(ca);
			if (index<0) continue;

			int time=cc.callerMinWaitingTimeMilliSecond.get(index);
			if (time<=0) continue;

			if (ca.recheckTimesMilliSecond==null) {
				ca.recheckTimesMilliSecond=new int[1];
				ca.recheckTimesMilliSecond[0]=time;
			} else {
				int[] old=ca.recheckTimesMilliSecond;
				ca.recheckTimesMilliSecond=new int[old.length+1];
				System.arraycopy(old,0,ca.recheckTimesMilliSecond,0,old.length);
				ca.recheckTimesMilliSecond[old.length]=time;
			}
		}

		return null;
	}

	/**
	 * Führt eine Plausibilitätsprüfung der Modellparameter durch.
	 * @return	Ergebnisse der Plausibilitätsprüfung (leerer String bedeutet keine Probleme)
	 */
	private String checkPlausibility() {
		final StringBuilder sb=new StringBuilder();

		/* Kundenanzahl bestimmen */
		int callerCount=0;
		int freshCallsCount=0;
		for (CallcenterModelCaller caller : editModel.caller) if (caller.active) {
			double forwardRate=caller.continueProbability;
			for (Double D: caller.continueTypeSkillTypeProbability) if (D!=null) forwardRate=Math.max(forwardRate,D);
			callerCount+=Math.round(caller.freshCallsCountMean*(1+forwardRate+forwardRate*forwardRate));
			freshCallsCount+=caller.freshCallsCountMean;
		}

		/* Bedienleistung bestimmen */
		int agentsHalfHourIntervals=0;
		int largestAgentsGroup=0;
		for (CallcenterModelCallcenter callcenter : editModel.callcenter) if (callcenter.active) for (CallcenterModelAgent agents : callcenter.agents) if (agents.active) {
			int groupSize=0;
			if (agents.count==-1) {
				if (agents.countPerInterval24!=null) groupSize=(int)(agents.countPerInterval24.sum()*2);
				if (agents.countPerInterval48!=null) groupSize=(int)agents.countPerInterval48.sum();
				if (agents.countPerInterval96!=null) groupSize=(int)(agents.countPerInterval96.sum()/2);
			} else {
				if (agents.count==-2) groupSize=agents.byCallersAvailableHalfhours; else groupSize=(int)Math.round((double)agents.count*(agents.workingTimeEnd-agents.workingTimeStart)/1800);
			}
			agentsHalfHourIntervals+=groupSize;
			largestAgentsGroup=Math.max(largestAgentsGroup,groupSize);
		}

		/* Höchste und niedrigste durchschnittliche Bediendauer bestimmen */
		double minAverageWorkingTime=86400;
		double maxAverageWorkingTime=0;
		for (CallcenterModelSkillLevel skillLevel : editModel.skills) for (int i=0;i<skillLevel.callerTypeName.size();i++) {
			double averageWorkingTime=DistributionTools.getMean(skillLevel.callerTypeWorkingTime.get(i))+DistributionTools.getMean(skillLevel.callerTypePostProcessingTime.get(i));
			minAverageWorkingTime=Math.min(minAverageWorkingTime,averageWorkingTime);
			maxAverageWorkingTime=Math.max(maxAverageWorkingTime,averageWorkingTime);
		}

		/* Plausibilitätstests */

		/* Unwahrscheinlich großes Modell */
		if (freshCallsCount>1000000) sb.append(String.format(Language.tr("Model.Plausibility.ManyFreshCalls"),NumberTools.formatLong(freshCallsCount))+"\n");
		if (agentsHalfHourIntervals>16*100000) sb.append(String.format(Language.tr("Model.Plausibility.ManyAgentHalfHourIntervals"),NumberTools.formatLong(agentsHalfHourIntervals))+"\n");
		if (largestAgentsGroup>200000) sb.append(String.format(Language.tr("Model.Plausibility.LargeAgentsGroup"),NumberTools.formatLong(largestAgentsGroup))+"\n");

		/* Sehr kurze oder sehr lange Bedienzeiten */
		if (maxAverageWorkingTime>3600) sb.append(Language.tr("Model.Plausibility.LongServiceTimes")+"\n");
		if (minAverageWorkingTime<5) sb.append(Language.tr("Model.Plausibility.ShortServiceTimes")+"\n");

		/* Zu viele oder zu wenige Agenten */
		if (10*callerCount*maxAverageWorkingTime/1800<agentsHalfHourIntervals) sb.append(Language.tr("Model.Plausibility.RelativelyManyAgents")+"\n");
		if (callerCount*minAverageWorkingTime/1800>10*agentsHalfHourIntervals) sb.append(Language.tr("Model.Plausibility.RelativelyManyCalls")+"\n");

		/* Geht die Wartezeit in die Berechnung des Score ein? */
		for (CallcenterModelCaller caller: editModel.caller) if (caller.active && Math.abs(caller.scoreSecond)<0.001) sb.append(String.format(Language.tr("Model.Plausibility.WaitingTimeNotUsedForScore"),caller.name)+"\n");

		/* Mindestschichtdauern */
		boolean b=(editModel.minimumShiftLength>=2);
		if (!b) for (CallcenterModelCallcenter callcenter : editModel.callcenter) if (callcenter.active) for (CallcenterModelAgent agent : callcenter.agents) if (agent.active && agent.count<0) {
			if (agent.minimumShiftLength>=2) {b=true; break;}
		}
		if (b) sb.append(Language.tr("Model.Plausibility.MinimumShiftLength")+"\n");

		return sb.toString();
	}

	/**
	 * Prüft ein Callcenter-Modell.
	 * @param editModel	Zu prüfendes Callcenter-Modell
	 * @return	Ergebnis der Prüfung (ist in jedem Fall ein nichtleerer String)
	 */
	public static String check(final CallcenterModel editModel) {
		CallcenterRunModel runModel;
		StringBuilder sb=new StringBuilder();
		String s;

		runModel=new CallcenterRunModel(editModel);
		s=runModel.checkAndInit(false,false,false);

		if (s!=null) sb.append(Language.tr("Model.Plausibility.Error")+"\n"+s);

		if (s==null) {
			runModel=new CallcenterRunModel(editModel);
			s=runModel.checkAndInit(false,false,true);
			if (s!=null) {
				sb.append(Language.tr("Model.Plausibility.ExtError")+"\n"+s);
				if (!SetupData.getSetup().strictCheck) sb.append("\n"+Language.tr("Model.Plausibility.ExtErrorIgnoreable"));
			}

			s=runModel.checkPlausibility();
			if (!s.isEmpty()) {
				if (sb.length()>0) sb.append("\n\n");
				sb.append(Language.tr("Model.Plausibility.Info")+"\n"+s);
			}
		}

		if (sb.length()==0) sb.append(Language.tr("Model.Plausibility.NoRemarks"));

		return sb.toString();
	}

	/**
	 * Liefert die Gesamtanzahl an (Laufzeit-)Agenten in dem Modell
	 * @return	Anzahl an Agenten in dem Modell
	 */
	public int getTotalNumberOfAgents() {
		int result=0;
		for (CallcenterRunModelCallcenter c: callcenter) result+=c.agents.length;
		return result;
	}

	/**
	 * Liefert die Gesamtanzahl an (Laufzeit-)Erstanrufern in dem Modell
	 * @return	Anzahl an Erstanrufern in dem Modell
	 */
	public int getTotalNumberOfFreshCalls() {
		int result=0;
		for (CallcenterRunModelCaller c: caller) result+=c.freshCallsCountMean;
		return result;
	}
}
