<<<<<<< HEAD
package org.vudroid.core.models;

import org.vudroid.core.events.DecodingProgressListener;
import org.vudroid.core.events.EventDispatcher;

public class DecodingProgressModel extends EventDispatcher
{
    private int currentlyDecoding;

    public void increase()
    {
        currentlyDecoding++;
        dispatchChanged();
    }

    private void dispatchChanged()
    {
        dispatch(new DecodingProgressListener.DecodingProgressEvent(currentlyDecoding));
    }

    public void decrease()
    {
        currentlyDecoding--;
        dispatchChanged();
    }
}
=======
package org.vudroid.core.models;

import org.vudroid.core.events.DecodingProgressListener;
import org.vudroid.core.events.EventDispatcher;

public class DecodingProgressModel extends EventDispatcher
{
    private int currentlyDecoding;

    public void increase()
    {
        currentlyDecoding++;
        dispatchChanged();
    }

    private void dispatchChanged()
    {
        dispatch(new DecodingProgressListener.DecodingProgressEvent(currentlyDecoding));
    }

    public void decrease()
    {
        currentlyDecoding--;
        dispatchChanged();
    }
}
>>>>>>> fa4ffd90cd3f6a0db797ede37e42becda41540e9
