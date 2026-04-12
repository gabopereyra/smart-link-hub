package org.gabo.domain.port.out;

import dev.langchain4j.service.SystemMessage;
import dev.langchain4j.service.UserMessage;
import io.quarkiverse.langchain4j.RegisterAiService;

@RegisterAiService
public interface UrlAnalyzerService {

    @SystemMessage("""
        Sos un analizador de seguridad de URLs. 
        Tu única tarea es determinar si una URL es potencialmente maliciosa.
        Respondé SOLO con un JSON con este formato:
        {"safe": true/false, "reason": "explicación breve"}
        
        Ejemplos:
        URL: https://google.com → {"safe": true, "reason": "Dominio confiable y conocido"}
        URL: http://free-iphone-click-here.xyz → {"safe": false, "reason": "Dominio sospechoso con patrón de phishing"}
        """)
    String analyze(@UserMessage String url);
}

