package PostgresDB;

import Data.Issue;
import Data.Project;
import Data.User;
import org.apache.log4j.Logger;
import sun.plugin2.message.Message;

import java.sql.*;
import java.util.List;

public class DBConnectionClass {
    private static final Logger log = Logger.getLogger(DBConnectionClass.class);
    //  Database credentials
    private static String DB_URL = "jdbc:postgresql://localhost:5432";
    private static String USER = "postgres";
    private static String PASS = "root";
    private static final String DBName = "simplebugtrackerdb";
    private static Connection connection;
    private static Statement stmt;
    private static PreparedStatement projectStatement;
    private static PreparedStatement usersStatement;
    private static PreparedStatement issuesStatement;
    private static String projectInsertQuery;
    private static String userInsertQuery;
    private static String issueInsertQuery;
    private static Boolean DBExist = false;
    private static String query;
    private static String message;
    public static Boolean isDBConnect = false;


    public DBConnectionClass(String ip, String user, String pass) throws SQLException {

       creatingQueries();

        DB_URL = "jdbc:postgresql://" + ip + ":5432";

        USER = user;
        PASS = pass;

        setConnection(DB_URL);

        if (connection != null) {
            log.info("Connecting to " + DB_URL);
            System.out.println("You successfully connected to database now");
            isDBConnect = true;
            log.info("Selecting databases from Server");
            ResultSet rs = stmt.executeQuery("SELECT * FROM pg_database");


            while (rs.next()) {
                String str = rs.getString(1);

                if (str.equals(DBName)) {
                    DBExist = true;
                }

            }
            if (DBExist == true) {

                DB_URL += "/" + DBName;
                log.info("Connecting to " + DB_URL);
                setConnection(DB_URL);
            } else {
                createDBTables();
            }
        } else {
            message = "Failed to establish database connection.";
            log.warn(message);
            System.out.println(message);
        }


    }

    private void setConnection(String URL) throws SQLException {
        log.info("Connecting to " + URL);

        try {
            Class.forName("org.postgresql.Driver");
        } catch (ClassNotFoundException e) {
            message = "PostgreSQL JDBC Driver is not found. Include it in your library path";
            log.warn(message);
            System.out.println(message);
            return;
        }


        connection = null;
        try {
            connection = DriverManager
                    .getConnection(URL, USER, PASS);
            isDBConnect = true;

        } catch (SQLException e) {
            message = "Connection Failed";
            log.warn(message);
            System.out.println(message);

            return;
        }
        stmt = connection.createStatement();

    }

    //add project to db
    public void addQueryProject(Project project) throws SQLException {
        projectStatement = connection.prepareStatement(projectInsertQuery);

        getInsertQueryForProject(project);
        projectStatement.executeBatch();
        connection.commit();
    }

    //add user to db
    public void addQueryUser(User user, int projectID) throws SQLException {
        usersStatement = connection.prepareStatement(userInsertQuery);

        getInsertQueryForUser(user, projectID);

        usersStatement.executeBatch();
        connection.commit();


    }

    //add issue to db
    public void addQueryIssue(Issue issue, int userID) throws SQLException {
        issuesStatement = connection.prepareStatement(issueInsertQuery);

        getInsertQueryForIssue(issue, userID);

        issuesStatement.executeBatch();
        connection.commit();

    }

    // upload memory transaction
    public void saveDatatoDB(List<Project> projects) throws SQLException {

        projectStatement = connection.prepareStatement(projectInsertQuery);
        usersStatement = connection.prepareStatement(userInsertQuery);

        issuesStatement = connection.prepareStatement(issueInsertQuery);
        connection.setAutoCommit(false);

        for (Project project : projects) {
            getInsertQueryForProject(project);

            List<User> users = project.getProjectUsers();
            for (User user : users) {
                getInsertQueryForUser(user, project.getId());

                List<Issue> issues = user.getIssues();
                for (Issue issue : issues) {
                    getInsertQueryForIssue(issue, user.getId());


                }
            }
        }
        projectStatement.executeBatch();
        usersStatement.executeBatch();
        issuesStatement.executeBatch();
        connection.commit();

        System.out.println("Saved to DB");
    }

    //creating insert statement query for projects
    private void getInsertQueryForProject(Project project) throws SQLException {

        projectStatement.setInt(1, project.getId());
        projectStatement.setString(2, project.getName());
        projectStatement.addBatch();
    }

    //creating insert statement query for users
    private void getInsertQueryForUser(User user, int projectID) throws SQLException {
        usersStatement.setInt(1, user.getId());
        usersStatement.setString(2, user.getFirstName());
        usersStatement.setInt(3, projectID);
        usersStatement.addBatch();
    }

    //creating insert statement query for issues
    private void getInsertQueryForIssue(Issue issue, int userID) throws SQLException {
        issuesStatement.setInt(1, issue.getId());
        issuesStatement.setString(2, issue.getIssue());
        issuesStatement.setInt(3, userID);
        issuesStatement.addBatch();
    }

    // creating tables (projects, users, issues) in simplebugtrackerdb data base
    private void createDBTables() throws SQLException {
        log.info("Creatin tables in " + DBName);
        query = "CREATE DATABASE " + DBName;
        stmt.executeUpdate(query);
        DB_URL += "/" + DBName;
        setConnection(DB_URL);

        query = "BEGIN;\n" +
                "CREATE TABLE issues\n" +
                "                ( \n" +
                "                    Id SERIAL PRIMARY KEY,\n" +
                "                    issue CHARACTER VARYING(30),\n" +
                "                    userID  INTEGER\n" +
                "                );\n" +
                "                \n" +
                "CREATE TABLE users\n" +
                "                (\n" +
                "                    Id SERIAL PRIMARY KEY,\n" +
                "                    FirstName CHARACTER VARYING(30),\n" +
                "                    LastName CHARACTER VARYING(30),\n" +
                "                    projectID  INTEGER\n" +
                "                );\n" +
                "                \n" +
                "CREATE TABLE projects\n" +
                "                (\n" +
                "                    Id SERIAL PRIMARY KEY,\n" +
                "                    name CHARACTER VARYING(30)\n" +
                "                );\n" +
                "COMMIT;";
        stmt.executeUpdate(query);
    }

    // Set String queries in project, user, issue queries
    private void creatingQueries(){
        projectInsertQuery = "INSERT INTO projects (id, name)VALUES (? ,?)" +
                "ON CONFLICT (id) DO NOTHING;";
        userInsertQuery ="INSERT INTO users (id, firstname, lastname, projectid) VALUES (?,?,null,?)" +
                "ON CONFLICT (id) DO NOTHING;";
        issueInsertQuery ="INSERT INTO issues (id, issue, userid) VALUES (?, ?,?)" +
                "ON CONFLICT (id) DO NOTHING;";
    }

    public static void closeConnection() throws SQLException {
        log.info("Close connection");
        connection.close();
        issuesStatement.close();
        usersStatement.close();
        projectStatement.close();
        stmt.close();
    }

}
