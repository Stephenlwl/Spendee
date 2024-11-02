package student.newinti.spendee;

import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.view.menu.ExpandedMenuView;
import androidx.core.app.ActivityCompat;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.view.View;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import com.github.mikephil.charting.data.PieData;
import com.github.mikephil.charting.data.PieDataSet;
import com.github.mikephil.charting.data.PieEntry;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Calendar;
import java.util.Map;

import com.github.mikephil.charting.charts.PieChart;

public class ExpenseTrackingActivity extends AppCompatActivity {

    private Button createRecordButton, logoutButton, saveButton, setLowBudgetAlertButton, viewMonthlyRecordsButton;
    private TextView username_text_view;
    private TextView budgetRemainingText, warningText, cautionText, monthlyIncome, monthlyExpenses, monthlyBudget;
    private RecyclerView transactionRecyclerView;
    private TransactionAdapter transactionAdapter;
    private List<Transaction> transactionsList = new ArrayList<>();
    private double totalExpenses = 0, totalBudget = 0, totalIncome = 0, budgetAmount = 0;
    private PieChart pieChart;
    // provide difference color for each expenses category
    private int[] pastelColors = {
            0xFFD3C1E3, // Pastel Lavender
            0xFFB2E3D4, // Pastel Mint
            0xFFFFCBA4, // Pastel Peach
            0xFFEAB8E4, // Pastel Lilac
            0xFF9DD6E1, // Pastel Sky Blue
            0xFFFF6F61 // Pastel Coral
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_expense_tracking);

        // Get userId from Intent
        String userId = getIntent().getStringExtra("UserID");
        getUsernameByUserId(userId);

        // Initialize views
        budgetRemainingText = findViewById(R.id.budget_remaining_text);
        warningText = findViewById(R.id.budget_exceed_alert_warning_text);
        cautionText = findViewById(R.id.low_budget_alert_warning_text);
        monthlyExpenses = findViewById(R.id.monthly_expenses_value);
        monthlyIncome = findViewById(R.id.monthly_income_value);
        monthlyBudget = findViewById(R.id.monthly_budget_value);
        transactionRecyclerView = findViewById(R.id.transaction_recycler_view);
        pieChart = findViewById(R.id.pie_chart);

        // Setup RecyclerView
        transactionAdapter = new TransactionAdapter(transactionsList);

        transactionRecyclerView.setLayoutManager(new LinearLayoutManager(this));
        transactionRecyclerView.setAdapter(transactionAdapter);

        // shows the user account name
        username_text_view = findViewById(R.id.username_text_view);
        saveButton = findViewById(R.id.set_budget_button);
        createRecordButton = findViewById(R.id.create_transaction_record_button);
        setLowBudgetAlertButton = findViewById(R.id.set_low_budget_alert_button);
        viewMonthlyRecordsButton = findViewById(R.id.view_monthly_record_button);
        logoutButton = findViewById(R.id.logout_button);

        getTransactions(userId);
        getBudget(userId);
        updatePieChart();

        saveButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                EditText budgetText = findViewById(R.id.set_budget_text);
                String budgetTextString = budgetText.getText().toString().trim();

                if (budgetTextString.isEmpty()) {
                    Toast.makeText(getApplicationContext(), "Please enter a budget value", Toast.LENGTH_SHORT).show();
                    return;  // Stop execution
                }

                // parse budget amount to double
                try {
                    budgetAmount = Double.parseDouble(budgetTextString);
                } catch (NumberFormatException e) {
                    e.printStackTrace();
                    Toast.makeText(ExpenseTrackingActivity.this, "Invalid input for budget amount", Toast.LENGTH_SHORT).show();
                    return;  // Stop execution
                }

                // check if the budget value is valid
                if (budgetAmount <= 0) {
                    Toast.makeText(getApplicationContext(), "Please enter a valid budget value greater than 0", Toast.LENGTH_SHORT).show();
                    return;  // Stop execution
                }

                // reference to fire db
                DatabaseReference databaseReference = FirebaseDatabase.getInstance("https://spendee-dd19f-default-rtdb.firebaseio.com/")
                        .getReference("Budget").child(userId);

                // update or insert the budget amount in fire db
                databaseReference.addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                        databaseReference.child("budgetAmount").setValue(budgetAmount);

                        getBudget(userId);
                        updateSummary(userId);
                        Toast.makeText(getApplicationContext(), "Budget saved successfully", Toast.LENGTH_SHORT).show();
                        clearInputFields(budgetText);
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError databaseError) {
                        // handle possible errors
                        Toast.makeText(getApplicationContext(), "Failed to save budget", Toast.LENGTH_SHORT).show();
                    }
                });
            }
        });

        createRecordButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(ExpenseTrackingActivity.this, AddRecordActivity.class);
                intent.putExtra("UserID", userId);
                startActivity(intent);
            }
        });

        setLowBudgetAlertButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(ExpenseTrackingActivity.this, BudgetSettingActivity.class);
                intent.putExtra("UserID", userId);
                intent.putExtra("BudgetAmount", totalBudget);
                startActivity(intent);
            }
        });

        viewMonthlyRecordsButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(ExpenseTrackingActivity.this, ViewMonthlyActivity.class);
                intent.putExtra("UserID", userId);
                startActivity(intent);
            }
        });

        logoutButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(ExpenseTrackingActivity.this, LoginActivity.class);
                startActivity(intent);
            }
        });
    }

    private void getUsernameByUserId(String userId) {
        if (userId != null && !userId.isEmpty()) { // check if UserID is valid
            DatabaseReference databaseReference = FirebaseDatabase.getInstance("https://spendee-dd19f-default-rtdb.firebaseio.com/")
                    .getReference("Users").child(userId); // reference to the specific user's node (specific user data based on userid)

            databaseReference.addListenerForSingleValueEvent(new ValueEventListener() {
                @Override
                public void onDataChange(DataSnapshot dataSnapshot) {
                    // if user data exists
                    if (dataSnapshot.exists()) {
                        String username = dataSnapshot.child("username").getValue(String.class);
                        if (username != null) {
                            username_text_view.setText(username);
                            System.out.println("username: " + username);
                        } else {
                            Toast.makeText(ExpenseTrackingActivity.this, R.string.username_not_found, Toast.LENGTH_SHORT).show();
                        }
                    } else {
                        Toast.makeText(ExpenseTrackingActivity.this, R.string.user_not_found, Toast.LENGTH_SHORT).show();
                    }
                }

                @Override
                public void onCancelled(DatabaseError databaseError) {
                    Toast.makeText(ExpenseTrackingActivity.this, R.string.database_error + databaseError.getMessage(), Toast.LENGTH_SHORT).show();
                }
            });
        } else {
            Toast.makeText(ExpenseTrackingActivity.this, R.string.invalid_user_id, Toast.LENGTH_SHORT).show();
        }
    }

    private void getTransactions(String userId) {
        DatabaseReference databaseReference = FirebaseDatabase.getInstance("https://spendee-dd19f-default-rtdb.firebaseio.com/")
                .getReference("Transactions");

        databaseReference.orderByChild("userId").equalTo(userId).addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                transactionsList.clear();
                totalExpenses = 0;
                totalIncome = 0;

                for (DataSnapshot snapshot : dataSnapshot.getChildren()) {
                    Transaction transaction = snapshot.getValue(Transaction.class);

                    if (transaction != null) {
                        // filter transaction record to include only those from the current month and year
                        if (isTransactionFromCurrentMonth(transaction)) {
                            transactionsList.add(transaction);
                            if (transaction.getType().equals("Expense")) {
                                totalExpenses += transaction.getAmount();
                            } else if (transaction.getType().equals("Income")) {
                                totalIncome += transaction.getAmount();
                            } else {
                                totalBudget += transaction.getAmount();
                            }
                        }
                    }
                }
                transactionAdapter.notifyDataSetChanged();
                updatePieChart();
                updateSummary(userId);
            }

            @Override
            public void onCancelled(DatabaseError error) {
                Toast.makeText(ExpenseTrackingActivity.this, error.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void getBudget(String userId) {
        // Reference to the specific user's node under "Budget"
        DatabaseReference databaseReference = FirebaseDatabase.getInstance("https://spendee-dd19f-default-rtdb.firebaseio.com/")
                .getReference("Budget").child(userId);

        databaseReference.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                totalBudget = 0;

                Budget budget = dataSnapshot.getValue(Budget.class);

                if (budget != null) {
                    totalBudget = budget.getBudgetAmount();
                }

                updatePieChart();
                updateSummary(userId);
            }

            @Override
            public void onCancelled(DatabaseError error) {
                // Handle errors
                Toast.makeText(ExpenseTrackingActivity.this, error.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }


    private boolean isTransactionFromCurrentMonth(Transaction transaction) {
        Calendar calendar = Calendar.getInstance();
        int currentMonth = calendar.get(Calendar.MONTH);
        int currentYear = calendar.get(Calendar.YEAR);

        String dateString = transaction.getDate();
        try {
            Date date = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).parse(dateString);
            Calendar transactionCalendar = Calendar.getInstance();
            transactionCalendar.setTime(date);

            int transactionMonth = transactionCalendar.get(Calendar.MONTH);
            int transactionYear = transactionCalendar.get(Calendar.YEAR);

            // return true if the transaction record is from the current month and year
            return transactionMonth == currentMonth && transactionYear == currentYear;
        } catch (ParseException e) {
            e.printStackTrace();
            return false; // skip this record if date not same as current month and year
        }
    }

    private void updateSummary(String userId) {
        DatabaseReference databaseReference = FirebaseDatabase.getInstance("https://spendee-dd19f-default-rtdb.firebaseio.com/")
                .getReference("Budget").child(userId);

        // Fetch the budget, expenses, and alert threshold from Firebase
        databaseReference.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                if (dataSnapshot.exists()) {
                    // Get budget and alert threshold, checking for null values
                    Double budgetAmount = dataSnapshot.child("budgetAmount").getValue(Double.class);
                    Double lowAmountBudgetAlert = dataSnapshot.child("lowAmountBudgetAlert").getValue(Double.class);

                    // If budgetAmount or lowAmountBudgetAlert is null, handle it accordingly
                    if (budgetAmount == null) {
                        budgetAmount = 0.0; // Default value or handle error
                    }
                    if (lowAmountBudgetAlert == null) {
                        lowAmountBudgetAlert = 0.0; // Default value or handle error
                    }

                    totalBudget = budgetAmount;
                    monthlyExpenses.setText(String.format(Locale.getDefault(), "RM " + "%.2f", totalExpenses));
                    monthlyIncome.setText(String.format(Locale.getDefault(), "RM " + "%.2f", totalIncome));
                    monthlyBudget.setText(String.format(Locale.getDefault(), "RM " + "%.2f", totalBudget));

                    double remainingBudget = totalBudget - totalExpenses;

                    // Update UI with remaining budget
                    budgetRemainingText.setText(String.format(Locale.getDefault(), getString(R.string.remaining_budget) + " %.2f", remainingBudget));

                    // Display warning text if remaining budget is below the alert threshold
                    if (remainingBudget <= lowAmountBudgetAlert && remainingBudget > totalExpenses ) {
                        cautionText.setVisibility(View.VISIBLE);
                        warningText.setVisibility(View.GONE);
                    } else if (remainingBudget <=0 ) {
                        warningText.setVisibility(View.VISIBLE);
                        cautionText.setVisibility(View.GONE);
                    } else {
                        warningText.setVisibility(View.GONE);
                        cautionText.setVisibility(View.GONE);
                    }
                } else {
                    // Handle case where the snapshot does not exist
                    Toast.makeText(ExpenseTrackingActivity.this, "Budget data not found", Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onCancelled(DatabaseError error) {
                // Handle errors
                Toast.makeText(ExpenseTrackingActivity.this, error.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void updatePieChart() {
        Map<String, Double> categoryExpenses = new HashMap<>();
        for (Transaction transaction : transactionsList) {
            if (transaction.getType().equals("Expense")) {
                String category = transaction.getCategory();
                double amount = transaction.getAmount();
                categoryExpenses.put(category, categoryExpenses.getOrDefault(category, 0.0) + amount);
            }
        }

        List<PieEntry> entries = new ArrayList<>();
        for (Map.Entry<String, Double> entry : categoryExpenses.entrySet()) {
            entries.add(new PieEntry(entry.getValue().floatValue(), entry.getKey()));
        }

        pieChart.setDrawEntryLabels(false); // disable the category label on the chart
        pieChart.getDescription().setEnabled(false); // Disable description label

        PieDataSet dataSet = new PieDataSet(entries, "Expenses by Category");
        List<Integer> colors = new ArrayList<>();
        for (int i = 0; i < entries.size(); i++) {
            if (i == entries.size() - 1) { // last entry for budget
                colors.add(0xFFFFA500); // Orange for budget
            } else {
                colors.add(pastelColors[i % pastelColors.length]); // Cycle through pastel colors
            }
        }

        // get the budget amount and set it as a separate entry
        double budgetAmount = totalBudget - totalExpenses;
        entries.add(new PieEntry((float) budgetAmount, "Budget Remain"));

        dataSet.setColors(colors);
        PieData data = new PieData(dataSet);
        pieChart.setData(data);
        pieChart.invalidate(); // refresh
    }
    // for the budget setting input
    private void clearInputFields(EditText budgetAmountInput) {
        budgetAmountInput.setText("");
    }

}
