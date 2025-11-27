package com.govagency;

import java.util.List;

import org.json.JSONObject;

import com.govagency.model.Citizen;
import com.govagency.model.Document;
import com.govagency.model.ServiceRequest;

public abstract class Database {
    
    public abstract void addCitizen(Citizen c);
    public abstract void deleteCitizen(String citizenId);
    public abstract void updateCitizen(String citizenId, Citizen c);
    public abstract List<JSONObject> getAllCitizens();
    
    public abstract void addDocument(Document d);
    public abstract void deleteDocument(String documentId);
    public abstract void updateDocument(String documentId, Document d);
    public abstract List<JSONObject> getAllDocuments();
    
    public abstract void addRequest(ServiceRequest r);
    public abstract void deleteRequest(String requestId);
    public abstract void updateRequest(String requestId, ServiceRequest r);
    public abstract List<JSONObject> getAllRequests();
    
    protected void logOperation(String operation) {
        System.out.println("Database operation: " + operation);
    }
}