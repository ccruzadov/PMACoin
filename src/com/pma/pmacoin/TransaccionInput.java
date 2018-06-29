package com.pma.pmacoin;

public class TransaccionInput {
	
	public String transaccionOutputId; //Reference to TransactionOutputs -> transactionId
	public TransaccionOutput UTXO; //Contains the Unspent transaction output
	
	public TransaccionInput(String transaccionOutputId) {
		this.transaccionOutputId = transaccionOutputId;
		
	}

}
