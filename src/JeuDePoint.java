package src;

import java.awt.*;
import java.awt.event.*;
import javax.swing.*;

import java.util.ArrayList;
import java.util.List;

public class JeuDePoint extends JPanel {
    public static void main(String[] args) {
        JFrame window = new JFrame("Jeu de point");
        JeuDePoint jeuDePoint = new JeuDePoint();
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

    private JButton Suggerer_point;
    private JButton Bloquer_adversaire;

    private JButton newGameButton;
    private JButton ContinuerJeuButton;
    private JLabel message;

    int win_r1 = 0, win_c1 = 0, win_r2 = 0, win_c2 = 0;

    public JeuDePoint() {
        setLayout(null);
        setPreferredSize(new Dimension(350, 300));
        setBackground(Color.LIGHT_GRAY);
        Board board = new Board();

        add(Suggerer_point);
        add(Bloquer_adversaire);
        add(board);
        add(newGameButton);
        add(ContinuerJeuButton);
        add(message);

        Suggerer_point.setBounds(200, 250, 120, 30);
        Bloquer_adversaire.setBounds(16, 250, 120, 30);

        board.setBounds(16, 16, 169, 169);
        newGameButton.setBounds(210, 60, 120, 30);
        ContinuerJeuButton.setBounds(210, 120, 120, 30);
        message.setBounds(0, 200, 350, 30);
    }

    public class Board extends JPanel implements ActionListener, MouseListener {
        int[][] board;
        static final int EMPTY = 0,
                WHITE = 1,
                BLACK = 2;
        boolean gameInProgress;
        int currentPlayer;

        public Board() {
            setBackground(Color.LIGHT_GRAY);
            addMouseListener(this);
            ContinuerJeuButton = new JButton("ContinuerJeu");
            ContinuerJeuButton.addActionListener(this);
            newGameButton = new JButton("Nouveau Jeu");
            newGameButton.addActionListener(this);
            message = new JLabel("", JLabel.CENTER);
            message.setFont(new Font("Serif", Font.BOLD, 13));
            message.setForeground(Color.WHITE);

            Suggerer_point = new JButton("Suggerer coup");
            Suggerer_point.addActionListener(this);
            Bloquer_adversaire = new JButton("Bloquer");
            Bloquer_adversaire.addActionListener(this);

            board = new int[13][13];
        }

        public void mouseClicked(MouseEvent e) {
            if (gameInProgress == false) {
                message.setText("Cliquer sur nouveau jeu");
            } else {
                int col = (e.getX()) / 13;
                int row = (e.getY()) / 13;

                if (col >= 0 && col < 13 && row >= 0 && row < 13)
                    doClickSquare(col, row);
            }
        }

        private void doClickSquare(int col, int row) {

            // Jerena raha efa misy point ao anatnle square
            if (board[row][col] != EMPTY) {
                if (currentPlayer == BLACK)
                    message.setText("BLACK:  Please click an empty square.");
                else
                    message.setText("WHITE:  Please click an empty square.");
                return;
            }

            // Initialisena ilay piece aminy alalany tour
            board[row][col] = currentPlayer;
            saveCoordination.save(row, col, currentPlayer);
            repaint();

            if (winner(row, col)) {
                if (currentPlayer == WHITE) {
                    gameOver("White win the game");
                    return;
                } else {
                    gameOver("Black win the game");
                    return;
                }
            }

            // Miova tour
            if (currentPlayer == BLACK) {
                message.setText("WHITE :  Make your move.");
                currentPlayer = WHITE;
            } else {
                message.setText("BLACK :  Make your move.");
                currentPlayer = BLACK;
            }

            // Verifier l'Etat des bouttons
            checkButtonsState();

        }

        private void checkButtonsState() {
            boolean canSuggest = false;
            boolean canBlock = false;

            for (int row = 0; row < 13; row++) {
                for (int col = 0; col < 13; col++) {
                    if (board[row][col] == currentPlayer && hasAlignedPoints(row, col, currentPlayer, 3)) {
                        canSuggest = true;
                        break;
                    }
                }
            }

            int opponent = (currentPlayer == BLACK) ? WHITE : BLACK;
            for (int row = 0; row < 13; row++) {
                for (int col = 0; col < 13; col++) {
                    if (board[row][col] == opponent && hasAlignedPoints(row, col, opponent, 4)) {
                        canBlock = true;
                        break;
                    }
                }
            }

            Suggerer_point.setEnabled(canSuggest);
            Bloquer_adversaire.setEnabled(canBlock);
        }

        private boolean hasAlignedPoints(int row, int col, int player, int targetCount) {
            return (count(player, row, col, 1, 0) == targetCount - 1) ||
                    (count(player, row, col, 0, 1) == targetCount - 1) ||
                    (count(player, row, col, 1, 1) == targetCount - 1) ||
                    (count(player, row, col, 1, -1) == targetCount - 1);
        }

        public void actionPerformed(ActionEvent e) {
            Object src = e.getSource();
            if (src == newGameButton)
                doNewGame();
            else if (src == ContinuerJeuButton)
                ContinuerJeu();
            else if (src == Suggerer_point)
                Suggerer_point();
            else if (src == Bloquer_adversaire)
                Bloquer_adversaire();
        }

        private void doNewGame() {

            for (int row = 0; row < 13; row++)
                for (int col = 0; col < 13; col++)
                    board[row][col] = EMPTY;

            currentPlayer = BLACK;

            gameInProgress = true;

            newGameButton.setEnabled(true);
            ContinuerJeuButton.setEnabled(true);

            saveCoordination.resetSave();

            repaint();
        }

        public void paintComponent(Graphics g) {

            super.paintComponent(g);

            // Les carreaux dans le board
            g.setColor(Color.DARK_GRAY);
            for (int i = 1; i < 13; i++) {
                g.drawLine(3 + 13 * i, 0, 3 + 13 * i, getSize().height); // Colonne
                g.drawLine(0, 4 + 13 * i, getSize().width, 4 + 13 * i); // Ligne
            }

            //
            for (int row = 0; row < 13; row++)
                for (int col = 0; col < 13; col++)
                    if (board[row][col] != EMPTY)
                        drawPiece(g, board[row][col], row, col);

            // if (win_r1 >= 0)
            //     drawWinLine(g);

        }

        // private void drawWinLine(Graphics g) {
        //     g.setColor(Color.RED);
        //     g.drawLine(8 + 13 * win_c1, 8 + 13 * win_r1, 8 + 13 * win_c2, 8 + 13 * win_r2);
        //     if (win_r1 == win_r2)
        //         g.drawLine(8 + 13 * win_c1, 7 + 13 * win_r1, 8 + 13 * win_c2, 7 + 13 * win_r2);
        //     else
        //         g.drawLine(7 + 13 * win_c1, 8 + 13 * win_r1, 7 + 13 * win_c2, 8 + 13 * win_r2);
        // }

        // Methode pour colorier les pieces
        private void drawPiece(Graphics g, int piece, int row, int col) {
            if (piece == WHITE)
                g.setColor(Color.WHITE);
            else
                g.setColor(Color.BLACK);
            g.fillOval( 13 * col, 13 * row, 8, 8);
        }

        private boolean winner(int row, int col) {
            boolean isWinner = false;

            if (count(board[row][col], row, col, 1, 0) >= 4) {
                return true;
            } else if (count(board[row][col], row, col, 0, 1) >= 4) {
                return true;
            } else if (count(board[row][col], row, col, 1, 1) >= 4) {
                return true;
            } else if (count(board[row][col], row, col, 1, -1) >= 4) {
                return true;
            }

            return isWinner;
        }

        public int count(int player, int rowTest, int colTest, int dirX, int dirY) {

            int ct = 0;

            int r, c;

            r = rowTest + dirX;
            c = colTest + dirY;

            while (r >= 0 && r < 13 && c >= 0 && c < 13 && board[r][c] == player) {
                ct++;
                r += dirX;
                c += dirY;
            }

            win_r1 = r - dirX;
            win_c1 = c - dirY;

            r = rowTest - dirX;
            c = colTest - dirY;

            while (r >= 0 && r < 13 && c >= 0 && c < 13 && board[r][c] == player) {
                ct++;
                r -= dirX;
                c -= dirY;
            }

            win_r2 = rowTest + dirX;
            win_c2 = colTest + dirY;

            // message.setText(Integer.toString(ct));

            return ct;
        }

        void gameOver(String str) {
            message.setText(str);
            newGameButton.setEnabled(true);
            ContinuerJeuButton.setEnabled(false);

            Suggerer_point.setEnabled(false);
            Bloquer_adversaire.setEnabled(false);

            gameInProgress = false;
        }

        private void ContinuerJeu() {
            List<int[]> savedMoves = saveCoordination.load();
            int lastPlayer = 0;
            for (int[] move : savedMoves) {
                int row = move[0];
                int col = move[1];
                int player = move[2];
                board[row][col] = player;
                lastPlayer = player;
            }

            currentPlayer = lastPlayer;
            gameInProgress = true;

            repaint();
            message.setText("Partie chargée. C'est au tour de " + (currentPlayer == BLACK ? "BLACK" : "WHITE"));
        }

        private void Bloquer_adversaire() {
            int[] bestMove = findWorstMoveForOpponent();
            board[bestMove[0]][bestMove[1]] = currentPlayer;
            repaint();

            // Miova tour
            if (currentPlayer == BLACK) {
                message.setText("WHITE :  Make your move.");
                currentPlayer = WHITE;
            } else {
                message.setText("BLACK :  Make your move.");
                currentPlayer = BLACK;
            }
        }

        private int[] findWorstMoveForOpponent() {
            List<int[]> possibleMoves = getPossibleMoves();
            int[] bestMove = null;

            for (int[] move : possibleMoves) {
                int row = move[0];
                int col = move[1];

                if (blocksAlignment(row, col, currentPlayer, 5)) {
                    return move;
                }

            }

            return bestMove != null ? bestMove : possibleMoves.get(0);
        }

        private void Suggerer_point() {
            int[] bestMove = findBestMoveWithPriority();
            board[bestMove[0]][bestMove[1]] = currentPlayer;
            repaint();

            if (winner(bestMove[0], bestMove[1])) {
                if (currentPlayer == WHITE) {
                    gameOver("White win the game");
                    return;
                } else {
                    gameOver("Black win the game");
                    return;
                }
            }

            // Miova tour
            if (currentPlayer == BLACK) {
                message.setText("WHITE :  Make your move.");
                currentPlayer = WHITE;
            } else {
                message.setText("BLACK :  Make your move.");
                currentPlayer = BLACK;
            }

        }

        private int[] countWithRange(int player, int row, int col, int dirX, int dirY) {
            int count = 0;

            // Compter les pions dans une direction
            int r = row + dirX;
            int c = col + dirY;
            while (r >= 0 && r < 13 && c >= 0 && c < 13 && board[r][c] == player) {
                count++;
                r += dirX;
                c += dirY;
            }
            int end1Row = r - dirX;
            int end1Col = c - dirY;

            // Compter les pions dans l'autre direction
            r = row - dirX;
            c = col - dirY;
            while (r >= 0 && r < 13 && c >= 0 && c < 13 && board[r][c] == player) {
                count++;
                r -= dirX;
                c -= dirY;
            }
            int end2Row = r + dirX;
            int end2Col = c + dirY;

            return new int[] { count, end1Row, end1Col, end2Row, end2Col };
        }

        private boolean completesContinuousAlignment(int row, int col, int player, int target) {
            int[][] directions = { { 1, 0 }, { 0, 1 }, { 1, 1 }, { 1, -1 } };
            for (int[] dir : directions) {
                int[] result = countWithRange(player, row, col, dir[0], dir[1]);
                int count = result[0];

                if (count >= target - 1) {
                    return true;
                }
            }
            return false;
        }

        public boolean isEmpty(int end1Row, int end1Col) {
            return board[end1Row][end1Col] == EMPTY;
        }

        private int[] findBestMoveWithPriority() {
            List<int[]> possibleMoves = getPossibleMoves();
            int[] bestMove = null;

            for (int[] move : possibleMoves) {
                int row = move[0];
                int col = move[1];

                // Priorité 1 : Gagner immédiatement
                if (createsAlignment(row, col, currentPlayer, 5)) {
                    return move;
                }

                // Priorité 2 : Bloquer un alignement adverse
                if (blocksAlignment(row, col, currentPlayer, 5)) {
                    return move;
                }

                // Priorité 3 : Étendre un alignement de 3
                if (createsAlignment(row, col, currentPlayer, 4)) {
                    bestMove = move;
                }

                // if (createsAlignment(row, col, currentPlayer, 3)) {
                // bestMove = move;
                // }

                // if (createsAlignment(row, col, currentPlayer, 2)) {
                // bestMove = move;
                // }

            }

            // Si aucune priorité élevée n'est trouvée, retourner un coup au hasard
            return bestMove != null ? bestMove : possibleMoves.get(0);
        }

        private boolean createsAlignment(int row, int col, int player, int target) {
            board[row][col] = player; // Simuler le coup
            boolean result = completesContinuousAlignment(row, col, player, target);
            board[row][col] = EMPTY; // Annuler le coup
            return result;
        }

        private boolean blocksAlignment(int row, int col, int player, int target) {
            int opponent = (player == BLACK) ? WHITE : BLACK;
            board[row][col] = opponent;
            boolean result = hasAlignedPoints(row, col, opponent, target);
            board[row][col] = EMPTY;
            return result;
        }

        private List<int[]> getPossibleMoves() {
            List<int[]> moves = new ArrayList<>();
            for (int row = 0; row < 13; row++) { // Parcourir toutes les lignes
                for (int col = 0; col < 13; col++) { // Parcourir toutes les colonnes
                    if (board[row][col] == EMPTY) { // Vérifier si la case est vide
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
