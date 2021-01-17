// -- MAIN ACTIVITY --

package com.example.quizmasteradmin;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.app.Dialog;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;


// Login Activity
// Enable authentication on firebase and connect the app
// Add the authentication and database dependencies in the project

public class MainActivity extends AppCompatActivity {

    // Declaring variables
    private EditText email, pass;
    private Button login;
    private FirebaseAuth firebaseAuth;
    private Dialog loadingDialog;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        // Initializing variables
        email = findViewById(R.id.email);
        pass = findViewById(R.id.password);
        login = findViewById(R.id.login);

        // Progress bar
        loadingDialog = new Dialog(MainActivity.this);
        loadingDialog.setContentView(R.layout.loading_progress_bar);
        loadingDialog.setCancelable(false);
        loadingDialog.getWindow().setBackgroundDrawableResource(R.drawable.progress_bg);
        loadingDialog.getWindow().setLayout(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT);

        // Initialising firebaseAuth variable
        firebaseAuth = FirebaseAuth.getInstance();

        // Setting actions to be performed on pressing login button
        login.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                // Validation checks
                // Checking if input values are null
                if(email.getText().toString().isEmpty()) {
                    // Show error message
                    email.setError("Enter Email ID");
                    return;
                }
                else {
                    // Setting error as null if it is not empty
                    email.setError(null);
                }
                if(pass.getText().toString().isEmpty()) {
                    // Show error message
                    pass.setError("Enter Password");
                    return;
                }
                else {
                    // Setting error as null if it is not empty
                    pass.setError(null);
                }

                // Calling function to verify details and login to the app
                firebaseLogin();

            }
        });

        // Checking if user is already logged in
        if(firebaseAuth.getCurrentUser() != null) {
            Intent intent = new Intent(MainActivity.this, CategoryActivity.class);
            startActivity(intent);
            finish();
        }

    }

    private void firebaseLogin() {

        // Display progress bar while authenticating
        loadingDialog.show();

        // API for signing into the app
        firebaseAuth.signInWithEmailAndPassword(email.getText().toString(), pass.getText().toString())
                .addOnCompleteListener(this, new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {
                        if (task.isSuccessful()) {
                            // Sign in success, update UI with the signed-in user's information
                            // Display success message
                            Toast.makeText(MainActivity.this, "Login Successful!", Toast.LENGTH_SHORT).show();

                            // Move to next activity
                            Intent intent = new Intent(MainActivity.this, CategoryActivity.class);
                            startActivity(intent);
                            finish();

                        } else {
                            // If sign in fails, display a message to the user.
                            Toast.makeText(MainActivity.this, "Login Failed!", Toast.LENGTH_SHORT).show();
                            // ...
                        }

                        // Dismissing progress bar
                        loadingDialog.dismiss();
                        // ...
                    }
                });
    }

}