package com.phasetranscrystal.breatechnology.common;

import com.phasetranscrystal.brealib.*;
import com.phasetranscrystal.brealib.api.registrate.BreaRegistrate;
import com.phasetranscrystal.breatechnology.BreaTechnology;

public class CommonProxy {

    public static final BreaRegistrate BreaTech = BreaRegistrate.create(BreaTechnology.MOD_ID);

    public CommonProxy() {
        CommonProxy.init();
        var eventBus = BreaTechnology.getModEventBus();
        eventBus.register(this);
        BreaTech.registerEventListeners(eventBus);
    }

    public static void init() {}
}
