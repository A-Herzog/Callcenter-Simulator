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

import java.io.IOException;
import java.io.InputStream;
import java.util.List;
import java.util.Locale;

import language.Language;
import ui.UpdateSystem;

/**
 * Webserver-Handler für die Anzeige des Menüs
 * @author Alexander Herzog
 * @see WebServerSystem
 * @see WebServerDataHandler
 */
public class HandlerMainMenu implements WebServerDataHandler {
	/** Applet anbieten */
	private final boolean showApplet;
	/** Ordner für die serverseitige Statistik-Anzeige */
	private final StatisticFolder[] showServerViewer;

	/**
	 * Konstruktor der Klasse
	 * @param showApplet	Applet anbieten
	 * @param showServerViewer	Ordner für die serverseitige Statistik-Anzeige
	 */
	public HandlerMainMenu(final boolean showApplet, final StatisticFolder[] showServerViewer) {
		this.showApplet=showApplet;
		this.showServerViewer=showServerViewer;
	}

	/**
	 * Konstruktor der Klasse
	 * @param showApplet	Applet anbieten
	 * @param showServerViewer	Ordner für die serverseitige Statistik-Anzeige
	 */
	public HandlerMainMenu(final boolean showApplet, final List<StatisticFolder> showServerViewer) {
		this.showApplet=showApplet;
		if (showServerViewer==null) this.showServerViewer=null; else this.showServerViewer=showServerViewer.toArray(new StatisticFolder[0]);
	}

	/**
	 * Liefert ein Bild über den Webserver aus
	 * @param imageFileName	Dateiname des Bildes (wird im Ressourcen-Ordner gesucht)
	 * @return	Antwortobjekt (bestehend aus Daten, Datenlänge und MIME-Type)
	 */
	private Object[] getImage(final String imageFileName) {
		if (imageFileName.contains("/") || imageFileName.contains("\\") || imageFileName.contains("..")) return null;
		String[] parts=imageFileName.split("\\.");
		if (parts==null || parts.length<2) return null;
		String extension=parts[parts.length-1].toLowerCase();
		if (extension.equals("jpg")) extension="jpeg";
		if (!extension.equals("png") && !extension.equals("gif") && !extension.equals("jpeg")) return null;

		try (InputStream stream1=HandlerMainMenu.class.getResourceAsStream("res/"+imageFileName)) {
			byte[] b=WebServerTools.getBinaryFile(stream1,false);
			try (InputStream stream2=HandlerMainMenu.class.getResourceAsStream("res/viewer/images/"+imageFileName)) {
				if (b==null) b=WebServerTools.getBinaryFile(stream2,false);
				return WebServerTools.buildImageResponse(b,extension);
			}
		} catch (IOException e) {
			return null;
		}
	}

	/**
	 * Liefert einen Eintrag für eine html-Liste
	 * @param link	Linkziel
	 * @param title	Titel des Eintrag
	 * @param icon	Icon für den Eintrag (kann <code>null</code> oder leer sein)
	 * @param isFirst	Ist dies der erste Eintrag?
	 * @return	Eintrag für html-Liste
	 * @see #getMenu()
	 */
	private String getLi(final String link, final String title, final String icon, final boolean isFirst) {
		String first=isFirst?" border-top: 1px solid #333;":"";
		String iconHtml="";
		if (icon!=null && !icon.isEmpty()) iconHtml="<img src=\"/images/"+icon+"\" alt=\""+title+"\" width=\"16\" height=\"16\"> ";
		return "  <li style=\"border-bottom: 1px solid #333;"+first+"\"><a style=\"display: block; padding: 10px 10px;\" href=\""+link+"\" target=\"_blank\">"+iconHtml+title+"</a></li>\n";
	}

	/**
	 * Erstellt das Hauptmenü.
	 * @return	Hauptmenü
	 * @see #process(WebServerThread, String, String, Locale)
	 */
	private String getMenu() {
		final StringBuilder sb=new StringBuilder();

		sb.append("<ul style=\"list-style-type: none; padding-left: 0;\">\n");
		sb.append(getLi("./viewer/",Language.tr("Server.WebMenu.Viewer.Local"),"icon_package_open.gif",true));
		if (showServerViewer!=null) for (int i=0;i<showServerViewer.length;i++) {
			sb.append(getLi("./viewer/"+(i+1)+"/list/",Language.tr("Server.WebMenu.Viewer.Server")+" <small>("+showServerViewer[i].name+")</small>","server.png",false));
			if (showServerViewer[i].isByDateAvailable()) {
				sb.append(getLi("./viewer/"+(i+1)+"/datelist/",Language.tr("Server.WebMenu.Viewer.ServerByDate")+" <small>("+showServerViewer[i].name+")</small>","calendar.png",false));
			}
		}
		/* sb.append(getLi(UpdateSystem.defaultProtocollHomepage+"://"+UpdateSystem.homeURL+"/viewer",Language.tr("Server.WebMenu.Viewer.WebService"),"server.png",false)); */
		if (showApplet) sb.append(getLi("./applet/",Language.tr("Server.WebMenu.Applet"),"Symbol.png",false));
		sb.append(getLi(UpdateSystem.defaultProtocollHomepage+"://"+UpdateSystem.wwwHomeURL,Language.tr("Server.WebMenu.Homepage"),"world.png",false));
		sb.append("</ul>\n");

		return WebServerTools.getWebPageFrame(Language.tr("Server.WebMenu.Title"),sb.toString(),Language.tr("Server.WebMenu.Title"));
	}

	@Override
	public Object[] process(WebServerThread server, String url, String serverHost, Locale language) {
		if (url.trim().equalsIgnoreCase("/favicon.ico")) {
			try (InputStream stream=HandlerMainMenu.class.getResourceAsStream("../res/Symbol.ico")) {
				return WebServerTools.buildICOResponse(WebServerTools.getBinaryFile(stream,false));
			} catch (IOException e) {
				return null;
			}
		}
		if (url.trim().toLowerCase().endsWith(".css") || url.trim().toLowerCase().endsWith("-css.php"))	{
			try (InputStream stream=HandlerMainMenu.class.getResourceAsStream("res/viewer/localtest.css")) {
				return WebServerTools.buildCSSResponse(WebServerTools.getTextFile(stream),true);
			} catch (IOException e) {
				return null;
			}
		}
		String urlImages=WebServerTools.testURLSegment(url,"images");
		if (urlImages!=null && urlImages.length()>1) return getImage(urlImages.substring(1));
		if (url.equals("/")) return WebServerTools.buildHTMLResponse(getMenu(),true);
		return null;
	}
}
