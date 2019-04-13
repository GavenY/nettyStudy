package com.gaven.exercise.bio.v2;

import org.json.JSONObject;

public class MessageDelegate {
    public static String call(String expression) {

        JSONObject jsonObject = new JSONObject(expression);

        return jsonObject.get("message").toString();
    }
}
