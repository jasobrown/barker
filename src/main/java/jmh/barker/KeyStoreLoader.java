package jmh.barker;

import java.io.FileInputStream;
import java.io.IOException;
import java.security.Key;
import java.security.KeyStore;
import java.security.KeyStoreException;
import java.security.NoSuchAlgorithmException;
import java.security.UnrecoverableKeyException;

import org.openjdk.jmh.util.FileUtils;

public class KeyStoreLoader
{
    public static final String CBC_ALG = "AES/CBC/PKCS5Padding";
    public static final String CBC_KEY = CBC_ALG + ":1".toLowerCase();

    public static final String GCM_ALG = "AES/GCM/NoPadding";
    public static final String GCM_KEY = GCM_ALG + ":1".toLowerCase();

    private KeyStore store;

    public KeyStoreLoader()
    {
        FileInputStream inputStream = null;
        try
        {
            inputStream = new FileInputStream("/tmp/keystore");
            store = KeyStore.getInstance("jceks");
            store.load(inputStream, "cassandra".toCharArray());
        }
        catch (Exception e)
        {
            throw new RuntimeException("couldn't load keystore", e);
        }
        finally
        {
            FileUtils.safelyClose(inputStream);
        }
    }

    public Key readFromKeyStore(String alias) throws IOException
    {
        FileInputStream inputStream = null;
        try
        {
            Key key = store.getKey(alias, "cassandra".toCharArray());
            if (key == null)
                throw new IOException(String.format("key %s was not found in keystore", alias));
            return key;
        }
        catch (KeyStoreException | UnrecoverableKeyException | NoSuchAlgorithmException e)
        {
            throw new IOException("failed to load secret key", e);
        }
        finally
        {
            FileUtils.safelyClose(inputStream);
        }
    }

    public KeyStore store()
    {
        return store;
    }
}
