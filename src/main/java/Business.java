import DAO.User;

import java.util.ArrayList;
import java.util.List;

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
        	//if (BCrypt.checkpw(candidate, hashed))
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
    
    public String[] saveRepository(String username, String repoName, String repository){
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
    
    /*public String getTokenByUserName(String username){
    	User user = this.mongoDB.getUserByUsername(username);
    	if(user != null){
    		this.currentUser = user;
    		return user.getToken();
    	}
    	return null;
    }
    
    /*public boolean checkLogin(String username, String password) {
    	User user = this.mongoDB.getUserByUsername(username);
    	if(user != null){
    		if (BCrypt.checkpw(password, user.getPassword())) {
        		return true;
        	}
    	}
    	return false;
    }*/

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