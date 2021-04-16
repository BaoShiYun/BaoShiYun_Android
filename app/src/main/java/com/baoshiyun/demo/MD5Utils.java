package com.baoshiyun.demo;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.channels.FileChannel;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

/**
 * MD5工具类
 */
public class MD5Utils {

    // 十六进制下数字到字符的映射数组
    private final static String[] hexDigits = {"0", "1", "2", "3", "4", "5",
            "6", "7", "8", "9", "a", "b", "c", "d", "e", "f"};

    /**
     * 指定算法为MD5的MessageDigest
     */
    private static MessageDigest messageDigest = null;

    /** 初始化messageDigest的加密算法为MD5 */
    static {
        try {
            messageDigest = MessageDigest.getInstance("MD5");
        } catch (NoSuchAlgorithmException e) {
            e.printStackTrace();
        }
    }

    /**
     * 获取文件的MD5
     *
     * @param file
     * @return
     */
    public static String getFileMD5(File file) {
        String ret = "";
        FileInputStream in = null;
        FileChannel ch = null;
        try {
            in = new FileInputStream(file);
            ch = in.getChannel();
            ByteBuffer byteBuffer = ch.map(FileChannel.MapMode.READ_ONLY, 0,
                    file.length());
            messageDigest.update(byteBuffer);
            ret = bytesToHex(messageDigest.digest());
        } catch (IOException e) {
            e.printStackTrace();

        } finally {
            if (in != null) {
                try {
                    in.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
            if (ch != null) {
                try {
                    ch.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
        return ret;
    }

    /**
     * 获取文件的MD5
     *
     * @param fileName
     * @return
     */
    public static String getFileMD5String(String fileName) {
        return getFileMD5(new File(fileName));
    }

    /**
     * 获取字符串 md5
     *
     * @param sourceStr
     * @return
     */

    public static String getMD5(String sourceStr) {
        return getMD5(sourceStr.getBytes());
    }

    /**
     * 获取 byte 数据的 md5
     *
     * @param bytes
     * @return
     */

    public static String getMD5(byte[] bytes) {
        messageDigest.update(bytes);
        return bytesToHex(messageDigest.digest());
    }

    /**
     * 校验 字符串 md5
     *
     * @param sourceStr
     * @param md5
     * @return
     */
    public static boolean checkMd5(String sourceStr, String md5) {
        return getMD5(sourceStr).equalsIgnoreCase(md5);
    }

    /**
     * 校验 file md5
     *
     * @param file
     * @param md5
     * @return
     */
    public static boolean checkFileMD5(File file, String md5) {
        return getFileMD5(file).equalsIgnoreCase(md5);

    }

    /**
     * 校验 file md5
     *
     * @param fileName
     * @param md5
     * @return
     */
    public static boolean checkFileMD5(String fileName, String md5) {
        return checkFileMD5(new File(fileName), md5);

    }

    /**
     * 将字节数组转换成16进制字符串
     *
     * @param bytes 目标字节数组
     * @return
     */
    public static String bytesToHex(byte[] bytes) {
        return bytesToHex(bytes, 0, bytes.length);

    }

    /**
     * 将字节数组中指定区间的子数组转换成16进制字符串
     *
     * @param bytes
     * @param start 起始位置（包括该位置）
     * @param end   结束位置（不包括该位置）
     * @return
     */
    public static String bytesToHex(byte[] bytes, int start, int end) {
        StringBuilder sb = new StringBuilder();
        for (int i = start; i < start + end; i++) {
            sb.append(byteToHex(bytes[i]));
        }
        return sb.toString();

    }

    /**
     * 将字节码转换成16进制字符串
     *
     * @param bt
     * @return
     */
    public static String byteToHex(byte bt) {
        return hexDigits[(bt & 0xf0) >> 4] + "" + hexDigits[bt & 0xf];

    }
}
