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

import java.awt.Container;
import java.net.URL;
import java.util.function.Consumer;

import javax.swing.JPanel;

import language.Language;
import systemtools.help.HelpBase;

/**
 * Ermöglicht die Anzeige von html-basierten Hilfeseiten.
 * @author Alexander Herzog
 */
public class Help extends HelpBase {
	/**
	 * Konstruktor der Klasse
	 * @param parent	Übergeordnetes Element
	 * @param topic	Anzuzeigende Hilfeseite
	 * @param modal	Modeler Dialog (<code>true</code>) oder normales Fenster (<code>false</code>)
	 */
	private Help(final Container parent, final String topic, final boolean modal) {
		super(parent,topic,modal);
	}

	@Override
	protected URL getPageURL(String res) {
		/* Die html-Dateien müssen in einem Unterordner namens "pages" des Ordners, in dem sich diese Datei befindet, liegen. */
		return getClass().getResource("pages_"+Language.tr("Numbers.Language")+"/"+res);
	}

	/**
	 * Zeigt eine Hilfeseite als nicht-modales Fenster an
	 * @param parent	Übergeordnetes Element
	 * @param topic	Anzuzeigendes Thema (Dateiname ohne ".html"-Endung). Kann leer sein, es wird dann die Startseite angezeigt.
	 */
	public static void topic(final Container parent, final String topic) {
		new Help(parent,topic,false);
	}

	/**
	 * Zeigt eine Hilfeseite als modalen Dialog an
	 * @param parent	Übergeordnetes Element
	 * @param topic	Anzuzeigendes Thema (Dateiname ohne ".html"-Endung). Kann leer sein, es wird dann die Startseite angezeigt.
	 */
	public static void topicModal(final Container parent, final String topic) {
		new Help(parent,topic,true);
	}

	/**
	 * Listener, der aufgerufen wird, wenn ein spezieller Link (beginnend mit "special:") angeklickt wird
	 * @see #infoPanel(String, Consumer, boolean)
	 */
	private Consumer<String> specialLinkListener;

	/**
	 * Listener, der beim Anklicken von "special:..."-Links aus der
	 * globalen Hilfe heraus benachrichtigt werden soll.
	 */
	public static Consumer<String> globalSpecialLinkListener;

	/**
	 * Erstellt ein Panel, in dem eine bestimmte Hilfe-Seite angezeigt wird
	 * @param topic	Anzuzeigendes Thema (Dateiname ohne ".html"-Endung).
	 * @param listener	Listener, der aufgerufen wird, wenn ein spezieller Link (beginnend mit "special:") angeklickt wird
	 * @param modalHelp	Handelt es sich um ein modales Hilfefenster?
	 * @return	Panel, welches die HTML-Seite enthält
	 */
	public static JPanel infoPanel(final String topic, final Consumer<String> listener, final boolean modalHelp) {
		final Help help=new Help(null,null,true);
		help.specialLinkListener=listener;
		return help.getHTMLPanel(topic,modalHelp);
	}

	/**
	 * Dieses Interface wird von <code>infoPanel</code> als Parameter verwendet.
	 * Die Methode des Interfaces wird aufgerufen, wenn in dem HTML-Panel ein
	 * Link, dessen Adresse mit "special:" beginnt, angeklickt wird.
	 * @author Alexander Herzog
	 * @see #infoPanel(String, Consumer, boolean)
	 */
	public interface SpecialLinkListener {
		/**
		 * Wird aufgerufen, wenn ein Link zu einem besonderen, internen Ziel angeklickt wurde
		 * @param href	Linkziel des angeklickten Links
		 */
		void specialLinkClicked(final String href);
	}

	@Override
	protected void processSpecialLink(String href, final boolean modalHelp) {
		final String key="special:";
		if (!href.substring(0,Math.min(href.length(),key.length())).equalsIgnoreCase(key)) return;
		href=href.substring(key.length());

		if (specialLinkListener!=null) {
			specialLinkListener.accept(href);
		} else {
			if (globalSpecialLinkListener!=null) globalSpecialLinkListener.accept(href);
		}
	}
}
