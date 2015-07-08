package net.neutrinosoft.brainiac.callback;

import net.neutrinosoft.brainiac.FftValue;

/**
 * OnReceiveFftDataCallback is used for handling dominant frequencies.
 */
public interface OnReceiveFftDataCallback {
    /**
     * Callback is called once transformed data received.
     *
     * @param fftValues is array of 4 FftValue instances which represent dominant frequencies for every channel
     */
    void onReceiveData(FftValue[] fftValues);
}
