/*
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package jmh.barker;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.security.Key;
import java.security.KeyStore;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.util.concurrent.TimeUnit;
import javax.crypto.Cipher;
import javax.crypto.NoSuchPaddingException;
import javax.crypto.spec.IvParameterSpec;

import org.openjdk.jmh.annotations.Benchmark;
import org.openjdk.jmh.annotations.BenchmarkMode;
import org.openjdk.jmh.annotations.Fork;
import org.openjdk.jmh.annotations.Measurement;
import org.openjdk.jmh.annotations.Mode;
import org.openjdk.jmh.annotations.OutputTimeUnit;
import org.openjdk.jmh.annotations.Param;
import org.openjdk.jmh.annotations.Scope;
import org.openjdk.jmh.annotations.State;
import org.openjdk.jmh.annotations.Threads;
import org.openjdk.jmh.annotations.Warmup;
import org.openjdk.jmh.infra.Blackhole;

@BenchmarkMode({Mode.Throughput, Mode.AverageTime})
@OutputTimeUnit(TimeUnit.MICROSECONDS)
@Warmup(iterations = 5, time = 1, timeUnit = TimeUnit.SECONDS)
@Measurement(iterations = 5, time = 2, timeUnit = TimeUnit.SECONDS)
@Fork(value = 1,jvmArgsAppend = "-Xmx512M")
@Threads(3)
@State(Scope.Benchmark)
public class CipherBench
{
    @Param({"AES/CBC/PKCS5Padding"})
    private String cipher = "AES/CBC/PKCS5Padding";

    static Key key = loadKey();
    static Key loadKey()
    {
        try
        {
            FileInputStream inputStream = new FileInputStream(new File("/opt/dev/cassandra/test/conf/cassandra.keystore"));
            KeyStore store = KeyStore.getInstance("JCEKS");
            store.load(inputStream, "cassandra".toCharArray());
            return store.getKey("testing:1", "cassandra".toCharArray());
        }
        catch (Exception e)
        {
            throw new RuntimeException("failed to read key", e);
        }
    }

    byte[] iv = iv();
    static byte[] iv()
    {
        byte[] iv = new byte[16];
        new SecureRandom().nextBytes(iv);
        return iv;
    }

    private final ThreadLocal<Cipher> cipherThreadLocal = new ThreadLocal<Cipher>()
    {
        protected Cipher initialValue()
        {
            try
            {
                return Cipher.getInstance(cipher);
            }
            catch (NoSuchAlgorithmException | NoSuchPaddingException e)
            {
                throw new RuntimeException(e);
            }
        }
    };

    @Benchmark
    public void cipherGetInstance(Blackhole bh) throws Exception
    {
        Cipher cipher = Cipher.getInstance(this.cipher);
        cipher.init(Cipher.DECRYPT_MODE, key, new IvParameterSpec(iv));
        bh.consume(cipher);
    }

    @Benchmark
    public void cipherThreadLocal(Blackhole bh) throws Exception
    {
        Cipher c = cipherThreadLocal.get();
        c.init(Cipher.DECRYPT_MODE, key, new IvParameterSpec(iv));
        bh.consume(cipher);
    }
}