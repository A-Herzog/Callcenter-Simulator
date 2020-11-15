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

import java.util.ArrayList;
import java.util.List;

import language.Language;

/**
 * Kapselt eine Reihe von Server-Threads.
 * @author Alexander Herzog
 * @version 1.0
 */
public abstract class SimServerManager {
	/** Nummer des aktiveren Threads */
	private int taskNr=0;
	/** Port, auf dem auf eingehende Verbindungen gewartet werden soll. */
	private final int port;
	/** Zu verwendendes Passwort für die verschlüsselte Datenübertragung (leer oder <code>null</code> für unverschlüsselte Übertragung) */
	private final String password;
	/** Gibt die Maximalanzahl an zu verwendenden Threads an. (&le;0 um ohne Begrenzung zu arbeiten) */
	private final int maxThreads;
	/** Gibt eine Liste von IP-Adressen (oder auch Anfängen von Adressen) an, die berechtigt sind, den Server zu nutzen. Wird <code>null</code> oder eine leere Liste übergeben, so werden alle Adressen akzeptiert. */
	private final String[] permittedIPs;
	/** Rechen-Threads für die einzelnen Anfragen */
	private final List<SimServerThread> threads;
	/** Soll der Server geordnet herunterfahren? Dann keine neuen Aufträge mehr annehmen. */
	private boolean shutdown=false;

	/**
	 * Konstruktor der Klasse <code>SimServerManager</code>
	 * @param port	Port, auf dem auf eingehende Verbindungen gewartet werden soll.
	 * @param password	Zu verwendendes Passwort für die verschlüsselte Datenübertragung (leer oder <code>null</code> für unverschlüsselte Übertragung)
	 * @param maxThreads Gibt die Maximalanzahl an zu verwendenden Threads an. (&le;0 um ohne Begrenzung zu arbeiten)
	 * @param permittedIPs Gibt eine Liste von IP-Adressen (oder auch Anfängen von Adressen) an, die berechtigt sind, den Server zu nutzen. Wird <code>null</code> oder eine leere Liste übergeben, so werden alle Adressen akzeptiert.
	 */
	protected SimServerManager(int port, String password, int maxThreads, String[] permittedIPs) {
		super();
		this.port=port;
		this.password=password;
		this.maxThreads=maxThreads;
		this.permittedIPs=permittedIPs;
		threads=new ArrayList<SimServerThread>();
	}

	/**
	 * Startet die Server-Anwendung
	 */
	public final void runServer() {
		showInfo(Language.tr("Server.ServerSystem"),String.format(Language.tr("Server.ServerStarted"),port) ,false);

		if (permittedIPs!=null && permittedIPs.length>0) {
			showInfo(Language.tr("Server.ServerSystem"),Language.tr("Server.IPFilter.Active"),false);
			for (String s: permittedIPs) showInfo(Language.tr("Server.ServerSystem"),Language.tr("Server.IPFilter.AllowedIP")+": "+s,false);
		}

		startNewThread();

		while (true) {
			/* Wenn Enter-Tastendruck Rückmeldung an Nutzer und System in Shutdown-Modus versetzen. */
			if (!shutdown && quitServer()) {
				showInfo(Language.tr("Server.ServerSystem"),Language.tr("Server.TerminationRequest"),true);
				shutdown=true;
			}

			/* Warten ... */
			try {Thread.sleep(50);} catch (InterruptedException e) {}

			/* Fertige Threads aus der Liste entfernen */
			int i=0;
			while (i<threads.size()) if (removeCheck(threads.get(i))) threads.remove(i); else i++;

			/* Wenn im Shutdown-Modus und Threadliste leer => Ende */
			if (shutdown && threads.size()==0) break;

			/* Threads im Leerlauf beenden */
			if (shutdown) for (i=0;i<threads.size();i++) threads.get(i).quit();

		}

		showInfo(Language.tr("Server.ServerSystem"),Language.tr("Server.Terminated"),true);
		finished();
	}

	/**
	 * Gibt Informationen über die Arbeit des Servers aus
	 * @param sender	Thread, auf den sich die Nachricht bezieht
	 * @param info		Nachrichtentext
	 * @param screenMessage	Nachricht auch im Logfile-Modus auf dem Bildschirm ausgeben
	 */
	protected abstract void showInfo(String sender, String info, boolean screenMessage);

	/**
	 * Startet einen neuen Rechenthread.
	 */
	private final void startNewThread() {
		if (shutdown) return;
		ThreadStartWork notify=new ThreadStartWork();
		SimServerThread thread=new SimServerThread(port,password,maxThreads,permittedIPs,notify);
		notify.thread=thread;
		taskNr++;
		taskNr=taskNr%10000000;
		thread.setName("Task-"+String.format("%07d",taskNr));
		thread.start();
		threads.add(thread);
	}

	/**
	 * Callback das benachrichtigt werden soll, wenn
	 * der zugehörige Simulations-Thread zu arbeiten beginnt.
	 * @see SimServerThread
	 */
	private final class ThreadStartWork implements Runnable {
		/**
		 * Rechen-Thread auf den sich dieses Benachrichtigungs-Objekt bezieht
		 */
		public SimServerThread thread;

		@Override
		public void run() {
			showInfo(thread.getName(),String.format(Language.tr("Server.ConnectedWith"),thread.getClientIP()),false);
			startNewThread();
		}
	}

	/**
	 * Entfernt einen abgeschlossenen Thread aus der Liste der überwachten Threads.
	 * @param thread	Zu entfernendes Thread-Objekt
	 * @return	Gibt an, ob der Thread entfernt werden konnte
	 */
	private final boolean removeCheck(final SimServerThread thread) {
		if (thread.isAlive()) {
			if (!thread.isWorkStarted() && shutdown) thread.quit();
			return false;
		}
		String s=thread.getResult();
		if (s!=null) {
			showInfo(thread.getName(),String.format(Language.tr("Server.ThreadDoneError"),s),false); if (thread.isFatalError()) shutdown=true;
		} else {
			if (thread.isWorkStarted()) showInfo(thread.getName(),Language.tr("Server.ThreadDone"),false);
		}
		return true;
	}

	/**
	 * Liefert zurück, ob der Server beendet werden soll
	 * @return	true, wenn sich der Server beenden soll
	 */
	protected abstract boolean quitServer();

	/**
	 * Wird vom Server aufgerufen, wenn er sich beendet.
	 */
	protected void finished() {}
}
