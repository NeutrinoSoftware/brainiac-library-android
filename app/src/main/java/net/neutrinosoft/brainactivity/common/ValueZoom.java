package net.neutrinosoft.brainactivity.common;

/**
 * Created by movsesiv on 27/06/15.
 */
public enum ValueZoom {
    Twenty("20 μV", 20000), Forty("40 μV", 40000), OneHundred("100 μV", 100000), TwoHundred("200 μV", 200000), FourHundred("400 μV", 400000);

    private final String label;
    private final int zoomValue;

    public String getLabel() {
        return label;
    }

    public int getZoomValue() {
        return zoomValue;
    }

    ValueZoom(String s, int zoomValue) {
        label = s;
        this.zoomValue = zoomValue;
    }
}
