package DAO;

import com.mongodb.MongoClientURI;
import com.mongodb.client.MongoCollection;
import com.mongodb.client.MongoDatabase;

import com.google.gson.Gson;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.mongodb.MongoClient;
import static com.mongodb.client.model.Filters.*;

//import java.util.List;

import org.bson.Document;
import org.bson.types.ObjectId;

public class MongoDB {
	private final String DB_USER = System.getenv().get("DB_USER");
	private final String DB_PASSWORD = System.getenv().get("DB_PASSWORD");
	private final String CONNECTION_URL = "mongodb://" + this.DB_USER + ":" + this.DB_PASSWORD + "@ds157677.mlab.com:57677/ccr";
	private MongoClientURI uri;
    private MongoClient client;
    private MongoDatabase database;
	private static MongoDB mongoDB;
	private MongoCollection<Document> users;
	//private MongoCollection<Document> owners;
	private MongoCollection<Document> repositories;

	private final String USER_COLLECTION = "user";
	//private final String OWNER_COLLECTION = "owner";
	private final String REPOSITORY_COLLECTION = "repository";
	private final Gson gson = new Gson();
	private final JsonParser parser = new JsonParser();
	
	private MongoDB(){
		try {
			this.uri  = new MongoClientURI(this.CONNECTION_URL);
			this.client = new MongoClient(this.uri);
			this.database = this.client.getDatabase(uri.getDatabase());
			this.users = this.database.getCollection(this.USER_COLLECTION);
			//this.owners = this.database.getCollection(this.OWNER_COLLECTION);
			this.repositories = this.database.getCollection(this.REPOSITORY_COLLECTION);
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
	
	public static MongoDB getMongoDB(){
		if(mongoDB == null){
			synchronized (MongoDB.class) {
				if(mongoDB == null)
					mongoDB = new MongoDB();
			}
		}
		return mongoDB;
	}

	public boolean saveUser(User user){
		try {
			Document newUser = new Document("username", user.getUsername())
		               .append("password", user.getPassword())
		               .append("email", user.getEmail())
		               .append("token", user.getToken())
		               .append("name", user.getName());
			
			this.users.insertOne(newUser);
		} catch(Exception e){
			e.printStackTrace();
			return false;
		}
		return true;
	}
	
	public boolean saveRepository(String repository, ObjectId _id){
		
		try {
			Document myDoc = Document.parse(repository);
			myDoc.append("user", _id);
			this.repositories.insertOne(myDoc);
			return true;
		}  catch (Exception e){ e.printStackTrace(); }
		
		return false;
	}
	
	public User getUserByUsername(String username){
		User user = null;
			
		try {
			Document myDoc = this.users.find(eq("username", username)).first();
			
			if(myDoc != null){
				JsonObject jObject = this.parser.parse(myDoc.toJson()).getAsJsonObject();
				user = this.gson.fromJson(jObject, User.class);
			}
		} catch (Exception e){ e.printStackTrace(); }
		
		return user;
	}
	
	public boolean closeConnection(){
		synchronized (MongoDB.class) {
			try {
				this.client.close();
				return true;
			} catch (Exception e) {
				e.printStackTrace();
				return false;
			}
		}
	}
}
/*
 * Document doc = Document.parse("{ \"list\":"+json+"}");
    Object list = doc.get("list");
    if(list instanceof List<?>) {
        return (List<Document>) doc.get("list");
    }
    */
