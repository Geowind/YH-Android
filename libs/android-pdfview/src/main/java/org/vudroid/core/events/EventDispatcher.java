<<<<<<< HEAD
package org.vudroid.core.events;

import java.util.ArrayList;

@SuppressWarnings("rawtypes")
public class EventDispatcher
{
    private final ArrayList<Object> listeners = new ArrayList<Object>();

	public void dispatch(Event event)
    {
        for (Object listener : listeners)
        {
            event.dispatchOn(listener);
        }
    }

    public void addEventListener(Object listener)
    {
        listeners.add(listener);
    }

    public void removeEventListener(Object listener)
    {
        listeners.remove(listener);
    }
}
=======
package org.vudroid.core.events;

import java.util.ArrayList;

@SuppressWarnings("rawtypes")
public class EventDispatcher
{
    private final ArrayList<Object> listeners = new ArrayList<Object>();

	public void dispatch(Event event)
    {
        for (Object listener : listeners)
        {
            event.dispatchOn(listener);
        }
    }

    public void addEventListener(Object listener)
    {
        listeners.add(listener);
    }

    public void removeEventListener(Object listener)
    {
        listeners.remove(listener);
    }
}
>>>>>>> fa4ffd90cd3f6a0db797ede37e42becda41540e9
