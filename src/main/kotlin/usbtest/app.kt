/******************************************************************************
 * kotlin-usb4java-test                                                       *
 *                                                                            *
 * Just a small test project for a Kotlin based usb4java project with Gradle  *
 * as build-tool.                                                             *
 *                                                                            *
 * Copyright (c) 2015 Daniel-Kurashige-Gollub, daniel@kurashige-gollub.de     *
 *                                                                            *
 *                                                                            *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell  *
 * copies of the Software, and to permit persons to whom the Software is      *
 * furnished to do so, subject to the following conditions:                   *
 *                                                                            *
 *                                                                            *
 *                                                                            *
 * The above copyright notice and this permission notice shall be included in *
 * all copies or substantial portions of the Software.                        *
 *                                                                            *
 *                                                                            *
 *                                                                            *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR *
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,   *
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT.  IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER     *
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN  *
 * THE SOFTWARE.                                                              *
 *                                                                            *
 *                                                                            *
 *                                                                            *
 ******************************************************************************/

package usbtest

import javax.usb.UsbConfiguration
import javax.usb.UsbDevice
import javax.usb.UsbDeviceDescriptor
import javax.usb.UsbEndpoint
import javax.usb.UsbException
import javax.usb.UsbHostManager
import javax.usb.UsbHub
import javax.usb.UsbInterface
import javax.usb.UsbPort
import javax.usb.UsbServices

// see http://usb4java.org/quickstart/javax-usb.html for more information

// also: used https://github.com/usb4java/usb4java-javax-examples as basis for the implementation

object Main {

    @JvmStatic
    fun main(args: Array<String>) {

        val services: UsbServices = UsbHostManager.getUsbServices()

        println("USB Service Implementation: "
                + services.getImpDescription())
        println("Implementation version: "
                + services.getImpVersion())
        println("Service API version: " + services.getApiVersion())
        println()

        // Dump the root USB hub
        dumpDeviceNames(services.getRootUsbHub())
    }

    @JvmStatic
    private fun dumpDeviceNames(device: UsbDevice)
    {
        // Get the USB services and dump information about them
        val services: UsbServices = UsbHostManager.getUsbServices()

        // Dump the root USB hub
        processDevice(services.getRootUsbHub())
    }

    @JvmStatic
    private fun processDevice(device: UsbDevice)
    {
        // When device is a hub then process all child devices
        if (device.isUsbHub())
        {
            val hub: UsbHub = device as UsbHub
            for (child in hub.getAttachedUsbDevices() as List<UsbDevice>)
            {
                processDevice(child)
            }
        }
        // When device is not a hub then dump its name.
        else
        {
            try
            {
                dumpName(device);
            }
            catch(e: Exception)
            {
                // On Linux this can fail because user has no write permission
                // on the USB device file. On Windows it can fail because
                // no libusb device driver is installed for the device
                println("Ignoring problematic device: " + e);
            }
        }
    }

    // based on https://github.com/usb4java/usb4java-javax-examples/blob/master/src/main/java/org/usb4java/javax/examples/DumpNames.java
    /**
     * Dumps the names of all USB devices by using the javax-usb API. On
     * Linux this can only work when your user has write permissions on all the USB
     * device files in /dev/bus/usb (Running this example as root will work). On
     * Windows this can only work for devices which have a libusb-compatible driver
     * installed. On OSX this usually works without problems.
     *
     * @author Klaus Reimer <k@ailis.de>
     */
    @JvmStatic
    private fun dumpName(device: UsbDevice)
    {
        // Read the string descriptor indices from the device descriptor.
        // If they are missing then ignore the device.
        val desc: UsbDeviceDescriptor = device.getUsbDeviceDescriptor()
        val iManufacturer: Byte = desc.iManufacturer()
        val iProduct: Byte = desc.iProduct()

        if (iManufacturer.equals(0) || iProduct.equals(0)) {
            return
        }

        // Dump the device name
        println(device.getString(iManufacturer) + " " + device.getString(iProduct))
    }


    @JvmStatic
    private fun dumpDevice(device: UsbDevice)
    {
        // Dump information about the device itself
        println(device)

        var port: UsbPort? = device.getParentUsbPort()

        if (port != null)
        {
            println("Connected to port: " + port.getPortNumber())
            println("Parent: " + port.getUsbHub())
        }

        // Dump device descriptor
        println(device.getUsbDeviceDescriptor())

        // Process all configurations
        for (configuration: UsbConfiguration in device.getUsbConfigurations() as List<UsbConfiguration>)
        {
            // Dump configuration descriptor
            println(configuration.getUsbConfigurationDescriptor())

            // Process all interfaces
            for (iface: UsbInterface in configuration.getUsbInterfaces() as List<UsbInterface>)
            {
                // Dump the interface descriptor
                println(iface.getUsbInterfaceDescriptor())

                // Process all endpoints
                for (endpoint: UsbEndpoint in iface.getUsbEndpoints() as List<UsbEndpoint>)
                {
                    // Dump the endpoint descriptor
                    println(endpoint.getUsbEndpointDescriptor())
                }
            }
        }

        println("-----")

        // Dump child devices if device is a hub
        if (device.isUsbHub())
        {
            println("Is usb hub!")
            val hub: UsbHub = device as UsbHub;
            for (child in hub.getAttachedUsbDevices() as List<UsbDevice>)
            {
                dumpDevice(child);
            }
        }
    }
}
