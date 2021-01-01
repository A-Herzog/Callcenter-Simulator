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

import java.awt.Component;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.PrintWriter;
import java.io.UnsupportedEncodingException;

import org.w3c.dom.Document;

import language.Language;
import simulator.Statistics;
import systemtools.MsgBox;
import tools.SetupData;
import xml.XMLTools;

/**
 * Diese Klasse erzeugt auf Basis der Statistikergebnisse einer
 * Simulation einen interaktiven Web-Viewer für diese Daten.
 * @author Alexander Herzog
 * @see Statistics
 */
public class StatisticWebAppWriter {
	/** Maximale Zeilenlänge bei der ausgabe */
	private static final int MIN_LINE_LENGTH=80;
	/** Pfad für die Bibliotheken für den Viewer */
	private static final String DEFAULT_VIEWER_URL=UpdateSystem.defaultProtocollHomepage+"://"+UpdateSystem.wwwHomeURL+"viewer/";

	/** Statistikerebnisse, die als Web-Viewer umgesetzt werden sollen */
	private final Statistics statistics;
	/** xml-Dokument der Statistikdaten */
	private Document resultDoc=null;
	/** html-Ausgabe-Daten */
	private String result=null;

	/**
	 * Konstruktor der Klasse
	 * @param statistics	Statistikerebnisse, die als Web-Viewer umgesetzt werden sollen
	 */
	public StatisticWebAppWriter(final Statistics statistics) {
		this.statistics=statistics;
	}

	/**
	 * Führt die eigentliche Verarbeitung durch
	 * @return	Gibt an, ob die Verarbeitung erfolgreich war
	 * @see #saveToString(String)
	 */
	private boolean process() {
		if (statistics==null) return false;
		if (result!=null) return true;

		resultDoc=statistics.saveToXMLDocument();
		String[] lines=XMLTools.xmlToJson(resultDoc.getDocumentElement(),true,false).split("\n");
		StringBuilder sb=new StringBuilder();
		StringBuilder temp=new StringBuilder();

		for (String s: lines) {
			s=s.trim();
			if (s.isEmpty()) continue;
			if (temp.length()>0) temp.append(' ');
			temp.append(s);
			if (temp.length()>MIN_LINE_LENGTH) {
				if (sb.length()>0) sb.append("\n");
				sb.append(temp);
				temp=new StringBuilder();
			}
		}
		if (temp.length()>0) {
			if (sb.length()>0) sb.append("\n");
			sb.append(temp);
		}
		result=sb.toString();
		return true;
	}

	/**
	 * Liefert den html-Kopf- und -Fuß-Bereich
	 * @param path	Internet-Pfad zu den JS-Bibliotheken und CSS-Dateien
	 * @param staticMode	Statische Server-Dateien (<code>true</code>) oder php-basierte Server-Dateien (<code>false</code>) einbinden
	 * @return	Liefert ein 3-elementiges Array: html-Kopfbereich bis in den JS-Bereich in body; JS-Bereich nach den einzufügenden js-Daten; Fußbereich
	 */
	private String[] getTemplate(final String path, final boolean staticMode) {
		String part1,part2,part3;
		StringBuilder sb;

		sb=new StringBuilder();
		sb.append("<!DOCTYPE html>\n");
		sb.append("<html>\n");
		sb.append("<head>\n");
		sb.append("  <meta http-equiv=\"Content-Type\" content=\"text/html; charset=UTF-8\">\n");
		sb.append("  <meta charset=\"utf-8\">\n");
		sb.append("  <meta http-equiv=\"X-UA-Compatible\" content=\"IE=edge\">\n");
		sb.append("  <meta content='True' name='HandheldFriendly'>\n");
		sb.append("  <meta name='viewport' content='width=device-width, initial-scale=1.0, maximum-scale=1.0, user-scalable=0'>\n");
		sb.append("  <meta name=\"apple-mobile-web-app-capable\" content=\"yes\">\n");
		sb.append("  <meta name=\"apple-mobile-web-app-status-bar-style\" content=\"black-translucent\">\n");
		sb.append("  <title>"+Language.tr("ViewerWebApp.Title")+"</title>\n");
		if (staticMode) {
			sb.append("  <link href=\""+path+"viewer-css.css\" rel=\"stylesheet\" type=\"text/css\">\n");
		} else {
			sb.append("  <link href=\""+path+"viewer-css.php\" rel=\"stylesheet\" type=\"text/css\">\n");
		}
		sb.append("</head>\n");
		sb.append("<body>\n");
		sb.append("<div class=\"title noprint\" id=\"title\">"+Language.tr("ViewerWebApp.Title")+"</div>\n");
		sb.append("<div id=\"status\">"+String.format(Language.tr("ViewerWebApp.Loading"),UpdateSystem.shortHomeURL)+"</div>\n");
		sb.append("<div id=\"content\"></div>\n");
		if (staticMode) {
			sb.append("<script src=\""+path+"viewer-lang.js\" type=\"application/javascript\" charset=\"UTF-8\"></script>\n");
			sb.append("<script src=\""+path+"viewer-js.js\" type=\"application/javascript\" charset=\"UTF-8\"></script>\n");
			sb.append("<script src=\""+path+"viewer-webapp.js\" type=\"application/javascript\" charset=\"UTF-8\"></script>\n");
		} else {
			sb.append("<script src=\""+path+"viewer-lang.php\" type=\"application/javascript\" charset=\"UTF-8\"></script>\n");
			sb.append("<script src=\""+path+"viewer-js.php\" type=\"application/javascript\" charset=\"UTF-8\"></script>\n");
			sb.append("<script src=\""+path+"viewer-webapp.js\" type=\"application/javascript\" charset=\"UTF-8\"></script>\n");
		}
		sb.append("<script type=\"text/javascript\">\n");
		sb.append("'use strict';\n");
		sb.append("var jsonData=");
		part1=sb.toString();

		sb=new StringBuilder();
		sb.append("\n");
		sb.append("var viewerLanguage=\""+Language.tr("ViewerWebApp.Language")+"\";\n");
		sb.append("var printCSS=\""+path+"viewer-css.php\";\n");
		sb.append("var printJS=\""+path+"viewer-js.php\";\n");
		sb.append("if (typeof initLanguage=='undefined') {\n");
		sb.append("  document.getElementById('status').innerHTML='<span style=\\\"color: red;\\\">"+Language.tr("ViewerWebApp.Error")+"</span>';\n");
		sb.append("} else {\n");
		sb.append("  initWebApp();\n");
		sb.append("}\n");
		sb.append("</script>\n");
		part2=sb.toString();

		sb=new StringBuilder();
		sb.append("</body>\n");
		sb.append("</html>\n");
		part3=sb.toString();

		return new String[]{part1,part2,part3};
	}

	/**
	 * Liefert das Ergebnis-html-Dokument als Zeichenkette
	 * @param path	Pfad für zusätzliche js-Dateien
	 * @return	Ergebnis-html-Dokument
	 */
	public String saveToString(final String path) {
		if (!process()) return null;
		String[] template=getTemplate(path,true);

		StringBuilder sb=new StringBuilder();

		sb.append(template[0]);
		sb.append(result);
		sb.append(template[1]);
		XMLTools xml=new XMLTools();
		String base64=xml.getBase64xml(resultDoc.getDocumentElement());
		if (base64!=null) {sb.append("<!--\nXMLDATA\n"); sb.append(base64); sb.append("\n-->\n");}
		sb.append(template[2]);

		return sb.toString();
	}

	/**
	 * Speichert das Ergebnis-html-Dokument als Datei
	 * und verwendet dabei die Standardadresse für die
	 * externen js-Dateien
	 * @param file	Ausgabedatei
	 * @return	Gibt an, ob die Erstellung erfolgreich war
	 */
	public boolean saveToFile(File file) {
		String path=SetupData.getSetup().viewerURL;
		if (path==null || path.isEmpty()) path=DEFAULT_VIEWER_URL;
		if (!path.endsWith("/")) path+="/";

		String data=saveToString(path);
		if (data==null) return false;
		try {
			try (PrintWriter pw=new PrintWriter(file,"UTF-8")) {
				pw.print(data);
			}
		} catch (FileNotFoundException | UnsupportedEncodingException e1) {return false;}

		/* Berücksichtigt den Zeichensatz nicht, Fehler bei Umlauten:
		try {
			FileWriter fw=new FileWriter(file);
			fw.write(data);
			fw.close();
		} catch (IOException e) {return false;}
		 */
		return true;
	}

	/**
	 * Zeigt einen Auswahldialog an in dem gewählt werden kann,
	 * ob die Statistikdaten als statischer Report oder als
	 * interaktiver Web-Viewer exportiert werden sollen.
	 * @param parentComponent	Übergeordnetes Element
	 * @return	Liefert im Falle der Wahl des Web-Viewers 0, im Falle des statische Reports 1 und im Falle eines Abbruchs -1
	 */
	public static int showSelectDialog(Component parentComponent) {
		final String[] optionTexts=new String[]{Language.tr("ViewerWebApp.Dialog.OptionWebApp"),Language.tr("ViewerWebApp.Dialog.OptionReport")};
		final String[] optionSubs=new String[]{Language.tr("ViewerWebApp.Dialog.OptionWebApp.Info"),Language.tr("ViewerWebApp.Dialog.OptionReport.Info")};
		return MsgBox.options(parentComponent,Language.tr("ViewerWebApp.Dialog.Title"),Language.tr("ViewerWebApp.Dialog.Label"),optionTexts,optionSubs);
	}
}
