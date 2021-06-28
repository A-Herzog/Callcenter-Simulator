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

import java.awt.Color;
import java.awt.Component;
import java.awt.Font;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;
import java.awt.event.MouseEvent;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.EventObject;
import java.util.List;

import javax.swing.AbstractCellEditor;
import javax.swing.JCheckBox;
import javax.swing.JTree;
import javax.swing.UIManager;
import javax.swing.tree.DefaultMutableTreeNode;
import javax.swing.tree.DefaultTreeModel;
import javax.swing.tree.TreeCellEditor;
import javax.swing.tree.TreeCellRenderer;
import javax.swing.tree.TreePath;

import language.Language;
import ui.model.CallcenterModel;
import ui.model.CallcenterModelCallcenter;
import ui.model.CallcenterModelCaller;

/**
 * Diese Klasse kapselt einen {@link JTree}-Baum aus Checkboxen.
 * @author Alexander Herzog
 * @see JTree
 */
public class CheckBoxTree {
	/**
	 * Erstellte Baumstruktur
	 */
	public final JTree tree;

	/**
	 * Datenmodell für {@link #tree}
	 */
	private final DefaultTreeModel model;

	/**
	 * Wurzelelement der Baumstruktur
	 */
	public final DefaultMutableTreeNode root;

	/**
	 * Konstruktor der Klasse
	 */
	public CheckBoxTree() {
		root=new DefaultMutableTreeNode();
		model=new DefaultTreeModel(root);
		tree=new JTree(model);
		tree.setRootVisible(false);
		tree.setCellRenderer(new CheckBoxNodeRenderer());
		tree.setCellEditor(new CheckBoxNodeEditor(tree));
		tree.setEditable(true);
	}

	/**
	 * Erstellt einen neuen Checkbox-Eintrag zur Baumstruktur hinzu
	 * @param name	Beschriftung der Checkbox
	 * @return	Checkbox-Node zum Hinzufügen zur Baumstruktur
	 */
	public static CheckBoxNode newNode(final String name) {
		return new CheckBoxNode(name);
	}

	/**
	 * Erstellt einen neuen Checkbox-Eintrag zur Baumstruktur hinzu
	 * @param name	Beschriftung der Checkbox
	 * @param selected	Gibt an, ob die Checkbox initial selektiert sein soll
	 * @return	Checkbox-Node zum Hinzufügen zur Baumstruktur
	 */
	public static CheckBoxNode newNode(final String name, final boolean selected) {
		return new CheckBoxNode(name,selected);
	}

	/**
	 * Erstellt eine neue Struktur, die nur aus Checkboxen besteht
	 * @param names	Beschriftungen der Checkboxen
	 */
	public void addStrings(final String[] names) {
		root.removeAllChildren();
		if (names!=null) for (String name: names) root.add(new DefaultMutableTreeNode(newNode(name)));
		model.reload();
		expand();
	}

	/**
	 * Erstellt eine neue Struktur, die die Namen der aktiveren Anrufergruppen enthält
	 * @param model	Callcenter-Modell dem die Daten entnommen werden sollen
	 */
	public void addActiveCaller(final CallcenterModel model) {
		root.removeAllChildren();
		if (model!=null) for (CallcenterModelCaller caller: model.caller) if (caller.active) {
			root.add(new DefaultMutableTreeNode(newNode(caller.name)));
		}
		this.model.reload();
		expand();
	}

	/**
	 * Erstellt eine neue Struktur, die die Namen der aktiveren Callcenter enthält
	 * @param model	Callcenter-Modell dem die Daten entnommen werden sollen
	 */
	public void addActiveCallcenter(final CallcenterModel model) {
		root.removeAllChildren();
		if (model!=null) for (CallcenterModelCallcenter callcenter: model.callcenter) if (callcenter.active) {
			root.add(new DefaultMutableTreeNode(newNode(callcenter.name)));
		}
		this.model.reload();
		expand();
	}

	/**
	 * Erstellt eine neue Struktur, die die Namen der aktiveren Agentengruppen in ihren jeweiligen Callcentern enthält
	 * @param model	Callcenter-Modell dem die Daten entnommen werden sollen
	 */
	public void addActiveAgents(final CallcenterModel model) {
		root.removeAllChildren();
		if (model!=null) for (CallcenterModelCallcenter callcenter: model.callcenter) if (callcenter.active) {
			DefaultMutableTreeNode node=null;
			for (int i=0;i<callcenter.agents.size();i++) {
				if (!callcenter.agents.get(i).active) continue;
				if (node==null) root.add(node=new DefaultMutableTreeNode(newNode(callcenter.name)));
				node.add(new DefaultMutableTreeNode(newNode(Language.tr("Optimizer.AgentGroup")+" "+(i+1))));
			}
		}

		this.model.reload();
		expand();
	}

	/**
	 * Klappt den Baum vollständis aus.
	 */
	public void expand() {
		int row=0; while (row<tree.getRowCount()) {
			DefaultMutableTreeNode node=(DefaultMutableTreeNode)(tree.getPathForRow(row).getLastPathComponent());
			if (!node.isLeaf()) tree.expandRow(row);
			row++;
		}
	}

	/**
	 * Liefert die Namen der ausgewählten Eintrag
	 * @param agentsList	Handelt es sich um eine (verschachtelte) Agentenliste (<code>true</code>) oder um eine Kunden- oder Callcenterliste (<code>false</code>)
	 * @return	Namen der ausgewählten Eintrag
	 */
	public String[] getSelected(boolean agentsList) {
		List<String> l=new ArrayList<>();

		if (agentsList) {
			/* Agenten */
			for (int i=0;i<root.getChildCount();i++) {
				if (!(root.getChildAt(i) instanceof DefaultMutableTreeNode)) continue;
				DefaultMutableTreeNode node=(DefaultMutableTreeNode)root.getChildAt(i);
				if (!(node.getUserObject() instanceof CheckBoxTree.CheckBoxNode)) continue;
				String callcenter=((CheckBoxTree.CheckBoxNode)node.getUserObject()).getText();
				for (int j=0;j<node.getChildCount();j++) {
					if (!(node.getChildAt(j) instanceof DefaultMutableTreeNode)) continue;
					DefaultMutableTreeNode node2=(DefaultMutableTreeNode)node.getChildAt(j);
					if (!(node2.getUserObject() instanceof CheckBoxTree.CheckBoxNode)) continue;
					CheckBoxTree.CheckBoxNode checkbox=(CheckBoxTree.CheckBoxNode)node2.getUserObject();
					if (checkbox.isSelected()) l.add((j+1)+"-"+callcenter);
				}
			}
		} else {
			/* Kunden oder Callcenter */
			for (int i=0;i<root.getChildCount();i++) {
				if (!(root.getChildAt(i) instanceof DefaultMutableTreeNode)) continue;
				DefaultMutableTreeNode node=(DefaultMutableTreeNode)root.getChildAt(i);
				if (!(node.getUserObject() instanceof CheckBoxTree.CheckBoxNode)) continue;
				CheckBoxTree.CheckBoxNode checkbox=(CheckBoxTree.CheckBoxNode)node.getUserObject();
				if (checkbox.isSelected()) l.add(checkbox.getText());
			}
		}

		return l.toArray(new String[0]);
	}

	/**
	 * Einzelner Eintrag für den in {@link CheckBoxTree}
	 * erzeugten Baum.
	 * @author Alexander Herzog
	 * @see CheckBoxTree#newNode(String)
	 * @see CheckBoxTree#newNode(String, boolean)
	 */
	public static final class CheckBoxNode {
		/** Beschriftung der Checkbox */
		private final String text;
		/** Gibt an, ob die Checkbox selektiert sein soll */
		private boolean selected=true;

		/**
		 * Konstruktor der Klasse
		 * @param text	Beschriftung der Checkbox
		 * @param selected	Gibt an, ob die Checkbox initial selektiert sein soll
		 */
		public CheckBoxNode(final String text, final boolean selected) {
			this.text=text;
			this.selected=selected;
		}

		/**
		 * Konstruktor der Klasse
		 * @param text	Beschriftung der Checkbox
		 */
		public CheckBoxNode(String text) {
			this.text=text;
		}

		/**
		 * Gibt an, ob die Checkbox selektiert ist.
		 * @return	Liefert <code>true</code>, wenn die	Checkbox selektiert ist
		 */
		public boolean isSelected() {
			return selected;
		}

		/**
		 * Stellt ein, ob die Checkbox selektiert dargestellt werden soll
		 * @param newValue	Checkbox selektieren
		 */
		public void setSelected(boolean newValue) {
			selected=newValue;
		}

		/**
		 * Liefert den Text der Checkbox
		 * @return	Text der Checkbox
		 */
		public String getText() {
			return text;
		}

		@Override
		public String toString() {
			return text;
		}
	}

	/**
	 * Editor für die Einträge in {@link CheckBoxTree#tree}
	 */
	private final class CheckBoxNodeEditor extends AbstractCellEditor implements TreeCellEditor {
		/**
		 * Serialisierungs-ID der Klasse
		 * @see Serializable
		 */
		private static final long serialVersionUID = 3461005571727645288L;

		/**
		 * Basis-Renderer
		 */
		private final CheckBoxNodeRenderer renderer=new CheckBoxNodeRenderer();

		/**
		 * Zugehörige Baumstruktur
		 */
		private final JTree tree;

		/**
		 * Konstruktor der Klasse
		 * @param tree	Zugehörige Baumstruktur
		 */
		public CheckBoxNodeEditor(final JTree tree) {
			this.tree=tree;
		}

		@Override
		public Object getCellEditorValue() {
			final JCheckBox checkbox=renderer.getLeafRenderer();
			final CheckBoxNode checkBoxNode=new CheckBoxNode(checkbox.getText(),checkbox.isSelected());
			return checkBoxNode;
		}

		@Override
		public boolean isCellEditable(EventObject event) {
			if (!(event instanceof MouseEvent)) return false;
			MouseEvent mouseEvent=(MouseEvent)event;

			TreePath path=tree.getPathForLocation(mouseEvent.getX(),mouseEvent.getY());
			if (path==null) return false;

			Object node=path.getLastPathComponent();
			if (node==null || !(node instanceof DefaultMutableTreeNode)) return false;

			return (((DefaultMutableTreeNode)node).getUserObject() instanceof CheckBoxNode);
		}

		@Override
		public Component getTreeCellEditorComponent(JTree tree, Object value, boolean selected, boolean expanded, boolean leaf, int row) {
			Component editor=renderer.getTreeCellRendererComponent(tree,value,true,expanded,leaf,row,true);

			DefaultMutableTreeNode node=null;
			if (value instanceof DefaultMutableTreeNode) node=(DefaultMutableTreeNode)value;

			if (editor instanceof JCheckBox) {
				JCheckBox checkBox=(JCheckBox)editor;
				ItemListener[] l=checkBox.getItemListeners();
				for (int i=0;i<l.length;i++) if (l[i] instanceof CheckBoxItemListener) checkBox.removeItemListener(l[i]);
				checkBox.addItemListener(new CheckBoxItemListener(this,node,checkBox,tree));
			}

			return editor;
		}

		/**
		 * Wird von {@link CheckBoxItemListener#itemStateChanged(ItemEvent)}
		 * aufgerufen, wenn sich die Einstellung einer Checkbox verändert hat.
		 */
		public void publicFireEditingStopped() {
			fireEditingStopped();
		}
	}

	/**
	 * Reagiert auf Änderungen der Einstellungen der Checkboxen
	 * in {@link CheckBoxTree#tree}
	 */
	private static final class CheckBoxItemListener implements ItemListener {
		/**
		 * Editor für den Eintrag
		 */
		private final CheckBoxNodeEditor editor;

		/**
		 * Aktueller Eintrag
		 */
		private final DefaultMutableTreeNode node;

		/**
		 * Checkbox in dem aktuellen Eintrag
		 */
		private final JCheckBox checkBox;

		/**
		 * Zugehörige Baumstruktur
		 */
		private final JTree tree;

		/**
		 * Konstruktor der Klasse
		 * @param editor	Editor für den Eintrag
		 * @param node	Aktueller Eintrag
		 * @param checkBox	Checkbox in dem aktuellen Eintrag
		 * @param tree	Zugehörige Baumstruktur
		 */
		public CheckBoxItemListener(final CheckBoxNodeEditor editor, final DefaultMutableTreeNode node, final JCheckBox checkBox, final JTree tree) {
			this.editor=editor;
			this.node=node;
			this.checkBox=checkBox;
			this.tree=tree;
		}

		/**
		 * Reagiert darauf, wenn eine Checkbox, die keine weiteren Untereinträge besitzt, aktiviert oder deaktiviert wurde.
		 * @param selected	Checkbox aktiviert oder deaktiviert
		 * @return	Gibt <code>true</code> zurück, wenn der Baum neu gezeichnet werden soll
		 * @see #updateSystem(boolean)
		 */
		private boolean updateSystemFromLeaf(final boolean selected) {
			if (!(node.getParent() instanceof DefaultMutableTreeNode)) return false;
			DefaultMutableTreeNode parent=(DefaultMutableTreeNode)node.getParent();

			if (selected) {
				if (!(parent.getUserObject() instanceof CheckBoxNode)) return false;
				((CheckBoxNode)parent.getUserObject()).setSelected(true);
				return true;
			} else {
				boolean anySelected=false;
				for (int i=0;i<parent.getChildCount();i++) {
					if (!(parent.getChildAt(i) instanceof DefaultMutableTreeNode)) continue;
					DefaultMutableTreeNode child=(DefaultMutableTreeNode)parent.getChildAt(i);
					if (child==node) continue;
					if (!(child.getUserObject() instanceof CheckBoxNode)) continue;
					if (((CheckBoxNode)child.getUserObject()).isSelected()) {anySelected=true; break;}
				}
				if (anySelected) return false;
				((CheckBoxNode)parent.getUserObject()).setSelected(false);
				return true;
			}
		}

		/**
		 * Reagiert darauf, wenn eine Checkbox, die Untereinträge besitzt, aktiviert oder deaktiviert wurde.
		 * @param selected	Checkbox aktiviert oder deaktiviert
		 * @return	Gibt <code>true</code> zurück, wenn der Baum neu gezeichnet werden soll
		 * @see #updateSystem(boolean)
		 */
		private boolean updateSystemFromRoot(final boolean selected) {
			boolean needRepaint=false;
			for (int i=0;i<node.getChildCount();i++) {
				if (!(node.getChildAt(i) instanceof DefaultMutableTreeNode)) continue;
				DefaultMutableTreeNode child=(DefaultMutableTreeNode)node.getChildAt(i);
				if (!(child.getUserObject() instanceof CheckBoxNode)) continue;
				((CheckBoxNode)child.getUserObject()).setSelected(selected);
				needRepaint=true;
			}
			return needRepaint;
		}

		/**
		 * Reagiert darauf, wenn eine Checkbox aktiviert oder deaktiviert wurde.
		 * @param selected	Checkbox aktiviert oder deaktiviert
		 * @return	Gibt <code>true</code> zurück, wenn der Baum neu gezeichnet werden soll
		 * @see #itemStateChanged(ItemEvent)
		 */
		private boolean updateSystem(final boolean selected) {
			return node.isLeaf()?updateSystemFromLeaf(selected):updateSystemFromRoot(selected);
		}

		@Override
		public void itemStateChanged(ItemEvent itemEvent) {
			if (updateSystem(itemEvent.getStateChange()==ItemEvent.SELECTED)) tree.repaint();
			checkBox.removeItemListener(this);
			if (editor.stopCellEditing()) editor.publicFireEditingStopped();
		}
	}

	/**
	 * Renderer für die Einträge in {@link CheckBoxTree#tree}
	 */
	private static final class CheckBoxNodeRenderer implements TreeCellRenderer {
		/**
		 * Basis-Element
		 */
		private final JCheckBox checkBox=new JCheckBox();

		/**
		 * Vordergrundfarbe für selektierte Elemente
		 */
		private final Color selectionForeground;

		/**
		 * Hintergrundfarbe für selektierte Elemente
		 */
		private final Color selectionBackground;

		/**
		 * Vordergrundfarbe für nicht-selektierte Elemente
		 */
		private final Color textForeground;

		/**
		 * Hintergrundfarbe für nicht-selektierte Elemente
		 */
		private final Color textBackground;

		/**
		 * Konstruktor der Klasse
		 */
		public CheckBoxNodeRenderer() {
			Font fontValue;
			fontValue=UIManager.getFont("Tree.font");
			if (fontValue!=null) checkBox.setFont(fontValue);

			Boolean booleanValue=(Boolean)UIManager.get("Tree.drawsFocusBorderAroundIcon");
			checkBox.setFocusPainted(booleanValue!=null && booleanValue.booleanValue());

			selectionForeground=UIManager.getColor("Tree.selectionForeground");
			selectionBackground=UIManager.getColor("Tree.selectionBackground");
			textForeground=UIManager.getColor("Tree.textForeground");
			textBackground=UIManager.getColor("Tree.textBackground");
		}

		/**
		 * Liefert einen Renderer für einen Eintrag.
		 * @return	Renderer für einen Eintrag
		 */
		protected JCheckBox getLeafRenderer() {
			return checkBox;
		}

		@Override
		public Component getTreeCellRendererComponent(JTree tree, Object value,	boolean selected, boolean expanded, boolean leaf, int row, boolean hasFocus) {
			String stringValue=tree.convertValueToText(value,selected,expanded,leaf,row,false);
			checkBox.setText(stringValue);
			checkBox.setSelected(false);

			checkBox.setEnabled(tree.isEnabled());

			if (selected) {
				checkBox.setForeground(selectionForeground);
				checkBox.setBackground(selectionBackground);
			} else {
				checkBox.setForeground(textForeground);
				checkBox.setBackground(textBackground);
			}

			if (value!=null && value instanceof DefaultMutableTreeNode) {
				Object userObject=((DefaultMutableTreeNode) value).getUserObject();
				if (userObject instanceof CheckBoxNode) {
					CheckBoxNode node=(CheckBoxNode)userObject;
					checkBox.setText(node.getText());
					checkBox.setSelected(node.isSelected());
				}
			}

			return checkBox;
		}
	}
}