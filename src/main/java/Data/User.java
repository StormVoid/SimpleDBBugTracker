package Data;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

public class User implements Serializable {
    private int id;
    private  String firstName;
    //private String lastName;
    private List<Issue> issues = new ArrayList<>();

    private String returnUserString;
    private StringBuffer returnStringBuffer = new StringBuffer("");

    public User(int id, String firstName) {
        this.id = id;
        this.firstName = firstName;

    }

    public User(int id, String firstName, List<Issue> issues) {
        this.id = id;
        this.firstName = firstName;
        this.issues = issues;
    }

    public String getFirstName() {
        return firstName;
    }

    public int getId() {
        return id;
    }

    public List<Issue> getIssues() {
        return issues;
    }

    public void addIssue(int issueID, String issue){
        issues.add(new Issue(issueID, issue));
    }

    @Override
    public String toString() {
        returnStringBuffer.append("\tUser (" + firstName + "): ");

        if (issues.isEmpty()) {
            returnStringBuffer.append("\n\t\t\t" + "This user does not have issues");

        } else {

            for (Issue s : issues) {
                returnStringBuffer.append("\n\t\t\t" + "-" + s.getIssue());
            }
        }


        returnUserString = String.valueOf(returnStringBuffer);
        return returnUserString;

    }
}
