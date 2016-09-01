<<<<<<< HEAD
package org.vudroid.core.utils;

import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

public class MD5StringUtil
{
    private static final MessageDigest digest;

    static
    {
        try
        {
            digest = MessageDigest.getInstance("MD5");
        }
        catch (NoSuchAlgorithmException e)
        {
            throw new RuntimeException(e);
        }
    }

    public static String md5StringFor(String s)
    {
        final byte[] hash = digest.digest(s.getBytes());
        final StringBuilder builder = new StringBuilder();
        for (byte b : hash)
        {
            builder.append(Integer.toString(b & 0xFF, 16));    
        }
        return builder.toString();
    }
}
=======
package org.vudroid.core.utils;

import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

public class MD5StringUtil
{
    private static final MessageDigest digest;

    static
    {
        try
        {
            digest = MessageDigest.getInstance("MD5");
        }
        catch (NoSuchAlgorithmException e)
        {
            throw new RuntimeException(e);
        }
    }

    public static String md5StringFor(String s)
    {
        final byte[] hash = digest.digest(s.getBytes());
        final StringBuilder builder = new StringBuilder();
        for (byte b : hash)
        {
            builder.append(Integer.toString(b & 0xFF, 16));    
        }
        return builder.toString();
    }
}
>>>>>>> fa4ffd90cd3f6a0db797ede37e42becda41540e9
