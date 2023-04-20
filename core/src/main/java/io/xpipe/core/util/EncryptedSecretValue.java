package io.xpipe.core.util;

import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.experimental.SuperBuilder;
import lombok.extern.jackson.Jacksonized;

import java.nio.ByteBuffer;
import java.nio.CharBuffer;
import java.nio.charset.StandardCharsets;
import java.util.Base64;

@SuperBuilder
@Jacksonized
@EqualsAndHashCode
public class EncryptedSecretValue implements SecretValue {

    @Getter
    String encryptedValue;

    public EncryptedSecretValue(char[] c) {
        var utf8 = StandardCharsets.UTF_8.encode(CharBuffer.wrap(c));
        var bytes = new byte[utf8.limit()];
        utf8.get(bytes);
        encryptedValue = SecretValue.base64e(encrypt(bytes));
    }

    @Override
    public String toString() {
        return "<encrypted secret>";
    }

    @Override
    public char[] getSecret() {
        try {
            var bytes = Base64.getDecoder().decode(encryptedValue.replace("-", "/"));
            bytes = decrypt(bytes);
            var charBuffer = StandardCharsets.UTF_8.decode(ByteBuffer.wrap(bytes));
            var chars = new char[charBuffer.limit()];
            charBuffer.get(chars);
            return chars;
        } catch (Exception ex) {
            throw new IllegalStateException("Unable to decrypt secret");
        }
    }

    public byte[] encrypt(byte[] c) {
        throw new UnsupportedOperationException();
    }

    public byte[] decrypt(byte[] c) {
        throw new UnsupportedOperationException();
    }
}