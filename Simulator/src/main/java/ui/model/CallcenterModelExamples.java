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

import org.apache.commons.math3.distribution.AbstractRealDistribution;
import org.apache.commons.math3.distribution.ExponentialDistribution;

import language.Language;
import mathtools.NumberTools;
import mathtools.distribution.DataDistributionImpl;
import mathtools.distribution.LogNormalDistributionImpl;
import mathtools.distribution.OnePointDistributionImpl;

/**
 * Diese Klasse stellt über statische Methoden verschiedene Callcenter-Beispiel-Modelle zur Verfügung.
 * @author Alexander Herzog
 * @version 1.0
 */
public class CallcenterModelExamples {
	/**
	 * Konstruktor der Klasse<br>
	 * Diese Klasse kann nicht instanziert werden,
	 * sie stellt lediglich statische Hilfsroutinen zur Verfügung.
	 */
	private CallcenterModelExamples() {}

	/**
	 * Erstellt ein leeres Callcenter-Modell.
	 * @return	Leeres Callcenter-Modell
	 */
	public static final CallcenterModel getEmpty() {
		CallcenterModel model=new CallcenterModel();

		model.name=Language.tr("Example.Empty.Title");
		model.description=Language.tr("Example.Empty.Description");

		model.maxQueueLength="1000";
		model.days=100;
		model.preferredShiftLength=48;

		model.prepareData();

		return model;
	}

	/**
	 * Erstellt ein Callcenter-Modell
	 * @param name	Name des Modells
	 * @param description	Beschreibung für das Modell
	 * @param callerDist	Verteilung der Kundenankünfte in der einzigen Kundengruppe über den Tag
	 * @param agentDist	Verteilung der verfügbaren Agenten über den Tag
	 * @return	Callcenter-Modell
	 */
	private static final CallcenterModel getExampleErlang(final String name, final String description, final DataDistributionImpl callerDist, final DataDistributionImpl agentDist) {
		CallcenterModel model=new CallcenterModel();
		model.name=name;
		model.description=description;

		model.maxQueueLength="1000";
		model.days=100;
		model.preferredShiftLength=48;

		CallcenterModelCaller caller;

		/* Anrufer */
		caller=new CallcenterModelCaller(Language.tr("Example.Erlang.Clients"));
		caller.freshCallsCountMean=(int)Math.round(callerDist.sum());
		caller.freshCallsDist48=callerDist;
		caller.scoreBase=0;
		caller.scoreSecond=1;
		caller.scoreContinued=0;
		caller.waitingTimeMode=CallcenterModelCaller.WAITING_TIME_MODE_SHORT;
		caller.waitingTimeDist=new ExponentialDistribution(900);
		caller.retryTimeDist=new ExponentialDistribution(900);
		caller.retryProbabiltyAfterBlockedFirstRetry=0;
		caller.retryProbabiltyAfterBlocked=0;
		caller.retryProbabiltyAfterGiveUpFirstRetry=0;
		caller.retryProbabiltyAfterGiveUp=0;
		caller.continueProbability=0;
		caller.continueTypeName.clear();
		caller.continueTypeRate.clear();
		caller.blocksLine=false;
		model.caller.add(caller);

		/* Anlegen eines Callcenters */
		CallcenterModelCallcenter callcenter=new CallcenterModelCallcenter(Language.tr("Example.Erlang.Callcenter"));
		callcenter.technicalFreeTime=0;
		callcenter.score=1;
		callcenter.agentScoreFreeTimeSinceLastCall=0;
		callcenter.agentScoreFreeTimePart=1;
		model.callcenter.add(callcenter);

		CallcenterModelAgent agent;

		/* Agenten vom Skill-Level A anlegen und zu Callcenter hinzufügen */
		agent=new CallcenterModelAgent();
		agent.count=-1;
		agent.countPerInterval48=agentDist;
		agent.countPerInterval48.updateCumulativeDensity();
		agent.lastShiftIsOpenEnd=false;
		agent.skillLevel=Language.tr("Example.Erlang.SkillLevel");
		callcenter.agents.add(agent);

		CallcenterModelSkillLevel skill;

		/* Skill-Level anlegen */
		skill=new CallcenterModelSkillLevel(Language.tr("Example.Erlang.SkillLevel"));
		skill.callerTypeName.add(Language.tr("Example.Erlang.Clients"));
		skill.callerTypeWorkingTimeAddOn.add("0");
		skill.callerTypeWorkingTime.add(new ExponentialDistribution(300));
		skill.callerTypePostProcessingTime.add(new OnePointDistributionImpl(0));
		skill.callerTypeIntervalWorkingTimeAddOn.add(new String[48]);
		skill.callerTypeIntervalWorkingTime.add(new AbstractRealDistribution[48]);
		skill.callerTypeIntervalPostProcessingTime.add(new AbstractRealDistribution[48]);
		skill.callerTypeScore.add(1);
		model.skills.add(skill);

		model.prepareData();

		return model;
	}

	/**
	 * Erstellt ein mit Erlang-C vergleichbares Callcenter-Modell
	 * @param name	Name des Modells
	 * @param callerCount	Anzahl an Anrufern pro Tag
	 * @param agentCount	Anzahl an dauerhaft verfügbaren Agenten
	 * @return	Callcenter-Modell
	 */
	private static final CallcenterModel getExampleErlang(final String name, final int callerCount, final int agentCount) {
		String description=String.format(Language.tr("Example.Exlang.Description"),NumberTools.formatLong(callerCount*48),NumberTools.formatNumber(((double)callerCount)/30),""+agentCount);

		double[] callerArray=new double[48];
		Arrays.fill(callerArray,callerCount);
		DataDistributionImpl callerDist=new DataDistributionImpl(86399,callerArray);
		double[] agentArray=new double[48];
		Arrays.fill(agentArray,agentCount);
		DataDistributionImpl agentDist=new DataDistributionImpl(86399,agentArray);

		return getExampleErlang(name,description,callerDist,agentDist);
	}

	/**
	 * Erstellt ein kleines Modell, welche durch Erlang-C beschrieben werden kann.
	 * @return	Kleines Erlang-C-beschreibbares Callcenter-Modell
	 */
	public static final CallcenterModel getExampleSmallErlang() {
		return getExampleErlang(Language.tr("Example.Small.Title")+Language.tr("Example.Exlang.TitleAddon"),39,8);
	}

	/**
	 * Erstellt ein mittelgroßes Modell, welche durch Erlang-C beschrieben werden kann.
	 * @return	Mittelgroßes Erlang-C-beschreibbares Callcenter-Modell
	 */
	public static final CallcenterModel getExampleMediumErlang() {
		CallcenterModel model=getExampleErlang(Language.tr("Example.Medium.Title")+Language.tr("Example.Exlang.TitleAddon"),570,100);
		DataDistributionImpl agents=model.callcenter.get(0).agents.get(0).countPerInterval48;
		agents.densityData[0]=95;
		agents.updateCumulativeDensity();
		return model;
	}

	/**
	 * Erstellt ein großes Modell, welche durch Erlang-C beschrieben werden kann.
	 * @return	Großes Erlang-C-beschreibbares Callcenter-Modell
	 */
	public static final CallcenterModel getExampleLargeErlang() {
		CallcenterModel model=getExampleErlang(Language.tr("Example.Large.Title")+Language.tr("Example.Exlang.TitleAddon"),3000,505);
		DataDistributionImpl agents=model.callcenter.get(0).agents.get(0).countPerInterval48;
		agents.densityData[0]=480;
		agents.updateCumulativeDensity();
		return model;
	}

	/**
	 * Erstellt ein kleines Callcenter-Modell.
	 * @return	Kleines Callcenter-Modell
	 */
	public static final CallcenterModel getExampleSmall() {
		CallcenterModel model=new CallcenterModel();

		/* Basisangaben */
		model.name=Language.tr("Example.Small.Title");
		model.description=Language.tr("Example.Small.Description");
		model.maxQueueLength="200";
		model.days=100;
		model.preferredShiftLength=16;

		double[] normalCaller={0,0,0,0,1,2,1,4,5,5,3,3, 5,7,10,10,15,17,25,50,60,65,65,62, 50,45,50,60,62,62,60,55,50,45,40,35, 35,40,40,37,35,30,25,15,10,8,5,2};
		double[] priorityCaller={0,0,0,0,0,0,0,1,2,2,1,2, 10,15,15,20,25,30,35,50,60,70,70,50, 40,40,60,70,70,65,60,55,55,25,15,10, 10,10,5,5,5,2,0,0,0,0,0,0};
		DataDistributionImpl normalCallerDist=new DataDistributionImpl(86399,normalCaller);
		DataDistributionImpl priorityCallerDist=new DataDistributionImpl(86399,priorityCaller);

		DataDistributionImpl singleAgentCount=normalCallerDist.divide(8).round().add(1);
		DataDistributionImpl multiAgentCount=normalCallerDist.add(priorityCallerDist).divide(15).round().add(1);

		CallcenterModelCaller caller;

		/* Anrufergruppe A anlegen */
		caller=new CallcenterModelCaller(String.format(Language.tr("Example.CallerGroupType"),"A"));
		caller.freshCallsCountMean=700;
		caller.freshCallsDist48=normalCallerDist.clone();
		caller.scoreBase=50;
		caller.scoreSecond=1;
		caller.scoreContinued=50;
		caller.waitingTimeMode=CallcenterModelCaller.WAITING_TIME_MODE_SHORT;
		caller.waitingTimeDist=new LogNormalDistributionImpl(300,200);
		caller.retryTimeDist=new ExponentialDistribution(900);
		caller.retryProbabiltyAfterBlockedFirstRetry=0.9;
		caller.retryProbabiltyAfterBlocked=0.8;
		caller.retryProbabiltyAfterGiveUpFirstRetry=0.9;
		caller.retryProbabiltyAfterGiveUp=0.8;
		caller.continueProbability=0.3;
		caller.continueTypeName.clear();
		caller.continueTypeRate.clear();
		caller.continueTypeName.add(String.format(Language.tr("Example.CallerGroupType"),"A"));
		caller.continueTypeName.add(String.format(Language.tr("Example.CallerGroupType"),"B"));
		caller.continueTypeRate.add(1.0);
		caller.continueTypeRate.add(9.0);
		model.caller.add(caller);

		/* Anrufergruppe B anlegen */
		caller=caller.clone();
		caller.name=String.format(Language.tr("Example.CallerGroupType"),"B");
		caller.freshCallsCountMean=800;
		caller.waitingTimeDist=new LogNormalDistributionImpl(200,150);
		caller.retryProbabiltyAfterBlockedFirstRetry=0.8;
		caller.retryProbabiltyAfterBlocked=0.5;
		caller.retryProbabiltyAfterGiveUpFirstRetry=0.8;
		caller.retryProbabiltyAfterGiveUp=0.5;
		caller.continueTypeName.clear();
		caller.continueTypeRate.clear();
		caller.continueTypeName.add(String.format(Language.tr("Example.CallerGroupType"),"A"));
		caller.continueTypeName.add(String.format(Language.tr("Example.CallerGroupType"),"B"));
		caller.continueTypeRate.add(8.0);
		caller.continueTypeRate.add(1.0);
		model.caller.add(caller);

		/* Anrufergruppe C anlegen */
		caller=new CallcenterModelCaller(Language.tr("Example.CallerGroupExtra"));
		caller.freshCallsCountMean=400;
		caller.freshCallsDist48=priorityCallerDist.clone();
		caller.scoreBase=200;
		caller.scoreSecond=5;
		caller.scoreContinued=150;
		caller.waitingTimeMode=CallcenterModelCaller.WAITING_TIME_MODE_SHORT;
		caller.waitingTimeDist=new LogNormalDistributionImpl(200,150);
		caller.retryTimeDist=new ExponentialDistribution(900);
		caller.retryProbabiltyAfterBlockedFirstRetry=0.5;
		caller.retryProbabiltyAfterBlocked=0.25;
		caller.retryProbabiltyAfterGiveUpFirstRetry=0.5;
		caller.retryProbabiltyAfterGiveUp=0.25;
		caller.continueProbability=0.05;
		caller.continueTypeName.clear();
		caller.continueTypeRate.clear();
		caller.continueTypeName.add(Language.tr("Example.CallerGroupExtra"));
		caller.continueTypeRate.add(1.0);
		model.caller.add(caller);

		/* Anlegen eines Callcenters */
		CallcenterModelCallcenter callcenter=new CallcenterModelCallcenter(String.format(Language.tr("Example.Callcenter"),"X"));
		callcenter.technicalFreeTime=2;
		callcenter.score=1;
		callcenter.agentScoreFreeTimeSinceLastCall=0;
		callcenter.agentScoreFreeTimePart=1;
		model.callcenter.add(callcenter);

		CallcenterModelAgent agent;

		/* Agenten vom Skill-Level A anlegen und zu Callcenter hinzufügen */
		agent=new CallcenterModelAgent();
		agent.count=-1;
		agent.countPerInterval48=singleAgentCount.clone();
		agent.countPerInterval48.updateCumulativeDensity();
		agent.lastShiftIsOpenEnd=true;
		agent.skillLevel=String.format(Language.tr("Example.SkillLevel"),"A");
		callcenter.agents.add(agent);

		/* Agenten vom Skill-Level B anlegen und zu Callcenter hinzufügen */
		agent=new CallcenterModelAgent();
		agent.count=-1;
		agent.countPerInterval48=singleAgentCount.clone();
		agent.countPerInterval48.updateCumulativeDensity();
		agent.lastShiftIsOpenEnd=true;
		agent.skillLevel=String.format(Language.tr("Example.SkillLevel"),"B");
		callcenter.agents.add(agent);

		/* Multi-Skill Agenten anlegen und zu Callcenter hinzufügen */
		agent=new CallcenterModelAgent();
		agent.count=-1;
		agent.countPerInterval48=multiAgentCount.clone();
		agent.countPerInterval48.updateCumulativeDensity();
		agent.lastShiftIsOpenEnd=true;
		agent.skillLevel=String.format(Language.tr("Example.MultiSkill"),"A+B");
		callcenter.agents.add(agent);

		CallcenterModelSkillLevel skill;

		/* Skill-Level A anlegen */
		skill=new CallcenterModelSkillLevel(String.format(Language.tr("Example.SkillLevel"),"A"));
		skill.callerTypeName.add(String.format(Language.tr("Example.CallerGroupType"),"A"));
		skill.callerTypeWorkingTimeAddOn.add("0");
		skill.callerTypeWorkingTime.add(new LogNormalDistributionImpl(300,30));
		skill.callerTypePostProcessingTime.add(new LogNormalDistributionImpl(75,30));
		skill.callerTypeIntervalWorkingTimeAddOn.add(new String[48]);
		skill.callerTypeIntervalWorkingTime.add(new AbstractRealDistribution[48]);
		skill.callerTypeIntervalPostProcessingTime.add(new AbstractRealDistribution[48]);
		skill.callerTypeScore.add(1);
		skill.callerTypeName.add(Language.tr("Example.CallerGroupExtra"));
		skill.callerTypeWorkingTimeAddOn.add("0");
		skill.callerTypeWorkingTime.add(new LogNormalDistributionImpl(300,30));
		skill.callerTypePostProcessingTime.add(new LogNormalDistributionImpl(75,30));
		skill.callerTypeIntervalWorkingTimeAddOn.add(new String[48]);
		skill.callerTypeIntervalWorkingTime.add(new AbstractRealDistribution[48]);
		skill.callerTypeIntervalPostProcessingTime.add(new AbstractRealDistribution[48]);
		skill.callerTypeScore.add(10);

		model.skills.add(skill);

		/* Skill-Level B anlegen */
		skill=new CallcenterModelSkillLevel(String.format(Language.tr("Example.SkillLevel"),"B"));
		skill.callerTypeName.add(String.format(Language.tr("Example.CallerGroupType"),"B"));
		skill.callerTypeWorkingTimeAddOn.add("0");
		skill.callerTypeWorkingTime.add(new LogNormalDistributionImpl(300,60));
		skill.callerTypePostProcessingTime.add(new LogNormalDistributionImpl(75,60));
		skill.callerTypeIntervalWorkingTimeAddOn.add(new String[48]);
		skill.callerTypeIntervalWorkingTime.add(new AbstractRealDistribution[48]);
		skill.callerTypeIntervalPostProcessingTime.add(new AbstractRealDistribution[48]);
		skill.callerTypeScore.add(1);
		skill.callerTypeName.add(Language.tr("Example.CallerGroupExtra"));
		skill.callerTypeWorkingTimeAddOn.add("0");
		skill.callerTypeWorkingTime.add(new LogNormalDistributionImpl(300,60));
		skill.callerTypePostProcessingTime.add(new LogNormalDistributionImpl(75,60));
		skill.callerTypeIntervalWorkingTimeAddOn.add(new String[48]);
		skill.callerTypeIntervalWorkingTime.add(new AbstractRealDistribution[48]);
		skill.callerTypeIntervalPostProcessingTime.add(new AbstractRealDistribution[48]);
		skill.callerTypeScore.add(10);
		model.skills.add(skill);

		/* Multi-Skill Skill-Level anlegen */
		skill=new CallcenterModelSkillLevel(String.format(Language.tr("Example.MultiSkill"),"A+B"));
		skill.callerTypeName.add(String.format(Language.tr("Example.CallerGroupType"),"A"));
		skill.callerTypeWorkingTimeAddOn.add("0");
		skill.callerTypeWorkingTime.add(new LogNormalDistributionImpl(480,30));
		skill.callerTypePostProcessingTime.add(new LogNormalDistributionImpl(90,30));
		skill.callerTypeIntervalWorkingTimeAddOn.add(new String[48]);
		skill.callerTypeIntervalWorkingTime.add(new AbstractRealDistribution[48]);
		skill.callerTypeIntervalPostProcessingTime.add(new AbstractRealDistribution[48]);
		skill.callerTypeScore.add(1);
		skill.callerTypeName.add(String.format(Language.tr("Example.CallerGroupType"),"B"));
		skill.callerTypeWorkingTimeAddOn.add("0");
		skill.callerTypeWorkingTime.add(new LogNormalDistributionImpl(480,60));
		skill.callerTypePostProcessingTime.add(new LogNormalDistributionImpl(90,60));
		skill.callerTypeIntervalWorkingTimeAddOn.add(new String[48]);
		skill.callerTypeIntervalWorkingTime.add(new AbstractRealDistribution[48]);
		skill.callerTypeIntervalPostProcessingTime.add(new AbstractRealDistribution[48]);
		skill.callerTypeScore.add(1);
		skill.callerTypeName.add(Language.tr("Example.CallerGroupExtra"));
		skill.callerTypeWorkingTimeAddOn.add("0");
		skill.callerTypeWorkingTime.add(new LogNormalDistributionImpl(480,120));
		skill.callerTypePostProcessingTime.add(new LogNormalDistributionImpl(90,120));
		skill.callerTypeIntervalWorkingTimeAddOn.add(new String[48]);
		skill.callerTypeIntervalWorkingTime.add(new AbstractRealDistribution[48]);
		skill.callerTypeIntervalPostProcessingTime.add(new AbstractRealDistribution[48]);
		skill.callerTypeScore.add(10);
		model.skills.add(skill);

		model.prepareData();

		return model;
	}

	/**
	 * Erstellt ein mittelgroßes Callcenter-Modell.
	 * @return	Mittelgroßes Callcenter-Modell
	 */
	public static final CallcenterModel getExampleMedium() {
		CallcenterModel model=new CallcenterModel();

		/* Basisangaben */
		model.name=Language.tr("Example.Medium.Title");
		model.description=Language.tr("Example.Medium.Description");
		model.maxQueueLength="200";
		model.days=100;
		model.preferredShiftLength=16;

		double[] normalCallerData={0,0,0,0,1,2,1,4,5,5,3,3, 5,7,10,10,15,17,25,50,60,65,65,62, 50,45,50,60,62,62,60,55,50,45,40,35, 35,40,40,37,35,30,25,15,10,8,5,2};
		double[] priorityCallerData={0,0,0,0,0,0,0,1,2,2,1,2, 10,15,15,20,25,30,35,50,60,70,70,50, 40,40,60,70,70,65,60,55,55,25,15,10, 10,10,5,5,5,2,0,0,0,0,0,0};

		DataDistributionImpl normalCallerDist=new DataDistributionImpl(86399,normalCallerData);
		DataDistributionImpl priorityCallerDist=new DataDistributionImpl(86399,priorityCallerData);

		DataDistributionImpl singleAgentACount=normalCallerDist.multiply(2.5).ceil().add(1);
		DataDistributionImpl singleAgentBCount=normalCallerDist.multiply(2.5).ceil().add(1);
		DataDistributionImpl multiAgentCount=normalCallerDist.multiply(1.5).ceil().add(1);

		/* === Anrufer === */
		CallcenterModelCaller caller;

		/* Basisdaten für normale Anrufer */
		caller=new CallcenterModelCaller("");
		caller.freshCallsDist48=normalCallerDist.clone();
		caller.scoreBase=50;
		caller.scoreSecond=1;
		caller.scoreContinued=50;
		caller.waitingTimeMode=CallcenterModelCaller.WAITING_TIME_MODE_SHORT;
		caller.waitingTimeDist=new LogNormalDistributionImpl(300,200);
		caller.retryTimeDist=new ExponentialDistribution(900);
		caller.retryProbabiltyAfterBlockedFirstRetry=0.9;
		caller.retryProbabiltyAfterBlocked=0.8;
		caller.retryProbabiltyAfterGiveUpFirstRetry=0.9;
		caller.retryProbabiltyAfterGiveUp=0.8;
		CallcenterModelCaller normalCaller=caller;

		/* Normale Kunden (Thema A) anlegen */
		caller=normalCaller.clone();
		caller.name=String.format(Language.tr("Example.CallerGroupType"),"A");
		caller.freshCallsCountMean=10000;
		caller.continueProbability=0.1;
		caller.continueTypeName.add(String.format(Language.tr("Example.CallerGroupType"),"A"));
		caller.continueTypeName.add(String.format(Language.tr("Example.CallerGroupType"),"B"));
		caller.continueTypeRate.add(1.0);
		caller.continueTypeRate.add(9.0);
		model.caller.add(caller);

		/* Normale Kunden (Thema B) anlegen */
		caller=normalCaller.clone();
		caller.name=String.format(Language.tr("Example.CallerGroupType"),"B");
		caller.freshCallsCountMean=8000;
		caller.continueProbability=0.08;
		caller.continueTypeName.add(String.format(Language.tr("Example.CallerGroupType"),"A"));
		caller.continueTypeName.add(String.format(Language.tr("Example.CallerGroupType"),"B"));
		caller.continueTypeRate.add(8.0);
		caller.continueTypeRate.add(1.0);
		model.caller.add(caller);

		/* Basisdaten für priorisierte Anrufer */
		caller=new CallcenterModelCaller("");
		caller.freshCallsDist48=priorityCallerDist.clone();
		caller.scoreBase=100;
		caller.scoreSecond=5;
		caller.scoreContinued=150;
		caller.waitingTimeMode=CallcenterModelCaller.WAITING_TIME_MODE_SHORT;
		caller.waitingTimeDist=new LogNormalDistributionImpl(200,150);
		caller.retryTimeDist=new ExponentialDistribution(900);
		caller.retryProbabiltyAfterBlockedFirstRetry=0.5;
		caller.retryProbabiltyAfterBlocked=0.25;
		caller.retryProbabiltyAfterGiveUpFirstRetry=0.5;
		caller.retryProbabiltyAfterGiveUp=0.25;
		CallcenterModelCaller priorityCaller=caller;

		/* Priorisierte Kunden (Thema A) anlegen */
		caller=priorityCaller.clone();
		caller.name=String.format(Language.tr("Example.CallerGroupExtraType"),"A");
		caller.freshCallsCountMean=7000;
		caller.continueProbability=0.1;
		caller.continueTypeName.add(String.format(Language.tr("Example.CallerGroupExtraType"),"A"));
		caller.continueTypeName.add(String.format(Language.tr("Example.CallerGroupExtraType"),"B"));
		caller.continueTypeRate.add(1.0);
		caller.continueTypeRate.add(9.0);
		model.caller.add(caller);

		/* Priorisierte Kunden (Thema B) anlegen */
		caller=priorityCaller.clone();
		caller.name=String.format(Language.tr("Example.CallerGroupExtraType"),"B");
		caller.freshCallsCountMean=5000;
		caller.continueProbability=0.08;
		caller.continueTypeName.add(String.format(Language.tr("Example.CallerGroupExtraType"),"A"));
		caller.continueTypeName.add(String.format(Language.tr("Example.CallerGroupExtraType"),"B"));
		caller.continueTypeRate.add(8.0);
		caller.continueTypeRate.add(1.0);
		model.caller.add(caller);

		/* === Skill-Level === */
		CallcenterModelSkillLevel skill;

		/* Skill-Level A anlegen */
		skill=new CallcenterModelSkillLevel(String.format(Language.tr("Example.SkillLevel"),"A"));
		skill.callerTypeName.add(String.format(Language.tr("Example.CallerGroupType"),"A"));
		skill.callerTypeName.add(String.format(Language.tr("Example.CallerGroupExtraType"),"A"));
		skill.callerTypeWorkingTimeAddOn.add("0");
		skill.callerTypeWorkingTimeAddOn.add("0");
		skill.callerTypeWorkingTime.add(new LogNormalDistributionImpl(300,30));
		skill.callerTypeWorkingTime.add(new LogNormalDistributionImpl(300,30));
		skill.callerTypePostProcessingTime.add(new LogNormalDistributionImpl(75,30));
		skill.callerTypePostProcessingTime.add(new LogNormalDistributionImpl(75,30));
		skill.callerTypeIntervalWorkingTimeAddOn.add(new String[48]);
		skill.callerTypeIntervalWorkingTimeAddOn.add(new String[48]);
		skill.callerTypeIntervalWorkingTime.add(new AbstractRealDistribution[48]);
		skill.callerTypeIntervalWorkingTime.add(new AbstractRealDistribution[48]);
		skill.callerTypeIntervalPostProcessingTime.add(new AbstractRealDistribution[48]);
		skill.callerTypeIntervalPostProcessingTime.add(new AbstractRealDistribution[48]);
		skill.callerTypeScore.add(1);
		skill.callerTypeScore.add(10);
		model.skills.add(skill);

		/* Skill-Level B anlegen */
		skill=new CallcenterModelSkillLevel(String.format(Language.tr("Example.SkillLevel"),"B"));
		skill.callerTypeName.add(String.format(Language.tr("Example.CallerGroupType"),"B"));
		skill.callerTypeName.add(String.format(Language.tr("Example.CallerGroupExtraType"),"B"));
		skill.callerTypeWorkingTimeAddOn.add("0");
		skill.callerTypeWorkingTimeAddOn.add("0");
		skill.callerTypeWorkingTime.add(new LogNormalDistributionImpl(300,60));
		skill.callerTypeWorkingTime.add(new LogNormalDistributionImpl(300,60));
		skill.callerTypePostProcessingTime.add(new LogNormalDistributionImpl(75,60));
		skill.callerTypePostProcessingTime.add(new LogNormalDistributionImpl(75,60));
		skill.callerTypeIntervalWorkingTimeAddOn.add(new String[48]);
		skill.callerTypeIntervalWorkingTimeAddOn.add(new String[48]);
		skill.callerTypeIntervalWorkingTime.add(new AbstractRealDistribution[48]);
		skill.callerTypeIntervalWorkingTime.add(new AbstractRealDistribution[48]);
		skill.callerTypeIntervalPostProcessingTime.add(new AbstractRealDistribution[48]);
		skill.callerTypeIntervalPostProcessingTime.add(new AbstractRealDistribution[48]);
		skill.callerTypeScore.add(1);
		skill.callerTypeScore.add(10);
		model.skills.add(skill);

		/* Multi-Skill Skill-Level anlegen */
		skill=new CallcenterModelSkillLevel(String.format(Language.tr("Example.MultiSkill"),"A+B"));
		skill.callerTypeName.add(String.format(Language.tr("Example.CallerGroupType"),"A"));
		skill.callerTypeName.add(String.format(Language.tr("Example.CallerGroupExtraType"),"A"));
		skill.callerTypeWorkingTimeAddOn.add("0");
		skill.callerTypeWorkingTimeAddOn.add("0");
		skill.callerTypeWorkingTime.add(new LogNormalDistributionImpl(480,30));
		skill.callerTypeWorkingTime.add(new LogNormalDistributionImpl(480,30));
		skill.callerTypePostProcessingTime.add(new LogNormalDistributionImpl(90,30));
		skill.callerTypePostProcessingTime.add(new LogNormalDistributionImpl(90,30));
		skill.callerTypeIntervalWorkingTimeAddOn.add(new String[48]);
		skill.callerTypeIntervalWorkingTimeAddOn.add(new String[48]);
		skill.callerTypeIntervalWorkingTime.add(new AbstractRealDistribution[48]);
		skill.callerTypeIntervalWorkingTime.add(new AbstractRealDistribution[48]);
		skill.callerTypeIntervalPostProcessingTime.add(new AbstractRealDistribution[48]);
		skill.callerTypeIntervalPostProcessingTime.add(new AbstractRealDistribution[48]);
		skill.callerTypeScore.add(1);
		skill.callerTypeScore.add(10);
		skill.callerTypeName.add(String.format(Language.tr("Example.CallerGroupType"),"B"));
		skill.callerTypeName.add(String.format(Language.tr("Example.CallerGroupExtraType"),"B"));
		skill.callerTypeWorkingTimeAddOn.add("0");
		skill.callerTypeWorkingTimeAddOn.add("0");
		skill.callerTypeWorkingTime.add(new LogNormalDistributionImpl(480,60));
		skill.callerTypeWorkingTime.add(new LogNormalDistributionImpl(480,60));
		skill.callerTypePostProcessingTime.add(new LogNormalDistributionImpl(90,60));
		skill.callerTypePostProcessingTime.add(new LogNormalDistributionImpl(90,60));
		skill.callerTypeIntervalWorkingTimeAddOn.add(new String[48]);
		skill.callerTypeIntervalWorkingTimeAddOn.add(new String[48]);
		skill.callerTypeIntervalWorkingTime.add(new AbstractRealDistribution[48]);
		skill.callerTypeIntervalWorkingTime.add(new AbstractRealDistribution[48]);
		skill.callerTypeIntervalPostProcessingTime.add(new AbstractRealDistribution[48]);
		skill.callerTypeIntervalPostProcessingTime.add(new AbstractRealDistribution[48]);
		skill.callerTypeScore.add(1);
		skill.callerTypeScore.add(10);
		model.skills.add(skill);

		/* === Callcenter === */
		CallcenterModelCallcenter callcenter;
		CallcenterModelAgent agent;

		/* Basisdaten für alle Callcenter */
		callcenter=new CallcenterModelCallcenter("");
		callcenter.technicalFreeTime=2;
		callcenter.agentScoreFreeTimeSinceLastCall=0;
		callcenter.agentScoreFreeTimePart=1;
		CallcenterModelCallcenter callcenterTemplate=callcenter;

		/* Callcenters 1 (Single-Skill A) */
		callcenter=callcenterTemplate.clone();
		callcenter.name=String.format(Language.tr("Example.CallcenterType"),"A");
		callcenter.score=10;
		model.callcenter.add(callcenter);

		/* Agenten vom Skill-Level A anlegen und zu Callcenter hinzufügen */
		agent=new CallcenterModelAgent();
		agent.count=-1;
		agent.countPerInterval48=singleAgentACount.clone();
		agent.countPerInterval48.updateCumulativeDensity();
		agent.lastShiftIsOpenEnd=true;
		agent.skillLevel=String.format(Language.tr("Example.SkillLevel"),"A");
		callcenter.agents.add(agent);

		/* Callcenters 2 (Single-Skill B) */
		callcenter=callcenterTemplate.clone();
		callcenter.name=String.format(Language.tr("Example.CallcenterType"),"B");
		callcenter.score=10;
		model.callcenter.add(callcenter);

		/* Agenten vom Skill-Level B anlegen und zu Callcenter hinzufügen */
		agent=new CallcenterModelAgent();
		agent.count=-1;
		agent.countPerInterval48=singleAgentBCount.clone();
		agent.countPerInterval48.updateCumulativeDensity();
		agent.lastShiftIsOpenEnd=true;
		agent.skillLevel=String.format(Language.tr("Example.SkillLevel"),"B");
		callcenter.agents.add(agent);

		/* Callcenters 3 (Multi-Skill A+B) */
		callcenter=callcenterTemplate.clone();
		callcenter.name=Language.tr("Example.CallcenterTypeAll");
		callcenter.score=1;
		model.callcenter.add(callcenter);

		/* Multi-Skill Agenten anlegen und zu Callcenter hinzufügen */
		agent=new CallcenterModelAgent();
		agent.count=-1;
		agent.countPerInterval48=multiAgentCount.clone();
		agent.countPerInterval48.updateCumulativeDensity();
		agent.lastShiftIsOpenEnd=true;
		agent.skillLevel=String.format(Language.tr("Example.MultiSkill"),"A+B");
		callcenter.agents.add(agent);

		model.prepareData();

		return model;
	}

	/**
	 * Erstellt ein großes Callcenter-Modell.
	 * @return	Großes Callcenter-Modell
	 */
	public static final CallcenterModel getExampleLarge() {
		CallcenterModel model=new CallcenterModel();

		/* Basisangaben */
		model.name=Language.tr("Example.Large.Title");
		model.description=Language.tr("Example.Large.Description");
		model.maxQueueLength="200";
		model.days=100;
		model.preferredShiftLength=16;

		double[] normalCallerData={0,0,0,0,1,2,1,4,5,5,3,3, 5,7,10,10,15,17,25,50,60,65,65,62, 50,45,50,60,62,62,60,55,50,45,40,35, 35,40,40,37,35,30,25,15,10,8,5,2};
		double[] priorityCallerData={0,0,0,0,0,0,0,1,2,2,1,2, 10,15,15,20,25,30,35,50,60,70,70,50, 40,40,60,70,70,65,60,55,55,25,15,10, 10,10,5,5,5,2,0,0,0,0,0,0};

		DataDistributionImpl normalCallerDist=new DataDistributionImpl(86399,normalCallerData);
		DataDistributionImpl priorityCallerDist=new DataDistributionImpl(86399,priorityCallerData);

		DataDistributionImpl singleAgentACount=normalCallerDist.multiply(4).ceil().add(1);
		DataDistributionImpl singleAgentBCount=normalCallerDist.multiply(4.5).ceil().add(1);
		DataDistributionImpl multiAgentCDCount=normalCallerDist.multiply(4.75).ceil().add(1);
		DataDistributionImpl multiAgentABCDCount=normalCallerDist.multiply(1.75).ceil().add(1);

		/* === Anrufer === */
		CallcenterModelCaller caller;

		/* Basisdaten für normale Anrufer */
		caller=new CallcenterModelCaller("");
		caller.freshCallsDist48=normalCallerDist.clone();
		caller.scoreBase=50;
		caller.scoreSecond=1;
		caller.scoreContinued=50;
		caller.waitingTimeMode=CallcenterModelCaller.WAITING_TIME_MODE_SHORT;
		caller.waitingTimeDist=new LogNormalDistributionImpl(300,200);
		caller.retryTimeDist=new ExponentialDistribution(900);
		caller.retryProbabiltyAfterBlockedFirstRetry=0.9;
		caller.retryProbabiltyAfterBlocked=0.8;
		caller.retryProbabiltyAfterGiveUpFirstRetry=0.9;
		caller.retryProbabiltyAfterGiveUp=0.8;
		CallcenterModelCaller normalCaller=caller;

		/* Normale Kunden (Thema A) anlegen */
		caller=normalCaller.clone();
		caller.name=String.format(Language.tr("Example.CallerGroupType"),"A");
		caller.freshCallsCountMean=30000;
		caller.continueProbability=0.1;
		caller.continueTypeName.add(String.format(Language.tr("Example.CallerGroupType"),"A"));
		caller.continueTypeName.add(String.format(Language.tr("Example.CallerGroupType"),"B"));
		caller.continueTypeName.add(String.format(Language.tr("Example.CallerGroupType"),"C"));
		caller.continueTypeName.add(String.format(Language.tr("Example.CallerGroupType"),"D"));
		caller.continueTypeRate.add(1.0);
		caller.continueTypeRate.add(9.0);
		caller.continueTypeRate.add(2.0);
		caller.continueTypeRate.add(2.0);
		model.caller.add(caller);

		/* Normale Kunden (Thema B) anlegen */
		caller=normalCaller.clone();
		caller.name=String.format(Language.tr("Example.CallerGroupType"),"B");
		caller.freshCallsCountMean=17000;
		caller.continueProbability=0.08;
		caller.continueTypeName.add(String.format(Language.tr("Example.CallerGroupType"),"A"));
		caller.continueTypeName.add(String.format(Language.tr("Example.CallerGroupType"),"B"));
		caller.continueTypeName.add(String.format(Language.tr("Example.CallerGroupType"),"C"));
		caller.continueTypeName.add(String.format(Language.tr("Example.CallerGroupType"),"D"));
		caller.continueTypeRate.add(8.0);
		caller.continueTypeRate.add(1.0);
		caller.continueTypeRate.add(2.0);
		caller.continueTypeRate.add(2.0);
		model.caller.add(caller);

		/* Normale Kunden (Thema C) anlegen */
		caller=normalCaller.clone();
		caller.name=String.format(Language.tr("Example.CallerGroupType"),"C");
		caller.freshCallsCountMean=15000;
		caller.continueProbability=0.12;
		caller.continueTypeName.add(String.format(Language.tr("Example.CallerGroupType"),"A"));
		caller.continueTypeName.add(String.format(Language.tr("Example.CallerGroupType"),"B"));
		caller.continueTypeName.add(String.format(Language.tr("Example.CallerGroupType"),"C"));
		caller.continueTypeName.add(String.format(Language.tr("Example.CallerGroupType"),"D"));
		caller.continueTypeRate.add(2.0);
		caller.continueTypeRate.add(2.0);
		caller.continueTypeRate.add(1.0);
		caller.continueTypeRate.add(7.0);
		model.caller.add(caller);

		/* Normale Kunden (Thema D) anlegen */
		caller=normalCaller.clone();
		caller.name=String.format(Language.tr("Example.CallerGroupType"),"D");
		caller.freshCallsCountMean=8000;
		caller.continueProbability=0.05;
		caller.continueTypeName.add(String.format(Language.tr("Example.CallerGroupType"),"A"));
		caller.continueTypeName.add(String.format(Language.tr("Example.CallerGroupType"),"B"));
		caller.continueTypeName.add(String.format(Language.tr("Example.CallerGroupType"),"C"));
		caller.continueTypeName.add(String.format(Language.tr("Example.CallerGroupType"),"D"));
		caller.continueTypeRate.add(2.0);
		caller.continueTypeRate.add(2.0);
		caller.continueTypeRate.add(10.0);
		caller.continueTypeRate.add(1.0);
		model.caller.add(caller);

		/* Basisdaten für priorisierte Anrufer */
		caller=new CallcenterModelCaller("");
		caller.freshCallsDist48=priorityCallerDist.clone();
		caller.scoreBase=100;
		caller.scoreSecond=5;
		caller.scoreContinued=150;
		caller.waitingTimeMode=CallcenterModelCaller.WAITING_TIME_MODE_SHORT;
		caller.waitingTimeDist=new LogNormalDistributionImpl(200,150);
		caller.retryTimeDist=new ExponentialDistribution(900);
		caller.retryProbabiltyAfterBlockedFirstRetry=0.5;
		caller.retryProbabiltyAfterBlocked=0.25;
		caller.retryProbabiltyAfterGiveUpFirstRetry=0.5;
		caller.retryProbabiltyAfterGiveUp=0.25;
		CallcenterModelCaller priorityCaller=caller;

		/* Priorisierte Kunden (Thema A) anlegen */
		caller=priorityCaller.clone();
		caller.name=String.format(Language.tr("Example.CallerGroupExtraType"),"A");
		caller.freshCallsCountMean=14000;
		caller.continueProbability=0.1;
		caller.continueTypeName.add(String.format(Language.tr("Example.CallerGroupExtraType"),"A"));
		caller.continueTypeName.add(String.format(Language.tr("Example.CallerGroupExtraType"),"B"));
		caller.continueTypeName.add(String.format(Language.tr("Example.CallerGroupExtraType"),"C"));
		caller.continueTypeName.add(String.format(Language.tr("Example.CallerGroupExtraType"),"D"));
		caller.continueTypeRate.add(1.0);
		caller.continueTypeRate.add(9.0);
		caller.continueTypeRate.add(3.0);
		caller.continueTypeRate.add(2.0);
		model.caller.add(caller);

		/* Priorisierte Kunden (Thema B) anlegen */
		caller=priorityCaller.clone();
		caller.name=String.format(Language.tr("Example.CallerGroupExtraType"),"B");
		caller.freshCallsCountMean=8000;
		caller.continueProbability=0.08;
		caller.continueTypeName.add(String.format(Language.tr("Example.CallerGroupExtraType"),"A"));
		caller.continueTypeName.add(String.format(Language.tr("Example.CallerGroupExtraType"),"B"));
		caller.continueTypeName.add(String.format(Language.tr("Example.CallerGroupExtraType"),"C"));
		caller.continueTypeName.add(String.format(Language.tr("Example.CallerGroupExtraType"),"D"));
		caller.continueTypeRate.add(8.0);
		caller.continueTypeRate.add(1.0);
		caller.continueTypeRate.add(2.0);
		caller.continueTypeRate.add(3.0);
		model.caller.add(caller);

		/* Priorisierte Kunden (Thema C) anlegen */
		caller=priorityCaller.clone();
		caller.name=String.format(Language.tr("Example.CallerGroupExtraType"),"C");
		caller.freshCallsCountMean=5000;
		caller.continueProbability=0.10;
		caller.continueTypeName.add(String.format(Language.tr("Example.CallerGroupExtraType"),"A"));
		caller.continueTypeName.add(String.format(Language.tr("Example.CallerGroupExtraType"),"B"));
		caller.continueTypeName.add(String.format(Language.tr("Example.CallerGroupExtraType"),"C"));
		caller.continueTypeName.add(String.format(Language.tr("Example.CallerGroupExtraType"),"D"));
		caller.continueTypeRate.add(3.0);
		caller.continueTypeRate.add(2.0);
		caller.continueTypeRate.add(1.0);
		caller.continueTypeRate.add(9.0);
		model.caller.add(caller);

		/* Priorisierte Kunden (Thema D) anlegen */
		caller=priorityCaller.clone();
		caller.name=String.format(Language.tr("Example.CallerGroupExtraType"),"D");
		caller.freshCallsCountMean=3000;
		caller.continueProbability=0.08;
		caller.continueTypeName.add(String.format(Language.tr("Example.CallerGroupExtraType"),"A"));
		caller.continueTypeName.add(String.format(Language.tr("Example.CallerGroupExtraType"),"B"));
		caller.continueTypeName.add(String.format(Language.tr("Example.CallerGroupExtraType"),"C"));
		caller.continueTypeName.add(String.format(Language.tr("Example.CallerGroupExtraType"),"D"));
		caller.continueTypeRate.add(2.0);
		caller.continueTypeRate.add(3.0);
		caller.continueTypeRate.add(8.0);
		caller.continueTypeRate.add(1.0);
		model.caller.add(caller);

		/* === Skill-Level === */
		CallcenterModelSkillLevel skill;

		/* Skill-Level A anlegen */
		skill=new CallcenterModelSkillLevel(String.format(Language.tr("Example.SkillLevel"),"A"));
		skill.callerTypeName.add(String.format(Language.tr("Example.CallerGroupType"),"A"));
		skill.callerTypeName.add(String.format(Language.tr("Example.CallerGroupExtraType"),"A"));
		skill.callerTypeWorkingTimeAddOn.add("0");
		skill.callerTypeWorkingTimeAddOn.add("0");
		skill.callerTypeWorkingTime.add(new LogNormalDistributionImpl(300,30));
		skill.callerTypeWorkingTime.add(new LogNormalDistributionImpl(300,30));
		skill.callerTypePostProcessingTime.add(new LogNormalDistributionImpl(75,30));
		skill.callerTypePostProcessingTime.add(new LogNormalDistributionImpl(75,30));
		skill.callerTypeIntervalWorkingTimeAddOn.add(new String[48]);
		skill.callerTypeIntervalWorkingTimeAddOn.add(new String[48]);
		skill.callerTypeIntervalWorkingTime.add(new AbstractRealDistribution[48]);
		skill.callerTypeIntervalWorkingTime.add(new AbstractRealDistribution[48]);
		skill.callerTypeIntervalPostProcessingTime.add(new AbstractRealDistribution[48]);
		skill.callerTypeIntervalPostProcessingTime.add(new AbstractRealDistribution[48]);
		skill.callerTypeScore.add(1);
		skill.callerTypeScore.add(10);
		model.skills.add(skill);

		/* Skill-Level B anlegen */
		skill=new CallcenterModelSkillLevel(String.format(Language.tr("Example.SkillLevel"),"B"));
		skill.callerTypeName.add(String.format(Language.tr("Example.CallerGroupType"),"B"));
		skill.callerTypeName.add(String.format(Language.tr("Example.CallerGroupExtraType"),"B"));
		skill.callerTypeWorkingTimeAddOn.add("0");
		skill.callerTypeWorkingTimeAddOn.add("0");
		skill.callerTypeWorkingTime.add(new LogNormalDistributionImpl(300,60));
		skill.callerTypeWorkingTime.add(new LogNormalDistributionImpl(300,60));
		skill.callerTypePostProcessingTime.add(new LogNormalDistributionImpl(75,60));
		skill.callerTypePostProcessingTime.add(new LogNormalDistributionImpl(75,60));
		skill.callerTypeIntervalWorkingTimeAddOn.add(new String[48]);
		skill.callerTypeIntervalWorkingTimeAddOn.add(new String[48]);
		skill.callerTypeIntervalWorkingTime.add(new AbstractRealDistribution[48]);
		skill.callerTypeIntervalWorkingTime.add(new AbstractRealDistribution[48]);
		skill.callerTypeIntervalPostProcessingTime.add(new AbstractRealDistribution[48]);
		skill.callerTypeIntervalPostProcessingTime.add(new AbstractRealDistribution[48]);
		skill.callerTypeScore.add(1);
		skill.callerTypeScore.add(10);
		model.skills.add(skill);

		/* Multi-Skill Skill-Level C+D anlegen */
		skill=new CallcenterModelSkillLevel(String.format(Language.tr("Example.MultiSkill"),"C+D"));
		skill.callerTypeName.add(String.format(Language.tr("Example.CallerGroupType"),"C"));
		skill.callerTypeName.add(String.format(Language.tr("Example.CallerGroupExtraType"),"C"));
		skill.callerTypeWorkingTimeAddOn.add("0");
		skill.callerTypeWorkingTimeAddOn.add("0");
		skill.callerTypeWorkingTime.add(new LogNormalDistributionImpl(150,60));
		skill.callerTypeWorkingTime.add(new LogNormalDistributionImpl(150,60));
		skill.callerTypePostProcessingTime.add(new LogNormalDistributionImpl(120,60));
		skill.callerTypePostProcessingTime.add(new LogNormalDistributionImpl(120,60));
		skill.callerTypeIntervalWorkingTimeAddOn.add(new String[48]);
		skill.callerTypeIntervalWorkingTimeAddOn.add(new String[48]);
		skill.callerTypeIntervalWorkingTime.add(new AbstractRealDistribution[48]);
		skill.callerTypeIntervalWorkingTime.add(new AbstractRealDistribution[48]);
		skill.callerTypeIntervalPostProcessingTime.add(new AbstractRealDistribution[48]);
		skill.callerTypeIntervalPostProcessingTime.add(new AbstractRealDistribution[48]);
		skill.callerTypeScore.add(1);
		skill.callerTypeScore.add(10);
		skill.callerTypeName.add(String.format(Language.tr("Example.CallerGroupType"),"D"));
		skill.callerTypeName.add(String.format(Language.tr("Example.CallerGroupExtraType"),"D"));
		skill.callerTypeWorkingTimeAddOn.add("0");
		skill.callerTypeWorkingTimeAddOn.add("0");
		skill.callerTypeWorkingTime.add(new LogNormalDistributionImpl(360,60));
		skill.callerTypeWorkingTime.add(new LogNormalDistributionImpl(360,60));
		skill.callerTypePostProcessingTime.add(new LogNormalDistributionImpl(75,60));
		skill.callerTypePostProcessingTime.add(new LogNormalDistributionImpl(75,60));
		skill.callerTypeIntervalWorkingTimeAddOn.add(new String[48]);
		skill.callerTypeIntervalWorkingTimeAddOn.add(new String[48]);
		skill.callerTypeIntervalWorkingTime.add(new AbstractRealDistribution[48]);
		skill.callerTypeIntervalWorkingTime.add(new AbstractRealDistribution[48]);
		skill.callerTypeIntervalPostProcessingTime.add(new AbstractRealDistribution[48]);
		skill.callerTypeIntervalPostProcessingTime.add(new AbstractRealDistribution[48]);
		skill.callerTypeScore.add(1);
		skill.callerTypeScore.add(10);
		model.skills.add(skill);

		/* Multi-Skill Skill-Level A+B+C+D anlegen */
		skill=new CallcenterModelSkillLevel(String.format(Language.tr("Example.MultiSkill"),"A+B+C+D"));
		skill.callerTypeName.add(String.format(Language.tr("Example.CallerGroupType"),"A"));
		skill.callerTypeName.add(String.format(Language.tr("Example.CallerGroupExtraType"),"A"));
		skill.callerTypeWorkingTimeAddOn.add("0");
		skill.callerTypeWorkingTimeAddOn.add("0");
		skill.callerTypeWorkingTime.add(new LogNormalDistributionImpl(480,30));
		skill.callerTypeWorkingTime.add(new LogNormalDistributionImpl(480,30));
		skill.callerTypePostProcessingTime.add(new LogNormalDistributionImpl(90,30));
		skill.callerTypePostProcessingTime.add(new LogNormalDistributionImpl(90,30));
		skill.callerTypeIntervalWorkingTimeAddOn.add(new String[48]);
		skill.callerTypeIntervalWorkingTimeAddOn.add(new String[48]);
		skill.callerTypeIntervalWorkingTime.add(new AbstractRealDistribution[48]);
		skill.callerTypeIntervalWorkingTime.add(new AbstractRealDistribution[48]);
		skill.callerTypeIntervalPostProcessingTime.add(new AbstractRealDistribution[48]);
		skill.callerTypeIntervalPostProcessingTime.add(new AbstractRealDistribution[48]);
		skill.callerTypeScore.add(1);
		skill.callerTypeScore.add(10);
		skill.callerTypeName.add(String.format(Language.tr("Example.CallerGroupType"),"B"));
		skill.callerTypeName.add(String.format(Language.tr("Example.CallerGroupExtraType"),"B"));
		skill.callerTypeWorkingTimeAddOn.add("0");
		skill.callerTypeWorkingTimeAddOn.add("0");
		skill.callerTypeWorkingTime.add(new LogNormalDistributionImpl(480,60));
		skill.callerTypeWorkingTime.add(new LogNormalDistributionImpl(480,60));
		skill.callerTypePostProcessingTime.add(new LogNormalDistributionImpl(90,60));
		skill.callerTypePostProcessingTime.add(new LogNormalDistributionImpl(90,60));
		skill.callerTypeIntervalWorkingTimeAddOn.add(new String[48]);
		skill.callerTypeIntervalWorkingTimeAddOn.add(new String[48]);
		skill.callerTypeIntervalWorkingTime.add(new AbstractRealDistribution[48]);
		skill.callerTypeIntervalWorkingTime.add(new AbstractRealDistribution[48]);
		skill.callerTypeIntervalPostProcessingTime.add(new AbstractRealDistribution[48]);
		skill.callerTypeIntervalPostProcessingTime.add(new AbstractRealDistribution[48]);
		skill.callerTypeScore.add(1);
		skill.callerTypeScore.add(10);
		skill.callerTypeName.add(String.format(Language.tr("Example.CallerGroupType"),"C"));
		skill.callerTypeName.add(String.format(Language.tr("Example.CallerGroupExtraType"),"C"));
		skill.callerTypeWorkingTimeAddOn.add("0");
		skill.callerTypeWorkingTimeAddOn.add("0");
		skill.callerTypeWorkingTime.add(new LogNormalDistributionImpl(180,60));
		skill.callerTypeWorkingTime.add(new LogNormalDistributionImpl(180,60));
		skill.callerTypePostProcessingTime.add(new LogNormalDistributionImpl(120,60));
		skill.callerTypePostProcessingTime.add(new LogNormalDistributionImpl(120,60));
		skill.callerTypeIntervalWorkingTimeAddOn.add(new String[48]);
		skill.callerTypeIntervalWorkingTimeAddOn.add(new String[48]);
		skill.callerTypeIntervalWorkingTime.add(new AbstractRealDistribution[48]);
		skill.callerTypeIntervalWorkingTime.add(new AbstractRealDistribution[48]);
		skill.callerTypeIntervalPostProcessingTime.add(new AbstractRealDistribution[48]);
		skill.callerTypeIntervalPostProcessingTime.add(new AbstractRealDistribution[48]);
		skill.callerTypeScore.add(1);
		skill.callerTypeScore.add(10);
		skill.callerTypeName.add(String.format(Language.tr("Example.CallerGroupType"),"D"));
		skill.callerTypeName.add(String.format(Language.tr("Example.CallerGroupExtraType"),"D"));
		skill.callerTypeWorkingTimeAddOn.add("0");
		skill.callerTypeWorkingTimeAddOn.add("0");
		skill.callerTypeWorkingTime.add(new LogNormalDistributionImpl(390,60));
		skill.callerTypeWorkingTime.add(new LogNormalDistributionImpl(390,60));
		skill.callerTypePostProcessingTime.add(new LogNormalDistributionImpl(75,60));
		skill.callerTypePostProcessingTime.add(new LogNormalDistributionImpl(75,60));
		skill.callerTypeIntervalWorkingTimeAddOn.add(new String[48]);
		skill.callerTypeIntervalWorkingTimeAddOn.add(new String[48]);
		skill.callerTypeIntervalWorkingTime.add(new AbstractRealDistribution[48]);
		skill.callerTypeIntervalWorkingTime.add(new AbstractRealDistribution[48]);
		skill.callerTypeIntervalPostProcessingTime.add(new AbstractRealDistribution[48]);
		skill.callerTypeIntervalPostProcessingTime.add(new AbstractRealDistribution[48]);
		skill.callerTypeScore.add(1);
		skill.callerTypeScore.add(10);
		model.skills.add(skill);

		/* === Callcenter === */
		CallcenterModelCallcenter callcenter;
		CallcenterModelAgent agent;

		/* Basisdaten für alle Callcenter */
		callcenter=new CallcenterModelCallcenter("");
		callcenter.technicalFreeTime=2;
		callcenter.agentScoreFreeTimeSinceLastCall=0;
		callcenter.agentScoreFreeTimePart=1;
		CallcenterModelCallcenter callcenterTemplate=callcenter;

		/* Callcenters 1 (Callcenter für Thema A) */
		callcenter=callcenterTemplate.clone();
		callcenter.name=String.format(Language.tr("Example.CallcenterType"),"A");
		callcenter.score=10;
		model.callcenter.add(callcenter);

		/* Agenten vom Skill-Level A anlegen und zu Callcenter hinzufügen */
		agent=new CallcenterModelAgent();
		agent.count=-1;
		agent.countPerInterval48=singleAgentACount.clone();
		agent.countPerInterval48.updateCumulativeDensity();
		agent.lastShiftIsOpenEnd=true;
		agent.skillLevel=String.format(Language.tr("Example.SkillLevel"),"A");
		callcenter.agents.add(agent);

		/* Callcenters 2 (Callcenter für Thema B) */
		callcenter=callcenterTemplate.clone();
		callcenter.name=String.format(Language.tr("Example.CallcenterType"),"B");
		callcenter.score=10;
		model.callcenter.add(callcenter);

		/* Agenten vom Skill-Level B anlegen und zu Callcenter hinzufügen */
		agent=new CallcenterModelAgent();
		agent.count=-1;
		agent.countPerInterval48=singleAgentBCount.clone();
		agent.countPerInterval48.updateCumulativeDensity();
		agent.lastShiftIsOpenEnd=true;
		agent.skillLevel=String.format(Language.tr("Example.SkillLevel"),"B");
		callcenter.agents.add(agent);

		/* Callcenters 3 (Alle Themen; Multi-Skill A+B+C+D) */
		callcenter=callcenterTemplate.clone();
		callcenter.name=Language.tr("Example.CallcenterTypeAll");
		callcenter.score=1;
		model.callcenter.add(callcenter);

		/* Multi-Skill Agenten anlegen und zu Callcenter hinzufügen */
		agent=new CallcenterModelAgent();
		agent.count=-1;
		agent.countPerInterval48=multiAgentABCDCount.clone();
		agent.countPerInterval48.updateCumulativeDensity();
		agent.lastShiftIsOpenEnd=true;
		agent.skillLevel=String.format(Language.tr("Example.MultiSkill"),"A+B+C+D");
		callcenter.agents.add(agent);

		/* Callcenters 4 (Single-Skill A und Multi-Skill C+D) */
		callcenter=callcenterTemplate.clone();
		callcenter.name=String.format(Language.tr("Example.CallcenterType"),"A+C+D");
		callcenter.score=2;
		model.callcenter.add(callcenter);

		/* Agenten vom Skill-Level A anlegen und zu Callcenter hinzufügen */
		agent=new CallcenterModelAgent();
		agent.count=-1;
		agent.countPerInterval48=singleAgentACount.clone();
		agent.countPerInterval48.updateCumulativeDensity();
		agent.lastShiftIsOpenEnd=true;
		agent.skillLevel=String.format(Language.tr("Example.SkillLevel"),"A");
		callcenter.agents.add(agent);

		/* Multi-Skill Agenten anlegen und zu Callcenter hinzufügen */
		agent=new CallcenterModelAgent();
		agent.count=-1;
		agent.countPerInterval48=multiAgentCDCount.clone();
		agent.countPerInterval48.updateCumulativeDensity();
		agent.lastShiftIsOpenEnd=true;
		agent.skillLevel=String.format(Language.tr("Example.MultiSkill"),"C+D");
		callcenter.agents.add(agent);

		model.prepareData();

		return model;
	}

	/**
	 * Erstellt ein sehr großes Callcenter-Modell.
	 * @return	Sehr großes Callcenter-Modell
	 */
	public static final CallcenterModel getExampleExtraLarge() {
		CallcenterModel model=new CallcenterModel();

		/* Basisangaben */
		model.name=Language.tr("Example.VeryLarge.Title");
		model.description=Language.tr("Example.VeryLarge.Description");
		model.maxQueueLength="200";
		model.days=100;
		model.preferredShiftLength=16;

		double[] normalCallerData={0,0,0,0,1,2,1,4,5,5,3,3, 5,7,10,10,15,17,25,50,60,65,65,62, 50,45,50,60,62,62,60,55,50,45,40,35, 35,40,40,37,35,30,25,15,10,8,5,2};
		double[] priorityCallerData={0,0,0,0,0,0,0,1,2,2,1,2, 10,15,15,20,25,30,35,50,60,70,70,50, 40,40,60,70,70,65,60,55,55,25,15,10, 10,10,5,5,5,2,0,0,0,0,0,0};

		DataDistributionImpl normalCallerDist=new DataDistributionImpl(86399,normalCallerData);
		DataDistributionImpl priorityCallerDist=new DataDistributionImpl(86399,priorityCallerData);

		DataDistributionImpl singleAgentACount=normalCallerDist.multiply(12).ceil().add(1);
		DataDistributionImpl singleAgentBCount=normalCallerDist.multiply(13.5).ceil().add(1);
		DataDistributionImpl multiAgentCDCount=normalCallerDist.multiply(14.25).ceil().add(1);
		DataDistributionImpl multiAgentABCDCount=normalCallerDist.multiply(5.25).ceil().add(1);

		/* === Anrufer === */
		CallcenterModelCaller caller;

		/* Basisdaten für normale Anrufer */
		caller=new CallcenterModelCaller("");
		caller.freshCallsDist48=normalCallerDist.clone();
		caller.scoreBase=50;
		caller.scoreSecond=1;
		caller.scoreContinued=50;
		caller.waitingTimeMode=CallcenterModelCaller.WAITING_TIME_MODE_SHORT;
		caller.waitingTimeDist=new LogNormalDistributionImpl(300,200);
		caller.retryTimeDist=new ExponentialDistribution(900);
		caller.retryProbabiltyAfterBlockedFirstRetry=0.9;
		caller.retryProbabiltyAfterBlocked=0.8;
		caller.retryProbabiltyAfterGiveUpFirstRetry=0.9;
		caller.retryProbabiltyAfterGiveUp=0.8;
		CallcenterModelCaller normalCaller=caller;

		/* Normale Kunden (Thema A) anlegen */
		caller=normalCaller.clone();
		caller.name=String.format(Language.tr("Example.CallerGroupType"),"A");
		caller.freshCallsCountMean=90000;
		caller.continueProbability=0.1;
		caller.continueTypeName.add(String.format(Language.tr("Example.CallerGroupType"),"A"));
		caller.continueTypeName.add(String.format(Language.tr("Example.CallerGroupType"),"B"));
		caller.continueTypeName.add(String.format(Language.tr("Example.CallerGroupType"),"C"));
		caller.continueTypeName.add(String.format(Language.tr("Example.CallerGroupType"),"D"));
		caller.continueTypeRate.add(1.0);
		caller.continueTypeRate.add(9.0);
		caller.continueTypeRate.add(2.0);
		caller.continueTypeRate.add(2.0);
		model.caller.add(caller);

		/* Normale Kunden (Thema B) anlegen */
		caller=normalCaller.clone();
		caller.name=String.format(Language.tr("Example.CallerGroupType"),"B");
		caller.freshCallsCountMean=51000;
		caller.continueProbability=0.08;
		caller.continueTypeName.add(String.format(Language.tr("Example.CallerGroupType"),"A"));
		caller.continueTypeName.add(String.format(Language.tr("Example.CallerGroupType"),"B"));
		caller.continueTypeName.add(String.format(Language.tr("Example.CallerGroupType"),"C"));
		caller.continueTypeName.add(String.format(Language.tr("Example.CallerGroupType"),"D"));
		caller.continueTypeRate.add(8.0);
		caller.continueTypeRate.add(1.0);
		caller.continueTypeRate.add(2.0);
		caller.continueTypeRate.add(2.0);
		model.caller.add(caller);

		/* Normale Kunden (Thema C) anlegen */
		caller=normalCaller.clone();
		caller.name=String.format(Language.tr("Example.CallerGroupType"),"C");
		caller.freshCallsCountMean=45000;
		caller.continueProbability=0.12;
		caller.continueTypeName.add(String.format(Language.tr("Example.CallerGroupType"),"A"));
		caller.continueTypeName.add(String.format(Language.tr("Example.CallerGroupType"),"B"));
		caller.continueTypeName.add(String.format(Language.tr("Example.CallerGroupType"),"C"));
		caller.continueTypeName.add(String.format(Language.tr("Example.CallerGroupType"),"D"));
		caller.continueTypeRate.add(2.0);
		caller.continueTypeRate.add(2.0);
		caller.continueTypeRate.add(1.0);
		caller.continueTypeRate.add(7.0);
		model.caller.add(caller);

		/* Normale Kunden (Thema D) anlegen */
		caller=normalCaller.clone();
		caller.name=String.format(Language.tr("Example.CallerGroupType"),"D");
		caller.freshCallsCountMean=24000;
		caller.continueProbability=0.05;
		caller.continueTypeName.add(String.format(Language.tr("Example.CallerGroupType"),"A"));
		caller.continueTypeName.add(String.format(Language.tr("Example.CallerGroupType"),"B"));
		caller.continueTypeName.add(String.format(Language.tr("Example.CallerGroupType"),"C"));
		caller.continueTypeName.add(String.format(Language.tr("Example.CallerGroupType"),"D"));
		caller.continueTypeRate.add(2.0);
		caller.continueTypeRate.add(2.0);
		caller.continueTypeRate.add(10.0);
		caller.continueTypeRate.add(1.0);
		model.caller.add(caller);

		/* Basisdaten für priorisierte Anrufer */
		caller=new CallcenterModelCaller("");
		caller.freshCallsDist48=priorityCallerDist.clone();
		caller.scoreBase=100;
		caller.scoreSecond=5;
		caller.scoreContinued=150;
		caller.waitingTimeMode=CallcenterModelCaller.WAITING_TIME_MODE_SHORT;
		caller.waitingTimeDist=new LogNormalDistributionImpl(200,150);
		caller.retryTimeDist=new ExponentialDistribution(900);
		caller.retryProbabiltyAfterBlockedFirstRetry=0.5;
		caller.retryProbabiltyAfterBlocked=0.25;
		caller.retryProbabiltyAfterGiveUpFirstRetry=0.5;
		caller.retryProbabiltyAfterGiveUp=0.25;
		CallcenterModelCaller priorityCaller=caller;

		/* Priorisierte Kunden (Thema A) anlegen */
		caller=priorityCaller.clone();
		caller.name=String.format(Language.tr("Example.CallerGroupExtraType"),"A");
		caller.freshCallsCountMean=42000;
		caller.continueProbability=0.1;
		caller.continueTypeName.add(String.format(Language.tr("Example.CallerGroupExtraType"),"A"));
		caller.continueTypeName.add(String.format(Language.tr("Example.CallerGroupExtraType"),"B"));
		caller.continueTypeName.add(String.format(Language.tr("Example.CallerGroupExtraType"),"C"));
		caller.continueTypeName.add(String.format(Language.tr("Example.CallerGroupExtraType"),"D"));
		caller.continueTypeRate.add(1.0);
		caller.continueTypeRate.add(9.0);
		caller.continueTypeRate.add(3.0);
		caller.continueTypeRate.add(2.0);
		model.caller.add(caller);

		/* Priorisierte Kunden (Thema B) anlegen */
		caller=priorityCaller.clone();
		caller.name=String.format(Language.tr("Example.CallerGroupExtraType"),"B");
		caller.freshCallsCountMean=24000;
		caller.continueProbability=0.08;
		caller.continueTypeName.add(String.format(Language.tr("Example.CallerGroupExtraType"),"A"));
		caller.continueTypeName.add(String.format(Language.tr("Example.CallerGroupExtraType"),"B"));
		caller.continueTypeName.add(String.format(Language.tr("Example.CallerGroupExtraType"),"C"));
		caller.continueTypeName.add(String.format(Language.tr("Example.CallerGroupExtraType"),"D"));
		caller.continueTypeRate.add(8.0);
		caller.continueTypeRate.add(1.0);
		caller.continueTypeRate.add(2.0);
		caller.continueTypeRate.add(3.0);
		model.caller.add(caller);

		/* Priorisierte Kunden (Thema C) anlegen */
		caller=priorityCaller.clone();
		caller.name=String.format(Language.tr("Example.CallerGroupExtraType"),"C");
		caller.freshCallsCountMean=15000;
		caller.continueProbability=0.10;
		caller.continueTypeName.add(String.format(Language.tr("Example.CallerGroupExtraType"),"A"));
		caller.continueTypeName.add(String.format(Language.tr("Example.CallerGroupExtraType"),"B"));
		caller.continueTypeName.add(String.format(Language.tr("Example.CallerGroupExtraType"),"C"));
		caller.continueTypeName.add(String.format(Language.tr("Example.CallerGroupExtraType"),"D"));
		caller.continueTypeRate.add(3.0);
		caller.continueTypeRate.add(2.0);
		caller.continueTypeRate.add(1.0);
		caller.continueTypeRate.add(9.0);
		model.caller.add(caller);

		/* Priorisierte Kunden (Thema D) anlegen */
		caller=priorityCaller.clone();
		caller.name=String.format(Language.tr("Example.CallerGroupExtraType"),"D");
		caller.freshCallsCountMean=9000;
		caller.continueProbability=0.08;
		caller.continueTypeName.add(String.format(Language.tr("Example.CallerGroupExtraType"),"A"));
		caller.continueTypeName.add(String.format(Language.tr("Example.CallerGroupExtraType"),"B"));
		caller.continueTypeName.add(String.format(Language.tr("Example.CallerGroupExtraType"),"C"));
		caller.continueTypeName.add(String.format(Language.tr("Example.CallerGroupExtraType"),"D"));
		caller.continueTypeRate.add(2.0);
		caller.continueTypeRate.add(3.0);
		caller.continueTypeRate.add(8.0);
		caller.continueTypeRate.add(1.0);
		model.caller.add(caller);

		/* === Skill-Level === */
		CallcenterModelSkillLevel skill;

		/* Skill-Level A anlegen */
		skill=new CallcenterModelSkillLevel(String.format(Language.tr("Example.SkillLevel"),"A"));
		skill.callerTypeName.add(String.format(Language.tr("Example.CallerGroupType"),"A"));
		skill.callerTypeName.add(String.format(Language.tr("Example.CallerGroupExtraType"),"A"));
		skill.callerTypeWorkingTimeAddOn.add("0");
		skill.callerTypeWorkingTimeAddOn.add("0");
		skill.callerTypeWorkingTime.add(new LogNormalDistributionImpl(300,30));
		skill.callerTypeWorkingTime.add(new LogNormalDistributionImpl(300,30));
		skill.callerTypePostProcessingTime.add(new LogNormalDistributionImpl(75,30));
		skill.callerTypePostProcessingTime.add(new LogNormalDistributionImpl(75,30));
		skill.callerTypeIntervalWorkingTimeAddOn.add(new String[48]);
		skill.callerTypeIntervalWorkingTimeAddOn.add(new String[48]);
		skill.callerTypeIntervalWorkingTime.add(new AbstractRealDistribution[48]);
		skill.callerTypeIntervalWorkingTime.add(new AbstractRealDistribution[48]);
		skill.callerTypeIntervalPostProcessingTime.add(new AbstractRealDistribution[48]);
		skill.callerTypeIntervalPostProcessingTime.add(new AbstractRealDistribution[48]);
		skill.callerTypeScore.add(1);
		skill.callerTypeScore.add(10);
		model.skills.add(skill);

		/* Skill-Level B anlegen */
		skill=new CallcenterModelSkillLevel(String.format(Language.tr("Example.SkillLevel"),"B"));
		skill.callerTypeName.add(String.format(Language.tr("Example.CallerGroupType"),"B"));
		skill.callerTypeName.add(String.format(Language.tr("Example.CallerGroupExtraType"),"B"));
		skill.callerTypeWorkingTimeAddOn.add("0");
		skill.callerTypeWorkingTimeAddOn.add("0");
		skill.callerTypeWorkingTime.add(new LogNormalDistributionImpl(300,60));
		skill.callerTypeWorkingTime.add(new LogNormalDistributionImpl(300,60));
		skill.callerTypePostProcessingTime.add(new LogNormalDistributionImpl(75,60));
		skill.callerTypePostProcessingTime.add(new LogNormalDistributionImpl(75,60));
		skill.callerTypeIntervalWorkingTimeAddOn.add(new String[48]);
		skill.callerTypeIntervalWorkingTimeAddOn.add(new String[48]);
		skill.callerTypeIntervalWorkingTime.add(new AbstractRealDistribution[48]);
		skill.callerTypeIntervalWorkingTime.add(new AbstractRealDistribution[48]);
		skill.callerTypeIntervalPostProcessingTime.add(new AbstractRealDistribution[48]);
		skill.callerTypeIntervalPostProcessingTime.add(new AbstractRealDistribution[48]);
		skill.callerTypeScore.add(1);
		skill.callerTypeScore.add(10);
		model.skills.add(skill);

		/* Multi-Skill Skill-Level C+D anlegen */
		skill=new CallcenterModelSkillLevel(String.format(Language.tr("Example.MultiSkill"),"C+D"));
		skill.callerTypeName.add(String.format(Language.tr("Example.CallerGroupType"),"C"));
		skill.callerTypeName.add(String.format(Language.tr("Example.CallerGroupExtraType"),"C"));
		skill.callerTypeWorkingTimeAddOn.add("0");
		skill.callerTypeWorkingTimeAddOn.add("0");
		skill.callerTypeWorkingTime.add(new LogNormalDistributionImpl(150,60));
		skill.callerTypeWorkingTime.add(new LogNormalDistributionImpl(150,60));
		skill.callerTypePostProcessingTime.add(new LogNormalDistributionImpl(120,60));
		skill.callerTypePostProcessingTime.add(new LogNormalDistributionImpl(120,60));
		skill.callerTypeIntervalWorkingTimeAddOn.add(new String[48]);
		skill.callerTypeIntervalWorkingTimeAddOn.add(new String[48]);
		skill.callerTypeIntervalWorkingTime.add(new AbstractRealDistribution[48]);
		skill.callerTypeIntervalWorkingTime.add(new AbstractRealDistribution[48]);
		skill.callerTypeIntervalPostProcessingTime.add(new AbstractRealDistribution[48]);
		skill.callerTypeIntervalPostProcessingTime.add(new AbstractRealDistribution[48]);
		skill.callerTypeScore.add(1);
		skill.callerTypeScore.add(10);
		skill.callerTypeName.add(String.format(Language.tr("Example.CallerGroupType"),"D"));
		skill.callerTypeName.add(String.format(Language.tr("Example.CallerGroupExtraType"),"D"));
		skill.callerTypeWorkingTimeAddOn.add("0");
		skill.callerTypeWorkingTimeAddOn.add("0");
		skill.callerTypeWorkingTime.add(new LogNormalDistributionImpl(360,60));
		skill.callerTypeWorkingTime.add(new LogNormalDistributionImpl(360,60));
		skill.callerTypePostProcessingTime.add(new LogNormalDistributionImpl(75,60));
		skill.callerTypePostProcessingTime.add(new LogNormalDistributionImpl(75,60));
		skill.callerTypeIntervalWorkingTimeAddOn.add(new String[48]);
		skill.callerTypeIntervalWorkingTimeAddOn.add(new String[48]);
		skill.callerTypeIntervalWorkingTime.add(new AbstractRealDistribution[48]);
		skill.callerTypeIntervalWorkingTime.add(new AbstractRealDistribution[48]);
		skill.callerTypeIntervalPostProcessingTime.add(new AbstractRealDistribution[48]);
		skill.callerTypeIntervalPostProcessingTime.add(new AbstractRealDistribution[48]);
		skill.callerTypeScore.add(1);
		skill.callerTypeScore.add(10);
		model.skills.add(skill);

		/* Multi-Skill Skill-Level A+B+C+D anlegen */
		skill=new CallcenterModelSkillLevel(String.format(Language.tr("Example.MultiSkill"),"A+B+C+D"));
		skill.callerTypeName.add(String.format(Language.tr("Example.CallerGroupType"),"A"));
		skill.callerTypeName.add(String.format(Language.tr("Example.CallerGroupExtraType"),"A"));
		skill.callerTypeWorkingTimeAddOn.add("0");
		skill.callerTypeWorkingTimeAddOn.add("0");
		skill.callerTypeWorkingTime.add(new LogNormalDistributionImpl(480,30));
		skill.callerTypeWorkingTime.add(new LogNormalDistributionImpl(480,30));
		skill.callerTypePostProcessingTime.add(new LogNormalDistributionImpl(90,30));
		skill.callerTypePostProcessingTime.add(new LogNormalDistributionImpl(90,30));
		skill.callerTypeIntervalWorkingTimeAddOn.add(new String[48]);
		skill.callerTypeIntervalWorkingTimeAddOn.add(new String[48]);
		skill.callerTypeIntervalWorkingTime.add(new AbstractRealDistribution[48]);
		skill.callerTypeIntervalWorkingTime.add(new AbstractRealDistribution[48]);
		skill.callerTypeIntervalPostProcessingTime.add(new AbstractRealDistribution[48]);
		skill.callerTypeIntervalPostProcessingTime.add(new AbstractRealDistribution[48]);
		skill.callerTypeScore.add(1);
		skill.callerTypeScore.add(10);
		skill.callerTypeName.add(String.format(Language.tr("Example.CallerGroupType"),"B"));
		skill.callerTypeName.add(String.format(Language.tr("Example.CallerGroupExtraType"),"B"));
		skill.callerTypeWorkingTimeAddOn.add("0");
		skill.callerTypeWorkingTimeAddOn.add("0");
		skill.callerTypeWorkingTime.add(new LogNormalDistributionImpl(480,60));
		skill.callerTypeWorkingTime.add(new LogNormalDistributionImpl(480,60));
		skill.callerTypePostProcessingTime.add(new LogNormalDistributionImpl(90,60));
		skill.callerTypePostProcessingTime.add(new LogNormalDistributionImpl(90,60));
		skill.callerTypeIntervalWorkingTimeAddOn.add(new String[48]);
		skill.callerTypeIntervalWorkingTimeAddOn.add(new String[48]);
		skill.callerTypeIntervalWorkingTime.add(new AbstractRealDistribution[48]);
		skill.callerTypeIntervalWorkingTime.add(new AbstractRealDistribution[48]);
		skill.callerTypeIntervalPostProcessingTime.add(new AbstractRealDistribution[48]);
		skill.callerTypeIntervalPostProcessingTime.add(new AbstractRealDistribution[48]);
		skill.callerTypeScore.add(1);
		skill.callerTypeScore.add(10);
		skill.callerTypeName.add(String.format(Language.tr("Example.CallerGroupType"),"C"));
		skill.callerTypeName.add(String.format(Language.tr("Example.CallerGroupExtraType"),"C"));
		skill.callerTypeWorkingTimeAddOn.add("0");
		skill.callerTypeWorkingTimeAddOn.add("0");
		skill.callerTypeWorkingTime.add(new LogNormalDistributionImpl(180,60));
		skill.callerTypeWorkingTime.add(new LogNormalDistributionImpl(180,60));
		skill.callerTypePostProcessingTime.add(new LogNormalDistributionImpl(120,60));
		skill.callerTypePostProcessingTime.add(new LogNormalDistributionImpl(120,60));
		skill.callerTypeIntervalWorkingTimeAddOn.add(new String[48]);
		skill.callerTypeIntervalWorkingTimeAddOn.add(new String[48]);
		skill.callerTypeIntervalWorkingTime.add(new AbstractRealDistribution[48]);
		skill.callerTypeIntervalWorkingTime.add(new AbstractRealDistribution[48]);
		skill.callerTypeIntervalPostProcessingTime.add(new AbstractRealDistribution[48]);
		skill.callerTypeIntervalPostProcessingTime.add(new AbstractRealDistribution[48]);
		skill.callerTypeScore.add(1);
		skill.callerTypeScore.add(10);
		skill.callerTypeName.add(String.format(Language.tr("Example.CallerGroupType"),"D"));
		skill.callerTypeName.add(String.format(Language.tr("Example.CallerGroupExtraType"),"D"));
		skill.callerTypeWorkingTimeAddOn.add("0");
		skill.callerTypeWorkingTimeAddOn.add("0");
		skill.callerTypeWorkingTime.add(new LogNormalDistributionImpl(390,60));
		skill.callerTypeWorkingTime.add(new LogNormalDistributionImpl(390,60));
		skill.callerTypePostProcessingTime.add(new LogNormalDistributionImpl(75,60));
		skill.callerTypePostProcessingTime.add(new LogNormalDistributionImpl(75,60));
		skill.callerTypeIntervalWorkingTimeAddOn.add(new String[48]);
		skill.callerTypeIntervalWorkingTimeAddOn.add(new String[48]);
		skill.callerTypeIntervalWorkingTime.add(new AbstractRealDistribution[48]);
		skill.callerTypeIntervalWorkingTime.add(new AbstractRealDistribution[48]);
		skill.callerTypeIntervalPostProcessingTime.add(new AbstractRealDistribution[48]);
		skill.callerTypeIntervalPostProcessingTime.add(new AbstractRealDistribution[48]);
		skill.callerTypeScore.add(1);
		skill.callerTypeScore.add(10);
		model.skills.add(skill);

		/* === Callcenter === */
		CallcenterModelCallcenter callcenter;
		CallcenterModelAgent agent;

		/* Basisdaten für alle Callcenter */
		callcenter=new CallcenterModelCallcenter("");
		callcenter.technicalFreeTime=2;
		callcenter.agentScoreFreeTimeSinceLastCall=0;
		callcenter.agentScoreFreeTimePart=1;
		CallcenterModelCallcenter callcenterTemplate=callcenter;

		/* Callcenters 1 (Callcenter für Thema A) */
		callcenter=callcenterTemplate.clone();
		callcenter.name=String.format(Language.tr("Example.CallcenterType"),"A");
		callcenter.score=10;
		model.callcenter.add(callcenter);

		/* Agenten vom Skill-Level A anlegen und zu Callcenter hinzufügen */
		agent=new CallcenterModelAgent();
		agent.count=-1;
		agent.countPerInterval48=singleAgentACount.clone();
		agent.countPerInterval48.updateCumulativeDensity();
		agent.lastShiftIsOpenEnd=true;
		agent.skillLevel=String.format(Language.tr("Example.SkillLevel"),"A");
		callcenter.agents.add(agent);

		/* Callcenters 2 (Callcenter für Thema B) */
		callcenter=callcenterTemplate.clone();
		callcenter.name=String.format(Language.tr("Example.CallcenterType"),"B");
		callcenter.score=10;
		model.callcenter.add(callcenter);

		/* Agenten vom Skill-Level B anlegen und zu Callcenter hinzufügen */
		agent=new CallcenterModelAgent();
		agent.count=-1;
		agent.countPerInterval48=singleAgentBCount.clone();
		agent.countPerInterval48.updateCumulativeDensity();
		agent.lastShiftIsOpenEnd=true;
		agent.skillLevel=String.format(Language.tr("Example.SkillLevel"),"B");
		callcenter.agents.add(agent);

		/* Callcenters 3 (Alle Themen; Multi-Skill A+B+C+D) */
		callcenter=callcenterTemplate.clone();
		callcenter.name=Language.tr("Example.CallcenterTypeAll");
		callcenter.score=1;
		model.callcenter.add(callcenter);

		/* Multi-Skill Agenten anlegen und zu Callcenter hinzufügen */
		agent=new CallcenterModelAgent();
		agent.count=-1;
		agent.countPerInterval48=multiAgentABCDCount.clone();
		agent.countPerInterval48.updateCumulativeDensity();
		agent.lastShiftIsOpenEnd=true;
		agent.skillLevel=String.format(Language.tr("Example.MultiSkill"),"A+B+C+D");
		callcenter.agents.add(agent);

		/* Callcenters 4 (Single-Skill A und Multi-Skill C+D) */
		callcenter=callcenterTemplate.clone();
		callcenter.name=String.format(Language.tr("Example.CallcenterType"),"A+C+D");
		callcenter.score=2;
		model.callcenter.add(callcenter);

		/* Agenten vom Skill-Level A anlegen und zu Callcenter hinzufügen */
		agent=new CallcenterModelAgent();
		agent.count=-1;
		agent.countPerInterval48=singleAgentACount.clone();
		agent.countPerInterval48.updateCumulativeDensity();
		agent.lastShiftIsOpenEnd=true;
		agent.skillLevel=String.format(Language.tr("Example.SkillLevel"),"A");
		callcenter.agents.add(agent);

		/* Multi-Skill Agenten anlegen und zu Callcenter hinzufügen */
		agent=new CallcenterModelAgent();
		agent.count=-1;
		agent.countPerInterval48=multiAgentCDCount.clone();
		agent.countPerInterval48.updateCumulativeDensity();
		agent.lastShiftIsOpenEnd=true;
		agent.skillLevel=String.format(Language.tr("Example.MultiSkill"),"C+D");
		callcenter.agents.add(agent);

		model.prepareData();

		return model;
	}

	/**
	 * Prüft, ob es sich bei einem Callcenter-Modell um eines der Beispielmodelle handelt.
	 * @param model	Zu prüfendes Modell
	 * @return	Nummer des Beispiels (0..6) oder -1, wenn das Modell keinem Beispielmodell entspricht
	 * @see #getExampleByNumber(int)
	 */
	public static final int equalsExampleModel(final CallcenterModel model) {
		if (getExampleSmall().equalsCallcenterModel(model)) return 0;
		if (getExampleMedium().equalsCallcenterModel(model)) return 1;
		if (getExampleLarge().equalsCallcenterModel(model)) return 2;
		if (getExampleExtraLarge().equalsCallcenterModel(model)) return 3;
		if (getExampleSmallErlang().equalsCallcenterModel(model)) return 4;
		if (getExampleMediumErlang().equalsCallcenterModel(model)) return 5;
		if (getExampleLargeErlang().equalsCallcenterModel(model)) return 6;
		return -1;
	}

	/**
	 * Liefert ein Beispiel-Callcenter-Modell auf Basis einer Nummer für das Beispiel.
	 * @param number	Nummer für das Beispiel
	 * @return	Beispiel-Callcenter-Modell oder <code>null</code>, wenn die Nummer zu keinem Beispiel passt.
	 * @see #equalsExampleModel(CallcenterModel)
	 */
	public static final CallcenterModel getExampleByNumber(final int number) {
		switch (number) {
		case 0: return getExampleSmall();
		case 1: return getExampleMedium();
		case 2: return getExampleLarge();
		case 3: return getExampleExtraLarge();
		case 4: return getExampleSmallErlang();
		case 5: return getExampleMediumErlang();
		case 6: return getExampleLargeErlang();
		}
		return null;
	}
}
