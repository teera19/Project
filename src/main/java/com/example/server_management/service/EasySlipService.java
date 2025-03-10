package com.example.server_management.service;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.multipart.MultipartFile;

import java.util.Map;

@Service
public class EasySlipService {

    @Value("${easyslip.api.url}")
    private String apiUrl;

    @Value("${easyslip.api.key}")
    private String apiKey;

    public Map<String, Object> validateSlip(MultipartFile slip) {
        try {
            RestTemplate restTemplate = new RestTemplate();

            // สร้าง Headers
            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.MULTIPART_FORM_DATA);
            headers.set("Authorization", apiKey);

            // สร้าง Body สำหรับส่งข้อมูล
            MultiValueMap<String, Object> body = new LinkedMultiValueMap<>();
            body.add("files", slip.getResource());

            // สร้าง Request Entity
            HttpEntity<MultiValueMap<String, Object>> requestEntity = new HttpEntity<>(body, headers);

            // เรียกใช้งาน EasySlip API
            ResponseEntity<Map> response = restTemplate.postForEntity(apiUrl, requestEntity, Map.class);

            if (response.getStatusCode() == HttpStatus.OK) {
                return response.getBody();
            }

            return null;
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }
}
