package com.pma.pmacoin;

import java.security.PublicKey;
import com.pma.pmacoin.seguridad.StringUtil;

public class TransaccionOutput {
	public String id; //HASH de la transaccion output
	public PublicKey reciepient; // Nuevo dueño de las coins.
	public float value; // cantidad de monedas
	public String parentTransactionId; // id de transaccion que la origino

	public TransaccionOutput(PublicKey reciepient, float value, String parentTransactionId) {
		this.reciepient = reciepient;
		this.value = value;
		this.parentTransactionId = parentTransactionId;
		this.id = StringUtil.applySha256(StringUtil.getStringFromKey(reciepient) + Float.toString(value) + parentTransactionId);
	}

	// Check if coin belongs to you
	public boolean isMine(PublicKey publicKey) {
		return (publicKey == reciepient);
	}

}
