package org.expasy.cellosaurus.formats;

import com.google.gson.JsonElement;
import org.expasy.cellosaurus.wrappers.Search;

import javax.ws.rs.core.MultivaluedMap;
import java.util.Map;

public class FormatsUtils {

    public static String metadata(Search search) {
        return "#" +
                "Description: '" +
                search.getDescription() +
                "';Data set: 'Cellosaurus release " +
                search.getCellosaurusRelease() +
                "';Run on: '" +
                search.getRunOn() +
                "';Tool version: '" +
                search.getToolVersion() +
                "';Algorithm: '" +
                search.getParameters().getAlgorithm() +
                "';Scoring mode: '" +
                search.getParameters().getScoringMode() +
                "';Score filter: '" +
                search.getParameters().getScoreFilter() +
                "';Max results: '" +
                search.getParameters().getMaxResults() +
                "';Include Amelogenin: '" +
                search.getParameters().isIncludeAmelogenin() +
                "'";
    }

    public static String getOutputFormat(MultivaluedMap<String, String> map) {
        String format = "";

        for (String key: map.keySet()) {
            if (key.equalsIgnoreCase("outputformat")) {
                if (map.getFirst(key).equalsIgnoreCase("json")) {
                    format = "JSON";
                } else if (map.getFirst(key).equalsIgnoreCase("csv")) {
                    format = "CSV";
                } else if (map.getFirst(key).equalsIgnoreCase("xlsx")) {
                    format = "XLSX";
                } else {
                    throw new IllegalArgumentException(map.getFirst(key));
                }
                break;
            }
        }
        return format;
    }

    public static String getOutputFormat(Map.Entry<String, JsonElement> elements) {
        String format = "";

        if (elements.getKey().equalsIgnoreCase("outputformat")) {
            if (elements.getValue().getAsString().equalsIgnoreCase("json")) {
                format = "JSON";
            } else if (elements.getValue().getAsString().equalsIgnoreCase("csv")) {
                format = "CSV";
            } else if (elements.getValue().getAsString().equalsIgnoreCase("xlsx")) {
                format = "XLSX";
            } else {
                throw new IllegalArgumentException(elements.getValue().getAsString());
            }
        }
        return format;
    }
}