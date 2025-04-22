package com.learning.journalApp.api.response;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class WeatherResponse {
    private Current current;


    @Getter
    @Setter
    public class Current {
        @JsonProperty("temperature")
        private int temperature;
        @JsonProperty("feelslike")
        private int feelsLike;
    }
}








