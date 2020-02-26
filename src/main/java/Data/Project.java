package Data;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

public class Project implements Serializable {
    private int id;
    private String name;
    private  List<User> projectUsers = new ArrayList<>();
    private StringBuffer returnStringBuffer = new StringBuffer("");
    private String returnProjectString;



    public Project(int id, String name) {
        this.id = id;
        this.name = name;

    }

    public Project(int id, String name, List<User> projectUsers) {
        this.id = id;
        this.name = name;
        this.projectUsers = projectUsers;
    }


    public User getUser(int index) {
        try {
            if (projectUsers != null)
                return projectUsers.get(index);

        } catch (Exception e) {
            System.out.println("Wrong index");
        }


        System.out.println("Project " + name + " is empty");
        return null;
    }

    public String getName() {
        return name;
    }

    public List<User> getProjectUsers() {
        return projectUsers;
    }

    public void addUser(int userID, String user) {
        projectUsers.add(new User(userID, user));
    }

    public int getId() {
        return id;
    }

    @Override
    public String toString() {
        returnStringBuffer.append("\nProject (" + name + "): ");

        if (projectUsers.isEmpty()) {
            returnStringBuffer.append("\n\t" + "no users in this project");

        } else {

            for (User user : projectUsers) {
                returnStringBuffer.append("\n\t" + user.toString());
            }
        }

        returnStringBuffer.append("\n");
        returnProjectString = String.valueOf(returnStringBuffer);
        return returnProjectString;
    }
}
