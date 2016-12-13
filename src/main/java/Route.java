import static spark.Spark.*;
import spark.ModelAndView;
import spark.Request;
import spark.Response;
import spark.template.freemarker.FreeMarkerEngine;

import java.util.HashMap;
import java.util.Map;
import java.util.Random;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import com.google.gson.JsonArray;
import com.mashape.unirest.http.HttpResponse;
import com.mashape.unirest.http.Unirest;
import com.mashape.unirest.http.exceptions.UnirestException;

import rit.swen772.ccr.GitHubCalls;

import com.mashape.unirest.http.JsonNode;

import org.json.JSONObject;
import org.json.JSONException;

import com.google.gson.JsonObject;

import DAO.User;

public class Route {

    private final String CLIENT_ID = System.getenv().get("CLIENT_ID");
    private final Business business = new Business();
    private GitHubCalls gCall;

    public Route(){
        staticFileLocation("/public");
        port(Integer.valueOf(System.getenv("PORT")));

        get("/", (req, res) -> {
        	res.redirect("/repositories");
        	return null;
        });

        get("/github/togithub", (req, res) -> {
            //option to generate random state for security purposes
            char[] chars = "ABCDEFGHIJLKMNOPQRSTUVWXYZ012345678-_".toCharArray();
            StringBuilder ramdy = new StringBuilder();
            Random random =  new Random();
            for(int i = 0; i < 10; i++){
                ramdy.append(chars[random.nextInt(chars.length)]);
            }

            req.session().attribute("ramdy", ramdy.toString());
            String url = String.format("https://github.com/login/oauth/authorize?client_id=%s&state=%s", CLIENT_ID, ramdy.toString());

            res.redirect(url);
            return null;
        });

        get("/github/callback", (req, res) -> {

            String code = req.queryParams("code");
            String state = req.queryParams("state");
            String ramdy = req.session().attribute("ramdy");
            req.session().removeAttribute("ramdy");

            if(state.equals(ramdy)){
                String secret = System.getenv().get("SECRET");

                HttpResponse<JsonNode> jsonRes = Unirest.post("https://github.com/login/oauth/access_token")
                    .header("Accept", "application/json")
                    .field("client_id", CLIENT_ID)
                    .field("client_secret", secret)
                    .field("code", code)
                    .asJson();
                
                JSONObject jsonR = jsonRes.getBody().getObject();
                try {
                    String token = jsonR.getString("access_token");
                    jsonRes = Unirest.get("https://api.github.com/user")
                        .header("Authorization", "token " + token)
                        .asJson();
                    jsonR = jsonRes.getBody().getObject();

                    req.session().attribute("token", token);
                    String params = "?";
                    try { params += "username=" + jsonR.getString("login") + "&"; } catch(JSONException e) { params += "username=&"; }
                    try { params += "email=" + jsonR.getString("email") + "&"; } catch(JSONException e) { params += "email=&"; }
                    try { params += "name=" + jsonR.getString("name"); } catch(JSONException e) { params += "name="; }
                    res.redirect("/user/signup" + params);
                    return null;
                } catch(JSONException e){
                    try {
                        e.printStackTrace();
                        String message = jsonR.getString("error_description");
                        res.redirect("/github/failed?message="+message);
                    } catch(JSONException ex) { res.redirect("/github/failed?message=Something went terribly wrong"); }
                }
            }
            
            return null;
        });

        get("/user/signup", (req, res) -> {
            Map<String, Object> attr = new HashMap<>();
            attr.put("username", req.queryParams("username"));
            attr.put("email", req.queryParams("email"));
            attr.put("name", req.queryParams("name"));
            attr.put("hidelogout", true);
            return new ModelAndView(attr, "sign-up.ftl");
        }, new FreeMarkerEngine());

        post("/user/signup", (req, res) -> {
            String password = req.queryParams("password");
            String confirm = req.queryParams("confirm");
            String errormessage = null;
            if(password.equals(confirm)){
                String [] elements = new String [5];
                elements[0] = req.queryParams("username");
                elements[1] = req.queryParams("name");
                elements[2] = req.queryParams("email");
                elements[3] = password;
                elements[4] = req.session().attribute("token");
                String[] result = business.handleTokenAndUser(elements);
                if(result[0].equals("OK")){
                    res.redirect("/user/login");
                    return null;
                } else {
                	req.session().removeAttribute("token");
                    errormessage = result[1];
                }
            } else {
                errormessage = "Passwords do not match";
            }
            Map<String, Object> attr = new HashMap<>();
            attr.put("username", req.queryParams("username"));
            attr.put("email", req.queryParams("email"));
            attr.put("name", req.queryParams("name"));
            attr.put("errormessage", errormessage);
            attr.put("hidelogout", true);
            return new ModelAndView(attr, "sign-up.ftl");
        }, new FreeMarkerEngine());

        get("/github/failed", (req, res) -> {
            String message = req.queryParams("message");
            Map<String, Object> attr = new HashMap<>();
            attr.put("message", message);
            return new ModelAndView(attr, "gh-fail-auth.ftl");
        }, new FreeMarkerEngine());

        get("/github/succ", (req, res) -> {
            Map<String, Object> attr = new HashMap<>();
            attr.put("result", "all good");
            return new ModelAndView(attr, "gh-succ-auth.ftl");
        }, new FreeMarkerEngine());
        
        get("/repositories", (req, res) -> {
        	if(!this.checkLogin(req, res))
        		return null;
        	Map<String, Object> attr = new HashMap<>();
        	attr.put("hidelogout", true);
        	String token = req.session().attribute("token");
        	if(token != null){
        		JsonArray repositories = this.business.getRepositories();
        		if(repositories != null){
        			if(repositories.size() == 0){
        				res.redirect("/repository/add");
        				return null;
        			}
        			attr.put("repositories", repositories.iterator());
        		} else {
        			attr.put("errormessage", "Could not get repositories");
        		}
        	} else {
        		attr.put("errormessage", "Could not get token");
        	}
        	
        	return new ModelAndView(attr, "repositories.ftl");
        }, new FreeMarkerEngine());
        
        get("/repository/add", (req, res) -> {
        	if(!this.checkLogin(req, res))
        		return null;
        	Map<String, Object> attr = new HashMap<>();
        	attr.put("hidelogout", true);
        	return new ModelAndView(attr, "repo-info.ftl");
        }, new FreeMarkerEngine());
        
        post("/repository/add", (req, res) -> {
        	if(!this.checkLogin(req, res))
        		return null;
        	String token = req.session().attribute("token");
        	Map<String, Object> attr = new HashMap<>();
        	attr.put("hidelogout", true);
        	if(token != null){
        		String owner = null;
        		String repository = null;
        		String choice = req.queryParams("choice");
        		Pattern pattern = null;
        		Matcher matcher = null;
        		
        		if(choice.equals("link")){
        			pattern = Pattern.compile("^(https:\\/\\/)?\\b(github\\.com){1}\\b\\/[a-zA-Z0-9]+-?[a-zA-Z0-9]+\\/[a-zA-Z0-9-_]+$");
        			String link = req.queryParams("link");
        			matcher = pattern.matcher(link);
        			if(matcher.matches()){
        				String[] elems = link.split("/");
        				owner = elems[elems.length - 2];
            			repository = elems[elems.length - 1];
            			if(this.validateRepo(owner, repository)){
            				String[] result;
            				if(this.business.checkRepository(owner, repository)){
            					result = this.business.attachRepository(owner, repository);
            					if(result[0].equals("OK")){
                					res.redirect("/repositories");
                					return null;
                				} else {
                					attr.put("errormessage", "Repository NOT saved");
                				}
            				} else {
            					this.gCall = new GitHubCalls(token, owner, repository);
                				result = this.business.saveRepository(gCall.getRepository());
                				
                				if(result[0].equals("OK")){
                					res.redirect("/repositories");
                					return null;
                				} else {
                					attr.put("errormessage", "Repository NOT saved");
                				}
            				}
            			} else {
            				attr.put("errormessage", "Invalid rep");
            			}
        			} else {
        				attr.put("errormessage", "It is not a valid GitHub URL");
        			}
        		} else if(choice.equals("detailed")){
        			pattern = Pattern.compile("^[a-zA-Z0-9]+(-?[a-zA-Z0-9]+)*$");
        			owner = req.queryParams("username");
        			matcher = pattern.matcher(owner);
        			if(matcher.matches()){
        				pattern = Pattern.compile("^[a-zA-Z0-9-_]+$");
        				repository = req.queryParams("repository");
        				matcher = pattern.matcher(repository);
        				if(matcher.matches()){
        					if(this.validateRepo(owner, repository)){
        						String[] result;
        						if(this.business.checkRepository(owner, repository)){
        							result = this.business.attachRepository(owner, repository);
                					if(result[0].equals("OK")){
                    					res.redirect("/repositories");
                    					return null;
                    				} else {
                    					attr.put("errormessage", "Repository NOT saved");
                    				}
                				} else {
                					this.gCall = new GitHubCalls(token, owner, repository);
                    				result = this.business.saveRepository(gCall.getRepository());
                    				
                    				if(result[0].equals("OK")){
                    					res.redirect("/repositories");
                    					return null;
                    				} else {
                    					attr.put("errormessage", "Repository NOT saved");
                    				}
                				}
                			} else {
                				attr.put("errormessage", "Invalid repo");
                			}
        				} else {
            				attr.put("errormessage", "It is not a valid GitHub repository name");
            			}
        			} else {
        				attr.put("errormessage", "It is not a valid GitHub username");
        			}
        		}
        	} else {
        		attr.put("errormessage", "Could not get token");
        	}
        	return new ModelAndView(attr, "repo-info.ftl");
        }, new FreeMarkerEngine());
        
        get("/repository/:owner/:repository", (req, res) -> {
        	if(!this.checkLogin(req, res))
        		return null;
        	String token = req.session().attribute("token");
        	Map<String, Object> attr = new HashMap<>();
        	if(token != null){
        		String owner = req.params(":owner");
            	String repository = req.params(":repository");
            	JsonObject repo = this.business.getRepository(owner, repository);
            	if(repo != null){
            		try {
            			attr.put("date", repo.get("repository").getAsJsonObject().get("createdAt").getAsString());
                		attr.put("description", repo.get("repository").getAsJsonObject().get("description").getAsString());
                		attr.put("url", repo.get("repository").getAsJsonObject().get("htmlUrl").getAsString());
            		} catch(Exception e) { e.printStackTrace(); }
            	}
        		attr.put("owner", owner);
                attr.put("repository", repository);
        	} else {
        		attr.put("errormessage", "Could not get token");
        	}
            attr.put("hidelogout", true);
        	return new ModelAndView(attr, "repository-info.ftl");
        }, new FreeMarkerEngine());
        
        get("/repository/:owner/:repository/contributors", (req, res) -> {
        	if(!this.checkLogin(req, res))
        		return null;
        	String token = req.session().attribute("token");
        	Map<String, Object> attr = new HashMap<>();
            attr.put("hidelogout", true);
        	if(token != null){
        		attr.put("owner", req.params(":owner"));
                attr.put("repository", req.params(":repository"));
        	} else {
        		attr.put("errormessage", "Could not get token");
        		attr.put("detailedmessage", "Please verify you have a valid token to make github calls");
        	}
        	return new ModelAndView(attr, "contributors.ftl");
        }, new FreeMarkerEngine());
        
        get("/repository/:owner/:repository/sourcecode", (req, res) -> {
        	if(!this.checkLogin(req, res))
        		return null;
        	String token = req.session().attribute("token");
        	Map<String, Object> attr = new HashMap<>();
            attr.put("hidelogout", true);
        	if(token != null){
        		attr.put("owner", req.params(":owner"));
                attr.put("repository", req.params(":repository"));
        	} else {
        		attr.put("errormessage", "Could not get token");
        		attr.put("detailedmessage", "Please verify you have a valid token to make github calls");
        	}
        	return new ModelAndView(attr, "source-code.ftl");
        }, new FreeMarkerEngine());
        
        get("/api/:owner/:repository/metrics", (req, res) -> {
        	if(!this.checkLogin(req, res))
        		return "{\"statusCode\":500, \"message\":\"You do not have permission to make this call\"}";
        	String token = req.session().attribute("token");
        	if(token != null){
        		String owner = req.params(":owner");
            	String repository = req.params(":repository");
            	if(this.business.checkForReleases()){
            		return "{\"statusCode\":200, \"message\":\"OK\"}";
            	} else {
            		if(this.fetchInfo(owner, repository, token)){
                		return "{\"statusCode\":200, \"message\":\"OK\"}";
                    }
            	}
        	}
        	return "{\"statusCode\":500, \"message\":\"Could not get metrics\"}";
        });
        
        get("/api/:owner/:repository/contributors", (req, res) -> {
        	if(!this.checkLogin(req, res))
        		return "{\"statusCode\":500, \"message\":\"You do not have permission to make this call\"}";
        	
        	String token = req.session().attribute("token");
        	if(token != null){
            	String response = this.business.apiGetContributors();
    			if(response != null)
    				return response;
        	}
        	return "{\"statusCode\":500, \"message\":\"Could not get contributors\"}";
        });
        
        get("/repository/:owner/:repository/releases", (req, res) -> {
        	if(!this.checkLogin(req, res))
        		return null;
        	String token = req.session().attribute("token");
        	Map<String, Object> attr = new HashMap<>();
            attr.put("hidelogout", true);
            if(token != null){
        		attr.put("owner", req.params(":owner"));
                attr.put("repository", req.params(":repository"));
        	} else {
        		attr.put("errormessage", "Could not get token");
        		attr.put("detailedmessage", "Please verify you have a valid token to make github calls");
        	}
            return new ModelAndView(attr, "releases.ftl");
        }, new FreeMarkerEngine());
        
        get("/api/:owner/:repository/releases", (req, res) -> {
        	if(!this.checkLogin(req, res))
        		return "{\"statusCode\":500, \"message\":\"You do not have permission to make this call\"}";
        	String token = req.session().attribute("token");
        	if(token != null){
        		String response = this.business.apiGetReleases();
    			if(response != null)
    				return response;
        	}
        	return "{\"statusCode\":500, \"message\":\"Could not get metrics\"}";
        });
        
        get("/user/login", (req, res) -> {
        	if(req.session().attribute("username") != null && req.session().attribute("token") != null){
        		res.redirect("/");
        		return null;
        	}
        	Map<String, Object> attr = new HashMap<>();
            attr.put("hidemenu", true);
            attr.put("hidesession", true);
        	return new ModelAndView(attr, "login.ftl");
        }, new FreeMarkerEngine());
        
        post("/user/login", (req, res) -> {
        	if(req.session().attribute("username") != null && req.session().attribute("token") != null){
        		return null;
        	}
        	String username = req.queryParams("username");
            String password = req.queryParams("password");
            
            User user = this.business.checkLogin(username, password);
            
            if (user != null) {
                req.session().attribute("username", user.getUsername());
                req.session().attribute("token", user.getToken());
            	res.redirect("/repositories");
                return null;
            }
            Map<String, Object> attr = new HashMap<>();
            attr.put("errormessage", "Username and password do not match");
            return new ModelAndView(attr, "login.ftl");
        }, new FreeMarkerEngine());

        get("/user/logout", (req, res) -> {
            req.session().invalidate();
            res.redirect("/user/login");
        	return null;
        });
    }
    
    @SuppressWarnings("unused")
	private boolean validateRepo(String username, String repository){
    	String url = "https://api.github.com/repos/" + username + "/" + repository;
    	HttpResponse<JsonNode> jsonRes;
		try {
			jsonRes = Unirest.get(url).asJson();
			JSONObject jsonR = jsonRes.getBody().getObject();
			try {
				String data = null;
				data = String.valueOf(jsonR.getInt("id"));
				data = jsonR.getString("name");
				data = jsonR.getString("full_name");
				data = String.valueOf(jsonR.getJSONObject("owner").getInt("id"));
				return true;
			} catch(JSONException e){
				e.printStackTrace();
				System.out.println("Could not get something");
			}
			
		} catch (UnirestException e) { e.printStackTrace(); }
		
    	return false;
    }
    
    private boolean fetchInfo(String owner, String repository, String token){
    	this.gCall = new GitHubCalls(token, owner, repository);
		
		String releases = this.gCall.getJSONReleases(token);
		if(releases != null && !releases.isEmpty()){
			if(!this.business.saveReleases(releases)){
				System.out.println("Could not save releases");
			}
			String contribs = this.gCall.getContributors();
			if(!this.business.saveContributors(contribs)){
				System.out.println("Could not save contributors");
			}
			return true;
		}
    	return false;
    }
    
    private boolean checkLogin(Request req, Response res){
    	if(req.session().attribute("username") == null || req.session().attribute("token") == null){
    		res.redirect("/user/login");
    		return false;
    	}
    	return true;
    }
}
/*
    mvn deploy:deploy-file -DgroupId=rit.swen772.ccr -DartifactId=core -Dversion=1.2 -Durl=file:./local-maven-repo/ -DrepositoryId=local-maven-repo -DupdateReleaseInfo=true -Dfile=/Users/jpavelw/Desktop/core.1.2.jar
*/