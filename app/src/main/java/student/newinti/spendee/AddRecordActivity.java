package student.newinti.spendee;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.Toast;
import android.widget.CheckBox;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

public class AddRecordActivity extends AppCompatActivity {

    private Button backButton;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_record);

        // Get userId from Intent
        String userId = getIntent().getStringExtra("UserID");

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

        // set back button to return to the main page
        backButton = findViewById(R.id.back_button);

        backButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(AddRecordActivity.this, ExpenseTrackingActivity.class);
                intent.putExtra("UserID", userId);
                startActivity(intent);
            }
        });

        addTransactionButton.setOnClickListener(v -> {
            String type = typeSpinner.getSelectedItem().toString();
            String category = categorySpinner.getSelectedItem().toString();
            String amountText = amountInput.getText().toString().trim(); // Get amount in text input
            String description = descriptionInput.getText().toString();

            if (validateInputs(type, category, amountText, description)) {
                double amount = Double.parseDouble(amountText);;
                String date = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(new Date());

                // add transaction/expenses record in firebase
                addTransaction(type, category, amount, description, date, userId);
                clearInputFields(amountInput, descriptionInput, typeSpinner, categorySpinner);
            }
        });
    }

    private void checkRecordType(String type) {
        Spinner categorySpinner = findViewById(R.id.category_spinner);
        if (type.equals("Expense")) {
            categorySpinner.setVisibility(View.VISIBLE);
        } else if (type.equals("Income")) {
            categorySpinner.setVisibility(View.GONE);
        }
    }

    private String checkRecordsType(String type, String category) {
        if (type.equals("Expense")) {
            return category;
        } else if (type.equals("Income")) {
            return null;
        } else {
            Toast.makeText(AddRecordActivity.this, R.string.select_record_type , Toast.LENGTH_SHORT).show();
            return null;
        }
    }

    private boolean validateInputs(String type, String category, String amountText, String description) {
        if (type.isEmpty()) {
            Toast.makeText(this, R.string.select_record_type, Toast.LENGTH_SHORT).show();
            return false;
        }

        // Validate category if the type is Expense
        category = checkRecordsType(type, category);
        if (type.equals("Expense")) {
            if (category.equals("Choose Expense Category")) {
                Toast.makeText(AddRecordActivity.this, R.string.valid_expense_category , Toast.LENGTH_SHORT).show();
                return false;
            }
        }

        if (amountText.isEmpty()) {
            Toast.makeText(this, R.string.enter_amount, Toast.LENGTH_SHORT).show();
            return false;
        }

        try {
            Double.parseDouble(amountText); // Validate if amount is a valid number
        } catch (NumberFormatException e) {
            Toast.makeText(this, R.string.enter_valid_amount, Toast.LENGTH_SHORT).show();
            return false;
        }

        if (type.equals("Expense") && category.equals("Choose Expense Category")) {
            Toast.makeText(this, R.string.valid_expense_category, Toast.LENGTH_SHORT).show();
            return false;
        }

        if (description.isEmpty()) {
            Toast.makeText(this, R.string.enter_description, Toast.LENGTH_SHORT).show();
            return false;
        }

        return true; // If all inputs are valid
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
                        Toast.makeText(AddRecordActivity.this, R.string.transaction_success, Toast.LENGTH_SHORT).show();

                    } else {
                        Toast.makeText(AddRecordActivity.this, R.string.transaction_fail , Toast.LENGTH_SHORT).show();
                    }
                });
    }

    private void clearInputFields(EditText amountInput, EditText descriptionInput, Spinner typeSpinner, Spinner categorySpinner) {
        amountInput.setText("");
        descriptionInput.setText("");
        typeSpinner.setSelection(0); // reset the spinner to default position where index 0 = default
        categorySpinner.setVisibility(View.GONE); // hide category spinner initially
    }
}