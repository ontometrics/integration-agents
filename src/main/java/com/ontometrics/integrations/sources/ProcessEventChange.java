package com.ontometrics.integrations.sources;

/**
 * Created by rob on 7/23/14.
 * Copyright (c) ontometrics, 2014 All Rights Reserved
 */
public class ProcessEventChange {

    private final String field;
    private final String priorValue;
    private final String currentValue;

    public ProcessEventChange(Builder builder) {
        field = builder.field;
        priorValue = builder.priorValue;
        currentValue = builder.currentValue;
    }

    public static class Builder {

        private String field;
        private String priorValue;
        private String currentValue;

        public Builder field(String field){
            this.field = field;
            return this;
            }

        public Builder priorValue(String priorValue){
            this.priorValue = priorValue;
            return this;
            }

        public Builder currentValue(String currentVale){
            this.currentValue = currentVale;
            return this;
            }

        public ProcessEventChange build(){
            return new ProcessEventChange(this);
            }
    }

    public String getField() {
        return field;
    }

    public String getPriorValue() {
        return priorValue;
    }

    public String getCurrentValue() {
        return currentValue;
    }

    @Override
    public String toString() {
        return String.format("%s changed from : %s to %s", field, priorValue, currentValue);
    }
}
