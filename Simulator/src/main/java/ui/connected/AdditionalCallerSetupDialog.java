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
package ui.connected;

import java.awt.FlowLayout;
import java.awt.GridLayout;
import java.awt.Window;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextField;

import language.Language;
import mathtools.NumberTools;
import systemtools.MsgBox;
import ui.editor.BaseEditDialog;
import ui.model.CallcenterModel;

/**
 * Erlaubt das Bearbeiten der Liste, wie viele zusätzliche Erstanrufer bei einer verketteten
 * Simulation mit in das System eingespielt werden sollen.
 * @author Alexander Herzog
 * @version 1.0
 */
public class AdditionalCallerSetupDialog extends BaseEditDialog {
	/**
	 * Serialisierungs-ID der Klasse
	 * @see Serializable
	 */
	private static final long serialVersionUID = -140281987544275534L;

	/** Liste mit den Name der Kundentypen, für die zusätzliche Kunden eingespielt werden sollen */
	private final List<String> callerNames;
	/** Liste mit den Anzahlen der zusätzlichen Kunden je Kundentyp */
	private final List<Integer> callerCount;
	/** Eingabefelder für die Anzahlen an Erstanrufern */
	private JTextField[] callerEdit;

	/**
	 * Konstruktor der Klasse <code>AdditionalCallerSetupDialog</code>
	 * @param owner	Elternfenster
	 * @param helpCallback	Callback, um die Online-Hilfe als modales Fenster aufzurufen.
	 * @param model	Callcenter-Modell, dem die Kundentypen-Namen entnommen werden sollen
	 * @param callerNames	Liste mit den Name der Kundentypen, für die zusätzliche Kunden eingespielt werden sollen
	 * @param callerCount	Liste mit den Anzahlen der zusätzlichen Kunden je Kundentyp
	 */
	public AdditionalCallerSetupDialog(Window owner, Runnable helpCallback, CallcenterModel model, List<String> callerNames, List<Integer> callerCount) {
		super(owner,Language.tr("Connected.Additional.Title"),getCallerNames(model),false,helpCallback);
		this.callerNames=new ArrayList<>(callerNames);
		this.callerCount=new ArrayList<>(callerCount);
		createSimpleGUI(400,500,null,null);
	}

	/**
	 * Liefert die Namen der Anrufergruppen in dem Modell
	 * @param model	Modell dem die Daten entnommen werden sollen
	 * @return	Namen der Anrufergruppen
	 */
	private static String[] getCallerNames(CallcenterModel model) {
		List<String> names=new ArrayList<>();
		for (int i=0;i<model.caller.size();i++) names.add(model.caller.get(i).name);
		return names.toArray(new String[0]);
	}

	@Override
	protected void createSimpleContent(JPanel content) {
		JPanel p=new JPanel(new GridLayout(callerTypeNames.length,2));
		callerEdit=new JTextField[callerTypeNames.length];
		for (int i=0;i<callerTypeNames.length;i++) {
			String s=callerTypeNames[i];
			JLabel l=new JLabel(s);
			JPanel p2=new JPanel(new FlowLayout(FlowLayout.LEFT));
			p2.add(l);
			p.add(p2);
			callerEdit[i]=new JTextField(10);
			int count=0;
			for (int j=0;j<callerNames.size();j++) if (callerNames.get(j).equalsIgnoreCase(s)) {count=callerCount.get(j); break;}
			callerEdit[i].setText(""+count);
			callerEdit[i].setEnabled(!readOnly);
			p2=new JPanel(new FlowLayout(FlowLayout.LEFT));
			p2.add(callerEdit[i]);
			p.add(p2);
		}
		JPanel p2=new JPanel();
		JScrollPane scroll=new JScrollPane(p2);
		p2.setLayout(new BoxLayout(p2,BoxLayout.PAGE_AXIS));
		p2.add(p);
		p2.add(Box.createVerticalGlue());
		scroll.setColumnHeaderView(new JLabel("<html><b>"+Language.tr("Connected.Additional.FreshCalls")+"</b></html>"));
		scroll.setBorder(BorderFactory.createEmptyBorder(0,5,0,0));
		content.add(scroll);
	}

	@Override
	protected boolean checkData() {
		for (int i=0;i<callerEdit.length;i++) {
			Integer I=NumberTools.getNotNegativeInteger(callerEdit[i],true);
			if (I==null) {
				MsgBox.error(this,Language.tr("Connected.Additional.FreshCalls.InvalidTitle"),String.format(Language.tr("Connected.Additional.FreshCalls.InvalidInfo"),callerTypeNames[i],callerEdit[i].getText()));
				return false;
			}
		}
		return true;
	}

	@Override
	protected void storeData() {
		callerNames.clear();
		callerCount.clear();
		for (int i=0;i<callerEdit.length;i++) {
			Integer I=NumberTools.getNotNegativeInteger(callerEdit[i],true);
			callerNames.add(callerTypeNames[i]);
			callerCount.add(I);
		}
	}

	/**
	 * Liefert nach dem Schließen des Dialogs eine Liste der Namen der Kundentypen, für die zusätzliche Kunden eingespielt werden sollen
	 * @return	Liste mit Namen von Kundentypen
	 */
	public List<String> getCallerNames() {
		return callerNames;
	}

	/**
	 * Liefert nach dem Schließen des Dialogs eine Liste der Anzahen der zusätzlichen Kunden je Kundentyp
	 * @return	List mit den Anzahlen der zusätzlichen Kunden je Kundentyp
	 */
	public List<Integer> getCallerCount() {
		return callerCount;
	}
}
