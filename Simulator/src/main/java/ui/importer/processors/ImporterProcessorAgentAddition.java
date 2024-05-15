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
import mathtools.distribution.DataDistributionImpl;
import ui.importer.ImporterProcessor;
import ui.model.CallcenterModel;
import ui.model.CallcenterModelAgent;

/**
 * Importiert den Krankheitsaufschlag pro Halbstundenintervall für eine Agentengruppe
 * @author Alexander Herzog
 * @version 1.0
 */
public class ImporterProcessorAgentAddition extends ImporterProcessor {
	/**
	 * Konstruktor der Klasse
	 */
	public ImporterProcessorAgentAddition() {
		/*
		 * Wird nur benötigt, um einen JavaDoc-Kommentar für diesen (impliziten) Konstruktor
		 * setzen zu können, damit der JavaDoc-Compiler keine Warnung mehr ausgibt.
		 */
	}

	@Override
	public String[] getNames() {
		List<String> list=new ArrayList<>();
		list.add(Language.tr("Importer.DiseaseSurcharge.Title"));
		for (String s: Language.trOther("Importer.DiseaseSurcharge.Title")) if (!list.contains(s)) list.add(s);
		return list.toArray(String[]::new);
	}

	@Override
	public ParameterType getParameterType() {
		return ParameterType.PARAMETER_TYPE_AGENT_GROUP;
	}

	@Override
	public String processNumbers(CallcenterModel model, String parameter, double[] data) {
		if (data.length!=48) return String.format(Language.tr("Importer.Error.Need48HalfHourIntervals"),data.length);
		for (int i=0;i<data.length;i++) if (data[i]<0) return String.format(Language.tr("Importer.Error.NeedNonNegativeNumbers"),i+1);

		CallcenterModelAgent agents=getAgentFromParameter(model,parameter);
		if (agents==null) return String.format(Language.tr("Importer.Errror.AgentsGroupForIntervalValueDoesNotExist"),parameter);

		DataDistributionImpl addition=model.additionPerInterval.clone();
		addition.densityData=data;
		agents.additionPerInterval=addition;
		return null;
	}
}
