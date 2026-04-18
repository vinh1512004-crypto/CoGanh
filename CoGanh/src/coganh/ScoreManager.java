package coganh;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;

public class ScoreManager {
    private ArrayList<String> history = new ArrayList<>();
    private final String fileName = "history_log.txt";

    public ScoreManager() {
        loadHistory();
    }

    public void loadHistory() {
        history.clear();
        File file = new File(fileName);
        if (!file.exists()) return;

        try (BufferedReader br = new BufferedReader(new FileReader(file))) {
            String line;
            while ((line = br.readLine()) != null) {
                history.add(line);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void addResult(String winner, boolean isPvE, String name) {
        String mode = isPvE ? "PvE" : "PvP";
        String record = winner + "|" + mode + "|" + name;
        
        history.add(record);
        try (BufferedWriter bw = new BufferedWriter(new FileWriter(fileName, true))) {
            bw.write(record);
            bw.newLine();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    // [CẬP NHẬT] Lấy danh sách giới hạn số ván (Thay vì mặc định 10)
    public ArrayList<String> getRecentHistory(int limit) {
        ArrayList<String> recent = new ArrayList<>();
        int start = Math.max(0, history.size() - limit);
        for (int i = start; i < history.size(); i++) {
            recent.add(history.get(i));
        }
        return recent;
    }

    public int getTotalGames() {
        return history.size();
    }

    public int getBlueWins() {
        int count = 0;
        for (String s : history) {
            if (s.startsWith("XANH")) count++;
        }
        return count;
    }

    public int getRedWins() {
        int count = 0;
        for (String s : history) {
            if (s.startsWith("DO")) count++;
        }
        return count;
    }

    // ==========================================
    // CÁC HÀM MỚI PHỤC VỤ THỐNG KÊ AI (PvE)
    // ==========================================
    
    public int getTotalPvEGames() {
        int count = 0;
        for (String s : history) {
            if (s.contains("|PvE|")) count++;
        }
        return count;
    }

    public int getPvEBlueWins() {
        int count = 0;
        for (String s : history) {
            if (s.startsWith("XANH") && s.contains("|PvE|")) count++;
        }
        return count;
    }

    public int getPvERedWins() {
        int count = 0;
        for (String s : history) {
            if (s.startsWith("DO") && s.contains("|PvE|")) count++;
        }
        return count;
    }
}