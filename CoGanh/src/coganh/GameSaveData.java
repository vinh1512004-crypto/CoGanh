package coganh;

import java.io.Serializable;
import java.util.ArrayList;

// Class này dùng để lưu trữ trạng thái game bằng Object (Chuẩn Java)
public class GameSaveData implements Serializable {
    private static final long serialVersionUID = 1L;

    // Các biến lưu trữ bàn cờ và trạng thái logic
    public int[][][] board;
    public boolean chonBlue, chon, check, chuMo, moCo, checkOMo, checkOBiMo, isPvE;

    // Đã bổ sung thêm undoCount vào đây
    public int oDaChon, oBatBuoc, undoCount, doKhoAI, helpCount;

    // Các danh sách quân cờ và nước đi
    public ArrayList<Integer> quanDo;
    public ArrayList<Integer> quanXanh;
    public ArrayList<Integer> listMo;
    public ArrayList<Integer> listBiMo;

    // Constructor nhận tất cả dữ liệu cần thiết để tái tạo lại ván cờ
    public GameSaveData(int[][][] board, boolean chonBlue, boolean chon, boolean check, boolean chuMo,
            boolean moCo, boolean checkOMo, boolean checkOBiMo, boolean isPvE,
            int oDaChon, int oBatBuoc, int undoCount, int doKhoAI, int helpCount,
            ArrayList<Integer> quanDo, ArrayList<Integer> quanXanh,
            ArrayList<Integer> listMo, ArrayList<Integer> listBiMo) {
        this.board = board;
        this.chonBlue = chonBlue;
        this.chon = chon;
        this.check = check;
        this.chuMo = chuMo;
        this.moCo = moCo;
        this.checkOMo = checkOMo;
        this.checkOBiMo = checkOBiMo;
        this.isPvE = isPvE;
        this.oDaChon = oDaChon;
        this.oBatBuoc = oBatBuoc;
        this.undoCount = undoCount;
        this.doKhoAI = doKhoAI;
        this.helpCount = helpCount;
        this.quanDo = quanDo;
        this.quanXanh = quanXanh;
        this.listMo = listMo;
        this.listBiMo = listBiMo;
    }

    public static void saveToFile(GameController gc, String fileName) {
        try {
            java.io.PrintWriter pw = new java.io.PrintWriter(new java.io.File(fileName));
            pw.println(" TRANGTHAI ");
            pw.println(gc.isPvE + " " + gc.chonBlue + " " + gc.chon + " " + gc.check + " " + gc.chuMo + " " + gc.moCo
                    + " " + gc.checkOMo
                    + " " + gc.checkOBiMo);
            pw.println(" NAMES ");
            pw.println(gc.playerNameBlue);
            pw.println(gc.playerNameRed);
            pw.println(" AVATARS ");
            pw.println(gc.avatarBlueIndex + " " + gc.avatarRedIndex);
            pw.println(" BIENSO ");
            pw.println(gc.oDaChon + " " + gc.oBatBuoc + " " + gc.undoCount + " " + gc.doKhoAI + " " + gc.helpCount + " "
                    + gc.timeLeftBlue + " " + gc.timeLeftRed);
            pw.println(" BANCO ");
            for (int i = 0; i < 5; i++) {
                for (int j = 0; j < 5; j++) {
                    pw.printf("%2d ", gc.board[i][j][2]);
                }
                pw.println();
            }
            pw.println(" DANHSACH ");
            saveList(pw, gc.quanDo);
            saveList(pw, gc.quanXanh);
            saveList(pw, gc.listMo);
            saveList(pw, gc.listBiMo);
            pw.close();
        } catch (java.io.IOException e) {
            e.printStackTrace();
        }
    }

    private static void saveList(java.io.PrintWriter pw, ArrayList<Integer> list) {
        if (list.isEmpty())
            pw.println("-");
        else {
            for (Integer val : list)
                pw.print(val + " ");
            pw.println();
        }
    }

    public static boolean loadFromFile(GameController gc, String fileName) {
        java.io.File f = new java.io.File(fileName);
        if (!f.exists())
            return false;

        try {
            java.util.Scanner sc = new java.util.Scanner(f);

            while (sc.hasNextLine())
                if (sc.nextLine().contains(" TRANGTHAI "))
                    break;
            gc.isPvE = sc.nextBoolean();
            gc.chonBlue = sc.nextBoolean();
            gc.chon = sc.nextBoolean();
            gc.check = sc.nextBoolean();
            gc.chuMo = sc.nextBoolean();
            gc.moCo = sc.nextBoolean();
            gc.checkOMo = sc.nextBoolean();
            gc.checkOBiMo = sc.nextBoolean();
            sc.nextLine(); // Consume newline còn lại sau dòng boolean (tránh đọc sai tên player)

            while (sc.hasNextLine()) {
                String line = sc.nextLine();
                if (line.contains(" NAMES ")) {
                    if (sc.hasNextLine()) gc.playerNameBlue = sc.nextLine().trim();
                    if (sc.hasNextLine()) gc.playerNameRed = sc.nextLine().trim();
                }
                if (line.contains(" AVATARS ")) {
                    if (sc.hasNextInt()) gc.avatarBlueIndex = sc.nextInt();
                    if (sc.hasNextInt()) gc.avatarRedIndex = sc.nextInt();
                    if (gc.avatarBlueIndex > 5) gc.avatarBlueIndex = 0;
                    if (gc.avatarRedIndex > 5) gc.avatarRedIndex = 1;
                    sc.nextLine(); // consume newline
                }
                if (line.contains(" BIENSO ")) break;
            }
            gc.oDaChon = sc.nextInt();
            gc.oBatBuoc = sc.nextInt();
            if (sc.hasNextInt()) {
                gc.undoCount = sc.nextInt();
            } else {
                gc.undoCount = 0;
            }
            if (sc.hasNextInt()) {
                gc.doKhoAI = sc.nextInt(); // Load lại độ khó đã lưu
                System.out.println("Loaded doKhoAI: " + gc.doKhoAI);
            }
            if (sc.hasNextInt()) {
                gc.helpCount = sc.nextInt();
            } else {
                gc.helpCount = 0;
            }
            if (sc.hasNextInt()) {
                gc.timeLeftBlue = sc.nextInt();
                gc.timeLeftRed = sc.nextInt();
                System.out.println("DEBUG: loaded timeLeftBlue=" + gc.timeLeftBlue + " timeLeftRed=" + gc.timeLeftRed);
            } else {
                gc.timeLeftBlue = 600;
                gc.timeLeftRed = 600;
                System.out.println("DEBUG: Failed to load timer, defaulted to 600");
            }

            while (sc.hasNextLine())
                if (sc.nextLine().contains(" BANCO "))
                    break;
            for (int i = 0; i < 5; i++)
                for (int j = 0; j < 5; j++)
                    gc.board[i][j][2] = sc.nextInt();

            while (sc.hasNextLine())
                if (sc.nextLine().contains(" DANHSACH "))
                    break;

            gc.quanDo = loadList(sc.nextLine());
            gc.quanXanh = loadList(sc.nextLine());
            gc.listMo = loadList(sc.nextLine());
            gc.listBiMo = loadList(sc.nextLine());

            sc.close();

            // Làm sạch các trạng thái tạm thời bị lưu (đang chọn, ô đích, quân bị vây...)
            // Quân trạng thái 4 là quân bị Mở cờ, thuộc về phe ĐANG chờ bị bắt
            // Phe đó là đối phương của phe đang có lượt (chonBlue):
            // - Nếu chonBlue=true (lượt Xanh) → Xanh Mở cờ → quân bị vây là Đỏ (-1)
            // - Nếu chonBlue=false (lượt Đỏ) → Đỏ Mở cờ → quân bị vây là Xanh (1)
            int pheBiVay = gc.chonBlue ? -1 : 1;
            for (int i = 0; i < 5; i++) {
                for (int j = 0; j < 5; j++) {
                    int val = gc.board[i][j][2];
                    if (val == 2)  gc.board[i][j][2] = 1;      // xanh đang chọn -> xanh
                    if (val == -2) gc.board[i][j][2] = -1;     // đỏ đang chọn  -> đỏ
                    if (val == 3)  gc.board[i][j][2] = 0;      // ô đích        -> trống
                    if (val == 4)  gc.board[i][j][2] = pheBiVay; // quân bị vây -> đúng phe
                }
            }
            // Reset trạng thái đang hành động
            gc.chon = false;
            gc.moCo = false;
            gc.chuMo = false;
            gc.check = false;
            gc.checkOMo = false;
            gc.checkOBiMo = false;
            gc.oBatBuoc = -1;
            gc.listMo.clear();
            gc.listBiMo.clear();

            gc.dongBoQuanCo();

            return true;

        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }

    private static ArrayList<Integer> loadList(String line) {
        ArrayList<Integer> list = new ArrayList<>();
        line = line.trim();
        if (line.equals("-") || line.isEmpty())
            return list;
        String[] parts = line.split(" ");
        for (String s : parts)
            try {
                list.add(Integer.parseInt(s));
            } catch (Exception e) {
            }
        return list;
    }
}