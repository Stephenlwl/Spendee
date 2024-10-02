package student.newinti.spendee;

public class Member {
    private int userID;
    private String username, email, password;

    public Member(){}

    public Member(int id, String username, String email, String password) {
        this.userID = id;
        this.username = username;
        this.email = email;
        this.password = password;
    }

    // Getters and setters
    public int getUserID() {
        return userID;
    }

    public String getUsername() {
        return username;
    }

    public String getEmail(){
        return email;
    }

    public String getPassword(){
        return password;
    }

    public void setUserID(int userID) {
        this.userID = userID;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public void setPassword(String password) {
        this.password = password;
    }
}
