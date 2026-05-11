import java.awt.*;
import java.awt.event.*;
import java.util.HashSet;
import java.util.Random;
import java.util.random.*;
import javax.swing.*;
import java.io.*;
import javax.sound.sampled.*;

public class PacMan extends JPanel implements ActionListener, KeyListener {
    class Block {
        int x;
        int y;
        int width;
        int height;
        Image image;

        int startX;
        int startY;
        char direction = 'U'; // D , L, R
        int velocityX = 0;
        int velocityY = 0;

        Block(Image image, int x, int y, int width, int height) {
            this.x = x;
            this.y = y;
            this.width = width;
            this.height = height;
            this.startX = x;
            this.startY = y;
            this.image = image;
        }

        void updateDirection(char direction) {
            char prevDirection = this.direction;
            this.direction = direction;
            updateVelocity();
            this.x += this.velocityX;
            this.y += this.velocityY;
            for (Block wall : walls) {
                if (collision(this, wall)) {
                    this.x -= this.velocityX;
                    this.y -= this.velocityY;
                    this.direction = prevDirection;
                    updateVelocity();
                }
            }
        }

        void updateVelocity() {
            if (this.direction == 'U') {
                this.velocityX = 0;
                this.velocityY = -tileSize / 4;
            } else if (this.direction == 'D') {
                this.velocityX = 0;
                this.velocityY = tileSize / 4;
            } else if (this.direction == 'L') {
                this.velocityX = -tileSize / 4;
                this.velocityY = 0;
            } else if (this.direction == 'R') {
                this.velocityX = tileSize / 4;
                this.velocityY = 0;
            }
        }

        void reset() {
            this.x = this.startX;
            this.y = this.startY;
        }
    }

    private int rowCount = 21;
    private int colCount = 19;
    private int tileSize = 32;
    private int boardWidth = colCount * tileSize;
    private int boardHeight = rowCount * tileSize;

    private Image wallImage;
    private Image blueGhostImage;
    private Image pinkGhostImage;
    private Image redGhostImage;
    private Image orangeGhostImage;
    private Image BackgroundImage;
    private Image FoodImage;

    private Image pacmanUpImage;
    private Image pacmanRightImage;
    private Image pacmanLeftImage;
    private Image pacmanDownImage;

    // X = 벽, O = 건너뛰기, P = 플레이어, ' ' = 음식
    // Ghosts: b = blue, o = orange, p = pink, r = red
    private String[] tileMap = {
            "XXXXXXXXXXXXXXXXXXX",
            "X        X        X",
            "X XX XXX X XXX XX X",
            "X                 X",
            "X XX X XXXXX X XX X",
            "X    X       X    X",
            "XXXX XXXX XXXX XXXX",
            "OOOX X       X XOOO",
            "XXXX X XXrXX X XXXX",
            "O       bpo       O",
            "XXXX X XXXXX X XXXX",
            "OOOX X       X XOOO",
            "XXXX X XXXXX X XXXX",
            "X        X        X",
            "X XX XXX X XXX XX X",
            "X  X     P     X  X",
            "XX X X XXXXX X X XX",
            "X    X   X   X    X",
            "X XXXXXX X XXXXXX X",
            "X                 X",
            "XXXXXXXXXXXXXXXXXXX"
    };

    HashSet<Block> walls;
    HashSet<Block> foods;
    HashSet<Block> ghosts;
    Block pacman;

    Timer gameLoop;
    char[] direction = { 'U', 'D', 'L', 'R' }; // 위,아래,왼쪽,오른쪽
    Random random = new Random();
    int score = 0;
    int lives = 3;
    int highScore = 0;
    boolean gameOver = false;

    PacMan() {
        setPreferredSize(new Dimension(boardWidth, boardHeight));
        // setBackground(Color.black);
        addKeyListener(this);
        setFocusable(true);

        // load Images
        BackgroundImage = new ImageIcon(getClass().getResource("/backGround.jpg")).getImage();
        wallImage = new ImageIcon(getClass().getResource("/wall.png")).getImage();
        blueGhostImage = new ImageIcon(getClass().getResource("/blueMushroom.png")).getImage();
        orangeGhostImage = new ImageIcon(getClass().getResource("/slime.png")).getImage();
        pinkGhostImage = new ImageIcon(getClass().getResource("/snail.png")).getImage();
        redGhostImage = new ImageIcon(getClass().getResource("/pig.png")).getImage();
        FoodImage = new ImageIcon(getClass().getResource("/food.gif")).getImage();

        pacmanUpImage = new ImageIcon(getClass().getResource("/pacmanUp.png")).getImage();
        pacmanDownImage = new ImageIcon(getClass().getResource("/pacmanDown.png")).getImage();
        pacmanLeftImage = new ImageIcon(getClass().getResource("/pacmanLeft.png")).getImage();
        pacmanRightImage = new ImageIcon(getClass().getResource("/pacmanRight.png")).getImage();

        loadHighScore();
        loadMap();
        for (Block ghost : ghosts) {
            char newDirection = direction[random.nextInt(4)];
            ghost.updateDirection(newDirection);
        }
        // 타이머를 시작하는 데 걸리는 시간, 프레임 사이에 걸리는 시간(밀리초)
        gameLoop = new Timer(50, this); // 20fps (1000/50)
        gameLoop.start();
    }

    // 최고 점수 로드
    private void loadHighScore() {
        try (BufferedReader reader = new BufferedReader(new FileReader("highscore.txt"))) {
            String line = reader.readLine();
            if (line != null) {
                highScore = Integer.parseInt(line);
            }
        } catch (IOException | NumberFormatException e) {
            // 파일이 없거나 내용이 비어있으면 0으로 유지
            highScore = 0;
        }
    }

    // 최고 점수 저장
    private void saveHighScore() {
        try (PrintWriter writer = new PrintWriter(new FileWriter("highscore.txt"))) {
            writer.println(highScore);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void loadMap() {
        walls = new HashSet<Block>();
        foods = new HashSet<Block>();
        ghosts = new HashSet<Block>();

        for (int r = 0; r < rowCount; r++) {
            for (int c = 0; c < colCount; c++) {
                String row = tileMap[r];
                char tileMapChar = row.charAt(c);

                int x = c * tileSize;
                int y = r * tileSize;

                if (tileMapChar == 'X') { // black wall
                    Block wall = new Block(wallImage, x, y, tileSize, tileSize);
                    walls.add(wall);
                } else if (tileMapChar == 'b') { // blue ghost
                    Block ghost = new Block(blueGhostImage, x, y, tileSize, tileSize);
                    ghosts.add(ghost);
                } else if (tileMapChar == 'o') { // orange ghost
                    Block ghost = new Block(orangeGhostImage, x, y, tileSize, tileSize);
                    ghosts.add(ghost);
                } else if (tileMapChar == 'p') { // pink ghost
                    Block ghost = new Block(pinkGhostImage, x, y, tileSize, tileSize);
                    ghosts.add(ghost);
                } else if (tileMapChar == 'r') { // red ghost
                    Block ghost = new Block(redGhostImage, x, y, tileSize, tileSize);
                    ghosts.add(ghost);
                } else if (tileMapChar == 'P') { // Pacman
                    pacman = new Block(pacmanRightImage, x, y, tileSize, tileSize);
                } else if (tileMapChar == ' ') { // food
                    Block food = new Block(FoodImage, x + 14, y + 14, 15, 15);
                    foods.add(food);
                }
            }
        }
    }

    public void paintComponent(Graphics g) {
        super.paintComponent(g);
        draw(g);
    }

    public void draw(Graphics g) {
        g.drawImage(BackgroundImage, 0, 0, 608, 672, null);
        g.drawImage(pacman.image, pacman.x, pacman.y, pacman.width, pacman.height, null);

        for (Block ghost : ghosts) {
            g.drawImage(ghost.image, ghost.x, ghost.y, ghost.width, ghost.height, null);
        }

        for (Block wall : walls) {
            g.drawImage(wall.image, wall.x, wall.y, wall.width, wall.height, null);
        }

        g.setColor(Color.WHITE);
        for (Block food : foods) {
            // g.fillRect(food.x, food.y, food.width, food.height);
            g.drawImage(food.image,food.x, food.y, food.width, food.height,null);
        }

        // score
        g.setFont(new Font("Arial", Font.PLAIN, 18));
        if (gameOver) {
            g.drawString("Game Over! High Score : " + highScore + " score : " + String.valueOf(score), tileSize / 2,
                    tileSize / 2);
            g.drawString("Press any Key to Restart", tileSize / 2, tileSize + 10);
        } else {
            g.drawString("Live : " + String.valueOf(lives) + " Score : " + String.valueOf(score), tileSize / 2,
                    tileSize / 2);
            g.drawString("HighScore: " + highScore, tileSize * 15, tileSize / 2);
        }
    }

   
    public void playSound(String soundFilePath) {
    try {
        // 사운드 파일 로드 (파일 경로에 맞게 수정 필요)
        java.net.URL soundURL = getClass().getResource("/eatingSound.wav");
        AudioInputStream audioIn = AudioSystem.getAudioInputStream(soundURL);
        Clip clip = AudioSystem.getClip();
        clip.open(audioIn);
        clip.start(); // 재생
    } catch (Exception e) {
        e.printStackTrace();
    }
}

    public void move() {
        pacman.x += pacman.velocityX;
        pacman.y += pacman.velocityY;
        // 팩맨 터널 통과 로직
        if (pacman.x < 0) {
            pacman.x = boardWidth - pacman.width;
        } else if (pacman.x + pacman.width > boardWidth) {
            pacman.x = 0;
        }
        // 충돌 감지 체크
        for (Block wall : walls) {
            if (collision(pacman, wall)) {
                pacman.x -= pacman.velocityX;
                pacman.y -= pacman.velocityY;
                break;
            }
        }

        for (Block ghost : ghosts) {
            // 유령 터널 통과 로직
            if (ghost.x < 0) {
                ghost.x = boardWidth - ghost.width;
            } else if (ghost.x + ghost.width > boardWidth) {
                ghost.x = 0;
            }
            if (collision(pacman, ghost)) {
                lives -= 1;
                if (lives == 0) {
                    gameOver = true;
                    return;
                }
                resetPositions();
            }
            if (ghost.y == tileSize * 9 && ghost.direction != 'U' && ghost.direction != 'D') {
                ghost.updateDirection('U');
            }
            ghost.x += ghost.velocityX;
            ghost.y += ghost.velocityY;
            for (Block wall : walls) {
                if (collision(ghost, wall) || ghost.x <= 0 || ghost.x + ghost.width >= boardWidth) {
                    ghost.x -= ghost.velocityX;
                    ghost.y -= ghost.velocityY;
                    char newDirection = direction[random.nextInt(4)];
                    ghost.updateDirection(newDirection);
                }
            }
        }

        // 음식 충돌 여부 확인
        Block foodeaten = null;
        for (Block food : foods) {
            if (collision(pacman, food)) {
                foodeaten = food;
                score += 10;

               playSound("eatingSound.wav");

                if (score > highScore) {
                    highScore = score;
                    saveHighScore();
                }
            }
        }
        foods.remove(foodeaten);

        if (foods.isEmpty()) {
            loadMap();
            resetPositions();
        }
    }

    public boolean collision(Block a, Block b) {
        return a.x < b.x + b.width &&
                a.x + a.width > b.x &&
                a.y < b.y + b.height &&
                a.y + a.height > b.y;
    }

    public void resetPositions() {
        pacman.reset();
        pacman.velocityX = 0;
        pacman.velocityY = 0;
        for (Block ghost : ghosts) {
            ghost.reset();
            char newDirection = direction[random.nextInt(4)];
            ghost.updateDirection(newDirection);
        }
    }

    @Override
    public void actionPerformed(ActionEvent e) {
        move();
        repaint();
        if (gameOver) {
            gameLoop.stop();
        }
    }

    @Override
    public void keyTyped(KeyEvent e) {

    }

    @Override
    public void keyPressed(KeyEvent e) {

    }

    @Override
    public void keyReleased(KeyEvent e) {
        if (gameOver) {
            loadMap();
            resetPositions();
            lives = 3;
            score = 0;
            gameOver = false;
            gameLoop.start();
        }
        // System.out.println("keyevent : " + e.getKeyCode());
        if (e.getKeyCode() == KeyEvent.VK_UP) {
            pacman.updateDirection('U');
        } else if (e.getKeyCode() == KeyEvent.VK_DOWN) {
            pacman.updateDirection('D');
        } else if (e.getKeyCode() == KeyEvent.VK_LEFT) {
            pacman.updateDirection('L');
        } else if (e.getKeyCode() == KeyEvent.VK_RIGHT) {
            pacman.updateDirection('R');
        }

        if (pacman.direction == 'U') {
            pacman.image = pacmanUpImage;
        } else if (pacman.direction == 'D') {
            pacman.image = pacmanDownImage;
        } else if (pacman.direction == 'L') {
            pacman.image = pacmanLeftImage;
        } else if (pacman.direction == 'R') {
            pacman.image = pacmanRightImage;
        }
    }

}
