package net.neutrinosoft.brainactivity.common;

public enum TimeZoom {
    One("1 sec", 250), Two("2 sec", 500), Four("4 sec", 1000), Five("5 sec", 1250);
    private String label;
    private int zoomValue;

    public String getLabel() {
        return label;
    }

    public int getZoomValue() {
        return zoomValue;
    }

    TimeZoom(String s, int zoomValue) {
        this.label = s;
        this.zoomValue = zoomValue;
    }
}
