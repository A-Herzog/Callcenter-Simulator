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
package ui.compare;

import java.awt.BorderLayout;
import java.awt.Window;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.io.Serializable;

import javax.swing.JFrame;

import language.Language;
import simulator.Statistics;
import tools.SetupData;
import ui.HelpLink;
import ui.model.CallcenterModel;

/**
 * Dialog, der den Vergleich mehrerer Statistik-Daten in einem eigenen Fenster ermöglicht.
 * @author Alexander Herzog
 * @version 1.0
 */
public class ComparePanelDialog extends JFrame {
	/**
	 * Serialisierungs-ID der Klasse
	 * @see Serializable
	 */
	private static final long serialVersionUID = 5337587149627166880L;

	/** Panel in dem die Daten angezeigt werden */
	private final ComparePanel compare;
	/** Meldet zurück, dass das Fenster geschlossen wird, um die GUI des unterliegenden Fensters wieder zu aktivieren */
	private Runnable closeNotify;

	/**
	 * Konstruktor der Klasse <code>CallcenterModelEditorPanelDialog</code>
	 * @param owner	Übergeordnetes Fenster
	 * @param statistic	Callcenter-Statistikdaten, welche angezeigt werden sollen.
	 * @param title Optionale Titel für die Statistik-Daten (z.B. "vorher", "nachher")
	 * @param nebeneinander	Gibt an, ob die Daten nebeneinander (<code>true</code>) oder untereinander (<code>false</code>) angezeigt werden sollen.
	 * @param allowLoadToEditor	Gibt an, ob die Schaltfläche zum Laden des Modells in den Editor angezeigt werden soll.
	 * @param helpLink Verknüpfung mit der Online-Hilfe
	 * @param helpCallback	Hilfe-Callback (nicht modal)
	 * @param helpModal	Hilfe-Callback (model)
	 */
	public ComparePanelDialog(Window owner, Statistics[] statistic, String[] title, boolean nebeneinander, boolean allowLoadToEditor, HelpLink helpLink, Runnable helpCallback, Runnable helpModal) {
		super(Language.tr("Compare.WindowTitle"));

		compare=new ComparePanel(this,statistic,title,nebeneinander,new CloseNotify(),allowLoadToEditor,helpLink,helpCallback,helpModal);
		getContentPane().setLayout(new BorderLayout());
		getContentPane().add(compare,BorderLayout.CENTER);

		addWindowListener(new WindowAdapter() {@Override
			public void windowClosing(WindowEvent event) {
			if (closeNotify!=null) closeNotify.run();
			setVisible(false);
			dispose();
		}});
		SetupData setup=SetupData.getSetup();
		setSize((int)Math.round(850*setup.scaleGUI),(int)Math.round(750*setup.scaleGUI));
		setMinimumSize(getSize());
		setLocationRelativeTo(owner);
	}

	/**
	 * Meldet zurück, dass das Fenster geschlossen wird, um die
	 * GUI des unterliegenden Fensters wieder zu aktivieren
	 * @param closeNotify	Objekt vom Typ {@link Runnable}, dessen <code>run</code>-Methode aufgerufen wird
	 */
	public void setCloseNotify(Runnable closeNotify) {
		this.closeNotify=closeNotify;
	}

	/**
	 * Soll nach dem Schließen des Vergleichs-Panels ein
	 * Callcenter-Modell in den Editor geladen werden?
	 * @return	Callcenter-Modell in den Editor laden?
	 * @see #getCallcenterModel()
	 */
	public boolean getLoadModelToEditor() {
		return compare.getModelForEditor()!=null;
	}

	/**
	 * Liefert das nach dem Schließen des Vergleichs-Panels
	 * in den Edtior zu ladende Callcenter-Modell.
	 * @return	In den Editor zu ladende Callcenter-Modell
	 * @see #getLoadModelToEditor()
	 */
	public CallcenterModel getCallcenterModel() {
		return compare.getModelForEditor();
	}

	/**
	 * Wird an das Compare-Panel übergeben, damit dieses den Dialog benachrichtigen kann,
	 * wenn dieser geschlossen werden soll.
	 * @see ComparePanelDialog#compare
	 */
	private class CloseNotify implements Runnable {
		/**
		 * Konstruktor der Klasse
		 */
		public CloseNotify() {
			/*
			 * Wird nur benötigt, um einen JavaDoc-Kommentar für diesen (impliziten) Konstruktor
			 * setzen zu können, damit der JavaDoc-Compiler keine Warnung mehr ausgibt.
			 */
		}

		@Override
		public void run() {
			if (closeNotify!=null) closeNotify.run();
			setVisible(false);
			dispose();
		}
	}

	@Override
	public void setVisible(boolean visible) {
		super.setVisible(visible);
		if (visible) compare.setVisibleInit();
	}
}
