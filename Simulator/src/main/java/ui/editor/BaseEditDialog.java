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
package ui.editor;

import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.Container;
import java.awt.Dialog;
import java.awt.FlowLayout;
import java.awt.Rectangle;
import java.awt.Window;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.io.Serializable;

import javax.swing.AbstractAction;
import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.Icon;
import javax.swing.InputMap;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JComboBox;
import javax.swing.JComponent;
import javax.swing.JDialog;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JRootPane;
import javax.swing.JTabbedPane;
import javax.swing.JTextField;
import javax.swing.KeyStroke;
import javax.swing.SwingUtilities;

import language.Language;
import mathtools.NumberTools;
import tools.SetupData;
import ui.images.Images;

/**
 * Diese Klasse stellt einen Dialog mit einem (optionalen) Namensfeld und (optional) mehreren,
 * benutzerdefinierbaren Tabs zur Verf�gung.
 * @author Alexander Herzog
 * @version 1.0
 */
public class BaseEditDialog extends JDialog {
	/**
	 * Serialisierungs-ID der Klasse
	 * @see Serializable
	 */
	private static final long serialVersionUID = -432438550461985704L;

	/**
	 * Konstante f�r "Abbruch"
	 * @see #getClosedBy()
	 */
	public static final int CLOSED_BY_CANCEL=0;

	/**
	 * Konstante f�r "OK"
	 * @see #getClosedBy()
	 */
	public static final int CLOSED_BY_OK=1;

	/**
	 * Konstante f�r "Vorheriges"
	 * @see #getClosedBy()
	 */
	public static final int CLOSED_BY_PREVIOUS=2;

	/**
	 * Konstante f�r "N�chstes"
	 * @see #getClosedBy()
	 */
	public static final int CLOSED_BY_NEXT=3;

	/** Schaltfl�che "Ok" */
	private JButton okButton=null;
	/** Schaltfl�che "Abbruch" */
	private JButton cancelButton=null;
	/** Schaltfl�che "Hilfe" */
	private JButton helpButton=null;
	/** Schaltfl�che "Zur�ck" */
	private JButton previousButton;
	/** Schaltfl�che "Weiter" */
	private JButton nextButton;
	/** Schaltfl�che "Schlie�en" */
	private JButton closeButton=null;

	/**
	 * Tabs des Dialogs (kann <code>null</code> sein, wenn keine Tabs eingerichtet wurden)
	 * @see #createTabsGUI(String, String, String, boolean, int, int, String, String)
	 */
	protected JTabbedPane tabs=null;

	/**
	 * �bergeordnetes Fenster
	 */
	protected final Window owner;

	/**
	 * Gibt an, wie der Dialog geschlossen wurde.
	 * @see #getClosedBy()
	 */
	private int closedBy=CLOSED_BY_CANCEL;

	/**
	 * Beschriftungen f�r die benutzerdefinierten Schaltfl�chen
	 * @see #addUserButtons(String[], Icon[], Runnable[])
	 * @see #addUserButtons(String[], String[], Icon[], Runnable[])
	 */
	private String[] userButtonCaptions=null;

	/**
	 * Tooltips f�r die benutzerdefinierten Schaltfl�chen
	 * @see #addUserButtons(String[], Icon[], Runnable[])
	 * @see #addUserButtons(String[], String[], Icon[], Runnable[])
	 */
	private String[] userButtonTooltips=null;

	/**
	 * Icons f�r die benutzerdefinierten Schaltfl�chen
	 * @see #addUserButtons(String[], Icon[], Runnable[])
	 * @see #addUserButtons(String[], String[], Icon[], Runnable[])
	 */
	private Icon[] userButtonIcons=null;

	/**
	 * Listener, der beim Anklicken einer benutzerdefinierten Schaltfl�che aktiviert wird.
	 * @see #userButtons
	 */
	private Runnable[] userButtonHandlers=null;

	/**
	 * Benutzerdefinierte Schaltfl�chen
	 * @see #addUserButtons(String[], Icon[], Runnable[])
	 * @see #addUserButtons(String[], String[], Icon[], Runnable[])
	 */
	private JButton[] userButtons;

	/**
	 * "Schlie�en" Schaltfl�che statt "Ok" und "Abbrechen" anzeigen
	 */
	protected boolean showCloseButton=false;

	/**
	 * Array mit den Namen der m�glichen Anrufertypen
	 */
	protected final String[] callerTypeNames;

	/**
	 * Textfeld, welches den Namen enth�lt
	 */
	protected JTextField name;

	/**
	 * Checkbox, die angibt, ob das Objekt aktiviert ist
	 */
	protected JCheckBox active;

	/**
	 * Gibt an, ob �nderungen zul�ssig sind (die "Ok"-Schaltfl�che aktiv ist) oder nicht
	 */
	protected final boolean readOnly;

	/**
	 * <code>Runnable</code>-Objekt, �ber das di Online-Hilfe aufgerufen werden kann.
	 */
	protected final Runnable helpCallback;

	/**
	 * Konstruktor der Klasse <code>BaseEditDialog</code>
	 * @param owner	�bergeordnetes Fenster
	 * @param title	Titel des Fensters
	 * @param callerTypeNames	Liste mit den Namen der m�glichen Anrufertypen
	 * @param readOnly	Wird dieser Parameter auf <code>true</code> gesetzt, so wird die "Ok"-Schaltfl�che deaktiviert
	 * @param helpCallback	Wird hier ein Wert ungleich <code>null</code> �bergeben, so wird eine "Hilfe"-Schaltfl�che angezeigt und die <code>Run</code>-Methode dieses Objekts beim Klicken auf diese Schaltfl�che aufgerufen
	 */
	protected BaseEditDialog(Component owner, String title, String[] callerTypeNames, boolean readOnly, Runnable helpCallback) {
		super(getOwnerWindow(owner),title,Dialog.ModalityType.DOCUMENT_MODAL);
		this.owner=getOwnerWindow(owner);
		this.callerTypeNames=callerTypeNames;
		this.readOnly=readOnly;
		this.helpCallback=helpCallback;
	}

	/**
	 * Konstruktor der Klasse <code>BaseEditDialog</code>
	 * @param owner	�bergeordnetes Fenster
	 * @param title	Titel des Fensters
	 * @param readOnly	Wird dieser Parameter auf <code>true</code> gesetzt, so wird die "Ok"-Schaltfl�che deaktiviert
	 * @param helpCallback	Hilfe-Callback
	 */
	protected BaseEditDialog(Component owner, String title, boolean readOnly, Runnable helpCallback) {
		this(owner,title,null,readOnly,helpCallback);
	}

	/**
	 * Liefert das �bergeordnete Fenster zu einer Komponente
	 * @param owner Komponente f�r die das �bergeordnete Fenster gesucht werden soll
	 * @return	�bergeordnetes Fenster oder <code>null</code>, wenn kein entsprechendes Fenster gefunden wurde
	 */
	private static Window getOwnerWindow(Component owner) {
		while (owner!=null && !(owner instanceof Window)) owner=owner.getParent();
		return (Window)owner;
	}

	/**
	 * Ersetzt die Methode <code>setSize</code>, ber�cksichtigt dabei die Bildschirmgr��e und reduziert n�tigenfalls die Fenstergr��e.
	 * @param window	Fenster, dessen Gr��e eingestellt werden soll.
	 * @param xSize	Gew�nschte Ausdehnung in x-Richtung des Fensters.
	 * @param ySize	Gew�nschte Ausdehnung in y-Richtung des Fensters.
	 */
	public static void setSizeRespectingScreensize(Window window, int xSize, int ySize) {
		SetupData setup=SetupData.getSetup();
		xSize=(int)Math.round(xSize*setup.scaleGUI);
		ySize=(int)Math.round(ySize*setup.scaleGUI);
		Rectangle area=window.getGraphicsConfiguration().getBounds();
		window.setSize(Math.min(area.width-50,xSize),Math.min(area.height-50,ySize));
	}

	@Override
	protected JRootPane createRootPane() {
		JRootPane rootPane=new JRootPane();
		InputMap inputMap=rootPane.getInputMap(JComponent.WHEN_IN_FOCUSED_WINDOW);

		KeyStroke stroke=KeyStroke.getKeyStroke("ESCAPE");
		inputMap.put(stroke,"ESCAPE");
		rootPane.getActionMap().put("ESCAPE",new SpecialKeyListener(0));

		stroke=KeyStroke.getKeyStroke("F1");
		inputMap.put(stroke,"F1");
		rootPane.getActionMap().put("F1",new SpecialKeyListener(1));

		return rootPane;
	}

	/**
	 * Klasse zur Reaktion auf F1- und Escape-Tastendr�cke
	 * @see BaseEditDialog#createRootPane()
	 */
	private class SpecialKeyListener extends AbstractAction {
		/**
		 * Serialisierungs-ID der Klasse
		 * @see Serializable
		 */
		private static final long serialVersionUID = -485008309903554823L;

		/**
		 * Aktion (0: Abbruch, 1: Hilfe)
		 */
		private final int action;

		/**
		 * Konstruktor der Klasse
		 * @param action	Aktion (0: Abbruch, 1: Hilfe)
		 */
		public SpecialKeyListener(int action) {
			this.action=action;
		}

		@Override
		public void actionPerformed(ActionEvent actionEvent) {
			switch (action) {
			case 0:	if ((cancelButton!=null && cancelButton.isVisible()) || (closeButton!=null && closeButton.isVisible())) {setVisible(false); dispose();} break;
			case 1: if (helpCallback!=null) SwingUtilities.invokeLater(helpCallback); break;
			}
		}
	}

	/**
	 * Erstellt den Fensterinhalt (mit Tabs).<br>
	 * (Bei der Erstellung des Fensters wird <code>createTabs</code> aufgerufen, d.h. wenn
	 * <code>createTabs</code> auf bestimmte Membervariablen zugreifen soll und diese im
	 * Konstruktor initialisiert werden, so muss der Aufruf von <code>createTabsGUI</code> nach
	 * der Belegung dieser Membervariablen erfolgen.)
	 * @param nameLabel	Beschriftung f�r das Namen-Eingabefeld (wird hier <code>null</code> �bergeben, so wird kein Name-Feld angelegt)
	 * @param nameValue	Initialer Wert f�r das Name-Eingabefeld
	 * @param activeLabel Beschriftung der Aktiv-Checkbox (wird hier <code>null</code> �bergeben, so wird keine Checkbox angelegt)
	 * @param activeValue Initialer Wert der Aktiv-Checkbox
	 * @param xSize	Breite des Fensters
	 * @param ySize	H�he des Fensters
	 * @param previous Beschriftung der Vorg�nger-Schaltl�che (wird ausgeblendet, wenn gleich null oder leer)
	 * @param next Beschriftung der Nachfolger-Schaltl�che (wird ausgeblendet, wenn gleich null oder leer)
	 * @see #createTabs(JTabbedPane)
	 */
	protected final void createTabsGUI(String nameLabel, String nameValue, String activeLabel, boolean activeValue, int xSize, int ySize, String previous, String next) {
		Container content=getContentPane();
		content.setLayout(new BorderLayout());

		addHeader(content,nameLabel,nameValue,activeLabel,activeValue);

		tabs=new JTabbedPane();
		content.add(tabs,BorderLayout.CENTER);
		tabs.setBorder(BorderFactory.createEmptyBorder(5,5,5,5));
		tabs.setTabLayoutPolicy(JTabbedPane.SCROLL_TAB_LAYOUT);
		createTabs(tabs);

		addFooter(content,xSize,ySize,previous,next);
	}

	/**
	 * Erstellt den Fensterinhalt (ohne Tabs).<br>
	 * (Bei der createSimpleContent des Fensters wird <code>createSimpleContent</code> aufgerufen, d.h. wenn
	 * <code>createSimpleContent</code> auf bestimmte Membervariablen zugreifen soll und diese im
	 * Konstruktor initialisiert werden, so muss der Aufruf von <code>createSimpleGUI</code> nach
	 * der Belegung dieser Membervariablen erfolgen.)
	 * @param nameLabel	Beschriftung f�r das Namen-Eingabefeld (wird hier <code>null</code> �bergeben, so wird kein Name-Feld angelegt)
	 * @param nameValue	Initialer Wert f�r das Name-Eingabefeld
	 * @param activeLabel Beschriftung der Aktiv-Checkbox (wird hier <code>null</code> �bergeben, so wird keine Checkbox angelegt)
	 * @param activeValue Initialer Wert der Aktiv-Checkbox
	 * @param xSize	Breite des Fensters
	 * @param ySize	H�he des Fensters
	 * @param previous Beschriftung der Vorg�nger-Schaltl�che (wird ausgeblendet, wenn gleich null oder leer)
	 * @param next Beschriftung der Nachfolger-Schaltl�che (wird ausgeblendet, wenn gleich null oder leer)
	 * @see #createSimpleContent(JPanel)
	 */
	protected final void createSimpleGUI(String nameLabel, String nameValue, String activeLabel, boolean activeValue, int xSize, int ySize, String previous, String next) {
		Container content=getContentPane();
		content.setLayout(new BorderLayout());

		addHeader(content,nameLabel,nameValue,activeLabel,activeValue);

		JPanel p=new JPanel(new FlowLayout(FlowLayout.LEFT));
		p.setBorder(BorderFactory.createEmptyBorder(5,5,5,5));
		content.add(p,BorderLayout.CENTER);
		createSimpleContent(p);

		addFooter(content,xSize,ySize,previous,next);
	}

	/**
	 * Erstellt den Fensterinhalt (ohne Tabs).<br>
	 * (Bei der Verwendung von createSimpleGUI wird <code>createSimpleContent</code> aufgerufen, d.h. wenn
	 * <code>createSimpleContent</code> auf bestimmte Membervariablen zugreifen soll und diese im
	 * Konstruktor initialisiert werden, so muss der Aufruf von <code>createSimpleGUI</code> nach
	 * der Belegung dieser Membervariablen erfolgen.)
	 * @param xSize	Breite des Fensters
	 * @param ySize	H�he des Fensters
	 * @param previous Beschriftung der Vorg�nger-Schaltl�che (wird ausgeblendet, wenn gleich null oder leer)
	 * @param next Beschriftung der Nachfolger-Schaltl�che (wird ausgeblendet, wenn gleich null oder leer)
	 * @see #createSimpleContent(JPanel)
	 */
	protected final void createSimpleGUI(int xSize, int ySize, String previous, String next) {
		Container content=getContentPane();
		content.setLayout(new BorderLayout());

		addHeader(content,null,null,null,false);

		JPanel p=new JPanel(new FlowLayout(FlowLayout.LEFT));
		p.setBorder(BorderFactory.createEmptyBorder(5,5,5,5));
		content.add(p,BorderLayout.CENTER);
		createSimpleContent(p);

		addFooter(content,xSize,ySize,previous,next);
	}

	/**
	 * F�r benutzerdefinierte Schaltfl�che in der Fu�zeile hinzu.
	 * Diese Funktion muss <b>vor</b> <code>createTabsGUI</code> oder <code>createSimpleGUI</code> aufgerufen werden!
	 * @param captions	Array mit den Namen der Schaltfl�chen
	 * @param icons	Array mit den URLs der Icons der Schaltfl�che (einzelne Eintr�ge oder der gesamte Parameter k�nnen <code>null</code> sein)
	 * @param handlers	Array mit den aufzurufenden Handlern f�r die Buttons
	 * @see #getUserButton(int)
	 */
	protected final void addUserButtons(String[] captions, Icon[] icons, Runnable[] handlers) {
		userButtonCaptions=captions;
		userButtonIcons=icons;
		userButtonHandlers=handlers;
	}

	/**
	 * F�r benutzerdefinierte Schaltfl�che in der Fu�zeile hinzu.
	 * Diese Funktion muss <b>vor</b> <code>createTabsGUI</code> oder <code>createSimpleGUI</code> aufgerufen werden!
	 * @param captions	Array mit den Namen der Schaltfl�chen
	 * @param tooltips	Array mit Tooltips f�r die Schaltfl�chen
	 * @param icons	Array mit den URLs der Icons der Schaltfl�che (einzelne Eintr�ge oder der gesamte Parameter k�nnen <code>null</code> sein)
	 * @param handlers	Array mit den aufzurufenden Handlern f�r die Buttons
	 * @see #getUserButton(int)
	 */
	protected final void addUserButtons(String[] captions, String[] tooltips, Icon[] icons, Runnable[] handlers) {
		userButtonCaptions=captions;
		userButtonTooltips=tooltips;
		userButtonIcons=icons;
		userButtonHandlers=handlers;
	}

	/**
	 * F�gt den Kopfbereich zu dem Dialog hinzu
	 * @param content	Gesamter Inhaltsbereich des Dialogs
	 * @param nameLabel	Beschriftung f�r das Name-Eingabefeld (kann <code>null</code> sein, dann wird kein Eingabefeld angelegt)
	 * @param nameValue Initialer Inhalt des Name-Eingabefeldes ({@link #name})
	 * @param activeLabel	Beschriftung f�r die Aktiv-Checkbox (kann <code>null</code> sein, dann wird keine Checkbox angelegt)
	 * @param activeValue Initiale Einstellung f�r die Aktiv-Checkbox ({@link #active})
	 * @see #name
	 * @see #active
	 */
	private void addHeader(Container content, String nameLabel, String nameValue, String activeLabel, boolean activeValue) {
		JPanel p;

		if ((nameLabel!=null && !nameLabel.isBlank()) || (activeLabel!=null && !activeLabel.isBlank())) {
			content.add(p=new JPanel(new BorderLayout(5,0)),BorderLayout.NORTH);
			p.setBorder(BorderFactory.createEmptyBorder(5,5,5,5));

			if (nameLabel!=null && !nameLabel.isBlank()) {
				if (nameValue==null) nameLabel="<html><body><b>"+nameLabel+"</b></body></html>";
				p.add(new JLabel(nameLabel),BorderLayout.WEST);
				if (nameValue!=null) {
					p.add(name=new JTextField(nameValue),BorderLayout.CENTER);
					name.addKeyListener(new NameKeyListener());
					name.setEnabled(!readOnly);
				}
			}

			if (activeLabel!=null && !activeLabel.isBlank()) {
				p.add(active=new JCheckBox(activeLabel,activeValue),BorderLayout.EAST);
				active.setEnabled(!readOnly);
			}

		} else {
			name=null;
		}
	}

	/**
	 * F�gt den Fu�bereich zu dem Dialog hinzu
	 * @param content	Gesamter Inhaltsbereich des Dialogs
	 * @param xSize	Breite des Fensters
	 * @param ySize	H�he des Fensters
	 * @param previous Beschriftung der Vorg�nger-Schaltl�che (wird ausgeblendet, wenn gleich null oder leer)
	 * @param next Beschriftung der Nachfolger-Schaltl�che (wird ausgeblendet, wenn gleich null oder leer)
	 */
	private void addFooter(Container content, int xSize, int ySize, String previous, String next) {
		JPanel p;

		content.add(p=new JPanel(new FlowLayout(FlowLayout.LEFT)),BorderLayout.SOUTH);

		if (showCloseButton) {
			p.add(closeButton=new JButton(Language.tr("Dialog.Button.Close")));
			closeButton.addActionListener(new CloseButtonActionEvents());
			closeButton.setIcon(Images.GENERAL_EXIT.getIcon());
			getRootPane().setDefaultButton(closeButton);
		} else {
			p.add(okButton=new JButton(Language.tr("Dialog.Button.Ok")));
			okButton.addActionListener(new CloseButtonActionEvents());
			okButton.setEnabled(!readOnly);
			okButton.setIcon(Images.MSGBOX_OK.getIcon());
			getRootPane().setDefaultButton(okButton);

			p.add(cancelButton=new JButton(Language.tr("Dialog.Button.Cancel")));
			cancelButton.addActionListener(new CloseButtonActionEvents());
			cancelButton.setIcon(Images.GENERAL_CANCEL.getIcon());
		}

		if (helpCallback!=null) {
			p.add(helpButton=new JButton(Language.tr("Dialog.Button.Help")));
			helpButton.addActionListener(new CloseButtonActionEvents());
			helpButton.setIcon(Images.HELP.getIcon());
		}

		if (previous!=null && !previous.isEmpty()) {
			p.add(previousButton=new JButton(previous));
			previousButton.addActionListener(new CloseButtonActionEvents());
			previousButton.setIcon(Images.ARROW_LEFT.getIcon());
		}

		if (next!=null && !next.isEmpty()) {
			p.add(nextButton=new JButton(next));
			nextButton.addActionListener(new CloseButtonActionEvents());
			nextButton.setIcon(Images.ARROW_RIGHT.getIcon());
		}

		if (userButtonCaptions!=null && userButtonCaptions.length>0) {
			userButtons=new JButton[userButtonCaptions.length];
			for (int i=0;i<userButtonCaptions.length;i++) {
				p.add(userButtons[i]=new JButton(userButtonCaptions[i]));
				if (userButtonTooltips!=null && userButtonTooltips.length>i && userButtonTooltips[i]!=null) userButtons[i].setToolTipText(userButtonTooltips[i]);
				if (userButtonIcons!=null && userButtonIcons.length>i && userButtonIcons[i]!=null) userButtons[i].setIcon(userButtonIcons[i]);
				if (userButtonHandlers!=null && userButtonHandlers.length>i && userButtonHandlers[i]!=null) userButtons[i].addActionListener(new UserButtonListener());
			}
		}

		addWindowListener(new WindowAdapter() {@Override
			public void windowClosing(WindowEvent event) {setVisible(false); dispose();}});
		setResizable(false);
		setSizeRespectingScreensize(this,xSize,ySize);
		setLocationRelativeTo(owner);
	}

	/**
	 * Aktiviert oder deaktiviert alle Schaltfl�chen.
	 * @param enabled	 Gibt an, ob die Schaltfl�chen aktiviert oder deaktiviert werden sollen.
	 */
	protected void setEnableButtons(boolean enabled) {
		if (okButton!=null) okButton.setEnabled(enabled);
		if (cancelButton!=null) cancelButton.setEnabled(enabled);
		if (helpButton!=null) helpButton.setEnabled(enabled);
		if (previousButton!=null) previousButton.setEnabled(enabled);
		if (nextButton!=null) nextButton.setEnabled(enabled);
		if (closeButton!=null) closeButton.setEnabled(enabled);
		if (userButtons!=null) for (JButton button: userButtons) if (button!=null) button.setEnabled(enabled);
	}

	/**
	 * Diese leere Methode muss in den Nachkommen von <code>BaseEditDialog</code>
	 * �berladen werden, um die einzelnen Dialogseiten zu erzeugen
	 * (sofern <code>createTabsGUI</code> verwendet werden soll).
	 * @param tabs	Referenz auf das <code>JTabbedPane</code>, welche die Tabs aufnimmt.
	 */
	protected void createTabs(JTabbedPane tabs) {}

	/**
	 * Diese leere Methode muss in den Nachkommen von <code>BaseEditDialog</code>
	 * �berladen werden, um den Dialoginhalt zu erzeugen
	 * (sofern <code>createSimpleGUI</code> verwendet werden soll).
	 * @param content	Referenz auf das Panel, welches den Dialoginhalt aufnimmt.
	 */
	protected void createSimpleContent(JPanel content) {}

	/**
	 * Diese Funktion f�gt ein Textfeld inkl. Beschreibung, nachfolgendem Button und Label hinter dem Button zu einem Panel hinzu.
	 * Das Panel sollte dabei den Layout-Typ GridLayout(2n,1) besitzen.
	 * @param p	Panel, in das das Eingabefeld eingef�gt werden soll
	 * @param name	Beschriftung f�r das Textfeld
	 * @param initialValue	Anf�nglicher Wert f�r das Textfeld
	 * @param buttonName	Beschriftung der Schaltfl�che
	 * @param buttonIcon	Icon das auf der Schaltfl�che angezeigt werden soll (wird hier <code>null</code> �bergeben, so wird kein Icon angezeigt)
	 * @return	Array aus drei Elementen: Referenz auf das neu erzeugte Textfeld, Rerferenz auf das neu erzeugte Button und Referenz auf das neu erzeugte Label rechts neben dem Button
	 */
	protected final JComponent[] addPercentInputLineWithButton(JPanel p, String name, double initialValue, String buttonName, Icon buttonIcon) {
		/* Initialwert f�r Textfeld vorbereiten */
		String s;
		if (initialValue>=0 && initialValue<=1)
			s=NumberTools.formatNumberMax(initialValue*100)+"%";
		else
			s=NumberTools.formatNumberMax(initialValue);

		JPanel subPanel;

		/* Textfeld anlegen */
		JTextField text;

		p.add(new JLabel(name));
		p.add(subPanel=new JPanel(new FlowLayout(FlowLayout.LEFT,0,0)));

		subPanel.setBorder(BorderFactory.createEmptyBorder(0,0,5,0));
		subPanel.add(text=new JTextField(s,10));

		/* Button anlegen */
		JButton button;

		subPanel.add(Box.createHorizontalStrut(10));
		subPanel.add(button=new JButton(buttonName));
		if (buttonIcon!=null) button.setIcon(buttonIcon);

		/* Label anlegen */
		JLabel label;
		subPanel.add(Box.createHorizontalStrut(10));
		subPanel.add(label=new JLabel());

		return new JComponent[]{text,button,label};
	}

	/**
	 * Diese Funktion f�gt ein Textfeld inkl. Beschreibung zu einem Panel hinzu.
	 * Das Panel sollte dabei den Layout-Typ GridLayout(2n,1) besitzen.
	 * @param p	Panel, in das das Eingabefeld eingef�gt werden soll
	 * @param name	Beschriftung f�r das Textfeld
	 * @param initialValue	Anf�nglicher Wert f�r das Textfeld
	 * @return	Referenz auf das neu erzeugte Textfeld
	 */
	protected final JTextField addPercentInputLine(JPanel p, String name, double initialValue) {
		String s;
		if (initialValue>=0 && initialValue<=1)
			s=NumberTools.formatNumberMax(initialValue*100)+"%";
		else
			s=NumberTools.formatNumberMax(initialValue);
		return addPercentInputLine(p,name,s);
	}

	/**
	 * Diese Funktion f�gt ein Textfeld inkl. Beschreibung zu einem Panel hinzu.
	 * Das Panel sollte dabei den Layout-Typ GridLayout(2n,1) besitzen.
	 * @param p	Panel, in das das Eingabefeld eingef�gt werden soll
	 * @param name	Beschriftung f�r das Textfeld
	 * @param initialValue	Anf�nglicher Wert f�r das Textfeld
	 * @return	Referenz auf das neu erzeugte Textfeld
	 */
	protected final JTextField addPercentInputLine(JPanel p, String name, String initialValue) {
		JPanel subPanel;
		JTextField text;

		p.add(new JLabel(name));
		p.add(subPanel=new JPanel(new FlowLayout(FlowLayout.LEFT,0,0)));

		subPanel.setBorder(BorderFactory.createEmptyBorder(0,0,5,0));
		subPanel.add(text=new JTextField(initialValue,10));
		return text;
	}

	/**
	 * Diese Funktion f�gt ein Textfeld inkl. Beschreibung zu einem Panel hinzu.
	 * Das Panel sollte dabei den Layout-Typ GridLayout(2n,1) besitzen.
	 * @param p	Panel, in das das Eingabefeld eingef�gt werden soll
	 * @param name	Beschriftung f�r das Textfeld
	 * @param initialValue	Anf�nglicher Wert f�r das Textfeld
	 * @return	Referenz auf das neu erzeugte Textfeld
	 */
	protected final JTextField addInputLine(JPanel p, String name, double initialValue) {
		return addInputLine(p,name,NumberTools.formatNumberMax(initialValue));
	}

	/**
	 * Diese Funktion f�gt ein Textfeld inkl. Beschreibung zu einem Panel hinzu.
	 * Das Panel sollte dabei den Layout-Typ GridLayout(2n,1) besitzen.
	 * @param p	Panel, in das das Eingabefeld eingef�gt werden soll
	 * @param name	Beschriftung f�r das Textfeld
	 * @param initialValue	Anf�nglicher Wert f�r das Textfeld
	 * @return	Referenz auf das neu erzeugte Textfeld
	 */
	protected final JTextField addInputLine(JPanel p, String name, String initialValue) {
		return addInputLine(p,name,initialValue,10);
	}

	/**
	 * Diese Funktion f�gt ein Textfeld inkl. Beschreibung zu einem Panel hinzu.
	 * Das Panel sollte dabei den Layout-Typ GridLayout(2n,1) besitzen.
	 * @param p	Panel, in das das Eingabefeld eingef�gt werden soll
	 * @param name	Beschriftung f�r das Textfeld
	 * @param initialValue	Anf�nglicher Wert f�r das Textfeld
	 * @param columns	Breite des Textfeldes
	 * @return	Referenz auf das neu erzeugte Textfeld
	 */
	protected final JTextField addInputLine(JPanel p, String name, String initialValue, int columns) {
		JPanel subPanel;
		JTextField text;

		p.add(new JLabel(name));
		p.add(subPanel=new JPanel(new FlowLayout(FlowLayout.LEFT,0,0)));

		subPanel.setBorder(BorderFactory.createEmptyBorder(0,0,5,0));
		subPanel.add(text=new JTextField(initialValue,columns));
		return text;
	}

	/**
	 * Diese Funktion f�gt ein Textfeld inkl. Beschreibung zu einem Panel hinzu.
	 * Label und Eingabezeile werden dabei untereinander dargestellt.
	 * Das Panel sollte dabei den Layout-Typ BoxLayout in vertikaler Ausrichtung besitzen.
	 * @param p	Panel, in das das Eingabefeld eingef�gt werden soll
	 * @param name	Beschriftung f�r das Textfeld
	 * @param initialValue	Anf�nglicher Wert f�r das Textfeld
	 * @param columns	Breite des Textfeldes
	 * @return	Referenz auf das neu erzeugte Textfeld
	 */
	protected final JTextField addVerticalInputLine(JPanel p, String name, String initialValue, int columns) {
		JPanel subPanel;
		JTextField text;

		p.add(subPanel=new JPanel(new FlowLayout(FlowLayout.LEFT,0,0)));
		subPanel.setBorder(BorderFactory.createEmptyBorder(0,0,5,0));
		subPanel.add(new JLabel(name));

		p.add(subPanel=new JPanel(new FlowLayout(FlowLayout.LEFT,0,0)));
		subPanel.setBorder(BorderFactory.createEmptyBorder(0,0,5,0));
		subPanel.add(text=new JTextField(initialValue,columns));
		return text;
	}

	/**
	 * Diese Funktion f�gt eine Combobox inkl. Beschreibung zu einem Panel hinzu.
	 * Das Panel sollte dabei den Layout-Typ GridLayout(2n,1) besitzen.
	 * @param p	Panel, in das das Eingabefeld eingef�gt werden soll
	 * @param name	Beschriftung f�r die Combobox
	 * @param values	Werte f�r die Combobox
	 * @return	Referenz auf die neu erzeugte Combobox
	 */
	protected final JComboBox<String> addComboBox(JPanel p, String name, String[] values) {
		JPanel subPanel;
		JComboBox<String> box;

		p.add(new JLabel(name));
		p.add(subPanel=new JPanel(new FlowLayout(FlowLayout.LEFT,0,0)));

		subPanel.setBorder(BorderFactory.createEmptyBorder(0,0,5,0));
		subPanel.add(box=new JComboBox<>(values));
		return box;
	}

	/**
	 * Versucht den Dialog mit dem angegebenen ClosedBy-Typ zu schlie�en.
	 * @param closedBy	Gibt an, auf welche Art der Dialog geschlossen werden soll (siehe <code>CLOSED_BY_*</code> Konstanten)
	 * @return	Gibt <code>true</code> zur�ck, wenn der Dialog geschlossen wurde.
	 */
	protected final boolean closeDialog(int closedBy) {
		if (closedBy!=CLOSED_BY_CANCEL) {
			if (!checkData()) return false;
			storeData();
		}
		this.closedBy=closedBy;
		setVisible(false);
		dispose();
		return true;
	}

	/**
	 * Gibt an, wie der Dialog geschlossen wurde.
	 * @return	Enth�lt eine der <code>CLOSED_BY_*</code>-Konstanten.
	 */
	public final int getClosedBy() {
		return closedBy;
	}

	/**
	 * Liefert zur�ck, welcher Tab als letztes aktiv war.
	 * @return	Aktiver Tab.
	 */
	public int getTabIndex() {
		if (tabs==null) return -1;
		return tabs.getSelectedIndex();
	}

	/**
	 * Stellt ein, welcher Tab angezeigt werden soll.
	 * @param index	Zu aktivierender Tab.
	 */
	public void setTabIndex(int index) {
		if (tabs==null) return;
		if (index>=0) tabs.setSelectedIndex(index);
	}

	/**
	 * Wird beim Klicken auf "Ok" aufgerufen, um zu pr�fen, ob die Daten in der aktuellen Form
	 * in Ordnung sind und gespeichert werden k�nnen.
	 * @return	Gibt <code>true</code> zur�ck, wenn die Daten in Ordnung sind.
	 */
	protected boolean checkData() {
		return true;
	}

	/**
	 * Speichert die Dialog-Daten in dem zugeh�rigen Daten-Objekt.<br>
	 * (Diese Routine wird beim Klicken auf "Ok" nach <code>checkData</code> aufgerufen.
	 * @see #checkData()
	 */
	protected void storeData() {
	}

	/**
	 * Wird aufgerufen, wenn der oben im Dialog angezeigte Name vom Benutzer ge�ndert wurde.
	 * Durch das �berladen dieser Dummy-Methode kann auf Namens�nderungen reagiert werden.
	 * @param newName	Neuer Name
	 */
	protected void nameChange(String newName) {}

	/**
	 * Liefert eines der per <code>addUserButtons</code> angelegten Nutzer-Schaltfl�chen zur�ck
	 * @param index	Index der Schaltfl�che innerhalb der Nutzer-Schaltfl�chen
	 * @return	Schaltfl�chen-Objekt oder <code>null</code>, wenn der Index au�erhalb des zul�ssigen Bereichs liegt.
	 * @see #addUserButtons(String[], Icon[], Runnable[])
	 * @see #addUserButtons(String[], String[], Icon[], Runnable[])
	 */
	protected JButton getUserButton(int index) {
		if (userButtons==null || index<0 || index>=userButtons.length) return null;
		return userButtons[index];
	}

	/**
	 * Reagiert auf Klicks auf die verschiedenen m�glichen Schaltfl�chen
	 * zum Schlie�en des Dialogs
	 */
	private class CloseButtonActionEvents implements ActionListener {
		/**
		 * Konstruktor der Klasse
		 */
		public CloseButtonActionEvents() {
			/*
			 * Wird nur ben�tigt, um einen JavaDoc-Kommentar f�r diesen (impliziten) Konstruktor
			 * setzen zu k�nnen, damit der JavaDoc-Compiler keine Warnung mehr ausgibt.
			 */
		}

		@Override
		public void actionPerformed(ActionEvent e) {
			if (e.getSource()==closeButton) {closedBy=CLOSED_BY_OK;}
			if (e.getSource()==okButton) {if (!checkData()) return;	storeData(); closedBy=CLOSED_BY_OK;}
			if (e.getSource()==helpButton && helpCallback!=null) {SwingUtilities.invokeLater(helpCallback); return;}
			if (e.getSource()==previousButton) {if (!readOnly) {if (!checkData()) return; storeData();} closedBy=CLOSED_BY_PREVIOUS;}
			if (e.getSource()==nextButton) {if (!readOnly) {if (!checkData()) return; storeData();} closedBy=CLOSED_BY_NEXT;}
			setVisible(false);
			dispose();
		}
	}

	/**
	 * Reagiert auf Tastendr�cke in {@link BaseEditDialog#name}
	 * @see BaseEditDialog#name
	 */
	private class NameKeyListener implements KeyListener {
		/**
		 * Konstruktor der Klasse
		 */
		public NameKeyListener() {
			/*
			 * Wird nur ben�tigt, um einen JavaDoc-Kommentar f�r diesen (impliziten) Konstruktor
			 * setzen zu k�nnen, damit der JavaDoc-Compiler keine Warnung mehr ausgibt.
			 */
		}

		@Override public void keyTyped(KeyEvent e) {nameChange(name.getText());}
		@Override public void keyPressed(KeyEvent e) {nameChange(name.getText());}
		@Override public void keyReleased(KeyEvent e) {nameChange(name.getText());}
	}

	/**
	 * Listener f�r die benutzerdefinierten Schaltfl�chen
	 */
	private class UserButtonListener implements ActionListener {
		/**
		 * Konstruktor der Klasse
		 */
		public UserButtonListener() {
			/*
			 * Wird nur ben�tigt, um einen JavaDoc-Kommentar f�r diesen (impliziten) Konstruktor
			 * setzen zu k�nnen, damit der JavaDoc-Compiler keine Warnung mehr ausgibt.
			 */
		}

		@Override
		public void actionPerformed(ActionEvent e) {
			int nr=-1;
			if (userButtons!=null) for (int i=0;i<userButtons.length;i++) if (userButtons[i]==e.getSource()) {nr=i; break;}
			if (nr<0) return;
			if (userButtonHandlers==null || userButtonHandlers.length<=nr || userButtonHandlers[nr]==null) return;
			userButtonHandlers[nr].run();
		}
	}
}
