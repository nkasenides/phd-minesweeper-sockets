package ui.component;

import javax.swing.*;
import javax.swing.border.Border;
import javax.swing.border.LineBorder;
import java.awt.*;

public class MinesweeperButton extends JButton {

    public static final String RESOURCE_FOLDER = "res/";
    public static final ImageIcon MINE;
    public static final ImageIcon FLAG;
    public static final ImageIcon ONE;
    public static final ImageIcon TWO;
    public static final ImageIcon THREE;
    public static final ImageIcon FOUR;
    public static final ImageIcon FIVE;
    public static final ImageIcon SIX;
    public static final ImageIcon SEVEN;
    public static final ImageIcon EIGHT;

    public static final int ICON_SIZE = 64;

    public static final Border BUTTON_BORDER = new LineBorder(Color.BLACK, 1);

    static {
        MINE = new ImageIcon(RESOURCE_FOLDER + "mine.png");
        MINE.setImage(MINE.getImage().getScaledInstance(ICON_SIZE, ICON_SIZE, Image.SCALE_SMOOTH));

        FLAG = new ImageIcon(RESOURCE_FOLDER + "flag.png");
        FLAG.setImage(FLAG.getImage().getScaledInstance(ICON_SIZE, ICON_SIZE, Image.SCALE_SMOOTH));

        ONE = new ImageIcon(RESOURCE_FOLDER + "one.png");
        ONE.setImage(ONE.getImage().getScaledInstance(ICON_SIZE, ICON_SIZE, Image.SCALE_SMOOTH));

        TWO = new ImageIcon(RESOURCE_FOLDER + "two.png");
        TWO.setImage(TWO.getImage().getScaledInstance(ICON_SIZE, ICON_SIZE, Image.SCALE_SMOOTH));

        THREE = new ImageIcon(RESOURCE_FOLDER + "three.png");
        THREE.setImage(THREE.getImage().getScaledInstance(ICON_SIZE, ICON_SIZE, Image.SCALE_SMOOTH));

        FOUR = new ImageIcon(RESOURCE_FOLDER + "four.png");
        FOUR.setImage(FOUR.getImage().getScaledInstance(ICON_SIZE, ICON_SIZE, Image.SCALE_SMOOTH));

        FIVE = new ImageIcon(RESOURCE_FOLDER + "five.png");
        FIVE.setImage(FIVE.getImage().getScaledInstance(ICON_SIZE, ICON_SIZE, Image.SCALE_SMOOTH));

        SIX = new ImageIcon(RESOURCE_FOLDER + "six.png");
        SIX.setImage(SIX.getImage().getScaledInstance(ICON_SIZE, ICON_SIZE, Image.SCALE_SMOOTH));

        SEVEN = new ImageIcon(RESOURCE_FOLDER + "seven.png");
        SEVEN.setImage(SEVEN.getImage().getScaledInstance(ICON_SIZE, ICON_SIZE, Image.SCALE_SMOOTH));

        EIGHT = new ImageIcon(RESOURCE_FOLDER + "eight.png");
        EIGHT.setImage(EIGHT.getImage().getScaledInstance(ICON_SIZE, ICON_SIZE, Image.SCALE_SMOOTH));
    }

    public MinesweeperButton() {
        setContentAreaFilled(false);
        setOpaque(true);
        setBorder(MinesweeperButton.BUTTON_BORDER);
        setBackground(Color.BLACK);
    }

}
