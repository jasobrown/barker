package jmh.barker;

import java.io.IOException;
import java.security.InvalidAlgorithmParameterException;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.security.NoSuchProviderException;
import java.security.spec.InvalidParameterSpecException;
import java.util.Random;
import java.util.concurrent.TimeUnit;

import javax.crypto.BadPaddingException;
import javax.crypto.Cipher;
import javax.crypto.IllegalBlockSizeException;
import javax.crypto.NoSuchPaddingException;

import org.openjdk.jmh.annotations.Benchmark;
import org.openjdk.jmh.annotations.BenchmarkMode;
import org.openjdk.jmh.annotations.Mode;
import org.openjdk.jmh.annotations.OutputTimeUnit;
import org.openjdk.jmh.annotations.Scope;
import org.openjdk.jmh.annotations.Setup;
import org.openjdk.jmh.annotations.State;

/**
 * A benchmark to measure the time to encrypt a random block of data using GCM vs. CBC block cipher modes.
 * replies on {@link jmh.barker.CipherReuse} to create ciphers.
 */
@State(Scope.Thread)
@OutputTimeUnit(TimeUnit.MICROSECONDS)
@BenchmarkMode(Mode.SampleTime)
public class EncryptBench
{
    private CipherReuse encryptCipher;
    private Random random;

    @Setup
    public void setup() throws IOException, NoSuchAlgorithmException
    {
        // 12 seems to be the 'standard' IV length for GCM, see:
        // http://crypto.stackexchange.com/questions/5807/aes-gcm-and-its-iv-nonce-value
        encryptCipher = new CipherReuse(true, 12);
        encryptCipher.setup();
        random = new Random();
    }

    @Benchmark
    public byte[] encrypt_CBC_bouncycastle() throws NoSuchProviderException, InvalidAlgorithmParameterException, NoSuchAlgorithmException, InvalidKeyException, NoSuchPaddingException, BadPaddingException, IllegalBlockSizeException
    {
        Cipher cipher = encryptCipher.reuseCipher_CBC_sameIV(true);
        return cipher.doFinal(generateCleartext());
    }

    /**
     * create a random blob between 32 and 64 kb. including this test as all encrypt methods depend on it, and we can get a sense of how long creation
     * of the plaintext buffer takes and subtract from the encrypt tests.
     */
    @Benchmark
    public byte[] generateCleartext()
    {
        int size = 32 * 1024;
        size += random.nextInt(size);
        byte[] buf = new byte[size];
        random.nextBytes(buf);
        return buf;
    }

    @Benchmark
    public byte[] encrypt_CBC_JDKDefault() throws NoSuchProviderException, InvalidAlgorithmParameterException, NoSuchAlgorithmException, InvalidKeyException, NoSuchPaddingException, BadPaddingException, IllegalBlockSizeException
    {
        Cipher cipher = encryptCipher.reuseCipher_CBC_sameIV(false);
        return cipher.doFinal(generateCleartext());
    }

    @Benchmark
    public byte[] encrypt_GCM_bouncycastle() throws NoSuchProviderException, InvalidAlgorithmParameterException, NoSuchAlgorithmException, InvalidKeyException, NoSuchPaddingException, BadPaddingException, IllegalBlockSizeException, InvalidParameterSpecException
    {
        Cipher cipher = encryptCipher.reuseCipher_GCM_newParamSpec(true);
        return cipher.doFinal(generateCleartext());
    }

    @Benchmark
    public byte[] encrypt_GCM_JDKDefault() throws NoSuchPaddingException, InvalidKeyException, NoSuchAlgorithmException, NoSuchProviderException, InvalidAlgorithmParameterException, InvalidParameterSpecException, BadPaddingException, IllegalBlockSizeException
    {
        Cipher cipher = encryptCipher.reuseCipher_GCM_newParamSpec(false);
        return cipher.doFinal(generateCleartext());
    }
}
