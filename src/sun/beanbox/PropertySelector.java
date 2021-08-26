			package sun.beanbox;

			import java.awt.*;
			import java.awt.event.*;
			import java.beans.*;
			import javax.swing.*;

			public class PropertySelector extends JComboBox implements ItemListener
			{

				public
				PropertySelector(PropertyEditor pe)
				{
					editor = pe;
					String tags[] = editor.getTags();
					for (int i = 0; i < tags.length; i++)
					{
						addItem(tags[i]);
					}
					setSelectedIndex(0);
					getModel().setSelectedItem(editor.getAsText());
					addItemListener(this);
				}

				public void itemStateChanged(ItemEvent evt)
				{
					String s = getModel().getSelectedItem().toString();
					editor.setAsText(s);
					getModel().setSelectedItem(s);
				}

				PropertyEditor editor;
			}
