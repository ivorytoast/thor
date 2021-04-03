package com.titan.thor.integration;

import com.titan.thor.Thor;
import com.titan.thor.integration.tesseract.TesseractTests;
import com.titan.thor.model.MawCancel;
import com.titan.thor.model.MawNew;
import com.titan.thor.model.MawOrderRequest;
import com.titan.thor.model.Order;
import lombok.extern.java.Log;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Log
@CrossOrigin
@RestController
@RequestMapping("maw")
public class Maw {

    private final TesseractTests tesseractTests;
    private final Thor thor;

    public Maw(Thor thor, TesseractTests tesseractTests) {
        this.thor = thor;
        this.tesseractTests = tesseractTests;
    }

    @GetMapping("/v1/run/tesseract/add")
    public String addTest() {
        return tesseractTests.test_Add();
    }

    @GetMapping("/v1/run/tesseract/quantity")
    public String quantityTest() {
        return tesseractTests.quantity_Test();
    }

    @PostMapping("/v1/submit/new/fix")
    public void newRequest(@RequestBody MawNew newRequest) {
        thor.mawNewFix(newRequest);
    }

    @CrossOrigin
    @PostMapping("/v1/submit/new/order")
    public void newOrderRequest(@RequestBody MawOrderRequest orderRequest) {
        thor.mawNewOrder(orderRequest);
    }

    @PostMapping("/v1/submit/cancel")
    public void cancelRequest(@RequestBody MawCancel cancelRequest) {
        thor.mawCancel(cancelRequest);
    }

    @GetMapping("/v1/orders/all")
    public List<Order> findAll() {
        return thor.getAllOrdersFromDatabase();
    }

}
