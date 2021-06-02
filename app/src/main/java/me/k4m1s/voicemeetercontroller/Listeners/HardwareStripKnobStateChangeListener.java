package me.k4m1s.voicemeetercontroller.Listeners;

import it.beppi.knoblibrary.Knob;
import me.k4m1s.voicemeetercontroller.MainActivity;
import me.k4m1s.voicemeetercontroller.VMHardwareStrip;

public class HardwareStripKnobStateChangeListener implements Knob.OnStateChanged {

    private VMHardwareStrip strip;

    public HardwareStripKnobStateChangeListener(VMHardwareStrip strip) {
        this.strip = strip;
    }

    @Override
    public void onState(int state) {
        MainActivity.getInstance().sendState(this.strip);
    }
}
