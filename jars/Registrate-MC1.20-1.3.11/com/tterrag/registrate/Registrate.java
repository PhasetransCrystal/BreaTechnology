package com.tterrag.registrate;

public class Registrate extends AbstractRegistrate<Registrate> {
    public static Registrate create(String modid) {
        Registrate ret = new Registrate(modid);
        ret.registerEventListeners(ret.getModEventBus());
        return ret;
    }

    protected Registrate(String modid) {
        super(modid);
    }
}