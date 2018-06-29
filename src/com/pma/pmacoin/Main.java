package com.pma.pmacoin;

import java.security.Security;
import java.util.ArrayList;
import java.util.HashMap;

import com.google.gson.GsonBuilder;
import com.pma.pmacoin.seguridad.StringUtil;

public class Main {

	public static ArrayList<Block> PMAcoin = new ArrayList<Block>();
	
	public static HashMap<String,TransaccionOutput> UTXOs = new HashMap<String,TransaccionOutput>();   //list of all unspent transactions. 
	
	public static int dificultadTrabajo = 0;
	public static float pisoTransaccion = 0.1f;
	

	public static Billetera walletA;
	public static Billetera walletB;

	public static Transaccion genesisTransaction;
	
	
	public static void main(String[] args) {
		
		//String derivada =  StringUtil.applySha512("nanzana");
		/*
		final String secretKey = "CARLOSs";
		
		String originalString = "howtodoinjava.com";
		String encryptedString = StringUtil.crypto(originalString, secretKey,true) ;
		String decryptedString = StringUtil.crypto(encryptedString, secretKey,false) ;
		
		System.out.println(originalString);
		System.out.println(encryptedString);
		System.out.println(decryptedString);*/
		
		
		
		
		
		
		
		
		// Setup Bouncey castle as a Security Provider
		Security.addProvider(new org.bouncycastle.jce.provider.BouncyCastleProvider());
		
		System.out.println("Dificultad: " + dificultadTrabajo);
		
		
		// Create the new wallets
		walletA = new Billetera("Carlos","carlos");
		
		if (true) return;
		
		
		walletB = new Billetera("Bertha","bertha");
		
		Billetera PMAgenesis = new Billetera("Genesis","");
		
		
		
		
		//create genesis transaction, which sends 100 NoobCoin to walletA: 
		genesisTransaction = new Transaccion(PMAgenesis.publicKey, walletA.publicKey, 100f, null);
		genesisTransaction.generateSignature(PMAgenesis.privateKey); 	 //Firma con llave privada wallet1 y wallet 2 y value
		genesisTransaction.transaccionId = "0"; //manually set the transaction id
		
		genesisTransaction.outputs.add(new TransaccionOutput(genesisTransaction.reciepient, genesisTransaction.value, genesisTransaction.transaccionId)); //primer Output manualmente
		//Tambien se agrega al UTXOs del main
		UTXOs.put(genesisTransaction.outputs.get(0).id, genesisTransaction.outputs.get(0)); //its important to store our first transaction in the UTXOs list.
				
		System.out.println("Creating and Mining Genesis block... ");
		Block genesis = new Block("Bloque Genesis","0");
		genesis.addTransaccion(genesisTransaction);
		addBlock(genesis);	
		

		//Prueba
				Block block1 = new Block("Bloque 1", genesis.hash);
				System.out.println("\nWalletA's balance is: " + walletA.getBalance());
				System.out.println("WalletB's balance is: " + walletB.getBalance());
				System.out.println("\nWalletA is Attempting to send funds (40) to WalletB...");
				block1.addTransaccion(walletA.sendFunds(walletB.publicKey, 40f));
				addBlock(block1);
				System.out.println("\nWalletA's balance is: " + walletA.getBalance());
				System.out.println("WalletB's balance is: " + walletB.getBalance());
				isChainValid();
				
				Block block2 = new Block("Bloque 2",block1.hash);
				System.out.println("\nWalletA Attempting to send more funds (1000) than it has...");
				block2.addTransaccion(walletA.sendFunds(walletB.publicKey, 1000f));
				addBlock(block2);
				System.out.println("\nWalletA's balance is: " + walletA.getBalance());
				System.out.println("WalletB's balance is: " + walletB.getBalance());
				isChainValid();
				
				Block block3 = new Block("Bloque 3",block2.hash);
				System.out.println("\nWalletB is Attempting to send funds (20) to WalletA...");
				block3.addTransaccion(walletB.sendFunds( walletA.publicKey, 20));
				addBlock(block3);
				System.out.println("\nWalletA's balance is: " + walletA.getBalance());
				System.out.println("WalletB's balance is: " + walletB.getBalance());
				
				isChainValid();
				

				Block block4 = new Block("Bloque 4",block3.hash);
				System.out.println("\nWalletA is Attempting to send funds (80) to WalletB...");
				block4.addTransaccion(walletA.sendFunds( walletB.publicKey, 80));
				addBlock(block4);
				System.out.println("\nWalletA's balance is: " + walletA.getBalance());
				System.out.println("WalletB's balance is: " + walletB.getBalance());
				
				isChainValid();
				
				
				String blockchainJson2 = new GsonBuilder().setPrettyPrinting().create().toJson(PMAcoin);
				System.out.println("\nThe block chain: ");
				//System.out.println(blockchainJson2);
				
				

		/*
		// Test public and private keys
		System.out.println("Private and public keys:");
		System.out.println("Privada A: " + StringUtil.getStringFromKey(walletA.privateKey));
		System.out.println("Pública A: " + StringUtil.getStringFromKey(walletA.publicKey));
		// Create a test transaction from WalletA to walletB
		Transaccion transaction = new Transaccion(walletA.publicKey, walletB.publicKey, 5, null);
		transaction.generateSignature(walletA.privateKey);
		// Verify the signature works and verify it from the public key
		System.out.println("Is signature verified");
		System.out.println(transaction.verifiySignature());
		*/

	}

	public static void mainDOS(String[] args) {

		// add our blocks to the blockchain ArrayList:
		PMAcoin.add(new Block("Primer Bloque", "0"));
		// String blockchainJson = new
		// GsonBuilder().setPrettyPrinting().create().toJson(PMAcoin);
		// System.out.println("\nThe block chain: ");
		// System.out.println(blockchainJson);

		System.out.println("Trying to Mine block Genesis... ");
		PMAcoin.get(0).mineBlock(dificultadTrabajo);

		PMAcoin.add(new Block("Segundo Bloque", PMAcoin.get(PMAcoin.size() - 1).hash));
		System.out.println("Trying to Mine block 2... ");
		PMAcoin.get(1).mineBlock(dificultadTrabajo);

		PMAcoin.add(new Block("Tercer Bloque", PMAcoin.get(PMAcoin.size() - 1).hash));
		System.out.println("Trying to Mine block 3... ");
		PMAcoin.get(2).mineBlock(dificultadTrabajo);

		PMAcoin.add(new Block("Cuarto Bloque", PMAcoin.get(PMAcoin.size() - 1).hash));

		System.out.println("Revisar blockchain:" + isChainValid());

		String blockchainJson2 = new GsonBuilder().setPrettyPrinting().create().toJson(PMAcoin);
		System.out.println("\nThe block chain: ");
		System.out.println(blockchainJson2);

	}

	public static Boolean isChainValid() {
		Block currentBlock;
		Block previousBlock;

		String hashTarget = StringUtil.getDificultyString(dificultadTrabajo);// new String(new char[difficulty]).replace('\0', '0');
		HashMap<String,TransaccionOutput> tempUTXOs = new HashMap<String,TransaccionOutput>(); //a temporary working list of unspent transactions at a given block state.
		tempUTXOs.put(genesisTransaction.outputs.get(0).id, genesisTransaction.outputs.get(0)); // 0 , priemra transaccionoutput
		
		
		// loop through blockchain to check hashes:
		for (int i = 1; i < PMAcoin.size(); i++) {
			currentBlock = PMAcoin.get(i);
			previousBlock = PMAcoin.get(i - 1);
			// compare registered hash and calculated hash:
			if (!currentBlock.hash.equals(currentBlock.calculateHash())) {
				System.out.println("Current Hashes not equal");
				return false;
			}
			// compare previous hash and registered previous hash
			if (!previousBlock.hash.equals(currentBlock.previousHash)) {
				System.out.println("Previous Hashes not equal");
				return false;
			}
			
			//check if hash is solved
			if(!currentBlock.hashIsSolved(Main.dificultadTrabajo)){
			//if(!currentBlock.hash.substring( 0, dificultadTrabajo).equals(hashTarget)) {
				System.out.println("#This block hasn't been mined");
				return false;
			}

			//loop thru blockchains transactions:
			TransaccionOutput tempOutput;
			for(int t=0; t <currentBlock.transaccions.size(); t++) {
				Transaccion currentTransaccion = currentBlock.transaccions.get(t);
				if(!currentTransaccion.verifiySignature()) {
					System.out.println("#Signature on Transaction(" + t + ") is Invalid");
					return false; 
				}
				if(currentTransaccion.getInputsValue() != currentTransaccion.getOutputsValue()) {
					System.out.println("#Inputs are note equal to outputs on Transaction(" + t + ")");
					return false; 
				}
				for(TransaccionInput input: currentTransaccion.inputs) {	
					tempOutput = tempUTXOs.get(input.transaccionOutputId);
					
					if(tempOutput == null) {
						System.out.println("#Referenced input on Transaction(" + t + ") is Missing");
						return false;
					}
					
					if(input.UTXO.value != tempOutput.value) {
						System.out.println("#Referenced input Transaction(" + t + ") value is Invalid");
						return false;
					}
					tempUTXOs.remove(input.transaccionOutputId);
				}
				for(TransaccionOutput output: currentTransaccion.outputs) {
					tempUTXOs.put(output.id, output);
				}

				if( currentTransaccion.outputs.get(0).reciepient != currentTransaccion.reciepient) {
					System.out.println("#Transaction(" + t + ") output reciepient is not who it should be");
					return false;
				}
				if( currentTransaccion.outputs.get(1).reciepient != currentTransaccion.sender) {
					System.out.println("#Transaction(" + t + ") output 'change' is not sender.");
					return false;
				}
				
			}
			
			
		}
		System.out.println("Blockchain VALIDO");
		return true;
	}
	
	public static void addBlock(Block newBlock) {
		newBlock.mineBlock(dificultadTrabajo);
		PMAcoin.add(newBlock);
		//evaluar si aumenta la complejidad
		dificultadTrabajo++;
		System.out.println("Bloque número: " + newBlock.data);
		System.out.println("Dificultad final del sistema: " + dificultadTrabajo);

	}
	
	

}
