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
import java.io.DataInputStream;
import java.io.IOException;
import java.net.Socket;

import language.Language;
import simulator.Statistics;
import ui.model.CallcenterModel;

/**
 * Simulations-Client-Thread
 * @author Alexander Herzog
 * @version 1.0
 */
public final class SimClientThread extends NetworkThread {
	/**
	 * Netzwerkname des Servers
	 */
	private final String serverName;

	/**
	 * Objekt vom Typ {@link CallcenterModel}, welches simuliert werden soll
	 */
	private final CallcenterModel editModel;

	/**
	 * Im Erfolgsfall das Statistik-Objekt mit dem Ergebnissen
	 * @see #getStatistics()
	 */
	private Statistics statistics;

	/**
	 * Im Falle eines Fehlers die Fehlermeldung
	 * @see #getErrorMessage()
	 */
	private String errorMessage;

	/**
	 * Priorität, die der Server für die Simulation verwenden soll (1: niedrig, 2: normal)
	 * @see #setPriority(int)
	 */
	private int setPriority;

	/**
	 * Anzahl an simulierten Ereignissen
	 * @see #getEventCount()
	 */
	private long eventCount=0;

	/**
	 * Pro Sekunde simulierte Ereignisse
	 * @see #getEventsPerSecond()
	 */
	private int eventsPerSecond=0;

	/**
	 * Gerade in Arbeit befindlicher Tag
	 * @see #getSimDayCount()
	 */
	private int simDayCount=0;

	/**
	 * Konstruktor der Klasse <code>SimClientThread</code>
	 * @param serverName	Netzwerkname des Servers
	 * @param serverPort	Port, auf dem der Server auf Aufträge wartet
	 * @param serverPassword	Optionales Passwort
	 * @param editModel		Objekt vom Typ <code>CallcenterModel</code>, welches simuliert werden soll
	 */
	public SimClientThread(String serverName, int serverPort, String serverPassword, CallcenterModel editModel) {
		super(serverPort,serverPassword);
		this.serverName=serverName;
		this.editModel=editModel;
	}

	/**
	 * Gibt nach Ende des Threads im Erfolgsfall ein Statistik-Objekt zurück
	 * @return	Im Erfolgsfall das Statistik-Objekt mit dem Ergebnissen, sonst <code>null</code>
	 */
	public Statistics getStatistics() {
		return statistics;
	}

	/**
	 * Gibt nach Ende des Threads im Falle eines Fehlers die Fehlermeldung zurück.
	 * @return	Im Falle eines Fehlers die Fehlermeldung, sonst <code>null</code>
	 */
	public String getErrorMessage() {
		return errorMessage;
	}

	/**
	 * Sender ein einzelnes Byte an den Server
	 * @param b	Zu sendendes Byte
	 * @see #run()
	 */
	@SuppressWarnings("resource")
	private void sendByte(final int b) {
		try {socket.getOutputStream().write(b);} catch (IOException e) {}
	}

	@Override
	public void run() {
		/* Verbindung aufbauen */
		try {socket=new Socket(serverName,port);} catch (IOException e) {errorMessage=String.format(Language.tr("Server.Error.CouldNotConnectToServer"),serverName); return;}

		try {
			/* Modell senden */
			try (ByteArrayOutputStream output=new ByteArrayOutputStream()) {
				if (!editModel.saveToStream(output,false)) {errorMessage=Language.tr("Server.Error.CouldNotSaveModel"); return;}
				if (!sendData((byte)0,output)) {errorMessage=Language.tr("Server.Error.CouldNotTransferModel"); return;}

				/* Auf Ergebnis warten */
				Object[] result;
				while (true) {
					result=receiveData();
					if (setPriority!=0) {
						sendByte(setPriority);
						setPriority=0;
					}
					if (isInterrupted()) {sendByte(0); return;}
					if (result==null || result[1]==null) {errorMessage=Language.tr("Server.Error.NoResultsReceived"); return;}
					if (result[1] instanceof String) {errorMessage=Language.tr("Server.Server")+": "+(String)result[1]; return;}
					if ((Integer)result[0]==0) break;
					if ((Integer)result[0]==1) {
						DataInputStream status=new DataInputStream((ByteArrayInputStream)result[1]);
						try {
							eventCount=status.readLong();
							eventsPerSecond=status.readInt();
							simDayCount=status.readInt();
						} catch (IOException e) {}
					}
				}

				/* Statistik laden und zurück liefern */
				statistics=new Statistics(null,null,0,0);
				String s=statistics.loadFromStream((ByteArrayInputStream)result[1]);
				if (s!=null) {errorMessage=String.format(Language.tr("Server.Error.LoadStatistic"),s); statistics=null; return;}
			} catch (IOException e) {}
		} finally {
			try {socket.close();} catch (IOException e) {}
		}
	}

	/**
	 * Stellt die Thread-Priorität ein
	 * @param low	Niedrige Priorität (<code>true</code>) oder normale Priorität (<code>false</code>)
	 */
	public void setServerPriority(boolean low) {
		setPriority=low?1:2;
	}

	/**
	 * Liefert die Anzahl an simulierten Ereignissen.
	 * @return	Anzahl an simulierten Ereignissen
	 */
	public long getEventCount() {
		return eventCount;
	}

	/**
	 * Liefert die Anzahl an pro Sekunde simulierten Ereignissen.
	 * @return	Pro Sekunde simulierte Ereignisse
	 */
	public int getEventsPerSecond() {
		return eventsPerSecond;
	}

	/**
	 * Gibt die Nummer des gerade im Simulator in Bearbeitung befindlichen Tages zurück.
	 * @return Gerade in Arbeit befindlicher Tag
	 */
	public int getSimDayCount() {
		return simDayCount;
	}

	/**
	 * Liefer die insgesamt zu simulierende Anzahl an Tagen.
	 * @return	Anzahl an zu simulierenden Tagen
	 */
	public int getSimDaysCount() {
		return editModel.days;
	}
}
