import commands.MediaKeys;

import javax.usb.*;
import java.io.UnsupportedEncodingException;
import java.util.List;
import javax.sound.sampled.*;

public class Main {

    static final int DEVICE_INPUT_BYTES = 0;  //Length of the incoming bytes the device sends at once
    static final int HAMA_INPUT_BYTES = 8;  //Length of the incoming bytes the device sends at once
    static final int WINGMAN_INPUT_BYTES = 3;  //Length of the incoming bytes the device sends at once

    static final int CURRENT_INPUT_DEVICE_BYTES = HAMA_INPUT_BYTES;


    public static void main(String[] args) {
	// write your code here
        try {
            UsbServices usbServices = UsbHostManager.getUsbServices();
            UsbHub usbHub = usbServices.getRootUsbHub();
            for(UsbDevice device: (List<UsbDevice>) usbHub.getAttachedUsbDevices()){
                    UsbConfiguration usbConfiguration = device.getActiveUsbConfiguration();
                    UsbInterface usbInterface = usbConfiguration.getUsbInterface((byte) 0);
                    //usbInterface.claim();
                usbInterface.claim(new UsbInterfacePolicy()
                {
                    @Override
                    public boolean forceClaim(UsbInterface usbInterface)
                    {
                        return true;
                    }
                });
                try
                {
                    //Evenutally pass another usbEndpoint from the list
                    UsbEndpoint usbEndpoint = (UsbEndpoint) usbInterface.getUsbEndpoints().get(1);
                    UsbPipe usbPipe = usbEndpoint.getUsbPipe();
                    CustomUsbPipeListener customUsbPipeListener = new CustomUsbPipeListener(CURRENT_INPUT_DEVICE_BYTES);
                    usbPipe.addUsbPipeListener(customUsbPipeListener);
                    usbPipe.open();
                    try {
                        //Gamepad sends 3 bytes
                        byte[] data = new byte[CURRENT_INPUT_DEVICE_BYTES];
                        while(usbPipe.isOpen()){
                            usbPipe.syncSubmit(data);
                            CustomUsbPipeListener.Key pressedKey = customUsbPipeListener.parseKeyOrNull(data);
                            if(pressedKey != null) {
                                switch (pressedKey){
                                    case UP:
                                        MediaKeys.volumeUp();
                                        break;
                                    case DOWN:
                                        MediaKeys.volumeDown();
                                        break;
                                    case LEFT:
                                        MediaKeys.songPrevious();
                                        break;
                                    case RIGHT:
                                        MediaKeys.songNext();
                                        break;
                                }
                            }
                            //usbPipe.asyncSubmit(data);
                            //Thread.sleep(50);
                        }
                    //} catch (InterruptedException e) {
                    //    e.printStackTrace();
                    } finally
                    {
                        usbPipe.close();
                    }

                }
                finally
                {
                    usbInterface.release();
                }
                    System.out.println(device.getManufacturerString());
            }
        } catch (UsbException e) {
            e.printStackTrace();
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
        }

    }





    public static UsbDevice findDevice(UsbHub hub, short vendorId, short productId)
    {
        for (UsbDevice device : (List<UsbDevice>) hub.getAttachedUsbDevices())
        {
            UsbDeviceDescriptor desc = device.getUsbDeviceDescriptor();
            if (desc.idVendor() == vendorId && desc.idProduct() == productId) return device;
            if (device.isUsbHub())
            {
                device = findDevice((UsbHub) device, vendorId, productId);
                if (device != null) return device;
            }
        }
        return null;
    }


}
