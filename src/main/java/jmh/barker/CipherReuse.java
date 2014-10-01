package jmh.barker;

import java.io.IOException;
import java.security.InvalidAlgorithmParameterException;
import java.security.InvalidKeyException;
import java.security.Key;
import java.security.NoSuchAlgorithmException;
import java.security.NoSuchProviderException;
import java.security.SecureRandom;
import java.security.Security;
import java.security.spec.InvalidParameterSpecException;
import java.util.concurrent.TimeUnit;

import javax.crypto.Cipher;
import javax.crypto.NoSuchPaddingException;
import javax.crypto.spec.GCMParameterSpec;
import javax.crypto.spec.IvParameterSpec;

import org.bouncycastle.jce.provider.BouncyCastleProvider;
import org.openjdk.jmh.annotations.Benchmark;
import org.openjdk.jmh.annotations.BenchmarkMode;
import org.openjdk.jmh.annotations.Mode;
import org.openjdk.jmh.annotations.OutputTimeUnit;
import org.openjdk.jmh.annotations.Scope;
import org.openjdk.jmh.annotations.Setup;
import org.openjdk.jmh.annotations.State;

/**
 * A benchmark to measure the time to init a new Cipher instance vs. reusing an existing one
 */
@State(Scope.Thread)
@OutputTimeUnit(TimeUnit.MICROSECONDS)
@BenchmarkMode(Mode.SampleTime)
public class CipherReuse
{
    private static final byte[] AAD = "Welcome2CassandrasAdditionalAuth".getBytes();
    private static final int TLEN = 128; //used by GCM for ADD length to use
    private final int gcmIvLength;

    private Key gcmKey;
    private Key cbcKey;
    private Cipher cipher;
    private SecureRandom secureRandom;
    private int cipherMode;

    static
    {
        Security.addProvider(new BouncyCastleProvider());
    }

    public CipherReuse()
    {
        // 12 seems to be the 'standard' IV length for GCM, see:
        // http://crypto.stackexchange.com/questions/5807/aes-gcm-and-its-iv-nonce-value
        this(true, 12);
    }

    public CipherReuse(boolean encrypt, int gcmIvLength)
    {
        cipherMode = encrypt ? Cipher.ENCRYPT_MODE : Cipher.DECRYPT_MODE;
        this.gcmIvLength = gcmIvLength;
    }

    @Setup
    public void setup() throws IOException, NoSuchAlgorithmException
    {
        KeyStoreLoader loader = new KeyStoreLoader();
        gcmKey = loader.readFromKeyStore(KeyStoreLoader.GCM_KEY);
        cbcKey = loader.readFromKeyStore(KeyStoreLoader.CBC_KEY);
        secureRandom = SecureRandom.getInstance("SHA1PRNG");
    }

    @Benchmark
    public Cipher newCipher_CBC_JDKDefault() throws InvalidAlgorithmParameterException, InvalidKeyException, NoSuchPaddingException, NoSuchAlgorithmException, NoSuchProviderException
    {
        return newCipher_CBC(false);
    }

    @Benchmark
    public Cipher newCipher_CBC_BouncyCastle() throws InvalidAlgorithmParameterException, InvalidKeyException, NoSuchPaddingException, NoSuchAlgorithmException, NoSuchProviderException
    {
        return newCipher_CBC(true);
    }

    public Cipher newCipher_CBC(boolean useBouncyCastle) throws InvalidAlgorithmParameterException, InvalidKeyException, NoSuchPaddingException, NoSuchAlgorithmException, NoSuchProviderException
    {

        Cipher cipher;
        if (useBouncyCastle)
            cipher = Cipher.getInstance(KeyStoreLoader.CBC_ALG, "BC");
        else
            cipher = Cipher.getInstance(KeyStoreLoader.CBC_ALG);
        byte[] iv = new byte[16];
        secureRandom.nextBytes(iv);
        cipher.init(cipherMode, cbcKey, new IvParameterSpec(iv));

        return cipher;
    }

    @Benchmark
    public Cipher newCipher_GCM_JDKDefault() throws NoSuchPaddingException, NoSuchAlgorithmException, InvalidAlgorithmParameterException, InvalidKeyException, NoSuchProviderException
    {
        return newCipher_GCM(false);
    }

    @Benchmark
    public Cipher newCipher_GCM_BoucyCastle() throws NoSuchPaddingException, NoSuchAlgorithmException, InvalidAlgorithmParameterException, InvalidKeyException, NoSuchProviderException
    {
        return newCipher_GCM(true);
    }

    public Cipher newCipher_GCM(boolean useBouncyCastle) throws NoSuchPaddingException, NoSuchAlgorithmException, InvalidAlgorithmParameterException, InvalidKeyException, NoSuchProviderException
    {
        Cipher cipher;
        if(useBouncyCastle)
            cipher = Cipher.getInstance(KeyStoreLoader.GCM_ALG, "BC");
        else
            cipher = Cipher.getInstance(KeyStoreLoader.GCM_ALG);
        byte[] iv = new byte[gcmIvLength];
        secureRandom.nextBytes(iv);
        cipher.init(cipherMode, gcmKey, new GCMParameterSpec(TLEN, iv));
        cipher.updateAAD(AAD);

        return cipher;
    }

    /**
     * note: looks like most cost comes from calling SecureRandom
     */
    @Benchmark
    public Cipher reuseCipher_CBC_newIV_JDKDefault() throws NoSuchAlgorithmException, NoSuchPaddingException, InvalidAlgorithmParameterException, InvalidKeyException, NoSuchProviderException
    {
        if (cipher == null)
        {
            cipher = newCipher_CBC(false);
            return cipher;
        }

        // this reinit might not be totally realistic, especially using a new IV, but let's bench it anyways
        byte[] iv = new byte[16];
        secureRandom.nextBytes(iv);
        cipher.init(cipherMode, gcmKey, new IvParameterSpec(iv));

        return cipher;
    }

    /**
     * note: looks like most cost comes from calling SecureRandom
     */
    @Benchmark
    public Cipher reuseCipher_CBC_newIV_BouncyCastle() throws NoSuchAlgorithmException, NoSuchPaddingException, InvalidAlgorithmParameterException, InvalidKeyException, NoSuchProviderException
    {
        if (cipher == null)
        {
            cipher = newCipher_CBC(true);
            return cipher;
        }

        // this reinit might not be totally realistic, especially using a new IV, but let's bench it anyways
        byte[] iv = new byte[16];
        secureRandom.nextBytes(iv);
        cipher.init(cipherMode, gcmKey, new IvParameterSpec(iv));

        return cipher;
    }

    Cipher reuseCipher_CBC_newIV(boolean useBouncyCastle) throws NoSuchAlgorithmException, NoSuchPaddingException, InvalidAlgorithmParameterException, InvalidKeyException, NoSuchProviderException
    {
        if (cipher == null)
        {
            cipher = newCipher_CBC(useBouncyCastle);
            return cipher;
        }

        // this reinit might not be totally realistic, especially using a new IV, but let's bench it anyways
        byte[] iv = new byte[16];
        secureRandom.nextBytes(iv);
        cipher.init(cipherMode, gcmKey, new IvParameterSpec(iv));

        return cipher;
    }

    /**
     * This bench should be stoopid fast, but just including it for comparison's sake
     */
    public Cipher reuseCipher_CBC_sameIV(boolean useBouncyCastle) throws NoSuchAlgorithmException, NoSuchPaddingException, InvalidAlgorithmParameterException, InvalidKeyException, NoSuchProviderException
    {
        if (cipher == null)
        {
            cipher = newCipher_CBC(useBouncyCastle);
        }

        return cipher;
    }

    /**
     * Javadoc for Cipher example of using GCM shows calling cipher.getParameters().getParameterSpec(GCMParameterSpec.class) to get previous TLEN.
     * Compared to just creating a new GCMParameterSpec (with a known TLEN, that you would have need to create the initial GCMParameterSpec, anyways),
     * this is marginally slower, but keeping the test around.
     */
    @Benchmark
    public Cipher reuseCipher_GCM_callOldParamSpec_BouncyCastle() throws InvalidParameterSpecException, InvalidAlgorithmParameterException, InvalidKeyException, NoSuchAlgorithmException, NoSuchPaddingException, NoSuchProviderException
    {
        return reuseCipher_GCM_callOldParamSpec(true);
    }

    @Benchmark
    public Cipher reuseCipher_GCM_callOldParamSpec_JDKDefault() throws InvalidParameterSpecException, InvalidAlgorithmParameterException, InvalidKeyException, NoSuchAlgorithmException, NoSuchPaddingException, NoSuchProviderException
    {
        return reuseCipher_GCM_callOldParamSpec(false);
    }

    Cipher reuseCipher_GCM_callOldParamSpec(boolean useBouncyCastle) throws InvalidParameterSpecException, InvalidAlgorithmParameterException, InvalidKeyException, NoSuchAlgorithmException, NoSuchPaddingException, NoSuchProviderException
    {
        if (cipher == null)
        {
            cipher = newCipher_GCM(useBouncyCastle);
            return cipher;
        }

        GCMParameterSpec previous = cipher.getParameters().getParameterSpec(GCMParameterSpec.class);
        byte[] iv = new byte[gcmIvLength];
        secureRandom.nextBytes(iv);

        cipher.init(cipherMode, gcmKey, new GCMParameterSpec(previous.getTLen(), iv));
        cipher.updateAAD(AAD);

        return cipher;
    }

    @Benchmark
    public Cipher reuseCipher_GCM_newParamSpec_JDKDefault() throws InvalidParameterSpecException, InvalidAlgorithmParameterException, InvalidKeyException, NoSuchAlgorithmException, NoSuchPaddingException, NoSuchProviderException
    {
        return reuseCipher_GCM_newParamSpec(false);
    }

    @Benchmark
    public Cipher reuseCipher_GCM_newParamSpec_BouncyCastle() throws InvalidParameterSpecException, InvalidAlgorithmParameterException, InvalidKeyException, NoSuchAlgorithmException, NoSuchPaddingException, NoSuchProviderException
    {
        return reuseCipher_GCM_newParamSpec(true);
    }

    Cipher reuseCipher_GCM_newParamSpec(boolean useBouncyCastle) throws InvalidParameterSpecException, InvalidAlgorithmParameterException, InvalidKeyException, NoSuchAlgorithmException, NoSuchPaddingException, NoSuchProviderException
    {
        if (cipher == null)
        {
            cipher = newCipher_GCM(useBouncyCastle);
            return cipher;
        }

        byte[] iv = new byte[gcmIvLength];
        secureRandom.nextBytes(iv);

        cipher.init(cipherMode, gcmKey, new GCMParameterSpec(TLEN, iv));
        cipher.updateAAD(AAD);

        return cipher;
    }
}
