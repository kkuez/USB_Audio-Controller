import com.sun.tools.javac.Main;

import javax.usb.event.UsbPipeDataEvent;
import javax.usb.event.UsbPipeErrorEvent;
import javax.usb.event.UsbPipeListener;
import java.util.HashSet;
import java.util.Set;

public class CustomUsbPipeListener implements UsbPipeListener {
    //TODO gedoppelt mit Main
    static final int DEVICE_INPUT_BYTES = 0;  //Length of the incoming bytes the device sends at once
    static final int HAMA_INPUT_BYTES = 8;  //Length of the incoming bytes the device sends at once
    static final int WINGMAN_INPUT_BYTES = 3;  //Length of the incoming bytes the device sends at once

    byte[] lastProcessedBytes = null;
    Set<Byte> validInputBytes = new HashSet<>(); //define!
    int CURRENT_INPUT_DEVICE_BYTES;


    public CustomUsbPipeListener(int CURRENT_INPUT_DEVICE_BYTES){
        this.CURRENT_INPUT_DEVICE_BYTES = CURRENT_INPUT_DEVICE_BYTES;
        validInputBytes.add((byte) 1);
        validInputBytes.add((byte) -1);
        validInputBytes.add((byte) 2);
        validInputBytes.add((byte) -2);
    }

    @Override
    public void errorEventOccurred(UsbPipeErrorEvent event) {

    }

    @Override
    public void dataEventOccurred(UsbPipeDataEvent event) {
        byte[] incomingBytes = event.getData();
        if(incomingBytes != lastProcessedBytes) {
            lastProcessedBytes = incomingBytes;
            Key pressedKey = parseKeyOrNull(incomingBytes);
            if (pressedKey != null) {
                System.out.println(pressedKey.toString());
            }
        }
    }

    public Key parseKeyOrNull(byte[] bytes){
        Key outputKey = null;
        switch (CURRENT_INPUT_DEVICE_BYTES){
            case WINGMAN_INPUT_BYTES:
                outputKey = parseWingmanKey(bytes);
                break;
            case HAMA_INPUT_BYTES:
                outputKey = parseHAMAKey(bytes);
                break;
        }
        return outputKey;
    }

    public Key parseHAMAKey(byte[] incomingBytes){
        Key outputKey = null;
        if(incomingBytes[0] != 127){
            //bytes[0] & [1] is control cross
            switch (incomingBytes[0]){
                case 0:
                    outputKey = Key.LEFT;
                    break;
                case -1:
                    outputKey = Key.RIGHT;
                    break;

            }
        }else {
            if (incomingBytes[1] != 127) {
                switch (incomingBytes[1]){
                    case 0:
                        outputKey = Key.UP;
                        break;
                    case -1:
                        outputKey = Key.DOWN;
                        break;
                }
            } else {
                if (incomingBytes[5] != 15) {
                    //bytes[5] is button cross
                    switch (incomingBytes[5]) {
                        case -113:
                            outputKey = Key.Y;
                            break;
                        case 31:
                            outputKey = Key.B;
                            break;
                        case 47:
                            outputKey = Key.A;
                            break;
                        case 79:
                            outputKey = Key.X;
                            break;
                    }
                }
            }
        }
        return outputKey;
    }

    public Key parseWingmanKey(byte[] bytes){
        Key outputKey = null;

        for(int i = 0; i< CURRENT_INPUT_DEVICE_BYTES; i++){
            byte incomingByte = bytes[i];
            if(hasValidInput(incomingByte)){
                int incomingInt = Byte.toUnsignedInt(incomingByte);
                switch (incomingByte){
                    case 1:
                        if(i == 1){
                            outputKey =  Key.LEFT;
                        }else{
                            if(i == 2){
                                outputKey = Key.UP;
                            }
                        }
                        break;
                    case -1:
                        break;
                    case 2:
                        break;
                    case -2:
                        if(i == 1){
                            outputKey =  Key.RIGHT;
                        }else{
                            if(i == 2){
                                outputKey = Key.DOWN;
                            }
                        }
                        break;
                }
            }
        }
        return outputKey;
    }

    public boolean hasValidInput(byte inputByte){
        if(validInputBytes.contains(inputByte)){
            return true;
        }
        return false;
    }

    public enum Key{
        UP, DOWN, LEFT, RIGHT, X, Y, A, B
    }
}
