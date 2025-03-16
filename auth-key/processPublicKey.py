if __name__ == "__main__":
    with open("public_key.pem") as f:
        content = "".join(f.readlines())
        content = content.replace("\n", "")
        content = content.replace("-----BEGIN PUBLIC KEY-----", "")
        content = content.replace("-----END PUBLIC KEY-----", "")
        print(content)
