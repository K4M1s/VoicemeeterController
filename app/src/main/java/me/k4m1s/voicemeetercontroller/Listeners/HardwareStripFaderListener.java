package me.k4m1s.voicemeetercontroller.Listeners;

import android.os.Handler;
import android.widget.SeekBar;
import android.widget.TextView;

import me.k4m1s.voicemeetercontroller.MainActivity;
import me.k4m1s.voicemeetercontroller.VMHardwareStrip;

public class HardwareStripFaderListener implements SeekBar.OnSeekBarChangeListener {

    private VMHardwareStrip strip;
    private TextView label;

    private long startTime;
    private int startVal;

    private long stopTime;
    private int stopVal;

    public HardwareStripFaderListener(VMHardwareStrip strip, TextView label) {
        this.strip = strip;
        this.label = label;
    }

    @Override
    public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
        int val = (progress * (seekBar.getHeight() - 2 * seekBar.getThumbOffset())) / seekBar.getMax();
        label.setText("" + ((progress - 600)/10.0f));
        label.setY(seekBar.getY() + seekBar.getHeight() - val - 36);
        new Handler().postDelayed(new Runnable() {
            @Override
            public void run() {
                label.setX(seekBar.getX() + (seekBar.getWidth()/2f) - (label.getWidth()/2f));
            }
        }, 1);
        MainActivity.getInstance().sendState(this.strip);
    }

    @Override
    public void onStartTrackingTouch(SeekBar seekBar) {
        startTime = System.currentTimeMillis();
        startVal = seekBar.getProgress();
        System.out.println("Start clicking");
    }

    @Override
    public void onStopTrackingTouch(SeekBar seekBar) {
        stopTime = System.currentTimeMillis();
        stopVal = seekBar.getProgress();

        if (stopTime - startTime > 2000) {
            if (Math.abs(startVal - stopVal) < 10) {
                strip.setFaderValue(0);
            }
        }
        System.out.println("Stop clicking");
    }
}
