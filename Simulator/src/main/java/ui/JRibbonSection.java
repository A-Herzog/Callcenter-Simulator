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

import java.awt.Color;
import java.awt.FlowLayout;
import java.awt.event.ActionListener;
import java.io.Serializable;
import java.net.URL;

import javax.swing.BorderFactory;
import javax.swing.Icon;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JPanel;
import javax.swing.JToolBar;
import javax.swing.SwingConstants;
import javax.swing.border.TitledBorder;

/**
 * Diese Klasse stellt einen Abschnitt innerhalb eines <code>JRibbonTab</code>-Ribbons dar.
 * @author Alexander Herzog
 * @version 1.0
 * @see JRibbonTab
 * @see JRibbonBar
 */
class JRibbonSection extends JPanel {
	/**
	 * Serialisierungs-ID der Klasse
	 * @see Serializable
	 */
	private static final long serialVersionUID = -3037779257349754703L;

	private JToolBar toolbar;

	/**
	 * <code>ActionListener</code> der für alle Buttons verwendet werden soll (kann <code>null</code> sein).
	 */
	public final ActionListener defaultActionListener;

	/**
	 * Konstruktor der Klasse <code>JRibbonSection</code>
	 * @param title	Unter den Buttons der Sektion anzuzeigende Beschreibung
	 * @param defaultActionListener	<code>ActionListener</code> der für alle Buttons verwendet werden soll (kann <code>null</code> sein).
	 */
	public JRibbonSection(final String title, final ActionListener defaultActionListener) {
		super();
		this.defaultActionListener=defaultActionListener;
		setLayout(new FlowLayout(FlowLayout.LEFT,0,0));
		setBorder(BorderFactory.createTitledBorder(BorderFactory.createMatteBorder(0,0,0,1,Color.LIGHT_GRAY),title,TitledBorder.CENTER,TitledBorder.ABOVE_BOTTOM,null,Color.GRAY));
		addRow();
	}

	/**
	 * Konstruktor der Klasse <code>JRibbonSection</code>
	 * @param title	Unter den Buttons der Sektion anzuzeigende Beschreibung
	 */
	public JRibbonSection(final String title) {
		this(title,null);
	}

	/**
	 * Fügt zu der Sektion eine neue vertikale Spalte hinzu.
	 * Nach dem Erstellen einer Sektion ist sofort eine erste vertikale Spalte vorhanden.
	 */
	public void addRow() {
		toolbar=new JToolBar(SwingConstants.VERTICAL);
		toolbar.setFloatable(false);
		add(toolbar);
	}

	/**
	 * Fügt eine Schaltfläche, bei der sich das Icon über dem Text befindet, zu der Sektion hinzu.
	 * @param title	Beschriftung der Schaltfläche
	 * @param tooltip	Tooltip, der angezeigt werden sollen, wenn die Maus über die Schaltfläche bewegt wird. (Kann <code>null</code> oder auch ein leerer String sein.)
	 * @param icon	Icon, das auf der Schaltfläche angezeigt werden soll. (Kann <code>null</code> sein.)
	 * @return	Objekt der neuen Schaltfläche
	 */
	public JButton addBigButtonURLIcon(final String title, final String tooltip, final URL icon) {
		ImageIcon imageIcon=(icon==null)?null:new ImageIcon(icon);
		return addBigButton(title,tooltip,imageIcon);
	}

	/**
	 * Fügt eine Schaltfläche, bei der sich das Icon über dem Text befindet, zu der Sektion hinzu.
	 * @param title	Beschriftung der Schaltfläche
	 * @param tooltip	Tooltip, der angezeigt werden sollen, wenn die Maus über die Schaltfläche bewegt wird. (Kann <code>null</code> oder auch ein leerer String sein.)
	 * @param icon	Icon, das auf der Schaltfläche angezeigt werden soll. (Kann <code>null</code> sein.)
	 * @return	Objekt der neuen Schaltfläche
	 */
	public JButton addBigButton(final String title, final String tooltip, final Icon icon) {
		JButton button=new JButton(title);
		if (tooltip!=null && !tooltip.isEmpty()) button.setToolTipText(tooltip);
		if (icon!=null) button.setIcon(icon);
		button.setVerticalTextPosition(SwingConstants.BOTTOM);
		button.setHorizontalTextPosition(SwingConstants.CENTER);
		if (defaultActionListener!=null) button.addActionListener(defaultActionListener);
		toolbar.add(button);
		return button;
	}

	/**
	 * Fügt eine Schaltfläche, bei der sich das Icon links vom Text angezeigt wird, zu der Sektion hinzu.
	 * @param title	Beschriftung der Schaltfläche
	 * @param tooltip	Tooltip, der angezeigt werden sollen, wenn die Maus über die Schaltfläche bewegt wird. (Kann <code>null</code> oder auch ein leerer String sein.)
	 * @param icon	Icon, das auf der Schaltfläche angezeigt werden soll. (Kann <code>null</code> sein.)
	 * @return	Objekt der neuen Schaltfläche
	 */
	public JButton addButtonURLIcon(final String title, final String tooltip, final URL icon) {
		ImageIcon imageIcon=(icon==null)?null:new ImageIcon(icon);
		return addButton(title,tooltip,imageIcon);
	}

	/**
	 * Fügt eine Schaltfläche, bei der sich das Icon links vom Text angezeigt wird, zu der Sektion hinzu.
	 * @param title	Beschriftung der Schaltfläche
	 * @param tooltip	Tooltip, der angezeigt werden sollen, wenn die Maus über die Schaltfläche bewegt wird. (Kann <code>null</code> oder auch ein leerer String sein.)
	 * @param icon	Icon, das auf der Schaltfläche angezeigt werden soll. (Kann <code>null</code> sein.)
	 * @return	Objekt der neuen Schaltfläche
	 */
	public JButton addButton(final String title, final String tooltip, final Icon icon) {
		JButton button=new JButton(title);
		if (tooltip!=null && !tooltip.isEmpty()) button.setToolTipText(tooltip);
		if (icon!=null) button.setIcon(icon);
		if (defaultActionListener!=null) button.addActionListener(defaultActionListener);
		toolbar.add(button);
		return button;
	}
}