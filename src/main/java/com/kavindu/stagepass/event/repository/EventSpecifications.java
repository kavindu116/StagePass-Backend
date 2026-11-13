package com.kavindu.stagepass.event.repository;

import com.kavindu.stagepass.entity.Enums.EventCategory;
import com.kavindu.stagepass.entity.Enums.EventStatus;
import com.kavindu.stagepass.entity.Event;
import com.kavindu.stagepass.entity.Tier;
import com.kavindu.stagepass.entity.Venue;
import com.kavindu.stagepass.event.dto.EventFilter;
import jakarta.persistence.criteria.Join;
import jakarta.persistence.criteria.Root;
import jakarta.persistence.criteria.Subquery;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.util.StringUtils;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

public final class EventSpecifications {

    private EventSpecifications() {
    }

    public static Specification<Event> withFilters(EventFilter f) {
        List<Specification<Event>> specs = new ArrayList<>();

        specs.add(isPublished());

        if (f.category() != null)          specs.add(hasCategory(f.category()));
        if (StringUtils.hasText(f.city())) specs.add(inCity(f.city().trim()));
        if (StringUtils.hasText(f.q()))    specs.add(titleContains(f.q().trim()));
        if (f.maxPrice() != null)          specs.add(priceFromAtMost(f.maxPrice()));
        return Specification.allOf(specs);


    }

    static Specification<Event> isPublished(){
        return (root, query, criteriaBuilder) -> criteriaBuilder.equal(root.get("status"), EventStatus.PUBLISHED);
    }

    static Specification<Event> hasCategory(EventCategory category){
        return (root, query, criteriaBuilder) -> criteriaBuilder.equal(root.get("category"), category);
    }

    static Specification<Event> inCity(String city) {
        return (root, query, cb) -> {
            Join<Event, Venue> venue = root.join("venue");
            return cb.equal(cb.lower(venue.get("city")), city.toLowerCase());
        };
    }

    static Specification<Event> titleContains(String q) {
        String pattern = "%" + escapeLike(q.toLowerCase()) + "%";
        return (root, query, cb) -> cb.like(cb.lower(root.get("title")), pattern, '\\');
    }

    private static String escapeLike(String s) {
        return s.replace("\\", "\\\\").replace("%", "\\%").replace("_", "\\_");
    }

    static Specification<Event> priceFromAtMost(BigDecimal max) {
        return (root, query, cb) -> {
            Subquery<BigDecimal> minPrice = query.subquery(BigDecimal.class);
            Root<Tier> tier = minPrice.from(Tier.class);
            minPrice.select(cb.min(tier.<BigDecimal>get("price")))
                    .where(cb.equal(tier.get("event"), root));
            return cb.lessThanOrEqualTo(minPrice, max);
        };
    }
}
