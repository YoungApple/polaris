package com.codingstory.polaris.web.client;

import com.google.gwt.dom.client.Element;

public final class NativeHelper {
    private NativeHelper() {
    }

    public static native void click(Element element) /*-{
        element.click();
    }-*/;

    public static native String prettyPrint(String code) /*-{
        return $wnd.prettyPrintOne(code);
    }-*/;
}