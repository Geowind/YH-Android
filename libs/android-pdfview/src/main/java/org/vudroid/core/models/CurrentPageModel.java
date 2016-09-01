<<<<<<< HEAD
package org.vudroid.core.models;

import org.vudroid.core.events.CurrentPageListener;
import org.vudroid.core.events.EventDispatcher;

public class CurrentPageModel extends EventDispatcher
{
    private int currentPageIndex;

    public void setCurrentPageIndex(int currentPageIndex)
    {
        if (this.currentPageIndex != currentPageIndex)
        {
            this.currentPageIndex = currentPageIndex;
            dispatch(new CurrentPageListener.CurrentPageChangedEvent(currentPageIndex));
        }
    }
}
=======
package org.vudroid.core.models;

import org.vudroid.core.events.CurrentPageListener;
import org.vudroid.core.events.EventDispatcher;

public class CurrentPageModel extends EventDispatcher
{
    private int currentPageIndex;

    public void setCurrentPageIndex(int currentPageIndex)
    {
        if (this.currentPageIndex != currentPageIndex)
        {
            this.currentPageIndex = currentPageIndex;
            dispatch(new CurrentPageListener.CurrentPageChangedEvent(currentPageIndex));
        }
    }
}
>>>>>>> fa4ffd90cd3f6a0db797ede37e42becda41540e9
