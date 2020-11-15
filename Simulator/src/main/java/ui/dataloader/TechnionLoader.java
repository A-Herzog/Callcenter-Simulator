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
package ui.dataloader;

import java.io.File;
import java.util.HashMap;
import java.util.Iterator;

import org.apache.commons.math3.distribution.AbstractRealDistribution;
import org.apache.commons.math3.distribution.ExponentialDistribution;

import language.Language;
import mathtools.NumberTools;
import mathtools.distribution.DataDistributionImpl;
import mathtools.distribution.LogNormalDistributionImpl;
import mathtools.distribution.OnePointDistributionImpl;
import ui.model.CallcenterModel;
import ui.model.CallcenterModelAgent;
import ui.model.CallcenterModelCallcenter;
import ui.model.CallcenterModelCaller;
import ui.model.CallcenterModelSkillLevel;

/**
 * Lädt Anruferzahlen aus einer Technion-Tabelle
 * @author Alexander Herzog
 * @see CallerTableLoader
 */
public final class TechnionLoader extends CallerTableLoader {
	/** Datum von dem die Daten geladen werden sollen */
	private final String dateToUse;

	/**
	 * Konstruktor der Klasse
	 * @param file	Dateiname der Datei aus der die Daten geladen werden sollen (darf nicht <code>null</code> sein)
	 * @param dateToUse	Datum von dem die Daten geladen werden sollen
	 */
	public TechnionLoader(final File file, final String dateToUse) {
		super(file,48);
		this.dateToUse=dateToUse;
		if (getLastError()==null) setColNamesToProcess(new String[]{"priority","type","date","vru_exit","q_time","outcome","ser_time"});
	}

	@Override
	protected boolean processRow(final String[] row) {
		String colDate, colType, colPriority, colStart, colQTime, colOutcome, colSerTime;
		colDate=row[2];
		colType=row[1];
		colPriority=row[0];
		colStart=row[3];
		colQTime=row[4];
		colOutcome=row[5];
		colSerTime=row[6];

		/* Filtern */
		if (!colDate.equalsIgnoreCase(dateToUse)) return true;
		if (!colOutcome.equalsIgnoreCase("HANG") && !colOutcome.equalsIgnoreCase("AGENT")) return true;

		/* Kundentyp-Datensatz finden */
		CallerData caller=getCallerDataForTypeName(colType+(colPriority.equals("2")?" ("+Language.tr("Loader.Priorized")+")":""));

		/* Daten erfassen */
		int interval=getIntervalFromTime(colStart);
		if (getLastError()!=null) return false;
		caller.freshCallsCount.densityData[interval]++;
		Integer i;
		if (colOutcome.equalsIgnoreCase("AGENT")) {
			i=NumberTools.getInteger(colQTime);
			if (i==null || i<0) {setLastError(String.format(Language.tr("Loader.ProcessError.InvalidWaitingTime"),colQTime)); return false;}
			caller.waitingTime.densityData[interval]+=i;
			i=NumberTools.getInteger(colSerTime);
			if (i==null || i<0) {setLastError(String.format(Language.tr("Loader.ProcessError.InvalidHoldingTime"),colSerTime)); return false;}
			caller.serviceTime.densityData[interval]+=i;
		} else {
			caller.cancelCount.densityData[interval]++;
			i=NumberTools.getInteger(colQTime);
			if (i==null || i<0) {setLastError(String.format(Language.tr("Loader.ProcessError.InvalidCancelTime"),colQTime)); return false;}
			caller.cancelTime.densityData[interval]+=i;
		}

		return true;
	}

	/**
	 * Fügt einen Skill zu einem Skill-Level hinzu.
	 * @param name	Kundentypname (neuer Skill)
	 * @param freshCalls	Anzahl der erfolgreichen Anrufe (zur Berechnung der Bedienzeiten)
	 * @param cancelCount	Anzahl der Warteabbrecher (zur Berechnung der Bedienzeiten)
	 * @param waitingTime	Wartezeiten (zur Berechnung der Bedienzeiten)
	 * @param serviceTime	Bedienzeiten (zur Berechnung der Bedienzeiten)
	 * @param model	Callcenter-Modell
	 * @param skill	Skill-Level
	 */
	private void addSkill(String name, HashMap<String,DataDistributionImpl> freshCalls, HashMap<String,DataDistributionImpl> cancelCount, HashMap<String,DataDistributionImpl> waitingTime, HashMap<String,DataDistributionImpl> serviceTime, CallcenterModel model, CallcenterModelSkillLevel skill) {
		skill.callerTypeName.add(name);

		DataDistributionImpl erfolg=freshCalls.get(name).sub(cancelCount.get(name)); /* Verteilung der erfolgreichen Kunden bestimmen */
		double sumWaitingTime=waitingTime.get(name).sum();
		double sumServiceTime=serviceTime.get(name).sum();
		double erfolgCount=erfolg.sum();
		double meanWaiting=(erfolgCount>0)?(sumWaitingTime/erfolgCount):0;
		double meanService=(erfolgCount>0)?(sumServiceTime/erfolgCount):0;

		model.description+="\n"+String.format(Language.tr("Loader.Technion.Model.ModelDescriptionAddOn"),name,NumberTools.formatNumber(meanWaiting));

		skill.callerTypeWorkingTimeAddOn.add("0");
		skill.callerTypeIntervalWorkingTimeAddOn.add(new String[48]);
		if (meanService==0) {
			model.description+="\n"+String.format(Language.tr("Loader.Technion.Model.ModelDescription.AttentionNoHoldingTimes"),name);
			skill.callerTypeWorkingTime.add(new LogNormalDistributionImpl(300,30));
			skill.callerTypeIntervalWorkingTime.add(new AbstractRealDistribution[48]);
		} else {
			skill.callerTypeWorkingTime.add(new LogNormalDistributionImpl(meanService,meanService));
			skill.callerTypeIntervalWorkingTime.add(new AbstractRealDistribution[48]);
		}
		skill.callerTypePostProcessingTime.add(new OnePointDistributionImpl(0));
		skill.callerTypeIntervalPostProcessingTime.add(new AbstractRealDistribution[48]);
		skill.callerTypeScore.add(1);
	}

	/**
	 * Liefert das auf Basis der geladenen Tabellendaten erstellte Callcenter-Modell.
	 * @return	Neues Callcenter-Modell
	 */
	public CallcenterModel getModel() {
		HashMap<String,DataDistributionImpl> freshCalls=getFreshCallsDistributions();
		HashMap<String,DataDistributionImpl> cancelCount=getCancelCountDistributions();
		HashMap<String,DataDistributionImpl> waitingTime=getWaitingTimeDistributions();
		HashMap<String,DataDistributionImpl> serviceTime=getServiceTimeDistributions();
		Iterator<String> it;

		/* Basisangaben */
		CallcenterModel model=new CallcenterModel(Language.tr("Loader.Technion.Model.ModelName"));
		model.description=Language.tr("Loader.Technion.Model.ModelDescription")+"\n";
		model.maxQueueLength="200";
		model.days=100;
		model.preferredShiftLength=16;

		/* Anlegen der Anrufergruppen */
		it=freshCalls.keySet().iterator();
		while (it.hasNext()) {
			String name=it.next();
			CallcenterModelCaller caller=new CallcenterModelCaller(name);
			caller.freshCallsDist48=freshCalls.get(name).clone();
			for (int i=0;i<14;i++) caller.freshCallsDist48.densityData[i]=0; /* Keine Agenten vor 7 Uhr */
			caller.freshCallsCountMean=(int) Math.round(caller.freshCallsDist48.sum());
			caller.scoreBase=(name.contains("("+Language.tr("Loader.Priorized")+")"))?90:0;
			caller.scoreSecond=1;
			caller.scoreContinued=50;
			caller.waitingTimeMode=CallcenterModelCaller.WAITING_TIME_MODE_SHORT;
			caller.waitingTimeDist=new LogNormalDistributionImpl(300,200);
			caller.retryTimeDist=new ExponentialDistribution(900);
			caller.retryProbabiltyAfterBlockedFirstRetry=0;
			caller.retryProbabiltyAfterBlocked=0;
			caller.retryProbabiltyAfterGiveUpFirstRetry=0;
			caller.retryProbabiltyAfterGiveUp=0;
			caller.continueProbability=0.0;
			caller.continueTypeName.clear();
			caller.continueTypeRate.clear();
			model.caller.add(caller);
		}

		/* Anlegen eines Callcenters */
		CallcenterModelCallcenter callcenter=new CallcenterModelCallcenter(Language.tr("Loader.Technion.Model.CallcenterName"));
		callcenter.technicalFreeTime=0;
		callcenter.score=1;
		callcenter.agentScoreFreeTimeSinceLastCall=0;
		callcenter.agentScoreFreeTimePart=1;
		model.callcenter.add(callcenter);

		/* Agenten der Agenten und zu Callcenter hinzufügen */
		CallcenterModelAgent agent;

		agent=new CallcenterModelAgent();
		agent.count=7; /* max 8 */
		agent.workingTimeStart=7*3600;
		agent.workingTimeEnd=24*3600;
		agent.skillLevel=Language.tr("Loader.Technion.Model.AgentsName.Default");
		callcenter.agents.add(agent);

		agent=new CallcenterModelAgent();
		agent.count=1; /* max 5 */
		agent.workingTimeStart=7*3600;
		agent.workingTimeEnd=24*3600;
		agent.skillLevel=Language.tr("Loader.Technion.Model.AgentsName.Internet");
		callcenter.agents.add(agent);

		/* Skill-Level anlegen */
		CallcenterModelSkillLevel skill;

		skill=new CallcenterModelSkillLevel(Language.tr("Loader.Technion.Model.AgentsName.Default"));
		it=freshCalls.keySet().iterator();
		while (it.hasNext()) {
			String name=it.next();
			if (name.startsWith("IN")) continue;
			addSkill(name,freshCalls,cancelCount,waitingTime,serviceTime,model,skill);
		}
		model.skills.add(skill);

		skill=new CallcenterModelSkillLevel(Language.tr("Loader.Technion.Model.AgentsName.Internet"));
		it=freshCalls.keySet().iterator();
		while (it.hasNext()) {
			String name=it.next();
			if (!name.startsWith("IN")) continue;
			addSkill(name,freshCalls,cancelCount,waitingTime,serviceTime,model,skill);
		}
		model.skills.add(skill);

		return model;
	}
}
