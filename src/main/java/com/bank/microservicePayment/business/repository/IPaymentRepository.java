package com.bank.microservicePayment.business.repository;

import com.bank.microservicePayment.Model.entity.PaymentEntity;
import org.springframework.data.mongodb.repository.ReactiveMongoRepository;

public interface IPaymentRepository extends
        ReactiveMongoRepository<PaymentEntity, String> {

}
