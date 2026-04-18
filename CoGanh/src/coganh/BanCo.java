package coganh;

import java.util.ArrayList;

public class BanCo {
    // 1: Xanh (Người), -1: Đỏ (Bot), 0: Trống
    public int[][] maTran = new int[5][5];

    // Lưu tọa độ ô bắt buộc phải đi vào (Khi bị đối phương Mở cờ)
    public int hangBatBuoc = -1;
    public int cotBatBuoc = -1;

    public BanCo() {
    }

    public BanCo copyNhap() {
        BanCo banCoMoi = new BanCo();
        for (int i = 0; i < 5; i++) {
            banCoMoi.maTran[i] = this.maTran[i].clone();
        }
        // Chép cả trạng thái Mở cờ
        banCoMoi.hangBatBuoc = this.hangBatBuoc;
        banCoMoi.cotBatBuoc = this.cotBatBuoc;
        return banCoMoi;
    }

    public boolean toaDoHopLe(int x) {
        return x >= 0 && x < 5;
    }

    public ArrayList<NuocDi> layCacNuocDiHopLe(int pheChoi) {
        ArrayList<NuocDi> danhSachNuocDi = new ArrayList<>();

        if (hangBatBuoc != -1 && cotBatBuoc != -1) {
            for (int a = -1; a <= 1; a++) {
                for (int b = -1; b <= 1; b++) {
                    if (a == 0 && b == 0)
                        continue;
                    int r = hangBatBuoc + a;
                    int c = cotBatBuoc + b;
                    if (toaDoHopLe(r) && toaDoHopLe(c)) {
                        if ((r + c) % 2 != 0 && a != 0 && b != 0)
                            continue;
                        if (maTran[r][c] == pheChoi) {
                            danhSachNuocDi.add(new NuocDi(r, c, hangBatBuoc, cotBatBuoc));
                        }
                    }
                }
            }
            if (!danhSachNuocDi.isEmpty()) {
                return danhSachNuocDi;
            } else {
                hangBatBuoc = -1;
                cotBatBuoc = -1;
            }
        }

        for (int i = 0; i < 5; i++) {
            for (int j = 0; j < 5; j++) {
                if (maTran[i][j] == pheChoi) {
                    for (int a = -1; a <= 1; a++) {
                        for (int b = -1; b <= 1; b++) {
                            if (a == 0 && b == 0)
                                continue;
                            int ni = i + a, nj = j + b;

                            if (toaDoHopLe(ni) && toaDoHopLe(nj)) {
                                if ((i + j) % 2 != 0 && a != 0 && b != 0)
                                    continue;
                                if (maTran[ni][nj] == 0) {
                                    danhSachNuocDi.add(new NuocDi(i, j, ni, nj));
                                }
                            }
                        }
                    }
                }
            }
        }
        return danhSachNuocDi;
    }

    public void diChuyenAo(NuocDi nd, int phe) {
        maTran[nd.hangMoi][nd.cotMoi] = phe;
        maTran[nd.hangCu][nd.cotCu] = 0;

        hangBatBuoc = -1;
        cotBatBuoc = -1;

        xulyGanh(nd.hangMoi, nd.cotMoi, phe);
        xulyVay(phe);
        kiemTraThietLapMo(nd.hangCu, nd.cotCu, phe);
    }

    public void kiemTraThietLapMo(int r, int c, int phe) {
        boolean taoTheMo = false;

        if (toaDoHopLe(c - 1) && toaDoHopLe(c + 1) && maTran[r][c - 1] == phe && maTran[r][c + 1] == phe)
            taoTheMo = true;
        if (toaDoHopLe(r - 1) && toaDoHopLe(r + 1) && maTran[r - 1][c] == phe && maTran[r + 1][c] == phe)
            taoTheMo = true;
        if ((r + c) % 2 == 0) {
            if (toaDoHopLe(r - 1) && toaDoHopLe(c - 1) && toaDoHopLe(r + 1) && toaDoHopLe(c + 1)
                    && maTran[r - 1][c - 1] == phe && maTran[r + 1][c + 1] == phe)
                taoTheMo = true;
            if (toaDoHopLe(r - 1) && toaDoHopLe(c + 1) && toaDoHopLe(r + 1) && toaDoHopLe(c - 1)
                    && maTran[r - 1][c + 1] == phe && maTran[r + 1][c - 1] == phe)
                taoTheMo = true;
        }

        if (taoTheMo) {
            int doiPhuong = -phe;
            for (int a = -1; a <= 1; a++) {
                for (int b = -1; b <= 1; b++) {
                    if (a == 0 && b == 0)
                        continue;
                    int nr = r + a, nc = c + b;
                    if (toaDoHopLe(nr) && toaDoHopLe(nc)) {
                        if ((nr + nc) % 2 != 0 && a != 0 && b != 0)
                            continue;
                        if (maTran[nr][nc] == doiPhuong) {
                            hangBatBuoc = r;
                            cotBatBuoc = c;
                            return;
                        }
                    }
                }
            }
        }
    }

    private void xetGanh(int r1, int c1, int r2, int c2, int phe, int doiPhuong) {
        if (toaDoHopLe(r1) && toaDoHopLe(c1) && toaDoHopLe(r2) && toaDoHopLe(c2)) {
            if (maTran[r1][c1] == doiPhuong && maTran[r2][c2] == doiPhuong) {
                maTran[r1][c1] = phe;
                maTran[r2][c2] = phe;
            }
        }
    }

    public void xulyGanh(int r, int c, int phe) {
        int doiPhuong = -phe;
        xetGanh(r, c - 1, r, c + 1, phe, doiPhuong);
        xetGanh(r - 1, c, r + 1, c, phe, doiPhuong);
        if ((r + c) % 2 == 0) {
            xetGanh(r - 1, c - 1, r + 1, c + 1, phe, doiPhuong);
            xetGanh(r - 1, c + 1, r + 1, c - 1, phe, doiPhuong);
        }
    }

    public void xulyVay(int phe) {
        int doiPhuong = -phe;
        boolean[][] daDuyet = new boolean[5][5];

        for (int i = 0; i < 5; i++) {
            for (int j = 0; j < 5; j++) {
                if (maTran[i][j] == doiPhuong && !daDuyet[i][j]) {
                    ArrayList<int[]> khoiQuanDich = new ArrayList<>();
                    boolean coDuongTho = checkDuongTho(i, j, doiPhuong, daDuyet, khoiQuanDich);

                    if (!coDuongTho) {
                        for (int[] toado : khoiQuanDich) {
                            maTran[toado[0]][toado[1]] = phe;
                        }
                    }
                }
            }
        }
    }

    private boolean checkDuongTho(int r, int c, int phe, boolean[][] daDuyet, ArrayList<int[]> khoi) {
        daDuyet[r][c] = true;
        khoi.add(new int[] { r, c });
        boolean coTho = false;

        for (int a = -1; a <= 1; a++) {
            for (int b = -1; b <= 1; b++) {
                if (a == 0 && b == 0)
                    continue;
                int nr = r + a, nc = c + b;

                if (toaDoHopLe(nr) && toaDoHopLe(nc)) {
                    if ((r + c) % 2 != 0 && a != 0 && b != 0)
                        continue;

                    if (maTran[nr][nc] == 0) {
                        coTho = true;
                    } else if (maTran[nr][nc] == phe && !daDuyet[nr][nc]) {
                        if (checkDuongTho(nr, nc, phe, daDuyet, khoi))
                            coTho = true;
                    }
                }
            }
        }
        return coTho;
    }

    // ==========================================
    // BỘ ĐỊNH GIÁ AI MỚI: TẬP TRUNG DIỆT ĐÍCH
    // ==========================================
    public int chamDiem() {
        int soQuanDo = 0;
        int soQuanXanh = 0;
        int soNuocDiXanh = 0;
        int diemViTriXanh = 0;

        // Bảng điểm vị trí: Khu vực giữa bàn cờ có điểm cao, góc có điểm thấp.
        // Bot (-1) muốn Xanh (1) bị dồn vào góc -> Điểm vị trí Xanh CÀNG THẤP CÀNG TỐT
        // cho Bot.
        int[][] viTri = {
                { 0, 1, 2, 1, 0 },
                { 1, 3, 4, 3, 1 },
                { 2, 4, 6, 4, 2 },
                { 1, 3, 4, 3, 1 },
                { 0, 1, 2, 1, 0 }
        };

        for (int i = 0; i < 5; i++) {
            for (int j = 0; j < 5; j++) {
                if (maTran[i][j] == -1) {
                    soQuanDo++;
                } else if (maTran[i][j] == 1) {
                    soQuanXanh++;
                    soNuocDiXanh += demSoNuocDiTaiO(i, j);
                    diemViTriXanh += viTri[i][j];
                }
            }
        }

        // 1. Chênh lệch quân số (Tuyệt đối quan trọng)
        int diem = (soQuanDo - soQuanXanh) * 10000;

        // 2. Ép đối phương hết đường đi (Càng ít đường đi, Bot càng lợi)
        diem -= soNuocDiXanh * 100;

        // 3. Ép đối phương ra góc (Xanh ở vị trí điểm cao -> Bot bị trừ điểm)
        diem -= diemViTriXanh * 10;

        return diem;
    }

    // Hàm phụ đếm số ô trống xung quanh 1 quân cờ để tính không gian di chuyển
    private int demSoNuocDiTaiO(int i, int j) {
        int count = 0;
        for (int a = -1; a <= 1; a++) {
            for (int b = -1; b <= 1; b++) {
                if (a == 0 && b == 0)
                    continue;
                int ni = i + a, nj = j + b;

                if (toaDoHopLe(ni) && toaDoHopLe(nj)) {
                    if ((i + j) % 2 != 0 && a != 0 && b != 0)
                        continue;
                    if (maTran[ni][nj] == 0) {
                        count++;
                    }
                }
            }
        }
        return count;
    }
}