package com.govagency;

import java.io.FileWriter;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

import org.json.JSONArray;
import org.json.JSONObject;

import com.govagency.model.Archive;
import com.govagency.model.Citizen;
import com.govagency.model.Document;
import com.govagency.model.ServiceRequest;

public class LocalDatabase extends Database {

    private static final String DB_PATH = "database.json";
    private final JSONObject root;

    public LocalDatabase() {
        root = load();
    }

    private JSONObject load() {
        try {
            if (! Files.exists(Path.of(DB_PATH))) {
                return initializeDatabase();
            }
            
            String text = Files.readString(Path. of(DB_PATH));
            JSONObject obj = new JSONObject(text);
            
            if (!obj.has("citizens")) {
                obj.put("citizens", new JSONArray());
            }
            if (! obj.has("documents")) {
                obj.put("documents", new JSONArray());
            }
            if (!obj.has("requests")) {
                obj. put("requests", new JSONArray());
            }
            if (!obj.has("archives")) {
                obj.put("archives", new JSONArray());
            }
            
            save(obj);
            return obj;
        } catch (IOException | org.json.JSONException e) {
            System.err.println("Error loading database: " + e. getMessage());
            return initializeDatabase();
        }
    }

    private JSONObject initializeDatabase() {
        JSONObject obj = new JSONObject();
        obj.put("citizens", new JSONArray());
        obj.put("documents", new JSONArray());
        obj.put("requests", new JSONArray());
        obj.put("archives", new JSONArray());
        save(obj);
        return obj;
    }

    private void save(JSONObject data) {
        try (FileWriter fw = new FileWriter(DB_PATH)) {
            fw.write(data.toString(4));
        } catch (Exception e) {
            System.err.println("Error saving database: " + e.getMessage());
        }
    }

    @Override
    public void addCitizen(Citizen c) {
        try {
            JSONArray arr = root.optJSONArray("citizens");
            if (arr == null) {
                arr = new JSONArray();
                root.put("citizens", arr);
            }

            JSONObject obj = new JSONObject();
            obj.put("id", c.getId());
            obj.put("name", c.getName());
            obj.put("email", c.getEmail());
            obj.put("number", c.getNumber());
            obj.put("password", c.getPassword());

            arr.put(obj);
            save(root);
        } catch (org.json.JSONException e) {
            System.err.println("Error adding citizen: " + e.getMessage());
        }
    }

    @Override
    public void deleteCitizen(String citizenId) {
        try {
            JSONArray arr = root.optJSONArray("citizens");
            if (arr == null) return;
            
            JSONArray newArr = new JSONArray();

            for (int i = 0; i < arr.length(); i++) {
                JSONObject obj = arr.getJSONObject(i);
                if (!obj.getString("id").equals(citizenId)) {
                    newArr.put(obj);
                }
            }

            root.put("citizens", newArr);
            save(root);
        } catch (org.json.JSONException e) {
            System.err.println("Error deleting citizen: " + e.getMessage());
        }
    }

    @Override
    public void updateCitizen(String citizenId, Citizen c) {
        try {
            JSONArray arr = root.optJSONArray("citizens");
            if (arr == null) return;

            for (int i = 0; i < arr.length(); i++) {
                JSONObject obj = arr.getJSONObject(i);
                if (obj.getString("id").equals(citizenId)) {
                    obj.put("name", c.getName());
                    obj.put("email", c.getEmail());
                    obj.put("number", c.getNumber());
                    obj.put("password", c.getPassword());
                    break;
                }
            }

            save(root);
        } catch (org.json.JSONException e) {
            System.err.println("Error updating citizen: " + e.getMessage());
        }
    }

    @Override
    public void addDocument(Document d) {
        try {
            JSONArray arr = root.optJSONArray("documents");
            if (arr == null) {
                arr = new JSONArray();
                root.put("documents", arr);
            }

            JSONObject obj = new JSONObject();
            obj.put("id", d.getId());
            obj.put("requestId", d.getAttachedRequestId());
            obj.put("citizenId", d.getCitizenId());
            obj.put("filePath", d.getFilePath());
            obj.put("status", d.getStatus().name());
            obj.put("reviewComment", d.getReviewComment() != null ? d.getReviewComment() : "");
            obj.put("uploadTime", d.getUploadTime().toString());
            
            if (d.getReviewTime() != null) {
                obj.put("reviewTime", d.getReviewTime().toString());
            }

            arr.put(obj);
            save(root);
            
            System.out.println("✓ Document saved successfully:");
            System.out.println("  - ID: " + d.getId());
            System.out.println("  - Citizen ID: " + d.getCitizenId());
            System.out.println("  - Request ID: " + d.getAttachedRequestId());
            System.out.println("  - File Path: " + d.getFilePath());
            
        } catch (org.json.JSONException e) {
            System.err.println("Error adding document: " + e.getMessage());
        }
    }

    @Override
    public void deleteDocument(String documentId) {
        try {
            JSONArray arr = root.optJSONArray("documents");
            if (arr == null) return;
            
            JSONArray newArr = new JSONArray();

            for (int i = 0; i < arr.length(); i++) {
                JSONObject obj = arr.getJSONObject(i);
                if (!obj.getString("id").equals(documentId)) {
                    newArr.put(obj);
                }
            }

            root.put("documents", newArr);
            save(root);
        } catch (org.json.JSONException e) {
            System.err.println("Error deleting document: " + e.getMessage());
        }
    }

    @Override
    public void updateDocument(String documentId, Document d) {
        try {
            JSONArray arr = root.optJSONArray("documents");
            if (arr == null) return;

            for (int i = 0; i < arr.length(); i++) {
                JSONObject obj = arr.getJSONObject(i);
                if (obj.getString("id").equals(documentId)) {
                    obj.put("status", d.getStatus().name());
                    obj.put("reviewComment", d.getReviewComment());
                    if (d.getReviewTime() != null) {
                        obj.put("reviewTime", d.getReviewTime().toString());
                    }
                    break;
                }
            }

            save(root);
            System.out.println("Document updated in database: " + documentId);
        } catch (org.json.JSONException e) {
            System.err.println("Error updating document: " + e.getMessage());
        }
    }

    @Override
    public void addRequest(ServiceRequest r) {
        try {
            JSONArray arr = root.optJSONArray("requests");
            if (arr == null) {
                arr = new JSONArray();
                root.put("requests", arr);
            }

            JSONObject obj = new JSONObject();
            obj.put("id", r.getId());
            obj.put("citizenId", r.getCitizenId());
            obj.put("type", r.getServiceType());
            obj.put("description", r.getDescription());
            obj.put("status", r.getStatus().name());
            obj.put("adminNote", r.getAdminNote());
            obj.put("date", LocalDateTime.now().toString());

            arr.put(obj);
            save(root);
            System.out.println("Request saved to database: " + r.getId());
        } catch (org.json.JSONException e) {
            System.err.println("Error adding request: " + e.getMessage());
        }
    }

    @Override
    public void deleteRequest(String requestId) {
        try {
            JSONArray arr = root.optJSONArray("requests");
            if (arr == null) return;
            
            JSONArray newArr = new JSONArray();

            for (int i = 0; i < arr.length(); i++) {
                JSONObject obj = arr.getJSONObject(i);
                if (!obj.getString("id").equals(requestId)) {
                    newArr.put(obj);
                }
            }

            root.put("requests", newArr);
            save(root);
        } catch (org.json.JSONException e) {
            System.err.println("Error deleting request: " + e.getMessage());
        }
    }

    @Override
    public void updateRequest(String requestId, ServiceRequest r) {
        try {
            JSONArray arr = root.optJSONArray("requests");
            if (arr == null) return;

            for (int i = 0; i < arr.length(); i++) {
                JSONObject obj = arr.getJSONObject(i);
                if (obj.getString("id").equals(requestId)) {
                    obj.put("status", r.getStatus().name());
                    obj.put("adminNote", r.getAdminNote());
                    break;
                }
            }

            save(root);
            System.out.println("Request updated in database: " + requestId);
        } catch (org.json.JSONException e) {
            System.err.println("Error updating request: " + e.getMessage());
        }
    }

    @Override
    public List<JSONObject> getAllCitizens() {
        JSONArray arr = root.optJSONArray("citizens");
        List<JSONObject> list = new ArrayList<>();
        
        if (arr != null) {
            for (int i = 0; i < arr.length(); i++) {
                try {
                    list.add(arr.getJSONObject(i));
                } catch (org.json.JSONException e) {
                    System.err.println("Error reading citizen: " + e.getMessage());
                }
            }
        }
        
        return list;
    }

    @Override
    public List<JSONObject> getAllDocuments() {
        JSONArray arr = root.optJSONArray("documents");
        List<JSONObject> list = new ArrayList<>();
        
        if (arr != null) {
            for (int i = 0; i < arr.length(); i++) {
                try {
                    JSONObject doc = arr.getJSONObject(i);
                    if (!doc.has("requestId") && doc.has("attachedRequestId")) {
                        doc.put("requestId", doc.get("attachedRequestId"));
                    }
                    list.add(doc);
                } catch (org.json.JSONException e) {
                    System.err.println("Error reading document: " + e.getMessage());
                }
            }
        }
        
        return list;
    }

    @Override
    public List<JSONObject> getAllRequests() {
        JSONArray arr = root.optJSONArray("requests");
        List<JSONObject> list = new ArrayList<>();
        
        if (arr != null) {
            for (int i = 0; i < arr.length(); i++) {
                try {
                    list.add(arr.getJSONObject(i));
                } catch (org.json.JSONException e) {
                    System.err.println("Error reading request: " + e.getMessage());
                }
            }
        }
        
        return list;
    }

    public List<JSONObject> getRequestsByCitizenId(String citizenId) {
        JSONArray arr = root.optJSONArray("requests");
        List<JSONObject> list = new ArrayList<>();
        
        if (arr != null) {
            for (int i = 0; i < arr.length(); i++) {
                try {
                    JSONObject obj = arr.getJSONObject(i);
                    if (obj.optString("citizenId", "").equals(citizenId)) {
                        list.add(obj);
                    }
                } catch (org.json.JSONException e) {
                    System.err.println("Error reading request: " + e.getMessage());
                }
            }
        }
        
        return list;
    }

    public List<JSONObject> getDocumentsByRequestId(String requestId) {
        JSONArray arr = root.optJSONArray("documents");
        List<JSONObject> list = new ArrayList<>();
        
        if (arr != null) {
            for (int i = 0; i < arr.length(); i++) {
                try {
                    JSONObject obj = arr.getJSONObject(i);
                    String docRequestId = obj.optString("requestId", obj.optString("attachedRequestId", ""));
                    if (docRequestId.equals(requestId)) {
                        list.add(obj);
                    }
                } catch (org.json.JSONException e) {
                    System.err.println("Error reading document: " + e.getMessage());
                }
            }
        }
        
        return list;
    }

    public List<JSONObject> getDocumentsByCitizenId(String citizenId) {
        JSONArray arr = root.optJSONArray("documents");
        List<JSONObject> list = new ArrayList<>();
        
        if (arr != null) {
            for (int i = 0; i < arr.length(); i++) {
                try {
                    JSONObject obj = arr.getJSONObject(i);
                    if (!obj.has("requestId") && obj.has("attachedRequestId")) {
                        obj.put("requestId", obj.get("attachedRequestId"));
                    }
                    if (obj.optString("citizenId", "").equals(citizenId)) {
                        list.add(obj);
                    }
                } catch (org.json.JSONException e) {
                    System.err.println("Error reading document: " + e.getMessage());
                }
            }
        }
        
        return list;
    }

    public List<JSONObject> reloadAllRequests() {
        try {
            String text = Files.readString(Path.of(DB_PATH));
            JSONObject obj = new JSONObject(text);
            JSONArray arr = obj.optJSONArray("requests");
            List<JSONObject> list = new ArrayList<>();
            
            if (arr != null) {
                for (int i = 0; i < arr.length(); i++) {
                    try {
                        list.add(arr.getJSONObject(i));
                    } catch (org.json.JSONException e) {
                        System.err.println("Error reading request: " + e.getMessage());
                    }
                }
            }
            
            return list;
        } catch (IOException e) {
            System.err.println("Error reloading requests: " + e.getMessage());
            return new ArrayList<>();
        }
    }
    
    public void addToArchive(Archive a) {
        try {
            JSONArray arr = root.optJSONArray("archives");
            if (arr == null) {
                arr = new JSONArray();
                root.put("archives", arr);
            }

            JSONObject obj = new JSONObject();
            obj.put("archiveId", a.getArchiveId());
            obj.put("entityId", a.getEntityId());
            obj.put("type", a.getType().name());
            obj.put("details", a.getDetails());
            obj.put("archivedAt", a.getArchivedAt().toString());
            obj.put("archivedBy", a.getArchivedBy());
            obj. put("reason", a.getReason());

            arr.put(obj);
            save(root);
            System.out.println("✓ Archived: " + a.getArchiveId());
        } catch (org.json.JSONException e) {
            System.err.println("Error archiving: " + e.getMessage());
        }
    }

    public List<JSONObject> getAllArchives() {
        JSONArray arr = root.optJSONArray("archives");
        List<JSONObject> list = new ArrayList<>();
        
        if (arr != null) {
            for (int i = 0; i < arr.length(); i++) {
                try {
                    list.add(arr.getJSONObject(i));
                } catch (org.json.JSONException e) {
                    System.err. println("Error reading archive: " + e.getMessage());
                }
            }
        }
        
        return list;
    }

    public List<JSONObject> getArchivesByType(String type) {
        JSONArray arr = root. optJSONArray("archives");
        List<JSONObject> list = new ArrayList<>();
        
        if (arr != null) {
            for (int i = 0; i < arr.length(); i++) {
                try {
                    JSONObject obj = arr.getJSONObject(i);
                    if (obj.optString("type", "").equals(type)) {
                        list.add(obj);
                    }
                } catch (org.json.JSONException e) {
                    System.err.println("Error reading archive: " + e.getMessage());
                }
            }
        }
        
        return list;
    }

    public void archiveCitizen(String citizenId, String reason) {
        try {
            JSONArray citizens = root.optJSONArray("citizens");
            JSONObject citizenData = null;
            
            if (citizens != null) {
                for (int i = 0; i < citizens.length(); i++) {
                    JSONObject obj = citizens.getJSONObject(i);
                    if (obj. getString("id").equals(citizenId)) {
                        citizenData = new JSONObject(obj. toString());
                        break;
                    }
                }
            }

            if (citizenData == null) return;

            String archiveId = "ARCH-" + citizenId + "-" + System.currentTimeMillis();
            Archive archive = new Archive(archiveId, citizenId, Archive. ArchiveType.DELETED_CITIZEN, 
                                        citizenData.toString(), "ADMIN");
            archive.setReason(reason);
            addToArchive(archive);

            deleteCitizen(citizenId);
            System.out.println("✓ Citizen archived and deleted: " + citizenId);
        } catch (org.json.JSONException e) {
            System.err.println("Error archiving citizen: " + e.getMessage());
        }
    }

    public JSONObject getArchiveById(String archiveId) {
        JSONArray arr = root.optJSONArray("archives");
        
        if (arr != null) {
            for (int i = 0; i < arr.length(); i++) {
                try {
                    JSONObject obj = arr.getJSONObject(i);
                    if (obj. optString("archiveId", "").equals(archiveId)) {
                        return obj;
                    }
                } catch (org.json.JSONException e) {
                    System.err.println("Error reading archive: " + e.getMessage());
                }
            }
        }
        
        return null;
    }

    public JSONObject getArchiveByEntityId(String entityId, String type) {
        JSONArray arr = root.optJSONArray("archives");
        
        if (arr != null) {
            for (int i = 0; i < arr.length(); i++) {
                try {
                    JSONObject obj = arr.getJSONObject(i);
                    if (obj.optString("entityId", "").equals(entityId) && 
                        obj.optString("type", "").equals(type)) {
                        return obj;
                    }
                } catch (org.json.JSONException e) {
                    System.err.println("Error reading archive: " + e.getMessage());
                }
            }
        }
        
        return null;
    }
}