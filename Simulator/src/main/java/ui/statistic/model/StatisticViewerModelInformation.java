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
package ui.statistic.model;

import org.apache.commons.math3.distribution.AbstractRealDistribution;

import language.Language;
import mathtools.NumberTools;
import mathtools.TimeTools;
import mathtools.distribution.DataDistributionImpl;
import mathtools.distribution.tools.DistributionTools;
import systemtools.statistics.StatisticViewerText;
import ui.model.CallcenterModel;
import ui.model.CallcenterModelAgent;
import ui.model.CallcenterModelCallcenter;
import ui.model.CallcenterModelCaller;
import ui.model.CallcenterModelSkillLevel;

/**
 * Zeigt allgemeine Modellinformationen an.
 * @author Alexander Herzog
 * @version 1.0
 */
public class StatisticViewerModelInformation extends StatisticViewerText {
	/** Callcenter-Editor-Modell dem die Daten entnommen werden sollen */
	private final CallcenterModel model;
	/** Kurz gehaltene, allgemeine Informationen anzeigen (<code>false</code>) oder vollständige Modellbeschreibung (<code>true</code>) */
	private final boolean fullInformation;

	/**
	 * Konstruktor der Klasse
	 * @param model	Callcenter-Editor-Modell dem die Daten entnommen werden sollen
	 * @param fullInformation	Kurz gehaltene, allgemeine Informationen anzeigen (<code>false</code>) oder vollständige Modellbeschreibung (<code>true</code>)
	 */
	public StatisticViewerModelInformation(final CallcenterModel model, final boolean fullInformation) {
		super();
		this.model=model;
		this.fullInformation=fullInformation;
	}

	/**
	 * Generiert die kurz gehaltenen, allgemeinen Informationen
	 * @see #buildText()
	 */
	private void buildShortText() {
		addHeading(1,Language.tr("SimStatistic.ModelInformation.Title"));

		addHeading(2,Language.tr("SimStatistic.ModelInformation.Name.Long"));
		beginParagraph();
		addLine(model.name);
		endParagraph();

		if (model.date!=null && !model.date.trim().isEmpty()) {
			addHeading(2,Language.tr("SimStatistic.ModelInformation.Date"));
			beginParagraph();
			addLine(CallcenterModel.dateToLocalString(CallcenterModel.stringToDate(model.date)));
			endParagraph();
		}

		addHeading(2,Language.tr("SimStatistic.ModelInformation.Description"));
		beginParagraph();
		addLines(model.description);
		endParagraph();

		addHeading(2,Language.tr("SimStatistic.ServiceLevel"));
		beginParagraph();
		addLine(String.format(Language.tr("SimStatistic.ServiceLevel.Description"),""+model.serviceLevelSeconds));
		for (int i=0;i<model.caller.size();i++) if (model.caller.get(i).serviceLevelSeconds>0) {
			addLine(String.format(Language.tr("SimStatistic.ServiceLevel.ForCallerType"),model.caller.get(i).name)+": "+String.format(Language.tr("SimStatistic.ServiceLevel.Description"),""+model.caller.get(i).serviceLevelSeconds));
		}
		endParagraph();

		addHeading(2,Language.tr("SimStatistic.ModelInformation.NumberOfDaysToBeSimulated"));
		beginParagraph();
		addLine(""+model.days);
		endParagraph();
	}

	/**
	 * Gibt eine Verteilung aus.
	 * @param data	Verteilung
	 */
	private void listDataDistribution(final DataDistributionImpl data) {
		int count=0;
		final int secondsPerInterval=86400/data.densityData.length;
		final int perLine=8; /* Einträge pro Zeile */
		for (int i=0;i<data.densityData.length/perLine;i++) {
			StringBuilder s=new StringBuilder();
			for (int j=0;j<perLine;j++) {
				if (s.length()>0) s.append("; ");
				s.append(NumberTools.formatNumberMax(data.densityData[count++]));
			}
			addLine(TimeTools.formatTime(i*perLine*secondsPerInterval)+"-"+TimeTools.formatTime((i+1)*perLine*secondsPerInterval-1)+": "+s.toString());
		}
	}

	/**
	 * Erstellt eine Beschreibung zu einer Anrufergruppe.
	 * @param caller	Anrufergruppe
	 * @see #buildLongText()
	 */
	private void buildLongCallerText(final CallcenterModelCaller caller) {
		String s="";
		DataDistributionImpl data;

		if (!caller.active) s=" ("+Language.tr("Dialog.deactivated.lower")+")";

		addHeading(2,Language.tr("SimStatistic.CallerGroup")+" \""+caller.name+"\""+s);

		addHeading(3,Language.tr("SimStatistic.Arrivals"));
		beginParagraph();
		addLine(Language.tr("SimStatistic.FreshCalls.PerDay")+": "+caller.freshCallsCountMean);
		if (caller.freshCallsCountSD>0) addLine(Language.tr("SimStatistic.FreshCalls.StdDev")+": "+NumberTools.formatNumber(caller.freshCallsCountSD,1));
		data=null;
		if (caller.freshCallsDist24!=null) {data=caller.freshCallsDist24.clone(); s=Language.tr("Statistic.Units.IntervalHour");}
		if (caller.freshCallsDist48!=null) {data=caller.freshCallsDist48.clone(); s=Language.tr("Statistic.Units.IntervalHalfHour");}
		if (caller.freshCallsDist96!=null) {data=caller.freshCallsDist96.clone(); s=Language.tr("Statistic.Units.IntervalQuarterHour");}

		if (data!=null) {
			data.normalizeDensity();
			data=data.multiply(caller.freshCallsCountMean).round();
			addLine(String.format(Language.tr("SimStatistic.FreshCalls.Per"),s)+":");
			listDataDistribution(data);
		}
		endParagraph();

		addHeading(3,Language.tr("SimStatistic.ScoreValues"));
		beginParagraph();
		addLine(Language.tr("SimStatistic.ScoreValues.Base")+": "+NumberTools.formatNumberMax(caller.scoreBase));
		addLine(Language.tr("SimStatistic.ScoreValues.WaitingSecond")+": "+NumberTools.formatNumberMax(caller.scoreSecond));
		addLine(Language.tr("SimStatistic.ScoreValues.ForwardedCall")+": "+NumberTools.formatNumberMax(caller.scoreContinued));
		endParagraph();

		addHeading(3,Language.tr("SimStatistic.WaitingTimeTolerance"));
		beginParagraph();
		switch (caller.waitingTimeMode) {
		case CallcenterModelCaller.WAITING_TIME_MODE_OFF:
			addLine(Language.tr("SimStatistic.WaitingTimeTolerance.ModeInfinity"));
			break;
		case CallcenterModelCaller.WAITING_TIME_MODE_SHORT:
			addLine(Language.tr("SimStatistic.WaitingTimeTolerance.ModeDistribution")+": "+DistributionTools.getDistributionName(caller.waitingTimeDist)+" ("+DistributionTools.getDistributionInfo(caller.waitingTimeDist)+")");
			break;
		case CallcenterModelCaller.WAITING_TIME_MODE_LONG:
			addLine(Language.tr("SimStatistic.WaitingTimeTolerance.ModeDistribution")+": "+DistributionTools.getDistributionName(caller.waitingTimeDistLong)+" ("+DistributionTools.getDistributionInfo(caller.waitingTimeDistLong)+")");
			break;
		case CallcenterModelCaller.WAITING_TIME_MODE_CALC:
			addLine(Language.tr("SimStatistic.WaitingTimeTolerance.ModeEstimationWaitingTime")+": "+NumberTools.formatNumberMax(caller.waitingTimeCalcMeanWaitingTime));
			addLine(Language.tr("SimStatistic.WaitingTimeTolerance.ModeEstimationCancelTime")+": "+NumberTools.formatNumberMax(caller.waitingTimeCalcCancelProbability));
			addLine(Language.tr("SimStatistic.WaitingTimeTolerance.ModeEstimationCorrectionValue")+": "+NumberTools.formatNumberMax(caller.waitingTimeCalcAdd));
			break;
		}
		endParagraph();

		if (caller.waitingTimeMode!=CallcenterModelCaller.WAITING_TIME_MODE_OFF) {
			addHeading(3,Language.tr("SimStatistic.Retrys"));
			beginParagraph();
			Double d;
			addLine(Language.tr("SimStatistic.RetryRate.Blocked")+"- "+Language.tr("SimStatistic.RetryRate.Try1")+": "+NumberTools.formatNumberMax(caller.retryProbabiltyAfterBlockedFirstRetry*100)+"%");
			d=0.0; for (int i=0;i<caller.retryCallerTypeRateAfterBlockedFirstRetry.size();i++) d+=caller.retryCallerTypeRateAfterBlockedFirstRetry.get(i);
			if (d>0) for (int i=0;i<caller.retryCallerTypeRateAfterBlockedFirstRetry.size();i++) addLine(String.format(Language.tr("SimStatistic.RetryChangeRate.Blocked"),caller.retryCallerTypeAfterBlockedFirstRetry.get(i))+" - "+Language.tr("SimStatistic.RetryRate.Try1")+": "+NumberTools.formatNumberMax(caller.retryCallerTypeRateAfterBlockedFirstRetry.get(i)));
			addLine(Language.tr("SimStatistic.RetryRate.Blocked")+" - "+Language.tr("SimStatistic.RetryRate.Try2")+": "+NumberTools.formatNumberMax(caller.retryProbabiltyAfterBlocked*100)+"%");
			d=0.0; for (int i=0;i<caller.retryCallerTypeRateAfterBlocked.size();i++) d+=caller.retryCallerTypeRateAfterBlocked.get(i);
			if (d>0) for (int i=0;i<caller.retryCallerTypeRateAfterBlocked.size();i++) addLine(String.format(Language.tr("SimStatistic.RetryChangeRate.Blocked"),caller.retryCallerTypeAfterBlocked.get(i))+" - "+Language.tr("SimStatistic.RetryRate.Try2")+": "+NumberTools.formatNumberMax(caller.retryCallerTypeRateAfterBlocked.get(i)));
			addLine(Language.tr("SimStatistic.RetryRate.Canceled")+" - "+Language.tr("SimStatistic.RetryRate.Try1")+": "+NumberTools.formatNumberMax(caller.retryProbabiltyAfterGiveUpFirstRetry*100)+"%");
			d=0.0; for (int i=0;i<caller.retryCallerTypeRateAfterGiveUpFirstRetry.size();i++) d+=caller.retryCallerTypeRateAfterGiveUpFirstRetry.get(i);
			if (d>0) for (int i=0;i<caller.retryCallerTypeRateAfterGiveUpFirstRetry.size();i++) addLine(String.format(Language.tr("SimStatistic.RetryChangeRate.Canceled"),caller.retryCallerTypeAfterGiveUpFirstRetry.get(i))+" - "+Language.tr("SimStatistic.RetryRate.Try1")+": "+NumberTools.formatNumberMax(caller.retryCallerTypeRateAfterGiveUpFirstRetry.get(i)));
			addLine(Language.tr("SimStatistic.RetryRate.Canceled")+" - "+Language.tr("SimStatistic.RetryRate.Try2")+": "+NumberTools.formatNumberMax(caller.retryProbabiltyAfterGiveUp*100)+"%");
			d=0.0; for (int i=0;i<caller.retryCallerTypeRateAfterGiveUp.size();i++) d+=caller.retryCallerTypeRateAfterGiveUp.get(i);
			if (d>0) for (int i=0;i<caller.retryCallerTypeRateAfterGiveUp.size();i++) addLine(String.format(Language.tr("SimStatistic.RetryChangeRate.Canceled"),caller.retryCallerTypeAfterGiveUp.get(i))+" - "+Language.tr("SimStatistic.RetryRate.Try2")+": "+NumberTools.formatNumberMax(caller.retryCallerTypeRateAfterGiveUp.get(i)));
			addLine("Verteilung der Wiederholabstände: "+DistributionTools.getDistributionName(caller.retryTimeDist)+" ("+DistributionTools.getDistributionInfo(caller.retryTimeDist)+")");
			endParagraph();
		}

		if (caller.continueTypeSkillType.size()>0) s=" ("+Language.tr("SimStatistic.Global.lower")+")"; else s="";
		addHeading(3,Language.tr("SimStatistic.ForwardedCalls")+s);
		beginParagraph();
		addLine(Language.tr("SimStatistic.Forwarding.GlobalProbability")+": "+NumberTools.formatNumberMax(caller.continueProbability*100)+"%");
		for (int i=0;i<caller.continueTypeName.size();i++) addLine(String.format(Language.tr("SimStatistic.Forwarding.ChangeRate"),caller.continueTypeName.get(i))+": "+NumberTools.formatNumberMax(caller.continueTypeRate.get(i)));
		endParagraph();

		for (int i=0;i<caller.continueTypeSkillType.size();i++) {
			addHeading(3,String.format(Language.tr("SimStatistic.Forwarding.OnAgentSkillLevel"),caller.continueTypeSkillType.get(i)));
			beginParagraph();
			addLine(Language.tr("SimStatistic.Forwarding.ProbabilityOnAgentSkillLevel")+": "+NumberTools.formatNumberMax(caller.continueTypeSkillTypeProbability.get(i)*100)+"%");
			for (int j=0;j<caller.continueTypeSkillTypeName.get(i).size();j++) addLine(String.format(Language.tr("SimStatistic.Forwarding.ChangeRate"),caller.continueTypeSkillTypeName.get(i).get(j))+": "+NumberTools.formatNumberMax(caller.continueTypeSkillTypeRate.get(i).get(j)));
			endParagraph();
		}

		addHeading(3,Language.tr("SimStatistic.CostStructure"));
		beginParagraph();
		addLine(Language.tr("SimStatistic.CostStructure.YieldPerClient")+": "+NumberTools.formatNumber(caller.revenuePerClient,2));
		addLine(Language.tr("SimStatistic.CostStructure.CostPerCanceledCall")+": "+NumberTools.formatNumber(caller.costPerCancel,2));
		addLine(Language.tr("SimStatistic.CostStructure.CostPerWaitingSecond")+": "+NumberTools.formatNumber(caller.costPerWaitingSec,2));
		endParagraph();
	}

	/**
	 * Erstellt eine Beschreibung zu einem Callcenter.
	 * @param callcenter	Callcenter
	 * @param model	Gesamtes Modell
	 * @see #buildLongText()
	 */
	private void buildLongCallcenterText(final CallcenterModelCallcenter callcenter, final CallcenterModel model) {
		String s="";

		if (!callcenter.active) s=" ("+Language.tr("Dialog.deactivated.lower")+")";

		addHeading(2,Language.tr("SimStatistic.Callcenter")+" \""+callcenter.name+"\""+s);

		beginParagraph();
		addLine(Language.tr("SimStatistic.TechnicalFreeTime.Info")+": "+callcenter.technicalFreeTime);
		if (!callcenter.technicalFreeTimeIsWaitingTime) addLine(Language.tr("SimStatistic.TechnicalFreeTime.IsNotWaitingTime"));
		addLine(Language.tr("SimStatistic.ScoreValues.Callcenter")+": "+callcenter.score);
		addLine(Language.tr("SimStatistic.ScoreValues.CallcenterAgentFreeTimeSinceLastCall")+": "+NumberTools.formatNumberMax(callcenter.agentScoreFreeTimeSinceLastCall));
		addLine(Language.tr("SimStatistic.ScoreValues.CallcenterAgentFreeTimePart")+": "+NumberTools.formatNumberMax(callcenter.agentScoreFreeTimePart));
		endParagraph();

		if (callcenter.callerMinWaitingTimeName.size()>0) {
			addHeading(3,Language.tr("SimStatistic.MinimumWaitingTime"));
			beginParagraph();
			for (int i=0;i<callcenter.callerMinWaitingTimeName.size();i++) addLine(String.format(Language.tr("SimStatistic.MinimumWaitingTime.Info"),callcenter.callerMinWaitingTimeName.get(i))+": "+callcenter.callerMinWaitingTime.get(i)+" "+Language.tr("Statistic.Seconds"));
			endParagraph();
		}

		for (int i=0;i<callcenter.agents.size();i++) buildLongAgentText(i+1,callcenter.agents.get(i),callcenter,model);
	}

	/**
	 * Erstellt eine Beschreibung zu einer Agentengruppe.
	 * @param nr	1-basierte Nummer der Agentengruppe innerhalb des Callcenters
	 * @param agent	Agentengruppe
	 * @param callcenter	Callcenter in dem sich die Agentengruppe befindet
	 * @param model	Gesamtes Modell
	 * @see #buildLongText()
	 */
	private void buildLongAgentText(final int nr, CallcenterModelAgent agent, final CallcenterModelCallcenter callcenter, final CallcenterModel model) {
		String s="";

		if (!agent.active) s=" ("+Language.tr("Dialog.deactivated.lower")+")";
		addHeading(3,Language.tr("SimStatistic.AgentGroup")+" "+nr+s);

		beginParagraph();
		addLine(Language.tr("SimStatistic.SkillLevel")+": "+agent.skillLevel);
		int shift=(agent.preferredShiftLength>=0)?agent.preferredShiftLength:model.preferredShiftLength;
		addLine(Language.tr("SimStatistic.Shift.PreferredLength")+": "+NumberTools.formatNumber((double)shift/2,1)+" "+Language.tr("Statistic.Units.Hours.lower"));

		if (agent.count>=0) {
			addLine(Language.tr("SimStatistic.Shift.Size")+": "+agent.count);
			addLine(Language.tr("SimStatistic.Shift.Start")+": "+TimeTools.formatTime(agent.workingTimeStart*1800));
			if (agent.workingNoEndTime) s=Language.tr("SimStatistic.Shift.OpenEnd"); else s=TimeTools.formatTime(agent.workingTimeEnd*1800);
			addLine(Language.tr("SimStatistic.Shift.End")+": "+s);
		}

		if (agent.count==-1) {
			DataDistributionImpl efficiency=agent.getEfficiency(callcenter,model);
			if (efficiency.getMin()<1 || efficiency.getMax()>1) {
				addLine(Language.tr("SimStatistic.Productivity.GrossAgents"));
				if (agent.countPerInterval24!=null) listDataDistribution(agent.countPerInterval24);
				if (agent.countPerInterval48!=null) listDataDistribution(agent.countPerInterval48);
				if (agent.countPerInterval96!=null) listDataDistribution(agent.countPerInterval96);
				addLine(Language.tr("SimStatistic.Productivity.NetAgents"));
				if (agent.countPerInterval24!=null) listDataDistribution(agent.countPerInterval24.multiply(efficiency).round());
				if (agent.countPerInterval24!=null) listDataDistribution(agent.countPerInterval48.multiply(efficiency).round());
				if (agent.countPerInterval24!=null) listDataDistribution(agent.countPerInterval96.multiply(efficiency).round());
			} else {
				addLine(Language.tr("SimStatistic.Productivity.ActiveAgents"));
				if (agent.countPerInterval24!=null) listDataDistribution(agent.countPerInterval24);
				if (agent.countPerInterval48!=null) listDataDistribution(agent.countPerInterval48);
				if (agent.countPerInterval96!=null) listDataDistribution(agent.countPerInterval96);
			}
			DataDistributionImpl addition=agent.getAddition(callcenter,model);
			if (addition.getMin()<1 || addition.getMax()>1) {
				addLine(Language.tr("SimStatistic.Productivity.DoubleGrossAgents"));
				if (agent.countPerInterval24!=null) listDataDistribution(agent.countPerInterval24.multiply(addition).round());
				if (agent.countPerInterval48!=null) listDataDistribution(agent.countPerInterval48.multiply(addition).round());
				if (agent.countPerInterval96!=null) listDataDistribution(agent.countPerInterval96.multiply(addition).round());
			}
		}

		if (agent.count==-2) {
			addLine(String.format(Language.tr("SimStatistic.AgentsModelingCallerDistribution"),NumberTools.formatNumber((double)(agent.byCallersAvailableHalfhours)/2)));
			for (int i=0;i<agent.byCallers.size();i++) addLine(String.format(Language.tr("SimStatistic.AgentsModelingCallerDistribution.CallerType"),agent.byCallers.get(i))+": "+NumberTools.formatNumberMax(agent.byCallersRate.get(i)));
			addLine(Language.tr("SimStatistic.AgentsModelingCallerDistribution.Info"));
		}

		endParagraph();

		beginParagraph();
		addLine(Language.tr("SimStatistic.CostStructure.WorkingHour")+": "+NumberTools.formatNumber(agent.costPerWorkingHour,2));
		for (int i=0;i<agent.costCallerTypes.size();i++) {
			addLine(String.format(Language.tr("SimStatistic.CostStructure.AgentConversation"),agent.costCallerTypes.get(i))+": "+NumberTools.formatNumber(agent.costPerCall.get(i),2));
			addLine(String.format(Language.tr("SimStatistic.CostStructure.AgentMinute"),agent.costCallerTypes.get(i))+": "+NumberTools.formatNumber(agent.costPerCallMinute.get(i),2));
		}
		endParagraph();
	}

	/**
	 * Erstellt eine Beschreibung zu einem Skill-Level
	 * @param skill	Skill-Level
	 * @see #buildLongText()
	 */
	private void buildLongSkillsText(final CallcenterModelSkillLevel skill) {
		addHeading(2,Language.tr("SimStatistic.SkillLevel")+" \""+skill.name+"\"");

		String[] sArray;
		AbstractRealDistribution[] dArray;
		for (int i=0;i<skill.callerTypeName.size();i++) {
			addHeading(3,Language.tr("SimStatistic.ClientType.Short")+" \""+skill.callerTypeName.get(i)+"\"");
			beginParagraph();
			addLine(Language.tr("SimStatistic.ScoreValues.Agent")+": "+skill.callerTypeScore.get(i));
			addLine(Language.tr("SimStatistic.HoldingTime.Distribution")+": "+DistributionTools.getDistributionName(skill.callerTypeWorkingTime.get(i))+" ("+DistributionTools.getDistributionInfo(skill.callerTypeWorkingTime.get(i))+")");
			if (skill.callerTypeWorkingTimeAddOn.get(i)!=null && !skill.callerTypeWorkingTimeAddOn.get(i).isEmpty() && !skill.callerTypeWorkingTimeAddOn.get(i).equals("0")) {
				addLine(Language.tr("SimStatistic.HoldingTimeAddOn.Value")+": "+skill.callerTypeWorkingTimeAddOn.get(i));
			}
			sArray=skill.callerTypeIntervalWorkingTimeAddOn.get(i);
			for (int j=0;j<48;j++) if (sArray[j]!=null) addLine(Language.tr("SimStatistic.HoldingTimeAddOnAddOn.Custom")+" "+TimeTools.formatTime(j*1800)+"-"+TimeTools.formatTime((j+1)*1800)+": "+sArray[j]);
			dArray=skill.callerTypeIntervalWorkingTime.get(i);
			for (int j=0;j<48;j++) if (dArray[j]!=null) addLine(Language.tr("SimStatistic.HoldingTime.CustomDistribution")+" "+TimeTools.formatTime(j*1800)+"-"+TimeTools.formatTime((j+1)*1800)+": "+DistributionTools.getDistributionName(dArray[j])+" ("+DistributionTools.getDistributionInfo(dArray[j])+")");
			addLine(Language.tr("SimStatistic.PostProcessingTime.Distribution")+": "+DistributionTools.getDistributionName(skill.callerTypePostProcessingTime.get(i))+" ("+DistributionTools.getDistributionInfo(skill.callerTypePostProcessingTime.get(i))+")");
			dArray=skill.callerTypeIntervalPostProcessingTime.get(i);
			for (int j=0;j<48;j++) if (dArray[j]!=null) addLine(Language.tr("SimStatistic.PostProcessingTime.CustomDistribution")+" "+TimeTools.formatTime(j*1800)+"-"+TimeTools.formatTime((j+1)*1800)+": "+DistributionTools.getDistributionName(dArray[j])+" ("+DistributionTools.getDistributionInfo(dArray[j])+")");
			endParagraph();
		}
	}

	/**
	 * Generiert die vollständige Modellbeschreibung.
	 * @see #buildText()
	 */
	private void buildLongText() {
		addHeading(1,Language.tr("SimStatistic.CallcenterModell")+" \""+model.name+"\"");

		addHeading(2,Language.tr("SimStatistic.ModelInformation.Description"));
		beginParagraph();
		addLines(model.description);
		endParagraph();

		if (model.date!=null && !model.date.trim().isEmpty()) {
			addHeading(2,Language.tr("SimStatistic.ModelInformation.Date"));
			beginParagraph();
			addLine(CallcenterModel.dateToLocalString(CallcenterModel.stringToDate(model.date)));
			endParagraph();
		}

		addHeading(2,Language.tr("SimStatistic.ModelInformation.GlobalSettings"));
		beginParagraph();
		addLine(Language.tr("SimStatistic.ServiceLevel")+": "+String.format(Language.tr("SimStatistic.ServiceLevel.Description"),""+model.serviceLevelSeconds));
		for (int i=0;i<model.caller.size();i++) if (model.caller.get(i).serviceLevelSeconds>0) {
			addLine(String.format(Language.tr("SimStatistic.ServiceLevel.ForCallerType"),model.caller.get(i).name)+": "+String.format(Language.tr("SimStatistic.ServiceLevel.Description"),""+model.caller.get(i).serviceLevelSeconds));
		}
		addLine(Language.tr("SimStatistic.ModelInformation.NumberOfDaysToBeSimulated")+": "+model.days);
		addLine(Language.tr("SimStatistic.MaximumQueueLength")+": "+model.maxQueueLength);
		addLine(Language.tr("SimStatistic.Shift.PreferredLength")+": "+NumberTools.formatNumber((double)(model.preferredShiftLength)/2,1)+" Stunden");
		endParagraph();

		for (int i=0;i<model.caller.size();i++) buildLongCallerText(model.caller.get(i));
		for (int i=0;i<model.callcenter.size();i++) buildLongCallcenterText(model.callcenter.get(i),model);
		for (int i=0;i<model.skills.size();i++) buildLongSkillsText(model.skills.get(i));
	}

	@Override
	protected void buildText() {
		if (fullInformation) buildLongText(); else buildShortText();
	}
}
