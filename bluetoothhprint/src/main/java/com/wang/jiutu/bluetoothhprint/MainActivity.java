package com.wang.jiutu.bluetoothhprint;

import android.view.Menu;
        import java.io.IOException;
        import java.io.OutputStream;
        import java.util.ArrayList;
        import java.util.List;
        import java.util.Set;
        import java.util.UUID;
        import android.os.Bundle;
        import android.app.Activity;
        import android.app.AlertDialog.Builder;
        import android.bluetooth.*;
        import android.content.DialogInterface;
        import android.content.Intent;
        import android.view.Menu;
        import android.view.MotionEvent;
        import android.view.View;
        import android.view.View.OnClickListener;
        import android.widget.ArrayAdapter;
        import android.widget.Button;
        import android.widget.EditText;
        import android.widget.Spinner;
        import android.widget.TextView;

public class MainActivity extends Activity {
    /** Called when the activity is first created. */

    private Button buttonPf=null;
    private Button buttonCash=null;
    private Button buttonCut=null;
    private Button buttonConnect=null;

    private EditText mprintfData=null;
    private TextView mprintfLog=null;
    private TextView mTipTextView=null;

    private Spinner mSpinner=null;
    private List<String>mpairedDeviceList=new ArrayList<String>();
    private ArrayAdapter<String>mArrayAdapter;

    private BluetoothAdapter mBluetoothAdapter = null;   //创建蓝牙适配器
    private BluetoothDevice mBluetoothDevice=null;
    private BluetoothSocket mBluetoothSocket=null;
    OutputStream mOutputStream=null;
    /*Hint: If you are connecting to a Bluetooth serial board then try using
     * the well-known SPP UUID 00001101-0000-1000-8000-00805F9B34FB. However
     * if you are connecting to an Android peer then please generate your own unique UUID.*/
    private static final UUID SPP_UUID = UUID.fromString("00001101-0000-1000-8000-00805F9B34FB");

    private Builder dialog=null;

    Set<BluetoothDevice>pairedDevices=null;
    private void PrintfLogs(String logs){
        this.mprintfLog.setText(logs);
    }
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        this.setTitle(this.getTitle()+"(C)XPrinter.net");
        setContentView(R.layout.activity_main);

        buttonPf=(Button)findViewById(R.id.buttonPrintTest);
        buttonCash=(Button)findViewById(R.id.ButtonOpenCash);
        buttonCut=(Button)findViewById(R.id.ButtonCutPaper);
        buttonConnect=(Button)findViewById(R.id.buttonConnect);

        mSpinner=(Spinner)findViewById(R.id.deviceSpinner);

        mprintfData=(EditText)findViewById(R.id.InputText);
        mprintfLog=(TextView)findViewById(R.id.TextLogs);
        mTipTextView=(TextView)findViewById(R.id.textTip);

        mBluetoothAdapter=BluetoothAdapter.getDefaultAdapter();
        ButtonListener buttonListener=new ButtonListener();
        buttonPf.setOnClickListener(buttonListener);
        buttonCash.setOnClickListener(buttonListener);
        buttonCut.setOnClickListener(buttonListener);
        buttonConnect.setOnClickListener(buttonListener);
        setButtonEnadle(false);

        dialog=new Builder(this);
        dialog.setTitle("XPrinter hint:");
        dialog.setMessage(getString(R.string.XPrinterhint));
        dialog.setPositiveButton("Yes", new DialogInterface.OnClickListener() {

            @Override
            public void onClick(DialogInterface dialog, int which) {
                // TODO Auto-generated method stub
                startActivity(new Intent(android.provider.Settings.ACTION_SETTINGS));
                //finish();
            }
        });

        dialog.setNeutralButton("No", new DialogInterface.OnClickListener() {

            @Override
            public void onClick(DialogInterface dialog, int which) {
                // TODO Auto-generated method stub
                //finish();
            }
        });


        mpairedDeviceList.add(this.getString(R.string.PlsChoiceDevice));
        mArrayAdapter=new ArrayAdapter<String>(this, android.R.layout.simple_spinner_dropdown_item,mpairedDeviceList);
        mSpinner.setAdapter(mArrayAdapter);
        mSpinner.setOnTouchListener(new Spinner.OnTouchListener(){
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                // TODO Auto-generated method stub
                if (event.getAction()!=MotionEvent.ACTION_UP) {
                    return false;
                }
                try {
                    if (mBluetoothAdapter==null) {
                        mTipTextView.setText(getString(R.string.NotBluetoothAdapter));
                        PrintfLogs(getString(R.string.NotBluetoothAdapter));
                    }
                    else if (mBluetoothAdapter.isEnabled()) {
                        String getName=mBluetoothAdapter.getName();
                        pairedDevices=mBluetoothAdapter.getBondedDevices();
                        while (mpairedDeviceList.size()>1) {
                            mpairedDeviceList.remove(1);
                        }
                        if (pairedDevices.size()==0) {
                            dialog.create().show();
                        }
                        for (BluetoothDevice device : pairedDevices) {
                            // Add the name and address to an array adapter to show in a ListView
                            getName=device.getName() + "#" + device.getAddress();
                            mpairedDeviceList.add(getName);
                        }
                    }
                    else {
                        PrintfLogs("BluetoothAdapter not open...");
                        dialog.create().show();
                    }
                }
                catch (Exception e) {
                    // TODO: handle exception
                    mprintfData.setText(e.toString());
                }
                return false;
            }
        });
    }

    private void setButtonEnadle(boolean state) {
        buttonPf.setEnabled(state);
        buttonCash.setEnabled(state);
        buttonCut.setEnabled(state);
    }

    class ButtonListener implements OnClickListener{

        @Override
        public void onClick(View v) {

            switch (v.getId()) {
                case R.id.buttonConnect:{
                    String temString=(String) mSpinner.getSelectedItem();
                    if (mSpinner.getSelectedItemId()!=0) {
                        if (buttonConnect.getText()!=getString(R.string.Disconnected)) {
                            try {
                                mOutputStream.close();
                                mBluetoothSocket.close();
                                buttonConnect.setText(getString(R.string.Disconnected));
                                setButtonEnadle(false);
                            } catch (Exception e) {
                                // TODO: handle exception
                                PrintfLogs(e.toString());
                            }
                            return;
                        }
                        temString=temString.substring(temString.length()-17);
                        try {
                            buttonConnect.setText(getString(R.string.Connecting));
                            mBluetoothDevice=mBluetoothAdapter.getRemoteDevice(temString);
                            mBluetoothSocket=mBluetoothDevice.createRfcommSocketToServiceRecord(SPP_UUID);
                            mBluetoothSocket.connect();
                            buttonConnect.setText(getString(R.string.Connected));
                            setButtonEnadle(true);
                        } catch (Exception e) {
                            // TODO: handle exception
                            PrintfLogs(getString(R.string.Disconnected));
                            buttonConnect.setText(getString(R.string.Disconnected));
                            setButtonEnadle(false);
                            PrintfLogs(getString(R.string.ConnectFailed)+e.toString());
                        }

                    }
                    else {
                        PrintfLogs("Pls select a bluetooth device...");
                    }
                }
                break;
                case R.id.buttonPrintTest:{
                    try {
                        if (mprintfData.getTextSize()==0) {
                            PrintfLogs("Pls input print data...");
                        }
                        mOutputStream=mBluetoothSocket.getOutputStream();
                        mOutputStream.write((mprintfData.getText().toString()+"--->"+getString(R.string.app_name)+getString(R.string.Logs)+"\n").getBytes("GBK"));
                        mOutputStream.flush();
                        PrintfLogs("Data sent successfully...");
                    } catch (IOException e) {
                        // TODO Auto-generated catch block
                        e.printStackTrace();
                        PrintfLogs(getString(R.string.PrintFaild)+e.getMessage());
                    }


                }
                break;
                case R.id.ButtonOpenCash:{
                    try {
                        if (mprintfData.getTextSize()==0) {
                            PrintfLogs("Pls input print data...");
                        }
                        mOutputStream=mBluetoothSocket.getOutputStream();
                        mOutputStream.write(new byte[]{0x1b,0x70,0x00,0x1e,(byte)0xff,0x00});
                        mOutputStream.flush();
                        PrintfLogs("Data sent successfully...");
                    } catch (IOException e) {
                        // TODO Auto-generated catch block
                        e.printStackTrace();
                        PrintfLogs(getString(R.string.OpenCashFaild)+e.getMessage());
                    }
                }
                break;
                case R.id.ButtonCutPaper:{
                    try {
                        if (mprintfData.getTextSize()==0) {
                            PrintfLogs("Pls input print data...");
                        }
                        mOutputStream=mBluetoothSocket.getOutputStream();
                        mOutputStream.write(new byte[]{0x0a,0x0a,0x1d,0x56,0x01});
                        mOutputStream.flush();
                        PrintfLogs("Data sent successfully...");
                    } catch (IOException e) {
                        // TODO Auto-generated catch block
                        e.printStackTrace();
                        PrintfLogs(getString(R.string.CutPaperFaild)+e.getMessage());
                    }

                }
                break;
                default:
                    break;
            }

        }

    }
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        //getMenuInflater().inflate(R.menu.main, menu);
        return true;
    }

}

