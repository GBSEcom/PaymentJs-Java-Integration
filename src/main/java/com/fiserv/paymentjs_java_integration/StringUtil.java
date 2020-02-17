package com.fiserv.paymentjs_java_integration;

import org.apache.commons.codec.binary.Base64;
import org.apache.commons.codec.binary.Hex;
import org.apache.commons.codec.digest.HmacAlgorithms;
import org.apache.commons.codec.digest.HmacUtils;

public class StringUtil {

    /**
     * Create Base64 encoded hmac hash
     *
     * @param msg Message components
     * @param secret Paymentj.s apiSecret
     * @return Message Signature
     */
    public static String genHmac(String msg, String secret) {
        HmacAlgorithms algorithm = HmacAlgorithms.HMAC_SHA_256;
        HmacUtils hmacUtils = new HmacUtils(algorithm, secret);
        Hex hexEncoder = new Hex();

        byte[] binaryEncodedHash = hmacUtils.hmac(msg);
        byte[] hexEncodedHash = hexEncoder.encode(binaryEncodedHash);
        return Base64.encodeBase64String(hexEncodedHash);
    }

    /**
     * Format boolean values in Json strings. Make lowercase and strip quotes
     *
     * @param input Json String
     * @return Json String
     */
    public static String formatBooleans(String input){
        return input.replaceAll("\"(?i)true\"","true").replaceAll("\"(?i)false\"","false");
    }
}
