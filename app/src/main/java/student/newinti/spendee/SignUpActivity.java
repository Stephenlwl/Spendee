package student.newinti.spendee;

import android.content.Intent;
import android.os.Bundle;
import android.text.InputType;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;
import android.widget.TextView;
import android.widget.CheckBox;

import androidx.appcompat.app.AppCompatActivity;

import com.google.firebase.FirebaseApp;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseAuthUserCollisionException;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import org.mindrot.jbcrypt.BCrypt;

public class SignUpActivity extends AppCompatActivity {
    //  declare the id variables
    private EditText email_input, password_input, confirm_password_input, username_input;
    private FirebaseAuth mAuth;
    private CheckBox showPasswordCheckbox;
    private android.text.InputType InputType;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_sign_up);

        // initialize Firebase Auth
        FirebaseApp.initializeApp(this);
        mAuth = FirebaseAuth.getInstance();
        username_input = findViewById(R.id.username_input);
        email_input = findViewById(R.id.email_input);
        password_input = findViewById(R.id.password_input);
        confirm_password_input = findViewById(R.id.confirm_password_input);
        showPasswordCheckbox = findViewById(R.id.show_password_checkbox);

        showPasswordCheckbox.setOnCheckedChangeListener((buttonView, isChecked) -> {
            if (isChecked) {
                password_input.setInputType(InputType.TYPE_TEXT_VARIATION_VISIBLE_PASSWORD);
                confirm_password_input.setInputType(InputType.TYPE_TEXT_VARIATION_VISIBLE_PASSWORD);
            } else {
                password_input.setInputType(InputType.TYPE_CLASS_TEXT | InputType.TYPE_TEXT_VARIATION_PASSWORD);
                confirm_password_input.setInputType(InputType.TYPE_CLASS_TEXT | InputType.TYPE_TEXT_VARIATION_PASSWORD);
            }
            // Move cursor to the end of the input field
            password_input.setSelection(password_input.getText().length());
            confirm_password_input.setSelection(confirm_password_input.getText().length());
        });

        Button sign_up_button = findViewById(R.id.sign_up_button);
        TextView login_link = findViewById(R.id.login_link);

        sign_up_button.setOnClickListener(v -> {
            String username = username_input.getText().toString().trim();
            String email = email_input.getText().toString().trim();
            String password = password_input.getText().toString().trim();
            String confirm_password = confirm_password_input.getText().toString().trim();

            // Check if the passwords match
            // Check if the passwords match and meet the requirements
            if (!password.equals(confirm_password)) {
                Toast.makeText(SignUpActivity.this, R.string.confirm_password_incorrect_toast, Toast.LENGTH_SHORT).show();
            } else if (!isPasswordValid(password)) {
                Toast.makeText(SignUpActivity.this, R.string.password_requirements_not_met, Toast.LENGTH_SHORT).show();
            } else {
                sendVerificationEmail(email, password, username);
            }
        });

        // Navigate to Login Screen
        login_link.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(SignUpActivity.this, LoginActivity.class);
                startActivity(intent);
            }
        });
    }

    private boolean isPasswordValid(String password) {
        return password.length() >= 6 &&
                password.matches(".*\\d.*") && // at least one numeric digit
                password.matches(".*[!@#$%^&*(),.?\":{}|<>].*"); // at least one special character
    }

    private void sendVerificationEmail(String email, String password, String username) {
        mAuth.createUserWithEmailAndPassword(email, password)
                .addOnCompleteListener(this, task -> {
                    if (task.isSuccessful()) {
                        // User registered successfully
                        FirebaseUser user = mAuth.getCurrentUser();
                        if (user != null) {
                            // Send verification email
                            user.sendEmailVerification()
                                    .addOnCompleteListener(emailTask -> {
                                        if (emailTask.isSuccessful()) {
                                            Toast.makeText(SignUpActivity.this, R.string.verification_sent_success, Toast.LENGTH_SHORT).show();
                                            storeUserInDatabase(user, email, username, password);
                                        } else {
                                            Toast.makeText(SignUpActivity.this, R.string.verification_sent_fail, Toast.LENGTH_SHORT).show();
                                        }
                                    });
                        }
                    } else {
                        String errorMessage = R.string.registration_fail + task.getException().getMessage();
                        if (task.getException() instanceof FirebaseAuthUserCollisionException) {
                            Toast.makeText(SignUpActivity.this, R.string.email_exist, Toast.LENGTH_SHORT).show();
                        } else {
                            Toast.makeText(SignUpActivity.this, errorMessage, Toast.LENGTH_SHORT).show();
                        }
                    }
                });
    }

    // Store user information in Firebase Realtime Database
    private void storeUserInDatabase(FirebaseUser user, String email, String username, String password) {
        // Get a reference to the Firebase Realtime Database
        DatabaseReference databaseReference = FirebaseDatabase.getInstance("https://spendee-dd19f-default-rtdb.firebaseio.com/")
                .getReference("Users");

        // Create a Member object to store in the database
        String userId = user.getUid();
        String hashedPassword = BCrypt.hashpw(password, BCrypt.gensalt());

        Member newUser = new Member(userId, username, email, hashedPassword);

        // store user info based on userid
        databaseReference.child(userId).setValue(newUser)
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        Toast.makeText(SignUpActivity.this, R.string.account_create_successful, Toast.LENGTH_SHORT).show();
                        Intent intent = new Intent(SignUpActivity.this, LoginActivity.class);
                        startActivity(intent);
                    } else {
                        Toast.makeText(SignUpActivity.this, R.string.account_create_fail, Toast.LENGTH_SHORT).show();
                    }
                });
    }
}
