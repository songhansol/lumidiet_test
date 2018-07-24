package com.doubleh.lumidiet.utils;

/**
 * Created by user-pc on 2016-10-03.
 */

public class BigEndianByteHandler {
    /**
     * short type을 byte로 변형
     **/
    public static final byte[] shortToByte( short s ) {
        byte[] dest = new byte[2];
        dest[1] = (byte)(s & 0xff);
        dest[0] = (byte)((s>>8) & 0xff);
        return dest;
    }
    /**
     * int type을 byte로 변형
     **/
    public static final byte[] intToByte( int i ) {
        byte[] dest = new byte[4];
        dest[3] = (byte)(i & 0xff);
        dest[2] = (byte)((i>>8) & 0xff);
        dest[1] = (byte)((i>>16) & 0xff);
        dest[0] = (byte)((i>>24) & 0xff);
        return dest;
    }
    /**
     * int type을 byte로 변형
     **/
    public static final byte[] intToByte2( int i ) {
        byte[] dest = new byte[2];
        dest[1] = (byte)(i & 0xff);
        dest[0] = (byte)((i>>8) & 0xff);
        return dest;
    }
    /**
     * long type을 byte로 변형
     **/
    public static final byte[] longToByte( long l ) {
        byte[] dest = new byte[8];
        dest[7] = (byte)(l & 0xff);
        dest[6] = (byte)((l>>8) & 0xff);
        dest[5] = (byte)((l>>16) & 0xff);
        dest[4] = (byte)((l>>24) & 0xff);
        dest[3] = (byte)((l>>32) & 0xff);
        dest[2] = (byte)((l>>40) & 0xff);
        dest[1] = (byte)((l>>48) & 0xff);
        dest[0] = (byte)((l>>56) & 0xff);
        return dest;
    }

    /**
     * double type을 byte로 변형
     **/
    public static final byte[] doubleToByte(double d) {
        long temp;
        byte dest[] = new byte[8];
        temp = Double.doubleToLongBits(d);
        dest = longToByte(temp);
        return dest;
    }

    /**
     * byte 배열을 short 형으로 변환한다.
     **/
    public static final short byteToShort( byte[] src, int offset ) {
        return (short)( ((src[offset]&0xff) << 8) | (src[offset+1]&0xff) ) ;
    }
    public static final short byteToShort( byte[] src){
        return byteToShort( src, 0 );
    }

    /**
     * byte 배열을 int 형으로 변환한다.
     **/
    public static final int byteToInt( byte[] src, int offset ) {
        return ((src[offset]&0xff) << 24) | ((src[offset+1]&0xff) << 16) |
                ((src[offset+2]&0xff) << 8) | (src[offset+3]&0xff) ;
    }
    public static final int byte2ToInt( byte[] src, int offset ) {
        return ((src[offset]&0xff) << 8) | (src[offset+1]&0xff) ;
    }
    public static final int byteToInt( byte[] src, int offset, int len ) {
        int intValue = 0;
        for(int i=0; i<len; i++)
            intValue |= (src[offset + i]&0xff) << 8*(3 - i);

        return intValue;
    }
    public static final int byteToInt(byte[] src){
        return byteToInt( src, 0 );
    }
    /**
     * byte 배열을 long 형으로 변환한다.
     **/
    public static final long byteToLong( byte[] src, int offset ) {
        return ((long) byteToInt(src,offset) << 32 ) |
                ((long) byteToInt(src, offset+4) & 0xffffffffL ) ;
    }
    public static final long byteToLong(byte[] src){
        return byteToLong( src, 0 );
    }
    /**
     * byte 배열을 double 형으로 변환한다.
     **/
    public static final double byteToDouble(byte[] buffer, int offset){
        long temp = byteToLong(buffer, offset);
        return Double.longBitsToDouble(temp);
    }
    public static final double byteToDouble(byte[] src){
        return byteToDouble( src, 0 );
    }

    /**
     * byte 배열에 short 값을 배정
     **/
    public static final byte[] setShort( byte[] dest, int offset, short s ) {
        dest[offset+1] = (byte)(s & 0xff);
        dest[offset] = (byte)((s>>8) & 0xff);
        return dest;
    }

    /**
     * byte 배열에 Int 값을 배정
     **/
    public static final byte[] setInt( byte[] dest, int offset, int i ) {
        dest[offset+3] = (byte)(i & 0xff);
        dest[offset+2] = (byte)((i>>8) & 0xff);
        dest[offset+1] = (byte)((i>>16) & 0xff);
        dest[offset] = (byte)((i>>24) & 0xff);
        return dest;
    }

    /**
     * byte 배열에 Long 값을 배정
     **/
    public static final byte[] setLong( byte[] dest, int offset, long l ) {
        setInt(dest, offset, (int)(l>>32) );
        setInt(dest, offset + 4, (int)(l & 0xffffffffL));
        return dest;
    }

    /**
     * byte array reverse
     * @param src   source byte array
     * @return      destination byte array
     */
    public static final byte[] byteTobyte(byte[] src) {
        byte[] dst = new byte[src.length];

        for (int i = 0; i < src.length; i++) {
            dst[i] = src[src.length - i - 1];
        }

        return dst;
    }
}