package main.java;

import org.usb4java.Device;


import javax.usb.*;
import javax.usb.event.UsbDeviceListener;
import javax.usb.event.UsbPipeDataEvent;
import javax.usb.event.UsbPipeErrorEvent;
import javax.usb.event.UsbPipeListener;
import java.io.UnsupportedEncodingException;
import java.nio.ByteBuffer;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class Main {

    static final int DEVICE_INPUT_BYTES = 0;  //Length of the incoming bytes the device sends at once
    static final int HAMA_INPUT_BYTES = 8;  //Length of the incoming bytes the device sends at once
    static final int WINGMAN_INPUT_BYTES = 3;  //Length of the incoming bytes the device sends at once

    static final int CURRENT_INPUT_DEVICE_BYTES = HAMA_INPUT_BYTES;

    static final Set<Byte> validInputBytes = new HashSet<>(); //define!

    public static void main(String[] args) {
	// write your code here
        try {
            validInputBytes.add((byte) 1);
            validInputBytes.add((byte) -1);
            validInputBytes.add((byte) 2);
            validInputBytes.add((byte) -2);

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
                    usbPipe.addUsbPipeListener(new CustomUsbPipeListener());
                    /*usbPipe.addUsbPipeListener(new UsbPipeListener() {
                        @Override
                        public void errorEventOccurred(UsbPipeErrorEvent event) {
                            //System.out.println(event.getUsbException().toString());
                        }

                        @Override
                        public void dataEventOccurred(UsbPipeDataEvent event) {

                        }
                    });*/
                    usbPipe.open();
                    try {
                        //Gamepad sends 3 bytes
                        byte[] data = new byte[CURRENT_INPUT_DEVICE_BYTES];
                        while(usbPipe.isOpen()){
                            usbPipe.syncSubmit(data);
                            CustomUsbPipeListener.Key pressedKey = CustomUsbPipeListener.parseKeyOrNull(data);
                            if(pressedKey != null) {
                                System.out.println(pressedKey.toString());
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



    public static boolean hasValidInput(byte inputByte){
        if(validInputBytes.contains(inputByte)){
            return true;
        }
        return false;
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
