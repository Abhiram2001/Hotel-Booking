package com.hotelbooking.property.application;

import com.hotelbooking.common.domain.error.ResourceNotFoundException;
import com.hotelbooking.property.domain.OwnerId;
import com.hotelbooking.property.domain.Property;
import com.hotelbooking.property.domain.PropertyId;
import com.hotelbooking.property.domain.PropertyOwner;
import com.hotelbooking.property.domain.PropertyOwnerRepository;
import com.hotelbooking.property.domain.PropertyRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@Transactional(readOnly = true)
public class PropertyQueryService {

    private final PropertyOwnerRepository owners;
    private final PropertyRepository properties;

    public PropertyQueryService(PropertyOwnerRepository owners, PropertyRepository properties) {
        this.owners = owners;
        this.properties = properties;
    }

    public Property getProperty(PropertyId id) {
        return properties.findById(id).orElseThrow(() -> new ResourceNotFoundException("Property", id));
    }

    public OwnerPortfolio getPortfolio(OwnerId ownerId) {
        PropertyOwner owner = owners.findById(ownerId).orElseThrow(() -> new ResourceNotFoundException("Owner", ownerId));
        return new OwnerPortfolio(owner, properties.findByOwner(ownerId));
    }

    public List<Property> findInCity(String city) {
        return properties.findByCity(city);
    }
}
