package org.expasy.cellosaurus.resources;

import com.google.gson.*;
import org.expasy.cellosaurus.Manager;
import org.expasy.cellosaurus.formats.FormatsUtils;
import org.expasy.cellosaurus.formats.xlsx.XlsxWriter;
import org.expasy.cellosaurus.formats.zip.ZipWriter;
import org.expasy.cellosaurus.wrappers.Search;
import org.glassfish.jersey.internal.util.ExceptionUtils;

import javax.ws.rs.Consumes;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MultivaluedHashMap;
import javax.ws.rs.core.MultivaluedMap;
import javax.ws.rs.core.Response;
import java.nio.file.Files;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * Class representing the batch API resource. Its  POST HTTP method is used to perform several STR similarity searches
 * and return all the results in the specified format.
 */
@Path("/batch")
public class BatchResource {

    /**
     * Process iteratively the submitted queries and return the STR similarity search results in the specified format.
     *
     * @param input the input query as a JSON {@code String}
     * @return the HTTP {@code Response}
     */
    @POST
    @Consumes("application/json")
    @Produces({"application/json", "application/zip", "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet"})
    public Response post(String input) {
        try {
            List<Search> searches = new ArrayList<>();
            String format = "JSON";

            JsonArray jsonArray = new JsonParser().parse(input).getAsJsonArray();
            for (int i = 0; i < jsonArray.size(); i++) {
                JsonObject object = jsonArray.get(i).getAsJsonObject();

                MultivaluedMap<String, String> map = new MultivaluedHashMap<>();
                for (Map.Entry<String, JsonElement> elements : object.entrySet()) {
                    map.add(elements.getKey().toUpperCase(), elements.getValue().getAsString());
                }
                if (!map.containsKey("DESCRIPTION")) map.add("DESCRIPTION", "Sample " + (i + 1));

                String jsonFormat = FormatsUtils.getFormat(object, null);
                if (jsonFormat != null) format = jsonFormat;

                searches.add(Manager.search(map));
            }
            switch (format) {
                case "JSON":
                    Gson gson = new Gson();

                    return Response
                            .status(200)
                            .entity(gson.toJson(searches))
                            .type("application/json")
                            .header("Content-Disposition", "inline")
                            .build();
                case "XLSX":
                    XlsxWriter xlsxWriter = new XlsxWriter();
                    byte[] xlsxBytes;
                    try {
                        for (Search search : searches) {
                            xlsxWriter.add(search);
                        }
                        xlsxWriter.write();
                        xlsxBytes = Files.readAllBytes(xlsxWriter.getXlsx().toPath());
                    } finally {
                        xlsxWriter.close();
                    }
                    return Response
                            .status(200)
                            .entity(xlsxBytes)
                            .type("application/vnd.openxmlformats-officedocument.spreadsheetml.sheet")
                            .header("Content-Disposition", "filename=Cellosaurus_STR_Results.xlsx")
                            .build();
                case "CSV":
                    ZipWriter zipWriter = new ZipWriter();
                    byte[] zipBytes;
                    try {
                        for (Search search : searches) {
                            zipWriter.add(search);
                        }
                        zipWriter.write();
                        zipBytes = Files.readAllBytes(zipWriter.getZip().toPath());
                    } finally {
                        zipWriter.close();
                    }
                    return Response
                            .status(200)
                            .entity(zipBytes)
                            .type("application/zip")
                            .header("Content-Disposition", "filename=Cellosaurus_STR_Results.zip")
                            .build();
                default:
                    throw new IllegalArgumentException("outputFormat=" + format);
            }
        } catch (IllegalArgumentException e) {
            return Response
                    .status(400)
                    .entity(ExceptionUtils.exceptionStackTraceAsString(e))
                    .type("text/plain")
                    .build();

        } catch (Exception e) {
            return Response
                    .status(500)
                    .entity(ExceptionUtils.exceptionStackTraceAsString(e))
                    .type("text/plain")
                    .build();
        }
    }
}
