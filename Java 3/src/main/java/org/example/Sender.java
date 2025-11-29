package org.example;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.Iterator;
import java.util.Queue;
import java.util.concurrent.Callable;

import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;

public class Sender implements Callable<String> {
    final Queue<String> queue;
    final String url;

    public Sender(Queue<String> queue, String url) {
        this.queue = queue;
        this.url = url;
    }

    @Override
    public String call() throws Exception {
        String message = "";
        try {
            URL surl = new URL("http://localhost:8080/" + url);
            System.out.println(surl);
            HttpURLConnection connection = (HttpURLConnection) surl.openConnection();

            connection.setRequestMethod("GET");
            connection.setRequestProperty("Accept", "application/json");

            if (connection.getResponseCode() == 200) {
                BufferedReader reader = new BufferedReader(new InputStreamReader(connection.getInputStream()));
                StringBuilder builder = new StringBuilder();

                String line;
                while((line = reader.readLine()) != null) {
                    builder.append(line);
                }

                JSONParser parser = new JSONParser();
                JSONObject json;
                try {
                    json = (JSONObject) parser.parse(builder.toString());
                } catch (ParseException e) {
                    System.out.println(e.getMessage());
                    throw new RuntimeException(e);
                }

                message = (String) json.get("message");
                JSONArray array = (JSONArray) json.get("successors");

                Iterator it = array.iterator();
                synchronized (queue){
                    while(it.hasNext()){
                        queue.add((String) it.next());
                    }
                }
            }



            connection.disconnect();

            return message;
        } catch (Exception e) {
            System.out.println(e.getMessage());
            throw new RuntimeException(e);
        }
    }
}
