package me.k4m1s.voicemeetercontroller;

import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import android.content.DialogInterface;
import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.os.StrictMode;
import android.text.InputType;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.view.WindowManager;
import android.widget.EditText;

import java.io.IOException;
import java.net.SocketTimeoutException;
import java.util.Timer;
import java.util.TimerTask;

public class MainActivity extends AppCompatActivity {

    private static MainActivity instance;

    public ConnectionManager connectionManager;

    private String computerAddress;

    public VMHardwareStrip hardwareStrips[] = new VMHardwareStrip[3];
    public VMSoftwareStrip softwareStrips[] = new VMSoftwareStrip[2];
    public VMBus bus[] = new VMBus[5];

    private boolean isSendingBlocked = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        StrictMode.ThreadPolicy policy = new StrictMode.ThreadPolicy.Builder().permitAll().build();
        StrictMode.setThreadPolicy(policy);

        requestWindowFeature(Window.FEATURE_NO_TITLE);
        FullScreencall();

        setContentView(R.layout.activity_main);

        Bundle b = getIntent().getExtras();
        computerAddress = b.getString("computerIP");

        hardwareStrips[0] = findViewById(R.id.Strip0);
        hardwareStrips[1] = findViewById(R.id.Strip1);
        hardwareStrips[2] = findViewById(R.id.Strip2);
        softwareStrips[0] = findViewById(R.id.Strip3);
        softwareStrips[1] = findViewById(R.id.Strip4);

        bus[0] = findViewById(R.id.Bus0);
        bus[1] = findViewById(R.id.Bus1);
        bus[2] = findViewById(R.id.Bus2);
        bus[3] = findViewById(R.id.Bus3);
        bus[4] = findViewById(R.id.Bus4);

        getWindow().getDecorView().post(new Runnable() {

            @Override
            public void run() {
                if (connectionManager == null) {
                    try {
                        connectionManager = new ConnectionManager(computerAddress);
                    } catch(SocketTimeoutException e) {
                        changeScreenToStartScreen("Could not connect to the server.");
                    } catch (IOException exception) {
                        exception.printStackTrace();
                        changeScreenToStartScreen("Could not connect to the server.");
                    }
                }
            }

        });

        instance = this;
    }

    @Override
    public void onResume() {
        super.onResume();
        FullScreencall();
    }

    public void FullScreencall() {
        if(Build.VERSION.SDK_INT > 11 && Build.VERSION.SDK_INT < 19) { // lower api
            View v = this.getWindow().getDecorView();
            v.setSystemUiVisibility(View.SYSTEM_UI_FLAG_HIDE_NAVIGATION | View.SYSTEM_UI_FLAG_FULLSCREEN);
        } else if(Build.VERSION.SDK_INT >= 19) {
            //for new api versions.
            View decorView = getWindow().getDecorView();
            decorView.setSystemUiVisibility(
                    View.SYSTEM_UI_FLAG_IMMERSIVE
                    // Set the content to appear under the system bars so that the
                    // content doesn't resize when the system bars hide and show.
                    | View.SYSTEM_UI_FLAG_LAYOUT_STABLE
                    | View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION
                    | View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN
                    // Hide the nav bar and status bar
                    | View.SYSTEM_UI_FLAG_HIDE_NAVIGATION
                    | View.SYSTEM_UI_FLAG_FULLSCREEN
            );
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        finishActivity(0);
        System.exit(0);
    }

    public void changeScreenToStartScreen(String error) {
        try {
            this.connectionManager.destroy();
        } catch (IOException e) {
            e.printStackTrace();
        }
        Intent i = new Intent(this, StartScreenActivity.class);
        i.putExtra("error", error);
        startActivity(i);
        finish();
    }

    public void sendState(VMHardwareStrip strip) {
        if (connectionManager == null || connectionManager.getSocket() == null)
            return;
        connectionManager.getSocketCommunication().sendData(strip.toString());
    }

    public void sendState(VMSoftwareStrip strip) {
        if (connectionManager == null || connectionManager.getSocket() == null)
            return;
        connectionManager.getSocketCommunication().sendData(strip.toString());
    }

    public void sendState(VMBus bus) {
        if (connectionManager == null || connectionManager.getSocket() == null)
            return;
        connectionManager.getSocketCommunication().sendData(bus.toString());
    }

    public void blockSending() {
        isSendingBlocked = true;
    }

    public void unblockSending() {
        isSendingBlocked = false;
    }

    public boolean isSendingBlocked() {
        return isSendingBlocked;
    }

    public ConnectionManager getConnectionManager() {
        return this.connectionManager;
    }

    public static MainActivity getInstance() {
        return instance;
    }
}