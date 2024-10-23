package student.newinti.spendee;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.EditText;
import android.widget.Toast;
import android.widget.Button;

import androidx.activity.EdgeToEdge;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

public class BudgetSettingActivity extends AppCompatActivity {

    private EditText setLowBudgetAlert;
    private Button setAlertButton, backButton;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_budget_setting);

        String userId = getIntent().getStringExtra("UserID");
        double budgetAmount = getIntent().getDoubleExtra("BudgetAmount", 0.0);

        setLowBudgetAlert = findViewById(R.id.low_amount_budget_alert_input);
        setAlertButton = findViewById(R.id.set_alert_button);
        backButton = findViewById(R.id.back_button);

        setAlertButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                // Get the budget alert input and validate it
                String budgetAlertInput = setLowBudgetAlert.getText().toString().trim();

                if (budgetAlertInput.isEmpty()) {
                    Toast.makeText(BudgetSettingActivity.this, R.string.invalid_budget_amount_input, Toast.LENGTH_SHORT).show();
                    return;  // Stop execution
                }

                // Parse budget alert amount to double
                double budgetAlertAmount;
                try {
                    budgetAlertAmount = Double.parseDouble(budgetAlertInput);
                } catch (NumberFormatException e) {
                    e.printStackTrace();
                    Toast.makeText(BudgetSettingActivity.this, R.string.invalid_budget_alert_input, Toast.LENGTH_SHORT).show();
                    return;  // Stop execution
                }

                // Check if the budget value is valid
                if (budgetAlertAmount <= 0 || budgetAlertAmount >= budgetAmount) {
                    Toast.makeText(getApplicationContext(), R.string.invalid_budget_alert_less_than_budget_message, Toast.LENGTH_SHORT).show();
                    return;  // Stop execution
                }

                saveBudgetAlert(budgetAlertAmount, userId);
                clearInputFields(setLowBudgetAlert);
            }
        });

        backButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(BudgetSettingActivity.this, ExpenseTrackingActivity.class);
                intent.putExtra("UserID", userId);
                startActivity(intent);
            }
        });
    }

    // Method to save budget alert value to Firebase
    private void saveBudgetAlert(Double budgetAmount, String userId) {
        // Reference to Firebase Database
        DatabaseReference databaseReference = FirebaseDatabase.getInstance("https://spendee-dd19f-default-rtdb.firebaseio.com/")
                .getReference("Budget").child(userId);

        // Update or insert the budget amount in Firebase DB
        databaseReference.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                databaseReference.child("lowAmountBudgetAlert").setValue(budgetAmount);
                Toast.makeText(getApplicationContext(), R.string.alert_budget_set_successful, Toast.LENGTH_SHORT).show();
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
                // Handle possible errors
                Toast.makeText(getApplicationContext(), R.string.alert_budget_set_failed, Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void clearInputFields(EditText budgetAlertInput) {
        budgetAlertInput.setText("");
    }
}
