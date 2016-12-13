package DAO;

public class User {
	private String username;
	private String password;
    private String email;
    private String token;
    private String name;
	
	public User() { }
	
	public User(String username, String name, String email, String password, String token) {
		this.username = username;
		this.password = password;
        this.email = email;
        this.token = token;
        this.name = name;
	}

    public String getUsername() { return username; }
    public void setUsername(String username) { this.username = username; }

    public String getPassword() { return password; }
	public void setPassword(String password) { this.password = password; }

    public String getEmail() { return email; }
	public void setEmail(String email) { this.email = email; }

	public String getName() { return name; }
    public void setName(String name) { this.name = name; }

    public String getToken() { return token; }
	public void setToken(String token) { this.token = token; }

    @Override
    public String toString(){
        return this.username + "-" + this.name + "-" + this.email + "-" + this.password + "-" + this.token;
    }
}