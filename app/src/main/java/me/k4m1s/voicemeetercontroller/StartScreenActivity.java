package me.k4m1s.voicemeetercontroller;

import android.content.Intent;
import android.os.Bundle;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

public class StartScreenActivity extends AppCompatActivity {

    private String errorMessage;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.start_screen_activity);

        Bundle b = getIntent().getExtras();
        if (b != null && b.containsKey("error")) {
            errorMessage = b.getString("error");
        }




        EditText editText = findViewById(R.id.computerAddress);
        Button connectButton = findViewById(R.id.connectButton);
        TextView errorText = findViewById(R.id.errorText);

        if (errorMessage != null) {
            errorText.setText(errorMessage);
        }

        connectButton.setOnClickListener((clickListener) -> {

            String computerIP = editText.getText().toString();
            if (computerIP.isEmpty()) {
                return;
            }

            Intent i = new Intent(this, MainActivity.class);
            i.putExtra("computerIP", computerIP);
            startActivity(i);
            finish();
        });

    }

}
