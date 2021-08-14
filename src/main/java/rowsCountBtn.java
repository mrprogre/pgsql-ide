import javax.swing.*;
import javax.swing.table.TableCellEditor;
import javax.swing.table.TableCellRenderer;
import javax.swing.table.TableColumnModel;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

class rowsCountBtn extends AbstractCellEditor implements TableCellRenderer, TableCellEditor, ActionListener {
    int rowCountInTable;
    JTable table;
    JButton renderButton;
    JButton editButton;
    String text;

    public rowsCountBtn(JTable table, int column) {
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
        ImageIcon sumIcon1 = new ImageIcon(Toolkit.getDefaultToolkit().createImage(getClass().getResource("icons/sum1.png")));
        ImageIcon sumIcon2 = new ImageIcon(Toolkit.getDefaultToolkit().createImage(getClass().getResource("icons/sum2.png")));
        if (hasFocus) {
            //renderButton.setText(String.valueOf(rowCountInTable));
            renderButton.setForeground(table.getForeground());
            renderButton.setBackground(UIManager.getColor("Button.background"));
        } else if (isSelected) {
            //renderButton.setText(String.valueOf(rowCountInTable));
            //renderButton.setForeground(table.getSelectionForeground());
            //renderButton.setBackground(table.getSelectionBackground());
            renderButton.setIcon(sumIcon2);
        } else {
            //renderButton.setText("rows");
            //renderButton.setForeground(table.getForeground());
            //renderButton.setBackground(UIManager.getColor("Button.background"));
            renderButton.setIcon(sumIcon1);
        }
        //renderButton.setText((value == null) ? ";" : value.toString() );
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
        Pg pg = new Pg();
        int row = Gui.executeTable.getSelectedRow();
        String table = (String) Gui.executeTable.getValueAt(row, 1);
        rowCountInTable = pg.rowsInTable(table);
        String s = String.format("%,d", rowCountInTable);
        Gui.executeTable.setValueAt(s, row, 4);
        fireEditingStopped();
    }
}