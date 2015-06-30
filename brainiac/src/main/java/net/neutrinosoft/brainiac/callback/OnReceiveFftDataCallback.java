package net.neutrinosoft.brainiac.callback;

import net.neutrinosoft.brainiac.FftValue;

public interface OnReceiveFftDataCallback {
        void onReceiveData(FftValue[] fftValues);
}
