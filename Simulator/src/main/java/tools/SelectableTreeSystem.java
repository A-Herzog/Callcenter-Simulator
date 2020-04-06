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
package tools;

import java.awt.Color;
import java.awt.Component;
import java.awt.Font;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;
import java.awt.event.MouseEvent;
import java.util.ArrayList;
import java.util.EventObject;
import java.util.List;

import javax.swing.AbstractCellEditor;
import javax.swing.ButtonGroup;
import javax.swing.JCheckBox;
import javax.swing.JRadioButton;
import javax.swing.JToggleButton;
import javax.swing.JTree;
import javax.swing.UIManager;
import javax.swing.tree.DefaultMutableTreeNode;
import javax.swing.tree.DefaultTreeCellRenderer;
import javax.swing.tree.DefaultTreeModel;
import javax.swing.tree.TreeCellEditor;
import javax.swing.tree.TreePath;

/**
 * Diese Hilfsklasse ermöglicht es, eine {@link JTree}-Baumstruktur
 * anzulegen, die wahlweise Texte, Radiobuttons und Checkboxen
 * (auch gemischt) enthält.
 * @author Alexander Herzog
 * @see JTree
 */
public class SelectableTreeSystem {
	private final List<SelectableTreeNode> nodes=new ArrayList<SelectableTreeNode>();
	private final JTree tree;
	private final DefaultMutableTreeNode root=new DefaultMutableTreeNode();
	private final DefaultTreeModel model;
	private final ButtonGroup group=new ButtonGroup();

	/**
	 * Konstruktor der Klasse
	 */
	public SelectableTreeSystem() {
		model=new DefaultTreeModel(root);
		tree=new JTree(model);

		tree.setRootVisible(false);
		tree.setCellRenderer(new SelectableNodeRenderer());
		tree.setCellEditor(new SelectableNodeEditor(tree));
		tree.setEditable(true);
	}

	/**
	 * Legt einen Text-Eintrag an.
	 * @param text	Anzuzeigender Text
	 * @return	Neuer Eintrag für die Baumstruktur
	 */
	public DefaultMutableTreeNode createNode(String text) {
		return createNode(text,TreeNodeType.TREENODETYPE_TEXT,0,false);
	}

	private DefaultMutableTreeNode createNode(String text, TreeNodeType type, int id) {
		return createNode(text,type,id,false);
	}

	private DefaultMutableTreeNode createNode(String text, TreeNodeType type, int id, boolean selected) {
		SelectableTreeNode node=new SelectableTreeNode(text,type,selected,id);
		nodes.add(node);
		return new DefaultMutableTreeNode(node);
	}

	/**
	 * Legt einen Text-Eintrag an.
	 * @param text	Anzuzeigender Text
	 * @return	Neuer Eintrag für die Baumstruktur
	 */
	public DefaultMutableTreeNode createText(String text) {
		return createNode(text,TreeNodeType.TREENODETYPE_TEXT,0);
	}

	/**
	 * Legt einen Checkbox-Eintrag an.
	 * @param text	Anzuzeigender Text
	 * @param id	ID des Eintrags
	 * @return	Neuer Eintrag für die Baumstruktur
	 */
	public DefaultMutableTreeNode createCheckBox(String text, int id) {
		return createNode(text,TreeNodeType.TREENODETYPE_CHECKBOX,id);
	}

	/**
	 * Legt einen Radiobutton-Eintrag an.
	 * @param text	Anzuzeigender Text
	 * @param id	ID des Eintrags
	 * @return	Neuer Eintrag für die Baumstruktur
	 */
	public DefaultMutableTreeNode createRadioButton(String text, int id) {
		return createNode(text,TreeNodeType.TREENODETYPE_RADIOBUTTON,id);
	}

	/**
	 * Liefert die gesamte Baumstruktur.
	 * @return	Baumstruktur, d.h. das {@link JTree}-Objekt welches die Einträge enthält
	 */
	public JTree getTree() {
		return tree;
	}

	/**
	 * Liefert das Wurzelelement der Baumstruktur.
	 * @return	Wurzelelement der Baumstruktur
	 */
	public DefaultMutableTreeNode getRoot() {
		return root;
	}

	/**
	 * Baut die Baumstruktur neu auf.
	 * @param expand	Alle Einträge ausgeklappt anzeigen?
	 */
	public void reload(boolean expand) {
		model.reload();

		if (expand) {
			int row=0; while (row<tree.getRowCount()) {
				DefaultMutableTreeNode node=(DefaultMutableTreeNode)(tree.getPathForRow(row).getLastPathComponent());
				if (!node.isLeaf()) tree.expandRow(row);
				row++;
			}
		}
	}

	/**
	 * Liefert eine Liste mit allen Einträgen der Baumstruktur.
	 * @return	Liste mit allen Einträgen der Baumstruktur
	 */
	public List<SelectableTreeNode> getNodes() {
		return nodes;
	}

	/* SelectableTreeNode */

	private enum TreeNodeType {
		TREENODETYPE_TEXT,
		TREENODETYPE_CHECKBOX,
		TREENODETYPE_RADIOBUTTON
	}

	/**
	 * Diese Objekte werden ale Nutzerobjekte in den {@link DefaultMutableTreeNode}-Elementen verwendet.
	 * @author Alexander Herzog
	 */
	public final class SelectableTreeNode {
		private final String text;
		private final TreeNodeType type;
		/** Radiobutton oder CheckBox */
		public final JToggleButton toggle;
		/** Bei der Erstellung angegebene ID */
		public final int id;

		private SelectableTreeNode(String text, TreeNodeType type, boolean selected, int id) {
			this.text=text;
			this.type=type;
			this.id=id;
			switch (type) {
			case TREENODETYPE_CHECKBOX:
				toggle=new JCheckBox();
				break;
			case TREENODETYPE_RADIOBUTTON:
				toggle=new JRadioButton();
				group.add(toggle);
				break;
			default:
				toggle=null;
			}
			if (toggle!=null) {
				toggle.setSelected(selected);
				toggle.setText(text);
			}
		}

		@Override
		public String toString() {
			return text;
		}
	}

	/* SelectableNodeRenderer */

	private final class SelectableNodeRenderer extends DefaultTreeCellRenderer {
		private static final long serialVersionUID = 6484012860741022315L;

		private final Color selectionForeground, selectionBackground, textForeground, textBackground;
		private SelectableTreeNode node;

		public SelectableNodeRenderer() {
			selectionForeground=UIManager.getColor("Tree.selectionForeground");
			selectionBackground=UIManager.getColor("Tree.selectionBackground");
			textForeground=UIManager.getColor("Tree.textForeground");
			textBackground=UIManager.getColor("Tree.textBackground");
		}

		public SelectableTreeNode getNode() {
			return node;
		}

		private JCheckBox getCheckBox(SelectableTreeNode node, boolean selected) {
			JCheckBox checkBox=(JCheckBox)node.toggle;

			Font fontValue;
			fontValue=UIManager.getFont("Tree.font");
			if (fontValue!=null) checkBox.setFont(fontValue);

			Boolean booleanValue=(Boolean)UIManager.get("Tree.drawsFocusBorderAroundIcon");
			checkBox.setFocusPainted(booleanValue!=null && booleanValue.booleanValue());

			if (selected) {
				checkBox.setForeground(selectionForeground);
				checkBox.setBackground(selectionBackground);
			} else {
				checkBox.setForeground(textForeground);
				checkBox.setBackground(textBackground);
			}

			return checkBox;

		}

		private JRadioButton getRadioButton(SelectableTreeNode node, boolean selected) {
			JRadioButton radioButton=(JRadioButton)node.toggle;


			Font fontValue;
			fontValue=UIManager.getFont("Tree.font");
			if (fontValue!=null) radioButton.setFont(fontValue);

			Boolean booleanValue=(Boolean)UIManager.get("Tree.drawsFocusBorderAroundIcon");
			radioButton.setFocusPainted(booleanValue!=null && booleanValue.booleanValue());

			radioButton.setEnabled(tree.isEnabled());

			if (selected) {
				radioButton.setForeground(selectionForeground);
				radioButton.setBackground(selectionBackground);
			} else {
				radioButton.setForeground(textForeground);
				radioButton.setBackground(textBackground);
			}

			return radioButton;
		}

		@Override
		public Component getTreeCellRendererComponent(JTree tree, Object value,	boolean selected, boolean expanded, boolean leaf, int row, boolean hasFocus) {
			if (value!=null && value instanceof DefaultMutableTreeNode) {
				Object userObject=((DefaultMutableTreeNode) value).getUserObject();
				if (userObject instanceof SelectableTreeNode) node=(SelectableTreeNode)userObject;
			}

			if (node!=null && node.type==TreeNodeType.TREENODETYPE_CHECKBOX) return getCheckBox(node,selected);
			if (node!=null && node.type==TreeNodeType.TREENODETYPE_RADIOBUTTON) return getRadioButton(node,selected);
			return super.getTreeCellRendererComponent(tree,value,selected,expanded,leaf,row,hasFocus);
		}
	}

	/* SelectableNodeEditor */

	private final class SelectableNodeEditor extends AbstractCellEditor implements TreeCellEditor {
		private static final long serialVersionUID = 2729327970043094220L;

		private final SelectableNodeRenderer renderer=new SelectableNodeRenderer();
		private final JTree tree;

		public SelectableNodeEditor(JTree tree) {
			this.tree=tree;
		}

		@Override
		public Object getCellEditorValue() {
			return renderer.getNode();
		}

		@Override
		public boolean isCellEditable(EventObject event) {
			if (!(event instanceof MouseEvent)) return false;
			MouseEvent mouseEvent=(MouseEvent)event;

			TreePath path=tree.getPathForLocation(mouseEvent.getX(),mouseEvent.getY());
			if (path==null) return false;

			Object node=path.getLastPathComponent();
			if (node==null || !(node instanceof DefaultMutableTreeNode)) return false;

			return (((DefaultMutableTreeNode)node).getUserObject() instanceof SelectableTreeNode);
		}

		@Override
		public Component getTreeCellEditorComponent(JTree tree, Object value, boolean selected, boolean expanded, boolean leaf, int row) {
			Component editor=renderer.getTreeCellRendererComponent(tree,value,true,expanded,leaf,row,true);

			DefaultMutableTreeNode node=null;
			if (value instanceof DefaultMutableTreeNode) node=(DefaultMutableTreeNode)value;

			if (editor instanceof JToggleButton) {
				JToggleButton toggle=(JToggleButton)editor;
				ItemListener[] l=toggle.getItemListeners();
				for (int i=0;i<l.length;i++) if (l[i] instanceof SelectableItemListener) toggle.removeItemListener(l[i]);
				toggle.addItemListener(new SelectableItemListener(this,node,toggle,tree));
			}

			return editor;
		}

		public void publicFireEditingStopped() {
			fireEditingStopped();
		}
	}

	/* SelectableItemListener */

	private final class SelectableItemListener implements ItemListener {
		private final SelectableNodeEditor editor;
		private final DefaultMutableTreeNode node;
		private final JToggleButton toggleButton;
		private final JTree tree;

		public SelectableItemListener(SelectableNodeEditor editor, DefaultMutableTreeNode node, JToggleButton toggleButton, JTree tree) {
			this.editor=editor;
			this.node=node;
			this.toggleButton=toggleButton;
			this.tree=tree;
		}

		private boolean updateSystemFromLeaf(boolean selected) {
			if (!(node.getUserObject() instanceof SelectableTreeNode)) return false;
			((SelectableTreeNode)node.getUserObject()).toggle.setSelected(selected);
			return true;
		}

		private boolean updateSystemFromRoot(boolean selected) {
			if (!(node.getUserObject() instanceof SelectableTreeNode)) return false;
			((SelectableTreeNode)node.getUserObject()).toggle.setSelected(selected);

			boolean needRepaint=false;
			for (int i=0;i<node.getChildCount();i++) {
				if (!(node.getChildAt(i) instanceof DefaultMutableTreeNode)) continue;
				DefaultMutableTreeNode child=(DefaultMutableTreeNode)node.getChildAt(i);
				if (!(child.getUserObject() instanceof SelectableTreeNode)) continue;
				((SelectableTreeNode)child.getUserObject()).toggle.setSelected(selected);
				needRepaint=true;
			}
			return needRepaint;
		}

		private boolean updateSystem(boolean selected) {
			return node.isLeaf()?updateSystemFromLeaf(selected):updateSystemFromRoot(selected);
		}

		@Override
		public void itemStateChanged(ItemEvent itemEvent) {
			updateSystem(itemEvent.getStateChange()==ItemEvent.SELECTED);
			toggleButton.removeItemListener(this);
			if (editor.stopCellEditing()) editor.publicFireEditingStopped();
			tree.repaint();
		}
	}
}
