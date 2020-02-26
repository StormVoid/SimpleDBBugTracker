import Data.Issue;
import Data.Project;
import Data.User;
import PostgresDB.DBConnectionClass;
import org.apache.log4j.Logger;

import java.io.*;
import java.sql.SQLException;
import java.util.*;

public class ConsoleApp implements Serializable {
    private static final Logger log = Logger.getLogger(ConsoleApp.class);
    private String message;
    private int currentProjectIndex;
    private int currentUserIndex;
    private int currentPorition;
    private Project currentProject;
    private User currentUser;

    private String command;
    private Scanner commandScanner;
    private Scanner selectScanner;

    private List<Project> projects;
    private List<User> usersList;
    private List<Project> changesForDBSave; // way to not send extra queries

    private FileOutputStream fileOutputStream;
    private ObjectOutputStream objectOutputStream;
    private FileInputStream fileInputStream;
    private ObjectInputStream objectInputStream;

    int[] countsArray;
    private int projectIDCount;
    private int userIDCount;
    private int issueIDCount;

    private DBConnectionClass dbConnectionClass;


    public ConsoleApp() throws IOException, SQLException {
        System.out.println("Welcome to the Simple Bug Tracker!");
        System.out.println("Write '-help' to see the list of commands " +
                "\n or '-guide' to view user's manual");
        currentProjectIndex = -1;
        currentUserIndex = -1;
        countsArray = new int[3];
        projectIDCount = 1;
        userIDCount = 1;
        issueIDCount = 1;

        commandScanner = new Scanner(System.in);
        projects = new ArrayList<>();


        try {

            readDataFile();
            readCountsFile();
        } catch (IOException e) {
            try {
                createDataFile();
                createCountsFile();

            } catch (IOException e1) {
                message = "Can't to create Data file";
                log.error(message + ": ", e);
                System.out.println(message);
            }
        } catch (ClassNotFoundException e) {
            log.error("ClassNotFoundException: ", e);
            e.printStackTrace();
        }

        if (projects.isEmpty()) {
            fillArrays();
        }


        while (true) {
            command = commandScanner.nextLine();

            switch (command.trim()) {
                case ("-help"):
                    handleHelpCommand();
                    break;

                case ("-guide"):
                    handleGuideCommand();
                    break;

                case ("-add project"):
                    handleAddProjectCommand();
                    break;

                case ("-list all"):
                    handleListAllCommand();
                    break;

                case ("-list projects"):
                    handleListProjectCommand();
                    break;


                case ("-list all users"):
                    handleListAllUsersCommand();
                    break;

                case ("-list current project users"):
                    handleListCurProjectUsersCommand();
                    break;


                case ("-list all issues"):
                    handleListAllIssuesCommand();
                    break;

                case ("-user report"):
                    handleUserReportCommand();
                    break;

                case ("-select"):
                    handleSelectCommand();
                    break;

                case ("-back"):
                    handleBackCommand();
                    break;

                case ("-add user"):
                    handleAddUserCommand();
                    break;

                case ("-add issue"):
                    handleAddIssueCommand();
                    break;

                case ("-exit"):

                    break;

                case ("-save"):
                    handleSaveCommand();
                    break;
                case ("-load"):
                    handleLoadCommand();
                    break;

                case ("-connect db"):
                    handleConnectDBCommand();
                    break;
                case ("-save db"):
                    handleSaveDBCommand();
                    break;

                case ("-disconnect"):
                    handleDisconnectCommand();

                    break;

                default:
                    System.out.println("Wrong Command");
            }
        }

    }

    //command -list all method
    private void handleListAllCommand() {
        log.info("Command: " + command);
        System.out.println(projects);
    }

    //command -disconnect method closing all connections
    private void handleDisconnectCommand() throws SQLException {
        log.info("Command: " + command);
        DBConnectionClass.isDBConnect = false;
        DBConnectionClass.closeConnection();
        System.out.println("Disconnected");
    }

    // command -exit method closing connection
    private void handleExitCommand() throws SQLException {
        log.info("Command: " + command);
        if (DBConnectionClass.isDBConnect == true) {
            DBConnectionClass.closeConnection();
        }
        System.exit(0);
    }

    // command -help method
    private void handleHelpCommand() {
        log.info("Command: " + command);

        System.out.println("-list all                   - view projects hierarchy");
        System.out.println("-list projects              - view projects");
        System.out.println("-list all users             - view all users");
        System.out.println("-list current project users - view all users from current project");
        System.out.println("-list all issues            - view all issues");
        System.out.println("-user report                - generate the report of all issues of selected user");
        System.out.println();
        System.out.println("-add project                - add new project into the app");
        System.out.println("-add user                   - add new user in selected project");
        System.out.println("-add issue                  - add new issue in selected user");
        System.out.println();
        System.out.println("-save                       - to save Data in memory");
        System.out.println("-load                       - to load Data from memory");
        System.out.println("-select                     - select project or user from selected project");
        System.out.println("-back                       - cancel selected object");
        System.out.println();
        System.out.println("-exit                       - exit program");

        System.out.println("______________________________________________________________________\n" +
                "Data Base commands:");
        System.out.println("-connect db                 - for connection to db");
        System.out.println("-save db                    - for upload memory data to db");
        System.out.println("-disconnect                 - for switch to memory mode");
    }

    // command -guide method
    private void handleGuideCommand() {
        log.info("Command: " + command);
        System.out.println("*Use commands for navigation in the application." +
                "\n*For add object first u need enter the command, press 'enter' on the keyboard and after enter the objectname" +
                "\n*U can list prefer objects or list all, use relevant commands." +
                "\n*To look list of commands, write '-help' in to the console." +
                "\n*For add new user first need to select project and user for add new issue respectively." +
                "\n*Use '-back' to cancel selected object" +
                "\n*Application supports duplicates and uppercase." +
                "\n*For connect to db using default postgreSQL port '5432'" +
                "\n*This version is designed for working with db according single user mode\n" +
                "\n*After connect to db all add queries performed into db and memory data bases" +
                "\n*Use '-disconnect' comand for switch memory mode");
    }

    // command -add project method
    private void handleAddProjectCommand() throws IOException, SQLException {
        log.info("Command: " + command);
        System.out.println("Enter the name of the new project:");
        command = commandScanner.nextLine();
        log.info("Add new project: " + command);

        projects.add(new Project(projectIDCount, command));


        if (dbConnectionClass.isDBConnect == true) {
            log.info("Add new project in DB: " + command + " id = " + projectIDCount);
            Project project = new Project(projectIDCount, command);
            dbConnectionClass.addQueryProject(project);


            createCountsFile();
            createDataFile();

        }
        projectIDCount++;
        command = null;
        System.out.println("Project added");
    }

    // command -list projects method
    private void handleListProjectCommand() {
        log.info("Command: " + command);
        currentPorition = 0;
        for (Project project : projects) {
            System.out.println(currentPorition + " " + project.getName());
            currentPorition++;
        }
    }

    // command -list all users method
    private void handleListAllUsersCommand() {
        log.info("Command: " + command);
        usersList = new ArrayList<>();
        Set<String> usersSet = new HashSet<>();

        for (Project project : projects) {
            usersList.addAll(project.getProjectUsers());
        }

        //no duplicates
        for (User user : usersList) {
            usersSet.add(user.getFirstName());
        }

        Iterator<String> iterator = usersSet.iterator();
        while (iterator.hasNext())
            System.out.println(iterator.next());

        usersList.clear();
    }

    // command -list current project users method
    private void handleListCurProjectUsersCommand() {
        log.info("Command: " + command);
        if (currentProjectIndex != -1) {
            currentProject = projects.get(currentProjectIndex);
            usersList.addAll(currentProject.getProjectUsers());

            currentPorition = 0;
            for (User user : usersList) {
                System.out.println(currentPorition + " " + user.getFirstName());
                currentPorition++;
            }
        } else {
            message = "First need to select project";
            log.warn(message);
            System.out.println(message);
        }
    }

    // command -list all issues method
    private void handleListAllIssuesCommand() {
        log.info("Command: " + command);
        usersList = new ArrayList<>();
        for (Project project : projects) {
            usersList.addAll(project.getProjectUsers());
        }
        List<Issue> issues = new ArrayList<>();

        for (User user : usersList) {
            issues.addAll(user.getIssues());
        }

        for (Issue issue : issues) {
            System.out.println(issue.getIssue());
        }


        usersList.clear();
    }

    // command -user report method
    private void handleUserReportCommand() {
        log.info("Command: " + command);
        String userReport = "";

        System.out.println("Enter the name of user");
        command = commandScanner.nextLine();
        log.info("User name: " + command);
        System.out.println("Issues of '" + command + "' user:");

        for (Project project : projects) {
            List<User> users = project.getProjectUsers();
            for (User user : users) {
                if (command.equals(user.getFirstName())) {
                    List<Issue> userIssues = user.getIssues();
                    for (Issue issue : userIssues) {
                        userReport += "\tProject '" + project.getName() + "' issue: " + issue.getIssue() + "\n";
                    }
                }
            }
        }
        if (userReport != "") {
            System.out.println(userReport);
        } else {
            message = "No issues of this user";
            log.warn(message);
            System.out.println(message);
        }

    }

    // command -select method
    private void handleSelectCommand() {
        log.info("Command: " + command);
        selectScanner = new Scanner(System.in);

        currentPorition = 0;
        if (currentProjectIndex == -1) {
            for (Project project : projects) {
                System.out.println(currentPorition + ". " + project.getName());
                currentPorition++;
            }
            System.out.println("\nEnter project index");
            try {
                currentProjectIndex = selectScanner.nextInt();
                log.info("Selected position: " + selectScanner);
                currentProject = projects.get(currentProjectIndex);

                System.out.println("'" + currentProject.getName() + "' is selected");
            } catch (Exception e) {
                message = "Enter correct project index!";
                log.warn(message);
                System.out.println(message);
            }


        } else {
            currentPorition = 0;
            currentProject = projects.get(currentProjectIndex);
            List<User> projectUsers = currentProject.getProjectUsers();
            for (User user : projectUsers) {
                System.out.println(currentPorition + ". " + user.getFirstName());
                currentPorition++;
            }
            message = "Enter user index";
            log.warn(message);
            System.out.println("\n" + message);
            try {
                currentUserIndex = selectScanner.nextInt();
                log.info("Selected user indes " + selectScanner);
                currentUser = projectUsers.get(currentUserIndex);

                System.out.println("'" + currentUser.getFirstName() + "' is selected");
            } catch (Exception e) {
                message = "Enter correct user index!";
                log.warn(message);
                System.out.println(message);
            }
        }


    }

    // command -back method
    private void handleBackCommand() {
        log.info("Command: " + command);
        if (currentProjectIndex != -1 & currentUserIndex != -1) {
            currentUserIndex = -1;
            log.info("Back to: " + currentProject.getName());
            System.out.println("Back to Project '" + currentProject.getName() + "'");
        } else {
            currentProjectIndex = -1;
            message = "Back to projects";
            log.info(message);
            System.out.println(message);
        }
    }

    // command -add user method
    private void handleAddUserCommand() throws IOException, SQLException {
        log.info("Command: " + command);
        if (currentProjectIndex != -1) {
            System.out.println("Enter new username");
            command = commandScanner.nextLine();
            log.info("New user: " + command);
            currentProject.addUser(userIDCount, command);


            if (dbConnectionClass.isDBConnect == true) {
                log.info("Add new user to db: " + command + " id=" + userIDCount);
                User user = new User(userIDCount,command);
                dbConnectionClass.addQueryUser(user, currentProject.getId());

                createCountsFile();
                createDataFile();

            }
            userIDCount++;
            System.out.println("User '" + command + "' successfully added:");
            currentProject = projects.get(currentProjectIndex);
            List<User> users = currentProject.getProjectUsers();


        } else {
            message = "First need to select project";
            log.warn(message);
            System.out.println(message);
        }
    }

    // command -add issue method
    private void handleAddIssueCommand() throws IOException, SQLException {
        log.info("Command: " + command);
        if (currentProjectIndex != -1 & currentUserIndex != -1) {
            System.out.println("Enter new issue");
            command = commandScanner.nextLine();
            log.info("New issue: " + command);
            currentUser.addIssue(issueIDCount, command);
            if (dbConnectionClass.isDBConnect == true) {
                log.info("Add new issue to db: " + command + " id=" + issueIDCount);
                Issue issue = new Issue(issueIDCount, command);
                dbConnectionClass.addQueryIssue(issue, currentUser.getId());

                createCountsFile();
                createDataFile();

            }
            issueIDCount++;
            System.out.println("issue successfully added:");
        } else {
            message = "First need to select user in the project";
            log.warn(message);
            System.out.println(message);
        }
    }

    // command -save method
    private void handleSaveCommand() {
        log.info("Command: " + command);
        try {
            createDataFile();
            createCountsFile();

        } catch (IOException e) {

            message = "Error:" +
                    "can't save Data!";
            log.error(message);
            System.out.println(message);
        }
        System.out.println("Saved");
    }

    // command -load method
    private void handleLoadCommand() {
        log.info("Command: " + command);
        try {
            readDataFile();
            readCountsFile();

        } catch (IOException e) {
            message = "Error:" +
                    "file does not exist";
            log.error(message);
            System.out.println(message);
        } catch (ClassNotFoundException e) {

            message = "Error:" +
                    "can't load Data!";
            log.error(message);
            System.out.println(message);
        }
        System.out.println("Loaded");
    }

    // command -connect db method
    private void handleConnectDBCommand() throws SQLException {
        log.info("Command: " + command);
        System.out.println("Enter ip address,\n" +
                "press 'enter' to connect by default ip: '100.103.231.218'");
        String ip = commandScanner.nextLine();
        log.info("IP: " + command);
        if (ip.equals("")) {
            log.info("default ip: 100.103.231.218");

            dbConnectionClass = new DBConnectionClass("100.103.231.218", "postgres", "root");

        } else {
            System.out.println("Enter login");
            String user = commandScanner.nextLine();
            log.info("DB login: " + command);
            System.out.println("Enter password");
            String pass = commandScanner.nextLine();
            log.info("DB password: " + command);

            dbConnectionClass = new DBConnectionClass(ip, user, pass);

        }

    }

    // command -save db method
    private void handleSaveDBCommand() throws IOException, SQLException {
        log.info("Command: " + command);
        if (dbConnectionClass != null) {

            dbConnectionClass.saveDatatoDB(projects);
            createDataFile();
            createCountsFile();

        } else {
            message = "First you need to connect to the database";
            log.info(message);
            System.out.println(message);
        }
    }

    // save Data in memory
    private void createDataFile() throws IOException {
        log.info("Creating file: Data");
        fileOutputStream = new FileOutputStream("Data");
        objectOutputStream = new ObjectOutputStream(fileOutputStream);
        objectOutputStream.writeObject(projects);
        objectOutputStream.close();

    }

    // read Data from memory
    private void readDataFile() throws IOException, ClassNotFoundException {
        log.info("Reading file: Data");
        fileInputStream = new FileInputStream("Data");
        objectInputStream = new ObjectInputStream(fileInputStream);
        projects = (ArrayList<Project>) objectInputStream.readObject();
        objectInputStream.close();


    }

    // save Counts in memory
    private void createCountsFile() throws IOException {
        log.info("Creating file: Counts");
        countsArray[0] = projectIDCount;
        countsArray[1] = userIDCount;
        countsArray[2] = issueIDCount;

        fileOutputStream = new FileOutputStream("Counts");
        objectOutputStream = new ObjectOutputStream(fileOutputStream);
        objectOutputStream.writeObject(countsArray);
        objectOutputStream.close();

    }

    // read Counts from memory
    private void readCountsFile() throws IOException, ClassNotFoundException {
        log.info("Reading file: Counts");
        fileInputStream = new FileInputStream("Counts");
        objectInputStream = new ObjectInputStream(fileInputStream);
        countsArray = (int[]) objectInputStream.readObject();
        objectInputStream.close();

        projectIDCount = countsArray[0];
        userIDCount = countsArray[1];
        issueIDCount = countsArray[2];


    }

    // fill arrays for demonstration
    private void fillArrays() {
        log.info("Filling array");
        List<Issue> issues = new ArrayList<>();
        List<User> users = new ArrayList<>();

        int numberOfProjects = 5;
        int numberOfUsers = 4;
        int numberOfIssues = 4;


        for (int i = 0; i <= numberOfProjects; i++) {
            int usersCount = 0 + (int) (Math.random() * numberOfUsers);
            for (int j = 0; j <= usersCount; j++) {
                int issuesCount = 0 + (int) (Math.random() * numberOfIssues);
                for (int k = 0; k <= issuesCount; k++) {
                    issues.add(new Issue(issueIDCount, String.format("%sth issue of %dth user", k + 1, j + 1)));
                    issueIDCount++;
                }
                users.add(new User(userIDCount, String.format("%sth user", j + 1), new ArrayList<>(issues)));
                userIDCount++;
                issues.clear();
            }
            projects.add(new Project(projectIDCount, String.format("%sth project", i + 1), new ArrayList<>(users)));
            projectIDCount++;
            users.clear();

        }


    }


}
