import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import java.util.ArrayList;

public class PacmanGame extends JFrame {
    CardLayout cardLayout = new CardLayout();
    JPanel mainPanel = new JPanel(cardLayout);
    LoginPanel loginPanel = new LoginPanel();
    GamePanel gamePanel = new GamePanel();

    public PacmanGame() {
        setTitle("Pac-Man with Powers");
        setDefaultCloseOperation(EXIT_ON_CLOSE);
        setSize(620, 680);
        setLocationRelativeTo(null);
        mainPanel.add(loginPanel, "Login");
        mainPanel.add(gamePanel, "Game");
        add(mainPanel);
        loginPanel.loginButton.addActionListener(e -> {
            String user = loginPanel.usernameField.getText();
            String pass = new String(loginPanel.passwordField.getPassword());
            if (!user.isEmpty() && !pass.isEmpty()) {
                cardLayout.show(mainPanel, "Game");
                SwingUtilities.invokeLater(() -> gamePanel.requestFocusInWindow());
            } else {
                JOptionPane.showMessageDialog(this, "Enter username & password!");
            }
        });
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> new PacmanGame().setVisible(true));
    }

    // ---------- LOGIN PANEL ----------
    static class LoginPanel extends JPanel {
        JTextField usernameField = new JTextField(10);
        JPasswordField passwordField = new JPasswordField(10);
        JButton loginButton = new JButton("Login");

        LoginPanel() {
            setLayout(new GridBagLayout());
            setBackground(Color.BLACK);
            GridBagConstraints gbc = new GridBagConstraints();
            gbc.insets = new Insets(10, 10, 10, 10);

            JLabel title = new JLabel("PAC-MAN");
            title.setFont(new Font("Arial", Font.BOLD, 36));
            title.setForeground(Color.YELLOW);
            gbc.gridx = 0; gbc.gridy = 0; gbc.gridwidth = 2;
            add(title, gbc);

            gbc.gridwidth = 1;
            gbc.gridx = 0; gbc.gridy = 1;
            JLabel ul = new JLabel("Username:");
            ul.setForeground(Color.WHITE);
            add(ul, gbc);
            gbc.gridx = 1;
            add(usernameField, gbc);

            gbc.gridx = 0; gbc.gridy = 2;
            JLabel pl = new JLabel("Password:");
            pl.setForeground(Color.WHITE);
            add(pl, gbc);
            gbc.gridx = 1;
            add(passwordField, gbc);

            gbc.gridy = 3; gbc.gridx = 0; gbc.gridwidth = 2;
            loginButton.setBackground(Color.YELLOW);
            loginButton.setForeground(Color.BLACK);
            loginButton.setFont(new Font("Arial", Font.BOLD, 14));
            add(loginButton, gbc);
        }
    }

    // ---------- GAME PANEL ----------
    static class GamePanel extends JPanel implements ActionListener, KeyListener {

        // --- Maze constants ---
        static final int TILE = 20;
        static final int COLS = 28;
        static final int ROWS = 28;

        // 0 = open/pellet, 1 = wall, 2 = power pellet spawn, 3 = empty (no pellet)
        // Ghost house area uses 3 (empty, no pellets placed there)
        static final int[][] MAP = {
            {1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1},
            {1,0,0,0,0,0,0,0,0,0,0,0,0,1,1,0,0,0,0,0,0,0,0,0,0,0,0,1},
            {1,0,1,1,1,1,0,1,1,1,1,1,0,1,1,0,1,1,1,1,1,0,1,1,1,1,0,1},
            {1,2,1,1,1,1,0,1,1,1,1,1,0,1,1,0,1,1,1,1,1,0,1,1,1,1,2,1},
            {1,0,1,1,1,1,0,1,1,1,1,1,0,1,1,0,1,1,1,1,1,0,1,1,1,1,0,1},
            {1,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,1},
            {1,0,1,1,1,1,0,1,1,0,1,1,1,1,1,1,1,1,0,1,1,0,1,1,1,1,0,1},
            {1,0,1,1,1,1,0,1,1,0,1,1,1,1,1,1,1,1,0,1,1,0,1,1,1,1,0,1},
            {1,0,0,0,0,0,0,1,1,0,0,0,0,1,1,0,0,0,0,1,1,0,0,0,0,0,0,1},
            {1,1,1,1,1,1,0,1,1,1,1,1,3,1,1,3,1,1,1,1,1,0,1,1,1,1,1,1},
            {1,1,1,1,1,1,0,1,1,3,3,3,3,3,3,3,3,3,3,1,1,0,1,1,1,1,1,1},
            {1,1,1,1,1,1,0,1,1,3,1,1,1,3,3,1,1,1,3,1,1,0,1,1,1,1,1,1},
            {1,1,1,1,1,1,0,1,1,3,1,3,3,3,3,3,3,1,3,1,1,0,1,1,1,1,1,1},
            {3,3,3,3,3,3,0,3,3,3,1,3,3,3,3,3,3,1,3,3,3,0,3,3,3,3,3,3},
            {1,1,1,1,1,1,0,1,1,3,1,3,3,3,3,3,3,1,3,1,1,0,1,1,1,1,1,1},
            {1,1,1,1,1,1,0,1,1,3,1,1,1,1,1,1,1,1,3,1,1,0,1,1,1,1,1,1},
            {1,1,1,1,1,1,0,1,1,3,3,3,3,3,3,3,3,3,3,1,1,0,1,1,1,1,1,1},
            {1,1,1,1,1,1,0,1,1,3,1,1,1,1,1,1,1,1,3,1,1,0,1,1,1,1,1,1},
            {1,0,0,0,0,0,0,0,0,0,0,0,0,1,1,0,0,0,0,0,0,0,0,0,0,0,0,1},
            {1,0,1,1,1,1,0,1,1,1,1,1,0,1,1,0,1,1,1,1,1,0,1,1,1,1,0,1},
            {1,0,1,1,1,1,0,1,1,1,1,1,0,1,1,0,1,1,1,1,1,0,1,1,1,1,0,1},
            {1,2,0,0,1,1,0,0,0,0,0,0,0,3,3,0,0,0,0,0,0,0,1,1,0,0,2,1},
            {1,1,1,0,1,1,0,1,1,0,1,1,1,1,1,1,1,1,0,1,1,0,1,1,0,1,1,1},
            {1,1,1,0,1,1,0,1,1,0,1,1,1,1,1,1,1,1,0,1,1,0,1,1,0,1,1,1},
            {1,0,0,0,0,0,0,1,1,0,0,0,0,1,1,0,0,0,0,1,1,0,0,0,0,0,0,1},
            {1,0,1,1,1,1,1,1,1,1,1,1,0,1,1,0,1,1,1,1,1,1,1,1,1,1,0,1},
            {1,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,1},
            {1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1},
        };

        Timer timer = new Timer(50, this);

        // Pacman — spawns at row 26 col 13, the open bottom corridor
        int pacX = 13 * TILE + TILE / 2;
        int pacY = 26 * TILE + TILE / 2;
        int dx = 0, dy = 0;
        int nextDx = 0, nextDy = 0;
        int pacSpeed = 2;
        int mouthAngle = 45;
        int mouthDir = -1;

        // Lives & score
        int lives = 3;
        int score = 0;
        boolean invincible = false;
        long invincibleEnd = 0;

        // Pellets & power pellets
        boolean[][] pellets;
        boolean[][] powerPellets;

        // Power-up state
        boolean ghostVulnerable = false;
        long powerEndTime = 0;

        // Exit waypoints: align X→col13, up to row10, shift X→col12, up to row8
        static final int EXIT_ALIGN_X = 13 * TILE;
        static final int EXIT_ROW10_Y = 10 * TILE;
        static final int EXIT_COL12_X = 12 * TILE;
        static final int EXIT_DONE_Y  =  8 * TILE;

        static class Ghost {
            int x, y, dx, dy;
            Color color;
            int mode; // 0=scatter, 1=chase, 2=frightened
            int scatterCol, scatterRow;
            long modeTimer;
            int lastTileX = -1, lastTileY = -1;
            boolean exiting = true;
            long exitDelay;

            Ghost(int x, int y, Color color, int scatterCol, int scatterRow, long exitDelay) {
                this.x = x; this.y = y;
                this.color = color;
                this.scatterCol = scatterCol;
                this.scatterRow = scatterRow;
                this.mode = 0;
                this.dx = 1; this.dy = 0;
                this.modeTimer = System.currentTimeMillis() + 7000;
                this.exitDelay = exitDelay;
                this.exiting = true;
            }
        }

        ArrayList<Ghost> ghosts = new ArrayList<>();

        GamePanel() {
            setBackground(Color.BLACK);
            setFocusable(true);
            addKeyListener(this);
            // Click anywhere on the panel to reclaim keyboard focus
            addMouseListener(new MouseAdapter() {
                public void mouseClicked(MouseEvent e) { requestFocusInWindow(); }
            });
            initGame();
        }

        void initGame() {
            pellets = new boolean[ROWS][COLS];
            powerPellets = new boolean[ROWS][COLS];
            for (int r = 0; r < ROWS; r++) {
                for (int c = 0; c < COLS; c++) {
                    if (MAP[r][c] == 0) pellets[r][c] = true;
                    if (MAP[r][c] == 2) { powerPellets[r][c] = true; }
                }
            }

            ghosts.clear();
            long now = System.currentTimeMillis();
            // All start inside the ghost house; staggered exit delays
            ghosts.add(new Ghost(13*TILE, 12*TILE, Color.RED,                  COLS-3, 0,        now));          // Blinky  - exits immediately
            ghosts.add(new Ghost(13*TILE, 13*TILE, new Color(255,184,255), 3,          0,        now + 3000));   // Pinky   - 3s
            ghosts.add(new Ghost(11*TILE, 13*TILE, new Color(0,255,255),  COLS-3, ROWS-1,        now + 6000));   // Inky    - 6s
            ghosts.add(new Ghost(15*TILE, 13*TILE, new Color(255,184,82), 0,     ROWS-1,        now + 9000));   // Clyde   - 9s

            timer.start();
        }

        // --- Maze helpers ---
        boolean isWall(int col, int row) {
            if (col < 0 || col >= COLS || row < 0 || row >= ROWS) return true;
            return MAP[row][col] == 1;
        }

        boolean canMove(int pixelX, int pixelY, int ddx, int ddy, int size) {
            int nx = pixelX + ddx * pacSpeed;
            int ny = pixelY + ddy * pacSpeed;
            int margin = 2;
            int left   = (nx - size / 2 + margin) / TILE;
            int right  = (nx + size / 2 - margin) / TILE;
            int top    = (ny - size / 2 + margin) / TILE;
            int bottom = (ny + size / 2 - margin) / TILE;
            return !isWall(left, top) && !isWall(right, top)
                && !isWall(left, bottom) && !isWall(right, bottom);
        }

        boolean canMoveGhost(int px, int py, int ddx, int ddy) {
            int nx = px + ddx * 2;
            int ny = py + ddy * 2;
            int margin = 3;
            int size = TILE;
            int left   = (nx + margin) / TILE;
            int right  = (nx + size - margin) / TILE;
            int top    = (ny + margin) / TILE;
            int bottom = (ny + size - margin) / TILE;
            return !isWall(left, top) && !isWall(right, top)
                && !isWall(left, bottom) && !isWall(right, bottom);
        }

        @Override
        public Dimension getPreferredSize() {
            return new Dimension(COLS * TILE, ROWS * TILE + 40);
        }

        @Override
        public void paintComponent(Graphics g) {
            super.paintComponent(g);
            Graphics2D g2 = (Graphics2D) g;
            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

            // Draw maze
            for (int r = 0; r < ROWS; r++) {
                for (int c = 0; c < COLS; c++) {
                    int px = c * TILE;
                    int py = r * TILE + 40;
                    if (MAP[r][c] == 1) {
                        g2.setColor(new Color(33, 33, 222));
                        g2.fillRect(px, py, TILE, TILE);
                        // Subtle inner border for 3D look
                        g2.setColor(new Color(66, 66, 255));
                        g2.drawRect(px + 1, py + 1, TILE - 2, TILE - 2);
                    }
                    // pellets
                    if (pellets[r][c]) {
                        g2.setColor(Color.WHITE);
                        g2.fillOval(px + TILE / 2 - 2, py + TILE / 2 - 2, 4, 4);
                    }
                    // power pellets
                    if (powerPellets[r][c]) {
                        long t = System.currentTimeMillis();
                        if ((t / 300) % 2 == 0) { // blink
                            g2.setColor(Color.WHITE);
                            g2.fillOval(px + TILE / 2 - 5, py + TILE / 2 - 5, 10, 10);
                        }
                    }
                }
            }

            // Draw ghosts
            for (Ghost gh : ghosts) {
                Color c;
                if (ghostVulnerable) {
                    long remaining = powerEndTime - System.currentTimeMillis();
                    if (remaining < 2000 && (remaining / 300) % 2 == 0)
                        c = Color.WHITE;
                    else
                        c = new Color(0, 0, 180);
                } else {
                    c = gh.color;
                }
                // Ghost body
                g2.setColor(c);
                int gx = gh.x, gy = gh.y + 40;
                g2.fillArc(gx, gy, TILE, TILE, 0, 180);
                g2.fillRect(gx, gy + TILE / 2, TILE, TILE / 2);
                // Wavy bottom
                int waveH = 4;
                int segments = 3;
                int segW = TILE / segments;
                for (int i = 0; i < segments; i++) {
                    g2.setColor(Color.BLACK);
                    g2.fillArc(gx + i * segW, gy + TILE - waveH, segW, waveH * 2, 180, 180);
                }
                // Eyes
                if (!ghostVulnerable) {
                    g2.setColor(Color.WHITE);
                    g2.fillOval(gx + 4, gy + 5, 5, 5);
                    g2.fillOval(gx + 11, gy + 5, 5, 5);
                    g2.setColor(Color.BLUE);
                    g2.fillOval(gx + 5, gy + 6, 3, 3);
                    g2.fillOval(gx + 12, gy + 6, 3, 3);
                } else {
                    // Scared face
                    g2.setColor(Color.WHITE);
                    g2.fillOval(gx + 4, gy + 5, 4, 4);
                    g2.fillOval(gx + 12, gy + 5, 4, 4);
                }
            }

            // Draw Pac-Man
            boolean blinking = invincible && ((System.currentTimeMillis() / 150) % 2 == 0);
            if (!blinking) {
                g2.setColor(Color.YELLOW);
                int facingAngle = 0;
                if (dx == 1) facingAngle = 0;
                else if (dx == -1) facingAngle = 180;
                else if (dy == -1) facingAngle = 90;
                else if (dy == 1) facingAngle = 270;
                int startAngle = facingAngle + mouthAngle / 2;
                int arcAngle = 360 - mouthAngle;
                int pw = 18;
                g2.fillArc(pacX - pw / 2, pacY - pw / 2 + 40, pw, pw, startAngle, arcAngle);
            }

            // HUD
            g2.setColor(Color.BLACK);
            g2.fillRect(0, 0, getWidth(), 40);
            g2.setColor(Color.YELLOW);
            g2.setFont(new Font("Arial", Font.BOLD, 16));
            g2.drawString("SCORE: " + score, 10, 26);

            // Lives
            g2.drawString("LIVES:", 200, 26);
            for (int i = 0; i < lives; i++) {
                int lx = 260 + i * 22;
                g2.setColor(Color.YELLOW);
                g2.fillArc(lx, 11, 16, 16, 30, 300);
            }

            // Power-up indicator
            if (ghostVulnerable) {
                long rem = (powerEndTime - System.currentTimeMillis()) / 1000;
                g2.setColor(Color.CYAN);
                g2.drawString("POWER: " + rem + "s", 370, 26);
            }
        }

        @Override
        public void actionPerformed(ActionEvent e) {
            // Try queued direction first, then keep current
            if (nextDx != dx || nextDy != dy) {
                if (canMove(pacX, pacY, nextDx, nextDy, 16)) {
                    dx = nextDx;
                    dy = nextDy;
                }
            }

            // Move pacman
            if (canMove(pacX, pacY, dx, dy, 16)) {
                pacX += dx * pacSpeed;
                pacY += dy * pacSpeed;
            } else {
                // Nudge toward grid center to allow turns
                int tileCol = pacX / TILE;
                int tileRow = pacY / TILE;
                int centerX = tileCol * TILE + TILE / 2;
                int centerY = tileRow * TILE + TILE / 2;
                if (dx != 0 && Math.abs(pacY - centerY) > 0)
                    pacY += (pacY < centerY) ? 1 : -1;
                if (dy != 0 && Math.abs(pacX - centerX) > 0)
                    pacX += (pacX < centerX) ? 1 : -1;
            }

            // Wrap tunnel (row 13, columns 0 and 27)
            if (pacX < 0) pacX = COLS * TILE - 1;
            if (pacX >= COLS * TILE) pacX = 0;

            // Mouth animation
            mouthAngle += mouthDir * 5;
            if (mouthAngle <= 5 || mouthAngle >= 50) mouthDir = -mouthDir;

            // Eat pellet
            int col = pacX / TILE;
            int row = pacY / TILE;
            if (row >= 0 && row < ROWS && col >= 0 && col < COLS) {
                if (pellets[row][col]) {
                    pellets[row][col] = false;
                    score += 10;
                }
                if (powerPellets[row][col]) {
                    powerPellets[row][col] = false;
                    activatePower();
                }
            }

            // Ghost AI & collision
            updateGhosts();

            // Check invincibility expiry
            if (invincible && System.currentTimeMillis() > invincibleEnd) {
                invincible = false;
            }

            // Check power expiry
            if (ghostVulnerable && System.currentTimeMillis() > powerEndTime) {
                ghostVulnerable = false;
            }

            // Check win
            boolean allEaten = true;
            outer:
            for (int r = 0; r < ROWS; r++) {
                for (int c = 0; c < COLS; c++) {
                    if (pellets[r][c] || powerPellets[r][c]) {
                        allEaten = false;
                        break outer;
                    }
                }
            }
            if (allEaten) {
                timer.stop();
                JOptionPane.showMessageDialog(this, "YOU WIN! Score: " + score);
                System.exit(0);
            }

            repaint();
        }

        void updateGhosts() {
            int pacCol = pacX / TILE;
            int pacRow = pacY / TILE;

            for (Ghost gh : ghosts) {
                // --- Exit-house mode: ignore normal AI until fully out ---
                if (gh.exiting) {
                    if (System.currentTimeMillis() >= gh.exitDelay) {
                        moveExiting(gh);
                    }
                    // Still check collision while exiting (just in case)
                    if (!invincible) {
                        int ex = Math.abs(gh.x + TILE/2 - pacX);
                        int ey = Math.abs(gh.y + TILE/2 - pacY);
                        if (ex < TILE-4 && ey < TILE-4) loseLife();
                    }
                    continue;
                }

                // --- Normal mode toggling ---
                if (!ghostVulnerable && System.currentTimeMillis() > gh.modeTimer) {
                    gh.mode = (gh.mode == 0) ? 1 : 0;
                    gh.modeTimer = System.currentTimeMillis() + (gh.mode == 1 ? 20000 : 7000);
                }
                if (ghostVulnerable) gh.mode = 2;

                // Move 2px per tick
                for (int s = 0; s < 2; s++) {
                    int tileX = gh.x / TILE;
                    int tileY = gh.y / TILE;
                    boolean atBoundary = (gh.x % TILE == 0 && gh.y % TILE == 0);
                    if (atBoundary && (tileX != gh.lastTileX || tileY != gh.lastTileY)) {
                        gh.lastTileX = tileX;
                        gh.lastTileY = tileY;
                        chooseGhostDirection(gh, pacCol, pacRow);
                    }
                    if (!canMoveGhost(gh.x, gh.y, gh.dx, gh.dy))
                        chooseGhostDirection(gh, pacCol, pacRow);
                    if (canMoveGhost(gh.x, gh.y, gh.dx, gh.dy)) {
                        gh.x += gh.dx;
                        gh.y += gh.dy;
                    }
                    if (gh.x < 0) gh.x = (COLS-1)*TILE;
                    if (gh.x >= COLS*TILE) gh.x = 0;
                }

                // Collision
                if (!invincible) {
                    int ex = Math.abs(gh.x + TILE/2 - pacX);
                    int ey = Math.abs(gh.y + TILE/2 - pacY);
                    if (ex < TILE-4 && ey < TILE-4) {
                        if (ghostVulnerable) {
                            // Send back to ghost house
                            gh.x = 13*TILE; gh.y = 13*TILE;
                            gh.exiting = true;
                            gh.exitDelay = System.currentTimeMillis() + 1500;
                            gh.dx = 1; gh.dy = 0;
                            score += 200;
                        } else {
                            loseLife();
                        }
                    }
                }
            }
        }

        /** Moves ghost along the 3-phase exit path out of the ghost house. */
        void moveExiting(Ghost gh) {
            int spd = 2;

            if (gh.y > EXIT_ROW10_Y) {
                // Phase 0: align X to col 13, then climb to row 10
                if (Math.abs(gh.x - EXIT_ALIGN_X) > spd) {
                    gh.x += (gh.x < EXIT_ALIGN_X) ? spd : -spd;
                } else {
                    gh.x = EXIT_ALIGN_X;
                    gh.y -= spd;
                }
            } else if (Math.abs(gh.x - EXIT_COL12_X) > spd) {
                // Phase 1: at row 10, shift left to col 12 (the clear upward path)
                gh.x += (gh.x < EXIT_COL12_X) ? spd : -spd;
            } else {
                // Phase 2: climb out through col 12 → col 12 is open at rows 9 and 8
                gh.x = EXIT_COL12_X;
                gh.y -= spd;
                if (gh.y <= EXIT_DONE_Y) {
                    gh.y = EXIT_DONE_Y;
                    gh.exiting = false;
                    gh.mode = 0;
                    gh.modeTimer = System.currentTimeMillis() + 7000;
                    gh.lastTileX = -1; gh.lastTileY = -1;
                }
            }
        }

        void chooseGhostDirection(Ghost gh, int pacCol, int pacRow) {
            int[][] dirs = {{1,0},{-1,0},{0,1},{0,-1}};
            int targetCol, targetRow;

            if (gh.mode == 2) {
                // Frightened: random
                ArrayList<int[]> valid = new ArrayList<>();
                for (int[] d : dirs) {
                    if (canMoveGhost(gh.x, gh.y, d[0], d[1])) valid.add(d);
                }
                if (!valid.isEmpty()) {
                    int[] chosen = valid.get((int)(Math.random() * valid.size()));
                    gh.dx = chosen[0]; gh.dy = chosen[1];
                }
                return;
            }

            if (gh.mode == 0) {
                // Scatter: head to corner
                targetCol = gh.scatterCol;
                targetRow = gh.scatterRow;
            } else {
                // Chase: target pacman
                targetCol = pacCol;
                targetRow = pacRow;
            }

            // Choose direction that minimizes distance to target (no reversing)
            int bestDist = Integer.MAX_VALUE;
            int bestDx = gh.dx, bestDy = gh.dy;
            for (int[] d : dirs) {
                // Prevent 180-degree reversal
                if (d[0] == -gh.dx && d[1] == -gh.dy) continue;
                if (!canMoveGhost(gh.x, gh.y, d[0], d[1])) continue;
                int nc = gh.x / TILE + d[0];
                int nr = gh.y / TILE + d[1];
                int dist = (nc - targetCol) * (nc - targetCol) + (nr - targetRow) * (nr - targetRow);
                if (dist < bestDist) {
                    bestDist = dist;
                    bestDx = d[0];
                    bestDy = d[1];
                }
            }
            gh.dx = bestDx;
            gh.dy = bestDy;
        }

        void activatePower() {
            ghostVulnerable = true;
            powerEndTime = System.currentTimeMillis() + 7000;
            for (Ghost gh : ghosts) gh.mode = 2;
        }

        void loseLife() {
            lives--;
            if (lives <= 0) {
                timer.stop();
                JOptionPane.showMessageDialog(this, "GAME OVER!\nFinal Score: " + score);
                System.exit(0);
            }
            // Respawn pacman in safe open corridor (row 26, col 13)
            pacX = 13 * TILE + TILE / 2;
            pacY = 26 * TILE + TILE / 2;
            dx = 0; dy = 0; nextDx = 0; nextDy = 0;
            // Invincibility frames
            invincible = true;
            invincibleEnd = System.currentTimeMillis() + 2500;
            // Reset ghosts back into ghost house, staggered exits
            long now2 = System.currentTimeMillis();
            int[] xPos = {13*TILE, 13*TILE, 11*TILE, 15*TILE};
            int[] yPos = {12*TILE, 13*TILE, 13*TILE, 13*TILE};
            for (int i = 0; i < ghosts.size(); i++) {
                Ghost gh = ghosts.get(i);
                gh.x = xPos[i]; gh.y = yPos[i];
                gh.dx = 1; gh.dy = 0;
                gh.mode = 0;
                gh.exiting = true;
                gh.exitDelay = now2 + i * 2000L;
                gh.modeTimer = now2 + 7000;
                gh.lastTileX = -1; gh.lastTileY = -1;
            }
            ghostVulnerable = false;
        }

        @Override
        public void keyPressed(KeyEvent e) {
            switch (e.getKeyCode()) {
                case KeyEvent.VK_LEFT  -> { nextDx = -1; nextDy = 0; }
                case KeyEvent.VK_RIGHT -> { nextDx =  1; nextDy = 0; }
                case KeyEvent.VK_UP    -> { nextDy = -1; nextDx = 0; }
                case KeyEvent.VK_DOWN  -> { nextDy =  1; nextDx = 0; }
                // WASD support
                case KeyEvent.VK_A -> { nextDx = -1; nextDy = 0; }
                case KeyEvent.VK_D -> { nextDx =  1; nextDy = 0; }
                case KeyEvent.VK_W -> { nextDy = -1; nextDx = 0; }
                case KeyEvent.VK_S -> { nextDy =  1; nextDx = 0; }
            }
        }

        @Override
        public void keyReleased(KeyEvent e) {}
        @Override
        public void keyTyped(KeyEvent e) {}
    }
}
