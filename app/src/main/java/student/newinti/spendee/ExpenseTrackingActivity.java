package student.newinti.spendee;

import android.content.Intent;
import android.os.Bundle;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.view.menu.ExpandedMenuView;
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

    private Button createRecordButton, logoutButton;
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

        // Get userId from Intent
        String userId = getIntent().getStringExtra("UserID");
        getUsernameByUserId(userId);

        // Initialize views
        budgetRemainingText = findViewById(R.id.budget_remaining_text);
        warningText = findViewById(R.id.warning_text);
        weeklySummary = findViewById(R.id.weekly_summary);
        transactionRecyclerView = findViewById(R.id.transaction_recycler_view);

        // Setup RecyclerView
        transactionAdapter = new TransactionAdapter(transactionsList);

        transactionRecyclerView.setLayoutManager(new LinearLayoutManager(this));
        transactionRecyclerView.setAdapter(transactionAdapter);

        // shows the user account name
        username_text_view = findViewById(R.id.username_text_view);
        getTransactions(userId);

        createRecordButton = findViewById(R.id.create_transaction_record_button);
        logoutButton = findViewById(R.id.logout_button);

        createRecordButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(ExpenseTrackingActivity.this, AddRecordActivity.class);
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
        budgetRemainingText.setText(String.format(Locale.getDefault(), getString(R.string.remaining_budget) + "%.2f", remainingBudget));

        if (remainingBudget <= (totalIncome * 0.50)) {
            warningText.setVisibility(View.VISIBLE);
        } else {
            warningText.setVisibility(View.GONE);
        }

        // set up weekly summary details under this section
        weeklySummary.setText(String.format(Locale.getDefault(), getString(R.string.weekly_income)  + "%.2f\n" + getString(R.string.weekly_expenses) + "%.2f",
                totalIncome, totalExpenses));
    }
}
