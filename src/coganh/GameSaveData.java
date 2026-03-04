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
    public int oDaChon, oBatBuoc, undoCount; 
    
    // Các danh sách quân cờ và nước đi
    public ArrayList<Integer> quanDo;
    public ArrayList<Integer> quanXanh;
    public ArrayList<Integer> listMo;
    public ArrayList<Integer> listBiMo;

    // Constructor nhận tất cả dữ liệu cần thiết để tái tạo lại ván cờ
    public GameSaveData(int[][][] board, boolean chonBlue, boolean chon, boolean check, boolean chuMo, 
                        boolean moCo, boolean checkOMo, boolean checkOBiMo, boolean isPvE,
                        int oDaChon, int oBatBuoc, int undoCount,
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
        this.undoCount = undoCount; // Khởi tạo giá trị undo
        this.quanDo = quanDo;
        this.quanXanh = quanXanh;
        this.listMo = listMo;
        this.listBiMo = listBiMo;
    }
}