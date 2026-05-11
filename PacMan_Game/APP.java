import javax.swing.JFrame;
public class APP {
    public static void main(String[] args) {
        int rowCount = 21;
        int colCount = 19;
        int tileSize = 32;
        int boardWidth = colCount * tileSize;
        int boardHeight = rowCount * tileSize;

        JFrame frame = new JFrame("PacMan");
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.setSize(boardWidth, boardHeight);
        frame.setResizable(false);
        
        PacMan pacmanGame = new PacMan();
        frame.add(pacmanGame);
        frame.pack();
        frame.setLocationRelativeTo(null);
        frame.setVisible(true);
        pacmanGame.requestFocus();
    }
}