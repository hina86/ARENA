package visualization.helpers;

import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.util.HashMap;
import java.util.Map;
import javax.swing.*;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;

public class LabelList {

    private final Map<String, Color> labelMap;

    public LabelList(String [] nameList) {
        labelMap = createImageMap(nameList);
        JList list = new JList(nameList);
        list.setCellRenderer(new ColorLabelListRenderer());

        JScrollPane scroll = new JScrollPane(list);
        scroll.setPreferredSize(new Dimension(300, 400));

        JFrame frame = new JFrame();
        frame.add(scroll);
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.pack();
        frame.setLocationRelativeTo(null);
        frame.setVisible(true);
    }

    public class ColorLabelListRenderer extends DefaultListCellRenderer {

        @Override
        public Component getListCellRendererComponent(
                JList list, Object value, int index,
                boolean isSelected, boolean cellHasFocus) {
            JLabel label = (JLabel) super.getListCellRendererComponent(
                    list, value, index, isSelected, cellHasFocus);
            label.setBackground(labelMap.get(value));
            label.setHorizontalTextPosition(JLabel.RIGHT);
            label.addMouseListener(new MouseAdapter() {
                @Override
                public void mouseClicked(MouseEvent e) {
                    JColorChooser colorChooser = new JColorChooser(Color.BLACK); // default color is black
                    colorChooser.setBorder(null);
                    colorChooser.getSelectionModel().addChangeListener(new ChangeListener() {
                        public void stateChanged(ChangeEvent e) {
                            Color color= colorChooser.getSelectionModel().getSelectedColor();
                            label.setBackground(color);
                            labelMap.put((String)value, color);
                        }
                    });
                }

            });
            return label;
        }
    }

    private Map<String, Color> createImageMap(String[] list) {
        Map<String, Color> map = new HashMap<>();
        try {
            for (int i=0; i< list.length; i++) {
                JLabel jLabel = new JLabel();
                int rgb = Color.HSBtoRGB(i*5,0.5f,0.5f);
                Color color = new Color(rgb);
                jLabel.setBackground(color);
                map.put(list[i], color);
            }
        } catch (Exception ex) {
            ex.printStackTrace();
        }
        return map;
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> {
            String[] list = {"net.pot8os.kotlintestsample", "org.secuso.privacyfriendlysudoku"};
            new LabelList(list);
        });
    }
}