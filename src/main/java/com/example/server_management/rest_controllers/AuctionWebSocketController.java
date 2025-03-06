package com.example.server_management.rest_controllers;


import com.example.server_management.models.Auction;
import com.example.server_management.models.Bid;
import com.example.server_management.repository.BidRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.handler.annotation.SendTo;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Controller;

import java.util.Map;

@Controller
public class AuctionWebSocketController {

    @Autowired
    private SimpMessagingTemplate messagingTemplate;

    @Autowired
    private BidRepository bidRepository;

    @MessageMapping("/auction.placeBid")
    @SendTo("/topic/auction")
    public Bid handleNewBid(Bid bid) {
        // ค้นหาการประมูลที่เกี่ยวข้อง
        Auction auction = bid.getAuction();

        // ✅ ค้นหาผู้ที่บิดสูงสุดก่อนหน้านี้
        Bid highestBidObj = bidRepository.findTopByAuctionOrderByBidAmountDesc(auction);
        double highestBid = highestBidObj != null ? highestBidObj.getBidAmount() : auction.getStartingPrice();
        String previousBidder = highestBidObj != null ? highestBidObj.getUser().getUserName() : null;

        // ✅ ตรวจสอบว่าบิดสูงกว่าราคาปัจจุบัน
        if (bid.getBidAmount() <= highestBid) {
            throw new RuntimeException("Bid amount must be higher than the current highest bid: " + highestBid);
        }

        // ✅ บันทึก Bid ใหม่
        bidRepository.save(bid);

        // ✅ แจ้งเตือนทุกคนที่ดูประมูลอยู่
        messagingTemplate.convertAndSend("/topic/auction", bid);

        // ✅ แจ้งเตือนผู้ที่ถูกบิดแซง
        if (previousBidder != null && !previousBidder.equals(bid.getUser().getUserName())) {
            messagingTemplate.convertAndSendToUser(previousBidder, "/queue/notifications",
                    Map.of("message", "⚠️ คุณถูกบิดแซงในประมูล " + auction.getProductName() + " ด้วยราคา " + bid.getBidAmount()));
        }

        return bid;
    }
}
