package net.neutrinosoft.brainiac.callback;


import net.neutrinosoft.brainiac.Value;

/**
 * OnReceiveDataCallback is used for handling a new bundle of raw data.
 */
public interface OnReceiveDataCallback {
        /**
         * Callback is called once raw data received.
         * @param value contains raw data of every channel
         */
        void onReceiveData(Value value);

}
