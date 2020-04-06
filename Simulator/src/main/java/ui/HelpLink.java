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
 * Verknüpft die konkreten Hilfe-Callbacks mit den Dialogen.
 * @author Alexander Herzog
 * @version 1.0
 */
public class HelpLink {
	/* *** *** *** Haupt-Panel *** *** *** */

	/** Hilfeseite für Editor|Allgemeine Daten (nicht-modal) */
	public final Runnable pageGeneral;

	/** Hilfeseite für Editor|Allgemeine Daten (nicht-modal) */
	public final Runnable pageGeneralModal;

	/** Hilfeseite für Editor|Anrufer (nicht-modal) */
	public final Runnable pageCaller;

	/** Hilfeseite für Editor|Anrufer (modal) */
	public final Runnable pageCallerModal;

	/** Hilfeseite für Editor|Callcenter und Agenten (nicht-modal) */
	public final Runnable pageCallcenter;

	/** Hilfeseite für Editor|Callcenter und Agenten (modal) */
	public final Runnable pageCallcenterModal;

	/** Hilfeseite für Editor|Skill-Level (nicht-modal) */
	public final Runnable pageSkillLevel;

	/** Hilfeseite für Editor|Skill-Level (modal) */
	public final Runnable pageSkillLevelModal;

	/** Hilfeseite für Editor|Modellinformationen (nicht-modal) */
	public final Runnable pageModelInformation;

	/** Hilfeseite für Editor|Modellinformationen (nicht-modal) */
	public final Runnable pageModelInformationModal;

	/** Hilfeseite für Statistikergebnisse (nicht-modal) */
	public final Runnable pageStatistic;

	/** Hilfeseite für Statistikergebnisse (modal) */
	public final Runnable pageStatisticModal;

	/** Hilfeseite für Statistikergebnisse|Schnellzugriff (nicht-modal) */
	public final Runnable pageStatisticFastAccess;

	/** Hilfeseite für Statistikergebnisse|Schnellzugriff (modal) */
	public final Runnable pageStatisticFastAccessModal;

	/* *** *** *** Special-Panel *** *** *** */

	/** Hilfeseite für das Batchverarbeitungs-Panel (nicht-modal) */
	public final Runnable pageBatch;

	/** Hilfeseite für das Batchverarbeitungs-Panel (modal) */
	public final Runnable pageBatchModal;

	/** Hilfeseite für Rearrange-Panel (nicht-modal) */
	public final Runnable pageRearrange;

	/** Hilfeseite für Rearrange-Panel (modal) */
	public final Runnable pageRearrangeModal;

	/** Hilfeseite für die verbundene Simulation (nicht-modal) */
	public final Runnable pageConnected;

	/** Hilfeseite für die verbundene Simulation (modal) */
	public final Runnable pageConnectedModal;

	/** Hilfeseite für den Connected Viewer (nicht-modal) */
	public final Runnable pageConnectedViewer;

	/** Hilfeseite für den Connected Viewer (modal) */
	public final Runnable pageConnectedViewerModal;

	/** Hilfeseite für den Modellvergleich (nicht-modal) */
	public final Runnable pageCompare;

	/** Hilfeseite für den Modellvergleich (modal) */
	public final Runnable pageCompareModal;

	/** Hilfeseite für den Optimierer (nicht-modal) */
	public final Runnable pageOptimize;

	/** Hilfeseite für den Optimierer (modal) */
	public final Runnable pageOptimizeModal;

	/** Hilfeseite für den Optimierer-Ergebnisansicht (nicht-modal) */
	public final Runnable pageOptimizeViewer;

	/** Hilfeseite für den Optimierer-Ergebnisansicht (modal) */
	public final Runnable pageOptimizeViewerModal;

	/** Hilfeseite für den heuristischen Ertragsverbesserer (nicht-modal) */
	public final Runnable pageRevenueOptimizer;

	/** Hilfeseite für die Kalibrierung (nicht-modal) */
	public final Runnable pageCalibrate;

	/** Hilfeseite für den Simulationsserver (nicht-modal) */
	public final Runnable pageServer;

	/** Hilfeseite für die Varianzanalyse (nicht-modal) */
	public final Runnable pageVarianceAnalysis;

	/** Hilfeseite für den Vorlagen-Import (nicht-modal) */
	public final Runnable pageTableImport;

	/* *** *** *** Dialoge *** *** *** */

	/** Hilfeseite für den Anrufer-Dialog (im Modell-Editor) (modal) */
	public final Runnable dialogCaller;

	/** Hilfeseite für den Callcenter-Dialog (im Modell-Editor) (modal) */
	public final Runnable dialogCallcenter;

	/** Hilfeseite für den Agenten-Dialog (im Modell-Editor) (modal) */
	public final Runnable dialogAgents;

	/** Hilfeseite für den Skill-Level-Dialog (im Modell-Editor) (modal) */
	public final Runnable dialogSkillLevel;

	/** Hilfeseite für den "Globale Parameter"-Dialog (auf der "Allgemeine Daten"-Seite des Modell-Editors) (modal) */
	public final Runnable dialogGlobalParameters;

	/** Hilfeseite für den "Schwellenwerte"-Dialog (auf der "Allgemeine Daten"-Seite des Modell-Editors) (modal) */
	public final Runnable dialogThreshold;

	/** Hilfeseite für Datei|Neu (modal) */
	public final Runnable dialogNew;

	/** Hilfeseite für Datei|Neu mit Assistent (modal) */
	public final Runnable dialogWizard;

	/** Hilfeseite für Datei|Einstellungen (modal) */
	public final Runnable dialogSetup;

	/** Hilfeseite für den Formelparser-Dialog (modal) */
	public final Runnable dialogParser;

	/** Hilfeseite für den Agenten-Vorplanungs-Dialog (modal) */
	public final Runnable dialogPreplanning;

	/** Hilfeseite für den Rechner-Dialog (modal) */
	public final Runnable dialogCalculator;

	/** Hilfeseite für den Auslastungsrechner-Dialog (modal) */
	public final Runnable dialogLoadCalculator;

	/** Hilfeseite für den Verteilungsanpassungs-Dialog (modal) */
	public final Runnable dialogFitDistribution;

	/** Hilfeseite für den Kommandozeilen-Dialog (modal) */
	public final Runnable dialogCommandLine;

	/** Hilfeseite für den Auto-Speicher-Dialog (modal) */
	public final Runnable dialogAutoSave;

	/** Hilfeseite für den Speical-Loader-Dialog ("Daten in speziellem Format laden") (modal) */
	public final Runnable dialogSpecialLoader;

	/** Hilfeseite für den Kundendaten-Generator-Dialog (modal) */
	public final Runnable dialogGeneratorClients;

	/** Hilfeseite für den Agentendaten-Generator-Dialog (modal) */
	public final Runnable dialogGeneratorAgents;

	/** Hilfeseite für den Agenten-Produktivität-Generator-Dialog (modal) */
	public final Runnable dialogGeneratorAgentsEfficiency;

	/** Hilfeseite für den Agenten-Krankheitsbedingten-Zuschlag-Dialog (modal) */
	public final Runnable dialogGeneratorAgentsAddition;

	/** Hilfeseite für den Skill-Level-Generator-Dialog (modal) */
	public final Runnable dialogGeneratorSkillLevels;

	/** Hilfeseite für den Einfache-Simulation-Dialog (modal) */
	public final Runnable dialogSimpleSimulation;



	private String topic="";
	private final Runnable openHelpNonModalCallback;
	private final Runnable openHelpModalCallback;

	/**
	 * Erstellt ein Objekt, welches konkrete Hilfeseiten (als Topic-Texte) mit Runnable verknüpft.
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
