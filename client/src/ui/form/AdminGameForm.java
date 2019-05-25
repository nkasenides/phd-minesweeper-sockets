package ui.form;

import clients.AdminClient;
import model.*;
import ui.component.MinesweeperButton;
import javax.swing.*;
import java.awt.*;
import java.awt.event.KeyEvent;
import java.awt.event.WindowEvent;

public class AdminGameForm extends JFrame {

    //UI
    private JPanel gamePanel;
    private MinesweeperButton[][] buttons;
    private static final int WINDOW_SIZE = 800;

    //Other
    private AdminClient client;

    public AdminGameForm(AdminClient client) {

        //Initialize UI:
        setTitle("View Game | Minesweeper");
        this.client = client;
        this.buttons = new MinesweeperButton[client.getPartialStatePreference().getWidth()][client.getPartialStatePreference().getHeight()];
        setSize(WINDOW_SIZE, WINDOW_SIZE);
        setLocationRelativeTo(null);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
//        setExtendedState(JFrame.MAXIMIZED_BOTH);
//        setUndecorated(true);

        //Set key listener:
        KeyEventDispatcher keyEventDispatcher = new KeyEventDispatcher() {
            @Override
            public boolean dispatchKeyEvent(final KeyEvent e) {
                if (isActive()) {
                    if (e.getID() == KeyEvent.KEY_PRESSED) {
                        Direction direction = null;
                        System.out.println("Key press -> " + e.getKeyCode());
                        switch (e.getKeyCode()) {
                            case KeyEvent.VK_UP:
                                if (client.xShift - 1 >= 0) {
                                    direction = Direction.UP;
                                    client.xShift--;
                                }
                                break;
                            case KeyEvent.VK_DOWN:
                                if (client.xShift + client.getPartialStatePreference().getWidth() < client.getGameWidth()) {
                                    direction = Direction.DOWN;
                                    client.xShift++;
                                }
                                break;
                            case KeyEvent.VK_LEFT:
                                if (client.yShift - 1 >= 0) {
                                    direction = Direction.LEFT;
                                    client.yShift--;
                                }
                                break;
                            case KeyEvent.VK_RIGHT:
                                if (client.yShift + client.getPartialStatePreference().getHeight() < client.getGameHeight()) {
                                    direction = Direction.RIGHT;
                                    client.yShift++;
                                }
                                break;
                        }
//                        System.out.println("cX: " + client.xShift + ", cY: " + client.yShift);
                        if (direction != null) {
                            client.viewGame(client.xShift, client.yShift);
                        }
                    }
                }

                return false;
            }
        };
        KeyboardFocusManager.getCurrentKeyboardFocusManager().addKeyEventDispatcher(keyEventDispatcher);

    }

    public void initialize() {
        gamePanel = new JPanel();
        gamePanel.setLayout(new GridLayout(client.getPartialStatePreference().getWidth(), client.getPartialStatePreference().getHeight()));
        for (int x = 0; x < client.getPartialStatePreference().getWidth(); x++) {
            for (int y = 0; y < client.getPartialStatePreference().getHeight(); y++) {
                MinesweeperButton button = new MinesweeperButton();
                buttons[x][y] = button;
                gamePanel.add(button);
            }
        }
        add(gamePanel);

        updateButtons();
        setVisible(true);
        MinesweeperButton.setIconSize(buttons[0][0].getWidth() - 8);
        MinesweeperButton.resizeIcons();
    }

    public void update() {

        System.out.println("gameForm.update() -> " + client.getGameState());

        switch (client.getGameState()) {
            case NOT_STARTED:
                JOptionPane.showMessageDialog(null, "Game not started", "Error", JOptionPane.WARNING_MESSAGE);
                dispatchEvent(new WindowEvent(this, WindowEvent.WINDOW_CLOSING));
                break;
            case STARTED:
            case ENDED_WON:
            case ENDED_LOST:
                updateButtons();
                break;
        }
    }

    private void updateButtons() {
        for (int x = 0; x < client.getPartialBoardState().getWidth(); x++) {
            for (int y = 0; y < client.getPartialBoardState().getHeight(); y++) {

                //Set background:
                switch (client.getGameState()) {
                    case NOT_STARTED:
                        break;
                    case STARTED:
                        buttons[x][y].setBackground(Color.LIGHT_GRAY);
                        //Set the background of blank revealed cells to a darker gray:
                        if (client.getPartialBoardState().getCells()[x][y].getRevealState() == RevealState.REVEALED_0) {
                            buttons[x][y].setBackground(Color.GRAY);
                        }
                        break;
                    case ENDED_WON:
                        buttons[x][y].setBackground(Color.GREEN);
                        break;
                    case ENDED_LOST:
                        buttons[x][y].setBackground(Color.RED);
                        break;
                }

                //Set the icon:
                switch (client.getPartialBoardState().getCells()[x][y].getRevealState()) {
                    case COVERED:
                    case REVEALED_0:
                        buttons[x][y].setIcon(null);
                        break;
                    case FLAGGED:
                        buttons[x][y].setIcon(MinesweeperButton.FLAG);
                        break;
                    case REVEALED_1:
                        buttons[x][y].setIcon(MinesweeperButton.ONE);
                        break;
                    case REVEALED_2:
                        buttons[x][y].setIcon(MinesweeperButton.TWO);
                        break;
                    case REVEALED_3:
                        buttons[x][y].setIcon(MinesweeperButton.THREE);
                        break;
                    case REVEALED_4:
                        buttons[x][y].setIcon(MinesweeperButton.FOUR);
                        break;
                    case REVEALED_5:
                        buttons[x][y].setIcon(MinesweeperButton.FIVE);
                        break;
                    case REVEALED_6:
                        buttons[x][y].setIcon(MinesweeperButton.SIX);
                        break;
                    case REVEALED_7:
                        buttons[x][y].setIcon(MinesweeperButton.SEVEN);
                        break;
                    case REVEALED_8:
                        buttons[x][y].setIcon(MinesweeperButton.EIGHT);
                        break;
                    case REVEALED_MINE:
                        buttons[x][y].setIcon(MinesweeperButton.MINE);
                        break;
                }

            }
        }
    }

}