package student.newinti.spendee;

import android.os.Bundle;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.view.View;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;

public class ExpenseTrackingActivity extends AppCompatActivity {

    private TextView username_text_view;
    private TextView budgetRemainingText, warningText, weeklySummary;
    private RecyclerView transactionRecyclerView;
    private TransactionAdapter transactionAdapter;
    private List<Transaction> transactionsList = new ArrayList<>();
    private double totalExpenses = 0, totalIncome = 0;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_expense_tracking);

        // Initialize views
        budgetRemainingText = findViewById(R.id.budget_remaining_text);
        warningText = findViewById(R.id.warning_text);
        weeklySummary = findViewById(R.id.weekly_summary);
        transactionRecyclerView = findViewById(R.id.transaction_recycler_view);

        // Setup RecyclerView
        transactionAdapter = new TransactionAdapter(transactionsList);

        transactionRecyclerView.setLayoutManager(new LinearLayoutManager(this));
        transactionRecyclerView.setAdapter(transactionAdapter);

        // Get userId from Intent
        String userId = getIntent().getStringExtra("UserID");
        getUsernameByUserId(userId);
        // shows the user account name
        username_text_view = findViewById(R.id.username_text_view);

        getTransactions(userId);
        // display the type and category selection
        Spinner typeSpinner = findViewById(R.id.type_spinner);
        Spinner categorySpinner = findViewById(R.id.category_spinner);

        // hide expense category at initial
        categorySpinner.setVisibility(View.GONE);

        // handle record type selection
        typeSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parentView, View selectedItemView, int position, long id) {
                String selectedType = typeSpinner.getSelectedItem().toString();
                checkRecordType(selectedType);
            }

            @Override
            public void onNothingSelected(AdapterView<?> adapterView) {

            }
        });

        EditText amountInput = findViewById(R.id.amount_input);
        EditText descriptionInput = findViewById(R.id.description_input);
        Button addTransactionButton = findViewById(R.id.add_transaction_button);

        addTransactionButton.setOnClickListener(v -> {
            String type = typeSpinner.getSelectedItem().toString();
            String category = categorySpinner.getSelectedItem().toString();
            String amountText = amountInput.getText().toString().trim(); // Get amount in text input
            double amount = 0.00;
            String description = descriptionInput.getText().toString();

            // return null if type is income else return the selected category
            category = checkRecordsType(type, category);
            // Validate type selection
            if (type.isEmpty()) {
                Toast.makeText(ExpenseTrackingActivity.this, "Please select a Record Type", Toast.LENGTH_SHORT).show();
                return;
            }

            // Validate amount input
            if (amountText.isEmpty()) {
                Toast.makeText(ExpenseTrackingActivity.this, "Please enter an amount", Toast.LENGTH_SHORT).show();
                return;
            }

            try {
                amount = Double.parseDouble(amountText);
            } catch (NumberFormatException e) {
                Toast.makeText(ExpenseTrackingActivity.this, "Please enter a valid number for the amount", Toast.LENGTH_SHORT).show();
                return;
            }

            // Validate category if the type is Expense
            category = checkRecordsType(type, category);
            if (type.equals("Expense")) {
                if (category.equals("Choose Expense Category")) {
                    Toast.makeText(ExpenseTrackingActivity.this, "Please select a valid expense category", Toast.LENGTH_SHORT).show();
                    return;
                }
            }

            // Validate description
            if (description.isEmpty()) {
                Toast.makeText(ExpenseTrackingActivity.this, "Please enter a description", Toast.LENGTH_SHORT).show();
                return;
            }
            String date = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(new Date());

            // add transaction/expenses record in firebase
            addTransaction(type, category, amount, description, date, userId);

        });
    }

    private String checkRecordsType(String type, String category) {

        if (type.equals("Expense")) {
            return category;
        } else if (type.equals("Income")) {
            return null;
        } else {
            Toast.makeText(ExpenseTrackingActivity.this, "Please select the Record Type", Toast.LENGTH_SHORT).show();
            return null;
        }
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
                            Toast.makeText(ExpenseTrackingActivity.this, "Username not found", Toast.LENGTH_SHORT).show();
                        }
                    } else {
                        Toast.makeText(ExpenseTrackingActivity.this, "User not found", Toast.LENGTH_SHORT).show();
                    }
                }

                @Override
                public void onCancelled(DatabaseError databaseError) {
                    Toast.makeText(ExpenseTrackingActivity.this, "Error: " + databaseError.getMessage(), Toast.LENGTH_SHORT).show();
                }
            });
        } else {
            Toast.makeText(ExpenseTrackingActivity.this, "Invalid UserID", Toast.LENGTH_SHORT).show();
        }
    }

    private void checkRecordType(String type){
        Spinner categorySpinner = findViewById(R.id.category_spinner);
        if (type.equals("Income")) {
            categorySpinner.setVisibility(View.GONE);
        } else if (type.equals("Expense")) {
            categorySpinner.setVisibility(View.VISIBLE);
        }
    }

    private void addTransaction(String type, String category, double amount, String description, String date, String userId) {
        DatabaseReference databaseReference = FirebaseDatabase.getInstance("https://spendee-dd19f-default-rtdb.firebaseio.com/")
                .getReference("Transactions");

        // Create transaction id as pk
        String transactionId = databaseReference.push().getKey();
        Transaction transaction = new Transaction(transactionId, userId, type, category, amount, description, date);
        databaseReference.child(transaction.getTransactionId()).setValue(transaction)
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        Toast.makeText(ExpenseTrackingActivity.this, "Transaction added successfully!", Toast.LENGTH_SHORT).show();

                    } else {
                        Toast.makeText(ExpenseTrackingActivity.this, "Failed to add transaction.", Toast.LENGTH_SHORT).show();
                    }
                });
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
                        transactionsList.add(transaction);
                        if (transaction.getType().equals("Expense")) {
                            totalExpenses += transaction.getAmount();
                        } else {
                            totalIncome += transaction.getAmount();
                        }
                    }
                }
                transactionAdapter.notifyDataSetChanged();
                updateSummary();
            }

            @Override
            public void onCancelled(DatabaseError error) {
                Toast.makeText(ExpenseTrackingActivity.this, error.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void updateSummary() {
        double remainingBudget = totalIncome - totalExpenses;
        budgetRemainingText.setText(String.format(Locale.getDefault(), "Remaining Budget: RM%.2f", remainingBudget));

        if (remainingBudget <= (totalIncome * 0.50)) {
            warningText.setVisibility(View.VISIBLE);
        } else {
            warningText.setVisibility(View.GONE);
        }

        // set up weekly summary details under this section
        weeklySummary.setText(String.format(Locale.getDefault(), "Weekly Income: RM%.2f\nWeekly Expenses: RM%.2f",
                totalIncome, totalExpenses));
    }
}
