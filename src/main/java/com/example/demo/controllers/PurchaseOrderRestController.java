package com.example.demo.controllers;

import com.example.demo.model.Quotation;
import com.example.demo.service.QuotationService;
import jakarta.json.Json;
import jakarta.json.JsonArray;
import jakarta.json.JsonObject;
import jakarta.json.JsonReader;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.*;

@RestController
@RequestMapping("/api/po")
public class PurchaseOrderRestController {

    @Autowired
    private QuotationService quotationSvc;

    @PostMapping(consumes = "application/json")
    public ResponseEntity<String> postPurchaseOrder(@RequestBody String payload) {

        System.out.println(payload);

        List<String> itemNames = new ArrayList<>();
        Map<String,Integer> lineItemMap = new HashMap<>();

        JsonArray lineItems = null;
        String name = null;
        try (InputStream is = new ByteArrayInputStream(payload.getBytes())){
            JsonReader reader = Json.createReader(is);
            JsonObject obj = reader.readObject();
            name = obj.getString("name");
            System.out.println(name);
            lineItems = obj.getJsonArray("lineItems");

            lineItems.stream().map(v->(JsonObject) v)
                    .forEach(v->{
                        String item = v.getString("item");
                        itemNames.add(item);
                        lineItemMap.put(item,v.getInt("quantity"));
                    });


            System.out.println("lineItemHashmap---->"+Arrays.asList(lineItemMap));

        } catch (IOException e) {
            System.out.println("Error creating List<String> items");
        }

        Optional<Quotation> opt = quotationSvc.getQuotations(itemNames);
        if(opt.isEmpty()) {
            return ResponseEntity.status(400).body(Json.createObjectBuilder().build().toString());
        }

        Quotation quotation = opt.get();

        double cost = quotationSvc.calculateTotalCost(itemNames,lineItemMap,quotation);
        System.out.println(">>> totalcost:"+cost);

        JsonObject totalCost = Json.createObjectBuilder()
                .add("invoiceId",quotation.getQuoteId())
                .add("name",name)
                .add("total",cost)
                .build();

        System.out.println("RespEntity >>>>"+totalCost.toString());

        return ResponseEntity.ok(totalCost.toString());
    }


}
