# Cờ Gánh (Vietnamese Traditional Board Game)

## 📌 Giới thiệu dự án
Cờ Gánh là một tựa game desktop được phát triển bằng ngôn ngữ **Java** (sử dụng thư viện giao diện Swing/AWT). Trò chơi mô phỏng một cách chân thực bộ môn cờ truyền thống lâu đời của Việt Nam là "Cờ Gánh", với đầy đủ các quy tắc chiến thuật đặc trưng như **Gánh**, **Chẹt**, và **Mở cờ**.

## 🚀 Tính năng nổi bật
- **Hai chế độ chơi đa dạng:**
  - **Người vs Người (PvP):** Chơi trực tiếp 2 người trên cùng một thiết bị. Có tích hợp hệ thống đếm ngược thời gian (Timer) tăng tính kịch tính.
  - **Người vs Máy (PvE):** Thử sức với Bot AI thông minh được xây dựng dựa trên thuật toán **Minimax** kết hợp cắt tỉa **Alpha-Beta (Alpha-Beta Pruning)** để tối ưu hóa thời gian suy nghĩ và tìm kiếm nước đi tốt nhất. Đặc biệt, AI có hệ thống **Tự động thăng hạng (Rank up)** độ sâu tìm kiếm (độ khó) dựa trên chuỗi chiến thắng liên tiếp của người chơi.
- **Hỗ trợ người chơi:**
  - **Gợi ý nước đi (Hint):** Tự động tìm kiếm và đề xuất nước đi tối ưu nhất bằng AI (Giới hạn 3 lần/ván).
  - **Hoàn tác (Undo):** Cho phép người chơi đi lại khi lỡ nước (Giới hạn 3 lần/ván).
- **Hệ thống Lưu & Tải game (Save/Load):** Tự động lưu hoặc lưu thủ công trận đấu đang dang dở để có thể chơi tiếp bất cứ lúc nào.
- **Bảng xếp hạng & Lịch sử (Leaderboard):** Ghi nhận kết quả của các ván cờ, quản lý điểm số.
- **Trải nghiệm Nghe - Nhìn:** Hoạt ảnh (Animation) di chuyển quân cờ mượt mà, hỗ trợ bật/tắt âm thanh (Sound) sống động.
- **Chống lặp vô hạn:** Tự động nhận diện và xử lý khi ván cờ (đặc biệt là Bot) bị mắc kẹt ở một trạng thái lặp đi lặp lại.

## 📂 Cấu trúc mã nguồn
Dự án được tổ chức gọn gàng trong thư mục `src/coganh/`:
- `GamePanel_CoGanh.java`: Lớp giao diện (UI) chính và là **Main Class** khởi chạy chương trình.
- `GameController.java`: Lớp chịu trách nhiệm điều phối toàn bộ Logic chính của trò chơi (Luật chơi, check thắng/thua, quản lý lượt...).
- `BotAI.java`: Cốt lõi của Trí tuệ nhân tạo. Triển khai thuật toán **Minimax** với kỹ thuật **Alpha-Beta Pruning** để tính toán điểm số và đưa ra nước đi ưu việt nhất cho Bot.
- `BanCo.java` & `NuocDi.java`: Quản lý cấu trúc bàn cờ 5x5 và thông tin các nước đi.
- `GameSaveData.java`: Đảm nhiệm việc đọc/ghi file để lưu trữ tiến trình trận đấu (`save_pve.txt`, `save_pvp.txt`).
- `ScoreManager.java`: Quản lý thông tin điểm số và lịch sử ván đấu.
- `PlayerSetupDialog.java`: Giao diện thiết lập thông tin người chơi/avatar trước khi vào ván.

## 💻 Cách cài đặt và khởi chạy
1. Yêu cầu hệ thống phải được cài đặt sẵn **Java Development Kit (JDK)** (khuyến nghị phiên bản 8 trở lên).
2. Bạn có thể mở thư mục dự án bằng các IDE phổ biến như **Eclipse**, **IntelliJ IDEA**, hoặc **VS Code**.
3. Biên dịch dự án và chạy file chính: `coganh.GamePanel_CoGanh`.

## 📜 Thông tin thêm
- Bàn cờ gồm 25 giao điểm (5x5), mỗi bên sở hữu 8 quân cờ (Xanh và Đỏ).
- Mục tiêu: Ăn hết quân cờ của đối phương bằng kỹ thuật Gánh, Chẹt, hoặc làm cho đối phương không thể di chuyển.

---
*Dự án là một phần nỗ lực nhằm số hóa và bảo tồn nét đẹp của trò chơi dân gian Việt Nam.*
