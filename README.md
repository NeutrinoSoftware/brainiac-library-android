# Overview 
The Brainiac library handles connections and data transfer between Braniac (alpha title) accessory and Android device.

#Requirements

Android 4.3+

# Version
0.1

# Getting started

1. Import library module to your android project.
2. Initialize BrainiacManager.
3. Set OnReceiveDataCallback.
4. Set OnFftDataCallback.
5. Connect to accessory

## 1. Import library module to your android project.

1. Download source from GitHub.
2. Open your project.
3. Use File->New->Import Module and choose brainiac library.

## 2. Initialize BrainiacManager.
```java
BrainiacManager brainiacManager = BrainiacManager.getBrainiacManager(this); // this - reference to instance of Context
```

## 3. Set OnReceiveDataCallback.
```java
brainiacManager.setOnReceiveDataCallback(new OnReceiveDataCallback() {
@Override
public void onReceiveData(Value value) {
//Do something with recived data
}
});
```

## 4. Set OnFftDataCallback.
```java
brainiacManager.setOnReceiveFftDataCallback(new OnReceiveFftDataCallback() {
@Override
public void onReceiveData(FftValue[] fftValues) {
//Do something with transformed data.
}
});
```

## 5. Connect to accessory
```java
BluetoothAdapter bluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
if (bluetoothAdapter.isEnabled()) {
if (brainiacManager.isConnected()) {
brainiacManager.release();
...
} else {
brainiacManager.startScan(onConnectCallback);
...
}
}
```

# Reference
## Value
Value - class which represents raw values received from Braniac accessory. It contains a few fields: 
1. channel1 - brain activity measure for channel 1 (T3) - double
2. channel2 - brain activity measure for channel 2 (O1) - double
3. channel3 - brain activity measure for channel 3 (T4) - double
4. channel4 - brain activity measure for channel 4 (O2) - double

##FftValue
FftValue - class which represents transformed values. It contains a few fields:
1. data1 - dominant frequency value for current time range and frequencies range 3-7 Hz - int
2. data2 - dominant frequency value for current time range and frequencies range 7-13 Hz - int
3. data3 - dominant frequency value for current time range and frequencies range 14-24 Hz - int

##OnConnectCallback
OnConnectCallback is used for handling connection state.

1. onConnectSuccess - callback is called after successful connection to Brainiac accessory.
2. onConnectFailed - callback is called after failed connection to Brainiac accessory.

##OnReceiveDataCallback
OnReceiveDataCallback is used for handling a new banch of raw data.

1.  void onReceiveData(Value value) - value contains raw data of every channel

##OnReceiveFftDataCallback
OnReceiveFftDataCallback is used for handling dominant frequencies.
1.  void onReceiveData(FftValue[] fftValues) - fft is array of 4 FftValue instances which represent dominant frequencies for every channel

##BrainiacManager
BrainiacManager - 


