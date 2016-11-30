package DAO;

import com.mongodb.MongoClientURI;
import com.mongodb.client.MongoCollection;
import com.mongodb.client.MongoCursor;
import com.mongodb.client.MongoDatabase;
import com.mongodb.BasicDBObject;
import com.mongodb.MongoClient;
import static com.mongodb.client.model.Filters.*;
import static com.mongodb.client.model.Updates.*;

import java.util.ArrayList;
import java.util.List;

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
	private MongoCollection<Document> contributors;

	private final String USER_COLLECTION = "user";
	//private final String OWNER_COLLECTION = "owner";
	private final String REPOSITORY_COLLECTION = "repository";
	private final String CONTRIBUTOR_COLLECTION = "contributor";
	
	private MongoDB(){
		try {
			this.uri  = new MongoClientURI(this.CONNECTION_URL);
			this.client = new MongoClient(this.uri);
			this.database = this.client.getDatabase(uri.getDatabase());
			this.users = this.database.getCollection(this.USER_COLLECTION);
			//this.owners = this.database.getCollection(this.OWNER_COLLECTION);
			this.repositories = this.database.getCollection(this.REPOSITORY_COLLECTION);
			this.contributors = this.database.getCollection(this.CONTRIBUTOR_COLLECTION);
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
	
	@SuppressWarnings("unchecked")
	public boolean saveRepository(Document repository, Document _user){
		
		try {
			this.repositories.insertOne(repository);
			this.users.updateOne(eq("_id", _user.getObjectId("_id")), addToSet("repositories", repository.getObjectId("_id")));
			List<ObjectId> _ids = (List<ObjectId>) _user.get("repositories");
			if(_ids == null){
				_ids = new ArrayList<>();
			}
			_ids.add(repository.getObjectId("_id"));
			_user.append("repositories", _ids);
			return true;
		}  catch (Exception e){ e.printStackTrace(); }
		
		return false;
	}
	
	public Document getUserByUsername(String username){
		Document myDoc = null;
		try {
			myDoc = this.users.find(eq("username", username)).first();
		} catch (Exception e){ e.printStackTrace(); }
		
		return myDoc;
	}
	
	@SuppressWarnings("unchecked")
	public List<Document> getRepositories(Document _user){
		List<Document> repos = null;
		try {
			List<ObjectId> _ids = (List<ObjectId>) _user.get("repositories");
			BasicDBObject filter = new BasicDBObject("_id", new BasicDBObject("$in", _ids));
			repos = this.repositories.find(filter).into(new ArrayList<>());
		} catch (Exception e) { e.printStackTrace(); }
		
		/*BasicDBObject filter = new BasicDBObject("_id", new BasicDBObject("$in", _user.get("repositories")));
		MongoCursor<Document> cursor = this.repositories.find(filter).iterator();
		
		try {
			if(cursor.hasNext()){
				repos = new ArrayList<>();
			}
			
		    while (cursor.hasNext()) {
		    	repos.add(cursor.next());
		    }
		} finally { cursor.close(); }*/
		
		return repos;
	}
	
	public List<Document> getContributors(ObjectId _id){
		List<Document> contribs = null;
		
		MongoCursor<Document> cursor = this.contributors.find(eq("repository", _id)).iterator();
		
		try {
			if(cursor.hasNext()){
				contribs = new ArrayList<>();
			}
			
		    while (cursor.hasNext()) {
		    	contribs.add(cursor.next());
		    }
		} finally { cursor.close(); }
		
		return contribs;
	}
	
	public Document getRepository(String owner, String repoName){
		Document repo = null;
		
		try {
			BasicDBObject filter = new BasicDBObject();
			filter.append("repository.name", repoName);
			filter.append("repository.owner.login", owner);
			repo = this.repositories.find(filter).first();
		} catch (Exception e){ e.printStackTrace(); }
		
		return repo;
	}
	
	public boolean saveContributors(List<Document> contributors){
    	try {
    		this.contributors.insertMany(contributors);
    		return true;
    	} catch(Exception e) { e.printStackTrace(); }
    	
    	return false;
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
