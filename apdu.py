from smartcard.System import readers

print("\"1\" - Incrémenter le compteur de 1")
print("\"2\" - Décrémenter le compteur de 1")
print("\"3\" - Initialiser le compteur à votre nombre")
print("\"4\" - Interroger le compteur")
choice = input("Entrez votre choix (1, 2, 3) : \n")

reader = readers()[0]
conn = reader.createConnection()
conn.connect()

# Select the AID
conn.transmit([0x00, 0xA4, 0x04, 0x00, 0x06, 0x11, 0x22, 0x33, 0x44, 0x55, 0x66])

if choice == 1 or choice == 2:
    pin_transformed = []
    pin = str(input("Entrez votre code pin"))
    for i in pin:
        pin_transformed.append(hex(int(i)))
    conn.transmit([0xB0, 0x05, 0x00, 0x00, 0x04, pin_transformed[0], pin_transformed[1], pin_transformed[2], pin_transformed[3], 0x7F])
    # B0 05 00 00 04 01 02 03 04 7F
    # Increment or Decrement the counter
    conn.transmit([0xB0, hex(choice), 0x00, 0x00])

elif choice == 3:
    pin_transformed = []
    pin = str(input("Entrez votre code pin"))
    for i in pin:
        pin_transformed.append(hex(int(i)))
    conn.transmit([0xB0, 0x05, 0x00, 0x00, 0x04, pin_transformed[0], pin_transformed[1], pin_transformed[2], pin_transformed[3], 0x7F])
    # Initialise the counter
    conn.transmit([0x00, 0xA4, 0x04, 0x00, 0x06, 0x11, 0x22, 0x33, 0x44, 0x55, 0x66])
    num = input("Entrez le nombre de votre choix (max 255) :")
    conn.transmit([0xB0, 0x04, hex(num), 0x00])

elif choice == 4:
    # Get the number in the counter
    conn.transmit([0xB0, 0x03, 0x00, 0x00, 0x01])

# Get the response and print it
response, sw1, sw2 = conn.transmit([0xB0, 0x03, 0x00, 0x00, 0x01])
print(response, sw1, sw2)
