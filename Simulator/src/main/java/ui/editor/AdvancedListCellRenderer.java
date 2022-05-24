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

import java.awt.Component;
import java.io.Serializable;

import javax.swing.BorderFactory;
import javax.swing.DefaultListCellRenderer;
import javax.swing.Icon;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.ListCellRenderer;

import language.Language;

/**
 * {@link ListCellRenderer}-Erweiterung, die es erlaubt, einen html-String anzuzeigen.
 * @author Alexander Herzog
 * @version 1.0
 * @see DefaultListCellRenderer
 */
public class AdvancedListCellRenderer extends DefaultListCellRenderer {
	/**
	 * Konstruktor der Klasse
	 */
	public AdvancedListCellRenderer() {
		/*
		 * Wird nur benötigt, um einen JavaDoc-Kommentar für diesen (impliziten) Konstruktor
		 * setzen zu können, damit der JavaDoc-Compiler keine Warnung mehr ausgibt.
		 */
	}

	/**
	 * Serialisierungs-ID der Klasse
	 * @see Serializable
	 */
	private static final long serialVersionUID = 6246581092762257134L;

	/**
	 * Fügt einen Zeilenumbruch zur Ausgabe hinzu.
	 * @param output	Ausgabe
	 */
	protected static final void addBr(final StringBuilder output) {
		output.append("<br>");
	}

	/**
	 * Fügt einen fett gedruckten Text zur Ausgabe hinzu.
	 * @param output	Ausgabe
	 * @param value	Auszugebender Text
	 */
	protected static final void addB(final StringBuilder output, final String value) {
		output.append("<b>"+value+"</b>");
	}

	/**
	 * Fügt einen Text in einer bestimmten Farbe zur Ausgabe hinzu.
	 * @param output	Ausgabe
	 * @param value	Auszugebender Text
	 * @param color	html-Farbname
	 */
	protected static final void addColor(StringBuilder output, String value, String color) {
		output.append("<font color=\""+color+"\">"+value+"</font>");
	}

	/**
	 * Fügt den Namen einer Gruppe (Kunden, Callcenter, Agenten, ...) zur Ausgabe hinzu.
	 * @param output	Ausgabe
	 * @param name	Name der Gruppe
	 * @param active	Ist diese aktiv?
	 */
	protected static final void addName(final StringBuilder output, final String name, final boolean active) {
		addB(output,name+" ");
		if (!active) addColor(output,"("+Language.tr("Dialog.deactivated.lower")+")","red");
	}

	/**
	 * Erstellt die vollständige Zeichenkette für den gewählten Eintrag
	 * @param value	Gewählter Eintrag
	 * @param index	Index des Eintrags
	 * @param output	Ausgabe
	 */
	protected void buildString(Object value, int index, StringBuilder output) {
	}

	/**
	 * Liefert ein Icon zu dem gewählten Eintrag
	 * @param value	Gewählter Eintrag
	 * @return	Icon oder <code>null</code>, wenn kein Icon ausgegeben werden soll
	 */
	protected Icon getIcon(Object value) {
		return null;
	}

	/* (non-Javadoc)
	 * @see javax.swing.ListCellRenderer#getListCellRendererComponent(javax.swing.JList, java.lang.Object, int, boolean, boolean)
	 */
	@Override
	public Component getListCellRendererComponent(JList<?> list, Object value, int index, boolean isSelected, boolean cellHasFocus)
	{
		JLabel k=(JLabel)super.getListCellRendererComponent(list,value,index,isSelected,cellHasFocus);
		StringBuilder s=new StringBuilder();
		s.append("<html><body>");
		buildString(value,index, s);
		addBr(s);
		s.append("&nbsp;");
		s.append("</body></html>");
		k.setText(s.toString());

		final Icon icon=getIcon(value);
		if (icon!=null) k.setIcon(icon);

		k.setBorder(BorderFactory.createEmptyBorder(0,5,0,0));

		return k;
	}
}
