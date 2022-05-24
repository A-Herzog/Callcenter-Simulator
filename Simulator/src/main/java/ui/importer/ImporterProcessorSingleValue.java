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
package ui.importer;

import language.Language;
import ui.model.CallcenterModel;

/**
 * Dies ist die abstrakte Basisklasse der Importprozessoren, die nur einen Zahlenwert als Parameter erwarten.
 * @author Alexander Herzog
 * @version 1.0
 */
public abstract class ImporterProcessorSingleValue extends ImporterProcessor {
	/**
	 * Konstruktor der Klasse
	 */
	public ImporterProcessorSingleValue() {
		/*
		 * Wird nur benötigt, um einen JavaDoc-Kommentar für diesen (impliziten) Konstruktor
		 * setzen zu können, damit der JavaDoc-Compiler keine Warnung mehr ausgibt.
		 */
	}

	/**
	 * Führt den Importvorgang aus
	 * @param model	Callcenter-Modell, in das die Daten importiert werden sollen
	 * @param parameter	 Zusätzlicher Parameter, der angibt, was importiert werden soll
	 * @param data	 Zahl die importiert werden soll
	 * @return	Liefert im Erfolgsfall <code>null</code> zurück, sonst eine entsprechende Fehlermeldung.
	 */
	protected abstract String processInt(CallcenterModel model, String parameter, double data);

	/* (non-Javadoc)
	 * @see complexcallcenter.importer.ImporterProcessor#process(complexcallcenter.model.CallcenterModel, java.lang.String, double[])
	 */
	@Override
	public final String processNumbers(CallcenterModel model, String parameter, double[] data) {
		if (data.length!=1) return String.format(Language.tr("Importer.Error.SingleCellExpected"),data.length);
		return processInt(model,parameter,data[0]);
	}
}
