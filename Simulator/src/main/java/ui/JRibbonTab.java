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
package ui;

import java.awt.FlowLayout;
import java.awt.event.ActionListener;
import java.io.Serializable;
import java.net.URL;

import javax.swing.Icon;
import javax.swing.JButton;
import javax.swing.JPanel;

/**
 * Diese Klasse implementiert ein einzelnes Toolbar-Ribbon innerhalb eines <code>JRibbonBar</code>.
 * @author Alexander Herzog
 * @version 1.0
 * @see JRibbonBar
 */
class JRibbonTab extends JPanel {
	/**
	 * Serialisierungs-ID der Klasse
	 * @see Serializable
	 */
	private static final long serialVersionUID = 4763704485543681895L;

	private JRibbonSection currentSection;

	/**
	 * <code>ActionListener</code> der f�r alle Buttons verwendet werden soll (kann <code>null</code> sein).
	 */
	public final ActionListener defaultActionListener;

	/**
	 * Konstruktor der Klasse <code>JRibbonTab</code>
	 * @param defaultActionListener	<code>ActionListener</code> der f�r alle Buttons verwendet werden soll (kann <code>null</code> sein).
	 */
	public JRibbonTab(final ActionListener defaultActionListener) {
		super();
		this.defaultActionListener=defaultActionListener;
		setLayout(new FlowLayout(FlowLayout.LEFT,0,0));
	}

	/**
	 * Konstruktor der Klasse <code>JRibbonTab</code>
	 */
	public JRibbonTab() {
		this(null);
	}

	/**
	 * F�gt eine neue Sektion zu dem Ribbon hinzu.
	 * @param title	Unter den Buttons der Sektion anzuzeigende Beschreibung
	 * @return	Liefert das Objekt der neuen Sektion zur�ck. �ber dieses Objekt k�nnen jederzeit weitere Icons zu der Sektion hinzugef�gt werden. (�ber die entsprechenden Methoden von <code>JRibbon</code> k�nnen jeweils nur zu der aktuellen Sektion Buttons hinzugef�gt werden.)
	 * @see JRibbonSection
	 */
	public JRibbonSection addSection(final String title) {
		JRibbonSection section=new JRibbonSection(title,defaultActionListener);
		add(section);
		currentSection=section;
		return section;
	}

	/**
	 * F�gt zu der aktuellen Sektion eine neue vertikale Spalte hinzu.
	 * Nach dem Hinzuf�gen einer Sektion zu einem Ribbon ist sofort eine erste vertikale Spalte vorhanden.
	 * @see JRibbonSection
	 */
	public void addRow() {
		if (currentSection==null) return;
		currentSection.addRow();
	}

	/**
	 * F�gt eine Schaltfl�che, bei der sich das Icon �ber dem Text befindet, zu der aktuellen Sektion hinzu.
	 * @param title	Beschriftung der Schaltfl�che
	 * @param tooltip	Tooltip, der angezeigt werden sollen, wenn die Maus �ber die Schaltfl�che bewegt wird. (Kann <code>null</code> oder auch ein leerer String sein.)
	 * @param icon	Icon, das auf der Schaltfl�che angezeigt werden soll. (Kann <code>null</code> sein.)
	 * @return	Objekt der neuen Schaltfl�che
	 */
	public JButton addBigButtonURLIcon(final String title, final String tooltip, final URL icon) {
		if (currentSection==null) return null;
		return currentSection.addBigButtonURLIcon(title,tooltip,icon);
	}

	/**
	 * F�gt eine Schaltfl�che, bei der sich das Icon �ber dem Text befindet, zu der aktuellen Sektion hinzu.
	 * @param title	Beschriftung der Schaltfl�che
	 * @param tooltip	Tooltip, der angezeigt werden sollen, wenn die Maus �ber die Schaltfl�che bewegt wird. (Kann <code>null</code> oder auch ein leerer String sein.)
	 * @param icon	Icon, das auf der Schaltfl�che angezeigt werden soll. (Kann <code>null</code> sein.)
	 * @return	Objekt der neuen Schaltfl�che
	 */
	public JButton addBigButton(final String title, final String tooltip, final Icon icon) {
		if (currentSection==null) return null;
		return currentSection.addBigButton(title,tooltip,icon);
	}

	/**
	 * F�gt eine Schaltfl�che, bei der sich das Icon links vom Text angezeigt wird, zu der aktuellen Sektion hinzu.
	 * @param title	Beschriftung der Schaltfl�che
	 * @param tooltip	Tooltip, der angezeigt werden sollen, wenn die Maus �ber die Schaltfl�che bewegt wird. (Kann <code>null</code> oder auch ein leerer String sein.)
	 * @param icon	Icon, das auf der Schaltfl�che angezeigt werden soll. (Kann <code>null</code> sein.)
	 * @return	Objekt der neuen Schaltfl�che
	 */
	public JButton addButtonURLIcon(final String title, final String tooltip, final URL icon) {
		if (currentSection==null) return null;
		return currentSection.addButtonURLIcon(title,tooltip,icon);
	}

	/**
	 * F�gt eine Schaltfl�che, bei der sich das Icon links vom Text angezeigt wird, zu der aktuellen Sektion hinzu.
	 * @param title	Beschriftung der Schaltfl�che
	 * @param tooltip	Tooltip, der angezeigt werden sollen, wenn die Maus �ber die Schaltfl�che bewegt wird. (Kann <code>null</code> oder auch ein leerer String sein.)
	 * @param icon	Icon, das auf der Schaltfl�che angezeigt werden soll. (Kann <code>null</code> sein.)
	 * @return	Objekt der neuen Schaltfl�che
	 */
	public JButton addButton(final String title, final String tooltip, final Icon icon) {
		if (currentSection==null) return null;
		return currentSection.addButton(title,tooltip,icon);
	}
}