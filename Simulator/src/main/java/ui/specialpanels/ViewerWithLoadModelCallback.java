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
package ui.specialpanels;

import java.io.Serializable;

import ui.model.CallcenterModel;

/**
 * @author Alexander Herzog
 * @version 1.0
 */
public class ViewerWithLoadModelCallback extends JWorkPanel {
	/**
	 * Serialisierungs-ID der Klasse
	 * @see Serializable
	 */
	private static final long serialVersionUID = 4964712439912861614L;

	/**
	 * Konstruktor der Klasse
	 * @param doneNotify	Callback wird aufgerufen, wenn das Batch-Panel geschlossen werden soll
	 * @param helpCallback	Hilfe-Callback
	 */
	public ViewerWithLoadModelCallback(Runnable doneNotify, Runnable helpCallback) {
		super(doneNotify,helpCallback);
	}

	/**
	 * Wird hier ein Modell hinterlegt, so kann dies über die Funktion
	 * <code>getLoadModelToEditor()</code> abgefragt werden und so nach dem
	 * Schließen des Panels das Modell in den Editor geladen werden.
	 */
	protected CallcenterModel editModel=null;

	/**
	 * Gibt an, ob der Ergebnis-Viewer durch den Nutzerwunsch, ein bestimmtes Modell in den Editor zu laden, geschlossen wurde.
	 * @return	Modell, das geladen werden soll oder <code>null</code>, wenn kein Modell geladen werden soll.
	 */
	public final CallcenterModel getLoadModelToEditor() {
		return editModel;
	}

	/**
	 * Meldet zurück, dass das Fenster geschlossen wird, um die
	 * GUI des unterliegenden Fensters wieder zu aktivieren
	 * @param closeNotify	Objekt vom Type <code>Runnabble</code>, dessen <code>run</code>-Methode aufgerufen wird
	 */
	public final void setCloseNotify(Runnable closeNotify) {
		doneNotify=closeNotify;
	}

	@Override
	protected final void run() {}
}
