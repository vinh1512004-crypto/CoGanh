package coganh;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Font;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.awt.event.MouseWheelEvent;
import java.awt.event.MouseWheelListener;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.awt.image.BufferedImage;
import java.util.ArrayList;
import javax.imageio.ImageIO;
import javax.swing.JFrame;
import javax.swing.JMenu;
import javax.swing.JMenuBar;
import javax.swing.JMenuItem;
import javax.swing.JPanel;

public class GamePanel_CoGanh extends JPanel {
    private static final long serialVersionUID = 1L;
    public static final int SCREEN_WIDTH = 1000;
    public static final int SCREEN_HEIGHT = 600;

    public BufferedImage background_paint, board_paint, on, off, hdImg, left, right, back, undoIcon, logoImg, hoTroIcon;
    public BufferedImage[] hd = new BufferedImage[3];
    private BufferedImage boardImg, background, backgr;

    public GameController gc;
    public JMenuBar menuBar;
    public JMenu categoryMenu, utilityMenu, settingsMenu;

    public boolean start;
    public GameSound music = new GameSound();
    public boolean soundOn = true;

    public GamePanel_CoGanh() {
        loadImage();

        JFrame mainFrame = new JFrame();
        if (logoImg != null) {
            mainFrame.setIconImage(logoImg);
        }
        mainFrame.setTitle("Cờ Gánh Việt Nam");
        mainFrame.setLayout(new BorderLayout());

        this.addMouseListener(new MouseControl(this));
        this.addMouseWheelListener(new MouseWheelListener() {
            @Override
            public void mouseWheelMoved(MouseWheelEvent e) {
                if (gc != null && gc.xemLichSu && gc.trangLS == 0) {
                    gc.cuonLichSu += e.getWheelRotation() * 30;
                    if (gc.cuonLichSu < 0) gc.cuonLichSu = 0;
                    int totalGames = gc.sm.getTotalGames();
                    int maxScroll = Math.max(0, (totalGames * 30) - 360);
                    if (gc.cuonLichSu > maxScroll) gc.cuonLichSu = maxScroll;
                    veMenu();
                }
            }
        });

        // Khởi tạo Thanh Menu (MenuBar)
        menuBar = new JMenuBar();
        menuBar.setBackground(new Color(60, 0, 120)); // Màu tím đậm
        menuBar.setBorderPainted(false);

        Color menuTextColor = Color.WHITE;
        Font menuFont = new Font("Times New Roman", Font.BOLD, 14);

        // --- Menu THỂ LOẠI (PvP & PvE) ---
        categoryMenu = new JMenu("Thể loại");
        categoryMenu.setForeground(menuTextColor);
        categoryMenu.setFont(menuFont);

        JMenuItem pvpItem = new JMenuItem("Phòng PvP (Người vs Người)");
        JMenu pveSubMenu = new JMenu("Phòng PvE (Người vs Máy)");
        
        JMenuItem pveEasyItem = new JMenuItem("Easy");
        JMenuItem pveMedItem = new JMenuItem("Medium");
        JMenuItem pveHardItem = new JMenuItem("Hard");

        pvpItem.addActionListener(e -> gc.kiemTraVaVaoGame(false));
        pveEasyItem.addActionListener(e -> { gc.doKhoAI = 1; gc.kiemTraVaVaoGame(true); });
        pveMedItem.addActionListener(e -> { gc.doKhoAI = 2; gc.kiemTraVaVaoGame(true); });
        pveHardItem.addActionListener(e -> { gc.doKhoAI = 4; gc.kiemTraVaVaoGame(true); });

        pveSubMenu.add(pveEasyItem);
        pveSubMenu.add(pveMedItem);
        pveSubMenu.add(pveHardItem);

        categoryMenu.add(pvpItem);
        categoryMenu.add(pveSubMenu);

        // --- Menu TIỆN ÍCH ---
        utilityMenu = new JMenu("Tiện ích");
        utilityMenu.setForeground(menuTextColor);
        utilityMenu.setFont(menuFont);

        JMenuItem hdItem = new JMenuItem("Hướng dẫn");
        JMenuItem lsItem = new JMenuItem("Xem lịch sử đấu");
        JMenuItem undoItem = new JMenuItem("Đi lại (Undo)");
        JMenuItem hintItem = new JMenuItem("Gợi ý (Hint)");

        hdItem.addActionListener(e -> { 
            if (hd[0] != null) hdImg = hd[0];
            gc.huongDan = true; 
            veMenu(); 
        });
        lsItem.addActionListener(e -> { gc.xemLichSu = true; gc.trangLS = 0; gc.cuonLichSu = 0; veMenu(); });
        undoItem.addActionListener(e -> gc.undoMove());
        hintItem.addActionListener(e -> gc.goiYNuocDi());

        utilityMenu.add(hdItem);
        utilityMenu.add(lsItem);

        // --- Menu CÀI ĐẶT ---
        settingsMenu = new JMenu("Cài đặt");
        settingsMenu.setForeground(menuTextColor);
        settingsMenu.setFont(menuFont);

        JMenuItem soundItem = new JMenuItem("Âm thanh (Bật/Tắt)");
        JMenuItem exitItem = new JMenuItem("Thoát");
        soundItem.addActionListener(e -> {
            soundOn = !soundOn;
            if (!soundOn) music.stopMusic();
            else music.batDau();
            repaint();
        });
        exitItem.addActionListener(e -> System.exit(0));

        settingsMenu.add(soundItem);
        settingsMenu.addSeparator();
        settingsMenu.add(exitItem);

        menuBar.add(categoryMenu);
        menuBar.add(utilityMenu);
        menuBar.add(settingsMenu);

        gc = new GameController(this);

        mainFrame.setJMenuBar(menuBar);
        mainFrame.add(this, BorderLayout.CENTER);

        // Không cần tăng chiều cao frame nhiều vì setJMenuBar không chiếm chỗ trong content pane
        mainFrame.setSize(SCREEN_WIDTH + 15, SCREEN_HEIGHT + 37 + 25);
        
        this.repaint();
        mainFrame.setLocationRelativeTo(null);
        mainFrame.setResizable(true);
        mainFrame.setVisible(true);

        music.batDau();

        mainFrame.setDefaultCloseOperation(JFrame.DO_NOTHING_ON_CLOSE);
        mainFrame.addWindowListener(new WindowAdapter() {
            @Override
            public void windowClosing(WindowEvent e) {
                if (start && gc != null && gc.end == 0) {
                    gc.saveGame();
                }
                System.exit(0);
            }
        });
        mainFrame.setResizable(true);
        mainFrame.setVisible(true);
    }

    private void loadImage() {
        try {
            back = ImageIO.read(getClass().getResourceAsStream("back.png"));
            background = ImageIO.read(getClass().getResourceAsStream("br_coganh.jpg"));
            boardImg = ImageIO.read(getClass().getResourceAsStream("board.png"));
            backgr = ImageIO.read(getClass().getResourceAsStream("brg.png"));
            on = ImageIO.read(getClass().getResourceAsStream("on.png"));
            off = ImageIO.read(getClass().getResourceAsStream("off.png"));
            hd[0] = ImageIO.read(getClass().getResourceAsStream("hd1.jpg"));
            hd[1] = ImageIO.read(getClass().getResourceAsStream("hd2.jpg"));
            hd[2] = ImageIO.read(getClass().getResourceAsStream("hd3.jpg"));
            left = ImageIO.read(getClass().getResourceAsStream("left.png"));
            right = ImageIO.read(getClass().getResourceAsStream("right.png"));
            undoIcon = ImageIO.read(getClass().getResourceAsStream("QL.png"));
            logoImg = ImageIO.read(getClass().getResourceAsStream("Logo.png"));
            hoTroIcon = ImageIO.read(getClass().getResourceAsStream("hotro.png"));
        } catch (Exception e) {
            System.out.println("Không tìm thấy ảnh.");
        }
    }

    private BufferedImage copyImage(BufferedImage img) {
        if (img == null)
            return null;
        BufferedImage b_paint = new BufferedImage(img.getWidth(), img.getHeight(), img.getType());
        Graphics g = b_paint.createGraphics();
        g.drawImage(img, 0, 0, null);
        g.dispose();
        return b_paint;
    }

    public void veMenu() {
        if (backgr == null)
            return;
        // Luôn sử dụng 1000x600 để các tọa độ vẽ thống nhất
        background_paint = new BufferedImage(1000, 600, BufferedImage.TYPE_INT_ARGB);
        Graphics g = background_paint.createGraphics();
        g.drawImage(backgr, 0, 0, 1000, 600, null); // Vẽ và scale ảnh nền vào 1000x600

        if (gc != null) {
            if (gc.dangChonDoKho) {
                g.setColor(Color.red);
                g.setFont(new Font("Times New Roman", Font.BOLD, 45));
                g.drawString("Easy", 40, 70);
                g.setColor(Color.orange);
                g.drawString("Medium", 40, 140);
                g.setColor(Color.MAGENTA);
                g.drawString("Hard", 40, 210);
                g.setColor(Color.red);
                g.drawString("Quay Lại", 40, 280);
            } else if (!gc.huongDan && !gc.xemLichSu) {
                // Đã xóa vẽ chữ đè hình (PvP, PvE, Hướng dẫn, Lịch sử đấu)
            } else if (gc.xemLichSu) {
                veBangLichSu(g);
            }
        }
        g.dispose();
        this.repaint();
    }

    private void veBangLichSu(Graphics g) {
        if (gc == null)
            return;
        g.setColor(new Color(0, 0, 0, 220));
        g.fillRect(50, 20, 900, 550);
        g.setColor(Color.WHITE);

        Font fontTieuDe = new Font("Times New Roman", Font.BOLD, 40);
        g.setFont(fontTieuDe);

        if (gc.trangLS == 0) {
            String title1 = "LỊCH SỬ CÁC VÁN ĐẤU";
            int title1Width = g.getFontMetrics().stringWidth(title1);
            g.drawString(title1, 500 - (title1Width / 2), 70);

            g.setFont(new Font("Times New Roman", Font.BOLD, 22));
            g.setColor(Color.LIGHT_GRAY);
            g.drawString("Ván", 200, 130);
            g.drawString("Chế độ", 450, 130);
            g.drawString("Kết quả", 700, 130);

            // 1. LẤY TOÀN BỘ LỊCH SỬ THAY VÌ 12 VÁN
            ArrayList<String> recent = gc.sm.getRecentHistory(gc.sm.getTotalGames());
            g.setFont(new Font("Times New Roman", Font.PLAIN, 22));
            int total = gc.sm.getTotalGames();

            // 2. TRỪ ĐI ĐỘ CUỘN (Scroll Offset)
            int y = 170 - gc.cuonLichSu;

            // 3. TẠO VÙNG CẮT (CLIP): Chỉ vẽ chữ trong khung giới hạn từ y=140 đến y=530
            g.setClip(50, 140, 900, 390);

            for (int i = recent.size() - 1; i >= 0; i--) {
                String record = recent.get(i);
                String[] parts = record.split("\\|");
                String winnerRaw = parts[0];
                String mode = (parts.length > 1) ? parts[1] : "PvP";

                String winnerText;
                Color c;

                // Phân tách logic hiển thị theo chế độ chơi
                if (mode.equals("PvP")) {
                    winnerText = winnerRaw.equals("XANH") ? "Xanh Win" : "Đỏ Win";
                    c = winnerRaw.equals("XANH") ? Color.CYAN : Color.RED;
                } else { // Chế độ PvE
                    winnerText = winnerRaw.equals("XANH") ? "Người thắng" : "Máy thắng";
                    c = winnerRaw.equals("XANH") ? Color.CYAN : Color.RED;
                }
                g.setColor(Color.WHITE);
                g.drawString("#" + (total - (recent.size() - 1 - i)), 200, y);
                g.setColor(Color.GREEN);
                g.drawString(mode, 450, y);
                g.setColor(c);
                g.drawString(winnerText, 700, y);
                y += 30;
            }

            // 4. HỦY VÙNG CẮT (Để vẽ các nút bấm và chữ ở dưới cùng không bị che)
            g.setClip(null);

            g.setColor(Color.YELLOW);
            g.setFont(new Font("Times New Roman", Font.ITALIC, 20));
            g.drawString("Trang 1/2: Lịch sử chung (Lăn chuột để xem thêm)", 300, 550);

        } else if (gc.trangLS == 1) {
            String title2 = "THỐNG KÊ PVE";
            int title2Width = g.getFontMetrics().stringWidth(title2);
            g.drawString(title2, 500 - (title2Width / 2), 70);

            int totalPvE = gc.sm.getTotalPvEGames();
            int bluePvE = gc.sm.getPvEBlueWins();
            int redPvE = gc.sm.getPvERedWins();
            float blueRate = totalPvE == 0 ? 0 : ((float) bluePvE / totalPvE) * 100;
            float redRate = totalPvE == 0 ? 0 : ((float) redPvE / totalPvE) * 100;

            g.setFont(new Font("Times New Roman", Font.PLAIN, 30));
            g.setColor(Color.CYAN);
            g.drawString("Tổng số ván đã chơi: " + totalPvE, 150, 160);
            g.setColor(Color.BLUE);
            g.drawString(String.format("NGƯỜI THẮNG: %d ván (%.1f%%)", bluePvE, blueRate), 150, 230);
            g.setColor(Color.RED);
            g.drawString(String.format("MÁY THẮNG: %d ván (%.1f%%)", redPvE, redRate), 150, 300);

            int barX = 150, barY = 360, barWidth = 700, barHeight = 45;
            g.setFont(new Font("Times New Roman", Font.BOLD, 22));
            if (totalPvE > 0) {
                int blueWidth = (int) (barWidth * ((float) bluePvE / totalPvE));
                int redWidth = barWidth - blueWidth;

                g.setColor(new Color(0, 50, 200));
                g.fillRect(barX, barY, blueWidth, barHeight);
                g.setColor(new Color(200, 0, 0));
                g.fillRect(barX + blueWidth, barY, redWidth, barHeight);
                g.setColor(Color.WHITE);
                g.drawRect(barX, barY, barWidth, barHeight);

                g.setColor(Color.WHITE);
                if (blueWidth > 80)
                    g.drawString(String.format("%.1f%%", blueRate), barX + blueWidth / 2 - 30, barY + 30);
                if (redWidth > 80)
                    g.drawString(String.format("%.1f%%", redRate), barX + blueWidth + redWidth / 2 - 30, barY + 30);
            } else {
                g.setColor(Color.DARK_GRAY);
                g.fillRect(barX, barY, barWidth, barHeight);
                g.setColor(Color.WHITE);
                g.drawRect(barX, barY, barWidth, barHeight);
                g.drawString("Chưa có dữ liệu ván đấu", barX + barWidth / 2 - 120, barY + 30);
            }
            g.setColor(Color.YELLOW);
            g.setFont(new Font("Times New Roman", Font.ITALIC, 20));
            g.drawString("Trang 2/2: Thống kê AI", 400, 550);
        }

        if (back != null)
            g.drawImage(back, 60, 30, 50, 50, null);
        if (gc.trangLS > 0 && left != null)
            g.drawImage(left, 380, 485, 40, 40, null);
        if (gc.trangLS < 1 && right != null)
            g.drawImage(right, 580, 485, 40, 40, null);
    }

    public void veLaiToanBo() {
        background_paint = new BufferedImage(1000, 600, BufferedImage.TYPE_INT_ARGB);
        Graphics g_bg = background_paint.createGraphics();
        
        if (background != null)
            g_bg.drawImage(background, 0, 0, 1000, 600, null);
        else if (backgr != null)
            g_bg.drawImage(backgr, 0, 0, 1000, 600, null);

        if (background_paint != null && gc != null) {
            Graphics g = g_bg; // Sử dụng graphics của background_paint luôn
            g.setColor(Color.BLACK);
            g.setFont(new Font("Times New Roman", Font.BOLD, 30));
            g.drawString("Back", 895, 50);

            g.setColor(new Color(147, 112, 219));
            g.fillRoundRect(730, 20, 130, 50, 30, 30);
            g.fillRoundRect(730, 520, 130, 50, 30, 30);

            g.setColor(Color.WHITE);
            g.setFont(new Font("Times New Roman", Font.BOLD, 40));
            g.drawString(gc.quanDo.size() + "", 755, 58);
            g.drawString(gc.quanXanh.size() + "", 755, 558);

            // Hiển thị đồng hồ đếm ngược PvP
            if (!gc.isPvE) {
                int minRed = gc.timeLeftRed / 60;
                int secRed = gc.timeLeftRed % 60;
                int minBlue = gc.timeLeftBlue / 60;
                int secBlue = gc.timeLeftBlue % 60;
                String timeRedStr = String.format("%02d:%02d", minRed, secRed);
                String timeBlueStr = String.format("%02d:%02d", minBlue, secBlue);

                // Khung chứa đồng hồ quân Đỏ (trên)
                g.setColor(new Color(147, 112, 219));
                g.fillRoundRect(730, 75, 130, 35, 30, 30);
                g.setFont(new Font("Times New Roman", Font.BOLD, 22));
                g.setColor(gc.timeLeftRed <= 30 ? Color.RED : Color.WHITE);
                g.drawString(timeRedStr, 760, 99);

                // Khung chứa đồng hồ quân Xanh (dưới)
                g.setColor(new Color(147, 112, 219));
                g.fillRoundRect(730, 480, 130, 35, 30, 30);
                g.setFont(new Font("Times New Roman", Font.BOLD, 22));
                g.setColor(gc.timeLeftBlue <= 30 ? Color.CYAN : Color.WHITE);
                g.drawString(timeBlueStr, 760, 504);
            }

            g.setColor(Color.yellow);
            if (!gc.chonBlue)
                g.fillOval(810, 30, 30, 30);
            else
                g.fillOval(810, 530, 30, 30);

            if (gc.chuMo) {
                if (!gc.moCo)
                    g.setColor(Color.BLACK);
                else
                    g.setColor(Color.pink);
                g.drawString("Mở", 780, 300);
            }

            if (gc.isPvE && gc.end == 0) {
                int boxX = 730;
                int boxY = 450;
                int boxW = 130;
                int boxH = 50;

                // Vẽ ô nền màu tím chung (1 ô duy nhất)
                g.setColor(new Color(147, 112, 219));
                g.fillRoundRect(boxX, boxY, boxW, boxH, 30, 30);

                // SIZE 39 LÀ ĐẸP NHẤT CHO ẢNH ĐÃ CẮT SÁT VIỀN
                int iconSize = 39;
                int iconY = boxY + (boxH - iconSize) / 2; // Căn giữa theo chiều dọc

                // --- VẼ NÚT HỖ TRỢ (BÓNG ĐÈN) BÊN TRÁI ---
                int hoTroX = 744; // Căn chính giữa của nửa ô bên trái
                if (hoTroIcon != null) {
                    g.drawImage(hoTroIcon, hoTroX, iconY, iconSize, iconSize, null);
                }
                int helpLeft = GameController.MAX_HELP - gc.helpCount;
                if (helpLeft > 0) {
                    g.setColor(Color.YELLOW);
                } else {
                    g.setColor(Color.RED);
                }
                g.setFont(new Font("Times New Roman", Font.BOLD, 15));
                // Vẽ số ở góc trên bên phải icon bóng đèn
                g.drawString(String.valueOf(helpLeft), hoTroX + iconSize - 2, iconY + 10);

                // --- VẼ NÚT ĐI LẠI (UNDO) BÊN PHẢI ---
                int undoX = 805; // Căn chính giữa của nửa ô bên phải
                if (undoIcon != null) {
                    g.drawImage(undoIcon, undoX, iconY, iconSize, iconSize, null);
                } else if (back != null) {
                    g.drawImage(back, undoX, iconY, iconSize, iconSize, null);
                }

                // Vẽ số lượt Undo (Góc trên bên phải của icon Undo)
                int luotConLai = GameController.MAX_UNDO - gc.undoCount;
                if (luotConLai > 0) {
                    g.setColor(Color.YELLOW);
                } else {
                    g.setColor(Color.RED);
                }
                g.setFont(new Font("Times New Roman", Font.BOLD, 15));
                g.drawString(String.valueOf(luotConLai), undoX + iconSize - 2, iconY + 10);
            }

            if (gc.xemLichSu) {
                veBangLichSu(g);
            }
            g.dispose();
        }

        if (boardImg != null)
            board_paint = copyImage(boardImg);
        if (board_paint != null && gc != null) {
            Graphics g = board_paint.createGraphics();
            for (int i = 0; i < 5; i++) {
                for (int j = 0; j < 5; j++) {
                    if (gc.board[i][j][2] != 0) {
                        if (gc.isAnimating && i == gc.animDestRow && j == gc.animDestCol) {
                            continue;
                        }
                        if (gc.board[i][j][2] == -1)
                            g.setColor(Color.red);
                        else if (gc.board[i][j][2] == 1)
                            g.setColor(Color.blue);
                        else if (gc.board[i][j][2] == 2)
                            g.setColor(Color.CYAN);
                        else if (gc.board[i][j][2] == -2)
                            g.setColor(Color.yellow);
                        else if (gc.board[i][j][2] == 3)
                            g.setColor(Color.white);
                        else if (gc.board[i][j][2] == 4)
                            g.setColor(Color.black);
                        g.fillOval(gc.board[i][j][0], gc.board[i][j][1], 30, 30);
                    }
                }
            }

            if (gc.isAnimating) {
                if (gc.animPieceType == -1)
                    g.setColor(Color.red);
                else if (gc.animPieceType == 1)
                    g.setColor(Color.blue);
                else if (gc.animPieceType == 2)
                    g.setColor(Color.CYAN);
                else if (gc.animPieceType == -2)
                    g.setColor(Color.yellow);
                g.fillOval((int) gc.animCurrX, (int) gc.animCurrY, 30, 30);
            }
            if (gc.end != 0) {
                g.setColor(new Color(147, 112, 219));
                g.fillRoundRect(130, 150, 340, 250, 45, 45);
                g.setFont(new Font("Times New Roman", Font.BOLD, 60));
                g.setColor(Color.yellow);
                g.drawString("Tiếp tục", 192, 350);
                g.setColor(Color.white);
                if (gc.isPvE) {
                    if (gc.end == 1)
                        g.drawString("Người thắng", 150, 250);
                    else if (gc.end == -1)
                        g.drawString("Máy thắng", 180, 250);
                } else {
                    if (gc.end == 1)
                        g.drawString("Xanh thắng", 160, 250);
                    else if (gc.end == -1)
                        g.drawString("Đỏ thắng", 190, 250);
                }

                g.dispose();
            }
        }
        this.repaint();
    }

    @Override
    public void paint(Graphics g) {
        super.paint(g);
        Graphics2D g2 = (Graphics2D) g.create();
        g2.setRenderingHint(RenderingHints.KEY_RENDERING, RenderingHints.VALUE_RENDER_QUALITY);

        int w = getWidth();
        int h = getHeight();
        double scaleX = (double) w / 1000;
        double scaleY = (double) h / 600;

        g2.scale(scaleX, scaleY);

        if (background_paint != null)
            g2.drawImage(background_paint, 0, 0, 1000, 600, this);

        if (start) {
            if (board_paint != null) {
                // Tọa độ gốc trong hệ 1000x600
                g2.drawImage(board_paint, 50, 0, this);
            }
        }

        if (gc != null && gc.huongDan) {
            g2.setColor(Color.white);
            if (hdImg != null)
                g2.drawImage(hdImg, 100, 30, 800, 540, this);
            if (right != null)
                g2.drawImage(right, 810, 490, 50, 50, this);
            if (left != null)
                g2.drawImage(left, 750, 490, 50, 50, this);
            if (back != null)
                g2.drawImage(back, 15, 30, 50, 50, this);
        } else if (gc != null && gc.xemLichSu) {
            veBangLichSu(g2);
        }

        if (gc != null && !gc.huongDan) {
            if (this.soundOn) {
                if (on != null)
                    g2.drawImage(on, 940, 520, 50, 50, this);
            } else {
                if (off != null)
                    g2.drawImage(off, 940, 520, 50, 50, null);
            }
        }
        g2.dispose();
    }

    public void resetGame() {
        this.start = false;
        this.gc = new GameController(this);
        veMenu();
    }

    public static void main(String args[]) {
        new GamePanel_CoGanh();
    }
}