package com.titan.thor.integration;

import com.titan.thor.integration.tesseract.TesseractTests;
import lombok.extern.java.Log;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@Log
@CrossOrigin
@RestController
@RequestMapping("maw")
public class Maw {

    private final TesseractTests tesseractTests;

    public Maw(TesseractTests tesseractTests) {
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


}
