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
import java.awt.FlowLayout;
import java.awt.GridLayout;
import java.awt.Window;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.io.Serializable;
import java.util.List;

import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.JCheckBox;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextField;

import language.Language;
import mathtools.NumberTools;

/**
 * In diesem Dialog können optionale Kundentyp-Änderungen bei
 * Anrufwiederholungen konfiguriert werden.
 * @author Alexander Herzog
 * @see CallerEditPanelRetryProbability
 */
public class CallerRetryChangeDialog extends BaseEditDialog {
	/**
	 * Serialisierungs-ID der Klasse
	 * @see Serializable
	 */
	private static final long serialVersionUID = 8911014010474864588L;

	/** Liste mit Namen der neuen Kundentypen */
	private final List<String> names;
	/** Liste mit den zu {@link #names} gehörenden Raten */
	private final List<Double> rates;

	/** Option: Kundentyp vor Wahlwiederholung gemäß folgenden Raten neu festlegen */
	private JCheckBox activeAdvancedMode;
	/** Eingabefelder mit den Raten mit denen sich der Kundentyp zu {@link #names} ändert */
	private final JTextField[] rateInput;

	/**
	 * Konstruktor der Klasse
	 * @param owner	Übergeordnetes Fenster
	 * @param names	Liste mit den Namen für mögliche Kundentyp-Änderungen
	 * @param rates	Raten mit denen die entsprechenden Kundentyp-Änderungen stattfinden
	 * @param readOnly	Nur-Lese-Status
	 * @param helpCallback	Hilfe-Callback
	 */
	public CallerRetryChangeDialog(final Window owner, final List<String> names, final List<Double> rates, final boolean readOnly, final Runnable helpCallback) {
		super(owner,Language.tr("Editor.Caller.RetryProbability.ClientTypeChange"),readOnly,helpCallback);
		this.names=names;
		this.rates=rates;
		rateInput=new JTextField[names.size()];
		createSimpleGUI(500,400,null,null);
	}

	@Override
	protected void createSimpleContent(JPanel content) {
		content.setLayout(new BorderLayout());

		JPanel p,p2;

		content.add(p=new JPanel(new FlowLayout(FlowLayout.LEFT)),BorderLayout.NORTH);
		p.add(activeAdvancedMode=new JCheckBox(Language.tr("Editor.Caller.RetryProbability.ClientTypeChange.Info")));
		activeAdvancedMode.setEnabled(!readOnly);
		double d=0; for (int i=0;i<rates.size();i++) d+=rates.get(i);
		activeAdvancedMode.setSelected(d>0);

		JScrollPane sp;
		content.add(sp=new JScrollPane(p2=new JPanel()),BorderLayout.CENTER);
		p2.setLayout(new BoxLayout(p2,BoxLayout.PAGE_AXIS));
		sp.setBorder(BorderFactory.createEmptyBorder(5,5,5,5));
		p2.add(p=new JPanel(new GridLayout(names.size()+1,2)));
		p2.add(Box.createVerticalGlue());

		p.add(new JLabel("<html><body><b>"+Language.tr("Editor.Caller.RetryProbability.ClientTypeChange.ClientType")+"</b></body></html>"));
		p.add(new JLabel("<html><body><b>"+Language.tr("Editor.Caller.RetryProbability.ClientTypeChange.Rate")+"</b></body></html>"));

		for (int i=0;i<names.size();i++) {
			p.add(new JLabel(names.get(i)));
			p.add(p2=new JPanel(new FlowLayout(FlowLayout.LEFT)));
			p2.add(rateInput[i]=new JTextField(15));
			rateInput[i].setEnabled(!readOnly);
			rateInput[i].setText(NumberTools.formatNumberMax(rates.get(i)));
			rateInput[i].addKeyListener(new RateInputListener());		}
	}

	/* (non-Javadoc)
	 * @see complexcallcenter.editor.BaseEditDialog#checkData()
	 */
	@Override
	protected boolean checkData() {
		if (!activeAdvancedMode.isSelected()) return true;
		for (int i=0;i<rateInput.length;i++) if (NumberTools.getNotNegativeDouble(rateInput[i],true)==null) return false;
		return true;
	}

	/* (non-Javadoc)
	 * @see complexcallcenter.editor.BaseEditDialog#storeData()
	 */
	@Override
	protected void storeData() {
		for (int i=0;i<rateInput.length;i++) rates.set(i,(activeAdvancedMode.isSelected())?NumberTools.getNotNegativeDouble(rateInput[i],true):0.0);
	}

	/**
	 * Reagiert auf Tastendrücke in {@link CallerRetryChangeDialog#rateInput}
	 * @see CallerRetryChangeDialog#rateInput
	 */
	private class RateInputListener implements KeyListener {
		/**
		 * Konstruktor der Klasse
		 */
		public RateInputListener() {
			/*
			 * Wird nur benötigt, um einen JavaDoc-Kommentar für diesen (impliziten) Konstruktor
			 * setzen zu können, damit der JavaDoc-Compiler keine Warnung mehr ausgibt.
			 */
		}

		@Override
		public void keyTyped(KeyEvent e) {
			activeAdvancedMode.setSelected(true); checkData();
		}

		@Override
		public void keyPressed(KeyEvent e) {
			activeAdvancedMode.setSelected(true); checkData();
		}

		@Override
		public void keyReleased(KeyEvent e) {
			activeAdvancedMode.setSelected(true); checkData();
		}
	}
}
