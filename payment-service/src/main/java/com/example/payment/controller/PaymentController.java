package com.example.payment.controller;

import java.util.concurrent.atomic.AtomicInteger;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/payments")
public class PaymentController {

    /*
     * Counter used only for the Day 4 retry exercise.
     *
     * First request  -> fails
     * Second request -> succeeds
     */
    private final AtomicInteger retryAttempt =
            new AtomicInteger(0);


    /*
     * ==========================================
     * HEALTH CHECK
     * ==========================================
     *
     * GET /api/payments/health
     */

    @GetMapping("/health")
    public ResponseEntity<String> health() {

        return ResponseEntity.ok(
                "Payment Service is UP"
        );
    }


    /*
     * ==========================================
     * NORMAL PAYMENT
     * ==========================================
     *
     * GET /api/payments/1001
     */

    @GetMapping("/{paymentId}")
    public ResponseEntity<String> getPayment(
            @PathVariable Long paymentId) {

        return ResponseEntity.ok(
                "Payment "
                + paymentId
                + " processed successfully"
        );
    }


    /*
     * ==========================================
     * SLOW PAYMENT
     * ==========================================
     *
     * Used for the Day 4 timeout exercise.
     *
     * Example:
     *
     * GET /api/payments/slow/10
     *
     * Payment Service waits 10 seconds.
     */

    @GetMapping("/slow/{seconds}")
    public ResponseEntity<String> slowPayment(
            @PathVariable int seconds)
            throws InterruptedException {

        if (seconds < 0) {

            return ResponseEntity
                    .badRequest()
                    .body(
                            "Seconds cannot be negative"
                    );
        }

        System.out.println(
                "Payment Service: "
                + "delaying for "
                + seconds
                + " seconds"
        );

        Thread.sleep(seconds * 1000L);

        return ResponseEntity.ok(
                "Slow payment completed after "
                + seconds
                + " seconds"
        );
    }


    /*
     * ==========================================
     * CONTROLLED RETRY TEST
     * ==========================================
     *
     * First attempt:
     *      HTTP 500
     *
     * Second attempt:
     *      HTTP 200
     *
     * This allows Order Service Retry
     * to demonstrate recovery.
     *
     * GET /api/payments/retry-test
     */

    @GetMapping("/retry-test")
    public ResponseEntity<String> retryTest() {

        int attempt =
                retryAttempt.incrementAndGet();

        System.out.println(
                "Payment Service retry-test attempt: "
                + attempt
        );


        /*
         * First attempt deliberately fails.
         */

        if (attempt == 1) {

            return ResponseEntity
                    .internalServerError()
                    .body(
                            "Temporary Payment failure - "
                            + "attempt "
                            + attempt
                    );
        }


        /*
         * Second and subsequent attempts succeed.
         */

        return ResponseEntity.ok(
                "Payment recovered on attempt "
                + attempt
        );
    }


    /*
     * ==========================================
     * RESET RETRY TEST
     * ==========================================
     *
     * Resets the counter back to zero.
     *
     * GET /api/payments/retry-test/reset
     */

    @GetMapping("/retry-test/reset")
    public ResponseEntity<String> resetRetryTest() {

        retryAttempt.set(0);

        System.out.println(
                "Payment retry test counter reset"
        );

        return ResponseEntity.ok(
                "Retry test counter reset"
        );
    }


    /*
     * ==========================================
     * ALWAYS FAILING PAYMENT
     * ==========================================
     *
     * Used to test what happens when every
     * retry attempt fails.
     *
     * GET /api/payments/fail
     */

    @GetMapping("/fail")
    public ResponseEntity<String> failPayment() {

        System.out.println(
                "Payment Service: deliberate failure"
        );

        return ResponseEntity
                .internalServerError()
                .body(
                        "Payment Service deliberately failed"
                );
    }
}