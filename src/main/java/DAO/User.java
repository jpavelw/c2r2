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
/*
<dependency>
      <groupId>github.core</groupId>
      <artifactId>githubapi</artifactId>
      <version>3.0</version>
      <scope>system</scope>
      <systemPath>${project.basedir}/lib/github.core.3.0.jar</systemPath>
    </dependency>
    <dependency>
      <groupId>gson</groupId>
      <artifactId>gsonapi</artifactId>
      <version>2.6.2</version>
      <scope>system</scope>
      <systemPath>${project.basedir}/lib/gson-2.6.2.jar</systemPath>
    </dependency>
    <dependency>
      <groupId>ormlite.core</groupId>
      <artifactId>ormlikeapi</artifactId>
      <version>5.0</version>
      <scope>system</scope>
      <systemPath>${project.basedir}/lib/ormlite-core-5.0.jar</systemPath>
    </dependency>
    <dependency>
      <groupId>ormlite-jdbc</groupId>
      <artifactId>jdbc</artifactId>
      <version>5.0</version>
      <scope>system</scope>
      <systemPath>${project.basedir}/lib/ormlite-jdbc-5.0.jar</systemPath>
    </dependency>
    <dependency>
      <groupId>sqlite.jdb</groupId>
      <artifactId>sqlite</artifactId>
      <version>3.14.2.1</version>
      <scope>system</scope>
      <systemPath>${project.basedir}/lib/sqlite-jdbc-3.14.2.1.jar</systemPath>
    </dependency>
*/