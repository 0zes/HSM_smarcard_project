package monpackage;

import javacard.framework.*;
import javacard.security.*;
import javacardx.crypto.*;

public class monpackage extends Applet
{
	// CLA byte declaration
	public static final byte CLA_MONPACKAGE= (byte) 0xB0;
	
	// INS declaration
    public static final byte INS_SEND_INFO= (byte) 0x01;
    public static final byte INS_VERIFY_PIN= (byte) 0x02;
    
    // PIN related declaration
    final static byte PIN_TRY_LIMIT = (byte) 0x03;
    final static byte MAX_PIN_SIZE = (byte) 0x04;
    final static short SW_VERIFICATION_FAILED = 0x6300;
    final static short SW_PIN_VERIFICATION_REQUIRED = (short) 0x9004;
    
    // Définition des constantes et des variables nécessaires
    private static final byte[] cleSession = {(byte)0x11, (byte)0x22, (byte)0x33, (byte)0x44, (byte)0x55, (byte)0x66, (byte)0x77, (byte)0x88, 
												(byte)0x99, (byte)0xAA, (byte)0xBB, (byte)0xCC, (byte)0xDD, (byte)0xEE, (byte)0xFF, (byte)0x00};
    private AESKey elementSecretPatient = (AESKey) KeyBuilder.buildKey(KeyBuilder.TYPE_AES_TRANSIENT_DESELECT, KeyBuilder.LENGTH_AES_256, false);
    private Cipher cipher;
    private OwnerPIN pin;
    private byte counter;
    private byte[] user_pin = {0x01, 0x02, 0x03, 0x04};
	byte[] key = {0x2d, 0x2a, 0x2d, 0x42, 0x55, 0x49, 0x4c, 0x44, 0x41, 0x43, 0x4f, 0x44, 0x45, 0x2d, 0x2a, 0x2d};
    
    private monpackage() {
        counter=0;
        //Pin creation
        pin = new OwnerPIN(PIN_TRY_LIMIT, MAX_PIN_SIZE);
        pin.update(user_pin, (short) 0, (byte) user_pin.length);
        
    }

	public static void install(byte[] bArray, short bOffset, byte bLength) 
	{
		// create an applet instance
		new monpackage().register(bArray, (short) (bOffset + 1), bArray[bOffset]);
	}
	
    public boolean select() {
        // The applet declines to be selected
        // if the pin is blocked.
        if (pin.getTriesRemaining() == 0) {
            return false;
        }
        return true;
    }

    public void deselect() {
        pin.reset();
    }
    
    public byte[] encryptCleSession(byte[] data, AESKey keyTrial, byte[] key) {
		keyTrial.setKey(key, (short)0);
		cipher = Cipher.getInstance(Cipher.ALG_AES_BLOCK_128_ECB_NOPAD, false);
		cipher.init(keyTrial, Cipher.MODE_ENCRYPT);
		byte[] encryptedData = new byte[16];
		cipher.doFinal(data, (short)0, (short)16, encryptedData, (short)0);
		return encryptedData;
    }
    
	public void process(APDU apdu)
	{
		byte[] buf = apdu.getBuffer();
		
		if (selectingApplet())
		{
			return;
		}
		
		if (buf[ISO7816.OFFSET_CLA] != CLA_MONPACKAGE) {
            ISOException.throwIt(ISO7816.SW_CLA_NOT_SUPPORTED);
        }
        
		switch (buf[ISO7816.OFFSET_INS])
		{
		case (byte)0x00:  
			break;
			
		case INS_SEND_INFO:
			if (!pin.isValidated()) {
				ISOException.throwIt(SW_PIN_VERIFICATION_REQUIRED);
			}
            byte[] encryptedCleSession = encryptCleSession(cleSession, elementSecretPatient, key);
            
			apdu.setOutgoing();
			apdu.setOutgoingLength((short)encryptedCleSession.length);
			apdu.sendBytesLong(encryptedCleSession, (short)0, (short)encryptedCleSession.length);
            break;
            
        case INS_VERIFY_PIN:
        	byte[] buffer = apdu.getBuffer();
			// retrieve the PIN data for validation.
			byte byteRead = (byte) (apdu.setIncomingAndReceive());

			// check pin
			// the PIN data is read into the APDU buffer
			// at the offset ISO7816.OFFSET_CDATA
			// the PIN data length = byteRead7F
			if (pin.check(buffer, ISO7816.OFFSET_CDATA, byteRead) == false) {
				ISOException.throwIt(SW_VERIFICATION_FAILED);
			}
			break;
			
		default:
			ISOException.throwIt(ISO7816.SW_INS_NOT_SUPPORTED);
		}
		
	}
}