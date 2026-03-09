package coganh;

import java.util.ArrayList;

public class BotAI {

    // Đã xóa DO_SAU_TOI_DA fix cứng để nhận tham số linh hoạt từ menu

    public NuocDi timNuocDiTotNhat(BanCo banCoHienTai, int pheCuaBot, int doKho) {
        ArrayList<NuocDi> cacNuocDi = banCoHienTai.layCacNuocDiHopLe(pheCuaBot);

        if (cacNuocDi.isEmpty()) {
            return null;
        }

        int diemTotNhat = Integer.MIN_VALUE;
        NuocDi nuocDiChon = cacNuocDi.get(0);

        System.out.println("Bot đang suy nghĩ... (Depth " + doKho + " + Alpha-Beta Pruning)");

        for (NuocDi nd : cacNuocDi) {
            BanCo banCoNhap = banCoHienTai.copyNhap();
            banCoNhap.diChuyenAo(nd, pheCuaBot);

            // Gọi thuật toán Minimax tối ưu có Alpha-Beta
            int diemThucTe = minimax(banCoNhap, doKho - 1, Integer.MIN_VALUE, Integer.MAX_VALUE, false);
            nd.diemSo = diemThucTe;

            if (diemThucTe > diemTotNhat) {
                diemTotNhat = diemThucTe;
                nuocDiChon = nd;
            }
        }

        return nuocDiChon;
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