package de.warhog.fpvlaptracker.entities;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@JsonIgnoreProperties
public class Result {

    private static final Logger LOG = LoggerFactory.getLogger(Result.class);

    private String result;

    public Result() {
    }

    public Result(String result) {
        this.result = result;
    }
    
    public String getResult() {
        return result;
    }

    public void setResult(String result) {
        this.result = result;
    }
    
    public boolean isOK() {
        return "OK".equals(result);
    }

    @Override
    public String toString() {
        return "Result{" + "result=" + result + '}';
    }

}
