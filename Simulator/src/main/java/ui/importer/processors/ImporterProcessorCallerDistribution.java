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
package ui.importer.processors;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import language.Language;
import mathtools.distribution.DataDistributionImpl;
import ui.importer.ImporterProcessor;
import ui.model.CallcenterModel;
import ui.model.CallcenterModelCaller;

/**
 * Lädt die Verteilung der Erstanrufern (die Anzahl an Erstanrufern wird nicht verändert)
 * @author Alexander Herzog
 * @version 1.0
 */
public final class ImporterProcessorCallerDistribution extends ImporterProcessor {
	/**
	 * Gibt an, welche Verteilung geladen werden soll
	 * @author Alexander Herzog
	 * @see ImporterProcessorCallerDistribution#ImporterProcessorCallerDistribution(CallerDist)
	 */
	public enum CallerDist {
		/** Lädt die Verteilung der Erstanrufern (die Anzahl an Erstanrufern wird nicht verändert) */
		CALLER_DIST_FRESH_CALLS,

		/** Lädt die Summe der Erstanrufern (die Verteilung der Erstanrufern wird nicht verändert) */
		CALLER_DIST_FRESH_CALLS_COUNT,

		/** Lädt die Verteilung der Erstanrufern und setzt die Anzahl an Erstanrufern auf die Summe der Verteilungswerte */
		CALLER_DIST_FRESH_CALLS_AND_COUNT,

		/** Lädt die Wartezeittoleranz-Verteilung für kurze Wartezeittoleranzen */
		CALLER_DIST_WAITING_TIME_TOLERANCE_SHORT,

		/** Lädt die Wartezeittoleranz-Verteilung für lange Wartezeittoleranzen */
		CALLER_DIST_WAITING_TIME_TOLERANCE_LONG,

		/** Lädt die Verteilung der Wiederholabstände */
		CALLER_DIST_RETRY_TIMES,

		/** Lädt die Verteilung der Wiederanrufbstände */
		CALLER_DIST_RECALL_TIMES
	}

	/** Gibt an, welche Verteilung geladen werden soll */
	private final CallerDist mode;

	/**
	 * Konstruktor der Klasse
	 * @param mode	Gibt an, welche Verteilung geladen werden soll
	 */
	public ImporterProcessorCallerDistribution(final CallerDist mode) {
		this.mode=mode;
	}

	@Override
	public String[] getNames() {
		List<String> list=new ArrayList<String>();
		switch (mode) {
		case CALLER_DIST_FRESH_CALLS:
			list.add(Language.tr("Importer.FreshCallsDistribution"));
			for (String s: Language.trOther("Importer.FreshCallsDistribution")) if (!list.contains(s)) list.add(s);
			break;
		case CALLER_DIST_FRESH_CALLS_COUNT:
			list.add(Language.tr("Importer.NumberOfFreshCalls"));
			for (String s: Language.trOther("Importer.FreshCallsDistribution")) if (!list.contains(s)) list.add(s);
			break;
		case CALLER_DIST_FRESH_CALLS_AND_COUNT:
			list.add(Language.tr("Importer.NumberAndDistributionOfFreshCalls"));
			for (String s: Language.trOther("Importer.FreshCallsDistribution")) if (!list.contains(s)) list.add(s);
			break;
		case CALLER_DIST_WAITING_TIME_TOLERANCE_SHORT:
			list.add(Language.tr("Importer.WaitingTimeToleranceSecondsBasis"));
			for (String s: Language.trOther("Importer.FreshCallsDistribution")) if (!list.contains(s)) list.add(s);
			break;
		case CALLER_DIST_WAITING_TIME_TOLERANCE_LONG:
			list.add(Language.tr("Importer.WaitingTimeToleranceHalfHoursBasis"));
			for (String s: Language.trOther("Importer.FreshCallsDistribution")) if (!list.contains(s)) list.add(s);
			break;
		case CALLER_DIST_RETRY_TIMES:
			list.add(Language.tr("Importer.RetryIntervalsDistribution"));
			for (String s: Language.trOther("Importer.FreshCallsDistribution")) if (!list.contains(s)) list.add(s);
			break;
		case CALLER_DIST_RECALL_TIMES:
			list.add(Language.tr("Importer.RecallIntervalsDistribution"));
			for (String s: Language.trOther("Importer.FreshCallsDistribution")) if (!list.contains(s)) list.add(s);
			break;
		default:
			list.add(Language.tr("Importer.FreshCallsDistribution"));
			for (String s: Language.trOther("Importer.FreshCallsDistribution")) if (!list.contains(s)) list.add(s);
			break;
		}
		return list.toArray(new String[0]);
	}

	@Override
	public ParameterType getParameterType() {
		return ParameterType.PARAMETER_TYPE_CALLER;
	}

	private String processInt(CallcenterModelCaller caller, double[] data) {
		double[] data2;
		switch (mode) {
		case CALLER_DIST_FRESH_CALLS:
			if (data.length!=24 && data.length!=48 && data.length!=96) return String.format(Language.tr("Importer.Error.WrongNumberOfFreshCallsDistributionValues"),data.length);
			caller.freshCallsDist24=null;
			caller.freshCallsDist48=null;
			caller.freshCallsDist96=null;
			switch (data.length) {
			case 24:
				caller.freshCallsDist24=new DataDistributionImpl(CallcenterModelCaller.freshCallsDistMaxX,data);
				break;
			case 48:
				caller.freshCallsDist48=new DataDistributionImpl(CallcenterModelCaller.freshCallsDistMaxX,data);
				break;
			case 96:
				caller.freshCallsDist96=new DataDistributionImpl(CallcenterModelCaller.freshCallsDistMaxX,data);
				break;
			}
			return null;
		case CALLER_DIST_FRESH_CALLS_COUNT:
			/* if (data.length!=1) return "Die Erstanruferanzahl muss aus genau einer Zellen bestehen.";*/
			double sum=data[0]; for (int i=1;i<data.length;i++) sum+=data[i];
			caller.freshCallsCountMean=(int)Math.round(sum);
			return null;
		case CALLER_DIST_FRESH_CALLS_AND_COUNT:
			if (data.length!=24 && data.length!=48 && data.length!=96) return String.format(Language.tr("Importer.Error.WrongNumberOfFreshCallsDistributionValues"),data.length);
			caller.freshCallsDist24=null;
			caller.freshCallsDist48=null;
			caller.freshCallsDist96=null;
			switch (data.length) {
			case 24:
				caller.freshCallsDist24=new DataDistributionImpl(CallcenterModelCaller.freshCallsDistMaxX,data);
				caller.freshCallsCountMean=(int)Math.round(caller.freshCallsDist24.sum());
				break;
			case 48:
				caller.freshCallsDist48=new DataDistributionImpl(CallcenterModelCaller.freshCallsDistMaxX,data);
				caller.freshCallsCountMean=(int)Math.round(caller.freshCallsDist48.sum());
				break;
			case 96:
				caller.freshCallsDist96=new DataDistributionImpl(CallcenterModelCaller.freshCallsDistMaxX,data);
				caller.freshCallsCountMean=(int)Math.round(caller.freshCallsDist96.sum());
				break;
			}
			return null;
		case CALLER_DIST_WAITING_TIME_TOLERANCE_SHORT:
			/* data2=Arrays.copyOf(data,Math.min(data.length,CallcenterModelCaller.waitingTimeDistMaxX+1)); */
			caller.waitingTimeDist=new DataDistributionImpl(data.length-1,data);
			caller.waitingTimeMode=CallcenterModelCaller.WAITING_TIME_MODE_SHORT;
			return null;
		case CALLER_DIST_WAITING_TIME_TOLERANCE_LONG:
			data2=Arrays.copyOf(data,Math.min(data.length,CallcenterModelCaller.waitingTimeDistLongMaxX/1800+1));
			caller.waitingTimeDistLong=new DataDistributionImpl((data2.length-1)*1800,data2);
			caller.waitingTimeMode=CallcenterModelCaller.WAITING_TIME_MODE_LONG;
			return null;
		case CALLER_DIST_RETRY_TIMES:
			data2=Arrays.copyOf(data,Math.min(data.length,CallcenterModelCaller.retryTimeDistMaxX+1));
			caller.retryTimeDist=new DataDistributionImpl(data2.length-1,data2);
			return null;
		case CALLER_DIST_RECALL_TIMES:
			data2=Arrays.copyOf(data,Math.min(data.length,CallcenterModelCaller.recallTimeDistMaxX+1));
			caller.recallTimeDist=new DataDistributionImpl(data2.length-1,data2);
			return null;
		}
		return null;
	}

	@Override
	public String processNumbers(CallcenterModel model, String parameter, double[] data) {
		for (int i=0;i<data.length;i++) if (data[i]<0) return String.format(Language.tr("Importer.Error.ValueNegative"),i+1);

		CallcenterModelCaller caller=getCallerFromParameter(model,parameter);
		if (caller==null) return String.format(Language.tr("Importer.Error.ClientTypeForDistributionDoesNotExist"));

		return processInt(caller,data);
	}
}
