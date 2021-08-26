
// Support for PropertyEditor with custom editors.

package sun.beanbox;

import java.awt.*;
import java.awt.event.*;
import java.beans.*;
import javax.swing.*;

class PropertyDialog extends JDialog implements ActionListener
{

	private JButton doneButton;
	private Component body;


	PropertyDialog(Frame frame, PropertyEditor pe, int x, int y)
	{
		super(frame, pe.getClass().getName(), true);
		new WindowCloser(this);

		setLayout(new BorderLayout());
		body = pe.getCustomEditor();
		JPanel bodyPanel = new JPanel();
		bodyPanel.add(body,BorderLayout.CENTER);
		//bodyPanel.setBackground(Color.RED);
		add(bodyPanel,BorderLayout.CENTER);

		JPanel buttonPanel = new JPanel();
		doneButton = new JButton("Done");
		doneButton.addActionListener(this);
		buttonPanel.add(doneButton,BorderLayout.CENTER);
		add(buttonPanel, BorderLayout.SOUTH);

		int dwidth = this.getWidth();
		int dheight = this.getHeight();



		setSize(dwidth+50,dheight+20);
		setLocation(x, y);
		this.pack();
		setVisible(true);
	}
	public void actionPerformed(ActionEvent evt)
	{
		// Button down.
		dispose();
	}


}
