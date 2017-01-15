package pl.kit.context_aware.lemur.HeartDROID.actions;

import heart.Action;
import heart.State;
import pl.kit.context_aware.lemur.HeartDROID.Inference;
import pl.kit.context_aware.lemur.PhoneActions.RingModes;

/**
 * Created by Krzysiek on 2016-12-27.
 */

public class SetSound implements Action {

    @Override
    public void execute(State state) {
        String argument = String.valueOf(state.getValueOfAttribute("sound"));
        switch (argument){
            case "on":
                RingModes.normalMode(Inference.getmContext());
                break;
            case "off":
                RingModes.silentMode(Inference.getmContext());
                break;
            case "vibration":
                RingModes.vibrationsMode(Inference.getmContext());
                break;
        }

    }

}