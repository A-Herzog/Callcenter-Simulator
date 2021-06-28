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

import java.awt.BorderLayout;
import java.awt.Desktop;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;
import java.io.IOException;
import java.io.Serializable;
import java.net.MalformedURLException;
import java.net.URISyntaxException;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;

import javax.swing.AbstractAction;
import javax.swing.Icon;
import javax.swing.InputMap;
import javax.swing.JButton;
import javax.swing.JComponent;
import javax.swing.JMenuItem;
import javax.swing.JPanel;
import javax.swing.JPopupMenu;
import javax.swing.JToolBar;
import javax.swing.KeyStroke;
import javax.swing.SwingUtilities;

import language.Language;
import systemtools.MsgBox;
import systemtools.help.HTMLBrowserPanel;
import systemtools.help.HTMLBrowserTextPane;
import ui.images.Images;

/**
 * Zeigt eine Internetseite in einem <code>JPanel</code> an.
 * @author Alexander Herzog
 * @version 1.0
 */
public final class HTMLPanel extends JPanel {
	/**
	 * Serialisierungs-ID der Klasse
	 * @see Serializable
	 */
	private static final long serialVersionUID = -3360161989499967773L;

	/** Wird aufgerufen, wenn der Nutzer auf die Schließen-Schaltfläche klickt. */
	private final Runnable closeNotify;

	/** Toolbar des Panels */
	private final JToolBar toolBar;

	/** "Schließen"-Schaltfläche */
	private final JButton buttonClose;

	/** "Start"-Schaltfläche */
	private final JButton buttonHome;

	/** "Zurück"-Schaltfläche */
	private final JButton buttonBack;

	/** "Weiter"-Schaltfläche */
	private final JButton buttonNext;

	/** "Inhalt anzeigen"-Schaltfläche */
	private final JButton buttonContent;

	/** Panel zur Anzeige des Hilfetextes */
	private final HTMLBrowserPanel textPane;

	/** Popup zur Anzeige der Inhaltselemente /wird über die "Inhalt anzeigen"-Schaltfläche aktiviert */
	private final JPopupMenu contentPopup;

	/** Gibt an, ob die Toolbar-Schaltfläche, die eine Popup-Menü mit einer Übersicht der Zwischenüberschriften der Seite enthält, angezeigt werden soll. */
	private final boolean showContent;

	/** Liste mit den "Zurück"-URLs */
	private final List<URL> listBack;

	/** Liste mit den "Weiter"-URLs */
	private final List<URL> listNext;

	/** Aktuell angezeigte URL */
	private URL currentURL=null;

	/** Startseiten-URL */
	private URL homeURL=null;

	/** Callback welches aufgerufen wird, wenn der Nutzer auf einen Link klickt, der keine URL enthält. */
	private Runnable processSpecialLink;

	/** Linkziel für einen angeklickten Link, der keine URL enthält. */
	private String specialLink="";

	/**
	 * Konstruktor der Klasse <code>HTMLPanel</code>
	 * @param showBackAndNext Zeigt die Vorwärts- und Rückwärtsschaltflächen an
	 * @param showContent	Zeigt eine Toolbar-Schaltfläche an, die eine Popup-Menü mit einer Übersicht der Zwischenüberschriften der Seite enthält
	 * @param closeNotify	Wird aufgerufen, wenn der Nutzer auf die Schließen-Schaltfläche klickt.
	 */
	public HTMLPanel(boolean showBackAndNext, boolean showContent, Runnable closeNotify) {
		setLayout(new BorderLayout());
		this.showContent=showContent;
		this.closeNotify=closeNotify;

		toolBar=new JToolBar();
		toolBar.setFloatable(false);
		buttonClose=addButton(Language.tr("Dialog.Button.Close"),Language.tr("Help.Close.Info"),Images.GENERAL_EXIT.getIcon());
		buttonClose.setVisible(showBackAndNext && closeNotify!=null);
		buttonHome=addButton(Language.tr("Help.StartPage"),Language.tr("Help.StartPage.Info"),Images.HELP_NAVIGATE_START.getIcon());
		buttonHome.setVisible(showBackAndNext);
		buttonBack=addButton(Language.tr("Dialog.Button.Back"),Language.tr("Help.Back.Info"),Images.HELP_NAVIGATE_PREVIOUS.getIcon());
		buttonBack.setVisible(showBackAndNext);
		buttonBack.setEnabled(false);
		buttonNext=addButton(Language.tr("Dialog.Button.Forward"),Language.tr("Help.Forward.Info"),Images.HELP_NAVIGATE_NEXT.getIcon());
		buttonNext.setVisible(showBackAndNext);
		buttonNext.setEnabled(false);
		buttonContent=addButton(Language.tr("Help.Content"),Language.tr("Help.Content.Info"),Images.HELP_NAVIGATE_FIND.getIcon());
		buttonHome.setVisible(showBackAndNext);
		buttonContent.setVisible(false);
		if (showBackAndNext) add(toolBar,BorderLayout.NORTH);

		textPane=new HTMLBrowserTextPane();
		textPane.init(new LinkClickListener(),new PageLoadListener());

		add(textPane.asScrollableJComponent(),BorderLayout.CENTER);

		contentPopup=new JPopupMenu();

		listBack=new ArrayList<>();
		listNext=new ArrayList<>();

		InputMap inputMap=getInputMap(JComponent.WHEN_IN_FOCUSED_WINDOW);
		KeyStroke stroke=KeyStroke.getKeyStroke("ESCAPE");
		inputMap.put(stroke,"ESCAPE");
		getActionMap().put("ESCAPE",new EscapeListener());
	}

	/**
	 * Liefert das eigentliche Browser-Element als <code>JComponent</code>-Objekt zurück
	 * @return	Eigentliches Browser-Element
	 */
	public JComponent getBrowserJComponent() {
		return textPane.asInnerJComponent();
	}

	/**
	 * Konstruktor der Klasse <code>HTMLPanel</code>
	 * (Vorwärts-, Zurück- und Inhalt-Schaltflächen werden angezeigt.)
	 * @param closeNotify	Wird aufgerufen, wenn der Nutzer auf die Schließen-Schaltfläche klickt.
	 */
	public HTMLPanel(Runnable closeNotify) {
		this(true,true,closeNotify);
	}

	/**
	 * Fügt eine neue Schaltfläche zur Symbolleiste {@link #toolBar} hinzu.
	 * @param title	Titel der Schaltfläche
	 * @param tip	Tooltip für die Schaltfläche (darf <code>null</code> sein)
	 * @param icon	Icon für die Schaltfläche (darf <code>null</code> sein)
	 * @return	Liefert die bereits eingefügte Schaltfläche.
	 * @see #toolBar
	 */
	private JButton addButton(String title, String tip, Icon icon) {
		JButton button=new JButton(title);
		if (tip!=null && !tip.equals("")) button.setToolTipText(tip);
		if (icon!=null) button.setIcon(icon);
		toolBar.add(button);
		button.addActionListener(new ButtonListener());
		return button;
	}

	/**
	 * Registrirt ein <code>Runnable</code>-Objekt, welches aufgerufen wird,
	 * wenn der Nutzer auf einen Link klickt, der keine URL enthält.
	 * @param processSpecialLink <code>Runnable</code>-Objekt, welche über das Klicken auf den besonderen Link informiert wird.
	 * @see #getSpecialLink()
	 */
	public void setProcessSpecialLink(Runnable processSpecialLink) {
		this.processSpecialLink=processSpecialLink;
	}

	/**
	 * Klickt der Nutzer auf einen Link, der keine URL enthält, so wird hier das angegebene Link-Ziel zurückgegeben.
	 * @return	Link-Ziel bei besonderen Links
	 * @see #setProcessSpecialLink(Runnable)
	 */
	public String getSpecialLink() {
		return specialLink;
	}

	/**
	 * Stellt die Seite ein, die über die "Startseite"-Schaltfläche erreichbar sein soll.
	 * @param file	Startseiten-Datei
	 * @return	Gibt <code>true</code> zurück, wenn die Datei erfolgreich geladen werden konnte.
	 */
	public boolean setHome(File file) {
		try {setHome(file.toURI().toURL());} catch (MalformedURLException e) {return false;}
		return true;
	}

	/**
	 * Stellt die Seite ein, die über die "Startseite"-Schaltfläche erreichbar sein soll.
	 * @param res	Ressourcen-String zu der Datei, die als Startseite verwendet werden soll.
	 */
	public void setHome(String res) {
		homeURL=HTMLPanel.class.getResource(res);
	}

	/**
	 * Stellt die Seite ein, die über die "Startseite"-Schaltfläche erreichbar sein soll.
	 * @param url	URL zu der Seite, die als Startseite verwendet werden soll.
	 */
	public void setHome(URL url) {
		homeURL=url;
	}

	/**
	 * Ruft die Startseite auf (sofern zuvor per <code>setHome</code> eine gesetzt wurde).
	 * @return Gibt <code>true</code> zurück, wenn die Seite erfolgreich geladen und angezeigt werden konnte.
	 * @see #setHome(File)
	 * @see #setHome(String)
	 * @see #setHome(URL)
	 */
	public boolean goHome() {
		if (homeURL==null) return false;
		return loadPage(homeURL);
	}

	/**
	 * Zeigt die als Parameter übergebene Seite an.
	 * @param file	Anzuzeiende Datei
	 * @return Gibt <code>true</code> zurück, wenn die Seite erfolgreich geladen und angezeigt werden konnte.
	 */
	public boolean loadPage(File file) {
		try {return loadPage(file.toURI().toURL());} catch (MalformedURLException e) {return false;}
	}

	/**
	 * Zeigt die als Parameter übergebene Seite an.
	 * @param res Ressourcen-String zu der anzuzeigenden Datei
	 * @return Gibt <code>true</code> zurück, wenn die Seite erfolgreich geladen und angezeigt werden konnte.
	 */
	public boolean loadPage(String res) {
		return loadPage(HTMLPanel.class.getResource(res));
	}

	/**
	 * Zeigt die als Parameter übergebene Seite an.
	 * @param url URL zu der anzuzeigenden Datei
	 * @return Gibt <code>true</code> zurück, wenn die Seite erfolgreich geladen und angezeigt werden konnte.
	 */
	public boolean loadPage(URL url) {
		if (currentURL!=null && url!=null && currentURL.sameFile(url)) {
			boolean ok=true;
			if (currentURL.getRef()==null && url.getRef()!=null) ok=false;
			if (currentURL.getRef()!=null && url.getRef()==null) ok=false;
			if (currentURL.getRef()!=null && url.getRef()!=null && !currentURL.getRef().equals(url.getRef())) ok=false;
			if (ok) return true;
		}

		if (!textPane.showPage(url)) return false;
		if (currentURL!=null) {listBack.add(currentURL); listNext.clear();}
		currentURL=url;
		return true;
	}

	/**
	 * Prüft, ob die angegebene Seite existiert.
	 * @param res	Ressourcen-String zu der zu prüfenden Datei
	 * @return	Gibt <code>true</code> zurück, wenn die Datei existiert.
	 */
	public boolean pageExists(String res) {
		return HTMLPanel.class.getResource(res)!=null;
	}

	/**
	 * Wählt die angegebene Seite in der gewählten Sprache (mit Fallback, falls die Datei in der gewählten Sprache nicht vorliegt.)
	 * @param res	URL zu der anzuzeigenden Datei
	 * @return Vollständiger Pfad in der gewählten Sprache
	 */
	public String languagePage(String res) {
		String[] supportedLanguages={"EN","DE"};

		for (int i=0;i<supportedLanguages.length;i++) if (Language.tr("Help.Language").equalsIgnoreCase(supportedLanguages[i])) {
			String url=String.format(HelpConsts.LANGUAGE_FOLDER,supportedLanguages[i].toUpperCase())+"/"+res;
			if (pageExists(url)) return url;
			return HelpConsts.FALLBACK_FOLDER+"/"+res;
		}

		return HelpConsts.FALLBACK_FOLDER+"/"+res;
	}

	/**
	 * Initialisiert die Einträge zur Auswahl bestimmter Elemente im {@link #contentPopup}.
	 * @see #contentPopup
	 * @see ButtonListener
	 */
	private void initContentPopup() {
		contentPopup.removeAll();
		List<String> content=textPane.getPageContent();
		List<Integer> level=textPane.getPageContentLevel();

		for (int i=0;i<content.size();i++) {
			String s="";
			if (level.get(i)>=4) s="  ";
			if (level.get(i)>=5) s+="   ";
			JMenuItem item=new JMenuItem(s+content.get(i));
			item.addActionListener(new ButtonListener());
			if (level.get(i)==1) item.setIcon(Images.HELP_NAVIGATE_POPUP_LEVEL1.getIcon());
			if (level.get(i)==2) item.setIcon(Images.HELP_NAVIGATE_POPUP_LEVEL2.getIcon());
			contentPopup.add(item);
		}
	}

	/**
	 * Reagiert auf Klicks auf die verschiedenen Schaltflächen
	 * @see HTMLPanel#buttonClose
	 * @see HTMLPanel#buttonBack
	 * @see HTMLPanel#buttonNext
	 * @see HTMLPanel#buttonHome
	 * @see HTMLPanel#buttonContent
	 */
	private final class ButtonListener implements ActionListener {
		@Override
		public void actionPerformed(ActionEvent e) {
			if (e.getSource()==buttonClose) {
				if (closeNotify!=null) closeNotify.run();
				return;
			}

			if (e.getSource()==buttonBack) {
				if (currentURL!=null) listNext.add(currentURL);
				currentURL=listBack.get(listBack.size()-1);
				listBack.remove(listBack.size()-1);
				textPane.showPage(currentURL);
				return;
			}

			if (e.getSource()==buttonNext) {
				if (currentURL!=null) listBack.add(currentURL);
				currentURL=listNext.get(listNext.size()-1);
				listNext.remove(listNext.size()-1);
				textPane.showPage(currentURL);
				return;
			}

			if (e.getSource()==buttonHome) {
				loadPage(homeURL);
				return;
			}

			if (e.getSource()==buttonContent && textPane.getPageContent().size()>0) {
				initContentPopup();
				contentPopup.show(buttonContent,0,buttonContent.getBounds().height);
				return;
			}

			if (e.getSource() instanceof JMenuItem) {
				int i=contentPopup.getComponentIndex((JMenuItem)e.getSource());
				if (i>=0) textPane.scrollToPageContent(i);
				return;
			}
		}
	}

	/**
	 * Lade-Lock
	 * @see HTMLPanel.PageLoadListener
	 */
	private final Object lockObject=new Object();

	/**
	 * Reagiert darauf, wenn das Laden einer Seite in {@link HTMLPanel#textPane}
	 * abgeschlossen ist (und stellt die Vor/Zurück-Schaltflächen usw. korrekt ein).
	 * @see HTMLPanel#textPane
	 */
	private final class PageLoadListener implements Runnable {
		@Override
		public void run() {
			buttonHome.setVisible(homeURL!=null);
			buttonHome.setEnabled(homeURL!=null	&& (currentURL==null || !homeURL.sameFile(currentURL)));
			buttonBack.setEnabled(listBack.size()>0);
			buttonNext.setEnabled(listNext.size()>0);
			buttonContent.setVisible(showContent && textPane.getPageContent().size()>0);

			synchronized(lockObject) {
				lockObject.notify();
			}
		}
	}

	/**
	 * Reagiert auf das Anklicken von Links innerhalb der HTML-Anzeige.
	 * @see HTMLPanel#textPane
	 * @see HTMLPanel#processSpecialLink
	 */
	private final class LinkClickListener implements Runnable {
		@Override
		public void run() {
			URL url=textPane.getLastClickedURL();

			if (url==null) {
				specialLink=textPane.getLastClickedURLDescription();
				if (processSpecialLink!=null) SwingUtilities.invokeLater(processSpecialLink);
			} else {
				String s=url.toString();
				if (s.toLowerCase().startsWith("mailto:")) {
					try {Desktop.getDesktop().mail(url.toURI());} catch (IOException | URISyntaxException e1) {
						MsgBox.error(HTMLPanel.this,Language.tr("Window.Info.NoEMailProgram.Title"),String.format(Language.tr("Window.Info.NoEMailProgram.Info"),url.toString()));
					}
				} else {
					if (s.toLowerCase().startsWith("http://") || s.toLowerCase().startsWith("https://")) {
						if (!MsgBox.confirmOpenURL(HTMLPanel.this,url)) return;
						try {Desktop.getDesktop().browse(url.toURI());} catch (IOException | URISyntaxException e1) {
							MsgBox.error(HTMLPanel.this,Language.tr("Window.Info.NoInternetConnection"),String.format(Language.tr("Window.Info.NoInternetConnection.Address"),url.toString()));
						}
					} else {
						s=url.toString();
						s=s.substring(s.lastIndexOf('/')+1);
						loadPage(languagePage(s));
					}
				}
			}
		}
	}

	/**
	 * Listener, der auf Escape-Tastendrücke reagiert
	 */
	private final class EscapeListener extends AbstractAction {
		/**
		 * Serialisierungs-ID der Klasse
		 * @see Serializable
		 */
		private static final long serialVersionUID = 3060385322767789283L;

		@Override
		public void actionPerformed(ActionEvent actionEvent) {
			specialLink="special:escape";
			if (processSpecialLink!=null) SwingUtilities.invokeLater(processSpecialLink);
		}
	}
}