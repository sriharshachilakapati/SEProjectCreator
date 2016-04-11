package ${packageName}.desktop;

import com.shc.silenceengine.backend.lwjgl.LwjglRuntime;
import ${packageName}.${className};

public class ${className}Launcher
{
    public static void main(String[] args)
    {
        LwjglRuntime.start(new ${className}());
    }
}
