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
import java.util.Arrays;
import java.util.List;

import org.apache.commons.math3.distribution.AbstractRealDistribution;

import language.Language;
import mathtools.distribution.tools.DistributionTools;
import parser.CalcSystem;
import parser.MathCalcError;
import parser.MathParser;

/**
 * Modelliert einen Agenten-Skill-Typ
 * @author Alexander Herzog
 * @version 1.0
 * @see CallcenterModelSkillLevel
 * @see CallcenterRunModelAgent
 */
public final class CallcenterRunModelSkillLevel {
	/** Name des Skill-Levels */
	public final String name;

	/**
	 * Liefert den Index in den folgenden Arrays für Kunden des jeweils durch den
	 * Index in diesem Array spezifizierten Typs. Werte -1 bedeuten, dass in diesem
	 * Skill-Level keine Daten für den jeweiligen Kundentyp hinterlegt sind.
	 */
	public short[] callerTypeByIndex;

	/** Liste der Kundentyp spezifische Daten (wird von <code>checkAndInit</code> gesetzt) */
	public CallcenterRunModelCaller[] callerType;
	/** Liste der Kundentyp spezifischen Bedienzeitverlängerungen gemäß der Wartezeit */
	public MathParser[][] callerTypeWorkingTimeAddOn;
	/** Liste der Kundentyp spezifischen Bedienzeitverteilungen */
	public AbstractRealDistribution[][] callerTypeWorkingTime;
	/** Liste der Kundentyp spezifischen Nachbearbeitungszeitverteilung */
	public AbstractRealDistribution[][] callerTypePostProcessingTime;
	/** Liste der Kundentyp spezifischen Prioritäten */
	public int[] callerTypeScore;

	/** Liste der Kundentypnamen */
	private List<String> callerTypeNameList;
	/** Liste der Kundentyp-spezifischen Bedienzeitverlängerung in Abhängigkeit von der Wartezeit */
	private List<String[]> callerTypeWorkingTimeAddOnList;
	/** Liste der Kundentyp-spezifischen Bedienzeitverteilungen */
	private List<AbstractRealDistribution[]> callerTypeWorkingTimeList;
	/** Liste der Kundentyp-spezifischen Nachbearbeitungszeitverteilung */
	private List<AbstractRealDistribution[]> callerTypePostProcessingTimeList;
	/** Liste der Kundentyp spezifischen Prioritäten */
	private List<Integer> callerTypeScoreList;

	/**
	 * Konstruktor der Klasse <code>CallcenterModelRunSkillLevel</code>
	 * @param editModel	Referenz auf ein Edit-Skill-Level-Modell, aus dem dieses Simulations-Modell erstellt werden soll.
	 */
	public CallcenterRunModelSkillLevel(CallcenterModelSkillLevel editModel) {
		name=editModel.name;
		callerTypeNameList=editModel.callerTypeName;

		callerTypeWorkingTimeAddOnList=new ArrayList<>();
		for (int i=0;i<editModel.callerTypeWorkingTimeAddOn.size();i++) {
			String s=editModel.callerTypeWorkingTimeAddOn.get(i); if (s.isBlank()) s=null;
			String[] sArray=new String[48];
			String[] sOrig=editModel.callerTypeIntervalWorkingTimeAddOn.get(i);
			for (int j=0;j<sArray.length;j++) sArray[j]=(sOrig[j]==null)?s:sOrig[j];
			callerTypeWorkingTimeAddOnList.add(sArray);
		}

		callerTypeWorkingTimeList=new ArrayList<>();
		for (int i=0;i<editModel.callerTypeWorkingTime.size();i++) {
			AbstractRealDistribution d=DistributionTools.normalizeDistribution(editModel.callerTypeWorkingTime.get(i));
			AbstractRealDistribution[] dArray=new AbstractRealDistribution[48];
			AbstractRealDistribution[] dOrig=editModel.callerTypeIntervalWorkingTime.get(i);
			for (int j=0;j<dArray.length;j++) dArray[j]=(dOrig[j]==null)?d:dOrig[j];
			callerTypeWorkingTimeList.add(dArray);
		}

		callerTypePostProcessingTimeList=new ArrayList<>();
		for (int i=0;i<editModel.callerTypePostProcessingTime.size();i++) {
			AbstractRealDistribution d=DistributionTools.normalizeDistribution(editModel.callerTypePostProcessingTime.get(i));
			AbstractRealDistribution[] dArray=new AbstractRealDistribution[48];
			AbstractRealDistribution[] dOrig=editModel.callerTypeIntervalPostProcessingTime.get(i);
			for (int j=0;j<dArray.length;j++) dArray[j]=(dOrig[j]==null)?d:dOrig[j];
			callerTypePostProcessingTimeList.add(dArray);
		}

		callerTypeScoreList=editModel.callerTypeScore;
	}

	/**
	 * Bereitet das Objekt auf die Simulation vor.
	 * @param caller	Liste mit allen Anrufer-Klassen
	 * @param editCaller	Liste mit allen Editor-Modell Anrufergruppen
	 * @param strict	Strenge Modellprüfung
	 * @return Gibt <code>null</code> zurück, wenn die Initialisierung erfolgreich war, andernfalls wird eine Fehlermeldung als String zurückgegeben,
	 */
	public String checkAndInit(final CallcenterRunModelCaller[] caller, final List<CallcenterModelCaller> editCaller, final boolean strict) {
		if (name.isBlank()) return Language.tr("Model.Check.SkillLevel.NoName");

		int maxIndex=0;
		for (CallcenterRunModelCaller c : caller) maxIndex=Math.max(maxIndex,c.index);
		callerTypeByIndex=new short[maxIndex+1];
		Arrays.fill(callerTypeByIndex,(short)(-1));

		List<CallcenterRunModelCaller> callerTypeList=new ArrayList<>();
		int i=0;
		while (i<callerTypeNameList.size()) {
			CallcenterRunModelCaller c=null;
			for (int j=0;j<caller.length;j++) if (caller[j].name.equalsIgnoreCase(callerTypeNameList.get(i))) {c=caller[j]; break;}
			if (c==null) {
				if (strict) {
					boolean isDisabledGroup=false;
					for (CallcenterModelCaller editGroup : editCaller) if (!editGroup.active && editGroup.name.equalsIgnoreCase(callerTypeNameList.get(i))) {isDisabledGroup=true; break;}
					if (!isDisabledGroup) return String.format(Language.tr("Model.Check.SkillLevel.UnknowClientType"),name,callerTypeNameList.get(i));
				}
				callerTypeNameList.remove(i);
				callerTypeWorkingTimeAddOnList.remove(i);
				callerTypeWorkingTimeList.remove(i);
				callerTypePostProcessingTimeList.remove(i);
				callerTypeScoreList.remove(i);
			} else {
				callerTypeList.add(c);
				callerTypeByIndex[c.index]=(short)(callerTypeList.size()-1);
				i++;
			}
		}

		callerType=callerTypeList.toArray(CallcenterRunModelCaller[]::new);
		callerTypeWorkingTimeAddOn=new MathParser[callerTypeWorkingTimeAddOnList.size()][];
		for (int j=0;j<callerTypeWorkingTimeAddOnList.size();j++) {
			String[] orig=callerTypeWorkingTimeAddOnList.get(j);
			callerTypeWorkingTimeAddOn[j]=new MathParser[orig.length];
			MathParser[] data=callerTypeWorkingTimeAddOn[j];
			for (int k=0;k<orig.length;k++) {
				data[k]=null;
				if (orig[k]!=null && !orig[k].isBlank() && !orig[k].trim().equals("0")) {
					MathParser calc=new CalcSystem(orig[k],new String[]{"w"});
					int pos=calc.parse();
					if (pos!=-1) {
						return String.format(Language.tr("Model.Check.SkillLevel.InvalidHoldingTimeAddOnExpression"),orig[k],pos+1);
					}
					if (!calc.isConstValue()) {
						data[k]=calc;
					} else {
						try {
							if (calc.calc()!=0) data[k]=calc;
						} catch (MathCalcError e) {}
					}
				}
			}
		}
		callerTypeWorkingTime=callerTypeWorkingTimeList.toArray(AbstractRealDistribution[][]::new);
		callerTypePostProcessingTime=callerTypePostProcessingTimeList.toArray(AbstractRealDistribution[][]::new);
		callerTypeScore=new int[callerTypeScoreList.size()]; for (int j=0;j<callerTypeScoreList.size();j++) callerTypeScore[j]=callerTypeScoreList.get(j);

		int size=callerTypeWorkingTimeAddOn.length;
		if (size!=callerTypeWorkingTime.length) return Language.tr("Model.Check.SkillLevel.InternalError");
		if (size!=callerTypePostProcessingTime.length) return Language.tr("Model.Check.SkillLevel.InternalError");

		/* Speicher sparen */
		callerTypeNameList=null;
		callerTypeWorkingTimeAddOnList=null;
		callerTypeWorkingTimeList=null;
		callerTypePostProcessingTimeList=null;
		callerTypeScoreList=null;

		return null;
	}
}
