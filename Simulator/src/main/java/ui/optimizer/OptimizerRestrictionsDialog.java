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
package ui.optimizer;

import java.awt.BorderLayout;
import java.awt.FlowLayout;
import java.awt.GridLayout;
import java.awt.Window;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.ArrayList;
import java.util.List;

import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JMenuItem;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JPopupMenu;
import javax.swing.JScrollPane;
import javax.swing.JTree;
import javax.swing.event.TreeSelectionEvent;
import javax.swing.event.TreeSelectionListener;
import javax.swing.tree.DefaultMutableTreeNode;
import javax.swing.tree.DefaultTreeModel;
import javax.swing.tree.TreePath;

import language.Language;
import mathtools.NumberTools;
import mathtools.distribution.DataDistributionImpl;
import mathtools.distribution.swing.JDataDistributionEditPanel;
import systemtools.MsgBox;
import ui.editor.BaseEditDialog;
import ui.images.Images;
import ui.model.CallcenterModel;
import ui.model.CallcenterModelCallcenter;

/**
 * Dialog zur Bearbeitung von Einschränkungen bzgl. der Agentenanzahl pro Gruppe und Halbstundenintervall bei der Optimierung
 * @author Alexander Herzog
 * @version 1.0
 */
public class OptimizerRestrictionsDialog extends BaseEditDialog {
	private static final long serialVersionUID = -6845756275945658029L;

	/** Einträge */
	public final List<String> names;
	/** Minimalwert pro Eintrag und Intervall */
	public final List<DataDistributionImpl> min;
	/** Maximalwert pro Eintrag und Intervall */
	public final List<DataDistributionImpl> max;

	private final CallcenterModel editModel;

	private JTree tree;
	private JPanel editArea;
	private InfoObject lastSelected;
	private JCheckBox active;
	private JButton tools;
	private JPopupMenu toolsMenu;
	private JMenuItem toolsMenuMin, toolsMenuMax;
	private JDataDistributionEditPanel minEdit;
	private JDataDistributionEditPanel maxEdit;

	private boolean treeSelectionChange=false;

	/**
	 * Konstruktor der Klasse
	 * @param owner	Übergeordnetes Fenster
	 * @param helpCallbackModal	Hilfe-Callback
	 * @param editModel	Ausgangs-Callcenter-Modell
	 * @param names	Liste der Einträge
	 * @param min	Initiale Minimalwerte pro Eintrag und Intervall
	 * @param max	Initiale Maximalwerte pro Eintrag und Intervall
	 */
	public OptimizerRestrictionsDialog(final Window owner, final Runnable helpCallbackModal, final CallcenterModel editModel, final List<String> names, final List<DataDistributionImpl> min, final List<DataDistributionImpl> max) {
		super(owner,Language.tr("Optimizer.ControlVariable.Restrictions.Dialog.Title"),false,helpCallbackModal);
		this.editModel=editModel;
		this.names=new ArrayList<String>();
		this.names.addAll(names);
		this.min=new ArrayList<DataDistributionImpl>();
		for (int i=0;i<min.size();i++) this.min.add(min.get(i).clone());
		this.max=new ArrayList<DataDistributionImpl>();
		for (int i=0;i<max.size();i++) this.max.add(max.get(i).clone());

		lastSelected=null;
		createSimpleGUI(900,750,null,null);
	}

	@Override
	protected void createSimpleContent(JPanel content) {
		content.setLayout(new BorderLayout());

		/* Baumstruktur links */

		DefaultMutableTreeNode treeRoot=new DefaultMutableTreeNode();
		DefaultTreeModel treeModel=new DefaultTreeModel(treeRoot);
		content.add(new JScrollPane(tree=new JTree(treeModel){
			private static final long serialVersionUID = 2220382012040310176L;
			@Override protected void setExpandedState(TreePath path, boolean state) {if (state) super.setExpandedState(path, state);}
		}),BorderLayout.WEST);
		tree.addTreeSelectionListener(new SelectionListener());
		tree.setRootVisible(false);

		DefaultMutableTreeNode node;
		for (int i=0;i<editModel.callcenter.size();i++) {
			CallcenterModelCallcenter callcenter=editModel.callcenter.get(i);
			if (!callcenter.active) continue;
			node=null;
			for (int j=0;j<callcenter.agents.size();j++) {
				if (!callcenter.agents.get(j).active) continue;
				if (node==null) treeRoot.add(node=new DefaultMutableTreeNode(new InfoObject(callcenter.name,0)));
				node.add(new DefaultMutableTreeNode(new InfoObject(callcenter.name,j+1)));
			}
		}
		treeModel.reload();
		for (int i=0;i<tree.getRowCount();i++) tree.expandRow(i);

		/* Editor auf der rechten Seite */

		editArea=new JPanel(new BorderLayout()); content.add(editArea,BorderLayout.CENTER);
		JPanel p2=new JPanel(new BorderLayout()); editArea.add(p2,BorderLayout.NORTH);
		JPanel p3=new JPanel(new FlowLayout(FlowLayout.LEFT)); p2.add(p3,BorderLayout.CENTER);
		p3.add(active=new JCheckBox(Language.tr("Optimizer.ControlVariable.Restrictions.Dialog.Active")));
		p3=new JPanel(new FlowLayout(FlowLayout.LEFT)); p2.add(p3,BorderLayout.EAST);
		p3.add(tools=new JButton(Language.tr("Dialog.Button.Tools")));
		tools.addActionListener(new DialogListener());
		tools.setIcon(Images.GENERAL_SETUP.getIcon());
		editArea.add(p2=new JPanel(),BorderLayout.CENTER);
		p2.setLayout(new GridLayout(2,1));
		p2.add(minEdit=new JDataDistributionEditPanel(null,JDataDistributionEditPanel.PlotMode.PLOT_DENSITY,true,1));
		minEdit.addChangeListener(new DialogListener());
		p2.add(maxEdit=new JDataDistributionEditPanel(null,JDataDistributionEditPanel.PlotMode.PLOT_DENSITY,true,1));
		maxEdit.addChangeListener(new DialogListener());

		/* Tools-Popup-Menü */

		toolsMenu=new JPopupMenu();
		toolsMenu.add(toolsMenuMin=new JMenuItem(Language.tr("Optimizer.ControlVariable.Restrictions.Dialog.SetMin")));
		toolsMenuMin.addActionListener(new DialogListener());
		toolsMenuMin.setIcon(Images.EDIT_DELETE.getIcon());
		toolsMenu.add(toolsMenuMax=new JMenuItem(Language.tr("Optimizer.ControlVariable.Restrictions.Dialog.SetMax")));
		toolsMenuMax.addActionListener(new DialogListener());
		toolsMenuMax.setIcon(Images.EDIT_ADD.getIcon());

		/* Erstes Element im Baum auswählen */

		editArea.setVisible(false);
		tree.setSelectionRow(0);
	}

	@Override
	protected void storeData() {
		tree.setSelectionRow(0);
	}

	private class InfoObject {
		public final String callcenter;
		public final int groupNr;

		public InfoObject (String callcenter, int groupNr) {
			this.callcenter=callcenter;
			this.groupNr=groupNr;
		}

		@Override
		public String toString() {
			if (groupNr<=0) return callcenter;
			return Language.tr("Optimizer.AgentGroup")+" "+groupNr;
		}

		public String toDataString() {
			return ""+groupNr+"-"+callcenter;
		}
	}

	private class SelectionListener implements TreeSelectionListener {
		@Override
		public void valueChanged(TreeSelectionEvent e) {
			DefaultMutableTreeNode node=(DefaultMutableTreeNode)tree.getLastSelectedPathComponent();
			InfoObject selected=(node==null)?null:(InfoObject)node.getUserObject();
			if (selected!=null && selected.groupNr<=0) selected=null;
			if (selected==lastSelected) return;

			treeSelectionChange=true;
			try {

				if (lastSelected!=null) {
					boolean useRestrictions=active.isSelected();
					DataDistributionImpl minDist=minEdit.getDistribution();
					DataDistributionImpl maxDist=maxEdit.getDistribution();
					int index=names.indexOf(lastSelected.toDataString());
					if (index>=0) {
						if (useRestrictions) {
							min.set(index,minDist);
							max.set(index,maxDist);
						} else {
							names.remove(index);
							min.remove(index);
							max.remove(index);
						}
					} else {
						if (useRestrictions) {
							names.add(lastSelected.toDataString());
							min.add(minDist);
							max.add(maxDist);
						}
					}
				}

				lastSelected=selected;

				editArea.setVisible(selected!=null);
				if (selected!=null) {
					int index=names.indexOf(lastSelected.toDataString());
					boolean useRestrictions=false;
					DataDistributionImpl minDist;
					DataDistributionImpl maxDist;
					if (index==-1) {
						minDist=new DataDistributionImpl(86399,48);
						minDist.setToValue(0);
						maxDist=new DataDistributionImpl(86399,48);
						maxDist.setToValue(1000);
					} else {
						useRestrictions=true;
						minDist=min.get(index);
						maxDist=max.get(index);
					}
					active.setSelected(useRestrictions);
					minEdit.setDistribution(minDist);
					maxEdit.setDistribution(maxDist);
				}
			} finally {
				treeSelectionChange=false;
			}
		}
	}

	private class DialogListener implements ActionListener {
		@Override
		public void actionPerformed(ActionEvent e) {

			if (e.getSource()==minEdit || e.getSource()==maxEdit) {
				if (treeSelectionChange) return;
				active.setSelected(true);
				return;
			}

			if (e.getSource()==tools) {
				toolsMenu.show(tools,tools.getX(),tools.getY()+tools.getHeight());
				return;
			}

			if (e.getSource()==toolsMenuMin) {
				String value="0";
				int newValue=0;
				while (true) {
					value=JOptionPane.showInputDialog(OptimizerRestrictionsDialog.this,Language.tr("Optimizer.ControlVariable.Restrictions.Dialog.SetMin.Title"),value);
					if (value==null) return;
					Integer val=NumberTools.getNotNegativeInteger(value);
					if (val!=null) {newValue=val; break;}
					MsgBox.error(OptimizerRestrictionsDialog.this,Language.tr("Optimizer.ControlVariable.Restrictions.Dialog.SetMin.ErrorTitle"),Language.tr("Optimizer.ControlVariable.Restrictions.Dialog.SetMin.ErrorSubTitle"));
				}
				DataDistributionImpl dist=new DataDistributionImpl(86399,48);
				dist.setToValue(newValue);
				minEdit.setDistribution(dist);
				active.setSelected(true);
				return;
			}

			if (e.getSource()==toolsMenuMax) {
				String value="1000";
				int newValue=0;
				while (true) {
					value=JOptionPane.showInputDialog(OptimizerRestrictionsDialog.this,Language.tr("Optimizer.ControlVariable.Restrictions.Dialog.SetMax.Title"),value);
					if (value==null) return;
					Integer val=NumberTools.getNotNegativeInteger(value);
					if (val!=null) {newValue=val; break;}
					MsgBox.error(OptimizerRestrictionsDialog.this,Language.tr("Optimizer.ControlVariable.Restrictions.Dialog.SetMax.ErrorTitle"),Language.tr("Optimizer.ControlVariable.Restrictions.Dialog.SetMax.ErrorSubTitle"));
				}
				DataDistributionImpl dist=new DataDistributionImpl(86399,48);
				dist.setToValue(newValue);
				maxEdit.setDistribution(dist);
				active.setSelected(true);
				return;
			}

		}
	}
}
