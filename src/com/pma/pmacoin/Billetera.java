package com.pma.pmacoin;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.PrintWriter;
import java.security.*;
import java.security.spec.ECGenParameterSpec;
import java.security.spec.EncodedKeySpec;
import java.security.spec.PKCS8EncodedKeySpec;
import java.security.spec.X509EncodedKeySpec;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

import org.apache.commons.codec.DecoderException;
import org.apache.commons.codec.binary.Base64;
import org.apache.commons.codec.binary.Hex;
import org.apache.commons.codec.binary.StringUtils;

import com.pma.pmacoin.seguridad.StringUtil;

public class Billetera {

	public String duenoBilletera;
	//private PrivateKey privateKey;
	//private PublicKey publicKey;
	public PrivateKey privateKey;
	public PublicKey publicKey;
	private byte[] cargaKeys;

	public HashMap<String, TransaccionOutput> UTXOs = new HashMap<String, TransaccionOutput>();

	public Billetera(String duenoBlilletera, String claveBilletera) {
		String nombreAlmacen = StringUtil.applySha256(StringUtil.crypto(duenoBlilletera, claveBilletera, true)) + ".DAT";
		this.duenoBilletera = duenoBlilletera;
		File file = new File(nombreAlmacen);
		try {
			
			if(!file.exists()){
				System.out.println("Generando nueva billetera..."); // creamos almacen
				generateKeyPair();
				cargaKeys = StringUtil.applyECDSASig(privateKey,nombreAlmacen);
				//convert stirng
				String publico = StringUtil.crypto(StringUtil.getStringFromKey(publicKey), claveBilletera, true);
				String privado = StringUtil.crypto(StringUtil.getStringFromKey(privateKey), claveBilletera, true);
				String carga   = StringUtil.crypto(StringUtil.getStringFromByte(cargaKeys), claveBilletera, true);
				//Save in file
				FileOutputStream keyfos = new FileOutputStream(nombreAlmacen);
				keyfos.write(publico.getBytes()); keyfos.write("\n".getBytes());
				keyfos.write(privado.getBytes());  keyfos.write("\n".getBytes());
				keyfos.write(carga.getBytes()); keyfos.write("\n".getBytes());
				//keyfos.write(StringUtil.getStringFromByte(cargaKeys).getBytes()); keyfos.write("\n".getBytes());
				keyfos.close();
				System.out.println("Generando nueva billetera...FIN"); // creamos almacen
			}else{
				System.out.println("Cargando datos de billetera..."); // creamos almacen
				FileReader f = new FileReader(nombreAlmacen);
				BufferedReader b = new BufferedReader(f);
				String pub = b.readLine();
				String priv = b.readLine();
				String validaCarga = b.readLine();
				b.close();	if (f!=null) f=null;  //Cerrando ficheros
				byte[] bytepublico = StringUtil.getByteFromString(StringUtil.crypto(pub, claveBilletera, false)); //carga y desencripta
				byte[] byteprivado = StringUtil.getByteFromString(StringUtil.crypto(priv, claveBilletera, false)); 
				cargaKeys = StringUtil.getByteFromString(StringUtil.crypto(validaCarga, claveBilletera, false));
				//Regenerando llaves
				KeyFactory keyFactory = KeyFactory.getInstance("ECDSA", "BC");
				X509EncodedKeySpec keySpec = new X509EncodedKeySpec(bytepublico); //recrea clave publica
				publicKey = keyFactory.generatePublic(keySpec);
				EncodedKeySpec privateKeySpec = new PKCS8EncodedKeySpec(byteprivado); //recrea clave privada
				privateKey = keyFactory.generatePrivate(privateKeySpec);
				if (privateKey != null && publicKey != null && cargaKeys != null) { //OK, validamos consistencia tanto en la firma como en la cadena
					boolean  tempo = StringUtil.verifyECDSASig(publicKey, nombreAlmacen, cargaKeys);
					System.out.println("Verificacion cargaKeys (publicKey): " + tempo + ". " + duenoBlilletera);
				}
				else{
					System.out.println("ERROR. Cargando llaves desde archivo: " + duenoBlilletera + ". Deberá generar nueva billetera.");
					privateKey = null; publicKey = null; cargaKeys = null;
					Exception e = new Exception("ERROR"); throw new RuntimeException(e);
				}
			}
		} catch (Exception e) {
			e.printStackTrace();
			throw new RuntimeException(e);
		} finally{
			if(file!=null) file = null;
			System.out.println("Finally..."); // creamos almacen
		}
	}

	public void generateKeyPair() {
		try {
			KeyPairGenerator keyGen = KeyPairGenerator.getInstance("ECDSA", "BC");
			SecureRandom random = SecureRandom.getInstance("SHA1PRNG");
			ECGenParameterSpec ecSpec = new ECGenParameterSpec("prime192v1");
			keyGen.initialize(ecSpec, random); // 256 bytes provides an
			KeyPair keyPair = keyGen.generateKeyPair();
			publicKey = keyPair.getPublic();
			privateKey = keyPair.getPrivate();
		} catch (Exception e) {
			throw new RuntimeException(e);
		}
	}

	public float getBalance() {
		float total = 0; // CERO
		for (Map.Entry<String, TransaccionOutput> item : Main.UTXOs.entrySet()) {
			TransaccionOutput UTXO = item.getValue();
			if (UTXO.isMine(publicKey)) { // if output belongs to me ( if coins
											// belong to me )
				UTXOs.put(UTXO.id, UTXO); // add it to our list of unspent
											// transactions.
				total += UTXO.value;
			}
		}
		return total;
	}

	// Generates and returns a new transaction from this wallet.
	public Transaccion sendFunds(PublicKey _recipient, float value) {
		if (getBalance() < value) { // gather balance and check funds.
			System.out.println("No hay fondos suficientes para enviar la transacción. Transacción descartada.");
			return null;
		}
		// create array list of inputs
		ArrayList<TransaccionInput> inputs = new ArrayList<TransaccionInput>();

		float total = 0;
		for (Map.Entry<String, TransaccionOutput> item : UTXOs.entrySet()) {
			TransaccionOutput UTXO = item.getValue();
			total += UTXO.value;
			inputs.add(new TransaccionInput(UTXO.id));
			if (total > value)
				break;
		}

		Transaccion newTransaction = new Transaccion(publicKey, _recipient, value, inputs);
		newTransaction.generateSignature(privateKey);

		for (TransaccionInput input : inputs) {
			UTXOs.remove(input.transaccionOutputId);
		}
		return newTransaction;
	}
}
