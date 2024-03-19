package com.github;

import java.io.BufferedReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.HashSet;
import java.util.Set;
import java.util.concurrent.TimeUnit;
import java.util.zip.GZIPInputStream;

import org.json.JSONObject;

public class GitHubQueryFetch {

    private static final String GITHUB_ARCHIVE_URL = "https://data.gharchive.org/";
    private static final String GITHUB_REPO_URL = "https://api.github.com/repos/%s/%s";
    private static final String OUTPUT_FILE_PATH = "github_data.csv";

    public static void main(String[] args) {
        LocalDate startDate = LocalDate.parse("2023-01-01");
        LocalDate endDate = LocalDate.parse("2023-01-31");

        try (FileWriter writer = new FileWriter(OUTPUT_FILE_PATH)) {
            for (LocalDate date = startDate; date.isBefore(endDate); date = date.plusDays(1)) {
                System.out.println("Processing date: " + date.toString());
                Set<EventObject> eventObjects = downloadAndExtractRepositories(date);
                appendRepositoryMetadata(eventObjects, writer);
                TimeUnit.SECONDS.sleep(3); // Adding a few seconds break between requests
                System.out.println("Objects written to CSV successfully.");
            }
        } catch (IOException | InterruptedException e) {
            e.printStackTrace();
        }
    }

    private static Set<EventObject> downloadAndExtractRepositories(LocalDate date) throws IOException {
        Set<EventObject> event = new HashSet<>();
        String[] hours = {"0", "1", "2", "3", "4", "5", "6", "7", "8", "9", "10", "11",
                          "12", "13", "14", "15", "16", "17", "18", "19", "20", "21", "22", "23"};
        
        for (String hour : hours) {
            String url = GITHUB_ARCHIVE_URL+ date + "-" + hour + ".json.gz";
            String fileName = date.toString()+"-" + hour + ".json.gz";
            
            try {
                URL downloadUrl = new URL(url);
                HttpURLConnection connection = (HttpURLConnection) downloadUrl.openConnection();
                connection.setRequestMethod("GET");
                
                int responseCode = connection.getResponseCode();
                if (responseCode == HttpURLConnection.HTTP_OK) {
                	try (GZIPInputStream gzipInputStream = new GZIPInputStream(connection.getInputStream());
                            InputStreamReader inputStreamReader = new InputStreamReader(gzipInputStream, StandardCharsets.UTF_8);
                            BufferedReader reader = new BufferedReader(inputStreamReader)) {

                           JSONObject jsonObject = new JSONObject(reader.readLine());
                           
                           JSONObject repo = jsonObject.getJSONObject("repo");
                           
                           Repository repository = new Repository().getInstance(repo.getInt("id"), repo.getString("name"), repo.getString("url"));
                           EventObject eventObject = new EventObject().getInstance(jsonObject.getString("id"), jsonObject.getString("type"), jsonObject.getString("created_at"), repository);
                           event.add(eventObject);
                       }
                    System.out.println("Downloaded: " + fileName);
                } else {
                    System.out.println("Failed to download: " + fileName + ", HTTP error code: " + responseCode);
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        return event;
    }

    private static void appendRepositoryMetadata(Set<EventObject> eventObjects, FileWriter writer) throws IOException {
        for(EventObject eventObject: eventObjects) {
        	String[] parts = eventObject.getRepository().getUrl().split("/");
            if (parts.length >= 3) {
                String owner = parts[parts.length - 2];
                String repo = parts[parts.length - 1];
                boolean isCreatedIn2023 = fetchRepositoryMetadata(owner, repo);
                System.out.println("isCreatedIn2023:"+isCreatedIn2023);
                if(isCreatedIn2023) {
                        StringBuilder sb = new StringBuilder();
                        String row[] = eventObject.toStringArray();
                        for (int i = 0; i < row.length; i++) {
                            sb.append(row[i]);
                            if (i < row.length - 1) {
                                sb.append(",");
                            }
                        }
                        sb.append(System.lineSeparator());
                        writer.write(sb.toString());
                }
                
            }
        }
    }

    private static boolean fetchRepositoryMetadata(String owner, String repo) throws IOException {
        String urlString = String.format(GITHUB_REPO_URL, owner, repo);
        URL url = new URL(urlString);
        HttpURLConnection connection = (HttpURLConnection) url.openConnection();
        connection.setRequestMethod("GET");
        System.out.println(urlString+"response code:"+connection.getResponseCode());
        if (connection.getResponseCode() == HttpURLConnection.HTTP_OK) {
            try (BufferedReader reader = new BufferedReader(new InputStreamReader(connection.getInputStream()))) {
                JSONObject repoResponse = new JSONObject(reader.readLine());
                String createdAt = repoResponse.getString("created_at");
               // String updatedAt = repoResponse.getString("updated_at");
                
                LocalDateTime dateToCheck = LocalDateTime.parse(createdAt, DateTimeFormatter.ISO_DATE_TIME);
                LocalDateTime startDate = LocalDateTime.of(2023, 1, 1, 0, 0);
                LocalDateTime endDate = LocalDateTime.of(2023, 12, 31, 23, 59, 59);
                System.out.println("dateToCheck:"+dateToCheck.toString());
                if (dateToCheck.isAfter(startDate) && dateToCheck.isBefore(endDate)) {
                    return true;
                } else {
                	return false;
                }
            }
        }
        return false;
    }
}
