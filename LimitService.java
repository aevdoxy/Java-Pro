package com.example.limits.service;

import com.example.limits.model.Limit;
import com.example.limits.model.Reservation;
import com.example.limits.repository.LimitRepository;
import com.example.limits.repository.ReservationRepository;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Optional;
import java.util.UUID;

@Service
public class LimitService {

    private final LimitRepository limitRepository;
    private final ReservationRepository reservationRepository;
    private final BigDecimal defaultValue;

    public LimitService(LimitRepository limitRepository, ReservationRepository reservationRepository,
                        @Value("${limits.default-value}") BigDecimal defaultValue) {
        this.limitRepository = limitRepository;
        this.reservationRepository = reservationRepository;
        this.defaultValue = defaultValue;
    }

    @Transactional
    public Limit getOrCreate(String userId) {
        Optional<Limit> opt = limitRepository.findById(userId);
        if (opt.isPresent()) return opt.get();
        Limit l = new Limit(userId, defaultValue);
        l.setUpdatedAt(LocalDateTime.now());
        return limitRepository.save(l);
    }

    @Transactional
    public String reserve(String userId, BigDecimal amount) {
        Limit l = getOrCreate(userId);
        BigDecimal available = l.getAmount().subtract(l.getReserved());
        if (available.compareTo(amount) < 0) {
            throw new IllegalArgumentException("Insufficient limit: available=" + available);
        }
        l.setReserved(l.getReserved().add(amount));
        l.setUpdatedAt(LocalDateTime.now());
        limitRepository.save(l);
        String resId = UUID.randomUUID().toString();
        Reservation r = new Reservation(resId, userId, amount, "PENDING");
        reservationRepository.save(r);
        return resId;
    }

    @Transactional
    public void confirm(String reservationId) {
        Reservation r = reservationRepository.findById(reservationId)
                .orElseThrow(() -> new IllegalArgumentException("Reservation not found"));
        if (!"PENDING".equals(r.getStatus())) throw new IllegalStateException("Reservation not pending");
        Limit l = getOrCreate(r.getUserId());
        // apply
        l.setReserved(l.getReserved().subtract(r.getAmount()));
        l.setAmount(l.getAmount().subtract(r.getAmount()));
        l.setUpdatedAt(LocalDateTime.now());
        limitRepository.save(l);
        r.setStatus("CONFIRMED");
        reservationRepository.save(r);
    }

    @Transactional
    public void cancel(String reservationId) {
        Reservation r = reservationRepository.findById(reservationId)
                .orElseThrow(() -> new IllegalArgumentException("Reservation not found"));
        if (!"PENDING".equals(r.getStatus())) throw new IllegalStateException("Reservation not pending");
        Limit l = getOrCreate(r.getUserId());
        l.setReserved(l.getReserved().subtract(r.getAmount()));
        l.setUpdatedAt(LocalDateTime.now());
        limitRepository.save(l);
        r.setStatus("CANCELLED");
        reservationRepository.save(r);
    }

    @Transactional
    public void restore(String userId, BigDecimal amount) {
        Limit l = getOrCreate(userId);
        l.setAmount(l.getAmount().add(amount));
        l.setUpdatedAt(LocalDateTime.now());
        limitRepository.save(l);
    }

    @Transactional
    public void decreaseDirect(String userId, BigDecimal amount) {
        Limit l = getOrCreate(userId);
        BigDecimal available = l.getAmount().subtract(l.getReserved());
        if (available.compareTo(amount) < 0) throw new IllegalArgumentException("Insufficient limit");
        l.setAmount(l.getAmount().subtract(amount));
        l.setUpdatedAt(LocalDateTime.now());
        limitRepository.save(l);
    }

    @Transactional
    public void resetAllToDefault() {
        for (Limit l : limitRepository.findAll()) {
            l.setAmount(defaultValue);
            l.setReserved(BigDecimal.ZERO);
            l.setUpdatedAt(LocalDateTime.now());
            limitRepository.save(l);
        }
    }
}\n