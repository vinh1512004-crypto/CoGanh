package coganh;

import java.util.ArrayList;
import java.util.HashSet;

public class BotAI {

    /**
     * Tạo một "hash key" đơn giản đại diện cho trạng thái bàn cờ hiện tại.
     */
    private String taoKhoa(BanCo banCo) {
        StringBuilder sb = new StringBuilder(25);
        for (int i = 0; i < 5; i++) {
            for (int j = 0; j < 5; j++) {
                int v = banCo.maTran[i][j];
                sb.append(v == 0 ? '0' : (v == 1 ? 'B' : 'R'));
            }
        }
        return sb.toString();
    }

    /**
     * Tìm nước đi tốt nhất cho bot.
     * @param banCoHienTai     Trạng thái bàn cờ hiện tại
     * @param pheCuaBot        Phe của bot (-1 = Đỏ)
     * @param doKho            Độ sâu tìm kiếm
     * @param lichSuHashThucTe Danh sách hash các trạng thái đã xuất hiện
     *                         trong ván thực tế (để tránh chọn nước lặp lại)
     */
    public NuocDi timNuocDiTotNhat(BanCo banCoHienTai, int pheCuaBot, int doKho,
                                    ArrayList<String> lichSuHashThucTe) {
        ArrayList<NuocDi> cacNuocDi = banCoHienTai.layCacNuocDiHopLe(pheCuaBot);

        if (cacNuocDi.isEmpty()) {
            return null;
        }

        int diemTotNhat = Integer.MIN_VALUE;
        NuocDi nuocDiChon = cacNuocDi.get(0);

        System.out.println("Bot đang suy nghĩ... (Depth " + doKho + " + Alpha-Beta Pruning)");

        // Set lịch sử thực tế để tránh chọn nước quay lại trạng thái đã qua
        HashSet<String> lichSuSet = new HashSet<>(lichSuHashThucTe);

        for (NuocDi nd : cacNuocDi) {
            BanCo banCoNhap = banCoHienTai.copyNhap();
            banCoNhap.diChuyenAo(nd, pheCuaBot);

            String khoaSauDi = taoKhoa(banCoNhap);

            // Tính điểm minimax thuần túy — không ảnh hưởng đến logic suy nghĩ
            int diemThucTe = minimax(banCoNhap, doKho - 1, Integer.MIN_VALUE, Integer.MAX_VALUE, false);

            // Chỉ phạt ở tầng GỐC (lượt bot thực sự đi), không ảnh hưởng minimax bên trong
            // → Bot vẫn suy nghĩ thông minh, chỉ tránh chọn nước quay lại trạng thái thực tế
            if (lichSuSet.contains(khoaSauDi)) {
                diemThucTe -= 500000;
            }

            nd.diemSo = diemThucTe;

            if (diemThucTe > diemTotNhat) {
                diemTotNhat = diemThucTe;
                nuocDiChon = nd;
            }
        }

        return nuocDiChon;
    }

    /**
     * Overload để tương thích với các lời gọi không truyền lichSuHash
     * (ví dụ: từ goiYNuocDi của người chơi).
     */
    public NuocDi timNuocDiTotNhat(BanCo banCoHienTai, int pheCuaBot, int doKho) {
        return timNuocDiTotNhat(banCoHienTai, pheCuaBot, doKho, new ArrayList<>());
    }

    private int minimax(BanCo banCo, int doSau, int alpha, int beta, boolean laLuotCuaBot) {
        if (doSau == 0) {
            return banCo.chamDiem();
        }

        int pheHienTai = laLuotCuaBot ? -1 : 1;
        ArrayList<NuocDi> cacNuocDi = banCo.layCacNuocDiHopLe(pheHienTai);

        // NHẬN DIỆN CHIẾU BÍ (VÂY CHẾT)
        if (cacNuocDi.isEmpty()) {
            // CỘNG THÊM doSau ĐỂ ƯU TIÊN KẾT THÚC NHANH
            return banCo.chamDiem() + (laLuotCuaBot ? (-1000000 - doSau) : (1000000 + doSau));
        }

        int bestEval = laLuotCuaBot ? Integer.MIN_VALUE : Integer.MAX_VALUE;

        for (NuocDi nd : cacNuocDi) {
            BanCo banCoMoi = banCo.copyNhap();
            banCoMoi.diChuyenAo(nd, pheHienTai);

            int eval = minimax(banCoMoi, doSau - 1, alpha, beta, !laLuotCuaBot);

            if (laLuotCuaBot) {
                bestEval = Math.max(bestEval, eval);
                alpha = Math.max(alpha, eval);
            } else {
                bestEval = Math.min(bestEval, eval);
                beta = Math.min(beta, eval);
            }

            if (beta <= alpha)
                break; // Alpha-Beta Pruning
        }

        return bestEval;
    }
}