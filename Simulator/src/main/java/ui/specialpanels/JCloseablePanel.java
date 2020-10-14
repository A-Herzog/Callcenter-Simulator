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

import java.awt.LayoutManager;
import java.io.File;
import java.io.Serializable;

import javax.swing.JPanel;

/**
 * <code>JPanel</code>, welches zusätzlich eine Funktion mitbringt, durch den Aufruf von der der Besitzer
 * dem Panel mitteilen kann, dass es geschlossen werden soll.
 * (Das Panel muss darauf reagieren, in dem es dem Hauptfenster mitteilt, dass es geschlossen werden möchte.
 * Andernfalls passiert nichts weiter.)
 * @author Alexander Herzog
 * @version 1.0
 */
public class JCloseablePanel extends JPanel {
	/**
	 * Serialisierungs-ID der Klasse
	 * @see Serializable
	 */
	private static final long serialVersionUID = 950090362066642976L;

	/**
	 * Konstruktor der Klasse
	 */
	protected JCloseablePanel() {
		super();
	}

	/**
	 * Konstruktor der Klasse
	 * @param layout	Zu verwendendes Layout
	 */
	protected JCloseablePanel(LayoutManager layout) {
		super(layout);
	}

	/**
	 * Diese Methode wird vom Panel aufgerufen,
	 * wenn es geschlossen werden möchte.
	 */
	public void requestClose() {
	}

	/**
	 * Über diese Methode nimmt das Panel
	 * (in abgeleiteten Klassen) Dateien
	 * per Drag&amp;Drop entgegen.
	 * @param file	Empfangene Datei
	 * @return	Gibt an, ob die Datei verarbeitet werden konnte
	 */
	public boolean dragDropLoad(File file) {
		return false;
	}
}
