package coganh;

import java.awt.Color;
import java.awt.Rectangle;
import java.awt.geom.Ellipse2D;
import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;

import javax.swing.JOptionPane;
import javax.swing.SwingUtilities;
import javax.swing.UIManager;

public class GameController {
    GamePanel_CoGanh gp;
    public ScoreManager sm = new ScoreManager();

    private final String SAVE_PVP = "save_pvp.txt";
    private final String SAVE_PVE = "save_pve.txt";

    public boolean chonBlue = true, chon, check, chuMo, moCo, checkOMo, checkOBiMo, huongDan, xemLichSu = false,
            isPvE = false;
    public String playerNameBlue = "Người Xanh";
    public String playerNameRed = "Người Đỏ";
    public int avatarBlueIndex = 0;
    public int avatarRedIndex = 1;
    public int oDaChon, end = 0, oBatBuoc = -1, trangHD = 0, trangLS = 0;
    public boolean timeoutEnd = false;

    public int cuonLichSu = 0;

    public int timeLeftBlue = 600;
    public int timeLeftRed = 600;
    public javax.swing.Timer countdownTimer;

    public int undoCount = 0;
    public static final int MAX_UNDO = 3;

    public int helpCount = 0;
    public static final int MAX_HELP = 3;

    public int doKhoAI = 5;
    public boolean dangChonDoKho = false;

    // --- PHÁT HIỆN LẶP VÔ HẠN ---
    // Lưu tần suất xuất hiện của từng trạng thái board (hash -> số lần)
    public HashMap<String, Integer> lichSuBoardHash = new HashMap<>();
    // Danh sách thứ tự các hash để BotAI tham chiếu
    public ArrayList<String> lichSuHashList = new ArrayList<>();
    // Nếu cùng 1 trạng thái xuất hiện >= MAX_LAP lần thì bot thua (lặp vô hạn)
    private static final int MAX_LAP = 6;

    // --- HỆ THỐNG THĂNG HẠNG TỰ ĐỘNG ---
    public int consecutiveWins = 0;          // Số ván thắng liên tiếp trong PvE
    private static final int WINS_TO_RANK_UP = 5; // Cần thắng bao nhiêu ván để thăng hạng
    public static final int WINS_PHASE1 = 3;     // 3 ván đầu ở độ khó gốc của rank
    public String endGameSubMsg = null;      // Thông điệp phụ hiển trong popup kết thúc ván

    /**
     * Tính độ khó thực tế cho bot trong ván hiện tại.
     * - 3 ván đầu (consecutiveWins < WINS_PHASE1): dùng doKhoAI gốc.
     * - 2 ván cuối (consecutiveWins >= WINS_PHASE1): tăng thêm 1 để thử thách.
     * - Hard (doKhoAI=5) hoặc đã max: giữ nguyên.
     */
    public int getEffectiveDoKho() {
        if (!isPvE) return doKhoAI;
        // Hard đã là max, không tăng thêm
        if (doKhoAI >= 5) return 5;
        // 2 ván cuối của chu kỳ 5 ván: tăng độ khó +1
        if (consecutiveWins >= WINS_PHASE1) {
            return doKhoAI + 1;
        }
        return doKhoAI;
    }

    public ArrayList<Integer> quanDo = new ArrayList<>();
    public ArrayList<Integer> quanXanh = new ArrayList<>();
    public ArrayList<Integer> listMo = new ArrayList<>();
    public ArrayList<Integer> listBiMo = new ArrayList<>();

    public int[][][] board = {
            { { 12, 12, -1 }, { 136, 12, -1 }, { 264, 12, -1 }, { 390, 12, -1 }, { 516, 12, -1 } },
            { { 12, 136, -1 }, {136, 136, 0 }, { 264, 140, 0 }, { 390, 140, 0 }, { 516, 140, -1 } },
            { { 12, 267, 1 }, { 136, 267, 0 }, { 264, 267, 0 }, { 390, 267, 0 }, { 516, 267, -1 } },
            { { 12, 395, 1 }, { 136, 395, 0 }, { 264, 395, 0 }, { 390, 395, 0 }, { 516, 395, 1 } },
            { { 12, 521, 1 }, { 136, 521, 1 }, { 264, 521, 1 }, { 390, 521, 1 }, {516, 521, 1 } }
    };

    // --- ANIMATION DATA ---
    public boolean isAnimating = false;
    public int animPieceType = 0;
    public int animDestRow = -1, animDestCol = -1;
    public double animCurrX = 0, animCurrY = 0;
    public double animStepX = 0, animStepY = 0;
    public int animFrames = 0;
    public javax.swing.Timer animTimer;

    public void startAnimation(int r1, int c1, int r2, int c2, int pieceType) {
        isAnimating = true;
        animPieceType = pieceType;
        animDestRow = r2;
        animDestCol = c2;

        animCurrX = board[r1][c1][0];
        animCurrY = board[r1][c1][1];
        double animEndX = board[r2][c2][0];
        double animEndY = board[r2][c2][1];

        int totalFrames = 15; // 15 frames at 16ms = ~240ms
        animStepX = (animEndX - animCurrX) / totalFrames;
        animStepY = (animEndY - animCurrY) / totalFrames;
        animFrames = totalFrames;

        if (animTimer != null)
            animTimer.stop();

        animTimer = new javax.swing.Timer(16, e -> {
            if (animFrames > 0) {
                animCurrX += animStepX;
                animCurrY += animStepY;
                animFrames--;
                gp.veLaiToanBo();
            } else {
                isAnimating = false;
                animTimer.stop();
                gp.veLaiToanBo();
            }
        });
        animTimer.start();
    }

    class GameStateSnapshot {
        int[][][] boardCopy = new int[5][5][3];
        boolean cBlue, cChon, cCheck, cChuMo, cMoCo, cCheckOMo, cCheckOBiMo;
        int cODaChon, cOBatBuoc;
        ArrayList<Integer> cQuanDo, cQuanXanh, cListMo, cListBiMo;

        public GameStateSnapshot() {
            for (int i = 0; i < 5; i++) {
                for (int j = 0; j < 5; j++) {
                    boardCopy[i][j][2] = board[i][j][2];
                }
            }
            cBlue = chonBlue;
            cChon = chon;
            cCheck = check;
            cChuMo = chuMo;
            cMoCo = moCo;
            cCheckOMo = checkOMo;
            cCheckOBiMo = checkOBiMo;
            cODaChon = oDaChon;
            cOBatBuoc = oBatBuoc;
            cQuanDo = new ArrayList<>(quanDo);
            cQuanXanh = new ArrayList<>(quanXanh);
            cListMo = new ArrayList<>(listMo);
            cListBiMo = new ArrayList<>(listBiMo);
        }

        public void restore() {
            for (int i = 0; i < 5; i++) {
                for (int j = 0; j < 5; j++) {
                    board[i][j][2] = boardCopy[i][j][2];
                }
            }
            chonBlue = cBlue;
            chon = cChon;
            check = cCheck;
            chuMo = cChuMo;
            moCo = cMoCo;
            checkOMo = cCheckOMo;
            checkOBiMo = cCheckOBiMo;
            oDaChon = cODaChon;
            oBatBuoc = cOBatBuoc;
            quanDo = new ArrayList<>(cQuanDo);
            quanXanh = new ArrayList<>(cQuanXanh);
            listMo = new ArrayList<>(cListMo);
            listBiMo = new ArrayList<>(cListBiMo);
        }
    }

    ArrayList<GameStateSnapshot> historyStack = new ArrayList<>();

    public GameController(GamePanel_CoGanh gp) {
        this.gp = gp;
        this.gp.gc = this;
        if (!gp.start) {
            gp.veMenu();
        } else {
            khoiTao();
            gp.veLaiToanBo();
        }
    }

    // ==========================================
    // BỘ ĐỒNG BỘ: QUÉT CHÍNH XÁC QUÂN TRÊN BÀN CỜ
    // ==========================================
    public void dongBoQuanCo() {
        quanDo.clear();
        quanXanh.clear();
        for (int i = 0; i < 5; i++) {
            for (int j = 0; j < 5; j++) {
                if (board[i][j][2] == 1) {
                    quanXanh.add(i * 10 + j);
                } else if (board[i][j][2] == -1) {
                    quanDo.add(i * 10 + j);
                }
            }
        }
    }

    public void kiemTraVaVaoGame(boolean cheDoPvE) {
        UIManager.put("Button.focus", new Color(0, 0, 0, 0));
        String fileKiemTra = cheDoPvE ? SAVE_PVE : SAVE_PVP;
        File f = new File(fileKiemTra);

        if (f.exists()) {
            String[] options = { "Chơi tiếp", "Ván mới" };
            int luaChon = JOptionPane.showOptionDialog(
                    gp, "Trận chiến cũ vẫn đang chờ bạn định đoạt. Tiếp tục chứ?", "Chào mừng trở lại!",
                    JOptionPane.YES_NO_OPTION, JOptionPane.QUESTION_MESSAGE, null, options, options[0]);

            if (luaChon == JOptionPane.YES_OPTION) {
                isPvE = cheDoPvE;
                if (loadGame(cheDoPvE)) {
                    gp.start = true;
                    if (gp.menuBar != null) {
                        gp.menuBar.setVisible(false);
                    }
                    khoiTaoTimer();
                    if (isPvE) {
                        historyStack.clear();
                        historyStack.add(new GameStateSnapshot());
                    }
                    if (isPvE && !chonBlue && end == 0) {
                        new Thread(() -> {
                            try {
                                Thread.sleep(500);
                            } catch (Exception e) {
                            }
                            botTurn();
                            gp.veLaiToanBo();
                        }).start();
                    }
                    gp.veLaiToanBo();
                } else
                    taoVanMoi(cheDoPvE);
            } else if (luaChon == JOptionPane.NO_OPTION) {
                deleteSaveFile(cheDoPvE);
                taoVanMoi(cheDoPvE);
            }
        } else
            taoVanMoi(cheDoPvE);
    }

    private void taoVanMoi(boolean cheDoPvE) {
        javax.swing.JFrame mainFrame = (javax.swing.JFrame) SwingUtilities.getWindowAncestor(gp);

        // Setup Player 1 (Blue)
        boolean setupBlue = false;
        while (!setupBlue) {
            PlayerSetupDialog dialogBlue = new PlayerSetupDialog(mainFrame, "Lựa chọn quân Xanh", gp.avatars, "Người Xanh", 1, true);
            dialogBlue.setVisible(true); 
            if (dialogBlue.isConfirmed()) {
                playerNameBlue = dialogBlue.getPlayerName();
                avatarBlueIndex = dialogBlue.getSelectedAvatarIndex();
                setupBlue = true;
            } else {
                int confirm = JOptionPane.showConfirmDialog(gp, "Bạn có chắc chắn muốn thoát không?", "Xác nhận", JOptionPane.YES_NO_OPTION);
                if (confirm == JOptionPane.YES_OPTION) {
                    return;
                }
            }
        }

        // Setup Player 2 (Red or Bot)
        boolean setupRed = false;
        while (!setupRed) {
            if (!cheDoPvE) {
                PlayerSetupDialog dialogRed = new PlayerSetupDialog(mainFrame, "Lựa chọn quân Đỏ", gp.avatars, "Người Đỏ", 2, true);
                dialogRed.setVisible(true);
                if (dialogRed.isConfirmed()) {
                    playerNameRed = dialogRed.getPlayerName();
                    avatarRedIndex = dialogRed.getSelectedAvatarIndex();
                    setupRed = true;
                } else {
                    int confirm = JOptionPane.showConfirmDialog(gp, "Bạn có chắc chắn muốn thoát không?", "Xác nhận", JOptionPane.YES_NO_OPTION);
                    if (confirm == JOptionPane.YES_OPTION) {
                        return;
                    }
                }
            } else {
                PlayerSetupDialog dialogBot = new PlayerSetupDialog(mainFrame, "Đặt tên cho BotAI", gp.avatars, "Bot", 0, false);
                dialogBot.setVisible(true);
                if (dialogBot.isConfirmed()) {
                    String botName = dialogBot.getPlayerName();
                    if (!botName.toLowerCase().contains("(ai)")) {
                        botName += " (AI)";
                    }
                    playerNameRed = botName;
                    avatarRedIndex = dialogBot.getSelectedAvatarIndex();
                    setupRed = true;
                } else {
                    int confirm = JOptionPane.showConfirmDialog(gp, "Bạn có chắc chắn muốn thoát không?", "Xác nhận", JOptionPane.YES_NO_OPTION);
                    if (confirm == JOptionPane.YES_OPTION) {
                        return;
                    }
                }
            }
        }

        isPvE = cheDoPvE;
        gp.start = true;
        if (gp.menuBar != null) {
            gp.menuBar.setVisible(false);
        }
        khoiTao();
        gp.veLaiToanBo();
    }

    public void choiLaiVanMoi() {
        int[][] maTranGoc = {
                { -1, -1, -1, -1, -1 },
                { -1, 0, 0, 0, -1 },
                { 1, 0, 0, 0, -1 },
                { 1, 0, 0, 0, 1 },
                { 1, 1, 1, 1, 1 }
        };

        for (int i = 0; i < 5; i++) {
            for (int j = 0; j < 5; j++) {
                board[i][j][2] = maTranGoc[i][j];
            }
        }

        chonBlue = true;
        chon = false;
        check = false;
        chuMo = false;
        moCo = false;
        checkOMo = false;
        checkOBiMo = false;
        end = 0;
        oBatBuoc = -1;
        listMo.clear();
        listBiMo.clear();
        khoiTao();
        gp.veLaiToanBo();
    }

    public void undoMove() {
        if (!isPvE || end != 0)
            return;

        if (!chonBlue) {
            JOptionPane.showMessageDialog(gp, "Vui lòng chờ Bot hoàn thành nước đi!", "Thông báo",
                    JOptionPane.WARNING_MESSAGE);
            return;
        }

        if (chon || moCo) {
            if (!historyStack.isEmpty()) {
                historyStack.get(historyStack.size() - 1).restore();
                gp.veLaiToanBo();
            }
            return;
        }

        if (historyStack.size() <= 1) {
            JOptionPane.showMessageDialog(gp, "Bạn chưa đi nước nào, không thể quay lại!", "Thông báo",
                    JOptionPane.INFORMATION_MESSAGE);
            return;
        }

        if (undoCount >= MAX_UNDO) {
            JOptionPane.showMessageDialog(gp, "Bạn đã sử dụng hết " + MAX_UNDO + " lượt đi lại trong ván này!",
                    "Hết lượt", JOptionPane.WARNING_MESSAGE);
            return;
        }

        historyStack.remove(historyStack.size() - 1);
        historyStack.get(historyStack.size() - 1).restore();
        undoCount++;
        gp.veLaiToanBo();
    }

    public void saveGame() {
        if (end == 0) {
            GameSaveData.saveToFile(this, isPvE ? SAVE_PVE : SAVE_PVP);
        }
    }

    public boolean loadGame(boolean modePvE) {
        return GameSaveData.loadFromFile(this, modePvE ? SAVE_PVE : SAVE_PVP);
    }

    public void deleteSaveFile(boolean cheDoPvE) {
        File f = new File(cheDoPvE ? SAVE_PVE : SAVE_PVP);
        if (f.exists())
            f.delete();
    }

    public void deleteCurrentSaveFile() {
        deleteSaveFile(this.isPvE);
    }

    void khoiTao() {
        timeLeftBlue = 600;
        timeLeftRed = 600;
        timeoutEnd = false;
        quanDo.clear();
        quanXanh.clear();
        for (int i = 0; i < 5; i++) {
            quanDo.add(i);
            quanXanh.add(40 + i);
        }
        for (int i = 0; i < 5; i += 4) {
            quanDo.add(10 + i);
            quanXanh.add(30 + i);
        }
        quanDo.add(24);
        quanXanh.add(20);
        undoCount = 0;
        helpCount = 0;
        // Reset lịch sử phát hiện lặp
        lichSuBoardHash.clear();
        lichSuHashList.clear();
        endGameSubMsg = null; // Reset thông điệp phụ
        if (isPvE) {
            historyStack.clear();
            historyStack.add(new GameStateSnapshot());
        }
        khoiTaoTimer();
    }

    public void khoiTaoTimer() {
        if (countdownTimer != null) {
            countdownTimer.stop();
        }
        if (isPvE)
            return; // Không sử dụng Timer trong chế độ PvE

        countdownTimer = new javax.swing.Timer(1000, new java.awt.event.ActionListener() {
            @Override
            public void actionPerformed(java.awt.event.ActionEvent e) {
                if (end != 0 || !gp.start || xemLichSu || huongDan) {
                    return;
                }

                if (chonBlue) {
                    timeLeftBlue--;
                    if (timeLeftBlue <= 0) {
                        end = -1; // Đỏ thắng
                        timeoutEnd = true;
                        countdownTimer.stop();
                        sm.addResult(end == 1 ? "XANH" : "DO", isPvE, end == 1 ? playerNameBlue : playerNameRed);
                        deleteCurrentSaveFile();
                    }
                } else {
                    if (isPvE)
                        return; // Máy đánh không tính thời gian hoặc tính gộp (tạm bỏ qua)
                    timeLeftRed--;
                    if (timeLeftRed <= 0) {
                        end = 1; // Xanh thắng
                        timeoutEnd = true;
                        countdownTimer.stop();
                        sm.addResult(end == 1 ? "XANH" : "DO", isPvE, end == 1 ? playerNameBlue : playerNameRed);
                        deleteCurrentSaveFile();
                    }
                }
                gp.veLaiToanBo();
            }
        });
        countdownTimer.start();
    }

    public BanCo createBanCoAo() {
        BanCo banCoAo = new BanCo();
        for (int i = 0; i < 5; i++) {
            for (int j = 0; j < 5; j++) {
                banCoAo.maTran[i][j] = board[i][j][2];
            }
        }
        return banCoAo;
    }

    // Tạo chuỗi hash đại diện cho trạng thái bàn cờ hiện tại
    private String taoHashBoardHienTai() {
        StringBuilder sb = new StringBuilder(25);
        for (int i = 0; i < 5; i++) {
            for (int j = 0; j < 5; j++) {
                int v = board[i][j][2];
                sb.append(v == 0 ? '0' : (v == 1 ? 'B' : 'R'));
            }
        }
        return sb.toString();
    }

    // Ghi nhận trạng thái board vào lịch sử
    public void ghiNhanBoardHienTai() {
        String hash = taoHashBoardHienTai();
        lichSuHashList.add(hash);
        lichSuBoardHash.merge(hash, 1, Integer::sum);
    }

    public void botTurn() {
        // Kiểm tra lặp vô hạn trước khi bot đi
        String hashHienTai = taoHashBoardHienTai();
        int soLanLap = lichSuBoardHash.getOrDefault(hashHienTai, 0);
        if (soLanLap >= MAX_LAP) {
            System.out.println("Bot lặp trạng thái " + soLanLap + " lần! Bot nhận thua do lặp vô hạn.");
            end = 1;
            sm.addResult("XANH", isPvE, playerNameBlue);
            deleteCurrentSaveFile();
            gp.veLaiToanBo();
            return;
        }

        BanCo banCoAo = createBanCoAo();
        BotAI ai = new BotAI();
        // Dùng độ khó thực tế (tăng ở 2 ván cuối của chu kỳ 5 ván)
        int doKhoHienTai = getEffectiveDoKho();
        System.out.println("[Bot] Độ khó thực tế: " + doKhoHienTai + " (rank=" + doKhoAI + ", ván thứ=" + (consecutiveWins + 1) + ")");
        NuocDi nuocDiTotNhat = ai.timNuocDiTotNhat(banCoAo, -1, doKhoHienTai, lichSuHashList);

        if (nuocDiTotNhat != null) {
            System.out.println("Bot quyết định đi: (" + nuocDiTotNhat.hangCu + ", " + nuocDiTotNhat.cotCu + ") -> ("
                    + nuocDiTotNhat.hangMoi + ", " + nuocDiTotNhat.cotMoi + ")");
            thucHienNuocDiBot(nuocDiTotNhat.hangCu, nuocDiTotNhat.cotCu, nuocDiTotNhat.hangMoi, nuocDiTotNhat.cotMoi);
            // Ghi nhận trạng thái board SAU khi bot di chuyển
            ghiNhanBoardHienTai();
        } else {
            System.out.println("CẢNH BÁO: Bot đã hết nước đi hợp lệ! NGƯỜI CHƠI THẮNG!");
            end = 1;
            sm.addResult("XANH", isPvE, playerNameBlue);
            deleteCurrentSaveFile();
            gp.veLaiToanBo();
        }
    }


    public void botKill() {
        if (oBatBuoc == -1) {
            botTurn();
            return;
        }
        int trap_r = oBatBuoc / 10;
        int trap_c = oBatBuoc % 10;
        ArrayList<int[]> blackPieces = new ArrayList<>();

        for (int i = 0; i < 5; i++) {
            for (int j = 0; j < 5; j++) {
                if (board[i][j][2] == 4)
                    blackPieces.add(new int[] { i, j });
            }
        }

        if (!blackPieces.isEmpty()) {
            System.out.println("Bot bị Người chơi Mở cờ! Tự động rơi vào bẫy.");
            int[] chosen = blackPieces.get(0);
            chonOSauKhiMoCo(chosen[0], chosen[1], 4);
            new Thread(() -> {
                try {
                    Thread.sleep(400);
                } catch (Exception e) {
                }
                chonOSauKhiMoCo(trap_r, trap_c, 3);
            }).start();
        } else {
            botTurn();
        }
    }

    private void thucHienNuocDiBot(int r1, int c1, int r2, int c2) {
        // Reset hết state Mở cờ từ lượt trước trước khi bot đi
        chuMo = false;
        listMo.clear();
        listBiMo.clear();
        chonO(r1, c1, false);
        if (chuMo && listMo.contains(r2 * 10 + c2)) {
            System.out.println("Bot vừa Mở cờ! Bắt buộc Người chơi phe Xanh phải Gánh.");
            moCo = true;
            chonOSauKhiMoCo(r2, c2, 3);
        } else {
            chuMo = false;
            listBiMo.clear();
            listMo.clear();
            chonO(r2, c2, true);
        }
    }

    public void goiYNuocDi() {
        if (!isPvE || !chonBlue || end != 0)
            return;

        if (helpCount >= MAX_HELP) {
            JOptionPane.showMessageDialog(gp, "Bạn đã hết 3 lần trợ giúp trong ván này!", "Hết lượt",
                    JOptionPane.WARNING_MESSAGE);
            return;
        }

        if (chon || moCo) {
            JOptionPane.showMessageDialog(gp, "Vui lòng bỏ chọn quân cờ hiện tại để nhận gợi ý mới!", "Gợi ý",
                    JOptionPane.INFORMATION_MESSAGE);
            return;
        }

        System.out.println("Đang tìm gợi ý tốt nhất cho bạn...");
        BanCo banCoAo = createBanCoAo();

        if (chuMo && oBatBuoc != -1) {
            banCoAo.hangBatBuoc = oBatBuoc / 10;
            banCoAo.cotBatBuoc = oBatBuoc % 10;
        }

        BotAI ai = new BotAI();
        NuocDi ndTotNhat = ai.timNuocDiTotNhat(banCoAo, 1, 4);

        if (ndTotNhat != null) {
            chonO(ndTotNhat.hangCu, ndTotNhat.cotCu, false);

            for (int i = 0; i < 5; i++) {
                for (int j = 0; j < 5; j++) {
                    if (board[i][j][2] == 3) {
                        if (i != ndTotNhat.hangMoi || j != ndTotNhat.cotMoi) {
                            board[i][j][2] = 0;
                        }
                    }
                }
            }

            helpCount++;
            gp.veLaiToanBo();
            System.out.println("Gợi ý: Đi từ (" + ndTotNhat.hangCu + "," + ndTotNhat.cotCu + ") tới ("
                    + ndTotNhat.hangMoi + "," + ndTotNhat.cotMoi + ")");
        } else {
            JOptionPane.showMessageDialog(gp, "Không tìm thấy nước đi an toàn nào lúc này!", "Xin lỗi",
                    JOptionPane.INFORMATION_MESSAGE);
        }
    }

    void xuLyChuot(int x_in, int y_in) {
        if (isAnimating)
            return;

        // Tỷ lệ co giãn dựa trên SCREEN_WIDTH (1000) và SCREEN_HEIGHT (600)
        int x = (int) (x_in * (1000.0 / gp.getWidth()));
        int y = (int) (y_in * (600.0 / gp.getHeight()));

        // 1. Click vào Sound
        if (!huongDan && !xemLichSu && new Rectangle(940, 520, 50, 50).contains(x, y)) {
            gp.soundOn = !gp.soundOn;
            if (!gp.soundOn)
                gp.music.stopMusic();
            else
                gp.music.batDau();
            gp.repaint();
            return;
        }

        // 2. Xử lý khi đang xem Hướng dẫn
        if (huongDan) {
            if (new Rectangle(810, 490, 50, 50).contains(x, y)) {
                if (trangHD < 2)
                    trangHD++;
                if (gp.hd[trangHD] != null)
                    gp.hdImg = gp.hd[trangHD];
                gp.repaint();
            } else if (new Rectangle(750, 490, 50, 50).contains(x, y)) {
                if (trangHD > 0)
                    trangHD--;
                if (gp.hd[trangHD] != null)
                    gp.hdImg = gp.hd[trangHD];
                gp.repaint();
            } else if (new Rectangle(15, 30, 50, 50).contains(x, y)) {
                huongDan = false;
                if (!gp.start)
                    gp.veMenu();
                else
                    gp.repaint();
            }
            return;
        }

        // 3. Xử lý khi đang xem Lịch sử
        if (xemLichSu) {
            if (new Rectangle(60, 30, 50, 50).contains(x, y)) {
                xemLichSu = false;
                trangLS = 0;
                if (!gp.start)
                    gp.veMenu();
                else
                    gp.repaint();
            } else if (trangLS == 1 && new Rectangle(380, 485, 40, 40).contains(x, y)) {
                trangLS = 0;
                if (!gp.start)
                    gp.veMenu();
                else
                    gp.repaint();
            } else if (trangLS == 0 && new Rectangle(580, 485, 40, 40).contains(x, y)) {
                trangLS = 1;
                if (!gp.start)
                    gp.veMenu();
                else
                    gp.repaint();
            }
            return;
        }

        int xanhPanelX = 20;
        int doPanelX = 800;
        int panelY = 20;

        // 4. Xử lý khi đang trong ván chơi
        if (gp.start) {
            // Nút "Trở về" góc trái dưới: g.fillRoundRect(10, 540, 100, 45, 20, 20);
            if (new Rectangle(10, 540, 100, 45).contains(x, y)) {
                if (end == 0)
                    saveGame();
                else
                    deleteCurrentSaveFile();
                gp.resetGame();
                return;
            }

            if (isPvE && end == 0) {
                int boxX = xanhPanelX + 25;
                int boxY = panelY + 350;
                if (new Rectangle(boxX, boxY, 130, 50).contains(x, y)) {
                    if (x >= boxX && x <= boxX + 65) {
                        goiYNuocDi();
                        return;
                    } else if (x >= boxX + 65 && x <= boxX + 130) {
                        undoMove();
                        return;
                    }
                }
            }

            if (end == 0) {
                if (isPvE && !chonBlue)
                    return;
                int moPanelX = chonBlue ? xanhPanelX : doPanelX;
                int moCoX = moPanelX + 40;
                int moCoY = panelY + 278;
                if (chuMo && new Rectangle(moCoX, moCoY, 100, 35).contains(x, y)) {
                    if (!moCo) {
                        moCo = true;
                        setMoCo();
                    } else {
                        moCo = false;
                        int i = oDaChon / 10, j = oDaChon % 10;
                        if (chonBlue)
                            board[i][j][2] = 1;
                        else
                            board[i][j][2] = -1;
                        chonO(i, j, true);
                    }
                } else {
                    // Loop qua tát cả điểm neo trên board_paint được vẽ ở x=225, y=25
                    for (int r = 0; r < 5; r++) {
                        for (int c = 0; c < 5; c++) {
                            Ellipse2D elip = new Ellipse2D.Float(board[r][c][0], board[r][c][1], 30, 30);
                            if (elip.contains(x - 225, y - 25)) {
                                if (!moCo)
                                    chonO(r, c, true);
                                else
                                    chonOSauKhiMoCo(r, c, board[r][c][2]);
                            }
                        }
                    }
                }
            } else {
                // End game dialog "Tiếp tục": vẽ ở y=345 (bình thường) hoặc y=375 (có sub-msg)
                // Offset bảng 225, 25 -> 225+130=355; y: 25+295=320 đến 25+430=455
                if (new Rectangle(355, 315, 240, 140).contains(x, y)) {
                    deleteCurrentSaveFile();
                    choiLaiVanMoi();
                }
            }
        } else {
            // Xử lý tại Menu chính (không vẽ chữ nhưng vẫn nhận diện click nếu cần)
            if (dangChonDoKho) {
                // ... (có thể lược bỏ vì dùng MenuBar)
                if (new Rectangle(40, 20, 300, 50).contains(x, y)) {
                    doKhoAI = 1;
                    dangChonDoKho = false;
                    kiemTraVaVaoGame(true);
                } else if (new Rectangle(40, 90, 300, 50).contains(x, y)) {
                    doKhoAI = 3;
                    dangChonDoKho = false;
                    kiemTraVaVaoGame(true);
                } else if (new Rectangle(40, 160, 240, 50).contains(x, y)) {
                    doKhoAI = 5;
                    dangChonDoKho = false;
                    kiemTraVaVaoGame(true);
                } else if (new Rectangle(40, 230, 240, 50).contains(x, y)) {
                    dangChonDoKho = false;
                    gp.veMenu();
                }
            }
        }
    }

    private void setMoCo() {
        int i = oDaChon / 10, j = oDaChon % 10;
        for (int a = -1; a < 2; a++) {
            if (i + a < 0 || i + a > 4)
                continue;
            for (int b = -1; b < 2; b++) {
                if (j + b < 0 || j + b > 4 || (a == 0 && b == 0))
                    continue;
                if ((i + j) % 2 != 0 && a != 0 && b != 0)
                    continue;
                if (board[i + a][j + b][2] == 3 && !listMo.contains(Integer.valueOf((i + a) * 10 + j + b))) {
                    board[i + a][j + b][2] = 0;
                }
            }
        }
        gp.veLaiToanBo();
    }

    void chonO(int i, int j, boolean veLai) {
        int o = board[i][j][2];
        if (!moCo) {
            if ((chonBlue && o == 1) || (!chonBlue && o == -1)) {
                if (!chon) {
                    chon = true;
                    oDaChon = i * 10 + j;
                    if (chonBlue)
                        board[i][j][2] = 2;
                    else
                        board[i][j][2] = -2;
                    for (int a = -1; a < 2; a++) {
                        if (!ktra(i + a))
                            continue;
                        for (int b = -1; b < 2; b++) {
                            if (!ktra(j + b) || (a == 0 && b == 0))
                                continue;
                            if ((i + j) % 2 != 0 && a != 0 && b != 0)
                                continue;
                            if (board[i + a][j + b][2] == 0) {
                                board[i + a][j + b][2] = 3;
                                mo(i + a, j + b);
                            }
                        }
                    }
                } else {
                    int oCu_i = oDaChon / 10;
                    int oCu_j = oDaChon % 10;
                    for (int a = -1; a < 2; a++) {
                        if (!ktra(oCu_i + a))
                            continue;
                        for (int b = -1; b < 2; b++) {
                            if (!ktra(oCu_j + b))
                                continue;
                            if (board[oCu_i + a][oCu_j + b][2] == 3)
                                board[oCu_i + a][oCu_j + b][2] = 0;
                            if (chonBlue)
                                board[oCu_i][oCu_j][2] = 1;
                            else
                                board[oCu_i][oCu_j][2] = -1;
                        }
                    }
                    chon = false;
                    if (chuMo) {
                        listBiMo.clear();
                        listMo.clear();
                        chuMo = false;
                    }
                    chonO(i, j, true);
                    return;
                }
            }
        }
        if (o == 3) {
            int oCu_i = oDaChon / 10;
            int oCu_j = oDaChon % 10;
            check = false;
            for (int a = -1; a < 2; a++) {
                if (!ktra(oCu_i + a))
                    continue;
                for (int b = -1; b < 2; b++) {
                    if (!ktra(oCu_j + b))
                        continue;
                    if (board[oCu_i + a][oCu_j + b][2] == 3 || (a == 0 && b == 0))
                        board[oCu_i + a][oCu_j + b][2] = 0;
                }
            }
            if (chonBlue) {
                startAnimation(oCu_i, oCu_j, i, j, 1);
                board[i][j][2] = 1;
                quanXanh.remove(Integer.valueOf(oDaChon));
                quanXanh.add(i * 10 + j);
            } else {
                startAnimation(oCu_i, oCu_j, i, j, -1);
                board[i][j][2] = -1;
                quanDo.remove(Integer.valueOf(oDaChon));
                quanDo.add(i * 10 + j);
            }

            ktraGanh(i, j, board[i][j][2]);
            dongBoQuanCo(); // Đồng bộ sau khi Gánh

            vayQuan(board[i][j][2]);
            dongBoQuanCo(); // Đồng bộ sau khi Vây

            chon = false;
            chonBlue = !chonBlue;

            kiemTraKetThuc();

            if (!moCo && veLai)
                chuMo = false;

            xuLyLuotTiepTheo();
        }
        if (veLai)
            gp.veLaiToanBo();
    }

    private void chonOSauKhiMoCo(int i, int j, int o) {
        if (o == 3) {
            if (chuMo) {
                oBatBuoc = oDaChon;
                chonO(i, j, false);
                listMo.clear();
                while (listBiMo.size() > 0) {
                    int a = listBiMo.remove(0);
                    if ((chonBlue && board[a / 10][a % 10][2] == 1) || (!chonBlue && board[a / 10][a % 10][2] == -1))
                        board[a / 10][a % 10][2] = 4;
                    while (listBiMo.remove(Integer.valueOf(a))) {
                    }
                    chuMo = false;
                }
            } else {
                int oCu_i = oDaChon / 10;
                int oCu_j = oDaChon % 10;
                check = false;
                for (int a = -1; a < 2; a++) {
                    if (!ktra(i + a))
                        continue;
                    for (int b = -1; b < 2; b++) {
                        if (!ktra(j + b))
                            continue;
                        if (board[i + a][j + b][2] == 4 || (a == 0 && b == 0))
                            if (chonBlue)
                                board[i + a][j + b][2] = 1;
                            else
                                board[i + a][j + b][2] = -1;
                    }
                }
                board[oCu_i][oCu_j][2] = 0;

                // Dọn dẹp: reset tất cả quân còn kẹt ở trạng thái 4 (đen) trên toàn bàn cờ
                for (int ii = 0; ii < 5; ii++) {
                    for (int jj = 0; jj < 5; jj++) {
                        if (board[ii][jj][2] == 4) {
                            board[ii][jj][2] = chonBlue ? 1 : -1;
                        }
                    }
                }

                if (chonBlue) {
                    startAnimation(oCu_i, oCu_j, i, j, 1);
                    board[i][j][2] = 1;
                    quanXanh.remove(Integer.valueOf(oDaChon));
                    quanXanh.add(i * 10 + j);
                } else {
                    startAnimation(oCu_i, oCu_j, i, j, -1);
                    board[i][j][2] = -1;
                    quanDo.remove(Integer.valueOf(oDaChon));
                    quanDo.add(i * 10 + j);
                }

                ktraGanh(i, j, board[i][j][2]);
                dongBoQuanCo(); // Đồng bộ

                vayQuan(board[i][j][2]);
                dongBoQuanCo(); // Đồng bộ

                chon = false;
                chonBlue = !chonBlue;

                kiemTraKetThuc();

                xuLyLuotTiepTheo();
                moCo = false;
            }
        } else if (o == 4) {
            if (!chon) {
                chon = true;
                oDaChon = i * 10 + j;
                if (chonBlue)
                    board[i][j][2] = 2;
                else
                    board[i][j][2] = -2;
                board[oBatBuoc / 10][oBatBuoc % 10][2] = 3;
            } else {
                chon = false;
                int oCu_i = oDaChon / 10, oCu_j = oDaChon % 10;
                board[oCu_i][oCu_j][2] = 4;
                chonOSauKhiMoCo(i, j, board[i][j][2]);
            }
        }
        gp.veLaiToanBo();
    }

    private void anQuan(int i1, int j1, int o, int i2, int j2) {
        board[i1][j1][2] = o;
        board[i2][j2][2] = o;
    }

    private boolean xuLyCapGanh(int r1, int c1, int r2, int c2, int o) {
        if (board[r1][c1][2] == -o && board[r2][c2][2] == -o) {
            if (checkOMo) {
                chuMo = true;
                check = true;
            } else if (checkOBiMo) {
                if (!listBiMo.isEmpty() && (listBiMo.get(listBiMo.size() - 1) == r1 * 10 + c1
                        || listBiMo.get(listBiMo.size() - 1) == r2 * 10 + c2)) {
                    checkOBiMo = false;
                    return false;
                }
            } else {
                anQuan(r1, c1, o, r2, c2);
            }
        }
        return true;
    }

    void ktraGanh(int i, int j, int o) {
        if (ktra(j + 1) && ktra(j - 1)) {
            if (!xuLyCapGanh(i, j + 1, i, j - 1, o))
                return;
            if ((i + j) % 2 == 0 && ktra(i + 1) && ktra(i - 1)) {
                for (int b = -1; b < 2; b += 2) {
                    if (!xuLyCapGanh(i + 1, j + b, i - 1, j - b, o))
                        return;
                }
            }
        }
        if (ktra(i + 1) && ktra(i - 1)) {
            if (!xuLyCapGanh(i + 1, j, i - 1, j, o))
                return;
        }
    }

    void vayQuan(int o) {
        BanCo bc = createBanCoAo();
        bc.xulyVay(o);
        for (int i = 0; i < 5; i++) {
            for (int j = 0; j < 5; j++) {
                board[i][j][2] = bc.maTran[i][j];
            }
        }
    }

    private void mo(int oDiDen_i, int oDiDen_j) {
        int o;
        check = false;
        if (chonBlue)
            o = 1;
        else
            o = -1;
        int i = oDaChon / 10, j = oDaChon % 10;

        for (int a = -1; a < 2; a++) {
            if (!ktra(i + a))
                continue;
            for (int b = -1; b < 2; b++) {
                if (!ktra(j + b) || (a == 0 && b == 0))
                    continue;
                if ((i + j) % 2 != 0 && a != 0 && b != 0)
                    continue;

                int oBiMo_i = i + a, oBiMo_j = j + b;
                if (board[oBiMo_i][oBiMo_j][2] == -o) {
                    listBiMo.add(oBiMo_i * 10 + oBiMo_j);
                    checkOBiMo = true;
                    ktraGanh(oDiDen_i, oDiDen_j, o);
                    if (!checkOBiMo) {
                        listBiMo.remove(listBiMo.size() - 1);
                        continue;
                    } else
                        check = true;
                    checkOBiMo = false;
                }
                if (check && listBiMo.size() > 0) {
                    check = false;
                    if (ktra(2 * i - oDiDen_i) && ktra(2 * j - oDiDen_j)
                            && board[2 * i - oDiDen_i][2 * j - oDiDen_j][2] == o) {
                        chuMo = true;
                        check = true;
                    } else {
                        checkOMo = true;
                        ktraGanh(i, j, -o);
                        checkOMo = false;
                    }
                    if (!listMo.contains(Integer.valueOf(oDiDen_i * 10 + oDiDen_j)) && check)
                        listMo.add(oDiDen_i * 10 + oDiDen_j);
                    else if (!check)
                        listBiMo.remove(listBiMo.size() - 1);

                    if (!chuMo) {
                        listBiMo.clear();
                        listMo.clear();
                    }
                    check = false;
                }
            }
        }
    }

    private boolean ktra(int a) {
        return a >= 0 && a < 5;
    }

    private void kiemTraKetThuc() {
        if (quanDo.size() == 0) {
            end = 1;
            sm.addResult("XANH", isPvE, playerNameBlue);
            deleteCurrentSaveFile();
            xuLyThangHang(true);
        } else if (quanXanh.size() == 0) {
            end = -1;
            sm.addResult("DO", isPvE, playerNameRed);
            deleteCurrentSaveFile();
            xuLyThangHang(false);
        } else {
            BanCo bc = createBanCoAo();
            if (chuMo && oBatBuoc != -1) {
                bc.hangBatBuoc = oBatBuoc / 10;
                bc.cotBatBuoc = oBatBuoc % 10;
            }

            int pheHienTai = chonBlue ? 1 : -1;
            if (bc.layCacNuocDiHopLe(pheHienTai).isEmpty()) {
                if (chonBlue) {
                    System.out.println("Xanh bị vây chặt hết đường đi! ĐỎ THẮNG!");
                    end = -1;
                    sm.addResult("DO", isPvE, playerNameRed);
                    deleteCurrentSaveFile();
                    xuLyThangHang(false);
                } else {
                    System.out.println("Đỏ bị vây chặt hết đường đi! XANH THẮNG!");
                    end = 1;
                    sm.addResult("XANH", isPvE, playerNameBlue);
                    deleteCurrentSaveFile();
                    xuLyThangHang(true);
                }
            }
        }
    }

    /**
     * Xử lý hệ thống thăng hạng tự động trong chế độ PvE.
     * @param nguoiThang true nếu người chơi (Xanh) thắng, false nếu thua
     */
    private void xuLyThangHang(boolean nguoiThang) {
        if (!isPvE) return; // Chỉ áp dụng trong PvE

        if (nguoiThang) {
            consecutiveWins++;
            System.out.println("Chuỗi thắng: " + consecutiveWins + "/" + WINS_TO_RANK_UP);

            if (consecutiveWins >= WINS_TO_RANK_UP) {
                // Đủ 5 ván → THĂNG HẠNG
                int doKhoCu = doKhoAI;
                if (doKhoAI == 1) {
                    doKhoAI = 3; // Easy → Medium
                } else if (doKhoAI == 3) {
                    doKhoAI = 5; // Medium → Hard
                }
                consecutiveWins = 0;

                if (doKhoAI != doKhoCu) {
                    String capBacMoi = getRankName(doKhoAI);
                    endGameSubMsg = "🏆 Thăng hạng lên " + capBacMoi + "! Xuất sắc!";
                    System.out.println("THĂNG HẠNG! Độ khó: " + doKhoCu + " → " + doKhoAI);
                } else {
                    // Đã ở mức Hard rồi
                    endGameSubMsg = "🏆 Đỉnh cao! Chinh phục tất cả cấp độ!";
                }
            } else {
                // Chưa đủ 5 ván — thông báo tiến độ theo phase
                if (consecutiveWins == WINS_PHASE1) {
                    // Vừa đạt 3 thắng → bước vào Phase 2 (độ khó tăng)
                    int doKhoGiai2 = (doKhoAI < 5) ? doKhoAI + 1 : 5;
                    endGameSubMsg = "🏆 Phase 2! Bot khó hơn (lv " + doKhoGiai2 + "). Còn " + (WINS_TO_RANK_UP - consecutiveWins) + " ván!";
                } else if (consecutiveWins < WINS_PHASE1) {
                    // Đang trong Phase 1 (độ khó gốc)
                    endGameSubMsg = "🏆 Phase 1: " + consecutiveWins + "/" + WINS_PHASE1 + " ván. Còn " + (WINS_TO_RANK_UP - consecutiveWins) + " ván nữa!";
                } else {
                    // Đang trong Phase 2
                    int p2done = consecutiveWins - WINS_PHASE1;
                    int p2total = WINS_TO_RANK_UP - WINS_PHASE1;
                    endGameSubMsg = "🏆 Phase 2: " + p2done + "/" + p2total + " ván. Còn " + (WINS_TO_RANK_UP - consecutiveWins) + " ván nữa!";
                }
            }
        } else {
            // Thua: reset chuỗi thắng
            if (consecutiveWins > 0) {
                endGameSubMsg = "💔 Gãy chuỗi! (" + consecutiveWins + " thắng). Về " + getRankName(doKhoAI);
                System.out.println("Thua! Chuỗi thắng bị reset (" + consecutiveWins + " → 0)");
            }
            consecutiveWins = 0;
        }
    }

    /** Trả về tên rank theo doKhoAI */
    private String getRankName(int dk) {
        if (dk == 1) return "Easy";
        if (dk == 3) return "Medium";
        return "Hard";
    }


    private void xuLyLuotTiepTheo() {
        if (isPvE && end == 0) {
            if (chonBlue && chuMo) {
                new Thread(() -> {
                    try {
                        Thread.sleep(300);
                    } catch (Exception e) {
                    }
                    SwingUtilities.invokeLater(() -> {
                        moCo = true;
                        setMoCo();
                        if (end == 0 && chonBlue)
                            historyStack.add(new GameStateSnapshot());
                    });
                }).start();
            } else if (!chonBlue && chuMo) {
                new Thread(() -> {
                    try {
                        Thread.sleep(300);
                    } catch (Exception e) {
                    }
                    SwingUtilities.invokeLater(() -> {
                        moCo = true;
                        setMoCo();
                    });
                    try {
                        Thread.sleep(500);
                    } catch (Exception e) {
                    }
                    SwingUtilities.invokeLater(() -> {
                        botKill();
                        if (end == 0 && chonBlue)
                            historyStack.add(new GameStateSnapshot());
                    });
                }).start();
            } else if (!chonBlue && !chuMo) {
                new Thread(() -> {
                    try {
                        Thread.sleep(500);
                    } catch (Exception e) {
                    }
                    SwingUtilities.invokeLater(() -> {
                        botTurn();
                        if (end == 0 && chonBlue)
                            historyStack.add(new GameStateSnapshot());
                    });
                }).start();
            }
        } else if (!isPvE && end == 0) {
            if (!chuMo) {
                SwingUtilities.invokeLater(() -> {
                    historyStack.add(new GameStateSnapshot());
                });
            }
        }
    }
}