package com.example.demo.service;

import com.example.demo.model.Quotation;
import jakarta.json.*;
import org.springframework.http.MediaType;
import org.springframework.http.RequestEntity;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClientException;
import org.springframework.web.client.RestTemplate;

import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.util.*;

@Service
public class QuotationService {

    private static final String QSYS_URL = "https://quotation.chuklee.com/quotation";

    public Optional<Quotation> getQuotations (List<String> items) {

        JsonArrayBuilder builder = Json.createArrayBuilder();
        items.stream().forEach(i->{
            builder.add(i);
        });

        JsonArray itemsArr = builder.build();

        RequestEntity<String> req = RequestEntity.post(QSYS_URL)
                .contentType(MediaType.APPLICATION_JSON)
                .header("Accept","application/json")
                .body(itemsArr.toString(),String.class);

        RestTemplate template = new RestTemplate();

        try {
            ResponseEntity<String> resp = template.exchange(req,String.class);
            System.out.println(">>>>QuotationService getQuotation: "+resp.getBody());
            if (resp.getStatusCodeValue() == 200) {
                Quotation quotation = createQuotation(resp.getBody());
                return Optional.of(quotation);
            }
            else {
                System.out.println("Resp status is not 200. Resp status is "+resp.getStatusCodeValue());
            }
        } catch (RestClientException e) {
            System.out.println(">>>> QuotationService - getQuotations: RestClientException"+e.getMessage());
        } catch (JsonException e) {
            System.out.println(">>>> QuotationService - getQuotations: JsonException"+e.getMessage());
        }

        return Optional.empty();
    }

    public static Quotation createQuotation(String json) {
        Quotation quotation = new Quotation();

        HashMap<String,Float> map = new HashMap<>();
        try {
            InputStream is = new ByteArrayInputStream(json.getBytes());
            JsonReader reader = Json.createReader(is);
            JsonObject object = reader.readObject();

            quotation.setQuoteId(object.getString("quoteId"));
            JsonArray quotations = object.getJsonArray("quotations");

            quotations.stream().map(v->(JsonObject) v)
                    .forEach(v -> {
                        map.put(v.getString("item"),
                                Float.valueOf(String.valueOf(v.getJsonNumber("unitPrice"))));
                    });

            quotation.setQuotations(map);

            System.out.println("QuotationHashmap---->"+Arrays.asList(map));


        } catch (Exception e) {
            System.out.println("----- Svc: createQuotation: unable to convert to JsonObject");
        }
        return quotation;
    }

    public double calculateTotalCost(List<String> itemNames, Map<String,Integer> lineItemMap, Quotation q ) {
        double totalCost = 0.00;

        Map<String,Float> qMap = q.getQuotations();

        for (String item : itemNames) {

            double cost = qMap.get(item)*lineItemMap.get(item);
            totalCost+=cost;

        }

        return totalCost;
    }
}
