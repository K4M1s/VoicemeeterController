package me.k4m1s.voicemeetercontroller;

import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.drawable.Drawable;
import android.graphics.drawable.GradientDrawable;
import android.os.Handler;
import android.os.Looper;
import android.util.AttributeSet;
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
import me.k4m1s.voicemeetercontroller.Listeners.SoftwareStripButtonListener;
import me.k4m1s.voicemeetercontroller.Listeners.SoftwareStripFaderListener;

public class VMSoftwareStrip extends LinearLayout {

    @StyleableRes
    int index0 = 0;

    TextView stripName;
    int stripID;

    Knob trebleKnob;
    Knob midKnob;
    Knob bassKnob;

    TextView trebleText;
    TextView midText;
    TextView bassText;

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

    SoftwareStripButtonListener softwareStripButtonListener;
    SoftwareStripFaderListener softwareStripFaderListener;

    private boolean ignoreNextSync = false;

    int knobSync = 0;

    public VMSoftwareStrip(Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
        init(context, attrs);
    }

    private void init(Context context, AttributeSet attrs) {
        inflate(context, R.layout.software_strip_view, this);

        TypedArray typedArray = context.obtainStyledAttributes(attrs, R.styleable.VMSoftwareStrip, 0, 0);
        CharSequence name = typedArray.getText(R.styleable.VMSoftwareStrip_sStripName);
        int ID = typedArray.getInt(R.styleable.VMSoftwareStrip_sStripID, -1);
        typedArray.recycle();

        initComponents();

        buttonDrawable = getResources().getDrawable(R.drawable.button_border);
        buttonSelectedDrawable = getResources().getDrawable(R.drawable.button_border_selected);

        setStripName(name);
        setStripID(ID);
    }

    private void initComponents() {

        softwareStripButtonListener = new SoftwareStripButtonListener(this);
        softwareStripFaderListener = new SoftwareStripFaderListener(this, findViewById(R.id.gain_fader_value));

        trebleKnob = findViewById(R.id.trebleKnob);
        trebleKnob.setOnStateChanged(state -> {
            MainActivity.getInstance().sendState(this);
            float value = getTrebleValue();
            trebleText.setText(String.format("%.1f", value));
            if (value < 0) {
                trebleText.setTextColor(getResources().getColor(R.color.vm_btn_selected));
            } else if (value == 0) {
                trebleText.setTextColor(getResources().getColor(R.color.vm_text));
            } else {
                trebleText.setTextColor(getResources().getColor(R.color.vm_btn_mute));
            }
        });
        trebleText = findViewById(R.id.trebleText);

        midKnob = findViewById(R.id.midKnob);
        midKnob.setOnStateChanged(state -> {
            MainActivity.getInstance().sendState(this);
            float value = getMidValue();
            midText.setText(String.format("%.1f", value));
            if (value < 0) {
                midText.setTextColor(getResources().getColor(R.color.vm_btn_selected));
            } else if (value == 0) {
                midText.setTextColor(getResources().getColor(R.color.vm_text));
            } else {
                midText.setTextColor(getResources().getColor(R.color.vm_btn_mute));
            }
        });
        midText = findViewById(R.id.midText);

        bassKnob = findViewById(R.id.bassKnob);
        bassKnob.setOnStateChanged(state -> {
            MainActivity.getInstance().sendState(this);
            float value = getBassValue();
            bassText.setText(String.format("%.1f", value));
            if (value < 0) {
                bassText.setTextColor(getResources().getColor(R.color.vm_btn_selected));
            } else if (value == 0) {
                bassText.setTextColor(getResources().getColor(R.color.vm_text));
            } else {
                bassText.setTextColor(getResources().getColor(R.color.vm_btn_mute));
            }

        });
        bassText = findViewById(R.id.bassText);

        stripName = findViewById(R.id.stripName);
        gainFader = findViewById(R.id.gain_fader);
        busA1Button = findViewById(R.id.btn_bus_a1);
        busA1Button.setOnClickListener(softwareStripButtonListener);
        busA2Button = findViewById(R.id.btn_bus_a2);
        busA2Button.setOnClickListener(softwareStripButtonListener);
        busA3Button = findViewById(R.id.btn_bus_a3);
        busA3Button.setOnClickListener(softwareStripButtonListener);
        busB1Button = findViewById(R.id.btn_bus_b1);
        busB1Button.setOnClickListener(softwareStripButtonListener);
        busB2Button = findViewById(R.id.btn_bus_b2);
        busB2Button.setOnClickListener(softwareStripButtonListener);
        monoButton = findViewById(R.id.btn_mono);
        monoButton.setOnClickListener(softwareStripButtonListener);
        soloButton = findViewById(R.id.btn_solo);
        soloButton.setOnClickListener(softwareStripButtonListener);
        muteButton = findViewById(R.id.btn_mute);
        muteButton.setOnClickListener(softwareStripButtonListener);

        vumeterL = findViewById(R.id.vumeterL);
        vumeterL.setProgress(0);

        vumeterR = findViewById(R.id.vumeterR);
        vumeterR.setProgress(0);

        gainFader.setOnSeekBarChangeListener(softwareStripFaderListener);
    }

    public CharSequence getStripName() {
        return stripName.getText();
    }

    public void setStripID(int value) { this.stripID = value; }

    public float getTrebleValue() {
        int val = trebleKnob.getState();
        return (val - 120) / 10f;
    }

    public void setTrebleValue(float value) {
        MainActivity.getInstance().runOnUiThread(()-> {
            int val = 120 + (int) (value * 10);
            trebleKnob.setState(val);
        });
    }

    public float getMidValue() {
        int val = midKnob.getState();
        return (val - 120) / 10f;
    }

    public void setMidValue(float value) {
        MainActivity.getInstance().runOnUiThread(()-> {
            int val = 120 + (int) (value * 10);
            midKnob.setState(val);
        });
    }

    public float getBassValue() {
        int val = bassKnob.getState();
        return (val - 120) / 10f;
    }

    public void setBassValue(float value) {
        MainActivity.getInstance().runOnUiThread(()-> {
            int val = 120 + (int) (value * 10);
            bassKnob.setState(val);
        });
    }

    public void setStripName(CharSequence value) {
        stripName.setText(value);
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
        return String.format(Locale.US, "SS%d:%.2f,%.2f,%.2f,%.2f,%.2f,%b,%b,%b,%b,%b,%b,%b,%b",
                stripID,
                getTrebleValue(),
                getMidValue(),
                getBassValue(),
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
        setTrebleValue( Float.parseFloat(values[0]) );
        setMidValue( Float.parseFloat(values[1]) );
        setBassValue( Float.parseFloat(values[2]) );
        setFaderValue( Float.parseFloat(values[3]) );
        // 4 is volume value
        setBusA1State( Boolean.parseBoolean(values[5]) );
        setBusA2State( Boolean.parseBoolean(values[6]) );
        setBusA3State( Boolean.parseBoolean(values[7]) );
        setBusB1State( Boolean.parseBoolean(values[8]) );
        setBusB2State( Boolean.parseBoolean(values[9]) );
        setMonoState( Boolean.parseBoolean(values[10]) );
        setSoloState( Boolean.parseBoolean(values[11]) );
        setMuteState( Boolean.parseBoolean(values[12]) );
    }

    public void setDataSync(String data) {
        if (ignoreNextSync) {
            ignoreNextSync = false;
            return;
        }
        setData(data);
    }
}
