package com.app.CryptoAnalyser.model;

public class DecryptWord {
    private final String encrypted;
    private final String decrypted;
    private final String algorithm;

    public DecryptWord(String encrypted, String decrypted, String algorithm) {
        this.encrypted = encrypted;
        this.decrypted = decrypted;
        this.algorithm = algorithm;
    }

    public String getEncrypted() {
        return encrypted;
    }

    public String getDecrypted() {
        return decrypted;
    }

    public String getAlgorithm() {
        return algorithm;
    }
}
