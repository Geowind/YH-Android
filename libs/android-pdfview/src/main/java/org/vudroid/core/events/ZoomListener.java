<<<<<<< HEAD
package org.vudroid.core.events;

public interface ZoomListener
{
    void zoomChanged(float newZoom, float oldZoom);

    void commitZoom();

    public class CommitZoomEvent extends SafeEvent<ZoomListener>
    {
        @Override
        public void dispatchSafely(ZoomListener listener)
        {
            listener.commitZoom();
        }
    }
}
=======
package org.vudroid.core.events;

public interface ZoomListener
{
    void zoomChanged(float newZoom, float oldZoom);

    void commitZoom();

    public class CommitZoomEvent extends SafeEvent<ZoomListener>
    {
        @Override
        public void dispatchSafely(ZoomListener listener)
        {
            listener.commitZoom();
        }
    }
}
>>>>>>> fa4ffd90cd3f6a0db797ede37e42becda41540e9
