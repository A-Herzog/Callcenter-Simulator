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

import java.util.Locale;

/**
 * Dieses Interface ermöglicht es verschiedenen Klassen
 * auf bestimmte Webserver-Anfragen zu reagieren.
 * @author Alexander Herzog
 * @see WebServerSystem
 */
public interface WebServerDataHandler {
	/**
	 * Verarbeitet einen Request
	 * @param server	Webserver-Thread
	 * @param url	Aufgerufene/zu verarbeitende URL
	 * @param remoteHost	Entfernter Host
	 * @param serverHost	Eigener Host
	 * @param language	Zu verwendende Sprache
	 * @return	Antwort auf die Anfrage, siehe z.B. {@link WebServerTools#buildHTMLResponse(String, boolean)}
	 * @see WebServerTools
	 */
	public Object[] process(WebServerThread server, String url, String remoteHost, String serverHost, Locale language);
}
