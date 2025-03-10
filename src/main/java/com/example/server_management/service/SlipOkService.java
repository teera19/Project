package com.example.server_management.service;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.multipart.MultipartFile;

import java.util.Map;

@Service
public class SlipOkService {

    @Value("${slipok.api.url}")
    private String apiUrl;

    @Value("${slipok.api.key}")
    private String apiKey;

    public Map<String, Object> validateSlip(MultipartFile slip) {
        try {
            HttpHeaders headers = new HttpHeaders();
            headers.set("x-authorization", apiKey); // ✅ ใช้ x-authorization ตาม API
            headers.setContentType(MediaType.MULTIPART_FORM_DATA);

            MultiValueMap<String, Object> body = new LinkedMultiValueMap<>();
            body.add("files", new ByteArrayResource(slip.getBytes()) {
                @Override
                public String getFilename() {
                    return slip.getOriginalFilename();
                }

                @Override
                public long contentLength() {
                    return slip.getSize();
                }
            });

            HttpEntity<MultiValueMap<String, Object>> requestEntity = new HttpEntity<>(body, headers);
            RestTemplate restTemplate = new RestTemplate();

            ResponseEntity<Map> response = restTemplate.exchange(apiUrl, HttpMethod.POST, requestEntity, Map.class);

            // ✅ ตรวจสอบว่า API Response กลับมาสำเร็จหรือไม่
            Map<String, Object> responseBody = response.getBody();
            if (responseBody != null && Boolean.TRUE.equals(responseBody.get("success"))) {
                return responseBody;
            } else {
                return Map.of("error", "Slip verification failed");
            }

        } catch (Exception e) {
            e.printStackTrace();
            return Map.of("error", "Slip validation failed");
        }
    }

}
