package com.wang.jiutu.usbprint;

import android.app.Activity;

import java.io.UnsupportedEncodingException;

import android.os.Bundle;
import android.app.Activity;
import android.view.Menu;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.EditText;

public class MainActivity extends Activity {
    private Button buttonCon = null;
    private Button buttonPf = null;
    private Button buttonCash = null;
    private Button buttonCut = null;
    private EditText mprintfData = null;
    private EditText mprintfLog = null;
    public UsbAdmin mUsbAdmin = null;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        buttonCon = (Button) findViewById(R.id.conTest);
        buttonPf = (Button) findViewById(R.id.printf);
        buttonCash = (Button) findViewById(R.id.buttonCash);
        buttonCut = (Button) findViewById(R.id.buttonCut);
        mprintfData = (EditText) findViewById(R.id.printfData);
        mprintfLog = (EditText) findViewById(R.id.printfLog);
        ButtonListener buttonListener = new ButtonListener();
        buttonCon.setOnClickListener(buttonListener);
        buttonPf.setOnClickListener(buttonListener);
        buttonCash.setOnClickListener(buttonListener);
        buttonCut.setOnClickListener(buttonListener);
        mUsbAdmin = new UsbAdmin(this);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.main, menu);
        return true;
    }

    class ButtonListener implements OnClickListener {

        @Override
        public void onClick(View v) {
            switch (v.getId()) {
                case R.id.conTest:
                    if (conTest()) {
                        PrintfLog("USB连接成功...");
                        buttonCon.setText("已打开USB");
                        buttonPf.setEnabled(true);
                        buttonCash.setEnabled(true);
                        buttonCut.setEnabled(true);
                    } else {
                        PrintfLog("连接失败...");
                        buttonCon.setText("打开失败");
                        buttonPf.setEnabled(false);
                        buttonCash.setEnabled(false);
                        buttonCut.setEnabled(false);
                    }
                    break;
                case R.id.printf:
                    try {
                        if (PrintfData((mprintfData.getText().toString() + "\nExample4USB-XPrinter\n").getBytes("GBK"))) {
                            PrintfLog("打印成功...");
                        } else {
                            PrintfLog("打印失败...");
                            buttonPf.setEnabled(false);
                        }
                    } catch (UnsupportedEncodingException e) {
                        e.printStackTrace();
                        PrintfLog("数据发送错误...");
                    }
                    break;
                case R.id.buttonCash:
                    byte SendCash[] = {0x1b, 0x70, 0x00, 0x1e, (byte) 0xff, 0x00};
                    if (PrintfData(SendCash)) {
                        PrintfLog("打开成功...");
                    } else {
                        PrintfLog("打开失败...");
                    }
                    break;
                case R.id.buttonCut:
                    byte SendCut[] = {0x0a, 0x0a, 0x1d, 0x56, 0x01};
                    if (PrintfData(SendCut)) {
                        PrintfLog("切纸成功...");
                    } else {
                        PrintfLog("切纸失败...");
                    }
                    break;
                default:
                    break;
            }

        }
    }

    public boolean conTest() {
        mUsbAdmin.Openusb();
        if (!mUsbAdmin.GetUsbStatus()) {
            return false;
        } else {
            return true;
        }
    }

    public void PrintfLog(String logString) {
        mprintfLog.setText(logString);
    }

    public boolean PrintfData(byte[] data) {
        if (!mUsbAdmin.sendCommand(data)) {
            return false;
        } else {
            return true;
        }
    }
}

