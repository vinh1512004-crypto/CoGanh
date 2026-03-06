package coganh;

import java.awt.Color;
import java.awt.Rectangle;
import java.awt.geom.Ellipse2D;
import java.io.File;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.Scanner;

import javax.swing.JOptionPane;
import javax.swing.SwingUtilities;
import javax.swing.UIManager;

public class GameController {
    GamePanel_CoGanh gp;
    public ScoreManager sm = new ScoreManager();
    
    private final String SAVE_PVP = "save_pvp.txt";
    private final String SAVE_PVE = "save_pve.txt";

    public boolean chonBlue = true, chon, check, chuMo, moCo, checkOMo, checkOBiMo, huongDan, xemLichSu = false, isPvE = false;
    public int oDaChon, end = 0, oBatBuoc = -1, trangHD = 0, trangLS = 0;
    
    public int cuonLichSu = 0;

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

    class GameStateSnapshot {
        int[][][] boardCopy = new int[5][5][3];
        boolean cBlue, cChon, cCheck, cChuMo, cMoCo, cCheckOMo, cCheckOBiMo;
        int cODaChon, cOBatBuoc;
        ArrayList<Integer> cQuanDo, cQuanXanh, cListMo, cListBiMo;

        public GameStateSnapshot() {
            for (int i=0; i<5; i++) {
                for (int j=0; j<5; j++) {
                    boardCopy[i][j][2] = board[i][j][2];
                }
            }
            cBlue = chonBlue; cChon = chon; cCheck = check;
            cChuMo = chuMo; cMoCo = moCo; cCheckOMo = checkOMo; cCheckOBiMo = checkOBiMo;
            cODaChon = oDaChon; cOBatBuoc = oBatBuoc;
            cQuanDo = new ArrayList<>(quanDo); cQuanXanh = new ArrayList<>(quanXanh);
            cListMo = new ArrayList<>(listMo); cListBiMo = new ArrayList<>(listBiMo);
        }

        public void restore() {
            for (int i=0; i<5; i++) {
                for (int j=0; j<5; j++) {
                    board[i][j][2] = boardCopy[i][j][2];
                }
            }
            chonBlue = cBlue; chon = cChon; check = cCheck;
            chuMo = cChuMo; moCo = cMoCo; checkOMo = cCheckOMo; checkOBiMo = cCheckOBiMo;
            oDaChon = cODaChon; oBatBuoc = cOBatBuoc;
            quanDo = new ArrayList<>(cQuanDo); quanXanh = new ArrayList<>(cQuanXanh);
            listMo = new ArrayList<>(cListMo); listBiMo = new ArrayList<>(cListBiMo);
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

    private void kiemTraVaVaoGame(boolean cheDoPvE) {
        UIManager.put("Button.focus", new Color(0, 0, 0, 0));
        String fileKiemTra = cheDoPvE ? SAVE_PVE : SAVE_PVP;
        File f = new File(fileKiemTra);
        
        if (f.exists()) {
            String[] options = {"Chơi tiếp", "Ván mới"};
            int luaChon = JOptionPane.showOptionDialog(
                    gp, "Trận chiến cũ vẫn đang chờ bạn định đoạt. Tiếp tục chứ?", "Chào mừng trở lại!",
                    JOptionPane.YES_NO_OPTION, JOptionPane.QUESTION_MESSAGE, null, options, options[0]
            );

            if (luaChon == JOptionPane.YES_OPTION) { 
                isPvE = cheDoPvE;
                if (loadGame(cheDoPvE)) {
                    gp.start = true;
                    if (isPvE) {
                        historyStack.clear();
                        historyStack.add(new GameStateSnapshot());
                    }
                    if (isPvE && !chonBlue && end == 0) {
                        new Thread(() -> {
                            try { Thread.sleep(500); } catch (Exception e) {}
                            botTurn();
                            gp.veLaiToanBo();
                        }).start();
                    }
                    gp.veLaiToanBo();
                } else taoVanMoi(cheDoPvE);
            } else if (luaChon == JOptionPane.NO_OPTION) { 
                deleteSaveFile(cheDoPvE);
                taoVanMoi(cheDoPvE);
            }
        } else taoVanMoi(cheDoPvE);
    }

    private void taoVanMoi(boolean cheDoPvE) {
        isPvE = cheDoPvE;
        gp.start = true;
        khoiTao();
        gp.veLaiToanBo();
    }

    public void choiLaiVanMoi() {
        int[][] maTranGoc = { 
            {-1, -1, -1, -1, -1},
            {-1,  0,  0,  0, -1},
            { 1,  0,  0,  0, -1},
            { 1,  0,  0,  0,  1},
            { 1,  1,  1,  1,  1}
        };
        
        for (int i = 0; i < 5; i++) {
            for (int j = 0; j < 5; j++) { 
                board[i][j][2] = maTranGoc[i][j]; 
            }
        }
        
        chonBlue = true; chon = false; check = false; chuMo = false; moCo = false;
        checkOMo = false; checkOBiMo = false; end = 0; oBatBuoc = -1;
        listMo.clear(); listBiMo.clear();
        khoiTao();
        gp.veLaiToanBo();
    }

    public void undoMove() {
        if (!isPvE || end != 0) return;
        
        if (!chonBlue) {
            JOptionPane.showMessageDialog(gp, "Vui lòng chờ Bot hoàn thành nước đi!", "Thông báo", JOptionPane.WARNING_MESSAGE);
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
            JOptionPane.showMessageDialog(gp, "Bạn chưa đi nước nào, không thể quay lại!", "Thông báo", JOptionPane.INFORMATION_MESSAGE);
            return;
        }

        if (undoCount >= MAX_UNDO) {
            JOptionPane.showMessageDialog(gp, "Bạn đã sử dụng hết " + MAX_UNDO + " lượt đi lại trong ván này!", "Hết lượt", JOptionPane.WARNING_MESSAGE);
            return;
        }

        historyStack.remove(historyStack.size() - 1); 
        historyStack.get(historyStack.size() - 1).restore(); 
        undoCount++; 
        gp.veLaiToanBo();
    }

    public void saveGame() {
        if (end == 0) {
            try {
                String fileName = isPvE ? SAVE_PVE : SAVE_PVP;
                PrintWriter pw = new PrintWriter(new File(fileName));
                pw.println(" TRANGTHAI ");
                pw.println(isPvE + " " + chonBlue + " " + chon + " " + check + " " + chuMo + " " + moCo + " " + checkOMo + " " + checkOBiMo);
                pw.println(" BIENSO ");
                pw.println(oDaChon + " " + oBatBuoc + " " + undoCount + " " + doKhoAI + " " + helpCount);
                pw.println(" BANCO ");
                for (int i = 0; i < 5; i++) {
                    for (int j = 0; j < 5; j++) {
                        pw.printf("%2d ", board[i][j][2]); 
                    }
                    pw.println(); 
                }
                pw.println(" DANHSACH ");
                saveList(pw, quanDo); saveList(pw, quanXanh); saveList(pw, listMo); saveList(pw, listBiMo);
                pw.close();
            } catch (IOException e) { e.printStackTrace(); }
        }
    }

    private void saveList(PrintWriter pw, ArrayList<Integer> list) {
        if (list.isEmpty()) pw.println("-");
        else { for (Integer val : list) pw.print(val + " "); pw.println(); }
    }

    public boolean loadGame(boolean modePvE) {
        String fileName = modePvE ? SAVE_PVE : SAVE_PVP;
        File f = new File(fileName);
        if (!f.exists()) return false;
        
        try {
            Scanner sc = new Scanner(f);
            
            while(sc.hasNextLine()) if (sc.nextLine().contains(" TRANGTHAI ")) break;
            this.isPvE = sc.nextBoolean(); this.chonBlue = sc.nextBoolean(); this.chon = sc.nextBoolean();
            this.check = sc.nextBoolean(); this.chuMo = sc.nextBoolean(); this.moCo = sc.nextBoolean();
            this.checkOMo = sc.nextBoolean(); this.checkOBiMo = sc.nextBoolean();
            
            while(sc.hasNextLine()) if (sc.nextLine().contains(" BIENSO ")) break;
            this.oDaChon = sc.nextInt(); this.oBatBuoc = sc.nextInt();
            if (sc.hasNextInt()) { this.undoCount = sc.nextInt(); } else { this.undoCount = 0; }
            if (sc.hasNextInt()) { 
                int doKhoCu = sc.nextInt(); 
                System.out.println("Ván cũ (Depth " + doKhoCu + ") -> Đổi sang độ khó mới (Depth " + this.doKhoAI + ")");
            }
            if (sc.hasNextInt()) { this.helpCount = sc.nextInt(); } else { this.helpCount = 0; }
            
            while(sc.hasNextLine()) if (sc.nextLine().contains(" BANCO ")) break;
            for (int i = 0; i < 5; i++) for (int j = 0; j < 5; j++) board[i][j][2] = sc.nextInt();
            
            while(sc.hasNextLine()) if (sc.nextLine().contains(" DANHSACH ")) break;
            
            if (sc.hasNextLine()) sc.nextLine();
            
            this.quanDo = loadList(sc.nextLine()); this.quanXanh = loadList(sc.nextLine());
            this.listMo = loadList(sc.nextLine()); this.listBiMo = loadList(sc.nextLine());
            
            sc.close();
            
            // CHẠY LỆNH ĐỒNG BỘ ĐỂ XÓA RÁC TỪ FILE SAVE CŨ
            dongBoQuanCo(); 
            
            return true;
            
        } catch (Exception e) { 
            e.printStackTrace(); 
            return false; 
        }
    }

    private ArrayList<Integer> loadList(String line) {
        ArrayList<Integer> list = new ArrayList<>();
        line = line.trim();
        if (line.equals("-") || line.isEmpty()) return list;
        String[] parts = line.split(" ");
        for (String s : parts) try { list.add(Integer.parseInt(s)); } catch (Exception e) {}
        return list;
    }

    public void deleteSaveFile(boolean cheDoPvE) {
        File f = new File(cheDoPvE ? SAVE_PVE : SAVE_PVP);
        if (f.exists()) f.delete();
    }
    
    public void deleteCurrentSaveFile() { deleteSaveFile(this.isPvE); }

    void khoiTao() {
        quanDo.clear(); quanXanh.clear();
        for (int i = 0; i < 5; i++) { quanDo.add(i); quanXanh.add(40 + i); }
        for (int i = 0; i < 5; i += 4) { quanDo.add(10 + i); quanXanh.add(30 + i); }
        quanDo.add(24); quanXanh.add(20);
        undoCount = 0; 
        helpCount = 0;
        if (isPvE) {
            historyStack.clear();
            historyStack.add(new GameStateSnapshot());
        }
    }

    public void botTurn() {
        BanCo banCoAo = new BanCo();
        for (int i = 0; i < 5; i++) {
            for (int j = 0; j < 5; j++) {
                banCoAo.maTran[i][j] = board[i][j][2];
            }
        }
        BotAI ai = new BotAI();
        NuocDi nuocDiTotNhat = ai.timNuocDiTotNhat(banCoAo, -1, doKhoAI);

        if (nuocDiTotNhat != null) {
            System.out.println("Bot quyết định đi: (" + nuocDiTotNhat.hangCu + ", " + nuocDiTotNhat.cotCu + ") -> (" + nuocDiTotNhat.hangMoi + ", " + nuocDiTotNhat.cotMoi + ")");
            thucHienNuocDiBot(nuocDiTotNhat.hangCu, nuocDiTotNhat.cotCu, nuocDiTotNhat.hangMoi, nuocDiTotNhat.cotMoi);
        } else {
            System.out.println("CẢNH BÁO: Bot đã hết nước đi hợp lệ! NGƯỜI CHƠI THẮNG!");
            end = 1; sm.addResult("XANH", isPvE);
            deleteCurrentSaveFile();
            gp.veLaiToanBo(); 
        }
    }

    public void botKill() {
        if (oBatBuoc == -1) { botTurn(); return; }
        int trap_r = oBatBuoc / 10;
        int trap_c = oBatBuoc % 10;
        ArrayList<int[]> blackPieces = new ArrayList<>();
        
        for (int i = 0; i < 5; i++) {
            for (int j = 0; j < 5; j++) {
                if (board[i][j][2] == 4) blackPieces.add(new int[]{i, j});
            }
        }

        if (!blackPieces.isEmpty()) {
            System.out.println("Bot bị Người chơi Mở cờ! Tự động rơi vào bẫy.");
            int[] chosen = blackPieces.get(0); 
            chonOSauKhiMoCo(chosen[0], chosen[1], 4); 
            new Thread(() -> {
                try { Thread.sleep(400); } catch (Exception e) {}
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
            chuMo = false; listBiMo.clear(); listMo.clear();
            chonO(r2, c2, true); 
        }
    }

    public void goiYNuocDi() {
        if (!isPvE || !chonBlue || end != 0) return;

        if (helpCount >= MAX_HELP) {
            JOptionPane.showMessageDialog(gp, "Bạn đã hết 3 lần trợ giúp trong ván này!", "Hết lượt", JOptionPane.WARNING_MESSAGE);
            return;
        }

        if (chon || moCo) {
            JOptionPane.showMessageDialog(gp, "Vui lòng bỏ chọn quân cờ hiện tại để nhận gợi ý mới!", "Gợi ý", JOptionPane.INFORMATION_MESSAGE);
            return;
        }

        System.out.println("Đang tìm gợi ý tốt nhất cho bạn...");
        BanCo banCoAo = new BanCo();
        for (int i = 0; i < 5; i++) {
            for (int j = 0; j < 5; j++) {
                banCoAo.maTran[i][j] = board[i][j][2];
            }
        }
        
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
            System.out.println("Gợi ý: Đi từ (" + ndTotNhat.hangCu + "," + ndTotNhat.cotCu + ") tới (" + ndTotNhat.hangMoi + "," + ndTotNhat.cotMoi + ")");
        } else {
            JOptionPane.showMessageDialog(gp, "Không tìm thấy nước đi an toàn nào lúc này!", "Xin lỗi", JOptionPane.INFORMATION_MESSAGE);
        }
    }

    void xuLyChuot(int x, int y) {
        if (!huongDan && !xemLichSu && new Rectangle(940, 520, 50, 50).contains(x, y)) {
            gp.soundOn = !gp.soundOn;
            if (!gp.soundOn) gp.music.stopMusic(); else gp.music.batDau();
            gp.repaint();
        } else {
            if (gp.start) {
                if (new Rectangle(890, 15, 100, 40).contains(x, y)) {
                    if (end == 0) saveGame(); else deleteCurrentSaveFile();
                    gp.resetGame(); return;
                }
                
                if (isPvE && end == 0) {
                    if (y >= 440 && y <= 510) {
                        if (x >= 680 && x <= 740) { 
                            goiYNuocDi(); 
                            return; 
                        } 
                        else if (x >= 755 && x <= 845) { 
                            undoMove(); 
                            return; 
                        }
                    }
                }
                
                if (end == 0) {
                    if (isPvE && !chonBlue) return; 
                    if (chuMo && new Rectangle(720, 265, 60, 40).contains(x, y)) {
                        if (!moCo) { moCo = true; setMoCo(); } 
                        else {
                            moCo = false;
                            int i = oDaChon / 10, j = oDaChon % 10;
                            if (chonBlue) board[i][j][2] = 1; else board[i][j][2] = -1;
                            chonO(i, j, true);
                        }
                    } else {
                        int j = (x - 85) / 125, i = (y - 35) / 125;
                        if (i < 5 && j < 5) {
                            Ellipse2D elip = new Ellipse2D.Float(board[i][j][0], board[i][j][1], 30, 30);
                            if (elip.contains(x - 50, y)) {
                                if (!moCo) chonO(i, j, true);
                                else chonOSauKhiMoCo(i, j, board[i][j][2]);
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
                if (dangChonDoKho) {
                    if (new Rectangle(40, 20, 300, 50).contains(x, y)) {
                        doKhoAI = 1; dangChonDoKho = false; kiemTraVaVaoGame(true); 
                    } else if (new Rectangle(40, 90, 300, 50).contains(x, y)) {
                        doKhoAI = 2; dangChonDoKho = false; kiemTraVaVaoGame(true); 
                    } else if (new Rectangle(40, 160, 240, 50).contains(x, y)) {
                        doKhoAI = 4; dangChonDoKho = false; kiemTraVaVaoGame(true); 
                    } else if (new Rectangle(40, 230, 240, 50).contains(x, y)) {
                        dangChonDoKho = false; gp.veMenu(); 
                    }
                }
                else if (!huongDan && !xemLichSu) { 
                    if (new Rectangle(40, 20, 300, 50).contains(x, y)) kiemTraVaVaoGame(false); 
                    else if (new Rectangle(40, 90, 300, 50).contains(x, y)) { 
                        dangChonDoKho = true; gp.veMenu(); 
                    } 
                    else if (new Rectangle(40, 160, 240, 50).contains(x, y)) { 
                        if(gp.hd[0] != null) gp.hdImg = gp.hd[0]; huongDan = true; gp.veMenu();
                    }
                    else if (new Rectangle(40, 230, 240, 50).contains(x, y)) { 
                        xemLichSu = true; trangLS = 0; cuonLichSu = 0; gp.veMenu();
                    }
                } 
                else if (huongDan) { 
                    if (new Rectangle(810, 490, 50, 50).contains(x, y)) {
                        if (trangHD < 2) trangHD++; if(gp.hd[trangHD] != null) gp.hdImg = gp.hd[trangHD]; gp.repaint();
                    } else if (new Rectangle(750, 490, 50, 50).contains(x, y)) {
                        if (trangHD > 0) trangHD--; if(gp.hd[trangHD] != null) gp.hdImg = gp.hd[trangHD]; gp.repaint();
                    } else if (new Rectangle(15, 30 , 50, 50).contains(x, y)) {
                        huongDan = false; gp.veMenu();
                    }
                }
                else if (xemLichSu) {
                    if (new Rectangle(60, 30, 50, 50).contains(x, y)) {
                        xemLichSu = false; trangLS = 0; gp.veMenu();
                    } else if (trangLS == 1 && new Rectangle(380, 485, 40, 40).contains(x, y)) {
                        trangLS = 0; gp.veMenu();
                    } else if (trangLS == 0 && new Rectangle(580, 485, 40, 40).contains(x, y)) {
                        trangLS = 1; gp.veMenu();
                    }
                }
            }
        }
    }

    private void setMoCo() {
        int i = oDaChon / 10, j = oDaChon % 10;
        for (int a = -1; a < 2; a++) {
            if (i + a < 0 || i + a > 4) continue;
            for (int b = -1; b < 2; b++) {
                if (j + b < 0 || j + b > 4 || (a == 0 && b == 0)) continue;
                if ((i + j) % 2 != 0 && a != 0 && b != 0) continue;
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
                    chon = true; oDaChon = i * 10 + j;
                    if (chonBlue) board[i][j][2] = 2; else board[i][j][2] = -2;
                    for (int a = -1; a < 2; a++) {
                        if (!ktra(i + a)) continue;
                        for (int b = -1; b < 2; b++) {
                            if (!ktra(j + b) || (a == 0 && b == 0)) continue;
                            if ((i + j) % 2 != 0 && a != 0 && b != 0) continue;
                            if (board[i + a][j + b][2] == 0) {
                                board[i + a][j + b][2] = 3; mo(i + a, j + b); 
                            }
                        }
                    }
                } else { 
                    int oCu_i = oDaChon / 10; int oCu_j = oDaChon % 10;
                    for (int a = -1; a < 2; a++) {
                        if (!ktra(oCu_i + a)) continue;
                        for (int b = -1; b < 2; b++) {
                            if (!ktra(oCu_j + b)) continue;
                            if (board[oCu_i + a][oCu_j + b][2] == 3) board[oCu_i + a][oCu_j + b][2] = 0; 
                            if (chonBlue) board[oCu_i][oCu_j][2] = 1; else board[oCu_i][oCu_j][2] = -1;
                        }
                    }
                    chon = false;
                    if (chuMo) { listBiMo.clear(); listMo.clear(); chuMo = false; }
                    chonO(i, j, true); return;
                }
            }
        }
        if (o == 3) {
            int oCu_i = oDaChon / 10; int oCu_j = oDaChon % 10; check = false;
            for (int a = -1; a < 2; a++) {
                if (!ktra(oCu_i + a)) continue;
                for (int b = -1; b < 2; b++) {
                    if (!ktra(oCu_j + b)) continue;
                    if (board[oCu_i + a][oCu_j + b][2] == 3 || (a == 0 && b == 0)) board[oCu_i + a][oCu_j + b][2] = 0; 
                }
            }
            if (chonBlue) {
                board[i][j][2] = 1; quanXanh.remove(Integer.valueOf(oDaChon)); quanXanh.add(i * 10 + j);
            } else {
                board[i][j][2] = -1; quanDo.remove(Integer.valueOf(oDaChon)); quanDo.add(i * 10 + j);
            }
            
            ktraGanh(i, j, board[i][j][2]); 
            dongBoQuanCo(); // Đồng bộ sau khi Gánh
            
            vayQuan(board[i][j][2]);
            dongBoQuanCo(); // Đồng bộ sau khi Vây
            
            chon = false; chonBlue = !chonBlue;
            
            kiemTraKetThuc();

            if (!moCo && veLai) chuMo = false; 

            if (isPvE && end == 0) {
                if (chonBlue && chuMo) { 
                    new Thread(() -> {
                        try { Thread.sleep(300); } catch (Exception e) {}
                        SwingUtilities.invokeLater(() -> {
                            moCo = true; setMoCo();
                            if (end == 0 && chonBlue) historyStack.add(new GameStateSnapshot());
                        });
                    }).start();
                }
                else if (!chonBlue && chuMo) { 
                    new Thread(() -> {
                        try { Thread.sleep(300); } catch (Exception e) {}
                        SwingUtilities.invokeLater(() -> {
                            moCo = true; setMoCo();
                        });
                        try { Thread.sleep(500); } catch (Exception e) {}
                        SwingUtilities.invokeLater(() -> {
                            botKill();
                            if (end == 0 && chonBlue) historyStack.add(new GameStateSnapshot());
                        });
                    }).start();
                }
                else if (!chonBlue && !chuMo) { 
                    new Thread(() -> {
                        try { Thread.sleep(500); } catch (Exception e) {}
                        SwingUtilities.invokeLater(() -> {
                            botTurn(); 
                            if (end == 0 && chonBlue) historyStack.add(new GameStateSnapshot());
                        });
                    }).start();
                }
            } else if (!isPvE && end == 0) {
                if (!chuMo) 
                    { SwingUtilities.invokeLater(() -> { historyStack.add(new GameStateSnapshot()); }); }
            }
        }
        if (veLai) gp.veLaiToanBo();
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
                    while (listBiMo.contains(Integer.valueOf(a))) listBiMo.remove(listBiMo.indexOf(Integer.valueOf(a)));
                    chuMo = false; 
                }
            } else { 
                int oCu_i = oDaChon / 10; int oCu_j = oDaChon % 10; check = false;
                for (int a = -1; a < 2; a++) {
                    if (!ktra(i + a)) continue;
                    for (int b = -1; b < 2; b++) {
                        if (!ktra(j + b)) continue;
                        if (board[i + a][j + b][2] == 4 || (a == 0 && b == 0))
                            if (chonBlue) board[i + a][j + b][2] = 1; else board[i + a][j + b][2] = -1;
                    }
                }
                board[oCu_i][oCu_j][2] = 0;
                
                if (chonBlue) {
                    board[i][j][2] = 1; quanXanh.remove(Integer.valueOf(oDaChon)); quanXanh.add(i * 10 + j);
                } else {
                    board[i][j][2] = -1; quanDo.remove(Integer.valueOf(oDaChon)); quanDo.add(i * 10 + j);
                }
                
                ktraGanh(i, j, board[i][j][2]); 
                dongBoQuanCo(); // Đồng bộ
                
                vayQuan(board[i][j][2]);
                dongBoQuanCo(); // Đồng bộ
                
                chon = false; chonBlue = !chonBlue;
                
                kiemTraKetThuc();
                
                if (isPvE && end == 0) {
                    if (chonBlue && chuMo) {
                        new Thread(() -> {
                            try { Thread.sleep(300); } catch (Exception e) {}
                            SwingUtilities.invokeLater(() -> {
                                moCo = true; setMoCo(); 
                                if (end == 0 && chonBlue) historyStack.add(new GameStateSnapshot());
                            });
                        }).start();
                    }
                    else if (!chonBlue && chuMo) {
                        new Thread(() -> {
                            try { Thread.sleep(300); } catch (Exception e) {}
                            SwingUtilities.invokeLater(() -> {
                                moCo = true; setMoCo(); 
                            });
                            try { Thread.sleep(500); } catch (Exception e) {}
                            SwingUtilities.invokeLater(() -> {
                                botKill();
                                if (end == 0 && chonBlue) historyStack.add(new GameStateSnapshot());
                            });
                        }).start();
                    }
                    else if (!chonBlue && !chuMo) {
                         new Thread(() -> {
                            try { Thread.sleep(500); } catch (Exception e) {}
                            SwingUtilities.invokeLater(() -> {
                                botTurn();
                                if (end == 0 && chonBlue) historyStack.add(new GameStateSnapshot());
                            });
                        }).start();
                    }
                } else if (!isPvE && end == 0) {
                    if (!chuMo) { SwingUtilities.invokeLater(() -> { historyStack.add(new GameStateSnapshot()); }); }
                }
                moCo = false;
            }
        } else if (o == 4) { 
            if (!chon) { 
                chon = true; oDaChon = i * 10 + j;
                if (chonBlue) board[i][j][2] = 2; else board[i][j][2] = -2;
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
        board[i1][j1][2] = o; board[i2][j2][2] = o;
    }

    void ktraGanh(int i, int j, int o) {
        if (ktra(j + 1) && ktra(j - 1)) { 
            if (board[i][j + 1][2] == -o && board[i][j - 1][2] == -o) {
                if (checkOMo) { chuMo = true; check = true; } 
                else if (checkOBiMo) { 
                    if (!listBiMo.isEmpty() && (listBiMo.get(listBiMo.size() - 1) == i * 10 + j + 1 || listBiMo.get(listBiMo.size() - 1) == i * 10 + j - 1)) { 
                        checkOBiMo = false; return; 
                    } 
                } 
                else { anQuan(i, j + 1, o, i, j - 1); }
            }
            if ((i + j) % 2 == 0 && ktra(i + 1) && ktra(i - 1)) 
                for (int b = -1; b < 2; b += 2)
                    if (board[i + 1][j + b][2] == -o && board[i - 1][j - b][2] == -o) {
                        if (checkOMo) { chuMo = true; check = true; } 
                        else if (checkOBiMo) { 
                            if (!listBiMo.isEmpty() && (listBiMo.get(listBiMo.size() - 1) == (i + 1) * 10 + j + b || listBiMo.get(listBiMo.size() - 1) == (i - 1) * 10 + j - b)) { 
                                checkOBiMo = false; return; 
                            } 
                        } 
                        else { anQuan(i + 1, j + b, o, i - 1, j - b); }
                    }
        }
        if (ktra(i + 1) && ktra(i - 1)) {
            if (board[i + 1][j][2] == -o && board[i - 1][j][2] == -o) {
                if (checkOMo) { chuMo = true; check = true; } 
                else if (checkOBiMo) { 
                    if (!listBiMo.isEmpty() && (listBiMo.get(listBiMo.size() - 1) == (i + 1) * 10 + j || listBiMo.get(listBiMo.size() - 1) == (i - 1) * 10 + j)) { 
                        checkOBiMo = false; return; 
                    } 
                } 
                else { anQuan(i + 1, j, o, i - 1, j); }
            }
        }
    }

    @SuppressWarnings("unchecked")
    void vayQuan(int o) {
        ArrayList<Integer> vay = new ArrayList<Integer>();
        if (o > 0) vay = (ArrayList<Integer>) quanDo.clone(); else vay = (ArrayList<Integer>) quanXanh.clone();
        
        loop: while (vay.size() > 0) {
            ArrayList<Integer> oKe = new ArrayList<Integer>();
            oKe.add(vay.get(0));
            int t = 0;
            while (t != oKe.size()) {
                int s = oKe.get(t), i = s / 10, j = s % 10;
                for (int a = -1; a < 2; a++) {
                    if (i + a < 0 || i + a > 4) continue;
                    for (int b = -1; b < 2; b++) {
                        if (j + b < 0 || j + b > 4 || (a == 0 && b == 0)) continue;
                        if ((i + j) % 2 != 0 && a != 0 && b != 0) continue;
                        if (board[i + a][j + b][2] == -o) {
                            if (!oKe.contains(Integer.valueOf((i + a) * 10 + j + b))) oKe.add((i + a) * 10 + j + b);
                        }
                        if (board[i + a][j + b][2] == 0) {
                            for (int h = 0; h < oKe.size(); h++) vay.remove(oKe.get(h));
                            oKe.clear(); continue loop;
                        }
                    }
                }
                t++;
                if (t == oKe.size()) {
                    for (int z = 0; z < t; z++) {
                        board[oKe.get(z) / 10][oKe.get(z) % 10][2] = o;
                        vay.remove(oKe.get(z));
                    }
                }
            }
        }
    }

    private void mo(int oDiDen_i, int oDiDen_j) {
        int o; check = false;
        if (chonBlue) o = 1; else o = -1;
        int i = oDaChon / 10, j = oDaChon % 10;
        
        for (int a = -1; a < 2; a++) {
            if (!ktra(i + a)) continue;
            for (int b = -1; b < 2; b++) {
                if (!ktra(j + b) || (a == 0 && b == 0)) continue;
                if ((i + j) % 2 != 0 && a != 0 && b != 0) continue;
                
                int oBiMo_i = i + a, oBiMo_j = j + b;
                if (board[oBiMo_i][oBiMo_j][2] == -o) { 
                    listBiMo.add(oBiMo_i * 10 + oBiMo_j);
                    checkOBiMo = true;
                    ktraGanh(oDiDen_i, oDiDen_j, o);
                    if (!checkOBiMo) { listBiMo.remove(listBiMo.size() - 1); continue; } 
                    else check = true;
                    checkOBiMo = false;
                }
                if (check && listBiMo.size() > 0) {
                    check = false;
                    if (ktra(2 * i - oDiDen_i) && ktra(2 * j - oDiDen_j) && board[2 * i - oDiDen_i][2 * j - oDiDen_j][2] == o) {
                        chuMo = true; check = true;
                    } else { 
                        checkOMo = true; ktraGanh(i, j, -o); checkOMo = false;
                    }
                    if (!listMo.contains(Integer.valueOf(oDiDen_i * 10 + oDiDen_j)) && check) listMo.add(oDiDen_i * 10 + oDiDen_j);
                    else if (!check) listBiMo.remove(listBiMo.size() - 1);
                    
                    if (!chuMo) { listBiMo.clear(); listMo.clear(); }
                    check = false;
                }
            }
        }
    }

    private boolean ktra(int a) { return a >= 0 && a < 5; }

    private void kiemTraKetThuc() {
        if (quanDo.size() == 0) { 
            end = 1; sm.addResult("XANH", isPvE); deleteCurrentSaveFile(); 
        } 
        else if (quanXanh.size() == 0) { 
            end = -1; sm.addResult("DO", isPvE); deleteCurrentSaveFile(); 
        } 
        else {
            BanCo bc = new BanCo();
            for(int i = 0; i < 5; i++) {
                for(int j = 0; j < 5; j++) {
                    bc.maTran[i][j] = board[i][j][2];
                }
            }
            if (chuMo && oBatBuoc != -1) {
                bc.hangBatBuoc = oBatBuoc / 10;
                bc.cotBatBuoc = oBatBuoc % 10;
            }
            
            int pheHienTai = chonBlue ? 1 : -1;
            if (bc.layCacNuocDiHopLe(pheHienTai).isEmpty()) {
                if (chonBlue) {
                    System.out.println("Xanh bị vây chặt hết đường đi! ĐỎ THẮNG!");
                    end = -1; sm.addResult("DO", isPvE); deleteCurrentSaveFile();
                } else {
                    System.out.println("Đỏ bị vây chặt hết đường đi! XANH THẮNG!");
                    end = 1; sm.addResult("XANH", isPvE); deleteCurrentSaveFile();
                }
            }
        }
    }
}