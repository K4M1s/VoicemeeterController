package me.k4m1s.voicemeetercontroller.Listeners;

import android.view.View;

import me.k4m1s.voicemeetercontroller.MainActivity;
import me.k4m1s.voicemeetercontroller.R;
import me.k4m1s.voicemeetercontroller.VMBus;

public class BusButtonListener implements View.OnClickListener {

    VMBus strip;

    public BusButtonListener(VMBus strip) {
        this.strip = strip;
    }


    @Override
    public void onClick(View v) {
        switch(v.getId()) {
            case R.id.btn_mono:
                strip.setMonoState(!strip.getMonoState());
                break;
            case R.id.btn_eq:
                strip.setEQState(!strip.getEQState());
                break;
            case R.id.btn_mute:
                strip.setMuteState(!strip.getMuteState());
                break;
        }
        MainActivity.getInstance().sendState(this.strip);
    }
}
