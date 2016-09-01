<<<<<<< HEAD
package org.vudroid.core;

public class VuDroidLibraryLoader
{
    private static boolean alreadyLoaded = false;

    public static void load()
    {
        if (alreadyLoaded)
        {
            return;
        }
        System.loadLibrary("vudroid");
        alreadyLoaded = true;
    }
}
=======
package org.vudroid.core;

public class VuDroidLibraryLoader
{
    private static boolean alreadyLoaded = false;

    public static void load()
    {
        if (alreadyLoaded)
        {
            return;
        }
        System.loadLibrary("vudroid");
        alreadyLoaded = true;
    }
}
>>>>>>> fa4ffd90cd3f6a0db797ede37e42becda41540e9
