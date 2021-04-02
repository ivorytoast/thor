package com.titan.thor.integration;

import com.titan.thor.Thor;
import com.titan.thor.database.Wanda;
import com.titan.thor.integration.tesseract.TesseractTests;
import com.titan.thor.model.MawOrder;
import lombok.extern.java.Log;
import org.springframework.web.bind.annotation.*;

@Log
@CrossOrigin
@RestController
@RequestMapping("maw")
public class Maw {

    private final TesseractTests tesseractTests;
    private Thor thor;

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

    @PostMapping("/v1/submit")
    public void submitRequest(@RequestBody MawOrder fixMessage) {
        thor.mawSubmit(fixMessage);
    }


}
