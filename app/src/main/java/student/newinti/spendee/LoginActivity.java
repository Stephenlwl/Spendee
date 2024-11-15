package student.newinti.spendee;

import android.os.Bundle;

import androidx.activity.EdgeToEdge;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import android.util.Patterns;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;
import android.content.Intent;
import android.widget.CheckBox;

import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.ValueEventListener;
import org.mindrot.jbcrypt.BCrypt;

public class LoginActivity extends AppCompatActivity {
    //  declare the id variables
    private EditText email_input, password_input;
    private Button login_button;
    private TextView sign_up_link;
    private CheckBox showPasswordCheckbox;
    private android.text.InputType InputType;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        email_input = findViewById(R.id.email_input);
        password_input = findViewById(R.id.password_input);
        login_button = findViewById(R.id.login_button);
        sign_up_link = findViewById(R.id.sign_up_link);
        showPasswordCheckbox = findViewById(R.id.show_password_checkbox);

        showPasswordCheckbox.setOnCheckedChangeListener((buttonView, isChecked) -> {
            if (isChecked) {
                password_input.setInputType(InputType.TYPE_TEXT_VARIATION_VISIBLE_PASSWORD);
            } else {
                password_input.setInputType(InputType.TYPE_CLASS_TEXT | InputType.TYPE_TEXT_VARIATION_PASSWORD);
            }
            // Move cursor to the end of the input field
            password_input.setSelection(password_input.getText().length());
        });

        login_button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v){
                String email = email_input.getText().toString().trim();
                String password = password_input.getText().toString().trim();

                if (!email.isEmpty() && !password.isEmpty()) {
                    getUserInDatabase(email, password);
                } else {
                    Toast.makeText(LoginActivity.this, R.string.login_incomplete_toast, Toast.LENGTH_SHORT).show();
                }
            }

        });

        sign_up_link.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v){
                // Navigate to the SignUpActivity
                Intent intent = new Intent(LoginActivity.this, SignUpActivity.class);
                startActivity(intent);
            }
        });

    }

    // get user information in Firebase for validate user account info
    private void getUserInDatabase(String emailInput, String passwordInput) {
        // get reference to the Firebase
        DatabaseReference databaseReference = FirebaseDatabase.getInstance("https://spendee-dd19f-default-rtdb.firebaseio.com/")
                .getReference("Users");

        // retrieve all users and iterate over them
        databaseReference.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                boolean userFound = false;
                boolean passwordCorrect = false;
                //  iterate through all children under the users node
                for (DataSnapshot snapshot : dataSnapshot.getChildren()) {
                    // get the Member object from each snapshot
                    Member storedUser = snapshot.getValue(Member.class);

                    if (storedUser != null) {

                        if (storedUser.getEmail().equals(emailInput)) {
                            userFound = true;
                            // Validate the email and password using bcrypt
                            if (BCrypt.checkpw(passwordInput, storedUser.getPassword())) {
                                passwordCorrect = true;
                                Toast.makeText(LoginActivity.this, R.string.login_success_toast, Toast.LENGTH_SHORT).show();
                                Intent intent = new Intent(LoginActivity.this, ExpenseTrackingActivity.class);
                                intent.putExtra("UserID", storedUser.getUserID());
                                startActivity(intent);
                                finish();
                                break;
                            }
                        }
                    }
                }
                if (!userFound) {
                    Toast.makeText(LoginActivity.this, R.string.user_not_found, Toast.LENGTH_SHORT).show();
                } else if (!passwordCorrect) {
                    Toast.makeText(LoginActivity.this,  R.string.login_fail_toast, Toast.LENGTH_SHORT).show();
                }
            }
            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Toast.makeText(LoginActivity.this, R.string.database_error + error.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }
}