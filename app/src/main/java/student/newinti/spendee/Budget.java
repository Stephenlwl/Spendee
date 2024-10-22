package student.newinti.spendee;

public class Budget {
    private double budgetAmount;

    public Budget(){};

    public Budget(double budgetAmount) {
        this.budgetAmount = budgetAmount;
    }

    public double getBudgetAmount() {
        return budgetAmount;
    }

    public void setBudgetAmount(double budgetAmount) {
        this.budgetAmount = budgetAmount;
    }
}
