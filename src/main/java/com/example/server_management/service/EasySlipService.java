package com.example.server_management.service;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.core.io.Resource;
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
            // ✅ สร้าง Header
            HttpHeaders headers = new HttpHeaders();
            headers.set("Authorization", apiKey);
            headers.setContentType(MediaType.MULTIPART_FORM_DATA);

            // ✅ สร้าง Body
            MultiValueMap<String, Object> body = new LinkedMultiValueMap<>();
            body.add("files", convertMultipartFileToResource(slip));

            HttpEntity<MultiValueMap<String, Object>> requestEntity = new HttpEntity<>(body, headers);

            RestTemplate restTemplate = new RestTemplate();
            ResponseEntity<Map> response = restTemplate.exchange(apiUrl, HttpMethod.POST, requestEntity, Map.class);

            return response.getBody();
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }

    private Resource convertMultipartFileToResource(MultipartFile file) throws Exception {
        return new ByteArrayResource(file.getBytes()) {
            @Override
            public String getFilename() {
                return file.getOriginalFilename();
            }
        };
    }
}
