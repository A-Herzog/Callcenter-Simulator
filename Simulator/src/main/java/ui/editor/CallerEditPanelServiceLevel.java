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
import java.awt.event.KeyEvent;
import java.io.Serializable;

import javax.swing.BoxLayout;
import javax.swing.ButtonGroup;
import javax.swing.Icon;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JRadioButton;
import javax.swing.JTextField;

import language.Language;
import mathtools.NumberTools;
import ui.images.Images;
import ui.model.CallcenterModelCaller;

/**
 * In diesem Panel kann der für diese Kundengruppe zu veranschlagende Service-Level-Wert eingestellt werden.
 * @author Alexander Herzog
 * @see CallerEditDialog
 */
public class CallerEditPanelServiceLevel extends CallerEditPanel {
	/**
	 * Serialisierungs-ID der Klasse
	 * @see Serializable
	 */
	private static final long serialVersionUID = 2648513300864319341L;

	/** Option: Globale Service-Level Vorgabe verwenden */
	private JRadioButton serviceLevelGlobal;
	/** Option: Service-Level lokal für diesen Kundentyp einstellen */
	private JRadioButton serviceLevelLocal;
	/** Eingabefeld für den lokalen Service-Level */
	private final JTextField serviceLevelEdit;

	/**
	 * Konstruktor der Klasse
	 * @param initData	Informationen über den aktuellen Kundentyp zur Initialisierung des Panels
	 * @see CallerEditPanel.InitData
	 */
	public CallerEditPanelServiceLevel(final InitData initData) {
		super(initData);

		JPanel p2, p3;

		setLayout(new BorderLayout());

		add(p2=new JPanel());
		p2.setLayout(new BoxLayout(p2,BoxLayout.Y_AXIS));
		p2.add(p3=new JPanel(new FlowLayout(FlowLayout.LEFT)));
		p3.add(serviceLevelGlobal=new JRadioButton(String.format(Language.tr("Editor.Caller.ServiceLevel.Global"),""+serviceLevelModel)));
		p2.add(p3=new JPanel(new FlowLayout(FlowLayout.LEFT)));
		p3.add(serviceLevelLocal=new JRadioButton(Language.tr("Editor.Caller.ServiceLevel.Individual")));
		p2.add(p3=new JPanel(new FlowLayout(FlowLayout.LEFT)));
		serviceLevelEdit=addInputLine(p3,Language.tr("Editor.Caller.ServiceLevel.Individual.Info")+":",0);
		p2.add(p3=new JPanel(new FlowLayout(FlowLayout.LEFT)));
		p3.add(new JLabel("<html>"+Language.tr("Editor.Caller.ServiceLevel.LocalGlobalInfo").replaceAll("\n","<br>")+"</html>"));
		ButtonGroup group=new ButtonGroup();
		group.add(serviceLevelGlobal);
		group.add(serviceLevelLocal);
		serviceLevelGlobal.setSelected(caller.serviceLevelSeconds<=0);
		serviceLevelLocal.setSelected(caller.serviceLevelSeconds>0);
		serviceLevelEdit.setText((caller.serviceLevelSeconds>0)?(""+caller.serviceLevelSeconds):(""+serviceLevelModel));
		serviceLevelEdit.setEnabled(!readOnly);
		serviceLevelEdit.addKeyListener(dialogElementListener);
	}

	@Override
	public String[] check(KeyEvent e) {
		String[] result=null;

		if (serviceLevelLocal.isSelected()) {
			Integer I=NumberTools.getNotNegativeInteger(serviceLevelEdit,true);
			if (I==null || I<=0) {
				if (result==null) result=new String[]{Language.tr("Editor.Caller.Error.ServiceLevel.Title"),String.format(Language.tr("Editor.Caller.Error.ServiceLevel.Info"),serviceLevelEdit.getText())};
			}
		}

		if (e!=null && e.getSource()==serviceLevelEdit) {
			serviceLevelLocal.setSelected(true);
		}

		return result;
	}

	@Override
	public void writeToCaller(CallcenterModelCaller newCaller) {
		newCaller.serviceLevelSeconds=(short)((serviceLevelLocal.isSelected())?(int)NumberTools.getInteger(serviceLevelEdit,false):-1);
	}

	@Override
	public String getTabName() {
		return Language.tr("Editor.Caller.Tabs.ServiceLevel");
	}

	@Override
	public Icon getTabIconObject() {
		return Images.EDITOR_CALLER_PAGE_SERVICE_LEVEL.getIcon();
	}
}
