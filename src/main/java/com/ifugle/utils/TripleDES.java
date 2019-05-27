package com.ifugle.utils;
import java.io.UnsupportedEncodingException;
import java.security.GeneralSecurityException;
import java.security.InvalidAlgorithmParameterException;
import java.security.InvalidKeyException;
import java.security.Key;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.util.Arrays;

import javax.crypto.BadPaddingException;
import javax.crypto.Cipher;
import javax.crypto.IllegalBlockSizeException;
import javax.crypto.KeyGenerator;
import javax.crypto.NoSuchPaddingException;
import javax.crypto.SecretKey;
import javax.crypto.spec.IvParameterSpec;
import javax.crypto.spec.SecretKeySpec;

import org.apache.commons.codec.DecoderException;
import org.apache.commons.codec.binary.Hex;
public class TripleDES {
    private Cipher cipher = null;
    private SecretKey key = null;
    private byte[] bytes = null;
    private IvParameterSpec iv = new IvParameterSpec(new byte[] { 0, 0, 0, 0, 0, 0, 0, 0 });

    public static void main(String[] args) throws Exception {
        try {
            //String strKey = "123456789123456789123456";
        	String strKey = "687A666A6B6A373839313233343536373839313233343536";
            byte[] encodeFormat=null;
            try {
              //秘钥 Hex解码为什么秘钥要进行解码，因为秘钥是某个秘钥明文进行了Hex编码后的值，所以在使用的时候要进行解码
            	encodeFormat = Hex.decodeHex(strKey.toCharArray());
            	//encodeFormat = key.getBytes();
            } catch (Exception e) {
              e.printStackTrace();
            }
            TripleDES encryptor = new TripleDES(encodeFormat);
            String original = "200";
            System.out.println("Oringal: \"" + original + "\"");
            String enc = encryptor.encrypt(original);
            System.out.println("Encrypted: \"" + enc + "\"");
            String dec = encryptor.decrypt(enc);
            System.out.println("Decrypted: \"" + dec + "\"");
            if (dec.equals(original)) {
                System.out.println("Encryption ==> Decryption Successful");
            }
            String aesKey = "hzfjkjczyxt_1203";
            System.out.println("aesKey: \"" + aesKey + "\"");
            String aesOriginal = "67891002223456.89";
            System.out.println("aesOriginal: \"" + aesOriginal + "\"");
            String aesenc = encrypt_AES(aesOriginal,aesKey);
            System.out.println("AES_Encrypted: \"" + aesenc + "\"");
            String aesdec = decrypt_AES(aesenc,aesKey);
            System.out.println("AESDecrypted: \"" + aesdec + "\"");
            if (dec.equals(original)) {
                System.out.println("AESEncryption ==> AESDecryption Successful");
            }
        }
        catch (Exception e) {
            System.out.println("Error: " + e.toString());
        }
    }

    public TripleDES(byte[] encryptionKey) throws GeneralSecurityException,  DecoderException{
        cipher = Cipher.getInstance("DESede/CBC/NoPadding");
        key = new SecretKeySpec(encryptionKey, "DESede");
        iv = new IvParameterSpec(Hex.decodeHex("0123456789abcdef".toCharArray()));
        //iv = new IvParameterSpec("0123456789abcdef".getBytes());
    }

    public String encrypt(String input) throws GeneralSecurityException, UnsupportedEncodingException {
    	bytes = input.getBytes("UTF-8");
    	bytes = Arrays.copyOf(bytes, ((bytes.length+7)/8)*8);
    	byte[] ebs = encryptB(bytes);
        return new String(Hex.encodeHex(ebs));
    }

    public String decrypt(String input) throws GeneralSecurityException, DecoderException, UnsupportedEncodingException {
        bytes = Hex.decodeHex(input.toCharArray());
        String decrypted = new String(decryptB(bytes), "UTF-8");
        if (decrypted.indexOf((char)0) > 0) {
            decrypted = decrypted.substring(0, decrypted.indexOf((char)0));
        }
        return decrypted;
    }

    public byte[] encryptB(byte[] bytes) throws GeneralSecurityException {
        cipher.init(Cipher.ENCRYPT_MODE, (Key)key, iv);
        return cipher.doFinal(bytes);
    }

    public byte[] decryptB(byte[] bytes) throws GeneralSecurityException {
        cipher.init(Cipher.DECRYPT_MODE, (Key)key, iv);
        return cipher.doFinal(bytes);
    }
    
    public static String encrypt_AES(String content, String password) {
    	byte[] result = null;
        try {
        	Cipher cipher = Cipher.getInstance("AES/CBC/PKCS5Padding");
            SecretKeySpec keySpec = new SecretKeySpec(password.getBytes("utf-8"),"AES");
            //向量iv
            byte[] ivs = "0123456789abcdef".getBytes("UTF-8");
            IvParameterSpec ivParameterSpec = new IvParameterSpec(ivs);
            cipher.init(Cipher.ENCRYPT_MODE,keySpec,ivParameterSpec);
            byte[] byteContent = content.getBytes("utf-8");
            result = cipher.doFinal(byteContent);       
        }catch (NoSuchPaddingException e) {
            e.printStackTrace();
        }catch (NoSuchAlgorithmException e) {
            e.printStackTrace();
        }catch (UnsupportedEncodingException e) {
            e.printStackTrace();
        }catch (InvalidKeyException e) {
            e.printStackTrace();
        }catch (IllegalBlockSizeException e) {
            e.printStackTrace();
        }catch (BadPaddingException e) {
            e.printStackTrace();
        }catch(Exception e){
        	e.printStackTrace();
        }
        return new String(Hex.encodeHex(result));
    }
    
    public static String decrypt_AES(String input, String password) {
    	byte[] result = null;
        try {
        	Cipher cipher = Cipher.getInstance("AES/CBC/PKCS5Padding");
            SecretKeySpec keySpec = new SecretKeySpec(password.getBytes("utf-8"),"AES");
            //向量iv
            byte[] ivs = "0123456789abcdef".getBytes("UTF-8");
            IvParameterSpec ivParameterSpec = new IvParameterSpec(ivs);
            cipher.init(Cipher.DECRYPT_MODE,keySpec,ivParameterSpec);
            byte[] content = Hex.decodeHex(input.toCharArray());
            result = cipher.doFinal(content);  
            return new String(result,"utf-8");
        }catch (NoSuchAlgorithmException e) {
            e.printStackTrace();
        }catch (NoSuchPaddingException e) {
            e.printStackTrace();
        }catch (UnsupportedEncodingException e) {
            e.printStackTrace();
        }catch (InvalidKeyException e) {
            e.printStackTrace();
        }catch (IllegalBlockSizeException e) {
            e.printStackTrace();
        }catch (BadPaddingException e) {
            e.printStackTrace();
        }catch(Exception e){
        	e.printStackTrace();
        }
        return null;
    }
}
