package com.petlifecycle.petlifecycle_backend;

import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.assertEquals;

public class FirstTrialTest {

    @Test
    public void testJUnitCalisiyorMu() {
        System.out.println("Test Ortamı Kontrol Ediliyor...");
        int sayi1 = 10;
        int sayi2 = 20;
        int sonuc = sayi1 + sayi2;
        assertEquals(30, sonuc, "Toplama işlemi hatalı, JUnit ortamında sorun var!");
    }
}
