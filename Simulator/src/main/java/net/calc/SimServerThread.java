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
package net.calc;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.ServerSocket;
import java.net.SocketTimeoutException;

import language.Language;
import simulator.Simulator;
import simulator.Statistics;
import tools.SetupData;
import ui.model.CallcenterModel;
import ui.model.CallcenterRunModel;

/**
 * Simulations-Server-Thread
 * @author Alexander Herzog
 * @version 1.0
 */
public final class SimServerThread extends NetworkThread {
	/** Wird aufgerufen, wenn ein neuer Rechentask gestartet wurde */
	private final Runnable threadBusy;
	/** Maximale Anzahl an zu verwendenden Rechenthreads */
	private final int maxThreads;
	/** Zulässige Client-IP-Adressen (oder <code>null</code>, wenn keine IP-Filterung erfolgen soll) */
	private final String[] permittedIPs;

	/**
	 * Wurde die Verarbeitung bereits gestartet?
	 * @see #isWorkStarted()
	 */
	private boolean workStarted=false;

	/**
	 * Fehlermeldung oder <code>null</code>, wenn kein Fehler aufgetreten ist.
	 * @see #getResult()
	 */
	private String runResult=null;

	/**
	 * IP-Adresse des Clienten dessen Anfrage
	 * bearbeitet wird (für Logging-Ausgaben).
	 * @see #getClientIP()
	 */
	private String clientIP="";

	/**
	 * Ist ein Fehler aufgetreten, der zum Abbruch führte?
	 * @see #isFatalError()
	 */
	private boolean fatalError=false;

	/**
	 * Eingabe-Stream aus dem Daten vom Clienten
	 * empfangen werden sollen.
	 */
	private InputStream inputStream;

	/**
	 * Soll sich der Server beenden?
	 */
	private boolean pleaseQuit=false;

	/**
	 * Konstruktor der Klasse
	 * @param port	Zu verwendender Listen-Port
	 * @param password	Passwort welches Clienten angeben müssen (oder <code>null</code>, wenn kein Passwort verwendet werden soll)
	 * @param maxThreads	Maximale Anzahl an zu verwendenden Rechenthreads
	 * @param permittedIPs	Zulässige Client-IP-Adressen (oder <code>null</code>, wenn keine IP-Filterung erfolgen soll)
	 * @param threadBusy	Wird aufgerufen, wenn ein neuer Rechentask gestartet wurde
	 */
	public SimServerThread(int port, String password, int maxThreads, String[] permittedIPs, Runnable threadBusy) {
		super(port,password);
		this.maxThreads=maxThreads;
		this.threadBusy=threadBusy;
		this.permittedIPs=permittedIPs;
	}

	/**
	 * Wurde die Verarbeitung bereits gestartet? (Kann aber auch schon abgeschlossen sein.)
	 * @return	Liefert <code>true</code>, wenn der Thread gestartet wurde
	 */
	public boolean isWorkStarted() {
		return workStarted;
	}

	/**
	 * Ist ein Fehler aufgetreten, der zum Abbruch führte?
	 * @return	Liefert <code>true</code>, wenn ein Fehler aufgetreten ist.
	 */
	public boolean isFatalError() {
		return fatalError;
	}

	/**
	 * Liefert die Fehlermeldung.
	 * @return	Fehlermeldung oder <code>null</code>, wenn kein Fehler aufgetreten ist.
	 */
	public String getResult() {
		return runResult;
	}

	/**
	 * Liefert die IP-Adresse der Anfragestellers.
	 * @return	IP-Adresse der Anfragestellers
	 */
	public String getClientIP() {
		return clientIP;
	}

	/**
	 * Signalisiert, das ein neuer Rechenthread gestartet wurde.
	 * @see #threadBusy
	 */
	private synchronized void threadBusyNow() {
		workStarted=true;
		if (threadBusy!=null) threadBusy.run();
	}

	/**
	 * Wurde eine Simulation abgebrochen?
	 * @param simulator	Simulator-Objekt
	 * @return	Simulation abgebrochen?
	 */
	private boolean isCanceled(final Simulator simulator) {
		try {
			if (inputStream.available()==0) return false;
			switch (inputStream.read()) {
			case 0: return true;
			case 1: simulator.setPriority(true); return false;
			case 2: simulator.setPriority(false); return false;
			default: return true;
			}
		} catch (IOException e) {
			return true;
		}
	}

	/**
	 * Führt eine Simulation durch.
	 * @param editModel	Zu simulierendes Modell
	 * @return	Liefert im Erfolgsfall die Statistikdaten, sonst eine Fehlermeldung
	 */
	private Object runSimulation(final CallcenterModel editModel) {
		/* Modell vorbereiten */
		if (ui.VersionConst.isNewerVersion(editModel.version)) return Language.tr("Server.Error.NewerServerVersionNeeded");
		if (ui.VersionConst.isOlderVersion(editModel.version)) return Language.tr("Server.Error.NewerClientVersionNeeded");

		CallcenterRunModel runModel=new CallcenterRunModel(editModel);
		String s=runModel.checkAndInit(false,false,SetupData.getSetup().strictCheck);
		if (s!=null) {return String.format(Language.tr("Server.Error.PreparationError"),s);}

		/* Simulation starten */
		int threads=SetupData.getSetup().getRealMaxThreadNumber();
		if (maxThreads>0) threads=Math.min(threads,maxThreads);
		Simulator simulator=new Simulator(threads,runModel);
		simulator.start(false);

		int count=0;
		while (simulator.isRunning()) {
			try {Thread.sleep(50);} catch (InterruptedException e) {}
			if (isCanceled(simulator) || isInterrupted()) {
				simulator.cancel();
				runResult=Language.tr("Server.CanceledByUserOnClientSide");
				return null;
			}
			count++;
			if (count%5==0) {
				ByteArrayOutputStream data=new ByteArrayOutputStream();
				DataOutputStream status=new DataOutputStream(data);
				try {
					status.writeLong(simulator.getEventCount());
					status.writeInt(simulator.getEventsPerSecond());
					status.writeInt((int)simulator.getSimDayCount());
				} catch (IOException e) {}
				sendData((byte)1,data);
			}
		}

		simulator.finalizeRun();
		Statistics statistics=simulator.collectStatistic();

		return statistics;
	}

	/**
	 * Prüft, ob die Clienten-IP-Adresse zulässig ist.
	 * @return	Client ist zulässig
	 * @see #clientIP
	 */
	private boolean checkIPpermitted() {
		if (permittedIPs==null || permittedIPs.length==0) return true;
		for (String range : permittedIPs) {
			if (clientIP.toUpperCase().startsWith(range.toUpperCase())) return true;
		}
		return false;
	}

	@Override
	public void run() {
		String s;

		/* Server-Socket initialisieren */
		try (ServerSocket server=new ServerSocket(port)) {
			server.setSoTimeout(50);

			/* Auf eingehende Verbindungen warten */
			boolean ok=false;
			while (!ok) {
				if (isInterrupted() || pleaseQuit) return;
				try {socket=server.accept();} catch (IOException e) {
					if (e instanceof SocketTimeoutException) {continue;}
					runResult=Language.tr("Server.Error.CannotListenForConnections"); fatalError=true;
					return;
				}
				ok=true;
			}
		}
		catch (IOException e1) {runResult=Language.tr("Server.Error.NoSocket"); fatalError=true; return;}

		/* Dem SimServerManager signalisieren, dass der Thread jetzt mit der Arbeit beginnt */
		clientIP=socket.getInetAddress().getHostAddress();
		threadBusyNow();

		/* Prüfen, ob die IP-Adresse zugelassen ist */
		if (!checkIPpermitted()) {
			runResult=Language.tr("Server.Error.BlockedByIPFilter");
			return;
		}

		/* Direkten InputStream für spätere Zugriffe aufheben */
		try {
			inputStream=socket.getInputStream();
		} catch (IOException e) {
			runResult=Language.tr("Server.Error.CannotConnect");
			return;
		}

		/* Sockets verbinden */
		try {
			Object[] input=receiveData();
			if (input==null || input[1]==null) {
				runResult=Language.tr("Server.Error.NoModelReceived");
				if (decryptError && password!=null && !password.isEmpty()) password="";
				sendData(runResult);
				return;
			}
			if (input[1] instanceof String) {runResult=(String)input[1]; sendData(runResult); return;}

			/* Modell laden */
			CallcenterModel editModel=new CallcenterModel();
			s=editModel.loadFromStream((ByteArrayInputStream)input[1]);
			if (s!=null) {sendData(s); runResult=s; return;}

			/* Simulation durchführen */
			Object result=runSimulation(editModel);
			if (result instanceof String) {sendData((String)result); return;}

			/* Ergebnisse zurückschreiben */
			if (result instanceof Statistics) {
				try (ByteArrayOutputStream output=new ByteArrayOutputStream()) {
					if (!((Statistics)result).saveToStream(output)) {runResult=Language.tr("Server.Error.CouldNotSendStatistic"); return;}
					if (!sendData((byte)0,output)) {runResult=Language.tr("Server.Error.CouldNotSendResults"); return;}
				} catch (IOException e) {return;}
			}
		} finally {
			try {socket.close();} catch (IOException e) {}
		}
	}

	/**
	 * Wird aufgerufen, wenn der Simulationsthread abgebrochen werden soll.
	 */
	public void quit() {
		pleaseQuit=true;
	}
}
