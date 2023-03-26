package monpackage;

import javacard.framework.*;

public class monpackage extends Applet
{
	// CLA byte declaration
	public static final byte CLA_MONPACKAGE= (byte) 0xB0;
	
	// INS declaration
    public static final byte INS_INCREMENTER_COUNTER= (byte) 0x01;
    public static final byte INS_DECREMENTER_COUNTER= (byte) 0x02;
    public static final byte INS_INTERROGER_COUNTER= (byte) 0x03;
    public static final byte INS_INITIALISER_COUNTER= (byte) 0x04;
    public static final byte INS_VERIFY_PIN= (byte) 0x05;
    
    // PIN related declaration
    final static byte PIN_TRY_LIMIT = (byte) 0x03;
    final static byte MAX_PIN_SIZE = (byte) 0x04;
    final static short SW_VERIFICATION_FAILED = 0x6300;
    final static short SW_PIN_VERIFICATION_REQUIRED = (short) 0x9004;
    
    OwnerPIN pin;
    private byte counter;
    private byte[] user_pin = {0x01, 0x02, 0x03, 0x04};
	
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
			
		case INS_INCREMENTER_COUNTER:
			if (!pin.isValidated()) {
				ISOException.throwIt(SW_PIN_VERIFICATION_REQUIRED);
			}
            counter++;
            break;
            
        case INS_DECREMENTER_COUNTER:
			if (!pin.isValidated()) {
				ISOException.throwIt(SW_PIN_VERIFICATION_REQUIRED);
			}
            counter--;
            break;
            
        case INS_INTERROGER_COUNTER:
            buf[2]=counter;
            apdu.setOutgoingAndSend((short)2, (short)1);
            break;
            
        case INS_INITIALISER_COUNTER:
        	if (!pin.isValidated()) {
				ISOException.throwIt(SW_PIN_VERIFICATION_REQUIRED);
			}
            apdu.setIncomingAndReceive();
            counter=buf[ISO7816.OFFSET_P1];
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
