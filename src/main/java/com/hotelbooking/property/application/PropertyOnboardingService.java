package com.hotelbooking.property.application;

import com.hotelbooking.common.domain.error.ResourceNotFoundException;
import com.hotelbooking.property.domain.OwnerId;
import com.hotelbooking.property.domain.Property;
import com.hotelbooking.property.domain.PropertyOnboardedEvent;
import com.hotelbooking.property.domain.PropertyOwner;
import com.hotelbooking.property.domain.PropertyOwnerRepository;
import com.hotelbooking.property.domain.PropertyRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@Transactional
public class PropertyOnboardingService {

    private static final Logger log = LoggerFactory.getLogger(PropertyOnboardingService.class);

    private final PropertyOwnerRepository owners;
    private final PropertyRepository properties;
    private final ApplicationEventPublisher events;

    public PropertyOnboardingService(PropertyOwnerRepository owners, PropertyRepository properties,
                                     ApplicationEventPublisher events) {
        this.owners = owners;
        this.properties = properties;
        this.events = events;
    }

    public OwnerPortfolio onboardOwner(OnboardOwnerCommand command) {
        PropertyOwner owner = owners.save(PropertyOwner.register(command.name(), command.contact()));
        log.info("Owner {} '{}' registered", owner.id(), owner.name());
        List<Property> onboarded = command.properties().stream()
                .map(draft -> onboard(owner.id(), draft))
                .toList();
        return new OwnerPortfolio(owner, onboarded);
    }

    public Property addProperty(OwnerId ownerId, PropertyDraft draft) {
        owners.findById(ownerId).orElseThrow(() -> new ResourceNotFoundException("Owner", ownerId));
        return onboard(ownerId, draft);
    }

    private Property onboard(OwnerId ownerId, PropertyDraft draft) {
        Property property = properties.save(draft.toProperty(ownerId));
        log.info("Property {} '{}' onboarded for owner {} in {} with {} room type(s)", property.id(), property.name(),
                ownerId, property.location().city(), property.roomTypes().size());
        // Listeners run synchronously in this transaction, so a failure rolls back the whole onboarding.
        events.publishEvent(new PropertyOnboardedEvent(property));
        return property;
    }
}
