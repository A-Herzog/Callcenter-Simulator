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
package ui;

import javax.swing.SwingUtilities;

/**
 * Verkn�pft die konkreten Hilfe-Callbacks mit den Dialogen.
 * @author Alexander Herzog
 * @version 1.0
 */
public class HelpLink {
	/* *** *** *** Haupt-Panel *** *** *** */

	/** Hilfeseite f�r Editor|Allgemeine Daten (nicht-modal) */
	public final Runnable pageGeneral;

	/** Hilfeseite f�r Editor|Allgemeine Daten (nicht-modal) */
	public final Runnable pageGeneralModal;

	/** Hilfeseite f�r Editor|Anrufer (nicht-modal) */
	public final Runnable pageCaller;

	/** Hilfeseite f�r Editor|Anrufer (modal) */
	public final Runnable pageCallerModal;

	/** Hilfeseite f�r Editor|Callcenter und Agenten (nicht-modal) */
	public final Runnable pageCallcenter;

	/** Hilfeseite f�r Editor|Callcenter und Agenten (modal) */
	public final Runnable pageCallcenterModal;

	/** Hilfeseite f�r Editor|Skill-Level (nicht-modal) */
	public final Runnable pageSkillLevel;

	/** Hilfeseite f�r Editor|Skill-Level (modal) */
	public final Runnable pageSkillLevelModal;

	/** Hilfeseite f�r Editor|Modellinformationen (nicht-modal) */
	public final Runnable pageModelInformation;

	/** Hilfeseite f�r Editor|Modellinformationen (nicht-modal) */
	public final Runnable pageModelInformationModal;

	/** Hilfeseite f�r Statistikergebnisse (nicht-modal) */
	public final Runnable pageStatistic;

	/** Hilfeseite f�r Statistikergebnisse (modal) */
	public final Runnable pageStatisticModal;

	/** Hilfeseite f�r Statistikergebnisse|Schnellzugriff (nicht-modal) */
	public final Runnable pageStatisticFastAccess;

	/** Hilfeseite f�r Statistikergebnisse|Schnellzugriff (modal) */
	public final Runnable pageStatisticFastAccessModal;

	/* *** *** *** Special-Panel *** *** *** */

	/** Hilfeseite f�r das Batchverarbeitungs-Panel (nicht-modal) */
	public final Runnable pageBatch;

	/** Hilfeseite f�r das Batchverarbeitungs-Panel (modal) */
	public final Runnable pageBatchModal;

	/** Hilfeseite f�r Rearrange-Panel (nicht-modal) */
	public final Runnable pageRearrange;

	/** Hilfeseite f�r Rearrange-Panel (modal) */
	public final Runnable pageRearrangeModal;

	/** Hilfeseite f�r die verbundene Simulation (nicht-modal) */
	public final Runnable pageConnected;

	/** Hilfeseite f�r die verbundene Simulation (modal) */
	public final Runnable pageConnectedModal;

	/** Hilfeseite f�r den Connected Viewer (nicht-modal) */
	public final Runnable pageConnectedViewer;

	/** Hilfeseite f�r den Connected Viewer (modal) */
	public final Runnable pageConnectedViewerModal;

	/** Hilfeseite f�r den Modellvergleich (nicht-modal) */
	public final Runnable pageCompare;

	/** Hilfeseite f�r den Modellvergleich (modal) */
	public final Runnable pageCompareModal;

	/** Hilfeseite f�r den Optimierer (nicht-modal) */
	public final Runnable pageOptimize;

	/** Hilfeseite f�r den Optimierer (modal) */
	public final Runnable pageOptimizeModal;

	/** Hilfeseite f�r den Optimierer-Ergebnisansicht (nicht-modal) */
	public final Runnable pageOptimizeViewer;

	/** Hilfeseite f�r den Optimierer-Ergebnisansicht (modal) */
	public final Runnable pageOptimizeViewerModal;

	/** Hilfeseite f�r den heuristischen Ertragsverbesserer (nicht-modal) */
	public final Runnable pageRevenueOptimizer;

	/** Hilfeseite f�r die Kalibrierung (nicht-modal) */
	public final Runnable pageCalibrate;

	/** Hilfeseite f�r den Simulationsserver (nicht-modal) */
	public final Runnable pageServer;

	/** Hilfeseite f�r die Varianzanalyse (nicht-modal) */
	public final Runnable pageVarianceAnalysis;

	/** Hilfeseite f�r den Vorlagen-Import (nicht-modal) */
	public final Runnable pageTableImport;

	/* *** *** *** Dialoge *** *** *** */

	/** Hilfeseite f�r den Anrufer-Dialog (im Modell-Editor) (modal) */
	public final Runnable dialogCaller;

	/** Hilfeseite f�r den Callcenter-Dialog (im Modell-Editor) (modal) */
	public final Runnable dialogCallcenter;

	/** Hilfeseite f�r den Agenten-Dialog (im Modell-Editor) (modal) */
	public final Runnable dialogAgents;

	/** Hilfeseite f�r den Skill-Level-Dialog (im Modell-Editor) (modal) */
	public final Runnable dialogSkillLevel;

	/** Hilfeseite f�r den "Globale Parameter"-Dialog (auf der "Allgemeine Daten"-Seite des Modell-Editors) (modal) */
	public final Runnable dialogGlobalParameters;

	/** Hilfeseite f�r den "Schwellenwerte"-Dialog (auf der "Allgemeine Daten"-Seite des Modell-Editors) (modal) */
	public final Runnable dialogThreshold;

	/** Hilfeseite f�r Datei|Neu (modal) */
	public final Runnable dialogNew;

	/** Hilfeseite f�r Datei|Neu mit Assistent (modal) */
	public final Runnable dialogWizard;

	/** Hilfeseite f�r Datei|Einstellungen (modal) */
	public final Runnable dialogSetup;

	/** Hilfeseite f�r den Formelparser-Dialog (modal) */
	public final Runnable dialogParser;

	/** Hilfeseite f�r den Agenten-Vorplanungs-Dialog (modal) */
	public final Runnable dialogPreplanning;

	/** Hilfeseite f�r den Rechner-Dialog (modal) */
	public final Runnable dialogCalculator;

	/** Hilfeseite f�r den Auslastungsrechner-Dialog (modal) */
	public final Runnable dialogLoadCalculator;

	/** Hilfeseite f�r den Verteilungsanpassungs-Dialog (modal) */
	public final Runnable dialogFitDistribution;

	/** Hilfeseite f�r den Kommandozeilen-Dialog (modal) */
	public final Runnable dialogCommandLine;

	/** Hilfeseite f�r den Auto-Speicher-Dialog (modal) */
	public final Runnable dialogAutoSave;

	/** Hilfeseite f�r den Speical-Loader-Dialog ("Daten in speziellem Format laden") (modal) */
	public final Runnable dialogSpecialLoader;

	/** Hilfeseite f�r den Kundendaten-Generator-Dialog (modal) */
	public final Runnable dialogGeneratorClients;

	/** Hilfeseite f�r den Agentendaten-Generator-Dialog (modal) */
	public final Runnable dialogGeneratorAgents;

	/** Hilfeseite f�r den Agenten-Produktivit�t-Generator-Dialog (modal) */
	public final Runnable dialogGeneratorAgentsEfficiency;

	/** Hilfeseite f�r den Agenten-Krankheitsbedingten-Zuschlag-Dialog (modal) */
	public final Runnable dialogGeneratorAgentsAddition;

	/** Hilfeseite f�r den Skill-Level-Generator-Dialog (modal) */
	public final Runnable dialogGeneratorSkillLevels;

	/** Hilfeseite f�r den Einfache-Simulation-Dialog (modal) */
	public final Runnable dialogSimpleSimulation;



	private String topic="";
	private final Runnable openHelpNonModalCallback;
	private final Runnable openHelpModalCallback;

	/**
	 * Erstellt ein Objekt, welches konkrete Hilfeseiten (als Topic-Texte) mit Runnable verkn�pft.
	 * @param openHelpNonModalCallback	Runnable, das aufgerufen werden soll, wenn die Hilfe nicht-modal aufgerufen werden soll.
	 * @param openHelpModalCallback	Runnable, das aufgerufen werden soll, wenn die Hilfe modal aufgerufen werden soll.
	 */
	public HelpLink(Runnable openHelpNonModalCallback, Runnable openHelpModalCallback) {
		this.openHelpNonModalCallback=openHelpNonModalCallback;
		this.openHelpModalCallback=openHelpModalCallback;

		pageGeneral=new HelpCallback("EditorAllgemeineDaten",false);
		pageGeneralModal=new HelpCallback("EditorAllgemeineDaten",true);
		pageCaller=new HelpCallback("EditorAnrufergruppen",false);
		pageCallerModal=new HelpCallback("EditorAnrufergruppen",true);
		pageCallcenter=new HelpCallback("EditorCallcenter",false);
		pageCallcenterModal=new HelpCallback("EditorCallcenter",true);
		pageSkillLevel=new HelpCallback("EditorSkillLevel",false);
		pageSkillLevelModal=new HelpCallback("EditorSkillLevel",true);
		pageModelInformation=new HelpCallback("EditorModell",false);
		pageModelInformationModal=new HelpCallback("EditorModell",true);
		pageStatistic=new HelpCallback("Statistik",false);
		pageStatisticModal=new HelpCallback("Statistik",true);
		pageStatisticFastAccess=new HelpCallback("Filter",false);
		pageStatisticFastAccessModal=new HelpCallback("Filter",true);

		pageBatch=new HelpCallback("Batch",false);
		pageBatchModal=new HelpCallback("Batch",true);
		pageRearrange=new HelpCallback("Rearrange",false);
		pageRearrangeModal=new HelpCallback("Rearrange",true);
		pageConnected=new HelpCallback("Connected",false);
		pageConnectedModal=new HelpCallback("Connected",true);
		pageConnectedViewer=new HelpCallback("ConnectedViewer",false);
		pageConnectedViewerModal=new HelpCallback("ConnectedViewer",true);
		pageCompare=new HelpCallback("Compare",false);
		pageCompareModal=new HelpCallback("Compare",true);
		pageOptimize=new HelpCallback("Optimize",false);
		pageOptimizeModal=new HelpCallback("Optimize",true);
		pageOptimizeViewer=new HelpCallback("OptimizeViewer",false);
		pageOptimizeViewerModal=new HelpCallback("OptimizeViewer",true);
		pageRevenueOptimizer=new HelpCallback("RevenueOptimizer",false);
		pageCalibrate=new HelpCallback("Calibrate",false);
		pageServer=new HelpCallback("ToolsServer",false);
		pageVarianceAnalysis=new HelpCallback("ToolsVarianzanalyse",false);
		pageTableImport=new HelpCallback("Importer",false);

		dialogCaller=new HelpCallback("DialogCaller",true);
		dialogCallcenter=new HelpCallback("DialogCallcenter",true);
		dialogAgents=new HelpCallback("DialogAgents",true);
		dialogSkillLevel=new HelpCallback("DialogSkillLevel",true);
		dialogGlobalParameters=new HelpCallback("GlobaleParameter",true);
		dialogThreshold=new HelpCallback("ThresholdWarnings",true);

		dialogNew=new HelpCallback("DialogNew",true);
		dialogWizard=new HelpCallback("DialogWizard",true);
		dialogSetup=new HelpCallback("Setup",true);
		dialogParser=new HelpCallback("Parser",true);
		dialogPreplanning=new HelpCallback("Preplanning",true);
		dialogCalculator=new HelpCallback("ToolsRechner",true);
		dialogLoadCalculator=new HelpCallback("ToolsAuslastungsrechner",true);
		dialogFitDistribution=new HelpCallback("ToolsVerteilungAnpassen",true);
		dialogCommandLine=new HelpCallback("ToolsCommandLineDialog",true);
		dialogAutoSave=new HelpCallback("DialogAutoSaveSetup",true);
		dialogSpecialLoader=new HelpCallback("ToolsSpecialLoader",true);
		dialogGeneratorClients=new HelpCallback("ToolsGeneratorKunden",true);
		dialogGeneratorAgents=new HelpCallback("ToolsGeneratorAgenten",true);
		dialogGeneratorAgentsEfficiency=new HelpCallback("ToolsGeneratorAgentenProduktivitaet",true);
		dialogGeneratorAgentsAddition=new HelpCallback("ToolsGeneratorAgentenZuschlag",true);
		dialogGeneratorSkillLevels=new HelpCallback("ToolsGeneratorSkills",true);
		dialogSimpleSimulation=new HelpCallback("ToolsSimpleSimulation",true);
	}

	/**
	 * Liefert den Namen des aufzurufenden Topics.
	 * @return	Name des Topics, das angezeigt werden soll.
	 */
	public String getTopic() {
		return topic;
	}

	private void showHelp(boolean modal, String topic) {
		this.topic=topic;
		if (modal) {
			if (openHelpModalCallback!=null) SwingUtilities.invokeLater(openHelpModalCallback); }
		else {
			if (openHelpNonModalCallback!=null) SwingUtilities.invokeLater(openHelpNonModalCallback);
		}
	}

	private class HelpCallback implements Runnable {
		private final String topic;
		private final boolean modal;

		public HelpCallback(String topic, boolean modal) {
			this.topic=topic;
			this.modal=modal;
		}

		@Override
		public void run() {
			showHelp(modal,topic);
		}
	}
}
