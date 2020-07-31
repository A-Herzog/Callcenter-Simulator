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
package tools;

import java.awt.Dimension;
import java.awt.Frame;
import java.awt.Point;
import java.io.File;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Semaphore;

import org.w3c.dom.CDATASection;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;

import language.Language;
import language.LanguageStaticLoader;
import language.Messages_Java11;
import mathtools.NumberTools;
import systemtools.SetupBase;

/**
 * Diese Klasse kapselt alle Setup-Daten des Programms und automatisiert das Laden und Speichern der Daten
 * @see SetupBase
 * @author Alexander Herzog
 */
public class SetupData extends SetupBase {
	/**
	 * Optionale nutzerdefiniere Konfigurationsdatei
	 */
	public static File userConfigFile=null;

	/**
	 * @see SetupData#startSizeMode
	 */
	public enum StartMode {
		/** Starten des Programms mit Vorgabe-Fenstergröße */
		START_MODE_DEFAULT,

		/** Starten des Programms im Vollbildmodus */
		START_MODE_FULLSCREEN,

		/** Wiederherstellung der letzten Fenstergröße beim Programmstart */
		START_MODE_LASTSIZE
	}

	private static final boolean WRITE_ENGLISH_KEYS=true;

	/* Setup-Einstellungen */
	/* ----- ----- ----- ----- ----- */

	/**
	 * Programmsprache
	 */
	public String language="";

	/**
	 * Fenstergröße beim Programmstart
	 * @see StartMode#START_MODE_DEFAULT
	 * @see StartMode#START_MODE_FULLSCREEN
	 * @see StartMode#START_MODE_LASTSIZE
	 */
	public StartMode startSizeMode=StartMode.START_MODE_DEFAULT;

	/**
	 * Willkommensseite beim Programmstart anzeigen?
	 */
	public boolean startWelcomePage=true;

	/**
	 * Soll geprüft werden, ob eine veraltete Java-Version verwendet wird?
	 */
	public boolean testJavaVersion=true;

	/**
	 * Beim Start zu ladendes Beispielmodell (Wert zwischen 0 und 6 jeweils einschließlich für ein Beispielmodell oder ein anderer Wert für ein leeres Modell)
	 */
	public int startModusModel=0;

	/**
	 * Ribbon-basiertes Menü verwenden?
	 */
	public boolean ribbonMode=true;

	/**
	 * Skalierung der Programmoberfläche
	 */
	public double scaleGUI=1;

	/**
	 * Ist startSizeMode=START_MODE_LASTSIZE gewählt, so wird hier gespeichert, ob das Fenster im Vollbildmodus dargestellt wird oder nicht
	 */
	public int lastSizeMode=Frame.NORMAL;

	/**
	 * Ist startSizeMode=START_MODE_LASTSIZE gewählt, so wird hier die letzte Position des Fensters gespeichert
	 */
	public Point lastPosition=new Point(0,0);

	/**
	 * Ist startSizeMode=START_MODE_LASTSIZE gewählt, so wird hier die letzte Größe des Fensters gespeichert
	 */
	public Dimension lastSize=new Dimension(0,0);

	/**
	 * Ist startSizeMode=START_MODE_LASTSIZE gewählt, so wird hier die letzte Position des Fensters gespeichert
	 */
	public int imageSize=1000;

	/**
	 * Gibt an, ob die Bilder bei HTML-Reports inline oder als separate Dateien ausgegeben werden sollen.
	 */
	public boolean imagesInline=true;

	/**
	 * Strenge Modellprüfung
	 */
	public boolean strictCheck=true;

	/**
	 * Automatische Hintergrundsimulation verwenden?
	 */
	public boolean backgroundSim=true;

	/**
	 * Automatische Hintergrundsimulation auch dann verwenden, wenn über das Netzwerk simuliert wird?
	 */
	public boolean backgroundSimInNetworkMode=false;

	/**
	 * Maximalanzahl an Threads (0 bedeutet "keine künstliche Limitierung)
	 */
	public int maxNumberOfThreads=0;

	/**
	 * Anzahl an simulierten Tagen ggf. automatisch erhöhen, um CPU-Kerne optimal auszunutzen
	 */
	public boolean increaseNumberOfDays=true;

	/**
	 * Mehr Threads als logische Kerne verwenden? (Um so ggf. die Auslastung zu maximieren.)
	 */
	public boolean moreThreads=false;

	/** Netzwerk-Simulation verwenden? */
	public boolean networkUse=false;
	/** Adresse des Servers oder durch ";" getrennte Adressen der Server */
	public String networkServer="";
	/** Port auf dem Server oder durch ";" getrennte Ports auf den Servern */
	public String networkPort="6783";
	/** Port für den Betrieb als Rechen-Server */
	public String networkServerPort="6783";
	/** Port für den Betrieb als Web-Server */
	public String networkServerPortWeb="80";
	/**  Passwort zum Zugriff auf den Server oder durch ";" getrennte Passwörter für den Zugriff auf die Server */
	public String networkPassword="";
	/** Passwort für den Rechen-Server */
	public String networkServerPassword="";
	/** Maximale Anzahl an Threads die der Rechen-Server nutzen soll (0 bedeutet "keine künstliche Limitierung) */
	public int networkMaxThreads=0;
	/** Für den Zugriff auf den Rechen-Server zugelassen IP-Adressen (<code>null</code> bedeutet, dass keine IP-Filterung erfolgt) */
	public String[] networkPermittedIPs=null;
	/** Anteil der Rechenlast für den oder die Rechen-Server */
	public String networkPart="1";

	/** Filterskripte für die Statistikergebnisse */
	public String[] filter=new String[10];
	/** 0-basierter Index des zuletzt verwendeten Statistikergebnis-Filterskripts */
	public int lastFilterIndex=0;

	/** Statistikergebnisse nach Simulation automatisch speichern */
	public boolean autoSaveStatistic=false;
	/** Ausgabeordner für automatische Statistikspeicherung */
	public String autoSaveStatisticFolder="";
	/** Filterskript bei automatischer Statistikspeicherung anwenden */
	public boolean autoSaveFilter=false;
	/** Filterskript für automatische Statistikspeicherung */
	public String autoSaveFilterScript="";
	/** Ausgabeverzeichnis für Filterskript für automatische Statistikspeicherung */
	public String autoSaveFilterOutput="";

	/** Filterskript auf Batch-Ergebnisse anwenden */
	public boolean batchSaveFilter=false;
	/** Datei des Filterskripts für die Batch-Ergebnisse */
	public String batchSaveFilterScript="";
	/** Ausgabeverzeichnis für die Ergebnisse des Filterskripts für die Batch-Ergebnisse */
	public String batchSaveFilterOutput="";
	/** Arbeitsverzeichnis für die Batch-Verarbeitung */
	public String batchFolder="";
	/** Ausgabeverzeichnis für die Parameterreihen-Simulation */
	public String batchXMLFolder="";
	/** XML-Element für die Parameterreihen-Simulation */
	public String batchXMLElement="";
	/** Art des XML-Elements für die Parameterreihen-Simulation */
	public int batchXMLElementType=-1;
	/** Startwert für den Inhalt des XML-Elements für die Parameterreihen-Simulation */
	public String batchXMLElementFrom="";
	/** Endwert für den Inhalt des XML-Elements für die Parameterreihen-Simulation */
	public String batchXMLElementTo="";
	/** Schrittweite für die Variation des Inhalts des XML-Elements für die Parameterreihen-Simulation */
	public String batchXMLElementStepSize="";

	/** Anzahl der Wiederholungen bei der Varianzanalyse */
	public int varianzAnalyseNumber=20;

	/** Automatisch nach Updates suchen? */
	public boolean updateAutomatic=true;
	/** Zeitpunkt der letzten automatischen Update-Prüfung */
	public String updateLastCheck="";

	/** Filterung der Einträge im Statistik-Baum */
	public String statisticTreeFilter="";
	/** Filterung der Einträge im Optimierer-Statistik-Baum */
	public String optimizerTreeFilter="";
	/** Filterung bzw. Auswahl der Einträge in der Report-Generator-Liste */
	public String simReportTreeFilter="";
	/** Filterung bzw. Auswahl der Einträge in der Modelleigenschaften-Report-Generator-Liste */
	public String modelReportTreeFilter="";
	/** Filterung bzw. Auswahl der Einträge in der Optimierer-Report-Generator-Liste */
	public String optimizeReportTreeFilter="";
	/** Filterung bzw. Auswahl der Einträge in der Verbundene-Simulator-Report-Generator-Liste */
	public String connectedReportTreeFilter="";
	/** Filterung der Einträge für die Simulationsergebnis-Übersicht-Statistik-Textseite */
	public String simOverviewFilter="";
	/** Filterung der Einträge für die Konfidenzintervall-Statistik-Textseite */
	public String confidenceFilter="";
	/** Filterung der Einträge für die Kundenanzahl-Statistik-Textseite */
	public String clientsCountFilter="";
	/** Filterung der Einträge für die Wartezeiten-Statistik-Textseite */
	public String clientsWaitingTimeFilter="";

	/** Optional abweichende URL für die js-Dateien für den Statistik-Web-Viewer-Export */
	public String viewerURL="";

	/**
	 * Liste der zuletzt verwendeten Dateien
	 */
	public String[] lastFiles=null;

	/**
	 * Option auf Statistik-Text-Viewer-Seiten: "Öffnen mit Word"
	 */
	public boolean openWord;

	/**
	 * Option auf Statistik-Text-Viewer-Seiten: "Öffnen mit OpenOffice/LibreOffice"
	 */
	public boolean openODT;

	/**
	 * Option auf Statistik-Tabellen-Viewer-Seiten: "Öffnen mit Excel"
	 */
	public boolean openExcel;

	/**
	 * Option auf Statistik-Tabellen-Viewer-Seiten: "Öffnen mit OpenOffice/LibreOffice"
	 */
	public boolean openODS;

	/**
	 * Letzter Fehler
	 * (Hier wird die Setup-Datei als Logdatei für solche Ereignisse verwendet.)
	 */
	public String lastError=null;

	/**
	 * Proxy-Server verwenden ja/nein (unabhängig von den anderen Proxy-Einstellungen kann die Server-Verwendung deaktiviert werden)
	 */
	public boolean useProxy;

	/**
	 * Name des Proxy-Server (Server wird nur verwendet, wenn {@link SetupData#useProxy} wahr ist)
	 */
	public String proxyHost;

	/**
	 * Port des Proxy-Server (Server wird nur verwendet, wenn {@link SetupData#useProxy} wahr ist)
	 */
	public int proxyPort;

	/**
	 * Nutzername für Anmeldung am Proxy-Server (Server wird nur verwendet, wenn {@link SetupData#useProxy} wahr ist)
	 */
	public String proxyUser;

	/**
	 * Passwort für Anmeldung am Proxy-Server (Server wird nur verwendet, wenn {@link SetupData#useProxy} wahr ist)
	 */
	public String proxyPassword;


	/* ----- ----- ----- ----- ----- */

	private static volatile SetupData setup=null;
	private static final Semaphore mutex=new Semaphore(1);

	private SetupData(final boolean loadSetupFile) {
		super();
		if (loadSetupFile) {
			loadSetupFromFile();
			autoSetLanguage();
		}
	}

	@Override
	protected void resetDataToDefaults() {
		language="";

		startSizeMode=StartMode.START_MODE_DEFAULT;
		startWelcomePage=true;
		testJavaVersion=true;
		startModusModel=0;
		ribbonMode=true;
		scaleGUI=1;

		lastSizeMode=Frame.NORMAL;
		lastPosition=new Point(0,0);
		lastSize=new Dimension(0,0);

		imageSize=1000;
		imagesInline=true;

		strictCheck=true;

		backgroundSim=true;
		backgroundSimInNetworkMode=false;
		maxNumberOfThreads=0;
		increaseNumberOfDays=true;
		moreThreads=false;

		networkUse=false;
		networkServer="";
		networkPort="6783";
		networkServerPort="6783";
		networkServerPortWeb="80";
		networkPassword="";
		networkServerPassword="";
		networkMaxThreads=0;
		networkPermittedIPs=null;
		networkPart="1";

		filter=new String[10];
		lastFilterIndex=0;

		autoSaveStatistic=false;
		autoSaveStatisticFolder="";
		autoSaveFilter=false;
		autoSaveFilterScript="";
		autoSaveFilterOutput="";

		batchSaveFilter=false;
		batchSaveFilterScript="";
		batchSaveFilterOutput="";
		batchFolder="";
		batchXMLFolder="";
		batchXMLElement="";
		batchXMLElementType=-1;
		batchXMLElementFrom="";
		batchXMLElementTo="";
		batchXMLElementStepSize="";

		varianzAnalyseNumber=20;


		updateLastCheck="";

		statisticTreeFilter="";
		optimizerTreeFilter="";
		simReportTreeFilter="";
		modelReportTreeFilter="";
		optimizeReportTreeFilter="";
		connectedReportTreeFilter="";
		simOverviewFilter="";
		confidenceFilter="";
		clientsCountFilter="";
		clientsWaitingTimeFilter="";

		viewerURL="";

		lastFiles=null;

		openWord=true;
		openODT=false;
		openExcel=true;
		openODS=false;

		lastError=null;

		useProxy=false;
		proxyHost="";
		proxyPort=8080;
		proxyUser="";
		proxyPassword="";
	}

	private boolean autoSetLanguageActive=false;

	/**
	 * Gibt an, ob die Programmsprache beim Programmstart gemäß der Systemsprache automatisch
	 * eingestellt wurde (oder ob die Programmsprache aus dem Setup geladen wurde)
	 * @return	Gibt <code>true</code> zurück, wenn die Programmsprache automatisch eingestellt wurde
	 */
	public boolean languageWasAutomaticallySet() {
		return autoSetLanguageActive;
	}

	/**
	 * Setzt den Status "Sprache wurde automatisch gesetzt" zurück.
	 */
	public void resetLanguageWasAutomatically() {
		autoSetLanguageActive=false;
	}

	private void autoSetLanguage() {
		if (!language.isEmpty()) return;
		final String userLanguage=System.getProperty("user.language");
		if (Language.isSupportedLanguage(userLanguage)) language=userLanguage.toLowerCase(); else language="en";
		autoSetLanguageActive=true;
		saveSetup();
	}

	/**
	 * Liefert das Setup-Singleton-Objekt zurück.<br>
	 * Der Aufruf wird über ein Mutex-Objekt abgesichert, ist also thread-safe.
	 * @return	Setup-Objekt
	 */
	public static SetupData getSetup() {
		return getSetup(true);
	}

	/**
	 * Liefert das Setup-Singleton-Objekt zurück
	 * @param lock	Gibt an, ob das evtl. notwendige Erstellen des Setup-Objektes über ein Mutex-Objekt vor Parallelaufrufen geschützt werden soll
	 * @return	Setup-Objekt
	 */
	public static SetupData getSetup(final boolean lock) {
		if (!lock) {
			if (setup==null) setup=new SetupData(true);
			return setup;
		}

		mutex.acquireUninterruptibly();
		try {
			if (setup==null) setup=new SetupData(true);
			return setup;
		} finally {
			mutex.release();
		}
	}

	/**
	 * Setzt das Setup auf die Defaultwerte zurück
	 */
	public static void resetSetup() {
		setup=new SetupData(false);
		setup.saveSetup();
	}

	/**
	 * Liefert den Pfadnamen des Verzeichnisses in dem sich die jar-Programmdatei befindet.
	 * @return	Pfad der Programmdatei
	 */
	public static File getProgramFolder() {
		try {
			final File source=new File(SetupData.class.getProtectionDomain().getCodeSource().getLocation().toURI());
			if (source.toString().toLowerCase().endsWith(".jar")) return new File(source.getParent());
		} catch (URISyntaxException e1) {}
		return new File(System.getProperty("user.dir"));
	}

	/**
	 * Name für den Ordner unterhalb von %APPDATA%, der für Programmeinstellungen verwendet
	 * werden soll, wenn das Programm von innerhalb des "Programme"-Verzeichnisses ausgeführt wird.
	 */
	private final static String USER_CONFIGURATION_FOLDER_NAME="Callcenter Simulator";

	/**
	 * Liefert den Pfadnamen des Verzeichnisses in dem die Einstellungsdatei abgelegt werden soll.
	 * @return	Pfad der Einstellungendatei
	 */
	public static File getSetupFolder() {
		final File programFolder=getProgramFolder();

		/* Abweichender Ordner nur unter Windows */
		final String osName=System.getProperty("os.name");
		if (osName==null) return programFolder;
		if (!osName.toLowerCase().contains("windows")) return programFolder;

		/* Programmverzeichnis ist Unterordner des home-Verzeichnisses */
		final String homeFolder=System.getProperty("user.home");
		if (homeFolder==null) return programFolder;
		final String s1=homeFolder.toString().toLowerCase();
		final String s2=programFolder.toString().toLowerCase();
		if (s1.equals(s2.substring(0,Math.min(s1.length(),s2.length())))) return programFolder;

		/* Alternativen Speicherort */
		final String appData=System.getenv("APPDATA");
		if (appData==null) return programFolder;
		final File appDataFolder=new File(appData);
		if (!appDataFolder.isDirectory()) return programFolder;
		final File folder=new File(appDataFolder,USER_CONFIGURATION_FOLDER_NAME);
		if (!folder.isDirectory()) {
			if (!folder.mkdir()) return programFolder;
		}
		if (!folder.isDirectory()) return programFolder;
		return folder;
	}

	/**
	 * Dateiname der Setup-Datei
	 */
	public static final String SETUP_FILE_NAME="CallcenterSimulator.cfg";


	@Override
	protected File getSetupFile() {
		return new File(getSetupFolder(),SETUP_FILE_NAME);
	}

	@Override
	protected void loadSetupFromXML(final Element root) {
		String[] filterKeyNames=new String[filter.length];
		for (int i=0;i<filter.length;i++) {
			filter[i]="";
			filterKeyNames[i]=(i==0)?"Filter":("Filter"+(i+1));
		}
		final List<String> ips=new ArrayList<String>();
		final List<String> files=new ArrayList<String>();

		NodeList l=root.getChildNodes();
		for (int i=0; i<l.getLength();i++) {
			if (!(l.item(i) instanceof Element)) continue;
			Element e=(Element)l.item(i);
			String s=e.getNodeName();

			if (s.equalsIgnoreCase("Sprache") || s.equalsIgnoreCase("Language")) {
				String t=e.getTextContent().toLowerCase();
				if (t.equals("de") || t.equals("en")) language=t;
				continue;
			}

			if (s.equalsIgnoreCase("Vollbild") || s.equalsIgnoreCase("Fullscreen")) {
				Integer j=NumberTools.getInteger(e.getTextContent());
				if (j!=null) {
					if (j==1) startSizeMode=StartMode.START_MODE_FULLSCREEN;
					if (j==2) startSizeMode=StartMode.START_MODE_LASTSIZE;
				}
				continue;
			}

			if (s.equalsIgnoreCase("LetzteFenstergroesse") || s.equalsIgnoreCase("LastWindowSize")) {
				Integer j=NumberTools.getInteger(e.getAttribute("Modus"));
				if (j!=null && (j==Frame.NORMAL || j==Frame.MAXIMIZED_HORIZ || j==Frame.MAXIMIZED_VERT || j==Frame.MAXIMIZED_BOTH)) lastSizeMode=j;
				j=NumberTools.getNotNegativeInteger(e.getAttribute("X"));
				if (j!=null) lastPosition.x=j;
				j=NumberTools.getNotNegativeInteger(e.getAttribute("Y"));
				if (j!=null) lastPosition.y=j;
				j=NumberTools.getNotNegativeInteger(e.getAttribute("Width"));
				if (j==null) j=NumberTools.getNotNegativeInteger(e.getAttribute("Breite"));
				if (j!=null) lastSize.width=j;
				j=NumberTools.getNotNegativeInteger(e.getAttribute("Height"));
				if (j==null) j=NumberTools.getNotNegativeInteger(e.getAttribute("Hoehe"));
				if (j!=null) lastSize.height=j;
			}

			if (s.equalsIgnoreCase("Willkommensseite") || s.equalsIgnoreCase("WelcomePage")) {
				Integer j=NumberTools.getInteger(e.getTextContent());
				if (j!=null) startWelcomePage=(j!=0);
				continue;
			}

			if (s.equalsIgnoreCase("JavaVersionCheck")) {
				Integer j=NumberTools.getInteger(e.getTextContent());
				if (j!=null) testJavaVersion=(j!=0);
				continue;
			}

			if (s.equalsIgnoreCase("Start")) {
				Integer j=NumberTools.getInteger(e.getTextContent());
				if (j!=null) startModusModel=Math.min(1,Math.max(-1,j-1));
				continue;
			}

			if (s.equalsIgnoreCase("Ribbons")) {
				ribbonMode=loadBoolean(e.getTextContent(),true);
				continue;
			}

			if (s.equalsIgnoreCase("Skalierung") || s.equalsIgnoreCase("Scale")) {
				Double d=NumberTools.getExtProbability(NumberTools.systemNumberToLocalNumber(e.getTextContent()));
				if (d!=null) scaleGUI=Math.min(2,Math.max(0.5,d));
				continue;
			}

			if (s.equalsIgnoreCase("Bilder") || s.equalsIgnoreCase("Images")) {
				Integer j=NumberTools.getInteger(e.getTextContent());
				if (j!=null) imageSize=Math.min(5000,Math.max(50,j));
				imagesInline=loadBoolean(e.getAttribute("Inline"),true);
				continue;
			}

			if (s.equalsIgnoreCase("StrengePruefung") || s.equalsIgnoreCase("StrictCheck")) {
				strictCheck=loadBoolean(e.getTextContent(),true);
				continue;
			}

			if (s.equalsIgnoreCase("HintergrundSimulation") || s.equalsIgnoreCase("BackgroundSimulation")) {
				backgroundSim=loadBoolean(e.getTextContent(),true);
				backgroundSimInNetworkMode=loadBoolean(e.getAttribute("AuchImNetzwerk"),false) || loadBoolean(e.getAttribute("InNetworkMode"),false);
				continue;
			}

			if (s.equalsIgnoreCase("MaximaleThreadAnzahl") || s.equalsIgnoreCase("MaximumNumberOfThreads")) {
				Integer j=NumberTools.getInteger(e.getTextContent());
				if (j!=null) maxNumberOfThreads=Math.min(1000,Math.max(0,j));
				continue;
			}

			if (s.equalsIgnoreCase("MehrThreadsAlsCPUKerne") || s.equalsIgnoreCase("MoreThreadsThanCPUCores")) {
				moreThreads=loadBoolean(e.getTextContent(),false);
				continue;
			}

			if (s.equalsIgnoreCase("AnzahlAnTagenErhoehen") || s.equalsIgnoreCase("IncreaseNumberOfDays")) {
				increaseNumberOfDays=loadBoolean(e.getTextContent(),true);
				continue;
			}

			if (s.equalsIgnoreCase("Network")) {
				networkUse=loadBoolean(e.getAttribute("verwenden"),false) || loadBoolean(e.getAttribute("use"),false);
				networkServer=e.getAttribute("Server");
				String t=e.getAttribute("Port");
				if (t!=null && !t.trim().isEmpty()) networkPort=t;
				t=e.getAttribute("ServerPort");
				if (t!=null && !t.trim().isEmpty()) networkServerPort=t;
				t=e.getAttribute("WebServerPort");
				if (t!=null && !t.trim().isEmpty()) networkServerPortWeb=t;
				networkPassword=e.getAttribute("Passwort");
				if (networkPassword.isEmpty()) networkPassword=e.getAttribute("Password");
				networkServerPassword=e.getAttribute("ServerPasswort");
				if (networkServerPassword.isEmpty()) networkServerPassword=e.getAttribute("ServerPassword");
				Integer I=NumberTools.getNotNegativeInteger(e.getAttribute("MaxThreads"));
				if (I!=null) networkMaxThreads=I;
				t=e.getAttribute("ServerAnteil");
				if (t==null || t.trim().isEmpty()) t=e.getAttribute("ServerPart");
				if (t!=null && !t.trim().isEmpty()) networkPart=t;
				continue;
			}

			if (s.equalsIgnoreCase("NetzwerkIPFilter") || s.equalsIgnoreCase("NetworkIPFilter")) {
				String t=e.getTextContent();
				if (t!=null && !t.isEmpty()) ips.add(t);
				continue;
			}

			for (int m=0;m<filterKeyNames.length;m++) if (s.equalsIgnoreCase(filterKeyNames[m])) {
				NodeList l2=e.getChildNodes();
				for (int j=0; j<l2.getLength();j++) {
					if (!(l2.item(j) instanceof CDATASection)) continue;
					filter[m]=((CDATASection)l2.item(j)).getData();
					break;
				}
				continue;
			}

			if (s.equalsIgnoreCase("AutomatischSpeichern") || s.equalsIgnoreCase("AutoSave")) {
				NodeList l2=e.getChildNodes();
				for (int j=0; j<l2.getLength();j++) {
					if (!(l2.item(j) instanceof Element)) continue;
					Element e2=(Element)l2.item(j);
					String t=e2.getNodeName();

					if (t.equalsIgnoreCase("Statistik") || t.equalsIgnoreCase("Statistic")) {
						autoSaveStatistic=loadBoolean(e2.getTextContent(),false);
						continue;
					}
					if (t.equalsIgnoreCase("StatistikOrdner") || t.equalsIgnoreCase("StatisticFolder")) {
						String u=e2.getTextContent();
						if (u!=null) autoSaveStatisticFolder=u;
						continue;
					}
					if (t.equalsIgnoreCase("Filter")) {
						autoSaveFilter=loadBoolean(e2.getTextContent(),false);
						continue;
					}
					if (t.equalsIgnoreCase("FilterScript")) {
						String u=e2.getTextContent();
						if (u!=null) autoSaveFilterScript=u;
						continue;
					}
					if (t.equalsIgnoreCase("FilterOrdner") || t.equalsIgnoreCase("FilterFolder")) {
						String u=e2.getTextContent();
						if (u!=null) autoSaveFilterOutput=u;
						continue;
					}
				}
				continue;
			}

			if (s.equalsIgnoreCase("Stapelverarbeitung") || s.equalsIgnoreCase("BatchProcessing")) {
				NodeList l2=e.getChildNodes();
				for (int j=0; j<l2.getLength();j++) {
					if (!(l2.item(j) instanceof Element)) continue;
					Element e2=(Element)l2.item(j);
					String t=e2.getNodeName();

					if (t.equalsIgnoreCase("Filter")) {
						batchSaveFilter=loadBoolean(e2.getTextContent(),false);
						continue;
					}
					if (t.equalsIgnoreCase("FilterScript")) {
						String u=e2.getTextContent();
						if (u!=null) batchSaveFilterScript=u;
						continue;
					}
					if (t.equalsIgnoreCase("FilterOrdner") || t.equalsIgnoreCase("FilterFolder")) {
						String u=e2.getTextContent();
						if (u!=null) batchSaveFilterOutput=u;
						continue;
					}
					if (t.equalsIgnoreCase("Folder")) {
						String u=e2.getTextContent();
						if (u!=null) batchFolder=u;
						continue;
					}
					if (t.equalsIgnoreCase("XMLFolder")) {
						String u=e2.getTextContent();
						if (u!=null) batchXMLFolder=u;
						continue;
					}
					if (t.equalsIgnoreCase("XMLElement")) {
						String u=e2.getTextContent();
						if (u!=null) batchXMLElement=u;
						continue;
					}
					if (t.equalsIgnoreCase("XMLElementType")) {
						Integer k=NumberTools.getInteger(e2.getTextContent());
						if (k!=null && k>=0) batchXMLElementType=k;
						continue;
					}
					if (t.equalsIgnoreCase("XMLElementFrom")) {
						String u=e2.getTextContent();
						if (u!=null) batchXMLElementFrom=u;
						continue;
					}
					if (t.equalsIgnoreCase("XMLElementTo")) {
						String u=e2.getTextContent();
						if (u!=null) batchXMLElementTo=u;
						continue;
					}
					if (t.equalsIgnoreCase("XMLElementStep")) {
						String u=e2.getTextContent();
						if (u!=null) batchXMLElementStepSize=u;
						continue;
					}
				}
				continue;
			}

			if (s.equalsIgnoreCase("Varianzanalyse") || s.equalsIgnoreCase("AnalysisOfVariance")) {
				Integer j=NumberTools.getInteger(e.getTextContent());
				if (j!=null) varianzAnalyseNumber=Math.min(1000,Math.max(1,j));
				continue;
			}

			if (s.equalsIgnoreCase("AutoUpdate")) {
				updateAutomatic=loadBoolean(e.getTextContent(),true);
				continue;
			}

			if (s.equalsIgnoreCase("UpdateTest")) {
				updateLastCheck=e.getTextContent();
				continue;
			}

			if (s.equalsIgnoreCase("StatistikBaum") || s.equalsIgnoreCase("StatisticTree")) {
				statisticTreeFilter=e.getTextContent();
				continue;
			}
			if (s.equalsIgnoreCase("OptimiererStatistikBaum") || s.equalsIgnoreCase("OptimizerStatisticTree")) {
				optimizerTreeFilter=e.getTextContent();
				continue;
			}
			if (s.equalsIgnoreCase("ReportFilter")) {
				simReportTreeFilter=e.getTextContent();
				continue;
			}
			if (s.equalsIgnoreCase("ModellReportFilter") || s.equalsIgnoreCase("ModelReportFilter")) {
				modelReportTreeFilter=e.getTextContent();
				continue;
			}
			if (s.equalsIgnoreCase("OptimiererReportFilter") || s.equalsIgnoreCase("OptimizerReportFilter")) {
				optimizeReportTreeFilter=e.getTextContent();
				continue;
			}
			if (s.equalsIgnoreCase("ConnectedReportFilter")) {
				connectedReportTreeFilter=e.getTextContent();
				continue;
			}
			if (s.equalsIgnoreCase("SimulationOverviewFilter")) {
				simOverviewFilter=e.getTextContent();
				continue;
			}
			if (s.equalsIgnoreCase("ConfidenceFilter")) {
				confidenceFilter=e.getTextContent();
				continue;
			}
			if (s.equalsIgnoreCase("ClientsCountFilter")) {
				clientsCountFilter=e.getTextContent();
				continue;
			}
			if (s.equalsIgnoreCase("ClientsWaitingTimeFilter")) {
				clientsWaitingTimeFilter=e.getTextContent();
				continue;
			}

			if (s.equalsIgnoreCase("ViewerURL")) {
				viewerURL=e.getTextContent();
				continue;
			}

			if (s.equalsIgnoreCase("LetzteDateien") || s.equalsIgnoreCase("LastFiles")) {
				files.add(e.getTextContent());
				continue;
			}
			if (s.equalsIgnoreCase("LetzteSchnellzugriffsauswahl") || s.equalsIgnoreCase("LastFastAccessSelection")) {
				Integer j=NumberTools.getInteger(e.getTextContent());
				if (j!=null) lastFilterIndex=Math.min(9,Math.max(0,j-1));
				continue;
			}

			if (s.equalsIgnoreCase("OpenStatistics")) {
				openWord=loadBoolean(e.getAttribute("docx"),true);
				openODT=loadBoolean(e.getAttribute("odt"),true);
				openExcel=loadBoolean(e.getAttribute("xlsx"),true);
				openODS=loadBoolean(e.getAttribute("ods"),true);
				continue;
			}
		}

		lastFiles=addToArray(lastFiles,files);
		networkPermittedIPs=addToArray(networkPermittedIPs,ips);
	}

	@Override
	protected void saveSetupToXML(final Document doc, final Element root) {
		Element node, node2;

		root.appendChild(node=doc.createElement(WRITE_ENGLISH_KEYS?"Language":"Sprache"));
		node.setTextContent(language.toLowerCase());

		if (startSizeMode!=StartMode.START_MODE_DEFAULT) {
			root.appendChild(node=doc.createElement(WRITE_ENGLISH_KEYS?"Fullscreen":"Vollbild"));
			if (startSizeMode==StartMode.START_MODE_FULLSCREEN) node.setTextContent("1");
			if (startSizeMode==StartMode.START_MODE_LASTSIZE) node.setTextContent("2");
		}
		if (!startWelcomePage) {
			root.appendChild(node=doc.createElement(WRITE_ENGLISH_KEYS?"WelcomePage":"Willkommensseite"));
			node.setTextContent("0");
		}
		if (!testJavaVersion) {
			root.appendChild(node=doc.createElement("JavaVersionCheck"));
			node.setTextContent("0");
		}
		if (startModusModel!=0) {
			root.appendChild(node=doc.createElement("Start"));
			node.setTextContent(""+(startModusModel+1));
		}

		if (!ribbonMode) {
			root.appendChild(node=doc.createElement("Ribbons"));
			node.setTextContent("0");
		}

		if (scaleGUI!=1) {
			root.appendChild(node=doc.createElement(WRITE_ENGLISH_KEYS?"Scale":"Skalierung"));
			node.setTextContent(NumberTools.localNumberToSystemNumber(NumberTools.formatNumber(scaleGUI)));
		}

		if (startSizeMode==StartMode.START_MODE_LASTSIZE) {
			root.appendChild(node=doc.createElement(WRITE_ENGLISH_KEYS?"LastWindowSize":"LetzteFenstergroesse"));
			node.setAttribute("Modus",""+lastSizeMode);
			node.setAttribute("X",""+lastPosition.x);
			node.setAttribute("Y",""+lastPosition.y);
			node.setAttribute(WRITE_ENGLISH_KEYS?"Width":"Breite",""+lastSize.width);
			node.setAttribute(WRITE_ENGLISH_KEYS?"Height":"Hoehe",""+lastSize.height);
		}

		if (imageSize!=1000 || !imagesInline) {
			root.appendChild(node=doc.createElement(WRITE_ENGLISH_KEYS?"Images":"Bilder"));
			node.setTextContent(""+imageSize);
			if (!imagesInline) node.setAttribute("Inline","0");
		}

		if (!strictCheck) {
			root.appendChild(node=doc.createElement(WRITE_ENGLISH_KEYS?"StrictCheck":"StrengePruefung"));
			node.setTextContent("0");
		}

		if (!backgroundSim || backgroundSimInNetworkMode) {
			root.appendChild(node=doc.createElement(WRITE_ENGLISH_KEYS?"BackgroundSimulation":"HintergrundSimulation"));
			node.setTextContent(backgroundSim?"1":"0");
			if (backgroundSimInNetworkMode) node.setAttribute(WRITE_ENGLISH_KEYS?"InNetworkMode":"AuchImNetzwerk","1");
		}

		if (maxNumberOfThreads!=0) {
			root.appendChild(node=doc.createElement(WRITE_ENGLISH_KEYS?"MaximumNumberOfThreads":"MaximaleThreadAnzahl"));
			node.setTextContent(""+maxNumberOfThreads);
		}

		if (moreThreads) {
			root.appendChild(node=doc.createElement(WRITE_ENGLISH_KEYS?"MoreThreadsThanCPUCores":"MehrThreadsAlsCPUKerne"));
			node.setTextContent("1");
		}

		if (!increaseNumberOfDays) {
			root.appendChild(node=doc.createElement(WRITE_ENGLISH_KEYS?"IncreaseNumberOfDays":"AnzahlAnTagenErhoehen"));
			node.setTextContent("0");
		}

		if (networkUse || !networkServer.isEmpty() || !networkPort.equals("6783") || !networkServerPort.equals("6783") || !networkServerPortWeb.equals("80") || !networkPassword.isEmpty() || !networkServerPassword.isEmpty() || networkMaxThreads!=0 || !networkPart.equals("1")) {
			root.appendChild(node=doc.createElement(WRITE_ENGLISH_KEYS?"Network":"Netzwerk"));
			node.setAttribute(WRITE_ENGLISH_KEYS?"use":"verwenden",networkUse?"1":"0");
			if (!networkServer.isEmpty()) node.setAttribute("Server",networkServer);
			if (!networkPort.equals("6783")) node.setAttribute("Port",""+networkPort);
			if (!networkServerPort.equals("6783")) node.setAttribute("ServerPort",""+networkServerPort);
			if (!networkServerPortWeb.equals("80")) node.setAttribute("WebServerPort",""+networkServerPortWeb);
			if (!networkPassword.isEmpty()) node.setAttribute(WRITE_ENGLISH_KEYS?"Password":"Passwort",""+networkPassword);
			if (!networkServerPassword.isEmpty()) node.setAttribute(WRITE_ENGLISH_KEYS?"ServerPassword":"ServerPasswort",""+networkServerPassword);
			if (networkMaxThreads!=0) node.setAttribute("MaxThreads",""+networkMaxThreads);
			if (!networkPart.isEmpty() && !networkPart.equals("1")) node.setAttribute(WRITE_ENGLISH_KEYS?"ServerPart":"ServerAnteil",networkPart);
		}
		if (networkPermittedIPs!=null) for (int i=0;i<networkPermittedIPs.length;i++) if (!networkPermittedIPs[i].isEmpty()) {
			root.appendChild(node=doc.createElement(WRITE_ENGLISH_KEYS?"NetworkIPFilter":"NetzwerkIPFilter")); node.setTextContent(networkPermittedIPs[i]);
		}

		if (filter!=null) for (int i=0;i<filter.length;i++) if (filter[i]!=null && !filter[i].isEmpty()) {
			root.appendChild(node=doc.createElement((i==0)?"Filter":("Filter"+(i+1))));
			CDATASection cdata=doc.createCDATASection("data");
			node.appendChild(cdata);
			cdata.setData(filter[i]);
		}

		if (autoSaveStatistic || !autoSaveStatisticFolder.isEmpty() || autoSaveFilter || !autoSaveFilterScript.isEmpty() || !autoSaveFilterOutput.isEmpty()) {
			root.appendChild(node=doc.createElement(WRITE_ENGLISH_KEYS?"AutoSave":"AutomatischSpeichern"));
			if (autoSaveStatistic) {
				node.appendChild(node2=doc.createElement(WRITE_ENGLISH_KEYS?"Statistic":"Statistik"));
				node2.setTextContent("1");
			}
			if (!autoSaveStatisticFolder.isEmpty()) {
				node.appendChild(node2=doc.createElement(WRITE_ENGLISH_KEYS?"StatisticFolder":"StatistikOrdner"));
				node2.setTextContent(autoSaveStatisticFolder);
			}
			if (autoSaveFilter) {
				node.appendChild(node2=doc.createElement("Filter"));
				node2.setTextContent("1");
			}
			if (!autoSaveFilterScript.isEmpty()) {
				node.appendChild(node2=doc.createElement("FilterScript"));
				node2.setTextContent(autoSaveFilterScript);
			}
			if (!autoSaveFilterOutput.isEmpty()) {
				node.appendChild(node2=doc.createElement(WRITE_ENGLISH_KEYS?"FilterFolder":"FilterOrdner"));
				node2.setTextContent(autoSaveFilterOutput);
			}
		}

		if (batchSaveFilter || !batchSaveFilterScript.isEmpty() || !batchSaveFilterOutput.isEmpty() || !batchFolder.isEmpty() || !batchXMLFolder.isEmpty() || !batchXMLElement.isEmpty() || batchXMLElementType>=0 || !batchXMLElementFrom.isEmpty() || !batchXMLElementTo.isEmpty() || !batchXMLElementStepSize.isEmpty()) {
			root.appendChild(node=doc.createElement(WRITE_ENGLISH_KEYS?"BatchProcessing":"Stapelverarbeitung"));
			if (batchSaveFilter) {
				node.appendChild(node2=doc.createElement("Filter"));
				node2.setTextContent("1");
			}
			if (!batchSaveFilterScript.isEmpty()) {
				node.appendChild(node2=doc.createElement("FilterScript"));
				node2.setTextContent(batchSaveFilterScript);
			}
			if (!batchSaveFilterOutput.isEmpty()) {
				node.appendChild(node2=doc.createElement(WRITE_ENGLISH_KEYS?"FilterFolder":"FilterOrdner"));
				node2.setTextContent(batchSaveFilterOutput);
			}
			if (!batchFolder.isEmpty()) {
				node.appendChild(node2=doc.createElement("Folder"));
				node2.setTextContent(batchFolder);
			}
			if (!batchXMLFolder.isEmpty()) {
				node.appendChild(node2=doc.createElement("XMLFolder"));
				node2.setTextContent(batchXMLFolder);
			}
			if (!batchXMLElement.isEmpty()) {
				node.appendChild(node2=doc.createElement("XMLElement"));
				node2.setTextContent(batchXMLElement);
			}
			if (batchXMLElementType>=0) {
				node.appendChild(node2=doc.createElement("XMLElementType"));
				node2.setTextContent(""+batchXMLElementType);
			}
			if (!batchXMLElementFrom.isEmpty()) {
				node.appendChild(node2=doc.createElement("XMLElementFrom"));
				node2.setTextContent(batchXMLElementFrom);
			}
			if (!batchXMLElementTo.isEmpty()) {
				node.appendChild(node2=doc.createElement("XMLElementTo"));
				node2.setTextContent(batchXMLElementTo);
			}
			if (!batchXMLElementStepSize.isEmpty()) {
				node.appendChild(node2=doc.createElement("XMLElementStep"));
				node2.setTextContent(batchXMLElementStepSize);
			}
		}

		if (varianzAnalyseNumber!=20) {
			root.appendChild(node=doc.createElement(WRITE_ENGLISH_KEYS?"AnalysisOfVariance":"Varianzanalyse"));
			node.setTextContent(""+varianzAnalyseNumber);
		}

		if (!updateAutomatic) {
			root.appendChild(node=doc.createElement("AutoUpdate"));
			node.setTextContent("0");
		}
		if (updateLastCheck!=null && !updateLastCheck.isEmpty()) {
			root.appendChild(node=doc.createElement("UpdateTest"));
			node.setTextContent(updateLastCheck);
		}
		if (statisticTreeFilter!=null && !statisticTreeFilter.isEmpty()) {
			root.appendChild(node=doc.createElement(WRITE_ENGLISH_KEYS?"StatisticTree":"StatistikBaum"));
			node.setTextContent(statisticTreeFilter);
		}
		if (optimizerTreeFilter!=null && !optimizerTreeFilter.isEmpty()) {
			root.appendChild(node=doc.createElement(WRITE_ENGLISH_KEYS?"OptimizerStatisticTree":"OptimiererStatistikBaum"));
			node.setTextContent(optimizerTreeFilter);
		}
		if (simReportTreeFilter!=null && !simReportTreeFilter.isEmpty()) {
			root.appendChild(node=doc.createElement("ReportFilter"));
			node.setTextContent(simReportTreeFilter);
		}
		if (modelReportTreeFilter!=null && !modelReportTreeFilter.isEmpty()) {
			root.appendChild(node=doc.createElement(WRITE_ENGLISH_KEYS?"ModelReportFilter":"ModellReportFilter"));
			node.setTextContent(modelReportTreeFilter);
		}
		if (optimizeReportTreeFilter!=null && !optimizeReportTreeFilter.isEmpty()) {
			root.appendChild(node=doc.createElement(WRITE_ENGLISH_KEYS?"OptimizerReportFilter":"OptimiererReportFilter"));
			node.setTextContent(optimizeReportTreeFilter);
		}
		if (connectedReportTreeFilter!=null && !connectedReportTreeFilter.isEmpty()) {
			root.appendChild(node=doc.createElement("ConnectedReportFilter"));
			node.setTextContent(connectedReportTreeFilter);
		}
		if (simOverviewFilter!=null && !simOverviewFilter.isEmpty()) {
			root.appendChild(node=doc.createElement("SimulationOverviewFilter"));
			node.setTextContent(simOverviewFilter);
		}
		if (confidenceFilter!=null && !confidenceFilter.isEmpty()) {
			root.appendChild(node=doc.createElement("ConfidenceFilter"));
			node.setTextContent(confidenceFilter);
		}
		if (clientsCountFilter!=null && !clientsCountFilter.isEmpty()) {
			root.appendChild(node=doc.createElement("ClientsCountFilter"));
			node.setTextContent(clientsCountFilter);
		}
		if (clientsWaitingTimeFilter!=null && !clientsWaitingTimeFilter.isEmpty()) {
			root.appendChild(node=doc.createElement("ClientsWaitingTimeFilter"));
			node.setTextContent(clientsWaitingTimeFilter);
		}

		if (viewerURL!=null && !viewerURL.isEmpty()) {
			root.appendChild(node=doc.createElement("ViewerURL"));
			node.setTextContent(viewerURL);
		}
		if (lastFiles!=null && lastFiles.length>0) for (int i=0;i<lastFiles.length;i++) {
			root.appendChild(node=doc.createElement(WRITE_ENGLISH_KEYS?"LastFiles":"LetzteDateien"));
			node.setTextContent(lastFiles[i]);
		}
		if (lastFilterIndex!=0) {
			root.appendChild(node=doc.createElement(WRITE_ENGLISH_KEYS?"LastFastAccessSelection":"LetzteSchnellzugriffsauswahl"));
			node.setTextContent(""+(lastFilterIndex+1));
		}

		if (lastError!=null && !lastError.isEmpty()) {
			root.appendChild(node=doc.createElement("LastError"));
			node.setTextContent(lastError);
		}

		if (!openWord || openODT || !openExcel || openODS) {
			root.appendChild(node=doc.createElement("OpenStatistics"));
			node.setAttribute("docx",openWord?"1":"0");
			node.setAttribute("odt",openODT?"1":"0");
			node.setAttribute("xlsx",openExcel?"1":"0");
			node.setAttribute("ods",openODS?"1":"0");
		}
	}

	/**
	 * Stellt die Systemsprache ein und reinitialisiert
	 * die <code>Language</code>- und <code>LanguageStaticLoader</code>-Systeme.
	 * @param langName	Sprache, "de" oder "en"
	 */
	public void setLanguage(final String langName) {
		language=langName;
		saveSetup();
		Language.init(language);
		LanguageStaticLoader.setLanguage();
		if (Messages_Java11.isFixNeeded()) Messages_Java11.setupMissingSwingMessages();
	}

	/**
	 * Liefert die gemäß den Einstellungen maximal zu verwendende Anzahl an Threads,
	 * die dann aber noch mit der Anzahl an verfügbaren logischen CPU-Kernen
	 * abgeglichen werden muss.
	 * @return	Laut Konfiguration maximal zu verwendende Anzahl an Threads
	 */
	public int getRealMaxThreadNumber() {
		return (maxNumberOfThreads<=0)?Integer.MAX_VALUE:maxNumberOfThreads;
	}
}
