package com.bookhub.api.repository;

import com.bookhub.api.model.Request;
import com.bookhub.api.model.RequestStatus;
import com.bookhub.api.model.RequestType;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface RequestRepository extends MongoRepository<Request, String> {
    // Finds all requests, optionally filtering by type if provided
    Page<Request> findByTypeAndStatus(RequestType type, RequestStatus status, Pageable pageable);

    // Finds all requests of a certain status if type is not provided
    Page<Request> findByStatus(RequestStatus status, Pageable pageable);
}