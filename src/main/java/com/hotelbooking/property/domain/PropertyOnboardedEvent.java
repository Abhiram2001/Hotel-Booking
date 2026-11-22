package com.hotelbooking.property.domain;

/**
 * Published when a property joins the platform. Other modules (inventory) react to it, so the property
 * module does not need to know about them.
 */
public record PropertyOnboardedEvent(Property property) {
}
