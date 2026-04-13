package coganh;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Font;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.awt.FontMetrics;
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

    public BufferedImage background_paint, board_paint, on, off, hdImg, left, right, back, undoIcon, logoImg, hoTroIcon, khungImg, kChuaImg;
    public BufferedImage[] hd = new BufferedImage[3];
    public BufferedImage[] avatars = new BufferedImage[6];
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
        Font menuFont = new Font("Times New Roman", Font.BOLD, 16);

        // --- Menu THỂ LOẠI (PvP & PvE) ---
        categoryMenu = new JMenu("Thể loại");
        categoryMenu.setForeground(menuTextColor);
        categoryMenu.setFont(menuFont);

        JMenuItem pvpItem = new JMenuItem("Phòng PvP (Người vs Người)");
        JMenu pveSubMenu = new JMenu("Phòng PvE (Người vs Máy)");
        
        JMenuItem pveEasyItem = new JMenuItem("Easy");
        JMenuItem pveMedItem = new JMenuItem("Medium");
        JMenuItem pveHardItem = new JMenuItem("Hard");

        pvpItem.addActionListener(e -> {
            if (gc != null && (gc.xemLichSu || gc.huongDan)) {
                javax.swing.JOptionPane.showMessageDialog(this, "Vui lòng thoát ra trước khi chọn cái khác.", "Thông báo", javax.swing.JOptionPane.WARNING_MESSAGE);
                return;
            }
            gc.kiemTraVaVaoGame(false);
        });
        pveEasyItem.addActionListener(e -> { 
            if (gc != null && (gc.xemLichSu || gc.huongDan)) {
                javax.swing.JOptionPane.showMessageDialog(this, "Vui lòng thoát ra trước khi chọn cái khác.", "Thông báo", javax.swing.JOptionPane.WARNING_MESSAGE);
                return;
            }
            gc.doKhoAI = 1; 
            gc.kiemTraVaVaoGame(true); 
        });
        pveMedItem.addActionListener(e -> { 
            if (gc != null && (gc.xemLichSu || gc.huongDan)) {
                javax.swing.JOptionPane.showMessageDialog(this, "Vui lòng thoát ra trước khi chọn cái khác.", "Thông báo", javax.swing.JOptionPane.WARNING_MESSAGE);
                return;
            }
            gc.doKhoAI = 3; 
            gc.kiemTraVaVaoGame(true); 
        });
        pveHardItem.addActionListener(e -> { 
            if (gc != null && (gc.xemLichSu || gc.huongDan)) {
                javax.swing.JOptionPane.showMessageDialog(this, "Vui lòng thoát ra trước khi chọn cái khác.", "Thông báo", javax.swing.JOptionPane.WARNING_MESSAGE);
                return;
            }
            gc.doKhoAI = 5; 
            gc.kiemTraVaVaoGame(true); 
        });

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
        JMenuItem lsItem = new JMenuItem("Lịch sử đấu");
        JMenuItem undoItem = new JMenuItem("Đi lại (Undo)");
        JMenuItem hintItem = new JMenuItem("Gợi ý (Hint)");

        hdItem.addActionListener(e -> { 
            if (gc != null && gc.xemLichSu) {
                javax.swing.JOptionPane.showMessageDialog(this, "Vui lòng thoát ra trước khi chọn cái khác.", "Thông báo", javax.swing.JOptionPane.WARNING_MESSAGE);
                return;
            }
            if (hd[0] != null) hdImg = hd[0];
            if (gc != null) gc.huongDan = true; 
            veMenu(); 
        });
        lsItem.addActionListener(e -> { 
            if (gc != null && gc.huongDan) {
                javax.swing.JOptionPane.showMessageDialog(this, "Vui lòng thoát ra trước khi chọn cái khác.", "Thông báo", javax.swing.JOptionPane.WARNING_MESSAGE);
                return;
            }
            if (gc != null) {
                gc.xemLichSu = true; 
                gc.trangLS = 0; 
                gc.cuonLichSu = 0; 
            }
            veMenu(); 
        });
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
       mainFrame.setSize(SCREEN_WIDTH + 15, SCREEN_HEIGHT + 45 + menuBar.getPreferredSize().height);
        
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
            background = ImageIO.read(getClass().getResourceAsStream("br_coganh.png"));
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
            


            try {
                kChuaImg = ImageIO.read(getClass().getResourceAsStream("K_chua.png"));
            } catch (Exception ex) {
                System.out.println("Không tìm thấy tệp K_chua.png");
            }
            
            try {
                String[] avatarNames = {"BotAi.png", "Hinh1.png", "Hinh2.png", "Hinh3.png", "Hinh4.png", "Hinh5.png"};
                for (int i = 0; i < 6; i++) {
                    BufferedImage raw = ImageIO.read(getClass().getResourceAsStream(avatarNames[i]));
                    if (raw != null) {
                        avatars[i] = getCircularAvatar(raw);
                    }
                }
            } catch (Exception ex) {
                System.out.println("Lỗi khi tải các tệp avatar");
            }
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

    private BufferedImage getCircularAvatar(BufferedImage src) {
        int diameter = Math.min(src.getWidth(), src.getHeight());
        // Scale down the crop box to zoom in and remove gray frame margins.
        int cropSize = (int)(diameter * 0.85); 
        int xOffset = (src.getWidth() - cropSize) / 2;
        int yOffset = (src.getHeight() - cropSize) / 2;
        
        BufferedImage dest = new BufferedImage(cropSize, cropSize, BufferedImage.TYPE_INT_ARGB);
        Graphics2D g2 = dest.createGraphics();
        g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        g2.setRenderingHint(RenderingHints.KEY_INTERPOLATION, RenderingHints.VALUE_INTERPOLATION_BILINEAR);
        
        g2.fillOval(0, 0, cropSize, cropSize);
        g2.setComposite(java.awt.AlphaComposite.SrcIn);
        g2.drawImage(src, 0, 0, cropSize, cropSize, xOffset, yOffset, xOffset + cropSize, yOffset + cropSize, null);
        g2.dispose();
        return dest;
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
        Graphics2D g2 = (Graphics2D) g;
        
        // Bật tính năng khử răng cưa để font Times New Roman hiển thị mượt mà, rành rọt nhất
        g2.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_ON);
        g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

        // Shadow
        g2.setColor(new Color(0, 0, 0, 100));
        g2.fillRoundRect(55, 25, 900, 550, 30, 30);

        // Nền giấy tông ấm
        g2.setColor(new Color(253, 245, 230, 240));
        g2.fillRoundRect(50, 20, 900, 550, 30, 30);

        // Viền trang trí
        g2.setColor(new Color(139, 69, 19));
        g2.setStroke(new java.awt.BasicStroke(4));
        g2.drawRoundRect(50, 20, 900, 550, 30, 30);
        
        g2.setStroke(new java.awt.BasicStroke(1));
        g2.setColor(Color.BLACK);

        Font fontTieuDe = new Font("Times New Roman", Font.BOLD, 40);
        g.setFont(fontTieuDe);

        if (gc.trangLS == 0) {
            String title1 = "LỊCH SỬ CÁC VÁN ĐẤU";
            int title1Width = g.getFontMetrics().stringWidth(title1);
            g.setColor(new Color(139, 69, 19));
            g.drawString(title1, 500 - (title1Width / 2), 70);
            g.drawLine(500 - (title1Width / 2) - 20, 85, 500 + (title1Width / 2) + 20, 85);

            g.setFont(new Font("Times New Roman", Font.BOLD, 22));
            g.drawString("Ván", 200, 130);
            g.drawString("Chế độ", 450, 130);
            g.drawString("Kết quả", 700, 130);
            
            g.drawLine(150, 145, 850, 145);

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
                if (parts.length > 2) {
                    winnerText = parts[2] + " thắng";
                    c = winnerRaw.equals("XANH") ? Color.BLUE : Color.RED;
                } else {
                    if (mode.equals("PvP")) {
                        winnerText = winnerRaw.equals("XANH") ? "Xanh Win" : "Đỏ Win";
                        c = winnerRaw.equals("XANH") ? Color.BLUE : Color.RED;
                    } else { // Chế độ PvE
                        winnerText = winnerRaw.equals("XANH") ? "Người Thắng" : "BotAI Thắng";
                        c = winnerRaw.equals("XANH") ? Color.BLUE : Color.RED;
                    }
                }
                int rowIndex = recent.size() - 1 - i;
                if (rowIndex % 2 == 0) {
                    g.setColor(new Color(0, 0, 0, 15)); // Sọc nền nhạt
                    g.fillRect(150, y - 22, 700, 30);
                }

                g.setColor(Color.BLACK);
                g.drawString(String.valueOf(total - rowIndex), 200, y);
                g.setColor(new Color(0, 150, 0));
                g.drawString(mode, 450, y);
                g.setColor(c);
                g.drawString(winnerText, 700, y);
                y += 30;
            }

            // 4. HỦY VÙNG CẮT (Để vẽ các nút bấm và chữ ở dưới cùng không bị che)
            g.setClip(null);

            g.setColor(new Color(200, 100, 0));
            g.setFont(new Font("Times New Roman", Font.ITALIC, 20));
            g.drawString("Trang 1/2: Lịch sử chung (Lăn chuột để xem thêm)", 300, 550);

        } else if (gc.trangLS == 1) {
            String title2 = "THỐNG KÊ PVE";
            int title2Width = g.getFontMetrics().stringWidth(title2);
            g.setColor(new Color(139, 69, 19));
            g.drawString(title2, 500 - (title2Width / 2), 70);
            g.drawLine(500 - (title2Width / 2) - 20, 85, 500 + (title2Width / 2) + 20, 85);

            int totalPvE = gc.sm.getTotalPvEGames();
            int bluePvE = gc.sm.getPvEBlueWins();
            int redPvE = gc.sm.getPvERedWins();
            float blueRate = totalPvE == 0 ? 0 : ((float) bluePvE / totalPvE) * 100;
            float redRate = totalPvE == 0 ? 0 : ((float) redPvE / totalPvE) * 100;

            g.setFont(new Font("Times New Roman", Font.PLAIN, 30));
            g.setColor(Color.BLACK);
            g.drawString("Tổng số ván đã chơi: " + totalPvE, 150, 160);
            g.setColor(Color.BLUE);
            g.drawString(String.format("Người Thắng: %d ván (%.1f%%)", bluePvE, blueRate), 150, 230);
            g.setColor(Color.RED);
            g.drawString(String.format("BotAI Thắng: %d ván (%.1f%%)", redPvE, redRate), 150, 300);

            int barX = 150, barY = 360, barWidth = 700, barHeight = 45;
            g.setFont(new Font("Times New Roman", Font.BOLD, 22));
            if (totalPvE > 0) {
                int blueWidth = (int) (barWidth * ((float) bluePvE / totalPvE));
                int redWidth = barWidth - blueWidth;

                g.setColor(new Color(0, 50, 200));
                g.fillRect(barX, barY, blueWidth, barHeight);
                g.setColor(new Color(200, 0, 0));
                g.fillRect(barX + blueWidth, barY, redWidth, barHeight);
                g.setColor(Color.BLACK);
                g.drawRect(barX, barY, barWidth, barHeight);

                g.setColor(Color.WHITE);
                if (blueWidth > 80)
                    g.drawString(String.format("%.1f%%", blueRate), barX + blueWidth / 2 - 30, barY + 30);
                if (redWidth > 80)
                    g.drawString(String.format("%.1f%%", redRate), barX + blueWidth + redWidth / 2 - 30, barY + 30);
            } else {
                g.setColor(Color.DARK_GRAY);
                g.fillRect(barX, barY, barWidth, barHeight);
                g.setColor(Color.BLACK);
                g.drawRect(barX, barY, barWidth, barHeight);
                g.drawString("Chưa có dữ liệu ván đấu", barX + barWidth / 2 - 120, barY + 30);
            }
            g.setColor(new Color(200, 100, 0));
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
        Graphics2D g_bg = background_paint.createGraphics();
        
        // Bật Antialias toàn cục cho bảng game để phông chữ Times New Roman rõ nét, không bị răng cưa mờ
        g_bg.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_ON);
        g_bg.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

        
        if (background != null)
            g_bg.drawImage(background, 0, 0, 1000, 600, null);
        else if (backgr != null)
            g_bg.drawImage(backgr, 0, 0, 1000, 600, null);

        if (background_paint != null && gc != null) {
            Graphics g = g_bg; // Sử dụng graphics của background_paint luôn
            
            // XÁC ĐỊNH KHU VỰC 2 BÊN
            int doPanelX = 800;
            int xanhPanelX = 10;
            int panelY = 10;
            // Kích thước khung chứa (căn theo K_chua.png)
            int panelW = 190;
            int panelH = 260; // Rút ngắn lại cho vừa vặn hơn

            // --- NÚT TRỞ VỀ ---
            g.setColor(new Color(255, 255, 255, 150));
            g.fillRoundRect(10, 540, 100, 45, 20, 20);
            g.setColor(Color.BLACK);
            g.setFont(new Font("Times New Roman", Font.BOLD, 25));
            g.drawString("Trở về", 22, 570);

            // --- KHU VỰC BÊN ĐỎ (Phe Đỏ / Máy) ---
            if (kChuaImg != null) {
                g.drawImage(kChuaImg, doPanelX, panelY, panelW, panelH, null);
            } else {
                g.setColor(new Color(0, 0, 0, 150));
                g.fillRoundRect(doPanelX, panelY, panelW, panelH, 30, 30);
            }
            
            // Vẽ Avatar Đỏ (giữa khung, thuỹ thấp xuống tránh hoa văn đầu)
            int avtSize = 100;
            int avtX_red = doPanelX + (panelW - avtSize) / 2;
            int avtY_red = panelY + 30;
            if (avatars[gc.avatarRedIndex] != null) {
                java.awt.Shape clipHolder = g.getClip();
                g.setClip(new java.awt.geom.Ellipse2D.Float(avtX_red, avtY_red, avtSize, avtSize));
                g.drawImage(avatars[gc.avatarRedIndex], avtX_red, avtY_red, avtSize, avtSize, null);
                g.setClip(clipHolder);
            } else {
                g.setColor(new Color(180, 40, 40));
                g.fillOval(avtX_red, avtY_red, avtSize, avtSize);
            }
            // Đoạn code vẽ khungImg đã được gỡ bỏ theo tóm tắt yêu cầu của bạn
            
            // Tên đỏ
            g.setFont(new Font("Times New Roman", Font.BOLD, 20));
            String nameRed = gc.playerNameRed;
            FontMetrics fmRed = g.getFontMetrics();
            while (fmRed.stringWidth(nameRed) > panelW - 20 && g.getFont().getSize() > 12) {
                g.setFont(g.getFont().deriveFont((float)(g.getFont().getSize() - 1)));
                fmRed = g.getFontMetrics();
            }
            g.setColor(new Color(255, 80, 80));
            g.drawString(nameRed, doPanelX + (panelW - fmRed.stringWidth(nameRed)) / 2, panelY + 155);
            
            // Số quân (đỏ)
            g.setColor(new Color(140, 90, 40, 120));
            g.fillRoundRect(doPanelX + 20, panelY + 160, panelW - 40, 33, 18, 18);
            g.setColor(Color.WHITE);
            g.setFont(new Font("Times New Roman", Font.BOLD, 22));
            String quanRedStr = "Quân: " + gc.quanDo.size();
            int quanRedW = g.getFontMetrics().stringWidth(quanRedStr);
            g.drawString(quanRedStr, doPanelX + (panelW - quanRedW) / 2, panelY + 183);

            // --- KHU VỰC BÊN XANH (Phe Xanh / Người) ---
            if (kChuaImg != null) {
                g.drawImage(kChuaImg, xanhPanelX, panelY, panelW, panelH, null);
            } else {
                g.setColor(new Color(0, 0, 0, 150));
                g.fillRoundRect(xanhPanelX, panelY, panelW, panelH, 30, 30);
            }
            
            // Vẽ Avatar Xanh
            int avtX_blue = xanhPanelX + (panelW - avtSize) / 2;
            int avtY_blue = panelY + 30;
            if (avatars[gc.avatarBlueIndex] != null) {
                java.awt.Shape clipHolder = g.getClip();
                g.setClip(new java.awt.geom.Ellipse2D.Float(avtX_blue, avtY_blue, avtSize, avtSize));
                g.drawImage(avatars[gc.avatarBlueIndex], avtX_blue, avtY_blue, avtSize, avtSize, null);
                g.setClip(clipHolder);
            } else {
                g.setColor(new Color(40, 80, 180));
                g.fillOval(avtX_blue, avtY_blue, avtSize, avtSize);
            }
            // Đoạn code vẽ khungImg đã được gỡ bỏ
            
            // Tên Xanh
            g.setFont(new Font("Times New Roman", Font.BOLD, 20));
            String nameBlue = gc.playerNameBlue;
            FontMetrics fmBlue = g.getFontMetrics();
            while (fmBlue.stringWidth(nameBlue) > panelW - 20 && g.getFont().getSize() > 12) {
                g.setFont(g.getFont().deriveFont((float)(g.getFont().getSize() - 1)));
                fmBlue = g.getFontMetrics();
            }
            g.setColor(new Color(100, 180, 255));
            g.drawString(nameBlue, xanhPanelX + (panelW - fmBlue.stringWidth(nameBlue)) / 2, panelY + 155);
            
            // Số quân (xanh)
            g.setColor(new Color(140, 90, 40, 120));
            g.fillRoundRect(xanhPanelX + 20, panelY + 160, panelW - 40, 33, 18, 18);
            g.setColor(Color.WHITE);
            g.setFont(new Font("Times New Roman", Font.BOLD, 22));
            String quanBlueStr = "Quân: " + gc.quanXanh.size();
            int quanBlueW = g.getFontMetrics().stringWidth(quanBlueStr);
            g.drawString(quanBlueStr, xanhPanelX + (panelW - quanBlueW) / 2, panelY + 183);

            // Thời gian PvP
            if (!gc.isPvE) {
                int minRed = gc.timeLeftRed / 60;
                int secRed = gc.timeLeftRed % 60;
                int minBlue = gc.timeLeftBlue / 60;
                int secBlue = gc.timeLeftBlue % 60;
                String timeRedStr = String.format("%02d:%02d", minRed, secRed);
                String timeBlueStr = String.format("%02d:%02d", minBlue, secBlue);

                // Timer Đỏ
                g.setColor(new Color(140, 90, 40, 120));
                g.fillRoundRect(doPanelX + 20, panelY + 200, panelW - 40, 33, 18, 18);
                g.setFont(new Font("Times New Roman", Font.BOLD, 22));
                g.setColor(gc.timeLeftRed <= 30 ? Color.RED : Color.WHITE);
                String tRed = timeRedStr;
                g.drawString(tRed, doPanelX + (panelW - g.getFontMetrics().stringWidth(tRed)) / 2, panelY + 223);

                // Timer Xanh
                g.setColor(new Color(140, 90, 40, 120));
                g.fillRoundRect(xanhPanelX + 20, panelY + 200, panelW - 40, 33, 18, 18);
                g.setFont(new Font("Times New Roman", Font.BOLD, 22));
                g.setColor(gc.timeLeftBlue <= 30 ? Color.CYAN : Color.WHITE);
                String tBlue = timeBlueStr;
                g.drawString(tBlue, xanhPanelX + (panelW - g.getFontMetrics().stringWidth(tBlue)) / 2, panelY + 223);
            }

            g.setColor(Color.yellow);
            if (!gc.chonBlue)
                g.fillOval(doPanelX + 10, panelY + 10, 25, 25);
            else
                g.fillOval(xanhPanelX + 10, panelY + 10, 25, 25);

            if (gc.chuMo) {
                // Bên nào đang có lượt thì hiển thị nút MỞ CỜ ở panel đó
                int moPanelX = gc.chonBlue ? xanhPanelX : doPanelX;
                Font moCoFont = new Font("Times New Roman", Font.BOLD, 30);
                g.setFont(moCoFont);
                java.awt.FontMetrics fm = g.getFontMetrics();
                String moCoText = "Mở cờ";
                int textW = fm.stringWidth(moCoText);
                int textH = fm.getAscent();
                int pad = 7;
                int rectX = moPanelX + 90 - textW / 2 - pad;
                int rectY = panelY + 278;
                int rectW = textW + pad * 2;
                int rectH = textH + pad;
                if (!gc.moCo) {
                    g.setColor(new Color(220, 50, 50));
                    g.fillRoundRect(rectX, rectY, rectW, rectH, 12, 12);
                    g.setColor(Color.WHITE);
                } else {
                    g.setColor(new Color(100, 200, 100));
                    g.fillRoundRect(rectX, rectY, rectW, rectH, 12, 12);
                    g.setColor(Color.BLACK);
                }
                g.drawString(moCoText, rectX + pad, rectY + textH);
            }

            if (gc.isPvE && gc.end == 0) {
                int boxX = xanhPanelX + 25;
                int boxY = panelY + 350; // Dịch xuống xíu tránh Mở cờ (280 -> 340)
                int boxW = 130;
                int boxH = 50;

                g.setColor(new Color(147, 112, 219));
                g.fillRoundRect(boxX, boxY, boxW, boxH, 30, 30);

                int iconSize = 39;
                int iconY = boxY + (boxH - iconSize) / 2; 

                int hoTroX = boxX + 14; 
                if (hoTroIcon != null) {
                    g.drawImage(hoTroIcon, hoTroX, iconY, iconSize, iconSize, null);
                }
                int helpLeft = GameController.MAX_HELP - gc.helpCount;
                if (helpLeft > 0) g.setColor(Color.YELLOW);
                else g.setColor(Color.RED);
                g.setFont(new Font("Times New Roman", Font.BOLD, 15));
                g.drawString(String.valueOf(helpLeft), hoTroX + iconSize - 2, iconY + 10);

                int undoX = boxX + 75; 
                if (undoIcon != null) {
                    g.drawImage(undoIcon, undoX, iconY, iconSize, iconSize, null);
                } else if (back != null) {
                    g.drawImage(back, undoX, iconY, iconSize, iconSize, null);
                }
                
                int luotConLai = GameController.MAX_UNDO - gc.undoCount;
                if (luotConLai > 0) g.setColor(Color.YELLOW);
                else g.setColor(Color.RED);
                g.setFont(new Font("Times New Roman", Font.BOLD, 15));
                // Vẽ số còn lại undo
                g.drawString(String.valueOf(luotConLai), undoX + iconSize - 2, iconY + 10);

                // --- 5 CHẤM CHUỖI THẮNG: phân biệt Phase 1 (vàng) vs Phase 2 (xanh lam) ---
                int dotSize = 22;
                int dotGap = 6;
                int totalDotsW = 5 * dotSize + 4 * dotGap;
                int dotStartX = xanhPanelX + (panelW - totalDotsW) / 2;
                int dotY = panelY + panelH - dotSize - 24;

                for (int s = 0; s < 5; s++) {
                    int dx = dotStartX + s * (dotSize + dotGap);
                    boolean isPhase2Slot = (s >= GameController.WINS_PHASE1); // chấm 4&5
                    boolean filled = (s < gc.consecutiveWins);

                    if (filled) {
                        if (isPhase2Slot) {
                            // Phase 2: màu xanh lam (độ khó tăng)
                            g.setColor(new Color(0, 200, 255));
                            g.fillOval(dx, dotY, dotSize, dotSize);
                            g.setColor(new Color(0, 130, 180));
                        } else {
                            // Phase 1: màu vàng (độ khó gốc)
                            g.setColor(new Color(255, 200, 0));
                            g.fillOval(dx, dotY, dotSize, dotSize);
                            g.setColor(new Color(200, 140, 0));
                        }
                        g.drawOval(dx, dotY, dotSize, dotSize);
                    } else {
                        // Chấm chưa đạt: nền xám, viền theo phase
                        g.setColor(new Color(55, 55, 55, 180));
                        g.fillOval(dx, dotY, dotSize, dotSize);
                        g.setColor(isPhase2Slot ? new Color(0, 150, 200, 150) : new Color(160, 130, 0, 150));
                        g.drawOval(dx, dotY, dotSize, dotSize);
                    }
                }


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
                // Tính chiều cao box: cao hơn nếu có sub-message
                boolean coSubMsg = gc.isPvE && gc.endGameSubMsg != null;
                int boxH = coSubMsg ? 280 : 250;
                g.setColor(new Color(147, 112, 219));
                g.fillRoundRect(130, 150, 340, boxH, 45, 45);

                // Xác định tên người thắng
                String winnerName = (gc.end == 1) ? gc.playerNameBlue : gc.playerNameRed;
                String winText = winnerName + " thắng!";

                // Tự động scale cỡ chữ nếu tên quá dài (max 300px trong khung 340px)
                int fontSize = 45;
                g.setFont(new Font("Times New Roman", Font.BOLD, fontSize));
                while (fontSize > 18 && g.getFontMetrics().stringWidth(winText) > 300) {
                    fontSize -= 2;
                    g.setFont(new Font("Times New Roman", Font.BOLD, fontSize));
                }

                if (gc.timeoutEnd) {
                    g.setColor(new Color(255, 60, 60)); // Màu đỏ nhạt báo hết giờ
                    String timeoutText = "Hết giờ!";
                    int timeoutW = g.getFontMetrics().stringWidth(timeoutText);
                    g.drawString(timeoutText, 130 + 340 / 2 - timeoutW / 2, 205);
                }

                // Căn giữa chữ thắng trong khung
                int textW = g.getFontMetrics().stringWidth(winText);
                int centerX = 130 + 340 / 2 - textW / 2;
                g.setColor(Color.white);
                g.drawString(winText, centerX, gc.timeoutEnd ? 260 : 255);

                // --- THÔNG ĐIỆP PHỤ (thăng hạng / gãy chuỗi) ---
                if (coSubMsg) {
                    boolean laThangHang = gc.endGameSubMsg.startsWith("🏆");
                    g.setColor(laThangHang ? new Color(255, 230, 80) : new Color(255, 160, 180));
                    g.setFont(new Font("Times New Roman", Font.BOLD, 17));
                    // Vẽ text xuống dòng nếu quá dài (word wrap thủ công)
                    // Bỏ emoji đầu (xử lý surrogate pair an toàn)
                    String rawMsg = gc.endGameSubMsg.trim();
                    int cpCount = rawMsg.codePointCount(0, rawMsg.length());
                    String subMsg = cpCount > 1 ? rawMsg.substring(rawMsg.offsetByCodePoints(0, 1)).trim() : rawMsg;
                    int maxSubW = 310;
                    int subBoxX = 130;
                    int subBoxCenterX = subBoxX + 340 / 2;
                    FontMetrics subFm = g.getFontMetrics();
                    String[] words = subMsg.split(" ");
                    java.util.List<String> subLines = new java.util.ArrayList<>();
                    StringBuilder curLine = new StringBuilder();
                    for (String word : words) {
                        String test = curLine.length() == 0 ? word : curLine + " " + word;
                        if (subFm.stringWidth(test) > maxSubW && curLine.length() > 0) {
                            subLines.add(curLine.toString());
                            curLine = new StringBuilder(word);
                        } else {
                            curLine = new StringBuilder(test);
                        }
                    }
                    if (curLine.length() > 0) subLines.add(curLine.toString());
                    int lineH = subFm.getHeight();
                    int subStartY = 280;
                    for (String line : subLines) {
                        int lw = subFm.stringWidth(line);
                        g.drawString(line, subBoxCenterX - lw / 2, subStartY);
                        subStartY += lineH;
                    }
                }

                // Nút Tiếp tục
                int tiepY = coSubMsg ? 375 : 345;
                g.setFont(new Font("Times New Roman", Font.BOLD, 50));
                g.setColor(Color.yellow);
                int tiepW = g.getFontMetrics().stringWidth("Tiếp tục");
                g.drawString("Tiếp tục", 130 + 340 / 2 - tiepW / 2, tiepY);

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

        if (background_paint != null) {
            g2.drawImage(background_paint, 0, 0, 1000, 600, this);
        } else {
            // NẾU CHƯA CÓ ẢNH NỀN, VẼ GRADIENT GỖ SANG TRỌNG
            // Màu nâu đậm ở dưới và nâu sáng hơn ở trên
            Color color1 = new Color(60, 30, 10); // Nâu đen
            Color color2 = new Color(101, 67, 33); // Nâu gỗ
            
            java.awt.GradientPaint gp = new java.awt.GradientPaint(0, 0, color2, 0, getHeight(), color1);
            g2.setPaint(gp);
            g2.fillRect(0, 0, 1000, 600);
            
            // Thêm một chút hiệu ứng vân sần nhẹ (tùy chọn)
            g2.setColor(new Color(255, 255, 255, 10)); // Trắng rất mờ
            for (int i = 0; i < 600; i += 4) {
                g2.drawLine(0, i, 1000, i);
            }
        }

        if (start) {
            if (board_paint != null) {
                // Tọa độ gốc trong hệ 1000x600, Bàn cờ nhảy ra giữa
                g2.drawImage(board_paint, 225, 25, this);
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
            g2.setColor(Color.WHITE);
            g2.setFont(new Font("Times New Roman", Font.BOLD, 30));
            g2.drawString("Back", 15, 65);
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
        if (this.menuBar != null) {
            this.menuBar.setVisible(true);
        }
        veMenu();
    }

    public static void main(String args[]) {
        new GamePanel_CoGanh();
    }
}