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
package ui.images;

import java.awt.Image;
import java.io.IOException;
import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import javax.imageio.ImageIO;
import javax.swing.Icon;
import javax.swing.ImageIcon;

/**
 * Diese Enumerations-Klasse hält die Icons für Toolbars und Menüs vor.
 * @author Alexander Herzog
 */
public enum Images {
	/* Allgemeine Icons */

	/** Symbol "Drucken" */
	GENERAL_PRINT("printer.png"),

	/** Symbol "Einstellungen" (Programmsetup) */
	GENERAL_SETUP("wrench.png"),

	/** Symbol (groß) "Einstellungen" (Programmsetup) */
	GENERAL_SETUP_BIG("wrench_big.png"),

	/** Symbol "Ende" */
	GENERAL_EXIT("door_in.png"),

	/** Symbol "Information" */
	GENERAL_INFO("information.png"),

	/** Symbol (groß) "Information" */
	GENERAL_SYMBOL_BIG("Symbol32.png"),

	/** Symbol "Abbruch" */
	GENERAL_CANCEL("cancel.png"),

	/** Symbol "Tools" */
	GENERAL_TOOLS("cog.png"),

	/** Symbol "Datei auswählen" */
	GENERAL_SELECT_FILE("folder_page_white.png"),

	/** Symbol "Skriptdatei auswählen" */
	GENERAL_SELECT_FILE_SCRIPT("lightning.png"),

	/** Symbol "Tabellendatei auswählen" */
	GENERAL_SELECT_FILE_TABLE("folder_table.png"),

	/** Symbol "Verzeichnis auswählen" */
	GENERAL_SELECT_FOLDER("folder.png"),

	/** Symbol "XML-Element auswählen" */
	GENERAL_SELECT_XML("add.png"),

	/** Symbol "Speichern" */
	GENERAL_SAVE("disk.png"),

	/** Symbol "Versionsgeschichte" */
	GENERAL_CHANGELOG("calendar.png"),

	/** Symbol "Lizenzen" */
	GENERAL_LICENSE("key.png"),

	/** Symbol "Aus" */
	GENERAL_OFF("cross.png"),

	/** Symbol "Dialog-Button 'Ok'" */
	MSGBOX_OK("accept.png"),

	/** Symbol "Dialog-Button 'Ja'" */
	MSGBOX_YES("tick.png"),

	/** Symbol "Dialog-Button 'Ja, speichern'" */
	MSGBOX_YES_SAVE("disk.png"),

	/** Symbol "Dialog-Button 'Nein'" */
	MSGBOX_NO("cancel.png"),

	/** Symbol "Dialog-Button 'Abbruch/Zurück'" */
	MSGBOX_CANCEL("arrow_redo2.png"),

	/** Symbol "Nach unten" */
	ARROW_DOWN("arrow_down.png"),

	/** Symbol "Nach oben" */
	ARROW_UP("arrow_up.png"),

	/** Symbol "Nach links" */
	ARROW_LEFT("arrow_left.png"),

	/** Symbol "Nach rechts" */
	ARROW_RIGHT("arrow_right.png"),

	/* Bearbeiten */

	/** Symbol "Bearbeiten - Kopieren" */
	EDIT_COPY("page_copy.png"),

	/** Symbol "Bearbeiten - Einfügen" */
	EDIT_PASTE("paste_plain.png"),

	/** Symbol "Bearbeiten - Hinzufügen" */
	EDIT_ADD("add.png"),

	/** Symbol "Bearbeiten - Löschen" */
	EDIT_DELETE("delete.png"),

	/** Symbol "Bearbeiten - Zeitbereich auswählen" */
	EDIT_RANGE("Table.png"),

	/** Symbol "Zoom" (allgemein) */
	ZOOM("zoom.png"),

	/* Modell */

	/** Symbol "Modell" */
	MODEL("brick.png"),

	/** Symbol "Leeres Modell" */
	MODEL_EMPTY("page_new.gif"),

	/** Symbol (groß) "Modell" */
	MODEL_BIG("brick_big.png"),

	/** Symbol "Modell - Neu" */
	MODEL_NEW("brick_add.png"),

	/** Symbol "Modell - Neu mit Assistent" */
	MODEL_WIZARD("emoticon_smile.png"),

	/** Symbol "Modell - Laden" */
	MODEL_LOAD("brick_go.png"),

	/** Symbol (groß) "Modell - Laden" */
	MODEL_LOAD_BIG("brick_go_big.png"),

	/** Symbol "Modell - Speichern" */
	MODEL_SAVE("disk.png"),

	/** Symbol (groß) "Modell - Speichern" */
	MODEL_SAVE_PLUS("disk_plus.png"),

	/** Symbol "Vergleichen - mehrere Statistikdaten" */
	MODEL_COMPARE("application_tile_horizontal.png"),

	/** Symbol "Vergleichen - Modell festhalten" */
	MODEL_COMPARE_KEEP("basket_put.png"),

	/** Symbol "Vergleichen - festgehaltenes und aktuelles Modell vergleichen" */
	MODEL_COMPARE_COMPARE("basket_go.png"),

	/** Symbol "Modellvereinfachen" */
	MODEL_SIMPLIFY("brick.png"),

	/* Daten */

	/** Symbol "Daten gemäß Schablone laden" */
	DATA_LOAD_BY_TEMPLATE("Table.png"),

	/** Symbol "Kundenankünfte laden" */
	DATA_LOAD_CALLER("user.png"),

	/** Symbol "Agentenanzahlen laden" */
	DATA_LOAD_AGENTS("group.png"),

	/** Symbol "Agentenproduktivität laden" */
	DATA_LOAD_AGENTS_EFFICIENCY("time_delete.png"),

	/** Symbol "Krankheitsbedingten Aufschlag laden" */
	DATA_LOAD_AGENTS_ADDITION("time_add.png"),

	/** Symbol "Bedien- und Nachbearbeitungszeiten laden" */
	DATA_LOAD_HOLDING_TIME("rosette.png"),

	/* Modell-Editor */

	/** Symbol "Editor - Tools" */
	EDITOR_GENERAL("wrench.png"),

	/** Symbol "Editor - Kosten" */
	EDITOR_COSTS("money_euro.png"),

	/** Symbol "Editor - Mindestwartezeiten" */
	EDITOR_MINIMUM_WAITING_TIMES("clock.png"),

	/** Editor - Inhalt dieser Dialogseite für andere Gruppen übernehmen */
	EDITOR_APPLY_SINGLE("application.png"),

	/** Editor - Inhalt aller Dialogseiten für andere Gruppen übernehmen */
	EDITOR_APPLY_ALL("application_double.png"),

	/** Symbol "Editor - Schwellenwert" */
	EDITOR_THRESHOLD("Ampel_klein.png"),

	/** Symbol (groß) "Editor - Schwellenwert" */
	EDITOR_THRESHOLD_BIG("Ampel_gross.png"),

	/** Symbol "Editor - Schwellenwert - Zeit" */
	EDITOR_THRESHOLD_TIME("clock.png"),

	/** Symbol "Editor - Schichtplan" */
	EDITOR_SHIFT_PLAN("clock.png"),

	/** Symbol "Editor - Schichtplan - Verteilung" */
	EDITOR_SHIFT_PLAN_DISTRIBUTION("chart_curve.png"),

	/** Symbol "Editor - Schichtplan - Tabelle" */
	EDITOR_SHIFT_PLAN_TABLE("Table.png"),

	/** Symbol "Editor - Schichtplan - Ergebnisdiagramm" */
	EDITOR_SHIFT_PLAN_RESULT_DIAGRAM("clock.png"),

	/** Symbol "Editor - Schichtplan - Ergebnistabelle" */
	EDITOR_SHIFT_PLAN_RESULT_TABLE("Table.png"),

	/** Symbol "Editor - Anrufergruppen" */
	EDITOR_CALLER("user.png"),

	/** Symbol (groß) "Editor - Anrufergruppen" */
	EDITOR_CALLER_BIG("big_person.png"),

	/** Symbol (groß) "Editor - Anrufergruppen - Deaktiviert" */
	EDITOR_CALLER_BIG_DISABLED("big_person_disabled.png"),

	/** Symbol "Editor - Anrufergruppen - rot" */
	EDITOR_CALLER_RED("user_red.png"),

	/** Symbol "Editor - Anrufergruppen - Hinzufügen" */
	EDITOR_CALLER_ADD("user_add.png"),

	/** Symbol "Editor - Anrufergruppen - Löschen" */
	EDITOR_CALLER_DELETE("user_delete.png"),

	/** Symbol "Editor - Anrufergruppen - Bearbeiten" */
	EDITOR_CALLER_EDIT("user_edit.png"),

	/** Symbol "Editor - Anrufergruppen - Kopieren" */
	EDITOR_CALLER_COPY("user_go.png"),

	/** Symbol "Editor - Anrufergruppen - Seite 'Erstanrufer'" */
	EDITOR_CALLER_PAGE_FRESH("wrench.png"),

	/** Symbol "Editor - Anrufergruppen - Seite 'Wartezeittoleranz'" */
	EDITOR_CALLER_PAGE_WAITING_TIME_TOLERANCE("clock.png"),

	/** Symbol "Editor - Anrufergruppen - Seite 'Wiederholer'" */
	EDITOR_CALLER_PAGE_RETRY("arrow_redo.png"),

	/** Symbol "Editor - Anrufergruppen - Seite 'Weiterleitungen'" */
	EDITOR_CALLER_PAGE_FORWARDING("arrow_right.png"),

	/** Symbol "Editor - Anrufergruppen - Seite 'Wiederanrufer'" */
	EDITOR_CALLER_PAGE_RECALLER("arrow_divide.png"),

	/** Symbol "Editor - Anrufergruppen - Seite 'Service-Level'" */
	EDITOR_CALLER_PAGE_SERVICE_LEVEL("clock.png"),

	/** Symbol "Editor - Anrufergruppen - Seite 'Score'" */
	EDITOR_CALLER_PAGE_SCORE("calculator.png"),

	/** Symbol "Editor - Anrufergruppen - Anruferverteilung zu Verteilung normieren" */
	EDITOR_CALLER_DENSITY_NORMALIZE("sum.png"),

	/** Symbol "Editor - Anrufergruppen - Anruferverteilung auf ganzzahlige Werte einstellen" */
	EDITOR_CALLER_DENSITY_INTEGER("chart_curve.png"),

	/** Symbol "Editor - Agentengruppe" */
	EDITOR_AGENTS("group.png"),

	/** Symbol "Editor - Agentengruppe - Hinzufügen" */
	EDITOR_AGENTS_ADD("group_add.png"),

	/** Symbol "Editor - Agentengruppe - Löschen" */
	EDITOR_AGENTS_DELETE("group_delete.png"),

	/** Symbol "Editor - Agentengruppe - Bearbeiten" */
	EDITOR_AGENTS_EDIT("group_edit.png"),

	/** Symbol "Editor - Agentengruppe - Kopieren" */
	EDITOR_AGENTS_COPY("group_go.png"),

	/** Symbol "Editor - Agentengruppe - Produktivität" */
	EDITOR_AGENTS_EFFICIENCY("time_delete.png"),

	/** Symbol "Editor - Agentengruppe - Krankheitsbedingter Zuschlag" */
	EDITOR_AGENTS_ADDITION("time_add.png"),

	/** Symbol (groß) "Editor - Agentengruppe" */
	EDITOR_AGENTS_BIG("big_group.png"),

	/** Symbol (groß) "Editor - Agentengruppe - deaktiviert" */
	EDITOR_AGENTS_BIG_DISABLED("big_group_disabled.png"),

	/** Symbol "Editor - Agentengruppe - Arbeitszeiten: fest" */
	EDITOR_AGENTS_MODE_FIXED("calendar.png"),

	/** Symbol "Editor - Agentengruppe - Arbeitszeiten: Verteilung über den Tag" */
	EDITOR_AGENTS_MODE_DISTRIBUTION("chart_bar.png"),

	/** Symbol "Editor - Agentengruppe - Arbeitszeiten: gemäß Kundenankünften" */
	EDITOR_AGENTS_MODE_BY_CLIENT("user.png"),

	/** Symbol "Editor - Agentengruppe - Arbeitszeiten: gemäß Auslastung berechnen" */
	EDITOR_AGENTS_CALCULATE("calculator.png"),

	/** Symbol "Editor - Callcenter" */
	EDITOR_CALLCENTER("house.png"),

	/** Symbol (groß) "Editor - Callcenter" */
	EDITOR_CALLCENTER_BIG("big_home.png"),

	/** Symbol (groß) "Editor - Callcenter - Deaktiviert" */
	EDITOR_CALLCENTER_BIG_DISABLED("big_home_disabled.png"),

	/** Symbol "Editor - Skill-Level" */
	EDITOR_SKILLLEVEL("rosette.png"),

	/** Symbol (groß) "Editor - Skill-Level" */
	EDITOR_SKILLLEVEL_BIG("big_crest.png"),

	/** Symbol "Editor - Skill-Level - Seite 'Service'" */
	EDITOR_SKILLLEVEL_PAGE_SERVICE("clock.png"),

	/** Symbol "Editor - Skill-Level - Seite 'Wartezeitabhängige Bedienzeitverlängerung'" */
	EDITOR_SKILLLEVEL_PAGE_WAITING("Symbol.png"),

	/** Symbol "Editor - Skill-Level - Seite 'Nachbearbeitungszeiten'" */
	EDITOR_SKILLLEVEL_PAGE_POST_PROCESSING("clock.png"),

	/** Symbol "Editor - Skill-Level - Seite 'Score'" */
	EDITOR_SKILLLEVEL_PAGE_SCORE("wrench.png"),

	/** Symbol "Editor - Modellinformationen" */
	EDITOR_MODELINFO("information.png"),

	/* Statistik */

	/** Symbol "Statistik" */
	STATISTICS("sum.png"),

	/** Symbol "Statistik" (dunkler) */
	STATISTICS_DARK("sum2.png"),

	/** Symbol "Statistik - laden" */
	STATISTICS_LOAD("icon_package_open.gif"),

	/** Symbol "Statistik - speichern" */
	STATISTICS_SAVE("icon_package_get.gif"),

	/** Symbol "Statistik - Modell in Editor laden */
	STATISTICS_SHOW_MODEL("brick.png"),

	/** Symbol "Statistik - Webviewer" */
	STATISTICS_SHOW_WEBVIEWER("world.png"),

	/** Symbol "Statistik - Zusammenfassung" */
	STATISTICS_REPORT("report.png"),

	/** Symbol "Statistik - Schnellzugriff" */
	STATISTICS_FILTER("lightning_go.png"),

	/** Symbol "Statistik - Schnellzugriff - Neu" */
	STATISTICS_FILTER_NEW("page_new.gif"),

	/** Symbol "Statistik - Schnellzugriff - Laden" */
	STATISTICS_FILTER_LOAD("page_up.gif"),

	/** Symbol "Statistik - Schnellzugriff - Speichern" */
	STATISTICS_SAVE_TABLE("Table.gif"),

	/* Simulation */

	/** Symbol "Simulation - Start" */
	SIMULATION("action_go.gif"),

	/** Symbol (groß) "Simulation - Start" */
	SIMULATION_BIG("action_go_big.png"),

	/** Symbol "Simulation - In Logdatei aufzeichnen" */
	SIMULATION_LOG("note_edit.png"),

	/** Symbol "Simulation - Modell prüfen" */
	SIMULATION_CHECK("accept.png"),

	/** Symbol "Simulation - Simulation mit direktem Speichern" */
	SIMULATION_AND_SAVE("disk.png"),

	/** Symbol "Simulation - Stapelverarbeitung" */
	SIMULATION_BATCH("server_go.png"),

	/** Symbol "Simulation - Stapelverarbeitung - Verzeichnis" */
	SIMULATION_BATCH_FOLDER("folder_page.png"),

	/** Symbol "Simulation - Stapelverarbeitung - Parameterreihe" */
	SIMULATION_BATCH_PARAMETERSERIES("calculator.png"),

	/** Symbol "Simulation - Kalibrierung" */
	SIMULATION_CALIBRATE("server_go.png"),

	/** Symbol "Simulation - Verbundene Simulation" */
	SIMULATION_CONNECTED("calendar.png"),

	/** Symbol "Simulation - Verbundene Simulation - Start" */
	SIMULATION_CONNECTED_RUN("server_go.png"),

	/** Symbol "Simulation - Verbundene Simulation - Ergebnisse anzeigen" */
	SIMULATION_CONNECTED_VIEWER("calendar.png"),

	/** Symbol "Simulation - Verbundene Simulation - Übertrag" */
	SIMULATION_CONNECTED_CARRY_OVER("application_form_edit.png"),

	/** Symbol "Simulation - Verbundene Simulation - Letztes Modell für nächsten Tag verwenden" */
	SIMULATION_CONNECTED_SELECT_MODEL_LAST("arrow_rotate_clockwise.png"),

	/** Symbol "Simulation - Verbundene Simulation - Modell aus Datei wählen" */
	SIMULATION_CONNECTED_SELECT_MODEL_FILE("brick_go.png"),

	/** Symbol "Simulation - Verbundene Simulation - Statistik nicht speichern" */
	SIMULATION_CONNECTED_SAVE_MODE_NO("cancel.png"),

	/** Symbol "Simulation - Verbundene Simulation - Datei für Statistikergebnisse wählen" */
	SIMULATION_CONNECTED_SAVE_MODE_SELECT_FILE("disk.png"),

	/** Symbol "Simulation - Mehrere Simulationen" */
	SIMULATION_MULTI("action_go_plus.png"),

	/* Optimierer */

	/** Symbol "Optimierer" */
	OPTIMIZER("chart_bar_add.png"),

	/** Symbol (groß) "Optimierer" */
	OPTIMIZER_BIG("chart_bar_add_big.png"),

	/** Symbol "Optimierer - Einstellungen laden" */
	OPTIMIZER_SETTINGS_LOAD("folder_page.png"),

	/** Symbol "Optimierer - Einstellungen speichern" */
	OPTIMIZER_SETTINGS_SAVE("disk.png"),

	/** Symbol "Optimierer - Pause" */
	OPTIMIZER_PAUSE("control_pause_blue.png"),

	/** Symbol "Optimierer - Ergebnisse" */
	OPTIMIZER_RESULTS("sum.png"),

	/** Symbol "Optimierer - Ergebnisse - Statistik" */
	OPTIMIZER_RESULTS_STATISTICS("chart_bar_add.png"),

	/** Symbol "Optimierer - Ergebnisse - Diagramme" */
	OPTIMIZER_RESULTS_DIAGRAMS("chart_bar.png"),

	/** Symbol "Optimierer - Ergebnisse - Letztes anzeigen" */
	OPTIMIZER_RESULTS_COMPARE_LAST("brick.png"),

	/** Symbol "Optimierer - Ergebnisse - Ergebnisse zu erstem und letztem Modell vergleichen" */
	OPTIMIZER_RESULTS_COMPARE_FIRST_LAST("application_tile_horizontal.png"),

	/** Symbol "Optimierer - Seite 'Optimierungsgröße'" */
	OPTIMIZER_PAGE_TARGET("chart_bar_add.png"),

	/** Symbol "Optimierer - Seite 'Stellgröße'" */
	OPTIMIZER_PAGE_CONTROL_VARIABLE("wrench.png"),

	/** Symbol "Optimierer - Seite 'Stellgröße' - Einschränkungen" */
	OPTIMIZER_PAGE_CONTROL_VARIABLE_RESTRICTION("group.png"),

	/** Symbol "Optimierer - Seite 'Intervalle'" */
	OPTIMIZER_PAGE_INTERVAL("time.png"),

	/** Symbol "Optimierer - Seite 'Intervalle' - Bereich auswählen" */
	OPTIMIZER_PAGE_INTERVAL_RANGE("Table.png"),

	/** Symbol "Optimierer - Seite 'Übertrag'" */
	OPTIMIZER_PAGE_CARRY_OVER("calendar.png"),

	/** Symbol "Ertragsoptimierung" */
	REVENUSE_OPTIMIZER("money_euro.png"),

	/** Symbol "Ertragsoptimierung - Start" */
	REVENUSE_OPTIMIZER_RUN("action_go.gif"),

	/* Extras */

	/** Symbol "Rechner" */
	EXTRAS_CALCULATOR("calculator.png"),

	/** Symbol "Warteschlangenrechner (Tab-Icons)" */
	EXTRAS_QUEUE_FUNCTION("fx.png"),

	/** Symbol "Verteilung anpassen" */
	EXTRAS_FIT_DISTRIBUTION("chart_curve.png"),

	/** Symbol "Kommandozeile" */
	EXTRAS_COMMANDLINE("application_xp_terminal.png"),

	/* Hilfe */

	/** Symbol "Hilfe" */
	HELP("help.png"),

	/** Symbol (groß) "Hilfe" */
	HELP_BIG("help_big.png"),

	/** Symbol "Hilfeinhalt" */
	HELP_CONTENT("book_open.png"),

	/** Symbol "Hilfe-pdf" */
	HELP_PDF("file_acrobat.gif"),

	/** Symbol "Lehrbuch" */
	HELP_BOOK("book.png"),

	/** Symbol "E-Mail" */
	HELP_EMAIL("icon_mail.gif"),

	/** Symbol "Homepage" */
	HELP_HOMEPAGE("world.png"),

	/** Symbol "Startseite" */
	HELP_NAVIGATE_START("house.png"),

	/** Symbol "Suchen" (in der Hilfe) */
	HELP_NAVIGATE_FIND("find.png"),

	/** Symbol "Zurück" (in der Hilfe) */
	HELP_NAVIGATE_PREVIOUS("resultset_previous.png"),

	/** Symbol "Weiter" (in der Hilfe) */
	HELP_NAVIGATE_NEXT("resultset_next.png"),

	/** Symbol "Ebene 1" (in der Hilfe) */
	HELP_NAVIGATE_POPUP_LEVEL1("flag_red.png"),

	/** Symbol "Ebene 2" (in der Hilfe) */
	HELP_NAVIGATE_POPUP_LEVEL2("resultset_next.png"),

	/* Sprache */

	/** Symbol "Sprache - Englisch" */
	LANGUAGE_EN("flag_gb.png"),

	/** Symbol "Sprache - Deutsch" */
	LANGUAGE_DE("flag_de.png"),

	/* Server */

	/** Symbol "Rechenserver" */
	SERVER_CALC("server.png"),

	/** Symbol "Rechenserver - Starten" */
	SERVER_CALC_RUN("server_go.png"),

	/* Verteilung anpassen */

	/** Symbol "Verteilung anpassen - Seite 'Werte'" */
	FIT_PAGE_VALUES("Table.png"),

	/** Symbol "Verteilung anpassen - Seite 'Empirische Verteilung'" */
	FIT_PAGE_EMPIRICAL_DISTRIBUTION("chart_bar.png"),

	/** Symbol "Verteilung anpassen - Seite 'Anpassung'" */
	FIT_PAGE_FIT("calculator.png"),

	/** Symbol "Verteilung anpassen - Seite 'Ergebnisse'" */
	FIT_PAGE_RESULT("chart_curve.png"),

	/* Varianzanalyse */

	/** Symbol "Varianzanalyse" */
	VARIANCE_ANALYSIS("chart_curve_error.png"),

	/** Symbol "Varianzanalyse - Seite 'mehrere Simulationen'" */
	VARIANCE_ANALYSIS_PAGE_SINGLE("action_go.gif"),

	/** Symbol "Varianzanalyse - Seite 'variable Anzahl an Simulationstagen'" */
	VARIANCE_ANALYSIS_PAGE_MULTI("time_add.png"),

	/** Symbol "Varianzanalyse - Seite 'variable Anzahl an Simulationstagen' - Tabelle" */
	VARIANCE_ANALYSIS_PAGE_MULTI_TABLE("Table.png"),

	/** Symbol "Varianzanalyse - Seite 'variable Anzahl an Simulationstagen' - Diagramm" */
	VARIANCE_ANALYSIS_PAGE_MULTI_CHART("chart_curve.png"),

	/** Symbol "Varianzanalyse" - Start */
	VARIANCE_ANALYSIS_RUN("server_go.png"),

	/* Anrufe verlagern */

	/** Symbol "Anrufer oder Agenten verlagern" */
	REARRANGE("chart_curve_go.png"),

	/** Symbol "Anrufer oder Agenten verlagern - Seite 'Anrufe verlagern'" */
	REARRANGE_PAGE_CALLER("user.png"),

	/** Symbol "Anrufer oder Agenten verlagern - Seite 'Agenten verlagern'" */
	REARRANGE_PAGE_AGENTS("group.png"),

	/** Symbol "Anrufer oder Agenten verlagern - Anpassen und laden" */
	REARRANGE_LOAD("chart_curve_go.png"),

	/** Symbol "Anrufer oder Agenten verlagern - Angepassten Modell simulieren und vergleichen" */
	REARRANGE_RUN("action_go.gif"),

	/* Einstellungen */

	/** Symbol "Einstellungen - Seite 'Benutzeroberfläche'" */
	SETUP_PAGE_APPLICATION("application_go.png"),

	/** Symbol "Einstellungen - Seite 'Simulation'" */
	SETUP_PAGE_SIMULATION("action_go.gif"),

	/** Symbol "Einstellungen - Seite 'Simulationssever'" */
	SETUP_PAGE_SERVER("server.png"),

	/** Symbol "Einstellungen - Seite 'Grafiken'" */
	SETUP_PAGE_IMPORT_EXPORT("image.gif"),

	/** Symbol "Einstellungen - Seite 'Statistik'" */
	SETUP_PAGE_STATISTICS("sum2.png"),

	/** Symbol "Einstellungen - Seite 'Lizenz'" */
	SETUP_PAGE_LICENSE("key.png"),

	/** Symbol "Einstellungen - Seite 'Update'" */
	SETUP_PAGE_UPDATE("tick.png"),

	/** Symbol "Einstellungen - Menü-Modus - Ribbons" */
	SETUP_MENU_RIBBONS("application_view_tile.png"),

	/** Symbol "Einstellungen - Menü-Modus - Klassische Menüs" */
	SETUP_MENU_CLASSIC("application.png"),

	/** Symbol "Einstellungen - Lizenz" */
	SETUP_LICENSE("key.png"),

	/** Symbol "Einstellungen - Lizenz - Anrufen" */
	SETUP_LICENSE_GET("action_refresh.gif"),

	/** Symbol "Einstellungen - Schriftgröße klein" */
	SETUP_FONT_SIZE1("FontSize_1.png"),

	/** Symbol "Einstellungen - Schriftgröße normal" */
	SETUP_FONT_SIZE2("FontSize_2.png"),

	/** Symbol "Einstellungen - Schriftgröße größer" */
	SETUP_FONT_SIZE3("FontSize_3.png"),

	/** Symbol "Einstellungen - Schriftgröße groß" */
	SETUP_FONT_SIZE4("FontSize_4.png"),

	/** Symbol "Einstellungen - Schriftgröße ganz groß" */
	SETUP_FONT_SIZE5("FontSize_5.png"),

	/** Symbol "Einstellungen - Fenstergröße - Vorgabe" */
	SETUP_WINDOW_SIZE_DEFAULT("application_double.png"),

	/** Symbol "Einstellungen - Fenstergröße - Vollbild" */
	SETUP_WINDOW_SIZE_FULL("application.png"),

	/** Symbol "Einstellungen - Fenstergröße - Letzte wiederherstellen" */
	SETUP_WINDOW_SIZE_LAST("application_edit.png"),

	/** Symbol "Einstellungen - Update - Suche" */
	SETUP_UPDATE_SEARCH("action_refresh.gif"),

	/** Symbol "Einstellungen - Update - Manuell" */
	SETUP_UPDATE_MANUAL("arrow_down.png"),

	/** Symbol "Einstellungen - Update - Webseite" */
	SETUP_UPDATE_WEB_PAGE("world.png");

	/**
	 * Dateiname des Icons
	 */
	private final String name;

	/**
	 * URLs des Icons
	 */
	private URL[] urls;

	/**
	 * Bild
	 */
	private Image image;

	/**
	 * Icon
	 */
	private Icon icon;

	/**
	 * Konstruktor des Enum
	 * @param name	Dateiname des Icons
	 */
	Images(final String name) {
		this.name=name;
	}

	/**
	 * Sucht ein Bild in einem Ordner und fügt es, wenn gefunden, zu einer Liste hinzu.
	 * @param list	Liste mit URLs zu der die neue URL hinzugefügt werden soll
	 * @param folder	Ordner in dem das Bild gesucht werden soll
	 * @param name	Name des Bildes
	 */
	private void addURL(final List<URL> list, final String folder, final String name) {
		URL url;

		url=getClass().getResource(folder+"/"+name);
		if (url!=null) {
			list.add(url);
		} else {
			url=getClass().getResource(folder+"/"+name.replace('_','-'));
			if (url!=null) list.add(url);
		}
	}

	/**
	 * Liefert die URL des Icons
	 * @return	URL des Icons
	 */
	public URL[] getURLs() {
		if (urls==null) {
			List<URL> list=new ArrayList<>();
			addURL(list,"res",name);
			addURL(list,"res24",name);
			addURL(list,"res32",name);
			addURL(list,"res48",name);
			urls=list.toArray(URL[]::new);
		}
		assert(urls!=null);
		return urls;
	}

	/**
	 * Wird das Programm unter Java 9 oder höher ausgeführt, so wird
	 * der Konstruktor der Multi-Resolution-Bild-Objektes geliefert, sonst <code>null</code>.
	 * @return	Multi-Resolution-Bild-Konstruktor oder <code>null</code>
	 */
	@SuppressWarnings("unchecked")
	private static Constructor<Object> getMultiImageConstructor() {
		try {
			final Class<?> cls=Class.forName("java.awt.image.BaseMultiResolutionImage");
			return (Constructor<Object>)cls.getDeclaredConstructor(int.class,Image[].class);
		} catch (ClassNotFoundException | NoSuchMethodException | SecurityException e) {
			return null;
		}
	}

	/**
	 * Liefert das Icon.
	 * @return	Icon
	 */
	public Icon getIcon() {
		if (icon==null) {
			final Image image=getImage();
			if (image!=null) icon=new ImageIcon(image);
		}
		assert(icon!=null);
		return icon;
	}

	/**
	 * Liefert basierend auf einer oder mehreren URLs das Standardbild (das Bild für die erste URL)
	 * @param urls	Liste mit URLs
	 * @return	Bild für die erste URL
	 */
	private Image getDefaultImage(final URL[] urls) {
		if (urls==null || urls.length==0) return null;
		try {
			return ImageIO.read(urls[0]);
		} catch (IOException e) {
			assert(false);
			return null;
		}
	}

	/**
	 * Liefert das Bild.
	 * @return	Bild
	 */
	public Image getImage() {
		if (image!=null) return image;

		final URL[] urls=getURLs();
		assert(urls.length>0);

		if (urls.length==1) return image=getDefaultImage(urls);

		final Constructor<Object> multiConstructor=getMultiImageConstructor();
		if (multiConstructor==null) return image=getDefaultImage(urls);

		final Image[] images=Arrays.asList(urls).stream().map(url->{
			try {
				return ImageIO.read(url);
			} catch (IOException e) {
				return image=getDefaultImage(urls);
			}
		}).toArray(Image[]::new);

		try {
			image=(Image)multiConstructor.newInstance(0,images);
			assert(image!=null);
			return image;
		} catch (InstantiationException|IllegalAccessException|IllegalArgumentException|InvocationTargetException e) {
			return image=getDefaultImage(urls);
		}
	}

	/**
	 * Prüft, ob alle Icons vorhanden sind.
	 */
	public static void checkAll() {
		for (Images image: values()) {
			System.out.print(image.name+": ");
			if (image.getIcon()==null) System.out.println("missing"); else System.out.println("ok");
		}
	}
}