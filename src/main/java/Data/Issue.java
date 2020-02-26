package Data;

import java.io.Serializable;

public class Issue implements Serializable {
    private int id;
    private String issue;

    public Issue(int id, String issue) {
        this.id = id;
        this.issue = issue;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getIssue() {
        return issue;
    }

    public void setIssue(String issue) {
        this.issue = issue;
    }
}
