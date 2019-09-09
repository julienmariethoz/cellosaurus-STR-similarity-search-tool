package org.expasy.cellosaurus;

import org.expasy.cellosaurus.db.Database;
import org.expasy.cellosaurus.genomics.str.*;
import org.expasy.cellosaurus.math.scoring.Algorithm;
import org.expasy.cellosaurus.math.scoring.Mode;
import org.expasy.cellosaurus.math.scoring.ScoringAlgorithm;
import org.expasy.cellosaurus.math.scoring.ScoringMode;
import org.expasy.cellosaurus.wrappers.Parameters;
import org.expasy.cellosaurus.wrappers.Search;

import javax.ws.rs.core.MultivaluedMap;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * Class connecting to the backend and managing the STR similarity searches. Its main purposes are to validate the
 * provided parameters, perform the search and wrap the results into a custom object.
 */
public final class Manager {

    private Manager() {}

    /**
     * Perform the STR similarity search using the provided parameters or using default ones and returns the
     * resulting matches and search information.
     *
     * @param map a {@code MultivaluedMap} representing the parameter keys and values
     * @return a {@code Search} object encapsulating all the resulting matches and corresponding search metadata
     * @throws IllegalArgumentException if one of the parameter value is not supported
     */
    public static Search search(MultivaluedMap<String, String> map) {
        int algorithm = 0;
        int mode = 0;
        int scoreFilter = 60;
        int minMarkers = 8;
        int maxResults = 200;
        boolean includeAmelogenin = false;
        String description = "";

        Species species;
        if (map.containsKey("species")) {
            species = Species.get(map.getFirst("species"));
        } else if (map.containsKey("Species")) {
            species = Species.get(map.getFirst("Species"));
        } else if (map.containsKey("SPECIES")) {
            species = Species.get(map.getFirst("SPECIES"));
        } else {
            species = Species.HUMAN;
        }
        if (species == null) throw new IllegalArgumentException();

        Profile query = new Profile();
        for (String key : map.keySet()) {
            String name = formatKey(key);

            switch (name) {
                case "ALGORITHM":
                    algorithm = Integer.parseInt(map.getFirst(key))-1;
                    if (algorithm < 0 || algorithm > 2) {
                        throw new IllegalArgumentException(name + '=' + map.getFirst(key));
                    }
                    break;
                case "SCORINGMODE":
                    mode = Integer.parseInt(map.getFirst(key))-1;
                    if (mode < 0 || mode > 2) {
                        throw new IllegalArgumentException(name + '=' + map.getFirst(key));
                    }
                    break;
                case "SCOREFILTER":
                    scoreFilter = Integer.parseInt(map.getFirst(key));
                    break;
                case "MINMARKERS":
                    minMarkers = Integer.parseInt(map.getFirst(key));
                    break;
                case "MAXRESULTS":
                    maxResults = Integer.parseInt(map.getFirst(key));
                    break;
                case "INCLUDEAMELOGENIN":
                    includeAmelogenin = Boolean.parseBoolean(map.getFirst(key));
                    break;
                case "DESCRIPTION":
                    description = map.getFirst(key);
                    break;
                case "SPECIES":
                case "OUTPUTFORMAT":
                    break;
                default:
                    Marker marker = new Marker(name);
                    if (species.getDefaultMarkers().contains(marker) || species.getOptionalMarkers().contains(marker)) {
                        if (!map.getFirst(key).isEmpty()) {
                            for (String allele : map.getFirst(key).split(",")) {
                                marker.getAlleles().add(new Allele(allele.trim().toUpperCase()));
                            }
                        }
                        if (!query.getMarkers().contains(marker)) query.addMarker(marker);
                    }
                    break;
            }
        }
        Algorithm scoringAlgorithm = ScoringAlgorithm.get(algorithm);
        Mode scoringMode = ScoringMode.get(mode);

        List<CellLine> matches = new ArrayList<>();
        for (CellLine cellLine : species.getCellLines()) {
            // each reference cell line is copied as not to cause any ConcurrentModificationException
            CellLine copy = new CellLine(cellLine);

            for (Profile profile : copy.getProfiles()) {
                double score = scoringAlgorithm.computeScore(scoringMode, query, profile, includeAmelogenin);
                profile.setScore(score);
            }
            copy.reduceProfiles();

            int numMarkers = copy.getProfiles().get(0).getMarkerNumber();
            if (includeAmelogenin) numMarkers--;
            if (copy.getBestScore() >= scoreFilter && numMarkers >= minMarkers) {
                matches.add(copy);
            }
        }
        Collections.sort(matches);
        if (matches.size() >= maxResults) {
            matches = matches.subList(0, maxResults);
        }

        // the irrelevant fields of the query are set to null to be hidden in the JSON format
        for (Marker marker : query.getMarkers()) {
            marker.setConflicted(null);
            marker.setSearched(null);
            marker.setSources(null);

            for (Allele allele : marker.getAlleles()) {
                allele.setMatched(null);
            }
        }
        Parameters parameters = new Parameters(species.getName(), algorithm, mode, scoreFilter, minMarkers, maxResults,
                includeAmelogenin);
        Collections.sort(query.getMarkers());
        parameters.setMarkers(query.getMarkers());

        Search search = new Search(matches, description, Database.CELLOSAURUS.getVersion());
        search.setParameters(parameters);

        return search;
    }

    /**
     * Format the parameter keys to be whitespace and case insensitive and make sure that the STR markers are properly
     * named using common misspells.
     *
     * @param key the parameter key
     * @return the formatted parameter key
     */
    private static String formatKey(String key) {
        String name = key.trim().toUpperCase().replaceAll("\\s+", "_");

        switch (name) {
            case "AM":
            case "AMEL":
            case "AMELOGENIN":
                return "Amelogenin";
            case "CSF1P0":
                return "CSF1PO";
            case "F13A1":
                return "F13A01";
            case "FES/FPS":
                return "FESFPS";
            case "PENTA_C":
            case "PENTA_D":
            case "PENTA_E":
                return "Penta_" + name.charAt(name.length() - 1);
            case "THO1":
                return "TH01";
            case "VWA":
                return "vWA";
            default:
                if (name.startsWith("MOUSE_STR")) return name.substring(10);
                if (name.startsWith("MOUSE_")) return name.substring(6);
                if (name.startsWith("DOG_") || name.startsWith("STR_")) return name.substring(4);
                return name;
        }
    }
}
