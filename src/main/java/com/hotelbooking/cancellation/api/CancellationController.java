package com.hotelbooking.cancellation.api;

import com.hotelbooking.booking.domain.BookingId;
import com.hotelbooking.cancellation.application.CancellationService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.UUID;

@RestController
@Tag(name = "Cancellation")
class CancellationController {

    private final CancellationService cancellations;

    CancellationController(CancellationService cancellations) {
        this.cancellations = cancellations;
    }

    @PostMapping("/api/v1/bookings/{bookingId}/cancellation")
    @Operation(summary = "Cancel a booking",
            description = "Releases the rooms and refunds according to the cancellation policy agreed at booking time.")
    CancellationResponse cancel(@PathVariable UUID bookingId) {
        return CancellationResponse.from(cancellations.cancel(new BookingId(bookingId)));
    }
}
