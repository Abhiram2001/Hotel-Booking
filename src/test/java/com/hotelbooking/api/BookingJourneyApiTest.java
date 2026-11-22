package com.hotelbooking.api;

import com.jayway.jsonpath.JsonPath;
import com.hotelbooking.support.IntegrationTestSupport;
import com.hotelbooking.support.TestData;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.ResultActions;
import org.springframework.test.web.servlet.request.MockHttpServletRequestBuilder;

import java.time.LocalDate;
import java.util.UUID;

import static org.hamcrest.Matchers.hasItem;
import static org.hamcrest.Matchers.startsWith;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.header;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/** Drives the public REST API end to end, the way a client would. */
class BookingJourneyApiTest extends IntegrationTestSupport {

    private static final String DECLINED_CARD = """
            {"method":"CARD","cardNumber":"4000000000000002","holderName":"Asha Rao","expiryMonth":12,"expiryYear":2031}""";
    private static final String WALLET = """
            {"method":"WALLET","provider":"PAYTM","walletId":"9812345678"}""";

    @Autowired
    MockMvc mvc;

    private String city;
    private LocalDate checkIn;
    private LocalDate checkOut;

    @BeforeEach
    void setUp() {
        city = TestData.uniqueCity();
        checkIn = clock.today().plusDays(10);
        checkOut = checkIn.plusDays(2);
    }

    @Test
    void guestDiscoversBooksPaysAndCancels() throws Exception {
        String owner = body(send(post("/api/v1/owners"), chainOwner())
                .andExpect(status().isCreated())
                .andExpect(header().string("Location", startsWith("/api/v1/owners/")))
                .andExpect(jsonPath("$.properties.length()").value(2)));
        String expressId = JsonPath.read(owner, "$.properties[1].propertyId");
        String standardRoomId = JsonPath.read(owner, "$.properties[1].roomTypes[0].roomTypeId");

        search().andExpect(jsonPath("$.count").value(2))
                .andExpect(jsonPath("$.results[0].name").value("Express"))
                .andExpect(jsonPath("$.results[0].offers[0].totalPrice.amount").value(5000.0));
        mvc.perform(searchRequest().param("amenities", "WIFI,SWIMMING_POOL"))
                .andExpect(jsonPath("$.count").value(1))
                .andExpect(jsonPath("$.results[0].name").value("Premier"));

        String booking = body(send(post("/api/v1/bookings"), bookingRequest(expressId, standardRoomId))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.status").value("PENDING_PAYMENT"))
                .andExpect(jsonPath("$.nights").value(2))
                .andExpect(jsonPath("$.totalAmount.amount").value(5000.0)));
        String bookingId = JsonPath.read(booking, "$.bookingId");

        send(post("/api/v1/bookings"), bookingRequest(expressId, standardRoomId))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.code").value("ROOMS_UNAVAILABLE"));
        search().andExpect(jsonPath("$.count").value(1));

        pay(bookingId, "key-declined-0001", DECLINED_CARD)
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.status").value("FAILED"));
        bookingStatus(bookingId, "PENDING_PAYMENT");

        String payment = body(pay(bookingId, "key-wallet-0001", WALLET)
                .andExpect(status().isCreated())
                .andExpect(header().string("Idempotent-Replayed", "false"))
                .andExpect(jsonPath("$.status").value("SUCCEEDED"))
                .andExpect(jsonPath("$.instrument").value("PAYTM ****5678")));
        pay(bookingId, "key-wallet-0001", WALLET)
                .andExpect(status().isOk())
                .andExpect(header().string("Idempotent-Replayed", "true"))
                .andExpect(jsonPath("$.paymentId").value(JsonPath.<String>read(payment, "$.paymentId")));
        bookingStatus(bookingId, "CONFIRMED");
        mvc.perform(get("/api/v1/bookings/{id}/payments", bookingId)).andExpect(jsonPath("$.length()").value(2));

        mvc.perform(post("/api/v1/bookings/{id}/cancellation", bookingId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.bookingStatus").value("CANCELLED"))
                .andExpect(jsonPath("$.refundPercentage").value(100))
                .andExpect(jsonPath("$.refundAmount.amount").value(5000.0))
                .andExpect(jsonPath("$.refundStatus").value("REFUNDED"));
        search().andExpect(jsonPath("$.count").value(2));

        mvc.perform(post("/api/v1/bookings/{id}/cancellation", bookingId))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.code").value("INVALID_STATE_TRANSITION"));
    }

    @Test
    void standaloneOwnerUsesTheSameFlowAndCanGrowIntoAChain() throws Exception {
        String owner = body(send(post("/api/v1/owners"), """
                {"name":"Sea Breeze","email":"stay@seabreeze.example","phone":"+918322123456","properties":[%s]}"""
                .formatted(property("Sea Breeze Homestay", 3, "NON_REFUNDABLE", "Garden Room", 3, 2, 3500)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.properties.length()").value(1)));
        String ownerId = JsonPath.read(owner, "$.ownerId");

        send(post("/api/v1/owners/{id}/properties", ownerId), property("Sea Breeze Annex", 2, "FLEXIBLE", "Twin", 2, 4, 2000))
                .andExpect(status().isCreated())
                .andExpect(header().string("Location", startsWith("/api/v1/properties/")))
                .andExpect(jsonPath("$.ownerId").value(ownerId));

        mvc.perform(get("/api/v1/owners/{id}", ownerId)).andExpect(jsonPath("$.properties.length()").value(2));
        search().andExpect(jsonPath("$.count").value(2));
        send(post("/api/v1/owners/{id}/properties", UUID.randomUUID()), property("Ghost", 2, "FLEXIBLE", "Twin", 2, 1, 2000))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.code").value("OWNER_NOT_FOUND"));
    }

    @Test
    void invalidRequestsGetProblemDetailsWithAStableCode() throws Exception {
        send(post("/api/v1/bookings"), "{\"guests\":0}")
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.code").value("VALIDATION_FAILED"))
                .andExpect(jsonPath("$.errors[*].field", hasItem("guests")));
        send(post("/api/v1/bookings"), bookingRequest(UUID.randomUUID().toString(), UUID.randomUUID().toString())
                .replace(checkOut.toString(), checkIn.toString()))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.detail").value("checkOut must be after checkIn"));
        send(post("/api/v1/owners"), """
                {"name":"Dup","email":"dup@example.com","phone":"+919876543210","properties":[{"name":"Dup Hotel",
                 "addressLine":"1 Road","locality":"Central","city":"%s","starRating":3,"cancellationPolicy":"FLEXIBLE",
                 "roomTypes":[{"name":"Twin","maxOccupancy":2,"totalRooms":1,"basePricePerNight":1000},
                              {"name":"twin","maxOccupancy":2,"totalRooms":1,"basePricePerNight":1000}]}]}"""
                .formatted(city))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.code").value("DUPLICATE_ROOM_TYPE"));
        mvc.perform(get("/api/v1/properties/search").param("checkIn", checkIn.toString()))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.code").value("VALIDATION_FAILED"));
        mvc.perform(get("/api/v1/bookings/{id}", UUID.randomUUID()))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.code").value("BOOKING_NOT_FOUND"));
        send(post("/api/v1/bookings/{id}/payments", UUID.randomUUID()), WALLET)
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.code").value("BAD_REQUEST"));
        pay(UUID.randomUUID().toString(), "key-unknown-0001", "{\"method\":\"BITCOIN\"}")
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.code").value("BAD_REQUEST"));
    }

    private ResultActions search() throws Exception {
        return mvc.perform(searchRequest()).andExpect(status().isOk());
    }

    private MockHttpServletRequestBuilder searchRequest() {
        return get("/api/v1/properties/search")
                .param("city", city)
                .param("checkIn", checkIn.toString())
                .param("checkOut", checkOut.toString())
                .param("guests", "2");
    }

    private ResultActions pay(String bookingId, String idempotencyKey, String body) throws Exception {
        return send(post("/api/v1/bookings/{id}/payments", bookingId).header("Idempotency-Key", idempotencyKey), body);
    }

    private void bookingStatus(String bookingId, String expected) throws Exception {
        mvc.perform(get("/api/v1/bookings/{id}", bookingId)).andExpect(jsonPath("$.status").value(expected));
    }

    private ResultActions send(MockHttpServletRequestBuilder request, String json) throws Exception {
        return mvc.perform(request.contentType(MediaType.APPLICATION_JSON).content(json));
    }

    private static String body(ResultActions actions) throws Exception {
        return actions.andReturn().getResponse().getContentAsString();
    }

    private String chainOwner() {
        return """
                {"name":"Lemon Leaf Hotels","email":"ops@lemonleaf.example","phone":"+918041234567","properties":[%s,%s]}"""
                .formatted(
                        property("Premier", 4, "MODERATE", "Deluxe", 2, 3, 5000).replace("[\"WIFI\"]", "[\"WIFI\",\"SWIMMING_POOL\"]"),
                        property("Express", 3, "FLEXIBLE", "Standard", 2, 1, 2500));
    }

    private String property(String name, int stars, String policy, String roomType, int maxOccupancy, int rooms,
                            int price) {
        return """
                {"name":"%s","addressLine":"1 Main Road","locality":"Central","city":"%s","starRating":%d,
                 "amenities":["WIFI"],"cancellationPolicy":"%s",
                 "roomTypes":[{"name":"%s","maxOccupancy":%d,"totalRooms":%d,"basePricePerNight":%d}]}"""
                .formatted(name, city, stars, policy, roomType, maxOccupancy, rooms, price);
    }

    private String bookingRequest(String propertyId, String roomTypeId) {
        return """
                {"propertyId":"%s","roomTypeId":"%s","checkIn":"%s","checkOut":"%s","guests":2,"rooms":1,
                 "guest":{"fullName":"Asha Rao","email":"asha@example.com","phone":"+919812345678"}}"""
                .formatted(propertyId, roomTypeId, checkIn, checkOut);
    }
}
