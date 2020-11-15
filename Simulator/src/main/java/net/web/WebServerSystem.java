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
package net.web;

import java.awt.Desktop;
import java.io.BufferedInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.net.URLConnection;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;

import javax.net.ssl.HostnameVerifier;
import javax.net.ssl.HttpsURLConnection;
import javax.net.ssl.SSLSession;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;

import language.Language;
import mathtools.NumberTools;
import net.calc.SimServerManager;
import ui.UpdateSystem;
import xml.XMLTools;

/**
 * Kapselt ein vollständiges Server-System, welches je nach Bedarf Web- und/oder Simulations-Server umfassen kann.
 * @author Alexander Herzog
 * @version 1.0
 */
public abstract class WebServerSystem {
	/**
	 * Array mit zulässigen IPs (leere Liste, wenn keine Einschränkung erfolgen soll)
	 */
	private final String[] permittedIPs;

	/**
	 * Ist der aktuelle Logging-Eintrag der erste in der Liste?
	 */
	private boolean firstNews=true;

	/**
	 * Ausgabedatei für Logging-Meldungen
	 */
	private File logFile;

	/**
	 * Signal-System zur Erkennung von Beenden-Anfragen
	 */
	private final CloseRequestSignal quitSignal;

	/**
	 * Soll sich der Server beenden?
	 */
	private boolean quitVariable=false;

	/**
	 * Lokaler Demo-Modus?
	 * @see #setupDemo()
	 */
	private boolean demoMode=false;

	/**
	 * Wurde die Initialisierung abgeschlossen?
	 * @see #initData()
	 */
	private boolean initMode=false;

	/**
	 * Statistik-Verzeichnisse
	 */
	private final List<StatisticFolder> webServerStatisticFolder=new ArrayList<StatisticFolder>();

	/**
	 * Optionales Filter-Skript
	 */
	private String webServerFilterFolder=null;

	/**
	 * Rechen-Server-System
	 */
	private SimServerManager calcServer;

	/**
	 * Web-Server-System
	 */
	private WebServerThread webServer;

	/**
	 * Konstruktor der Klasse <code>WebServerSystem</code>.
	 * @param permittedIPs	Array mit zulässigen IPs (leere Liste, wenn keine Einschränkung erfolgen soll)
	 * @param useQuitSignal	Gibt an, ob der Server auf SIGTERM und Tastendrücke als Quit-Signale hören soll
	 * @param in	Ist <code>useQuitSignal</code> gesetzt, so kann hierüber ein Lesestream für Tastendrücke angegeben werden
	 */
	public WebServerSystem(String[] permittedIPs, boolean useQuitSignal, InputStream in) {
		this.permittedIPs=permittedIPs;
		if (useQuitSignal) quitSignal=new CloseRequestSignal(true,in); else quitSignal=null;
	}

	/**
	 * Gibt an, dass der Server sich beenden soll.
	 */
	public void setQuit() {
		quitVariable=true;
	}

	/**
	 * Gibt an, ob sich das System beenden soll.
	 * @return	Gibt <code>true</code> zurück, wenn sich das System beenden soll.
	 */
	protected boolean isQuit() {
		return quitVariable || (quitSignal!=null && quitSignal.isQuit());
	}

	/**
	 * Lädt die Konfiguration für das SaaS-System aus einer Datei
	 * @param file	Zu ladende Konfigurationsdatei
	 * @return	Gibt <code>null</code> zurück, wenn die Konfiguration erfolgreich geladen werden konnte, sonst eine Fehlermeldung.
	 */
	public String setupFromConfigFile(File file) {
		if (file==null || !file.exists()) return String.format(Language.tr("CommandLine.Error.File.ConfigDoesNotExist"),file);
		XMLTools xml=new XMLTools(file);
		Element root=xml.load();
		if (root==null) return xml.getError();
		return setupFromConfigXML(root);
	}

	/**
	 * Liefert, wenn verfügbar, das SaaS-Applet aus.
	 * @return	SaaS-Applet
	 */
	private byte[] downloadApplet() {
		URL home;
		try {home=new URL(UpdateSystem.defaultProtocollConnect+"://"+UpdateSystem.wwwHomeURL+"Java/CallcenterSimulatorApplet.jar");} catch (MalformedURLException e) {return null;}

		URLConnection connection;
		try {connection=home.openConnection();} catch (IOException e) {return null;}
		if (!(connection instanceof HttpURLConnection)) return null;
		if (connection instanceof HttpsURLConnection) {
			((HttpsURLConnection )connection).setHostnameVerifier(new HostnameVerifier() {
				@Override
				public boolean verify(String hostname, SSLSession session) {
					return hostname.equalsIgnoreCase(UpdateSystem.wwwHomeURL);
				}
			});
		}
		try (BufferedInputStream in=new BufferedInputStream(connection.getInputStream())) {
			byte[] data=new byte[32768];
			try (ByteArrayOutputStream buf=new ByteArrayOutputStream()) {

				writeConsoleOutput(Language.tr("Server.DownloadingApplet.Status")+" ");
				int x=0, count=0;
				try {
					while((x=in.read(data,0,data.length))>=0) {
						if (isQuit()) {writeConsoleOutput("\n"); return null;}
						buf.write(data,0,x);
						count++;
						if (count%50==0) writeConsoleOutput(".");
					}
				} catch (IOException e) {writeConsoleOutput("\n"); return null;}

				writeConsoleOutput("\n");
				log(Language.tr("Server.WebServerSystem"),String.format(Language.tr("Server.DownloadingApplet.Done"),buf.size()/1024/1024),true);
				return buf.toByteArray();
			}
		} catch (IOException e) {return null;}
	}

	/**
	 * Lädt die Konfiguration für das SaaS-System aus einem XML-Objekt
	 * @param node	Basiskonten der Konfiguration
	 * @return	Gibt <code>null</code> zurück, wenn die Konfiguration erfolgreich geladen werden konnte, sonst eine Fehlermeldung.
	 */
	public String setupFromConfigXML(Element node) {
		if (!Language.trAll("XML.SaaS",node.getNodeName())) return String.format(Language.tr("XML.SaaS.Error"),Language.tr("XML.SaaS"));

		boolean useSimServer=false;
		boolean useWebServer=false;

		int simServerPort=6783;
		String simServerPassword=null;
		int simServerMaxThreads=0;

		int webServerPort=80;
		File webServerAppletJarFile=null;

		NodeList l=node.getChildNodes();
		for (int i=0; i<l.getLength();i++) {
			if (!(l.item(i) instanceof Element)) continue;
			Element e=(Element)l.item(i);
			String s=e.getNodeName();

			/* Logdatei */
			if (Language.trAll("XML.SaaS.LogFile",s)) {
				logFile=new File(e.getTextContent());
				if (logFile.isDirectory()) return String.format(Language.tr("CommandLine.Error.File.LogFileExistsAsDirectory"),logFile.toString());
				if (logFile.exists()) {
					if (!logFile.canWrite()) return String.format(Language.tr("CommandLine.Error.File.CannotWriteLogFile"),logFile.toString());
				} else {
					try {
						if (!logFile.createNewFile()) return String.format(Language.tr("CommandLine.Error.File.CannotCreateLogFile"),logFile.toString());
					} catch (IOException e2) {
						return String.format(Language.tr("CommandLine.Error.File.CannotCreateLogFile"),logFile.toString());
					}
				}
				continue;
			}

			/* Simulationssserver */
			if (Language.trAll("XML.SaaS.SimServer",s)) {
				useSimServer=true;
				NodeList l2=e.getChildNodes();
				for (int j=0; j<l2.getLength();j++) {
					if (!(l2.item(j) instanceof Element)) continue;
					Element e2=(Element)l2.item(j);
					String t=e2.getNodeName();

					if (Language.trAll("XML.SaaS.Port",t)) {
						Integer I=NumberTools.getNotNegativeInteger(e2.getTextContent());
						if (I==null) return String.format(Language.tr("CommandLine.SaaSServer.InvalidDataPort"),e2.getTextContent());
						simServerPort=I;
						continue;
					}

					if (Language.trAll("XML.SaaS.Password",t)) {simServerPassword=e2.getTextContent(); continue;}

					if (Language.trAll("XML.SaaS.MaxThreads",t)) {
						Integer I=NumberTools.getNotNegativeInteger(e2.getTextContent());
						if (I==null || I<0) return String.format(Language.tr("CommandLine.SaaSServer.InvalidMaximumNumberOfThreads"),e2.getTextContent());
						simServerMaxThreads=I;
						continue;
					}
				}
				continue;
			}

			/* Webserver */
			if (Language.trAll("XML.SaaS.WebServer",s)) {
				useWebServer=true;
				NodeList l2=e.getChildNodes();
				for (int j=0; j<l2.getLength();j++) {
					if (!(l2.item(j) instanceof Element)) continue;
					Element e2=(Element)l2.item(j);
					String t=e2.getNodeName();

					if (Language.trAll("XML.SaaS.Port",t)) {
						Integer I=NumberTools.getNotNegativeInteger(e2.getTextContent());
						if (I==null) return String.format(Language.tr("CommandLine.SaaSServer.InvalidHTMLPort"),e2.getTextContent());
						webServerPort=I;
						continue;
					}

					if (Language.trAll("XML.SaaS.AppletJarFile",t)) {webServerAppletJarFile=new File(e2.getTextContent()); continue;}

					if (Language.trAll("XML.SaaS.StatisticFolder",t)) {
						webServerStatisticFolder.add(new StatisticFolder(e2.getTextContent(),Language.trAllAttribute("XML.SaaS.StatisticFolder.Name",e2)));
						continue;
					}

					if (Language.trAll("XML.SaaS.FilterFolder",t)) webServerFilterFolder=e2.getTextContent();
				}
				continue;
			}
		}

		if (!useSimServer && !useWebServer) {
			return Language.tr("CommandLine.SaaSServer.NoServices");
		}

		if (useSimServer) {
			setupCalcServer(simServerPort,simServerPassword,simServerMaxThreads);
		}

		if (useWebServer) {
			List<WebServerDataHandler> handlers=new ArrayList<WebServerDataHandler>();
			if (useSimServer) {
				if (webServerAppletJarFile==null) {
					log(Language.tr("Server.WebServerSystem"),Language.tr("Server.DownloadingApplet.NoLocalPathToAppletTryingToDownload"),false);
					byte[] b=downloadApplet();
					if (b==null) return Language.tr("Server.DownloadingApplet.FailedToDownloadApplet");
					handlers.add(new HandlerCalcServer(simServerPort,simServerPassword,b));
				} else {
					webServerAppletJarFile=webServerAppletJarFile.getAbsoluteFile();
					if (!webServerAppletJarFile.exists()) return String.format(Language.tr("CommandLine.SaaSServer.JarFileDoesNotExist"),webServerAppletJarFile);
					handlers.add(new HandlerCalcServer(simServerPort,simServerPassword,webServerAppletJarFile));
				}
			}
			for (StatisticFolder folder : webServerStatisticFolder) {
				if (!folder.isDirectory()) return String.format(Language.tr("CommandLine.Error.File.FolderDoesNotExist"),folder.folder.toString());
			}
			handlers.add(new HandlerViewerSystem(webServerStatisticFolder,webServerFilterFolder));
			handlers.add(new HandlerMainMenu(useSimServer,webServerStatisticFolder));
			setupWebServer(webServerPort,handlers.toArray(new WebServerDataHandler[0]));
		}

		return null;
	}

	/**
	 * Stellt eine Basis-Konfiguration für das SaaS-System ein.
	 * @return	Gibt <code>null</code> zurück, wenn die Konfiguration erfolgreich eingerichtet werden konnte, sonst eine Fehlermeldung.
	 */
	public String setupDemo() {
		demoMode=true;

		setupCalcServer(6783,null,0);

		List<WebServerDataHandler> handlers=new ArrayList<WebServerDataHandler>();
		File webServerAppletJarFile=new File("CallcenterSimulatorApplet.jar").getAbsoluteFile();
		if (webServerAppletJarFile.isFile()) {
			handlers.add(new HandlerCalcServer(6783,null,webServerAppletJarFile));
		} else {
			log(Language.tr("Server.WebServerSystem"),Language.tr("Server.DownloadingApplet.NoLocalPathToAppletTryingToDownload"),false);
			byte[] b=downloadApplet();
			if (b==null) return Language.tr("Server.DownloadingApplet.FailedToDownloadApplet");
			handlers.add(new HandlerCalcServer(6783,null,b));
		}
		log(Language.tr("Server.WebServerSystem"),Language.tr("Server.DemoStatisticFromDesktopFolder"),true);

		webServerStatisticFolder.add(new StatisticFolder(System.getProperty("user.home")+File.separator+"Desktop",null));
		handlers.add(new HandlerViewerSystem(webServerStatisticFolder,null));
		handlers.add(new HandlerMainMenu(true,webServerStatisticFolder));
		setupWebServer(80,handlers.toArray(new WebServerDataHandler[0]));

		return null;
	}

	/**
	 * Erstellt und speichert eine SaaS-Basiskonfiguration und lädt wenn nötig die Applet-jar-Datei herunter.
	 * @return	Gibt <code>null</code> zurück, wenn die Konfiguration erfolgreich eingerichtet werden konnte, sonst eine Fehlermeldung.
	 */
	public String initData() {
		initMode=true;
		File configFile=new File("Simulator-SaaS-Server-Setup.xml").getAbsoluteFile();
		File webServerAppletJarFile=new File("CallcenterSimulatorApplet.jar").getAbsoluteFile();

		log(Language.tr("Server.ServerSystem"),Language.tr("Server.InitializationStart"),true);

		/* Konfigurationsdatei erstellen */
		XMLTools xml=new XMLTools(configFile);
		Element root=xml.generateRoot(Language.trPrimary("XML.SaaS"));
		if (root==null) return Language.tr("Server.ErrorCreatingConfiguration");
		Document doc=root.getOwnerDocument();
		Element e,e2;
		root.appendChild(e=doc.createElement(Language.trPrimary("XML.SaaS.SimServer")));
		e.appendChild(e2=doc.createElement(Language.tr("XML.SaaS.Port"))); e2.setTextContent("6783");
		root.appendChild(e=doc.createElement(Language.trPrimary("XML.SaaS.WebServer")));
		e.appendChild(e2=doc.createElement(Language.tr("XML.SaaS.Port"))); e2.setTextContent("80");
		e.appendChild(e2=doc.createElement(Language.tr("XML.SaaS.AppletJarFile"))); e2.setTextContent(webServerAppletJarFile.toString());
		e.appendChild(e2=doc.createElement(Language.tr("XML.SaaS.StatisticFolder"))); e2.setTextContent(new File(System.getProperty("user.home")+"/Desktop").toString());
		if (!xml.save(root)) return String.format(Language.tr("Server.ErrorSavingConfiguration"),configFile.toString());

		/* Applet herunterladen */
		if (!webServerAppletJarFile.isFile()) {
			byte[] b=downloadApplet();
			if (b==null) return Language.tr("Server.DownloadingApplet.FailedToDownloadApplet");
			try (FileOutputStream out=new FileOutputStream(webServerAppletJarFile);) {
				out.write(b);
			} catch (IOException e1) {return String.format(Language.tr("Server.DownloadingApplet.FailedSaving"),webServerAppletJarFile.toString());}
		}

		log(Language.tr("Server.ServerSystem"),String.format(Language.tr("Server.InitializationFinished.ConfigFile"),configFile.toString()),true);
		log(Language.tr("Server.ServerSystem"),Language.tr("Server.InitializationFinished"),true);

		return null;
	}

	/**
	 * Definiert eine Logdatei für die Ausgaben.
	 * Wird keine Logdatei angegeben bzw. hier <code>null</code> übergeben, so erfolgen alle Ausgaben auf der Konsole,
	 * ansonsten werden nur die wichtigen Meldungen auf der Console ausgegeben.
	 * @param file	Logdatei für Ausgaben
	 */
	public void setupLogFile(File file) {
		logFile=file;
	}

	/**
	 * Konfiguriert den Simulationsserver-Dienst
	 * @param port	Port auf dem der Simulationsserver auf Aufträge warten soll
	 * @param password	Passwort für Zugriffe auf den Simulationsserver
	 * @param maxThread	Maximale Anzahl an Threads, die der Simulationsserver pro Anfrage verwenden darf (0 für keine Beschränkung)
	 */
	public void setupCalcServer(int port, String password, int maxThread) {
		calcServer=new SimServerManager(port,password,maxThread,permittedIPs) {
			@Override protected void showInfo(String sender, String info, boolean screenMessage) {log(sender,info,screenMessage);}
			@Override protected boolean quitServer() {return isQuit();}
			@Override protected void finished() {setQuit();}
		};
	}

	/**
	 * Konfiguriert den Webserver-Dienst
	 * @param port	Port auf dem der Webserver auf Anfragen warten soll
	 * @param handlers	Array mit Handlern vom Typ <code>WebServerDataHandler</code> zur Bearbeitung der Anfragen
	 * @see WebServerDataHandler
	 */
	public void setupWebServer(int port, WebServerDataHandler[] handlers) {
		webServer=new WebServerThread(port,permittedIPs) {
			@Override public void showInfo(String sender, String info, boolean screenMessage) {log(sender,info,screenMessage);}
			@Override protected boolean quitTest() {return isQuit();}
			@Override protected void finished() {setQuit();}
			@Override protected void started() {
				for (StatisticFolder folder : webServerStatisticFolder) log(Language.tr("Server.WebServerSystem"),Language.tr("Server.StatisticFolder")+": "+folder.folder.toString(),true);
				if (webServerFilterFolder!=null && !webServerFilterFolder.trim().isEmpty()) log(Language.tr("Server.WebServerSystem"),Language.tr("Server.FilterFolder")+": "+webServerFilterFolder,true);
			}
		};
		for (WebServerDataHandler handler: handlers) webServer.registerHandler(handler);
	}

	/**
	 * Konfiguriert den Webserver-Dienst
	 * @param portWeb	Port, auf dem der Webserver laufen soll (muss &gt;0 sein)
	 * @param portCalc	Port, auf dem optional ein Rechenserver läuft (==0, wenn kein Rechenserver aktiv sein wird). Ist hier ein Wert &gt;0 angegeben, so wird die Applet-Version angeboten
	 * @param applet	Pfad zur Applet-Version des Simulators. Ist <code>portCalc&gt;0</code> und <code>applet==null</code>, so wird das Applet erst heruntergeladen
	 * @param passwordCalc	Passwort für den Zugriff auf den Rechenserver (kann <code>null</code> sein).
	 * @param statisticFolder	Optionales Verzeichnis mit Statistikdateien, die über den Webserver zur Anzeige angeboten werden sollen (kann <code>null</code> sein).
	 * @return	Gibt an, ob die Konfiguration in Ordnung ist.
	 */
	public boolean setupWebServer(int portWeb, int portCalc, File applet, String passwordCalc, File statisticFolder) {
		boolean useApplet=false;
		StatisticFolder serverViewer=null;

		List<WebServerDataHandler> handlers=new ArrayList<WebServerDataHandler>();

		if (portCalc>0) {
			useApplet=true;
			if (applet==null || !applet.isFile()) {
				log(Language.tr("Server.WebServerSystem"),Language.tr("Server.DownloadingApplet.NoLocalPathToAppletTryingToDownload"),false);
				byte[] b=downloadApplet();
				if (b==null) {
					log(Language.tr("Server.WebServerSystem"),Language.tr("Server.DownloadingApplet.FailedToDownloadApplet"),true);
					return false;
				}
				handlers.add(new HandlerCalcServer(portCalc,passwordCalc,b));
			} else {
				handlers.add(new HandlerCalcServer(portCalc,passwordCalc,applet));
			}
		}

		if (statisticFolder!=null) {
			serverViewer=new StatisticFolder(statisticFolder,null);
			if (!serverViewer.isDirectory()) {
				log(Language.tr("Server.WebServerSystem"),String.format(Language.tr("CommandLine.Error.File.FolderDoesNotExist"),statisticFolder.toString()),true);
				return false;
			}
			webServerStatisticFolder.add(serverViewer);
			handlers.add(new HandlerViewerSystem(webServerStatisticFolder,null));
		}

		handlers.add(new HandlerMainMenu(useApplet,(serverViewer!=null)?webServerStatisticFolder:null));
		setupWebServer(portWeb,handlers.toArray(new WebServerDataHandler[0]));

		return true;
	}

	/**
	 * Startet den oder die Server-Dienste und kehrt erst nach dem Beenden des Systems zurück.
	 */
	public void run() {
		if (webServer!=null) {
			webServer.start();
		}
		if (demoMode) try {Desktop.getDesktop().browse(new URI("http://localhost:80/"));} catch (IOException | URISyntaxException e) {}
		if (calcServer!=null) {
			/* calcServer ist kein Thread, runServer kehrt erst beim Beenden zurück */
			calcServer.runServer();
		} else {
			/* Warten ... */
			while (!isQuit()) try {Thread.sleep(50);} catch (InterruptedException e) {}
		}
	}

	/**
	 * Gibt eine Logging-Meldung aus.
	 * @param sender	Absender der Meldung
	 * @param info	Meldung
	 * @param screenMessage	Soll die Meldung auch im Falle der Verwendung einer Logdatei auf dem Bildschirm ausgegeben werden?
	 */
	private void log(final String sender, final String info, final boolean screenMessage) {
		final Calendar cal=Calendar.getInstance();
		final SimpleDateFormat sdf=new SimpleDateFormat(Language.tr("Simulation.FullDateFormat"));
		String date=sdf.format(cal.getTime());
		if (logFile==null) {
			writeConsoleOutput(date+" "+sender+": "+info+"\n");
		} else {
			if (screenMessage) writeConsoleOutput(sender+": "+info+"\n");
			FileWriter writer;
			try {
				writer=new FileWriter(logFile,true);
				try {
					writer.write(date+" "+sender+": "+info+"\n");
					writer.flush();
				} finally {
					writer.close();
				}
			} catch (IOException e) {
				writeConsoleOutput(String.format(Language.tr("Server.Error.Log"),logFile.toString())+"\n");
				writeConsoleOutput(date+" "+sender+": "+info+"\n");
			}
		}
		if (firstNews && !initMode && quitSignal!=null) {writeConsoleOutput(date+" "+sender+": "+Language.tr("Server.TerminateByUserCommand")+"\n"); firstNews=false;}
	}

	/**
	 * Ausgabe von Statusmeldungen auf der Konsole
	 * @param text	Auszugebender Text (ist ggf. notwendigen Zeilenumbrüchen)
	 */
	protected abstract void writeConsoleOutput(String text);
}
