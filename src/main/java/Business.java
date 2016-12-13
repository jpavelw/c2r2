import DAO.User;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.bson.Document;
import org.mindrot.jbcrypt.BCrypt;

import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

import DAO.MongoDB;

public class Business {

    private MongoDB mongoDB;
    private Document currentUser;
    private Document currentRepository;
    private final Gson gson = new Gson();
	private final JsonParser parser = new JsonParser();

    public Business() { 
        this.mongoDB = MongoDB.getMongoDB();
        this.currentUser = null;
    }

    public String[] handleTokenAndUser(String [] elements) {
        String[] result = new String[]{"FL", ""};
        if(elements.length == 5){
        	String hasshed = BCrypt.hashpw(elements[3], BCrypt.gensalt());
            User user = new User(elements[0], elements[1], elements[2], hasshed, elements[4]);
            if(this.mongoDB.getUserByUsername(elements[0]) == null){
                if(this.mongoDB.saveUser(user)){
                    result[0] = "OK";
                } else {
                    result[1] = "Oops. Something went wrong";
                }
            } else {
                result[1] = "Oops. Username already exists";
            }
        } else {
            result[1] = "Number of elements must be 5";
        }
        return result;
    }
    
    public String[] saveRepository(String repository){
    	String[] result = new String[]{"FL", ""};
    	Document myDoc = Document.parse(repository);
    	if(this.mongoDB.saveRepository(myDoc, this.currentUser)){
    		result[0] = "OK";
    	} else {
    		result[1] = "Oops. Something went wrong";
    	}
    	
    	return result;
    }
    
    public boolean saveContributors(String contributors){
    	try {
    		JsonArray contributorsArray = this.parser.parse(contributors).getAsJsonArray();
    		if(contributorsArray != null && contributorsArray.size() > 0){
    			List<Document> contributorsList = new ArrayList<Document>();
    			
            	for(JsonElement elem : contributorsArray){
            		Document contributor = Document.parse(this.gson.toJson(elem.getAsJsonObject()));
            		contributor.append("repository", this.currentRepository.get("_id"));
            		contributorsList.add(contributor);
            	}
            	if(this.mongoDB.saveContributors(contributorsList))
            		return true;
    		}
    	} catch(Exception e) { e.printStackTrace(); }
    	return false;
    }
    
    public boolean saveReleases(String releases){
    	try {
    		JsonObject releasesObjects = this.parser.parse(releases).getAsJsonObject();
    		if(releasesObjects != null && releasesObjects.entrySet().size() > 0){
    			List<Document> releasesList = new ArrayList<Document>();
    			SimpleDateFormat parser = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
    			for (Map.Entry<String, JsonElement> entry : releasesObjects.entrySet()) {
    				Document release = Document.parse(this.gson.toJson(entry.getValue().getAsJsonObject()));
    				release.append("created_at", parser.parse(release.getString("created_at")));
    				release.append("repository", this.currentRepository.get("_id"));
    				releasesList.add(release);
    			}
            	if(this.mongoDB.saveReleases(releasesList))
            		return true;
    		}
    	} catch(Exception e) { e.printStackTrace(); }
    	return false;
    }
    
    public JsonArray getRepositories(){
    	List<Document> repositoryList = this.mongoDB.getRepositories(this.currentUser);
    	JsonArray repositoryArray = null;
    	if(repositoryList != null){
    		Document doc = new Document("list", repositoryList);
    	    String json = doc.toJson();
    	    String jList = json.substring(json.indexOf(":")+2, json.length()-1);
    	    repositoryArray = this.parser.parse(jList).getAsJsonArray();
    	}
    	return repositoryArray;
    }
    
    public String apiGetRepositories(){
    	String response = null;
    	
    	try {
    		JsonArray repositoryArray = this.getRepositories();
    		if(repositoryArray != null){
    			response = this.gson.toJson(repositoryArray);
    		}
    	} catch(Exception e) { e.printStackTrace(); }
    	
    	return response;
    }
    
    public boolean checkRepository(String owner, String repository){
    	Document result = this.mongoDB.getRepository(owner, repository);
    	if(result != null)
    		return true;
    	return false;
    }
    
    public boolean checkForReleases(){
    	List<Document> releasesList = this.mongoDB.getReleases(this.currentRepository.getObjectId("_id"));
    	if(releasesList != null && !releasesList.isEmpty())
    		return true;
    	return false;
    }
    
    public String[] attachRepository(String owner, String repository){
    	String[] result = new String[]{"FL", ""};
    	Document repo = this.mongoDB.getRepository(owner, repository);
    	if(repo != null){
    		if(this.mongoDB.attachRepository(repo, this.currentUser)){
    			result[0] = "OK";
    		} else {
    			result[1] = "Could not attach repository";
    		}
    		
    	} else {
    		result[1] = "Could not find repository";
    	}
    	
    	return result;
    }
    
    public JsonObject getRepository(String owner, String repository){
    	this.currentRepository = this.mongoDB.getRepository(owner, repository);
    	if(this.currentRepository != null){
    		JsonObject jObject = this.parser.parse(this.currentRepository.toJson()).getAsJsonObject();
    		return jObject;
    	}
    	return null;
    }
    
    public JsonArray getContributors(){
    	List<Document> contributorsList = this.mongoDB.getContributors(this.currentRepository.getObjectId("_id"));
    	JsonArray contributorsArray = null;
    	if(contributorsList != null){
    		Document doc = new Document("list", contributorsList);
    	    String json = doc.toJson();
    	    String jList = json.substring(json.indexOf(":")+2, json.length()-1);
    	    contributorsArray = this.parser.parse(jList).getAsJsonArray();
    	}
    	return contributorsArray;
    }
    
    public String apiGetContributors(){
    	String response = null;
    	
    	try {
    		JsonArray contributors = this.getContributors();
    		if(contributors != null){
    			JsonObject jResponse = new JsonObject();
            	jResponse.add("login", new JsonArray());
            	jResponse.add("contributions", new JsonArray());
        		for(JsonElement elem : contributors){
        			jResponse.get("login").getAsJsonArray().add(elem.getAsJsonObject().get("contributor_name").getAsString());
        			jResponse.get("contributions").getAsJsonArray().add(elem.getAsJsonObject().get("number_of_contributions").getAsString());
        		}
        		jResponse.addProperty("statusCode", 200);
        		response = this.gson.toJson(jResponse);
    		}
    	} catch(Exception e) { e.printStackTrace(); }
    	
    	return response;
    }
    
    public JsonArray getReleases(){
    	List<Document> releasesList = this.mongoDB.getReleases(this.currentRepository.getObjectId("_id"));
    	JsonArray releasesArray = null;
    	if(releasesList != null){
    		Document doc = new Document("list", releasesList);
    	    String json = doc.toJson();
    	    String jList = json.substring(json.indexOf(":")+2, json.length()-1);
    	    releasesArray = this.parser.parse(jList).getAsJsonArray();
    	}
    	return releasesArray;
    }
    
    public String apiGetReleases(){
    	String response = null;
    	
    	try {
    		JsonArray releases = this.getReleases();
    		if(releases != null){
    			JsonObject jResponse = new JsonObject();
            	jResponse.add("tag_name", new JsonArray());
            	jResponse.add("commits_per_release", new JsonArray());
            	jResponse.add("release_additions", new JsonArray());
            	jResponse.add("release_deletions", new JsonArray());
            	
            	//jResponse.add("release_forks", new JsonArray());
            	//jResponse.add("release_branches", new JsonArray());
            	//jResponse.add("release_stars", new JsonArray());
            	jResponse.add("number_methods", new JsonArray());
            	jResponse.add("avg_number_methods_per_class", new JsonArray());
            	jResponse.add("avg_number_of_fields", new JsonArray());
            	jResponse.add("number_fields", new JsonArray());
            	jResponse.add("number_of_files", new JsonArray());
            	
        		for(JsonElement elem : releases){
        			jResponse.get("tag_name").getAsJsonArray().add(elem.getAsJsonObject().get("tag_name").getAsString());
        			jResponse.get("commits_per_release").getAsJsonArray().add(elem.getAsJsonObject().get("commits_per_release").getAsInt());
        			jResponse.get("release_additions").getAsJsonArray().add(elem.getAsJsonObject().get("loc_release_additions").getAsInt());
        			jResponse.get("release_deletions").getAsJsonArray().add(elem.getAsJsonObject().get("loc_release_deletions").getAsInt());
        			
        			//jResponse.get("release_forks").getAsJsonArray().add(elem.getAsJsonObject().get("release_forks").getAsInt());
        			//jResponse.get("release_branches").getAsJsonArray().add(elem.getAsJsonObject().get("release_branches").getAsInt());
        			//jResponse.get("release_stars").getAsJsonArray().add(elem.getAsJsonObject().get("release_stars").getAsInt());
        			jResponse.get("number_methods").getAsJsonArray().add(elem.getAsJsonObject().get("total_number_methods").getAsInt());
        			jResponse.get("avg_number_methods_per_class").getAsJsonArray().add(elem.getAsJsonObject().get("avg_number_methods_per_class").getAsFloat());
        			jResponse.get("avg_number_of_fields").getAsJsonArray().add(elem.getAsJsonObject().get("avg_number_of_fields").getAsFloat());
        			jResponse.get("number_fields").getAsJsonArray().add(elem.getAsJsonObject().get("total_number_fields").getAsInt());
        			jResponse.get("number_of_files").getAsJsonArray().add(elem.getAsJsonObject().get("number_of_files").getAsInt());
        		}
        		jResponse.addProperty("statusCode", 200);
        		response = this.gson.toJson(jResponse);
    		}
    	} catch(Exception e) { e.printStackTrace(); }
    	
    	return response;
    }

    public User checkLogin(String username, String password) {
    	User user = null;
    	try {
    		Document myDoc = this.mongoDB.getUserByUsername(username);
        	if(myDoc != null){
        		if (BCrypt.checkpw(password, myDoc.getString("password"))) {
        			JsonObject jObject = this.parser.parse(myDoc.toJson()).getAsJsonObject();
        			user = this.gson.fromJson(jObject, User.class);
        			this.currentUser = myDoc;
            		return user;
            	}
    		}
    	} catch (Exception e) { e.printStackTrace(); }
    	
    	return null;
    }
}