package com.govagency.util;

import java.time.LocalDateTime;
import java. time.format.DateTimeFormatter;
import java.util.Map;

import com.govagency.model. Citizen;

public class CitizenIdGenerator {

    /**
     * Generate unique citizen ID
     * @param citizenMap existing citizens map to get count
     * @return unique citizen ID
     */
    public static String generateCitizenId(Map<String, Citizen> citizenMap) {
        // Get total number of citizens (0-indexed, so add 1)
        int totalCitizens = citizenMap. size() + 1;
        
        // Get current date and time
        LocalDateTime now = LocalDateTime.now();
        
        // Format: MMDDYY (e.g., 112625 for Nov 26, 2025)
        DateTimeFormatter dateFormatter = DateTimeFormatter.ofPattern("MMddyy");
        String datePart = now.format(dateFormatter);
        
        // Format: HHmm (e.g., 1503 for 3:03 PM)
        DateTimeFormatter timeFormatter = DateTimeFormatter.ofPattern("HHmm");
        String timePart = now.format(timeFormatter);
        
        // Get seconds (e.g., 34)
        String secondsPart = String.format("%02d", now.getSecond());
        
        // Combine: CT-{total}-{date}-{time}-{seconds}
        String citizenId = String.format("CT-%d-%s-%s-%s", 
            totalCitizens, 
            datePart, 
            timePart, 
            secondsPart);
        
        return citizenId;
    }

    /**
     * Generate unique citizen ID with custom prefix
     * @param citizenMap existing citizens map to get count
     * @param prefix custom prefix (default: CT)
     * @return unique citizen ID
     */
    public static String generateCitizenId(Map<String, Citizen> citizenMap, String prefix) {
        int totalCitizens = citizenMap.size() + 1;
        LocalDateTime now = LocalDateTime.now();
        
        DateTimeFormatter dateFormatter = DateTimeFormatter.ofPattern("MMddyy");
        String datePart = now.format(dateFormatter);
        
        DateTimeFormatter timeFormatter = DateTimeFormatter.ofPattern("HHmm");
        String timePart = now.format(timeFormatter);
        
        String secondsPart = String.format("%02d", now.getSecond());
        
        String citizenId = String.format("%s-%d-%s-%s-%s", 
            prefix, 
            totalCitizens, 
            datePart, 
            timePart, 
            secondsPart);
        
        return citizenId;
    }
}