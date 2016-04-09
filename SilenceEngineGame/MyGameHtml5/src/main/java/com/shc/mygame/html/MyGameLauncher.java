package com.shc.mygame.html;

import com.google.gwt.core.client.EntryPoint;
import com.shc.silenceengine.backend.gwt.GwtRuntime;
import com.shc.mygame.MyGame;

public class MyGameLauncher implements EntryPoint
{
    @Override
    public void onModuleLoad()
    {
        GwtRuntime.start(new MyGame());
    }
}
