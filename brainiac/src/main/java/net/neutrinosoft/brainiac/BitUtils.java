package net.neutrinosoft.brainiac;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;

public class BitUtils {


    public static short getShortFromLittleBytes(byte b, byte b1) {
        ByteBuffer bb = ByteBuffer.allocate(2);
        bb.order(ByteOrder.LITTLE_ENDIAN);
        bb.put(b);
        bb.put(b1);
        return bb.getShort(0);
    }

    public static short getShortFromBigBytes(byte b, byte b1) {
        ByteBuffer bb = ByteBuffer.allocate(2);
        bb.order(ByteOrder.BIG_ENDIAN);
        bb.put(b);
        bb.put(b1);
        return bb.getShort(0);
    }

}
