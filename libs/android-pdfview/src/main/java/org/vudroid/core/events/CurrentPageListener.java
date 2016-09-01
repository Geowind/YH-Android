<<<<<<< HEAD
package org.vudroid.core.events;

public interface CurrentPageListener
{
    void currentPageChanged(int pageIndex);

    public class CurrentPageChangedEvent extends SafeEvent<CurrentPageListener>
    {
        private final int pageIndex;

        public CurrentPageChangedEvent(int pageIndex)
        {
            this.pageIndex = pageIndex;
        }

        @Override
        public void dispatchSafely(CurrentPageListener listener)
        {
            listener.currentPageChanged(pageIndex);
        }
    }
}
=======
package org.vudroid.core.events;

public interface CurrentPageListener
{
    void currentPageChanged(int pageIndex);

    public class CurrentPageChangedEvent extends SafeEvent<CurrentPageListener>
    {
        private final int pageIndex;

        public CurrentPageChangedEvent(int pageIndex)
        {
            this.pageIndex = pageIndex;
        }

        @Override
        public void dispatchSafely(CurrentPageListener listener)
        {
            listener.currentPageChanged(pageIndex);
        }
    }
}
>>>>>>> fa4ffd90cd3f6a0db797ede37e42becda41540e9
