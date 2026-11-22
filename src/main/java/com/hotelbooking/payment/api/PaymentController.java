package com.hotelbooking.payment.api;

import com.hotelbooking.booking.domain.BookingId;
import com.hotelbooking.payment.api.dto.PaymentRequest;
import com.hotelbooking.payment.api.dto.PaymentResponse;
import com.hotelbooking.payment.application.PayForBookingCommand;
import com.hotelbooking.payment.application.PaymentResult;
import com.hotelbooking.payment.application.PaymentService;
import com.hotelbooking.payment.domain.IdempotencyKey;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/v1/bookings/{bookingId}/payments")
@Tag(name = "Payments")
class PaymentController {

    static final String IDEMPOTENCY_KEY = "Idempotency-Key";
    static final String REPLAYED = "Idempotent-Replayed";

    private final PaymentService payments;

    PaymentController(PaymentService payments) {
        this.payments = payments;
    }

    @PostMapping
    @Operation(summary = "Pay for a booking",
            description = """
                    Charges the booking amount with the given method. A successful charge confirms the booking; a
                    declined charge returns status FAILED and the booking stays payable until its hold expires.
                    Repeating a request with the same Idempotency-Key returns the original result (200, with
                    header Idempotent-Replayed: true) and never charges twice.""")
    ResponseEntity<PaymentResponse> pay(
            @PathVariable UUID bookingId,
            @Parameter(description = "Unique per payment attempt, e.g. a UUID") @RequestHeader(IDEMPOTENCY_KEY) String idempotencyKey,
            @Valid @RequestBody PaymentRequest request) {
        PaymentResult result = payments.pay(new PayForBookingCommand(
                new BookingId(bookingId), request.toInstrument(), new IdempotencyKey(idempotencyKey)));
        return ResponseEntity.status(result.replayed() ? HttpStatus.OK : HttpStatus.CREATED)
                .header(REPLAYED, String.valueOf(result.replayed()))
                .body(PaymentResponse.from(result.payment()));
    }

    @GetMapping
    @Operation(summary = "List payment attempts for a booking")
    List<PaymentResponse> list(@PathVariable UUID bookingId) {
        return payments.findForBooking(new BookingId(bookingId)).stream().map(PaymentResponse::from).toList();
    }
}
