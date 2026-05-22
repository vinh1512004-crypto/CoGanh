# 👑 CỜ GÁNH — TRÒ CHƠI DÂN GIAN VIỆT NAM 👑

<div align="center">
  <h3>✨ Trò chơi trí tuệ dân gian độc đáo của người Việt — Nơi hội tụ tư duy chiến thuật, sự nhạy bén và tinh thần thượng võ! ✨</h3>
  <p><i>Cờ Gánh (hay còn gọi là cờ Chẹt) là trò chơi dân gian mộc mạc nhưng ẩn chứa những thế cờ vô cùng biến hóa và kịch tính. Không chỉ đơn thuần là giải trí, Cờ Gánh là cuộc đấu trí cân não đòi hỏi người chơi phải có tầm nhìn chiến lược, khả năng xoay chuyển cục diện nhanh chóng và tận dụng sơ hở của đối phương để thực hiện những pha "Gánh", "Chẹt" ngoạn mục.</i></p>
</div>


---

> ### 🌟 **DỰ ÁN SỐ HÓA VÀ BẢO TỒN NÉT ĐẸP CHIẾN THUẬT TRÍ TUỆ VIỆT**
> *Trải nghiệm tựa game cờ truyền thống lâu đời được nâng tầm với giao diện đồ họa sống động cùng Trí Tuệ Nhân Tạo (AI) thông minh thách thức mọi kỳ thủ.*

---

## 📌 GIỚI THIỆU DỰ ÁN

**Cờ Gánh** là một dự án game Desktop cao cấp được phát triển bằng ngôn ngữ **Java** (sử dụng thư viện giao diện đồ họa **Swing/AWT**). Trò chơi mô phỏng một cách chân thực và sống động bộ môn cờ truyền thống lâu đời của Việt Nam, tái hiện đầy đủ các quy tắc chiến thuật độc đáo và kịch tính:

*   **💥 Gánh:** Ăn quân đối phương khi kẹp giữa hai quân ta.
*   **⚡ Chẹt:** Vây thế bí và tiêu diệt quân của đối thủ.
*   **🌀 Mở cờ:** Tạo nước đi chiến lược chủ động đón đầu đối thủ.

---

## 🚀 TÍNH NĂNG NỔI BẬT

### 🎮 CHẾ ĐỘ CHƠI LINH HOẠT
*   **👥 Người vs Người (PvP):** Trận đấu tay đôi kịch tính trực tiếp trên cùng một thiết bị, hỗ trợ đếm ngược thời gian (**Timer**) để tăng áp lực thi đấu.
*   **🤖 Người vs Máy (PvE):** 
    *   Thách thức trí tuệ nhân tạo **Bot AI** thông minh áp dụng thuật toán **Minimax** kết hợp cắt tỉa **Alpha-Beta (Alpha-Beta Pruning)**.
    *   Hệ thống **Tự động thăng hạng (Rank up)** độ khó của AI dựa trên chuỗi thắng liên tiếp của người chơi, mang lại thử thách tăng tiến không ngừng.

### 💡 TRỢ NĂNG THÔNG MINH
*   **🔮 Gợi ý nước đi (Hint):** AI tự động phân tích thế cờ để đề xuất nước đi tối ưu nhất (giới hạn 3 lần/ván).
*   **↩️ Hoàn tác (Undo / Thu hồi lệnh):** Hỗ trợ người chơi đi lại nước cờ sai lầm (giới hạn 3 lần/ván).

### ⚙️ HỆ THỐNG & TRẢI NGHIỆM
*   **💾 Lưu & Tải game (Save/Load):** Tự động lưu tiến trình game hoặc lưu thủ công để có thể tiếp tục cuộc chơi mọi lúc.
*   **📊 Bảng xếp hạng & Lịch sử:** Lưu trữ lịch sử đấu trực quan kèm biểu đồ tỉ lệ thắng PvE chi tiết.
*   **🎵 Trải nghiệm Nghe - Nhìn:** Hiệu ứng chuyển động cờ mượt mà, kết hợp âm nhạc và âm thanh sống động (hỗ trợ Bật/Tắt dễ dàng).
*   **🛡️ Chống lặp vô hạn:** Tự động phát hiện và thoát khỏi trạng thái lặp nước vô hạn giữa người chơi và máy.

---

## 📂 CẤU TRÚC MÃ NGUỒN (`src/coganh/`)

| Tên File | Vai trò & Chức năng chính |
| :--- | :--- |
| 🖥️ **`GamePanel_CoGanh.java`** | Giao diện đồ họa (UI) chính, xử lý luồng vẽ màn hình và là **Main Class** khởi chạy game. |
| ⚙️ **`GameController.java`** | Trung tâm điều khiển toàn bộ logic trận đấu (luật chơi, kiểm tra gánh/chẹt, lượt đi, kết quả). |
| 🧠 **`BotAI.java`** | Trí tuệ nhân tạo của máy, tính toán điểm số thế trận và tìm kiếm nước đi bằng **Minimax & Alpha-Beta**. |
| 🗺️ **`BanCo.java` & `NuocDi.java`**| Định nghĩa cấu trúc bàn cờ 5x5 và mô hình hóa thông tin tọa độ các nước đi. |
| 💾 **`GameSaveData.java`** | Phụ trách đọc/ghi dữ liệu để lưu trữ trạng thái bàn cờ hiện tại ra file text (`save_pve.txt`). |
| 📊 **`ScoreManager.java`** | Ghi nhận lịch sử các ván đấu và thống kê tỷ lệ thắng thua. |
| 🎭 **`PlayerSetupDialog.java`** | Hộp thoại thiết lập thông tin (tên người chơi, chọn hình đại diện/avatar) mang đậm phong cách dân gian. |

---

## 💻 CÀI ĐẶT VÀ KHỞI CHẠY

1.  **☕ Yêu cầu hệ thống:** Máy tính của bạn cần cài đặt **Java Development Kit (JDK)** từ phiên bản **8** trở lên.
2.  **📂 Mở dự án:** Nhập thư mục vào các IDE như **IntelliJ IDEA**, **Eclipse**, hoặc **VS Code**.
3.  **▶️ Khởi chạy chương trình:** Chạy trực tiếp lớp `coganh.GamePanel_CoGanh`.

---

## 📜 LUẬT CHƠI CƠ BẢN

*   **🟪 Bàn cờ:** Gồm 25 giao điểm tạo bởi lưới 5x5.
*   **🔴 Lực lượng:** Mỗi bên sở hữu **8 quân cờ** (Đỏ và Xanh).
*   **🎯 Mục tiêu:** Tiêu diệt toàn bộ quân đối phương hoặc vây chặt khiến đối phương không còn nước đi hợp lệ nào.

---

<div align="center">
  <h3>✨ Chúc các bạn có những giây phút đấu trí thư giãn cùng Cờ Gánh Việt Nam! ✨</h3>
</div>
