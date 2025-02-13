package com.example.server_management.models;

public enum AuctionStatus {
    ONGOING,    // กำลังประมูล
    COMPLETED,  // มีผู้ชนะ
    CANCELLED,  // ถูกยกเลิกโดยเจ้าของ
    ENDED       // หมดเวลาประมูล โดยไม่มีคนประมูล
}
