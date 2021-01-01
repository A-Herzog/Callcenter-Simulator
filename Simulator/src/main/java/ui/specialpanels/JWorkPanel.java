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
package ui.specialpanels;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.Container;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

import javax.swing.AbstractAction;
import javax.swing.BorderFactory;
import javax.swing.Icon;
import javax.swing.InputMap;
import javax.swing.JButton;
import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JToolBar;
import javax.swing.KeyStroke;
import javax.swing.SwingUtilities;

import language.Language;
import ui.images.Images;

/**
 * Basisklasse für Batch-Verarbeitung, Optimierung usw.
 * Das erzeugte <code>JPanel</code> kann direkt innerhalb des Hauptfensters verwendet werden.
 * @author Alexander Herzog
 * @version 1.0
 */
public class JWorkPanel extends JCloseablePanel {
	/**
	 * Serialisierungs-ID der Klasse
	 * @see Serializable
	 */
	private static final long serialVersionUID = 5224062990520423023L;

	/**
	 * Objekt vom Typ <code>Runnable</code>, dessen <code>run()</code>-Methode aufgerufen wird, wenn sich das Panel schließen möchte.
	 */
	protected Runnable doneNotify;

	/**
	 * Symbolleiste in dem Panel
	 */
	protected JToolBar buttonPanel;

	/** Schaltfläche "Start" */
	private JButton workButton;
	/** Schaltfläche "Abbruch" */
	private JButton cancelButton;
	/** Schaltfläche "Schließen" */
	private JButton closeButton;
	/** Schaltfläche "Hilfe" */
	private JButton helpButton;
	/** Weitere benutzerdefinierte Schaltflächen ({@link #addFooterButton(String)}) */
	private final List<JButton> userButtons=new ArrayList<JButton>();
	/** Wird hier ein Wert ungleich <code>null</code> übergeben, so wird eine "Hilfe"-Schaltfläche angezeigt und die <code>Run</code>-Methode dieses Objekts beim Klicken auf diese Schaltfläche aufgerufen */
	private final Runnable helpCallback;

	/**
	 * Soll die Verarbeitung abgebrochen werden?
	 */
	protected boolean cancelWork=false;

	/**
	 * Konstruktor der Klasse <code>JWorkPanel</code>
	 * @param doneNotify	Objekt vom Typ <code>Runnable</code>, dessen <code>run()</code>-Methode aufgerufen wird, wenn sich das Panel schließen möchte.
	 * @param helpCallback	Wird hier ein Wert ungleich <code>null</code> übergeben, so wird eine "Hilfe"-Schaltfläche angezeigt und die <code>Run</code>-Methode dieses Objekts beim Klicken auf diese Schaltfläche aufgerufen
	 */
	protected JWorkPanel(Runnable doneNotify, Runnable helpCallback) {
		super();
		this.doneNotify=doneNotify;
		this.helpCallback=helpCallback;
		setLayout(new BorderLayout());

		InputMap inputMap=getInputMap(JComponent.WHEN_IN_FOCUSED_WINDOW);

		KeyStroke stroke=KeyStroke.getKeyStroke("ESCAPE");
		inputMap.put(stroke,"ESCAPE");
		getActionMap().put("ESCAPE",new SpecialKeyListener(0));

		stroke=KeyStroke.getKeyStroke("F1");
		inputMap.put(stroke,"F1");
		getActionMap().put("F1",new SpecialKeyListener(1));

	}

	/**
	 * Reagiert auf F1- und Escape-Tastendrücke
	 */
	private final class SpecialKeyListener extends AbstractAction {
		/**
		 * Serialisierungs-ID der Klasse
		 * @see Serializable
		 */
		private static final long serialVersionUID = -3547884828037503034L;

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
			case 0: requestClose(); break;
			case 1: if (helpCallback!=null) SwingUtilities.invokeLater(helpCallback);
			}
		}
	}

	@Override
	public void setVisible(boolean visible) {
		super.setVisible(visible);
		if (visible) setVisibleInit();
	}

	/**
	 * Initialisiert die GUI-Komponenten des Panels
	 */
	public void setVisibleInit() {
		if (buttonPanel==null) {
			addCompleteFooter();
			setWorkMode(false);
		}
	}

	/** Name der "Start"-Schaltfläche */
	private String workName=null;
	/** Symbol, welches auf der "Start"-Schaltfläche angezeigt werden soll */
	private Icon workIcon=null;
	/** Name der "Abbrechen"-Schaltfläche */
	private String cancelName=null;

	/**
	 * Fügt die Fußzeile mit den Schaltflächen zum Starten des Prozesses und zum Schließen des Panels ein.
	 * @param workName	Name der "Start"-Schaltfläche
	 * @param workIcon	Symbol, welches auf der "Start"-Schaltfläche angezeigt werden soll
	 * @param cancelName	Name der "Abbrechen"-Schaltfläche
	 */
	protected final void addFooter(String workName, Icon workIcon, String cancelName) {
		this.workName=workName;
		this.workIcon=workIcon;
		this.cancelName=cancelName;
	}

	/**
	 * Fügt den Fußbereich hinzu.
	 */
	private final void addCompleteFooter() {
		/* add(buttonPanel=new JPanel(new FlowLayout(FlowLayout.LEFT)),BorderLayout.SOUTH); */
		add(buttonPanel=new JToolBar(),BorderLayout.NORTH);

		if (buttonPanel instanceof JToolBar) {
			buttonPanel.setFloatable(false);
			buttonPanel.setBorder(BorderFactory.createMatteBorder(0,0,1,0,Color.GRAY));
		}

		if (workName!=null && !workName.isEmpty()) {
			buttonPanel.add(workButton=new JButton(workName));
			workButton.addActionListener(new WorkButtonListener());
			if (workIcon!=null) workButton.setIcon(workIcon);
		}

		if (cancelName==null || cancelName.isEmpty()) cancelName=Language.tr("Dialog.Button.Abort");
		buttonPanel.add(cancelButton=new JButton(cancelName));
		cancelButton.addActionListener(new WorkButtonListener());
		cancelButton.setVisible(false);
		cancelButton.setIcon(Images.GENERAL_CANCEL.getIcon());

		buttonPanel.add(closeButton=new JButton(Language.tr("Dialog.Button.Close")));
		closeButton.addActionListener(new WorkButtonListener());
		closeButton.setIcon(Images.GENERAL_EXIT.getIcon());

		if (buttonPanel instanceof JToolBar) buttonPanel.addSeparator();

		if (userButtons.size()>0) {
			for (int i=0;i<userButtons.size();i++) buttonPanel.add(userButtons.get(i));
			if (buttonPanel instanceof JToolBar) buttonPanel.addSeparator();
		}

		if (helpCallback!=null) {
			buttonPanel.add(helpButton=new JButton(Language.tr("Dialog.Button.Help")));
			helpButton.addActionListener(new WorkButtonListener());
			helpButton.setIcon(Images.HELP.getIcon());
		}
	}

	/**
	 * Fügt eine weitere Schaltfläche zur Fußzeile hinzu. Die neue Schaltfläche wird mit den anderen beim Umschalen in
	 * den Run-Modus ausgeblendet.
	 * @param title	Beschriftung der Schaltfläche
	 * @return	Neue Schaltfläche
	 * @see #userButtonClick(int, JButton)
	 * @see #addFooter(String, Icon, String)
	 */
	protected final JButton addFooterButton(String title) {
		return addFooterButton(title,null);
	}

	/**
	 * Fügt eine weitere Schaltfläche zur Fußzeile hinzu. Die neue Schaltfläche wird mit den anderen beim Umschalen in
	 * den Run-Modus ausgeblendet.
	 * @param title	Beschriftung der Schaltfläche
	 * @param icon	Icon das neben der Schaltfläche angezeigt werden soll
	 * @return	Neue Schaltfläche
	 * @see #userButtonClick(int, JButton)
	 * @see #addFooter(String, Icon, String)
	 */
	protected final JButton addFooterButton(String title, Icon icon) {
		JButton newButton;
		if (icon==null) {
			newButton=new JButton(title);
		} else {
			newButton=new JButton(title,icon);
		}
		newButton.addActionListener(new WorkButtonListener());
		userButtons.add(newButton);
		return newButton;
	}

	/**
	 * Liefert eine der Fußzeilenschaltflächen zurück.
	 * @param index	Nummer der Schaltfläche
	 * @return	Objekt der Schaltfläche oder <code>null</code> wenn der Index außerhalb des gültigen Bereichs liegt.
	 */
	protected final JButton getFooterButton(int index) {
		if (index<0 || index>=userButtons.size()) return null;
		return userButtons.get(index);
	}

	/**
	 * Schaltet die Fußzeilen-GUI zwischen Bearbeiten- und Prozess-Modus um
	 * @param running	Mit <code>true</code> wird der Prozess-Modus aktiviert.
	 */
	protected void setWorkMode(boolean running) {
		if (!isVisible()) return;
		if (workButton!=null) workButton.setVisible(!running);
		if (cancelButton!=null) cancelButton.setVisible(running);
		if (closeButton!=null) closeButton.setVisible(!running);
		for (int i=0;i<userButtons.size();i++) userButtons.get(i).setVisible(!running);
	}

	/**
	 * Schließt das Panel.
	 */
	protected void done() {
		SwingUtilities.invokeLater(doneNotify);
	}

	/**
	 * In dieser Routine findet die eigentlich Arbeit statt.
	 * Sie wird durch das Klicken auf die "Start"-Schaltfläche gestartet.
	 */
	protected void run() {
	}

	/**
	 * Wird aufgerufen, wenn auf eines der per <code>addFooterButton</code> erzeugten Buttons geklickt wird.
	 * @param index	0-basierender Index des Buttons
	 * @param button	angeklichtes <code>JButton</code>
	 * @see #addFooterButton(String)
	 * @see #addFooterButton(String, Icon)
	 */
	protected void userButtonClick(int index, JButton button) {}

	/**
	 * Reagiert auf Klicks auf die Schaltflächen
	 * @see JWorkPanel#workButton
	 * @see JWorkPanel#cancelButton
	 * @see JWorkPanel#closeButton
	 * @see JWorkPanel#helpButton
	 * @see JWorkPanel#userButtons
	 *
	 */
	private final class WorkButtonListener implements ActionListener {
		@Override
		public void actionPerformed(ActionEvent e) {
			if (e.getSource()==workButton) {cancelWork=false; run(); return;}
			if (e.getSource()==cancelButton) {cancelWork=true; return;}
			if (e.getSource()==closeButton) {done(); return;}
			if (e.getSource()==helpButton && helpCallback!=null) SwingUtilities.invokeLater(helpCallback);
			for (int i=0;i<userButtons.size();i++) if (e.getSource()==userButtons.get(i)) {userButtonClick(i,userButtons.get(i)); return;}
		}
	}

	/* (non-Javadoc)
	 * @see complexcallcenter.specialpanels.JCloseablePanel#requestClose()
	 */
	@Override
	public void requestClose() {
		if (cancelButton!=null && cancelButton.isVisible()) cancelWork=true; else done();
	}

	/**
	 * Aktiviert oder deaktiviert eine Komponentne und alle
	 * möglicherweise darin enthaltenen Unterkomponenten.
	 * @param comp	Zu aktivierende oder zu deaktivierende Komponente
	 * @param active	Aktivieren (<code>true</code>) oder deaktivieren (<code>false</code>)
	 */
	public static void setEnableGUI(Component comp, boolean active) {
		if (!(comp instanceof JPanel) && !(comp instanceof JLabel)) comp.setEnabled(active);
		if (comp instanceof Container)
			for (Component c: ((Container)comp).getComponents()) setEnableGUI(c,active);
	}
}
