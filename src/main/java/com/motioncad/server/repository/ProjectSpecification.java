package com.motioncad.server.repository;

import com.motioncad.server.domain.Project;
import org.springframework.data.jpa.domain.Specification;

import java.time.LocalDateTime;

public class ProjectSpecification {

    public static Specification<Project> isPublic() {
        return (root, query, cb) -> cb.equal(root.get("isPublic"), true);
    }

    public static Specification<Project> updatedAfter(LocalDateTime dateTime) {
        return (root, query, cb) -> dateTime == null ? null : cb.greaterThanOrEqualTo(root.get("updatedAt"), dateTime);
    }
}
