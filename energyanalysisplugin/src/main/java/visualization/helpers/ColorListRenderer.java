package visualization.helpers;

import javax.swing.*;
import javax.swing.border.Border;
import java.awt.*;

public class ColorListRenderer extends DefaultListCellRenderer {
    private final JLabel jlblCell = new JLabel(" ", JLabel.LEFT);
    Border lineBorder = BorderFactory.createLineBorder(Color.WHITE, 3);
    @Override
    public Component getListCellRendererComponent(
            JList list, Object value, int index,
            boolean isSelected, boolean cellHasFocus) {
        jlblCell.setOpaque(true);
        jlblCell.setBackground((Color) value);
        jlblCell.setText("");
        jlblCell.setBorder(lineBorder);
        return jlblCell;
    }
}