package net.neutrinosoft.brainiac;

import android.os.Parcel;
import android.os.Parcelable;

/**
 * FftValue  class which represents transformed values.
 */
public class FftValue implements Parcelable {
    private int data1;
    private int data2;
    private int data3;

    public FftValue() {
    }

    protected FftValue(Parcel in) {
        data1 = in.readInt();
        data2 = in.readInt();
        data3 = in.readInt();
    }

    public static final Creator<FftValue> CREATOR = new Creator<FftValue>() {
        @Override
        public FftValue createFromParcel(Parcel in) {
            return new FftValue(in);
        }

        @Override
        public FftValue[] newArray(int size) {
            return new FftValue[size];
        }
    };

    public int getData3() {
        return data3;
    }

    public void setData3(int data3) {
        this.data3 = data3;
    }

    public int getData2() {
        return data2;
    }

    public void setData2(int data2) {
        this.data2 = data2;
    }

    public int getData1() {

        return data1;
    }

    public void setData1(int data1) {
        this.data1 = data1;
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel parcel, int i) {
        parcel.writeInt(data1);
        parcel.writeInt(data2);
        parcel.writeInt(data3);
    }

    public static FftValue[] createFromParcelableArray(Parcelable[] parcelables) {

        FftValue[] values = new FftValue[parcelables.length];
        for (int i = 0; i < parcelables.length; i++) {
            values[i] = (FftValue) parcelables[i];
        }
        return values;
    }

}
