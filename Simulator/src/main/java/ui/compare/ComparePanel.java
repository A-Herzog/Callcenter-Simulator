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

import java.awt.Window;
import java.io.File;
import java.io.Serializable;
import java.util.Arrays;

import javax.swing.JButton;
import javax.swing.JMenuItem;
import javax.swing.JPopupMenu;

import language.Language;
import simulator.Statistics;
import ui.HelpLink;
import ui.editor.CallcenterModelEditorPanelDialog;
import ui.images.Images;
import ui.model.CallcenterModel;
import ui.specialpanels.JWorkPanel;
import ui.statistic.StatisticPanel;

/**
 * Ermöglicht den Vergleich von zwei Statistik-Dateien
 * @author Alexander Herzog
 * @version 1.0
 */
public class ComparePanel extends JWorkPanel {
	/**
	 * Serialisierungs-ID der Klasse
	 * @see Serializable
	 */
	private static final long serialVersionUID = 1696555531378569922L;

	/** Übergeordnetes Fenster */
	private final Window owner;
	/** Gibt an, ob angeboten werden soll, die zugehörigen Modelle in den Editor zu laden */
	private final boolean allowLoadToEditor;
	/** Anzuzeigende Statistikdateien */
	private final Statistics[] statistic;
	/** Schaltfläche "Modell und Statistikdaten anzeigen" */
	private final JButton showModelButton;
	/** Hilfe-Verknüpfung */
	private final HelpLink helpLink;
	/** Wird hier ein Modell eingetragen, so steht dieses über {@link #getModelForEditor()} nach dem Schließen des Panels zum Laden in den Editor bereit */
	private CallcenterModel loadModelIntoEditor=null;

	/**
	 * Konstruktor der Klasse <code>ComparePanel</code>
	 * @param owner	Übergeordnetes Fenster
	 * @param statistic	Anzuzeigende Statistikdateien
	 * @param title	Zusätzliche Überschriften über den Statistikdateien
	 * @param nebeneinander	Sollen die Statistikdaten nebeneinander (<code>true</code>) oder untereinander (<code>false</code>) angezeigt werden
	 * @param doneNotify	Wird aufgerufen, wenn sich das Panel schließen möchte
	 * @param allowLoadToEditor	Gibt an, ob angeboten werden soll, die zugehörigen Modelle in den Editor zu laden
	 * @param helpLink	Hilfe-Link
	 * @param helpCallback	Hilfe-Callback für Panel (nicht-modal)
	 * @param helpModal	Hilfe-Callback für Dialoge (modal)
	 */
	public ComparePanel(Window owner, Statistics[] statistic, String[] title, boolean nebeneinander, Runnable doneNotify, boolean allowLoadToEditor, HelpLink helpLink, Runnable helpCallback, Runnable helpModal) {
		super(doneNotify,helpCallback);

		this.owner=owner;
		this.helpLink=helpLink;
		this.statistic=statistic;
		String[] titleArray=new String[statistic.length];
		for (int i=0;i<titleArray.length;i++) {
			if (title==null || title.length<=i || title[i]==null) titleArray[i]=(statistic[i]==null || statistic[i].editModel==null)?"":statistic[i].editModel.name; else titleArray[i]=title[i];
		}

		this.allowLoadToEditor=allowLoadToEditor;
		StatisticPanel statisticPanel=new StatisticPanel(nebeneinander,helpLink,helpModal,null,null,statistic.length);
		statisticPanel.setStatistic(statistic,titleArray);
		add(statisticPanel);

		addFooter(null,null,null);
		showModelButton=addFooterButton(Language.tr("MainToolbar.ShowModelAndStatistic"),Images.MODEL.getIcon());
	}

	/* (non-Javadoc)
	 * @see complexcallcenter.specialpanels.JWorkPanel#run()
	 */
	@Override
	protected void run() {}

	/**
	 * Lädt die angegebenen Statistikdateien in Statistikobjekte
	 * @param statisticFiles	Array der Statistikdateien
	 * @return	Array der Statistikobjekte; lässt sich eine Datei nicht laden, so wird die Verarbeitung abgebrochen und an der entsprechenden Stelle im Array ein <code>null</code> zurückgegeben.
	 */
	public static final Statistics[] getStatisticFiles(File[] statisticFiles) {
		Statistics[] statistic=new Statistics[statisticFiles.length];
		Arrays.fill(statistic,null);
		for (int i=0;i<statisticFiles.length;i++) {
			Statistics data=new Statistics(null,null,0,0);
			String s=data.loadFromFile(statisticFiles[i]); if (s!=null) break;
			statistic[i]=data;
		}
		return statistic;
	}

	@Override
	protected void userButtonClick(int index, JButton button) {
		if (button==showModelButton) {
			JPopupMenu popupMenu=new JPopupMenu();
			for (int i=0;i<statistic.length;i++) {
				final Statistics statisticData=statistic[i];
				if (statisticData==null || statisticData.editModel==null) continue;
				JMenuItem item=new JMenuItem(statisticData.editModel.name);
				item.addActionListener(e-> {
					final CallcenterModelEditorPanelDialog modelViewer=new CallcenterModelEditorPanelDialog(owner,statisticData.editModel,statisticData,allowLoadToEditor,helpLink);
					modelViewer.setCloseNotify(new ModelViewerClosed(modelViewer));
					JWorkPanel.setEnableGUI(ComparePanel.this,false);
					modelViewer.setVisible(true);

				});
				popupMenu.add(item);
			}
			popupMenu.show(ComparePanel.this,button.getX(),button.getY()+button.getHeight());
			return;
		}
	}

	/**
	 * Gibt an, ob beim Schließen der Vergleichsansicht ein Modell in den Editor geladen werden soll.
	 * @return	Zu ladendes Modell oder <code>null</code> wenn nichts geladen werden soll.
	 */
	public CallcenterModel getModelForEditor() {
		return loadModelIntoEditor;
	}

	/**
	 * Reagiert darauf, wenn der Viewer geschlossen werden soll.
	 * @see CallcenterModelEditorPanelDialog
	 */
	private class ModelViewerClosed implements Runnable {
		/** Modell-Viewer */
		private final CallcenterModelEditorPanelDialog modelViewer;

		/**
		 * Konstruktor der Klasse
		 * @param modelViewer	Modell-Viewer
		 */
		public ModelViewerClosed(final CallcenterModelEditorPanelDialog modelViewer) {
			this.modelViewer=modelViewer;
		}

		@Override
		public void run() {
			JWorkPanel.setEnableGUI(ComparePanel.this,true);
			if (modelViewer.getLoadModelToEditor()) {
				loadModelIntoEditor=modelViewer.getCallcenterModel();
				requestClose();
			}
		}
	}
}
