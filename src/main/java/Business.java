import DAO.User;

import org.mindrot.jbcrypt.BCrypt;

import DAO.MongoDB;

public class Business {

    private MongoDB mongoDB;
    private User currentUser;

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
    
    public String[] saveRepository(String repository){
    	String[] result = new String[]{"FL", ""};
    	
    	if(this.mongoDB.saveRepository(repository, this.currentUser.getId())){
    		result[0] = "OK";
    	} else {
    		result[1] = "Oops. Something went wrong";
    	}
    	
    	return result;
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
    	User user = this.mongoDB.getUserByUsername(username);
    	if(user != null){
    		if (BCrypt.checkpw(password, user.getPassword())) {
    			this.currentUser = user;
        		return user;
        	}
    	}
    	return null;
    }
}