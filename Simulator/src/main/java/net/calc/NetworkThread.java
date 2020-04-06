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
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.PrintWriter;
import java.net.Socket;
import java.net.SocketTimeoutException;
import java.util.Scanner;
import java.util.zip.GZIPInputStream;
import java.util.zip.GZIPOutputStream;

import xml.ChiperTools;

/**
 * Basisklasse zur Datenübertragung über das Netzwerk
 * @author Alexander Herzog
 * @version 1.0
 */
public class NetworkThread extends Thread {
	/**
	 * Zu verwendende Portnummer auf dem Server.
	 * (Wird bereits durch den Konstruktor gesetzt.)
	 */
	protected final int port;

	/**
	 * Zu verwendendes Passwort für die Datenübertragung.
	 * (Wird bereits durch den Konstruktor gesetzt.)
	 */
	protected String password;

	/**
	 * Client-Socket, der zur Datenübertragung verwendet werden soll.
	 * (Muss von abgeleiteten Klassen gesetzt werden.)
	 */
	protected Socket socket;

	/**
	 * Hat receiveData deshalb nichts geliefert, weil sich die Daten nicht entschlüsseln ließen?
	 */
	protected boolean decryptError=false;

	/**
	 * Konstruktor der Klasse <code>NetworkThread</code>
	 * @param port	Portnummer auf dem Server
	 * @param password	Optionales Passwort (kann <code>null</code> oder leer sein)
	 */
	public NetworkThread(int port, String password) {
		this.port=port;
		this.password=password;
	}

	/**
	 * Liefert die eigene IP-Adresse
	 * @return	Eigene IP-Adresse
	 */
	protected final String getOwnIP() {
		return socket.getLocalAddress().getHostAddress();
	}

	private final ByteArrayOutputStream compress(ByteArrayOutputStream data) {
		ByteArrayOutputStream result=new ByteArrayOutputStream();

		try {
			try (GZIPOutputStream zip=new GZIPOutputStream(result)) {
				data.writeTo(zip);
			}
		} catch (IOException e) {return null;}

		return result;
	}

	private final ByteArrayInputStream decompress(ByteArrayInputStream data) {
		ByteArrayOutputStream bufferStream;
		byte[] buf=new byte[32768];
		int count;

		try {
			GZIPInputStream zip=new GZIPInputStream(data);
			bufferStream=new ByteArrayOutputStream();
			while ((count=zip.read(buf))!=-1) bufferStream.write(buf,0,count);
		} catch (IOException e) {return null;}

		return new ByteArrayInputStream(bufferStream.toByteArray());
	}

	private final byte[] getSize(ByteArrayOutputStream data) {
		byte[] b=new byte[4];
		int s=data.size();
		for (int i=0;i<3;i++) {b[i]=(byte)(s%256); s/=256;}
		return b;
	}

	private final int getSize(byte[] data) {
		int value=(0xFF & data[3])<<24;
		value|=(0xFF & data[2])<<16;
		value|=(0xFF & data[1])<<8;
		value|=(0xFF & data[0]);
		return value;
	}

	private final boolean sendDataIntern(byte type, ByteArrayOutputStream data) {
		try {
			@SuppressWarnings("resource")
			OutputStream out=socket.getOutputStream();
			out.write(type);
			out.write(getSize(data));
			data.writeTo(out);
			out.flush();
		} catch (IOException e) {return false;}
		return true;
	}

	/**
	 * Sendet einen Stream
	 * @param type		Benutzerdefinierbarer Type, der dem Empfänger angibt, was in dem Stream steht (es muss <code>type</code>>=0 gelten)
	 * @param output	Zu sendender Stream
	 * @return			Gibt true zurück, wenn die Daten erfolgreich abgesendet werden konnten
	 */
	protected final boolean sendData(byte type, ByteArrayOutputStream output) {
		/* Komprimieren */
		ByteArrayOutputStream data=compress(output);
		if (data==null) return false;

		/* Ggf. verschlüsseln */
		ByteArrayOutputStream data2;
		if (password==null || password.isEmpty()) data2=data; else data2=xml.ChiperTools.encrypt(data,password);
		if (data2==null) return false;

		/* Senden */
		return sendDataIntern(type,data2);
	}

	/**
	 * Sendet eine Zeichenkette
	 * @param s	Zu sendende Zeichenkette
	 * @return	Gibt true zurück, wenn die Daten erfolgreich abgesendet werden konnten
	 */
	protected final boolean sendData(String s) {
		/* Zeichenkette in Stream schreiben */
		ByteArrayOutputStream data=new ByteArrayOutputStream();
		PrintWriter pw=new PrintWriter(data);
		pw.println(s);
		pw.flush();

		/* Ggf. verschlüsseln */
		ByteArrayOutputStream data2;
		if (password==null || password.isEmpty()) data2=data; else data2=ChiperTools.encrypt(data,password);
		if (data2==null) return false;

		/* Senden */
		return sendDataIntern((byte)-1,data2);
	}

	private final ByteArrayOutputStream receiveDataIntern(int expectedSize, int timeOut) {
		byte[] b=new byte[expectedSize];
		int count=0;

		int time=0;
		while (!isInterrupted()) try {
			if (timeOut>0 && time>timeOut) return null;
			socket.setSoTimeout(50);
			time+=50;
			@SuppressWarnings("resource")
			InputStream in=socket.getInputStream();
			int c=in.read(b,count,expectedSize-count);
			if (c>0) {
				count+=c;
				if (count>=expectedSize) {
					ByteArrayOutputStream result=new ByteArrayOutputStream(expectedSize);
					result.write(b);
					return result;
				}
			}
		} catch (IOException e) {
			if (e instanceof SocketTimeoutException && !isInterrupted()) continue;
			return null;
		}
		return null;
	}

	private final ByteArrayOutputStream receiveDataIntern(int expectedSize) {
		return receiveDataIntern(expectedSize,0);
	}

	/**
	 * Empfängt Daten über das Netzwerk.
	 * @return	Liefert ein Array aus zwei Komponenten: 1. Type des Objekts, 2. Objekt vom Type <code>ByteArrayInputStream</code> oder <code>String</code>, oder im Fehlerfall <code>null</code>.
	 */
	protected final Object[] receiveData() {
		ByteArrayOutputStream data;

		data=receiveDataIntern(1,10000);
		if (data==null) return null;
		int type=data.toByteArray()[0];

		data=receiveDataIntern(4);
		if (data==null) return null;
		int size=getSize(data.toByteArray());

		data=receiveDataIntern(size);
		if (data==null) return null;

		ByteArrayOutputStream data2;
		if (password==null || password.isEmpty()) data2=data; else data2=ChiperTools.decrypt(data,password);
		if (data2==null) return null;

		Object[] obj=new Object[2];
		obj[0]=type;
		if (type==-1) {
			StringBuilder s=new StringBuilder();
			try (Scanner scanner=new Scanner(new ByteArrayInputStream(data2.toByteArray()))) {
				scanner.useDelimiter("\n");
				while (scanner.hasNext()) s.append(scanner.next());
				obj[1]=s.toString();
			}
		} else {
			obj[1]=decompress(new ByteArrayInputStream(data2.toByteArray()));
		}
		return obj;
	}

	@Override
	public void run() {
	}
}
