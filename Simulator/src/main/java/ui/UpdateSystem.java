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
import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.UnsupportedEncodingException;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLConnection;
import java.text.SimpleDateFormat;
import java.util.Arrays;
import java.util.Calendar;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

import javax.net.ssl.HostnameVerifier;
import javax.net.ssl.HttpsURLConnection;
import javax.net.ssl.SSLSession;

import language.Language;
import systemtools.GUITools;
import systemtools.MsgBox;
import systemtools.MsgBoxBackendTaskDialog;
import tools.SetupData;

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

	private final Lock getDataLock=new ReentrantLock(true);

	private static UpdateSystem updateSystem;

	private static final Lock mutex=new ReentrantLock(true);

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

	private String newVersion;
	private double newVersionDownload=0;

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

	/**
	 * Liefert die Versionskennung der neusten auf dem Server verfügbaren Version.
	 * @return	Versionskennung der neusten auf dem Server verfügbaren Version oder <code>null</code>, wenn kein Kennung abgerufen werden konnte
	 */
	public static String checkUpdateAvailable() {
		try {
			URL home=new URL(defaultProtocollConnect+"://"+wwwHomeURL+"version.txt");
			URLConnection connect=home.openConnection();
			if (!(connect instanceof HttpURLConnection)) return null;
			if (connect instanceof HttpsURLConnection) {
				((HttpsURLConnection )connect).setHostnameVerifier(new HostnameVerifier() {
					@Override
					public boolean verify(String hostname, SSLSession session) {
						return hostname.equalsIgnoreCase(wwwHomeURL);
					}
				});
			}
			BufferedReader in=new BufferedReader(new InputStreamReader(connect.getInputStream()));
			return in.readLine();
		} catch (UnsupportedEncodingException e) {return null;} catch (MalformedURLException e) {return null;} catch (IOException e) {return null;}
	}

	private boolean downloadUpdate(final File folderForManualInstallation) {
		if (updateInstaller.isFile()) return true;

		newVersionDownload=0;
		try {
			URL home1=new URL(defaultProtocollConnect+"://"+updateFullURL1);
			URL home2=new URL(defaultProtocollConnect+"://"+updateFullURL2);
			byte[] data=new byte[32768];

			/* Datei herunterladen */
			URLConnection connection=home1.openConnection();
			if (!(connection instanceof HttpURLConnection)) return false;
			if (connection instanceof HttpsURLConnection) {
				((HttpsURLConnection )connection).setHostnameVerifier(new HostnameVerifier() {
					@Override
					public boolean verify(String hostname, SSLSession session) {
						return hostname.equalsIgnoreCase(updateServer);
					}
				});
			}
			int downloadSize=connection.getContentLength();
			try (BufferedInputStream in=new BufferedInputStream(connection.getInputStream())) {
				FileOutputStream out=new FileOutputStream(updateInstallerPart);
				try (BufferedOutputStream buf=new BufferedOutputStream(out,32768)) {
					int x=0, downloaded=0;
					while((x=in.read(data,0,data.length))>=0) {
						buf.write(data,0,x);
						downloaded+=x;
						getDataLock.lock(); try {newVersionDownload=(double)downloaded/downloadSize;} finally {getDataLock.unlock();}
					}
				}
			}

			/* Prüfsumme laden */
			connection=home2.openConnection();
			if (!(connection instanceof HttpURLConnection)) {newVersionDownload=-1; return false;}
			if (connection instanceof HttpsURLConnection) {
				((HttpsURLConnection )connection).setHostnameVerifier(new HostnameVerifier() {
					@Override
					public boolean verify(String hostname, SSLSession session) {
						return hostname.equalsIgnoreCase(homeURL);
					}
				});
			}
			try (BufferedInputStream in=new BufferedInputStream(connection.getInputStream())) {
				int size=in.read(data,0,data.length);
				if (size<=0) {
					/* Prüfung fehlgeschlagen (keine Signatur verfügbar) */
					updateInstallerPart.delete();
					getDataLock.lock(); try {newVersionDownload=-1;} finally {getDataLock.unlock();}
					return true;
				}
				byte[] sign=Arrays.copyOf(data,size);
				UpdateSystemSignature tester=new UpdateSystemSignature(updateInstallerPart);
				if (!tester.verify(new String(sign))) {
					/* Prüfung fehlgeschlagen */
					updateInstallerPart.delete();
					getDataLock.lock(); try {newVersionDownload=-1;} finally {getDataLock.unlock();}
					return true;
				}
			}
		} catch (UnsupportedEncodingException | MalformedURLException e) {newVersionDownload=-1; return false;} catch (IOException e) {newVersionDownload=-1; return false;}

		if (folderForManualInstallation==null) {
			/* Update on next start */
			updateInstallerPart.renameTo(updateInstaller);
		} else {
			/* Only store file for manual update */
			if (!updateInstallerPart.renameTo(new File(folderForManualInstallation,updateInstaller.getName())))
				updateInstallerPart.delete();
		}

		return true;
	}

	/**
	 * Liefert die im Setup-Dialog anzuzeigende neue Version.
	 * @return	Im Setup-Dialog anzuzeigende neue Version
	 */
	public String getNewVersion() {
		getDataLock.lock();
		try {
			return newVersion;
		} finally {
			getDataLock.unlock();
		}
	}

	/**
	 * Liefert den Download-Fortschritt für den Setup-Dialog.
	 * @return	Download-Fortschritt für den Setup-Dialog (Werte zwischen 0 und 1 einschließlich stellen den Fortschritt dar; Werte größer als 1 bedeuten "Abgeschlossen, bitte neu starten" und Werte kleiner als 0 bedeuten "Fehlgeschlagen")
	 */
	public double getDownloadState() {
		getDataLock.lock();
		try {
			return newVersionDownload;
		} finally {
			getDataLock.unlock();
		}
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
				getDataLock.lock();
				try {
					newVersion=s;
				} finally {
					getDataLock.unlock();
				}
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
