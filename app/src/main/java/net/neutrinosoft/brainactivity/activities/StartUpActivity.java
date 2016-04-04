package net.neutrinosoft.brainactivity.activities;

import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.FragmentActivity;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import com.squareup.otto.Bus;

import net.neutrinosoft.brainactivity.R;
import net.neutrinosoft.brainactivity.common.BusProvider;
import net.neutrinosoft.brainiac.BrainiacManager;
import net.neutrinosoft.brainiac.FftValue;
import net.neutrinosoft.brainiac.Value;
import net.neutrinosoft.brainiac.callback.OnDeviceCallback;
import net.neutrinosoft.brainiac.callback.OnReceiveDataCallback;
import net.neutrinosoft.brainiac.callback.OnReceiveFftDataCallback;
import net.neutrinosoft.brainiac.callback.OnScanCallback;


public class StartUpActivity extends FragmentActivity implements View.OnClickListener {
    
    public static final String TAG = StartUpActivity.class.getSimpleName();
    
    private TextView statusLabel;
    private TextView frequencyLabel;
    private TextView battery;
    private Button connectBtn;
    private Button stopScanBtn;
    private Button disconnectBtn;
    private Button plusFrequency;
    private Button minusFrequency;
    private Button startTest;

    private int frequency = 3;

    public static final int REQUEST_ENABLE_BT = 1;
    public static final String ACTION_NEW_VALUE = "ACTION_NEW_VALUE";
    public static final String ACTION_FFT_VALUE = "ACTION_FFT_VALUES";

    private BrainiacManager brainiacManager;

    private OnScanCallback onScanCallback = new OnScanCallback() {

        @Override
        public void onScanStart() {
            runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    Log.d(TAG, "onScanStart()");
                    showStopScanView();
                }
            });
        }

        @Override
        public void onScanStop() {
            runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    Log.d(TAG, "onScanStop()");
                }
            });
        }

        @Override
        public void onScanFailed(int errorCode) {
            runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    Log.d(TAG, "onScanFailed()");
                    showConnectView();
                }
            });
        }
    };

    private OnDeviceCallback onDeviceCallback = new OnDeviceCallback() {
        @Override
        public void onDeviceFound(BluetoothDevice device) {
            runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    Log.d(TAG, "onDeviceFound()");
                    statusLabel.setText(R.string.device_found);
                }
            });
        }

        @Override
        public void onDeviceConnected(BluetoothDevice device) {
            runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    Log.d(TAG, "onDeviceConnected()");
                    showDisconnectView();
                }
            });
        }

        @Override
        public void onDeviceConnecting(final BluetoothDevice device) {
            runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    Log.d(TAG, "onDeviceConnecting()");
                    statusLabel.setText(String.format("%s %s", getString(R.string.connecting_to), device.getName()));
                }
            });
        }

        @Override
        public void onDeviceDisconnected(BluetoothDevice device) {
            runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    Log.d(TAG, "onDeviceDisconnected()");
                    showConnectView();
                }
            });
        }

        @Override
        public void onDeviceConnectionError(BluetoothDevice device) {
            runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    Log.d(TAG, "onDeviceConnectionError()");
                    showConnectView();
                }
            });
        }

        @Override
        public void onDeviceDisconnecting(final BluetoothDevice device) {
            runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    Log.d(TAG, "onDeviceDisconnecting()");
                    statusLabel.setText(R.string.disconnecting);
                }
            });
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_startup);

        statusLabel = (TextView) findViewById(R.id.statusLabel);
        frequencyLabel = (TextView) findViewById(R.id.frequency);
        battery = (TextView) findViewById(R.id.battery);
        connectBtn = (Button) findViewById(R.id.connectBtn);
        stopScanBtn = (Button) findViewById(R.id.stopScanBtn);
        disconnectBtn = (Button) findViewById(R.id.disconnectBtn);
        plusFrequency = (Button) findViewById(R.id.plusFrequency);
        minusFrequency = (Button) findViewById(R.id.minusFrequency);
        startTest = (Button) findViewById(R.id.startTest);

        Button showPlotBtn = (Button) findViewById(R.id.showPlotBtn);
        showPlotBtn.setOnClickListener(this);

        disconnectBtn.setVisibility(View.GONE);
        stopScanBtn.setVisibility(View.GONE);

        connectBtn.setOnClickListener(this);
        disconnectBtn.setOnClickListener(this);
        stopScanBtn.setOnClickListener(this);
        minusFrequency.setOnClickListener(this);
        plusFrequency.setOnClickListener(this);
        startTest.setOnClickListener(this);

        final Bus bus = BusProvider.getBus();
        brainiacManager = BrainiacManager.getBrainiacManager(this);
        brainiacManager.setOnScanCallback(onScanCallback);
        brainiacManager.setOnDeviceCallback(onDeviceCallback);
        brainiacManager.setOnReceiveDataCallback(new OnReceiveDataCallback() {
            @Override
            public void onReceiveData(final Value value) {
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        bus.post(value);
                    }
                });

            }
        });

        brainiacManager.setOnReceiveFftDataCallback(new OnReceiveFftDataCallback() {
            @Override
            public void onReceiveData(final FftValue[] fftValues) {
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        bus.post(fftValues);
                    }
                });
            }
        });

        showConnectView();

        throw new RuntimeException("Fabric Crashlytics Test");
    }


    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == REQUEST_ENABLE_BT) {
            if (resultCode == RESULT_OK) {
                connect();
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
    protected void onResume() {
        super.onResume();
        if (brainiacManager.isConnected()) {
            battery.setText(String.format("Battery Level: %d", brainiacManager.getBatteryLevel()));
        }
    }

    @Override
    public void onClick(View v) {
        BluetoothAdapter bluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
        switch (v.getId()) {
            case R.id.showPlotBtn: {
                startActivity(MainActivity.createIntent(this));
                break;
            }
            case R.id.connectBtn: {
                startTest.setText(R.string.start_test);
                brainiacManager.stopTest();

                if (bluetoothAdapter != null && bluetoothAdapter.isEnabled()) {
                    connect();
                } else {
                    Intent enableBtIntent = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
                    startActivityForResult(enableBtIntent, REQUEST_ENABLE_BT);
                }
                break;
            }
            case R.id.disconnectBtn: {
                disconnect();
                break;
            }
            case R.id.stopScanBtn: {
                stopScan();
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

    private void connect() {
        showStopScanView();
        brainiacManager.startScan();
    }

    private void disconnect() {
        brainiacManager.stopScan();
        brainiacManager.release();
        showConnectView();
    }

    private void stopScan() {
        brainiacManager.stopScan();
        showConnectView();
    }

    private void showStopScanView() {
        connectBtn.setVisibility(View.GONE);
        disconnectBtn.setVisibility(View.GONE);
        stopScanBtn.setVisibility(View.VISIBLE);
        statusLabel.setText(R.string.scanning_started);
    }

    private void showConnectView() {
        connectBtn.setVisibility(View.VISIBLE);
        disconnectBtn.setVisibility(View.GONE);
        stopScanBtn.setVisibility(View.GONE);
        statusLabel.setText(R.string.ready_to_scan);
    }

    private void showDisconnectView() {
        connectBtn.setVisibility(View.GONE);
        disconnectBtn.setVisibility(View.VISIBLE);
        stopScanBtn.setVisibility(View.GONE);
        statusLabel.setText(R.string.connected);
    }
}
