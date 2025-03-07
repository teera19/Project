package com.example.server_management.component;

import org.springframework.stereotype.Component;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Component
public class ChatStatusTracker {
    private final Map<String, Integer> activeChats = new ConcurrentHashMap<>();

    // ✅ บันทึกว่าผู้ใช้กำลังเปิดห้องแชทไหน
    public void setActiveChat(String username, int chatId) {
        activeChats.put(username, chatId);
    }

    // ✅ ลบข้อมูลเมื่อผู้ใช้ออกจากห้องแชท
    public void clearActiveChat(String username) {
        activeChats.remove(username);
    }

    // ✅ ตรวจสอบว่าผู้ใช้กำลังดูห้องแชทที่กำหนดอยู่ไหม
    public boolean isUserInChat(String username, int chatId) {
        return activeChats.getOrDefault(username, -1) == chatId;
    }
    public Integer getActiveChat(String username) {
        return activeChats.get(username);
    }
}
