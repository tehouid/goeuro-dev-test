package com.github.tehouid.goeuro;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.dataformat.csv.CsvMapper;
import com.fasterxml.jackson.dataformat.csv.CsvSchema;

import java.io.BufferedWriter;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.nio.file.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Scanner;

/**
 * The class consists of the static methods to query the API provided by
 * <a href="http://api.goeuro.com/api/v2/position/suggest/en/CITY_NAME">
 * http://api.goeuro.com/api/v2/position/suggest/en/CITY_NAME</a>,
 * and to create a CSV file from JSON array returned by the API. <i>CITY_NAME</i>
 * is the parameter supplied by the user.
 *<p>
 * Each object returned by the API has a name and a geo_position key.
 * The geo_position key is an object with latitude and longitude fields.
 *<p>
 * The CSV file has the form {_id, name, type, latitude, longitude}.
 * The CSV file has the name locations.csv and is written to the current
 * directory, i.e., the directory from which the class or jar is run.
 *<p>
 * The class relies on <a href="https://github.com/FasterXML/jackson-databind">
 * Jackson</a> library for reading and parsing JSON, and its extension for
 * writing CSV encoded data
 */
public class LocationJSONReader {
    private static final String endpointBaseUrl = "http://api.goeuro.com/api/v2/position/suggest/en/";
    private static final String csvFilePath = "./locations.csv";

    /**
     * Retrieves the array of JSON documents provided by the API. Creates and
     * returns a list of <code>Location</code> objects containing fields of
     * interest from JSON documents
     *
     * @param   url the url of the API to retrieve JSON documents from
     * @return  the <code>List</code> of <code>Location</code> objects if API
     *          returned non-empty array, an empty <code>List</code> otherwise
     * @throws  JsonProcessingException
     * @throws  IOException if a low-level I/O problem (unexpected end-of-input,
     *          network error) occurs
     * @throws  MalformedURLException if API URL is not a valid one
     * @see     Location
     */
    private static List<Location> readJsonFromUrl(String url) throws IOException {
        ObjectMapper mapper = new ObjectMapper();
        JsonNode locationArray = mapper.readTree(new URL(url));
        List<Location> locations = new ArrayList<>();

        for (int i = 0; i < locationArray.size(); i++) {
            locations.add(
                    new Location.Builder(locationArray.get(i).get("_id").asInt()).
                            name(locationArray.get(i).get("name").asText()).
                            type(locationArray.get(i).get("type").asText()).
                            latitude(locationArray.get(i).get("geo_position").get("latitude").asDouble()).
                            longitude(locationArray.get(i).get("geo_position").get("longitude").asDouble()).
                            build()
            );
        }

        return locations;
    }

    /**
     * Writes the <code>List</code> of <code>Location</code> objects to the CSV
     * file <i>locations.csv</i> in form {_id, name, type, latitude, longitude}.
     * If the file already exists, user is prompted to overwrite it by entering "o",
     * or to append the data to the end of the file by entering "a". Otherwise the execution
     * of the program is terminated and <code>FileAlreadyExistsException</code> is thrown.
     *
     * @param   locations the <code>List</code> of <code>Location</code> objects
     *          to be written to the CSV file
     * @param   path the path of the CSV file <i>location.csv</i>
     * @throws  FileAlreadyExistsException if CSV file already exists and the user
     *          hasn't chosen to owerwrite the file or append it
     * @throws  IOException if a low-level I/O problem occurs
     * @throws  JsonProcessingException
     */
    private static void writeJsonToCsv(List<Location> locations, String path) throws IOException {
        Path csvFile = Paths.get(path);
        CsvMapper csvMapper = new CsvMapper();
        csvMapper.disable(SerializationFeature.FAIL_ON_EMPTY_BEANS);
        CsvSchema schema = csvMapper.schemaFor(Location.class);

        if (!Files.exists(csvFile)) {
            try (BufferedWriter fileWriter = Files.newBufferedWriter(csvFile, StandardCharsets.UTF_8,
                    StandardOpenOption.WRITE, StandardOpenOption.CREATE_NEW)) {
                for (Location l : locations) {
                    fileWriter.write(csvMapper.writer(schema).writeValueAsString(l));
                }
            }
        } else {
            Scanner reader = new Scanner(System.in);
            System.out.println("The file " + path + " already exists. \nIf you want to overwrite it, enter \"O\", "
                    + "if you want to append the data to the end of the file, enter \"A\""
            + "\nIf you want to terminate the execution of the program, enter any other letter, or press CTRL+C");
            String choice = reader.next();
            if (choice.equalsIgnoreCase("o")) {
                try (BufferedWriter fileWriter = Files.newBufferedWriter(csvFile, StandardCharsets.UTF_8,
                        StandardOpenOption.WRITE, StandardOpenOption.TRUNCATE_EXISTING)) {
                    for (Location l : locations) {
                        fileWriter.write(csvMapper.writer(schema).writeValueAsString(l));
                    }
                }
            } else if (choice.equalsIgnoreCase("a")) {
                try (BufferedWriter fileWriter = Files.newBufferedWriter(csvFile, StandardCharsets.UTF_8,
                        StandardOpenOption.WRITE, StandardOpenOption.APPEND)) {
                    for (Location l : locations) {
                        fileWriter.write(csvMapper.writer(schema).writeValueAsString(l));
                    }
                }
            } else {
                throw new FileAlreadyExistsException("File " + path + " already exists");
            }
        }
    }

    /**
     * The main method which makes the use of <code>readJsonFromUrl</code>
     * and <code>writeJsonToCsv</code> methods
     *
     * @param   args the name of the location the API is queried with, i.e., CITY_NAME
     */
    public static void main(String[] args) {
        if (args.length == 0 || args.length > 1) {
            System.out.println("You need to supply the name of the location in the form \"LOCATION_NAME\", e.g. \"BERLIN\"");
            return;
        }

        String cityName = args[0];
        String url = endpointBaseUrl.concat(cityName);

        try {
            writeJsonToCsv(readJsonFromUrl(url),csvFilePath);
        } catch (MalformedURLException e) {
            System.err.println("Caught MalformedURLException while trying to access API: " + e.getMessage());
        } catch (JsonProcessingException e) {
            System.err.println("Caught JsonProcessingException while either reading JSON from the API or writing JSON" +
                    " according to CSV schema: " + e.getMessage());
        } catch (FileAlreadyExistsException e) {
            System.err.println("Caught FileAlreadyExistsException because the file already exists and user chose " +
                    "neither to owerwrite it, nor to append the data to it: "+ e.getMessage());
        } catch (IOException e) {
            System.err.println("Caught IOException");
            e.printStackTrace();
        }
    }
}
