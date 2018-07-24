package com.doubleh.lumidiet.ble;


/**
 * kingabdulazizcenterdotcom
 * Author: Steve Kim
 * Description: This is a class file for Event Object
 * THIS FILE WAS DEVELOPED TO BE USED FOR THE KACWC IT PACKAGE SOLUTION.
 */
/**
 * This is a class for Event Object.
 * Just contain event id and data.
 */
public class MessageEvent {

    public final Constant.EVENT_TYPE event;
    public Object object;

    public MessageEvent(Constant.EVENT_TYPE value) {
        this.event = value;
        this.object = null;
    }

    public MessageEvent(Constant.EVENT_TYPE value, Object obj) {
        this.event = value;
        this.object = obj;
    }
}
