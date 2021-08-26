package sun.beanbox;

import javax.swing.*;
import java.beans.PropertyEditor;
import java.text.DecimalFormat;
        import java.awt.Component;
        import javax.swing.table.TableCellRenderer;
        import javax.swing.table.DefaultTableCellRenderer;

public class EditorRenderer extends PropertyCanvas implements TableCellRenderer {
    PropertyEditor editor;
    public EditorRenderer(PropertyEditor pe) {
        super(pe);
        editor=pe;
    }

    public Component getTableCellRendererComponent(JTable table,
                                                   Object value, boolean isSelected, boolean hasFocus, int row,
                                                   int col) {
        DefaultTableCellRenderer renderer =
                new DefaultTableCellRenderer();
        Component c = renderer.getTableCellRendererComponent(table,
                value, isSelected, hasFocus, row, col);
        String s = "";
        if (col == 1) {
            Component view =new PropertyCanvas(editor);
            c = renderer.getTableCellRendererComponent(table, view,
                    isSelected, hasFocus, row, col);
        }
        return c;
    }
}
