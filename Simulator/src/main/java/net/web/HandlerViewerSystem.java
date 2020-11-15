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

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.List;
import java.util.Locale;

import language.Language;
import simulator.Statistics;
import ui.StatisticWebAppWriter;
import ui.UpdateSystem;

/**
 * Webserver-Handler zur Auslieferung von Verzeichnisinhalten
 * @author Alexander Herzog
 * @see WebServerSystem
 * @see WebServerDataHandler
 */
public class HandlerViewerSystem implements WebServerDataHandler {
	/** Verzeichnisse mit Statistikdateien */
	private final StatisticFolder[] serverFolder;
	/** Verzeichnis mit Filterskripten */
	private final String filterFolder;

	/**
	 * Konstruktor der Klasse
	 * @param serverFolder	Verzeichnisse mit Statistikdateien
	 * @param filterFolder	Verzeichnis mit Filterskripten
	 */
	public HandlerViewerSystem(final StatisticFolder[] serverFolder, final String filterFolder) {
		this.serverFolder=serverFolder;
		this.filterFolder=filterFolder;
	}

	/**
	 * Konstruktor der Klasse
	 * @param serverFolder	Verzeichnisse mit Statistikdateien
	 * @param filterFolder	Verzeichnis mit Filterskripten
	 */
	public HandlerViewerSystem(final List<StatisticFolder> serverFolder, final String filterFolder) {
		this.serverFolder=serverFolder.toArray(new StatisticFolder[0]);
		this.filterFolder=filterFolder;
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
		try (InputStream stream1=HandlerViewerSystem.class.getResourceAsStream("res/"+imageFileName)) {
			byte[] b=WebServerTools.getBinaryFile(stream1,false);
			try (InputStream stream2=HandlerViewerSystem.class.getResourceAsStream("res/viewer/images/"+imageFileName)) {
				if (b==null) b=WebServerTools.getBinaryFile(stream2,false);
				return WebServerTools.buildImageResponse(b,extension);
			}
		} catch (IOException e) {
			return null;
		}
	}

	/**
	 * Prüft, ob die angegebene Bilddatei existiert und liefert diese im Erfolgsfall aus.
	 * @param url	Bilddatei
	 * @return	Antwortobjekt (bestehend aus Daten, Datenlänge und MIME-Type) oder im Fehlerfall <code>null</code>
	 */
	private Object[] testAndGetImage(final String url) {
		String urlImages=WebServerTools.testURLSegment(url,"images");
		if (urlImages!=null && urlImages.length()>1) return getImage(urlImages.substring(1));
		return null;
	}

	/**
	 * Liefert ein Beispielmodell aus.
	 * @param fileName	Beispielmodell
	 * @return	Antwortobjekt (bestehend aus Daten, Datenlänge und MIME-Type) oder im Fehlerfall <code>null</code>
	 */
	private Object[] getExample(final String fileName) {
		if (fileName.contains("/") || fileName.contains("\\") || fileName.contains("..")) return null;
		try (InputStream stream=HandlerViewerSystem.class.getResourceAsStream("res/viewer/examples/"+fileName)) {
			return WebServerTools.buildXMLResponse(WebServerTools.getTextFile(stream),false,null);
		} catch (IOException e) {
			return null;
		}
	}

	/**
	 * Prüft, ob ein Beispielmodell existiert und liefert dieses im Erfolgsfall aus.
	 * @param url	Name	Beispielmodell
	 * @return	Antwortobjekt (bestehend aus Daten, Datenlänge und MIME-Type) oder im Fehlerfall <code>null</code>
	 */
	private Object[] testAndGetExample(final String url) {
		String exampleUrl=WebServerTools.testURLSegment(url,"examples");
		if (exampleUrl!=null && exampleUrl.length()>1) return getExample(exampleUrl.substring(1));
		return null;
	}

	/**
	 * Liefert eine Liste mit allen Statistik-Dateien in einem Verzeichnis als html-Datei
	 * @param index	Index des Verzeichnisses mit den Statistikdateien in {@link #serverFolder}
	 * @return	Antwortobjekt (bestehend aus Daten, Datenlänge und MIME-Type)
	 */
	private Object[] getServerList(final int index) {
		String data=HandlerViewerSystemTools.getServerFileList(serverFolder[index].listFiles(),index,false);
		return WebServerTools.buildHTMLResponse(WebServerTools.getWebPageFrame(Language.tr("Server.WebMenu.ViewerList.Title"),data,Language.tr("Server.WebMenu.ViewerList.Title")),true);
	}

	/**
	 * Liefert eine Liste mit allen Statistik-Dateien nach Datum sortiert in einem Verzeichnis als html-Datei
	 * @param index	Index des Verzeichnisses mit den Statistikdateien in {@link #serverFolder}
	 * @return	Antwortobjekt (bestehend aus Daten, Datenlänge und MIME-Type)
	 */
	private Object[] getServerDateList(final int index) {
		String data=HandlerViewerSystemTools.getServerDateFileList(serverFolder[index],index);
		return WebServerTools.buildHTMLResponse(WebServerTools.getWebPageFrame(Language.tr("Server.WebMenu.ViewerList.Title"),data,Language.tr("Server.WebMenu.ViewerList.Title")),true);
	}

	/**
	 * Liefert Statistikdaten als Viewer aus
	 * @param index	Index des Verzeichnisses mit den Statistikdateien in {@link #serverFolder}
	 * @param xmlFile	Statistik-Datei
	 * @param serverHost	Server-Name
	 * @return	Antwortobjekt (bestehend aus Daten, Datenlänge und MIME-Type) oder im Fehlerfall <code>null</code>
	 */
	private Object[] directViewer(int index, String xmlFile, String serverHost) {
		Statistics statistic=serverFolder[index].getStatisticFromFile(xmlFile);
		if (statistic==null) return null;

		StatisticWebAppWriter writer=new StatisticWebAppWriter(statistic);
		String html=writer.saveToString("");
		html=html.replace(UpdateSystem.shortHomeURL,serverHost);
		return WebServerTools.buildHTMLResponse(html,true);
	}

	/**
	 * Liefert Statistikdaten als Viewer aus
	 * @param index	Index des Verzeichnisses mit den Statistikdateien in {@link #serverFolder}
	 * @param xmlFile	Statistik-Datei
	 * @return	Antwortobjekt (bestehend aus Daten, Datenlänge und MIME-Type) oder im Fehlerfall <code>null</code>
	 */
	private Object[] dataViewer(int index, String xmlFile) {
		Statistics statistic=serverFolder[index].getStatisticFromFile(xmlFile);
		if (statistic==null) return null;

		String content=HandlerViewerSystemTools.getModelAndStatisticData(index,statistic,xmlFile,filterFolder);
		if (content==null || content.isEmpty()) return null;

		return WebServerTools.buildHTMLResponse(WebServerTools.getWebPageFrame(Language.tr("Server.WebMenu.ViewerData.Title"),content,Language.tr("Server.WebMenu.ViewerData.Title")),true);
	}

	/**
	 * Liefert ein Filter-Skript aus.
	 * @param index	Index des Verzeichnisses mit den Statistikdateien in {@link #serverFolder}
	 * @param xmlFile	Statistik-Datei
	 * @return	Antwortobjekt (bestehend aus Daten, Datenlänge und MIME-Type) oder im Fehlerfall <code>null</code>
	 */
	private Object[] dataFilter(final int index, final String xmlFile) {
		if (xmlFile==null) return null;
		String[] parts=xmlFile.split("\\?");
		if (parts==null || parts.length!=2) return null;

		Statistics statistic=serverFolder[index].getStatisticFromFile(parts[0]);
		if (statistic==null) return null;

		FastAccessFolder fastAccessFolder=new FastAccessFolder(filterFolder);
		String result=fastAccessFolder.runScript(statistic.saveToXMLDocument(),parts[1]);
		if (result==null) return null;

		return WebServerTools.buildTXTResponse(result,true,"Filter.txt");
	}

	/**
	 * Liefert das Modell als Download.
	 * @param index	Index des Verzeichnisses mit den Statistikdateien in {@link #serverFolder}
	 * @param xmlFile	Statistik-Datei
	 * @return	Antwortobjekt (bestehend aus Daten, Datenlänge und MIME-Type) oder im Fehlerfall <code>null</code>
	 */
	private Object[] downloadModel(final int index, final String xmlFile) {
		Statistics statistic=serverFolder[index].getStatisticFromFile(xmlFile);
		if (statistic==null) return null;

		try (ByteArrayOutputStream out=new ByteArrayOutputStream()) {
			if (!statistic.editModel.saveToStream(out,false)) return null;
			return WebServerTools.buildXMLResponse(out.toByteArray(),"model-"+xmlFile);
		} catch (IOException e) {return null;}
	}

	/**
	 * Liefert die Statistikdaten als Download.
	 * @param index	Index des Verzeichnisses mit den Statistikdateien in {@link #serverFolder}
	 * @param xmlFile	Statistik-Datei
	 * @return	Antwortobjekt (bestehend aus Daten, Datenlänge und MIME-Type) oder im Fehlerfall <code>null</code>
	 */
	private Object[] downloadStatistic(final int index, final String xmlFile) {
		Statistics statistic=serverFolder[index].getStatisticFromFile(xmlFile);
		if (statistic==null) return null;

		try (ByteArrayOutputStream out=new ByteArrayOutputStream()) {
			if (!statistic.saveToStream(out)) return null;
			return WebServerTools.buildXMLResponse(out.toByteArray(),xmlFile);
		} catch (IOException e) {return null;}
	}

	/**
	 * Liefert die Hauptdatei aus.
	 * @param language	Sprache
	 * @param serverHost	Server-Name
	 * @return	html-Code der Hauptdatei
	 */
	private String viewerRoot(final Locale language, final String serverHost) {
		try (InputStream stream=language.equals(Locale.GERMAN)?HandlerViewerSystem.class.getResourceAsStream("res/viewer_nonlocal/index_de.php"):HandlerViewerSystem.class.getResourceAsStream("res/viewer_nonlocal/index_en.php")) {
			String html=WebServerTools.getTextFile(stream);
			if (html!=null) html=html.replace(UpdateSystem.shortHomeURL,serverHost);
			return html;
		} catch (IOException e) {
			return null;
		}
	}

	@Override
	public Object[] process(WebServerThread server, String url, String remoteHost, String serverHost, Locale language) {
		Object[] obj;

		url=WebServerTools.testURLSegment(url,"viewer");
		if (url==null) return null;

		/* Evtl. Index aus der URL extrahieren */

		int index=-1;
		if (serverFolder!=null) for (int i=0;i<serverFolder.length;i++) if (serverFolder[i].isDirectory()) {
			String s=WebServerTools.testURLSegment(url,""+(i+1));
			if (s!=null) {url=s; index=i; break; }
		}

		/* Viewer-System, zugehörige Bilder und Beispiele (für allgemeinen Viewer und konkrete Index-Daten */

		if (index<0 && url.equals("/")) return WebServerTools.buildHTMLResponse(viewerRoot(language,serverHost),false);

		if (url.equalsIgnoreCase("/viewer-lang.php")) try (InputStream stream=HandlerViewerSystem.class.getResourceAsStream("res/viewer/local_test.lang")) {
			return WebServerTools.buildJSResponse(WebServerTools.getBinaryFile(stream,true));
		} catch (IOException e) {
			return null;
		}

		if (url.equalsIgnoreCase("/viewer-js.php")) try (InputStream stream=HandlerViewerSystem.class.getResourceAsStream("res/viewer/local_test.js")) {
			return WebServerTools.buildJSResponse(WebServerTools.getBinaryFile(stream,true));
		} catch (IOException e) {
			return null;
		}
		if (url.equalsIgnoreCase("/viewer.js")) try (InputStream stream=HandlerViewerSystem.class.getResourceAsStream("res/viewer_nonlocal/viewer.js")) {
			return WebServerTools.buildJSResponse(WebServerTools.getBinaryFile(stream,true));
		} catch (IOException e) {
			return null;
		}
		if (url.equalsIgnoreCase("/viewer-webapp.js")) try (InputStream stream=HandlerViewerSystem.class.getResourceAsStream("res/viewer_nonlocal/viewer-webapp.js")) {
			return WebServerTools.buildJSResponse(WebServerTools.getBinaryFile(stream,true));
		} catch (IOException e) {
			return null;
		}

		obj=testAndGetImage(url); if (obj!=null) return obj;
		if (index<0) {obj=testAndGetExample(url); if (obj!=null) return obj;}

		/* Ab hier nur noch für bestimmte Index-Daten */

		if (index<0) return null;

		String listUrl=WebServerTools.testURLSegment(url,"list");
		if (listUrl!=null) {
			if (listUrl.equals("/")) return getServerList(index);
			obj=testAndGetImage(url); if (obj!=null) return obj;
		}

		String dateListUrl=WebServerTools.testURLSegment(url,"datelist");
		if (dateListUrl!=null) {
			if (dateListUrl.equals("/")) return getServerDateList(index);
			obj=testAndGetImage(url); if (obj!=null) return obj;
		}

		String dataUrl=WebServerTools.testURLSegment(url,"data");
		if (dataUrl!=null) {
			obj=testAndGetImage(url); if (obj!=null) return obj;
			if (dataUrl.length()>1) return dataViewer(index,dataUrl.substring(1));
		}

		String filterUrl=WebServerTools.testURLSegment(url,"filter");
		if (filterUrl!=null && filterUrl.length()>1) return dataFilter(index,filterUrl.substring(1));

		String download1Url=WebServerTools.testURLSegment(url,"downloadmodel");
		if (download1Url!=null && download1Url.length()>1) return downloadModel(index,download1Url.substring(1));

		String download2Url=WebServerTools.testURLSegment(url,"downloadstatistic");
		if (download2Url!=null && download2Url.length()>1) return downloadStatistic(index,download2Url.substring(1));

		if (url.length()>1) return directViewer(index,url.substring(1),serverHost);

		return null;
	}
}