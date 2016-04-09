package com.shc.mygame.desktop;

import com.shc.silenceengine.backend.lwjgl.LwjglRuntime;
import com.shc.mygame.MyGame;

public class MyGameLauncher
{
    public static void main(String[] args)
    {
        LwjglRuntime.start(new MyGame());
    }
}
