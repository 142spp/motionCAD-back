package com.motioncad.server.repository;

import com.motioncad.server.domain.Part;
import com.motioncad.server.domain.PartCategory;
import com.motioncad.server.domain.PartType;
import org.springframework.data.jpa.domain.Specification;

import java.time.LocalDateTime;

public class PartSpecification {

    public static Specification<Part> hasType(PartType type) {
        return (root, query, cb) -> cb.equal(root.get("type"), type);
    }

    public static Specification<Part> isPublic() {
        return (root, query, cb) -> cb.equal(root.get("isPublic"), true);
    }

    public static Specification<Part> hasCategory(PartCategory category) {
        return (root, query, cb) -> category == null ? null : cb.equal(root.get("category"), category);
    }

    public static Specification<Part> isAiGenerated(Boolean isAiGenerated) {
        return (root, query, cb) -> isAiGenerated == null ? null : cb.equal(root.get("isAiGenerated"), isAiGenerated);
    }

    public static Specification<Part> updatedAfter(LocalDateTime dateTime) {
        return (root, query, cb) -> dateTime == null ? null : cb.greaterThanOrEqualTo(root.get("updatedAt"), dateTime);
    }
}
