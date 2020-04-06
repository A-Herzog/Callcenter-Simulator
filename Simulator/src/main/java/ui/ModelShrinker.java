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
package ui;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.apache.commons.math3.distribution.AbstractRealDistribution;

import language.Language;
import mathtools.distribution.DataDistributionImpl;
import mathtools.distribution.LogNormalDistributionImpl;
import mathtools.distribution.NeverDistributionImpl;
import mathtools.distribution.tools.DistributionTools;
import ui.model.CallcenterModel;
import ui.model.CallcenterModelAgent;
import ui.model.CallcenterModelCallcenter;
import ui.model.CallcenterModelCaller;
import ui.model.CallcenterModelSkillLevel;

/**
 * Erstellt auf Basis eines gegebenen, komplexen Modells
 * ein einfacheres Callcenter-Modell
 * @author Alexander Herzog
 */
public class ModelShrinker {
	private final CallcenterModel baseModel;

	/**
	 * Konstruktor der Klasse
	 * @param model	Zu vereinfachendes Callcenter-Modell
	 */
	public ModelShrinker(final CallcenterModel model) {
		baseModel=model;
	}

	private CallcenterModel calcBasics() {
		CallcenterModel model=baseModel.clone();
		/* Callcenter behalten wir, nur Caller und Skills werden neu gesetzt */
		model.caller.clear();
		model.skills.clear();

		return model;
	}

	private static AbstractRealDistribution joinDistributions(List<AbstractRealDistribution> distribution, List<Double> weight, String fallbackDistributionName, boolean useMax) {
		if (distribution.size()==0) return new NeverDistributionImpl();
		if (distribution.size()==1) return distribution.get(0);

		boolean allSame=true;
		for (int i=1;i<distribution.size();i++) if (!DistributionTools.compare(distribution.get(0),distribution.get(i))) {allSame=false; break;}
		if (allSame) return distribution.get(0);

		String preferedType="";
		double sum=0;
		double mean=0;
		double sd=0;
		for (int i=0;i<distribution.size();i++) {
			AbstractRealDistribution dist=distribution.get(i);

			String s=DistributionTools.getDistributionName(dist);
			if (i==0) preferedType=s; else {if (!s.equals(preferedType)) preferedType="";}

			sum+=weight.get(i);
			if (useMax) {
				mean=Math.max(mean,DistributionTools.getMean(dist));
			} else {
				mean+=DistributionTools.getMean(dist)*weight.get(i);
			}
			sd+=DistributionTools.getStandardDeviation(dist)*weight.get(i);
		}
		if (sum>0) {
			if (!useMax) mean=mean/sum;
			sd=sd/sum;
		}
		mean=(double)Math.round(mean*100)/100;
		sd=(double)Math.round(sd*100)/100;

		AbstractRealDistribution joinedDistribution=null;
		if (!preferedType.isEmpty()) joinedDistribution=DistributionTools.getDistributionFromInfo(preferedType,mean,sd);
		if (joinedDistribution==null && fallbackDistributionName!=null && !fallbackDistributionName.isEmpty()) joinedDistribution=DistributionTools.getDistributionFromInfo(fallbackDistributionName,mean,sd);
		if (joinedDistribution==null) joinedDistribution=DistributionTools.getDistributionFromInfo(DistributionTools.DistExp[0],mean,sd);
		return joinedDistribution;
	}

	private static AbstractRealDistribution joinDistributions(List<AbstractRealDistribution> distribution, String fallbackDistributionName, boolean useMax) {
		List<Double> weight=new ArrayList<Double>();
		for (int i=0;i<distribution.size();i++) weight.add(1.0);
		return joinDistributions(distribution,weight,fallbackDistributionName,useMax);
	}

	private CallcenterModelCaller calcCaller() {
		CallcenterModelCaller caller=new CallcenterModelCaller();

		/* Kundentyp anlegen */
		StringBuilder sb=new StringBuilder();
		for (CallcenterModelCaller c : baseModel.caller) if (c.active) {
			if (sb.length()>0) sb.append(", ");
			sb.append(c.name);
		}
		if (sb.length()==0) return null;
		caller=new CallcenterModelCaller(sb.toString());

		/* Anruferanzahl */
		caller.freshCallsCountMean=0;
		caller.freshCallsCountSD=0;
		for (CallcenterModelCaller c : baseModel.caller) if (c.active) {
			caller.freshCallsCountMean+=c.freshCallsCountMean;
			caller.freshCallsCountSD+=c.freshCallsCountSD*c.freshCallsCountMean;
		}
		if (caller.freshCallsCountMean>0) caller.freshCallsCountSD=caller.freshCallsCountSD/caller.freshCallsCountMean;
		caller.freshCallsCountSD=(double)Math.round(caller.freshCallsCountSD*100)/100;

		/* Feinste Granularität bestimmen */
		int level=24;
		for (CallcenterModelCaller c : baseModel.caller) if (c.active) {
			if (c.freshCallsDist96!=null) {level=96; break;}
			if (level==24 && c.freshCallsDist48!=null) level=48;
		}

		/* Verteilung der Erstanrufer bestimmen */
		DataDistributionImpl freshCalls;
		switch (level) {
		case 24: freshCalls=new DataDistributionImpl(CallcenterModelCaller.freshCallsDistMaxX,24); break;
		case 48: freshCalls=new DataDistributionImpl(CallcenterModelCaller.freshCallsDistMaxX,48); break;
		case 96: freshCalls=new DataDistributionImpl(CallcenterModelCaller.freshCallsDistMaxX,96); break;
		default: freshCalls=new DataDistributionImpl(CallcenterModelCaller.freshCallsDistMaxX,48); break;
		}
		int freshCallsSum=0;
		for (CallcenterModelCaller c : baseModel.caller) if (c.active) {
			DataDistributionImpl freshCallsAdd=freshCalls.clone();
			freshCallsAdd.setToValue(0);

			if (c.freshCallsDist24!=null) switch (level) {
			case 24: freshCallsAdd=c.freshCallsDist24; break;
			case 48: for (int i=0;i<48;i++) freshCallsAdd.densityData[i]=c.freshCallsDist24.densityData[i/2]/2; break;
			case 96: for (int i=0;i<96;i++) freshCallsAdd.densityData[i]=c.freshCallsDist24.densityData[i/4]/4; break;
			}

			if (c.freshCallsDist48!=null) switch (level) {
			case 48: freshCallsAdd=c.freshCallsDist48; break;
			case 96: for (int i=0;i<96;i++) freshCallsAdd.densityData[i]=c.freshCallsDist48.densityData[i/2]/2; break;
			}

			if (c.freshCallsDist96!=null) freshCallsAdd=c.freshCallsDist96;

			freshCalls=freshCalls.add(freshCallsAdd.multiply(c.freshCallsCountMean));
			freshCallsSum+=c.freshCallsCountMean;
		}
		if (freshCallsSum>0) {
			freshCalls=freshCalls.divide((double)freshCallsSum/100);
			freshCalls=freshCalls.round();
			freshCalls=freshCalls.divide(100);
		}
		switch (level) {
		case 24: caller.freshCallsDist24=freshCalls; break;
		case 48: caller.freshCallsDist48=freshCalls; break;
		case 96: caller.freshCallsDist96=freshCalls; break;
		}

		/* Score */
		caller.scoreBase=0;
		caller.scoreSecond=0;
		caller.scoreContinued=0;
		int scoreSum=0;
		for (CallcenterModelCaller c : baseModel.caller) if (c.active) {
			caller.scoreBase+=c.scoreBase*c.freshCallsCountMean;
			caller.scoreSecond+=c.scoreSecond*c.freshCallsCountMean;
			caller.scoreContinued+=c.scoreContinued*c.freshCallsCountMean;
			scoreSum+=c.freshCallsCountMean;
		}
		if (scoreSum>0) {
			caller.scoreBase=(double)Math.round(caller.scoreBase/scoreSum*100)/100;
			caller.scoreSecond=(double)Math.round(caller.scoreSecond/scoreSum*100)/100;
			caller.scoreContinued=(double)Math.round(caller.scoreContinued/scoreSum*100)/100;
		}

		/* Belegt Telefonleitung? */
		int blocksLineCount=0;
		int notBlocksLineCount=0;
		for (CallcenterModelCaller c : baseModel.caller) if (c.active) {
			if (c.blocksLine) blocksLineCount+=c.freshCallsCountMean; else notBlocksLineCount+=c.freshCallsCountMean;
		}
		caller.blocksLine=(blocksLineCount>=notBlocksLineCount);

		/* Kein Kundentyp abhängiges Service-Level */
		caller.serviceLevelSeconds=-1;

		/* Wartezeittoleranz */
		boolean noWaitingTimeTolerance=true;
		boolean longWaitingTimeTolerance=false;
		for (CallcenterModelCaller c : baseModel.caller) if (c.active) {
			if (c.waitingTimeMode!=CallcenterModelCaller.WAITING_TIME_MODE_OFF) noWaitingTimeTolerance=false;
			if (c.waitingTimeMode==CallcenterModelCaller.WAITING_TIME_MODE_LONG) longWaitingTimeTolerance=true;
		}
		if (noWaitingTimeTolerance) {
			caller.waitingTimeMode=CallcenterModelCaller.WAITING_TIME_MODE_OFF;
		} else {
			List<AbstractRealDistribution> dist=new ArrayList<AbstractRealDistribution>();
			List<Double> weight=new ArrayList<Double>();
			for (CallcenterModelCaller c : baseModel.caller) if (c.active) switch (c.waitingTimeMode) {
			case CallcenterModelCaller.WAITING_TIME_MODE_OFF:
				break;
			case CallcenterModelCaller.WAITING_TIME_MODE_SHORT:
				dist.add(c.waitingTimeDist);
				weight.add((double)c.freshCallsCountMean);
				break;
			case CallcenterModelCaller.WAITING_TIME_MODE_LONG:
				dist.add(c.waitingTimeDistLong);
				weight.add((double)c.freshCallsCountMean);
				break;
			case CallcenterModelCaller.WAITING_TIME_MODE_CALC:
				double mean=c.waitingTimeCalcMeanWaitingTime/c.waitingTimeCalcCancelProbability;
				double sd=mean;
				mean=Math.max(1,mean+c.waitingTimeCalcAdd);
				dist.add(new LogNormalDistributionImpl(mean,sd));
				weight.add((double)c.freshCallsCountMean);
				break;
			}
			AbstractRealDistribution joinedDist=joinDistributions(dist,weight,DistributionTools.DistExp[0],false);
			if (longWaitingTimeTolerance) {
				caller.waitingTimeMode=CallcenterModelCaller.WAITING_TIME_MODE_LONG;
				caller.waitingTimeDistLong=joinedDist;
			} else {
				caller.waitingTimeMode=CallcenterModelCaller.WAITING_TIME_MODE_SHORT;
				caller.waitingTimeDist=joinedDist;
			}
		}

		/* Retry */
		caller.retryProbabiltyAfterBlockedFirstRetry=0;
		caller.retryProbabiltyAfterBlocked=0;
		caller.retryProbabiltyAfterGiveUpFirstRetry=0;
		caller.retryProbabiltyAfterGiveUp=0;
		double retrySum=0;
		List<AbstractRealDistribution> retryDistributions=new ArrayList<AbstractRealDistribution>();
		List<Double> retryWeights=new ArrayList<Double>();
		for (CallcenterModelCaller c : baseModel.caller) if (c.active) {
			retryDistributions.add(c.recallTimeDist);
			retryWeights.add((double)c.freshCallsCountMean);
			caller.retryProbabiltyAfterBlockedFirstRetry+=c.retryProbabiltyAfterBlockedFirstRetry*c.freshCallsCountMean;
			caller.retryProbabiltyAfterBlocked+=c.retryProbabiltyAfterBlocked*c.freshCallsCountMean;
			caller.retryProbabiltyAfterGiveUpFirstRetry+=c.retryProbabiltyAfterGiveUpFirstRetry*c.freshCallsCountMean;
			caller.retryProbabiltyAfterGiveUp+=c.retryProbabiltyAfterGiveUp*c.freshCallsCountMean;
			retrySum+=c.freshCallsCountMean;
		}
		if (retrySum>0) {
			caller.retryProbabiltyAfterBlockedFirstRetry=(double)Math.round(caller.retryProbabiltyAfterBlockedFirstRetry/retrySum*1000)/1000;
			caller.retryProbabiltyAfterBlocked=(double)Math.round(caller.retryProbabiltyAfterBlocked/retrySum*1000)/1000;
			caller.retryProbabiltyAfterGiveUpFirstRetry=(double)Math.round(caller.retryProbabiltyAfterGiveUpFirstRetry/retrySum*1000)/1000;
			caller.retryProbabiltyAfterGiveUp=(double)Math.round(caller.retryProbabiltyAfterGiveUp/retrySum*1000)/1000;
		}

		/* Weiterleitungen */
		double continueProbability=0;
		double continueSum=0;
		for (CallcenterModelCaller c : baseModel.caller) if (c.active) {
			continueProbability+=c.continueProbability*c.freshCallsCountMean;
			continueSum+=c.freshCallsCountMean;
		}
		if (continueSum>0) continueProbability=continueProbability/continueSum;
		caller.continueProbability=(double)Math.round(continueProbability*1000)/1000;
		caller.continueTypeName.add(caller.name);
		caller.continueTypeRate.add(1.0);

		/* Recall */
		double recallProbability=0;
		double recallSum=0;
		for (CallcenterModelCaller c : baseModel.caller) if (c.active) {
			recallProbability+=c.recallProbability*c.freshCallsCountMean;
			recallSum+=c.freshCallsCountMean;
		}
		if (recallSum>0) recallProbability=recallProbability/recallSum;
		caller.recallProbability=(double)Math.round(recallProbability*1000)/1000;
		caller.recallTypeName.add(caller.name);
		caller.recallTypeRate.add(1.0);

		/* Kosten */
		for (CallcenterModelCaller c : baseModel.caller) if (c.active) {
			caller.revenuePerClient+=c.revenuePerClient*c.freshCallsCountMean;
			caller.costPerCancel+=c.costPerCancel*c.freshCallsCountMean;
			caller.costPerWaitingSec+=c.costPerWaitingSec*c.freshCallsCountMean;
		}
		caller.revenuePerClient=(double)Math.round(caller.revenuePerClient/caller.freshCallsCountMean*100)/100;
		caller.costPerCancel=(double)Math.round(caller.costPerCancel/caller.freshCallsCountMean*100)/100;
		caller.costPerWaitingSec=(double)Math.round(caller.costPerWaitingSec/caller.freshCallsCountMean*100)/100;

		return caller;
	}

	private void changeCallcenter(String callerTypeName, List<CallcenterModelCallcenter> callcenterList) {
		for (CallcenterModelCallcenter callcenter : callcenterList) {
			callcenter.callerMinWaitingTimeName.clear();
			callcenter.callerMinWaitingTime.clear();
			callcenter.callerMinWaitingTimeName.add(callerTypeName);
			callcenter.callerMinWaitingTime.add(0);
			for (CallcenterModelAgent agent : callcenter.agents) agent.skillLevel=Language.tr("SimStatistic.AllClients");
		}
	}

	private CallcenterModelSkillLevel calcSkillLevel(String callerTypeName, boolean useMaxAHT) {
		CallcenterModelSkillLevel skillLevel=new CallcenterModelSkillLevel();
		skillLevel.name=Language.tr("SimStatistic.AllClients");

		/* Welche Intervalle müssen individuell betrachtet werden? */
		boolean[] intervalDataNeeded1=new boolean[48];
		boolean[] intervalDataNeeded2=new boolean[48];
		Arrays.fill(intervalDataNeeded1,false);
		Arrays.fill(intervalDataNeeded2,false);
		for (CallcenterModelSkillLevel skill : baseModel.skills) for (int i=0;i<skill.callerTypeName.size();i++) {
			if (skill.callerTypeIntervalWorkingTime!=null && skill.callerTypeIntervalWorkingTime.get(i)!=null) {
				AbstractRealDistribution[] d=skill.callerTypeIntervalWorkingTime.get(i);
				for (int j=0;j<48;j++) if (d[j]!=null) intervalDataNeeded1[j]=true;
			}
			if (skill.callerTypeIntervalPostProcessingTime!=null && skill.callerTypeIntervalPostProcessingTime.get(i)!=null) {
				AbstractRealDistribution[] d=skill.callerTypeIntervalPostProcessingTime.get(i);
				for (int j=0;j<48;j++) if (d[j]!=null) intervalDataNeeded2[j]=true;
			}
		}

		/* Globale Werte */
		List<AbstractRealDistribution> distWorkingTime=new ArrayList<AbstractRealDistribution>();
		List<AbstractRealDistribution> distPostProcessingTime=new ArrayList<AbstractRealDistribution>();
		for (CallcenterModelSkillLevel skill : baseModel.skills) for (int i=0;i<skill.callerTypeName.size();i++) {
			distWorkingTime.add(skill.callerTypeWorkingTime.get(i));
			distPostProcessingTime.add(skill.callerTypePostProcessingTime.get(i));

		}
		AbstractRealDistribution workingTime=joinDistributions(distWorkingTime,DistributionTools.DistLogNormal[0],useMaxAHT);
		AbstractRealDistribution postProcessingTime=joinDistributions(distPostProcessingTime,DistributionTools.DistLogNormal[0],useMaxAHT);

		/* Intervallbasierende Werte */
		AbstractRealDistribution[] workingTimeInterval=new AbstractRealDistribution[48];
		AbstractRealDistribution[] postProcessingTimeInterval=new AbstractRealDistribution[48];

		for (int j=0;j<48;j++) {
			if (intervalDataNeeded1[j]) {
				List<AbstractRealDistribution> list=new ArrayList<AbstractRealDistribution>();
				for (CallcenterModelSkillLevel skill : baseModel.skills) for (int i=0;i<skill.callerTypeName.size();i++) {
					AbstractRealDistribution d=null;
					if (skill.callerTypeIntervalWorkingTime!=null && skill.callerTypeIntervalWorkingTime.get(i)!=null) d=skill.callerTypeIntervalWorkingTime.get(i)[j];
					if (d==null) d=skill.callerTypeWorkingTime.get(i);
					list.add(d);
				}
				workingTimeInterval[j]=joinDistributions(list,DistributionTools.DistLogNormal[0],useMaxAHT);
			}
			if (intervalDataNeeded2[j]) {
				List<AbstractRealDistribution> list=new ArrayList<AbstractRealDistribution>();
				for (CallcenterModelSkillLevel skill : baseModel.skills) for (int i=0;i<skill.callerTypeName.size();i++) {
					AbstractRealDistribution d=null;
					if (skill.callerTypeIntervalPostProcessingTime!=null && skill.callerTypeIntervalPostProcessingTime.get(i)!=null) d=skill.callerTypeIntervalPostProcessingTime.get(i)[j];
					if (d==null) d=skill.callerTypePostProcessingTime.get(i);
					list.add(d);
				}
				postProcessingTimeInterval[j]=joinDistributions(list,DistributionTools.DistLogNormal[0],useMaxAHT);
			}
		}

		skillLevel.callerTypeName.add(callerTypeName);
		skillLevel.callerTypeWorkingTimeAddOn.add("");
		skillLevel.callerTypeWorkingTime.add(workingTime);
		skillLevel.callerTypePostProcessingTime.add(postProcessingTime);
		skillLevel.callerTypeIntervalWorkingTimeAddOn.add(new String[48]);
		skillLevel.callerTypeIntervalWorkingTime.add(workingTimeInterval);
		skillLevel.callerTypeIntervalPostProcessingTime.add(postProcessingTimeInterval);
		skillLevel.callerTypeScore.add(1);

		return skillLevel;
	}

	/**
	 * Liefert das vereinfachte Callcenter-Modell zurück.
	 * @param useMaxAHT	Beim Zusammenfassen der Kundentypen durchschittliche Bedienzeiten verwenden (<code>false</code>) oder maximale Bedienzeitverteilung verwenden (<code>true</code>)
	 * @return	Vereinfachtes Callcenter-Modell
	 */
	public CallcenterModel calc(boolean useMaxAHT) {
		String callerTypeName="";

		CallcenterModel model=calcBasics();

		CallcenterModelCaller caller=calcCaller();
		if (caller!=null) {model.caller.add(caller); callerTypeName=caller.name;}

		changeCallcenter(callerTypeName,model.callcenter);

		CallcenterModelSkillLevel skillLevel=calcSkillLevel(callerTypeName,useMaxAHT);
		if (skillLevel!=null) model.skills.add(skillLevel);

		model.prepareData();

		return model;
	}
}
