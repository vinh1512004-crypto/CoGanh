package coganh;

import java.awt.Color;
import java.awt.Rectangle;
import java.awt.geom.Ellipse2D;
import java.io.File;
import java.util.ArrayList;

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
    public int oDaChon, end = 0, oBatBuoc = -1, trangHD = 0, trangLS = 0;

    public int cuonLichSu = 0;

    public int timeLeftBlue = 600;
    public int timeLeftRed = 600;
    public javax.swing.Timer countdownTimer;

    public int undoCount = 0;
    public static final int MAX_UNDO = 3;

    public int helpCount = 0;
    public static final int MAX_HELP = 3;

    public int doKhoAI = 4;
    public boolean dangChonDoKho = false;

    public ArrayList<Integer> quanDo = new ArrayList<>();
    public ArrayList<Integer> quanXanh = new ArrayList<>();
    public ArrayList<Integer> listMo = new ArrayList<>();
    public ArrayList<Integer> listBiMo = new ArrayList<>();

    public int[][][] board = {
            { { 35, 35, -1 }, { 160, 35, -1 }, { 285, 35, -1 }, { 410, 35, -1 }, { 535, 35, -1 } },
            { { 35, 160, -1 }, { 160, 160, 0 }, { 285, 160, 0 }, { 410, 160, 0 }, { 535, 160, -1 } },
            { { 35, 285, 1 }, { 160, 285, 0 }, { 285, 285, 0 }, { 410, 285, 0 }, { 535, 285, -1 } },
            { { 35, 410, 1 }, { 160, 410, 0 }, { 285, 410, 0 }, { 410, 410, 0 }, { 535, 410, 1 } },
            { { 35, 535, 1 }, { 160, 535, 1 }, { 285, 535, 1 }, { 410, 535, 1 }, { 535, 535, 1 } }
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
        isPvE = cheDoPvE;
        gp.start = true;
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
                        JOptionPane.showMessageDialog(gp, "Hết thời gian! Quân Đỏ Thắng!", "Kết thúc",
                                JOptionPane.INFORMATION_MESSAGE);
                        countdownTimer.stop();
                        sm.addResult(end == 1 ? "XANH" : "DO", isPvE);
                        deleteCurrentSaveFile();
                    }
                } else {
                    if (isPvE)
                        return; // Máy đánh không tính thời gian hoặc tính gộp (tạm bỏ qua)
                    timeLeftRed--;
                    if (timeLeftRed <= 0) {
                        end = 1; // Xanh thắng
                        JOptionPane.showMessageDialog(gp, "Hết thời gian! Quân Xanh Thắng!", "Kết thúc",
                                JOptionPane.INFORMATION_MESSAGE);
                        countdownTimer.stop();
                        sm.addResult(end == 1 ? "XANH" : "DO", isPvE);
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

    public void botTurn() {
        BanCo banCoAo = createBanCoAo();
        BotAI ai = new BotAI();
        NuocDi nuocDiTotNhat = ai.timNuocDiTotNhat(banCoAo, -1, doKhoAI);

        if (nuocDiTotNhat != null) {
            System.out.println("Bot quyết định đi: (" + nuocDiTotNhat.hangCu + ", " + nuocDiTotNhat.cotCu + ") -> ("
                    + nuocDiTotNhat.hangMoi + ", " + nuocDiTotNhat.cotMoi + ")");
            thucHienNuocDiBot(nuocDiTotNhat.hangCu, nuocDiTotNhat.cotCu, nuocDiTotNhat.hangMoi, nuocDiTotNhat.cotMoi);
        } else {
            System.out.println("CẢNH BÁO: Bot đã hết nước đi hợp lệ! NGƯỜI CHƠI THẮNG!");
            end = 1;
            sm.addResult("XANH", isPvE);
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

        // 4. Xử lý khi đang trong ván chơi
        if (gp.start) {
            if (new Rectangle(895, 20, 100, 50).contains(x, y)) {
                if (end == 0)
                    saveGame();
                else
                    deleteCurrentSaveFile();
                gp.resetGame();
                return;
            }

            if (isPvE && end == 0) {
                // KIỂM TRA CLICK CÁC NÚT PVE VỚI TOẠ ĐỘ VÙNG CLICK ĐÃ ĐƯỢC CHỈNH CHUẨN XÁC
                if (y >= 440 && y <= 510) {
                    if (x >= 740 && x <= 780) {
                        goiYNuocDi();
                        return;
                    }
                    else if (x >= 800 && x <= 860) {
                        undoMove();
                        return;
                    }
                }
            }

            if (end == 0) {
                if (isPvE && !chonBlue)
                    return;
                if (chuMo && new Rectangle(720, 265, 60, 40).contains(x, y)) {
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
                    int j = (x - 85) / 125, i = (y - 35) / 125;
                    if (i < 5 && j < 5) {
                        Ellipse2D elip = new Ellipse2D.Float(board[i][j][0], board[i][j][1], 30, 30);
                        if (elip.contains(x - 50, y)) {
                            if (!moCo)
                                chonO(i, j, true);
                            else
                                chonOSauKhiMoCo(i, j, board[i][j][2]);
                        }
                    }
                }
            } else {
                if (new Rectangle(180, 290, 240, 80).contains(x, y)) {
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
                    doKhoAI = 2;
                    dangChonDoKho = false;
                    kiemTraVaVaoGame(true);
                } else if (new Rectangle(40, 160, 240, 50).contains(x, y)) {
                    doKhoAI = 4;
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
            sm.addResult("XANH", isPvE);
            deleteCurrentSaveFile();
        } else if (quanXanh.size() == 0) {
            end = -1;
            sm.addResult("DO", isPvE);
            deleteCurrentSaveFile();
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
                    sm.addResult("DO", isPvE);
                    deleteCurrentSaveFile();
                } else {
                    System.out.println("Đỏ bị vây chặt hết đường đi! XANH THẮNG!");
                    end = 1;
                    sm.addResult("XANH", isPvE);
                    deleteCurrentSaveFile();
                }
            }
        }
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