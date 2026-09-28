package com.reservas.controller;

import com.reservas.dto.ConfirmReservationRequest;
import com.reservas.dto.ConfirmReservationResponse;
import com.reservas.dto.CreateReservationRequest;
import com.reservas.dto.CreateReservationResponse;
import com.reservas.dto.ErrorResponse;
import com.reservas.dto.ReservationResponse;
import com.reservas.entity.Event;
import com.reservas.entity.Payment;
import com.reservas.entity.Reservation;
import com.reservas.service.EventService;
import com.reservas.service.ReservationService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/events")
public class ReservationController {

    private final EventService eventService;
    private final ReservationService reservationService;

    public ReservationController(EventService eventService, ReservationService reservationService) {
        this.eventService = eventService;
        this.reservationService = reservationService;
    }

    @PostMapping("/{eventId}/reservations")
    public ResponseEntity<CreateReservationResponse> createReservation(
            @PathVariable Long eventId,
            @RequestBody CreateReservationRequest request) {

        Event event = eventService.getEventById(eventId);
        Reservation reservation = reservationService.createReservation(event, request.getCpf());

        CreateReservationResponse response = new CreateReservationResponse(
                reservation.getId(),
                reservation.getStatus().toString(),
                reservation.getCpf(),
                event.getId(),
                reservation.getCreatedAt()
        );

        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @PostMapping("/reservations/{reservationId}/confirm")
    public ResponseEntity<?> confirmReservation(
            @PathVariable Long reservationId,
            @RequestBody ConfirmReservationRequest request) {

        try {
            Payment.PaymentType paymentType = Payment.PaymentType.valueOf(request.getPaymentType().toUpperCase());
            reservationService.confirmReservation(reservationId, paymentType, request.getPaymentData());

            Reservation reservation = reservationService.getReservationById(reservationId);

            ConfirmReservationResponse response = new ConfirmReservationResponse(
                    reservation.getId(),
                    reservation.getStatus().toString(),
                    reservation.getCpf(),
                    null,
                    null
            );

            return ResponseEntity.ok(response);
        } catch (IllegalArgumentException e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(
                    new ErrorResponse(e.getMessage())
            );
        }
    }

    @GetMapping("/reservations")
    public ResponseEntity<List<ReservationResponse>> listAllReservations() {
        List<ReservationResponse> reservations = reservationService.getAllReservations();
        return ResponseEntity.ok(reservations);
    }
}
