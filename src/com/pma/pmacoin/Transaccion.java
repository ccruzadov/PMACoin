package com.pma.pmacoin;

import java.security.PrivateKey;
import java.security.PublicKey;
import java.util.ArrayList;
import com.pma.pmacoin.seguridad.StringUtil;

public class Transaccion {

	public String transaccionId; // this is also the hash of the transaction.
	public PublicKey sender; // senders address/public key.
	public PublicKey reciepient; // Recipients address/public key.
	public float value;
	private byte[] signature; // this is to prevent anybody else from spending // funds in our wallet.
	public ArrayList<TransaccionInput> inputs = new ArrayList<TransaccionInput>();
	public ArrayList<TransaccionOutput> outputs = new ArrayList<TransaccionOutput>();

	private static int sequence = 0; // a rough count of how many transactions
										// have been generated.

	// Constructor:
	public Transaccion(PublicKey from, PublicKey to, float value, ArrayList<TransaccionInput> inputs) {
		this.sender = from;
		this.reciepient = to;
		this.value = value;
		this.inputs = inputs;
	}

	// This Calculates the transaction hash (which will be used as its Id)
	private String calulateHash() {
		sequence++; // increase the sequence to avoid 2 identical transactions
					// having the same hash
		return StringUtil.applySha256(StringUtil.getStringFromKey(sender) + StringUtil.getStringFromKey(reciepient)
				+ Float.toString(value) + sequence);
	}

	// Signs all the data we dont wish to be tampered with.
	public void generateSignature(PrivateKey privateKey) {
		String data = StringUtil.getStringFromKey(sender) + StringUtil.getStringFromKey(reciepient)
				+ Float.toString(value);
		signature = StringUtil.applyECDSASig(privateKey, data);
	}

	// Verifies the data we signed hasnt been tampered with
	public boolean verifiySignature() {
		String data = StringUtil.getStringFromKey(sender) + StringUtil.getStringFromKey(reciepient)
				+ Float.toString(value);
		return StringUtil.verifyECDSASig(sender, data, signature);
	}

	public boolean processTransaccion() {
		if (verifiySignature() == false) {
			System.out.println("#Transaction Signature failed to verify");
			return false;
		}

		// gather transaction inputs (Make sure they are unspent):
		for (TransaccionInput i : inputs) {
			i.UTXO = Main.UTXOs.get(i.transaccionOutputId);
		}

		// check if transaction is valid:
		if (getInputsValue() < Main.pisoTransaccion) {
			System.out.println("#Transaction Inputs to small: " + getInputsValue());
			return false;
		}

		// generate transaction outputs:
		float leftOver = getInputsValue() - value; // get value of inputs then
													// the left over change:
		transaccionId = calulateHash();
		outputs.add(new TransaccionOutput(this.reciepient, value, transaccionId)); // send
																					// value
																					// to
																					// recipient
		outputs.add(new TransaccionOutput(this.sender, leftOver, transaccionId)); // send
																					// the
																					// left
																					// over
																					// 'change'
																					// back
																					// to
																					// sender

		// add outputs to Unspent list
		for (TransaccionOutput o : outputs) {
			Main.UTXOs.put(o.id, o);
		}

		// remove transaction inputs from UTXO lists as spent:
		for (TransaccionInput i : inputs) {
			if (i.UTXO == null)
				continue; // if Transaction can't be found skip it
			Main.UTXOs.remove(i.UTXO.id);
		}

		return true;
	}

	// returns sum of inputs(UTXOs) values
	public float getInputsValue() {
		float total = 0;
		for (TransaccionInput i : inputs) {
			if (i.UTXO == null)
				continue; // if Transaction can't be found skip it
			total += i.UTXO.value;
		}
		return total;
	}

	// returns sum of outputs:
	public float getOutputsValue() {
		float total = 0;
		for (TransaccionOutput o : outputs) {
			total += o.value;
		}
		return total;
	}

}
