package com.paragraph.gerasimov.editors;

import java.beans.*;
import java.awt.*;
import java.awt.event.*;
import javax.swing.*;
import java.lang.reflect.*;
import java.util.Arrays;

import sun.beanbox.*;

public class IndexedPropertyEditor extends JPanel implements PropertyEditor, PropertyChangeListener
{
	//========================= PropoertyEditor interface ======================
	private static final boolean isArraysEqual(Object[] array1, Object[] array2)
	{
//s_dbgOut("isArraysEqual("+array1+", "+array2+") invoked.");
//System.err.println("isArraysEqual("+array1+", "+array2+") invoked.");
		if((array1 == null) || (array2 == null))
			return false;
		else
		{
			boolean result = true;
			if(
					(array1.length != array2.length)
							||
							(array1.getClass().getComponentType() != array2.getClass().getComponentType())
			)
			{
				result = false;
				//return result;
			}
			else
			{
				if((array1.length != 0) && (array2.length != 0))
				{
					for(int i = 0; i < array1.length;  i++)
					{
						if((array1[i] == array2[i]) == false)
						{
							result = false;
							break;
						}
					}
				}
			}
			//s_dbgOut("isArraysEqual() -> "+result);
			return result;
		}
	}

	public void setValue(Object value)
	{
		if(value == null)
		{
			return;
		}
		if(indexedPropertyValue == null)
		{
			indexedPropertyValue = value;
			support.firePropertyChange(null, null, null);
			return;
		}
		if(
				(indexedPropertyValue.getClass().isArray())
						&&
						(value.getClass().isArray())
		)
		{
			if(isArraysEqual((Object[])indexedPropertyValue, (Object[])value))
			{
				for (int i = 0; i < ((Object[]) indexedPropertyValue).length; i++) {
				}
				return;
			}
			else
			{
				indexedPropertyValue = value;
				support.firePropertyChange(null, null, null);
			}
		}
		else
		{
			System.err.println(getClass().getName()+" indexedPropertyValue IS NOT ARRAY.");
		}
	}

	public Object getValue()
	{
//System.err.println(getClass().getName()+".getValue().");
		return indexedPropertyValue;
	}

	public boolean isPaintable()
	{
//System.err.println(getClass().getName()+".isPaintable().");
		return true;
	}
	public void paintValue(java.awt.Graphics gfx, java.awt.Rectangle box)
	{
//System.err.println(getClass().getName()+".paintValue(...).");
		Color c = gfx.getColor();
		gfx.setColor(Color.white);
		gfx.fillRect(box.x, box.y, box.width, box.height);
		gfx.setColor(Color.black);
		gfx.drawString(getAsText(), box.x+3, box.y + box.height/2);
		gfx.setColor(c);
	}

	public String getJavaInitializationString()
	{
//System.err.println(getClass().getName()+".getJavaInitializationString().");
		return ""; // stub
	}

	public String getAsText()
	{
		String strValue = ((indexedPropertyValue == null)?
				"null"
				:
				"["+Array.getLength(indexedPropertyValue)+"]");
		return "Array : "+ strValue;

	}

	public void setAsText(String text) throws java.lang.IllegalArgumentException
	{
//System.out.println(getClass().getName()+".setAsText("+text+").");
	}

	public String[] getTags()
	{
//System.out.println(getClass().getName()+".getTegs().");
		return null; // stub
	}

	public java.awt.Component getCustomEditor()
	{
//System.out.println(getClass().getName()+".getCustomEditor().");
		//removeAll();
		//if(componentTypeEditor == null)
		//{
		isInited = false;
		componentType = indexedPropertyDescriptor.getIndexedPropertyType();
//System.err.println("componentType = "+componentType.getName());
		componentTypeEditor = PropertyEditorManager.findEditor(componentType);

		componentTypeEditor.addPropertyChangeListener(this);

		//------------------------
		Object[] allParentsBeans = wrapper.getAllParentsBeans();
		Class editorClass = componentTypeEditor.getClass();

		// it can be optimized: we need implement initContextInfo(Object o);
		Method initMethod = null;
		Class[] parameterTypes = {Object.class};
		try
		{
			initMethod = editorClass.getMethod("initContextInfo", parameterTypes);
			if(initMethod != null)
			{
				Object[] initArgs = {allParentsBeans};
				initMethod.invoke(componentTypeEditor, initArgs);
			}
		}
		catch(Exception e)
		{
//				System.err.println(editorClass.getName()+": Cannot get initContextInfo(...) method");
		}
		//------------------------

		JLabel labelBean = new JLabel("bean: "+wrapper.getBeanLabel());//"@"+ Integer.toHexString(wrapper.getBean().hashCode()));
		add(labelBean);
		JLabel labelPropertyType = new JLabel("property` "+indexedPropertyDescriptor.getPropertyType().toString());
		add(labelPropertyType);
		valuePanel = new IndexedValuePanel();
		add(valuePanel);
		//}
		valuePanel.init();
		return this;
	}

	public boolean supportsCustomEditor()
	{
//System.out.println(getClass().getName()+".supportsCustomEditor().");
		return true;
	}

	public void addPropertyChangeListener(PropertyChangeListener listener)
	{
//System.out.println(getClass().getName()+".addPropertyChangeListener("+listener+").");
		support.addPropertyChangeListener(listener);
	}

	public void removePropertyChangeListener(PropertyChangeListener listener)
	{
//System.out.println(getClass().getName()+".removePropertyChangeListener("+listener+").");
		support.removePropertyChangeListener(listener);
	}
	//--------------------------- implementation --------------------------
	public Dimension getPreferredSize()
	{
		return new Dimension(400, 330);
	}

	public IndexedPropertyEditor(Wrapper w, IndexedPropertyDescriptor ipd, Frame f)
	{
		//	(JFrame)f.setBackground(Color.lightGray);
		this.frame = f;
		support = new PropertyChangeSupport(this);
		wrapper = w;
		indexedPropertyDescriptor = ipd;
	}

	public Wrapper getWrapper()
	{
		return wrapper;
	}

	// PropertyChangeListener:
	public void propertyChange(PropertyChangeEvent e)
	{
//System.err.println(getClass().getName()+".propertyChange(): e.getSource().getValue() = "+((PropertyEditor)e.getSource()).getValue());
		PropertyEditor cpe = (PropertyEditor)e.getSource();
		componentValue = cpe.getValue();
		Array.set(indexedPropertyValue, componentIndex, componentValue);
		int count = valuePanel.list.getItemCount();
		if( count > 0 && componentIndex < count)
			valuePanel.list.replaceItem(cpe.getAsText(), componentIndex);
		if(isInited)
			setValue(indexedPropertyValue);
	}

	private Frame frame;
	private PropertyChangeSupport support;
	private Wrapper wrapper;
	private IndexedPropertyDescriptor indexedPropertyDescriptor;
	private IndexedValuePanel valuePanel;

	private Class componentType;
	private PropertyEditor componentTypeEditor;

	private Object componentValue;
	private int componentIndex;
	private Object indexedPropertyValue;

	private boolean isInited;

	//---------------------------- internal panel -----------------------------
	class IndexedValuePanel
			extends
			JPanel
			//implements
			//	ItemListener

	{

		IndexedValuePanel()
		{
			//setBackground(Color.white);
			setLayout(new BorderLayout());

			list = new List();
			list.addItemListener( new ItemListener()
								  {
									  public void itemStateChanged(ItemEvent ie)
									  {
//System.err.println("itemStateChanged("+ie+").");
										  Object item = ie.getItem();
//System.err.println("itemStateChanged("+ie+"): item -> "+item+" (class "+item.getClass().getName()+")");
										  //initCurrentItem(((Integer)item).intValue());
										  setCurrentItem(((Integer)item).intValue());
										 //list.replaceItem("null",(Integer)item);
										 //list.select(list.getSelectedIndex());
									  }
								  }
			);
			list.setBackground(Color.white);
			b_newItem = new JButton("Add");
			b_newItem.addActionListener( new ActionListener()
										 {
											 public void actionPerformed(ActionEvent e)
											 {
//System.err.println("actionPerformed("+e.getSource()+").");
												 //Object old = indexedPropertyValue;
												 Object oldValue = getValue();
												 Object newValue = null;
												 System.out.println(Arrays.toString(new Object[]{oldValue})+"-");
												 System.out.println("________________________________________");

												 try
												 {
													 if(oldValue == null || Array.getLength(oldValue) == 0)
													 {
														 newValue = Array.newInstance(componentType, 1);
													 }
													 else
													 {
														 int oldLength = Array.getLength(oldValue);
														 newValue = Array.newInstance(componentType, oldLength + 1);
														 System.arraycopy(oldValue, 0, newValue, 0, oldLength);
													 }
												 }
												 catch(NegativeArraySizeException nase)
												 {
													 System.err.println(getClass().getName()+": Cannot instantiate componentType[]: "+nase);
												 }
												 setValue(newValue);

												 setCurrentItem(list.getItemCount());
												 list.add(componentTypeEditor.getAsText()+"");
												 //list.setBackground(Color.black);
												 list.select(list.getItemCount()-1);

											 }
										 }
			);
			add(list, BorderLayout.CENTER);

			b_remove = new JButton("Remove");
			b_remove.addActionListener( new ActionListener()
										{
											public void actionPerformed(ActionEvent e)
											{
//System.err.println("actionPerformed("+e.getSource()+").");
												int index = list.getSelectedIndex();
												if(index >= 0) {
													list.delItem(index);

												Object oldValue = getValue();
												Object newValue =null;

													System.out.println(Arrays.toString(new Object[]{oldValue})+"-");
													System.out.println("________________________________________");
												try {
													if (oldValue != null || Array.getLength(oldValue) > 0) {
														int oldLength = Array.getLength(oldValue);
														newValue = Array.newInstance(componentType, oldLength - 1);
														System.arraycopy(oldValue, index + 1, newValue, index, oldLength - index - 1);
													}
												}
												catch(NegativeArraySizeException nase)
													{
														System.err.println(getClass().getName()+": Cannot instantiate componentType[]: "+nase);
													}
												setValue(newValue);

											}
												list.select(index-1);
										}
										}
			);
			b_moveUp = new JButton("Move Up");
			b_moveUp.addActionListener( new ActionListener()
										{
											public void actionPerformed(ActionEvent e)
											{
												String selectedItem = list.getSelectedItem();//get item value
												int index = list.getSelectedIndex();// get item index

												if (index > 0) {
													list.remove(index);// remove selected item from the list
													list.add(componentTypeEditor.getAsText(), index - 1);// add the item to a new position in the list

													Object oldValue = getValue();
													Object newValue =null;
													System.out.println(Arrays.toString(new Object[]{oldValue}));
													System.out.println("________________________________________");

													try {
														if (oldValue != null || Array.getLength(oldValue) > 0) {
															int oldLength = Array.getLength(oldValue);
															newValue = Array.newInstance(componentType, oldLength);
															//System.arraycopy(oldValue, index + 1, newValue, index, oldLength - index - 1);
															System.arraycopy(oldValue, index, newValue, index + 1, oldLength - index - 1);
														}
													}
													catch(NegativeArraySizeException nase)
													{
														System.err.println(getClass().getName()+": Cannot instantiate componentType[]: "+nase);
													}
													setValue(newValue);
												}



													list.select(index-1);// set selection to the new item

											}
										}
			);
			b_moveDown = new JButton("Move Down");
			b_moveDown.addActionListener( new ActionListener() {
											  public void actionPerformed(ActionEvent e) {
												  String selectedItem = list.getSelectedItem();//get item value
												  int index = list.getSelectedIndex();// get item index

												  if( index < list.getItemCount() -1 ){
													  list.remove(index);
													  list.add(componentTypeEditor.getAsText(), index + 1);
													  Object oldValue = getValue();
													  Object newValue =null;
													  System.out.println(Arrays.toString(new Object[]{oldValue}));
													  System.out.println("________________________________________");

													  try {
														  if (oldValue != null || Array.getLength(oldValue) > 0) {
															  int oldLength = Array.getLength(oldValue);
															  newValue = Array.newInstance(componentType, oldLength);
															  //System.arraycopy(oldValue, index + 1, newValue, index, oldLength - index - 1);
															  System.arraycopy(oldValue, index, newValue, index + 1, oldLength - index + 1);
														  }
													  }
													  catch(NegativeArraySizeException nase)
													  {
														  System.err.println(getClass().getName()+": Cannot instantiate componentType[]: "+nase);
													  }
													  System.out.println(Arrays.toString(new Object[]{newValue}));
													  setValue(newValue);
													  list.select(index+1);
													  // System.err.println("actionPerformed("+e.getSource()+").");
// }
												  }
											  }
										  }
			);

			//-------------- currentItemPanel ------------------
			JPanel currentItemPanel = new JPanel();
			//currentItemPanel.setBackground(Color.green);
			currentItemPanel.setLayout(new BorderLayout());
			if (componentTypeEditor.isPaintable() && componentTypeEditor.supportsCustomEditor())
			{
				//view = new PropertyCanvas(frame, componentTypeEditor);
				view = new PropertyCanvas(componentTypeEditor);
			}
			else
			if (componentTypeEditor.getTags() != null)
			{
				view = new PropertySelector(componentTypeEditor);
			}
			else
			if (componentTypeEditor.getAsText() != null)
			{
				view = new PropertyText(componentTypeEditor);
			}
			else
			{
				view = new JLabel("<non-displayabale editor>");
			}
			//view.setBackground(Color.lightGray);
			view.setEnabled(false);
			currentItemPanel.add(view, BorderLayout.CENTER);

			add(currentItemPanel, BorderLayout.NORTH);

			JPanel buttonPanel = new JPanel();
			///buttonPanel.setBackground(Color.lightGray);
			GridLayout gbl = new GridLayout();
			buttonPanel.setLayout(gbl);
			buttonPanel.add(b_newItem);
			buttonPanel.add(b_remove);
			buttonPanel.add(b_moveUp);
			buttonPanel.add(b_moveDown);
			add(buttonPanel, BorderLayout.SOUTH);


		}

		private void init()
		{
			if(!isInited)
			{
				if(indexedPropertyValue != null)
				{
//	System.err.println(getClass().getName()+".init(): indexedPropertyValue != null.");
					int count = Array.getLength(indexedPropertyValue);
					if(count > 0)
					{
						list.removeAll();
						for(int i = 0; i < count; ++i)
						{
							setCurrentItem(i);
							String s = componentTypeEditor.getAsText();
							list.add(s);
						}
						//setCurrentItem(0);
					}
				}
				else
				{
					System.err.println(getClass().getName()+".init(): indexedPropertyValue = null.");
				}
			}
			isInited = true;
		}

		private void setCurrentItem(int index)
		{
//System.err.println(getClass().getName()+".setCurrentItem("+index+") invoked.");
			if(index >= 0)
			{
				componentIndex = index;
				componentTypeEditor.setValue(Array.get(indexedPropertyValue, index));

				view.setEnabled(true);
				view.repaint();
				//view.requestFocus();
				list.deselect(list.getSelectedIndex());
				list.select(index);
				list.makeVisible(index);
				list.requestFocus();
			}
			else
			{
				view.setEnabled(false);
			}
		}

		public Dimension getPreferredSize()
		{
			return new Dimension(400, 280);
		}

		JButton b_newItem;
		JButton b_remove;
		JButton b_moveUp;
		JButton b_moveDown;
		List list;
		Component view;

	}
//---------------------------------------------------------------------------

}