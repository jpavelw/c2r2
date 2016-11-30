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

import DAO.User;

public class Route {

    private final String CLIENT_ID = System.getenv().get("CLIENT_ID");
    //private final String MY_TOKEN = System.getenv().get("TOKEN");
    private final Business business = new Business();
    private GitHubCalls gCall;

    public Route(){
        staticFileLocation("/public");
        port(Integer.valueOf(System.getenv("PORT")));

        get("/", (req, res) -> {
        	res.redirect("/repositories");
        	return null;
        });

        /*get("/hello/:name", (req, res) -> {
            return "Hello " + req.params(":name");
        });*/

        // matches "GET /say/hello/to/world"
        // request.splat()[0] is 'hello' and request.splat()[1] 'world'
        //get("/say/*/to/*", (req, res) -> {
            //return "Number of splat parameters: " + req.splat().length;
        //});

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
                        //String error = jsonR.getString("error");
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
            //attr.put("result", jsonR.toString());
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
        		String username = null;
        		String repoName = null;
        		String choice = req.queryParams("choice");
        		Pattern pattern = null;
        		Matcher matcher = null;
        		
        		if(choice.equals("link")){
        			pattern = Pattern.compile("^(https:\\/\\/)?\\b(github\\.com){1}\\b\\/[a-zA-Z0-9]+-?[a-zA-Z0-9]+\\/[a-zA-Z0-9-_]+$");
        			String link = req.queryParams("link");
        			matcher = pattern.matcher(link);
        			if(matcher.matches()){
        				String[] elems = link.split("/");
            			username = elems[elems.length - 2];
            			repoName = elems[elems.length - 1];
            			if(this.validateRepo(username, repoName)){
            				this.gCall = new GitHubCalls(token, username, repoName);
            				String[] result = this.business.saveRepository(username, repoName, gCall.getRepository());
            				if(result[0].equals("OK")){
            					res.redirect("/repositories");
            					return null;
            				} else {
            					attr.put("errormessage", "Repository NOT saved");
            				}
            			} else {
            				attr.put("errormessage", "Invalid rep");
            			}
        			} else {
        				attr.put("errormessage", "It is not a valid GitHub URL");
        			}
        		} else if(choice.equals("detailed")){
        			pattern = Pattern.compile("^[a-zA-Z0-9]+(-?[a-zA-Z0-9]+)*$");
        			username = req.queryParams("username");
        			matcher = pattern.matcher(username);
        			if(matcher.matches()){
        				pattern = Pattern.compile("^[a-zA-Z0-9-_]+$");
        				repoName = req.queryParams("repository");
        				matcher = pattern.matcher(repoName);
        				if(matcher.matches()){
        					if(this.validateRepo(username, repoName)){
        						res.redirect("/repositories");
                				return null;
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
        		//GitHubCalls gCall = new GitHubCalls(token, "alibaba", "weex");
        		//GitHubCalls gCall = new GitHubCalls(token, username, repoName);
        		
    			/*Path directory = Paths.get("CCRS.txt");
    			StringBuilder stringBuilder = new StringBuilder();
    			if (Files.exists(directory)) {
    				try (BufferedReader br = new BufferedReader(new FileReader("CCRS.txt"))) {
                        String line = null;
                        while ((line = br.readLine()) != null) {
                            stringBuilder.append(line);
                        }
    				}
    				
    				File testFile = new File("CCRS.txt");
    				stringBuilder.append(testFile.getAbsolutePath());
    				String imageUrl = "http://www.avajava.com/images/avajavalogo.jpg";
    				String destinationFile = "image.jpg";
    					
    				try {
    					saveImage(imageUrl, destinationFile);
    					File testImageFile = new File("image.jpg");
        				stringBuilder.append(testImageFile.getAbsolutePath());
        				
        				//JavaCodeParser javaCodeParser = new JavaCodeParser(GitHubCalls.releaseDownloadsPath);
        				//javaCodeParser.getMetrics();

    				} catch(Exception e) { }
    				return stringBuilder.toString();
    			}*/
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
        		attr.put("owner", req.params(":owner"));
                attr.put("repository", req.params(":repository"));
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
        
        get("/api/:owner/:repository/contributors", (req, res) -> {
        	if(!this.checkLogin(req, res))
        		return "{\"statusCode\":500, \"message\":\"You do not have permission to make this call\"}";
        	
        	String token = req.session().attribute("token");
        	if(token != null){
        		String owner = req.params(":owner");
            	String repository = req.params(":repository");
            	//if(this.fetchContributors(owner, repository, token)){
            		if(this.business.getRepository(owner, repository) != null){
            			String response = this.business.apiGetContributors();
            			if(response != null)
            				return response;
            		}
            	/*} else {
            		return "{\"statusCode\":500, \"message\":\"Could not save contributors\"}";
            	}*/
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
        		String owner = req.params(":owner");
            	String repository = req.params(":repository");
            	//if(this.fetchReleases(owner, repository, token)){
            		if(this.business.getRepository(owner, repository) != null){
            			String response = this.business.apiGetReleases();
            			if(response != null)
            				return response;
            		}
            	/*} else {
            		return "{\"statusCode\":500, \"message\":\"Could not save releases\"}";
            	}*/
        	}
        	return "{\"statusCode\":500, \"message\":\"Could not get releases\"}";
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
			
		} catch (UnirestException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
    	return false;
    }
    
    private boolean fetchContributors(String owner, String repository, String token){
		this.gCall = new GitHubCalls(token, owner, repository);
		if(this.business.getRepository(owner, repository) != null){
			this.gCall.getReleases();
    		this.gCall.getCommitsInfo();
    		//this.gCall.makeCallsOnContributorCommit(MY_TOKEN);
    		String contribs = this.gCall.getContributors();
			if(this.business.saveContributors(contribs)){
				return true;
			}
		}
    	return false;
    }
    
    private boolean fetchReleases(String owner, String repository, String token){
		//this.gCall = new GitHubCalls(token, owner, repository);
		//if(this.business.getRepository(owner, repository) != null){
			//this.gCall.getReleases();
    		//this.gCall.getCommitsInfo();
    		//this.gCall.makeCallsOnContributorCommit();
    		String releases = this.gCall.getJSONReleases();
    		//String releases = "{\"v0.8.0\":{\"tag_name\":\"v0.8.0\",\"commits_per_release\":1019,\"release_additions\":0,\"release_deletions\":0},\"v0.7.0\":{\"tag_name\":\"v0.7.0\",\"commits_per_release\":823,\"release_additions\":0,\"release_deletions\":0},\"v0.6.1\":{\"tag_name\":\"v0.6.1\",\"commits_per_release\":1225,\"release_additions\":0,\"release_deletions\":0},\"v0.5.0\":{\"tag_name\":\"v0.5.0\",\"commits_per_release\":637,\"release_additions\":143381,\"release_deletions\":437}}";
			if(this.business.saveReleases(releases)){
				return true;
			}
		//}
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
/*{
    "gists_url": "https://api.github.com/users/arung90/gists{/gist_id}",
    "repos_url": "https://api.github.com/users/arung90/repos",
    "following_url": "https://api.github.com/users/arung90/following{/other_user}",
    "bio": null,
    "created_at": "2016-01-04T15:05:30Z",
    "login": "arung90",
    "type": "User",
    "blog": null,
    "subscriptions_url": "https://api.github.com/users/arung90/subscriptions",
    "updated_at": "2016-11-05T07:30:33Z",
    "site_admin": false,
    "company": null,
    "id": 16541961,
    "public_repos": 0,
    "gravatar_id": "",
    "email": null,
    "organizations_url": "https://api.github.com/users/arung90/orgs",
    "hireable": null,
    "starred_url": "https://api.github.com/users/arung90/starred{/owner}{/repo}",
    "followers_url": "https://api.github.com/users/arung90/followers",
    "public_gists": 0,
    "url": "https://api.github.com/users/arung90",
    "received_events_url": "https://api.github.com/users/arung90/received_events",
    "followers": 0,
    "avatar_url": "https://avatars.githubusercontent.com/u/16541961?v=3",
    "events_url": "https://api.github.com/users/arung90/events{/privacy}",
    "html_url": "https://github.com/arung90",
    "following": 0,
    "name": null,
    "location": null
}*/
/*
    mvn deploy:deploy-file -DgroupId=rit.swen772.ccr -DartifactId=ccr.core -Dversion=1.0 -Durl=file:./local-maven-repo/ -DrepositoryId=local-maven-repo -DupdateReleaseInfo=true -Dfile=/Users/jpavelw/Desktop/ccr.core.3.0.jar
*/