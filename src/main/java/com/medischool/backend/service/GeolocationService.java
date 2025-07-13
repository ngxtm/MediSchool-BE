package com.medischool.backend.service;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;

import org.springframework.stereotype.Service;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

import lombok.extern.slf4j.Slf4j;

@Service
@Slf4j
public class GeolocationService {
    
    private static final String IP_API_BASE_URL = "http://ip-api.com/json/";
    private static final String FALLBACK_LOCATION = "Việt Nam";
    
    private final ObjectMapper objectMapper = new ObjectMapper();
    
    public String getLocationFromIp(String ipAddress) {
        if (ipAddress == null || ipAddress.isEmpty() || ipAddress.equals("unknown")) {
            return FALLBACK_LOCATION;
        }
        
        if (isLocalOrPrivateIp(ipAddress)) {
            return FALLBACK_LOCATION;
        }
        
        try {
            String apiUrl = IP_API_BASE_URL + ipAddress;
            String response = makeHttpRequest(apiUrl);
            
            if (response != null) {
                JsonNode jsonNode = objectMapper.readTree(response);
                
                // Kiểm tra status
                if (jsonNode.has("status") && "success".equals(jsonNode.get("status").asText())) {
                    String city = jsonNode.has("city") ? jsonNode.get("city").asText() : "";
                    String region = jsonNode.has("regionName") ? jsonNode.get("regionName").asText() : "";
                    String country = jsonNode.has("country") ? jsonNode.get("country").asText() : "";
                    
                    // Tạo location string
                    StringBuilder location = new StringBuilder();
                    if (!city.isEmpty()) {
                        location.append(city);
                    }
                    if (!region.isEmpty() && !region.equals(city)) {
                        if (location.length() > 0) location.append(", ");
                        location.append(region);
                    }
                    if (!country.isEmpty()) {
                        if (location.length() > 0) location.append(", ");
                        location.append(country);
                    }
                    
                    if (location.length() > 0) {
                        log.info("Geolocation found for IP {}: {}", ipAddress, location.toString());
                        return location.toString();
                    }
                } else {
                    log.warn("IP-API returned error for IP {}: {}", ipAddress, response);
                }
            }
        } catch (Exception e) {
            log.error("Error getting geolocation for IP {}: {}", ipAddress, e.getMessage());
        }
        
        return FALLBACK_LOCATION;
    }
    
    private String makeHttpRequest(String urlString) {
        try {
            URL url = new URL(urlString);
            HttpURLConnection connection = (HttpURLConnection) url.openConnection();
            connection.setRequestMethod("GET");
            connection.setConnectTimeout(5000);
            connection.setReadTimeout(5000);
            
            int responseCode = connection.getResponseCode();
            if (responseCode == 200) {
                BufferedReader reader = new BufferedReader(
                    new InputStreamReader(connection.getInputStream())
                );
                StringBuilder response = new StringBuilder();
                String line;
                while ((line = reader.readLine()) != null) {
                    response.append(line);
                }
                reader.close();
                return response.toString();
            } else {
                log.warn("HTTP request failed with response code: {}", responseCode);
            }
        } catch (Exception e) {
            log.error("Error making HTTP request to {}: {}", urlString, e.getMessage());
        }
        return null;
    }

    private boolean isLocalOrPrivateIp(String ipAddress) {
        if (ipAddress == null) return true;
        
        // Localhost
        if (ipAddress.equals("127.0.0.1") || ipAddress.equals("localhost")) {
            return true;
        }
        
        // Private IP ranges
        String[] parts = ipAddress.split("\\.");
        if (parts.length != 4) return true;
        
        try {
            int firstOctet = Integer.parseInt(parts[0]);
            int secondOctet = Integer.parseInt(parts[1]);
            
            // 10.0.0.0 - 10.255.255.255
            if (firstOctet == 10) return true;
            
            // 172.16.0.0 - 172.31.255.255
            if (firstOctet == 172 && secondOctet >= 16 && secondOctet <= 31) return true;
            
            // 192.168.0.0 - 192.168.255.255
            if (firstOctet == 192 && secondOctet == 168) return true;
            
        } catch (NumberFormatException e) {
            return true;
        }
        
        return false;
    }
} 