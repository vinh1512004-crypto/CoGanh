package coganh;

import java.awt.*;
import java.awt.event.*;
import java.awt.geom.RoundRectangle2D;
import java.awt.image.BufferedImage;
import javax.swing.*;
import javax.swing.border.EmptyBorder;

public class PlayerSetupDialog extends JDialog {
    private String playerName;
    private int selectedAvatarIndex;
    private boolean confirmed = false;
    private JButton[] avatarButtons;

    // Màu theme Dân Gian (Folk UI)
    private static final Color BG_TOP     = new Color(255, 248, 235); // Giấy dó sáng
    private static final Color BG_BOT     = new Color(245, 232, 210); // Màu be, gai
    private static final Color WOOD_LIGHT = new Color(205, 170, 125); // Vàng tre/gỗ sáng
    private static final Color WOOD_DARK  = new Color(139, 90, 43);   // Nâu gỗ đậm
    private static final Color TEXT_DARK  = new Color(70, 40, 20);    // Chữ nâu đen
    private static final Color BRICK_RED  = new Color(180, 50, 40);   // Đỏ gạch nung điểm nhấn

    public PlayerSetupDialog(JFrame parent, String title, BufferedImage[] avatars,
                             String defaultName, int defaultAvatarIndex, boolean allowAvatarSelection) {
        super(parent, title, true);
        this.playerName = defaultName;
        // Ép người dùng tự chọn Avatar, không chọn sẵn nếu cho phép chọn
        this.selectedAvatarIndex = allowAvatarSelection ? -1 : defaultAvatarIndex;

        // Nền toàn dialog
        JPanel root = new JPanel(new BorderLayout(10, 15)) {
            @Override
            protected void paintComponent(Graphics g) {
                super.paintComponent(g);
                Graphics2D g2 = (Graphics2D) g;
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                // Gradient nền giấy dó
                GradientPaint gp = new GradientPaint(0, 0, BG_TOP, 0, getHeight(), BG_BOT);
                g2.setPaint(gp);
                g2.fillRect(0, 0, getWidth(), getHeight());
                // Đường viền khắc gỗ mộc
                g2.setColor(WOOD_DARK);
                g2.setStroke(new BasicStroke(3f));
                g2.drawRoundRect(6, 6, getWidth() - 13, getHeight() - 13, 20, 20);
                g2.setColor(WOOD_LIGHT);
                g2.setStroke(new BasicStroke(1.5f));
                g2.drawRoundRect(10, 10, getWidth() - 21, getHeight() - 21, 16, 16);
            }
        };
        root.setOpaque(false);
        root.setFocusable(true); // Cho phép root nhận Focus thay thế
        root.setBorder(new EmptyBorder(22, 20, 16, 20));
        setContentPane(root);

        // --- TOP: Nhập tên ---
        JPanel topPanel = new JPanel(new FlowLayout(FlowLayout.CENTER, 10, 10));
        topPanel.setOpaque(false);

        JLabel nameLabel = new JLabel("Nhập tên: ");
        nameLabel.setFont(new Font("Times New Roman", Font.BOLD, 20));
        nameLabel.setForeground(TEXT_DARK);

        // Khởi tạo text field với chuỗi rỗng để thấy được chữ mồi
        JTextField nameField = new JTextField("", 16) {
            @Override
            protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g;
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g2.setColor(new Color(255, 255, 255, 220)); // Trắng đục mộc mạc
                g2.fillRoundRect(0, 0, getWidth(), getHeight(), 12, 12);
                super.paintComponent(g);
                
                // Vẽ chữ mồi (Placeholder) trực tiếp lên nền nếu text đang trống
                if (getText().isEmpty()) {
                    g2.setFont(new Font("Times New Roman", Font.ITALIC, 17));
                    g2.setColor(new Color(150, 120, 90)); // Màu chữ mờ
                    FontMetrics fm = g2.getFontMetrics();
                    int y = (getHeight() - fm.getHeight()) / 2 + fm.getAscent();
                    String ph = allowAvatarSelection ? "Vui lòng nhập tên người chơi" : "Vui lòng nhập tên BotAI";
                    g2.drawString(ph, getInsets().left, y);
                }
            }
        };
        nameField.setFont(new Font("Times New Roman", Font.PLAIN, 18));
        nameField.setForeground(TEXT_DARK);
        nameField.setCaretColor(TEXT_DARK);
        nameField.setOpaque(false);
        nameField.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(WOOD_DARK, 1, true),
            BorderFactory.createEmptyBorder(6, 10, 6, 10)
        ));

        topPanel.add(nameLabel);
        topPanel.add(nameField);
        root.add(topPanel, BorderLayout.NORTH);

        // --- CENTER: Avatar ---
        if (allowAvatarSelection) {
            JLabel sectionLabel = new JLabel("Chọn hình đại diện", SwingConstants.CENTER);
            sectionLabel.setFont(new Font("Times New Roman", Font.ITALIC | Font.BOLD, 18));
            sectionLabel.setForeground(WOOD_DARK);
            sectionLabel.setBorder(new EmptyBorder(8, 0, 12, 0));

            JPanel centerWrapper = new JPanel(new BorderLayout(0, 6));
            centerWrapper.setOpaque(false);
            centerWrapper.add(sectionLabel, BorderLayout.NORTH);

            JPanel gridPanel = new JPanel(new GridLayout(2, 3, 15, 10)); // Khoảng cách khít lại để thu nhỏ Form
            gridPanel.setOpaque(false);
            gridPanel.setBorder(new EmptyBorder(0, 15, 0, 15));

            avatarButtons = new JButton[6];
            for (int i = 0; i < 6; i++) {
                final int currentButtonIndex = i;
                avatarButtons[i] = new JButton() {
                    private boolean hovered = false;
                    {
                        addMouseListener(new MouseAdapter() {
                            public void mouseEntered(MouseEvent e) { hovered = true; repaint(); }
                            public void mouseExited(MouseEvent e)  { hovered = false; repaint(); }
                        });
                    }
                    @Override
                    protected void paintComponent(Graphics g) {
                        Graphics2D g2 = (Graphics2D) g.create();
                        g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                        
                        int size = 85; 
                        int offset = (getWidth() - size) / 2;
                        
                        // HIỆU ỨNG 3D: Nổi Dậy & Bóng Đổ (Drop Shadow & Elevate)
                        if (hovered && selectedAvatarIndex != currentButtonIndex) {
                            // 1. Vẽ bóng đen in hằn xuống mặt gỗ
                            g2.setColor(new Color(0, 0, 0, 90));
                            g2.fillOval(offset + 2, offset + 5, size, size);
                            
                            // 2. Nhấc bổng không gian trục Y lên 4 pixel
                            g2.translate(0, -4);
                        }
                        
                        // Gọi hàm gốc để JButton tự vẽ chuẩn xác bức ảnh (Icon) với nền trong suốt
                        super.paintComponent(g2);
                        
                        // Vẽ viền lấp lánh bọc quanh sau khi ảnh đã được vẽ (và đã được nhấc lên)
                        if (hovered && selectedAvatarIndex != currentButtonIndex) {
                            g2.setColor(new Color(255, 215, 0, 200)); // Viền Vàng Kim loại
                            g2.setStroke(new BasicStroke(2.5f, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND));
                            g2.drawOval(offset - 1, offset - 1, size + 2, size + 2);
                        }

                        // Nếu avatar này đang được đánh dấu CHỌN XONG
                        if (selectedAvatarIndex == currentButtonIndex) {
                            g2.setColor(BRICK_RED); // Viền lụa đậm
                            g2.setStroke(new BasicStroke(4f, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND));
                            g2.drawOval(offset - 2, offset - 2, size + 4, size + 4); 
                        }
                        
                        g2.dispose();
                    }
                };
                if (avatars != null && avatars[i] != null) {
                    Image scaled = avatars[i].getScaledInstance(85, 85, Image.SCALE_SMOOTH); // Avatar TO HƠN
                    avatarButtons[i].setIcon(new ImageIcon(scaled));
                } else {
                    avatarButtons[i].setText("" + (i + 1));
                    avatarButtons[i].setForeground(TEXT_DARK);
                }
                
                // Set kích thước cố định cho button rộng hơn một chút (icon 85 + viền vẽ ngoài)
                avatarButtons[i].setPreferredSize(new Dimension(96, 96));
                avatarButtons[i].setFocusPainted(false);
                avatarButtons[i].setContentAreaFilled(false);
                avatarButtons[i].setBorderPainted(false);
                avatarButtons[i].setMargin(new Insets(0, 0, 0, 0)); // Bỏ padding
                avatarButtons[i].setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
                avatarButtons[i].addActionListener(e -> selectAvatar(currentButtonIndex));
                
                // Wrap button vào một FlowLayout để không bị GridLayout kéo giãn
                JPanel btnWrapper = new JPanel(new FlowLayout(FlowLayout.CENTER, 0, 0));
                btnWrapper.setOpaque(false);
                btnWrapper.add(avatarButtons[i]);
                
                gridPanel.add(btnWrapper);
            }
            centerWrapper.add(gridPanel, BorderLayout.CENTER);
            root.add(centerWrapper, BorderLayout.CENTER);
            // Cố tình không gọi selectAvatar(...) ở đây để ép người chơi phải ấn chọn
        }

        // --- BOTTOM: Nút Xác nhận ---
        JPanel bottomPanel = new JPanel(new FlowLayout(FlowLayout.CENTER, 10, 10));
        bottomPanel.setOpaque(false);

        JButton okButton = new JButton("Xác nhận") {
            private boolean hovered = false;
            {
                addMouseListener(new MouseAdapter() {
                    public void mouseEntered(MouseEvent e) { hovered = true; repaint(); }
                    public void mouseExited(MouseEvent e)  { hovered = false; repaint(); }
                });
            }
            @Override
            protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g;
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                GradientPaint gp = hovered
                    ? new GradientPaint(0, 0, new Color(220, 185, 140), 0, getHeight(), WOOD_DARK)
                    : new GradientPaint(0, 0, WOOD_LIGHT, 0, getHeight(), WOOD_DARK);
                g2.setPaint(gp);
                g2.fillRoundRect(0, 0, getWidth(), getHeight(), 20, 20); // Bo góc tròn mộc mạc
                g2.setColor(new Color(255, 255, 255, 40));
                g2.setStroke(new BasicStroke(1.5f));
                g2.drawRoundRect(1, 1, getWidth()-3, getHeight()-3, 20, 20);
                super.paintComponent(g);
            }
        };
        okButton.setFont(new Font("Times New Roman", Font.BOLD, 18));
        okButton.setForeground(Color.WHITE); // Chữ nổi bật trên gỗ
        okButton.setFocusPainted(false);
        okButton.setContentAreaFilled(false);
        okButton.setBorderPainted(false);
        okButton.setPreferredSize(new Dimension(160, 45));
        okButton.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        okButton.addActionListener(e -> {
            String entered = nameField.getText().trim();
            
            boolean isNameEmpty = entered.isEmpty() || entered.equals("Vui lòng nhập tên người chơi") || entered.equals("Vui lòng nhập tên BotAI");
            boolean isAvatarMissing = allowAvatarSelection && this.selectedAvatarIndex == -1;

            // Bắt lỗi tổng hợp: Cả 2 đều chưa có
            if (isNameEmpty && isAvatarMissing) {
                JOptionPane.showMessageDialog(this, "Vui lòng nhập Tên và chọn Avatars!", "Thông báo", JOptionPane.WARNING_MESSAGE);
                return; // Dừng lại
            }
            
            // Cảnh báo nếu chỉ chưa nhập tên
            if (isNameEmpty) {
                JOptionPane.showMessageDialog(this, allowAvatarSelection ? "Vui lòng nhập tên người chơi!" : "Vui lòng nhập tên BotAI!", "Thông báo", JOptionPane.WARNING_MESSAGE);
                return; // Dừng lại
            } 
            
            // Cảnh báo nếu chỉ chưa chọn Avatar
            if (isAvatarMissing) {
                JOptionPane.showMessageDialog(this, "Vui lòng chọn hình đại diện!", "Thông báo", JOptionPane.WARNING_MESSAGE);
                return; // Dừng lại
            }
            
            // Ghi nhận và thành công
            this.playerName = entered;
            this.confirmed = true;
            this.dispose();
        });

        bottomPanel.add(okButton);
        root.add(bottomPanel, BorderLayout.SOUTH);

        if (allowAvatarSelection) {
            setSize(520, 480); // Tăng kích thước hộp thoại để nhường chỗ cho Avatar lớn (cộng thêm thanh tiêu đề OS)
        } else {
            setSize(440, 220); // Tăng giới hạn dưới cho Form nhập BotAI để không đè nút bấm
        }
        setUndecorated(false);
        setLocationRelativeTo(parent);
        setDefaultCloseOperation(DISPOSE_ON_CLOSE);
        // Bo góc cửa sổ dialog nếu OS hỗ trợ
        try { setShape(new RoundRectangle2D.Double(0, 0, getWidth(), getHeight(), 20, 20)); }
        catch (Exception ignored) {}

        // Reset Focus khi cửa sổ vừa mở ra để tránh con trỏ tự nháy vào ô nhập tên
        this.addWindowListener(new java.awt.event.WindowAdapter() {
            @Override
            public void windowOpened(java.awt.event.WindowEvent e) {
                root.requestFocusInWindow();
            }
        });
    }

    private void selectAvatar(int index) {
        if (index < 0 || index > 5) return;
        this.selectedAvatarIndex = index;
        if (avatarButtons == null) return;
        for (int i = 0; i < 6; i++) {
            // Không set Border nữa vì đã vẽ vòng tròn trực tiếp trong paintComponent
            avatarButtons[i].setBorder(BorderFactory.createEmptyBorder());
            avatarButtons[i].repaint();
        }
    }

    public boolean isConfirmed()        { return confirmed; }
    public String getPlayerName()       { return playerName; }
    public int getSelectedAvatarIndex() { return selectedAvatarIndex; }
}
