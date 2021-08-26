				package sun.beanbox;

				import java.awt.*;
				import java.awt.event.*;
				import java.beans.*;
				import javax.swing.*;

				public class PropertyCanvas extends JButton implements ActionListener
				{
					public
					PropertyCanvas(PropertyEditor pe)
					{
						this.frame = new JFrame();
						editor = pe;
						addActionListener(this);
					}

					public void paint(Graphics g)
					{
						Rectangle box = new Rectangle(2, 2, getSize().width - 4, getSize().height - 4);
						editor.paintValue(g, box);
					}

					private static boolean ignoreClick = false;

					private Frame getFrame()
					{
						Component c = this;
						while((c = c.getParent()) != null)
							if(c instanceof Frame)
								return (Frame)c;
						return null;
					}



					private JFrame frame;
					private PropertyEditor editor;

					@Override
					public void actionPerformed(ActionEvent e) {
						if(frame == null)
						{
						}

						if (! ignoreClick)
						{
							try
							{
								ignoreClick = true;
								int x = frame.getLocation().x - 30;
								int y = frame.getLocation().y + 50;
								new PropertyDialog(frame, editor, x, y);
							}
							finally
							{
								ignoreClick = false;
							}
						}
					}
				}
