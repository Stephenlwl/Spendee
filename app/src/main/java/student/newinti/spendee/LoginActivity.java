package student.newinti.spendee;

import android.os.Bundle;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import org.w3c.dom.Text;

public class LoginActivity extends AppCompatActivity {
    //  declare the id variables
    private EditText email_input, password_input;
    private Button login_button;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        email_input = findViewById(R.id.email_input);
        password_input = findViewById(R.id.password_input);
        login_button = findViewById(R.id.login_button);

        login_button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v){
                String email = email_input.getText().toString().trim();
                String password = password_input.getText().toString().trim();

//            For testing purpose where email and password not empty then able proceed
                if (!email.isEmpty() && !password.isEmpty()) {
                    Toast.makeText(LoginActivity.this, R.string.login_success_toast, Toast.LENGTH_SHORT).show();
                } else {
                    Toast.makeText(LoginActivity.this, R.string.login_fail_toast, Toast.LENGTH_SHORT).show();
                }
            }

        });


    }
}