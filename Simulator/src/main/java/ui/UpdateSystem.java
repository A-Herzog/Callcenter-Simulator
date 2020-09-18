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

import java.awt.GraphicsEnvironment;
import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLConnection;
import java.text.SimpleDateFormat;
import java.util.Arrays;
import java.util.Calendar;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

import org.w3c.dom.Element;

import language.Language;
import systemtools.GUITools;
import systemtools.MsgBox;
import systemtools.MsgBoxBackendTaskDialog;
import tools.NetHelper;
import tools.SetupData;
import xml.XMLTools;

/**
 * @author Alexander Herzog
 * @version 1.0
 */
public class UpdateSystem {
	/** Kurze Server-URL; ohne Subdomain */
	public static final String shortHomeURL="a-herzog.github.io/Callcenter-Simulator";
	/** WWW-Basisdomain */
	public static final String wwwHomeURL="a-herzog.github.io/Callcenter-Simulator/";
	/** URL für Update- und Lizenzserver */
	public static final String homeURL="a-herzog.github.io";
	/** Update server */
	public static final String updateServer="github.com";
	/** Versions-JSON-URL */
	public static final String updateFullJSONURL="api.github.com/repos/A-Herzog/Callcenter-Simulator/releases/latest";
	/** Update URL 1 */
	public static final String updateFullURL1="github.com/A-Herzog/Callcenter-Simulator/releases/latest/download/CallcenterSimulatorSetup.exe";
	/** Update URL 2 */
	public static final String updateFullURL2="github.com/A-Herzog/Callcenter-Simulator/releases/latest/download/CallcenterSimulatorSetup.sig";
	/** E-Mail-Adresse */
	public static final String mailURL="alexander.herzog@tu-clausthal.de";
	/** Protokoll für Homepage-Aufrufe */
	public static final String defaultProtocollHomepage="https";
	/** Protokoll für Verbindungen zu Update- und Lizenz-Server */
	public static final String defaultProtocollConnect="https";

	private static final File updateInstallerPart=new File(System.getProperty("java.io.tmpdir"),"CallcenterSimulatorSetup.exe.part");
	private static final File updateInstaller=new File(System.getProperty("java.io.tmpdir"),"CallcenterSimulatorSetup.exe");
	private static final File updateInstallerRun=new File(System.getProperty("java.io.tmpdir"),"CallcenterSimulatorSetupWork.exe");

	private static UpdateSystem updateSystem;

	private static final Lock mutex=new ReentrantLock(true);

	private volatile int updateDownloadStatusFullSize=0;
	private volatile int updateDownloadStatusPercent=0;

	/**
	 * Liefert das Update-System-Singleton
	 * @return	Update-System-Singleton
	 */
	public static UpdateSystem getUpdateSystem() {
		mutex.lock();
		try {
			if (updateSystem==null) updateSystem=new UpdateSystem();
			return updateSystem;
		} finally {
			mutex.unlock();
		}
	}

	/**
	 * Kann der Simulator über das Update-System aktualisiert werden?
	 */
	public final boolean active;

	private volatile String newVersion;

	private UpdateSystem() {
		boolean b=(System.getProperty("os.name").toUpperCase().contains("WIN") && SetupData.getProgramFolder().toString().equals(SetupData.getSetupFolder().toString()));
		if (b) {
			File programFile=new File(new File(System.getProperty("user.dir")),"CallcenterSimulator.exe");
			b=programFile.exists();
		}
		active=b;
	}

	/**
	 * Löscht (alte) Teildownloads usw.<br>
	 * Setzt damit das Update-System auf einen definierten Startzustand zurück.
	 */
	public static void resetUpdateSystem() {
		if (updateInstallerPart.isFile()) updateInstallerPart.delete();
		if (updateInstaller.isFile()) updateInstaller.delete();
		if (updateInstallerRun.isFile()) updateInstallerRun.delete();
	}

	private boolean checkJavaVersion8() {
		String[] ver=System.getProperty("java.version").split("\\.");
		if (ver.length<2) return false;
		Integer ver1=Integer.parseInt(ver[0]);
		Integer ver2=Integer.parseInt(ver[1]);
		if (ver1==null || ver2==null) return false;
		return (ver1==1 && ver2>=8) || ver1>1;
	}

	/**
	 * Prüft ob das als "user.dir" angegebene Verzeichnis beschreibbar ist.
	 * @return	Liefert <code>true</code>, wenn in "user.dir" geschrieben werden kann.
	 */
	public boolean isUserDirWriteable() {
		File testFile=new File(new File(System.getProperty("user.dir")),"CallcenterSimulatorWriteTest.tmp");
		try {
			try (FileOutputStream out=new FileOutputStream(testFile)) {
				out.write(0);
				out.flush();
			}
			testFile.delete();
		} catch (IOException e) {return false;}

		return true;
	}

	/**
	 * Prüft, ob beim letzten Programmaufruf Updates herunter geladen wurden,
	 * die nun installiert werden können.
	 * @return	Liefert <code>true</code>, wenn jetzt Updates installiert werden und sich das Programm daher sofort wieder beenden soll.
	 */
	public boolean runUpdate() {
		if (!active) return false;

		if (updateInstallerPart.isFile()) {
			if (!updateInstallerPart.delete()) return false;
		}
		if (updateInstallerRun.isFile()) {
			if (!updateInstallerRun.delete()) return false;
		}

		if (!isUserDirWriteable()) return false;

		if (!updateInstaller.isFile()) return false;

		if (!updateInstaller.renameTo(updateInstallerRun)) return false;

		if (!GraphicsEnvironment.isHeadless()) {
			GUITools.setupUI();
			MsgBox.setBackend(new MsgBoxBackendTaskDialog());
			MsgBox.info(null,Language.tr("Update.Updater.Title"),Language.tr("Update.Updater.Info"));
		}

		final String cmd=updateInstallerRun.getAbsolutePath()+" /S /D="+System.getProperty("user.dir");
		Runtime.getRuntime().addShutdownHook(new Thread(new Runnable() {
			@Override
			public void run() {
				try {
					Runtime.getRuntime().exec(cmd);
				} catch (IOException e) {}
			}
		},"RunUpdateThread"));
		System.exit(0);
		return true;
	}


	private static String downloadTextFile(final String urlString) {
		URL url;
		try {
			url=new URL("https://"+urlString);
		} catch (MalformedURLException e1) {return null;}

		return NetHelper.loadText(url,false,true);
	}

	/**
	 * Liefert die Versionskennung der neusten auf dem Server verfügbaren Version.
	 * @return	Versionskennung der neusten auf dem Server verfügbaren Version oder <code>null</code>, wenn kein Kennung abgerufen werden konnte
	 */
	public static String checkUpdateAvailable() {
		final String json=downloadTextFile(updateFullJSONURL);
		if (json!=null) {
			final Element root=XMLTools.jsonToXml("{root: "+json+"}",true);
			if (root!=null) return root.getAttribute("tag_name");
		}
		/* alt: final String line=downloadTextFile(wwwHomeURL+"version.txt"); */
		return null;
	}

	/**
	 * Öffnet die Verbindung zum Server, um eine Datei herunterzuladen
	 * @param urlString	Update-Request
	 * @return	Serververbindung oder <code>null</code>, wenn die Verbindung fehlgeschlagen ist.
	 */
	private InputStream openServerFile(final String urlString) {
		/* URL zusammenbauen */
		URL url;
		try {
			url=new URL("https://"+urlString);
		} catch (MalformedURLException e1) {return null;}

		try {
			/* Verbindung öffnen */
			final URLConnection connect=NetHelper.openConnection(url,false,true);
			if (connect==null) return null;

			/* InputStream zurückliefern */
			updateDownloadStatusFullSize=connect.getContentLength();
			return connect.getInputStream();

		} catch (IOException e) {return null;}
	}

	private boolean downloadFile(final InputStream inputStream, final File outputFile) {
		if (inputStream==null) return false;

		FileOutputStream out;
		try {out=new FileOutputStream(outputFile);} catch (FileNotFoundException e) {return false;}
		try (BufferedOutputStream buf=new BufferedOutputStream(out,32768)) {
			byte[] data=new byte[65536];
			int downloaded=0;
			int size;
			while((size=inputStream.read(data,0,data.length))>=0) {
				downloaded+=size;
				if (updateDownloadStatusFullSize>0) updateDownloadStatusPercent=(int)Math.round(downloaded*100.0/updateDownloadStatusFullSize);
				buf.write(data,0,size);
			}
		} catch (IOException e) {
			try {out.close();} catch (IOException e2) {}
			return false;
		}
		try {out.close();} catch (IOException e) {}
		updateDownloadStatusPercent=100;
		return true;
	}

	private boolean downloadFile(final String urlString, final File outputFile) {
		try (InputStream inputStream=openServerFile(urlString)) {
			return downloadFile(inputStream,outputFile);
		} catch (IOException e1) {
			return false;
		}
	}

	private boolean downloadUpdate(final File folderForManualInstallation) {
		if (updateInstaller.isFile()) return true;

		updateDownloadStatusPercent=0;
		try {
			/* Datei herunterladen */
			if (!downloadFile(updateFullURL1,updateInstallerPart)) return false;

			/* Prüfsumme laden */
			try (InputStream in=openServerFile(updateFullURL2)) {
				byte[] data=new byte[32768];
				int size=in.read(data,0,data.length);
				if (size<=0) {
					/* Prüfung fehlgeschlagen (keine Signatur verfügbar) */
					updateInstallerPart.delete();
					updateDownloadStatusPercent=-1;
					return true;
				}
				byte[] sign=Arrays.copyOf(data,size);
				UpdateSystemSignature tester=new UpdateSystemSignature(updateInstallerPart);
				if (!tester.verify(new String(sign))) {
					/* Prüfung fehlgeschlagen */
					updateInstallerPart.delete();
					updateDownloadStatusPercent=-1;
					return true;
				}
			}
		} catch (IOException e) {
			updateDownloadStatusPercent=-1;
			return false;
		}

		if (folderForManualInstallation==null) {
			/* Update on next start */
			updateInstallerPart.renameTo(updateInstaller);
		} else {
			/* Only store file for manual update */
			if (!updateInstallerPart.renameTo(new File(folderForManualInstallation,updateInstaller.getName())))
				updateInstallerPart.delete();
		}

		updateDownloadStatusPercent=101;
		return true;
	}

	/**
	 * Liefert die im Setup-Dialog anzuzeigende neue Version.
	 * @return	Im Setup-Dialog anzuzeigende neue Version
	 */
	public String getNewVersion() {
		return newVersion;
	}

	/**
	 * Liefert den Download-Fortschritt für den Setup-Dialog.
	 * @return	Download-Fortschritt für den Setup-Dialog (Werte zwischen 0 und 100 einschließlich stellen den Fortschritt dar; Werte größer als 100 bedeuten "Abgeschlossen, bitte neu starten" und Werte kleiner als 0 bedeuten "Fehlgeschlagen")
	 */
	public int getDownloadState() {
		return updateDownloadStatusPercent;
	}

	private void checkUpdate(final boolean force, final File folderForManualInstallation) {
		if (active && updateInstallerPart.isFile()) {
			if (!updateInstallerPart.delete()) return;
		}

		Calendar cal=Calendar.getInstance();
		SimpleDateFormat sdf=new SimpleDateFormat("dd.MM.yyyy");
		String date=sdf.format(cal.getTime());

		SetupData setup=SetupData.getSetup();
		if (!force && folderForManualInstallation==null) {
			if (!setup.updateAutomatic) return;
			if (date.equals(setup.updateLastCheck)) return;
		}

		setup.updateLastCheck=date;
		setup.saveSetup();

		new Thread(new Runnable() {
			@Override
			public void run() {
				String s=checkUpdateAvailable();
				if (!VersionConst.isNewerVersionFull(s)) {newVersion=""; return;}
				if (!checkJavaVersion8() && VersionConst.isNewerVersionFull("5.9.9999",s)) {newVersion=""; return;} /* kein Update über 5.9 hinaus, wenn nicht Java 8 installiert ist */
				newVersion=s;
				if (!active && folderForManualInstallation==null) return;
				downloadUpdate(folderForManualInstallation);
			}
		},"UpdateDownloader").start();
	}

	/**
	 * Führt einen Update-Check aus
	 * @param force	Erneuten Check erzwingen, auch wenn bereits geprüft wurde
	 */
	public void checkUpdate(final boolean force) {
		checkUpdate(force,null);
	}

	/**
	 * Update für manuelle Installation herunterladen
	 * @param folderForManualInstallation	Zielpfad für den Download
	 */
	public void checkUpdate(final File folderForManualInstallation) {
		checkUpdate(true,folderForManualInstallation);
	}
}
