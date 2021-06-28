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
import java.util.ArrayList;
import java.util.List;

import org.apache.commons.math3.distribution.AbstractRealDistribution;
import org.apache.commons.math3.distribution.ExponentialDistribution;

import language.Language;
import mathtools.NumberTools;
import mathtools.Table;
import mathtools.distribution.DataDistributionImpl;
import mathtools.distribution.LogNormalDistributionImpl;
import mathtools.distribution.OnePointDistributionImpl;
import ui.model.CallcenterModel;
import ui.model.CallcenterModelAgent;
import ui.model.CallcenterModelCallcenter;
import ui.model.CallcenterModelCaller;
import ui.model.CallcenterModelSkillLevel;

/**
 * Lädt ein vollständiges Callcenter-Modell aus zwei Tabellen (Anrufer und Agenten).
 * @author Alexander Herzog
 * @version 1.0
 */
public final class SimpleModelLoader extends SimpleModelBaseLoader {
	/** Tabelle die die Anruferdaten enthält */
	private Table callerTable;
	/** Tabelle die die Agentendaten enthält */
	private Table agentsTable;

	/**
	 * Legt die Tabelle mit den Anruferzahlen fest.
	 * @param file	Dateiname der Tabelle mit den Anruferzahlen.
	 * @param table	Tabelle in der Arbeitsmappe (kann <code>null</code> oder leer sein, dann wird die erste Tabelle verwendet)
	 * @param startCol	Startspalte (kann <code>null</code> oder leer sein, dann wird "A" angenommen, d.h. es werden dann keine Spalten entfernt)
	 * @return	Liefert im Erfolgsfall <code>null</code>, sonst eine Fehlermeldung
	 */
	public String setCallerTable(final File file, final String table, final String startCol) {
		Object O=processTable(file,table,startCol);
		if (O instanceof String) return (String)O;
		callerTable=(Table)O;
		return null;
	}

	/**
	 * Legt die Tabelle mit den Anruferzahlen fest.
	 * @param file	Dateiname der Tabelle mit den Anruferzahlen.
	 * @param table	Tabelle in der Arbeitsmappe (kann <code>null</code> oder leer sein, dann wird die erste Tabelle verwendet)
	 * @return	Liefert im Erfolgsfall <code>null</code>, sonst eine Fehlermeldung
	 */
	public String setCallerTable(final File file, final String table) {
		return setCallerTable(file,table,"A");
	}

	/**
	 * Legt die Tabelle mit den Agentenzahlen fest.
	 * @param file	Dateiname der Tabelle mit den Agentenzahlen.
	 * @param table	Tabelle in der Arbeitsmappe (kann <code>null</code> oder leer sein, dann wird die erste Tabelle verwendet)
	 * @param startCol	Startspalte (kann <code>null</code> oder leer sein, dann wird "A" angenommen, d.h. es werden dann keine Spalten entfernt)
	 * @return	Liefert im Erfolgsfall <code>null</code>, sonst eine Fehlermeldung
	 */
	public String setAgentsTable(final File file, final String table, final String startCol) {
		Object O=processTable(file,table,startCol);
		if (O instanceof String) return (String)O;
		agentsTable=(Table)O;
		return null;
	}

	/**
	 * Legt die Tabelle mit den Agentenzahlen fest.
	 * @param file	Dateiname der Tabelle mit den Agentenzahlen.
	 * @param table	abelle in der Arbeitsmappe (kann <code>null</code> oder leer sein, dann wird die erste Tabelle verwendet)
	 * @return	Liefert im Erfolgsfall <code>null</code>, sonst eine Fehlermeldung
	 */
	public String setAgentsTable(final File file, final String table) {
		return setAgentsTable(file,table,"A");
	}

	@Override
	protected Object buildModelIntern() {
		/* Basisprüfung der Eingabedaten */
		if (callerTable==null || callerTable.getSize(1)==0) return Language.tr("Loader.SimpleModel.NoCallerData");
		if (callerTable.getSize(0)<1+48+6) return Language.tr("Loader.SimpleModel.InvalidCallerData");
		if (agentsTable==null || agentsTable.getSize(1)==0) return Language.tr("Loader.SimpleModel.NoAgentsData");
		if (callerTable.getSize(0)<1+48) return Language.tr("Loader.SimpleModel.InvalidAgentsData");

		Double D;

		CallcenterModel model=new CallcenterModel(Language.tr("Loader.SimpleModel.ModelName"));

		/* Kunden */
		List<List<Double>> forward=new ArrayList<>();
		for (List<String> line : callerTable.transpose().getData()) {
			if (line.size()<1+48+6) return Language.tr("Loader.SimpleModel.InvalidCallerData");
			CallcenterModelCaller caller=new CallcenterModelCaller(line.get(0));
			caller.continueProbability=0;

			/* Ankünfte */
			DataDistributionImpl dist=DataDistributionImpl.createFromString(getDistLoaderString(line,1,48),CallcenterModelCaller.freshCallsDistMaxX);
			if (dist==null) return Language.tr("Loader.SimpleModel.InvalidCallerData");
			caller.freshCallsDist48=dist;
			caller.freshCallsCountMean=(int)Math.round(dist.sum());

			/* Priorität */
			D=NumberTools.getDouble(line.get(49));
			if (D==null) return Language.tr("Loader.SimpleModel.InvalidCallerData");
			caller.scoreBase=D;
			D=NumberTools.getDouble(line.get(50));
			if (D==null) return Language.tr("Loader.SimpleModel.InvalidCallerData");
			caller.scoreSecond=D;
			D=NumberTools.getDouble(line.get(51));
			if (D==null) return Language.tr("Loader.SimpleModel.InvalidCallerData");
			caller.scoreContinued=D;

			/* Wartezeittolerenz */
			D=NumberTools.getPositiveDouble(line.get(52));
			if (D==null) return Language.tr("Loader.SimpleModel.InvalidCallerData");
			caller.waitingTimeMode=CallcenterModelCaller.WAITING_TIME_MODE_SHORT;
			caller.waitingTimeDist=new LogNormalDistributionImpl(D,D*0.5);

			/* Wiederholwahrscheinlichkeit */
			D=NumberTools.getProbability(line.get(53));
			if (D==null) return Language.tr("Loader.SimpleModel.InvalidCallerData");
			caller.retryProbabiltyAfterBlockedFirstRetry=D;
			caller.retryProbabiltyAfterBlocked=D;
			caller.retryProbabiltyAfterGiveUpFirstRetry=D;
			caller.retryProbabiltyAfterGiveUp=D;

			/* Wiederholabstände */
			D=NumberTools.getPositiveDouble(line.get(54));
			if (D==null) return Language.tr("Loader.SimpleModel.InvalidCallerData");
			caller.retryTimeDist=new ExponentialDistribution(D);

			/* Weiterleitungen */
			List<Double> forwardCaller=new ArrayList<>(); forward.add(forwardCaller);
			for (int i=55;i<line.size();i++) {
				String s=line.get(i).trim();
				if (!s.isEmpty()) {
					D=NumberTools.getExtProbability(s);
					if (D==null) return Language.tr("Loader.SimpleModel.InvalidCallerData");
				} else {
					D=0.0;
				}
				forwardCaller.add(D);
			}

			model.caller.add(caller);
		}
		for (int i=0; i<model.caller.size();i++) {
			CallcenterModelCaller caller=model.caller.get(i);
			List<Double> forwardCaller=forward.get(i);
			while (forwardCaller.size()<model.caller.size()) forwardCaller.add(0.0);

			double sum=0;
			for (Double f: forwardCaller) sum+=f;
			if (sum>1) return Language.tr("Loader.SimpleModel.InvalidCallerData");
			if (sum>0) {
				caller.continueProbability=sum;
				caller.continueTypeName=new ArrayList<>();
				caller.continueTypeRate=new ArrayList<>();
				for (int j=0;j<model.caller.size();j++) {
					caller.continueTypeName.add(model.caller.get(j).name);
					caller.continueTypeRate.add(forwardCaller.get(j));
				}
			}
		}

		/* Callcenter und Agenten */
		CallcenterModelCallcenter callcenter=new CallcenterModelCallcenter();
		model.callcenter.add(callcenter);
		for (List<String> line : agentsTable.transpose().getData()) {
			if (line.size()<1+48) return Language.tr("Loader.SimpleModel.InvalidAgentsData");
			CallcenterModelAgent agents=new CallcenterModelAgent(line.get(0));
			CallcenterModelSkillLevel skill=new CallcenterModelSkillLevel(line.get(0));

			/* Anzahl pro Intervall */
			DataDistributionImpl dist=DataDistributionImpl.createFromString(getDistLoaderString(line,1,48),CallcenterModelAgent.countPerIntervalMaxX);
			if (dist==null) return Language.tr("Loader.SimpleModel.InvalidAgentsData");
			agents.countPerInterval48=dist;
			agents.count=-1;

			/* Skills der Gruppe */
			for (int i=0;i<model.caller.size();i++) {
				if (line.size()<48+1+i+1) break;
				String s=line.get(48+1+i).trim();
				if (s.isEmpty()) continue;
				D=NumberTools.getNotNegativeDouble(s);
				if (D==null) return Language.tr("Loader.SimpleModel.InvalidAgentsData");
				if (D==0) continue;
				skill.callerTypeScore.add(1);
				skill.callerTypeName.add(model.caller.get(i).name);
				skill.callerTypeWorkingTimeAddOn.add("");
				skill.callerTypeWorkingTime.add(new LogNormalDistributionImpl(D,D/10));
				skill.callerTypePostProcessingTime.add(new OnePointDistributionImpl(0));
				skill.callerTypeIntervalWorkingTimeAddOn.add(new String[48]);
				skill.callerTypeIntervalWorkingTime.add(new AbstractRealDistribution[48]);
				skill.callerTypeIntervalPostProcessingTime.add(new AbstractRealDistribution[48]);
			}

			callcenter.agents.add(agents);
			model.skills.add(skill);
		}

		return model;
	}
}
