package student.newinti.spendee;

public class Transaction {

    private String transactionId, userId, type, category, description, date; // type=food or transportation or entertainment or others
    private double amount;

//    required for firebase
    public Transaction() {}

    public Transaction(String transactionId, String userId, String type, String category, double amount, String description, String date) {
        this.transactionId = transactionId;
        this.userId = userId;
        this.type = type;
        this.category = category;
        this.amount = amount;
        this.description = description;
        this.date = date;
    }

    public String getTransactionId() {
        return transactionId;
    }

    public String getUserId() {return userId; }

    public String getType() {
        return type;
    }

    public String getCategory() {
        return category;
    }

    public double getAmount() {
        return amount;
    }

    public String getDescription() {
        return description;
    }

    public String getDate() {
        return date;
    }

    public void setTransactionId(String transactionId) {
        this.transactionId = transactionId;
    }

    public void setUserId(String userId) {this.userId = userId; }

    public void setType(String type) {
        this.type = type;
    }

    public void setCategory(String category) {
        this.category = category;
    }

    public void setAmount(double amount) {
        this.amount = amount;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public void setDate(String date) {
        this.date = date;
    }
}

