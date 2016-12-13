# C2R2

Code changes analyzer for Java GitHub projects

## Installation

### 1. Install Java Development Kit (JDK) 8
Go to the following link and follow the instructions on how to download and install JDK 8. [JDK 8](http://www.oracle.com/technetwork/java/javase/downloads/jdk8-downloads-2133151.html).

### 2. Install Maven.
Go to the following link and follow the instructions on how to download and install Maven. [Maven](http://maven.apache.org).

### 3. Install Heroku Command Line Interface (CLI)
Go to the following link and follow the instructions on how to download and install Heroku CLI. [Heroku Command Line](https://devcenter.heroku.com/articles/heroku-command-line).

### 4. Install MongoDB
Go to the following link and follow the instructions on how to download and install MongoDB Server. [MongoDB Server](https://www.mongodb.com/download-center?jmp=nav#community).

For our project, we used mLab. mLab is a fully managed cloud database service which provisions MongoDB databases. [mLab](https://mlab.com).

### 5. Download the web project
Go to the following link and download the project. [Project Link](https://github.com/jpavelw/c2r2).

You can optionally clone the project if you have GitHub CLI installed on your machine.

Login through the terminal using the following command and using your Heroku credentials (email and password used to login on Heroku):

```
$ heroku login
```

### 6. Deploy the project
After creating the account, create a new app and deploy it with the project you downloaded from GitHub. You can do this from the Heroku website through the dashboard right after you log in. After the application is created, select it (click on it) and click on the Deploy tab. Follow those steps.

### 7. Create a GitHib application
Go to your GitHub settings on the GitHub website and click on OAuth Applications. Register a new application and add the callback and homepage URL. It shall be something like *[Homepage URL]/github/callback*.

### 8. Configure environment variables
In the project base directory, create a file called .env (no name, only extension env). In this file is where we set the environment variables. We have three so far:

* __CLIENT_ID=[client_id]__: this is the client id you get from GitHub after you register the app.

* __SECRET=[secret]__: this is the client secret you get from GitHub after you register the app.

* __CONNECTION_URL=[MongoDB connection URL]__: this is the connection URL to the MongoDB Server.

These same variables must be configured in Heroku. After selecting your application in the Heroku website, click on Settings and then on Reveal Config Vars. There you can add the same environment variables you added on the .env file.

The variables that are in the .env file, are the variables that will be read to run the application locally on your machine. Whereas the configuration variables you configure through the Heroku website, are the ones that will be read when you deploy and run the application with Heroku.

### 9. How to run it
Open the terminal and go to the base project directory. To compile the project run locally:

```
$ mvn clean install
```

To run the project locally run:

```
$ heroku local web
```

Once a project is deployed with Heroku, it’s automatically available and up and running.

### To make changes to the API
We created an API that processes the information gotten from the GitHub Java API and from GitHub. We included this API in our project and it is available for any changes and improvements. Use the following link to download the project from GitHub: [Project URL](https://github.com/LumbardhAgaj/Code-Changes-Reporter).

## License

The underlying source code used to format and display that content is licensed under the [MIT license](https://github.com/jpavelw/c2r2/blob/master/LICENSE)