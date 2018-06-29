package com.pma.pmacoin.seguridad;

import java.io.UnsupportedEncodingException;
import java.security.Key;
import java.security.KeyFactory;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.security.PrivateKey;
import java.security.PublicKey;
import java.security.Signature;
import java.util.ArrayList;
import java.util.Arrays;

import javax.crypto.Cipher;
import javax.crypto.spec.SecretKeySpec;

import org.bouncycastle.util.encoders.Base64Encoder;
import org.bouncycastle.util.encoders.HexEncoder;
import org.apache.commons.codec.Decoder;
import org.apache.commons.codec.binary.Base64;
import org.apache.commons.codec.binary.Hex;

import com.pma.pmacoin.Transaccion;

public class StringUtil {

	public static String applySha512(String input) {
		//Retorna HASH byte_hex_String
		try {
			MessageDigest digest = MessageDigest.getInstance("SHA-512");
			byte[] hash = digest.digest(input.getBytes("UTF-8"));
			/*StringBuffer hexString = new StringBuffer();
			for (int i = 0; i < hash.length; i++) {
				String hex = Integer.toHexString(0xff & hash[i]);
				if (hex.length() == 1)	hexString.append('0');
				hexString.append(hex);
			}
			//return hexString.toString();
			//System.out.println("con el HEX:" + Hex.encodeHexString(hash).toString());
			//System.out.println( (Hex.encodeHexString(hash).toString().equals(hexString.toString()) ? true:false));*/
			return Hex.encodeHexString(hash).toString();
		} catch (NoSuchAlgorithmException e) {
			e.printStackTrace();
			throw new RuntimeException(e);
		} catch (UnsupportedEncodingException e) {
			e.printStackTrace();
		}catch (Exception e) {
			e.printStackTrace();
			throw new RuntimeException(e);
		}
		return null;
	}

	public static String getStringFromKey(Key key) {
		return Base64.encodeBase64String(  key.getEncoded());
	}
	
	public static Key getKeyFromString(String cadena) {
		KeyFactory kf = null;
		
		
		return (Key) Base64.decode  .decodeBase64(cadena);
		
	}

	public static SecretKeySpec setKey(String pass) {
		byte[] key = null;
		SecretKeySpec retorno = null;
		try {
			//Obtenemos key en String y lo decodificamos
			key = Hex.decodeHex(applySha512(pass)) ; //Base64.decodeBase64();;
			key = Arrays.copyOf(key, 16);
			retorno = new SecretKeySpec(key, "AES"); //encriptado con algoritsmo estandar
		}  catch (Exception e) {
			e.printStackTrace();
		}
		return retorno;
	}

	public static String crypto(String strCrypto, String secret, boolean trx) {
		SecretKeySpec secretKey = null;
		try {
			secretKey = setKey(secret);
			Cipher cipher = Cipher.getInstance("AES/ECB/PKCS5PADDING");
			if (trx) {
				cipher.init(Cipher.ENCRYPT_MODE, secretKey);
				return Base64.encodeBase64String(cipher.doFinal(strCrypto.getBytes("UTF-8")));
			} else {
				cipher.init(Cipher.DECRYPT_MODE, secretKey);
				return new String(cipher.doFinal(Base64.decodeBase64(strCrypto)));
			}
		} catch (Exception e) {
			System.out.println("Error while crypto. Tx:" + trx + ". Err:" + e.toString());
		}
		return null;
	}

	public static String applySha256(String input) {
		try {
			MessageDigest digest = MessageDigest.getInstance("SHA-256");
			byte[] hash = digest.digest(input.getBytes("UTF-8")); // UTF8
			/*StringBuffer hexString = new StringBuffer(); // This will contain
			for (int i = 0; i < hash.length; i++) {
				String hex = Integer.toHexString(0xff & hash[i]);
				if (hex.length() == 1)
					hexString.append('0');
				hexString.append(hex);
			}
			System.out.println("con el HEX:" + Hex.encodeHexString(hash).toString());
			System.out.println( (Hex.encodeHexString(hash).toString().equals(hexString.toString()) ? true:false));
			return hexString.toString();*/
			return Hex.encodeHexString(hash).toString();
		} catch (Exception e) {
			throw new RuntimeException(e);
		}
	}

	// Applies ECDSA Signature and returns the result ( as bytes ).
	public static byte[] applyECDSASig(PrivateKey privateKey, String input) {
		Signature dsa;
		byte[] output = new byte[0];
		try {
			dsa = Signature.getInstance("ECDSA", "BC");
			dsa.initSign(privateKey);
			byte[] strByte = input.getBytes();
			dsa.update(strByte);
			byte[] realSig = dsa.sign();
			output = realSig;
		} catch (Exception e) {
			throw new RuntimeException(e);
		}
		return output;
	}

	// Verifies a String signature
	public static boolean verifyECDSASig(PublicKey publicKey, String data, byte[] signature) {
		try {
			Signature ecdsaVerify = Signature.getInstance("ECDSA", "BC");
			ecdsaVerify.initVerify(publicKey);
			ecdsaVerify.update(data.getBytes());
			return ecdsaVerify.verify(signature);
		} catch (Exception e) {
			throw new RuntimeException(e);
		}
	}

	public static String getMerkleRoot(ArrayList<Transaccion> transaccions) {
		int count = transaccions.size();
		ArrayList<String> previousTreeLayer = new ArrayList<String>();
		for (Transaccion transaccion : transaccions) {
			previousTreeLayer.add(transaccion.transaccionId);
		}
		ArrayList<String> treeLayer = previousTreeLayer;
		while (count > 1) {
			treeLayer = new ArrayList<String>();
			for (int i = 1; i < previousTreeLayer.size(); i++) {
				treeLayer.add(applySha256(previousTreeLayer.get(i - 1) + previousTreeLayer.get(i)));
			}
			count = treeLayer.size();
			previousTreeLayer = treeLayer;
		}
		String merkleRoot = (treeLayer.size() == 1) ? treeLayer.get(0) : "";
		return merkleRoot;
	}

	public static String getDificultyString(int difficulty) {
		return new String(new char[difficulty]).replace('\0', '0');
	}

}
