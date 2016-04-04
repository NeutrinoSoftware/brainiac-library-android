# Overview
The Brainiac library handles connections and data transfer between Braniac (alpha title) accessory and Android device.

#Requirements

Android 4.3+

# Version
0.1

# Getting started

1. Install.
2. Initialize BrainiacManager.
3. Set OnReceiveDataCallback.
4. Set OnFftDataCallback.
5. Connect to accessory

## 1. Install.

Gradle: 
```groovy
compile 'net.neutrinosoft.brainiac:brainiac:0.6'
```

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
5. timeframe - number of milliseconds passed from 1970 year for each packet - long
6. counter - serial internal order number identifying number of data packet - long
7. hardwareOrderNumber - hardware order number identifying number of data packet - int

##FftValue
FftValue - class which represents transformed values. It contains a few fields:

1. data1 - dominant frequency value for current time range and frequencies range 3-7 Hz - int
2. data2 - dominant frequency value for current time range and frequencies range 7-13 Hz - int
3. data3 - dominant frequency value for current time range and frequencies range 14-24 Hz - int
4. timeframe - number of milliseconds passed from 1970 year for each packet - long
5. counter - serial internal order number identifying number of data packet - long

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

###isInTestMode
Indicates whether BrainiacManager launched in test mode. Returns true if instance is in test mode, false otherwise

```java
public boolean isInTestMode()
```

###startTest
Starts sending test data values via delegate methods (without using hardware accessory). Starts dispatching data via delegate methods immediately. Use this method to test correct data receiving sequences and draw sample data plots.

*Parameter*

* **frequency** - value of testing frequency.
```java
public void startTest(final int frequency)
```

###stopTest
Stop sending test data
```java
public void stopTest()
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

###processGreenChannel
Returns flag which define the state of examined man detecting if the state of brain activity is full and active. Defined as: When closing the eyes or with simple contemplation of neutral images dominant frequency in the range of alpha (7-13 Hz) has no oscillations more than 20% for 3 minutes during the registration process. Method returns flag for such activity for last 5 sec (so app should call this method each 5 sec to get trend activity)

*Parameter*
* **channel** - Number for processing channel (1-4)
```java
public boolean processGreenChannel(int channel)
```

###processYellowForChannel
Returns flag which define the state of examined man detecting if the state of brain activity is Relaxation brain activity (EEG spectrum for simply “nice” relaxation, during which the person can not adequately drive or write software). Defined as: the dominant frequency in the range of alpha (7-13 Hz) increases in amplitude (power spectrum) on greater than 20% but less than 30% within 3 minutes. Method returns flag for such activity for last 5 sec (so app should call this method each 5 sec to get trend activity)

*Parameter*
* **channel** - Number for processing channel (1-4)
```java
public boolean processYellowForChannel(int channel)
```

###processRed1ForChannel
Returns flag which define the state of examined man detecting if the state of brain activity is Excessive stimulation of neurons and therefore the beginning of inappropriate, excessive actions. Defined as: the dominant frequency (range) of alpha (7-13 Hz) is reduced in amplitude (power spectrum) on greater than 20% for 3 minutes. Method returns flag for such activity for last 5 sec (so app should call this method each 5 sec to get trend activity)

*Parameter*
* **channel** - Number for processing channel (1-4)
```java
public boolean processRed1ForChannel(int channel)
```

###processRed2ForChannel
Returns flag which define the state of examined man detecting if the state of brain activity is in super relaxation. Defined as: the dominant frequency (range) of alpha (7-13 Hz) is increasing in amplitude (power spectrum) on greater than 30% for 3 minutes. Method returns flag for such activity for last 5 sec (so app should call this method each 5 sec to get trend activity)

*Parameter*
* **channel** - Number for processing channel (1-4)
```java
public boolean processRed2ForChannel(int channel)
```
