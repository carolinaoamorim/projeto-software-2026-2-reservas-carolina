package com.reservas.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDate;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class PagamentoDto {
	private String tipo;
	private BigDecimal valor;
	private Integer parcelas;
	private LocalDate dataCompra;
	private String chaveOrigem;
	private String chaveDestino;
	private String numeroCartao;
	private String dataValidade;
	private String cvc;
}
