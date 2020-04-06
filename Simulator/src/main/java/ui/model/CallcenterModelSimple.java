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

import org.apache.commons.math3.distribution.AbstractRealDistribution;
import org.apache.commons.math3.distribution.ExponentialDistribution;

import mathtools.distribution.DataDistributionImpl;
import mathtools.distribution.LogNormalDistributionImpl;
import mathtools.distribution.NeverDistributionImpl;
import mathtools.distribution.OnePointDistributionImpl;
import ui.simplesimulation.SimpleSimulation;

/**
 * Diese Klasse stellt über statische Methoden ein Callcenter-Modell zur Schnellsimulation zur Verfügung.
 * @author Alexander Herzog
 * @version 1.0
 * @see SimpleSimulation
 */
public class CallcenterModelSimple {
	private CallcenterModelSimple() {}

	/**
	 * Erstellt auf Basis von warteschlangentheoretischen Werten
	 * ein Callcenter-Modell.
	 * @param lambda	Ankunftsrate
	 * @param EWT	Mittlere Wartezeittoleranz
	 * @param StdWT	Standardabweichung der Wartezeittoleranz
	 * @param retryProbability	Wiederholwahrscheinlichkeit
	 * @param ERetry Mittlerer Wiederholabstand
	 * @param c	Anzahl an Agenten
	 * @param ES	Mittlere Bediendauer
	 * @param StdS	Standardabweichung der Bediendauern
	 * @param ES2	Mittlere Nachbearbeitungszeit
	 * @param StdS2	Standardabweichung der Nachbearbeitungszeiten
	 * @param continueProbability	Weiterleitungswahrscheinlichkeiten
	 * @return	Neues Callcenter-Modell
	 */
	public static final CallcenterModel getModel(final double lambda, final double EWT, final double StdWT, final double retryProbability, final double ERetry, final int c, final double ES, final double StdS, final double ES2, final double StdS2, final double continueProbability) {
		CallcenterModel model=new CallcenterModel();

		model.name="";
		model.description="";

		model.maxQueueLength="1000";
		model.days=100;
		model.preferredShiftLength=48;

		DataDistributionImpl uniformDistribution=new DataDistributionImpl(86399,48);
		uniformDistribution.setToValue(1);

		CallcenterModelCaller caller;

		/* Anrufergruppe anlegen */
		caller=new CallcenterModelCaller("A");
		caller.freshCallsCountMean=(int)Math.round(lambda*60*24);
		caller.freshCallsDist48=uniformDistribution.clone();
		caller.scoreBase=0;
		caller.scoreSecond=1;
		caller.scoreContinued=0;

		if (EWT<=0) {
			caller.waitingTimeMode=CallcenterModelCaller.WAITING_TIME_MODE_OFF;
			caller.waitingTimeDist=new NeverDistributionImpl();
		} else {
			caller.waitingTimeMode=CallcenterModelCaller.WAITING_TIME_MODE_SHORT;
			caller.waitingTimeDist=new LogNormalDistributionImpl(300,200);
		}

		if (retryProbability>0) {
			caller.retryTimeDist=new ExponentialDistribution(ERetry*60);
			caller.retryProbabiltyAfterBlockedFirstRetry=retryProbability;
			caller.retryProbabiltyAfterBlocked=retryProbability;
			caller.retryProbabiltyAfterGiveUpFirstRetry=retryProbability;
			caller.retryProbabiltyAfterGiveUp=retryProbability;
		} else {
			caller.retryTimeDist=new ExponentialDistribution(900);
			caller.retryProbabiltyAfterBlockedFirstRetry=0;
			caller.retryProbabiltyAfterBlocked=0;
			caller.retryProbabiltyAfterGiveUpFirstRetry=0;
			caller.retryProbabiltyAfterGiveUp=0;
		}

		if (continueProbability>0) {
			caller.continueProbability=continueProbability;
		} else {
			caller.continueProbability=0;
		}
		caller.continueTypeName.clear();
		caller.continueTypeRate.clear();
		caller.continueTypeName.add("A");
		caller.continueTypeRate.add(1.0);
		model.caller.add(caller);

		/* Anlegen eines Callcenters */
		CallcenterModelCallcenter callcenter=new CallcenterModelCallcenter("");
		callcenter.technicalFreeTime=2;
		callcenter.score=1;
		callcenter.agentScoreFreeTimeSinceLastCall=0;
		callcenter.agentScoreFreeTimePart=1;
		model.callcenter.add(callcenter);

		/* Agenten anlegen und zu Callcenter hinzufügen */
		CallcenterModelAgent agent=new CallcenterModelAgent();
		agent.count=c;
		agent.workingTimeStart=0;
		agent.workingTimeEnd=86400;
		agent.lastShiftIsOpenEnd=true;
		agent.skillLevel="A";
		callcenter.agents.add(agent);

		/* Skill-Level anlegen */
		CallcenterModelSkillLevel skill=new CallcenterModelSkillLevel("A");
		skill.callerTypeName.add("A");
		skill.callerTypeWorkingTimeAddOn.add("0");
		skill.callerTypeWorkingTime.add(new LogNormalDistributionImpl(ES*60,StdS*60));
		if (ES2>0) {
			skill.callerTypePostProcessingTime.add(new LogNormalDistributionImpl(ES2*60,StdS2*60));
		} else {
			skill.callerTypePostProcessingTime.add(new OnePointDistributionImpl(0));
		}
		skill.callerTypeIntervalWorkingTimeAddOn.add(new String[48]);
		skill.callerTypeIntervalWorkingTime.add(new AbstractRealDistribution[48]);
		skill.callerTypeIntervalPostProcessingTime.add(new AbstractRealDistribution[48]);
		skill.callerTypeScore.add(1);
		model.skills.add(skill);

		model.prepareData();

		return model;
	}
}
