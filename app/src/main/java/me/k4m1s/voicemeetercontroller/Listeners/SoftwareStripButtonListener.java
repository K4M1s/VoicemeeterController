package me.k4m1s.voicemeetercontroller.Listeners;

import android.view.View;

import me.k4m1s.voicemeetercontroller.MainActivity;
import me.k4m1s.voicemeetercontroller.R;
import me.k4m1s.voicemeetercontroller.VMSoftwareStrip;

public class SoftwareStripButtonListener implements View.OnClickListener {
    VMSoftwareStrip strip;

    public SoftwareStripButtonListener(VMSoftwareStrip strip) {
        this.strip = strip;
    }

    public void onClick(View v) {
        switch(v.getId()) {
            case R.id.btn_bus_a1:
                strip.setBusA1State(!strip.getBusA1State());
                break;
            case R.id.btn_bus_a2:
                strip.setBusA2State(!strip.getBusA2State());
                break;
            case R.id.btn_bus_a3:
                strip.setBusA3State(!strip.getBusA3State());
                break;
            case R.id.btn_bus_b1:
                strip.setBusB1State(!strip.getBusB1State());
                break;
            case R.id.btn_bus_b2:
                strip.setBusB2State(!strip.getBusB2State());
                break;
            case R.id.btn_mono:
                strip.setMonoState(!strip.getMonoState());
                break;
            case R.id.btn_solo:
                strip.setSoloState(!strip.getSoloState());
                break;
            case R.id.btn_mute:
                strip.setMuteState(!strip.getMuteState());
                break;
        }
        MainActivity.getInstance().sendState(this.strip);
    }
}
