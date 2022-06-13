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

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.util.Locale;

import language.Language;
import ui.MainFrame;

/**
 * Webserver-Handler der ein SaaS-Applet ausliefern kann
 * @author Alexander Herzog
 * @see WebServerSystem
 * @see WebServerDataHandler
 */
public class HandlerCalcServer implements WebServerDataHandler {
	/** Dem Client mitzuteilender Port des Rechenservers */
	private final int portCalc;
	/** Dem Client mitzuteilendes Passwort des Rechenservers */
	private final String passwordCalc;
	/** jar-Datei für das SaaS-Applet */
	private final File jarFile;

	/** jar-Datei für das SaaS-Applet */
	private byte[] jarData=null;

	/**
	 * Konstruktor der Klasse
	 * @param portCalc	Dem Client mitzuteilender Port des Rechenservers
	 * @param passwordCalc	Dem Client mitzuteilendes Passwort des Rechenservers
	 * @param jarFile	jar-Datei für das SaaS-Applet
	 */
	public HandlerCalcServer(final int portCalc, final String passwordCalc, final File jarFile) {
		this.portCalc=portCalc;
		this.passwordCalc=passwordCalc;
		this.jarFile=jarFile;
		this.jarData=null;
	}

	/**
	 * Konstruktor der Klasse
	 * @param portCalc	Dem Client mitzuteilender Port des Rechenservers
	 * @param passwordCalc	Dem Client mitzuteilendes Passwort des Rechenservers
	 * @param jarData	jar-Datei für das SaaS-Applet
	 */
	public HandlerCalcServer(final int portCalc, final String passwordCalc, final byte[] jarData) {
		this.portCalc=portCalc;
		this.passwordCalc=passwordCalc;
		this.jarFile=null;
		this.jarData=jarData;
	}

	/**
	 * Liefert das Haupt-HTML-Dokument für den Webserver
	 * @param portHtml	Server-Port
	 * @param serverHost	Server-URL
	 * @param language	Sprache
	 * @return	Antwortobjekt (bestehend aus Daten, Datenlänge und MIME-Type)
	 */
	private Object[] getRootDocument(int portHtml, String serverHost, Locale language) {
		String hostName=serverHost;
		String hostNameWithSlash=hostName;
		if (!hostNameWithSlash.endsWith("/")) hostNameWithSlash+="/";

		String langString="en";
		if (language.equals(Locale.GERMAN)) langString="de";

		StringBuilder sb=new StringBuilder();

		sb.append("<html><head><title>"+MainFrame.PROGRAM_NAME+"</title></head><body style=\"margin: 0; padding: 0;\">\n");
		sb.append("<applet code=\"FullSimulatorApplet.class\" archive=\"/applet/gui.jar\" width=\"100%\" height=\"100%\" style=\"background: #ddd; margin: 0px; padding: 0px;\" hspace=\"0\" vspace=\"0\" alt=\""+Language.tr("Server.NeedJavaApplet")+"\">\n");
		sb.append("<param name=\"serverURL\" value=\""+hostName+"\">\n");
		sb.append("<param name=\"serverPort\" value=\""+portCalc+"\">\n");
		sb.append("<param name=\"serverHtmlPort\" value=\""+portHtml+"\">\n");
		if (passwordCalc!=null && !passwordCalc.isEmpty()) sb.append("<param name=\"serverPassword\" value=\""+passwordCalc+"\">\n");
		sb.append("<param name=\"language\" value=\""+langString+"\">\n");
		sb.append(Language.tr("Server.Error.JavaNeeded"));
		sb.append("</applet>\n");
		sb.append("</body></html>\n");

		return WebServerTools.buildHTMLResponse(sb.toString(),true);
	}

	/**
	 * Liefert, wenn verfügbar, das Applet aus.
	 * @return	Antwortobjekt (bestehend aus Daten, Datenlänge und MIME-Type) oder <code>null</code>, wenn das Applet nicht zur Verfügung steht
	 */
	private Object[] getApplet() {
		if (jarData==null) {
			try {
				jarData=Files.readAllBytes(jarFile.toPath());
			} catch (IOException e) {jarData=null;}
		}

		if (jarData!=null) return WebServerTools.buildAppletResponse(jarData);
		return null;
	}

	@Override
	public Object[] process(WebServerThread server, String url, String serverHost, Locale language) {
		url=WebServerTools.testURLSegment(url,"applet");
		if (url==null) return null;

		if (url.equals("/")) return getRootDocument(server.port,serverHost,language);

		if (url.endsWith("/gui.jar")) return getApplet();

		/*
		if (url.startsWith("/id/")) {
			server.showInfo(Language.tr("Server.SaaSSystem"),String.format(Language.tr("Server.Error.ClientHasNoSaaSLicense"),remoteHost),false);
			try {
				url=URLDecoder.decode(url.substring(4),"UTF-8");
			} catch (UnsupportedEncodingException e) {return null;}
			String[] a=url.split(";");
			server.showInfo(Language.tr("Server.SaaSSystem"),Language.tr("Server.Error.ClientHasNoSaaSLicense.UserName")+": "+a[0],false);
			if (a.length>1)	server.showInfo(Language.tr("Server.SaaSSystem"),Language.tr("Server.Error.ClientHasNoSaaSLicense.SystemID")+": "+a[1],false);
			return null;
		}
		 */

		return null;
	}
}
