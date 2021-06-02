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
import me.k4m1s.voicemeetercontroller.Listeners.BusButtonListener;
import me.k4m1s.voicemeetercontroller.Listeners.BusFaderListener;
import me.k4m1s.voicemeetercontroller.Listeners.HardwareStripButtonListener;
import me.k4m1s.voicemeetercontroller.Listeners.HardwareStripFaderListener;

public class VMBus extends LinearLayout {

    TextView stripName;
    int stripID;

    VerticalSeekBar gainFader;

    Button monoButton;
    Boolean monoState = false;

    Button eqButton;
    Boolean eqState = false;

    Button muteButton;
    Boolean muteState = false;

    ProgressBar vumeterL;
    ProgressBar vumeterR;

    Drawable buttonDrawable;
    Drawable buttonSelectedDrawable;

    BusButtonListener busButtonListener;
    BusFaderListener busFaderListener;

    private boolean ignoreNextSync = false;

    public VMBus(Context context) {
        super(context);
    }

    public VMBus(Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
        init(context, attrs);
    }

    private void init(Context context, AttributeSet attrs) {
        inflate(context, R.layout.bus_view, this);

        TypedArray typedArray = context.obtainStyledAttributes(attrs, R.styleable.VMBus, 0, 0);
        CharSequence name = typedArray.getText(R.styleable.VMBus_bStripName);
        int ID = typedArray.getInt(R.styleable.VMBus_bStripID, -1);
        typedArray.recycle();

        initComponents();

        buttonDrawable = getResources().getDrawable(R.drawable.button_border);
        buttonSelectedDrawable = getResources().getDrawable(R.drawable.button_border_selected);

        setStripName(name);
        setStripID(ID);
        System.out.println("STRIPE ID: " + ID);
    }

    private void initComponents() {

        busButtonListener = new BusButtonListener(this);
        busFaderListener = new BusFaderListener(this, findViewById(R.id.gain_fader_value));

        stripName = findViewById(R.id.stripName);
        gainFader = findViewById(R.id.gain_fader);
        monoButton = findViewById(R.id.btn_mono);
        monoButton.setOnClickListener(busButtonListener);
        eqButton = findViewById(R.id.btn_eq);
        eqButton.setOnClickListener(busButtonListener);
        muteButton = findViewById(R.id.btn_mute);
        muteButton.setOnClickListener(busButtonListener);

        vumeterL = findViewById(R.id.vumeterL);
        vumeterL.setProgress(0);
        vumeterR = findViewById(R.id.vumeterR);
        vumeterR.setProgress(0);

        gainFader.setOnSeekBarChangeListener(busFaderListener);
    }

    public CharSequence getStripName() {
        return stripName.getText();
    }

    public void setStripName(CharSequence value) {
        stripName.setText(value);
    }
    public void setStripID(int value) { this.stripID = value; }

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

    public Boolean getEQState() {
        return eqState;
    }

    public void setEQState(Boolean soloState) {
        MainActivity.getInstance().runOnUiThread(()-> {
            this.eqState = soloState;
            changeBackground(this.eqButton, soloState);
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
        return String.format(Locale.US, "BS%d:%.2f,%.2f,%b,%b,%b", stripID, getFaderValue(), 0f, getMonoState(), getEQState(), getMuteState());
    }

    public void setData(String data) {
        String[] values = data.split(Pattern.quote(","));
        setFaderValue(Float.parseFloat(values[0]));
        // 1 is volume value
        setMonoState( Boolean.parseBoolean(values[2]) );
        setEQState( Boolean.parseBoolean(values[3]) );
        setMuteState( Boolean.parseBoolean(values[4]) );
    }

    public void setIgnoreNextSync(boolean value) {
        ignoreNextSync = value;
    }

    public void setDataSync(String data) {
        if (ignoreNextSync) {
            ignoreNextSync = false;
            return;
        }
        setData(data);
    }
}
