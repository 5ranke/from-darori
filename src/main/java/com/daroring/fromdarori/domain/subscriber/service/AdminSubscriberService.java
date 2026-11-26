package com.daroring.fromdarori.domain.subscriber.service;

import com.daroring.fromdarori.domain.story.exception.ApiException;
import com.daroring.fromdarori.domain.subscriber.dto.AdminSubscriberCreateRequest;
import com.daroring.fromdarori.domain.subscriber.dto.AdminSubscriberCreateResponse;
import com.daroring.fromdarori.domain.subscriber.entity.Subscriber;
import com.daroring.fromdarori.domain.subscriber.entity.SubscriberStatus;
import com.daroring.fromdarori.domain.subscriber.repository.SubscriberRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class AdminSubscriberService {
    private final SubscriberRepository subscriberRepository;

    @Transactional
    public AdminSubscriberCreateResponse create(AdminSubscriberCreateRequest request) {
        var optionalSubscriber = subscriberRepository.findByEmail(request.email());
        Subscriber subscriber;
        if (optionalSubscriber.isEmpty()) {
            // 신규 이메일
            subscriber = subscriberRepository.save(Subscriber.subscribeByAdmin(request.name(), request.email()));
        } else if (optionalSubscriber.get().getStatus() == SubscriberStatus.UNSUBSCRIBED) {
            // 구독 해지한 사람
            subscriber = optionalSubscriber.get();
            Subscriber.subscribeAgainByAdmin(request.name(), subscriber);
        } else {
            // 이미 ACTIVE 상태
            throw new ApiException(HttpStatus.CONFLICT, "SUBSCRIBER4091", "이미 구독 중인 이메일입니다.");
        }
        return AdminSubscriberCreateResponse.from(subscriber);
    }
}
