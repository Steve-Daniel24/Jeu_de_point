import java.awt.*;
import java.awt.event.*;
import javax.swing.*;
import java.util.ArrayList;
import java.util.List;

public class JeuDepointClean extends JPanel {
    public static void main(String[] args) {
        JFrame window = new JFrame("Jeu de point");
        JeuDepointClean jeuDePoint = new JeuDepointClean();
        window.setContentPane(jeuDePoint);
        window.pack();
        Dimension screensize = Toolkit.getDefaultToolkit().getScreenSize();
        window.setLocation((screensize.width - window.getWidth()) / 2,
                (screensize.height - window.getHeight()) / 2);
        window.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        window.setResizable(false);
        window.setVisible(true);
    }

    private SaveCoordination saveCoordination = new SaveCoordination();

    private JButton suggestMoveButton;
    private JButton blockOpponentButton;
    private JButton newGameButton;
    private JButton continueGameButton;
    private JLabel message;

    private static final int MAX_POINTS_PER_PLAYER = 6;
    private static final int BOARD_SIZE = 17;
    private static final int WINNING_POINTS = 4;

    private int[][] playerPoints = new int[2][MAX_POINTS_PER_PLAYER];
    private int[] pointCounts = new int[2];

    private static final int[][] WIN_CONDITIONS = {
            { 0, 1, 1, 0 }, { 1, 0, 0, 1 }, { 1, 1, 1, -1 }, { 1, -1, 1, 1 }
    };

    private static final int[] REQUIRED_COUNTS = { 3, 1, 2 };

    public JeuDepointClean() {
        setLayout(null);
        setPreferredSize(new Dimension(750, 700));
        setBackground(Color.white);
        Board board = new Board();

        add(suggestMoveButton);
        add(blockOpponentButton);
        add(board);
        add(newGameButton);
        add(continueGameButton);
        add(message);

        suggestMoveButton.setBounds(300, 600, 120, 30);
        blockOpponentButton.setBounds(100, 600, 120, 30);
        board.setBounds(BOARD_SIZE, BOARD_SIZE, 500, 500);
        newGameButton.setBounds(600, 60, 120, 30);
        continueGameButton.setBounds(600, 120, 120, 30);
        message.setBounds(100, 650, 350, 30);

        resetPlayerPoints();
    }

    private void resetPlayerPoints() {
        for (int i = 0; i < 2; i++) {
            pointCounts[i] = 0;
            for (int j = 0; j < MAX_POINTS_PER_PLAYER; j++) {
                playerPoints[i][j] = -1;
            }
        }
    }

    public class Board extends JPanel implements ActionListener, MouseListener {
        private static final int EMPTY = 0, RED = 1, BLUE = 2;
        private int[][] board;
        private boolean gameInProgress;
        private int currentPlayer;

        public Board() {
            setBackground(Color.white);
            addMouseListener(this);
            continueGameButton = new JButton("ContinuerJeu");
            continueGameButton.addActionListener(this);
            newGameButton = new JButton("Nouveau Jeu");
            newGameButton.addActionListener(this);
            message = new JLabel("", JLabel.CENTER);
            message.setFont(new Font("Serif", Font.BOLD, BOARD_SIZE));
            message.setForeground(Color.black);

            suggestMoveButton = new JButton("Suggerer coup");
            suggestMoveButton.addActionListener(this);
            blockOpponentButton = new JButton("Bloquer");
            blockOpponentButton.addActionListener(this);

            board = new int[BOARD_SIZE][BOARD_SIZE];
        }

        public void mouseClicked(MouseEvent e) {
            if (!gameInProgress) {
                message.setText("Cliquer sur nouveau jeu");
            } else {
                int col = e.getX() / 30;
                int row = e.getY() / 30;
                if (col >= 0 && col < BOARD_SIZE && row >= 0 && row < BOARD_SIZE) {
                    doClickSquare(col, row);
                }
            }
        }

        private void doClickSquare(int col, int row) {
            if (board[row][col] != EMPTY) {
                message.setText(currentPlayer == BLUE ? "BLUE:  Please click an empty square."
                        : "RED:  Please click an empty square.");
                return;
            }

            board[row][col] = currentPlayer;
            saveCoordination.save(row, col, currentPlayer);
            repaint();

            if (isWinningMove("click", row, col)) {
                gameOver(currentPlayer == RED ? "RED win the game" : "BLUE win the game");
                return;
            }

            currentPlayer = (currentPlayer == BLUE) ? RED : BLUE;
            message.setText((currentPlayer == BLUE ? "BLUE" : "RED") + " :  Make your move.");
            checkButtonsState();
        }

        private boolean isWinningMove(String mouvement, int row, int col) {
            int player = board[row][col];
            for (int[] condition : WIN_CONDITIONS) {
                if (checkLShape(mouvement, row, col, player, condition[0], condition[1], condition[2], condition[3]) ||
                        checkLShapeSquare(mouvement, row, col, player, condition[0], condition[1], condition[2],
                                condition[3])) {
                    return true;
                }
            }

            return false;
        }

        private boolean checkLShapeGeneral(int row, int col, int player, int firstDirX, int firstDirY, int secondDirX,
                int secondDirY, boolean isSquare) {
            for (int requiredCount : REQUIRED_COUNTS) {
                int countFirst = countInPosSense(player, row, col, firstDirX, firstDirY);
                int countFirstNeg = countInNegSense(player, row, col, firstDirX, firstDirY);

                if (countFirst >= requiredCount || countFirstNeg >= requiredCount) {
                    int countFinal = (countFirst >= requiredCount) ? countFirst : -countFirstNeg;

                    int endRow = 0;
                    int endCol = 0;

                    if (!isSquare) {
                        endRow = row + countFinal * firstDirX;
                        endCol = col + countFinal * firstDirY;
                    } else {
                        endRow = row;
                        endCol = col;
                    }

                    if (isValid(endRow, endCol) && board[endRow][endCol] == player) {
                        int countSecond = countInPosSense(player, endRow, endCol, secondDirX, secondDirY);
                        int countSecondNeg = countInNegSense(player, endRow, endCol, secondDirX, secondDirY);

                        if ((countSecond >= (WINNING_POINTS - requiredCount)
                                || countSecondNeg >= (WINNING_POINTS - requiredCount))
                                && !isTShape(row, col, endRow, endCol, firstDirX, firstDirY, secondDirX, secondDirY)) {
                            return true;
                        }
                    }
                }
            }
            return false;
        }

        private boolean checkLShape(String mouvement, int row, int col, int player, int firstDirX, int firstDirY,
                int secondDirX, int secondDirY) {
            return checkLShapeGeneral(row, col, player, firstDirX, firstDirY, secondDirX, secondDirY, false);
        }

        private boolean checkLShapeSquare(String mouvement, int row, int col, int player, int firstDirX, int firstDirY,
                int secondDirX, int secondDirY) {
            return checkLShapeGeneral(row, col, player, firstDirX, firstDirY, secondDirX, secondDirY, true);
        }

        private boolean isTShape(int startRow, int startCol, int endRow, int endCol, int firstDirX, int firstDirY,
                int secondDirX, int secondDirY) {
            return (Math.abs(firstDirX) == Math.abs(secondDirX) && Math.abs(firstDirY) == Math.abs(secondDirY));
        }

        private boolean isValid(int row, int col) {
            return row >= 0 && row < BOARD_SIZE && col >= 0 && col < BOARD_SIZE;
        }

        public int countInPosSense(int player, int rowTest, int colTest, int dirX, int dirY) {
            int ct = 0;
            int r = rowTest + dirX;
            int c = colTest + dirY;
            while (r >= 0 && r < BOARD_SIZE && c >= 0 && c < BOARD_SIZE && board[r][c] == player) {
                ct++;
                r += dirX;
                c += dirY;
            }
            return ct;
        }

        public int countInNegSense(int player, int rowTest, int colTest, int dirX, int dirY) {
            int ct = 0;
            int r = rowTest - dirX, c = colTest - dirY;
            while (r >= 0 && r < BOARD_SIZE && c >= 0 && c < BOARD_SIZE && board[r][c] == player) {
                ct++;
                r -= dirX;
                c -= dirY;
            }
            return ct;
        }

        private void checkButtonsState() {
            int opponent = (currentPlayer == BLUE) ? RED : BLUE;
            boolean canSuggest = canBlock(currentPlayer);
            boolean canBlock = canBlock(opponent);
            for (int row = 0; row < BOARD_SIZE; row++) {
                for (int col = 0; col < BOARD_SIZE; col++) {
                    if (board[row][col] == currentPlayer && hasAlignedPoints(row, col, currentPlayer, 3)) {
                        canSuggest = true;
                        break;
                    }
                }
            }
            suggestMoveButton.setEnabled(canSuggest);
            blockOpponentButton.setEnabled(canBlock);
        }

        private boolean canBlock(int player) {
            for (int row = 0; row < BOARD_SIZE; row++) {
                for (int col = 0; col < BOARD_SIZE; col++) {
                    if (board[row][col] == EMPTY) {
                        board[row][col] = player;
                        boolean playerWins = false;
                        for (int[] condition : WIN_CONDITIONS) {
                            int countPostOne = countInPosSense(player, row, col, condition[0], condition[1]);
                            int countPostTwo = countInPosSense(player, row, col, condition[0], condition[1]);
                            int countNegOne = countInNegSense(player, row, col, condition[2], condition[3]);
                            int countNegTwo = countInNegSense(player, row, col, condition[2], condition[3]);

                            if (checkLShape(
                                    "suggest", row, col, player, condition[0], condition[1], condition[2], condition[3])
                                    ||
                                    checkLShapeSquare("suggest", row, col, player, condition[0], condition[1],
                                            condition[2],
                                            condition[3])
                                    || countPostOne >= 3 || countPostTwo >= 3 || countNegOne >= 3 || countNegTwo >= 3) {
                                playerWins = true;
                                break;
                            }
                        }
                        board[row][col] = EMPTY;
                        if (playerWins) {
                            return true;
                        }
                    }
                }
            }
            return false;
        }

        private boolean hasAlignedPoints(int row, int col, int player, int targetCount) {
            return (countInPosSense(player, row, col, 1, 0) >= targetCount - 1) ||
                    (countInPosSense(player, row, col, 0, 1) >= targetCount - 1) ||
                    (countInPosSense(player, row, col, 1, 1) >= targetCount - 1) ||
                    (countInPosSense(player, row, col, 1, -1) >= targetCount - 1);
        }

        public void actionPerformed(ActionEvent e) {
            Object src = e.getSource();
            if (src == newGameButton) {
                doNewGame();
            } else if (src == continueGameButton) {
                continueGame();
            } else if (src == suggestMoveButton) {
                suggestMove();
            } else if (src == blockOpponentButton) {
                blockOpponent();
            }
        }

        private void doNewGame() {
            for (int row = 0; row < BOARD_SIZE; row++) {
                for (int col = 0; col < BOARD_SIZE; col++) {
                    board[row][col] = EMPTY;
                }
            }
            currentPlayer = BLUE;
            gameInProgress = true;
            newGameButton.setEnabled(true);
            continueGameButton.setEnabled(true);
            saveCoordination.resetSave();
            resetPlayerPoints();
            repaint();
        }

        public void paintComponent(Graphics g) {
            super.paintComponent(g);
            g.setColor(Color.DARK_GRAY);
            int cellSize = 30;
            for (int i = 1; i < BOARD_SIZE; i++) {
                g.drawLine(3 + cellSize * i, 0, 3 + cellSize * i, getSize().height);
                g.drawLine(0, 4 + cellSize * i, getSize().width, 4 + cellSize * i);
            }
            for (int row = 0; row < BOARD_SIZE; row++) {
                for (int col = 0; col < BOARD_SIZE; col++) {
                    if (board[row][col] != EMPTY) {
                        drawPiece(cellSize, g, board[row][col], row, col);
                    }
                }
            }
        }

        private void drawPiece(int cellSize, Graphics g, int piece, int row, int col) {
            g.setColor(piece == RED ? Color.RED : Color.BLUE);
            g.fillOval(cellSize * col, cellSize * row, 8, 8);
        }

        private void gameOver(String str) {
            message.setText(str);
            newGameButton.setEnabled(true);
            continueGameButton.setEnabled(false);
            suggestMoveButton.setEnabled(false);
            blockOpponentButton.setEnabled(false);
            gameInProgress = false;
        }

        private void continueGame() {
            List<int[]> savedMoves = saveCoordination.load();
            int lastPlayer = 0;
            for (int[] move : savedMoves) {
                int row = move[0];
                int col = move[1];
                int player = move[2];
                board[row][col] = player;
                lastPlayer = player;
            }
            currentPlayer = (lastPlayer == BLUE) ? RED : BLUE;
            gameInProgress = true;
            repaint();
            message.setText("Partie chargée. C'est au tour de " + (currentPlayer == BLUE ? "BLUE" : "RED"));
        }

        private void blockOpponent() {
            int opponent = (currentPlayer == BLUE) ? RED : BLUE;
            int[] bestMove = createsAlignment(opponent, WINNING_POINTS);
            board[bestMove[0]][bestMove[1]] = currentPlayer;
            repaint();
            currentPlayer = (currentPlayer == BLUE) ? RED : BLUE;
            message.setText((currentPlayer == BLUE ? "BLUE" : "RED") + " :  Make your move.");
        }

        private void suggestMove() {
            int opponent = (currentPlayer == BLUE) ? RED : BLUE;

            int[] bestMove = createsAlignment(currentPlayer, WINNING_POINTS);
            if (bestMove[0] != 0 && bestMove[1] != 0) {
                System.out.println("Je peux gagner, je joue ce coup !");
            } else {
                bestMove = createsAlignment(opponent, WINNING_POINTS);
                if (bestMove[0] != 0 && bestMove[1] != 0) {
                    System.out.println("L'adversaire peut gagner, je bloque !");
                } else {
                    bestMove = createsAlignment(currentPlayer, WINNING_POINTS - 1);
                    if (bestMove[0] != 0 && bestMove[1] != 0) {
                        System.out.println("Je prépare mon alignement !");
                    } else {
                        List<int[]> possibleMoves = getPossibleMoves();
                        if (!possibleMoves.isEmpty()) {
                            bestMove = findBestStrategicMove(possibleMoves, currentPlayer);
                            System.out.println("Je joue un coup stratégique !");
                        } else {
                            return;
                        }
                    }
                }
            }

            if (bestMove != null) {
                board[bestMove[0]][bestMove[1]] = currentPlayer;
                repaint();
                if (isWinningMove("suggest", bestMove[0], bestMove[1])) {
                    gameOver(currentPlayer == RED ? "RED wins the game" : "BLUE wins the game");
                    return;
                }
                currentPlayer = (currentPlayer == BLUE) ? RED : BLUE;
                message.setText((currentPlayer == BLUE ? "BLUE" : "RED") + " : Make your move.");
            }
        }

        private int[] findBestStrategicMove(List<int[]> possibleMoves, int player) {
            int[] bestMove = null;
            int maxAlignment = 0;

            for (int[] move : possibleMoves) {
                int row = move[0];
                int col = move[1];

                board[row][col] = player;
                int alignmentScore = countMaxAlignment(row, col, player);
                board[row][col] = EMPTY;

                if (alignmentScore > maxAlignment) {
                    maxAlignment = alignmentScore;
                    bestMove = move;
                }
            }

            return (bestMove != null) ? bestMove : possibleMoves.get(0);
        }

        private int countMaxAlignment(int row, int col, int player) {
            int maxCount = 0;
            int[][] directions = { { 1, 0 }, { 0, 1 }, { 1, 1 }, { 1, -1 } }; 

            for (int[] dir : directions) {
                int count = countInPosSense(player, row, col, dir[0], dir[1]) +
                        countInNegSense(player, row, col, dir[0], dir[1]) + 1;
                if (count > maxCount) {
                    maxCount = count;
                }
            }

            return maxCount;
        }

        private int[] createsAlignment(int player, int target) {
            List<int[]> possibleMoves = getPossibleMoves();
            int[] bestMove = null;
            for (int[] move : possibleMoves) {
                int row = move[0];
                int col = move[1];
                board[row][col] = player;
                boolean wins = false;
                for (int[] condition : WIN_CONDITIONS) {
                    if (checkLShape("suggest", row, col, player, condition[0], condition[1], condition[2], condition[3])
                            ||
                            checkLShapeSquare("suggest", row, col, player, condition[0], condition[1], condition[2],
                                    condition[3])) {
                        wins = true;
                        break;
                    }
                }
                board[row][col] = EMPTY;
                if (wins) {
                    return move;
                }
                if (bestMove == null) {
                    bestMove = move;
                }
            }
            return bestMove;
        }

        private List<int[]> getPossibleMoves() {
            List<int[]> moves = new ArrayList<>();
            for (int row = 0; row < BOARD_SIZE; row++) {
                for (int col = 0; col < BOARD_SIZE; col++) {
                    if (board[row][col] == EMPTY) {
                        moves.add(new int[] { row, col });
                    }
                }
            }
            return moves;
        }

        public void mousePressed(MouseEvent e) {
        }

        public void mouseReleased(MouseEvent e) {
        }

        public void mouseEntered(MouseEvent e) {
        }

        public void mouseExited(MouseEvent e) {
        }
    }
}
