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

import java.awt.Component;
import java.awt.Dimension;
import java.awt.event.ActionListener;
import java.io.Serializable;
import java.net.URL;

import javax.swing.Icon;
import javax.swing.ImageIcon;
import javax.swing.JTabbedPane;
import javax.swing.SwingUtilities;

/**
 * Diese Klasse implementiert einen Ribbon-basierenden Toolbar.
 * Der Toolbar besteht dabei, wie bei Ribbons üblich, aus mehreren Kategorien.
 * Daher leitet sich die Klasse von <code>JTabbedPane</code> ab.
 * @author Alexander Herzog
 * @version 1.0
 */
class JRibbonBar extends JTabbedPane {
	/**
	 * Serialisierungs-ID der Klasse
	 * @see Serializable
	 */
	private static final long serialVersionUID = 8136795265917207807L;

	/**
	 * <code>ActionListener</code> der für alle Buttons verwendet werden soll (kann <code>null</code> sein).
	 */
	public final ActionListener defaultActionListener;

	/**
	 * Konstruktor der Klasse <code>JRibbonBar</code>
	 * @param defaultActionListener	<code>ActionListener</code> der für alle Buttons verwendet werden soll (kann <code>null</code> sein).
	 */
	public JRibbonBar(final ActionListener defaultActionListener) {
		super();
		this.defaultActionListener=defaultActionListener;
		SwingUtilities.invokeLater(new Runnable() {
			@Override
			public void run() {
				updateSizes();
			}
		});
	}

	/**
	 * Konstruktor der Klasse <code>JRibbonBar</code>
	 */
	public JRibbonBar() {
		this(null);
	}

	/**
	 * Fügt einen Tab zu dem Ribbon-Toolbar hinz.
	 * @param title	Titel des Tabs.
	 * @param tooltip	Tooltip, der angezeigt werden sollen, wenn die Maus über den Tab-Titel bewegt wird. (Kann <code>null</code> oder auch ein leerer String sein.)
	 * @param icon	Icon, das in dem Tab angezeigt werden soll. (Kann <code>null</code> sein.)
	 * @return	Liefert das Objekt, welches die Elemente des Ribbons aufnehmen soll, zurück.
	 */
	public JRibbonTab addRibbonURLIcon(final String title, final String tooltip, final URL icon) {
		ImageIcon imageIcon=(icon==null)?null:new ImageIcon(icon);
		return addRibbon(title,tooltip,imageIcon);
	}

	/**
	 * Fügt einen Tab zu dem Ribbon-Toolbar hinz.
	 * @param title	Titel des Tabs.
	 * @param tooltip	Tooltip, der angezeigt werden sollen, wenn die Maus über den Tab-Titel bewegt wird. (Kann <code>null</code> oder auch ein leerer String sein.)
	 * @param icon	Icon, das in dem Tab angezeigt werden soll. (Kann <code>null</code> sein.)
	 * @return	Liefert das Objekt, welches die Elemente des Ribbons aufnehmen soll, zurück.
	 */
	public JRibbonTab addRibbon(final String title, final String tooltip, final Icon icon) {
		JRibbonTab component=new JRibbonTab(defaultActionListener);
		addTab(title,icon,component,tooltip);
		return component;
	}

	/**
	 * Fügt einen Tab zu dem Ribbon-Toolbar hinz.
	 * @param title	Titel des Tabs.
	 * @param tooltip	Tooltip, der angezeigt werden sollen, wenn die Maus über den Tab-Titel bewegt wird. (Kann <code>null</code> oder auch ein leerer String sein.)
	 * @return	Liefert das Objekt, welches die Elemente des Ribbons aufnehmen soll, zurück.
	 */
	public JRibbonTab addRibbon(final String title, final String tooltip) {
		return addRibbon(title,tooltip,null);
	}

	/**
	 * Werden nach dem initialen Zeichnen des Ribbon-Toolsbar später weitere Buttons hinzugefügt, so dass
	 * evtl. global die Höhe des Bars geändert werden muss, so muss diese Methode aufgerufen werden.
	 */
	public void updateSizes() {
		int maximumHeight=0;

		for (Component component : getComponents()) if (component instanceof JRibbonTab) {
			JRibbonTab ribbon=(JRibbonTab)component;
			for (Component section : ribbon.getComponents()) {
				maximumHeight=Math.max(maximumHeight,section.getSize().height);
			}
		}

		for (Component component : getComponents()) if (component instanceof JRibbonTab) {
			JRibbonTab ribbon=(JRibbonTab)component;
			for (Component section : ribbon.getComponents()) {
				Dimension d=new Dimension(section.getSize().width,maximumHeight);
				section.setSize(d);
				section.setMinimumSize(d);
				section.setMaximumSize(d);
				section.setPreferredSize(d);
			}
			Dimension d=new Dimension(ribbon.getSize().width,maximumHeight);
			ribbon.setSize(d);
			ribbon.setMinimumSize(d);
			ribbon.setMaximumSize(d);
			ribbon.setPreferredSize(d);
		}
	}
}