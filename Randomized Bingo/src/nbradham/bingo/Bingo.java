package nbradham.bingo;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.GridLayout;
import java.awt.event.ActionListener;
import java.awt.event.KeyEvent;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.io.IOException;
import java.nio.file.Files;
import java.util.Arrays;
import java.util.Collections;
import java.util.Stack;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.swing.JFileChooser;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JMenu;
import javax.swing.JMenuBar;
import javax.swing.JMenuItem;
import javax.swing.JPanel;
import javax.swing.KeyStroke;
import javax.swing.SwingUtilities;
import javax.swing.border.LineBorder;
import javax.swing.filechooser.FileNameExtensionFilter;

/**
 * Handles everything.
 *
 * @author Nickolas S. Bradham
 */
public final class Bingo {

    private final BingoCell[][] cells = new BingoCell[5][5];
    private String[] opts;

    /**
     * Create and show GUI.
     */
    private void start() {
        SwingUtilities.invokeLater(() -> {
            JFrame frame = new JFrame("Bingo!");
            frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
            frame.setLayout(new GridLayout(5, 5));
            JMenuBar bar = new JMenuBar();
            JMenu actMen = new JMenu("Action");
            JFileChooser jfc = new JFileChooser();
            jfc.setDialogTitle("Open Option List");
            jfc.setFileFilter(new FileNameExtensionFilter("Line Seperated Text File", "txt"));
            actMen.add(createItem("Load Opts...", KeyEvent.VK_L, e -> {
                if (jfc.showOpenDialog(frame) == JFileChooser.APPROVE_OPTION) {
                    try {
                        opts = Files.readString(jfc.getSelectedFile().toPath()).split("\n");
                        regen();
                    } catch (IOException ex) {
                        Logger.getLogger(Bingo.class.getName()).log(Level.SEVERE, null, ex);
                    }
                }
            }));
            actMen.add(createItem("Reset", KeyEvent.VK_R, e -> resetAll()));
            actMen.add(createItem("Regenerate", KeyEvent.VK_G, e -> regen()));
            bar.add(actMen);
            frame.setJMenuBar(bar);
            for (byte r = 0; r < cells.length; ++r) {
                for (byte c = 0; c < cells[r].length; ++c) {
                    frame.add((cells[r][c] = new BingoCell()).pane);
                }
            }
            cells[2][2].setCenter();
            frame.pack();
            frame.setVisible(true);
        });
    }

    /**
     * Resets all cells.
     */
    private void resetAll() {
        for (BingoCell[] r : cells) {
            for (BingoCell c : r) {
                c.reset();
            }
        }
    }

    /**
     * Regenerates all cells.
     */
    private void regen() {
        Stack<String> stack = new Stack<>();
        stack.addAll(Arrays.asList(opts));
        Collections.shuffle(stack);
        resetAll();
        for (BingoCell[] r : cells) {
            for (BingoCell c : r) {
                if (c.notCent) {
                    c.setText(stack.pop());
                }
            }
        }
    }

    /**
     * Creates a JMenuItem
     *
     * @param text The text of the item.
     * @param accel The accelerator key to use.
     * @param l The ActionListener to call on click.
     * @return The newly created JMenuItem
     */
    private static JMenuItem createItem(String text, int accel, ActionListener l) {
        JMenuItem i = new JMenuItem(text);
        i.setAccelerator(KeyStroke.getKeyStroke(accel, KeyEvent.CTRL_DOWN_MASK));
        i.addActionListener(l);
        return i;
    }

    /**
     * Constructs and starts a new Bingo instance.
     *
     * @param args Ignored.
     */
    public static void main(String[] args) {
        new Bingo().start();
    }

    /**
     * Handles cell logic.
     */
    private final class BingoCell {

        private static final Color C_UNSEL = Color.DARK_GRAY, C_SEL = Color.MAGENTA;
        private static final LineBorder B_DEF = new LineBorder(Color.BLACK), B_BING = new LineBorder(Color.ORANGE, 10);

        private final JPanel pane = new JPanel(new BorderLayout());
        private final JLabel label = new JLabel("Bingo!", JLabel.CENTER);

        private boolean notCent = true, sel = false;

        /**
         * Constructs a new BingoCell.
         */
        private BingoCell() {
            pane.setPreferredSize(new Dimension(200, 200));
            pane.setBorder(B_DEF);
            pane.setBackground(C_UNSEL);
            pane.addMouseListener(new MouseAdapter() {
                @Override
                public final void mousePressed(MouseEvent e) {
                    if (notCent) {
                        toggle();
                        boolean bing;
                        for (BingoCell[] r : cells) {
                            for (BingoCell c : r) {
                                c.noBing();
                            }
                        }
                        for (byte r = 0; r < cells.length; ++r) {
                            bing = true;
                            for (byte c = 0; bing && c < cells[r].length; ++c) {
                                bing = cells[r][c].sel;
                            }
                            if (bing) {
                                for (byte c = 0; bing && c < cells[r].length; ++c) {
                                    cells[r][c].bing();
                                }
                            }
                        }
                        for (byte c = 0; c < cells.length; ++c) {
                            bing = true;
                            for (byte r = 0; bing && r < cells[0].length; ++r) {
                                bing = cells[r][c].sel;
                            }
                            if (bing) {
                                for (byte r = 0; bing && r < cells[0].length; ++r) {
                                    cells[r][c].bing();
                                }
                            }
                        }
                        bing = true;
                        for (byte x = 0; bing && x < cells[0].length; ++x) {
                            bing = cells[x][x].sel;
                        }
                        if (bing) {
                            for (byte x = 0; bing && x < cells[0].length; ++x) {
                                cells[x][x].bing();
                            }
                        }
                        bing = true;
                        int lm = cells[0].length - 1;
                        for (byte x = 0; bing && x < cells[0].length; ++x) {
                            bing = cells[x][lm - x].sel;
                        }
                        if (bing) {
                            for (byte x = 0; bing && x < cells[0].length; ++x) {
                                cells[x][lm - x].bing();
                            }
                        }
                    }
                }
            });
            label.setForeground(Color.GREEN);
            label.setFont(label.getFont().deriveFont(32f));
            label.setHorizontalAlignment(JLabel.CENTER);
            pane.add(label);
        }

        /**
         * Sets this cell as the center free cell.
         */
        private void setCenter() {
            notCent = false;
            sel = true;
            pane.setBackground(Color.BLUE);
            label.setText("Free!");
        }

        /**
         * Sets the label text.
         *
         * @param text The text to display.
         */
        private void setText(String text) {
            label.setText("<html><style>h1 {text-align: center;}</style><h1>" + text + "</h1></html>");
        }

        /**
         * Resets this cell.
         */
        private void reset() {
            if (sel && notCent) {
                toggle();
            }
            noBing();
        }

        /**
         * Toggles the selection state of this cell.
         */
        private void toggle() {
            pane.setBackground((sel = !sel) ? C_SEL : C_UNSEL);
        }

        /**
         * Marks this cell as part of a bingo.
         */
        private void bing() {
            pane.setBorder(B_BING);
        }

        /**
         * Unmarks this cell from being part of a bingo.
         */
        private void noBing() {
            pane.setBorder(B_DEF);
        }
    }
}
