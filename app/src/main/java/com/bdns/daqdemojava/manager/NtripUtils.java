package com.bdns.daqdemojava.manager;

import android.util.Base64;

public class NtripUtils {

    public static String CreateHttpRequsets(String mountPoint, String userId, String password) {
        String msg = "GET /" + mountPoint + " HTTP/1.0\r\n";
        msg = msg + "User-Agent: NTRIP GNSSInternetRadio/1.4.11\r\n";
        msg = msg + "Accept: */*\r\n";
        msg = msg + "Connection: close\r\n";
        String tempString = userId + ":" + password;
        byte[] buf = tempString.getBytes();
        String code = Base64.encodeToString(buf, 2);
        msg = msg + "Authorization: Basic " + code + "\r\n";
        msg = msg + "\r\n";
        return msg;
    }
    public static String bytesToHexString(byte[] bArray) {
        StringBuffer sb = new StringBuffer(bArray.length);
        String sTemp;
        for (int i = 0; i < bArray.length; i++) {
            sTemp = Integer.toHexString(0xFF & bArray[i]);
            if (sTemp.length() < 2)
                sb.append(0);
            sb.append(sTemp.toUpperCase());
        }
        return sb.toString();
    }

    //byte转16进制字符串
    public static String byte2HexStr(byte[] b) {
        String hs = "";
        String stmp = "";
        for (int n = 0; n < b.length; n++) {
            stmp = (Integer.toHexString(b[n] & 0XFF));
            if (stmp.length() == 1)
                hs = hs + "0" + stmp;
            else
                hs = hs + stmp;
            // if (n<b.length-1) hs=hs+":";
        }
        return hs.toUpperCase();
    }

    public static byte[] HexCommandtoByte(byte[] data) {
        if (data == null) {
            return null;
        } else {
            int nLength = data.length;
            if (nLength >= 10 && data[2] == 32 && data[5] == 32 && data[8] == 32) {
                do {
                    if (data[nLength - 1] == 10) {
                        --nLength;
                    } else if (data[nLength - 1] == 13) {
                        --nLength;
                    } else {
                        if (data[nLength - 1] != 32) {
                            String strTemString = new String(data, 0, nLength);
                            String[] strings = strTemString.split(" ");
                            nLength = strings.length;
                            data = new byte[nLength];

                            for(int i = 0; i < nLength; ++i) {
                                if (strings[i].length() != 2) {
                                    return null;
                                }

                                try {
                                    data[i] = (byte)Integer.parseInt(strings[i], 16);
                                } catch (Exception var7) {
                                    return null;
                                }
                            }

                            return data;
                        }

                        --nLength;
                    }
                } while(nLength >= 8);

                return null;
            } else {
                return data;
            }
        }
    }


}
