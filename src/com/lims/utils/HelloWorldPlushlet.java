package com.lims.utils;

import nl.justobjects.pushlet.core.Event;
import nl.justobjects.pushlet.core.EventPullSource;

/**
 * Created by qulongjun on 2017/3/8.
 */
public class HelloWorldPlushlet {
    static public class HwPlushlet extends EventPullSource {
        // 休眠五秒
        @Override
        protected long getSleepTime() {
            return 5000;
        }

        @Override
        protected Event pullEvent() {
            int i = 0;
            Event event = Event.createDataEvent("/cuige/he");
            event.setField("mess", "i:" + i++);
            return event;
        }
    }
}
