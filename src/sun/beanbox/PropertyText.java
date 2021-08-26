
            // Support for a PropertyEditor that uses text.

            package sun.beanbox;

            import java.awt.*;
            import java.awt.event.*;
            import java.beans.*;
            import javax.swing.*;

            public class PropertyText extends JTextField implements KeyListener, FocusListener {

                public PropertyText(PropertyEditor pe) {
                super(pe.getAsText());
                this.editor = pe;
                addKeyListener(this);
                addFocusListener(this);
                }


                protected void updateEditor() {
                try {
                    editor.setAsText(getText());
                } catch (IllegalArgumentException ex) {
                }
                }
                public void focusGained(FocusEvent e) {
                }

                public void focusLost(FocusEvent e) {
                    updateEditor();
                }
                public void keyReleased(KeyEvent e) {
                if (e.getKeyCode() == KeyEvent.VK_ENTER) {
                    updateEditor();
                    System.out.println("Yes event!");
                }
                }

                public void keyPressed(KeyEvent e) {
                }

                public void keyTyped(KeyEvent e) {
                }

                private PropertyEditor editor;
            }
