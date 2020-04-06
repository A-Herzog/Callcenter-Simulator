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

import java.awt.Window;
import java.io.File;

import language.Language;
import systemtools.MsgBox;
import ui.model.CallcenterModel;

/**
 * Lädt die alle Callcenter-Daten aus einer Technion-Tabelle und speichert diese in einer neuen Ausgabedatei.
 * @author Alexander Herzog
 * @see AbstractTechnionProcessing
 */
public final class TechnionFullProcessing extends AbstractTechnionProcessing {
	/**
	 * Konstruktor der Klasse
	 * @param owner	Übergeordnetes Fenster
	 */
	public TechnionFullProcessing(final Window owner) {
		super(owner);
		showOutputSelect=true;
		infoText="<p>\n"+Language.tr("Loader.Info.Technion")+"\n</p>\n<p>\n"+Language.tr("Loader.Info.Technion.All.Description")+"</p>";
	}

	@Override
	public String getName() {
		return Language.tr("Loader.Info.Technion.All.Title");
	}

	@Override
	protected CallcenterModel processTechnion(TechnionLoader loader, File outFile) {
		if (!loader.saveResults(outFile)) MsgBox.error(owner,Language.tr("Loader.Error.SavingData.Title"),loader.getLastError());
		return null;
	}
}
