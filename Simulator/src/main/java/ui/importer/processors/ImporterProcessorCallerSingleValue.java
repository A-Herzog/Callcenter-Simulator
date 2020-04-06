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
import java.util.List;

import language.Language;
import mathtools.NumberTools;
import ui.importer.ImporterProcessorSingleValue;
import ui.model.CallcenterModel;
import ui.model.CallcenterModelCaller;

/**
 * Lädt die eine Information, die aus einem einzigen Datenwert besteht, in einen Kundentyp-Datensatz
 * @author Alexander Herzog
 * @version 1.0
 */
public final class ImporterProcessorCallerSingleValue extends ImporterProcessorSingleValue {
	/**
	 * Gibt an, welche Information importiert werden soll
	 * @author Alexander Herzog
	 * @see ImporterProcessorCallerSingleValue#ImporterProcessorCallerSingleValue(CallerValue)
	 */
	public enum CallerValue {
		/** Importiert die Gesamtanzahl an Erstanrufern (die Verteilung über den Tag wird nicht verändert) */
		CALLER_VALUE_COUNT,

		/** Importiert die Standardabweichung der Gesamtanzahl an Erstanrufern (die Verteilung über den Tag wird nicht verändert) */
		CALLER_VALUE_STD,

		/** Importiert die Wiederholwahrscheinlichkeit nach einer Blockierung im ersten Versuch */
		CALLER_VALUE_RETRY_BLOCKED_FIRST,

		/** Importiert die Wiederholwahrscheinlichkeit nach einer Blockierung ab dem zweiten Versuch */
		CALLER_VALUE_RETRY_BLOCKED,

		/** Importiert die Wiederholwahrscheinlichkeit nach einem Warteabbruch im ersten Versuch */
		CALLER_VALUE_RETRY_CANCEL_FIRST,

		/** Importiert die Wiederholwahrscheinlichkeit nach einem Warteabbruch ab dem zweiten Versuch */
		CALLER_VALUE_RETRY_CANCEL,

		/** Importiert die Weiterleitungsrate */
		CALLER_VALUE_CONTINUE
	}

	private final CallerValue mode;

	/**
	 * Konstruktor der Klasse <code>ImporterProcessorCallerSingleValue</code>
	 * @param mode	Gibt an, welche Information importiert werden soll
	 */
	public ImporterProcessorCallerSingleValue(final CallerValue mode) {
		this.mode=mode;
	}

	/* (non-Javadoc)
	 * @see complexcallcenter.importer.ImporterProcessor#getName()
	 */
	@Override
	public String[] getNames() {
		List<String> list=new ArrayList<String>();
		switch (mode) {
		case CALLER_VALUE_COUNT:
			list.add(Language.tr("Importer.NumberOfFreshCallsSingleValue"));
			for (String s: Language.trOther("Importer.NumberOfFreshCallsSingleValue")) if (!list.contains(s)) list.add(s);
			break;
		case CALLER_VALUE_STD:
			list.add(Language.tr("Importer.StdDevOfFreshCallsSingleValue"));
			for (String s: Language.trOther("Importer.StdDevOfFreshCallsSingleValue")) if (!list.contains(s)) list.add(s);
			break;
		case CALLER_VALUE_RETRY_BLOCKED_FIRST:
			list.add(Language.tr("Importer.RetryProbability.BlockedFirst"));
			for (String s: Language.trOther("Importer.RetryProbability.BlockedFirst")) if (!list.contains(s)) list.add(s);
			break;
		case CALLER_VALUE_RETRY_BLOCKED:
			list.add(Language.tr("Importer.RetryProbability.Blocked"));
			for (String s: Language.trOther("Importer.RetryProbability.Blocked")) if (!list.contains(s)) list.add(s);
			break;
		case CALLER_VALUE_RETRY_CANCEL_FIRST:
			list.add(Language.tr("Importer.RetryProbability.CanceledFirst"));
			for (String s: Language.trOther("Importer.RetryProbability.CanceledFirst")) if (!list.contains(s)) list.add(s);
			break;
		case CALLER_VALUE_RETRY_CANCEL:
			list.add(Language.tr("Importer.RetryProbability.Canceled"));
			for (String s: Language.trOther("Importer.RetryProbability.Canceled")) if (!list.contains(s)) list.add(s);
			break;
		case CALLER_VALUE_CONTINUE:
			list.add(Language.tr("Importer.ForwardingProbability"));
			for (String s: Language.trOther("Importer.ForwardingProbability")) if (!list.contains(s)) list.add(s);
			break;
		default:
			list.add(Language.tr("Importer.ForwardingProbability"));
			for (String s: Language.trOther("Importer.ForwardingProbability")) if (!list.contains(s)) list.add(s);
			break;
		}
		return list.toArray(new String[0]);
	}

	@Override
	public ParameterType getParameterType() {
		return ParameterType.PARAMETER_TYPE_CALLER;
	}

	private String processInt2(CallcenterModelCaller caller, double data) {
		switch (mode) {
		case CALLER_VALUE_COUNT:
			if (Math.round(data)!=data) return String.format(Language.tr("Importer.Error.NonNegativeIntegerNeeded"),NumberTools.formatNumberMax(data));
			caller.freshCallsCountMean=(int)Math.round(data);
			return null;
		case CALLER_VALUE_STD:
			if (data<0) return String.format(Language.tr("Importer.Error.NonNegativeNeeded"),NumberTools.formatNumberMax(data));
			caller.freshCallsCountSD=data;
			return null;
		case CALLER_VALUE_RETRY_BLOCKED_FIRST:
			if (data>1) return String.format(Language.tr("Importer.Error.NumberBetween0And1Needed"),NumberTools.formatNumberMax(data));
			caller.retryProbabiltyAfterBlockedFirstRetry=data;
			return null;
		case CALLER_VALUE_RETRY_BLOCKED:
			if (data>1) return String.format(Language.tr("Importer.Error.NumberBetween0And1Needed"),NumberTools.formatNumberMax(data));
			caller.retryProbabiltyAfterBlocked=data;
			return null;
		case CALLER_VALUE_RETRY_CANCEL_FIRST:
			if (data>1) return String.format(Language.tr("Importer.Error.NumberBetween0And1Needed"),NumberTools.formatNumberMax(data));
			caller.retryProbabiltyAfterGiveUpFirstRetry=data;
			return null;
		case CALLER_VALUE_RETRY_CANCEL:
			if (data>1) return String.format(Language.tr("Importer.Error.NumberBetween0And1Needed"),NumberTools.formatNumberMax(data));
			caller.retryProbabiltyAfterGiveUp=data;
			return null;
		case CALLER_VALUE_CONTINUE:
			if (data>1) return String.format(Language.tr("Importer.Error.NumberBetween0And1Needed"),NumberTools.formatNumberMax(data));
			caller.continueProbability=data;
			return null;
		}
		return null;
	}

	/* (non-Javadoc)
	 * @see complexcallcenter.importer.ImportProcessorSingleValue#processInt(complexcallcenter.model.CallcenterModel, java.lang.String, double)
	 */
	@Override
	protected String processInt(CallcenterModel model, String parameter, double data) {
		if (data<0) return String.format(Language.tr("Importer.Error.NonNegativeIntegerNeeded"),NumberTools.formatNumberMax(data));
		if (parameter==null || parameter.isEmpty()) return Language.tr("Importer.Error.NoClientTypeSelected");

		CallcenterModelCaller caller=getCallerFromParameter(model,parameter);
		if (caller==null) return String.format(Language.tr("ClientTypeForValueDoesNotExist"),parameter);

		return processInt2(caller,data);
	}
}
