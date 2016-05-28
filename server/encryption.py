from bcrypt import hashpw, checkpw, gensalt
from Crypto.Cipher import AES
from Crypto import Random
import random
import string

from settings import *

def pad(s):
    return s + b"\0" * (AES.block_size - len(s) % AES.block_size)

def encrypt(message, key, key_size=256):
    message = pad(message)
    iv = Random.new().read(AES.block_size)
    cipher = AES.new(key, AES.MODE_CBC, iv)
    return iv + cipher.encrypt(message)

def decrypt(ciphertext, key):
    iv = ciphertext[:AES.block_size]
    cipher = AES.new(key, AES.MODE_CBC, iv)
    plaintext = cipher.decrypt(ciphertext[AES.block_size:])
    return plaintext.rstrip(b"\0")

def encrypt_file(file_name, key):
    with open(file_name, 'rb') as fo:
        plaintext = fo.read()
    enc = encrypt(plaintext, key)
    with open(file_name + ENCRYPTED_FILE_POSTFIX, 'wb') as fo:
        fo.write(enc)

def decrypt_file(file_name, key):
    with open(file_name + ENCRYPTED_FILE_POSTFIX, 'rb') as fo:
        ciphertext = fo.read()
    dec = decrypt(ciphertext, key)
    with open(file_name, 'wb') as fo:
        fo.write(dec)

def get_key():
    return ''.join(
        random.choice(string.ascii_uppercase + string.digits + string.ascii_lowercase)
        for _ in range(32)
    )

def hash_key(key):
    return hashpw('testing', gensalt())

def check_key(key, hashed_key):
    return checkpw(key, hashed_key)
