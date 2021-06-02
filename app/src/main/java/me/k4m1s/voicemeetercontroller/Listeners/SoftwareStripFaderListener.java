package me.k4m1s.voicemeetercontroller.Listeners;

import android.os.Handler;
import android.widget.SeekBar;
import android.widget.TextView;

import me.k4m1s.voicemeetercontroller.MainActivity;
import me.k4m1s.voicemeetercontroller.VMHardwareStrip;
import me.k4m1s.voicemeetercontroller.VMSoftwareStrip;

public class SoftwareStripFaderListener implements SeekBar.OnSeekBarChangeListener {

    private VMSoftwareStrip strip;
    private TextView label;

    public SoftwareStripFaderListener(VMSoftwareStrip strip, TextView label) {
        this.strip = strip;
        this.label = label;
    }

    @Override
    public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
        int val = (progress * (seekBar.getHeight() - 2 * seekBar.getThumbOffset())) / seekBar.getMax();
        label.setText("" + ((progress - 600)/10.0f));
        label.setY(seekBar.getY() + seekBar.getHeight() - val - 36);
        new Handler().postDelayed(() -> label.setX(seekBar.getX() + (seekBar.getWidth()/2f) - (label.getWidth()/2f)), 1);
        MainActivity.getInstance().sendState(this.strip);
    }

    @Override
    public void onStartTrackingTouch(SeekBar seekBar) {

    }

    @Override
    public void onStopTrackingTouch(SeekBar seekBar) {

    }
}
