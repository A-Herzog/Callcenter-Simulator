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

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.net.URL;

import javax.swing.Icon;
import javax.swing.JComponent;
import javax.swing.JPanel;
import javax.swing.JTextField;

import ui.model.CallcenterModelCaller;

/**
 * Basisklasse f�r die Tabs des Anrufergruppen-Editors
 * @author Alexander Herzog
 * @version 1.0
 */
public abstract class CallerEditPanel extends JPanel {
	private static final long serialVersionUID = 2370102609837485314L;

	/**
	 * �bergeordnetes Dialogfenster.
	 */
	protected final BaseEditDialog parent;

	/**
	 * Kundentyp, der bearbeitet werden soll (Daten <b>nicht</b> hinhin zur�ckschreiben
	 */
	protected final CallcenterModelCaller caller;

	/**
	 * Gibt den Index den aktuellen Kundentyps in der Liste alle Kundentypen an (z.B. um diesen in solch einer Liste hervorzuheben)
	 */
	protected final int callerTypeIndexForThisType;

	/**
	 * Systemweiter Service-Level
	 */
	protected final short serviceLevelModel;

	/**
	 * Liste aller Anrufergruppennamen
	 */
	protected final String[] callerTypeNames;

	/**
	 * Liste aller Skill-Level-Namen
	 */
	protected final String[] skills;

	/**
	 * Gibt an, ob die Einstellungen ver�ndert werden d�rfen.
	 */
	protected final boolean readOnly;

	/**
	 * Runnable, welches aufgerufen wird, um die Hilfe zu diesem Dialog anzuzeigen.
	 */
	protected final Runnable helpCallback;

	/**
	 * Stellt einen <code>ActionListener</code>, <code>KeyListener</code> bereit, die
	 * bei Dialogelementen angegeben werden. Tastendr�cke l�sen <code>check</code>-Aufrufe
	 * aus und Dialogaktionen k�nnen per <code>processDialogEvents</code> bearbeitet werden.
	 * @see #check(KeyEvent)
	 * @see #processDialogEvents(ActionEvent)
	 */
	protected final DialogElementListener dialogElementListener;

	/**
	 * Konstruktor der Klasse <code>CallerEditPanel</code>
	 * @param initData	Datensatz, der alle zur Initialisierung des Panels notwendigen Daten enth�lt
	 */
	public CallerEditPanel(final InitData initData) {
		this.parent=initData.parent;
		this.serviceLevelModel=initData.serviceLevelModel;
		this.caller=initData.caller;
		this.callerTypeIndexForThisType=initData.callerTypeIndexForThisType;
		this.callerTypeNames=initData.callerTypeNames;
		this.skills=initData.skills;
		this.readOnly=initData.readOnly;
		this.helpCallback=initData.helpCallback;
		dialogElementListener=new DialogElementListener();
	}

	/**
	 * Pr�ft, ob alle Einstellung in Ordnung sind.
	 * (Und f�rbt dabei ggf. fehlerhafte Eingabefelder rot ein.)
	 * @param e	Option kann hier angegeben werden, wenn ein Dialogelement durch eine Eingabe den Check ausgel�st hat.
	 * @return	Gibt <code>null</code> zur�ck, wenn alles in Ordnung ist, ansonsten ein 2-elementiges Array aus Titel und Infotext der Fehlermeldung.
	 */
	public String[] check(KeyEvent e) {
		return null;
	}

	/**
	 * Schreibt die Daten aus dem Dialog in einen Kundentyp zur�ck
	 * @param newCaller	Kundentyp-Instanz, in die die Daten geschrieben werden sollen
	 */
	public abstract void writeToCaller(CallcenterModelCaller newCaller);

	/**
	 * Name der als �berschrift im Tab angezeigt werden soll
	 * @return	Name des Tabs
	 */
	public abstract String getTabName();

	/**
	 * Icon-Objekt, das im Tab angezeigt werden soll
	 * (Liest daf�r <code>getTabIcon</code> aus und erstellt ein Icon-Objekt).
	 * @return Icon-Objekt oder <code>null</code>, wenn kein Icon gesetzt ist.
	 */
	public abstract Icon getTabIconObject();

	/**
	 * Erm�glicht dem Panel auf �nderungen des Namens des Kundentyps zu reagieren
	 * @param newName	Neuer Name f�r den Kundentyp
	 */
	public void nameChange(String newName) {}

	/**
	 * Verarbeitet optionale Dialog-Element-Events
	 * @param e	Durchgereichetes <code>ActionEvent</code>
	 */
	protected void processDialogEvents(ActionEvent e) {}

	private class DialogElementListener implements ActionListener, KeyListener {
		@Override
		public void actionPerformed(ActionEvent e) {
			processDialogEvents(e);
		}

		@Override
		public void keyTyped(KeyEvent e) {
			check(e);
		}

		@Override
		public void keyPressed(KeyEvent e) {
			check(e);
		}

		@Override
		public void keyReleased(KeyEvent e) {
			check(e);
		}
	}

	private int openGenerator=0;

	/**
	 * Weist den Dialog an, sich zu schlie�en und ein openGenerator-Level zur�ckzugeben.
	 * @param number	openGenerator-Code, der durchgereicht werden soll
	 */
	protected final void requestOpenGenerator(int number) {
		openGenerator=number;
		if (!parent.closeDialog(BaseEditDialog.CLOSED_BY_OK)) openGenerator=0;
		return;
	}

	/**
	 * Ergm�glich dem Eltern-Dialog abzufragen, ob dieses Panel den Generator �ffnen m�chte
	 * @return	Gibt 0 zur�ck, wenn es keine besonderen W�nsche seitens des Panels gibt; ansonsten die Nummer des Generators, die ge�ffnet werden soll.
	 */
	public final int getOpenGenerator() {
		return openGenerator;
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
		return parent.addInputLine(p,name,initialValue,columns);
	}

	/**
	 * Diese Funktion f�gt ein Textfeld inkl. Beschreibung, nachfolgendem Button und Label hinter dem Button zu einem Panel hinzu.
	 * Das Panel sollte dabei den Layout-Typ GridLayout(2n,1) besitzen.
	 * @param p	Panel, in das das Eingabefeld eingef�gt werden soll
	 * @param name	Beschriftung f�r das Textfeld
	 * @param initialValue	Anf�nglicher Wert f�r das Textfeld
	 * @param buttonName	Beschriftung der Schaltfl�che
	 * @param buttonIcon	URL zu dem Icon, dass auf der Schaltfl�che angezeigt werden soll (wird hier <code>null</code> �bergeben, so wird kein Icon angezeigt)
	 * @return	Array aus drei Elementen: Referenz auf das neu erzeugte Textfeld, Rerferenz auf das neu erzeugte Button und Referenz auf das neu erzeugte Label rechts neben dem Button
	 */
	protected final JComponent[] addPercentInputLineWithButton(JPanel p, String name, double initialValue, String buttonName, URL buttonIcon) {
		return parent.addPercentInputLineWithButton(p,name,initialValue,buttonName,buttonIcon);
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
		return parent.addPercentInputLine(p,name,initialValue);
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
		return parent.addInputLine(p,name,initialValue);
	}

	/**
	 * Datensatz mit allen Informationen, die ein Panel zur Initialisierung ben�tigt.
	 * @author Alexander Herzog
	 * @version 1.0
	 */
	public final static class InitData {
		private final BaseEditDialog parent;
		private final CallcenterModelCaller caller;
		private final int callerTypeIndexForThisType;
		private final short serviceLevelModel;
		private final String[] callerTypeNames;
		private final String[] skills;
		private final boolean readOnly;
		private final Runnable helpCallback;

		/**
		 * Konstruktor der Klasse <code>CallerEditPanel.InitData</code>
		 * @param parent	Eltern-Dialog-Fenster vom Type <code>BaseEditDialog</code> (bei dem gegebenenfalls die <code>closeDialog</code>-Funktion aufgerufen wird, wenn das Panel den Dialog schlie�en m�chte).
		 * @param caller	Anrufergruppe, die bearbeitet werden soll. (Wird der Klasse per protected Membervariable zur Verf�gung gestellt.)
		 * @param callerTypeIndexForThisType	Gibt den Index den aktuellen Kundentyps in der Liste alle Kundentypen an (z.B. um diesen in solch einer Liste hervorzuheben) (Wird der Klasse per protected Membervariable zur Verf�gung gestellt.)
		 * @param serviceLevelModel	Systemweiter Service-Level (Wird der Klasse per protected Membervariable zur Verf�gung gestellt.)
		 * @param callerTypeNames Liste aller Anrufergruppennamen (Wird der Klasse per protected Membervariable zur Verf�gung gestellt.)
		 * @param skills Liste aller Skill-Level-Namen (Wird der Klasse per protected Membervariable zur Verf�gung gestellt.)
		 * @param readOnly Gibt an, ob die Einstellungen ver�ndert werden d�rfen. (Wird der Klasse per protected Membervariable zur Verf�gung gestellt.)
		 * @param helpCallback	Wird hier ein Wert ungleich <code>null</code> �bergeben, so wird eine "Hilfe"-Schaltfl�che angezeigt und die <code>Run</code>-Methode dieses Objekts beim Klicken auf diese Schaltfl�che aufgerufen
		 */
		public InitData(BaseEditDialog parent, CallcenterModelCaller caller, int callerTypeIndexForThisType, short serviceLevelModel, String[] callerTypeNames, String[] skills, boolean readOnly, Runnable helpCallback) {
			this.parent=parent;
			this.serviceLevelModel=serviceLevelModel;
			this.caller=caller;
			this.callerTypeIndexForThisType=callerTypeIndexForThisType;
			this.callerTypeNames=callerTypeNames;
			this.skills=skills;
			this.readOnly=readOnly;
			this.helpCallback=helpCallback;
		}
	}
}