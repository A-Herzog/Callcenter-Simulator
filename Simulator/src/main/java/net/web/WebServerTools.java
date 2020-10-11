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
import java.nio.charset.Charset;
import java.util.Arrays;

import language.Language;

/**
 * Werkzeuge in Form von statischen Methoden, die die Bearbeitung von
 * Webserver-Anfragen vereinfachen sollen.
 * @author Alexander Herzog
 * @see	WebServerThread
 * @see	WebServerDataHandler
 */
public class WebServerTools {
	/**
	 * Konstruktor der Klasse<br>
	 * Diese Klasse kann nicht instanziert werden. Sie stellt nur statische Hilfsroutinen zur Verfügung.
	 */
	private WebServerTools() {}

	/**
	 * Erstellt ein Antwortobjekt, welches <code>WebServerDataHandler.process</code> im Fall einer HTML-Seite als Antwort zurückliefern kann.
	 * @param text	Inhalt der HTML-Seite
	 * @param needUTF8Encoding	Muss der Text vor der Ausgabe noch von ANSI nach UTF-8 konvertiert werden?
	 * @return	Antwortobjekt (bestehend aus Daten, Datenlänge und MIME-Type)
	 * @see WebServerDataHandler#process(WebServerThread, String, String, String, java.util.Locale)
	 */
	public static Object[] buildHTMLResponse(final String text, final boolean needUTF8Encoding) {
		if (text==null) return null;
		if (needUTF8Encoding) {
			byte[] b=Charset.forName("UTF-8").encode(text).array();
			int index=b.length; for (int i=0;i<b.length;i++) if (b[i]==0) {index=i; break;}
			return new Object[]{"text/html; charset=utf-8",index,Arrays.copyOf(b,index)};
		} else {
			return new Object[]{"text/html; charset=utf-8",text.length(),text.getBytes()};
		}
	}

	/**
	 * Erstellt ein Antwortobjekt, welches <code>WebServerDataHandler.process</code> im Fall einer TXT-Datei als Antwort zurückliefern kann.
	 * @param text	Inhalt der Textdatei
	 * @param needUTF8Encoding	Muss der Text vor der Ausgabe noch von ANSI nach UTF-8 konvertiert werden?
	 * @param downloadFileName	Dateiname unter dem die Datei zum Download angeboten werden soll (oder <code>null</code> wenn die Datei nicht als "zum Download" markiert werden soll)
	 * @return	Antwortobjekt (bestehend aus Daten, Datenlänge und MIME-Type)
	 * @see WebServerDataHandler#process(WebServerThread, String, String, String, java.util.Locale)
	 */
	public static Object[] buildTXTResponse(final String text, final boolean needUTF8Encoding, final String downloadFileName) {
		if (text==null) return null;
		if (needUTF8Encoding) {
			byte[] b=Charset.forName("UTF-8").encode(text).array();
			int index=b.length; for (int i=0;i<b.length;i++) if (b[i]==0) {index=i; break;}
			if (downloadFileName==null) {
				return new Object[]{"text/plain; charset=utf-8",index,Arrays.copyOf(b,index)};
			} else {
				return new Object[]{"text/plain; charset=utf-8",index,Arrays.copyOf(b,index),false,new String[]{"content-disposition","attachment; filename=\"" +downloadFileName+"\""}};
			}
		} else {
			if (downloadFileName==null) {
				return new Object[]{"text/plain; charset=utf-8",text.length(),text.getBytes()};
			} else {
				return new Object[]{"text/plain; charset=utf-8",text.length(),text.getBytes(),false,new String[]{"content-disposition","attachment; filename=\"" +downloadFileName+"\""}};
			}
		}
	}

	/**
	 * Erstellt ein Antwortobjekt, welches <code>WebServerDataHandler.process</code> im Fall einer XML-Datei als Antwort zurückliefern kann.
	 * @param text	Inhalt der XML-Datei
	 * @param needUTF8Encoding	Muss der Text vor der Ausgabe noch von ANSI nach UTF-8 konvertiert werden?
	 * @param downloadFileName	Dateiname unter dem die Datei zum Download angeboten werden soll (oder <code>null</code> wenn die Datei nicht als "zum Download" markiert werden soll)
	 * @return	Antwortobjekt (bestehend aus Daten, Datenlänge und MIME-Type)
	 * @see WebServerDataHandler#process(WebServerThread, String, String, String, java.util.Locale)
	 */
	public static Object[] buildXMLResponse(final String text, final boolean needUTF8Encoding, final String downloadFileName) {
		if (text==null) return null;
		byte[] b;
		if (needUTF8Encoding) {
			b=Charset.forName("UTF-8").encode(text).array();
		} else {
			b=text.getBytes();
		}
		if (downloadFileName==null) {
			return new Object[]{"text/xml; charset=utf-8",b.length,b};
		} else {
			return new Object[]{"text/xml; charset=utf-8",b.length,b,false,new String[]{"content-disposition","attachment; filename=\"" +downloadFileName+"\""}};
		}
	}

	/**
	 * Erstellt ein Antwortobjekt, welches <code>WebServerDataHandler.process</code> im Fall einer XML-Datei als Antwort zurückliefern kann.
	 * @param data	xml-Datei als bytes-Array
	 * @param downloadFileName	Dateiname unter dem die Datei zum Download angeboten werden soll (oder <code>null</code> wenn die Datei nicht als "zum Download" markiert werden soll)
	 * @return	Antwortobjekt (bestehend aus Daten, Datenlänge und MIME-Type)
	 * @see WebServerDataHandler#process(WebServerThread, String, String, String, java.util.Locale)
	 */
	public static Object[] buildXMLResponse(final byte[] data, final String downloadFileName) {
		if (data==null) return null;
		if (downloadFileName==null) {
			return new Object[]{"text/xml; charset=utf-8",data.length,data};
		} else {
			return new Object[]{"text/xml; charset=utf-8",data.length,data,false,new String[]{"content-disposition","attachment; filename=\"" +downloadFileName+"\""}};
		}
	}

	/**
	 * Erstellt ein Antwortobjekt, welches <code>WebServerDataHandler.process</code> im Fall einer CSS-Datei als Antwort zurückliefern kann.
	 * @param text	Inhalt der CSS-Datei
	 * @param needUTF8Encoding	Muss der Text vor der Ausgabe noch von ANSI nach UTF-8 konvertiert werden?
	 * @return	Antwortobjekt (bestehend aus Daten, Datenlänge und MIME-Type)
	 * @see WebServerDataHandler#process(WebServerThread, String, String, String, java.util.Locale)
	 */
	public static Object[] buildCSSResponse(final String text, final boolean needUTF8Encoding) {
		if (text==null) return null;
		byte[] b;
		if (needUTF8Encoding) {
			b=Charset.forName("UTF-8").encode(text).array();
		} else {
			b=text.getBytes();
		}
		return new Object[]{"text/css; charset=utf-8",b.length,b,true};
	}

	/**
	 * Erstellt ein Antwortobjekt, welches <code>WebServerDataHandler.process</code> im Fall einer Javascript-Datei als Antwort zurückliefern kann.
	 * @param text	Inhalt der Javascript-Datei
	 * @param needUTF8Encoding	Muss der Text vor der Ausgabe noch von ANSI nach UTF-8 konvertiert werden?
	 * @return	Antwortobjekt (bestehend aus Daten, Datenlänge und MIME-Type)
	 * @see WebServerDataHandler#process(WebServerThread, String, String, String, java.util.Locale)
	 */
	public static Object[] buildJSResponse(final String text, final boolean needUTF8Encoding) {
		if (text==null) return null;
		byte[] b;
		if (needUTF8Encoding) {
			b=Charset.forName("UTF-8").encode(text).array();
		} else {
			b=text.getBytes();
		}
		return new Object[]{"application/javascript; charset=utf-8",b.length,b};
	}

	/**
	 * Erstellt ein Antwortobjekt, welches <code>WebServerDataHandler.process</code> im Fall einer Javascript-Datei als Antwort zurückliefern kann.
	 * @param data	Javascript-Datei als bytes-Array
	 * @return	Antwortobjekt (bestehend aus Daten, Datenlänge und MIME-Type)
	 * @see WebServerDataHandler#process(WebServerThread, String, String, String, java.util.Locale)
	 */
	public static Object[] buildJSResponse(final byte[] data) {
		if (data==null) return null;
		return new Object[]{"application/javascript; charset=utf-8",data.length,data};
	}

	/**
	 * Erstellt ein Antwortobjekt, welches <code>WebServerDataHandler.process</code> im Fall einer Applets-jar-Datei als Antwort zurückliefern kann.
	 * @param data	jar-Datei als bytes-Array
	 * @return	Antwortobjekt (bestehend aus Daten, Datenlänge und MIME-Type)
	 * @see WebServerDataHandler#process(WebServerThread, String, String, String, java.util.Locale)
	 */
	public static Object[] buildAppletResponse(final byte[] data) {
		if (data==null) return null;
		return new Object[]{"application/x-java-applet",data.length,data};
	}

	/**
	 * Erstellt ein Antwortobjekt, welches <code>WebServerDataHandler.process</code> im Fall einer ico-Datei als Antwort zurückliefern kann.
	 * @param data	ico-Datei als bytes-Array
	 * @return	Antwortobjekt (bestehend aus Daten, Datenlänge und MIME-Type)
	 * @see WebServerDataHandler#process(WebServerThread, String, String, String, java.util.Locale)
	 */
	public static Object[] buildICOResponse(final byte[] data) {
		if (data==null) return null;
		return new Object[]{"image/x-icon",data.length,data};
	}

	/**
	 * Erstellt ein Antwortobjekt, welches <code>WebServerDataHandler.process</code> im Fall einer Bilddatei als Antwort zurückliefern kann.
	 * @param data	Bilddatei als bytes-Array
	 * @param	extension	Dateiendung ("png", "jpeg" oder "gif")
	 * @return	Antwortobjekt (bestehend aus Daten, Datenlänge und MIME-Type)
	 * @see WebServerDataHandler#process(WebServerThread, String, String, String, java.util.Locale)
	 */
	public static Object[] buildImageResponse(final byte[] data, final String extension) {
		if (data==null) return null;
		return new Object[]{"image/"+extension.toLowerCase(),data.length,data,true};
	}

	/**
	 * Prüft, ob eine URL mit einem bestimmten Segment beginnt und liefert in diesem Fall den Rest der URL (mit führendem "/" zurück)
	 * @param url	Zu prüfende URL
	 * @param start	Erstes URL-Segment (Groß- und Kleinschreibung wird nicht berücksichtigt; ohne führendes oder abschließendes "/")
	 * @return	Liefert im Erfolgsfall den Rest der URL mit einem führenden "/" zurück, sonst <code>null</code>.
	 */
	public static String testURLSegment(String url, final String start) {
		if (url==null) return null;
		url=url.trim();
		if (!url.toLowerCase().startsWith("/"+start.toLowerCase())) return null;
		url=url.substring(1+start.length()).trim();
		if (url.isEmpty()) url="/";
		return url;
	}

	/**
	 * Liefert den Rahmen für eine Webseite
	 * @param title	Titel der Seite
	 * @param content	Seiteninhalt
	 * @param info	Optional Text für eine Programminfo-Schaltfläche oben rechts (wird hier <code>null</code> oder ein leerer String übergeben, wird keine Schaltfläche angezeigt)
	 * @return	Aus Rahmen und Inhalt zusammengesetzte Webseite
	 */
	public static String getWebPageFrame(final String title, String content, final String info) {
		StringBuilder sb=new StringBuilder();

		content=content.replaceAll("P\\(W<=","P(W&le;");

		sb.append("<!DOCTYPE html>\n");
		sb.append("<html><head>\n");
		sb.append("  <meta http-equiv=\"Content-Type\" content=\"text/html; charset=UTF-8\">\n");
		sb.append("  <meta charset=\"utf-8\">\n");
		sb.append("  <meta http-equiv=\"X-UA-Compatible\" content=\"IE=edge\">\n");
		sb.append("  <meta content='True' name='HandheldFriendly'>\n");
		sb.append("  <meta name='viewport' content='width=device-width, initial-scale=1.0, maximum-scale=1.0, user-scalable=0'>\n");
		sb.append("  <meta name=\"apple-mobile-web-app-capable\" content=\"yes\">\n");
		sb.append("  <meta name=\"apple-mobile-web-app-status-bar-style\" content=\"black-translucent\">\n");
		sb.append("  <title>"+title+"</title>\n");
		sb.append("  <link rel=\"stylesheet\" type=\"text/css\" href=\"css.css\">\n");
		sb.append("</head><body>\n");
		sb.append("<div class=\"title noprint\" id=\"title\">\n");
		if (info!=null && !info.isEmpty()) sb.append("  <a style=\"float: right;\" class=\"toolbaricon\" href=\"javascript:alert('"+info+"\\n(c) Alexander Herzog');\" title=\""+Language.tr("Server.WebMenu.ProgramInfo")+"\"><div class=\"toolbaricon-any toolbaricon-info\">&nbsp;</div></a>\n");
		sb.append("  "+title+"\n");
		sb.append("</div>\n");
		sb.append(content);
		sb.append("</body></html>\n");

		return sb.toString();
	}

	/**
	 * Lädt eine Binärdatei aus den Ressourcen
	 * @param stream	Ressourcen-InputStream, aus dem geladen werden soll
	 * @param cutAtNul	Soll beim Lesen eines 0-Zeichens das Umwandeln abgebrochen werden?
	 * @return	Daten der Datei als Byte-Array oder im Fehlerfall <code>null</code>
	 */
	public static byte[] getBinaryFile(InputStream stream, boolean cutAtNul) {
		if (stream!=null) {
			try {
				byte[] b=new byte[32768];
				try (ByteArrayOutputStream out=new ByteArrayOutputStream()) {
					int count;
					while ((count=stream.read(b))>0) {
						if (cutAtNul) {
							int max=-1;
							for (int i=0;i<count;i++) if (b[i]==0) {max=i-1; break;}
							if (max>=0) {if (max>0) out.write(b,0,max); break;}
						}
						out.write(b,0,count);
					}
					return out.toByteArray();
				}
			} catch (IOException e) {}
		}
		return null;
	}

	/**
	 * Lädt eine Textdatei aus den Ressourcen
	 * @param stream	Ressourcen-InputStream, aus dem geladen werden soll
	 * @return	Zeilen der Datei als String oder im Fehlerfall <code>null</code>
	 */
	public static String getTextFile(InputStream stream) {
		byte[] b=getBinaryFile(stream,true);
		if (b==null || b.length==0) return null;
		return new String(b);
	}
}
