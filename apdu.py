from Crypto.PublicKey import RSA
from Crypto.Cipher import AES, PKCS1_OAEP
from Crypto.Random import get_random_bytes
from smartcard.System import readers
from smartcard.util import toHexString, toBytes
from smartcard.CardRequest import CardRequest
from smartcard.CardType import ATRCardType
import sys
import os
# from tabulate import tabulate

os.system('color 9')
def clear(): return os.system('cls')


Title = '''
        _____   ____________          ___________
   _____\    \_ \           \        /           \ 
  /     /|     | \           \      /    _   _    \ 
 /     / /____/|  |    /\     |    /    //   \\\    \ 
|     | |____|/   |   |  |    |   /    //     \\\    \ 
|     |  _____    |    \/     |  /     \\\_____//     \ 
|\     \|\    \  /           /| /       \ ___ /       \ 
| \_____\|    | /___________/ |/________/|   |\________\ 
| |     /____/||           | /|        | |   | |        |
 \|_____|    |||___________|/ |________|/     \|________|
        |____|/
'''

elementSecretMedecin = ''


def select_AID():
    reader = readers()[0]
    conn = reader.createConnection()
    conn.connect()
    AID = toBytes('00 A4 04 00 06 11 22 33 44 55 66')
    data, sw1, sw2 = conn.transmit(AID)
    return conn


def compteur():
    if choice == 4:
        # Get the number in the counter
        GET_COUNTER = toBytes('B0 03 00 00 01')
        data, sw1, sw2 = conn.transmit(GET_COUNTER)
        print('Réponse : ' + toHexString(data))
        print('Code de statut : ' + toHexString([sw1, sw2]))


def RSAencrypt(input_file_path, output_file_path, public_key_path):
    # Charger le contenu du fichier
    with open(input_file_path, 'rb') as input_file:
        data = input_file.read()

    # Charger la clé publique RSA
    with open(public_key_path, 'rb') as public_key_file:
        public_key = RSA.import_key(public_key_file.read())

    # Chiffrer les données avec la clé publique RSA
    cipher_rsa = PKCS1_OAEP.new(public_key)
    encrypted_data = cipher_rsa.encrypt(data)

    # Écrire les données chiffrées dans un fichier de sortie
    with open(output_file_path, 'wb') as output_file:
        output_file.write(encrypted_data)


def RSAdecrypt(input_file_path, output_file_path, private_key_path):
    # Charger les données chiffrées à partir du fichier d'entrée
    with open(input_file_path, 'rb') as input_file:
        encrypted_data = input_file.read()

    # Charger la clé privée RSA
    with open(private_key_path, 'rb') as private_key_file:
        private_key = RSA.import_key(private_key_file.read())

    # Déchiffrer les données avec la clé privée RSA
    cipher_rsa = PKCS1_OAEP.new(private_key)
    decrypted_data = cipher_rsa.decrypt(encrypted_data)

    # Écrire les données déchiffrées dans un fichier de sortie
    with open(output_file_path, 'wb') as output_file:
        output_file.write(decrypted_data)


def AESencrypt(input_file_path, output_file_path, key):
    # Charger le contenu du fichier
    with open(input_file_path, 'rb') as input_file:
        data = input_file.read()

    # Générer un vecteur d'initialisation aléatoire
    iv = get_random_bytes(AES.block_size)

    # Chiffrer les données avec la clé AES et le vecteur d'initialisation
    cipher_aes = AES.new(key, AES.MODE_CBC, iv)
    ciphertext = cipher_aes.encrypt(data)

    # Écrire les données chiffrées dans un fichier de sortie
    with open(output_file_path, 'wb') as output_file:
        output_file.write(iv)
        output_file.write(ciphertext)


def AESdecrypt(input_file_path, output_file_path, key):
    # Charger les données chiffrées à partir du fichier d'entrée
    with open(input_file_path, 'rb') as input_file:
        iv = input_file.read(AES.block_size)
        ciphertext = input_file.read()

    # Déchiffrer les données avec la clé AES et le vecteur d'initialisation
    cipher_aes = AES.new(key, AES.MODE_CBC, iv)
    decrypted_data = cipher_aes.decrypt(ciphertext)

    # Écrire les données déchiffrées dans un fichier de sortie
    with open(output_file_path, 'wb') as output_file:
        output_file.write(decrypted_data)


if __name__ == '__main__':
    clear()
    print(Title)
    while True:
        conn = select_AID()
        encrypt('ordonnance.txt', 'fichier_chiffré', 'id_rsa.pub')
        decrypt('fichier_chiffré', 'fichier_déchiffré', 'id_rsa')
        cardtype = AnyCardType()

        cardrequest = CardRequest(timeout=1, cardType=cardtype)

        cardservice = cardrequest.waitforcard()