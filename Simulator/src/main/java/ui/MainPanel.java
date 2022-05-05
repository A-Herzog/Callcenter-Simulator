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

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.Container;
import java.awt.Desktop;
import java.awt.Point;
import java.awt.Window;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.InputEvent;
import java.awt.event.KeyEvent;
import java.io.File;
import java.io.IOException;
import java.io.Serializable;
import java.lang.reflect.InvocationTargetException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Timer;
import java.util.TimerTask;

import javax.swing.AbstractAction;
import javax.swing.AbstractButton;
import javax.swing.Action;
import javax.swing.JButton;
import javax.swing.JComponent;
import javax.swing.JFileChooser;
import javax.swing.JFrame;
import javax.swing.JMenu;
import javax.swing.JMenuBar;
import javax.swing.JMenuItem;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JPopupMenu;
import javax.swing.JToolBar;
import javax.swing.KeyStroke;
import javax.swing.SwingUtilities;
import javax.swing.filechooser.FileFilter;
import javax.swing.filechooser.FileNameExtensionFilter;

import org.w3c.dom.Element;

import language.Language;
import mathtools.distribution.swing.CommonVariables;
import mathtools.distribution.tools.FileDropperData;
import net.calc.SimServerPanel;
import net.calc.StartAnySimulator;
import simulator.BackgroundSimulator;
import simulator.CallcenterSimulatorInterface;
import simulator.Statistics;
import systemtools.MainPanelBase;
import systemtools.MsgBox;
import systemtools.commandline.CommandLineDialog;
import systemtools.help.HelpBase;
import systemtools.help.IndexSystem;
import systemtools.statistics.StatisticsBasePanel;
import tools.SetupData;
import ui.calculator.CalculatorDialog;
import ui.calculator.QueueingCalculatorDialog;
import ui.commandline.CommandLineSystem;
import ui.compare.ComparePanel;
import ui.compare.CompareSelectDialog;
import ui.connected.ConnectedModel;
import ui.connected.ConnectedPanel;
import ui.connected.ConnectedViewer;
import ui.dataloader.SpecialProcessingDialog;
import ui.dialogs.AutoSaveSetupDialog;
import ui.dialogs.FitDialog;
import ui.dialogs.InfoDialog;
import ui.dialogs.LicenseViewer;
import ui.dialogs.PreplanningDialog;
import ui.dialogs.SetupDialog;
import ui.editor.BaseEditDialog;
import ui.editor.CallcenterModelEditorPanel;
import ui.editor.CallcenterModelEditorPanelDialog;
import ui.editor.NewModelDialog;
import ui.editor.NewModelWizard;
import ui.generator.AgentsGeneratorDialog;
import ui.generator.CallerGeneratorDialog;
import ui.generator.GeneratorBaseDialog;
import ui.generator.SkillsGeneratorDialog;
import ui.help.HTMLPanel;
import ui.help.Help;
import ui.help.HelpConsts;
import ui.images.Images;
import ui.importer.ImporterData;
import ui.importer.ImporterPanel;
import ui.model.CallcenterModel;
import ui.model.CallcenterModelExamples;
import ui.model.CallcenterRunModel;
import ui.optimizer.OptimizeData;
import ui.optimizer.OptimizePanel;
import ui.optimizer.OptimizeSetup;
import ui.optimizer.OptimizeViewer;
import ui.simplesimulation.SimpleSimulationDialog;
import ui.specialpanels.BatchPanel;
import ui.specialpanels.CalibratePanel;
import ui.specialpanels.CallcenterRunPanel;
import ui.specialpanels.JCloseablePanel;
import ui.specialpanels.JWorkPanel;
import ui.specialpanels.RearrangePanel;
import ui.specialpanels.RevenueOptimizerPanel;
import ui.specialpanels.VarianzAnalysePanel;
import ui.specialpanels.ViewerWithLoadModelCallback;
import ui.statistic.StatisticPanel;
import xml.XMLTools;

/**
 * Diese Klasse stellt den Arbeitsbereich innerhalb des Programmfensters dar.
 * @author Alexander Herzog
 */
public final class MainPanel extends MainPanelBase {
	/**
	 * Serialisierungs-ID der Klasse
	 * @see Serializable
	 */
	private static final long serialVersionUID=4554929295486959960L;

	/**
	 * Homepage-Adresse f�r Webbrowseraufrufe
	 */
	public static final String WEB_URL="github.com/A-Herzog/Callcenter-Simulator";

	/**
	 * Autor des Programms
	 */
	public static final String AUTHOR="Alexander Herzog";

	/** Aktuelle Unterversionsnummer in der Java 8 Versionsreihe */
	private static final int JAVA8_SECURE_MIN_VERSION=332;
	/** Aktuelle Unterversionsnummer in der Java 9 Versionsreihe */
	private static final int JAVA9_SECURE_MIN_VERSION=4;
	/** Aktuelle Unterversionsnummer in der Java 10 Versionsreihe */
	private static final int JAVA10_SECURE_MIN_VERSION=2;
	/** Aktuelle Unterversionsnummer in der Java 11 Versionsreihe */
	private static final int JAVA11_SECURE_MIN_VERSION=15;
	/** Aktuelle Unterversionsnummer in der Java 12 Versionsreihe */
	private static final int JAVA12_SECURE_MIN_VERSION=2;
	/** Aktuelle Unterversionsnummer in der Java 13 Versionsreihe */
	private static final int JAVA13_SECURE_MIN_VERSION=2;
	/** Aktuelle Unterversionsnummer in der Java 14 Versionsreihe */
	private static final int JAVA14_SECURE_MIN_VERSION=2;
	/** Aktuelle Unterversionsnummer in der Java 15 Versionsreihe */
	private static final int JAVA15_SECURE_MIN_VERSION=2;
	/** Aktuelle Unterversionsnummer in der Java 16 Versionsreihe */
	private static final int JAVA16_SECURE_MIN_VERSION=2;
	/** Aktuelle Unterversionsnummer in der Java 17 Versionsreihe */
	private static final int JAVA17_SECURE_MIN_VERSION=3;
	/** Aktuelle Unterversionsnummer in der Java 18 Versionsreihe */
	private static final int JAVA18_SECURE_MIN_VERSION=1;

	/**
	 * Bezeichnung f�r "ungespeichertes Modell" in der Titelzeile f�r ein neues Modell, welches noch keinen Namen besitzt
	 */
	public static String UNSAVED_MODEL="";

	/**
	 * Callback, welches ein Neuladen des Fensters veranlasst.
	 */
	private Runnable reloadWindow;

	/**
	 * Callback das an Dialoge �bergeben wird, um
	 * Hilfeseiten aufrufen zu k�nnen.
	 */
	private final HelpLink helpLink;

	/**
	 * Toolbar innerhalb des Panels
	 */
	private JToolBar toolBar;

	/**
	 * Ribbon-Bar innerhalb des Panels
	 */
	private JRibbonBar ribbonBar;

	/**
	 * Info-Panel f�r die Anzeige von
	 * Nachriten innerhalb des Panels
	 */
	private JPanel infoPanel;

	/** Schaltfl�che "Modell laden" */
	private JButton loadModelButton;

	/** Schaltfl�che "Modell speichern" */
	private JButton saveModelButton;

	/** Schaltfl�che "Statistik laden" */
	private JButton loadStatisticButton;

	/** Schaltfl�che "Statistik speichern" */
	private JButton saveStatisticButton;

	/** Schaltfl�che "Modell-Editor anzeigen" */
	private JButton editorButton;

	/** Schaltfl�che "Statistikansicht anzeigen" */
	private JButton statistikButton;

	/** Schaltfl�che "Simulation starten" */
	private JButton simButton;

	/** Schaltfl�che "Modell zu den Ergebnissen anzeigen" */
	private JButton showModel;

	/** Schaltfl�che "Statistik-WebViewer anzeigen" */
	private JButton showWebViewer;

	/**
	 * Liste der "Statistik speichern"-Men�eintr�ge oder -Schaltfl�chen
	 */
	private final List<AbstractButton> menuSaveStatistics=new ArrayList<>();

	/**
	 * Liste der "Modell zu den Ergebnissen anzeigen"-Men�eintr�ge oder -Schaltfl�chen
	 */
	private final List<AbstractButton> menuSimulationShowModel=new ArrayList<>();

	/**
	 * Liste der "Statistik-WebViewer anzeigen"-Men�eintr�ge oder -Schaltfl�chen
	 */

	private final List<AbstractButton> menuSimulationShowWebViewer=new ArrayList<>();

	/**
	 * Liste der "Mit festgehaltenem Modell vergleichen"-Men�eintr�ge oder -Schaltfl�chen
	 */
	private final List<AbstractButton> menuToolsCompareKeptModel=new ArrayList<>();

	/**
	 * Men�punkt "Datei" - "Zuletzt verwendet"
	 */
	private JMenu menuRecentlyUsed;

	/**
	 * Willkommen-Seite (wenn gerade aktiv)
	 */
	private HTMLPanel welcomePanel;

	/**
	 * Editor-Panel, welches das Modell vorh�lt
	 */
	private CallcenterModelEditorPanel modelPanel;

	/**
	 * Statistik-Seite
	 */
	private StatisticPanel statisticPanel;

	/**
	 * Aktuell aktives Arbeits-Panel f�r besondere
	 * Programmfunktionen (kann <code>null</code>
	 * sein, wenn kein Arbeits-Panel aktiv ist)
	 */
	private JCloseablePanel workPanel=null;


	/**
	 * 0=Modell-Editor, 1=Statistik
	 * @see #setGUIMode(short)
	 */
	private short currentGUIMode=-1;

	/**
	 * System f�r die Hintergrundsimulation
	 */
	private final BackgroundSimulator backgroundSimulator;

	/**
	 * Stellt ein, ob nach einer laufenden Simulation die
	 * Ergebnisse sofort gespeichert werden sollen.
	 * @see #commandSimulationRun(boolean, File)
	 * @see SimDoneNotify
	 */
	private boolean saveAfterSim;

	/**
	 * Speichert zwischen, ob das aktuelle Modell
	 * im Editor einem Beispielmodell entspricht
	 */
	private int equalsExampleModel=-1;

	/**
	 * Modell f�r den Vergleich mit einem ge�nderten Modell festhalten
	 */
	private CallcenterModel pinnedModel;

	/** Statistikdaten, die sich beim Modellvergleich ergeben haben */
	private final Statistics[] pinnedCompareStatistic=new Statistics[2];

	/**
	 * Konstruktor der Klasse
	 * @param ownerWindow	�bergeordnetes Fenster
	 * @param programName	Name des Programms (wird dann �ber {@link MainPanelBase#programName} angeboten)
	 * @param isReload	Gibt an, ob es sich bei dem Aufbau des Panels um einen Programmstart (<code>false</code>) oder nur um einen Wiederaufbau z.B. nach dem �ndern der Sprache (<code>true</code>) handelt
	 */
	public MainPanel(final Window ownerWindow, final String programName, final boolean isReload) {
		super(ownerWindow,programName);
		initActions();
		initToolbar();

		SetupData setup=SetupData.getSetup();

		helpLink=new HelpLink(
				link->commandHelp(link.getTopic()),
				link->showModalHelp(link.getTopic(),link.getParent()),
				ownerWindow
				);
		Help.globalSpecialLinkListener=href->processSpecialLink(href);

		/* Editor, Statistik und Welcome */
		Thread t1=new Thread() {
			@Override
			public void run() {
				modelPanel=new CallcenterModelEditorPanel(
						MainPanel.this.ownerWindow,!SetupData.getSetup().ribbonMode,getNewModel(SetupData.getSetup().startModusModel),false,
						helpLink,
						new GeneratorCallback(0),new GeneratorCallback(1),new GeneratorCallback(2),
						new GeneratorCallback(3),new GeneratorCallback(4),new GeneratorCallback(5),
						new GeneratorCallback(8),new GeneratorCallback(9),new GeneratorCallback(4));
			}
		};
		t1.start();

		Thread t2=new Thread() {
			@Override
			public void run() {
				statisticPanel=new StatisticPanel(true,helpLink,helpLink.pageStatisticModal,()->commandSimulationRun(false,null),()->commandFileStatisticsLoad(null),1);
				statisticPanel.addFileDropListener(e->{if (e.getSource() instanceof FileDropperData) dropFile((FileDropperData)e.getSource());});
			}
		};
		t2.start();


		Thread t3=new Thread() {
			@Override
			public void run() {
				welcomePanel=new HTMLPanel(false,false,null);
				welcomePanel.setProcessSpecialLink(new ProcessSpecialWelcomeLink());
			}
		};
		t3.start();

		try {t1.join();} catch (InterruptedException e) {}

		try {t2.join();} catch (InterruptedException e) {}

		try {t3.join();} catch (InterruptedException e) {}

		/* Finale GUI-Einrichtung (erfordert geladene Sprache) */
		setAdditionalTitle(UNSAVED_MODEL);
		setGUIMode((short)(setup.startWelcomePage?2:0));

		StatisticsBasePanel.viewerPrograms.clear();
		if (setup.openWord) StatisticsBasePanel.viewerPrograms.add(StatisticsBasePanel.ViewerPrograms.WORD);
		if (setup.openODT) StatisticsBasePanel.viewerPrograms.add(StatisticsBasePanel.ViewerPrograms.ODT);
		if (setup.openExcel) StatisticsBasePanel.viewerPrograms.add(StatisticsBasePanel.ViewerPrograms.EXCEL);
		if (setup.openODS) StatisticsBasePanel.viewerPrograms.add(StatisticsBasePanel.ViewerPrograms.ODS);
		if (setup.openPDF) StatisticsBasePanel.viewerPrograms.add(StatisticsBasePanel.ViewerPrograms.PDF);

		/* Hintergrundpr�fung und -Simulation initialisieren */
		backgroundSimulator=new BackgroundSimulator(new BackgroundModelGetter(),s->{
			if (infoPanel==null || s!=null) setMessagePanel(null,s,MessagePanelIcon.WARNING);
		});

		/* Verz�gerte Ausf�hrung */
		SwingUtilities.invokeLater(()->{
			/* Java-Pr�fung */
			javaVersionCheck();

			/* Hilfe-Index aufbauen */
			final IndexSystem indexSystem=IndexSystem.getInstance();
			indexSystem.addLanguage("de","pages_de");
			indexSystem.addLanguage("en","pages_en");
			indexSystem.setCharset(StandardCharsets.ISO_8859_1);
			indexSystem.init(Help.class);
			IndexSystem.getInstance().setLanguage(Language.getCurrentLanguage());
		});
	}

	@Override
	protected void registerDropTargets(systemtools.MainFrameBase.DropTargetRegister dropTargetRegister) {
		dropTargetRegister.registerJComponent(modelPanel);
		dropTargetRegister.registerJComponent(statisticPanel);
		dropTargetRegister.registerJComponent(welcomePanel);
		dropTargetRegister.registerJComponent(welcomePanel.getBrowserJComponent());
	}

	/**
	 * Reagiert auf Drag&amp;Drop-Aktionen auf das Editor- oder das Statistik-Panel.
	 * @param drop	Drag&amp;Drop-Element
	 */
	private void dropFile(final FileDropperData drop) {
		final File file=drop.getFile();
		if (file.isFile()) {
			drop.dragDropConsumed();
			SwingUtilities.invokeLater(()->{
				if (loadAnyFile(file,drop.getDropComponent(),drop.getDropPosition(),true)) {
					CommonVariables.setInitialDirectoryFromFile(file);
				}
			});
		}
	}

	/**
	 * Liefert dem Namen f�r die Hilfeseite der Willkommens-Seite
	 * (ist abh�ngig davon, ob der normale Toolbar oder der Ribbon-Bar verwendet werden soll).
	 * @return	Namen f�r die Hilfeseite der Willkommens-Seite
	 */
	private String getWelcomePageName() {
		if (SetupData.getSetup().ribbonMode) {
			return HelpConsts.WELCOME_PAGE_RIBBON;
		} else {
			return HelpConsts.WELCOME_PAGE;
		}
	}

	/**
	 * Liefert die Versionskennung der Java-Laufzeitumgebung
	 * @return	Versionskennung zerlegt in einzelne Teile
	 * @see #javaVersionCheck()
	 */
	private static int[] getJavaVersion() {
		final String version=System.getProperty("java.version");
		if (version==null) return new int[]{7,0};

		if (version.startsWith("1.8")) {
			final String[] ver=version.split("_");
			if (ver.length!=2 || !ver[0].equals("1.8.0")) return new int[]{8,0};
			int security=0;
			try {security=Integer.parseInt(ver[1]);} catch (Exception e) {security=0;}
			return new int[]{8,security};
		}

		final String[] ver=version.split("\\.");

		int major=0;
		try {major=Integer.parseInt(ver[0]);} catch (Exception e) {major=0;}
		if (major==0) return new int[]{7,0};
		int security=0;
		if (ver.length>1) try {
			final String part=ver[ver.length-1].split("\\-")[0];
			security=Integer.parseInt(part);
		} catch (Exception e) {security=0;}
		return new int[]{major,security};
	}

	/**
	 * Pr�ft, ob die verwendete Java-Version noch aktuell ist
	 * und zeigt ggf. eine Warnung an.
	 */
	private void javaVersionCheck() {
		final SetupData setup=SetupData.getSetup();

		if (setup.languageWasAutomaticallySet()) {
			infoPanel=setMessagePanel(null,Language.tr("Window.LanguageAutomatic"),MessagePanelIcon.INFO);
			infoPanel.setBackground(new Color(255,255,240));
			new Timer().schedule(new TimerTask() {@Override public void run() {
				setMessagePanel(null,null,null);
				if (infoPanel!=null) infoPanel.setBackground(new Color(255,240,0));
				infoPanel=null;
			}},7500);
		} else {
			if (!setup.testJavaVersion) return;
			final int[] ver=getJavaVersion();
			boolean ok=true;
			if (ver[0]==8 && ver[1]<JAVA8_SECURE_MIN_VERSION) ok=false;
			if (ver[0]==9 && ver[1]<JAVA9_SECURE_MIN_VERSION) ok=false;
			if (ver[0]==10 && ver[1]<JAVA10_SECURE_MIN_VERSION) ok=false;
			if (ver[0]==11 && ver[1]<JAVA11_SECURE_MIN_VERSION) ok=false;
			if (ver[0]==12 && ver[1]<JAVA12_SECURE_MIN_VERSION) ok=false;
			if (ver[0]==13 && ver[1]<JAVA13_SECURE_MIN_VERSION) ok=false;
			if (ver[0]==14 && ver[1]<JAVA14_SECURE_MIN_VERSION) ok=false;
			if (ver[0]==15 && ver[1]<JAVA15_SECURE_MIN_VERSION) ok=false;
			if (ver[0]==16 && ver[1]<JAVA16_SECURE_MIN_VERSION) ok=false;
			if (ver[0]==17 && ver[1]<JAVA17_SECURE_MIN_VERSION) ok=false;
			if (ver[0]==18 && ver[1]<JAVA18_SECURE_MIN_VERSION) ok=false;
			if (ok) return;

			infoPanel=setMessagePanel(Language.tr("Dialog.Title.Warning"),Language.tr("Window.JavaSecurityWarnung"),Language.tr("Window.JavaSecurityWarnung.Link"),MessagePanelIcon.WARNING);
			new Timer().schedule(new TimerTask() {@Override public void run() {
				setMessagePanel(null,null,null);
				infoPanel=null;
			}},7500);
		}
	}

	@Override
	protected URL getResourceURL(String path) {
		return getClass().getResource(path);
	}

	@Override
	public boolean allowQuitProgram() {
		if (workPanel!=null) {workPanel.requestClose(); return false;}
		if (modelPanel==null) return false;
		if (modelPanel.isModelChanged()) {if (!discardModelOk()) return false;}
		return true;
	}

	/**
	 * Initiiert die Zuordnung von Aktionsnamen zu {@link Runnable}-Funktionen.
	 */
	private void initActions() {
		/* Datei */
		addAction("FileNew",e->commandFileModelNewFromTemplate());
		addAction("FileNewWizard",e->commandFileModelNewFromWizard());
		addAction("FileLoad",e->commandFileModelLoad(null));
		addAction("FileSave",e->commandFileModelSave());
		addAction("FileSaveAs",e->commandFileModelSaveAs());
		addAction("FileStatisticsLoad",e->commandFileStatisticsLoad(null));
		addAction("FileStatisticsSave",e->commandFileStatisticsSave());
		addAction("FileSetup",e->commandFileSetup(false));
		addAction("FileQuit",e->close());

		/* Ansicht */
		addAction("ViewWelcome",e->setGUIMode((short)2));
		addAction("ViewEditor",e->setGUIMode((short)0));
		addAction("ViewStatistic",e->setGUIMode((short)1));

		/* Daten */
		addAction("DataAll",e->commandDataGenerator(7,-1,null));
		addAction("DataCaller24",e->commandDataGenerator(0,-1,null));
		addAction("DataCaller48",e->commandDataGenerator(1,-1,null));
		addAction("DataCaller96",e->commandDataGenerator(2,-1,null));
		addAction("DataAgents24",e->commandDataGenerator(3,-1,null));
		addAction("DataAgents48",e->commandDataGenerator(4,-1,null));
		addAction("DataAgents96",e->commandDataGenerator(5,-1,null));
		addAction("DataAgentsEfficiency",e->commandDataGenerator(8,-1,null));
		addAction("DataAgentsAddition",e->commandDataGenerator(9,-1,null));
		addAction("DataBedienzeiten",e->commandDataGenerator(6,-1,null));
		addAction("DataSimplify",e->commandDataSimplify());
		addAction("DataSpecialLoader",e->commandDataSpecialLoader());

		/* Simulation */
		addAction("SimulationCheck",e->commandSimulationCheck());
		addAction("SimulationRun",e->commandSimulationRun(false, null));
		addAction("SimulationRunAndSave",e->commandSimulationRun(true, null));
		addAction("SimulationRunAndSaveSetup",e->commandSimulationRunAndSaveSetup());
		addAction("SimulationBatch",e->commandSimulationBatch());
		addAction("SimulationCalibrate",e->commandSimulationCalibrate());
		addAction("SimulationConnected",e->commandSimulationConnected(null));
		addAction("SimulationConnectedViewer",e->commandSimulationConnectedViewer());
		addAction("SimulationPreplanning",e->commandSimulationPreplanning());
		addAction("SimulationOptimize",e->commandSimulationOptimize(null));
		addAction("SimulationOptimizeViewer",e->commandSimulationOptimizeViewer());
		addAction("SimulationRearrange",e->commandSimulationRearrange());
		addAction("SimulationHeuristicRevenueOptimizer",e->commandSimulationRevenueOptimizer());
		addAction("SimulationShowModel",e->commandSimulationShowModel());
		addAction("SimulationShowWebViewer",e->commandSimulationShowWebViewer());

		/* Tools */
		addAction("ToolsRechner",e->commandToolsRechner());
		addAction("ToolsAuslastungsrechner",e->commandToolsAuslastungsrechner());
		addAction("ToolsSimpleSimulation",e->commandToolsSimpleSimulation());
		addAction("ToolsVarianzanalyse",e->commandToolsVarianzanalyse());
		addAction("ToolsFitting",e->commandToolsFitting());
		addAction("ToolsExecuteCommand",e->commandToolsExecuteCommand());
		addAction("ToolsLogRun",e->commandToolsLogRun());
		addAction("ToolsReport",e->{
			setGUIMode((short)1);
			if (!statisticPanel.selectReportNode()) MsgBox.error(MainPanel.this,Language.tr("Window.Report.NoStatisticAvailable"),Language.tr("Window.Report.NoStatisticAvailable.Info"));
		});
		addAction("ToolsCompare",e->commandToolsCompare());
		addAction("ToolsKeepModel",e->commandToolsKeepModel());
		addAction("ToolsCompareKeptModel",e->commandToolsCompareKeptModel(0));
		addAction("ToolsServer",e->commandToolsServer());

		/* Hilfe */
		addAction("HelpHelp",e->commandHelp(null));
		addAction("HelpContent",e->commandHelp("Start"));
		addAction("HelpModel",e->commandHelpModel());
		addAction("HelpGlossary",e->commandHelpGlossary());
		addAction("HelpBook",e->commandHelpBook());
		addAction("HelpMail",e->{
			try {
				Desktop.getDesktop().mail(new URI("mailto:"+UpdateSystem.mailURL));
			} catch (IOException | URISyntaxException e1) {
				MsgBox.error(MainPanel.this,Language.tr("Window.Info.NoEMailProgram.Title"),String.format(Language.tr("Window.Info.NoEMailProgram.Info"),"mailto:"+UpdateSystem.mailURL));
			}
		});
		addAction("HelpHomepage",e->{
			try {
				final URI uri=new URI(UpdateSystem.defaultProtocollHomepage+"://"+WEB_URL);
				if (!MsgBox.confirmOpenURL(this,uri)) return;
				Desktop.getDesktop().browse(uri);
			} catch (IOException | URISyntaxException e1) {
				MsgBox.error(this,Language.tr("Window.Info.NoInternetConnection"),String.format(Language.tr("Window.Info.NoInternetConnection.ModelOverview"),UpdateSystem.defaultProtocollHomepage+"://"+WEB_URL));
			}
		});
		addAction("HelpWelcome",e->setGUIMode((short)2));
		addAction("HelpLicense",e->commandHelpLicenseInfo());
		addAction("HelpUpdates",e->commandFileSetup(true));
		addAction("HelpInfo",e->commandHelpInfo());
	}

	@Override
	public JComponent createToolBar() {
		if (SetupData.getSetup().ribbonMode) {
			JRibbonBar ribbonBar=createRibbonBar();
			ribbonBar.addChangeListener(e-> {
				int index=ribbonBar.getSelectedIndex();
				switch (index) {
				case 0: setGUIMode((short)2); break;
				case 1:
				case 2:
				case 3:
				case 4:
				case 5:	setGUIMode((short)0); modelPanel.setSelectedTabIndex(index-1); break;
				case 6: setGUIMode((short)1); break;
				}
				if (ribbonBar.getSelectedIndex()!=index) ribbonBar.setSelectedIndex(index);
			});
			this.ribbonBar=ribbonBar;
			return ribbonBar;
		} else {
			return createClassicToolBar();
		}
	}

	/**
	 * Erzeugt einen normalen Toolbar
	 * @return	Neuer Toolbar
	 * @see #createToolBar()
	 */
	private JToolBar createClassicToolBar() {
		final JToolBar toolbar=new JToolBar();
		toolbar.setFloatable(false);

		loadModelButton=createToolbarButton(toolbar,Language.tr("MainToolbar.Load"),Language.tr("MainToolbar.Load.Tooltip"),Images.MODEL_LOAD.getIcon(),"FileLoad");
		saveModelButton=createToolbarButton(toolbar,Language.tr("MainToolbar.Save"),Language.tr("MainToolbar.Save.Tooltip"),Images.MODEL_SAVE.getIcon(),"FileSave");
		loadStatisticButton=createToolbarButton(toolbar,Language.tr("MainToolbar.LoadStatistic"),Language.tr("MainToolbar.LoadStatistic.Tooltip"),Images.STATISTICS_LOAD.getIcon(),"FileStatisticsLoad");
		saveStatisticButton=createToolbarButton(toolbar,Language.tr("MainToolbar.SaveStatistic"),Language.tr("MainToolbar.SaveStatistic.Tooltip"),Images.STATISTICS_SAVE.getIcon(),"FileStatisticsSave");
		menuSaveStatistics.add(saveStatisticButton);

		toolbar.addSeparator();

		editorButton=createToolbarButton(toolbar,Language.tr("MainToolbar.Editor"),Language.tr("MainToolbar.Editor.Tooltip"),Images.MODEL.getIcon(),"ViewEditor");
		statistikButton=createToolbarButton(toolbar,Language.tr("MainToolbar.Results"),Language.tr("MainToolbar.Results.Tooltip"),Images.STATISTICS.getIcon(),"ViewStatistic");

		toolbar.addSeparator();

		simButton=createToolbarButton(toolbar,Language.tr("MainToolbar.Run"),Language.tr("MainToolbar.Run.Tooltip"),Images.SIMULATION.getIcon(),"SimulationRun");
		showModel=createToolbarButton(toolbar,Language.tr("MainToolbar.ShowModel"),Language.tr("MainToolbar.ShowModel.Tooltip"),Images.STATISTICS_SHOW_MODEL.getIcon(),"SimulationShowModel");
		menuSimulationShowModel.add(showModel);
		showModel.setVisible(false);
		showWebViewer=createToolbarButton(toolbar,Language.tr("MainToolbar.ShowWebViewer"),Language.tr("MainToolbar.ShowWebViewer.Tooltip"),Images.STATISTICS_SHOW_WEBVIEWER.getIcon(),"SimulationShowWebViewer");
		menuSimulationShowWebViewer.add(showWebViewer);
		showWebViewer.setVisible(false);

		toolbar.addSeparator();

		createToolbarButton(toolbar,Language.tr("MainToolbar.Help"),Language.tr("MainToolbar.Help.Tooltip"),Images.HELP.getIcon(),"HelpHelp");

		/*
		JButton button;
		toolbar.add(button=new JButton("Test"));
		button.addActionListener(e->{ });
		 */

		this.toolBar=toolbar;
		return toolbar;
	}

	@Override
	public JMenuBar createMenu() {
		if (SetupData.getSetup().ribbonMode) return null;

		JMenuBar menubar=new JMenuBar();
		JMenu menu;
		JMenuItem item;

		/* Datei */
		menubar.add(menu=new JMenu(Language.tr("MainMenu.File")));
		setMnemonic(menu,Language.tr("MainMenu.File.Mnemonic"));

		createMenuItemCtrl(menu,Language.tr("MainMenu.File.NewModel"),Images.MODEL_NEW.getIcon(),Language.tr("MainMenu.File.NewModel.EmptyModel.Mnemonic"),KeyEvent.VK_N,"FileNew");
		createMenuItemCtrlShift(menu,Language.tr("MainMenu.File.NewModel.CreateWithWizard"),Images.MODEL_WIZARD.getIcon(),Language.tr("MainMenu.File.NewModel.CreateWithWizard.Mnemonic"),KeyEvent.VK_N,"FileNewWizard");
		createMenuItemCtrl(menu,Language.tr("MainMenu.File.LoadModel"),Images.MODEL_LOAD.getIcon(),Language.tr("MainMenu.File.LoadModel.Mnemonic"),KeyEvent.VK_L,"FileLoad");
		menu.add(menuRecentlyUsed=new JMenu(Language.tr("MainMenu.File.RecentlyUsedModels")));
		setMnemonic(menuRecentlyUsed,Language.tr("MainMenu.File.RecentlyUsedModels.Mnemonic"));
		updateLastFilesList();
		createMenuItemCtrl(menu,Language.tr("MainMenu.File.SaveModel"),Images.MODEL_SAVE.getIcon(),Language.tr("MainMenu.File.SaveModel.Mnemonic"),KeyEvent.VK_S,"FileSave");
		createMenuItemCtrl(menu,Language.tr("MainMenu.File.SaveModelAs"),Language.tr("MainMenu.File.SaveModelAs.Mnemonic"),KeyEvent.VK_U,"FileSaveAs");
		menu.addSeparator();
		createMenuItemCtrlShift(menu,Language.tr("MainMenu.File.LoadStatistics"),Images.STATISTICS_LOAD.getIcon(),Language.tr("MainMenu.File.LoadStatistics.Mnemonic"),KeyEvent.VK_L,"FileStatisticsLoad");
		menuSaveStatistics.add(createMenuItemCtrlShift(menu,Language.tr("MainMenu.File.SaveStatistics"),Images.STATISTICS_SAVE.getIcon(),Language.tr("MainMenu.File.SaveStatistics.Mnemonic"),KeyEvent.VK_S,"FileStatisticsSave"));
		menu.addSeparator();
		createMenuItemCtrl(menu,Language.tr("MainMenu.File.Setup"),Images.GENERAL_SETUP.getIcon(),Language.tr("MainMenu.File.Setup.Mnemonic"),KeyEvent.VK_P,"FileSetup");
		menu.addSeparator();
		createMenuItemCtrl(menu,Language.tr("MainMenu.File.Quit"),Images.GENERAL_EXIT.getIcon(),Language.tr("MainMenu.File.Quit.Mnemonic"),KeyEvent.VK_W,"FileQuit");

		/* Ansicht */
		menubar.add(menu=new JMenu(Language.tr("MainMenu.View")));
		setMnemonic(menu,Language.tr("MainMenu.View.Mnemonic"));

		createMenuItem(menu,Language.tr("MainMenu.View.Welcome"),Images.GENERAL_INFO.getIcon(),Language.tr("MainMenu.View.Welcome.Mnemonic"),KeyEvent.VK_F2,"ViewWelcome");
		createMenuItem(menu,Language.tr("MainMenu.View.ModelEditor"),Images.MODEL.getIcon(),Language.tr("MainMenu.View.ModelEditor.Mnemonic"),KeyEvent.VK_F3,"ViewEditor");
		createMenuItem(menu,Language.tr("MainMenu.View.Statistics"),Images.STATISTICS.getIcon(),Language.tr("MainMenu.View.Statistics.Mnemonic"),KeyEvent.VK_F4,"ViewStatistic");

		/* Daten */
		menubar.add(menu=new JMenu(Language.tr("MainMenu.Data")));
		setMnemonic(menu, Language.tr("MainMenu.Data.Mnemonic"));

		createMenuItemCtrl(menu,Language.tr("MainMenu.Data.LoadViaTemplate"),Images.DATA_LOAD_BY_TEMPLATE.getIcon(),Language.tr("MainMenu.Data.LoadViaTemplate.Mnemonic"),KeyEvent.VK_I,"DataAll");
		createMenuItem(menu,Language.tr("MainMenu.Data.LoadExampleData"),Language.tr("MainMenu.Data.LoadExampleData.Mnemonic"),"DataSpecialLoader");
		menu.addSeparator();
		createMenuItem(menu,Language.tr("MainMenu.Data.LoadCaller24"),Images.DATA_LOAD_CALLER.getIcon(),Language.tr("MainMenu.Data.LoadCaller24.Mnemonic"),"DataCaller24");
		createMenuItem(menu,Language.tr("MainMenu.Data.LoadCaller48"),Images.DATA_LOAD_CALLER.getIcon(),Language.tr("MainMenu.Data.LoadCaller48.Mnemonic"),"DataCaller48");
		createMenuItem(menu,Language.tr("MainMenu.Data.LoadCaller96"),Images.DATA_LOAD_CALLER.getIcon(),Language.tr("MainMenu.Data.LoadCaller96.Mnemonic"),"DataCaller96");
		createMenuItem(menu,Language.tr("MainMenu.Data.LoadAgents24"),Images.DATA_LOAD_AGENTS.getIcon(),'\0',"DataAgents24");
		createMenuItem(menu,Language.tr("MainMenu.Data.LoadAgents48"),Images.DATA_LOAD_AGENTS.getIcon(),Language.tr("MainMenu.Data.LoadAgents.Mnemonic"),"DataAgents48");
		createMenuItem(menu,Language.tr("MainMenu.Data.LoadAgents96"),Images.DATA_LOAD_AGENTS.getIcon(),'\0',"DataAgents96");
		createMenuItem(menu,Language.tr("MainMenu.Data.LoadAgentsEfficiency"),Images.DATA_LOAD_AGENTS_EFFICIENCY.getIcon(),Language.tr("MainMenu.Data.LoadAgentsEfficiency.Mnemonic"),"DataAgentsEfficiency");
		createMenuItem(menu,Language.tr("MainMenu.Data.LoadAgentsAddition"),Images.DATA_LOAD_AGENTS_ADDITION.getIcon(),Language.tr("MainMenu.Data.LoadAgentsAddition.Mnemonic"),"DataAgentsAddition");
		createMenuItem(menu,Language.tr("MainMenu.Data.LoadHoldingTimes"),Images.DATA_LOAD_HOLDING_TIME.getIcon(),Language.tr("MainMenu.Data.LoadHoldingTimes.Mnemonic"),"DataBedienzeiten");
		menu.addSeparator();
		item=createMenuItem(menu,Language.tr("MainMenu.Data.SimplifyModel"),'\0',"DataSimplify");

		/* Simulation */
		menubar.add(menu=new JMenu(Language.tr("MainMenu.Simulation")));
		setMnemonic(menu,Language.tr("MainMenu.Simulation.Mnemonic"));

		createMenuItem(menu,Language.tr("MainMenu.Simulation.Check"),Images.SIMULATION_CHECK.getIcon(),Language.tr("MainMenu.Simulation.Check.Mnemonic"),"SimulationCheck");
		createMenuItem(menu,Language.tr("MainMenu.Simulation.Run"),Images.SIMULATION.getIcon(),Language.tr("MainMenu.Simulation.Run.Mnemonic"),KeyEvent.VK_F5,"SimulationRun");
		createMenuItemShift(menu,Language.tr("MainMenu.Simulation.RunAndSave"),Language.tr("MainMenu.Simulation.RunAndSave.Mnemonic"),KeyEvent.VK_F5,"SimulationRunAndSave");
		createMenuItem(menu,Language.tr("MainMenu.Simulation.RunAndSaveSetup"),Language.tr("MainMenu.Simulation.RunAndSaveSetup.Mnemonic"),"SimulationRunAndSaveSetup");
		menu.addSeparator();
		createMenuItem(menu,Language.tr("MainMenu.Simulation.BatchProcesscing"),Images.SIMULATION_BATCH.getIcon(),Language.tr("MainMenu.Simulation.BatchProcesscing.Mnemonic"),KeyEvent.VK_F7,"SimulationBatch");
		createMenuItem(menu,Language.tr("MainMenu.Simulation.Calibration"),Language.tr("MainMenu.Simulation.Calibration.Mnemonic"),"SimulationCalibrate");
		menu.addSeparator();
		createMenuItem(menu,Language.tr("MainMenu.Simulation.ConnectedSimulation"),Images.SIMULATION_CONNECTED.getIcon(),Language.tr("MainMenu.Simulation.ConnectedSimulation.Mnemonic"),KeyEvent.VK_F9,"SimulationConnected");
		createMenuItemCtrl(menu,Language.tr("MainMenu.Simulation.ConnectedSimulationResults"),Language.tr("MainMenu.Simulation.ConnectedSimulationResults.Mnemonic"),KeyEvent.VK_F9,"SimulationConnectedViewer");
		menu.addSeparator();
		createMenuItem(menu,Language.tr("MainMenu.Simulation.Preplanning"),Language.tr("MainMenu.Simulation.Preplanning.Mnemonic"),"SimulationPreplanning");
		createMenuItem(menu,Language.tr("MainMenu.Simulation.Optimizer"),Images.OPTIMIZER.getIcon(),Language.tr("MainMenu.Simulation.Optimizer.Mnemonic"),KeyEvent.VK_F8,"SimulationOptimize");
		createMenuItemCtrl(menu,Language.tr("MainMenu.Simulation.OptimizerResults"),Language.tr("MainMenu.Simulation.OptimizerResults.Mnemonic"),KeyEvent.VK_F8,"SimulationOptimizeViewer");
		createMenuItem(menu,Language.tr("MainMenu.Simulation.Rearrange"),Images.REARRANGE.getIcon(),'\0',"SimulationRearrange");
		createMenuItem(menu,Language.tr("MainMenu.Simulation.HeuristicRevenueOptimizer"),Images.REVENUSE_OPTIMIZER.getIcon(),'\0',"SimulationHeuristicRevenueOptimizer");

		/* Tools */
		menubar.add(menu=new JMenu(Language.tr("MainMenu.Tools")));
		setMnemonic(menu,Language.tr("MainMenu.Tools.Mnemonic"));

		createMenuItem(menu,Language.tr("MainMenu.Tools.Calculator"),Images.EXTRAS_CALCULATOR.getIcon(),Language.tr("MainMenu.Tools.Calculator.Mnemonic"),"ToolsRechner");
		createMenuItem(menu,Language.tr("MainMenu.Tools.UtilizationCalculator"),Language.tr("MainMenu.Tools.UtilizationCalculator.Mnemonic"),"ToolsAuslastungsrechner");
		createMenuItem(menu,Language.tr("MainMenu.Tools.SimpleSimulation"),Language.tr("MainMenu.Tools.SimpleSimulation.Mnemonic"),"ToolsSimpleSimulation");
		createMenuItem(menu,Language.tr("MainMenu.Tools.VarianceAnalysis"),Images.VARIANCE_ANALYSIS.getIcon(),Language.tr("MainMenu.Tools.VarianceAnalysis.Mnemonic"),"ToolsVarianzanalyse");
		createMenuItem(menu,Language.tr("MainMenu.Tools.FitDistribution"),Images.EXTRAS_FIT_DISTRIBUTION.getIcon(),Language.tr("MainMenu.Tools.FitDistribution.Mnemonic"),"ToolsFitting");
		createMenuItem(menu,Language.tr("MainMenu.Tools.ExecuteCommand"),Images.EXTRAS_COMMANDLINE.getIcon(),'\0',"ToolsExecuteCommand");
		createMenuItem(menu,Language.tr("MainMenu.Tools.LogRun"),Images.SIMULATION_LOG.getIcon(),Language.tr("MainMenu.Tools.LogRun.Mnemonic"),"ToolsLogRun");
		createMenuItemCtrl(menu,Language.tr("MainMenu.Tools.CreateReport"),Images.STATISTICS_REPORT.getIcon(),Language.tr("MainMenu.Tools.CreateReport.Mnemonic"),KeyEvent.VK_R,"ToolsReport");
		menu.addSeparator();
		createMenuItem(menu,Language.tr("MainMenu.Tools.CompareResults"),Images.MODEL_COMPARE.getIcon(),Language.tr("MainMenu.Tools.CompareResults.Mnemonic"),"ToolsCompare");
		createMenuItem(menu,Language.tr("MainMenu.Tools.KeepModel"),Images.MODEL_COMPARE_KEEP.getIcon(),'\0',"ToolsKeepModel");
		item=createMenuItem(menu,Language.tr("MainMenu.Tools.CompareKeptModel"),Images.MODEL_COMPARE_COMPARE.getIcon(),'\0',"ToolsCompareKeptModel");
		item.setEnabled(false); /* Erst freischalten, wenn ein Modell festgehalten wurde. */
		menuToolsCompareKeptModel.add(item);

		menu.addSeparator();
		createMenuItem(menu,Language.tr("MainMenu.Tools.RunServer"),Images.SERVER_CALC.getIcon(),Language.tr("MainMenu.Tools.RunServer.Mnemonic"),"ToolsServer");

		/* Hilfe */
		menubar.add(menu=new JMenu(Language.tr("MainMenu.Help")));
		setMnemonic(menu,Language.tr("MainMenu.Help.Mnemonic"));

		createMenuItem(menu,Language.tr("MainMenu.Help.Help"),Images.HELP.getIcon(),Language.tr("MainMenu.Help.Help.Mnemonic"),KeyEvent.VK_F1,"HelpHelp");
		createMenuItemShift(menu,Language.tr("MainMenu.Help.HelpContent"),Images.HELP_CONTENT.getIcon(),Language.tr("MainMenu.Help.HelpContent.Mnemonic"),KeyEvent.VK_F1,"HelpContent");
		createMenuItemCtrl(menu,Language.tr("MainMenu.Help.ShowModel"),Images.HELP_PDF.getIcon(),Language.tr("MainMenu.Help.ShowModel.Mnemonic"),KeyEvent.VK_F1,"HelpModel");
		createMenuItem(menu,Language.tr("MainMenu.Help.ShowGlossary"),Images.HELP_PDF.getIcon(),Language.tr("MainMenu.Help.ShowGlossary.Mnemonic"),"HelpGlossary");
		createMenuItem(menu,Language.tr("MainMenu.Help.Book"),Images.HELP_BOOK.getIcon(),Language.tr("MainMenu.Help.Book.Mnemonic"),"HelpBook");
		createMenuItem(menu,Language.tr("MainMenu.Help.SupportRequest"),Images.HELP_EMAIL.getIcon(),Language.tr("MainMenu.Help.SupportRequest.Mnemonic"),"HelpMail");
		createMenuItem(menu,Language.tr("MainMenu.Help.Homepage"),Images.HELP_HOMEPAGE.getIcon(),Language.tr("MainMenu.Help.Homepage.Mnemonic"),"HelpHomepage");
		createMenuItem(menu,Language.tr("Main.Menu.Help.Updates"),Images.SETUP_UPDATE_SEARCH.getIcon(),Language.tr("Main.Menu.Help.Updates.Mnemonic"),"HelpUpdates");
		createMenuItem(menu,Language.tr("MainMenu.Help.ShowWelcomePage"),Images.GENERAL_INFO.getIcon(),Language.tr("MainMenu.Help.ShowWelcomePage.Mnemonic"),"HelpWelcome");

		menu.addSeparator();

		createMenuItem(menu,Language.tr("Main.Menu.Help.LicenseInformation"),Language.tr("Main.Menu.Help.LicenseInformation.Mnemonic"),"HelpLicense");
		createMenuItem(menu,Language.tr("MainMenu.Help.ProgramInformation"),Language.tr("MainMenu.Help.ProgramInformation.Mnemonic"),"HelpInfo");

		return menubar;
	}

	/**
	 * Erstellt eine Kopie eines Men�eintrags (zur �bertragung aus dem normalen
	 * Men� in ein Popupmen� eines Ribbon-Men�punkts)
	 * @param original	Ausgangsmen�punkts
	 * @return	Kopierter Men�punkt
	 * @see #setDropDownMenu(JButton, JMenu)
	 */
	private JMenuItem cloneMenuItem(final JMenuItem original) {
		JMenuItem copy=new JMenuItem(original.getText(),original.getIcon());
		copy.setMnemonic(original.getMnemonic());
		copy.setAccelerator(original.getAccelerator());
		copy.setActionCommand(original.getActionCommand());
		copy.addActionListener(e-> {
			ActionEvent event=new ActionEvent(original,e.getID(),e.getActionCommand());
			for (ActionListener listener : original.getActionListeners()) listener.actionPerformed(event);
		});
		return copy;
	}

	/**
	 * F�gt ein Dropdown-Men� an eine Ribbon-Schaltfl�che an.
	 * @param button	Ribbon-Schaltfl�che
	 * @param menu	Ausgangsmen� das kopiert werden soll
	 */
	private void setDropDownMenu(final JButton button, final JMenu menu) {
		for (ActionListener listener : button.getActionListeners()) button.removeActionListener(listener);

		button.addActionListener(e-> {
			final JPopupMenu popup=new JPopupMenu();
			for (Component element : menu.getMenuComponents()) {
				if (element instanceof JPopupMenu.Separator) popup.addSeparator();
				if (element instanceof JMenuItem) popup.add(cloneMenuItem((JMenuItem)element));
			}
			popup.show(button,0,button.getHeight());
		});
	}

	/**
	 * Registriert einen Hotkey bei dessen Bet�tigung die {@link ActionListener}
	 * einer Schaltfl�che ausgel�st werden sollen.
	 * @param button	Schaltfl�che deren {@link ActionListener} ausgel�st werden sollen
	 * @param hotkey	Hotkey der die Ausf�hrung der Aktionen ausl�sen soll
	 */
	private void addHotkey(final AbstractButton button, KeyStroke hotkey) {
		if (hotkey==null && button!=null && button instanceof JMenuItem) {
			hotkey=((JMenuItem)button).getAccelerator();
		}
		if (hotkey==null) return;

		Action action=new AbstractAction() {
			/**
			 * Serialisierungs-ID der Klasse
			 * @see Serializable
			 */
			private static final long serialVersionUID = 2527640187493552160L;
			@Override
			public void actionPerformed(ActionEvent e) {
				if (workPanel!=null) return;
				Object obj=getValue("Sender");
				if (obj==null || !(obj instanceof AbstractButton)) return;
				AbstractButton button=(AbstractButton)obj;
				ActionEvent event=new ActionEvent(button,e.getID(),button.getActionCommand());
				for (ActionListener listener : button.getActionListeners()) listener.actionPerformed(event);
			}
		};
		action.putValue("Sender",button);

		getInputMap(JComponent.WHEN_IN_FOCUSED_WINDOW).put(hotkey,hotkey.toString());
		getActionMap().put(hotkey.toString(),action);

		if (button!=null && button instanceof JButton) {
			String hotkeyName=KeyEvent.getKeyText(hotkey.getKeyCode());
			String hotkeyStringModifiers=InputEvent.getModifiersExText(hotkey.getModifiers());
			if (hotkeyStringModifiers!=null && !hotkeyStringModifiers.isEmpty()) hotkeyName=hotkeyStringModifiers+"+"+hotkeyName;
			String text=button.getToolTipText();
			text=text+" ("+hotkeyName+")";
			button.setToolTipText(text);
		}
	}

	/**
	 * Registriert eine Aktion, dei beim Durcken eines Hotkeys ausgel�st werden soll.
	 * @param runnable	Auszul�sende Aktion
	 * @param hotkey	Hotkey der die Ausf�hrung der Aktion ausl�sen soll
	 */
	private void addHotkey(final Runnable runnable, KeyStroke hotkey) {
		Action action=new AbstractAction() {
			/**
			 * Serialisierungs-ID der Klasse
			 * @see Serializable
			 */
			private static final long serialVersionUID = 2527640187493552160L;
			@Override
			public void actionPerformed(ActionEvent e) {
				if (workPanel!=null) return;
				Object obj=getValue("Runnable");
				if (obj==null || !(obj instanceof Runnable)) return;
				((Runnable)obj).run();
			}
		};
		action.putValue("Runnable",runnable);

		getInputMap(JComponent.WHEN_IN_FOCUSED_WINDOW).put(hotkey,hotkey.toString());
		getActionMap().put(hotkey.toString(),action);
	}

	/**
	 * Erzeugt den "Modell"-Abschnitt in einem Ribbon-Toolbar-Tab
	 * @param tab	Ribbon-Toolbar-Tab an das der neue Abschnitt angeh�ngt werden soll
	 * @see #createRibbonBar()
	 */
	private void addRibbonSectionModel(final JRibbonTab tab) {
		JButton button;

		tab.addSection(Language.tr("Editor.GeneralData.Model"));

		if (menuRecentlyUsed==null) {
			menuRecentlyUsed=new JMenu(Language.tr("MainMenu.File.RecentlyUsedModels"));
			setMnemonic(menuRecentlyUsed,Language.tr("MainMenu.File.RecentlyUsedModels.Mnemonic"));
			updateLastFilesList();
		}

		button=tab.addBigButton(Language.tr("MainMenu.File.LoadModel.Short"),Language.tr("MainMenu.File.LoadModel.Tooltip"),Images.MODEL_LOAD_BIG.getIcon());
		setDropDownMenu(button,menuRecentlyUsed);

		tab.addRow();

		button=tab.addButton(Language.tr("MainMenu.File.NewModel.Short"),Language.tr("MainMenu.File.NewModel.Tooltip"),Images.MODEL_NEW.getIcon());
		JMenu sub=new JMenu();
		JMenuItem item;
		item=createMenuItemCtrl(sub,Language.tr("MainMenu.File.NewModel"),Images.MODEL_NEW.getIcon(),Language.tr("MainMenu.File.NewModel.EmptyModel.Mnemonic"),KeyEvent.VK_N,"FileNew");
		addHotkey(item,null);
		item=createMenuItemCtrlShift(sub,Language.tr("MainMenu.File.NewModel.CreateWithWizard"),Images.MODEL_WIZARD.getIcon(),Language.tr("MainMenu.File.NewModel.CreateWithWizard.Mnemonic"),KeyEvent.VK_N,"FileNewWizard");
		addHotkey(item,null);
		setDropDownMenu(button,sub);
		button=tab.addButton(Language.tr("MainMenu.File.SaveModel.Short"),Language.tr("MainMenu.File.SaveModel.Tooltip"),Images.MODEL_SAVE.getIcon());
		registerAction(button,"FileSave");
		addHotkey(button,KeyStroke.getKeyStroke(KeyEvent.VK_S,InputEvent.CTRL_DOWN_MASK));
		button=tab.addButton(Language.tr("MainMenu.File.SaveModelAs.Short"),Language.tr("MainMenu.File.SaveModelAs.Tooltip"),Images.MODEL_SAVE_PLUS.getIcon());
		registerAction(button,"FileSaveAs");
		addHotkey(button,KeyStroke.getKeyStroke(KeyEvent.VK_U,InputEvent.CTRL_DOWN_MASK));
	}

	/**
	 * Erzeugt den "Daten"-Abschnitt in einem Ribbon-Toolbar-Tab
	 * @param tab	Ribbon-Toolbar-Tab an das der neue Abschnitt angeh�ngt werden soll
	 * @param mode	0: Allgemeine Daten; 1: Anrufergruppen; 2: Callcenter und Agenten; 3: Callcenter und Agenten
	 * @see #createRibbonBar()
	 */
	private void addRibbonSectionData(final JRibbonTab tab, final int mode) {
		JButton button;
		JMenu menu;
		JMenuItem item;

		tab.addSection(Language.tr("MainMenu.Data"));

		button=tab.addButton(Language.tr("MainMenu.Data.LoadTable.Short"),Language.tr("MainMenu.Data.LoadTable.Tooltip"),Images.DATA_LOAD_BY_TEMPLATE.getIcon());
		menu=new JMenu();

		item=createMenuItemCtrl(menu,Language.tr("MainMenu.Data.LoadViaTemplate"),Images.DATA_LOAD_BY_TEMPLATE.getIcon(),'\0',KeyEvent.VK_I,"DataAll");
		item.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_I,InputEvent.CTRL_DOWN_MASK));
		addHotkey(item,null);

		if (mode>0) menu.addSeparator();

		if (mode==1) {
			createMenuItem(menu,Language.tr("MainMenu.Data.LoadCaller24"),Images.DATA_LOAD_CALLER.getIcon(),Language.tr("MainMenu.Data.LoadCaller24.Mnemonic"),"DataCaller24");
			createMenuItem(menu,Language.tr("MainMenu.Data.LoadCaller48"),Images.DATA_LOAD_CALLER.getIcon(),Language.tr("MainMenu.Data.LoadCaller48.Mnemonic"),"DataCaller48");
			createMenuItem(menu,Language.tr("MainMenu.Data.LoadCaller96"),Images.DATA_LOAD_CALLER.getIcon(),Language.tr("MainMenu.Data.LoadCaller96.Mnemonic"),"DataCaller96");
		}

		if (mode==2) {
			createMenuItem(menu,Language.tr("MainMenu.Data.LoadAgents24"),Images.DATA_LOAD_AGENTS.getIcon(),'\0',"DataAgents24");
			createMenuItem(menu,Language.tr("MainMenu.Data.LoadAgents48"),Images.DATA_LOAD_AGENTS.getIcon(),Language.tr("MainMenu.Data.LoadAgents.Mnemonic"),"DataAgents48");
			createMenuItem(menu,Language.tr("MainMenu.Data.LoadAgents96"),Images.DATA_LOAD_AGENTS.getIcon(),'\0',"DataAgents96");
			createMenuItem(menu,Language.tr("MainMenu.Data.LoadAgentsEfficiency"),Images.DATA_LOAD_AGENTS_EFFICIENCY.getIcon(),Language.tr("MainMenu.Data.LoadAgentsEfficiency.Mnemonic"),"DataAgentsEfficiency");
			createMenuItem(menu,Language.tr("MainMenu.Data.LoadAgentsAddition"),Images.DATA_LOAD_AGENTS_ADDITION.getIcon(),Language.tr("MainMenu.Data.LoadAgentsAddition.Mnemonic"),"DataAgentsAddition");
		}

		if (mode==3) {
			createMenuItem(menu,Language.tr("MainMenu.Data.LoadHoldingTimes"),Images.DATA_LOAD_HOLDING_TIME.getIcon(),Language.tr("MainMenu.Data.LoadHoldingTimes.Mnemonic"),"DataBedienzeiten");
		}

		setDropDownMenu(button,menu);

		button=tab.addButton(Language.tr("MainMenu.Data.LoadExampleData.Short"),Language.tr("MainMenu.Data.LoadExampleData.Tooltip"),Images.MODEL_LOAD.getIcon());
		registerAction(button,"DataSpecialLoader");
		button=tab.addButton(Language.tr("MainMenu.Data.SimplifyModel.Short"),Language.tr("MainMenu.Data.SimplifyModel.Tooltip"),Images.MODEL_SIMPLIFY.getIcon());
		registerAction(button,"DataSimplify");
	}

	/**
	 * Erzeugt den "Simulation"-Abschnitt in einem Ribbon-Toolbar-Tab
	 * @param tab	Ribbon-Toolbar-Tab an das der neue Abschnitt angeh�ngt werden soll
	 * @see #createRibbonBar()
	 */
	private void addRibbonSectionSimulation(final JRibbonTab tab) {
		JButton button;
		JMenu menu;

		tab.addSection(Language.tr("MainMenu.Simulation"));

		button=tab.addBigButton(Language.tr("MainMenu.Simulation.Run.Short"),Language.tr("MainMenu.Simulation.Run.Tooltip"),Images.SIMULATION_BIG.getIcon());
		addHotkey(button,KeyStroke.getKeyStroke(KeyEvent.VK_F5,0));
		registerAction(button,"SimulationRun");

		tab.addRow();

		button=tab.addButton(Language.tr("MainMenu.Simulation.Check"),Language.tr("MainMenu.Simulation.Check.Tooltip"),Images.SIMULATION_CHECK.getIcon());
		registerAction(button,"SimulationCheck");
		button=tab.addButton(Language.tr("MainMenu.Simulation.SimulationAndSave"),Language.tr("MainMenu.Simulation.SimulationAndSave.Tooltip"),Images.SIMULATION_AND_SAVE.getIcon());
		menu=new JMenu();
		addHotkey(createMenuItemShift(menu,Language.tr("MainMenu.Simulation.RunAndSave"),Language.tr("MainMenu.Simulation.RunAndSave.Mnemonic"),KeyEvent.VK_F5,"SimulationRunAndSave"),null);
		createMenuItem(menu,Language.tr("MainMenu.Simulation.RunAndSaveSetup"),Language.tr("MainMenu.Simulation.RunAndSaveSetup.Mnemonic"),"SimulationRunAndSaveSetup");
		menu.addSeparator();
		createMenuItem(menu,Language.tr("MainMenu.Tools.LogRun"),Images.SIMULATION_LOG.getIcon(),Language.tr("MainMenu.Tools.LogRun.Mnemonic"),"ToolsLogRun");
		setDropDownMenu(button,menu);

		button=tab.addButton(Language.tr("MainMenu.Simulation.Multiple"),Language.tr("MainMenu.Simulation.Multiple.Tooltip"),Images.SIMULATION_MULTI.getIcon());
		menu=new JMenu();
		addHotkey(createMenuItem(menu,Language.tr("MainMenu.Simulation.BatchProcesscing"),Images.SIMULATION_BATCH.getIcon(),Language.tr("MainMenu.Simulation.BatchProcesscing.Mnemonic"),KeyEvent.VK_F7,"SimulationBatch"),null);
		createMenuItem(menu,Language.tr("MainMenu.Simulation.Calibration"),Language.tr("MainMenu.Simulation.Calibration.Mnemonic"),"SimulationCalibrate");
		menu.addSeparator();
		addHotkey(createMenuItem(menu,Language.tr("MainMenu.Simulation.ConnectedSimulation"),Images.SIMULATION_CONNECTED.getIcon(),Language.tr("MainMenu.Simulation.ConnectedSimulation.Mnemonic"),KeyEvent.VK_F9,"SimulationConnected"),null);
		addHotkey(createMenuItemCtrl(menu,Language.tr("MainMenu.Simulation.ConnectedSimulationResults"),Language.tr("MainMenu.Simulation.ConnectedSimulationResults.Mnemonic"),KeyEvent.VK_F9,"SimulationConnectedViewer"),null);
		setDropDownMenu(button,menu);
	}

	/**
	 * Erzeugt den "Modelle vergleichen"-Abschnitt in einem Ribbon-Toolbar-Tab
	 * @param tab	Ribbon-Toolbar-Tab an das der neue Abschnitt angeh�ngt werden soll
	 * @see #createRibbonBar()
	 */
	private void addRibbonSectionCompare(final JRibbonTab tab) {
		JButton button;

		tab.addSection(Language.tr("MainMenu.Compare"));

		button=tab.addButton(Language.tr("MainMenu.Tools.CompareResults.Short"),Language.tr("MainMenu.Tools.CompareResults.Tooltip"),Images.MODEL_COMPARE.getIcon());
		registerAction(button,"ToolsCompare");
		button=tab.addButton(Language.tr("MainMenu.Tools.KeepModel.Short"),Language.tr("MainMenu.Tools.KeepModel.Tooltip"),Images.MODEL_COMPARE_KEEP.getIcon());
		registerAction(button,"ToolsKeepModel");
		button=tab.addButton(Language.tr("MainMenu.Tools.CompareKeptModel.Short"),Language.tr("MainMenu.Tools.CompareKeptModel.Tooltip"),Images.MODEL_COMPARE_COMPARE.getIcon());
		button.setEnabled(false); /* Erst freischalten, wenn ein Modell festgehalten wurde. */
		registerAction(button,"ToolsCompareKeptModel");
		menuToolsCompareKeptModel.add(button);
	}

	/**
	 * Erzeugt den "Optimierung"-Abschnitt in einem Ribbon-Toolbar-Tab
	 * @param tab	Ribbon-Toolbar-Tab an das der neue Abschnitt angeh�ngt werden soll
	 * @see #createRibbonBar()
	 */
	private void addRibbonSectionOptimization(final JRibbonTab tab) {
		JButton button;
		JMenu menu;

		tab.addSection(Language.tr("MainMenu.Optimization"));

		button=tab.addBigButton(Language.tr("MainMenu.Simulation.Optimizer"),Language.tr("MainMenu.Simulation.Optimizer.Tooltip"),Images.OPTIMIZER_BIG.getIcon());
		addHotkey(button,KeyStroke.getKeyStroke(KeyEvent.VK_F8,0));
		registerAction(button,"SimulationOptimize");

		tab.addRow();

		button=tab.addButton(Language.tr("MainMenu.Simulation.OptimizerResults.Short"),Language.tr("MainMenu.Simulation.OptimizerResults.Tooltip"),Images.STATISTICS.getIcon());
		addHotkey(button,KeyStroke.getKeyStroke(KeyEvent.VK_F8, InputEvent.CTRL_DOWN_MASK));
		registerAction(button,"SimulationOptimizeViewer");

		button=tab.addButton(Language.tr("MainMenu.Simulation.HeuristicRevenueOptimizer.Short"),Language.tr("MainMenu.Simulation.HeuristicRevenueOptimizer.Tooltip"),Images.REVENUSE_OPTIMIZER.getIcon());
		registerAction(button,"SimulationHeuristicRevenueOptimizer");

		button=tab.addButton(Language.tr("MainMenu.Simulation.MoreOptimization"),Language.tr("MainMenu.Simulation.MoreOptimization.Tooltip"),Images.OPTIMIZER.getIcon());
		menu=new JMenu();
		createMenuItem(menu,Language.tr("MainMenu.Simulation.Preplanning"),Language.tr("MainMenu.Simulation.Preplanning.Mnemonic"),"SimulationPreplanning");
		createMenuItem(menu,Language.tr("MainMenu.Simulation.Rearrange"),Images.REARRANGE.getIcon(),'\0',"SimulationRearrange");
		setDropDownMenu(button,menu);
	}

	/**
	 * Erzeugt den "Tools"-Abschnitt in einem Ribbon-Toolbar-Tab
	 * @param tab	Ribbon-Toolbar-Tab an das der neue Abschnitt angeh�ngt werden soll
	 * @see #createRibbonBar()
	 */
	private void addRibbonSectionTools(final JRibbonTab tab) {
		JButton button;
		JMenu menu;

		tab.addSection(Language.tr("MainMenu.Tools"));

		button=tab.addButton(Language.tr("MainMenu.Tools.Calculations"),Language.tr("MainMenu.Tools.Calculations.Tooltip"),Images.EXTRAS_CALCULATOR.getIcon());
		menu=new JMenu();
		createMenuItem(menu,Language.tr("MainMenu.Tools.Calculator"),Images.EXTRAS_CALCULATOR.getIcon(),Language.tr("MainMenu.Tools.Calculator.Mnemonic"),"ToolsRechner");
		createMenuItem(menu,Language.tr("MainMenu.Tools.UtilizationCalculator"),Language.tr("MainMenu.Tools.UtilizationCalculator.Mnemonic"),"ToolsAuslastungsrechner");
		createMenuItem(menu,Language.tr("MainMenu.Tools.SimpleSimulation"),Language.tr("MainMenu.Tools.SimpleSimulation.Mnemonic"),"ToolsSimpleSimulation");
		setDropDownMenu(button,menu);

		button=tab.addButton(Language.tr("MainMenu.Tools.VarianceAnalysis.Short"),Language.tr("MainMenu.Tools.VarianceAnalysis.Tooltip"),Images.VARIANCE_ANALYSIS.getIcon());
		registerAction(button,"ToolsVarianzanalyse");
		button=tab.addButton(Language.tr("MainMenu.Tools.FitDistribution.Short"),Language.tr("MainMenu.Tools.FitDistribution.Tooltip"),Images.EXTRAS_FIT_DISTRIBUTION.getIcon());
		registerAction(button,"ToolsFitting");
	}

	/**
	 * Erzeugt den "System"-Abschnitt in einem Ribbon-Toolbar-Tab
	 * @param tab	Ribbon-Toolbar-Tab an das der neue Abschnitt angeh�ngt werden soll
	 * @see #createRibbonBar()
	 */
	private void addRibbonSectionSystem(final JRibbonTab tab) {
		JButton button;

		tab.addSection(Language.tr("Editor.System"));

		button=tab.addBigButton(Language.tr("MainMenu.File.Setup.Short"),Language.tr("MainMenu.Setup.Tooltip"),Images.GENERAL_SETUP_BIG.getIcon());
		registerAction(button,"FileSetup");
		addHotkey(button,KeyStroke.getKeyStroke(KeyEvent.VK_P,InputEvent.CTRL_DOWN_MASK));

		tab.addRow();

		button=tab.addButton(Language.tr("MainMenu.Tools.ExecuteCommand.Short"),Language.tr("MainMenu.Tools.ExecuteCommand.Tooltip"),Images.EXTRAS_COMMANDLINE.getIcon());
		registerAction(button,"ToolsExecuteCommand");
		button=tab.addButton(Language.tr("MainMenu.Tools.RunServer.Short"),Language.tr("MainMenu.Tools.RunServer.Tooltip"),Images.SERVER_CALC.getIcon());
		registerAction(button,"ToolsServer");
		button=tab.addButton(Language.tr("MainMenu.Help.ProgramInformation.Short"),Language.tr("MainMenu.Help.ProgramInformation.Tooltip"),Images.GENERAL_INFO.getIcon());
		registerAction(button,"HelpInfo");
	}

	/**
	 * Erzeugt den "Statistik"-Abschnitt in einem Ribbon-Toolbar-Tab
	 * @param tab	Ribbon-Toolbar-Tab an das der neue Abschnitt angeh�ngt werden soll
	 * @see #createRibbonBar()
	 */
	private void addRibbonSectionStatistic(final JRibbonTab tab) {
		JButton button;

		tab.addSection(Language.tr("MainMenu.File.Statistic"));

		button=tab.addBigButton(Language.tr("MainToolbar.ShowModel.Short"),Language.tr("MainToolbar.ShowModel.Tooltip"),Images.MODEL_BIG.getIcon());
		registerAction(button,"SimulationShowModel");
		menuSimulationShowModel.add(button);

		tab.addRow();

		button=tab.addButton(Language.tr("MainMenu.File.LoadStatistics.Short"),Language.tr("MainMenu.File.LoadStatistics.Tooltip"),Images.STATISTICS_LOAD.getIcon());
		registerAction(button,"FileStatisticsLoad");
		addHotkey(button,KeyStroke.getKeyStroke(KeyEvent.VK_L,InputEvent.CTRL_DOWN_MASK + InputEvent.SHIFT_DOWN_MASK));
		button=tab.addButton(Language.tr("MainMenu.File.SaveStatistics.Short"),Language.tr("MainMenu.File.SaveStatistics.Tooltip"),Images.STATISTICS_SAVE.getIcon());
		registerAction(button,"FileStatisticsSave");
		menuSaveStatistics.add(button);
		addHotkey(button,KeyStroke.getKeyStroke(KeyEvent.VK_S,InputEvent.CTRL_DOWN_MASK + InputEvent.SHIFT_DOWN_MASK));
		/* menuToolsReport.add(button=tab.addButton(Language.tr("MainMenu.Tools.CreateReport.Short"),Language.tr("MainMenu.Tools.CreateReport.Tooltip"),Images.STATISTICS_REPORT.getIcon())); */
		/* addHotkey(button,KeyStroke.getKeyStroke(KeyEvent.VK_R, InputEvent.CTRL_DOWN_MASK)); */
		button=tab.addButton(Language.tr("MainToolbar.ShowWebViewer"),Language.tr("MainToolbar.ShowWebViewer.Tooltip"),Images.STATISTICS_SHOW_WEBVIEWER.getIcon());
		registerAction(button,"SimulationShowWebViewer");
		menuSimulationShowWebViewer.add(button);
	}

	/**
	 * Erzeugt den "Hilfe"-Abschnitt in einem Ribbon-Toolbar-Tab
	 * @param tab	Ribbon-Toolbar-Tab an das der neue Abschnitt angeh�ngt werden soll
	 * @see #createRibbonBar()
	 */
	private void addRibbonSectionHelp(final JRibbonTab tab) {
		JButton button;

		tab.addSection(Language.tr("MainMenu.Help"));

		button=tab.addBigButton(Language.tr("MainMenu.Help.Help"),Language.tr("MainMenu.Help.Help.Tooltip"),Images.HELP_BIG.getIcon());
		addHotkey(button,KeyStroke.getKeyStroke(KeyEvent.VK_F1,0));
		registerAction(button,"HelpHelp");

		tab.addRow();

		button=tab.addButton(Language.tr("MainMenu.Help.HelpContent.Short"),Language.tr("MainMenu.Help.HelpContent.Tooltip"),Images.HELP_CONTENT.getIcon());
		addHotkey(button,KeyStroke.getKeyStroke(KeyEvent.VK_F1, InputEvent.SHIFT_DOWN_MASK));
		registerAction(button,"HelpContent");

		button=tab.addButton(Language.tr("MainMenu.Help.ShowModel.Short"),Language.tr("MainMenu.Help.ShowModel.Tooltip"),Images.HELP_PDF.getIcon());
		addHotkey(button,KeyStroke.getKeyStroke(KeyEvent.VK_F1, InputEvent.CTRL_DOWN_MASK));
		registerAction(button,"HelpModel");

		button=tab.addButton(Language.tr("MainMenu.Help.ShowGlossary.Short"),Language.tr("MainMenu.Help.ShowGlossary.Tooltip"),Images.HELP_PDF.getIcon());
		registerAction(button,"HelpGlossary");

		tab.addRow();

		button=tab.addButton(Language.tr("MainMenu.Help.Book.Short"),Language.tr("MainMenu.Help.Book.Tooltip"),Images.HELP_BOOK.getIcon());
		registerAction(button,"HelpBook");

		button=tab.addButton(Language.tr("MainMenu.Help.Homepage"),Language.tr("MainMenu.Help.Homepage.Tooltip"),Images.HELP_HOMEPAGE.getIcon());
		registerAction(button,"HelpHomepage");

		button=tab.addButton(Language.tr("MainMenu.Help.Updates"),Language.tr("MainMenu.Help.Updates.Tooltip"),Images.SETUP_UPDATE_SEARCH.getIcon());
		registerAction(button,"HelpUpdates");
	}

	/**
	 * Erzeugt einen Ribbon-bassierten Toolbar
	 * @return	Neuer Toolbar
	 * @see #createToolBar()
	 */
	private JRibbonBar createRibbonBar() {
		JRibbonBar ribbonBar=new JRibbonBar(actionListener);
		JRibbonTab tab;

		/* Umschalter zwischen den Seiten (sonst per Men�-Hotkey verf�gbar) */
		addHotkey(()->setGUIMode((short)2),KeyStroke.getKeyStroke(KeyEvent.VK_F2,0));
		addHotkey(()->setGUIMode((short)0),KeyStroke.getKeyStroke(KeyEvent.VK_F3,0));
		addHotkey(()->setGUIMode((short)1),KeyStroke.getKeyStroke(KeyEvent.VK_F4,0));

		/* Willkommen */
		tab=ribbonBar.addRibbon(Language.tr("MainMenu.View.Welcome.Short"),Language.tr("MainMenu.View.Welcome.Short.Tooltip")+" (F2)",Images.GENERAL_INFO.getIcon());
		addRibbonSectionModel(tab);
		addRibbonSectionSimulation(tab);
		addRibbonSectionOptimization(tab);
		addRibbonSectionTools(tab);
		addRibbonSectionSystem(tab);
		addRibbonSectionHelp(tab);

		/* Allgemeine Daten */
		tab=ribbonBar.addRibbon(Language.tr("Editor.GeneralData"),Language.tr("Editor.GeneralData.Tooltip")+" (F3)",Images.EDITOR_GENERAL.getIcon());
		addRibbonSectionModel(tab);
		addRibbonSectionData(tab,0);
		addRibbonSectionSimulation(tab);
		addRibbonSectionCompare(tab);
		addRibbonSectionOptimization(tab);
		addRibbonSectionTools(tab);
		addRibbonSectionHelp(tab);

		/* Anrufergruppen */
		tab=ribbonBar.addRibbon(Language.tr("Editor.CallerGroups"),Language.tr("Editor.CallerGroups.Tooltips"),Images.EDITOR_CALLER.getIcon());
		addRibbonSectionModel(tab);
		addRibbonSectionData(tab,1);
		addRibbonSectionSimulation(tab);
		addRibbonSectionCompare(tab);
		addRibbonSectionOptimization(tab);
		addRibbonSectionTools(tab);
		addRibbonSectionHelp(tab);

		/* Callcenter und Agenten */
		tab=ribbonBar.addRibbon(Language.tr("Editor.CallcenterAndAgents"),Language.tr("Editor.CallcenterAndAgents.Tooltips"),Images.EDITOR_CALLCENTER.getIcon());
		addRibbonSectionModel(tab);
		addRibbonSectionData(tab,2);
		addRibbonSectionSimulation(tab);
		addRibbonSectionCompare(tab);
		addRibbonSectionOptimization(tab);
		addRibbonSectionTools(tab);
		addRibbonSectionHelp(tab);

		/* Callcenter und Agenten */
		tab=ribbonBar.addRibbon(Language.tr("Editor.SkillLevelOfTheAgents"),Language.tr("Editor.SkillLevelOfTheAgents.Tooltips"),Images.EDITOR_SKILLLEVEL.getIcon());
		addRibbonSectionModel(tab);
		addRibbonSectionData(tab,3);
		addRibbonSectionSimulation(tab);
		addRibbonSectionCompare(tab);
		addRibbonSectionOptimization(tab);
		addRibbonSectionTools(tab);
		addRibbonSectionHelp(tab);

		/* Modell�berblick */
		tab=ribbonBar.addRibbon(Language.tr("Editor.ModelOverview"),Language.tr("Editor.ModelOverview.Tooltips"),Images.EDITOR_MODELINFO.getIcon());
		addRibbonSectionModel(tab);
		addRibbonSectionData(tab,0);
		addRibbonSectionSimulation(tab);
		addRibbonSectionCompare(tab);
		addRibbonSectionOptimization(tab);
		addRibbonSectionTools(tab);
		addRibbonSectionHelp(tab);

		/* Simulationsergebnisse */
		tab=ribbonBar.addRibbon(Language.tr("MainMenu.View.Statistics"),Language.tr("MainMenu.View.Statistics.Tooltip")+" (F4)",Images.STATISTICS.getIcon());
		addRibbonSectionStatistic(tab);
		addRibbonSectionOptimization(tab);
		addRibbonSectionTools(tab);
		addRibbonSectionHelp(tab);

		return ribbonBar;
	}

	/**
	 * Aktualisiert die Liste der zuletzt verwendeten Dateien im Men�.
	 * @see #menuRecentlyUsed
	 */
	private void updateLastFilesList() {
		SetupData setup=SetupData.getSetup();

		menuRecentlyUsed.removeAll();

		if (SetupData.getSetup().ribbonMode) {
			JMenuItem sub=createMenuItemCtrl(menuRecentlyUsed,Language.tr("MainMenu.File.LoadModel"),Images.MODEL_LOAD.getIcon(),Language.tr("MainMenu.File.LoadModel.Mnemonic"),KeyEvent.VK_L,"FileLoad");
			addHotkey(sub,null);
			menuRecentlyUsed.add(sub);
			menuRecentlyUsed.addSeparator();
		}

		menuRecentlyUsed.setEnabled(setup.lastFiles!=null && setup.lastFiles.length>0);
		if (!menuRecentlyUsed.isEnabled()) return;

		for (int i=0; i<setup.lastFiles.length;i++) {
			final JMenuItem sub=new JMenuItem(setup.lastFiles[i]);
			sub.addActionListener(actionListener);
			menuRecentlyUsed.add(sub);
		}

		if (setup.lastFiles.length>0) {
			menuRecentlyUsed.addSeparator();
			final JMenuItem sub=new JMenuItem(Language.tr("Main.Menu.File.RecentlyUsed.Delete"));
			sub.setIcon(Images.GENERAL_OFF.getIcon());
			sub.addActionListener(e->{
				setup.lastFiles=new String[0];
				setup.saveSetup();
				updateLastFilesList();
			});
			menuRecentlyUsed.add(sub);
		}
	}

	/**
	 * F�gt einen Eintrag zu der Liste der zuletzt verwendeten Dateien hinzu,
	 * speichert das Setup und baut das Men� entsprechend neu auf.
	 * @param fileName	Dateiname, der zu der Liste hinzugef�gt werden soll (wenn er nicht bereits enthalten ist)
	 */
	private void addFileToRecentlyUsedList(String fileName) {
		SetupData setup=SetupData.getSetup();
		List<String> files=(setup.lastFiles==null)?new ArrayList<>():new ArrayList<>(Arrays.asList(setup.lastFiles));

		int index=files.indexOf(fileName);
		if (index==0) return; /* Eintrag ist bereits ganz oben in der Liste, nichts zu tun */
		if (index>0) files.remove(index); /* Wenn schon in Liste: Element an alter Position entfernen */
		files.add(0,fileName); /* Element ganz vorne einf�gen */
		while (files.size()>5) files.remove(files.size()-1); /* Maximal die letzten 5 Dateien merken */

		setup.lastFiles=files.toArray(new String[0]);
		setup.saveSetup(); /* saveSetupWithWarning(this) - nein, hierf�r keine Warnung */

		updateLastFilesList();
	}

	/**
	 * Liefert ein neues Beispielmodell
	 * @param exampleNr	Nummer des Beispielmodells
	 * @return	Neues Beispielmodell
	 */
	private CallcenterModel getNewModel(int exampleNr) {
		switch (exampleNr) {
		case 0: return CallcenterModelExamples.getExampleSmall();
		case 1: return CallcenterModelExamples.getExampleMedium();
		case 2: return CallcenterModelExamples.getExampleLarge();
		case 3: return CallcenterModelExamples.getExampleExtraLarge();
		case 4: return CallcenterModelExamples.getExampleSmallErlang();
		case 5: return CallcenterModelExamples.getExampleMediumErlang();
		case 6: return CallcenterModelExamples.getExampleLargeErlang();
		default: return new CallcenterModel();
		}
	}

	/**
	 * Darf das aktuelle Modell verworfen werden?
	 * @return	Liefert <code>true</code>, wenn das aktuell im Editor befindliche Modell verworfen werden darf
	 */
	private boolean discardModelOk() {
		int i=MsgBox.confirmSave(this,Language.tr("Window.DiscardConfirmation.Title"),Language.tr("Window.DiscardConfirmation.Info"));
		if (i==JOptionPane.YES_OPTION) {
			if (!commandFileModelSave()) return false;
			if (!modelPanel.isModelChanged()) return true;
			return discardModelOk();
		}
		if (i==JOptionPane.NO_OPTION) return true;
		return false;
	}

	/**
	 * L�dt das Modell aus dem angegebenen root-Element.
	 * @param root	XML-Root-Element
	 * @param fileName	Dateiname der Quelldatei
	 * @return Gibt <code>null</code> zur�ck, wenn das Modell erfolgreich geladen wurde. Andernfalls wird die Fehlermeldung als String zur�ckgegeben.
	 */
	private boolean loadModel(final Element root, final String fileName) {
		if (modelPanel.isModelChanged()) {if (!discardModelOk()) return false;}

		String s=modelPanel.loadModel(root,fileName);
		if (modelPanel.getLastFileName().isEmpty()) setAdditionalTitle(UNSAVED_MODEL); else setAdditionalTitle(modelPanel.getLastFileName());
		if (s!=null) MsgBox.error(this,Language.tr("Window.LoadModelError.Title"),s); else {statisticPanel.setStatistic(null); setGUIMode((short)0);}

		if (s==null && !modelPanel.getLastFileName().isEmpty()) addFileToRecentlyUsedList(modelPanel.getLastFileName());

		return (s==null);
	}

	/**
	 * L�dt ein Modell aus einer angegebenen Modelldatei.
	 * @param fileName	Dateiname der Modelldatei
	 * @return	Gibt <code>true</code> zur�ck, wenn das Modell erfolgreich geladen wurde.
	 */
	private boolean loadModelFileAsString(final String fileName) {
		final File file=new File(fileName);
		if (!file.exists()) {
			String s=String.format(Language.tr("Window.LoadModelError.FileNotFound"),fileName);
			MsgBox.error(this,Language.tr("Window.LoadModelError.Title"),s);
			return false;
		}

		return commandFileModelLoad(file);
	}

	/**
	 * L�dt Statistikdaten aus einem angegebenen root-Element.
	 * @param root	XML-Root-Element
	 * @return Gibt <code>true</code> zur�ck, wenn die Statistikdaten erfolgreich geladen wurde.
	 */
	private boolean loadStatisticsElement(final Element root) {
		String s=statisticPanel.loadStatistic(root);
		if (s!=null) MsgBox.error(this,Language.tr("Window.LoadStatisticsError.Title"),s); else setGUIMode((short)1);
		return (s==null);
	}

	@Override
	public boolean loadAnyFile(File file, Component dropComponent, Point dropPosition, boolean errorMessageOnFail) {
		if (file==null) {
			if (errorMessageOnFail) MsgBox.error(this,Language.tr("XML.LoadErrorTitle"),Language.tr("XML.NoFileSelected"));
			return false;
		}
		if (!file.exists()) {
			if (errorMessageOnFail) MsgBox.error(this,Language.tr("XML.LoadErrorTitle"),String.format(Language.tr("XML.FileNotFound"),file.toString()));
			return false;
		}
		if (workPanel!=null) return workPanel.dragDropLoad(file);

		XMLTools xml=new XMLTools(file);
		Element root=xml.load();
		if (root==null) {
			if (errorMessageOnFail) MsgBox.error(this,Language.tr("XML.LoadErrorTitle"),xml.getError());
			return false;
		}

		final String name=root.getNodeName();
		for (String s : CallcenterModel.XMLBaseElement) if (name.equalsIgnoreCase(s)) return loadModel(root,file.toString());
		for (String s : Statistics.XMLBaseElement) if (name.equalsIgnoreCase(s)) return loadStatisticsElement(root);
		for (String s : OptimizeSetup.XMLBaseElement) if (name.equalsIgnoreCase(s)) {commandSimulationOptimize(file); return true;}
		for (String s : OptimizeData.XMLBaseElement) if (name.equalsIgnoreCase(s)) return showOptimizeViewer(file);
		for (String s : ConnectedModel.XMLBaseElement) if (name.equalsIgnoreCase(s)) {commandSimulationConnected(file); return true;}
		for (String s : ImporterData.XMLBaseElement) if (name.equalsIgnoreCase(s)) {commandDataGenerator(7,-1,file); return true;}

		if (errorMessageOnFail) MsgBox.error(this,Language.tr("XML.LoadErrorTitle"),String.format(Language.tr("XML.ErrorProcessingFile"),file.toString()));
		return false;
	}

	/**
	 * Stellt ein Modell f�r den Modell-Editor ein
	 * @param model	Neues Modell
	 * @return	Liefert <code>true</code>, wenn das neue Modell eingestellt werden konnte (d.h. wenn das alte Modell verworfen werden konnte)
	 */
	private boolean setModel(final CallcenterModel model) {
		if (modelPanel.isModelChanged()) {if (!discardModelOk()) return false;}

		modelPanel.setModel(model);
		if (modelPanel.getLastFileName().isEmpty()) setAdditionalTitle(UNSAVED_MODEL); else setAdditionalTitle(modelPanel.getLastFileName());

		statisticPanel.setStatistic(null);
		setGUIMode((short)0);
		return true;
	}

	/**
	 * Befehl: Datei - Neues Modell
	 */
	private void commandFileModelNewFromTemplate() {
		if (modelPanel.isModelChanged()) {if (!discardModelOk()) return;}

		NewModelDialog dialog=new NewModelDialog(ownerWindow,helpLink);
		dialog.setVisible(true);
		if (dialog.getClosedBy()!=BaseEditDialog.CLOSED_BY_OK) return;
		CallcenterModel model=dialog.getModel();
		if (model==null) {
			NewModelWizard wizard=new NewModelWizard(ownerWindow,helpLink);
			wizard.setVisible(true);
			model=wizard.getModel();
		}

		if (model==null) return;
		modelPanel.setModel(model);
		setAdditionalTitle(UNSAVED_MODEL);
		statisticPanel.setStatistic(null);
		setGUIMode((short)0);
	}

	/**
	 * Befehl: Datei - Neues Modell mit Assistent anlegen
	 */
	private void commandFileModelNewFromWizard() {
		if (modelPanel.isModelChanged()) {if (!discardModelOk()) return;}

		NewModelWizard wizard=new NewModelWizard(ownerWindow,helpLink);
		wizard.setVisible(true);
		CallcenterModel model=wizard.getModel();

		if (model==null) return;
		modelPanel.setModel(model);
		setAdditionalTitle(UNSAVED_MODEL);
		statisticPanel.setStatistic(null);
		setGUIMode((short)0);
	}

	/**
	 * Befehl: Datei - Laden
	 * @param file	Zu ladende Datei; wird <code>null</code> �bergeben, so wird ein Dateiauswahldialog angezeigt
	 * @return	Liefert <code>true</code>, wenn eine Datei geladen wurde
	 */
	private boolean commandFileModelLoad(File file) {
		if (modelPanel.isModelChanged()) {if (!discardModelOk()) return false;}

		String s=modelPanel.loadModel(file);
		if (modelPanel.getLastFileName().isEmpty()) setAdditionalTitle(UNSAVED_MODEL); else setAdditionalTitle(modelPanel.getLastFileName());
		if (s!=null) MsgBox.error(this,Language.tr("Window.LoadModelError.Title"),s); else {statisticPanel.setStatistic(null); setGUIMode((short)0);}

		if (s==null && !modelPanel.getLastFileName().isEmpty()) addFileToRecentlyUsedList(modelPanel.getLastFileName());

		return (s==null);
	}

	/**
	 * Befehl: Datei - Speichern
	 * @return	Liefert <code>true</code>, wenn das Modell gespeichert wurde
	 */
	private boolean commandFileModelSave() {
		if (modelPanel.getLastFileName().isEmpty()) return commandFileModelSaveAs();
		String s=modelPanel.saveModel(new File(modelPanel.getLastFileName()));
		if (modelPanel.getLastFileName().isEmpty()) setAdditionalTitle(UNSAVED_MODEL); else setAdditionalTitle(modelPanel.getLastFileName());
		if (s!=null) MsgBox.error(this,Language.tr("Window.SaveModelError.Title"),s);

		if (s==null && !modelPanel.getLastFileName().isEmpty()) addFileToRecentlyUsedList(modelPanel.getLastFileName());

		return (s==null);
	}

	/**
	 * Befehl: Datei - Speichern unter
	 * @return	Liefert <code>true</code>, wenn das Modell gespeichert wurde
	 */
	private boolean commandFileModelSaveAs() {
		String s=modelPanel.saveModel(null);
		if (modelPanel.getLastFileName().isEmpty()) setAdditionalTitle(UNSAVED_MODEL); else setAdditionalTitle(modelPanel.getLastFileName());
		if (s!=null) MsgBox.error(this,Language.tr("Window.SaveModelError.Title"),s);

		if (s==null && !modelPanel.getLastFileName().isEmpty()) addFileToRecentlyUsedList(modelPanel.getLastFileName());

		return (s==null);
	}

	/**
	 * Befehl: Datei - Statistik laden
	 * @param file	Zu ladende Datei; wird <code>null</code> �bergeben, so wird ein Dateiauswahldialog angezeigt
	 * @return	Liefert <code>true</code>, wenn eine Datei geladen wurde
	 */
	private boolean commandFileStatisticsLoad(File file) {
		String s=statisticPanel.loadStatistic(file);
		if (s!=null) MsgBox.error(this,Language.tr("Window.LoadStatisticsError.Title"),s); else setGUIMode((short)1);
		return (s==null);
	}

	/**
	 * Befehl: Datei - Statistik speichern unter
	 */
	private void commandFileStatisticsSave() {
		Statistics[] statistic=statisticPanel.getStatistic();
		if (statistic==null || statistic.length==0 || statistic[0]==null) {MsgBox.error(this,Language.tr("Window.SaveStatisticsError.Title"),Language.tr("Window.SaveStatisticsError.NoSimulationResults")); return;}
		String s=statisticPanel.saveStatistic(null);
		if (s!=null) MsgBox.error(this,Language.tr("Window.SaveStatisticsError.Title"),s);
	}

	/**
	 * Befehl: Datei - Einstellungen
	 * @param showUpdatesPage	Initial die Updates-Seite anzeigen? (Statt der "Benutzeroberfl�che"-Seite)
	 */
	private void commandFileSetup(final boolean showUpdatesPage) {
		SetupData setup=SetupData.getSetup();

		String currentLanguage=setup.language;
		boolean currentRibbonMode=setup.ribbonMode;
		equalsExampleModel=CallcenterModelExamples.equalsExampleModel(modelPanel.getModel(false));

		SetupDialog dialog=new SetupDialog(ownerWindow,helpLink,showUpdatesPage);
		dialog.setVisible(true);

		backgroundSimulator.loadSetup();

		if (!setup.language.equals(currentLanguage) || setup.ribbonMode!=currentRibbonMode) {
			SetupData.getSetup().resetLanguageWasAutomatically();
			HelpBase.hideHelpFrame();
			StatisticsBasePanel.viewerPrograms.clear();
			if (setup.openWord) StatisticsBasePanel.viewerPrograms.add(StatisticsBasePanel.ViewerPrograms.WORD);
			if (setup.openODT) StatisticsBasePanel.viewerPrograms.add(StatisticsBasePanel.ViewerPrograms.ODT);
			if (setup.openExcel) StatisticsBasePanel.viewerPrograms.add(StatisticsBasePanel.ViewerPrograms.EXCEL);
			if (setup.openODS) StatisticsBasePanel.viewerPrograms.add(StatisticsBasePanel.ViewerPrograms.ODS);
			if (setup.openPDF) StatisticsBasePanel.viewerPrograms.add(StatisticsBasePanel.ViewerPrograms.PDF);
			if (reloadWindow!=null) SwingUtilities.invokeLater(reloadWindow);
		}
	}

	/**
	 * Generatordialog f�r Anrufer, Agenten oder Skills aufrufen
	 * @param editType	0,1,2=Anrufer (24/48/49 Intervalle), 3,4,5=Agenten  (24/48/49 Intervalle), 6=Skills, 7=Schablonen-Import, 8=Agenten-Effizient, 9=Agenten-Verf�gbarkeit
	 * @param selectCallcenterNr	Wird hier eine Zahl >=0 �bergeben, so wird die entsprechende Callcenternummer im Falle editType=1 verwendet, sonst wird das aktuell gew�hlte Callcenter verwendet.
	 * @param file	Datei f�r Schablonen-Import
	 */
	private void commandDataGenerator(int editType, int selectCallcenterNr, File file) {
		CallcenterModel model=modelPanel.getModel(true);
		GeneratorBaseDialog dialog;
		switch (editType) {
		case 0: dialog=new CallerGeneratorDialog(ownerWindow,helpLink.dialogGeneratorClients,model,24); break;
		case 1: dialog=new CallerGeneratorDialog(ownerWindow,helpLink.dialogGeneratorClients,model,48); break;
		case 2: dialog=new CallerGeneratorDialog(ownerWindow,helpLink.dialogGeneratorClients,model,96); break;
		case 3: if (selectCallcenterNr==-1) selectCallcenterNr=(modelPanel.getSelectedTabIndex()==2)?modelPanel.getSelectedCallcenterIndex():-1; dialog=new AgentsGeneratorDialog(ownerWindow,helpLink.dialogGeneratorAgents,model,selectCallcenterNr,AgentsGeneratorDialog.AgentsGeneratorMode.AGENTS_GENERATOR_MODE_WORKING,24); break;
		case 4: if (selectCallcenterNr==-1) selectCallcenterNr=(modelPanel.getSelectedTabIndex()==2)?modelPanel.getSelectedCallcenterIndex():-1; dialog=new AgentsGeneratorDialog(ownerWindow,helpLink.dialogGeneratorAgents,model,selectCallcenterNr,AgentsGeneratorDialog.AgentsGeneratorMode.AGENTS_GENERATOR_MODE_WORKING,48); break;
		case 5: if (selectCallcenterNr==-1) selectCallcenterNr=(modelPanel.getSelectedTabIndex()==2)?modelPanel.getSelectedCallcenterIndex():-1; dialog=new AgentsGeneratorDialog(ownerWindow,helpLink.dialogGeneratorAgents,model,selectCallcenterNr,AgentsGeneratorDialog.AgentsGeneratorMode.AGENTS_GENERATOR_MODE_WORKING,96); break;
		case 6: dialog=new SkillsGeneratorDialog(ownerWindow,helpLink.dialogGeneratorSkillLevels,model); break;
		case 7: setGUIState(new ImporterPanel(ownerWindow,model,file,new SimDoneNotify(),helpLink)); return;
		case 8: if (selectCallcenterNr==-1) selectCallcenterNr=(modelPanel.getSelectedTabIndex()==2)?modelPanel.getSelectedCallcenterIndex():-1; dialog=new AgentsGeneratorDialog(ownerWindow,helpLink.dialogGeneratorAgentsEfficiency,model,selectCallcenterNr,AgentsGeneratorDialog.AgentsGeneratorMode.AGENTS_GENERATOR_MODE_EFFICIENCY,48); break;
		case 9: if (selectCallcenterNr==-1) selectCallcenterNr=(modelPanel.getSelectedTabIndex()==2)?modelPanel.getSelectedCallcenterIndex():-1; dialog=new AgentsGeneratorDialog(ownerWindow,helpLink.dialogGeneratorAgentsAddition,model,selectCallcenterNr,AgentsGeneratorDialog.AgentsGeneratorMode.AGENTS_GENERATOR_MODE_ADDITION,48); break;
		default: dialog=null; break;
		}

		if (dialog==null) return;
		dialog.setVisible(true);
		model=dialog.getModel();
		if (model!=null) {modelPanel.setModel(model); modelPanel.setModelChanged(true);}
	}

	/**
	 * Befehl: Daten - Modell vereinfachen
	 */
	private void commandDataSimplify() {
		if (modelPanel.isModelChanged()) {if (!discardModelOk()) return;}
		if (!MsgBox.confirm(this,Language.tr("Shrink.Title"),Language.tr("Shrink.Info"),Language.tr("Shrink.Yes.Info"),Language.tr("Shrink.No.Info"))) return;
		backgroundSimulator.stop(true);

		ModelShrinker shrinker=new ModelShrinker(modelPanel.getModel(false));
		CallcenterModel newModel=shrinker.calc(false);

		if (newModel!=null) {
			modelPanel.setModel(newModel);
		}
	}

	/**
	 * Befehl: Daten - Daten gem�� vorgegenem Format verarbeiten
	 */
	private void commandDataSpecialLoader() {
		SpecialProcessingDialog dialog=new SpecialProcessingDialog(ownerWindow,helpLink);
		dialog.setVisible(true);
		if (dialog.getClosedBy()==BaseEditDialog.CLOSED_BY_OK && dialog.getModel()!=null) setModel(dialog.getModel());
	}

	/**
	 * Befehl: Simulation - Modellpr�fung
	 */
	private void commandSimulationCheck() {
		final String results=CallcenterRunModel.check(modelPanel.getModel(true));
		MsgBox.info(this,Language.tr("Model.Plausibility.ResultsTitle"),results);
	}

	/**
	 * Befehl: Simulation - Simulation starten
	 * @param autoSaveResults	Nach der Simulation Ergebnisse sofort speichern?
	 * @param logFile	Optionale Logging-Ausgabedatei
	 */
	private void commandSimulationRun(final boolean autoSaveResults, File logFile) {
		saveAfterSim=autoSaveResults;

		CallcenterModel editModel=modelPanel.getModel(true);
		if (logFile!=null) {backgroundSimulator.stop(true); editModel.days=Math.min(editModel.days,2);}

		/* eigentlich �berfl�ssig und nur bremsend: statisticPanel.setStatistic(null);*/

		CallcenterSimulatorInterface simulator=backgroundSimulator.getSimulatorForModel(editModel);
		if (simulator==null) {
			StartAnySimulator startAnySimulator=new StartAnySimulator(editModel,logFile);
			CallcenterRunModel runModel=backgroundSimulator.getRunModelforModel(editModel);
			String s=startAnySimulator.check(runModel);
			if (s!=null) {
				MsgBox.error(this,Language.tr("Window.ErrorStartingSimulation.Title"),s);
				return;
			}
			simulator=startAnySimulator.run();
		}

		if (simulator.isRunning()) {
			setGUIState(new CallcenterRunPanel(simulator,new SimDoneNotify(),autoSaveResults,-1));
		} else {
			new SimDoneNotify().simDone(true,logFile,simulator.collectStatistic());
		}
	}

	/**
	 * Befehl: Simulation - Simulation starten und Ergebnisse speichern
	 */
	private void commandSimulationRunAndSaveSetup() {
		AutoSaveSetupDialog dialog=new AutoSaveSetupDialog(ownerWindow,helpLink.dialogAutoSave);
		dialog.setVisible(true);
	}

	/**
	 * Befehl: Simulation - Stapelverarbeitung
	 */
	private void commandSimulationBatch() {
		backgroundSimulator.stop(true);
		setGUIState(new BatchPanel(ownerWindow,modelPanel.getModel(true),new SimDoneNotify(),helpLink));
	}

	/**
	 * Befehl: Simulation - Wartezeittoleranzkalibrierung
	 */
	private void commandSimulationCalibrate() {
		backgroundSimulator.stop(true);
		setGUIState(new CalibratePanel(modelPanel.getModel(true),new SimDoneNotify(),helpLink));
	}

	/**
	 * Befehl: Simulation - Mehrt�gige Simulation
	 * @param loadFile	 Zu ladende Verkettete-XML-Datei. (Wird <code>null</code> �bergeben, so wird initial kein Modell geladen.)
	 */
	private void commandSimulationConnected(File loadFile) {
		backgroundSimulator.stop(true);
		setGUIState(new ConnectedPanel(ownerWindow,loadFile,new SimDoneNotify(),helpLink));
	}

	/**
	 * Befehl: Simulation - Ergebnisse einer mehrt�gigen Simulation anzeigen
	 */
	private void commandSimulationConnectedViewer() {
		File file=XMLTools.showLoadDialog(this,Language.tr("Window.ConnectedResults.Title"));
		if (file==null) return;
		showConnectedViewer(file);
	}

	/**
	 * Befehl: Simulation - Agenten-Vorplanung
	 */
	private void commandSimulationPreplanning() {
		if (modelPanel.isModelChanged()) {if (!discardModelOk()) return;}
		backgroundSimulator.stop(true);
		PreplanningDialog dialog=new PreplanningDialog(ownerWindow,modelPanel.getModel(false),helpLink);
		dialog.setVisible(true);
		CallcenterModel newModel=dialog.getResultModel();
		if (newModel!=null) {
			modelPanel.setModel(newModel);
		}
	}

	/**
	 * Befehl: Simulation - Optimierer
	 * @param file	Initial zu ladende Optimierungseinstellungendatei (kann <code>null</code> sein)
	 */
	private void commandSimulationOptimize(final File file) {
		CallcenterModel editModel=modelPanel.getModel(true);

		CallcenterRunModel runModel=new CallcenterRunModel(editModel);
		String s=runModel.checkAndInit(false,false,SetupData.getSetup().strictCheck);
		if (s!=null) {
			MsgBox.error(this,Language.tr("Window.ErrorStartingSimulation.Title"),s);
			return;
		}

		backgroundSimulator.stop(true);

		setGUIState(new OptimizePanel(ownerWindow,new SimDoneNotify(),editModel,file,helpLink));
	}

	/**
	 * Befehl: Simulation - Optimierer-Ergebnis anzeigen
	 */
	private void commandSimulationOptimizeViewer() {
		File file=XMLTools.showLoadDialog(this,Language.tr("Window.OptimizerResults.Titel"));
		if (file==null) return;
		showOptimizeViewer(file);
	}

	/**
	 * Befehl: Simulation - Anrufer oder Agenten verlagern
	 */
	private void commandSimulationRearrange() {
		CallcenterModel editModel=modelPanel.getModel(true);
		Rearranger rearranger=new Rearranger(editModel);
		if (rearranger.modelError!=null) {
			MsgBox.error(this,Language.tr("Window.ErrorStartingSimulation.Title"),rearranger.modelError);
			return;
		}

		backgroundSimulator.stop(true);
		setGUIState(new RearrangePanel(ownerWindow,new SimDoneNotify(),editModel,helpLink));
	}

	/**
	 * Befehl: Simulation - Heuristische Ertragsverbesserung
	 */
	private void commandSimulationRevenueOptimizer() {
		CallcenterModel editModel=modelPanel.getModel(true);

		RevenueOptimizer revenueOptimizer=new RevenueOptimizer(editModel);
		if (!revenueOptimizer.check()) {
			MsgBox.error(this,Language.tr("Window.ErrorStartingSimulation.Title"),revenueOptimizer.getError());
			return;
		}

		if (modelPanel.isModelChanged()) {if (!discardModelOk()) return;}
		backgroundSimulator.stop(true);

		setGUIState(new RevenueOptimizerPanel(ownerWindow,new SimDoneNotify(),helpLink,editModel));
	}

	/**
	 * Befehl: (Toolbar bei Statistikansicht) - Modell zu diesen Ergebnissen
	 */
	private void commandSimulationShowModel() {
		CallcenterModel editModel=null;
		if (statisticPanel.getStatistic()!=null && statisticPanel.getStatistic().length>0 && statisticPanel.getStatistic()[0]!=null) editModel=statisticPanel.getStatistic()[0].editModel;
		if (editModel==null) {
			MsgBox.error(this,Language.tr("Window.NoModelInformationAvailable.Title"),Language.tr("Window.NoModelInformationAvailable.Info"));
			return;
		}
		CallcenterModelEditorPanelDialog modelViewer=new CallcenterModelEditorPanelDialog(ownerWindow,editModel,null,true,helpLink);
		modelViewer.setCloseNotify(new ModelViewerClosed(modelViewer));
		JWorkPanel.setEnableGUI(ownerWindow,false);
		modelViewer.setVisible(true);
	}

	/**
	 * Befehl: (Toolbar bei Statistikansicht) - WebViewer
	 * @return	Liefert <code>true</code>, wenn der WebViewer erfolgreich aufgerufen werden konnte
	 */
	private boolean commandSimulationShowWebViewer() {
		if (statisticPanel.getStatistic()==null || statisticPanel.getStatistic().length!=1 || statisticPanel.getStatistic()[0]==null) {
			MsgBox.error(this,Language.tr("Window.SaveStatisticsError.Title"),Language.tr("Window.SaveStatisticsError.NoSimulationResults"));
			return false;
		}

		File tempFile=null;
		try {
			tempFile=File.createTempFile("cs-web-statistics",".html");
			tempFile.deleteOnExit();
		} catch (IOException e) {
			String s=(tempFile==null)?"cs-web-statistics.html":tempFile.toString();
			MsgBox.error(this,Language.tr("Window.SaveStatisticsError.Title"),String.format(Language.tr("Window.SaveStatisticsError.Info"),s));
			return false;
		}

		StatisticWebAppWriter writer=new StatisticWebAppWriter(statisticPanel.getStatistic()[0]);
		if (!writer.saveToFile(tempFile)) {
			MsgBox.error(this,Language.tr("Window.SaveStatisticsError.Title"),String.format(Language.tr("Window.SaveStatisticsError.Info"),tempFile.toString()));
			return false;
		}

		try {
			Desktop.getDesktop().open(tempFile);
		} catch (IOException e) {
			MsgBox.error(this,Language.tr("Window.SaveStatisticsError.Title"),String.format(Language.tr("Window.SaveStatisticsError.Info"),tempFile.toString()));
		}

		return true;
	}

	/**
	 * Befehl: Tools - Rechner
	 */
	private void commandToolsRechner() {
		CalculatorDialog calc=new CalculatorDialog(ownerWindow,helpLink);
		calc.setVisible(true);
	}

	/**
	 * Befehl: Tools - Warteschlangenrechner
	 */
	private void commandToolsAuslastungsrechner() {
		final QueueingCalculatorDialog dialog=new QueueingCalculatorDialog(this,helpLink);
		dialog.setVisible(true);
	}

	/**
	 * Befehl: Tools - Simulation eines Einfach-Modells
	 */
	private void commandToolsSimpleSimulation() {
		SimpleSimulationDialog sim=new SimpleSimulationDialog(ownerWindow,helpLink);
		sim.setVisible(true);
	}

	/**
	 * Befehl: Tools - Varianzanalyse erstellen
	 */
	private void commandToolsVarianzanalyse() {
		CallcenterModel model=modelPanel.getModel(true);

		backgroundSimulator.stop(true);

		setGUIState(new VarianzAnalysePanel(new SimDoneNotify(),model,helpLink));
	}

	/**
	 * Befehl: Tools - Verteilung anpassen
	 */
	private void commandToolsFitting() {
		FitDialog fit=new FitDialog(ownerWindow,modelPanel.getModel(true),helpLink);
		fit.setVisible(true);
		if (fit.modelChanged) {modelPanel.setModel(fit.model); modelPanel.setModelChanged(true);}
	}

	/**
	 * Befehl: Tools - Kommandozeilenbefehl ausf�hren
	 */
	private void commandToolsExecuteCommand() {
		new CommandLineDialog(this,stream->new CommandLineSystem(null,stream),window->helpLink.dialogCommandLine.run());
	}

	/**
	 * Befehl: Tools - Simulationslauf in Logdatei aufzeichnen
	 */

	private void commandToolsLogRun() {
		CallcenterModel editModel=modelPanel.getModel(true);
		int sum=0;
		for (int i=0; i<editModel.caller.size(); i++) sum+=editModel.caller.get(i).freshCallsCountMean;
		if (sum>10000) {
			if (!MsgBox.confirm(this,
					Language.tr("Window.LogRun.LargeModel.Title"),Language.tr("Window.LogRun.LargeModel.Info"),
					Language.tr("Window.LogRun.LargeModel.Yes.Info"),
					Language.tr("Window.LogRun.LargeModel.No.Info")))
				return;
		}

		JFileChooser fc=new JFileChooser();
		fc.setDialogTitle(Language.tr("Window.LogRun.Title"));
		CommonVariables.initialDirectoryToJFileChooser(fc);
		FileFilter txt=new FileNameExtensionFilter(Language.tr("FileType.Text")+" (*.txt)","txt");
		fc.addChoosableFileFilter(txt);
		fc.setFileFilter(txt);
		fc.setAcceptAllFileFilterUsed(false);

		if (fc.showSaveDialog(this) != JFileChooser.APPROVE_OPTION) return;
		CommonVariables.initialDirectoryFromJFileChooser(fc);
		File file=fc.getSelectedFile();

		if (file.getName().indexOf('.')<0) {if (fc.getFileFilter()==txt) file=new File(file.getAbsoluteFile()+".txt");}

		commandSimulationRun(false,file);
	}

	/**
	 * Befehl: Tools - Simulationergebnisse verschiedener Modelle vergleichen
	 */
	private void commandToolsCompare() {
		CompareSelectDialog dialog=new CompareSelectDialog(ownerWindow,helpLink.pageCompareModal,5);
		dialog.setVisible(true);
		if (dialog.getClosedBy()!=BaseEditDialog.CLOSED_BY_OK) return;

		Statistics[] statistic=ComparePanel.getStatisticFiles(dialog.statisticFiles);
		String[] title=new String[statistic.length];
		for (int i=0;i<statistic.length;i++) {
			if (statistic[i]==null) {
				MsgBox.error(this,Language.tr("Window.Compare.NotAValidStatisticsFile.Title"),String.format(Language.tr("Window.Compare.NotAValidStatisticsFile.Info"),""+(i+1),dialog.statisticFiles[i].toString()));
				return;
			}
			title[i]=statistic[i].editModel.name;
		}

		setGUIState(new ComparePanel(ownerWindow,statistic,title,dialog.isNebeneinander(),new SimDoneNotify(),true,
				helpLink,helpLink.pageCompare,helpLink.pageCompareModal
				));
	}

	/**
	 * Befehl: Tools - Aktuelles Modell f�r sp�teren Vergleich festhalten
	 */
	private void commandToolsKeepModel() {
		CallcenterModel model=modelPanel.getModel(true);
		CallcenterRunModel runModel=new CallcenterRunModel(model);
		String s=runModel.checkAndInit(false,false,SetupData.getSetup().strictCheck);
		if (s!=null) {
			MsgBox.error(this,Language.tr("Compare.Error.ModelError.Title"),Language.tr("Compare.Error.ModelError.CannotKeep"));
			return;
		}

		if (pinnedModel!=null) {
			if (!MsgBox.confirm(this,Language.tr("Compare.ReplaceKeptModel.Title"),Language.tr("Compare.ReplaceKeptModel.Info"),Language.tr("Compare.ReplaceKeptModel.YesInfo"),Language.tr("Compare.ReplaceKeptModel.NoInfo"))) return;
		}

		pinnedModel=model;
		MsgBox.info(this,Language.tr("Compare.Kept.Title"),Language.tr("Compare.Kept.Info"));
		for (AbstractButton button : menuToolsCompareKeptModel) button.setEnabled(true);
	}

	/**
	 * Befehl Extras - Aktuelles und festgehaltenes Modell vergleichen
	 * @param level	0: festgehaltenes Modell simulieren; 1: aktuelles Modell simulieren; 2: Ergebnisse anzeigen
	 */
	private void commandToolsCompareKeptModel(int level) {
		if (level==0) {
			if (pinnedModel==null) {
				MsgBox.error(this,Language.tr("Compare.Error.NoModelKept.Title"),Language.tr("Compare.Error.NoModelKept.Info"));
				return;
			}

			CallcenterModel model=modelPanel.getModel(true);
			CallcenterRunModel runModel=new CallcenterRunModel(model);
			String s=runModel.checkAndInit(false,false,SetupData.getSetup().strictCheck);
			if (s!=null) {
				MsgBox.error(this,Language.tr("Compare.Error.ModelError.Title"),Language.tr("Compare.Error.ModelError.CannotCompare"));
				return;
			}

			if (pinnedModel.equalsCallcenterModel(model)) {
				MsgBox.error(this,Language.tr("Compare.Error.IdenticalModels.Title"),Language.tr("Compare.Error.IdenticalModels.Info"));
				return;
			}

			backgroundSimulator.stop(true);

			StartAnySimulator startAnySimulator=new StartAnySimulator(pinnedModel,null);
			startAnySimulator.check();
			setGUIState(new CallcenterRunPanel(startAnySimulator.run(),new SimDoneNotify(),false,1));
			return;
		}

		if (level==1) {
			StartAnySimulator startAnySimulator=new StartAnySimulator(modelPanel.getModel(true),null);
			startAnySimulator.check();
			setGUIState(new CallcenterRunPanel(startAnySimulator.run(),new SimDoneNotify(),false,2));
			return;
		}

		if (level==2) {
			setGUIState(new ComparePanel(ownerWindow,pinnedCompareStatistic,new String[]{Language.tr("Compare.Models.Base"),Language.tr("Compare.Models.Changed")},
					true,new SimDoneNotify(),true,
					helpLink,
					helpLink.pageCompare,helpLink.pageCompareModal
					));
			return;
		}
	}

	/**
	 * Befehl: Tools - Serverdienste
	 */
	private void commandToolsServer() {
		backgroundSimulator.stop(true);
		setGUIState(new SimServerPanel(ownerWindow,new SimDoneNotify(),helpLink));
	}

	/**
	 * Befehl: Hilfe - Hilfe
	 * @param topic	Anzuzeigende Hilfeseite
	 */
	private void commandHelp(String topic) {
		/* Ggf. passende Hilfeseite finde */
		if (topic==null || topic.isEmpty()) {
			Runnable helpRunnable=findHelpTopic();
			if (helpRunnable!=null) {
				helpRunnable.run();
				return;
			}
		}

		/* Thema in Hilfe laden */
		if (topic==null) topic=HelpConsts.CONTENT_PAGE;
		Help.topic(this,topic);
	}

	/**
	 * Befehl: Hilfe - Modell anzeigen
	 */
	private void commandHelpModel() {
		File modelFile=new File(new File(System.getProperty("user.dir")),Language.tr("Window.Info.ModelOverview.FileName"));
		if (!modelFile.isFile()) modelFile=new File(new File(System.getProperty("user.dir"))+File.separator+"docs",Language.tr("Window.Info.ModelOverview.FileName"));

		if (!modelFile.isFile()) {
			try {
				final URI uri=new URI(XMLTools.mediaURL+Language.tr("Window.Info.ModelOverview.FileName"));
				if (!MsgBox.confirmOpenURL(this,uri)) return;
				Desktop.getDesktop().browse(uri);
			} catch (IOException | URISyntaxException e) {
				MsgBox.error(this,Language.tr("Window.Info.NoInternetConnection"),String.format(Language.tr("Window.Info.NoInternetConnection.ModelOverview"),XMLTools.mediaURL+Language.tr("Window.Info.ModelOverview.FileName")));
			}
			return;
		}
		try {
			Desktop.getDesktop().open(modelFile);
		} catch (IOException e) {
			MsgBox.error(this,Language.tr("Window.Info.ModelOverviewError.Opening.Title"),String.format(Language.tr("Window.Info.ModelOverviewError.Opening.Info"),modelFile.toString(),e.getLocalizedMessage()));
		}
	}

	/**
	 * Befehl: Hilfe - Glossar anzeigen
	 */
	private void commandHelpGlossary() {
		File modelFile=new File(new File(System.getProperty("user.dir")),Language.tr("Window.Info.ShowGlossary.FileName"));
		if (!modelFile.isFile()) modelFile=new File(new File(System.getProperty("user.dir"))+File.separator+"docs",Language.tr("Window.Info.ShowGlossary.FileName"));

		if (!modelFile.isFile()) {
			try {
				final URI uri=new URI(XMLTools.mediaURL+Language.tr("Window.Info.ShowGlossary.FileName"));
				if (!MsgBox.confirmOpenURL(this,uri)) return;
				Desktop.getDesktop().browse(uri);
			} catch (IOException | URISyntaxException e) {
				MsgBox.error(this,Language.tr("Window.Info.NoInternetConnection"),String.format(Language.tr("Window.Info.NoInternetConnection.ShowGlossary"),XMLTools.mediaURL+Language.tr("Window.Info.ShowGlossary.FileName")));
			}
			return;
		}
		try {
			Desktop.getDesktop().open(modelFile);
		} catch (IOException e) {
			MsgBox.error(this,Language.tr("Window.Info.ShowGlossaryError.Opening.Title"),String.format(Language.tr("Window.Info.ShowGlossaryError.Opening.Info"),modelFile.toString(),e.getLocalizedMessage()));
		}
	}

	/**
	 * Befehl: Hilfe - Lehrbuch
	 */
	private void commandHelpBook() {
		try {
			final URI uri=new URI("https://www.springer.com/de/book/9783658183080");
			if (!MsgBox.confirmOpenURL(this,uri)) return;
			Desktop.getDesktop().browse(uri);
		} catch (IOException | URISyntaxException e) {
			MsgBox.error(this,Language.tr("Window.Info.NoInternetConnection"),String.format(Language.tr("Window.Info.NoInternetConnection.Address"),"https://www.springer.com/de/book/9783658183080"));
		}
	}

	/**
	 * Befehl: Hilfe - Lizenzinformationen
	 */
	private void commandHelpLicenseInfo() {
		new LicenseViewer(this);
	}

	/**
	 * Befehl: Hilfe - Programminfo
	 */
	private void commandHelpInfo() {
		final InfoDialog infoDialog=new InfoDialog(MainPanel.this.ownerWindow,VersionConst.version);

		final String currentLanguage=SetupData.getSetup().language;
		equalsExampleModel=CallcenterModelExamples.equalsExampleModel(modelPanel.getModel(false));

		infoDialog.showVersionHistory=false;
		infoDialog.setLocationRelativeTo(infoDialog.getOwner());
		infoDialog.setVisible(true);

		if (!SetupData.getSetup().language.equals(currentLanguage)) {
			SetupData.getSetup().resetLanguageWasAutomatically();
			HelpBase.hideHelpFrame();
			if (reloadWindow!=null) SwingUtilities.invokeLater(reloadWindow);
		}

		if (infoDialog.showVersionHistory) commandHelp("Changelog");
		if (infoDialog.showLicenses) commandHelpLicenseInfo();
	}

	/**
	 * Zeigt die Optimierungsergebnisse an.
	 * @param results	Optimierungsergebnisse
	 */
	private void showOptimizeViewer(final OptimizeData results) {
		OptimizeViewer viewer=new OptimizeViewer(ownerWindow,helpLink);
		viewer.loadResults(results);
		viewer.setCloseNotify(new ModelCallbackViewerClosed(viewer));
		setGUIState(viewer);
	}

	/**
	 * Zeigt die Optimierungsergebnisse an.
	 * @param file	Zu ladende Datei mit Optimierungsergebnissen
	 * @return	Liefert <code>true</code>, wenn die Daten erfolgreich geladen werden konnte
	 */
	private boolean showOptimizeViewer(final File file) {
		OptimizeViewer viewer=new OptimizeViewer(ownerWindow,helpLink);
		String s=viewer.loadResults(file);
		if (s!=null) {MsgBox.error(this,Language.tr("Window.OptimizerResults.Error"),s); return false;}
		viewer.setCloseNotify(new ModelCallbackViewerClosed(viewer));
		setGUIState(viewer);
		return true;
	}

	/**
	 * Zeigt die Ergebnisse einer mehrt�gigen Simulation an
	 * @param file	Zu ladende Datei mitden Ergebnissen der mehrt�gigen Simulation
	 * @return	Liefert <code>true</code>, wenn die Daten erfolgreich geladen werden konnte
	 */
	private boolean showConnectedViewer(final File file) {
		ConnectedModel model=new ConnectedModel();
		String s=model.loadFromFile(file);
		if (s!=null) {
			MsgBox.error(this,Language.tr("Window.ConnectedResults.Error.Title"),String.format(Language.tr("Window.ConnectedResults.Error.Info"),file.toString(),s));
			return false;
		}
		Object o=model.getStatistics();
		if (o instanceof String) {
			MsgBox.error(this,Language.tr("Window.ConnectedResults.Error.StatisticTitle"),(String)o);
			return false;
		}
		if (o instanceof Statistics[]) {
			ConnectedViewer viewer=new ConnectedViewer(ownerWindow,helpLink,(Statistics[])o);
			viewer.setCloseNotify(new ModelCallbackViewerClosed(viewer));
			setGUIState(viewer);
			return true;
		} else {
			return false;
		}
	}

	/**
	 * Liefert das Hilfe-Thema f�r die aktuell ge�ffnete Seite im Programmfenster
	 * @return	Runnable das die entsprechende Hilfe-Aktion ausf�hrt
	 */
	private Runnable findHelpTopic() {
		if (workPanel != null) {
			if (workPanel instanceof BatchPanel) return helpLink.pageBatch;
			if (workPanel instanceof OptimizePanel) return helpLink.pageOptimize;
			if (workPanel instanceof ConnectedPanel) return helpLink.pageConnected;
			if (workPanel instanceof SimServerPanel) return helpLink.pageServer;
			if (workPanel instanceof VarianzAnalysePanel) return helpLink.pageVarianceAnalysis;
			if (workPanel instanceof ImporterPanel) return helpLink.pageTableImport;
		} else
			switch (currentGUIMode) {
			case 0:
				switch (modelPanel.getSelectedTabIndex()) {
				case 0: return helpLink.pageGeneral;
				case 1: return helpLink.pageCaller;
				case 2: return helpLink.pageCallcenter;
				case 3: return helpLink.pageSkillLevel;
				case 4: return helpLink.pageModelInformation;
				}
				break;
			case 1: return helpLink.pageStatistic;
			case 2: return null;
			}
		return null;
	}

	/**
	 * Zeigt eine Hilfe-Seite als modalen Dialog an.
	 * @param topic	Anzuzeigende Hilfeseite
	 * @param parent	�bergeordnetes Fenster
	 */
	private void showModalHelp(final String topic, final Container parent) {
		Help.topicModal(parent,topic);
	}

	/**
	 * DTD-Datei zur Beschreibung der Dateiformate aufrufen
	 * (wird �ber Hilfe-Special-Links ausgef�hrt).
	 */
	private void showDTD() {
		File modelFile=new File(new File(System.getProperty("user.dir")),Language.tr("XML.DTD"));
		if (!modelFile.isFile()) {
			MsgBox.error(this,Language.tr("Window.Info.FileFormatError.NotExist.Title"),String.format(Language.tr("Window.Info.FileFormatError.NotExist.Info"),modelFile.toString()));
			return;
		}
		try {
			Desktop.getDesktop().open(modelFile);
		} catch (IOException e) {
			MsgBox.error(this,Language.tr("Window.Info.FileFormatError.Opening.Title"),String.format(Language.tr("Window.Info.FileFormatError.Opening.Info"),modelFile.toString(), e.getLocalizedMessage()));
		}
	}

	/**
	 * XSD-Datei zur Beschreibung der Dateiformate aufrufen
	 * (wird �ber Hilfe-Special-Links ausgef�hrt).
	 */
	private void showXSD() {
		File modelFile=new File(new File(System.getProperty("user.dir")),Language.tr("XML.XSD"));
		if (!modelFile.isFile()) {
			MsgBox.error(this,Language.tr("Window.Info.FileFormatError.NotExist.Title"),String.format(Language.tr("Window.Info.FileFormatError.NotExist.Info"),modelFile.toString()));
			return;
		}
		try {
			Desktop.getDesktop().open(modelFile);
		} catch (IOException e) {
			MsgBox.error(this,Language.tr("Window.Info.FileFormatError.Opening.Title"),String.format(Language.tr("Window.Info.FileFormatError.Opening.Info"),modelFile.toString(), e.getLocalizedMessage()));
		}
	}

	/**
	 * Aktiviert oder deaktiviert ein besonderes Arbeits-Panel im Hauptfenster
	 * @param workPanel	Arbeits-Panel (kann <code>null</code> sein, wenn die normalen Programmfunktionen des Hauptfensters aktiviert werden sollen)
	 */
	private void setGUIState(JCloseablePanel workPanel) {
		if (workPanel==null) {
			final JMenuBar menu=((JFrame)ownerWindow).getJMenuBar();
			if (menu!=null) menu.setVisible(true);
			if (toolBar!=null) toolBar.setVisible(true);
			if (ribbonBar!=null) ribbonBar.setVisible(true);
			add(mainPanel,BorderLayout.CENTER);
			if (this.workPanel!=null) remove(this.workPanel);
			mainPanel.repaint();
		} else {
			final JMenuBar menu=((JFrame)ownerWindow).getJMenuBar();
			if (menu!=null) menu.setVisible(false);
			if (toolBar!=null) toolBar.setVisible(false);
			if (ribbonBar!=null) ribbonBar.setVisible(false);
			remove(mainPanel);
			add(workPanel,BorderLayout.CENTER);
			if (workPanel instanceof JWorkPanel) ((JWorkPanel)workPanel).setVisibleInit();
		}

		this.workPanel=workPanel;
	}

	/**
	 * Stellt sicher, dass w�hrend der Verarbeitung von
	 * {@link #setGUIMode(short)} keine weiteren Aufrufe
	 * stattfinden.
	 * @see #setGUIMode(short)
	 */
	private boolean inSetGUIMode=false;

	/**
	 * Zeigt Modell-Editor oder Statistik
	 * @param mode 0=Modell-Editor, 1=Statistik
	 */
	private void setGUIMode(short mode) {
		if (inSetGUIMode) return;

		Statistics[] stat=statisticPanel.getStatistic();
		for (AbstractButton button : menuSaveStatistics) button.setEnabled(stat!=null && stat.length>0 && stat[0]!=null);

		if (mode==currentGUIMode) return;
		inSetGUIMode=true;

		SetupData setup=SetupData.getSetup();

		if (setup.ribbonMode) {
			if (mode==0) ribbonBar.setSelectedIndex(1);
			if (mode==1) ribbonBar.setSelectedIndex(6);
			if (mode==2) ribbonBar.setSelectedIndex(0);
		} else {
			editorButton.setSelected(mode==0);
			statistikButton.setSelected(mode==1);
		}

		mainPanel.remove(modelPanel);
		mainPanel.remove(statisticPanel);
		mainPanel.remove(welcomePanel);

		switch (mode) {
		case 0:
			mainPanel.add(modelPanel,BorderLayout.CENTER);
			break;
		case 1:
			mainPanel.add(statisticPanel,BorderLayout.CENTER);
			break;
		case 2:
			mainPanel.add(welcomePanel,BorderLayout.CENTER);
			SwingUtilities.invokeLater(()->welcomePanel.loadPage(welcomePanel.languagePage(getWelcomePageName()+".html")));
			break;
		}

		modelPanel.setVisible(mode==0);
		statisticPanel.setVisible(mode==1);
		welcomePanel.setVisible(mode==2);

		mainPanel.invalidate();

		if (!SetupData.getSetup().ribbonMode) {
			loadModelButton.setVisible(mode==0 || mode==2);
			saveModelButton.setVisible(mode==0 || mode==2);
			simButton.setVisible(mode==0 || mode==2);
			loadStatisticButton.setVisible(mode==1);
			saveStatisticButton.setVisible(mode==1);
			showModel.setVisible(mode==1);
			showWebViewer.setVisible(mode==1);
		}
		Statistics[] statistic=statisticPanel.getStatistic();
		boolean statisticAvailable=statistic!=null && statistic.length>0 && statistic[0]!=null;
		for (AbstractButton button : menuSimulationShowModel) button.setEnabled(statisticAvailable);
		for (AbstractButton button : menuSimulationShowWebViewer) button.setEnabled(statisticAvailable);

		currentGUIMode=mode;
		inSetGUIMode=false;
	}

	/**
	 * F�hrt die automatische Speicherung der Statistikdaten durch.
	 * @param statistic	Zu speichernde Statistikdaten
	 */
	private void runAutoSave(final Statistics statistic) {
		final SetupData setup=SetupData.getSetup();
		if (!setup.autoSaveStatistic && !setup.autoSaveFilter) {
			MsgBox.warning(this,Language.tr("Window.RunAndSave.NoResultsToBeSavedDefined.Title"),Language.tr("Window.RunAndSave.NoResultsToBeSavedDefined.Info"));
			return;
		}

		final AutoSave autoSave=new AutoSave(statistic);
		if (setup.autoSaveStatistic) {
			String s=autoSave.saveStatistic(setup.autoSaveStatisticFolder);
			if (s!=null) MsgBox.error(this,Language.tr("Window.SaveStatisticsError.Title"),s);
		}

		if (setup.autoSaveFilter) {
			String s=autoSave.saveFilter(setup.autoSaveFilterScript,setup.autoSaveFilterOutput);
			if (s!=null) MsgBox.error(this,Language.tr("Window.SaveFilteredStatisticsError.Title"),s);
		}
	}

	@Override
	protected void action(final Object sender) {
		if (!(sender instanceof JMenuItem)) return;
		final JMenuItem obj=(JMenuItem)sender;

		/* Letzte Modelle */

		for (int i=0;i<menuRecentlyUsed.getMenuComponentCount();i++) if (menuRecentlyUsed.getMenuComponent(i)==obj) {
			loadModelFileAsString(obj.getText());
			return;
		}
	}

	/**
	 * Liefert alle Daten innerhalb dieses Panels als Objekt-Array
	 * um dann das Panel neu laden und die Daten wiederherstellen
	 * zu k�nnen.
	 * @return	7-elementiges Objekt-Array mit allen Daten des Panels
	 * @see #setAllData(Object[])
	 */
	public Object[] getAllData() {
		return new Object[]{
				modelPanel.getModel(true),
				modelPanel.isModelChanged(),
				modelPanel.getLastFileName(),
				statisticPanel.getStatistic()[0],
				((int)currentGUIMode),
				modelPanel.getSelectedTabIndex(),
				equalsExampleModel,
		};
	}

	/**
	 * Reinitialisiert die Daten in dem Panel wieder aus einem
	 * zuvor erstellten Objekt-Array.
	 * @param data	7-elementiges Objekt-Array mit allen Daten des Panels
	 * @return	Gibt an, ob die Daten aus dem Array erfolgreich geladen werden konnten
	 * @see #getAllData()
	 */
	public boolean setAllData(Object[] data) {
		if (data==null || data.length!=7) return false;
		if (!(data[0] instanceof CallcenterModel)) return false;
		if (!(data[1] instanceof Boolean)) return false;
		if (data[2]!=null && !(data[2] instanceof String)) return false;
		if (data[3]!=null && !(data[3] instanceof Statistics)) return false;
		if (data[4]==null || !(data[4] instanceof Integer)) return false;
		if (data[5]==null || !(data[5] instanceof Integer)) return false;
		if (data[6]==null || !(data[6] instanceof Integer)) return false;

		int equalsExampleModel=(Integer)data[6];
		CallcenterModel model=(CallcenterModel)data[0];
		if (equalsExampleModel>=0) model=CallcenterModelExamples.getExampleByNumber(equalsExampleModel);

		modelPanel.setModel(model);
		modelPanel.setModelChanged((Boolean)data[1]);
		modelPanel.setLastFileName((String)data[2]); if (data[2]!=null) setAdditionalTitle((String)data[2]);
		statisticPanel.setStatistic(new Statistics[]{(Statistics)data[3]});
		setGUIMode((short)((int)((Integer)data[4])));
		if (SetupData.getSetup().ribbonMode && (((Integer)data[4]))==0) {
			ribbonBar.setSelectedIndex((Integer)data[5]+1);
		}
		modelPanel.setSelectedTabIndex((Integer)data[5]);

		return true;
	}

	/**
	 * �ber diese Methode kann dem Panal ein Callback mitgeteilt werden,
	 * das aufgerufen wird, wenn das Fenster neu geladen werden soll.
	 * @param reloadWindow	Callback, welches ein Neuladen des Fensters veranlasst.
	 */
	public void setReloadWindow(final Runnable reloadWindow) {
		this.reloadWindow=reloadWindow;
	}

	/**
	 * Wird beim Schlie�en des Modell-Viewers (eines Modells zu den Statistikergebnissen) aufgerufen.
	 * @see ViewerWithLoadModelCallback
	 */
	private class ModelCallbackViewerClosed implements Runnable {
		/** Modell-Viewer */
		private final ViewerWithLoadModelCallback viewer;

		/**
		 * Konstruktor der Klasse
		 * @param viewer	Modell-Viewer
		 */
		public ModelCallbackViewerClosed(ViewerWithLoadModelCallback viewer) {
			this.viewer=viewer;
		}

		@Override
		public void run() {
			if (viewer.getLoadModelToEditor()!=null) {
				if (modelPanel.isModelChanged()) {if (!discardModelOk()) return;}
				modelPanel.setModel(viewer.getLoadModelToEditor());
				statisticPanel.setStatistic(null);
				setGUIMode((short)0);
			}

			new SimDoneNotify().run();
		}
	}

	/**
	 * Wird beim Abschluss einer Simulation aufgerufen.
	 */
	private class SimDoneNotify implements Runnable {
		/**
		 * F�hrt die Verarbeitung zum Abschluss einer Simulation durch.
		 * @param runComplete	Ist die Simulation erfolgreich abgeschlossen worden?
		 * @param logFile	Log-Datei f�r die Simulation
		 * @param statistic	W�hrend der Simulation generierte Statistikdaten
		 */
		public void simDone(final boolean runComplete, final File logFile, final Statistics statistic) {
			if (runComplete) {
				statisticPanel.setStatistic(new Statistics[] {statistic});

				if (logFile==null) {
					setGUIMode((short)1);
				} else {
					setGUIMode((short)0);
					try {Desktop.getDesktop().open(logFile);} catch (IOException e) {MsgBox.error(MainPanel.this,Language.tr("Window.LogRun.ErrorOpeningLog.Title"),String.format(Language.tr("Window.LogRun.ErrorOpeningLog.Info"),logFile.toString()));}
				}
				if (saveAfterSim) runAutoSave(statistic);
			}
			saveAfterSim=false;
		}

		@Override
		public void run() {
			if (workPanel instanceof CallcenterRunPanel) {
				CallcenterRunPanel runPanel=(CallcenterRunPanel) workPanel;

				if (runPanel.compareModelsMode>0 && runPanel.getRunComplete()) {
					setGUIState(null);
					pinnedCompareStatistic[runPanel.compareModelsMode-1]=runPanel.getSimulator().collectStatistic();
					commandToolsCompareKeptModel(runPanel.compareModelsMode);
					return;
				}
				pinnedCompareStatistic[0]=null; /* Wenn pinned compare schief l�uft, Statistik freigeben */
				pinnedCompareStatistic[1]=null;

				if (!runPanel.getRunComplete()) MsgBox.warning(MainPanel.this,Language.tr("Window.Simulation.Canceled"),Language.tr("Window.Simulation.Canceled.Info"));
				/*
				 *  Reihenfolge ist wichtig, erst per simDone getGUIMode aufrufen, dann setGUIState,
				 *  damit das welcomePanel nicht kurz aufflackert, das hat einen eigenen Thread und der
				 *  reagiert nicht so schnell und dann laufen wir in einen Fehler in JavaFX:
				 *  http://hg.openjdk.java.net/openjfx/9-dev/rt/rev/ad5669aafe66
				 */
				simDone(runPanel.getRunComplete(), runPanel.getSimulator().getLogFile(),runPanel.getSimulator().collectStatistic());
				setGUIState(null);
				System.gc();
				return;
			}

			if (workPanel instanceof OptimizePanel) {
				OptimizePanel optimizePanel=(OptimizePanel)workPanel;
				setGUIMode((short)0); /* Theoretisch unn�tig, aber sch�tzt vor dem Fehler s.o. */
				setGUIState(null);
				if (optimizePanel.getResults()!=null) showOptimizeViewer(optimizePanel.getResults());
				System.gc();
				return;
			}

			if (workPanel instanceof ImporterPanel) {
				ImporterPanel importerPanel=(ImporterPanel)workPanel;
				setGUIMode((short)0); /* Theoretisch unn�tig, aber sch�tzt vor dem Fehler s.o. */
				setGUIState(null);
				CallcenterModel model=importerPanel.getResults();
				if (model!=null) {
					if (setModel(model)) modelPanel.setModelChanged(true);
				}
				System.gc();
				return;
			}

			if (workPanel instanceof ComparePanel) {
				ComparePanel comparePanel=(ComparePanel)workPanel;
				setGUIMode((short)0); /* Theoretisch unn�tig, aber sch�tzt vor dem Fehler s.o. */
				setGUIState(null);
				CallcenterModel editModel=comparePanel.getModelForEditor();
				if (editModel!=null) {
					if (setModel(editModel)) modelPanel.setModelChanged(true);
				}
				pinnedCompareStatistic[0]=null;
				pinnedCompareStatistic[1]=null;
				System.gc();
				return;
			}

			if (workPanel instanceof RearrangePanel) {
				RearrangePanel rearrangePanel=(RearrangePanel)workPanel;
				setGUIMode((short)0); /* Theoretisch unn�tig, aber sch�tzt vor dem Fehler s.o. */
				setGUIState(null);
				CallcenterModel editModel=rearrangePanel.getModelForEditor();
				if (editModel!=null) {
					if (setModel(editModel)) modelPanel.setModelChanged(true);
				}
				System.gc();
				return;
			}

			if (workPanel instanceof RevenueOptimizerPanel) {
				RevenueOptimizerPanel revenueOptimizerPanel=(RevenueOptimizerPanel)workPanel;
				setGUIMode((short)0); /* Theoretisch unn�tig, aber sch�tzt vor dem Fehler s.o. */
				setGUIState(null);
				CallcenterModel newModel=revenueOptimizerPanel.getBestModel();
				if (newModel!=null) {
					if (setModel(newModel)) modelPanel.setModelChanged(true);
				}
				System.gc();
				return;
			}

			/* Alle anderen, normalen Panels, die keine besondere Verarbeitung ben�tigen */
			setGUIState(null);
			System.gc();
		}
	}

	/**
	 * Wird beim Schlie�en des Modell-Viewers aufgerufen.
	 * @see	CallcenterModelEditorPanelDialog
	 */
	private class ModelViewerClosed implements Runnable {
		/** Modell-Viewer */
		private final CallcenterModelEditorPanelDialog modelViewer;

		/**
		 * Konstruktor der Klasse
		 * @param modelViewer	Modell-Viewer
		 */
		public ModelViewerClosed(CallcenterModelEditorPanelDialog modelViewer) {
			this.modelViewer=modelViewer;
		}

		@Override
		public void run() {
			JWorkPanel.setEnableGUI(ownerWindow,true);
			if (modelViewer.getLoadModelToEditor()) {
				if (modelPanel.isModelChanged()) {if (!discardModelOk()) return;}
				modelPanel.setModel(modelViewer.getCallcenterModel());
				statisticPanel.setStatistic(null);
				setGUIMode((short)0);
			}
		}
	}

	/**
	 * Liefert einen m�glichen Hilfe-Special-Link aus dem Welcome-Panel-
	 */
	private class ProcessSpecialWelcomeLink extends ProcessSpecialLink {
		@Override
		protected String getHRef() {
			return welcomePanel.getSpecialLink();
		}
	}

	/**
	 * F�hrt die Verarbeitung beim Anklicken eines Hilfe-Special-Links durch.
	 * @param href	Hilfe-Special-Link
	 */
	private void processSpecialLink(final String href) {
		if (href.equalsIgnoreCase("escape")) {setGUIMode((short)0); return;}
		if (href.equalsIgnoreCase("go")) {setGUIMode((short)0); return;}
		if (href.equalsIgnoreCase("help")) {commandHelp(null); return;}
		if (href.length()>4 && href.substring(0,5).equalsIgnoreCase("help#")) {commandHelp(href.substring(5)); return;}
		if (href.equalsIgnoreCase("model")) {commandHelpModel(); return;}
		if (href.equalsIgnoreCase("glossar")) {commandHelpGlossary(); return;}
		if (href.equalsIgnoreCase("dtd")) {showDTD(); return;}
		if (href.equalsIgnoreCase("xsd")) {showXSD(); return;}
		if (href.equalsIgnoreCase("programhome")) {
			try {
				final URI uri=new URI(UpdateSystem.defaultProtocollHomepage+"://"+UpdateSystem.wwwHomeURL);
				if (!MsgBox.confirmOpenURL(this,uri)) return;
				Desktop.getDesktop().browse(uri);
			} catch (IOException | URISyntaxException e) {
				MsgBox.error(MainPanel.this,Language.tr("Window.Info.NoInternetConnection"),String.format(Language.tr("Window.Info.NoInternetConnection.Address"),UpdateSystem.defaultProtocollHomepage+"://"+UpdateSystem.wwwHomeURL));
			}
			return;
		}
		if (href.equalsIgnoreCase("mail")) {
			try {
				Desktop.getDesktop().mail(new URI("mailto:"+UpdateSystem.mailURL));
			} catch (IOException | URISyntaxException e1) {
				MsgBox.error(MainPanel.this,Language.tr("Window.Info.NoEMailProgram.Title"),String.format(Language.tr("Window.Info.NoEMailProgram.Info"),"mailto:"+UpdateSystem.mailURL));
			}
			return;
		}
	}

	/**
	 * Klasse zur Verarbeitung von Hilfe-Special-Links
	 */
	private abstract class ProcessSpecialLink implements Runnable {
		/**
		 * Liefert die Adresse des angeklicken Links
		 * @return	Adresse des angeklicken Links
		 */
		protected abstract String getHRef();

		@Override
		public void run() {
			final String key="special:";
			String href=getHRef();
			if (!href.substring(0,Math.min(href.length(),key.length())).equalsIgnoreCase(key)) return;
			href=href.substring(key.length());
			processSpecialLink(href);
		}
	}

	/**
	 * Wird nach dem Abschluss des Modell-Generator-Dialogs aufgerufen
	 */
	private class GeneratorCallback implements Runnable {
		/** 	0,1,2=Anrufer (24/48/49 Intervalle), 3,4,5=Agenten (24/48/49 Intervalle), 6=Skills, 7=Schablonen-Import, 8=Agenten-Effizient, 9=Agenten-Verf�gbarkeit */
		private final int editType;

		/**
		 * Konstruktor der Klasse
		 * @param editType		0,1,2=Anrufer (24/48/49 Intervalle), 3,4,5=Agenten (24/48/49 Intervalle), 6=Skills, 7=Schablonen-Import, 8=Agenten-Effizient, 9=Agenten-Verf�gbarkeit
		 */
		public GeneratorCallback(int editType) {
			this.editType=editType;
		}

		@Override
		public void run() {
			commandDataGenerator(editType,modelPanel.getSelectedCallcenterIndex(),null);
		}
	}

	/**
	 * Task der zeitgesteuert ausgef�hrt wird, um eine
	 * Kopie des aktuellen Modells im Editor f�r die Hintergrundsimulation
	 * bereit zu stellen.
	 * @see MainPanel#backgroundSimulator
	 */
	private class BackgroundModelGetter extends TimerTask {
		@Override
		public void run() {
			if (workPanel!=null) return;
			try {
				if (SwingUtilities.isEventDispatchThread()) {
					if (modelPanel!=null && backgroundSimulator.equalsCurrentModel(modelPanel.getModel(false))) return;
					backgroundSimulator.setModel((modelPanel==null)?null:(modelPanel.getModel(true)));
				} else {
					SwingUtilities.invokeAndWait(()-> {
						if (modelPanel!=null && backgroundSimulator.equalsCurrentModel(modelPanel.getModel(false))) return;
						backgroundSimulator.setModel((modelPanel==null)?null:(modelPanel.getModel(true)));
					});
				}
			} catch (InvocationTargetException | InterruptedException e) {}
		}
	}
}