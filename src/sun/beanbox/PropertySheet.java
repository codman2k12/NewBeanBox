package sun.beanbox;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.beans.*;
import java.lang.reflect.*;
import java.awt.*;
import java.util.*;
import java.util.List;
import javax.swing.*;
import javax.swing.event.CellEditorListener;
import javax.swing.event.TableModelListener;
import javax.swing.table.TableCellEditor;
import javax.swing.table.TableCellRenderer;
import javax.swing.table.TableModel;

import com.paragraph.gerasimov.editors.*;

public class PropertySheet extends JFrame
{
	private PropertySheetPanel panel;
	private boolean started;

	PropertySheet(Wrapper target, int x, int y)
	{
		super("Properties - <initializing...>");
		setLayout(new BorderLayout());
		//setBackground(Color.lightGray);
		setLocation(x,y);
		//setSize(400,600);

		panel = new PropertySheetPanel(this);

		panel.setTarget(target);
		setTitle("Properties - " + target.getBeanLabel());


		pack();
		started = true;
		setVisible(true);
	}

	void setTarget(Wrapper targ)
	{
		Object bean = targ.getBean();
		String displayName = targ.getBeanLabel();
		panel.setTarget(targ);
		setTitle("Properties - " + displayName);
	}


	void setCustomizer(Customizer c)
	{
		panel.setCustomizer(c);
	}

	void wasModified(PropertyChangeEvent evt)
	{
		panel.wasModified(evt);
	}
}

class PropertySheetPanel extends JPanel
{

	PropertySheetPanel(PropertySheet frame)
	{
		this.frame = frame;
		//setBackground(Color.red);
		setLayout(new BorderLayout());
	}

	synchronized void setTarget(Wrapper targ)
	{
		//frame.removeAll();
		removeAll();
		tableComponents.clear();

		if (target != null)
		{
			setVisible(false);
		}
		targetWrapper = targ;
		target = targ.getBean();

		Object[] allParentsBeans = targetWrapper.getAllParentsBeans();

		try
		{
			BeanInfo bi = Introspector.getBeanInfo(target.getClass());
			properties = bi.getPropertyDescriptors();
		}
		catch (IntrospectionException ex)
		{
			error("PropertySheet: Couldn't introspect", ex);
			return;
		}

		editors = new PropertyEditor[properties.length];
		values = new Object[properties.length];
		views = new JComponent[properties.length];
		names = new String[properties.length];
		int count=0;
		EditedAdaptor adaptor = new EditedAdaptor(frame);
		String name;
		Class type;


		JComponent view = null;


		for (int i = 0; i < properties.length; i++) {
			if (properties[i].isHidden() || properties[i].isExpert()) {
				continue;
			}

			name = properties[i].getDisplayName();

			type = properties[i].getPropertyType();
			if (type == null) {
				System.err.println("name = " + name + ",  property type == null.");
			}

			Method getter = properties[i].getReadMethod();
			Method setter = properties[i].getWriteMethod();
			if (getter == null) {
				Method indexedGetter =
						(properties[i] instanceof IndexedPropertyDescriptor) ?
								((IndexedPropertyDescriptor) properties[i]).getIndexedReadMethod()
								:
								null;
				if ((getter == null) && (indexedGetter == null)) {
					continue;
				}
				System.err.println(" ONLY HAVE indexedGetter = " + indexedGetter);
			}


			try {
				Object value = getPropertyValueByPropertyDescriptor(properties[i]);
				values[i] = value;

				PropertyEditor editor = null;
				Class pec = properties[i].getPropertyEditorClass();
				if (pec != null) {
					try {
						editor = (PropertyEditor) pec.newInstance();
					} catch (Exception ex) {

					}
				}

				if (editor == null && type != null) {
					editor = PropertyEditorManager.findEditor(type);
				}
				editors[i] = editor;

				if (editor == null) {
					if (properties[i] instanceof IndexedPropertyDescriptor) {
						System.err.println("property[" + i + "] type is Array");
						if (type != null)
							System.err.println("property[" + i + "] type is " + type.getName());
						editors[i] = new IndexedPropertyEditor
								(
										targetWrapper,
										(IndexedPropertyDescriptor) properties[i],
										frame
								);
					}
					if (editors[i] == null) // E.G.
					{
						continue;
					} else {
						editor = editors[i];
					}
				}

				Class editorClass = editor.getClass();
				Method initMethod = null;
				Class[] parameterTypes = {Object.class};
				try {

					initMethod = editorClass.getMethod("initContextInfo", parameterTypes);
					if (initMethod != null) {
						Object[] initArgs = new Object[allParentsBeans.length + 2];
						initArgs[0] = "" + targetWrapper.getBeanLabel() + " @" + Integer.toHexString(target.hashCode());
						initArgs[1] = properties[i];
						System.arraycopy(allParentsBeans, 0, initArgs, 2, allParentsBeans.length);
						Object[] invokeParam = {initArgs};
						initMethod.invoke(editor, invokeParam);
					}
				} catch (Exception e) {
				}
				editor.setValue(value);
				editor.addPropertyChangeListener(adaptor);

				if (editor.isPaintable() && editor.supportsCustomEditor()) {
					view = new PropertyCanvas(editor);


				} else if (editor.getTags() != null) {
					view = new PropertySelector(editor);
				} else if (editor.getAsText() != null) {
					String init = editor.getAsText();
					view = new PropertyText(editor);
				} else {
					System.err.println("Warning: Property \"" + name
							+ "\" has non-displayabale editor.  Skipping.");
					continue;
				}

			} catch (InvocationTargetException ex) {
				System.err.println("Skipping property " + name + " ; exception on target: " + ex.getTargetException());
				ex.getTargetException().printStackTrace();
				continue;
			} catch (Exception ex) {
				System.err.println("Skipping property " + name + " ; exception: " + ex);
				ex.printStackTrace();
				continue;
			}
			count++;
			views[i]=view;
			names[i] = name;
			tableComponents.add(new TableComponent(names[i], views[i]));
		}

		TableComponentModel tableComponentModel = new TableComponentModel(tableComponents);
		JTable table = new JTable(tableComponentModel);
		TableCellRenderer tableRenderer = table.getDefaultRenderer(JComponent.class);
		table.setDefaultRenderer(JComponent.class, new JTableComponentRenderer(tableRenderer));
		TableCellEditor tableEditor = table.getDefaultEditor(JComponent.class);
		table.setDefaultEditor(JComponent.class, new JTableComponentEditor(tableEditor));
		table.setRowHeight(30);

		JScrollPane scrollPane = new JScrollPane(table);
		add(scrollPane, BorderLayout.CENTER);

		frame.add(this, BorderLayout.CENTER);
		setVisible(true);

		processEvents = true;


	}

	synchronized void setCustomizer(Customizer c)
	{
		if (c != null)
		{
			c.addPropertyChangeListener(new EditedAdaptor(frame));
		}
	}

	synchronized void wasModified(PropertyChangeEvent evt)
	{
		if(!processEvents)
		{
			return;
		}


		if(evt.getSource() instanceof PropertyEditor)
		{
			PropertyEditor editor = (PropertyEditor) evt.getSource();
			for(int i = 0 ; i < editors.length; i++)
			{
				if (editors[i] == editor)
				{
					PropertyDescriptor property = properties[i];
					Object value = editor.getValue();

					values[i] = value;

					Method setter = property.getWriteMethod();

					if(setter != null)
					{
						try
						{
							Object args[] = { value };
							setter.invoke(target, args);
							targetWrapper.getChangedProperties().addElement(properties[i]);

						}
						catch (InvocationTargetException ex)
						{
							if(ex.getTargetException() instanceof PropertyVetoException)
							{
								System.err.println("WARNING: Vetoed; reason is: "
										+ ex.getTargetException().getMessage());
							}
							else
								error("InvocationTargetException while updating property ["
										+ property.getName()
										+"]", ex.getTargetException());
						}
						catch (Exception ex)
						{
							error("Unexpected exception while updating "
									+ property.getName(), ex);
						}

					}


					break;
				}

			}
		}

		for(int i = 0; i < properties.length; i++)
		{
			Object o;
			try
			{
				o = getPropertyValueByPropertyDescriptor(properties[i]);
			}
			catch(Exception ex)
			{
				o = null;
			}

			if(properties[i] instanceof IndexedPropertyDescriptor)
			{
				if(o == values[i] || areArraysEqual((Object[])values[i], (Object[])o))
				{
					continue;
				}
			}
			else
			{
				if(o == values[i] || (o != null && o.equals(values[i])))
				{
					continue;
				}
			}

			if (editors[i] == null)
			{
				continue;
			}
			editors[i].setValue(o);

			values[i] = o; ///????????????????????
		}

		if(Beans.isInstanceOf(target, Component.class))
		{
			((Component)(Beans.getInstanceOf(target, Component.class))).repaint();
		}
	}

	private void warning(String s)
	{
		new ErrorDialog(frame, "Warning: " + s);
	}


	private void error(String message, Throwable th)
	{
		String mess = message + ":\n" + th;
		new ErrorDialog(frame, mess);
	}
	private Object getPropertyValueByPropertyDescriptor(PropertyDescriptor pd)
			throws InvocationTargetException, IllegalAccessException
	{
		//return null;//stub
		if(
				(pd instanceof IndexedPropertyDescriptor)
						&&
						(((IndexedPropertyDescriptor)pd).getReadMethod() == null)
		)
		{
			return getPropertyValueByIndexedGetter((IndexedPropertyDescriptor)pd);
		}
		else
		{
			Method getter = pd.getReadMethod();
			Object args[] = {};
			return getter.invoke(target, args);
		}
	}
	private Object getPropertyValueByIndexedGetter(IndexedPropertyDescriptor ipd)
			throws InvocationTargetException, IllegalAccessException
	{
		Method indexedGetter = ipd.getIndexedReadMethod();// method surely exists!
		java.util.Vector tmp = null;//new java.util.Vector();
		int index = 0;
		try
		{
			while(true)
			{
				Object[] args = {new Integer(index)};
				Object tmpObject = indexedGetter.invoke(target, args);
				if(tmp == null)
					tmp = new java.util.Vector();
				tmp.addElement(tmpObject);
				index++;
			}
		}
		catch(InvocationTargetException ex)
		{
			Throwable targetException = ((InvocationTargetException)ex).getTargetException();
			if(targetException instanceof NullPointerException)
			{
				return null;
			}
			else
			if(targetException instanceof RuntimeException)
			{
				if(tmp == null)
				{
					return null;
				}
				int count = tmp.size();
				Object[] result = (Object[])Array.newInstance(ipd.getIndexedPropertyType(), count);
				tmp.copyInto(result);
				return result;
			}
			else
			{
				throw ex;
			}
		}
	}
	public static final boolean areArraysEqual(Object[] a1, Object[] a2)
	{
		if(a1 == null)
		{
			if(a2 == null)
				return true;
			else
				return false;
		}
		else
		{ // a1 != null
			if(a2 == null)
				return false;
			else
			{ // a2 != null
				if(a1.length != a2.length)
					return false;
				else
				{
					for(int i = a1.length - 1; i >= 0; i--)
					{
						if(a1[i] == null)
						{
							if(a2[i] == null)
								continue;
							else
								return false;
						}
						else
						{
							if(a1[i].equals(a2[i]))
								continue;
							else
								return false;
						}
					}
					return true;
				}

			}
		}
	}

	private PropertySheet frame;

	private Wrapper targetWrapper;
	private Object target;
	private PropertyDescriptor properties[];
	private PropertyEditor editors[];
	private Object values[];
	private String[] names;
	private JComponent[] views;
	private boolean processEvents;
	private static int hPad = 4;
	private static int vPad = 4;
	private int maxHeight = 500;
	private int maxWidth = 300;

	List<TableComponent> tableComponents = new ArrayList<>();

	static class TableComponent {
		private String property;
		private JComponent view;

		public TableComponent(String property, JComponent view) {
			this.property = property;
			this.view = view;
		}

		public String getProperty() {
			return property;
		}

		public void setProperty(String property) {
			this.property = property;
		}

		public JComponent getView() {
			return view;
		}

		public void setView(JComponent view) {
			this.view = view;
		}
	}

	static class JTableComponentRenderer implements TableCellRenderer {
		private final TableCellRenderer defaultRenderer;

		public JTableComponentRenderer(TableCellRenderer renderer) {
			defaultRenderer = renderer;
		}

		public Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected, boolean hasFocus, int row, int column) {
			if (value instanceof JComponent)
				return (JComponent) value;
			return defaultRenderer.getTableCellRendererComponent(table, value, isSelected, hasFocus, row, column);
		}
	}

	class JTableComponentEditor  extends AbstractCellEditor implements TableCellEditor,  ActionListener {
		TableCellEditor defaulteditor;
		protected static final String EDIT = "edit";

		JComponent view;

		public JTableComponentEditor(TableCellEditor editor) {
			defaulteditor = editor;
		}

		public void actionPerformed(ActionEvent e) {

			System.out.println("Event "+e.toString());
		}

		public Object getCellEditorValue() {
			return view;
		}

		public Component getTableCellEditorComponent(JTable table, Object value, boolean isSelected, int row, int column) {
			if (value instanceof JComponent)
				return (JComponent) value;
			return defaulteditor.getTableCellEditorComponent(table, value, isSelected,  row, column);
		}
	}
	static class TableComponentModel implements TableModel {

		private Set<TableModelListener> listeners = new HashSet<>();

		private List<TableComponent> tableComponents;

		public TableComponentModel(List<TableComponent> tableComponents) {
			this.tableComponents = tableComponents;
		}

		@Override
		public int getRowCount() {
			return tableComponents.size();
		}

		@Override
		public int getColumnCount() {
			return 2;
		}

		@Override
		public String getColumnName(int columnIndex) {
			switch (columnIndex) {
				case 0:
					return "Name";
				case 1:
					return "Editor";
			}
			return "";
		}

		@Override
		public Class<?> getColumnClass(int columnIndex) {
			switch (columnIndex) {
				case 0:
					return String.class;
				case 1:
					return JComponent.class;
			}
			return null;
		}

		@Override
		public boolean isCellEditable(int rowIndex, int columnIndex) {

			if(columnIndex==1)
				return true;
			return false;
		}

		@Override
		public Object getValueAt(int rowIndex, int columnIndex) {
			TableComponent thisTableComponent = tableComponents.get(rowIndex);
			switch (columnIndex) {
				case 0:
					return thisTableComponent.getProperty();
				case 1:
					return thisTableComponent.getView();
			}
			return "";
		}

		@Override
		public void setValueAt(Object aValue, int rowIndex, int columnIndex) {

		}

		@Override
		public void addTableModelListener(TableModelListener listener) {
			listeners.add(listener);
		}

		@Override
		public void removeTableModelListener(TableModelListener listener) {
			listeners.remove(listener);
		}
	}
}