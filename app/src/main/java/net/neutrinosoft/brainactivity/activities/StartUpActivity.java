package net.neutrinosoft.brainactivity.activities;

import android.bluetooth.BluetoothAdapter;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.support.v4.app.FragmentActivity;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import net.neutrinosoft.brainactivity.R;
import net.neutrinosoft.brainiac.BrainiacManager;
import net.neutrinosoft.brainiac.FftValue;
import net.neutrinosoft.brainiac.Value;
import net.neutrinosoft.brainiac.callback.OnConnectCallback;
import net.neutrinosoft.brainiac.callback.OnReceiveDataCallback;
import net.neutrinosoft.brainiac.callback.OnReceiveFftDataCallback;

import java.util.Arrays;


public class StartUpActivity extends FragmentActivity implements View.OnClickListener {


    private TextView statusLabel;
    private TextView frequencyLabel;
    private Button connectBtn;
    private Button plusFrequency;
    private Button minusFrequency;
    private Button startTest;

    private int frequency = 3;

    public static final int REQUEST_ENABLE_BT = 1;
    public static final String ACTION_NEW_VALUE = "ACTION_NEW_VALUE";
    public static final String ACTION_FFT_VALUE = "ACTION_FFT_VALUES";

    private BrainiacManager brainiacManager;

    private OnConnectCallback onConnectCallback = new OnConnectCallback() {
        @Override
        public void onConnectSuccess() {
            connectBtn.setText(R.string.disconnect);
            statusLabel.setText("Connected");

        }

        @Override
        public void onConnectFailed() {
            Handler handler = new Handler();
            handler.postDelayed(new Runnable() {
                @Override
                public void run() {
                    connectBtn.setText(R.string.connect_to_device);
                    statusLabel.setText(R.string.no_devices_found);
                }
            }, 10000);
        }
    };


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_startup);
        statusLabel = (TextView) findViewById(R.id.statusLabel);
        frequencyLabel = (TextView) findViewById(R.id.frequency);
        connectBtn = (Button) findViewById(R.id.connectBtn);
        plusFrequency = (Button) findViewById(R.id.plusFrequency);
        minusFrequency = (Button) findViewById(R.id.minusFrequency);
        startTest = (Button) findViewById(R.id.startTest);

        Button showPlotBtn = (Button) findViewById(R.id.showPlotBtn);
        showPlotBtn.setOnClickListener(this);

        connectBtn.setOnClickListener(this);
        minusFrequency.setOnClickListener(this);
        plusFrequency.setOnClickListener(this);
        startTest.setOnClickListener(this);




        brainiacManager = BrainiacManager.getBrainiacManager(this);
        brainiacManager.setOnReceiveDataCallback(new OnReceiveDataCallback() {
            @Override
            public void onReceiveData(Value value) {
                final Intent rawDataIntent = new Intent(ACTION_NEW_VALUE);
                rawDataIntent.putExtra(MainActivity.EXTRA_VALUES, value);
                sendBroadcast(rawDataIntent);
            }
        });

        brainiacManager.setOnReceiveFftDataCallback(new OnReceiveFftDataCallback() {
            @Override
            public void onReceiveData(FftValue[] fftValues) {
                Log.d("FFT-DATA", Arrays.toString(fftValues));
                final Intent fftDataIntent = new Intent(ACTION_FFT_VALUE);
                fftDataIntent.putExtra(MainActivity.EXTRA_VALUES, fftValues);
                sendBroadcast(fftDataIntent);
            }
        });
    }


    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == REQUEST_ENABLE_BT) {
            if (resultCode == RESULT_OK) {
                brainiacManager.startScan(onConnectCallback);
            } else {
                Toast.makeText(this, getString(R.string.please_enable_bluetooth), Toast.LENGTH_LONG).show();
            }
        }
    }


    @Override
    protected void onDestroy() {
        super.onDestroy();
        brainiacManager.release();
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.showPlotBtn: {
                startActivity(MainActivity.createIntent(this));
                break;
            }
            case R.id.connectBtn: {
                startTest.setText(R.string.start_test);
                brainiacManager.stopTest();
                BluetoothAdapter bluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
                if (bluetoothAdapter != null && bluetoothAdapter.isEnabled()) {
                    if (bluetoothAdapter.isDiscovering()) {
                        brainiacManager.stopScan();
                        connectBtn.setText(R.string.connect_to_device);
                        statusLabel.setText(R.string.ready_to_scan);
                    } else if (brainiacManager.isConnected()) {
                        brainiacManager.release();
                        connectBtn.setText(R.string.connect_to_device);
                        statusLabel.setText(R.string.ready_to_scan);
                    } else {
                        brainiacManager.startScan(onConnectCallback);
                        statusLabel.setText(R.string.scanning_started);
                        connectBtn.setText(R.string.stop);
                    }
                } else {
                    Intent enableBtIntent = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
                    startActivityForResult(enableBtIntent, REQUEST_ENABLE_BT);
                }
                break;
            }
            case R.id.minusFrequency: {
                if (frequency == 4) {
                    minusFrequency.setEnabled(false);
                } else if (frequency == 24) {
                    plusFrequency.setEnabled(true);
                }
                frequencyLabel.setText(String.valueOf(--frequency));
                break;
            }
            case R.id.plusFrequency: {
                if (frequency == 23) {
                    plusFrequency.setEnabled(false);
                } else if (frequency == 3) {
                    minusFrequency.setEnabled(true);
                }
                frequencyLabel.setText(String.valueOf(++frequency));
                break;
            }
            case R.id.startTest: {
                if (brainiacManager.isInTestMode()) {
                    startTest.setText(R.string.start_test);
                    brainiacManager.stopTest();
                } else {
                    startTest.setText(R.string.stop);
                    brainiacManager.startTest(frequency);
                }
                break;
            }
        }
    }
}
