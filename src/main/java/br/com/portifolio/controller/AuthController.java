package br.com.portifolio.controller;

import br.com.portifolio.config.JwtService;
import br.com.portifolio.dto.request.LoginRequest;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.AuthenticationException;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.LinkedHashMap;
import java.util.Map;

@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
@Tag(name = "Autenticação", description = "Validação de credenciais para a interface web")
public class AuthController {

    private final AuthenticationManager authenticationManager;
    private final JwtService jwtService;

    @PostMapping("/login")
    @Operation(summary = "Validar credenciais de acesso")
    public ResponseEntity<Map<String, Object>> login(@Valid @RequestBody LoginRequest request) {
        try {
            authenticationManager.authenticate(
                    new UsernamePasswordAuthenticationToken(request.getUsername(), request.getPassword())
            );

            String accessToken = jwtService.generateToken(request.getUsername());
            Map<String, Object> body = new LinkedHashMap<>();
            body.put("accessToken", accessToken);
            body.put("tokenType", "Bearer");
            body.put("expiresIn", jwtService.getExpirationSeconds());
            return ResponseEntity.ok(body);
        } catch (AuthenticationException ex) {
            return ResponseEntity.status(401)
                    .body(Map.of("message", "Usuário ou senha inválidos"));
        }
    }
}
