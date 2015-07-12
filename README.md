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
5. timeframe - number of milliseconds passed from 1970 year for each packet
6. counter - serial internal order number identifying number of data packet
7. hardwareOrderNumber - hardware order number identifying number of data packet 

##FftValue
FftValue - class which represents transformed values. It contains a few fields:

1. data1 - dominant frequency value for current time range and frequencies range 3-7 Hz - int
2. data2 - dominant frequency value for current time range and frequencies range 7-13 Hz - int
3. data3 - dominant frequency value for current time range and frequencies range 14-24 Hz - int
4. timeframe - number of milliseconds passed from 1970 year for each packet 
5. counter - serial internal order number identifying number of data packet

##OnConnectCallback
OnConnectCallback is used for handling connection state.

###onConnectSuccess
Callback is called after successful connection to Brainiac accessory.

```java
void onConnectSuccess()
```

###onConnectFailed
Callback is called after failed connection to Brainiac accessory.

```java
void onConnectFailed()
```

##OnReceiveDataCallback
OnReceiveDataCallback is used for handling a new bundle of raw data.

###onReceiveData
Callback is called once raw data received.

```java
void onReceiveData(Value value)
```
*Parameter*

* **value** - contains raw data of every channel

##OnReceiveFftDataCallback
OnReceiveFftDataCallback is used for handling dominant frequencies.

###onReceiveData
Callback is called once transformed data received.

```java
void onReceiveData(FftValue[] fftValues)
```
*Parameter*

* **fftValues** - is array of 4 FftValue instances which represent dominant frequencies for every channel


##BrainiacManager
The BrainiacManager class handles connections and data transfer between Braniac (alpha title) accessory and Android device.

###setOnReceiveFftDataCallback
Register a callback to be invoked when fft data received.

```java
public void setOnReceiveFftDataCallback(OnReceiveFftDataCallback onReceiveFftDataCallback)
```
*Parameter*

* **onReceiveFftDataCallback** - An implementation of OnReceiveFftDataCallback

###getBrainiacManager
Returns BrainiacManager singleton. 

```java
public static BrainiacManager getBrainiacManager(Context context)
```
*Parameter*

* **context** - application context

###setOnReceiveDataCallback
Register a callback to be invoked when raw data received.

```java
public void setOnReceiveDataCallback(OnReceiveDataCallback onReceiveDataCallback)
```
*Parameter*

* **onReceiveDataCallback** - An implementation of OnReceiveDataCallback

###startScan
Starts a scan for Brainiac devices.

```java
public void startScan(final OnConnectCallback onConnectCallback)
```
*Parameter*

* **onConnectCallback** - These callbacks may get called at any time, when connected to a brainiac device.

###isConnected
Indicates whether BrainiacManager connected to device. Return true if instance connected to device, false otherwise

```java
public boolean isConnected()
```

###stopScan
Stops an ongoing Bluetooth LE device scan.

```java
public void stopScan()
```

###release
Release all using resources

```java
public void release()
```



