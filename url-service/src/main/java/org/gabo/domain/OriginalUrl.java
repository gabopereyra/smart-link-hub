package org.gabo.domain;

public class OriginalUrl {
    private final String value;

    public OriginalUrl(String value){
        if(value == null || value.isBlank()){
            throw new IllegalArgumentException("URL cannot be empty");
        }
        this.value = value.startsWith("http") ? value : ("https://"+value);
    }

    public String getValue(){
        return this.value;
    }
}
