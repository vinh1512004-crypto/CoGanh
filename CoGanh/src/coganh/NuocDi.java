package coganh;

public class NuocDi {
    public int hangCu, cotCu;       // Tọa độ hiện tại (i, j cũ)
    public int hangMoi, cotMoi;     // Tọa độ muốn đi tới (i, j mới)
    public int diemSo;              // Điểm đánh giá (càng cao càng tốt)

    public NuocDi(int hangCu, int cotCu, int hangMoi, int cotMoi) {
        this.hangCu = hangCu;
        this.cotCu = cotCu;
        this.hangMoi = hangMoi;
        this.cotMoi = cotMoi;
    }
}