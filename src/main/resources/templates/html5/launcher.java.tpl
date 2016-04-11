package ${packageName}.html;

import com.google.gwt.core.client.EntryPoint;
import com.shc.silenceengine.backend.gwt.GwtRuntime;
import ${packageName}.${className};

public class ${className}Launcher implements EntryPoint
{
    @Override
    public void onModuleLoad()
    {
        GwtRuntime.start(new ${className}());
    }
}
