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
 * A benchmark to measure the time to decrypt a random block of data using GCM vs. CBC block cipher modes.
 * replies on {@link CipherReuse} to create ciphers.
 */
@State(Scope.Thread)
@OutputTimeUnit(TimeUnit.MICROSECONDS)
@BenchmarkMode(Mode.SampleTime)
public class DecryptBench
{
    private CipherReuse decryptCipher;
    private Random random;
    private byte[][] buffers;
    private int curBuf;

    @Setup
    public void setup() throws IOException, NoSuchAlgorithmException
    {
        // 12 seems to be the 'standard' IV length for GCM, see:
        // http://crypto.stackexchange.com/questions/5807/aes-gcm-and-its-iv-nonce-value
        decryptCipher = new CipherReuse(true, 12);
        decryptCipher.setup();
        random = new Random();

        int bufCnt = 16;
        buffers = new byte[bufCnt][];
        for (int i = 0; i < bufCnt; i++)
            buffers[i] = generateCleartext();
    }

    /**
     * create a random blob between 32 and 64 kb.
     */
    public byte[] generateCleartext()
    {
        int size = 32 * 1024;
        size += random.nextInt(size);
        byte[] buf = new byte[size];
        random.nextBytes(buf);
        return buf;
    }

    @Benchmark
    public byte[] decrypt_CBC_bouncycastle() throws NoSuchProviderException, InvalidAlgorithmParameterException, NoSuchAlgorithmException, InvalidKeyException, NoSuchPaddingException, BadPaddingException, IllegalBlockSizeException
    {
        Cipher cipher = decryptCipher.reuseCipher_CBC_newIV(true);
        curBuf++;
        if (curBuf == buffers.length)
            curBuf = 0;
        return cipher.doFinal(buffers[curBuf]);
    }

    @Benchmark
    public byte[] decrypt_CBC_JDKDefault() throws NoSuchProviderException, InvalidAlgorithmParameterException, NoSuchAlgorithmException, InvalidKeyException, NoSuchPaddingException, BadPaddingException, IllegalBlockSizeException
    {
        Cipher cipher = decryptCipher.reuseCipher_CBC_newIV(false);
        curBuf++;
        if (curBuf == buffers.length)
            curBuf = 0;
        return cipher.doFinal(buffers[curBuf]);
    }

//    @Benchmark
//    public byte[] decrypt_GCM_bouncycastle() throws NoSuchProviderException, InvalidAlgorithmParameterException, NoSuchAlgorithmException, InvalidKeyException, NoSuchPaddingException, BadPaddingException, IllegalBlockSizeException, InvalidParameterSpecException
//    {
//        Cipher cipher = decryptCipher.reuseCipher_GCM_newParamSpec(true);
//        return cipher.doFinal(generateCleartext());
//    }
//
//    @Benchmark
//    public byte[] decrypt_GCM_JDKDefault() throws NoSuchPaddingException, InvalidKeyException, NoSuchAlgorithmException, NoSuchProviderException, InvalidAlgorithmParameterException, InvalidParameterSpecException, BadPaddingException, IllegalBlockSizeException
//    {
//        Cipher cipher = decryptCipher.reuseCipher_GCM_newParamSpec(false);
//        return cipher.doFinal(generateCleartext());
//    }
}
