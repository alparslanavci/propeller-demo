package com.hazelcast.propeller;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.client.RestTemplate;

import javax.servlet.http.HttpServletResponse;
import java.util.UUID;

@RestController
public class TransactionController {

    @Value("${NEXT_SERVICE:#{null}}")
    private String nextService;

    @Value("${SHOULD_FAIL:#{null}}")
    private Boolean shouldFail;

    @Value("${SLEEP_TIME_MS:#{null}}")
    private Integer sleepTimeMs;

    @RequestMapping("/action")
    public String action(@RequestHeader(value = "transactionId", required = false) String transactionId,
                                         HttpServletResponse response) throws InterruptedException {
        System.out.println();
        if (transactionId != null) {
            System.out.println("Execution action with transactionId '" + transactionId + "'...");
        }
        System.out.println("Execution action...");
        System.out.println();

        // sleep
        Integer sleepTimeTotalMs = sleepTimeMs;
        if (sleepTimeTotalMs == null) {
            sleepTimeTotalMs = 3000;
        }
        int sleepTimePart = sleepTimeTotalMs / 20;
        for (int i = 0; i < 20; i++) {
            System.out.println("|");
            Thread.sleep(sleepTimePart);
        }
        System.out.println("v");
        System.out.println("DONE");
        System.out.println();

        if (transactionId == null) {
            transactionId = generateTransactionId();
        }

        // call other service
        if (nextService != null) {
            System.out.println("Calling next service '" + nextService + "'...");
            System.out.println();

            RestTemplate restTemplate = new RestTemplate();
            HttpHeaders headers = new HttpHeaders();
            headers.set("transactionId", transactionId);
            HttpEntity entity = new HttpEntity(headers);
            new Thread(
                    () -> {
                        try {
                            restTemplate.exchange("http://" + nextService + ":8080/action", HttpMethod.GET, entity, String.class);
                        } catch (Exception e) {
                            // ignore
                        }
                    }
            ).start();
        }

        response.addHeader("transactionId", transactionId);
        // success or fail
        if (shouldFail != null && shouldFail != false) {
            throw new RuntimeException("Service execution failed!");
        } else {
            return "Action executed";
        }
    }

    private String generateTransactionId() {
        return UUID.randomUUID().toString();
    }

    @RequestMapping("/compensation")
    public String compensation() throws InterruptedException {
        System.out.println();
        System.out.println("Execution compensation action...");
        System.out.println();

        // sleep
        Integer sleepTimeTotalMs = sleepTimeMs;
        if (sleepTimeTotalMs == null) {
            sleepTimeTotalMs = 3000;
        }
        int sleepTimePart = sleepTimeTotalMs / 20;
        for (int i = 0; i < 20; i++) {
            System.out.println("|");
            Thread.sleep(sleepTimePart);
        }
        System.out.println("v");
        System.out.println("DONE");
        System.out.println();

        return "Compensation executed";
    }

}