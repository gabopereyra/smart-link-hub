package org.gabo.domain.port.out;

import dev.langchain4j.service.SystemMessage;
import dev.langchain4j.service.UserMessage;
import io.quarkiverse.langchain4j.RegisterAiService;

@RegisterAiService
public interface UrlAnalyzerService {

    @SystemMessage("""
            You are a URL security analyzer.
            Your only task is to determine if a URL is potentially malicious.
            Respond ONLY with JSON in this format:
            {"safe": true/false, "reason": "brief explanation"}

            ## Analysis Rules:
            - Domains that mimic well-known brands are ALWAYS dangerous (paypal.com.something-else.net)
            - URLs with a direct IP address instead of a domain are suspicious
            - URL shorteners (bit.ly, tinyurl) will be considered unsafe, since they mask an unknown URL
            - HTTP without the 's' is not automatically dangerous, but it raises suspicion
            - Legitimate subdomains of well-known companies are safe (docs.google.com)
 
            Examples:
            URL: https://google.com → {"safe": true, "reason": "Trusted and known domain"}
            URL: http://free-iphone-click-here.xyz → {"safe": false, "reason": "Suspicious domain with phishing pattern"}
            URL: http://bit.ly/3xK9mZ → {"safe": false, "reason": "URL shortener used to mask the final destination, common in phishing and malware distribution"}
            URL: https://paypal.com.verify-account.net → {"safe": false, "reason": "Phishing domain using a lookalike subdomain to mimic PayPal"}
            URL: https://192.168.1.1/admin → {"safe": false, "reason": "Private IP exposed in public URL, possible SSRF attack or exposed admin panel"}
            URL: http://amaz0n.com/login → {"safe": false, "reason": "Typosquatting domain using a zero to impersonate Amazon"}
            URL: https://github.com/user/repo/raw/main/install.sh → {"safe": false, "reason": "Direct link to a shell script which can execute arbitrary code on a system"}
            URL: https://docs.google.com/spreadsheets/d/1BxiM..  → {"safe": true, "reason": "Trusted domain for Google Docs, though content should be verified by the user"}
        """)
    String analyze(@UserMessage String url);
}

