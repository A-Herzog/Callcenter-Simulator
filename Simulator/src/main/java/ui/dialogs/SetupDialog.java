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
package ui.dialogs;

import java.awt.Desktop;
import java.awt.FlowLayout;
import java.awt.Window;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.io.File;
import java.io.IOException;
import java.io.Serializable;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.ButtonGroup;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JComboBox;
import javax.swing.JFileChooser;
import javax.swing.JLabel;
import javax.swing.JMenuItem;
import javax.swing.JPanel;
import javax.swing.JPopupMenu;
import javax.swing.JRadioButton;
import javax.swing.JScrollPane;
import javax.swing.JTabbedPane;
import javax.swing.JTextArea;
import javax.swing.JTextField;
import javax.swing.SwingUtilities;
import javax.swing.Timer;

import language.Language;
import mathtools.NumberTools;
import mathtools.distribution.swing.CommonVariables;
import systemtools.GUITools;
import systemtools.MsgBox;
import tools.IconListCellRenderer;
import tools.SetupData;
import tools.SetupData.StartMode;
import ui.HelpLink;
import ui.UpdateSystem;
import ui.editor.BaseEditDialog;
import ui.images.Images;

/**
 * Zeigt einen Dialog mit Konfigurationseinstellungen an
 * @author Alexander Herzog
 * @version 1.0
 */
public class SetupDialog extends BaseEditDialog {
	/**
	 * Serialisierungs-ID der Klasse
	 * @see Serializable
	 */
	private static final long serialVersionUID = 6586401312717916706L;

	/* Dialogseite "Benutzeroberfläche" */

	/** Auswahlfeld "Anzeigesprache" */
	private JComboBox<String> languages;
	/** Auswahlfeld "Menü-Modus" */
	private JComboBox<String> programMenuMode;
	/** Auswahlfeld "Schriftgrößen" */
	private JComboBox<String> fontSizes;
	/** Auswahlfeld "Thema" */
	private JComboBox<String> lookAndFeel;
	/** Menü in Titelzeile kombinieren? (Für Flat-Look&amp;Feels unter Windows) */
	private JCheckBox lookAndFeelCombinedMenu;
	/** Auswahlfeld "Fenstergröße beim Programmstart" */
	private JComboBox<String> programStartWindow;
	/** Auswahlfeld "Modell beim Programmstart laden" */
	private JComboBox<String> programStartModel;
	/** Option "Willkommensseite beim Programmstart anzeigen" */
	private JCheckBox programStartWelcomePage;
	/** Option "Java-Version prüfen und waren, wenn veraltet und unsicher" */
	private JCheckBox programStartJavaCheck;

	/* Dialogseite "Simulation" */

	/** Option "Strenge Prüfung des Modells" */
	private JCheckBox strictCheck;
	/** Option "Simulation im Hintergrund starten" */
	private JCheckBox backgroundSim;
	/** Eingabefeld "Maximale Anzahl an Threads" */
	private JTextField maxNumberOfThreads;
	/** Option "Anzahl an zu simulierenden Tagen erhöhen wenn sinnvoll" */
	private JCheckBox increaseNumberOfDays;
	/** Auswahloption "Simulation auf lokalem Rechner durchführen" */
	private JRadioButton simulationLocal;
	/** Auswahloption "Simulation auf Rechner im Netzwerk durchführen" */
	private JRadioButton simulationRemote;
	/** Eingabefeld "Netzwerkname des Servers" */
	private JTextField networkName;
	/** Eingabefeld "Portnummer des Servers" */
	private JTextField networkPort;
	/** Eingabefeld "Passwort für Server (optional)" */
	private JTextField networkPasswort;
	/** Eingabefeld "Anteil an Tagen, der auf dem Server simuliert werden soll" */
	private JTextField networkServer;
	/** Schaltfläche "Automatisch einstellen" (Aufteilung lokal/remote Simulation) */
	private JButton networkLocalButton;

	/* Dialogseite "Simulationsserver" */

	/** Eingabefeld "Portnummer des Simulationsservers" */
	private JTextField networkServerPort;

	/** Eingabefeld "Portnummer des Webservers" */
	private JTextField networkServerPortWeb;

	/** Eingabefeld "Passwort für Server (optional)" */
	private JTextField networkServerPasswort;

	/** Eingabebereich "Zulässige Client-IPs" */
	private JTextArea networkServerIPs;

	/* Dialogseite "Grafiken" */

	/** Eingabefeld "Auflösung beim Speichern" */
	private JTextField imageSize;

	/* Dialogseite "Statistik" */

	/** Option "Texte mit Word öffnen" */
	private JCheckBox openWord;
	/** Option "Texte mit OpenOffice/LibreOffice öffnen" */
	private JCheckBox openODT;
	/** Option "Tabellen mit Excel öffnen" */
	private JCheckBox openExcel;
	/** Option "Tabellen mit OpenOffice/LibreOffice öffnen" */
	private JCheckBox openODS;
	/** Anbieten, Statistik-Ergebnisse als PDF zu öffnen? */
	private JCheckBox openPDF;

	/* Dialogseite "Updates" */

	/** Option "Programm automatisch aktualisieren" */
	private JCheckBox autoUpdate;
	/** Infotext 1 für Updates */
	private JLabel autoUpdateInfo;
	/** Infotext 2 für Updates */
	private JLabel autoUpdateInfo2;
	/** Schaltfläche "Updates jetzt suchen" */
	private JButton autoUpdateButton;
	/** Schaltfläche "Manuell aktualisieren" */
	private JButton manualUpdateButton;
	/** Popupmenü für Schaltfläche "Manuell aktualisieren" */
	private JPopupMenu manualUpdatePopup;
	/** Eintrag "Webseite aufrufen" für Manuelles-Update-Popupmenü */
	private JMenuItem manualUpdatePopupWebPage;
	/** Eintrag "Update herunterladen" für Manuelles-Update-Popupmenü */
	private JMenuItem manualUpdatePopupDownload;

	/** Referenz auf das Setup-Singleton. */
	private final SetupData setup=SetupData.getSetup();

	/** Timer für die Anzeige des Fortschritts des Update-Downloads */
	private Timer downloadInfoTimer;

	/**
	 * Konstruktor der Klasse <code>SetupDialog</code>
	 * @param owner	Übergeordnetes Fenster
	 * @param helpLink Verknüpfung mit der Online-Hilfe
	 * @param showUpdatesPage	Beim Aufruf des Dialogs direkt zur Update-Seite wechseln (<code>true</code>) oder nicht (<code>false</code>)
	 */
	public SetupDialog(final Window owner, final HelpLink helpLink, final boolean showUpdatesPage) {
		super(owner,Language.tr("SettingsDialog.Title"),null,false,helpLink.dialogSetup);
		createTabsGUI(null,null,null,false,650,625,null,null);
		loadData();
		if (showUpdatesPage) {
			tabs.setSelectedIndex(5);
		}
	}

	@Override
	protected void createTabs(JTabbedPane tabs) {
		JPanel tab, mainarea, p, p2;
		JLabel label;

		/* Dialogseite "Benutzeroberfläche" */
		tabs.addTab(Language.tr("SettingsDialog.Tabs.ProgramStart"),tab=new JPanel(new FlowLayout(FlowLayout.LEFT))); tab.add(mainarea=new JPanel());
		mainarea.setLayout(new BoxLayout(mainarea,BoxLayout.Y_AXIS));

		mainarea.add(p=new JPanel(new FlowLayout(FlowLayout.LEFT)));
		p.add(new JLabel("<html><body><b>"+Language.tr("SettingsDialog.Tabs.ProgramStart.General")+"</b></body></html>"));

		mainarea.add(p=new JPanel(new FlowLayout(FlowLayout.LEFT)));
		p.add(new JLabel(Language.tr("SettingsDialog.Languages")+":"));
		p.add(languages=new JComboBox<>(new String[]{Language.tr("SettingsDialog.Languages.English"),Language.tr("SettingsDialog.Languages.German")}));
		languages.setRenderer(new IconListCellRenderer(new Images[]{Images.LANGUAGE_EN,Images.LANGUAGE_DE}));
		languages.setToolTipText(Language.tr("SettingsDialog.Languages.Info"));

		mainarea.add(p=new JPanel(new FlowLayout(FlowLayout.LEFT)));
		p.add(new JLabel(Language.tr("SettingsDialog.MenuMode")+":"));
		p.add(programMenuMode=new JComboBox<>(new String[]{
				Language.tr("SettingsDialog.MenuMode.Ribbons"),
				Language.tr("SettingsDialog.MenuMode.Classic"),
		}));
		programMenuMode.setRenderer(new IconListCellRenderer(new Images[] {
				Images.SETUP_MENU_RIBBONS,
				Images.SETUP_MENU_CLASSIC
		}));

		mainarea.add(p=new JPanel(new FlowLayout(FlowLayout.LEFT)));
		p.add(new JLabel(Language.tr("SettingsDialog.FontSizes")+":"));
		p.add(fontSizes=new JComboBox<>(new String[]{Language.tr("SettingsDialog.FontSizes.Small")+" (90%)",Language.tr("SettingsDialog.FontSizes.Normal")+" (100%)",Language.tr("SettingsDialog.FontSizes.Larger")+" (110%)",Language.tr("SettingsDialog.FontSizes.VeryLarge")+" (125%)",Language.tr("SettingsDialog.FontSizes.Maximum")+" (150%)"}));
		fontSizes.setRenderer(new IconListCellRenderer(new Images[]{
				Images.SETUP_FONT_SIZE1,
				Images.SETUP_FONT_SIZE2,
				Images.SETUP_FONT_SIZE3,
				Images.SETUP_FONT_SIZE4,
				Images.SETUP_FONT_SIZE5
		}));

		mainarea.add(p=new JPanel(new FlowLayout(FlowLayout.LEFT)));
		p.add(label=new JLabel(Language.tr("SettingsDialog.Theme")+":"));
		final List<String> lookAndFeels=new ArrayList<>();
		lookAndFeels.add(Language.tr("SettingsDialog.Theme.System"));
		lookAndFeels.addAll(Arrays.asList(GUITools.listLookAndFeels()));
		p.add(lookAndFeel=new JComboBox<>(lookAndFeels.toArray(String[]::new)));
		label.setLabelFor(lookAndFeel);
		p.add(lookAndFeelCombinedMenu=new JCheckBox(Language.tr("SettingsDialog.LookAndFeel.MenuInWindowTitle")));
		lookAndFeelCombinedMenu.setToolTipText(Language.tr("SettingsDialog.LookAndFeel.MenuInWindowTitle.Tooltip"));

		mainarea.add(p=new JPanel(new FlowLayout(FlowLayout.LEFT)));
		p.add(new JLabel("<html>("+Language.tr("SettingsDialog.FontSizes.Info")+")</html>"));

		mainarea.add(Box.createVerticalStrut(15));
		mainarea.add(p=new JPanel(new FlowLayout(FlowLayout.LEFT)));
		p.add(new JLabel("<html><body><b>"+Language.tr("SettingsDialog.Tabs.ProgramStart.ProgramStart")+"</b></body></html>"));

		mainarea.add(p=new JPanel(new FlowLayout(FlowLayout.LEFT)));
		p.add(new JLabel(Language.tr("SettingsDialog.WindowSizeProgrmStart")+":"));
		p.add(programStartWindow=new JComboBox<>(new String[]{
				Language.tr("SettingsDialog.WindowSizeProgrmStart.Normal"),
				Language.tr("SettingsDialog.WindowSizeProgrmStart.FullScreen"),
				Language.tr("SettingsDialog.WindowSizeProgrmStart.LastSize")
		}));
		programStartWindow.setRenderer(new IconListCellRenderer(new Images[]{
				Images.SETUP_WINDOW_SIZE_DEFAULT,
				Images.SETUP_WINDOW_SIZE_FULL,
				Images.SETUP_WINDOW_SIZE_LAST
		}));

		mainarea.add(p=new JPanel(new FlowLayout(FlowLayout.LEFT)));
		p.add(new JLabel(Language.tr("SettingsDialog.LoadModelOnProgramStart")+":"));
		p.add(programStartModel=new JComboBox<>(new String[]{
				Language.tr("MainMenu.File.NewModel.EmptyModel"),
				Language.tr("MainMenu.File.NewModel.SmallExampleModel"),
				Language.tr("MainMenu.File.NewModel.MediumExampleModel"),
				Language.tr("MainMenu.File.NewModel.LargeExampleModel"),
				Language.tr("MainMenu.File.NewModel.VeryLargeExampleModel"),
				Language.tr("MainMenu.File.NewModel.SmallErlangCExampleModel"),
				Language.tr("MainMenu.File.NewModel.MediumErlangCExampleModel"),
				Language.tr("MainMenu.File.NewModel.LargeErlangCExampleModel")
		}));
		programStartModel.setRenderer(new IconListCellRenderer(new Images[]{
				Images.MODEL_EMPTY,
				Images.MODEL,
				Images.MODEL,
				Images.MODEL,
				Images.MODEL,
				Images.MODEL,
				Images.MODEL,
				Images.MODEL
		}));

		mainarea.add(p=new JPanel(new FlowLayout(FlowLayout.LEFT)));
		p.add(programStartWelcomePage=new JCheckBox(Language.tr("SettingsDialog.ShowWelcomePage")));

		mainarea.add(Box.createVerticalStrut(15));
		mainarea.add(p=new JPanel(new FlowLayout(FlowLayout.LEFT)));
		p.add(new JLabel("<html><body><b>"+Language.tr("SettingsDialog.Tabs.ProgramStart.Security")+"</b></body></html>"));

		mainarea.add(p=new JPanel(new FlowLayout(FlowLayout.LEFT)));
		p.add(programStartJavaCheck=new JCheckBox(Language.tr("SettingsDialog.TestJavaVersionOnProgramStart")));

		mainarea.add(p=new JPanel(new FlowLayout(FlowLayout.LEFT)));
		p.add(new JLabel(Language.tr("SettingsDialog.JDK.Info")+": "));
		p.add(label=new JLabel("<html><body><a href=\""+Language.tr("SettingsDialog.JDK.Link")+"\">"+Language.tr("SettingsDialog.JDK.Link")+"</a></body></html>"));
		label.setCursor(new java.awt.Cursor(java.awt.Cursor.HAND_CURSOR));
		label.addMouseListener(new MouseAdapter() {
			@Override
			public void mouseClicked(final MouseEvent e) {
				if (SwingUtilities.isLeftMouseButton(e)) {
					try {
						final URI uri=new URI(Language.tr("SettingsDialog.JDK.Link"));
						if (!MsgBox.confirmOpenURL(SetupDialog.this,uri)) return;
						Desktop.getDesktop().browse(uri);
					} catch (IOException | URISyntaxException e1) {}
				}
			}
		});

		/* Dialogseite Simulation */
		tabs.addTab(Language.tr("SettingsDialog.Tabs.Simulation"),tab=new JPanel(new FlowLayout(FlowLayout.LEFT))); tab.add(mainarea=new JPanel()); // "Simulation"
		mainarea.setLayout(new BoxLayout(mainarea,BoxLayout.Y_AXIS));

		mainarea.add(p=new JPanel(new FlowLayout(FlowLayout.LEFT)));
		p.add(new JLabel("<html><body><b>"+Language.tr("SettingsDialog.Tabs.Simulation.Local")+"</b></body></html>"));

		mainarea.add(p=new JPanel(new FlowLayout(FlowLayout.LEFT)));
		p.add(strictCheck=new JCheckBox(Language.tr("SettingsDialog.StrictModelCheck")));
		mainarea.add(p=new JPanel(new FlowLayout(FlowLayout.LEFT)));
		p.add(backgroundSim=new JCheckBox(Language.tr("SettingsDialog.BackgroundSimulation")));
		mainarea.add(p=new JPanel(new FlowLayout(FlowLayout.LEFT)));
		p.add(new JLabel(Language.tr("SettingsDialog.MaxNumberOfThreads")+":"));
		p.add(maxNumberOfThreads=new JTextField(5));
		p.add(new JLabel(Language.tr("SettingsDialog.MaxNumberOfThreads.Info")));
		mainarea.add(p=new JPanel(new FlowLayout(FlowLayout.LEFT)));
		p.add(increaseNumberOfDays=new JCheckBox(Language.tr("SettingsDialog.IncreaseNumberOfDays")));

		mainarea.add(Box.createVerticalStrut(15));
		mainarea.add(p=new JPanel(new FlowLayout(FlowLayout.LEFT)));
		p.add(new JLabel("<html><body><b>"+Language.tr("SettingsDialog.Tabs.Simulation.Network")+"</b></body></html>"));

		mainarea.add(p=new JPanel(new FlowLayout(FlowLayout.LEFT)));
		p.add(simulationLocal=new JRadioButton(Language.tr("SettingsDialog.NetworkSimulation.ModeLocal")));
		mainarea.add(p=new JPanel(new FlowLayout(FlowLayout.LEFT)));
		p.add(simulationRemote=new JRadioButton(Language.tr("SettingsDialog.NetworkSimulation.ModeNetwork")));
		ButtonGroup simulationGroup=new ButtonGroup();
		simulationGroup.add(simulationLocal);
		simulationGroup.add(simulationRemote);
		simulationLocal.setSelected(true);
		mainarea.add(Box.createVerticalStrut(15));
		mainarea.add(p2=new JPanel());
		p2.setLayout(new BoxLayout(p2,BoxLayout.Y_AXIS));
		p2.setBorder(BorderFactory.createTitledBorder(Language.tr("SettingsDialog.NetworkSimulation.Settings")));
		p2.add(p=new JPanel(new FlowLayout(FlowLayout.LEFT)));
		p.add(new JLabel(Language.tr("SettingsDialog.NetworkSimulation.ServerName")+":"));
		p.add(networkName=new JTextField(30));
		p2.add(p=new JPanel(new FlowLayout(FlowLayout.LEFT)));
		p.add(new JLabel(Language.tr("SettingsDialog.NetworkSimulation.Port")+":"));
		p.add(networkPort=new JTextField(5));
		p2.add(p=new JPanel(new FlowLayout(FlowLayout.LEFT)));
		p.add(new JLabel(Language.tr("SettingsDialog.NetworkSimulation.Password")+":"));
		p.add(networkPasswort=new JTextField(30));
		p2.add(p=new JPanel(new FlowLayout(FlowLayout.LEFT)));
		p.add(new JLabel(Language.tr("SettingsDialog.NetworkSimulation.ServerPart")+":"));
		p.add(networkServer=new JTextField(5));
		p.add(networkLocalButton=new JButton(Language.tr("SettingsDialog.NetworkSimulation.ServerPart.Setup")));
		networkLocalButton.addActionListener(new ButtonWork());
		mainarea.add(Box.createVerticalGlue());

		/* Dialogseite Simulationsserver */
		tabs.addTab(Language.tr("SettingsDialog.Tabs.SimulationServer"),tab=new JPanel(new FlowLayout(FlowLayout.LEFT))); tab.add(mainarea=new JPanel());
		mainarea.setLayout(new BoxLayout(mainarea,BoxLayout.Y_AXIS));

		mainarea.add(p=new JPanel(new FlowLayout(FlowLayout.LEFT)));
		p.add(new JLabel("<html>"+Language.tr("SettingsDialog.SimulationServer.Info")+"</html>"));

		mainarea.add(p=new JPanel(new FlowLayout(FlowLayout.LEFT)));
		p.add(new JLabel(Language.tr("SettingsDialog.SimulationServer.Port")+":"));
		p.add(networkServerPort=new JTextField(5));
		mainarea.add(p=new JPanel(new FlowLayout(FlowLayout.LEFT)));
		p.add(new JLabel(Language.tr("SettingsDialog.SimulationServer.PortWeb")+":"));
		p.add(networkServerPortWeb=new JTextField(5));
		mainarea.add(p=new JPanel(new FlowLayout(FlowLayout.LEFT)));
		p.add(new JLabel(Language.tr("SettingsDialog.SimulationServer.Password")+":"));
		p.add(networkServerPasswort=new JTextField(30));
		mainarea.add(p=new JPanel(new FlowLayout(FlowLayout.LEFT)));
		p.add(new JLabel(Language.tr("SettingsDialog.SimulationServer.IPFilter")+":"));
		mainarea.add(p=new JPanel(new FlowLayout(FlowLayout.LEFT)));
		p.add(new JScrollPane(networkServerIPs=new JTextArea(7,60)));
		mainarea.add(p=new JPanel(new FlowLayout(FlowLayout.LEFT)));
		p.add(new JLabel("("+Language.tr("SettingsDialog.SimulationServer.IPFilter.Info")+")"));

		/* Dialogseite "Grafiken" */
		tabs.addTab(Language.tr("SettingsDialog.Tabs.Graphics"),tab=new JPanel(new FlowLayout(FlowLayout.LEFT))); tab.add(mainarea=new JPanel());
		mainarea.setLayout(new BoxLayout(mainarea,BoxLayout.Y_AXIS));

		mainarea.add(p=new JPanel(new FlowLayout(FlowLayout.LEFT)));
		p.add(new JLabel(Language.tr("SettingsDialog.ImageResolution")+":"));
		p.add(imageSize=new JTextField(5));

		/* Dialogseite "Statistik" */
		tabs.add(Language.tr("SettingsDialog.Tabs.GUI.Statistics"),tab=new JPanel(new FlowLayout(FlowLayout.LEFT))); tab.add(mainarea=new JPanel());
		mainarea.setLayout(new BoxLayout(mainarea,BoxLayout.Y_AXIS));

		mainarea.add(p=new JPanel(new FlowLayout(FlowLayout.LEFT)));
		p.add(openWord=new JCheckBox(Language.tr("SettingsDialog.Tabs.Statistics.OpenWord")));

		mainarea.add(p=new JPanel(new FlowLayout(FlowLayout.LEFT)));
		p.add(openODT=new JCheckBox(Language.tr("SettingsDialog.Tabs.Statistics.OpenODT")));

		mainarea.add(p=new JPanel(new FlowLayout(FlowLayout.LEFT)));
		p.add(openExcel=new JCheckBox(Language.tr("SettingsDialog.Tabs.Statistics.OpenExcel")));

		mainarea.add(p=new JPanel(new FlowLayout(FlowLayout.LEFT)));
		p.add(openODS=new JCheckBox(Language.tr("SettingsDialog.Tabs.Statistics.OpenODS")));

		mainarea.add(p=new JPanel(new FlowLayout(FlowLayout.LEFT)));
		p.add(openPDF=new JCheckBox(Language.tr("SettingsDialog.Tabs.Statistics.OpenPDF")));

		/* Dialogseite "Updates" */
		tabs.addTab(Language.tr("SettingsDialog.Tabs.Updates"),tab=new JPanel(new FlowLayout(FlowLayout.LEFT))); tab.add(mainarea=new JPanel());
		mainarea.setLayout(new BoxLayout(mainarea,BoxLayout.Y_AXIS));

		mainarea.add(p=new JPanel(new FlowLayout(FlowLayout.LEFT)));
		p.add(autoUpdate=new JCheckBox(Language.tr("SettingsDialog.AutoUpdate")));

		mainarea.add(p=new JPanel(new FlowLayout(FlowLayout.LEFT)));
		p.add(autoUpdateInfo=new JLabel());

		mainarea.add(p=new JPanel(new FlowLayout(FlowLayout.LEFT)));
		p.add(autoUpdateButton=new JButton(Language.tr("SettingsDialog.SearchForUpdatesNow")));
		autoUpdateButton.addActionListener(new ButtonWork());
		autoUpdateButton.setIcon(Images.SETUP_UPDATE_SEARCH.getIcon());

		p.add(manualUpdateButton=new JButton(Language.tr("SettingsDialog.ManualUpdateDownload")));
		manualUpdateButton.addActionListener(new ButtonWork());
		manualUpdateButton.setVisible(false);
		manualUpdateButton.setIcon(Images.SETUP_UPDATE_MANUAL.getIcon());

		mainarea.add(p=new JPanel(new FlowLayout(FlowLayout.LEFT)));
		p.add(autoUpdateInfo2=new JLabel());

		manualUpdatePopup=new JPopupMenu();
		manualUpdatePopup.add(manualUpdatePopupWebPage=new JMenuItem(Language.tr("SettingsDialog.ManualUpdateDownload.WebPage")));
		manualUpdatePopupWebPage.addActionListener(new ButtonWork());
		manualUpdatePopupWebPage.setIcon(Images.SETUP_UPDATE_WEB_PAGE.getIcon());
		manualUpdatePopup.add(manualUpdatePopupDownload=new JMenuItem(Language.tr("SettingsDialog.ManualUpdateDownload.Download")));
		manualUpdatePopupDownload.addActionListener(new ButtonWork());
		manualUpdatePopupDownload.setIcon(Images.SETUP_UPDATE_MANUAL.getIcon());

		tabs.setIconAt(0,Images.SETUP_PAGE_APPLICATION.getIcon());
		tabs.setIconAt(1,Images.SETUP_PAGE_SIMULATION.getIcon());
		tabs.setIconAt(2,Images.SETUP_PAGE_SERVER.getIcon());
		tabs.setIconAt(3,Images.SETUP_PAGE_IMPORT_EXPORT.getIcon());
		tabs.setIconAt(4,Images.SETUP_PAGE_STATISTICS.getIcon());
		tabs.setIconAt(5,Images.SETUP_PAGE_UPDATE.getIcon());
	}

	@Override
	protected boolean checkData() {
		Integer I;

		I=NumberTools.getInteger(maxNumberOfThreads,false);
		if (I==null || I<0) {
			MsgBox.error(this,Language.tr("SettingsDialog.MaxNumberOfThreads.Invalid.Title"),Language.tr("SettingsDialog.MaxNumberOfThreads.Invalid.Info"));
			return false;
		}

		I=NumberTools.getInteger(imageSize,false);
		if (I==null || I<50 || I>5000) {
			MsgBox.error(this,Language.tr("SettingsDialog.ImageResolution.Invalid.Title"),Language.tr("SettingsDialog.ImageResolution.Invalid.Info"));
			return false;
		}

		return true;
	}

	/**
	 * Lädt die Einstellungen aus dem Setup in den Dialog.
	 */
	private void loadData() {
		switch (setup.startSizeMode) {
		case START_MODE_DEFAULT: programStartWindow.setSelectedIndex(0); break;
		case START_MODE_FULLSCREEN: programStartWindow.setSelectedIndex(1); break;
		case START_MODE_LASTSIZE: programStartWindow.setSelectedIndex(2); break;
		}
		if (setup.ribbonMode) programMenuMode.setSelectedIndex(0); else programMenuMode.setSelectedIndex(1);
		programStartModel.setSelectedIndex(Math.max(-1,Math.min(1,setup.startModusModel))+1);
		programStartWelcomePage.setSelected(setup.startWelcomePage);
		programStartJavaCheck.setSelected(setup.testJavaVersion);

		fontSizes.setSelectedIndex(1);
		if (setup.scaleGUI<1) fontSizes.setSelectedIndex(0);
		if (setup.scaleGUI>1) fontSizes.setSelectedIndex(2);
		if (setup.scaleGUI>1.1) fontSizes.setSelectedIndex(3);
		if (setup.scaleGUI>1.3) fontSizes.setSelectedIndex(4);

		final String[] lookAndFeels2=GUITools.listLookAndFeels();
		lookAndFeel.setSelectedIndex(0);
		for (int i=0;i<lookAndFeels2.length;i++) if (lookAndFeels2[i].equalsIgnoreCase(setup.lookAndFeel)) {
			lookAndFeel.setSelectedIndex(i+1);
			break;
		}
		lookAndFeelCombinedMenu.setSelected(setup.lookAndFeelCombinedMenu);

		if (setup.language==null || setup.language.isEmpty() || setup.language.equalsIgnoreCase("de")) languages.setSelectedIndex(1); else languages.setSelectedIndex(0);

		strictCheck.setSelected(setup.strictCheck);
		backgroundSim.setSelected(setup.backgroundSim);
		maxNumberOfThreads.setText(""+setup.maxNumberOfThreads);
		increaseNumberOfDays.setSelected(setup.increaseNumberOfDays);
		simulationRemote.setSelected(setup.networkUse);
		networkName.setText(setup.networkServer);
		networkPort.setText(""+setup.networkPort);
		networkPasswort.setText(setup.networkPassword);
		networkServer.setText(setup.networkPart);

		networkServerPort.setText(setup.networkServerPort);
		networkServerPortWeb.setText(setup.networkServerPortWeb);
		networkServerPasswort.setText(setup.networkServerPassword);

		StringBuilder IPs=new StringBuilder();
		if (setup.networkPermittedIPs!=null && setup.networkPermittedIPs.length>0) {
			IPs.append(setup.networkPermittedIPs[0]);
			for (int i=1;i<setup.networkPermittedIPs.length;i++) IPs.append("\n"+setup.networkPermittedIPs[i]);
		}
		networkServerIPs.setText(IPs.toString());

		imageSize.setText(""+Math.min(5000,Math.max(50,setup.imageSize)));

		openWord.setSelected(setup.openWord);
		openODT.setSelected(setup.openODT);
		openExcel.setSelected(setup.openExcel);
		openODS.setSelected(setup.openODS);
		openPDF.setSelected(setup.openPDF);

		autoUpdate.setSelected(setup.updateAutomatic);
		loadDataAutoUpdate();
	}

	/**
	 * Aktualisiert die Anzeige des Update-Fortschritts.
	 */
	private void loadDataAutoUpdate() {
		if (!UpdateSystem.getUpdateSystem().active) {
			autoUpdateInfo2.setText("<html><body>"+Language.tr("SettingsDialog.AutoUpdate.WindowsOnly")+"</body></html>");
			autoUpdate.setEnabled(false);
			autoUpdate.setSelected(false);
		}

		String s=UpdateSystem.getUpdateSystem().getNewVersion();
		if (s!=null) {
			if (s.isEmpty()) {
				autoUpdateInfo.setText(Language.tr("SettingsDialog.Update.NewestVersion"));
			} else {
				if (UpdateSystem.getUpdateSystem().active && UpdateSystem.getUpdateSystem().isUserDirWriteable()) {
					if (downloadInfoTimer==null) {
						autoUpdateButton.setEnabled(false);
						startDownloadInfo(true);
					}
				} else {
					autoUpdateInfo.setText("<html><body>"+String.format(Language.tr("SettingsDialog.Update.ManualUpdateAvailable"),s)+"</body></html>");
					manualUpdateButton.setVisible(true);
				}
			}
			return;
		}
	}

	/**
	 * Zeigt Informationen zum Download an.
	 * @param isAutoUpdate	Handelt es sich bei dem Download um ein Auto-Update?
	 */
	private void startDownloadInfo(final boolean isAutoUpdate) {
		final String statusDone=isAutoUpdate?Language.tr("SettingsDialog.Update.UpdateOnRestart"):Language.tr("SettingsDialog.Update.ReadyForManualInstall");
		final String statusFailed=Language.tr("SettingsDialog.Update.Failed");
		final String statusProgress=Language.tr("SettingsDialog.Update.Progress");
		downloadInfoTimer=new Timer(250,e-> {
			int progress=UpdateSystem.getUpdateSystem().getDownloadState();
			String ver=UpdateSystem.getUpdateSystem().getNewVersion();
			String s="";
			if (progress>=100) {
				s=String.format(statusDone,ver);
			} else {
				if (progress<0) s=String.format(statusFailed,ver); else s=String.format(statusProgress,NumberTools.formatPercent(progress/100.0),ver);
			}
			autoUpdateInfo.setText(s);
			if (progress>=100 || progress<0) downloadInfoTimer.stop();
		});
		downloadInfoTimer.start();
	}

	@Override
	protected void storeData() {
		String[] list;
		List<String> k;
		Integer I;

		switch (programStartWindow.getSelectedIndex()) {
		case 0: setup.startSizeMode=StartMode.START_MODE_DEFAULT; break;
		case 1: setup.startSizeMode=StartMode.START_MODE_FULLSCREEN; break;
		case 2: setup.startSizeMode=StartMode.START_MODE_LASTSIZE; break;
		}
		setup.ribbonMode=(programMenuMode.getSelectedIndex()==0);
		setup.startModusModel=programStartModel.getSelectedIndex()-1;
		setup.startWelcomePage=programStartWelcomePage.isSelected();
		setup.testJavaVersion=programStartJavaCheck.isSelected();
		switch (fontSizes.getSelectedIndex()) {
		case 0: setup.scaleGUI=0.9; break;
		case 1: setup.scaleGUI=1; break;
		case 2: setup.scaleGUI=1.1; break;
		case 3: setup.scaleGUI=1.25; break;
		case 4: setup.scaleGUI=1.5; break;
		}
		if (lookAndFeel.getSelectedIndex()==0) setup.lookAndFeel=""; else setup.lookAndFeel=(String)lookAndFeel.getSelectedItem();
		setup.lookAndFeelCombinedMenu=lookAndFeelCombinedMenu.isSelected();
		setup.language=(languages.getSelectedIndex()==1)?"de":"en";

		setup.strictCheck=strictCheck.isSelected();
		setup.backgroundSim=backgroundSim.isSelected();
		I=NumberTools.getInteger(maxNumberOfThreads,false);
		setup.increaseNumberOfDays=increaseNumberOfDays.isSelected();
		if (I!=null) setup.maxNumberOfThreads=I;
		setup.networkUse=simulationRemote.isSelected();
		setup.networkServer=networkName.getText().trim();
		setup.networkPort=networkPort.getText().trim();
		setup.networkPassword=networkPasswort.getText().trim();
		setup.networkPart=networkServer.getText().trim();

		setup.networkServerPort=networkServerPort.getText().trim();
		setup.networkServerPortWeb=networkServerPortWeb.getText().trim();
		setup.networkServerPassword=networkServerPasswort.getText().trim();
		list=networkServerIPs.getText().split("\n");
		k=new ArrayList<>();	for (int i=0;i<list.length;i++) if (!list[i].isEmpty()) k.add(list[i]);
		setup.networkPermittedIPs=k.toArray(String[]::new);

		I=NumberTools.getInteger(imageSize,false);
		if (I!=null) setup.imageSize=I;

		setup.openWord=openWord.isSelected();
		setup.openODT=openODT.isSelected();
		setup.openExcel=openExcel.isSelected();
		setup.openODS=openODS.isSelected();
		setup.openPDF=openPDF.isSelected();

		if (autoUpdate.isEnabled()) setup.updateAutomatic=autoUpdate.isSelected();

		setup.saveSetup();
	}

	/**
	 * Führt jetzt eine Aktualisierung aus.
	 */
	private void doUpdateNow() {
		UpdateSystem update=UpdateSystem.getUpdateSystem();
		if (update.getNewVersion()==null) update.checkUpdate(true);
		while (update.getNewVersion()==null) try {Thread.sleep(500);} catch (InterruptedException e) {}
		loadDataAutoUpdate();
	}

	/**
	 * Führt den lokal/remote Netzwerk-Balance Test durch.
	 * @see #networkLocalButton
	 */
	private void autoSetupNetworkBalance() {
		if (!checkData()) return;

		String server=networkName.getText().trim();
		String port=networkPort.getText();
		String password=networkPasswort.getText().trim();

		SetupDialogNetworkBalance dialog=new SetupDialogNetworkBalance(this,server,port,password);
		double[] d=dialog.getServerParts();
		if (d!=null && d.length>0) {
			StringBuilder s=new StringBuilder();
			s.append(NumberTools.formatNumber(d[0]*100,0)+"%");
			for (int i=1;i<d.length;i++) s.append(";"+NumberTools.formatNumber(d[i]*100,0)+"%");
			networkServer.setText(s.toString());
		}
	}

	/**
	 * Führt ein manuelles Update durch.
	 */
	private void doManualUpdate() {
		JFileChooser fc=new JFileChooser();
		CommonVariables.initialDirectoryToJFileChooser(fc);
		fc.setDialogTitle(Language.tr("SettingsDialog.Update.ManualUpdateAvailable.Folder"));
		fc.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY);
		if (fc.showSaveDialog(owner)!=JFileChooser.APPROVE_OPTION) return;
		CommonVariables.initialDirectoryFromJFileChooser(fc);
		File file=fc.getSelectedFile();

		startDownloadInfo(false);
		UpdateSystem.getUpdateSystem().checkUpdate(file);

		MsgBox.info(this,Language.tr("SettingsDialog.Update.ManualUpdateAvailable.DownloadTitle"),Language.tr("SettingsDialog.Update.ManualUpdateAvailable.DownloadInfo"));
	}

	/**
	 * Öffnet die Update-Webseite.
	 */
	private void openDownloadWebPage() {
		try {
			final URL url=new URI(UpdateSystem.defaultProtocollHomepage+"://"+UpdateSystem.wwwHomeURL).toURL();
			if (!MsgBox.confirmOpenURL(SetupDialog.this,url)) return;
			Desktop.getDesktop().browse(url.toURI());
		} catch (IOException | URISyntaxException e1) {
			MsgBox.error(SetupDialog.this,Language.tr("Window.Info.NoInternetConnection"),String.format(Language.tr("Window.Info.NoInternetConnection.Address"),UpdateSystem.defaultProtocollHomepage+"://"+UpdateSystem.wwwHomeURL));
		}
	}

	/**
	 * Reagiert auf Klicks auf die Schaltflächen.
	 * @see SetupDialog#autoUpdateButton
	 *@see SetupDialog#manualUpdateButton
	 *@see SetupDialog#manualUpdatePopupWebPage
	 *@see SetupDialog#manualUpdatePopupDownload
	 *@see SetupDialog#networkLocalButton
	 */
	private class ButtonWork implements ActionListener {
		/**
		 * Konstruktor der Klasse
		 */
		public ButtonWork() {
			/*
			 * Wird nur benötigt, um einen JavaDoc-Kommentar für diesen (impliziten) Konstruktor
			 * setzen zu können, damit der JavaDoc-Compiler keine Warnung mehr ausgibt.
			 */
		}

		@Override
		public void actionPerformed(ActionEvent e) {
			if (e.getSource()==autoUpdateButton) {doUpdateNow(); return;}
			if (e.getSource()==manualUpdateButton) {manualUpdatePopup.show(manualUpdateButton,0,manualUpdateButton.getHeight()); return;}
			if (e.getSource()==manualUpdatePopupWebPage) {openDownloadWebPage(); return;}
			if (e.getSource()==manualUpdatePopupDownload) {doManualUpdate(); return;}
			if (e.getSource()==networkLocalButton) {autoSetupNetworkBalance(); return;}
		}
	}
}
