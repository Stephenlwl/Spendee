package student.newinti.spendee;

public class Member {
    private String userID, username, email, password;

    public Member(){}

    public Member(String userId, String username, String email, String password) {
        this.userID = userId;
        this.username = username;
        this.email = email;
        this.password = password;
    }

    // Getters and setters
    public String getUserID() {
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

    public void setUserID(String userID) {
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
