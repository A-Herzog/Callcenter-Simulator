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

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.List;
import java.util.Locale;
import java.util.Vector;

import org.nanohttpd.protocols.http.IHTTPSession;
import org.nanohttpd.protocols.http.NanoHTTPD;
import org.nanohttpd.protocols.http.response.Response;
import org.nanohttpd.protocols.http.response.Status;

import language.Language;

/**
 * Führt einen Webserver aus, der konfigurierbar auf verschiedene Arten von Anfragen
 * reagieren kann.
 * @author Alexander Herzog
 * @version 1.0
 */
public class WebServerThread extends Thread {
	/**
	 * Soll sich der Server beenden?
	 */
	private boolean pleaseQuit=false;

	/**
	 * HTTP-Server
	 */
	private NanoHTTPD server;

	/**
	 * Liste der Handler für die Antworten auf verschiedene
	 * angefragt URLs
	 * @see #registerHandler(WebServerDataHandler)
	 */
	private final List<WebServerDataHandler> handlers;

	/**
	 * Port auf dem der Server auf Anfragen wartet.
	 */
	public final int port;

	/**
	 * Wird hier eine nicht leeres Array mit IP-Adressen übergeben, so werden nur Anfragen von diesen Adressen zugelassen.
	 */
	private final String[] permittedIPs;

	/**
	 * Konstruktor der Klasse <code>WebServerThread</code>
	 * @param port	Port, auf dem der Server auf Anfragen warten soll
	 * @param permittedIPs	Wird hier eine nicht leeres Array mit IP-Adressen übergeben, so werden nur Anfragen von diesen Adressen zugelassen.
	 */
	public WebServerThread(final int port, final String[] permittedIPs) {
		super();
		this.port=port;
		this.permittedIPs=permittedIPs;
		handlers=new Vector<>();
	}

	/**
	 * Registriert einen weiteren Handler, der bestimmte Anfragen beantwortet.
	 * @param handler	Weiterer Anfragen-Handler, der bei der Beantwortungen von Anfragen aufgerufen wird.
	 * @see	WebServerDataHandler
	 */
	public void registerHandler(WebServerDataHandler handler) {
		handlers.add(handler);
	}

	@Override
	public void run() {
		server=new NanoHTTPD(port) {
			@Override public Response serve(final IHTTPSession session) {return processRequest(session);}
		};
		try {
			server.start();
		} catch (IOException e) {
			showInfo(Language.tr("Server.WebServerSystem"),Language.tr("Server.Error.CannotListenForConnections"),true);
			server=null;
			finished();
			return;
		}

		showInfo(Language.tr("Server.WebServerSystem"),String.format(Language.tr("Server.HTTPStarted"),port),true);

		if (permittedIPs!=null && permittedIPs.length>0) {
			showInfo(Language.tr("Server.WebServerSystem"),Language.tr("Server.IPFilter.Active"),false);
			for (String s: permittedIPs) showInfo(Language.tr("Server.ServerSystem"),Language.tr("Server.IPFilter.AllowedIP")+": "+s,false);
		}

		started();

		while (!isInterrupted() && !pleaseQuit) {
			if (quitTest()) quit();
			try {sleep(100);} catch (InterruptedException e) {}
		}
		if (server!=null) server.stop();
		showInfo(Language.tr("Server.WebServerSystem"),Language.tr("Server.Terminated"),true);
		finished();
	}

	/**
	 * Wird aufgerufen, wenn der Server erfolgreich gestartet werden konnte.
	 */
	protected void started() {}

	/**
	 * Weist den Server-Thread an, sich zu beenden.
	 */
	public void quit() {
		pleaseQuit=true;
	}

	/**
	 * Kann in abgeleiteten Klassen überschreiben werden; prüft, ob der Server beendet
	 * werden soll und liefert <code>true</code> zurück, wenn dem so ist.
	 * @return	Soll der Server beendet werden?
	 */
	protected boolean quitTest() {
		return false;
	}

	/**
	 * Gibt Informationen über die Arbeit des Servers aus
	 * @param sender	Thread, auf den sich die Nachricht bezieht
	 * @param info		Nachrichtentext
	 * @param screenMessage	Nachricht auch im Logfile-Modus auf dem Bildschirm ausgeben
	 */
	public void showInfo(String sender, String info, boolean screenMessage) {}

	/**
	 * Verarbeitet eine Anfrage.
	 * @param url	Angefragt URL
	 * @param serverHost	Hostname dieses Servers
	 * @param language	Sprache für die Ausgabe
	 * @return	Antwortobjekt (bestehend aus Daten, Datenlänge und MIME-Type)
	 */
	private Object[] processRequest(String url, String serverHost, Locale language) {
		for (WebServerDataHandler handler : handlers) {
			Object[] result=handler.process(this,url,serverHost,language);
			if (result!=null && result.length>0) return result;
		}
		return null;
	}

	/**
	 * Liefert eine 404-Fehler-Antwort
	 * @return	404-Fehler-Antwort
	 */
	private Response getErrorResponse() {
		final Response nanoResponse=Response.newFixedLengthResponse(Status.NOT_FOUND,"text/plain; charset=utf-8",null,0);
		nanoResponse.addHeader("Cache-Control","no-cache, no-store, must-revalidate");
		return nanoResponse;
	}

	/**
	 * Erstellt ein Daten-Antwortobjekt
	 * @param data	Auszugebende Daten
	 * @param mime	Mime-Typ der Daten
	 * @param allowCaching	Darf der Client die Antwort cachen?
	 * @param additionalHeaders	Optionale zusätzliche Header-Felder (kann <code>null</code> sein)
	 * @return	Daten-Antwortobjekt
	 */
	private Response getDataResponse(final byte[] data, final String mime, boolean allowCaching, final String[] additionalHeaders) {

		final Response nanoResponse=Response.newFixedLengthResponse(Status.OK,mime,new ByteArrayInputStream(data),data.length);
		nanoResponse.addHeader("Cache-Control","no-cache, no-store, must-revalidate");

		if (allowCaching) {
			nanoResponse.addHeader("Cache-Control","max-age=86400, public");
		} else {
			nanoResponse.addHeader("Cache-Control","no-cache, no-store, must-revalidate");
		}

		if (additionalHeaders!=null) {
			int index=0;
			while (index<additionalHeaders.length-1) {
				nanoResponse.addHeader(additionalHeaders[index],additionalHeaders[index+1]);
				index+=2;
			}
		}
		return nanoResponse;
	}

	/**
	 * Verarbeitet eine http-Anfrage
	 * @param session	http-Anfrage
	 * @return	Antwortobjekt
	 */
	private Response processRequest(final IHTTPSession session) {
		/* Anfrage von zulässiger IP? */
		if (permittedIPs!=null && permittedIPs.length>0) {
			String clientIP=session.getRemoteIpAddress();
			boolean ok=false;
			for (String range : permittedIPs) {
				if (clientIP.toUpperCase().startsWith(range.toUpperCase())) {ok=true; break;}
			}
			if (!ok) return getErrorResponse();
		}

		/* Sprache der Anfrage */
		Locale language=null;
		List<String> lang=session.getParameters().get("Accept-Language");
		if (lang!=null && lang.size()>0) {
			LangEnd:
				for(String s: lang) {
					String[] list=s.split(",");
					for (String t: list) {
						if (t.toLowerCase().startsWith("de")) {language=Locale.GERMAN; break LangEnd;}
						if (t.toLowerCase().startsWith("en")) {language=Locale.ENGLISH; break LangEnd;}
					}
				}
		}
		if (language==null) language=Locale.ENGLISH;

		/* Wie heißt der Host aus Client-Sicht? */
		String host="";
		List<String> hostList=session.getParameters().get("Host");
		if (hostList!=null && hostList.size()>0) {
			String[] s=hostList.get(0).split(":");
			if (s!=null && s.length>0) host=s[0];
		}
		if (host.isEmpty()) try {host=InetAddress.getLocalHost().getHostName();} catch (UnknownHostException e) {host="localhost";}

		/* Anfrage bearbeiten */
		Object[] response=processRequest(session.getUri(),host,language);

		if (response==null || response.length==0) {
			return getErrorResponse();
		} else {
			return getDataResponse(
					(byte[])response[2],
					(String)response[0],
					response.length>=4 && ((Boolean)response[3]),
					(response.length>=5)?(String[])response[4]:new String[0]
					);
		}
	}

	/**
	 * Wird vom Server aufgerufen, wenn er sich beendet.
	 */
	protected void finished() {}
}
