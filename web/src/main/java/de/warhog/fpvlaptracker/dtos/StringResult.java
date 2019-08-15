package de.warhog.fpvlaptracker.dtos;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@JsonIgnoreProperties
public class StringResult {

    private static final Logger LOG = LoggerFactory.getLogger(StringResult.class);

    private String result;

    public static String OK = "OK";
    public static String NOK = "NOK";
    
    public StringResult() {
    }

    public StringResult(String result) {
        this.result = result;
    }
    
    public String getResult() {
        return result;
    }

    public void setResult(String result) {
        this.result = result;
    }
    
    public boolean isOK() {
        return OK.equals(result);
    }
    
    public boolean containsNOK() {
        return result.contains(NOK);
    }

    @Override
    public String toString() {
        return "Result{" + "result=" + result + '}';
    }

}
