package com.pma.pmacoin;
import java.util.ArrayList;
import java.util.Date;
import com.pma.pmacoin.seguridad.StringUtil;

public class Block {
	public String hash;  //hash actual
	public String previousHash;  //hash previo
	public String merkleRoot;   //para merkle
	public ArrayList<Transaccion> transaccions = new ArrayList<Transaccion>(); //Todas las transacciones del block.   //Actualmente sólo una TX
	
	private int dificultadBlock;
	public String data; //DUMMY.
	private int nonce;  //para prueba de trabajo
	private long timeStamp; // número de milisegundos
	
	public boolean hashIsSolved(int dificultadTrabajo){ //Si block fue minado vs prueba de trabajo asociada
		//System.out.println("Dificultad:\nDel block:   " + dificultadBlock + "\nDel Sistema: " + dificultadTrabajo ); 
		String hashTarget = StringUtil.getDificultyString(dificultadBlock);
		if(!hash.substring( 0, dificultadBlock).equals(hashTarget)) {
			System.out.println("BLOCK: Bloque no minado");
			return false;
		}
		return true;
	}
	
	// Block CONSTRUCTOR.
	public Block(String data, String previousHash) {
		this.previousHash = previousHash;
		this.timeStamp = new Date().getTime();
		this.hash = calculateHash(); 	//Calculando hash por defecto que será reemplazado
		this.dificultadBlock = 1;		//por defecto que será reemplazado
		this.data = data;
		
	}

	public String calculateHash() {
		String calculatedhash = StringUtil.applySha256(previousHash + Long.toString(timeStamp) + Integer.toString(nonce) + merkleRoot);
		return calculatedhash;
	}
	
	public void mineBlock(int dificultadTrabajo) {
		System.out.println("Minando con dificultad: " + dificultadTrabajo);
		merkleRoot = StringUtil.getMerkleRoot(transaccions);
		String target = StringUtil.getDificultyString(dificultadTrabajo); // obtiene la dificultad en ceros
		while (!hash.substring(0, dificultadTrabajo).equals(target)) {
			nonce++;
			hash = calculateHash();
		}
		dificultadBlock = dificultadTrabajo;//asignar dificultad a cada bloque;
		System.out.println("Block minado: " + hash + ". Dificultad: " + dificultadBlock);
	}

	public boolean addTransaccion(Transaccion transaccion) {
		// process transaction and check if valid, unless block is genesis block
		// then ignore.
		if (transaccion == null) return false;
		if ((previousHash != "0")) {
			if ((transaccion.processTransaccion() != true)) {
				System.out.println("Error processTransaccion. No se añadió.");
				return false;
			}
		}
		transaccions.add(transaccion); // exito
		System.out.println("Transaccion exitosa se agrega al BLOCK.");
		return true;
	}

}
