package com.eshakorps.tateti_firebase;

import android.widget.EditText;

import java.util.Map;

public class Commons {

    public boolean checkIfNotVoid( Map<EditText, String> items){
        boolean result = true;
        for(Map.Entry<EditText, String> entry : items.entrySet()) {
            EditText editText = entry.getKey();
            String value = entry.getValue();
            if(value.isEmpty()){
                editText.setError("Debe ingresar este dato");
                result=false;
            }
        }
        return result;
    }
}
