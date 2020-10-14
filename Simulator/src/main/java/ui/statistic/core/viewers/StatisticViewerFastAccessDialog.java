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
package ui.statistic.core.viewers;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.GridLayout;
import java.awt.Window;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.List;

import javax.swing.AbstractButton;
import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.ButtonGroup;
import javax.swing.DefaultCellEditor;
import javax.swing.JCheckBox;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JRadioButton;
import javax.swing.JScrollPane;
import javax.swing.JSplitPane;
import javax.swing.JTable;
import javax.swing.JTree;
import javax.swing.event.TableModelEvent;
import javax.swing.event.TreeSelectionEvent;
import javax.swing.event.TreeSelectionListener;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.DefaultTableModel;
import javax.swing.tree.DefaultMutableTreeNode;
import javax.swing.tree.TreePath;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NamedNodeMap;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import language.Language;
import ui.editor.BaseEditDialog;
import ui.statistic.core.filter.DataFilterBase;

/**
 * Ermöglicht die Auswahl eines xml-Elements auf Basis eines XML-Dokuments.
 * @author Alexander Herzog
 * @version 1.0
 */
public class StatisticViewerFastAccessDialog extends BaseEditDialog {
	/**
	 * Serialisierungs-ID der Klasse
	 * @see Serializable
	 */
	private static final long serialVersionUID = 871510627014695528L;

	private final Document xmlDoc;
	private final int simDays;
	private final boolean plainMode;

	private JTree tree;
	private JPanel contentArea;
	private DefaultReadOnlyTableModel contentModel;
	private DefaultReadOnlyTableModel attributeModel;
	private JLabel xmlInfoLabel;
	private JTable contentTable;
	private JTable attributeTable;
	private ButtonGroup buttonGroup;
	private ButtonGroup insertButtonGroup;
	private JRadioButton[] insertButtons;

	/**
	 * Konstruktor der Klasse
	 * @param owner	Übergeordnetes Fenster
	 * @param xmlDoc	xml-Dokument (Statistik oder Modell) dem die Daten entnommen werden sollen
	 * @param helpModal	Hilfe für Schnellzugriff-Dialog
	 * @param plainMode	Sollen Optionen zum Einfügen des gewählten Elements in ein Skript angeboten werden (<code>false</code>) oder geht es nur um die Auswahl des XML-Elements als solches (<code>true</code>)
	 */
	public StatisticViewerFastAccessDialog(Window owner, Document xmlDoc, Runnable helpModal, boolean plainMode) {
		super(owner,Language.tr("Statistic.FastAccess.SelectXMLTag"),false,helpModal);
		this.xmlDoc=xmlDoc;
		this.plainMode=plainMode;
		simDays=DataFilterBase.readSimDays(xmlDoc);
		createSimpleGUI(750,600,null,null);
	}

	private DefaultMutableTreeNode createTreeNode(Element xmlNode) {
		DefaultMutableTreeNode treeNode=new DefaultMutableTreeNode(new XMLNodeWrapper(xmlNode));

		NodeList list=xmlNode.getChildNodes();
		for (int i=0;i<list.getLength();i++) if (list.item(i) instanceof Element) {
			Element xmlChild=(Element)list.item(i);
			treeNode.add(createTreeNode(xmlChild));
		}

		return treeNode;
	}

	@Override
	protected void createSimpleContent(JPanel content) {
		content.setLayout(new BorderLayout());
		JSplitPane split=new JSplitPane(JSplitPane.HORIZONTAL_SPLIT);
		content.add(split,BorderLayout.CENTER);

		Dimension minimumSize=new Dimension(250,50);

		JScrollPane scroll=new JScrollPane(tree=new JTree(createTreeNode(xmlDoc.getDocumentElement())));
		scroll.setMinimumSize(minimumSize);
		split.add(scroll);
		tree.addTreeSelectionListener(new TreeSelectionChanged());

		split.add(contentArea=new JPanel());
		contentArea.setLayout(new BoxLayout(contentArea,BoxLayout.Y_AXIS));
		contentArea.setMinimumSize(minimumSize);

		setTableData((XMLNodeWrapper)((DefaultMutableTreeNode)tree.getModel().getRoot()).getUserObject());
	}

	private void setTableData(XMLNodeWrapper node) {
		buttonGroup=new ButtonGroup();

		if (xmlInfoLabel==null) {
			JPanel p;
			contentArea.add(p=new JPanel(new FlowLayout(FlowLayout.LEFT)));
			p.add(xmlInfoLabel=new JLabel());
			p.setMaximumSize(new Dimension(1000,20));
			p.setBackground(Color.GRAY);
			p.setBorder(BorderFactory.createLineBorder(Color.BLACK));
		}
		if (node.xmlNode==null) {
			xmlInfoLabel.setVisible(false);
		} else {
			xmlInfoLabel.setVisible(true);
			String[] list=node.getPathList(null);
			StringBuilder sb=new StringBuilder();
			for (int i=0;i<list.length;i++) {
				if (i>0) sb.append("<br>");
				for (int j=0;j<=i*2;j++) sb.append("&nbsp;");
				sb.append(list[i]);
			}
			xmlInfoLabel.setText("<html><body style=\"color: white;\"><b>"+sb.toString()+"</b></body></html>");
		}

		contentModel=new DefaultReadOnlyTableModel(node.getContentTableData(buttonGroup));
		if (contentTable==null) {
			contentTable=new JTable(contentModel){
				/**
				 * Serialisierungs-ID der Klasse
				 * @see Serializable
				 */
				private static final long serialVersionUID = -3007665088654159769L;
				@Override
				public void tableChanged(TableModelEvent e) {
					super.tableChanged(e);
					repaint();
				}
			};
			JPanel p;
			contentArea.add(p=new JPanel(new FlowLayout(FlowLayout.LEFT)));
			p.add(new JLabel("<html><body><b>"+Language.tr("Statistic.FastAccess.SelectXMLTag.ElementContent")+"</b></body></html>"));
			p.setMaximumSize(new Dimension(1000,20));
			contentArea.add(contentTable.getTableHeader());
			contentArea.add(contentTable);
		} else {
			contentTable.setModel(contentModel);
		}
		contentTable.getTableHeader().setReorderingAllowed(false);
		contentTable.getColumnModel().getColumn(0).setCellRenderer(new RadioButtonRenderer());
		contentTable.getColumnModel().getColumn(0).setCellEditor(new RadioButtonEditor(new JCheckBox()));

		attributeModel=new DefaultReadOnlyTableModel(node.getAttributeTableData(buttonGroup));
		if (attributeTable==null) {
			attributeTable=new JTable(attributeModel) {
				/**
				 * Serialisierungs-ID der Klasse
				 * @see Serializable
				 */
				private static final long serialVersionUID = -3007665088654159769L;
				@Override
				public void tableChanged(TableModelEvent e) {
					super.tableChanged(e);
					repaint();
				}
			};
			contentArea.add(Box.createVerticalStrut(10));
			JPanel p;
			contentArea.add(p=new JPanel(new FlowLayout(FlowLayout.LEFT)));
			p.add(new JLabel("<html><body><b>"+Language.tr("Statistic.FastAccess.SelectXMLTag.Attributes")+"</b></body></html>"));
			p.setMaximumSize(new Dimension(1000,20));
			contentArea.add(attributeTable.getTableHeader());
			contentArea.add(attributeTable);
		} else {
			attributeTable.setModel(attributeModel);
		}
		attributeTable.getTableHeader().setReorderingAllowed(false);
		attributeTable.getColumnModel().getColumn(0).setCellRenderer(new RadioButtonRenderer());
		attributeTable.getColumnModel().getColumn(0).setCellEditor(new RadioButtonEditor(new JCheckBox()));

		if (!plainMode) {
			if (insertButtonGroup==null) {
				JPanel p;
				contentArea.add(p=new JPanel(new FlowLayout(FlowLayout.LEFT)));
				p.add(new JLabel("<html><body><b>"+Language.tr("Statistic.FastAccess.SelectXMLTag.InsertMethod")+"</b></body></html>"));
				p.setMaximumSize(new Dimension(1000,20));
				insertButtonGroup=new ButtonGroup();
				insertButtons=new JRadioButton[4];
				contentArea.add(p=new JPanel(new GridLayout(insertButtons.length,0)));
				p.add(insertButtons[0]=new JRadioButton(Language.tr("Statistic.FastAccess.SelectXMLTag.InsertMethod.AtTheEndWithPrint"))); insertButtonGroup.add(insertButtons[0]);
				p.add(insertButtons[1]=new JRadioButton(Language.tr("Statistic.FastAccess.SelectXMLTag.InsertMethod.AtCursorPositionWithPrint"))); insertButtonGroup.add(insertButtons[2]);
				p.add(insertButtons[2]=new JRadioButton(Language.tr("Statistic.FastAccess.SelectXMLTag.InsertMethod.AtTheEnd"))); insertButtonGroup.add(insertButtons[1]);
				p.add(insertButtons[3]=new JRadioButton(Language.tr("Statistic.FastAccess.SelectXMLTag.InsertMethod.AtCursorPosition"))); insertButtonGroup.add(insertButtons[2]);
				p.setMaximumSize(new Dimension(1000,70));
				insertButtons[1].setSelected(true);
				contentArea.add(Box.createVerticalGlue());
			}
		}

		if (buttonGroup.getButtonCount()>0) {
			Enumeration<AbstractButton> buttons=buttonGroup.getElements();
			buttons.nextElement().setSelected(true);
		}
	}

	/* (non-Javadoc)
	 * @see complexcallcenter.editor.BaseEditDialog#checkData()
	 */
	@Override
	protected boolean checkData() {
		return buttonGroup.getButtonCount()>0;
	}

	/* (non-Javadoc)
	 * @see complexcallcenter.editor.BaseEditDialog#storeData()
	 */
	@Override
	protected void storeData() {
	}

	/**
	 * Liefert das gewählte xml-Element bzw. -Attribut
	 * @return	Gewähltes xml-Element bzw. -Attribut
	 */
	public String getXMLSelector() {
		Enumeration<AbstractButton> buttons=buttonGroup.getElements();
		while (buttons.hasMoreElements()) {
			AbstractButton button=buttons.nextElement();
			if (button.isSelected()) return button.getToolTipText();
		}
		return null;
	}

	/**
	 * Wurde der Dialog nicht im <code>plainMode</code> aufgerufen,
	 * so kann über diese Methode die gewählte Einfüge-Art abgefragt werden.
	 * @return	Gewählte Art auf die das XML-Element in das Skript eingefügt werden soll
	 */
	public int getInsertType() {
		if (insertButtons!=null) for (int i=0;i<insertButtons.length;i++) if (insertButtons[i].isSelected()) return i;
		return 0;
	}

	private class DefaultReadOnlyTableModel extends DefaultTableModel {
		/**
		 * Serialisierungs-ID der Klasse
		 * @see Serializable
		 */
		private static final long serialVersionUID = 2744629774042523677L;

		public DefaultReadOnlyTableModel(Object[][] data) {
			super(data,new String[]{Language.tr("Statistic.FastAccess.SelectXMLTag.Property"),Language.tr("Statistic.FastAccess.SelectXMLTag.Value")});
		}

		@Override
		public boolean isCellEditable(int rowIndex, int columnIndex) {return columnIndex==0;}
	}

	private class RadioButtonRenderer extends DefaultTableCellRenderer {
		/**
		 * Serialisierungs-ID der Klasse
		 * @see Serializable
		 */
		private static final long serialVersionUID = -7643893461330181707L;

		@Override
		public Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected, boolean hasFocus, int row, int column) {
			if (value==null || !(value instanceof Component)) return super.getTableCellRendererComponent(table,value,isSelected,hasFocus,row,column);
			return (Component)value;
		}
	}

	private class RadioButtonEditor extends DefaultCellEditor implements ItemListener {
		/**
		 * Serialisierungs-ID der Klasse
		 * @see Serializable
		 */
		private static final long serialVersionUID = -2826551849190366679L;

		private JRadioButton button;

		public RadioButtonEditor(JCheckBox checkBox) {
			super(checkBox);
		}

		@Override
		public Component getTableCellEditorComponent(JTable table, Object value, boolean isSelected, int row, int column) {
			if (value==null || !(value instanceof JRadioButton)) return null;
			button=(JRadioButton)value;
			button.addItemListener(this);
			return (Component)value;
		}

		@Override
		public Object getCellEditorValue() {
			button.removeItemListener(this);
			return button;
		}

		@Override
		public void itemStateChanged(ItemEvent e) {
			super.fireEditingStopped();
		}
	}

	private class XMLNodeWrapper {
		private final String[] specialTags=new String[] {
				Language.trPrimary("XML.Model.BaseElement")+","+Language.trPrimary("XML.Model.ClientType"),
				Language.trPrimary("XML.Model.BaseElement")+","+Language.trPrimary("XML.Model.CallCenter"),
				Language.trPrimary("XML.Model.BaseElement")+","+Language.trPrimary("XML.Model.SkillLevel"),
				Language.trPrimary("XML.Statistic.BaseElement")+","+Language.trPrimary("XML.Model.BaseElement")+","+Language.trPrimary("XML.Model.ClientType"),
				Language.trPrimary("XML.Statistic.BaseElement")+","+Language.trPrimary("XML.Model.BaseElement")+","+Language.trPrimary("XML.Model.CallCenter"),
				Language.trPrimary("XML.Statistic.BaseElement")+","+Language.trPrimary("XML.Model.BaseElement")+","+Language.trPrimary("XML.Model.SkillLevel"),
				Language.trPrimary("XML.Statistic.BaseElement")+","+Language.trPrimary("XML.Statistic.Clients"),
				Language.trPrimary("XML.Statistic.BaseElement")+","+Language.trPrimary("XML.Statistic.Clients")+","+Language.trPrimary("XML.Statistic.Clients.Summary"),
				Language.trPrimary("XML.Statistic.BaseElement")+","+Language.trPrimary("XML.Statistic.Queue.Summary"),
				Language.trPrimary("XML.Statistic.BaseElement")+","+Language.trPrimary("XML.Statistic.Agents"),
				Language.trPrimary("XML.Statistic.BaseElement")+","+Language.trPrimary("XML.Statistic.Agents")+","+Language.trPrimary("XML.Statistic.Agents.ClientType")
		};

		public final Element xmlNode;
		public final boolean needID;
		public final boolean needNrID;
		public final int nrID;

		public XMLNodeWrapper(Element xmlNode) {
			this.xmlNode=xmlNode;

			if (xmlNode==null) {needID=false; needNrID=false; nrID=-1; return;}

			/* Nodes mit Name="..." Parametern */
			String s=xmlNode.getNodeName(); Node path=xmlNode.getParentNode();
			while (path.getParentNode()!=null) {s=path.getNodeName()+","+s; path=path.getParentNode();}
			boolean b=false;
			for (int i=0;i<specialTags.length;i++) if (specialTags[i].equalsIgnoreCase(s)) {b=true; break;}
			needID=b;

			/* Nodes, die es mehrfach gibt */
			if (needID) {
				needNrID=false; nrID=-1;
			} else {
				if (xmlNode.getParentNode()==null || !(xmlNode.getParentNode() instanceof Element)) {needNrID=false; nrID=-1;} else {
					Element parent=(Element)xmlNode.getParentNode();
					int count=0;
					int nr=0;
					String name=xmlNode.getNodeName();
					NodeList list=parent.getChildNodes();
					for (int i=0;i<list.getLength();i++) if (list.item(i) instanceof Element) {
						Element el=(Element)list.item(i);
						if (el.getNodeName().equals(name)) count++;
						if (el==xmlNode) nr=count;
					}
					needNrID=(count>1);
					nrID=nr;
				}
			}
		}

		private String getIDSelector() {
			if (needID) {
				String[] testIDs=new String[]{"XML.Statistic.Clients.Summary.Range","XML.Statistic.Queue.Summary.Range","XML.Statistic.GeneralAttributes.Name"};

				for (String testID : testIDs) {
					String special=Language.trAllAttribute(testID,xmlNode);
					if (!special.isEmpty()) return "["+Language.trPrimary(testID)+"=\""+special+"\"]";
				}

				return "["+Language.trPrimary(testIDs[testIDs.length-1])+"=\"\"]";
			}

			if (needNrID) {
				return "["+nrID+"]";
			}

			return "";
		}

		@Override
		public String toString() {
			if (xmlNode==null) return "("+Language.tr("Statistic.FastAccess.SelectXMLTag.empty")+")";
			return xmlNode.getNodeName()+getIDSelector();
		}

		public String toXMLString() {
			if (xmlNode==null) return "("+Language.tr("Statistic.FastAccess.SelectXMLTag.empty")+")";
			return xmlNode.getNodeName()+getIDSelector();
		}

		private String getPath(String attribute) {
			if (xmlNode==null) return "";
			String s=toXMLString();
			Node path=xmlNode.getParentNode();
			while (path!=null && path.getParentNode()!=null && path.getParentNode().getParentNode()!=null) {
				if (path instanceof Element) {
					XMLNodeWrapper parent=new XMLNodeWrapper((Element)path);
					s=parent.toXMLString()+"->"+s;
				}
				path=path.getParentNode();
			}
			if (attribute!=null && !attribute.isEmpty()) {
				if (needID) s+="->";
				s+="["+attribute+"]";
			}
			return s;
		}

		private String[] getPathList(String attribute) {
			if (xmlNode==null) return new String[0];
			List<String> list=new ArrayList<String>();
			list.add(toXMLString());
			Node path=xmlNode.getParentNode();
			while (path!=null && path.getParentNode()!=null && path.getParentNode().getParentNode()!=null) {
				if (path instanceof Element) {
					XMLNodeWrapper parent=new XMLNodeWrapper((Element)path);
					list.add(0,parent.toXMLString());
				}
				path=path.getParentNode();
			}
			if (attribute!=null && !attribute.isEmpty()) {
				if (needID) list.add("["+attribute+"]"); else list.set(list.size()-1,list.get(list.size()-1)+"["+attribute+"]");
			}

			return list.toArray(new String[0]);
		}

		private String formatValue(String value, String attribute) {
			List<String> path=new ArrayList<String>();
			if (attribute!=null && !attribute.isEmpty()) path.add(attribute);
			path.add(0,xmlNode.getNodeName());
			Node parent=xmlNode.getParentNode();
			while (parent!=null && parent.getParentNode()!=null && parent.getParentNode().getParentNode()!=null) {
				if (parent instanceof Element) {
					path.add(0,parent.getNodeName());
				}
				parent=parent.getParentNode();
			}
			return DataFilterBase.formatNumber(value,path,simDays,false,false,false,';');
		}

		public Object[][] getContentTableData(ButtonGroup buttonGroup) {
			Object[][] data=new Object[1][2];
			String s=null;
			if (xmlNode!=null) s=xmlNode.getTextContent();
			boolean b=false;
			if (xmlNode!=null) {
				NodeList list=xmlNode.getChildNodes();
				for (int i=0;i<list.getLength();i++) if (list.item(i) instanceof Element) {b=true; break;}
			}
			if (b) s=null;
			if (s==null || s.isEmpty()) {
				data[0][0]=Language.tr("Statistic.FastAccess.SelectXMLTag.ElementContent");
				data[0][1]="("+Language.tr("Statistic.FastAccess.SelectXMLTag.empty")+")";
			} else {
				JRadioButton rb=new JRadioButton(Language.tr("Statistic.FastAccess.SelectXMLTag.ElementContent"));
				buttonGroup.add(rb);
				rb.setToolTipText(getPath(null));
				data[0][0]=rb;
				data[0][1]=formatValue(s,null);
			}
			return data;
		}

		public Object[][] getAttributeTableData(ButtonGroup buttonGroup) {
			NamedNodeMap map=null;
			if (xmlNode!=null) map=xmlNode.getAttributes();
			Object[][] data;
			if (map==null || map.getLength()==0) {
				data=new Object[1][2];
				data[0][0]="("+Language.tr("Statistic.FastAccess.SelectXMLTag.NoAttributes")+")";
				data[0][1]="";
			} else {
				data=new Object[map.getLength()][2];
				for (int i=0;i<map.getLength();i++) {
					JRadioButton rb=new JRadioButton(map.item(i).getNodeName());
					buttonGroup.add(rb);
					rb.setToolTipText(getPath(map.item(i).getNodeName()));
					data[i][0]=rb;
					data[i][1]=formatValue(map.item(i).getNodeValue(),map.item(i).getNodeName());
				}
			}
			return data;
		}
	}

	private class TreeSelectionChanged implements TreeSelectionListener {
		private DefaultMutableTreeNode getSelItem() {
			TreePath path=tree.getSelectionPath(); if (path==null || path.getPathCount()==0) return null;
			Object obj=path.getPath()[path.getPathCount()-1];
			if (obj==null || !(obj instanceof DefaultMutableTreeNode)) return null;
			return (DefaultMutableTreeNode)obj;
		}

		@Override
		public void valueChanged(TreeSelectionEvent e) {
			DefaultMutableTreeNode treeNode=getSelItem();
			XMLNodeWrapper xmlNode;
			if (treeNode==null) xmlNode=new XMLNodeWrapper(null); else xmlNode=(XMLNodeWrapper)treeNode.getUserObject();
			setTableData(xmlNode);
		}
	}
}
