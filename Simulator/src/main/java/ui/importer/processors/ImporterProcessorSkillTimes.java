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

import org.apache.commons.math3.distribution.AbstractRealDistribution;

import language.Language;
import mathtools.TimeTools;
import mathtools.distribution.DataDistributionImpl;
import ui.importer.ImporterProcessor;
import ui.model.CallcenterModel;
import ui.model.CallcenterModelSkillLevel;

/**
 * Lädt Bedien- und Nachbearbeitungszeiten.
 * @author Alexander Herzog
 * @version 1.0
 */
public class ImporterProcessorSkillTimes extends ImporterProcessor {
	/**
	 * Gibt an, was geladen werden soll
	 * @author Alexander Herzog
	 * @see ImporterProcessorSkillTimes#ImporterProcessorSkillTimes(SkillMode, int)
	 */
	public enum SkillMode {
		/** Lädt die Bedienzeitverteilung */
		SKILL_WORKING_TIME,

		/** Lädt die Nachbearbeitungszeitverteilung */
		SKILL_POSTPROCESSING_TIME,

		/** Lädt die wartezeitabhängige Bedienzeitverlängerung */
		SKILL_WORKING_TIME_ADDON
	}

	/** Gibt an, was geladen werden soll */
	private final SkillMode mode;
	/** Gibt das Intervall an, um das es sich handelt. (-1 für globale Verteilung) */
	private final int interval;

	/**
	 * Konstruktor der Klasse
	 * @param mode	Gibt an, was geladen werden soll
	 * @param interval Gibt das Intervall an, um das es sich handelt. (-1 für globale Verteilung)
	 */
	public ImporterProcessorSkillTimes(final SkillMode mode, final int interval) {
		this.mode=mode;
		this.interval=interval;
	}

	@Override
	public String[] getNames() {
		String s=Language.tr("SimStatistic.Global.lower");
		if (interval>=0) s=TimeTools.formatTime(interval*1800)+"-"+TimeTools.formatTime((interval+1)*1800-1);
		if (!s.isEmpty()) s=" ("+s+")";
		List<String> list=new ArrayList<>();
		switch (mode) {
		case SKILL_WORKING_TIME:
			list.add(Language.tr("Importer.HoldingTimeDistribution")+s);
			for (String l: Language.trOther("Importer.HoldingTimeDistribution")) if (!list.contains(l+s)) list.add(l+s);
			break;
		case SKILL_POSTPROCESSING_TIME:
			list.add(Language.tr("Importer.PostProcessingTimeDistribution")+s);
			for (String l: Language.trOther("Importer.PostProcessingTimeDistribution")) if (!list.contains(l+s)) list.add(l+s);
			break;
		case SKILL_WORKING_TIME_ADDON:
			list.add(Language.tr("Importer.HoldingTimeAddOn")+s);
			for (String l: Language.trOther("Importer.HoldingTimeAddOn")) if (!list.contains(l+s)) list.add(l+s);
			break;
		default:
			list.add(Language.tr("Importer.HoldingTimeDistribution")+s);
			for (String l: Language.trOther("Importer.HoldingTimeDistribution")) if (!list.contains(l+s)) list.add(l+s);
			break;
		}
		return list.toArray(String[]::new);
	}

	@Override
	public ParameterType getParameterType() {
		return ParameterType.PARAMETER_TYPE_SKILL_TIME;
	}
	@Override
	public boolean isNumbericalParameter() {
		return mode!=SkillMode.SKILL_WORKING_TIME_ADDON;
	}

	@Override
	public String processNumbers(CallcenterModel model, String parameter, double[] data) {
		Object[] obj=getSkillLevelFromParameter(model,parameter);
		if (obj==null) return String.format(Language.tr("Importer.Error.SkillDoesNotExist"),parameter);
		CallcenterModelSkillLevel skill=(CallcenterModelSkillLevel)(obj[0]);
		int nr=(Integer)(obj[1]);

		for (int i=0;i<data.length;i++) if (data[i]<0) return String.format(Language.tr("Importer.Error.NeedNonNegativeIntegerNumbers"),i+1);
		double[] data2;
		switch (mode) {
		case SKILL_WORKING_TIME:
			data2=Arrays.copyOf(data,Math.min(data.length,CallcenterModelSkillLevel.callerTypeWorkingTimeMaxX));
			if (interval==-1) {
				skill.callerTypeWorkingTime.set(nr,new DataDistributionImpl(data2.length-1,data2));
				skill.callerTypeIntervalWorkingTime.set(nr,new AbstractRealDistribution[48]);
			} else {
				AbstractRealDistribution[] dists=skill.callerTypeIntervalWorkingTime.get(nr);
				dists[interval]=new DataDistributionImpl(data2.length-1,data2);
				skill.callerTypeIntervalWorkingTime.set(nr,dists);
			}
			break;
		case SKILL_POSTPROCESSING_TIME:
			data2=Arrays.copyOf(data,Math.min(data.length,CallcenterModelSkillLevel.callerTypePostProcessingTimeMaxX));
			if (interval==-1) {
				skill.callerTypePostProcessingTime.set(nr,new DataDistributionImpl(data2.length-1,data2));
				skill.callerTypeIntervalPostProcessingTime.set(nr,new AbstractRealDistribution[48]);
			} else {
				AbstractRealDistribution[] dists=skill.callerTypeIntervalPostProcessingTime.get(nr);
				dists[interval]=new DataDistributionImpl(data2.length-1,data2);
				skill.callerTypeIntervalPostProcessingTime.set(nr,dists);
			}
			break;
		case SKILL_WORKING_TIME_ADDON:
			/* Wurde vorher bereits weggefiltert. */
			break;
		}
		return null;
	}

	@Override
	public String processStrings(CallcenterModel model, String parameter, String[] data) {
		Object[] obj=getSkillLevelFromParameter(model,parameter);
		if (obj==null) return String.format(Language.tr("Importer.Error.SkillDoesNotExist"),parameter);
		CallcenterModelSkillLevel skill=(CallcenterModelSkillLevel)(obj[0]);
		int nr=(Integer)(obj[1]);

		if (data.length!=1) return String.format(Language.tr("Importer.Error.SingleCellExpected"),data.length);

		switch (mode) {
		case SKILL_WORKING_TIME_ADDON:
			if (interval==-1) {
				skill.callerTypeWorkingTimeAddOn.set(nr,data[0]);
				skill.callerTypeIntervalWorkingTimeAddOn.set(nr,new String[48]);
			} else {
				String[] values=skill.callerTypeIntervalWorkingTimeAddOn.get(nr);
				values[interval]=data[0];
				skill.callerTypeIntervalWorkingTimeAddOn.set(nr,values);
			}
			break;
		case SKILL_POSTPROCESSING_TIME:
		case SKILL_WORKING_TIME:
			/* Wurden vorher bereits weggefiltert. */
			break;
		}
		return null;
	}
}
