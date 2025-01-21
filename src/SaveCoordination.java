package src;


import java.io.*;
import java.util.ArrayList;
import java.util.List;

public class SaveCoordination {
    private static final String SAVE_FILE = "game_save.txt";

    public void save(int row, int col, int player) {
        try (BufferedWriter writer = new BufferedWriter(new FileWriter(SAVE_FILE, true))) {
            writer.write(row + "," + col + "," + player);
            writer.newLine();
        } catch (IOException e) {
            System.out.println("Erreur lors de la sauvegarde : " + e.getMessage());
        }
    }

    public List<int[]> load() {
        List<int[]> moves = new ArrayList<>();
        try (BufferedReader reader = new BufferedReader(new FileReader(SAVE_FILE))) {
            String line;
            while ((line = reader.readLine()) != null) {
                String[] parts = line.split(",");
                if (parts.length == 3) {
                    int row = Integer.parseInt(parts[0]);
                    int col = Integer.parseInt(parts[1]);
                    int player = Integer.parseInt(parts[2]);
                    moves.add(new int[]{row, col, player});
                }
            }
        } catch (IOException e) {
            System.out.println("Erreur lors du chargement : " + e.getMessage());
        }
        return moves;
    }

    public void resetSave() {
        try {
            new PrintWriter(SAVE_FILE).close();
        } catch (IOException e) {
            System.out.println("Erreur lors de la réinitialisation : " + e.getMessage());
        }
    }
}
