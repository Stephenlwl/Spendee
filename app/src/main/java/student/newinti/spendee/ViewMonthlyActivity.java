package student.newinti.spendee;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;
import java.util.Objects;

public class ViewMonthlyActivity extends AppCompatActivity {

    private Spinner monthSpinner, yearSpinner;
    private int selectedMonth, selectedYear;
    private Button backButton, applyFilterButton;
    private RecyclerView recyclerView;
    private TextView totalSpentTextView, budgetTextView;
    private TransactionAdapter transactionAdapter;
    private DatabaseReference databaseReference;
    private double totalSpent = 0.0;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_view_monthly);
        String userId = getIntent().getStringExtra("UserID");

        monthSpinner = findViewById(R.id.month_spinner);
        yearSpinner = findViewById(R.id.year_spinner);
        applyFilterButton = findViewById(R.id.apply_filter_button);

        // populate month spinner
        ArrayAdapter<String> monthAdapter = new ArrayAdapter<>(this,
                android.R.layout.simple_spinner_item,
                new String[]{"January", "February", "March", "April", "May", "June",
                        "July", "August", "September", "October", "November", "December"});
        monthSpinner.setAdapter(monthAdapter);

        // populate year spinner (for last 10 years)
        int currentYear = Calendar.getInstance().get(Calendar.YEAR);
        List<String> years = new ArrayList<>();
        for (int i = currentYear; i >= currentYear - 10; i--) {
            years.add(String.valueOf(i));
        }
        ArrayAdapter<String> yearAdapter = new ArrayAdapter<>(this, android.R.layout.simple_spinner_item, years);
        yearSpinner.setAdapter(yearAdapter);

        // shows related records
        recyclerView = findViewById(R.id.expensesRecyclerView);
        totalSpentTextView = findViewById(R.id.totalSpentTextView);
        backButton = findViewById(R.id.back_button);

        transactionAdapter = new TransactionAdapter(new ArrayList<>());
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        recyclerView.setAdapter(transactionAdapter);

        databaseReference = FirebaseDatabase.getInstance().getReference("transactions").child(userId);

        applyFilterButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // get selected month and year
                selectedMonth = monthSpinner.getSelectedItemPosition(); // 0-indexed (Jan = 0)
                selectedYear = Integer.parseInt(yearSpinner.getSelectedItem().toString());
                totalSpent = 0.0;
                // fetch transactions and filter by selected month and year , plus 1 to get exact month
                fetchMonthlyData(selectedMonth+1, selectedYear);
            }
        });

        backButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(ViewMonthlyActivity.this, ExpenseTrackingActivity.class);
                intent.putExtra("UserID", userId);
                startActivity(intent);
            }
        });
    }

    private void fetchMonthlyData(int selectedMonth, int selectedYear) {
        DatabaseReference databaseReference = FirebaseDatabase.getInstance("https://spendee-dd19f-default-rtdb.firebaseio.com/")
                .getReference("Transactions");

        // clear existing transactions before fetching new transaction
        List<Transaction> transactions = new ArrayList<>();

        databaseReference.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                transactions.clear(); // clear transactions for new data

                for (DataSnapshot monthSnapshot : snapshot.getChildren()) {
                    Transaction transaction = monthSnapshot.getValue(Transaction.class);
                    if (transaction != null) {
                        // get the transaction date as a calendar object
                        Calendar transactionDate = transaction.getDateAsCalendar();
                        int transactionMonth = transactionDate.get(Calendar.MONTH)+1;
                        int transactionYear = transactionDate.get(Calendar.YEAR);
                        String recordsType = transaction.getType();

                        Log.d("transactionMonth", String.valueOf(transactionMonth));
                        Log.d("transactionYear", String.valueOf(transactionYear));

                        // check if the transaction matches the selected month and year and records type
                        if (transactionMonth == selectedMonth && transactionYear == selectedYear && Objects.equals(recordsType, "Expense")) {
                            transactions.add(transaction);
                            totalSpent += transaction.getAmount();
                        }
                    }
                }

                // Log the results
                Log.d("SelectedMonth", String.valueOf(selectedMonth)); // Log the selected month
                Log.d("SelectedYear", String.valueOf(selectedYear)); // Log the selected year
                Log.d("TotalSpent", "Total Spent: RM" + totalSpent);
                Log.d("Transactions", "Number of transactions: " + transactions.size());

                totalSpentTextView.setText(String.format("Total Spent: RM%.2f", totalSpent));
                transactionAdapter.updateTransactions(transactions);
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Toast.makeText(getApplicationContext(), R.string.alert_budget_set_failed, Toast.LENGTH_SHORT).show();
            }
        });
    }

}
