package com.myapps.documentscanner.helpers;

/**
 * Created by ANIRUDDHA.
 */
public class DocumentMessage  {

    private String command;
    private Object obj;

    public DocumentMessage( String command , Object obj ) {
        setObj(obj);
        setCommand(command);
    }


    public String getCommand() {
        return command;
    }

    public void setCommand(String command) {
        this.command = command;
    }

    public Object getObj() {
        return obj;
    }

    public void setObj(Object obj) {
        this.obj = obj;
    }
}
