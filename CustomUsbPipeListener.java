package main.java;

import javax.usb.event.UsbPipeDataEvent;
import javax.usb.event.UsbPipeErrorEvent;
import javax.usb.event.UsbPipeListener;

public class CustomUsbPipeListener implements UsbPipeListener {
    byte[] lastProcessedBytes = null;


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

    public static Key parseKeyOrNull(byte[] bytes){
        Key outputKey = null;
        switch (Main.CURRENT_INPUT_DEVICE_BYTES){
            case Main.WINGMAN_INPUT_BYTES:
                outputKey = parseWingmanKey(bytes);
                break;
            case Main.HAMA_INPUT_BYTES:
                outputKey = parseHAMAKey(bytes);
                break;
        }
        return outputKey;
    }

    public static Key parseHAMAKey(byte[] incomingBytes){
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

    public static Key parseWingmanKey(byte[] bytes){
        Key outputKey = null;

        for(int i =0;i<Main.CURRENT_INPUT_DEVICE_BYTES;i++){
            byte incomingByte = bytes[i];
            if(Main.hasValidInput(incomingByte)){
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

    enum Key{
        UP, DOWN, LEFT, RIGHT, X, Y, A, B
    }
}
