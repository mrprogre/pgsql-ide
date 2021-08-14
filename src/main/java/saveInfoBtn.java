import javax.swing.*;
import javax.swing.table.TableCellEditor;
import javax.swing.table.TableCellRenderer;
import javax.swing.table.TableColumnModel;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

class saveInfoBtn extends AbstractCellEditor implements TableCellRenderer, TableCellEditor, ActionListener {
    JTable table;
    JButton renderButton;
    JButton editButton;
    String text;

    public saveInfoBtn(JTable table, int column) {
        super();
        this.table = table;
        renderButton = new JButton();

        editButton = new JButton();
        editButton.setFocusPainted(false);
        editButton.addActionListener(this);

        TableColumnModel columnModel = table.getColumnModel();
        columnModel.getColumn(column).setCellRenderer(this);
        columnModel.getColumn(column).setCellEditor(this);
    }

    public Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected, boolean hasFocus, int row, int column) {
        ImageIcon sumIcon1 = new ImageIcon(Toolkit.getDefaultToolkit().createImage(getClass().getResource("icons/save.png")));
        ImageIcon leftIcon = new ImageIcon(Toolkit.getDefaultToolkit().createImage(getClass().getResource("icons/left.png")));
        if (hasFocus) {
            renderButton.setForeground(table.getForeground());
            renderButton.setBackground(UIManager.getColor("Button.background"));
        } else if (isSelected) {
            renderButton.setIcon(leftIcon);
        } else {
            renderButton.setIcon(sumIcon1);
        }
        return renderButton;
    }

    public Component getTableCellEditorComponent(JTable table, Object value, boolean isSelected, int row, int column) {
        text = (value == null) ? "?" : value.toString();
        editButton.setText(text);
        return editButton;
    }

    public Object getCellEditorValue() {
        return text;
    }

    public void actionPerformed(ActionEvent e) {
        Common c = new Common();
        int rowIdx = Gui.executeTable.getSelectedRow();
        String currentTable = Gui.executeTable.getValueAt(rowIdx, 2).toString();
        c.deleteFromFavorites(currentTable + ":info");
        String tabInfo;
        try {
            tabInfo = Gui.executeTable.getValueAt(rowIdx, 6).toString();
            assert tabInfo != null;
            if (tabInfo.length() > 0) {
                c.writeToConfig(currentTable + ":info", tabInfo);
            }
        } catch (NullPointerException ignored) {
        }
        fireEditingStopped();
    }
}