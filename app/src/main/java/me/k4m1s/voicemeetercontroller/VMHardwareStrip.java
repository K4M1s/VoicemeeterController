package me.k4m1s.voicemeetercontroller;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.drawable.Drawable;
import android.graphics.drawable.GradientDrawable;
import android.os.Handler;
import android.os.Looper;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.View;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.VerticalSeekBar;

import androidx.annotation.Nullable;
import androidx.annotation.StyleableRes;

import java.util.Locale;
import java.util.regex.Pattern;

import it.beppi.knoblibrary.Knob;
import me.k4m1s.voicemeetercontroller.Listeners.HardwareStripButtonListener;
import me.k4m1s.voicemeetercontroller.Listeners.HardwareStripFaderListener;
import me.k4m1s.voicemeetercontroller.Listeners.HardwareStripKnobStateChangeListener;

public class VMHardwareStrip extends LinearLayout {

    @StyleableRes
    int index0 = 0;

    TextView stripName;
    int stripID;

    Knob compositionKnob;
    Knob gateKnob;

    VerticalSeekBar gainFader;

    Button busA1Button;
    Boolean busA1State = false;

    Button busA2Button;
    Boolean busA2State = false;

    Button busA3Button;
    Boolean busA3State = false;

    Button busB1Button;
    Boolean busB1State = false;

    Button busB2Button;
    Boolean busB2State = false;

    Button monoButton;
    Boolean monoState = false;

    Button soloButton;
    Boolean soloState = false;

    Button muteButton;
    Boolean muteState = false;

    ProgressBar vumeterL;
    ProgressBar vumeterR;

    Drawable buttonDrawable;
    Drawable buttonSelectedDrawable;

    HardwareStripButtonListener hardwareStripButtonListener;
    HardwareStripFaderListener hardwareStripFaderListener;
    HardwareStripKnobStateChangeListener hardwareStripKnobStateChangeListener;

    private boolean ignoreNextSync = false;

    public VMHardwareStrip(Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
        init(context, attrs);
    }

    @SuppressLint("UseCompatLoadingForDrawables")
    private void init(Context context, AttributeSet attrs) {
        inflate(context, R.layout.hardware_strip_view, this);

        TypedArray typedArray = context.obtainStyledAttributes(attrs, R.styleable.VMHardwareStrip, 0, 0);
        CharSequence name = typedArray.getText(R.styleable.VMHardwareStrip_hStripName);
        int ID = typedArray.getInt(R.styleable.VMHardwareStrip_hStripID, -1);
        typedArray.recycle();

        initComponents();

        buttonDrawable = getResources().getDrawable(R.drawable.button_border);
        buttonSelectedDrawable = getResources().getDrawable(R.drawable.button_border_selected);

        setStripName(name);
        setStripID(ID);
    }

    private void initComponents() {

        hardwareStripButtonListener = new HardwareStripButtonListener(this);
        hardwareStripFaderListener = new HardwareStripFaderListener(this, findViewById(R.id.gain_fader_value));
        hardwareStripKnobStateChangeListener = new HardwareStripKnobStateChangeListener(this);

        compositionKnob = findViewById(R.id.compKnob);
        compositionKnob.setOnStateChanged(hardwareStripKnobStateChangeListener);
        gateKnob = findViewById(R.id.gateKnob);
        gateKnob.setOnStateChanged(hardwareStripKnobStateChangeListener);
        stripName = findViewById(R.id.stripName);
        gainFader = findViewById(R.id.gain_fader);
        busA1Button = findViewById(R.id.btn_bus_a1);
        busA1Button.setOnClickListener(hardwareStripButtonListener);
        busA2Button = findViewById(R.id.btn_bus_a2);
        busA2Button.setOnClickListener(hardwareStripButtonListener);
        busA3Button = findViewById(R.id.btn_bus_a3);
        busA3Button.setOnClickListener(hardwareStripButtonListener);
        busB1Button = findViewById(R.id.btn_bus_b1);
        busB1Button.setOnClickListener(hardwareStripButtonListener);
        busB2Button = findViewById(R.id.btn_bus_b2);
        busB2Button.setOnClickListener(hardwareStripButtonListener);
        monoButton = findViewById(R.id.btn_mono);
        monoButton.setOnClickListener(hardwareStripButtonListener);
        soloButton = findViewById(R.id.btn_solo);
        soloButton.setOnClickListener(hardwareStripButtonListener);
        muteButton = findViewById(R.id.btn_mute);
        muteButton.setOnClickListener(hardwareStripButtonListener);

        vumeterL = findViewById(R.id.vumeterL);
        vumeterL.setProgress(0);

        vumeterR = findViewById(R.id.vumeterR);
        vumeterR.setProgress(0);

        gainFader.setOnSeekBarChangeListener(hardwareStripFaderListener);
    }

    public CharSequence getStripName() {
        return stripName.getText();
    }

    public void setStripName(CharSequence value) {
        stripName.setText(value);
    }
    public void setStripID(int value) { this.stripID = value; }

    public float getCompValue() {
        int val = compositionKnob.getState();
        return (val) / 10f;
    }

    public void setCompValue(float value) {
        MainActivity.getInstance().runOnUiThread(()-> {
            int val = (int) (value * 10);
            compositionKnob.setState(val);
        });
    }

    public float getGateValue() {
        int val = gateKnob.getState();
        return val / 10f;
    }

    public void setGateValue(float value) {
        MainActivity.getInstance().runOnUiThread(()-> {
            int val = (int) (value * 10);
            gateKnob.setState(val);
        });
    }

    public float getFaderValue() {
        int progress = gainFader.getProgress();
        return (progress - 600) / 10.0f;
    }

    public void setFaderValue(float value) {
        MainActivity.getInstance().runOnUiThread(()-> {
            int iValue = Math.round((value * 10) + 600);
            gainFader.setProgress(iValue);
            gainFader.onSizeChanged(gainFader.getWidth(), gainFader.getHeight(), 0, 0);
        });
    }

    public Boolean getBusA1State() {
        return busA1State;
    }

    public void setBusA1State(Boolean busA1State) {
        MainActivity.getInstance().runOnUiThread(()-> {
            this.busA1State = busA1State;
            changeBackground(this.busA1Button, busA1State);
        });
    }

    public Boolean getBusA2State() {
        return busA2State;
    }

    public void setBusA2State(Boolean busA2State) {
        MainActivity.getInstance().runOnUiThread(()-> {
            this.busA2State = busA2State;
            changeBackground(this.busA2Button, busA2State);
        });
    }

    public Boolean getBusA3State() {
        return busA3State;
    }

    public void setBusA3State(Boolean busA3State) {
        MainActivity.getInstance().runOnUiThread(()-> {
            this.busA3State = busA3State;
            changeBackground(this.busA3Button, busA3State);
        });
    }

    public Boolean getBusB1State() {
        return busB1State;
    }

    public void setBusB1State(Boolean busB1State) {
        MainActivity.getInstance().runOnUiThread(()-> {
            this.busB1State = busB1State;
            changeBackground(this.busB1Button, busB1State);
        });
    }

    public Boolean getBusB2State() {
        return busB2State;
    }

    public void setBusB2State(Boolean busB2State) {
        MainActivity.getInstance().runOnUiThread(()-> {
            this.busB2State = busB2State;
            changeBackground(this.busB2Button, busB2State);
        });
    }

    public Boolean getMonoState() {
        return monoState;
    }

    public void setMonoState(Boolean monoState) {
        MainActivity.getInstance().runOnUiThread(()-> {
            this.monoState = monoState;
            changeBackground(this.monoButton, monoState);
        });
    }

    public Boolean getMuteState() {
        return muteState;
    }

    public void setMuteState(Boolean muteState) {
        MainActivity.getInstance().runOnUiThread(()-> {
            this.muteState = muteState;
            if (this.muteState) {
                changeBackground(this.muteButton, getResources().getColor(R.color.vm_btn_mute));
                return;
            }
            changeBackground(this.muteButton, getResources().getColor(R.color.vm_btn));
        });
    }

    public Boolean getSoloState() {
        return soloState;
    }

    public void setSoloState(Boolean soloState) {
        MainActivity.getInstance().runOnUiThread(()-> {
            this.soloState = soloState;
            changeBackground(this.soloButton, soloState);
        });
    }

    public void setVUValue(int progressL, int progressR) {
        int cValue = vumeterL.getProgress();
        if (cValue < progressL) {
            vumeterL.setProgress(progressL);
        } else {
            if (cValue > 0)
                vumeterL.setProgress(cValue - 1);
        }

        cValue = vumeterR.getProgress();
        if (cValue < progressR) {
            vumeterR.setProgress(progressR);
        } else {
            if (cValue > 0)
                vumeterR.setProgress(cValue - 1);
        }

    }

    private void changeBackground(Button btn, Boolean state) {
        GradientDrawable drawable = (GradientDrawable) btn.getBackground();
        if (state) {
            drawable.setStroke(2, getResources().getColor(R.color.vm_btn_selected));
            btn.setTextColor(getResources().getColor(R.color.vm_btn_selected));
            return;
        }
        drawable.setStroke(2, getResources().getColor(R.color.vm_btn));
        btn.setTextColor(getResources().getColor(R.color.vm_btn));
    }

    private void changeBackground(Button btn, int color) {
        GradientDrawable drawable = (GradientDrawable) btn.getBackground();
        drawable.setStroke(2, color);
        btn.setTextColor(color);
    }

    public String toString() {
        return String.format(Locale.US, "HS%d:%.2f,%.2f,%.2f,%.2f,%b,%b,%b,%b,%b,%b,%b,%b",
                stripID,
                getCompValue(),
                getGateValue(),
                getFaderValue(),
                0f,
                getBusA1State(),
                getBusA2State(),
                getBusA3State(),
                getBusB1State(),
                getBusB2State(),
                getMonoState(),
                getSoloState(),
                getMuteState()
        );
    }

    public void setData(String data) {
        String[] values = data.split(Pattern.quote(","));
        setCompValue( Float.parseFloat(values[0]) );
        setGateValue( Float.parseFloat(values[1]) );
        setFaderValue( Float.parseFloat(values[2]) );
        // 3 is volume value
        setBusA1State( Boolean.parseBoolean(values[4]) );
        setBusA2State( Boolean.parseBoolean(values[5]) );
        setBusA3State( Boolean.parseBoolean(values[6]) );
        setBusB1State( Boolean.parseBoolean(values[7]) );
        setBusB2State( Boolean.parseBoolean(values[8]) );
        setMonoState( Boolean.parseBoolean(values[9]) );
        setSoloState( Boolean.parseBoolean(values[10]) );
        setMuteState( Boolean.parseBoolean(values[11]) );
    }

    public void setIgnoreNextSync(boolean value) {
        ignoreNextSync = value;
    }

    public void setDataSync(String data) {
        if (ignoreNextSync) {
            ignoreNextSync = false;
            System.out.println("Sync data ignored");
            return;
        }
        setData(data);
    }
}
