import bcrypt
import argparse

def crypt(password):
    return bcrypt.hashpw(password, bcrypt.gensalt(rounds = 10, prefix=b"2a"))

if __name__ == "__main__":
    parser = argparse.ArgumentParser()
    parser.add_argument('-p', dest='password', help="asdasd")
    result=parser.parse_args()
    print(crypt(result.password))