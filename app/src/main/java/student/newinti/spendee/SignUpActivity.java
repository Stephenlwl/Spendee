package student.newinti.spendee;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

import com.google.firebase.FirebaseApp;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseAuthUserCollisionException;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

public class SignUpActivity extends AppCompatActivity {
    //  declare the id variables
    private EditText email_input, password_input, confirm_password_input, username_input;
    private FirebaseAuth mAuth;

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
        Button sign_up_button = findViewById(R.id.sign_up_button);
        TextView login_link = findViewById(R.id.login_link);

        sign_up_button.setOnClickListener(v -> {
            String username = username_input.getText().toString().trim();
            String email = email_input.getText().toString().trim();
            String password = password_input.getText().toString().trim();
            String confirm_password = confirm_password_input.getText().toString().trim();

            // Check if the passwords match
            if (!password.equals(confirm_password)) {
                Toast.makeText(SignUpActivity.this, R.string.confirm_password_incorrect_toast, Toast.LENGTH_SHORT).show();
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
        Member newUser = new Member(userId, username, email, password);

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
