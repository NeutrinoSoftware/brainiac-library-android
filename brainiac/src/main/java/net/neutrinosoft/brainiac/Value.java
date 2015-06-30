package net.neutrinosoft.brainiac;

import android.os.Parcel;
import android.os.Parcelable;

public class   Value implements Parcelable {
    private double channel1;
    private double channel2;
    private double channel3;
    private double channel4;

    public Value(Parcel in) {
        channel1 = in.readDouble();
        channel2 = in.readDouble();
        channel3 = in.readDouble();
        channel4 = in.readDouble();
    }

    public static final Creator<Value> CREATOR = new Creator<Value>() {
        @Override
        public Value createFromParcel(Parcel in) {
            return new Value(in);
        }

        @Override
        public Value[] newArray(int size) {
            return new Value[size];
        }
    };

    public Value() {

    }

    public double getChannel1() {
        return channel1;
    }

    public void setChannel1(double channel1) {
        this.channel1 = channel1;
    }

    public double getChannel2() {
        return channel2;
    }

    public void setChannel2(double channel2) {
        this.channel2 = channel2;
    }

    public double getChannel3() {
        return channel3;
    }

    public void setChannel3(double channel3) {
        this.channel3 = channel3;
    }

    public double getChannel4() {
        return channel4;
    }

    public void setChannel4(double channel4) {
        this.channel4 = channel4;
    }

    public float[] toFloatArray() {
        float[] array = new float[4];
        array[0] = (float) channel1;
        array[1] = (float) channel2;
        array[2] = (float) channel3;
        array[3] = (float) channel4;
        return array;
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel parcel, int i) {
        parcel.writeDouble(channel1);
        parcel.writeDouble(channel2);
        parcel.writeDouble(channel3);
        parcel.writeDouble(channel4);
    }


}
