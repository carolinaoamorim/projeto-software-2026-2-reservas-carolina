package com.reservas.service;

import com.reservas.dto.PagamentoDto;
import com.reservas.dto.PagamentoResponseDTO;
import com.reservas.dto.ReservationResponse;
import com.reservas.entity.Event;
import com.reservas.entity.Payment;
import com.reservas.entity.Reservation;
import com.reservas.entity.Ticket;
import com.reservas.repository.PaymentRepository;
import com.reservas.repository.ReservationRepository;
import com.reservas.repository.TicketRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.client.RestClient;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class ReservationService {

    private final ReservationRepository reservationRepository;
    private final PaymentRepository paymentRepository;
    private final TicketRepository ticketRepository;

    public ReservationService(ReservationRepository reservationRepository,
                              PaymentRepository paymentRepository,
                              TicketRepository ticketRepository) {
        this.reservationRepository = reservationRepository;
        this.paymentRepository = paymentRepository;
        this.ticketRepository = ticketRepository;
    }

    @Transactional
    public Reservation createReservation(Event event, String cpf) {
        // Verifica se há ingressos disponíveis
        if (event.getAvailableTickets() <= 0) {
            throw new IllegalArgumentException("Não há ingressos disponíveis");
        }

        // Cria a reserva
        Reservation reservation = new Reservation(event, cpf);
        Reservation savedReservation = reservationRepository.save(reservation);

        // Diminui a quantidade de ingressos disponíveis
        event.setAvailableTickets(event.getAvailableTickets() - 1);

        return savedReservation;
    }

    @Transactional
    public void confirmReservation(Long reservationId, Payment.PaymentType paymentType, String paymentData) {
        Reservation reservation = reservationRepository.findById(reservationId)
                .orElseThrow(() -> new IllegalArgumentException("Reserva não encontrada"));

        if (reservation.getStatus() != Reservation.ReservationStatus.PENDING) {
            throw new IllegalArgumentException("Reserva não está pendente");
        }

        RestClient restClient = RestClient.builder().build();

        PagamentoDto dto = new PagamentoDto();
        dto.setTipo(paymentType.toString());
        dto.setChaveOrigem("123");
        dto.setChaveDestino(paymentData);
        dto.setDataCompra(LocalDate.now());
        dto.setParcelas(1);
        dto.setValor(new BigDecimal(reservation.getEvent().getPrice()));

        PagamentoResponseDTO response = restClient.post()
                .uri("http://localhost:8081/api/pagamentos")
                .body(dto)
                .retrieve()
                .body(PagamentoResponseDTO.class);


        // Cria o pagamento
        //Payment payment = new Payment(reservation, paymentType, paymentData);
       // payment.setStatus(Payment.PaymentStatus.APPROVED);
        //paymentRepository.save(payment);

        // Atualiza o status da reserva
        reservation.setStatus(Reservation.ReservationStatus.CONFIRMED);
        // reservation.setPayment(payment);
        reservationRepository.save(reservation);

        // Cria o ticket confirmado
        Event event = reservation.getEvent();
        Ticket ticket = new Ticket(
                reservation,
                reservation.getCpf(),
                event.getName(),
                event.getEventDate(),
                event.getPrice()
        );
        ticketRepository.save(ticket);
    }

    public Reservation getReservationById(Long reservationId) {
        return reservationRepository.findById(reservationId)
                .orElseThrow(() -> new IllegalArgumentException("Reserva não encontrada"));
    }

    public List<ReservationResponse> getAllReservations() {
        return reservationRepository.findAll()
                .stream()
                .map(this::convertToResponse)
                .collect(Collectors.toList());
    }

    private ReservationResponse convertToResponse(Reservation reservation) {
        Event event = reservation.getEvent();
        String paymentStatus = reservation.getPayment() != null
                ? reservation.getPayment().getStatus().toString()
                : null;

        return new ReservationResponse(
                reservation.getId(),
                event.getId(),
                event.getName(),
                reservation.getCpf(),
                reservation.getStatus().toString(),
                reservation.getCreatedAt(),
                paymentStatus
        );
    }
}
