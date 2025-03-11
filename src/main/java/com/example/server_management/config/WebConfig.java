package com.example.server_management.config;

import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.converter.HttpMessageConverter;
import org.springframework.http.converter.json.MappingJackson2HttpMessageConverter;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;
import java.util.List;

@Configuration
public class WebConfig implements WebMvcConfigurer {

    @Override
    public void configureMessageConverters(List<HttpMessageConverter<?>> converters) {
        // เพิ่ม MappingJackson2HttpMessageConverter สำหรับรองรับ UTF-8
        MappingJackson2HttpMessageConverter converter = new MappingJackson2HttpMessageConverter();

        // ตั้งค่า ObjectMapper เพื่อให้สามารถอ่านข้อมูล JSON ในรูปแบบ UTF-8
        ObjectMapper objectMapper = new ObjectMapper();
        objectMapper.configure(DeserializationFeature.ACCEPT_SINGLE_VALUE_AS_ARRAY, true);

        // ใช้ ObjectMapper ที่ตั้งค่าแล้ว
        converter.setObjectMapper(objectMapper);

        // เพิ่ม MessageConverter ใน converters
        converters.add(converter);
    }
}
