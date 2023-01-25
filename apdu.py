from smartcard.System import readers

reader = readers()[0]
conn = reader.createConnection()
conn.connect()

# Select the AID
conn.transmit([0x00, 0xA4, 0x04, 0x00, 0x06, 0x11, 0x22, 0x33, 0x44, 0x55, 0x66])

# Increment the counter
conn.transmit([0xB0, 0x01, 0x00, 0x00])

# Get the response and print it
response, sw1, sw2 = conn.transmit([0xB0, 0x03, 0x00, 0x00, 0x01])
azeaze
print(response, sw1, sw2)aa
