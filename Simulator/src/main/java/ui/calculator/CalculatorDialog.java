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
package ui.calculator;

import java.awt.Dimension;
import java.awt.Window;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.io.Serializable;

import javax.swing.BoxLayout;
import javax.swing.JPanel;
import javax.swing.JTextField;

import language.Language;
import mathtools.NumberTools;
import parser.CalcSystem;
import parser.MathCalcError;
import ui.HelpLink;
import ui.editor.BaseEditDialog;

/**
 * Einfacher Rechner-Dialog
 * @author Alexander Herzog
 * @version 1.0
 */
public class CalculatorDialog extends BaseEditDialog {
	/**
	 * Serialisierungs-ID der Klasse
	 * @see Serializable
	 */
	private static final long serialVersionUID = 8846361974707063923L;

	/** Eingabezeile */
	private JTextField input;
	/** Ergebnisausgabezeile */
	private JTextField output;

	/**
	 * Konstuktor der Klasse
	 * @param owner	�bergeordnetes Element
	 * @param helpLink	Hilfe-Link
	 */
	public CalculatorDialog(final Window owner, final HelpLink helpLink) {
		super(owner,Language.tr("Calculator.Title"),null,false,helpLink.dialogCalculator);
		showCloseButton=true;
		createSimpleGUI(300,300,null,null);
		pack();
		Dimension d=getSize(); d.width=Math.max(d.width,525); setSize(d);
	}

	@Override
	protected void createSimpleContent(JPanel content) {
		content.setLayout(new BoxLayout(content, BoxLayout.Y_AXIS));

		input=addVerticalInputLine(content,Language.tr("Calculator.Input"),"",60);
		input.addKeyListener(new InputListener());

		output=addVerticalInputLine(content,Language.tr("Calculator.Output"),"",60);
		output.setEditable(false);
	}

	/**
	 * Reagiert auf Eingaben in {@link CalculatorDialog#input}
	 * @see CalculatorDialog#input
	 */
	private class InputListener implements KeyListener {
		/**
		 * Rechensystem
		 */
		private final CalcSystem calc=new CalcSystem();

		/**
		 * Konstruktor der Klasse
		 */
		public InputListener() {
			/*
			 * Wird nur ben�tigt, um einen JavaDoc-Kommentar f�r diesen (impliziten) Konstruktor
			 * setzen zu k�nnen, damit der JavaDoc-Compiler keine Warnung mehr ausgibt.
			 */
		}

		/**
		 * Rechnung aktualisieren
		 */
		private void recalc() {
			input.getText();
			int pos=calc.parse(input.getText());
			String result;
			if (pos>=0) {
				result=String.format(Language.tr("Statistic.Filter.InvalidExpressionPosition"),pos+1);
			} else {
				try {
					result=NumberTools.formatNumberMax(calc.calc());
				} catch (MathCalcError e) {
					result=Language.tr("Statistic.Filter.InvalidExpression");
				}
			}
			output.setText(result);
		}

		@Override public void keyTyped(KeyEvent e) {}
		@Override public void keyPressed(KeyEvent e) {}
		@Override public void keyReleased(KeyEvent e) {recalc();}
	}
}
