package com.daroring.fromdarori.domain.subscriber.service;

import com.daroring.fromdarori.domain.story.exception.ApiException;
import com.daroring.fromdarori.domain.subscriber.dto.SubscribeCreateRequest;
import com.daroring.fromdarori.domain.subscriber.dto.SubscribeCreateResponse;
import com.daroring.fromdarori.domain.subscriber.dto.UnsubscribeRequest;
import com.daroring.fromdarori.domain.subscriber.dto.UnsubscribeResponse;
import com.daroring.fromdarori.domain.subscriber.entity.Subscriber;
import com.daroring.fromdarori.domain.subscriber.entity.SubscriberStatus;
import com.daroring.fromdarori.domain.subscriber.entity.Unsubscriber;
import com.daroring.fromdarori.domain.subscriber.repository.SubscriberRepository;
import com.daroring.fromdarori.domain.subscriber.repository.UnsubscriberRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class SubscriptionService {
    private final SubscriberRepository subscriberRepository;
    private final UnsubscriberRepository unsubscriberRepository;

    @Transactional
    public SubscribeCreateResponse subscribe(SubscribeCreateRequest request) {
        var optionalSubscriber = subscriberRepository.findByEmail(request.email());
        Subscriber subscriber;
        if (optionalSubscriber.isPresent() && optionalSubscriber.get().getStatus() == SubscriberStatus.UNSUBSCRIBED) {
            // 다시 구독하기
            subscriber = optionalSubscriber.get();
            Subscriber.subscribeAgain(subscriber);
        } else if (optionalSubscriber.isPresent() && optionalSubscriber.get().getStatus() == SubscriberStatus.ACTIVE) {
            // 이미 구독 중
            throw new ApiException(HttpStatus.BAD_REQUEST, "SUBSCRIBER4001", "이미 구독하셨습니다.");
        } else {
            // 새로 구독하기
            subscriber = subscriberRepository.save(Subscriber.subscribeNew(request.email()));
        }

        return SubscribeCreateResponse.from(subscriber);
    }

    @Transactional
    public UnsubscribeResponse unsubscribe(UnsubscribeRequest request) {
        // 구독한 적이 있는지 확인하기
        Subscriber subscriber = subscriberRepository.findByEmail(request.email())
                .orElseThrow(() -> new ApiException(HttpStatus.NOT_FOUND, "UNSUBSCRIBER4041", "구독 중인 회원이 아닙니다."));

        if (subscriber.getStatus() == SubscriberStatus.ACTIVE) {
            // 구독 중일 경우
            Subscriber.unsubscribe(subscriber);
            Unsubscriber unsubscriber = unsubscriberRepository.save(Unsubscriber.create(subscriber, request.reasonCode(), request.detail()));
            return UnsubscribeResponse.from(subscriber, unsubscriber);
        } else {
            // 이미 해지했을 때
            Unsubscriber unsubscriber = unsubscriberRepository.findTopBySubscriberOrderByUnsubscribedAtDesc(subscriber);
            return UnsubscribeResponse.from(subscriber, unsubscriber);
        }
    }
}
