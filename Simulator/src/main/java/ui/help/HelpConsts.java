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
package ui.help;

import systemtools.help.HelpBase;

/**
 * Hilfekonstanten
 * @author Alexander Herzog
 * @see Help
 */
public class HelpConsts {
	/**
	 * Konstruktor der Klasse<br>
	 * Diese Klasse kann nicht instanziert werden. Sie enthält nur statische Konstanten.
	 */
	private HelpConsts() {}

	/** Startseite der Hilfe */
	public static final String CONTENT_PAGE="Start";

	/** Willkommensweise im Programmfenster im Menü-Modus */
	public static final String WELCOME_PAGE="Willkommen";
	/** Willkommensweise im Programmfenster im Ribbons-Modus */
	public static final String WELCOME_PAGE_RIBBON="WillkommenRibbon";
	/** Willkommensweise im Programmfenster im Menü-Modus (Shareware) */
	public static final String WELCOME_PAGE_SHAREWARE="WillkommenShareware";
	/** Willkommensweise im Programmfenster im Ribbons-Modus (Shareware) */
	public static final String WELCOME_PAGE_RIBBON_SHAREWARE="WillkommenRibbonShareware";

	/** Verzeichnis mit den Hilfedateien */
	public static final String LANGUAGE_FOLDER="pages_%s";
	/** Verzeichnis mit den Hilfedateien im Fallback-Modus wenn die Spracheinstellung keine Hilfe hat */
	public static final String FALLBACK_FOLDER="pages_de";

	static {
		HelpBase.CONTENT_PAGE=CONTENT_PAGE+".html";
	}
}
