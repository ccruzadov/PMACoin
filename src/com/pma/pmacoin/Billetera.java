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
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

import org.apache.commons.codec.DecoderException;
import org.apache.commons.codec.binary.Hex;
import org.apache.commons.codec.binary.StringUtils;
import org.bouncycastle.asn1.eac.PublicKeyDataObject;

import com.pma.pmacoin.seguridad.StringUtil;

public class Billetera {

	public String duenoBilletera;
	private String claveBilletera;
	
	//private PrivateKey privateKey;
	//private PublicKey publicKey;
	
	public PrivateKey privateKey;
	public PublicKey publicKey;

	public HashMap<String, TransaccionOutput> UTXOs = new HashMap<String, TransaccionOutput>();
	
	public Billetera(String duenoBlilletera, String clave){
		String nombreAlmacen = StringUtil.applySha256(StringUtil.crypto(duenoBlilletera, clave, true)) + ".dat";
		System.out.println(nombreAlmacen);
		this.duenoBilletera = duenoBlilletera;
		//this.claveBilletera = clave; //NO SE ALMACENA LA CLAVE
		try {
			File file = new File(nombreAlmacen);
			System.out.println("Generando billetera...");		//creamos almacen
			generateKeyPair();
			if (privateKey != null && publicKey != null ){
				//System.out.println("Usuario ya existe....cargando datos");
				System.out.println(privateKey);
				String publico = StringUtil.crypto(StringUtil.getStringFromKey(publicKey),clave,true);
				String privado = StringUtil.crypto(StringUtil.getStringFromKey(privateKey),clave,true);
				System.out.println(publico);
				System.out.println(privado);
				
				FileOutputStream keyfos = new FileOutputStream(nombreAlmacen);
				keyfos.write(publico.getBytes());
				keyfos.write("\n".getBytes());
				keyfos.write(privado.getBytes());
				keyfos.close();
			}
			
		    String cadena = "";
		    FileReader f = new FileReader(nombreAlmacen);
		      BufferedReader b = new BufferedReader(f);
		      cadena = b.readLine();
		      String priv = cadena;
		      cadena = b.readLine();
		      String pub = cadena;
		      
		      System.out.println(priv);
		      System.out.println(pub);
		      b.close();
			
			
		} catch (Exception e){
			e.printStackTrace();
		}
		
		
		
		
		
		
		

	}

	public void generateKeyPair() {
		try {
			KeyPairGenerator keyGen = KeyPairGenerator.getInstance("ECDSA", "BC");
			SecureRandom random = SecureRandom.getInstance("SHA1PRNG");
			ECGenParameterSpec ecSpec = new ECGenParameterSpec("prime192v1");
			keyGen.initialize(ecSpec, random); // 256 bytes provides an acceptable security level
			KeyPair keyPair = keyGen.generateKeyPair();
			publicKey = keyPair.getPublic();
			privateKey = keyPair.getPrivate();
		} catch (Exception e) {
			throw new RuntimeException(e);
		}
	}

	public float getBalance() {
		float total = 0;		//CERO
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
