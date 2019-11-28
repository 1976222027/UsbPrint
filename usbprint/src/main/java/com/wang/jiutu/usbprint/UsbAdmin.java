package com.wang.jiutu.usbprint;

import android.util.Log;

		import java.util.HashMap;
		import java.util.Iterator;

		import android.annotation.SuppressLint;
		import android.app.PendingIntent;
		import android.content.BroadcastReceiver;
		import android.content.Context;
		import android.content.Intent;
		import android.content.IntentFilter;
		import android.hardware.usb.UsbConstants;
		import android.hardware.usb.UsbDevice;
		import android.hardware.usb.UsbDeviceConnection;
		import android.hardware.usb.UsbEndpoint;
		import android.hardware.usb.UsbInterface;
		import android.hardware.usb.UsbManager;
		import android.util.Log;

public class UsbAdmin
{
	private static final String TAG = "UsbAdmin";
	private UsbManager mUsbManager;
	private UsbDevice mDevice;
	private UsbDeviceConnection mConnection;
	private UsbEndpoint mEndpointIntr;
	private static PendingIntent mPermissionIntent=null;
	private static final String ACTION_USB_PERMISSION=
			"com.android.example.USB_PERMISSION";

	@SuppressLint("NewApi")
	private void setDevice(UsbDevice device)
	{
		if(device!=null)
		{
			UsbInterface intf=null;
			UsbEndpoint ep=null;

			int InterfaceCount=device.getInterfaceCount();
			int j;

			mDevice = device;
			for(j=0;j<InterfaceCount;j++)
			{
				int i;

				intf = device.getInterface(j);
				Log.i(TAG,"接口是:"+j+"类是:"+intf.getInterfaceClass());
				if(intf.getInterfaceClass()==7)
				{
					int UsbEndpointCount=intf.getEndpointCount();
					for(i = 0;i < UsbEndpointCount;i++)
					{
						ep=intf.getEndpoint(i);
						Log.i(TAG,"端点是:"+i+"方向是:"+ep.getDirection()+"类型是:"+ep.getType());
						if(ep.getDirection() == 0 && ep.getType() == UsbConstants.USB_ENDPOINT_XFER_BULK)
						{
							Log.i(TAG,"接口是:"+j+"端点是:"+i);
							break;
						}
					}
					if(i != UsbEndpointCount)
					{
						break;
					}
				}
			}
			if(j == InterfaceCount)
			{
				Log.i(TAG,"没有打印机接口");
				return;
			}

			mEndpointIntr = ep;
			if (device != null)
			{
				UsbDeviceConnection connection = mUsbManager.openDevice(device);

				if (connection != null && connection.claimInterface(intf, true)) {
					Log.i(TAG,"打开成功！ ");
					mConnection = connection;

				} else {
					Log.i(TAG,"打开失败！ ");
					mConnection = null;
				}
			}
		}
		else
		{

		}
	}
	@SuppressLint("NewApi")
	public void Openusb()
	{
		if(mDevice!=null)
		{
			setDevice(mDevice);
			if(mConnection==null)
			{
				HashMap<String, UsbDevice> deviceList = mUsbManager.getDeviceList();
				Iterator<UsbDevice> deviceIterator = deviceList.values().iterator();

				while(deviceIterator.hasNext()){
					UsbDevice device=deviceIterator.next();
					mUsbManager.requestPermission(device, mPermissionIntent);
				}
			}
		}
		else
		{
			HashMap<String, UsbDevice> deviceList = mUsbManager.getDeviceList();
			Iterator<UsbDevice> deviceIterator = deviceList.values().iterator();

			while(deviceIterator.hasNext()){
				UsbDevice device=deviceIterator.next();
				mUsbManager.requestPermission(device, mPermissionIntent);
			}
		}
	}
	@SuppressLint("NewApi")
	public void Closeusb()
	{
		if (mConnection != null)
		{
			mConnection.close();
			mConnection=null;
		}
	}
	public String GetUsbStatus(boolean Language)
	{
		if(mDevice==null)
		{
			if(Language)
				return "没有Usb设备！";
			else
				return "No Usb Device!";
		}
		if(mConnection==null)
		{
			if(Language)
				return "Usb设备不是打印机！";
			else
				return "Usb device is not a printer!";
		}
		if(Language)
			return "Usb打印机打开成功！";
		return "Usb Printer Open success！";
	}
	public boolean GetUsbStatus()
	{
		if(mConnection==null)
			return false;
		return true;
	}
	@SuppressLint("NewApi")
	public boolean sendCommand(byte [] Content)
	{
		boolean Result;
		synchronized (this)
		{
			int len=-1;
			if (mConnection != null)
			{
				len=mConnection.bulkTransfer(mEndpointIntr, Content, Content.length, 10000);
			}

			if(len<0)
			{
				Result=false;
				Log.i(TAG,"发送失败！ "+len);
			}
			else
			{
				Result=true;
				Log.i(TAG,"发送"+len+"字节数据");
			}
		}
		return Result;
	}
	private final BroadcastReceiver mUsbReceiver = new BroadcastReceiver() {
		public void onReceive(Context context, Intent intent) {
			String action = intent.getAction();
			if (ACTION_USB_PERMISSION.equals(action))
			{
				synchronized (this)
				{
					UsbDevice device = (UsbDevice)intent.getParcelableExtra(UsbManager.EXTRA_DEVICE);
					if (intent.getBooleanExtra(UsbManager.EXTRA_PERMISSION_GRANTED, false))
					{
						if(device != null)
						{
							setDevice(device);
						}
						else
						{
							Closeusb();
							mDevice=device;
						}
					}
					else
					{
						Log.d(TAG, "permission denied for device " + device);
					}

				}

			}
		}
	};
	public UsbAdmin(Context context)
	{
		mUsbManager = (UsbManager)context.getSystemService(Context.USB_SERVICE);
		mPermissionIntent=PendingIntent.getBroadcast(context,0,new Intent(ACTION_USB_PERMISSION), 0);
		IntentFilter filter= new IntentFilter(ACTION_USB_PERMISSION);
		context.registerReceiver(mUsbReceiver, filter);
	}
}