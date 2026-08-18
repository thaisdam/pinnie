package com.pinnie.controller;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api")
public class CsrfController {

    @GetMapping("/csrf")
    public ResponseEntity<Void> getCsrfToken() {
        // O CsrfCookieFilter já garante que o token seja gerado e injetado nos cookies.
        // O endpoint apenas retorna 200 OK.
        return ResponseEntity.ok().build();
    }
}
