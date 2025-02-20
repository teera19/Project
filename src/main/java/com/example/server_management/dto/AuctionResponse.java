package com.example.server_management.dto;

import com.example.server_management.models.Auction;

import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.util.Base64;
import javax.imageio.ImageIO;

public class AuctionResponse {
    private String imageBase64; // ✅ ลดขนาดภาพก่อนแปลงเป็น Base64

    public AuctionResponse(Auction auction) {
        if (auction.getImage() != null) {
            this.imageBase64 = convertToBase64Thumbnail(auction.getImage());
        } else {
            this.imageBase64 = null;
        }
    }

    private String convertToBase64Thumbnail(byte[] imageBytes) {
        try {
            ByteArrayInputStream bais = new ByteArrayInputStream(imageBytes);
            BufferedImage originalImage = ImageIO.read(bais);

            // ✅ กำหนดขนาด Thumbnail
            int targetWidth = 200;  // ปรับขนาดภาพให้เล็กลง
            int targetHeight = (originalImage.getHeight() * targetWidth) / originalImage.getWidth();
            Image resizedImage = originalImage.getScaledInstance(targetWidth, targetHeight, Image.SCALE_SMOOTH);

            // ✅ แปลงกลับเป็น BufferedImage
            BufferedImage bufferedThumbnail = new BufferedImage(targetWidth, targetHeight, BufferedImage.TYPE_INT_RGB);
            Graphics2D g2d = bufferedThumbnail.createGraphics();
            g2d.drawImage(resizedImage, 0, 0, null);
            g2d.dispose();

            // ✅ แปลง BufferedImage เป็น byte[]
            ByteArrayOutputStream baos = new ByteArrayOutputStream();
            ImageIO.write(bufferedThumbnail, "jpg", baos);

            // ✅ แปลงเป็น Base64
            return Base64.getEncoder().encodeToString(baos.toByteArray());

        } catch (Exception e) {
            return null; // กรณีเกิดข้อผิดพลาด
        }
    }
}
